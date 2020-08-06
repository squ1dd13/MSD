package com.squ1dd13.msd.high.blocks;

import com.squ1dd13.msd.uni.*;

import java.util.*;

public class SubroutineBlock implements CodeBlock {
    public int offset;
    public List<CodeBlock> body = new ArrayList<>();
    public int consumed;

    // Construct a subroutine block at the current index.
    public SubroutineBlock(List<Command> commands, int index) {
        int start = index;
        offset = commands.get(index).offset;

        while(index < commands.size()) {
            BlockFactory.FactoryOutput output = BlockFactory.createBlock(commands, index);
            body.add(output.block);

            int i = index;
            index += output.consumed;

            // Check if this is a return.
            if(commands.get(i).opcode == 0x51) {
                break;
            }
        }

        consumed = index - start;
    }

    @Override
    public List<String> toLineStrings() {
        List<String> lines = new ArrayList<>(List.of("void proc_" + offset + "() {"));

        for(CodeBlock block : body) {
            var blockLines = block.toLineStrings();

            for(String line : blockLines) {
                lines.add("$i" + line);
            }
        }

        lines.add("}");
        lines.add("");

        return lines;
    }
}
