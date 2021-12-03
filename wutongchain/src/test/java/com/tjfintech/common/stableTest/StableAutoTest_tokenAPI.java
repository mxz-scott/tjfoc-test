package com.tjfintech.common.stableTest;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.Interface.Token;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.functionTest.mixTest.VerifyTests;
import com.tjfintech.common.functionTest.smartTokenTest.SmartTokenCommon;
import com.tjfintech.common.utils.GetTest;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.junit.Assert.assertEquals;


@Slf4j
public class StableAutoTest_tokenAPI {
    TestBuilder testBuilder = TestBuilder.getInstance();
    Token tokenModule = testBuilder.getToken();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    StableAutoTest sat = new StableAutoTest();
    SmartTokenCommon stc = new SmartTokenCommon();

    private static String tokenType;
    List<String>listurl = new ArrayList();

    @BeforeClass
    public static void beforeConfig() throws Exception {
        if (MULITADD2.isEmpty()) {
            BeforeCondition bf = new BeforeCondition();
            bf.updatePubPriKey();
            bf.createSTAddresses();
        }
        if(tokenAccount1.isEmpty()) {
            BeforeCondition beforeCondition = new BeforeCondition();
            beforeCondition.createTokenAccount();
            Thread.sleep(SLEEPTIME);
        }
    }


    /**
     * token api稳定性测试
     */
    @Test
    public void tokenStableTest() throws Exception {

        if (tokenAccount1.isEmpty()) {
            BeforeCondition beforeCondition = new BeforeCondition();
            beforeCondition.createTokenAccount();
            Thread.sleep(SLEEPTIME);
        }

        String[] ids = sat.getLedgerIDs();
        int ledgerNumber = ids.length;

        for (int j = 0; j < ledgerNumber; j++) {
            subLedger = ids[j];
            BeforeCondition bf = new BeforeCondition();
            bf.updatePubPriKey();
            bf.tokenAddIssueCollAddr();
        }

        int i = 0;
        int number = 1;  // 单链单次循环发送的交易数
        int loop = 500; // 循环次数
        int total = loop * number; // 循环次数

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType, SLEEPTIME);

        long[] startTimestamps = sat.getTimestamps(ids);
        int[] startHeights = sat.getHeights(ids);

        while (i < loop) {

            for (int j = 0; j < ledgerNumber; j++) {
                tokenIssueTest(ids[j]);
            }

            commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.tokenApiGetTxHashType),
                    utilsClass.tokenApiGetTxDetailTType, SLEEPTIME);

            i++;

        }

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType, SLEEPTIME);

        long[] endTimestamps = sat.getTimestamps(ids);
        int[] endHeights = sat.getHeights(ids);

        int count = 0;

        for (int k = 0; k < ids.length; k++) {
            log.info("*****************************************************************");
            int totalOnChain = commonFunc.CalculatetotalTxs(ids[k], startHeights[k], endHeights[k]);  // 上链交易数
            log.info("应用链ID：" + ids[k]);
            long timeDiff = (endTimestamps[k] - startTimestamps[k]) / 1000 / 60;   // 按分钟计时
            log.info("测试时长：" + timeDiff + "分钟");
            log.info("开始区块高度：" + startHeights[k]);
            log.info("结束区块高度：" + endHeights[k]);
            log.info("区块数：" + (endHeights[k] - startHeights[k]));
            log.info("发送交易总数：" + total);
            log.info("上链交易总数：" + totalOnChain);
            if (total != totalOnChain) {
                count++;
                log.error("交易丢了!");
                for (int M = 0; M < listurl.size(); M++){
                    String response = GetTest.doGet2(SDKADD + "/v1/gettxdetail" + "?" + listurl.get(M));
                    if ((JSONObject.fromObject(response).getString("state").equals("400"))){
                        log.error("#################交易丢了"+listurl.get(M));
                    }
                }
            }
            log.info("*****************************************************************");
        }

        assertEquals("交易丢了", 0, count);

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
    public void tokenIssueTest(String id) throws Exception {

        subLedger = id;
        tokenType = "tokenErr_" + UtilsClass.Random(8);
        //发行失败交易
        String issueResponse = tokenModule.tokenIssue(tokenAccount7, tokenAccount7, tokenType, "1000", "");
        assertEquals("200", JSONObject.fromObject(issueResponse).getString("state"));

        //发行成功交易
        tokenType = "tokenSoMU_" + UtilsClass.Random(8);
        issueResponse = tokenModule.tokenIssue(tokenAccount1, tokenAccount1, tokenType, "1000", "");
        assertEquals("200", JSONObject.fromObject(issueResponse).getString("state"));
        String issueHash = JSONObject.fromObject(issueResponse).getString("data");
        String issueHashUrl = URLEncoder.encode(issueHash);
        String url = "hash="+issueHashUrl +"&ledger="+subLedger;
        listurl.add(url);
        System.out.println(listurl);
    }





}
