package com.squ1dd13.msd.old.decompiler.high.blocks;

import com.squ1dd13.msd.old.decompiler.shared.*;
import com.squ1dd13.msd.old.misc.gxt.*;
import com.squ1dd13.msd.old.shared.*;

import java.util.*;

// TODO: Un-staticfy BlockFactory.
public class BlockFactory {
    public static Set<Integer> calledAddresses = new HashSet<>();
    private static boolean notInLabel = true;
    private static boolean notInSub = true;

    public static class FactoryOutput {
        public CodeBlock block;
        public int consumed;

        public FactoryOutput(CodeBlock block, int consumed) {
            this.block = block;
            this.consumed = consumed;
        }
    }

    static final boolean produceCompilableOutput = false;

    public static FactoryOutput createBlock(List<Command> commands, int index) {
        return createBlock(commands, index, null);
    }

    public static FactoryOutput createBlock(List<Command> commands, int index, List<String> comments) {
        Command command = commands.get(index);

        if(comments == null) {
            // Look for GXT references in arguments.
            List<String> gxtComments = new ArrayList<>();
            for(DataValue argumentValue : command.arguments) {
                if(argumentValue.type.isString()) {
                    String key = argumentValue.stringValue.strip();

                    int nullIndex = key.indexOf(0);

                    if(nullIndex != -1) key = key.substring(0, nullIndex).strip();

                    if(!GXT.mainGXT.contains(key)) {
                        continue;
                    }

                    String commentString = String.format("%s = %s", key, GXT.mainGXT.get(key));
                    gxtComments.add(commentString);
                }
            }

            var output = createBlock(commands, index, gxtComments);

            if(!gxtComments.isEmpty()) {
                GenericCodeBlock codeBlock = new GenericCodeBlock();

                for(String commentString : gxtComments) {
                    codeBlock.add(new CommentBlock(commentString));
                }

                codeBlock.add(output.block);

                output.block = codeBlock;
                return output;
            }
        }

        if(!produceCompilableOutput) {
            if(command.opcode == 0x50 && calledAddresses.contains(command.jumpDest())) {
                return new FactoryOutput(new CallBlock(command), 1);
            }

            if(notInSub) {
                if(calledAddresses.contains(command.offset)) {
                    notInSub = false;
                    SubroutineBlock subBlock = new SubroutineBlock(commands, index);
                    notInSub = true;

                    return new FactoryOutput(subBlock, subBlock.consumed);
                }
            }
        }

        if(notInLabel) {
            // Check for jumps to this offset.
            for(Command cmd : commands) {
                if(cmd.isJump() && Math.abs(cmd.arguments[0].intValue) == command.offset) {
                    // Stop the next createBlock call looking for a label here.
                    notInLabel = false;

                    FactoryOutput output = createBlock(commands, index);
                    LabelBlock labelBlock = new LabelBlock(command.offset, output.block);

                    notInLabel = true;

                    return new FactoryOutput(labelBlock, output.consumed);
                }
            }
        }

        // Check for 'if' opcode.
        if(command.opcode == 0xD6) {
            ConditionalBlock conditionalBlock = new ConditionalBlock(commands, index);

            if(conditionalBlock.consumed != 0) {
                return new FactoryOutput(conditionalBlock, conditionalBlock.consumed);
            }
        }

//        List<Command> operationCommands = new ArrayList<>();
//
//        int opIndex = index;
//        while(VariableOperation.forCommand(commands.get(opIndex)) != null) {
//            operationCommands.add(commands.get(opIndex++));
//        }
//
//        if(operationCommands.size() > 1) {
//            return new FactoryOutput(new ArithmeticBlock(operationCommands), operationCommands.size());
//        }

        return new FactoryOutput(new SingleCommand(command), 1);
    }
}
