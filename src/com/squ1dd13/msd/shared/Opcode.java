package com.squ1dd13.msd.shared;

// Opcodes that are useful for us to know.
public enum Opcode {
    // Relative jumps don't really exist. They're placeholders for real jumps
    //  that are used before the real jump destination is known. The opcode
    //  value is too large to be compiled (it takes advantage of the fact
    //  that we always use 'int' to store opcodes).
    RelativeConditionalJump(0x60701A),
    RelativeUnconditionalJump(0x60701B),

    Jump(0x2),

    JumpIfFalse(0x4D),
    Call(0x50),
    If(0xD6);

    private final int num;
    Opcode(int n) {
        num = n;
    }

    public int get() {
        return num;
    }
}
