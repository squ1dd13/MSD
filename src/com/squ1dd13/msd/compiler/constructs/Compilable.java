package com.squ1dd13.msd.compiler.constructs;

import com.squ1dd13.msd.compiler.constructs.language.*;

import java.util.*;

public interface Compilable {
    Collection<Integer> compile(Context context);
    Collection<LowLevelCommand> toCommands(Context ctx);
}
