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
public class GDV2_ShareChangeProperty_UniqueId_Test {

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
    public void resetVar()throws Exception{
        busUUID = "";
        gdEquityCode = "prodEq" + Random(12);
        gdCompanyID = "P1Re" + Random(8);
        register_product_ref = gdEquityCode;
        roll_register_product_ref = gdEquityCode;
        transaction_custody_product_ref = gdEquityCode;

        register_event_type = 1;//非交易登记
        tempsubject_investor_qualification_certifier_ref =subject_investor_qualification_certifier_ref;
        tempregister_transaction_ref = register_transaction_ref;

        List<Map> shareList = gdConstructShareList(gdAccount1,issueAmount,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,issueAmount,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,issueAmount,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,issueAmount,0,shareList3);

        String response= uf.shareIssue(gdEquityCode,shareList4,false);
        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        String txDetail = store.GetTxDetail(txId);
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

    /***
     * 股权性质变更 部分变更
     * 变更后 未变更部分保持原有股权性质不变 P17对应的需求点
     * @throws Exception
     */
    @Test
    public void shareChangeProperty_SFNS() throws Exception {

        log.info(registerInfo.toString());
        mapAddrRegObjId.clear(); //先清空map 这样后面map中拿到的就是此次发行的登记对象标识

        String eqCode = gdEquityCode;
        String address = gdAccount1;

        log.info("++++++++++++++++++++++++++++++++++++++++++++++++++++++" + gdEquityCode);

        int oldProperty = 0;
        int newProperty = 1;

        Map testReg1 = gdBF.init05RegInfo();
        Map testReg2 = gdBF.init05RegInfo();
        String regObjId1 = mapAccAddr.get(address) + "CProp1" + Random(6);
        String regObjId2 = mapAccAddr.get(address) + "CProp2" + Random(6);

        testReg1.put("register_registration_object_id",regObjId1);
        testReg1.put("register_subject_account_ref","SH" + gdAccClientNo1);

        testReg2.put("register_registration_object_id",regObjId2);
        testReg2.put("register_subject_account_ref","SH" + gdAccClientNo1);

        testReg2.put("register_time",""); //设置register_time为空 使得发行成功 报送失败

        List<Map> regListInfo = new ArrayList<>();
        regListInfo.add(testReg1);
        regListInfo.add(testReg2);

        sleepAndSaveInfo(3000);
        int beforeBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");

        String response = gd.GDShareChangeProperty(gdPlatfromKeyID,address,eqCode,changeAmount,oldProperty,newProperty,regListInfo);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("监管数据校验失败",true,JSONObject.fromObject(response).getString("message").contains("数据格式校验失败"));


        busUUID = tempUUID;

        sleepAndSaveInfo(4000);
        //待校验的股东持股信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,4500,0,0,mapShareENCN().get("0"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount1, 500, 1, 0, mapShareENCN().get("1"), respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2,5000,0,0,mapShareENCN().get("0"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3,5000,0,0,mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4,5000,0,0,mapShareENCN().get("0"), respShareList3);

        int afterBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");
        assertEquals("区块高度不增加，无存证上链",beforeBlockHeight,afterBlockHeight);

//        String blockDetail = store.GetBlockByHeight(afterBlockHeight);
//        assertEquals("区块仅有一笔交易",1,JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").size());
//        //确认交易是股份性质变更交易
//        String getTXDetails = store.GetTxDetail(JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").getString(0));
//        JSONObject jsonObject1 = JSONObject.fromObject(getTXDetails).getJSONObject("data");
//        assertEquals("1",jsonObject1.getJSONObject("header").getString("version"));
//        assertEquals("1",jsonObject1.getJSONObject("header").getString("type"));
//        assertEquals("11",jsonObject1.getJSONObject("header").getString("subType"));
//
//        assertEquals(true,getTXDetails.contains("{\"from\":\"" + address + "\",\"to\":\"" + address
//                + "\",\"tokenType\":\"" + gdEquityCode + "\",\"amount\":\"500\",\"subType\":\"0\",\"newSubType\":\"1\"}"));
//        assertEquals(true,getTXDetails.contains("{\"from\":\"" + address + "\",\"to\":\"" + address
//                + "\",\"tokenType\":\"" + gdEquityCode + "\",\"amount\":\"4500\",\"subType\":\"0\"}"));
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
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,regObjId1 + "/0",""));
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,regObjId2 + "/0",""));

//        //7.判断数据库中是否存储登记对象ID
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId1));
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId2));
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId3));
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId4));

        //重复执行 使用上次的uuid
        testReg2.put("register_time",time2); //设置register_time正常 使得报送可以成功
        response= gd.GDShareChangeProperty(gdPlatfromKeyID,address,eqCode,changeAmount,oldProperty,newProperty,regListInfo);

        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        String txDetail = store.GetTxDetail(txId);
        assertEquals("200", JSONObject.fromObject(txDetail).getString("state"));

        busUUID = "";

        //检查各个查询对象返回信息中不包含敏感词
        assertEquals("不包含敏感词",true,gdCF.chkSensitiveWord(txDetail,regType));

        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200", JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");

        log.info("================================检查存证数据格式化《开始》================================");

        Map uriInfo = gdCF.getJGURIStoreHash(txId,conJGFileName(regObjId1,"0"),1);

        String tempAddr = address;

        log.info("检查股权性质变更存证登记格式化及信息内容与传入一致");
        String regVer = "0" ;//gdCF.getObjectLatestVer(account_subject_ref);

        //获取链上mini url的存证信息 并检查是否包含uri信息 每个登记都是新的 则都是0
        String regfileName1 = conJGFileName(regObjId1,regVer);
        String regfileName2 = conJGFileName(regObjId2,regVer);
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
        regInfoInput.put("content",gdCF.constructContentTreeMap(regType,regObjId1,regVer,"create",String.valueOf(ts5)));
        log.info("检查登记存证信息内容与传入一致\n" + regInfoInput.toString() + "\n" + getRegInfo1.toString());
        bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(regInfoInput,regType)),replaceCertain(getRegInfo1.toString()));
        assertEquals("1检查数据是否一致" ,true,bSame);

        regInfoInput = testReg2;
        regInfoInput.put("content",gdCF.constructContentTreeMap(regType,regObjId2,regVer,"create",String.valueOf(ts5)));
        log.info("检查登记存证信息内容与传入一致\n" + regInfoInput.toString() + "\n" + getRegInfo1.toString());
        bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(regInfoInput,regType)),replaceCertain(getRegInfo2.toString()));
        assertEquals("2检查数据是否一致" ,true,bSame);

        log.info("================================检查存证数据格式化《结束》================================");

        //实际应该持股情况信息
        respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1, 4500, 0, 0, mapShareENCN().get("0"), respShareList);
        respShareList = gdConstructQueryShareList(gdAccount1, 500, 1, 0, mapShareENCN().get("1"), respShareList);
        respShareList2 = gdConstructQueryShareList(gdAccount2, 5000, 0, 0, mapShareENCN().get("0"), respShareList);
        respShareList3 = gdConstructQueryShareList(gdAccount3, 5000, 0, 0, mapShareENCN().get("0"), respShareList2);
        respShareList4 = gdConstructQueryShareList(gdAccount4, 5000, 0, 0, mapShareENCN().get("0"), respShareList3);

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
    }

    @Test
    public void shareChangeProperty_SFNFNS() throws Exception {

        log.info(registerInfo.toString());
        mapAddrRegObjId.clear(); //先清空map 这样后面map中拿到的就是此次发行的登记对象标识

        String eqCode = gdEquityCode;
        String address = gdAccount1;

        log.info("++++++++++++++++++++++++++++++++++++++++++++++++++++++" + gdEquityCode);

        int oldProperty = 0;
        int newProperty = 1;

        Map testReg1 = gdBF.init05RegInfo();
        Map testReg2 = gdBF.init05RegInfo();
        String regObjId1 = mapAccAddr.get(address) + "CProp1" + Random(6);
        String regObjId2 = mapAccAddr.get(address) + "CProp2" + Random(6);

        testReg1.put("register_registration_object_id",regObjId1);
        testReg1.put("register_subject_account_ref","SH" + gdAccClientNo1);

        testReg2.put("register_registration_object_id",regObjId2);
        testReg2.put("register_subject_account_ref","SH" + gdAccClientNo1);

        testReg2.put("register_time",""); //设置register_time为空 使得发行成功 报送失败

        List<Map> regListInfo = new ArrayList<>();
        regListInfo.add(testReg1);
        regListInfo.add(testReg2);

        sleepAndSaveInfo(3000);
        int beforeBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");

        String response = gd.GDShareChangeProperty(gdPlatfromKeyID,address,eqCode,changeAmount,oldProperty,newProperty,regListInfo);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("监管数据校验失败",true,JSONObject.fromObject(response).getString("message").contains("数据格式校验失败"));
        busUUID = tempUUID;

        sleepAndSaveInfo(4000);
        //待校验的股东持股信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,4500,0,0,mapShareENCN().get("0"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount1, 500, 1, 0, mapShareENCN().get("1"), respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2,5000,0,0,mapShareENCN().get("0"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3,5000,0,0,mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4,5000,0,0,mapShareENCN().get("0"), respShareList3);

        int afterBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");
        assertEquals("区块高度不增加，无存证上链",beforeBlockHeight,afterBlockHeight);

//        String blockDetail = store.GetBlockByHeight(afterBlockHeight);
//        assertEquals("区块仅有一笔交易",1,JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").size());
//        //确认交易是股份性质变更交易
//        String getTXDetails = store.GetTxDetail(JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").getString(0));
//        JSONObject jsonObject1 = JSONObject.fromObject(getTXDetails).getJSONObject("data");
//        assertEquals("1",jsonObject1.getJSONObject("header").getString("version"));
//        assertEquals("1",jsonObject1.getJSONObject("header").getString("type"));
//        assertEquals("11",jsonObject1.getJSONObject("header").getString("subType"));
//
//        assertEquals(true,getTXDetails.contains("{\"from\":\"" + address + "\",\"to\":\"" + address
//                + "\",\"tokenType\":\"" + gdEquityCode + "\",\"amount\":\"500\",\"subType\":\"0\",\"newSubType\":\"1\"}"));
//        assertEquals(true,getTXDetails.contains("{\"from\":\"" + address + "\",\"to\":\"" + address
//                + "\",\"tokenType\":\"" + gdEquityCode + "\",\"amount\":\"4500\",\"subType\":\"0\"}"));
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
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,regObjId1 + "/0",""));
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,regObjId2 + "/0",""));

//        //7.判断数据库中是否存储登记对象ID
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId1));
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId2));
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId3));
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId4));



        //重复执行 使用上次的uuid
        testReg2.put("register_time","2021-03-15 12:00:00"); //设置register_time格式错误 使得报送仍失败
        response = gd.GDShareChangeProperty(gdPlatfromKeyID,address,eqCode,changeAmount,oldProperty,newProperty,regListInfo);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        sleepAndSaveInfo(4000);
        afterBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");
        assertEquals("区块高度无增加，无存证上链",beforeBlockHeight,afterBlockHeight);

        //6.判断所有登记对象ID是否存在OSS中
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,regObjId1 + "/0",""));
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,regObjId2 + "/0",""));



        //重复执行 使用上次的uuid
        testReg2.put("register_time",time2); //设置register_time正常 使得报送可以成功
        response= gd.GDShareChangeProperty(gdPlatfromKeyID,address,eqCode,changeAmount,oldProperty,newProperty,regListInfo);

        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        String txDetail = store.GetTxDetail(txId);
        assertEquals("200", JSONObject.fromObject(txDetail).getString("state"));

        busUUID = "";

        //检查各个查询对象返回信息中不包含敏感词
        assertEquals("不包含敏感词",true,gdCF.chkSensitiveWord(txDetail,regType));

        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200", JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");

        log.info("================================检查存证数据格式化《开始》================================");

        Map uriInfo = gdCF.getJGURIStoreHash(txId,conJGFileName(regObjId1,"0"),1);

        String tempAddr = address;

        log.info("检查股权性质变更存证登记格式化及信息内容与传入一致");
        String regVer = "0" ;//gdCF.getObjectLatestVer(account_subject_ref);

        //获取链上mini url的存证信息 并检查是否包含uri信息 每个登记都是新的 则都是0
        String regfileName1 = conJGFileName(regObjId1,regVer);
        String regfileName2 = conJGFileName(regObjId2,regVer);
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
        regInfoInput.put("content",gdCF.constructContentTreeMap(regType,regObjId1,regVer,"create",String.valueOf(ts5)));
        log.info("检查登记存证信息内容与传入一致\n" + regInfoInput.toString() + "\n" + getRegInfo1.toString());
        bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(regInfoInput,regType)),replaceCertain(getRegInfo1.toString()));
        assertEquals("1检查数据是否一致" ,true,bSame);

        regInfoInput = testReg2;
        regInfoInput.put("content",gdCF.constructContentTreeMap(regType,regObjId2,regVer,"create",String.valueOf(ts5)));
        log.info("检查登记存证信息内容与传入一致\n" + regInfoInput.toString() + "\n" + getRegInfo1.toString());
        bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(regInfoInput,regType)),replaceCertain(getRegInfo2.toString()));
        assertEquals("2检查数据是否一致" ,true,bSame);

        log.info("================================检查存证数据格式化《结束》================================");

        //实际应该持股情况信息
        respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1, 4500, 0, 0, mapShareENCN().get("0"), respShareList);
        respShareList = gdConstructQueryShareList(gdAccount1, 500, 1, 0, mapShareENCN().get("1"), respShareList);
        respShareList2 = gdConstructQueryShareList(gdAccount2, 5000, 0, 0, mapShareENCN().get("0"), respShareList);
        respShareList3 = gdConstructQueryShareList(gdAccount3, 5000, 0, 0, mapShareENCN().get("0"), respShareList2);
        respShareList4 = gdConstructQueryShareList(gdAccount4, 5000, 0, 0, mapShareENCN().get("0"), respShareList3);

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
    }

    @Test
    public void shareChangeProperty_SFNS02() throws Exception {

        log.info(registerInfo.toString());
        mapAddrRegObjId.clear(); //先清空map 这样后面map中拿到的就是此次发行的登记对象标识

        String eqCode = gdEquityCode;
        String address = gdAccount1;

        log.info("++++++++++++++++++++++++++++++++++++++++++++++++++++++" + gdEquityCode);

        int oldProperty = 0;
        int newProperty = 1;

        Map testReg1 = gdBF.init05RegInfo();
        Map testReg2 = gdBF.init05RegInfo();
        String regObjId1 = mapAccAddr.get(address) + "CProp1" + Random(120);
        String regObjId2 = mapAccAddr.get(address) + "CProp2" + Random(6);

        testReg1.put("register_registration_object_id",regObjId1);
        testReg1.put("register_subject_account_ref","SH" + gdAccClientNo1);

        testReg2.put("register_registration_object_id",regObjId2);
        testReg2.put("register_subject_account_ref","SH" + gdAccClientNo1);

        List<Map> regListInfo = new ArrayList<>();
        regListInfo.add(testReg1);
        regListInfo.add(testReg2);

        sleepAndSaveInfo(3000);
        int beforeBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");

        String response = gd.GDShareChangeProperty(gdPlatfromKeyID,address,eqCode,changeAmount,oldProperty,newProperty,regListInfo);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("监管数据校验失败",true,JSONObject.fromObject(response).getString("message").contains("Data too long for column 'object_id' at row 1"));


        busUUID = tempUUID;

        sleepAndSaveInfo(4000);
        //待校验的股东持股信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,4500,0,0,mapShareENCN().get("0"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount1, 500, 1, 0, mapShareENCN().get("1"), respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2,5000,0,0,mapShareENCN().get("0"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3,5000,0,0,mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4,5000,0,0,mapShareENCN().get("0"), respShareList3);

        int afterBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");
        assertEquals("区块高度不增加，无存证上链",beforeBlockHeight,afterBlockHeight);

//        String blockDetail = store.GetBlockByHeight(afterBlockHeight);
//        assertEquals("区块仅有一笔交易",1,JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").size());
//        //确认交易是股份性质变更交易
//        String getTXDetails = store.GetTxDetail(JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").getString(0));
//        JSONObject jsonObject1 = JSONObject.fromObject(getTXDetails).getJSONObject("data");
//        assertEquals("1",jsonObject1.getJSONObject("header").getString("version"));
//        assertEquals("1",jsonObject1.getJSONObject("header").getString("type"));
//        assertEquals("11",jsonObject1.getJSONObject("header").getString("subType"));
//
//        assertEquals(true,getTXDetails.contains("{\"from\":\"" + address + "\",\"to\":\"" + address
//                + "\",\"tokenType\":\"" + gdEquityCode + "\",\"amount\":\"500\",\"subType\":\"0\",\"newSubType\":\"1\"}"));
//        assertEquals(true,getTXDetails.contains("{\"from\":\"" + address + "\",\"to\":\"" + address
//                + "\",\"tokenType\":\"" + gdEquityCode + "\",\"amount\":\"4500\",\"subType\":\"0\"}"));
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
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,regObjId1 + "/0",""));
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,regObjId2 + "/0",""));

//        //7.判断数据库中是否存储登记对象ID
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId1));
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId2));
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId3));
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId4));

        //重复执行 使用上次的uuid
        regObjId1 = mapAccAddr.get(address) + "CProp1" + Random(6);
        testReg1.put("register_registration_object_id",regObjId1);
        response= gd.GDShareChangeProperty(gdPlatfromKeyID,address,eqCode,changeAmount,oldProperty,newProperty,regListInfo);
        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        String txDetail = store.GetTxDetail(txId);
        assertEquals("200", JSONObject.fromObject(txDetail).getString("state"));

        busUUID = "";

        //检查各个查询对象返回信息中不包含敏感词
        assertEquals("不包含敏感词",true,gdCF.chkSensitiveWord(txDetail,regType));

        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200", JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");

        log.info("================================检查存证数据格式化《开始》================================");

        Map uriInfo = gdCF.getJGURIStoreHash(txId,conJGFileName(regObjId1,"0"),1);

        String tempAddr = address;

        log.info("检查股权性质变更存证登记格式化及信息内容与传入一致");
        String regVer = "0" ;//gdCF.getObjectLatestVer(account_subject_ref);

        //获取链上mini url的存证信息 并检查是否包含uri信息 每个登记都是新的 则都是0
        String regfileName1 = conJGFileName(regObjId1,regVer);
        String regfileName2 = conJGFileName(regObjId2,regVer);
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
        regInfoInput.put("content",gdCF.constructContentTreeMap(regType,regObjId1,regVer,"create",String.valueOf(ts5)));
        log.info("检查登记存证信息内容与传入一致\n" + regInfoInput.toString() + "\n" + getRegInfo1.toString());
        bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(regInfoInput,regType)),replaceCertain(getRegInfo1.toString()));
        assertEquals("1检查数据是否一致" ,true,bSame);

        regInfoInput = testReg2;
        regInfoInput.put("content",gdCF.constructContentTreeMap(regType,regObjId2,regVer,"create",String.valueOf(ts5)));
        log.info("检查登记存证信息内容与传入一致\n" + regInfoInput.toString() + "\n" + getRegInfo1.toString());
        bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(regInfoInput,regType)),replaceCertain(getRegInfo2.toString()));
        assertEquals("2检查数据是否一致" ,true,bSame);

        log.info("================================检查存证数据格式化《结束》================================");

        //实际应该持股情况信息
        respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1, 4500, 0, 0, mapShareENCN().get("0"), respShareList);
        respShareList = gdConstructQueryShareList(gdAccount1, 500, 1, 0, mapShareENCN().get("1"), respShareList);
        respShareList2 = gdConstructQueryShareList(gdAccount2, 5000, 0, 0, mapShareENCN().get("0"), respShareList);
        respShareList3 = gdConstructQueryShareList(gdAccount3, 5000, 0, 0, mapShareENCN().get("0"), respShareList2);
        respShareList4 = gdConstructQueryShareList(gdAccount4, 5000, 0, 0, mapShareENCN().get("0"), respShareList3);

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
    }

    //20210930 登记对象允许update
    @Test
    public void shareChangeProperty_SameUniqueIdSameRequest() throws Exception {

        log.info(registerInfo.toString());
        mapAddrRegObjId.clear(); //先清空map 这样后面map中拿到的就是此次发行的登记对象标识

        String eqCode = gdEquityCode;
        String address = gdAccount1;

        log.info("++++++++++++++++++++++++++++++++++++++++++++++++++++++" + gdEquityCode);

        int oldProperty = 0;
        int newProperty = 1;

        Map testReg1 = gdBF.init05RegInfo();
        Map testReg2 = gdBF.init05RegInfo();
        String regObjId1 = mapAccAddr.get(address) + "CProp1" + Random(6);
        String regObjId2 = mapAccAddr.get(address) + "CProp2" + Random(6);

        testReg1.put("register_registration_object_id",regObjId1);
        testReg1.put("register_subject_account_ref","SH" + gdAccClientNo1);

        testReg2.put("register_registration_object_id",regObjId2);
        testReg2.put("register_subject_account_ref","SH" + gdAccClientNo1);


        List<Map> regListInfo = new ArrayList<>();
        regListInfo.add(testReg1);
        regListInfo.add(testReg2);

        sleepAndSaveInfo(3000);
        int beforeBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");

        String response = gd.GDShareChangeProperty(gdPlatfromKeyID,address,eqCode,changeAmount,oldProperty,newProperty,regListInfo);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));

        busUUID = tempUUID;

        sleepAndSaveInfo(4000);
        //待校验的股东持股信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,4500,0,0,mapShareENCN().get("0"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount1, 500, 1, 0, mapShareENCN().get("1"), respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2,5000,0,0,mapShareENCN().get("0"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3,5000,0,0,mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4,5000,0,0,mapShareENCN().get("0"), respShareList3);

        int afterBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");
        assertEquals("区块高度增加2，存证上链，合约交易执行",beforeBlockHeight + 2,afterBlockHeight);

        String blockDetail = store.GetBlockByHeight(afterBlockHeight -1);
        assertEquals("区块仅有一笔交易",1,JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").size());
        //确认交易是股份性质变更交易
        String getTXDetails = store.GetTxDetail(JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").getString(0));
        JSONObject jsonObject1 = JSONObject.fromObject(getTXDetails).getJSONObject("data");
        assertEquals("1",jsonObject1.getJSONObject("header").getString("version"));
        assertEquals("1",jsonObject1.getJSONObject("header").getString("type"));
        assertEquals("11",jsonObject1.getJSONObject("header").getString("subType"));

        assertEquals(true,getTXDetails.contains("{\"from\":\"" + address + "\",\"to\":\"" + address
                + "\",\"tokenType\":\"" + gdEquityCode + "\",\"amount\":\"500\",\"subType\":\"0\",\"newSubType\":\"1\"}"));
        assertEquals(true,getTXDetails.contains("{\"from\":\"" + address + "\",\"to\":\"" + address
                + "\",\"tokenType\":\"" + gdEquityCode + "\",\"amount\":\"4500\",\"subType\":\"0\"}"));

        blockDetail = store.GetBlockByHeight(afterBlockHeight);
        assertEquals("区块仅有一笔交易",1,JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").size());
        //确认交易是发行交易
        getTXDetails = store.GetTxDetail(JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").getString(0));
        assertEquals(true,getTXDetails.contains(regObjId1));
        assertEquals(true,getTXDetails.contains(regObjId2));


        //4.判断股东列表有变更
        String queryList = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(queryList).getString("state"));
        JSONArray dataShareListNow = JSONObject.fromObject(queryList).getJSONArray("data");
        assertEquals(respShareList4.size(),dataShareListNow.size());
        List<Map> getShareListNow = getShareListFromQueryNoZeroAcc(dataShareListNow);
        assertEquals(respShareList4.size(),getShareListNow.size());
        assertEquals(true,respShareList4.containsAll(getShareListNow) && getShareListNow.containsAll(respShareList4));


        //重复执行 使用上次的uuid
        response= gd.GDShareChangeProperty(gdPlatfromKeyID,address,eqCode,changeAmount,oldProperty,newProperty,regListInfo);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,JSONObject.fromObject(response).getString("message").contains("请检查此对象标识是否已经存在"));

        sleepAndSaveInfo(3000);
        int afterBlockHeight2 = JSONObject.fromObject(store.GetHeight()).getInt("data");
        assertEquals("区块高度增加，无存证上链",afterBlockHeight + 1,afterBlockHeight2);
    }

    @Test
    public void shareChangeProperty_SameUniqueIdDiffRequest() throws Exception {

        log.info(registerInfo.toString());
        mapAddrRegObjId.clear(); //先清空map 这样后面map中拿到的就是此次发行的登记对象标识

        String eqCode = gdEquityCode;
        String address = gdAccount1;

        log.info("++++++++++++++++++++++++++++++++++++++++++++++++++++++" + gdEquityCode);

        int oldProperty = 0;
        int newProperty = 1;

        Map testReg1 = gdBF.init05RegInfo();
        Map testReg2 = gdBF.init05RegInfo();
        String regObjId1 = mapAccAddr.get(address) + "CProp1" + Random(6);
        String regObjId2 = mapAccAddr.get(address) + "CProp2" + Random(6);

        testReg1.put("register_registration_object_id",regObjId1);
        testReg1.put("register_subject_account_ref","SH" + gdAccClientNo1);

        testReg2.put("register_registration_object_id",regObjId2);
        testReg2.put("register_subject_account_ref","SH" + gdAccClientNo1);


        List<Map> regListInfo = new ArrayList<>();
        regListInfo.add(testReg1);
        regListInfo.add(testReg2);

        sleepAndSaveInfo(3000);
        int beforeBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");

        String response = gd.GDShareChangeProperty(gdPlatfromKeyID,address,eqCode,changeAmount,oldProperty,newProperty,regListInfo);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));

        busUUID = tempUUID;

        sleepAndSaveInfo(4000);
        //待校验的股东持股信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,4500,0,0,mapShareENCN().get("0"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount1, 500, 1, 0, mapShareENCN().get("1"), respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2,5000,0,0,mapShareENCN().get("0"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3,5000,0,0,mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4,5000,0,0,mapShareENCN().get("0"), respShareList3);

        int afterBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");
        assertEquals("区块高度增加2，存证上链，合约交易执行",beforeBlockHeight + 2,afterBlockHeight);

        String blockDetail = store.GetBlockByHeight(afterBlockHeight -1);
        assertEquals("区块仅有一笔交易",1,JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").size());
        //确认交易是股份性质变更交易
        String getTXDetails = store.GetTxDetail(JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").getString(0));
        JSONObject jsonObject1 = JSONObject.fromObject(getTXDetails).getJSONObject("data");
        assertEquals("1",jsonObject1.getJSONObject("header").getString("version"));
        assertEquals("1",jsonObject1.getJSONObject("header").getString("type"));
        assertEquals("11",jsonObject1.getJSONObject("header").getString("subType"));

        assertEquals(true,getTXDetails.contains("{\"from\":\"" + address + "\",\"to\":\"" + address
                + "\",\"tokenType\":\"" + gdEquityCode + "\",\"amount\":\"500\",\"subType\":\"0\",\"newSubType\":\"1\"}"));
        assertEquals(true,getTXDetails.contains("{\"from\":\"" + address + "\",\"to\":\"" + address
                + "\",\"tokenType\":\"" + gdEquityCode + "\",\"amount\":\"4500\",\"subType\":\"0\"}"));

        blockDetail = store.GetBlockByHeight(afterBlockHeight);
        assertEquals("区块仅有一笔交易",1,JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").size());
        //确认交易是发行交易
        getTXDetails = store.GetTxDetail(JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").getString(0));
        assertEquals(true,getTXDetails.contains(regObjId1));
        assertEquals(true,getTXDetails.contains(regObjId2));


        //4.判断股东列表有变更
        String queryList = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(queryList).getString("state"));
        JSONArray dataShareListNow = JSONObject.fromObject(queryList).getJSONArray("data");
        assertEquals(respShareList4.size(),dataShareListNow.size());
        List<Map> getShareListNow = getShareListFromQueryNoZeroAcc(dataShareListNow);
        assertEquals(respShareList4.size(),getShareListNow.size());
        assertEquals(true,respShareList4.containsAll(getShareListNow) && getShareListNow.containsAll(respShareList4));


        //重复执行 使用上次的uuid
        regObjId1 = mapAccAddr.get(address) + "CProp1" + Random(6);
        regObjId2 = mapAccAddr.get(address) + "CProp2" + Random(6);

        testReg1.put("register_registration_object_id",regObjId1);
        testReg2.put("register_registration_object_id",regObjId2);

        response= gd.GDShareChangeProperty(gdPlatfromKeyID,address,eqCode,changeAmount,oldProperty,newProperty,regListInfo);
        //合约未执行 但是uri存证上报了
        sleepAndSaveInfo(3000);

        int afterBlockHeight2 = JSONObject.fromObject(store.GetHeight()).getInt("data");
        assertEquals("区块高度增加1，存证上链，合约不执行",afterBlockHeight + 1,afterBlockHeight2);

        blockDetail = store.GetBlockByHeight(afterBlockHeight2);
        assertEquals("区块仅有一笔交易",1,JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").size());
        //确认交易是发行交易
        getTXDetails = store.GetTxDetail(JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").getString(0));
        assertEquals(true,getTXDetails.contains(regObjId1));
        assertEquals(true,getTXDetails.contains(regObjId2));
        assertEquals("0",JSONObject.fromObject(getTXDetails).getJSONObject("data").getJSONObject("header").getString("type"));
    }

}
