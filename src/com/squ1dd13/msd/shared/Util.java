package com.squ1dd13.msd.shared;

public class Util {
    // The SCM files use little-endian, so we need to be able to use that.
    public static int[] intToBytesLE(int v) {
        return new int[] {
            v & 0xFF,
            (v >> 8) & 0xFF,
            (v >> 16) & 0xFF,
            (v >> 24) & 0xFF
        };
    }
}
