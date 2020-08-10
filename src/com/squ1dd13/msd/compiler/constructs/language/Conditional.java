package com.squ1dd13.msd.compiler.constructs.language;

import com.squ1dd13.msd.compiler.constructs.*;
import com.squ1dd13.msd.shared.*;

import java.util.*;
import java.util.stream.*;

public class Conditional implements Compilable {
    public boolean isAnd;
    public List<BasicCommand> conditions;
    public List<Compilable> mainBodyElements;
    public List<Compilable> elseBodyElements;

    private int conditionEncoding() {
        // The 'if' command's single argument is a number that encodes the number
        //  of conditions and also how they should be combined (AND/OR).

        int numConditions = conditions.size();
        if(numConditions == 1) {
            return 0;
        }

        if(isAnd) {
            return numConditions - 1;
        }

        return numConditions - 19;
    }

    private List<BasicCommand> getMainBodyCommands() {
        return mainBodyElements.stream().map(
            Compilable::toCommands
        ).flatMap(Collection::stream).collect(Collectors.toList());
    }

    private List<BasicCommand> getElseBodyCommands() {
        if(elseBodyElements == null) return new ArrayList<>();

        return elseBodyElements.stream().map(
            Compilable::toCommands
        ).flatMap(Collection::stream).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "Conditional: " + toCommands();
    }

    @Override
    public Collection<BasicCommand> toCommands() {
        // Create an 'if' command.
        int argumentValue = conditionEncoding();

        BasicCommand command = new BasicCommand();
        command.highLevel = Command.commands.get(Opcode.If.get()).copy();
        command.arguments = List.of(new Argument(LowLevelType.S8, argumentValue));

        List<BasicCommand> commands = new ArrayList<>();
        commands.add(command);
        commands.addAll(conditions);

        var mainBody = getMainBodyCommands();
        var elseBody = getElseBodyCommands();

        // Add a relative jump. This is not a real thing in SCM,
        //  it just means we can add the jump without worrying
        //  about its absolute offset from the start. The real
        //  offset will be added later.

        // We need to know the size of the main body before adding the jump.
        int bodySize = mainBody.stream().mapToInt(c -> c.compile().size()).sum();

        BasicCommand bodyJump = BasicCommand.create(
            Opcode.RelativeConditionalJump.get(),
            "relative_jump_conditional",
            new Argument(LowLevelType.S32, bodySize)
        );

        commands.add(bodyJump);
        commands.addAll(mainBody);

        if(!elseBody.isEmpty()) {
            int elseSize = elseBody.stream().mapToInt(c -> c.compile().size()).sum();

            BasicCommand elseJump = BasicCommand.create(
                Opcode.RelativeUnconditionalJump.get(),
                "relative_jump",
                new Argument(LowLevelType.S32, elseSize)
            );

            commands.add(elseJump);
            commands.addAll(elseBody);
        }

        return commands;
    }
}
