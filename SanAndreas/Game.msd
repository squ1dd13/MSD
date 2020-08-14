static class Game {
    [0x77E]
    void getCurrentInterior(GlobalIntFloat interiorID);

    [0x3D3]
    void getClosestVehicleNodePositionAndAngle(Flt x, Flt y, Flt z, GlobalIntFloat nodeX, GlobalIntFloat nodeY, GlobalIntFloat nodeZ, GlobalIntFloat nodeAngle);

    [0x3E6]
    void clearPrint();

    [0x512]
    void printIndefinitely(Str gxtKey);

    [0x4E]
    void endCurrentScript();

    [0x3A4]
    void setThreadName(Str name);

    [0x111]
    void setCanGameOver(Bool canIt);
}