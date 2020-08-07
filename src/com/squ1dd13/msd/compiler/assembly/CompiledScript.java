package com.squ1dd13.msd.compiler.assembly;

import com.squ1dd13.msd.compiler.constructs.*;

import java.io.*;
import java.util.*;

public class CompiledScript {
    public List<Compilable> elements = new ArrayList<>();

    public void compileAndWrite(String filePath) throws IOException {
        Context ctx = new Context();

        FileOutputStream stream = new FileOutputStream(filePath);
//        try {
            for(Compilable elem : elements) {
                var compiled = elem.compile(ctx);
                for(int b : compiled) {
                    stream.write(b);
                }
            }
//        } finally {
            stream.close();
//        }
    }
}
