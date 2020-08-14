static class Stats {
    [0x623]
    void increaseStat(Int statNo, Int amount);

    [0x624]
    void increaseFloatStat(Int statNo, Flt amount);

    [0x625]
    void decreaseStat(Int statNo, Int amount);

    [0x626]
    void decreaseFloatStat(Int statNo, Flt amount);
}