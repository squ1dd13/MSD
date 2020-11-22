package com.squ1dd13.msd.old.decompiler.high.blocks;

import java.util.*;

public class CommentBlock implements CodeBlock {
    private final String content;

    public CommentBlock(String content) {
        this.content = content;
    }

    @Override
    public List<String> toLineStrings() {
        return new ArrayList<>(List.of("// " + content));
    }
}
