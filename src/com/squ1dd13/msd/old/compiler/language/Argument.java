package com.squ1dd13.msd.old.compiler.language;


import com.squ1dd13.msd.old.shared.*;

import java.util.*;

public class Argument {
    public ConcreteType type;

    public int intValue;
    public float floatValue;
    public String stringValue = "";

    public Argument(ConcreteType type, int intValue) {
        this.type = type;
        this.intValue = intValue;
    }

    public Argument(ConcreteType type, float floatValue) {
        this.type = type;
        this.floatValue = floatValue;
    }

    public Argument(ConcreteType type, String stringValue) {
        this.type = type;
        this.stringValue = stringValue;
    }

    public Argument(ConcreteType type, int intValue, float floatValue, String stringValue) {
        this.type = type;
        this.intValue = intValue;
        this.floatValue = floatValue;
        this.stringValue = stringValue;
    }

    @Override
    public String toString() {
        var hlt = type.highLevelType();

        if(hlt.isInteger()) {
            return Integer.toString(intValue);
        }

        if(hlt.isString()) {
            return stringValue;
        }

        if(hlt.isFloat()) {
            return Float.toString(floatValue);
        }

        return "<?>";
    }

    public Collection<Integer> compile() {
        AbstractType hlt = type.highLevelType();

        int[] bytes = null;

        if(hlt == AbstractType.End) {
            if(intValue != 0) {
                hlt = AbstractType.Int;
                type = ConcreteType.S16;
            } else if(!stringValue.isEmpty()) {
                System.out.println("yeee");
                hlt = AbstractType.Str;
                type = ConcreteType.String8;
            } else if(floatValue != 0) {
                hlt = AbstractType.Flt;
                type = ConcreteType.F32;
            } else {
                hlt = AbstractType.Int;
                type = ConcreteType.S8;
            }
        }

        if(hlt.isInteger()) {
            bytes = type.toBytes(intValue);
        } else if(hlt.isFloat()) {
            bytes = type.toBytes(floatValue);
        } else if(hlt.isString()) {
            bytes = type.toBytes(stringValue);
        }

        if(bytes == null) {
            Util.emitFatalError("Unsupported type " + type + " cannot be compiled.");
            return new ArrayList<>();
        }

        // Convert to a list and add the type identifier.
        var byteList = Util.intArrayToList(bytes);
        if(type.highLevelType().isInteger()) {
            byteList = byteList.subList(0, type.valueLength());
        }

        byteList.add(0, type.ordinal());

        return byteList;
    }
}
