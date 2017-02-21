package com.incarcloud.rooster.parser;

/**
 * 三旗ADAS数据包命令枚举
 * 这部分扩展了GB/T32960-2016
 * 以后应考虑整合进一个分层解析框架中
 */
public enum TricheerAdasCmd {
    NA(0x00),
    Login(0x01),
    Telemetry(0x02),
    TelemetryResend(0x03),
    Logout(0x04),
    PlatformLogin(0x05),
    PlatformLogout(0x06),
    HeartBeat(0x07),
    TimeCalibration(0x08),
    Query(0x80),
    Config(0x81),
    RemoteCtrl(0x82);

    TricheerAdasCmd(int value){
        this._value = (byte)value;
    }

    public byte getValue(){ return this._value; }

    private final byte _value;
}
