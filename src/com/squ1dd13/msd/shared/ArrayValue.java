package com.squ1dd13.msd.shared;

import com.squ1dd13.msd.decompiler.shared.*;

// SCM array
public class ArrayValue {
    public int offsetNumber; // 2 bytes
    public int index; // 2 bytes
    public int size; // 1 byte
    public ArrayElementType elementType; // 7 bits
    public boolean indexIsGlobalVar; // 1 bit

    enum ArrayElementType {
        Int,
        Flt,
        TextLabel,
        TextLabel16
    };

    public ArrayValue(int[] bytes) {
        offsetNumber = bytes[0] | bytes[1] << 8;
        index = bytes[2] | bytes[3] << 8;
        size = bytes[4];
        elementType = ArrayElementType.values()[bytes[5] & 0b01111111];
        indexIsGlobalVar = bytes[5] >> 7 == 1;
    }

    @Override
    public String toString() {
        return toCodeString();
    }

    public String toCodeString() {
        return String.format("Array<%s, %d, %s>[%s]",
            elementType,
            size,
            offsetNumber,
            indexIsGlobalVar ? "globalX_" + index : index
            );
    }
}
