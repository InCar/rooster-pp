package com.incarcloud.rooster.parser;

public enum TSR2Flag {
    NA(0x00),
    Rain(0x01),
    Snow(0x02),
    Trailer(0x03),
    Time(0x04),
    ArrowLeft(0x05),
    ArrowRight(0x06),
    BendArrowLeft(0x07),
    BendArrowRight(0x08),
    Truck(0x09),
    DistanceArrow(0x0a),
    Weight(0x0b),
    DistanceIn(0x0c),
    Tractor(0x0d),
    SnowRain(0x0e),
    School(0x0f),
    RainCloud(0x10),
    Fog(0x11),
    HazardousMaterial(0x12),
    Night(0x13),
    NotInUse(0xff);

    TSR2Flag(int value){ this._value = value; }

    public int getValue(){ return this._value; }

    private final int _value;
}
