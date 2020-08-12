package com.squ1dd13.msd.shared;

import com.squ1dd13.msd.decompiler.disassembler.*;

import java.io.*;
import java.util.*;
import java.util.stream.*;

// Shared information about commands.
// TODO: Make CommandRegistry fully static.
public class CommandRegistry implements Serializable {
    public static class CommandEntry implements Serializable {
        public static class ParameterType implements Serializable {
            public LowLevelType lowLevel;
            public AbstractType highLevel, absolute;

            public LowLevelType getLowLevel() {
                return lowLevel;
            }

            public AbstractType getHighLevel() {
                return highLevel;
            }

            public AbstractType getAbsolute() {
                return absolute;
            }

            public ParameterType(LowLevelType lowLevelType, AbstractType absolute) {
                lowLevel = lowLevelType;
                highLevel = lowLevelType.highLevelType();
                this.absolute = lowLevelType.highLevelType();
            }

            public ParameterType(LowLevelType llt) {
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

        public List<LowLevelType> lowLevelParameters() {
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

        conditionalEntry.parameterTypes.add(new CommandEntry.ParameterType(LowLevelType.S32));
        unconditionalEntry.parameterTypes.add(new CommandEntry.ParameterType(LowLevelType.S32));
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
}