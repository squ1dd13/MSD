package com.squ1dd13.msd;

import com.squ1dd13.msd.compiler.*;
import com.squ1dd13.msd.compiler.text.lexer.*;
import com.squ1dd13.msd.compiler.text.parser.*;
import com.squ1dd13.msd.decomp.*;
import com.squ1dd13.msd.decomp.controlflow.*;
import com.squ1dd13.msd.decomp.dataflow.*;
import com.squ1dd13.msd.misc.gxt.*;
import com.squ1dd13.msd.shared.*;

import java.io.*;
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

        CommandRegistry.addPseudoCommands();
        Command.loadFile("/Users/squ1dd13/Documents/MSD-Project/Java/MSD/commands.ini");
        CommandRegistry.loadParameterCounts("/Users/squ1dd13/Downloads/SASCM.ini");

        Operator.parse(Files.readString(Path.of("/Users/squ1dd13/Documents/MSD-Project/Java/MSD/operators.txt")));

        Decompiler decompiler = new Decompiler("/Users/squ1dd13/Documents/MSD-Project/cpp/GTA-ASM/GTA Scripts/shopper.scm");//"/Users/squ1dd13/Downloads/1496840926_visual-car-spawner-v1/VCS_v2.0_Android.csa");

        BasicScript script = decompiler.decompile();

        ControlFlowGraph cfg = new ControlFlowGraph(
            script.getBodyInstructions(),
            new OffsetMap(script)
        );

        cfg.build();
//        cfg.print();

        var reachable = cfg.getReachableInstructions();
        int unreachableCount = script.bodyCode.size() - reachable.size();
        System.out.printf("%d instructio%s removed\n", unreachableCount, unreachableCount == 1 ? "n" : "ns");

        script.bodyCode = reachable;

        FlowAnalyzer flowAnalyzer = new FlowAnalyzer(script);
        flowAnalyzer.analyze();
        flowAnalyzer.createScript().print();

        // Load GTA: San Andreas classes.

//        CommandRegistry.addPseudoCommands();
//
//        Command.loadFile("/Users/squ1dd13/Documents/MSD-Project/Java/MSD/commands.ini");
//        CommandRegistry.loadParameterCounts("/Users/squ1dd13/Downloads/SASCM.ini");
//
//        SCM scm = new SCM("/Users/squ1dd13/Documents/MSD-Project/cpp/GTA-ASM/GTA Scripts/ammu.scm");
//        SCM scm1 = new SCM("/Users/squ1dd13/Documents/MSD-Project/cpp/GTA-ASM/GTA Scripts/trains.scm");
//        SCM scm2 = new SCM("/Users/squ1dd13/Documents/MSD-Project/cpp/GTA-ASM/GTA Scripts/shopper.scm");
//        DecompiledScript script = scm.toScript();
//        script = scm1.toScript();
//        script = scm2.toScript();
////        script.print();
//
//
//
//        CommandRegistry.loadVariadicInstructions("/Users/squ1dd13/Documents/MSD-Project/Java/MSD/variadic.txt");
//
//        CommandRegistry.writeCStyleEnum("/Users/squ1dd13/Documents/Opcode.h", "Opcode");

//        Script newScript = new Script("/Users/squ1dd13/Documents/MSD-Project/cpp/GTA-ASM/GTA Scripts/carmod1.scm");
//        newScript.print();

//        MainScript mainScript = new MainScript(
//            "/Users/squ1dd13/gta_wine/drive_c/Program Files/Rockstar Games/GTA San Andreas/data/script/main.scm",
//            "/Users/squ1dd13/Downloads/mission_scripts"
//        );



//        HighLevelScript highLevelScript = new HighLevelScript(script);
//
//        var allClasses = ClassRegistry.allClasses();
//        for(ParsedClass parsedClass : allClasses) {
//            parsedClass.addVariableNames(script.commands);
//        }
//
//        highLevelScript.print();
//
//        ClassIdentifier menuIdentifier = new ClassIdentifier("Object",
//            new DataValue(AbstractType.LocalIntFloat, 0));
//
//        menuIdentifier.analyzeCommands(script.commands);
//        menuIdentifier.printClass();

//        for(DataValue value : menuIdentifier.targetVars) {
//            System.out.println(value.intValue + " is Object");
//        }

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

//        System.out.println("Saving registry...");
        CommandRegistry.save(registryPath);
    }
}
