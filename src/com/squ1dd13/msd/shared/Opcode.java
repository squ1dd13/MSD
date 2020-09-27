package com.squ1dd13.msd.shared;

import com.squ1dd13.msd.compiler.language.*;

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
    Terminate(0x4E),
    Call(0x50),
    Return(0x51),
    If(0xD6),
    Switch(0x871);

    private final int num;
    Opcode(int n) {
        num = n;
    }

    public int get() {
        return num;
    }

    public static boolean isJump(int op) {
        return op == Jump.num || op == JumpIfFalse.num;
    }

    public static boolean isCall(int op) {
        return op == Call.num;
    }
}
