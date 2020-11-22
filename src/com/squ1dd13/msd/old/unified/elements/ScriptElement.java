package com.squ1dd13.msd.old.unified.elements;

import com.squ1dd13.msd.old.unified.*;

import java.util.*;

public interface ScriptElement extends ByteRepresentable {
    // For calculating offsets.
    int getLength();

    String comment = null;
    List<String> toLineStrings();
}
