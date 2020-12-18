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
    public static void BeforeClass()throws Exception{
        if (MULITADD1.isEmpty()) {
            BeforeCondition bf = new BeforeCondition();
            bf.updatePubPriKey();
            bf.createSTAddresses();
            bf.installSmartAccountContract("account_simple.wlang");
        }

    }


    @Test
    public void smtTransferApplyInterfaceTest()throws Exception{

        tokenType =  stc.beforeConfigIssueNewToken("1000.25");
        String invalidAddress = "SsPB7k7FFcTG3DbtCHPpn9n3op46pu4GVQ3SW2PxRWqanES6yP7";
        List<Map> payList= stc.smartConstructTokenList(ADDRESS1, "test", "10",null);
        List<Map> collList= stc.smartConstructTokenList(MULITADD4,"test", "10",null);

        log.info("tokenType为空");
        String transferResp= st.SmartTransferReq("", payList, collList, "", "", "");
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true,transferResp.contains("数字资产类型为必填字段"));

        log.info("转出list不存在");
        payList.clear();
        transferResp= st.SmartTransferReq(tokenType, payList, collList, "", "", "");
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true,transferResp.contains("token list  invalid!"));

        log.info("支出地址不存在");
        payList.clear();
        payList= stc.smartConstructTokenList("", "test", "10",null);
        transferResp= st.SmartTransferReq(tokenType, payList, collList, "", "", "");
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true,transferResp.contains("支出地址为必填字段"));

        log.info("支出金额不存在");
        payList.clear();
        payList= stc.smartConstructTokenList(ADDRESS1, "test", "",null);
        transferResp= st.SmartTransferReq(tokenType, payList, collList, "", "", "");
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true,transferResp.contains("支出金额为必填字段"));

        log.info("受让list不存在");
        payList.clear();
        payList= stc.smartConstructTokenList(ADDRESS1, "test", "10",null);
        collList.clear();
        transferResp= st.SmartTransferReq(tokenType, payList, collList, "", "", "");
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true,transferResp.contains("token list  invalid!"));

        log.info("收入地址不存在");
        collList.clear();
        collList= stc.smartConstructTokenList("", "test", "10",null);
        transferResp= st.SmartTransferReq(tokenType, payList, collList, "", "", "");
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true,transferResp.contains("收入地址为必填字段"));

        log.info("收入金额不存在");
        collList.clear();
        collList= stc.smartConstructTokenList(MULITADD4, "test", "",null);
        transferResp= st.SmartTransferReq(tokenType, payList, collList, "", "", "");
        assertEquals("400",JSONObject.fromObject(transferResp).getString("state"));
        assertEquals(true,transferResp.contains("收入金额为必填字段"));

    }

    //发行申请必填字段验证及有效期最大流转层级数值的增发验证
    @Test
    public void smtissueApplyTest()throws Exception{

        tokenType = "TB_" + UtilsClass.Random(10);
        double timeStampNow = System.currentTimeMillis();
        BigDecimal expiredDate = new BigDecimal(timeStampNow + 12356789);
        BigDecimal activeDate = new BigDecimal(timeStampNow );
        String contractAddress = smartAccoutContractAddress;
        List<Map> toList = stc.smartConstructTokenList(ADDRESS1, "test", "10",null);
        log.info("合约为空");
        String IssueApplyResp= st.SmartIssueTokenReq("", tokenType, expiredDate, toList,activeDate,true,0,"");
        assertEquals("400",JSONObject.fromObject(IssueApplyResp).getString("state"));
        assertEquals(true,IssueApplyResp.contains("账户合约地址为必填字段"));


        log.info("tokenType为空");
        String IssueApplyResp1= st.SmartIssueTokenReq(contractAddress,"", expiredDate, toList,activeDate,true,0,"");
        assertEquals("400",JSONObject.fromObject(IssueApplyResp1).getString("state"));
        assertEquals(true,IssueApplyResp1.contains("数字资产类型为必填字段"));


        log.info("数字资产有效期时间戳为空");
        String IssueApplyResp2= st.SmartIssueTokenReq(contractAddress,tokenType, null,toList,activeDate,true,0,"");
        assertEquals("400",JSONObject.fromObject(IssueApplyResp2).getString("state"));
        assertEquals(true,IssueApplyResp2.contains("数字资产有效期时间戳为必填字段"));


        log.info("tokenList不存在");
        toList.clear();
        String IssueApplyResp3= st.SmartIssueTokenReq(contractAddress,tokenType,expiredDate,toList,activeDate,true,0,"");
        assertEquals("400",JSONObject.fromObject(IssueApplyResp3).getString("state"));
        assertEquals(true,IssueApplyResp3.contains("数字资产接收信息必须至少包含1项"));

        log.info("接收金额为空");
        toList.clear();
        toList = stc.smartConstructTokenList(ADDRESS1, "test", "",null);
        String IssueApplyResp4= st.SmartIssueTokenReq(contractAddress,tokenType,expiredDate,toList,activeDate,true,0,"");
        assertEquals("400",JSONObject.fromObject(IssueApplyResp4).getString("state"));
        assertEquals(true,IssueApplyResp4.contains("接收金额为必填字段"));

        log.info("受让地址为空");
        toList.clear();
        toList = stc.smartConstructTokenList("", "test", "10",null);
        String IssueApplyResp5= st.SmartIssueTokenReq(contractAddress,tokenType,expiredDate,toList,activeDate,true,0,"");
        assertEquals("400",JSONObject.fromObject(IssueApplyResp5).getString("state"));
        assertEquals(true,IssueApplyResp5.contains("接收地址为必填字段"));

        log.info("最大流转层级为1");
        toList.clear();
        toList = stc.smartConstructTokenList(ADDRESS1, "test", "10",null);
        String IssueApplyResp6= st.SmartIssueTokenReq(contractAddress,tokenType,expiredDate,toList,activeDate,true,1,"");
        assertEquals("400",JSONObject.fromObject(IssueApplyResp6).getString("state"));
        assertEquals(true,IssueApplyResp6.contains("可流转层级必须大于1"));

        log.info("最大流转层级为负数");
        toList.clear();
        toList = stc.smartConstructTokenList(ADDRESS1, "test", "10",null);
        String IssueApplyResp8= st.SmartIssueTokenReq(contractAddress,tokenType,expiredDate,toList,activeDate,true,-1,"");
        assertEquals("400",JSONObject.fromObject(IssueApplyResp8).getString("state"));
        assertEquals(true,IssueApplyResp8.contains("可流转层级必须大于1"));


        log.info("数字资产有效期小于数字资产激活日期时间戳");
        BigDecimal expiredDate1 = new BigDecimal(timeStampNow - 12356789);
        BigDecimal activeDate1= new BigDecimal(timeStampNow );
        toList = stc.smartConstructTokenList(ADDRESS1, "test", "10",null);
        String IssueApplyResp7= st.SmartIssueTokenReq(contractAddress,tokenType,expiredDate1,toList,activeDate1,true,0,"");
        assertEquals("400",JSONObject.fromObject(IssueApplyResp7).getString("state"));
        assertEquals(true,IssueApplyResp7.contains("数字资产有效期时间戳必须大于数字资产激活日期"));


        log.info("不支持增发");
        //第一次发行
        //发行申请
        String IssueApplyResp9= st.SmartIssueTokenReq(contractAddress,"TB_1113",expiredDate,toList,activeDate,false ,0,"");
        String sigMsg1 = JSONObject.fromObject(IssueApplyResp9).getJSONObject("data").getString("sigMsg");
        //发行审核
        String tempSM3Hash = certTool.getSm3Hash(PEER4IP, sigMsg1);
        String cryptMsg = certTool.sign(PEER4IP, PRIKEY1, "", tempSM3Hash, "hex");
        String approveResp = st.SmartIssueTokenApprove(sigMsg1, cryptMsg, PUBKEY1);
        assertEquals("200", JSONObject.fromObject( approveResp).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash( approveResp, utilsClass.sdkGetTxHashType21),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        // 验证第一次发行查询账户余额
        log.info("查询发行的账户余额");
        stc.verifyAddressHasBalance(ADDRESS1, "TB_1113", "10");

        //第二次发行
        toList.clear();
        List<Map> toList1 = stc.smartConstructTokenList(ADDRESS1, "test", "20",null);
        String IssueApplyResp10= st.SmartIssueTokenReq(contractAddress,"TB_1113",expiredDate,toList1,activeDate,false ,0,"");
        String sigMsg2= JSONObject.fromObject(IssueApplyResp10).getJSONObject("data").getString("sigMsg");
        String tempSM3Hash1= certTool.getSm3Hash(PEER4IP, sigMsg2);
        String cryptMsg1 = certTool.sign(PEER4IP, PRIKEY1, "", tempSM3Hash1, "hex");
        String approveResp1= st.SmartIssueTokenApprove(sigMsg2, cryptMsg1, PUBKEY1);
        assertEquals("200", JSONObject.fromObject( approveResp1).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash( approveResp1, utilsClass.sdkGetTxHashType21),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        //验证第二次发行后账户余额应该不变【因为链上不会报错，所以这边验证余额】
        log.info("查询发行的账户余额");
        stc.verifyAddressHasBalance(ADDRESS1, "TB_1113", "10");

    }









    }
