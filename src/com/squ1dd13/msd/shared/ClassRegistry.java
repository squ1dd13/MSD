package com.squ1dd13.msd.shared;

import com.squ1dd13.msd.compiler.text.lexer.*;
import com.squ1dd13.msd.compiler.text.parser.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class ClassRegistry {
    private static final Map<String, ClassParser> parsedClasses = new HashMap<>();
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

    public static void loadClass(String classPath) throws IOException {
        var tokens = ParserUtils.filterBlankTokens(Lexer.lex(Files.readString(Paths.get(classPath))));

        ClassParser parser = new ClassParser(tokens.iterator());
        if(parsedClasses.containsKey(parser.name)) {
            // TODO: Implement extension classes.
            Util.emitFatalError(
                "Class '" + parser.name + "' already exists." +
                    "To extend it, use 'class extension " + parser.name + "' " +
                    "instead of 'class " + parser.name + "'."
            );
        }

        parsedClasses.put(parser.name, parser);
    }

    public static void loadClasses(String dirPath) throws IOException {
        Files.list(Paths.get(dirPath)).forEach(path -> {
            try {
                loadClass(path.toString());
            } catch(IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static Optional<ClassParser> getClass(String name) {
        return parsedClasses.containsKey(name) ? Optional.of(parsedClasses.get(name)) : Optional.empty();
    }

    public static Optional<String> getClassNameForGlobal(int global) {
        if(globalVariableClasses.containsKey(global)) {
            return Optional.of(globalVariableClasses.get(global));
        }

        return Optional.empty();
    }

    public static Collection<ClassParser> allClasses() {
        return parsedClasses.values();
    }
}
