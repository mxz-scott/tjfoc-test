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
public class GDV2_JGFormat_Part6_TransferNotSendData_Test {

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
    ArrayList<String> txList = new ArrayList<>();

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
        start = (new Date()).getTime();

    }

    @AfterClass
    public static void reset(){
        bNotStoreSuperviseInfo = false;//将报送flag重置为默认值
    }

    @Before
    public void resetVar(){


        register_event_type = "1";//非交易登记
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
    public void TC01_shareIssue() throws Exception {
        log.info(registerInfo.toString());
        mapAddrRegObjId.clear(); //先清空map 这样后面map中拿到的就是此次发行的登记对象标识

        List<Map> shareList = gdConstructShareList(gdAccount1,issueAmount,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,issueAmount,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,issueAmount,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,issueAmount,0,shareList3);

        String response= uf.shareIssue(gdEquityCode,shareList4,false);
        JSONObject jsonObject=JSONObject.fromObject(response);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        String txId = jsonObject.getJSONObject("data").getString("txId");
        txList.add(txId);

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
    }


    @Test
    public void TC021_shareTransfer_NotSendJGData()throws Exception{
        bNotStoreSuperviseInfo = true;

        String keyId = gdAccountKeyID1;
        String fromAddr = gdAccount1;
        String toAddr = gdAccount5;
        int shareProperty = 0;
        String eqCode = gdEquityCode;

        register_event_type = "2";//交易登记
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
        String response= gd.GDShareTransfer(keyId,fromAddr,transferAmount,toAddr,shareProperty,eqCode,txInfo,fromNow,toNow);
        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        String txDetail = store.GetTxDetail(txId);
        assertEquals("200", JSONObject.fromObject(txDetail).getString("state"));

        txList.add(txId);

        sleepAndSaveInfo(3000);

        //直接从minio上获取报送数据文件信息 没有报送
        MinIOOperation minio = new MinIOOperation();
        assertEquals("未获取到更新的登记对象版本信息",true,
                minio.getFileFromMinIO(minIOEP,jgBucket,tempObjIdFrom + "/0" ,"").contains("错误"));
        assertEquals("未获取到更新的登记对象版本信息",true,
                minio.getFileFromMinIO(minIOEP,jgBucket,tempObjIdTo + "/0" ,"").contains("错误"));
        assertEquals("未获取到交易报告对象版本信息",true,
                minio.getFileFromMinIO(minIOEP,jgBucket,txRpObjId + "/0" ,"").contains("错误"));
        assertEquals("未获取到更新的主体对象版本信息",true,
                minio.getFileFromMinIO(minIOEP,jgBucket,gdCompanyID + "/1" ,"").contains("错误"));



        //检查交易详情
        JSONObject jsonObject1 = JSONObject.fromObject(txDetail).getJSONObject("data");
        assertEquals("1",jsonObject1.getJSONObject("header").getString("version"));
        assertEquals("1",jsonObject1.getJSONObject("header").getString("type"));
        assertEquals("11",jsonObject1.getJSONObject("header").getString("subType"));


        String getTotal = enterpriseSubjectInfo.get("subject_shareholders_number").toString();
        int oldTotal = Integer.parseInt(getTotal);
        enterpriseSubjectInfo.put("subject_shareholders_number",oldTotal);     //变更总股东数不变更

        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200", JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");



        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,4000,0,0,mapShareENCN().get("0"),respShareList);
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
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":4000,\"lockAmount\":0}"));

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
    public void TC022_shareTransfer_NotSendJGData_NotCheckJGData()throws Exception{
        bNotStoreSuperviseInfo = true;

        String keyId = gdAccountKeyID1;
        String fromAddr = gdAccount1;
        String toAddr = gdAccount5;
        int shareProperty = 0;
        String eqCode = gdEquityCode;

        register_event_type = "2";//交易登记
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
        String response= gd.GDShareTransfer(keyId,fromAddr,transferAmount,toAddr,shareProperty,eqCode,null,null,null);
        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        String txDetail = store.GetTxDetail(txId);
        assertEquals("200", JSONObject.fromObject(txDetail).getString("state"));

        txList.add(txId);
        sleepAndSaveInfo(3000);

        //直接从minio上获取报送数据文件信息 没有报送
        MinIOOperation minio = new MinIOOperation();
        assertEquals("未获取到更新的登记对象版本信息",true,
                minio.getFileFromMinIO(minIOEP,jgBucket,tempObjIdFrom + "/0" ,"").contains("错误"));
        assertEquals("未获取到更新的登记对象版本信息",true,
                minio.getFileFromMinIO(minIOEP,jgBucket,tempObjIdTo + "/0" ,"").contains("错误"));
        assertEquals("未获取到交易报告对象版本信息",true,
                minio.getFileFromMinIO(minIOEP,jgBucket,txRpObjId + "/0" ,"").contains("错误"));
        assertEquals("未获取到更新的主体对象版本信息",true,
                minio.getFileFromMinIO(minIOEP,jgBucket,gdCompanyID + "/1" ,"").contains("错误"));



        //检查交易详情
        JSONObject jsonObject1 = JSONObject.fromObject(txDetail).getJSONObject("data");
        assertEquals("1",jsonObject1.getJSONObject("header").getString("version"));
        assertEquals("1",jsonObject1.getJSONObject("header").getString("type"));
        assertEquals("11",jsonObject1.getJSONObject("header").getString("subType"));


        String getTotal = enterpriseSubjectInfo.get("subject_shareholders_number").toString();
        int oldTotal = Integer.parseInt(getTotal);
        enterpriseSubjectInfo.put("subject_shareholders_number",oldTotal);     //变更总股东数不变更

        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200", JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");



        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,3000,0,0,mapShareENCN().get("0"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount5,2000,0,0,mapShareENCN().get("0"),respShareList);
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
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":3000,\"lockAmount\":0}"));

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
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":2000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo6);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));
    }


    @Test
    public void TC023_shareTransfer_NoJGData()throws Exception{
        bNotStoreSuperviseInfo = true;

        String keyId = gdAccountKeyID1;
        String fromAddr = gdAccount1;
        String toAddr = gdAccount5;
        int shareProperty = 0;
        String eqCode = gdEquityCode;


        Map<String, Object> mapSimple = new HashMap<>();
        mapSimple.put("keyId", keyId);
        mapSimple.put("fromAddress", fromAddr);
        mapSimple.put("amount", 1000);
        mapSimple.put("toAddress", toAddr);
        mapSimple.put("shareProperty", shareProperty);
        mapSimple.put("equityCode", eqCode);
        mapSimple.put("notStoreSuperviseInfo",true);

        if(bUseUUID) {
            //20210319 新增业务唯一标识
            tempUUID = Random(26);
            if (!busUUID.isEmpty()) tempUUID = busUUID;
            mapSimple.put("uniqueId", tempUUID);
        }

        String tempObj = gdCompanyID;

        String query2 = gd.GDObjectQueryByVer(gdCompanyID,-1);

        //执行交易
        String response= gd.GDShareTransfer(mapSimple);
        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        String txDetail = store.GetTxDetail(txId);
        assertEquals("200", JSONObject.fromObject(txDetail).getString("state"));

        txList.add(txId);

        sleepAndSaveInfo(3000);

        //直接从minio上获取报送数据文件信息 没有报送
        MinIOOperation minio = new MinIOOperation();
        assertEquals("未获取到更新的主体对象版本信息",true,
                minio.getFileFromMinIO(minIOEP,jgBucket,gdCompanyID + "/1" ,"").contains("错误"));



        //检查交易详情
        JSONObject jsonObject1 = JSONObject.fromObject(txDetail).getJSONObject("data");
        assertEquals("1",jsonObject1.getJSONObject("header").getString("version"));
        assertEquals("1",jsonObject1.getJSONObject("header").getString("type"));
        assertEquals("11",jsonObject1.getJSONObject("header").getString("subType"));


        String getTotal = enterpriseSubjectInfo.get("subject_shareholders_number").toString();
        int oldTotal = Integer.parseInt(getTotal);
        enterpriseSubjectInfo.put("subject_shareholders_number",oldTotal);     //变更总股东数不变更

        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200", JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");



        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,2000,0,0,mapShareENCN().get("0"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount5,3000,0,0,mapShareENCN().get("0"),respShareList);
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
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":2000,\"lockAmount\":0}"));

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
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":3000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo6);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));
    }

    @Test
    public void TC024_shareTransfer_NoJGDataNotEnough()throws Exception{
        bNotStoreSuperviseInfo = true;

        String keyId = gdAccountKeyID1;
        String fromAddr = gdAccount1;
        String toAddr = gdAccount5;
        int shareProperty = 0;
        String eqCode = gdEquityCode;


        Map<String, Object> mapSimple = new HashMap<>();
        mapSimple.put("keyId", keyId);
        mapSimple.put("fromAddress", fromAddr);
        mapSimple.put("amount", 5000);
        mapSimple.put("toAddress", toAddr);
        mapSimple.put("shareProperty", shareProperty);
        mapSimple.put("equityCode", eqCode);
        mapSimple.put("notStoreSuperviseInfo",true);

        if(bUseUUID) {
            //20210319 新增业务唯一标识
            tempUUID = Random(26);
            if (!busUUID.isEmpty()) tempUUID = busUUID;
            mapSimple.put("uniqueId", tempUUID);
        }

        String tempObj = gdCompanyID;

        String query2 = gd.GDObjectQueryByVer(gdCompanyID,-1);

        //执行交易
        String response = gd.GDShareTransfer(mapSimple);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));

        sleepAndSaveInfo(3000);

        //直接从minio上获取报送数据文件信息 没有报送
        MinIOOperation minio = new MinIOOperation();
        assertEquals("未获取到更新的主体对象版本信息",true,
                minio.getFileFromMinIO(minIOEP,jgBucket,gdCompanyID + "/1" ,"").contains("错误"));


        String getTotal = enterpriseSubjectInfo.get("subject_shareholders_number").toString();
        int oldTotal = Integer.parseInt(getTotal);
        enterpriseSubjectInfo.put("subject_shareholders_number",oldTotal);     //变更总股东数不变更

        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200", JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");



        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,2000,0,0,mapShareENCN().get("0"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount5,3000,0,0,mapShareENCN().get("0"),respShareList);
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
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":2000,\"lockAmount\":0}"));

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
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":3000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo6);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));
    }

    @Test
    public void checkTxReport()throws Exception {
        end = (new Date()).getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String sdStart = sdf.format(start); // 时间戳转换日期
        String sdEnd = sdf.format(end); // 时间戳转换日期
        String response = gd.GDGetTxReportInfo("4", "", sdStart, sdEnd);

        for (int i = 0; i < txList.size(); i++) {
            assertEquals("包含指定交易", true, response.contains(txList.get(i)));
        }
    }

}
