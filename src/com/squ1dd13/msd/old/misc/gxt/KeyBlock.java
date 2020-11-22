package com.squ1dd13.msd.old.misc.gxt;

import com.squ1dd13.msd.old.shared.*;

import java.io.*;
import java.util.*;
import java.util.zip.*;

class KeyBlock {
    public String name;
    private final Map<Long, Long> CRCsAndOffsets = new HashMap<>();

    public KeyBlock(RandomAccessFile randomAccessFile) throws IOException {
        name = Util.readString(randomAccessFile, 4);

        long blockSize = Util.readUnsignedInt(randomAccessFile);
        long entryCount = blockSize / 8;

        for(int i = 0; i < entryCount; ++i) {
            long offset = Util.readUnsignedInt(randomAccessFile);
            long crc = Util.readUnsignedInt(randomAccessFile);

            CRCsAndOffsets.put(crc, offset);
        }
    }

    private static final CRC32 CRC = new CRC32();
    private static long calculateCRC(String s) {
        CRC.reset();
        CRC.update(s.getBytes());

        // GTA uses JAMCRC, so we need to invert the bits.
        // The long type is used so that unsigned ints can be stored,
        //  so if we bitwise AND the inverted bits with the max unsigned int
        //  value, we get an "unsigned" integer.
        return ~CRC.getValue() & 0xFFFFFFFFL;
    }

    public long offsetForKey(String key) {
        return CRCsAndOffsets.getOrDefault(calculateCRC(key), -1L);
    }

    public long offsetForKey(long key) {
        return CRCsAndOffsets.getOrDefault(key, -1L);
    }

    public Set<Long> keySet() {
        return CRCsAndOffsets.keySet();
    }
}
