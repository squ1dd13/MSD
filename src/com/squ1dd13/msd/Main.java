package com.squ1dd13.msd;

import com.squ1dd13.msd.compiler.*;
import com.squ1dd13.msd.compiler.text.lexer.*;
import com.squ1dd13.msd.compiler.text.parser.*;
import com.squ1dd13.msd.decompiler.*;
import com.squ1dd13.msd.decompiler.disassembler.*;
import com.squ1dd13.msd.decompiler.high.*;
import com.squ1dd13.msd.misc.gxt.*;
import com.squ1dd13.msd.misc.img.*;
import com.squ1dd13.msd.shared.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

// Nowhere better to put these...
// TODO: Add comments with GXT contents (when GXT file is referenced).
// TODO: Add support for main.scm
// TODO: Add basic .img packing/unpacking capabilities for script.img. (Maybe decompile the whole thing.)
// TODO: Use Optional<...> instead of random null returns (gonna take a while)

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

        var parsed = new Parser(tokens).parseTokens();

        List<Compilable> commandList = new ArrayList<>();
        for(Compilable compilable : parsed) {
            commandList.addAll(compilable.toCommands());
        }

        CompilableScript script = new CompilableScript();
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

        GXT gxt = GXT.load("/Users/squ1dd13/gta_wine/drive_c/Program Files/Rockstar Games/GTA San Andreas/Text/american.gxt");
        gxt.print();

        IMG img = new IMG("/Users/squ1dd13/gta_wine/drive_c/Program Files/Rockstar Games/GTA San Andreas/data/script/script.img");
        img.withOpen(
            archive -> {
                var buf = archive.get("trains.scm");
                if(buf.isPresent()) {
                    try {
                        Files.write(Paths.get("/Users/squ1dd13/Documents/thing.scm"), buf.get().array());
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Util.emitWarning("No file");
                }
            }
        );

        System.out.println("Saving registry...");
        CommandRegistry.save(registryPath);
    }
}
