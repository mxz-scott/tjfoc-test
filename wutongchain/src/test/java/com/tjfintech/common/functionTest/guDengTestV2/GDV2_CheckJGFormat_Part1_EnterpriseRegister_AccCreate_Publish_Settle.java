package com.tjfintech.common.functionTest.guDengTestV2;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.GuDeng;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
//import net.sf.json.JSONObject;
import org.junit.*;
import org.junit.rules.TestName;
import org.junit.runners.MethodSorters;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.tjfintech.common.functionTest.guDengTestV2.GDCommonFunc.replaceCertain;
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
    }

//    @After
    public void calJGDataAfterTx()throws Exception{
        testCurMethodName = tm.getMethodName();
        GDUnitFunc uf = new GDUnitFunc();
//        uf.calJGData();
        uf.calJGDataEachHeight();
    }

    //企业 股权类 登记
    @Test
    public void TC011_enterpriseRegisterEquityCheckFormat() throws Exception {
        long shareTotals = 1000000;
        Map enSubInfo = gdBF.init01EnterpriseSubjectInfo();
        Map prodInfo = gdBF.init03EquityProductInfo();

        int gdCpmIdOldVer = Integer.parseInt(gdCF.getObjectLatestVer(gdCompanyID));//获取当前挂牌主体最新版本信息

        String response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,enSubInfo,
                prodInfo,null,null);
        String txId = net.sf.json.JSONObject.fromObject(response).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", net.sf.json.JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        //查询挂牌企业主体数据  交易上链后 数据可能还未写入合约 在此做2s内数据查询
        for(int i = 0;i<20;i++) {
            response = gd.GDMainSubjectQuery(gdContractAddress, gdCompanyID);
            if(net.sf.json.JSONObject.fromObject(response).getString("state").equals("200"))
                break;
            sleepAndSaveInfo(100);
        }
        assertEquals("200", net.sf.json.JSONObject.fromObject(response).getString("state"));



        //获取主体/产品存证hash
        String query = store.GetTxDetail(txId);
        String questInfo = net.sf.json.JSONObject.fromObject(query).getJSONObject("data").getJSONObject("wvm"
        ).getJSONObject("wvmContractTx").getJSONObject("arg").getJSONArray("args").get(0).toString();

        //获取监管数据存证hash
        String jgType = prodType;
        String ProductInfoTxId = gdCF.getJGStoreHash2(txId,jgType,1);
        jgType = subjectType;//jgType = "主体";
        String SubjectObjectTxId = gdCF.getJGStoreHash2(txId,jgType,1);


        Map getSubInfo = gdCF.contructEnterpriseSubInfo(SubjectObjectTxId);
        Map getProInfo = gdCF.contructEquityProdInfo(ProductInfoTxId);
        if(bChkHeader) {
            String timeStampSub = gdCF.getTimeStampFromMap(getSubInfo, "subject_create_time");
            String timeStampProd = gdCF.getTimeStampFromMap(getProInfo, "product_create_time");
            enSubInfo.put("content",
                gdCF.constructContentMap(subjectType,gdCompanyID,String.valueOf(verTemp),"create",
                        timeStampSub));
            prodInfo.put("content",
                gdCF.constructContentMap(prodType,gdEquityCode,String.valueOf(verTemp),"create",
                        timeStampProd));
        }
        product_issuer_subject_ref = gdCompanyID;

        assertEquals(String.valueOf(0),gdCF.getObjectLatestVer(subject_investor_qualification_certifier_ref));
        assertEquals(String.valueOf(0),gdCF.getObjectLatestVer(product_market_subject_ref));
        assertEquals(String.valueOf(0),gdCF.getObjectLatestVer(gdCompanyID));
        assertEquals(String.valueOf(gdCpmIdOldVer + 1),gdCF.getObjectLatestVer(service_provider_subject_ref));

        String[] verForSub = new String[]{"/" + gdCF.getObjectLatestVer(subject_investor_qualification_certifier_ref)};
        String[] verForProd = new String[]{"/" + gdCF.getObjectLatestVer(product_market_subject_ref),
                                            "/" + gdCF.getObjectLatestVer(gdCompanyID),
                                            "/" + gdCF.getObjectLatestVer(service_provider_subject_ref)};

        log.info("检查主体存证信息内容与传入一致\n" + enSubInfo.toString() + "\n" + getSubInfo.toString());
        assertEquals(replaceCertain(gdCF.matchRefMapCertVer(enSubInfo,subjectType,verForSub)),replaceCertain(getSubInfo.toString()));

        log.info("检查产品存证信息内容与传入一致\n" + prodInfo.toString() + "\n" + getProInfo.toString());
        assertEquals(replaceCertain(gdCF.matchRefMapCertVer(prodInfo,prodType,verForProd)),replaceCertain(getProInfo.toString()));
    }




    //企业 债券类 登记
    @Test
    public void TC012_enterpriseRegisterBondCheckFormat() throws Exception {
        long shareTotals = 1000000;
        Map enSubInfo = gdBF.init01EnterpriseSubjectInfo();
        Map prodInfo = gdBF.init03BondProductInfo();

        int gdCpmIdOldVer = Integer.parseInt(gdCF.getObjectLatestVer(gdCompanyID));//获取当前挂牌主体最新版本信息

        String response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,enSubInfo,
                null,prodInfo,null);
        String txId = net.sf.json.JSONObject.fromObject(response).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", net.sf.json.JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        //查询挂牌企业主体数据  交易上链后 数据可能还未写入合约 在此做2s内数据查询
        response = gd.GDMainSubjectQuery(gdContractAddress, gdCompanyID);
        assertEquals("200", net.sf.json.JSONObject.fromObject(response).getString("state"));

        //获取主体/产品存证hash
        String query = store.GetTxDetail(txId);
        String questInfo = net.sf.json.JSONObject.fromObject(query).getJSONObject("data").getJSONObject("wvm"
        ).getJSONObject("wvmContractTx").getJSONObject("arg").getJSONArray("args").get(0).toString();
        JSONObject jsonObject = JSONObject.parseObject(questInfo);

        //获取监管数据存证hash
        String jgType = prodType;
        String ProductInfoTxId = gdCF.getJGStoreHash2(txId,jgType,1);
        jgType = subjectType;
        String SubjectObjectTxId = gdCF.getJGStoreHash2(txId,jgType,1);


        Map getSubInfo = gdCF.contructEnterpriseSubInfo(SubjectObjectTxId);
        Map getProInfo = gdCF.contructBondProdInfo(ProductInfoTxId);


        if(bChkHeader) {
            String timeStampSub = gdCF.getTimeStampFromMap(getSubInfo, "subject_create_time");
            String timeStampProd = gdCF.getTimeStampFromMap(getProInfo, "product_create_time");
            enSubInfo.put("content",
                    gdCF.constructContentMap(subjectType,gdCompanyID,String.valueOf(verTemp),"create",
                            timeStampSub));
            prodInfo.put("content",
                    gdCF.constructContentMap(prodType,gdEquityCode,String.valueOf(verTemp),"create",
                            timeStampProd));
        }

        assertEquals(String.valueOf(0),gdCF.getObjectLatestVer(subject_investor_qualification_certifier_ref));
        assertEquals(String.valueOf(0),gdCF.getObjectLatestVer(product_market_subject_ref));
        assertEquals(String.valueOf(0),gdCF.getObjectLatestVer(gdCompanyID));
        assertEquals(String.valueOf(gdCpmIdOldVer + 1),gdCF.getObjectLatestVer(service_provider_subject_ref));

        log.info("检查主体存证信息内容与传入一致\n" + enSubInfo.toString() + "\n" + getSubInfo.toString());
        assertEquals(replaceCertain(gdCF.matchRefMapCertVer2(enSubInfo,subjectType)),replaceCertain(getSubInfo.toString()));

        log.info("检查产品存证信息内容与传入一致\n" + prodInfo.toString() + "\n" + getProInfo.toString());
        assertEquals(replaceCertain(gdCF.matchRefMapCertVer2(prodInfo,prodType)),replaceCertain(getProInfo.toString()));

    }

    //企业 基金类 登记
    @Test
    public void TC013_enterpriseRegisterFundCheckFormat() throws Exception {
        long shareTotals = 1000000;
        Map enSubInfo = gdBF.init01EnterpriseSubjectInfo();
        Map prodInfo = gdBF.init03FundProductInfo();

        int gdCpmIdOldVer = Integer.parseInt(gdCF.getObjectLatestVer(gdCompanyID));//获取当前挂牌主体最新版本信息

        String response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,enSubInfo,
                null,null,prodInfo);
        String txId = net.sf.json.JSONObject.fromObject(response).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", net.sf.json.JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        //查询挂牌企业主体数据  交易上链后 数据可能还未写入合约 在此做2s内数据查询
        for(int i = 0;i<20;i++) {
            response = gd.GDMainSubjectQuery(gdContractAddress, gdCompanyID);
            if(net.sf.json.JSONObject.fromObject(response).getString("state").equals("200"))
                break;
            sleepAndSaveInfo(100);
        }
        assertEquals("200", net.sf.json.JSONObject.fromObject(response).getString("state"));



        //获取主体/产品存证hash
        String query = store.GetTxDetail(txId);
        String questInfo = net.sf.json.JSONObject.fromObject(query).getJSONObject("data").getJSONObject("wvm"
        ).getJSONObject("wvmContractTx").getJSONObject("arg").getJSONArray("args").get(0).toString();
        JSONObject jsonObject = JSONObject.parseObject(questInfo);

        //获取监管数据存证hash
        String jgType = prodType;
        String ProductInfoTxId = gdCF.getJGStoreHash2(txId,jgType,1);
        jgType = subjectType;
        String SubjectObjectTxId = gdCF.getJGStoreHash2(txId,jgType,1);

        Map getSubInfo = gdCF.contructEnterpriseSubInfo(SubjectObjectTxId);
        Map getProInfo = gdCF.contructFundProdInfo(ProductInfoTxId);


        if(bChkHeader) {
            String timeStampSub = gdCF.getTimeStampFromMap(getSubInfo, "subject_create_time");
            String timeStampProd = gdCF.getTimeStampFromMap(getProInfo, "product_create_time");
            enSubInfo.put("content",
                    gdCF.constructContentMap(subjectType,gdCompanyID,String.valueOf(verTemp),"create",
                            timeStampSub));
            prodInfo.put("content",
                    gdCF.constructContentMap(prodType,gdEquityCode,String.valueOf(verTemp),"create",
                            timeStampProd));
        }

        assertEquals(String.valueOf(0),gdCF.getObjectLatestVer(subject_investor_qualification_certifier_ref));
        assertEquals(String.valueOf(0),gdCF.getObjectLatestVer(product_market_subject_ref));
        assertEquals(String.valueOf(0),gdCF.getObjectLatestVer(gdCompanyID));
        assertEquals(String.valueOf(gdCpmIdOldVer + 1),gdCF.getObjectLatestVer(service_provider_subject_ref));

        String[] verForSub = new String[]{"/" + gdCF.getObjectLatestVer(subject_investor_qualification_certifier_ref)};
        String[] verForProd = new String[]{"/" + gdCF.getObjectLatestVer(product_market_subject_ref),
                "/" + gdCF.getObjectLatestVer(gdCompanyID),
                "/" + gdCF.getObjectLatestVer(service_provider_subject_ref)};

        log.info("检查主体存证信息内容与传入一致\n" + enSubInfo.toString() + "\n" + getSubInfo.toString());
        assertEquals(replaceCertain(gdCF.matchRefMapCertVer(enSubInfo,subjectType,verForSub)),replaceCertain(getSubInfo.toString()));

        log.info("检查产品存证信息内容与传入一致\n" + prodInfo.toString() + "\n" + getProInfo.toString());
        assertEquals(replaceCertain(gdCF.matchRefMapCertVer(prodInfo,prodType,verForProd)),replaceCertain(getProInfo.toString()));
    }

    //机构 会员登记
    @Test
    public void TC013_enterpriseRegisterMemberCheckFormat() throws Exception {
        Map enSubInfo = gdBF.init01EnterpriseSubjectInfo();

        int gdCpmIdOldVer = Integer.parseInt(gdCF.getObjectLatestVer(gdCompanyID));//获取当前挂牌主体最新版本信息

        String response= gd.GDEnterpriseResister(gdContractAddress,"",0,enSubInfo,
                null,null,null);
        String txId = net.sf.json.JSONObject.fromObject(response).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", net.sf.json.JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        //查询挂牌企业主体数据  交易上链后 数据可能还未写入合约 在此做2s内数据查询
        for(int i = 0;i<20;i++) {
            response = gd.GDMainSubjectQuery(gdContractAddress, gdCompanyID);
            if(net.sf.json.JSONObject.fromObject(response).getString("state").equals("200"))
                break;
            sleepAndSaveInfo(100);
        }
        assertEquals("200", net.sf.json.JSONObject.fromObject(response).getString("state"));


        //获取主体/产品存证hash
        String query = store.GetTxDetail(txId);
        String questInfo = net.sf.json.JSONObject.fromObject(query).getJSONObject("data").getJSONObject("wvm"
                ).getJSONObject("wvmContractTx").getJSONObject("arg").getJSONArray("args").get(0).toString();
        JSONObject jsonObject = JSONObject.parseObject(questInfo);

        //获取监管数据存证hash
        String jgType = prodType;
        String ProductInfoTxId = gdCF.getJGStoreHash2(txId,jgType,1);
        jgType = subjectType;
        String SubjectObjectTxId = gdCF.getJGStoreHash2(txId,jgType,1);


        Map getSubInfo = gdCF.contructEnterpriseSubInfo(SubjectObjectTxId);
        if(bChkHeader) {
            String timeStampSub = gdCF.getTimeStampFromMap(getSubInfo, "subject_create_time");
            enSubInfo.put("content",
                    gdCF.constructContentMap(subjectType,gdCompanyID,String.valueOf(verTemp),"create",
                            timeStampSub));
        }


        assertEquals(String.valueOf(0),gdCF.getObjectLatestVer(subject_investor_qualification_certifier_ref));
        assertEquals(String.valueOf(0),gdCF.getObjectLatestVer(product_market_subject_ref));
        assertEquals(String.valueOf(0),gdCF.getObjectLatestVer(gdCompanyID));
        assertEquals(String.valueOf(gdCpmIdOldVer + 1),gdCF.getObjectLatestVer(service_provider_subject_ref));

        log.info("检查主体存证信息内容与传入一致\n" + enSubInfo.toString() + "\n" + getSubInfo.toString());
        assertEquals(replaceCertain(gdCF.matchRefMapCertVer2(enSubInfo,subjectType)),replaceCertain(getSubInfo.toString()));

    }

    @Test
    public void TC014_createAccTestCheckFormat() throws Exception {
        GDBeforeCondition gdBC = new GDBeforeCondition();

        String cltNo = "tet00" + Random(12);
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
        assertEquals("200", net.sf.json.JSONObject.fromObject(response).getString("state"));

        //获取监管数据存证hash
        String jgType = accType;
        String accStoreId = gdCF.getJGStoreHash2(txId,jgType,1);
        jgType = subjectType;
        String subStoreId = gdCF.getJGStoreHash2(txId,jgType,1);



        Map getSubInfo = gdCF.contructPersonalSubInfo(subStoreId);
        Map getFundAccInfo = gdCF.contructFundAccountInfo(accStoreId,"fund" + cltNo);
        Map getSHAccInfo = gdCF.contructEquityAccountInfo(accStoreId,"SH" + cltNo);

        Map enSubInfo = investorSubjectInfo;
        Map accFund = fundAccountInfo;
        Map accSH = shAccountInfo;

        if(bChkHeader) {
            String timeStampSub = gdCF.getTimeStampFromMap(getSubInfo, "subject_create_time");
            String timeStampFundAcc = gdCF.getTimeStampFromMap(getFundAccInfo, "account_create_time");
            String timeStampSHAcc = gdCF.getTimeStampFromMap(getSHAccInfo, "account_create_time");
            enSubInfo.put("content",
                    gdCF.constructContentMap(subjectType,cltNo,String.valueOf(verTemp),"create",
                            timeStampSub));
            accFund.put("content",
                    gdCF.constructContentMap(accType,"fund" + cltNo,String.valueOf(verTemp),"create",
                            timeStampFundAcc));
            accSH.put("content",
                    gdCF.constructContentMap(accType,"SH" + cltNo,String.valueOf(verTemp),"create",
                            timeStampSHAcc));
        }

        String[] verForSub = new String[]{"/0"};
        String[] verForAccSH = new String[]{"/0","/0"};
        String[] verForAcc = new String[]{"/0","/0","/0"};

        log.info("检查主体存证信息内容与传入一致\n" + enSubInfo.toString() + "\n" + getSubInfo.toString());
        assertEquals(replaceCertain(gdCF.matchRefMapCertVer(enSubInfo,subjectType,verForSub)),replaceCertain(getSubInfo.toString()));

        log.info("检查股权账户存证信息内容与传入一致\n" + accSH.toString() + "\n" + getSHAccInfo.toString());
        assertEquals(replaceCertain(gdCF.matchRefMapCertVer(accSH,accSHType,verForAccSH)),replaceCertain(getSHAccInfo.toString()));

        log.info("检查资金账户存证信息内容与传入一致\n" + accFund.toString() + "\n" + getFundAccInfo.toString());
        assertEquals(replaceCertain(gdCF.matchRefMapCertVer(accFund,accType,verForAcc)),replaceCertain(getFundAccInfo.toString()));

    }


    //企业 股权类 登记
    //产品主体引用置为空
    @Test
    public void TC021_enterpriseRegisterEquityEmpty() throws Exception {
        Map enSubInfo = gdBF.init01EnterpriseSubjectInfo();
        Map prodInfo = gdBF.init03EquityProductInfo();

        int gdCpmIdOldVer = Integer.parseInt(gdCF.getObjectLatestVer(gdCompanyID));//获取当前挂牌主体最新版本信息

        prodInfo.put("product_issuer_subject_ref","");
        long shareTotals = 1000000;
        String response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,enSubInfo,
                prodInfo,null,null);
        String txId = net.sf.json.JSONObject.fromObject(response).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",net.sf.json.JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));


        //获取主体/产品存证hash
        String query = store.GetTxDetail(txId);
        String questInfo = net.sf.json.JSONObject.fromObject(query).getJSONObject("data").getJSONObject("wvm"
                ).getJSONObject("wvmContractTx").getJSONObject("arg").getJSONArray("args").get(0).toString();


        //获取监管数据存证hash
        String jgType = prodType;
        String ProductInfoTxId = gdCF.getJGStoreHash2(txId,jgType,1);
        jgType = subjectType;//jgType = "主体";
        String SubjectObjectTxId = gdCF.getJGStoreHash2(txId,jgType,1);


        Map getSubInfo = gdCF.contructEnterpriseSubInfo(SubjectObjectTxId);
        Map getProInfo = gdCF.contructEquityProdInfo(ProductInfoTxId);
        if(bChkHeader) {
            String timeStampSub = gdCF.getTimeStampFromMap(getSubInfo, "subject_create_time");
            String timeStampProd = gdCF.getTimeStampFromMap(getProInfo, "product_create_time");
            enSubInfo.put("content",
                    gdCF.constructContentMap(subjectType,gdCompanyID,String.valueOf(verTemp),"create",
                            timeStampSub));
            prodInfo.put("content",
                    gdCF.constructContentMap(prodType,gdEquityCode,String.valueOf(verTemp),"create",
                            timeStampProd));
        }
        prodInfo.put("product_issuer_subject_ref",enSubInfo.get("subject_object_id").toString());

        assertEquals(String.valueOf(0),gdCF.getObjectLatestVer(subject_investor_qualification_certifier_ref));
        assertEquals(String.valueOf(0),gdCF.getObjectLatestVer(product_market_subject_ref));
        assertEquals(String.valueOf(0),gdCF.getObjectLatestVer(gdCompanyID));
        assertEquals(String.valueOf(gdCpmIdOldVer + 1),gdCF.getObjectLatestVer(service_provider_subject_ref));

        log.info("检查主体存证信息内容与传入一致\n" + enSubInfo.toString() + "\n" + getSubInfo.toString());
        assertEquals(replaceCertain(gdCF.matchRefMapCertVer2(enSubInfo,subjectType)),replaceCertain(getSubInfo.toString()));

        log.info("检查产品存证信息内容与传入一致\n" + prodInfo.toString() + "\n" + getProInfo.toString());
        assertEquals(replaceCertain(gdCF.matchRefMapCertVer2(prodInfo,prodType)),replaceCertain(getProInfo.toString()));

    }


    //企业 债券类 登记
    //产品主体引用置为空
    @Test
    public void TC022_enterpriseRegisterBondEmpty() throws Exception {
        Map enSubInfo = gdBF.init01EnterpriseSubjectInfo();
        Map prodInfo = gdBF.init03BondProductInfo();

        int gdCpmIdOldVer = Integer.parseInt(gdCF.getObjectLatestVer(gdCompanyID));//获取当前挂牌主体最新版本信息

        prodInfo.put("product_issuer_subject_ref","");
        long shareTotals = 1000000;
        String response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,enSubInfo,
                null,prodInfo,null);
        String txId =  net.sf.json.JSONObject.fromObject(response).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",net.sf.json.JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        //获取主体/产品存证hash
        String query = store.GetTxDetail(txId);
        String questInfo = net.sf.json.JSONObject.fromObject(query).getJSONObject("data").getJSONObject("wvm"
                    ).getJSONObject("wvmContractTx").getJSONObject("arg").getJSONArray("args").get(0).toString();
        JSONObject jsonObject = JSONObject.parseObject(questInfo);

        //获取监管数据存证hash
        String jgType = prodType;
        String ProductInfoTxId = gdCF.getJGStoreHash2(txId,jgType,1);
        jgType = subjectType;
        String SubjectObjectTxId = gdCF.getJGStoreHash2(txId,jgType,1);


        Map getSubInfo = gdCF.contructEnterpriseSubInfo(SubjectObjectTxId);
        Map getProInfo = gdCF.contructBondProdInfo(ProductInfoTxId);

        if(bChkHeader) {
            String timeStampSub = gdCF.getTimeStampFromMap(getSubInfo, "subject_create_time");
            String timeStampProd = gdCF.getTimeStampFromMap(getProInfo, "product_create_time");
            enSubInfo.put("content",
                    gdCF.constructContentMap(subjectType,gdCompanyID,String.valueOf(verTemp),"create",
                            timeStampSub));
            prodInfo.put("content",
                    gdCF.constructContentMap(prodType,gdEquityCode,String.valueOf(verTemp),"create",
                            timeStampProd));
        }
        prodInfo.put("product_issuer_subject_ref",enSubInfo.get("subject_object_id").toString());

        assertEquals(String.valueOf(0),gdCF.getObjectLatestVer(subject_investor_qualification_certifier_ref));
        assertEquals(String.valueOf(0),gdCF.getObjectLatestVer(product_market_subject_ref));
        assertEquals(String.valueOf(0),gdCF.getObjectLatestVer(gdCompanyID));
        assertEquals(String.valueOf(gdCpmIdOldVer + 1),gdCF.getObjectLatestVer(service_provider_subject_ref));

        log.info("检查主体存证信息内容与传入一致\n" + enSubInfo.toString() + "\n" + getSubInfo.toString());
        assertEquals(replaceCertain(gdCF.matchRefMapCertVer2(enSubInfo,subjectType)),replaceCertain(getSubInfo.toString()));

        log.info("检查产品存证信息内容与传入一致\n" + prodInfo.toString() + "\n" + getProInfo.toString());
        assertEquals(replaceCertain(gdCF.matchRefMapCertVer2(prodInfo,prodType)),replaceCertain(getProInfo.toString()));
    }

    //企业 基金类 登记
    //产品主体引用置为空
    @Test
    public void TC023_enterpriseRegisterFundEmpty() throws Exception {
        Map enSubInfo = gdBF.init01EnterpriseSubjectInfo();
        Map prodInfo = gdBF.init03FundProductInfo();

        int gdCpmIdOldVer = Integer.parseInt(gdCF.getObjectLatestVer(gdCompanyID));//获取当前挂牌主体最新版本信息

        prodInfo.put("product_issuer_subject_ref","");
        long shareTotals = 1000000;
        String response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,enSubInfo,
                null,null,prodInfo);
        String txId =  net.sf.json.JSONObject.fromObject(response).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",net.sf.json.JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        //获取主体/产品存证hash
        String query = store.GetTxDetail(txId);
        String questInfo = net.sf.json.JSONObject.fromObject(query).getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getJSONObject("arg").getJSONArray("args").get(0).toString();


        //获取监管数据存证hash
        String jgType = prodType;
        String ProductInfoTxId = gdCF.getJGStoreHash2(txId,jgType,1);
        jgType = subjectType;
        String SubjectObjectTxId = gdCF.getJGStoreHash2(txId,jgType,1);


        Map getSubInfo = gdCF.contructEnterpriseSubInfo(SubjectObjectTxId);
        Map getProInfo = gdCF.contructFundProdInfo(ProductInfoTxId);

        if(bChkHeader) {
            String timeStampSub = gdCF.getTimeStampFromMap(getSubInfo, "subject_create_time");
            String timeStampProd = gdCF.getTimeStampFromMap(getProInfo, "product_create_time");
            enSubInfo.put("content",
                    gdCF.constructContentMap(subjectType,gdCompanyID,String.valueOf(verTemp),"create",
                            timeStampSub));
            prodInfo.put("content",
                    gdCF.constructContentMap(prodType,gdEquityCode,String.valueOf(verTemp),"create",
                            timeStampProd));
        }
        prodInfo.put("product_issuer_subject_ref",enSubInfo.get("subject_object_id").toString());

        assertEquals(String.valueOf(0),gdCF.getObjectLatestVer(subject_investor_qualification_certifier_ref));
        assertEquals(String.valueOf(0),gdCF.getObjectLatestVer(product_market_subject_ref));
        assertEquals(String.valueOf(0),gdCF.getObjectLatestVer(gdCompanyID));
        assertEquals(String.valueOf(gdCpmIdOldVer + 1),gdCF.getObjectLatestVer(service_provider_subject_ref));

        log.info("检查主体存证信息内容与传入一致\n" + enSubInfo.toString() + "\n" + getSubInfo.toString());
        assertEquals(replaceCertain(gdCF.matchRefMapCertVer2(enSubInfo,subjectType)),replaceCertain(getSubInfo.toString()));

        log.info("检查产品存证信息内容与传入一致\n" + prodInfo.toString() + "\n" + getProInfo.toString());
        assertEquals(replaceCertain(gdCF.matchRefMapCertVer2(prodInfo,prodType)),replaceCertain(getProInfo.toString()));

    }

    //开户
    //对象标识为空
    @Test
    public void TC023_createAccTestEmpty() throws Exception {
        GDBeforeCondition gdBC = new GDBeforeCondition();
        Map shAcc = gdBC.init02ShareholderAccountInfo();
        Map fundAcc = gdBC.init02FundAccountInfo();
        shAcc.put("account_subject_ref","");
        shAcc.put("account_associated_account_ref","");

        fundAcc.put("account_subject_ref","");
        fundAcc.put("account_associated_account_ref","");


        String cltNo = "test00" + Random(12);
        String shareHolderNo = "SH" + cltNo;
        String fundNo = "fund" + cltNo;

        //构造股权账户信息
        Map shareHolderInfo = new HashMap();
        shAcc.put("account_object_id",shareHolderNo);  //更新账户对象标识字段
        shareHolderInfo.put("shareholderNo",shareHolderNo);
        shareHolderInfo.put("accountInfo", shAcc);

        //构造资金账户信息
        fundAcc.put("account_object_id",fundNo);  //更新账户对象标识字段
        Map mapFundInfo = new HashMap();
        mapFundInfo.put("fundNo",fundNo);
        mapFundInfo.put("accountInfo",fundAcc);

        //构造个人/投资者主体信息
        gdBC.init01PersonalSubjectInfo();
        investorSubjectInfo.put("subject_object_id",cltNo);  //更新对象标识字段
        investorSubjectInfo.put("subject_id","sid" + cltNo);  //更新主体标识字段

        String response = gd.GDCreateAccout(gdContractAddress,cltNo,mapFundInfo,shareHolderInfo, investorSubjectInfo);
        commonFunc.sdkCheckTxOrSleep(net.sf.json.JSONObject.fromObject(response).getJSONObject("data"
                    ).getString("txId"),utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        String txId = net.sf.json.JSONObject.fromObject(response).getJSONObject("data").getString("txId");

        //获取监管数据存证hash
        String jgType = accType;
        String accStoreId = gdCF.getJGStoreHash2(txId,jgType,1);
        jgType = subjectType;
        String subStoreId = gdCF.getJGStoreHash2(txId,jgType,1);

        //补足引用字段
        shAcc.put("account_subject_ref",investorSubjectInfo.get("subject_object_id"));
//        shAccountInfo.put("account_associated_account_ref",investorSubjectInfo.get("subject_object_id"));//当前未自动填入

        fundAcc.put("account_subject_ref",investorSubjectInfo.get("subject_object_id"));
        fundAcc.put("account_associated_account_ref",shareHolderInfo.get("subject_object_id"));

        Map getSubInfo = gdCF.contructPersonalSubInfo(subStoreId);
        Map getFundAccInfo = gdCF.contructFundAccountInfo(accStoreId,"fund" + cltNo);
        Map getSHAccInfo = gdCF.contructEquityAccountInfo(accStoreId,"SH" + cltNo);


        Map enSubInfo = investorSubjectInfo;


        if(bChkHeader) {
            String timeStampSub = gdCF.getTimeStampFromMap(getSubInfo, "subject_create_time");
            String timeStampFundAcc = gdCF.getTimeStampFromMap(getFundAccInfo, "account_create_time");
            String timeStampSHAcc = gdCF.getTimeStampFromMap(getSHAccInfo, "account_create_time");
            enSubInfo.put("content",
                    gdCF.constructContentMap(subjectType,cltNo,String.valueOf(verTemp),"create",
                            timeStampSub));
            fundAcc.put("content",
                    gdCF.constructContentMap(accType,"fund" + cltNo,String.valueOf(verTemp),"create",
                            timeStampFundAcc));
            shAcc.put("content",
                    gdCF.constructContentMap(accType,"SH" + cltNo,String.valueOf(verTemp),"create",
                            timeStampSHAcc));
        }

        log.info("检查主体存证信息内容与传入一致\n" + enSubInfo.toString() + "\n" + getSubInfo.toString());
        assertEquals(replaceCertain(enSubInfo.toString()),replaceCertain(getSubInfo.toString()));

        log.info("检查股权账户存证信息内容与传入一致\n" + shAcc.toString() + "\n" + getSHAccInfo.toString());
        assertEquals(replaceCertain(shAcc.toString()),replaceCertain(getSHAccInfo.toString()));

        log.info("检查资金账户存证信息内容与传入一致\n" + fundAcc.toString() + "\n" + getFundAccInfo.toString());
        assertEquals(replaceCertain(fundAcc.toString()),replaceCertain(getFundAccInfo.toString()));

    }



    @Test
    public void TC15_infodisclosurePublishAndGet() throws Exception {
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
