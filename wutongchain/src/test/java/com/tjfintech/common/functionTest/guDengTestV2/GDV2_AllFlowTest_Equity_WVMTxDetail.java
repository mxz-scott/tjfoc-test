package com.tjfintech.common.functionTest.guDengTestV2;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.GuDeng;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.*;
import org.junit.rules.TestName;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.tjfintech.common.functionTest.guDengTestV2.GDCommonFunc.*;
import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassGD.*;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class GDV2_AllFlowTest_Equity_WVMTxDetail {

    TestBuilder testBuilder= TestBuilder.getInstance();
    GuDeng gd =testBuilder.getGuDeng();
    Store store =testBuilder.getStore();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    GDBeforeCondition gdBF = new GDBeforeCondition();
    GDCommonFunc gdCF = new GDCommonFunc();
    GDUnitFunc uf = new GDUnitFunc();
    public static String bizNoTest = "test" + Random(12);
    Boolean bNotCheck = false;

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
        gdEquityCode = "fondTest" + Random(12);
        sleepAndSaveInfo(3000);//确认前置步骤可能执行到的交易和后续交易不打在同一个区块中
    }

    @After
    public void calJGDataAfterTx()throws Exception{
        testCurMethodName = tm.getMethodName();
        GDUnitFunc uf = new GDUnitFunc();
        int endHeight = net.sf.json.JSONObject.fromObject(store.GetHeight()).getInt("data");
        uf.checkJGHeaderOpVer(blockHeight,endHeight);
    }

    @Test
    public void TC01_enterpriseRegister() throws Exception {
        long shareTotals = 1000000;
        Map eqProd = gdBF.init03EquityProductInfo();
        Map enSub = gdBF.init01EnterpriseSubjectInfo();
        String response = gd.GDEnterpriseResister(gdContractAddress, gdEquityCode, shareTotals, enSub, eqProd, bondProductInfo, fundProductInfo);
        JSONObject jsonObject = JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        response = store.GetTxDetail(txId);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));

        //查交易详情
        response = store.GetTxDetail(
                JSONObject.fromObject(store.GetBlockByHeight(
                        JSONObject.fromObject(store.GetHeight()).getInt("data") - 1)
                ).getJSONObject("data").getJSONArray("txs").getString(0));

        //检查交易详情
        JSONObject jsonObject1 = JSONObject.fromObject(response).getJSONObject("data");
        assertEquals("2", jsonObject1.getJSONObject("header").getString("version"));
        assertEquals("3", jsonObject1.getJSONObject("header").getString("type"));
        assertEquals("42", jsonObject1.getJSONObject("header").getString("subType"));

        JSONObject jsonObjectWVM = jsonObject1.getJSONObject("wvm").getJSONObject("wvmContractTx");
        assertEquals("AddListedCompany", jsonObjectWVM.getJSONObject("arg").getString("method"));
        assertEquals(gdContractAddress, jsonObjectWVM.getString("name"));

        String args = jsonObjectWVM.getJSONObject("arg").getJSONArray("args").get(0).toString();

        log.info(args);
        assertEquals(true, args.contains("\"EquityCode\":\"" + gdEquityCode + "\""));
        assertEquals(true, args.contains("\"TotalShares\":" + shareTotals + ""));
        assertEquals(true, response.contains("DBSet"));
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

        commonFunc.sdkCheckTxOrSleep(mapAcc.get("txId"),utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        String response = store.GetTxDetail(mapAcc.get("txId"));
        assertEquals("200",JSONObject.fromObject(response).getString("state"));


        //查交易详情
        response = store.GetTxDetail(
                JSONObject.fromObject(store.GetBlockByHeight(
                        JSONObject.fromObject(store.GetHeight()).getInt("data") -1)
                ).getJSONObject("data").getJSONArray("txs").getString(0));

        //检查交易详情
        JSONObject jsonObject1 = JSONObject.fromObject(response).getJSONObject("data");
        assertEquals("2",jsonObject1.getJSONObject("header").getString("version"));
        assertEquals("3",jsonObject1.getJSONObject("header").getString("type"));
        assertEquals("42",jsonObject1.getJSONObject("header").getString("subType"));

        JSONObject jsonObjectWVM = jsonObject1.getJSONObject("wvm").getJSONObject("wvmContractTx");
        assertEquals("AddInvestor",jsonObjectWVM.getJSONObject("arg").getString("method"));
        assertEquals(gdContractAddress,jsonObjectWVM.getString("name"));
        String args = jsonObjectWVM.getJSONObject("arg").getJSONArray("args").get(0).toString();
        assertEquals(true,args.contains("\"ClientNo\":\"" + cltNo + "\""));
        assertEquals(true,response.contains("DBSet"));

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
        response = store.GetTxDetail(txId);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));

        //查交易详情
        response = store.GetTxDetail(
                JSONObject.fromObject(store.GetBlockByHeight(
                        JSONObject.fromObject(store.GetHeight()).getInt("data") -1)
                ).getJSONObject("data").getJSONArray("txs").getString(0));

        //检查交易详情
        JSONObject jsonObject1 = JSONObject.fromObject(response).getJSONObject("data");
        assertEquals("2",jsonObject1.getJSONObject("header").getString("version"));
        assertEquals("3",jsonObject1.getJSONObject("header").getString("type"));
        assertEquals("43",jsonObject1.getJSONObject("header").getString("subType"));

        JSONObject jsonObjectWVM = jsonObject1.getJSONObject("wvm").getJSONObject("wvmContractTx");
        assertEquals("IssueToken",jsonObjectWVM.getJSONObject("arg").getString("method"));
        assertEquals("Sys_SmartTokenContract",jsonObjectWVM.getString("name"));
        String args = jsonObjectWVM.getJSONObject("arg").toString();
        assertEquals(true,args.contains("\"Type\":\"" + gdEquityCode + "\""));
        assertEquals(true,args.contains("\"Status\":\"Normal\""));
        assertEquals(true,args.contains("\"Amount\":20000000000"));
        assertEquals(true,args.contains(gdContractAddress));

        assertEquals(true,response.contains("{\"from\":\""+ gdContractAddress + "\",\"to\":\"" +
                gdAccount1 + "\",\"tokenType\":\"" + gdEquityCode + "\",\"amount\":\"5000\",\"subType\":\"0\"}"));
        assertEquals(true,response.contains("{\"from\":\""+ gdContractAddress + "\",\"to\":\"" +
                gdAccount2 + "\",\"tokenType\":\"" + gdEquityCode + "\",\"amount\":\"5000\",\"subType\":\"0\"}"));
        assertEquals(true,response.contains("{\"from\":\""+ gdContractAddress + "\",\"to\":\"" +
                gdAccount3 + "\",\"tokenType\":\"" + gdEquityCode + "\",\"amount\":\"5000\",\"subType\":\"0\"}"));
        assertEquals(true,response.contains("{\"from\":\""+ gdContractAddress + "\",\"to\":\"" +
                gdAccount4 + "\",\"tokenType\":\"" + gdEquityCode + "\",\"amount\":\"5000\",\"subType\":\"0\"}"));
        assertEquals(true,response.contains("DBSet"));
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
        response = store.GetTxDetail(txId);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));

        //查交易详情
        response = store.GetTxDetail(
                JSONObject.fromObject(store.GetBlockByHeight(
                        JSONObject.fromObject(store.GetHeight()).getInt("data") -1)
                ).getJSONObject("data").getJSONArray("txs").getString(0));

        //检查交易详情
        JSONObject jsonObject1 = JSONObject.fromObject(response).getJSONObject("data");
        assertEquals("1",jsonObject1.getJSONObject("header").getString("version"));
        assertEquals("1",jsonObject1.getJSONObject("header").getString("type"));
        assertEquals("11",jsonObject1.getJSONObject("header").getString("subType"));


        assertEquals(true,response.contains("{\"from\":\"" + address + "\",\"to\":\"" + address
                + "\",\"tokenType\":\"" + gdEquityCode + "\",\"amount\":\"500\",\"subType\":\"0\",\"newSubType\":\"1\"}"));
        assertEquals(true,response.contains("{\"from\":\"" + address + "\",\"to\":\"" + address
                + "\",\"tokenType\":\"" + gdEquityCode + "\",\"amount\":\"4500\",\"subType\":\"0\"}"));
//        assertEquals(false,response.contains("register_investor_subject_ref")); //确认不包含登记数据
    }

    @Test
    public void TC08_shareTransfer()throws Exception{
        String keyId = gdAccountKeyID1;
        String fromAddr = gdAccount1;
        String toAddr = gdAccount5;
        int shareProperty = 0;
        String eqCode = gdEquityCode;

        register_event_type = 2;//交易登记
        //交易报告数据
        Map txInfo = gdBF.init04TxInfo();
        String txRpObjId = "txReport" + Random(6);
        txInfo.put("transaction_object_id",txRpObjId);
//        txInfo.put("transaction_custody_product_ref",gdEquityCode);

        //登记数据
        String tempObjIdFrom = "reg" + mapAccAddr.get(gdAccount1).toString() + Random(3);
        String tempObjIdTo = "reg" + mapAccAddr.get(gdAccount5).toString() + Random(3);

        register_transaction_ref = txRpObjId;//登记引用的是交易报告的对象标识
        transaction_custody_product_ref = gdEquityCode;
        register_product_ref = gdEquityCode;

        Map fromNow = gdBF.init05RegInfo();
        Map toNow = gdBF.init05RegInfo();

        fromNow.put("register_registration_object_id",tempObjIdFrom);
        fromNow.put("register_subject_account_ref","SH" + gdAccClientNo1);

        toNow.put("register_registration_object_id",tempObjIdTo);
        toNow.put("register_subject_account_ref","SH" + gdAccClientNo5);

//        fromNow.put("register_transaction_ref",txRpObjId);
//        toNow.put("register_transaction_ref",txRpObjId);


        String tempObj = gdCompanyID;

        String query2 = gd.GDObjectQueryByVer(gdCompanyID,-1);

        //执行交易
        String response= gd.GDShareTransfer(keyId,fromAddr,1000,toAddr,shareProperty,eqCode,txInfo,fromNow,toNow);

        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        response = store.GetTxDetail(txId);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));

        //查交易详情
        response = store.GetTxDetail(
                JSONObject.fromObject(store.GetBlockByHeight(
                        JSONObject.fromObject(store.GetHeight()).getInt("data") -1)
                ).getJSONObject("data").getJSONArray("txs").getString(0));

        //检查交易详情
        JSONObject jsonObject1 = JSONObject.fromObject(response).getJSONObject("data");
        assertEquals("1",jsonObject1.getJSONObject("header").getString("version"));
        assertEquals("1",jsonObject1.getJSONObject("header").getString("type"));
        assertEquals("11",jsonObject1.getJSONObject("header").getString("subType"));


        assertEquals(true,response.contains("{\"from\":\"" + fromAddr + "\",\"to\":\"" + toAddr
                + "\",\"tokenType\":\"" + gdEquityCode + "\",\"amount\":\"1000\",\"subType\":\"0\"}"));
        assertEquals(true,response.contains("{\"from\":\"" + fromAddr + "\",\"to\":\"" + fromAddr
                + "\",\"tokenType\":\"" + gdEquityCode + "\",\"amount\":\"3500\",\"subType\":\"0\"}"));
//        assertEquals(true,response.contains("transaction_intermediary_information"));
    }

    @Test
    public void TC09_shareIncrease() throws Exception {

        log.info("增发前查询机构主体信息");
        String query2 = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);
//        BigDecimal totalShares = new BigDecimal(JSONObject.fromObject(query2).getJSONObject("data").getString("subject_total_share_capital"));

        String eqCode = gdEquityCode;
        String reason = "股份分红";

        Map eqProd = gdBF.init03EquityProductInfo();

        List<Map> shareList = gdConstructShareList(gdAccount1,1000,1);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,1, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,1, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,1, shareList3);

        String response= gd.GDShareIncrease(gdPlatfromKeyID,eqCode,shareList4,reason, eqProd);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        response = store.GetTxDetail(txId);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));

        //查交易详情
        response = store.GetTxDetail(
                JSONObject.fromObject(store.GetBlockByHeight(
                        JSONObject.fromObject(store.GetHeight()).getInt("data") -1)
                ).getJSONObject("data").getJSONArray("txs").getString(0));

        //检查交易详情
        JSONObject jsonObject1 = JSONObject.fromObject(response).getJSONObject("data");
        assertEquals("2",jsonObject1.getJSONObject("header").getString("version"));
        assertEquals("3",jsonObject1.getJSONObject("header").getString("type"));
        assertEquals("43",jsonObject1.getJSONObject("header").getString("subType"));

        JSONObject jsonObjectWVM = jsonObject1.getJSONObject("wvm").getJSONObject("wvmContractTx");
        assertEquals("IssueToken",jsonObjectWVM.getJSONObject("arg").getString("method"));
        assertEquals("Sys_SmartTokenContract",jsonObjectWVM.getString("name"));
        String args = jsonObjectWVM.getJSONObject("arg").toString();
        log.info(args);
        assertEquals(true,args.contains("\"Type\":\"" + gdEquityCode + "\""));
        assertEquals(true,args.contains("\"Status\":\"Normal\""));
        assertEquals(true,args.contains("\"Amount\":4000000000"));
        assertEquals(true,args.contains(gdContractAddress));

        assertEquals(true,response.contains("{\"from\":\""+ gdContractAddress + "\",\"to\":\"" +
                gdAccount1 + "\",\"tokenType\":\"" + gdEquityCode + "\",\"amount\":\"1000\",\"subType\":\"1\"}"));
        assertEquals(true,response.contains("{\"from\":\""+ gdContractAddress + "\",\"to\":\"" +
                gdAccount2 + "\",\"tokenType\":\"" + gdEquityCode + "\",\"amount\":\"1000\",\"subType\":\"1\"}"));
        assertEquals(true,response.contains("{\"from\":\""+ gdContractAddress + "\",\"to\":\"" +
                gdAccount3 + "\",\"tokenType\":\"" + gdEquityCode + "\",\"amount\":\"1000\",\"subType\":\"1\"}"));
        assertEquals(true,response.contains("{\"from\":\""+ gdContractAddress + "\",\"to\":\"" +
                gdAccount4 + "\",\"tokenType\":\"" + gdEquityCode + "\",\"amount\":\"1000\",\"subType\":\"1\"}"));
        assertEquals(true,response.contains("DBSet"));

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

        //查交易详情
        response = store.GetTxDetail(
                JSONObject.fromObject(store.GetBlockByHeight(
                        JSONObject.fromObject(store.GetHeight()).getInt("data") -1)
                ).getJSONObject("data").getJSONArray("txs").getString(0));

        //检查交易详情
        JSONObject jsonObject1 = JSONObject.fromObject(response).getJSONObject("data");
        assertEquals("2",jsonObject1.getJSONObject("header").getString("version"));
        assertEquals("3",jsonObject1.getJSONObject("header").getString("type"));
        assertEquals("42",jsonObject1.getJSONObject("header").getString("subType"));

        JSONObject jsonObjectWVM = jsonObject1.getJSONObject("wvm").getJSONObject("wvmContractTx");
        assertEquals("ShareLock",jsonObjectWVM.getJSONObject("arg").getString("method"));
        assertEquals(gdContractAddress,jsonObjectWVM.getString("name"));
        String args = jsonObjectWVM.getJSONObject("arg").getJSONArray("args").get(0).toString();
        assertEquals(true,args.contains("\"BizNo\":\"" + bizNo + "\""));
        assertEquals(true,args.contains("\"Address\":\"" + address + "\""));
        assertEquals(true,args.contains("\"EquityCode\":\"" + gdEquityCode + "\""));
        assertEquals(true,args.contains("\"Amount\":" + lockAmount + "000000"));
        assertEquals(true,args.contains("\"ShareProperty\":\"" + shareProperty + "\""));
        assertEquals(true,args.contains("\"Reason\":\"" + reason + "\""));

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        log.info("change " + simpleDateFormat.parse(cutoffDate).getTime());
        assertEquals(true,args.contains("\"CutoffDate\":" + simpleDateFormat.parse(cutoffDate).getTime()));
        assertEquals(true,response.contains("DBSet"));

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

        //查交易详情
        response = store.GetTxDetail(
                JSONObject.fromObject(store.GetBlockByHeight(
                        JSONObject.fromObject(store.GetHeight()).getInt("data") -1)
                ).getJSONObject("data").getJSONArray("txs").getString(0));

        //检查交易详情
        JSONObject jsonObject1 = JSONObject.fromObject(response).getJSONObject("data");
        assertEquals("2",jsonObject1.getJSONObject("header").getString("version"));
        assertEquals("3",jsonObject1.getJSONObject("header").getString("type"));
        assertEquals("42",jsonObject1.getJSONObject("header").getString("subType"));

        JSONObject jsonObjectWVM = jsonObject1.getJSONObject("wvm").getJSONObject("wvmContractTx");
        assertEquals("ShareUnlock",jsonObjectWVM.getJSONObject("arg").getString("method"));
        assertEquals(gdContractAddress,jsonObjectWVM.getString("name"));
        String args = jsonObjectWVM.getJSONObject("arg").toString();
        log.info(args);
        assertEquals(true,args.contains(bizNo));
        assertEquals(true,response.contains( gdEquityCode));
        assertEquals(true,args.contains(amount + "000000"));
        assertEquals(true,response.contains("DBSet"));
    }

    @Test
    public void TC1201_shareRecycleOneAcc() throws Exception {

        String eqCode = gdEquityCode;
        String remark = "777777";

        log.info("回收前查询机构主体信息");
        String query2 = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);
//        BigDecimal totalShares = new BigDecimal(JSONObject.fromObject(query2).getJSONObject("data").getString("subject_total_share_capital"));

        List<Map> shareList = gdConstructShareList(gdAccount1,100,0);

        String response= gd.GDShareRecycle(gdPlatfromKeyID,eqCode,shareList,remark);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        response = store.GetTxDetail(txId);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));

        //查交易详情
        response = store.GetTxDetail(
                JSONObject.fromObject(store.GetBlockByHeight(
                        JSONObject.fromObject(store.GetHeight()).getInt("data") -1)
                ).getJSONObject("data").getJSONArray("txs").getString(0));

        //检查交易详情
        JSONObject jsonObject1 = JSONObject.fromObject(response).getJSONObject("data");
        assertEquals("1",jsonObject1.getJSONObject("header").getString("version"));
        assertEquals("1",jsonObject1.getJSONObject("header").getString("type"));
        assertEquals("12",jsonObject1.getJSONObject("header").getString("subType"));


        assertEquals(true,response.contains("{\"from\":\"" + gdAccount1 + "\",\"to\":\"" + zeroAccount
                + "\",\"tokenType\":\"" + gdEquityCode + "\",\"amount\":\"100\",\"subType\":\"0\"}"));
        assertEquals(true,response.contains("{\"from\":\"" + gdAccount1 + "\",\"to\":\"" + gdAccount1
                + "\",\"tokenType\":\"" + gdEquityCode + "\",\"amount\":\"3400\",\"subType\":\"0\"}"));
//        assertEquals(true,response.contains("register_account_obj_id"));
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

        //查交易详情
        response = store.GetTxDetail(
                JSONObject.fromObject(store.GetBlockByHeight(
                        JSONObject.fromObject(store.GetHeight()).getInt("data") -1)
                ).getJSONObject("data").getJSONArray("txs").getString(0));

        //检查交易详情
        JSONObject jsonObject1 = JSONObject.fromObject(response).getJSONObject("data");
        assertEquals("1",jsonObject1.getJSONObject("header").getString("version"));
        assertEquals("1",jsonObject1.getJSONObject("header").getString("type"));
        assertEquals("12",jsonObject1.getJSONObject("header").getString("subType"));


        assertEquals(true,response.contains("{\"from\":\"" + gdAccount1 + "\",\"to\":\"" + zeroAccount
                + "\",\"tokenType\":\"" + gdEquityCode + "\",\"amount\":\"100\",\"subType\":\"0\"}"));
        assertEquals(true,response.contains("{\"from\":\"" + gdAccount1 + "\",\"to\":\"" + gdAccount1
                + "\",\"tokenType\":\"" + gdEquityCode + "\",\"amount\":\"3300\",\"subType\":\"0\"}"));

        assertEquals(true,response.contains("{\"from\":\"" + gdAccount2 + "\",\"to\":\"" + zeroAccount
                + "\",\"tokenType\":\"" + gdEquityCode + "\",\"amount\":\"100\",\"subType\":\"0\"}"));
        assertEquals(true,response.contains("{\"from\":\"" + gdAccount2 + "\",\"to\":\"" + gdAccount2
                + "\",\"tokenType\":\"" + gdEquityCode + "\",\"amount\":\"4900\",\"subType\":\"0\"}"));

        assertEquals(true,response.contains("{\"from\":\"" + gdAccount3 + "\",\"to\":\"" + zeroAccount
                + "\",\"tokenType\":\"" + gdEquityCode + "\",\"amount\":\"100\",\"subType\":\"0\"}"));
        assertEquals(true,response.contains("{\"from\":\"" + gdAccount3 + "\",\"to\":\"" + gdAccount3
                + "\",\"tokenType\":\"" + gdEquityCode + "\",\"amount\":\"4900\",\"subType\":\"0\"}"));

        assertEquals(true,response.contains("{\"from\":\"" + gdAccount4 + "\",\"to\":\"" + zeroAccount
                + "\",\"tokenType\":\"" + gdEquityCode + "\",\"amount\":\"100\",\"subType\":\"0\"}"));
        assertEquals(true,response.contains("{\"from\":\"" + gdAccount4 + "\",\"to\":\"" + gdAccount4
                + "\",\"tokenType\":\"" + gdEquityCode + "\",\"amount\":\"4900\",\"subType\":\"0\"}"));
//        assertEquals(true,response.contains("register_account_obj_id"));
    }

    @Test
    public void TC13_shareChangeBoard() throws Exception {

        String oldEquityCode = gdEquityCode;
        String newEquityCode = "newCode" + Random(5);
        String cpnyId = gdCompanyID;

        gdEquityCode = newEquityCode;
        List<Map> regList = uf.getAllHolderListReg(oldEquityCode,regNo);


        product_issuer_subject_ref = gdCompanyID;
        Map oldEqProd = gdBF.init03EquityProductInfo();
        oldEqProd.put("product_object_id",oldEquityCode);

        gdEquityCode = newEquityCode;
        product_issuer_subject_ref = gdCompanyID;
        Map newEqProd = gdBF.init03EquityProductInfo();
        newEqProd.put("product_object_id",newEquityCode);

        String response= gd.GDShareChangeBoard(gdPlatfromKeyID,cpnyId,oldEquityCode,newEquityCode,regList,oldEqProd,newEqProd);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        response = store.GetTxDetail(txId);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));

        //查交易详情
        response = store.GetTxDetail(
                JSONObject.fromObject(store.GetBlockByHeight(
                        JSONObject.fromObject(store.GetHeight()).getInt("data") -1)
                ).getJSONObject("data").getJSONArray("txs").getString(0));

        //检查交易详情
        JSONObject jsonObject1 = JSONObject.fromObject(response).getJSONObject("data");
        assertEquals("1",jsonObject1.getJSONObject("header").getString("version"));
        assertEquals("1",jsonObject1.getJSONObject("header").getString("type"));
        assertEquals("13",jsonObject1.getJSONObject("header").getString("subType"));

        assertEquals(true,response.contains("{\"from\":\"" + gdAccount1 + "\",\"to\":\"" + gdAccount1 +
                "\",\"tokenType\":\"" + oldEquityCode + "\",\"newTokenType\":\"" + newEquityCode + "\",\"amount\":\"3300\",\"subType\":\"0\"}"));
        assertEquals(true,response.contains("{\"from\":\"" + gdAccount1 + "\",\"to\":\"" + gdAccount1 +
                "\",\"tokenType\":\"" + oldEquityCode + "\",\"newTokenType\":\"" + newEquityCode + "\",\"amount\":\"1500\",\"subType\":\"1\"}"));

        assertEquals(true,response.contains("{\"from\":\"" + gdAccount5 + "\",\"to\":\"" + gdAccount5 +
                "\",\"tokenType\":\"" + oldEquityCode + "\",\"newTokenType\":\"" + newEquityCode + "\",\"amount\":\"1000\",\"subType\":\"0\"}"));

        assertEquals(true,response.contains("{\"from\":\"" + gdAccount2 + "\",\"to\":\"" + gdAccount2 +
                "\",\"tokenType\":\"" + oldEquityCode + "\",\"newTokenType\":\"" + newEquityCode + "\",\"amount\":\"4900\",\"subType\":\"0\"}"));
        assertEquals(true,response.contains("{\"from\":\"" + gdAccount2 + "\",\"to\":\"" + gdAccount2 +
                "\",\"tokenType\":\"" + oldEquityCode + "\",\"newTokenType\":\"" + newEquityCode + "\",\"amount\":\"1000\",\"subType\":\"1\"}"));

        assertEquals(true,response.contains("{\"from\":\"" + gdAccount3 + "\",\"to\":\"" + gdAccount3 +
                "\",\"tokenType\":\"" + oldEquityCode + "\",\"newTokenType\":\"" + newEquityCode + "\",\"amount\":\"4900\",\"subType\":\"0\"}"));
        assertEquals(true,response.contains("{\"from\":\"" + gdAccount3 + "\",\"to\":\"" + gdAccount3 +
                "\",\"tokenType\":\"" + oldEquityCode + "\",\"newTokenType\":\"" + newEquityCode + "\",\"amount\":\"1000\",\"subType\":\"1\"}"));

        assertEquals(true,response.contains("{\"from\":\"" + gdAccount4 + "\",\"to\":\"" + gdAccount4 +
                "\",\"tokenType\":\"" + oldEquityCode + "\",\"newTokenType\":\"" + newEquityCode + "\",\"amount\":\"4900\",\"subType\":\"0\"}"));
        assertEquals(true,response.contains("{\"from\":\"" + gdAccount4 + "\",\"to\":\"" + gdAccount4 +
                "\",\"tokenType\":\"" + oldEquityCode + "\",\"newTokenType\":\"" + newEquityCode + "\",\"amount\":\"1000\",\"subType\":\"1\"}"));

//        assertEquals(false,response.contains("register_account_obj_id"));//不应该包含监管数据信息

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

        //查交易详情
        response = store.GetTxDetail(
                JSONObject.fromObject(store.GetBlockByHeight(
                        JSONObject.fromObject(store.GetHeight()).getInt("data") -1)
                ).getJSONObject("data").getJSONArray("txs").getString(0));


        //检查交易详情
        JSONObject jsonObject1 = JSONObject.fromObject(response).getJSONObject("data");
        assertEquals("2",jsonObject1.getJSONObject("header").getString("version"));
        assertEquals("3",jsonObject1.getJSONObject("header").getString("type"));
        assertEquals("42",jsonObject1.getJSONObject("header").getString("subType"));

        JSONObject jsonObjectWVM = jsonObject1.getJSONObject("wvm").getJSONObject("wvmContractTx");
        assertEquals("DestroyInvestor",jsonObjectWVM.getJSONObject("arg").getString("method"));
        assertEquals(gdContractAddress,jsonObjectWVM.getString("name"));

        String args = jsonObjectWVM.getJSONObject("arg").toString();
        assertEquals(true,args.contains(clntNo));
        assertEquals(true,response.contains("DBSet"));

    }

}
