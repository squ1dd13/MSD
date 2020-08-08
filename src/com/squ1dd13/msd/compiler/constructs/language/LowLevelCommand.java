package com.squ1dd13.msd.compiler.constructs.language;

import com.squ1dd13.msd.compiler.constructs.*;
import com.squ1dd13.msd.shared.*;

import java.util.*;

// Wrapper for the shared Command class that implements more compiler-specific features.
public class LowLevelCommand implements Compilable {
    public Command command = null;
    public List<Argument> arguments = new ArrayList<>();

//    public static LowLevelCommand parse(String commandString) {
//         set_var_int(&globalInt_38092, 0)
//
//        StringReader reader = new StringReader(commandString);
//    }

    public List<Integer> getOpcodeBytes() {
        // We need the opcode bytes in little-endian, and then we only want the sublist 0->2,
        //  because the opcode is only 2 bytes long.
        return Util.intArrayToList(Util.intToBytesLE(command.opcode)).subList(0, 2);
    }

    @Override
    public Collection<Integer> compile(Context context) {
        List<Integer> compiled = new ArrayList<>(getOpcodeBytes());

        CommandInfoDesk.CommandInfo info = CommandInfoDesk.getInfo(command.opcode);

//        System.out.print(command.name + " [");
        for(int i = 0; i < arguments.size(); i++) {
            Argument arg = arguments.get(i);

            if(arg == null) {
                Util.emitWarning("Argument null");
                continue;
            }

            var paramInfo = command.getParamInfo(i);

            if(paramInfo != null) {
                var llt = command.getParamInfo(i).lowLevelType;
                if(llt != LowLevelType.Unknown) {
                    arg.type = llt;
                }
            }

            if(arg.type == LowLevelType.Unknown) {
                if(info == null || info.lowLevelParamTypes == null) {
                    arg.type = LowLevelType.Unknown;
//                    System.out.println("no infofff");
                } else {
                    arg.type = info.lowLevelParamTypes[i];
//                    System.out.println(arg.type);
                }
            }

            if(arg.type == LowLevelType.Unknown) {
                arg.type = command.getParamInfo(i).type.guessLowLevelType();
            }

            compiled.addAll(arg.compile(context));
//            System.out.print(arg.type + ", ");
        }

//        System.out.print("]\n");

        return compiled;
    }
}
