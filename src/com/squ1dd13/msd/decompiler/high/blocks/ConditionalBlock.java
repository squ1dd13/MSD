package com.squ1dd13.msd.decompiler.high.blocks;

import com.squ1dd13.msd.compiler.language.*;
import com.squ1dd13.msd.shared.*;

import java.util.*;

// If/else if/else
public class ConditionalBlock implements CodeBlock {
    // Can improve output quality, but introduces lots of nesting.
    // Not recommended until nesting reduction features implemented.
    public static final boolean addElseBlock = true;

    public boolean isAnd;
    public int conditionCount;
    public int consumed;

    List<Command> conditions = new ArrayList<>();
    Command falseJump;
    GenericCodeBlock body = new GenericCodeBlock();
    boolean maybeIfElse, maybeWhile;
    Command elseJump;
    GenericCodeBlock elseBody = new GenericCodeBlock();

    public ConditionalBlock(List<Command> commands, int index) {
        int originalIndex = index;

        Command testCommand = commands.get(index++);

        int numType = testCommand.arguments[0].intValue;

        if(numType == 0) {
            // Type 0 = 1 condition
            conditionCount = 1;
        } else if(numType < 8) {
            // Types 1->7 = 2->8 conditions and combination AND.
            isAnd = true;
            conditionCount = numType + 1;
        } else if(numType < 28) {
            // Types 21->27 = 2->8 conditions and combination OR.
            isAnd = false;
            conditionCount = numType - 19;
        }

        for(int i = 0; i < conditionCount; ++i) {
            conditions.add(commands.get(index++));
        }

        // There should be a jump past the body of the conditional.
        // The opcode should be 0x4D (jumpIfFalse).
        Command bodyJump = commands.get(index++);
        if(bodyJump.opcode != 0x4D) {
            System.out.println("Cannot process if statement.");
            return;
        }

        falseJump = bodyJump;
        int bodyEnd = Math.abs(falseJump.arguments[0].intValue);

        while(commands.get(index).offset < bodyEnd) {
            BlockFactory.FactoryOutput output = BlockFactory.createBlock(commands, index);
            body.add(output.block);

            index += output.consumed;
        }

        // TODO: Add compiler support for 'while' loops.
        try {
            SingleCommand single = (SingleCommand)body.commands.get(body.commands.size() - 1);
            if(single.command.isJump() && single.command.jumpDest() == testCommand.offset) {
//                System.out.println("while");

                body.commands.remove(body.commands.size() - 1);
                maybeWhile = true;
            }
        } catch(Exception ignored) {
        }

        if(addElseBlock) {
            try {
                SingleCommand single = (SingleCommand)body.commands.get(body.commands.size() - 1);
                if(single.command.isGoto() && single.command.jumpDest() > single.command.offset) {
                    maybeIfElse = true;

                    elseJump = single.command;
                    int elseEnd = Math.abs(elseJump.arguments[0].intValue);

//                    System.out.println("jump 1: " + falseJump.offset + "\njump 2: " + elseJump.offset);

//                    index++;

                    while(commands.get(index).offset < elseEnd) {
                        BlockFactory.FactoryOutput output = BlockFactory.createBlock(commands, index);
                        elseBody.add(output.block);

                        index += output.consumed;
                    }
                }
            } catch(Exception ignored) {
            }
        }

        consumed = index - originalIndex;
    }

    @Override
    public List<String> toLineStrings() {
        List<String> lines = new ArrayList<>();

        StringBuilder mainLineBuilder = new StringBuilder(maybeWhile ? "while(" : "if(");

        for(int i = 0; i < conditions.size(); i++) {
            Command command = conditions.get(i);
            mainLineBuilder.append(command.formattedString());

            if(i != conditions.size() - 1) {
                mainLineBuilder.append(isAnd ? " and " : " or ");
            }
        }

        lines.add(mainLineBuilder.append(") {").toString());

        for(CodeBlock bodyCommand : body.commands) {
            for(String line : bodyCommand.toLineStrings()) {
                lines.add("$i" + line);
            }
        }

        if(maybeIfElse) {
            if(elseBody.commands.get(0) instanceof ConditionalBlock) {
                List<String> elseIfLines = new ArrayList<>();

                for(CodeBlock bodyCommand : elseBody.commands) {
                    elseIfLines.addAll(bodyCommand.toLineStrings());
                }

                lines.add("} else " + elseIfLines.get(0));

                // Skip the first line and the last line.
                // If we don't skip the last line, we get loads of closing
                //  braces at the end.
                for(int i = 1; i < elseIfLines.size() - 1; ++i) {
                    lines.add(elseIfLines.get(i));
                }

                // FIXME: ConditionalBlock is nasty and hacky. It also prints the goto() at the end of elif bodies.
            } else {
                lines.add("} else {");

                for(CodeBlock bodyCommand : elseBody.commands) {
                    for(String line : bodyCommand.toLineStrings()) {
                        lines.add("$i" + line);
                    }
                }
            }
        }

        lines.add("}");

        return lines;
    }
}
