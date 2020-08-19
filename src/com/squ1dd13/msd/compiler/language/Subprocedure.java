package com.squ1dd13.msd.compiler.language;

import com.squ1dd13.msd.compiler.*;
import com.squ1dd13.msd.compiler.text.lexer.*;
import com.squ1dd13.msd.compiler.text.parser.*;
import com.squ1dd13.msd.shared.*;

import java.util.*;

import static com.squ1dd13.msd.compiler.text.lexer.Token.*;

public class Subprocedure implements Compilable {
    public String name;
    public List<Compilable> body;

    public Subprocedure(Iterator<Token> tokenIterator) {
        Token nameToken = tokenIterator.next();
        name = nameToken.getText();

        tokenIterator.next();
        tokenIterator.next();

        Token openBrace = tokenIterator.next();
        if(openBrace.isNot(TokenType.OpenBrace)) {
            Util.emitFatalError("Invalid function syntax.");
        }

        var bodyTokens = ParserUtils.readCurrentLevel(tokenIterator, TokenType.OpenBrace, TokenType.CloseBrace);
        body = new Parser(bodyTokens).parseTokens();
    }


    @Override
    public Collection<BasicCommand> toCommands() {
        List<BasicCommand> commands = new ArrayList<>();
        for(Compilable bodyElement : body) {
            commands.addAll(bodyElement.toCommands());
        }

        return commands;
    }
}
