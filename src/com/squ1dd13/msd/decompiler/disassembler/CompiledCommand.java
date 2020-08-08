package com.squ1dd13.msd.decompiler.disassembler;

import com.squ1dd13.msd.decompiler.shared.*;
import com.squ1dd13.msd.shared.*;

import java.util.*;

public class CompiledCommand {
    public static class CompiledArgument {
        public int typeCode;
        public int[] valueBytes;
        public int length;

        public LowLevelType lowLevelType() {
            return LowLevelType.decode(typeCode);
        }

        public CompiledArgument(int[] bytes) {
            typeCode = bytes[0];

            valueBytes = new int[bytes.length - 1];
            System.arraycopy(bytes, 1, valueBytes, 0, bytes.length - 1);
        }

        public DataValue toDataValue() {
            LowLevelType type = lowLevelType();

            DataValue value = new DataValue();
            value.type = LowLevelType.decode(typeCode).highLevelType();

            if(value.type.isInteger()) {
                value.intValue = Util.intFromBytesLE(valueBytes, type.valueLength());
                length = type.valueLength() + 1;
            } else if(value.type.isString()) {
                char[] chars;

                if(type == LowLevelType.StringVar) {
                    // Read the size and then the characters.
                    chars = new char[valueBytes[0]];
                    length = valueBytes[0] + 1;

                    for(int i = 1; i < valueBytes.length; ++i) {
                        chars[i - 1] = (char)valueBytes[i];
                    }
                } else {
                    // Just read the correct number of characters (or until null found).
                    chars = new char[type == LowLevelType.String8 ? 8 : 16];
                    length = chars.length + 1;

                    for(int i = 0; i < chars.length; ++i) {
                        if(valueBytes[i] == 0) break;
                        chars[i] = (char)valueBytes[i];
                    }
                }

                value.stringValue = new String(chars);
            } else if(value.type.isFloat()) {
                length = 5;
                value.floatValue = Util.floatFromBytesLE(valueBytes);
            } else {
                length = type.valueLength() + 1;
                System.out.println("Unsupported type: " + type);
            }

            return value;
        }
    }

    public int opcode;
    public List<CompiledArgument> compiledArguments = new ArrayList<>();
    public int length;
    public int offset;

    public CompiledCommand(int[] bytes) {
        opcode = bytes[0] | bytes[1] << 8;

        if(opcode == 0) {
            opcode = Integer.MAX_VALUE;
            return;
        }

        Command cmd = Command.commands.getOrDefault(opcode, null);
        if(cmd == null) {
            if((cmd = Command.commands.getOrDefault(opcode & 0xFFF, null)) == null) {
                System.out.println("Unknown opcode " + Integer.toHexString(opcode));
                return;
            }

            opcode &= 0xFFF;
        }

        int numArgs = (int)(cmd.name.chars().filter(c -> c == ',').count() + 1);
        if(cmd.name.lastIndexOf('(') == cmd.name.lastIndexOf(')') - 1) numArgs = 0;

        List<Integer> byteList = Util.intArrayToList(bytes);

        int byteIndex = 2;
        for(int i = 0; i < numArgs; ++i) {
            int[] nextBytes = Util.intListToArray(
                byteList.subList(byteIndex, byteList.size())
            );

            var arg = new CompiledArgument(nextBytes);
            arg.toDataValue();
            compiledArguments.add(arg);
            byteIndex += arg.length;
        }

        length = byteIndex;
    }

    public Command toCommand() {
        Command base = Command.commands.get(opcode).copy();
        base.arguments = new DataValue[compiledArguments.size()];
        base.offset = offset;

        for(int i = 0; i < compiledArguments.size(); ++i) {
            base.arguments[i] = compiledArguments.get(i).toDataValue();
        }

        return base;
    }
}
