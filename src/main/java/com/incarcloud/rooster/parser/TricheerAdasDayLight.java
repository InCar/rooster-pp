package com.incarcloud.rooster.parser;

public enum TricheerAdasDayLight {
    Day(0x00),
    Dusk(0x01),
    Night(0x02);

    TricheerAdasDayLight(int value){
        this._value = value;
    }

    public int getValue(){ return this._value; }

    private final int _value;
}
