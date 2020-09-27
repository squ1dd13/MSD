package com.squ1dd13.msd.decomp;

public class RenamedInstruction extends BasicScript.Instruction {
    private String outputString;

    public RenamedInstruction(int readOpcode) {
        super(readOpcode);
    }

    public RenamedInstruction(BasicScript.Instruction other) {
        super(other);
    }

    public String getOutputString() {
        return outputString;
    }

    public void setOutputString(String outputString) {
        if(this.outputString == null) this.outputString = outputString;
    }

    @Override
    public String toCodeString(int indent) {
        return String.format("%s%s;", " ".repeat(indent), outputString);
    }
}
