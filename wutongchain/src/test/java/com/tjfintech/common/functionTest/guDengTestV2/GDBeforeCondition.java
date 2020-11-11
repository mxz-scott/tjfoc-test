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
import static org.junit.Assert.assertEquals;


@Slf4j

public class GDBeforeCondition {
    TestBuilder testBuilder = TestBuilder.getInstance();
    GuDeng gd = testBuilder.getGuDeng();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    Store store = testBuilder.getStore();
    


    @Test
    public void gdCreateAccout()throws Exception{
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

        commonFunc.sdkCheckTxOrSleep(txId10,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //判断所有开户接口交易上链
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId1)).getString("state"));
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId2)).getString("state"));
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId3)).getString("state"));
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId4)).getString("state"));
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId5)).getString("state"));
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId6)).getString("state"));
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId7)).getString("state"));
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId8)).getString("state"));
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId9)).getString("state"));
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId10)).getString("state"));


    }

    public Map<String,String> gdCreateAccParam(String clientNo){
        String cltNo = clientNo;
        String shareHolderNo = "SH" + cltNo;
        String fundNo = "fund" + cltNo;

        //构造股权账户信息
        Map shareHolderInfo = new HashMap();
        init02EquityAccountInfo();
        equityaccountInfo.put("account_object_id",cltNo);  //更新账户对象标识字段
        log.info(equityaccountInfo.toString());
        shareHolderInfo.put("shareholderNo",shareHolderNo);
        shareHolderInfo.put("accountInfo", equityaccountInfo);
        log.info(shareHolderInfo.toString());

        //资金账户信息
        init02FundAccountInfo();
        fundaccountInfo.put("account_object_id",cltNo);  //更新账户对象标识字段
        Map mapFundInfo = new HashMap();
        mapFundInfo.put("fundNo",fundNo);
        mapFundInfo.put("accountInfo",fundaccountInfo);

        //构造个人/投资者主体信息
        init01PersonalSubjectInfo();
        investorSubjectInfo.put("letter_object_identification",cltNo);  //更新对象标识字段
        investorSubjectInfo.put("subject_id","sid" + cltNo);  //更新主体标识字段

        String response = gd.GDCreateAccout(gdContractAddress,cltNo,mapFundInfo,shareHolderInfo, investorSubjectInfo);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        assertEquals(cltNo,JSONObject.fromObject(response).getJSONObject("data").getJSONObject("accountList").getString("clientNo"));
        assertEquals(shareHolderNo,JSONObject.fromObject(response).getJSONObject("data").getJSONObject("accountList").getString("shareholderNo"));
        String keyID = JSONObject.fromObject(response).getJSONObject("data").getJSONObject("accountList").getString("keyId");
        String addr= JSONObject.fromObject(response).getJSONObject("data").getJSONObject("accountList").getString("address");

        Map mapAccInfo = new HashMap();
        mapAccInfo.put("keyID",keyID);
        mapAccInfo.put("accout",addr);
        mapAccInfo.put("txId",txId);
        mapAccInfo.put("response",response);

        mapAccAddr.put(addr,clientNo);

        return mapAccInfo;
    }

//    @Test
    public void initRegulationData() {
        //更新系统合约
        gd.GDEquitySystemInit(gdContractAddress,gdPlatfromKeyID);

        log.info("初始化监管相关数据结构");
        enterpriseSubjectInfo = init01EnterpriseSubjectInfo();      //初始化企业主体数据信息  涉及接口 企业挂牌登记
        investorSubjectInfo = init01PersonalSubjectInfo();        //初始化个人主体数据信息  涉及接口 开户
        equityaccountInfo = init02EquityAccountInfo();          //初始化账户数据信息 股权账户  涉及接口 开户
        fundaccountInfo = init02FundAccountInfo();            //初始化账户数据信息 资金账户  涉及接口 开户
        equityProductInfo = init03EquityProductInfo();          //初始化股权类产品数据信息  涉及接口 挂牌企业登记 股份增发 场内转板
        bondProductInfo = init03BondProductInfo();            //初始化债券类产品数据信息  涉及接口 挂牌企业登记 股份增发 场内转板
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


        List<Map> listQual = new ArrayList<>();
        Map qualification1 = new HashMap();
        Map qualification2 = new HashMap();
        listQual.add(qualification1);
        listQual.add(qualification2);

        qualification2.put("subject_qualification_category",0);
        qualification2.put("subject_market_roles_types",0);
        qualification2.put("subject_financial_qualification_types",0);
        qualification2.put("subject_role_qualification_number","CHpCT82aXlhaA3");
        qualification2.put("subject_role_qualification_certification_document",fileList);
        qualification2.put("subject_qualification_party_certification","CHbG59a6kufNo1");
        qualification2.put("subject_role_qualification_reviewer","CH8Ggm9JnTBIp5");
        qualification2.put("subject_certification_time","2020/11/05 14:14:59");
        qualification2.put("subject_audit_time","2020/11/05 14:14:59");

        qualification2.put("subject_qualification_category",0);
        qualification2.put("subject_market_roles_types",0);
        qualification2.put("subject_financial_qualification_types",0);
        qualification2.put("subject_role_qualification_number","CHpCT82aXlhaA3");
        qualification2.put("subject_role_qualification_certification_document",fileList);
        qualification2.put("subject_qualification_party_certification","CHbG59a6kufNo1");
        qualification2.put("subject_role_qualification_reviewer","CH8Ggm9JnTBIp5");
        qualification2.put("subject_certification_time","2020/11/06 14:14:59");
        qualification2.put("subject_audit_time","2020/11/06 14:14:59");


        mapTemp.put("subject_object_id",gdCompanyID);
        mapTemp.put("subject_object_information_type",0);
        mapTemp.put("subject_id",gdCompanyID + "sub");
        mapTemp.put("subject_industry_code","CHDcdIuA52fhLo");
        mapTemp.put("subject_type",0);
        mapTemp.put("subject_create_time","2020/11/06 14:14:59");

        mapTemp.put("subject_qualification_information",listQual);

        mapTemp.put("subject_organization_type",0);
        mapTemp.put("subject_organization_nature",0);
        mapTemp.put("subject_company_name","CHbne9QxJO40e1");
        mapTemp.put("subject_company_english_name","CHRTK405U5Mvde");
        mapTemp.put("subject_company_short_name","CH10V60bs23xrV");
        mapTemp.put("subject_company_short_english_name","CHMCp8U1af57p1");
        mapTemp.put("subject_company_type",0);
        mapTemp.put("subject_company_component",0);
        mapTemp.put("subject_unified_social_credit_code","CHMA2zB1DYH6n2");
        mapTemp.put("subject_organization_code","CH6B532hqn28G3");
        mapTemp.put("subject_establishment_day","2020/11/06");
        mapTemp.put("subject_business_license","AYg.pdf");
        mapTemp.put("subject_business_scope","CH0iZ3oTi0vO21");
        mapTemp.put("subject_industry",0);
        mapTemp.put("subject_company_business","CHFzt0hqd1Mxlq");
        mapTemp.put("subject_company_profile","textn257v7Om57");
        mapTemp.put("subject_registered_capital",500000);
        mapTemp.put("subject_registered_capital_currency",0);
        mapTemp.put("subject_paid_in_capital",500000);
        mapTemp.put("subject_paid_in_capital_currency",0);
        mapTemp.put("subject_registered_address","CH8FwoDbZ16lWw");
        mapTemp.put("subject_office_address","CHi7F2o8ATYX1y");
        mapTemp.put("subject_contact_address","CHUoq9J3T03hv5");
        mapTemp.put("subject_contact_number","CHKQjCN9edY163");
        mapTemp.put("subject_fax","CH7gjsG5STOJK0");
        mapTemp.put("subject_postal_code","CH6l11K3f1atI5");
        mapTemp.put("subject_internet_address","CHJ3vf23l57ynm");
        mapTemp.put("subject_mail_box","CHWV3Kh2S4jL8C");
        mapTemp.put("subject_association_articles","textTG5F4q9yQ1");
        mapTemp.put("subject_competent_unit","CHF3420egqe8IN");
        mapTemp.put("subject_shareholders_number",500000);
        mapTemp.put("subject_total_share_capital",500000);
        mapTemp.put("subject_actual_controller","CH2cnqo0H67567");
        mapTemp.put("subject_actual_controller_id_type","CH7QgaY0T0o7n0");
        mapTemp.put("subject_actual_controller_id","CHGly2JJ06590f");
        mapTemp.put("subject_legal_rep_name","CHNoDE64t45VVV");
        mapTemp.put("subject_legal_person_nature",0);
        mapTemp.put("subject_legal_rep_id_doc_type",0);
        mapTemp.put("subject_legal_rep_id_doc_number","CH97U3iwgZq1M2");
        mapTemp.put("subject_legal_rep_post",0);
        mapTemp.put("subject_legal_rep_cellphone_number","CHy1RxR5uo1j0d");

        return mapTemp;
    }

    public Map init01PersonalSubjectInfo() {
        Map mapTemp = new HashMap();
        log.info("初始化01主体个人数据结构");
        List<String> fileList1 = new ArrayList<>();
        fileList1.add("test1.pdf");
        fileList1.add("test2.pdf");
        List<Map> mapQuali = new ArrayList<>();
        Map qual = new HashMap();

        mapTemp.clear();
        String cltNo = "test00001";
        mapTemp.put("letter_object_identification",cltNo);
        mapTemp.put("subject_id",cltNo);
        mapTemp.put("subject_industry_code","JR");
        mapTemp.put("subject_type",0);
        mapTemp.put("subject_create_time","2020/09/12 12:01:12");

        qual.put("资质认证类型",0);
        qual.put("account_qualification_certification_file",fileList1);
        qual.put("account_certifier","苏州市监管局");
        qual.put("account_auditor","苏州市监管局");
        qual.put("account_certification_time","2020/09/12 12:01:12");
        qual.put("account_audit_time","2020/09/12 12:01:12");

        mapQuali.add(qual);
        mapTemp.put("subject_qualification_information",mapQuali);
        mapTemp.put("subject_investor_name","zhangsan");
        mapTemp.put("subject_id_doc_type",0);
        mapTemp.put("subject_id_doc_number","325689199512230001");
        mapTemp.put("subject_contact_address","相城");
        mapTemp.put("subject_investor_contact_number","15865487895");
        mapTemp.put("subject_cellphone_number","15865487895");
        mapTemp.put("subject_education",4);
        mapTemp.put("subject_industry",0);
        mapTemp.put("subject_birthday","1949/09/12");
        mapTemp.put("subject_gender",0);
        mapTemp.put("subject_rating_results","通过");
        mapTemp.put("subject_rating_time","2020/09/12 12:01:12");
        mapTemp.put("subject_rating_record","记录");

        return mapTemp;
    }

    public Map init02EquityAccountInfo() {
        Map mapTemp = new HashMap();
        log.info("初始化02账户数据结构");
        //默认股权账户
        List<String> fileList1 = new ArrayList<>();
        fileList1.add("test1.pdf");
        fileList1.add("test2.pdf");

        List<String> fileList2 = new ArrayList<>();
        fileList2.add("test21.pdf");
        fileList2.add("test22.pdf");
        List<String> fileList3 = new ArrayList<>();
        fileList3.add("test31.pdf");
        fileList3.add("test32.pdf");
        List<String> fileList4 = new ArrayList<>();
        fileList4.add("test41.pdf");
        fileList4.add("test42.pdf");
        List<String> fileList5 = new ArrayList<>();
        fileList5.add("test51.pdf");
        fileList5.add("test52.pdf");
        mapTemp.clear();
        mapTemp.put("account_object_id","testacc00001");
        mapTemp.put("account_holder_subject_ref","hrefid00001");
        mapTemp.put("account_depository_subject_ref","drefid00001");
        mapTemp.put("account_number","h0123555");
        mapTemp.put("account_type",0);  //默认股权账户
        mapTemp.put("account_never",0);
        mapTemp.put("account_status",0);
        mapTemp.put("account_opening_date","2012/6/25");
        mapTemp.put("account_opening_certificate",fileList4);
        mapTemp.put("account_closing_date","2022/6/25");
        mapTemp.put("account_closing_certificate",fileList2);
        mapTemp.put("account_forzen_date","2020/6/25");
        mapTemp.put("account_forzen_certificate",fileList3);
        mapTemp.put("account_thaw_date","2020/6/25");
        mapTemp.put("account_thaw_certificate",fileList4);
        mapTemp.put("account_association",0);
        mapTemp.put("account_associated_account_ref","t5pdf");
        mapTemp.put("account_associated_acct_certificates",fileList5);
        return mapTemp;
    }

    public Map init02FundAccountInfo() {
        Map mapTemp = new HashMap();
        log.info("初始化02账户数据结构");
        //默认股权账户
        List<String> fileList1 = new ArrayList<>();
        fileList1.add("test1.pdf");
        fileList1.add("test2.pdf");

        List<String> fileList2 = new ArrayList<>();
        fileList2.add("test21.pdf");
        fileList2.add("test22.pdf");
        List<String> fileList3 = new ArrayList<>();
        fileList3.add("test31.pdf");
        fileList3.add("test32.pdf");
        List<String> fileList4 = new ArrayList<>();
        fileList4.add("test41.pdf");
        fileList4.add("test42.pdf");
        List<String> fileList5 = new ArrayList<>();
        fileList5.add("test51.pdf");
        fileList5.add("test52.pdf");
        mapTemp.clear();
        mapTemp.put("account_object_id","testacc00001");
        mapTemp.put("account_holder_subject_ref","hrefid00001");
        mapTemp.put("account_depository_subject_ref","drefid00001");
        mapTemp.put("account_number","h0123555");
        mapTemp.put("account_type",1);  //资金账户
        mapTemp.put("account_never",0);
        mapTemp.put("account_status",0);
        mapTemp.put("account_opening_date","2012/6/25");
        mapTemp.put("account_opening_certificate",fileList4);
        mapTemp.put("account_closing_date","2022/6/25");
        mapTemp.put("account_closing_certificate",fileList2);
        mapTemp.put("account_forzen_date","2020/6/25");
        mapTemp.put("account_forzen_certificate",fileList3);
        mapTemp.put("account_thaw_date","2020/6/25");
        mapTemp.put("account_thaw_certificate",fileList4);
        mapTemp.put("account_association",0);
        mapTemp.put("account_associated_account_ref","t5pdf");
        mapTemp.put("account_associated_acct_certificates",fileList5);
        return mapTemp;
    }

    public Map init03EquityProductInfo() {
        Map mapTemp = new HashMap();
        log.info("初始化03产品数据结构");
        mapTemp.clear();
        mapTemp.put("product_object_id",gdEquityCode + "01");
        mapTemp.put("product_issuer_subject_ref",gdCompanyID);
        mapTemp.put("product_issuer_name","suzhou");
        mapTemp.put("product_registry_subject_ref","regobj001");
        mapTemp.put("product_trustee_subject_ref","tuoguanobj001");
        mapTemp.put("product_code","SH00001");
        mapTemp.put("product_name","联合股权");
        mapTemp.put("product_abbreviation","联股");
        mapTemp.put("product_type",0);
        mapTemp.put("product_term",0);
        mapTemp.put("product_info_disclosure_way",0);
        mapTemp.put("product_scale_unit",0);
        mapTemp.put("product_scale_currency","156");
        mapTemp.put("product_scale",10000000);
        mapTemp.put("product_customer_browsing_right",0);
        mapTemp.put("product_customer_trading_right",0);
        mapTemp.put("product_underwriter_subject_ref","chxobj0001");
        mapTemp.put("product_underwriter_name","chx123");
        mapTemp.put("product_law_firm_subject_ref","lawobj0001");
        mapTemp.put("product_law_firm_name","lawcorp");
        mapTemp.put("product_accounting_firm_subject_ref","accountobj001");
        mapTemp.put("product_accounting_firm_name","accoutcorp");
        mapTemp.put("发行方联系人","李四");
        mapTemp.put("发行方联系信息","acccorp");

        List<String> listFile = new ArrayList<>();
        listFile.add("dd.pdf");
        List<Map> mapList1 = new ArrayList<>();
        Map equityMap = new HashMap();

        equityMap.put("发行代码","tea111");
        equityMap.put("product_issue_price",10000);
        equityMap.put("发行股数",0);
        equityMap.put("product_annual_net_profit",10000);
        equityMap.put("product_before_authorized_shares",0);
        equityMap.put("product_after_authorized_shares",0);
        equityMap.put("product_after_issue_market_value",10000);
        equityMap.put("product_net_profit",10000);
        equityMap.put("募集金额",10000);
        equityMap.put("product_number_of_directional_issuers",0);
        equityMap.put("product_issue_start_date","2020/10/25");
        equityMap.put("product_issue_end_date","2020/10/25");
        equityMap.put("register_date","2020/10/25");
        equityMap.put("发行文件编号","tea111");
        equityMap.put("发行文件列表",listFile);
        equityMap.put("product_listing_code","tea111");
        equityMap.put("product_listing_time","2020/10/25");
        equityMap.put("product_listing_remarks","tea111");
        equityMap.put("product_listing_status",0);
        equityMap.put("product_delisting_time","2020/10/25");
        equityMap.put("product_delisting_reason",0);

        mapList1.add(equityMap);
        mapTemp.put("股权类-发行增资信息",mapList1);
        return mapTemp;
    }

    public Map init03BondProductInfo() {
        Map mapTemp = new HashMap();
        log.info("初始化03产品数据结构");
        mapTemp.clear();
        mapTemp.put("product_object_id",gdEquityCode + "02");
        mapTemp.put("product_issuer_subject_ref",gdCompanyID);
        mapTemp.put("product_issuer_name","issue00001");
        mapTemp.put("product_registry_subject_ref","tea11001");
        mapTemp.put("product_trustee_subject_ref","tea11002");
        mapTemp.put("product_code","SH00001");
        mapTemp.put("product_name","联合股权");
        mapTemp.put("product_abbreviation","联股");
        mapTemp.put("product_type",0);
        mapTemp.put("product_term",10);
        mapTemp.put("product_info_disclosure_way",0);
        mapTemp.put("product_scale_unit",100000);
        mapTemp.put("product_scale_currency","156");
        mapTemp.put("product_scale",1000000);
        mapTemp.put("product_customer_browsing_right",0);
        mapTemp.put("product_customer_trading_right",0);
        mapTemp.put("product_underwriter_subject_ref","cx00001");
        mapTemp.put("product_underwriter_name","cxabc");
        mapTemp.put("product_law_firm_subject_ref","lawobj00001");
        mapTemp.put("product_law_firm_name","lawyer");
        mapTemp.put("product_accounting_firm_subject_ref","countobj001");
        mapTemp.put("product_accounting_firm_name","countax");
        mapTemp.put("发行方联系人","zhagnss");
        mapTemp.put("发行方联系信息","15968526398");
        mapTemp.put("发行代码","12011");
        mapTemp.put("product_duration","2022/10/25");
        mapTemp.put("product_min_account_num",0);
        mapTemp.put("product_face_value",100);
        mapTemp.put("product_coupon_rate",10);
        mapTemp.put("product_lnterest_rate_form","155");
        mapTemp.put("product_Interest_payment_frequency","12");
        mapTemp.put("product_nonleap_year_interest_bearing_days",0);
        mapTemp.put("product_leap_year_interest_bearing_days",0);
        mapTemp.put("product_issue_price",100);
        mapTemp.put("product_option_clause",0);
        mapTemp.put("product_issue_scale_up",1000000);
        mapTemp.put("product_issue_scale_low",100);
        mapTemp.put("product_issue_start_date","2020/10/25");
        mapTemp.put("product_issue_end_date","2023/10/25");
        mapTemp.put("register_date","2020/10/25");
        mapTemp.put("product_value_date","2020/10/25");
        mapTemp.put("product_due_date","2026/10/25");
        mapTemp.put("product_first_interest_payment_date","2020/10/25");
        mapTemp.put("发行文件编号","wenjian00001");
        List<String> listFile = new ArrayList<>();
        listFile.add("dd.pdf");
        mapTemp.put("发行文件列表",listFile);
        mapTemp.put("product_issuer_credit_rating",0);
        mapTemp.put("product_credit_enhancement_agency_subject_ref","acobj0001");
        mapTemp.put("product_credit_enhancement_agency_name","acter");
        mapTemp.put("product_credit_enhancement_agency_credit_rating",0);
        mapTemp.put("product_credit_rating_agency_subject_ref","cdcobj001");
        mapTemp.put("product_credit_rating_agency_name","scder");
        mapTemp.put("product_guarantor_subject_ref","sdobj001");
        mapTemp.put("product_guarantor_name","sdqwe");
        mapTemp.put("product_guarantee_arrangement","122");
        mapTemp.put("product_termination_conditions","撤销");
        return mapTemp;
    }


    public Map init04TxInfo() {
        Map mapTemp = new HashMap();
        log.info("初始化04交易数据结构");
        //04交易报告
        mapTemp.put("transaction_object_id","txoid00001");
        mapTemp.put("transaction_traded_product_ref",gdEquityCode + "01");
        mapTemp.put("transaction_product_name","联合股权");
        mapTemp.put("transaction_type",0);
        mapTemp.put("交易场所","上海");
        mapTemp.put("transaction_description","交易");
        mapTemp.put("transaction_serial_num","00000000001");
        mapTemp.put("transaction_close_method",0);
        mapTemp.put("transaction_close_currency","156");
        mapTemp.put("transaction_close_price",1000);
        mapTemp.put("transaction_close_amount",1000);
        mapTemp.put("transaction_close_time","2020/10/8");
        mapTemp.put("transaction_close_description","交易成功");
        mapTemp.put("transaction_Issuer_principal_subject_ref",gdCompanyID);
        mapTemp.put("transaction_issuer_name","issue001");
        mapTemp.put("transaction_Investor_subject_ref","accobj0001");
        mapTemp.put("transaction_Investor_name","联合");
        mapTemp.put("transaction_original_owner_subject_ref","acchobj001");
        mapTemp.put("transaction_original_owner_name","zhagnsan");
        mapTemp.put("transaction_counterparty_subject_ref","acchobj002");
        mapTemp.put("transaction_counterparty_name","李四");
        mapTemp.put("transaction_order_verification_certificates","ddd.pdf");
        mapTemp.put("transaction_close_verification_certificates","erq.pdf");

        List<Map> mapList1 = new ArrayList<>();
        Map equityMap = new HashMap();
        equityMap.put("中介类型",0);
        equityMap.put("transaction_intermediary_subject_ref","obj001");
        equityMap.put("transaction_intermediary_name","中介");
        mapList1.add(equityMap);
        mapTemp.put("transaction_intermediary_information",mapList1);

        return mapTemp;
    }

    public Map init05RegInfo() {
        Map mapTemp = new HashMap();
        log.info("初始化05登记数据结构");
        //05登记
        List<String> listRegFile = new ArrayList<>();
        listRegFile.add("verify.crt");
        mapTemp.clear();
        mapTemp.put("register_registration_object_id","regid00001");
        mapTemp.put("register_registration_type",0);
        mapTemp.put("register_registration_serial_number",regNo);
        mapTemp.put("register_time","2020/7/8");
        mapTemp.put("register_rights_subject_ref",gdCompanyID);
        mapTemp.put("register_rights_subject_type",0);
        mapTemp.put("register_unit",0);
        mapTemp.put("register_currency","156");
        mapTemp.put("register_rights_change_amount",10000);
        mapTemp.put("register_available_balance",10000);
        mapTemp.put("register_available_percentage",78);
        mapTemp.put("register_rights_pledge_change_amount",10000);
        mapTemp.put("register_rights_pledge_balance",10000);
        mapTemp.put("register_rights_frozen_change_amount",10000);
        mapTemp.put("register_rights_frozen_balance",10000);
        mapTemp.put("register_holding_status",0);
        mapTemp.put("register_holding_attribute",0);
        mapTemp.put("来源类型",0);
        mapTemp.put("register_notes","登记联合股权项目产品");
        mapTemp.put("register_verification_certificates",listRegFile);
        mapTemp.put("登记产品类型",0);
        mapTemp.put("登记产品引用","regobj00011");
        mapTemp.put("权利人账户引用","accoid00001");
        mapTemp.put("capita_transaction_ref",0);
        mapTemp.put("register_roster_subject_ref","dd");
        mapTemp.put("register_rights_type",0);
        mapTemp.put("register_date","2020/7/8");
        mapTemp.put("register_shareholder_subject_ref","haccobj001");
        mapTemp.put("register_shareholder_subject_type",0);
        mapTemp.put("register_nature_of_shares",0);
        mapTemp.put("register_subscription_amount",10000);
        mapTemp.put("register_paid_in_amount",10000);
        mapTemp.put("register_shareholding_ratio",20);
        mapTemp.put("register_creditor_subject_ref","bondobj0001");
        mapTemp.put("register_creditor_type",0);
        mapTemp.put("register_creditor_subscription_count",0);
        mapTemp.put("register_creditor_paid_in_amount",10000);
        mapTemp.put("register_creditor_contact_info","ws@wutongchain.com");
        return mapTemp;
    }

    public Map init06SettleInfo() {
        Map mapTemp = new HashMap();
        log.info("初始化06资金清算数据结构");
        mapTemp.clear();
        List<String> listCert = new ArrayList<>();
        listCert.add("tix.pdf");
        listCert.add("tix2.pdf");

        //对象信息
        mapTemp.put("capita_settlement_object_id","CHAOyf0iKl68R3");
        mapTemp.put("capita_object_information_type",0);

        //资金结算基本信息
        mapTemp.put("capita_clearing_house_subject_ref","CH5LDWxGtu6142");
        mapTemp.put("capita_settlement_type",0);
        mapTemp.put("capita_settlement_serial_num","CH1ktvx01x2e04");
        mapTemp.put("capita_settlement_time","2020/11/06 14:14:59");
        mapTemp.put("capita_transaction_ref","CHVXgXA1JsB5y2");
        mapTemp.put("capita_currency","CHW5HHZl0gGgnj");
        mapTemp.put("capita_amount",1000000);
        mapTemp.put("capita_notes","textemu4AW4U57");
        mapTemp.put("capita_operation_certificates",listCert);

        //转出方信息
        mapTemp.put("capita_out_bank_code","CH3e9Vp967INOA");
        mapTemp.put("capita_out_bank_name","CH1tY37wB57auz");
        mapTemp.put("capita_out_bank_number","CHHe8e3M4j0N8o");
        mapTemp.put("capita_out_account_obj_ref","CH62QCHF5Q7sob");
        mapTemp.put("capita_out_fund_account_name","CH8tC5aG5om158");
        mapTemp.put("capita_out_amount_before_transfer",1000000);
        mapTemp.put("capita_out_amount_after_transfer",1000000);

        //转入方信息
        mapTemp.put("capita_in_bank_code","CH7YfKps3x65Y2");
        mapTemp.put("capita_in_bank_name","CHp42HpuGtf6y3");
        mapTemp.put("capita_in_bank_number","CHE0H230A17lu8");
        mapTemp.put("capita_in_account_obj_ref","CH8QL30e0ggRvp");
        mapTemp.put("capita_in_fund_account_name","CH6FU5bf2N2Y6B");
        mapTemp.put("capita_in_account_number","CHd8re1Dojtmx0");
        mapTemp.put("capita_in_amount_before_transfer",1000000);
        mapTemp.put("capita_in_amount_after_transfer",1000000);

        return mapTemp;
    }

    public Map init07PublishInfo() {
        Map mapTemp = new HashMap();
        log.info("初始化07信披数据结构");

        //07信披监管数据
        List<String> listPubFile = new ArrayList<>();
        listPubFile.add("tix.pdf");

        //诚信档案
        List<Map> listCredit = new ArrayList<>();
        Map mapCd = new HashMap();

        mapTemp.clear();


        //对象标识
        mapTemp.put("letter_disclosure_object_id","CHo8M7Jzo6y6Uh");
        mapTemp.put("letter_object_information_type",0);

        //企业展示信息
        mapTemp.put("letter_show_subject_reference","IDSR006");
        mapTemp.put("letter_show_subject_reference_ref","IDSR007");
        mapTemp.put("letter_display_code","C1106");
        mapTemp.put("letter_display_content","xinpi");
        mapTemp.put("letter_display_main_audit_voucher","mainpingzhen.pdf"); //20201106 此处监管数据为空格 与开发确认修改为下划线
        mapTemp.put("letter_show_content_audit_voucher","contentpingzhen.pdf");
        mapTemp.put("letter_show_start_date","2020/11/06");
        mapTemp.put("letter_show_end_date","2020/11/06");

        //信批基本信息
        mapTemp.put("letter_disclosure_subject_ref","CH58dk5280yB0A");
        mapTemp.put("letter_disclosure_uploader_ref","CH58dk5280yB0Aref");
        mapTemp.put("letter_approval_time","2020/11/06 14:15:00");


        //诚信档案
        //事项基本信息
        mapCd.put("letter_provider_subject_ref","CHl55WqGnO3hRd");
        mapCd.put("letter_provider_name","CH60FNHO699i3S");
        mapCd.put("letter_identified_party_subject_ref","CHAVko0Oq9139x");
        mapCd.put("letter_identified_party_name","CHccO9f2A3gQuK");
        mapCd.put("letter_appraiser_subject_ref","CH4Ntw87gm4H3m");
        mapCd.put("letter_appraiser_name","CHc112rQVG0I5U");
        //事项明细
        mapCd.put("letter_item_number","letter00000001");
        mapCd.put("letter_item_name","lettername");
        mapCd.put("letter_item_type",0);
        mapCd.put("letter_item_describe","因非法开设证券期货交易场所或者组织证券期货交易被地方政府行政处罚或者采取清理整顿措施");
        mapCd.put("letter_term_of_validity",1);
        mapCd.put("letter_start_time","2020/10/29");
        mapCd.put("letter_end_time","2022/10/29");
        mapCd.put("letter_item_state",1);
        mapCd.put("letter_item_file",listPubFile);
        listCredit.add(mapCd);
        mapTemp.put("integrity_archives",listCredit);

        //基本财务信息
        mapTemp.put("letter_start_date","2020/11/06");
        mapTemp.put("letter_deadline","2020/11/06");
        mapTemp.put("letter_report_type",0);
        mapTemp.put("letter_ending_total_asset",1000000);
        mapTemp.put("letter_ending_net_asset",1000000);
        mapTemp.put("letter_total_liability",1000000);
        mapTemp.put("letter_current_operating_income",1000000);
        mapTemp.put("letter_current_total_profit",1000000);
        mapTemp.put("letter_current_net_profit",1000000);
        mapTemp.put("letter_cash_flow",1000000);
        mapTemp.put("letter_whether_r&d_costs",1000000);
        mapTemp.put("letter_r&d_costs",1000000);
        //财务报表信息
        mapTemp.put("letter_balance_sheet","829.pdf");
        mapTemp.put("letter_cash_flow_sheet","53d.pdf");
        mapTemp.put("letter_profit_sheet","6L6.pdf");

        //重大事件列表
        List listME = new ArrayList();
        Map majorEvent = new HashMap();
        majorEvent.put("letter_major_event_type",0);
        majorEvent.put("letter_file_list","C63.pdf");
        majorEvent.put("letter_description_document","7P6.pdf");
        majorEvent.put("letter_submission_time","2020/11/06 14:15:00");

        listME.add(majorEvent);
        mapTemp.put("major_event_information",listME);

        //公告
        List listNotice = new ArrayList();
        Map notice = new HashMap();
        notice.put("letter_announcement_type",0);
        notice.put("letter_file_list","OG3.pdf");
        notice.put("letter_description_announcement","535.pdf");
        notice.put("letter_announcement_time","2020/11/06 14:15:00");

        listNotice.add(notice);
        mapTemp.put("letter_notice",listNotice);

        return mapTemp;
    }

}
