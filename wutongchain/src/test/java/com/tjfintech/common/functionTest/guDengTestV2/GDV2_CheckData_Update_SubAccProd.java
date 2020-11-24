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
    public void TC20_allUpdateSubjectInfo_Enterprise()throws Exception{
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
        assertEquals(replaceCertain(testMap1.toString()),
                replaceCertain(testMap2.toString()));

        //更新主体信息数据 全部数据
        Map mapTemp = new HashMap();
        List<String> fileList = new ArrayList<>();
        fileList.add("must.txt");
        mapTemp.clear();

        //-----------------主体资质信息---------------//
        List<Map> listSQI = new ArrayList<>();
        List<Map> listQAI = new ArrayList<>();
        List<Map> listISI = new ArrayList<>();
        Map mapSQI = new HashMap();

        Map mapQAI = new HashMap();
        Map mapISI = new HashMap();

        //主体基本信息 主体资质信息 资质信息
        mapSQI.put("subject_qualification_category",0);
        mapSQI.put("subject_market_roles_type",0);

        List<Integer> type = new ArrayList<>();type.add(2);type.add(3);
        mapSQI.put("subject_intermediary_qualification",type);
        mapSQI.put("subject_financial_qualification_type",0);


        //主体基本信息 主体资质信息 资质认证信息
        //{"file_number":"1","file_name": "12312312","url": "12312312","hash": "12312312","summary": "12312312","term_of_validity_type": "0","term_of_validity":"yyyy/MM/dd"}
        //文件对象
        Map fileMap = new HashMap();
        fileMap.put("file_number",2);
        fileMap.put("file_name","file2.pdf");
        fileMap.put("hash","hash0222222222222");
        fileMap.put("url","http://test.com/file/201/file2.pdf");
        fileMap.put("summary","简述2");
        fileMap.put("term_of_validity_type","1");
        fileMap.put("term_of_validity","2020/08/18");

        mapQAI.put("subject_qualification_code","资质代码02");
        mapQAI.put("subject_role_qualification_certification_doc",fileMap);
        mapQAI.put("subject_qualification_authenticator","认证方2");
        mapQAI.put("subject_certification_time","2020/10/12 12:00:02");
        mapQAI.put("subject_qualification_reviewer","审核方2");
        mapQAI.put("subject_review_time","2020/10/11 12:00:02");
        mapQAI.put("subject_qualification_status",false);
        listQAI.add(mapQAI);

        //主体基本信息 主体资质信息 投资者适当性信息
        mapISI.put("subject_investor_qualification",0);
        mapISI.put("subject_investor_qualification_sub","适当性认证子类2");
        mapISI.put("subject_investor_qualification_description","适当性认证描述2");
        mapISI.put("subject_investor_qualification_certificate",fileMap);
        mapISI.put("subject_investor_qualification_cerifier_ref","sub_ref_0002");
        mapISI.put("subject_investor_qualification_cerifier_name","适当性认证方主体名称2");
        mapISI.put("subject_investor_qualification_certificate_time","2020/11/12 12:00:02");
        mapISI.put("subject_investor_qualification_status",false);
        listISI.add(mapISI);

        mapSQI.put("investor_suitability_information",listISI);
        mapSQI.put("qualification_authentication_information",listQAI);


        listSQI.add(mapSQI);
        //-----------------主体资质信息---------------//


        //-----------------主体基本信息---------------//
        //对象标识
        mapTemp.put("subject_object_id",gdCompanyID);
//        mapTemp.put("subject_object_information_type",0);
        mapTemp.put("subject_type",1);

        //主体信息 主体基本信息 主体通用信息
        mapTemp.put("subject_id",gdCompanyID + "sub");
        mapTemp.put("subject_main_administrative_region",1);
        mapTemp.put("subject_create_time","2020/11/06 14:14:00");

        //主体信息 主体基本信息 主体资质信息
        //主体信息 主体基本信息 主体资质信息 资质信息
        mapTemp.put("subject_qualification_information",listSQI);
        //-----------------主体基本信息---------------//


        //-----------------机构主体信息---------------//
        //主体信息 机构主体信息 企业基本信息 基本信息描述
        mapTemp.put("subject_company_name","公司全称CHARACTER2");
        mapTemp.put("subject_company_english_name","英文名称CHARACTER2");
        mapTemp.put("subject_company_short_name","公司简称CHARACTER2");
        mapTemp.put("subject_company_short_english_name","英文简称CHARACTER2");
        mapTemp.put("subject_organization_nature",1);
        mapTemp.put("subject_legal_type",1);
        mapTemp.put("subject_economic_type",1);
        mapTemp.put("subject_company_type",1);
        mapTemp.put("subject_scale_type",1);
        mapTemp.put("subject_high_technology_enterpris",1);
        mapTemp.put("subject_document_infomation","证件类型及证件号码CHARACTER2");
        mapTemp.put("subject_registry_date","2010/10/13");
        mapTemp.put("subject_business_license",fileMap);
        mapTemp.put("subject_business_scope","经营范围CHARACTER2");
        mapTemp.put("subject_industry",2);
        mapTemp.put("subject_company_business","主营业务CHARACTER2");
        mapTemp.put("subject_company_profile","公司简介TEXT2");
        mapTemp.put("subject_registered_capital",6000002);
        mapTemp.put("subject_registered_capital_currency","注册资本币种CHARACTER2");
        mapTemp.put("subject_paid_in_capital",6000002);
        mapTemp.put("subject_paid_in_capital_currency","实收资本币种CHARACTER2");
        mapTemp.put("subject_registered_address","注册地址CHARACTER2");
        mapTemp.put("subject_province","注册地所在省份CHARACTER2");
        mapTemp.put("subject_city","注册地所在市CHARACTER2");
        mapTemp.put("subject_district","注册地所在区CHARACTER2");
        mapTemp.put("subject_office_address","办公地址CHARACTER2");
        mapTemp.put("subject_contact_address","联系地址CHARACTER2");
        mapTemp.put("subject_contact_number","联系电话CHARACTER2");
        mapTemp.put("subject_enterprise_fax","企业传真CHARACTER2");
        mapTemp.put("subject_postal_code","邮政编码CHARACTER2");
        mapTemp.put("subject_internet_address","互联网地址CHARACTER2");
        mapTemp.put("subject_mail_box","电子邮箱CHARACTER2");
        mapTemp.put("subject_association_articles",fileMap);
        mapTemp.put("subject_regulator","主管单位CHARACTER2");
        mapTemp.put("subject_shareholders_number",11);
        mapTemp.put("subject_taxpayer_id_number","纳税人识别号CHARACTER2");
        mapTemp.put("subject_invoice_bank","发票开户行CHARACTER2");
        mapTemp.put("subject_invoice_account_number","发票账号CHARACTER2");
        mapTemp.put("subject_invoice_address","发票地址CHARACTER2");
        mapTemp.put("subject_invoice_telephone_number","发票电话CHARACTER2");
        mapTemp.put("subject_approval_time","2010/12/12 15:00:02");
        mapTemp.put("subject_insured_number",48);
        mapTemp.put("subject_company_status",1);
        mapTemp.put("subject_company_status_deregistration","注销原因CHARACTER2");
        mapTemp.put("subject_company_status_deregistration_date","yyyy/MM/dd HH:mm:ss");
        mapTemp.put("subject_company_status_windingup","吊销原因CHARACTER2");
        mapTemp.put("subject_company_status_windingup_date","yyyy/MM/dd HH:mm:ss");
        mapTemp.put("subject_name_used_before","企业曾用名CHARACTER2");
        mapTemp.put("subject_personnel_size","人员规模CHARACTER2");
        //-----------------机构主体信息---------------//


        //主体信息 机构主体信息 企业基本信息 主要人员信息
        //-----------------主要人员信息---------------//
        Map mapLMI = new HashMap();
        List<Map> listLMI = new ArrayList<>();
        mapLMI.put("subject_keypersonnel_appointment_end","2029/12/21");
        mapLMI.put("subject_keypersonnel_type",1);
        mapLMI.put("subject_keypersonnel_position",1);
        mapLMI.put("subject_keypersonnel_appointment_start","2020/12/21");
        mapLMI.put("subject_keypersonnel_name","姓名CHARACTER2");
        mapLMI.put("subject_keypersonnel_nationality","国籍CHARACTER2");
        mapLMI.put("subject_document_type",1);
        mapLMI.put("subject_keypersonnel_id","证件号码CHARACTER2");
        mapLMI.put("subject_keypersonnel_address","证件地址CHARACTER2");
        mapLMI.put("subject_keypersonnel_shareholding_ratio",21);
        mapLMI.put("subject_keypersonnel_shareholding",501);
        mapLMI.put("subject_keypersonnel_contact","联系方式CHARACTER2");
        listLMI.add(mapLMI);

        mapTemp.put("leading_member_information",listLMI);
        //-----------------主要人员信息---------------//

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
        assertEquals(replaceCertain(testMap3.toString()),
                replaceCertain(testMap4.toString()));
    }

    @Test
    public void TC20_partUpdateSubjectInfo_Enterprise()throws Exception{
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
        assertEquals(replaceCertain(testMap1.toString()),
                replaceCertain(testMap2.toString()));

        //更新部分主体信息数据
        Map mapTemp = new HashMap();



        //-----------------主体基本信息---------------//
        //对象标识
        mapTemp.put("subject_object_id",gdCompanyID);
//        mapTemp.put("subject_object_information_type",0);
        mapTemp.put("subject_type",1);

        //主体信息 主体基本信息 主体通用信息
        mapTemp.put("subject_main_administrative_region",1);
        mapTemp.put("subject_create_time","2020/11/06 14:14:00");

        //-----------------机构主体信息---------------//
        //主体信息 机构主体信息 企业基本信息 基本信息描述
        mapTemp.put("subject_company_name","公司全称CHARACTER2");
        mapTemp.put("subject_document_infomation","证件类型及证件号码CHARACTER2");

        //执行update操作
        String resp2 = gd.GDUpdateSubjectInfo(gdContractAddress,0,mapTemp);
        txId = JSONObject.fromObject(resp2).getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        sleepAndSaveInfo(2000);

        response = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);
        Map mapTest = (Map)com.alibaba.fastjson.JSON.parse(JSONObject.fromObject(response).getString("data"));
        Map mapR = new HashMap();
        String key = "subject_object_id"; mapR.put(key,mapTest.get(key));
        key = "subject_type"; mapR.put(key,mapTest.get(key));
        key = "subject_main_administrative_region"; mapR.put(key,mapTest.get(key));
        key = "subject_create_time"; mapR.put(key,mapTest.get(key));
        key = "subject_company_name"; mapR.put(key,mapTest.get(key));
        key = "subject_document_infomation"; mapR.put(key,mapTest.get(key));

//        assertEquals(true,mapTest.get("subject_company_short_english_name").equals(null));

        Map<String, String> testMap3 = new TreeMap<String, String>(mapTemp);
        Map<String, String> testMap4 = new TreeMap<String, String>(mapR);
        assertEquals(replaceCertain(testMap3.toString()),
                replaceCertain(testMap4.toString()));
    }


    @Test
    public void TC20_allUpdateSubjectInfo_Personal()throws Exception{
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
        assertEquals(replaceCertain(testMap1.toString()),
                replaceCertain(testMap2.toString()));


        //更新主体信息数据
        Map mapTemp = new HashMap();
        List<Map> listSQI = new ArrayList<>();
        List<Map> listQAI = new ArrayList<>();
        List<Map> listISI = new ArrayList<>();
        Map mapSQI = new HashMap();

        Map mapQAI = new HashMap();
        Map mapISI = new HashMap();

        //主体基本信息 主体资质信息 资质信息
        mapSQI.put("subject_qualification_category",0);
        mapSQI.put("subject_market_roles_type",0);

        List<Integer> type = new ArrayList<>();type.add(4);type.add(5);
        mapSQI.put("subject_intermediary_qualification",type);
        mapSQI.put("subject_financial_qualification_type",0);


        //主体基本信息 主体资质信息 资质认证信息
        //{"file_number":"1","file_name": "12312312","url": "12312312","hash": "12312312","summary": "12312312","term_of_validity_type": "0","term_of_validity":"yyyy/MM/dd"}
        //文件对象
        Map fileMap = new HashMap();
        fileMap.put("file_number",1);
        fileMap.put("file_name","file3.pdf");
        fileMap.put("hash","da1234filehash5223");
        fileMap.put("url","http://test.com/file/201/file3.pdf");
        fileMap.put("summary","简述3");
        fileMap.put("term_of_validity_type","3");
        fileMap.put("term_of_validity","2020/03/18");

        mapQAI.put("subject_qualification_code","资质代码3");
        mapQAI.put("subject_role_qualification_certification_doc",fileMap);
        mapQAI.put("subject_qualification_authenticator","认证方3");
        mapQAI.put("subject_certification_time","2020/10/12 12:00:03");
        mapQAI.put("subject_qualification_reviewer","审核方3");
        mapQAI.put("subject_review_time","2020/10/11 12:00:03");
        mapQAI.put("subject_qualification_status",true);
        listQAI.add(mapQAI);

        //主体基本信息 主体资质信息 投资者适当性信息
        mapISI.put("subject_investor_qualification",3);
        mapISI.put("subject_investor_qualification_sub","适当性认证子类3");
        mapISI.put("subject_investor_qualification_description","适当性认证描述3");
        mapISI.put("subject_investor_qualification_certificate",fileMap);
        mapISI.put("subject_investor_qualification_cerifier_ref","sub_ref_0003");
        mapISI.put("subject_investor_qualification_cerifier_name","适当性认证方主体名称3");
        mapISI.put("subject_investor_qualification_certificate_time","2020/11/12 12:00:03");
        mapISI.put("subject_investor_qualification_status",true);
        listISI.add(mapISI);

        mapSQI.put("investor_suitability_information",listISI);
        mapSQI.put("qualification_authentication_information",listQAI);


        listSQI.add(mapSQI);
        //-----------------主体资质信息---------------//


        //-----------------主体基本信息---------------//
        //对象标识
        mapTemp.put("subject_object_id",cltNo);
//        mapTemp.put("subject_object_information_type",0);
        mapTemp.put("subject_type",2);

        //主体信息 主体基本信息 主体通用信息
        mapTemp.put("subject_id",cltNo + "sub");
        mapTemp.put("subject_main_administrative_region",3);
        mapTemp.put("subject_create_time","2020/11/06 14:14:53");

        //主体信息 主体基本信息 主体资质信息
        //主体信息 主体基本信息 主体资质信息 资质信息
        mapTemp.put("subject_qualification_information",listSQI);
        //-----------------主体基本信息---------------//

        //主体信息 个人主体信息 个人主体基本信息
        mapTemp.put("subject_investor_name","个人姓名CHARACTER3");
        mapTemp.put("subject_id_type",0);
        mapTemp.put("subject_id_number","个人身份证件号CHARACTER3");
        mapTemp.put("subject_id_address","个人证件地址CHARACTER3");
        mapTemp.put("subject_contact_address","个人联系地址CHARACTER3");
        mapTemp.put("subject_contact_number","个人联系电话CHARACTER3");
        mapTemp.put("subject_cellphone_number","个人手机号CHARACTER3");
        mapTemp.put("subject_personal_fax","个人传真CHARACTER3");
        mapTemp.put("subject_postalcode_number","邮政编码CHARACTER3");
        mapTemp.put("subject_id_doc_mailbox","电子邮箱CHARACTER3");
        mapTemp.put("subject_education",3);
        mapTemp.put("subject_occupation",3);
        mapTemp.put("subject_industry",3);
        mapTemp.put("subject_birthday","yyyy/MM/dd");
        mapTemp.put("subject_gender",2);
        mapTemp.put("subject_work_unit","工作单位CHARACTER3");
        mapTemp.put("subject_investment_period","投资年限CHARACTER3");
        mapTemp.put("subject_investment_experience","投资经历CHARACTER3");
        mapTemp.put("subject_native_place","籍贯CHARACTER3");
        mapTemp.put("subject_province","省份CHARACTER3");
        mapTemp.put("subject_city","城市CHARACTER3");
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
        assertEquals(replaceCertain(testMap3.toString()),
                replaceCertain(testMap4.toString()));

    }

    @Test
    public void TC20_partUpdateSubjectInfo_Personal()throws Exception{
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
        assertEquals(replaceCertain(testMap1.toString()),
                replaceCertain(testMap2.toString()));


        //更新部分个人主体信息
        Map mapTemp = new HashMap();
        //对象标识
        mapTemp.put("subject_object_id",cltNo);
        mapTemp.put("subject_type",0);

        //主体信息 主体基本信息 主体通用信息
        mapTemp.put("subject_id","123456789");
        mapTemp.put("subject_main_administrative_region",2);
        mapTemp.put("subject_create_time","2020/11/06 14:14:00");

        //主体信息 个人主体信息 个人主体基本信息
        mapTemp.put("subject_investor_name","zhangsan1");
        mapTemp.put("subject_id_type",1);
        mapTemp.put("subject_id_number","325689199512230022");
        //执行update操作 更新个人主体信息
        String resp2 = gd.GDUpdateSubjectInfo(gdContractAddress,1,mapTemp);
        txId = JSONObject.fromObject(resp2).getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        sleepAndSaveInfo(2000);

        //查询个人主体信息
        response = gd.GDMainSubjectQuery(gdContractAddress,cltNo);
        Map mapTest = (Map)com.alibaba.fastjson.JSON.parse(JSONObject.fromObject(response).getString("data"));
        Map mapR = new HashMap();
        String key = "subject_object_id"; mapR.put(key,mapTest.get(key));
        key = "subject_type"; mapR.put(key,mapTest.get(key));
        key = "subject_id"; mapR.put(key,mapTest.get(key));
        key = "subject_main_administrative_region"; mapR.put(key,mapTest.get(key));
        key = "subject_create_time"; mapR.put(key,mapTest.get(key));
        key = "subject_investor_name"; mapR.put(key,mapTest.get(key));
        key = "subject_id_type"; mapR.put(key,mapTest.get(key));
        key = "subject_id_number"; mapR.put(key,mapTest.get(key));

//        assertEquals(true,mapTest.get("subject_id_address").equals(null));

        Map<String, String> testMap3 = new TreeMap<String, String>(mapTemp);
        Map<String, String> testMap4 = new TreeMap<String, String>(mapR);
        assertEquals(replaceCertain(testMap3.toString()),
                replaceCertain(testMap4.toString()));

    }

    @Test
    public void TC21_allUpdateSHAccInfo_Personal()throws Exception{
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
        assertEquals(replaceCertain(testMap11.toString()),
                replaceCertain(testMap12.toString()));

        Map<String, String> testMap13 = new TreeMap<String, String>(fundAccountInfo);
        Map<String, String> testMap14 = new TreeMap<String, String>(mapFundccGet1);
        log.info(fundAccountInfo.toString());
        assertEquals(replaceCertain(testMap13.toString()),replaceCertain(testMap14.toString()));


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

        String upResp = gd.GDUpdateAccountInfo(gdContractAddress,cltNo,mapTemp);
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
        assertEquals(replaceCertain(testMap11.toString()),
                replaceCertain(testMap12.toString()));

        Map<String, String> testMap23 = new TreeMap<String, String>(fundAccountInfo);
        Map<String, String> testMap24 = new TreeMap<String, String>(mapFundAccGet2);
        assertEquals(replaceCertain(testMap23.toString()),
                replaceCertain(testMap24.toString()));
    }


    @Test
    public void TC21_partUpdateSHAccInfo_Personal()throws Exception{
        //开户
        String cltNo = "updateCLI" + Random(12);
        String shareHolderNo = "SH" + cltNo;
        String fundNo = "fund" + cltNo;

        Map shAccInfo = gdBF.init02ShareholderAccountInfo();
        Map fundAccInfo = gdBF.init02FundAccountInfo();

        //构造股权账户信息
        Map shareHolderInfo = new HashMap();

        shAccInfo.put("account_object_id",shareHolderNo);  //更新账户对象标识字段
        log.info(shAccInfo.toString());
        shareHolderInfo.put("shareholderNo",shareHolderNo);
        shareHolderInfo.put("accountInfo", shAccInfo);
        log.info(shareHolderInfo.toString());

        //资金账户信息
        fundAccInfo.put("account_object_id",fundNo);  //更新账户对象标识字段
        Map mapFundInfo = new HashMap();
        mapFundInfo.put("fundNo",fundNo);
        mapFundInfo.put("accountInfo", fundAccInfo);

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

        shAccInfo.put("account_forzen_date","");
        fundAccInfo.put("account_forzen_date","");

        Map<String, String> testMap11 = new TreeMap<String, String>(shAccInfo);
        Map<String, String> testMap12 = new TreeMap<String, String>(mapSHAccGet1);
        assertEquals(replaceCertain(testMap11.toString()),
                replaceCertain(testMap12.toString()));

        Map<String, String> testMap13 = new TreeMap<String, String>(fundAccInfo);
        Map<String, String> testMap14 = new TreeMap<String, String>(mapFundccGet1);
        assertEquals(replaceCertain(testMap13.toString()),
                replaceCertain(testMap14.toString()));

        Map tempSHAcc = new HashMap();
        //更新股权账户信息数据
        log.info("更新部分账户数据结构");
        //对象信息
        tempSHAcc.put("account_object_id",cltNo);
//        mapTemp.put("account_object_information_type",1);

        //账户信息 账户基本信息
        tempSHAcc.put("account_number","parth0123552");
        //账户信息 账户生命周期信息
        //账户信息 账户生命周期信息 开户信息
        tempSHAcc.put("account_opening_date","2012/8/25");
        //账户信息 账户关联信息
        tempSHAcc.put("account_association",6);

        String upResp = gd.GDUpdateAccountInfo(gdContractAddress,cltNo,tempSHAcc);
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

        Map mapR = new HashMap();
        String key = "account_object_id"; mapR.put(key,mapSHAccGet2.get(key));
        key = "account_number"; mapR.put(key,mapSHAccGet2.get(key));
        key = "account_opening_date"; mapR.put(key,mapSHAccGet2.get(key));
        key = "account_association"; mapR.put(key,mapSHAccGet2.get(key));

        assertEquals(true,mapSHAccGet2.get("account_certification_time").equals(null));


        Map<String, String> testMap21 = new TreeMap<String, String>(tempSHAcc);
        Map<String, String> testMap22 = new TreeMap<String, String>(mapR);
        assertEquals(replaceCertain(testMap11.toString()),
                replaceCertain(testMap12.toString()));

        Map<String, String> testMap23 = new TreeMap<String, String>(fundAccInfo);
        Map<String, String> testMap24 = new TreeMap<String, String>(mapFundAccGet2);
        assertEquals(replaceCertain(testMap23.toString()),
                replaceCertain(testMap24.toString()));
    }

    @Test
    public void TC21_allUpdateFundAccInfo_Personal()throws Exception{
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
        assertEquals(testMap11.toString().replaceAll("( )?","").replaceAll(":","="),
                testMap12.toString().replaceAll("(\")?( )?", "").replaceAll(":","="));

        Map<String, String> testMap13 = new TreeMap<String, String>(fundAccountInfo);
        Map<String, String> testMap14 = new TreeMap<String, String>(mapFundccGet1);
        assertEquals(replaceCertain(testMap13.toString()),replaceCertain(testMap14.toString()));

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

        String upResp = gd.GDUpdateAccountInfo(gdContractAddress,cltNo,mapTemp);

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
        assertEquals(replaceCertain(testMap11.toString()),
                replaceCertain(testMap12.toString()));

        Map<String, String> testMap23 = new TreeMap<String, String>(mapTemp);
        Map<String, String> testMap24 = new TreeMap<String, String>(mapFundAccGet2);
        assertEquals(replaceCertain(testMap23.toString()),
                replaceCertain(testMap24.toString()));

    }

    @Test
    public void TC21_partUpdateFundAccInfo_Personal()throws Exception{
        //开户
        String cltNo = "updateCLI" + Random(12);
        String shareHolderNo = "SH" + cltNo;
        String fundNo = "fund" + cltNo;

        Map shAccInfo = gdBF.init02ShareholderAccountInfo();
        Map fundAccInfo = gdBF.init02FundAccountInfo();

        //构造股权账户信息
        Map shareHolderInfo = new HashMap();

        shAccInfo.put("account_object_id",shareHolderNo);  //更新账户对象标识字段
        log.info(shAccInfo.toString());
        shareHolderInfo.put("shareholderNo",shareHolderNo);
        shareHolderInfo.put("accountInfo", shAccInfo);
        log.info(shareHolderInfo.toString());

        //资金账户信息
        fundAccInfo.put("account_object_id",fundNo);  //更新账户对象标识字段
        Map mapFundInfo = new HashMap();
        mapFundInfo.put("fundNo",fundNo);
        mapFundInfo.put("accountInfo", fundAccInfo);

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

        shAccInfo.put("account_forzen_date","");
        fundAccInfo.put("account_forzen_date","");
        Map<String, String> testMap11 = new TreeMap<String, String>(shAccInfo);
        Map<String, String> testMap12 = new TreeMap<String, String>(mapSHAccGet1);
        assertEquals(replaceCertain(testMap11.toString()),
                replaceCertain(testMap12.toString()));

        Map<String, String> testMap13 = new TreeMap<String, String>(fundAccInfo);
        Map<String, String> testMap14 = new TreeMap<String, String>(mapFundccGet1);
        assertEquals(replaceCertain(testMap13.toString()),
                replaceCertain(testMap14.toString()));

        //更新资金账户信息数据
        Map mapTemp = new HashMap();
        //更新股权账户信息数据
        log.info("更新部分账户数据结构");
        //对象信息
        mapTemp.put("account_object_id",cltNo);
//        mapTemp.put("account_object_information_type",1);

        //账户信息 账户基本信息
        mapTemp.put("account_number","parth0123552");
        //账户信息 账户生命周期信息
        //账户信息 账户生命周期信息 开户信息
        mapTemp.put("account_opening_date","2012/8/25");
        //账户信息 账户关联信息
        mapTemp.put("account_association",6);

        String upResp = gd.GDUpdateAccountInfo(gdContractAddress,cltNo,mapTemp);

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


        Map mapR = new HashMap();
        String key = "account_object_id"; mapR.put(key,mapFundAccGet2.get(key));
        key = "account_number"; mapR.put(key,mapFundAccGet2.get(key));
        key = "account_opening_date"; mapR.put(key,mapFundAccGet2.get(key));
        key = "account_association"; mapR.put(key,mapFundAccGet2.get(key));

        assertEquals(true,mapFundAccGet2.get("account_certification_time").equals(null));


        Map<String, String> testMap21 = new TreeMap<String, String>(mapR);
        Map<String, String> testMap22 = new TreeMap<String, String>(mapTemp);
        assertEquals(replaceCertain(testMap11.toString()),
                replaceCertain(testMap12.toString()));

        Map<String, String> testMap23 = new TreeMap<String, String>(mapTemp);
        Map<String, String> testMap24 = new TreeMap<String, String>(mapFundAccGet2);
        assertEquals(replaceCertain(testMap23.toString()),
                replaceCertain(testMap24.toString()));

    }


    @Test
    public void TC22_allUpdateEquityProdAccInfo()throws Exception{
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
    public void TC22_allUpdateBondProdAccInfo()throws Exception{
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
    public void TC22_allUpdateFundProdAccInfo()throws Exception{
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
