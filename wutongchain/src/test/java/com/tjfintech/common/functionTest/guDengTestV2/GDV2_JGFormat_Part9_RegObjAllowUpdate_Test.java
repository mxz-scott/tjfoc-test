package com.tjfintech.common.functionTest.guDengTestV2;

import com.alibaba.fastjson.JSON;
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

import java.text.SimpleDateFormat;
import java.util.*;

import static com.tjfintech.common.functionTest.guDengTestV2.GDCommonFunc.*;
import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassGD.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
/**
 * 登记对象标识允许使用已有对象标识 即允许登记对象更新
 */
public class GDV2_JGFormat_Part9_RegObjAllowUpdate_Test {

    TestBuilder testBuilder= TestBuilder.getInstance();
    GuDeng gd =testBuilder.getGuDeng();
    Store store =testBuilder.getStore();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    GDCommonFunc gdCF = new GDCommonFunc();
    GDUnitFunc uf = new GDUnitFunc();
    GDBeforeCondition gdBF = new GDBeforeCondition();
    public static String bizNoTest = "test" + Random(12);
    long issueAmount = 5000;
    long increaseAmount = 1000;
    long lockAmount = 500;
    long recycleAmount = 100;
    long changeAmount = 500;
    long transferAmount = 1000;
    Boolean bSame = true;

    Boolean bCheckList = true;

    String tempaccount_subject_ref = account_subject_ref;
    String tempsubject_investor_qualification_certifier_ref = subject_investor_qualification_certifier_ref;
    String tempproduct_issuer_subject_ref = product_issuer_subject_ref;
    String tempregister_transaction_ref = register_transaction_ref;

    public static String objID1 = "form1Reg"+Random(10);
    public static String objID11 = "form1Reg"+Random(10);
    public static String objID2 = "form2Reg"+Random(10);
    public static String objID3 = "form3Reg"+Random(10);
    public static String objID4 = "form4Reg"+Random(10);
    public static String objID5 = "form5Reg"+Random(10);


    @Rule
    public TestName tm = new TestName();

    @BeforeClass
    public static void Before()throws Exception{
        gdEquityCode = "prodEq" + Random(12);
        gdCompanyID = "P1Re" + Random(8);
        register_product_ref = gdEquityCode;
        roll_register_product_ref = gdEquityCode;
        transaction_custody_product_ref = gdEquityCode;
        GDBeforeCondition gdBefore = new GDBeforeCondition();
        gdBefore.gdCreateAccout();
//        gdBefore.initRegulationData();
        equityProductInfo = gdBefore.init03EquityProductInfo();
        bondProductInfo = null;
        fundProductInfo = null;

        log.info("+++++++++++++++++++++++++++++++++++++++++");
        log.info(objID1);log.info(objID11);log.info(objID2);log.info(objID3);log.info(objID4);log.info(objID5);
    }

    @Before
    public void resetVar(){


        register_event_type = 1;//非交易登记
        tempsubject_investor_qualification_certifier_ref =subject_investor_qualification_certifier_ref;
        tempregister_transaction_ref = register_transaction_ref;
    }

    @After
    public void calJGDataAfterTx()throws Exception{
        testCurMethodName = tm.getMethodName();
        GDUnitFunc uf = new GDUnitFunc();
        int endHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");
//        uf.checkJGHeaderOpVer(blockHeight,endHeight);

        subject_investor_qualification_certifier_ref = tempsubject_investor_qualification_certifier_ref;
        register_transaction_ref = tempregister_transaction_ref;
    }

    @Test
    public void TC06_shareIssue() throws Exception {
        log.info(registerInfo.toString());
        mapAddrRegObjId.clear(); //先清空map 这样后面map中拿到的就是此次发行的登记对象标识

        List<Map> shareList = gdConstructShareListWithObjID(gdAccount1,issueAmount,0,objID1);
        List<Map> shareList2 = gdConstructShareListWithObjID(gdAccount2,issueAmount,0,objID2,shareList);
        List<Map> shareList3 = gdConstructShareListWithObjID(gdAccount3,issueAmount,0,objID3, shareList2);
        List<Map> shareList4 = gdConstructShareListWithObjID(gdAccount4,issueAmount,0,objID4,shareList3);

        String response= uf.shareIssue(gdEquityCode,shareList4,false);
        JSONObject jsonObject=JSONObject.fromObject(response);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        String txDetail = store.GetTxDetail(txId);
        assertEquals("200", JSONObject.fromObject(txDetail).getString("state"));

        //检查各个查询对象返回信息中不包含敏感词
        assertEquals("不包含敏感词",true,
                gdCF.chkSensitiveWord(txDetail,regType));

        sleepAndSaveInfo(3000);
        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
//        assertEquals("200",JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");

        log.info("================================检查存证数据格式化《开始》================================");

        //遍历检查所有账户登记及交易存证信息
        for(int k = 0 ;k < dataShareList.size(); k++) {
            String tempAddr = JSONObject.fromObject(dataShareList.get(k)).getString("address");
            String tempSP = JSONObject.fromObject(dataShareList.get(k)).getString("shareProperty");
            String tempObjId = mapAddrRegObjId.get(tempAddr + tempSP).toString();

            register_subject_account_ref = "SH" + mapAccAddr.get(tempAddr);
            Map mapChkKeys = new HashMap();
            mapChkKeys.put("address","");
            mapChkKeys.put("txId",txId);
            mapChkKeys.put("objectid",tempObjId);
            mapChkKeys.put("version","0");
            mapChkKeys.put("contentType",regType);
            mapChkKeys.put("subProdSubType","");
            mapChkKeys.put("operationType","create");

            Map mapKeyUpdate = new HashMap();
            mapKeyUpdate.put("register_subject_account_ref","SH" + mapAccAddr.get(tempAddr));

            String json = JSON.toJSONString(mapKeyUpdate);
            mapChkKeys.put("updateMap",json);

            assertEquals("检查数据",true,gdCF.bCheckJGParams(mapChkKeys));

        }
        log.info("================================检查存证数据格式化《结束》================================");


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
        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
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

        assertEquals("校验对象1性质0","0",gdCF.getObjectLatestVer(objID1));
        assertEquals("校验对象1性质1","-1",gdCF.getObjectLatestVer(objID11));
        assertEquals("校验对象2","0",gdCF.getObjectLatestVer(objID2));
        assertEquals("校验对象3","0",gdCF.getObjectLatestVer(objID3));
        assertEquals("校验对象4","0",gdCF.getObjectLatestVer(objID4));
        assertEquals("校验对象5","-1",gdCF.getObjectLatestVer(objID5));
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

        log.info("++++++++++++++++++++++++++++++++++++++++++++++++++++++" + gdEquityCode);

        int oldProperty = 0;
        int newProperty = 1;

        Map testReg1 = gdBF.init05RegInfo();
        Map testReg2 = gdBF.init05RegInfo();
        String regObjId1 = objID1;
        String regObjId2 = objID11;

        testReg1.put("register_registration_object_id",regObjId1);
        testReg1.put("register_subject_account_ref","SH" + gdAccClientNo1);

        testReg2.put("register_registration_object_id",regObjId2);
        testReg2.put("register_subject_account_ref","SH" + gdAccClientNo1);

        List<Map> regListInfo = new ArrayList<>();
        regListInfo.add(testReg1);
        regListInfo.add(testReg2);

        String response= gd.GDShareChangeProperty(gdPlatfromKeyID,address,eqCode,changeAmount,oldProperty,newProperty,regListInfo);
        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        String txDetail = store.GetTxDetail(txId);
        assertEquals("200", JSONObject.fromObject(txDetail).getString("state"));

        mapAddrRegObjId.put(address + newProperty,regObjId2);//方便后面测试验证

        //检查各个查询对象返回信息中不包含敏感词
        assertEquals("不包含敏感词",true,gdCF.chkSensitiveWord(txDetail,regType));

        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200", JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");

        log.info("================================检查存证数据格式化《开始》================================");
        log.info("检查股权性质变更存证登记格式化及信息内容与传入一致");
        String regVer = gdCF.getObjectLatestVer(regObjId1);
        String regVer11 = gdCF.getObjectLatestVer(regObjId2);
        Map uriInfo = gdCF.getJGURIStoreHash(txId,conJGFileName(regObjId1,regVer),1);

        String tempAddr = address;

        //获取链上mini url的存证信息 并检查是否包含uri信息 每个登记都是新的 则都是0
        String regfileName1 = conJGFileName(regObjId1,regVer);
        String regfileName2 = conJGFileName(regObjId2,regVer11);
        String chkRegURI1 = regfileName1;
        String chkRegURI2 = regfileName2;

        log.info(uriInfo.get("storeData").toString());
        log.info(chkRegURI1 + "      " + chkRegURI2);
        assertEquals(true,uriInfo.get("storeData").toString().contains(chkRegURI1));
        assertEquals(true,uriInfo.get("storeData").toString().contains(chkRegURI2));
        assertEquals(true,gdCF.bContainJGFlag(uriInfo.get("storeData").toString()));//确认meta信息包含监管关键字

        //直接从minio上获取报送数据文件信息
        Map getRegInfo1 = gdCF.constructJGDataFromStr(regfileName1,regType,"");
        Map getRegInfo2 = gdCF.constructJGDataFromStr(regfileName2,regType,"");

        register_subject_account_ref = "SH" + gdAccClientNo1;
        register_product_ref = gdEquityCode;
        Map regInfoInput = testReg1;
//        Map regInfoInput = gdBF.init05RegInfo();
        regInfoInput.put("content",gdCF.constructContentTreeMap(regType,regObjId1,regVer,"update",String.valueOf(ts5)));
        log.info("检查登记存证信息内容与传入一致\n" + regInfoInput.toString() + "\n" + getRegInfo1.toString());
        bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(regInfoInput,regType)),replaceCertain(getRegInfo1.toString()));
        assertEquals("1检查数据是否一致" ,true,bSame);

        regInfoInput = testReg2;
        regInfoInput.put("content",gdCF.constructContentTreeMap(regType,regObjId2,regVer11,"create",String.valueOf(ts5)));
        log.info("检查登记存证信息内容与传入一致\n" + regInfoInput.toString() + "\n" + getRegInfo1.toString());
        bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(regInfoInput,regType)),replaceCertain(getRegInfo2.toString()));
        assertEquals("2检查数据是否一致" ,true,bSame);

        log.info("================================检查存证数据格式化《结束》================================");

        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1, 4500, 0, 0, mapShareENCN().get("0"), respShareList);
        respShareList = gdConstructQueryShareList(gdAccount1, 500, 1, 0, mapShareENCN().get("1"), respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2, 5000, 0, 0, mapShareENCN().get("0"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3, 5000, 0, 0, mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4, 5000, 0, 0, mapShareENCN().get("0"), respShareList3);

        //检查存在余额的股东列表
        assertEquals(respShareList4.size(), dataShareList.size());

        List<Map> getShareList = getShareListFromQueryNoZeroAcc(dataShareList);

        assertEquals(respShareList4.size(), getShareList.size());
        assertEquals(true, respShareList4.containsAll(getShareList) && getShareList.containsAll(respShareList4));


        //查询股东持股情况 无当前股权代码信息
        query = gd.GDGetShareHolderInfo(gdContractAddress, gdAccClientNo1);
        assertEquals(gdAccClientNo1, JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));

        assertEquals(true, query.contains("\"shareholderNo\":\"SH" + gdAccClientNo1 + "\""));
        assertEquals(true, query.contains("\"address\":\"" + gdAccount1 + "\""));
        assertEquals(true, query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":4500,\"lockAmount\":0}"));
        assertEquals(true, query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":500,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress, gdAccClientNo2);
        assertEquals(gdAccClientNo2, JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true, query.contains("\"shareholderNo\":\"SH" + gdAccClientNo2 + "\""));
        assertEquals(true, query.contains("\"address\":\"" + gdAccount2 + "\""));
        assertEquals(true, query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":5000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress, gdAccClientNo3);
        assertEquals(gdAccClientNo3, JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true, query.contains("\"shareholderNo\":\"SH" + gdAccClientNo3 + "\""));
        assertEquals(true, query.contains("\"address\":\"" + gdAccount3 + "\""));
        assertEquals(true, query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":5000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress, gdAccClientNo4);
        assertEquals(gdAccClientNo4, JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true, query.contains("\"shareholderNo\":\"SH" + gdAccClientNo4 + "\""));
        assertEquals(true, query.contains("\"address\":\"" + gdAccount4 + "\""));
        assertEquals(true, query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":5000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress, gdAccClientNo5);
        assertEquals(false, query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        query = gd.GDGetShareHolderInfo(gdContractAddress, gdAccClientNo6);
        assertEquals(false, query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        assertEquals("校验对象1性质0","1",gdCF.getObjectLatestVer(objID1));
        assertEquals("校验对象1性质1","0",gdCF.getObjectLatestVer(objID11));
        assertEquals("校验对象2","0",gdCF.getObjectLatestVer(objID2));
        assertEquals("校验对象3","0",gdCF.getObjectLatestVer(objID3));
        assertEquals("校验对象4","0",gdCF.getObjectLatestVer(objID4));
        assertEquals("校验对象5","-1",gdCF.getObjectLatestVer(objID5));
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
        String tempObjIdFrom = objID1;
        String tempObjIdTo = objID5;

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

        mapAddrRegObjId.put(toAddr + shareProperty,tempObjIdTo);//方便后面测试验证

        String tempObj = gdCompanyID;

        String query2 = gd.GDObjectQueryByVer(gdCompanyID,-1);

        //执行交易
        String response= gd.GDShareTransfer(keyId,fromAddr,transferAmount,toAddr,shareProperty,eqCode,txInfo,fromNow,toNow);
        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        String txDetail = store.GetTxDetail(txId);
        assertEquals("200", JSONObject.fromObject(txDetail).getString("state"));

        //检查各个查询对象返回信息中不包含敏感词
        assertEquals("不包含主体数据敏感词",true,gdCF.chkSensitiveWord(txDetail,subjectType));
        assertEquals("不包含登记数据敏感词",true,gdCF.chkSensitiveWord(txDetail,regType));
        assertEquals("不包含交易报告敏感词",true,gdCF.chkSensitiveWord(txDetail,txrpType));

        //获取上链交易时间戳
        long onChainTS = JSONObject.fromObject(store.GetTxDetail(txId)).getJSONObject("data").getJSONObject("header").getLong("timestamp");

        String getTotal = enterpriseSubjectInfo.get("subject_shareholders_number").toString();
        int oldTotal = Integer.parseInt(getTotal);
        enterpriseSubjectInfo.put("subject_shareholders_number",oldTotal + 1);     //变更总股东数 + 1 转给新的账户

        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200", JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");

        log.info("================================检查存证数据格式化《开始》================================");

//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        String sd = sdf.format(new Date(onChainTS)); // 时间戳转换日期
        log.info("检查过户转让存证登记格式化及信息内容与传入一致:" + tempObjIdFrom);
//        fromNow.put("register_rights_change_amount","-" + transferAmount);
//        fromNow.put("register_time",txInformation.get("transaction_close_time").toString());
//        fromNow.put("register_available_balance",issueAmount - changeAmount - transferAmount);
//        fromNow.put("register_rights_frozen_balance", 0);   //当前冻结余额修改为实际冻结数

//        toNow.put("register_rights_change_amount",transferAmount);
//        toNow.put("register_rights_frozen_balance", 0);   //当前冻结余额修改为实际冻结数
//        toNow.put("register_time",txInformation.get("transaction_close_time").toString());
//        toNow.put("register_available_balance",transferAmount);


        log.info("================================检查存证数据格式化《开始》================================");
        String regVer = gdCF.getObjectLatestVer(tempObjIdFrom);
        String regVerTo = gdCF.getObjectLatestVer(tempObjIdTo);
        Map uriInfo = gdCF.getJGURIStoreHash(txId,conJGFileName(tempObjIdFrom,regVer),1);

        log.info("检查股权性质变更存证登记格式化及信息内容与传入一致");

        String txRpVer = "0" ;//gdCF.getObjectLatestVer(account_subject_ref);
        String subVer = gdCF.getObjectLatestVer(gdCompanyID);

        //获取链上mini url的存证信息 并检查是否包含uri信息 每个登记都是新的 则都是0
        String regfileName1 = conJGFileName(tempObjIdFrom,regVer);
        String regfileName2 = conJGFileName(tempObjIdTo,regVerTo);
        String txfileName = conJGFileName(txRpObjId,txRpVer);
        String subfileName = conJGFileName(gdCompanyID,subVer);

        String chkRegURI1 =  regfileName1;
        String chkRegURI2 = regfileName2;
        String chkTxRpURI = txfileName;
        String chkSubURI = subfileName;

        log.info(uriInfo.get("storeData").toString());
        log.info(chkRegURI1 + "      " + chkRegURI2);
        assertEquals(true,uriInfo.get("storeData").toString().contains(chkRegURI1));
        assertEquals(true,uriInfo.get("storeData").toString().contains(chkRegURI2));
        assertEquals(true,uriInfo.get("storeData").toString().contains(chkTxRpURI));
        assertEquals(true,uriInfo.get("storeData").toString().contains(chkSubURI));
        assertEquals(true,gdCF.bContainJGFlag(uriInfo.get("storeData").toString()));//确认meta信息包含监管关键字

        //直接从minio上获取报送数据文件信息
        Map getRegInfo1 = gdCF.constructJGDataFromStr(regfileName1,regType,"");
        Map getRegInfo2 = gdCF.constructJGDataFromStr(regfileName2,regType,"");
        Map getTxRpInfo = gdCF.constructJGDataFromStr(txfileName,txrpType,"");
        Map getSubInfo = gdCF.constructJGDataFromStr(subfileName,subjectType,"1");


        register_subject_account_ref = "SH" + gdAccClientNo1;
        //填充header content字段
        fromNow.put("content",gdCF.constructContentTreeMap(regType,tempObjIdFrom,regVer,"update",String.valueOf(ts5)));
        log.info("检查登记存证信息内容与传入一致\n" + fromNow.toString() + "\n" + getRegInfo1.toString());
        bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(fromNow,regType)),replaceCertain(getRegInfo1.toString()));
        assertEquals("from检查数据是否一致" ,true,bSame);
//        log.info("from登记检查数据是否一致" + bSame);

        register_subject_account_ref = "SH" + gdAccClientNo5;
        //填充header content字段
        toNow.put("content",gdCF.constructContentTreeMap(regType,tempObjIdTo,regVerTo,"create",String.valueOf(ts5)));
        log.info("检查登记存证信息内容与传入一致\n" + toNow.toString() + "\n" + getRegInfo1.toString());
        bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(toNow,regType)),replaceCertain(getRegInfo2.toString()));
        assertEquals("from检查数据是否一致" ,true,bSame);
//        log.info("to登记检查数据是否一致" + bSame);

        //填充header content字段
        txInfo.put("content",gdCF.constructContentTreeMap(txrpType,txRpObjId,txRpVer,"create",String.valueOf(ts4)));
        bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(txInfo,txrpType)),replaceCertain(getTxRpInfo.toString()));
        assertEquals("检查转让交易报告数据是否一致" ,true,bSame);
//        log.info("交易报告检查数据是否一致" + bSame);


        query2 = gd.GDObjectQueryByVer(gdCompanyID,-1);
        int totalMembers = JSONObject.fromObject(query2).getJSONObject("data").getJSONObject("body"
                                ).getJSONObject("subject_information").getJSONObject("organization_subject_information"
                                ).getJSONObject("basic_information_of_enterprise").getJSONObject("basic_information_description"
                                ).getInt("subject_shareholders_number");

        //填充header content字段
        Map enSubInfo = gdBF.init01EnterpriseSubjectInfo();

        //更新股东数量 非用户传入的数据 而是依据前面转让给新账户后的股东数
        enSubInfo.put("subject_shareholders_number",totalMembers);

        log.info("+++++++++++++++++++++++++++++++++++++++++++++++++++");
        log.info(getSubInfo.toString());
        Map mapContent = gdCF.constructContentTreeMap(subjectType,gdCompanyID,subVer,"update",String.valueOf(ts1));
        enSubInfo.put("content",mapContent);
        bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(enSubInfo,subjectType)),replaceCertain(getSubInfo.toString()));
        assertEquals("检查转让主体数据是否一致" ,true,bSame);


        log.info("================================检查存证数据格式化《结束》================================");

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

        assertEquals("校验对象1性质0","2",gdCF.getObjectLatestVer(objID1));
        assertEquals("校验对象1性质1","0",gdCF.getObjectLatestVer(objID11));
        assertEquals("校验对象2","0",gdCF.getObjectLatestVer(objID2));
        assertEquals("校验对象3","0",gdCF.getObjectLatestVer(objID3));
        assertEquals("校验对象4","0",gdCF.getObjectLatestVer(objID4));
        assertEquals("校验对象5","0",gdCF.getObjectLatestVer(objID5));
    }

    //交易报告填写且类型为1 发行融资 即需要报送交易报告
    @Test
    public void TC09_shareIncrease() throws Exception {

        log.info("增发前查询机构主体信息");
        String query2 = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);
//        BigDecimal totalShares = new BigDecimal(JSONObject.fromObject(query2).getJSONObject("data").getString("subject_total_share_capital"));

        register_event_type = 1;//非交易登记 不报送交易报告库

        String eqCode = gdEquityCode;
        String reason = "股份分红";
        String txObjId = "4increaseObj" + Random(6);

//        Map eqProd = gdBF.init03EquityProductInfo();
//        Map txInfo = gdBF.init04TxInfo();
//        txInfo.put("transaction_object_id",txObjId);
//        register_transaction_ref = txObjId; //此处为发行融资 设置登记引用接口中的交易报告

        log.info("发行主体版本  " + gdCF.getObjectLatestVer(gdCompanyID));

        List<Map> shareList = gdConstructShareListWithObjID(gdAccount1,increaseAmount,0,objID1);
        List<Map> shareList2 = gdConstructShareListWithObjID(gdAccount2,increaseAmount,0, objID2,shareList);
        List<Map> shareList3 = gdConstructShareListWithObjID(gdAccount3,increaseAmount,0, objID3,shareList2);
        List<Map> shareList4 = gdConstructShareListWithObjID(gdAccount4,increaseAmount,0, objID4,shareList3);

        String response= gd.GDShareIncreaseNoProduct(gdPlatfromKeyID,eqCode,shareList4,reason);
        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        String txDetail = store.GetTxDetail(txId);
        assertEquals("200", JSONObject.fromObject(txDetail).getString("state"));

        //检查各个查询对象返回信息中不包含敏感词
        assertEquals("不包含主体数据敏感词",true,gdCF.chkSensitiveWord(txDetail,subjectType));
        assertEquals("不包含登记数据敏感词",true,gdCF.chkSensitiveWord(txDetail,regType));
//        assertEquals("不包含交易报告敏感词",true,gdCF.chkSensitiveWord(txDetail,txrpType));

        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");

        log.info("================================检查存证数据格式化《开始》================================");

        Map uriInfo = gdCF.getJGURIStoreHash(txId,"supervision",1);

        //遍历检查所有账户登记及交易存证信息
        for(int k = 0 ;k < shareList4.size(); k++) {
            String tempAddr = JSONObject.fromObject(shareList4.get(k)).getString("address");
            String tempSP = JSONObject.fromObject(shareList4.get(k)).getString("shareProperty");
            String tempObjId = mapAddrRegObjId.get(tempAddr + tempSP).toString();

            register_subject_account_ref = "SH" + mapAccAddr.get(tempAddr);
            Map mapChkKeys = new HashMap();
            mapChkKeys.put("address","");
            mapChkKeys.put("txId",txId);
            mapChkKeys.put("objectid",tempObjId);
            String tempVer = gdCF.getObjectLatestVer(tempObjId);
            assertNotEquals("0",tempVer);
            mapChkKeys.put("version",tempVer);
            mapChkKeys.put("contentType",regType);
            mapChkKeys.put("subProdSubType","");
            mapChkKeys.put("operationType","update");

            Map mapKeyUpdate = new HashMap();
            mapKeyUpdate.put("register_subject_account_ref","SH" + mapAccAddr.get(tempAddr));

            String json = JSON.toJSONString(mapKeyUpdate);
            mapChkKeys.put("updateMap",json);

            assertEquals("检查数据",true,gdCF.bCheckJGParams(mapChkKeys));
        }

        String newEqProdVer = gdCF.getObjectLatestVer(gdEquityCode);

        log.info(uriInfo.get("storeData").toString());
        product_issuer_subject_ref = gdCompanyID;

        log.info("主体版本信息 " + gdCF.getObjectLatestVer(gdCompanyID));
//        log.info("产品版本信息 " +  gdEquityCode +  newEqProdVer );
//
//        Map getProInfo = gdCF.constructJGDataFromStr(conJGFileName(gdEquityCode, newEqProdVer), prodType, "1");
//        eqProd.put("content",gdCF.constructContentTreeMap(prodType, gdEquityCode, newEqProdVer, "update", String.valueOf(ts3)));
//        log.info("检查产品存证信息内容与传入一致\n" + eqProd.toString() + "\n" + getProInfo.toString());
//        bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(eqProd, prodType)), replaceCertain(getProInfo.toString()));
//        assertEquals("检查增发产品是否一致" ,true,bSame);
//        log.info("检查增发产品是否一致:" + bSame);


        product_issuer_subject_ref = gdCompanyID;
        transaction_custody_product_ref = gdEquityCode;

//        txInfo.put("content",gdCF.constructContentTreeMap(txrpType, txObjId, "0", "create", String.valueOf(ts4)));
//        Map getTxRpInfo = gdCF.constructJGDataFromStr(conJGFileName(txObjId, "0"), txrpType, "");
//        bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(txInfo,txrpType)),replaceCertain(getTxRpInfo.toString()));
//        assertEquals("检查增发交易报告数据是否一致" ,true,bSame);
//        log.info("检查增发交易报告数据是否一致:" + bSame);


        query2 = gd.GDObjectQueryByVer(gdCompanyID,-1);
        int totalMembers = JSONObject.fromObject(query2).getJSONObject("data").getJSONObject("body"
        ).getJSONObject("subject_information").getJSONObject("organization_subject_information"
        ).getJSONObject("basic_information_of_enterprise").getJSONObject("basic_information_description"
        ).getInt("subject_shareholders_number");

        Map enSub = gdBF.init01EnterpriseSubjectInfo();
        String subObjId = gdCompanyID;
        String subVer = gdCF.getObjectLatestVer(subObjId);

        //更新股东数量 非用户传入的数据 而是依据前面转让给新账户后的股东数
        enSub.put("subject_shareholders_number",totalMembers);

        enSub.put("content",gdCF.constructContentTreeMap(subjectType, subObjId, subVer, "update", String.valueOf(ts1)));
        Map getEnSubInfo = gdCF.constructJGDataFromStr(conJGFileName(subObjId, subVer), subjectType, "1");
        bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(enSub,subjectType)),replaceCertain(getEnSubInfo.toString()));
        assertEquals("检查增发主体数据是否一致" ,true,bSame);
        log.info("检查增发主体数据是否一致:" + bSame);

        log.info("================================检查存证数据格式化《结束》================================");


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
//
//        log.info("判断增发前后机构主体查询总股本数增加数正确");
//        assertEquals(totalShares.add(new BigDecimal("4000")),totalShares2);

        assertEquals("校验对象1性质0","3",gdCF.getObjectLatestVer(objID1));
        assertEquals("校验对象1性质1","0",gdCF.getObjectLatestVer(objID11));
        assertEquals("校验对象2","1",gdCF.getObjectLatestVer(objID2));
        assertEquals("校验对象3","1",gdCF.getObjectLatestVer(objID3));
        assertEquals("校验对象4","1",gdCF.getObjectLatestVer(objID4));
        assertEquals("校验对象5","0",gdCF.getObjectLatestVer(objID5));

    }


    @Test
    public void TC10_shareLock() throws Exception {

        log.info("发行主体版本  " + gdCF.getObjectLatestVer(gdCompanyID));

        sleepAndSaveInfo(5000);
        String bizNo = bizNoTest;
        String eqCode = gdEquityCode;
        String address = gdAccount1;
        int shareProperty = 0;
        String reason = "司法冻结";
        String cutoffDate = "2022-09-30";

        register_product_ref = gdEquityCode;
        register_subject_account_ref = "SH" + mapAccAddr.get(address);
        //登记数据
        Map regInfo = gdBF.init05RegInfo();
        String tempObjId = objID1;
        regInfo.put("register_registration_object_id",tempObjId);
        regInfo.put("register_subject_account_ref","SH" + mapAccAddr.get(address));

        String response= gd.GDShareLock(bizNo,address,eqCode,lockAmount,shareProperty,reason,cutoffDate,regInfo);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        String txDetail = store.GetTxDetail(txId);
        assertEquals("200", JSONObject.fromObject(txDetail).getString("state"));

        //检查各个查询对象返回信息中不包含敏感词
        assertEquals("不包含登记数据敏感词",true,gdCF.chkSensitiveWord(txDetail,regType));

        log.info("================================检查存证数据格式化《开始》================================");
        String tempAddr = address;

        log.info("检查股权性质变更存证登记格式化及信息内容与传入一致");
        String regVer = gdCF.getObjectLatestVer(tempObjId);
        Map uriInfo = gdCF.getJGURIStoreHash(txId,conJGFileName(tempObjId,regVer),1);

        //获取链上mini url的存证信息 并检查是否包含uri信息 每个登记都是新的 则都是0
        String regfileName1 = conJGFileName(tempObjId,regVer);
        String chkRegURI1 = regfileName1;

        log.info(uriInfo.get("storeData").toString());
        assertEquals(true,uriInfo.get("storeData").toString().contains(chkRegURI1));
        assertEquals(true,gdCF.bContainJGFlag(uriInfo.get("storeData").toString()));//确认meta信息包含监管关键字


        log.info("发行主体版本  " + gdCF.getObjectLatestVer(gdCompanyID));

        //直接从minio上获取报送数据文件信息
        Map getRegInfo1 = gdCF.constructJGDataFromStr(regfileName1,regType,"");

        log.info("发行主体版本  " + gdCF.getObjectLatestVer(gdCompanyID));

        regInfo.put("content",gdCF.constructContentTreeMap(regType,tempObjId,regVer,"update",String.valueOf(ts5)));
        log.info("检查登记存证信息内容与传入一致\n" + regInfo.toString() + "\n" + getRegInfo1.toString());
        bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(regInfo,regType)),replaceCertain(getRegInfo1.toString()));
        assertEquals("1检查数据是否一致" ,true,bSame);

        log.info("================================检查存证数据格式化《结束》================================");

        if(!bCheckList) return;

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

        assertEquals("校验对象1性质0","4",gdCF.getObjectLatestVer(objID1));
        assertEquals("校验对象1性质1","0",gdCF.getObjectLatestVer(objID11));
        assertEquals("校验对象2","1",gdCF.getObjectLatestVer(objID2));
        assertEquals("校验对象3","1",gdCF.getObjectLatestVer(objID3));
        assertEquals("校验对象4","1",gdCF.getObjectLatestVer(objID4));
        assertEquals("校验对象5","0",gdCF.getObjectLatestVer(objID5));
    }

    @Test
    public void TC11_shareUnlock() throws Exception {

        log.info("发行主体版本  " + gdCF.getObjectLatestVer(gdCompanyID));
        sleepAndSaveInfo(3000);

        String bizNo = bizNoTest;
        String eqCode = gdEquityCode;
        long amount = 500;

        register_product_ref = gdEquityCode;
        register_subject_account_ref = "SH" + gdAccClientNo1;
        //登记数据
        Map regInfo = gdBF.init05RegInfo();
        String tempObjId = objID1;
        regInfo.put("register_registration_object_id",tempObjId);
        regInfo.put("register_subject_account_ref","SH" + gdAccClientNo1);

        String response= gd.GDShareUnlock(bizNo,eqCode,amount,regInfo);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        String txDetail = store.GetTxDetail(txId);
        assertEquals("200", JSONObject.fromObject(txDetail).getString("state"));

        //检查各个查询对象返回信息中不包含敏感词
        assertEquals("不包含登记数据敏感词",true,gdCF.chkSensitiveWord(txDetail,regType));

        log.info("================================检查存证数据格式化《开始》================================");


        log.info("检查股权性质变更存证登记格式化及信息内容与传入一致");
        String regVer = gdCF.getObjectLatestVer(tempObjId);
        Map uriInfo = gdCF.getJGURIStoreHash(txId,conJGFileName(tempObjId,regVer),1);

        //获取链上mini url的存证信息 并检查是否包含uri信息 每个登记都是新的 则都是0
        String regfileName1 = conJGFileName(tempObjId,regVer);
        String chkRegURI1 = regfileName1;

        log.info(uriInfo.get("storeData").toString());
        assertEquals(true,uriInfo.get("storeData").toString().contains(chkRegURI1));
        assertEquals(true,gdCF.bContainJGFlag(uriInfo.get("storeData").toString()));//确认meta信息包含监管关键字

        log.info("发行主体版本  " + gdCF.getObjectLatestVer(gdCompanyID));

        //直接从minio上获取报送数据文件信息
        Map getRegInfo1 = gdCF.constructJGDataFromStr(regfileName1,regType,"");

        log.info("发行主体版本  " + gdCF.getObjectLatestVer(gdCompanyID));
        register_subject_account_ref = "SH" + gdAccClientNo1;
        regInfo.put("content",gdCF.constructContentTreeMap(regType,tempObjId,regVer,"update",String.valueOf(ts5)));
        log.info("检查登记存证信息内容与传入一致\n" + regInfo.toString() + "\n" + getRegInfo1.toString());
        bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(regInfo,regType)),replaceCertain(getRegInfo1.toString()));
        assertEquals("1检查数据是否一致" ,true,bSame);

        log.info("================================检查存证数据格式化《结束》================================");

        if(!bCheckList) return;

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

        assertEquals("校验对象1性质0","5",gdCF.getObjectLatestVer(objID1));
        assertEquals("校验对象1性质1","0",gdCF.getObjectLatestVer(objID11));
        assertEquals("校验对象2","1",gdCF.getObjectLatestVer(objID2));
        assertEquals("校验对象3","1",gdCF.getObjectLatestVer(objID3));
        assertEquals("校验对象4","1",gdCF.getObjectLatestVer(objID4));
        assertEquals("校验对象5","0",gdCF.getObjectLatestVer(objID5));

    }

    @Test
    public void TC1201_shareRecycleOneAcc() throws Exception {
        log.info("发行主体版本  " + gdCF.getObjectLatestVer(gdCompanyID));

        String eqCode = gdEquityCode;
        String remark = "777777";

        String address = gdAccount1;
        log.info("回收前查询机构主体信息");

        String subObjId = gdCompanyID;

        String query2 = gd.GDObjectQueryByVer(gdCompanyID,-1);
        int totalMembers = JSONObject.fromObject(query2).getJSONObject("data").getJSONObject("body"
                            ).getJSONObject("subject_information").getJSONObject("organization_subject_information"
                            ).getJSONObject("basic_information_of_enterprise").getJSONObject("basic_information_description"
                            ).getInt("subject_shareholders_number");

        List<Map> shareList = gdConstructShareListWithObjID(address,recycleAmount,0,objID1);

        String response= gd.GDShareRecycle(gdPlatfromKeyID,eqCode,shareList,remark);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));


        int totalMembersAft = JSONObject.fromObject(query2).getJSONObject("data").getJSONObject("body"
        ).getJSONObject("subject_information").getJSONObject("organization_subject_information"
        ).getJSONObject("basic_information_of_enterprise").getJSONObject("basic_information_description"
        ).getInt("subject_shareholders_number");

        assertEquals(totalMembers,totalMembersAft);//总数无变更

        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");


        log.info("================================检查存证数据格式化《开始》================================");
        String tempAddr = address;
        String tempSP = "0";
        String tempObjId = mapAddrRegObjId.get(tempAddr + tempSP).toString();

        register_subject_account_ref = "SH" + mapAccAddr.get(tempAddr);
        Map mapChkKeys = new HashMap();
        mapChkKeys.put("address","");
        mapChkKeys.put("txId",txId);
        mapChkKeys.put("objectid",tempObjId);

        String tempVer = gdCF.getObjectLatestVer(tempObjId);
        assertNotEquals("0",tempVer);
        mapChkKeys.put("version",tempVer);
        mapChkKeys.put("contentType",regType);
        mapChkKeys.put("subProdSubType","");
        mapChkKeys.put("operationType","update");



        Map mapKeyUpdate = new HashMap();
        mapKeyUpdate.put("register_subject_account_ref","SH" + mapAccAddr.get(tempAddr));

        String json = JSON.toJSONString(mapKeyUpdate);
        mapChkKeys.put("updateMap",json);

        assertEquals("检查登记数据",true,gdCF.bCheckJGParams(mapChkKeys));


        log.info("检查回收存证主体格式化及信息内容与传入一致");
        mapChkKeys = new HashMap();
        assertEquals("更新主体版本非0",false,gdCF.getObjectLatestVer(gdCompanyID).equals("0"));
        mapChkKeys.clear();
        mapChkKeys.put("address","");
        mapChkKeys.put("txId",txId);
        mapChkKeys.put("objectid",gdCompanyID);
        mapChkKeys.put("version",gdCF.getObjectLatestVer(gdCompanyID));
        mapChkKeys.put("contentType",subjectType);
        mapChkKeys.put("subProdSubType","1");
        mapChkKeys.put("operationType","update");
        mapKeyUpdate =  new HashMap();
        mapKeyUpdate.put("subject_shareholders_number",totalMembersAft);
        json = JSON.toJSONString(mapKeyUpdate);
        mapChkKeys.put("updateMap",json);

        assertEquals("检查数据-主体",true,gdCF.bCheckJGParams(mapChkKeys));
        log.info("================================检查存证数据格式化《结束》================================");


        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,4400,0,0,mapShareENCN().get("0"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount1,500,1,0,mapShareENCN().get("1"),respShareList);
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
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":4400,\"lockAmount\":0}"));
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


        log.info("回收后查询机构主体信息");
        String query3 = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);
//        BigDecimal totalShares2 = new BigDecimal(JSONObject.fromObject(query3).getJSONObject("data").getString("subject_total_share_capital"));
//
//        log.info("判断增发前后机构主体查询总股本数增加数正确");
//        assertEquals(totalShares.subtract(new BigDecimal("100")),totalShares2);

        assertEquals("校验对象1性质0","6",gdCF.getObjectLatestVer(objID1));
        assertEquals("校验对象1性质1","0",gdCF.getObjectLatestVer(objID11));
        assertEquals("校验对象2","1",gdCF.getObjectLatestVer(objID2));
        assertEquals("校验对象3","1",gdCF.getObjectLatestVer(objID3));
        assertEquals("校验对象4","1",gdCF.getObjectLatestVer(objID4));
        assertEquals("校验对象5","0",gdCF.getObjectLatestVer(objID5));

    }

    @Test
    public void TC1202_shareRecycleMultiAcc() throws Exception {

        String eqCode = gdEquityCode;
        String remark = "777777";

        log.info("多个回收前查询机构主体信息");
        String query2 = gd.GDObjectQueryByVer(gdCompanyID,-1);
        int totalMembers = JSONObject.fromObject(query2).getJSONObject("data").getJSONObject("body"
                            ).getJSONObject("subject_information").getJSONObject("organization_subject_information"
                            ).getJSONObject("basic_information_of_enterprise").getJSONObject("basic_information_description"
                            ).getInt("subject_shareholders_number");

        List<Map> shareList = gdConstructShareListWithObjID(gdAccount1,100,0,objID1);
        List<Map> shareList2 = gdConstructShareListWithObjID(gdAccount2,100,0,objID2,shareList);
        List<Map> shareList3 = gdConstructShareListWithObjID(gdAccount3,100,0,objID3,shareList2);
        List<Map> shareList4 = gdConstructShareListWithObjID(gdAccount4,100,0, objID4,shareList3);

        String response= gd.GDShareRecycle(gdPlatfromKeyID,eqCode,shareList4,remark);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        int totalMembersAft = JSONObject.fromObject(query2).getJSONObject("data").getJSONObject("body"
        ).getJSONObject("subject_information").getJSONObject("organization_subject_information"
        ).getJSONObject("basic_information_of_enterprise").getJSONObject("basic_information_description"
        ).getInt("subject_shareholders_number");

        assertEquals(totalMembers,totalMembersAft);//总数无变更

        log.info("================================检查存证数据格式化《开始》================================");

        for(int k = 0;k < shareList4.size();k ++) {
            String tempAddr = JSONObject.fromObject(shareList4.get(k)).getString("address");
            String tempSP = JSONObject.fromObject(shareList4.get(k)).getString("shareProperty");
            String tempObjId = mapAddrRegObjId.get(tempAddr + tempSP).toString();

            register_subject_account_ref = "SH" + mapAccAddr.get(tempAddr);
            Map mapChkKeys = new HashMap();
            mapChkKeys.put("address","");
            mapChkKeys.put("txId",txId);
            mapChkKeys.put("objectid",tempObjId);

            String tempVer = gdCF.getObjectLatestVer(tempObjId);
            assertNotEquals("0",tempVer);
            mapChkKeys.put("version",tempVer);
            mapChkKeys.put("contentType",regType);
            mapChkKeys.put("subProdSubType","");
            mapChkKeys.put("operationType","update");

            Map mapKeyUpdate = new HashMap();
            mapKeyUpdate.put("register_subject_account_ref","SH" + mapAccAddr.get(tempAddr));

            String json = JSON.toJSONString(mapKeyUpdate);
            mapChkKeys.put("updateMap",json);

            assertEquals("检查数据",true,gdCF.bCheckJGParams(mapChkKeys));
        }

        log.info("检查回收存证主体格式化及信息内容与传入一致");
        log.info("检查回收存证主体格式化及信息内容与传入一致");
        Map mapChkKeys = new HashMap();
        assertEquals("更新主体版本非0",false,gdCF.getObjectLatestVer(gdCompanyID).equals("0"));
        mapChkKeys.clear();
        mapChkKeys.put("address","");
        mapChkKeys.put("txId",txId);
        mapChkKeys.put("objectid",gdCompanyID);
        mapChkKeys.put("version",gdCF.getObjectLatestVer(gdCompanyID));
        mapChkKeys.put("contentType",subjectType);
        mapChkKeys.put("subProdSubType","1");
        mapChkKeys.put("operationType","update");
        Map mapKeyUpdate =  new HashMap();
        mapKeyUpdate.put("subject_shareholders_number",totalMembersAft);
        String json = JSON.toJSONString(mapKeyUpdate);
        mapChkKeys.put("updateMap",json);

        assertEquals("检查数据-主体",true,gdCF.bCheckJGParams(mapChkKeys));

        log.info("================================检查存证数据格式化《结束》================================");


        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");


        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,4300,0,0,mapShareENCN().get("0"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount1,500,1,0,mapShareENCN().get("1"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount5,1000,0,0,mapShareENCN().get("0"),respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2,5900,0,0,mapShareENCN().get("0"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3,5900,0,0,mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4,5900,0,0,mapShareENCN().get("0"), respShareList3);



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
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":4300,\"lockAmount\":0}"));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":500,\"lockAmount\":0}"));

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
//
//        log.info("判断增发前后机构主体查询总股本数增加数正确");
//        assertEquals(totalShares.subtract(new BigDecimal("400")),totalShares2);

        assertEquals("校验对象1性质0","7",gdCF.getObjectLatestVer(objID1));
        assertEquals("校验对象1性质1","0",gdCF.getObjectLatestVer(objID11));
        assertEquals("校验对象2","2",gdCF.getObjectLatestVer(objID2));
        assertEquals("校验对象3","2",gdCF.getObjectLatestVer(objID3));
        assertEquals("校验对象4","2",gdCF.getObjectLatestVer(objID4));
        assertEquals("校验对象5","0",gdCF.getObjectLatestVer(objID5));
    }

    @Test
    public void TC13_shareChangeBoard() throws Exception {

        String oldEquityCode = gdEquityCode;
        String newEquityCode = "newCode" + Random(5);
        String cpnyId = gdCompanyID;
        log.info(mapAddrRegObjId.toString());

        gdEquityCode = newEquityCode;
        List<Map> regList = uf.getAllHolderListRegWithExistRegObjID(oldEquityCode,regNo);


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
        String details = store.GetTxDetail(txId);
        assertEquals("200",JSONObject.fromObject(details).getString("state"));

        //获取上链交易时间戳
        long onChainTS = JSONObject.fromObject(details).getJSONObject("data").getJSONObject("header").getLong("timestamp");

        gdEquityCode = newEquityCode;

        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");
        String totalAmount = getTotalAmountFromShareList(dataShareList);

        log.info("================================检查存证数据格式化《开始》================================");

        //遍历检查所有账户登记及交易存证信息
        for(int k = 0 ;k < dataShareList.size(); k++) {
            String tempAddr = JSONObject.fromObject(dataShareList.get(k)).getString("address");
            String tempSP = JSONObject.fromObject(dataShareList.get(k)).getString("shareProperty");
            String tempObjId = mapAddrRegObjId.get(tempAddr + tempSP).toString();

            register_subject_account_ref = "SH" + mapAccAddr.get(tempAddr);
            register_product_ref = newEquityCode;

            Map mapChkKeys = new HashMap();
            mapChkKeys.put("address","");
            mapChkKeys.put("txId",txId);
            mapChkKeys.put("objectid",tempObjId);

            String tempVer = gdCF.getObjectLatestVer(tempObjId);
            assertNotEquals("0",tempVer);
            mapChkKeys.put("version",tempVer);
            mapChkKeys.put("contentType",regType);
            mapChkKeys.put("subProdSubType","");
            mapChkKeys.put("operationType","update");

            Map mapKeyUpdate = new HashMap();
            mapKeyUpdate.put("register_subject_account_ref","SH" + mapAccAddr.get(tempAddr));

            String json = JSON.toJSONString(mapKeyUpdate);
            mapChkKeys.put("updateMap",json);

            assertEquals("检查数据",true,gdCF.bCheckJGParams(mapChkKeys));
        }
        log.info("检查场内转板存证产品格式化及信息内容与传入一致");
        String oldProdVer = gdCF.getObjectLatestVer(oldEquityCode);
        String newEqProdVer = gdCF.getObjectLatestVer(newEquityCode);

        assertEquals("0",newEqProdVer);

        //检查历史
        Map getOldProInfo = gdCF.constructJGDataFromStr(conJGFileName(oldEquityCode, oldProdVer), prodType, "1");
//        oldEqProd.put("product_code",oldEquityCode);
        oldEqProd.put("content",gdCF.constructContentTreeMap(prodType, oldEquityCode, oldProdVer, "delete", String.valueOf(ts8)));
        log.info("检查转板前产品存证信息内容与传入一致\n" + oldEqProd.toString() + "\n" + getOldProInfo.toString());
        bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(oldEqProd, prodType)), replaceCertain(getOldProInfo.toString()));
        assertEquals("检查场内转板旧产品是否一致" ,true,bSame);

        product_issuer_subject_ref = gdCompanyID;
//        newEqProd.put("product_code",newEquityCode);//////////
        //检查新产品
        Map getNewProInfo = gdCF.constructJGDataFromStr(conJGFileName(newEquityCode, newEqProdVer), prodType, "1");
        newEqProd.put("content",gdCF.constructContentTreeMap(prodType, newEquityCode, newEqProdVer, "create", String.valueOf(ts3)));
        log.info("检查转板后产品存证信息内容与传入一致\n" + newEqProd.toString() + "\n" + getNewProInfo.toString());
        bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(newEqProd, prodType)), replaceCertain(getNewProInfo.toString()));
        assertEquals("检查场内转板新产品是否一致" ,true,bSame);

        log.info("================================检查存证数据格式化《结束》================================");


        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,4300,0,0,mapShareENCN().get("0"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount1,500,1,0,mapShareENCN().get("1"),respShareList);
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

        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo1 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount1 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":4300,\"lockAmount\":0}"));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":500,\"lockAmount\":0}"));

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

        assertEquals("校验对象1性质0","8",gdCF.getObjectLatestVer(objID1));
        assertEquals("校验对象1性质1","1",gdCF.getObjectLatestVer(objID11));
        assertEquals("校验对象2","3",gdCF.getObjectLatestVer(objID2));
        assertEquals("校验对象3","3",gdCF.getObjectLatestVer(objID3));
        assertEquals("校验对象4","3",gdCF.getObjectLatestVer(objID4));
        assertEquals("校验对象5","1",gdCF.getObjectLatestVer(objID5));
    }


    @Test
    public void TC205_accountDestroy() throws Exception {
        log.info("销户前查询个人主体信息");
        String cltNo = gdAccClientNo10;
        int gdClient = Integer.parseInt(gdCF.getObjectLatestVer(cltNo));//获取当前开户主体最新版本信息
        String name = "销户代理人姓名2";
        String number = "销户代理人电话2";
        String response= gd.GDAccountDestroy(gdContractAddress,cltNo,date4,getListFileObj(),date4,getListFileObj(),
                name,number);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        String txDetail = store.GetTxDetail(txId);
        assertEquals("200", JSONObject.fromObject(txDetail).getString("state"));

        //检查各个查询对象返回信息中不包含敏感词
        assertEquals("不包含敏感词",true,gdCF.chkSensitiveWord(txDetail,accType));


        log.info("================================检查存证数据格式化《开始》================================");
        //获取监管数据存证
        String SHObjId = "SH" + cltNo;
        String fundObjId = "fund" + cltNo;

        String shAccVer = gdCF.getObjectLatestVer(SHObjId);
        String fundAccVer = gdCF.getObjectLatestVer(fundObjId);
        //获取链上mini url的存证信息 并检查是否包含uri信息
        String shAccfileName = conJGFileName(SHObjId,shAccVer);
        String fundAccfileName = conJGFileName(fundObjId,fundAccVer);

        Map uriInfo = gdCF.getJGURIStoreHash(txId,shAccfileName,1);
        String chkSHAccURI = shAccfileName;
        String chkFundAccURI = fundAccfileName;
        log.info(uriInfo.get("storeData").toString());
        assertEquals(true,uriInfo.get("storeData").toString().contains(chkSHAccURI));
        assertEquals(true,uriInfo.get("storeData").toString().contains(chkFundAccURI));
        assertEquals(true,gdCF.bContainJGFlag(uriInfo.get("storeData").toString()));//确认meta信息包含监管关键字


        //直接从minio上获取报送数据文件信息
        Map getFundAccInfo = gdCF.constructJGDataFromStr(fundAccfileName,accType,"2");
        Map getSHAccInfo = gdCF.constructJGDataFromStr(shAccfileName,accType,"1");


        Map accFund = gdBF.init02FundAccountInfo();
        Map accSH = gdBF.init02ShareholderAccountInfo();

        accFund.put("account_subject_ref",cltNo);
        accSH.put("account_subject_ref",cltNo);

        accFund.put("account_object_id",fundObjId);
        accSH.put("account_object_id",SHObjId);

        accFund.put("account_status",2);//2是销户
        accSH.put("account_status",2);//2是销户

        accFund.put("account_associated_account_ref",SHObjId);

        accFund.put("account_closing_agent_name",name);
        accFund.put("account_closing_agent_contact_number",number);
        accFund.put("account_closing_date",date4);

        accSH.put("account_closing_agent_name",name);
        accSH.put("account_closing_agent_contact_number",number);
        accSH.put("account_closing_date",date4);

        accFund.put("content",gdCF.constructContentTreeMap(accType,fundObjId,fundAccVer,"delete",String.valueOf(ts8)));
        accSH.put("content",gdCF.constructContentTreeMap(accType,SHObjId,shAccVer,"delete",String.valueOf(ts8)));

        //确认投资者主体对象标识版本不变
        assertEquals(String.valueOf(gdClient),gdCF.getObjectLatestVer(cltNo));

        //账户的如下字段默认引用的是开户主体的对象标识
        account_subject_ref = cltNo;

        log.info("检查股权账户存证信息内容与传入一致\n" + accSH.toString() + "\n" + getSHAccInfo.toString());
        bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(accSH,accType)),replaceCertain(getSHAccInfo.toString()));
        assertEquals("检查销户后股权账户数据",true,bSame);
        account_associated_account_ref = SHObjId;

        log.info("检查资金账户存证信息内容与传入一致\n" + accFund.toString() + "\n" + getFundAccInfo.toString());
        bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(accFund,accType)),replaceCertain(getFundAccInfo.toString()));
        assertEquals("检查销户后股权账户数据",true,bSame);
    }


    @Test
    public void TCN16_balanceCount() throws Exception {
        settlement_product_ref = gdEquityCode;
//        Map enSubInfo = gdBF.init01EnterpriseSubjectInfo();
//        Map prodInfo = gdBF.init03EquityProductInfo();

        int gdCpmIdOldVer = Integer.parseInt(gdCF.getObjectLatestVer(gdCompanyID));//获取当前挂牌主体最新版本信息
//        String response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,1000,enSubInfo,
//                prodInfo,null,null);
//        assertEquals("200", net.sf.json.JSONObject.fromObject(response).getString("state"));
//        sleepAndSaveInfo(2000);

//        //开户
//        if(gdCF.getObjectLatestVer(gdAccClientNo1).equals("-1")){
//            String cltNo = "settleAcc" + Random(6);
//            gdAccClientNo1 = cltNo;
//            //执行开户
//            Map mapCreate = gdBF.gdCreateAccParam(cltNo);
//            String txId = mapCreate.get("txId").toString();
//            commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//            String txDetail = store.GetTxDetail(txId);
//            assertEquals("200", net.sf.json.JSONObject.fromObject(txDetail).getString("state"));
//            sleepAndSaveInfo(2000);
//        }

        Map testSettleInfo = gdBF.init06SettleInfo();
        testSettleInfo.put("settlement_in_account_object_ref","SH" + gdAccClientNo1);
        testSettleInfo.put("settlement_out_account_object_ref","SH" + gdAccClientNo5);
        String response= gd.GDCapitalSettlement(testSettleInfo);
        JSONObject jsonObject= JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        String txDetail = store.GetTxDetail(txId);
        assertEquals("200", JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));


        //设置各个主体版本变量
        String objPrefix = "uri";

        //获取（从交易详情中）链上mini url的存证信息并检查是否包含uri信息 通过前缀信息获取信披对象id
        String storeData = com.alibaba.fastjson.JSONObject.parseObject(txDetail).getJSONObject(
                "data").getJSONObject("store").getString("storeData").toString();
        log.info(storeData);
        com.alibaba.fastjson.JSONObject objURI = com.alibaba.fastjson.JSONObject.parseObject(
                com.alibaba.fastjson.JSONObject.parseArray(storeData).get(0).toString());
        String chkObjURI = objPrefix;
        assertEquals(true,storeData.contains(chkObjURI));
        assertEquals(true,gdCF.bContainJGFlag(storeData));//确认meta信息包含监管关键字

        String objVerTemp =  objURI.getString("uri").trim();

        String newSettleObjId = "";
        newSettleObjId = objVerTemp.substring(0,objVerTemp.lastIndexOf("/"));

        String newDisObjIdVer = objVerTemp.substring(objVerTemp.lastIndexOf("/") + 1);//gdCF.getObjectLatestVer(newDisObjId);
        log.info(objVerTemp + " " + newDisObjIdVer);

        String objfileName = conJGFileName(newSettleObjId,newDisObjIdVer);


        //直接从minio上通过对象标识+版本号的方式获取指定对象文件
        Map getSettleInfo = gdCF.constructJGDataFromStr(objfileName,settleType,"");


        //填充header content字段
        testSettleInfo.put("content",gdCF.constructContentTreeMap(settleType,newSettleObjId,newDisObjIdVer,"create",String.valueOf(ts6)));
        testSettleInfo.put("settlement_transaction_ref","null");//默认没有携带交易报告对象引用信息
        settlement_in_account_object_ref = "SH" + gdAccClientNo1;
        settlement_out_account_object_ref = "SH" + gdAccClientNo5;
        log.info("检查主体存证信息内容与传入一致\n" + testSettleInfo.toString() + "\n" + getSettleInfo.toString());
        assertEquals(replaceCertain(gdCF.matchRefMapCertVer2(testSettleInfo,settleType)),replaceCertain(getSettleInfo.toString()));

    }
}
