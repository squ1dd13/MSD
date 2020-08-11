package com.squ1dd13.msd.shared;

import java.io.*;
import java.nio.*;
import java.util.*;

public class Util {
    // The SCM files use little-endian, so we need to be able to use that.
    public static int[] intToBytesLE(int v) {
        return new int[]{
            v & 0xFF,
            (v >> 8) & 0xFF,
            (v >> 16) & 0xFF,
            (v >> 24) & 0xFF
        };
    }

    public static int[] intToBytesLE(int v, int n) {
        int[] bytes = new int[n];

        for(int i = 0; i < n; ++i) {
            bytes[i] = (v >> (i * 8)) & 0xFF;
        }

        return bytes;
    }

    public static int[] byteArrayToIntArray(byte[] buf) {
        int[] array = new int[buf.length];
        for(int i = 0; i < array.length; i++) {
            array[i] = buf[i] & 0xFF;
        }
        return array;
    }

    public static int[] floatToBytesLE(float f) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        return byteArrayToIntArray(buffer.putFloat(f).array());
    }

    public static int intFromBytesLE(int[] bytes, int n) {
        int v = 0;

        for(int i = 0; i < n; ++i) {
            v |= (bytes[i] << (i * 8));
        }

        return v;
    }

    public static int intFromBytesLE(int[] bytes) {
        return intFromBytesLE(bytes, 4);
    }

    public static float floatFromBytesLE(int[] bytes) {
        int asInt = (bytes[0] & 0xFF)
            | ((bytes[1] & 0xFF) << 8)
            | ((bytes[2] & 0xFF) << 16)
            | ((bytes[3] & 0xFF) << 24);

        return Float.intBitsToFloat(asInt);
    }

    public static List<Integer> intArrayToList(int[] arr) {
        List<Integer> list = new ArrayList<>(arr.length);

        for(int n : arr) {
            list.add(n);
        }

        return list;
    }

    public static int[] intListToArray(List<Integer> list) {
        int[] arr = new int[list.size()];

        for(int i = 0; i < list.size(); i++) {
            arr[i] = list.get(i);
        }

        return arr;
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

    public static String cropString(String s) {
        return cropString(s, 1);
    }

    public static String insetString(String s, int n) {
        return cropString(s, n).substring(n);
    }

    public static String insetString(String s) {
        return insetString(s, 1);
    }

    public static List<String> readLines(String path) {
        try {
            List<String> lines = new ArrayList<>();

            try(BufferedReader reader = new BufferedReader(new FileReader(path))) {
                String ln;

                while((ln = reader.readLine()) != null) {
                    lines.add(ln);
                }
            }

            return lines;
        } catch(Exception e) {
            return null;
        }
    }

    public static void writeToFile(String path, String s) {
        try {
            FileWriter writer = new FileWriter(path);
            writer.append(s);
            writer.close();
        } catch(IOException e) {
            System.out.println("Write failed");
        }
    }

    public static <T> T[] takeSome(T[] array, int num) {
        return Arrays.copyOfRange(array, 0, num);
    }

    public static int[] subArray(int[] arr, int start, int end) {
        // FIXME: Very inefficient
        return intListToArray(intArrayToList(arr).subList(start, end));
    }

    public static int roundUpToMultiple(int n, int m) {
        return (n + m - 1) / m * m;
    }

    public static int countOccurrences(String s, char c) {
        return (int)s.chars().filter(ch -> ch == c).count();
    }

    public static <T> List<T> between(List<T> list, T a, T b) {
        return list.subList(list.indexOf(a) + 1, list.lastIndexOf(b));
    }

    public static String readString(RandomAccessFile randomAccessFile, int length) throws IOException {
        byte[] buf = new byte[length];
        randomAccessFile.read(buf);

        int[] bytes = byteArrayToIntArray(buf);

        StringBuilder builder = new StringBuilder();
        for(int b : bytes) {
            if(b == 0) break;

            builder.append((char)b);
        }

        return builder.toString();//new String(buf);
    }

    public static long readUnsignedInt(RandomAccessFile randomAccessFile) throws IOException {
        byte[] buf = new byte[4];
        randomAccessFile.read(buf);

        return ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN).getInt() & 0xFFFFFFFFL;
    }
}
