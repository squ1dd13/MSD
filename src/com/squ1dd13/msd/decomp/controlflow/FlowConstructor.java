package com.squ1dd13.msd.decomp.controlflow;

import com.squ1dd13.msd.*;
import com.squ1dd13.msd.decomp.*;
import com.squ1dd13.msd.decomp.generic.*;
import com.squ1dd13.msd.old.shared.*;

import java.util.*;
import java.util.stream.*;

import static com.squ1dd13.msd.decomp.BasicScript.*;

public class FlowConstructor {
    public OffsetMap offsets;
    public List<Instruction> instructions;
    private List<CodeElement> elements = new ArrayList<>();
    private final Map<Integer, Integer> functionSpans = new HashMap<>();
    private final FunctionGenerator functionGenerator;
    public boolean canGenerateFunctions = true;

    public FlowConstructor(CodeContainer<?> container) {
        instructions = container.getBodyInstructions();
        offsets = new OffsetMap(container);
        functionGenerator = new FunctionGenerator(this);
    }

    public static void createStructures(CodeContainer<CodeElement> container) {
        if(container == null) return;

        FlowConstructor analyzer = new FlowConstructor(container);
        analyzer.build();
        container.bodyCode = analyzer.elements;
    }

    // Returns the index of the first instruction after the loop.
    private int createDoWhile(int index, List<Instruction> conditions) {
        // We start at the index of the jump. The body starts at the jump location,
        //  and ends at the 'if' that executes the jump. We need to backtrack to find
        //  the 'if'.
        Instruction jump = instructions.get(index);

        int bodyEndIndex = -1;
        for(int i = index; i >= 0; --i) {
            Instruction maybeIf = instructions.get(i);

            if(maybeIf.opcode == Opcode.If.get()) {
                bodyEndIndex = i;
                break;
            }
        }

        if(bodyEndIndex == -1) {
            System.err.println("did not find 'if'");
            return -1;
        }

        // The jump is a jump to the start of the body.
        int bodyIndex = offsets.toIndex(jump.getJumpOffset());

        // Now we have the bounding indices of the loop's body, we can construct the loop.
        Instruction ifInstruction = instructions.get(bodyEndIndex);
        WhileLoop loop = new WhileLoop(ifInstruction);
        loop.isDoWhile = true;
//        loop.conditions = conditions;

        for(int i = bodyEndIndex + 1; i < instructions.size() && i < loop.conditionCount; ++i) {
            loop.addCondition(instructions.get(i));//conditions.add(instructions.get(i));
        }

        for(int i = bodyIndex; i < bodyEndIndex && i < instructions.size(); ++i) {
            loop.bodyCode.add(instructions.get(i));
        }

        elements.add(loop);

        // jump + 1
        return index + 1;
    }

    private int createElseForConditional(int bodyStart, Conditional conditional) {
        /*

        if statement
        body
        jump past end of 'else'
        else body              <-- We start here
        location jumped to

         */

        Instruction elseBodyJump = instructions.get(bodyStart - 1);

        if(elseBodyJump.opcode != Opcode.Jump.get()) {
            return -1;
        }

        int elseEndOffset = elseBodyJump.getJumpOffset();
        int elseEndIndex = offsets.toIndex(elseEndOffset);

        if(elseEndIndex <= bodyStart) return -1;

        // Construct the 'else' block.
        Conditional.ElseBlock block = new Conditional.ElseBlock();

        for(int i = bodyStart; i < elseEndIndex; ++i) {
            block.bodyCode.add(instructions.get(i));
        }

        conditional.elseBlock = block;

        // Remove the jump from the end of the 'if' body.
        conditional.bodyCode.remove(conditional.bodyCode.size() - 1);

        return elseEndIndex;
    }

    private int createSwitch(int index) {
        // HACK: This code is terrible and almost certainly won't work for switches with 'default' cases.
        Instruction instruction = instructions.get(index);
        Switch.Info info = new Switch.Info(instruction);

        Switch theSwitch = new Switch();
        theSwitch.switchValue = info.switchValue;

        for(int k = 0; k < info.caseOffsets.size(); k++) {
            int offset = info.caseOffsets.get(k);
            Switch.Case theCase = new Switch.Case();
            theCase.value = info.caseValues.get(offset);

            int caseStartIndex = offsets.toIndex(offset);
            int caseEndOffset = k == info.caseOffsets.size() - 1 ? info.defaultCaseOffset : info.caseOffsets.get(k + 1);
            int caseEndIndex = offsets.toIndex(caseEndOffset);

            for(int j = caseStartIndex; j < caseEndIndex && j < instructions.size(); ++j) {
                var caseInstr = instructions.get(j);
                if(caseInstr.opcode == Opcode.Jump.get() && caseInstr.getJumpOffset() == info.defaultCaseOffset) {
                    RenamedInstruction breakInstruction = new RenamedInstruction(caseInstr);
                    breakInstruction.setOutputString("break");
                    theCase.bodyCode.add(breakInstruction);
                    continue;
                }

                theCase.bodyCode.add(caseInstr);
            }

            FlowConstructor caseAnalyzer = new FlowConstructor(theCase);
            caseAnalyzer.build();

            theCase.bodyCode = caseAnalyzer.elements;

            theSwitch.addCase(info.caseValues.get(offset), theCase);
        }

        int endIndex = offsets.toIndex(info.defaultCaseOffset);

        elements.add(theSwitch);

        return endIndex;
    }

    private void buildStructuresNice() {

    }

    private void buildStructures() {
        int numInstructions = instructions.size();

        // TODO: Clean up this mess...
        for(int i = 0; i < numInstructions; ++i) {
            Instruction instruction = instructions.get(i);

            if(canGenerateFunctions) {
                int funcResult = functionGenerator.createFunctionIfExists(instruction.offset, elements);
                if(funcResult != -1) {
                    i = funcResult;
                    continue;
                }
            }

            boolean shouldContinue = false;
            ifStatementCheck:
            if(instruction.opcode == Opcode.If.get()) {
                int j = i;
                Conditional conditional = new Conditional(instruction);
                conditional.conditions = new ArrayList<>();
                conditional.isLabel = instruction.isLabel;

                // Find the end-of-body jump.
                for(++j; j < numInstructions; ++j) {
                    Instruction jump = instructions.get(j);

                    if(jump.opcode == Opcode.JumpIfFalse.get()) {
                        int endOfBodyOffset = Math.abs((int)jump.arguments.get(0).intValue);

                        int endOfBodyIndex = offsets.toIndex(endOfBodyOffset);
                        if(endOfBodyIndex == -1) break ifStatementCheck;
                        if(endOfBodyIndex < j) {
                            int endIndex = createDoWhile(j, conditional.conditions);
                            if(endIndex == -1) {
                                break;
                            }
                            i = endIndex - 1;

                            j = Integer.MAX_VALUE;
                            shouldContinue = true;
                            break ifStatementCheck;
                        }

                        for(++j; j < endOfBodyIndex && j < numInstructions; ++j) {
                            conditional.bodyCode.add(instructions.get(j));
                        }

                        var maybeJumpToStart = instructions.get(endOfBodyIndex - 1);
                        if(maybeJumpToStart.opcode == Opcode.Jump.get()) {
                            if(maybeJumpToStart.getJumpOffset() == instruction.offset) {
                                conditional = new WhileLoop(conditional);

                                // Remove the jump from the end of the body.
                                conditional.bodyCode.remove(conditional.bodyCode.size() - 1);
                            }
                        }


                        break;
                    } else {
                        conditional.conditions.add(jump);
                    }
                }

                if(j < numInstructions) {
                    // Success
                    i = j - 1;

                    int elseResult = createElseForConditional(i + 1, conditional);
                    if(elseResult != -1) {
                        i = elseResult - 1;

                        createStructures(conditional.elseBlock);
                    }

                    createStructures(conditional);
                    conditional.invertIfNeeded();

                    elements.add(conditional);

                    continue;
                }

                System.out.println("Failed to create conditional");

                // Fail, so just interpret as a normal instruction.
            } else if(instruction.opcode == Opcode.Switch.get()) {
                i = createSwitch(i) - 1;
                continue;
            }

            if(shouldContinue) continue;
            elements.add(instruction);
        }
    }

    private void createLabels() {
        final Set<Integer> jumpOpcodes = Set.of(
            Opcode.Jump.get(),
            Opcode.JumpIfFalse.get()
        );

        for(BasicScript.Instruction instruction : instructions) {
            if(jumpOpcodes.contains(instruction.opcode)) {
                int dest = offsets.toIndex(instruction.getJumpOffset());
                if(dest == -1) continue;
                instructions.get(dest).isLabel = true;
            }
        }
    }

    private void removeDuplicateOffsets() {
        // Some code elements are constructed after elements they contain have already been processed.
        // To stop code appearing twice, we remove any structure that contains an instruction at an offset
        //  that has already been included in another structure. Newer (towards the end of the list) elements
        //  take precedence over older ones.

        // HACK: This entire system is basically just a hack to get around the fact that
        //  the structure identification algorithms add instructions before checking if they're
        //  part of a larger structure. It will also probably fail horribly in some edge cases.

        List<CodeElement> cleanedElements = new ArrayList<>();

        Set<Integer> usedOffsets = new HashSet<>();
        for(int i = elements.size() - 1; i >= 0; --i) {
            var offsets = elements.get(i).getInstructionOffsets();

            boolean containsUsed = false;
            for(int offset : offsets) {
                if(usedOffsets.contains(offset)) {
                    containsUsed = true;
                    break;
                }
            }

            if(containsUsed) {
                continue;
            }

            cleanedElements.add(elements.get(i));
            usedOffsets.addAll(offsets);
        }

        // We iterated backwards, so we have to reverse the cleaned elements.
        Collections.reverse(cleanedElements);

        elements = cleanedElements;
    }

    public void build() {
        if(!Config.decompiler.buildHighLevelStructures) {
            elements = instructions.stream().map(instr -> (CodeElement)instr).collect(Collectors.toList());
            return;
        }

        createLabels();
        if(canGenerateFunctions) functionGenerator.findFunctions();
        buildStructures();
        removeDuplicateOffsets();
    }

    public FlowScript createScript() {
        return new FlowScript(elements);
    }

    public List<CodeElement> getElements() {
        return elements;
    }
}
