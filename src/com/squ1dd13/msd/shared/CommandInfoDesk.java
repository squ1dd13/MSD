package com.squ1dd13.msd.shared;

import java.io.*;
import java.util.*;

public class CommandInfoDesk {
    public static class CommandInfo {
        public LowLevelType[] lowLevelParamTypes;

        // Parses comma-separated strings of low level type codes.
        // Numbers in input should be decimal.
        public static CommandInfo parse(String infoComment) {
            String[] strings = infoComment.strip().split(",");

            CommandInfo info = new CommandInfo();
            info.lowLevelParamTypes = new LowLevelType[strings.length];

            for(int i = 0; i < strings.length; ++i) {
                int typeNum = Integer.parseInt(strings[i]);

                if(typeNum >= LowLevelType.values().length) typeNum = LowLevelType.Unknown.ordinal();
                info.lowLevelParamTypes[i] = LowLevelType.decode(typeNum);
            }

            return info;
        }

        @Override
        public String toString() {
            return "CommandInfo{" +
                "lowLevelParamTypes=" + Arrays.toString(lowLevelParamTypes) +
                '}';
        }
    }

    private static Map<Integer, CommandInfo> infoMap = new HashMap<>();
    private static Map<String, Integer> commandNames = new HashMap<>();

    public static CommandInfo getInfo(int opcode) {
        return infoMap.getOrDefault(opcode, null);
    }

    public static int getOpcode(String name) {
        return commandNames.getOrDefault(name, -1);
    }

    public static void loadFile(String path) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(path));
        try {
            String line;

            while((line = reader.readLine()) != null) {
                String[] parts = line.split(":");

                int opcode = Integer.parseInt(parts[0]);

                infoMap.put(opcode, CommandInfo.parse(parts[1]));
            }
        } finally {
            reader.close();
        }
    }

    public static void loadCommandNames() {
        for(var entry : Command.commands.entrySet()) {
            commandNames.put(entry.getValue().name.replaceAll("\\([^)]*\\)", ""), entry.getKey());
        }
    }
}
