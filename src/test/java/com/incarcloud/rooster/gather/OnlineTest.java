package com.incarcloud.rooster.gather;

import com.alicloud.openservices.tablestore.SyncClient;
import com.alicloud.openservices.tablestore.model.*;
import com.incarcloud.rooster.BaseTest;
import com.incarcloud.rooster.parser.TricheerAdasPack;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=AliYunConfig.class)
@SpringBootTest
public class OnlineTest extends BaseTest {

    @Test
    public void FetchOTS(){
        // 从OTS上找回来没有完全解析的东东
        RangeRowQueryCriteria qryRange = new RangeRowQueryCriteria("telemetry");
        qryRange.setMaxVersions(1);
        qryRange.setLimit(5000);

        PrimaryKeyBuilder pkBuilder = PrimaryKeyBuilder.createPrimaryKeyBuilder();
        pkBuilder.addPrimaryKeyColumn("key", PrimaryKeyValue.fromString("0000"));
        qryRange.setInclusiveStartPrimaryKey(pkBuilder.build());

        pkBuilder = PrimaryKeyBuilder.createPrimaryKeyBuilder();
        pkBuilder.addPrimaryKeyColumn("key", PrimaryKeyValue.fromString("zzzz"));
        qryRange.setExclusiveEndPrimaryKey(pkBuilder.build());

        // 计数统计
        HashMap<String, Integer> stat = new HashMap<>();

        SyncClient client = findBigDataStorage();
        Base64.Decoder b64decoder = Base64.getDecoder();
        PrimaryKey pkNext = null;
        int i = 0;
        do{
            int j = 0;

            List<RowDeleteChange> listWaitToDel = new ArrayList<>();
            GetRangeResponse resp = client.getRange(new GetRangeRequest(qryRange));
            for(Row row:resp.getRows()){
                String key = row.getPrimaryKey().getPrimaryKeyColumn("key").getValue().asString();
                String data = row.getLatestColumn("data").getValue().asString();

                String shortKey = key.substring(0, 36);
                if(!stat.containsKey(shortKey)){
                    stat.put(shortKey, 1);
                }else{
                    stat.put(shortKey, stat.get(shortKey)+1);
                }

                // 尝试解析以前解不开的数据包
                if(key.contains("TriAdas-b64")){
                    byte[] buf = b64decoder.decode(data);
                    TricheerAdasPack pack = TricheerAdasPack.parse(buf);

                    if (pack != null && pack.isResolved()) {
                        // 可以解开
                        BigTableEntry bigEntry = pack.prepareBigTableEntry();
                        s_logger.info("{}: {}", bigEntry.makePK(), bigEntry.getData());
                        // 准备删除这些已经可以解开的
                        listWaitToDel.add(new RowDeleteChange("telemetry", row.getPrimaryKey()));

                        s_logger.info("Deleting {}", key);
                    }
                }

                j++;
            }

            String defTest = System.getProperty("test");
            if(defTest != null && defTest.startsWith("online-del")) {
                // 每批200个
                BatchWriteRowRequest bat = new BatchWriteRowRequest();
                for (RowDeleteChange change : listWaitToDel) {
                    bat.addRowChange(change);
                    if (bat.getRowsCount() >= 200) {
                        client.batchWriteRow(bat);
                        bat = new BatchWriteRowRequest();
                    }
                }
                if (bat.getRowsCount() > 0) client.batchWriteRow(bat);
            }
            else if(!listWaitToDel.isEmpty()){
                s_logger.info("忽略删除,如果需要添加 -Dtest=online-del 执行");
            }

            s_logger.info("Round {} {}", i++, j);

            pkNext = resp.getNextStartPrimaryKey();
            if(pkNext != null) qryRange.setInclusiveStartPrimaryKey(resp.getNextStartPrimaryKey());
        }
        while(pkNext != null);

        for(String key : stat.keySet()){
            s_logger.info("{} {}", key, stat.get(key));
        }
        s_logger.info("total: {}", stat.size());
    }

    private SyncClient findBigDataStorage(){
        if(s_client == null){
            s_client = new SyncClient(_aliYunConfig.otsEndPoint,
                    _aliYunConfig.accessKeyId, _aliYunConfig.accessKeySecret, _aliYunConfig.otsInstance);
        }
        return s_client;
    }

    @Autowired
    private AliYunConfig _aliYunConfig;

    private static SyncClient s_client;
}
