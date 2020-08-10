package com.squ1dd13.msd.compiler.language;

import com.squ1dd13.msd.shared.*;

// Should be used for typename lookups when MSD code is high-level enough for that.
public class Typename {
    public static String get(HighLevelType type) {
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
