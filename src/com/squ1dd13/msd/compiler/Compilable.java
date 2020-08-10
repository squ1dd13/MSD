package com.squ1dd13.msd.compiler;

import java.util.*;

public interface Compilable {
    Collection<BasicCommand> toCommands();
}
