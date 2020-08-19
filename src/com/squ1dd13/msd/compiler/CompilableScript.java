package com.squ1dd13.msd.compiler;

import com.squ1dd13.msd.compiler.language.*;
import com.squ1dd13.msd.shared.*;

import java.io.*;
import java.util.*;
import java.util.stream.*;

public class CompilableScript {
    public List<Compilable> elements = new ArrayList<>();

    // Change a relative jump to an actual jump.
    private static BasicCommand swapJump(BasicCommand rel, int offset) {
        // We need the size of the jump to calculate the right destination offset.
        final int compiledJumpSize = 7;

        int realOffset = -(rel.arguments.get(0).intValue + compiledJumpSize + offset);
        int opcode = rel.highLevel.opcode == Opcode.RelativeConditionalJump.get()
            ? Opcode.JumpIfFalse.get()
            : Opcode.Jump.get();

        return BasicCommand.create(
            opcode,
            Command.commands.get(opcode).name,
            new Argument(ConcreteType.S32, realOffset)
        );
    }

    public void compileAndWrite(String filePath) throws IOException {
        FileOutputStream stream = new FileOutputStream(filePath);

        List<BasicCommand> allCommands = elements.stream().map(
            Compilable::toCommands
        ).flatMap(Collection::stream).collect(Collectors.toList());

        int offset = 0;
        for(BasicCommand command : allCommands) {
            BasicCommand finalCommand = command;
            if(command.highLevel.opcode == Opcode.RelativeConditionalJump.get()
                || command.highLevel.opcode == Opcode.RelativeUnconditionalJump.get()) {
                finalCommand = swapJump(command, offset);
            }

            System.out.println(offset + ": " + command.toString());
            List<Integer> bytes = new ArrayList<>(finalCommand.compile());

            offset += bytes.size();

            for(int b : bytes) {
                stream.write(b);
            }
        }

        stream.close();
    }
}
