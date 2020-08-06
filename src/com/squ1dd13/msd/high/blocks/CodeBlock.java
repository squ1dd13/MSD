package com.squ1dd13.msd.high.blocks;

import com.squ1dd13.msd.low.*;

import java.util.*;

// A single command or a group of commands.
public interface CodeBlock {
    // A list of lines of code that represent this block.
    // The list is required so each line can be easily indented correctly.
    // A line should contain the token "$i" where extra indentation is needed.
    List<String> toLineStrings();
}
