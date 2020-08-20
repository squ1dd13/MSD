package com.squ1dd13.msd.unified;

import com.squ1dd13.msd.shared.*;
import com.squ1dd13.msd.unified.elements.*;

import java.io.*;
import java.util.*;

// main.scm
public class MainScript {
    Call nhcJump;
    char game;
    // empty space until next jump
    Call nhcJump2;
    int chunkIndex;
    long usedObjects;
    List<String> objectNames = new ArrayList<>();
    Call nhcJump3;
    long mainScriptSize;
    long largestMissionScriptSize;
    int missionScriptCount;
    int exclusiveMissionScriptCount;
    long maxMissionLocals;
    List<Long> missionOffsets = new ArrayList<>();
    Call nhcJump4;
    long largestStreamedScript;
    long streamedScriptCount;
    List<StreamedScriptInfo> streamedScripts = new ArrayList<>();
    Call nhcJump5;
    Call mainScriptJump;
    long globalVariableSpaceSize;

    Script mainScript;

    public MainScript(String filename, String extractDir) throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile(filename, "r");

        nhcJump = new Call(randomAccessFile);
        game = (char)randomAccessFile.read();
        randomAccessFile.seek(Math.abs(nhcJump.getArgValue(0).getInt()));
        nhcJump2 = new Call(randomAccessFile);
        chunkIndex = randomAccessFile.read();
        usedObjects = Util.readUnsignedInt(randomAccessFile);

        for(long i = 0; i < usedObjects; ++i) {
            String objectName = Util.readString(randomAccessFile, 24);
            System.out.println(objectName);

            objectNames.add(objectName);
        }

        nhcJump3 = new Call(randomAccessFile);
//        randomAccessFile.skipBytes(1);
        int ci = randomAccessFile.read();

        mainScriptSize = Util.readUnsignedInt(randomAccessFile);
        largestMissionScriptSize = Util.readUnsignedInt(randomAccessFile);
        missionScriptCount = randomAccessFile.readUnsignedShort();
        exclusiveMissionScriptCount = randomAccessFile.readUnsignedShort();
        maxMissionLocals = Util.readUnsignedInt(randomAccessFile);

        for(long i = 0; i < missionScriptCount; ++i) {
            missionOffsets.add(Util.readUnsignedInt(randomAccessFile));
        }

        randomAccessFile.seek(Math.abs(nhcJump3.getArgValue(0).getInt()));
        nhcJump4 = new Call(randomAccessFile);
        randomAccessFile.skipBytes(1);

        largestStreamedScript = Util.readUnsignedInt(randomAccessFile);
        streamedScriptCount = Util.readUnsignedInt(randomAccessFile);

        for(long i = 0; i < streamedScriptCount; ++i) {
            streamedScripts.add(new StreamedScriptInfo(
                Util.readString(randomAccessFile, 20),
                Util.readUnsignedInt(randomAccessFile),
                Util.readUnsignedInt(randomAccessFile)
            ));
        }

        nhcJump5 = new Call(randomAccessFile);
        randomAccessFile.skipBytes(5);

        mainScriptJump = new Call(randomAccessFile);
        randomAccessFile.skipBytes(1);
        globalVariableSpaceSize = Util.readUnsignedInt(randomAccessFile);
        randomAccessFile.skipBytes(4);

        RandomAccessFile mainScriptFile = new RandomAccessFile(filename + ".script.scm", "rw");
        for(long i = 0; i < mainScriptSize; ++i) {
            mainScriptFile.write(randomAccessFile.read());
        }
        mainScriptFile.close();

//        mainScript = new Script(filename + ".script.scm");

        for(int i = 0; i < missionOffsets.size(); i++) {
            long missionOffset = missionOffsets.get(i);
            long length = i != missionOffsets.size() - 1 ? missionOffsets.get(i + 1) - missionOffset : -1;
            randomAccessFile.seek(missionOffset);

            System.out.println("\n***** Mission " + missionOffset + " *****");
            Script script = new Script(randomAccessFile, length);
//            script.print();
            BufferedWriter writer = new BufferedWriter(new FileWriter(extractDir + "/mission_" + missionOffset + ".msd"));
            script.write(writer);
            writer.close();

//            mainScript.getCallAtOffset((int)missionOffset).ifPresent(call -> {
//                call.comment = "***** MISSION *****";
//            });
        }

//        mainScript.print();

        randomAccessFile.close();
    }

    private static class StreamedScriptInfo {
        String scriptName;
        long offset;
        long size;

        public StreamedScriptInfo(String name, long off, long sz) {
            scriptName = name;
            offset = off;
            size = sz;
        }
    }
}
