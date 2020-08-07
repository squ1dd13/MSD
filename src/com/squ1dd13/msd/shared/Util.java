package com.squ1dd13.msd.shared;

import java.nio.*;
import java.util.*;

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

    public static int[] byteArrayToIntArray(byte[] buf) {
        int[] array = new int[buf.length];
        for (int i = 0; i < array.length; i++) {
            array[i] = buf[i];
        }
        return array;
    }

    public static int[] floatToBytesLE(float f) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        return byteArrayToIntArray(buffer.putFloat(f).array());
    }

    public static List<Integer> intArrayToList(int[] arr) {
        List<Integer> list = new ArrayList<>(arr.length);

        for(int n : arr) {
            list.add(n);
        }

        return list;
    }

    public static void emitFatalError(String s) {
        System.err.println("Error: " + s);
        System.exit(1);
    }

    public static void emitWarning(String s) {
        System.err.println("Warning: " + s);
    }

    public static String cropString(String s, int n) {
        return s.substring(0, s.length() - n);
    }

    public static String cropString(String s) { return cropString(s, 1); }

    public static String insetString(String s, int n) {
        return cropString(s, n).substring(n);
    }

    public static String insetString(String s) {
        return insetString(s, 1);
    }
}
