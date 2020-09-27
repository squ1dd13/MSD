package com.squ1dd13.msd.decomp.dataflow;

import com.squ1dd13.msd.decomp.*;
import com.squ1dd13.msd.decomp.BasicScript.*;
import com.squ1dd13.msd.misc.*;
import com.squ1dd13.msd.shared.*;

import java.util.*;

public class Operator {
    private final String operatorString;
    private final Map<Integer, Pair<AbstractType, AbstractType>> overloads = new HashMap<>();
    private static final List<Operator> operators = new ArrayList<>();
    private static final Map<Integer, Integer> operatorsForOpcodes = new HashMap<>();

    public static Instruction getOverload(Instruction instruction) {
        int operatorIndex = operatorsForOpcodes.getOrDefault(instruction.opcode, -1);

        if(operatorIndex == -1) {
            // The instruction is not an operator overload.
            return instruction;
        }

        Operator operator = operators.get(operatorIndex);
        var types = operator.overloads.get(instruction.opcode);
        return new OperatorOverload(operator.operatorString, types, instruction);
    }

    private Operator(String declaration) {
        /*

        +=
            0008 = Int, Int

         */

        String[] lines = declaration.split("\n");
        operatorString = lines[0].trim();

        for(int i = 1; i < lines.length; ++i) {
            String line = lines[i].trim();
            if(line.isEmpty()) continue;

            String[] declParts = line.split(" ", 2);
            String[] argTypes = declParts[1].split(",");

            String opcodeHex = declParts[0].trim();
            int opcode = Integer.parseInt(opcodeHex, 16);

            AbstractType leftType = AbstractType.valueOf(argTypes[0].trim());
            AbstractType rightType = AbstractType.valueOf(argTypes[1].trim());

            overloads.put(opcode, new Pair<>(leftType, rightType));
            operatorsForOpcodes.put(opcode, operators.size());
        }
    }

    public static void parse(String all) {
        String[] individualDeclarations = all.split("end");

        for(String decl : individualDeclarations) {
            var operator = new Operator(decl.trim());
            operators.add(operator);
            Highlight.addOperator(operator.operatorString);
        }
    }

    public static class OperatorOverload extends Instruction {
        public Pair<AbstractType, AbstractType> types;
        private final String operatorString;

        public OperatorOverload(String operatorString, Pair<AbstractType, AbstractType> types, Instruction instruction) {
            super(instruction);
            this.operatorString = operatorString;
            this.types = types;
        }

        @Override
        public String toConditionString() {
            return String.format("%s %s %s", getArg(0), operatorString, getArg(1));
        }

        @Override
        public String toCodeString(int indent) {
            // %d: offset
            return String.format("%s%s %s %s;", " ".repeat(indent), getArg(0), operatorString, getArg(1));
        }
    }
}
