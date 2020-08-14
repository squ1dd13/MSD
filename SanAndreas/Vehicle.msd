class Vehicle {
    [0x1C2]
    void release();

    [0x1C1]
    bool isStopped();

    [0x2E3]
    void getSpeed(GlobalIntFloat speed);

    [0xA06]
    bool isNextTrainStationUnlocked();

    [0xA07]
    void moveToNextUnlockedStation();

    [0xAE]
    void setCarDrivingStyle(Int p1);

    [0x119]
    bool isCarDead();

    [0x129]
    void createCharInsideCar(Int p1, Int p2, LocalIntFloat p3);

    [0x175]
    void setCarHeading(GlobalIntFloat p1);

    [0x1C3]
    bool release();

    [0x1C8]
    bool createCharAsPassenger(Int p1, Int p2, Int p3, LocalIntFloat p4);

    [0x224]
    bool setCarHealth(Int p1);

    [0x229]
    bool changeCarColour(Int p1, Int p2);

    [0x2CA]
    bool isCarOnScreen();

    [0x3CE]
    bool isCarStuck();

    [0x72F]
    void addStuckCarCheckWithWarp(Flt p1, Int p2, Int p3, Int p4, Int p5, Int p6);
}