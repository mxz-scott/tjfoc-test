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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.CommonFunc.gdConstructShareList;
import static com.tjfintech.common.utils.UtilsClass.SLEEPTIME;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class GuDengInterfaceTest {

    TestBuilder testBuilder= TestBuilder.getInstance();
    GuDeng gd =testBuilder.getGuDeng();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    Store store =testBuilder.getStore();
    String contractAddr = "48cb62af2f6363a5088264ce41193a362455b27fedb1d72eb512a7bfeb339523";
    String platformKeyID = "bt45k19pgfltc7nnqn50";
    String equityCode = "SZ100001";


    @Test
    public void enterpriseIssueInterfaceTest() throws Exception {

        Map mapBaseInfo = new HashMap();
        mapBaseInfo.put("equityCode","SZ100001");
        mapBaseInfo.put("equitySimpleName","苏州股权代码");
        mapBaseInfo.put("equityType",0);
        mapBaseInfo.put("totalShares",1000000);
        mapBaseInfo.put("companyFullName","苏州同济区块链研究");
        mapBaseInfo.put("eNName","Suzhou Tongji Blockchain Research Institute");
        mapBaseInfo.put("certificateType",0);
        mapBaseInfo.put("certificateNo","1585685245666821236");
        mapBaseInfo.put("currency","人民币");
        mapBaseInfo.put("companyId","1598222555555");
        mapBaseInfo.put("companyLogo","苏州同济区块链研究");
        mapBaseInfo.put("companySimpleName","苏同院");
        mapBaseInfo.put("companyENName","tjfoc");
        mapBaseInfo.put("pinyinCode","suzhoutongjiqukuailianyanjiuyuan");
        mapBaseInfo.put("txStatus",0);
        mapBaseInfo.put("listingDate","2018-09-01 18:56:56");
        mapBaseInfo.put("delistingDate","2018-09-01 18:56:56");
        mapBaseInfo.put("industry","互联网");
        mapBaseInfo.put("enterpriseStatus",0);

        Map mapBzInfo = new HashMap();
        mapBzInfo.put("province","江苏省");
        mapBzInfo.put("city","苏州");
        mapBzInfo.put("registeredAddress","相城区");
        mapBzInfo.put("officeAddress","领寓商务");
        mapBzInfo.put("contactAddress","相城区领寓商务广场12楼");
        mapBzInfo.put("phone","051261116444");
        mapBzInfo.put("fax","051261116444");
        mapBzInfo.put("postalCode","621552");
        mapBzInfo.put("internetAddress","www.wutongchain.com");
        mapBzInfo.put("mailBox","test@wutongchain.com");
        mapBzInfo.put("registeredCapital","1000000");

        Map maplegalPersonInfo = new HashMap();
        maplegalPersonInfo.put("name","苏同院");
        maplegalPersonInfo.put("certificateType",0);
        maplegalPersonInfo.put("certificateNo","123456789684984");
        maplegalPersonInfo.put("job","test");
        maplegalPersonInfo.put("legalPersonPhone","15685297828");

        String extend = "{\"city\":\"苏州\"}";

        //验证接口正常
        String response= gd.GDEnterpriseResister(contractAddr,mapBaseInfo,mapBzInfo,maplegalPersonInfo,extend);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        //BasicInfo.equityCode为空
        mapBaseInfo.put("equityCode","");
        response= gd.GDEnterpriseResister(contractAddr,mapBaseInfo,mapBzInfo,maplegalPersonInfo,extend);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("Error:Field validation for 'EquityCode' failed on the 'required"));

        //BasicInfo.equitySimpleName为空
        mapBaseInfo.put("equityCode","SZ100001");
        mapBaseInfo.put("equitySimpleName","");
        response= gd.GDEnterpriseResister(contractAddr,mapBaseInfo,mapBzInfo,maplegalPersonInfo,extend);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("Error:Field validation for 'EquitySimpleName' failed on the 'required"));

        //BasicInfo.companyFullName为空
        mapBaseInfo.put("equitySimpleName","苏州股权代码");
        mapBaseInfo.put("companyFullName","");
        response= gd.GDEnterpriseResister(contractAddr,mapBaseInfo,mapBzInfo,maplegalPersonInfo,extend);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("Error:Field validation for 'CompanyFullName' failed on the 'required"));

        //BasicInfo.eNName为空
        mapBaseInfo.put("companyFullName","苏州同济区块链研究");
        mapBaseInfo.put("eNName","");
        response= gd.GDEnterpriseResister(contractAddr,mapBaseInfo,mapBzInfo,maplegalPersonInfo,extend);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("Error:Field validation for 'ENName' failed on the 'required"));

        //BasicInfo.certificateNo为空
        mapBaseInfo.put("eNName","Suzhou Tongji Blockchain Research Institute");
        mapBaseInfo.put("certificateNo","");
        response= gd.GDEnterpriseResister(contractAddr,mapBaseInfo,mapBzInfo,maplegalPersonInfo,extend);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("Error:Field validation for 'CertificateNo' failed on the 'required"));

        //BasicInfo.currency为空
        mapBaseInfo.put("certificateNo","1585685245666821236");
        mapBaseInfo.put("currency","");
        response= gd.GDEnterpriseResister(contractAddr,mapBaseInfo,mapBzInfo,maplegalPersonInfo,extend);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("Error:Field validation for 'Currency' failed on the 'required"));

        //BasicInfo.companyId为空
        mapBaseInfo.put("currency","人民币");
        mapBaseInfo.put("companyId","");
        response= gd.GDEnterpriseResister(contractAddr,mapBaseInfo,mapBzInfo,maplegalPersonInfo,extend);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("Error:Field validation for 'CompanyId' failed on the 'required"));

        //BasicInfo.listingDate为空
        mapBaseInfo.put("companyId","1598222555555");
        mapBaseInfo.put("listingDate","");
        response= gd.GDEnterpriseResister(contractAddr,mapBaseInfo,mapBzInfo,maplegalPersonInfo,extend);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("Error:Field validation for 'ListingDate' failed on the 'required"));

        //BasicInfo.delistingDate为空
        mapBaseInfo.put("listingDate","2018-09-01 18:56:56");
        mapBaseInfo.put("delistingDate","");
        response= gd.GDEnterpriseResister(contractAddr,mapBaseInfo,mapBzInfo,maplegalPersonInfo,extend);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("Error:Field validation for 'DelistingDate' failed on the 'required"));

        //LegalPersonInfo.name为空
        maplegalPersonInfo.put("delistingDate","2018-09-01 18:56:56");
        maplegalPersonInfo.put("name","");
        response= gd.GDEnterpriseResister(contractAddr,mapBaseInfo,mapBzInfo,maplegalPersonInfo,extend);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("Error:Field validation for 'Name' failed on the 'required"));

        //LegalPersonInfo.certificateNo为空
        maplegalPersonInfo.put("name","苏同院");
        maplegalPersonInfo.put("certificateNo","");
        response= gd.GDEnterpriseResister(contractAddr,mapBaseInfo,mapBzInfo,maplegalPersonInfo,extend);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("Error:Field validation for 'CertificateNo' failed on the 'required"));

        //contractAddress为空
        maplegalPersonInfo.put("certificateNo","1585685245666821236");
        response= gd.GDEnterpriseResister("",mapBaseInfo,mapBzInfo,maplegalPersonInfo,extend);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("Error:Field validation for 'ContractAddress' failed on the 'required"));

        //BasicInfo.equityType不传参
        mapBaseInfo.clear();
        mapBaseInfo.put("equityCode","SZ100001");
        mapBaseInfo.put("equitySimpleName","苏州股权代码");

        mapBaseInfo.put("totalShares",1000000);
        mapBaseInfo.put("companyFullName","苏州同济区块链研究");
        mapBaseInfo.put("eNName","Suzhou Tongji Blockchain Research Institute");
        mapBaseInfo.put("certificateType",0);
        mapBaseInfo.put("certificateNo","1585685245666821236");
        mapBaseInfo.put("currency","人民币");
        mapBaseInfo.put("companyId","1598222555555");
        mapBaseInfo.put("companyLogo","苏州同济区块链研究");
        mapBaseInfo.put("companySimpleName","苏同院");
        mapBaseInfo.put("companyENName","tjfoc");
        mapBaseInfo.put("pinyinCode","suzhoutongjiqukuailianyanjiuyuan");
        mapBaseInfo.put("txStatus",0);
        mapBaseInfo.put("listingDate","2018-09-01 18:56:56");
        mapBaseInfo.put("delistingDate","2018-09-01 18:56:56");
        mapBaseInfo.put("industry","互联网");
        mapBaseInfo.put("enterpriseStatus",0);
        response= gd.GDEnterpriseResister(contractAddr,mapBaseInfo,mapBzInfo,maplegalPersonInfo,extend);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("Error:Field validation for 'EquityType' failed on the 'required"));

        //BasicInfo.totalShares不传参
        mapBaseInfo.clear();
        mapBaseInfo.put("equityCode","SZ100001");
        mapBaseInfo.put("equitySimpleName","苏州股权代码");
        mapBaseInfo.put("equityType",0);

        mapBaseInfo.put("companyFullName","苏州同济区块链研究");
        mapBaseInfo.put("eNName","Suzhou Tongji Blockchain Research Institute");
        mapBaseInfo.put("certificateType",0);
        mapBaseInfo.put("certificateNo","1585685245666821236");
        mapBaseInfo.put("currency","人民币");
        mapBaseInfo.put("companyId","1598222555555");
        mapBaseInfo.put("companyLogo","苏州同济区块链研究");
        mapBaseInfo.put("companySimpleName","苏同院");
        mapBaseInfo.put("companyENName","tjfoc");
        mapBaseInfo.put("pinyinCode","suzhoutongjiqukuailianyanjiuyuan");
        mapBaseInfo.put("txStatus",0);
        mapBaseInfo.put("listingDate","2018-09-01 18:56:56");
        mapBaseInfo.put("delistingDate","2018-09-01 18:56:56");
        mapBaseInfo.put("industry","互联网");
        mapBaseInfo.put("enterpriseStatus",0);
        response= gd.GDEnterpriseResister(contractAddr,mapBaseInfo,mapBzInfo,maplegalPersonInfo,extend);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("Error:Field validation for 'TotalShares' failed on the 'required"));

        //BasicInfo.certificateType不传参
        mapBaseInfo.clear();
        mapBaseInfo.put("equityCode","SZ100001");
        mapBaseInfo.put("equitySimpleName","苏州股权代码");
        mapBaseInfo.put("equityType",0);
        mapBaseInfo.put("totalShares",1000000);
        mapBaseInfo.put("companyFullName","苏州同济区块链研究");
        mapBaseInfo.put("eNName","Suzhou Tongji Blockchain Research Institute");

        mapBaseInfo.put("certificateNo","1585685245666821236");
        mapBaseInfo.put("currency","人民币");
        mapBaseInfo.put("companyId","1598222555555");
        mapBaseInfo.put("companyLogo","苏州同济区块链研究");
        mapBaseInfo.put("companySimpleName","苏同院");
        mapBaseInfo.put("companyENName","tjfoc");
        mapBaseInfo.put("pinyinCode","suzhoutongjiqukuailianyanjiuyuan");
        mapBaseInfo.put("txStatus",0);
        mapBaseInfo.put("listingDate","2018-09-01 18:56:56");
        mapBaseInfo.put("delistingDate","2018-09-01 18:56:56");
        mapBaseInfo.put("industry","互联网");
        mapBaseInfo.put("enterpriseStatus",0);
        response= gd.GDEnterpriseResister(contractAddr,mapBaseInfo,mapBzInfo,maplegalPersonInfo,extend);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("Error:Field validation for 'CertificateType' failed on the 'required"));

        //BasicInfo.txStatus不传参
        mapBaseInfo.clear();
        mapBaseInfo.put("equityCode","SZ100001");
        mapBaseInfo.put("equitySimpleName","苏州股权代码");
        mapBaseInfo.put("equityType",0);
        mapBaseInfo.put("totalShares",1000000);
        mapBaseInfo.put("companyFullName","苏州同济区块链研究");
        mapBaseInfo.put("eNName","Suzhou Tongji Blockchain Research Institute");
        mapBaseInfo.put("certificateType",0);
        mapBaseInfo.put("certificateNo","1585685245666821236");
        mapBaseInfo.put("currency","人民币");
        mapBaseInfo.put("companyId","1598222555555");
        mapBaseInfo.put("companyLogo","苏州同济区块链研究");
        mapBaseInfo.put("companySimpleName","苏同院");
        mapBaseInfo.put("companyENName","tjfoc");
        mapBaseInfo.put("pinyinCode","suzhoutongjiqukuailianyanjiuyuan");

        mapBaseInfo.put("listingDate","2018-09-01 18:56:56");
        mapBaseInfo.put("delistingDate","2018-09-01 18:56:56");
        mapBaseInfo.put("industry","互联网");
        mapBaseInfo.put("enterpriseStatus",0);
        response= gd.GDEnterpriseResister(contractAddr,mapBaseInfo,mapBzInfo,maplegalPersonInfo,extend);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("Error:Field validation for 'TxStatus' failed on the 'required"));

        //LegalPersonPhone.certificateType不传参
        mapBaseInfo.put("txStatus",0);
        maplegalPersonInfo.clear();
        maplegalPersonInfo.put("name","苏同院");
        maplegalPersonInfo.put("certificateType",0);

        maplegalPersonInfo.put("job","test");
        maplegalPersonInfo.put("legalPersonPhone","15685297828");
        response= gd.GDEnterpriseResister(contractAddr,mapBaseInfo,mapBzInfo,maplegalPersonInfo,extend);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("Error:Field validation for 'CertificateType' failed on the 'required"));


    }


    @Test
    public void shareIncreaseInterfaceTest() throws Exception {

        String eqCode = equityCode;
        String reason = "股份分红";

        List<Map> shareList = gdConstructShareList("SnxqVBW7K7L8bRykHKttVjG81phwUYu7ZzZMB1bs1qYaA2GBbJM",1000,0);
        List<Map> shareList2 = gdConstructShareList("So6uaUagSbBcDEt935v8sdA52cQ2QFRnVx9nBoaNmzKxomxSRkn",1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList("Sn6KRMf6heVv55V2AWzyE4mF9n8isgshAeZJVMhuW1bG2ARsd15",1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList("SnnswixfQNaJd9v19LPEFY4UoAmxGtmEivHn6GBnYDD8aPtyjpY",1000,0, shareList3);

        //验证接口正常
        String response= gd.GDShareIncrease(contractAddr,platformKeyID,eqCode,shareList4,reason);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        //platformKeyId为空
        response= gd.GDShareIncrease(contractAddr,"",eqCode,shareList4,reason);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("无效的参数"));

        //equityCode为空
        response= gd.GDShareIncrease(contractAddr,platformKeyID,"",shareList4,reason);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("无效的参数"));

        //shareList.address为空
        shareList4.clear();
        shareList4 = gdConstructShareList("",1000,0, shareList3);
        response= gd.GDShareIncrease(contractAddr,platformKeyID,"",shareList4,reason);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("无效的参数"));

//        //shareList.amount不传参
//        shareList4.clear();
//        shareList4 = gdConstructShareList("",1000,0, shareList3);
//        response= gd.GDShareIncrease(contractAddr,platformKeyID,"",shareList4,reason);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("Error:Field validation for 'EquityCode' failed on the 'required"));
//
//        //shareList.shareProperty不传参
//        shareList4.clear();
//        shareList4 = gdConstructShareList("",1000,0, shareList3);
//        response= gd.GDShareIncrease(contractAddr,platformKeyID,"",shareList4,reason);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("Error:Field validation for 'EquityCode' failed on the 'required"));


    }

    @Test
    public void shareLockInterfaceTest() throws Exception {

        String bizNo = "test20200828001";
        String eqCode = equityCode;
        String address = "SnxqVBW7K7L8bRykHKttVjG81phwUYu7ZzZMB1bs1qYaA2GBbJM";
        double lockAmount = 500;
        int shareProperty = 0;
        String reason = "司法冻结";
        String cutoffDate = "20200930";

        //验证接口正常
        String response= gd.GDShareLock(bizNo,address,eqCode,lockAmount,shareProperty,reason,cutoffDate);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        //bizNo为空
        response= gd.GDShareLock("",address,eqCode,lockAmount,shareProperty,reason,cutoffDate);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("Error:Field validation for 'BizNo' failed on the 'required"));

        //address为空
        response= gd.GDShareLock(bizNo,"",eqCode,lockAmount,shareProperty,reason,cutoffDate);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("Error:Field validation for 'Address' failed on the 'required"));

        //equityCode为空
        response= gd.GDShareLock(bizNo,address,"",lockAmount,shareProperty,reason,cutoffDate);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("Error:Field validation for 'EquityCode' failed on the 'required"));

//        //amount不传参
//        response= gd.GDShareLock(bizNo,address,eqCode,lockAmount,shareProperty,reason,cutoffDate);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("Error:Field validation for 'EquityCode' failed on the 'required"));
//
//        //shareProperty不传参
//        response= gd.GDShareLock(bizNo,address,eqCode,lockAmount,shareProperty,reason,cutoffDate);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("Error:Field validation for 'EquityCode' failed on the 'required"));

        //reason为空
        response= gd.GDShareLock(bizNo,address,eqCode,lockAmount,shareProperty,"",cutoffDate);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("Error:Field validation for 'Reason' failed on the 'required"));

        //cutoffDate为空
        response= gd.GDShareLock(bizNo,address,eqCode,lockAmount,shareProperty,reason,"");
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("Error:Field validation for 'CutoffDate' failed on the 'required"));
    }

    @Test
    public void shareUnlockInterfaceTest() throws Exception {

        String bizNo = "test20200828001";
        String eqCode = equityCode;
        double amount = 100;

        //验证接口正常
        String response= gd.GDShareUnlock(bizNo,eqCode,amount);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        //bizNo为空
        response= gd.GDShareUnlock("",eqCode,amount);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("Error:Field validation for 'BizNo' failed on the 'required"));

        //equityCode为空
        response= gd.GDShareUnlock(bizNo,"",amount);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("Error:Field validation for 'EquityCode' failed on the 'required"));

//        //amount为空
//        response= gd.GDShareUnlock(bizNo,eqCode,amount);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("Error:Field validation for 'EquityCode' failed on the 'required"));

    }

    @Test
    public void shareRecycleInterfaceTest() throws Exception {

        String eqCode = equityCode;
        String remark = "777777";

        List<Map> shareList = gdConstructShareList("SnxqVBW7K7L8bRykHKttVjG81phwUYu7ZzZMB1bs1qYaA2GBbJM",1000,0);
        List<Map> shareList2 = gdConstructShareList("So6uaUagSbBcDEt935v8sdA52cQ2QFRnVx9nBoaNmzKxomxSRkn",1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList("Sn6KRMf6heVv55V2AWzyE4mF9n8isgshAeZJVMhuW1bG2ARsd15",1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList("SnnswixfQNaJd9v19LPEFY4UoAmxGtmEivHn6GBnYDD8aPtyjpY",1000,0, shareList3);

        //验证接口正常
        String response= gd.GDShareRecycle(platformKeyID,eqCode,shareList4,remark);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        //platformKeyId为空
        response= gd.GDShareRecycle("",eqCode,shareList4,remark);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("Error:Field validation for 'EquityCode' failed on the 'required"));

        //equityCode为空
        response= gd.GDShareRecycle(platformKeyID,"",shareList4,remark);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("Error:Field validation for 'EquityCode' failed on the 'required"));

        //AddressList.address为空
        shareList4.clear();
        shareList4 = gdConstructShareList("",1000,0,shareList3);
        response= gd.GDShareRecycle(platformKeyID,eqCode,shareList4,remark);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("Error:Field validation for 'EquityCode' failed on the 'required"));

//        //AddressList.amount不传参
//        response= gd.GDShareRecycle(platformKeyID,eqCode,shareList4,remark);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("Error:Field validation for 'EquityCode' failed on the 'required"));
//
//        //AddressList.shareProperty不传参
//        response= gd.GDShareRecycle(platformKeyID,eqCode,shareList4,remark);
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("Error:Field validation for 'EquityCode' failed on the 'required"));

        //equityCode为空
        response= gd.GDShareRecycle(platformKeyID,"",shareList4,remark);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("Error:Field validation for 'EquityCode' failed on the 'required"));

    }



}
