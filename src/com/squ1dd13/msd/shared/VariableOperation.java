package com.squ1dd13.msd.shared;

import com.squ1dd13.msd.decompiler.high.*;
import com.squ1dd13.msd.decompiler.shared.*;

import java.util.*;

import static com.squ1dd13.msd.shared.AbstractType.*;

public class VariableOperation {
    // Absolute operand types (int/float/string).
    public AbstractType leftType, rightType;

    private DataValue left, right;
    public String operatorString;
    public int opcode;

    // Actual operand types (references).
    public AbstractType actualLeft, actualRight;

    public VariableOperation(AbstractType leftType, String operatorString, AbstractType rightType, int opcode) {
        this.leftType = leftType;
        this.rightType = rightType;
        this.operatorString = operatorString;
        this.opcode = opcode;
    }

    public VariableOperation(DataValue left, String operatorString, AbstractType rightType, int opcode) {
        this.leftType = left.type;
        this.left = left;
        this.rightType = rightType;
        this.operatorString = operatorString;
        this.opcode = opcode;
    }

    private VariableOperation withTypes(AbstractType l, AbstractType r) {
        VariableOperation copy = new VariableOperation(leftType, operatorString, rightType, opcode);
        copy.actualLeft = l;
        copy.actualRight = r;

        return copy;
    }

    private static VariableOperation V(AbstractType leftType, String operatorString, AbstractType rightType, int opcode) {
        return new VariableOperation(leftType, operatorString, rightType, opcode);
    }

    // Temporary
    private static final Set<VariableOperation> operations = Set.of(
        V(Flt, "=", Flt, 0x5).withTypes(GlobalIntFloat, Flt),
        V(Int, "==", Int, 0x38).withTypes(GlobalIntFloat, Int),
        V(Flt, "+=", Flt, 0x59).withTypes(GlobalIntFloat, GlobalIntFloat),
        V(Int, ">", Int, 0x18).withTypes(GlobalIntFloat, Int),
        V(Int, "=", Int, 0x4).withTypes(GlobalIntFloat, Int),
        V(Int, "==", Int, 0x4A3).withTypes(GlobalIntFloat, Int),
        V(Str, "=", Str, 0x5A9).withTypes(GlobalStr, Str),
        V(Int, "+=", Int, 0x8).withTypes(GlobalIntFloat, Int),
        V(Int, "<", Int, 0x1A).withTypes(Int, GlobalIntFloat),
        V(Int, "=", Int, 0x84).withTypes(GlobalIntFloat, GlobalIntFloat),
        V(Str, "==", Str, 0x5AD).withTypes(GlobalStr, Str),
        V(Int, ">", Int, 0x1C).withTypes(GlobalIntFloat, GlobalIntFloat),
        V(Str, "=", Str, 0x6D1).withTypes(GlobalStr, Str),
        V(Flt, "=", Flt, 0x86).withTypes(GlobalIntFloat, GlobalIntFloat),
        V(Flt, "+=", Flt, 0x9).withTypes(LocalIntFloat, Flt),
        V(Int, ">=", Int, 0x28).withTypes(GlobalIntFloat, Int),
        V(Flt, "==", Flt, 0x42).withTypes(GlobalIntFloat, Flt),
        V(Int, "=", Int, 0x6).withTypes(LocalIntFloat, Int),
        V(Int, ">=", Int, 0x2C).withTypes(GlobalIntFloat, GlobalIntFloat),
        V(Int, ">=", Int, 0x29).withTypes(LocalIntFloat, Int),
        V(Flt, "==", Flt, 0x44).withTypes(GlobalIntFloat, GlobalIntFloat),
        V(Int, "==", Int, 0x39).withTypes(LocalIntFloat, Int),
        V(Int, "==", Int, 0x4A4).withTypes(LocalIntFloat, Int),
        V(Int, "+=", Int, 0xA).withTypes(LocalIntFloat, Int),
        V(Int, ">", Int, 0x19).withTypes(LocalIntFloat, Int),
        V(Int, "=", Int, 0x85).withTypes(LocalIntFloat, LocalIntFloat),
        V(Flt, "-", Flt, 0xD).withTypes(GlobalIntFloat, Flt)
    );

    private static final HashMap<Integer, VariableOperation> operationsMap = new HashMap<>();
    public static void build() {
        if(!operationsMap.isEmpty()) return;

        for(var op : operations) {
            operationsMap.put(op.opcode, op);
        }
    }

    public static VariableOperation forCommand(Command cmd) {
        build();
        var op = operationsMap.getOrDefault(cmd.opcode, null);
        if(op == null) return null;
        return op.withCommand(cmd);
    }

    public static Optional<VariableOperation> forOperatorWithTypes(String operator, AbstractType leftType, AbstractType rightType) {
        VariableOperation operation = null;

        for(VariableOperation variableOperation : operationsMap.values()) {
            if(variableOperation.operatorString.equals(operator)) {
                if(variableOperation.leftType == leftType && variableOperation.rightType == rightType) {
                    operation = variableOperation;
                    break;
                }
            }
        }

        return Optional.ofNullable(operation);
    }

    private VariableOperation withCommand(Command cmd) {
        VariableOperation copy = new VariableOperation(leftType, operatorString, rightType, opcode);
        copy.left = cmd.arguments[0];
        copy.right = cmd.arguments[1];

        if(!leftType.isVariable()) {
            Variable.getOrCreate(copy.left).valueType = leftType.getAbsolute();
        }

        if(!rightType.isVariable()) {
            Variable.getOrCreate(copy.right).valueType = rightType.getAbsolute();
        }

        return copy;
    }

    public List<String> combineWith(VariableOperation other) {
        if(operatorString.equals("=") && other.operatorString.equals("+=") && left.equalsVariable(other.left)) {
            final String assignAddedFormat = "%s = %s + %s";
            return List.of(String.format(assignAddedFormat, left, right, other.right));
        }

        return List.of(toString(), other.toString());
    }

    @Override
    public String toString() {
        return String.format("%s %s %s", left, operatorString, right);
    }
}
