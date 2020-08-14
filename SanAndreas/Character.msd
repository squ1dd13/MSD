class Character {
    [0x1C2]
    void release();

    [0x8C7]
    void setIndividualPosition(GlobalIntFloat x, GlobalIntFloat y, GlobalIntFloat z);

    [0x70A]
    void attachObjectAnimated(GlobalIntFloat object, Flt x, Flt y, Flt z, Int boneID, Int unknown, Str animName, Str animFile, Int animTime);

    [0x60B]
    void setBrain(Int handle);

    [0x70B]
    void dropObject(Bool unknown);

    [0x611]
    bool isPlayingAnimation(Str animName);
    
    [0x812]
    void playFullAnimation(Str animName, Str animFile, Flt frameDelta, Bool loop, Bool lockX, Bool lockY, Bool lockF, Int time);
    
    [0x792]
    void forceClearTasks();

    [0x613]
    void getAnimationProgress(Str animName, GlobalIntFloat progress);

    [0x118]
    bool isDead();
    
    [0x81A]
    void setWeaponSkill(Int skill);

    [0x9B]
    void delete();

    [0x31D]
    bool damagedByWeapon(Int weapon);

    [0x4DD]
    void getArmorLevel(GlobalIntFloat armorLevel);

    [0x2E0]
    bool isShooting();

    [0xA0]
    void getCoordinates(GlobalIntFloat x, GlobalIntFloat y, GlobalIntFloat z);

    [0xA1]
    void setCoordinates(GlobalIntFloat x, GlobalIntFloat y, GlobalIntFloat z);

    [0x2E2]
    void setShotAccuracy(Int accuracy);

    [0x5E2]
    void killCharOnFoot(GlobalIntFloat killTarget);
    
    [0x2A9]
    void setVulnerableToPlayerOnly(Bool vuln);
    
    [0x1B2]
    void giveWeaponWithAmmo(Int weapon, Int ammo);
    
    [0x173]
    void setZAngle(GlobalIntFloat zAngle);
    
    [0x638]
    void setStayPut(Bool stayPut);
    
    [0x4B8]
    void getWeaponSlotInfo(GlobalIntFloat slot, GlobalIntFloat id, GlobalIntFloat ammo, GlobalIntFloat model);
    
    [0x1B9]
    void setCurrentWeapon(Int weaponID);

    [0x5BA]
    void standStill(Int time);

    [0xDD]
    bool isInModel(Int modelNo);

    [0x3C0]
    void getVehicleWeak(GlobalIntFloat vehicleHandle);

    [0x449]
    bool sittingInVehicle();

    [0x4C8]
    bool isInAircraft();
}