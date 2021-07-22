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

import static com.tjfintech.common.utils.UtilsClass.SLEEPTIME;
import static com.tjfintech.common.utils.UtilsClass.sleepAndSaveInfo;
import static com.tjfintech.common.utils.UtilsClassGD.*;
import static org.junit.Assert.assertEquals;


@Slf4j

/***
 * 此类用于测试监管非必填字段不填写场景的传参
 */
public class GDBeforeConditionN {
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
        Map mapSHAcc = init02ShareholderAccountInfo();
        mapSHAcc.put("account_object_id", shareHolderNo);  //更新账户对象标识字段
        mapSHAcc.put("account_subject_ref", cltNo);  //更新账户所属主体引用

        log.info(shAccountInfo.toString());
        shareHolderInfo.put("createTime", ts2);
        shareHolderInfo.put("shareholderNo", shareHolderNo);
        shareHolderInfo.put("accountInfo", mapSHAcc);
        log.info(shareHolderInfo.toString());

        //资金账户信息
        Map mapFundAcc = init02FundAccountInfo();
        mapFundAcc.put("account_object_id", fundNo);  //更新账户对象标识字段
        mapFundAcc.put("account_subject_ref", cltNo);  //更新账户所属主体引用
        mapFundAcc.put("account_associated_account_ref", shareHolderNo);  //更新关联账户对象引用

        Map mapFundInfo = new HashMap();
        mapFundInfo.put("createTime", ts2);
        mapFundInfo.put("fundNo", fundNo);
        mapFundInfo.put("accountInfo", mapFundAcc);

        //构造个人/投资者主体信息
        Map investor = init01PersonalSubjectInfo();
        investor.put("subject_object_id", cltNo);  //更新对象标识字段

        String response = gd.GDCreateAccout(gdContractAddress, cltNo, mapFundInfo, shareHolderInfo, investor);
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
        mapAccInfo.put("shareholderNo", shareHolderNo);
        mapAccInfo.put("fundNo", fundNo);
        mapAccInfo.put("response", response);

        mapAccAddr.put(addr, clientNo);

        return mapAccInfo;
    }

    //    @Test
    public void initRegulationData() {
        updateWord = "";
        //更新系统合约
        gd.GDEquitySystemInit(gdContractAddress, gdPlatfromKeyID);

        refData();

        //为缩短初始化时间 判断对象标识是否存在 存在则不再挂牌 否则执行
//        updateCommonRefSubAndReg();
        if (!JSONObject.fromObject(gd.GDObjectQueryByVer(subject_investor_qualification_certifier_ref, -1)).getString("state").equals("200")){
            initCommonRefSubAndReg();                           //初始化监管引用数据做挂牌企业登记操作
//        updateCommonRefSubAndReg();
        }

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
        log.info("初始化01主体企业数据结构，不填写非必填字段");

        //-----------------主体基本信息---------------//
        //对象标识
        mapTemp.put("subject_object_id", gdCompanyID);

        //主体信息 主体基本信息 主体通用信息
        mapTemp.put("subject_type", 1);
        mapTemp.put("subject_main_administrative_region", 0);
        mapTemp.put("subject_create_time", time3);
        //-----------------主体基本信息---------------//

        //-----------------机构主体信息---------------//
        //主体信息 机构主体信息 企业基本信息 基本信息描述
        mapTemp.put("subject_company_name", "公司全称CHARACTER");
        mapTemp.put("subject_shareholders_number",10);
        ArrayList listLMI = new ArrayList();
        mapTemp.put("leading_member_information", listLMI);
        mapTemp.put("subject_qualification_information", listLMI);
        //-----------------机构主体信息---------------//
        return mapTemp;
    }


    public Map init01PersonalSubjectInfo() {
        Map mapTemp = new HashMap();

        //-----------------主体基本信息start---------------//
        //对象标识
        mapTemp.put("subject_object_id", "clientNo0001");

        //主体信息 主体基本信息 主体通用信息
        mapTemp.put("subject_type", 2);
        mapTemp.put("subject_main_administrative_region", 0);
        mapTemp.put("subject_create_time", time1);
        //-----------------主体基本信息end---------------//

        //主体信息 个人主体信息 个人主体基本信息
        mapTemp.put("subject_investor_name", "zhangsan");
        mapTemp.put("subject_id_type", 0);
        mapTemp.put("subject_id_number", "个人身份证件号CHARACTER");

        ArrayList listEmpty = new ArrayList();
        mapTemp.put("subject_qualification_information", listEmpty);
        return mapTemp;
    }

    public Map init02ShareholderAccountInfo() {
        Map mapTemp = new HashMap();
        log.info("初始化02账户数据结构");
        mapTemp.clear();

        mapTemp.put("account_object_id", "testacc00001");

        //账户信息 账户基本信息
        mapTemp.put("account_subject_ref", account_subject_ref);
        mapTemp.put("account_depository_ref", account_depository_ref);
        mapTemp.put("account_type", 1);  //默认证券/股权账户
        List<Integer> type = new ArrayList<>();
        type.add(0);
        type.add(1);
        mapTemp.put("account_purpose", type);
        mapTemp.put("account_status", 1);
        mapTemp.put("account_create_time", time1);

        ArrayList listEmpty = new ArrayList();
        mapTemp.put("account_opening_doc",listEmpty);
        mapTemp.put("account_thaw_doc", listEmpty);
        mapTemp.put("account_frozen_doc", listEmpty);
        mapTemp.put("account_opening_doc", listEmpty);
        mapTemp.put("account_closing_doc", listEmpty);
//        mapTemp.put("account_thaw_date", "");

        return mapTemp;
    }

    public Map init02FundAccountInfo() {
        Map mapTemp = new HashMap();
        log.info("初始化02账户数据结构");
        mapTemp.clear();

        mapTemp.put("account_object_id", "testacc00001");

        //账户信息 账户基本信息
        mapTemp.put("account_subject_ref", account_subject_ref);
        mapTemp.put("account_depository_ref", account_depository_ref);
        mapTemp.put("account_type", 2);  //资金账户
        List<Integer> type = new ArrayList<>();
        type.add(0);
        type.add(1);
        mapTemp.put("account_purpose", type);
        mapTemp.put("account_status", 1);
        mapTemp.put("account_create_time", time1);

        ArrayList listEmpty = new ArrayList();
        mapTemp.put("account_opening_doc",listEmpty);
        mapTemp.put("account_thaw_doc", listEmpty);
        mapTemp.put("account_frozen_doc", listEmpty);
        mapTemp.put("account_opening_doc", listEmpty);
        mapTemp.put("account_closing_doc", listEmpty);

        return mapTemp;
    }


    public Map productCommonInfo(int type) {
        Map mapProdComInfo = new HashMap();

        //对象标识
        mapProdComInfo.put("product_object_id", gdEquityCode);
//        mapProdComInfo.put("product_object_information_type",0);

        //产品信息 基本信息 产品基本信息
        mapProdComInfo.put("product_trading_market_category", 1);
        mapProdComInfo.put("product_market_subject_ref", product_market_subject_ref);
        mapProdComInfo.put("product_market_subject_name", "交易场所主体名称CHARACTER"+ updateWord);
        mapProdComInfo.put("product_issuer_subject_ref", gdCompanyID);
        mapProdComInfo.put("product_issuer_name", "发行主体名称CHARACTER"+ updateWord);
        mapProdComInfo.put("product_code", "产品代码CHARACTER"+ updateWord);
        mapProdComInfo.put("product_name", "产品全称CHARACTER"+ updateWord);
        mapProdComInfo.put("product_type", type);
        mapProdComInfo.put("product_create_time", time2);

        //产品信息 基本信息 服务方信息
        List<Map> listZero = new ArrayList<>();
        mapProdComInfo.put("service_provider_information", listZero);
        mapProdComInfo.put("product_file_information", listZero);
        mapProdComInfo.put("product_shares_issued_class", listZero);
        mapProdComInfo.put("filing_information", listZero);
        return mapProdComInfo;
    }

//    public Map productCommonInfo2(Map mapProdComInfo) {
//
//        //产品信息 交易信息
//        //产品信息 交易信息 交易状态
//        mapProdComInfo.put("product_transaction_status", 1);
//
//        //产品信息 交易信息 挂牌信息
//        mapProdComInfo.put("product_transaction_scope", 1);
//        mapProdComInfo.put("product_transfer_permission_institution_to_individual", true);
//        mapProdComInfo.put("product_transfer_lockup_days", 20);
//        mapProdComInfo.put("product_transfer_validity", 30);
//        mapProdComInfo.put("product_risk_level", "产品风险级别CHARACTER"+ updateWord);
//        mapProdComInfo.put("product_transaction_unit", 1000);
//        mapProdComInfo.put("product_listing_code", "挂牌代码CHARACTER"+ updateWord);
//        mapProdComInfo.put("product_listing_date", date2);
//        mapProdComInfo.put("product_listing_remarks", "挂牌备注信息CHARACTER"+ updateWord);
//
//        //产品信息 交易信息 摘牌信息
//        mapProdComInfo.put("product_delisting_date", date3);
//        mapProdComInfo.put("product_delisting_type", 1);
//        mapProdComInfo.put("product_delisting_reason", 1);
//        mapProdComInfo.put("product_transfer_board_market", 2);
//        mapProdComInfo.put("product_acquisition_company_market", 3);
//        mapProdComInfo.put("product_delisting_remarks", "摘牌备注信息TEXT"+ updateWord);
//
//        //产品信息 托管信息
//        mapProdComInfo.put("product_custodian_registration_date", date2);
//        mapProdComInfo.put("product_custodian_documents", getListFileObj());
//        mapProdComInfo.put("product_custodian_notes", "托管备注信息TEXT"+ updateWord);
//        mapProdComInfo.put("product_escrow_deregistration_date", date4);
//        mapProdComInfo.put("product_escrow_deregistration_document", getListFileObj());
//        mapProdComInfo.put("product_escrow_deregistration_remarks", "解除托管备注信息TEXT"+ updateWord);
//
//
//        return mapProdComInfo;
//    }

    public Map init03EquityProductInfo() {
        Map mapTemp = new HashMap();
        log.info("初始化03产品数据结构");
        mapTemp = productCommonInfo(0); //私募股权
        return mapTemp;
    }

    //私募可转债
    public Map init03BondProductInfo() {
        Map mapTemp = new HashMap();
        log.info("初始化03产品数据结构");
        mapTemp = productCommonInfo(0); //私募股权
        return mapTemp;
    }

    public Map init03FundProductInfo() {
        Map mapTemp = new HashMap();
        log.info("初始化03产品数据结构");
        mapTemp = productCommonInfo(0); //私募股权
        return mapTemp;
    }


    public Map init04TxInfo() {
        Map mapTemp = new HashMap();
        log.info("初始化04交易数据结构");
        //04交易报告
        ArrayList listTemp = new ArrayList();
        //对象信息
        mapTemp.put("transaction_object_id", "txoid00001");
//        mapTemp.put("transaction_object_information_type",0);

        //交易报告信息 交易基本信息
        mapTemp.put("transaction_market_type", 0);
        mapTemp.put("transaction_type", 1);
        mapTemp.put("transaction_method", 1);
        mapTemp.put("transaction_create_time", time4);

        //交易报告信息 交易资产信息
        mapTemp.put("transaction_product_custody_status", 0);
        mapTemp.put("transaction_intermediary_information",listTemp);
        return mapTemp;
    }

    public Map init05RegInfo() {
        Map mapTemp = new HashMap();
//        log.info("初始化05登记数据结构");
        //05登记
        List<String> listRegFile = new ArrayList<>();
        mapTemp.clear();
        //对象标识
        mapTemp.put("register_registration_object_id", "regid00001");

        //登记信息 登记基本信息
        mapTemp.put("register_object_type", 1);
        mapTemp.put("register_event_type", register_event_type);//默认托管登记

        //登记信息 权利信息 权利基本信息 权利基本信息描述
        mapTemp.put("register_time", time2);
        mapTemp.put("register_subject_ref",register_subject_ref);
        mapTemp.put("register_subject_type", 1);
        mapTemp.put("register_subject_account_ref", register_subject_account_ref);
        mapTemp.put("register_asset_type", 1);
        mapTemp.put("register_asset_unit", 1);
        mapTemp.put("register_asset_currency", "156");
        mapTemp.put("register_create_time", time2);
        mapTemp.put("register_product_ref", gdEquityCode);

        mapTemp.put("register_source_type", 0);
        mapTemp.put("register_shareholders",listRegFile);
        mapTemp.put("fund_investors",listRegFile);
        mapTemp.put("register_creditors",listRegFile);

        return mapTemp;
    }

    public Map init06SettleInfo() {
        Map mapTemp = new HashMap();
        log.info("初始化06资金清算数据结构");
        mapTemp.clear();
        //资金结算信息 资金结算基本信息
        mapTemp.put("settlement_type", 0);
        mapTemp.put("settlement_currency", "156");
        mapTemp.put("settlement_value", 600000);
        mapTemp.put("settlement_information_maintenance_time", time1);

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
        //信披对象标识
//        mapTemp.put("letter_disclosure_object_id", "CHo8M7Jzo6y6Uh");

        //信披信息 信披基本信息
        mapTemp.put("disclosure_subject_ref", disclosure_subject_ref);
        mapTemp.put("disclosure_type", disclosureType);
        mapTemp.put("disclosure_submit_type", 0);
        mapTemp.put("disclosure_submit_date", date1);
//        mapTemp.put("disclosure_referer_subject_ref", disclosure_referer_subject_ref);//非必填字段 20210107 已提bug
//        mapTemp.put("disclosure_referer_name", "非必填字段");//非必填字段 20210107 已提bug
//        mapTemp.put("disclosure_submit_description", "非必填字段");//非必填字段 20210107 已提bug

        List<Map> listEmptyp = new ArrayList<>();

        switch (disclosureType) {
            case 1:
                //信披信息 企业展示信息
                List<Map> listEnterpriseInformation = new ArrayList<>();
                Map mapEId = new HashMap();
                mapEId.put("disclosure_display_platform_ref", disclosure_display_platform_ref);
                mapEId.put("disclosure_display_name", "展示文件名称");
//                mapEId.put("disclosure_display_doc", getListFileObj());
                mapEId.put("disclosure_display_description", "展示文件简述");
//                mapEId.put("disclosure_display_code", "展示代码");
                mapEId.put("disclosure_display_content", "展示内容");
//                mapEId.put("disclosure_display_start_date", date1);
//                mapEId.put("disclosure_display_end_date", date2);
                listEnterpriseInformation.add(mapEId);

                //其他需要传List
                mapTemp.put("enterprise_display_information", listEnterpriseInformation);
//                mapTemp.put("regulatory_information", listEmptyp);
//                mapTemp.put("enterprise_report", listEmptyp);
//                mapTemp.put("disclosure_notice", listEmptyp);
//                mapTemp.put("major_event_information", listEmptyp);
//                mapTemp.put("integrity_archives", listEmptyp);
//                mapTemp.put("financial_information", listEmptyp);
//                mapTemp.put("business_information", listEmptyp);
//                mapTemp.put("expand_information", listEmptyp);
                break;

            case 3:
                //信披信息 监管信息
                List<Map> listRegulatoryInfomation = new ArrayList<>();
                Map mapRId = new HashMap();
                mapRId.put("disclosure_regulatory_report_date", date1);
                mapRId.put("disclosure_regulatory_report_name", "监管报告名称");
                mapRId.put("disclosure_regulatory_measures", "监管措施");
                mapRId.put("disclosure_regulatory_report_description", "报告简述");
//                mapRId.put("disclosure_regulatory_report_doc", getListFileObj());
                mapRId.put("disclosure_regulatory_type", 1);
                listRegulatoryInfomation.add(mapRId);

                //其他需要传List
//                mapTemp.put("enterprise_display_information", listEmptyp);
                mapTemp.put("regulatory_information", listRegulatoryInfomation);
//                mapTemp.put("enterprise_report", listEmptyp);
//                mapTemp.put("disclosure_notice", listEmptyp);
//                mapTemp.put("major_event_information", listEmptyp);
//                mapTemp.put("integrity_archives", listEmptyp);
//                mapTemp.put("financial_information", listEmptyp);
//                mapTemp.put("business_information", listEmptyp);
//                mapTemp.put("expand_information", listEmptyp);

                break;

            case 4:
                //信披信息 企业报告
                List<Map> listEnterpriseReport = new ArrayList<>();
                Map mapERd = new HashMap();
                mapERd.put("disclosure_enterprise_report_type", 1);
                mapERd.put("disclosure_enterprise_report_name", "报告名称");
//                mapERd.put("disclosure_enterprise_report_doc", getListFileObj());
                mapERd.put("disclosure_enterprise_report_description", "报告简述");
                mapERd.put("disclosure_enterprise_report_date", date1);
                listEnterpriseReport.add(mapERd);

                //其他需要传List
//                mapTemp.put("enterprise_display_information", listEmptyp);
//                mapTemp.put("regulatory_information", listEmptyp);
                mapTemp.put("enterprise_report", listEnterpriseReport);
//                mapTemp.put("disclosure_notice", listEmptyp);
//                mapTemp.put("major_event_information", listEmptyp);
//                mapTemp.put("integrity_archives", listEmptyp);
//                mapTemp.put("financial_information", listEmptyp);
//                mapTemp.put("business_information", listEmptyp);
//                mapTemp.put("expand_information", listEmptyp);

                break;
            case 5:
                //信披信息 公告
                List listNotice = new ArrayList();
                Map notice = new HashMap();
                notice.put("disclosure_announcement_type", 0);
                notice.put("disclosure_announcement_name", "公告名称");
//                notice.put("disclosure_announcement_doc", getListFileObj());
                notice.put("disclosure_announcement_description", "公告简述0001");
                notice.put("disclosure_announcement_date", date1);
                listNotice.add(notice);

                //其他需要传List
//                mapTemp.put("enterprise_display_information", listEmptyp);
//                mapTemp.put("regulatory_information", listEmptyp);
//                mapTemp.put("enterprise_report", listEmptyp);
                mapTemp.put("disclosure_notice", listNotice);
//                mapTemp.put("major_event_information", listEmptyp);
//                mapTemp.put("integrity_archives", listEmptyp);
//                mapTemp.put("financial_information", listEmptyp);
//                mapTemp.put("business_information", listEmptyp);
//                mapTemp.put("expand_information", listEmptyp);

                break;

            case 6:
                //信披信息 重大事件信息
                List listME = new ArrayList();
                Map majorEvent = new HashMap();

                majorEvent.put("disclosure_major_event_name", "事件名称");
                majorEvent.put("disclosure_major_event_type", 0);
//                majorEvent.put("disclosure_major_event_doc", getListFileObj());
                majorEvent.put("disclosure_major_event_document_description", "事件简述");
                majorEvent.put("disclosure_major_event_reporting_date", date1);
                listME.add(majorEvent);

                //其他需要传List
//                mapTemp.put("enterprise_display_information", listEmptyp);
//                mapTemp.put("regulatory_information", listEmptyp);
//                mapTemp.put("enterprise_report", listEmptyp);
//                mapTemp.put("disclosure_notice", listEmptyp);
                mapTemp.put("major_event_information", listME);
//                mapTemp.put("integrity_archives", listEmptyp);
//                mapTemp.put("financial_information", listEmptyp);
//                mapTemp.put("business_information", listEmptyp);
//                mapTemp.put("expand_information", listEmptyp);

                break;

            case 7:
                //信披信息 诚信档案
                //事项基本信息
                List<Map> listCredit = new ArrayList<>();
                Map mapCd = new HashMap();
//                mapCd.put("disclosure_identifier_ref", disclosure_identifier_ref);
//                mapCd.put("disclosure_identifier_name", "认定方名称");
//                mapCd.put("disclosure_auditor_ref", disclosure_auditor_ref);
//                mapCd.put("disclosure_auditor_name", "鉴定方名称");
                //事项明细
//                mapCd.put("disclosure_event_id", "事项编号");
                mapCd.put("disclosure_event_name", "事项名称");
//                mapCd.put("disclosure_event_doc", getListFileObj());
                mapCd.put("disclosure_event_description", "因非法开设证券期货交易场所或者组织证券期货交易被地方政府行政处罚或者采取清理整顿措施");
                mapCd.put("disclosure_event_type", 1);
//                mapCd.put("disclosure_event_valid_time", "期限00001");
//                mapCd.put("disclosure_event_start_date", date1);
//                mapCd.put("disclosure_event_end_date", date2);
                mapCd.put("disclosure_event_status", 1);
                listCredit.add(mapCd);

                //其他需要传List
//                mapTemp.put("enterprise_display_information", listEmptyp);
//                mapTemp.put("regulatory_information", listEmptyp);
//                mapTemp.put("enterprise_report", listEmptyp);
//                mapTemp.put("disclosure_notice", listEmptyp);
//                mapTemp.put("major_event_information", listEmptyp);
                mapTemp.put("integrity_archives", listCredit);
//                mapTemp.put("financial_information", listEmptyp);
//                mapTemp.put("business_information", listEmptyp);
//                mapTemp.put("expand_information", listEmptyp);

                break;

            case 8:
                //信披信息 财务信息
                List<Map> listFinancialInformation = new ArrayList<>();
                Map mapFId = new HashMap();
                //基本财务信息
//                mapFId.put("disclosure_financial_start_date", date1);
//                mapFId.put("disclosure_financial_end_date", date2);
                mapFId.put("disclosure_financial_type", 0);
//                mapFId.put("disclosure_financial_periodend_total_asset", 1000000);
//                mapFId.put("disclosure_financial_periodend_net_asset", 1000000);
//                mapFId.put("disclosure_financial_periodend_total_liability", 1000000);
//                mapFId.put("disclosure_financial_periodend_revenue", 1000000);
//                mapFId.put("disclosure_financial_periodend_gross_profit", 1000000);
//                mapFId.put("disclosure_financial_periodend_net_profit", 1000000);
//                mapFId.put("disclosure_financial_cashflow", 1000000);
//                mapFId.put("disclosure_financial_whether_rd", true);
//                mapFId.put("disclosure_financial_rd_cost", 1000000);
//                //财务报表文件
//                mapFId.put("disclosure_financial_balance_sheet_name", "资产负债表名称");
//                mapFId.put("disclosure_financial_balance_sheet", getListFileObj());
//                mapFId.put("disclosure_financial_balance_sheet_description", "资产负债表简述");
//                mapFId.put("disclosure_financial_cashflow_statement_name", "现金流量表名称");
//                mapFId.put("disclosure_financial_cashflow_statement", getListFileObj());
//                mapFId.put("disclosure_financial_cashflow_statement_description", "现金流量表简述");
//                mapFId.put("disclosure_financial_income_statement_name", "利润表名称");
//                mapFId.put("disclosure_financial_income_statement", getListFileObj());
//                mapFId.put("disclosure_financial_income_statement_description", "利润表简述");
                listFinancialInformation.add(mapFId);

                //其他需要传List
//                mapTemp.put("enterprise_display_information", listEmptyp);
//                mapTemp.put("regulatory_information", listEmptyp);
//                mapTemp.put("enterprise_report", listEmptyp);
//                mapTemp.put("disclosure_notice", listEmptyp);
//                mapTemp.put("major_event_information", listEmptyp);
//                mapTemp.put("integrity_archives", listEmptyp);
                mapTemp.put("financial_information", listFinancialInformation);
//                mapTemp.put("business_information", listEmptyp);
//                mapTemp.put("expand_information", listEmptyp);

                break;

            case 9:
                //信披信息 企业经营信息
                List listBusinessInformation = new ArrayList();
                Map mapBID = new HashMap();
                //经营基本信息
                mapBID.put("disclosure_business_overview", "概述0001");
//                mapBID.put("disclosure_main_business_analysis", "主营业务情况分析");
//                mapBID.put("disclosure_main_business_analysis_non", "非主营业务情况分析");
//                mapBID.put("disclosure_assets_and_liabilities_analysis", "资产及负债状况分析");
//                mapBID.put("disclosure_major_shareholders_analysis", "主要股东情况分析");
//                mapBID.put("disclosure_major_events", "企业重大活动情况");
                mapBID.put("disclosure_business_report_name", "经营报告名称");
//                mapBID.put("disclosure_business_report_doc", getListFileObj());
                mapBID.put("disclosure_business_report_description","经营报告简述");
                mapBID.put("disclosure_business_report_date", date1);
                //投融资信息
//                mapBID.put("disclosure_investment_analysis", "投资情况分析");
//                mapBID.put("disclosure_fund_raising_analysis", "募集资金情况分析");
//                mapBID.put("disclosure_sell_situation_analysis", "重大资产和股权出售情况");
                listBusinessInformation.add(mapBID);

                //其他需要传List
//                mapTemp.put("enterprise_display_information", listEmptyp);
//                mapTemp.put("regulatory_information", listEmptyp);
//                mapTemp.put("enterprise_report", listEmptyp);
//                mapTemp.put("disclosure_notice", listEmptyp);
//                mapTemp.put("major_event_information", listEmptyp);
//                mapTemp.put("integrity_archives", listEmptyp);
//                mapTemp.put("financial_information", listEmptyp);
                mapTemp.put("business_information", listBusinessInformation);
//                mapTemp.put("expand_information", listEmptyp);

                break;

            case 10:
                //信披信息 第三方拓展信息
                List listExpandInformation = new ArrayList();
                Map mapEID = new HashMap();
                mapEID.put("disclosure_expand_name", "拓展信息名称");
//                mapEID.put("disclosure_expand_doc", getListFileObj());
                mapEID.put("disclosure_expand_description", "拓展信息简述");
                mapEID.put("disclosure_expand_date", date1);
//                listExpandInformation.add(mapEID);

                //其他需要传List
//                mapTemp.put("enterprise_display_information", listEmptyp);
//                mapTemp.put("regulatory_information", listEmptyp);
//                mapTemp.put("enterprise_report", listEmptyp);
//                mapTemp.put("disclosure_notice", listEmptyp);
//                mapTemp.put("major_event_information", listEmptyp);
//                mapTemp.put("integrity_archives", listEmptyp);
//                mapTemp.put("financial_information", listEmptyp);
//                mapTemp.put("business_information", listEmptyp);
                mapTemp.put("expand_information", listExpandInformation);
            break;
        }


        return mapTemp;
    }

    public void initCommonRefSubAndReg() {

        log.info("初始化监管引用相关数据结构");
//        resetRefObjectId();
        Map mapTemp = init01EnterpriseSubjectInfo();
        String refDataArray[]=refData();

        for (int i=0;i<refDataArray.length;i++){
            mapTemp.put("subject_object_id",refDataArray[i]);
//            mapTemp.remove("subject_qualification_information");
            ArrayList aSqi = new ArrayList();
            mapTemp.put("subject_qualification_information",aSqi);
            log.info(mapTemp.toString());
            gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,1000,mapTemp,null,null,null);
        }

//        for (int i=0;i<refDataArray.length;i++){
//            String response = gd.GDMainSubjectQuery(gdContractAddress, refDataArray[i]);
//            assertEquals(true,response.contains(refDataArray[i]));
//        }
    }

    public void updateCommonRefSubAndReg() {

        log.info("初始化监管引用相关数据结构");
//        resetRefObjectId();
        Map mapTemp = init01EnterpriseSubjectInfo();
        String refDataArray[]=refData();

        for (int i=0;i<refDataArray.length;i++){
            mapTemp.put("subject_object_id",refDataArray[i]);
//            mapTemp.remove("subject_qualification_information");
            ArrayList aSqi = new ArrayList();
            mapTemp.put("subject_qualification_information",aSqi);
            log.info(mapTemp.toString());
            gd.GDUpdateSubjectInfo(gdContractAddress,0,mapTemp);
        }

//        for (int i=0;i<refDataArray.length;i++){
//            String response = gd.GDMainSubjectQuery(gdContractAddress, refDataArray[i]);
//            assertEquals(true,response.contains(refDataArray[i]));
//        }
    }

    public void resetRefObjectId(){
        //主体
        subject_investor_qualification_certifier_ref = "SIQCR" + UtilsClass.Random(9);
        //账户
        account_subject_ref = "ASR" + UtilsClass.Random(9);
        account_depository_ref = "ADR" + UtilsClass.Random(9);
//        account_associated_account_ref = "AAAR" + UtilsClass.Random(9);
        //产品
        product_market_subject_ref = "PMSR" + UtilsClass.Random(9);
        product_issuer_subject_ref = "PISR" + UtilsClass.Random(9);
        service_provider_subject_ref = "SPSR" + UtilsClass.Random(9);

        //交易报告
        transaction_custody_product_ref = "";//"TCPR" + UtilsClass.Random(9);
        transaction_product_issuer_ref = "TPIR" + UtilsClass.Random(9);
        transaction_issuer_ref = "TIssR" + UtilsClass.Random(9);
        transaction_investor_ref = "TIR" + UtilsClass.Random(9);
        transaction_investor_original_ref = "TIOR" + UtilsClass.Random(9);
        transaction_investor_counterparty_ref = "TICR" + UtilsClass.Random(9);
        transaction_intermediary_subject_ref = "TISR" + UtilsClass.Random(9);
        //登记
        register_subject_ref = "RSR" + UtilsClass.Random(9);
        register_subject_account_ref = "RSAR" + UtilsClass.Random(9);
        register_transaction_ref = "RTR" + UtilsClass.Random(9);
        register_product_ref = "";//"RPR" + UtilsClass.Random(9);
        register_right_recognition_subject_ref = "RRRSR" + UtilsClass.Random(9);
        register_right_recognition_agent_subject_ref = "RRRASR" + UtilsClass.Random(9);
        roll_register_subject_ref = "RRSR" + UtilsClass.Random(9);
        roll_register_product_ref = "";//"RRPR" + UtilsClass.Random(9);
        register_equity_subject_ref = "RESR" + UtilsClass.Random(9);
        register_debt_holder_ref = "RDHR" + UtilsClass.Random(9);
        register_investor_subject_ref = "RISR" + UtilsClass.Random(9);
        //资金结算
        settlement_subject_ref = "SSR" + UtilsClass.Random(9);
        settlement_product_ref = "";//"SPR" + UtilsClass.Random(9);
        settlement_transaction_ref = "STR" + UtilsClass.Random(9);
        settlement_out_account_object_ref = "SOAOR" + UtilsClass.Random(9);
        settlement_in_account_object_ref = "SIAOR" + UtilsClass.Random(9);
        //信披
        disclosure_subject_ref = "DSR" + UtilsClass.Random(9);
        disclosure_referer_subject_ref = "DRSR" + UtilsClass.Random(9);
        disclosure_display_platform_ref = "DDPR" + UtilsClass.Random(9);
        disclosure_identifier_ref = "DIR" + UtilsClass.Random(9);
        disclosure_auditor_ref = "DAR" + UtilsClass.Random(9);
    }

}
