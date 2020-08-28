package com.tjfintech.common.functionTest.guDengTest;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.GuDeng;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.SLEEPTIME;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class createAccountInterfaceTest {

    TestBuilder testBuilder= TestBuilder.getInstance();
    GuDeng gd =testBuilder.getGuDeng();
    Store store =testBuilder.getStore();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    String contractAddr = "48cb62af2f6363a5088264ce41193a362455b27fedb1d72eb512a7bfeb339523";
    String platformKeyID = "bt45k19pgfltc7nnqn50";
    String companyId = "companyI1100001";
    String clientNo = "cI1100001";
    String equityCode = "SZI1100001";



    @Test
    public void createAccoutInterfaceMustParamTest() throws Exception {

        String cltNo = clientNo;
        String shareHolderNo = "sI1100002";
        String eqCode = equityCode;

        Map mapPersonInfo = new HashMap();
        mapPersonInfo.put("clientFullName",eqCode);
        mapPersonInfo.put("organizationType","苏州股权代码");
        mapPersonInfo.put("certificateType",0);
        mapPersonInfo.put("certificateNo","123456468123153");
        mapPersonInfo.put("certificateAddress","certificateAddress");
        mapPersonInfo.put("gender","苏州同济区块链研究");
        mapPersonInfo.put("telephone","1598222555555");
        mapPersonInfo.put("phone","1598222555555");
        mapPersonInfo.put("postalCode","1585685245666821236");
        mapPersonInfo.put("contactAddress","人民币");
        mapPersonInfo.put("mailBox","1598222555555");
        mapPersonInfo.put("fax","苏州同济区块链研究");
        mapPersonInfo.put("equityCode","苏同院");
        mapPersonInfo.put("equityAmount",5000);
        mapPersonInfo.put("shareProperty",0);

        Map mapinvestor = new HashMap();
        mapinvestor.put("salesDepartment","业务一部");
        mapinvestor.put("clientGroups","群组");
        mapinvestor.put("equityAccountNo","111111");
        mapinvestor.put("currency","人民币");
        mapinvestor.put("board","E板");
        mapinvestor.put("accountType",0);
        mapinvestor.put("accountStatus",0);
        mapinvestor.put("registrationDate","621552");
        mapinvestor.put("lastTradingDate","20200828");
        mapinvestor.put("closingDate","20200828");
        mapinvestor.put("shareholderAmount",3);

        String extend = "";

        Map mapInvestorInfo = new HashMap();

        mapInvestorInfo.put("clientName","034654");
        mapInvestorInfo.put("shareholderNo",shareHolderNo);
        mapInvestorInfo.put("fundNo","f0000001");
        mapInvestorInfo.put("clientNo",cltNo);
        mapInvestorInfo.put("extend",extend);
        mapInvestorInfo.put("personalInfo",mapPersonInfo);
        mapInvestorInfo.put("investor",mapinvestor);


        String response= gd.GDCreateAccout(contractAddr,mapInvestorInfo);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        String txId = jsonObject.getJSONObject("data").getString("txId");
//        assertEquals(clientNo,JSONObject.fromObject(response).getJSONObject("data").getJSONObject("accountList").getString("clientNo"));
//        assertEquals(shareHolderNo,JSONObject.fromObject(response).getJSONObject("data").getJSONObject("accountList").getString("shareholderNo"));
//        String keyId = JSONObject.fromObject(response).getJSONObject("data").getJSONObject("accountList").getString("keyId");
//        String address = JSONObject.fromObject(response).getJSONObject("data").getJSONObject("accountList").getString("address");
//
//        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        log.info(" ************************ test contractAddress must ************************ ");
        response = gd.GDCreateAccout("",mapInvestorInfo);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'InvestorsAndContractAddress.ContractAddress' Error:Field validation for 'ContractAddress' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));


        log.info(" ************************ test mapInvestorInfo must ************************ ");
        response = gd.GDCreateAccout(contractAddr,null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));

        log.info("required count: " + StringUtils.countOccurrencesOf(response,"required"));


        log.info(" ************************ test investorInfo.clientName must ************************ ");
        mapInvestorInfo.put("clientName","");

        response = gd.GDCreateAccout(contractAddr,mapInvestorInfo);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'InvestorsAndContractAddress.InvestorInfo.ClientName' Error:Field validation for 'ClientName' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));


        log.info(" ************************ test investorInfo.shareholderNo must ************************ ");
        mapInvestorInfo.put("clientName","testclientName00001");
        mapInvestorInfo.put("shareholderNo","");

        response = gd.GDCreateAccout(contractAddr,mapInvestorInfo);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'InvestorsAndContractAddress.InvestorInfo.ShareholderNo' Error:Field validation for 'ShareholderNo' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));


        log.info(" ************************ test investorInfo.fundNo must ************************ ");
        mapInvestorInfo.put("shareholderNo","testshNo00001");
        mapInvestorInfo.put("fundNo","");

        response = gd.GDCreateAccout(contractAddr,mapInvestorInfo);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'InvestorsAndContractAddress.InvestorInfo.FundNo' Error:Field validation for 'FundNo' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));

        log.info(" ************************ test investorInfo.clientNo must ************************ ");
        mapInvestorInfo.put("fundNo","testfNo00001");
        mapInvestorInfo.put("clientNo","");

        response = gd.GDCreateAccout(contractAddr,mapInvestorInfo);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'InvestorsAndContractAddress.InvestorInfo.ClientNo' Error:Field validation for 'ClientNo' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));


        log.info(" ************************ test investorInfo.personalInfo must ************************ ");
        mapInvestorInfo.put("clientNo","testclNo00001");
        mapInvestorInfo.put("personalInfo",null);

        response = gd.GDCreateAccout(contractAddr,mapInvestorInfo);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'InvestorsAndContractAddress.ContractAddress' Error:Field validation for 'ContractAddress' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));
        log.info("required count: " + StringUtils.countOccurrencesOf(response,"required"));


        log.info(" ************************ test investorInfo.personalInfo.clientFullName must ************************ ");
        mapPersonInfo.put("clientFullName","");
        mapInvestorInfo.put("personalInfo",mapPersonInfo);

        response = gd.GDCreateAccout(contractAddr,mapInvestorInfo);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'InvestorsAndContractAddress.InvestorInfo.PersonalInfo.ClientFullName' Error:Field validation for 'ClientFullName' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));


        log.info(" ************************ test investorInfo.personalInfo.certificateType must ************************ ");
        mapPersonInfo.clear();
        mapPersonInfo.put("clientFullName",eqCode);
        mapPersonInfo.put("organizationType","苏州股权代码");
//        mapPersonInfo.put("certificateType",0);//不传入该参数
        mapPersonInfo.put("certificateNo","123456468123153");
        mapPersonInfo.put("certificateAddress","certificateAddress");
        mapPersonInfo.put("gender","苏州同济区块链研究");
        mapPersonInfo.put("telephone","1598222555555");
        mapPersonInfo.put("phone","1598222555555");
        mapPersonInfo.put("postalCode","1585685245666821236");
        mapPersonInfo.put("contactAddress","人民币");
        mapPersonInfo.put("mailBox","1598222555555");
        mapPersonInfo.put("fax","苏州同济区块链研究");
        mapPersonInfo.put("equityCode","苏同院");
        mapPersonInfo.put("equityAmount",5000);
        mapPersonInfo.put("shareProperty",0);

        mapInvestorInfo.put("personalInfo",mapPersonInfo);

        response = gd.GDCreateAccout(contractAddr,mapInvestorInfo);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'InvestorsAndContractAddress.ContractAddress' Error:Field validation for 'ContractAddress' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));

        log.info(" ************************ test investorInfo.personalInfo.certificateNo must ************************ ");
        mapPersonInfo.put("certificateNo","");
        mapInvestorInfo.put("personalInfo",mapPersonInfo);

        response = gd.GDCreateAccout(contractAddr,mapInvestorInfo);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'InvestorsAndContractAddress.InvestorInfo.PersonalInfo.CertificateNo' Error:Field validation for 'CertificateNo' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));


        log.info(" ************************ test investorInfo.personalInfo.certificateAddress must ************************ ");
        mapPersonInfo.put("certificateNo","testcertNo00001");
        mapPersonInfo.put("certificateAddress","");
        mapInvestorInfo.put("personalInfo",mapPersonInfo);

        response = gd.GDCreateAccout(contractAddr,mapInvestorInfo);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'InvestorsAndContractAddress.ContractAddress' Error:Field validation for 'ContractAddress' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));


        log.info(" ************************ test investorInfo.personalInfo.telephone must ************************ ");
        mapPersonInfo.put("certificateAddress","testcertAddr00001");
        mapPersonInfo.put("telephone","");
        mapInvestorInfo.put("personalInfo",mapPersonInfo);

        response = gd.GDCreateAccout(contractAddr,mapInvestorInfo);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'InvestorsAndContractAddress.ContractAddress' Error:Field validation for 'ContractAddress' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));


        log.info(" ************************ test investorInfo.personalInfo.phone must ************************ ");
        mapPersonInfo.put("telephone","testtelephoneNo00001");
        mapPersonInfo.put("phone","");
        mapInvestorInfo.put("personalInfo",mapPersonInfo);

        response = gd.GDCreateAccout(contractAddr,mapInvestorInfo);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'InvestorsAndContractAddress.ContractAddress' Error:Field validation for 'ContractAddress' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));


        log.info(" ************************ test investorInfo.personalInfo.equityCode must ************************ ");
        mapPersonInfo.put("phone","testphoneNo00001");
        mapPersonInfo.put("equityCode","");
        mapInvestorInfo.put("personalInfo",mapPersonInfo);

        response = gd.GDCreateAccout(contractAddr,mapInvestorInfo);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'InvestorsAndContractAddress.InvestorInfo.PersonalInfo.EquityCode' Error:Field validation for 'EquityCode' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));



        log.info(" ************************ test investorInfo.personalInfo.equityAmount must ************************ ");
        mapPersonInfo.clear();
        mapPersonInfo.put("clientFullName",eqCode);
        mapPersonInfo.put("organizationType","苏州股权代码");
        mapPersonInfo.put("certificateType",0);
        mapPersonInfo.put("certificateNo","123456468123153");
        mapPersonInfo.put("certificateAddress","certificateAddress");
        mapPersonInfo.put("gender","苏州同济区块链研究");
        mapPersonInfo.put("telephone","1598222555555");
        mapPersonInfo.put("phone","1598222555555");
        mapPersonInfo.put("postalCode","1585685245666821236");
        mapPersonInfo.put("contactAddress","人民币");
        mapPersonInfo.put("mailBox","1598222555555");
        mapPersonInfo.put("fax","苏州同济区块链研究");
        mapPersonInfo.put("equityCode","苏同院");
//        mapPersonInfo.put("equityAmount",5000);//不传该参数
        mapPersonInfo.put("shareProperty",0);

        mapInvestorInfo.put("personalInfo",mapPersonInfo);

        response = gd.GDCreateAccout(contractAddr,mapInvestorInfo);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'InvestorsAndContractAddress.InvestorInfo.PersonalInfo.EquityAmount' Error:Field validation for 'EquityAmount' failed on the 'gt' tag",
                JSONObject.fromObject(response).getString("message"));


        log.info(" ************************ test investorInfo.personalInfo.shareProperty type mismatch ************************ ");
        mapPersonInfo.put("equityAmount",500);
        mapPersonInfo.put("shareProperty","");

        mapInvestorInfo.put("personalInfo",mapPersonInfo);

        response = gd.GDCreateAccout(contractAddr,mapInvestorInfo);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:json: cannot unmarshal string into Go struct field PersonalInfo.InvestorInfo.PersonalInfo.ShareProperty of type uint16",
                JSONObject.fromObject(response).getString("message"));


        log.info(" ************************ test investorInfo.personalInfo.shareProperty must ************************ ");
        mapPersonInfo.clear();
        mapPersonInfo.put("clientFullName",eqCode);
        mapPersonInfo.put("organizationType","苏州股权代码");
        mapPersonInfo.put("certificateType",0);
        mapPersonInfo.put("certificateNo","123456468123153");
        mapPersonInfo.put("certificateAddress","certificateAddress");
        mapPersonInfo.put("gender","苏州同济区块链研究");
        mapPersonInfo.put("telephone","1598222555555");
        mapPersonInfo.put("phone","1598222555555");
        mapPersonInfo.put("postalCode","1585685245666821236");
        mapPersonInfo.put("contactAddress","人民币");
        mapPersonInfo.put("mailBox","1598222555555");
        mapPersonInfo.put("fax","苏州同济区块链研究");
        mapPersonInfo.put("equityCode","苏同院");
        mapPersonInfo.put("equityAmount",5000);
//        mapPersonInfo.put("shareProperty",0);//不传该参数

        mapInvestorInfo.put("personalInfo",mapPersonInfo);

        response = gd.GDCreateAccout(contractAddr,mapInvestorInfo);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'InvestorsAndContractAddress.ContractAddress' Error:Field validation for 'ContractAddress' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));


        log.info(" ************************ test investorInfo.investor must ************************ ");
        mapPersonInfo.put("shareProperty",0);
        mapInvestorInfo.put("personalInfo",mapPersonInfo);
        mapInvestorInfo.put("investor",null);

        response = gd.GDCreateAccout(contractAddr,mapInvestorInfo);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'InvestorsAndContractAddress.ContractAddress' Error:Field validation for 'ContractAddress' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));

        log.info("required count: " + StringUtils.countOccurrencesOf(response,"required"));


        log.info(" ************************ test investorInfo.investor.currency must ************************ ");
        mapinvestor.put("currency","");
        mapInvestorInfo.put("investor",mapinvestor);

        response = gd.GDCreateAccout(contractAddr,mapInvestorInfo);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'InvestorsAndContractAddress.InvestorInfo.Investor.Currency' Error:Field validation for 'Currency' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));


        log.info(" ************************ test investorInfo.investor.accountType must ************************ ");
        mapinvestor.clear();
        mapinvestor.put("salesDepartment","业务一部");
        mapinvestor.put("clientGroups","群组");
        mapinvestor.put("equityAccountNo","111111");
        mapinvestor.put("currency","人民币");
        mapinvestor.put("board","E板");
//        mapinvestor.put("accountType",0);//不传入该字段
        mapinvestor.put("accountStatus",0);
        mapinvestor.put("registrationDate","621552");
        mapinvestor.put("lastTradingDate","20200828");
        mapinvestor.put("closingDate","20200828");
        mapinvestor.put("shareholderAmount",3);

        mapInvestorInfo.put("investor",mapinvestor);

        response = gd.GDCreateAccout(contractAddr,mapInvestorInfo);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'InvestorsAndContractAddress.ContractAddress' Error:Field validation for 'ContractAddress' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));


        log.info(" ************************ test investorInfo.investor.accountStatus must ************************ ");
        mapinvestor.clear();
        mapinvestor.put("salesDepartment","业务一部");
        mapinvestor.put("clientGroups","群组");
        mapinvestor.put("equityAccountNo","111111");
        mapinvestor.put("currency","人民币");
        mapinvestor.put("board","E板");
        mapinvestor.put("accountType",0);
//        mapinvestor.put("accountStatus",0);//不传入该字段
        mapinvestor.put("registrationDate","621552");
        mapinvestor.put("lastTradingDate","20200828");
        mapinvestor.put("closingDate","20200828");
        mapinvestor.put("shareholderAmount",3);

        mapInvestorInfo.put("investor",mapinvestor);

        response = gd.GDCreateAccout(contractAddr,mapInvestorInfo);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("无效的参数:Key: 'InvestorsAndContractAddress.ContractAddress' Error:Field validation for 'ContractAddress' failed on the 'required' tag",
//                JSONObject.fromObject(response).getString("message"));


        log.info(" ************************ test investorInfo.investor.registrationDate must ************************ ");
        mapinvestor.put("accountStatus",0);
        mapinvestor.put("registrationDate","");
        mapInvestorInfo.put("investor",mapinvestor);

        response = gd.GDCreateAccout(contractAddr,mapInvestorInfo);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'InvestorsAndContractAddress.InvestorInfo.Investor.RegistrationDate' Error:Field validation for 'RegistrationDate' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));


        log.info(" ************************ test investorInfo.investor.lastTradingDate must ************************ ");
        mapinvestor.put("registrationDate","20200828");
        mapinvestor.put("lastTradingDate","");
        mapInvestorInfo.put("investor",mapinvestor);

        response = gd.GDCreateAccout(contractAddr,mapInvestorInfo);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'InvestorsAndContractAddress.InvestorInfo.Investor.LastTradingDate' Error:Field validation for 'LastTradingDate' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));


        log.info(" ************************ test investorInfo.investor.shareholderAmount must ************************ ");
        mapinvestor.clear();
        mapinvestor.put("salesDepartment","业务一部");
        mapinvestor.put("clientGroups","群组");
        mapinvestor.put("equityAccountNo","111111");
        mapinvestor.put("currency","人民币");
        mapinvestor.put("board","E板");
        mapinvestor.put("accountType",0);
        mapinvestor.put("accountStatus",0);
        mapinvestor.put("registrationDate","621552");
        mapinvestor.put("lastTradingDate","20200828");
        mapinvestor.put("closingDate","20200828");
//        mapinvestor.put("shareholderAmount",3);//不传入该参数

        mapInvestorInfo.put("investor",mapinvestor);

        response = gd.GDCreateAccout(contractAddr,mapInvestorInfo);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:Key: 'InvestorsAndContractAddress.InvestorInfo.Investor.ShareholderAmount' Error:Field validation for 'ShareholderAmount' failed on the 'required' tag",
                JSONObject.fromObject(response).getString("message"));



    }

    @Test
    public void TC03_createAccoutInterfaceNotNecessaryParamTest() throws Exception {

        String cltNo = "cI1200002";
        String shareHolderNo = "sI1200002";
        String eqCode = "ECI1200002";

        Map mapPersonInfo = new HashMap();
        mapPersonInfo.put("clientFullName", eqCode);
//        mapPersonInfo.put("organizationType", "苏州股权代码");
        mapPersonInfo.put("certificateType", 0);
        mapPersonInfo.put("certificateNo", "123456468123153");
        mapPersonInfo.put("certificateAddress", "certificateAddress");
//        mapPersonInfo.put("gender", "苏州同济区块链研究");
        mapPersonInfo.put("telephone", "1598222555555");
        mapPersonInfo.put("phone", "1598222555555");
//        mapPersonInfo.put("postalCode", "221005");
//        mapPersonInfo.put("contactAddress", "苏州市相城区");
//        mapPersonInfo.put("mailBox", "1598222555555");
//        mapPersonInfo.put("fax", "苏州同济区块链研究");
        mapPersonInfo.put("equityCode", "苏同院");
        mapPersonInfo.put("equityAmount", 5000);
        mapPersonInfo.put("shareProperty", 0);

        Map mapinvestor = new HashMap();
//        mapinvestor.put("salesDepartment", "业务一部");
//        mapinvestor.put("clientGroups", "群组");
//        mapinvestor.put("equityAccountNo", "111111");
        mapinvestor.put("currency", "人民币");
//        mapinvestor.put("board", "E板");
        mapinvestor.put("accountType", 0);
        mapinvestor.put("accountStatus", 0);
        mapinvestor.put("registrationDate", "621552");
        mapinvestor.put("lastTradingDate", "20200828");
//        mapinvestor.put("closingDate", "20200828");
        mapinvestor.put("shareholderAmount", 3);

        String extend = "";

        Map mapInvestorInfo = new HashMap();

        mapInvestorInfo.put("clientName", "034654");
        mapInvestorInfo.put("shareholderNo", shareHolderNo);
        mapInvestorInfo.put("fundNo", "f0000001");
        mapInvestorInfo.put("clientNo", cltNo);
//        mapInvestorInfo.put("extend", extend);
        mapInvestorInfo.put("personalInfo", mapPersonInfo);
        mapInvestorInfo.put("investor", mapinvestor);


        String response = gd.GDCreateAccout(contractAddr, mapInvestorInfo);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        String txId = jsonObject.getJSONObject("data").getString("txId");
//        assertEquals(clientNo,JSONObject.fromObject(response).getJSONObject("data").getJSONObject("accountList").getString("clientNo"));
//        assertEquals(shareHolderNo,JSONObject.fromObject(response).getJSONObject("data").getJSONObject("accountList").getString("shareholderNo"));
//        String keyId = JSONObject.fromObject(response).getJSONObject("data").getJSONObject("accountList").getString("keyId");
//        String address = JSONObject.fromObject(response).getJSONObject("data").getJSONObject("accountList").getString("address");
//
//        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));
    }

}
