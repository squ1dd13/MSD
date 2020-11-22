package com.squ1dd13.msd.old.compiler;

import java.util.*;

public interface Compilable {
    Collection<BasicCommand> toCommands();
}
