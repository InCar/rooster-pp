package com.incarcloud.rooster.parser;

import com.incarcloud.rooster.gather.BigTableEntry;
import org.json.JSONStringer;
import org.json.JSONWriter;

import java.util.Base64;

public class TricheerAdasPackTelemetryPos extends TricheerAdasPackTelemetry {
    @Override
    public BigTableEntry prepareBigTableEntry(){
        BigTableEntry t = new BigTableEntry(_vin, "TriAdas.Pos", _tm);
        JSONWriter writer = new JSONStringer()
                .object()
                .key("lon").value(_dLon)
                .key("lat").value(_dLat)
                .key("valid").value(_bValid)
                .endObject();
        t.setData(writer.toString());
        return t;
    }

    @Override
    protected void resolveFields(byte[] data){
        super.resolveFields(data);

        if(_payload.length < s_frameLenTelemetry+9){
            s_logger.error("三旗ADAS实时信息上报位置信息数据包长度{}小于最小可能长度{}字节",
                    _payload.length, s_frameLenTelemetry+9);
            return;
        }

        // valid 7:0
        _bValid = ((_payload[7] & 0x01) == 0x00);

        // lon 8...11 * 7:2
        _dLon = ((_payload[8]&0xff)<<24)|((_payload[9]&0xff)<<12)|((_payload[10]&0xff)<<8)|(_payload[11]&0xff);
        _dLon = (((_payload[7] & 0x04) == 0x00)?(_dLon):(-1.0*_dLon))*1.0E-6;
        // lat 12...15 * 7:1
        _dLat = ((_payload[12]&0xff)<<24)|((_payload[13]&0xff)<<12)|((_payload[14]&0xff)<<8)|(_payload[15]&0xff);
        _dLat = (((_payload[7] & 0x02) == 0x00)?(_dLat):(-1.0*_dLat))*1.0E-6;

        super._resolved = true;
    }

    // 如果是false,表明没有定位成功,返回的是最后一次有效定位值
    private boolean _bValid;
    public boolean isValid(){ return _bValid; }

    private double _dLon;
    public double getLon(){ return _dLon; }

    private double _dLat;
    public double getLat(){ return _dLat; }
}
