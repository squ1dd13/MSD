package com.squ1dd13.msd.compiler.assembly;

import com.squ1dd13.msd.compiler.constructs.*;
import com.squ1dd13.msd.compiler.constructs.language.*;
import com.squ1dd13.msd.shared.*;

import java.io.*;
import java.util.*;
import java.util.stream.*;

public class CompiledScript {
    public List<Compilable> elements = new ArrayList<>();

    // Change a relative jump to an actual jump.
    private static LowLevelCommand swapJump(LowLevelCommand rel, int offset) {
        // We need the size of the jump to calculate the right destination offset.
        final int compiledJumpSize = 7;

        int realOffset = -(rel.arguments.get(0).intValue + compiledJumpSize + offset);
        int opcode = rel.command.opcode == Opcode.RelativeConditionalJump.get()
            ? Opcode.JumpIfFalse.get()
            : Opcode.Jump.get();

        return LowLevelCommand.create(
            opcode,
            Command.commands.get(opcode).name,
            new Argument(LowLevelType.S32, realOffset)
        );
    }

    public void compileAndWrite(String filePath) throws IOException {
        Context ctx = new Context();
        ctx.compilingStreamedScript = true;

        FileOutputStream stream = new FileOutputStream(filePath);

        List<LowLevelCommand> allCommands = elements.stream().map(
            e -> e.toCommands(ctx)
        ).flatMap(Collection::stream).collect(Collectors.toList());

        int offset = 0;
        for(LowLevelCommand command : allCommands) {
            List<Integer> bytes = new ArrayList<>();

            if(command.command.opcode == Opcode.RelativeConditionalJump.get()
                || command.command.opcode == Opcode.RelativeUnconditionalJump.get()) {
                bytes.addAll(swapJump(command, offset).compile(ctx));
            } else {
                bytes.addAll(command.compile(ctx));
            }

            offset += bytes.size();

            for(int b : bytes) {
                stream.write(b);
            }
        }

        stream.close();
    }
}
