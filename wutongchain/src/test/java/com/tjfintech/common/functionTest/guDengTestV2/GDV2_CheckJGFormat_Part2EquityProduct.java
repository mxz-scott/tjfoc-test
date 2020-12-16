package com.tjfintech.common.functionTest.guDengTestV2;

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

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class GDV2_CheckJGFormat_Part2EquityProduct {

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

    String tempaccount_subject_ref,tempsubject_investor_qualification_certifier_ref,tempproduct_issuer_subject_ref;

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
        gdEquityCode = "fondTest" + Random(12);
    }

    @Before
    public void resetVar(){
        tempsubject_investor_qualification_certifier_ref =subject_investor_qualification_certifier_ref;
    }

    @After
    public void calJGDataAfterTx()throws Exception{
        testCurMethodName = tm.getMethodName();
        GDUnitFunc uf = new GDUnitFunc();
//        uf.calJGData();
//        uf.calJGDataEachHeight();
        subject_investor_qualification_certifier_ref =tempsubject_investor_qualification_certifier_ref;
    }

    @Test
    public void TC06_shareIssue() throws Exception {
        log.info(registerInfo.toString());
        mapAddrRegObjId.clear(); //先清空map 这样后面map中拿到的就是此次发行的登记对象标识

        List<Map> shareList = gdConstructShareList(gdAccount1,issueAmount,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,issueAmount,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,issueAmount,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,issueAmount,0,shareList3);

        String response= uf.shareIssue(gdEquityCode,shareList4,false);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        String txDetail = store.GetTxDetail(txId);
        assertEquals("200", net.sf.json.JSONObject.fromObject(txDetail).getString("state"));

        //检查各个查询对象返回信息中不包含敏感词
        assertEquals("不包含敏感词",true,
                gdCF.chkSensitiveWord(txDetail,regType));

        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");

        log.info("================================检查存证数据格式化《开始》================================");

        //定义相关对象标识版本变量
//        String regRSRefVer = gdCF.getObjectLatestVer(register_subject_ref);
//        String regRSARefVer = gdCF.getObjectLatestVer(register_subject_account_ref);
//        String regRTRefVer = gdCF.getObjectLatestVer(register_transaction_ref);
//        String regRPRefVer = gdCF.getObjectLatestVer(register_product_ref);
//        String regRRRSRefVer = gdCF.getObjectLatestVer(register_right_recognition_subject_ref);
//        String regRRRASRefVer = gdCF.getObjectLatestVer(register_right_recognition_agent_subject_ref);
//        String regRPRRefVer = gdCF.getObjectLatestVer(roll_register_product_ref);
//        String regRRSRefVer = gdCF.getObjectLatestVer(roll_register_subject_ref);
//        String regRESRefVer = gdCF.getObjectLatestVer(register_equity_subject_ref);
//        String regRDHRefVer = gdCF.getObjectLatestVer(register_debt_holder_ref);
//        String regRISRefVer = gdCF.getObjectLatestVer(register_investor_subject_ref);

        Map uriInfo = gdCF.getJGURIStoreHash(txId,conJGFileName(mapAddrRegObjId.get(gdAccount1).toString(),"0"),1);

        //遍历检查所有账户登记及交易存证信息
        for(int k = 0 ;k < dataShareList.size(); k++) {
            String tempAddr = JSONObject.fromObject(dataShareList.get(k)).getString("address");
            String tempObjId = mapAddrRegObjId.get(tempAddr).toString();
            String regVer = "0" ;//gdCF.getObjectLatestVer(account_subject_ref);

            //获取链上mini url的存证信息 并检查是否包含uri信息 每个登记都是新的 则都是0
            String regfileName = conJGFileName(tempObjId,regVer);
            String chkRegURI = regfileName;
            assertEquals(true,uriInfo.get("storeData").toString().contains(chkRegURI));
            assertEquals(true,gdCF.bContainJGFlag(uriInfo.get("storeData").toString()));//确认meta信息包含监管关键字

            //直接从minio上获取报送数据文件信息
            Map getRegInfo = gdCF.constructJGDataFromStr(regfileName,regType,"");

            Map regInfoInput = gdBF.init05RegInfo();
            regInfoInput.put("register_registration_object_id",tempObjId);
            regInfoInput.put("content",gdCF.constructContentMap(regType,tempObjId,regVer,"create",String.valueOf(ts5)));


//            //需要将比较的对象标识增加版本号信息
//            String[] verForReg = new String[]{"/" + regRSRefVer,"/" + regRSARefVer,"/" + regRTRefVer
//                    ,"/" + regRPRefVer,"/" + regRRRSRefVer,"/" + regRRRASRefVer,"/" + regRPRRefVer
//                    ,"/" + regRESRefVer,"/" + regRDHRefVer,"/" + regRISRefVer};

//            registerInfo.put("register_rights_change_amount", issueAmount);     //变动额修改为单个账户发行数量
//            registerInfo.put("register_rights_frozen_balance", 0);   //当前冻结余额修改为实际冻结数
//            registerInfo.put("register_available_balance", issueAmount);   //当前当前可用余额修改为当前实际可用余额
//            registerInfo.put("register_creditor_subscription_count", issueAmount);   //当前认购数量修改为当前实际余额
//            registerInfo.put("register_rights_frozen_change_amount", 0);   //冻结变动额修改为当前实际冻结变更额

            log.info("检查登记存证信息内容与传入一致\n" + regInfoInput.toString() + "\n" + getRegInfo.toString());
            bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(regInfoInput,regType)),replaceCertain(getRegInfo.toString()));
            assertEquals(tempObjId + "检查发行登记报告数据是否一致" ,true,bSame);
//            log.info("检查发行不包送交易报告数据");
//            assertEquals("检查发行不包送交易报告数据",false,store.GetTxDetail(storeId).contains("\"type\":\"交易报告\""));

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

    /***
     * 股权性质变更 部分变更
     * 变更后 未变更部分保持原有股权性质不变 P17对应的需求点
     * @throws Exception
     */
    @Test
    public void TC07_shareChangeProperty() throws Exception {
        String eqCode = gdEquityCode;
        String address = gdAccount1;

        int oldProperty = 0;
        int newProperty = 1;

        Map testReg1 = gdBF.init05RegInfo();
        Map testReg2 = gdBF.init05RegInfo();
        String regObjId1 = mapAccAddr.get(address) + "CProp1" + Random(6);
        String regObjId2 = mapAccAddr.get(address) + "CProp2" + Random(6);
        testReg1.put("register_account_obj_id",mapAccAddr.get(address));
        testReg1.put("register_registration_object_id",regObjId1);
        testReg1.put("register_nature_of_shares", oldProperty);

        testReg2.put("register_account_obj_id",mapAccAddr.get(address));
        testReg2.put("register_registration_object_id",regObjId2);
        testReg2.put("register_nature_of_shares", newProperty);

        List<Map> regListInfo = new ArrayList<>();
        regListInfo.add(testReg1);
        regListInfo.add(testReg2);

        String response= gd.GDShareChangeProperty(gdPlatfromKeyID,address,eqCode,changeAmount,oldProperty,newProperty,regListInfo);
        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");

        String txDetail = store.GetTxDetail(txId);
        assertEquals("200", net.sf.json.JSONObject.fromObject(txDetail).getString("state"));

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

        Map regInfoInput = gdBF.init05RegInfo();
        regInfoInput.put("register_registration_object_id",regObjId1);
        regInfoInput.put("content",gdCF.constructContentMap(regType,regObjId1,regVer,"create",String.valueOf(ts5)));
        log.info("检查登记存证信息内容与传入一致\n" + regInfoInput.toString() + "\n" + getRegInfo1.toString());
        bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(regInfoInput,regType)),replaceCertain(getRegInfo1.toString()));
        assertEquals("1检查数据是否一致" ,true,bSame);


        regInfoInput.put("register_registration_object_id",regObjId2);
        regInfoInput.put("content",gdCF.constructContentMap(regType,regObjId2,regVer,"create",String.valueOf(ts5)));
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
    }

    @Test
    public void TC08_shareTransfer()throws Exception{

        String keyId = gdAccountKeyID1;
        String fromAddr = gdAccount1;
        String toAddr = gdAccount5;
        int shareProperty = 0;
        String eqCode = gdEquityCode;

        String tempRegister_subject_ref = register_subject_ref;
        register_subject_ref = gdCompanyID;

        //交易报告数据
        Map txInfo = gdBF.init04TxInfo();
        String txRpObjId = "txReport" + Random(6);
        txInfo.put("transaction_object_id",txRpObjId);

        //登记数据
        String tempObjIdFrom = "reg" + mapAccAddr.get(gdAccount1).toString() + Random(3);
        String tempObjIdTo = "reg" + mapAccAddr.get(gdAccount5).toString() + Random(3);

        Map fromNow = gdBF.init05RegInfo();
        Map toNow = gdBF.init05RegInfo();

        register_transaction_ref = txRpObjId;//登记引用的是交易报告的对象标识

        fromNow.put("register_registration_object_id",tempObjIdFrom);
        toNow.put("register_registration_object_id",tempObjIdTo);

        fromNow.put("register_transaction_ref",txRpObjId);
        toNow.put("register_transaction_ref",txRpObjId);



        String tempObj = gdCompanyID;

        String query2 = gd.GDObjectQueryByVer(gdCompanyID,-1);

        //执行交易
        String response= gd.GDShareTransfer(keyId,fromAddr,transferAmount,toAddr,shareProperty,eqCode,txInfo,fromNow,toNow);
        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        String txDetail = store.GetTxDetail(txId);
        assertEquals("200", net.sf.json.JSONObject.fromObject(txDetail).getString("state"));

        //检查各个查询对象返回信息中不包含敏感词
        assertEquals("不包含登记数据敏感词",true,gdCF.chkSensitiveWord(txDetail,subjectType));
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
//        assertEquals(true,gdCF.bContainJGFlag(uriInfo.get("storeData").toString()));//确认meta信息包含监管关键字

        //直接从minio上获取报送数据文件信息
        Map getRegInfo1 = gdCF.constructJGDataFromStr(regfileName1,regType,"");
        Map getRegInfo2 = gdCF.constructJGDataFromStr(regfileName2,regType,"");
        Map getTxRpInfo = gdCF.constructJGDataFromStr(txfileName,txrpType,"");
        Map getSubInfo = gdCF.constructJGDataFromStr(subfileName,subjectType,"1");


        //填充header content字段
        fromNow.put("content",gdCF.constructContentMap(regType,tempObjIdFrom,regVer,"create",String.valueOf(ts5)));
        log.info("检查登记存证信息内容与传入一致\n" + fromNow.toString() + "\n" + getRegInfo1.toString());
        bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(fromNow,regType)),replaceCertain(getRegInfo1.toString()));
        assertEquals("from检查数据是否一致" ,true,bSame);
//        log.info("from登记检查数据是否一致" + bSame);

        //填充header content字段
        toNow.put("content",gdCF.constructContentMap(regType,tempObjIdTo,regVer,"create",String.valueOf(ts5)));
        log.info("检查登记存证信息内容与传入一致\n" + toNow.toString() + "\n" + getRegInfo1.toString());
        bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(toNow,regType)),replaceCertain(getRegInfo2.toString()));
        assertEquals("from检查数据是否一致" ,true,bSame);
//        log.info("to登记检查数据是否一致" + bSame);

        //填充header content字段
        txInfo.put("content",gdCF.constructContentMap(txrpType,txRpObjId,txRpVer,"create",String.valueOf(ts4)));
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
        enSubInfo.put("content",gdCF.constructContentMap(subjectType,gdCompanyID,subVer,"update",String.valueOf(ts1)));
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
    }

    @Test
    public void TC09_shareIncrease() throws Exception {

        log.info("增发前查询机构主体信息");
        String query2 = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);
//        BigDecimal totalShares = new BigDecimal(JSONObject.fromObject(query2).getJSONObject("data").getString("subject_total_share_capital"));

        String eqCode = gdEquityCode;
        String reason = "股份分红";
        String txObjId = "increaseObj" + Random(6);

        Map eqProd = gdBF.init03EquityProductInfo();
        Map txInfo = gdBF.init04TxInfo();
        txInfo.put("transaction_object_id",txObjId);

        List<Map> shareList = gdConstructShareList(gdAccount1,increaseAmount,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,increaseAmount,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,increaseAmount,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,increaseAmount,0, shareList3);

        String response= gd.GDShareIncrease(gdPlatfromKeyID,eqCode,shareList4,reason, eqProd);
        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        String txDetail = store.GetTxDetail(txId);
        assertEquals("200", net.sf.json.JSONObject.fromObject(txDetail).getString("state"));

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
            String tempObjId = mapAddrRegObjId.get(tempAddr).toString();
            String regVer = "0" ;
            //获取链上mini url的存证信息 并检查是否包含uri信息 每个登记都是新的 则都是0
            String regfileName = conJGFileName(tempObjId,regVer);
            String chkRegURI = regfileName;
            assertEquals(true,uriInfo.get("storeData").toString().contains(chkRegURI));
//            assertEquals(true,gdCF.bContainJGFlag(uriInfo.get("storeData").toString()));//确认meta信息包含监管关键字
            log.info("检查增发存证产品格式化及信息内容与传入一致");

            //直接从minio上获取报送数据文件信息
            Map getRegInfo = gdCF.constructJGDataFromStr(regfileName,regType,"");

            Map regInfoInput = gdBF.init05RegInfo();
            regInfoInput.put("register_registration_object_id",mapAddrRegObjId.get(tempAddr));
            regInfoInput.put("content",gdCF.constructContentMap(regType,tempObjId,regVer,"create",String.valueOf(ts5)));

            log.info("检查登记存证信息内容与传入一致\n" + regInfoInput.toString() + "\n" + getRegInfo.toString());
            bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(regInfoInput,regType)),replaceCertain(getRegInfo.toString()));
            assertEquals("检查增发登记数据是否一致" ,true,bSame);
//            log.info("检查增发登记数据:" + bSame);
        }

        String newEqProdVer = gdCF.getObjectLatestVer(gdEquityCode);

        log.info(uriInfo.get("storeData").toString());
        product_issuer_subject_ref = gdCompanyID;

        Map getProInfo = gdCF.constructJGDataFromStr(conJGFileName(gdEquityCode, newEqProdVer), prodType, "1");
        eqProd.put("content",gdCF.constructContentMap(prodType, gdEquityCode, newEqProdVer, "update", String.valueOf(ts3)));
        log.info("检查产品存证信息内容与传入一致\n" + eqProd.toString() + "\n" + getProInfo.toString());
        bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(eqProd, prodType)), replaceCertain(getProInfo.toString()));
//        assertEquals("检查增发产品是否一致" ,true,bSame);
        log.info("检查增发产品是否一致:" + bSame);


        product_issuer_subject_ref = gdCompanyID;
        txInfo.put("content",gdCF.constructContentMap(txrpType, txObjId, "0", "create", String.valueOf(ts4)));
        Map getTxRpInfo = gdCF.constructJGDataFromStr(conJGFileName(txObjId, "0"), txrpType, "");
        bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(txInfo,txrpType)),replaceCertain(getTxRpInfo.toString()));
//        assertEquals("检查增发交易报告数据是否一致" ,true,bSame);
        log.info("检查增发交易报告数据是否一致:" + bSame);

        Map enSub = gdBF.init01EnterpriseSubjectInfo();
        String subObjId = gdCompanyID;
        String subVer = gdCF.getObjectLatestVer(subObjId);
        enSub.put("content",gdCF.constructContentMap(subjectType, subObjId, subVer, "update", String.valueOf(ts1)));
        Map getEnSubInfo = gdCF.constructJGDataFromStr(conJGFileName(subObjId, subVer), subjectType, "");
        bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(enSub,subjectType)),replaceCertain(getEnSubInfo.toString()));
//        assertEquals("检查增发交易报告数据是否一致" ,true,bSame);
        log.info("检查增发交易报告数据是否一致:" + bSame);

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

    }


    @Test
    public void TC10_shareLock() throws Exception {

        sleepAndSaveInfo(5000);
        String bizNo = bizNoTest;
        String eqCode = gdEquityCode;
        String address = gdAccount1;
        int shareProperty = 0;
        String reason = "司法冻结";
        String cutoffDate = "2022-09-30";


        //登记数据
        Map regInfo = gdBF.init05RegInfo();
        String tempObjId = mapAccAddr.get(gdAccount1).toString() + Random(5);

        regNo = "Eq" + "lock" + bizNo + (new Date()).getTime();   //区分不同类型的交易登记以流水号
        regInfo.put("register_serial_number",regNo);       //更新对比的登记流水号
        regInfo.put("register_registration_object_id",tempObjId);

        String response= gd.GDShareLock(bizNo,address,eqCode,lockAmount,shareProperty,reason,cutoffDate,regInfo);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");
        String txDetail = store.GetTxDetail(txId);
        assertEquals("200", net.sf.json.JSONObject.fromObject(txDetail).getString("state"));

        //检查各个查询对象返回信息中不包含敏感词
        assertEquals("不包含登记数据敏感词",true,gdCF.chkSensitiveWord(txDetail,regType));

        log.info("================================检查存证数据格式化《开始》================================");

        Map uriInfo = gdCF.getJGURIStoreHash(txId,conJGFileName(tempObjId,"0"),1);

        String tempAddr = address;

        log.info("检查股权性质变更存证登记格式化及信息内容与传入一致");
        String regVer = "0" ;//gdCF.getObjectLatestVer(account_subject_ref);

        //获取链上mini url的存证信息 并检查是否包含uri信息 每个登记都是新的 则都是0
        String regfileName1 = conJGFileName(tempObjId,regVer);
        String chkRegURI1 = regfileName1;

        log.info(uriInfo.get("storeData").toString());
        assertEquals(true,uriInfo.get("storeData").toString().contains(chkRegURI1));
        assertEquals(true,gdCF.bContainJGFlag(uriInfo.get("storeData").toString()));//确认meta信息包含监管关键字

        //直接从minio上获取报送数据文件信息
        Map getRegInfo1 = gdCF.constructJGDataFromStr(regfileName1,regType,"");

        regInfo.put("content",gdCF.constructContentMap(regType,tempObjId,regVer,"create",String.valueOf(ts5)));
        log.info("检查登记存证信息内容与传入一致\n" + regInfo.toString() + "\n" + getRegInfo1.toString());
        bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(regInfo,regType)),replaceCertain(getRegInfo1.toString()));
        assertEquals("1检查数据是否一致" ,true,bSame);

        log.info("================================检查存证数据格式化《结束》================================");

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

    }

    @Test
    public void TC11_shareUnlock() throws Exception {
        sleepAndSaveInfo(3000);

        String bizNo = bizNoTest;
        String eqCode = gdEquityCode;
        long amount = 500;


        //登记数据
        Map regInfo = gdBF.init05RegInfo();
        String tempObjId = mapAccAddr.get(gdAccount1).toString() + Random(5);
        regNo = "Eq" + "unlock" + bizNo + (new Date()).getTime();   //区分不同类型的交易登记以流水号
        regInfo.put("register_serial_number",regNo);       //更新对比的登记流水号
        regInfo.put("register_registration_object_id",tempObjId);

        String response= gd.GDShareUnlock(bizNo,eqCode,amount,regInfo);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");
        String txDetail = store.GetTxDetail(txId);
        assertEquals("200", net.sf.json.JSONObject.fromObject(txDetail).getString("state"));

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

        //直接从minio上获取报送数据文件信息
        Map getRegInfo1 = gdCF.constructJGDataFromStr(regfileName1,regType,"");

        regInfo.put("content",gdCF.constructContentMap(regType,tempObjId,regVer,"create",String.valueOf(ts5)));
        log.info("检查登记存证信息内容与传入一致\n" + regInfo.toString() + "\n" + getRegInfo1.toString());
        bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(regInfo,regType)),replaceCertain(getRegInfo1.toString()));
        assertEquals("1检查数据是否一致" ,true,bSame);

        log.info("================================检查存证数据格式化《结束》================================");


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

    }

    @Test
    public void TC1201_shareRecycleOneAcc() throws Exception {
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

        List<Map> shareList = gdConstructShareList(address,recycleAmount,0);

        String response= gd.GDShareRecycle(gdPlatfromKeyID,eqCode,shareList,remark);
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
        Map uriInfo = gdCF.getJGURIStoreHash(txId,conJGFileName(mapAddrRegObjId.get(address).toString(),"0"),1);
        //获取监管数据存证hash
        String tempAddr = address;
        String tempObjId = mapAddrRegObjId.get(tempAddr).toString();
        String regVer = "0" ;
        String subVer = gdCF.getObjectLatestVer(gdCompanyID);

        //获取链上mini url的存证信息 并检查是否包含uri信息 每个登记都是新的 则都是0
        String regfileName = conJGFileName(tempObjId,regVer);
        String subfileName = conJGFileName(gdCompanyID,subVer);
        String chkRegURI = regfileName;
        String chkSubURI = subfileName;
        assertEquals(true,uriInfo.get("storeData").toString().contains(chkRegURI));
        assertEquals(true,uriInfo.get("storeData").toString().contains(chkSubURI));
//        assertEquals(true,gdCF.bContainJGFlag(uriInfo.get("storeData").toString()));//确认meta信息包含监管关键字
        log.info("检查增发存证产品格式化及信息内容与传入一致");

        //直接从minio上获取报送数据文件信息
        Map getRegInfo = gdCF.constructJGDataFromStr(regfileName,regType,"");

        Map regInfoInput = gdBF.init05RegInfo();
        regInfoInput.put("register_registration_object_id",mapAddrRegObjId.get(tempAddr));
        regInfoInput.put("content",gdCF.constructContentMap(regType,tempObjId,regVer,"create",String.valueOf(ts5)));

        log.info("检查登记存证信息内容与传入一致\n" + regInfoInput.toString() + "\n" + getRegInfo.toString());
        bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(regInfoInput,regType)),replaceCertain(getRegInfo.toString()));
//        assertEquals("检查增发登记数据是否一致" ,true,bSame);

        log.info("检查回收存证主体格式化及信息内容与传入一致");


        Map enSub = gdBF.init01EnterpriseSubjectInfo();
        //更新股东数量 非用户传入的数据 而是依据前面转让给新账户后的股东数
        enSub.put("subject_shareholders_number",totalMembers);

        enSub.put("content",gdCF.constructContentMap(subjectType, subObjId, subVer, "update", String.valueOf(ts1)));
        Map getEnSubInfo = gdCF.constructJGDataFromStr(conJGFileName(subObjId, subVer), subjectType, "1");
        bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(enSub,subjectType)),replaceCertain(getEnSubInfo.toString()));
        assertEquals("检查增发交易报告数据是否一致" ,true,bSame);
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

        regNo = "Eq" + "recylce2" + (new Date()).getTime();   //区分不同类型的交易登记以流水号
        registerInfo.put("register_registration_serial_number",regNo);       //更新对比的登记流水号

        List<Map> shareList = gdConstructShareList(gdAccount1,100,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,100,0,shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,100,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,100,0, shareList3);

        String response= gd.GDShareRecycle(gdPlatfromKeyID,eqCode,shareList4,remark);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));


        log.info("================================检查存证数据格式化《开始》================================");

        Map uriInfo = gdCF.getJGURIStoreHash(txId,conJGFileName(mapAddrRegObjId.get(gdAccount1).toString(),"0"),1);

        for(int k = 0;k < shareList4.size();k ++) {
            String tempAddr = JSONObject.fromObject(shareList4.get(k)).getString("address");
            String tempObjId = mapAddrRegObjId.get(tempAddr).toString();
            String regVer = "0" ;//gdCF.getObjectLatestVer(account_subject_ref);

            //获取链上mini url的存证信息 并检查是否包含uri信息 每个登记都是新的 则都是0
            String regfileName = conJGFileName(tempObjId,regVer);
            String chkRegURI = regfileName;
            assertEquals(true,uriInfo.get("storeData").toString().contains(chkRegURI));
//            assertEquals(true,gdCF.bContainJGFlag(uriInfo.get("storeData").toString()));//确认meta信息包含监管关键字

            //直接从minio上获取报送数据文件信息
            Map getRegInfo = gdCF.constructJGDataFromStr(regfileName,regType,"");

            Map regInfoInput = gdBF.init05RegInfo();
            regInfoInput.put("register_registration_object_id",tempObjId);
            regInfoInput.put("content",gdCF.constructContentMap(regType,tempObjId,regVer,"create",String.valueOf(ts5)));

            log.info("检查登记存证信息内容与传入一致\n" + regInfoInput.toString() + "\n" + getRegInfo.toString());
            bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(regInfoInput,regType)),replaceCertain(getRegInfo.toString()));
            assertEquals(tempObjId + "检查回收登记报告数据是否一致" ,true,bSame);
        }

        log.info("检查回收存证主体格式化及信息内容与传入一致");
        Map enSub = gdBF.init01EnterpriseSubjectInfo();
        //更新股东数量 非用户传入的数据 而是依据前面转让给新账户后的股东数
        enSub.put("subject_shareholders_number",totalMembers);

        String subObjId = gdCompanyID;
        String subVer = gdCF.getObjectLatestVer(subObjId);
        enSub.put("content",gdCF.constructContentMap(subjectType, subObjId, subVer, "update", String.valueOf(ts1)));
        Map getEnSubInfo = gdCF.constructJGDataFromStr(conJGFileName(subObjId, subVer), subjectType, "1");
        bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(enSub,subjectType)),replaceCertain(getEnSubInfo.toString()));
        assertEquals("检查增发交易报告数据是否一致" ,true,bSame);

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
    }

    @Test
    public void TC13_shareChangeBoard() throws Exception {

        String oldEquityCode = gdEquityCode;
        String newEquityCode = gdEquityCode + "new" + Random(2);
        String cpnyId = gdCompanyID;

        regNo = "Eq" + "changeboard" + (new Date()).getTime();   //区分不同类型的交易登记以流水号
        List<Map> regList = uf.getAllHolderListReg(gdEquityCode,regNo);
        registerInfo = gdBF.init05RegInfo();

        Map eqProd = gdBF.init03EquityProductInfo();
        eqProd.put("product_object_id",newEquityCode);

        String response= gd.GDShareChangeBoard(gdPlatfromKeyID,cpnyId,oldEquityCode,newEquityCode,regList,eqProd,null);
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

        Map uriInfo = gdCF.getJGURIStoreHash(txId,conJGFileName(mapAddrRegObjId.get(gdAccount1).toString(),"0"),1);

        //遍历检查所有账户登记及交易存证信息
        for(int k = 0 ;k < dataShareList.size(); k++) {
            String tempAddr = JSONObject.fromObject(dataShareList.get(k)).getString("address");
            String tempObjId = mapAddrRegObjId.get(tempAddr).toString();
            String regVer = "0" ;//gdCF.getObjectLatestVer(account_subject_ref);

            //获取链上mini url的存证信息 并检查是否包含uri信息 每个登记都是新的 则都是0
            String regfileName = conJGFileName(tempObjId,regVer);
            String chkRegURI = regfileName;
            assertEquals(true,uriInfo.get("storeData").toString().contains(chkRegURI));
            assertEquals(true,gdCF.bContainJGFlag(uriInfo.get("storeData").toString()));//确认meta信息包含监管关键字

            //直接从minio上获取报送数据文件信息
            Map getRegInfo = gdCF.constructJGDataFromStr(regfileName,regType,"");

            Map regInfoInput = gdBF.init05RegInfo();
            regInfoInput.put("register_registration_object_id",tempObjId);
            regInfoInput.put("content",gdCF.constructContentMap(regType,tempObjId,regVer,"create",String.valueOf(ts5)));

            log.info("检查登记存证信息内容与传入一致\n" + regInfoInput.toString() + "\n" + getRegInfo.toString());
            bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(regInfoInput,regType)),replaceCertain(getRegInfo.toString()));
//            assertEquals(tempObjId + "检查发行登记报告数据是否一致" ,true,bSame);
        }
        log.info("检查场内转板存证产品格式化及信息内容与传入一致");
        String oldProdVer = gdCF.getObjectLatestVer(oldEquityCode);
        String newEqProdVer = gdCF.getObjectLatestVer(newEquityCode);

//        assertEquals("0",newEqProdVer);

        //检查历史
        Map getOldProInfo = gdCF.constructJGDataFromStr(conJGFileName(oldEquityCode, oldProdVer), prodType, "1");
        eqProd.put("product_code",oldEquityCode);
        eqProd.put("content",gdCF.constructContentMap(prodType, oldEquityCode, oldProdVer, "delete", String.valueOf(ts8)));
        log.info("检查转板前产品存证信息内容与传入一致\n" + eqProd.toString() + "\n" + getOldProInfo.toString());
        bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(eqProd, prodType)), replaceCertain(getOldProInfo.toString()));
//        assertEquals("检查增发产品是否一致" ,true,bSame);

        product_issuer_subject_ref = gdCompanyID;
        eqProd.put("product_code",newEquityCode);
        //检查新产品
        Map getNewProInfo = gdCF.constructJGDataFromStr(conJGFileName(newEquityCode, newEqProdVer), prodType, "1");
        eqProd.put("content",gdCF.constructContentMap(prodType, newEquityCode, newEqProdVer, "create", String.valueOf(ts3)));
        log.info("检查转板后产品存证信息内容与传入一致\n" + eqProd.toString() + "\n" + getNewProInfo.toString());
        bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(eqProd, prodType)), replaceCertain(getNewProInfo.toString()));
        assertEquals("检查增发产品是否一致" ,true,bSame);

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
    }


    @Test
    public void TC205_accountDestroy() throws Exception {
        log.info("销户前查询个人主体信息");
        String cltNo = gdAccClientNo10;
        int gdClient = Integer.parseInt(gdCF.getObjectLatestVer(cltNo));//获取当前开户主体最新版本信息

        String response= gd.GDAccountDestroy(gdContractAddress,cltNo,date4,getListFileObj(),date4,getListFileObj());
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        String txDetail = store.GetTxDetail(txId);
        assertEquals("200", net.sf.json.JSONObject.fromObject(txDetail).getString("state"));

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
        Map getFundAccInfo = gdCF.constructJGDataFromStr(fundAccfileName,accType,"1");
        Map getSHAccInfo = gdCF.constructJGDataFromStr(shAccfileName,accType,"2");


        Map accFund = fundAccountInfo;
        Map accSH = shAccountInfo;


        accFund.put("content",gdCF.constructContentMap(accType,fundObjId,fundAccVer,"delete",String.valueOf(ts8)));
        accSH.put("content",gdCF.constructContentMap(accType,SHObjId,shAccVer,"delete",String.valueOf(ts8)));

        //确认投资者主体对象标识版本不变
        assertEquals(String.valueOf(gdClient),gdCF.getObjectLatestVer(cltNo));

        //账户的如下字段默认引用的是开户主体的对象标识
        account_subject_ref = cltNo;

        log.info("检查股权账户存证信息内容与传入一致\n" + accSH.toString() + "\n" + getSHAccInfo.toString());
        assertEquals(replaceCertain(gdCF.matchRefMapCertVer2(accSH,accType)),replaceCertain(getSHAccInfo.toString()));

        account_associated_account_ref = SHObjId;

        log.info("检查资金账户存证信息内容与传入一致\n" + accFund.toString() + "\n" + getFundAccInfo.toString());
        assertEquals(replaceCertain(gdCF.matchRefMapCertVer2(accFund,accType)),replaceCertain(getFundAccInfo.toString()));

    }
}
