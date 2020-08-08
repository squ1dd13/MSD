package com.squ1dd13.msd;

import com.squ1dd13.msd.compiler.constructs.language.*;
import com.squ1dd13.msd.compiler.text.*;
import com.squ1dd13.msd.decompiler.disassembler.*;
import com.squ1dd13.msd.decompiler.high.*;
import com.squ1dd13.msd.decompiler.low.*;
import com.squ1dd13.msd.shared.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Main {
    public static void compile(String inPath, String outPath) throws IOException {
        List<String> lines = new ArrayList<>();

        try(BufferedReader reader = new BufferedReader(new FileReader(inPath))) {
            String ln;

            while((ln = reader.readLine()) != null) {
                lines.add(ln);
            }
        }

        Parser parser = new Parser();

        var scm = parser.parse(lines);
        scm.compileAndWrite(outPath);
    }

    public static void main(String[] args) throws IOException {
        System.out.println("MSD v1.0 Beta");

        Command.loadFile("/Users/squ1dd13/Documents/MSD-Project/Java/MSD/commands.ini");
        CommandInfoDesk.loadCommandNames();
        CommandInfoDesk.loadFile("/Users/squ1dd13/Documents/MSD-Project/llp.txt");

        CompiledSCM scm = new CompiledSCM("/Users/squ1dd13/Documents/MSD-Project/cpp/GTA-ASM/GTA Scripts/trains.scm");
        LowScript script = scm.toScript();

        HighLevelScript highLevelScript = new HighLevelScript(script);
        highLevelScript.print();

        compile(
            "/Users/squ1dd13/Documents/MSD-Project/script.msd",
            "/Users/squ1dd13/Documents/MSD-Project/compiled.scm"
        );
    }
}
