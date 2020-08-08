package com.squ1dd13.msd.compiler.text;

import com.squ1dd13.msd.compiler.constructs.language.*;
import com.squ1dd13.msd.shared.*;

import java.util.*;
import java.util.regex.*;

public class ClassParser {
    public static ClassConstruct parseClass(List<String> lines) {
        final Pattern stubRegex = Pattern.compile(
            "stub\\s+([^(\\s]+)\\s*\\(((?:[^\\s]+\\s+[^\\s,)]+,\\s*)*(?:[^\\s]+\\s+[^\\s,)]+)?)\\)\\s*=\\s*\\((\\d+),?\\s*((?:(?:\\d+,\\s*)*\\d+))\\);"
        );

        final Pattern classRegex = Pattern.compile(
            "class\\s+([A-Za-z_][A-Za-z0-9_]*)\\s*\\{"
        );

        Matcher classMatcher = classRegex.matcher(lines.get(0));
        if(!classMatcher.matches()) {
            Util.emitFatalError("Class declarations must start with: 'class Name {'");
        }

        ClassConstruct theClass = new ClassConstruct();
        theClass.name = classMatcher.group(1);

        for(int i = 1; i < lines.size() - 1; ++i) {
            String line = lines.get(i).strip();
            if(line.startsWith("//")) continue;

            if(line.contains("//")) {
                line = line.substring(0, line.indexOf("//"));
                if(line.isBlank()) continue;
            }

            if(!line.startsWith("stub")) {
                Util.emitWarning("Classes may only contain stubs.");
                continue;
            }

            Matcher stubMatcher = stubRegex.matcher(line);
            if(!stubMatcher.matches()) {
                Util.emitWarning("Invalid stub declaration: '" + line + "' (Line" + (i + 1) + ")");
                continue;
            }

            String stubName = stubMatcher.group(1);
            String stubArgs = stubMatcher.group(2);
            String stubOpcode = stubMatcher.group(3);
            String[] stubArgTypes = stubMatcher.group(4).split(",");

            ClassConstruct.StubMethod stub = new ClassConstruct.StubMethod();
            stub.name = stubName;
            stub.opcode = Integer.parseInt(stubOpcode);

            stub.paramTypes = new LowLevelType[stubArgTypes.length];
            stub.highLevelTypes = new HighLevelType[stubArgTypes.length];

            for(int j = 0; j < stubArgTypes.length; ++j) {
                var s = stubArgTypes[j].strip();

                LowLevelType type = LowLevelType.decode(Integer.parseInt(s));
                stub.paramTypes[j] = type;
                stub.highLevelTypes[j] = type.highLevelType();
            }

            theClass.stubs.add(stub);
        }

        if(!lines.get(lines.size() - 1).strip().equals("}")) {
            Util.emitWarning("Missing closing brace ('}') in class definition.");
        }

        return theClass;
    }
}
