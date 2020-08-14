package com.squ1dd13.msd.shared;

import com.squ1dd13.msd.compiler.text.parser.*;
import com.squ1dd13.msd.decompiler.high.*;
import com.squ1dd13.msd.decompiler.shared.*;

import java.io.*;
import java.util.*;

public class Command {
    public static final Map<Integer, Command> commands = new HashMap<>();

    private ParamInfo[] paramInfo;

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
                CommandRegistry.addCommand(command);

                commands.put(opcode, command);
            }
        } catch(IOException e) {
            System.out.println("Unable to load command definitions:");
            e.printStackTrace();
        }
    }

    public ParamInfo getParamInfo(int paramIndex) {
        var paramType = CommandRegistry.get(opcode).parameterTypes.get(paramIndex);

        return new ParamInfo(
            paramType.getHighLevel(),
            paramType.getHighLevel().getAbsolute(),
            false,
            paramType.getLowLevel()
        );
    }

    public String argumentString(int i) {
        DataValue arg = arguments[i];

        var str = arg.toString();
        var paramInfo = getParamInfo(i);

        String finalString = str;
        if(paramInfo != null && paramInfo.isOutVal) {
            finalString = "&" + str;
        }

        return finalString;
    }

    public String formattedString() {
        if(arguments.length > 0) {
            if(arguments[0].type.isVariable() || arguments[0].type.isArray()) {
                Optional<String> className;

                if(arguments[0].type.isGlobal()) {
                    className = ClassRegistry.getClassNameForGlobal(arguments[0].intValue);
                } else {
                    className = Optional.ofNullable(HighLevelScript.currentLocalClassMap.get(arguments[0].intValue));
                }

                if(className.isPresent()) {
                    Optional<ClassParser> classObject = ClassRegistry.getClass(className.get());

                    if(classObject.isPresent()) {
                        var callStr = classObject.get().createCallString(this);
                        if(callStr != null) return callStr;
                    }
                }
            }

            var classOptional = ClassRegistry.classForCommand(this).flatMap(ClassRegistry::getClass);

            if(classOptional.isPresent()) {
                return classOptional.get().createCallString(this);
            }
        }

        var operationString = VariableOperation.forCommand(this);
        if(operationString != null) {
            return operationString.toString();
        }

        String mainPart = name.contains("(") ? name.substring(0, name.indexOf('(')) : name;

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
