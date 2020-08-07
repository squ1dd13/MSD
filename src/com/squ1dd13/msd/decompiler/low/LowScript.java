package com.squ1dd13.msd.decompiler.low;

import com.squ1dd13.msd.decompiler.shared.*;
import com.squ1dd13.msd.shared.*;

import java.io.*;
import java.util.*;

public class LowScript {
    // Loading a disassembled script (created using the C++ tool).
    // Each line of the file looks like this:
    //
    //  11:5[G38100,F0.000000]
    //  <offset>:<opcode>[<parameters>]
    //
    // Each parameter value has a prefix character that identifies the type.
    // Strings have a prefix and a suffix: "'".
    //
    // Example:
    //
    //  158:188['TRAINS',T20000,B1]
    //
    // Even though there are multiple types of string (8-byte, 16-byte, variable length),
    //  they are all encoded in the same way. The string type is abstracted
    //  because it is useful only when disassembling (to work out offsets).
    //
    // Arrays are represented as bytes separated by '.':
    //
    //  10282:132[A68.150.248.149.3.128,G38208]
    //
    // All numerical values are in decimal format.

    public List<Command> commands = new ArrayList<>();

    public static LowScript load(String path) throws IOException {
        LowScript script = new LowScript();

        try(BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line = null;

            while((line = reader.readLine()) != null) {
                String[] colonSeparated = line.split(":");

                String afterOffset = colonSeparated[1];
                String argumentListStr = afterOffset.substring(afterOffset.indexOf('[') + 1, afterOffset.lastIndexOf(']'));

                boolean hasArgs = !argumentListStr.isBlank();

                // Split by commas that are not between quotes ('').
                String[] argumentStrings = hasArgs ? argumentListStr.strip().split(",(?=(?:[^']*'[^']*')*[^']*$)") : new String[]{};

                int offset = Integer.parseInt(colonSeparated[0]);

                int opcode = Integer.parseInt(colonSeparated[1].substring(0, colonSeparated[1].indexOf('[')));

                var cmd = Command.commands.getOrDefault(opcode, Command.commands.get(opcode & 0x0FFF));
                if(cmd == null) {
                    cmd = new Command(opcode, "unknown_" + Integer.toHexString(opcode) + "()");
                }

                cmd = cmd.copy();

                cmd.offset = offset;
                cmd.scriptIndex = script.commands.size();

                cmd.arguments = new DataValue[argumentStrings.length];
                for(int i = 0; i < cmd.arguments.length; ++i) {
                    cmd.arguments[i] = new DataValue(argumentStrings[i]);
                }

                script.commands.add(cmd);
            }
        }

        return script;
    }

    public void print() {
        for(Command command : commands) {
            System.out.println(command.formattedString());
        }
    }
}
