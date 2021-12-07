package com.tjfintech.common.stableTest;

import com.tjfintech.common.*;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.Interface.Token;
import com.tjfintech.common.functionTest.mixTest.VerifyTests;
import com.tjfintech.common.functionTest.smartTokenTest.SmartTokenCommon;
import com.tjfintech.common.utils.GetTest;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class StableAutoTest {
    TestBuilder testBuilder = TestBuilder.getInstance();
    Store store = testBuilder.getStore();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();

    @BeforeClass
    public static void beforeConfig() throws Exception {

            BeforeCondition bf = new BeforeCondition();
            bf.updatePubPriKey();
//            bf.createSTAddresses();
//            bf.installSmartAccountContract("account_simple.wlang");

    }


    /**
     *  系统稳定性测试，是否丢交易测试
     */
    @Test
    public void SystemStableTest() throws Exception {

        String[] ids = getLedgerIDs();
        int ledgerNumber = ids.length;

        for (int j = 0; j < ledgerNumber; j++) {
            subLedger = ids[j];
            storeTest(ids[j]);
//            BeforeCondition bf = new BeforeCondition();
//            bf.createSTAddresses();
//            bf.installSmartAccountContract("account_simple.wlang");
        }

        int i = 0;
        int number = 2;  // 单链单次循环发送的交易数
        int loop = 10000; // 循环次数
        int total = loop * number; // 循环次数

        commonFunc.sdkCheckTxOrSleep(storeHash, utilsClass.sdkGetTxDetailType, SLEEPTIME);
        Thread.sleep(SLEEPTIME);
        long[] startTimestamps = getTimestamps(ids);
        int[] startHeights = getHeights(ids);


        while (i < loop) {

            for (int j = 0; j < ledgerNumber; j++) {
                storeTest(ids[j]);
            }

            for (int j = 0; j < ledgerNumber; j++) {
                priStoreTest(ids[j]);
            }

//            Thread.sleep(100);
//            for (int j = 0; j < ledgerNumber; j++) {
//                smartTokenTest(ids[j]);
//            }

            i++;
            log.info("i ===================== " + i);
            if ( i % 100 == 0) {
                utilsClass.setAndRestartPeer(PEER3IP); //10.1.5.161
            }

        }

        commonFunc.sdkCheckTxOrSleep(storeHash, utilsClass.sdkGetTxDetailType, SLEEPTIME);
        Thread.sleep(SLEEPTIME);

        long[] endTimestamps = getTimestamps(ids);
        int[] endHeights = getHeights(ids);

        int[] totalOnChains = new int[ids.length];

        for (i = 0; i < ids.length; i++) {
            subLedger = ids[i];
            totalOnChains[i] = commonFunc.CalculatetotalTxs(ids[i], startHeights[i], endHeights[i]);  // 上链交易数
        }

        int count = 0;

        for (int k = 0; k < ids.length; k++) {
            log.info("*****************************************************************");
            log.info("应用链ID：" + ids[k]);
            long timeDiff = (endTimestamps[k] - startTimestamps[k]) / 1000 / 60;   // 按分钟计时
            log.info("测试时长：" + timeDiff + "分钟");
            log.info("开始区块高度：" + startHeights[k]);
            log.info("结束区块高度：" + endHeights[k]);
            log.info("区块数：" + (endHeights[k] - startHeights[k]));
            log.info("发送交易总数：" + total);
            log.info("上链交易总数：" + totalOnChains[k]);
            if (total != totalOnChains[k]) {
                count++;
                log.info(ids[k] + " 发送交易数与上链交易数不一致!");
            }
            log.info("*****************************************************************");
        }

        assertEquals("发送交易数与上链交易数不一致", 0, count);

    }



    /**
     * 测试同步接口返回结果后立即查询数据
     */
    @Test
    public void SyncTest() throws Exception {
        syncFlag = true;
        int i = 0;
        int loop = 500; // 循环次数

        while (i < loop) {

            StoreAndQuery(subLedger);
            i++;

            if (i % 100 == 0) {
                log.info("========运行次数： " + i);
            }
        }
        syncFlag = false;
    }

    /**
     *  节点内存是否溢出测试
     *
     *  大数据存证稳定性测试
     */
    @Test
    public void OutOfMemoryTest() throws Exception {
        String[] ids = getLedgerIDs();
        int ledgerNumber = ids.length;

        for (int j = 0; j < ledgerNumber; j++) {
            subLedger = ids[j];
            storeTest(ids[j]);
        }

        int i = 0;
        int loop = 300; // 循环次数
        int[] total = new int[ids.length];

        commonFunc.sdkCheckTxOrSleep(storeHash, utilsClass.sdkGetTxDetailType, SLEEPTIME);
        Thread.sleep(SLEEPTIME);
        long[] startTimestamps = getTimestamps(ids);
        int[] startHeights = getHeights(ids);

        while (i < loop) {

            for (int j = 0; j < ledgerNumber; j++) {
//                if ( bigStoreTest(ids[j]) == 200 ){
                bigStoreTest(ids[j]);
                total[j]++;
//                }
                Thread.sleep(500);
            }
            i++;
        }

        commonFunc.sdkCheckTxOrSleep(storeHash, utilsClass.sdkGetTxDetailType, SLEEPTIME);
        Thread.sleep(SLEEPTIME);

        long[] endTimestamps = getTimestamps(ids);
        int[] endHeights = getHeights(ids);
        int[] totalOnChains = new int[ids.length];

        for (i = 0; i < ids.length; i++) {
            subLedger = ids[i];
            totalOnChains[i] = commonFunc.CalculatetotalTxs(ids[i], startHeights[i], endHeights[i]);  // 上链交易数
        }

        int count = 0;

        for (int k = 0; k < ids.length; k++) {
            log.info("*****************************************************************");
            log.info("应用链ID：" + ids[k]);
            long timeDiff = (endTimestamps[k] - startTimestamps[k]) / 1000 / 60;   // 按分钟计时
            log.info("测试时长：" + timeDiff + "分钟");
            log.info("开始区块高度：" + startHeights[k]);
            log.info("结束区块高度：" + endHeights[k]);
            log.info("区块数：" + (endHeights[k] - startHeights[k]));
            log.info("发送交易总数：" + total[k]);
            log.info("上链交易总数：" + totalOnChains[k]);
            if (total[k] != totalOnChains[k]) {
                count++;
                log.info(ids[k] + " 发送交易数与上链交易数不一致!");
            }
            log.info("*****************************************************************");
        }

        assertEquals("发送交易数与上链交易数不一致", 0, count);

    }

    /**
     * 事件稳定性测试
     */
    @Test
    public void EventStableTest() throws Exception {
        String[] ids = getLedgerIDs();
        int ledgerNumber = ids.length;

        for (int j = 0; j < ledgerNumber; j++) {
            subLedger = ids[j];
        }

        int i = 0;
        int loop = 500; // 循环次数
        int[] total = new int[ids.length];

        while (i < loop) {

            for (int j = 0; j < ledgerNumber; j++) {
                if ( storeTest(ids[j]) == 200 ){
                    total[j]++;
                };
            }
            i++;

            Thread.sleep(100);
            log.info("i ===================== " + i);
            if ( i % 10 == 0) {
                utilsClass.setAndRestartPeer(PEER4IP); //10.1.3.164
            }
        }

        commonFunc.sdkCheckTxOrSleep(storeHash, utilsClass.sdkGetTxDetailType, SLEEPTIME);
        Thread.sleep(SLEEPTIME);

        syncFlag = true;
        assertEquals("事件不稳定", storeTest(ids[0]), 200);
        syncFlag = false;
    }

    //----------------------------------------------------------------------------------------------------------------

    // 同步接口发送隐私存证并查询
    public void StoreAndQuery(String id) throws Exception {
//        String Data = "test11234567" + UtilsClass.Random(4);
//        String response = store.CreateStore(Data);

//        String storeHash = JSONObject.fromObject(response).getString("data");
//        String response2= store.GetTxDetail(storeHash);
//        assertThat(response2, containsString("200"));

        String Data = "test" + UtilsClass.Random(4);
        Map<String,Object>map=new HashMap<>();
        map.put("pubKey1",PUBKEY1);
        String response= store.CreatePrivateStore(Data,map);

        String hash = JSONObject.fromObject(response).getString("data");
        assertThat(store.GetStorePost(hash,PRIKEY1),containsString("200"));

    }

    // 普通存证
    public int bigStoreTest(String id) throws Exception {
        subLedger = id;
        String Data = UtilsClass.Random(10) + utilsClass.readStringFromFile(testDataPath
                + "store/bigsize3.txt");
        String response = store.CreateStore(Data);

        return JSONObject.fromObject(response).getInt("state");
    }

    // 普通存证
    public int storeTest(String id) throws Exception {
        subLedger = id;
        JSONObject fileInfo = new JSONObject();
        JSONObject data = new JSONObject();

        fileInfo.put("fileName", "201911041058.jpg");
        fileInfo.put("fileSize", "298KB");
        fileInfo.put("fileModel", "iphoneXR");
        fileInfo.put("fileLongitude", 123.45784545);
        fileInfo.put("fileStartTime", "1571901219");
        fileInfo.put("fileFormat", "jpg");
        fileInfo.put("fileLatitude", 31.25648);

        data.put("projectCode", UtilsClass.Random(10));
        data.put("waybillId", "1260");
        data.put("fileInfo", fileInfo);
        data.put("fileUrl", "/var/mobile/containers/data/111");
        data.put("projectName", "钰翔供应链测试006");
        data.put("projectId", "1234");
        data.put("fileType", "2");
        data.put("fileId", "");
        data.put("waybillNo", "y201911041032");
        String Data = data.toString();

        String response = store.CreateStore(Data);
        return JSONObject.fromObject(response).getInt("state");
    }

    // 隐私存证
    public int priStoreTest(String id) throws Exception {
        subLedger = id;
        String data = "Testcx-" + UtilsClass.Random(2);
        Map<String, Object> map = new HashMap<>();
        map.put("pubKeys", PUBKEY1);
        map.put("pubkeys", PUBKEY6);
        String response = store.CreatePrivateStore(data, map);

        return JSONObject.fromObject(response).getInt("state");

    }

      //获取应用链ID数组
    public String[] getLedgerIDs() throws Exception {

        JSONObject ledgers = JSONObject.fromObject(store.GetLedger());
        int number = ledgers.getJSONObject("data").getInt("number");

        String[] ids = new String[number];


        JSONArray ledgersInfo = ledgers.getJSONObject("data").getJSONArray("ledgers");

        for (int i = 0; i < number; i++) {
            String id = ledgersInfo.getJSONObject(i).getString("id");
            ids[i] = id;
        }

        return ids;

    }

    //获取应用链时间戳
    public long[] getTimestamps(String[] ids) throws Exception {

        long[] ts = new long[ids.length];

        for (int i = 0; i < ids.length; i++) {
            subLedger = ids[i];
            int start = Integer.parseInt(JSONObject.fromObject(store.GetHeight()).getString("data")); // 区块高度
            String timestamp = JSONObject.fromObject(store.GetBlockByHeight(start)).getJSONObject("data").getJSONObject("header").getString("timestamp");
            ts[i] = Long.parseLong(timestamp); // 开始时间
        }

        return ts;

    }

    //获取应用链区块高度
    public int[] getHeights(String[] ids) throws Exception {

        int[] hs = new int[ids.length];

        for (int i = 0; i < ids.length; i++) {
            subLedger = ids[i];
            hs[i] = Integer.parseInt(JSONObject.fromObject(store.GetHeight()).getString("data")); // 区块高度
        }

        return hs;

    }


}
