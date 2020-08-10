package com.squ1dd13.msd.shared;

public enum HighLevelType {
    // There are fewer types here than in the disassembler because
    //  the disassembler needs to know all of the types. Here, we can use higher-level
    //  data types that abstract sizing details and such.

    End,
    Int,
    Flt,
    Str,
    GlobalIntFloat,
    LocalIntFloat,
    GlobalIntFloatArr,
    LocalIntFloatArr,
    GlobalStr,
    LocalStr,
    GlobalStrArr,
    LocalStrArr,
    Unknown;

    public boolean isInteger() {
        return this == Int
            || this == GlobalIntFloat
            || this == LocalIntFloat
            || this == GlobalStr
            || this == LocalStr;
    }

    public boolean isArray() {
        return this == GlobalIntFloatArr
            || this == LocalIntFloatArr
            || this == GlobalStrArr
            || this == LocalStrArr;
    }

    public boolean isFloat() {
        return this == Flt;
    }

    public boolean isString() {
        return this == Str;
    }

    public boolean isLocal() {
        return this == LocalIntFloat || this == LocalStr;
    }

    public boolean isGlobal() {
        return this == GlobalIntFloat || this == GlobalStr;
    }

    public boolean isVariable() {
        return isGlobal() || isLocal();
    }

    public static HighLevelType typeForString(String s) {
        char c = s.charAt(0);

        switch(c) {
            case '\'':
                return Str;

            case 'E':
                return End;

            case 'G':
                return GlobalIntFloat;

            case 'L':
                return LocalIntFloat;

            case 'S':
            case 'B':
            case 'T':
                return Int;

            case 'F':
                return Flt;

            case 'A':
                return GlobalIntFloatArr;

            case 'X':
                return LocalIntFloatArr;

            case 'K':
            case 'M':
                return GlobalStr;

            case 'J':
            case 'N':
                return LocalStr;

            case 'R':
            case 'V':
                return GlobalStrArr;

            case 'Z':
            case 'W':
                return LocalStrArr;

            default:
                return Unknown;
        }
    }

    public HighLevelType getAbsolute() {
        if(isFloat()) return Flt;
        if(isInteger()) return Int;
        if(isString()) return Str;

        return this;
    }
}