package com.squ1dd13.msd;

public class Config {
    public static class DecompilerConfig {
        public boolean threadJumps = true;
        public boolean removeDeadCode = true;
        public boolean buildHighLevelStructures = true;

        public DecompilerConfig printWarnings() {
            if(!threadJumps) {
                System.out.println("Warning (decompiler): Jump threading is disabled. Output may be cluttered.");
            }

            if(!removeDeadCode) {
                System.out.println("Warning (decompiler): Dead code is not being removed. This may cause cluttered output and/or inaccurate control flow evaluation.");
            }

            return this;
        }
    }

    public static DecompilerConfig decompiler = new DecompilerConfig().printWarnings();
}
