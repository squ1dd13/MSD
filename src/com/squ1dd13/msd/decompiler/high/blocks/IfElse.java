package com.squ1dd13.msd.decompiler.high.blocks;

import com.squ1dd13.msd.shared.*;

import java.util.*;

public class IfElse implements CodeBlock {
    /*

    If-else:
        if(condition count)
        ... conditions ...
        goto_if_false('else' body)
        ... if body ...
        goto(end)
        ... else body ...
        end

    If-elif-else:
        if(condition count)
        ... conditions ...
        goto_if_false('else if' if command)
        ... if body ...
        goto(end)
        if(else if condition count)
        goto_if_false('else' body)
        ... else if body ...
        goto(end)
        ... else body ...
        end

     */

    private static class IfCommand {
        int conditionCount;
        boolean isAnd;

        public IfCommand(Command cmd) {
            int numType = cmd.arguments[0].intValue;

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
        }
    }

    private final List<Command> conditions;
    private final List<Command> firstBody = new ArrayList<>();
    private final List<IfElse> elifs = new ArrayList<>();

    public IfElse(List<Command> commands, int index) {
        List<Command> commandList = commands.subList(index, commands.size());

        IfCommand ifInfo = new IfCommand(commandList.get(0));
        conditions = new ArrayList<>(ifInfo.conditionCount);

        for(int i = 1; i <= ifInfo.conditionCount; ++i) {
            conditions.add(commandList.get(i));
        }

        int afterConditions = ifInfo.conditionCount + 1;
        Command firstJump = commandList.get(afterConditions);

        if(firstJump.opcode != Opcode.JumpIfFalse.get()) {
            // No jumpIfFalse?
            return;
        }

        int nextPartPosition = firstJump.jumpDest();

        int currentIndex = afterConditions + 1;
        int currentOffset = commandList.get(currentIndex).offset;

        while(currentOffset != nextPartPosition) {
            var cmd = commandList.get(currentIndex++);
            firstBody.add(cmd);
            currentOffset = cmd.offset;
        }

        Command endOfBodyCommand = commandList.get(currentIndex - 1);

        if(endOfBodyCommand.isGoto()) {
            // The next command will tell us whether this is an if, an if-else, or an if-elif-else.
            Command afterBodyCommand = commandList.get(currentIndex);
            int deciderOpcode = afterBodyCommand.opcode;

            if(deciderOpcode == Opcode.Jump.get()) {
                // If-else

            } else if(deciderOpcode == Opcode.If.get()) {
                // If-elif...-else
                elifs.add(new IfElse(commandList, currentIndex));
            } else {
                // If
            }
        } else {
            // ...
        }
    }

    @Override
    public List<String> toLineStrings() {
        return null;
    }
}
