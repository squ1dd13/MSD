package com.squ1dd13.msd.decompiler.disassembler;

import com.squ1dd13.msd.decompiler.shared.*;
import com.squ1dd13.msd.shared.*;

import java.util.*;

public class ReversedCommand {
    public int opcode;
    public List<ReversedArgument> reversedArguments = new ArrayList<>();
    public int length;
    public int offset;
    public int originalOpcode;
    public ReversedCommand(int[] bytes) {
        originalOpcode = opcode = bytes[0] | bytes[1] << 8;

        if(opcode == 0) {
            opcode = Integer.MAX_VALUE;
            return;
        }

        Command cmd = Command.commands.getOrDefault(opcode, null);
        if(cmd == null) {
            if((cmd = Command.commands.getOrDefault(opcode & 0xFFF, null)) == null) {
                Util.emitFatalError("Unknown opcode " + Integer.toHexString(opcode));
                return;
            }

            opcode &= 0xFFF;
        }

//        System.out.print(cmd.name);

        int numArgs = (int)(cmd.name.chars().filter(c -> c == ',').count() + 1);
        if(cmd.name.lastIndexOf('(') == cmd.name.lastIndexOf(')') - 1) numArgs = 0;

        List<Integer> byteList = Util.intArrayToList(bytes);

        int byteIndex = 2;
        for(int i = 0; i < numArgs; ++i) {
            int[] nextBytes = Util.intListToArray(
                byteList.subList(byteIndex, byteList.size())
            );

            var arg = new ReversedArgument(nextBytes);
            arg.toDataValue();
            reversedArguments.add(arg);

            byteIndex += arg.length;
        }

//        System.out.println();

        length = byteIndex;
    }

    public Command toCommand() {
        Command base = Command.commands.get(opcode).copy();
        base.arguments = new DataValue[reversedArguments.size()];
        base.offset = offset;

        for(int i = 0; i < reversedArguments.size(); ++i) {
            base.arguments[i] = reversedArguments.get(i).toDataValue();
        }

        return base;
    }

    public static class ReversedArgument {
        public int typeCode;
        public int[] valueBytes;
        public int length;

        public ReversedArgument(int[] bytes) {
            typeCode = bytes[0];
            if(typeCode > 0x13) {
                bytes = Util.subArray(bytes, 1, bytes.length);
                typeCode = bytes[0];
            }

            valueBytes = new int[bytes.length - 1];
            System.arraycopy(bytes, 1, valueBytes, 0, bytes.length - 1);
        }

        public ConcreteType lowLevelType() {
            return ConcreteType.decode(typeCode);
        }

        public DataValue toDataValue() {
            ConcreteType type = lowLevelType();

            DataValue value = new DataValue();
            value.type = ConcreteType.decode(typeCode).highLevelType();

            if(value.type.isInteger()) {
                value.intValue = Util.intFromBytesLE(valueBytes, type.valueLength());
                length = type.valueLength() + 1;
            } else if(value.type.isString()) {
                char[] chars;

                if(type == ConcreteType.StringVar) {
                    // Read the size and then the characters.
                    chars = new char[valueBytes[0]];
                    length = valueBytes[0] + 2;

                    for(int i = 1; i < valueBytes.length && i - 1 < chars.length; ++i) {
                        chars[i - 1] = (char)valueBytes[i];
                    }
                } else {
                    // Just read the correct number of characters (or until null found).
                    chars = new char[type == ConcreteType.String8 ? 8 : 16];
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
                length = 7;
                value.arrayValue = new ArrayValue(type, valueBytes);
            }

            return value;
        }
    }
}
