package com.squ1dd13.msd.decomp.controlflow;

import com.squ1dd13.msd.decomp.*;
import com.squ1dd13.msd.decomp.generic.*;

import java.util.*;

// A script with control flow information.
public class FlowScript extends CodeContainer<CodeElement> {
    public FlowScript(List<CodeElement> codeElements) {
        super(codeElements);
    }

    public void print() {
        for(CodeElement element : bodyCode) {
            var lines = element.toCodeString(0).split("\n");

            for(String line : lines) {
                System.out.println(Highlight.highlightLine(line));
            }
        }
    }

    @Override
    public String toCodeString(int indent) {
        return "script";
    }
}
