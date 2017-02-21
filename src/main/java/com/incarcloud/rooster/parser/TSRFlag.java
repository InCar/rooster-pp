package com.incarcloud.rooster.parser;

/**
 * 交通标识
 */
public enum TSRFlag {
    NA,
    Regular,
    RegularEnd,
    RegularEndAll,
    Electronic,
    ElectronicEnd,
    ElectronicEndAll,
    MotorWayBegin,
    MotorWayEnd,
    ExpressWayBegin,
    ExpressWayEnd,
    PlaygroundAreaBegin,
    PlaygroundAreaEnd,
    NoPassingBegin,
    NoPassingEnd,
    ElectronicNoPassingBegin,
    ElectronicNoPassingEnd,
    NoSignDetected,
    InvalidSign;

    // 转换
    public static TSRFlagV from(int value){
        TSRFlagV x = new TSRFlagV();

        if(value >=0 && value < 20){
            x.flag = Regular;
            x.value = 10 * (value + 1);
        }
        else if(value == 20){
            x.flag = RegularEnd;
        }
        else if(value >= 28 && value < 50){
            x.flag = Electronic;
            x.value = 10 * (value - 27);
        }
        else if(value == 50){
            x.flag = ElectronicEnd;
        }
        else if(value == 64){
            x.flag = RegularEndAll;
        }
        else if(value == 65){
            x.flag = ElectronicEndAll;
        }
        else if(value >= 100 && value <= 114){
            x.flag = Regular;
            x.value = 10 * (value - 100) + 5;
        }
        else if(value >= 115 && value <= 129){
            x.flag = Electronic;
            x.value = 10 * (value - 115) + 5;
        }
        else{
            switch(value){
                case 171: x.flag = MotorWayBegin; break;
                case 172: x.flag = MotorWayEnd; break;
                case 173: x.flag = ExpressWayBegin; break;
                case 174: x.flag = ExpressWayEnd; break;
                case 175: x.flag = PlaygroundAreaBegin; break;
                case 176: x.flag = PlaygroundAreaEnd; break;
                case 200: x.flag = NoPassingBegin; break;
                case 201: x.flag = NoPassingEnd; break;
                case 220: x.flag = ElectronicNoPassingBegin; break;
                case 221: x.flag = ElectronicNoPassingEnd; break;
                case 254: x.flag = NoSignDetected; break;
                case 255: x.flag = InvalidSign; break;
                default:
                    x.flag = NA;
                    x.value = value;
            }
        }

        return x;
    }
}

class TSRFlagV{
    public TSRFlag flag;
    public int value;
}