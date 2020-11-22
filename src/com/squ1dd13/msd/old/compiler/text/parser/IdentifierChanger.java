package com.squ1dd13.msd.old.compiler.text.parser;

import com.squ1dd13.msd.old.compiler.text.lexer.*;

// Anything that can change the meaning of an identifier (or keyword at this stage).
// This could be a declaration or a 'using' or 'global' statement.
public interface IdentifierChanger {
    TypedToken modifyIdentifier(Token identifier);
}
