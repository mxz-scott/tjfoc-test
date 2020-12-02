package com.tjfintech.common.functionTest.guDengTestV2;


import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.GuDeng;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassGD.*;
import static com.tjfintech.common.utils.UtilsClassGD.getFileObj;
import static org.junit.Assert.assertEquals;


@Slf4j

public class GDBeforeCondition {
    TestBuilder testBuilder = TestBuilder.getInstance();
    GuDeng gd = testBuilder.getGuDeng();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    Store store = testBuilder.getStore();


    @Test
    public void gdCreateAccout() throws Exception {
        initRegulationData();//初始化监管对接数据
        sleepAndSaveInfo(5000);

        String cltNo = gdAccClientNo1;

        //创建第1个账户
        Map mapAcc = new HashMap();
        mapAcc = gdCreateAccParam(cltNo);
        gdAccountKeyID1 = mapAcc.get("keyID").toString();
        gdAccount1 = mapAcc.get("accout").toString();
        String txId1 = mapAcc.get("txId").toString();

        //创建第2个账户
        mapAcc.clear();
        cltNo = gdAccClientNo2;
        mapAcc = gdCreateAccParam(cltNo);
        gdAccountKeyID2 = mapAcc.get("keyID").toString();
        gdAccount2 = mapAcc.get("accout").toString();
        String txId2 = mapAcc.get("txId").toString();

        //创建第3个账户
        mapAcc.clear();
        cltNo = gdAccClientNo3;
        mapAcc = gdCreateAccParam(cltNo);
        gdAccountKeyID3 = mapAcc.get("keyID").toString();
        gdAccount3 = mapAcc.get("accout").toString();
        String txId3 = mapAcc.get("txId").toString();

        //创建第4个账户
        mapAcc.clear();
        cltNo = gdAccClientNo4;
        mapAcc = gdCreateAccParam(cltNo);
        gdAccountKeyID4 = mapAcc.get("keyID").toString();
        gdAccount4 = mapAcc.get("accout").toString();
        String txId4 = mapAcc.get("txId").toString();

        //创建第5个账户
        mapAcc.clear();
        cltNo = gdAccClientNo5;
        mapAcc = gdCreateAccParam(cltNo);
        gdAccountKeyID5 = mapAcc.get("keyID").toString();
        gdAccount5 = mapAcc.get("accout").toString();
        String txId5 = mapAcc.get("txId").toString();

        //创建第6个账户
        mapAcc.clear();
        cltNo = gdAccClientNo6;
        mapAcc = gdCreateAccParam(cltNo);
        gdAccountKeyID6 = mapAcc.get("keyID").toString();
        gdAccount6 = mapAcc.get("accout").toString();
        String txId6 = mapAcc.get("txId").toString();

        //创建第7个账户
        mapAcc.clear();
        cltNo = gdAccClientNo7;
        mapAcc = gdCreateAccParam(cltNo);
        gdAccountKeyID7 = mapAcc.get("keyID").toString();
        gdAccount7 = mapAcc.get("accout").toString();
        String txId7 = mapAcc.get("txId").toString();

        //创建第8个账户
        mapAcc.clear();
        cltNo = gdAccClientNo8;
        mapAcc = gdCreateAccParam(cltNo);
        gdAccountKeyID8 = mapAcc.get("keyID").toString();
        gdAccount8 = mapAcc.get("accout").toString();
        String txId8 = mapAcc.get("txId").toString();

        //创建第9个账户
        mapAcc.clear();
        cltNo = gdAccClientNo9;
        mapAcc = gdCreateAccParam(cltNo);
        gdAccountKeyID9 = mapAcc.get("keyID").toString();
        gdAccount9 = mapAcc.get("accout").toString();
        String txId9 = mapAcc.get("txId").toString();

        //创建第10个账户
        mapAcc.clear();
        cltNo = gdAccClientNo10;
        mapAcc = gdCreateAccParam(cltNo);
        gdAccountKeyID10 = mapAcc.get("keyID").toString();
        gdAccount10 = mapAcc.get("accout").toString();
        String txId10 = mapAcc.get("txId").toString();

        commonFunc.sdkCheckTxOrSleep(txId10, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        //判断所有开户接口交易上链
        assertEquals("200", JSONObject.fromObject(store.GetTxDetail(txId1)).getString("state"));
        assertEquals("200", JSONObject.fromObject(store.GetTxDetail(txId2)).getString("state"));
        assertEquals("200", JSONObject.fromObject(store.GetTxDetail(txId3)).getString("state"));
        assertEquals("200", JSONObject.fromObject(store.GetTxDetail(txId4)).getString("state"));
        assertEquals("200", JSONObject.fromObject(store.GetTxDetail(txId5)).getString("state"));
        assertEquals("200", JSONObject.fromObject(store.GetTxDetail(txId6)).getString("state"));
        assertEquals("200", JSONObject.fromObject(store.GetTxDetail(txId7)).getString("state"));
        assertEquals("200", JSONObject.fromObject(store.GetTxDetail(txId8)).getString("state"));
        assertEquals("200", JSONObject.fromObject(store.GetTxDetail(txId9)).getString("state"));
        assertEquals("200", JSONObject.fromObject(store.GetTxDetail(txId10)).getString("state"));


    }

    public Map<String, String> gdCreateAccParam(String clientNo) {
        String cltNo = clientNo;
        String shareHolderNo = "SH" + cltNo;
        String fundNo = "fund" + cltNo;

        //构造股权账户信息
        Map shareHolderInfo = new HashMap();
        init02ShareholderAccountInfo();
        shAccountInfo.put("account_object_id", shareHolderNo);  //更新账户对象标识字段
        log.info(shAccountInfo.toString());
        shareHolderInfo.put("shareholderNo", shareHolderNo);
        shareHolderInfo.put("accountInfo", shAccountInfo);
        log.info(shareHolderInfo.toString());

        //资金账户信息
        init02FundAccountInfo();
        fundAccountInfo.put("account_object_id", fundNo);  //更新账户对象标识字段
        Map mapFundInfo = new HashMap();
        mapFundInfo.put("fundNo", fundNo);
        mapFundInfo.put("accountInfo", fundAccountInfo);

        //构造个人/投资者主体信息
        init01PersonalSubjectInfo();
        investorSubjectInfo.put("subject_object_id", cltNo);  //更新对象标识字段
        investorSubjectInfo.put("subject_id", "sid" + cltNo);  //更新主体标识字段

        String response = gd.GDCreateAccout(gdContractAddress, cltNo, mapFundInfo, shareHolderInfo, investorSubjectInfo);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        assertEquals(cltNo, JSONObject.fromObject(response).getJSONObject("data").getJSONObject("accountList").getString("clientNo"));
        assertEquals(shareHolderNo, JSONObject.fromObject(response).getJSONObject("data").getJSONObject("accountList").getString("shareholderNo"));
        String keyID = JSONObject.fromObject(response).getJSONObject("data").getJSONObject("accountList").getString("keyId");
        String addr = JSONObject.fromObject(response).getJSONObject("data").getJSONObject("accountList").getString("address");

        Map mapAccInfo = new HashMap();
        mapAccInfo.put("keyID", keyID);
        mapAccInfo.put("accout", addr);
        mapAccInfo.put("txId", txId);
        mapAccInfo.put("response", response);

        mapAccAddr.put(addr, clientNo);

        return mapAccInfo;
    }

    //    @Test
    public void initRegulationData() {
        //更新系统合约
        gd.GDEquitySystemInit(gdContractAddress, gdPlatfromKeyID);

        log.info("初始化监管相关数据结构");
        enterpriseSubjectInfo = init01EnterpriseSubjectInfo();      //初始化企业主体数据信息  涉及接口 企业挂牌登记
        investorSubjectInfo = init01PersonalSubjectInfo();        //初始化个人主体数据信息  涉及接口 开户
        shAccountInfo = init02ShareholderAccountInfo();          //初始化账户数据信息 股权账户  涉及接口 开户
        fundAccountInfo = init02FundAccountInfo();            //初始化账户数据信息 资金账户  涉及接口 开户
        equityProductInfo = init03EquityProductInfo();          //初始化私募股权类产品数据信息  涉及接口 挂牌企业登记 股份增发 场内转板
        bondProductInfo = init03BondProductInfo();            //初始化私募可转债产品数据信息  涉及接口 挂牌企业登记 发行
        fundProductInfo = init03FundProductInfo();            //初始化基金股权产品数据信息  涉及接口 挂牌企业登记 发行
        txInformation = init04TxInfo();                     //初始化交易数据信息  涉及接口 过户转让
        registerInfo = init05RegInfo();                    //初始化登记数据信息  涉及接口 发行 股份性质变更 过户转让 增发 冻结 解除冻结
        settleInfo = init06SettleInfo();                 //初始化资金结算数据信息  涉及接口 资金清算
        disclosureInfo = init07PublishInfo();                //初始化信息数据信息  涉及接口 写入公告

        //初始化listRegInfo
        listRegInfo.clear();
        listRegInfo.add(registerInfo);
        listRegInfo.add(registerInfo);
    }

    public Map init01EnterpriseSubjectInfo() {
        Map mapTemp = new HashMap();

        log.info("初始化01主体企业数据结构");
        List<String> fileList = new ArrayList<>();
        fileList.add("file.txt");
        mapTemp.clear();

        //-----------------主体资质信息---------------//
        List<Map> listSQI = new ArrayList<>();
        List<Map> listQAI = new ArrayList<>();
        List<Map> listISI = new ArrayList<>();
        Map mapSQI = new HashMap();

        Map mapQAI = new HashMap();
        Map mapISI = new HashMap();

        //主体基本信息 主体资质信息 资质信息
        mapSQI.put("subject_qualification_category", 1);
        mapSQI.put("subject_market_roles_type", 1);
        List<Integer> type = new ArrayList<>();
        type.add(0);
        type.add(1);
        mapSQI.put("subject_intermediary_qualification", type);
        mapSQI.put("subject_financial_qualification_type", 1);


        //主体基本信息 主体资质信息 资质认证信息
        mapQAI.put("subject_qualification_code", "资质代码");
        mapQAI.put("subject_role_qualification_certification_doc", getListFileObj());
        mapQAI.put("subject_qualification_authenticator", "认证方");
        mapQAI.put("subject_certification_time", time1);
        mapQAI.put("subject_qualification_reviewer", "审核方");
        mapQAI.put("subject_review_time", time1);
        mapQAI.put("subject_qualification_status", true);
        listQAI.add(mapQAI);

        //主体基本信息 主体资质信息 投资者适当性信息
        mapISI.put("subject_investor_qualification", 1);
        mapISI.put("subject_investor_qualification_sub", "适当性认证子类");
        mapISI.put("subject_investor_qualification_description", "适当性认证描述");
        mapISI.put("subject_investor_qualification_certificate", getListFileObj());
        mapISI.put("subject_investor_qualification_certifier_ref", "sub_ref_0001");
        mapISI.put("subject_investor_qualification_certifier_name", "适当性认证方主体名称");
        mapISI.put("subject_investor_qualification_certificate_time", time2);
        mapISI.put("subject_investor_qualification_status", true);
        listISI.add(mapISI);

        mapSQI.put("investor_suitability_information", listISI);
        mapSQI.put("qualification_authentication_information", listQAI);


        listSQI.add(mapSQI);
        //-----------------主体资质信息---------------//


        //-----------------主体基本信息---------------//
        //对象标识
        mapTemp.put("subject_object_id", gdCompanyID);
//        mapTemp.put("subject_object_information_type",0);
//        mapTemp.put("subject_type",1);

        //主体信息 主体基本信息 主体通用信息
        mapTemp.put("subject_id", gdCompanyID);
        mapTemp.put("subject_type", 1);
        mapTemp.put("subject_main_administrative_region", 0);
        mapTemp.put("subject_create_time", time3);

        //主体信息 主体基本信息 主体资质信息
        //主体信息 主体基本信息 主体资质信息 资质信息
        mapTemp.put("subject_qualification_information", listSQI);
        //-----------------主体基本信息---------------//


        //-----------------机构主体信息---------------//
        //主体信息 机构主体信息 企业基本信息 基本信息描述
        mapTemp.put("subject_company_name", "公司全称CHARACTER");
        mapTemp.put("subject_company_english_name", "英文名称CHARACTER");
        mapTemp.put("subject_company_short_name", "公司简称CHARACTER");
        mapTemp.put("subject_company_short_english_name", "英文简称CHARACTER");
        mapTemp.put("subject_organization_nature", 0);
        mapTemp.put("subject_legal_type", 0);
        mapTemp.put("subject_economic_type", 0);
        mapTemp.put("subject_company_type", 0);
        mapTemp.put("subject_scale_type", 0);
        mapTemp.put("subject_high_technology_enterprise", 0);

        Map mapCertDoc = new HashMap();
        List<Map> listCertDoc = new ArrayList<>();
        mapCertDoc.put("type",1);
        mapCertDoc.put("code","1233333333");
        listCertDoc.add(mapCertDoc);

        mapTemp.put("subject_document_information",listCertDoc);
        mapTemp.put("subject_registry_date", date1);
        mapTemp.put("subject_business_license", getListFileObj());
        mapTemp.put("subject_business_scope", "经营范围CHARACTER");
        mapTemp.put("subject_industry", 1);
        mapTemp.put("subject_company_business", "主营业务CHARACTER");
        mapTemp.put("subject_company_profile", "公司简介TEXT");
        mapTemp.put("subject_registered_capital", 6000000);
        mapTemp.put("subject_registered_capital_currency", "156");
        mapTemp.put("subject_paid_in_capital", 6000000);
        mapTemp.put("subject_paid_in_capital_currency", "156");
        mapTemp.put("subject_registered_address", "注册地址CHARACTER");
        mapTemp.put("subject_province", "注册地所在省份CHARACTER");
        mapTemp.put("subject_city", "注册地所在市CHARACTER");
        mapTemp.put("subject_district", "注册地所在区CHARACTER");
        mapTemp.put("subject_office_address", "办公地址CHARACTER");
        mapTemp.put("subject_contact_address", "联系地址CHARACTER");
        mapTemp.put("subject_contact_number", "联系电话CHARACTER");
        mapTemp.put("subject_fax", "企业传真CHARACTER");
        mapTemp.put("subject_postal_code", "邮政编码CHARACTER");
        mapTemp.put("subject_internet_address", "互联网地址CHARACTER");
        mapTemp.put("subject_mail_box", "电子邮箱CHARACTER");
        mapTemp.put("subject_association_articles", getListFileObj());
        mapTemp.put("subject_regulator", "主管单位CHARACTER");
        mapTemp.put("subject_shareholders_number", 10);
        mapTemp.put("subject_taxpayer_id_number", "纳税人识别号CHARACTER");
        mapTemp.put("subject_invoice_bank", "发票开户行CHARACTER");
        mapTemp.put("subject_invoice_account_number", "发票账号CHARACTER");
        mapTemp.put("subject_invoice_address", "发票地址CHARACTER");
        mapTemp.put("subject_invoice_telephone_number", "发票电话CHARACTER");
        mapTemp.put("subject_approval_time", time2);
        mapTemp.put("subject_insured_number", 50);
        mapTemp.put("subject_company_status", 0);
        mapTemp.put("subject_company_status_deregistration", "注销原因CHARACTER");
        mapTemp.put("subject_company_status_deregistration_date", date4);
        mapTemp.put("subject_company_status_windingup", "吊销原因CHARACTER");
        mapTemp.put("subject_company_status_windingup_date", date4);
        List<String> name = new ArrayList<>();
        name.add("曾用名a");
        name.add("曾用名b");
        name.add("曾用名c");
        mapTemp.put("subject_name_used_before", name);
        mapTemp.put("subject_personnel_size", "人员规模CHARACTER");
        //-----------------机构主体信息---------------//


        //主体信息 机构主体信息 企业基本信息 主要人员信息
        //-----------------主要人员信息---------------//
        Map mapLMI = new HashMap();
        List<Map> listLMI = new ArrayList<>();
        mapLMI.put("subject_key_personnel_appointment_end", date2);
        mapLMI.put("subject_key_personnel_type", 0);
        mapLMI.put("subject_key_personnel_position", 0);
        mapLMI.put("subject_key_personnel_appointment_start", date2);
        mapLMI.put("subject_key_personnel_name", "姓名CHARACTER");
        mapLMI.put("subject_key_personnel_nationality", "国籍CHARACTER");
        mapLMI.put("subject_document_type", 0);
        mapLMI.put("subject_key_personnel_id", "证件号码CHARACTER");
        mapLMI.put("subject_key_personnel_address", "证件地址CHARACTER");
        mapLMI.put("subject_key_personnel_shareholding_ratio", 20);
        mapLMI.put("subject_key_personnel_shareholding", 500);
        mapLMI.put("subject_key_personnel_contact", "联系方式CHARACTER");
        listLMI.add(mapLMI);

        mapTemp.put("leading_member_information", listLMI);
        //-----------------主要人员信息---------------//


        return mapTemp;
    }

//    public Map init01PersonalSubjectInfo() {
//        Map mapTemp = new HashMap();
//        log.info("初始化01主体个人数据结构");
//        //-----------------主体资质信息---------------//
//        List<Map> listSQI = new ArrayList<>();
//        List<Map> listQAI = new ArrayList<>();
//        List<Map> listISI = new ArrayList<>();
//        Map mapSQI = new HashMap();
//
//        Map mapQAI = new HashMap();
//        Map mapISI = new HashMap();
//
//        //主体基本信息 主体资质信息 资质信息
//        mapSQI.put("subject_qualification_category",1);
//        mapSQI.put("subject_market_roles_type",1);
//        List<Integer> type = new ArrayList<>(); type.add(0);type.add(1);
//        mapSQI.put("subject_intermediary_qualification",type);
//        mapSQI.put("subject_financial_qualification_type",1);
//
//
//        //主体基本信息 主体资质信息 资质认证信息
//        //{"file_number":"1","file_name": "12312312","url": "12312312","hash": "12312312","summary": "12312312","term_of_validity_type": "0","term_of_validity":"yyyy/MM/dd"}
//        //文件对象
//        Map fileMap = new HashMap();
//        fileMap.put("file_number",1);
//        fileMap.put("file_name","file1.pdf");
//        fileMap.put("hash","da1234filehash5222");
//        fileMap.put("url","http://test.com/file/201/file1.pdf");
//        fileMap.put("summary","简述");
//        fileMap.put("term_of_validity_type","0");
//        fileMap.put("term_of_validity","2020/04/18");
//
//        mapQAI.put("subject_qualification_code","资质代码");
//        mapQAI.put("subject_role_qualification_certification_doc",fileMap);
//        mapQAI.put("subject_qualification_authenticator","认证方");
//        mapQAI.put("subject_certification_time","2020/10/12 12:00:00");
//        mapQAI.put("subject_qualification_reviewer","审核方");
//        mapQAI.put("subject_review_time","2020/10/11 12:00:00");
//        mapQAI.put("subject_qualification_status",true);
//        listQAI.add(mapQAI);
//
//        //主体基本信息 主体资质信息 投资者适当性信息
//        mapISI.put("subject_investor_qualification",1);
//        mapISI.put("subject_investor_qualification_sub","适当性认证子类");
//        mapISI.put("subject_investor_qualification_description","适当性认证描述");
//        mapISI.put("subject_investor_qualification_certificate",fileMap);
//        mapISI.put("subject_investor_qualification_certifier_ref","sub_ref_0001");
//        mapISI.put("subject_investor_qualification_certifier_name","适当性认证方主体名称");
//        mapISI.put("subject_investor_qualification_certificate_time","2020/11/12 12:00:00");
//        mapISI.put("subject_investor_qualification_status",true);
//        listISI.add(mapISI);
//
//        mapSQI.put("investor_suitability_information",listISI);
//        mapSQI.put("qualification_authentication_information",listQAI);
//
//        listSQI.add(mapSQI);
//        //-----------------主体资质信息---------------//
//
//
//        //-----------------主体基本信息---------------//
//        //对象标识
//        mapTemp.put("subject_object_id","clientNo0001");
////        mapTemp.put("subject_object_information_type",0);
//        mapTemp.put("subject_type",2);
//
//        //主体信息 主体基本信息 主体通用信息
//        mapTemp.put("subject_id", "clientNo0001sub");
//        mapTemp.put("subject_main_administrative_region",0);
//        mapTemp.put("subject_create_time","2020/11/06 14:14:59");
//
//        //主体信息 主体基本信息 主体资质信息
//        //主体信息 主体基本信息 主体资质信息 资质信息
//        mapTemp.put("subject_qualification_information",listSQI);
//        //-----------------主体基本信息---------------//
//
//        //主体信息 个人主体信息 个人主体基本信息
//        mapTemp.put("subject_investor_name","zhangsan");
//        mapTemp.put("subject_id_type",0);
//        mapTemp.put("subject_id_number","个人身份证件号CHARACTER");
//        mapTemp.put("subject_id_address","个人证件地址CHARACTER");
//        mapTemp.put("subject_contact_address","个人联系地址CHARACTER");
//        mapTemp.put("subject_contact_number","个人联系电话CHARACTER");
//        mapTemp.put("subject_cellphone_number","个人手机号CHARACTER");
//        mapTemp.put("subject_personal_fax","个人传真CHARACTER");
//        mapTemp.put("subject_postal_code","邮政编码CHARACTER");
//        mapTemp.put("subject_id_doc_mailbox","电子邮箱CHARACTER");
//        mapTemp.put("subject_education",1);
//        mapTemp.put("subject_occupation",2);
//        mapTemp.put("subject_industry",1);
//        mapTemp.put("subject_birthday","yyyy/MM/dd");
//        mapTemp.put("subject_gender",0);
//        mapTemp.put("subject_work_unit","工作单位CHARACTER");
//        mapTemp.put("subject_investment_period","投资年限CHARACTER");
//        mapTemp.put("subject_investment_experience","投资经历CHARACTER");
//        mapTemp.put("subject_native_place","籍贯CHARACTER");
//        mapTemp.put("subject_province","省份CHARACTER");
//        mapTemp.put("subject_city","城市CHARACTER");
//
//
//        return mapTemp;
//    }

    public Map init01PersonalSubjectInfo() {
        Map mapTemp = new HashMap();
        log.info("初始化01主体个人数据结构");
        mapTemp.clear();

        //-----------------主体资质信息start---------------//
        List<Map> listSQI = new ArrayList<>();
        List<Map> listQAI = new ArrayList<>();
        List<Map> listISI = new ArrayList<>();

        Map mapSQI = new HashMap();
        Map mapQAI = new HashMap();
        Map mapISI = new HashMap();

        //主体信息 主体基本信息 主体资质信息 资质信息
        mapSQI.put("subject_qualification_category", 1);
        mapSQI.put("subject_market_roles_type", 1);
        List<Integer> type = new ArrayList<>();
        type.add(0);
        type.add(1);
        mapSQI.put("subject_intermediary_qualification", type);
        mapSQI.put("subject_financial_qualification_type", 1);

        //主体信息 主体基本信息 主体资质信息 资质认证信息
        mapQAI.put("subject_qualification_code", "资质代码");
        mapQAI.put("subject_role_qualification_certification_doc", getListFileObj());
        mapQAI.put("subject_qualification_authenticator", "认证方");
        mapQAI.put("subject_certification_time", time1);
        mapQAI.put("subject_qualification_reviewer", "审核方");
        mapQAI.put("subject_review_time", time1);
        mapQAI.put("subject_qualification_status", true);
        listQAI.add(mapQAI);

        //主体信息 主体基本信息 主体资质信息 投资者适当性信息
        mapISI.put("subject_investor_qualification", 1);
        mapISI.put("subject_investor_qualification_sub", "适当性认证子类");
        mapISI.put("subject_investor_qualification_description", "适当性认证描述");
        mapISI.put("subject_investor_qualification_certificate", getListFileObj());
        mapISI.put("subject_investor_qualification_certifier_ref", "sub_ref_0001");
        mapISI.put("subject_investor_qualification_certifier_name", "适当性认证方主体名称");
        mapISI.put("subject_investor_qualification_certificate_time", time1);
        mapISI.put("subject_investor_qualification_status", true);
        listISI.add(mapISI);

        mapSQI.put("investor_suitability_information", listISI);
        mapSQI.put("qualification_authentication_information", listQAI);

        listSQI.add(mapSQI);
        //-----------------主体资质信息end---------------//


        //-----------------主体基本信息start---------------//
        //对象标识
        mapTemp.put("subject_object_id", "clientNo0001");
        mapTemp.put("subject_type", 2);

        //主体信息 主体基本信息 主体通用信息
        mapTemp.put("subject_id", "clientNo0001sub");
        mapTemp.put("subject_main_administrative_region", 0);
        mapTemp.put("subject_create_time", time1);

        //主体信息 主体基本信息 主体资质信息
        mapTemp.put("subject_qualification_information", listSQI);
        //-----------------主体基本信息end---------------//

        //主体信息 个人主体信息 个人主体基本信息
        mapTemp.put("subject_investor_name", "zhangsan");
        mapTemp.put("subject_id_type", 0);
        mapTemp.put("subject_id_number", "个人身份证件号CHARACTER");
        mapTemp.put("subject_id_address", "个人证件地址CHARACTER");
        mapTemp.put("subject_contact_address", "个人联系地址CHARACTER");
        mapTemp.put("subject_contact_number", "个人联系电话CHARACTER");
        mapTemp.put("subject_cellphone_number", "个人手机号CHARACTER");
        mapTemp.put("subject_personal_fax", "个人传真CHARACTER");
        mapTemp.put("subject_postal_code", "邮政编码CHARACTER");
        mapTemp.put("subject_id_doc_mailbox", "电子邮箱CHARACTER");
        mapTemp.put("subject_education", 1);
        mapTemp.put("subject_occupation", 2);
        mapTemp.put("subject_industry", 1);
        mapTemp.put("subject_birthday", date1);
        mapTemp.put("subject_gender", 0);
        mapTemp.put("subject_work_unit", "工作单位CHARACTER");
        mapTemp.put("subject_investment_period", "投资年限CHARACTER");
        mapTemp.put("subject_investment_experience", "投资经历CHARACTER");
        mapTemp.put("subject_native_place", "籍贯CHARACTER");
        mapTemp.put("subject_province", "省份CHARACTER");
        mapTemp.put("subject_city", "城市CHARACTER");

        return mapTemp;
    }

//    public Map init02ShareholderAccountInfo() {
//        Map mapTemp = new HashMap();
//        log.info("初始化02账户数据结构");
//        //默认股权账户
//        List<String> fileList1 = new ArrayList<>();
//        fileList1.add("test1.pdf");
//        fileList1.add("test2.pdf");
//
//        List<String> fileList2 = new ArrayList<>();
//        fileList2.add("test21.pdf");
//        fileList2.add("test22.pdf");
//        List<String> fileList3 = new ArrayList<>();
//        fileList3.add("test31.pdf");
//        fileList3.add("test32.pdf");
//        List<String> fileList4 = new ArrayList<>();
//        fileList4.add("test41.pdf");
//        fileList4.add("test42.pdf");
//        List<String> fileList5 = new ArrayList<>();
//        fileList5.add("test51.pdf");
//        fileList5.add("test52.pdf");
//        mapTemp.clear();
//
//        //对象信息
//        mapTemp.put("account_object_id", "testacc00001");
//        mapTemp.put("account_object_information_type", 0);
//
//        //账户信息 账户基本信息
//        mapTemp.put("account_holder_subject_ref", "hrefid00001");
//        mapTemp.put("account_depository_subject_ref", "drefid00001");
//        mapTemp.put("account_number", "h0123555");
//        mapTemp.put("account_type", 0);  //默认股权账户
//        mapTemp.put("account_never", 0);
//        mapTemp.put("account_status", 0);
//
//        //账户信息 账户资质信息
//        mapTemp.put("account_qualification_certification_file", fileList1);
//        mapTemp.put("account_certifier", "监管局");
//        mapTemp.put("account_auditor", "认证者");
//        mapTemp.put("account_certification_time", "2012/6/25");
//        mapTemp.put("account_audit_time", "2012/6/25");
//
//        //账户信息 账户生命周期信息
//        //账户信息 账户生命周期信息 开户信息
//        mapTemp.put("account_opening_date", "2012/6/25");
//        mapTemp.put("account_opening_certificate", fileList4);
//
//        //账户信息 账户生命周期信息 销户信息
//        mapTemp.put("account_closing_date", "2022/6/25");
//        mapTemp.put("account_closing_certificate", fileList2);
//
//        //账户信息 账户生命周期信息 冻结信息
//        mapTemp.put("account_forzen_date", "2020/6/25");
//        mapTemp.put("account_forzen_certificate", fileList3);
//
//        //账户信息 账户生命周期信息 解冻信息
//        mapTemp.put("account_thaw_date", "2020/6/25");
//        mapTemp.put("account_thaw_certificate", fileList4);
//
//        //账户信息 账户关联信息
//        mapTemp.put("account_association", 0);
//        mapTemp.put("account_associated_account_ref", "t5pdf");
//        mapTemp.put("account_associated_acct_certificates", fileList5);
//        return mapTemp;
//    }

    public Map init02ShareholderAccountInfo() {
        Map mapTemp = new HashMap();
        log.info("初始化02账户数据结构");
        mapTemp.clear();

        //账户信息 账户基本信息
        mapTemp.put("account_subject_ref", "hrefid00001");
        mapTemp.put("account_depository_ref", "drefid00001");
        mapTemp.put("account_number", "h0123555");
        mapTemp.put("account_type", 0);  //默认股权账户
        List<Integer> type = new ArrayList<>();
        type.add(0);
        type.add(1);
        mapTemp.put("account_purpose", type);
        mapTemp.put("account_status", 0);
        mapTemp.put("account_create_time", time1);

        //账户信息 账户生命周期信息
        //账户信息 账户生命周期信息 开户信息
        mapTemp.put("account_establish_date", date1);
        mapTemp.put("account_open_date", date1);
        mapTemp.put("account_open_doc", getListFileObj());
        mapTemp.put("account_open_agency_name", "name0001");
        mapTemp.put("account_open_agency_phone", "phone0001");

        //账户信息 账户生命周期信息 销户信息
        mapTemp.put("account_close_date", date1);
        mapTemp.put("account_close_doc", getListFileObj());
        mapTemp.put("account_close_agency_name", "name0001");
        mapTemp.put("account_close_agency_phone", "phone0001");

        //账户信息 账户生命周期信息 冻结信息
        mapTemp.put("account_frozen_date", date1);
        mapTemp.put("account_frozen_doc", getListFileObj());
        mapTemp.put("account_frozen_applicant_name", "name0001");
        mapTemp.put("account_frozen_event_description", "phone0001");

        //账户信息 账户生命周期信息 解冻信息
        mapTemp.put("account_thaw_date", date1);
        mapTemp.put("account_thaw_doc", getListFileObj());
        mapTemp.put("account_thaw_applicant_name", "name0001");
        mapTemp.put("account_thaw_event_description", "escription0001");

        //账户信息 账户关联信息
        mapTemp.put("account_association", 0);
        mapTemp.put("account_associated_account_ref", "t5pdf");
        return mapTemp;
    }

//    public Map init02FundAccountInfo() {
//        Map mapTemp = new HashMap();
//        log.info("初始化02账户数据结构");
//        //默认股权账户
//        List<String> fileList1 = new ArrayList<>();
//        fileList1.add("test1.pdf");
//        fileList1.add("test2.pdf");
//
//        List<String> fileList2 = new ArrayList<>();
//        fileList2.add("test21.pdf");
//        fileList2.add("test22.pdf");
//        List<String> fileList3 = new ArrayList<>();
//        fileList3.add("test31.pdf");
//        fileList3.add("test32.pdf");
//        List<String> fileList4 = new ArrayList<>();
//        fileList4.add("test41.pdf");
//        fileList4.add("test42.pdf");
//        List<String> fileList5 = new ArrayList<>();
//        fileList5.add("test51.pdf");
//        fileList5.add("test52.pdf");
//        mapTemp.clear();
//
//        //对象信息
//        mapTemp.put("account_object_id", "testacc00001");
//        mapTemp.put("account_object_information_type", 0);
//
//        //账户信息 账户基本信息
//        mapTemp.put("account_holder_subject_ref", "hrefid00001");
//        mapTemp.put("account_depository_subject_ref", "drefid00001");
//        mapTemp.put("account_number", "h0123555");
//        mapTemp.put("account_type", 1);  //资金账户
//        mapTemp.put("account_never", 0);
//        mapTemp.put("account_status", 0);
//
//        //账户信息 账户资质信息
//        mapTemp.put("account_qualification_certification_file", fileList1);
//        mapTemp.put("account_certifier", "监管局");
//        mapTemp.put("account_auditor", "认证者");
//        mapTemp.put("account_certification_time", "2012/6/25");
//        mapTemp.put("account_audit_time", "2012/6/25");
//
//        //账户信息 账户生命周期信息
//        //账户信息 账户生命周期信息 开户信息
//        mapTemp.put("account_opening_date", "2012/6/25");
//        mapTemp.put("account_opening_certificate", fileList4);
//
//        //账户信息 账户生命周期信息 销户信息
//        mapTemp.put("account_closing_date", "2022/6/25");
//        mapTemp.put("account_closing_certificate", fileList2);
//
//        //账户信息 账户生命周期信息 冻结信息
//        mapTemp.put("account_forzen_date", "2020/6/25");
//        mapTemp.put("account_forzen_certificate", fileList3);
//
//        //账户信息 账户生命周期信息 解冻信息
//        mapTemp.put("account_thaw_date", "2020/6/25");
//        mapTemp.put("account_thaw_certificate", fileList4);
//
//        //账户信息 账户关联信息
//        mapTemp.put("account_association", 0);
//        mapTemp.put("account_associated_account_ref", "t5pdf");
//        mapTemp.put("account_associated_acct_certificates", fileList5);
//        return mapTemp;
//    }

    public Map init02FundAccountInfo() {
        Map mapTemp = new HashMap();
        log.info("初始化02账户数据结构");
        mapTemp.clear();

        //账户信息 账户基本信息
        mapTemp.put("account_subject_ref", "hrefid00001");
        mapTemp.put("account_depository_ref", "drefid00001");
        mapTemp.put("account_number", "h0123555");
        mapTemp.put("account_type", 1);  //资金账户
        List<Integer> type = new ArrayList<>();
        type.add(0);
        type.add(1);
        mapTemp.put("account_purpose", type);
        mapTemp.put("account_status", 0);
        mapTemp.put("account_create_time", time1);

        //账户信息 账户生命周期信息
        //账户信息 账户生命周期信息 开户信息
        mapTemp.put("account_establish_date", date1);
        mapTemp.put("account_open_date", date1);
        mapTemp.put("account_open_doc", getListFileObj());
        mapTemp.put("account_open_agency_name", "name0001");
        mapTemp.put("account_open_agency_phone", "phone0001");

        //账户信息 账户生命周期信息 销户信息
        mapTemp.put("account_close_date", "2022/6/25");
        mapTemp.put("account_close_doc", getListFileObj());
        mapTemp.put("account_close_agency_name", "name0001");
        mapTemp.put("account_close_agency_phone", "phone0001");

        //账户信息 账户生命周期信息 冻结信息
        mapTemp.put("account_frozen_date", date1);
        mapTemp.put("account_frozen_doc", getListFileObj());
        mapTemp.put("account_frozen_applicant_name", "name0001");
        mapTemp.put("account_frozen_event_description", "phone0001");

        //账户信息 账户生命周期信息 解冻信息
        mapTemp.put("account_thaw_date", date1);
        mapTemp.put("account_thaw_doc", getListFileObj());
        mapTemp.put("account_thaw_applicant_name", "name0001");
        mapTemp.put("account_thaw_event_description", "escription0001");

        //账户信息 账户关联信息
        mapTemp.put("account_association", 0);
        mapTemp.put("account_associated_account_ref", "t5pdf");
        return mapTemp;
    }


    public Map productCommonInfo(int type) {
        Map mapProdComInfo = new HashMap();

        //对象标识
        mapProdComInfo.put("product_object_id", gdEquityCode);
//        mapProdComInfo.put("product_object_information_type",0);

        //产品信息 基本信息 产品基本信息
        mapProdComInfo.put("product_trading_market_category", 2);
        mapProdComInfo.put("product_market_subject", "交易场所主体引用CHARACTER");
        mapProdComInfo.put("product_market_subject_name", "交易场所主体名称CHARACTER");
        mapProdComInfo.put("product_plate_trading_name", "交易场所板块名称CHARACTER");
        mapProdComInfo.put("product_issuer_subject_ref", gdCompanyID);
        mapProdComInfo.put("product_issuer_name", "发行主体名称CHARACTER");
        mapProdComInfo.put("product_code", "产品代码CHARACTER");
        mapProdComInfo.put("product_name", "产品全称CHARACTER");
        mapProdComInfo.put("product_name_abbreviation", "产品简称CHARACTER");
        mapProdComInfo.put("product_type_function", 2);
        mapProdComInfo.put("product_type", type);
        mapProdComInfo.put("product_account_number_max", 2000);
        mapProdComInfo.put("product_info_disclosure_way", 0);
        mapProdComInfo.put("product_scale_unit", 12);
        mapProdComInfo.put("product_scale_currency", "产品规模币种CHARACTER");
        mapProdComInfo.put("product_scale", 50000);
        mapProdComInfo.put("product_customer_browsing_right", 1);
        mapProdComInfo.put("product_issuer_contact_person", "联系人CHARACTER");
        mapProdComInfo.put("product_issuer_contact_info", "联系信息CHARACTER");
        mapProdComInfo.put("product_create_time", time2);

        //产品信息 基本信息 服务方信息
        Map mapServ = new HashMap();
        List<Map> listServ = new ArrayList<>();
        mapServ.put("service_provider_type", 0);
        mapServ.put("service_provider_subject_ref", "服务方主体引用");
        mapServ.put("service_provider_name", "服务方主体名称");
        listServ.add(mapServ);
        mapServ.clear();
        mapServ.put("service_provider_type", 1);
        mapServ.put("service_provider_subject_ref", "服务方主体引用");
        mapServ.put("service_provider_name", "服务方主体名称");
        listServ.add(mapServ);

        mapProdComInfo.put("service_provider_information", listServ);

        //产品信息 基本信息 产品文件信息
        List<Map> listMapPF = new ArrayList<>();
        Map mapPF = new HashMap();
        mapPF.put("product_issue_doc", getListFileObj());
        listMapPF.add(mapPF);
        mapProdComInfo.put("product_file_information", listMapPF);


        //产品信息 产品标的信息
        mapProdComInfo.put("product_fund_use_type", 2);
        mapProdComInfo.put("product_description_fund_use", "资金用途描述CHARACTER");
        mapProdComInfo.put("product_document_describing_funds", getListFileObj());
        mapProdComInfo.put("product_business_purpose_name", "经营用途名称CHARACTER");
        mapProdComInfo.put("product_business_purpose_details", "经营用途详情CHARACTER");
        mapProdComInfo.put("product_business_purpose_documents", getListFileObj());
        mapProdComInfo.put("product_investment_products_type", 1);
        mapProdComInfo.put("product_Investment_proportion_range", 50);
        mapProdComInfo.put("product_Investment_product_details", "投资产品详情CHARACTER");
        mapProdComInfo.put("product_detailed_description_document", getListFileObj());


        return mapProdComInfo;
    }

    public Map productCommonInfo2(Map mapProdComInfo) {

        //产品信息 交易信息
        //产品信息 交易信息 交易状态
        mapProdComInfo.put("product_transaction_status", 0);

        //产品信息 交易信息 挂牌信息
        mapProdComInfo.put("product_transaction_scope", 12);
        mapProdComInfo.put("product_transfer_permission_institution_to_individual", true);
        mapProdComInfo.put("product_transfer_lockup_days", 20);
        mapProdComInfo.put("product_trasfer_validity", 30);
        mapProdComInfo.put("product_risk_level", "产品风险级别CHARACTER");
        mapProdComInfo.put("product_transaction_unit", 1000);
        mapProdComInfo.put("product_listing_code", "挂牌代码CHARACTER");
        mapProdComInfo.put("product_listing_date", date2);
        mapProdComInfo.put("product_listing_remarks", "挂牌备注信息CHARACTER");

        //产品信息 交易信息 摘牌信息
        mapProdComInfo.put("product_delisting_date", date3);
        mapProdComInfo.put("product_delisting_type", 1);
        mapProdComInfo.put("product_delisting_reason", 1);
        mapProdComInfo.put("product_transfer_board_mrket", 2);
        mapProdComInfo.put("product_acquisition_company_market", 3);
        mapProdComInfo.put("product_delisting_remarks", "摘牌备注信息TEXT");

        //产品信息 托管信息
        mapProdComInfo.put("product_custodian_registration_date", date2);
        mapProdComInfo.put("product_cusodian_documents", getListFileObj());
        mapProdComInfo.put("product_custodian_notes", "托管备注信息TEXT");
        mapProdComInfo.put("product_escrow_deregistration_date", date4);
        mapProdComInfo.put("product_escrow_deregistration_document", getListFileObj());
        mapProdComInfo.put("product_escrow_deregistration_remarks", "解除托管备注信息TEXT");


        return mapProdComInfo;
    }

    public Map init03EquityProductInfo() {
        Map mapTemp = new HashMap();
        log.info("初始化03产品数据结构");
        mapTemp.clear();

        mapTemp = productCommonInfo(0); //私募股权

        //产品信息 发行信息 备案信息
        List<Map> listMapPR = new ArrayList<>();
        Map mapPR = new HashMap();

        mapPR.put("product_license_type", 122);
        mapPR.put("product_filing_date", date1);
        mapPR.put("product_filing_doc", getListFileObj());
        mapPR.put("product_filing_examine_doc", getListFileObj());
        mapPR.put("product_filing_documentation", "psf.pdf");
        listMapPR.add(mapPR);
        mapTemp.put("filing_information", listMapPR);

        //产品信息 发行信息 私募股权
        mapTemp.put("product_issue_scope", 12);
        mapTemp.put("product_issue_type", 3);
        List<Integer> iClass = new ArrayList<>();
        iClass.add(1);
        iClass.add(2);
        mapTemp.put("product_shares_issued_class", iClass);
        mapTemp.put("release_note_information", "发行说明信息TEXT");
        mapTemp.put("product_issue_price", 20);
        mapTemp.put("product_issue_price_method", "发行价格定价标准TEXT");
        mapTemp.put("product_before_authorized_shares", 6000);
        mapTemp.put("product_after_authorized_shares", 10000);
        mapTemp.put("product_after_issue_market_value", 60000000);
        mapTemp.put("product_net_profit", 30000000);
        mapTemp.put("product_annual_net_profit", 60000000);
        mapTemp.put("product_actual_raising_scale", 300000);
        mapTemp.put("product_raising_start_time", date1);
        mapTemp.put("product_raising_end_time", date1);
        mapTemp.put("product_registered_capital_before_issuance", 3000000);
        mapTemp.put("product_registered_capital_issuance", 5000000);
        mapTemp.put("product_paid_shares", 6000000);
        mapTemp.put("product_shares_subscribed_number", 6000000);
        mapTemp.put("product_unlimited_sales_number_shares", 100000);
        mapTemp.put("product_restricted_shares_number", 500000);

        //交易信息 托管信息
        productCommonInfo2(mapTemp);
        return mapTemp;
    }

    //私募可转债
    public Map init03BondProductInfo() {
        Map mapTemp = new HashMap();
        log.info("初始化03产品数据结构");
        mapTemp.clear();

        mapTemp = productCommonInfo(0); //私募股权

        //产品信息 发行信息 备案信息
        List<Map> listMapPR = new ArrayList<>();
        Map mapPR = new HashMap();

        mapPR.put("product_license_type", 122);
        mapPR.put("product_filing_date", date2);
        mapPR.put("product_filing_doc", getListFileObj());
        mapPR.put("product_filing_examine_doc", getListFileObj());
        mapPR.put("product_filing_documentation", "psf.pdf");
        listMapPR.add(mapPR);
        mapTemp.put("filing_information", listMapPR);


        //产品信息 发行信息 私募债
        mapTemp.put("product_scope_issue", 9);
        mapTemp.put("product_bond_duration_unit", 15);
        mapTemp.put("product_bond_duration", 20);
        mapTemp.put("product_filing_amount", 200000);
        mapTemp.put("product_by_stages", true);
        mapTemp.put("product_bond_interest_rate_floor", 0.16);
        mapTemp.put("product_bond_interest_rate_cap", 56.23);
        mapTemp.put("product_staging_frequency", 3);
        mapTemp.put("product_initial_issue_amount", 10000);
        mapTemp.put("product_initial_ratio", 1.3);
        mapTemp.put("product_initial_interest_rate", 2.3);
        mapTemp.put("product_interest_calculation_method", 2);
        mapTemp.put("product_interest_calculation_method_remarks", "计息方式备注CHARACTER");
        mapTemp.put("product_payment_method", 1);
        mapTemp.put("product_payment_method_remarks", "兑付方式备注CHARACTER");
        mapTemp.put("product_is_appoint_repayment_date", true);
        mapTemp.put("product_appoint_repayment_date", date2);
        mapTemp.put("product_guarantee_measure", "担保措施及方式TEXT");
        mapTemp.put("product_converting_shares_condition", "转股条件TEXT");
        mapTemp.put("product_converting_shares_price_mode", "转股价格的确定方式TEXT");
        mapTemp.put("product_converting_shares_term", "转股期限TEXT");
        mapTemp.put("product_redemption", "赎回条款TEXT");
        mapTemp.put("product_issue_price", 20);
        mapTemp.put("product_face_value", 200);
        mapTemp.put("product_subscription_base", 20);
        mapTemp.put("product_successful_release_proportion", 2.5);
        mapTemp.put("product_fund_raising_conversion_condition", "募集资金划转条件TEXT");
        mapTemp.put("product_is_make_over", true);
        mapTemp.put("product_number_of_holders_max", 5000);
        mapTemp.put("product_subscription_upper_limit", 5000);
        mapTemp.put("product_subscription_lower_limit", 10);
        mapTemp.put("product_redemption_clause", "赎回及回售条款TEXT");
        mapTemp.put("product_termination_conditions", "产品终止条件CHARACTER");
        mapTemp.put("product_duration", "存续期限CHARACTER");
        mapTemp.put("product_adjustment_change_control", "控制权变更调整CHARACTER");
        mapTemp.put("product_conversion_premium", 10);
        mapTemp.put("product_conversion_price_ref", 15);
        mapTemp.put("product_actual_issue_size", 2000);
        mapTemp.put("product_raising_start_date", date2);
        mapTemp.put("product_raising_end_date", date4);
        mapTemp.put("product_start_date", date4);
        mapTemp.put("product_due_date", date4);
        mapTemp.put("product_amount_cashed", 5000);
        mapTemp.put("product_first_interest_payment_date", date4);
        mapTemp.put("product_issuer_credit_rating", 0);
        mapTemp.put("product_credit_enhancement_agency_credit_rating", 1);
        mapTemp.put("product_guarantee_arrangement", "担保安排CHARACTER");
        mapTemp.put("product_repo_arrangement", "回售安排CHARACTER");
        mapTemp.put("product_lockup", "股东禁售期限CHARACTER");


        //交易信息 托管信息
        productCommonInfo2(mapTemp);
        return mapTemp;
    }

    public Map init03FundProductInfo() {
        Map mapTemp = new HashMap();
        log.info("初始化03产品数据结构");
        mapTemp.clear();

        mapTemp = productCommonInfo(0); //私募股权


        //产品信息 基本信息 备案信息
        List<Map> listMapPR = new ArrayList<>();
        Map mapPR = new HashMap();

        mapPR.put("product_license_type", 122);
        mapPR.put("product_filing_date", date2);

        Map mapDoc = new HashMap();
        mapPR.put("product_filing_doc", getListFileObj());
        List<Map> listPFI = new ArrayList<>();
        listPFI.add(mapDoc);
        mapPR.put("product_file_information", getListFileObj());
        mapPR.put("product_filing_examine_doc", listPFI);
        mapPR.put("product_filing_documentation", "psf.pdf");
        listMapPR.add(mapPR);
        mapTemp.put("filing_information", listMapPR);


        //产品信息 发行信息 私募基金
        mapTemp.put("product_raising_information_identification", "募集信息标识CHARACTER");
        mapTemp.put("product_scope_fund_raising", "募集范围TEXT");
        mapTemp.put("product_record_number", "备案编号CHARACTER");
        mapTemp.put("product_fund_filing_date", date2);
        List<Integer> ftype = new ArrayList<>();
        ftype.add(1);
        ftype.add(3);
        mapTemp.put("product_fund_type", ftype);
        mapTemp.put("product_foundation_date", date2);
        mapTemp.put("product_escrow_bank", "托管行CHARACTER");
        mapTemp.put("product_total_fund_share", 50000);
        mapTemp.put("product_fund_unit_holders_number", "基金份额持有人数CHARACTER");
        mapTemp.put("product_fund_nav", 1000000);
        mapTemp.put("product_fund_fairvalue", 1000000);
        mapTemp.put("product_raise_start_date", date2);
        mapTemp.put("product_raise_end_date", date3);
        mapTemp.put("sales_organization_name", "销售机构名称CHARACTER");
        mapTemp.put("product_unified_social_credit_code", "统一社会信用代码CHARACTER");
        mapTemp.put("product_sales_organization_member_code", "销售机构会员编码CHARACTER");
        mapTemp.put("product_fund_manager_name", "基金管理人名称CHARACTER");
        mapTemp.put("product_fund_manager_certificate_number", "基金管理人证件号码CHARACTER");
        mapTemp.put("product_management_style", "管理方式TEXT");
        mapTemp.put("product_funds_under_management_number", 2000);
        mapTemp.put("product_fund_management_scale", 50000);


        //交易信息 托管信息
        productCommonInfo2(mapTemp);
        return mapTemp;
    }


    public Map init04TxInfo() {
        Map mapTemp = new HashMap();
        log.info("初始化04交易数据结构");
        //04交易报告
        //对象信息
        mapTemp.put("transaction_object_id", "txoid00001");
//        mapTemp.put("transaction_object_information_type",0);

        //交易报告信息 交易基本信息
        mapTemp.put("transaction_market_type", 0);
        mapTemp.put("transaction_type", 1);
        mapTemp.put("transaction_method", 2);
        mapTemp.put("transaction_description", "交易描述信息300字以内描述");
        mapTemp.put("transaction_create_time", time3);

        //交易报告信息 交易资产信息
        mapTemp.put("transaction_product_custody_status", 0);
        mapTemp.put("transaction_custody_product_ref", "交易产品引用CHARACTER");
        mapTemp.put("transaction_product_name", "交易资产名称CHARACTER");
        mapTemp.put("transaction_product_issuer_ref", "交易资产发行主体引用CHARACTER");
        mapTemp.put("transaction_product_issuer_name", "交易资产发行主体名称CHARACTER");
        mapTemp.put("transaction_product_asset_type", 0);
        mapTemp.put("transaction_product_asset_unit", 1);
        mapTemp.put("transaction_product_asset_currency", "交易资产规模币种CHARACTER");
        mapTemp.put("transaction_product_asset_value", 10000);
        mapTemp.put("transaction_product_asset_doc", getListFileObj());
        mapTemp.put("transaction_product_decription", "交易资产描述信息CHARACTER");


        //交易报告信息 交易成交信息 成交内容信息
        mapTemp.put("transaction_series_number", "交易成交流水号CHARACTER");
        mapTemp.put("transaction_settlement_currency", "成交币种CHARACTER");
        mapTemp.put("transaction_settlement_price", 20);
        mapTemp.put("transaction_settlement_quantity", 1000);
        mapTemp.put("transaction_settlement_time", time3);
        mapTemp.put("transaction_settlement_description", "交易成交描述信息CHARACTER");


        //交易报告信息 交易成交信息 融资类交易成交方信息
        mapTemp.put("transaction_issuer_ref", gdCompanyID);
        mapTemp.put("transaction_issuer_name", "issue001");
        mapTemp.put("transaction_investor_ref", "accobj0001");
        mapTemp.put("transaction_investor_name", "联合");

        //交易报告信息 交易成交信息 交易成交方信息
        mapTemp.put("transaction_investor_original_ref", "acchobj001");
        mapTemp.put("transaction_investor_original_name", "zhagnsan");
        mapTemp.put("transaction_investor_counterparty_ref", "acchobj002");
        mapTemp.put("transaction_investor_counterparty_name", "李四");

        //交易报告信息 交易成交信息 成交核验信息
        mapTemp.put("transaction_orderplacing_verification_doc", getListFileObj());
        mapTemp.put("transaction_verification_doc", getListFileObj());

        //交易报告信息 交易中介信息
        List<Map> mapList1 = new ArrayList<>();
        Map equityMap = new HashMap();

        equityMap.put("transaction_intermediary_type", 0);
        equityMap.put("transaction_intermediary_subject_ref", "obj001");
        equityMap.put("transaction_intermediary_description", "中介机构服务描述200字以内描述");
        mapTemp.put("transaction_intermediary_name", "中介机构名称");

        return mapTemp;
    }

    public Map init05RegInfo() {
        Map mapTemp = new HashMap();
        log.info("初始化05登记数据结构");
        //05登记
        List<String> listRegFile = new ArrayList<>();
        listRegFile.add("verify.crt");
        mapTemp.clear();
        //对象标识
        mapTemp.put("register_registration_object_id", "regid00001");
//        mapTemp.put("register_object_information_type",0);
//        mapTemp.put("register_registration_type",0);

        //登记信息 登记基本信息
        mapTemp.put("register_object_type", 0);
        mapTemp.put("register_event_type", 1);

        //登记信息 权利信息 权利基本信息 权利基本信息描述
        mapTemp.put("register_serial_number", "登记流水号CHARACTER");
        mapTemp.put("register_time", time2);
        mapTemp.put("register_subject_ref", "登记主体引用CHARACTER");
        mapTemp.put("register_subject_type", 1);
        mapTemp.put("register_subject_account_reference", "登记账户引用CHARACTER");
        mapTemp.put("register_asset_type", 1);
        mapTemp.put("register_asset_unit", 1);
        mapTemp.put("register_asset_currency", "登记币种CHARACTER");
        mapTemp.put("register_transaction_ref", 1);
        mapTemp.put("register_product_ref", "登记产品引用");
        mapTemp.put("register_description", "登记描述信息CHARACTER");
        mapTemp.put("register_create_time", time2);
        mapTemp.put("register_authentic_right_recognition_status", 1);

        //登记信息 权利登记 权利基本信息 确权记录
        mapTemp.put("register_authentic_right_recognition_date", date3);
        mapTemp.put("register_right_recognition_mode", 1);
        mapTemp.put("register_right_recognition_subject_ref", "确权方主体引用CHARACTER");
        mapTemp.put("register_right_recognition_subject_name", "确权方主体名称CHARACTER");
        mapTemp.put("register_right_recognition_agent_subject_ref", "确权代理方主体引用CHARACTER");
        mapTemp.put("register_right_recognition_agent_subject_name", "确权代理方主体名称CHARACTER");
        mapTemp.put("register_right_recognition_doc", getListFileObj());
        mapTemp.put("register_right_recognition_description", "确权描述信息CHARACTER");

        //登记信息 权利登记 权利基本信息 可用登记
        mapTemp.put("register_asset_balance_change", 1000);
        mapTemp.put("register_asset_balance_before", 2000);
        mapTemp.put("register_asset_balance_after", 3000);

        //登记信息 权利登记 权利基本信息 质押登记
        mapTemp.put("register_pledge_balance_change", 1000);
        mapTemp.put("register_pledge_balance_before", 1000);
        mapTemp.put("register_plged_balance_after", 5000);

        //登记信息 权利登记 权利基本信息 冻结登记
        mapTemp.put("register_frozen_balance_change", 500);
        mapTemp.put("register_frozen_balance", 1000);
        mapTemp.put("register_frozen_balance_after", 5000);
        mapTemp.put("register_thaw_doc", getListFileObj());
        mapTemp.put("register_thaw_description", "冻结/解冻说明信息TEXT");

        //登记信息 权利登记 权利基本信息 状态信息描述
        mapTemp.put("register_asset_holding_status", 1);
        mapTemp.put("register_asset_holding_status_description", "持有状态说明TEXT");
        mapTemp.put("register_asset_holding_nature", 1);
        mapTemp.put("register_asset_equity_type", 1);
        mapTemp.put("register_source_type", 0);
        mapTemp.put("register_asset_note", "登记说明TEXT");
        mapTemp.put("register_asset_verrification_doc", getListFileObj());


        //登记信息 名册登记 名册基本信息
        mapTemp.put("register_list_subject_ref", "名册主体引用CHARACTER");
        mapTemp.put("register_product_ref", "产品引用");
        mapTemp.put("register_product_name", "产品名称");
        mapTemp.put("register_product_description", "产品描述");
        mapTemp.put("register_list_asset_type", 1);
        mapTemp.put("register_list_date", date3);


        //登记信息 名册登记 股东名册
        List<Map> listRSH = new ArrayList<>();
        Map mapRSH = new HashMap();
        mapRSH.put("register_equity_subject_ref", "股东主体引用CHARACTER");
        mapRSH.put("register_equity_subject_type", 1);
        mapRSH.put("register_equity_type", 1);
        mapRSH.put("register_equity_capital", 200);
        mapRSH.put("register_equity_capital_paidin", 30000);
        mapRSH.put("register_equity_number", 20);
        mapRSH.put("register_equity_shareholding", 10);
        listRSH.add(mapRSH);
        mapTemp.put("register_shareholders", listRSH);

        //登记信息 名册登记 债权人名册
        List<Map> listRC = new ArrayList<>();
        Map mapRC = new HashMap();
        mapRC.put("register_debt_holder_ref", "债权人主体引用CHARACTER");
        mapRC.put("register_debt_holder_type", 1);
        mapRC.put("register_debt_holder_subscription_quantity", 1000);
        mapRC.put("register_debt_holder_subscription_price", 10000);
        mapRC.put("register_debt_holder_contact_number", "债权人联系方式CHARACTER");

        listRC.add(mapRC);
        mapTemp.put("register_creditors", listRC);

        //登记信息 名册登记 基金投资人名册
        List<Map> listFi = new ArrayList<>();
        Map mapFI = new HashMap();
        mapFI.put("register_investor_subject_citation", "投资人主体引用CHARACTER");
        mapFI.put("register_investor_name", "投资人主体名称CHARACTER");
        mapFI.put("register_subscription_amount", 1000);
        mapFI.put("register_subscription_number", 2000);
        mapFI.put("register_fund_investors_classification", 1);

        listFi.add(mapFI);
        mapTemp.put("fund_investors", listFi);

        return mapTemp;
    }

//    public Map init06SettleInfo() {
//        Map mapTemp = new HashMap();
//        log.info("初始化06资金清算数据结构");
//        mapTemp.clear();
//        List<String> listCert = new ArrayList<>();
//        listCert.add("tix.pdf");
//        listCert.add("tix2.pdf");
//
//        //对象信息
//        mapTemp.put("capita_settlement_object_id", "CHAOyf0iKl68R3");
//        mapTemp.put("capita_object_information_type", 0);
//
//        //资金结算基本信息
//        mapTemp.put("capita_clearing_house_subject_ref", "CH5LDWxGtu6142");
//        mapTemp.put("capita_settlement_type", 0);
//        mapTemp.put("capita_settlement_serial_num", "CH1ktvx01x2e04");
//        mapTemp.put("capita_settlement_time", "2020/11/06 14:14:59");
//        mapTemp.put("capita_transaction_ref", "CHVXgXA1JsB5y2");
//        mapTemp.put("capita_currency", "CHW5HHZl0gGgnj");
//        mapTemp.put("capita_amount", 1000000);
//        mapTemp.put("capita_notes", "textemu4AW4U57");
//        mapTemp.put("capita_operation_certificates", listCert);
//
//        //转出方信息
//        mapTemp.put("capita_out_bank_code", "CH3e9Vp967INOA");
//        mapTemp.put("capita_out_bank_name", "CH1tY37wB57auz");
//        mapTemp.put("capita_out_bank_number", "CHHe8e3M4j0N8o");
//        mapTemp.put("capita_out_account_obj_ref", "CH62QCHF5Q7sob");
//        mapTemp.put("capita_out_fund_account_name", "CH8tC5aG5om158");
//        mapTemp.put("capita_out_amount_before_transfer", 1000000);
//        mapTemp.put("capita_out_amount_after_transfer", 1000000);
//
//        //转入方信息
//        mapTemp.put("capita_in_bank_code", "CH7YfKps3x65Y2");
//        mapTemp.put("capita_in_bank_name", "CHp42HpuGtf6y3");
//        mapTemp.put("capita_in_bank_number", "CHE0H230A17lu8");
//        mapTemp.put("capita_in_account_obj_ref", "CH8QL30e0ggRvp");
//        mapTemp.put("capita_in_fund_account_name", "CH6FU5bf2N2Y6B");
//        mapTemp.put("capita_in_account_number", "CHd8re1Dojtmx0");
//        mapTemp.put("capita_in_amount_before_transfer", 1000000);
//        mapTemp.put("capita_in_amount_after_transfer", 1000000);
//
//        return mapTemp;
//    }

    public Map init06SettleInfo() {
        Map mapTemp = new HashMap();
        log.info("初始化06资金清算数据结构");
        mapTemp.clear();

        //资金结算信息 资金结算基本信息
        mapTemp.put("settlement_subject_ref", "CH5LDWxGtu6142");
        mapTemp.put("settlement_type", 0);
        mapTemp.put("settlement_serial_number", "CH1ktvx01x2e04");
        mapTemp.put("settlement_time", time1);
        mapTemp.put("settlement_product_ref", "CHVXgXA1JsB5y2");
        mapTemp.put("settlement_transaction_reference", "CHW5HHZl0gGgnj");
        mapTemp.put("settlement_currency", "156");
        mapTemp.put("settlement_value", 600000);
        mapTemp.put("settlement_note", "textemu4AW4U57");
        mapTemp.put("settlement_operation_doc", getListFileObj());
        mapTemp.put("settlement_information_maintenance_time", time1);

        //资金结算信息 转出方信息
        mapTemp.put("settlement_out_bank_code", "CH3e9Vp967INOA");
        mapTemp.put("settlement_out_bank_name", "CH1tY37wB57auz");
        mapTemp.put("settlement_out_bank_account", "CHHe8e3M4j0N8o");
        mapTemp.put("settlement_out_account_object_ref", "CH62QCHF5Q7sob");
        mapTemp.put("settlement_out_account_name", "CH8tC5aG5om158");
        mapTemp.put("settlement_out_account_balance_before_transfer", 1000000);
        mapTemp.put("settlement_out_account_balance_after_transfer", 1000000);

        //资金结算信息 转入方信息
        mapTemp.put("settlement_in_bank_code", "CH7YfKps3x65Y2");
        mapTemp.put("settlement_in_bank_name", "CHp42HpuGtf6y3");
        mapTemp.put("settlement_in_bank_account", "CHE0H230A17lu8");
        mapTemp.put("settlement_in_account_object_ref", "CH8QL30e0ggRvp");
        mapTemp.put("settlement_in_account_name", "CH6FU5bf2N2Y6B");
        mapTemp.put("settlement_in_account_balance_before_transfer", 1000000);
        mapTemp.put("settlement_in_account_balance_after_transfer", 1000000);

        return mapTemp;
    }

//    public Map init07PublishInfo() {
//        Map mapTemp = new HashMap();
//        log.info("初始化07信披数据结构");
//
//        //07信披监管数据
//        List<String> listPubFile = new ArrayList<>();
//        listPubFile.add("tix.pdf");
//
//        //诚信档案
//        List<Map> listCredit = new ArrayList<>();
//        Map mapCd = new HashMap();
//
//        mapTemp.clear();
//
//
//        //对象标识
//        mapTemp.put("letter_disclosure_object_id", "CHo8M7Jzo6y6Uh");
//        mapTemp.put("letter_object_information_type", 0);
//
//        //企业展示信息
//        mapTemp.put("letter_show_subject_reference", "IDSR006");
//        mapTemp.put("letter_show_subject_reference_ref", "IDSR007");
//        mapTemp.put("letter_display_code", "C1106");
//        mapTemp.put("letter_display_content", "xinpi");
//        mapTemp.put("letter_display_main_audit_voucher", "mainpingzhen.pdf"); //20201106 此处监管数据为空格 与开发确认修改为下划线
//        mapTemp.put("letter_show_content_audit_voucher", "contentpingzhen.pdf");
//        mapTemp.put("letter_show_start_date", "2020/11/06");
//        mapTemp.put("letter_show_end_date", "2020/11/06");
//
//        //信批基本信息
//        mapTemp.put("letter_disclosure_subject_ref", "CH58dk5280yB0A");
//        mapTemp.put("letter_disclosure_uploader_ref", "CH58dk5280yB0Aref");
//        mapTemp.put("letter_approval_time", "2020/11/06 14:15:00");
//
//
//        //诚信档案
//        //事项基本信息
//        mapCd.put("letter_provider_subject_ref", "CHl55WqGnO3hRd");
//        mapCd.put("letter_provider_name", "CH60FNHO699i3S");
//        mapCd.put("letter_identified_party_subject_ref", "CHAVko0Oq9139x");
//        mapCd.put("letter_identified_party_name", "CHccO9f2A3gQuK");
//        mapCd.put("letter_appraiser_subject_ref", "CH4Ntw87gm4H3m");
//        mapCd.put("letter_appraiser_name", "CHc112rQVG0I5U");
//        //事项明细
//        mapCd.put("letter_item_number", "letter00000001");
//        mapCd.put("letter_item_name", "lettername");
//        mapCd.put("letter_item_type", 0);
//        mapCd.put("letter_item_describe", "因非法开设证券期货交易场所或者组织证券期货交易被地方政府行政处罚或者采取清理整顿措施");
//        mapCd.put("letter_term_of_validity", 1);
//        mapCd.put("letter_start_time", "2020/10/29");
//        mapCd.put("letter_end_time", "2022/10/29");
//        mapCd.put("letter_item_state", 1);
//        mapCd.put("letter_item_file", listPubFile);
//        listCredit.add(mapCd);
//        mapTemp.put("integrity_archives", listCredit);
//
//        //基本财务信息
//        mapTemp.put("letter_start_date", "2020/11/06");
//        mapTemp.put("letter_deadline", "2020/11/06");
//        mapTemp.put("letter_report_type", 0);
//        mapTemp.put("letter_ending_total_asset", 1000000);
//        mapTemp.put("letter_ending_net_asset", 1000000);
//        mapTemp.put("letter_total_liability", 1000000);
//        mapTemp.put("letter_current_operating_income", 1000000);
//        mapTemp.put("letter_current_total_profit", 1000000);
//        mapTemp.put("letter_current_net_profit", 1000000);
//        mapTemp.put("letter_cash_flow", 1000000);
//        mapTemp.put("letter_whether_r&d_costs", 1000000);
//        mapTemp.put("letter_r&d_costs", 1000000);
//        //财务报表信息
//        mapTemp.put("letter_balance_sheet", "829.pdf");
//        mapTemp.put("letter_cash_flow_sheet", "53d.pdf");
//        mapTemp.put("letter_profit_sheet", "6L6.pdf");
//
//        //重大事件列表
//        List listME = new ArrayList();
//        Map majorEvent = new HashMap();
//        majorEvent.put("letter_major_event_type", 0);
//        majorEvent.put("letter_file_list", "C63.pdf");
//        majorEvent.put("letter_description_document", "7P6.pdf");
//        majorEvent.put("letter_submission_time", "2020/11/06 14:15:00");
//
//        listME.add(majorEvent);
//        mapTemp.put("major_event_information", listME);
//
//        //公告
//        List listNotice = new ArrayList();
//        Map notice = new HashMap();
//        notice.put("letter_announcement_type", 0);
//        notice.put("letter_file_list", "OG3.pdf");
//        notice.put("letter_description_announcement", "535.pdf");
//        notice.put("letter_announcement_time", "2020/11/06 14:15:00");
//
//        listNotice.add(notice);
//        mapTemp.put("letter_notice", listNotice);
//
//        return mapTemp;
//    }

    public Map init07PublishInfo() {
        Map mapTemp = new HashMap();
        log.info("初始化07信披数据结构");
        mapTemp.clear();

        //信披信息 信披基本信息
        mapTemp.put("disclosure_subject_ref", "CHo8M7Jzo6y6Uh");
        mapTemp.put("disclosure_type", 0);
        mapTemp.put("disclosure_submit_type", 0);
        mapTemp.put("disclosure_referer_subject_ref", "CHo8M7Jzo6y6Uh");
        mapTemp.put("disclosure_referer_name", "CHo8M7Jzo6y6Uh");
        mapTemp.put("disclosure_submit_date", date1);
        mapTemp.put("disclosure_submit_description", "CHo8M7Jzo6y6Uh");

        //信披信息 企业展示信息
        List<Map> listEnterpriseInformation = new ArrayList<>();
        Map mapEId = new HashMap();
        mapEId.put("disclosure_display_platform_ref", "IDSR006");
        mapEId.put("disclosure_display_name", "IDSR007");
        mapEId.put("disclosure_display_doc", getListFileObj());
        mapEId.put("disclosure_display_description", "xinpi");
        mapEId.put("disclosure_display_code", "mainpingzhen.pdf");
        mapEId.put("disclosure_display_content", "contentpingzhen.pdf");
        mapEId.put("disclosure_display_start_date", date1);
        mapEId.put("disclosure_display_end_date", date2);
        listEnterpriseInformation.add(mapEId);
        mapTemp.put("enterprise_display_information",listEnterpriseInformation);

        //信披信息 监管信息
        List<Map> listRegulatoryInfomation = new ArrayList<>();
        Map mapRId = new HashMap();
        mapRId.put("disclosure_regulatory_type",1);
        mapRId.put("disclosure_regulatory_report_name", "IDSR007");
        mapRId.put("disclosure_regulatory_report_doc", getListFileObj());
        mapRId.put("disclosure_regulatory_report_description", "xinpi");
        mapRId.put("disclosure_regulatory_measures", "mainpingzhen.pdf");
        mapRId.put("disclosure_regulatory_report_date", date1);
        listRegulatoryInfomation.add(mapRId);
        mapTemp.put("regulatory_infomation",listRegulatoryInfomation);

        //信披信息 企业报告
        List<Map> listEnterpriseReport = new ArrayList<>();
        Map mapERd = new HashMap();
        mapERd.put("disclosure_enterprise_report_type",1);
        mapERd.put("disclosure_enterprise_report_name", "IDSR007");
        mapERd.put("disclosure_enterprise_report_doc", getListFileObj());
        mapERd.put("disclosure_enterprise_report_description", "xinpi");
        mapERd.put("disclosure_enterprise_report_date", date1);
        listEnterpriseReport.add(mapERd);
        mapTemp.put("enterprise_report",listEnterpriseReport);

        //信披信息 公告
        List listNotice = new ArrayList();
        Map notice = new HashMap();
        notice.put("disclosure_announcement_type", 0);
        notice.put("disclosure_announcement_name", "OG3.pdf");
        notice.put("disclosure_announcement_doc", getListFileObj());
        notice.put("disclosure_announcement_description", "公告简述0001");
        notice.put("disclosure_announcement_date", date1);
        listNotice.add(notice);
        mapTemp.put("disclosure_notice", listNotice);

        //信披信息 重大事件信息
        List listME = new ArrayList();
        Map majorEvent = new HashMap();
        majorEvent.put("disclosure_major_event_type", 0);
        majorEvent.put("disclosure_major_event_name", "C63.pdf");
        majorEvent.put("disclosure_major_event_doc", getListFileObj());
        majorEvent.put("disclosure_major_event_document_description", "事件简述0001");
        majorEvent.put("disclosure_major_event_reporting_date", date1);
        listME.add(majorEvent);
        mapTemp.put("major_event_information", listME);


        //信披信息 诚信档案
        //事项基本信息
        List<Map> listCredit = new ArrayList<>();
        Map mapCd = new HashMap();
        mapCd.put("disclosure_identifier_ref", "CHl55WqGnO3hRd");
        mapCd.put("disclosure_identifier_name", "CH60FNHO699i3S");
        mapCd.put("disclosure_auditor_ref", "CHAVko0Oq9139x");
        mapCd.put("disclosure_auditor_name", "CHccO9f2A3gQuK");
        //事项明细
        mapCd.put("disclosure_event_id", "letter00000001");
        mapCd.put("disclosure_event_name", "lettername");
        mapCd.put("disclosure_event_doc", getListFileObj());
        mapCd.put("disclosure_event_description", "因非法开设证券期货交易场所或者组织证券期货交易被地方政府行政处罚或者采取清理整顿措施");
        mapCd.put("disclosure_event_type", 1);
        mapCd.put("disclosure_event_valid_time", "期限00001");
        mapCd.put("disclosure_event_start_date", date1);
        mapCd.put("disclosure_event_end_date", date2);
        mapCd.put("disclosure_event_status", 1);
        listCredit.add(mapCd);
        mapTemp.put("integrity_archives", listCredit);

        //信披信息 财务信息
        List<Map> listFinancialInformation = new ArrayList<>();
        Map mapFId = new HashMap();
        //基本财务信息
        mapFId.put("disclosure_financial_start_date", date1);
        mapFId.put("disclosure_financial_end_date", date2);
        mapFId.put("disclosure_financial_type", 0);
        mapFId.put("disclosure_financial_periodend_total_asset", 1000000);
        mapFId.put("disclosure_financial_periodend_net_asset", 1000000);
        mapFId.put("disclosure_financial_periodend_total_liability", 1000000);
        mapFId.put("disclosure_financial_periodend_revenue", 1000000);
        mapFId.put("disclosure_financial_periodend_gross_profit", 1000000);
        mapFId.put("disclosure_financial_periodend_net_profit", 1000000);
        mapFId.put("disclosure_financial_cashflow", 1000000);
        mapFId.put("disclosure_financial_whether_rd", true);
        mapFId.put("letter_r&disclosure_financial_rd_cost", 1000000);
        //财务报表文件
        mapFId.put("disclosure_financial_balance_sheet_name", "829.pdf");
        mapFId.put("disclosure_financial_balance_sheet", getListFileObj());
        mapFId.put("disclosure_financial_balance_sheet_description", "6L6.pdf");
        mapFId.put("disclosure_financial_cashflow_statement_name", "6L6.pdf");
        mapFId.put("disclosure_financial_cashflow_statement", getListFileObj());
        mapFId.put("disclosure_financial_cashflow_statement_description", "6L6.pdf");
        mapFId.put("disclosure_financial_income_statement_name", "6L6.pdf");
        mapFId.put("disclosure_financial_income_statement", getListFileObj());
        mapFId.put("disclosure_financial_income_statement_description", "6L6.pdf");
        listFinancialInformation.add(mapFId);
        mapTemp.put("financial_information", listFinancialInformation);

        //信披信息 企业经营信息
        List listBusinessInformation = new ArrayList();
        Map mapBID = new HashMap();
        //经营基本信息
        mapBID.put("disclosure_business_overview", "概述0001");
        mapBID.put("disclosure_main_business_analysis", "C63.pdf");
        mapBID.put("disclosure_main_business_analysis_non", "7P6.pdf");
        mapBID.put("disclosure_assets_and_liabilities_analysis", "2020/11/06 14:15:00");
        mapBID.put("disclosure_major_shareholders_analysis", 0);
        mapBID.put("disclosure_major_events", "C63.pdf");
        mapBID.put("disclosure_business_report_name", "7P6.pdf");
        mapBID.put("disclosure_business_report_doc", getListFileObj());
        mapBID.put("disclosure_business_report_description", 0);
        mapBID.put("disclosure_business_report_date", date1);
        //投融资信息
        mapBID.put("disclosure_investment_analysis", "7P6.pdf");
        mapBID.put("disclosure_fund_raising_analysis", "2020/11/06 14:15:00");
        mapBID.put("disclosure_sell_situation_analysis","情况00001");
        listBusinessInformation.add(mapBID);
        mapTemp.put("business_information", listBusinessInformation);

        //信披信息 第三方拓展信息
        List listExpandInformation = new ArrayList();
        Map mapEID = new HashMap();
        mapBID.put("disclosure_expand_name", "概述0001");
        mapBID.put("disclosure_expand_doc", getListFileObj());
        mapBID.put("disclosure_expand_description", "7P6.pdf");
        mapBID.put("disclosure_expand_date", date1);
        listExpandInformation.add(mapEID);
        mapTemp.put("expand_information", listExpandInformation);


        return mapTemp;
    }

}
