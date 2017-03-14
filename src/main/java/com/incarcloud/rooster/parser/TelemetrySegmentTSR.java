package com.incarcloud.rooster.parser;

import com.incarcloud.rooster.gather.BigTableEntry;
import org.json.*;

import java.time.ZonedDateTime;
import java.util.*;

public class TelemetrySegmentTSR extends TelemetrySegment  {
    @Override
    public BigTableEntry prepareBigTableEntry(String vin, ZonedDateTime tm){
        BigTableEntry t = new BigTableEntry(vin, "TriAdas.TSR", tm);
        JSONWriter writer = new JSONStringer().array();
        {
            for(TSR tsr : _listTSR){
                writer.object()
                    .key("flag").value(tsr.flag.name())
                    .key("speed").value(tsr.value)
                    .key("flag2").value(tsr.flag2.name())
                    .key("pos").object()
                        .key("x").value(tsr.x)
                        .key("y").value(tsr.y)
                        .key("z").value(tsr.z)
                    .endObject()
                    .key("filter").value(tsr.filter.name())
                .endObject();
            }
        }
        writer.endArray();
        t.setData(writer.toString());
        return t;
    }

    @Override
    public int resolve(byte[] buf, int start){
        if(buf.length - start < 1){
            s_logger.error("三旗ADAS实时信息上报TSR数据包小于最小可能长度1字节");
            return buf.length - start;
        }

        // 解析TSR个数 1~7个
        final int nTSR = (buf[start] & 0xff);
        if(buf.length - start < 1+8*nTSR){
            s_logger.error("三旗ADAS实时信息上报TSR数据包长度{}无法容纳{}个TSR消息",
                    buf.length - start, nTSR);
            return buf.length - start;
        }
        if(nTSR <= 0 || nTSR > 7){
            s_logger.warn("三旗ADAS实时信息上报TSR数据包TSR数目{}不在1~7之间", nTSR);
        }

        // 解析TSR
        final int base = start+1;
        for(int i=0;i<nTSR;i++){
            TSR tsr = new TSR();

            TSRFlagV flagV = TSRFlag.from(buf[base+8*i] & 0xff);
            tsr.flag = flagV.flag;
            tsr.value = flagV.value;

            int flag2 = buf[base+8*i+1] & 0xff;
            for(TSR2Flag f2:TSR2Flag.values()){
                if(f2.getValue() == flag2){
                    tsr.flag2 = f2;
                    break;
                }
            }

            tsr.x = 0.5f * (buf[base+8*i+2] & 0xff);
            tsr.y = 0.5f * (buf[base+8*i+3] & 0x3f) * ((buf[base+8*i+3] & 0x40)>0?-1.0f:1.0f);
            tsr.z = 0.5f * (buf[base+8*i+4] & 0x1f) * ((buf[base+8*i+4] & 0x20)>0?-1.0f:1.0f);

            int filter = buf[base+8*i+5] & 0xff;
            for(TSRFilter f:TSRFilter.values()){
                if(f.getValue() == filter){
                    tsr.filter = f;
                    break;
                }
            }

            _listTSR.add(tsr);
        }

        _resolved = true;
        return 1+8*nTSR;
    }

    private List<TSR> _listTSR = new ArrayList<>();
}
