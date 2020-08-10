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

public class Main {
    public static void compile(String inPath, String outPath) throws IOException {
        StringBuilder builder = new StringBuilder();

        try(BufferedReader reader = new BufferedReader(new FileReader(inPath))) {
            String ln;

            while((ln = reader.readLine()) != null) {
                builder.append(ln);
            }
        }

        CompiledScript script = new CompiledScript();
        script.elements = new Parser(Lexer.lex(builder.toString())).parseTokens();
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

        SCM scm = new SCM("/Users/squ1dd13/Documents/MSD-Project/cpp/GTA-ASM/GTA Scripts/trains.scm");
        DecompiledScript script = scm.toScript();

        HighLevelScript highLevelScript = new HighLevelScript(script);
        highLevelScript.print();

        compile(
            "/Users/squ1dd13/Documents/MSD-Project/script_.msd.txt",
            "/Users/squ1dd13/Documents/MSD-Project/compiled.scm"
        );

        System.out.println("Saving registry...");
        CommandRegistry.save(registryPath);
    }
}
