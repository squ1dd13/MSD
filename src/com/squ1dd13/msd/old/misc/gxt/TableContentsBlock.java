package com.squ1dd13.msd.old.misc.gxt;

import com.squ1dd13.msd.old.shared.*;

import java.io.*;
import java.util.*;

class TableContentsBlock {
    public Map<String, Table> tables = new HashMap<>();

    public TableContentsBlock(RandomAccessFile randomAccessFile) throws IOException {
        byte[] identifierBytes = new byte[4];
        int r = randomAccessFile.read(identifierBytes);

        String identifier = new String(identifierBytes);
        if(!identifier.equals("TABL")) {
            Util.emitFatalError("Expected 'TABL' as table block identifier, but got '" + identifier + "'");
        }

        long blockSize = Util.readUnsignedInt(randomAccessFile);

        long entryCount = blockSize / 12L;
        for(int i = 0; i < entryCount; ++i) {
            String name = Util.readString(randomAccessFile, 8);
            long offset = Util.readUnsignedInt(randomAccessFile);

            long positionBefore = randomAccessFile.getFilePointer();
            randomAccessFile.seek(offset);
            if(!name.equals("MAIN")) {
                Util.readString(randomAccessFile, 8);
            }
            tables.put(name, new Table(randomAccessFile));
            randomAccessFile.seek(positionBefore);
        }
    }

    public void print() {
        for(var entry : tables.entrySet()) {
            System.out.println();
            System.out.println(entry.getKey() + ":");

            Table table = entry.getValue();
            Set<Long> keys = table.keySet();

            for(long key : keys) {
                System.out.println("  " + Long.toHexString(key) + " = " + table.get(key));
            }
        }
    }
}
