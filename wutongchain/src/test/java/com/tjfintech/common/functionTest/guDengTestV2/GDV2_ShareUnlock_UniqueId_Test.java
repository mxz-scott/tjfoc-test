package com.tjfintech.common.functionTest.guDengTestV2;

import com.alibaba.fastjson.JSON;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.GuDeng;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.MinIOOperation;
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

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class GDV2_ShareUnlock_UniqueId_Test {

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
    MinIOOperation minio = new MinIOOperation();

    @Rule
    public TestName tm = new TestName();
    /***
     * 测试说明
     * 转让 会转给新的账户 因此转让会使得总股东数增加
     * 股权性质变更 变更部分
     * 增发 增发给发行时的所有账户  增发不会增加总股东数
     * 冻结/解除冻结  部分冻结 解除全部冻结
     * 回收 没有一个账户为回收全部  回收不会减少总股东数
     * 场内转板
     * @throws Exception
     */

    @BeforeClass
    public static void Before()throws Exception{

        GDBeforeCondition gdBefore = new GDBeforeCondition();
        gdBefore.gdCreateAccout();
//        gdBefore.initRegulationData();
        equityProductInfo = gdBefore.init03EquityProductInfo();
        bondProductInfo = null;
        fundProductInfo = null;

    }

    @Before
    public void resetVar() throws Exception{
        busUUID = "";

        gdEquityCode = "prodEq" + Random(12);
        gdCompanyID = "P1Re" + Random(8);
        register_product_ref = gdEquityCode;
        roll_register_product_ref = gdEquityCode;
        transaction_custody_product_ref = gdEquityCode;

        bizNoTest = "UUID" + Random(12);
        register_event_type = 1;//非交易登记
        tempsubject_investor_qualification_certifier_ref =subject_investor_qualification_certifier_ref;
        tempregister_transaction_ref = register_transaction_ref;

        //发行
        log.info(registerInfo.toString());
        mapAddrRegObjId.clear(); //先清空map 这样后面map中拿到的就是此次发行的登记对象标识

        List<Map> shareList = gdConstructShareList(gdAccount1,issueAmount,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,issueAmount,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,issueAmount,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,issueAmount,0,shareList3);

        String response= uf.shareIssue(gdEquityCode,shareList4,false);
        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        String txDetail = store.GetTxDetail(txId);
        assertEquals("200", JSONObject.fromObject(txDetail).getString("state"));

        //冻结
        log.info("发行主体版本  " + gdCF.getObjectLatestVer(gdCompanyID));
        sleepAndSaveInfo(2000);
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
        String tempObjId = mapAccAddr.get(gdAccount1).toString() + Random(5);
        regInfo.put("register_registration_object_id",tempObjId);
        regInfo.put("register_subject_account_ref","SH" + mapAccAddr.get(address));

        response= gd.GDShareLock(bizNo,address,eqCode,lockAmount,shareProperty,reason,cutoffDate,regInfo);
        txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        txDetail = store.GetTxDetail(txId);
        assertEquals("200", JSONObject.fromObject(txDetail).getString("state"));

    }

    @After
    public void calJGDataAfterTx()throws Exception{
        busUUID = "";
        testCurMethodName = tm.getMethodName();
        GDUnitFunc uf = new GDUnitFunc();
        int endHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");
//        uf.checkJGHeaderOpVer(blockHeight,endHeight);

        subject_investor_qualification_certifier_ref = tempsubject_investor_qualification_certifier_ref;
        register_transaction_ref = tempregister_transaction_ref;
    }

    @Test
    public void shareUnlock_SFNS() throws Exception {
        //解冻
        log.info("发行主体版本  " + gdCF.getObjectLatestVer(gdCompanyID));
        sleepAndSaveInfo(3000);

        String bizNo = bizNoTest;
        String eqCode = gdEquityCode;
        long amount = 500;

        register_product_ref = gdEquityCode;
        register_subject_account_ref = "SH" + gdAccClientNo1;
        //登记数据
        Map regInfo = gdBF.init05RegInfo();
        String tempObjId = mapAccAddr.get(gdAccount1).toString() + Random(5);
        regInfo.put("register_registration_object_id",tempObjId);
        regInfo.put("register_subject_account_ref","SH" + gdAccClientNo1);
        regInfo.put("register_time",""); //设置register_time为空 使得发行成功 报送失败

        sleepAndSaveInfo(3000);
        int beforeBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");

        String response= gd.GDShareUnlock(bizNo,eqCode,amount,regInfo);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("监管数据校验失败",true,JSONObject.fromObject(response).getString("message").contains("数据格式校验失败"));

        busUUID = tempUUID;

        sleepAndSaveInfo(4000);
        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,5000,0,0,mapShareENCN().get("0"),respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2,5000,0,0,mapShareENCN().get("0"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3,5000,0,0,mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4,5000,0,0,mapShareENCN().get("0"), respShareList3);

        int afterBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");
        assertEquals("区块高度不增加，无存证上链",beforeBlockHeight,afterBlockHeight);

//        String blockDetail = store.GetBlockByHeight(afterBlockHeight);
//        assertEquals("区块仅有一笔交易",1,JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").size());
//        //确认交易是解冻交易
//        String getTXDetails = store.GetTxDetail(JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").getString(0));
//        JSONObject jsonObject1 = JSONObject.fromObject(getTXDetails).getJSONObject("data");
//        assertEquals("2",jsonObject1.getJSONObject("header").getString("version"));
//        assertEquals("3",jsonObject1.getJSONObject("header").getString("type"));
//        assertEquals("42",jsonObject1.getJSONObject("header").getString("subType"));
//
//        JSONObject jsonObjectWVM = jsonObject1.getJSONObject("wvm").getJSONObject("wvmContractTx");
//        assertEquals("ShareUnlock",jsonObjectWVM.getJSONObject("arg").getString("method"));
//        assertEquals(gdContractAddress,jsonObjectWVM.getString("name"));
//
//        //4.判断股东列表有变更
//        String queryList = gd.GDGetEnterpriseShareInfo(gdEquityCode);
//        assertEquals("200",JSONObject.fromObject(queryList).getString("state"));
//        JSONArray dataShareListNow = JSONObject.fromObject(queryList).getJSONArray("data");
//        assertEquals(respShareList4.size(),dataShareListNow.size());
//        List<Map> getShareListNow = getShareListFromQueryNoZeroAcc(dataShareListNow);
//        assertEquals(respShareList4.size(),getShareListNow.size());
//        assertEquals(true,respShareList4.containsAll(getShareListNow) && getShareListNow.containsAll(respShareList4));

        //5.判断uuid是否存在合约中 方法待确认

        //6.判断所有登记对象ID是否存在OSS中
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,tempObjId + "/0",""));
//        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,regObjId2 + "/0",""));

//        //7.判断数据库中是否存储登记对象ID
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId1));
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId2));
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId3));
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId4));

        //重复执行 使用上次的uuid
        regInfo.put("register_time",time2); //设置register_time正常 使得报送可以成功
        response= gd.GDShareUnlock(bizNo,eqCode,amount,regInfo);

        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        String txDetail = store.GetTxDetail(txId);
        assertEquals("200", JSONObject.fromObject(txDetail).getString("state"));

        busUUID = "";

        //检查各个查询对象返回信息中不包含敏感词
        assertEquals("不包含登记数据敏感词",true,gdCF.chkSensitiveWord(txDetail,regType));

        log.info("================================检查存证数据格式化《开始》================================");

        Map uriInfo = gdCF.getJGURIStoreHash(txId,conJGFileName(tempObjId,"0"),1);

        log.info("检查股权性质变更存证登记格式化及信息内容与传入一致");
        String regVer = "0" ;//gdCF.getObjectLatestVer(account_subject_ref);

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
        regInfo.put("content",gdCF.constructContentTreeMap(regType,tempObjId,regVer,"create",String.valueOf(ts5)));
        log.info("检查登记存证信息内容与传入一致\n" + regInfo.toString() + "\n" + getRegInfo1.toString());
        bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(regInfo,regType)),replaceCertain(getRegInfo1.toString()));
        assertEquals("1检查数据是否一致" ,true,bSame);

        log.info("================================检查存证数据格式化《结束》================================");

        respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,5000,0,0,mapShareENCN().get("0"),respShareList);
        respShareList2 = gdConstructQueryShareList(gdAccount2,5000,0,0,mapShareENCN().get("0"), respShareList);
        respShareList3 = gdConstructQueryShareList(gdAccount3,5000,0,0,mapShareENCN().get("0"), respShareList2);
        respShareList4 = gdConstructQueryShareList(gdAccount4,5000,0,0,mapShareENCN().get("0"), respShareList3);

        if(!bCheckList) return;

        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");

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

    }


    @Test
    public void shareUnlock_SFNFNS() throws Exception {
        //解冻
        log.info("发行主体版本  " + gdCF.getObjectLatestVer(gdCompanyID));
        sleepAndSaveInfo(3000);

        String bizNo = bizNoTest;
        String eqCode = gdEquityCode;
        long amount = 500;

        register_product_ref = gdEquityCode;
        register_subject_account_ref = "SH" + gdAccClientNo1;
        //登记数据
        Map regInfo = gdBF.init05RegInfo();
        String tempObjId = mapAccAddr.get(gdAccount1).toString() + Random(5);
        regInfo.put("register_registration_object_id",tempObjId);
        regInfo.put("register_subject_account_ref","SH" + gdAccClientNo1);
        regInfo.put("register_time",""); //设置register_time为空 使得发行成功 报送失败

        sleepAndSaveInfo(3000);
        int beforeBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");

        String response= gd.GDShareUnlock(bizNo,eqCode,amount,regInfo);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("监管数据校验失败",true,JSONObject.fromObject(response).getString("message").contains("数据格式校验失败"));

        busUUID = tempUUID;

        sleepAndSaveInfo(4000);
        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,5000,0,0,mapShareENCN().get("0"),respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2,5000,0,0,mapShareENCN().get("0"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3,5000,0,0,mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4,5000,0,0,mapShareENCN().get("0"), respShareList3);

        int afterBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");
        assertEquals("区块高度不增加，无存证上链",beforeBlockHeight,afterBlockHeight);

//        String blockDetail = store.GetBlockByHeight(afterBlockHeight);
//        assertEquals("区块仅有一笔交易",1,JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").size());
//        //确认交易是解冻交易
//        String getTXDetails = store.GetTxDetail(JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").getString(0));
//        JSONObject jsonObject1 = JSONObject.fromObject(getTXDetails).getJSONObject("data");
//        assertEquals("2",jsonObject1.getJSONObject("header").getString("version"));
//        assertEquals("3",jsonObject1.getJSONObject("header").getString("type"));
//        assertEquals("42",jsonObject1.getJSONObject("header").getString("subType"));
//
//        JSONObject jsonObjectWVM = jsonObject1.getJSONObject("wvm").getJSONObject("wvmContractTx");
//        assertEquals("ShareUnlock",jsonObjectWVM.getJSONObject("arg").getString("method"));
//        assertEquals(gdContractAddress,jsonObjectWVM.getString("name"));
//
//        //4.判断股东列表有变更
//        String queryList = gd.GDGetEnterpriseShareInfo(gdEquityCode);
//        assertEquals("200",JSONObject.fromObject(queryList).getString("state"));
//        JSONArray dataShareListNow = JSONObject.fromObject(queryList).getJSONArray("data");
//        assertEquals(respShareList4.size(),dataShareListNow.size());
//        List<Map> getShareListNow = getShareListFromQueryNoZeroAcc(dataShareListNow);
//        assertEquals(respShareList4.size(),getShareListNow.size());
//        assertEquals(true,respShareList4.containsAll(getShareListNow) && getShareListNow.containsAll(respShareList4));

        //5.判断uuid是否存在合约中 方法待确认

        //6.判断所有登记对象ID是否存在OSS中
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,tempObjId + "/0",""));
//        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,regObjId2 + "/0",""));

//        //7.判断数据库中是否存储登记对象ID
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId1));
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId2));
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId3));
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId4));


        //重复执行 使用上次的uuid
        regInfo.put("register_time","2021-03-25 10:00:00"); //设置register_time错误格式 使得报送仍失败
        response = gd.GDShareUnlock(bizNo,eqCode,amount,regInfo);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        sleepAndSaveInfo(4000);
        afterBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");
        assertEquals("区块高度无增加，无存证上链",beforeBlockHeight,afterBlockHeight);
        //6.判断所有登记对象ID是否存在OSS中
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,tempObjId + "/0",""));


        //重复执行 使用上次的uuid
        regInfo.put("register_time",time2); //设置register_time正常 使得报送可以成功
        response= gd.GDShareUnlock(bizNo,eqCode,amount,regInfo);

        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        String txDetail = store.GetTxDetail(txId);
        assertEquals("200", JSONObject.fromObject(txDetail).getString("state"));

        busUUID = "";

        //检查各个查询对象返回信息中不包含敏感词
        assertEquals("不包含登记数据敏感词",true,gdCF.chkSensitiveWord(txDetail,regType));

        log.info("================================检查存证数据格式化《开始》================================");

        Map uriInfo = gdCF.getJGURIStoreHash(txId,conJGFileName(tempObjId,"0"),1);

        log.info("检查股权性质变更存证登记格式化及信息内容与传入一致");
        String regVer = "0" ;//gdCF.getObjectLatestVer(account_subject_ref);

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
        regInfo.put("content",gdCF.constructContentTreeMap(regType,tempObjId,regVer,"create",String.valueOf(ts5)));
        log.info("检查登记存证信息内容与传入一致\n" + regInfo.toString() + "\n" + getRegInfo1.toString());
        bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(regInfo,regType)),replaceCertain(getRegInfo1.toString()));
        assertEquals("1检查数据是否一致" ,true,bSame);

        log.info("================================检查存证数据格式化《结束》================================");

        respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,5000,0,0,mapShareENCN().get("0"),respShareList);
        respShareList2 = gdConstructQueryShareList(gdAccount2,5000,0,0,mapShareENCN().get("0"), respShareList);
        respShareList3 = gdConstructQueryShareList(gdAccount3,5000,0,0,mapShareENCN().get("0"), respShareList2);
        respShareList4 = gdConstructQueryShareList(gdAccount4,5000,0,0,mapShareENCN().get("0"), respShareList3);

        if(!bCheckList) return;

        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");

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

    }

    @Test
    public void shareUnlock_SFNS02() throws Exception {
        //解冻
        log.info("发行主体版本  " + gdCF.getObjectLatestVer(gdCompanyID));
        sleepAndSaveInfo(3000);

        String bizNo = bizNoTest;
        String eqCode = gdEquityCode;
        long amount = 500;

        register_product_ref = gdEquityCode;
        register_subject_account_ref = "SH" + gdAccClientNo1;
        //登记数据
        Map regInfo = gdBF.init05RegInfo();
        String tempObjId = mapAccAddr.get(gdAccount1).toString() + Random(120);
        regInfo.put("register_registration_object_id",tempObjId);
        regInfo.put("register_subject_account_ref","SH" + gdAccClientNo1);

        sleepAndSaveInfo(3000);
        int beforeBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");

        String response= gd.GDShareUnlock(bizNo,eqCode,amount,regInfo);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("监管数据校验失败",true,JSONObject.fromObject(response).getString("message").contains("Data too long for column 'object_id' at row 1"));

        busUUID = tempUUID;

        sleepAndSaveInfo(4000);
        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,5000,0,0,mapShareENCN().get("0"),respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2,5000,0,0,mapShareENCN().get("0"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3,5000,0,0,mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4,5000,0,0,mapShareENCN().get("0"), respShareList3);

        int afterBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");
        assertEquals("区块高度不增加，无存证上链",beforeBlockHeight,afterBlockHeight);

//        String blockDetail = store.GetBlockByHeight(afterBlockHeight);
//        assertEquals("区块仅有一笔交易",1,JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").size());
//        //确认交易是解冻交易
//        String getTXDetails = store.GetTxDetail(JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").getString(0));
//        JSONObject jsonObject1 = JSONObject.fromObject(getTXDetails).getJSONObject("data");
//        assertEquals("2",jsonObject1.getJSONObject("header").getString("version"));
//        assertEquals("3",jsonObject1.getJSONObject("header").getString("type"));
//        assertEquals("42",jsonObject1.getJSONObject("header").getString("subType"));
//
//        JSONObject jsonObjectWVM = jsonObject1.getJSONObject("wvm").getJSONObject("wvmContractTx");
//        assertEquals("ShareUnlock",jsonObjectWVM.getJSONObject("arg").getString("method"));
//        assertEquals(gdContractAddress,jsonObjectWVM.getString("name"));
//
//        //4.判断股东列表有变更
//        String queryList = gd.GDGetEnterpriseShareInfo(gdEquityCode);
//        assertEquals("200",JSONObject.fromObject(queryList).getString("state"));
//        JSONArray dataShareListNow = JSONObject.fromObject(queryList).getJSONArray("data");
//        assertEquals(respShareList4.size(),dataShareListNow.size());
//        List<Map> getShareListNow = getShareListFromQueryNoZeroAcc(dataShareListNow);
//        assertEquals(respShareList4.size(),getShareListNow.size());
//        assertEquals(true,respShareList4.containsAll(getShareListNow) && getShareListNow.containsAll(respShareList4));

        //5.判断uuid是否存在合约中 方法待确认

        //6.判断所有登记对象ID是否存在OSS中
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,tempObjId + "/0",""));
//        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,regObjId2 + "/0",""));

//        //7.判断数据库中是否存储登记对象ID
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId1));
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId2));
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId3));
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId4));

        //重复执行 使用上次的uuid
        tempObjId = "5" + mapAccAddr.get(gdAccount1) + Random(6); //设置id长度正常 使得报送可以成功
        regInfo.put("register_registration_object_id",tempObjId);
        response = gd.GDShareUnlock(bizNo,eqCode,amount,regInfo);

        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        String txDetail = store.GetTxDetail(txId);
        assertEquals("200", JSONObject.fromObject(txDetail).getString("state"));

        busUUID = "";

        //检查各个查询对象返回信息中不包含敏感词
        assertEquals("不包含登记数据敏感词",true,gdCF.chkSensitiveWord(txDetail,regType));

        log.info("================================检查存证数据格式化《开始》================================");

        Map uriInfo = gdCF.getJGURIStoreHash(txId,conJGFileName(tempObjId,"0"),1);

        log.info("检查股权性质变更存证登记格式化及信息内容与传入一致");
        String regVer = "0" ;//gdCF.getObjectLatestVer(account_subject_ref);

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
        regInfo.put("content",gdCF.constructContentTreeMap(regType,tempObjId,regVer,"create",String.valueOf(ts5)));
        log.info("检查登记存证信息内容与传入一致\n" + regInfo.toString() + "\n" + getRegInfo1.toString());
        bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(regInfo,regType)),replaceCertain(getRegInfo1.toString()));
        assertEquals("1检查数据是否一致" ,true,bSame);

        log.info("================================检查存证数据格式化《结束》================================");

        respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,5000,0,0,mapShareENCN().get("0"),respShareList);
        respShareList2 = gdConstructQueryShareList(gdAccount2,5000,0,0,mapShareENCN().get("0"), respShareList);
        respShareList3 = gdConstructQueryShareList(gdAccount3,5000,0,0,mapShareENCN().get("0"), respShareList2);
        respShareList4 = gdConstructQueryShareList(gdAccount4,5000,0,0,mapShareENCN().get("0"), respShareList3);

        if(!bCheckList) return;

        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");

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

    }


    @Test
    public void shareUnLockSameUniqueIdSameRequest() throws Exception {
        //解冻
        log.info("发行主体版本  " + gdCF.getObjectLatestVer(gdCompanyID));
        sleepAndSaveInfo(3000);

        String bizNo = bizNoTest;
        String eqCode = gdEquityCode;
        long amount = 500;

        register_product_ref = gdEquityCode;
        register_subject_account_ref = "SH" + gdAccClientNo1;
        //登记数据
        Map regInfo = gdBF.init05RegInfo();
        String tempObjId = mapAccAddr.get(gdAccount1).toString() + Random(5);
        regInfo.put("register_registration_object_id",tempObjId);
        regInfo.put("register_subject_account_ref","SH" + gdAccClientNo1);

        sleepAndSaveInfo(3000);
        int beforeBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");

        String response= gd.GDShareUnlock(bizNo,eqCode,amount,regInfo);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
        busUUID = tempUUID;

        sleepAndSaveInfo(4000);
        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,5000,0,0,mapShareENCN().get("0"),respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2,5000,0,0,mapShareENCN().get("0"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3,5000,0,0,mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4,5000,0,0,mapShareENCN().get("0"), respShareList3);

        int afterBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");
        assertEquals("区块高度仅增加2，存证上链 合约执行",beforeBlockHeight + 2,afterBlockHeight);

        String blockDetail = store.GetBlockByHeight(afterBlockHeight - 1);
        assertEquals("区块仅有一笔交易",1,JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").size());
        //确认交易是解冻交易
        String getTXDetails = store.GetTxDetail(JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").getString(0));
        JSONObject jsonObject1 = JSONObject.fromObject(getTXDetails).getJSONObject("data");
        assertEquals("2",jsonObject1.getJSONObject("header").getString("version"));
        assertEquals("3",jsonObject1.getJSONObject("header").getString("type"));
        assertEquals("42",jsonObject1.getJSONObject("header").getString("subType"));

        JSONObject jsonObjectWVM = jsonObject1.getJSONObject("wvm").getJSONObject("wvmContractTx");
        assertEquals("ShareUnlock",jsonObjectWVM.getJSONObject("arg").getString("method"));
        assertEquals(gdContractAddress,jsonObjectWVM.getString("name"));

        //4.判断股东列表有变更
        String queryList = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(queryList).getString("state"));
        JSONArray dataShareListNow = JSONObject.fromObject(queryList).getJSONArray("data");
        assertEquals(respShareList4.size(),dataShareListNow.size());
        List<Map> getShareListNow = getShareListFromQueryNoZeroAcc(dataShareListNow);
        assertEquals(respShareList4.size(),getShareListNow.size());
        assertEquals(true,respShareList4.containsAll(getShareListNow) && getShareListNow.containsAll(respShareList4));

        //5.判断uuid是否存在合约中 方法待确认

        //6.判断所有登记对象ID是否存在OSS中
        assertEquals(true,minio.getFileFromMinIO(minIOEP,jgBucket,tempObjId + "/0","").contains("区域性股权市场跨链业务数据模型"));
//        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,regObjId2 + "/0",""));



        busUUID = tempUUID;
        response= gd.GDShareUnlock(bizNo,eqCode,amount,regInfo);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,JSONObject.fromObject(response).getString("message").contains("请检查此对象标识是否已经存在"));

        sleepAndSaveInfo(3000);
        int afterBlockHeight2 = JSONObject.fromObject(store.GetHeight()).getInt("data");
        assertEquals("区块高度无增加，无存证上链",afterBlockHeight,afterBlockHeight2);
    }

    @Test
    public void shareUnLockSameUniqueIdDiffRequest() throws Exception {
        //解冻
        log.info("发行主体版本  " + gdCF.getObjectLatestVer(gdCompanyID));
        sleepAndSaveInfo(3000);

        String bizNo = bizNoTest;
        String eqCode = gdEquityCode;
        long amount = 300;

        register_product_ref = gdEquityCode;
        register_subject_account_ref = "SH" + gdAccClientNo1;
        //登记数据
        Map regInfo = gdBF.init05RegInfo();
        String tempObjId = mapAccAddr.get(gdAccount1).toString() + Random(5);
        regInfo.put("register_registration_object_id",tempObjId);
        regInfo.put("register_subject_account_ref","SH" + gdAccClientNo1);

        sleepAndSaveInfo(3000);
        int beforeBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");

        String response= gd.GDShareUnlock(bizNo,eqCode,amount,regInfo);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
        busUUID = tempUUID;

        sleepAndSaveInfo(4000);
        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,5000,0,200,mapShareENCN().get("0"),respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2,5000,0,0,mapShareENCN().get("0"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3,5000,0,0,mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4,5000,0,0,mapShareENCN().get("0"), respShareList3);

        int afterBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");
        assertEquals("区块高度仅增加2，存证上链 合约执行",beforeBlockHeight + 2,afterBlockHeight);

        String blockDetail = store.GetBlockByHeight(afterBlockHeight - 1);
        assertEquals("区块仅有一笔交易",1,JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").size());
        //确认交易是解冻交易
        String getTXDetails = store.GetTxDetail(JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").getString(0));
        JSONObject jsonObject1 = JSONObject.fromObject(getTXDetails).getJSONObject("data");
        assertEquals("2",jsonObject1.getJSONObject("header").getString("version"));
        assertEquals("3",jsonObject1.getJSONObject("header").getString("type"));
        assertEquals("42",jsonObject1.getJSONObject("header").getString("subType"));

        JSONObject jsonObjectWVM = jsonObject1.getJSONObject("wvm").getJSONObject("wvmContractTx");
        assertEquals("ShareUnlock",jsonObjectWVM.getJSONObject("arg").getString("method"));
        assertEquals(gdContractAddress,jsonObjectWVM.getString("name"));

        //4.判断股东列表有变更
        String queryList = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(queryList).getString("state"));
        JSONArray dataShareListNow = JSONObject.fromObject(queryList).getJSONArray("data");
        assertEquals(respShareList4.size(),dataShareListNow.size());
        List<Map> getShareListNow = getShareListFromQueryNoZeroAcc(dataShareListNow);
        assertEquals(respShareList4.size(),getShareListNow.size());
        assertEquals(true,respShareList4.containsAll(getShareListNow) && getShareListNow.containsAll(respShareList4));

        //5.判断uuid是否存在合约中 方法待确认

        //6.判断所有登记对象ID是否存在OSS中
        assertEquals(true,minio.getFileFromMinIO(minIOEP,jgBucket,tempObjId + "/0","").contains("区域性股权市场跨链业务数据模型"));
//        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,regObjId2 + "/0",""));


        //重复uuid 但不同的业务
        busUUID = tempUUID;
        tempObjId = mapAccAddr.get(gdAccount1).toString() + Random(5);
        regInfo.put("register_registration_object_id",tempObjId);
        response= gd.GDShareUnlock(bizNo,eqCode,200,regInfo);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));

        //合约未执行 但是uri存证上报了
        sleepAndSaveInfo(3000);

        int afterBlockHeight2 = JSONObject.fromObject(store.GetHeight()).getInt("data");
        assertEquals("区块高度增加1，存证上链，合约不执行",afterBlockHeight + 1,afterBlockHeight2);

        blockDetail = store.GetBlockByHeight(afterBlockHeight2);
        assertEquals("区块仅有一笔交易",1,JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").size());
        //确认交易是发行交易
        getTXDetails = store.GetTxDetail(JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").getString(0));
        assertEquals(true,getTXDetails.contains(tempObjId));

        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));


        //查询股东持股情况 无当前股权代码信息
        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        assertEquals(gdAccClientNo1,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));

        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo1 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount1 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":5000,\"lockAmount\":200}"));
    }
}
