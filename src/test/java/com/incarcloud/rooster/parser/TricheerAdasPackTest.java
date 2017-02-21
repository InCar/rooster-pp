package com.incarcloud.rooster.parser;

import com.incarcloud.rooster.BaseTest;
import com.incarcloud.rooster.gather.BigTableEntry;
import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.junit.*;
import org.slf4j.*;

public class TricheerAdasPackTest extends BaseTest {
    @Test
    public void TricheerAdasPackLogin(){
        byte[] data = {0x23, 0x23, 0x01, (byte)0xfe,
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
        byte[] data = {0x23, 0x23, 0x01, (byte)0xfe,
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
        byte[] data = {0x23, 0x23, 0x01, (byte)0xfe,
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
}
