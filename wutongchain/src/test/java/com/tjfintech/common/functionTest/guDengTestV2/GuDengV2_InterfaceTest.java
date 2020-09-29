//package com.tjfintech.common.functionTest.guDengTestV2;
//
//import com.tjfintech.common.CommonFunc;
//import com.tjfintech.common.GDBeforeCondition;
//import com.tjfintech.common.Interface.GuDeng;
//import com.tjfintech.common.Interface.GuDengV1;
//import com.tjfintech.common.Interface.Store;
//import com.tjfintech.common.TestBuilder;
//import com.tjfintech.common.utils.UtilsClass;
//import lombok.extern.slf4j.Slf4j;
//import net.sf.json.JSONObject;
//import org.junit.BeforeClass;
//import org.junit.FixMethodOrder;
//import org.junit.Test;
//import org.junit.runners.MethodSorters;
//import org.springframework.util.StringUtils;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import static com.tjfintech.common.utils.UtilsClass.*;
//import static com.tjfintech.common.utils.UtilsClassGD.*;
//import static org.junit.Assert.assertEquals;
//
//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
//@Slf4j
//public class GuDengV2_InterfaceTest {
//
//    TestBuilder testBuilder= TestBuilder.getInstance();
//    GuDeng gd =testBuilder.getGuDeng();
//    UtilsClass utilsClass = new UtilsClass();
//    CommonFunc commonFunc = new CommonFunc();
//    Store store =testBuilder.getStore();
//
//    @BeforeClass
//    public static void Before()throws Exception{
//        GDBeforeCondition gdBefore = new GDBeforeCondition();
//        gdBefore.gdCreateAccout();
//    }
//
//
//    @Test
//    public void enterpriseIssueInterfaceTest() throws Exception {
//
//        Map mapBaseInfo = new HashMap();
//        mapBaseInfo.put("equityCode","SZ100001");
//        mapBaseInfo.put("equitySimpleName","苏州股权代码");
//        mapBaseInfo.put("equityType",0);
//        mapBaseInfo.put("totalShares",1000000);
//        mapBaseInfo.put("companyFullName","苏州同济区块链研究");
//        mapBaseInfo.put("eNName","Suzhou Tongji Blockchain Research Institute");
//        mapBaseInfo.put("certificateType",0);
//        mapBaseInfo.put("certificateNo","1585685245666821236");
//        mapBaseInfo.put("currency","人民币");
//        mapBaseInfo.put("companyId","1598222555555");
//        mapBaseInfo.put("companyLogo","苏州同济区块链研究");
//        mapBaseInfo.put("companySimpleName","苏同院");
//        mapBaseInfo.put("companyENName","tjfoc");
//        mapBaseInfo.put("pinyinCode","suzhoutongjiqukuailianyanjiuyuan");
//        mapBaseInfo.put("txStatus",0);
//        mapBaseInfo.put("listingDate","2018-09-01 18:56:56");
//        mapBaseInfo.put("delistingDate","2018-09-01 18:56:56");
//        mapBaseInfo.put("industry","互联网");
//        mapBaseInfo.put("enterpriseStatus",0);
//
//        Map mapBzInfo = new HashMap();
//        mapBzInfo.put("province","江苏省");
//        mapBzInfo.put("city","苏州");
//        mapBzInfo.put("registeredAddress","相城区");
//        mapBzInfo.put("officeAddress","领寓商务");
//        mapBzInfo.put("contactAddress","相城区领寓商务广场12楼");
//        mapBzInfo.put("phone","051261116444");
//        mapBzInfo.put("fax","051261116444");
//        mapBzInfo.put("postalCode","621552");
//        mapBzInfo.put("internetAddress","www.wutongchain.com");
//        mapBzInfo.put("mailBox","test@wutongchain.com");
//        mapBzInfo.put("registeredCapital","1000000");
//
//        Map maplegalPersonInfo = new HashMap();
//        maplegalPersonInfo.put("name","苏同院");
//        maplegalPersonInfo.put("certificateType",0);
//        maplegalPersonInfo.put("certificateNo","123456789684984");
//        maplegalPersonInfo.put("job","test");
//        maplegalPersonInfo.put("legalPersonPhone","15685297828");
//
//        String extend = "{\"city\":\"苏州\"}";
//
//        //验证接口正常
//        String response= gd.GDEnterpriseResister(gdContractAddress,mapBaseInfo,mapBzInfo,maplegalPersonInfo,extend);
//        JSONObject jsonObject=JSONObject.fromObject(response);
//        String txId = jsonObject.getJSONObject("data").getString("txId");
//        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));
//
//        //BasicInfo.equityCode为空
//        mapBaseInfo.put("equityCode","");
//        response= gd.GDEnterpriseResister(gdContractAddress,mapBaseInfo,mapBzInfo,maplegalPersonInfo,extend);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("Error:Field validation for 'EquityCode' failed on the 'required"));
//
//        //BasicInfo.equitySimpleName为空
//        mapBaseInfo.put("equityCode","SZ100001");
//        mapBaseInfo.put("equitySimpleName","");
//        response= gd.GDEnterpriseResister(gdContractAddress,mapBaseInfo,mapBzInfo,maplegalPersonInfo,extend);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("Error:Field validation for 'EquitySimpleName' failed on the 'required"));
//
//        //BasicInfo.companyFullName为空
//        mapBaseInfo.put("equitySimpleName","苏州股权代码");
//        mapBaseInfo.put("companyFullName","");
//        response= gd.GDEnterpriseResister(gdContractAddress,mapBaseInfo,mapBzInfo,maplegalPersonInfo,extend);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("Error:Field validation for 'CompanyFullName' failed on the 'required"));
//
//        //BasicInfo.eNName为空
//        mapBaseInfo.put("companyFullName","苏州同济区块链研究");
//        mapBaseInfo.put("eNName","");
//        response= gd.GDEnterpriseResister(gdContractAddress,mapBaseInfo,mapBzInfo,maplegalPersonInfo,extend);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("Error:Field validation for 'ENName' failed on the 'required"));
//
//        //BasicInfo.certificateNo为空
//        mapBaseInfo.put("eNName","Suzhou Tongji Blockchain Research Institute");
//        mapBaseInfo.put("certificateNo","");
//        response= gd.GDEnterpriseResister(gdContractAddress,mapBaseInfo,mapBzInfo,maplegalPersonInfo,extend);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("Error:Field validation for 'CertificateNo' failed on the 'required"));
//
//        //BasicInfo.currency为空
//        mapBaseInfo.put("certificateNo","1585685245666821236");
//        mapBaseInfo.put("currency","");
//        response= gd.GDEnterpriseResister(gdContractAddress,mapBaseInfo,mapBzInfo,maplegalPersonInfo,extend);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("Error:Field validation for 'Currency' failed on the 'required"));
//
//        //BasicInfo.companyId为空
//        mapBaseInfo.put("currency","人民币");
//        mapBaseInfo.put("companyId","");
//        response= gd.GDEnterpriseResister(gdContractAddress,mapBaseInfo,mapBzInfo,maplegalPersonInfo,extend);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("Error:Field validation for 'CompanyId' failed on the 'required"));
//
//        //BasicInfo.listingDate为空
//        mapBaseInfo.put("companyId","1598222555555");
//        mapBaseInfo.put("listingDate","");
//        response= gd.GDEnterpriseResister(gdContractAddress,mapBaseInfo,mapBzInfo,maplegalPersonInfo,extend);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("Error:Field validation for 'ListingDate' failed on the 'required"));
//
//        //BasicInfo.delistingDate为空
//        mapBaseInfo.put("listingDate","2018-09-01 18:56:56");
//        mapBaseInfo.put("delistingDate","");
//        response= gd.GDEnterpriseResister(gdContractAddress,mapBaseInfo,mapBzInfo,maplegalPersonInfo,extend);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("Error:Field validation for 'DelistingDate' failed on the 'required"));
//
//        //LegalPersonInfo.name为空
//        maplegalPersonInfo.put("delistingDate","2018-09-01 18:56:56");
//        maplegalPersonInfo.put("name","");
//        response= gd.GDEnterpriseResister(gdContractAddress,mapBaseInfo,mapBzInfo,maplegalPersonInfo,extend);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("Error:Field validation for 'Name' failed on the 'required"));
//
//        //LegalPersonInfo.certificateNo为空
//        maplegalPersonInfo.put("name","苏同院");
//        maplegalPersonInfo.put("certificateNo","");
//        response= gd.GDEnterpriseResister(gdContractAddress,mapBaseInfo,mapBzInfo,maplegalPersonInfo,extend);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("Error:Field validation for 'CertificateNo' failed on the 'required"));
//
//        //gdContractAddress为空
//        maplegalPersonInfo.put("certificateNo","1585685245666821236");
//        response= gd.GDEnterpriseResister("",mapBaseInfo,mapBzInfo,maplegalPersonInfo,extend);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("Error:Field validation for 'ContractAddress' failed on the 'required"));
//
//
//        //BasicInfo.totalShares不传参
//        mapBaseInfo.clear();
//        mapBaseInfo.put("equityCode","SZ100001");
//        mapBaseInfo.put("equitySimpleName","苏州股权代码");
//        mapBaseInfo.put("equityType",0);
//
//        mapBaseInfo.put("companyFullName","苏州同济区块链研究");
//        mapBaseInfo.put("eNName","Suzhou Tongji Blockchain Research Institute");
//        mapBaseInfo.put("certificateType",0);
//        mapBaseInfo.put("certificateNo","1585685245666821236");
//        mapBaseInfo.put("currency","人民币");
//        mapBaseInfo.put("companyId","1598222555555");
//        mapBaseInfo.put("companyLogo","苏州同济区块链研究");
//        mapBaseInfo.put("companySimpleName","苏同院");
//        mapBaseInfo.put("companyENName","tjfoc");
//        mapBaseInfo.put("pinyinCode","suzhoutongjiqukuailianyanjiuyuan");
//        mapBaseInfo.put("txStatus",0);
//        mapBaseInfo.put("listingDate","2018-09-01 18:56:56");
//        mapBaseInfo.put("delistingDate","2018-09-01 18:56:56");
//        mapBaseInfo.put("industry","互联网");
//        mapBaseInfo.put("enterpriseStatus",0);
//        response= gd.GDEnterpriseResister(gdContractAddress,mapBaseInfo,mapBzInfo,maplegalPersonInfo,extend);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("Error:Field validation for 'TotalShares' failed on the 'gt' tag"));
//
//    }
//
//    @Test
//    public void enterpriseIssueInterfaceNotNecessaryTest() throws Exception {
//
//        Map mapBaseInfo = new HashMap();
//        mapBaseInfo.put("equityCode","SZ100001");
//        mapBaseInfo.put("equitySimpleName","苏州股权代码");
////        mapBaseInfo.put("equityType",0);
//        mapBaseInfo.put("totalShares",1000000);
//        mapBaseInfo.put("companyFullName","苏州同济区块链研究");
//        mapBaseInfo.put("eNName","Suzhou Tongji Blockchain Research Institute");
////        mapBaseInfo.put("certificateType",0);
//        mapBaseInfo.put("certificateNo","1585685245666821236");
//        mapBaseInfo.put("currency","人民币");
//        mapBaseInfo.put("companyId","1598222555555");
////        mapBaseInfo.put("companyLogo","苏州同济区块链研究");
////        mapBaseInfo.put("companySimpleName","苏同院");
////        mapBaseInfo.put("companyENName","tjfoc");
////        mapBaseInfo.put("pinyinCode","suzhoutongjiqukuailianyanjiuyuan");
////        mapBaseInfo.put("txStatus",0);
//        mapBaseInfo.put("listingDate","2018-09-01 18:56:56");
//        mapBaseInfo.put("delistingDate","2018-09-01 18:56:56");
////        mapBaseInfo.put("industry","互联网");
////        mapBaseInfo.put("enterpriseStatus",0);
//
//        Map mapBzInfo = new HashMap();
////        mapBzInfo.put("province","江苏省");
////        mapBzInfo.put("city","苏州");
////        mapBzInfo.put("registeredAddress","相城区");
////        mapBzInfo.put("officeAddress","领寓商务");
////        mapBzInfo.put("contactAddress","相城区领寓商务广场12楼");
////        mapBzInfo.put("phone","051261116444");
////        mapBzInfo.put("fax","051261116444");
////        mapBzInfo.put("postalCode","621552");
////        mapBzInfo.put("internetAddress","www.wutongchain.com");
////        mapBzInfo.put("mailBox","test@wutongchain.com");
////        mapBzInfo.put("registeredCapital","1000000");
//
//        Map maplegalPersonInfo = new HashMap();
//        maplegalPersonInfo.put("name","苏同院");
////        maplegalPersonInfo.put("certificateType",0);
//        maplegalPersonInfo.put("certificateNo","123456789684984");
////        maplegalPersonInfo.put("job","test");
////        maplegalPersonInfo.put("legalPersonPhone","15685297828");
//
//        String extend = "{\"city\":\"苏州\"}";
//
//        //验证接口正常
//        String response= gd.GDEnterpriseResister(gdContractAddress,mapBaseInfo,mapBzInfo,maplegalPersonInfo,"");
//        JSONObject jsonObject=JSONObject.fromObject(response);
//        String txId = jsonObject.getJSONObject("data").getString("txId");
//        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));
//
//    }
//
//    @Test
//    public void createAccoutInterfaceMustParamTest() throws Exception {
//
//        String cltNo = "11212" + Random(6);
//        String shareHolderNo = "sh" + cltNo;
//        String eqCode = "createAccMust" + Random(2);
//
//        Map mapPersonInfo = new HashMap();
//        mapPersonInfo.put("clientFullName",eqCode);
//        mapPersonInfo.put("organizationType","苏州股权代码");
//        mapPersonInfo.put("certificateType",0);
//        mapPersonInfo.put("certificateNo","123456468123153");
//        mapPersonInfo.put("certificateAddress","certificateAddress");
//        mapPersonInfo.put("gender",0);
//        mapPersonInfo.put("telephone","1598222555555");
//        mapPersonInfo.put("phone","1598222555555");
//        mapPersonInfo.put("postalCode","1585685245666821236");
//        mapPersonInfo.put("contactAddress","人民币");
//        mapPersonInfo.put("mailBox","1598222555555");
//        mapPersonInfo.put("fax","苏州同济区块链研究");
//        mapPersonInfo.put("equityCode","苏同院");
//        mapPersonInfo.put("equityAmount",5000);
//        mapPersonInfo.put("shareProperty",0);
//
//        Map mapinvestor = new HashMap();
//        mapinvestor.put("salesDepartment","业务一部");
//        mapinvestor.put("clientGroups","群组");
//        mapinvestor.put("equityAccountNo","111111");
//        mapinvestor.put("currency","人民币");
//        mapinvestor.put("board","E板");
//        mapinvestor.put("accountType",0);
//        mapinvestor.put("accountStatus",0);
//        mapinvestor.put("registrationDate","621552");
//        mapinvestor.put("lastTradingDate","20200828");
//        mapinvestor.put("closingDate","20200828");
//        mapinvestor.put("shareholderAmount",3);
//
//        String extend = "";
//
//        Map mapInvestorInfo = new HashMap();
//
//        mapInvestorInfo.put("clientName","034654");
//        mapInvestorInfo.put("shareholderNo",shareHolderNo);
//        mapInvestorInfo.put("fundNo","f0000001");
//        mapInvestorInfo.put("clientNo",cltNo);
//        mapInvestorInfo.put("extend",extend);
//        mapInvestorInfo.put("personalInfo",mapPersonInfo);
//        mapInvestorInfo.put("investor",mapinvestor);
//
//
//        String response= gd.GDCreateAccout(gdContractAddress,mapInvestorInfo);
//        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
//        assertEquals(cltNo,JSONObject.fromObject(response).getJSONObject("data").getJSONObject("accountList").getString("clientNo"));
//        assertEquals(shareHolderNo,JSONObject.fromObject(response).getJSONObject("data").getJSONObject("accountList").getString("shareholderNo"));
//        String keyId = JSONObject.fromObject(response).getJSONObject("data").getJSONObject("accountList").getString("keyId");
//        String address = JSONObject.fromObject(response).getJSONObject("data").getJSONObject("accountList").getString("address");
//
//        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));
//
//        log.info(" ************************ test gdContractAddressess must ************************ ");
//        response = gd.GDCreateAccout("",mapInvestorInfo);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'InvestorsAndContractAddress.ContractAddress' Error:Field validation for 'ContractAddress' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));
//
//
//        log.info(" ************************ test mapInvestorInfo must ************************ ");
//        response = gd.GDCreateAccout(gdContractAddress,null);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//
//        log.info("required count: " + StringUtils.countOccurrencesOf(response,"required"));
//
//
//        log.info(" ************************ test investorInfo.clientName must ************************ ");
//        mapInvestorInfo.put("clientName","");
//
//        response = gd.GDCreateAccout(gdContractAddress,mapInvestorInfo);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'InvestorsAndContractAddress.InvestorInfo.ClientName' Error:Field validation for 'ClientName' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));
//
//
//        log.info(" ************************ test investorInfo.shareholderNo must ************************ ");
//        mapInvestorInfo.put("clientName","testclientName00001");
//        mapInvestorInfo.put("shareholderNo","");
//
//        response = gd.GDCreateAccout(gdContractAddress,mapInvestorInfo);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'InvestorsAndContractAddress.InvestorInfo.ShareholderNo' Error:Field validation for 'ShareholderNo' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));
//
//
//        log.info(" ************************ test investorInfo.fundNo must ************************ ");
//        mapInvestorInfo.put("shareholderNo","testshNo00001");
//        mapInvestorInfo.put("fundNo","");
//
//        response = gd.GDCreateAccout(gdContractAddress,mapInvestorInfo);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'InvestorsAndContractAddress.InvestorInfo.FundNo' Error:Field validation for 'FundNo' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));
//
//        log.info(" ************************ test investorInfo.clientNo must ************************ ");
//        mapInvestorInfo.put("fundNo","testfNo00001");
//        mapInvestorInfo.put("clientNo","");
//
//        response = gd.GDCreateAccout(gdContractAddress,mapInvestorInfo);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'InvestorsAndContractAddress.InvestorInfo.ClientNo' Error:Field validation for 'ClientNo' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));
//
//
//        log.info(" ************************ test investorInfo.personalInfo must ************************ ");
//        mapInvestorInfo.put("clientNo","testclNo00001");
//        mapInvestorInfo.put("personalInfo",null);
//
//        response = gd.GDCreateAccout(gdContractAddress,mapInvestorInfo);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        log.info("required count: " + StringUtils.countOccurrencesOf(response,"required"));
//
//
//        log.info(" ************************ test investorInfo.personalInfo.clientFullName must ************************ ");
//        mapPersonInfo.put("clientFullName","");
//        mapInvestorInfo.put("personalInfo",mapPersonInfo);
//
//        response = gd.GDCreateAccout(gdContractAddress,mapInvestorInfo);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'InvestorsAndContractAddress.InvestorInfo.PersonalInfo.ClientFullName' Error:Field validation for 'ClientFullName' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));
//
//
//        log.info(" ************************ test investorInfo.personalInfo.certificateNo must ************************ ");
//        mapPersonInfo.put("clientFullName","test111");
//        mapPersonInfo.put("certificateNo","");
//        mapInvestorInfo.put("personalInfo",mapPersonInfo);
//
//        response = gd.GDCreateAccout(gdContractAddress,mapInvestorInfo);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'InvestorsAndContractAddress.InvestorInfo.PersonalInfo.CertificateNo' Error:Field validation for 'CertificateNo' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));
//
//
//        log.info(" ************************ test investorInfo.personalInfo.certificateAddress must ************************ ");
//        mapPersonInfo.put("certificateNo","testcertNo00001");
//        mapPersonInfo.put("certificateAddress","");
//        mapInvestorInfo.put("personalInfo",mapPersonInfo);
//
//        response = gd.GDCreateAccout(gdContractAddress,mapInvestorInfo);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'InvestorsAndContractAddress.InvestorInfo.PersonalInfo.CertificateAddress' Error:Field validation for 'CertificateAddress' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));
//
//
//        log.info(" ************************ test investorInfo.personalInfo.phone must ************************ ");
//        mapPersonInfo.put("certificateAddress","1112223333");
//        mapPersonInfo.put("telephone","testtelephoneNo00001");
//        mapPersonInfo.put("phone","");
//        mapInvestorInfo.put("personalInfo",mapPersonInfo);
//
//        response = gd.GDCreateAccout(gdContractAddress,mapInvestorInfo);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'InvestorsAndContractAddress.InvestorInfo.PersonalInfo.Phone' Error:Field validation for 'Phone' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));
//
//
//        log.info(" ************************ test investorInfo.personalInfo.contactAddress must ************************ ");
//        mapPersonInfo.put("phone","051261616161");
//        mapPersonInfo.put("contactAddress","");
//        mapInvestorInfo.put("personalInfo",mapPersonInfo);
//
//        response = gd.GDCreateAccout(gdContractAddress,mapInvestorInfo);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'InvestorsAndContractAddress.InvestorInfo.PersonalInfo.ContactAddress' Error:Field validation for 'ContactAddress' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));
//
//
//        log.info(" ************************ test investorInfo.personalInfo.equityCode must ************************ ");
//        mapPersonInfo.put("contactAddress","苏州");
//        mapPersonInfo.put("equityCode","");
//        mapInvestorInfo.put("personalInfo",mapPersonInfo);
//
//        response = gd.GDCreateAccout(gdContractAddress,mapInvestorInfo);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'InvestorsAndContractAddress.InvestorInfo.PersonalInfo.EquityCode' Error:Field validation for 'EquityCode' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));
//
//
//
//        log.info(" ************************ test investorInfo.personalInfo.equityAmount must ************************ ");
//        mapPersonInfo.clear();
//        mapPersonInfo.put("clientFullName",eqCode);
//        mapPersonInfo.put("organizationType","苏州股权代码");
//        mapPersonInfo.put("certificateType",0);
//        mapPersonInfo.put("certificateNo","123456468123153");
//        mapPersonInfo.put("certificateAddress","certificateAddress");
//        mapPersonInfo.put("gender",0);
//        mapPersonInfo.put("telephone","1598222555555");
//        mapPersonInfo.put("phone","1598222555555");
//        mapPersonInfo.put("postalCode","1585685245666821236");
//        mapPersonInfo.put("contactAddress","人民币");
//        mapPersonInfo.put("mailBox","1598222555555");
//        mapPersonInfo.put("fax","苏州同济区块链研究");
//        mapPersonInfo.put("equityCode","苏同院");
////        mapPersonInfo.put("equityAmount",5000);//不传该参数
//        mapPersonInfo.put("shareProperty",0);
//
//        mapInvestorInfo.put("personalInfo",mapPersonInfo);
//
//        response = gd.GDCreateAccout(gdContractAddress,mapInvestorInfo);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'InvestorsAndContractAddress.InvestorInfo.PersonalInfo.EquityAmount' Error:Field validation for 'EquityAmount' failed on the 'gt' tag",
//                JSONObject.fromObject(response).getString("message"));
//
//
//        log.info(" ************************ test investorInfo.personalInfo.shareProperty type mismatch ************************ ");
//        mapPersonInfo.put("equityAmount",500);
//        mapPersonInfo.put("shareProperty","");
//
//        mapInvestorInfo.put("personalInfo",mapPersonInfo);
//
//        response = gd.GDCreateAccout(gdContractAddress,mapInvestorInfo);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:json: cannot unmarshal string into Go struct field PersonalInfo.InvestorInfo.PersonalInfo.ShareProperty of type uint16",
//                JSONObject.fromObject(response).getString("message"));
//
//
//        log.info(" ************************ test investorInfo.investor must ************************ ");
//        mapPersonInfo.put("shareProperty",0);
//        mapInvestorInfo.put("personalInfo",mapPersonInfo);
//        mapInvestorInfo.put("investor",null);
//
//        response = gd.GDCreateAccout(gdContractAddress,mapInvestorInfo);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//
//        log.info("required count: " + StringUtils.countOccurrencesOf(response,"required"));
//
//
//        log.info(" ************************ test investorInfo.investor.currency must ************************ ");
//        mapinvestor.put("currency","");
//        mapInvestorInfo.put("investor",mapinvestor);
//
//        response = gd.GDCreateAccout(gdContractAddress,mapInvestorInfo);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'InvestorsAndContractAddress.InvestorInfo.Investor.Currency' Error:Field validation for 'Currency' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));
//
//
//        log.info(" ************************ test investorInfo.investor.registrationDate must ************************ ");
//        mapinvestor.put("currency","人民币");
//        mapinvestor.put("accountStatus",0);
//        mapinvestor.put("registrationDate","");
//        mapInvestorInfo.put("investor",mapinvestor);
//
//        response = gd.GDCreateAccout(gdContractAddress,mapInvestorInfo);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'InvestorsAndContractAddress.InvestorInfo.Investor.RegistrationDate' Error:Field validation for 'RegistrationDate' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));
//
//
//        log.info(" ************************ test investorInfo.investor.lastTradingDate must ************************ ");
//        mapinvestor.put("registrationDate","20200828");
//        mapinvestor.put("lastTradingDate","");
//        mapInvestorInfo.put("investor",mapinvestor);
//
//        response = gd.GDCreateAccout(gdContractAddress,mapInvestorInfo);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'InvestorsAndContractAddress.InvestorInfo.Investor.LastTradingDate' Error:Field validation for 'LastTradingDate' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));
//
//
//        log.info(" ************************ test investorInfo.investor.shareholderAmount must ************************ ");
//        mapinvestor.clear();
//        mapinvestor.put("salesDepartment","业务一部");
//        mapinvestor.put("clientGroups","群组");
//        mapinvestor.put("equityAccountNo","111111");
//        mapinvestor.put("currency","人民币");
//        mapinvestor.put("board","E板");
//        mapinvestor.put("accountType",0);
//        mapinvestor.put("accountStatus",0);
//        mapinvestor.put("registrationDate","621552");
//        mapinvestor.put("lastTradingDate","20200828");
//        mapinvestor.put("closingDate","20200828");
////        mapinvestor.put("shareholderAmount",3);//不传入该参数
//
//        mapInvestorInfo.put("investor",mapinvestor);
//
//        response = gd.GDCreateAccout(gdContractAddress,mapInvestorInfo);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'InvestorsAndContractAddress.InvestorInfo.Investor.ShareholderAmount' Error:Field validation for 'ShareholderAmount' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));
//
//
//
//    }
//
//    @Test
//    public void createAccoutInterfaceNotNecessaryParamTest() throws Exception {
//
//        String cltNo = "cI12000" + Random(7);
//        String shareHolderNo = "sh" + cltNo;
//        String eqCode = "createAcc" + Random(6);
//
//        Map mapPersonInfo = new HashMap();
//        mapPersonInfo.put("clientFullName", eqCode);
////        mapPersonInfo.put("organizationType", "苏州股权代码");
////        mapPersonInfo.put("certificateType", 0);
//        mapPersonInfo.put("certificateNo", "123456468123153");
//        mapPersonInfo.put("certificateAddress", "certificateAddress");
////        mapPersonInfo.put("gender", 0);
////        mapPersonInfo.put("telephone", "1598222555555");
//        mapPersonInfo.put("phone", "1598222555555");
////        mapPersonInfo.put("postalCode", "221005");
//        mapPersonInfo.put("contactAddress", "苏州市相城区");
////        mapPersonInfo.put("mailBox", "1598222555555");
////        mapPersonInfo.put("fax", "苏州同济区块链研究");
//        mapPersonInfo.put("equityCode", "苏同院");
//        mapPersonInfo.put("equityAmount", 5000);
////        mapPersonInfo.put("shareProperty", 0);
//
//        Map mapinvestor = new HashMap();
////        mapinvestor.put("salesDepartment", "业务一部");
////        mapinvestor.put("clientGroups", "群组");
////        mapinvestor.put("equityAccountNo", "111111");
//        mapinvestor.put("currency", "人民币");
////        mapinvestor.put("board", "E板");
////        mapinvestor.put("accountType", 0);
////        mapinvestor.put("accountStatus", 0);
//        mapinvestor.put("registrationDate", "2018-11-12");
//        mapinvestor.put("lastTradingDate", "2019-11-12");
////        mapinvestor.put("closingDate", "2023-11-12");
//        mapinvestor.put("shareholderAmount", 3);
//
//        String extend = "";
//
//        Map mapInvestorInfo = new HashMap();
//
//        mapInvestorInfo.put("clientName", "034654");
//        mapInvestorInfo.put("shareholderNo", shareHolderNo);
//        mapInvestorInfo.put("fundNo", "f0000001");
//        mapInvestorInfo.put("clientNo", cltNo);
////        mapInvestorInfo.put("extend", extend);
//        mapInvestorInfo.put("personalInfo", mapPersonInfo);
//        mapInvestorInfo.put("investor", mapinvestor);
//
//
//        String response = gd.GDCreateAccout(gdContractAddress, mapInvestorInfo);
//        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
//        assertEquals(cltNo,JSONObject.fromObject(response).getJSONObject("data").getJSONObject("accountList").getString("clientNo"));
//        assertEquals(shareHolderNo,JSONObject.fromObject(response).getJSONObject("data").getJSONObject("accountList").getString("shareholderNo"));
//        String keyId = JSONObject.fromObject(response).getJSONObject("data").getJSONObject("accountList").getString("keyId");
//        String address = JSONObject.fromObject(response).getJSONObject("data").getJSONObject("accountList").getString("address");
//
//        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));
//    }
//
//    @Test
//    public void shareIssueInterfaceMustParamTest() throws Exception {
//        String eqCode = "issue" + Random(6);
//
//        List<Map> shareList = CommonFunc.gdConstructShareListV1(gdAccount1,5000,0);
//        List<Map> shareList2 = CommonFunc.gdConstructShareListV1(gdAccount2,5000,0, shareList);
//
//        String response= gd.GDShareIssue(gdContractAddress,gdPlatfromKeyID,eqCode,shareList2);
//        JSONObject jsonObject=JSONObject.fromObject(response);
//        String txId = jsonObject.getJSONObject("data").getString("txId");
//
//        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));
//
//
//        log.info(" ************************ test platformKeyId must ************************ ");
//
//        response = gd.GDShareIssue(gdContractAddress,"",eqCode,shareList2);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'SharesRegister.PlatformKeyId' Error:Field validation for 'PlatformKeyId' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));
//
//        log.info(" ************************ test equityCode must ************************ ");
//
//        response = gd.GDShareIssue(gdContractAddress,gdPlatfromKeyID,"",shareList2);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'SharesRegister.EquityCode' Error:Field validation for 'EquityCode' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));
//
//
//        log.info(" ************************ test gdContractAddressess must ************************ ");
//
//        response = gd.GDShareIssue("",gdPlatfromKeyID,eqCode,shareList2);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'SharesRegister.ContractAddress' Error:Field validation for 'ContractAddress' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));
//
//
//        log.info(" ************************ test shareList must ************************ ");
//        eqCode = "must" + Random(12);
//        response = gd.GDShareIssue(gdContractAddress,gdPlatfromKeyID,eqCode,null);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("至少传入一个股权账号信息",JSONObject.fromObject(response).getString("message"));
//
//        log.info(" ************************ test shareList.address must ************************ ");
//        List<Map> shareListErr1 = CommonFunc.gdConstructShareListV1("",5000,0);
//        response = gd.GDShareIssue(gdContractAddress,gdPlatfromKeyID,eqCode,shareListErr1);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'SharesRegister.ShareList[0].Address' Error:Field validation for 'Address' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));
//
//        log.info(" ************************ test shareList.amount 0 ************************ ");
//        List<Map> shareListErr2 = CommonFunc.gdConstructShareListV1(gdAccount1,0,0);
//        response = gd.GDShareIssue(gdContractAddress,gdPlatfromKeyID,eqCode,shareListErr2);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'SharesRegister.ShareList[0].Amount' Error:Field validation for 'Amount' failed on the 'gt' tag",
//                JSONObject.fromObject(response).getString("message"));
//
//        log.info(" ************************ test shareList.amount must ************************ ");
//        Map<String,Object> shares2 = new HashMap<>();
//        shares2.put("address",gdAccount1);
//        shares2.put("shareProperty",0);
//
//        List<Map> shareList22 = new ArrayList<>();
//        shareList22.add(shares2);
//        response = gd.GDShareIssue(gdContractAddress,gdPlatfromKeyID,eqCode,shareList22);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'SharesRegister.ShareList[0].Amount' Error:Field validation for 'Amount' failed on the 'gt' tag",
//                JSONObject.fromObject(response).getString("message"));
//
//        log.info(" ************************ test shareList.shareProperty must ************************ ");
//        Map<String,Object> shares3 = new HashMap<>();
//        shares3.put("address",gdAccount1);
//        shares3.put("amount",500);
//
//        List<Map> shareList3 = new ArrayList<>();
//        shareList3.add(shares3);
//
//        response = gd.GDShareIssue(gdContractAddress,gdPlatfromKeyID,eqCode,shareList3);
////        assertEquals("400",JSONObject.fromObject(response).getString("state"));
////        assertEquals("无效的参数",
////                JSONObject.fromObject(response).getString("message"));
//
//
//        log.info(" ************************ test all must ************************ ");
//
//        response = gd.GDShareIssue("","","",null);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        log.info("required count: " + StringUtils.countOccurrencesOf(response,"required"));
//    }
//
//    @Test
//    public void shareIncreaseInterfaceTest() throws Exception {
//
//        String eqCode = "increaseEC" + Random(6);
//        String reason = "股份分红";
//
//        List<Map> shareList = CommonFunc.gdConstructShareListV1(gdAccount1,1000,0);
//        List<Map> shareList2 = CommonFunc.gdConstructShareListV1(gdAccount2,1000,0, shareList);
//        List<Map> shareList3 = CommonFunc.gdConstructShareListV1(gdAccount3,1000,0, shareList2);
//        List<Map> shareList4 = CommonFunc.gdConstructShareListV1(gdAccount4,1000,0, shareList3);
//
//        //发行股权代码
//        String response= gd.GDShareIssue(gdContractAddress,gdPlatfromKeyID,eqCode,shareList4);
//        JSONObject jsonObject=JSONObject.fromObject(response);
//        String txId = jsonObject.getJSONObject("data").getString("txId");
//
//        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));
//
//        sleepAndSaveInfo(3000);
//
//        //验证接口正常
//        response= gd.GDShareIncrease(gdContractAddress,gdPlatfromKeyID,eqCode,shareList4,reason);
//        txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
//        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));
//
//        //platformKeyId为空
//        response= gd.GDShareIncrease(gdContractAddress,"",eqCode,shareList4,reason);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("无效的参数"));
//
//        //equityCode为空
//        response= gd.GDShareIncrease(gdContractAddress,gdPlatfromKeyID,"",shareList4,reason);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("无效的参数"));
//
//        //shareList.address为空
//        shareList4.clear();
//        shareList4 = CommonFunc.gdConstructShareListV1("",1000,0, shareList3);
//        response= gd.GDShareIncrease(gdContractAddress,gdPlatfromKeyID,"",shareList4,reason);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("无效的参数"));
//
////        //shareList.amount不传参
////        shareList4.clear();
////        shareList4 = gdConstructShareListV1("",1000,0, shareList3);
////        response= gd.GDShareIncrease(gdContractAddress,gdPlatfromKeyID,"",shareList4,reason);
////        assertEquals("400",JSONObject.fromObject(response).getString("state"));
////        assertEquals(true,response.contains("Error:Field validation for 'EquityCode' failed on the 'required"));
////
////        //shareList.shareProperty不传参
////        shareList4.clear();
////        shareList4 = gdConstructShareListV1("",1000,0, shareList3);
////        response= gd.GDShareIncrease(gdContractAddress,gdPlatfromKeyID,"",shareList4,reason);
////        assertEquals("400",JSONObject.fromObject(response).getString("state"));
////        assertEquals(true,response.contains("Error:Field validation for 'EquityCode' failed on the 'required"));
//
//
//    }
//
//    @Test
//    public void shareLockInterfaceTest() throws Exception {
//        String eqCode = "lockEC" + Random(6);
//
//        List<Map> shareList = CommonFunc.gdConstructShareListV1(gdAccount1,1000,0);
//        List<Map> shareList2 = CommonFunc.gdConstructShareListV1(gdAccount2,1000,0, shareList);
//        List<Map> shareList3 = CommonFunc.gdConstructShareListV1(gdAccount3,1000,0, shareList2);
//        List<Map> shareList4 = CommonFunc.gdConstructShareListV1(gdAccount4,1000,0, shareList3);
//
//        String response= gd.GDShareIssue(gdContractAddress,gdPlatfromKeyID,eqCode,shareList4);
//        JSONObject jsonObject=JSONObject.fromObject(response);
//        String txId = jsonObject.getJSONObject("data").getString("txId");
//
//        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));
//
//
//        String bizNo = "test20200828001" + Random(10);
//        String address = gdAccount1;
//        double lockAmount = 500;
//        int shareProperty = 0;
//        String reason = "司法冻结";
//        String cutoffDate = "2020-09-30";
//
//        //验证接口正常
//        response= gd.GDShareLock(bizNo,address,eqCode,lockAmount,shareProperty,reason,cutoffDate);
//        txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
//        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));
//
//        //bizNo为空
//        response= gd.GDShareLock("",address,eqCode,lockAmount,shareProperty,reason,cutoffDate);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("Error:Field validation for 'BizNo' failed on the 'required"));
//
//        //address为空
//        response= gd.GDShareLock(bizNo,"",eqCode,lockAmount,shareProperty,reason,cutoffDate);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("Error:Field validation for 'Address' failed on the 'required"));
//
//        //equityCode为空
//        response= gd.GDShareLock(bizNo,address,"",lockAmount,shareProperty,reason,cutoffDate);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("Error:Field validation for 'EquityCode' failed on the 'required"));
//
////        //amount不传参
////        response= gd.GDShareLock(bizNo,address,eqCode,lockAmount,shareProperty,reason,cutoffDate);
////        assertEquals("400",JSONObject.fromObject(response).getString("state"));
////        assertEquals(true,response.contains("Error:Field validation for 'EquityCode' failed on the 'required"));
////
////        //shareProperty不传参
////        response= gd.GDShareLock(bizNo,address,eqCode,lockAmount,shareProperty,reason,cutoffDate);
////        assertEquals("400",JSONObject.fromObject(response).getString("state"));
////        assertEquals(true,response.contains("Error:Field validation for 'EquityCode' failed on the 'required"));
//
//        //reason为空
//        response= gd.GDShareLock(bizNo,address,eqCode,lockAmount,shareProperty,"",cutoffDate);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("Error:Field validation for 'Reason' failed on the 'required"));
//
//        //cutoffDate为空
//        response= gd.GDShareLock(bizNo,address,eqCode,lockAmount,shareProperty,reason,"");
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("Error:Field validation for 'CutoffDate' failed on the 'required"));
//    }
//
//    @Test
//    public void shareUnlockInterfaceTest() throws Exception {
//        String eqCode = "unlockEC" + Random(6);
//        String bizNo = "test" + Random(10);
//        double amount = 100;
//
//        //发行
//        List<Map> shareList = CommonFunc.gdConstructShareListV1(gdAccount1,1000,0);
//        List<Map> shareList2 = CommonFunc.gdConstructShareListV1(gdAccount2,1000,0, shareList);
//        List<Map> shareList3 = CommonFunc.gdConstructShareListV1(gdAccount3,1000,0, shareList2);
//        List<Map> shareList4 = CommonFunc.gdConstructShareListV1(gdAccount4,1000,0, shareList3);
//
//        String response= gd.GDShareIssue(gdContractAddress,gdPlatfromKeyID,eqCode,shareList4);
//        JSONObject jsonObject=JSONObject.fromObject(response);
//        String txId = jsonObject.getJSONObject("data").getString("txId");
//
//        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));
//
//
//        //冻结
//        response= gd.GDShareLock(bizNo,gdAccount1,eqCode,3000,0,"司法冻结","2021-10-01");
//        txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
//        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));
//
//        sleepAndSaveInfo(3000);
//
//
//        //验证接口正常
//        response= gd.GDShareUnlock(bizNo,eqCode,amount);
//        txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
//        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));
//
//        //bizNo为空
//        response= gd.GDShareUnlock("",eqCode,amount);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("Error:Field validation for 'BizNo' failed on the 'required"));
//
//        //equityCode为空
//        response= gd.GDShareUnlock(bizNo,"",amount);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("Error:Field validation for 'EquityCode' failed on the 'required"));
//
////        //amount为空
////        response= gd.GDShareUnlock(bizNo,eqCode,amount);
////        assertEquals("400",JSONObject.fromObject(response).getString("state"));
////        assertEquals(true,response.contains("Error:Field validation for 'EquityCode' failed on the 'required"));
//
//    }
//
//    @Test
//    public void shareTransferInterfaceMustParamTest() throws Exception {
//        //转账前发行
//
//        String eqCode = "transfer" + Random(6);
//
//        List<Map> shareList = CommonFunc.gdConstructShareListV1(gdAccount1,5000,0);
//        List<Map> shareList2 = CommonFunc.gdConstructShareListV1(gdAccount2,5000,0, shareList);
//        List<Map> shareList3 = CommonFunc.gdConstructShareListV1(gdAccount3,5000,0, shareList2);
//        List<Map> shareList4 = CommonFunc.gdConstructShareListV1(gdAccount4,5000,0, shareList3);
//
//        String response= gd.GDShareIssue(gdContractAddress,gdPlatfromKeyID,eqCode,shareList4);
//        JSONObject jsonObject=JSONObject.fromObject(response);
//        String txId = jsonObject.getJSONObject("data").getString("txId");
//
//        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));
//
//        //转账接口测试
//
//        String keyId = "bt3hd3ppgfltc7nnqlt0";
//        String fromAddr = gdAccount1;
//        double amount = 10;
//        String toAddr = gdAccount1;
//        int shareProperty = 0;
//        int txType = 0;
//        String orderNo = "test202008280952";
//        int orderWay = 0;
//        int orderType = 0;
//        String price = "10000";
//        String time = "20200828";
//        String remark = "转账";
//
//        Map<String, Object> map = new HashMap<>();
//        map.put("keyId", keyId);
//        map.put("fromAddress", fromAddr);
//        map.put("amount", amount);
//        map.put("toAddress", toAddr);
//        map.put("shareProperty", shareProperty);
//        map.put("equityCode", eqCode);
//        map.put("txType", txType);
//        map.put("orderNo", orderNo);
//        map.put("orderWay", orderWay);
//        map.put("orderType", orderType);
//        map.put("price", price);
//        map.put("time", time);
//        map.put("remark", remark);
//
//
//
//
//        log.info(" ************************ test keyId must ************************ ");
//        map.put("keyId", "");
//        response= gd.GDShareTransfer(map);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'SharesTransfer.KeyId' Error:Field validation for 'KeyId' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));
//
//        log.info(" ************************ test fromAddress must ************************ ");
//        map.put("keyId", keyId);
//        map.put("fromAddress", "");
//        response= gd.GDShareTransfer(map);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'SharesTransfer.FromAddress' Error:Field validation for 'FromAddress' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));
//
//        log.info(" ************************ test amount must ************************ ");
//
//        map.clear();
//        map.put("keyId", keyId);
//        map.put("fromAddress", fromAddr);
////        map.put("amount", amount); //不传入参数
//        map.put("toAddress", toAddr);
//        map.put("shareProperty", shareProperty);
//        map.put("equityCode", eqCode);
//        map.put("txType", txType);
//        map.put("orderNo", orderNo);
//        map.put("orderWay", orderWay);
//        map.put("orderType", orderType);
//        map.put("price", price);
//        map.put("time", time);
//        map.put("remark", remark);
//
//        response= gd.GDShareTransfer(map);
////        assertEquals("400",JSONObject.fromObject(response).getString("state"));
////        assertEquals("无效的参数",
////                JSONObject.fromObject(response).getString("message"));
//
//        log.info(" ************************ test toAddress must ************************ ");
//
//        map.clear();
//        map.put("keyId", keyId);
//        map.put("fromAddress", fromAddr);
//        map.put("amount", amount);
//        map.put("toAddress", "");
//        map.put("shareProperty", shareProperty);
//        map.put("equityCode", eqCode);
//        map.put("txType", txType);
//        map.put("orderNo", orderNo);
//        map.put("orderWay", orderWay);
//        map.put("orderType", orderType);
//        map.put("price", price);
//        map.put("time", time);
//        map.put("remark", remark);
//
//        response= gd.GDShareTransfer(map);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'SharesTransfer.ToAddress' Error:Field validation for 'ToAddress' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));
//
//        log.info(" ************************ test shareProperty must ************************ ");
//
//        map.clear();
//        map.put("keyId", keyId);
//        map.put("fromAddress", fromAddr);
//        map.put("amount", amount);
//        map.put("toAddress", toAddr);
////        map.put("shareProperty", shareProperty);//不传入
//        map.put("equityCode", eqCode);
//        map.put("txType", txType);
//        map.put("orderNo", orderNo);
//        map.put("orderWay", orderWay);
//        map.put("orderType", orderType);
//        map.put("price", price);
//        map.put("time", time);
//        map.put("remark", remark);
//
//        response= gd.GDShareTransfer(map);
////        assertEquals("400",JSONObject.fromObject(response).getString("state"));
////        assertEquals("无效的参数",
////                JSONObject.fromObject(response).getString("message"));
//
//        log.info(" ************************ test equityCode must ************************ ");
//
//        map.clear();
//        map.put("keyId", keyId);
//        map.put("fromAddress", fromAddr);
//        map.put("amount", amount);
//        map.put("toAddress", toAddr);
//        map.put("shareProperty", shareProperty);//不传入
//        map.put("equityCode", "");
//        map.put("txType", txType);
//        map.put("orderNo", orderNo);
//        map.put("orderWay", orderWay);
//        map.put("orderType", orderType);
//        map.put("price", price);
//        map.put("time", time);
//        map.put("remark", remark);
//
//        response= gd.GDShareTransfer(map);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'SharesTransfer.EquityCode' Error:Field validation for 'EquityCode' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));
//
//
//        log.info(" ************************ test txType must ************************ ");
//
//        map.clear();
//        map.put("keyId", keyId);
//        map.put("fromAddress", fromAddr);
//        map.put("amount", amount);
//        map.put("toAddress", toAddr);
//        map.put("shareProperty", shareProperty);
//        map.put("equityCode", eqCode);
////        map.put("txType", txType);//不传入
//        map.put("orderNo", orderNo);
//        map.put("orderWay", orderWay);
//        map.put("orderType", orderType);
//        map.put("price", price);
//        map.put("time", time);
//        map.put("remark", remark);
//
//        response= gd.GDShareTransfer(map);
////        assertEquals("400",JSONObject.fromObject(response).getString("state"));
////        assertEquals("无效的参数",
////                JSONObject.fromObject(response).getString("message"));
//
//        log.info(" ************************ test orderNo must ************************ ");
//
//        map.clear();
//        map.put("keyId", keyId);
//        map.put("fromAddress", fromAddr);
//        map.put("amount", amount);
//        map.put("toAddress", toAddr);
//        map.put("shareProperty", shareProperty);
//        map.put("equityCode", eqCode);
//        map.put("txType", txType);
//        map.put("orderNo", "");
//        map.put("orderWay", orderWay);
//        map.put("orderType", orderType);
//        map.put("price", price);
//        map.put("time", time);
//        map.put("remark", remark);
//
//        response= gd.GDShareTransfer(map);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'SharesTransfer.OrderNo' Error:Field validation for 'OrderNo' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));
//
//        log.info(" ************************ test orderWay must ************************ ");
//
//        map.clear();
//        map.put("keyId", keyId);
//        map.put("fromAddress", fromAddr);
//        map.put("amount", amount);
//        map.put("toAddress", toAddr);
//        map.put("shareProperty", shareProperty);
//        map.put("equityCode", eqCode);
//        map.put("txType", txType);
//        map.put("orderNo", orderNo);
////        map.put("orderWay", orderWay);    //不传入
//        map.put("orderType", orderType);
//        map.put("price", price);
//        map.put("time", time);
//        map.put("remark", remark);
//
//        response= gd.GDShareTransfer(map);
////        assertEquals("400",JSONObject.fromObject(response).getString("state"));
////        assertEquals("无效的参数",
////                JSONObject.fromObject(response).getString("message"));
//
//        log.info(" ************************ test orderType must ************************ ");
//
//        map.clear();
//        map.put("keyId", keyId);
//        map.put("fromAddress", fromAddr);
//        map.put("amount", amount);
//        map.put("toAddress", toAddr);
//        map.put("shareProperty", shareProperty);
//        map.put("equityCode", eqCode);
//        map.put("txType", txType);
//        map.put("orderNo", orderNo);
//        map.put("orderWay", orderWay);
////        map.put("orderType", orderType);    //不传入
//        map.put("price", price);
//        map.put("time", time);
//        map.put("remark", remark);
//
//        response= gd.GDShareTransfer(map);
////        assertEquals("400",JSONObject.fromObject(response).getString("state"));
////        assertEquals("无效的参数",
////                JSONObject.fromObject(response).getString("message"));
//
//        log.info(" ************************ test price must ************************ ");
//
//        map.clear();
//        map.put("keyId", keyId);
//        map.put("fromAddress", fromAddr);
//        map.put("amount", amount);
//        map.put("toAddress", toAddr);
//        map.put("shareProperty", shareProperty);
//        map.put("equityCode", eqCode);
//        map.put("txType", txType);
//        map.put("orderNo", orderNo);
//        map.put("orderWay", orderWay);
//        map.put("orderType", orderType);
//        map.put("price", "");
//        map.put("time", time);
//        map.put("remark", remark);
//
//        response= gd.GDShareTransfer(map);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'SharesTransfer.Price' Error:Field validation for 'Price' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));
//
//        log.info(" ************************ test tradeTime must ************************ ");
//
//        map.clear();
//        map.put("keyId", keyId);
//        map.put("fromAddress", fromAddr);
//        map.put("amount", amount);
//        map.put("toAddress", toAddr);
//        map.put("shareProperty", shareProperty);
//        map.put("equityCode", eqCode);
//        map.put("txType", txType);
//        map.put("orderNo", orderNo);
//        map.put("orderWay", orderWay);
//        map.put("orderType", orderType);
//        map.put("price", price);
//        map.put("time", "");
//        map.put("remark", remark);
//
//        response= gd.GDShareTransfer(map);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'SharesTransfer.Time' Error:Field validation for 'Time' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));
//
//        log.info(" ************************ test remark must ************************ ");
//
//        map.clear();
//        map.put("keyId", keyId);
//        map.put("fromAddress", fromAddr);
//        map.put("amount", amount);
//        map.put("toAddress", toAddr);
//        map.put("shareProperty", shareProperty);
//        map.put("equityCode", eqCode);
//        map.put("txType", txType);
//        map.put("orderNo", orderNo);
//        map.put("orderWay", orderWay);
//        map.put("orderType", orderType);
//        map.put("price", price);
//        map.put("time", time);
//        map.put("remark", "");
//
//        response= gd.GDShareTransfer(map);
////        assertEquals("400",JSONObject.fromObject(response).getString("state"));
////        assertEquals("无效的参数",
////                JSONObject.fromObject(response).getString("message"));
//
//
//        log.info(" ************************ test all must ************************ ");
//
//        response = gd.GDShareTransfer(null);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        log.info("required count: " + StringUtils.countOccurrencesOf(response,"required"));
//    }
//
//    @Test
//    public void shareRecycleInterfaceTest() throws Exception {
//
//        String eqCode = "recycle" + Random(6);
//        String remark = "777777";
//
//        List<Map> shareList = CommonFunc.gdConstructShareListV1(gdAccount1,1000,0);
//        List<Map> shareList2 = CommonFunc.gdConstructShareListV1(gdAccount2,1000,0, shareList);
//        List<Map> shareList3 = CommonFunc.gdConstructShareListV1(gdAccount3,1000,0, shareList2);
//        List<Map> shareList4 = CommonFunc.gdConstructShareListV1(gdAccount4,1000,0, shareList3);
//
//        //发行
//        String response= gd.GDShareIssue(gdContractAddress,gdPlatfromKeyID,eqCode,shareList4);
//        JSONObject jsonObject=JSONObject.fromObject(response);
//        String txId = jsonObject.getJSONObject("data").getString("txId");
//
//        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));
//
//
//        //验证接口正常
//        response= gd.GDShareRecycle(gdPlatfromKeyID,eqCode,shareList4,remark);
//        txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
//        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));
//
//        //platformKeyId为空
//        response= gd.GDShareRecycle("",eqCode,shareList4,remark);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("无效的参数:Key: 'SharesRecycle.PlatformKeyId' Error:Field validation for 'PlatformKeyId' failed on the 'required' tag"));
//
//        //equityCode为空
//        response= gd.GDShareRecycle(gdPlatfromKeyID,"",shareList4,remark);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("Error:Field validation for 'EquityCode' failed on the 'required"));
//
//        //AddressList.address为空
//        shareList4.clear();
//        shareList4 = CommonFunc.gdConstructShareListV1("",1000,0,shareList3);
//        response= gd.GDShareRecycle(gdPlatfromKeyID,eqCode,shareList4,remark);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("无效的参数:Key: 'SharesRecycle.AddressList[3].Address' Error:Field validation for 'Address' failed on the 'required' tag"));
//
////        //AddressList.amount不传参
////        response= gd.GDShareRecycle(gdPlatfromKeyID,eqCode,shareList4,remark);
////        assertEquals("400",JSONObject.fromObject(response).getString("state"));
////        assertEquals(true,response.contains("Error:Field validation for 'EquityCode' failed on the 'required"));
////
////        //AddressList.shareProperty不传参
////        response= gd.GDShareRecycle(gdPlatfromKeyID,eqCode,shareList4,remark);
////        assertEquals("400",JSONObject.fromObject(response).getString("state"));
////        assertEquals(true,response.contains("Error:Field validation for 'EquityCode' failed on the 'required"));
//
//        //equityCode为空
//        shareList4 = CommonFunc.gdConstructShareListV1(gdAccount4,1000,0,shareList3);
//        response= gd.GDShareRecycle(gdPlatfromKeyID,"",shareList4,remark);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("Error:Field validation for 'EquityCode' failed on the 'required"));
//
//        //remark为空
//        response= gd.GDShareRecycle(gdPlatfromKeyID,eqCode,shareList4,"");
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("Error:Field validation for 'Remark' failed on the 'required"));
//
//    }
//
//
//    @Test
//    public void accoutDestroyInterfaceMustParamTest() throws Exception {
//
//        log.info(" ************************ test contractAddress must ************************ ");
//
//        String response = gd.GDAccountDestroy("","testcliI1300001");
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'AccountDestroy.ContractAddress' Error:Field validation for 'ContractAddress' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));
//
//        log.info(" ************************ test clientNo must ************************ ");
//
//        response = gd.GDAccountDestroy(gdContractAddress,"");
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'AccountDestroy.ClientNo' Error:Field validation for 'ClientNo' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));
//
//
//        log.info(" ************************ test all must ************************ ");
//
//        response = gd.GDAccountDestroy("","");
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("无效的参数:Key: 'AccountDestroy.ContractAddress' Error:Field validation for 'ContractAddress' failed on the 'required' tag"));
//        assertEquals(true,response.contains("Key: 'AccountDestroy.ClientNo' Error:Field validation for 'ClientNo' failed on the 'required' tag"));
//
//
//    }
//
//
//    @Test
//    public void shareChangeBoardInterfaceMustParamTest() throws Exception {
//
//        String eqCode = "changeBoard" + Random(6);
//
//        List<Map> shareList = CommonFunc.gdConstructShareListV1(gdAccount1,5000,0);
//        List<Map> shareList2 = CommonFunc.gdConstructShareListV1(gdAccount2,5000,0, shareList);
//        List<Map> shareList3 = CommonFunc.gdConstructShareListV1(gdAccount3,5000,0, shareList2);
//        List<Map> shareList4 = CommonFunc.gdConstructShareListV1(gdAccount4,5000,0, shareList3);
//
//        //发行
//        String response= gd.GDShareIssue(gdContractAddress,gdPlatfromKeyID,eqCode,shareList4);
//        JSONObject jsonObject=JSONObject.fromObject(response);
//        String txId = jsonObject.getJSONObject("data").getString("txId");
//
//        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));
//
//
//        log.info(" ************************ test platformKeyId must ************************ ");
//
//        response = gd.GDShareChangeBoard("","testcompanyId",eqCode,"testnew001");
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'TransferPlate.PlatformKeyId' Error:Field validation for 'PlatformKeyId' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));
//
//        log.info(" ************************ test companyId must ************************ ");
//
//        response = gd.GDShareChangeBoard(gdPlatfromKeyID,"",eqCode,"testnew001");
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'TransferPlate.CompanyId' Error:Field validation for 'CompanyId' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));
//
//
//
//        log.info(" ************************ test oldEquityCode must ************************ ");
//
//        response = gd.GDShareChangeBoard(gdPlatfromKeyID,"testcompanyId","","testnew001");
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'TransferPlate.OldEquityCode' Error:Field validation for 'OldEquityCode' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));
//
//
//        log.info(" ************************ test newEquityCode must ************************ ");
//
//        response = gd.GDShareChangeBoard(gdPlatfromKeyID,"testcompanyId",eqCode,"");
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'TransferPlate.NewEquityCode' Error:Field validation for 'NewEquityCode' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));
//
//
//        log.info(" ************************ test all must ************************ ");
//
//        response = gd.GDShareChangeBoard("","","","");
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//
//        assertEquals(4,StringUtils.countOccurrencesOf(response,"required"));
//        log.info("required count: " + StringUtils.countOccurrencesOf(response,"required"));
//
//    }
//
//    @Test
//    public void changeSharePropertyInterfaceMustParamTest() throws Exception {
//
//        String eqCode = "changePro" + Random(6);
//
//        List<Map> shareList = CommonFunc.gdConstructShareListV1(gdAccount1,5000,0);
//        List<Map> shareList2 = CommonFunc.gdConstructShareListV1(gdAccount2,5000,0, shareList);
//        List<Map> shareList3 = CommonFunc.gdConstructShareListV1(gdAccount3,5000,0, shareList2);
//        List<Map> shareList4 = CommonFunc.gdConstructShareListV1(gdAccount4,5000,0, shareList3);
//
//        //发行
//        String response= gd.GDShareIssue(gdContractAddress,gdPlatfromKeyID,eqCode,shareList4);
//        JSONObject jsonObject=JSONObject.fromObject(response);
//        String txId = jsonObject.getJSONObject("data").getString("txId");
//
//        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));
//
//
//        String address = gdAccount1;
//        double changeAmount = 100;
//        int oldProperty = 0;
//        int newProperty = 1;
//
//
//        log.info(" ************************ test platformkeyId must ************************ ");
//        response = gd.GDShareChangeProperty("",address,eqCode,changeAmount,oldProperty,newProperty);
//
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'SharesChange.PlatformKeyId' Error:Field validation for 'PlatformKeyId' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));
//
//        log.info(" ************************ test address must ************************ ");
//        response = gd.GDShareChangeProperty(gdPlatfromKeyID,"",eqCode,changeAmount,oldProperty,newProperty);
//
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'SharesChange.Address' Error:Field validation for 'Address' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));
//
//        log.info(" ************************ test equityCode must ************************ ");
//
//        response = gd.GDShareChangeProperty(gdPlatfromKeyID,address,"",changeAmount,oldProperty,newProperty);
//
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'SharesChange.EquityCode' Error:Field validation for 'EquityCode' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));
//
//
//        log.info(" ************************ test amount must ************************ ");
//        Map<String, Object> map = new HashMap<>();
//        map.put("platformkeyId", gdPlatfromKeyID);
//        map.put("address", address);
//        map.put("equityCode", eqCode);
////        map.put("amount", changeAmount); //不传入此参数
//        map.put("oldShareProperty", oldProperty);
//        map.put("newShareProperty", newProperty);
//
//        response = gd.GDShareChangeProperty(map);
//
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'SharesChange.Amount' Error:Field validation for 'Amount' failed on the 'gt' tag",
//                JSONObject.fromObject(response).getString("message"));
//
//
//        log.info(" ************************ test oldShareProperty must ************************ ");
//
//        map.clear();
//        map.put("platformkeyId", gdPlatfromKeyID);
//        map.put("address", address);
//        map.put("equityCode", eqCode);
//        map.put("amount", changeAmount);
////        map.put("oldShareProperty", oldProperty);//不传入此参数
//        map.put("newShareProperty", newProperty);
//
//        response = gd.GDShareChangeProperty(map);
//
////        assertEquals("400", JSONObject.fromObject(response).getString("state"));
////        assertEquals("无效的参数:Key: 'SharesChange.OldShareProperty' Error:Field validation for 'OldShareProperty' failed on the 'gt' tag",
////                JSONObject.fromObject(response).getString("message"));
//
//
//        log.info(" ************************ test newShareProperty must ************************ ");
//
//        map.clear();
//        map.put("platformkeyId", gdPlatfromKeyID);
//        map.put("address", address);
//        map.put("equityCode", eqCode);
//        map.put("amount", changeAmount);
//        map.put("oldShareProperty", oldProperty);
////        map.put("newShareProperty", newProperty);//不传入此参数
//
//        response = gd.GDShareChangeProperty(map);
//
////        assertEquals("400",JSONObject.fromObject(response).getString("state"));
////        assertEquals("无效的参数:Key: 'TransferPlate.PlatformKeyId' Error:Field validation for 'PlatformKeyId' failed on the 'required' tag",
////                JSONObject.fromObject(response).getString("message"));
//
//
//        log.info(" ************************ test all must ************************ ");
//
//        response = gd.GDShareChangeProperty(null);
//
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
////        assertEquals("无效的参数:Key: 'TransferPlate.PlatformKeyId' Error:Field validation for 'PlatformKeyId' failed on the 'required' tag",
////                JSONObject.fromObject(response).getString("message"));
//
////        assertEquals(6, StringUtils.countOccurrencesOf(response,"required"));
//        log.info("required count: " + StringUtils.countOccurrencesOf(response,"required"));
//    }
//
//
//    @Test
//    public void infoDisclosurePublishInterfaceMustParamTest() throws Exception {
//
//        String type = "公告";
//        String subType = "企业公告";
//        String title = "挂牌企业登记信息";
//        String fileHash = "dfhafdd1111111651575452";
//        String fileURL = "test/publish/company0001info";
//        String hashAlgo = "sha256";
//        String publisher = "上海股权托管登记交易所";
//        String publishTime = "20200828 10:43";
//        String enterprise = "201804152125222515";
//
//
//        log.info(" ************************ test type must ************************ ");
//        String response= gd.GDInfoPublish("",subType,title,fileHash,fileURL,hashAlgo,publisher,publishTime,enterprise);
//
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("Invalid parameter, error:Key: 'InfoDisc.Type' Error:Field validation for 'Type' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));
//
//        log.info(" ************************ test subType must ************************ ");
//        response= gd.GDInfoPublish(type,"",title,fileHash,fileURL,hashAlgo,publisher,publishTime,enterprise);
//
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("Invalid parameter, error:Key: 'InfoDisc.SubType' Error:Field validation for 'SubType' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));
//
//        log.info(" ************************ test title must ************************ ");
//
//        response= gd.GDInfoPublish(type,subType,"",fileHash,fileURL,hashAlgo,publisher,publishTime,enterprise);
//
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("Invalid parameter, error:Key: 'InfoDisc.Title' Error:Field validation for 'Title' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));
//
//
//        log.info(" ************************ test fileHash must ************************ ");
//
//        response= gd.GDInfoPublish(type,subType,title,"",fileURL,hashAlgo,publisher,publishTime,enterprise);
//
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("Invalid parameter, error:Key: 'InfoDisc.FileHash' Error:Field validation for 'FileHash' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));
//
//
//        log.info(" ************************ test hashAlgo must ************************ ");
//
//        response= gd.GDInfoPublish(type,subType,title,fileHash,fileURL,"",publisher,publishTime,enterprise);
//
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("Invalid parameter, error:Key: 'InfoDisc.HashAlgo' Error:Field validation for 'HashAlgo' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));
//
//
//        log.info(" ************************ test publisher must ************************ ");
//
//        response= gd.GDInfoPublish(type,subType,title,fileHash,fileURL,hashAlgo,"",publishTime,enterprise);
//
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("Invalid parameter, error:Key: 'InfoDisc.Publisher' Error:Field validation for 'Publisher' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));
//
//
//        log.info(" ************************ test publishTime must ************************ ");
//
//        response= gd.GDInfoPublish(type,subType,title,fileHash,fileURL,hashAlgo,publisher,"",enterprise);
//
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("Invalid parameter, error:Key: 'InfoDisc.PublishTime' Error:Field validation for 'PublishTime' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));
//
//
//        log.info(" ************************ test enterprise must ************************ ");
//
//        response= gd.GDInfoPublish(type,subType,title,fileHash,fileURL,hashAlgo,publisher,publishTime,"");
//
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("Invalid parameter, error:Key: 'InfoDisc.Enterprise' Error:Field validation for 'Enterprise' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));
//
//
//        log.info(" ************************ test all must ************************ ");
//
//        response= gd.GDInfoPublish("","","","","","","","","");
//
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//
//
//    }
//
//    @Test
//    public void infoDisclosurePublishInterfaceNotNecessaryParamTest() throws Exception {
//
//        String type = "公告";
//        String subType = "企业公告";
//        String title = "挂牌企业登记信息";
//        String fileHash = "dfhafdd1111111651575452";
//        String hashAlgo = "sha256";
//        String publisher = "上海股权托管登记交易所";
//        String publishTime = "20200828 10:43";
//        String enterprise = "201804152125222515";
//
//
//        log.info(" ************************ test fileURL must ************************ ");
//        String response= gd.GDInfoPublish(type,subType,title,fileHash,"",hashAlgo,publisher,publishTime,enterprise);
//
//        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//    }
//
//
//}
