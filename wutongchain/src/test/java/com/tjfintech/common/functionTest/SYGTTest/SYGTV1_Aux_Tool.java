package com.tjfintech.common.functionTest.SYGTTest;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.SYGT;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassSYGT.*;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class SYGTV1_Aux_Tool {

    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    SYGT sygt = testBuilder.getSygt();
    SYGTCommonFunc sygtCF = new SYGTCommonFunc();

    /**
     * 查询场景下的可用资产 并全部执行下线 for web自动化测试使用
     * @throws Exception
     */

    @Test
    public void offlineAssetAllMember() throws Exception {
        subLedger = "aepnt01eop";
        String scene = "SH005";
        String curSDK1 = "http://121.229.39.12:38080";
        String curSDK2 = "http://121.229.47.197:38080";
        String curSDK3 = "http://121.229.44.152:38080";

        String response = "";
        SDKADD = curSDK1;
        response = sygt.SSAssetQuery(scene, "");
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        com.alibaba.fastjson.JSONArray dataList = com.alibaba.fastjson.JSONObject.parseArray(
                JSONObject.fromObject(response).getString("data"));
        if(response.contains("assetID")) {
            for (int i = 0; i < dataList.size(); i++) {
                String assetID = JSONObject.fromObject(dataList.getJSONObject(i)).getString("assetID");
                SDKADD = curSDK2;
                response = sygt.SSAssetOff(assetID);
                assertEquals("200", JSONObject.fromObject(response).getString("state"));

                SDKADD = curSDK3;
                response = sygt.SSAssetOff(assetID);
                assertEquals("200", JSONObject.fromObject(response).getString("state"));
            }
        }


        SDKADD = curSDK2;
        response = sygt.SSAssetQuery(scene, "");
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        dataList = com.alibaba.fastjson.JSONObject.parseArray(
                JSONObject.fromObject(response).getString("data"));
        if(response.contains("assetID")) {
            for (int i = 0; i < dataList.size(); i++) {
                String assetID = JSONObject.fromObject(dataList.getJSONObject(i)).getString("assetID");
                SDKADD = curSDK1;
                response = sygt.SSAssetOff(assetID);
                assertEquals("200", JSONObject.fromObject(response).getString("state"));

                SDKADD = curSDK3;
                response = sygt.SSAssetOff(assetID);
                assertEquals("200", JSONObject.fromObject(response).getString("state"));
            }
        }

        SDKADD = curSDK3;
        response = sygt.SSAssetQuery(scene, "");
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        dataList = com.alibaba.fastjson.JSONObject.parseArray(
                JSONObject.fromObject(response).getString("data"));
        if(response.contains("assetID")) {
            for (int i = 0; i < dataList.size(); i++) {
                String assetID = JSONObject.fromObject(dataList.getJSONObject(i)).getString("assetID");
                SDKADD = curSDK1;
                response = sygt.SSAssetOff(assetID);
                assertEquals("200", JSONObject.fromObject(response).getString("state"));

                SDKADD = curSDK2;
                response = sygt.SSAssetOff(assetID);
                assertEquals("200", JSONObject.fromObject(response).getString("state"));
            }
        }

        SDKADD = curSDK1;
        response = sygt.SSAssetQuery(scene, "");
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals("null", JSONObject.fromObject(response).getString("data"));


        SDKADD = curSDK2;
        response = sygt.SSAssetQuery(scene, "");
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals("null", JSONObject.fromObject(response).getString("data"));


        SDKADD = curSDK3;
        response = sygt.SSAssetQuery(scene, "");
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals("null", JSONObject.fromObject(response).getString("data"));

    }

    /**
     * 查询场景下的可用资产 并全部执行下线 for web自动化测试使用
     * @throws Exception
     */

    @Test
    public void checkAvailableInfoMatchQueryResult() throws Exception {
        subLedger = "aepnt01eop";
        String scene = "SH004";
        String curSDK1 = "http://121.229.39.12:38080";
        String curSDK2 = "http://121.229.47.197:38080";
        String curSDK3 = "http://121.229.44.152:38080";

        String response = "";
        SDKADD = curSDK1;
        checkSameResult(scene);

        SDKADD = curSDK2;
        checkSameResult(scene);

        SDKADD = curSDK3;
        checkSameResult(scene);

    }

    public void checkSameResult(String scene)throws Exception {
        String response = "";
        response = sygt.SSAssetQuery(scene, "");
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        com.alibaba.fastjson.JSONArray dataList = com.alibaba.fastjson.JSONObject.parseArray(
                JSONObject.fromObject(response).getString("data"));

        Map<String, Integer> queryIDNo = new HashMap();

        if (response.contains("assetID")) {
            for (int i = 0; i < dataList.size(); i++) {
                String json = dataList.getJSONObject(i).toString();
                String assetID = JSONObject.fromObject(json).getString("assetID");

                int assetNo = StringUtils.countOccurrencesOf(json, "assetID");
                int authIdNo = StringUtils.countOccurrencesOf(json, "AuthId");
                int serviceIdNo = StringUtils.countOccurrencesOf(json, "ServiceId");
                if(assetNo > 0 && authIdNo > 0 && serviceIdNo > 0) {
                    if (assetNo == authIdNo) {
                        //单个查询字段场景
                        assertEquals("确认一致 数据资产数 授权数 服务数", assetNo, serviceIdNo);
                        log.info("确认一致 数据资产数 授权数 服务数");
                    } else {
                        //多个查询字段场景
                        assertEquals("确认一致 授权数 服务数", authIdNo, serviceIdNo);
                        log.info("确认一致 授权数 服务数");
                    }
                }
                else if(serviceIdNo == 0){
                    log.info(json);
                    continue;
                }

                com.alibaba.fastjson.JSONArray queryIDList = com.alibaba.fastjson.JSONObject.parseArray(
                        JSONObject.fromObject(json).getString("ServiceList"));
                for (int j = 0; j < queryIDList.size(); j++) {
                    String queryID = JSONObject.fromObject(queryIDList.getJSONObject(j)).getString("QueryId");
                    log.info(queryID);
                    if (queryIDNo.containsKey(queryID)) {
                        int curNo = queryIDNo.get(queryID);
                        curNo++;
                        queryIDNo.put(queryID, curNo);
                    } else {
                        queryIDNo.put(queryID, 1);
                    }
                }
            }
        }


        Iterator iter = queryIDNo.keySet().iterator();
        while (iter.hasNext()) {
            Object key = iter.next();

            //匿踪查询聚合结果返回确认
            Map queryIDValue = new HashMap();
            queryIDValue.put(key.toString(), "ABC0001");
            response = sygt.SSSafeQueryDo(scene, "", queryIDValue);
            int queryAssetNo = StringUtils.countOccurrencesOf(response, "result");
            assertEquals("确认一致 被授权的资产数和查询返回结果数", queryIDNo.get(key).intValue(), queryAssetNo);

        }


    }

}
