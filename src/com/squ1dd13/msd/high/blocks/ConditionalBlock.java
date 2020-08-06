package com.squ1dd13.msd.high.blocks;

import com.squ1dd13.msd.uni.*;

import java.util.*;

// If/else if/else
public class ConditionalBlock implements CodeBlock {
    // Can improve output quality, but introduces lots of nesting.
    // Not recommended until nesting reduction features implemented.
    public static final boolean addElseBlock = false;

    public boolean isAnd = false;
    public int conditionCount = 0;
    public int consumed = 0;

    List<Command> conditions = new ArrayList<>();
    Command falseJump = null;
    GenericCodeBlock body = new GenericCodeBlock();
    boolean maybeIfElse = false, maybeWhile = false;
    Command elseJump = null;
    GenericCodeBlock elseBody = new GenericCodeBlock();
//    Command trueJump = null;

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

        if(body.commands.isEmpty()) {
            System.out.println("Warning: Allowing empty if body");
//            consumed = 0;
//            return;
        }

        try {
            SingleCommand single = (SingleCommand)body.commands.get(body.commands.size() - 1);
            if(single.command.isJump() && single.command.jumpDest() == testCommand.offset) {
                System.out.println("while");

                body.commands.remove(body.commands.size() - 1);
                maybeWhile = true;
            }
        } catch(Exception ignored) { }

        if(addElseBlock) {
            try {
                SingleCommand single = (SingleCommand)body.commands.get(body.commands.size() - 1);
                if(single.command.isGoto() && single.command.jumpDest() > single.command.offset) {
                    maybeIfElse = true;

                    elseJump = single.command;
                    int elseEnd = Math.abs(elseJump.arguments[0].intValue);

                    System.out.println("jump 1: " + falseJump.offset + "\njump 2: " + elseJump.offset);

                    index++;

                    while(commands.get(index).offset < elseEnd) {
                        BlockFactory.FactoryOutput output = BlockFactory.createBlock(commands, index);
                        elseBody.add(output.block);

                        index += output.consumed;
                    }
                }
            } catch(Exception ignored) { }
        }

        consumed = index - originalIndex;
    }

    @Override
    public List<String> toLineStrings() {
        List<String> lines = new ArrayList<>();

//        if(maybeWhile) {
//            lines.add("// Maybe while loop?");
//        }

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
            lines.add("} else {");

            for(CodeBlock bodyCommand : elseBody.commands) {
                for(String line : bodyCommand.toLineStrings()) {
                    lines.add("$i" + line);
                }
            }
        }

        lines.add("}");

        return lines;
    }
}
