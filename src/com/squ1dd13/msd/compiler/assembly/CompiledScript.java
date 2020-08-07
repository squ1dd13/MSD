package com.squ1dd13.msd.compiler.assembly;

import com.squ1dd13.msd.compiler.constructs.*;
import com.squ1dd13.msd.compiler.constructs.language.*;

import java.io.*;
import java.util.*;

public class CompiledScript {
    public List<Compilable> elements = new ArrayList<>();

    public void compileAndWrite(String filePath) throws IOException {
        Context ctx = new Context();

        FileOutputStream stream = new FileOutputStream(filePath);
//        try {
            for(Compilable elem : elements) {
//                try {
//                    System.out.println(((LowLevelCommand)elem).command.name);
//                    System.out.println((((LowLevelCommand)elem).arguments.get(0).type));
//                } catch(Exception e) {}

                var compiled = elem.compile(ctx);
                for(int b : compiled) {
                    System.out.println(Integer.toHexString(b));
                    stream.write(b);
                }
            }
//        } finally {
            stream.close();
//        }
    }
}
