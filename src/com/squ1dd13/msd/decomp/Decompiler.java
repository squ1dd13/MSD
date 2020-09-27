package com.squ1dd13.msd.decomp;

import com.squ1dd13.msd.decomp.dataflow.*;
import com.squ1dd13.msd.shared.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Decompiler {
    private final int[] scriptBytes;
    private int currentPosition;

    public Decompiler(String filePath) throws IOException {
        scriptBytes = Util.byteArrayToIntArray(Files.readAllBytes(Paths.get(filePath)));
    }

    public BasicScript decompile() {
        BasicScript script = new BasicScript();

        while(currentPosition < scriptBytes.length && currentPosition + 1 < scriptBytes.length) {
            int opcode = scriptBytes[currentPosition++] + (scriptBytes[currentPosition++] << 8);

            var instruction = new BasicScript.Instruction(opcode);

            // -2 for the 2 bytes we just read for the opcode.
            instruction.offset = currentPosition - 2;

            Optional<CommandRegistry.CommandEntry> opt = CommandRegistry.getOptional(instruction.opcode);
            if(opt.isEmpty()) {
                System.out.printf("Bad instruction 0x%x\n", instruction.opcode);
                break;
            }

            var commandInfo = opt.get();

            if(commandInfo.isVariadic) {
                while(scriptBytes[currentPosition] != 0) {
                    instruction.arguments.add(readNextValue());
                }
            } else {
                int paramCount = commandInfo.parameterTypes.size();
                for(int i = 0; i < paramCount; ++i) {
                    instruction.arguments.add(readNextValue());
                }
            }

            script.bodyCode.add(Operator.getOverload(instruction));
        }

        return script;
    }

    private BasicScript.BasicValue readNextValue() {
        ConcreteType type = ConcreteType.decode(scriptBytes[currentPosition++]);
        BasicScript.BasicValue value = new BasicScript.BasicValue(type);

        int length = type.valueLength();
        if(length == 0) {
            // Assume array type.
            currentPosition += 6;
            return value;
        }

        if(type == ConcreteType.StringVar) {
            // We can't use the type's valueLength(). The length is in the first byte of the string.
            length = scriptBytes[currentPosition++];
        }

        // We can classify abstract types more easily than concrete ones.
        AbstractType abstractType = type.highLevelType();

        int[] valueBytes = Arrays.copyOfRange(scriptBytes, currentPosition, currentPosition + length);

        if(abstractType.isInteger()) {
            value.intValue = Util.intFromBytesLE(valueBytes, length);
        } else if(abstractType.isFloat()) {
            value.floatValue = Util.floatFromBytesLE(valueBytes);
        } else if(abstractType.isString()) {
            StringBuilder builder = new StringBuilder(length);

            for(int b : valueBytes) {
                if(b == 0) break;
                builder.append((char)b);
            }

            value.stringValue = builder.toString();
        }

        currentPosition += length;

        return value;
    }
}
