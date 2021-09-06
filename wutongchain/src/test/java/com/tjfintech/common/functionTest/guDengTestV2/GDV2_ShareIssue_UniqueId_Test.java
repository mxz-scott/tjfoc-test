package com.tjfintech.common.functionTest.guDengTestV2;

import com.alibaba.fastjson.JSON;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.GuDeng;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.MinIOOperation;
import com.tjfintech.common.utils.MysqlOperation;
import com.tjfintech.common.utils.UtilsClass;
import io.minio.errors.MinioException;
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
public class GDV2_ShareIssue_UniqueId_Test {

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


    String tempaccount_subject_ref = account_subject_ref;
    String tempsubject_investor_qualification_certifier_ref = subject_investor_qualification_certifier_ref;
    String tempproduct_issuer_subject_ref = product_issuer_subject_ref;
    String tempregister_transaction_ref = register_transaction_ref;
    MinIOOperation minio = new MinIOOperation();
    MysqlOperation mysql =  new MysqlOperation();

//    String dbStr = utilsClass.getSDKWalletDBConfig();

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
    }

    @Before
    public void resetVar(){
        gdEquityCode = "prodEq" + Random(12);
        gdCompanyID = "P1Re" + Random(8);
        register_product_ref = gdEquityCode;
        roll_register_product_ref = gdEquityCode;
        transaction_custody_product_ref = gdEquityCode;

//        gdBefore.initRegulationData();
        equityProductInfo = gdBF.init03EquityProductInfo();
        bondProductInfo = null;
        fundProductInfo = null;

        register_event_type = 1;//非交易登记
        tempsubject_investor_qualification_certifier_ref =subject_investor_qualification_certifier_ref;
        tempregister_transaction_ref = register_transaction_ref;

        busUUID = "";
        bSaveBuff = false;
    }

    @After
    public void calJGDataAfterTx()throws Exception{
        testCurMethodName = tm.getMethodName();
        GDUnitFunc uf = new GDUnitFunc();
        int endHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");
//        uf.checkJGHeaderOpVer(blockHeight,endHeight);

        subject_investor_qualification_certifier_ref = tempsubject_investor_qualification_certifier_ref;
        register_transaction_ref = tempregister_transaction_ref;
        busUUID = "";
        bSaveBuff = false;
    }

    /***
     * 初始股份登记 合约交易执行成功 报送失败（登记数据校验错误）；再次登记 更新数据 报送成功
     * S 执行成功     * N 不执行     * F 执行失败
     *
     * @throws Exception
     */
    @Test
    public void TC06_shareIssue_SFNS() throws Exception {
        log.info(registerInfo.toString());
        mapAddrRegObjId.clear(); //先清空map 这样后面map中拿到的就是此次发行的登记对象标识
        register_product_ref = gdEquityCode;

        String regObjId1 = "5" + mapAccAddr.get(gdAccount1) + Random(6);// + "_" + indexReg;
        Map tempReg1 = gdBF.init05RegInfo();
        tempReg1.put("register_registration_object_id",regObjId1);
        tempReg1.put("register_subject_account_ref","SH" + mapAccAddr.get(gdAccount1));
        if(bChangeRegSN) tempReg1.put("register_serial_number", regObjId1);//区分同一账户多次登记//
        mapAddrRegObjId.put(gdAccount1 + "0" + indexReg,regObjId1);//方便后面测试验证


        String regObjId2 = "5" + mapAccAddr.get(gdAccount2) + Random(6);// + "_" + indexReg;
        Map tempReg2 = gdBF.init05RegInfo();
        tempReg2.put("register_registration_object_id",regObjId2);
        tempReg2.put("register_subject_account_ref","SH" + mapAccAddr.get(gdAccount2));
        if(bChangeRegSN) tempReg2.put("register_serial_number", regObjId2);//区分同一账户多次登记//
        mapAddrRegObjId.put(gdAccount2 + "0" + indexReg,regObjId2);//方便后面测试验证

        String regObjId3 = "5" + mapAccAddr.get(gdAccount3) + Random(6);// + "_" + indexReg;
        Map tempReg3 = gdBF.init05RegInfo();
        tempReg3.put("register_registration_object_id",regObjId3);
        tempReg3.put("register_subject_account_ref","SH" + mapAccAddr.get(gdAccount3));
        if(bChangeRegSN) tempReg3.put("register_serial_number", regObjId3);//区分同一账户多次登记//
        mapAddrRegObjId.put(gdAccount3 + "0" + indexReg,regObjId3);//方便后面测试验证

        String regObjId4 = "5" + mapAccAddr.get(gdAccount4) + Random(6);// + "_" + indexReg;
        Map tempReg4 = gdBF.init05RegInfo();
        tempReg4.put("register_registration_object_id",regObjId4);
        tempReg4.put("register_subject_account_ref","SH" + mapAccAddr.get(gdAccount4));
        if(bChangeRegSN) tempReg4.put("register_serial_number", regObjId4);//区分同一账户多次登记
        mapAddrRegObjId.put(gdAccount4 + "0" + indexReg,regObjId4);//方便后面测试验证

        tempReg1.put("register_time",""); //设置register_time为空 使得发行成功 报送失败

        List<Map> shareList = gdConstructShareListWithRegMap(gdAccount1,issueAmount,0,tempReg1);
        List<Map> shareList2 = gdConstructShareListWithRegMap(gdAccount2,issueAmount,0,tempReg2, shareList);
        List<Map> shareList3 = gdConstructShareListWithRegMap(gdAccount3,issueAmount,0,tempReg3, shareList2);
        List<Map> shareList4 = gdConstructShareListWithRegMap(gdAccount4,issueAmount,0,tempReg4,shareList3);

        uf.enterpriseReg(gdEquityCode,true);
        String query2 = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);

        sleepAndSaveInfo(3000);
        int beforeBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");

        log.info("发行");
        String response= gd.GDShareIssue(gdContractAddress,gdPlatfromKeyID,gdEquityCode,shareList4);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("监管数据校验失败",true,JSONObject.fromObject(response).getString("message").contains("数据格式校验失败"));


        busUUID = tempUUID;

        sleepAndSaveInfo(4000);
        //待校验的股东持股信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,5000,0,0,mapShareENCN().get("0"),respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2,5000,0,0,mapShareENCN().get("0"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3,5000,0,0,mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4,5000,0,0,mapShareENCN().get("0"), respShareList3);


        int afterBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");
        assertEquals("区块高度不增加，无存证上链",beforeBlockHeight,afterBlockHeight);

//        String blockDetail = store.GetBlockByHeight(afterBlockHeight);
//        assertEquals("区块仅有一笔交易",1,JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").size());
//        //确认交易是发行交易
//        String getTXDetails = store.GetTxDetail(JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").getString(0));
//        JSONObject jsonObject1 = JSONObject.fromObject(getTXDetails).getJSONObject("data");
//        assertEquals("2",jsonObject1.getJSONObject("header").getString("version"));
//        assertEquals("3",jsonObject1.getJSONObject("header").getString("type"));
//        assertEquals("43",jsonObject1.getJSONObject("header").getString("subType"));
//        JSONObject jsonObjectWVM = jsonObject1.getJSONObject("wvm").getJSONObject("wvmContractTx");
//        assertEquals("IssueToken",jsonObjectWVM.getJSONObject("arg").getString("method"));
//        assertEquals("Sys_SmartTokenContract",jsonObjectWVM.getString("name"));
//
//
//        //4.判断股东列表有变更
//        String queryList = gd.GDGetEnterpriseShareInfo(gdEquityCode);
//        assertEquals("200",JSONObject.fromObject(queryList).getString("state"));
//        JSONArray dataShareListNow = JSONObject.fromObject(queryList).getJSONArray("data");
//        assertEquals(respShareList4.size(),dataShareListNow.size());
//        List<Map> getShareListNow = getShareListFromQueryNoZeroAcc(dataShareListNow);
//        assertEquals(respShareList4.size(),getShareListNow.size());
//        log.info("444444444444444444444444444444444444444444444444444444444444444444444");
//        log.info(respShareList4.toString());
//        log.info(getShareListNow.toString());
//        assertEquals(true,respShareList4.containsAll(getShareListNow) && getShareListNow.containsAll(respShareList4));

        //5.判断uuid是否存在合约中 方法待确认

        //6.判断所有登记对象ID是否存在OSS中
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,regObjId1 + "/0",""));
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,regObjId2 + "/0",""));
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,regObjId3 + "/0",""));
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,regObjId4 + "/0",""));

//        //7.判断数据库中是否存储登记对象ID
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId1));
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId2));
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId3));
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId4));

        //重复执行 使用上次的uuid
        tempReg1.put("register_time",time2); //设置register_time正常 使得报送可以成功
        response= gd.GDShareIssue(gdContractAddress,gdPlatfromKeyID,gdEquityCode,shareList4);


        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        String txDetail = store.GetTxDetail(txId);
        assertEquals("200", JSONObject.fromObject(txDetail).getString("state"));

        busUUID = "";

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

        log.info(mapAddrRegObjId.toString());

        //遍历检查所有账户登记及交易存证信息
        for(int k = 0 ;k < dataShareList.size(); k++) {
            String tempAddr = JSONObject.fromObject(dataShareList.get(k)).getString("address");
            String tempSP = JSONObject.fromObject(dataShareList.get(k)).getString("shareProperty");
            log.info(tempAddr + tempSP);
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

        //检查存在余额的股东列表
        respShareList.clear();respShareList2.clear();respShareList3.clear();respShareList4.clear();
        respShareList = gdConstructQueryShareList(gdAccount1,5000,0,0,mapShareENCN().get("0"),respShareList);
        respShareList2 = gdConstructQueryShareList(gdAccount2,5000,0,0,mapShareENCN().get("0"), respShareList);
        respShareList3 = gdConstructQueryShareList(gdAccount3,5000,0,0,mapShareENCN().get("0"), respShareList2);
        respShareList4 = gdConstructQueryShareList(gdAccount4,5000,0,0,mapShareENCN().get("0"), respShareList3);

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


    /***
     * 初始股份登记 合约交易执行成功 报送失败（登记数据校验错误）；再次登记 登记数据仍错误
     * S 执行成功     * N 不执行     * F 执行失败
     *
     * @throws Exception
     */
    @Test
    public void TC06_shareIssue_SFNFNS() throws Exception {
        log.info(registerInfo.toString());
        mapAddrRegObjId.clear(); //先清空map 这样后面map中拿到的就是此次发行的登记对象标识
        register_product_ref = gdEquityCode;

        String regObjId1 = "5" + mapAccAddr.get(gdAccount1) + Random(6);// + "_" + indexReg;
        Map tempReg1 = gdBF.init05RegInfo();
        tempReg1.put("register_registration_object_id",regObjId1);
        tempReg1.put("register_subject_account_ref","SH" + mapAccAddr.get(gdAccount1));
        if(bChangeRegSN) tempReg1.put("register_serial_number", regObjId1);//区分同一账户多次登记//
        mapAddrRegObjId.put(gdAccount1 + "0" + indexReg,regObjId1);//方便后面测试验证


        String regObjId2 = "5" + mapAccAddr.get(gdAccount1) + Random(6);// + "_" + indexReg;
        Map tempReg2 = gdBF.init05RegInfo();
        tempReg2.put("register_registration_object_id",regObjId2);
        tempReg2.put("register_subject_account_ref","SH" + mapAccAddr.get(gdAccount2));
        if(bChangeRegSN) tempReg2.put("register_serial_number", regObjId2);//区分同一账户多次登记//
        mapAddrRegObjId.put(gdAccount2 + "0" + indexReg,regObjId2);//方便后面测试验证

        String regObjId3 = "5" + mapAccAddr.get(gdAccount1) + Random(6);// + "_" + indexReg;
        Map tempReg3 = gdBF.init05RegInfo();
        tempReg3.put("register_registration_object_id",regObjId3);
        tempReg3.put("register_subject_account_ref","SH" + mapAccAddr.get(gdAccount3));
        if(bChangeRegSN) tempReg3.put("register_serial_number", regObjId3);//区分同一账户多次登记//
        mapAddrRegObjId.put(gdAccount3 + "0" + indexReg,regObjId3);//方便后面测试验证

        String regObjId4 = "5" + mapAccAddr.get(gdAccount1) + Random(6);// + "_" + indexReg;
        Map tempReg4 = gdBF.init05RegInfo();
        tempReg4.put("register_registration_object_id",regObjId4);
        tempReg4.put("register_subject_account_ref","SH" + mapAccAddr.get(gdAccount4));
        if(bChangeRegSN) tempReg4.put("register_serial_number", regObjId4);//区分同一账户多次登记
        mapAddrRegObjId.put(gdAccount4 + "0" + indexReg,regObjId4);//方便后面测试验证

        tempReg1.put("register_time","2021-03-21 10:00:00"); //设置register_time错误格式 使得发行成功 报送失败

        List<Map> shareList = gdConstructShareListWithRegMap(gdAccount1,issueAmount,0,tempReg1);
        List<Map> shareList2 = gdConstructShareListWithRegMap(gdAccount2,issueAmount,0,tempReg2, shareList);
        List<Map> shareList3 = gdConstructShareListWithRegMap(gdAccount3,issueAmount,0,tempReg3, shareList2);
        List<Map> shareList4 = gdConstructShareListWithRegMap(gdAccount4,issueAmount,0,tempReg4,shareList3);

        uf.enterpriseReg(gdEquityCode,true);
        String query2 = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);

        sleepAndSaveInfo(3000);
        int beforeBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");

        log.info("发行");
        String response = gd.GDShareIssue(gdContractAddress,gdPlatfromKeyID,gdEquityCode,shareList4);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("监管数据校验失败",true,JSONObject.fromObject(response).getString("message").contains("数据格式校验失败"));

        busUUID = tempUUID;

        sleepAndSaveInfo(4000);

        //待校验的股东持股信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,5000,0,0,mapShareENCN().get("0"),respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2,5000,0,0,mapShareENCN().get("0"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3,5000,0,0,mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4,5000,0,0,mapShareENCN().get("0"), respShareList3);


        int afterBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");
        assertEquals("区块高度不增加，无存证上链",beforeBlockHeight,afterBlockHeight);

//        String blockDetail = store.GetBlockByHeight(afterBlockHeight);
//        assertEquals("区块仅有一笔交易",1,JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").size());
//        //确认交易是发行交易
//        String getTXDetails = store.GetTxDetail(JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").getString(0));
//        JSONObject jsonObject1 = JSONObject.fromObject(getTXDetails).getJSONObject("data");
//        assertEquals("2",jsonObject1.getJSONObject("header").getString("version"));
//        assertEquals("3",jsonObject1.getJSONObject("header").getString("type"));
//        assertEquals("43",jsonObject1.getJSONObject("header").getString("subType"));
//        JSONObject jsonObjectWVM = jsonObject1.getJSONObject("wvm").getJSONObject("wvmContractTx");
//        assertEquals("IssueToken",jsonObjectWVM.getJSONObject("arg").getString("method"));
//        assertEquals("Sys_SmartTokenContract",jsonObjectWVM.getString("name"));


        //4.判断股东列表有变更
        String queryList = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("400",JSONObject.fromObject(queryList).getString("state"));
//        JSONArray dataShareListNow = JSONObject.fromObject(queryList).getJSONArray("data");
//        assertEquals(respShareList4.size(),dataShareListNow.size());
//        List<Map> getShareListNow = getShareListFromQueryNoZeroAcc(dataShareListNow);
//        assertEquals(respShareList4.size(),getShareListNow.size());
//        assertEquals(true,respShareList4.containsAll(getShareListNow) && getShareListNow.containsAll(respShareList4));

        //5.判断uuid是否存在合约中 方法待确认

        //6.判断所有登记对象ID是否存在OSS中
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,regObjId1 + "/0",""));
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,regObjId2 + "/0",""));
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,regObjId3 + "/0",""));
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,regObjId4 + "/0",""));

//        //7.判断数据库中是否存储登记对象ID
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId1));
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId2));
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId3));
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId4));



        //重复执行 使用上次的uuid 仍使用错误的字段
        tempReg1.put("register_time",""); //设置register_time 为空 使得报送仍失败
        response = gd.GDShareIssue(gdContractAddress,gdPlatfromKeyID,gdEquityCode,shareList4);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        sleepAndSaveInfo(4000);
        afterBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");
        assertEquals("区块高度无增加，无存证上链",beforeBlockHeight,afterBlockHeight);


        //6.判断所有登记对象ID是否存在OSS中
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,regObjId1 + "/0",""));
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,regObjId2 + "/0",""));
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,regObjId3 + "/0",""));
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,regObjId4 + "/0",""));


        //重复执行 使用上次的uuid 可以成功校验
        tempReg1.put("register_time",time2); //设置register_time正常 使得报送可以成功
        response = gd.GDShareIssue(gdContractAddress,gdPlatfromKeyID,gdEquityCode,shareList4);
        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        String txDetail = store.GetTxDetail(txId);
        assertEquals("200", JSONObject.fromObject(txDetail).getString("state"));

        busUUID = "";

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

        log.info(mapAddrRegObjId.toString());

        //遍历检查所有账户登记及交易存证信息
        for(int k = 0 ;k < dataShareList.size(); k++) {
            String tempAddr = JSONObject.fromObject(dataShareList.get(k)).getString("address");
            String tempSP = JSONObject.fromObject(dataShareList.get(k)).getString("shareProperty");
            log.info(tempAddr + tempSP);
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
        respShareList.clear();respShareList2.clear();respShareList3.clear();respShareList4.clear();
        respShareList = gdConstructQueryShareList(gdAccount1,5000,0,0,mapShareENCN().get("0"),respShareList);
        respShareList2 = gdConstructQueryShareList(gdAccount2,5000,0,0,mapShareENCN().get("0"), respShareList);
        respShareList3 = gdConstructQueryShareList(gdAccount3,5000,0,0,mapShareENCN().get("0"), respShareList2);
        respShareList4 = gdConstructQueryShareList(gdAccount4,5000,0,0,mapShareENCN().get("0"), respShareList3);

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


    /***
     * 初始股份登记 合约交易执行成功 报送失败（登记数据校验错误 id长度过长）；再次登记 更新数据 报送成功
     * S 执行成功     * N 不执行     * F 执行失败
     *
     * @throws Exception
     */
    @Test
    public void TC06_shareIssue_SFNS02() throws Exception {
        log.info(registerInfo.toString());
        mapAddrRegObjId.clear(); //先清空map 这样后面map中拿到的就是此次发行的登记对象标识
        register_product_ref = gdEquityCode;

        String regObjId1 = "5" + mapAccAddr.get(gdAccount1) + Random(120);// + "_" + indexReg; //使得发行成功 报送失败
        Map tempReg1 = gdBF.init05RegInfo();
        tempReg1.put("register_registration_object_id",regObjId1);
        tempReg1.put("register_subject_account_ref","SH" + mapAccAddr.get(gdAccount1));
        if(bChangeRegSN) tempReg1.put("register_serial_number", regObjId1);//区分同一账户多次登记//
        mapAddrRegObjId.put(gdAccount1 + "0" + indexReg,regObjId1);//方便后面测试验证


        String regObjId2 = "5" + mapAccAddr.get(gdAccount2) + Random(6);// + "_" + indexReg;
        Map tempReg2 = gdBF.init05RegInfo();
        tempReg2.put("register_registration_object_id",regObjId2);
        tempReg2.put("register_subject_account_ref","SH" + mapAccAddr.get(gdAccount2));
        if(bChangeRegSN) tempReg2.put("register_serial_number", regObjId2);//区分同一账户多次登记//
        mapAddrRegObjId.put(gdAccount2 + "0" + indexReg,regObjId2);//方便后面测试验证

        String regObjId3 = "5" + mapAccAddr.get(gdAccount3) + Random(6);// + "_" + indexReg;
        Map tempReg3 = gdBF.init05RegInfo();
        tempReg3.put("register_registration_object_id",regObjId3);
        tempReg3.put("register_subject_account_ref","SH" + mapAccAddr.get(gdAccount3));
        if(bChangeRegSN) tempReg3.put("register_serial_number", regObjId3);//区分同一账户多次登记//
        mapAddrRegObjId.put(gdAccount3 + "0" + indexReg,regObjId3);//方便后面测试验证

        String regObjId4 = "5" + mapAccAddr.get(gdAccount4) + Random(6);// + "_" + indexReg;
        Map tempReg4 = gdBF.init05RegInfo();
        tempReg4.put("register_registration_object_id",regObjId4);
        tempReg4.put("register_subject_account_ref","SH" + mapAccAddr.get(gdAccount4));
        if(bChangeRegSN) tempReg4.put("register_serial_number", regObjId4);//区分同一账户多次登记
        mapAddrRegObjId.put(gdAccount4 + "0" + indexReg,regObjId4);//方便后面测试验证

        List<Map> shareList = gdConstructShareListWithRegMap(gdAccount1,issueAmount,0,tempReg1);
        List<Map> shareList2 = gdConstructShareListWithRegMap(gdAccount2,issueAmount,0,tempReg2, shareList);
        List<Map> shareList3 = gdConstructShareListWithRegMap(gdAccount3,issueAmount,0,tempReg3, shareList2);
        List<Map> shareList4 = gdConstructShareListWithRegMap(gdAccount4,issueAmount,0,tempReg4,shareList3);

        uf.enterpriseReg(gdEquityCode,true);
        String query2 = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);

        sleepAndSaveInfo(3000);
        int beforeBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");

        log.info("发行");
        String response= gd.GDShareIssue(gdContractAddress,gdPlatfromKeyID,gdEquityCode,shareList4);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("监管数据校验失败",true,JSONObject.fromObject(response).getString("message").contains("Data too long for column 'object_id' at row 1"));


        busUUID = tempUUID;

        sleepAndSaveInfo(4000);
        //待校验的股东持股信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,5000,0,0,mapShareENCN().get("0"),respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2,5000,0,0,mapShareENCN().get("0"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3,5000,0,0,mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4,5000,0,0,mapShareENCN().get("0"), respShareList3);


        int afterBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");
        assertEquals("区块高度不增加，无存证上链",beforeBlockHeight,afterBlockHeight);

//        String blockDetail = store.GetBlockByHeight(afterBlockHeight);
//        assertEquals("区块仅有一笔交易",1,JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").size());
//        //确认交易是发行交易
//        String getTXDetails = store.GetTxDetail(JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").getString(0));
//        JSONObject jsonObject1 = JSONObject.fromObject(getTXDetails).getJSONObject("data");
//        assertEquals("2",jsonObject1.getJSONObject("header").getString("version"));
//        assertEquals("3",jsonObject1.getJSONObject("header").getString("type"));
//        assertEquals("43",jsonObject1.getJSONObject("header").getString("subType"));
//        JSONObject jsonObjectWVM = jsonObject1.getJSONObject("wvm").getJSONObject("wvmContractTx");
//        assertEquals("IssueToken",jsonObjectWVM.getJSONObject("arg").getString("method"));
//        assertEquals("Sys_SmartTokenContract",jsonObjectWVM.getString("name"));
//
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
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,regObjId3 + "/0",""));
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,regObjId4 + "/0",""));

//        //7.判断数据库中是否存储登记对象ID
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId1));
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId2));
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId3));
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId4));

        //重复执行 使用上次的uuid
        regObjId1 = "5" + mapAccAddr.get(gdAccount1) + Random(6); //设置id长度正常 使得报送可以成功
        tempReg1.put("register_registration_object_id",regObjId1);
        tempReg1.put("register_subject_account_ref","SH" + mapAccAddr.get(gdAccount1));
        if(bChangeRegSN) tempReg1.put("register_serial_number", regObjId1);//区分同一账户多次登记//
        mapAddrRegObjId.put(gdAccount1 + "0" + indexReg,regObjId1);//方便后面测试验证

        //更新数据
        regObjId2 = "5" + mapAccAddr.get(gdAccount2) + Random(6);// + "_" + indexReg;
        tempReg2.put("register_registration_object_id",regObjId2);
        tempReg2.put("register_subject_account_ref","SH" + mapAccAddr.get(gdAccount2));
        if(bChangeRegSN) tempReg2.put("register_serial_number", regObjId2);//区分同一账户多次登记//
        mapAddrRegObjId.put(gdAccount2 + "0" + indexReg,regObjId2);//方便后面测试验证


        response= gd.GDShareIssue(gdContractAddress,gdPlatfromKeyID,gdEquityCode,shareList4);


        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        String txDetail = store.GetTxDetail(txId);
        assertEquals("200", JSONObject.fromObject(txDetail).getString("state"));

        busUUID = "";

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

        log.info(mapAddrRegObjId.toString());

        //遍历检查所有账户登记及交易存证信息
        for(int k = 0 ;k < dataShareList.size(); k++) {
            String tempAddr = JSONObject.fromObject(dataShareList.get(k)).getString("address");
            String tempSP = JSONObject.fromObject(dataShareList.get(k)).getString("shareProperty");
            log.info(tempAddr + tempSP);
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

        //检查存在余额的股东列表
        respShareList.clear();respShareList2.clear();respShareList3.clear();respShareList4.clear();
        respShareList = gdConstructQueryShareList(gdAccount1,5000,0,0,mapShareENCN().get("0"),respShareList);
        respShareList2 = gdConstructQueryShareList(gdAccount2,5000,0,0,mapShareENCN().get("0"), respShareList);
        respShareList3 = gdConstructQueryShareList(gdAccount3,5000,0,0,mapShareENCN().get("0"), respShareList2);
        respShareList4 = gdConstructQueryShareList(gdAccount4,5000,0,0,mapShareENCN().get("0"), respShareList3);

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
    public void shareIssueSameUniqueIdSameRequest() throws Exception {
        log.info(registerInfo.toString());
        mapAddrRegObjId.clear(); //先清空map 这样后面map中拿到的就是此次发行的登记对象标识

        List<Map> shareList = gdConstructShareList(gdAccount1,issueAmount,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,issueAmount,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,issueAmount,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,issueAmount,0,shareList3);

        int beforeBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");

        String response = uf.shareIssue(gdEquityCode,shareList4,false);
        JSONObject jsonObject = JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        String txDetail = store.GetTxDetail(txId);
        assertEquals("200", net.sf.json.JSONObject.fromObject(txDetail).getString("state"));

        sleepAndSaveInfo(3000);
        int afterBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");
        assertEquals("区块高度仅增加4，存证上链",beforeBlockHeight + 4,afterBlockHeight);//登记+发行

        String blockDetail = store.GetBlockByHeight(afterBlockHeight-1);
        assertEquals("区块仅有一笔交易",1,JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").size());
        //确认交易是发行交易
        String getTXDetails = store.GetTxDetail(JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").getString(0));
        JSONObject jsonObject1 = JSONObject.fromObject(getTXDetails).getJSONObject("data");
        assertEquals("2",jsonObject1.getJSONObject("header").getString("version"));
        assertEquals("3",jsonObject1.getJSONObject("header").getString("type"));
        assertEquals("43",jsonObject1.getJSONObject("header").getString("subType"));
        JSONObject jsonObjectWVM = jsonObject1.getJSONObject("wvm").getJSONObject("wvmContractTx");
        assertEquals("IssueToken",jsonObjectWVM.getJSONObject("arg").getString("method"));
        assertEquals("Sys_SmartTokenContract",jsonObjectWVM.getString("name"));

        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals(4,JSONObject.fromObject(query).getJSONArray("data").size());
        assertEquals(true,query.contains(gdAccount1));
        assertEquals(true,query.contains(gdAccount2));
        assertEquals(true,query.contains(gdAccount3));
        assertEquals(true,query.contains(gdAccount4));
        assertEquals(String.valueOf(issueAmount * 4),gdCF.getTotalAmountFromShareList(JSONObject.fromObject(query).getJSONArray("data")));

        busUUID = tempUUID;
        response = gd.GDShareIssue(gdContractAddress,gdPlatfromKeyID,gdEquityCode,shareList4);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,JSONObject.fromObject(response).getString("message").contains("请检查此对象标识是否已经存在"));

        sleepAndSaveInfo(3000);
        int afterBlockHeight2 = JSONObject.fromObject(store.GetHeight()).getInt("data");
        assertEquals("区块高度无增加，无存证上链",afterBlockHeight,afterBlockHeight2);
    }


    @Test
    public void shareIssueSameUniqueIdDiffRequest() throws Exception {
        log.info(registerInfo.toString());
        mapAddrRegObjId.clear(); //先清空map 这样后面map中拿到的就是此次发行的登记对象标识

        int beforeBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");

        List<Map> shareList = gdConstructShareList(gdAccount1,issueAmount,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,issueAmount,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,issueAmount,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,issueAmount,0,shareList3);

        String response = uf.shareIssue(gdEquityCode,shareList4,false);
        JSONObject jsonObject = JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        String txDetail = store.GetTxDetail(txId);
        assertEquals("200", net.sf.json.JSONObject.fromObject(txDetail).getString("state"));

        sleepAndSaveInfo(3000);
        int afterBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");
        assertEquals("区块高度仅增加4，存证上链",beforeBlockHeight + 4,afterBlockHeight);//登记+发行



        busUUID = tempUUID;
        mapAddrRegObjId.clear(); //先清空map 这样后面map中拿到的就是此次发行的登记对象标识

        gdEquityCode = "prodEq" + Random(12);
        gdCompanyID = "P1Re" + Random(8);
        register_product_ref = gdEquityCode;
        roll_register_product_ref = gdEquityCode;

        List<Map> shareList11 = gdConstructShareList(gdAccount1,issueAmount,0);
        List<Map> shareList12 = gdConstructShareList(gdAccount2,issueAmount,0, shareList11);
        List<Map> shareList13 = gdConstructShareList(gdAccount3,issueAmount,0, shareList12);
        List<Map> shareList14 = gdConstructShareList(gdAccount4,issueAmount,0,shareList13);

        response = uf.shareIssue(gdEquityCode,shareList14,false);

        //合约未执行 但是uri存证上报了
        sleepAndSaveInfo(3000);

        int afterBlockHeight2 = JSONObject.fromObject(store.GetHeight()).getInt("data");
        assertEquals("区块高度增加3，存证上链，合约不执行",afterBlockHeight + 3,afterBlockHeight2);//包含登记*2 + 发行存证*1

        String blockDetail = store.GetBlockByHeight(afterBlockHeight2);
        assertEquals("区块仅有一笔交易",1,JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").size());
        //确认交易是发行交易
        String getTXDetails = store.GetTxDetail(JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").getString(0));
        assertEquals(true,getTXDetails.contains(mapAddrRegObjId.get(gdAccount1 + "0").toString()));
        assertEquals(true,getTXDetails.contains(mapAddrRegObjId.get(gdAccount2 + "0").toString()));
        assertEquals(true,getTXDetails.contains(mapAddrRegObjId.get(gdAccount3 + "0").toString()));
        assertEquals(true,getTXDetails.contains(mapAddrRegObjId.get(gdAccount4 + "0").toString()));

        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("400",JSONObject.fromObject(query).getString("state"));

    }


    @Test
    public void shareIssueMultiIssue() throws Exception {
        log.info(registerInfo.toString());
        mapAddrRegObjId.clear(); //先清空map 这样后面map中拿到的就是此次发行的登记对象标识
        bSaveBuff = true;
        List<Map> shareList = gdConstructShareList(gdAccount1,issueAmount,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,issueAmount,0, shareList);

        uf.enterpriseReg(gdEquityCode,true);
        String query2 = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);

        sleepAndSaveInfo(3000);
        int beforeBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");

        log.info("发行");
        String response = gd.GDShareIssue(gdContractAddress,gdPlatfromKeyID,gdEquityCode,shareList2);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
        assertEquals("{\"txId\":\"\"}",JSONObject.fromObject(response).getString("data"));//当前实现方式

//        JSONObject jsonObject = JSONObject.fromObject(response);
//        String txId = jsonObject.getJSONObject("data").getString("txId");
//
//        String txDetail = store.GetTxDetail(txId);
//        assertEquals("200", net.sf.json.JSONObject.fromObject(txDetail).getString("state"));

        sleepAndSaveInfo(2000);

        //待校验的股东持股信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,5000,0,0,mapShareENCN().get("0"),respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2,5000,0,0,mapShareENCN().get("0"), respShareList);


        int afterBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");
        assertEquals("区块高度增加+1，无存证上链",beforeBlockHeight + 1,afterBlockHeight);

        String blockDetail = store.GetBlockByHeight(afterBlockHeight);
        assertEquals("区块仅有一笔交易",1,JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").size());
        //确认交易是发行交易
        String getTXDetails = store.GetTxDetail(JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").getString(0));
        JSONObject jsonObject1 = JSONObject.fromObject(getTXDetails).getJSONObject("data");
        assertEquals("2",jsonObject1.getJSONObject("header").getString("version"));
        assertEquals("3",jsonObject1.getJSONObject("header").getString("type"));
        assertEquals("43",jsonObject1.getJSONObject("header").getString("subType"));
        JSONObject jsonObjectWVM = jsonObject1.getJSONObject("wvm").getJSONObject("wvmContractTx");
        assertEquals("IssueToken",jsonObjectWVM.getJSONObject("arg").getString("method"));
        assertEquals("Sys_SmartTokenContract",jsonObjectWVM.getString("name"));


        //4.判断股东列表有变更
        String queryList = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(queryList).getString("state"));
        JSONArray dataShareListNow = JSONObject.fromObject(queryList).getJSONArray("data");
        assertEquals(respShareList2.size(),dataShareListNow.size());
        List<Map> getShareListNow = getShareListFromQueryNoZeroAcc(dataShareListNow);
        assertEquals(respShareList2.size(),getShareListNow.size());
        assertEquals(true,respShareList2.containsAll(getShareListNow) && getShareListNow.containsAll(respShareList2));

        //5.判断uuid是否存在合约中 方法待确认

        //6.判断所有登记对象ID是否存在OSS中 数据已经上报OSS 但是无存证
        assertEquals(true,minio.getFileFromMinIO(minIOEP,jgBucket,
                mapAddrRegObjId.get(gdAccount1 + "0").toString() + "/0","").contains("区域性股权市场跨链业务数据模型"));
        assertEquals(true,minio.getFileFromMinIO(minIOEP,jgBucket,
                mapAddrRegObjId.get(gdAccount2 + "0").toString() + "/0","").contains("区域性股权市场跨链业务数据模型"));



        List<Map> shareList13 = gdConstructShareList(gdAccount3,issueAmount,0);
        List<Map> shareList14 = gdConstructShareList(gdAccount4,issueAmount,0,shareList13);
        response = gd.GDShareIssue(gdContractAddress,gdPlatfromKeyID,gdEquityCode,shareList14);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));


        List<Map> shareList23 = gdConstructShareList(gdAccount5,issueAmount,0);
        bSaveBuff = false;
        response = gd.GDShareIssue(gdContractAddress,gdPlatfromKeyID,gdEquityCode,shareList23);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");

        sleepAndSaveInfo(3000);


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
        assertEquals(gdAccClientNo5,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo5 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount5 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":5000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo6);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

    }


}
