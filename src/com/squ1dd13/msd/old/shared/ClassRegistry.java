package com.squ1dd13.msd.old.shared;

import com.squ1dd13.msd.old.compiler.text.lexer.*;
import com.squ1dd13.msd.old.compiler.text.parser.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class ClassRegistry {
    private static final Map<String, ParsedClass> parsedClasses = new HashMap<>();
    private static final Map<Integer, String> globalVariableClasses = new HashMap<>(Map.of(
        12, "Character",
        38160, "Character",
        9692, "Menu",
        9684, "Menu",
        9676, "Menu",
        9668, "Menu",
        8, "Player",
        38096, "Vehicle"
    ));
    private static final Map<Integer, String> opcodesAndClasses = new HashMap<>();

    public static void loadClass(String classPath) throws IOException {
        var tokens = ParserUtils.filterBlankTokens(Lexer.lex(Files.readString(Paths.get(classPath))));

        ParsedClass parser = new ParsedClass(tokens.iterator());
        if(parsedClasses.containsKey(parser.name)) {
            // TODO: Implement extension classes.
//            Util.emitFatalError(
//                "Class '" + parser.name + "' already exists." +
//                    "To extend it, use 'extension class " + parser.name + "' " +
//                    "instead of 'class " + parser.name + "'."
//            );
            Util.emitFatalError("Class '" + parser.name + "' already exists.");
        }

        parsedClasses.put(parser.name, parser);

        for(int opcode : parser.parsedMethods.keySet()) {
            opcodesAndClasses.put(opcode, parser.name);
        }
    }

    public static void loadClasses(String dirPath) throws IOException {
        Files.list(Paths.get(dirPath)).forEach(path -> {
            try {
                if(!path.toString().endsWith(".msd")) return;
                loadClass(path.toString());
            } catch(IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static Optional<String> classForCommand(Command command) {
        String nameOrNull = opcodesAndClasses.getOrDefault(command.opcode, null);
        return nameOrNull == null ? Optional.empty() : Optional.of(nameOrNull);
    }

    public static Map<Integer, String> generateLocalClassMap(List<Command> commands) {
        Map<Integer, String> classMap = new HashMap<>();

        for(Command command : commands) {
            var commandClass = classForCommand(command);

            if(commandClass.isPresent() && command.arguments.length > 0) {
                classMap.put(command.arguments[0].intValue, commandClass.get());
            }
        }

        return classMap;
    }

    public static Optional<ParsedClass> getClass(String name) {
        return parsedClasses.containsKey(name) ? Optional.of(parsedClasses.get(name)) : Optional.empty();
    }

    public static Optional<String> getClassNameForGlobal(int global) {
        if(globalVariableClasses.containsKey(global)) {
            return Optional.of(globalVariableClasses.get(global));
        }

        return Optional.empty();
    }

    public static Collection<ParsedClass> allClasses() {
        return parsedClasses.values();
    }
}
