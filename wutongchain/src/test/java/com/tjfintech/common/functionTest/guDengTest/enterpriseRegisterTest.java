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

import java.util.*;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class enterpriseRegisterTest {

    TestBuilder testBuilder= TestBuilder.getInstance();
    GuDeng gd =testBuilder.getGuDeng();
    Store store =testBuilder.getStore();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    String contractAddr = "48cb62af2f6363a5088264ce41193a362455b27fedb1d72eb512a7bfeb339523";
    String platformKeyID = "bt45k19pgfltc7nnqn50";
    String companyId = "company100001";
    String clientNo = "c0000001";
    String equityCode = "SZ100001";


    @Test
    public void TC01_enterpriseRegister() throws Exception {
        String compId = companyId;
        String eqCode = equityCode;

        Map mapBaseInfo = new HashMap();
        mapBaseInfo.put("equityCode",eqCode);
        mapBaseInfo.put("equitySimpleName","苏州股权代码");
        mapBaseInfo.put("equityType",0);
        mapBaseInfo.put("totalShares",1000000);
        mapBaseInfo.put("companyFullName","苏州同济区块链研究");
        mapBaseInfo.put("eNName","Suzhou Tongji Blockchain Research Institute");
        mapBaseInfo.put("certificateType",0);
        mapBaseInfo.put("certificateNo","1585685245666821236");
        mapBaseInfo.put("currency","人民币");
        mapBaseInfo.put("companyId",compId);
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

        String response= gd.GDEnterpriseResister(contractAddr,mapBaseInfo,mapBzInfo,maplegalPersonInfo,extend);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

    }


    @Test
    public void TC03_createAccout() throws Exception {

        String cltNo = clientNo;
        String shareHolderNo = "s0000001";
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
        mapinvestor.put("lastTradingDate","www.wutongchain.com");
        mapinvestor.put("closingDate","test@wutongchain.com");
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
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");
        assertEquals(clientNo,JSONObject.fromObject(response).getJSONObject("data").getJSONObject("accountList").getString("clientNo"));
        assertEquals(shareHolderNo,JSONObject.fromObject(response).getJSONObject("data").getJSONObject("accountList").getString("shareholderNo"));
        String keyId = JSONObject.fromObject(response).getJSONObject("data").getJSONObject("accountList").getString("keyId");
        String address = JSONObject.fromObject(response).getJSONObject("data").getJSONObject("accountList").getString("address");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));
    }

    @Test
    public void TC06_shareIssue() throws Exception {

        String eqCode = equityCode;

        List<Map> shareList = gdConstructShareList("SnxqVBW7K7L8bRykHKttVjG81phwUYu7ZzZMB1bs1qYaA2GBbJM",5000,0);
        List<Map> shareList2 = gdConstructShareList("So6uaUagSbBcDEt935v8sdA52cQ2QFRnVx9nBoaNmzKxomxSRkn",5000,0, shareList);
        List<Map> shareList3 = gdConstructShareList("Sn6KRMf6heVv55V2AWzyE4mF9n8isgshAeZJVMhuW1bG2ARsd15",5000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList("SnnswixfQNaJd9v19LPEFY4UoAmxGtmEivHn6GBnYDD8aPtyjpY",5000,0, shareList3);

        String response= gd.GDShareIssue(contractAddr,platformKeyID,eqCode,shareList4);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));
    }

    @Test
    public void TC07_shareChangeProperty() throws Exception {

        String eqCode = equityCode;
        String address = "SnxqVBW7K7L8bRykHKttVjG81phwUYu7ZzZMB1bs1qYaA2GBbJM";
        double changeAmount = 500;
        int oldProperty = 0;
        int newProperty = 1;

        String response= gd.GDShareChangeProperty(platformKeyID,address,eqCode,changeAmount,oldProperty,newProperty);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

    }

    @Test
    public void TC08_shareTransfer()throws Exception{
        String keyId = "bt3hd3ppgfltc7nnqlt0";
        String fromAddr = "SnxqVBW7K7L8bRykHKttVjG81phwUYu7ZzZMB1bs1qYaA2GBbJM";
        double amount = 100;
        String toAddr = "SnxqVBW7K7L8bRykHKttVjG81phwUYu7ZzZMB1bs1qYaA2GBbJM";
        int shareProperty = 0;
        String eqCode = equityCode;
        int txType = 0;
        String orderNo = "test202008280952";
        int orderWay = 0;
        int orderType = 0;
        String price = "10000";
        String time = "20200828";
        String remark = "转账";
        String response= gd.GDShareTransfer(keyId,fromAddr,amount,toAddr,shareProperty,eqCode,txType,
                orderNo,orderWay,orderType,price,time,remark);

        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));
    }

    @Test
    public void TC09_shareIncrease() throws Exception {

        String eqCode = equityCode;
        String reason = "股份分红";

        List<Map> shareList = gdConstructShareList("SnxqVBW7K7L8bRykHKttVjG81phwUYu7ZzZMB1bs1qYaA2GBbJM",1000,0);
        List<Map> shareList2 = gdConstructShareList("So6uaUagSbBcDEt935v8sdA52cQ2QFRnVx9nBoaNmzKxomxSRkn",1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList("Sn6KRMf6heVv55V2AWzyE4mF9n8isgshAeZJVMhuW1bG2ARsd15",1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList("SnnswixfQNaJd9v19LPEFY4UoAmxGtmEivHn6GBnYDD8aPtyjpY",1000,0, shareList3);

        String response= gd.GDShareIncrease(contractAddr,platformKeyID,eqCode,shareList4,reason);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));
    }


    @Test
    public void TC10_shareLock() throws Exception {

        String bizNo = "test20200828001";
        String eqCode = equityCode;
        String address = "SnxqVBW7K7L8bRykHKttVjG81phwUYu7ZzZMB1bs1qYaA2GBbJM";
        double lockAmount = 500;
        int shareProperty = 0;
        String reason = "司法冻结";
        String cutoffDate = "20200930";

        String response= gd.GDShareLock(bizNo,address,eqCode,lockAmount,shareProperty,reason,cutoffDate);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

    }

    @Test
    public void TC11_shareUnlock() throws Exception {

        String bizNo = "test20200828001";
        String eqCode = equityCode;
        double amount = 100;

        String response= gd.GDShareUnlock(bizNo,eqCode,amount);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

    }

    @Test
    public void TC12_shareRecycle() throws Exception {

        String eqCode = equityCode;
        String remark = "777777";

        List<Map> shareList = gdConstructShareList("SnxqVBW7K7L8bRykHKttVjG81phwUYu7ZzZMB1bs1qYaA2GBbJM",1000,0);
        List<Map> shareList2 = gdConstructShareList("So6uaUagSbBcDEt935v8sdA52cQ2QFRnVx9nBoaNmzKxomxSRkn",1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList("Sn6KRMf6heVv55V2AWzyE4mF9n8isgshAeZJVMhuW1bG2ARsd15",1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList("SnnswixfQNaJd9v19LPEFY4UoAmxGtmEivHn6GBnYDD8aPtyjpY",1000,0, shareList3);

        String response= gd.GDShareRecycle(platformKeyID,eqCode,shareList4,remark);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));
    }

    @Test
    public void TC13_shareChangeBoard() throws Exception {

        String oldEquityCode = equityCode;
        String newEquityCode = "SZ100003";
        String cpnyId = "XXXX";

        String response= gd.GDShareChangeBoard(platformKeyID,cpnyId,oldEquityCode,newEquityCode);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

    }


    @Test
    public void TC205_accountDestroy() throws Exception {
        String clntNo = clientNo;

        String response= gd.GDAccountDestroy(contractAddr,clntNo);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

    }


    @Test
    public void TC15_infodisclosurePublishAndGet() throws Exception {
        String type = "公告";
        String subType = "企业公告";
        String title = "挂牌企业登记信息";
        String fileHash = "dfhafdd1111111651575452";
        String fileURL = "test/publish/company0001info";
        String hashAlgo = "sha256";
        String publisher = "上海股权托管登记交易所";
        String publishTime = "20200828 10:43";
        String enterprise = "201804152125222515";

        String response= gd.GDInfoPublish(type,subType,title,fileHash,fileURL,hashAlgo,publisher,publishTime,enterprise);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getString("data");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));
        
        String responseGet = gd.GDInfoPublishGet(txId);
        assertEquals(type,JSONObject.fromObject(responseGet).getJSONObject("data").getString("type"));
        assertEquals(subType,JSONObject.fromObject(responseGet).getJSONObject("data").getString("subtype"));
        assertEquals(title,JSONObject.fromObject(responseGet).getJSONObject("data").getString("title"));
        assertEquals(fileHash,JSONObject.fromObject(responseGet).getJSONObject("data").getString("fileHash"));
        assertEquals(fileURL,JSONObject.fromObject(responseGet).getJSONObject("data").getString("fileURL"));
        assertEquals(hashAlgo,JSONObject.fromObject(responseGet).getJSONObject("data").getString("hashAlgo"));
        assertEquals(publisher,JSONObject.fromObject(responseGet).getJSONObject("data").getString("publisher"));
        assertEquals(publishTime,JSONObject.fromObject(responseGet).getJSONObject("data").getString("publishTime"));
        assertEquals(enterprise,JSONObject.fromObject(responseGet).getJSONObject("data").getString("enterprise"));

    }





    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public  List<Map>   gdConstructShareList(String address, double amount, int shareProperty){

        Map<String,Object> shares = new HashMap<>();
        shares.put("address",address);
        shares.put("amount",amount);
        shares.put("shareProperty",shareProperty);

        List<Map> shareList = new ArrayList<>();
        shareList.add(shares);
        return shareList;
    }

    public  List<Map>   gdConstructShareList(String address, double amount, int shareProperty, List<Map> list){
        List<Map> shareList = new ArrayList<>();
        for(int i = 0 ; i < list.size() ; i++) {
            shareList.add(list.get(i));
        }
        Map<String,Object> shares = new HashMap<>();
        shares.put("address",address);
        shares.put("amount",amount);
        shares.put("shareProperty",shareProperty);

        shareList.add(shares);
        return shareList;
    }

}
