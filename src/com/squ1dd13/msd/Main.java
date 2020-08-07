package com.squ1dd13.msd;

import com.squ1dd13.msd.high.*;
import com.squ1dd13.msd.low.*;
import com.squ1dd13.msd.uni.*;

import java.io.*;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("MSD v1.0 Beta");

        Command.loadFile("/Users/squ1dd13/Documents/MSD-Project/Java/MSD/commands.ini");

        LowScript script = LowScript.load("/Users/squ1dd13/Documents/trains.txt");

        HighLevelScript highLevelScript = new HighLevelScript(script);
        highLevelScript.print();
//        script.print();
    }
}
