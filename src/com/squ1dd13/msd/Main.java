package com.squ1dd13.msd;

import com.squ1dd13.msd.compiler.assembly.*;
import com.squ1dd13.msd.compiler.constructs.*;
import com.squ1dd13.msd.compiler.text.*;
import com.squ1dd13.msd.compiler.text.lexer.*;
import com.squ1dd13.msd.decompiler.disassembler.*;
import com.squ1dd13.msd.decompiler.high.*;
import com.squ1dd13.msd.decompiler.low.*;
import com.squ1dd13.msd.shared.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Main {
    public static void compile(String inPath, String outPath) throws IOException {
        StringBuilder builder = new StringBuilder();

        try(BufferedReader reader = new BufferedReader(new FileReader(inPath))) {
            String ln;

            while((ln = reader.readLine()) != null) {
                builder.append(ln);
            }
        }

        Parser parser = new Parser();

        CompiledScript script = new CompiledScript();
        script.elements = new NewParser(Lexer.lex(builder.toString())).parseTokens();
        script.compileAndWrite(outPath);
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        System.out.println("MSD v1.0 Beta");

        String registryPath = "/Users/squ1dd13/Documents/MSD-Project/commands.msdreg";
        if(Files.exists(Paths.get(registryPath))) {
            try {
                CommandRegistry.load(registryPath);
            } catch(Exception e) {
                CommandRegistry.init();
            }
        } else {
            CommandRegistry.init();
        }

        Command.loadFile("/Users/squ1dd13/Documents/MSD-Project/Java/MSD/commands.ini");
        CommandInfoDesk.loadCommandNames();
        CommandInfoDesk.loadFile("/Users/squ1dd13/Documents/MSD-Project/llp.txt");

        SCM scm = new SCM("/Users/squ1dd13/Documents/MSD-Project/cpp/GTA-ASM/GTA Scripts/trains.scm");
        LowScript script = scm.toScript();

//        final String s = "if(?is_char_in_model(globalInt_12, 570)) {\n" +
//            "    goto(-123);\n" +
//            "}\n" +
//            "goto(-123);";
//
//        var tokens = Lexer.lex(s);
//        System.out.println(tokens);

//        NewParser newParser = new NewParser(tokens);
//        var intermediate = newParser.parseTokens();

//        for(Compilable comp : intermediate) {
//            System.out.println(comp.toString());
//        }

//        var p = newParser.parseIf();
//        System.out.println(p);

        HighLevelScript highLevelScript = new HighLevelScript(script);
        highLevelScript.print();
//
        compile(
            "/Users/squ1dd13/Documents/MSD-Project/script.msd",
            "/Users/squ1dd13/Documents/MSD-Project/compiled.scm"
        );
//
//        Stream<Path> paths = Files.list(Paths.get("/Users/squ1dd13/Documents/MSD-Project/cpp/GTA-ASM/GTA Scripts"));
//        paths.forEach(path -> {
//            long length = path.toFile().length();
//            System.out.println(path.getFileName().toString() + " = " + length + " bytes");
//            System.out.println("that's " + ((float)length / 2048.f) + " * 2048");
//        });

        System.out.println("Saving registry...");
        CommandRegistry.save(registryPath);
    }
}
