package com.squ1dd13.msd.old.decompiler.shared;

import com.squ1dd13.msd.old.shared.*;

public class ParamInfo {
    // The type can be anything: int, float, reference to int, reference to float, etc.
    public AbstractType type;

    // The absolute type is the type of the value. This cannot be a reference type.
    public AbstractType absoluteType;

    // Whether or not a value is stored in this variable by the command.
    // This can only be true when type is a reference type.
    public boolean isOutVal = false;

    public ConcreteType concreteType;

    public ParamInfo(AbstractType type, AbstractType absoluteType, boolean isOutVal, ConcreteType concreteType) {
        this.type = type;
        this.absoluteType = absoluteType;
        this.isOutVal = isOutVal;
        this.concreteType = concreteType;
    }

    // Only handles past the '=' (i.e. does not set isOutVal).
    private static ParamInfo fromStringInternal(String s) {
        switch(s) {
            case "vf":
                return new ParamInfo(AbstractType.GlobalIntFloat, AbstractType.Flt, false, ConcreteType.GlobalIntFloat);

            case "vi":
                return new ParamInfo(AbstractType.GlobalIntFloat, AbstractType.Int, false, ConcreteType.GlobalIntFloat);

            case "lf":
                return new ParamInfo(AbstractType.LocalIntFloat, AbstractType.Flt, false, ConcreteType.LocalIntFloat);

            case "li":
                return new ParamInfo(AbstractType.LocalIntFloat, AbstractType.Int, false, ConcreteType.LocalIntFloat);

            case "f":
                // Can still be a reference type if there is an equals sign.
                return new ParamInfo(AbstractType.Flt, AbstractType.Flt, false, ConcreteType.Unknown);

            case "i":
                return new ParamInfo(AbstractType.Int, AbstractType.Int, false, ConcreteType.Unknown);

            case "vt":
                return new ParamInfo(AbstractType.GlobalStr, AbstractType.Str, false, ConcreteType.Unknown);

            case "lt":
                return new ParamInfo(AbstractType.LocalStr, AbstractType.Str, false, ConcreteType.Unknown);

            case "t":
                return new ParamInfo(AbstractType.Str, AbstractType.Str, false, ConcreteType.Unknown);

            case "p":
                return new ParamInfo(AbstractType.Int, AbstractType.Int, false, ConcreteType.S32);
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
