package com.squ1dd13.msd.compiler.text.parser;

import com.squ1dd13.msd.compiler.text.lexer.*;
import com.squ1dd13.msd.shared.*;

import java.util.*;

import static com.squ1dd13.msd.compiler.text.lexer.Token.*;

// Converts mathematical expressions into lists of commands.
public class ArithmeticConverter {
    public static List<Token> infixToPostfix2(List<Token> infix) {
        final List<String> operators = List.of(
            "-", "+", "/", "*", "^", "u+", "u-"
        );

        final Set<String> mayBeUnary = Set.of("+", "-");

        // Find and resolve unary operations.
        for(int i = 0; i < infix.size(); ++i) {
            Token token = infix.get(i);
            if(token.isNot(TokenType.Operator)) continue;

            boolean isUnary = false;

            if(i == 0 && token.is(TokenType.Operator)) {
                isUnary = true;
            } else if(i != 0) {
                var prev = infix.get(i - 1);
                isUnary = prev.is(TokenType.Operator) || prev.is(TokenType.OpenBracket);
            }

            if(isUnary) {
                if(!mayBeUnary.contains(token.getText())) {
                    Util.emitFatalError("Operator '" + token.getText() + "' is not a unary operator");
                }

                infix.set(i, token.withText("u" + token.getText()));
            }
        }

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

                            Token operatorToken = withType(TokenType.Operator).withText(operator);
                            postfixTokens.add(operatorToken);
                        } else {
                            break;
                        }
                    }
                }

                symbolStack.push(operatorIndex);
            } else if(token.is(TokenType.OpenBracket)) {
                symbolStack.push(-2);
            } else if(token.is(TokenType.CloseBracket)) {
                while(symbolStack.peek() != -2) {
                    String operator = operators.get(symbolStack.pop());

                    Token operatorToken = withType(TokenType.Operator).withText(operator);
                    postfixTokens.add(operatorToken);
                }
                symbolStack.pop();
            } else {
                postfixTokens.add(token);
            }
        }
        while(!symbolStack.isEmpty()) {
            String operator = operators.get(symbolStack.pop());

            Token operatorToken = withType(TokenType.Operator).withText(operator);
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

            case "u+":
                result = a;
                break;

            case "u-":
                result = -a;
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
                if(token.is(TokenType.Operator)) {
                    if(token.getText().startsWith("u")) {
                        // Unary, so pass 0 as b.
                        numberStack.push(performOperation(token, numberStack.pop(), 0));

                        continue;
                    }

                    // Apply the operator to the last two numbers.
                    double b = numberStack.pop();
                    double a = numberStack.pop();

                    numberStack.push(performOperation(token, a, b));
                } else {
                    if(token.is(TokenType.IdentifierOrKeyword)) {
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

    // TODO: Convert any mathematical operations that can't be statically solved into commands.
}
