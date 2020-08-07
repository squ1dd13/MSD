package com.squ1dd13.msd.compiler.constructs.language;

import com.squ1dd13.msd.decompiler.shared.*;

public class Typename {
    public static String get(DataType type) {
        switch(type) {
            case Int:
                return "int";
            case Flt:
                return "float";
            case Str:
                return "string";
            case GlobalIntFloat:
                return "GlobalNumber";
            case LocalIntFloat:
                return "LocalNumber";
            case GlobalIntFloatArr:
                return "GlobalNumberArray";
            case LocalIntFloatArr:
                return "LocalNumberArray";
            case GlobalStr:
                return "GlobalString";
            case LocalStr:
                return "LocalString";
            case GlobalStrArr:
                return "GlobalStringArray";
            case LocalStrArr:
                return "LocalStringArray";
        }

        return type.toString();
    }
}
