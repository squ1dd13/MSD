package com.squ1dd13.msd;

import com.squ1dd13.msd.compiler.text.*;
import com.squ1dd13.msd.decompiler.high.*;
import com.squ1dd13.msd.decompiler.low.*;
import com.squ1dd13.msd.shared.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("MSD v1.0 Beta");

        Command.loadFile("/Users/squ1dd13/Documents/MSD-Project/Java/MSD/commands.ini");
        CommandInfoDesk.loadCommandNames();
        CommandInfoDesk.loadFile("/Users/squ1dd13/Documents/MSD-Project/llp.txt");

        System.out.println(CommandInfoDesk.getInfo(0x3A4));

        LowScript script = LowScript.load("/Users/squ1dd13/Documents/trains.txt");

        HighLevelScript highLevelScript = new HighLevelScript(script);
        highLevelScript.print();

        List<String> lines = new ArrayList<>();

        String scriptPath = "/Users/squ1dd13/Documents/MSD-Project/script.msd";
//        Files.createFile(Paths.get(scriptPath));
        try(BufferedReader reader = new BufferedReader(new FileReader(scriptPath))) {
            String ln;

            while((ln = reader.readLine()) != null) {
                lines.add(ln);
            }
        }

        Parser parser = new Parser();

        var scm = parser.parse(lines);
        scm.compileAndWrite("/Users/squ1dd13/Documents/MSD-Project/compiled.scm");
//        script.print();
    }
}
