package com.incarcloud.rooster.parser;

import com.incarcloud.rooster.gather.BigTableEntry;
import org.json.*;

import java.time.ZonedDateTime;
import java.util.*;

public class TelemetrySegmentTSRDecision extends TelemetrySegment{
    @Override
    public BigTableEntry prepareBigTableEntry(String vin, ZonedDateTime tm){
        BigTableEntry t = new BigTableEntry(vin, "TriAdas.TSRD", tm);
        JSONWriter writer = new JSONStringer().array();
        {
            for(TSR tsr : _listTSR){
                writer.object()
                        .key("flag").value(tsr.flag.name())
                        .key("speed").value(tsr.value)
                        .key("flag2").value(tsr.flag2.name())
                    .endObject();
            }
        }
        writer.endArray();
        t.setData(writer.toString());
        return t;
    }

    @Override
    public int resolve(byte[] buf, int start){
        final int LEN = 8; // 固定长度8字节
        // 长度检查
        if(buf.length - start < LEN){
            s_logger.error("三旗ADAS实时信息上报TSRDecision数据包小于最小可能长度{}字节", LEN);
            return buf.length - start;
        }

        final int nTSR = 4; // 固定4个
        for(int i=0;i<4;i++){
            TSR tsr = new TSR();

            TSRFlagV flagV = TSRFlag.from(buf[start+2*i] & 0xff);
            tsr.flag = flagV.flag;
            tsr.value = flagV.value;

            int flag2 = buf[start+2*i+1] & 0xff;
            for(TSR2Flag f2:TSR2Flag.values()){
                if(f2.getValue() == flag2){
                    tsr.flag2 = f2;
                    break;
                }
            }

            _listTSR.add(tsr);
        }

        _resolved = true;
        return LEN;
    }

    private List<TSR> _listTSR = new ArrayList<>();
}
