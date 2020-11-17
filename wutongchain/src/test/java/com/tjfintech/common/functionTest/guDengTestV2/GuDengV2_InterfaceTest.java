package com.tjfintech.common.functionTest.guDengTestV2;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.GuDeng;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.util.StringUtils;

import java.util.*;

import static com.tjfintech.common.functionTest.guDengTestV2.GDCommonFunc.gdConstructShareList;
import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassGD.*;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class GuDengV2_InterfaceTest {

    TestBuilder testBuilder= TestBuilder.getInstance();
    GuDeng gd =testBuilder.getGuDeng();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    GDCommonFunc gdCF = new GDCommonFunc();
    GDUnitFunc uf = new GDUnitFunc();
    GDBeforeCondition gdBF = new GDBeforeCondition();
    Store store =testBuilder.getStore();

    @BeforeClass
    public static void Before()throws Exception{
        GDBeforeCondition gdBefore = new GDBeforeCondition();
        gdBefore.gdCreateAccout();
    }


    @Test
    public void enterpriseIssueInterfaceTest() throws Exception {
        //验证接口正常
        long shareTotals = 1000000;
        String response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,enterpriseSubjectInfo, equityProductInfo,bondProductInfo,fundProductInfo);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        //gdContractAddress为空
        response= gd.GDEnterpriseResister("",gdEquityCode,shareTotals,enterpriseSubjectInfo, equityProductInfo,bondProductInfo,fundProductInfo);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("Error:Field validation for 'ContractAddress' failed on the 'required"));

        //主体信息为空
        response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,null, equityProductInfo,bondProductInfo,fundProductInfo);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("机构主体信息中对象标识不可以为空"));

    }

    @Test
    public void createAccoutInterfaceMustParamTest() throws Exception {

        String cltNo = "IFCreateAcc" + Random(6);
        String shareHolderNo = "SH" + cltNo;
        String fundNo = "fund" + cltNo;

        //构造股权账户信息
        Map shareHolderInfo = new HashMap();
        gdBF.init02ShareholderAccountInfo();
        shAccountInfo.put("account_object_id",cltNo);  //更新账户对象标识字段
        log.info(shAccountInfo.toString());
        shareHolderInfo.put("shareholderNo",shareHolderNo);
        shareHolderInfo.put("accountInfo", shAccountInfo);
        log.info(shareHolderInfo.toString());

        //资金账户信息
        gdBF.init02FundAccountInfo();
        fundAccountInfo.put("account_object_id",cltNo);  //更新账户对象标识字段
        Map mapFundInfo = new HashMap();
        mapFundInfo.put("fundNo",fundNo);
        mapFundInfo.put("accountInfo", fundAccountInfo);

        //构造个人/投资者主体信息
        gdBF.init01PersonalSubjectInfo();
        investorSubjectInfo.put("letter_object_identification",cltNo);  //更新对象标识字段
        investorSubjectInfo.put("subject_id","sid" + cltNo);  //更新主体标识字段

        log.info(" ************************ test fundInfo not must ************************ ");
        String response = gd.GDCreateAccout(gdContractAddress,cltNo,null,shareHolderInfo, investorSubjectInfo);
//        assertEquals("200",JSONObject.fromObject(response).getString("state"));//当前存在bug

        log.info(" ************************ test contractAddress must ************************ ");
        response = gd.GDCreateAccout("",cltNo,mapFundInfo,shareHolderInfo, investorSubjectInfo);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'AccountCreateV2.ContractAddress' Error:Field validation for 'ContractAddress' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));


        log.info(" ************************ test clientNo must ************************ ");
        response = gd.GDCreateAccout(gdContractAddress,"",mapFundInfo,shareHolderInfo, investorSubjectInfo);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'AccountCreateV2.ClientNo' Error:Field validation for 'ClientNo' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));


        log.info(" ************************ test shareholderInfo must ************************ ");
        response = gd.GDCreateAccout(gdContractAddress,cltNo,mapFundInfo,null, investorSubjectInfo);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,JSONObject.fromObject(response).getString("message").contains(
                "无效的参数:Key: 'AccountCreateV2.ShareholderInfo.ShareholderNo' Error:Field validation for 'ShareholderNo' failed on the 'required' tag"));


        log.info(" ************************ test investorInfo must ************************ ");
        response = gd.GDCreateAccout(gdContractAddress,cltNo,mapFundInfo,shareHolderInfo, null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'AccountCreateV2.InvestorInfo' Error:Field validation for 'InvestorInfo' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));
    }


    @Test
    public void shareIssueInterfaceMustParamTest() throws Exception {
        String eqCode = "issue" + Random(6);
        long shareTotals = 1000000;
        //执行挂牌企业登记
        String response= gd.GDEnterpriseResister(gdContractAddress,eqCode,shareTotals,enterpriseSubjectInfo, equityProductInfo,bondProductInfo,fundProductInfo);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
        sleepAndSaveInfo(5000);
        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);

        log.info(" ************************ test platformKeyId must ************************ ");
        response= gd.GDShareIssue(gdContractAddress,"",eqCode,shareList);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'SharesRegisterV2.PlatformKeyId' Error:Field validation for 'PlatformKeyId' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));

        log.info(" ************************ test equityCode must ************************ ");
        response= gd.GDShareIssue(gdContractAddress,gdPlatfromKeyID,"",shareList);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'SharesRegisterV2.EquityCode' Error:Field validation for 'EquityCode' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));


        log.info(" ************************ test gdContractAddressess must ************************ ");
        response = gd.GDShareIssue("",gdPlatfromKeyID,eqCode,shareList);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'SharesRegisterV2.ContractAddress' Error:Field validation for 'ContractAddress' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));


        log.info(" ************************ test shareList must ************************ ");
        eqCode = "must" + Random(12);
        response = gd.GDShareIssue(gdContractAddress,gdPlatfromKeyID,eqCode,null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("请填写股权账号信息",JSONObject.fromObject(response).getString("message"));

        log.info(" ************************ test shareList.address must ************************ ");
        List<Map> shareListErr1 = gdConstructShareList("",1000,0);
        response = gd.GDShareIssue(gdContractAddress,gdPlatfromKeyID,eqCode,shareListErr1);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'SharesRegisterV2.ShareList[0].Address' Error:Field validation for 'Address' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));

        log.info(" ************************ test shareList.amount 0 ************************ ");
        List<Map> shareListErr2 = gdConstructShareList(gdAccount1,0,0);
        response = gd.GDShareIssue(gdContractAddress,gdPlatfromKeyID,eqCode,shareListErr2);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'SharesRegisterV2.ShareList[0].Amount' Error:Field validation for 'Amount' failed on the 'gt' tag",
                JSONObject.fromObject(response).getString("message"));

        log.info(" ************************ test shareList.amount must ************************ ");
        Map<String,Object> shares2 = new HashMap<>();
        shares2.put("address",gdAccount1);
        shares2.put("shareProperty",0);

        List<Map> shareList22 = new ArrayList<>();
        shareList22.add(shares2);
        response = gd.GDShareIssue(gdContractAddress,gdPlatfromKeyID,eqCode,shareList22);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'SharesRegisterV2.ShareList[0].Amount' Error:Field validation for 'Amount' failed on the 'gt' tag\n" +
                        "Key: 'SharesRegisterV2.ShareList[0].RegisterInformation' Error:Field validation for 'RegisterInformation' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));

        log.info(" ************************ test all must ************************ ");

        response = gd.GDShareIssue("","","",null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
    }

    @Test
    public void shareIncreaseInterfaceTest() throws Exception {

        gdEquityCode = "increaseEC" + Random(6);
        String eqCode = gdEquityCode;
        String reason = "股份分红";

        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        uf.shareIssue(gdEquityCode,shareList,true); //执行发行

        sleepAndSaveInfo(3000);

        //验证接口正常
        String response = uf.shareIncrease(eqCode,shareList,true);

        //platformKeyId为空
        response = gd.GDShareIncrease("",eqCode,shareList,reason, equityProductInfo);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("无效的参数"));

        //equityCode为空
        response= gd.GDShareIncrease(gdPlatfromKeyID,"",shareList,reason, equityProductInfo);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("无效的参数"));

        //shareList为空
        response= gd.GDShareIncrease(gdPlatfromKeyID,eqCode,null,reason, equityProductInfo);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("无效的参数"));

        //产品数据为空
        response= gd.GDShareIncrease(gdPlatfromKeyID,eqCode,shareList,reason, null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("产品主体信息中发行主体引用不可以为空"));

        response= gd.GDShareIncrease("","",null,"", null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("无效的参数"));


    }

    @Test
    public void shareLockInterfaceTest() throws Exception {

        gdEquityCode = "lockEC" + Random(6);
        String eqCode = gdEquityCode;
        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        uf.shareIssue(gdEquityCode,shareList,true); //执行发行

        sleepAndSaveInfo(3000);

        String bizNo = "test20200828001" + Random(10);
        String address = gdAccount1;
        long lockAmount = 500;
        int shareProperty = 0;
        String reason = "司法冻结";
        String cutoffDate = "2020-09-30";

        //验证接口正常
        String response= gd.GDShareLock(bizNo,address,eqCode,lockAmount,shareProperty,reason,cutoffDate,registerInfo);
        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        //bizNo为空
        bizNo = "test20200828001" + Random(10);
        response= gd.GDShareLock("",address,eqCode,lockAmount,shareProperty,reason,cutoffDate,registerInfo);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("Error:Field validation for 'BizNo' failed on the 'required"));

        //address为空
        response= gd.GDShareLock(bizNo,"",eqCode,lockAmount,shareProperty,reason,cutoffDate,registerInfo);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("Error:Field validation for 'Address' failed on the 'required"));

        //equityCode为空
        response= gd.GDShareLock(bizNo,address,"",lockAmount,shareProperty,reason,cutoffDate,registerInfo);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("Error:Field validation for 'EquityCode' failed on the 'required"));

//        //amount不传参
        response= gd.GDShareLock(bizNo,address,eqCode,0,shareProperty,reason,cutoffDate,registerInfo);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("Field validation for 'Amount' failed on the 'gt' tag"));
//
//        //shareProperty不传参
//        response= gd.GDShareLock(bizNo,address,eqCode,lockAmount,shareProperty,reason,cutoffDate);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("Error:Field validation for 'EquityCode' failed on the 'required"));

        //reason为空
        response= gd.GDShareLock(bizNo,address,eqCode,lockAmount,shareProperty,"",cutoffDate,registerInfo);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("Error:Field validation for 'Reason' failed on the 'required"));

        //cutoffDate为空
        response= gd.GDShareLock(bizNo,address,eqCode,lockAmount,shareProperty,reason,"",registerInfo);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("Error:Field validation for 'CutoffDate' failed on the 'required"));

        //registerInformation为空
        response= gd.GDShareLock(bizNo,address,eqCode,lockAmount,shareProperty,reason,cutoffDate,null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("Error:Field validation for 'RegisterInformation' failed on the 'required"));
    }

    @Test
    public void shareUnlockInterfaceTest() throws Exception {
        gdEquityCode = "unlockEC" + Random(6);
        String eqCode = gdEquityCode;
        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        uf.shareIssue(gdEquityCode,shareList,true); //执行发行

        sleepAndSaveInfo(3000);

        String bizNo = "test20200828001" + Random(10);
        String address = gdAccount1;
        long lockAmount = 500;
        long amount = 10;
        int shareProperty = 0;
        String reason = "司法冻结";
        String cutoffDate = "2020-09-30";

        //验证接口正常
        String response= gd.GDShareLock(bizNo,address,eqCode,lockAmount,shareProperty,reason,cutoffDate,registerInfo);
        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        sleepAndSaveInfo(3000);


        //验证接口正常
        response= gd.GDShareUnlock(bizNo,eqCode,amount,registerInfo);
        txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        //bizNo为空
        response= gd.GDShareUnlock("",eqCode,amount,registerInfo);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("Error:Field validation for 'BizNo' failed on the 'required"));

        //equityCode为空
        response= gd.GDShareUnlock(bizNo,"",amount,registerInfo);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("Error:Field validation for 'EquityCode' failed on the 'required"));

        //登记信息为空
        response= gd.GDShareUnlock(bizNo,eqCode,amount,null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("Error:Field validation for 'RegisterInformation' failed on the 'required"));

//        //amount为空
        response= gd.GDShareUnlock(bizNo,eqCode,0,registerInfo);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("无效的参数:Key: 'SharesUnLock.Amount' Error:Field validation for 'Amount' failed on the 'gt' tag"));

    }

    @Test
    public void shareTransferInterfaceMustParamTest() throws Exception {
        //转账前发行

        gdEquityCode = "unlockEC" + Random(6);
        String eqCode = gdEquityCode;
        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        uf.shareIssue(gdEquityCode,shareList,true); //执行发行

        sleepAndSaveInfo(3000);

        //转账接口测试
        String keyId = gdAccountKeyID1;
        String fromAddr = gdAccount1;
        String toAddr = gdAccount5;
        int shareProperty = 0;
        long transferAmount = 10;

        String tempObjIdFrom = mapAccAddr.get(gdAccount1).toString();
        String tempObjIdTo = mapAccAddr.get(gdAccount5).toString();

        Map fromNow = gdBF.init05RegInfo();
        Map toNow = gdBF.init05RegInfo();

        //执行交易
        String response= gd.GDShareTransfer(keyId,fromAddr,transferAmount,toAddr,shareProperty,eqCode,txInformation,fromNow,toNow);

        log.info(" ************************ test keyId must ************************ ");
        response= gd.GDShareTransfer("",fromAddr,transferAmount,toAddr,shareProperty,eqCode,txInformation,fromNow,toNow);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'SharesTransfer.KeyId' Error:Field validation for 'KeyId' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));

        log.info(" ************************ test fromAddress must ************************ ");
        response= gd.GDShareTransfer(keyId,"",transferAmount,toAddr,shareProperty,eqCode,txInformation,fromNow,toNow);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'SharesTransfer.FromAddress' Error:Field validation for 'FromAddress' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));

        log.info(" ************************ test amount must ************************ ");
        response= gd.GDShareTransfer(keyId,fromAddr,0,toAddr,shareProperty,eqCode,txInformation,fromNow,toNow);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'SharesTransfer.Amount' Error:Field validation for 'Amount' failed on the 'gt' tag",
                JSONObject.fromObject(response).getString("message"));

        log.info(" ************************ test toAddress must ************************ ");
        response= gd.GDShareTransfer(keyId,fromAddr,transferAmount,"",shareProperty,eqCode,txInformation,fromNow,toNow);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'SharesTransfer.ToAddress' Error:Field validation for 'ToAddress' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));

        log.info(" ************************ test txInformation must ************************ ");
        response= gd.GDShareTransfer(keyId,fromAddr,transferAmount,toAddr,shareProperty,eqCode,null,fromNow,toNow);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'SharesTransfer.TxInformation' Error:Field validation for 'TxInformation' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));

        log.info(" ************************ test fromRegisterInformation must ************************ ");
        response= gd.GDShareTransfer(keyId,fromAddr,transferAmount,toAddr,shareProperty,eqCode,txInformation,null,toNow);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'SharesTransfer.FromRegisterInformation' Error:Field validation for 'FromRegisterInformation' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));


        log.info(" ************************ test toRegisterInformation must ************************ ");
        response= gd.GDShareTransfer(keyId,fromAddr,transferAmount,toAddr,shareProperty,eqCode,txInformation,fromNow,null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'SharesTransfer.ToRegisterInformation' Error:Field validation for 'ToRegisterInformation' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));


        log.info(" ************************ test all must ************************ ");

        response = gd.GDShareTransfer(null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        log.info("required count: " + StringUtils.countOccurrencesOf(response,"required"));
    }

    @Test
    public void shareRecycleInterfaceTest() throws Exception {

        String remark = "777777";

        gdEquityCode = "unlockEC" + Random(6);
        String eqCode = gdEquityCode;
        List<Map> shareListIssue = gdConstructShareList(gdAccount1,1000,0);
        uf.shareIssue(gdEquityCode,shareListIssue,true); //执行发行

        sleepAndSaveInfo(3000);

        List<Map> shareList = gdConstructShareList(gdAccount1,10,0);
        //验证接口正常
        String response= gd.GDShareRecycle(gdPlatfromKeyID,eqCode,shareList,remark);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));

        //platformKeyId为空
        response= gd.GDShareRecycle("",eqCode,shareList,remark);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("无效的参数:Key: 'SharesRecycle.PlatformKeyId' Error:Field validation for 'PlatformKeyId' failed on the 'required' tag"));

        //equityCode为空
        response= gd.GDShareRecycle(gdPlatfromKeyID,"",shareList,remark);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("Error:Field validation for 'EquityCode' failed on the 'required"));

        //List为空
        response= gd.GDShareRecycle(gdPlatfromKeyID,eqCode,null,remark);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("无效的参数:Key: 'SharesRecycle.AddressList' Error:Field validation for 'AddressList' failed on the 'required' tag"));

        //registerInformation不传参
        Map<String,Object> shares = new HashMap<>();
        shares.put("address",gdAccount1);
        shares.put("amount",10);
        shares.put("shareProperty",0);
        shares.put("registerInformation",null);

        List<Map> shareList2 = new ArrayList<>();
        shareList2.add(shares);

        response= gd.GDShareRecycle(gdPlatfromKeyID,eqCode,shareList2,remark);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'SharesRecycle.AddressList[0].RegisterInformation' Error:Field validation for 'RegisterInformation' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));

        //地址不传
        shares.clear();
        shares = new HashMap<>();
        shares.put("address","");
        shares.put("amount",10);
        shares.put("shareProperty",0);
        shares.put("registerInformation",registerInfo);
        shareList2 = new ArrayList<>();
        shareList2.add(shares);
        response= gd.GDShareRecycle(gdPlatfromKeyID,eqCode,shareList2,remark);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'SharesRecycle.AddressList[0].Address' Error:Field validation for 'Address' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));

        //数量不传
        shares.clear();
        shares = new HashMap<>();
        shares.put("address",gdAccount1);
        shares.put("shareProperty",0);
        shares.put("registerInformation",registerInfo);
        shareList2 = new ArrayList<>();
        shareList2.add(shares);
        response= gd.GDShareRecycle(gdPlatfromKeyID,eqCode,shareList2,remark);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'SharesRecycle.AddressList[0].Amount' Error:Field validation for 'Amount' failed on the 'gt' tag",
                JSONObject.fromObject(response).getString("message"));

        //remark为空
        response= gd.GDShareRecycle(gdPlatfromKeyID,eqCode,shareList,"");
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("Error:Field validation for 'Remark' failed on the 'required"));

    }


    @Test
    public void accoutDestroyInterfaceMustParamTest() throws Exception {

        log.info(" ************************ test contractAddress must ************************ ");
        String response = gd.GDAccountDestroy("",gdAccClientNo8,
                "2020/05/12 18:08:08","1.txt","2020/05/12 18:08:08","2.txt");
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'AccountDestroy.ContractAddress' Error:Field validation for 'ContractAddress' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));

        log.info(" ************************ test clientNo must ************************ ");
        response = gd.GDAccountDestroy(gdContractAddress,gdAccClientNo8,
                "","1.txt","2020/05/12 18:08:08","2.txt");
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'AccountDestroy.ClientNo' Error:Field validation for 'ClientNo' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));

        log.info(" ************************ test shareholderClosingDate must ************************ ");
        response = gd.GDAccountDestroy(gdContractAddress,"",
                "2020/05/12 18:08:08","1.txt","2020/05/12 18:08:08","2.txt");
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'AccountDestroy.ShareholderClosingDate' Error:Field validation for 'ShareholderClosingDate' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));

        log.info(" ************************ test shareholderClosingCertificate must ************************ ");
        response = gd.GDAccountDestroy(gdContractAddress,gdAccClientNo8,
                "2020/05/12 18:08:08","","2020/05/12 18:08:08","2.txt");
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'AccountDestroy.ShareholderClosingCertificate' Error:Field validation for 'ShareholderClosingCertificate' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));

        log.info(" ************************ test fundClosingDate must ************************ ");
        response = gd.GDAccountDestroy(gdContractAddress,gdAccClientNo8,
                "2020/05/12 18:08:08","1.txt","","test.cert");
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'AccountDestroy.FundClosingDate' Error:Field validation for 'FundClosingDate' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));

        log.info(" ************************ test fundClosingCertificate must ************************ ");
        response = gd.GDAccountDestroy(gdContractAddress,gdAccClientNo8,
                "2020/05/12 18:08:08","1.txt","2020/05/12 18:08:08","");
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'AccountDestroy.FundClosingCertificate' Error:Field validation for 'FundClosingCertificate' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));

        log.info(" ************************ test all must ************************ ");
        response = gd.GDAccountDestroy("","","","","","");
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("无效的参数"));
//        assertEquals(true,response.contains("Key: 'AccountDestroy.ClientNo' Error:Field validation for 'ClientNo' failed on the 'required' tag"));


    }


    @Test
    public void shareChangeBoardInterfaceMustParamTest() throws Exception {

        gdEquityCode = "unlockEC" + Random(6);
        String eqCode = gdEquityCode;
        List<Map> shareListIssue = gdConstructShareList(gdAccount1,1000,0);
        uf.shareIssue(gdEquityCode,shareListIssue,true); //执行发行

        sleepAndSaveInfo(3000);

        String oldEquityCode = gdEquityCode;
        String newEquityCode = gdEquityCode + Random(5);
        String cpnyId = gdCompanyID;
//        String response= gd.GDShareChangeBoard(gdPlatfromKeyID,cpnyId,oldEquityCode,newEquityCode,registerInfo, equityProductInfo,null);

        List<Map> regList = uf.getAllHolderListReg(gdEquityCode,"interface" + Random(6));
        log.info(" ************************ test platformKeyId must ************************ ");
        String response = gd.GDShareChangeBoard("",cpnyId,oldEquityCode,newEquityCode,regList, equityProductInfo,null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'TransferPlate.PlatformKeyId' Error:Field validation for 'PlatformKeyId' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));

        log.info(" ************************ test companyId must ************************ ");
        response = gd.GDShareChangeBoard(gdPlatfromKeyID,"",oldEquityCode,newEquityCode,regList, equityProductInfo,null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'TransferPlate.CompanyId' Error:Field validation for 'CompanyId' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));

        log.info(" ************************ test oldEquityCode must ************************ ");
        response = gd.GDShareChangeBoard(gdPlatfromKeyID,cpnyId,"",newEquityCode,regList, equityProductInfo,null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'TransferPlate.OldEquityCode' Error:Field validation for 'OldEquityCode' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));

        log.info(" ************************ test newEquityCode must ************************ ");
        response = gd.GDShareChangeBoard(gdPlatfromKeyID,cpnyId,oldEquityCode,"",regList, equityProductInfo,null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'TransferPlate.NewEquityCode' Error:Field validation for 'NewEquityCode' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));

        log.info(" ************************ test registerInformation must ************************ ");
        response = gd.GDShareChangeBoard(gdPlatfromKeyID,cpnyId,oldEquityCode,newEquityCode,null, equityProductInfo,null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'TransferPlate.RegisterInformationList' Error:Field validation for 'RegisterInformationList' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));

        log.info(" ************************ test equityProductInfo must ************************ ");
        response = gd.GDShareChangeBoard(gdPlatfromKeyID,cpnyId,oldEquityCode,newEquityCode,regList, null,null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("产品对象标识不可以为空"));

        log.info(" ************************ test all must ************************ ");
        response = gd.GDShareChangeBoard("","","","",null, null,null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));

        assertEquals(5,StringUtils.countOccurrencesOf(response,"required"));
        log.info("required count: " + StringUtils.countOccurrencesOf(response,"required"));

    }

    @Test
    public void shareChangePropertyInterfaceMustParamTest() throws Exception {

        gdEquityCode = "unlockEC" + Random(6);
        String eqCode = gdEquityCode;
        List<Map> shareListIssue = gdConstructShareList(gdAccount1,1000,0);
        uf.shareIssue(gdEquityCode,shareListIssue,true); //执行发行

        sleepAndSaveInfo(3000);

        String address = gdAccount1;
        int oldProperty = 0;
        int newProperty = 1;
        Map tempReg1 = new HashMap();
        Map tempReg2 = new HashMap();
        tempReg1 = gdBF.init05RegInfo();
        tempReg2 = gdBF.init05RegInfo();
        List<Map> regListInfo = new ArrayList<>();
        regListInfo.add(tempReg1);
        regListInfo.add(tempReg2);

//        String response = gd.GDShareChangeProperty(gdPlatfromKeyID, address, eqCode, 100, oldProperty, newProperty, regListInfo);


        log.info(" ************************ test platformkeyId must ************************ ");
        String response = gd.GDShareChangeProperty("", address, eqCode, 100, oldProperty, newProperty, regListInfo);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'SharesChange.PlatformKeyId' Error:Field validation for 'PlatformKeyId' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));

        log.info(" ************************ test address must ************************ ");
        response = gd.GDShareChangeProperty(gdPlatfromKeyID, "", eqCode, 100, oldProperty, newProperty, regListInfo);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'SharesChange.Address' Error:Field validation for 'Address' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));

        log.info(" ************************ test equityCode must ************************ ");
        response = gd.GDShareChangeProperty(gdPlatfromKeyID, address, "", 100, oldProperty, newProperty, regListInfo);

        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'SharesChange.EquityCode' Error:Field validation for 'EquityCode' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));


        log.info(" ************************ test amount must ************************ ");
        response = gd.GDShareChangeProperty(gdPlatfromKeyID, address, eqCode, 0, oldProperty, newProperty, regListInfo);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'SharesChange.Amount' Error:Field validation for 'Amount' failed on the 'gt' tag",
                JSONObject.fromObject(response).getString("message"));

        log.info(" ************************ test registerInformationList must ************************ ");
        response = gd.GDShareChangeProperty(gdPlatfromKeyID, address, eqCode, 100, oldProperty, newProperty, null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'SharesChange.RegisterInformationList' Error:Field validation for 'RegisterInformationList' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));

        log.info(" ************************ test registerInformationList 只有一个************************ ");
        regListInfo = new ArrayList<>();
        regListInfo.add(tempReg1);
        response = gd.GDShareChangeProperty(gdPlatfromKeyID, address, eqCode, 100, oldProperty, newProperty, regListInfo);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("此处登记信息应为两条",
                JSONObject.fromObject(response).getString("message"));


//        log.info(" ************************ test oldShareProperty must ************************ ");
//        response = gd.GDShareChangeProperty(gdPlatfromKeyID, address, eqCode, 100, oldProperty, newProperty, regListInfo);

//        log.info(" ************************ test newShareProperty must ************************ ");
//        response = gd.GDShareChangeProperty(gdPlatfromKeyID, address, eqCode, 100, oldProperty, newProperty, regListInfo);


        log.info(" ************************ test all must ************************ ");

        response = gd.GDShareChangeProperty(null);

        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,JSONObject.fromObject(response).getString("message").contains("无效的参数"));

        assertEquals(6, StringUtils.countOccurrencesOf(response,"Error:"));
//        log.info("required count: " + StringUtils.countOccurrencesOf(response,"Error:"));
    }

    @Test
    public void queryShareHolderListInterfaceMustParamTest() throws Exception {
        log.info(" ************************ test type must ************************ ");
        String response= gd.GDGetEnterpriseShareInfo("");
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'QueryShares.EquityCode' Error:Field validation for 'EquityCode' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));
    }

    @Test
    public void infoDisclosurePublishInterfaceMustParamTest() throws Exception {
        log.info(" ************************ test type must ************************ ");
        String response= gd.GDInfoPublish(null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("Invalid parameter, error:Key: 'InfoDisc.Type' Error:Field validation for 'Type' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));
    }

    @Test
    public void balanceCountInterfaceMustParamTest() throws Exception {
        log.info(" ************************ test balanceAccount must ************************ ");
        String response= gd.GDCapitalSettlement(null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        //        assertEquals("Invalid parameter, error:Key: 'InfoDisc.Type' Error:Field validation for 'Type' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));
    }

    @Test
    public void queryAccountSharesInterfaceMustParamTest() throws Exception {
        log.info(" ************************ test contractAddress must ************************ ");
        String response= gd.GDGetShareHolderInfo("",gdAccClientNo1);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'ShareholderQuery.ContractAddress' Error:Field validation for 'ContractAddress' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));

        log.info(" ************************ test clientNo must ************************ ");
        response= gd.GDAccountQuery(gdContractAddress,"");
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'ShareholderQuery.ClientNo' Error:Field validation for 'ClientNo' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));
    }

    @Test
    public void queryAccountInterfaceMustParamTest() throws Exception {
        log.info(" ************************ test contractAddress must ************************ ");
        String response= gd.GDAccountQuery("",gdAccClientNo1);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
                assertEquals("无效的参数:Key: 'ShareholderQuery.ContractAddress' Error:Field validation for 'ContractAddress' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));

        log.info(" ************************ test clientNo must ************************ ");
        response= gd.GDAccountQuery(gdContractAddress,"");
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
                assertEquals("无效的参数:Key: 'ShareholderQuery.ClientNo' Error:Field validation for 'ClientNo' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));
    }

    @Test
    public void querySubjectInterfaceMustParamTest() throws Exception {
        log.info(" ************************ test contractAddress must ************************ ");
        String response= gd.GDMainSubjectQuery("",gdCompanyID);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'SubjectQuery.ContractAddress' Error:Field validation for 'ContractAddress' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));

        log.info(" ************************ test subjectInfo must ************************ ");
        response= gd.GDMainSubjectQuery(gdContractAddress,"");
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'SubjectQuery.SubjectObjectId' Error:Field validation for 'SubjectObjectId' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));
    }

    @Test
    public void updateSubjectInterfaceMustParamTest() throws Exception {
        log.info(" ************************ test contractAddress must ************************ ");
        String response= gd.GDUpdateSubjectInfo("",0,enterpriseSubjectInfo);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'SubjectUpdate.ContractAddress' Error:Field validation for 'ContractAddress' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));

        log.info(" ************************ test subjectInfo must ************************ ");
        response= gd.GDUpdateSubjectInfo(gdContractAddress,0,null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("主体信息不可以为空",JSONObject.fromObject(response).getString("message"));
    }

    @Test
    public void queryTXReportInterfaceMustParamTest() throws Exception {
        log.info(" ************************ test type must ************************ ");
        String response= gd.GDGetTxReportInfo("","","","");
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("Invalid parameter, error:Key: 'QueryTX.Type' Error:Field validation for 'Type' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));

        log.info(" ************************ test type 1 value must ************************ ");
        response= gd.GDGetTxReportInfo("1","","","");
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("query value cannot empty",JSONObject.fromObject(response).getString("message"));

        log.info(" ************************ test type 2 value must ************************ ");
        response= gd.GDGetTxReportInfo("2","","","");
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("query value cannot empty",JSONObject.fromObject(response).getString("message"));

        log.info(" ************************ test type 3 value must ************************ ");
        response= gd.GDGetTxReportInfo("3","","","");
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("query value cannot empty",JSONObject.fromObject(response).getString("message"));

        log.info(" ************************ test type 4 value must ************************ ");
        response= gd.GDGetTxReportInfo("4","","","");
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("query value cannot empty",JSONObject.fromObject(response).getString("message"));

        log.info(" ************************ test type 5 value must ************************ ");
        response= gd.GDGetTxReportInfo("5",gdCompanyID,"","");
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("query parameter cannot empty",JSONObject.fromObject(response).getString("message"));
    }


}
