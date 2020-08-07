package com.squ1dd13.msd.compiler.text;

import com.squ1dd13.msd.compiler.assembly.*;
import com.squ1dd13.msd.compiler.constructs.language.*;
import com.squ1dd13.msd.shared.*;

import java.io.*;
import java.util.*;
import java.util.regex.*;

public class Parser {
    public static Argument readArgument(StringReader reader) throws IOException {
        StringBuilder cleanArgument = new StringBuilder();
        boolean insideString = false;

        int lastChar = -1;

        int c;
        while((c = reader.read()) != -1) {
            // So we can skip without setting lastChar:
            int last = lastChar;
            lastChar = c;

            if(!insideString && Character.isWhitespace(c)) continue;
            if(!insideString && c == ',') break;
            if(c == '\'' && last != '\\') insideString = !insideString;

            cleanArgument.append((char)c);
        }

        String argStr = cleanArgument.toString().strip();

        // References are purely syntactical.
        if(argStr.startsWith("&")) {
            argStr = argStr.substring(1);
        }

        // The first regex will match invalid float literals (missing 'f'), as well as all other numbers.
        if(argStr.matches("-?\\d+(?:\\.\\d+f?)?")) {
            if(argStr.matches("-?\\d+\\.\\d+")) {
                Util.emitWarning("Float literals should end with 'f' (e.g. \"12.34f\").");
                argStr += 'f';
            }

            if(argStr.endsWith("f")) {
                return new Argument(LowLevelType.F32, Float.parseFloat(Util.cropString(argStr)));
            }

            // We don't know the correct integer type to use.
            return new Argument(LowLevelType.Unknown, Integer.parseInt(argStr));
        }

        if(argStr.startsWith("'") && argStr.endsWith("'")) {
            return new Argument(LowLevelType.Unknown, Util.insetString(argStr));
        }

        String localGlobalPattern = "(?:(?:local)|(?:global))[^_]+_(\\d+)";

        if(argStr.matches(localGlobalPattern)) {
            var matcher = Pattern.compile(localGlobalPattern).matcher(argStr);

            if(matcher.matches()) {
                int offset = Integer.parseInt(matcher.toMatchResult().group(1));
                return new Argument(LowLevelType.Unknown, offset);
            }
        }

        if(argStr.matches("[A-Za-z][\\w$]*(\\.[\\w$]+)?(\\[\\d+])?")) {
            Util.emitFatalError("Custom variables are not yet supported.");
        }

        return null;
    }

    public static String readUntil(StringReader reader, String endChars) throws IOException {
        StringBuilder builder = new StringBuilder();

        boolean insideString = false;

        int lastChar = -1;

        int c;
        while((c = reader.read()) != -1) {
            // So we can skip without setting lastChar:
            int last = lastChar;
            lastChar = c;

            builder.append((char)c);
            if(c == '\'' && last != '\\') insideString = !insideString;

            if(!insideString && endChars.contains(Character.toString(c))) break;
        }

        return builder.toString();
    }

    public static LowLevelCommand readCommand(StringReader reader) throws IOException {
        boolean gotName = false;
        StringBuilder nameBuilder = new StringBuilder();

        String name = "";
        List<Argument> parsedArgs = new ArrayList<>();

        // set_var_int(&globalInt_38092, 0)
        int c;

        while((c = reader.read()) != -1) {
            if(c == '(') {
                gotName = true;
                name = nameBuilder.toString();

                if(name.startsWith("0x")) {
                    // TODO: Allow unknown commands.
                    Util.emitFatalError("not yet");
                } else if(CommandInfoDesk.getOpcode(name) < 0) {
                    Util.emitFatalError("Unknown command name '" + name + "'.\n" +
                        "If you know the opcode, you may use it instead of the name like this: 0x123(...)");
                }
            }

            if(!gotName) {
                nameBuilder.append((char)c);
            } else {
                break;
            }
        }

        String argList = Util.cropString(readUntil(reader, ")"));

        LowLevelCommand command = new LowLevelCommand();
        int opcode = CommandInfoDesk.getOpcode(name);

        if(!argList.isBlank()) {
            String[] argStrs = argList.split(",");

            for(String s : argStrs) {
                parsedArgs.add(readArgument(new StringReader(s)));
            }

            command.arguments = parsedArgs;

            CommandInfoDesk.CommandInfo info = CommandInfoDesk.getInfo(opcode);
            if(info == null || command.arguments.size() != info.lowLevelParamTypes.length) {
                System.out.println("no info");
            }
        }

        command.command = Command.commands.get(opcode);

        return command;
    }

    public CompiledScript parse(List<String> lines) throws IOException {
        CompiledScript script = new CompiledScript();

        for(String line : lines) {
            script.elements.add(readCommand(new StringReader(line)));
        }

        return script;
    }
}
