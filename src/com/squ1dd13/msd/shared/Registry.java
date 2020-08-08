package com.squ1dd13.msd.shared;

import com.squ1dd13.msd.decompiler.disassembler.*;

import java.io.*;
import java.util.*;
import java.util.stream.*;

// Shared information about commands.
public class Registry implements Serializable {
    public static class CommandEntry implements Serializable {
        public static class ParameterType implements Serializable {
            public LowLevelType lowLevel;
            public HighLevelType highLevel, absolute;

            public LowLevelType getLowLevel() {
                return lowLevel;
            }

            public HighLevelType getHighLevel() {
                return highLevel;
            }

            public HighLevelType getAbsolute() {
                return absolute;
            }

            public ParameterType(LowLevelType lowLevelType, HighLevelType absolute) {
                lowLevel = lowLevelType;
                highLevel = lowLevelType.highLevelType();
                this.absolute = lowLevelType.highLevelType();
            }

            public ParameterType(LowLevelType llt) {
                lowLevel = llt;
                highLevel = llt.highLevelType();
            }
        }

        public int opcode;
        public String name;
        public List<ParameterType> parameterTypes = new ArrayList<>();

        public List<LowLevelType> lowLevelParameters() {
            return parameterTypes.stream().map(ParameterType::getLowLevel).collect(Collectors.toList());
        }

        public List<HighLevelType> highLevelParameters() {
            return parameterTypes.stream().map(ParameterType::getHighLevel).collect(Collectors.toList());
        }

        public List<HighLevelType> absoluteParameterTypes() {
            return parameterTypes.stream().map(
                ParameterType::getAbsolute
            ).collect(Collectors.toList());
        }

        // Basic constructor that adds information that is available
        //  from the Command object itself.
        public CommandEntry(Command command) {
            opcode = command.opcode;
            name = command.name.substring(0, command.name.indexOf('('));
        }

        // Adds information from a ReversedCommand (a disassembled command).
        public void addReversed(ReversedCommand reversed) {
            // Disassembled commands provide very reliable type information,
            //  so it's safe to overwrite any existing type information with
            //  new info.
            parameterTypes.clear();

            for(ReversedCommand.ReversedArgument argument : reversed.reversedArguments) {
                parameterTypes.add(new ParameterType(
                    argument.lowLevelType(),
                    argument.lowLevelType().highLevelType().getAbsolute()
                ));
            }
        }
    }

    private final Map<Integer, CommandEntry> commandEntries = new HashMap<>();

    private static Registry shared = null;

    public static void init() {
        if(shared != null) {
            throw new RuntimeException("Only one Registry is allowed");
        }

        shared = new Registry();
    }

    public static void load(String filePath) throws IOException, ClassNotFoundException {
        if(shared != null) {
            throw new RuntimeException("Only one Registry is allowed");
        }

        ObjectInputStream stream = new ObjectInputStream(new FileInputStream(filePath));
        shared = (Registry)stream.readObject();
        stream.close();
    }

    public static void save(String filePath) throws IOException {
        ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(filePath));
        stream.writeObject(shared);
        stream.close();
    }

    public static CommandEntry getCommand(int opcode) {
        return shared.commandEntries.getOrDefault(opcode, null);
    }

    public static CommandEntry getCommand(Command command) {
        CommandEntry entry = getCommand(command.opcode);

        if(entry == null) {
            entry = new CommandEntry(command);
            shared.commandEntries.put(command.opcode, entry);
        }

        return entry;
    }

    public static CommandEntry addCommand(Command command) {
        var entry = new CommandEntry(command);
        shared.commandEntries.put(command.opcode, entry);
        return entry;
    }
}
