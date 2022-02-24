package com.tjfintech.common.utils;

import com.alibaba.fastjson.JSON;
import com.tjfintech.common.Interface.Shca;
import com.tjfintech.common.TestBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class UtilsClassShca {
    TestBuilder testBuilder = TestBuilder.getInstance();
    Shca sheca = testBuilder.getShca();
    public static String SHCAADD = "http://36.156.139.97:59092";
    public static String SHCAADD2= "http://36.156.139.97:59091";
    public static String SHCAADD3 = "http://117.48.158.99:58080";

    public static String id = "did:example:4Q9gaxppw6tpDQ4yGan1T378fB88";
    public static String vcId ="dyyid-001";
    public static String didJson = "{\\\"@context\\\":\\\"https://w3id.org/did/v1\\\",\\\"id\\\":\\\"did:example:4Q9gaxppw6tpDQ4yGan1T378fB88\\\",\\\"created\\\":\\\"2021-12-17T14:44:07Z\\\",\\\"updated\\\":\\\"2021-12-17T14:44:07Z\\\",\\\"publicKey\\\":[{\\\"id\\\":\\\"did:example:4Q9gaxppw6tpDQ4yGan1T378fBGi#key-1\\\",\\\"type\\\":\\\"EC\\\",\\\"publicKeyHex\\\":\\\"3059301306072a8648ce3d020106082a811ccf5501822d034200042d86ce03614405921f7ec14d11e2884af2958032f2a9232e792b69b3d199a35bf0518f4cda42c311a33c27274269c7c5e90afae4f9e8ce4920efeb44bbaba681\\\"}],\\\"authentication\\\":[\\\"did:example:4Q9gaxppw6tpDQ4yGan1T378fBGi#key-1\\\"],\\\"proof\\\":{\\\"type\\\":\\\"SM3WITHSM2\\\",\\\"creator\\\":\\\"did:example:4Q9gaxppw6tpDQ4yGan1T378fBGi#key-1\\\",\\\"signatureValue\\\":\\\"MEUCIQCJl3o4lGMU503bQ/arGKDKK2WDYf479Z+q0D3VY8hIwwIgClsj9zwUpqe88ReGS8Mkp2uwniP15Rom0FlYWTFcOVc=\\\"}}";
    public static String vcJson = "{\\\"@context\\\":[\\\"https://www.w3.org/2018/credentials/v1\\\",\\\"https://www.w3.org/2018/credentials/examples/v1\\\"],\\\"id\\\":\\\"did:shca:3npxAKr3qkAhtdyP7pHcQ3Cnfyyz_did:sheca-uat:2hRjHswx7GCMzQncf33hyregc2Db\\\",\\\"type\\\":[\\\"VerifiableCredential\\\",\\\"Test_type\\\"],\\\"issuer\\\":\\\"did:shca:3npxAKr3qkAhtdyP7pHcQ3Cnfyyz\\\",\\\"issuanceDate\\\":\\\"2022-02-14T10:32:29Z\\\",\\\"credentialSubject\\\":{\\\"name\\\":\\\"duyuyang\\\",\\\"id\\\":\\\"did:sheca-uat:2hRjHswx7GCMzQncf33hyregc2Db\\\"},\\\"proof\\\":{\\\"type\\\":\\\"SM3WITHSM2\\\",\\\"creator\\\":\\\"did:shca:3npxAKr3qkAhtdyP7pHcQ3Cnfyyz#key-1\\\",\\\"signatureValue\\\":\\\"MEUCIQC7j9FUBuOeYWZgx/C3N5w/0UJcA/08P9D+brKjuRWuMgIgHzAPOilIYdfTOeH5eddLSSv6CpeKYUQ261QVST58ZH4=\\\"}}";
    public static String applicantDid = "did:sheca-uat:2hRjHswx7GCMzQncf33hyregc2Db";
    public static String vcType = "Test_type";
    public static String name = "dyyid-001";
    public static String password = "Pass@7899";
    public static String pubKey = "3059301306072a8648ce3d020106082a8648ce3d03010703420004b4a148a7ab9c0c03abe581e014cb9f18659ef29ca63146e1fbd1941d2b747ec4548428710163de42f88299b14b27828423bcf5f70bb1ed5f41252eed8e11b545";
    public static String publicKeyId = "123456";
    public static String didid = "did:sheca-uat:5m2rkcTeWcz1FCX3jyW7vdRceqn";




    /*
    获取创建DID文档，上链hash
     */
    public static String getdata(String response1) {
        Map<String, Object> parse = JSON.parseObject(response1, Map.class);
        Object data = parse.get("data");
        return data.toString();
    }

    /**
     * 创建VC，vcSubject
     */
    public static Map vcSubject(String name) {
        Map<String, Object> amountMap = new HashMap<>();
        amountMap.put("name", name);
        return amountMap;
    }

}
