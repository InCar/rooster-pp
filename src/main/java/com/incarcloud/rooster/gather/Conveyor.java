package com.incarcloud.rooster.gather;

import com.alicloud.openservices.tablestore.*;
import com.alicloud.openservices.tablestore.model.*;
import com.aliyun.mns.client.*;
import com.aliyun.mns.model.*;
import com.incarcloud.rooster.parser.TricheerAdasCmd;
import com.incarcloud.rooster.parser.TricheerAdasPack;
import com.incarcloud.rooster.parser.TricheerAdasPackTelemetry;
import org.json.JSONObject;

import java.time.DateTimeException;
import java.util.*;
import org.slf4j.*;

/**
 * 从消息队列中取出消息，屯积到大数据存储区
 * MNS -> OTS
 * 扩展: 以后在面对企业应用时,用可以独立部署的其它中间件来替代
 *       比如,用redis或RocketMQ替代MNS,作为前置缓冲区;用HBase替代OTS作为大数据存储区
 *       从Conveyor抽取抽象基类,依据不同的中间件实现各自的派生类
 * 扩展: 当前仅实现了一个非常简单的阻塞式转运循环,以后需要扩展为非阻塞式,多线程并发模式
 *       现在每次仅从队列中取出一条消息,以后需要扩展为批处理方式,减少远程网络往返开销
 * 扩展: 增强稳定性,现在仅有非常有限的异常处理机制
 */
public class Conveyor {
    public Conveyor(){
        this._b64decoder = Base64.getDecoder();
    }

    // 设置来源队列
    public void configFrom(AliYunConfig configMNS){
        this._configMNS = configMNS;
    }

    // 设置存储
    public void configTo(AliYunConfig configOTS){
        this._configOTS = configOTS;
    }

    // 阻塞方式的简单转运循环
    public void blockedTransport(){
        CloudQueue queue = this.findQueue();
        CloudTopic topic = this.findTopic();
        SyncClient ots = this.findBigDataStorage();

        while(true){
            Message msg = queue.popMessage(30);
            if (msg != null) {
                try {
                    JSONObject body = new JSONObject(msg.getMessageBodyAsString());
                    String mark = body.getString("mark");
                    byte[] buf = this._b64decoder.decode(body.getString("data"));

                    // 按mark进行处理
                    List<BigTableEntry> listEntries = null;
                    if (mark.startsWith("tricheer-adas-1.")){
                        s_logger.warn("三旗ADAS数据包1.x版本已经不再支持", mark);
                    }
                    else if (mark.startsWith("tricheer-adas-")) {
                        // 三旗ADAS设备数据
                        TricheerAdasPack pack = TricheerAdasPack.parse(buf);
                        if (pack != null && pack.isResolved()){
                            s_logger.info("三旗ADAS数据包:{}", body.getString("data"));
                            listEntries = pack.prepareBigTableEntries();
                        }
                        else s_logger.warn("无法解析的三旗ADAS数据包[base64]:{}", body.getString("data"));
                    } else {
                        s_logger.warn("消息队列中不可识别的记号:{}", mark);
                    }

                    // INSERT INTO BIG TABLE
                    boolean bSaved = save(listEntries, ots);

                    // DELETE MESSAGE FROM MNS
                    queue.deleteMessage(msg.getReceiptHandle());

                    if(bSaved){
                        // save okay, then send notification
                        notify(listEntries, topic);
                    }
                }
                catch(DateTimeException ex){
                    s_logger.warn("丢弃消息,无效时间戳 {}", ex.toString());
                    queue.deleteMessage(msg.getReceiptHandle());
                }
                catch (Exception ex){
                    s_logger.warn("消费消息出错: {}", ex.getMessage());
                    queue.deleteMessage(msg.getReceiptHandle());
                    s_logger.warn("移除消息: {}", msg.getMessageBodyAsString());
                    throw ex;
                }
            }
        }
    }

    private CloudQueue findQueue(){
        CloudAccount account = new CloudAccount(this._configMNS.accessKeyId, this._configMNS.accessKeySecret,
                this._configMNS.mnsEndPoint);
        MNSClient client = account.getMNSClient();
        CloudQueue queue = client.getQueueRef(this._configMNS.mnsQueue);
        return queue;
    }

    private CloudTopic findTopic(){
        CloudAccount account = new CloudAccount(this._configMNS.accessKeyId, this._configMNS.accessKeySecret,
                this._configMNS.mnsEndPoint);
        MNSClient client = account.getMNSClient();
        CloudTopic topic = client.getTopicRef(this._configMNS.mnsTopic);
        return topic;
    }

    private SyncClient findBigDataStorage(){
        SyncClient client = new SyncClient(this._configOTS.otsEndPoint,
                this._configOTS.accessKeyId, this._configOTS.accessKeySecret, this._configOTS.otsInstance);
        return client;
    }

    private Boolean save(List<BigTableEntry> listEntries, SyncClient ots){
        if(listEntries == null || listEntries.isEmpty() || ots == null) return false;

        // telemetry
        BatchWriteRowRequest bat = new BatchWriteRowRequest();
        for(BigTableEntry entry: listEntries) {
            String key = entry.makePK();
            PrimaryKeyBuilder primaryKeyBuilder = PrimaryKeyBuilder.createPrimaryKeyBuilder();
            primaryKeyBuilder.addPrimaryKeyColumn("key", PrimaryKeyValue.fromString(key));
            PrimaryKey pk = primaryKeyBuilder.build();

            RowPutChange rowPutChange = new RowPutChange("telemetry", pk);
            rowPutChange.addColumn("data", ColumnValue.fromString(entry.getData()));

            bat.addRowChange(rowPutChange);
            s_logger.info("AliYun OTS Saving telemetry -> {}", key);

            // vehicle
            if (key.contains("TriAdas.Login")) {
                String keyVehicle = entry.makePKVehicle();
                primaryKeyBuilder = PrimaryKeyBuilder.createPrimaryKeyBuilder();
                primaryKeyBuilder.addPrimaryKeyColumn("key", PrimaryKeyValue.fromString(keyVehicle));
                PrimaryKey pkVehicle = primaryKeyBuilder.build();

                rowPutChange = new RowPutChange("vehicle", pkVehicle);
                rowPutChange.setCondition(new Condition(RowExistenceExpectation.EXPECT_NOT_EXIST));

                bat.addRowChange(rowPutChange);
                s_logger.info("AliYun OTS Saving vehicle -> {}", keyVehicle);
            }
        }
        if(!bat.isEmpty()){
                BatchWriteRowResponse resp = ots.batchWriteRow(bat);

                boolean bOkay = true;
                for (BatchWriteRowResponse.RowResult result : resp.getFailedRows()){
                    // OTSConditionCheckFail 这种错误直接忽略
                    if(!result.getError().getCode().equals("OTSConditionCheckFail")){
                        bOkay = false;
                        RowChange rowChange = bat.getRowChange(result.getTableName(), result.getIndex());
                        s_logger.error("AliYun OTS Saving Failed {} -> {} \n\t{}",
                                result.getTableName(),
                                rowChange.getPrimaryKey().getPrimaryKeyColumn(0).getValue().asString(),
                                result.getError().toString());
                    }
                }
                if(bOkay) s_logger.info("AliYun OTS Saved Successfully");
                return bOkay;
        }
        return false;
    }

    private void notify(List<BigTableEntry> listEntries, CloudTopic topic){
        for(BigTableEntry entry: listEntries) {
            String key = entry.makePK();

            TopicMessage msg = new Base64TopicMessage();
            msg.setMessageBody(key);
            msg.setMessageTag(entry.getMark());

            topic.asyncPublishMessage(msg, new AsyncCallback<TopicMessage>() {
                @Override
                public void onSuccess(TopicMessage result) {
                    // ignore
                }

                @Override
                public void onFail(Exception ex) {
                    s_logger.warn("Sent notifcation failed: {}", ex.getMessage());
                }
            });
        }
    }

    private AliYunConfig _configMNS;
    private AliYunConfig _configOTS;
    private Base64.Decoder _b64decoder;

    protected static Logger s_logger = LoggerFactory.getLogger(Conveyor.class);
}