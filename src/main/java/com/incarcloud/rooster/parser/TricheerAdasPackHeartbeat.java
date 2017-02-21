package com.incarcloud.rooster.parser;

import com.incarcloud.rooster.gather.BigTableEntry;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TricheerAdasPackHeartbeat extends TricheerAdasPack {
    @Override
    public BigTableEntry prepareBigTableEntry(){
        // 心跳数据只需要保留一份,数据列记录最后一次心跳时间
        BigTableEntry t = new BigTableEntry(_vin, "TriAdas.HeartBt", ZonedDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneId.of("Z")));
        String data = ZonedDateTime.now(ZoneId.of("Z")).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        t.setData(data);
        return t;
    }

    @Override
    protected void resolveFields(byte[] data){
        super.resolveFields(data);
        super._resolved = true;
    }
}
