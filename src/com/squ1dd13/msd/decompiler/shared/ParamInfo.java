package com.squ1dd13.msd.decompiler.shared;

import com.squ1dd13.msd.shared.*;

public class ParamInfo {
    // The type can be anything: int, float, reference to int, reference to float, etc.
    public HighLevelType type;

    // The absolute type is the type of the value. This cannot be a reference type.
    public HighLevelType absoluteType;

    // Whether or not a value is stored in this variable by the command.
    // This can only be true when type is a reference type.
    public boolean isOutVal = false;

    public LowLevelType lowLevelType;

    public ParamInfo(HighLevelType type, HighLevelType absoluteType, boolean isOutVal, LowLevelType lowLevelType) {
        this.type = type;
        this.absoluteType = absoluteType;
        this.isOutVal = isOutVal;
        this.lowLevelType = lowLevelType;
    }

    // Only handles past the '=' (i.e. does not set isOutVal).
    private static ParamInfo fromStringInternal(String s) {
        switch(s) {
            case "vf":
                return new ParamInfo(HighLevelType.GlobalIntFloat, HighLevelType.Flt, false, LowLevelType.GlobalIntFloat);

            case "vi":
                return new ParamInfo(HighLevelType.GlobalIntFloat, HighLevelType.Int, false, LowLevelType.GlobalIntFloat);

            case "lf":
                return new ParamInfo(HighLevelType.LocalIntFloat, HighLevelType.Flt, false, LowLevelType.LocalIntFloat);

            case "li":
                return new ParamInfo(HighLevelType.LocalIntFloat, HighLevelType.Int, false, LowLevelType.LocalIntFloat);

            case "f":
                // Can still be a reference type if there is an equals sign.
                return new ParamInfo(HighLevelType.Flt, HighLevelType.Flt, false, LowLevelType.Unknown);

            case "i":
                return new ParamInfo(HighLevelType.Int, HighLevelType.Int, false, LowLevelType.Unknown);

            case "vt":
                return new ParamInfo(HighLevelType.GlobalStr, HighLevelType.Str, false, LowLevelType.Unknown);

            case "lt":
                return new ParamInfo(HighLevelType.LocalStr, HighLevelType.Str, false, LowLevelType.Unknown);

            case "t":
                return new ParamInfo(HighLevelType.Str, HighLevelType.Str, false, LowLevelType.Unknown);

            case "p":
                return new ParamInfo(HighLevelType.Int, HighLevelType.Int, false, LowLevelType.S32);
        }

        return null;
    }

    public static ParamInfo fromString(String s) {
        try {
            ParamInfo base = null;

            if(s.startsWith("=")) {
                base = fromStringInternal(s.substring(1));

                // If the type is primitive, it just means that both global and local refs can be passed.
                // In all cases, isOutVal should be true here.
                base.isOutVal = true;
            } else {
                base = fromStringInternal(s);
//                if(base.absoluteType == DataType.Int && base.lowLevelType == LowLevelType.Unknown) {
//                    base.lowLevelType = LowLevelType.S16;
//                }
            }

            return base;
        } catch(Exception e) {
            return null;
        }
    }
}
