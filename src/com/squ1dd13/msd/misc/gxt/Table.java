package com.squ1dd13.msd.misc.gxt;

import java.io.*;
import java.util.*;

class Table {
    private final KeyBlock keyBlock;
    private final DataBlock dataBlock;

    public Table(RandomAccessFile randomAccessFile) throws IOException {
        keyBlock = new KeyBlock(randomAccessFile);
        dataBlock = new DataBlock(randomAccessFile);
    }

    public String get(String key) {
        var offset = keyBlock.offsetForKey(key);
        return offset != -1L ? dataBlock.getValue(offset) : null;
    }

    public String get(long key) {
        return dataBlock.getValue(keyBlock.offsetForKey(key));
    }

    public Set<Long> keySet() {
        return keyBlock.keySet();
    }
}
