package com.squ1dd13.msd.compiler.constructs;

import com.squ1dd13.msd.shared.*;

import java.util.*;

// Wrapper for the shared Command class that implements more compiler-specific features.
public class LowLevelCommand implements Compilable {
    private Command internalCommand = null;



    @Override
    public Collection<Integer> compile(Context context) {
        List<Integer> compiled = new ArrayList<>();
        compiled.add(internalCommand.opcode);

//        for()

        return compiled;
    }
}
