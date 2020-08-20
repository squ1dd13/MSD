package com.squ1dd13.msd.unified;

import java.util.*;
import java.util.function.*;

public class PeekIterator<T> implements Iterator<T> {
    private final List<T> backingList;
    private int index = 0;

    public PeekIterator(List<T> list) {
        this.backingList = list;
    }

    public void advance() {
        ++index;
    }

    public T peek() {
        return backingList.get(index);
    }

    @Override
    public boolean hasNext() {
        return index < backingList.size();
    }

    @Override
    public T next() {
        return backingList.get(index++);
    }

    @SuppressWarnings("unchecked")
    public <V> V castNext() {
        return (V)next();
    }

    @SuppressWarnings("unchecked")
    public <V> V castPeek() {
        return (V)peek();
    }

    @Override
    public void forEachRemaining(Consumer<? super T> action) {
        for(; index < backingList.size(); ++index) {
            action.accept(backingList.get(index));
        }
    }

    public PeekIterator<T> iteratorFromHere() {
        return new PeekIterator<>(backingList.subList(index, backingList.size()));
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
