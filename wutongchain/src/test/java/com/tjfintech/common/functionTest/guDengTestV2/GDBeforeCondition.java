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
        init02ShareholderAccountInfo();
        shAccountInfo.put("account_object_id",shareHolderNo);  //更新账户对象标识字段
        log.info(shAccountInfo.toString());
        shareHolderInfo.put("shareholderNo",shareHolderNo);
        shareHolderInfo.put("accountInfo", shAccountInfo);
        log.info(shareHolderInfo.toString());

        //资金账户信息
        init02FundAccountInfo();
        fundAccountInfo.put("account_object_id",fundNo);  //更新账户对象标识字段
        Map mapFundInfo = new HashMap();
        mapFundInfo.put("fundNo",fundNo);
        mapFundInfo.put("accountInfo", fundAccountInfo);

        //构造个人/投资者主体信息
        init01PersonalSubjectInfo();
        investorSubjectInfo.put("subject_object_id",cltNo);  //更新对象标识字段
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
        shAccountInfo = init02ShareholderAccountInfo();          //初始化账户数据信息 股权账户  涉及接口 开户
        fundAccountInfo = init02FundAccountInfo();            //初始化账户数据信息 资金账户  涉及接口 开户
        equityProductInfo = init03EquityProductInfo();          //初始化私募股权类产品数据信息  涉及接口 挂牌企业登记 股份增发 场内转板
        bondProductInfo = init03BondProductInfo();            //初始化私募可转债产品数据信息  涉及接口 挂牌企业登记 发行
        fundProductInfo = init03JiJinProductInfo();            //初始化基金股权产品数据信息  涉及接口 挂牌企业登记 发行
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
//        listQual.add(qualification2);

        //对象标识
        mapTemp.put("subject_object_id",gdCompanyID);
        mapTemp.put("subject_object_information_type",0);

        //主体信息 主体基本信息 主体通用信息
        mapTemp.put("subject_id",gdCompanyID + "sub");
        mapTemp.put("subject_industry_code","CHDcdIuA52fhLo");
        mapTemp.put("subject_type",0);
        mapTemp.put("subject_create_time","2020/11/06 14:14:59");

        //主体信息 主体基本信息 主体资质信息
        //主体信息 主体基本信息 主体资质信息 资质信息
        mapTemp.put("subject_qualification_category",0);
        mapTemp.put("subject_market_roles_types",0);
        mapTemp.put("subject_financial_qualification_types",0);

        //主体信息 主体基本信息 主体资质信息 资质认证信息
        mapTemp.put("subject_role_qualification_number","CHpCT82aXlhaA3");
        mapTemp.put("subject_role_qualification_certification_document",fileList);
        mapTemp.put("subject_qualification_party_certification","CHbG59a6kufNo1");
        mapTemp.put("subject_role_qualification_reviewer","CH8Ggm9JnTBIp5");
        mapTemp.put("subject_certification_time","2020/11/05 14:14:59");
        mapTemp.put("subject_audit_time","2020/11/05 14:14:59");

//        mapTemp.put("subject_qualification_information",listQual);

        //主体信息 机构主体信息 机构分类信息
        mapTemp.put("subject_organization_type",0);
        mapTemp.put("subject_organization_nature",0);

        //主体信息 机构主体信息 企业基本信息
        mapTemp.put("subject_company_name","CHbne9QxJO40e1");
        mapTemp.put("subject_company_english_name","CHRTK405U5Mvde");
        mapTemp.put("subject_company_short_name","CH10V60bs23xrV");
        mapTemp.put("subject_company_short_english_name","CHMCp8U1af57p1");
        mapTemp.put("subject_company_type",0);
        mapTemp.put("subject_company_component",0);
        mapTemp.put("subject_unified_social_credit_code","cdSN0000000001");
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
        mapTemp.put("subject_shareholders_number",10);
        mapTemp.put("subject_total_share_capital",500000);
        mapTemp.put("subject_actual_controller","CH2cnqo0H67567");
        mapTemp.put("subject_actual_controller_id_type","CH7QgaY0T0o7n0");
        mapTemp.put("subject_actual_controller_id","CHGly2JJ06590f");

        //主体信息 机构主体信息 法人信息
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
        Map qualification1 = new HashMap();

        mapTemp.clear();
        String cltNo = "test00001";

        //对象标识
        mapTemp.put("subject_object_id",cltNo);
        mapTemp.put("subject_object_information_type",0);

        //主体信息 主体基本信息 主体通用信息
        mapTemp.put("subject_id",gdCompanyID + "sub");
        mapTemp.put("subject_industry_code","CHDcdIuA52fhLo");
        mapTemp.put("subject_type",0);
        mapTemp.put("subject_create_time","2020/11/06 14:14:59");

        //主体信息 主体基本信息 主体资质信息
        //主体信息 主体基本信息 主体资质信息 资质信息
        mapTemp.put("subject_qualification_category",0);
        mapTemp.put("subject_market_roles_types",0);
        mapTemp.put("subject_financial_qualification_types",0);

        //主体信息 主体基本信息 主体资质信息 资质认证信息
        mapTemp.put("subject_role_qualification_number","CHpCT82aXlhaA3");
        mapTemp.put("subject_role_qualification_certification_document",fileList1);
        mapTemp.put("subject_qualification_party_certification","CHbG59a6kufNo1");
        mapTemp.put("subject_role_qualification_reviewer","CH8Ggm9JnTBIp5");
        mapTemp.put("subject_certification_time","2020/11/05 14:14:59");
        mapTemp.put("subject_audit_time","2020/11/05 14:14:59");

//        mapQuali.add(qualification1);
//        mapTemp.put("subject_qualification_information",mapQuali);

        //主体信息 个人主体信息 个人主体基本信息
        mapTemp.put("subject_investor_name","zhangsan");
        mapTemp.put("subject_id_doc_type",0);
        mapTemp.put("subject_id_doc_number","325689199512230001");
        mapTemp.put("subject_id_address","相城");
        mapTemp.put("subject_contact_address","相城");
        mapTemp.put("subject_investor_contact_number","15865487895");
        mapTemp.put("subject_cellphone_number","15865487895");
        mapTemp.put("subject_personal_fax_number","56892586");
        mapTemp.put("subject_postalcode_number","122336");
        mapTemp.put("subject_id_doc_mailbox","xx@123.com");
        mapTemp.put("subject_education",4);
        mapTemp.put("subject_industry",0);
        mapTemp.put("subject_birthday","1949/09/12");
        mapTemp.put("subject_gender",0);
        mapTemp.put("subject_work_unit","苏同院");
        mapTemp.put("subject_Investment_period","12");
        mapTemp.put("subject_Investment_experience","12");
        mapTemp.put("subject_native_place","江苏苏州");
        mapTemp.put("subject_mail_box","高铁新城11");
        mapTemp.put("subject_province","江苏");
        mapTemp.put("subject_city","苏州");


        //主体信息 个人主体信息 个人主体风险评级
        mapTemp.put("subject_rating_results","通过");
        mapTemp.put("subject_rating_time","2020/09/12 12:01:12");
        mapTemp.put("subject_rating_record","记录");

        return mapTemp;
    }

    public Map init02ShareholderAccountInfo() {
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

        //对象信息
        mapTemp.put("account_object_id","testacc00001");
        mapTemp.put("account_object_information_type",0);

        //账户信息 账户基本信息
        mapTemp.put("account_holder_subject_ref","hrefid00001");
        mapTemp.put("account_depository_subject_ref","drefid00001");
        mapTemp.put("account_number","h0123555");
        mapTemp.put("account_type",0);  //默认股权账户
        mapTemp.put("account_never",0);
        mapTemp.put("account_status",0);

        //账户信息 账户资质信息
        mapTemp.put("account_qualification_certification_file",fileList1);
        mapTemp.put("account_certifier","监管局");
        mapTemp.put("account_auditor","认证者");
        mapTemp.put("account_certification_time","2012/6/25");
        mapTemp.put("account_audit_time","2012/6/25");

        //账户信息 账户生命周期信息
        //账户信息 账户生命周期信息 开户信息
        mapTemp.put("account_opening_date","2012/6/25");
        mapTemp.put("account_opening_certificate",fileList4);

        //账户信息 账户生命周期信息 销户信息
        mapTemp.put("account_closing_date","2022/6/25");
        mapTemp.put("account_closing_certificate",fileList2);

        //账户信息 账户生命周期信息 冻结信息
        mapTemp.put("account_forzen_date","2020/6/25");
        mapTemp.put("account_forzen_certificate",fileList3);

        //账户信息 账户生命周期信息 解冻信息
        mapTemp.put("account_thaw_date","2020/6/25");
        mapTemp.put("account_thaw_certificate",fileList4);

        //账户信息 账户关联信息
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

        //对象信息
        mapTemp.put("account_object_id","testacc00001");
        mapTemp.put("account_object_information_type",0);

        //账户信息 账户基本信息
        mapTemp.put("account_holder_subject_ref","hrefid00001");
        mapTemp.put("account_depository_subject_ref","drefid00001");
        mapTemp.put("account_number","h0123555");
        mapTemp.put("account_type",1);  //资金账户
        mapTemp.put("account_never",0);
        mapTemp.put("account_status",0);

        //账户信息 账户资质信息
        mapTemp.put("account_qualification_certification_file",fileList1);
        mapTemp.put("account_certifier","监管局");
        mapTemp.put("account_auditor","认证者");
        mapTemp.put("account_certification_time","2012/6/25");
        mapTemp.put("account_audit_time","2012/6/25");

        //账户信息 账户生命周期信息
        //账户信息 账户生命周期信息 开户信息
        mapTemp.put("account_opening_date","2012/6/25");
        mapTemp.put("account_opening_certificate",fileList4);

        //账户信息 账户生命周期信息 销户信息
        mapTemp.put("account_closing_date","2022/6/25");
        mapTemp.put("account_closing_certificate",fileList2);

        //账户信息 账户生命周期信息 冻结信息
        mapTemp.put("account_forzen_date","2020/6/25");
        mapTemp.put("account_forzen_certificate",fileList3);

        //账户信息 账户生命周期信息 解冻信息
        mapTemp.put("account_thaw_date","2020/6/25");
        mapTemp.put("account_thaw_certificate",fileList4);

        //账户信息 账户关联信息
        mapTemp.put("account_association",0);
        mapTemp.put("account_associated_account_ref","t5pdf");
        mapTemp.put("account_associated_acct_certificates",fileList5);
        return mapTemp;
    }


    public Map productCommonInfo(int type){
        Map mapProdComInfo = new HashMap();

        //对象标识
        mapProdComInfo.put("product_object_id",gdEquityCode + "01");
        mapProdComInfo.put("product_object_information_type",0);

        //产品信息 基本信息 产品基本信息
        mapProdComInfo.put("product_trading_market_category",0);
        mapProdComInfo.put("product_market_subject","msN0000110");
        mapProdComInfo.put("product_market_subject_name","上海交易所");
        mapProdComInfo.put("product_plate_trading_name","N板");  //以上为新增
        mapProdComInfo.put("product_issuer_subject_ref",gdCompanyID);
        mapProdComInfo.put("product_issuer_name","sh");
        mapProdComInfo.put("product_code","SH10001");
        mapProdComInfo.put("product_name","发行小额");
        mapProdComInfo.put("product_abbreviation","融资类小额");
        mapProdComInfo.put("product_use",0);
        mapProdComInfo.put("product_type",type);//私募股权 可转债 基金会有不同
        mapProdComInfo.put("product_term",50000);
        mapProdComInfo.put("product_info_disclosure_way",0);
        mapProdComInfo.put("product_scale_unit",5000);
        mapProdComInfo.put("product_scale_currency",0);
        mapProdComInfo.put("product_scale",100000);
        mapProdComInfo.put("product_customer_browsing_right",0);
        mapProdComInfo.put("product_customer_trading_right",0);
        mapProdComInfo.put("product_issuer_contact_person","张三");
        mapProdComInfo.put("product_issuer_contact_info","1555222");

        //产品信息 基本信息 服务方信息
        mapProdComInfo.put("product_registry_subject_ref","regobj001");
        mapProdComInfo.put("product_name_registration_body","登记机构名");
        mapProdComInfo.put("product_trustee_subject_ref","regobj001");
        mapProdComInfo.put("product_name_trustee","联合股权");
        mapProdComInfo.put("product_underwriter_subject_ref","regobj001");
        mapProdComInfo.put("product_underwriter_name","承销机构名");
        mapProdComInfo.put("product_sponsor_subject","regobj001");
        mapProdComInfo.put("product_sponsor_subject_name","保荐机构名");
        mapProdComInfo.put("product_fund_administrator_account","regobj001");
        mapProdComInfo.put("product_fund_administrator_name","管理人名");
        mapProdComInfo.put("product_guarantor_subject_ref","regobj001");
        mapProdComInfo.put("product_guarantor_name","担保机构名");
        mapProdComInfo.put("product_law_firm_subject_ref","regobj001");
        mapProdComInfo.put("product_law_firm_name","律所名");
        mapProdComInfo.put("product_accounting_firm_subject_ref","regobj001");
        mapProdComInfo.put("product_accounting_firm_name","事务所名");
        mapProdComInfo.put("product_credit_enhancement_agency_subject_ref","regobj001");
        mapProdComInfo.put("product_credit_enhancement_agency_name","信用机构名");
        mapProdComInfo.put("product_credit_rating_agency_subject_ref","regobj001");
        mapProdComInfo.put("product_credit_rating_agency_name","评级机构名");


        //产品信息 基本信息 产品文件信息
        List<Map> listMapPF = new ArrayList<>();
        Map mapPF = new HashMap();
        mapPF.put("product_issue_doc_num",122);
        mapPF.put("product_file_name","12345");
        mapPF.put("product_issue_doc","psf.pdf");
        listMapPF.add(mapPF);
        mapProdComInfo.put("product_file_information",listMapPF);

        //产品信息 基本信息 产品注册备案信息
        List<Map> listMapPR = new ArrayList<>();
        Map mapPR = new HashMap();

        mapPR.put("product_license_type",122);
        mapPR.put("product_license_number","12345");
        mapPR.put("product_license_file_name","psf.pdf");
        mapPR.put("product_license_documents","psf.pdf");
        listMapPR.add(mapPR);
        mapProdComInfo.put("product_registration_information",listMapPR);


        List<String> listFile = new ArrayList<>();
        listFile.add("dd.pdf");

        //产品信息 产品标的信息
        mapProdComInfo.put("product_fund_type",0);
        mapProdComInfo.put("product_description_fund_use",1);
        //产品信息 产品标准信息 经营用途信息
        List<Map> listMapBi = new ArrayList<>();
        Map mapBi = new HashMap();
        mapBi.put("product_business_purpose_details","用途详情"); //与文档顺序中和名称调换 为数据比对
        mapBi.put("product_business_purpose_name","用途名称");
        mapBi.put("product_documents_list",listFile);
        listMapBi.add(mapBi);
        mapProdComInfo.put("business_information",listMapBi);
        //产品信息 产品标准信息 投资组合信息
        List<Map> listMapPi = new ArrayList<>();
        Map mapPi = new HashMap();
        mapPi.put("product_business_purpose_details","用途详情");//与文档顺序中和名称调换 为数据比对
        mapPi.put("product_business_purpose_name","用途名称");
        mapPi.put("product_documents_list",listFile);
        listMapPi.add(mapPi);
        mapProdComInfo.put("portfolio_information",listMapPi);

        return mapProdComInfo;
    }

    public Map init03EquityProductInfo() {
        Map mapTemp = new HashMap();
        log.info("初始化03产品数据结构");
        mapTemp.clear();

        mapTemp = productCommonInfo(0); //私募股权

        //产品信息 发行信息 私募股权
        mapTemp.put("product_issue_price",50);
        mapTemp.put("product_issue_price_method",0);
        mapTemp.put("product_before_authorized_shares",5000);
        mapTemp.put("product_after_authorized_shares",5000);
        mapTemp.put("product_after_issue_market_value",5000);
        mapTemp.put("product_net_profit",5000);
        mapTemp.put("product_annual_net_profit",5000);
        mapTemp.put("product_number_of_directional_issuers",5000);
        mapTemp.put("product_issue_start_date","2021/10/11");
        mapTemp.put("product_issue_end_date","2025/10/11");
        mapTemp.put("product_registration_date","2021/10/11");
        mapTemp.put("product_unlimited_sales_number_shares",5000);
        mapTemp.put("restricted_shares_number",5000);


        //交易信息
        mapTemp.put("product_listing_code",gdEquityCode);
        mapTemp.put("product_listing_time","2010/8/9");
        mapTemp.put("product_listing_status",0);
        mapTemp.put("product_listing_remarks","挂牌信息备注");
        mapTemp.put("product_delisting_time","2010/8/9");
        mapTemp.put("product_delisting_reason","不再挂牌");
        return mapTemp;
    }

    //私募可转债
    public Map init03BondProductInfo() {
        Map mapTemp = new HashMap();
        log.info("初始化03产品数据结构");
        mapTemp.clear();

        mapTemp = productCommonInfo(1);//私募可转债

        //产品信息 发行信息 私募可转债
        mapTemp.put("product_duration","2025/10/11");
        mapTemp.put("product_min_account_num",10);
        mapTemp.put("product_face_value",10);
        mapTemp.put("product_conversion_right","12");
        mapTemp.put("product_conversion_price",12);
        mapTemp.put("product_conversion_price_remark","zhuanhuan");
        mapTemp.put("product_adjustment_change_control",15);
        mapTemp.put("product_conversion_ratio",5);
        mapTemp.put("product_conversion_period",20);
        mapTemp.put("product_conversion_share_ranking",0);
        mapTemp.put("product_redemption",0);
        mapTemp.put("product_conversion_premium",12);
        mapTemp.put("product_conversion_price_ref",20);
        mapTemp.put("product_coupon_rate",10);
        mapTemp.put("product_lnterest_rate_form","12");
        mapTemp.put("product_interest_payment_method",0);
        mapTemp.put("product_interest_type",0);
        mapTemp.put("product_Interest_payment_frequency",10);
        mapTemp.put("product_nonleap_year_interest_bearing_days",20);
        mapTemp.put("product_leap_year_interest_bearing_days",60);
        mapTemp.put("product_issue_price",12);
        mapTemp.put("product_option_clause",0);
        mapTemp.put("product_issue_scale_up",150000);
        mapTemp.put("product_issue_scale_low",0);
        mapTemp.put("product_issue_start_date","2020/12/12");
        mapTemp.put("product_issue_end_date","2025/12/12");
        mapTemp.put("product_registration_date","2020/12/12");
        mapTemp.put("product_value_date","2021/12/12");
        mapTemp.put("product_due_date","2025/12/12");
        mapTemp.put("product_first_interest_payment_date","2021/12/12");
        mapTemp.put("product_issuer_credit_rating",3);
        mapTemp.put("product_credit_enhancement_agency_credit_rating",2);
        mapTemp.put("product_guarantee_arrangement","d测试");
        mapTemp.put("product_lockup",1222);
        mapTemp.put("product_termination_conditions","退托管");

        //交易信息
        mapTemp.put("product_listing_code",gdEquityCode);
        mapTemp.put("product_listing_time","2010/8/9");
        mapTemp.put("product_listing_status",0);
        mapTemp.put("product_listing_remarks","挂牌信息备注");
        mapTemp.put("product_delisting_time","2010/8/9");
        mapTemp.put("product_delisting_reason","不再挂牌");

        return mapTemp;
    }

    public Map init03JiJinProductInfo(){
        Map mapTemp = new HashMap();
        log.info("初始化03产品数据结构");
        mapTemp.clear();

        mapTemp = productCommonInfo(2); //基金

        //产品信息 发行信息 私募基金
        mapTemp.put("product_fund_type",0);
        mapTemp.put("product_foundation_time","2020/12/12");
        mapTemp.put("product_fund_legal_form",1);
        mapTemp.put("product_fund_fairvalue",100000);
        mapTemp.put("product_starting_time_raising","2020/12/12");
        mapTemp.put("product_closing_time_raising","2026/12/12");

        //交易信息
        mapTemp.put("product_listing_code",gdEquityCode);
        mapTemp.put("product_listing_time","2010/8/9");
        mapTemp.put("product_listing_status",0);
        mapTemp.put("product_listing_remarks","挂牌信息备注");
        mapTemp.put("product_delisting_time","2010/8/9");
        mapTemp.put("product_delisting_reason","不再挂牌");

        return mapTemp;
    }


    public Map init04TxInfo() {
        Map mapTemp = new HashMap();
        log.info("初始化04交易数据结构");
        //04交易报告
        //对象信息
        mapTemp.put("transaction_object_id","txoid00001");
        mapTemp.put("transaction_object_information_type",0);

        //交易报告信息 交易基本信息
        mapTemp.put("transaction_traded_product_ref",gdEquityCode + "01");
        mapTemp.put("transaction_product_name","联合股权");
        mapTemp.put("transaction_type",0);
        mapTemp.put("transaction_description","交易");

        //交易报告信息 交易成交信息 成交内容信息
        mapTemp.put("transaction_serial_num","00000000001");
        mapTemp.put("transaction_close_method",0);
        mapTemp.put("transaction_close_currency","156");
        mapTemp.put("transaction_close_price",1000);
        mapTemp.put("transaction_close_amount",1000);
        mapTemp.put("transaction_close_time","2020/10/8");
        mapTemp.put("transaction_close_description","交易成功");

        //交易报告信息 交易成交信息 融资类交易成交信息
        mapTemp.put("transaction_Issuer_principal_subject_ref",gdCompanyID);
        mapTemp.put("transaction_issuer_name","issue001");
        mapTemp.put("transaction_Investor_subject_ref","accobj0001");
        mapTemp.put("transaction_Investor_name","联合");
        //交易报告信息 交易成交信息 交易成交方信息
        mapTemp.put("transaction_original_owner_subject_ref","acchobj001");
        mapTemp.put("transaction_original_owner_name","zhagnsan");
        mapTemp.put("transaction_counterparty_subject_ref","acchobj002");
        mapTemp.put("transaction_counterparty_name","李四");
        List<String> list1 = new ArrayList<>();list1.add("ddd.pdf");
        List<String> list2 = new ArrayList<>();list2.add("erq.pdf");
        mapTemp.put("transaction_order_verification_certificates",list1);
        mapTemp.put("transaction_close_verification_certificates",list2);

        List<Map> mapList1 = new ArrayList<>();
        Map equityMap = new HashMap();
        equityMap.put("transaction_intermediary_type",0);
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
        //对象标识
        mapTemp.put("register_registration_object_id","regid00001");
        mapTemp.put("register_object_information_type",0);
        mapTemp.put("register_registration_type",0);

        //登记信息 权利信息 权利基本信息
        mapTemp.put("register_registration_serial_number",regNo);
        mapTemp.put("register_time","2020/7/8");
        mapTemp.put("register_rights_subject_ref",gdCompanyID);
        mapTemp.put("register_rights_subject_type",0);
        mapTemp.put("register_account_obj_id","dej55222");
        mapTemp.put("registration_rights_type",0);
        mapTemp.put("registration_object_right","123");
        mapTemp.put("register_unit",0);
        mapTemp.put("register_currency","156");
        mapTemp.put("register_rights_change_amount",10000);
        mapTemp.put("register_available_balance",10000);
        mapTemp.put("register_available_percentage",78);
        mapTemp.put("register_rights_pledge_change_amount",10000);
        mapTemp.put("register_rights_pledge_balance",10000);
        mapTemp.put("register_frozen_category",0);
        mapTemp.put("register_rights_frozen_change_amount",10000);
        mapTemp.put("register_rights_frozen_balance",10000);
        mapTemp.put("register_freeze_deadline_time","2020/2/8");
        mapTemp.put("register_holding_status",0);
        mapTemp.put("register_holding_attribute",0);
        mapTemp.put("registration_source",0);
        mapTemp.put("register_source_type",0);
        mapTemp.put("register_notes","登记联合股权项目产品");
        mapTemp.put("register_verification_certificates",listRegFile);
        mapTemp.put("transaction_type",0);
        mapTemp.put("register_transaction_obj_id","1235");

        //登记信息 名册登记 名册基本信息
        mapTemp.put("register_roster_subject_ref","dd");
        mapTemp.put("register_rights_type",0);
        mapTemp.put("register_date","2020/7/8");

        //登记信息 名册登记 股东名册
        mapTemp.put("register_shareholder_subject_ref","haccobj001");
        mapTemp.put("register_shareholder_subject_type",0);
        mapTemp.put("register_nature_of_shares",0);
        mapTemp.put("register_subscription_amount",10000);
        mapTemp.put("register_paid_in_amount",10000);
        mapTemp.put("register_shareholding_ratio",20);

        //登记信息 名册登记 债权人名册
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
