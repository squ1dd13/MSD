package com.squ1dd13.msd.old.unified;

// Used for passing information between levels (e.g. main parser to call parser, compiler to script call).
public interface Context {
    <T> T getValue(String valueName);
    <T> void setValue(String valueName, T value);
}
