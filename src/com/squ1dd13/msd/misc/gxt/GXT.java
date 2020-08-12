package com.squ1dd13.msd.misc.gxt;

import java.io.*;

// Currently only GTA: SA format (as with most other things here).
public class GXT {
    private TableContentsBlock tableContentsBlock;
    public static GXT mainGXT;

    public static GXT load(String path) throws IOException {
        GXT gxt = new GXT();

        try(RandomAccessFile file = new RandomAccessFile(path, "r")) {
            file.skipBytes(4);
            gxt.tableContentsBlock = new TableContentsBlock(file);
        }

        return gxt;
    }

    public String get(String tableName, String key) {
        return tableContentsBlock.tables.get(tableName).get(key);
    }

    public String get(String key) {
        return get("MAIN", key);
    }

    public void print() {
        tableContentsBlock.print();
    }

    public boolean contains(String key) {
        return get(key) != null;
    }
}
