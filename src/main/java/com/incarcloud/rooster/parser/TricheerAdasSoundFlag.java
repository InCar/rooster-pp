package com.incarcloud.rooster.parser;

public enum TricheerAdasSoundFlag {
    Silent(0x00),
    LaneDepartureWarningLeft(0x01),
    LaneDepartureWarningRight(0x02),
    HeadWay(0x03),
    TrafficSignsRecognition(0x04),
    UFCW(0x05),
    FCWandPCW(0x06);

    TricheerAdasSoundFlag(int value){
        this._value = value;
    }

    public int getValue(){ return this._value; }

    private final int _value;
}
