package com.squ1dd13.msd.decomp;

import com.squ1dd13.msd.decomp.generic.*;
import com.squ1dd13.msd.shared.*;

import java.util.*;

public class BasicScript extends CodeContainer<BasicScript.Instruction> {
    public static class BasicValue {
        public ConcreteType valueType;

        public long intValue;
        public float floatValue;
        public String stringValue;

        public BasicValue(ConcreteType type) {
            valueType = type;
        }

        @Override
        public String toString() {
            AbstractType highLevelType = valueType.highLevelType();

            if(highLevelType.isArray()) {
                return "<ARRAY>";
            }

            if(highLevelType.isString()) {
                return String.format("\"%s\"", stringValue);
            }

            if(highLevelType.isFloat()) {
                return String.format("%ff", floatValue);
            }

            if(highLevelType.isInteger()) {
                if(highLevelType.isLocal()) {
                    return String.format("local%s_%d", highLevelType.getAbsolute(), intValue);
                }

                if(highLevelType.isGlobal()) {
                    return String.format("global%s_%d", highLevelType.getAbsolute(), intValue);
                }

                return Long.toString(intValue);
            }

            return "<??>";
        }

        public int getInt() {
            return (int)intValue;
        }
    }

    public static class Instruction extends CodeElement {
        private int opcodeAsRead;

        public int offset;
        public int opcode;
        public boolean conditional;
        public List<BasicValue> arguments = new ArrayList<>();

        public Instruction(int readOpcode) {
            conditional = (readOpcode >> 0xF & 1) != 0;
            opcode = readOpcode & 0x7FFF;
            opcodeAsRead = readOpcode;
        }

        public Instruction(Instruction other) {
            opcode = other.opcode;
            arguments = other.arguments;
            conditional = other.conditional;
            offset = other.offset;
            opcodeAsRead = other.opcodeAsRead;
        }

        public String toConditionString() {
            String name = CommandRegistry.get(opcode).name;

            StringBuilder builder = new StringBuilder(name).append("(");

            for(int i = 0; i < arguments.size(); ++i) {
                BasicValue value = arguments.get(i);
                builder.append(value.toString());

                if(i != arguments.size() - 1) {
                    builder.append(", ");
                }
            }

            return builder.append(")").toString();
        }

        @Override
        public String toCodeString(int indent) {
            String indentStr = " ".repeat(indent);

            String prefix = isLabel ? "label_" + offset + ":\n" : "";

            if(opcode == Opcode.Jump.get() || opcode == Opcode.JumpIfFalse.get()) {
                return prefix + String.format("%sgoto label_%d;", indentStr, getJumpOffset());
            }

            if(opcode == Opcode.Call.get()) {
                // %d: offset
                return prefix + String.format("%sproc_%d();", indentStr, getJumpOffset());
            }

//            if(opcode == Opcode.Jump.get()) {
//                return String.format("%s%d: goto(label_%d);", indentStr, offset, getJumpOffset());
//            }

            String name = CommandRegistry.get(opcode).name;

            StringBuilder builder = new StringBuilder(indentStr)/*.append(String.format("%d: ", offset))*/.append(name).append("(");

            for(int i = 0; i < arguments.size(); ++i) {
                BasicValue value = arguments.get(i);
                builder.append(value.toString());

                if(i != arguments.size() - 1) {
                    builder.append(", ");
                }
            }

            builder.append(");");

//            if(conditional) {
//                builder.append(" // conditional");
//            }

            return prefix + builder.toString();
        }

        @Override
        public Set<Integer> getInstructionOffsets() {
            return Set.of(offset);
        }

        public int getJumpOffset() {
            return arguments.isEmpty() ? -1 : (int)Math.abs(arguments.get(0).intValue);
        }

        public BasicValue getArg(int n) {
            return arguments.get(n);
        }
    }

    public void print() {
        for(Instruction instruction : bodyCode) {
            System.out.println(instruction.toCodeString(0));
        }
    }

    @Override
    public List<Instruction> getBodyInstructions() {
        return bodyCode;
    }

    @Override
    public String toCodeString(int indent) {
        return "script";
    }
}
