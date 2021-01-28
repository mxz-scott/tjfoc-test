package com.tjfintech.common.stableTest;

import com.tjfintech.common.*;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.functionTest.mixTest.VerifyTests;
import com.tjfintech.common.functionTest.smartTokenTest.SmartTokenCommon;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
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
    UtilsClass utilsClass=new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    VerifyTests vt = new VerifyTests();
    SmartTokenCommon stc = new SmartTokenCommon();

    private static String tokenType;
    @BeforeClass
    public static void beforeConfig() throws Exception {
        if (MULITADD2.isEmpty()) {
            BeforeCondition bf = new BeforeCondition();
            bf.updatePubPriKey();
            bf.createSTAddresses();
            bf.installSmartAccountContract("account_simple.wlang");
        }
        Thread.sleep(SLEEPTIME);
    }


    /**
     * 稳定性测试
     */
    @Test
    public  void stableTest()throws Exception{
        int i = 0;
        int total = 2500 * 6;
        int interval = 1000; //交易时间间隔

        int start = Integer.parseInt(JSONObject.fromObject(store.GetHeight()).getString("data"));
        String timestamp = JSONObject.fromObject(store.GetBlockByHeight(start)).getJSONObject("data").getJSONObject("header").getString("timestamp");
        long blkTimeStamp1 = Long.parseLong(timestamp);

        while( i < total ){

            storeTest();
            i++;

            priStoreTest();
            i++;

            smartTokenTest();
            i = i+ 4;
            Thread.sleep(interval);

        }

        commonFunc.sdkCheckTxOrSleep(storeHash,utilsClass.sdkGetTxDetailType,SHORTMEOUT);

        int end = Integer.parseInt(JSONObject.fromObject(store.GetHeight()).getString("data"));
        timestamp = JSONObject.fromObject(store.GetBlockByHeight(end)).getJSONObject("data").getJSONObject("header").getString("timestamp");
        long blkTimeStamp2 = Long.parseLong(timestamp);
        long timeDiff = (blkTimeStamp2 - blkTimeStamp1) / 1000 / 60;

        int totalOnChain = vt.CalculatetotalTxs(start, end);

        log.info("测试时长：" + timeDiff + "分钟");
        log.info("区块数：" + (end - start));
        log.info("发送交易数：" + total);
        log.info("链上交易数：" + totalOnChain);
        assertEquals("交易丢了", total,totalOnChain);

    }

    // 普通存证
    public void storeTest()throws Exception{
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
    public void priStoreTest() throws Exception {
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
    public void smartTokenTest() throws Exception {

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


}
