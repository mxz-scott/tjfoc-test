package com.tjfintech.common.utils;

import com.alibaba.fastjson.JSON;
import com.tjfintech.common.Interface.Jml;
import com.tjfintech.common.TestBuilder;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.util.*;

@Slf4j
public class UtilsClassJml {
    TestBuilder testBuilder = TestBuilder.getInstance();
    Jml jml = testBuilder.getJml();

    public static String JMLADD = "http://dev-env.wutongchain.com:48090";

    public static String subjectType = "natural";//自然人
    public static String bankId = "";
    public static String fileHash = "sdds1c7a6305fa3b2a979bf81d760aec3fca866";
    public static String endTime = "2022-01-31 23:59:59";
    public static String requestId = "4a60b99d-fd0d-4dd9-a4c6-821eb14fa3bb";
    public static String personId = "";
    public static String personName = "";
    public static String purpose = "放款审查";


    /**
     * 新增查询用户 List
     */
    public static Map subject(String id, String name) {

        Map<String, Object> amountMap = new HashMap<>();
        amountMap.put("id", id);
        amountMap.put("name", name);

        return amountMap;
    }

    /**
     * 获取authId
     * @param response1
     * @return
     */
    public static String getValueByKey(String response1) {
        Map<String, Object> parse = JSON.parseObject(response1, Map.class);
        Object data = parse.get("data");
        Map<String, Object> dataMap = JSON.parseObject(data.toString(), Map.class);
        Object authId = dataMap.get("authId");

        return authId.toString();
    }

    /**
     * 获取txId
     * @param response1
     * @return
     */
    public static String gettxId(String response1) {
        Map<String, Object> parse = JSON.parseObject(response1, Map.class);
        Object data = parse.get("data");
        Map<String, Object> dataMap = JSON.parseObject(data.toString(), Map.class);
        Object authId = dataMap.get("txId");

        return authId.toString();
    }


    /**
     * 上传授信结果
     * @param id
     * @param name
     * @return
     */
    public static Map results(String bizOrderId, String status, String comments, String name, String id, int creditAmount, int duration, String interest, String product) {

        Map<String, Object> amountMap = new HashMap<>();
        amountMap.put("bizOrderId", bizOrderId);
        amountMap.put("status", status);
        amountMap.put("comments", comments);
        amountMap.put("name", name);
        amountMap.put("id", id);
        amountMap.put("creditAmount", creditAmount);
        amountMap.put("duration", duration);
        amountMap.put("interest", interest);
        amountMap.put("product", product);

        return amountMap;
    }

    public static String itemsId(String response2) {
        Map<String, Object> parse = JSON.parseObject(response2, Map.class);
        Object data = parse.get("items");
        Map<String, Object> dataMap = JSON.parseObject(data.toString(), Map.class);
        dataMap.put("\"totalItems\":9","\"errItems\":0");
        Object items = dataMap.get("identity");
        Map<String, Object> itemNameMap = JSON.parseObject(items.toString(), Map.class);
        itemNameMap.put("网格数据","itemValue");
        itemNameMap.put("户籍信息","itemValue");
        itemNameMap.put("婚姻登记","itemValue");
        itemNameMap.put("用气信息","itemValue");
        itemNameMap.put("用水信息","itemValue");
        itemNameMap.put("车辆登记信息","itemValue");
        itemNameMap.put("社保缴纳","itemValue");
        itemNameMap.put("房屋权属","itemValue");
        itemNameMap.put("严重失信","itemValue");

        return items.toString();
    }

}


