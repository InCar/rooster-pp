package com.incarcloud.rooster.parser;

import com.incarcloud.rooster.BaseTest;
import com.incarcloud.rooster.gather.BigTableEntry;
import org.apache.commons.codec.binary.Hex;
import org.junit.*;

import java.util.*;

public class TricheerAdasPackTest extends BaseTest {
    @Test
    public void TricheerAdasPackLogin(){
        byte[] data = {0x24, 0x24, 0x01, (byte)0xfe,
                0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37,
                0x01, 0x00, 36, // enc+len
                17, 2, 14, 17, 5, 19, // TIMESTAMP
                0x00, 0x07, // SN
                0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x30, // ICCID
                0x03, 0x02,
                0x31, 0x32, 0x31, 0x32, 0x31, 0x32,
                (byte)0xf4 };
        s_logger.info("Parse Tricheer ADAS package for LOGIN...");
        s_logger.info(Hex.encodeHexString(data));
        TricheerAdasPack pack = TricheerAdasPack.parse(data);
        BigTableEntry t = pack.prepareBigTableEntry();
        s_logger.info(t.makePK());
        s_logger.info(t.getData());
    }

    @Test
    public void TricheerAdasPackLoginWithZeroLengthSubSystem(){
        byte[] data = {0x24, 0x24, 0x01, (byte)0xfe,
                0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37,
                0x01, 0x00, 30, // enc+len
                17, 2, 14, 17, 5, 19, // TIMESTAMP
                0x00, 0x07, // SN
                0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x30, // ICCID
                0x03, 0x00,
                (byte)0xcf };
        s_logger.info("Parse Tricheer ADAS package for LOGIN...");
        s_logger.info(Hex.encodeHexString(data));
        TricheerAdasPack pack = TricheerAdasPack.parse(data);
        BigTableEntry t = pack.prepareBigTableEntry();
        s_logger.info(t.makePK());
        s_logger.info(t.getData());
    }

    @Test
    public void TricheerAdasPackLoginWithZeroSubSystem(){
        byte[] data = {0x24, 0x24, 0x01, (byte)0xfe,
                0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37,
                0x01, 0x00, 30, // enc+len
                17, 2, 14, 17, 5, 19, // TIMESTAMP
                0x00, 0x07, // SN
                0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x30, // ICCID
                0x00, 0x00,
                (byte)0xcc };
        s_logger.info("Parse Tricheer ADAS package for LOGIN...");
        s_logger.info(Hex.encodeHexString(data));
        TricheerAdasPack pack = TricheerAdasPack.parse(data);
        BigTableEntry t = pack.prepareBigTableEntry();
        s_logger.info(t.makePK());
        s_logger.info(t.getData());
    }

    @Test
    public void TricheerAdasPackTelemetry(){
        byte[] data = {0x24, 0x24, 0x02, (byte)0xfe,
                0x54, 0x45, 0x53, 0x54, 0x23, 0x49, 0x43, 0x23, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39,
                0x01, 0x00, 0x67,
                0x11, 0x03, 0x07, 0x0b, 0x1e, 0x00, 0x01, 0x05,
                (byte)0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
                (byte)0x81, 0x07,
                    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                (byte)0x82, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                (byte)0x83, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x05, 0x01, 0x06, (byte)0xca, (byte)0xaf, (byte)0xf0, 0x01, 0x57, (byte)0xff, (byte)0xec,
                0x66 };
        s_logger.info("Parse Tricheer ADAS package for TELEMETRY...");
        s_logger.info(Hex.encodeHexString(data));
        TricheerAdasPack pack = TricheerAdasPack.parse(data);
        List<BigTableEntry> listEntries = pack.prepareBigTableEntries();
        for(BigTableEntry t:listEntries) {
            s_logger.info(t.makePK());
            s_logger.info(t.getData());
        }
    }
}
