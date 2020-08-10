package com.squ1dd13.msd;

import com.squ1dd13.msd.compiler.*;
import com.squ1dd13.msd.compiler.text.lexer.*;
import com.squ1dd13.msd.compiler.text.parser.*;
import com.squ1dd13.msd.decompiler.*;
import com.squ1dd13.msd.decompiler.disassembler.*;
import com.squ1dd13.msd.decompiler.high.*;
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
                if(ln.strip().startsWith("//")) continue;
                builder.append(ln);
            }
        }

        var tokens = Lexer.lex(builder.toString());

        var parsed = new Parser(tokens).parseTokens2();

        List<Compilable> commandList = new ArrayList<>();
        for(Compilable compilable : parsed) {
            commandList.addAll(compilable.toCommands());
        }

        CompiledScript script = new CompiledScript();
        script.elements = commandList;
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

        CommandRegistry.addPseudoCommands();

        Command.loadFile("/Users/squ1dd13/Documents/MSD-Project/Java/MSD/commands.ini");

        SCM scm = new SCM("/Users/squ1dd13/Documents/MSD-Project/cpp/GTA-ASM/GTA Scripts/trains.scm");
        DecompiledScript script = scm.toScript();

        HighLevelScript highLevelScript = new HighLevelScript(script);
//        highLevelScript.print();
//
        compile(
            "/Users/squ1dd13/Documents/MSD-Project/script.msd",
            "/Users/squ1dd13/Documents/MSD-Project/compiled.scm"
        );

        highLevelScript = new HighLevelScript(new SCM("/Users/squ1dd13/Documents/MSD-Project/compiled.scm").toScript());
        highLevelScript.print();

        String maths = "1 * -2";
        var lexedMaths = Parser.filterBlankTokens(Lexer.lex(maths));
        var tokens = ArithmeticConverter.infixToPostfix2(lexedMaths);

        StringBuilder expressionBuilder = new StringBuilder();
        for(Token t : tokens) {
            expressionBuilder.append(t.hasText ? t.getText() : t.getInteger()).append(' ');
        }

        System.out.println(maths + "\nbecomes\n" + expressionBuilder);

        System.out.println(ArithmeticConverter.solve(tokens));

        System.out.println("Saving registry...");
        CommandRegistry.save(registryPath);
    }
}
