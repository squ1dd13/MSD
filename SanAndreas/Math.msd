static class Math {
    [0x208]
    bool randf(Flt lower, Flt upper, GlobalIntFloat randomOut);

    [0x209]
    bool rand(Int lower, Int upper, GlobalIntFloat randomOut);

    [0x509]
    void distance2D(GlobalIntFloat ax, GlobalIntFloat ay, GlobalIntFloat bx, GlobalIntFloat by, GlobalIntFloat distance)
}