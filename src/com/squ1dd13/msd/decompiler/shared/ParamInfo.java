package com.squ1dd13.msd.decompiler.shared;

public class ParamInfo {
    // The type can be anything: int, float, reference to int, reference to float, etc.
    public DataType type;

    // The absolute type is the type of the value. This cannot be a reference type.
    public DataType absoluteType;

    // Whether or not a value is stored in this variable by the command.
    // This can only be true when type is a reference type.
    public boolean isOutVal = false;

    public ParamInfo(DataType type, DataType absoluteType, boolean isOutVal) {
        this.type = type;
        this.absoluteType = absoluteType;
        this.isOutVal = isOutVal;
    }

    // Only handles past the '=' (i.e. does not set isOutVal).
    private static ParamInfo fromStringInternal(String s) {
        switch(s) {
            case "vf":
                return new ParamInfo(DataType.GlobalIntFloat, DataType.Flt, false);

            case "vi":
                return new ParamInfo(DataType.GlobalIntFloat, DataType.Int, false);

            case "lf":
                return new ParamInfo(DataType.LocalIntFloat, DataType.Flt, false);

            case "li":
                return new ParamInfo(DataType.LocalIntFloat, DataType.Int, false);

            case "f":
                // Can still be a reference type if there is an equals sign.
                return new ParamInfo(DataType.Flt, DataType.Flt, false);

            case "i":
                return new ParamInfo(DataType.Int, DataType.Int, false);

            case "vt":
                return new ParamInfo(DataType.GlobalStr, DataType.Str, false);

            case "lt":
                return new ParamInfo(DataType.LocalStr, DataType.Str, false);

            case "t":
                return new ParamInfo(DataType.Str, DataType.Str, false);
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
            }

            return base;
        } catch(Exception e) {
            return null;
        }
    }
}
