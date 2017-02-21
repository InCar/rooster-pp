package com.incarcloud.rooster.parser;

import com.incarcloud.rooster.gather.BigTableEntry;

import java.time.*;
import java.util.Base64;

public class TricheerAdasPackTelemetry extends TricheerAdasPack{
    @Override
    public BigTableEntry prepareBigTableEntry(){
        BigTableEntry t = new BigTableEntry(_vin, "TriAdas.TMR-b64", _tm);
        String b64 = Base64.getEncoder().encodeToString(_data);
        t.setData(b64);
        return t;
    }

    @Override
    protected void resolveFields(byte[] data){
        super.resolveFields(data);

        // timestamp 0...5
        this._tm = ZonedDateTime.of(2000+_payload[0], _payload[1], _payload[2],
                _payload[3], _payload[4], _payload[5], 0, ZoneId.of("+8")).withZoneSameInstant(ZoneId.of("Z"));

        // flag 6
        for(TricheerAdasFlag flag:TricheerAdasFlag.values()){
            if(data[6] == flag.getValue()) this._flag = flag;
        }
    }

    protected ZonedDateTime _tm;
    public ZonedDateTime getTimeStamp(){ return this._tm; }

    protected TricheerAdasFlag _flag = TricheerAdasFlag.NA;
    public TricheerAdasFlag getFlag(){ return this._flag; }

    static TricheerAdasPackTelemetry create(byte[] data){
        if(data.length < (s_frameLen+s_frameLenTelemetry)){
            s_logger.error("三旗ADAS实时信息上报数据包长度{}小于最小可能长度{}字节",
                    data.length, s_frameLen+s_frameLenTelemetry);
            return null;
        }

        if(data[30] == TricheerAdasFlag.Position.getValue())
            return new TricheerAdasPackTelemetryPos();
        else if(data[30] == TricheerAdasFlag.MobileyeCarInfo.getValue())
            return new TricheerAdasPackTelemetryCarInfo();
        else if(data[30] == TricheerAdasFlag.MobileysTrafficSign.getValue())
            return new TricheerAdasPackTelemetryTSR();
        else if(data[30] == TricheerAdasFlag.MobileysTrafficSignDecision.getValue())
            return new TricheerAdasPackTelemetryTSRDecision();
        else if(data[30] == TricheerAdasFlag.MobileyeStd.getValue())
            return new TricheerAdasPackTelemetryMobileye();
        else
            return new TricheerAdasPackTelemetry();
    }

    protected static final int s_frameLenTelemetry = 7;
}
