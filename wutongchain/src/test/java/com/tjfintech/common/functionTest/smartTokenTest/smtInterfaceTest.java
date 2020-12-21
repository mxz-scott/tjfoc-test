package com.tjfintech.common.functionTest.smartTokenTest;

import com.tjfintech.common.*;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.functionTest.contract.WVMContractTest;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class smtInterfaceTest {
    TestBuilder testBuilder = TestBuilder.getInstance();
    GoSmartToken st = new GoSmartToken();
    UtilsClass utilsClass = new UtilsClass();
    SmartTokenCommon stc = new SmartTokenCommon();
    CommonFunc commonFunc = new CommonFunc();
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

    //转让申请必填字段验证
    @Test
    public void smtTransferApplyInterfaceTest() throws Exception {

        tokenType = stc.beforeConfigIssueNewToken("1000.25");
        List<Map> payList = stc.smartConstructTokenList(ADDRESS1, "test", "10", null);
        List<Map> collList = stc.smartConstructTokenList(MULITADD4, "test", "10", null);

        log.info("tokenType为空");
        String transferResp = st.SmartTransferReq("", payList, collList, "", "", "");
        assertEquals("400", JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true, transferResp.contains("数字资产类型为必填字段"));

        log.info("转出list不存在");
        payList.clear();
        transferResp = st.SmartTransferReq(tokenType, payList, collList, "", "", "");
        assertEquals("400", JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true, transferResp.contains("支出列表必须大于0项"));
        transferResp = st.SmartTransferReq(tokenType, null, collList, "", "", "");
        assertEquals("400", JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true, transferResp.contains("支出列表必须大于0项"));

        log.info("支出地址不存在");
        payList.clear();
        payList = stc.smartConstructTokenList("", "test", "10", null);
        transferResp = st.SmartTransferReq(tokenType, payList, collList, "", "", "");
        assertEquals("400", JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true, transferResp.contains("支出地址为必填字段"));

        log.info("支出金额不存在");
        payList.clear();
        payList = stc.smartConstructTokenList(ADDRESS1, "test", "", null);
        transferResp = st.SmartTransferReq(tokenType, payList, collList, "", "", "");
        assertEquals("400", JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true, transferResp.contains("支出金额为必填字段"));

        log.info("受让list不存在");
        payList.clear();
        payList = stc.smartConstructTokenList(ADDRESS1, "test", "10", null);
        collList.clear();
        transferResp = st.SmartTransferReq(tokenType, payList, collList, "", "", "");
        assertEquals("400", JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true, transferResp.contains("收入列表必须大于0项"));
        transferResp = st.SmartTransferReq(tokenType, payList, null, "", "", "");
        assertEquals("400", JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true, transferResp.contains("收入列表必须大于0项"));

        log.info("收入地址不存在");
        collList.clear();
        collList = stc.smartConstructTokenList("", "test", "10", null);
        transferResp = st.SmartTransferReq(tokenType, payList, collList, "", "", "");
        assertEquals("400", JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true, transferResp.contains("收入地址为必填字段"));

        log.info("收入金额不存在");
        collList.clear();
        collList = stc.smartConstructTokenList(MULITADD4, "test", "", null);
        transferResp = st.SmartTransferReq(tokenType, payList, collList, "", "", "");
        assertEquals("400", JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true, transferResp.contains("收入金额为必填字段"));

    }

    //转让/转换/回收审核必填字段验证
    @Test
    public void smtStokenApproveInterfaceTest() throws Exception {

        tokenType = stc.beforeConfigIssueNewToken("200");
        String invalidAddress = "SsPB7k7FFcTG3DbtCHPpn9n3op46pu4GVQ3SW2PxRWqanES6yP7";
        List<Map> payList = stc.smartConstructTokenList(ADDRESS1, "test", "200", null);
        List<Map> collList = stc.smartConstructTokenList(MULITADD4, "test", "200", null);
        String transferResp = st.SmartTransferReq(tokenType, payList, collList, "", "", "");
        assertEquals("200", JSONObject.fromObject(transferResp).getString("state"));
        String UTXOInfo = JSONObject.fromObject(transferResp).getJSONObject("data").getString("UTXOInfo");
        String signMsg = JSONObject.fromObject(transferResp).getJSONObject("data").getString("sigMsg");
        HashMap<String, Object> sigMsgmap = stc.smartContractApproveData(signMsg);
        String signAddress = sigMsgmap.get("signAddress").toString();
        List<String> pubkeys = (List<String>) sigMsgmap.get("pubkeys");
        List<String> signList = (List<String>) sigMsgmap.get("signList");
        List<Map> payInfoList = stc.smartConstructPayAddressInfoList(signAddress, pubkeys, signList, null);

        log.info("type为空");
        String approveResp = st.SmartTEDApprove("", payInfoList, UTXOInfo);
        assertEquals("400", JSONObject.fromObject(approveResp).getString("state"));
        assertEquals(true, approveResp.contains("交易类型为必填字段"));

        log.info("UTXOInfo为空");
        approveResp = st.SmartTEDApprove("transfer", payInfoList, "");
        assertEquals("400", JSONObject.fromObject(approveResp).getString("state"));
        assertEquals(true, approveResp.contains("UTXO结构为必填字段"));

        log.info("payAddressInfoList转让方信息列表为空");
        approveResp = st.SmartTEDApprove("transfer", null, UTXOInfo);
        assertEquals("400", JSONObject.fromObject(approveResp).getString("state"));
        assertEquals(true, approveResp.contains("支出账户的信息为必填字段"));

        log.info("payAddressInfoList.address转让方签名地址为空");
        payInfoList = stc.smartConstructPayAddressInfoList("", pubkeys, signList, null);
        approveResp = st.SmartTEDApprove("transfer", payInfoList, UTXOInfo);
        assertEquals("400", JSONObject.fromObject(approveResp).getString("state"));
        assertEquals(true, approveResp.contains("账户地址为必填字段"));

        log.info("payAddressInfoList.pubkeyList转让方签名地址公钥为空");
        payInfoList = stc.smartConstructPayAddressInfoList(signAddress, null, signList, null);
        approveResp = st.SmartTEDApprove("transfer", payInfoList, UTXOInfo);
        assertEquals("400", JSONObject.fromObject(approveResp).getString("state"));
        assertEquals(true, approveResp.contains("生成账户地址的公钥为必填字段"));

        log.info("payAddressInfoList.signList公钥签名后的数据为空");
        payInfoList = stc.smartConstructPayAddressInfoList(signAddress, pubkeys, null, null);
        approveResp = st.SmartTEDApprove("transfer", payInfoList, UTXOInfo);
        assertEquals("400", JSONObject.fromObject(approveResp).getString("state"));
        assertEquals(true, approveResp.contains("生成账户地址的公钥对应私钥的签名为必填字段"));


    }

    //转换申请必填字段验证
    @Test
    public void smtExchangeApplyInterfaceTest() throws Exception {

        String issueAmount = "100";
        tokenType = stc.beforeConfigIssueNewToken(issueAmount);
        String newTokenType = "NEW" + tokenType;
        List<Map> payList = stc.smartConstructTokenList(ADDRESS1, "test", issueAmount, null);
        List<Map> collList = stc.smartConstructTokenList(ADDRESS1, "test", issueAmount, null);

        log.info("tokenType为空");
        String transferResp = st.SmartExchangeReq("", payList, collList, newTokenType, "", "");
        assertEquals("400", JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true, transferResp.contains("数字资产类型为必填字段"));

        log.info("newTokenType为空");
        transferResp = st.SmartExchangeReq(tokenType, payList, collList, "", "", "");
        assertEquals("400", JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true, transferResp.contains("数字资产类型为必填字段"));

        log.info("转出list不存在");
        payList.clear();
        transferResp = st.SmartExchangeReq(tokenType, payList, collList, newTokenType, "", "");
        assertEquals("400", JSONObject.fromObject(transferResp).getString("state"));
//        assertEquals(true,transferResp.contains("支出列表必须大于0项"));
        transferResp = st.SmartExchangeReq(tokenType, null, collList, newTokenType, "", "");
        assertEquals("400", JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true, transferResp.contains("支出列表为必填字段"));

        log.info("支出地址不存在");
        payList.clear();
        payList = stc.smartConstructTokenList("", "test", issueAmount, null);
        transferResp = st.SmartExchangeReq(tokenType, payList, collList, newTokenType, "", "");
        assertEquals("400", JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true, transferResp.contains("支出地址为必填字段"));

        log.info("支出金额不存在");
        payList.clear();
        payList = stc.smartConstructTokenList(ADDRESS1, "test", "", null);
        transferResp = st.SmartExchangeReq(tokenType, payList, collList, newTokenType, "", "");
        assertEquals("400", JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true, transferResp.contains("支出金额为必填字段"));

        log.info("受让list不存在");
        payList.clear();
        payList = stc.smartConstructTokenList(ADDRESS1, "test", issueAmount, null);
        collList.clear();
        transferResp = st.SmartExchangeReq(tokenType, payList, collList, newTokenType, "", "");
        assertEquals("400", JSONObject.fromObject(transferResp).getString("state"));
//        assertEquals(true,transferResp.contains("收入列表必须大于0项"));
        transferResp = st.SmartExchangeReq(tokenType, payList, null, newTokenType, "", "");
        assertEquals("400", JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true, transferResp.contains("收入列表为必填字段"));

        log.info("收入地址不存在");
        collList.clear();
        collList = stc.smartConstructTokenList("", "test", issueAmount, null);
        transferResp = st.SmartExchangeReq(tokenType, payList, collList, newTokenType, "", "");
        assertEquals("400", JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true, transferResp.contains("收入地址为必填字段"));

        log.info("收入金额不存在");
        collList.clear();
        collList = stc.smartConstructTokenList(ADDRESS1, "test", "", null);
        transferResp = st.SmartExchangeReq(tokenType, payList, collList, newTokenType, "", "");
        assertEquals("400", JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true, transferResp.contains("收入金额为必填字段"));

    }

    //发行申请必填字段验证及有效期最大流转层级的验证

    @Test
    public void smtissueApplyTest() throws Exception {

        tokenType = "TB_" + UtilsClass.Random(10);
        double timeStampNow = System.currentTimeMillis();
        BigDecimal expiredDate = new BigDecimal(timeStampNow + 12356789);
        BigDecimal activeDate = new BigDecimal(timeStampNow);
        String contractAddress = smartAccoutContractAddress;
        List<Map> toList = stc.smartConstructTokenList(ADDRESS1, "test", "10", null);
        log.info("合约为空");
        String IssueApplyResp = st.SmartIssueTokenReq("", tokenType, expiredDate, toList, activeDate, true, 0, "");
        assertEquals("400", JSONObject.fromObject(IssueApplyResp).getString("state"));
        assertEquals(true, IssueApplyResp.contains("账户合约地址为必填字段"));


        log.info("tokenType为空");

        IssueApplyResp= st.SmartIssueTokenReq(contractAddress,"", expiredDate, toList,activeDate,true,0,"");
        assertEquals("400",JSONObject.fromObject(IssueApplyResp).getString("state"));
        assertEquals(true,IssueApplyResp.contains("数字资产类型为必填字段"));


        log.info("数字资产有效期时间戳为空");
        IssueApplyResp= st.SmartIssueTokenReq(contractAddress,tokenType, null,toList,activeDate,true,0,"");
        assertEquals("400",JSONObject.fromObject(IssueApplyResp).getString("state"));
        assertEquals(true,IssueApplyResp.contains("数字资产有效期时间戳为必填字段"));

        String IssueApplyResp1 = st.SmartIssueTokenReq(contractAddress, "", expiredDate, toList, activeDate, true, 0, "");
        assertEquals("400", JSONObject.fromObject(IssueApplyResp1).getString("state"));
        assertEquals(true, IssueApplyResp1.contains("数字资产类型为必填字段"));


        log.info("tokenList不存在");
        toList.clear();

        IssueApplyResp= st.SmartIssueTokenReq(contractAddress,tokenType,expiredDate,toList,activeDate,true,0,"");
        assertEquals("400",JSONObject.fromObject(IssueApplyResp).getString("state"));
        assertEquals(true,IssueApplyResp.contains("数字资产接收信息必须至少包含1项"));

        log.info("接收金额为空");
        toList.clear();
        toList = stc.smartConstructTokenList(ADDRESS1, "test", "",null);
        IssueApplyResp= st.SmartIssueTokenReq(contractAddress,tokenType,expiredDate,toList,activeDate,true,0,"");
        assertEquals("400",JSONObject.fromObject(IssueApplyResp).getString("state"));
        assertEquals(true,IssueApplyResp.contains("接收金额为必填字段"));

        log.info("受让地址为空");
        toList.clear();
        toList = stc.smartConstructTokenList("", "test", "10",null);
        IssueApplyResp= st.SmartIssueTokenReq(contractAddress,tokenType,expiredDate,toList,activeDate,true,0,"");
        assertEquals("400",JSONObject.fromObject(IssueApplyResp).getString("state"));
        assertEquals(true,IssueApplyResp.contains("接收地址为必填字段"));

        log.info("最大流转层级为1");
        toList.clear();
        toList = stc.smartConstructTokenList(ADDRESS1, "test", "10",null);
        IssueApplyResp= st.SmartIssueTokenReq(contractAddress,tokenType,expiredDate,toList,activeDate,true,1,"");
        assertEquals("400",JSONObject.fromObject(IssueApplyResp).getString("state"));
        assertEquals(true,IssueApplyResp.contains("可流转层级必须大于1"));

        log.info("最大流转层级为负数");
        toList.clear();
        toList = stc.smartConstructTokenList(ADDRESS1, "test", "10",null);
        IssueApplyResp= st.SmartIssueTokenReq(contractAddress,tokenType,expiredDate,toList,activeDate,true,-1,"");
        assertEquals("400",JSONObject.fromObject(IssueApplyResp).getString("state"));
        assertEquals(true,IssueApplyResp.contains("可流转层级必须大于1"));

        String IssueApplyResp3 = st.SmartIssueTokenReq(contractAddress, tokenType, expiredDate, toList, activeDate, true, 0, "");
        assertEquals("400", JSONObject.fromObject(IssueApplyResp3).getString("state"));
        assertEquals(true, IssueApplyResp3.contains("数字资产接收信息必须至少包含1项"));


        log.info("数字资产有效期小于数字资产激活日期时间戳");
        BigDecimal expiredDate1 = new BigDecimal(timeStampNow - 12356789);

        BigDecimal activeDate1= new BigDecimal(timeStampNow );
        toList = stc.smartConstructTokenList(ADDRESS1, "test", "10",null);
        IssueApplyResp= st.SmartIssueTokenReq(contractAddress,tokenType,expiredDate1,toList,activeDate1,true,0,"");
        assertEquals("400",JSONObject.fromObject(IssueApplyResp).getString("state"));
        assertEquals(true,IssueApplyResp.contains("数字资产有效期时间戳必须大于数字资产激活日期"));

    }

    //发行审核必填字段验证
    @Test

    public void smtissueapproveTest()throws Exception {
        tokenType = "TB_" + UtilsClass.Random(10);
        double timeStampNow = System.currentTimeMillis();
        BigDecimal expiredDate = new BigDecimal(timeStampNow + 12356789);
        BigDecimal activeDate = new BigDecimal(timeStampNow);
        String contractAddress = smartAccoutContractAddress;
        List<Map> toList = stc.smartConstructTokenList(ADDRESS1, "test", "1000.25", null);
        //发行申请
        String IssueApplyResp = st.SmartIssueTokenReq(contractAddress, tokenType, expiredDate, toList, activeDate, true, 2, "");
        String sigMsg1 = JSONObject.fromObject(IssueApplyResp).getJSONObject("data").getString("sigMsg");
        String tempSM3Hash = certTool.getSm3Hash(PEER4IP, sigMsg1);
        String cryptMsg = certTool.sign(PEER4IP, PRIKEY1, "", tempSM3Hash, "hex");

        log.info("sigMsg为空");
        String approveResp = st.SmartIssueTokenApprove("", cryptMsg, PUBKEY1);
        assertEquals("400", JSONObject.fromObject(approveResp).getString("state"));
        assertEquals(true, approveResp.contains("签名原文为必填字段"));


        log.info("sigCrypt为空");
        approveResp = st.SmartIssueTokenApprove(sigMsg1, "", PUBKEY1);
        assertEquals("400", JSONObject.fromObject(approveResp).getString("state"));
        assertEquals(true, approveResp.contains("签名数据为必填字段"));

        log.info("PUBKEY1为空");
        approveResp = st.SmartIssueTokenApprove(sigMsg1, cryptMsg, "");
        assertEquals("400", JSONObject.fromObject(approveResp).getString("state"));
        assertEquals(true, approveResp.contains("签名公钥为必填字段"));

        }

        //冻结资产必填字段验证
        @Test
        public void smtfreezeTest () throws Exception {
            log.info("tokentype 为空");
            String freezeResp = st.SmartFreeze("", "");
            assertEquals("400", JSONObject.fromObject(freezeResp).getString("state"));
            assertEquals(true, freezeResp.contains("数字资产类型为必填字段"));
        }

        //解冻资产必填字段验证
        @Test
        public void smtrecoverTest () throws Exception {
            log.info("tokentype 为空");
            String freezeResp = st.SmartRecover("", "");
            assertEquals("400", JSONObject.fromObject(freezeResp).getString("state"));
            assertEquals(true, freezeResp.contains("数字资产类型为必填字段"));

        }
    }