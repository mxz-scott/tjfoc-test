package com.tjfintech.common.functionTest.smartTokenTest;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CertTool;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.GoSmartToken;
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
public class smtMultiInvalidTest {
    UtilsClass utilsClass = new UtilsClass();
    SmartTokenCommon stc = new SmartTokenCommon();
    CommonFunc commonFunc = new CommonFunc();
    GoSmartToken st = new GoSmartToken();
    CertTool certTool = new CertTool();

    private static String tokenType;
    String invalidAddress = "SsPB7k7FFcTG3DbtCHPpn9n3op46pu4GVQ3SW2PxRWqanES6yP7";
    String newTokenType = "";

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
        String transferResp = stc.smartExchange(tokenType, payList, collList, newTokenType, "", transferData);

        assertEquals("200", JSONObject.fromObject(transferResp).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType, SLEEPTIME);

        log.info("查询 ADDRESS1 和 MULITADD4 余额，判断转账是否成功");
        stc.verifyAddressNoBalance(ADDRESS1, tokenType);
        stc.verifyAddressHasBalance(MULITADD4, newTokenType, "200");


    }

    /**
     * 支出和收入金额不匹配，转账申请失败
     */
    @Test
    public void TC_transferAmountMismatch() throws Exception {

        //发行
        tokenType = stc.beforeConfigIssueNewToken("200");

        //转账
        String transferData = "ADDRESS1 向 MULITADD4/ADDRESS2 转账10.123456/20.123456个" + tokenType;
        List<Map> payList = stc.smartConstructTokenList(ADDRESS1, "test", "200", null);
        List<Map> collList = stc.smartConstructTokenList(MULITADD4, "test", "10.123456", null);
        List<Map> collList2 = stc.smartConstructTokenList(ADDRESS2, "test", "20.123456", collList);
        String transferResp = st.SmartTransferReq(tokenType, payList, collList2, "", "", transferData);
        assertEquals("400", JSONObject.fromObject(transferResp).getString("state"));

        log.info("查询 ADDRESS1 和 MULITADD4 余额，判断转账是否成功");
        stc.verifyAddressHasBalance(ADDRESS1, tokenType, "200");
        String querInfo = st.SmartGetBalanceByAddr(MULITADD4, "");
        String querInfo2 = st.SmartGetBalanceByAddr(ADDRESS2, "");
        assertEquals(false, querInfo.contains(tokenType));
        assertEquals(false, querInfo2.contains(tokenType));

    }

    /**
     * token未发行、未审核通过、冻结状态，转账申请失败
     */
    @Test
    public void TC_transferTokenInvalid() throws Exception {

        //token未发行
        tokenType = System.currentTimeMillis() + "";
        String transferData = "ADDRESS1 向 MULITADD4 转账200个" + tokenType;
        List<Map> payList = stc.smartConstructTokenList(ADDRESS1, "test", "200", null);
        List<Map> collList = stc.smartConstructTokenList(MULITADD4, "test", "200", null);
        String transferResp = st.SmartTransferReq(tokenType, payList, collList, "", "", transferData);
        assertEquals("400", JSONObject.fromObject(transferResp).getString("state"));

        //未审核状态
        tokenType = "TB_" + UtilsClass.Random(10);
        double timeStampNow = System.currentTimeMillis();
        BigDecimal deadline = new BigDecimal(timeStampNow + 12356789);
        List<Map> list = stc.smartConstructTokenList(ADDRESS1, "test", "200", null);
        String issueResp = st.SmartIssueTokenReq
                (smartAccoutContractAddress, tokenType, deadline, list, new BigDecimal(0), true, 0, "");
        assertEquals("200", JSONObject.fromObject(issueResp).getString("state"));

        transferResp = st.SmartTransferReq(tokenType, payList, collList, "", "", transferData);
        assertEquals("400", JSONObject.fromObject(transferResp).getString("state"));

        //冻结状态
        tokenType = stc.beforeConfigIssueNewToken("200");
        String freezeResp = st.SmartFreeze(tokenType, "");
        assertEquals("200", JSONObject.fromObject(freezeResp).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType, SLEEPTIME);

        transferResp = stc.smartTransfer(tokenType, payList, collList, "", "", transferData);
        assertEquals("200", JSONObject.fromObject(transferResp).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType, SLEEPTIME);

        log.info("查询 ADDRESS1 和 MULITADD4 余额，判断转账是否成功");
        stc.verifyAddressHasBalance(ADDRESS1, tokenType, "200");
        stc.verifyAddressNoBalance(MULITADD4, tokenType);

    }

    /**
     * 首次增发资产限制后，增发失败
     */
    @Test
    public void TC_reissedFalsetest() throws Exception {


        String tokenType = "TB_" + UtilsClass.Random(10);
        double timeStampNow = System.currentTimeMillis();
        BigDecimal expiredDate = new BigDecimal(timeStampNow + 12356789);
        BigDecimal activeDate = new BigDecimal(timeStampNow);
        String contractAddress = smartAccoutContractAddress;
        List<Map> toList = stc.smartConstructTokenList(ADDRESS1, "test", "10", null);
        //第一次发行
        //发行申请
        String IssueApplyResp9 = st.SmartIssueTokenReq(contractAddress, tokenType, expiredDate, toList, activeDate, false, 0, "");
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
        stc.verifyAddressHasBalance(ADDRESS1, tokenType, "10");

    }

    /**
     * 增发资产后再次增发资产限制增发，增发失败
     */
    @Test
    public void TC_01reissedFalsetest() throws Exception {


        String tokenType = "TB_" + UtilsClass.Random(10);
        double timeStampNow = System.currentTimeMillis();
        BigDecimal expiredDate = new BigDecimal(timeStampNow + 12356789);
        BigDecimal activeDate = new BigDecimal(timeStampNow);
        String contractAddress = smartAccoutContractAddress;
        List<Map> toList = stc.smartConstructTokenList(ADDRESS1, "test", "10", null);
        //第一次发行
        //发行申请
        String IssueApplyResp1 = st.SmartIssueTokenReq(contractAddress, tokenType, expiredDate, toList, activeDate, true, 0, "");
        String sigMsg1 = JSONObject.fromObject(IssueApplyResp1).getJSONObject("data").getString("sigMsg");
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

        String IssueApplyResp2 = st.SmartIssueTokenReq(contractAddress, tokenType, expiredDate, toList, activeDate, false, 0, "");
        String sigMsg = JSONObject.fromObject(IssueApplyResp2).getJSONObject("data").getString("sigMsg");
        String tempSM3Hash1 = certTool.getSm3Hash(PEER4IP, sigMsg);
        String cryptMsg1 = certTool.sign(PEER4IP, PRIKEY1, "", tempSM3Hash1, "hex");
        String approveResp1 = st.SmartIssueTokenApprove(sigMsg, cryptMsg1, PUBKEY1);
        assertEquals("200", JSONObject.fromObject(approveResp1).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(approveResp1, utilsClass.sdkGetTxHashType21),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        //验证第二次发行后账户余额
        log.info("查询发行的账户余额");
        stc.verifyAddressHasBalance(ADDRESS1, tokenType, "20");


        //第3次发行

        String IssueApplyResp3 = st.SmartIssueTokenReq(contractAddress, tokenType, expiredDate, toList, activeDate, false, 0, "");
        String sigMsg3 = JSONObject.fromObject(IssueApplyResp3).getJSONObject("data").getString("sigMsg");
        String tempSM3Hash2= certTool.getSm3Hash(PEER4IP, sigMsg3);
        String cryptMsg2 = certTool.sign(PEER4IP, PRIKEY1, "", tempSM3Hash2, "hex");
        String approveResp2= st.SmartIssueTokenApprove(sigMsg3, cryptMsg2, PUBKEY1);
        assertEquals("200", JSONObject.fromObject(approveResp2).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(approveResp2, utilsClass.sdkGetTxHashType21),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        //验证第3次发行后账户余额
        log.info("查询发行的账户余额");
        stc.verifyAddressHasBalance(ADDRESS1, tokenType, "20");

    }


    /**
     * paymentList/collectionList列表数据异常，转账申请失败
     */
    @Test
    public void TC_transferPayCollListInvalid() throws Exception {

        //paymentList.address地址错误
        tokenType = stc.beforeConfigIssueNewToken("200");
        String transferData = "ADDRESS1 向 MULITADD4 转账200个" + tokenType;
        List<Map> payList = stc.smartConstructTokenList(invalidAddress, "test", "200", null);
        List<Map> collList = stc.smartConstructTokenList(MULITADD4, "test", "200", null);
        String transferResp = st.SmartTransferReq(tokenType, payList, collList, "", "", transferData);
        assertEquals("400", JSONObject.fromObject(transferResp).getString("state"));

        //paymentList.amount数量超出余额
        payList.clear();
        payList = stc.smartConstructTokenList(ADDRESS1, "test", "300", null);
        transferResp = st.SmartTransferReq(tokenType, payList, collList, "", "", transferData);
        assertEquals("400", JSONObject.fromObject(transferResp).getString("state"));

        //collectionList.amount数量超出支出金额
        payList.clear();
        payList = stc.smartConstructTokenList(ADDRESS1, "test", "200", null);
        collList.clear();
        collList = stc.smartConstructTokenList(MULITADD4, "test", "300", null);
        transferResp = st.SmartTransferReq(tokenType, payList, collList, "", "", transferData);
        assertEquals("400", JSONObject.fromObject(transferResp).getString("state"));


    }

    /**
     * token未发行、未审核通过、冻结状态，转换申请失败
     */
    @Test
    public void TC_exchangeTokenInvalid() throws Exception {

        //token未发行
        tokenType = System.currentTimeMillis() + "";
        newTokenType = "NEW" + tokenType;
        String exchangeData = "token 向 newtoken 转换";
        List<Map> payList = stc.smartConstructTokenList(ADDRESS1, "test", "200", null);
        List<Map> collList = stc.smartConstructTokenList(MULITADD4, "test", "200", null);
        String exchangeResp = st.SmartExchangeReq(tokenType, payList, collList, newTokenType, "", exchangeData);
        assertEquals("400", JSONObject.fromObject(exchangeResp).getString("state"));

        //未审核状态
        tokenType = "TB_" + UtilsClass.Random(10);
        double timeStampNow = System.currentTimeMillis();
        BigDecimal deadline = new BigDecimal(timeStampNow + 12356789);
        List<Map> list = stc.smartConstructTokenList(ADDRESS1, "test", "200", null);
        String issueResp = st.SmartIssueTokenReq
                (smartAccoutContractAddress, tokenType, deadline, list, new BigDecimal(0), true, 0, "");
        assertEquals("200", JSONObject.fromObject(issueResp).getString("state"));
        collList.clear();
        collList = stc.smartConstructTokenList(ADDRESS1, "test", "200", null);
        exchangeResp = st.SmartExchangeReq(tokenType, payList, collList, newTokenType, "", exchangeData);
        assertEquals("400", JSONObject.fromObject(exchangeResp).getString("state"));

        //冻结状态
        tokenType = stc.beforeConfigIssueNewToken("200");
        String freezeResp = st.SmartFreeze(tokenType, "");
        assertEquals("200", JSONObject.fromObject(freezeResp).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType, SLEEPTIME);

        exchangeResp = stc.smartExchange(tokenType, payList, collList, newTokenType, "", exchangeData);
        assertEquals("200", JSONObject.fromObject(exchangeResp).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType, SLEEPTIME);

        log.info("查询 ADDRESS1 和 MULITADD4 余额，判断转账是否成功");
        stc.verifyAddressHasBalance(ADDRESS1, tokenType, "200");
        stc.verifyAddressNoBalance(ADDRESS1, newTokenType);

    }


    /**
     * paymentList/collectionList列表数据异常，转换申请失败
     */
    @Test
    public void TC_exchangePayCollListInvalid() throws Exception {

        //paymentList.address地址错误
        tokenType = stc.beforeConfigIssueNewToken("200");
        newTokenType = "New" + tokenType;
        String exchangeResp = "token 向 newtoken 转换";
        List<Map> payList = stc.smartConstructTokenList(invalidAddress, "test", "200", null);
        List<Map> collList = stc.smartConstructTokenList(MULITADD4, "test", "200", null);
        String transferResp = st.SmartExchangeReq(tokenType, payList, collList, newTokenType, "", exchangeResp);
        assertEquals("400", JSONObject.fromObject(transferResp).getString("state"));

        //paymentList.amount数量不等于余额
        payList.clear();
        payList = stc.smartConstructTokenList(ADDRESS1, "test", "300", null);
        transferResp = st.SmartExchangeReq(tokenType, payList, collList, newTokenType, "", exchangeResp);
        assertEquals("400", JSONObject.fromObject(transferResp).getString("state"));

        //collectionList.amount数量超出支出金额
        payList.clear();
        payList = stc.smartConstructTokenList(ADDRESS1, "test", "200", null);
        collList.clear();
        collList = stc.smartConstructTokenList(MULITADD4, "test", "300", null);
        transferResp = st.SmartExchangeReq(tokenType, payList, collList, newTokenType, "", exchangeResp);
        assertEquals("400", JSONObject.fromObject(transferResp).getString("state"));


    }


    /**
     * 支出和收入金额不匹配，转换申请失败
     */
    @Test
    public void TC_exchangeAmountMismatch() throws Exception {

        //发行
        tokenType = stc.beforeConfigIssueNewToken("200");

        //paymentList.amount大于该地址的token余额
        newTokenType = "New" + tokenType;
        String exchangeResp = "token 向 newtoken 转换";
        List<Map> payList = stc.smartConstructTokenList(ADDRESS1, "test", "300", null);
        List<Map> collList = stc.smartConstructTokenList(ADDRESS1, "test", "300", null);
        String transferResp = st.SmartExchangeReq(tokenType, payList, collList, newTokenType, "", exchangeResp);
        assertEquals("400", JSONObject.fromObject(transferResp).getString("state"));

        //paymentList.amount小于该地址的token余额
        payList.clear();
        collList.clear();
        payList = stc.smartConstructTokenList(ADDRESS1, "test", "100", null);
        collList = stc.smartConstructTokenList(ADDRESS1, "test", "100", null);
//        transferResp= st.SmartExchangeReq(tokenType, payList, collList, newTokenType, "", exchangeResp);
        transferResp = stc.smartExchange(tokenType, payList, collList, newTokenType, "", exchangeResp);
        assertEquals("400", JSONObject.fromObject(transferResp).getString("state"));

    }
    /**
     * 过期转换 过期回收
     */
    @Test
    public void TC_expired() throws Exception {

        tokenType = "TB_" + UtilsClass.Random(10);
        double timeStampNow = System.currentTimeMillis();
        BigDecimal expiredDate = new BigDecimal(timeStampNow + 60000);
        BigDecimal activeDate = new BigDecimal(timeStampNow);
        String contractAddress = smartAccoutContractAddress;

        List<Map> toList = stc.smartConstructTokenList(ADDRESS1, "test", "100.15", null);

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
        stc.verifyAddressHasBalance(ADDRESS1, tokenType, "100.15");

        Thread.sleep(180000);//3分钟

        //转换
        String newTokenType = "NEW_" + UtilsClass.Random(10);
        String transferData = "ADDRESS1 向 MULITADD4 转换10个" + tokenType;
        List<Map> payList = stc.smartConstructTokenList(ADDRESS1, "test", "100.15", null);
        List<Map> collList = stc.smartConstructTokenList(MULITADD4, "test", "100.15", null);
        String transferResp1 = stc.smartExchange
                (tokenType, payList, collList, newTokenType, "", transferData);

        assertEquals("200", JSONObject.fromObject(transferResp1).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType, SLEEPTIME);

        log.info("查询 ADDRESS1 和 MULITADD4 余额，判断转账是否成功");
        stc.verifyAddressHasBalance(ADDRESS1, tokenType, "100.15");
        stc.verifyAddressNoBalance(MULITADD4,newTokenType);

        //回收
        String destroyData1 = "销毁 ADDRESS1 中的" + tokenType;
        List<Map> payList1 = stc.smartConstructTokenList(ADDRESS1, "test", "100.15", null);
        String destroyResp1 = stc.smartDestroy(tokenType, payList1, "", destroyData1);
        assertEquals("200", JSONObject.fromObject(destroyResp1).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType, SLEEPTIME);
        log.info("查询回收后账户余额是否为0");
        stc.verifyAddressNoBalance(ADDRESS1, tokenType);


        log.info("查询回收账户余额");
        stc.verifyAddressHasBalance(ZEROADDRESS, tokenType, "100.15");

    }
    /**
     * 未激活时转让（不可以转让）转换（不可以转换）回收（可以回收）
     */
    @Test
    public void TC_activeDate() throws Exception {

        tokenType = "TB_" + UtilsClass.Random(10);
        double timeStampNow = System.currentTimeMillis();
        BigDecimal expiredDate = new BigDecimal(timeStampNow + 240000);
        BigDecimal activeDate = new BigDecimal(timeStampNow + 120000);
        String contractAddress = smartAccoutContractAddress;

        List<Map> toList = stc.smartConstructTokenList(ADDRESS1, "test", "100.15", null);
        List<Map> payList = stc.smartConstructTokenList(ADDRESS1, "test", "100.15", null);
        List<Map> collList = stc.smartConstructTokenList(MULITADD4, "test", "100.15", null);
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
        stc.verifyAddressHasBalance(ADDRESS1, tokenType, "100.15");
        //转账
        String transferData = "ADDRESS1 向 MULITADD4 转账10个" + tokenType;
        String transferResp1= stc.smartTransfer(tokenType, payList, collList, "", "", transferData);
        assertEquals("200",JSONObject.fromObject(transferResp1).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        log.info("查询 ADDRESS1 和 MULITADD4 余额，判断转账是否成功");
        stc.verifyAddressHasBalance(ADDRESS1, tokenType, "100.15");
        stc.verifyAddressNoBalance(MULITADD4, tokenType);

        //转换
        String newTokenType = "NEW_" + UtilsClass.Random(10);
        String exchangeData = "ADDRESS1 向 MULITADD4 转换10个" + tokenType;

        String exchangeResp = stc.smartExchange
                (tokenType, payList, collList, newTokenType, "", exchangeData);

        assertEquals("200", JSONObject.fromObject(exchangeResp).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType, SLEEPTIME);

        log.info("查询 ADDRESS1 和 MULITADD4 余额，判断转账是否成功");
        stc.verifyAddressHasBalance(ADDRESS1, tokenType, "100.15");
        stc.verifyAddressNoBalance(MULITADD4,newTokenType);

        //回收
        String destroyData1 = "销毁 ADDRESS1 中的" + tokenType;
        List<Map> payList1 = stc.smartConstructTokenList(ADDRESS1, "test", "100.15", null);
        String destroyResp1 = stc.smartDestroy(tokenType, payList1, "", destroyData1);
        assertEquals("200", JSONObject.fromObject(destroyResp1).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType, SLEEPTIME);
        log.info("查询回收后账户余额是否为0");
        stc.verifyAddressNoBalance(ADDRESS1, tokenType);


        log.info("查询回收账户余额");
        stc.verifyAddressHasBalance(ZEROADDRESS, tokenType, "100.15");

    }


}
