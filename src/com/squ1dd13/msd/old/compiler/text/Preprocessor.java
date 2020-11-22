package com.squ1dd13.msd.old.compiler.text;

import java.util.*;

public class Preprocessor {
    public static String preprocess(String codeStr) {
        String withoutBlocks = codeStr.replaceAll("/\\*(.|[\\s])*?\\*/", "");

        String[] lines = withoutBlocks.split(System.lineSeparator());

        List<String> cleanLines = new ArrayList<>();

        for(String line : lines) {
            String cleanLine = line.strip();
            int commentIndex = cleanLine.indexOf("//");

            if(commentIndex > -1) {
                cleanLine = cleanLine.substring(0, commentIndex).strip();
            }

            if(!cleanLine.isBlank()) {
                cleanLines.add(cleanLine);
            }
        }

        return String.join("\n", cleanLines);
    }
}
