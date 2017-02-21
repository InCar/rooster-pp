package com.incarcloud.rooster.parser;

public enum TSRFilter {
    NA(0x00),
    IrrelevantToHost(0x01),
    TSonVehicle(0x02),
    Embedded(0x03);

    TSRFilter(int value){ this._value = value; }

    public int getValue(){ return this._value; }

    private final int _value;
}
