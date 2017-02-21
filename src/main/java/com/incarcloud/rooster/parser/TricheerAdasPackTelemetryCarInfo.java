package com.incarcloud.rooster.parser;

import com.incarcloud.rooster.gather.BigTableEntry;
import org.json.*;

public class TricheerAdasPackTelemetryCarInfo extends TricheerAdasPackTelemetry {
    @Override
    public BigTableEntry prepareBigTableEntry(){
        BigTableEntry t = new BigTableEntry(_vin, "TriAdas.CarInfo", _tm);
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
    protected void resolveFields(byte[] data){
        super.resolveFields(data);

        if(_payload.length < s_frameLenTelemetry+8){
            s_logger.error("三旗ADAS实时信息上报整车信息数据包长度{}小于最小可能长度{}字节",
                    _payload.length, s_frameLenTelemetry+8);
            return;
        }

        _brakes = ((_payload[s_frameLenTelemetry] & 0x01) > 0);
        _left =   ((_payload[s_frameLenTelemetry] & 0x02) > 0);
        _right =  ((_payload[s_frameLenTelemetry] & 0x04) > 0);
        _wipers = ((_payload[s_frameLenTelemetry] & 0x08) > 0);

        _beamLow =   ((_payload[s_frameLenTelemetry] & 0x10) > 0);
        _beamHigh =  ((_payload[s_frameLenTelemetry] & 0x20) > 0);

        _wipersAvailable   = ((_payload[s_frameLenTelemetry+1] & 0x08) > 0);
        _beamLowAvailable  = ((_payload[s_frameLenTelemetry+1] & 0x10) > 0);
        _beamHighAvailable = ((_payload[s_frameLenTelemetry+1] & 0x20) > 0);
        _speedAvailable    = ((_payload[s_frameLenTelemetry+1] & 0x80) > 0);

        _speed = (_payload[s_frameLenTelemetry+2] & 0xff);

        super._resolved = true;
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
