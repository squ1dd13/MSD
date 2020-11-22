package com.squ1dd13.msd.old.decompiler.high;

import com.squ1dd13.msd.old.shared.*;

import java.util.*;
import java.util.function.*;

public class CommandIterator implements Iterator<Command> {
    private Iterator<Command> iterator;

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public Command next() {
        return iterator.next();
    }

    @Override
    public void remove() {
        iterator.remove();
    }

    @Override
    public void forEachRemaining(Consumer<? super Command> action) {
        iterator.forEachRemaining(action);
    }

    public Command peek() {
        var it = iterator;
        return it.next();
    }
}
