package com.squ1dd13.msd;

public class Config {
    public static class DecompilerConfig {
        public boolean threadJumps = true;
        public boolean removeDeadCode = true;
        public boolean buildHighLevelStructures = true;
    }

    public static DecompilerConfig decompiler = new DecompilerConfig();
}
