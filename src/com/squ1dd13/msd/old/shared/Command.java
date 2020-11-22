package com.squ1dd13.msd.old.shared;

import com.google.gson.stream.*;
import com.squ1dd13.msd.old.decompiler.shared.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import com.google.gson.*;

public class Command {
    public static final Map<Integer, Command> commands = new HashMap<>();
    public int opcode;
    public int offset;
    public int scriptIndex;
    public String name;
    public DataValue[] arguments;
    private ParamInfo[] paramInfo;

    public String iniRep = "";

    public Command(int opcode, String name) {
        this.opcode = opcode;
        this.name = name;
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

    public static void createBlocklyJSON(String outPath) throws IOException {
        JsonArray array = new JsonArray();

        /*
        {
          "type": "cool_thing",
          "message0": "do thing with this %1 and this %2",
          "args0": [
            {
              "type": "input_value",
              "name": "VALUE",
              "check": "String"
            },
            {
              "type": "input_value",
              "name": "VALUE19",
              "check": "String"
            }
          ],
          "previousStatement": null,
          "nextStatement": null
        }
        */

        for(Command command : commands.values()) {
            JsonObject block = new JsonObject();

            String blockType = "gta_block_" + Integer.toHexString(command.opcode);
            block.add("type", new JsonPrimitive(blockType));

            // TODO: Fetch format string from .ini file.
            StringBuilder messageBuilder = new StringBuilder(command.name);
            JsonArray argArray = new JsonArray();

            int count = CommandRegistry.get(command.opcode).parameterTypes.size();
            for(int i = 0; i < count; ++i) {
                messageBuilder.append('%').append(i + 1).append(", ");

                JsonObject argObject = new JsonObject();
                argObject.add("type", new JsonPrimitive("input_value"));
                argObject.add("name", new JsonPrimitive("input_value_" + Integer.toString(i)));
                // No type checks for now.

                argArray.add(argObject);
            }

            String cleanedName = command.iniRep.strip();
            if(cleanedName.isBlank()) {
                block.add("message0", new JsonPrimitive(messageBuilder.toString()));
            } else {
                cleanedName = cleanedName.replaceAll("%(\\d+)[^%]%", "%$1");
                block.add("message0", new JsonPrimitive(cleanedName.strip()));
            }

            block.add("args0", argArray);
            block.add("previousStatement", JsonNull.INSTANCE);
            block.add("nextStatement", JsonNull.INSTANCE);

            array.add(block);
        }

        Files.writeString(Path.of(outPath), array.toString());
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
        var classOptional = ClassRegistry.classForCommand(this).flatMap(ClassRegistry::getClass);

        if(classOptional.isPresent()) {
            return classOptional.get().createCallString(this);
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
