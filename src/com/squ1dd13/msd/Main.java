package com.squ1dd13.msd;

import com.squ1dd13.msd.compiler.*;
import com.squ1dd13.msd.compiler.text.lexer.*;
import com.squ1dd13.msd.compiler.text.parser.*;
import com.squ1dd13.msd.decompiler.*;
import com.squ1dd13.msd.decompiler.disassembler.*;
import com.squ1dd13.msd.decompiler.high.*;
import com.squ1dd13.msd.decompiler.shared.*;
import com.squ1dd13.msd.misc.gxt.*;
import com.squ1dd13.msd.misc.img.*;
import com.squ1dd13.msd.shared.*;

import java.io.*;
import java.nio.*;
import java.nio.file.*;
import java.util.*;

// TODO: Add support for main.scm

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
        System.out.println("MSD v1.0");

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

        GXT.mainGXT = GXT.load("/Users/squ1dd13/gta_wine/drive_c/Program Files/Rockstar Games/GTA San Andreas/Text/american.gxt");

        // Load GTA: San Andreas classes.
        ClassRegistry.loadClasses("/Users/squ1dd13/Documents/MSD-Project/Java/MSD/SanAndreas");
        CommandRegistry.addPseudoCommands();

        Command.loadFile("/Users/squ1dd13/Documents/MSD-Project/Java/MSD/commands.ini");

        SCM scm = new SCM("/Users/squ1dd13/Documents/MSD-Project/cpp/GTA-ASM/GTA Scripts/debt.scm");
        DecompiledScript script = scm.toScript();
//        script.print();

        HighLevelScript highLevelScript = new HighLevelScript(script);

        var allClasses = ClassRegistry.allClasses();
        for(ClassParser classParser : allClasses) {
            classParser.addVariableNames(script.commands);
        }

        highLevelScript.print();

        ClassIdentifier menuIdentifier = new ClassIdentifier("Player",
            new DataValue(AbstractType.GlobalIntFloat, 8));

        menuIdentifier.analyzeCommands(script.commands);
        menuIdentifier.printClass();

        for(DataValue value : menuIdentifier.targetVars) {
            System.out.println(value.intValue + " is Player");
        }

//        var classTokens = Lexer.lex(Files.readString(Paths.get("/Users/squ1dd13/Documents/MSD-Project/Character.msd")));
//        classTokens = ParserUtils.filterBlankTokens(classTokens);
//        ClassParser classParser = new ClassParser(classTokens.iterator());
//


//        compile(
//            "/Users/squ1dd13/Documents/MSD-Project/shopper.msd",
//            "/Users/squ1dd13/Documents/MSD-Project/compiled.scm"
//        );
//
//        highLevelScript = new HighLevelScript(new SCM("/Users/squ1dd13/Documents/MSD-Project/compiled.scm").toScript());
//        highLevelScript.print();
//
//        byte[] scmBytes = Files.readAllBytes(Paths.get("/Users/squ1dd13/Documents/MSD-Project/compiled.scm"));

//        IMG img = new IMG("/Users/squ1dd13/gta_wine/drive_c/Program Files/Rockstar Games/GTA San Andreas/data/script/script copy.img");
//        img.withOpen(
//            archive -> {
//                archive.write("shopper.scm", ByteBuffer.wrap(scmBytes));
//            }
//        );

        System.out.println("Saving registry...");
        CommandRegistry.save(registryPath);
    }
}
