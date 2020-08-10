package com.squ1dd13.msd.compiler.constructs;

import java.util.*;

public interface Compilable {
    Collection<BasicCommand> toCommands();
}
