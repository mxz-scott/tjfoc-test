package com.tjfintech.common.functionTest.guDengTestV2;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.GuDeng;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.MinIOOperation;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
//import net.sf.json.JSONObject;
import org.junit.*;
import org.junit.rules.TestName;
import org.junit.runners.MethodSorters;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.tjfintech.common.functionTest.guDengTestV2.GDCommonFunc.*;
import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassGD.*;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class GDV2_CheckJGFormat_Part1_EnterpriseRegister_AccCreate_Publish_Settle {

    TestBuilder testBuilder= TestBuilder.getInstance();
    GuDeng gd =testBuilder.getGuDeng();
    Store store =testBuilder.getStore();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    GDCommonFunc gdCF = new GDCommonFunc();
    GDBeforeCondition gdBF = new GDBeforeCondition();
    GDUnitFunc uf = new GDUnitFunc();
    public static String bizNoTest = "test" + Random(12);
    public int verTemp = 0;
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

        tempaccount_subject_ref = account_subject_ref;
        tempaccount_associated_account_ref =account_associated_account_ref;
        tempproduct_issuer_subject_ref = product_issuer_subject_ref;
    }

    @After
    public void calJGDataAfterTx()throws Exception{
        //中间可能有做过重新赋值 执行完成后恢复原设置值
        account_subject_ref = tempaccount_subject_ref;
        account_associated_account_ref =tempaccount_associated_account_ref;
        product_issuer_subject_ref = tempproduct_issuer_subject_ref;

        testCurMethodName = tm.getMethodName();
//        GDUnitFunc uf = new GDUnitFunc();
//        uf.calJGDataEachHeight();
    }

    //企业 股权类 登记
    @Test
    public void TCN011_enterpriseRegisterEquityCheckFormat() throws Exception {
        regTestUnit("1",false);
    }

    //企业 债券类 登记
    @Test
    public void TCN012_enterpriseRegisterBondCheckFormat() throws Exception {
        regTestUnit("2",false);
    }

    //企业 基金类 登记
    @Test
    public void TCN013_enterpriseRegisterFundCheckFormat() throws Exception {
        regTestUnit("3",false);
    }

    //机构会员登记
    @Test
    public void TCN014_enterpriseRegisterMemberCheckFormat() throws Exception {
        regTestUnit("4",false);
    }

    //挂牌登记模块封装
    public void regTestUnit(String type,Boolean bSetEmpty)throws Exception{
        long shareTotals = 1000000;
        Map enSubInfo = gdBF.init01EnterpriseSubjectInfo();
        Map prodInfo = null;

        int gdCpmIdOldVer = -1;//Integer.parseInt(gdCF.getObjectLatestVer(gdCompanyID));//获取当前挂牌主体最新版本信息
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
        assertEquals("200", net.sf.json.JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        //查询挂牌企业主体数据  交易上链后 数据可能还未写入合约 在此做2s内数据查询
        response = gd.GDMainSubjectQuery(gdContractAddress, gdCompanyID);
//        assertEquals("200", net.sf.json.JSONObject.fromObject(response).getString("state"));

        //设置各个主体版本变量
        String newSubVer = "0";//gdCF.getObjectLatestVer(gdCompanyID);
        String newEqProdVer = "0";//gdCF.getObjectLatestVer(gdEquityCode);

        String subSIQCRefVer = "2";//gdCF.getObjectLatestVer(subject_investor_qualification_certifier_ref);
        String prodPMSRefVer = "2";//gdCF.getObjectLatestVer(product_market_subject_ref);
        String prodSPSRefVer = "2";//gdCF.getObjectLatestVer(service_provider_subject_ref);

        //获取链上mini url的存证信息并检查是否包含uri信息
        String subfileName = conJGFileName(gdCompanyID,newSubVer);
        String prodfileName = conJGFileName(gdEquityCode,newEqProdVer);

        Map uriInfo = gdCF.getJGURIStoreHash(txId,conJGFileName(gdCompanyID,newSubVer),1);
        String chkSubURI = minIOEP + "/" + jgBucket + "/" + subfileName;
        String chkProdURI = minIOEP + "/" + jgBucket + "/" + prodfileName;
        log.info(uriInfo.get("storeData").toString());
        log.info(chkSubURI);
        assertEquals(true,uriInfo.get("storeData").toString().contains(chkSubURI));
        if(!type.equals("4")) assertEquals(true,uriInfo.get("storeData").toString().contains(chkProdURI));
//        assertEquals(false,uriInfo.get("storeData").toString().contains("meta"));//确认meta信息移除


        //直接从minio上通过对象标识+版本号的方式获取指定对象文件
        Map getSubInfo = gdCF.constructJGDataFromStr(conJGFileName(gdCompanyID,newSubVer),subjectType,"1");
        Map getProInfo = null;
        if(!type.equals("4")) getProInfo = gdCF.constructJGDataFromStr(conJGFileName(gdEquityCode, newEqProdVer), prodType, type);

        //填充header content字段
        enSubInfo.put("content",gdCF.constructContentMap(subjectType,gdCompanyID,newSubVer,"create",String.valueOf(ts1)));
        //如果不是机构会员登记 则执行产品填充header content字段
        if(!type.equals("4")) {
            prodInfo.put("content",gdCF.constructContentMap(prodType, gdEquityCode, newEqProdVer, "create", String.valueOf(ts3)));
        }

        //产品发行主体引用设置为空场景 当前代码会自动补充发行主体对象标识
        if(bSetEmpty) prodInfo.put("product_issuer_subject_ref", enSubInfo.get("subject_object_id").toString());
        //产品如下字段引用的是发行主体
        product_issuer_subject_ref = gdCompanyID;
//        assertEquals(String.valueOf(gdCpmIdOldVer + 1),gdCF.getObjectLatestVer(gdCompanyID));

        assertEquals(String.valueOf(gdCpmIdOldVer + 1),newSubVer);

        String[] verForSub = new String[]{"/" + subSIQCRefVer };
        String[] verForProd = new String[]{"/" + prodPMSRefVer,"/" + newSubVer,"/" + prodSPSRefVer};

        log.info("检查主体存证信息内容与传入一致\n" + enSubInfo.toString() + "\n" + getSubInfo.toString());
        assertEquals(replaceCertain(gdCF.matchRefMapCertVer(enSubInfo,subjectType,verForSub)),replaceCertain(getSubInfo.toString()));

        if(!type.equals("4")) {
            log.info("检查产品存证信息内容与传入一致\n" + prodInfo.toString() + "\n" + getProInfo.toString());
            assertEquals(replaceCertain(gdCF.matchRefMapCertVer(prodInfo, prodType, verForProd)), replaceCertain(getProInfo.toString()));
        }
    }


    @Test
    public void TC014_createAccTestCheckFormat() throws Exception {
        GDBeforeCondition gdBC = new GDBeforeCondition();

        String cltNo = "tet00" + Random(12);

        int gdClient = -1; //Integer.parseInt(gdCF.getObjectLatestVer(cltNo));//获取当前开户主体最新版本信息

        //执行开户
        Map mapCreate = gdBC.gdCreateAccParam(cltNo);
        String txId = mapCreate.get("txId").toString();
        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", net.sf.json.JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        //查询投资者账户信息
        String response = "";
        response = gd.GDGetShareHolderInfo(gdContractAddress,cltNo);
        assertEquals("200", net.sf.json.JSONObject.fromObject(response).getString("state"));

        //查询个人主体信息  交易上链后 数据可能还未写入合约 在此做2s内数据查询
        response = gd.GDMainSubjectQuery(gdContractAddress, cltNo);
//        assertEquals("200", net.sf.json.JSONObject.fromObject(response).getString("state"));

        //定义相关对象标识版本变量
        String accASrefVer = "0" ;//gdCF.getObjectLatestVer(account_subject_ref);
        String accADrefVer = "2" ;// gdCF.getObjectLatestVer(account_depository_ref);
        String accAAARefVer = "2" ;// gdCF.getObjectLatestVer(account_associated_account_ref);

        String shAccVer = "0" ;// gdCF.getObjectLatestVer("SH" + cltNo);
        String fundAccVer = "0" ;// gdCF.getObjectLatestVer("fund" + cltNo);
        String personSubVer = "0" ;// gdCF.getObjectLatestVer(cltNo);

        String subSIQCRefVer = "2" ;//gdCF.getObjectLatestVer(subject_investor_qualification_certifier_ref);

        String fundObjId = mapCreate.get("fundNo").toString();
        String SHObjId = mapCreate.get("shareholderNo").toString();
        //获取链上mini url的存证信息 并检查是否包含uri信息
        String subfileName = conJGFileName(cltNo,personSubVer);
        String shAccfileName = conJGFileName(SHObjId,shAccVer);
        String fundAccfileName = conJGFileName(fundObjId,fundAccVer);

        Map uriInfo = gdCF.getJGURIStoreHash(txId,subfileName,1);
        String chkSubURI = minIOEP + "/" + jgBucket + "/" + subfileName;
        String chkSHAccURI = minIOEP + "/" + jgBucket + "/" + shAccfileName;
        String chkFundAccURI = minIOEP + "/" + jgBucket + "/" + fundAccfileName;
        log.info(uriInfo.get("storeData").toString());
        log.info(chkSubURI);
        assertEquals(true,uriInfo.get("storeData").toString().contains(chkSubURI));
        assertEquals(true,uriInfo.get("storeData").toString().contains(chkSHAccURI));
        assertEquals(true,uriInfo.get("storeData").toString().contains(chkFundAccURI));
//        assertEquals(false,uriInfo.get("storeData").toString().contains("meta"));//确认meta信息移除


        //直接从minio上获取报送数据文件信息
        Map getSubInfo = gdCF.constructJGDataFromStr(subfileName,subjectType,"2");
        Map getFundAccInfo = gdCF.constructJGDataFromStr(fundAccfileName,accType,"1");
        Map getSHAccInfo = gdCF.constructJGDataFromStr(shAccfileName,accType,"2");

        Map enSubInfo = investorSubjectInfo;
        Map accFund = fundAccountInfo;
        Map accSH = shAccountInfo;


        //填充header content 信息
        enSubInfo.put("content",gdCF.constructContentMap(subjectType,cltNo,personSubVer,"create",String.valueOf(ts1)));
        accFund.put("content",gdCF.constructContentMap(accType,fundObjId,fundAccVer,"create",String.valueOf(ts2)));
        accSH.put("content",gdCF.constructContentMap(accType,SHObjId,shAccVer,"create",String.valueOf(ts2)));

        assertEquals(String.valueOf(gdClient + 1),personSubVer);

        //账户的如下字段默认引用的是开户主体的对象标识
        account_subject_ref = cltNo;

        //需要将比较的对象标识增加版本号信息
        String[] verForSub = new String[]{"/" + subSIQCRefVer};
        String[] verForAccSH = new String[]{"/" + personSubVer,"/" + accADrefVer,"/" + accAAARefVer};


        log.info("检查主体存证信息内容与传入一致\n" + enSubInfo.toString() + "\n" + getSubInfo.toString());
        assertEquals(replaceCertain(gdCF.matchRefMapCertVer(enSubInfo,subjectType,verForSub)),replaceCertain(getSubInfo.toString()));

        log.info("检查股权账户存证信息内容与传入一致\n" + accSH.toString() + "\n" + getSHAccInfo.toString());
        assertEquals(replaceCertain(gdCF.matchRefMapCertVer(accSH,accType,verForAccSH)),replaceCertain(getSHAccInfo.toString()));

        account_associated_account_ref = SHObjId;
        String[] verForAccFund = new String[]{"/" + personSubVer,"/" + accADrefVer,"/" + shAccVer};

        log.info("检查资金账户存证信息内容与传入一致\n" + accFund.toString() + "\n" + getFundAccInfo.toString());
        assertEquals(replaceCertain(gdCF.matchRefMapCertVer(accFund,accType,verForAccFund)),replaceCertain(getFundAccInfo.toString()));
    }


    //企业 股权类 登记
    //产品主体引用置为空
    @Test
    public void TC021_enterpriseRegisterEquityEmpty() throws Exception {
        regTestUnit("1",true);

    }


    //企业 债券类 登记
    //产品主体引用置为空
    @Test
    public void TC022_enterpriseRegisterBondEmpty() throws Exception {
        regTestUnit("2",true);
    }

    //企业 基金类 登记
    //产品主体引用置为空
    @Test
    public void TC023_enterpriseRegisterFundEmpty() throws Exception {
        regTestUnit("3",true);
    }

    //开户
    //对象标识为空
//    @Test  当前不会自动关联
    public void TC023_createAccTestEmpty() throws Exception {
        GDBeforeCondition gdBC = new GDBeforeCondition();
        Map enSubInfo = gdBC.init01PersonalSubjectInfo();
        Map accSH = gdBC.init02ShareholderAccountInfo();
        Map accFund = gdBC.init02FundAccountInfo();
        accSH.put("account_subject_ref", "");
        accSH.put("account_associated_account_ref", "");

        accFund.put("account_subject_ref", "");
        accFund.put("account_associated_account_ref", "");


        String cltNo = "test00" + Random(12);
        String shareHolderNo = "SH" + cltNo;
        String fundNo = "fund" + cltNo;

        int gdClient = -1; //Integer.parseInt(gdCF.getObjectLatestVer(cltNo));//获取当前开户主体最新版本信息

        //构造股权账户信息
        Map shareHolderInfo = new HashMap();
        accSH.put("account_object_id", shareHolderNo);  //更新账户对象标识字段
        shareHolderInfo.put("createTime", ts2);
        shareHolderInfo.put("shareholderNo", shareHolderNo);
        shareHolderInfo.put("accountInfo", accSH);

        //构造资金账户信息
        accFund.put("account_object_id", fundNo);  //更新账户对象标识字段
        Map mapFundInfo = new HashMap();
        mapFundInfo.put("createTime", ts2);
        mapFundInfo.put("fundNo", fundNo);
        mapFundInfo.put("accountInfo", accFund);

        //构造个人/投资者主体信息
        enSubInfo.put("subject_object_id", cltNo);  //更新对象标识字段
        enSubInfo.put("subject_id", "sid" + cltNo);  //更新主体标识字段

        String response = gd.GDCreateAccout(gdContractAddress, cltNo, mapFundInfo, shareHolderInfo, enSubInfo);

        commonFunc.sdkCheckTxOrSleep(net.sf.json.JSONObject.fromObject(response).getJSONObject("data"
        ).getString("txId"), utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        String txId = net.sf.json.JSONObject.fromObject(response).getJSONObject("data").getString("txId");



        //补足引用字段
        accSH.put("account_subject_ref", investorSubjectInfo.get("subject_object_id"));
//        shAccountInfo.put("account_associated_account_ref",investorSubjectInfo.get("subject_object_id"));//当前未自动填入

        accFund.put("account_subject_ref", investorSubjectInfo.get("subject_object_id"));
        accFund.put("account_associated_account_ref", shareHolderInfo.get("subject_object_id"));

        //定义相关对象标识版本变量
        String accASrefVer = "0" ;//gdCF.getObjectLatestVer(account_subject_ref);
        String accADrefVer = "2" ;// gdCF.getObjectLatestVer(account_depository_ref);
        String accAAARefVer = "2" ;// gdCF.getObjectLatestVer(account_associated_account_ref);

        String shAccVer = "0" ;// gdCF.getObjectLatestVer("SH" + cltNo);
        String fundAccVer = "0" ;// gdCF.getObjectLatestVer("fund" + cltNo);
        String personSubVer = "0" ;// gdCF.getObjectLatestVer(cltNo);

        String subSIQCRefVer = "2" ;//gdCF.getObjectLatestVer(subject_investor_qualification_certifier_ref);

        String fundObjId = fundNo;
        String SHObjId = shareHolderNo;
        //获取链上mini url的存证信息 并检查是否包含uri信息
        String subfileName = conJGFileName(cltNo,personSubVer);
        String shAccfileName = conJGFileName(SHObjId,shAccVer);
        String fundAccfileName = conJGFileName(fundObjId,fundAccVer);

        Map uriInfo = gdCF.getJGURIStoreHash(txId,subfileName,1);
        String chkSubURI = minIOEP + "/" + jgBucket + "/" + subfileName;
        String chkSHAccURI = minIOEP + "/" + jgBucket + "/" + shAccfileName;
        String chkFundAccURI = minIOEP + "/" + jgBucket + "/" + fundAccfileName;
//        log.info(uriInfo.get("storeData").toString());
//        log.info(chkSubURI);
        assertEquals(true,uriInfo.get("storeData").toString().contains(chkSubURI));
        assertEquals(true,uriInfo.get("storeData").toString().contains(chkSHAccURI));
        assertEquals(true,uriInfo.get("storeData").toString().contains(chkFundAccURI));
//        assertEquals(false,uriInfo.get("storeData").toString().contains("meta"));//确认meta信息移除


        //直接从minio上获取报送数据文件信息
        Map getSubInfo = gdCF.constructJGDataFromStr(subfileName,subjectType,"2");
        Map getFundAccInfo = gdCF.constructJGDataFromStr(fundAccfileName,accType,"1");
        Map getSHAccInfo = gdCF.constructJGDataFromStr(shAccfileName,accType,"2");


        //填充header content 信息
        enSubInfo.put("content",gdCF.constructContentMap(subjectType,cltNo,personSubVer,"create",String.valueOf(ts1)));
        accFund.put("content",gdCF.constructContentMap(accType,fundObjId,fundAccVer,"create",String.valueOf(ts2)));
        accSH.put("content",gdCF.constructContentMap(accType,SHObjId,shAccVer,"create",String.valueOf(ts2)));

        assertEquals(String.valueOf(gdClient + 1),personSubVer);

        //账户的如下字段默认引用的是开户主体的对象标识
        account_subject_ref = cltNo;

        //需要将比较的对象标识增加版本号信息
        String[] verForSub = new String[]{"/" + subSIQCRefVer};
        String[] verForAccSH = new String[]{"/" + personSubVer,"/" + accADrefVer,"/" + accAAARefVer};


        log.info("检查主体存证信息内容与传入一致\n" + enSubInfo.toString() + "\n" + getSubInfo.toString());
        assertEquals(replaceCertain(gdCF.matchRefMapCertVer(enSubInfo,subjectType,verForSub)),replaceCertain(getSubInfo.toString()));

        log.info("检查股权账户存证信息内容与传入一致\n" + accSH.toString() + "\n" + getSHAccInfo.toString());
        assertEquals(replaceCertain(gdCF.matchRefMapCertVer(accSH,accType,verForAccSH)),replaceCertain(getSHAccInfo.toString()));

        account_associated_account_ref = SHObjId;
        String[] verForAccFund = new String[]{"/" + personSubVer,"/" + accADrefVer,"/" + shAccVer};

        log.info("检查资金账户存证信息内容与传入一致\n" + accFund.toString() + "\n" + getFundAccInfo.toString());
        assertEquals(replaceCertain(gdCF.matchRefMapCertVer(accFund,accType,verForAccFund)),replaceCertain(getFundAccInfo.toString()));

    }



    @Test
    public void TC15_type1_infodisclosurePublishAndGet() throws Exception {
        infodisclosurePublishAndGetByType(1);
//        infodisclosurePublishAndGetByType(3);
//        infodisclosurePublishAndGetByType(4);
//        infodisclosurePublishAndGetByType(5);
//        infodisclosurePublishAndGetByType(6);
//        infodisclosurePublishAndGetByType(7);
//        infodisclosurePublishAndGetByType(8);
//        infodisclosurePublishAndGetByType(9);
//        infodisclosurePublishAndGetByType(10);
    }

    public void infodisclosurePublishAndGetByType(int type) throws Exception {
        disclosureType = type;
        disclosureInfo = gdBF.init07PublishInfo();
        String response= gd.GDInfoPublish(disclosureInfo);
        net.sf.json.JSONObject jsonObject= net.sf.json.JSONObject.fromObject(response);
        String txId = jsonObject.getString("data");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", net.sf.json.JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        String responseGet = gd.GDInfoPublishGet(txId);
        assertEquals("200", net.sf.json.JSONObject.fromObject(responseGet).getString("state"));
        assertEquals(false,responseGet.contains("\"data\":null"));


        log.info("检查信批存证格式化及信息内容与传入一致");
        Map tempPub = gdCF.contructPublishInfo(txId);
        log.info(tempPub.toString().replaceAll("\"",""));

        log.info(disclosureInfo.toString());
        disclosureInfo.put("letter_disclosure_object_id",tempPub.get("letter_disclosure_object_id"));
        assertEquals(disclosureInfo.toString(), tempPub.toString().replaceAll("\"",""));


    }

    @Test
    public void TC16_balanceCount() throws Exception {
        settleInfo = gdBF.init06SettleInfo();
        String response= gd.GDCapitalSettlement(settleInfo);
        net.sf.json.JSONObject jsonObject= net.sf.json.JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", net.sf.json.JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        Map tempSet = gdCF.contructSettleInfo(txId);
        log.info("检查资金结算存证格式化及信息内容与传入一致");
        log.info(tempSet.toString().replaceAll("\"","").replaceAll(" ",""));

        settleInfo.put("capita_settlement_object_id",tempSet.get("capita_settlement_object_id"));
        log.info(settleInfo.toString().replaceAll(" ",""));
        assertEquals(settleInfo.toString().replaceAll(" ",""),
                tempSet.toString().replaceAll("\"","").replaceAll(" ",""));
    }



}
