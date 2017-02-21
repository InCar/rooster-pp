package com.incarcloud.rooster.gather;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AliYunConfig {

    @Value("${aliyun.accessKeyId}")
    String accessKeyId;

    @Value("${aliyun.accessKeySecret}")
    String accessKeySecret;

    @Value("${aliyun.MNS.endpoint}")
    String mnsEndPoint;

    @Value("${aliyun.MNS.queue}")
    String mnsQueue;

    @Value("${aliyun.OTS.endpoint}")
    String otsEndPoint;

    @Value("${aliyun.OTS.instance}")
    String otsInstance;
}
