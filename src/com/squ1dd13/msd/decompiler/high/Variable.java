package com.squ1dd13.msd.decompiler.high;

import com.squ1dd13.msd.decompiler.shared.*;
import com.squ1dd13.msd.shared.*;

import java.util.*;

// High-level variable information.
public class Variable {
    // All known variables. Key is positive for locals, negative for globals.
    private static final Map<Integer, Variable> variableMap = new HashMap<>();

    public int offset;
    public AbstractType referenceType = AbstractType.Unknown;
    public AbstractType valueType = AbstractType.Unknown;
    private String customName;
    public String customTypeName;

    public void registerUse(Command cmd, int index) {
        ParamInfo info = cmd.getParamInfo(index);
        if(info == null) return;

        if(valueType != referenceType && valueType != AbstractType.Unknown && info.absoluteType != valueType) {
            return;
        }

        valueType = info.absoluteType;
    }

    private static int dataValueOffset(DataValue dv) {
        if(dv.type.isGlobal()) {
            return -dv.intValue;
        }

        return dv.intValue;
    }

    public static Variable getOrCreate(DataValue dataValue) {
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

    public static Variable get(int offset) {
        return variableMap.getOrDefault(offset, null);
    }

    public static boolean isRegistered(DataValue value) {
        if(!value.type.isVariable()) return false;

        return variableMap.containsKey(dataValueOffset(value));
    }

    public boolean hasCustomName() {
        return customName != null;
    }

    public void setCustomName(String name) {
        customName = name;
    }

    public static void set(DataValue dv, Variable v) {
        variableMap.put(dataValueOffset(dv), v);
    }

    @Override
    public String toString() {
        String typeString = customTypeName == null ? valueType.toString() : customTypeName;
        var base = (referenceType.isLocal() ? "local" : "global") + typeString + "_" + offset;

        if(hasCustomName()) return base + "_" + customName;
        return base;
    }
}
