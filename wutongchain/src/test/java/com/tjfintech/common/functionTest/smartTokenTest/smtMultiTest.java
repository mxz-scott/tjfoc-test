package com.tjfintech.common.functionTest.smartTokenTest;

import com.tjfintech.common.*;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.junit.Assert.assertEquals;


@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class smtMultiTest {
    UtilsClass utilsClass = new UtilsClass();
    SmartTokenCommon stc = new SmartTokenCommon();
    CommonFunc commonFunc = new CommonFunc();
    GoSmartToken st = new GoSmartToken();
    CertTool certTool = new CertTool();

    private static String tokenType;

    @BeforeClass
    public static void BeforeClass() throws Exception {
        if (MULITADD1.isEmpty()) {
            BeforeCondition bf = new BeforeCondition();
            bf.updatePubPriKey();
            bf.createSTAddresses();
            bf.installSmartAccountContract("account_simple.wlang");
        }
    }

    /**
     * 多签正常流程-发币：签名：查询：转账：查询:回收：查询
     */
    @Test
    public void TC03_multiProgress() throws Exception {

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

        log.info("查询 ADDRESS1 和 MULITADD4 余额，判断转账是否成功");
        stc.verifyAddressHasBalance(ADDRESS1, tokenType, "990.25");
        stc.verifyAddressHasBalance(MULITADD4, tokenType, "10");

        //销毁
        String destroyData1 = "销毁 ADDRESS1 中的" + tokenType;
        String destroyData2 = "销毁 MULITADD4 中的" + tokenType;
        List<Map> payList1 = stc.smartConstructTokenList(ADDRESS1, "test", "990.25", null);
        List<Map> payList2 = stc.smartConstructTokenList(MULITADD4, "test", "10", null);

        String destroyResp1 = stc.smartDestroy(tokenType, payList1, "", destroyData1);
        String destroyResp2 = stc.smartDestroy(tokenType, payList2, "", destroyData2);

        assertEquals("200", JSONObject.fromObject(destroyResp1).getString("state"));
        assertEquals("200", JSONObject.fromObject(destroyResp2).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType, SLEEPTIME);

        log.info("查询回收后账户余额是否为0");
        stc.verifyAddressNoBalance(ADDRESS1, tokenType);
        stc.verifyAddressNoBalance(MULITADD4, tokenType);

        log.info("查询回收账户余额");
        stc.verifyAddressHasBalance(ZEROADDRESS, tokenType, "1000.25");
    }


    /**
     * 资产类型转换
     */
    @Test
    public void TC_exchange() throws Exception {

        //发行
        tokenType = stc.beforeConfigIssueNewToken("200");
        String newTokenType = "NEW_" + UtilsClass.Random(10);

        //转换
        String transferData = "ADDRESS1 向 MULITADD4 转账10个" + tokenType;
        List<Map> payList = stc.smartConstructTokenList(ADDRESS1, "test", "200", null);
        List<Map> collList = stc.smartConstructTokenList(MULITADD4, "test", "200", null);
        String transferResp = stc.smartExchange
                (tokenType, payList, collList, newTokenType, "", transferData);

        assertEquals("200", JSONObject.fromObject(transferResp).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType, SLEEPTIME);

        log.info("查询 ADDRESS1 和 MULITADD4 余额，判断转账是否成功");
        stc.verifyAddressNoBalance(ADDRESS1, tokenType);
        stc.verifyAddressHasBalance(MULITADD4, newTokenType, "200");

    }

    /**
     * 连续向单签和多签地址转让2次
     */
    @Test
    public void TC_transferSoloMulti() throws Exception {

        //发行
        tokenType = stc.beforeConfigIssueNewToken("200");

        //转账
        String transferData = "ADDRESS1 向 MULITADD4/ADDRESS2 转账10.123456/20.123456个" + tokenType;
        List<Map> payList = stc.smartConstructTokenList(ADDRESS1, "test", "30.246912", null);
        List<Map> collList = stc.smartConstructTokenList(MULITADD4, "test", "10.123456", null);
        List<Map> collList2 = stc.smartConstructTokenList(ADDRESS2, "test", "20.123456", collList);
        String transferResp = stc.smartTransfer(tokenType, payList, collList2, "", "", transferData);

        assertEquals("200", JSONObject.fromObject(transferResp).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType, SLEEPTIME);

        log.info("查询 ADDRESS1 和 MULITADD4 余额，判断转账是否成功");
        stc.verifyAddressHasBalance(ADDRESS1, tokenType, "169.753088");
        stc.verifyAddressHasBalance(MULITADD4, tokenType, "10.123456");
        stc.verifyAddressHasBalance(ADDRESS2, tokenType, "20.123456");

        //转账
        transferResp = stc.smartTransfer(tokenType, payList, collList2, "", "", transferData);
        assertEquals("200", JSONObject.fromObject(transferResp).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType, SLEEPTIME);

        stc.verifyAddressHasBalance(ADDRESS1, tokenType, "139.506176");
        stc.verifyAddressHasBalance(MULITADD4, tokenType, "20.246912");
        stc.verifyAddressHasBalance(ADDRESS2, tokenType, "40.246912");

    }

    /**
     * MAXlevel为2验证流转层级大于2的情况[第2次转让不会成功，验证余额]
     */

    @Test
    public void TC_MAXleveltransfer() throws Exception {
        tokenType = "TB_" + Random(10);
        double timeStampNow = System.currentTimeMillis();
        BigDecimal expiredDate = new BigDecimal(timeStampNow + 12356789);
        BigDecimal activeDate = new BigDecimal(timeStampNow);
        String contractAddress = smartAccoutContractAddress;

        List<Map> toList = stc.smartConstructTokenList(ADDRESS1, "test", "1000.25", null);

        //发行申请
        String IssueApplyResp9 = st.SmartIssueTokenReq(contractAddress, tokenType, expiredDate, toList, activeDate, true, 2, "");
        String sigMsg1 = JSONObject.fromObject(IssueApplyResp9).getJSONObject("data").getString("sigMsg");
        //发行审核
        String tempSM3Hash = certTool.getSm3Hash(PEER4IP, sigMsg1);
        String cryptMsg = certTool.sign(PEER4IP, PRIKEY1, "", tempSM3Hash, "hex");
        String approveResp = st.SmartIssueTokenApprove(sigMsg1, cryptMsg, PUBKEY1);
        assertEquals("200", JSONObject.fromObject(approveResp).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(approveResp, utilsClass.sdkGetTxHashType21),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        // 第一次发行查询账户余额
        log.info("查询发行的账户余额");
        stc.verifyAddressHasBalance(ADDRESS1, tokenType, "1000.25");

        //转让为2级流转
        String transferData = "ADDRESS1 向 MULITADD4 转账10个" + tokenType;
        List<Map> payList = stc.smartConstructTokenList(ADDRESS1, "test", "10", null);
        List<Map> collList = stc.smartConstructTokenList(MULITADD4, "test", "10", null);
        String transferResp = stc.smartTransfer(tokenType, payList, collList, "", "", transferData);
        assertEquals("200", JSONObject.fromObject(transferResp).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType, SLEEPTIME);

        //流转后验证余额
        log.info("查询 ADDRESS1 和 MULITADD4 余额，判断转账是否成功");
        stc.verifyAddressHasBalance(ADDRESS1, tokenType, "990.25");
        stc.verifyAddressHasBalance(MULITADD4, tokenType, "10");


        //在转1次3级流转
        String transferData1 = "MULITADD4向 ADDRESS1 转账5个" + tokenType;
        List<Map> payList1 = stc.smartConstructTokenList(MULITADD4, "test", "5", null);
        List<Map> collList1 = stc.smartConstructTokenList(ADDRESS1, "test", "5", null);
        String transferResp1 = stc.smartTransfer(tokenType, payList1, collList1, "", "", transferData1);
        //请求应该是成功，但是应该转不过去的
        assertEquals("200", JSONObject.fromObject(transferResp1).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType, SLEEPTIME);
        //流转后验证余额
        log.info("查询 ADDRESS1 和 MULITADD4 余额，判断转账是否成功/这里应该是失败的");
        stc.verifyAddressHasBalance(ADDRESS1, tokenType, "990.25");
        stc.verifyAddressHasBalance(MULITADD4, tokenType, "10");

    }

    /**
     * 过期资产转让
     */
    @Test
    public void TC_expiredtransfer() throws Exception {

        tokenType = "TB_" + UtilsClass.Random(10);
        double timeStampNow = System.currentTimeMillis();
        BigDecimal expiredDate = new BigDecimal(timeStampNow + 60000);
        BigDecimal activeDate = new BigDecimal(timeStampNow);
        String contractAddress = smartAccoutContractAddress;

        List<Map> toList = stc.smartConstructTokenList(ADDRESS1, "test", "10.15", null);

        //发行申请
        String IssueApplyResp9 = st.SmartIssueTokenReq(contractAddress, tokenType, expiredDate, toList, activeDate, true, 0, "");
        String sigMsg1 = JSONObject.fromObject(IssueApplyResp9).getJSONObject("data").getString("sigMsg");
        //发行审核
        String tempSM3Hash = certTool.getSm3Hash(PEER4IP, sigMsg1);
        String cryptMsg = certTool.sign(PEER4IP, PRIKEY1, "", tempSM3Hash, "hex");
        String approveResp = st.SmartIssueTokenApprove(sigMsg1, cryptMsg, PUBKEY1);
        assertEquals("200", JSONObject.fromObject(approveResp).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(approveResp, utilsClass.sdkGetTxHashType21),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        // 第一次发行查询账户余额
        log.info("查询发行的账户余额");
        stc.verifyAddressHasBalance(ADDRESS1, tokenType, "10.15");

        Thread.sleep(180000);//3分钟

        //转让【应该转让不成功】
        String transferData = "ADDRESS1 向 MULITADD4 转账10个" + tokenType;
        List<Map> payList = stc.smartConstructTokenList(ADDRESS1, "test", "10", null);
        List<Map> collList = stc.smartConstructTokenList(MULITADD4, "test", "10", null);

        String transferResp = stc.smartTransfer(tokenType, payList, collList, "", "", transferData);

        assertEquals("200", JSONObject.fromObject(transferResp).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType, SLEEPTIME);

        log.info("查询 ADDRESS1 和 MULITADD4 余额，判断转账是否成功");
        stc.verifyAddressHasBalance(ADDRESS1, tokenType, "10.15");
        stc.verifyAddressNoBalance(MULITADD4, tokenType);

    }

    /**
     * 增发资产
     */

    @Test
    public void TC_reissedTruetest() throws Exception {

        String tokenType = "TB_" + UtilsClass.Random(10);
        double timeStampNow = System.currentTimeMillis();
        BigDecimal expiredDate = new BigDecimal(timeStampNow + 12356789);
        BigDecimal activeDate = new BigDecimal(timeStampNow);
        String contractAddress = smartAccoutContractAddress;
        List<Map> toList = stc.smartConstructTokenList(ADDRESS1, "test", "10", null);
        //第一次发行
        //发行申请
        String IssueApplyResp9 = st.SmartIssueTokenReq(contractAddress, tokenType, expiredDate, toList, activeDate, true, 0, "");
        String sigMsg1 = JSONObject.fromObject(IssueApplyResp9).getJSONObject("data").getString("sigMsg");
        //发行审核
        String tempSM3Hash = certTool.getSm3Hash(PEER4IP, sigMsg1);
        String cryptMsg = certTool.sign(PEER4IP, PRIKEY1, "", tempSM3Hash, "hex");
        String approveResp = st.SmartIssueTokenApprove(sigMsg1, cryptMsg, PUBKEY1);
        assertEquals("200", JSONObject.fromObject(approveResp).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(approveResp, utilsClass.sdkGetTxHashType21),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        // 验证第一次发行查询账户余额
        log.info("查询发行的账户余额");
        stc.verifyAddressHasBalance(ADDRESS1, tokenType, "10");
        //第二次发行
        toList.clear();
        List<Map> toList1 = stc.smartConstructTokenList(ADDRESS1, "test", "30", null);
        String IssueApplyResp10 = st.SmartIssueTokenReq(contractAddress, tokenType, expiredDate, toList1, activeDate, false, 0, "");
        String sigMsg2 = JSONObject.fromObject(IssueApplyResp10).getJSONObject("data").getString("sigMsg");
        String tempSM3Hash1 = certTool.getSm3Hash(PEER4IP, sigMsg2);
        String cryptMsg1 = certTool.sign(PEER4IP, PRIKEY1, "", tempSM3Hash1, "hex");
        String approveResp1 = st.SmartIssueTokenApprove(sigMsg2, cryptMsg1, PUBKEY1);
        assertEquals("200", JSONObject.fromObject(approveResp1).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(approveResp1, utilsClass.sdkGetTxHashType21),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        //验证第二次发行后账户余额
        log.info("查询发行的账户余额");
        stc.verifyAddressHasBalance(ADDRESS1, tokenType, "40");
    }

    /**
     * 发行 - 冻结 - 转让 - 解冻 - 回收
     */
    @Test
    public void TC_recovertransfer() throws Exception {

        String transferData = "ADDRESS1 向 MULITADD4 转账10个" + tokenType;
        List<Map> payList = stc.smartConstructTokenList(ADDRESS1, "test", "10", null);
        List<Map> collList = stc.smartConstructTokenList(MULITADD4, "test", "10", null);

        // 发行
        tokenType = stc.beforeConfigIssueNewToken("100.75");
        //冻结
        String freezeResp = st.SmartFreeze(tokenType, "");
        assertEquals("200", JSONObject.fromObject(freezeResp).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType, SLEEPTIME);
        //转账
        String transferResp = stc.smartTransfer(tokenType, payList, collList, "", "", transferData);
        assertEquals("200", JSONObject.fromObject(transferResp).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType, SLEEPTIME);

        log.info("查询 ADDRESS1 和 MULITADD4 余额，判断转账是否成功");
        stc.verifyAddressHasBalance(ADDRESS1, tokenType, "100.75");
        stc.verifyAddressNoBalance(MULITADD4, tokenType);
        //解冻
        String recoverResp = st.SmartRecover(tokenType, "");
        assertEquals("200", JSONObject.fromObject(recoverResp).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType, SLEEPTIME);

        String transferResp1 = stc.smartTransfer(tokenType, payList, collList, "", "", transferData);
        assertEquals("200", JSONObject.fromObject(transferResp1).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType, SLEEPTIME);

        log.info("查询 ADDRESS1 和 MULITADD4 余额，判断转账是否成功");
        stc.verifyAddressHasBalance(ADDRESS1, tokenType, "90.75");
        stc.verifyAddressHasBalance(MULITADD4, tokenType, "10");


    }


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
//        String transferInfo= multiSign.SmartTransferReq(IMPPUTIONADD,PRIKEY4,"",list,transferData,"");
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
//        String recycleInfo2 = multiSign.SmartRecyle(MULITADD4, PRIKEY1, "",tokenType, issueAmount1,"");
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
//        String minAmount = "";
//
//        if (UtilsClass.PRECISION == 10) {
//            minAmount = "0.0000000001";
//        }else {
//            minAmount = "0.000001";
//        }
//
//        log.info("多签发行小数量token");
//
//        double timeStampNow = System.currentTimeMillis();
//        BigDecimal deadline = new BigDecimal(timeStampNow + 12356789);
//
//        List<Map>list = utilsClass.smartConstuctIssueToList(IMPPUTIONADD,minAmount);
//
//        String issueResp2 = smartIssueToken(minToken,deadline,list);
//        assertEquals("200",JSONObject.fromObject(issueResp2).getString("state"));
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
//        String transferInfo= multiSign.SmartTransferReq(IMPPUTIONADD,PRIKEY4,"", list,transferData, "");
//        assertThat(transferInfo, containsString("200"));
//
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
//                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
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
//        String recycleInfo2 = multiSign.SmartRecyle(MULITADD4, PRIKEY1, "",tokenType, "0.01","小数量回收");
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
////    /**
////     * Tc024锁定后转账:
////     *
////     */
////    @Test
////    public void TC024_TransferAfterFrozen() throws Exception {
////
////        //20190411增加锁定步骤后进行转账
////        log.info("锁定待转账Token: "+tokenType);
////        String resp=multiSign.freezeToken(tokenType);
////
////        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType21),
////                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
////
////        log.info("查询归集地址中两种token余额");
////        String response1 = multiSign.SmartGetBalanceByAddr(IMPPUTIONADD,  tokenType);
////        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
////        assertEquals(actualAmount1,JSONObject.fromObject(response1).getJSONObject("data").getString("total"));
////
////        String transferData = "归集地址向MULITADD4转账10个" + tokenType;
////        List<Map>list=utilsClass.constructToken(MULITADD4,tokenType,"10");
////        log.info(transferData);
////
////        String transferInfo= SmartTransferReq(IMPPUTIONADD,PRIKEY4,"", transferData, IMPPUTIONADD,list);//相同币种
////        assertEquals("500",JSONObject.fromObject(transferInfo).getString("state"));
////
////
//////        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
//////                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
////
////
////        log.info("查询余额判断转账是否成功");
////        String queryInfo= multiSign.SmartGetBalanceByAddr(MULITADD4,tokenType);
////        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
////        assertEquals("0",JSONObject.fromObject(queryInfo).getJSONObject("data").getString("total"));
////
////        log.info("解除锁定待转账Token: "+tokenType);
////        String resp1=multiSign.recoverFrozenToken(tokenType);
////
////        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType21),
////                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
////
////
////        log.info("查询归集地址中两种token余额");
////        response1 = multiSign.SmartGetBalanceByAddr(IMPPUTIONADD, tokenType);
////        String response2 = multiSign.SmartGetBalanceByAddr(IMPPUTIONADD, tokenType2);
////        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
////        assertEquals(actualAmount1,JSONObject.fromObject(response1).getJSONObject("data").getString("total"));
////        assertEquals("200",JSONObject.fromObject(response2).getString("state"));
////        assertEquals(actualAmount2,JSONObject.fromObject(response2).getJSONObject("data").getString("total"));
////
////        transferData = "归集地址向" + "MULITADD4" + "转账10个" + tokenType+"归集地址向" + "MULITADD4" + "转账10个" + tokenType;
////        list=utilsClass.constructToken(MULITADD4,tokenType,"10");
////        List<Map>list2=utilsClass.constructToken(MULITADD5,tokenType2,"10",list);
////        List<Map>list3=utilsClass.constructToken(MULITADD5,tokenType,"10",list);
////        log.info(transferData);
////        transferInfo= SmartTransferReq(IMPPUTIONADD,PRIKEY4,"", transferData, IMPPUTIONADD,list2);//不同币种
////
////        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
////                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
////
////
////        String transferInfo2= SmartTransferReq(IMPPUTIONADD,PRIKEY4,"", transferData, IMPPUTIONADD,list3);//相同币种
////        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
////        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("state"));
////
////        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
////                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
////
////
////        log.info("查询余额判断转账是否成功");
////        queryInfo= multiSign.SmartGetBalanceByAddr(MULITADD4,tokenType);
////        String queryInfo2= multiSign.SmartGetBalanceByAddr(MULITADD5,tokenType2);
////        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
////        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("state"));
////        assertEquals("20",JSONObject.fromObject(queryInfo).getJSONObject("data").getString("total"));
////        assertEquals("10",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString("total"));
////
////        multiSign.SmartGetBalanceByAddr(IMPPUTIONADD,tokenType);
////        multiSign.SmartGetBalanceByAddr(IMPPUTIONADD, tokenType2);
////
////        String amount1, amount2;
////
////        if (UtilsClass.PRECISION == 10) {
////            amount1 = "970.1234567891";
////            amount2 = "990.8765432123";
////        }else {
////            amount1 = "970.123456";
////            amount2 = "990.876543";
////        }
////
////
////        log.info("回收Token");
////        String recycleInfo = multiSign.SmartRecyle(IMPPUTIONADD, PRIKEY4, tokenType, amount1);
////        String recycleInfo2 = multiSign.SmartRecyle(IMPPUTIONADD, PRIKEY4, tokenType2, amount2);
////        String recycleInfo3 = multiSign.SmartRecyle(MULITADD4, PRIKEY1, tokenType, "20");
////        String recycleInfo4 = multiSign.SmartRecyle(MULITADD5, PRIKEY1, tokenType2, "10");
////        String recycleInfo5 = multiSign.SmartRecyle(MULITADD5, PRIKEY1, tokenType, "10");
////
////        assertEquals("200",JSONObject.fromObject(recycleInfo).getString("state"));
////        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("state"));
////        assertEquals("200",JSONObject.fromObject(recycleInfo3).getString("state"));
////        assertEquals("200",JSONObject.fromObject(recycleInfo4).getString("state"));
////        assertEquals("200",JSONObject.fromObject(recycleInfo5).getString("state"));
////
////        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
////                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
////        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(recycleInfo2,utilsClass.sdkGetTxHashType20),
////                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
////
////
////        log.info("查询余额判断回收成功与否");
////        String queryInfo3= multiSign.SmartGetBalanceByAddr(MULITADD4,tokenType);
////        String queryInfo4= multiSign.SmartGetBalanceByAddr(MULITADD5,tokenType2);
////        String queryInfo5= multiSign.SmartGetBalanceByAddr(MULITADD5,tokenType);
////
////        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("state"));
////        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("state"));
////        assertEquals("200",JSONObject.fromObject(queryInfo5).getString("state"));
////
////        assertEquals("0",JSONObject.fromObject(queryInfo3).getJSONObject("data").getString("total"));
////        assertEquals("0",JSONObject.fromObject(queryInfo4).getJSONObject("data").getString("total"));
////        assertEquals("0",JSONObject.fromObject(queryInfo5).getJSONObject("data").getString("total"));
////
////    }
////
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
//         String transferInfo= multiSign.SmartTransferReq(IMPPUTIONADD,PRIKEY4,"",list2, transferData, "");//不同币种
//
//         commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
//                 utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//
//
//         String transferInfo2= multiSign.SmartTransferReq(IMPPUTIONADD,PRIKEY4,"", list3,transferData, "");//相同币种
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
//         String recycleInfo = multiSign.SmartRecyle(IMPPUTIONADD, PRIKEY4,"", tokenType, amount1,"");
//         String recycleInfo2 = multiSign.SmartRecyle(IMPPUTIONADD, PRIKEY4, "",tokenType2, amount2,"");
//         String recycleInfo3 = multiSign.SmartRecyle(MULITADD4, PRIKEY1, "",tokenType, "20","");
//         String recycleInfo4 = multiSign.SmartRecyle(MULITADD5, PRIKEY1, "",tokenType2, "10","");
//         String recycleInfo5 = multiSign.SmartRecyle(MULITADD5, PRIKEY1, "",tokenType, "10","");
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
//        String transferInfo= multiSign.SmartTransferReq(IMPPUTIONADD,PRIKEY4,"", list2,transferData, "");//不相同币种
//
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
//                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//
//
//        String transferInfo2= multiSign.SmartTransferReq(IMPPUTIONADD,PRIKEY4,"",list3, transferData, "");//相同币种
//        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
//        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("state"));
//
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
//                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//
//
//        log.info("查询余额判断转账是否成功");
//        String queryInfo= multiSign.SmartGetBalanceByAddr(ADDRESS1,tokenType);
//        String queryInfo2= multiSign.SmartGetBalanceByAddr(MULITADD4,tokenType2);
//        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
//        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("state"));
//        assertEquals("20",JSONObject.fromObject(queryInfo).getJSONObject("data").getString("total"));
//        assertEquals("10",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString("total"));
//
//        log.info("回收Token");
//        String recycleInfo = multiSign.SmartRecyle(IMPPUTIONADD, PRIKEY4, "",tokenType, "970","");
//        String recycleInfo2 = multiSign.SmartRecyle(IMPPUTIONADD, PRIKEY4, "",tokenType2, "990","");
//        String recycleInfo3 = multiSign.SmartRecyle(ADDRESS1,PRIKEY1,"", tokenType, "20","");
//        String recycleInfo4 = multiSign.SmartRecyle(MULITADD4, PRIKEY1, "",tokenType2, "10","");
//        String recycleInfo5 = multiSign.SmartRecyle(MULITADD4, PRIKEY1, "",tokenType, "10","");
//
//        assertEquals("200",JSONObject.fromObject(recycleInfo).getString("state"));
//        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("state"));
//        assertEquals("200",JSONObject.fromObject(recycleInfo3).getString("state"));
//        assertEquals("200",JSONObject.fromObject(recycleInfo4).getString("state"));
//        assertEquals("200",JSONObject.fromObject(recycleInfo5).getString("state"));
//
//        log.info("查询余额判断回收成功与否");
//        String queryInfo3= multiSign.SmartGetBalanceByAddr(ADDRESS1,tokenType);
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
//        String transferInfo= multiSign.SmartTransferReq(IMPPUTIONADD,PRIKEY4,"",list2, transferData, "");//两个地址不同币种
//
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
//                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//
//
//        String transferInfo2= multiSign.SmartTransferReq(IMPPUTIONADD,PRIKEY4,"",list3, transferData, "");//两个地址相同币种
//        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
//        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("state"));
//
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
//                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//
//
//        log.info("查询余额判断转账是否成功");
//        String queryInfo= multiSign.SmartGetBalanceByAddr(ADDRESS1,tokenType);
//        String queryInfo2= multiSign.SmartGetBalanceByAddr(ADDRESS2,tokenType2);
//        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
//        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("state"));
//        assertEquals("20",JSONObject.fromObject(queryInfo).getJSONObject("data").getString("total"));
//        assertEquals("10",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString("total"));
//
//        log.info("回收Token");
//        String recycleInfo = multiSign.SmartRecyle(IMPPUTIONADD, PRIKEY4,"", tokenType, "970","");
//        String recycleInfo2 = multiSign.SmartRecyle(IMPPUTIONADD, PRIKEY4, "",tokenType2, "990","");
//        String recycleInfo3 = multiSign.SmartRecyle(ADDRESS1,PRIKEY1, "",tokenType, "20","");
//        String recycleInfo4 = multiSign.SmartRecyle( ADDRESS2,PRIKEY2,"", tokenType2, "10","");
//        String recycleInfo5 = multiSign.SmartRecyle(ADDRESS2,PRIKEY2, "",tokenType, "10","");
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
//        String queryInfo3= multiSign.SmartGetBalanceByAddr(ADDRESS1,tokenType);
//        String queryInfo4= multiSign.SmartGetBalanceByAddr(ADDRESS2,tokenType2);
//        String queryInfo5= multiSign.SmartGetBalanceByAddr(ADDRESS2,tokenType);
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
//        String transferInfoInit = multiSign.SmartTransferReq(IMPPUTIONADD,PRIKEY4,"",list0, transferData, "" );//转账给多签地址
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
//        String transferInfo = multiSign.SmartTransferReq(ADDRESS1,PRIKEY1, "",list2,transferData, "" );//不相同币种
//
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
//                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//
//
//        String transferInfo2 = multiSign.SmartTransferReq(ADDRESS1,PRIKEY1, "",list3,transferData, "" );//相同币种
//        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
//        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("state"));
//
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
//                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//
//
//        log.info("查询余额判断转账是否成功");
//        String queryInfo = multiSign.SmartGetBalanceByAddr(ADDRESS1, tokenType);
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
//        String recycleInfo = multiSign.SmartRecyle(MULITADD4, PRIKEY1,"", tokenType, "970","");
//        String recycleInfo2 = multiSign.SmartRecyle(MULITADD4, PRIKEY1,"", tokenType2, "990","");
//        String recycleInfo3 = multiSign.SmartRecyle(ADDRESS1,PRIKEY1, "",tokenType, "20","");
//        String recycleInfo4 = multiSign.SmartRecyle(MULITADD5, PRIKEY1, "",tokenType2, "10","");
//        String recycleInfo5 = multiSign.SmartRecyle(MULITADD5, PRIKEY1, "",tokenType, "10","");
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
//        String queryInfo3 = multiSign.SmartGetBalanceByAddr(ADDRESS1, tokenType);
//        String queryInfo4 = multiSign.SmartGetBalanceByAddr(MULITADD5,  tokenType2);
//        String queryInfo5 = multiSign.SmartGetBalanceByAddr(MULITADD5,  tokenType);
//        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("state"));
//        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("state"));
//        assertEquals("200",JSONObject.fromObject(queryInfo5).getString("state"));
//        assertEquals("0",JSONObject.fromObject(queryInfo3).getJSONObject("data").getString("total"));
//        assertEquals("0",JSONObject.fromObject(queryInfo4).getJSONObject("data").getString("total"));
//        assertEquals("0",JSONObject.fromObject(queryInfo5).getJSONObject("data").getString("total"));
//    }
////
////
////    /**
////     *TC33多签地址向两个多签地址转账
////     * 发两种币-查询归集地址-转账两个地址不同token-查询-回收-查询
////     * @throws Exception
////     */
////    @Test
////    public void TC33_MultiToMulti() throws Exception {
////
////
////        String transferData = "归集地址向" + "MULITADD4" + "转账1000个" + tokenType + "归集地址向" + "MULITADD4" + "转账1000个" + tokenType;
////        List<Map> listInit = utilsClass.constructToken(MULITADD4, tokenType, "1000");
////        List<Map> list0 = utilsClass.constructToken(MULITADD4, tokenType2, "1000", listInit);
////        log.info(transferData);
////        String transferInfoInit = SmartTransferReq(IMPPUTIONADD,PRIKEY4,"", transferData, IMPPUTIONADD, list0);//转账给多签地址
////        assertEquals("200",JSONObject.fromObject(transferInfoInit).getString("state"));
////
////        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
////                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
////
////
////        String ab = multiSign.SmartGetBalanceByAddr(MULITADD4,tokenType);
////        log.info("----------------------");
////        List<Map> list = utilsClass.constructToken(MULITADD6, tokenType, "10");
////        List<Map> list2 = utilsClass.constructToken(MULITADD5, tokenType2, "10", list);
////        List<Map> list3 = utilsClass.constructToken(MULITADD5, tokenType, "10", list);
////        log.info(transferData);
////        String transferInfo = multiSign.SmartTransferReq(PRIKEY1, transferData, MULITADD4, list2);//不相同币种
////
////        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
////                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
////
////
////        String transferInfo2 = multiSign.SmartTransferReq(PRIKEY1, transferData, MULITADD4, list3);//相同币种
////        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
////        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("state"));
////
////        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
////                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
////
////
////        log.info("查询余额判断转账是否成功");
////        String queryInfo = multiSign.SmartGetBalanceByAddr(MULITADD6,tokenType);
////        String queryInfo2 = multiSign.SmartGetBalanceByAddr(MULITADD5,tokenType2);
////        String queryInfo1 = multiSign.SmartGetBalanceByAddr(MULITADD5,tokenType);
////        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
////        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("state"));
////        assertEquals("200",JSONObject.fromObject(queryInfo1).getString("state"));
////        assertEquals("20",JSONObject.fromObject(queryInfo).getJSONObject("data").getString("total"));
////        assertEquals("10",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString("total"));
////        assertEquals("10",JSONObject.fromObject(queryInfo1).getJSONObject("data").getString("total"));
////
////
////        //20190411增加锁定解锁操作步骤后进行回收
////        log.info("锁定待回收Token: "+tokenType);
////        String resp=multiSign.freezeToken(tokenType);
////
////        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType21),
////                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
////
////        log.info("解除锁定待回收Token: "+tokenType);
////        String resp1=multiSign.recoverFrozenToken(tokenType);
////
////        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType21),
////                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
////
////
////        log.info("回收Token");
////        String recycleInfo = multiSign.SmartRecyle(MULITADD4, PRIKEY1, tokenType, "970");
////        String recycleInfo2 = multiSign.SmartRecyle(MULITADD4, PRIKEY1, tokenType2, "990");
////        String recycleInfo3 = multiSign.SmartRecyle(MULITADD6,PRIKEY4, tokenType, "20");
////        String recycleInfo4 = multiSign.SmartRecyle(MULITADD5, PRIKEY1, tokenType2, "10");
////        String recycleInfo5 = multiSign.SmartRecyle(MULITADD5, PRIKEY1, tokenType, "10");
////
////        assertEquals("200",JSONObject.fromObject(recycleInfo).getString("state"));
////        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("state"));
////        assertEquals("200",JSONObject.fromObject(recycleInfo3).getString("state"));
////        assertEquals("200",JSONObject.fromObject(recycleInfo4).getString("state"));
////        assertEquals("200",JSONObject.fromObject(recycleInfo5).getString("state"));
////
////        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(recycleInfo2,utilsClass.sdkGetTxHashType20),
////                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
////        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(recycleInfo5,utilsClass.sdkGetTxHashType20),
////                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
////
////        log.info("查询余额判断回收成功与否");
////        String queryInfo3 = multiSign.SmartGetBalanceByAddr(MULITADD6,tokenType);
////        String queryInfo4 = multiSign.SmartGetBalanceByAddr(MULITADD5, tokenType2);
////        String queryInfo5 = multiSign.SmartGetBalanceByAddr(MULITADD5, tokenType);
////        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("state"));
////        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("state"));
////        assertEquals("200",JSONObject.fromObject(queryInfo5).getString("state"));
////        assertEquals("0",JSONObject.fromObject(queryInfo3).getJSONObject("data").getString("total"));
////        assertEquals("0",JSONObject.fromObject(queryInfo4).getJSONObject("data").getString("total"));
////        assertEquals("0",JSONObject.fromObject(queryInfo5).getJSONObject("data").getString("total"));
////
////        String zero=  multiSign.QueryZero(tokenType);
////        String zero2= multiSign.QueryZero(tokenType2);
////
////        assertEquals("200",JSONObject.fromObject(zero).getString("state"));
////        assertEquals("1000",JSONObject.fromObject(zero).getJSONObject("data").getJSONObject("detail").getString(tokenType));
////        assertEquals("200",JSONObject.fromObject(zero2).getString("state"));
////        assertEquals("1000",JSONObject.fromObject(zero2).getJSONObject("data").getJSONObject("detail").getString(tokenType2));
////    }
////
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
//        String transferInfoInit = multiSign.SmartTransferReq(IMPPUTIONADD,PRIKEY4,"", list0,transferData, "" );//转账给多签地址
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
//        String transferInfo = multiSign.SmartTransferReq(ADDRESS1,PRIKEY1, "",list2,transferData, "" );//不相同币种
//        Thread.sleep(SLEEPTIME);//UTXO关系，两笔交易之间需要休眠
//        String transferInfo2 = multiSign.SmartTransferReq(ADDRESS1,PRIKEY1, "", list3,transferData, "" );//相同币种
//        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
//        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("state"));
//
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
//                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//
//
//        log.info("查询余额判断转账是否成功");
//        String queryInfo = multiSign.SmartGetBalanceByAddr(ADDRESS1, tokenType);
//        String queryInfo2 = multiSign.SmartGetBalanceByAddr(ADDRESS2, tokenType2);
//        String queryInfo_2= multiSign.SmartGetBalanceByAddr(ADDRESS2,tokenType);
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
//        String recycleInfo = multiSign.SmartRecyle(MULITADD4, PRIKEY1,"", tokenType, "970","");
//        String recycleInfo2 = multiSign.SmartRecyle(MULITADD4, PRIKEY1, "",tokenType2, "990","");
//        String recycleInfo3 = multiSign.SmartRecyle(ADDRESS1,PRIKEY1, "",tokenType, "20","");
//        String recycleInfo4 = multiSign.SmartRecyle(ADDRESS2,PRIKEY2, "",tokenType2, "10","");
//        String recycleInfo5 = multiSign.SmartRecyle(ADDRESS2,PRIKEY2, "",tokenType, "10","");
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
//        String queryInfo3 = multiSign.SmartGetBalanceByAddr(ADDRESS1, tokenType);
//        String queryInfo4 = multiSign.SmartGetBalanceByAddr(ADDRESS2, tokenType2);
//        String queryInfo5 = multiSign.SmartGetBalanceByAddr(ADDRESS2, tokenType);
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
//        String transferInfoInit = multiSign.SmartTransferReq(IMPPUTIONADD,PRIKEY4,"", list0,transferData, "" );//转账给包含自己地址的多个地址
//        assertEquals(true,transferInfoInit.contains("can't transfer to self"));
//        String transferInfoInit2 = multiSign.SmartTransferReq(IMPPUTIONADD,PRIKEY4,"",listInit, transferData, "" );//仅转账给自己
//        assertEquals(true,transferInfoInit2.contains("can't transfer to self"));
//    }
//
//    //-----------------------------------------------------------------------------------------------------------
//
//
//    public void installSmartAccountContract(String abfileName)throws Exception{
//        WVMContractTest wvmContractTestSA = new WVMContractTest();
//        UtilsClass utilsClassSA = new UtilsClass();
//        CommonFunc commonFuncTeSA = new CommonFunc();
//
//        //如果smartAccoutCtHash为空或者contractFileName不为constFileName 即"wvm\\account_simple.wlang" 时会重新安装
//        if(smartAccoutContractAddress.equals("") || (!contractFileName.equals(constFileName))){
//            //安装
//            String response =wvmContractTestSA.wvmInstallTest(abfileName,"");
//            assertEquals("200",JSONObject.fromObject(response).getString("state"));
//            commonFuncTeSA.sdkCheckTxOrSleep(commonFuncTeSA.getTxHash(response,utilsClassSA.sdkGetTxHashType20),
//                    utilsClassSA.sdkGetTxDetailTypeV2,SLEEPTIME);
//            smartAccoutContractAddress = JSONObject.fromObject(response).getJSONObject("data").getString("name");
//        }
//    }
//
//    //单签账户目前为签名公私钥对为PUBKEY1 PRIKEY1
//    public String smartIssueToken(String tokenType,BigDecimal deadline,List<Map> issueToList)throws Exception{
//        String isResult= multiSign.SmartIssueTokenReq(smartAccoutContractAddress,tokenType,true,
//                deadline,new BigDecimal(0),0,issueToList,"123456");
//        String sigMsg1 = JSONObject.fromObject(isResult).getJSONObject("data").getString("sigMsg");
////        assertEquals(sigMsg1,String.valueOf(Hex.encodeHex(
////                JSONObject.fromObject(isResult).getJSONObject("data").getString("msg").getBytes(StandardCharsets.UTF_8))));
//
//        String tempSM3Hash = certTool.getSm3Hash(PEER4IP,sigMsg1);
//        String cryptMsg = certTool.sign(PEER4IP ,PRIKEY1,"",tempSM3Hash,"hex");
//
//        String pubkey = utilsClass.readStringFromFile(testDataPath + "cert/SM2/keys1/pubkey.pem").replaceAll("\r\n","\n");
////        pubkey = (new BASE64Decoder()).decodeBuffer(PUBKEY1).toString().replaceAll("\r\n","\n");
//
//        String approveResp = multiSign.SmartIssueTokenApprove(sigMsg1,cryptMsg,pubkey);
//        return approveResp;
//    }
}
