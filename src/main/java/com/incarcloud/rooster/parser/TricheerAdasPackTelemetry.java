package com.incarcloud.rooster.parser;

import com.incarcloud.rooster.gather.BigTableEntry;

import java.time.*;
import java.util.*;

public class TricheerAdasPackTelemetry extends TricheerAdasPack{
    @Override
    public BigTableEntry prepareBigTableEntry(){
        throw new UnsupportedOperationException();
    }

    @Override
    public List<BigTableEntry> prepareBigTableEntries(){
        List<BigTableEntry> listEntries = new ArrayList<>();
        for(TelemetrySegment s:_listSegments){
            BigTableEntry entry = s.prepareBigTableEntry(_vin, _tm);
            if(entry != null) listEntries.add(entry);
        }
        return listEntries;
    }

    @Override
    protected void resolveFields(byte[] data){
        super.resolveFields(data);

        // 2017-03-21 增加2个字节的毫秒
        // timestamp 0...7
        int ms = ((_payload[6]&0xff) << 8 | _payload[7]&0xff);
        this._tm = ZonedDateTime.of(2000+_payload[0], _payload[1], _payload[2],
                _payload[3], _payload[4], _payload[5], ms*1000000, ZoneId.of("+8")).withZoneSameInstant(ZoneId.of("Z"));
        this._tm = correctTime(this._tm);

        int posNext = 8;
        do{
            // flag
            TricheerAdasFlag flag = TricheerAdasFlag.NA;
            for(TricheerAdasFlag f: TricheerAdasFlag.values()){
                if(f.getValue() == _payload[posNext]){
                    flag = f;
                    break;
                }
            }

            TelemetrySegment segment = TelemetrySegment.create(flag);
            if(segment == null) {
                s_logger.warn("忽略余下的数据区");
                break;
            }
            int bytes = segment.resolve(_payload, posNext+1);
            if(segment.isResolved()) _listSegments.add(segment);
            else s_logger.warn("三旗ADAS实时信息上报一个数据段{}解析失败,忽略", flag.name());

            posNext += (bytes + 1);
        }while(posNext < _payload.length);

        super._resolved = true;
    }

    protected ZonedDateTime _tm;
    public ZonedDateTime getTimeStamp(){ return this._tm; }

    private List<TelemetrySegment> _listSegments = new ArrayList<>();
}
