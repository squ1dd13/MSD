static class Animation {
    [0x4ED]
    void loadFile(Str name);

    [0x4EE]
    bool isFileLoaded(Str name);

    [0x4EF]
    void releaseFile(Str name);
}