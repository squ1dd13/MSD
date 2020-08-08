package com.squ1dd13.msd.decompiler.shared;

import com.squ1dd13.msd.decompiler.high.*;
import com.squ1dd13.msd.shared.*;

public class DataValue {
    public HighLevelType type = HighLevelType.Unknown;
    public String stringValue;
    public int intValue;
    public float floatValue;

    public DataValue(){}

    public DataValue(String s) {
        if(s.length() < 2) {
            System.err.println("Can't parse " + s);
            return;
        }

        type = HighLevelType.typeForString(s);

        if(type == HighLevelType.Str) {
            stringValue = s;
        } else if(type == HighLevelType.Int
            || type == HighLevelType.GlobalIntFloat
            || type == HighLevelType.GlobalStr
            || type == HighLevelType.LocalIntFloat
            || type == HighLevelType.LocalStr) {
            intValue = Integer.parseInt(s.substring(1));
        } else if(type == HighLevelType.Flt) {
            floatValue = Float.parseFloat(s.substring(1));
        }
    }

    @Override
    public String toString() {
        if(Variable.isRegistered(this)) {
            var variable = Variable.get(this);
            return (type.isLocal() ? "local" : "global") + variable.valueType + "_" + intValue;
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
                return intValue + "";

            case Str:
                return "'" + stringValue + "'";
        }

        final String base = "(" + type.name() + ")";

        if(type.isString()) {
            return base + stringValue;
        }

        if(type.isArray()) {
            return "<value>";
        }

        return "<unknown>";
    }
}
