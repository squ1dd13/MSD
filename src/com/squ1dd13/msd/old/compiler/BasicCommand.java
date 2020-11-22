package com.squ1dd13.msd.old.compiler;

import com.squ1dd13.msd.old.compiler.language.*;
import com.squ1dd13.msd.old.shared.*;

import java.util.*;

// Wrapper for the shared Command class that implements more compiler-specific features.
public class BasicCommand implements Compilable {
    public Command highLevel = null;
    public List<Argument> arguments = new ArrayList<>();

    public List<Integer> getOpcodeBytes() {
        int opcode = CommandRegistry.get(highLevel).getOpcode();

        // We need the opcode bytes in little-endian, and then we only want the sublist 0->2
        //  because the opcode is only 2 bytes long.
        return Util.intArrayToList(Util.intToBytesLE(opcode)).subList(0, 2);
    }

    public List<Integer> compile() {
        List<Integer> compiled = new ArrayList<>(getOpcodeBytes());

        for(int i = 0; i < arguments.size(); i++) {
            Argument arg = arguments.get(i);
            arg.type = CommandRegistry.get(highLevel).concreteParamTypes().get(i);

            compiled.addAll(arg.compile());
        }

        return compiled;
    }

    @Override
    public Collection<BasicCommand> toCommands() {
        return new ArrayList<>(List.of(this));
    }

    public static BasicCommand create(int opcode, String name, Argument ...args) {
        BasicCommand cmd = new BasicCommand();
        cmd.highLevel = new Command(
            opcode,
            name
        );

        cmd.arguments = new ArrayList<>(Arrays.asList(args));
        return cmd;
    }

    @Override
    public String toString() {
        String baseName = CommandRegistry.get(highLevel).name;
        StringBuilder builder = new StringBuilder(baseName).append("(");

        for(int i = 0; i < arguments.size(); i++) {
            Argument arg = arguments.get(i);
            builder.append(arg.toString()).append(i == arguments.size() - 1 ? ")" : ", ");
        }

        return builder.toString();
    }
}
