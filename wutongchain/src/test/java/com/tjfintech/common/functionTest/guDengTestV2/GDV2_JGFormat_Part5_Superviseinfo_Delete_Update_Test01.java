package com.tjfintech.common.functionTest.guDengTestV2;

import com.alibaba.fastjson.JSONObject;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.GuDeng;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.MinIOOperation;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import org.junit.*;
import org.junit.rules.TestName;
import org.junit.runners.MethodSorters;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.functionTest.guDengTestV2.GDCommonFunc.conJGFileName;
import static com.tjfintech.common.functionTest.guDengTestV2.GDCommonFunc.replaceCertain;
import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassGD.*;
import static org.junit.Assert.assertEquals;

//import net.sf.json.JSONObject;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
/**
 * 监管数据删除及更新接口 测试接口用例补充
 * 删除后更新
 * 删除个人主体 账户 机构主体 产品信息后再更新
 */
public class GDV2_JGFormat_Part5_Superviseinfo_Delete_Update_Test01 {

    TestBuilder testBuilder= TestBuilder.getInstance();
    GuDeng gd =testBuilder.getGuDeng();
    Store store =testBuilder.getStore();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    GDCommonFunc gdCF = new GDCommonFunc();
    GDBeforeCondition gdBF = new GDBeforeCondition();
    GDUnitFunc uf = new GDUnitFunc();
    public static String bizNoTest = "test" + Random(12);
    String tempaccount_subject_ref,tempaccount_associated_account_ref,tempproduct_issuer_subject_ref;

    @Rule
    public TestName tm = new TestName();


    @BeforeClass
    public static void Before()throws Exception{
        GDBeforeCondition gdBefore = new GDBeforeCondition();
//        gdBefore.gdCreateAccout();
        gdBefore.initRegulationData();
    }

    @Before
    public void BeforeTest()throws Exception{
        gdEquityCode = Random(20);
        gdCompanyID = "P1Re" + Random(8);

        settlement_product_ref = gdEquityCode;

        tempaccount_subject_ref = account_subject_ref;
        tempaccount_associated_account_ref =account_associated_account_ref;
        tempproduct_issuer_subject_ref = product_issuer_subject_ref;
    }

//    @After
    public void calJGDataAfterTx()throws Exception{
        testCurMethodName = tm.getMethodName();
        GDUnitFunc uf = new GDUnitFunc();
        int endHeight = net.sf.json.JSONObject.fromObject(store.GetHeight()).getInt("data");
        uf.checkJGHeaderOpVer(blockHeight,endHeight);

        //中间可能有做过重新赋值 执行完成后恢复原设置值
        account_subject_ref = tempaccount_subject_ref;
        account_associated_account_ref =tempaccount_associated_account_ref;
        product_issuer_subject_ref = tempproduct_issuer_subject_ref;

    }


    //企业 股权类 登记
    @Test
    public void TCN011_enterpriseRegisterEquityCheckFormat() throws Exception {
        regTestUnit("1",false);
    }



    @Test
    public void TC20_allUpdateSubjectInfo_Personal()throws Exception {
        //开户
        String cltNo = "updateCLI" + Random(12);
        String shareHolderNo = "SH" + cltNo;
        String fundNo = "fund" + cltNo;

        int gdClient = Integer.parseInt(gdCF.getObjectLatestVer(cltNo));//获取当前开户主体最新版本信息
        account_subject_ref = cltNo;
        account_associated_account_ref = shareHolderNo;
        Map tempSHAcc = gdBF.init02ShareholderAccountInfo();
        Map tempFundAcc = gdBF.init02FundAccountInfo();

        log.info(tempSHAcc.toString());
        log.info(tempFundAcc.toString());

        //构造股权账户信息
        Map shareHolderInfo = new HashMap();

        tempSHAcc.put("account_object_id", shareHolderNo);  //更新账户对象标识字段
        log.info(tempSHAcc.toString());
        shareHolderInfo.put("createTime", ts2);
        shareHolderInfo.put("shareholderNo", shareHolderNo);
        shareHolderInfo.put("accountInfo", tempSHAcc);
        log.info(shareHolderInfo.toString());

        //资金账户信息
        tempFundAcc.put("account_object_id", fundNo);  //更新账户对象标识字段
        Map mapFundInfo = new HashMap();
        mapFundInfo.put("createTime", ts2);
        mapFundInfo.put("fundNo", fundNo);
        mapFundInfo.put("accountInfo", tempFundAcc);
//        mapFundInfo.put("account_associated_account_ref", shareHolderNo);

        //构造个人/投资者主体信息
        Map testSub = gdBF.init01PersonalSubjectInfo();
        testSub.put("subject_object_id", cltNo);  //更新对象标识字段

        String response = gd.GDCreateAccout(gdContractAddress, cltNo, mapFundInfo, shareHolderInfo, testSub);
        String txId = net.sf.json.JSONObject.fromObject(response).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        assertEquals("200", net.sf.json.JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));


        //=========================================================================================
        //删除主体 账户对象
        gd.GDEquitySuperviseInfoDelete(cltNo,0,subjectType);
        gd.GDEquitySuperviseInfoDelete(fundNo,0,accType);
        gd.GDEquitySuperviseInfoDelete(shareHolderNo,0,accType);

        //检查头信息
        gdCF.checkHeaderContentInfo(cltNo,0,subjectType,"delete");
        gdCF.checkHeaderContentInfo(fundNo,0,accType,"delete");
        gdCF.checkHeaderContentInfo(shareHolderNo,0,accType,"delete");

        //更新对象
        String updateResp = gd.GDEquitySuperviseInfoUpdate(cltNo,subjectType,ts1,testSub);
        assertEquals("200", net.sf.json.JSONObject.fromObject(updateResp).getString("state"));
        updateResp = gd.GDEquitySuperviseInfoUpdate(shareHolderNo,accType,ts2,tempSHAcc);
        assertEquals("200", net.sf.json.JSONObject.fromObject(updateResp).getString("state"));
        updateResp = gd.GDEquitySuperviseInfoUpdate(fundNo,accType,ts2,tempFundAcc);
        assertEquals("200", net.sf.json.JSONObject.fromObject(updateResp).getString("state"));
        //=========================================================================================


        //定义相关对象标识版本变量
        String accASrefVer = gdCF.getObjectLatestVer(account_subject_ref);
        String accADrefVer =  gdCF.getObjectLatestVer(account_depository_ref);
        String accAAARefVer =  gdCF.getObjectLatestVer(account_associated_account_ref);

        String shAccVer =  gdCF.getObjectLatestVer("SH" + cltNo);
        String fundAccVer =  gdCF.getObjectLatestVer("fund" + cltNo);
        String personSubVer =  gdCF.getObjectLatestVer(cltNo);

        String subSIQCRefVer = gdCF.getObjectLatestVer(subject_investor_qualification_certifier_ref);

        String fundObjId = fundNo;
        String SHObjId = shareHolderNo;
        //获取链上mini url的存证信息 并检查是否包含uri信息
        String subfileName = conJGFileName(cltNo,personSubVer);
        String shAccfileName = conJGFileName(SHObjId,shAccVer);
        String fundAccfileName = conJGFileName(fundObjId,fundAccVer);

        Map uriInfo = gdCF.getJGURIStoreHash(txId,subfileName,1);
        String chkSubURI = subfileName;
        String chkSHAccURI = shAccfileName;
        String chkFundAccURI = fundAccfileName;
        log.info(uriInfo.get("storeData").toString());
        log.info(chkSubURI);
//        assertEquals(true,uriInfo.get("storeData").toString().contains(chkSubURI));
//        assertEquals(true,uriInfo.get("storeData").toString().contains(chkSHAccURI));
//        assertEquals(true,uriInfo.get("storeData").toString().contains(chkFundAccURI));
//        assertEquals(true,gdCF.bContainJGFlag(uriInfo.get("storeData").toString()));//确认meta信息包含监管关键字


        //直接从minio上获取报送数据文件信息
        Map getSubInfo = gdCF.constructJGDataFromStr(subfileName,subjectType,"2");
        Map getFundAccInfo = gdCF.constructJGDataFromStr(fundAccfileName,accType,"2");
        Map getSHAccInfo = gdCF.constructJGDataFromStr(shAccfileName,accType,"1");

        Map enSubInfo = gdBF.init01PersonalSubjectInfo();
        Map accFund = gdBF.init02FundAccountInfo();
        Map accSH = gdBF.init02ShareholderAccountInfo();

        enSubInfo.put("subject_object_id",cltNo);

        //填充header content 信息
        enSubInfo.put("content",gdCF.constructContentTreeMap(subjectType,cltNo,personSubVer,"update",String.valueOf(ts1)));
        accFund.put("content",gdCF.constructContentTreeMap(accType,fundObjId,fundAccVer,"update",String.valueOf(ts2)));
        accSH.put("content",gdCF.constructContentTreeMap(accType,SHObjId,shAccVer,"update",String.valueOf(ts2)));

        assertEquals(String.valueOf(gdClient + 2),personSubVer);

        //账户的如下字段默认引用的是开户主体的对象标识
        account_subject_ref = cltNo;

        //需要将比较的对象标识增加版本号信息
        String[] verForSub = new String[]{"/" + subSIQCRefVer};
        String[] verForAccSH = new String[]{"/" + personSubVer,"/" + accADrefVer,"/" + accAAARefVer};


        log.info("检查主体存证信息内容与传入一致\n" + enSubInfo.toString() + "\n" + getSubInfo.toString());
        assertEquals(replaceCertain(gdCF.matchRefMapCertVer(enSubInfo,subjectType,verForSub)),replaceCertain(getSubInfo.toString()));

        accSH.put("account_object_id",SHObjId);
        accSH.put("account_subject_ref",cltNo);
        log.info("检查股权账户存证信息内容与传入一致\n" + accSH.toString() + "\n" + getSHAccInfo.toString());
        assertEquals(replaceCertain(gdCF.matchRefMapCertVer2(accSH,accType)),replaceCertain(getSHAccInfo.toString()));

        account_associated_account_ref = SHObjId;
        String[] verForAccFund = new String[]{"/" + personSubVer,"/" + accADrefVer,"/" + shAccVer};

        accFund.put("account_object_id",fundObjId);
        accFund.put("account_subject_ref",cltNo);
        accFund.put("account_associated_account_ref",SHObjId);
        log.info("检查资金账户存证信息内容与传入一致\n" + accFund.toString() + "\n" + getFundAccInfo.toString());
        assertEquals(replaceCertain(gdCF.matchRefMapCertVer2(accFund,accType)),replaceCertain(getFundAccInfo.toString()));

    }


//    @Test
    public void TCN16_balanceCount() throws Exception {
        settlement_product_ref = gdEquityCode;
        Map enSubInfo = gdBF.init01EnterpriseSubjectInfo();
        Map prodInfo = gdBF.init03EquityProductInfo();

        int gdCpmIdOldVer = Integer.parseInt(gdCF.getObjectLatestVer(gdCompanyID));//获取当前挂牌主体最新版本信息
        String response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,1000,enSubInfo,
                        prodInfo,null,null);
        assertEquals("200", net.sf.json.JSONObject.fromObject(response).getString("state"));
        sleepAndSaveInfo(2000);

        //开户
        if(gdCF.getObjectLatestVer(gdAccClientNo1).equals("-1")){
            String cltNo = "settleAcc" + Random(6);
            gdAccClientNo1 = cltNo;
            //执行开户
            Map mapCreate = gdBF.gdCreateAccParam(cltNo);
            String txId = mapCreate.get("txId").toString();
            commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
            String txDetail = store.GetTxDetail(txId);
            assertEquals("200", net.sf.json.JSONObject.fromObject(txDetail).getString("state"));
            sleepAndSaveInfo(2000);
        }

        Map testSettleInfo = gdBF.init06SettleInfo();
        testSettleInfo.put("settlement_in_account_object_ref","SH" + gdAccClientNo1);
        response= gd.GDCapitalSettlement(testSettleInfo);
        net.sf.json.JSONObject jsonObject= net.sf.json.JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        String txDetail = store.GetTxDetail(txId);
        assertEquals("200", net.sf.json.JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));


        //设置各个主体版本变量
        String objPrefix = "uri";

        //获取（从交易详情中）链上mini url的存证信息并检查是否包含uri信息 通过前缀信息获取信披对象id
        String storeData = JSONObject.parseObject(txDetail).getJSONObject(
                "data").getJSONObject("store").getString("storeData").toString();
        log.info(storeData);
        JSONObject objURI = JSONObject.parseObject(
                JSONObject.parseArray(storeData).get(0).toString());
        String chkObjURI = objPrefix;
        assertEquals(true,storeData.contains(chkObjURI));
        assertEquals(true,gdCF.bContainJGFlag(storeData));//确认meta信息包含监管关键字

        String objVerTemp =  objURI.getString("uri").trim();

        String newSettleObjId = "";
        newSettleObjId = objVerTemp.substring(0,objVerTemp.lastIndexOf("/"));

        String newDisObjIdVer = objVerTemp.substring(objVerTemp.lastIndexOf("/") + 1);//gdCF.getObjectLatestVer(newDisObjId);
        log.info(objVerTemp + " " + newDisObjIdVer);

        String objfileName = conJGFileName(newSettleObjId,newDisObjIdVer);


        //直接从minio上通过对象标识+版本号的方式获取指定对象文件
        Map getSettleInfo = gdCF.constructJGDataFromStr(objfileName,settleType,"");


        //填充header content字段
        testSettleInfo.put("content",gdCF.constructContentTreeMap(settleType,newSettleObjId,newDisObjIdVer,"create",String.valueOf(ts6)));
        testSettleInfo.put("settlement_transaction_ref","null");//默认没有携带交易报告对象引用信息
        settlement_in_account_object_ref = gdAccClientNo1;
        log.info("检查主体存证信息内容与传入一致\n" + testSettleInfo.toString() + "\n" + getSettleInfo.toString());
        assertEquals(replaceCertain(gdCF.matchRefMapCertVer2(testSettleInfo,settleType)),replaceCertain(getSettleInfo.toString()));

    }
    //挂牌登记模块封装
    public void regTestUnit(String type,Boolean bSetEmpty)throws Exception{
        long shareTotals = 1000000;
        Map enSubInfo = gdBF.init01EnterpriseSubjectInfo();
        Map prodInfo = null;

        int gdCpmIdOldVer = Integer.parseInt(gdCF.getObjectLatestVer(gdCompanyID));//获取当前挂牌主体最新版本信息
        String response= "";

        //根据产品类型获取产品信息并执行挂牌
        switch (type){
            case "1":   prodInfo = gdBF.init03EquityProductInfo();
                if(bSetEmpty) prodInfo.put("product_issuer_subject_ref","");
                response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,enSubInfo,
                        prodInfo,null,null);
                break;
            case "2":   prodInfo = gdBF.init03BondProductInfo();
                if(bSetEmpty) prodInfo.put("product_issuer_subject_ref","");
                response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,enSubInfo,
                        null,prodInfo,null);
                break;
            case "3":   prodInfo = gdBF.init03FundProductInfo();
                if(bSetEmpty) prodInfo.put("product_issuer_subject_ref","");
                response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,enSubInfo,
                        null,null,prodInfo);
                break;
            case "4":
                response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,enSubInfo,
                        null,null,null);
                break;
            default:    assertEquals("非法类型" + type, false,true);
        }

        //获取交易hash
        String txId = net.sf.json.JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        //判断交易上链
        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        String txDetail = store.GetTxDetail(txId);
        assertEquals("200", net.sf.json.JSONObject.fromObject(txDetail).getString("state"));

        //检查各个查询对象返回信息中不包含敏感词
        assertEquals("不包含敏感词",true,
                gdCF.chkSensitiveWord(txDetail,subjectType));
        assertEquals("不包含敏感词",true,
                gdCF.chkSensitiveWord(txDetail,prodType));

        //查询挂牌企业主体数据  交易上链后 数据可能还未写入合约 在此做2s内数据查询
        response = gd.GDMainSubjectQuery(gdContractAddress, gdCompanyID);
//        assertEquals("200", net.sf.json.JSONObject.fromObject(response).getString("state"));

        //=========================================================================================
        //删除主体对象 产品对象
        String chkResp = gd.GDEquitySuperviseInfoDelete(gdCompanyID,0,subjectType);
        chkResp = gd.GDEquitySuperviseInfoDelete(gdEquityCode,0,prodType);

        //检查头信息
        gdCF.checkHeaderContentInfo(gdCompanyID,0,subjectType,"delete");
        gdCF.checkHeaderContentInfo(gdEquityCode,0,prodType,"delete");

        //更新主体对象 产品对象
        chkResp = gd.GDEquitySuperviseInfoUpdate(gdCompanyID,subjectType,ts1,enSubInfo);
        chkResp = gd.GDEquitySuperviseInfoUpdate(gdEquityCode,prodType,ts3,prodInfo);

        txId = net.sf.json.JSONObject.fromObject(chkResp).getJSONObject("data").getString("txId");
        //=========================================================================================

        //设置各个主体版本变量
        String newSubVer = gdCF.getObjectLatestVer(gdCompanyID);
        String newEqProdVer = gdCF.getObjectLatestVer(gdEquityCode);

        String subSIQCRefVer = gdCF.getObjectLatestVer(subject_investor_qualification_certifier_ref);
        String prodPMSRefVer = gdCF.getObjectLatestVer(product_market_subject_ref);
        String prodSPSRefVer = gdCF.getObjectLatestVer(service_provider_subject_ref);

        //获取链上mini url的存证信息并检查是否包含uri信息
        String subfileName = conJGFileName(gdCompanyID,newSubVer);
        String prodfileName = conJGFileName(gdEquityCode,newEqProdVer);

        Map uriInfo = gdCF.getJGURIStoreHash(txId,conJGFileName(gdCompanyID,newSubVer),1);
        String chkSubURI = subfileName;
        String chkProdURI = prodfileName;
        log.info(uriInfo.get("storeData").toString());
        log.info(chkSubURI);


        //直接从minio上通过对象标识+版本号的方式获取指定对象文件
        Map getSubInfo = gdCF.constructJGDataFromStr(conJGFileName(gdCompanyID,newSubVer),subjectType,"1");
        Map getProInfo = null;
        if(!type.equals("4")) getProInfo = gdCF.constructJGDataFromStr(conJGFileName(gdEquityCode, newEqProdVer), prodType, type);

        //填充header content字段
        log.info("********************************" + newSubVer);
        if(newSubVer.equals("0")) {
            enSubInfo.put("content", gdCF.constructContentTreeMap(subjectType, gdCompanyID, newSubVer, "create", String.valueOf(ts1)));
        }else {
            enSubInfo.put("content", gdCF.constructContentTreeMap(subjectType, gdCompanyID, newSubVer, "update", String.valueOf(ts1)));
        }
        //如果不是机构会员登记 则执行产品填充header content字段
        if(!type.equals("4")) {
            prodInfo.put("content",gdCF.constructContentTreeMap(prodType, gdEquityCode, newEqProdVer, "update", String.valueOf(ts3)));
        }

        //产品发行主体引用设置为空场景 当前代码会自动补充发行主体对象标识
        if(bSetEmpty) prodInfo.put("product_issuer_subject_ref", enSubInfo.get("subject_object_id").toString());
        //产品如下字段引用的是发行主体
        product_issuer_subject_ref = gdCompanyID;
//        assertEquals(String.valueOf(gdCpmIdOldVer + 1),gdCF.getObjectLatestVer(gdCompanyID));

        assertEquals(String.valueOf(gdCpmIdOldVer + 2),newSubVer);

        String[] verForSub = new String[]{"/" + subSIQCRefVer };
        String[] verForProd = new String[]{"/" + prodPMSRefVer,"/" + newSubVer,"/" + prodSPSRefVer};

        log.info("检查主体存证信息内容与传入一致\n" + enSubInfo.toString() + "\n" + getSubInfo.toString());
        assertEquals(replaceCertain(gdCF.matchRefMapCertVer(enSubInfo,subjectType,verForSub)),replaceCertain(getSubInfo.toString()));

        if(!type.equals("4")) {
            log.info("检查产品存证信息内容与传入一致\n" + prodInfo.toString() + "\n" + getProInfo.toString());
            assertEquals(replaceCertain(gdCF.matchRefMapCertVer(prodInfo, prodType, verForProd)), replaceCertain(getProInfo.toString()));
        }
    }



}
