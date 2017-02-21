package com.incarcloud.rooster.parser;

public enum TricheerAdasRespFlag {
    NA(0x00),
    SUCCESS(0x01),
    FAIL(0X02),
    VinConflict(0x03),
    Cmd(0xfe);

    TricheerAdasRespFlag(int value){
        this._value = (byte)value;
    }

    public byte getValue(){ return this._value; }

    private final byte _value;
}
