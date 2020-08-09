package com.squ1dd13.msd.decompiler.disassembler;

import com.squ1dd13.msd.decompiler.low.*;
import com.squ1dd13.msd.shared.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class SCM {
    public List<ReversedCommand> commands = new ArrayList<>();

    public SCM(String filePath) throws IOException {
        int[] bytes = Util.byteArrayToIntArray(Files.readAllBytes(Paths.get(filePath)));

        for(int i = 0; i < bytes.length;) {
            int[] commandBytes = Util.subArray(bytes, i, bytes.length);

            ReversedCommand command = new ReversedCommand(commandBytes);
            command.offset = i;
            if(command.opcode == Integer.MAX_VALUE) break;
            commands.add(command);
            CommandRegistry.getCommand(command.opcode).addReversed(command);

            i += command.length;
        }
    }

    public LowScript toScript() {
        LowScript script = new LowScript();

        for(int i = 0; i < commands.size(); i++) {
            ReversedCommand command = commands.get(i);

            Command realCommand = command.toCommand();
            realCommand.scriptIndex = i;

            script.commands.add(realCommand);
        }

        return script;
    }
}
