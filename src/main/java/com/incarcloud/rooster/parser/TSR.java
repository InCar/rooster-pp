package com.incarcloud.rooster.parser;

// Traffic Signs Recognition
public class TSR {
    public TSRFlag flag;
    public int value;
    public TSR2Flag flag2;
    public float x;
    public float y;
    public float z;
    public TSRFilter filter;

    public TSR(){
        flag = TSRFlag.NA;
        flag2 = TSR2Flag.NA;
        filter = TSRFilter.NA;
    }
}
