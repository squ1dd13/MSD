package com.squ1dd13.msd.unified;

import com.squ1dd13.msd.decompiler.high.*;
import com.squ1dd13.msd.shared.*;
import com.squ1dd13.msd.unified.elements.*;

import java.io.*;
import java.util.*;

public class Script extends ElementContainer implements ByteRepresentable {
    public Script(RandomAccessFile randomAccessFile, long len) throws IOException {
        disassemble(randomAccessFile, len);
    }

    public Script(String filename) throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile(filename, "r");

        if(filename.endsWith(".scm")) {
            // Compiled
            disassemble(randomAccessFile, -1);
        }

        randomAccessFile.close();

//        buildStructures();
    }

    private void disassemble(RandomAccessFile randomAccessFile, long maxRead) throws IOException {
        elements = new ArrayList<>();

        final int nopLimit = 4;
        List<Call> nopCalls = new ArrayList<>();

        if(maxRead == -1) maxRead = randomAccessFile.length();
        for(long i = 0; i < maxRead; ) {
            Call call = new Call(randomAccessFile);

            if(call.getOpcode() == 0 || call.getOpcode() == -1) {
                nopCalls.add(call);
            } else {
                if(!nopCalls.isEmpty()) {
                    elements.addAll(nopCalls);
                    nopCalls.clear();
                }

                elements.add(call);
            }

            if(nopCalls.size() > nopLimit) {
                System.out.println("nop limit reached, stopping");
                break;
            }

            i += call.getLength();
        }
    }

    @Override
    public String toString() {
        return "Script{" +
            "elements=" + elements +
            '}';
    }

    @Override
    public List<Integer> toBytes(Context context) {
        return null;
    }

    public void print() {
        for(ScriptElement element : elements) {
            var lines = element.toLineStrings();

            for(String line : lines) {
                System.out.println(SyntaxHighlight.highlightLine(line.replace("$i", "    ")));
            }
        }
    }

    public void write(BufferedWriter writer) throws IOException {
        for(ScriptElement element : elements) {
            var lines = element.toLineStrings();

            for(String line : lines) {
                writer.append(line.replace("$i", "    "));
                writer.newLine();
            }
        }
    }
}
