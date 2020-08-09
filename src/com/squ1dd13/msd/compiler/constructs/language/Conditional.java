package com.squ1dd13.msd.compiler.constructs.language;

import com.squ1dd13.msd.compiler.constructs.*;
import com.squ1dd13.msd.shared.*;

import java.util.*;
import java.util.stream.*;

public class Conditional implements Compilable {
    public boolean isAnd = false;
    public List<LowLevelCommand> conditions;
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

    private List<LowLevelCommand> getMainBodyCommands(Context ctx) {
        return mainBodyElements.stream().map(
            compilable -> compilable.toCommands(ctx)
        ).flatMap(Collection::stream).collect(Collectors.toList());
    }

    private List<LowLevelCommand> getElseBodyCommands(Context ctx) {
        return elseBodyElements.stream().map(
            compilable -> compilable.toCommands(ctx)
        ).flatMap(Collection::stream).collect(Collectors.toList());
    }

    @Override
    public Collection<Integer> compile(Context context) {
        return new ArrayList<>();
    }

    @Override
    public Collection<LowLevelCommand> toCommands(Context ctx) {
        // Create an 'if' command.
        int argumentValue = conditionEncoding();

        LowLevelCommand command = new LowLevelCommand();
        command.command = Command.commands.get(Opcode.If.get()).copy();
        command.arguments = List.of(new Argument(LowLevelType.S8, argumentValue));

        List<LowLevelCommand> commands = new ArrayList<>();
        commands.add(command);
        commands.addAll(conditions);

        var mainBody = getMainBodyCommands(ctx);
        var elseBody = getElseBodyCommands(ctx);

        // Add a relative jump. This is not a real thing in SCM,
        //  it just means we can add the jump without worrying
        //  about its absolute offset from the start. The real
        //  offset will be added later.

        // We need to know the size of the main body before adding the jump.
        int bodySize = mainBody.stream().mapToInt(c -> c.compile(ctx).size()).sum();

        LowLevelCommand bodyJump = LowLevelCommand.create(
            Opcode.RelativeConditionalJump.get(),
            "relative_jump_conditional",
            new Argument(LowLevelType.S32, bodySize)
        );

        commands.add(bodyJump);
        commands.addAll(mainBody);

        if(!elseBody.isEmpty()) {
            int elseSize = elseBody.stream().mapToInt(c -> c.compile(ctx).size()).sum();

            LowLevelCommand elseJump = LowLevelCommand.create(
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
