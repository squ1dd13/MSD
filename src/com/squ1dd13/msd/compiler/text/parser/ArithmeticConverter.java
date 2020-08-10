package com.squ1dd13.msd.compiler.text.parser;

import com.squ1dd13.msd.compiler.text.lexer.*;
import com.squ1dd13.msd.shared.*;

import java.util.*;

// Converts mathematical expressions into lists of commands.
public class ArithmeticConverter {
    public static List<Token> infixToPostfix2(List<Token> infix) {
        if(infix.size() == 2 && infix.get(0).getText().equals("-")) {
            Token numberToken = infix.get(1);
            Token compactToken = Token.withType(infix.get(1).type);

            return new ArrayList<>(List.of(
                numberToken.hasFloat
                ? compactToken.withFloat(-numberToken.getFloat())
                : compactToken.withInt(-numberToken.getInteger())
            ));
        }

        final List<String> operators = List.of(
            "-", "+", "/", "*", "^"
        );

        List<Token> postfixTokens = new ArrayList<>();
        Stack<Integer> symbolStack = new Stack<>();

        for(Token token : infix) {
            if(operators.contains(token.getText())) {
                int operatorIndex = operators.indexOf(token.getText());

                if(!symbolStack.isEmpty()) {
                    while(!symbolStack.isEmpty()) {
                        int prec2 = symbolStack.peek() / 2;
                        int prec1 = operatorIndex / 2;
                        if(prec2 > prec1 || prec2 == prec1 /* and not right-associative */) {
                            String operator = operators.get(symbolStack.pop());

                            Token operatorToken = Token.withType(Token.TokenType.Operator).withText(operator);
                            postfixTokens.add(operatorToken);
                        } else {
                            break;
                        }
                    }
                }

                symbolStack.push(operatorIndex);
            } else if(token.is(Token.TokenType.OpenBracket)) {
                symbolStack.push(-2);
            } else if(token.is(Token.TokenType.CloseBracket)) {
                while(symbolStack.peek() != -2) {
                    String operator = operators.get(symbolStack.pop());

                    Token operatorToken = Token.withType(Token.TokenType.Operator).withText(operator);
                    postfixTokens.add(operatorToken);
                }
                symbolStack.pop();
            } else {
                postfixTokens.add(token);
            }
        }
        while(!symbolStack.isEmpty()) {
            String operator = operators.get(symbolStack.pop());

            Token operatorToken = Token.withType(Token.TokenType.Operator).withText(operator);
            postfixTokens.add(operatorToken);
        }

        return postfixTokens;
    }

    private static double getTokenValue(Token token) {
        if(token.hasFloat) return token.getFloat();
        if(token.hasInt) return token.getInteger();

        return 0;
    }

    private static double performOperation(Token operatorToken, double a, double b) {
        double result;
        switch(operatorToken.getText()) {
            case "+":
                result = a + b;
                break;

            case "-":
                result = a - b;
                break;

            case "*":
                result = a * b;
                break;

            case "/":
                result = a / b;
                break;

            default:
                result = 0;
        }

        return result;
    }

    private static String expressionToString(List<Token> expressionTokens) {
        StringBuilder expressionBuilder = new StringBuilder();

        for(Token t : expressionTokens) {
            expressionBuilder.append(t.hasText ? t.getText() : getTokenValue(t)).append(' ');
        }

        return expressionBuilder.toString();
    }

    // Solve the postfix expression represented by the token list.
    // This is useful for evaluating expressions at compile time (for optimisation).
    public static Optional<Double> solve(List<Token> postfixExpression) {
        Stack<Double> numberStack = new Stack<>();

        try {
            for(Token token : postfixExpression) {
                if(token.is(Token.TokenType.Operator)) {
                    // Apply the operator to the last two numbers.
                    double b = numberStack.pop();
                    double a = numberStack.pop();

                    numberStack.push(performOperation(token, a, b));
                } else {
                    if(token.is(Token.TokenType.IdentifierOrKeyword)) {
                        return Optional.empty();
                    }

                    numberStack.push(getTokenValue(token));
                }
            }
        } catch(EmptyStackException e) {
            Util.emitFatalError("Invalid postfix expression: " + expressionToString(postfixExpression));
        }

        return Optional.of(numberStack.pop());
    }
}
