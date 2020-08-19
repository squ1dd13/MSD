package com.squ1dd13.msd.unified;

import com.squ1dd13.msd.decompiler.high.*;

import java.io.*;
import java.util.*;

public class Script implements ByteRepresentable {
    public List<ScriptElement> elements;

    private void disassemble(RandomAccessFile randomAccessFile) throws IOException {
        elements = new ArrayList<>();

        final int nopLimit = 4;
        List<Call> nopCalls = new ArrayList<>();

        for(long i = 0; i < randomAccessFile.length();) {
            Call call = new Call(randomAccessFile);

            if(call.getOpcode() == 0) {
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

    public Script(String filename) throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile(filename, "r");

        if(filename.endsWith(".scm")) {
            // Compiled
            disassemble(randomAccessFile);
        }

        randomAccessFile.close();
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
            System.out.println(SyntaxHighlight.highlightLine(element.toString()));
        }
    }
}
