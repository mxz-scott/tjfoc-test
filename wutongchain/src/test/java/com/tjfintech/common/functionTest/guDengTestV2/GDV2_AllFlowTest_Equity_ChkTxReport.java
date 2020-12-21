package com.tjfintech.common.functionTest.guDengTestV2;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.GuDeng;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.*;
import org.junit.rules.TestName;
import org.junit.runners.MethodSorters;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.tjfintech.common.functionTest.guDengTestV2.GDCommonFunc.*;
import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassGD.*;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class GDV2_AllFlowTest_Equity_ChkTxReport {

    TestBuilder testBuilder= TestBuilder.getInstance();
    GuDeng gd =testBuilder.getGuDeng();
    Store store =testBuilder.getStore();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    GDBeforeCondition gdBF = new GDBeforeCondition();
    GDCommonFunc gdCF = new GDCommonFunc();
    GDUnitFunc uf = new GDUnitFunc();
    public static String bizNoTest = "TxRp" + Random(12);
    Boolean bNotCheck = false;
    public static String oldEquity = "";
    public static String newEquity = "";

//    long start = (new Date()).getTime();
//    long end = 0;
//    int beginHeigh = Integer.parseInt(JSONObject.fromObject(store.GetHeight()).getString("data"));
//    int endHeight = 0;
    @Rule
    public TestName tm = new TestName();

    @BeforeClass
    public static void Before()throws Exception{
        TestBuilder tbTemp = TestBuilder.getInstance();
        Store storeTemp =tbTemp.getStore();
        beginHeigh = Integer.parseInt(JSONObject.fromObject(storeTemp.GetHeight()).getString("data"));
        start = (new Date()).getTime();

        GDBeforeCondition gdBefore = new GDBeforeCondition();
        gdBefore.gdCreateAccout();
        gdBefore.initRegulationData();
        bondProductInfo = null;
        equityProductInfo = gdBefore.init03EquityProductInfo();
        fundProductInfo = null;
        gdCompanyID = "chkRpSub" + Random(5);
        gdEquityCode = "chkRpProd" + Random(12);
        oldEquity = gdEquityCode;
    }

    @After
    public void calJGDataAfterTx()throws Exception{
        testCurMethodName = tm.getMethodName();
        GDUnitFunc uf = new GDUnitFunc();
        int endHeight = net.sf.json.JSONObject.fromObject(store.GetHeight()).getInt("data");
        uf.checkJGHeaderOpVer(blockHeight,endHeight);
        uf.updateBlockHeightParam(endHeight);
    }

    @Test
    public void TC01_enterpriseRegister() throws Exception {
        long shareTotals = 1000000;
        Map eqProd = gdBF.init03EquityProductInfo();
        Map enSub = gdBF.init01EnterpriseSubjectInfo();
        String response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,enSub, eqProd,bondProductInfo,fundProductInfo);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        response = store.GetTxDetail(txId);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));


        //查询投资者信息
        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("400",JSONObject.fromObject(query).getString("state"));
        assertEquals("股权代码还未发行或者已经转场",JSONObject.fromObject(query).getString("message"));

        //查询股东持股情况 无当前股权代码信息
        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo3);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo4);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo5);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo6);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

    }


    @Test
    public void TC03_createAccout() throws Exception {
        GDBeforeCondition gdBC = new GDBeforeCondition();

        String cltNo = "testclientNo" + Random(6);
        Map<String,String> mapAcc = gdBC.gdCreateAccParam(cltNo);
        log.info(mapAcc.toString());
        assertEquals(cltNo,JSONObject.fromObject(mapAcc.get("response")).getJSONObject("data").getJSONObject("accountList").getString("clientNo"));
        assertEquals("SH" + cltNo,JSONObject.fromObject(mapAcc.get("response")).getJSONObject("data").getJSONObject("accountList").getString("shareholderNo"));
        String keyId = mapAcc.get("keyId");
        String address = mapAcc.get("address");

        commonFunc.sdkCheckTxOrSleep(mapAcc.get("txId").toString(),utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(mapAcc.get("txId").toString())).getString("state"));

        String query2 = gd.GDAccountQuery(gdContractAddress,cltNo);
        assertEquals("200",JSONObject.fromObject(query2).getString("state"));
        assertEquals(true,query2.contains(cltNo));

        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("400",JSONObject.fromObject(query).getString("state"));
        assertEquals("股权代码还未发行或者已经转场",JSONObject.fromObject(query).getString("message"));

        //查询股东持股情况 无当前股权代码信息
        query = gd.GDGetShareHolderInfo(gdContractAddress,cltNo);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        //投资者信息查询
        String query3 = gd.GDMainSubjectQuery(gdContractAddress,cltNo);

    }

    @Test
    public void TC06_shareIssue() throws Exception {

        List<Map> shareList = gdConstructShareList(gdAccount1,5000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,5000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,5000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,5000,0,shareList3);

        String response= gd.GDShareIssue(gdContractAddress,gdPlatfromKeyID,gdEquityCode,shareList4);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        String testReturn = "";
        if(testReturn == "" && bNotCheck) return;

        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        String query = "";
        for(int i =0 ;i < 20;i++) {
            query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
            if(JSONObject.fromObject(query).getString("state").equals("200")) break;
            sleepAndSaveInfo(100,"等待数据更新");
        }
        assertEquals("200",JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");

        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,5000,0,0,mapShareENCN().get("0"),respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2,5000,0,0,mapShareENCN().get("0"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3,5000,0,0,mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4,5000,0,0,mapShareENCN().get("0"), respShareList3);

        //检查存在余额的股东列表
        assertEquals(respShareList4.size(),dataShareList.size());

        List<Map> getShareList = getShareListFromQueryNoZeroAcc(dataShareList);

        assertEquals(respShareList4.size(),getShareList.size());
        assertEquals(true,respShareList4.containsAll(getShareList) && getShareList.containsAll(respShareList4));


        //查询股东持股情况 无当前股权代码信息
        query = gd.GDGetShareHolderInfo(gdContractAddress, gdAccClientNo1);
        assertEquals(gdAccClientNo1,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));

        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo1 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount1 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":5000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(gdAccClientNo2,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo2 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount2 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":5000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo3);
        assertEquals(gdAccClientNo3,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo3 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount3 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":5000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo4);
        assertEquals(gdAccClientNo4,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo4 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount4 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":5000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo5);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo6);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));
    }

    /***
     * 股权性质变更 部分变更
     * 变更后 未变更部分保持原有股权性质不变 P17对应的需求点
     * @throws Exception
     */
    @Test
    public void TC07_shareChangeProperty() throws Exception {

        String eqCode = gdEquityCode;
        String address = gdAccount1;
        long changeAmount = 500;
        int oldProperty = 0;
        int newProperty = 1;
        Map testReg1 = gdBF.init05RegInfo();
        Map testReg2 = gdBF.init05RegInfo();
        String regObjId1 = mapAccAddr.get(address) + "CProp1" + Random(6);
        String regObjId2 = mapAccAddr.get(address) + "CProp2" + Random(6);
        testReg1.put("register_registration_serial_number","ChangeProperty000001");
        testReg1.put("register_account_obj_id",mapAccAddr.get(address));
        testReg1.put("register_registration_object_id",regObjId1);

        testReg2.put("register_registration_serial_number","ChangeProperty000001");
        testReg2.put("register_account_obj_id",mapAccAddr.get(address));
        testReg2.put("register_registration_object_id",regObjId2);
        List<Map> regListInfo = new ArrayList<>();
        regListInfo.add(testReg1);
        regListInfo.add(testReg2);

        String response= gd.GDShareChangeProperty(gdPlatfromKeyID,address,eqCode,changeAmount,oldProperty,newProperty,regListInfo);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));


        String testReturn = "";
        if(testReturn == "" && bNotCheck) return;

        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");

        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,4500,0,0,mapShareENCN().get("0"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount1,500,1,0,mapShareENCN().get("1"),respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2,5000,0,0,mapShareENCN().get("0"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3,5000,0,0,mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4,5000,0,0,mapShareENCN().get("0"), respShareList3);

        //检查存在余额的股东列表
        assertEquals(respShareList4.size(),dataShareList.size());

        List<Map> getShareList = getShareListFromQueryNoZeroAcc(dataShareList);

        assertEquals(respShareList4.size(),getShareList.size());
        assertEquals(true,respShareList4.containsAll(getShareList) && getShareList.containsAll(respShareList4));


        //查询股东持股情况 无当前股权代码信息
        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        assertEquals(gdAccClientNo1,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
//        String get = JSONObject.fromObject(JSONObject.fromObject(query).getJSONObject("data").getJSONArray("accountList").get(0)).getString("shareholderNo");
//        assertEquals("SH"+gdAccClientNo1,get);

        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo1 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount1 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":4500,\"lockAmount\":0}"));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":500,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(gdAccClientNo2,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo2 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount2 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":5000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo3);
        assertEquals(gdAccClientNo3,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo3 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount3 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":5000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo4);
        assertEquals(gdAccClientNo4,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo4 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount4 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":5000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo5);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo6);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));
    }

    @Test
    public void TC08_shareTransfer()throws Exception{
        String keyId = gdAccountKeyID1;
        String fromAddr = gdAccount1;
        long amount = 1000;
        String toAddr = gdAccount5;
        int shareProperty = 0;
        String eqCode = gdEquityCode;

        register_subject_account_ref = gdCompanyID;
        Map testReg1 = gdBF.init05RegInfo();
        Map testReg2 = gdBF.init05RegInfo();
        String regObjId1 = mapAccAddr.get(fromAddr) + "Trf1" + Random(6);
        String regObjId2 = mapAccAddr.get(toAddr) + "Trf2" + Random(6);

        testReg1.put("register_registration_serial_number","transfer000001");
        testReg1.put("register_account_obj_id",mapAccAddr.get(fromAddr));
        testReg1.put("register_registration_object_id",regObjId1);

        testReg2.put("register_registration_serial_number","transfer000001");
        testReg2.put("register_account_obj_id",mapAccAddr.get(toAddr));
        testReg2.put("register_registration_object_id",regObjId2);

        txInformation.put("transaction_object_id",mapAccAddr.get(toAddr) + "TrfTx" + Random(6));

        List<Map> regInfoList = new ArrayList<>();
        regInfoList.add(testReg1);
        regInfoList.add(testReg2);
        String response= gd.GDShareTransfer(keyId,fromAddr,amount,toAddr,shareProperty,eqCode,txInformation,testReg1,testReg2);

        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        response = store.GetTxDetail(txId);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));


        String testReturn = "";
        if(testReturn == "" && bNotCheck) return;

        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");

        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,3500,0,0,mapShareENCN().get("0"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount1,500,1,0,mapShareENCN().get("1"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount5,1000,0,0,mapShareENCN().get("0"),respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2,5000,0,0,mapShareENCN().get("0"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3,5000,0,0,mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4,5000,0,0,mapShareENCN().get("0"), respShareList3);

        log.info(respShareList4.toString());
        //检查存在余额的股东列表
        assertEquals(respShareList4.size(),dataShareList.size());

        List<Map> getShareList = getShareListFromQueryNoZeroAcc(dataShareList);

        assertEquals(respShareList4.size(),getShareList.size());
        assertEquals(true,respShareList4.containsAll(getShareList) && getShareList.containsAll(respShareList4));


        //查询股东持股情况 无当前股权代码信息
        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        assertEquals(gdAccClientNo1,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
//        String get = JSONObject.fromObject(JSONObject.fromObject(query).getJSONObject("data").getJSONArray("accountList").get(0)).getString("shareholderNo");
//        assertEquals("SH"+gdAccClientNo1,get);

        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo1 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount1 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":3500,\"lockAmount\":0}"));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":500,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(gdAccClientNo2,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo2 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount2 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":5000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo3);
        assertEquals(gdAccClientNo3,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo3 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount3 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":5000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo4);
        assertEquals(gdAccClientNo4,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo4 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount4 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":5000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo5);
        assertEquals(gdAccClientNo5,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo5 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount5 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo6);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));
    }

    @Test
    public void TC09_shareIncrease() throws Exception {

        log.info("增发前查询机构主体信息");
        String query2 = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);
//        BigDecimal totalShares = new BigDecimal(JSONObject.fromObject(query2).getJSONObject("data").getString("subject_total_share_capital"));

        String eqCode = gdEquityCode;
        String reason = "股份分红";

        Map eqProd = gdBF.init03EquityProductInfo();

        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,0, shareList3);

        String response= gd.GDShareIncrease(gdPlatfromKeyID,eqCode,shareList4,reason, eqProd);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        response = store.GetTxDetail(txId);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
        String testReturn = "";
        if(testReturn == "" && bNotCheck) return;
        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");


        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,4500,0,0,mapShareENCN().get("0"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount1,500,1,0,mapShareENCN().get("1"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount5,1000,0,0,mapShareENCN().get("0"),respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2,6000,0,0,mapShareENCN().get("0"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3,6000,0,0,mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4,6000,0,0,mapShareENCN().get("0"), respShareList3);



        //检查存在余额的股东列表
        assertEquals(respShareList4.size(),dataShareList.size());

        List<Map> getShareList = getShareListFromQueryNoZeroAcc(dataShareList);

        assertEquals(respShareList4.size(),getShareList.size());
        assertEquals(true,respShareList4.containsAll(getShareList) && getShareList.containsAll(respShareList4));


        //查询股东持股情况 无当前股权代码信息
        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        assertEquals(gdAccClientNo1,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
//        String get = JSONObject.fromObject(JSONObject.fromObject(query).getJSONObject("data").getJSONArray("accountList").get(0)).getString("shareholderNo");
//        assertEquals("SH"+gdAccClientNo1,get);

        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo1 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount1 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":4500,\"lockAmount\":0}"));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":500,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(gdAccClientNo2,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo2 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount2 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":6000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo3);
        assertEquals(gdAccClientNo3,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo3 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount3 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":6000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo4);
        assertEquals(gdAccClientNo4,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo4 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount4 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":6000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo5);
        assertEquals(gdAccClientNo5,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo5 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount5 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo6);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));


        log.info("增发后查询机构主体信息");
        String query3 = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);
//        BigDecimal totalShares2 = new BigDecimal(JSONObject.fromObject(query3).getJSONObject("data").getString("subject_total_share_capital"));
//        BigDecimal totalShares2 = new BigDecimal(JSONObject.fromObject(query3).getJSONObject("data").getJSONObject(
//                "body").getJSONObject("subject_information").getJSONObject("subject_main_body_information").getJSONObject("basic_information_enterprise").getString("subject_total_share_capital"));

//        log.info("判断增发前后机构主体查询总股本数增加数正确");
//        assertEquals(totalShares.add(new BigDecimal("4000")),totalShares2);

    }


    @Test
    public void TC10_shareLock() throws Exception {

        String bizNo = bizNoTest;
        String eqCode = gdEquityCode;
        String address = gdAccount1;
        long lockAmount = 500;
        int shareProperty = 0;
        String reason = "司法冻结";
        String cutoffDate = "2022-09-30";

        Map testReg1 = gdBF.init05RegInfo();
        String regObjId1 = mapAccAddr.get(address) + "lock" + Random(6);
        testReg1.put("register_registration_serial_number","lock" + bizNo);
        testReg1.put("register_account_obj_id",mapAccAddr.get(address));
        testReg1.put("register_registration_object_id",regObjId1);

        String response= gd.GDShareLock(bizNo,address,eqCode,lockAmount,shareProperty,reason,cutoffDate,testReg1);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        response = store.GetTxDetail(txId);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));

        String testReturn = "";
        if(testReturn == "" && bNotCheck) return;
        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");


        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,4500,0,500,mapShareENCN().get("0"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount1,500,1,0,mapShareENCN().get("1"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount5,1000,0,0,mapShareENCN().get("0"),respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2,6000,0,0,mapShareENCN().get("0"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3,6000,0,0,mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4,6000,0,0,mapShareENCN().get("0"), respShareList3);



        //检查存在余额的股东列表
        assertEquals(respShareList4.size(),dataShareList.size());

        List<Map> getShareList = getShareListFromQueryNoZeroAcc(dataShareList);

        assertEquals(respShareList4.size(),getShareList.size());
        assertEquals(true,respShareList4.containsAll(getShareList) && getShareList.containsAll(respShareList4));


        //查询股东持股情况 无当前股权代码信息
        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        assertEquals(gdAccClientNo1,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
//        String get = JSONObject.fromObject(JSONObject.fromObject(query).getJSONObject("data").getJSONArray("accountList").get(0)).getString("shareholderNo");
//        assertEquals("SH"+gdAccClientNo1,get);

        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo1 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount1 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":4500,\"lockAmount\":500}"));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":500,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(gdAccClientNo2,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo2 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount2 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":6000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo3);
        assertEquals(gdAccClientNo3,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo3 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount3 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":6000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo4);
        assertEquals(gdAccClientNo4,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo4 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount4 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":6000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo5);
        assertEquals(gdAccClientNo5,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo5 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount5 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo6);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

    }

    @Test
    public void TC11_shareUnlock() throws Exception {
        sleepAndSaveInfo(3000);
        String address = gdAccount1; //需要和冻结对应
        String bizNo = bizNoTest;
        String eqCode = gdEquityCode;
        long amount = 500;

        Map testReg1 = gdBF.init05RegInfo();
        String regObjId1 = mapAccAddr.get(address) + "unlock" + Random(6);
        testReg1.put("register_registration_serial_number","unlock" + bizNo);
        testReg1.put("register_account_obj_id",mapAccAddr.get(address));
        testReg1.put("register_registration_object_id",regObjId1);

        String response= gd.GDShareUnlock(bizNo,eqCode,amount,testReg1);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        response = store.GetTxDetail(txId);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));

        String testReturn = "";
        if(testReturn == "" && bNotCheck) return;

        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");


        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,4500,0,0,mapShareENCN().get("0"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount1,500,1,0,mapShareENCN().get("1"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount5,1000,0,0,mapShareENCN().get("0"),respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2,6000,0,0,mapShareENCN().get("0"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3,6000,0,0,mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4,6000,0,0,mapShareENCN().get("0"), respShareList3);



        //检查存在余额的股东列表
        assertEquals(respShareList4.size(),dataShareList.size());

        List<Map> getShareList = getShareListFromQueryNoZeroAcc(dataShareList);

        assertEquals(respShareList4.size(),getShareList.size());
        assertEquals(true,respShareList4.containsAll(getShareList) && getShareList.containsAll(respShareList4));


        //查询股东持股情况 无当前股权代码信息
        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        assertEquals(gdAccClientNo1,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
//        String get = JSONObject.fromObject(JSONObject.fromObject(query).getJSONObject("data").getJSONArray("accountList").get(0)).getString("shareholderNo");
//        assertEquals("SH"+gdAccClientNo1,get);

        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo1 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount1 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":4500,\"lockAmount\":0}"));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":500,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(gdAccClientNo2,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo2 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount2 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":6000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo3);
        assertEquals(gdAccClientNo3,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo3 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount3 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":6000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo4);
        assertEquals(gdAccClientNo4,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo4 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount4 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":6000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo5);
        assertEquals(gdAccClientNo5,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo5 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount5 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo6);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

    }

    @Test
    public void TC1201_shareRecycleOneAcc() throws Exception {

        String eqCode = gdEquityCode;
        String remark = "777777";

        log.info("回收前查询机构主体信息");
        String query2 = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);
//        BigDecimal totalShares = new BigDecimal(JSONObject.fromObject(query2).getJSONObject("data").getString("subject_total_share_capital"));

        List<Map> shareList = gdConstructShareList(gdAccount1,100,1);

        String response= gd.GDShareRecycle(gdPlatfromKeyID,eqCode,shareList,remark);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        response = store.GetTxDetail(txId);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));

        String testReturn = "";
        if(testReturn == "" && bNotCheck) return;
        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");


        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,4500,0,0,mapShareENCN().get("0"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount1,400,1,0,mapShareENCN().get("1"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount5,1000,0,0,mapShareENCN().get("0"),respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2,6000,0,0,mapShareENCN().get("0"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3,6000,0,0,mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4,6000,0,0,mapShareENCN().get("0"), respShareList3);



        //检查存在余额的股东列表
        assertEquals(respShareList4.size()+1,dataShareList.size());

        List<Map> getShareList = getShareListFromQueryNoZeroAcc(dataShareList);
        log.info(getShareList.toString());

        assertEquals(respShareList4.size(),getShareList.size());
        assertEquals(true,respShareList4.containsAll(getShareList) && getShareList.containsAll(respShareList4));


        //查询股东持股情况 无当前股权代码信息
        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        assertEquals(gdAccClientNo1,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
//        String get = JSONObject.fromObject(JSONObject.fromObject(query).getJSONObject("data").getJSONArray("accountList").get(0)).getString("shareholderNo");
//        assertEquals("SH"+gdAccClientNo1,get);

        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo1 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount1 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":4500,\"lockAmount\":0}"));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":400,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(gdAccClientNo2,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo2 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount2 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":6000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo3);
        assertEquals(gdAccClientNo3,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo3 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount3 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":6000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo4);
        assertEquals(gdAccClientNo4,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo4 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount4 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":6000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo5);
        assertEquals(gdAccClientNo5,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo5 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount5 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo6);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));


        log.info("回收后查询机构主体信息");
        String query3 = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);
//        BigDecimal totalShares2 = new BigDecimal(JSONObject.fromObject(query3).getJSONObject("data").getString("subject_total_share_capital"));
//        BigDecimal totalShares2 = new BigDecimal(JSONObject.fromObject(query3).getJSONObject("data").getJSONObject(
//                "body").getJSONObject("subject_information").getJSONObject("subject_main_body_information").getJSONObject("basic_information_enterprise").getString("subject_total_share_capital"));

//        log.info("判断增发前后机构主体查询总股本数增加数正确");
//        assertEquals(totalShares.subtract(new BigDecimal("100")),totalShares2);

    }

    @Test
    public void TC1202_shareRecycleMultiAcc() throws Exception {

        String eqCode = gdEquityCode;
        String remark = "777777";

        log.info("多个回收前查询机构主体信息");
        String query2 = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);
//        BigDecimal totalShares = new BigDecimal(JSONObject.fromObject(query2).getJSONObject("data").getString("subject_total_share_capital"));

        registerInfo.put("register_registration_serial_number","recycle000002");

        List<Map> shareList = gdConstructShareList(gdAccount1,100,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,100,0,shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,100,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,100,0, shareList3);

        String response= gd.GDShareRecycle(gdPlatfromKeyID,eqCode,shareList4,remark);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        response = store.GetTxDetail(txId);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));

        String testReturn = "";
        if(testReturn == "" && bNotCheck) return;
        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");


        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,4400,0,0,mapShareENCN().get("0"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount1,400,1,0,mapShareENCN().get("1"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount5,1000,0,0,mapShareENCN().get("0"),respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2,5900,0,0,mapShareENCN().get("0"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3,5900,0,0,mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4,5900,0,0,mapShareENCN().get("0"), respShareList3);



        //检查存在余额的股东列表
        assertEquals(respShareList4.size()+2,dataShareList.size());

        List<Map> getShareList = getShareListFromQueryNoZeroAcc(dataShareList);
        log.info(getShareList.toString());
        assertEquals(respShareList4.size(),getShareList.size());
        assertEquals(true,respShareList4.containsAll(getShareList) && getShareList.containsAll(respShareList4));


        //查询股东持股情况 无当前股权代码信息
        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        assertEquals(gdAccClientNo1,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
//        String get = JSONObject.fromObject(JSONObject.fromObject(query).getJSONObject("data").getJSONArray("accountList").get(0)).getString("shareholderNo");
//        assertEquals("SH"+gdAccClientNo1,get);

        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo1 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount1 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":4400,\"lockAmount\":0}"));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":400,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(gdAccClientNo2,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo2 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount2 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":5900,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo3);
        assertEquals(gdAccClientNo3,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo3 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount3 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":5900,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo4);
        assertEquals(gdAccClientNo4,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo4 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount4 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":5900,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo5);
        assertEquals(gdAccClientNo5,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo5 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount5 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo6);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        log.info("多个回收后查询机构主体信息");
        String query3 = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);
//        BigDecimal totalShares2 = new BigDecimal(JSONObject.fromObject(query3).getJSONObject("data").getString("subject_total_share_capital"));
//        BigDecimal totalShares2 = new BigDecimal(JSONObject.fromObject(query3).getJSONObject("data").getJSONObject(
//                "body").getJSONObject("subject_information").getJSONObject("subject_main_body_information").getJSONObject("basic_information_enterprise").getString("subject_total_share_capital"));
//
//        log.info("判断增发前后机构主体查询总股本数增加数正确");
//        assertEquals(totalShares.subtract(new BigDecimal("400")),totalShares2);
    }

    @Test
    public void TC13_shareChangeBoard() throws Exception {

        String oldEquityCode = gdEquityCode;
        String newEquityCode = gdEquityCode + Random(5);
        String cpnyId = gdCompanyID;

        gd.GDObjectQueryByVer("" + newEquityCode,-1);

        mapAddrRegObjId.clear();

        Map eqProd = gdBF.init03EquityProductInfo();
        eqProd.put("product_object_id","new" + newEquityCode);

        gd.GDObjectQueryByVer(oldEquityCode,-1);
        gd.GDObjectQueryByVer("new" + newEquityCode,-1);

        String flowNo = "changeboard000001";
        List<Map> regList = uf.getAllHolderListReg(gdEquityCode,flowNo);

        String response= gd.GDShareChangeBoard(gdPlatfromKeyID,cpnyId,oldEquityCode,newEquityCode,regList,eqProd,null);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        response = store.GetTxDetail(txId);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));

        gdEquityCode = newEquityCode;

        String testReturn = "";
        if(testReturn == "" && bNotCheck) return;

        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");

        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,4400,0,0,mapShareENCN().get("0"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount1,400,1,0,mapShareENCN().get("1"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount5,1000,0,0,mapShareENCN().get("0"),respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2,5900,0,0,mapShareENCN().get("0"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3,5900,0,0,mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4,5900,0,0,mapShareENCN().get("0"), respShareList3);


        //检查存在余额的股东列表
        assertEquals(respShareList4.size(),dataShareList.size());

        List<Map> getShareList = getShareListFromQueryNoZeroAcc(dataShareList);

        assertEquals(respShareList4.size(),getShareList.size());
        assertEquals(true,respShareList4.containsAll(getShareList) && getShareList.containsAll(respShareList4));


        //查询股东持股情况 无当前股权代码信息
        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        assertEquals(gdAccClientNo1,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
//        String get = JSONObject.fromObject(JSONObject.fromObject(query).getJSONObject("data").getJSONArray("accountList").get(0)).getString("shareholderNo");
//        assertEquals("SH"+gdAccClientNo1,get);

        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo1 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount1 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":4400,\"lockAmount\":0}"));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":400,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(gdAccClientNo2,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo2 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount2 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":5900,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo3);
        assertEquals(gdAccClientNo3,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo3 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount3 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":5900,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo4);
        assertEquals(gdAccClientNo4,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo4 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount4 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":5900,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo5);
        assertEquals(gdAccClientNo5,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo5 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount5 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo6);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));
    }

    @Test
    public void TC205_accountDestroy() throws Exception {
        log.info("销户前查询个人主体信息");
        String query2 = gd.GDMainSubjectQuery(gdContractAddress,gdAccClientNo10);

        String clntNo = gdAccClientNo10;

        String response= gd.GDAccountDestroy(gdContractAddress,clntNo,date3,getListFileObj(),date3,getListFileObj(),
                "name3","num03");

        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        response = store.GetTxDetail(txId);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));

    }


    @Test
    public void TC15_infodisclosurePublishAndGet() throws Exception {
        disclosureInfo = gdBF.init07PublishInfo();
        String response= gd.GDInfoPublish(disclosureInfo);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getString("data");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));
        
        String responseGet = gd.GDInfoPublishGet(txId);
        assertEquals("200",JSONObject.fromObject(responseGet).getString("state"));
        assertEquals(false,responseGet.contains("\"data\":null"));

        end = (new Date()).getTime();

    }

    @Test
    public void TC16_balanceCount() throws Exception {
        settleInfo = gdBF.init06SettleInfo();
        String response= gd.GDCapitalSettlement(settleInfo);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        end = (new Date()).getTime();

    }


    @Test
    public void TC30_txReportQueryTest_ByTime()throws Exception{

        //{"end":"2020-12-21 13:20:25","type":"5","value":"No000jA8Jo","begin":"2020-12-21 13:17:53"}
        //获取最开始的区块高度
        endHeight = Integer.valueOf(JSONObject.fromObject(store.GetHeight()).getString("data"));

//        beginHeigh = 4182;
//        endHeight = 4206;

//        log.info("起始高度 " + beginHeigh + " 结束高度 " + endHeight);
        //排除存证、合约安装、更新主体信息、销户等交易
        ArrayList<String> txList = commonFunc.getTxArrayExceptKeyWord(commonFunc.getTxFromBlock(beginHeigh+1,endHeight),
                "\"type\":0","\"method\":\"DestroyInvestor\"","\"method\":\"UpdateSubject\"","\"subType\":40");//排除存证

//        Date dateNow = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String sdStart = sdf.format(start); // 时间戳转换日期
        String sdEnd = sdf.format(end); // 时间戳转换日期
//        sdStart = "2020-12-21 13:17:53";
//        sdEnd = "2020-12-21 13:20:25";
        String type = "5";
        String value = gdAccClientNo1;

        String response = gd.GDGetTxReportInfo(type,value,sdStart,sdEnd);
        log.info(txList.toString());

        Boolean bContain = true;
        for(int i =0;i< txList.size();i++){
            if(!response.contains(txList.get(i))){
                bContain = false;
                log.info("not contain " + txList.get(i));
            }
        }

        Boolean bFull = true;
        if(!response.contains("\"txType\":\"投资者开户\"")) {log.info("投资者开户交易不存在");bFull = false;}
        if(!response.contains("\"txType\":\"挂牌企业登记\"")) {log.info("挂牌企业登记交易不存在");bFull = false;}
        if(!response.contains("\"txType\":\"股份发行\"")) {log.info("股份发行交易不存在");bFull = false;}
        if(!response.contains("\"txType\":\"过户转让\"")) {log.info("过户转让交易不存在");bFull = false;}
        if(!response.contains("\"txType\":\"股份性质变更\"")) {log.info("股份性质变更交易不存在");bFull = false;}
        if(!response.contains("\"txType\":\"股份冻结\"")) {log.info("股份冻结交易不存在");bFull = false;}
        if(!response.contains("\"txType\":\"股份解冻\"")) {log.info("股份解冻交易不存在");bFull = false;}
        if(!response.contains("\"txType\":\"股份回收\"")) {log.info("股份回收交易不存在");bFull = false;}
        if(!response.contains("\"txType\":\"场内转板\"")) {log.info("场内转板交易不存在");bFull = false;}

        assertEquals("包含所有交易",true,bFull);

        Boolean bNumOk = true;
        String txType = "投资者开户";
        if(StringUtils.countOccurrencesOf(response,txType) != 11) {
            log.info(txType + "交易11 缺失：" + StringUtils.countOccurrencesOf(response,txType));
            bNumOk = bNumOk && false;
        }
        txType = "挂牌企业登记";
        if(StringUtils.countOccurrencesOf(response,txType) != 1) {
            log.info(txType + "交易1 缺失：" + StringUtils.countOccurrencesOf(response,txType));
            bNumOk = bNumOk && false;
        }
        txType = "股份发行";
        if(StringUtils.countOccurrencesOf(response,txType) != 8) {
            log.info(txType + "交易8 缺失：" + StringUtils.countOccurrencesOf(response,txType));
            bNumOk = bNumOk && false;
        }

        txType = "过户转让";
        if(StringUtils.countOccurrencesOf(response,txType) != 1) {
            log.info(txType + "交易1 缺失：" + StringUtils.countOccurrencesOf(response,txType));
            bNumOk = bNumOk && false;
        }

        txType = "股份性质变更";
        if(StringUtils.countOccurrencesOf(response,txType) != 1) {
            log.info(txType + "交易1 缺失：" + StringUtils.countOccurrencesOf(response,txType));
            bNumOk = bNumOk && false;
        }
        txType = "股份冻结";
        if(StringUtils.countOccurrencesOf(response,txType) != 1) {
            log.info(txType + "交易1 缺失：" + StringUtils.countOccurrencesOf(response,txType));
            bNumOk = bNumOk && false;
        }
        txType = "股份解冻";
        if(StringUtils.countOccurrencesOf(response,txType) != 1) {
            log.info(txType + "交易1 缺失：" + StringUtils.countOccurrencesOf(response,txType));
            bNumOk = bNumOk && false;
        }
        txType = "股份回收";
        if(StringUtils.countOccurrencesOf(response,txType) != 5) {
            log.info(txType + "交易5 缺失：" + StringUtils.countOccurrencesOf(response,txType));
            bNumOk = bNumOk && false;
        }
        txType = "场内转板";
        if(StringUtils.countOccurrencesOf(response,txType) != 6) {
            log.info(txType + "交易6 缺失：" + StringUtils.countOccurrencesOf(response,txType));
            bNumOk = bNumOk && false;
        }
        Boolean bAccOK = checkAccDetail(response);

        assertEquals("是否包含所有交易 " + bFull + "存在与链上不一致的交易 " + bContain +
                        " 个数 " + bNumOk + " 账户包含情况" + bAccOK,
                true,bFull && bContain && bNumOk && bAccOK);


    }

    @Test
    public void TC31_txReportQueryTest_ByClientNo()throws Exception{

        log.info("通过客户号查询");
        String response = gd.GDGetTxReportInfo("1",gdAccClientNo1,"","");
        assertEquals("确认投资者开户交易存在",true,response.contains("\"txType\":\"投资者开户\""));
//        assertEquals(2,JSONObject.fromObject(response).getJSONArray("data").size());
    }

    @Test
    public void TC32_txReportQueryTest_ByClientName()throws Exception{
        log.info("通过客户姓名查询");
        String clientName = "zhangsan";
        String response = gd.GDGetTxReportInfo("2",clientName,"","");
        assertEquals("确认投资者开户交易存在",true,response.contains("\"txType\":\"投资者开户\""));
//        assertEquals(12, StringUtils.countOccurrencesOf(response,"投资者开户"));
//        assertEquals(12,JSONObject.fromObject(response).getJSONArray("data").size());

    }

    @Test
    public void TC33_txReportQueryTest_BySHNo()throws Exception{
        log.info("通过股东号查询");
        String response = gd.GDGetTxReportInfo("3","SH" + gdAccClientNo1,"","");
//        assertEquals("确认投资者开户交易存在",true,response.contains("\"txType\":\"投资者开户\""));
//        assertEquals(12, StringUtils.countOccurrencesOf(response,"投资者开户"));
//        assertEquals(12,JSONObject.fromObject(response).getJSONArray("data").size());
    }

    @Test
    public void TC34_txReportQueryTest_ByEquityCode()throws Exception{
        log.info("通过股权代码查询");
        String response = gd.GDGetTxReportInfo("4",gdEquityCode,"","");
//        assertEquals("确认投资者开户交易存在",true,response.contains("\"txType\":\"投资者开户\""));
//        assertEquals(12, StringUtils.countOccurrencesOf(response,"投资者开户"));
//        assertEquals(12,JSONObject.fromObject(response).getJSONArray("data").size());
    }


    public Boolean checkAccDetail(String response){
        Boolean bResult = true;
        Boolean bAll = true;
        Boolean bOk = true;
        bOk = response.contains("\"clientNo\": \"" + gdAccClientNo1 +
                "\",\"shareholderNo\": \"SH" + gdAccClientNo1 + "\",\"txType\": \"投资者开户\"");  bResult = bResult || bOk;
        bOk = response.contains("\"clientNo\": \"" + gdAccClientNo2 +
                "\",\"shareholderNo\": \"SH" + gdAccClientNo2 + "\",\"txType\": \"投资者开户\"");bResult = bResult || bOk;
        bOk = response.contains("\"clientNo\": \"" + gdAccClientNo3 +
                "\",\"shareholderNo\": \"SH" + gdAccClientNo3 + "\",\"txType\": \"投资者开户\"");bResult = bResult || bOk;
        bOk = response.contains("\"clientNo\": \"" + gdAccClientNo4 +
                "\",\"shareholderNo\": \"SH" + gdAccClientNo4 + "\",\"txType\": \"投资者开户\"");bResult = bResult || bOk;
        bOk = response.contains("\"clientNo\": \"" + gdAccClientNo5 +
                "\",\"shareholderNo\": \"SH" + gdAccClientNo5 + "\",\"txType\": \"投资者开户\"");bResult = bResult || bOk;
        bOk = response.contains("\"clientNo\": \"" + gdAccClientNo6 +
                "\",\"shareholderNo\": \"SH" + gdAccClientNo6 + "\",\"txType\": \"投资者开户\"");bResult = bResult || bOk;
        bOk = response.contains("\"clientNo\": \"" + gdAccClientNo7 +
                "\",\"shareholderNo\": \"SH" + gdAccClientNo7 + "\",\"txType\": \"投资者开户\"");bResult = bResult || bOk;
        bOk = response.contains("\"clientNo\": \"" + gdAccClientNo8 +
                "\",\"shareholderNo\": \"SH" + gdAccClientNo8 + "\",\"txType\": \"投资者开户\"");bResult = bResult || bOk;
        bOk = response.contains("\"clientNo\": \"" + gdAccClientNo9 +
                "\",\"shareholderNo\": \"SH" + gdAccClientNo9 + "\",\"txType\": \"投资者开户\"");bResult = bResult || bOk;
        bOk = response.contains("\"clientNo\": \"" + gdAccClientNo10 +
                "\",\"shareholderNo\": \"SH" + gdAccClientNo10 + "\",\"txType\": \"投资者开户\"");bResult = bResult || bOk;
        if(!bResult) log.info("存在投资者开户数据不一致的情况");

        bAll = bAll || bResult;bResult = true;

        bOk = response.contains("\"equityCode\": \"" + oldEquity + "\",\"txType\": \"挂牌企业登记\"");bResult = bResult || bOk;
        if(!bResult) log.info("存在挂牌企业登记数据不一致的情况");

        bAll = bAll || bResult;bResult = true;

        bOk = response.contains("\"clientNo\": \"" + gdAccClientNo1 +
                "\",\"equityCode\": \"" + oldEquity + "\",\"txType\": \"股份发行\",\"close_amount\": 5000");bResult = bResult || bOk;
        bOk = response.contains("\"clientNo\": \"" + gdAccClientNo2 +
                "\",\"equityCode\": \"" + oldEquity + "\",\"txType\": \"股份发行\",\"close_amount\": 5000");bResult = bResult || bOk;
        bOk = response.contains("\"clientNo\": \"" + gdAccClientNo3 +
                "\",\"equityCode\": \"" + oldEquity + "\",\"txType\": \"股份发行\",\"close_amount\": 5000");bResult = bResult || bOk;
        bOk = response.contains("\"clientNo\": \"" + gdAccClientNo4 +
                "\",\"equityCode\": \"" + oldEquity + "\",\"txType\": \"股份发行\",\"close_amount\": 5000");bResult = bResult || bOk;
        if(!bResult) log.info("存在股份发行数据不一致的情况");

        bAll = bAll || bResult;bResult = true;

        bOk = response.contains("\"clientNo\": \"" + gdAccClientNo1 +
                "\",\"equityCode\": \"" + oldEquity + "\",\"shareProperty\": \"0\",\"txType\": \"股份性质变更\",\"close_amount\": 500");bResult = bResult || bOk;
        if(!bResult) log.info("存在股份性质变更数据不一致的情况");

        bAll = bAll || bResult;bResult = true;
        bOk = response.contains("\"clientNo\": \"" + gdAccClientNo1 + "\",\"equityCode\": \""
                + oldEquity + "\",\"close_price\": \"1000\",\"txType\": \"过户转让\",\"close_amount\": 1000");bResult = bResult || bOk;
        if(!bResult) log.info("存在过户转让数据不一致的情况");

        bAll = bAll || bResult;bResult = true;
        bOk = response.contains("\"clientNo\": \"" + gdAccClientNo1 +
                "\",\"equityCode\": \"" + oldEquity + "\",\"txType\": \"股份发行\",\"close_amount\": 1000");bResult = bResult || bOk;
        bOk = response.contains("\"clientNo\": \"" + gdAccClientNo2 +
                "\",\"equityCode\": \"" + oldEquity + "\",\"txType\": \"股份发行\",\"close_amount\": 1000");bResult = bResult || bOk;
        bOk = response.contains("\"clientNo\": \"" + gdAccClientNo3 +
                "\",\"equityCode\": \"" + oldEquity + "\",\"txType\": \"股份发行\",\"close_amount\": 1000");bResult = bResult || bOk;
        bOk = response.contains("\"clientNo\": \"" + gdAccClientNo4 +
                "\",\"equityCode\": \"" + oldEquity + "\",\"txType\": \"股份发行\",\"close_amount\": 1000");bResult = bResult || bOk;
        if(!bResult) log.info("存在增发数据不一致的情况");

        bAll = bAll || bResult;bResult = true;
        bOk = response.contains("\"clientNo\": \"" + gdAccClientNo1 + "\",\"equityCode\": \"" +
                oldEquity + "\",\"shareProperty\": \"0\",\"txType\": \"股份冻结\",\"close_amount\": 500,\"remark\": \"司法冻结\"");bResult = bResult || bOk;
        if(!bResult) log.info("存在司法冻结数据不一致的情况");

        bAll = bAll || bResult;bResult = true;
        bOk = response.contains("\"clientNo\": \"" + gdAccClientNo1 + "\",\"equityCode\": \""
                + oldEquity + "\",\"txType\": \"股份解冻\",\"close_amount\": 500,\"remark\": \"" + bizNoTest + "\"");bResult = bResult || bOk;
        if(!bResult) log.info("存在股份解冻数据不一致的情况");

        bAll = bAll || bResult;bResult = true;
        bOk = response.contains("\"clientNo\": \"" + gdAccClientNo1 + "\",\"equityCode\": \"" +
                oldEquity + "\",\"txType\": \"股份回收\",\"close_amount\": 100,");bResult = bResult || bOk;
        bOk = response.contains("\"clientNo\": \"" + gdAccClientNo1 + "\",\"equityCode\": \"" +
                oldEquity + "\",\"txType\": \"股份回收\",\"close_amount\": 100,");bResult = bResult || bOk;
        bOk = response.contains("\"clientNo\": \"" + gdAccClientNo2 + "\",\"equityCode\": \"" +
                oldEquity + "\",\"txType\": \"股份回收\",\"close_amount\": 100,");bResult = bResult || bOk;
        bOk = response.contains("\"clientNo\": \"" + gdAccClientNo3 + "\",\"equityCode\": \"" +
                oldEquity + "\",\"txType\": \"股份回收\",\"close_amount\": 100,");bResult = bResult || bOk;
        bOk = response.contains("\"clientNo\": \"" + gdAccClientNo4 + "\",\"equityCode\": \"" +
                oldEquity + "\",\"txType\": \"股份回收\",\"close_amount\": 100,");bResult = bResult || bOk;
        if(!bResult) log.info("存在股份回收数据不一致的情况");

        bAll = bAll || bResult;bResult = true;
        bOk = response.contains("\"clientNo\": \"" + gdAccClientNo1 + "\",\"equityCode\": \"" +
                newEquity + "\",\"txType\": \"场内转板\",\"close_amount\": 4400,");bResult = bResult || bOk;
        bOk = response.contains("\"clientNo\": \"" + gdAccClientNo1 + "\",\"equityCode\": \"" +
                newEquity + "\",\"txType\": \"场内转板\",\"close_amount\": 400,");bResult = bResult || bOk;
        bOk = response.contains("\"clientNo\": \"" + gdAccClientNo2 + "\",\"equityCode\": \"" +
                newEquity + "\",\"txType\": \"场内转板\",\"close_amount\": 5900,");bResult = bResult || bOk;
        bOk = response.contains("\"clientNo\": \"" + gdAccClientNo3 + "\",\"equityCode\": \"" +
                newEquity + "\",\"txType\": \"场内转板\",\"close_amount\": 5900,");bResult = bResult || bOk;
        bOk = response.contains("\"clientNo\": \"" + gdAccClientNo4 + "\",\"equityCode\": \"" +
                newEquity + "\",\"txType\": \"场内转板\",\"close_amount\": 5900,");bResult = bResult || bOk;
        bOk = response.contains("\"clientNo\": \"" + gdAccClientNo5 + "\",\"equityCode\": \"" +
                newEquity + "\",\"txType\": \"场内转板\",\"close_amount\": 1000,");bResult = bResult || bOk;
        if(!bResult) log.info("存在场内转板数据不一致的情况");
        bAll = bAll || bResult;
        log.info("账户比对结果" + bAll);
        return bAll;
    }
}
