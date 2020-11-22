package com.squ1dd13.msd.old.misc.gxt;

import com.squ1dd13.msd.old.shared.*;

import java.io.*;

class DataBlock {
    private final byte[] rawBlockData;
    private final long blockSize;

    public DataBlock(RandomAccessFile randomAccessFile) throws IOException {
        randomAccessFile.skipBytes(4);

        blockSize = Util.readUnsignedInt(randomAccessFile);

        rawBlockData = new byte[(int)blockSize];
        randomAccessFile.read(rawBlockData);
    }

    public String getValue(long offset) {
        StringBuilder stringBuilder = new StringBuilder();

        for(long i = offset; i < blockSize; ++i) {
            byte b = rawBlockData[(int)i];

            if(b == 0) break;
            stringBuilder.append((char)(b & 0xFF));
        }

        return stringBuilder.toString();
    }
}
