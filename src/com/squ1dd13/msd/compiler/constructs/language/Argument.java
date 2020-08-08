package com.squ1dd13.msd.compiler.constructs.language;

import com.squ1dd13.msd.compiler.constructs.*;
import com.squ1dd13.msd.decompiler.shared.*;
import com.squ1dd13.msd.shared.*;

import java.util.*;

public class Argument implements Compilable {
    public LowLevelType type = LowLevelType.Unknown;

    public int intValue = 0;
    public float floatValue = 0;
    public String stringValue = "";

    public Argument(LowLevelType type, int intValue) {
        this.type = type;
        this.intValue = intValue;
    }

    public Argument(LowLevelType type, float floatValue) {
        this.type = type;
        this.floatValue = floatValue;
    }

    public Argument(LowLevelType type, String stringValue) {
        this.type = type;
        this.stringValue = stringValue;
    }

    public Argument(LowLevelType type, int intValue, float floatValue, String stringValue) {
        this.type = type;
        this.intValue = intValue;
        this.floatValue = floatValue;
        this.stringValue = stringValue;
    }

    @Override
    public Collection<Integer> compile(Context context) {
        DataType hlt = type.highLevelType();

        int[] bytes = null;

        if(hlt == DataType.End) {
            if(intValue != 0) {
                hlt = DataType.Int;
                type = LowLevelType.S16;
            } else if(!stringValue.isEmpty()) {
                System.out.println("yeee");
                hlt = DataType.Str;
                type = LowLevelType.String8;
            } else if(floatValue != 0) {
                hlt = DataType.Flt;
                type = LowLevelType.F32;
            } else {
                hlt = DataType.Int;
                type = LowLevelType.S8;
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
            System.err.println("Error: Unsupported type " + type + " cannot be compiled.");
//            System.exit(1);
//            return null;
            bytes = new int[1];
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
