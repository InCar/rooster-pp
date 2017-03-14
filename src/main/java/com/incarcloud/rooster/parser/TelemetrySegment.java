package com.incarcloud.rooster.parser;

import com.incarcloud.rooster.gather.BigTableEntry;
import org.slf4j.*;

import java.time.ZonedDateTime;

public class TelemetrySegment {
    static TelemetrySegment create(TricheerAdasFlag flag){
        TelemetrySegment segment = null;

        if(flag == TricheerAdasFlag.Position) segment = new TelemetrySegmentPos();
        else if(flag == TricheerAdasFlag.MobileyeCarInfo) segment = new TelemetrySegmentCarInfo();
        else if(flag == TricheerAdasFlag.MobileyeStd) segment = new TelemetrySegmentMobileye();
        else if(flag == TricheerAdasFlag.MobileysTrafficSign) segment = new TelemetrySegmentTSR();
        else if(flag == TricheerAdasFlag.MobileysTrafficSignDecision) segment = new TelemetrySegmentTSRDecision();
        else s_logger.warn("尚不支持的数据({})", flag.name());

        return segment;
    }

    protected static Logger s_logger = LoggerFactory.getLogger(TelemetrySegment.class);

    public BigTableEntry prepareBigTableEntry(String vin, ZonedDateTime tm){
        return null;
    }

    // 返回有多个字节已经解析掉了
    public int resolve(byte[] buf, int start){
        return 0;
    }

    protected boolean _resolved;
    public boolean isResolved(){ return _resolved; }
}
