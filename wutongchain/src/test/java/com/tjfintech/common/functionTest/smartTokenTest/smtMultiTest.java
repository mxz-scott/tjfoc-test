package com.tjfintech.common.functionTest.smartTokenTest;

import com.tjfintech.common.*;
import com.tjfintech.common.Interface.Store;
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
    TestBuilder testBuilder = TestBuilder.getInstance();
    Store store = testBuilder.getStore();

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
    public void TC001_multiProgress() throws Exception {

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
    public void TC002_exchange() throws Exception {

        //发行
        tokenType = stc.beforeConfigIssueNewToken("200");
        String newTokenType = "NEW_" + UtilsClass.Random(10);

        //转换
        String transferData = tokenType + "转化为" + newTokenType;
        List<Map> payList = stc.smartConstructTokenList(ADDRESS1, "test", "200", null);
        String exchangeResp = stc.smartExchange
                (tokenType, payList,  newTokenType, "", transferData);

        assertEquals("200", JSONObject.fromObject(exchangeResp).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType, SLEEPTIME);

        log.info("查询 ADDRESS1 和 MULITADD4 余额，判断转账是否成功");
        stc.verifyAddressNoBalance(ADDRESS1, tokenType);
        stc.verifyAddressHasBalance(ADDRESS1, newTokenType, "200");

    }

    /**
     * 连续向单签和多签地址转让2次
     */
    @Test
    public void TC003_transferSoloMulti() throws Exception {

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

    public void TC004_MAXleveltransfer() throws Exception {
        tokenType = "TB_" + UtilsClass.Random(10);


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
     * 增发资产
     */

    @Test
    public void TC005_reissedTruetest() throws Exception {

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
    public void TC006_recovertransfer() throws Exception {

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
        //转账失败
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
        //转账
        String transferResp1 = stc.smartTransfer(tokenType, payList, collList, "", "", transferData);
        assertEquals("200", JSONObject.fromObject(transferResp1).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType, SLEEPTIME);

        log.info("查询 ADDRESS1 和 MULITADD4 余额，判断转账是否成功");
        stc.verifyAddressHasBalance(ADDRESS1, tokenType, "90.75");
        stc.verifyAddressHasBalance(MULITADD4, tokenType, "10");

        //回收
        String destroyData1 = "销毁 ADDRESS1 中的" + tokenType;
        String destroyData2 = "销毁 MULITADD4 中的" + tokenType;
        List<Map> payList1 = stc.smartConstructTokenList(ADDRESS1, "test", "90.75", null);
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
        stc.verifyAddressHasBalance(ZEROADDRESS, tokenType, "100.75");

    }

    /**
     * 发行 - 冻结 - 部分回收 - 转换 -解冻 -转换[验证点：冻结后可回收/冻结后不可转换/解冻后可转换]
     */
    @Test
    public void TC007_freezeDestroy() throws Exception {

        // 发行
        tokenType = stc.beforeConfigIssueNewToken("100.75");
        //冻结

        String freezeResp = st.SmartFreeze(tokenType, "");
        assertEquals("200", JSONObject.fromObject(freezeResp).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType, SLEEPTIME);

        //部分回收
        String destroyData1 = "销毁 ADDRESS1 中的" + tokenType;

        List<Map> payList1 = stc.smartConstructTokenList(ADDRESS1, "test", "90.75", null);


        String destroyResp1 = stc.smartDestroy(tokenType, payList1, "", destroyData1);
        assertEquals("200", JSONObject.fromObject(destroyResp1).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType, SLEEPTIME);
        log.info("查询回收后账户余额是否为10");
        stc.verifyAddressHasBalance(ADDRESS1, tokenType, "10");
        log.info("查询回收账户余额");
        stc.verifyAddressHasBalance(ZEROADDRESS, tokenType, "90.75");
        //剩余资产转换
        String newTokenType = "NEW_" + UtilsClass.Random(10);
        String transferData = "ADDRESS1 向 MULITADD4 转换10个" + tokenType;
        List<Map> payList = stc.smartConstructTokenList(ADDRESS1, "test", "10", null);
        String exchangeResp = stc.smartExchange(tokenType, payList,  newTokenType, "", transferData);

        assertEquals("200", JSONObject.fromObject(exchangeResp).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType, SLEEPTIME);

        log.info("查询 ADDRESS1余额，判断转换是否成功");
        stc.verifyAddressHasBalance(ADDRESS1, tokenType, "10");
        //解冻
        String recoverResp = st.SmartRecover(tokenType, "");
        assertEquals("200", JSONObject.fromObject(recoverResp).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType, SLEEPTIME);
        //转换
        String exchangeResp1 = stc.smartExchange
                (tokenType,payList,  newTokenType, "", transferData);

        assertEquals("200", JSONObject.fromObject(exchangeResp1).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType, SLEEPTIME);

        log.info("查询 ADDRESS1 余额，判断转账是否成功");
        stc.verifyAddressNoBalance(ADDRESS1, tokenType);
        stc.verifyAddressHasBalance(ADDRESS1, newTokenType, "10");

    }

    /**
     * 单签回收、多签回收、单多签同时回收、冻结后回收
     */
    @Test
    public void TC008_destroySoloMulti() throws Exception {

        //发行
        tokenType = stc.beforeConfigIssueNewToken("200");
        sleepAndSaveInfo(2000);

        //转账
        String transferData = "ADDRESS1 向 MULITADD4 转账100.123456个" + tokenType;
        List<Map> payList = stc.smartConstructTokenList(ADDRESS1, "test", "100.123456", null);
        List<Map> collList = stc.smartConstructTokenList(MULITADD4, "test", "100.123456", null);
        String transferResp = stc.smartTransfer(tokenType, payList, collList, "", "", transferData);

        assertEquals("200", JSONObject.fromObject(transferResp).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType, SLEEPTIME);

        log.info("查询 ADDRESS1 和 MULITADD4 余额，判断转账是否成功");
        stc.verifyAddressHasBalance(ADDRESS1, tokenType, "99.876544");
        stc.verifyAddressHasBalance(MULITADD4, tokenType, "100.123456");

        //ADDRESS1回收50.000044
        payList.clear();
        payList = stc.smartConstructTokenList(ADDRESS1, "test", "50.000044", null);
        String destroyResp = stc.smartDestroy(tokenType, payList, "", "");
        assertEquals("200", JSONObject.fromObject(destroyResp).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType, SLEEPTIME);

        stc.verifyAddressHasBalance(ADDRESS1, tokenType, "49.8765");
        stc.verifyAddressHasBalance(MULITADD4, tokenType, "100.123456");
        stc.verifyAddressHasBalance(ZEROADDRESS, tokenType, "50.000044");

        //MULITADD4回收50.023456
        payList.clear();
        payList = stc.smartConstructTokenList(MULITADD4, "test", "50.023456", null);
        destroyResp = stc.smartDestroy(tokenType, payList, "", "");
        assertEquals("200", JSONObject.fromObject(destroyResp).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType, SLEEPTIME);

        stc.verifyAddressHasBalance(ADDRESS1, tokenType, "49.8765");
        stc.verifyAddressHasBalance(MULITADD4, tokenType, "50.1");
        stc.verifyAddressHasBalance(ZEROADDRESS, tokenType, "100.0235");

        //ADDRESS1/MULITADD4同时回收20
        payList.clear();
        payList = stc.smartConstructTokenList(ADDRESS1, "test", "20", null);
        List<Map> payList2 = stc.smartConstructTokenList(MULITADD4, "test", "20", payList);
        destroyResp = stc.smartDestroy(tokenType, payList2, "", "");
        assertEquals("200", JSONObject.fromObject(destroyResp).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType, SLEEPTIME);

        stc.verifyAddressHasBalance(ADDRESS1, tokenType, "29.8765");
        stc.verifyAddressHasBalance(MULITADD4, tokenType, "30.1");
        stc.verifyAddressHasBalance(ZEROADDRESS, tokenType, "140.0235");

        //冻结tokentype
        String freezeResp = st.SmartFreeze(tokenType,"");
        assertEquals("200", JSONObject.fromObject(freezeResp).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType, SLEEPTIME);

        //ADDRESS1/MULITADD4同时回收20
        payList.clear();
        payList2.clear();
        payList = stc.smartConstructTokenList(ADDRESS1, "test", "29.8765", null);
        payList2 = stc.smartConstructTokenList(MULITADD4, "test", "30.1", payList);
        destroyResp = stc.smartDestroy(tokenType, payList2, "", "");
        assertEquals("200", JSONObject.fromObject(destroyResp).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType, SLEEPTIME);

        stc.verifyAddressNoBalance(ADDRESS1, tokenType);
        stc.verifyAddressNoBalance(MULITADD4, tokenType);
        stc.verifyAddressHasBalance(ZEROADDRESS, tokenType, "200");

    }

    /**
     * smt接口TxDetail、TxRaw验证测试
     */
    @Test
    public void TC009_smtProgressVerifyTest() throws Exception {

        tokenType = utilsClass.Random(8);
        String newTokenType = "NEW" + utilsClass.Random(8);

        //发行
        double timeStampNow = System.currentTimeMillis();
        BigDecimal deadline = new BigDecimal(timeStampNow + 12356789);
        List<Map> list = stc.smartConstructTokenList(ADDRESS1, "test", "200.123456", null);
        String response = stc.smartIssueToken(tokenType, deadline, list, true, 0, "");
        String responseHash = JSONObject.fromObject(response).getString("data");
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(response, utilsClass.sdkGetTxHashType21),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        stc.verifyAddressHasBalance(ADDRESS1, tokenType, "200.123456");
        commonFunc.verifyTxDetailField(responseHash,"smt_issue","2","3","43");
        commonFunc.verifyTxRawField(responseHash, "2","3","43");
        commonFunc.verifyRawFieldMatch(responseHash);
        String txDetail = store.GetTxDetail(responseHash);
        assertEquals("200.123456", JSONObject.fromObject(txDetail).getJSONObject("data").getJSONObject("wvm").
                getJSONArray("txRecords").getJSONObject(0).getString("amount"));

        //转让
        list = stc.smartConstructTokenList(ADDRESS1, "test", "10.12", null);
        List<Map> colllist = stc.smartConstructTokenList(MULITADD4, "test", "10.12", null);
        response = stc.smartTransfer(tokenType,list,colllist,"","","");
        responseHash = JSONObject.fromObject(response).getString("data");
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(response, utilsClass.sdkGetTxHashType21),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        stc.verifyAddressHasBalance(ADDRESS1, tokenType, "190.003456");
        stc.verifyAddressHasBalance(MULITADD4, tokenType, "10.12");
        commonFunc.verifyTxDetailField(responseHash,"smt_transfer","1","1","11");
        commonFunc.verifyTxRawField(responseHash, "1","1","11");
        commonFunc.verifyRawFieldMatch(responseHash);
        txDetail = store.GetTxDetail(responseHash);
        assertEquals("10.12", JSONObject.fromObject(txDetail).getJSONObject("data").getJSONObject("utxo").
                getJSONArray("txRecords").getJSONObject(1).getString("amount"));

        //销毁
        list.clear();
        list = stc.smartConstructTokenList(ADDRESS1, "test", "10.0034", null);
        response = stc.smartDestroy(tokenType, list, "", "");
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        responseHash = JSONObject.fromObject(response).getString("data");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType, SLEEPTIME);
        stc.verifyAddressHasBalance(ADDRESS1, tokenType, "180.000056");
        stc.verifyAddressHasBalance(ZEROADDRESS, tokenType, "10.0034");
        commonFunc.verifyTxDetailField(responseHash,"smt_destroy","1","1","12");
        commonFunc.verifyTxRawField(responseHash, "1","1","12");
        commonFunc.verifyRawFieldMatch(responseHash);
        txDetail = store.GetTxDetail(responseHash);
        assertEquals("10.0034", JSONObject.fromObject(txDetail).getJSONObject("data").getJSONObject("utxo").
                getJSONArray("txRecords").getJSONObject(0).getString("amount"));

        //冻结
        response = st.SmartFreeze(tokenType,"");
        responseHash = JSONObject.fromObject(response).getString("data");
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType, SLEEPTIME);
        commonFunc.verifyTxDetailField(responseHash,"smt_freeze","2","3","42");
        commonFunc.verifyTxRawField(responseHash, "2","3","42");
        commonFunc.verifyRawFieldMatch(responseHash);

        //解冻
        response = st.SmartRecover(tokenType,"");
        responseHash = JSONObject.fromObject(response).getString("data");
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType, SLEEPTIME);
        commonFunc.verifyTxDetailField(responseHash,"smt_recover","2","3","42");
        commonFunc.verifyTxRawField(responseHash, "2","3","42");
        commonFunc.verifyRawFieldMatch(responseHash);

        //转换
        response = stc.smartExchange(tokenType, colllist,  newTokenType, "", "");
        responseHash = JSONObject.fromObject(response).getString("data");
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType, SLEEPTIME);
        stc.verifyAddressNoBalance(MULITADD4, tokenType);
        stc.verifyAddressHasBalance(MULITADD4, newTokenType, "10");
        commonFunc.verifyTxDetailField(responseHash,"smt_exchange","1","1","13");
        commonFunc.verifyTxRawField(responseHash, "1","1","13");
        commonFunc.verifyRawFieldMatch(responseHash);
        txDetail = store.GetTxDetail(responseHash);
        assertEquals("10.12", JSONObject.fromObject(txDetail).getJSONObject("data").getJSONObject("utxo").
                getJSONArray("txRecords").getJSONObject(0).getString("amount"));

    }

    /**
     * 过期资产转让
     */
    @Test
    public void TC010_expiredtransfer() throws Exception {

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
     *  双花验证
     */
    @Test
    public void TC011_doubleSpendVerify() throws Exception {

        //发行
        tokenType = stc.beforeConfigIssueNewToken("200");

        //转账
        String transferData = " 双花验证 " + tokenType;
        List<Map> payList = stc.smartConstructTokenList(ADDRESS1, "test", "30.5", null);
        List<Map> collList = stc.smartConstructTokenList(MULITADD4, "test", "30.5", null);
        String transferResp1 = stc.smartTransfer(tokenType, payList, collList, "", "", transferData);
        String transferResp2 = stc.smartTransfer(tokenType, payList, collList, "", "", transferData);

        assertEquals("200", JSONObject.fromObject(transferResp1).getString("state"));
        assertEquals("200", JSONObject.fromObject(transferResp2).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType, SLEEPTIME);

        log.info("查询 ADDRESS1 和 MULITADD4 余额，判断转账是否成功");
        stc.verifyAddressHasBalance(ADDRESS1, tokenType, "169.5");
        stc.verifyAddressHasBalance(MULITADD4, tokenType, "30.5");

    }

}
