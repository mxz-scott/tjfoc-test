package com.tjfintech.common.functionTest.smartTokenTest;


import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CertTool;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.functionTest.contract.WVMContractTest;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.codec.binary.Hex;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

//import static com.tjfintech.common.functionTest.store.StoreTest.SLEEPTIME;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class smtMultiTest {
    TestBuilder testBuilder= TestBuilder.getInstance();
    MultiSign multiSign =testBuilder.getMultiSign();
    SoloSign soloSign=testBuilder.getSoloSign();
    UtilsClass utilsClass=new UtilsClass();
    private static String tokenType;
    private static String tokenType2;

    private static String issueAmount1;
    private static String issueAmount2;

    private static String actualAmount1;
    private static String actualAmount2;

    CommonFunc commonFunc = new CommonFunc();
    CertTool certTool = new CertTool();

    String constFileName = "wvm\\account_simple.wlang";
    String contractFileName = "wvm\\account_simple.wlang";
    String HQuotaFileName = "wvm\\account_simple_HQuota.wlang";


    @BeforeClass
    public static void beforeClass() throws Exception {
        BeforeCondition bf = new BeforeCondition();
        bf.updatePubPriKey();
        bf.createAddresses();
    }

//    @Before
    @Test
    public void beforeConfig() throws Exception {

        //安装smart token定制化合约
        installSmartAccountContract(contractFileName);


        issueAmount1 = "10000.12345678912345";
        issueAmount2 = "20000.876543212345";

        if (UtilsClass.PRECISION == 10) {
            actualAmount1 = "10000.1234567891";
            actualAmount2 = "20000.8765432123";
        }else {
            actualAmount1 = "10000.123456";
            actualAmount2 = "20000.876543";
        }

        double timeStampNow = System.currentTimeMillis();
        BigDecimal deadline = new BigDecimal(timeStampNow + 12356789);

        List<Map>list = utilsClass.smartConstuctIssueToList(IMPPUTIONADD,actualAmount1);


        log.info("发行两种token");
        tokenType = "SOLOTC-"+UtilsClass.Random(6);
        String issueResp = smartIssueToken(tokenType,deadline,list);
        assertEquals("200",JSONObject.fromObject(issueResp).getString("state"));

        list.clear();
        list = utilsClass.smartConstuctIssueToList(IMPPUTIONADD,actualAmount2);
        tokenType2 = "SOLOTC-"+UtilsClass.Random(6);
        String issueResp2 = smartIssueToken(tokenType2,deadline,list);
        assertEquals("200",JSONObject.fromObject(issueResp2).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(issueResp,utilsClass.sdkGetTxHashType21),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        log.info("查询归集地址中两种token余额");
        String queryBalance = multiSign.SmartGetBalanceByAddr(IMPPUTIONADD, "");
        assertEquals(actualAmount1,JSONObject.fromObject(queryBalance).getJSONObject("data").getJSONObject("detail").getString(tokenType));
        assertEquals(actualAmount1,JSONObject.fromObject(queryBalance).getJSONObject("data").getJSONObject("detail").getString(tokenType));

        assertEquals("200",JSONObject.fromObject(queryBalance).getString("state"));
    }


    /**
     * Tc03多签正常流程-发币：签名：查询：转账：查询:回收：查询
     *
     */
    @Test
    public void TC03_multiProgress() throws Exception {
        String transferData = "归集地址向" + MULITADD4 + "转账10个" + tokenType;
        log.info(transferData);
        List<Map>list=utilsClass.constructToken(MULITADD4,tokenType,"10");
        log.info(transferData);
        String transferInfo= multiSign.SmartTransfer(IMPPUTIONADD,PRIKEY4,"",list,transferData,"");
        Thread.sleep(SLEEPTIME);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);


        assertThat(transferInfo, containsString("200"));

        String amount1;

        if (UtilsClass.PRECISION == 10) {
            amount1 = "990.1234567891";
        }else {
            amount1 = "990.123456";
        }

        log.info("查询归集地址跟MULITADD4余额，判断转账是否成功");
        String queryInfo = multiSign.SmartGetBalanceByAddr(IMPPUTIONADD, tokenType);
        String queryInfo2 = multiSign.SmartGetBalanceByAddr(MULITADD4,  tokenType);
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
        assertEquals(amount1,JSONObject.fromObject(queryInfo).getJSONObject("data").getString("total"));
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("state"));
        assertEquals("10",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString("total"));

        log.info("回收归集地址跟MULITADD4的新发token");
        String recycleInfo = multiSign.SmartRecyle(IMPPUTIONADD, PRIKEY4, "",tokenType, amount1,"");
        String recycleInfo2 = multiSign.SmartRecyle(MULITADD4, PRIKEY1,"", tokenType, "10","");
        assertEquals("200",JSONObject.fromObject(recycleInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("state"));


        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);


        log.info("查询回收后账户余额是否为0");
        String queryInfo3 = multiSign.SmartGetBalanceByAddr(IMPPUTIONADD,  tokenType);
        String queryInfo4 = multiSign.SmartGetBalanceByAddr(MULITADD4,tokenType);
        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("state"));
        assertEquals("0",JSONObject.fromObject(queryInfo3).getJSONObject("data").getString("total"));
        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("state"));
        assertEquals("0",JSONObject.fromObject(queryInfo4).getJSONObject("data").getString("total"));


    }

//
//    /**
//     * 精度测试
//     *
//     */
//    @Test
//    public void TC03_PrecisionTest() throws Exception {
//
//
//        String transferData = "归集地址向" + MULITADD4 + "转账" + tokenType;
//        log.info(transferData);
//        List<Map>list=utilsClass.constructToken(MULITADD4,tokenType,issueAmount1);
//        log.info(transferData);
//        String transferInfo= SmartTransfer(IMPPUTIONADD,PRIKEY4,"", transferData, IMPPUTIONADD,list);
//
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
//                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//
//
//        assertThat(transferInfo, containsString("200"));
//
//        String amount1;
//
//        if (UtilsClass.PRECISION == 10) {
//            amount1 = "1000.1234567891";
//        }else {
//            amount1 = "1000.123456";
//        }
//
//        log.info("查询归集地址跟MULITADD4余额，判断转账是否成功");
//        String queryInfo = multiSign.SmartGetBalanceByAddr(IMPPUTIONADD,  tokenType);
//        String queryInfo2 = multiSign.SmartGetBalanceByAddr(MULITADD4, tokenType);
//        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
//        assertEquals("0",JSONObject.fromObject(queryInfo).getJSONObject("data").getString("total"));
//        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("state"));
//        assertEquals(amount1,JSONObject.fromObject(queryInfo2).getJSONObject("data").getString("total"));
//
//        log.info("回收归集地址跟MULITADD4的新发token");
//        String recycleInfo2 = multiSign.SmartRecyle(MULITADD4, PRIKEY1, tokenType, issueAmount1);
//        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("state"));
//
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
//                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//
//
//        log.info("查询回收后账户余额是否为0");
//        String queryInfo3 = multiSign.SmartGetBalanceByAddr(IMPPUTIONADD,  tokenType);
//        String queryInfo4 = multiSign.SmartGetBalanceByAddr(MULITADD4, tokenType);
//        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("state"));
//        assertEquals("0",JSONObject.fromObject(queryInfo3).getJSONObject("data").getString("total"));
//        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("state"));
//        assertEquals("0",JSONObject.fromObject(queryInfo4).getJSONObject("data").getString("total"));
//
//        String queryInfo5 = multiSign.QueryZero(tokenType);
//        assertEquals(amount1,JSONObject.fromObject(queryInfo5).getJSONObject("data").getJSONObject("detail").getString(tokenType));
//
//    }
//
//    /**
//     * 小数量发行
//     *
//     */
//    @Test
//    public void TC_MultiMinIssue()throws Exception{
//        String minToken = "TxTypeMulMin-" + UtilsClass.Random(6);
//        String minData = "多签" + IMPPUTIONADD + "发行token " + minToken;
//        String minAmount = "";
//
//        if (UtilsClass.PRECISION == 10) {
//            minAmount = "0.0000000001";
//        }else {
//            minAmount = "0.000001";
//        }
//        log.info(minData);
//
//        log.info("多签发行小数量token");
//        String response = multiSign.issueTokenCarryPri(IMPPUTIONADD,minToken,minAmount,PRIKEY4,minData);
//
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
//                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//
//
//        log.info("查询归集地址中token余额");
//        String response1 = multiSign.SmartGetBalanceByAddr(IMPPUTIONADD,minToken);
//        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
//        assertEquals(minAmount,JSONObject.fromObject(response1).getJSONObject("data").getString("total"));
//    }
//
//
//    /**
//     * 小数量转账、回收测试
//     *
//     */
//    @Test
//    public void TC_MultiMiniTest() throws Exception {
//        String transferData = "归集地址向" + MULITADD4 + "转账" + tokenType;
//        log.info(transferData);
//        List<Map>list=utilsClass.constructToken(MULITADD4,tokenType,"0.02");
//        log.info(transferData);
//        String transferInfo= SmartTransfer(IMPPUTIONADD,PRIKEY4,"", transferData, IMPPUTIONADD,list);
//        assertThat(transferInfo, containsString("200"));
//
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
//                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//
//
//        String amount1;
//
//        if (UtilsClass.PRECISION == 10) {
//            amount1 = "1000.1034567891";
//        }else {
//            amount1 = "1000.103456";
//        }
//
//        log.info("查询归集地址跟MULITADD4余额，判断转账是否成功");
//        String queryInfo = multiSign.SmartGetBalanceByAddr(IMPPUTIONADD,  tokenType);
//        String queryInfo2 = multiSign.SmartGetBalanceByAddr(MULITADD4,  tokenType);
//        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
//        assertEquals(amount1,JSONObject.fromObject(queryInfo).getJSONObject("data").getString("total"));
//        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("state"));
//        assertEquals("0.02",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString("total"));
//
//        log.info("回收MULITADD4的新发token");
//        String recycleInfo2 = multiSign.SmartRecyle(MULITADD4, PRIKEY1, tokenType, "0.01");
//        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("state"));
//
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
//                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//
//
//        log.info("查询回收后账户余额是否为0.01");
//        String queryInfo3 = multiSign.SmartGetBalanceByAddr(IMPPUTIONADD, tokenType);
//        String queryInfo4 = multiSign.SmartGetBalanceByAddr(MULITADD4, tokenType);
//        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("state"));
//        assertEquals(amount1,JSONObject.fromObject(queryInfo3).getJSONObject("data").getString("total"));
//        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("state"));
//        assertEquals("0.01",JSONObject.fromObject(queryInfo4).getJSONObject("data").getString("total"));
//
//        String queryInfo5 = multiSign.QueryZero(tokenType);
//        assertEquals("0.01",JSONObject.fromObject(queryInfo5).getJSONObject("data").getJSONObject("detail").getString(tokenType));
//
//    }
//
//
//    /**
//     * Tc024锁定后转账:
//     *
//     */
//    @Test
//    public void TC024_TransferAfterFrozen() throws Exception {
//
//        //20190411增加锁定步骤后进行转账
//        log.info("锁定待转账Token: "+tokenType);
//        String resp=multiSign.freezeToken(tokenType);
//
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType21),
//                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//
//        log.info("查询归集地址中两种token余额");
//        String response1 = multiSign.SmartGetBalanceByAddr(IMPPUTIONADD,  tokenType);
//        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
//        assertEquals(actualAmount1,JSONObject.fromObject(response1).getJSONObject("data").getString("total"));
//
//        String transferData = "归集地址向MULITADD4转账10个" + tokenType;
//        List<Map>list=utilsClass.constructToken(MULITADD4,tokenType,"10");
//        log.info(transferData);
//
//        String transferInfo= SmartTransfer(IMPPUTIONADD,PRIKEY4,"", transferData, IMPPUTIONADD,list);//相同币种
//        assertEquals("500",JSONObject.fromObject(transferInfo).getString("state"));
//
//
////        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
////                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//
//
//        log.info("查询余额判断转账是否成功");
//        String queryInfo= multiSign.SmartGetBalanceByAddr(MULITADD4,tokenType);
//        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
//        assertEquals("0",JSONObject.fromObject(queryInfo).getJSONObject("data").getString("total"));
//
//        log.info("解除锁定待转账Token: "+tokenType);
//        String resp1=multiSign.recoverFrozenToken(tokenType);
//
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType21),
//                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//
//
//        log.info("查询归集地址中两种token余额");
//        response1 = multiSign.SmartGetBalanceByAddr(IMPPUTIONADD, tokenType);
//        String response2 = multiSign.SmartGetBalanceByAddr(IMPPUTIONADD, tokenType2);
//        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
//        assertEquals(actualAmount1,JSONObject.fromObject(response1).getJSONObject("data").getString("total"));
//        assertEquals("200",JSONObject.fromObject(response2).getString("state"));
//        assertEquals(actualAmount2,JSONObject.fromObject(response2).getJSONObject("data").getString("total"));
//
//        transferData = "归集地址向" + "MULITADD4" + "转账10个" + tokenType+"归集地址向" + "MULITADD4" + "转账10个" + tokenType;
//        list=utilsClass.constructToken(MULITADD4,tokenType,"10");
//        List<Map>list2=utilsClass.constructToken(MULITADD5,tokenType2,"10",list);
//        List<Map>list3=utilsClass.constructToken(MULITADD5,tokenType,"10",list);
//        log.info(transferData);
//        transferInfo= SmartTransfer(IMPPUTIONADD,PRIKEY4,"", transferData, IMPPUTIONADD,list2);//不同币种
//
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
//                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//
//
//        String transferInfo2= SmartTransfer(IMPPUTIONADD,PRIKEY4,"", transferData, IMPPUTIONADD,list3);//相同币种
//        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
//        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("state"));
//
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
//                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//
//
//        log.info("查询余额判断转账是否成功");
//        queryInfo= multiSign.SmartGetBalanceByAddr(MULITADD4,tokenType);
//        String queryInfo2= multiSign.SmartGetBalanceByAddr(MULITADD5,tokenType2);
//        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
//        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("state"));
//        assertEquals("20",JSONObject.fromObject(queryInfo).getJSONObject("data").getString("total"));
//        assertEquals("10",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString("total"));
//
//        multiSign.SmartGetBalanceByAddr(IMPPUTIONADD,tokenType);
//        multiSign.SmartGetBalanceByAddr(IMPPUTIONADD, tokenType2);
//
//        String amount1, amount2;
//
//        if (UtilsClass.PRECISION == 10) {
//            amount1 = "970.1234567891";
//            amount2 = "990.8765432123";
//        }else {
//            amount1 = "970.123456";
//            amount2 = "990.876543";
//        }
//
//
//        log.info("回收Token");
//        String recycleInfo = multiSign.SmartRecyle(IMPPUTIONADD, PRIKEY4, tokenType, amount1);
//        String recycleInfo2 = multiSign.SmartRecyle(IMPPUTIONADD, PRIKEY4, tokenType2, amount2);
//        String recycleInfo3 = multiSign.SmartRecyle(MULITADD4, PRIKEY1, tokenType, "20");
//        String recycleInfo4 = multiSign.SmartRecyle(MULITADD5, PRIKEY1, tokenType2, "10");
//        String recycleInfo5 = multiSign.SmartRecyle(MULITADD5, PRIKEY1, tokenType, "10");
//
//        assertEquals("200",JSONObject.fromObject(recycleInfo).getString("state"));
//        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("state"));
//        assertEquals("200",JSONObject.fromObject(recycleInfo3).getString("state"));
//        assertEquals("200",JSONObject.fromObject(recycleInfo4).getString("state"));
//        assertEquals("200",JSONObject.fromObject(recycleInfo5).getString("state"));
//
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
//                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(recycleInfo2,utilsClass.sdkGetTxHashType20),
//                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//
//
//        log.info("查询余额判断回收成功与否");
//        String queryInfo3= multiSign.SmartGetBalanceByAddr(MULITADD4,tokenType);
//        String queryInfo4= multiSign.SmartGetBalanceByAddr(MULITADD5,tokenType2);
//        String queryInfo5= multiSign.SmartGetBalanceByAddr(MULITADD5,tokenType);
//
//        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("state"));
//        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("state"));
//        assertEquals("200",JSONObject.fromObject(queryInfo5).getString("state"));
//
//        assertEquals("0",JSONObject.fromObject(queryInfo3).getJSONObject("data").getString("total"));
//        assertEquals("0",JSONObject.fromObject(queryInfo4).getJSONObject("data").getString("total"));
//        assertEquals("0",JSONObject.fromObject(queryInfo5).getJSONObject("data").getString("total"));
//
//    }
//
//    /**
//     *TC19归集地址向两个多签地址转账
//     * 发两种币-查询归集地址-转账两个地址不同token-查询-回收-查询
//     * @throws Exception
//     */
//     @Test
//     public void TC19_transferMulti()throws  Exception{
//
//         log.info("查询归集地址中两种token余额");
//         String response1 = multiSign.SmartGetBalanceByAddr(IMPPUTIONADD,tokenType);
//         String response2 = multiSign.SmartGetBalanceByAddr(IMPPUTIONADD,tokenType2);
//         assertEquals("200",JSONObject.fromObject(response1).getString("state"));
//         assertEquals("200",JSONObject.fromObject(response2).getString("state"));
//         assertEquals(actualAmount1,JSONObject.fromObject(response1).getJSONObject("data").getString("total"));
//         assertEquals(actualAmount2,JSONObject.fromObject(response2).getJSONObject("data").getString("total"));
//
//         String transferData = "归集地址向" + "MULITADD4" + "转账10个" + tokenType+"归集地址向" + "MULITADD4" + "转账10个" + tokenType;
//         List<Map>list=utilsClass.constructToken(MULITADD4,tokenType,"10");
//         List<Map>list2=utilsClass.constructToken(MULITADD5,tokenType2,"10",list);
//         List<Map>list3=utilsClass.constructToken(MULITADD5,tokenType,"10",list);
//         log.info(transferData);
//         String transferInfo= SmartTransfer(IMPPUTIONADD,PRIKEY4,"", transferData, IMPPUTIONADD,list2);//不同币种
//
//         commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
//                 utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//
//
//         String transferInfo2= SmartTransfer(IMPPUTIONADD,PRIKEY4,"", transferData, IMPPUTIONADD,list3);//相同币种
//         assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
//         assertEquals("200",JSONObject.fromObject(transferInfo2).getString("state"));
//
//         commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
//                 utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//
//
//         log.info("查询余额判断转账是否成功");
//         String queryInfo= multiSign.SmartGetBalanceByAddr(MULITADD4,tokenType);
//         String queryInfo2= multiSign.SmartGetBalanceByAddr(MULITADD5,tokenType2);
//         assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
//         assertEquals("200",JSONObject.fromObject(queryInfo2).getString("state"));
//         assertEquals("20",JSONObject.fromObject(queryInfo).getJSONObject("data").getString("total"));
//         assertEquals("10",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString("total"));
//
//         multiSign.SmartGetBalanceByAddr(IMPPUTIONADD,tokenType);
//         multiSign.SmartGetBalanceByAddr(IMPPUTIONADD,tokenType2);
//
//         String amount1, amount2;
//
//         if (UtilsClass.PRECISION == 10) {
//             amount1 = "970.1234567891";
//             amount2 = "990.8765432123";
//         }else {
//             amount1 = "970.123456";
//             amount2 = "990.876543";
//         }
//
//         log.info("回收Token");
//         String recycleInfo = multiSign.SmartRecyle(IMPPUTIONADD, PRIKEY4, tokenType, amount1);
//         String recycleInfo2 = multiSign.SmartRecyle(IMPPUTIONADD, PRIKEY4, tokenType2, amount2);
//         String recycleInfo3 = multiSign.SmartRecyle(MULITADD4, PRIKEY1, tokenType, "20");
//         String recycleInfo4 = multiSign.SmartRecyle(MULITADD5, PRIKEY1, tokenType2, "10");
//         String recycleInfo5 = multiSign.SmartRecyle(MULITADD5, PRIKEY1, tokenType, "10");
//
//         assertEquals("200",JSONObject.fromObject(recycleInfo).getString("state"));
//         assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("state"));
//         assertEquals("200",JSONObject.fromObject(recycleInfo3).getString("state"));
//         assertEquals("200",JSONObject.fromObject(recycleInfo4).getString("state"));
//         assertEquals("200",JSONObject.fromObject(recycleInfo5).getString("state"));
//
//         commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(recycleInfo2,utilsClass.sdkGetTxHashType20),
//                 utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//         commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
//                 utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//
//
//         log.info("查询余额判断回收成功与否");
//         String queryInfo3= multiSign.SmartGetBalanceByAddr(MULITADD4,tokenType);
//         String queryInfo4= multiSign.SmartGetBalanceByAddr(MULITADD5,tokenType2);
//         String queryInfo5= multiSign.SmartGetBalanceByAddr(MULITADD5,tokenType);
//         assertEquals("200",JSONObject.fromObject(queryInfo3).getString("state"));
//         assertEquals("200",JSONObject.fromObject(queryInfo4).getString("state"));
//         assertEquals("200",JSONObject.fromObject(queryInfo5).getString("state"));
//         assertEquals("0",JSONObject.fromObject(queryInfo3).getJSONObject("data").getString("total"));
//         assertEquals("0",JSONObject.fromObject(queryInfo4).getJSONObject("data").getString("total"));
//         assertEquals("0",JSONObject.fromObject(queryInfo5).getJSONObject("data").getString("total"));
//     }
//
//
//    /**
//     *TC31归集地址向单签和多签地址转账
//     * 发两种币-查询归集地址-转账两个地址不同token-查询-回收-查询
//     * @throws Exception
//     */
//    @Test
//    public void TC31_transferSoloMulti()throws  Exception{
//
//        String transferData = "归集地址向" + "ADDRESS1" + "转账10个" + tokenType+"归集地址向" + "MULITADD4" + "转账10个" + tokenType;
//        List<Map>list=utilsClass.constructToken(ADDRESS1,tokenType,"10");
//        List<Map>list2=utilsClass.constructToken(MULITADD4,tokenType2,"10",list);
//        List<Map>list3=utilsClass.constructToken(MULITADD4,tokenType,"10",list);
//        log.info(transferData);
//        String transferInfo= SmartTransfer(IMPPUTIONADD,PRIKEY4,"", transferData, IMPPUTIONADD,list2);//不相同币种
//
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
//                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//
//
//        String transferInfo2= SmartTransfer(IMPPUTIONADD,PRIKEY4,"", transferData, IMPPUTIONADD,list3);//相同币种
//        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
//        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("state"));
//
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
//                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//
//
//        log.info("查询余额判断转账是否成功");
//        String queryInfo= soloSign.SmartGetBalanceByAddr(ADDRESS1,tokenType);
//        String queryInfo2= multiSign.SmartGetBalanceByAddr(MULITADD4,tokenType2);
//        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
//        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("state"));
//        assertEquals("20",JSONObject.fromObject(queryInfo).getJSONObject("data").getString("total"));
//        assertEquals("10",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString("total"));
//
//        log.info("回收Token");
//        String recycleInfo = multiSign.SmartRecyle(IMPPUTIONADD, PRIKEY4, tokenType, "970");
//        String recycleInfo2 = multiSign.SmartRecyle(IMPPUTIONADD, PRIKEY4, tokenType2, "990");
//        String recycleInfo3 = soloSign.Recycle(PRIKEY1, tokenType, "20");
//        String recycleInfo4 = multiSign.SmartRecyle(MULITADD4, PRIKEY1, tokenType2, "10");
//        String recycleInfo5 = multiSign.SmartRecyle(MULITADD4, PRIKEY1, tokenType, "10");
//
//        assertEquals("200",JSONObject.fromObject(recycleInfo).getString("state"));
//        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("state"));
//        assertEquals("200",JSONObject.fromObject(recycleInfo3).getString("state"));
//        assertEquals("200",JSONObject.fromObject(recycleInfo4).getString("state"));
//        assertEquals("200",JSONObject.fromObject(recycleInfo5).getString("state"));
//
//        log.info("查询余额判断回收成功与否");
//        String queryInfo3= soloSign.SmartGetBalanceByAddr(ADDRESS1,tokenType);
//        String queryInfo4= multiSign.SmartGetBalanceByAddr(MULITADD4,tokenType2);
//        String queryInfo5= multiSign.SmartGetBalanceByAddr(MULITADD4,tokenType);
//
//        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("state"));
//        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("state"));
//        assertEquals("200",JSONObject.fromObject(queryInfo5).getString("state"));
//        assertEquals("20",JSONObject.fromObject(queryInfo3).getJSONObject("data").getString("total"));
//        assertEquals("10",JSONObject.fromObject(queryInfo4).getJSONObject("data").getString("total"));
//        assertEquals("10",JSONObject.fromObject(queryInfo5).getJSONObject("data").getString("total"));
//    }
//
//
//    /**
//     *TC32归集地址向两个单签地址转账
//     * 发两种币-查询归集地址-转账两个地址不同token-查询-回收-查询
//     * @throws Exception
//     */
//    @Test
//    public void TC32_transferSolo()throws  Exception{
//
//        String transferData = "归集地址向" + "ADDRESS1" + "转账10个" + tokenType+"归集地址向" + "ADDRESS2" + "转账10个" + tokenType;
//        List<Map>list=utilsClass.constructToken(ADDRESS1,tokenType,"10");
//        List<Map>list2=utilsClass.constructToken(ADDRESS2,tokenType2,"10",list);
//        List<Map>list3=utilsClass.constructToken(ADDRESS2,tokenType,"10",list);
//        log.info(transferData);
//        String transferInfo= SmartTransfer(IMPPUTIONADD,PRIKEY4,"", transferData, IMPPUTIONADD,list2);//两个地址不同币种
//
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
//                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//
//
//        String transferInfo2= SmartTransfer(IMPPUTIONADD,PRIKEY4,"", transferData, IMPPUTIONADD,list3);//两个地址相同币种
//        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
//        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("state"));
//
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
//                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//
//
//        log.info("查询余额判断转账是否成功");
//        String queryInfo= soloSign.SmartGetBalanceByAddr(ADDRESS1,tokenType);
//        String queryInfo2= soloSign.SmartGetBalanceByAddr(ADDRESS2,tokenType2);
//        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
//        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("state"));
//        assertEquals("20",JSONObject.fromObject(queryInfo).getJSONObject("data").getString("total"));
//        assertEquals("10",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString("total"));
//
//        log.info("回收Token");
//        String recycleInfo = multiSign.SmartRecyle(IMPPUTIONADD, PRIKEY4, tokenType, "970");
//        String recycleInfo2 = multiSign.SmartRecyle(IMPPUTIONADD, PRIKEY4, tokenType2, "990");
//        String recycleInfo3 = soloSign.Recycle(PRIKEY1, tokenType, "20");
//        String recycleInfo4 = soloSign.Recycle( PRIKEY2, tokenType2, "10");
//        String recycleInfo5 = soloSign.Recycle(PRIKEY2, tokenType, "10");
//
//        assertEquals("200",JSONObject.fromObject(recycleInfo).getString("state"));
//        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("state"));
//        assertEquals("200",JSONObject.fromObject(recycleInfo3).getString("state"));
//        assertEquals("200",JSONObject.fromObject(recycleInfo4).getString("state"));
//        assertEquals("200",JSONObject.fromObject(recycleInfo5).getString("state"));
//
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType21),
//                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(recycleInfo2,utilsClass.sdkGetTxHashType20),
//                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//
//
//
//        log.info("查询余额判断回收成功与否");
//        String queryInfo3= soloSign.SmartGetBalanceByAddr(ADDRESS1,tokenType);
//        String queryInfo4= soloSign.SmartGetBalanceByAddr(ADDRESS2,tokenType2);
//        String queryInfo5= soloSign.SmartGetBalanceByAddr(ADDRESS2,tokenType);
//        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("state"));
//        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("state"));
//        assertEquals("200",JSONObject.fromObject(queryInfo5).getString("state"));
//        assertEquals("0",JSONObject.fromObject(queryInfo3).getJSONObject("data").getString("total"));
//        assertEquals("0",JSONObject.fromObject(queryInfo4).getJSONObject("data").getString("total"));
//        assertEquals("0",JSONObject.fromObject(queryInfo5).getJSONObject("data").getString("total"));
//    }
//
//    /**
//     *TC35多签地址向单签和多签地址转账
//     * 发两种币-查询归集地址-转账两个地址不同token-查询-回收-查询
//     * @throws Exception
//     */
//    @Test
//    public void TC35_MultiToSoloMulti() throws Exception {
//
//
//        String transferData = "归集地址向" + "MULITADD4" + "转账1000个" + tokenType + "归集地址向" + "MULITADD4" + "转账1000个" + tokenType;
//        List<Map> listInit = utilsClass.constructToken(MULITADD4, tokenType, "1000");
//        List<Map> list0 = utilsClass.constructToken(MULITADD4, tokenType2, "1000", listInit);
//        log.info(transferData);
//        String transferInfoInit = SmartTransfer(IMPPUTIONADD,PRIKEY4,"", transferData, IMPPUTIONADD, list0);//转账给多签地址
//        assertEquals("200",JSONObject.fromObject(transferInfoInit).getString("state"));
//
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
//                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//
//
//        List<Map> list = utilsClass.constructToken(ADDRESS1, tokenType, "10");
//        List<Map> list2 = utilsClass.constructToken(MULITADD5, tokenType2, "10", list);
//        List<Map> list3 = utilsClass.constructToken(MULITADD5, tokenType, "10", list);
//        log.info(transferData);
//        String transferInfo = multiSign.SmartTransfer(PRIKEY1, transferData, MULITADD4, list2);//不相同币种
//
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
//                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//
//
//        String transferInfo2 = multiSign.SmartTransfer(PRIKEY1, transferData, MULITADD4, list3);//相同币种
//        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
//        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("state"));
//
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
//                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//
//
//        log.info("查询余额判断转账是否成功");
//        String queryInfo = soloSign.SmartGetBalanceByAddr(ADDRESS1, tokenType);
//        String queryInfo2 = multiSign.SmartGetBalanceByAddr(MULITADD5,tokenType2);
//        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
//        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("state"));
//        assertEquals("20",JSONObject.fromObject(queryInfo).getJSONObject("data").getString("total"));
//        assertEquals("10",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString("total"));
//
//        log.info("锁定待回收Token: "+tokenType);
//        String resp2=multiSign.freezeToken(tokenType);
//
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType21),
//                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//
//        log.info("回收Token");
//        String recycleInfo = multiSign.SmartRecyle(MULITADD4, PRIKEY1, tokenType, "970");
//        String recycleInfo2 = multiSign.SmartRecyle(MULITADD4, PRIKEY1, tokenType2, "990");
//        String recycleInfo3 = soloSign.Recycle(PRIKEY1, tokenType, "20");
//        String recycleInfo4 = multiSign.SmartRecyle(MULITADD5, PRIKEY1, tokenType2, "10");
//        String recycleInfo5 = multiSign.SmartRecyle(MULITADD5, PRIKEY1, tokenType, "10");
//
//        assertEquals("200",JSONObject.fromObject(recycleInfo).getString("state"));
//        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("state"));
//        assertEquals("200",JSONObject.fromObject(recycleInfo3).getString("state"));
//        assertEquals("200",JSONObject.fromObject(recycleInfo4).getString("state"));
//        assertEquals("200",JSONObject.fromObject(recycleInfo5).getString("state"));
//
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(recycleInfo2,utilsClass.sdkGetTxHashType20),
//                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(recycleInfo5,utilsClass.sdkGetTxHashType20),
//                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//
//        log.info("查询余额判断回收成功与否");
//        String queryInfo3 = soloSign.SmartGetBalanceByAddr(ADDRESS1, tokenType);
//        String queryInfo4 = multiSign.SmartGetBalanceByAddr(MULITADD5,  tokenType2);
//        String queryInfo5 = multiSign.SmartGetBalanceByAddr(MULITADD5,  tokenType);
//        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("state"));
//        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("state"));
//        assertEquals("200",JSONObject.fromObject(queryInfo5).getString("state"));
//        assertEquals("0",JSONObject.fromObject(queryInfo3).getJSONObject("data").getString("total"));
//        assertEquals("0",JSONObject.fromObject(queryInfo4).getJSONObject("data").getString("total"));
//        assertEquals("0",JSONObject.fromObject(queryInfo5).getJSONObject("data").getString("total"));
//    }
//
//
//    /**
//     *TC33多签地址向两个多签地址转账
//     * 发两种币-查询归集地址-转账两个地址不同token-查询-回收-查询
//     * @throws Exception
//     */
//    @Test
//    public void TC33_MultiToMulti() throws Exception {
//
//
//        String transferData = "归集地址向" + "MULITADD4" + "转账1000个" + tokenType + "归集地址向" + "MULITADD4" + "转账1000个" + tokenType;
//        List<Map> listInit = utilsClass.constructToken(MULITADD4, tokenType, "1000");
//        List<Map> list0 = utilsClass.constructToken(MULITADD4, tokenType2, "1000", listInit);
//        log.info(transferData);
//        String transferInfoInit = SmartTransfer(IMPPUTIONADD,PRIKEY4,"", transferData, IMPPUTIONADD, list0);//转账给多签地址
//        assertEquals("200",JSONObject.fromObject(transferInfoInit).getString("state"));
//
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
//                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//
//
//        String ab = multiSign.SmartGetBalanceByAddr(MULITADD4,tokenType);
//        log.info("----------------------");
//        List<Map> list = utilsClass.constructToken(MULITADD6, tokenType, "10");
//        List<Map> list2 = utilsClass.constructToken(MULITADD5, tokenType2, "10", list);
//        List<Map> list3 = utilsClass.constructToken(MULITADD5, tokenType, "10", list);
//        log.info(transferData);
//        String transferInfo = multiSign.SmartTransfer(PRIKEY1, transferData, MULITADD4, list2);//不相同币种
//
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
//                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//
//
//        String transferInfo2 = multiSign.SmartTransfer(PRIKEY1, transferData, MULITADD4, list3);//相同币种
//        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
//        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("state"));
//
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
//                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//
//
//        log.info("查询余额判断转账是否成功");
//        String queryInfo = multiSign.SmartGetBalanceByAddr(MULITADD6,tokenType);
//        String queryInfo2 = multiSign.SmartGetBalanceByAddr(MULITADD5,tokenType2);
//        String queryInfo1 = multiSign.SmartGetBalanceByAddr(MULITADD5,tokenType);
//        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
//        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("state"));
//        assertEquals("200",JSONObject.fromObject(queryInfo1).getString("state"));
//        assertEquals("20",JSONObject.fromObject(queryInfo).getJSONObject("data").getString("total"));
//        assertEquals("10",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString("total"));
//        assertEquals("10",JSONObject.fromObject(queryInfo1).getJSONObject("data").getString("total"));
//
//
//        //20190411增加锁定解锁操作步骤后进行回收
//        log.info("锁定待回收Token: "+tokenType);
//        String resp=multiSign.freezeToken(tokenType);
//
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType21),
//                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//
//        log.info("解除锁定待回收Token: "+tokenType);
//        String resp1=multiSign.recoverFrozenToken(tokenType);
//
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType21),
//                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//
//
//        log.info("回收Token");
//        String recycleInfo = multiSign.SmartRecyle(MULITADD4, PRIKEY1, tokenType, "970");
//        String recycleInfo2 = multiSign.SmartRecyle(MULITADD4, PRIKEY1, tokenType2, "990");
//        String recycleInfo3 = multiSign.SmartRecyle(MULITADD6,PRIKEY4, tokenType, "20");
//        String recycleInfo4 = multiSign.SmartRecyle(MULITADD5, PRIKEY1, tokenType2, "10");
//        String recycleInfo5 = multiSign.SmartRecyle(MULITADD5, PRIKEY1, tokenType, "10");
//
//        assertEquals("200",JSONObject.fromObject(recycleInfo).getString("state"));
//        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("state"));
//        assertEquals("200",JSONObject.fromObject(recycleInfo3).getString("state"));
//        assertEquals("200",JSONObject.fromObject(recycleInfo4).getString("state"));
//        assertEquals("200",JSONObject.fromObject(recycleInfo5).getString("state"));
//
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(recycleInfo2,utilsClass.sdkGetTxHashType20),
//                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(recycleInfo5,utilsClass.sdkGetTxHashType20),
//                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//
//        log.info("查询余额判断回收成功与否");
//        String queryInfo3 = multiSign.SmartGetBalanceByAddr(MULITADD6,tokenType);
//        String queryInfo4 = multiSign.SmartGetBalanceByAddr(MULITADD5, tokenType2);
//        String queryInfo5 = multiSign.SmartGetBalanceByAddr(MULITADD5, tokenType);
//        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("state"));
//        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("state"));
//        assertEquals("200",JSONObject.fromObject(queryInfo5).getString("state"));
//        assertEquals("0",JSONObject.fromObject(queryInfo3).getJSONObject("data").getString("total"));
//        assertEquals("0",JSONObject.fromObject(queryInfo4).getJSONObject("data").getString("total"));
//        assertEquals("0",JSONObject.fromObject(queryInfo5).getJSONObject("data").getString("total"));
//
//        String zero=  multiSign.QueryZero(tokenType);
//        String zero2= multiSign.QueryZero(tokenType2);
//
//        assertEquals("200",JSONObject.fromObject(zero).getString("state"));
//        assertEquals("1000",JSONObject.fromObject(zero).getJSONObject("data").getJSONObject("detail").getString(tokenType));
//        assertEquals("200",JSONObject.fromObject(zero2).getString("state"));
//        assertEquals("1000",JSONObject.fromObject(zero2).getJSONObject("data").getJSONObject("detail").getString(tokenType2));
//    }
//
//
//    /**
//     *TC34多签地址向两个单签地址转账
//     * 发两种币-查询归集地址-转账两个地址不同token-查询-锁定token-回收-查询
//     * @throws Exception
//     */
//    @Test
//    public void TC34_MultiToSolo() throws Exception {
//
//
//        String transferData = "归集地址向" + "MULITADD4" + "转账1000个" + tokenType + "归集地址向" + "MULITADD4" + "转账1000个" + tokenType;
//        List<Map> listInit = utilsClass.constructToken(MULITADD4, tokenType, "1000");
//        List<Map> list0 = utilsClass.constructToken(MULITADD4, tokenType2, "1000", listInit);
//        log.info(transferData);
//        String transferInfoInit = SmartTransfer(IMPPUTIONADD,PRIKEY4,"", transferData, IMPPUTIONADD, list0);//转账给多签地址
//        assertEquals("200",JSONObject.fromObject(transferInfoInit).getString("state"));
//
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
//                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//
//
//        List<Map> list = utilsClass.constructToken(ADDRESS1, tokenType, "10");
//        List<Map> list2 = utilsClass.constructToken(ADDRESS2,tokenType2, "10", list);
//        List<Map> list3 = utilsClass.constructToken(ADDRESS2, tokenType, "10", list);
//        log.info(transferData);
//        String transferInfo = multiSign.SmartTransfer(PRIKEY1, transferData, MULITADD4, list2);//不相同币种
//        Thread.sleep(SLEEPTIME);//UTXO关系，两笔交易之间需要休眠
//        String transferInfo2 = multiSign.SmartTransfer(PRIKEY1, transferData, MULITADD4, list3);//相同币种
//        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
//        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("state"));
//
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
//                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//
//
//        log.info("查询余额判断转账是否成功");
//        String queryInfo = soloSign.SmartGetBalanceByAddr(ADDRESS1, tokenType);
//        String queryInfo2 = soloSign.SmartGetBalanceByAddr(ADDRESS2, tokenType2);
//        String queryInfo_2= soloSign.SmartGetBalanceByAddr(ADDRESS2,tokenType);
//        assertEquals("20",JSONObject.fromObject(queryInfo).getJSONObject("data").getString("total"));
//        assertEquals("10",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString("total"));
//        assertEquals("10",JSONObject.fromObject(queryInfo_2).getJSONObject("data").getString("total"));
//
//        log.info("锁定待回收Token: "+tokenType);
//        String resp=multiSign.freezeToken(tokenType);
//
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType21),
//                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//
//        log.info("回收Token");
//        String recycleInfo = multiSign.SmartRecyle(MULITADD4, PRIKEY1, tokenType, "970");
//        String recycleInfo2 = multiSign.SmartRecyle(MULITADD4, PRIKEY1, tokenType2, "990");
//        String recycleInfo3 = soloSign.Recycle(PRIKEY1, tokenType, "20");
//        String recycleInfo4 = soloSign.Recycle(PRIKEY2, tokenType2, "10");
//        String recycleInfo5 = soloSign.Recycle(PRIKEY2, tokenType, "10");
//
//        assertEquals("200",JSONObject.fromObject(recycleInfo).getString("state"));
//        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("state"));
//        assertEquals("200",JSONObject.fromObject(recycleInfo3).getString("state"));
//        assertEquals("200",JSONObject.fromObject(recycleInfo4).getString("state"));
//        assertEquals("200",JSONObject.fromObject(recycleInfo5).getString("state"));
//
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType21),
//                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(recycleInfo2,utilsClass.sdkGetTxHashType20),
//                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//
//
//        log.info("查询余额判断回收成功与否");
//        String queryInfo3 = soloSign.SmartGetBalanceByAddr(ADDRESS1, tokenType);
//        String queryInfo4 = soloSign.SmartGetBalanceByAddr(ADDRESS2, tokenType2);
//        String queryInfo5 = soloSign.SmartGetBalanceByAddr(ADDRESS2, tokenType);
//        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("state"));
//        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("state"));
//        assertEquals("200",JSONObject.fromObject(queryInfo5).getString("state"));
//        assertEquals("0",JSONObject.fromObject(queryInfo3).getJSONObject("data").getString("total"));
//        assertEquals("0",JSONObject.fromObject(queryInfo4).getJSONObject("data").getString("total"));
//        assertEquals("0",JSONObject.fromObject(queryInfo5).getJSONObject("data").getString("total"));
//    }
//
//    /**
//     * 多签账户转账给自己 无法转给自己
//     * @throws Exception
//     */
//    @Test
//    public void TransferToSelf() throws Exception {
//        String transferData = IMPPUTIONADD + "转给自己1000个" + tokenType;
//        List<Map> listInit = utilsClass.constructToken(IMPPUTIONADD, tokenType, "1000");
//        List<Map> list0 = utilsClass.constructToken(MULITADD4, tokenType2, "1000", listInit);
//        log.info(transferData);
//        String transferInfoInit = SmartTransfer(IMPPUTIONADD,PRIKEY4,"", transferData, IMPPUTIONADD, list0);//转账给包含自己地址的多个地址
//        assertEquals(true,transferInfoInit.contains("can't transfer to self"));
//        String transferInfoInit2 = SmartTransfer(IMPPUTIONADD,PRIKEY4,"", transferData, IMPPUTIONADD, listInit);//仅转账给自己
//        assertEquals(true,transferInfoInit2.contains("can't transfer to self"));
//    }

    //-----------------------------------------------------------------------------------------------------------


    public void installSmartAccountContract(String abfileName)throws Exception{
        WVMContractTest wvmContractTestSA = new WVMContractTest();
        UtilsClass utilsClassSA = new UtilsClass();
        CommonFunc commonFuncTeSA = new CommonFunc();

        //如果smartAccoutCtHash为空或者contractFileName不为constFileName 即"wvm\\account_simple.wlang" 时会重新安装
        if(smartAccoutCtHash.equals("") || (!contractFileName.equals(constFileName))){
            //安装
            String response =wvmContractTestSA.wvmInstallTest(abfileName,"");
            assertEquals("200",JSONObject.fromObject(response).getString("state"));
            commonFuncTeSA.sdkCheckTxOrSleep(commonFuncTeSA.getTxHash(response,utilsClassSA.sdkGetTxHashType20),
                    utilsClassSA.sdkGetTxDetailTypeV2,SLEEPTIME);
            smartAccoutCtHash = JSONObject.fromObject(response).getJSONObject("data").getString("name");
        }
    }

    //单签账户目前为签名公私钥对为PUBKEY1 PRIKEY1
    public String smartIssueToken(String tokenType,BigDecimal deadline,List<Map> issueToList)throws Exception{
        String isResult= multiSign.SmartIssueTokenReq(smartAccoutCtHash,tokenType,true,
                deadline,new BigDecimal(0),0,issueToList,"123456");
        String sigMsg1 = JSONObject.fromObject(isResult).getJSONObject("data").getString("sigMsg");
//        assertEquals(sigMsg1,String.valueOf(Hex.encodeHex(
//                JSONObject.fromObject(isResult).getJSONObject("data").getString("msg").getBytes(StandardCharsets.UTF_8))));

        String tempSM3Hash = certTool.getSm3Hash(PEER4IP,sigMsg1);
        String cryptMsg = certTool.sign(PEER4IP ,PRIKEY1,"",tempSM3Hash,"hex");

        String pubkey = utilsClass.readStringFromFile(resourcePath + "SM2/keys1/pubkey.pem").replaceAll("\r\n","\n");
//        pubkey = (new BASE64Decoder()).decodeBuffer(PUBKEY1).toString().replaceAll("\r\n","\n");

        String approveResp = multiSign.SmartIssueTokenApprove(sigMsg1,cryptMsg,pubkey);
        return approveResp;
    }
}
