package com.incarcloud.rooster.gather;

import com.incarcloud.rooster.BaseTest;
import org.junit.*;
import org.slf4j.*;

import java.time.ZonedDateTime;

public class BigTableEntryTest extends BaseTest{
    @Test
    public void PrimaryKeyTest(){
        BigTableEntry t = new BigTableEntry("V123", "MKmoreThan15Characters", ZonedDateTime.parse("2017-02-23T19:09:22+08:00"));
        s_logger.info("MD5(VIN)前4位+VIN17位+MARK15位+时间戳14位 长度不足的补#超过的截短");
        s_logger.info(t.makePK());
        Assert.assertTrue(t.makePK().equals("b622V123#############MKmoreThan15Cha20170223110922"));
    }
}
