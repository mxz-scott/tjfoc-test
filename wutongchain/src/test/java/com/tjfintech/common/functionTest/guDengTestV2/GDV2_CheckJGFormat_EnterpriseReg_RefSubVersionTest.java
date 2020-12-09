package com.tjfintech.common.functionTest.guDengTestV2;

import com.alibaba.fastjson.JSONObject;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.GuDeng;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import org.junit.*;
import org.junit.rules.TestName;
import org.junit.runners.MethodSorters;

import java.util.HashMap;
import java.util.Map;

import static com.tjfintech.common.functionTest.guDengTestV2.GDCommonFunc.replaceCertain;
import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassGD.*;
import static org.junit.Assert.assertEquals;

//import net.sf.json.JSONObject;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class GDV2_CheckJGFormat_EnterpriseReg_RefSubVersionTest {

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
    String SIQCRef = "SIQCRef" + Random(5);
    String PMSRef = "PMSRef" + Random(5);
    String SPSRef = "SPSRef" + Random(5);

    @Rule
    public TestName tm = new TestName();


    @BeforeClass
    public static void Before()throws Exception{
        GDBeforeCondition gdBefore = new GDBeforeCondition();
//        gdBefore.gdCreateAccout();
        gdBefore.initRegulationData();
        gdBefore.initCommonRefSubAndReg();
    }

    @Before
    public void BeforeTest()throws Exception {
        gdEquityCode = Random(20);
    }

    public void updateSubjectObject(String objectId){
        Map mapTemp = gdBF.init01EnterpriseSubjectInfo();
        mapTemp.put("subject_object_id",objectId);
        gd.GDUpdateSubjectInfo(gdContractAddress,1,mapTemp);
    }



//    @After
    public void calJGDataAfterTx()throws Exception{
        testCurMethodName = tm.getMethodName();
        GDUnitFunc uf = new GDUnitFunc();
//        uf.calJGData();
        uf.calJGDataEachHeight();
    }

    //企业 股权类 登记
    //主体 字段涉及引用
    //不必填字段 适当性认证方主体引用	subject_investor_qualification_certifier_ref
    //产品 字段涉及引用
    //必填字段 交易场所主体引用	product_market_subject_ref
    //必填字段 发行主体引用	product_issuer_subject_ref  已确定须引用同一个请求中的登记机构主体
    //不必填字段 服务方主体引用	service_provider_subject_ref

    /***
     * 引用所有主体均为当前最新版本
     * @throws Exception
     */
    @Test
    public void TC01_enterpriseRegisterRefTest() throws Exception {
        long shareTotals = 1000000;
        Map enSubInfo = gdBF.init01EnterpriseSubjectInfo();
        Map prodInfo = gdBF.init03EquityProductInfo();

        int gdCpmIdOldVer = Integer.parseInt(gdCF.getObjectLatestVer(gdCompanyID));//获取当前挂牌主体最新版本信息

        String response = gd.GDEnterpriseResister(gdContractAddress, gdEquityCode, shareTotals, enSubInfo,
                prodInfo, null, null);
        String txId = net.sf.json.JSONObject.fromObject(response).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        assertEquals("200", net.sf.json.JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        //查询挂牌企业主体数据  交易上链后 数据可能还未写入合约 在此做2s内数据查询
        response = gd.GDMainSubjectQuery(gdContractAddress, gdCompanyID);
        assertEquals("200", net.sf.json.JSONObject.fromObject(response).getString("state"));


        //获取主体/产品存证hash
        String query = store.GetTxDetail(txId);
        String questInfo = net.sf.json.JSONObject.fromObject(query).getJSONObject("data").getJSONObject("wvm"
        ).getJSONObject("wvmContractTx").getJSONObject("arg").getJSONArray("args").get(0).toString();

        //获取监管数据存证hash
        String jgType = prodType;
        String ProductInfoTxId = gdCF.getJGStoreHash2(txId, jgType, 1);
        jgType = subjectType;//jgType = "主体";
        String SubjectObjectTxId = gdCF.getJGStoreHash2(txId, jgType, 1);


        Map getSubInfo = gdCF.contructEnterpriseSubInfo(SubjectObjectTxId);
        Map getProInfo = gdCF.contructEquityProdInfo(ProductInfoTxId);
        String timeStampSub = gdCF.getTimeStampFromMap(getSubInfo, "subject_create_time");
        String timeStampProd = gdCF.getTimeStampFromMap(getProInfo, "product_create_time");
        enSubInfo.put("content",
                gdCF.constructContentMap(subjectType, gdCompanyID, String.valueOf(verTemp), "create",
                        timeStampSub));
        prodInfo.put("content",
                gdCF.constructContentMap(prodType, gdEquityCode, String.valueOf(verTemp), "create",
                        timeStampProd));


        assertEquals(String.valueOf(0), gdCF.getObjectLatestVer(subject_investor_qualification_certifier_ref));
        assertEquals(String.valueOf(0), gdCF.getObjectLatestVer(product_market_subject_ref));
        assertEquals(String.valueOf(gdCpmIdOldVer + 1), gdCF.getObjectLatestVer(gdCompanyID));
        assertEquals(String.valueOf(0), gdCF.getObjectLatestVer(service_provider_subject_ref));

        String[] verForSub = new String[]{"/" + gdCF.getObjectLatestVer(subject_investor_qualification_certifier_ref)};
        String[] verForProd = new String[]{"/" + gdCF.getObjectLatestVer(product_market_subject_ref),
                "/" + gdCF.getObjectLatestVer(gdCompanyID),
                "/" + gdCF.getObjectLatestVer(service_provider_subject_ref)};

        log.info("检查主体存证信息内容与传入一致\n" + enSubInfo.toString() + "\n" + getSubInfo.toString());
        assertEquals(replaceCertain(gdCF.matchRefMapCertVer(enSubInfo, subjectType, verForSub)), replaceCertain(getSubInfo.toString()));

        log.info("检查产品存证信息内容与传入一致\n" + prodInfo.toString() + "\n" + getProInfo.toString());
        assertEquals(replaceCertain(gdCF.matchRefMapCertVer(prodInfo, prodType, verForProd)), replaceCertain(getProInfo.toString()));
    }

    /***
     * 引用所有主体均其中一个引用subject_investor_qualification_certifier_ref 在执行前有一个版本更新
     * @throws Exception
     */
    @Test
    public void TC02_enterpriseRegisterRefTest() throws Exception {
        long shareTotals = 1000000;
        Map enSubInfo = gdBF.init01EnterpriseSubjectInfo();
        Map prodInfo = gdBF.init03EquityProductInfo();

        int gdCpmIdOldVer = Integer.parseInt(gdCF.getObjectLatestVer(gdCompanyID));//获取当前挂牌主体最新版本信息
        int gdRefIdOldVer = Integer.parseInt(gdCF.getObjectLatestVer(subject_investor_qualification_certifier_ref));//获取当前引用主体最新版本信息
        //更新待引用的subject_investor_qualification_certifier_ref 主体版本
        updateSubjectObject(subject_investor_qualification_certifier_ref);
//        sleepAndSaveInfo(2000);
//        assertEquals(String.valueOf(gdRefIdOldVer + 1),gdCF.getObjectLatestVer(subject_investor_qualification_certifier_ref));

        String response = gd.GDEnterpriseResister(gdContractAddress, gdEquityCode, shareTotals, enSubInfo,
                prodInfo, null, null);
        String txId = net.sf.json.JSONObject.fromObject(response).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        assertEquals("200", net.sf.json.JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        //查询挂牌企业主体数据  交易上链后 数据可能还未写入合约 在此做2s内数据查询
        response = gd.GDMainSubjectQuery(gdContractAddress, gdCompanyID);
        assertEquals("200", net.sf.json.JSONObject.fromObject(response).getString("state"));


        //获取主体/产品存证hash
        String query = store.GetTxDetail(txId);
        String questInfo = net.sf.json.JSONObject.fromObject(query).getJSONObject("data").getJSONObject("wvm"
        ).getJSONObject("wvmContractTx").getJSONObject("arg").getJSONArray("args").get(0).toString();

        //获取监管数据存证hash
        String jgType = prodType;
        String ProductInfoTxId = gdCF.getJGStoreHash2(txId, jgType, 1);
        jgType = subjectType;//jgType = "主体";
        String SubjectObjectTxId = gdCF.getJGStoreHash2(txId, jgType, 1);


        Map getSubInfo = gdCF.contructEnterpriseSubInfo(SubjectObjectTxId);
        Map getProInfo = gdCF.contructEquityProdInfo(ProductInfoTxId);
        String timeStampSub = gdCF.getTimeStampFromMap(getSubInfo, "subject_create_time");
        String timeStampProd = gdCF.getTimeStampFromMap(getProInfo, "product_create_time");
        enSubInfo.put("content",
                gdCF.constructContentMap(subjectType, gdCompanyID, String.valueOf(verTemp), "create",
                        timeStampSub));
        prodInfo.put("content",
                gdCF.constructContentMap(prodType, gdEquityCode, String.valueOf(verTemp), "create",
                        timeStampProd));


        assertEquals(String.valueOf(gdRefIdOldVer + 1), gdCF.getObjectLatestVer(subject_investor_qualification_certifier_ref));
        assertEquals(String.valueOf(0), gdCF.getObjectLatestVer(product_market_subject_ref));
        assertEquals(String.valueOf(gdCpmIdOldVer + 1), gdCF.getObjectLatestVer(gdCompanyID));
        assertEquals(String.valueOf(0), gdCF.getObjectLatestVer(service_provider_subject_ref));

        String[] verForSub = new String[]{"/" + gdCF.getObjectLatestVer(subject_investor_qualification_certifier_ref)};
        String[] verForProd = new String[]{"/" + gdCF.getObjectLatestVer(product_market_subject_ref),
                "/" + gdCF.getObjectLatestVer(gdCompanyID),
                "/" + gdCF.getObjectLatestVer(service_provider_subject_ref)};

        log.info("检查主体存证信息内容与传入一致\n" + enSubInfo.toString() + "\n" + getSubInfo.toString());
        assertEquals(replaceCertain(gdCF.matchRefMapCertVer(enSubInfo, subjectType, verForSub)), replaceCertain(getSubInfo.toString()));

        log.info("检查产品存证信息内容与传入一致\n" + prodInfo.toString() + "\n" + getProInfo.toString());
        assertEquals(replaceCertain(gdCF.matchRefMapCertVer(prodInfo, prodType, verForProd)), replaceCertain(getProInfo.toString()));
    }

    /***
     * 引用所有主体分别在执行前做了一次 两次 和未更新
     * product_market_subject_ref 在执行前有两个版本更新
     * subject_investor_qualification_certifier_ref 未更新
     * service_provider_subject_ref 更新一次
     * @throws Exception
     */
    @Test
    public void TC03_enterpriseRegisterRefTest() throws Exception {
        long shareTotals = 1000000;
        Map enSubInfo = gdBF.init01EnterpriseSubjectInfo();
        Map prodInfo = gdBF.init03EquityProductInfo();

        int gdCpmIdOldVer = Integer.parseInt(gdCF.getObjectLatestVer(gdCompanyID));//获取当前挂牌主体最新版本信息
        int gdRefId1 = Integer.parseInt(gdCF.getObjectLatestVer(subject_investor_qualification_certifier_ref));//获取当前引用主体最新版本信息
        int gdRefId2 = Integer.parseInt(gdCF.getObjectLatestVer(product_market_subject_ref));//获取当前引用主体最新版本信息
        int gdRefId3 = Integer.parseInt(gdCF.getObjectLatestVer(service_provider_subject_ref));//获取当前引用主体最新版本信息

        //更新待引用的subject_investor_qualification_certifier_ref 主体版本
        updateSubjectObject(service_provider_subject_ref);

        updateSubjectObject(product_market_subject_ref);
        updateSubjectObject(product_market_subject_ref);
//        sleepAndSaveInfo(2000);
//        assertEquals(String.valueOf(gdRefIdOldVer + 1),gdCF.getObjectLatestVer(subject_investor_qualification_certifier_ref));

        String response = gd.GDEnterpriseResister(gdContractAddress, gdEquityCode, shareTotals, enSubInfo,
                prodInfo, null, null);
        String txId = net.sf.json.JSONObject.fromObject(response).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        assertEquals("200", net.sf.json.JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        //查询挂牌企业主体数据  交易上链后 数据可能还未写入合约 在此做2s内数据查询
        response = gd.GDMainSubjectQuery(gdContractAddress, gdCompanyID);
        assertEquals("200", net.sf.json.JSONObject.fromObject(response).getString("state"));


        //获取主体/产品存证hash
        String query = store.GetTxDetail(txId);
        String questInfo = net.sf.json.JSONObject.fromObject(query).getJSONObject("data").getJSONObject("wvm"
        ).getJSONObject("wvmContractTx").getJSONObject("arg").getJSONArray("args").get(0).toString();

        //获取监管数据存证hash
        String jgType = prodType;
        String ProductInfoTxId = gdCF.getJGStoreHash2(txId, jgType, 1);
        jgType = subjectType;//jgType = "主体";
        String SubjectObjectTxId = gdCF.getJGStoreHash2(txId, jgType, 1);


        Map getSubInfo = gdCF.contructEnterpriseSubInfo(SubjectObjectTxId);
        Map getProInfo = gdCF.contructEquityProdInfo(ProductInfoTxId);
        String timeStampSub = gdCF.getTimeStampFromMap(getSubInfo, "subject_create_time");
        String timeStampProd = gdCF.getTimeStampFromMap(getProInfo, "product_create_time");
        enSubInfo.put("content",
                gdCF.constructContentMap(subjectType, gdCompanyID, String.valueOf(verTemp), "create",
                        timeStampSub));
        prodInfo.put("content",
                gdCF.constructContentMap(prodType, gdEquityCode, String.valueOf(verTemp), "create",
                        timeStampProd));


        assertEquals(String.valueOf(gdRefId1), gdCF.getObjectLatestVer(subject_investor_qualification_certifier_ref));
        assertEquals(String.valueOf(gdRefId2 + 2), gdCF.getObjectLatestVer(product_market_subject_ref));
        assertEquals(String.valueOf(gdCpmIdOldVer + 1), gdCF.getObjectLatestVer(gdCompanyID));
        assertEquals(String.valueOf(gdRefId3 + 1), gdCF.getObjectLatestVer(service_provider_subject_ref));

        String[] verForSub = new String[]{"/" + gdCF.getObjectLatestVer(subject_investor_qualification_certifier_ref)};
        String[] verForProd = new String[]{"/" + gdCF.getObjectLatestVer(product_market_subject_ref),
                "/" + gdCF.getObjectLatestVer(gdCompanyID),
                "/" + gdCF.getObjectLatestVer(service_provider_subject_ref)};

        log.info("检查主体存证信息内容与传入一致\n" + enSubInfo.toString() + "\n" + getSubInfo.toString());
        assertEquals(replaceCertain(gdCF.matchRefMapCertVer(enSubInfo, subjectType, verForSub)), replaceCertain(getSubInfo.toString()));

        log.info("检查产品存证信息内容与传入一致\n" + prodInfo.toString() + "\n" + getProInfo.toString());
        assertEquals(replaceCertain(gdCF.matchRefMapCertVer(prodInfo, prodType, verForProd)), replaceCertain(getProInfo.toString()));
    }

    /***
     * 非必填引用不传 但做更新
     * @throws Exception
     */
    @Test
    public void TC04_enterpriseRegisterRefTest() throws Exception {
        long shareTotals = 1000000;
        Map enSubInfo = gdBF.init01EnterpriseSubjectInfo();
        enSubInfo.remove("subject_qualification_information");//移除非必填的subject_qualification_information信息 包含一个主体引用
        Map prodInfo = gdBF.init03EquityProductInfo();
        prodInfo.remove("service_provider_information");//移除非笔填的service_provider_information信息 包含一个主体引用

        int gdCpmIdOldVer = Integer.parseInt(gdCF.getObjectLatestVer(gdCompanyID));//获取当前挂牌主体最新版本信息
        int gdRefId1 = Integer.parseInt(gdCF.getObjectLatestVer(subject_investor_qualification_certifier_ref));//获取当前引用主体最新版本信息
        int gdRefId2 = Integer.parseInt(gdCF.getObjectLatestVer(product_market_subject_ref));//获取当前引用主体最新版本信息
        int gdRefId3 = Integer.parseInt(gdCF.getObjectLatestVer(service_provider_subject_ref));//获取当前引用主体最新版本信息

        //更新待引用的subject_investor_qualification_certifier_ref service_provider_subject_ref  主体版本
        updateSubjectObject(subject_investor_qualification_certifier_ref);
        updateSubjectObject(service_provider_subject_ref);


        String response = gd.GDEnterpriseResister(gdContractAddress, gdEquityCode, shareTotals, enSubInfo,
                prodInfo, null, null);
        String txId = net.sf.json.JSONObject.fromObject(response).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        assertEquals("200", net.sf.json.JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        //查询挂牌企业主体数据  交易上链后 数据可能还未写入合约 在此做2s内数据查询
        response = gd.GDMainSubjectQuery(gdContractAddress, gdCompanyID);
        assertEquals("200", net.sf.json.JSONObject.fromObject(response).getString("state"));


        //获取主体/产品存证hash
        String query = store.GetTxDetail(txId);
        String questInfo = net.sf.json.JSONObject.fromObject(query).getJSONObject("data").getJSONObject("wvm"
        ).getJSONObject("wvmContractTx").getJSONObject("arg").getJSONArray("args").get(0).toString();

        //获取监管数据存证hash
        String jgType = prodType;
        String ProductInfoTxId = gdCF.getJGStoreHash2(txId, jgType, 1);
        jgType = subjectType;//jgType = "主体";
        String SubjectObjectTxId = gdCF.getJGStoreHash2(txId, jgType, 1);


        Map getSubInfo = gdCF.contructEnterpriseSubInfo(SubjectObjectTxId);
        Map getProInfo = gdCF.contructEquityProdInfo(ProductInfoTxId);
        String timeStampSub = gdCF.getTimeStampFromMap(getSubInfo, "subject_create_time");
        String timeStampProd = gdCF.getTimeStampFromMap(getProInfo, "product_create_time");
        enSubInfo.put("content",
                gdCF.constructContentMap(subjectType, gdCompanyID, String.valueOf(verTemp), "create",
                        timeStampSub));
        prodInfo.put("content",
                gdCF.constructContentMap(prodType, gdEquityCode, String.valueOf(verTemp), "create",
                        timeStampProd));

        assertEquals(String.valueOf(gdRefId1 + 1), gdCF.getObjectLatestVer(subject_investor_qualification_certifier_ref));
        assertEquals(String.valueOf(gdRefId2), gdCF.getObjectLatestVer(product_market_subject_ref));
        assertEquals(String.valueOf(gdCpmIdOldVer + 1), gdCF.getObjectLatestVer(gdCompanyID));
        assertEquals(String.valueOf(gdRefId3 + 1), gdCF.getObjectLatestVer(service_provider_subject_ref));

        String[] verForSub = new String[]{"/" + gdCF.getObjectLatestVer(subject_investor_qualification_certifier_ref)};
        String[] verForProd = new String[]{"/" + gdCF.getObjectLatestVer(product_market_subject_ref),
                "/" + gdCF.getObjectLatestVer(gdCompanyID),
                "/" + gdCF.getObjectLatestVer(service_provider_subject_ref)};


        log.info("检查主体存证信息内容与传入一致\n" + enSubInfo.toString() + "\n" + getSubInfo.toString());
        assertEquals(replaceCertain(gdCF.matchRefMapCertVer(enSubInfo, subjectType, verForSub)), replaceCertain(getSubInfo.toString()));

        log.info("检查产品存证信息内容与传入一致\n" + prodInfo.toString() + "\n" + getProInfo.toString());
        assertEquals(replaceCertain(gdCF.matchRefMapCertVer(prodInfo, prodType, verForProd)), replaceCertain(getProInfo.toString()));
    }

}
