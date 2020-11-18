package com.tjfintech.common.functionTest.guDengTestV2;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.GuDeng;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;
import java.util.*;

import static com.tjfintech.common.functionTest.guDengTestV2.GDCommonFunc.*;
import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassGD.*;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class GDV2_CheckData_Update_SubAccProd {

    TestBuilder testBuilder= TestBuilder.getInstance();
    GuDeng gd =testBuilder.getGuDeng();
    Store store =testBuilder.getStore();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    GDBeforeCondition gdBF = new GDBeforeCondition();
    GDCommonFunc gdCF = new GDCommonFunc();
    GDUnitFunc uf = new GDUnitFunc();
    public static String bizNoTest = "test" + Random(12);


    @Rule
    public TestName tm = new TestName();

    @BeforeClass
    public static void Before()throws Exception{
        TestBuilder tbTemp = TestBuilder.getInstance();
        Store storeTemp =tbTemp.getStore();
        beginHeigh = Integer.parseInt(JSONObject.fromObject(storeTemp.GetHeight()).getString("data"));
        start = (new Date()).getTime();

        GDBeforeCondition gdBefore = new GDBeforeCondition();
//        gdBefore.gdCreateAccout();
        gdBefore.initRegulationData();
        gdEquityCode = "fondTest" + Random(12);
    }

//    @After
    public void calJGDataAfterTx()throws Exception{
        testCurMethodName = tm.getMethodName();
        GDUnitFunc uf = new GDUnitFunc();
//        uf.calJGData();
        uf.calJGDataEachHeight();
    }


    @Test
    public void TC20_updateSubjectInfo_Enterprise()throws Exception{
        gdEquityCode = "update" + Random(12);
        //挂牌企业登记
        long shareTotals = 1000000;
        Map testSub = gdBF.init01EnterpriseSubjectInfo();
        String response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,testSub,
                equityProductInfo,bondProductInfo,fundProductInfo);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        //查询挂牌企业主体数据
        for(int i = 0;i<20;i++) {
            response = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);
            if(JSONObject.fromObject(response).getString("state").equals("200"))
                break;
            sleepAndSaveInfo(100);
        }
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
        Map mapSubject = (Map)com.alibaba.fastjson.JSON.parse(JSONObject.fromObject(response).getString("data"));

        Map<String, String> testMap1 = new TreeMap<String, String>(testSub);
        Map<String, String> testMap2 = new TreeMap<String, String>(mapSubject);
        assertEquals(testMap1.toString().replaceAll("( )?","").replaceAll(":","="),
                testMap2.toString().replaceAll("(\")?( )?", "").replaceAll(":","="));

        //更新主体信息数据
        Map mapTemp = new HashMap();
        List<String> fileList = new ArrayList<>();
        fileList.add("file22.txt");
        mapTemp.clear();


        List<Map> listQual = new ArrayList<>();
        Map qualification1 = new HashMap();
        Map qualification2 = new HashMap();
        listQual.add(qualification1);
//        listQual.add(qualification2);

        //对象标识
        mapTemp.put("subject_object_id",gdCompanyID);
        mapTemp.put("subject_object_information_type",1);

        //主体信息 主体基本信息 主体通用信息
        mapTemp.put("subject_id",gdCompanyID + "sub");
        mapTemp.put("subject_industry_code","22");
        mapTemp.put("subject_type",1);
        mapTemp.put("subject_create_time","2021/11/06 14:44:59");

        //主体信息 主体基本信息 主体资质信息
        //主体信息 主体基本信息 主体资质信息 资质信息
        mapTemp.put("subject_qualification_category",1);
        mapTemp.put("subject_market_roles_types",1);
        mapTemp.put("subject_financial_qualification_types",1);

        //主体信息 主体基本信息 主体资质信息 资质认证信息
        mapTemp.put("subject_role_qualification_number","CHpCT82aXlhaA322");
        mapTemp.put("subject_role_qualification_certification_document",fileList);
        mapTemp.put("subject_qualification_party_certification","CHbG59a6kufNo2");
        mapTemp.put("subject_role_qualification_reviewer","CH8Ggm9JnTBIp522");
        mapTemp.put("subject_certification_time","2020/12/05 14:19:59");
        mapTemp.put("subject_audit_time","2020/10/05 14:49:59");

//        mapTemp.put("subject_qualification_information",listQual);

        //主体信息 机构主体信息 机构分类信息
        mapTemp.put("subject_organization_type",1);
        mapTemp.put("subject_organization_nature",1);

        //主体信息 机构主体信息 企业基本信息
        mapTemp.put("subject_company_name","CHbne9QxJO40e22");
        mapTemp.put("subject_company_english_name","CHRTK405U5Mvde22");
        mapTemp.put("subject_company_short_name","CH10V60bs23xrV22");
        mapTemp.put("subject_company_short_english_name","CHMCp8U1af57p133");
        mapTemp.put("subject_company_type",3);
        mapTemp.put("subject_company_component",3);
        mapTemp.put("subject_unified_social_credit_code","cdSN0000000033");
        mapTemp.put("subject_organization_code","CH6B532hqn28G333");
        mapTemp.put("subject_establishment_day","2020/10/16");
        mapTemp.put("subject_business_license","AYg22.pdf");
        mapTemp.put("subject_business_scope","CH0iZ3oTi0vO56");
        mapTemp.put("subject_industry",3);
        mapTemp.put("subject_company_business","CHFzt0hqd1Mx33lq");
        mapTemp.put("subject_company_profile","textn257v7Om3357");
        mapTemp.put("subject_registered_capital",5000000);
        mapTemp.put("subject_registered_capital_currency",65);
        mapTemp.put("subject_paid_in_capital",5000010);
        mapTemp.put("subject_paid_in_capital_currency",65);
        mapTemp.put("subject_registered_address","CH8FwoDbZ16lWw33");
        mapTemp.put("subject_office_address","苏州高铁新城");
        mapTemp.put("subject_contact_address","苏州高铁新城");
        mapTemp.put("subject_contact_number","23568798");
        mapTemp.put("subject_fax","23568798");
        mapTemp.put("subject_postal_code","233655");
        mapTemp.put("subject_internet_address","er@163.com");
        mapTemp.put("subject_mail_box","er@163.com");
        mapTemp.put("subject_association_articles","textTG5F4q9y22Q1");
        mapTemp.put("subject_competent_unit","CHF3420egqe8IN66");
        mapTemp.put("subject_shareholders_number",11);
        mapTemp.put("subject_total_share_capital",5000010);
        mapTemp.put("subject_actual_controller","CH2cnqo0H67523");
        mapTemp.put("subject_actual_controller_id_type","0");
        mapTemp.put("subject_actual_controller_id","CHGly2JJ06590f23");

        //主体信息 机构主体信息 法人信息
        mapTemp.put("subject_legal_rep_name","CHNoDE64t45V88");
        mapTemp.put("subject_legal_person_nature",1);
        mapTemp.put("subject_legal_rep_id_doc_type",1);
        mapTemp.put("subject_legal_rep_id_doc_number","123456789");
        mapTemp.put("subject_legal_rep_post",568978);
        mapTemp.put("subject_legal_rep_cellphone_number","12345678");

        //执行update操作
        String resp2 = gd.GDUpdateSubjectInfo(gdContractAddress,0,mapTemp);
        txId = JSONObject.fromObject(resp2).getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        sleepAndSaveInfo(2000);

        response = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);
        Map mapTest = (Map)com.alibaba.fastjson.JSON.parse(JSONObject.fromObject(response).getString("data"));

        Map<String, String> testMap3 = new TreeMap<String, String>(mapTemp);
        Map<String, String> testMap4 = new TreeMap<String, String>(mapTest);
        assertEquals(testMap3.toString().replaceAll("( )?","").replaceAll(":","="),
                testMap4.toString().replaceAll("(\")?( )?", "").replaceAll(":","="));
    }

    @Test
    public void TC20_updateSubjectInfo_Personal()throws Exception{
        //开户
        String cltNo = "updateCLI" + Random(12);
        String shareHolderNo = "SH" + cltNo;
        String fundNo = "fund" + cltNo;

        //构造股权账户信息
        Map shareHolderInfo = new HashMap();

        shAccountInfo.put("account_object_id",shareHolderNo);  //更新账户对象标识字段
        log.info(shAccountInfo.toString());
        shareHolderInfo.put("shareholderNo",shareHolderNo);
        shareHolderInfo.put("accountInfo", shAccountInfo);
        log.info(shareHolderInfo.toString());

        //资金账户信息
        fundAccountInfo.put("account_object_id",fundNo);  //更新账户对象标识字段
        Map mapFundInfo = new HashMap();
        mapFundInfo.put("fundNo",fundNo);
        mapFundInfo.put("accountInfo", fundAccountInfo);

        //构造个人/投资者主体信息
        Map testSub = gdBF.init01PersonalSubjectInfo();
        testSub.put("subject_object_id",cltNo);  //更新对象标识字段
        testSub.put("subject_id","sid" + cltNo);  //更新主体标识字段

        String response = gd.GDCreateAccout(gdContractAddress,cltNo,mapFundInfo,shareHolderInfo, testSub);
        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        //查询个人主体数据
        for(int i = 0;i < 20; i++) {
            response = gd.GDMainSubjectQuery(gdContractAddress,cltNo);
            if(JSONObject.fromObject(response).getString("state").equals("200"))
                break;
            sleepAndSaveInfo(100);
        }
        assertEquals("200",JSONObject.fromObject(response).getString("state"));

        Map mapSubject = (Map)com.alibaba.fastjson.JSON.parse(JSONObject.fromObject(response).getString("data"));
        Map<String, String> testMap1 = new TreeMap<String, String>(testSub);
        Map<String, String> testMap2 = new TreeMap<String, String>(mapSubject);
        assertEquals(testMap1.toString().replaceAll("( )?","").replaceAll(":","="),
                testMap2.toString().replaceAll("(\")?( )?", "").replaceAll(":","="));


        //更新主体信息数据
        List<String> fileList1 = new ArrayList<>();
        fileList1.add("test3.pdf");
        fileList1.add("test4.pdf");


        Map mapTemp = new HashMap();

        //对象标识
        mapTemp.put("subject_object_id",cltNo);
        mapTemp.put("subject_object_information_type",1);

        //主体信息 主体基本信息 主体通用信息
        mapTemp.put("subject_id",gdCompanyID + "sub");
        mapTemp.put("subject_industry_code","CHDcdIuA52fhLo12");
        mapTemp.put("subject_type",0);
        mapTemp.put("subject_create_time","2020/11/16 20:14:59");

        //主体信息 主体基本信息 主体资质信息
        //主体信息 主体基本信息 主体资质信息 资质信息
        mapTemp.put("subject_qualification_category",1);
        mapTemp.put("subject_market_roles_types",1);
        mapTemp.put("subject_financial_qualification_types",1);

        //主体信息 主体基本信息 主体资质信息 资质认证信息
        mapTemp.put("subject_role_qualification_number","CHpCT82aXlhaA311");
        mapTemp.put("subject_role_qualification_certification_document",fileList1);
        mapTemp.put("subject_qualification_party_certification","CHbG59a6kufNo111");
        mapTemp.put("subject_role_qualification_reviewer","CH8Ggm9JnTBIp511");
        mapTemp.put("subject_certification_time","2023/11/05 13:14:59");
        mapTemp.put("subject_audit_time","2023/11/05 11:14:59");

//        mapQuali.add(qualification1);
//        mapTemp.put("subject_qualification_information",mapQuali);

        //主体信息 个人主体信息 个人主体基本信息
        mapTemp.put("subject_investor_name","zhangsan1");
        mapTemp.put("subject_id_doc_type",1);
        mapTemp.put("subject_id_doc_number","325689199512230022");
        mapTemp.put("subject_id_address","相城2");
        mapTemp.put("subject_contact_address","相城2");
        mapTemp.put("subject_investor_contact_number","158654878951");
        mapTemp.put("subject_cellphone_number","158654878951");
        mapTemp.put("subject_personal_fax_number","56892587");
        mapTemp.put("subject_postalcode_number","122339");
        mapTemp.put("subject_id_doc_mailbox","xx@12143.com");
        mapTemp.put("subject_education",5);
        mapTemp.put("subject_industry",0);
        mapTemp.put("subject_birthday","1989/09/12");
        mapTemp.put("subject_gender",1);
        mapTemp.put("subject_work_unit","苏同院2");
        mapTemp.put("subject_Investment_period","112");
        mapTemp.put("subject_Investment_experience","112");
        mapTemp.put("subject_native_place","江苏苏州22");
        mapTemp.put("subject_mail_box","高铁新城22");
        mapTemp.put("subject_province","江苏22");
        mapTemp.put("subject_city","苏州22");


        //主体信息 个人主体信息 个人主体风险评级
        mapTemp.put("subject_rating_results","通过22");
        mapTemp.put("subject_rating_time","2020/12/12 12:05:12");
        mapTemp.put("subject_rating_record","记录22");

        //执行update操作 更新个人主体信息
        String resp2 = gd.GDUpdateSubjectInfo(gdContractAddress,1,mapTemp);
        txId = JSONObject.fromObject(resp2).getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        sleepAndSaveInfo(2000);

        //查询个人主体信息
        response = gd.GDMainSubjectQuery(gdContractAddress,cltNo);
        Map mapTest = (Map)com.alibaba.fastjson.JSON.parse(JSONObject.fromObject(response).getString("data"));

        Map<String, String> testMap3 = new TreeMap<String, String>(mapTemp);
        Map<String, String> testMap4 = new TreeMap<String, String>(mapTest);
        assertEquals(testMap3.toString().replaceAll("( )?","").replaceAll(":","="),
                testMap4.toString().replaceAll("(\")?( )?", "").replaceAll(":","="));

    }

    @Test
    public void TC21_updateSHAccInfo_Personal()throws Exception{
        //开户
        String cltNo = "updateCLI" + Random(12);
        String shareHolderNo = "SH" + cltNo;
        String fundNo = "fund" + cltNo;

        shAccountInfo = gdBF.init02ShareholderAccountInfo();
        fundAccountInfo = gdBF.init02FundAccountInfo();

        //构造股权账户信息
        Map shareHolderInfo = new HashMap();

        shAccountInfo.put("account_object_id",shareHolderNo);  //更新账户对象标识字段
        log.info(shAccountInfo.toString());
        shareHolderInfo.put("shareholderNo",shareHolderNo);
        shareHolderInfo.put("accountInfo", shAccountInfo);
        log.info(shareHolderInfo.toString());

        //资金账户信息
        fundAccountInfo.put("account_object_id",fundNo);  //更新账户对象标识字段
        Map mapFundInfo = new HashMap();
        mapFundInfo.put("fundNo",fundNo);
        mapFundInfo.put("accountInfo", fundAccountInfo);

        //构造个人/投资者主体信息
        Map testSub = gdBF.init01PersonalSubjectInfo();
        testSub.put("subject_object_id",cltNo);  //更新对象标识字段
        testSub.put("subject_id","sid" + cltNo);  //更新主体标识字段

        String response = gd.GDCreateAccout(gdContractAddress,cltNo,mapFundInfo,shareHolderInfo, testSub);
        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        //查询个人主体数据
        for(int i = 0;i < 20; i++) {
            response = gd.GDMainSubjectQuery(gdContractAddress,cltNo);
            if(JSONObject.fromObject(response).getString("state").equals("200"))
                break;
            sleepAndSaveInfo(100);
        }
        assertEquals("200",JSONObject.fromObject(response).getString("state"));

        //查询投资者账户信息
        String query = gd.GDAccountQuery(gdContractAddress,cltNo);
        Map mapSHAccGet1 = (Map)com.alibaba.fastjson.JSON.parse(JSONObject.fromObject(
                JSONObject.fromObject(query).getJSONObject("data").getJSONArray(
                        "AccountInfoList").get(0).toString()).getJSONObject(
                "ShareholderAccount").getJSONObject("AccountInfo").toString());
        Map mapFundccGet1 = (Map)com.alibaba.fastjson.JSON.parse(JSONObject.fromObject(
                JSONObject.fromObject(query).getJSONObject("data").getJSONArray(
                        "AccountInfoList").get(0).toString()).getJSONObject(
                "FundAccount").getJSONObject("AccountInfo").toString());

        shAccountInfo.put("account_forzen_date","");
        fundAccountInfo.put("account_forzen_date","");

        Map<String, String> testMap11 = new TreeMap<String, String>(shAccountInfo);
        Map<String, String> testMap12 = new TreeMap<String, String>(mapSHAccGet1);
        assertEquals(testMap11.toString().replaceAll("( )?(\\[)?(])?","").replaceAll(":","="),
                testMap12.toString().replaceAll("(\")?( )?", "").replaceAll(":","="));

        Map<String, String> testMap13 = new TreeMap<String, String>(fundAccountInfo);
        Map<String, String> testMap14 = new TreeMap<String, String>(mapFundccGet1);
        assertEquals(testMap13.toString().replaceAll("( )?(\\[)?(])?","").replaceAll(":","="),
                testMap14.toString().replaceAll("(\")?( )?", "").replaceAll(":","="));


        //更新股权账户信息数据

        Map mapTemp = new HashMap();
        log.info("初始化02账户数据结构");
        //默认股权账户
        List<String> fileList1 = new ArrayList<>();
        fileList1.add("test111.pdf");
        fileList1.add("test112.pdf");

        List<String> fileList2 = new ArrayList<>();
        fileList2.add("test121.pdf");
        fileList2.add("test122.pdf");
        List<String> fileList3 = new ArrayList<>();
        fileList3.add("test131.pdf");
        fileList3.add("test132.pdf");
        List<String> fileList4 = new ArrayList<>();
        fileList4.add("test141.pdf");
        fileList4.add("test142.pdf");
        List<String> fileList5 = new ArrayList<>();
        fileList5.add("test151.pdf");
        fileList5.add("test152.pdf");
        mapTemp.clear();

        //对象信息
        mapTemp.put("account_object_id",cltNo);
        mapTemp.put("account_object_information_type",1);

        //账户信息 账户基本信息
        mapTemp.put("account_holder_subject_ref","hrefid00002");
        mapTemp.put("account_depository_subject_ref","drefid00002");
        mapTemp.put("account_number","h0123552");
        mapTemp.put("account_type",0);  //默认股权账户
        mapTemp.put("account_never",2);
        mapTemp.put("account_status",1);

        //账户信息 账户资质信息
        mapTemp.put("account_qualification_certification_file",fileList1);
        mapTemp.put("account_certifier","监管局22");
        mapTemp.put("account_auditor","认证者22");
        mapTemp.put("account_certification_time","2012/8/25");
        mapTemp.put("account_audit_time","2012/8/25");

        //账户信息 账户生命周期信息
        //账户信息 账户生命周期信息 开户信息
        mapTemp.put("account_opening_date","2012/8/25");
        mapTemp.put("account_opening_certificate",fileList4);

        //账户信息 账户生命周期信息 销户信息
        mapTemp.put("account_closing_date","2022/6/25");
        mapTemp.put("account_closing_certificate",fileList2);

        //账户信息 账户生命周期信息 冻结信息
        mapTemp.put("account_forzen_date","2020/9/25");
        mapTemp.put("account_forzen_certificate",fileList3);

        //账户信息 账户生命周期信息 解冻信息
        mapTemp.put("account_thaw_date","2020/9/25");
        mapTemp.put("account_thaw_certificate",fileList4);

        //账户信息 账户关联信息
        mapTemp.put("account_association",2);
        mapTemp.put("account_associated_account_ref","t512pdf");
        mapTemp.put("account_associated_acct_certificates",fileList5);

        String upResp = gd.GDUpdateAccountInfo(gdContractAddress,mapTemp);
        txId = JSONObject.fromObject(upResp).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        //查询投资者账户信息
        query = gd.GDAccountQuery(gdContractAddress,cltNo);
        Map mapSHAccGet2 = (Map)com.alibaba.fastjson.JSON.parse(JSONObject.fromObject(
                JSONObject.fromObject(query).getJSONObject("data").getJSONArray(
                        "AccountInfoList").get(0).toString()).getJSONObject(
                "ShareholderAccount").getJSONObject("AccountInfo").toString());

        Map mapFundAccGet2 = (Map)com.alibaba.fastjson.JSON.parse(JSONObject.fromObject(
                JSONObject.fromObject(query).getJSONObject("data").getJSONArray(
                        "AccountInfoList").get(0).toString()).getJSONObject(
                "FundAccount").getJSONObject("AccountInfo").toString());


        Map<String, String> testMap21 = new TreeMap<String, String>(mapTemp);
        Map<String, String> testMap22 = new TreeMap<String, String>(mapSHAccGet2);
        assertEquals(testMap11.toString().replaceAll("( )?(\\[)?(])?","").replaceAll(":","="),
                testMap12.toString().replaceAll("(\")?( )?", "").replaceAll(":","="));

        Map<String, String> testMap23 = new TreeMap<String, String>(fundAccountInfo);
        Map<String, String> testMap24 = new TreeMap<String, String>(mapFundAccGet2);
        assertEquals(testMap23.toString().replaceAll("( )?(\\[)?(])?","").replaceAll(":","="),
                testMap24.toString().replaceAll("(\")?( )?", "").replaceAll(":","="));
    }

    @Test
    public void TC21_updateFundAccInfo_Personal()throws Exception{
        //开户
        String cltNo = "updateCLI" + Random(12);
        String shareHolderNo = "SH" + cltNo;
        String fundNo = "fund" + cltNo;

        //构造股权账户信息
        Map shareHolderInfo = new HashMap();

        shAccountInfo.put("account_object_id",shareHolderNo);  //更新账户对象标识字段
        log.info(shAccountInfo.toString());
        shareHolderInfo.put("shareholderNo",shareHolderNo);
        shareHolderInfo.put("accountInfo", shAccountInfo);
        log.info(shareHolderInfo.toString());

        //资金账户信息
        fundAccountInfo.put("account_object_id",fundNo);  //更新账户对象标识字段
        Map mapFundInfo = new HashMap();
        mapFundInfo.put("fundNo",fundNo);
        mapFundInfo.put("accountInfo", fundAccountInfo);

        //构造个人/投资者主体信息
        Map testSub = gdBF.init01PersonalSubjectInfo();
        testSub.put("subject_object_id",cltNo);  //更新对象标识字段
        testSub.put("subject_id","sid" + cltNo);  //更新主体标识字段

        String response = gd.GDCreateAccout(gdContractAddress,cltNo,mapFundInfo,shareHolderInfo, testSub);
        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        //查询个人主体数据
        for(int i = 0;i < 20; i++) {
            response = gd.GDMainSubjectQuery(gdContractAddress,cltNo);
            if(JSONObject.fromObject(response).getString("state").equals("200"))
                break;
            sleepAndSaveInfo(100);
        }
        assertEquals("200",JSONObject.fromObject(response).getString("state"));

        //查询投资者账户信息
        String query = gd.GDAccountQuery(gdContractAddress,cltNo);
        Map mapSHAccGet1 = (Map)com.alibaba.fastjson.JSON.parse(JSONObject.fromObject(
                JSONObject.fromObject(query).getJSONObject("data").getJSONArray(
                        "AccountInfoList").get(0).toString()).getJSONObject(
                "ShareholderAccount").getJSONObject("AccountInfo").toString());
        Map mapFundccGet1 = (Map)com.alibaba.fastjson.JSON.parse(JSONObject.fromObject(
                JSONObject.fromObject(query).getJSONObject("data").getJSONArray(
                        "AccountInfoList").get(0).toString()).getJSONObject(
                "FundAccount").getJSONObject("AccountInfo").toString());

        shAccountInfo.put("account_forzen_date","");
        fundAccountInfo.put("account_forzen_date","");
        Map<String, String> testMap11 = new TreeMap<String, String>(shAccountInfo);
        Map<String, String> testMap12 = new TreeMap<String, String>(mapSHAccGet1);
        assertEquals(testMap11.toString().replaceAll("( )?(\\[)?(])?","").replaceAll(":","="),
                testMap12.toString().replaceAll("(\")?( )?", "").replaceAll(":","="));

        Map<String, String> testMap13 = new TreeMap<String, String>(fundAccountInfo);
        Map<String, String> testMap14 = new TreeMap<String, String>(mapFundccGet1);
        assertEquals(testMap13.toString().replaceAll("( )?(\\[)?(])?","").replaceAll(":","="),
                testMap14.toString().replaceAll("(\")?( )?", "").replaceAll(":","="));

        //更新资金账户信息数据
        Map mapTemp = new HashMap();
        log.info("初始化02账户数据结构");
        //默认股权账户
        List<String> fileList1 = new ArrayList<>();
        fileList1.add("test211.pdf");
        fileList1.add("test212.pdf");

        List<String> fileList2 = new ArrayList<>();
        fileList2.add("test221.pdf");
        fileList2.add("test222.pdf");
        List<String> fileList3 = new ArrayList<>();
        fileList3.add("test231.pdf");
        fileList3.add("test232.pdf");
        List<String> fileList4 = new ArrayList<>();
        fileList4.add("test2141.pdf");
        fileList4.add("test2142.pdf");
        List<String> fileList5 = new ArrayList<>();
        fileList5.add("test2151.pdf");
        fileList5.add("test2152.pdf");
        mapTemp.clear();

        //对象信息
        mapTemp.put("account_object_id",cltNo);
        mapTemp.put("account_object_information_type",0);

        //账户信息 账户基本信息
        mapTemp.put("account_holder_subject_ref","hrefid000022");
        mapTemp.put("account_depository_subject_ref","drefid000022");
        mapTemp.put("account_number","h01235522");
        mapTemp.put("account_type",1);  //资金账户
        mapTemp.put("account_never",3);
        mapTemp.put("account_status",2);

        //账户信息 账户资质信息
        mapTemp.put("account_qualification_certification_file",fileList1);
        mapTemp.put("account_certifier","监管局232");
        mapTemp.put("account_auditor","认证者232");
        mapTemp.put("account_certification_time","2012/9/25");
        mapTemp.put("account_audit_time","2012/9/25");

        //账户信息 账户生命周期信息
        //账户信息 账户生命周期信息 开户信息
        mapTemp.put("account_opening_date","2012/8/29");
        mapTemp.put("account_opening_certificate",fileList4);

        //账户信息 账户生命周期信息 销户信息
        mapTemp.put("account_closing_date","2022/6/29");
        mapTemp.put("account_closing_certificate",fileList2);

        //账户信息 账户生命周期信息 冻结信息
        mapTemp.put("account_forzen_date","2020/9/29");
        mapTemp.put("account_forzen_certificate",fileList3);

        //账户信息 账户生命周期信息 解冻信息
        mapTemp.put("account_thaw_date","2020/9/29");
        mapTemp.put("account_thaw_certificate",fileList4);

        //账户信息 账户关联信息
        mapTemp.put("account_association",3);
        mapTemp.put("account_associated_account_ref","t5123pdf");
        mapTemp.put("account_associated_acct_certificates",fileList5);

        String upResp = gd.GDUpdateAccountInfo(gdContractAddress,mapTemp);

        txId = JSONObject.fromObject(upResp).getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        //查询投资者账户信息
        query = gd.GDAccountQuery(gdContractAddress,cltNo);
        Map mapSHAccGet2 = (Map)com.alibaba.fastjson.JSON.parse(JSONObject.fromObject(
                JSONObject.fromObject(query).getJSONObject("data").getJSONArray(
                        "AccountInfoList").get(0).toString()).getJSONObject(
                "ShareholderAccount").getJSONObject("AccountInfo").toString());

        Map mapFundAccGet2 = (Map)com.alibaba.fastjson.JSON.parse(JSONObject.fromObject(
                JSONObject.fromObject(query).getJSONObject("data").getJSONArray(
                        "AccountInfoList").get(0).toString()).getJSONObject(
                "FundAccount").getJSONObject("AccountInfo").toString());


        Map<String, String> testMap21 = new TreeMap<String, String>(shAccountInfo);
        Map<String, String> testMap22 = new TreeMap<String, String>(mapSHAccGet2);
        assertEquals(testMap11.toString().replaceAll("( )?(\\[)?(])?","").replaceAll(":","="),
                testMap12.toString().replaceAll("(\")?( )?", "").replaceAll(":","="));

        Map<String, String> testMap23 = new TreeMap<String, String>(mapTemp);
        Map<String, String> testMap24 = new TreeMap<String, String>(mapFundAccGet2);
        assertEquals(testMap23.toString().replaceAll("( )?(\\[)?(])?","").replaceAll(":","="),
                testMap24.toString().replaceAll("(\")?( )?", "").replaceAll(":","="));

    }

    @Test
    public void TC22_updateEquityProdAccInfo()throws Exception{
        //挂牌登记一个股权类产品
        String response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,10000,enterpriseSubjectInfo,
                equityProductInfo,null,null);
        String txId = net.sf.json.JSONObject.fromObject(response).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", net.sf.json.JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));
        //检查产品信息
        //更新股权类产品信息
        Map tempPro = new HashMap();
        String updateProd = gd.GDUpdateProductInfo(gdContractAddress,tempPro);
        //检查产品信息是否是更新后的信息
    }

    @Test
    public void TC22_updateBondProdAccInfo()throws Exception{
        //挂牌登记一个债券类产品
        String response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,10000,enterpriseSubjectInfo,
                null,bondProductInfo,null);
        String txId = net.sf.json.JSONObject.fromObject(response).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", net.sf.json.JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));
        //检查产品信息
        //更新债券类产品信息
        Map tempPro = new HashMap();
        String updateProd = gd.GDUpdateProductInfo(gdContractAddress,tempPro);
        //检查产品信息是否是更新后的信息
    }

    @Test
    public void TC22_updateFundProdAccInfo()throws Exception{
        //挂牌登记一个基金类产品
        String response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,10000,enterpriseSubjectInfo,
                null,null,fundProductInfo);
        String txId = net.sf.json.JSONObject.fromObject(response).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", net.sf.json.JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));
        //检查产品信息
        //更新基金类产品信息
        Map tempPro = new HashMap();
        String updateProd = gd.GDUpdateProductInfo(gdContractAddress,tempPro);
        //检查产品信息是否是更新后的信息
    }

}
