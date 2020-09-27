package com.squ1dd13.msd.decomp;

import com.squ1dd13.msd.decomp.generic.*;

import java.util.*;

// Glorified map with a few convenience methods. Cuts down on duplicate code.
public class OffsetMap {
    private final Map<Integer, Integer> offsetIndices = new HashMap<>();

    public OffsetMap(CodeContainer<?> container) {
        var instructions = container.getBodyInstructions();

        for(int i = 0; i < instructions.size(); ++i) {
            offsetIndices.put(instructions.get(i).offset, i);
        }
    }

    public int toIndex(int offset) {
        return offsetIndices.getOrDefault(Math.abs(offset), -1);
    }
}
