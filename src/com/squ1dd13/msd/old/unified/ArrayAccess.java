package com.squ1dd13.msd.old.unified;

import com.squ1dd13.msd.old.shared.*;

import java.util.*;

public class ArrayAccess implements ByteRepresentable {
    int startOffset, index, length;
    ElementType type;
    boolean globalIndex;

    public ArrayAccess(List<Integer> bytes) {
        startOffset = Util.intFromBytesLE(new int[]{bytes.get(0), bytes.get(1)}, 2);
        index = Util.intFromBytesLE(new int[]{bytes.get(2), bytes.get(3)}, 2);
        length = bytes.get(4);

        int properties = bytes.get(5);
        globalIndex = (properties & 0x80) != 0;
        type = ElementType.values()[properties & 0x7F];
    }

    @Override
    public List<Integer> toBytes(Context context) {
        List<Integer> bytes = new ArrayList<>(Util.intToByteListLE(startOffset, 2));
        bytes.addAll(Util.intToByteListLE(index, 2));
        bytes.addAll(Util.intToByteListLE(length, 1));

        int indexTypeMask = globalIndex ? 0x80 : 0;
        bytes.add(type.ordinal() | indexTypeMask);

        return bytes;
    }

    @Override
    public String toString() {
        AbstractType indexType = globalIndex ? AbstractType.GlobalIntFloat : AbstractType.LocalIntFloat;
        return String.format("%d<%s, %d>[%s]",
            startOffset,
            type.name(),
            length,
            ScriptValue.valueToString(indexType, AnyValue.with(index))
        );
    }

    private enum ElementType {
        Int,
        Flt,
        String8,
        String16
    }
}
