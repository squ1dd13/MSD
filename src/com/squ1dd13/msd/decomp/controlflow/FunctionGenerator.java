package com.squ1dd13.msd.decomp.controlflow;

import com.squ1dd13.msd.decomp.*;
import com.squ1dd13.msd.decomp.generic.*;
import com.squ1dd13.msd.shared.*;

import java.util.*;

/*

    ***** Function endpoints *****

        code        <-- gosub() to here
        code
        code
        return      <-- A
        code        <-- gosub() to here
        code
        return      <-- B
        code
        return      <-- C

    We know return A must belong to the first subprocedure, because there are
    no gosub() calls for offsets between its gosub and the return.

    We know both returns B and C must belong to the second subprocedure, because
    there are no gosub() calls for offsets between B and C.

    "Know" here isn't entirely correct - certain bits of hacky code could use
    a return() in another subprocedure to end the current subprocedure:

        sub a:
        code
        code
        code
        goto(x)

        sub b:
        code
        code
        code
        return  <-- At offset 'x'

    However, this is not something I've ever come across. A simple system
    to identify suspicious jumps like the above could prevent issues with
    trying to decompile weird code.

 */

// Could be part of FlowAnalyzer, but there's a lot of code here.
public class FunctionGenerator {
    private final Map<Integer, Integer> functionSpans = new HashMap<>();
    private final OffsetMap offsets;
    private final List<BasicScript.Instruction> instructions;

    public FunctionGenerator(FlowAnalyzer analyzer) {
        offsets = analyzer.offsets;
        instructions = analyzer.instructions;
    }

    private void logSuspiciousJumps() {
        final Set<Integer> jumpOpcodes = Set.of(
            Opcode.Jump.get(),
            Opcode.JumpIfFalse.get()
        );

        Set<Integer> destinationIndices = new HashSet<>();

        for(BasicScript.Instruction instruction : instructions) {
            if(jumpOpcodes.contains(instruction.opcode)) {
                destinationIndices.add(offsets.toIndex(instruction.getJumpOffset()));
            }
        }

        for(int dest : destinationIndices) {
            for(Map.Entry<Integer, Integer> span : functionSpans.entrySet()) {
                int funcOffset = instructions.get(span.getKey()).offset;

                if(span.getKey() < dest && dest <= span.getValue()) {
                    System.out.printf("suspicious jump into function at %d\n", funcOffset);
                }
            }
        }
    }

    // Find the starts of functions and all returns from them.
    private List<FunctionComponent> findFunctionComponents() {
        List<FunctionComponent> components = new ArrayList<>();

        // So we don't get duplicate functions causing errors:
        Set<Integer> discoveredEntryPoints = new HashSet<>();

        for(BasicScript.Instruction instruction : instructions) {
            // gosub() calls tell us where a function starts (because we get the offset).
            if(instruction.opcode == Opcode.Call.get()) {
                int callIndex = offsets.toIndex(instruction.getJumpOffset());
                if(discoveredEntryPoints.contains(callIndex)) {
                    continue;
                }

                // Prevent "rediscoveries".
                discoveredEntryPoints.add(callIndex);

                components.add(
                    new FunctionComponent(
                        BoundType.Start,

                        // We don't add the index of the gosub() call, we add the index for
                        //  the offset that is passed as an argument.
                        callIndex
                    )
                );
            } else if(instruction.opcode == Opcode.Return.get()) {
                components.add(
                    new FunctionComponent(
                        BoundType.End,
                        offsets.toIndex(instruction.offset)
                    )
                );
            }
        }

        // Sort by index. We have to do this because the gosub() calls are
        //  not in order for the
        components.sort(Comparator.comparing(a -> a.index));

        return components;
    }

    private void addSpan(int start, int end) {
        if(start != -1) {
            if(end != -1) {
                functionSpans.put(start, end);
            } else {
                System.err.println("not adding function with no end index");
            }
        }
    }

    private void createSpans() {
        var components = findFunctionComponents();

        int currentStart = -1, currentEnd = -1;
        for(FunctionComponent component : components) {
            if(component.boundType == BoundType.Start) {
                addSpan(currentStart, currentEnd);

                currentStart = component.index;
                currentEnd = -1;
            } else {
                currentEnd = component.index;
            }
        }

        // Add the last function.
        addSpan(currentStart, currentEnd);
    }

    public int createFunctionIfExists(int offset, List<CodeElement> elements) {
        int index = offsets.toIndex(offset);
        int functionEnd = functionSpans.getOrDefault(index, -1);

        if(functionEnd != -1) {
            Procedure procedure = new Procedure(offset);

            for(int i = index; i <= functionEnd && i < instructions.size(); ++i) {
                procedure.bodyCode.add(instructions.get(i));
            }

            FlowAnalyzer procedureAnalyzer = new FlowAnalyzer(procedure);
            procedureAnalyzer.analyze();

            procedure.bodyCode = procedureAnalyzer.getElements();

            elements.add(procedure);
        }

        return functionEnd;
    }

    public void findFunctions() {
        createSpans();
//        logSuspiciousJumps();
    }

    public enum BoundType {
        Start,
        End
    }

    private static class FunctionComponent {
        final BoundType boundType;
        final int index;

        public FunctionComponent(BoundType boundType, int index) {
            this.boundType = boundType;
            this.index = index;
        }
    }
}
