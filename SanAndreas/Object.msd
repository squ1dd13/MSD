class Object {
    [0x29B]
    static void create(Int modelNo, Flt x, Flt y, Flt z, GlobalIntFloat objectOut);

    [0x176]
    void getHeading(GlobalIntFloat angle);

    [0x3CA]
    bool exists();

    [0x400]
    void getCoordinatesWithOffset(Flt offX, Flt offY, Flt offZ, GlobalIntFloat objX, GlobalIntFloat objY, GlobalIntFloat objZ);

    [0x977]
    bool isInScriptTriggerRadius();

    [0x9CC]
    bool hasModel(Int modelNo);

    [0x1C4]
    void release();
}