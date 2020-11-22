package com.squ1dd13.msd.old.misc.img;

import com.squ1dd13.msd.old.shared.*;

import java.io.*;
import java.nio.*;
import java.util.*;
import java.util.function.*;

public class IMG {
    private final Map<String, DirectoryEntry> entries = new HashMap<>();

    // Stores changes that can't be written until a repack.
    private final Map<String, ByteBuffer> unwrittenChanges = new HashMap<>();

    // We don't want to have the file open all the time,
    //  so we need to keep the path so we can open it on demand.
    final String archivePath;
    private RandomAccessFile openFile;

    // If this is true, the next time close() is called, the entire archive
    //  will be repacked. This is one reason why it's a good idea not to
    //  open the archive for writing too often.
    private boolean needsRepack = false;

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

    public void open(String mode) {
        try {
            openFile = new RandomAccessFile(archivePath, "rw");
        } catch(FileNotFoundException e) {
            Util.emitFatalError("Cannot open archive file (does not exist)");
        }
    }

    public void open() {
        open("rw");
    }

    public void close() {
        if(needsRepack) {
            try {
                repack();
            } catch(IOException e) {
                Util.emitFatalError("Unable to repack archive file");
            }
        }

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

    public void withOpen(Consumer<IMG> consumer, String mode) {
        open(mode);
        consumer.accept(this);
        close();
    }

    public void withOpen(Consumer<IMG> consumer) {
        withOpen(consumer, "rw");
    }

    public Optional<ByteBuffer> get(String name) {
        assertOpen();

        try {
            return Optional.of(entries.get(name).readData(openFile));
        } catch(IOException e) {
            return Optional.empty();
        }
    }

    public void write(String name, ByteBuffer contents) {
        DirectoryEntry entry = new DirectoryEntry();
        entry.sectorCount = Util.roundUpToMultiple(contents.array().length, 2048) / 2048;
        entry.fileName = name;

        if(entries.containsKey(name)) {
            DirectoryEntry oldEntry = entries.get(name);

            // If the new sector count is larger than the old one, the archive should be repacked.
            // Technically a full repack is not needed, but it keeps the code simpler (!!).
            if(entry.sectorCount > oldEntry.sectorCount) {
                needsRepack = true;

                // We can't write the changes now, so they'll be written on the next repack.
                unwrittenChanges.put(name, contents);
                oldEntry.sectorCount = entry.sectorCount;
            } else {
                updateEntry(oldEntry, entry.sectorCount, contents);
            }
        } else {
            // New entry.
            needsRepack = true;

            entries.put(name, entry);
            unwrittenChanges.put(name, contents);
        }
    }

    private void updateEntry(DirectoryEntry entry, long newSectorCount, ByteBuffer data) {
        long oldSize = entry.sectorCount * 2048;

        if(entry.sectorCount != newSectorCount) {
            entry.sectorCount = newSectorCount;

            // Write the *entry* with the new size.
            try {
                entry.writeEntry(openFile);
            } catch(IOException e) {
                Util.emitFatalError("Unable to write updated directory entry to archive");
            }
        }

        // Write the new data.
        try {
            long pos = openFile.getFilePointer();
            openFile.seek(entry.sectorOffset * 2048);

            // Write oldSize bytes so we null out old data.
            for(long i = 0; i < oldSize; ++i) {
                if(i >= data.limit()) {
                    openFile.writeByte(0);
                } else {
                    openFile.writeByte(data.get((int)i));
                }
            }

            openFile.seek(pos);
        } catch(IOException e) {
            e.printStackTrace();
            Util.emitFatalError("Error while writing data");
        }
    }

    // Get the newest data for the given name.
    private ByteBuffer getEntryData(String name) {
        if(unwrittenChanges.containsKey(name)) {
            return unwrittenChanges.get(name);
        }

        return get(name).orElse(null);
    }

    private void repack() throws IOException {
        assertOpen();

        System.out.println("repacking archive");

        // Repacking is the process of rebuilding the entire archive from scratch.
        // It can sometimes create a smaller archive, but is also a good way to add
        //  data that wouldn't (easily) fit in otherwise.

        // Sort the entries alphabetically (why not?)
        List<String> sortedEntries = new ArrayList<>(entries.keySet());
        sortedEntries.sort(String::compareToIgnoreCase);

        String tempArchivePath = archivePath + ".tmp";

        RandomAccessFile newArchive = new RandomAccessFile(tempArchivePath, "rw");

        // Write the header.
        Util.writeString(newArchive, "VER2", 4);
        Util.writeUnsignedInt(newArchive, sortedEntries.size());

        // Work out the archive directory size so we know where raw data can start.
        long currentDataOffset = Util.roundUpToMultiple(sortedEntries.size() * 32, 2048);

        for(String key : sortedEntries) {
            DirectoryEntry entry = entries.get(key);
            entry.entryOffset = newArchive.getFilePointer();
            entry.sectorOffset = (currentDataOffset += entry.sectorCount * 2048) / 2048;

            entry.writeEntry(newArchive);

            ByteBuffer fileData = getEntryData(entry.fileName);
            if(fileData == null) {
                Util.emitFatalError("Unable to get file data for '" + entry.fileName + "'");
                break;
            }

            long pos = newArchive.getFilePointer();
            newArchive.seek(entry.sectorOffset * 2048);

            for(int i = 0; i < entry.sectorCount * 2048; ++i) {
                if(i >= fileData.limit()) {
                    newArchive.writeByte(0);
                } else {
                    newArchive.writeByte(fileData.get(i));
                }
            }

            newArchive.seek(pos);
        }

        System.out.println("repacked archive");
    }

    public Set<String> getEntryList() {
        return entries.keySet();
    }
}
