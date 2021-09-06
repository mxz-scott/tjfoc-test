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
public class GDV2_ShareRecycle_UniqueId_Test {

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
    public void shareRecycle_SFNS()throws Exception{
        String eqCode = gdEquityCode;
        String remark = "777777";

        log.info("多个回收前查询机构主体信息");
        String query2 = gd.GDObjectQueryByVer(gdCompanyID,-1);
        int totalMembers = JSONObject.fromObject(query2).getJSONObject("data").getJSONObject("body"
        ).getJSONObject("subject_information").getJSONObject("organization_subject_information"
        ).getJSONObject("basic_information_of_enterprise").getJSONObject("basic_information_description"
        ).getInt("subject_shareholders_number");

        String subVerInit = gdCF.getObjectLatestVer(gdCompanyID);//获取初始主体版本

        log.info(registerInfo.toString());
        mapAddrRegObjId.clear(); //先清空map 这样后面map中拿到的就是此次发行的登记对象标识
        register_product_ref = gdEquityCode;

        String regObjId1 = "5" + mapAccAddr.get(gdAccount1) + Random(6);// + "_" + indexReg;
        Map tempReg1 = gdBF.init05RegInfo();
        tempReg1.put("register_registration_object_id",regObjId1);
        if(regObjType == 1){
        tempReg1.put("register_subject_account_ref","SH" + mapAccAddr.get(gdAccount1));}
        if(bChangeRegSN) tempReg1.put("register_serial_number", regObjId1);//区分同一账户多次登记//
        mapAddrRegObjId.put(gdAccount1 + "0" + indexReg,regObjId1);//方便后面测试验证


        String regObjId2 = "5" + mapAccAddr.get(gdAccount2) + Random(6);// + "_" + indexReg;
        Map tempReg2 = gdBF.init05RegInfo();
        tempReg2.put("register_registration_object_id",regObjId2);
        if(regObjType == 1){
        tempReg2.put("register_subject_account_ref","SH" + mapAccAddr.get(gdAccount2));}
        if(bChangeRegSN) tempReg2.put("register_serial_number", regObjId2);//区分同一账户多次登记//
        mapAddrRegObjId.put(gdAccount2 + "0" + indexReg,regObjId2);//方便后面测试验证

        String regObjId3 = "5" + mapAccAddr.get(gdAccount3) + Random(6);// + "_" + indexReg;
        Map tempReg3 = gdBF.init05RegInfo();
        tempReg3.put("register_registration_object_id",regObjId3);
        if(regObjType == 1){
        tempReg3.put("register_subject_account_ref","SH" + mapAccAddr.get(gdAccount3));}
        if(bChangeRegSN) tempReg3.put("register_serial_number", regObjId3);//区分同一账户多次登记//
        mapAddrRegObjId.put(gdAccount3 + "0" + indexReg,regObjId3);//方便后面测试验证

        String regObjId4 = "5" + mapAccAddr.get(gdAccount4) + Random(6);// + "_" + indexReg;
        Map tempReg4 = gdBF.init05RegInfo();
        tempReg4.put("register_registration_object_id",regObjId4);
        if(regObjType == 1){
        tempReg4.put("register_subject_account_ref","SH" + mapAccAddr.get(gdAccount4));}
        if(bChangeRegSN) tempReg4.put("register_serial_number", regObjId4);//区分同一账户多次登记
        mapAddrRegObjId.put(gdAccount4 + "0" + indexReg,regObjId4);//方便后面测试验证

        tempReg1.put("register_time",""); //设置register_time为空 使得发行成功 报送失败

        int beforeBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");

        List<Map> shareList = gdConstructShareListWithRegMap(gdAccount1,1000,0,tempReg1);
        List<Map> shareList2 = gdConstructShareListWithRegMap(gdAccount2,500,0,tempReg2, shareList);
        List<Map> shareList3 = gdConstructShareListWithRegMap(gdAccount3,500,0,tempReg3, shareList2);
        List<Map> shareList4 = gdConstructShareListWithRegMap(gdAccount4,500,0,tempReg4,shareList3);

        gd.GDGetEnterpriseShareInfo(gdEquityCode);
        log.info("====================================================================================");
        String response= gd.GDShareRecycle(gdPlatfromKeyID,eqCode,shareList4,remark);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("监管数据校验失败",true,JSONObject.fromObject(response).getString("message").contains("数据格式校验失败"));

        busUUID = tempUUID;

        sleepAndSaveInfo(4000);
        //待校验的股东持股信息
        List<Map> respShareList = new ArrayList<>();
//        respShareList = gdConstructQueryShareList(gdAccount1,500,0,0,mapShareENCN().get("0"),respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2,500,0,0,mapShareENCN().get("0"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3,500,0,0,mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4,500,0,0,mapShareENCN().get("0"), respShareList3);


        int afterBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");
        assertEquals("区块高度不增加，无存证上链",beforeBlockHeight,afterBlockHeight);

//        String blockDetail = store.GetBlockByHeight(afterBlockHeight);
//        assertEquals("区块仅有一笔交易",1,JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").size());
//        //确认交易是发行交易
//        String getTXDetails = store.GetTxDetail(JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").getString(0));
//        JSONObject jsonObject1 = JSONObject.fromObject(getTXDetails).getJSONObject("data");
//        assertEquals("1",jsonObject1.getJSONObject("header").getString("version"));
//        assertEquals("1",jsonObject1.getJSONObject("header").getString("type"));
//        assertEquals("12",jsonObject1.getJSONObject("header").getString("subType"));


//        //4.判断股东列表有变更
//        String queryList = gd.GDGetEnterpriseShareInfo(gdEquityCode);
//        assertEquals("200",JSONObject.fromObject(queryList).getString("state"));
//        JSONArray dataShareListNow = JSONObject.fromObject(queryList).getJSONArray("data");
//        assertEquals(respShareList4.size() + 1,dataShareListNow.size());
//        List<Map> getShareListNow = getShareListFromQueryNoZeroAcc(dataShareListNow);
//        assertEquals(respShareList4.size(),getShareListNow.size());
//        assertEquals(true,respShareList4.containsAll(getShareListNow) && getShareListNow.containsAll(respShareList4));

        //5.判断uuid是否存在合约中 方法待确认

        //6.判断所有登记对象ID是否存在OSS中
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,regObjId1 + "/0",""));
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,regObjId2 + "/0",""));
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,regObjId3 + "/0",""));
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,regObjId4 + "/0",""));
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,gdCompanyID + "/" + (subVerInit + 1),""));

        query2 = gd.GDObjectQueryByVer(gdCompanyID,-1);
        int totalMembersAft = JSONObject.fromObject(query2).getJSONObject("data").getJSONObject("body"
        ).getJSONObject("subject_information").getJSONObject("organization_subject_information"
        ).getJSONObject("basic_information_of_enterprise").getJSONObject("basic_information_description"
        ).getInt("subject_shareholders_number");
        assertEquals(totalMembers,totalMembersAft);//判断主体数据更新


        gd.GDGetEnterpriseShareInfo(gdEquityCode);
        log.info("====================================================================================2");

        //重复执行 使用上次的uuid
        tempReg1.put("register_time",time2); //设置register_time正常 使得报送可以成功
        response= gd.GDShareRecycle(gdPlatfromKeyID,eqCode,shareList4,remark);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));

        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        String txDetail = store.GetTxDetail(txId);
        assertEquals("200", JSONObject.fromObject(txDetail).getString("state"));

        sleepAndSaveInfo(2000);

        gd.GDGetEnterpriseShareInfo(gdEquityCode);
        log.info("====================================================================================3");

        query2 = gd.GDObjectQueryByVer(gdCompanyID,-1);
        totalMembersAft = JSONObject.fromObject(query2).getJSONObject("data").getJSONObject("body"
        ).getJSONObject("subject_information").getJSONObject("organization_subject_information"
        ).getJSONObject("basic_information_of_enterprise").getJSONObject("basic_information_description"
        ).getInt("subject_shareholders_number");
//        assertEquals(totalMembers-1,totalMembersAft);//判断主体数据更新

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
        List<Map> respShareList11 = new ArrayList<>();
        List<Map> respShareList12 = gdConstructQueryShareList(gdAccount2,500,0,0,mapShareENCN().get("0"), respShareList11);
        List<Map> respShareList13 = gdConstructQueryShareList(gdAccount3,500,0,0,mapShareENCN().get("0"), respShareList12);
        List<Map> respShareList14 = gdConstructQueryShareList(gdAccount4,500,0,0,mapShareENCN().get("0"), respShareList13);



        //检查存在余额的股东列表
        assertEquals(respShareList14.size()+1,dataShareList.size());

        List<Map> getShareList = getShareListFromQueryNoZeroAcc(dataShareList);
        log.info(getShareList.toString());
        assertEquals(respShareList14.size(),getShareList.size());
        assertEquals(true,respShareList14.containsAll(getShareList) && getShareList.containsAll(respShareList14));


        //查询股东持股情况 无当前股权代码信息
        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        assertEquals(gdAccClientNo1,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
//        String get = JSONObject.fromObject(JSONObject.fromObject(query).getJSONObject("data").getJSONArray("accountList").get(0)).getString("shareholderNo");
//        assertEquals("SH"+gdAccClientNo1,get);

        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo1 + "\""));
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(gdAccClientNo2,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo2 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount2 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":500,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo3);
        assertEquals(gdAccClientNo3,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo3 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount3 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":500,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo4);
        assertEquals(gdAccClientNo4,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo4 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount4 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":500,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo5);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo6);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        log.info("多个回收后查询机构主体信息");
        String query3 = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);
//        BigDecimal totalShares2 = new BigDecimal(JSONObject.fromObject(query3).getJSONObject("data").getString("subject_total_share_capital"));
//
//        log.info("判断增发前后机构主体查询总股本数增加数正确");
//        assertEquals(totalShares.subtract(new BigDecimal("400")),totalShares2);
    }


    @Test
    public void shareRecycle_SFNFNS()throws Exception{
        String eqCode = gdEquityCode;
        String remark = "777777";

        log.info("多个回收前查询机构主体信息");
        String query2 = gd.GDObjectQueryByVer(gdCompanyID,-1);
        int totalMembers = JSONObject.fromObject(query2).getJSONObject("data").getJSONObject("body"
        ).getJSONObject("subject_information").getJSONObject("organization_subject_information"
        ).getJSONObject("basic_information_of_enterprise").getJSONObject("basic_information_description"
        ).getInt("subject_shareholders_number");

        String subVerInit = gdCF.getObjectLatestVer(gdCompanyID);//获取初始主体版本

        log.info(registerInfo.toString());
        mapAddrRegObjId.clear(); //先清空map 这样后面map中拿到的就是此次发行的登记对象标识
        register_product_ref = gdEquityCode;

        String regObjId1 = "5" + mapAccAddr.get(gdAccount1) + Random(6);// + "_" + indexReg;
        Map tempReg1 = gdBF.init05RegInfo();
        tempReg1.put("register_registration_object_id",regObjId1);
        if(regObjType == 1){
        tempReg1.put("register_subject_account_ref","SH" + mapAccAddr.get(gdAccount1));}
        if(bChangeRegSN) tempReg1.put("register_serial_number", regObjId1);//区分同一账户多次登记//
        mapAddrRegObjId.put(gdAccount1 + "0" + indexReg,regObjId1);//方便后面测试验证


        String regObjId2 = "5" + mapAccAddr.get(gdAccount2) + Random(6);// + "_" + indexReg;
        Map tempReg2 = gdBF.init05RegInfo();
        tempReg2.put("register_registration_object_id",regObjId2);
        if(regObjType == 1){
        tempReg2.put("register_subject_account_ref","SH" + mapAccAddr.get(gdAccount2));}
        if(bChangeRegSN) tempReg2.put("register_serial_number", regObjId2);//区分同一账户多次登记//
        mapAddrRegObjId.put(gdAccount2 + "0" + indexReg,regObjId2);//方便后面测试验证

        String regObjId3 = "5" + mapAccAddr.get(gdAccount3) + Random(6);// + "_" + indexReg;
        Map tempReg3 = gdBF.init05RegInfo();
        tempReg3.put("register_registration_object_id",regObjId3);
        if(regObjType == 1){
        tempReg3.put("register_subject_account_ref","SH" + mapAccAddr.get(gdAccount3));}
        if(bChangeRegSN) tempReg3.put("register_serial_number", regObjId3);//区分同一账户多次登记//
        mapAddrRegObjId.put(gdAccount3 + "0" + indexReg,regObjId3);//方便后面测试验证

        String regObjId4 = "5" + mapAccAddr.get(gdAccount4) + Random(6);// + "_" + indexReg;
        Map tempReg4 = gdBF.init05RegInfo();
        tempReg4.put("register_registration_object_id",regObjId4);
        if(regObjType == 1){
        tempReg4.put("register_subject_account_ref","SH" + mapAccAddr.get(gdAccount4));}
        if(bChangeRegSN) tempReg4.put("register_serial_number", regObjId4);//区分同一账户多次登记
        mapAddrRegObjId.put(gdAccount4 + "0" + indexReg,regObjId4);//方便后面测试验证

        tempReg1.put("register_time",""); //设置register_time为空 使得发行成功 报送失败

        int beforeBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");

        List<Map> shareList = gdConstructShareListWithRegMap(gdAccount1,1000,0,tempReg1);
        List<Map> shareList2 = gdConstructShareListWithRegMap(gdAccount2,500,0,tempReg2, shareList);
        List<Map> shareList3 = gdConstructShareListWithRegMap(gdAccount3,500,0,tempReg3, shareList2);
        List<Map> shareList4 = gdConstructShareListWithRegMap(gdAccount4,500,0,tempReg4,shareList3);

        gd.GDGetEnterpriseShareInfo(gdEquityCode);
        log.info("====================================================================================");
        String response= gd.GDShareRecycle(gdPlatfromKeyID,eqCode,shareList4,remark);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("监管数据校验失败",true,JSONObject.fromObject(response).getString("message").contains("数据格式校验失败"));

        busUUID = tempUUID;

        sleepAndSaveInfo(4000);
        //待校验的股东持股信息
        List<Map> respShareList = new ArrayList<>();
//        respShareList = gdConstructQueryShareList(gdAccount1,500,0,0,mapShareENCN().get("0"),respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2,500,0,0,mapShareENCN().get("0"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3,500,0,0,mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4,500,0,0,mapShareENCN().get("0"), respShareList3);


        int afterBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");
        assertEquals("区块高度不增加，无存证上链",beforeBlockHeight,afterBlockHeight);

//        String blockDetail = store.GetBlockByHeight(afterBlockHeight);
//        assertEquals("区块仅有一笔交易",1,JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").size());
//        //确认交易是发行交易
//        String getTXDetails = store.GetTxDetail(JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").getString(0));
//        JSONObject jsonObject1 = JSONObject.fromObject(getTXDetails).getJSONObject("data");
//        assertEquals("1",jsonObject1.getJSONObject("header").getString("version"));
//        assertEquals("1",jsonObject1.getJSONObject("header").getString("type"));
//        assertEquals("12",jsonObject1.getJSONObject("header").getString("subType"));
//
//
//        //4.判断股东列表有变更
//        String queryList = gd.GDGetEnterpriseShareInfo(gdEquityCode);
//        assertEquals("200",JSONObject.fromObject(queryList).getString("state"));
//        JSONArray dataShareListNow = JSONObject.fromObject(queryList).getJSONArray("data");
//        assertEquals(respShareList4.size() + 1,dataShareListNow.size());
//        List<Map> getShareListNow = getShareListFromQueryNoZeroAcc(dataShareListNow);
//        assertEquals(respShareList4.size(),getShareListNow.size());
//        assertEquals(true,respShareList4.containsAll(getShareListNow) && getShareListNow.containsAll(respShareList4));

        //5.判断uuid是否存在合约中 方法待确认

        //6.判断所有登记对象ID是否存在OSS中
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,regObjId1 + "/0",""));
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,regObjId2 + "/0",""));
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,regObjId3 + "/0",""));
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,regObjId4 + "/0",""));
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,gdCompanyID + "/" + (subVerInit + 1),""));

        query2 = gd.GDObjectQueryByVer(gdCompanyID,-1);
        int totalMembersAft = JSONObject.fromObject(query2).getJSONObject("data").getJSONObject("body"
        ).getJSONObject("subject_information").getJSONObject("organization_subject_information"
        ).getJSONObject("basic_information_of_enterprise").getJSONObject("basic_information_description"
        ).getInt("subject_shareholders_number");
        assertEquals(totalMembers,totalMembersAft);//判断主体数据更新


        //重复执行 使用上次的uuid 仍使用错误的字段
        tempReg1.put("register_time",""); //设置register_time 为空 使得报送仍失败
        response= gd.GDShareRecycle(gdPlatfromKeyID,eqCode,shareList4,remark);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        sleepAndSaveInfo(3000);
        afterBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");
        assertEquals("区块高度无增加，无存证上链",beforeBlockHeight,afterBlockHeight);


        //6.判断所有登记对象ID是否存在OSS中
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,regObjId1 + "/0",""));
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,regObjId2 + "/0",""));
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,regObjId3 + "/0",""));
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,regObjId4 + "/0",""));
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,gdCompanyID + "/" + (subVerInit + 1),""));


        gd.GDGetEnterpriseShareInfo(gdEquityCode);
        log.info("====================================================================================2");

        //重复执行 使用上次的uuid
        tempReg1.put("register_time",time2); //设置register_time正常 使得报送可以成功
        response= gd.GDShareRecycle(gdPlatfromKeyID,eqCode,shareList4,remark);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));

        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        String txDetail = store.GetTxDetail(txId);
        assertEquals("200", JSONObject.fromObject(txDetail).getString("state"));

        sleepAndSaveInfo(2000);

        gd.GDGetEnterpriseShareInfo(gdEquityCode);
        log.info("====================================================================================3");

        query2 = gd.GDObjectQueryByVer(gdCompanyID,-1);
        totalMembersAft = JSONObject.fromObject(query2).getJSONObject("data").getJSONObject("body"
        ).getJSONObject("subject_information").getJSONObject("organization_subject_information"
        ).getJSONObject("basic_information_of_enterprise").getJSONObject("basic_information_description"
        ).getInt("subject_shareholders_number");
//        assertEquals(totalMembers-1,totalMembersAft);//判断主体数据更新

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
        List<Map> respShareList11 = new ArrayList<>();
        List<Map> respShareList12 = gdConstructQueryShareList(gdAccount2,500,0,0,mapShareENCN().get("0"), respShareList11);
        List<Map> respShareList13 = gdConstructQueryShareList(gdAccount3,500,0,0,mapShareENCN().get("0"), respShareList12);
        List<Map> respShareList14 = gdConstructQueryShareList(gdAccount4,500,0,0,mapShareENCN().get("0"), respShareList13);



        //检查存在余额的股东列表
        assertEquals(respShareList14.size()+1,dataShareList.size());

        List<Map> getShareList = getShareListFromQueryNoZeroAcc(dataShareList);
        log.info(getShareList.toString());
        assertEquals(respShareList14.size(),getShareList.size());
        assertEquals(true,respShareList14.containsAll(getShareList) && getShareList.containsAll(respShareList14));


        //查询股东持股情况 无当前股权代码信息
        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        assertEquals(gdAccClientNo1,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
//        String get = JSONObject.fromObject(JSONObject.fromObject(query).getJSONObject("data").getJSONArray("accountList").get(0)).getString("shareholderNo");
//        assertEquals("SH"+gdAccClientNo1,get);

        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo1 + "\""));
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(gdAccClientNo2,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo2 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount2 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":500,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo3);
        assertEquals(gdAccClientNo3,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo3 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount3 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":500,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo4);
        assertEquals(gdAccClientNo4,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo4 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount4 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":500,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo5);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo6);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        log.info("多个回收后查询机构主体信息");
        String query3 = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);
//        BigDecimal totalShares2 = new BigDecimal(JSONObject.fromObject(query3).getJSONObject("data").getString("subject_total_share_capital"));
//
//        log.info("判断增发前后机构主体查询总股本数增加数正确");
//        assertEquals(totalShares.subtract(new BigDecimal("400")),totalShares2);
    }


    @Test
    public void shareRecycle_SFNS_restart()throws Exception{
        String eqCode = gdEquityCode;
        String remark = "777777";

        log.info("多个回收前查询机构主体信息");
        String query2 = gd.GDObjectQueryByVer(gdCompanyID,-1);
        int totalMembers = JSONObject.fromObject(query2).getJSONObject("data").getJSONObject("body"
        ).getJSONObject("subject_information").getJSONObject("organization_subject_information"
        ).getJSONObject("basic_information_of_enterprise").getJSONObject("basic_information_description"
        ).getInt("subject_shareholders_number");

        String subVerInit = gdCF.getObjectLatestVer(gdCompanyID);//获取初始主体版本

        log.info(registerInfo.toString());
        mapAddrRegObjId.clear(); //先清空map 这样后面map中拿到的就是此次发行的登记对象标识
        register_product_ref = gdEquityCode;

        String regObjId1 = "5" + mapAccAddr.get(gdAccount1) + Random(128);// + "_" + indexReg;
        Map tempReg1 = gdBF.init05RegInfo();
        tempReg1.put("register_registration_object_id",regObjId1);
        if(regObjType == 1){
        tempReg1.put("register_subject_account_ref","SH" + mapAccAddr.get(gdAccount1));}
        if(bChangeRegSN) tempReg1.put("register_serial_number", regObjId1);//区分同一账户多次登记//
        mapAddrRegObjId.put(gdAccount1 + "0" + indexReg,regObjId1);//方便后面测试验证


        String regObjId2 = "5" + mapAccAddr.get(gdAccount2) + Random(6);// + "_" + indexReg;
        Map tempReg2 = gdBF.init05RegInfo();
        tempReg2.put("register_registration_object_id",regObjId2);
        if(regObjType == 1){
        tempReg2.put("register_subject_account_ref","SH" + mapAccAddr.get(gdAccount2));}
        if(bChangeRegSN) tempReg2.put("register_serial_number", regObjId2);//区分同一账户多次登记//
        mapAddrRegObjId.put(gdAccount2 + "0" + indexReg,regObjId2);//方便后面测试验证

        String regObjId3 = "5" + mapAccAddr.get(gdAccount3) + Random(6);// + "_" + indexReg;
        Map tempReg3 = gdBF.init05RegInfo();
        tempReg3.put("register_registration_object_id",regObjId3);
        if(regObjType == 1){
        tempReg3.put("register_subject_account_ref","SH" + mapAccAddr.get(gdAccount3));}
        if(bChangeRegSN) tempReg3.put("register_serial_number", regObjId3);//区分同一账户多次登记//
        mapAddrRegObjId.put(gdAccount3 + "0" + indexReg,regObjId3);//方便后面测试验证

        String regObjId4 = "5" + mapAccAddr.get(gdAccount4) + Random(6);// + "_" + indexReg;
        Map tempReg4 = gdBF.init05RegInfo();
        tempReg4.put("register_registration_object_id",regObjId4);
        if(regObjType == 1){
        tempReg4.put("register_subject_account_ref","SH" + mapAccAddr.get(gdAccount4));}
        if(bChangeRegSN) tempReg4.put("register_serial_number", regObjId4);//区分同一账户多次登记
        mapAddrRegObjId.put(gdAccount4 + "0" + indexReg,regObjId4);//方便后面测试验证

        int beforeBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");

        List<Map> shareList = gdConstructShareListWithRegMap(gdAccount1,1000,0,tempReg1);
        List<Map> shareList2 = gdConstructShareListWithRegMap(gdAccount2,500,0,tempReg2, shareList);
        List<Map> shareList3 = gdConstructShareListWithRegMap(gdAccount3,500,0,tempReg3, shareList2);
        List<Map> shareList4 = gdConstructShareListWithRegMap(gdAccount4,500,0,tempReg4,shareList3);

        gd.GDGetEnterpriseShareInfo(gdEquityCode);
        log.info("====================================================================================");
        String response= gd.GDShareRecycle(gdPlatfromKeyID,eqCode,shareList4,remark);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("监管数据校验失败",true,JSONObject.fromObject(response).getString("message").contains("数据格式校验失败"));

        busUUID = tempUUID;

        sleepAndSaveInfo(4000);

        query2 = gd.GDObjectQueryByVer(gdCompanyID,-1);
        int totalMembersAft01 = JSONObject.fromObject(query2).getJSONObject("data").getJSONObject("body"
        ).getJSONObject("subject_information").getJSONObject("organization_subject_information"
        ).getJSONObject("basic_information_of_enterprise").getJSONObject("basic_information_description"
        ).getInt("subject_shareholders_number");
//        log.info("111111111111111111111111111111111111：：：：：：" + totalMembersAft01);
//        assertEquals(totalMembers-1,totalMembersAft01);//判断主体数据更新

        //待校验的股东持股信息
        List<Map> respShareList = new ArrayList<>();
//        respShareList = gdConstructQueryShareList(gdAccount1,500,0,0,mapShareENCN().get("0"),respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2,500,0,0,mapShareENCN().get("0"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3,500,0,0,mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4,500,0,0,mapShareENCN().get("0"), respShareList3);


        int afterBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");
        assertEquals("区块高度不增加，无存证上链",beforeBlockHeight,afterBlockHeight);

//        String blockDetail = store.GetBlockByHeight(afterBlockHeight);
//        assertEquals("区块仅有一笔交易",1,JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").size());
//        //确认交易是发行交易
//        String getTXDetails = store.GetTxDetail(JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").getString(0));
//        JSONObject jsonObject1 = JSONObject.fromObject(getTXDetails).getJSONObject("data");
//        assertEquals("1",jsonObject1.getJSONObject("header").getString("version"));
//        assertEquals("1",jsonObject1.getJSONObject("header").getString("type"));
//        assertEquals("12",jsonObject1.getJSONObject("header").getString("subType"));
//
//
//        //4.判断股东列表有变更
//        String queryList = gd.GDGetEnterpriseShareInfo(gdEquityCode);
//        assertEquals("200",JSONObject.fromObject(queryList).getString("state"));
//        JSONArray dataShareListNow = JSONObject.fromObject(queryList).getJSONArray("data");
//        assertEquals(respShareList4.size() + 1,dataShareListNow.size());
//        List<Map> getShareListNow = getShareListFromQueryNoZeroAcc(dataShareListNow);
//        assertEquals(respShareList4.size(),getShareListNow.size());
//        assertEquals(true,respShareList4.containsAll(getShareListNow) && getShareListNow.containsAll(respShareList4));

        //5.判断uuid是否存在合约中 方法待确认

        //6.判断所有登记对象ID是否存在OSS中
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,regObjId1 + "/0",""));
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,regObjId2 + "/0",""));
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,regObjId3 + "/0",""));
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,regObjId4 + "/0",""));
        assertEquals(false,minio.getFileFromMinIO(minIOEP,jgBucket,
                gdCompanyID + "/" + (Integer.valueOf(subVerInit) + 1),"").contains("区域性股权市场跨链业务数据模型"));


        query2 = gd.GDObjectQueryByVer(gdCompanyID,-1);
        int totalMembersAft = JSONObject.fromObject(query2).getJSONObject("data").getJSONObject("body"
        ).getJSONObject("subject_information").getJSONObject("organization_subject_information"
        ).getJSONObject("basic_information_of_enterprise").getJSONObject("basic_information_description"
        ).getInt("subject_shareholders_number");

        assertEquals(totalMembers,totalMembersAft);//判断主体数据更新

        gd.GDGetEnterpriseShareInfo(gdEquityCode);
        log.info("====================================================================================2");
        log.info("请重启SDK");

        //重启SDK
        UtilsClass utilsClassTemp = new UtilsClass();
        utilsClassTemp.setAndRestartSDK();
        sleepAndSaveInfo(SLEEPTIME,"等待SDK重启");


        //重复执行 使用上次的uuid
        regObjId1 = "5" + mapAccAddr.get(gdAccount1) + Random(6); //设置id长度正常 使得报送可以成功
        tempReg1.put("register_registration_object_id",regObjId1);
        if(regObjType == 1){
        tempReg1.put("register_subject_account_ref","SH" + mapAccAddr.get(gdAccount1));}
        if(bChangeRegSN) tempReg1.put("register_serial_number", regObjId1);//区分同一账户多次登记//
        mapAddrRegObjId.put(gdAccount1 + "0" + indexReg,regObjId1);//方便后面测试验证

        //更新数据
        regObjId2 = "5" + mapAccAddr.get(gdAccount2) + Random(6);// + "_" + indexReg;
        tempReg2.put("register_registration_object_id",regObjId2);
        if(regObjType == 1){
        tempReg2.put("register_subject_account_ref","SH" + mapAccAddr.get(gdAccount2));}
        if(bChangeRegSN) tempReg2.put("register_serial_number", regObjId2);//区分同一账户多次登记//
        mapAddrRegObjId.put(gdAccount2 + "0" + indexReg,regObjId2);//方便后面测试验证

        response= gd.GDShareRecycle(gdPlatfromKeyID,eqCode,shareList4,remark);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));

        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        String txDetail = store.GetTxDetail(txId);
        assertEquals("200", JSONObject.fromObject(txDetail).getString("state"));

        sleepAndSaveInfo(2000);

        gd.GDGetEnterpriseShareInfo(gdEquityCode);
        log.info("====================================================================================3");

        query2 = gd.GDObjectQueryByVer(gdCompanyID,-1);
        totalMembersAft = JSONObject.fromObject(query2).getJSONObject("data").getJSONObject("body"
        ).getJSONObject("subject_information").getJSONObject("organization_subject_information"
        ).getJSONObject("basic_information_of_enterprise").getJSONObject("basic_information_description"
        ).getInt("subject_shareholders_number");

        assertEquals(totalMembers -1 ,totalMembersAft);//判断主体数据更新  重启后 缓存丢失 数据更新错误

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
        List<Map> respShareList11 = new ArrayList<>();
        List<Map> respShareList12 = gdConstructQueryShareList(gdAccount2,500,0,0,mapShareENCN().get("0"), respShareList11);
        List<Map> respShareList13 = gdConstructQueryShareList(gdAccount3,500,0,0,mapShareENCN().get("0"), respShareList12);
        List<Map> respShareList14 = gdConstructQueryShareList(gdAccount4,500,0,0,mapShareENCN().get("0"), respShareList13);



        //检查存在余额的股东列表
        assertEquals(respShareList14.size()+1,dataShareList.size());

        List<Map> getShareList = getShareListFromQueryNoZeroAcc(dataShareList);
        log.info(getShareList.toString());
        assertEquals(respShareList14.size(),getShareList.size());
        assertEquals(true,respShareList14.containsAll(getShareList) && getShareList.containsAll(respShareList14));


        //查询股东持股情况 无当前股权代码信息
        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        assertEquals(gdAccClientNo1,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
//        String get = JSONObject.fromObject(JSONObject.fromObject(query).getJSONObject("data").getJSONArray("accountList").get(0)).getString("shareholderNo");
//        assertEquals("SH"+gdAccClientNo1,get);

        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo1 + "\""));
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(gdAccClientNo2,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo2 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount2 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":500,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo3);
        assertEquals(gdAccClientNo3,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo3 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount3 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":500,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo4);
        assertEquals(gdAccClientNo4,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo4 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount4 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":500,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo5);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo6);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        log.info("多个回收后查询机构主体信息");
        String query3 = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);
//        BigDecimal totalShares2 = new BigDecimal(JSONObject.fromObject(query3).getJSONObject("data").getString("subject_total_share_capital"));
//
//        log.info("判断增发前后机构主体查询总股本数增加数正确");
//        assertEquals(totalShares.subtract(new BigDecimal("400")),totalShares2);
    }

    @Test
    public void shareRecycle_SFNS02()throws Exception{
        String eqCode = gdEquityCode;
        String remark = "777777";

        log.info("多个回收前查询机构主体信息");
        String query2 = gd.GDObjectQueryByVer(gdCompanyID,-1);
        int totalMembers = JSONObject.fromObject(query2).getJSONObject("data").getJSONObject("body"
        ).getJSONObject("subject_information").getJSONObject("organization_subject_information"
        ).getJSONObject("basic_information_of_enterprise").getJSONObject("basic_information_description"
        ).getInt("subject_shareholders_number");

        String subVerInit = gdCF.getObjectLatestVer(gdCompanyID);//获取初始主体版本

        log.info(registerInfo.toString());
        mapAddrRegObjId.clear(); //先清空map 这样后面map中拿到的就是此次发行的登记对象标识
        register_product_ref = gdEquityCode;

        String regObjId1 = "5" + mapAccAddr.get(gdAccount1) + Random(128);// + "_" + indexReg;
        Map tempReg1 = gdBF.init05RegInfo();
        tempReg1.put("register_registration_object_id",regObjId1);
        if(regObjType == 1){
        tempReg1.put("register_subject_account_ref","SH" + mapAccAddr.get(gdAccount1));}
        if(bChangeRegSN) tempReg1.put("register_serial_number", regObjId1);//区分同一账户多次登记//
        mapAddrRegObjId.put(gdAccount1 + "0" + indexReg,regObjId1);//方便后面测试验证


        String regObjId2 = "5" + mapAccAddr.get(gdAccount2) + Random(6);// + "_" + indexReg;
        Map tempReg2 = gdBF.init05RegInfo();
        tempReg2.put("register_registration_object_id",regObjId2);
        if(regObjType == 1){
        tempReg2.put("register_subject_account_ref","SH" + mapAccAddr.get(gdAccount2));}
        if(bChangeRegSN) tempReg2.put("register_serial_number", regObjId2);//区分同一账户多次登记//
        mapAddrRegObjId.put(gdAccount2 + "0" + indexReg,regObjId2);//方便后面测试验证

        String regObjId3 = "5" + mapAccAddr.get(gdAccount3) + Random(6);// + "_" + indexReg;
        Map tempReg3 = gdBF.init05RegInfo();
        tempReg3.put("register_registration_object_id",regObjId3);
        if(regObjType == 1){
        tempReg3.put("register_subject_account_ref","SH" + mapAccAddr.get(gdAccount3));}
        if(bChangeRegSN) tempReg3.put("register_serial_number", regObjId3);//区分同一账户多次登记//
        mapAddrRegObjId.put(gdAccount3 + "0" + indexReg,regObjId3);//方便后面测试验证

        String regObjId4 = "5" + mapAccAddr.get(gdAccount4) + Random(6);// + "_" + indexReg;
        Map tempReg4 = gdBF.init05RegInfo();
        tempReg4.put("register_registration_object_id",regObjId4);
        if(regObjType == 1){
        tempReg4.put("register_subject_account_ref","SH" + mapAccAddr.get(gdAccount4));}
        if(bChangeRegSN) tempReg4.put("register_serial_number", regObjId4);//区分同一账户多次登记
        mapAddrRegObjId.put(gdAccount4 + "0" + indexReg,regObjId4);//方便后面测试验证

        int beforeBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");

        List<Map> shareList = gdConstructShareListWithRegMap(gdAccount1,1000,0,tempReg1);
        List<Map> shareList2 = gdConstructShareListWithRegMap(gdAccount2,500,0,tempReg2, shareList);
        List<Map> shareList3 = gdConstructShareListWithRegMap(gdAccount3,500,0,tempReg3, shareList2);
        List<Map> shareList4 = gdConstructShareListWithRegMap(gdAccount4,500,0,tempReg4,shareList3);

        gd.GDGetEnterpriseShareInfo(gdEquityCode);
        log.info("====================================================================================");
        String response= gd.GDShareRecycle(gdPlatfromKeyID,eqCode,shareList4,remark);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("监管数据校验失败",true,JSONObject.fromObject(response).getString("message").contains("数据格式校验失败"));

        busUUID = tempUUID;

        sleepAndSaveInfo(4000);

        query2 = gd.GDObjectQueryByVer(gdCompanyID,-1);
//        int totalMembersAft01 = JSONObject.fromObject(query2).getJSONObject("data").getJSONObject("body"
//        ).getJSONObject("subject_information").getJSONObject("organization_subject_information"
//        ).getJSONObject("basic_information_of_enterprise").getJSONObject("basic_information_description"
//        ).getInt("subject_shareholders_number");
//        assertEquals(totalMembers-1,totalMembersAft01);//判断主体数据更新

        //待校验的股东持股信息
        List<Map> respShareList = new ArrayList<>();
//        respShareList = gdConstructQueryShareList(gdAccount1,500,0,0,mapShareENCN().get("0"),respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2,500,0,0,mapShareENCN().get("0"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3,500,0,0,mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4,500,0,0,mapShareENCN().get("0"), respShareList3);


        int afterBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");
        assertEquals("区块高度不增加，无存证上链",beforeBlockHeight,afterBlockHeight);

//        String blockDetail = store.GetBlockByHeight(afterBlockHeight);
//        assertEquals("区块仅有一笔交易",1,JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").size());
//        //确认交易是发行交易
//        String getTXDetails = store.GetTxDetail(JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").getString(0));
//        JSONObject jsonObject1 = JSONObject.fromObject(getTXDetails).getJSONObject("data");
//        assertEquals("1",jsonObject1.getJSONObject("header").getString("version"));
//        assertEquals("1",jsonObject1.getJSONObject("header").getString("type"));
//        assertEquals("12",jsonObject1.getJSONObject("header").getString("subType"));
//
//
//        //4.判断股东列表有变更
//        String queryList = gd.GDGetEnterpriseShareInfo(gdEquityCode);
//        assertEquals("200",JSONObject.fromObject(queryList).getString("state"));
//        JSONArray dataShareListNow = JSONObject.fromObject(queryList).getJSONArray("data");
//        assertEquals(respShareList4.size() + 1,dataShareListNow.size());
//        List<Map> getShareListNow = getShareListFromQueryNoZeroAcc(dataShareListNow);
//        assertEquals(respShareList4.size(),getShareListNow.size());
//        assertEquals(true,respShareList4.containsAll(getShareListNow) && getShareListNow.containsAll(respShareList4));

        //5.判断uuid是否存在合约中 方法待确认

        //6.判断所有登记对象ID是否存在OSS中
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,regObjId1 + "/0",""));
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,regObjId2 + "/0",""));
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,regObjId3 + "/0",""));
        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,regObjId4 + "/0",""));
//        assertEquals("错误",minio.getFileFromMinIO(minIOEP,jgBucket,gdCompanyID + "/" + (Integer.valueOf(subVerInit) + 1),""));
        assertEquals(false,minio.getFileFromMinIO(minIOEP,jgBucket,
                gdCompanyID + "/" + (Integer.valueOf(subVerInit) + 1),"").contains("区域性股权市场跨链业务数据模型"));


        query2 = gd.GDObjectQueryByVer(gdCompanyID,-1);
        int totalMembersAft = JSONObject.fromObject(query2).getJSONObject("data").getJSONObject("body"
        ).getJSONObject("subject_information").getJSONObject("organization_subject_information"
        ).getJSONObject("basic_information_of_enterprise").getJSONObject("basic_information_description"
        ).getInt("subject_shareholders_number");

        assertEquals(totalMembers,totalMembersAft);//判断主体数据更新

        gd.GDGetEnterpriseShareInfo(gdEquityCode);
        log.info("====================================================================================2");

        //重复执行 使用上次的uuid
        regObjId1 = "5" + mapAccAddr.get(gdAccount1) + Random(6); //设置id长度正常 使得报送可以成功
        tempReg1.put("register_registration_object_id",regObjId1);
        if(regObjType == 1){
        tempReg1.put("register_subject_account_ref","SH" + mapAccAddr.get(gdAccount1));}
        if(bChangeRegSN) tempReg1.put("register_serial_number", regObjId1);//区分同一账户多次登记//
        mapAddrRegObjId.put(gdAccount1 + "0" + indexReg,regObjId1);//方便后面测试验证

        //更新数据
        regObjId2 = "5" + mapAccAddr.get(gdAccount2) + Random(6);// + "_" + indexReg;
        tempReg2.put("register_registration_object_id",regObjId2);
        if(regObjType == 1){
        tempReg2.put("register_subject_account_ref","SH" + mapAccAddr.get(gdAccount2));}
        if(bChangeRegSN) tempReg2.put("register_serial_number", regObjId2);//区分同一账户多次登记//
        mapAddrRegObjId.put(gdAccount2 + "0" + indexReg,regObjId2);//方便后面测试验证

        response= gd.GDShareRecycle(gdPlatfromKeyID,eqCode,shareList4,remark);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));

        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        String txDetail = store.GetTxDetail(txId);
        assertEquals("200", JSONObject.fromObject(txDetail).getString("state"));

        sleepAndSaveInfo(2000);

        gd.GDGetEnterpriseShareInfo(gdEquityCode);
        log.info("====================================================================================3");

        query2 = gd.GDObjectQueryByVer(gdCompanyID,-1);
        totalMembersAft = JSONObject.fromObject(query2).getJSONObject("data").getJSONObject("body"
        ).getJSONObject("subject_information").getJSONObject("organization_subject_information"
        ).getJSONObject("basic_information_of_enterprise").getJSONObject("basic_information_description"
        ).getInt("subject_shareholders_number");

        assertEquals(totalMembers - 1,totalMembersAft);//判断主体数据更新

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
        List<Map> respShareList11 = new ArrayList<>();
        List<Map> respShareList12 = gdConstructQueryShareList(gdAccount2,500,0,0,mapShareENCN().get("0"), respShareList11);
        List<Map> respShareList13 = gdConstructQueryShareList(gdAccount3,500,0,0,mapShareENCN().get("0"), respShareList12);
        List<Map> respShareList14 = gdConstructQueryShareList(gdAccount4,500,0,0,mapShareENCN().get("0"), respShareList13);



        //检查存在余额的股东列表
        assertEquals(respShareList14.size()+1,dataShareList.size());

        List<Map> getShareList = getShareListFromQueryNoZeroAcc(dataShareList);
        log.info(getShareList.toString());
        assertEquals(respShareList14.size(),getShareList.size());
        assertEquals(true,respShareList14.containsAll(getShareList) && getShareList.containsAll(respShareList14));


        //查询股东持股情况 无当前股权代码信息
        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        assertEquals(gdAccClientNo1,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
//        String get = JSONObject.fromObject(JSONObject.fromObject(query).getJSONObject("data").getJSONArray("accountList").get(0)).getString("shareholderNo");
//        assertEquals("SH"+gdAccClientNo1,get);

        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo1 + "\""));
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(gdAccClientNo2,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo2 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount2 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":500,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo3);
        assertEquals(gdAccClientNo3,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo3 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount3 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":500,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo4);
        assertEquals(gdAccClientNo4,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo4 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount4 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":500,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo5);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo6);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        log.info("多个回收后查询机构主体信息");
        String query3 = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);
//        BigDecimal totalShares2 = new BigDecimal(JSONObject.fromObject(query3).getJSONObject("data").getString("subject_total_share_capital"));
//
//        log.info("判断增发前后机构主体查询总股本数增加数正确");
//        assertEquals(totalShares.subtract(new BigDecimal("400")),totalShares2);
    }

    @Test
    public void shareRecycle_SameUniqueIdSameRequest()throws Exception{

        String eqCode = gdEquityCode;
        String remark = "777777";

        log.info("多个回收前查询机构主体信息");
        String query2 = gd.GDObjectQueryByVer(gdCompanyID,-1);
        int totalMembers = JSONObject.fromObject(query2).getJSONObject("data").getJSONObject("body"
        ).getJSONObject("subject_information").getJSONObject("organization_subject_information"
        ).getJSONObject("basic_information_of_enterprise").getJSONObject("basic_information_description"
        ).getInt("subject_shareholders_number");

        String subVerInit = gdCF.getObjectLatestVer(gdCompanyID);//获取初始主体版本

        log.info(registerInfo.toString());
        mapAddrRegObjId.clear(); //先清空map 这样后面map中拿到的就是此次发行的登记对象标识
        register_product_ref = gdEquityCode;

        String regObjId1 = "5" + mapAccAddr.get(gdAccount1) + Random(6);// + "_" + indexReg;
        Map tempReg1 = gdBF.init05RegInfo();
        tempReg1.put("register_registration_object_id",regObjId1);
        if(regObjType == 1){
        tempReg1.put("register_subject_account_ref","SH" + mapAccAddr.get(gdAccount1));}
        if(bChangeRegSN) tempReg1.put("register_serial_number", regObjId1);//区分同一账户多次登记//
        mapAddrRegObjId.put(gdAccount1 + "0" + indexReg,regObjId1);//方便后面测试验证


        String regObjId2 = "5" + mapAccAddr.get(gdAccount2) + Random(6);// + "_" + indexReg;
        Map tempReg2 = gdBF.init05RegInfo();
        tempReg2.put("register_registration_object_id",regObjId2);
        if(regObjType == 1){
        tempReg2.put("register_subject_account_ref","SH" + mapAccAddr.get(gdAccount2));}
        if(bChangeRegSN) tempReg2.put("register_serial_number", regObjId2);//区分同一账户多次登记//
        mapAddrRegObjId.put(gdAccount2 + "0" + indexReg,regObjId2);//方便后面测试验证

        String regObjId3 = "5" + mapAccAddr.get(gdAccount3) + Random(6);// + "_" + indexReg;
        Map tempReg3 = gdBF.init05RegInfo();
        tempReg3.put("register_registration_object_id",regObjId3);
        if(regObjType == 1){
        tempReg3.put("register_subject_account_ref","SH" + mapAccAddr.get(gdAccount3));}
        if(bChangeRegSN) tempReg3.put("register_serial_number", regObjId3);//区分同一账户多次登记//
        mapAddrRegObjId.put(gdAccount3 + "0" + indexReg,regObjId3);//方便后面测试验证

        String regObjId4 = "5" + mapAccAddr.get(gdAccount4) + Random(6);// + "_" + indexReg;
        Map tempReg4 = gdBF.init05RegInfo();
        tempReg4.put("register_registration_object_id",regObjId4);
        if(regObjType == 1){
        tempReg4.put("register_subject_account_ref","SH" + mapAccAddr.get(gdAccount4));}
        if(bChangeRegSN) tempReg4.put("register_serial_number", regObjId4);//区分同一账户多次登记
        mapAddrRegObjId.put(gdAccount4 + "0" + indexReg,regObjId4);//方便后面测试验证

        int beforeBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");

        List<Map> shareList = gdConstructShareListWithRegMap(gdAccount1,1000,0,tempReg1);
        List<Map> shareList2 = gdConstructShareListWithRegMap(gdAccount2,500,0,tempReg2, shareList);
        List<Map> shareList3 = gdConstructShareListWithRegMap(gdAccount3,500,0,tempReg3, shareList2);
        List<Map> shareList4 = gdConstructShareListWithRegMap(gdAccount4,500,0,tempReg4,shareList3);

        gd.GDGetEnterpriseShareInfo(gdEquityCode);
        log.info("====================================================================================");
        String response= gd.GDShareRecycle(gdPlatfromKeyID,eqCode,shareList4,remark);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));

        busUUID = tempUUID;

        sleepAndSaveInfo(3000);

        int afterBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");
        assertEquals("区块高度仅增加2，存证上链",beforeBlockHeight + 2,afterBlockHeight);//回收

        String blockDetail = store.GetBlockByHeight(afterBlockHeight-1);
        assertEquals("区块仅有一笔交易",1,JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").size());
        //确认交易是发行交易
        String getTXDetails = store.GetTxDetail(JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").getString(0));
        JSONObject jsonObject1 = JSONObject.fromObject(getTXDetails).getJSONObject("data");
        assertEquals("1",jsonObject1.getJSONObject("header").getString("version"));
        assertEquals("1",jsonObject1.getJSONObject("header").getString("type"));
        assertEquals("12",jsonObject1.getJSONObject("header").getString("subType"));

        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals(4,JSONObject.fromObject(query).getJSONArray("data").size());
        assertEquals(false,query.contains(gdAccount1));
        assertEquals(true,query.contains(gdAccount2));
        assertEquals(true,query.contains(gdAccount3));
        assertEquals(true,query.contains(gdAccount4));
        assertEquals(String.valueOf(4000),gdCF.getTotalAmountFromShareList(JSONObject.fromObject(query).getJSONArray("data")));

        //查询股东持股情况 无当前股权代码信息
        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(gdAccClientNo2,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo2 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount2 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":500,\"lockAmount\":0}"));


        busUUID = tempUUID;

        response = gd.GDShareRecycle(gdPlatfromKeyID,eqCode,shareList4,remark);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,JSONObject.fromObject(response).getString("message").contains("请检查此对象标识是否已经存在"));

        sleepAndSaveInfo(3000);
        int afterBlockHeight2 = JSONObject.fromObject(store.GetHeight()).getInt("data");
        assertEquals("区块高度无增加，无存证上链",afterBlockHeight,afterBlockHeight2);

        //查询股东持股情况 无当前股权代码信息
        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(gdAccClientNo2,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo2 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount2 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":500,\"lockAmount\":0}"));
    }

    @Test
    public void shareRecycleSameUniqueIdDiffRequest() throws Exception {

        String eqCode = gdEquityCode;
        String remark = "777777";

        log.info("多个回收前查询机构主体信息");
        String query2 = gd.GDObjectQueryByVer(gdCompanyID,-1);
        int totalMembers = JSONObject.fromObject(query2).getJSONObject("data").getJSONObject("body"
        ).getJSONObject("subject_information").getJSONObject("organization_subject_information"
        ).getJSONObject("basic_information_of_enterprise").getJSONObject("basic_information_description"
        ).getInt("subject_shareholders_number");

        String subVerInit = gdCF.getObjectLatestVer(gdCompanyID);//获取初始主体版本

        log.info(registerInfo.toString());
        mapAddrRegObjId.clear(); //先清空map 这样后面map中拿到的就是此次发行的登记对象标识
        register_product_ref = gdEquityCode;

        String regObjId1 = "5" + mapAccAddr.get(gdAccount1) + Random(6);// + "_" + indexReg;
        Map tempReg1 = gdBF.init05RegInfo();
        tempReg1.put("register_registration_object_id",regObjId1);
        if(regObjType == 1){
        tempReg1.put("register_subject_account_ref","SH" + mapAccAddr.get(gdAccount1));}
        if(bChangeRegSN) tempReg1.put("register_serial_number", regObjId1);//区分同一账户多次登记//
        mapAddrRegObjId.put(gdAccount1 + "0" + indexReg,regObjId1);//方便后面测试验证


        String regObjId2 = "5" + mapAccAddr.get(gdAccount2) + Random(6);// + "_" + indexReg;
        Map tempReg2 = gdBF.init05RegInfo();
        tempReg2.put("register_registration_object_id",regObjId2);
        if(regObjType == 1){
        tempReg2.put("register_subject_account_ref","SH" + mapAccAddr.get(gdAccount2));}
        if(bChangeRegSN) tempReg2.put("register_serial_number", regObjId2);//区分同一账户多次登记//
        mapAddrRegObjId.put(gdAccount2 + "0" + indexReg,regObjId2);//方便后面测试验证

        String regObjId3 = "5" + mapAccAddr.get(gdAccount3) + Random(6);// + "_" + indexReg;
        Map tempReg3 = gdBF.init05RegInfo();
        tempReg3.put("register_registration_object_id",regObjId3);
        if(regObjType == 1){
        tempReg3.put("register_subject_account_ref","SH" + mapAccAddr.get(gdAccount3));}
        if(bChangeRegSN) tempReg3.put("register_serial_number", regObjId3);//区分同一账户多次登记//
        mapAddrRegObjId.put(gdAccount3 + "0" + indexReg,regObjId3);//方便后面测试验证

        String regObjId4 = "5" + mapAccAddr.get(gdAccount4) + Random(6);// + "_" + indexReg;
        Map tempReg4 = gdBF.init05RegInfo();
        tempReg4.put("register_registration_object_id",regObjId4);
        if(regObjType == 1){
        tempReg4.put("register_subject_account_ref","SH" + mapAccAddr.get(gdAccount4));}
        if(bChangeRegSN) tempReg4.put("register_serial_number", regObjId4);//区分同一账户多次登记
        mapAddrRegObjId.put(gdAccount4 + "0" + indexReg,regObjId4);//方便后面测试验证

        int beforeBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");

        List<Map> shareList = gdConstructShareListWithRegMap(gdAccount1,1000,0,tempReg1);
        List<Map> shareList2 = gdConstructShareListWithRegMap(gdAccount2,500,0,tempReg2, shareList);
        List<Map> shareList3 = gdConstructShareListWithRegMap(gdAccount3,500,0,tempReg3, shareList2);
        List<Map> shareList4 = gdConstructShareListWithRegMap(gdAccount4,500,0,tempReg4,shareList3);

        gd.GDGetEnterpriseShareInfo(gdEquityCode);
        log.info("====================================================================================");
        String response= gd.GDShareRecycle(gdPlatfromKeyID,eqCode,shareList4,remark);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));

        busUUID = tempUUID;

        sleepAndSaveInfo(3000);

        int afterBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");
        assertEquals("区块高度仅增加2，存证上链",beforeBlockHeight + 2,afterBlockHeight);//回收

        String blockDetail = store.GetBlockByHeight(afterBlockHeight-1);
        assertEquals("区块仅有一笔交易",1,JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").size());
        //确认交易是发行交易
        String getTXDetails = store.GetTxDetail(JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").getString(0));
        JSONObject jsonObject1 = JSONObject.fromObject(getTXDetails).getJSONObject("data");
        assertEquals("1",jsonObject1.getJSONObject("header").getString("version"));
        assertEquals("1",jsonObject1.getJSONObject("header").getString("type"));
        assertEquals("12",jsonObject1.getJSONObject("header").getString("subType"));

        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals(4,JSONObject.fromObject(query).getJSONArray("data").size());
        assertEquals(false,query.contains(gdAccount1));
        assertEquals(true,query.contains(gdAccount2));
        assertEquals(true,query.contains(gdAccount3));
        assertEquals(true,query.contains(gdAccount4));
        assertEquals(String.valueOf(4000),gdCF.getTotalAmountFromShareList(JSONObject.fromObject(query).getJSONArray("data")));

        //查询股东持股情况 无当前股权代码信息
        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(gdAccClientNo2,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo2 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount2 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":500,\"lockAmount\":0}"));


        busUUID = tempUUID;
        List<Map> shareList12 = gdConstructShareList(gdAccount2,100,0);
        List<Map> shareList13 = gdConstructShareList(gdAccount3,100,0, shareList12);
        List<Map> shareList14 = gdConstructShareList(gdAccount4,100,0,shareList13);
        response = gd.GDShareRecycle(gdPlatfromKeyID,eqCode,shareList14,remark);


        //合约未执行 但是uri存证上报了
        sleepAndSaveInfo(3000);

        int afterBlockHeight2 = JSONObject.fromObject(store.GetHeight()).getInt("data");
        assertEquals("区块高度增加1，存证上链，合约不执行",afterBlockHeight + 1,afterBlockHeight2);//回收存证

        blockDetail = store.GetBlockByHeight(afterBlockHeight2);
        assertEquals("区块仅有一笔交易",1,JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").size());
        //确认交易是发行交易
        getTXDetails = store.GetTxDetail(JSONObject.fromObject(blockDetail).getJSONObject("data").getJSONArray("txs").getString(0));
        assertEquals(true,getTXDetails.contains(mapAddrRegObjId.get(gdAccount2 + "0").toString()));
        assertEquals(true,getTXDetails.contains(mapAddrRegObjId.get(gdAccount3 + "0").toString()));
        assertEquals(true,getTXDetails.contains(mapAddrRegObjId.get(gdAccount4 + "0").toString()));
//        assertEquals(false,getTXDetails.contains(gdCompanyID));

        query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));
        assertEquals(String.valueOf(4000),gdCF.getTotalAmountFromShareList(JSONObject.fromObject(query).getJSONArray("data")));

        //查询股东持股情况 无当前股权代码信息
        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(gdAccClientNo2,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo2 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount2 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":500,\"lockAmount\":0}"));

    }


}
