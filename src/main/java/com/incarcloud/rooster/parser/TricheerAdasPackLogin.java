package com.incarcloud.rooster.parser;

import com.incarcloud.rooster.gather.BigTableEntry;
import org.json.JSONStringer;
import org.json.JSONWriter;

import java.time.*;

public class TricheerAdasPackLogin extends TricheerAdasPack {

    @Override
    public BigTableEntry prepareBigTableEntry(){
        BigTableEntry t = new BigTableEntry(_vin, "TriAdas.Login", _tm);

        JSONWriter writer = new JSONStringer()
            .object()
                .key("sn").value(_sn)
                .key("iccid").value(_iccid)
                .key("subsystem").array();
                    for(String s : _subsystem) writer.value(s);
                writer.endArray()
            .endObject();
        t.setData(writer.toString());
        return t;
    }

    @Override
    protected void resolveFields(byte[] data){
        super.resolveFields(data);

        if(_payload.length < 30) {
            s_logger.error("三旗ADAS登入数据包长度{}小于最小可能长度30字节", _payload.length);
            return; // 数据区的最小可能尺寸
        }

        // timestamp 0...5
        this._tm = ZonedDateTime.of(2000+_payload[0], _payload[1], _payload[2],
                _payload[3], _payload[4], _payload[5], 0, ZoneId.of("+8")).withZoneSameInstant(ZoneId.of("Z"));
        this._tm = correctTime(this._tm);

        // SN 6,7
        this._sn = ((_payload[6]&0xff)<<8)|(_payload[7]&0xff);

        // ICCID 8...27
        this._iccid = new String(_payload, 8, 20);

        // subsystem count 28 * each len 29
        int count = _payload[28]&0xff;
        int size = _payload[29]&0xff;
        if(_payload.length < 30 + size*count){
            s_logger.error("三旗ADAS登入数据包长度错误,期望{}字节,但只有{}字节", 30 + size*count, _payload.length);
            return;
        }
        this._subsystem = new String[count];
        for(int i=0;i<count;i++){
            this._subsystem[i] = new String(_payload, 30+size*i, size);
        }

        super._resolved = true;
    }

    private ZonedDateTime _tm;
    public ZonedDateTime getTimeStamp(){ return this._tm; }

    private int _sn;
    public int getSN(){ return this._sn; }

    private String _iccid;
    public String getICCID(){ return this._iccid; }

    private String[] _subsystem;
}
