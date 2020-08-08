package com.squ1dd13.msd.decompiler.high;

import com.squ1dd13.msd.decompiler.shared.*;
import com.squ1dd13.msd.shared.*;

import java.util.*;

// High-level variable information.
public class Variable {
    // All known variables. Key is positive for locals, negative for globals.
    private static final Map<Integer, Variable> variableMap = new HashMap<>();

    public int offset;
    public HighLevelType referenceType = HighLevelType.Unknown;
    public HighLevelType valueType = HighLevelType.Unknown;

    public void registerUse(Command cmd, int index) {
        ParamInfo info = cmd.getParamInfo(index);
        if(info == null) return;

        if(valueType != referenceType && valueType != HighLevelType.Unknown && info.absoluteType != valueType) {
            System.out.println("Warning: Variable with ref type " + referenceType + " used as " + valueType + " and " + info.absoluteType);
            System.out.println("Newest use gets priority.");
        }

        valueType = info.absoluteType;
    }

    private static int dataValueOffset(DataValue dv) {
        if(dv.type.isGlobal()) {
            return -dv.intValue;
        }

        return dv.intValue;
    }

    public static Variable create(DataValue dataValue) {
//        System.out.println(dataValue.type);

        int key = dataValueOffset(dataValue);

        if(variableMap.containsKey(key)) {
            return get(dataValue);
        }

        Variable variable = new Variable();

        variable.offset = dataValue.intValue;
        variable.referenceType = dataValue.type;

        variableMap.put(key, variable);
        return variable;
    }

    public static Variable get(DataValue value) {
        return variableMap.getOrDefault(dataValueOffset(value), null);
    }

    public static boolean isRegistered(DataValue value) {
        if(!value.type.isVariable()) return false;

        return variableMap.containsKey(dataValueOffset(value));
    }
}
