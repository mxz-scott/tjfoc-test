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
public class GDV2_ShareIncrease_UniqueId_Test {

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

        //发行
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


    //交易报告填写且类型为1 发行融资 即需要报送交易报告
    @Test
    public void TC09_shareIncrease_SFNS03() throws Exception {

        log.info(registerInfo.toString());
        mapAddrRegObjId.clear(); //先清空map 这样后面map中拿到的就是此次发行的登记对象标识

        log.info("增发前查询机构主体信息");
        String query2 = gd.GDObjectQueryByVer(gdCompanyID,-1);
        int totalMembersBf = JSONObject.fromObject(query2).getJSONObject("data").getJSONObject("body"
        ).getJSONObject("subject_information").getJSONObject("organization_subject_information"
        ).getJSONObject("basic_information_of_enterprise").getJSONObject("basic_information_description"
        ).getInt("subject_shareholders_number");

        String subVerInit = gdCF.getObjectLatestVer(gdCompanyID);//获取初始主体版本

        register_event_type = "2";//非交易登记

        String eqCode = gdEquityCode;
        String reason = "股份分红";
        String txObjId = "4increaseObj" + Random(6);

        Map eqProd = gdBF.init03EquityProductInfo();
        Map txInfo = gdBF.init04TxInfo();
        txInfo.put("transaction_object_id",txObjId);
        register_transaction_ref = txObjId; //此处为发行融资 设置登记引用接口中的交易报告

        txInfo.put("transaction_create_time",""); //设置transaction_create_time为空 使得发行成功 报送失败

        log.info("发行主体版本  " + gdCF.getObjectLatestVer(gdCompanyID));

        List<Map> shareList = gdConstructShareList(gdAccount1,increaseAmount,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,increaseAmount,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount6,increaseAmount,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount5,increaseAmount,0, shareList3);

        sleepAndSaveInfo(3000);
        int beforeBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");

        String response = gd.GDShareIncrease(gdPlatfromKeyID,eqCode,shareList4,reason, eqProd,txInfo);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        busUUID = tempUUID;

        sleepAndSaveInfo(4000);
        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,6000,0,0,mapShareENCN().get("0"),respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2,6000,0,0,mapShareENCN().get("0"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3,5000,0,0,mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4,5000,0,0,mapShareENCN().get("0"), respShareList3);
        respShareList4 = gdConstructQueryShareList(gdAccount5,1000,0,0,mapShareENCN().get("0"), respShareList4);
        respShareList4 = gdConstructQueryShareList(gdAccount6,1000,0,0,mapShareENCN().get("0"), respShareList4);

        System.out.println(respShareList4);
        int afterBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");
        assertEquals("区块高度仅增加1，无存证上链",beforeBlockHeight + 1,afterBlockHeight);

        String blockDetail = store.GetBlockByHeight(afterBlockHeight);
        assertEquals("区块仅有一笔交易",1,JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").size());
        //确认交易是增发交易
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
        assertEquals(respShareList4.size(),dataShareListNow.size());
        List<Map> getShareListNow = getShareListFromQueryNoZeroAcc(dataShareListNow);
        assertEquals(respShareList4.size(),getShareListNow.size());
        assertEquals(true,respShareList4.containsAll(getShareListNow) && getShareListNow.containsAll(respShareList4));

        //5.判断uuid是否存在合约中 方法待确认

        //6.判断所有登记对象ID是否存在OSS中
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,txObjId + "/0",""));
        assertEquals(true,minio.getFileFromMinIO(minIOEP,jgBucket,
                gdCompanyID + "/" + (Integer.valueOf(subVerInit) + 1),"").contains("区域性股权市场跨链业务数据模型"));

//        //7.判断数据库中是否存储登记对象ID
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId1));
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId2));
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId3));
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId4));

        //重复执行 使用上次的uuid
        txInfo.put("transaction_create_time",time4); //设置transaction_create_time正常 使得报送可以成功
        response= gd.GDShareIncrease(gdPlatfromKeyID,eqCode,shareList4,reason, eqProd,txInfo);

        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        String txDetail = store.GetTxDetail(txId);
        assertEquals("200", JSONObject.fromObject(txDetail).getString("state"));

        busUUID = "";

        txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        txDetail = store.GetTxDetail(txId);
        assertEquals("200", JSONObject.fromObject(txDetail).getString("state"));

        //检查各个查询对象返回信息中不包含敏感词
        assertEquals("不包含登记数据敏感词",true,gdCF.chkSensitiveWord(txDetail,subjectType));
        assertEquals("不包含登记数据敏感词",true,gdCF.chkSensitiveWord(txDetail,regType));
        assertEquals("不包含交易报告敏感词",true,gdCF.chkSensitiveWord(txDetail,txrpType));

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

        String newEqProdVer = gdCF.getObjectLatestVer(gdEquityCode);

        log.info(uriInfo.get("storeData").toString());
        product_issuer_subject_ref = gdCompanyID;

        Map getProInfo = gdCF.constructJGDataFromStr(conJGFileName(gdEquityCode, newEqProdVer), prodType, "1");
        eqProd.put("content",gdCF.constructContentTreeMap(prodType, gdEquityCode, newEqProdVer, "update", String.valueOf(ts3)));
        log.info("检查产品存证信息内容与传入一致\n" + eqProd.toString() + "\n" + getProInfo.toString());
        bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(eqProd, prodType)), replaceCertain(getProInfo.toString()));
        assertEquals("检查增发产品是否一致" ,true,bSame);
        log.info("检查增发产品是否一致:" + bSame);


        product_issuer_subject_ref = gdCompanyID;
        transaction_custody_product_ref = gdEquityCode;

        txInfo.put("content",gdCF.constructContentTreeMap(txrpType, txObjId, "0", "create", String.valueOf(ts4)));
        Map getTxRpInfo = gdCF.constructJGDataFromStr(conJGFileName(txObjId, "0"), txrpType, "");
        bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(txInfo,txrpType)),replaceCertain(getTxRpInfo.toString()));
        assertEquals("检查增发交易报告数据是否一致" ,true,bSame);
        log.info("检查增发交易报告数据是否一致:" + bSame);


        query2 = gd.GDObjectQueryByVer(gdCompanyID,-1);
        int totalMembers = JSONObject.fromObject(query2).getJSONObject("data").getJSONObject("body"
        ).getJSONObject("subject_information").getJSONObject("organization_subject_information"
        ).getJSONObject("basic_information_of_enterprise").getJSONObject("basic_information_description"
        ).getInt("subject_shareholders_number");
        assertEquals(totalMembersBf + 2,totalMembers);

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
        respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,6000,0,0,mapShareENCN().get("0"),respShareList);
        respShareList2 = gdConstructQueryShareList(gdAccount2,6000,0,0,mapShareENCN().get("0"), respShareList);
        respShareList3 = gdConstructQueryShareList(gdAccount3,5000,0,0,mapShareENCN().get("0"), respShareList2);
        respShareList4 = gdConstructQueryShareList(gdAccount4,5000,0,0,mapShareENCN().get("0"), respShareList3);
        respShareList4 = gdConstructQueryShareList(gdAccount5,1000,0,0,mapShareENCN().get("0"), respShareList4);
        respShareList4 = gdConstructQueryShareList(gdAccount6,1000,0,0,mapShareENCN().get("0"), respShareList4);
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
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":6000,\"lockAmount\":0}"));

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
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":5000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo4);
        assertEquals(gdAccClientNo4,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo4 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount4 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":5000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo5);
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo6);
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));


        log.info("增发后查询机构主体信息");
        String query3 = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);
//        BigDecimal totalShares2 = new BigDecimal(JSONObject.fromObject(query3).getJSONObject("data").getString("subject_total_share_capital"));
//
//        log.info("判断增发前后机构主体查询总股本数增加数正确");
//        assertEquals(totalShares.add(new BigDecimal("4000")),totalShares2);

    }


    @Test
    public void TC09_shareIncrease_SFNS() throws Exception {

        log.info(registerInfo.toString());
        mapAddrRegObjId.clear(); //先清空map 这样后面map中拿到的就是此次发行的登记对象标识

        log.info("增发前查询机构主体信息");
        String query2 = gd.GDObjectQueryByVer(gdCompanyID,-1);
        int totalMembersBf = JSONObject.fromObject(query2).getJSONObject("data").getJSONObject("body"
        ).getJSONObject("subject_information").getJSONObject("organization_subject_information"
        ).getJSONObject("basic_information_of_enterprise").getJSONObject("basic_information_description"
        ).getInt("subject_shareholders_number");
        register_event_type = "2";//非交易登记

        String eqCode = gdEquityCode;
        String reason = "股份分红";
        String txObjId = "4increaseObj" + Random(6);

        Map eqProd = gdBF.init03EquityProductInfo();
        Map txInfo = gdBF.init04TxInfo();
        txInfo.put("transaction_object_id",txObjId);
        register_transaction_ref = txObjId; //此处为发行融资 设置登记引用接口中的交易报告

        txInfo.put("transaction_create_time",""); //设置transaction_create_time为空 使得发行成功 报送失败

        log.info("发行主体版本  " + gdCF.getObjectLatestVer(gdCompanyID));

        List<Map> shareList = gdConstructShareList(gdAccount1,increaseAmount,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,increaseAmount,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,increaseAmount,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,increaseAmount,0, shareList3);

        sleepAndSaveInfo(3000);
        int beforeBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");

        String response = gd.GDShareIncrease(gdPlatfromKeyID,eqCode,shareList4,reason, eqProd,txInfo);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        busUUID = tempUUID;

        sleepAndSaveInfo(4000);
        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,6000,0,0,mapShareENCN().get("0"),respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2,6000,0,0,mapShareENCN().get("0"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3,6000,0,0,mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4,6000,0,0,mapShareENCN().get("0"), respShareList3);
        System.out.println(respShareList4);
        int afterBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");
        assertEquals("区块高度仅增加1，无存证上链",beforeBlockHeight + 1,afterBlockHeight);

        String blockDetail = store.GetBlockByHeight(afterBlockHeight);
        assertEquals("区块仅有一笔交易",1,JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").size());
        //确认交易是增发交易
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
        assertEquals(respShareList4.size(),dataShareListNow.size());
        List<Map> getShareListNow = getShareListFromQueryNoZeroAcc(dataShareListNow);
        assertEquals(respShareList4.size(),getShareListNow.size());
        assertEquals(true,respShareList4.containsAll(getShareListNow) && getShareListNow.containsAll(respShareList4));

        //5.判断uuid是否存在合约中 方法待确认

        //6.判断所有登记对象ID是否存在OSS中
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,txObjId + "/0",""));
//        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,regObjId2 + "/0",""));

//        //7.判断数据库中是否存储登记对象ID
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId1));
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId2));
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId3));
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId4));

        //重复执行 使用上次的uuid
        txInfo.put("transaction_create_time",time4); //设置transaction_create_time正常 使得报送可以成功
        response= gd.GDShareIncrease(gdPlatfromKeyID,eqCode,shareList4,reason, eqProd,txInfo);

        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        String txDetail = store.GetTxDetail(txId);
        assertEquals("200", JSONObject.fromObject(txDetail).getString("state"));

        busUUID = "";

        txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        txDetail = store.GetTxDetail(txId);
        assertEquals("200", JSONObject.fromObject(txDetail).getString("state"));

        //检查各个查询对象返回信息中不包含敏感词
        assertEquals("不包含登记数据敏感词",true,gdCF.chkSensitiveWord(txDetail,subjectType));
        assertEquals("不包含登记数据敏感词",true,gdCF.chkSensitiveWord(txDetail,regType));
        assertEquals("不包含交易报告敏感词",true,gdCF.chkSensitiveWord(txDetail,txrpType));

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

        String newEqProdVer = gdCF.getObjectLatestVer(gdEquityCode);

        log.info(uriInfo.get("storeData").toString());
        product_issuer_subject_ref = gdCompanyID;

        Map getProInfo = gdCF.constructJGDataFromStr(conJGFileName(gdEquityCode, newEqProdVer), prodType, "1");
        eqProd.put("content",gdCF.constructContentTreeMap(prodType, gdEquityCode, newEqProdVer, "update", String.valueOf(ts3)));
        log.info("检查产品存证信息内容与传入一致\n" + eqProd.toString() + "\n" + getProInfo.toString());
        bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(eqProd, prodType)), replaceCertain(getProInfo.toString()));
        assertEquals("检查增发产品是否一致" ,true,bSame);
        log.info("检查增发产品是否一致:" + bSame);


        product_issuer_subject_ref = gdCompanyID;
        transaction_custody_product_ref = gdEquityCode;

        txInfo.put("content",gdCF.constructContentTreeMap(txrpType, txObjId, "0", "create", String.valueOf(ts4)));
        Map getTxRpInfo = gdCF.constructJGDataFromStr(conJGFileName(txObjId, "0"), txrpType, "");
        bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(txInfo,txrpType)),replaceCertain(getTxRpInfo.toString()));
        assertEquals("检查增发交易报告数据是否一致" ,true,bSame);
        log.info("检查增发交易报告数据是否一致:" + bSame);


        query2 = gd.GDObjectQueryByVer(gdCompanyID,-1);
        int totalMembers = JSONObject.fromObject(query2).getJSONObject("data").getJSONObject("body"
        ).getJSONObject("subject_information").getJSONObject("organization_subject_information"
        ).getJSONObject("basic_information_of_enterprise").getJSONObject("basic_information_description"
        ).getInt("subject_shareholders_number");

        assertEquals(totalMembersBf,totalMembers);

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
        respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,6000,0,0,mapShareENCN().get("0"),respShareList);
        respShareList2 = gdConstructQueryShareList(gdAccount2,6000,0,0,mapShareENCN().get("0"), respShareList);
        respShareList3 = gdConstructQueryShareList(gdAccount3,6000,0,0,mapShareENCN().get("0"), respShareList2);
        respShareList4 = gdConstructQueryShareList(gdAccount4,6000,0,0,mapShareENCN().get("0"), respShareList3);

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
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":6000,\"lockAmount\":0}"));

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
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo6);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));


        log.info("增发后查询机构主体信息");
        String query3 = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);
//        BigDecimal totalShares2 = new BigDecimal(JSONObject.fromObject(query3).getJSONObject("data").getString("subject_total_share_capital"));
//
//        log.info("判断增发前后机构主体查询总股本数增加数正确");
//        assertEquals(totalShares.add(new BigDecimal("4000")),totalShares2);

    }

    @Test
    public void TC09_shareIncrease_SFNFNS() throws Exception {

        log.info(registerInfo.toString());
        mapAddrRegObjId.clear(); //先清空map 这样后面map中拿到的就是此次发行的登记对象标识

        log.info("增发前查询机构主体信息");
        String query2 = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);
//        BigDecimal totalShares = new BigDecimal(JSONObject.fromObject(query2).getJSONObject("data").getString("subject_total_share_capital"));

        register_event_type = "2";//非交易登记

        String eqCode = gdEquityCode;
        String reason = "股份分红";
        String txObjId = "4increaseObj" + Random(6);

        Map eqProd = gdBF.init03EquityProductInfo();
        Map txInfo = gdBF.init04TxInfo();
        txInfo.put("transaction_object_id",txObjId);
        register_transaction_ref = txObjId; //此处为发行融资 设置登记引用接口中的交易报告

        txInfo.put("transaction_create_time",""); //设置transaction_create_time为空 使得发行成功 报送失败

        log.info("发行主体版本  " + gdCF.getObjectLatestVer(gdCompanyID));

        List<Map> shareList = gdConstructShareList(gdAccount1,increaseAmount,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,increaseAmount,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,increaseAmount,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,increaseAmount,0, shareList3);

        sleepAndSaveInfo(3000);
        int beforeBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");

        String response = gd.GDShareIncrease(gdPlatfromKeyID,eqCode,shareList4,reason, eqProd,txInfo);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        busUUID = tempUUID;

        sleepAndSaveInfo(4000);
        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,6000,0,0,mapShareENCN().get("0"),respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2,6000,0,0,mapShareENCN().get("0"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3,6000,0,0,mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4,6000,0,0,mapShareENCN().get("0"), respShareList3);
        System.out.println(respShareList4);
        int afterBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");
        assertEquals("区块高度仅增加1，无存证上链",beforeBlockHeight + 1,afterBlockHeight);

        String blockDetail = store.GetBlockByHeight(afterBlockHeight);
        assertEquals("区块仅有一笔交易",1,JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").size());
        //确认交易是增发交易
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
        assertEquals(respShareList4.size(),dataShareListNow.size());
        List<Map> getShareListNow = getShareListFromQueryNoZeroAcc(dataShareListNow);
        assertEquals(respShareList4.size(),getShareListNow.size());
        assertEquals(true,respShareList4.containsAll(getShareListNow) && getShareListNow.containsAll(respShareList4));

        //5.判断uuid是否存在合约中 方法待确认

        //6.判断所有登记对象ID是否存在OSS中
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,txObjId + "/0",""));
//        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,regObjId2 + "/0",""));

//        //7.判断数据库中是否存储登记对象ID
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId1));
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId2));
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId3));
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId4));

        //重复执行 使用上次的uuid
        txInfo.put("transaction_create_time","2021-03-01 12:12:12"); //设置transaction_create_time为错误的格式 报送失败
        response = gd.GDShareIncrease(gdPlatfromKeyID,eqCode,shareList4,reason, eqProd,txInfo);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));
        sleepAndSaveInfo(4000);
        afterBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");
        assertEquals("区块高度无增加，无存证上链",beforeBlockHeight + 1,afterBlockHeight);


        //重复执行 使用上次的uuid
        txInfo.put("transaction_create_time",time4); //设置transaction_create_time正常 使得报送可以成功
        response= gd.GDShareIncrease(gdPlatfromKeyID,eqCode,shareList4,reason, eqProd,txInfo);

        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        String txDetail = store.GetTxDetail(txId);
        assertEquals("200", JSONObject.fromObject(txDetail).getString("state"));

        busUUID = "";

        txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        txDetail = store.GetTxDetail(txId);
        assertEquals("200", JSONObject.fromObject(txDetail).getString("state"));

        //检查各个查询对象返回信息中不包含敏感词
        assertEquals("不包含登记数据敏感词",true,gdCF.chkSensitiveWord(txDetail,subjectType));
        assertEquals("不包含登记数据敏感词",true,gdCF.chkSensitiveWord(txDetail,regType));
        assertEquals("不包含交易报告敏感词",true,gdCF.chkSensitiveWord(txDetail,txrpType));

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

        String newEqProdVer = gdCF.getObjectLatestVer(gdEquityCode);

        log.info(uriInfo.get("storeData").toString());
        product_issuer_subject_ref = gdCompanyID;

        Map getProInfo = gdCF.constructJGDataFromStr(conJGFileName(gdEquityCode, newEqProdVer), prodType, "1");
        eqProd.put("content",gdCF.constructContentTreeMap(prodType, gdEquityCode, newEqProdVer, "update", String.valueOf(ts3)));
        log.info("检查产品存证信息内容与传入一致\n" + eqProd.toString() + "\n" + getProInfo.toString());
        bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(eqProd, prodType)), replaceCertain(getProInfo.toString()));
        assertEquals("检查增发产品是否一致" ,true,bSame);
        log.info("检查增发产品是否一致:" + bSame);


        product_issuer_subject_ref = gdCompanyID;
        transaction_custody_product_ref = gdEquityCode;

        txInfo.put("content",gdCF.constructContentTreeMap(txrpType, txObjId, "0", "create", String.valueOf(ts4)));
        Map getTxRpInfo = gdCF.constructJGDataFromStr(conJGFileName(txObjId, "0"), txrpType, "");
        bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(txInfo,txrpType)),replaceCertain(getTxRpInfo.toString()));
        assertEquals("检查增发交易报告数据是否一致" ,true,bSame);
        log.info("检查增发交易报告数据是否一致:" + bSame);


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
        respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,6000,0,0,mapShareENCN().get("0"),respShareList);
        respShareList2 = gdConstructQueryShareList(gdAccount2,6000,0,0,mapShareENCN().get("0"), respShareList);
        respShareList3 = gdConstructQueryShareList(gdAccount3,6000,0,0,mapShareENCN().get("0"), respShareList2);
        respShareList4 = gdConstructQueryShareList(gdAccount4,6000,0,0,mapShareENCN().get("0"), respShareList3);

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
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":6000,\"lockAmount\":0}"));

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
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo6);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));


        log.info("增发后查询机构主体信息");
        String query3 = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);
//        BigDecimal totalShares2 = new BigDecimal(JSONObject.fromObject(query3).getJSONObject("data").getString("subject_total_share_capital"));
//
//        log.info("判断增发前后机构主体查询总股本数增加数正确");
//        assertEquals(totalShares.add(new BigDecimal("4000")),totalShares2);

    }

    @Test
    public void TC09_shareIncrease_SFNS02() throws Exception {

        log.info(registerInfo.toString());
        mapAddrRegObjId.clear(); //先清空map 这样后面map中拿到的就是此次发行的登记对象标识

        log.info("增发前查询机构主体信息");
        String query2 = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);
//        BigDecimal totalShares = new BigDecimal(JSONObject.fromObject(query2).getJSONObject("data").getString("subject_total_share_capital"));

        register_event_type = "2";//非交易登记

        String eqCode = gdEquityCode;
        String reason = "股份分红";
        String txObjId = "4increaseObj" + Random(120);

        Map eqProd = gdBF.init03EquityProductInfo();
        Map txInfo = gdBF.init04TxInfo();
        txInfo.put("transaction_object_id",txObjId);
        register_transaction_ref = txObjId; //此处为发行融资 设置登记引用接口中的交易报告

        log.info("发行主体版本  " + gdCF.getObjectLatestVer(gdCompanyID));

        List<Map> shareList = gdConstructShareList(gdAccount1,increaseAmount,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,increaseAmount,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,increaseAmount,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,increaseAmount,0, shareList3);

        sleepAndSaveInfo(3000);
        int beforeBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");

        String response = gd.GDShareIncrease(gdPlatfromKeyID,eqCode,shareList4,reason, eqProd,txInfo);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        busUUID = tempUUID;

        sleepAndSaveInfo(4000);
        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,6000,0,0,mapShareENCN().get("0"),respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2,6000,0,0,mapShareENCN().get("0"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3,6000,0,0,mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4,6000,0,0,mapShareENCN().get("0"), respShareList3);
        System.out.println(respShareList4);
        int afterBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");
        assertEquals("区块高度仅增加1，无存证上链",beforeBlockHeight + 1,afterBlockHeight);

        String blockDetail = store.GetBlockByHeight(afterBlockHeight);
        assertEquals("区块仅有一笔交易",1,JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").size());
        //确认交易是增发交易
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
        assertEquals(respShareList4.size(),dataShareListNow.size());
        List<Map> getShareListNow = getShareListFromQueryNoZeroAcc(dataShareListNow);
        assertEquals(respShareList4.size(),getShareListNow.size());
        assertEquals(true,respShareList4.containsAll(getShareListNow) && getShareListNow.containsAll(respShareList4));

        //5.判断uuid是否存在合约中 方法待确认

        //6.判断所有登记对象ID是否存在OSS中
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,txObjId + "/0",""));
//        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,regObjId2 + "/0",""));

//        //7.判断数据库中是否存储登记对象ID
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId1));
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId2));
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId3));
//        assertEquals(false,mysql.checkDataExist(dbStr.split(",")[1],dbStr.split(",")[2],"id_version","object_id",regObjId4));

        //重复执行 使用上次的uuid
        txObjId = "4increaseObj" + Random(6);
        txInfo.put("transaction_object_id",txObjId);
        register_transaction_ref = txObjId; //此处为发行融资 设置登记引用接口中的交易报告
        List<Map> shareList11 = gdConstructShareList(gdAccount1,increaseAmount,0);
        List<Map> shareList12 = gdConstructShareList(gdAccount2,increaseAmount,0, shareList11);
        List<Map> shareList13 = gdConstructShareList(gdAccount3,increaseAmount,0, shareList12);
        List<Map> shareList14 = gdConstructShareList(gdAccount4,increaseAmount,0, shareList13);
        response = gd.GDShareIncrease(gdPlatfromKeyID,eqCode,shareList14,reason, eqProd,txInfo);

        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        String txDetail = store.GetTxDetail(txId);
        assertEquals("200", JSONObject.fromObject(txDetail).getString("state"));

        busUUID = "";

        txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        txDetail = store.GetTxDetail(txId);
        assertEquals("200", JSONObject.fromObject(txDetail).getString("state"));

        //检查各个查询对象返回信息中不包含敏感词
        assertEquals("不包含登记数据敏感词",true,gdCF.chkSensitiveWord(txDetail,subjectType));
        assertEquals("不包含登记数据敏感词",true,gdCF.chkSensitiveWord(txDetail,regType));
        assertEquals("不包含交易报告敏感词",true,gdCF.chkSensitiveWord(txDetail,txrpType));

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

        String newEqProdVer = gdCF.getObjectLatestVer(gdEquityCode);

        log.info(uriInfo.get("storeData").toString());
        product_issuer_subject_ref = gdCompanyID;

        Map getProInfo = gdCF.constructJGDataFromStr(conJGFileName(gdEquityCode, newEqProdVer), prodType, "1");
        eqProd.put("content",gdCF.constructContentTreeMap(prodType, gdEquityCode, newEqProdVer, "update", String.valueOf(ts3)));
        log.info("检查产品存证信息内容与传入一致\n" + eqProd.toString() + "\n" + getProInfo.toString());
        bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(eqProd, prodType)), replaceCertain(getProInfo.toString()));
        assertEquals("检查增发产品是否一致" ,true,bSame);
        log.info("检查增发产品是否一致:" + bSame);


        product_issuer_subject_ref = gdCompanyID;
        transaction_custody_product_ref = gdEquityCode;

        txInfo.put("content",gdCF.constructContentTreeMap(txrpType, txObjId, "0", "create", String.valueOf(ts4)));
        Map getTxRpInfo = gdCF.constructJGDataFromStr(conJGFileName(txObjId, "0"), txrpType, "");
        bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(txInfo,txrpType)),replaceCertain(getTxRpInfo.toString()));
        assertEquals("检查增发交易报告数据是否一致" ,true,bSame);
        log.info("检查增发交易报告数据是否一致:" + bSame);


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
        respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,6000,0,0,mapShareENCN().get("0"),respShareList);
        respShareList2 = gdConstructQueryShareList(gdAccount2,6000,0,0,mapShareENCN().get("0"), respShareList);
        respShareList3 = gdConstructQueryShareList(gdAccount3,6000,0,0,mapShareENCN().get("0"), respShareList2);
        respShareList4 = gdConstructQueryShareList(gdAccount4,6000,0,0,mapShareENCN().get("0"), respShareList3);

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
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":6000,\"lockAmount\":0}"));

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
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo6);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));


        log.info("增发后查询机构主体信息");
        String query3 = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);
//        BigDecimal totalShares2 = new BigDecimal(JSONObject.fromObject(query3).getJSONObject("data").getString("subject_total_share_capital"));
//
//        log.info("判断增发前后机构主体查询总股本数增加数正确");
//        assertEquals(totalShares.add(new BigDecimal("4000")),totalShares2);

    }

    @Test
    public void TC09_shareIncrease_SameUniqueIdSameRequest() throws Exception {

        log.info(registerInfo.toString());
        mapAddrRegObjId.clear(); //先清空map 这样后面map中拿到的就是此次发行的登记对象标识

        log.info("增发前查询机构主体信息");
        String query2 = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);
//        BigDecimal totalShares = new BigDecimal(JSONObject.fromObject(query2).getJSONObject("data").getString("subject_total_share_capital"));

        register_event_type = "2";//非交易登记

        String eqCode = gdEquityCode;
        String reason = "股份分红";
        String txObjId = "4increaseObj" + Random(6);

        Map eqProd = gdBF.init03EquityProductInfo();
        Map txInfo = gdBF.init04TxInfo();
        txInfo.put("transaction_object_id",txObjId);
        register_transaction_ref = txObjId; //此处为发行融资 设置登记引用接口中的交易报告
        log.info("发行主体版本  " + gdCF.getObjectLatestVer(gdCompanyID));

        List<Map> shareList = gdConstructShareList(gdAccount1,increaseAmount,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,increaseAmount,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,increaseAmount,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,increaseAmount,0, shareList3);

        sleepAndSaveInfo(3000);
        int beforeBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");

        String response = gd.GDShareIncrease(gdPlatfromKeyID,eqCode,shareList4,reason, eqProd,txInfo);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
        busUUID = tempUUID;

        sleepAndSaveInfo(3000);

        int afterBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");
        assertEquals("区块高度仅增加2，存证上链",beforeBlockHeight + 2,afterBlockHeight);

        String blockDetail = store.GetBlockByHeight(afterBlockHeight - 1);
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
        String args = jsonObjectWVM.getJSONObject("arg").toString();

        blockDetail = store.GetBlockByHeight(afterBlockHeight);
        assertEquals("区块仅有一笔交易",1,JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").size());
        //确认交易是发行交易
        getTXDetails = store.GetTxDetail(JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").getString(0));
        assertEquals(true,getTXDetails.contains(mapAddrRegObjId.get(gdAccount1 + "0").toString()));
        assertEquals(true,getTXDetails.contains(mapAddrRegObjId.get(gdAccount2 + "0").toString()));
        assertEquals(true,getTXDetails.contains(mapAddrRegObjId.get(gdAccount3 + "0").toString()));
        assertEquals(true,getTXDetails.contains(mapAddrRegObjId.get(gdAccount4 + "0").toString()));

        busUUID = tempUUID;
        response = gd.GDShareIncrease(gdPlatfromKeyID,eqCode,shareList4,reason, eqProd,txInfo);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,JSONObject.fromObject(response).getString("message").contains("请检查此对象标识是否已经存在"));

        sleepAndSaveInfo(3000);
        int afterBlockHeight2 = JSONObject.fromObject(store.GetHeight()).getInt("data");
        assertEquals("区块高度无增加，无存证上链",afterBlockHeight,afterBlockHeight2);

    }

    @Test
    public void TC09_shareIncrease_SameUniqueIdDiffRequest() throws Exception {


        log.info(registerInfo.toString());
        mapAddrRegObjId.clear(); //先清空map 这样后面map中拿到的就是此次发行的登记对象标识

        log.info("增发前查询机构主体信息");
        String query2 = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);
//        BigDecimal totalShares = new BigDecimal(JSONObject.fromObject(query2).getJSONObject("data").getString("subject_total_share_capital"));

        register_event_type = "2";//非交易登记

        String eqCode = gdEquityCode;
        String reason = "股份分红";
        String txObjId = "4increaseObj" + Random(6);

        Map eqProd = gdBF.init03EquityProductInfo();
        Map txInfo = gdBF.init04TxInfo();
        txInfo.put("transaction_object_id",txObjId);
        register_transaction_ref = txObjId; //此处为发行融资 设置登记引用接口中的交易报告
        log.info("发行主体版本  " + gdCF.getObjectLatestVer(gdCompanyID));

        List<Map> shareList = gdConstructShareList(gdAccount1,increaseAmount,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,increaseAmount,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,increaseAmount,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,increaseAmount,0, shareList3);

        sleepAndSaveInfo(3000);
        int beforeBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");

        String response = gd.GDShareIncrease(gdPlatfromKeyID,eqCode,shareList4,reason, eqProd,txInfo);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
        busUUID = tempUUID;

        sleepAndSaveInfo(3000);

        int afterBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");
        assertEquals("区块高度仅增加2，存证上链",beforeBlockHeight + 2,afterBlockHeight);

        String blockDetail = store.GetBlockByHeight(afterBlockHeight - 1);
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
        String args = jsonObjectWVM.getJSONObject("arg").toString();

        blockDetail = store.GetBlockByHeight(afterBlockHeight);
        assertEquals("区块仅有一笔交易",1,JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").size());
        //确认交易是发行交易
        getTXDetails = store.GetTxDetail(JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").getString(0));
        assertEquals(true,getTXDetails.contains(mapAddrRegObjId.get(gdAccount1 + "0").toString()));
        assertEquals(true,getTXDetails.contains(mapAddrRegObjId.get(gdAccount2 + "0").toString()));
        assertEquals(true,getTXDetails.contains(mapAddrRegObjId.get(gdAccount3 + "0").toString()));
        assertEquals(true,getTXDetails.contains(mapAddrRegObjId.get(gdAccount4 + "0").toString()));

        busUUID = tempUUID;
        mapAddrRegObjId.clear();
        txObjId = "4increaseObj" + Random(6);
        eqProd = gdBF.init03EquityProductInfo();
        txInfo = gdBF.init04TxInfo();
        txInfo.put("transaction_object_id",txObjId);
        register_transaction_ref = txObjId; //此处为发行融资 设置登记引用接口中的交易报告
        log.info("发行主体版本  " + gdCF.getObjectLatestVer(gdCompanyID));

        List<Map> shareList11 = gdConstructShareList(gdAccount1,increaseAmount,0);
        List<Map> shareList12 = gdConstructShareList(gdAccount2,increaseAmount,0, shareList11);
        List<Map> shareList13 = gdConstructShareList(gdAccount3,increaseAmount,0, shareList12);
        List<Map> shareList14 = gdConstructShareList(gdAccount4,increaseAmount,0, shareList13);

        response = gd.GDShareIncrease(gdPlatfromKeyID,eqCode,shareList14,reason, eqProd,txInfo);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
        //合约未执行 但是uri存证上报了
        sleepAndSaveInfo(3000);

        int afterBlockHeight2 = JSONObject.fromObject(store.GetHeight()).getInt("data");
        assertEquals("区块高度增加1，存证上链，合约不执行",afterBlockHeight + 1,afterBlockHeight2);

        blockDetail = store.GetBlockByHeight(afterBlockHeight2);
        assertEquals("区块仅有一笔交易",1,JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").size());
        //确认交易是发行交易
        getTXDetails = store.GetTxDetail(JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").getString(0));
        assertEquals(true,getTXDetails.contains(mapAddrRegObjId.get(gdAccount1 + "0").toString()));
        assertEquals(true,getTXDetails.contains(mapAddrRegObjId.get(gdAccount2 + "0").toString()));
        assertEquals(true,getTXDetails.contains(mapAddrRegObjId.get(gdAccount3 + "0").toString()));
        assertEquals(true,getTXDetails.contains(mapAddrRegObjId.get(gdAccount4 + "0").toString()));

        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));


        //查询股东持股情况 无当前股权代码信息
        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        assertEquals(gdAccClientNo1,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));

        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo1 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount1 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":6000,\"lockAmount\":0}"));
    }

}
