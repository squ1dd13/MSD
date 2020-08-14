package com.squ1dd13.msd.decompiler.high;

import com.squ1dd13.msd.decompiler.*;
import com.squ1dd13.msd.decompiler.high.blocks.*;
import com.squ1dd13.msd.decompiler.shared.*;
import com.squ1dd13.msd.shared.*;

import java.util.*;

// A script that contains blocks of code rather than a basic sequence of commands.
public class HighLevelScript {
    public GenericCodeBlock commandBlock = new GenericCodeBlock();
    public static Map<Integer, String> currentLocalClassMap = new HashMap<>();

    public HighLevelScript(DecompiledScript decompiledScript) {
        List<Command> commands = decompiledScript.commands;//compactJumps(lowScript.commands);
        findVariables(commands);

        currentLocalClassMap = ClassRegistry.generateLocalClassMap(commands);

        BlockFactory.calledAddresses = calledOffsets(commands);

        for(int i = 0; i < commands.size();) {
            BlockFactory.FactoryOutput output = BlockFactory.createBlock(commands, i);
            commandBlock.add(output.block);

            i += output.consumed;
        }
    }

    private static void findVariables(List<Command> commands) {
        for(Command command : commands) {
            DataValue[] arguments = command.arguments;

            for(int i = 0; i < arguments.length; i++) {
                DataValue dataValue = arguments[i];
                if(dataValue.type.isVariable()) {
                    Variable.getOrCreate(dataValue).registerUse(command, i);
                }
            }
        }
    }

    private static Set<Integer> calledOffsets(List<Command> commands) {
        int call = 0x50;

        Set<Integer> offsets = new HashSet<>();
        for(Command command : commands) {
            if(command.opcode == call) {
                offsets.add(command.jumpDest());
            }
        }

        return offsets;
    }

    // Preprocessing step. Turns a jump from A->B->C into a jump from A->C as long as B is unconditional.
    private static List<Command> compactJumps(List<Command> commands) {
        Map<Integer, Command> commandsByOffset = new HashMap<>();
        commands.forEach(c -> commandsByOffset.put(c.offset, c));

        // So we can check for jump tables later.
        Set<Integer> allOpcodes = new HashSet<>();

        for(Command first : commands) {
            allOpcodes.add(first.opcode);

            if(first.isJump()) {
                Command second = commandsByOffset.get(first.jumpDest());

                // A goto is unconditional, whereas a jump may not be.
                if(second.isGoto()) {
                    first.arguments[0].intValue = second.arguments[0].intValue;
                }
            }
        }

        // Step 2 is to remove any old jumps which are now unreachable.
        // We can only do this safely if there are no jump tables in the script.
        if(allOpcodes.contains(0x871) || allOpcodes.contains(0x872)) {
            return commands;
        }

        Set<Integer> jumpDestinations = new HashSet<>();
        for(Command command : commands) {
            if(command.isJump()) {
                jumpDestinations.add(command.jumpDest());
            }
        }

        // No jump tables, so we can safely remove jumps.
        for(int i = 1; i < commands.size(); ++i) {
            Command commandA = commands.get(i - 1);
            Command commandB = commands.get(i);

            if(commandA == null || commandB == null) continue;

            if(commandA.isGoto() && !jumpDestinations.contains(commandB.offset)) {
                System.out.println("dead " + commandB.formattedString());
                commands.set(i, null);
            }
        }

        commands.removeIf(Objects::isNull);

        return commands;
    }

    public void print() {
        String indent = "    ";

        final boolean syntaxHighlight = true;

        for(String line : commandBlock.toLineStrings()) {
            String indented = line.replaceAll("\\$i", indent);

            if(syntaxHighlight) {
                indented = SyntaxHighlight.highlightLine(indented);
            }

            System.out.println(indented);
        }
    }
}
