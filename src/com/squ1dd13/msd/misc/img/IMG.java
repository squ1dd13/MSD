package com.squ1dd13.msd.misc.img;

import com.squ1dd13.msd.shared.*;

import java.io.*;
import java.nio.*;
import java.util.*;
import java.util.function.*;

public class IMG {
    private final Map<String, DirectoryEntry> entries = new HashMap<>();

    // We don't want to have the file open all the time,
    //  so we need to keep the path so we can open it on demand.
    private final String archivePath;
    private RandomAccessFile openFile;

    public IMG(String path) throws IOException {
        archivePath = path;

        open();

        if(!Util.readString(openFile, 4).equals("VER2")) {
            Util.emitFatalError("Cannot open archive: invalid identifier");
        }

        long entryCount = Util.readUnsignedInt(openFile);
        for(long i = 0; i < entryCount; ++i) {
            var entry = new DirectoryEntry(openFile);
            entries.put(entry.fileName, entry);
        }

        close();
    }

    public void open() {
        try {
            openFile = new RandomAccessFile(archivePath, "r");
        } catch(FileNotFoundException e) {
            Util.emitFatalError("Cannot open archive file (does not exist)");
        }
    }

    public void close() {
        try {
            openFile.close();
        } catch(IOException e) {
            Util.emitFatalError("Unable to close archive file");
        }

        openFile = null;
    }

    private void assertOpen() {
        if(openFile == null) Util.emitFatalError("Archive file not open");
    }

    public void withOpen(Consumer<IMG> consumer) {
        open();
        consumer.accept(this);
        close();
    }

    public Optional<ByteBuffer> get(String name) {
        assertOpen();

        try {
            return Optional.of(entries.get(name).readData(openFile));
        } catch(IOException e) {
            return Optional.empty();
        }
    }
}
