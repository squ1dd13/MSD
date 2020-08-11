package com.squ1dd13.msd.misc.img;

import com.squ1dd13.msd.shared.*;

import java.io.*;
import java.nio.*;

class DirectoryEntry {
    public long sectorOffset;
    public long sectorCount;
    public String fileName;

    public DirectoryEntry(RandomAccessFile randomAccessFile) throws IOException {
        sectorOffset = Util.readUnsignedInt(randomAccessFile);
        sectorCount = Util.readUnsignedInt(randomAccessFile);
        fileName = Util.readString(randomAccessFile, 24);
    }

    public ByteBuffer readData(RandomAccessFile randomAccessFile) throws IOException {
        long offset = sectorOffset * 2048;
        long size = sectorCount * 2048;

        long pos = randomAccessFile.getFilePointer();
        randomAccessFile.seek(offset);

        ByteBuffer byteBuffer = ByteBuffer.allocate((int)size).order(ByteOrder.LITTLE_ENDIAN);
        for(long i = 0; i < size; ++i) {
            byteBuffer.put(randomAccessFile.readByte());
        }

        // Return to original position.
        randomAccessFile.seek(pos);

        return byteBuffer;
    }
}
