package com.incarcloud.rooster.parser;

import com.incarcloud.rooster.gather.BigTableEntry;
import org.json.JSONStringer;
import org.json.JSONWriter;

public class TricheerAdasPackTelemetryMobileye extends TricheerAdasPackTelemetry {
    @Override
    public BigTableEntry prepareBigTableEntry(){
        BigTableEntry t = new BigTableEntry(_vin, "TriAdas.MobiEye", _tm);
        JSONWriter writer = new JSONStringer()
                .object()
                    .key("sound").value(_flagSound.name())
                    .key("daylight").value(_daylight.name())
                    .key("stopped").value(_stopped)
                    .key("headway").object()
                            .key("seconds").value(_headway)
                            .key("level").value(_headwayLevel)
                            .key("repeatable").value(_headwayRepeat)
                        .endObject()
                    .key("LDW").object()
                            .key("isOff").value(_ldwOff)
                            .key("left").value(_ldwLeft)
                            .key("right").value(_ldwRight)
                        .endObject()
                    .key("TamperAlert").value(_tamperAlert)
                    .key("FCW").value(_fcw)
                    .key("PedsFCW").value(_PedsFCW)
                    .key("PedsDZ").value(_PedsDZ)
                    .key("TSR").object()
                            .key("enabled").value(_TSR)
                            .key("level").value(_TSRLevel)
                        .endObject()
                    .key("maintenance").value(_maintenance)
                    .key("failsafe").value(_failsafe)
                    .key("error").value(_errorCode)
                .endObject();
        t.setData(writer.toString());
        return t;
    }

    @Override
    protected void resolveFields(byte[] data) {
        super.resolveFields(data);

        // 长度检查
        if(_payload.length < s_frameLenTelemetry+8){
            s_logger.error("三旗ADAS实时信息上报Mobileye数据包长度{}小于最小可能长度{}字节",
                    _payload.length, s_frameLenTelemetry+8);
            return;
        }
        else if(_payload.length > s_frameLenTelemetry+8){
            s_logger.warn("三旗ADAS实时信息上报Mobileye数据包长度过长,应当{}但有{}",
                    s_frameLenTelemetry+8, _payload.length);
        }

        // sound
        int sound = _payload[s_frameLenTelemetry] & 0x07;
        for(TricheerAdasSoundFlag f : TricheerAdasSoundFlag.values()){
            if(sound == f.getValue()){
                _flagSound = f;
                break;
            }
        }

        // daylight
        int daylight = ((_payload[s_frameLenTelemetry] & 0x18) >> 3);
        for(TricheerAdasDayLight f:TricheerAdasDayLight.values()){
            if(daylight == f.getValue()){
                _daylight = f;
                break;
            }
        }

        // stopped
        _stopped = (_payload[s_frameLenTelemetry+1] & 0x20) > 0;

        // headway
        boolean bHeadwayValid = (_payload[s_frameLenTelemetry+2] & 0x01) > 0;
        if(!bHeadwayValid) _headway = -1.0f;
        else{
            _headway = ((_payload[s_frameLenTelemetry+2] & 0xfe) >> 1) * 0.1f;
        }

        // error code
        boolean bHasError = (_payload[s_frameLenTelemetry+3] & 0x01) == 0x00;
        if(!bHasError) _errorCode = -1;
        else{
            _errorCode = ((_payload[s_frameLenTelemetry+3] & 0xfe) >> 1);
        }

        // LDW FCW FailSafe
        _ldwOff = (_payload[s_frameLenTelemetry+4] & 0x01) > 0;
        _ldwLeft = (_payload[s_frameLenTelemetry+4] & 0x02) > 0;
        _ldwRight = (_payload[s_frameLenTelemetry+4] & 0x04) > 0;
        _fcw = (_payload[s_frameLenTelemetry+4] & 0x08) > 0;
        _maintenance = (_payload[s_frameLenTelemetry+4] & 0x40) > 0;
        _failsafe = (_payload[s_frameLenTelemetry+4] & 0x80) > 0;

        // Peds
        _PedsFCW = (_payload[s_frameLenTelemetry+5] & 0x02) > 0;
        _PedsDZ = (_payload[s_frameLenTelemetry+5] & 0x04) > 0;
        _tamperAlert = (_payload[s_frameLenTelemetry+5] & 0x20) > 0;
        _TSR = (_payload[s_frameLenTelemetry+5] & 0x80) > 0;

        // Level
        _TSRLevel = (_payload[s_frameLenTelemetry+6] & 0x07);
        _headwayLevel = (_payload[s_frameLenTelemetry+7] & 0x03);
        _headwayRepeat = (_payload[s_frameLenTelemetry+7] & 0x40) > 0;

        _resolved = true;
    }

    private TricheerAdasSoundFlag _flagSound = TricheerAdasSoundFlag.Silent;
    public TricheerAdasSoundFlag getSoundFlag(){ return _flagSound; }

    private TricheerAdasDayLight _daylight = TricheerAdasDayLight.Day;
    public TricheerAdasDayLight getDaylight(){ return _daylight; }

    private boolean _stopped = false;
    public boolean isStopped(){ return _stopped; }

    // 秒,负值代表无效
    private float _headway = -1.0f;
    public boolean isHeadwayValid(){ return _headway > 0.0f; }
    public float getHeadway(){ return _headway; }

    // 负值代表无Error
    private int _errorCode = -1;
    public boolean hasError(){ return _errorCode >= 0; }
    public int getErrorCode(){ return _errorCode; }

    // LDW OFF
    private boolean _ldwOff = false;
    public boolean isLDWOFF(){ return _ldwOff; }

    // LDW Left
    private boolean _ldwLeft = false;
    public boolean isLDWLeftON(){ return _ldwLeft; }

    // LDW Right
    private boolean _ldwRight = false;
    public boolean isLDWRightON(){ return _ldwRight; }

    // FCW
    private boolean _fcw = false;
    public boolean isFCWON(){ return _fcw; }

    // Maintenance
    private boolean _maintenance = false;
    public boolean isMaintenance(){ return _maintenance; }

    // FailSafe
    private boolean _failsafe = false;
    public boolean isFailSafe(){ return _failsafe; }

    // PedsFCW
    private boolean _PedsFCW = false;
    public boolean isPedsFCW(){ return _PedsFCW; }

    // PedsDZ
    private boolean _PedsDZ = false;
    public boolean isPedsDZ(){ return _PedsDZ; }

    // TamperAlert
    private boolean _tamperAlert = false;
    public boolean isTamperAlert(){ return _tamperAlert; }

    // TSR
    private boolean _TSR = false;
    public boolean isTSREnabled(){ return _TSR; }

    // TSR Level
    private int _TSRLevel = 0;
    public int getTSRLevel(){ return _TSRLevel; }

    // Headway Warning Level
    private int _headwayLevel = 0;
    public int getHeadwayWarningLevel(){ return _headwayLevel; }

    // Headway repeatable enabled
    private boolean _headwayRepeat = false;
    public boolean isHeadwayRepeatableEnabled(){ return _headwayRepeat; }
}
