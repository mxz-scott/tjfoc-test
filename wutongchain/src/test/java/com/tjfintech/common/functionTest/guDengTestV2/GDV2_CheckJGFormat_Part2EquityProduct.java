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

//    @After
    public void calJGDataAfterTx()throws Exception{
        testCurMethodName = tm.getMethodName();
        GDUnitFunc uf = new GDUnitFunc();
//        uf.calJGData();
        uf.calJGDataEachHeight();
    }

    @Test
    public void TC06_shareIssue() throws Exception {
        log.info(registerInfo.toString());
        mapAddrRegObjId.clear(); //先清空map 这样后面map中拿到的就是此次发行的登记对象标识
        regNo = "Eq" + "issue" + (new Date()).getTime();   //区分不同类型的交易登记以流水号
        registerInfo.put("register_registration_serial_number",regNo);       //更新对比的登记流水号

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
            String chkRegURI = minIOEP + "/" + jgBucket + "/" + regfileName;
            assertEquals(true,uriInfo.get("storeData").toString().contains(chkRegURI));
//        assertEquals(false,uriInfo.get("storeData").toString().contains("meta"));//确认meta信息移除

            //直接从minio上获取报送数据文件信息
            Map getRegInfo = gdCF.constructJGDataFromStr(regfileName,regType,"");

            Map regInfoInput = gdBF.init05RegInfo();
            regInfoInput.put("register_registration_object_id",mapAddrRegObjId.get(tempAddr));
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
            assertEquals(replaceCertain(gdCF.matchRefMapCertVer2(regInfoInput,regType)),replaceCertain(getRegInfo.toString()));
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
        testReg1.put("register_registration_serial_number","ChangeProperty000001");
        testReg1.put("register_account_obj_id",mapAccAddr.get(address));
        testReg1.put("register_registration_object_id",regObjId1);
        testReg1.put("register_nature_of_shares", oldProperty);

        testReg2.put("register_registration_serial_number","ChangeProperty000001");
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
//        assertEquals("不包含敏感词",true,gdCF.chkSensitiveWord(txDetail,regType));

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
        String chkRegURI1 = minIOEP + "/" + jgBucket + "/" + regfileName1;
        String chkRegURI2 = minIOEP + "/" + jgBucket + "/" + regfileName2;

        log.info(uriInfo.get("storeData").toString());
        log.info(chkRegURI1 + "      " + chkRegURI2);
        assertEquals(true,uriInfo.get("storeData").toString().contains(chkRegURI1));
        assertEquals(true,uriInfo.get("storeData").toString().contains(chkRegURI2));
        assertEquals(true,uriInfo.get("storeData").toString().contains("\"meta\":null"));//确认meta信息移除

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

        //登记数据
        String tempObjIdFrom = mapAccAddr.get(gdAccount1).toString() + Random(5);
        String tempObjIdTo = mapAccAddr.get(gdAccount5).toString() + Random(5);

        Map fromNow = gdBF.init05RegInfo();
        Map toNow = gdBF.init05RegInfo();

        regNo = "Eq" + "transfer" + (new Date()).getTime();   //区分不同类型的交易登记以流水号
        fromNow.put("register_registration_serial_number",regNo);       //更新对比的登记流水号
        toNow.put("register_registration_serial_number",regNo);       //更新对比的登记流水号

        fromNow.put("register_registration_object_id",tempObjIdFrom);
        toNow.put("register_registration_object_id",tempObjIdTo);

        //交易报告数据
        Map txInfo = gdBF.init04TxInfo();
        String txRpObjId = "txReport" + Random(6);
        txInfo.put("transaction_object_id",txRpObjId);

        String query2 = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);
        //执行交易
        String response= gd.GDShareTransfer(keyId,fromAddr,transferAmount,toAddr,shareProperty,eqCode,txInfo,fromNow,toNow);
        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        String txDetail = store.GetTxDetail(txId);
        assertEquals("200", net.sf.json.JSONObject.fromObject(txDetail).getString("state"));

        //检查各个查询对象返回信息中不包含敏感词
        assertEquals("不包含登记数据敏感词",true,gdCF.chkSensitiveWord(txDetail,regType));
//        assertEquals("不包含交易报告敏感词",true,gdCF.chkSensitiveWord(txDetail,txrpType));

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





        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
//        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
//        assertEquals("200", JSONObject.fromObject(query).getString("state"));
//        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");

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

        String chkRegURI1 = minIOEP + "/" + jgBucket + "/" + regfileName1;
        String chkRegURI2 = minIOEP + "/" + jgBucket + "/" + regfileName2;
        String chkTxRpURI = minIOEP + "/" + jgBucket + "/" + txfileName;
        String chkSubURI = minIOEP + "/" + jgBucket + "/" + subfileName;

        log.info(uriInfo.get("storeData").toString());
        log.info(chkRegURI1 + "      " + chkRegURI2);
        assertEquals(true,uriInfo.get("storeData").toString().contains(chkRegURI1));
        assertEquals(true,uriInfo.get("storeData").toString().contains(chkRegURI2));
        assertEquals(true,uriInfo.get("storeData").toString().contains(chkTxRpURI));
        assertEquals(true,uriInfo.get("storeData").toString().contains(chkSubURI));
        assertEquals(true,uriInfo.get("storeData").toString().contains("\"meta\":null"));//确认meta信息移除

        //直接从minio上获取报送数据文件信息
        Map getRegInfo1 = gdCF.constructJGDataFromStr(regfileName1,regType,"");
        Map getRegInfo2 = gdCF.constructJGDataFromStr(regfileName2,regType,"");
        Map getTxRpInfo = gdCF.constructJGDataFromStr(txfileName,txrpType,"");
        Map getSubInfo = gdCF.constructJGDataFromStr(subfileName,subjectType,"");

        Map regInfoInput = gdBF.init05RegInfo();
        regInfoInput.put("register_registration_object_id",tempObjIdFrom);
        regInfoInput.put("content",gdCF.constructContentMap(regType,tempObjIdFrom,regVer,"create",String.valueOf(ts5)));
        log.info("检查登记存证信息内容与传入一致\n" + regInfoInput.toString() + "\n" + getRegInfo1.toString());
        bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(regInfoInput,regType)),replaceCertain(getRegInfo1.toString()));
//        assertEquals("from检查数据是否一致" ,true,bSame);
        log.info("from登记检查数据是否一致" + bSame);
        //        assertEquals(replaceCertain(gdCF.matchRefMapCertVer2(regInfoInput,regType)),replaceCertain(getRegInfo1.toString()));


        regInfoInput.put("register_registration_object_id",tempObjIdTo);
        regInfoInput.put("content",gdCF.constructContentMap(regType,tempObjIdTo,regVer,"create",String.valueOf(ts5)));
        log.info("检查登记存证信息内容与传入一致\n" + regInfoInput.toString() + "\n" + getRegInfo1.toString());
        bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(regInfoInput,regType)),replaceCertain(getRegInfo2.toString()));
        log.info("to登记检查数据是否一致" + bSame);
        //        assertEquals("to检查数据是否一致" ,true,bSame);
//        assertEquals(replaceCertain(gdCF.matchRefMapCertVer2(regInfoInput,regType)),replaceCertain(getRegInfo2.toString()));

        txInfo.put("content",gdCF.constructContentMap(txrpType,txRpObjId,txRpVer,"create",String.valueOf(ts4)));
        bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(txInfo,txrpType)),replaceCertain(getTxRpInfo.toString()));
        log.info("交易报告检查数据是否一致" + bSame);
//        assertEquals("to检查数据是否一致" ,true,bSame);


        //填充header content字段
        Map enSubInfo = gdBF.init01EnterpriseSubjectInfo();
        enSubInfo.put("content",gdCF.constructContentMap(subjectType,gdCompanyID,subVer,"update",String.valueOf(ts1)));
        log.info("检查主体存证信息内容与传入一致\n" + enSubInfo.toString() + "\n" + getSubInfo.toString());
        bSame = commonFunc.compareTwoStr(replaceCertain(gdCF.matchRefMapCertVer2(enSubInfo,subjectType)),replaceCertain(getSubInfo.toString()));
//        assertEquals("to检查数据是否一致" ,true,bSame);


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

        regNo = "Eq" + "increase" + (new Date()).getTime();   //区分不同类型的交易登记以流水号
        registerInfo.put("register_serial_number",regNo);       //更新对比的登记流水号

        List<Map> shareList = gdConstructShareList(gdAccount1,increaseAmount,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,increaseAmount,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,increaseAmount,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,increaseAmount,0, shareList3);

        String response= gd.GDShareIncrease(gdPlatfromKeyID,eqCode,shareList4,reason, equityProductInfo);
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
        //获取监管数据存证hash
        String jgType = regType;
        String regStoreId = gdCF.getJGStoreHash2(txId,jgType,1);
        jgType = prodType;
        String prodStoreId = gdCF.getJGStoreHash2(txId,jgType,1);
        jgType = subjectType;
        String subStoreId = gdCF.getJGStoreHash2(txId,jgType,1);

        //遍历检查所有账户登记及交易存证信息
        for(int k = 0 ;k < shareList4.size(); k++) {
            String tempAddr = JSONObject.fromObject(shareList4.get(k)).getString("address");
            String tempObjId = mapAccAddr.get(tempAddr).toString();

            registerInfo = gdBF.init05RegInfo();

            log.info("检查增发存证登记格式化及信息内容与传入一致");
            registerInfo.put("register_account_obj_id",tempObjId);
//            registerInfo.put("register_rights_change_amount",increaseAmount);     //变动额修改为单个账户发行数量
            log.info(gdCF.contructRegisterInfo(regStoreId,4,tempObjId).toString().replaceAll("\"",""));
            log.info(registerInfo.toString());
            assertEquals(registerInfo.toString(),
                    gdCF.contructRegisterInfo(regStoreId,4,tempObjId).toString().replaceAll("\"","").replaceAll(":","="));

            log.info("检查增发存证产品格式化及信息内容与传入一致");

            log.info(gdCF.contructEquityProdInfo(prodStoreId).toString().replaceAll("\"",""));
            log.info(equityProductInfo.toString());
            assertEquals(replaceCertain(equityProductInfo.toString()),
                    replaceCertain(gdCF.contructEquityProdInfo(prodStoreId).toString()));
        }

        log.info("检查增发存证主体格式化及信息内容与传入一致");
        String getTotalMem = enterpriseSubjectInfo.get("subject_shareholders_number").toString();
        int oldTotalMem = Integer.parseInt(getTotalMem);
        //依赖之前的转让触发的主体数据变更 不需要额外更新
//        enterpriseSubjectInfo.put("subject_shareholders_number",oldTotalMem);     //变更总股本数为增发量 + 原始股本总数

//        String getTotal = enterpriseSubjectInfo.get("subject_total_share_capital").toString();
//        BigDecimal oldTotal = new BigDecimal(getTotal);
//        enterpriseSubjectInfo.put("subject_total_share_capital",oldTotal.add(new BigDecimal(increaseAmount*4)));     //变更总股本数为增发量 + 原始股本总数
//        enterpriseSubjectInfo.put("subject_object_information_type",1); //触发主体变更 类型变更为1
        log.info(gdCF.contructEnterpriseSubInfo(subStoreId).toString().replaceAll("\"",""));
        log.info(enterpriseSubjectInfo.toString());
        assertEquals(replaceCertain(enterpriseSubjectInfo.toString()), replaceCertain(gdCF.contructEnterpriseSubInfo(subStoreId).toString()));

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
        String chkRegURI1 = minIOEP + "/" + jgBucket + "/" + regfileName1;

        log.info(uriInfo.get("storeData").toString());
        assertEquals(true,uriInfo.get("storeData").toString().contains(chkRegURI1));
        assertEquals(true,uriInfo.get("storeData").toString().contains("\"meta\":null"));//确认meta信息移除

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
        String chkRegURI1 = minIOEP + "/" + jgBucket + "/" + regfileName1;

        log.info(uriInfo.get("storeData").toString());
        assertEquals(true,uriInfo.get("storeData").toString().contains(chkRegURI1));
        assertEquals(true,uriInfo.get("storeData").toString().contains("\"meta\":null"));//确认meta信息移除

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
        String query2 = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);
//        BigDecimal totalShares = new BigDecimal(JSONObject.fromObject(query2).getJSONObject("data").getString("subject_total_share_capital"));

        registerInfo = gdBF.init05RegInfo();
        regNo = "Eq" + "recylce" + (new Date()).getTime();   //区分不同类型的交易登记以流水号
        registerInfo.put("register_registration_serial_number",regNo);       //更新对比的登记流水号

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

        //获取监管数据存证hash
        String jgType = regType;
        String regStoreId = gdCF.getJGStoreHash2(txId,jgType,1);
        jgType = subjectType;
        String subStoreId = gdCF.getJGStoreHash2(txId,jgType,1);

        String tempObjId = mapAccAddr.get(address).toString();
        log.info("检查回收存证登记格式化及信息内容与传入一致");
//        registerInfo.put("register_rights_change_amount",(-1) * recycleAmount);     //变动额修改为单个账户发行数量
        registerInfo.put("register_account_obj_id",mapAccAddr.get(address).toString());     //变动额修改为单个账户发行数量
//        registerInfo.put("register_rights_frozen_change_amount",10000);     //变动额修改为单个账户发行数量
        log.info(gdCF.contructRegisterInfo(regStoreId,1,tempObjId).toString().replaceAll("\"",""));
        log.info(registerInfo.toString());
        assertEquals(registerInfo.toString(), gdCF.contructRegisterInfo(regStoreId,1,tempObjId).toString().replaceAll("\"",""));


        log.info("检查回收存证主体格式化及信息内容与传入一致");

//        String getTotal = enterpriseSubjectInfo.get("subject_total_share_capital").toString();
//        BigDecimal oldTotal = new BigDecimal(getTotal);
//        enterpriseSubjectInfo.put("subject_total_share_capital",oldTotal.subtract(new BigDecimal(recycleAmount)));     //变更总股本数为增发量 + 原始股本总数
        log.info(gdCF.contructEnterpriseSubInfo(subStoreId).toString().replaceAll("\"",""));
//        enterpriseSubjectInfo.put("subject_object_information_type",1);
        log.info(enterpriseSubjectInfo.toString());
        assertEquals(replaceCertain(enterpriseSubjectInfo.toString()), replaceCertain(gdCF.contructEnterpriseSubInfo(subStoreId).toString()));

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
        String query2 = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);
//        BigDecimal totalShares = new BigDecimal(JSONObject.fromObject(query2).getJSONObject("data").getString("subject_total_share_capital"));

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
        //获取监管数据存证hash
        String jgType = regType;
        String regStoreId = gdCF.getJGStoreHash2(txId,jgType,1);
        jgType = subjectType;
        String subStoreId = gdCF.getJGStoreHash2(txId,jgType,1);

        for(int k = 0;k < shareList4.size();k ++) {
            String tempAddr = JSONObject.fromObject(shareList4.get(k)).getString("address");
            String tempObjId = mapAccAddr.get(tempAddr).toString();
            String tempAmount = JSONObject.fromObject(shareList4.get(k)).getString("amount");

            log.info("检查回收存证登记格式化及信息内容与传入一致");
//            registerInfo.put("register_rights_change_amount", "-100");     //变动额修改为单个账户发行数量
            registerInfo.put("register_account_obj_id",tempObjId);     //变动额修改为单个账户发行数量
//            registerInfo.put("register_rights_frozen_change_amount",10000);     //变动额修改为单个账户发行数量
            log.info(gdCF.contructRegisterInfo(regStoreId, 4,tempObjId).toString().replaceAll("\"", ""));
            log.info(registerInfo.toString());
            assertEquals(registerInfo.toString(), gdCF.contructRegisterInfo(regStoreId, 4,tempObjId).toString().replaceAll("\"", ""));

        }
        log.info("检查回收存证主体格式化及信息内容与传入一致");

//        String getTotal = enterpriseSubjectInfo.get("subject_total_share_capital").toString();
//        BigDecimal oldTotal = new BigDecimal(getTotal);
//        enterpriseSubjectInfo.put("subject_total_share_capital",oldTotal.subtract(new BigDecimal(recycleAmount * 4)));     //变更总股本数为增发量 + 原始股本总数
        log.info(gdCF.contructEnterpriseSubInfo(subStoreId).toString().replaceAll("\"",""));
//        enterpriseSubjectInfo.put("subject_object_information_type",1);
        log.info(enterpriseSubjectInfo.toString());
        assertEquals(replaceCertain(enterpriseSubjectInfo.toString()), replaceCertain(gdCF.contructEnterpriseSubInfo(subStoreId).toString()));

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
        String newEquityCode = gdEquityCode + Random(5);
        String cpnyId = gdCompanyID;

        regNo = "Eq" + "changeboard" + (new Date()).getTime();   //区分不同类型的交易登记以流水号
        List<Map> regList = uf.getAllHolderListReg(gdEquityCode,regNo);
        registerInfo = gdBF.init05RegInfo();

        String response= gd.GDShareChangeBoard(gdPlatfromKeyID,cpnyId,oldEquityCode,newEquityCode,regList,equityProductInfo,null);
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

        //获取监管数据存证hash
        String jgType = regType;
        String regStoreId = gdCF.getJGStoreHash2(txId,jgType,1);
        jgType = prodType;
        String prodStoreId = gdCF.getJGStoreHash2(txId,jgType,1);


        //遍历检查所有账户登记及交易存证信息
        for(int k = 0 ;k < dataShareList.size(); k++) {
            String tempAddr = JSONObject.fromObject(dataShareList.getString(k)).getString("address");
            //零地址则直接下一个
            if(tempAddr.equals(zeroAccount)) continue;

            String tempObjId = mapAccAddr.get(tempAddr).toString();
            String tempAmount = JSONObject.fromObject(dataShareList.getString(k)).getString("amount");

            log.info("================================检查存证数据格式化《开始》================================");

            log.info("检查场内转板存证登记格式化及信息内容与传入一致:" + tempObjId);
            registerInfo.put("register_account_obj_id",tempObjId);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            String sd = sdf.format(new Date(onChainTS)); // 时间戳转换日期

//            registerInfo.put("register_rights_change_amount", tempAmount);//有问题 数据不对
//            registerInfo.put("register_available_balance", tempAmount);//有问题 数据不对
//            registerInfo.put("register_time", sd);
            registerInfo.put("register_registration_serial_number", regNo);
//            registerInfo.put("register_creditor_subscription_count", 0);
            log.info(gdCF.contructRegisterInfo(regStoreId, dataShareList.size() + 2,tempObjId).toString().replaceAll("\"", ""));
            log.info(registerInfo.toString());
            assertEquals(registerInfo.toString(), gdCF.contructRegisterInfo(regStoreId, dataShareList.size() + 2,tempObjId).toString().replaceAll("\"", ""));

        }
        log.info("检查场内转板存证产品格式化及信息内容与传入一致");

        log.info(gdCF.contructEquityProdInfo(prodStoreId).toString().replaceAll("\"",""));
        log.info(equityProductInfo.toString());
        equityProductInfo.put("product_code",gdEquityCode);
        assertEquals(replaceCertain(equityProductInfo.toString()),
                replaceCertain(gdCF.contructEquityProdInfo(prodStoreId).toString()));

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
        String query2 = gd.GDMainSubjectQuery(gdContractAddress,gdAccClientNo10);
        assertEquals("200", JSONObject.fromObject(query2).getString("state"));

        String clntNo = gdAccClientNo10;
        String[] cert1 = new String[]{"test.txt"};
        String[] cert2 = new String[]{"close.cert"};
        String response= gd.GDAccountDestroy(gdContractAddress,clntNo,
                date4,getListFileObj(),date4,getListFileObj());
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        //获取上链交易时间戳
        long onChainTS = JSONObject.fromObject(store.GetTxDetail(txId)).getJSONObject("data").getJSONObject("header").getLong("timestamp");

        String query3 = gd.GDMainSubjectQuery(gdContractAddress,gdAccClientNo10);

        log.info("================================检查存证数据格式化《开始》================================");
        //获取监管数据存证hash
        String storeId = gdCF.getJGStoreHash(txId,1);

        log.info("检查销户存证资金账户格式化及信息内容与传入一致");
        fundAccountInfo.put("account_status",1);  //变更账号状态为1

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String sd = sdf.format(new Date(onChainTS)); // 时间戳转换日期
        fundAccountInfo.put("account_closing_date",sd);
        fundAccountInfo.put("account_closing_certificate","2.txt");
        log.info(gdCF.contructFundAccountInfo(storeId,"fund" + clntNo).toString().replaceAll("\"",""));
        log.info(fundAccountInfo.toString());
        assertEquals(fundAccountInfo.toString().replaceAll(" ","").replaceAll("\\[","").replaceAll("]",""),
                gdCF.contructFundAccountInfo(storeId,clntNo).toString().replaceAll("\"","").replaceAll(" ",""));

        log.info("检查销户存证股权账户格式化及信息内容与传入一致");
        shAccountInfo.put("account_status",1);  //变更账号状态为1
        shAccountInfo.put("account_closing_date",sd);
        shAccountInfo.put("account_closing_certificate","1.txt");
        log.info(gdCF.contructEquityAccountInfo(storeId,"SH" + clntNo).toString().replaceAll("\"",""));
        log.info(shAccountInfo.toString());
        assertEquals(shAccountInfo.toString().replaceAll(" ","").replaceAll("\\[","").replaceAll("]",""),
                gdCF.contructEquityAccountInfo(storeId,clntNo).toString().replaceAll("\"","").replaceAll(" ",""));
        log.info("================================检查存证数据格式化《结束》================================");

    }
}
