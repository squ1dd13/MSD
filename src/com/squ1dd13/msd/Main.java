package com.squ1dd13.msd;

import com.squ1dd13.msd.high.*;
import com.squ1dd13.msd.low.*;
import com.squ1dd13.msd.uni.*;

import java.io.*;

public class Main {
    public static void main(String[] args) throws IOException {
        Command.loadFile("/Users/squ1dd13/Documents/msd/commands.ini");

        LowScript script = LowScript.load("/Users/squ1dd13/Downloads/debt.txt");

        HighLevelScript highLevelScript = new HighLevelScript(script);
        highLevelScript.print();
//        script.print();
    }
}
