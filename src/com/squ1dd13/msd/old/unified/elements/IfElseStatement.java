package com.squ1dd13.msd.old.unified.elements;

import com.squ1dd13.msd.old.shared.*;
import com.squ1dd13.msd.old.unified.*;

import java.util.*;

public class IfElseStatement implements ScriptElement {
    Call ifCall; // if(x)
    List<Call> conditions = new ArrayList<>(); // x_is_y();
    Call bodyJump; // jump_if_false(<end of body>)
    ElementContainer body = new ElementContainer(); // ...
    Call endJump; // jump(<endif>)
    ElementContainer elseBody;
    List<IfElseStatement> elifs = new ArrayList<>();
    int length;

    boolean isAnd;

    public IfElseStatement(PeekIterator<ScriptElement> elementIterator, ElementContainer script) {
        // In case the if statement isn't valid.
        int startIndex = elementIterator.getIndex();

        try {
            ifCall = elementIterator.castNext();
            length += ifCall.getLength();

            IfInfo info = new IfInfo(ifCall.getArgValue(0).getInt());
            isAnd = info.isAnd;

            for(int i = 0; i < info.conditionCount; ++i) {
                conditions.add(elementIterator.castNext());
            }

            bodyJump = elementIterator.castNext();
            length += bodyJump.getLength();
            int bodyEndIndex = script.getIndexForOffset(bodyJump.getArgValue(0).getInt());

            if(bodyEndIndex == Script.invalidIndex) {
                elementIterator.setIndex(startIndex);
                System.out.println("invalid");
                return;
            }

            int currentIndex = 0;
            while(currentIndex < bodyEndIndex) {
                var next = elementIterator.peek();
                if(next instanceof Call) {
                    currentIndex = script.getIndexForOffset(((Call)next).offset);

                    if(currentIndex == Integer.MAX_VALUE) {
                        System.out.println("fail");
                        break;
                    }
                }

                if(currentIndex >= bodyEndIndex) break;
                body.add(next);
                length += next.getLength();

                elementIterator.advance();
            }

            body.buildStructures();
            elementIterator.setIndex(elementIterator.getIndex() - 1);

//        System.out.println("end at " + elementIterator.peek());

            Call possiblyEndJump = elementIterator.castPeek();
            if(possiblyEndJump.getOpcode() != Opcode.Jump.get()) {
                return;
            }

            endJump = possiblyEndJump;
            elementIterator.advance();
            length += endJump.getLength();

            if(elementIterator.hasNext() && elementIterator.peek() instanceof Call) {
                Call call = elementIterator.castPeek();

                if(call.getOpcode() == Opcode.If.get()) {
                    elifs.add(new IfElseStatement(elementIterator, script));
                }
            }
        } catch(Exception e) {
            elementIterator.setIndex(startIndex);
        }
    }

    @Override
    public int getLength() {
        return 0;
    }

    @Override
    public List<String> toLineStrings() {
        List<String> lines = new ArrayList<>();
        StringBuilder builder = new StringBuilder("if(");

        for(int i = 0; i < conditions.size(); i++) {
            builder.append(conditions.get(i).toString());

            if(i != conditions.size() - 1) {
                builder.append(isAnd ? " and " : " or ");
            }
        }

        lines.add(builder.append(") {").toString());

        for(ScriptElement bodyElement : body.elements) {
            var elementLines = bodyElement.toLineStrings();
            for(String line : elementLines) {
                lines.add("$i" + line);
            }
        }

        if(!elifs.isEmpty()) {
            String firstLine = "} else ";
            var elifLines = elifs.get(0).toLineStrings();
            lines.add(firstLine + elifLines.get(0));

            for(int i = 1; i < elifLines.size() - 1; ++i) {
                lines.add(elifLines.get(i));
            }
        }

        lines.add("}");
        return lines;
    }

    @Override
    public List<Integer> toBytes(Context context) {
        return null;
    }

    public boolean isNull() {
        return false;// == null;
    }

    private static class IfInfo {
        int conditionCount;
        boolean isAnd;

        public IfInfo(int numType) {
            if(numType == 0) {
                // Type 0 = 1 condition
                conditionCount = 1;
            } else if(numType < 8) {
                // Types 1->7 = 2->8 conditions and combination AND.
                isAnd = true;
                conditionCount = numType + 1;
            } else if(numType < 28) {
                // Types 21->27 = 2->8 conditions and combination OR.
                isAnd = false;
                conditionCount = numType - 19;
            }
        }
    }
}
