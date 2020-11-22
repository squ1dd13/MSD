package com.squ1dd13.msd.old.shared;

import com.squ1dd13.msd.old.decompiler.disassembler.*;

import java.io.*;
import java.util.*;
import java.util.stream.*;

// Shared information about commands.
// TODO: Make CommandRegistry fully static.
public class CommandRegistry implements Serializable {
    public static class CommandEntry implements Serializable {
        public static class ParameterType implements Serializable {
            public ConcreteType lowLevel;
            public AbstractType highLevel, absolute;

            public ConcreteType getLowLevel() {
                return lowLevel;
            }

            public AbstractType getHighLevel() {
                return highLevel;
            }

            public AbstractType getAbsolute() {
                return absolute;
            }

            public ParameterType(ConcreteType concreteType, AbstractType absolute) {
                lowLevel = concreteType;
                highLevel = concreteType.highLevelType();
                this.absolute = concreteType.highLevelType();
            }

            public ParameterType(ConcreteType llt) {
                lowLevel = llt;
                highLevel = llt.highLevelType();
            }
        }

        // Opcodes are weird. No wikis I've found mention this, but some opcodes
        //  (normally condition opcodes) are compiled with an 8 on the front.
        // For example, "is_player_playing" is always documented as having the
        //  opcode 0x256. However, in the actual GTA SCM files, it appears as
        //  0x8256.
        //  MSD's disassembler removes this 8, but we need to know
        //   that it was there.

        // Without extra 8:
        public int opcode;

        // With extra 8:
        public int compiledOpcode = -1;

        public String name;
        public List<ParameterType> parameterTypes = new ArrayList<>();

        public boolean isVariadic = false;

        public List<ConcreteType> concreteParamTypes() {
            return parameterTypes.stream().map(ParameterType::getLowLevel).collect(Collectors.toList());
        }

        public List<AbstractType> highLevelParameters() {
            return parameterTypes.stream().map(ParameterType::getHighLevel).collect(Collectors.toList());
        }

        public List<AbstractType> absoluteParameterTypes() {
            return parameterTypes.stream().map(
                ParameterType::getAbsolute
            ).collect(Collectors.toList());
        }

        // Basic constructor that adds information that is available
        //  from the Command object itself.
        public CommandEntry(Command command) {
            opcode = command.opcode;
            name = command.name.substring(0, command.name.indexOf('('));

            if(command.name.indexOf(')') - command.name.indexOf('(') != 1) {
                int argumentCount = Math.max(1, Util.countOccurrences(command.name, ',') + 1);

                for(int i = 0; i < argumentCount; ++i) {
                    parameterTypes.add(new ParameterType(ConcreteType.Unknown));
                }
            }
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

            if((reversed.originalOpcode & 0xF000) > (compiledOpcode & 0xF000)) {
                compiledOpcode = reversed.originalOpcode;
            }
        }

        public int getOpcode() {
            return compiledOpcode < 0 ? opcode : compiledOpcode;
        }
    }

    private final Map<Integer, CommandEntry> commandEntries = new HashMap<>();
    private final Map<String, Integer> commandNames = new HashMap<>();

    private static CommandRegistry shared = null;

    public static void init() {
        if(shared != null) {
            throw new RuntimeException("Only one CommandRegistry is allowed");
        }

        shared = new CommandRegistry();
    }

    public static void load(String filePath) throws IOException, ClassNotFoundException {
        if(shared != null) {
            throw new RuntimeException("Only one CommandRegistry is allowed");
        }

        ObjectInputStream stream = new ObjectInputStream(new FileInputStream(filePath));
        shared = (CommandRegistry)stream.readObject();
        stream.close();
    }

    public static void save(String filePath) throws IOException {
        ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(filePath));
        stream.writeObject(shared);
        stream.close();
    }

    public static Optional<CommandEntry> getOptional(int opcode) {
        var entry = shared.commandEntries.getOrDefault(opcode, null);
        return Optional.ofNullable(entry);
    }

    public static CommandEntry get(int opcode) {
        var r = shared.commandEntries.getOrDefault(opcode, null);
        if(r == null) {
            return addCommand(new Command(opcode, "unknown_command_" + opcode + "()"));
        }

        return r;
    }

    public static CommandEntry get(Command command) {
        CommandEntry entry = get(command.opcode);

        if(entry == null) {
            entry = new CommandEntry(command);
            shared.commandEntries.put(command.opcode, entry);
            shared.commandNames.put(entry.name, command.opcode);
        }

        return entry;
    }

    public static CommandEntry addCommand(Command command) {
        if(contains(command.opcode)) {
            var newEntry = new CommandEntry(command);
            var currentEntry = get(command);

            currentEntry.name = newEntry.name;
            return currentEntry;
        }

        var entry = new CommandEntry(command);
        shared.commandEntries.put(command.opcode, entry);
        shared.commandNames.put(entry.name, command.opcode);
        return entry;
    }

    public static void addPseudoCommands() {
        var conditionalEntry =
            addCommand(new Command(Opcode.RelativeConditionalJump.get(), "relative_jump_conditional()"));
        var unconditionalEntry =
            addCommand(new Command(Opcode.RelativeUnconditionalJump.get(), "relative_jump_unconditional()"));

        conditionalEntry.parameterTypes.add(new CommandEntry.ParameterType(ConcreteType.S32));
        unconditionalEntry.parameterTypes.add(new CommandEntry.ParameterType(ConcreteType.S32));
    }

    public static boolean contains(int opcode) {
        return shared.commandEntries.containsKey(opcode);
    }

    public static boolean contains(String name) {
        return shared.commandNames.containsKey(name);
    }

    public static int opcodeForName(String name) {
        return shared.commandNames.getOrDefault(name, -1);
    }

    public static CommandEntry get(String name) {
        return get(opcodeForName(name));
    }

    public static Optional<CommandEntry> getOptional(String name) {
        return getOptional(opcodeForName(name));
    }

    public static void loadVariadicInstructions(String filename) throws IOException {
        try(BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while((line = reader.readLine()) != null) {
                getOptional(Integer.parseInt(line, 16)).ifPresent(commandEntry -> {
                    commandEntry.isVariadic = true;
                });
            }
        }
    }

    private static String createEnumMemberName(String commandName) {
        String name = commandName.strip();

        char first = name.charAt(0);
        if(first != '_' || !Character.isAlphabetic(first)) {
            name = "_" + name;
        }

        StringBuilder builder = new StringBuilder();
        for(char c : name.toCharArray()) {
            if(c == '_' || Character.isLetterOrDigit(c)) {
                builder.append(c);
            } else {
                builder.append('_');
            }
        }

        return builder.toString();
    }

    // Writes a C enum of all the command names to a file (useful for loading in decompilers).
    public static void writeCStyleEnum(String filename, String enumName) throws IOException {
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("enum " + enumName + " {\n");

            for(var entry : shared.commandEntries.values()) {
                String name = createEnumMemberName(entry.name);
                String valueString = "0x" + Integer.toHexString(entry.opcode);

                writer.write("    " + name + " = " + valueString + ",\n");
            }

            writer.write("};");
        }
    }

    public static void loadParameterCounts(String filename) throws IOException {
        try(BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while((line = reader.readLine()) != null) {
                if(line.startsWith(";")) continue;

                try {
                    int opcode = Integer.parseInt(line.substring(0, line.indexOf('=')), 16);
                    int count = Integer.parseInt(line.substring(line.indexOf('=') + 1, line.indexOf(',')));

                    if(!contains(opcode)) {
                        addCommand(new Command(opcode, "noname" + opcode));
                    }

                    Command commandObject = Command.commands.getOrDefault(opcode, null);
                    if(commandObject != null) {
                        int end = line.contains(";") ? line.indexOf(";") : line.strip().length();
                        commandObject.iniRep = line.substring(line.indexOf(',') + 1, end).strip();
                    }

                    var optional = getOptional(opcode);

                    if(optional.isPresent()) {
                        var commandEntry = optional.get();

                        commandEntry.parameterTypes = new ArrayList<>();
                        for(int i = 0; i < count; ++i) {
                            commandEntry.parameterTypes.add(new CommandEntry.ParameterType(ConcreteType.Unknown));
                        }
                    } else {
                        System.out.println("bad");
                    }
                } catch(Exception ignored) { }
            }
        }
    }
}