package com.squ1dd13.msd.shared;

import com.squ1dd13.msd.decompiler.high.*;

// SCM array
public class ArrayValue {
    public ConcreteType arrayReferenceType;
    public int offsetNumber; // 2 bytes
    public int index; // 2 bytes
    public int size; // 1 byte
    public ArrayElementType elementType; // 7 bits
    public boolean indexIsGlobalVar; // 1 bit

    enum ArrayElementType {
        Int,
        Flt,
        TextLabel,
        TextLabel16;

        @Override
        public String toString() {
            switch(this) {
                case Int:
                    return "Int";
                case Flt:
                    return "Float";
                case TextLabel:
                    return "String";
                case TextLabel16:
                    return "String16";
            }

            return "wtf";
        }
    };

    public ArrayValue(ConcreteType refType, int[] bytes) {
        arrayReferenceType = refType;
        offsetNumber = bytes[0] | bytes[1] << 8;
        index = bytes[2] | bytes[3] << 8;
        size = bytes[4];
        elementType = ArrayElementType.values()[bytes[5] & 0b01111111];
        indexIsGlobalVar = bytes[5] >> 7 == 1;
    }

    @Override
    public String toString() {
        return toCodeString();
    }

    private String createArrayName() {
        AbstractType abstractType = arrayReferenceType.highLevelType();
        return (abstractType.isGlobal() ? "global" : "local") + elementType + "Array_" + offsetNumber;
    }

    public String toCodeString() {
        String indexString = Integer.toString(index);
        if(indexIsGlobalVar) {
            Variable variable = Variable.get(-index);
            if(variable == null) {
                indexString = "globalX_" + index;
            } else {
                indexString = variable.toString();
            }
        }

        return String.format("%s[%s]", createArrayName(), indexString);
    }
}
