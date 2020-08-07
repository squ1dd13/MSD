package com.squ1dd13.msd.compiler.constructs.language;

import com.squ1dd13.msd.compiler.constructs.*;
import com.squ1dd13.msd.decompiler.shared.*;
import com.squ1dd13.msd.shared.*;

import java.util.*;

// Only stub classes supported at the moment.
public class ClassConstruct {
    public static class StubMethod {
        public String name;
        public int opcode;
        public LowLevelType[] paramTypes;
        public DataType[] highLevelTypes;

        // TODO: Be stricter with whitespace in regexes.

        public StubMethod(){}

        public static StubMethod fromCommand(Command cmd) {
            try {
                StubMethod stub = new StubMethod();

                stub.paramTypes = CommandInfoDesk.getInfo(cmd.opcode).lowLevelParamTypes;
                stub.highLevelTypes = new DataType[stub.paramTypes.length];
                stub.name = cmd.name.substring(0, cmd.name.indexOf("("));
                stub.opcode = cmd.opcode;

                for(int i = 0; i < stub.highLevelTypes.length; ++i) {
                    stub.highLevelTypes[i] = stub.paramTypes[i].highLevelType();
                }

                return stub;
            } catch(Exception e) {
                return null;
            }
        }

        public Command toCommand() {
            Command command = new Command(opcode, name);

            command.arguments = new DataValue[paramTypes.length];

            for(int i = 0; i < highLevelTypes.length; ++i) {
                var type = highLevelTypes[i];
                command.arguments[i].type = type;
                command.setParamInfo(i, new ParamInfo(type, type.getAbsolute(), true));
            }

            return command;
        }

        public String toDeclaration() {
            final String format = "    stub %s(%s) = (%d, %s);";

            StringBuilder paramBuilder = new StringBuilder();
            StringBuilder dataBuilder = new StringBuilder();

            for(int i = 0; i < highLevelTypes.length; i++) {
                DataType type = highLevelTypes[i];
                paramBuilder.append(Typename.get(type)).append(" param").append(i + 1);

                dataBuilder.append(paramTypes[i].ordinal());

                if(i != highLevelTypes.length - 1) {
                    paramBuilder.append(", ");
                    dataBuilder.append(", ");
                }
            }

            return String.format(format, name, paramBuilder, opcode, dataBuilder);
        }

        @Override
        public String toString() {
            return "StubMethod{" +
                "name='" + name + '\'' +
                ", opcode=" + opcode +
                ", paramTypes=" + Arrays.toString(paramTypes) +
                ", highLevelTypes=" + Arrays.toString(highLevelTypes) +
                '}';
        }
    }

    public String name;
    public List<StubMethod> stubs = new ArrayList<>();

    @Override
    public String toString() {
        StringBuilder declBuilder = new StringBuilder("class ").append(name).append(" {\n");

        for(StubMethod method : stubs) {
            declBuilder.append(method.toDeclaration()).append("\n");
        }

        return declBuilder.append("}").toString();
    }

    // Create a class of stubs with the commands we know.
    public static ClassConstruct buildClass() {
        ClassConstruct theClass = new ClassConstruct();
        theClass.name = "Game";

        for(Command command : Command.commands.values()) {
            var stub = StubMethod.fromCommand(command);
            if(stub == null) continue;
            theClass.stubs.add(stub);
        }

        return theClass;
    }

    public void createCommands() {
        for(StubMethod method : stubs) {
            var command = method.toCommand();
            Command.commands.put(command.opcode, command);
        }
    }
}
