package com.squ1dd13.msd.compiler.text.lexer;

import java.io.*;
import java.util.*;

public class CharacterReader extends PushbackReader {
    public CharacterReader(Reader in, int size) {
        super(in, size);
    }

    public CharacterReader(Reader in) {
        super(in);
    }

    private final Deque<StringBuilder> peekCharacters = new ArrayDeque<>();
    public StringBuilder currentBuilder = new StringBuilder();

    public void save() {
        peekCharacters.push(currentBuilder);
        currentBuilder = new StringBuilder();
    }

    public void restore() throws IOException {
        char[] chars = currentBuilder.toString().toCharArray();

        for(int i = chars.length - 1; i >= 0; --i) {
            unread(chars[i]);
        }

        currentBuilder = peekCharacters.pop();
    }

    @Override
    public int read() throws IOException {
        int c = super.read();
        currentBuilder.append((char)c);
        return c;
    }

    public int peek() throws IOException {
        int c = super.read();
        unread(c);

        return c;
    }

    public void advance() throws IOException {
        super.read();
    }
}
