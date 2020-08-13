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
        targetVars = new ArrayList<>(Arrays.asList(targets));
        generated = new GeneratedClass(name);
    }

    public void analyzeCommands(List<Command> commands) {
        while(true) {
            boolean shouldContinue = false;
            for(Command command : commands) {
                // If another instance was discovered, we need to start again.
                if(!analyze(command)) {
                    shouldContinue = true;
                    break;
                }
            }

            if(shouldContinue) continue;
            break;
        }
    }

    private boolean isTargeted(DataValue val) {
        return targetVars.stream().anyMatch(val::equalsVariable);
    }

    // Returns false if the calling loop (if there is one) should be restarted.
    // That should happen when another instance of the class is discovered,
    //  meaning that methods may have been missed.
    private boolean analyze(Command command) {
        if(command.arguments.length < 1) return true;
        if(!isTargeted(command.arguments[0])) {
            // If this is a method opcode, we've found another instance of this class.
            if(generated.methodOpcodes.contains(command.opcode)) {
                targetVars.add(command.arguments[0]);

                // Go back to find any methods we've missed.
                return false;
            } else {
                return true;
            }
        }

        generated.methodOpcodes.add(command.opcode);
        return true;
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

        private String cleverName(String commandName) {
            // Since the commands.ini names aren't method names, they
            //  often contain the name of the class in them (because
            //  you have to specify that in a non object-oriented
            //  situation). We can remove that to get closer to a
            //  real method name.
            //
            // set_menu_item_with_number -> setItemWithNumber

            // We need to replace double underscores with single ones after removing
            //  the class name (otherwise you get things like "set__property").
            String methodName = commandName.toLowerCase().replace(name.toLowerCase(), "").replace("__", "_");

            StringBuilder camelCaseBuilder = new StringBuilder();

            boolean lastWasUnderscore = false;
            for(char c : methodName.toCharArray()) {
                if(c == '_') {
                    lastWasUnderscore = true;
                    continue;
                }

                char character = c;
                if(lastWasUnderscore) {
                    character = Character.toUpperCase(character);
                    lastWasUnderscore = false;
                }

                camelCaseBuilder.append(character);
            }

            return camelCaseBuilder.toString();
        }

        // TODO: Output generated classes in compilable format.
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder("class " + name + " {\n");

            List<Integer> sortedOpcodes = new ArrayList<>(methodOpcodes);
            sortedOpcodes.sort(Integer::compareTo);

            for(int j = 0; j < sortedOpcodes.size(); j++) {
                int opcode = sortedOpcodes.get(j);

                var registryEntry = CommandRegistry.get(opcode);

                builder.append("    [0x").append(Integer.toHexString(opcode).toUpperCase()).append("]\n");

                String returnTypeName;
                String methodName;

                if(registryEntry.name.startsWith("?")) {
                    returnTypeName = "bool";
                    methodName = registryEntry.name.substring(1);
                } else {
                    returnTypeName = "void";
                    methodName = registryEntry.name;
                }

                methodName = cleverName(methodName);

                builder.append("    ").append(returnTypeName).append(' ').append(methodName).append('(');

                List<AbstractType> types = registryEntry.highLevelParameters();
                for(int i = 1; i < types.size(); ++i) {
                    builder.append(types.get(i)).append(" p").append(i);

                    if(i != types.size() - 1) {
                        builder.append(", ");
                    }
                }

                builder.append(");\n");

                if(j != sortedOpcodes.size() - 1) {
                    builder.append('\n');
                }
            }

            return builder.append("}").toString();
        }
    }
}
