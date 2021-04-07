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
public class GDV2_ShareChangeBoard_UniqueId_Test {

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

        register_event_type = "1";//非交易登记
        tempsubject_investor_qualification_certifier_ref =subject_investor_qualification_certifier_ref;
        tempregister_transaction_ref = register_transaction_ref;

        List<Map> shareList = gdConstructShareList(gdAccount1,issueAmount,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,issueAmount,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,issueAmount,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,issueAmount,0,shareList3);

        String response= uf.shareIssue(gdEquityCode,shareList4,false);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");
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


    @Test
    public void shareChangeBoard_SFNS() throws Exception {

        //发行
        log.info(registerInfo.toString());
        mapAddrRegObjId.clear(); //先清空map 这样后面map中拿到的就是此次发行的登记对象标识

        //场内转板
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
        newEqProd.put("product_create_time",""); //设置product_create_time为空 使得发行成功 报送失败

        sleepAndSaveInfo(3000);
        int beforeBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");

        String response= gd.GDShareChangeBoard(gdPlatfromKeyID,cpnyId,oldEquityCode,newEquityCode,regList,oldEqProd,newEqProd);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));

        busUUID = tempUUID;
        sleepAndSaveInfo(4000);
        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,5000,0,0,mapShareENCN().get("0"),respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2,5000,0,0,mapShareENCN().get("0"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3,5000,0,0,mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4,5000,0,0,mapShareENCN().get("0"), respShareList3);

        int afterBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");
        assertEquals("区块高度仅增加1，无存证上链",beforeBlockHeight + 1,afterBlockHeight);

        String blockDetail = store.GetBlockByHeight(afterBlockHeight);
        assertEquals("区块仅有一笔交易",1,JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").size());
        //确认交易是场内转板交易
        String getTXDetails = store.GetTxDetail(JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").getString(0));
        JSONObject jsonObject1 = JSONObject.fromObject(getTXDetails).getJSONObject("data");
        assertEquals("1",jsonObject1.getJSONObject("header").getString("version"));
        assertEquals("1",jsonObject1.getJSONObject("header").getString("type"));
        assertEquals("13",jsonObject1.getJSONObject("header").getString("subType"));

        assertEquals(true,getTXDetails.contains("{\"from\":\"" + gdAccount1 + "\",\"to\":\"" + gdAccount1 +
                "\",\"tokenType\":\"" + oldEquityCode + "\",\"newTokenType\":\"" + newEquityCode + "\",\"amount\":\"5000\",\"subType\":\"0\"}"));

        assertEquals(true,getTXDetails.contains("{\"from\":\"" + gdAccount2 + "\",\"to\":\"" + gdAccount2 +
                "\",\"tokenType\":\"" + oldEquityCode + "\",\"newTokenType\":\"" + newEquityCode + "\",\"amount\":\"5000\",\"subType\":\"0\"}"));

        assertEquals(true,getTXDetails.contains("{\"from\":\"" + gdAccount3 + "\",\"to\":\"" + gdAccount3 +
                "\",\"tokenType\":\"" + oldEquityCode + "\",\"newTokenType\":\"" + newEquityCode + "\",\"amount\":\"5000\",\"subType\":\"0\"}"));

        assertEquals(true,getTXDetails.contains("{\"from\":\"" + gdAccount4 + "\",\"to\":\"" + gdAccount4 +
                "\",\"tokenType\":\"" + oldEquityCode + "\",\"newTokenType\":\"" + newEquityCode + "\",\"amount\":\"5000\",\"subType\":\"0\"}"));

        //4.判断股东列表有变更
        String queryList = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(queryList).getString("state"));
        JSONArray dataShareListNow = JSONObject.fromObject(queryList).getJSONArray("data");
        assertEquals(respShareList4.size(),dataShareListNow.size());
        List<Map> getShareListNow = getShareListFromQueryNoZeroAcc(dataShareListNow);
        assertEquals(respShareList4.size(),getShareListNow.size());
        log.info("444444444444444444444444444444444444444444444444444444444444444444444");
        log.info(respShareList4.toString());
        log.info(getShareListNow.toString());
        assertEquals(true,respShareList4.containsAll(getShareListNow) && getShareListNow.containsAll(respShareList4));

        //5.判断uuid是否存在合约中 方法待确认

        //6.判断所有登记对象ID是否存在OSS中
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,newEquityCode + "/0",""));
//        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,regObjId2 + "/0",""));

//        //7.判断数据库中是否存储登记对象ID
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId1));
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId2));
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId3));
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId4));

        //重复执行 使用上次的uuid
        newEqProd.put("product_create_time",time2); //设置product_create_time正常 使得报送可以成功
        response= gd.GDShareChangeBoard(gdPlatfromKeyID,cpnyId,oldEquityCode,newEquityCode,regList,oldEqProd,newEqProd);

        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        String details = store.GetTxDetail(txId);
        assertEquals("200",JSONObject.fromObject(details).getString("state"));

        busUUID = "";

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
        respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,5000,0,0,mapShareENCN().get("0"),respShareList);
        respShareList2 = gdConstructQueryShareList(gdAccount2,5000,0,0,mapShareENCN().get("0"), respShareList);
        respShareList3 = gdConstructQueryShareList(gdAccount3,5000,0,0,mapShareENCN().get("0"), respShareList2);
        respShareList4 = gdConstructQueryShareList(gdAccount4,5000,0,0,mapShareENCN().get("0"), respShareList3);

        //检查存在余额的股东列表
        assertEquals(respShareList4.size(),dataShareList.size());

        List<Map> getShareList = getShareListFromQueryNoZeroAcc(dataShareList);

        assertEquals(respShareList4.size(),getShareList.size());
        System.out.println(getShareList);
        System.out.println(respShareList4);
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
    public void shareChangeBoard_SFNFNS() throws Exception {

        //发行
        log.info(registerInfo.toString());
        mapAddrRegObjId.clear(); //先清空map 这样后面map中拿到的就是此次发行的登记对象标识

        //场内转板
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
        newEqProd.put("product_create_time",""); //设置product_create_time为空 使得发行成功 报送失败

        sleepAndSaveInfo(3000);
        int beforeBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");

        String response= gd.GDShareChangeBoard(gdPlatfromKeyID,cpnyId,oldEquityCode,newEquityCode,regList,oldEqProd,newEqProd);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));

        busUUID = tempUUID;
        sleepAndSaveInfo(4000);
        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,5000,0,0,mapShareENCN().get("0"),respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2,5000,0,0,mapShareENCN().get("0"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3,5000,0,0,mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4,5000,0,0,mapShareENCN().get("0"), respShareList3);

        int afterBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");
        assertEquals("区块高度仅增加1，无存证上链",beforeBlockHeight + 1,afterBlockHeight);

        String blockDetail = store.GetBlockByHeight(afterBlockHeight);
        assertEquals("区块仅有一笔交易",1,JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").size());
        //确认交易是场内转板交易
        String getTXDetails = store.GetTxDetail(JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").getString(0));
        JSONObject jsonObject1 = JSONObject.fromObject(getTXDetails).getJSONObject("data");
        assertEquals("1",jsonObject1.getJSONObject("header").getString("version"));
        assertEquals("1",jsonObject1.getJSONObject("header").getString("type"));
        assertEquals("13",jsonObject1.getJSONObject("header").getString("subType"));

        assertEquals(true,getTXDetails.contains("{\"from\":\"" + gdAccount1 + "\",\"to\":\"" + gdAccount1 +
                "\",\"tokenType\":\"" + oldEquityCode + "\",\"newTokenType\":\"" + newEquityCode + "\",\"amount\":\"5000\",\"subType\":\"0\"}"));

        assertEquals(true,getTXDetails.contains("{\"from\":\"" + gdAccount2 + "\",\"to\":\"" + gdAccount2 +
                "\",\"tokenType\":\"" + oldEquityCode + "\",\"newTokenType\":\"" + newEquityCode + "\",\"amount\":\"5000\",\"subType\":\"0\"}"));

        assertEquals(true,getTXDetails.contains("{\"from\":\"" + gdAccount3 + "\",\"to\":\"" + gdAccount3 +
                "\",\"tokenType\":\"" + oldEquityCode + "\",\"newTokenType\":\"" + newEquityCode + "\",\"amount\":\"5000\",\"subType\":\"0\"}"));

        assertEquals(true,getTXDetails.contains("{\"from\":\"" + gdAccount4 + "\",\"to\":\"" + gdAccount4 +
                "\",\"tokenType\":\"" + oldEquityCode + "\",\"newTokenType\":\"" + newEquityCode + "\",\"amount\":\"5000\",\"subType\":\"0\"}"));

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
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,newEquityCode + "/0",""));
//        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,regObjId2 + "/0",""));

//        //7.判断数据库中是否存储登记对象ID
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId1));
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId2));
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId3));
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId4));


        //重复执行 使用上次的uuid
        newEqProd.put("product_create_time","2021-03-15 10:12:00"); //设置product_create_time错误格式 报送失败
        response= gd.GDShareChangeBoard(gdPlatfromKeyID,cpnyId,oldEquityCode,newEquityCode,regList,oldEqProd,newEqProd);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        sleepAndSaveInfo(4000);
        afterBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");
        assertEquals("区块高度无增加，无存证上链",beforeBlockHeight + 1,afterBlockHeight);


        //6.判断所有登记对象ID是否存在OSS中
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,oldEquityCode + "/1",""));
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,newEquityCode + "/0",""));
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,mapAddrRegObjId.get(gdAccount1 + "0").toString() + "/0",""));


        //重复执行 使用上次的uuid
        newEqProd.put("product_create_time",time2); //设置product_create_time正常 使得报送可以成功
        response= gd.GDShareChangeBoard(gdPlatfromKeyID,cpnyId,oldEquityCode,newEquityCode,regList,oldEqProd,newEqProd);

        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        String details = store.GetTxDetail(txId);
        assertEquals("200",JSONObject.fromObject(details).getString("state"));

        busUUID = "";

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
        respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,5000,0,0,mapShareENCN().get("0"),respShareList);
        respShareList2 = gdConstructQueryShareList(gdAccount2,5000,0,0,mapShareENCN().get("0"), respShareList);
        respShareList3 = gdConstructQueryShareList(gdAccount3,5000,0,0,mapShareENCN().get("0"), respShareList2);
        respShareList4 = gdConstructQueryShareList(gdAccount4,5000,0,0,mapShareENCN().get("0"), respShareList3);

        //检查存在余额的股东列表
        assertEquals(respShareList4.size(),dataShareList.size());

        List<Map> getShareList = getShareListFromQueryNoZeroAcc(dataShareList);

        assertEquals(respShareList4.size(),getShareList.size());
        System.out.println(getShareList);
        System.out.println(respShareList4);
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

    //当前脚本 股权代码即是对象标识 过长 会导致数据库失败 进而无法继续同步数据
//    @Test
    public void shareChangeBoard_SFNS02() throws Exception {

        //发行
        log.info(registerInfo.toString());
        mapAddrRegObjId.clear(); //先清空map 这样后面map中拿到的就是此次发行的登记对象标识

        //场内转板
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
        newEqProd.put("product_create_time",""); //设置product_create_time为空 使得发行成功 报送失败

        sleepAndSaveInfo(3000);
        int beforeBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");

        String response= gd.GDShareChangeBoard(gdPlatfromKeyID,cpnyId,oldEquityCode,newEquityCode,regList,oldEqProd,newEqProd);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));

        busUUID = tempUUID;
        sleepAndSaveInfo(4000);
        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,5000,0,0,mapShareENCN().get("0"),respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2,5000,0,0,mapShareENCN().get("0"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3,5000,0,0,mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4,5000,0,0,mapShareENCN().get("0"), respShareList3);

        int afterBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");
        assertEquals("区块高度仅增加1，无存证上链",beforeBlockHeight + 1,afterBlockHeight);

        String blockDetail = store.GetBlockByHeight(afterBlockHeight);
        assertEquals("区块仅有一笔交易",1,JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").size());
        //确认交易是场内转板交易
        String getTXDetails = store.GetTxDetail(JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").getString(0));
        JSONObject jsonObject1 = JSONObject.fromObject(getTXDetails).getJSONObject("data");
        assertEquals("1",jsonObject1.getJSONObject("header").getString("version"));
        assertEquals("1",jsonObject1.getJSONObject("header").getString("type"));
        assertEquals("13",jsonObject1.getJSONObject("header").getString("subType"));

        assertEquals(true,getTXDetails.contains("{\"from\":\"" + gdAccount1 + "\",\"to\":\"" + gdAccount1 +
                "\",\"tokenType\":\"" + oldEquityCode + "\",\"newTokenType\":\"" + newEquityCode + "\",\"amount\":\"5000\",\"subType\":\"0\"}"));

        assertEquals(true,getTXDetails.contains("{\"from\":\"" + gdAccount2 + "\",\"to\":\"" + gdAccount2 +
                "\",\"tokenType\":\"" + oldEquityCode + "\",\"newTokenType\":\"" + newEquityCode + "\",\"amount\":\"5000\",\"subType\":\"0\"}"));

        assertEquals(true,getTXDetails.contains("{\"from\":\"" + gdAccount3 + "\",\"to\":\"" + gdAccount3 +
                "\",\"tokenType\":\"" + oldEquityCode + "\",\"newTokenType\":\"" + newEquityCode + "\",\"amount\":\"5000\",\"subType\":\"0\"}"));

        assertEquals(true,getTXDetails.contains("{\"from\":\"" + gdAccount4 + "\",\"to\":\"" + gdAccount4 +
                "\",\"tokenType\":\"" + oldEquityCode + "\",\"newTokenType\":\"" + newEquityCode + "\",\"amount\":\"5000\",\"subType\":\"0\"}"));

        //4.判断股东列表有变更
        String queryList = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(queryList).getString("state"));
        JSONArray dataShareListNow = JSONObject.fromObject(queryList).getJSONArray("data");
        assertEquals(respShareList4.size(),dataShareListNow.size());
        List<Map> getShareListNow = getShareListFromQueryNoZeroAcc(dataShareListNow);
        assertEquals(respShareList4.size(),getShareListNow.size());
        log.info("444444444444444444444444444444444444444444444444444444444444444444444");
        log.info(respShareList4.toString());
        log.info(getShareListNow.toString());
        assertEquals(true,respShareList4.containsAll(getShareListNow) && getShareListNow.containsAll(respShareList4));

        //5.判断uuid是否存在合约中 方法待确认

        //6.判断所有登记对象ID是否存在OSS中
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,newEquityCode + "/0",""));
//        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,regObjId2 + "/0",""));

//        //7.判断数据库中是否存储登记对象ID
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId1));
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId2));
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId3));
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId4));

        //重复执行 使用上次的uuid
        newEqProd.put("product_create_time",time2); //设置product_create_time正常 使得报送可以成功
        response= gd.GDShareChangeBoard(gdPlatfromKeyID,cpnyId,oldEquityCode,newEquityCode,regList,oldEqProd,newEqProd);

        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        String details = store.GetTxDetail(txId);
        assertEquals("200",JSONObject.fromObject(details).getString("state"));

        busUUID = "";

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
        respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,5000,0,0,mapShareENCN().get("0"),respShareList);
        respShareList2 = gdConstructQueryShareList(gdAccount2,5000,0,0,mapShareENCN().get("0"), respShareList);
        respShareList3 = gdConstructQueryShareList(gdAccount3,5000,0,0,mapShareENCN().get("0"), respShareList2);
        respShareList4 = gdConstructQueryShareList(gdAccount4,5000,0,0,mapShareENCN().get("0"), respShareList3);

        //检查存在余额的股东列表
        assertEquals(respShareList4.size(),dataShareList.size());

        List<Map> getShareList = getShareListFromQueryNoZeroAcc(dataShareList);

        assertEquals(respShareList4.size(),getShareList.size());
        System.out.println(getShareList);
        System.out.println(respShareList4);
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
    public void shareChangeBoard_SameUniqueIdSameRequest() throws Exception {

        //发行
        log.info(registerInfo.toString());
        mapAddrRegObjId.clear(); //先清空map 这样后面map中拿到的就是此次发行的登记对象标识

        //场内转板
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

        sleepAndSaveInfo(3000);
        int beforeBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");

        String response= gd.GDShareChangeBoard(gdPlatfromKeyID,cpnyId,oldEquityCode,newEquityCode,regList,oldEqProd,newEqProd);
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
        assertEquals("区块高度增加2，合约执行 存证上链",beforeBlockHeight + 2,afterBlockHeight);

        String blockDetail = store.GetBlockByHeight(afterBlockHeight - 1);
        assertEquals("区块仅有一笔交易",1,JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").size());
        //确认交易是场内转板交易
        String getTXDetails = store.GetTxDetail(JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").getString(0));
        JSONObject jsonObject1 = JSONObject.fromObject(getTXDetails).getJSONObject("data");
        assertEquals("1",jsonObject1.getJSONObject("header").getString("version"));
        assertEquals("1",jsonObject1.getJSONObject("header").getString("type"));
        assertEquals("13",jsonObject1.getJSONObject("header").getString("subType"));

        assertEquals(true,getTXDetails.contains("{\"from\":\"" + gdAccount1 + "\",\"to\":\"" + gdAccount1 +
                "\",\"tokenType\":\"" + oldEquityCode + "\",\"newTokenType\":\"" + newEquityCode + "\",\"amount\":\"5000\",\"subType\":\"0\"}"));

        assertEquals(true,getTXDetails.contains("{\"from\":\"" + gdAccount2 + "\",\"to\":\"" + gdAccount2 +
                "\",\"tokenType\":\"" + oldEquityCode + "\",\"newTokenType\":\"" + newEquityCode + "\",\"amount\":\"5000\",\"subType\":\"0\"}"));

        assertEquals(true,getTXDetails.contains("{\"from\":\"" + gdAccount3 + "\",\"to\":\"" + gdAccount3 +
                "\",\"tokenType\":\"" + oldEquityCode + "\",\"newTokenType\":\"" + newEquityCode + "\",\"amount\":\"5000\",\"subType\":\"0\"}"));

        assertEquals(true,getTXDetails.contains("{\"from\":\"" + gdAccount4 + "\",\"to\":\"" + gdAccount4 +
                "\",\"tokenType\":\"" + oldEquityCode + "\",\"newTokenType\":\"" + newEquityCode + "\",\"amount\":\"5000\",\"subType\":\"0\"}"));

        //4.判断股东列表有变更
        String queryList = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(queryList).getString("state"));
        JSONArray dataShareListNow = JSONObject.fromObject(queryList).getJSONArray("data");
        assertEquals(respShareList4.size(),dataShareListNow.size());
        List<Map> getShareListNow = getShareListFromQueryNoZeroAcc(dataShareListNow);
        assertEquals(respShareList4.size(),getShareListNow.size());
        assertEquals(true,respShareList4.containsAll(getShareListNow) && getShareListNow.containsAll(respShareList4));


        gdEquityCode = newEquityCode;

        //6.判断所有登记对象ID是否存在OSS中 数据已经上报OSS 但是无存证
        assertEquals(true,minio.getFileFromMinIO(minIOEP,jgBucket,
                newEquityCode + "/0","").contains("区域性股权市场跨链业务数据模型"));
        assertEquals(true,minio.getFileFromMinIO(minIOEP,jgBucket,
                oldEquityCode + "/1","").contains("区域性股权市场跨链业务数据模型"));
        assertEquals(true,minio.getFileFromMinIO(minIOEP,jgBucket,
                mapAddrRegObjId.get(gdAccount1 + "0").toString() + "/0","").contains("区域性股权市场跨链业务数据模型"));
        assertEquals(true,minio.getFileFromMinIO(minIOEP,jgBucket,
                mapAddrRegObjId.get(gdAccount2 + "0").toString() + "/0","").contains("区域性股权市场跨链业务数据模型"));
        assertEquals(true,minio.getFileFromMinIO(minIOEP,jgBucket,
                mapAddrRegObjId.get(gdAccount3 + "0").toString() + "/0","").contains("区域性股权市场跨链业务数据模型"));
        assertEquals(true,minio.getFileFromMinIO(minIOEP,jgBucket,
                mapAddrRegObjId.get(gdAccount4 + "0").toString() + "/0","").contains("区域性股权市场跨链业务数据模型"));


        response= gd.GDShareChangeBoard(gdPlatfromKeyID,cpnyId,oldEquityCode,newEquityCode,regList,oldEqProd,newEqProd);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,JSONObject.fromObject(response).getString("message").contains("股权代码还未发行或者已经转场"));

        sleepAndSaveInfo(3000);
        int afterBlockHeight2 = JSONObject.fromObject(store.GetHeight()).getInt("data");
        assertEquals("区块高度无增加，无存证上链",afterBlockHeight,afterBlockHeight2);
    }

    @Test
    public void shareChangeBoard_SameUniqueIdDiffRequest() throws Exception {


        //发行
        log.info(registerInfo.toString());
        mapAddrRegObjId.clear(); //先清空map 这样后面map中拿到的就是此次发行的登记对象标识

        //场内转板
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

        sleepAndSaveInfo(3000);
        int beforeBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");

        String response= gd.GDShareChangeBoard(gdPlatfromKeyID,cpnyId,oldEquityCode,newEquityCode,regList,oldEqProd,newEqProd);
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
        assertEquals("区块高度增加2，合约执行 存证上链",beforeBlockHeight + 2,afterBlockHeight);

        String blockDetail = store.GetBlockByHeight(afterBlockHeight -1);
        assertEquals("区块仅有一笔交易",1,JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").size());
        //确认交易是场内转板交易
        String getTXDetails = store.GetTxDetail(JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").getString(0));
        JSONObject jsonObject1 = JSONObject.fromObject(getTXDetails).getJSONObject("data");
        assertEquals("1",jsonObject1.getJSONObject("header").getString("version"));
        assertEquals("1",jsonObject1.getJSONObject("header").getString("type"));
        assertEquals("13",jsonObject1.getJSONObject("header").getString("subType"));

        assertEquals(true,getTXDetails.contains("{\"from\":\"" + gdAccount1 + "\",\"to\":\"" + gdAccount1 +
                "\",\"tokenType\":\"" + oldEquityCode + "\",\"newTokenType\":\"" + newEquityCode + "\",\"amount\":\"5000\",\"subType\":\"0\"}"));

        assertEquals(true,getTXDetails.contains("{\"from\":\"" + gdAccount2 + "\",\"to\":\"" + gdAccount2 +
                "\",\"tokenType\":\"" + oldEquityCode + "\",\"newTokenType\":\"" + newEquityCode + "\",\"amount\":\"5000\",\"subType\":\"0\"}"));

        assertEquals(true,getTXDetails.contains("{\"from\":\"" + gdAccount3 + "\",\"to\":\"" + gdAccount3 +
                "\",\"tokenType\":\"" + oldEquityCode + "\",\"newTokenType\":\"" + newEquityCode + "\",\"amount\":\"5000\",\"subType\":\"0\"}"));

        assertEquals(true,getTXDetails.contains("{\"from\":\"" + gdAccount4 + "\",\"to\":\"" + gdAccount4 +
                "\",\"tokenType\":\"" + oldEquityCode + "\",\"newTokenType\":\"" + newEquityCode + "\",\"amount\":\"5000\",\"subType\":\"0\"}"));

        //4.判断股东列表有变更
        String queryList = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(queryList).getString("state"));
        JSONArray dataShareListNow = JSONObject.fromObject(queryList).getJSONArray("data");
        assertEquals(respShareList4.size(),dataShareListNow.size());
        List<Map> getShareListNow = getShareListFromQueryNoZeroAcc(dataShareListNow);
        assertEquals(respShareList4.size(),getShareListNow.size());
        assertEquals(true,respShareList4.containsAll(getShareListNow) && getShareListNow.containsAll(respShareList4));


        gdEquityCode = newEquityCode;

        //6.判断所有登记对象ID是否存在OSS中 数据已经上报OSS 但是无存证
        assertEquals(true,minio.getFileFromMinIO(minIOEP,jgBucket,
                newEquityCode + "/0","").contains("区域性股权市场跨链业务数据模型"));
        assertEquals(true,minio.getFileFromMinIO(minIOEP,jgBucket,
                oldEquityCode + "/1","").contains("区域性股权市场跨链业务数据模型"));
        assertEquals(true,minio.getFileFromMinIO(minIOEP,jgBucket,
                mapAddrRegObjId.get(gdAccount1 + "0").toString() + "/0","").contains("区域性股权市场跨链业务数据模型"));
        assertEquals(true,minio.getFileFromMinIO(minIOEP,jgBucket,
                mapAddrRegObjId.get(gdAccount2 + "0").toString() + "/0","").contains("区域性股权市场跨链业务数据模型"));
        assertEquals(true,minio.getFileFromMinIO(minIOEP,jgBucket,
                mapAddrRegObjId.get(gdAccount3 + "0").toString() + "/0","").contains("区域性股权市场跨链业务数据模型"));
        assertEquals(true,minio.getFileFromMinIO(minIOEP,jgBucket,
                mapAddrRegObjId.get(gdAccount4 + "0").toString() + "/0","").contains("区域性股权市场跨链业务数据模型"));



        //使用相同的uniqueId 进行不同的业务

        gdEquityCode = "prodEq" + Random(12);
        gdCompanyID = "P1Re" + Random(8);
        register_product_ref = gdEquityCode;
        roll_register_product_ref = gdEquityCode;
        transaction_custody_product_ref = gdEquityCode;

        register_event_type = "1";//非交易登记
        tempsubject_investor_qualification_certifier_ref =subject_investor_qualification_certifier_ref;
        tempregister_transaction_ref = register_transaction_ref;
        mapAddrRegObjId.clear();
        List<Map> shareList = gdConstructShareList(gdAccount1,issueAmount,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,issueAmount,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,issueAmount,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,issueAmount,0,shareList3);

        response= uf.shareIssue(gdEquityCode,shareList4,false);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");
        String txDetail = store.GetTxDetail(txId);
        assertEquals("200", JSONObject.fromObject(txDetail).getString("state"));

        int afterBlockHeight2 = JSONObject.fromObject(store.GetHeight()).getInt("data");
        assertEquals("区块高度仅增加3，存证上链",afterBlockHeight + 3,afterBlockHeight2);//挂牌登记 存证*2
        assertEquals("400",JSONObject.fromObject(gd.GDGetEnterpriseShareInfo(gdEquityCode)).getString("state"));

    }


}
