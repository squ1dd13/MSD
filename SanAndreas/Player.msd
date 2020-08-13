class Player {
    [0x10B]
    void getMoney(GlobalIntFloat money);

    [0x1B4]
    bool setControlling(Bool controlling);

    [0x256]
    bool notWastedOrBusted();

    [0x3EE]
    bool canStartMission();

    [0x457]
    bool isAimingAt(GlobalIntFloat character);

    [0x945]
    void getMaxArmor(GlobalIntFloat armorLevel);
}