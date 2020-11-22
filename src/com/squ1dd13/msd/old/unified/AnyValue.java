package com.squ1dd13.msd.old.unified;

import com.squ1dd13.msd.old.shared.*;

import java.util.*;

public class AnyValue implements ByteRepresentable {
    private int intValue;
    private float floatValue;
    private String stringValue;
    private ArrayAccess arrayValue;

    private boolean hasInt, hasFloat, hasString, hasArray;

    public static <T> AnyValue with(T value) {
        AnyValue any = new AnyValue();

        if(value instanceof Integer) {
            any.setInt((Integer)value);
        } else if(value instanceof Float) {
            any.setFloat((Float)value);
        } else if(value instanceof String) {
            any.setString((String)value);
        } else if(value instanceof ArrayAccess) {
            any.setArray((ArrayAccess)value);
        } else {
            System.out.println("Cannot create AnyValue with object of type " + value.getClass());
        }

        return any;
    }

    public static AnyValue array(List<Integer> bytes) {
        return with(new ArrayAccess(bytes));
    }

    @Override
    public List<Integer> toBytes(Context context) {
        if(hasInt) {
            return toIntBytes(context, 4);
        }

        if(hasFloat) {
            return Util.floatToByteListLE(floatValue);
        }

        if(hasString) {
            return Util.byteArrayToIntList(stringValue.getBytes());
        }

        if(hasArray) {
            return arrayValue.toBytes(context);
        }

        System.out.println("Invalid AnyValue cannot be converted to byte list");
        return new ArrayList<>();
    }

    public List<Integer> toIntBytes(Context context, int n) {
        return Util.intToByteListLE(getInt());
    }

    public long getUnsignedInt() {
        return Integer.toUnsignedLong(getInt());
    }

    public int getInt() {
        if(!hasInt) {
            System.out.println("getInt() should not be used when no integer value is contained");
        }

        return intValue;
    }

    public void setInt(int intValue) {
        this.intValue = intValue;
        hasInt = true;
    }

    public float getFloat() {
        if(!hasFloat) {
            System.out.println("getFloat() should not be used when no float value is contained");
        }

        return floatValue;
    }

    public void setFloat(float floatValue) {
        this.floatValue = floatValue;
        hasFloat = true;
    }

    public String getString() {
        if(!hasString) {
            System.out.println("getString() should not be used when no string value is contained");
        }

        return stringValue;
    }

    public void setString(String stringValue) {
        this.stringValue = stringValue;
        hasString = true;
    }

    public ArrayAccess getArray() {
        if(!hasArray) {
            System.out.println("getArray() should not be used when no array value is contained");
        }

        return arrayValue;
    }

    public void setArray(ArrayAccess arrayValue) {
        this.arrayValue = arrayValue;
        hasArray = true;
    }

    @Override
    public String toString() {
        if(hasInt) {
            return String.valueOf(intValue);
        }

        if(hasFloat) {
            return String.valueOf(floatValue);
        }

        if(hasString) {
            return '"' + stringValue + '"';
        }

        if(hasArray) {
            return arrayValue.toString();
        }

        return "<??>";
    }
}
