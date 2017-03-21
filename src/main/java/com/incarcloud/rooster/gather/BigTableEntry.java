package com.incarcloud.rooster.gather;

import org.apache.commons.codec.binary.Hex;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * 大数据项
 */
public class BigTableEntry {
    public BigTableEntry(){}

    public BigTableEntry(String vin, String mark, ZonedDateTime tm){
        setVIN(vin);
        setMark(mark);
        setTM(tm);
    }

    public BigTableEntry(String vin, String mark, ZonedDateTime tm, String data){
        this(vin, mark, tm);
        setData(data);
    }

    // 车架号,用于唯一标识一台车辆
    private String _vin;
    public String getVIN(){ return _vin; }
    public void setVIN(String value){
        // 强制17个字符
        _vin = forceLen(value, 17);
    }

    // 数据分类标识,相同类型的数据相邻存放
    private String _mark;
    public String getMark(){ return _mark; }
    public void setMark(String value){
        // 强制15个字符
        _mark = forceLen(value, 15);
    }

    // 时间戳
    private ZonedDateTime _tm;
    public ZonedDateTime getTM(){ return _tm; }
    public void setTM(ZonedDateTime value){ _tm = value.withZoneSameInstant(ZoneId.of("Z")); }

    // 数据
    private String _data;
    public String getData(){ return _data; }
    public void setData(String value){ _data = value; }

    // 生成主键
    public String makePK(){
        // MD5(VIN,前4位)+VIN+MARK+TIMESTAMP
        return String.format("%s%s%s%s",
                calcMd5(_vin).substring(0, 4),
                _vin,
                _mark,
                _tm.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")));
    }

    // 生成Vehicle主键
    public String makePKVehicle(){
        // MD5(VIN,前4位)+VIN
        return String.format("%s%s",
                calcMd5(_vin).substring(0, 4),
                _vin);
    }

    // 强制字符长度
    private static String forceLen(String value, int len){
        if(value == null) value = "";

        if(value.length() == len) return value;
        else if(value.length() > len)
            return value.substring(0, len);
        else{
            // 为了提高性能,不太长的字符串不使用StringBuilder来补足
            int lenPad = len - value.length();
            if(lenPad <= c_sharp32.length()) return value + c_sharp32.substring(0, lenPad);
            else{
                StringBuilder sbX = new StringBuilder(len);
                sbX.append(value);
                sbX.setLength(len);
                for(int i=value.length();i<len;i++)
                    sbX.setCharAt(i, '#');
                return sbX.toString();
            }
        }
    }

    // 固定32个字符,用来补充长度不足的字串
    private static final String c_sharp32 = "################################";

    // 计算一个字符串的MD5
    private static String calcMd5(String value){
        try {
            if (s_md5 == null) {
                s_md5 = MessageDigest.getInstance("MD5");
            }

            byte[] md5 = s_md5.digest(value.getBytes("utf-8"));
            return Hex.encodeHexString(md5);
        }
        catch(NoSuchAlgorithmException ex){
            // ignore
        }
        catch(UnsupportedEncodingException ex){
            // ignore
        }

        return null;
    }

    private static MessageDigest s_md5;
}
