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
public class GDV2_CheckJGFormat_Part3SubjectChangeTxSend {

    TestBuilder testBuilder= TestBuilder.getInstance();
    GuDeng gd =testBuilder.getGuDeng();
    Store store =testBuilder.getStore();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    GDCommonFunc gdCF = new GDCommonFunc();
    GDUnitFunc uf = new GDUnitFunc();
    GDBeforeCondition gdBF = new GDBeforeCondition();
    public static String bizNoTest = "";
    long issueAmount = 5000;
    long increaseAmount = 1000;
    long lockAmount = 500;
    long recycleAmount = 100;
    long changeAmount = 500;
    long transferAmount = issueAmount;
    String lockAddress = gdAccount1;

    String tempaccount_subject_ref = account_subject_ref;
    String tempsubject_investor_qualification_certifier_ref = subject_investor_qualification_certifier_ref;
    String tempproduct_issuer_subject_ref = product_issuer_subject_ref;
    String tempregister_transaction_ref = register_transaction_ref;

    @Rule
    public TestName tm = new TestName();
    /***
     * 测试说明
     * 增发新增股东数
     * 场内转板
     * @throws Exception
     */

    @BeforeClass
    public static void Before()throws Exception{
        GDBeforeCondition gdBefore = new GDBeforeCondition();
        gdBefore.gdCreateAccout();
//        gdBefore.initRegulationData();
        bondProductInfo = null;//本测试类为股权类产品
        equityProductInfo = gdBefore.init03EquityProductInfo();
        CNKey = "P31";
    }


    @Before
    public void shareIssue() throws Exception {

        tempsubject_investor_qualification_certifier_ref =subject_investor_qualification_certifier_ref;
        tempregister_transaction_ref = register_transaction_ref;


        gdEquityCode = CNKey + "Token_" + Random(8);
        gdCompanyID = CNKey + "Sub_" + Random(3);
        bizNoTest = CNKey + "Lock_" + Random(3);
        gdBF.initRegulationData();//股权代码/产品对象标识 有变更 则重新初始化

        List<Map> shareList = gdConstructShareList(gdAccount1,issueAmount,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,issueAmount,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,issueAmount,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,issueAmount,0,shareList3);

        String response= uf.shareIssue(gdEquityCode,shareList4,false);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
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

    }

    @After
    public void calJGDataAfterTx()throws Exception{
        testCurMethodName = tm.getMethodName();
        GDUnitFunc uf = new GDUnitFunc();
        int endHeight = net.sf.json.JSONObject.fromObject(store.GetHeight()).getInt("data");
        uf.checkJGHeaderOpVer(blockHeight,endHeight);
//        uf.updateBlockHeightParam(endHeight);

        subject_investor_qualification_certifier_ref = tempsubject_investor_qualification_certifier_ref;
        register_transaction_ref = tempregister_transaction_ref;
    }

    @Test
    public void shareIssueWithDiffShareProperty() throws Exception {
        gdCompanyID = CNKey + "Sub2_" + Random(4);
        gdEquityCode = CNKey + "Token2_" + Random(4);

        List<Map> shareList = gdConstructShareList(gdAccount1,issueAmount,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount1,issueAmount,1, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount2,issueAmount,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount2,issueAmount,1,shareList3);

        String response= uf.shareIssue(gdEquityCode,shareList4,false);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");

        log.info("================================检查存证数据格式化《开始》================================");

        //遍历检查所有账户登记及交易存证信息
        for(int k = 0 ;k < dataShareList.size(); k++) {
            String tempAddr = JSONObject.fromObject(dataShareList.get(k)).getString("address");
            String tempObjId = mapAddrRegObjId.get(tempAddr).toString();

            Map mapChkKeys = new HashMap();
            mapChkKeys.put("address","");
            mapChkKeys.put("txId",txId);
            mapChkKeys.put("objectid",tempObjId);
            mapChkKeys.put("version","0");
            mapChkKeys.put("contentType",regType);
            mapChkKeys.put("subProdSubType","");
            mapChkKeys.put("operationType","create");

            assertEquals("检查数据",true,gdCF.bCheckJGParams(mapChkKeys));
        }

        log.info("================================检查存证数据格式化《结束》================================");


        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,5000,0,0,mapShareENCN().get("0"),respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount1,5000,1,0,mapShareENCN().get("1"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount2,5000,0,0,mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount2,5000,1,0,mapShareENCN().get("1"), respShareList3);

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
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":5000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(gdAccClientNo2,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo2 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount2 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":5000,\"lockAmount\":0}"));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":5000,\"lockAmount\":0}"));

    }

    //部分转让给已有账户
    @Test
    public void shareTransfer_PartOut_NoSubmitSubject()throws Exception{

        String keyId = gdAccountKeyID1;
        String fromAddr = gdAccount1;
        String toAddr = gdAccount2;
        int shareProperty = 0;
        String eqCode = gdEquityCode;

        String tempObjIdFrom = "5" + CNKey + mapAccAddr.get(fromAddr).toString() + Random(4);
        String tempObjIdTo = "5" + CNKey + mapAccAddr.get(toAddr).toString() + Random(4);

        //交易报告数据
        Map txInfo = gdBF.init04TxInfo();
        String txRpObjId = "4" + CNKey + Random(6);
        txInfo.put("transaction_object_id",txRpObjId);

        //登记数据
        Map fromNow = gdBF.init05RegInfo();
        Map toNow = gdBF.init05RegInfo();

        register_transaction_ref = txRpObjId;//登记引用的是交易报告的对象标识

        fromNow.put("register_registration_object_id",tempObjIdFrom);
        toNow.put("register_registration_object_id",tempObjIdTo);
        fromNow.put("register_transaction_ref",txRpObjId);
        toNow.put("register_transaction_ref",txRpObjId);


        log.info("转让前查询机构主体信息");
        String query2 = gd.GDObjectQueryByVer(gdCompanyID,-1);
        int totalMembers = JSONObject.fromObject(query2).getJSONObject("data").getJSONObject("body"
        ).getJSONObject("subject_information").getJSONObject("organization_subject_information"
        ).getJSONObject("basic_information_of_enterprise").getJSONObject("basic_information_description"
        ).getInt("subject_shareholders_number");


        //执行交易
        String response= gd.GDShareTransfer(keyId,fromAddr,transferAmount/2,toAddr,shareProperty,eqCode,txInfo,fromNow,toNow);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));

        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));


        log.info("转让后查询机构主体信息");
        query2 = gd.GDObjectQueryByVer(gdCompanyID,-1);
        int totalMembersAft = JSONObject.fromObject(query2).getJSONObject("data").getJSONObject("body"
        ).getJSONObject("subject_information").getJSONObject("organization_subject_information"
        ).getJSONObject("basic_information_of_enterprise").getJSONObject("basic_information_description"
        ).getInt("subject_shareholders_number");



        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200", JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");

        log.info("================================检查存证数据格式化《开始》================================");
        log.info("检查转让from账户登记信息");
        String tempObjId = tempObjIdFrom;

        Map mapChkKeys = new HashMap();
        mapChkKeys.put("address","");
        mapChkKeys.put("txId",txId);
        mapChkKeys.put("objectid",tempObjId);
        mapChkKeys.put("version","0");
        mapChkKeys.put("contentType",regType);
        mapChkKeys.put("subProdSubType","");
        mapChkKeys.put("operationType","create");


        assertEquals("检查数据-登记",true,gdCF.bCheckJGParams(mapChkKeys));

        log.info("检查转让from账户登记信息");
        tempObjId = tempObjIdTo;

        mapChkKeys.clear();
        mapChkKeys.put("address","");
        mapChkKeys.put("txId",txId);
        mapChkKeys.put("objectid",tempObjId);
        mapChkKeys.put("version","0");
        mapChkKeys.put("contentType",regType);
        mapChkKeys.put("subProdSubType","");
        mapChkKeys.put("operationType","create");

        assertEquals("检查数据-登记",true,gdCF.bCheckJGParams(mapChkKeys));

        assertEquals("不报送主体信息",false,uriStoreData.contains(gdCompanyID));
//        assertEquals("更新主体版本非0",false,gdCF.getObjectLatestVer(gdCompanyID).equals("0"));
//        mapChkKeys.clear();
//        mapChkKeys.put("address","");
//        mapChkKeys.put("txId",txId);
//        mapChkKeys.put("objectid",gdCompanyID);
//        mapChkKeys.put("version",gdCF.getObjectLatestVer(gdCompanyID));
//        mapChkKeys.put("contentType",subjectType);
//        mapChkKeys.put("subProdSubType","1");
//        mapChkKeys.put("operationType","update");
//
//        Map mapKeyUpdate =  new HashMap();
//        mapKeyUpdate.put("subject_shareholders_number",totalMembersAft);
//
//        String json = JSON.toJSONString(mapKeyUpdate);
//        mapChkKeys.put("updateMap",json);
//
//        assertEquals("检查数据-主体",true,gdCF.bCheckJGParams(mapChkKeys));

        mapChkKeys.clear();
        mapChkKeys.put("address","");
        mapChkKeys.put("txId",txId);
        mapChkKeys.put("objectid",txRpObjId);
        mapChkKeys.put("version","0");
        mapChkKeys.put("contentType",txrpType);
        mapChkKeys.put("subProdSubType","");
        mapChkKeys.put("operationType","create");

        assertEquals("检查数据-交易报告",true,gdCF.bCheckJGParams(mapChkKeys));



        log.info("================================检查存证数据格式化《结束》================================");

        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,2500,0,0,mapShareENCN().get("0"),respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2,7500,0,0,mapShareENCN().get("0"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3,5000,0,0,mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4,5000,0,0,mapShareENCN().get("0"), respShareList3);

        log.info(respShareList4.toString());
        //检查存在余额的股东列表
        assertEquals(respShareList4.size(),dataShareList.size());
        List<Map> getShareList = getShareListFromQueryNoZeroAcc(dataShareList);
        assertEquals(respShareList4.size(),getShareList.size());
        assertEquals(true,respShareList4.containsAll(getShareList) && getShareList.containsAll(respShareList4));

        assertEquals("检查总股东数变更是否正确",totalMembersAft,totalMembers);
    }


    //转让全部转出转给已存在的股东 减少一个股东数 存在主体报送
    @Test
    public void shareTransfer_AllOut_SubmitSubject()throws Exception{

        String keyId = gdAccountKeyID1;
        String fromAddr = gdAccount1;
        String toAddr = gdAccount2;
        int shareProperty = 0;
        String eqCode = gdEquityCode;

        String tempObjIdFrom = "5" + CNKey + mapAccAddr.get(fromAddr).toString() + Random(4);
        String tempObjIdTo = "5" + CNKey + mapAccAddr.get(toAddr).toString() + Random(4);

        //交易报告数据
        Map txInfo = gdBF.init04TxInfo();
        String txRpObjId = "4" + CNKey + Random(6);
        txInfo.put("transaction_object_id",txRpObjId);

        //登记数据
        Map fromNow = gdBF.init05RegInfo();
        Map toNow = gdBF.init05RegInfo();

        register_transaction_ref = txRpObjId;//登记引用的是交易报告的对象标识

        fromNow.put("register_registration_object_id",tempObjIdFrom);
        toNow.put("register_registration_object_id",tempObjIdTo);
        fromNow.put("register_transaction_ref",txRpObjId);
        toNow.put("register_transaction_ref",txRpObjId);


        log.info("转让前查询机构主体信息");
        String query2 = gd.GDObjectQueryByVer(gdCompanyID,-1);
        int totalMembers = JSONObject.fromObject(query2).getJSONObject("data").getJSONObject("body"
        ).getJSONObject("subject_information").getJSONObject("organization_subject_information"
        ).getJSONObject("basic_information_of_enterprise").getJSONObject("basic_information_description"
        ).getInt("subject_shareholders_number");


        //执行交易
        String response= gd.GDShareTransfer(keyId,fromAddr,transferAmount,toAddr,shareProperty,eqCode,txInfo,fromNow,toNow);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));

        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));


        log.info("转让后查询机构主体信息");
        query2 = gd.GDObjectQueryByVer(gdCompanyID,-1);
        int totalMembersAft = JSONObject.fromObject(query2).getJSONObject("data").getJSONObject("body"
        ).getJSONObject("subject_information").getJSONObject("organization_subject_information"
        ).getJSONObject("basic_information_of_enterprise").getJSONObject("basic_information_description"
        ).getInt("subject_shareholders_number");



        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200", JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");

        log.info("================================检查存证数据格式化《开始》================================");
        log.info("检查转让from账户登记信息");
        String tempObjId = tempObjIdFrom;

        Map mapChkKeys = new HashMap();
        mapChkKeys.put("address","");
        mapChkKeys.put("txId",txId);
        mapChkKeys.put("objectid",tempObjId);
        mapChkKeys.put("version","0");
        mapChkKeys.put("contentType",regType);
        mapChkKeys.put("subProdSubType","");
        mapChkKeys.put("operationType","create");


        assertEquals("检查数据-登记",true,gdCF.bCheckJGParams(mapChkKeys));

        log.info("检查转让from账户登记信息");
        tempObjId = tempObjIdTo;

        mapChkKeys.clear();
        mapChkKeys.put("address","");
        mapChkKeys.put("txId",txId);
        mapChkKeys.put("objectid",tempObjId);
        mapChkKeys.put("version","0");
        mapChkKeys.put("contentType",regType);
        mapChkKeys.put("subProdSubType","");
        mapChkKeys.put("operationType","create");

        assertEquals("检查数据-登记",true,gdCF.bCheckJGParams(mapChkKeys));


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

        mapChkKeys.clear();
        mapChkKeys.put("address","");
        mapChkKeys.put("txId",txId);
        mapChkKeys.put("objectid",txRpObjId);
        mapChkKeys.put("version","0");
        mapChkKeys.put("contentType",txrpType);
        mapChkKeys.put("subProdSubType","");
        mapChkKeys.put("operationType","create");

        assertEquals("检查数据-交易报告",true,gdCF.bCheckJGParams(mapChkKeys));



        log.info("================================检查存证数据格式化《结束》================================");

        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2,10000,0,0,mapShareENCN().get("0"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3,5000,0,0,mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4,5000,0,0,mapShareENCN().get("0"), respShareList3);

        log.info(respShareList4.toString());
        //检查存在余额的股东列表
        assertEquals(respShareList4.size(),dataShareList.size());
        List<Map> getShareList = getShareListFromQueryNoZeroAcc(dataShareList);
        assertEquals(respShareList4.size(),getShareList.size());
        assertEquals(true,respShareList4.containsAll(getShareList) && getShareList.containsAll(respShareList4));

        assertEquals("检查总股东数变更是否正确",totalMembersAft + 1,totalMembers);
    }

    //使用不同币种和登记类型 针对开发提出更新数据中有更新这两个部分 但实际上不需要做强制变更的修改
    @Test
    public void shareTransfer_DiffMoneyType()throws Exception{

        String keyId = gdAccountKeyID1;
        String fromAddr = gdAccount1;
        String toAddr = gdAccount2;
        int shareProperty = 0;
        String eqCode = gdEquityCode;

        String tempObjIdFrom = "5" + CNKey + mapAccAddr.get(fromAddr).toString() + Random(4);
        String tempObjIdTo = "5" + CNKey + mapAccAddr.get(toAddr).toString() + Random(4);

        //交易报告数据
        Map txInfo = gdBF.init04TxInfo();
        String txRpObjId = "4" + CNKey + Random(6);
        txInfo.put("transaction_object_id",txRpObjId);

        //登记数据
        Map fromNow = gdBF.init05RegInfo();
        Map toNow = gdBF.init05RegInfo();

        register_transaction_ref = txRpObjId;//登记引用的是交易报告的对象标识

        fromNow.put("register_registration_object_id",tempObjIdFrom);
        toNow.put("register_registration_object_id",tempObjIdTo);
        fromNow.put("register_transaction_ref",txRpObjId);
        toNow.put("register_transaction_ref",txRpObjId);
        fromNow.put("register_asset_currency","978");//设置非156币种
        toNow.put("register_asset_currency","276");//设置非156币种


        log.info("转让前查询机构主体信息");
        String query2 = gd.GDObjectQueryByVer(gdCompanyID,-1);
        int totalMembers = JSONObject.fromObject(query2).getJSONObject("data").getJSONObject("body"
        ).getJSONObject("subject_information").getJSONObject("organization_subject_information"
        ).getJSONObject("basic_information_of_enterprise").getJSONObject("basic_information_description"
        ).getInt("subject_shareholders_number");


        //执行交易
        String response= gd.GDShareTransfer(keyId,fromAddr,transferAmount,toAddr,shareProperty,eqCode,txInfo,fromNow,toNow);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));

        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));


        log.info("转让后查询机构主体信息");
        query2 = gd.GDObjectQueryByVer(gdCompanyID,-1);
        int totalMembersAft = JSONObject.fromObject(query2).getJSONObject("data").getJSONObject("body"
        ).getJSONObject("subject_information").getJSONObject("organization_subject_information"
        ).getJSONObject("basic_information_of_enterprise").getJSONObject("basic_information_description"
        ).getInt("subject_shareholders_number");



        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200", JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");

        log.info("================================检查存证数据格式化《开始》================================");
        Map mapKeyUpdate =  new HashMap();

        log.info("检查转让from账户登记信息");
        String tempObjId = tempObjIdFrom;

        Map mapChkKeys = new HashMap();
        mapChkKeys.put("address","");
        mapChkKeys.put("txId",txId);
        mapChkKeys.put("objectid",tempObjId);
        mapChkKeys.put("version","0");
        mapChkKeys.put("contentType",regType);
        mapChkKeys.put("subProdSubType","");
        mapChkKeys.put("operationType","create");
        mapKeyUpdate.clear();
        mapKeyUpdate.put("register_asset_currency","978");

        String json = JSON.toJSONString(mapKeyUpdate);
        mapChkKeys.put("updateMap",json);


        assertEquals("检查数据-登记",true,gdCF.bCheckJGParams(mapChkKeys));

        log.info("检查转让from账户登记信息");
        tempObjId = tempObjIdTo;

        mapChkKeys.clear();
        mapChkKeys.put("address","");
        mapChkKeys.put("txId",txId);
        mapChkKeys.put("objectid",tempObjId);
        mapChkKeys.put("version","0");
        mapChkKeys.put("contentType",regType);
        mapChkKeys.put("subProdSubType","");
        mapChkKeys.put("operationType","create");

        mapKeyUpdate.clear();
        mapKeyUpdate.put("register_asset_currency","276");

        json = JSON.toJSONString(mapKeyUpdate);
        mapChkKeys.put("updateMap",json);

        assertEquals("检查数据-登记",true,gdCF.bCheckJGParams(mapChkKeys));


        assertEquals("更新主体版本非0",false,gdCF.getObjectLatestVer(gdCompanyID).equals("0"));
        mapChkKeys.clear();
        mapChkKeys.put("address","");
        mapChkKeys.put("txId",txId);
        mapChkKeys.put("objectid",gdCompanyID);
        mapChkKeys.put("version",gdCF.getObjectLatestVer(gdCompanyID));
        mapChkKeys.put("contentType",subjectType);
        mapChkKeys.put("subProdSubType","1");
        mapChkKeys.put("operationType","update");

        mapKeyUpdate.clear();
        mapKeyUpdate.put("subject_shareholders_number",totalMembersAft);

        json = JSON.toJSONString(mapKeyUpdate);
        mapChkKeys.put("updateMap",json);

        assertEquals("检查数据-主体",true,gdCF.bCheckJGParams(mapChkKeys));

        mapChkKeys.clear();
        mapChkKeys.put("address","");
        mapChkKeys.put("txId",txId);
        mapChkKeys.put("objectid",txRpObjId);
        mapChkKeys.put("version","0");
        mapChkKeys.put("contentType",txrpType);
        mapChkKeys.put("subProdSubType","");
        mapChkKeys.put("operationType","create");

        assertEquals("检查数据-交易报告",true,gdCF.bCheckJGParams(mapChkKeys));
        log.info("================================检查存证数据格式化《结束》================================");

        assertEquals("检查总股东数变更是否正确",totalMembers - 1,totalMembersAft);

        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2,10000,0,0,mapShareENCN().get("0"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3,5000,0,0,mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4,5000,0,0,mapShareENCN().get("0"), respShareList3);

        log.info(respShareList4.toString());
        //检查存在余额的股东列表
        assertEquals(respShareList4.size(),dataShareList.size());
        List<Map> getShareList = getShareListFromQueryNoZeroAcc(dataShareList);
        assertEquals(respShareList4.size(),getShareList.size());
        assertEquals(true,respShareList4.containsAll(getShareList) && getShareList.containsAll(respShareList4));

    }


    /***
     * 增发给新的股东 * 2  交易类型为1 发行融资 报送交易报告
     * @throws Exception
     */

    @Test
    public void IncreaseWithTxReportType1_SubmitTxReport()throws Exception{
        shareIncreaseSpec(1);
    }


    public void shareIncreaseSpec(int type) throws Exception {

        mapAddrRegObjId.clear();
        log.info("增发前查询机构主体信息");
        String query3 = gd.GDObjectQueryByVer(gdEquityCode,-1);
        String query2 = gd.GDObjectQueryByVer(gdCompanyID,-1);

        int totalMembers = JSONObject.fromObject(query2).getJSONObject("data").getJSONObject("body"
        ).getJSONObject("subject_information").getJSONObject("organization_subject_information"
        ).getJSONObject("basic_information_of_enterprise").getJSONObject("basic_information_description"
        ).getInt("subject_shareholders_number");

        String eqCode = gdEquityCode;
        String reason = "股份分红";

        String txObjId = "4IncreaseObj" + Random(6);
        product_issuer_subject_ref = gdCompanyID;

        Map eqProd = gdBF.init03EquityProductInfo();
        eqProd.put("product_object_id",gdEquityCode);

        Map txInfo = gdBF.init04TxInfo();
        txInfo.put("transaction_object_id",txObjId);
        txInfo.put("transaction_type",type);//交易报告类型
        register_transaction_ref = txObjId; //此处为发行融资 设置登记引用接口中的交易报告

        String ver1 = gdCF.getObjectLatestVer(gdCompanyID);

        List<Map> shareList = gdConstructShareList(gdAccount5,increaseAmount,0);
        List<Map> shareList4 = gdConstructShareList(gdAccount6,increaseAmount,0, shareList);

        //引用接口中的交易报告 因不会报送 故会直接报错
        String response= gd.GDShareIncrease(gdPlatfromKeyID,eqCode,shareList4,reason, eqProd,txInfo);
        if(type != 1){
            assertEquals("400",JSONObject.fromObject(response).getString("state"));

            sleepAndSaveInfo(4000);
            String ver2 = gdCF.getObjectLatestVer(gdCompanyID);

            assertEquals("主体版本变更",true,ver1.equals(ver2));//应该不要变更
            //直接从minio上获取报送数据文件信息
            MinIOOperation minio = new MinIOOperation();
            String storeData2 = minio.getFileFromMinIO(minIOEP,jgBucket,gdCompanyID + "/" + Integer.valueOf(ver1)+1,"");
            assertEquals("未获取到更新的主体版本信息",true,storeData2.contains("错误"));

            register_transaction_ref = tempregister_transaction_ref; //设置登记引用交易报告对象为已存在的对象
            List<Map> shareList2 = gdConstructShareList(gdAccount5,increaseAmount,0);
            shareList4 = gdConstructShareList(gdAccount6,increaseAmount,0, shareList2);

            response= gd.GDShareIncrease(gdPlatfromKeyID,eqCode,shareList4,reason, eqProd,txInfo);
        }
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));


        query2 = gd.GDObjectQueryByVer(gdCompanyID,-1);
        int totalMembersAft = JSONObject.fromObject(query2).getJSONObject("data").getJSONObject("body"
        ).getJSONObject("subject_information").getJSONObject("organization_subject_information"
        ).getJSONObject("basic_information_of_enterprise").getJSONObject("basic_information_description"
        ).getInt("subject_shareholders_number");

        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");

        log.info("================================检查存证数据格式化《开始》================================");

        //遍历检查所有账户登记及交易存证信息
        for(int k = 0 ;k < shareList4.size(); k++) {
            String tempAddr = JSONObject.fromObject(shareList4.get(k)).getString("address");
            String tempObjId = mapAddrRegObjId.get(tempAddr).toString();

            Map mapChkKeys = new HashMap();
            mapChkKeys.put("address","");
            mapChkKeys.put("txId",txId);
            mapChkKeys.put("objectid",tempObjId);
            mapChkKeys.put("version","0");
            mapChkKeys.put("contentType",regType);
            mapChkKeys.put("subProdSubType","");
            mapChkKeys.put("operationType","create");

            assertEquals("检查数据-登记",true,gdCF.bCheckJGParams(mapChkKeys));

        }

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

        //产品对象检查
        assertEquals("更新主体版本非0",false,gdCF.getObjectLatestVer(gdEquityCode).equals("0"));
        mapChkKeys.clear();
        mapChkKeys.put("address","");
        mapChkKeys.put("txId",txId);
        mapChkKeys.put("objectid",gdEquityCode);
        mapChkKeys.put("version",gdCF.getObjectLatestVer(gdEquityCode));
        mapChkKeys.put("contentType",prodType);
        mapChkKeys.put("subProdSubType","1");
        mapChkKeys.put("operationType","update");

        assertEquals("检查数据-产品",true,gdCF.bCheckJGParams(mapChkKeys));

        //交易报告发行融资类型1报送交易报告 否则不报送
        if(type == 1) {
            //交易报告对象检查
            assertEquals("更新交易报告版本非0", true, gdCF.getObjectLatestVer(txObjId).equals("0"));
            mapChkKeys.clear();
            mapChkKeys.put("address", "");
            mapChkKeys.put("txId", txId);
            mapChkKeys.put("objectid", txObjId);
            mapChkKeys.put("version", "0");
            mapChkKeys.put("contentType", txrpType);
            mapChkKeys.put("subProdSubType", "");
            mapChkKeys.put("operationType", "create");

            assertEquals("检查数据-交易报告", true, gdCF.bCheckJGParams(mapChkKeys));
        }else {
            assertEquals("不包含交易报告数据",false,uriStoreData.contains(txObjId));
        }

        assertEquals("增发后股东总数",totalMembers + 2,totalMembersAft);
        log.info("================================检查存证数据格式化《结束》================================");


        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,5000,0,0,mapShareENCN().get("0"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount5,1000,0,0,mapShareENCN().get("0"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount6,1000,0,0,mapShareENCN().get("0"),respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2,5000,0,0,mapShareENCN().get("0"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3,5000,0,0,mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4,5000,0,0,mapShareENCN().get("0"), respShareList3);

        //检查存在余额的股东列表
        assertEquals(respShareList4.size(),dataShareList.size());
        List<Map> getShareList = getShareListFromQueryNoZeroAcc(dataShareList);
        assertEquals(respShareList4.size(),getShareList.size());
        assertEquals(true,respShareList4.containsAll(getShareList) && getShareList.containsAll(respShareList4));

    }



    /***
     * 增发给新的股东 * 2  增发带交易报告（交易报告类型为0 空） 不报送交易报告
     * @throws Exception
     * 20201222 当前存在bug
     */
    @Test
    public void IncreaseWithTxReportType0_NoSubmitTxReport() throws Exception {
        shareIncreaseSpec(0);
    }

    /***
     * 增发给新的股东 * 2  增发带交易报告（交易报告类型为2 偿付回购） 不报送交易报告
     * @throws Exception
     * 20201222 当前存在bug
     */
    @Test
    public void IncreaseWithTxReportType2_NoSubmitTxReport() throws Exception {
        shareIncreaseSpec(2);
    }


    /***
     * 质押融资方式冻结 需带交易报告 交易类型为质押融资 即6
     * @throws Exception
     */
    @Test
    public void shareLock_Type6_SubmitTxReport() throws Exception {
        shareLockWithType(6);
    }
    /***
     * 质押融资方式冻结 需带交易报告 交易类型非质押融资 非6 不报送交易报告
     * @throws Exception
     */
    @Test
    public void shareLock_Type0_NoSubmitTxReport() throws Exception {
        shareLockWithType(0);
    }

    /***
     * 质押融资方式冻结 需带交易报告 交易类型非质押融资 非6 不报送交易报告
     * @throws Exception
     */
    @Test
    public void shareLock_Type1_NoSubmitTxReport() throws Exception {
        shareLockWithType(1);
    }

    /***
     * 质押融资方式冻结 需带交易报告 交易类型非质押融资 非6 不报送交易报告
     * @throws Exception
     */
    @Test
    public void shareLock_Type2_NoSubmitTxReport() throws Exception {
        shareLockWithType(2);
    }

    /***
     * 质押融资方式冻结 需带交易报告 交易类型非质押融资 非6 不报送交易报告
     * @throws Exception
     */
    @Test
    public void shareLock_Type7_NoSubmitTxReport() throws Exception {
        shareLockWithType(7);
    }

    public void shareLockWithType(int type) throws Exception {

        log.info("发行主体版本  " + gdCF.getObjectLatestVer(gdCompanyID));

        sleepAndSaveInfo(5000);
        String bizNo = bizNoTest;
        String eqCode = gdEquityCode;
        String address = gdAccount1;
        int shareProperty = 0;
        String reason = "司法冻结";
        String cutoffDate = "2022-09-30";


        String txObjId = "4LockObj" + Random(6);
        Map txInfo = gdBF.init04TxInfo();
        txInfo.put("transaction_object_id",txObjId);
        txInfo.put("transaction_type",type);//交易报告类型

        register_transaction_ref = txObjId; //设置登记引用接口中的交易报告



        //登记数据
        Map regInfo = gdBF.init05RegInfo();
        String tempObjId = "5LockOBJ" + mapAccAddr.get(gdAccount1).toString() + Random(5);
        regInfo.put("register_registration_object_id",tempObjId);

        String ver1 = gdCF.getObjectLatestVer(tempObjId);
        assertEquals("-1",ver1);

        String response= gd.GDShareLock(bizNo,address,eqCode,lockAmount,shareProperty,reason,cutoffDate,regInfo,txInfo);

        //非报送交易报告场景时 引用的是一个不存在的交易报告对象 因此会失败，失败之后 正确引用或者执行
        if(type !=6){
            assertEquals("400",JSONObject.fromObject(response).getString("state"));

            sleepAndSaveInfo(4000);
            //查询企业股东信息
            String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
//            assertEquals("未冻结成功",false,query.contains("\"lockAmount\":" + lockAmount));

            String ver2 = gdCF.getObjectLatestVer(tempObjId);

            assertEquals("登记版本变更",true,ver1.equals(ver2));//应该不要变更
            //直接从minio上获取报送数据文件信息
            MinIOOperation minio = new MinIOOperation();
            String storeData2 = minio.getFileFromMinIO(minIOEP,jgBucket,tempObjId + "/" + Integer.valueOf(ver1)+1,"");
            assertEquals("未获取到更新的登记对象版本信息",true,storeData2.contains("错误"));

            assertEquals("RTR012",tempregister_transaction_ref);
            register_transaction_ref = tempregister_transaction_ref; //设置登记引用已存在的交易报告对象

            regInfo = gdBF.init05RegInfo();
            regInfo.put("register_registration_object_id",tempObjId);
            regInfo.remove("register_transaction_ref");
//            bizNo = Random(15);//不应该 但临时测试报送
            response= gd.GDShareLock(bizNo,address,eqCode,lockAmount,shareProperty,reason,cutoffDate,regInfo,txInfo);

        }
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        String txDetail = store.GetTxDetail(txId);
        assertEquals("200", net.sf.json.JSONObject.fromObject(txDetail).getString("state"));

        //检查各个查询对象返回信息中不包含敏感词
        assertEquals("不包含登记数据敏感词",true,gdCF.chkSensitiveWord(txDetail,regType));

        log.info("================================检查存证数据格式化《开始》================================");

        Map mapChkKeys = new HashMap();
        mapChkKeys.put("address","");
        mapChkKeys.put("txId",txId);
        mapChkKeys.put("objectid",tempObjId);
        mapChkKeys.put("version","0");
        mapChkKeys.put("contentType",regType);
        mapChkKeys.put("subProdSubType","");
        mapChkKeys.put("operationType","create");
        assertEquals("检查数据-登记",true,gdCF.bCheckJGParams(mapChkKeys));

        //交易报告质押融资类型6报送交易报告 否则不报送
        if(type == 6) {
            //交易报告对象检查
            assertEquals("更新交易报告版本非0", true, gdCF.getObjectLatestVer(txObjId).equals("0"));
            mapChkKeys.clear();
            mapChkKeys.put("address", "");
            mapChkKeys.put("txId", txId);
            mapChkKeys.put("objectid", txObjId);
            mapChkKeys.put("version", "0");
            mapChkKeys.put("contentType", txrpType);
            mapChkKeys.put("subProdSubType", "");
            mapChkKeys.put("operationType", "create");

            Map mapKeyUpdate = new HashMap();
            mapKeyUpdate.put("transaction_type",type);

            String json = JSON.toJSONString(mapKeyUpdate);
            mapChkKeys.put("updateMap",json);

            assertEquals("检查数据-交易报告", true, gdCF.bCheckJGParams(mapChkKeys));
        }else {
            assertEquals("不包含交易报告数据",false,uriStoreData.contains(txObjId));
        }

        log.info("================================检查存证数据格式化《结束》================================");

        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));
        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");

        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,5000,0,lockAmount,mapShareENCN().get("0"),respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2,5000,0,0,mapShareENCN().get("0"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3,5000,0,0,mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4,5000,0,0,mapShareENCN().get("0"), respShareList3);

        //检查存在余额的股东列表
        assertEquals(respShareList4.size(),dataShareList.size());
        List<Map> getShareList = getShareListFromQueryNoZeroAcc(dataShareList);
        assertEquals(respShareList4.size(),getShareList.size());
        assertEquals(true,respShareList4.containsAll(getShareList) && getShareList.containsAll(respShareList4));

    }


    /***
     * 解除冻结还款解质押方式 7 需带交易报告 会报送交易报告
     * @throws Exception
     */
    @Test
    public void shareUnlock_Type7_SubmitTxReport() throws Exception {
        shareUnlockWithType(7);
    }
    public void shareUnlockWithType(int type) throws Exception {
        bizNoTest = "unlockReport" + Random(13);
        String bizNo = bizNoTest;
        String eqCode = gdEquityCode;
        String address = gdAccount1;
        int shareProperty = 0;
        long amount = lockAmount;


        String txObjId = "4LockObj" + Random(6);
        Map txInfo = gdBF.init04TxInfo();
        txInfo.put("transaction_object_id",txObjId);
        txInfo.put("transaction_type",0);//交易报告类型

        //登记数据
        Map regInfo = gdBF.init05RegInfo();
        String tempObjId = "5Lock2OBJ" + mapAccAddr.get(gdAccount1).toString() + Random(5);
        regInfo.put("register_registration_object_id",tempObjId);

        String response= gd.GDShareLock(bizNo,address,eqCode,lockAmount,shareProperty,"司法冻结","2022-09-30",regInfo);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));


        String txObjIdUnlock = "4LockObj" + Random(6);
        Map txInfoUnlock = gdBF.init04TxInfo();
        txInfoUnlock.put("transaction_object_id",txObjIdUnlock);
        txInfoUnlock.put("transaction_type",type);//交易报告类型

        register_transaction_ref = txObjIdUnlock; //设置登记引用接口中的交易报告

        //登记数据
        Map regInfoUnlock = gdBF.init05RegInfo();
        String tempObjIdUnlock = "5Lock2OBJ" + mapAccAddr.get(gdAccount1).toString() + Random(5);
        regInfoUnlock.put("register_registration_object_id",tempObjIdUnlock);

        String ver1 = gdCF.getObjectLatestVer(tempObjIdUnlock);
        assertEquals("-1",ver1);



        //解除冻结
        response= gd.GDShareUnlock(bizNo,eqCode,amount,regInfoUnlock,txInfoUnlock);
        if(type != 7){
            assertEquals("400",JSONObject.fromObject(response).getString("state"));

            sleepAndSaveInfo(4000);
            String ver2 = gdCF.getObjectLatestVer(tempObjIdUnlock);
            assertEquals("登记版本变更",true,ver1.equals(ver2));//应该不要变更
            //直接从minio上获取报送数据文件信息
            MinIOOperation minio = new MinIOOperation();
            String storeData2 = minio.getFileFromMinIO(minIOEP,jgBucket,tempObjIdUnlock + "/" + Integer.valueOf(ver1)+1,"");
            assertEquals("未获取到更新的登记对象版本信息",true,storeData2.contains("错误"));

            register_transaction_ref = tempregister_transaction_ref;
            regInfoUnlock = gdBF.init05RegInfo();
            regInfoUnlock.put("register_registration_object_id",tempObjIdUnlock);
            //解除冻结
            response= gd.GDShareUnlock(bizNo,eqCode,amount,regInfoUnlock,txInfoUnlock);
        }

        assertEquals("200",JSONObject.fromObject(response).getString("state"));
        txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));


        log.info("================================检查存证数据格式化《开始》================================");

        Map mapChkKeys = new HashMap();
        mapChkKeys.put("address","");
        mapChkKeys.put("txId",txId);
        mapChkKeys.put("objectid",tempObjIdUnlock);
        mapChkKeys.put("version","0");
        mapChkKeys.put("contentType",regType);
        mapChkKeys.put("subProdSubType","");
        mapChkKeys.put("operationType","create");
        assertEquals("检查数据-登记",true,gdCF.bCheckJGParams(mapChkKeys));

        //交易报告质押融资类型6报送交易报告 否则不报送
        if(type == 7) {
            //交易报告对象检查
            assertEquals("更新交易报告版本非0", true, gdCF.getObjectLatestVer(tempObjIdUnlock).equals("0"));
            mapChkKeys.clear();
            mapChkKeys.put("address", "");
            mapChkKeys.put("txId", txId);
            mapChkKeys.put("objectid", txObjIdUnlock);
            mapChkKeys.put("version", "0");
            mapChkKeys.put("contentType", txrpType);
            mapChkKeys.put("subProdSubType", "");
            mapChkKeys.put("operationType", "create");

            Map mapKeyUpdate = new HashMap();
            mapKeyUpdate.put("transaction_type",type);

            String json = JSON.toJSONString(mapKeyUpdate);
            mapChkKeys.put("updateMap",json);

            assertEquals("检查数据-交易报告", true, gdCF.bCheckJGParams(mapChkKeys));
        }else {
            assertEquals("不包含交易报告数据",false,uriStoreData.contains(txObjIdUnlock));
        }

        log.info("================================检查存证数据格式化《结束》================================");


        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
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

    }

    /***
     * 解除冻结 非还款解质押方式 带交易报告 但不会报送交易报告数据
     * @throws Exception
     */
    @Test
    public void shareUnlock_Type0_NoSubmitTxReport() throws Exception {
        shareUnlockWithType(0);
    }

    /***
     * 账户存在股份性质0*1000 1*1000
     * 冻结0*500 1*500
     * @throws Exception
     */
    public void lockMutli()throws Exception{

    }
}
