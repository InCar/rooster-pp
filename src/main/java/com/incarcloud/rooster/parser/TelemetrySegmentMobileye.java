package com.incarcloud.rooster.parser;

import com.incarcloud.rooster.gather.BigTableEntry;
import org.json.*;

import java.time.ZonedDateTime;

public class TelemetrySegmentMobileye extends TelemetrySegment {
    @Override
    public BigTableEntry prepareBigTableEntry(String vin, ZonedDateTime tm){
        BigTableEntry t = new BigTableEntry(vin, "TriAdas.MobiEye", tm);
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
    public int resolve(byte[] buf, int start){
        final int LEN = 8; // 固定8字节
        // 长度检查
        if(buf.length - start < LEN){
            s_logger.error("三旗ADAS实时信息上报Mobileye数据包小于最小可能长度{}字节", LEN);
            return buf.length - start;
        }

        // sound
        int sound = buf[start] & 0x07;
        for(TricheerAdasSoundFlag f : TricheerAdasSoundFlag.values()){
            if(sound == f.getValue()){
                _flagSound = f;
                break;
            }
        }

        // daylight
        int daylight = ((buf[start] & 0x18) >> 3);
        for(TricheerAdasDayLight f:TricheerAdasDayLight.values()){
            if(daylight == f.getValue()){
                _daylight = f;
                break;
            }
        }

        // stopped
        _stopped = (buf[start+1] & 0x20) > 0;

        // headway
        boolean bHeadwayValid = (buf[start+2] & 0x01) > 0;
        if(!bHeadwayValid) _headway = -1.0f;
        else{
            _headway = ((buf[start+2] & 0xfe) >> 1) * 0.1f;
        }

        // error code
        boolean bHasError = (buf[start+3] & 0x01) == 0x00;
        if(!bHasError) _errorCode = -1;
        else{
            _errorCode = ((buf[start+3] & 0xfe) >> 1);
        }

        // LDW FCW FailSafe
        _ldwOff = (buf[start+4] & 0x01) > 0;
        _ldwLeft = (buf[start+4] & 0x02) > 0;
        _ldwRight = (buf[start+4] & 0x04) > 0;
        _fcw = (buf[start+4] & 0x08) > 0;
        _maintenance = (buf[start+4] & 0x40) > 0;
        _failsafe = (buf[start+4] & 0x80) > 0;

        // Peds
        _PedsFCW = (buf[start+5] & 0x02) > 0;
        _PedsDZ = (buf[start+5] & 0x04) > 0;
        _tamperAlert = (buf[start+5] & 0x20) > 0;
        _TSR = (buf[start+5] & 0x80) > 0;

        // Level
        _TSRLevel = (buf[start+6] & 0x07);
        _headwayLevel = (buf[start+7] & 0x03);
        _headwayRepeat = (buf[start+7] & 0x40) > 0;

        _resolved = true;
        return LEN;
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
