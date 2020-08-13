package com.squ1dd13.msd.decompiler.high;

import com.squ1dd13.msd.decompiler.shared.*;
import com.squ1dd13.msd.shared.*;

import java.util.*;

/*
Given a variable and class name, this class will find calls that take that variable
as the first argument (the equivalent of the "this" param that gets added to methods in C++).
The called command is then added to a "class" with the given name, which can be
manually refined afterwards.
 */
public class ClassIdentifier {
    private final String className;
    private final List<DataValue> targetVars;
    private final GeneratedClass generated;

    public ClassIdentifier(String name, DataValue... targets) {
        className = name;
        targetVars = Arrays.asList(targets);
        generated = new GeneratedClass(name);
    }

    public void analyzeCommands(List<Command> commands) {
        commands.forEach(this::analyze);
    }

    private boolean isTargeted(DataValue val) {
        return targetVars.stream().anyMatch(val::equalsVariable);
    }

    private void analyze(Command command) {
        if(command.arguments.length < 1) return;
        if(!isTargeted(command.arguments[0])) return;

        generated.methodOpcodes.add(command.opcode);
    }

    public void printClass() {
        System.out.println(generated);
    }

    private static class GeneratedClass {
        public String name;
        public Set<Integer> methodOpcodes = new HashSet<>();

        public GeneratedClass(String className) {
            name = className;
        }

        // TODO: Output generated classes in compilable format.
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder("class " + name + " {\n");

            for(int opcode : methodOpcodes) {
                var registryEntry = CommandRegistry.get(opcode);
                builder.append("    ").append(registryEntry.name).append("(");

                List<AbstractType> types = registryEntry.highLevelParameters();
                for(int i = 1; i < types.size(); ++i) {
                    builder.append(types.get(i));

                    if(i != types.size() - 1) {
                        builder.append(", ");
                    }
                }

                builder.append("); // 0x").append(Integer.toHexString(opcode)).append("\n");
            }

            return builder.append("}").toString();
        }
    }
}
