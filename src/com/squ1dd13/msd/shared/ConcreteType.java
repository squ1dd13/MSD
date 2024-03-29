package com.squ1dd13.msd.shared;

public enum ConcreteType {
    // This enum has all of the types that exist in GTA: SA's SCM files.
    // An SCM disassembler must know all of these, but a decompiler can use
    //  a shorter list (by combining the integer types and string types into just Int/String).
    // An SCM compiler must also know these types.

    End(0x0),
    S32(0x1),  // Immediate
    GlobalIntFloat(0x2), // 16-bit global offset
    LocalIntFloat(0x3), // 16-bit local offset
    S8(0x4), // Immediate
    S16(0x5), // Immediate
    F32(0x6), // Immediate
    GlobalIntFloatArr(0x7),
    LocalIntFloatArr(0x8),
    String8(0x9), // Immediate
    GlobalString8(0xA), // 16-bit global offset
    LocalString8(0xB), // 16-bit local offset
    GlobalString8Arr(0xC),
    LocalString8Arr(0xD),
    StringVar(0xE), // Immediate, 8-bit size followed by characters
    String16(0xF), // Immediate
    GlobalString16(0x10), // 16-bit global offset
    LocalString16(0x11), // 16-bit local offset
    GlobalString16Arr(0x12),
    LocalString16Arr(0x13),

    // Not a real type, so should never be written to a file.
    Unknown(0x14);

    // 'n' has no significance, but it allows the enum to act as a visual lookup
    //  table when debugging (because the type encoding is written).
    ConcreteType(int n) { }

    public static ConcreteType decode(int n) {
        return values()[n];
    }

    public int valueLength() {
        switch(this) {
            case End:
            case Unknown:
                return 0;

            case S32:
            case F32:
                return 4;

            case GlobalIntFloat:
            case LocalString16:
            case GlobalString16:
            case LocalString8:
            case GlobalString8:
            case S16:
            case LocalIntFloat:
                return 2;

            case S8:
                return 1;

            case GlobalIntFloatArr:
            case LocalString16Arr:
            case GlobalString16Arr:
            case LocalString8Arr:
            case GlobalString8Arr:
            case LocalIntFloatArr:
                return 6;

            case String8:
                return 8;

            case StringVar:
                return -1;

            case String16:
                return 16;
        }

        return 0;
    }

    public int[] toBytes(int value) {
        return Util.intToBytesLE(value);
    }

    public int[] toBytes(float value) {
        return Util.floatToBytesLE(value);
    }

    public int[] toBytes(String value) {
        if(this == StringVar) {
            // Size comes first.
            int[] bytes = new int[value.length() + 1];
            bytes[0] = value.length();

            for(int i = 0; i < value.length(); i++) {
                bytes[i + 1] = value.charAt(i);
            }

            return bytes;
        }

        int[] bytes = new int[valueLength()];

        // bytes is filled with zeros, so only copy the length we need.
        // The null padding is implicit.
        for(int i = 0; i < value.length(); ++i) {
            bytes[i] = value.charAt(i);
        }

        return bytes;
    }

    public AbstractType highLevelType() {
        switch(this) {
            case String8:
            case String16:
            case StringVar:
                return AbstractType.Str;

            case End:
                return AbstractType.End;

            case GlobalIntFloat:
                return AbstractType.GlobalIntFloat;

            case LocalIntFloat:
                return AbstractType.LocalIntFloat;

            case S8:
            case S16:
            case S32:
                return AbstractType.Int;

            case F32:
                return AbstractType.Flt;

            case GlobalIntFloatArr:
                return AbstractType.GlobalIntFloatArr;

            case LocalIntFloatArr:
                return AbstractType.LocalIntFloatArr;

            case GlobalString8:
            case GlobalString16:
                return AbstractType.GlobalStr;

            case LocalString8:
            case LocalString16:
                return AbstractType.LocalStr;

            case GlobalString8Arr:
            case GlobalString16Arr:
                return AbstractType.GlobalStrArr;

            case LocalString8Arr:
            case LocalString16Arr:
                return AbstractType.LocalStrArr;

            default:
                return AbstractType.Unknown;
        }
    }
}
