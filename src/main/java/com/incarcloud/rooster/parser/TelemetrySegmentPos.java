package com.incarcloud.rooster.parser;

import com.incarcloud.rooster.gather.BigTableEntry;
import org.json.*;

import java.time.ZonedDateTime;

public class TelemetrySegmentPos extends TelemetrySegment {
    @Override
    public BigTableEntry prepareBigTableEntry(String vin, ZonedDateTime tm){
        BigTableEntry t = new BigTableEntry(vin, "TriAdas.Pos", tm);
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
    public int resolve(byte[] buf, int start){
        final int LEN = 9; // 固定9字节
        if(buf.length - start < LEN){
            s_logger.error("三旗ADAS实时信息上报位置信息数据包小于最小可能长度{}字节", LEN);
            return buf.length - start;
        }

        // valid 0:0
        _bValid = ((buf[start] & 0x01) == 0x00);

        // lon 1...4 * 0:2
        _dLon = ((buf[start+1]&0xff)<<24)|((buf[start+2]&0xff)<<16)|((buf[start+3]&0xff)<<8)|(buf[start+4]&0xff);
        _dLon = (((buf[start] & 0x04) == 0x00)?(_dLon):(-1.0*_dLon))*1.0E-6;
        // lat 5...8 * 0:1
        _dLat = ((buf[start+5]&0xff)<<24)|((buf[start+6]&0xff)<<16)|((buf[start+7]&0xff)<<8)|(buf[start+8]&0xff);
        _dLat = (((buf[start] & 0x02) == 0x00)?(_dLat):(-1.0*_dLat))*1.0E-6;

        super._resolved = true;
        return LEN;
    }

    // 如果是false,表明没有定位成功,返回的是最后一次有效定位值
    private boolean _bValid;
    public boolean isValid(){ return _bValid; }

    private double _dLon;
    public double getLon(){ return _dLon; }

    private double _dLat;
    public double getLat(){ return _dLat; }
}
