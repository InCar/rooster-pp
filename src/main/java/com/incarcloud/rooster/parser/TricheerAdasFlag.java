package com.incarcloud.rooster.parser;

/**
 * 三旗测量数据标志位
 */
public enum TricheerAdasFlag {
    NA(0x00),
    Vehile(0x01),
    Motor(0x02),
    Battery(0x03),
    Engine(0x04),
    Position(0x05),
    Extreme(0x06),
    Warning(0x07),
    Voltage(0x08),
    Temperature(0x09),
    MobileyeStd(0x80),
    MobileysTrafficSign(0x81),
    MobileysTrafficSignDecision(0x82),
    MobileyeCarInfo(0x83);

    TricheerAdasFlag(int value){ this._value = (byte)value; }

    public byte getValue(){ return this._value; }

    private final byte _value;
}
