package com.incarcloud.rooster.parser;

import com.incarcloud.rooster.gather.BigTableEntry;
import org.json.JSONStringer;
import org.json.JSONWriter;

import java.util.*;

public class TricheerAdasPackTelemetryTSRDecision extends TricheerAdasPackTelemetry {
    @Override
    public BigTableEntry prepareBigTableEntry(){
        BigTableEntry t = new BigTableEntry(_vin, "TriAdas.TSRD", _tm);
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
    protected void resolveFields(byte[] data){
        super.resolveFields(data);

        // 长度检查
        if(_payload.length < s_frameLenTelemetry+8){
            s_logger.error("三旗ADAS实时信息上报TSRDecision数据包长度{}小于最小可能长度{}字节",
                    _payload.length, s_frameLenTelemetry+8);
            return;
        }
        else if(_payload.length > s_frameLenTelemetry+8){
            s_logger.warn("三旗ADAS实时信息上报TSRDecision数据包长度过长,应当{}但有{}",
                    s_frameLenTelemetry+8, _payload.length);
        }

        final int nTSR = 4; // 固定4个
        for(int i=0;i<4;i++){
            TSR tsr = new TSR();

            TSRFlagV flagV = TSRFlag.from(_payload[s_frameLenTelemetry+2*i] & 0xff);
            tsr.flag = flagV.flag;
            tsr.value = flagV.value;

            int flag2 = _payload[s_frameLenTelemetry+2*i+1] & 0xff;
            for(TSR2Flag f2:TSR2Flag.values()){
                if(f2.getValue() == flag2){
                    tsr.flag2 = f2;
                    break;
                }
            }

            _listTSR.add(tsr);
        }

        _resolved = true;
    }

    private List<TSR> _listTSR = new ArrayList<>();
}
