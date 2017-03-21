package com.incarcloud.rooster.parser;

import java.time.*;
import java.util.*;

import com.incarcloud.rooster.gather.BigTableEntry;
import org.slf4j.*;

/**
 * 三旗ADAS基本包
 */
public class TricheerAdasPack {
    TricheerAdasPack(){ }

    public BigTableEntry prepareBigTableEntry(){
        BigTableEntry t = new BigTableEntry(_vin, "TriAdas-b64", ZonedDateTime.now(ZoneId.of("Z")));
        String b64 = Base64.getEncoder().encodeToString(_data);
        t.setData(b64);
        return t;
    }

    public List<BigTableEntry> prepareBigTableEntries(){
        List<BigTableEntry> listEntries = new ArrayList<>();
        listEntries.add(prepareBigTableEntry());
        return listEntries;
    }

    protected TricheerAdasCmd _cmd = TricheerAdasCmd.NA;
    public TricheerAdasCmd getCmd(){ return this._cmd; }

    protected TricheerAdasRespFlag _flagResp = TricheerAdasRespFlag.NA;
    public TricheerAdasRespFlag getRespFlag(){ return this._flagResp; }

    protected String _vin;
    public String getVin(){ return this._vin; }

    protected byte _encryptMode;
    public byte getEncryptMode(){ return this._encryptMode; }

    protected byte[] _payload;
    protected byte[] _data;

    // 解析完成
    protected boolean _resolved = false;
    public boolean isResolved(){ return this._resolved; }

    protected static Logger s_logger = LoggerFactory.getLogger(TricheerAdasPack.class);

    // 解析
    protected void resolveFields(byte[] data){
        // cmd
        for(TricheerAdasCmd cmd : TricheerAdasCmd.values()){
            if(data[2] == cmd.getValue()) this._cmd = cmd;
        }

        // response flag
        for(TricheerAdasRespFlag flag: TricheerAdasRespFlag.values()){
            if(data[3] == flag.getValue()) this._flagResp = flag;
        }

        // VIN
        this._vin = new String(data, 4, 17);

        // 加密(KEY从哪儿来?) 1-PLAIN 2-RSA 3-AES128 0xfe-EXCEPTION 0xff-INVALID
        this._encryptMode = data[21];
        if(this._encryptMode != 0x01){
            s_logger.error("尚无法处理加密的数据包");
        }

        // 数据单元
        this._data = data;
        this._payload = Arrays.copyOfRange(data, 24, 24+(((data[22]&0xff)<<8)|(data[23]&0xff)));

        // 如果是派生类,解析还没有完成
        if(this.getClass() == TricheerAdasPack.class)
            this._resolved = true;
    }

    public static TricheerAdasPack parse(byte[] data){
        // 基本检查如果不通过,直接返回null
        if(!TricheerAdasPack.check(data)) return null;

        // 分类构造
        TricheerAdasPack pack = null;
        if(data[2] == TricheerAdasCmd.Login.getValue())
            pack = new TricheerAdasPackLogin();
        else if(data[2] == TricheerAdasCmd.HeartBeat.getValue())
            pack = new TricheerAdasPackHeartbeat();
        else if(data[2] == TricheerAdasCmd.Telemetry.getValue()){
            pack = new TricheerAdasPackTelemetry();
        }
        else
            pack = new TricheerAdasPack();

        if(pack != null) pack.resolveFields(data);
        return pack;
    }

    public static boolean check(byte[] data){
        if(data == null) return false;

        // 数据包最小长度检查
        if(data.length < s_frameLen) return false;

        // leading '$$'
        if(data[0] != '$' || data[1] != '$') return false;

        // VIN 只能包含可见字符0x20~0x7e
        for(int i=4;i<21;i++){
            int v = data[i]&0xff;
            if(v < 0x20 || v > 0x7e){
                s_logger.error("无效VIN码,包含不可见ASCII码");
                return false;
            }
        }

        // 可变区长度
        int dataLen = ((data[22]&0xff) << 8) | (data[23]&0xff);
        if(data.length < dataLen + s_frameLen) return false;

        // 异或校验码
        byte bcc = 0x00;
        for(int i=2;i<data.length-1;i++) bcc ^= data[i];
        if(data[dataLen+s_frameLen-1] != bcc) {
            s_logger.error(String.format("BCC checking failed, should be 0x%02X but 0x%02X", bcc, data[dataLen+s_frameLen-1]));
            return false;
        }

        return true;
    }

    protected static final int s_frameLen = 25;

    protected static ZonedDateTime correctTime(ZonedDateTime tm){
        // 如果时间早于2017年,则返回当前时间
        if(tm != null && tm.getYear() < 2017){
            return ZonedDateTime.now(ZoneId.of("Z"));
        }

        return tm;
    }
}


