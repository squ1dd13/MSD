package com.squ1dd13.msd.decompiler.disassembler;

import com.squ1dd13.msd.decompiler.low.*;
import com.squ1dd13.msd.shared.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class CompiledSCM {
    public List<CompiledCommand> commands = new ArrayList<>();

    public CompiledSCM(String filePath) throws IOException {
        int[] bytes = Util.byteArrayToIntArray(Files.readAllBytes(Paths.get(filePath)));

        for(int i = 0; i < bytes.length;) {
            int[] commandBytes = Util.subArray(bytes, i, bytes.length);

            CompiledCommand command = new CompiledCommand(commandBytes);
            command.offset = i;
            if(command.opcode == Integer.MAX_VALUE) break;
            commands.add(command);

            i += command.length;
        }
    }

    public LowScript toScript() {
        LowScript script = new LowScript();

        for(int i = 0; i < commands.size(); i++) {
            CompiledCommand command = commands.get(i);

            Command realCommand = command.toCommand();
            realCommand.scriptIndex = i;

            script.commands.add(realCommand);
        }

        return script;
    }
}
