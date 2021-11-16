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
/***
 * 事务一致性异常场景测试用例 过户转让
 */

public class GDV2_ShareTransfer_UniqueId_Test {

    TestBuilder testBuilder= TestBuilder.getInstance();
    GuDeng gd =testBuilder.getGuDeng();
    Store store =testBuilder.getStore();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    GDCommonFunc gdCF = new GDCommonFunc();
    GDUnitFunc uf = new GDUnitFunc();
    GDBeforeCondition gdBF = new GDBeforeCondition();
    public static String bizNoTest = "test" + Random(12);
    long issueAmount = 1000;
    long increaseAmount = 1000;
    long lockAmount = 500;
    long recycleAmount = 100;
    long changeAmount = 500;
    long transferAmount = 500;
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

    }

    @Before
    public void IssueEquity()throws Exception{
        busUUID = "";
        bizNoTest = "test" + Random(12);
        gdEquityCode = "test_trf" + Random(8);

        //发行
        uf.commonIssuePP0(1000);//发行给账户1~4 股权性质对应 0 1 0 1


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



    @Test
    public void shareTransfer_SFNS()throws Exception{
        String subVerInit = gdCF.getObjectLatestVer(gdCompanyID);
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
        if(regObjType == 1){
        fromNow.put("register_subject_account_ref","SH" + gdAccClientNo1);}

        toNow.put("register_registration_object_id",tempObjIdTo);
        if(regObjType == 1){
        toNow.put("register_subject_account_ref","SH" + gdAccClientNo5);}

//        fromNow.put("register_transaction_ref",txRpObjId);
//        toNow.put("register_transaction_ref",txRpObjId);



        String tempObj = gdCompanyID;

        String query2 = gd.GDObjectQueryByVer(gdCompanyID,-1);

        fromNow.put("register_time","2021-03-19 10:00:00");
        int beforeBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");

        //执行交易 合约交易执行成功 但报送数据异常
        String response= gd.GDShareTransfer(keyId,fromAddr,transferAmount,toAddr,shareProperty,eqCode,txInfo,fromNow,toNow);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));
        assertEquals("监管数据校验失败",true,JSONObject.fromObject(response).getString("message").contains("数据格式校验失败"));

        int afterBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");
        assertEquals("区块高度不增加，无存证上链",beforeBlockHeight,afterBlockHeight);

//        String blockDetail = store.GetBlockByHeight(afterBlockHeight);
//        assertEquals("区块仅有一笔交易",1,JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").size());
//        //确认交易是发行交易
//        String getTXDetails = store.GetTxDetail(JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").getString(0));
//        JSONObject jsonObject1 = JSONObject.fromObject(getTXDetails).getJSONObject("data");
//        assertEquals("1",jsonObject1.getJSONObject("header").getString("version"));
//        assertEquals("1",jsonObject1.getJSONObject("header").getString("type"));
//        assertEquals("11",jsonObject1.getJSONObject("header").getString("subType"));


        //5.判断uuid是否存在合约中 方法待确认

        //6.判断所有登记对象ID是否存在OSS中
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,txRpObjId + "/0",""));
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,tempObjIdFrom + "/0",""));
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,tempObjIdTo + "/0",""));
        assertEquals(false,minio.getFileFromMinIO(minIOEP,jgBucket,
                gdCompanyID + "/" + (Integer.valueOf(subVerInit)+1),"").contains("body"));


        //执行交易  使用相同的uuid再次执行
        busUUID = tempUUID;
        fromNow.put("register_time",time2);
        response= gd.GDShareTransfer(keyId,fromAddr,transferAmount,toAddr,shareProperty,eqCode,txInfo,fromNow,toNow);
        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
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

        log.info("================================检查存证数据格式化《开始》================================");

        Map uriInfo = gdCF.getJGURIStoreHash(txId,conJGFileName(tempObjIdFrom,"0"),1);

        log.info("检查股权性质变更存证登记格式化及信息内容与传入一致");
        String regVer = "0" ;//gdCF.getObjectLatestVer(account_subject_ref);
        String txRpVer = "0" ;//gdCF.getObjectLatestVer(account_subject_ref);
        String subVer = gdCF.getObjectLatestVer(gdCompanyID);

        //获取链上mini url的存证信息 并检查是否包含uri信息 每个登记都是新的 则都是0
        String regfileName1 = conJGFileName(tempObjIdFrom,regVer);
        String regfileName2 = conJGFileName(tempObjIdTo,regVer);
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
        fromNow.put("content",gdCF.constructContentTreeMap(regType,tempObjIdFrom,regVer,"create",String.valueOf(ts5)));
        log.info("检查登记存证信息内容与传入一致\n" + fromNow.toString() + "\n" + getRegInfo1.toString());
        bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(fromNow,regType)),replaceCertain(getRegInfo1.toString()));
        assertEquals("from检查数据是否一致" ,true,bSame);
//        log.info("from登记检查数据是否一致" + bSame);

        register_subject_account_ref = "SH" + gdAccClientNo5;
        //填充header content字段
        toNow.put("content",gdCF.constructContentTreeMap(regType,tempObjIdTo,regVer,"create",String.valueOf(ts5)));
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

        //查询股东持股情况 无当前股权代码信息
        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        assertEquals(gdAccClientNo1,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));

        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo1 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount1 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":500,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(gdAccClientNo2,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo2 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount2 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo3);
        assertEquals(gdAccClientNo3,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo3 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount3 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo4);
        assertEquals(gdAccClientNo4,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo4 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount4 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo5);
        assertEquals(gdAccClientNo5,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo5 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount5 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":500,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo6);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));
    }


    @Test
    public void shareTransfer_SFNFNS()throws Exception{
        String subVerInit = gdCF.getObjectLatestVer(gdCompanyID);
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
        if(regObjType == 1){
        fromNow.put("register_subject_account_ref","SH" + gdAccClientNo1);}

        toNow.put("register_registration_object_id",tempObjIdTo);
        if(regObjType == 1){
        toNow.put("register_subject_account_ref","SH" + gdAccClientNo5);}

//        fromNow.put("register_transaction_ref",txRpObjId);
//        toNow.put("register_transaction_ref",txRpObjId);



        String tempObj = gdCompanyID;

        String query2 = gd.GDObjectQueryByVer(gdCompanyID,-1);

        fromNow.put("register_time","2021-03-19 10:00:00");
        int beforeBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");

        //执行交易 合约交易执行成功 但报送数据异常
        String response= gd.GDShareTransfer(keyId,fromAddr,transferAmount,toAddr,shareProperty,eqCode,txInfo,fromNow,toNow);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));
        assertEquals("监管数据校验失败",true,JSONObject.fromObject(response).getString("message").contains("数据格式校验失败"));

        int afterBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");
        assertEquals("区块高度不增加，无存证上链",beforeBlockHeight,afterBlockHeight);

//        String blockDetail = store.GetBlockByHeight(afterBlockHeight);
//        assertEquals("区块仅有一笔交易",1,JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").size());
//        //确认交易是发行交易
//        String getTXDetails = store.GetTxDetail(JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").getString(0));
//        JSONObject jsonObject1 = JSONObject.fromObject(getTXDetails).getJSONObject("data");
//        assertEquals("1",jsonObject1.getJSONObject("header").getString("version"));
//        assertEquals("1",jsonObject1.getJSONObject("header").getString("type"));
//        assertEquals("11",jsonObject1.getJSONObject("header").getString("subType"));


        //5.判断uuid是否存在合约中 方法待确认

        //6.判断所有登记对象ID是否存在OSS中
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,txRpObjId + "/0",""));
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,tempObjIdFrom + "/0",""));
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,tempObjIdTo + "/0",""));
        assertEquals(false,minio.getFileFromMinIO(minIOEP,jgBucket,
                gdCompanyID + "/" + (Integer.valueOf(subVerInit)+1),"").contains("body"));


        //执行交易  使用相同的uuid再次执行 但校验数据仍旧失败
        busUUID = tempUUID;

        fromNow.put("register_time","");
        //执行交易 合约交易执行成功 但报送数据异常
        response= gd.GDShareTransfer(keyId,fromAddr,transferAmount,toAddr,shareProperty,eqCode,txInfo,fromNow,toNow);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));
        sleepAndSaveInfo(2000);

        afterBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");
        assertEquals("区块高度无增加，无存证上链",beforeBlockHeight,afterBlockHeight);

        //6.判断所有登记对象ID是否存在OSS中
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,txRpObjId + "/0",""));
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,tempObjIdFrom + "/0",""));
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,tempObjIdTo + "/0",""));
//        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,gdCompanyID + "/" + (Integer.valueOf(subVerInit)+1),""));


        //执行交易  使用相同的uuid再次执行
        fromNow.put("register_time",time2);
        response= gd.GDShareTransfer(keyId,fromAddr,transferAmount,toAddr,shareProperty,eqCode,txInfo,fromNow,toNow);
        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
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

        log.info("================================检查存证数据格式化《开始》================================");

        Map uriInfo = gdCF.getJGURIStoreHash(txId,conJGFileName(tempObjIdFrom,"0"),1);

        log.info("检查股权性质变更存证登记格式化及信息内容与传入一致");
        String regVer = "0" ;//gdCF.getObjectLatestVer(account_subject_ref);
        String txRpVer = "0" ;//gdCF.getObjectLatestVer(account_subject_ref);
        String subVer = gdCF.getObjectLatestVer(gdCompanyID);

        //获取链上mini url的存证信息 并检查是否包含uri信息 每个登记都是新的 则都是0
        String regfileName1 = conJGFileName(tempObjIdFrom,regVer);
        String regfileName2 = conJGFileName(tempObjIdTo,regVer);
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
        fromNow.put("content",gdCF.constructContentTreeMap(regType,tempObjIdFrom,regVer,"create",String.valueOf(ts5)));
        log.info("检查登记存证信息内容与传入一致\n" + fromNow.toString() + "\n" + getRegInfo1.toString());
        bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(fromNow,regType)),replaceCertain(getRegInfo1.toString()));
        assertEquals("from检查数据是否一致" ,true,bSame);
//        log.info("from登记检查数据是否一致" + bSame);

        register_subject_account_ref = "SH" + gdAccClientNo5;
        //填充header content字段
        toNow.put("content",gdCF.constructContentTreeMap(regType,tempObjIdTo,regVer,"create",String.valueOf(ts5)));
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


        //查询股东持股情况 无当前股权代码信息
        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        assertEquals(gdAccClientNo1,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));

        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo1 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount1 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":500,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(gdAccClientNo2,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo2 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount2 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo3);
        assertEquals(gdAccClientNo3,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo3 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount3 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo4);
        assertEquals(gdAccClientNo4,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo4 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount4 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo5);
        assertEquals(gdAccClientNo5,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo5 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount5 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":500,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo6);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));
    }

    @Test
    public void shareTransfer_SFNS02()throws Exception{
        String subVerInit = gdCF.getObjectLatestVer(gdCompanyID);
        String keyId = gdAccountKeyID1;
        String fromAddr = gdAccount1;
        String toAddr = gdAccount5;
        int shareProperty = 0;
        String eqCode = gdEquityCode;

        register_event_type = 2;//交易登记
        //交易报告数据
        Map txInfo = gdBF.init04TxInfo();
        String txRpObjId = "txReport" + Random(128);
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
        if(regObjType == 1){
        fromNow.put("register_subject_account_ref","SH" + gdAccClientNo1);}

        toNow.put("register_registration_object_id",tempObjIdTo);
        if(regObjType == 1){
        toNow.put("register_subject_account_ref","SH" + gdAccClientNo5);}

//        fromNow.put("register_transaction_ref",txRpObjId);
//        toNow.put("register_transaction_ref",txRpObjId);



        String tempObj = gdCompanyID;

        String query2 = gd.GDObjectQueryByVer(gdCompanyID,-1);

        int beforeBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");

        //执行交易 合约交易执行成功 但报送数据异常
        String response= gd.GDShareTransfer(keyId,fromAddr,transferAmount,toAddr,shareProperty,eqCode,txInfo,fromNow,toNow);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));
        assertEquals("监管数据校验失败",true,JSONObject.fromObject(response).getString("message").contains("Data too long for column 'object_id' at row 1"));

        int afterBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");
        assertEquals("区块高度不增加，无存证上链",beforeBlockHeight,afterBlockHeight);

//        String blockDetail = store.GetBlockByHeight(afterBlockHeight);
//        assertEquals("区块仅有一笔交易",1,JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").size());
//        //确认交易是发行交易
//        String getTXDetails = store.GetTxDetail(JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").getString(0));
//        JSONObject jsonObject1 = JSONObject.fromObject(getTXDetails).getJSONObject("data");
//        assertEquals("1",jsonObject1.getJSONObject("header").getString("version"));
//        assertEquals("1",jsonObject1.getJSONObject("header").getString("type"));
//        assertEquals("11",jsonObject1.getJSONObject("header").getString("subType"));


        //5.判断uuid是否存在合约中 方法待确认

        //6.判断所有登记对象ID是否存在OSS中
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,txRpObjId + "/0",""));
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,tempObjIdFrom + "/0",""));
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,tempObjIdTo + "/0",""));
        assertEquals(false,minio.getFileFromMinIO(minIOEP,jgBucket,
                gdCompanyID + "/" + (Integer.valueOf(subVerInit)+1),"").contains("body"));


        //执行交易  使用相同的uuid再次执行
        busUUID = tempUUID;

        txRpObjId = "txReport" + Random(6);
        txInfo.put("transaction_object_id",txRpObjId);
        register_transaction_ref = txRpObjId;//登记引用的是交易报告的对象标识
        fromNow = gdBF.init05RegInfo();
        toNow = gdBF.init05RegInfo();
        fromNow.put("register_registration_object_id",tempObjIdFrom);
        if(regObjType == 1){
        fromNow.put("register_subject_account_ref","SH" + gdAccClientNo1);}
        toNow.put("register_registration_object_id",tempObjIdTo);
        if(regObjType == 1){
        toNow.put("register_subject_account_ref","SH" + gdAccClientNo5);}

        response= gd.GDShareTransfer(keyId,fromAddr,transferAmount,toAddr,shareProperty,eqCode,txInfo,fromNow,toNow);
        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
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

        log.info("================================检查存证数据格式化《开始》================================");

        Map uriInfo = gdCF.getJGURIStoreHash(txId,conJGFileName(tempObjIdFrom,"0"),1);

        log.info("检查股权性质变更存证登记格式化及信息内容与传入一致");
        String regVer = "0" ;//gdCF.getObjectLatestVer(account_subject_ref);
        String txRpVer = "0" ;//gdCF.getObjectLatestVer(account_subject_ref);
        String subVer = gdCF.getObjectLatestVer(gdCompanyID);

        //获取链上mini url的存证信息 并检查是否包含uri信息 每个登记都是新的 则都是0
        String regfileName1 = conJGFileName(tempObjIdFrom,regVer);
        String regfileName2 = conJGFileName(tempObjIdTo,regVer);
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
        fromNow.put("content",gdCF.constructContentTreeMap(regType,tempObjIdFrom,regVer,"create",String.valueOf(ts5)));
        log.info("检查登记存证信息内容与传入一致\n" + fromNow.toString() + "\n" + getRegInfo1.toString());
        bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(fromNow,regType)),replaceCertain(getRegInfo1.toString()));
        assertEquals("from检查数据是否一致" ,true,bSame);
//        log.info("from登记检查数据是否一致" + bSame);

        register_subject_account_ref = "SH" + gdAccClientNo5;
        //填充header content字段
        toNow.put("content",gdCF.constructContentTreeMap(regType,tempObjIdTo,regVer,"create",String.valueOf(ts5)));
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

        //查询股东持股情况 无当前股权代码信息
        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        assertEquals(gdAccClientNo1,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));

        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo1 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount1 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":500,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(gdAccClientNo2,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo2 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount2 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo3);
        assertEquals(gdAccClientNo3,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo3 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount3 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo4);
        assertEquals(gdAccClientNo4,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo4 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount4 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo5);
        assertEquals(gdAccClientNo5,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo5 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount5 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":500,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo6);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));
    }


    @Test
    public void shareTransfer_SameUniqueIdSameRequest()throws Exception{
        String subVerInit = gdCF.getObjectLatestVer(gdCompanyID);
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
        if(regObjType == 1){
        fromNow.put("register_subject_account_ref","SH" + gdAccClientNo1);}

        toNow.put("register_registration_object_id",tempObjIdTo);
        if(regObjType == 1){
        toNow.put("register_subject_account_ref","SH" + gdAccClientNo5);}

//        fromNow.put("register_transaction_ref",txRpObjId);
//        toNow.put("register_transaction_ref",txRpObjId);



        String tempObj = gdCompanyID;

        String query2 = gd.GDObjectQueryByVer(gdCompanyID,-1);

        int beforeBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");

        //执行交易 合约交易执行成功 数据报送成功
        String response= gd.GDShareTransfer(keyId,fromAddr,transferAmount,toAddr,shareProperty,eqCode,txInfo,fromNow,toNow);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));

        int afterBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");
        assertEquals("区块高度仅增加1，无存证上链",beforeBlockHeight + 2,afterBlockHeight);

        String blockDetail = store.GetBlockByHeight(afterBlockHeight - 1);
        assertEquals("区块仅有一笔交易",1,JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").size());
        //确认交易是发行交易
        String getTXDetails = store.GetTxDetail(JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").getString(0));
        JSONObject jsonObject1 = JSONObject.fromObject(getTXDetails).getJSONObject("data");
        assertEquals("1",jsonObject1.getJSONObject("header").getString("version"));
        assertEquals("1",jsonObject1.getJSONObject("header").getString("type"));
        assertEquals("11",jsonObject1.getJSONObject("header").getString("subType"));

        blockDetail = store.GetBlockByHeight(afterBlockHeight);
        assertEquals("区块仅有一笔交易",1,JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").size());
        //确认交易是发行交易
        getTXDetails = store.GetTxDetail(JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").getString(0));
        assertEquals(true,getTXDetails.contains(tempObjIdFrom));
        assertEquals(true,getTXDetails.contains(tempObjIdTo));
        assertEquals(true,getTXDetails.contains(txRpObjId));
        assertEquals(true,getTXDetails.contains(gdCompanyID));



        //执行交易  使用相同的uuid再次执行 相同的请求
        busUUID = tempUUID;
        response= gd.GDShareTransfer(keyId,fromAddr,transferAmount,toAddr,shareProperty,eqCode,txInfo,fromNow,toNow);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        sleepAndSaveInfo(3000);
        afterBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");
        assertEquals("区块高度无增加，无存证上链",beforeBlockHeight + 2,afterBlockHeight);


        //查询股东持股情况 无当前股权代码信息
        String query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        assertEquals(gdAccClientNo1,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));

        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo1 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount1 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":500,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(gdAccClientNo2,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo2 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount2 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo3);
        assertEquals(gdAccClientNo3,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo3 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount3 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo4);
        assertEquals(gdAccClientNo4,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo4 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount4 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo5);
        assertEquals(gdAccClientNo5,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo5 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount5 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":500,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo6);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));
    }

    @Test
    public void shareTransfer_SameUniqueIdDiffRequest()throws Exception{
        String subVerInit = gdCF.getObjectLatestVer(gdCompanyID);
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
        if(regObjType == 1){
        fromNow.put("register_subject_account_ref","SH" + gdAccClientNo1);}

        toNow.put("register_registration_object_id",tempObjIdTo);
        if(regObjType == 1){
        toNow.put("register_subject_account_ref","SH" + gdAccClientNo5);}

//        fromNow.put("register_transaction_ref",txRpObjId);
//        toNow.put("register_transaction_ref",txRpObjId);



        String tempObj = gdCompanyID;

        String query2 = gd.GDObjectQueryByVer(gdCompanyID,-1);

        int beforeBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");

        //执行交易 合约交易执行成功 正常报送
        String response= gd.GDShareTransfer(keyId,fromAddr,transferAmount,toAddr,shareProperty,eqCode,txInfo,fromNow,toNow);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));

        int afterBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");
        assertEquals("区块高度仅增加2，存证上链",beforeBlockHeight + 2,afterBlockHeight);

        String blockDetail = store.GetBlockByHeight(afterBlockHeight - 1);
        assertEquals("区块仅有一笔交易",1,JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").size());
        //确认交易是发行交易
        String getTXDetails = store.GetTxDetail(JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").getString(0));
        JSONObject jsonObject1 = JSONObject.fromObject(getTXDetails).getJSONObject("data");
        assertEquals("1",jsonObject1.getJSONObject("header").getString("version"));
        assertEquals("1",jsonObject1.getJSONObject("header").getString("type"));
        assertEquals("11",jsonObject1.getJSONObject("header").getString("subType"));

        blockDetail = store.GetBlockByHeight(afterBlockHeight);
        assertEquals("区块仅有一笔交易",1,JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").size());
        //确认交易是发行交易
        getTXDetails = store.GetTxDetail(JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").getString(0));
        assertEquals(true,getTXDetails.contains(tempObjIdFrom));
        assertEquals(true,getTXDetails.contains(tempObjIdTo));
        assertEquals(true,getTXDetails.contains(txRpObjId));
        assertEquals(true,getTXDetails.contains(gdCompanyID));

        //执行交易  使用相同的uuid执行不同的业务
        busUUID = tempUUID;

        txRpObjId = "txReport" + Random(6);
        txInfo.put("transaction_object_id",txRpObjId);
        register_transaction_ref = txRpObjId;//登记引用的是交易报告的对象标识
        //登记数据
        tempObjIdFrom = "reg" + mapAccAddr.get(gdAccount1).toString() + Random(4);
        tempObjIdTo = "reg" + mapAccAddr.get(gdAccount6).toString() + Random(4);
        fromNow = gdBF.init05RegInfo();
        toNow = gdBF.init05RegInfo();
        fromNow.put("register_registration_object_id",tempObjIdFrom);
        if(regObjType == 1){
        fromNow.put("register_subject_account_ref","SH" + gdAccClientNo1);}
        toNow.put("register_registration_object_id",tempObjIdTo);
        if(regObjType == 1){
        toNow.put("register_subject_account_ref","SH" + gdAccClientNo6);}

        toAddr = gdAccount6;
        response = gd.GDShareTransfer(keyId,fromAddr,transferAmount,toAddr,shareProperty,eqCode,txInfo,fromNow,toNow);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));

        sleepAndSaveInfo(3000);

        int afterBlockHeight2 = JSONObject.fromObject(store.GetHeight()).getInt("data");
        assertEquals("区块高度增加1，存证上链，合约不执行",afterBlockHeight + 1,afterBlockHeight2);

        blockDetail = store.GetBlockByHeight(afterBlockHeight2);
        assertEquals("区块仅有一笔交易",1,JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").size());
        //确认交易是发行交易
        getTXDetails = store.GetTxDetail(JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").getString(0));
        assertEquals(true,getTXDetails.contains(tempObjIdFrom));
        assertEquals(true,getTXDetails.contains(tempObjIdTo));
        assertEquals(true,getTXDetails.contains(txRpObjId));
        assertEquals(false,getTXDetails.contains(gdCompanyID));

    }

}
