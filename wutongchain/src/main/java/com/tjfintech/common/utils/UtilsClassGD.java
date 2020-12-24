package com.tjfintech.common.utils;

import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import sun.reflect.generics.tree.VoidDescriptor;

import java.util.*;

import static com.tjfintech.common.utils.UtilsClass.testDataPath;


@Slf4j
public class UtilsClassGD {

    //gudeng 信息
//    public static String gdContractAddress = "faa54ef5e71e8f69cb30af684ae9f145b5c099ed4253a49fc1b144db1fe27f3c";
//    public static String gdPlatfromKeyID = "btguv5bsnk7r259qh85g";
    //10.1.3.161:7779
    public static String gdContractAddress = "018a976954474678a3a100d33200b7f9fd1b34a60118c4cb13cfd79f6f324b5d";
    public static String gdPlatfromKeyID = "bv2q01d2uehbsbgg4u50";
    //股登164环境
//    public static String gdContractAddress = "48cb62af2f6363a5088264ce41193a362455b27fedb1d72eb512a7bfeb339523";
//    public static String gdPlatfromKeyID = "bu1qdg1pgfltc7no3hp0";
    //股登性能测试环境
//    public static String gdContractAddress = "e0618bd8a18d799e18d6716824e4b767e381d2ce58a49be20c89b48b53cff900";
//    public static String gdPlatfromKeyID = "bv7h273snk7pq9fpgr70";
    //外网对接
//    public static String gdContractAddress = "48cb62af2f6363a5088264ce41193a362455b27fedb1d72eb512a7bfeb339523";
//    public static String gdPlatfromKeyID = "bv6t8lphdkebd3873ti0";
    public static String gdEquityCode = "gdTokenZ0" + UtilsClass.Random(6);
    public static String gdCompanyID = "gdCmpyId01" + UtilsClass.Random(6);
//    public static String gdCompanyID = "gdCmpyId0002";

    public static String gdAccClientNo1 = "No000" + UtilsClass.Random(5);
    public static String gdAccClientNo2 = "No100" + UtilsClass.Random(5);
    public static String gdAccClientNo3 = "No200" + UtilsClass.Random(5);
    public static String gdAccClientNo4 = "No300" + UtilsClass.Random(5);
    public static String gdAccClientNo5 = "No400" + UtilsClass.Random(5);
    public static String gdAccClientNo6 = "No500" + UtilsClass.Random(5);
    public static String gdAccClientNo7 = "No600" + UtilsClass.Random(5);
    public static String gdAccClientNo8 = "No700" + UtilsClass.Random(5);
    public static String gdAccClientNo9 = "No800" + UtilsClass.Random(5);
    public static String gdAccClientNo10 = "No900" + UtilsClass.Random(5);

    public static String gdAccount1,gdAccount2,gdAccount3,gdAccount4,gdAccount5,
                         gdAccount6,gdAccount7,gdAccount8,gdAccount9,gdAccount10;
    public static String gdAccountKeyID1,gdAccountKeyID2,gdAccountKeyID3,gdAccountKeyID4,gdAccountKeyID5,
                         gdAccountKeyID6,gdAccountKeyID7,gdAccountKeyID8,gdAccountKeyID9,gdAccountKeyID10;

    public static Map mapAccAddr = new HashMap<>(); //存放地址和客户号（账户对象标识 主体标识）
    public static Map mapAddrRegObjId = new HashMap<>(); //存放地址和登记主体id

    public static Map registerInfo = new HashMap();//05登记 //发行 股份性质变更 过户转让 股份增发
    public static Map txInformation = new HashMap();//04交易报告  //过户转让
    public static Map enterpriseSubjectInfo = new HashMap();//01主体  //挂牌企业登记
    public static Map equityProductInfo = new HashMap();//03产品 股权类 //挂牌企业登记  股份增发 场内转板
    public static Map bondProductInfo = new HashMap();//03产品 债券类 //挂牌企业登记  股份发行
    public static Map fundProductInfo = new HashMap();//03产品 基金类 //挂牌企业登记  股份发行
    public static Map shAccountInfo = new HashMap();//02账户 股权账户 //投资者开户
    public static Map fundAccountInfo = new HashMap();//02账户 资金账户 //投资者开户
    public static Map investorSubjectInfo = new HashMap();//01主体  //投资者开户
    public static Map disclosureInfo = new HashMap();//07信披  //写入公告 信息披露
    public static Map settleInfo = new HashMap();//06资金结算  //资金结算
    public static List<Map> listRegInfo = new ArrayList<>();//主体信息列表
    public static String regNo = "regNo000000";


    public static long start = (new Date()).getTime();
    public static long end = 0;
    public static int beginHeigh = 0;
    public static int endHeight = 0;
	public static int blockHeight = 4892;
    public static long timeStamp = (new Date()).getTime();
    public static String testCurMethodName = "";

    public static String gdJGModelProtocol = "上海区域股权市场跨链监管业务数据模型";
    public static String gdJGModelVersion = "2.0.0-alpha3";

    public static String subjectType = "subject";
    public static String accType = "account";
    public static String accSHType = "accountSH";
    public static String prodType = "product";
    public static String txrpType = "transactionreport";
    public static String regType = "registration";
    public static String settleType = "settlement";
    public static String infoType = "infodisclosure";

//    public static String time1 = "2017/12/16 13:40:00";
//    public static String time2 = "2020/12/16 12:00:25";
//    public static String time3 = "2024/12/16 13:50:00";
//    public static String time4 = "2028/11/10 10:50:10";
//
//    public static String date1 = "2018/10/23";
//    public static String date2 = "2019/12/21";
//    public static String date3 = "2025/12/16";
//    public static String date4 = "2029/11/10";

    public static String time1 = "2017-12-16T13:40:00+08:00";
    public static String time2 = "2020-12-16T12:00:25+08:00";
    public static String time3 = "2024-12-16T13:50:00+08:00";
    public static String time4 = "2028-11-10T10:50:10+08:00";

    public static String date1 = "2018-10-23";
    public static String date2 = "2019-12-21";
    public static String date3 = "2025-12-16";
    public static String date4 = "2029-11-10";

    public static long ts1 = 1513374000;
    public static long ts2 = 1608062425;
    public static long ts3 = 1734299400;
    public static long ts4 = 1857408610;
    public static long ts5 = 1513374000;
    public static long ts6 = 1513374000;
    public static long ts7 = 1513374000;
    public static long ts8 = 1888934400;


    public static String gdSchema = "Schema-v2.0.0-Alpha4.2.json" ;
    public static String gdTestData = "test.json";
    public static String chkSchemaToolName = "gojsonschema.exe";
    public static String dirSchemaData = System.getProperty("user.dir") + "/" + testDataPath + "schemavalidate/";
//    public static Boolean bChkHeader = true;

    public static String updateWord = "";

    public static Integer disclosureType = 1;

    public static String minIOUser ="minioadmin";
    public static String minIOPwd ="Pass@7899";
    public static String minIOEP ="http://b2904236d6.zicp.vip:8098";
    public static String jgBucket = "my-oss-test";

    public static Boolean bRegTxRef = false;
    public static Boolean bHeaderCalOK = true;
    public static String CNKey = "Eq1";
    public static String uriStoreData = "";
    public static String productType = "1";

    public static  HashMap<String,String> refInfo = new HashMap<String,String>();

    //获取引用信息初始值放入数组
    public static  String[] refData() {
        refInfo.clear();
        //主体
        refInfo.put("subject_investor_qualification_certifier_ref", subject_investor_qualification_certifier_ref);
        //账户
        refInfo.put("account_subject_ref", account_subject_ref);
        refInfo.put("account_depository_ref", account_depository_ref);
        refInfo.put("account_associated_account_ref", account_associated_account_ref);
        //产品
        refInfo.put("product_market_subject_ref", product_market_subject_ref);
        refInfo.put("product_issuer_subject_ref", product_issuer_subject_ref);
        refInfo.put("service_provider_subject_ref", service_provider_subject_ref);
        refInfo.put("product_conversion_price_ref", product_conversion_price_ref);
        //交易报告
        refInfo.put("transaction_custody_product_ref", transaction_custody_product_ref);
        refInfo.put("transaction_product_issuer_ref", transaction_product_issuer_ref);
        refInfo.put("transaction_issuer_ref", transaction_issuer_ref);
        refInfo.put("transaction_investor_ref", transaction_investor_ref);
        refInfo.put("transaction_investor_original_ref", transaction_investor_original_ref);
        refInfo.put("transaction_investor_counterparty_ref", transaction_investor_counterparty_ref);
        refInfo.put("transaction_intermediary_subject_ref", transaction_intermediary_subject_ref);
        //登记
        refInfo.put("register_subject_ref", register_subject_ref);
        refInfo.put("register_subject_account_ref", register_subject_account_ref);
        refInfo.put("register_transaction_ref", register_transaction_ref);
        refInfo.put("register_product_ref", register_product_ref);
        refInfo.put("register_right_recognition_subject_ref", register_right_recognition_subject_ref);
        refInfo.put("register_right_recognition_agent_subject_ref", register_right_recognition_agent_subject_ref);
        refInfo.put("roll_register_subject_ref", roll_register_subject_ref);
        refInfo.put("roll_register_product_ref", roll_register_product_ref);
        refInfo.put("register_equity_subject_ref",register_equity_subject_ref);
        refInfo.put("register_debt_holder_ref", register_debt_holder_ref);
        refInfo.put("register_investor_subject_ref", register_investor_subject_ref);
        //资金结算
        refInfo.put("settlement_subject_ref", settlement_subject_ref);
        refInfo.put("settlement_product_ref", settlement_product_ref);
        refInfo.put("settlement_transaction_ref", settlement_transaction_ref);
        refInfo.put("settlement_out_account_object_ref", settlement_out_account_object_ref);
        refInfo.put("settlement_in_account_object_ref", settlement_in_account_object_ref);
        //信披
        refInfo.put("disclosure_subject_ref", disclosure_subject_ref);
        refInfo.put("disclosure_referer_subject_ref", disclosure_referer_subject_ref);
        refInfo.put("disclosure_display_platform_ref", disclosure_display_platform_ref);
        refInfo.put("disclosure_identifier_ref", disclosure_identifier_ref);
        refInfo.put("disclosure_auditor_ref", disclosure_auditor_ref);

        String refInfoArray[] = new String[refInfo.size()];
        int i=0;
        for(String value: refInfo.values()) {
            refInfoArray[i] = value;
            i++;
        }
        return refInfoArray;
    }
    public static String commNo = "011"; //存oss后会导致 db删除后挂牌会挂不上 1011
    //主体
    public static String subject_investor_qualification_certifier_ref = "SIQCR" + commNo;// + UtilsClass.Random(8);
    //账户
    public static String account_subject_ref = "ASR" + commNo;// + UtilsClass.Random(8);
    public static String account_depository_ref = "ADR" + commNo;// + UtilsClass.Random(8);
    public static String account_associated_account_ref = "AAAR" + commNo;// + UtilsClass.Random(8);
    //产品
    public static String product_market_subject_ref = "PMSR" + commNo;// + UtilsClass.Random(8);
    public static String product_issuer_subject_ref = "PISR" + commNo;// + UtilsClass.Random(8);
    public static String service_provider_subject_ref = "SPSR" + commNo;// + UtilsClass.Random(8);
    public static String product_conversion_price_ref = "PCPR" + commNo;// + UtilsClass.Random(8);
    //交易报告
    public static String transaction_custody_product_ref = "TCPR" + commNo;// + UtilsClass.Random(8);
    public static String transaction_product_issuer_ref = "TPIR" + commNo;// + UtilsClass.Random(8);
    public static String transaction_issuer_ref = "TIssR" + commNo;// + UtilsClass.Random(8);
    public static String transaction_investor_ref = "TIR" + commNo;// + UtilsClass.Random(8);
    public static String transaction_investor_original_ref = "TIOR" + commNo;// + UtilsClass.Random(8);
    public static String transaction_investor_counterparty_ref = "TICR" + commNo;// + UtilsClass.Random(8);
    public static String transaction_intermediary_subject_ref = "TISR" + commNo;// + UtilsClass.Random(8);
    //登记
    public static String register_subject_ref = "RSR" + commNo;// + UtilsClass.Random(8);
    public static String register_subject_account_ref = "RSAR" + commNo;// + UtilsClass.Random(8);
    public static String register_transaction_ref = "RTR" + commNo;// + UtilsClass.Random(8);
    public static String register_product_ref = "RPR" + commNo;// + UtilsClass.Random(8);
    public static String register_right_recognition_subject_ref = "RRRSR" + commNo;// + UtilsClass.Random(8);
    public static String register_right_recognition_agent_subject_ref = "RRRASR" + commNo;// + UtilsClass.Random(8);
    public static String roll_register_subject_ref = "RRSR" + commNo;// + UtilsClass.Random(8);
    public static String roll_register_product_ref = "RRPR" + commNo;// + UtilsClass.Random(8);
    public static String register_equity_subject_ref = "RESR" + commNo;// + UtilsClass.Random(8);
    public static String register_debt_holder_ref = "RDHR" + commNo;// + UtilsClass.Random(8);
    public static String register_investor_subject_ref = "RISR" + commNo;// + UtilsClass.Random(8);
    //资金结算
    public static String settlement_subject_ref = "SSR" + commNo;// + UtilsClass.Random(8);
    public static String settlement_product_ref = "SPR" + commNo;// + UtilsClass.Random(8);
    public static String settlement_transaction_ref = "STR" + commNo;// + UtilsClass.Random(8);
    public static String settlement_out_account_object_ref = "SOAOR" + commNo;// + UtilsClass.Random(8);
    public static String settlement_in_account_object_ref = "SIAOR" + commNo;// + UtilsClass.Random(8);
    //信披
    public static String disclosure_subject_ref = "DSR" + commNo;// + UtilsClass.Random(8);
    public static String disclosure_referer_subject_ref = "DRSR" + commNo;// + UtilsClass.Random(8);
    public static String disclosure_display_platform_ref = "DDPR" + commNo;// + UtilsClass.Random(8);
    public static String disclosure_identifier_ref = "DIR" + commNo;// + UtilsClass.Random(8);
    public static String disclosure_auditor_ref = "DAR" + commNo;// + UtilsClass.Random(8);

    public static Map fileObj = new HashMap();
    public static List<Map> listFileObj = new ArrayList<>();

    public static Map getFileObj(String word){
        //{"file_number":"1","file_name": "12312312","url": "12312312","hash": "12312312","summary": "12312312","term_of_validity_type": "0","term_of_validity":"yyyy/MM/dd"}
        //文件对象
        Map fileMap = new HashMap();
        fileMap.put("file_number","2");
        fileMap.put("file_name",word + "file1.pdf");
        fileMap.put("hash","da1234filehash5222" + word);
        fileMap.put("url","http://test.com/file/201/" + word + "file1.pdf");
        fileMap.put("summary","简述" + word);
        fileMap.put("term_of_validity_type","0");
        fileMap.put("term_of_validity",date1);
        return fileMap;
    }
    public static Map getFileObj(){
        //{"file_number":"1","file_name": "12312312","url": "12312312","hash": "12312312","summary": "12312312","term_of_validity_type": "0","term_of_validity":"yyyy/MM/dd"}
        //文件对象
        Map fileMap = new HashMap();
        fileMap.put("file_number","1");
        fileMap.put("file_name","file1.pdf");
        fileMap.put("hash","da1234filehash5222");
        fileMap.put("url","http://test.com/file/201/file1.pdf");
        fileMap.put("summary","简述");
        fileMap.put("term_of_validity_type","0");
        fileMap.put("term_of_validity",date2);
        return fileMap;
    }

    public static List<Map> getListFileObj(){
        List<Map> listFileObj = new ArrayList<>();
        //{"file_number":"1","file_name": "12312312","url": "12312312","hash": "12312312","summary": "12312312","term_of_validity_type": "0","term_of_validity":"yyyy/MM/dd"}
        //文件对象
        listFileObj.add(getFileObj("1"));
        listFileObj.add(getFileObj("2"));
        return listFileObj;
    }
}
