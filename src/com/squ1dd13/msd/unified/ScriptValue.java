package com.squ1dd13.msd.unified;

import com.squ1dd13.msd.compiler.language.*;
import com.squ1dd13.msd.compiler.text.lexer.*;
import com.squ1dd13.msd.compiler.text.parser.*;
import com.squ1dd13.msd.shared.*;

import java.io.*;
import java.util.*;

public class ScriptValue implements ByteRepresentable {
    public ConcreteType concreteType;
    private AbstractType abstractType;
    public AnyValue value;
    private int length;
    public static boolean readType = true;

    public ScriptValue(RandomAccessFile randomAccessFile) throws IOException {
        int customStringLength = -1;

        if(readType) {
            concreteType = ConcreteType.decode(randomAccessFile.read());
        } else {
            concreteType = ConcreteType.StringVar;
            customStringLength = 128;
        }

        abstractType = concreteType.highLevelType();

        int valueLength = concreteType.valueLength();

        if(abstractType.isInteger()) {
            int[] valueBytes = new int[valueLength];
            for(int i = 0; i < valueLength; ++i) {
                valueBytes[i] = randomAccessFile.read();
            }

            value = AnyValue.with(Util.intFromBytesLE(valueBytes, concreteType.valueLength()));
            length = valueLength + 1;
        } else if(abstractType.isString()) {
            char[] chars;

            if(concreteType == ConcreteType.StringVar) {
                // Read the size and then the characters.
                chars = new char[customStringLength > -1 ? customStringLength : randomAccessFile.read()];
                length = customStringLength > -1 ? customStringLength : (chars.length + 2);

                for(int i = 0; i < chars.length; ++i) {
                    chars[i] = (char)randomAccessFile.read();
                }
            } else {
                // Just read the correct number of characters.
                chars = new char[concreteType == ConcreteType.String8 ? 8 : 16];
                length = chars.length + 1;

                for(int i = 0; i < chars.length; ++i) {
                    char c = (char)randomAccessFile.read();
//                    if(c == 0) break;
                    chars[i] = c;
                }
            }

            // We have to remove the null characters first.
            value = AnyValue.with(Util.removeNullChars(new String(chars)));
        } else if(abstractType.isFloat()) {
            length = 5;

            int[] valueBytes = new int[valueLength];
            for(int i = 0; i < valueLength; ++i) {
                valueBytes[i] = randomAccessFile.read();
            }

            value = AnyValue.with(Util.floatFromBytesLE(valueBytes));
        } else {
            length = 7;
            List<Integer> bytes = new ArrayList<>();
            for(int i = 0; i < 6; ++i) {
                bytes.add(randomAccessFile.read());
            }

            value = AnyValue.array(bytes);
        }

        customStringLength = -1;
    }

    public ScriptValue() {

    }

    public static Optional<ScriptValue> parse(TokenIterator tokenIterator) {
        Token token = tokenIterator.next();

        ScriptValue scriptValue = new ScriptValue();
        switch(token.type) {
            case FloatLiteral:
                scriptValue.abstractType = AbstractType.Flt;
                scriptValue.value = AnyValue.with(token.getFloat());
                break;

            case IntLiteral:
                scriptValue.value = AnyValue.with(token.getInteger());
                break;

            case IdentifierOrKeyword:
                if(!token.getText().matches("&?(global|local)[^_]+_\\d+")) {
                    Util.emitFatalError("Variable names are not yet supported");
                }

                String offsetString = token.getText().split("_")[1];
                scriptValue.value = AnyValue.with(Integer.parseInt(offsetString));
                break;

            case StringLiteral:
                scriptValue.value = AnyValue.with(token.getText());
                break;
        }

        return Optional.of(scriptValue);
    }

    @Override
    public List<Integer> toBytes(Context context) {
        // FIXME: Variable-length string sizes aren't added

        if(concreteType == null) {
            System.out.println("Can't compile ScriptValue with no concrete type set");
        }

        if(abstractType.isInteger()) {
            var intBytes = value.toIntBytes(context, length);

            intBytes.add(0, concreteType.ordinal());

            return intBytes;
        }

        if(abstractType.isFloat()) {
            return Util.intArrayToList(concreteType.toBytes(value.getFloat()));
        }

        if(abstractType.isString()) {
            return Util.intArrayToList(concreteType.toBytes(value.getString()));
        }

        if(abstractType.isArray()) {
            var arrayBytes = value.toBytes(context);
            arrayBytes.add(0, concreteType.ordinal());

            return arrayBytes;
        }

        return new ArrayList<>();
    }

    public int getLength() {
        return length;
    }

    public static String valueToString(AbstractType type, AnyValue value) {
        if(type.isVariable() || type.isArray()) {
            String typeName = type.name();
            typeName = Character.toLowerCase(typeName.charAt(0)) + typeName.substring(1);

            return typeName + "_" + value.toString();
        }

        return value.toString();
    }

    @Override
    public String toString() {
        return valueToString(abstractType, value);
    }
}
