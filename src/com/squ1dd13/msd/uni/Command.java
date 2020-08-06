package com.squ1dd13.msd.uni;

import java.io.*;
import java.util.*;

public class Command {
    public static final Map<Integer, Command> commands = new HashMap<>();

    private ParamInfo[] paramInfo = null;

    public int opcode;
    public int offset;
    public int scriptIndex;
    public String name;
    public DataValue[] arguments;

    public Command(int opcode, String name) {
        this.opcode = opcode;
        this.name = name;
    }

    public Command copy() {
        Command command = new Command(opcode, name);
        command.offset = offset;
        command.arguments = arguments;
        command.scriptIndex = scriptIndex;
        command.paramInfo = paramInfo;

        return command;
    }

    public boolean isJump() {
        return opcode == 0x2 || opcode == 0x4D;
    }

    public boolean isGoto() {
        return opcode == 0x2;
    }

    public int jumpDest() {
        return Math.abs(arguments[0].intValue);
    }

    // Load command info from a file.
    public static void loadFile(String path) {
        try(BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line = null;

            while((line = reader.readLine()) != null) {
                line = line.strip();
                if(line.startsWith(";") || line.isBlank()) continue;

                if(line.contains(";")) {
                    line = line.split(";")[0].strip();
                }

                String opcodeHex = line.substring(0, 4);
                String name = line.substring(4).strip();

                int opcode = Integer.parseInt(opcodeHex, 16);

                Command command = new Command(opcode, name);
                command.parseParamTypes();

                commands.put(opcode, command);
            }
        } catch(IOException e) {
            System.out.println("Unable to load command definitions:");
            e.printStackTrace();
        }
    }

    private void parseParamTypes() {
        String paramList = name.substring(name.indexOf('(') + 1, name.length() - 1);
        if(paramList.isEmpty()) return;

        String[] typeStrings = paramList.split(",");

        paramInfo = new ParamInfo[typeStrings.length];
        for(int i = 0; i < typeStrings.length; ++i) {
            paramInfo[i] = ParamInfo.fromString(typeStrings[i]);
        }
    }

    public ParamInfo getParamInfo(int paramIndex) {
        if(paramInfo == null || paramInfo.length <= paramIndex) return null;

        return paramInfo[paramIndex];
    }

    public String formattedString() {
        String mainPart = name.substring(0, name.indexOf('('));

        List<String> argStrings = new ArrayList<>();
        for(int i = 0; i < arguments.length; i++) {
            DataValue arg = arguments[i];

            var str = arg.toString();
            var paramInfo = getParamInfo(i);

            String finalString = str;
            if(paramInfo != null && paramInfo.isOutVal) {
                finalString = "&" + str;
            }

            argStrings.add(finalString);
        }

        return mainPart + "(" + String.join(", ", argStrings) + ")";
    }
}
