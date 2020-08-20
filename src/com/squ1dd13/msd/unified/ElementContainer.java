package com.squ1dd13.msd.unified;

import com.squ1dd13.msd.shared.*;
import com.squ1dd13.msd.unified.elements.*;

import java.util.*;

public class ElementContainer {
    public static final int invalidIndex = Integer.MAX_VALUE;
    final HashMap<Integer, Integer> offsetsToIndices = new HashMap<>();
    public List<ScriptElement> elements = new ArrayList<>();

    public void buildStructures() {
        if(elements.size() == 1) {
            return;
        }

        for(var elem : elements) {
            if(!(elem instanceof Call)) continue;

            Call call = (Call)elem;
            if(Opcode.isJump(call.getOpcode())) {
                var jumpedTo = getCallAtOffset(call.getArgValue(0).getInt());
                jumpedTo.ifPresent(value -> value.comment = String.valueOf(value.offset));
            }
        }

        List<ScriptElement> structureElements = new ArrayList<>();

        PeekIterator<ScriptElement> elementIterator = new PeekIterator<>(elements);
        boolean lastWasNull = false;
        while(elementIterator.hasNext()) {
            ScriptElement element = elementIterator.peek();

            if(!lastWasNull && element instanceof Call) {
                if(((Call)element).getOpcode() == Opcode.If.get()) {
                    int pos = elementIterator.getIndex();
                    var statement = new IfElseStatement(elementIterator, this);
                    if(elementIterator.getIndex() == pos) {
                        lastWasNull = true;
                        continue;
                    }
//                    elementIterator.advance();

                    structureElements.add(statement);
                    continue;
                }
            }

            lastWasNull = false;

            elementIterator.advance();
            structureElements.add(element);
        }

        elements.clear();
        elements.addAll(structureElements);
        offsetsToIndices.clear();
    }

    public ScriptElement getElementAtIndex(int index) {
        return (index >= elements.size()) ? null : elements.get(index);
    }

    private void buildMapIfNeeded() {
        if(offsetsToIndices.isEmpty()) {
            // Build the offset->index map.
            for(int i = 0; i < elements.size(); i++) {
                ScriptElement element = elements.get(i);

                if(element instanceof Call) {
                    offsetsToIndices.put(((Call)element).offset, i);
                }
            }
        }
    }

    public int getIndexForOffset(int offset) {
        buildMapIfNeeded();
        return offsetsToIndices.getOrDefault(offset, offsetsToIndices.getOrDefault(-offset, Integer.MAX_VALUE));
    }

    public Optional<Call> getCallAtOffset(int offset) {
        buildMapIfNeeded();

        // If we can't find the offset with its original sign, reverse the sign and try again.
        // This is useful because jumps and calls use negative offsets.
        var index = offsetsToIndices.getOrDefault(
            offset,
            offsetsToIndices.getOrDefault(-offset, -1)
        );

        if(index == -1) return Optional.empty();

        var element = getElementAtIndex(index);

        if(element == null) {
            return Optional.empty();
        }

        return element instanceof Call ? Optional.of((Call)element) : Optional.empty();
    }

    public void add(ScriptElement element) {
        elements.add(element);
    }
}
