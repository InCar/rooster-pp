package com.incarcloud.rooster.parser;

import com.incarcloud.rooster.gather.BigTableEntry;
import org.json.*;

import java.time.ZonedDateTime;

public class TelemetrySegmentCarInfo extends TelemetrySegment {
    @Override
    public BigTableEntry prepareBigTableEntry(String vin, ZonedDateTime tm){
        BigTableEntry t = new BigTableEntry(vin, "TriAdas.CarInfo", tm);
        JSONWriter writer = new JSONStringer()
                .object()
                    .key("brakes").value(_brakes)
                    .key("left").value(_left)
                    .key("right").value(_right)
                    .key("wipers").value(_wipersAvailable?_wipers:"NA")
                    .key("beam").object()
                        .key("low").value(_beamLowAvailable?_beamLow:"NA")
                        .key("high").value(_beamHighAvailable?_beamHigh:"NA")
                    .endObject()
                    .key("speed").value(_speedAvailable?_speed:"NA")
                .endObject();
        t.setData(writer.toString());
        return t;
    }

    @Override
    public int resolve(byte[] buf, int start){
        final int LEN = 8; // 固定8字节

        if(buf.length - start < LEN){
            s_logger.error("三旗ADAS实时信息上报整车信息数据包小于最小可能长度{}字节", LEN);
            return buf.length - start;
        }

        _brakes = ((buf[start] & 0x01) > 0);
        _left =   ((buf[start] & 0x02) > 0);
        _right =  ((buf[start] & 0x04) > 0);
        _wipers = ((buf[start] & 0x08) > 0);

        _beamLow =   ((buf[start] & 0x10) > 0);
        _beamHigh =  ((buf[start] & 0x20) > 0);

        _wipersAvailable   = ((buf[start+1] & 0x08) > 0);
        _beamLowAvailable  = ((buf[start+1] & 0x10) > 0);
        _beamHighAvailable = ((buf[start+1] & 0x20) > 0);
        _speedAvailable    = ((buf[start+1] & 0x80) > 0);

        _speed = (buf[start+2] & 0xff);

        super._resolved = true;
        return LEN;
    }

    private boolean _brakes;
    public boolean getBrakes(){ return _brakes; }

    private boolean _left;
    public boolean getLeft(){ return _left; }

    private boolean _right;
    public boolean getRight(){ return _right; }

    private boolean _wipers;
    public boolean getWipers(){ return _wipers; }

    private boolean _wipersAvailable;
    public boolean getWipersAvailable(){ return _wipersAvailable; }

    private boolean _beamLow;
    public boolean getBeamLow(){ return _beamLow; }

    private boolean _beamLowAvailable;
    public boolean getBeamLowAvailable(){ return _beamLowAvailable; }

    private boolean _beamHigh;
    public boolean getBeamHigh(){ return _beamHigh; }

    private boolean _beamHighAvailable;
    public boolean getBeamHighAvailable(){ return _beamHighAvailable; }

    private int _speed;
    public int getSpeed(){ return _speed; }

    private boolean _speedAvailable;
    public boolean getSpeedAvailable(){ return _speedAvailable; }
}
