package com.squ1dd13.msd;

import com.squ1dd13.msd.old.compiler.*;
import com.squ1dd13.msd.old.compiler.text.lexer.*;
import com.squ1dd13.msd.old.compiler.text.parser.*;
import com.squ1dd13.msd.decomp.*;
import com.squ1dd13.msd.decomp.controlflow.*;
import com.squ1dd13.msd.decomp.dataflow.*;
import com.squ1dd13.msd.old.misc.gxt.*;
import com.squ1dd13.msd.old.shared.*;

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

    private static boolean init() {
        try {
            CommandRegistry.init();

            Command.loadFile("/home/squ1dd13/Documents/Projects/Java/MSD/commands.ini");
            CommandRegistry.loadParameterCounts("/home/squ1dd13/Documents/Projects/Java/MSD/SASCM.ini");

            // Probably don't need these for decompilation.
            CommandRegistry.addPseudoCommands();

            Operator.parse(Files.readString(Path.of("/home/squ1dd13/Documents/Projects/Java/MSD/operators.txt")));
            Command.createBlocklyJSON("/home/squ1dd13/WebstormProjects/GTA-Blocks/blocks-generated.json");
        } catch(Exception ignored) {
            ignored.printStackTrace();
            return false;
        }

        return true;
    }

    private static boolean printDecompiled(String path) {
        Decompiler decompiler = null;

        try {
            decompiler = new Decompiler(path);
        } catch(IOException ignored) {
            return false;
        }

        BasicScript script = decompiler.decompile();

        ControlFlowGraph cfg = new ControlFlowGraph(
            script.getBodyInstructions(),
            new OffsetMap(script)
        );

        FlowConstructor flowConstructor = new FlowConstructor(script);
        flowConstructor.build();
        flowConstructor.createScript().print();

        return true;
    }

    public static void main(String[] args) throws IOException {
        // '2.0' = 'the one that is less terrible than the last'
        // The change in terribleness was enough for a major version change.
        System.out.println("MSD v2.0");

        if(!init()) {
            System.out.println("Error: Initialisation failed! Exiting...");
            System.exit(1);
        }

        boolean status = printDecompiled("/home/squ1dd13/Downloads/SkinSelectorbyVisek/skinselectorbyvisek.csa");
        if(!status) {
            System.out.println("Error: Decompilation failed! Exiting...");
            System.exit(2);
        }
    }

    public static void oldmain(String[] args) throws IOException, ClassNotFoundException {
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

//        GXT.mainGXT = GXT.load("/Users/squ1dd13/gta_wine/drive_c/Program Files/Rockstar Games/GTA San Andreas/Text/american.gxt");

        CommandRegistry.addPseudoCommands();
        Command.loadFile("/Users/squ1dd13/Documents/MSD-Project/Java/MSD/commands.ini");
        CommandRegistry.loadParameterCounts("/Users/squ1dd13/Downloads/SASCM.ini");

        Operator.parse(Files.readString(Path.of("/Users/squ1dd13/Documents/MSD-Project/Java/MSD/operators.txt")));

        Decompiler decompiler = new Decompiler("/Users/squ1dd13/Downloads/1470772056_72851/com.rockstargames.gtasa/spawner.csa");//"/Users/squ1dd13/Documents/MSD-Project/cpp/GTA-ASM/GTA Scripts/shopper.scm");//"/Users/squ1dd13/Downloads/1496840926_visual-car-spawner-v1/VCS_v2.0_Android.csa");

        BasicScript script = decompiler.decompile();

        ControlFlowGraph cfg = new ControlFlowGraph(
            script.getBodyInstructions(),
            new OffsetMap(script)
        );

//        cfg.build();
//        cfg.print();
//
//        var reachable = cfg.getReachableInstructions();
//        int unreachableCount = script.bodyCode.size() - reachable.size();
//        System.out.printf("%d instructio%s removed\n", unreachableCount, unreachableCount == 1 ? "n" : "ns");
//
//        script.bodyCode = reachable;

        FlowConstructor flowConstructor = new FlowConstructor(script);
        flowConstructor.build();
        flowConstructor.createScript().print();

        CommandRegistry.save(registryPath);
    }
}
