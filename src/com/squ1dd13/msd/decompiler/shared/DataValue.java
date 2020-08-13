package com.squ1dd13.msd.decompiler.shared;

import com.squ1dd13.msd.decompiler.high.*;
import com.squ1dd13.msd.shared.*;

public class DataValue {
    public AbstractType type = AbstractType.Unknown;
    public String stringValue;
    public int intValue;
    public float floatValue;
    public ArrayValue arrayValue;

    public DataValue(){}

    public DataValue(String s) {
        if(s.length() < 2) {
            System.err.println("Can't parse " + s);
            return;
        }

        type = AbstractType.typeForString(s);

        if(type == AbstractType.Str) {
            stringValue = s;
        } else if(type == AbstractType.Int
            || type == AbstractType.GlobalIntFloat
            || type == AbstractType.GlobalStr
            || type == AbstractType.LocalIntFloat
            || type == AbstractType.LocalStr) {
            intValue = Integer.parseInt(s.substring(1));
        } else if(type == AbstractType.Flt) {
            floatValue = Float.parseFloat(s.substring(1));
        }
    }

    public DataValue(AbstractType type, int intValue) {
        this.type = type;
        this.intValue = intValue;
    }

    public static DataValue withString(String s) {
        DataValue value = new DataValue();
        value.type = AbstractType.Str;
        value.stringValue = s;

        return value;
    }

    public boolean equalsVariable(DataValue other) {
        return type == other.type && intValue == other.intValue;
    }

    @Override
    public String toString() {
        if(Variable.isRegistered(this)) {
            var variable = Variable.get(this);
            return variable.toString();
        }

        switch(type) {
            case LocalIntFloat:
                return "localNumber_" + intValue;

            case LocalStr:
                return "localString_" + intValue;

            case GlobalIntFloat:
                return "globalNumber_" + intValue;

            case GlobalStr:
                return "globalString_" + intValue;

            case Flt:
                return floatValue + "f";

            case Int:
                return String.valueOf(intValue);

            case Str:
                return "\"" + stringValue + "\"";
        }

        final String base = "(" + type.name() + ")";

        if(type.isString()) {
            return base + stringValue;
        }

        if(type.isArray()) {
            return arrayValue.toCodeString();
        }

        return "<unknown>";
    }
}
