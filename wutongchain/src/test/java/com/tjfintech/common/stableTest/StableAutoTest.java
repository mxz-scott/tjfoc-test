package com.tjfintech.common.stableTest;

import com.tjfintech.common.*;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.Interface.Token;
import com.tjfintech.common.functionTest.mixTest.VerifyTests;
import com.tjfintech.common.functionTest.smartTokenTest.SmartTokenCommon;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;


@Slf4j
public class StableAutoTest {
    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    MultiSign multiSign=testBuilder.getMultiSign();
    SoloSign soloSign=testBuilder.getSoloSign();
    Token tokenModule = testBuilder.getToken();
    UtilsClass utilsClass=new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    VerifyTests vt = new VerifyTests();
    SmartTokenCommon stc = new SmartTokenCommon();

    private static String tokenType;

    @BeforeClass
    public static void beforeConfig() throws Exception {
//        if (MULITADD2.isEmpty()) {
//            BeforeCondition bf = new BeforeCondition();
//            bf.updatePubPriKey();
//            bf.createSTAddresses();
//            bf.installSmartAccountContract("account_simple.wlang");
//        }
        if(tokenAccount1.isEmpty()) {
            BeforeCondition beforeCondition = new BeforeCondition();
            beforeCondition.createTokenAccount();
            Thread.sleep(SLEEPTIME);
        }
    }


    /**
     *  稳定性测试
     */
    @Test
    public  void stableTest()throws Exception{

//        String[] ids = {"ra0erdvwpd"};
        String[] ids = getLedgerIDs();
        int ledgerNumber = ids.length;

        for (int j = 0; j < ledgerNumber; j++) {
            subLedger = ids[j];
            storeTest(ids[j]);
            BeforeCondition bf = new BeforeCondition();
            bf.updatePubPriKey();
//            bf.createSTAddresses();
//            bf.installSmartAccountContract("account_simple.wlang");
        }

        int i = 0;
        int number = 2;  // 单链单次循环发送的交易数
        int loop = 100000 ; // 循环次数
        int total = loop * number ; // 循环次数

        commonFunc.sdkCheckTxOrSleep(storeHash,utilsClass.sdkGetTxDetailType,SLEEPTIME);
        Thread.sleep(SLEEPTIME);
        long[] startTimestamps = getTimestamps(ids);
        int[] startHeights = getHeights(ids);


        while( i < loop ){

            for (int j = 0; j < ledgerNumber; j++){
                storeTest(ids[j]);
            }

            for (int j = 0; j < ledgerNumber; j++){
                 priStoreTest(ids[j]);
            }

//            for (int j = 0; j < ledgerNumber; j++){
//                smartTokenTest(ids[j]);
//            }

           i++;

        }

        commonFunc.sdkCheckTxOrSleep(storeHash,utilsClass.sdkGetTxDetailType,SLEEPTIME);
        Thread.sleep(SLEEPTIME);

        long[] endTimestamps = getTimestamps(ids);
        int[] endHeights = getHeights(ids);

        int count = 0;

        for (int k = 0 ; k < ids.length; k++){
            log.info("*****************************************************************");
            int totalOnChain = commonFunc.CalculatetotalTxs(ids[k], startHeights[k], endHeights[k]);  // 上链交易数
            log.info("应用链ID：" + ids[k]);
            long timeDiff = (endTimestamps[k] - startTimestamps[k]) / 1000 / 60 ;   // 按分钟计时
            log.info("测试时长：" + timeDiff + "分钟");
            log.info("开始区块高度：" + startHeights[k]);
            log.info("结束区块高度：" + endHeights[k]);
            log.info("区块数：" + (endHeights[k] - startHeights[k]));
            log.info("发送交易总数：" + total );
            log.info("上链交易总数：" + totalOnChain);
            if (total != totalOnChain){
                count++;
                log.error("交易丢了!");
            }
            log.info("*****************************************************************");
        }

        assertEquals("交易丢了", 0,count);

    }

    /**
     *  token api稳定性测试
     */
    @Test
    public  void tokenStableTest()throws Exception{

//        String[] ids = {"ra0erdvwpd"};
        String[] ids = getLedgerIDs();
        int ledgerNumber = ids.length;

        for (int j = 0; j < ledgerNumber; j++) {
            subLedger = ids[j];
            BeforeCondition bf = new BeforeCondition();
            bf.updatePubPriKey();
            bf.tokenAddIssueCollAddr();
        }

        int i = 0;
        int number = 1;  // 单链单次循环发送的交易数
        int loop = 2 ; // 循环次数
        int total = loop * number ; // 循环次数

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        long[] startTimestamps = getTimestamps(ids);
        int[] startHeights = getHeights(ids);


        while( i < loop ){

            for (int j = 0; j < ledgerNumber; j++){
                tokenIssueTest(ids[j]);
            }
//
//            for (int j = 0; j < ledgerNumber; j++){
//                priStoreTest(ids[j]);
//            }

//            for (int j = 0; j < ledgerNumber; j++){
//                smartTokenTest(ids[j]);
//            }

            i++;

        }

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        long[] endTimestamps = getTimestamps(ids);
        int[] endHeights = getHeights(ids);

        int count = 0;

        for (int k = 0 ; k < ids.length; k++){
            log.info("*****************************************************************");
            int totalOnChain = commonFunc.CalculatetotalTxs(ids[k], startHeights[k], endHeights[k]);  // 上链交易数
            log.info("应用链ID：" + ids[k]);
            long timeDiff = (endTimestamps[k] - startTimestamps[k]) / 1000 / 60 ;   // 按分钟计时
            log.info("测试时长：" + timeDiff + "分钟");
            log.info("开始区块高度：" + startHeights[k]);
            log.info("结束区块高度：" + endHeights[k]);
            log.info("区块数：" + (endHeights[k] - startHeights[k]));
            log.info("发送交易总数：" + total );
            log.info("上链交易总数：" + totalOnChain);
            if (total != totalOnChain){
                count++;
                log.error("交易丢了!");
            }
            log.info("*****************************************************************");
        }

        assertEquals("交易丢了", 0,count);

    }

    // 普通存证
    public void storeTest(String id)throws Exception{
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
        data.put("projectName", "测试006");
        data.put("projectId", "1234");
        data.put("fileType", "2");
        data.put("fileId", "");
        data.put("waybillNo", "y201911041032");
        String Data = data.toString();

        String response= store.CreateStore(Data);
        assertThat(response, containsString("200"));
        assertThat(response,containsString("data"));

    }

    // 隐私存证
    public void priStoreTest(String id) throws Exception {
        subLedger = id;
        String data = "Testcx-" + UtilsClass.Random(2);
        Map<String,Object>map=new HashMap<>();
        map.put("pubKeys",PUBKEY1);
        map.put("pubkeys",PUBKEY6);
        String response = store.CreatePrivateStore(data,map);
        assertThat(response, containsString("200"));
        assertThat(response,containsString("data"));

    }

    /**
     * 多签正常流程-发行：签名：查询：转账：查询:回收：查询
     */
    public void smartTokenTest(String id) throws Exception {
        subLedger = id;
        //发行
        tokenType = stc.beforeConfigIssueNewToken("1000.25");

        //转让
        String transferData = "ADDRESS1 向 MULITADD4 转账10个" + tokenType;
        List<Map> payList = stc.smartConstructTokenList(ADDRESS1, "test", "10", null);
        List<Map> collList = stc.smartConstructTokenList(MULITADD4, "test", "10", null);
        String transferResp = stc.smartTransfer(tokenType, payList, collList, "", "", transferData);

        assertEquals("200", JSONObject.fromObject(transferResp).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType, SLEEPTIME);

        //回收
        String destroyData1 = "销毁 ADDRESS1 中的" + tokenType;
        String destroyData2 = "销毁 MULITADD4 中的" + tokenType;
        List<Map> payList1 = stc.smartConstructTokenList(ADDRESS1, "test", "990.25", null);
        List<Map> payList2 = stc.smartConstructTokenList(MULITADD4, "test", "10", null);

        String destroyResp1 = stc.smartDestroy(tokenType, payList1, "", destroyData1);
        String destroyResp2 = stc.smartDestroy(tokenType, payList2, "", destroyData2);

        assertEquals("200", JSONObject.fromObject(destroyResp1).getString("state"));
        assertEquals("200", JSONObject.fromObject(destroyResp2).getString("state"));

    }

    // token发行
    public void tokenIssueTest(String id)throws Exception{

        subLedger = id;
        tokenType = "tokenErr_"+ UtilsClass.Random(8);
        //发行失败交易
        String issueResponse = tokenModule.tokenIssue(tokenAccount7,tokenAccount7,tokenType,"1000","");
        assertEquals("200",JSONObject.fromObject(issueResponse).getString("state"));

        //发行成功交易
        tokenType = "tokenSoMU_"+ UtilsClass.Random(8);
        issueResponse = tokenModule.tokenIssue(tokenAccount1,tokenAccount1,tokenType,"1000","");
        assertEquals("200",JSONObject.fromObject(issueResponse).getString("state"));


    }


    //获取应用链ID数组
    public  String[] getLedgerIDs()throws Exception{

        JSONObject ledgers = JSONObject.fromObject(store.GetLedger());
        int number = ledgers.getJSONObject("data").getInt("number");

        String[] ids = new String[number];


        JSONArray ledgersInfo = ledgers.getJSONObject("data").getJSONArray("ledgers");

       for (int i = 0; i < number; i++){
           String id =  ledgersInfo.getJSONObject(i).getString("id");
           ids[i] = id;
       }

       return ids;

    }

    //获取应用链时间戳
    public  long[] getTimestamps(String[] ids)throws Exception{

        long[] ts = new long[ids.length];

        for (int i =0; i < ids.length; i++){
            subLedger = ids[i];
            int start = Integer.parseInt(JSONObject.fromObject(store.GetHeight()).getString("data")); // 区块高度
            String timestamp = JSONObject.fromObject(store.GetBlockByHeight(start)).getJSONObject("data").getJSONObject("header").getString("timestamp");
            ts[i] = Long.parseLong(timestamp); // 开始时间
        }

        return ts;

    }

    //获取应用链区块高度
    public  int[] getHeights(String[] ids)throws Exception{

        int[] hs = new int[ids.length];

        for (int i =0; i < ids.length; i++){
            subLedger = ids[i];
            hs[i] = Integer.parseInt(JSONObject.fromObject(store.GetHeight()).getString("data")); // 区块高度
         }

        return hs;

    }
}
