package com.incarcloud.rooster.parser;

import com.incarcloud.rooster.gather.BigTableEntry;
import org.json.JSONStringer;
import org.json.JSONWriter;

import java.util.ArrayList;
import java.util.List;

public class TricheerAdasPackTelemetryTSR extends TricheerAdasPackTelemetry {
    @Override
    public BigTableEntry prepareBigTableEntry(){
        BigTableEntry t = new BigTableEntry(_vin, "TriAdas.TSR", _tm);
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
    protected void resolveFields(byte[] data){
        super.resolveFields(data);

        if(_payload.length < s_frameLenTelemetry+1){
            s_logger.error("三旗ADAS实时信息上报TSR数据包长度{}小于最小可能长度{}字节",
                    _payload.length, s_frameLenTelemetry+1);
            return;
        }

        // 解析TSR个数 1~7个
        final int nTSR = (_payload[s_frameLenTelemetry] & 0xff);
        if(_payload.length < s_frameLenTelemetry+1+8*nTSR){
            s_logger.error("三旗ADAS实时信息上报TSR数据包长度{}无法容纳{}个TSR消息",
                    _payload.length, nTSR);
            return;
        }
        else if(_payload.length > s_frameLenTelemetry+1+8*nTSR){
            s_logger.warn("三旗ADAS实时信息上报TSR数据包长度过长,应当{}但有{}",
                    s_frameLenTelemetry+1+8*nTSR, _payload.length);
        }
        if(nTSR < 0 || nTSR > 7){
            s_logger.warn("三旗ADAS实时信息上报TSR数据包TSR数目{}不在1~7之间", nTSR);
        }

        // 解析TSR
        final int base = s_frameLenTelemetry+1;
        for(int i=0;i<nTSR;i++){
            TSR tsr = new TSR();

            TSRFlagV flagV = TSRFlag.from(_payload[base+8*i] & 0xff);
            tsr.flag = flagV.flag;
            tsr.value = flagV.value;

            int flag2 = _payload[base+8*i+1] & 0xff;
            for(TSR2Flag f2:TSR2Flag.values()){
                if(f2.getValue() == flag2){
                    tsr.flag2 = f2;
                    break;
                }
            }

            tsr.x = 0.5f * (_payload[base+8*i+2] & 0xff);
            tsr.y = 0.5f * (_payload[base+8*i+3] & 0x3f) * ((_payload[base+8*i+3] & 0x40)>0?-1.0f:1.0f);
            tsr.z = 0.5f * (_payload[base+8*i+4] & 0x1f) * ((_payload[base+8*i+4] & 0x20)>0?-1.0f:1.0f);

            int filter = _payload[base+8*i+5] & 0xff;
            for(TSRFilter f:TSRFilter.values()){
                if(f.getValue() == filter){
                    tsr.filter = f;
                    break;
                }
            }

            _listTSR.add(tsr);
        }

        _resolved = true;
    }

    private List<TSR> _listTSR = new ArrayList<>();
}
