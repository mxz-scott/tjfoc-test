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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.functionTest.guDengTestV2.GDCommonFunc.*;
import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassGD.*;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class GDV2_CheckJGFormat_Part3SubjectChangeTxSend_NoComma {

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
    long transferAmount = issueAmount;
    String lockAddress = gdAccount1;

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
    }

    @Before
    public void TC06_shareIssue() throws Exception {
        gdEquityCode = "fondTest" + Random(12);

        regNo = "Eq" + "issue" + ( new Date()).getTime();   //区分不同类型的交易登记以流水号
        registerInfo.put("register_registration_serial_number",regNo);       //更新对比的登记流水号

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
        uf.updateBlockHeightParam(endHeight);
    }

    @Test
    public void shareIssueWithDiffShareProperty() throws Exception {
        gdEquityCode = "fondTest" + Random(12);
        log.info(registerInfo.toString());

        regNo = "Eq" + "issue" + (new Date()).getTime();   //区分不同类型的交易登记以流水号
        registerInfo = gdBF.init05RegInfo();
        registerInfo.put("register_registration_serial_number",regNo);       //更新对比的登记流水号

        List<Map> shareList = gdConstructShareList2(gdAccount1,issueAmount,0);
        List<Map> shareList2 = gdConstructShareList2(gdAccount1,issueAmount,1, shareList);
        List<Map> shareList3 = gdConstructShareList2(gdAccount2,issueAmount,0, shareList2);
        List<Map> shareList4 = gdConstructShareList2(gdAccount2,issueAmount,1,shareList3);

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
        //获取监管数据存证hash
        String storeId = gdCF.getJGStoreHash(txId,1);
        store.GetTxDetail(storeId);


        //遍历检查所有账户登记及交易存证信息
        for(int k = 0 ;k < dataShareList.size(); k++) {
            String tempAddr = JSONObject.fromObject(dataShareList.get(k)).getString("address");
            String tempObjId = mapAccAddr.get(tempAddr).toString();
            String tempPP = JSONObject.fromObject(dataShareList.get(k)).getString("shareProperty");

            log.info("检查发行存证登记格式化及信息内容与传入一致:" + tempObjId);
            registerInfo.put("register_account_obj_id",tempObjId);

            registerInfo.put("register_nature_of_shares", tempPP);
            registerInfo.put("register_rights_change_amount", issueAmount);     //变动额修改为单个账户发行数量
            registerInfo.put("register_rights_frozen_balance", 0);   //当前冻结余额修改为实际冻结数
            registerInfo.put("register_available_balance", issueAmount);   //当前当前可用余额修改为当前实际可用余额
            registerInfo.put("register_creditor_subscription_count", issueAmount);   //当前认购数量修改为当前实际余额
            registerInfo.put("register_rights_frozen_change_amount", 0);   //冻结变动额修改为当前实际冻结变更额
            log.info(gdCF.contructRegisterInfo(storeId, 4,tempObjId,tempPP).toString().replaceAll("\"", ""));
            log.info(registerInfo.toString());
            assertEquals(registerInfo.toString(), gdCF.contructRegisterInfo(storeId, 4,tempObjId,tempPP).toString().replaceAll("\"", ""));

            assertEquals("检查发行不包送交易报告数据",false,store.GetTxDetail(storeId).contains("\"type\":\"交易报告\""));

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


    //转让全部转出转给已存在的股东
    @Test
    public void shareTransfer_AllOut()throws Exception{

        String keyId = gdAccountKeyID1;
        String fromAddr = gdAccount1;
        String toAddr = gdAccount2;
        int shareProperty = 0;
        String eqCode = gdEquityCode;

        String tempObjIdFrom = mapAccAddr.get(fromAddr).toString();
        String tempObjIdTo = mapAccAddr.get(toAddr).toString();

        Map fromNow = gdBF.init05RegInfo();
        Map toNow = gdBF.init05RegInfo();

        regNo = "Eq" + "transfer" + (new Date()).getTime();   //区分不同类型的交易登记以流水号
        fromNow.put("register_registration_serial_number",regNo);       //更新对比的登记流水号
        toNow.put("register_registration_serial_number",regNo);       //更新对比的登记流水号
        fromNow.put("register_account_obj_id",tempObjIdFrom);       //更新对比的权利人账户引用
        toNow.put("register_account_obj_id",tempObjIdTo);       //更新对比的权利人账户引用

        txInformation.put("transaction_original_owner_subject_ref",tempObjIdFrom);

        log.info("转让前查询机构主体信息");
        String query2 = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);
        int totalHolderAccount = JSONObject.fromObject(query2).getJSONObject("data").getInt("subject_shareholders_number");

        //执行交易
        String response= gd.GDShareTransfer(keyId,fromAddr,transferAmount,toAddr,shareProperty,eqCode,txInformation,fromNow,toNow);

        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        sleepAndSaveInfo(SLEEPTIME);
        //获取上链交易时间戳
        long onChainTS = JSONObject.fromObject(store.GetTxDetail(txId)).getJSONObject("data").getJSONObject("header").getLong("timestamp");

        log.info("转让后查询机构主体信息");
        query2 = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);
        int totalHolderAccountAft = JSONObject.fromObject(query2).getJSONObject("data").getInt("subject_shareholders_number");

        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200", JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");

        log.info("================================检查存证数据格式化《开始》================================");
        //获取监管数据存证hash
        String storeId = gdCF.getJGStoreHash(txId, 1);

//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        String sd = sdf.format(new Date(onChainTS)); // 时间戳转换日期
        log.info("检查过户转让存证登记格式化及信息内容与传入一致:" + tempObjIdFrom);
//        fromNow.put("register_rights_change_amount","-" + transferAmount);
//        fromNow.put("register_time",txInformation.get("transaction_close_time").toString());
//        fromNow.put("register_available_balance",issueAmount - transferAmount);
//        fromNow.put("register_rights_frozen_balance", 0);   //当前冻结余额修改为实际冻结数
//
//        toNow.put("register_rights_change_amount",transferAmount);
//        toNow.put("register_rights_frozen_balance", 0);   //当前冻结余额修改为实际冻结数
//        toNow.put("register_time",txInformation.get("transaction_close_time").toString());
//        toNow.put("register_available_balance",issueAmount + transferAmount);
        log.info(gdCF.contructRegisterInfo(storeId, 3, tempObjIdFrom).toString().replaceAll("\"", ""));
        log.info(fromNow.toString());
        assertEquals(fromNow.toString(), gdCF.contructRegisterInfo(storeId,3,tempObjIdFrom).toString().replaceAll("\"",""));

        log.info("检查过户转让存证交易格式化及信息内容与传入一致:" + tempObjIdFrom);

        log.info(gdCF.contructTxInfo(storeId, 4, tempObjIdFrom).toString().replaceAll("\"", ""));
        log.info(txInformation.toString());
        assertEquals(txInformation.toString(), gdCF.contructTxInfo(storeId, 4, tempObjIdFrom).toString().replaceAll("\"", ""));

        log.info("检查过户转让存证登记格式化及信息内容与传入一致:" + tempObjIdTo);
        log.info(gdCF.contructRegisterInfo(storeId, 4, tempObjIdTo).toString().replaceAll("\"", ""));
        log.info(toNow.toString());
        assertEquals(toNow.toString(), gdCF.contructRegisterInfo(storeId,4,tempObjIdTo).toString().replaceAll("\"",""));


        log.info("检查转让存证主体格式化及信息内容与传入一致");

        //获取监管数据存证hash
        String jgType = subjectType;
        String subStoreId = gdCF.getJGStoreHash2(txId,jgType,1);

        String getTotal = enterpriseSubjectInfo.get("subject_shareholders_number").toString();
        int oldTotal = Integer.parseInt(getTotal);
        enterpriseSubjectInfo.put("subject_shareholders_number",oldTotal - 1);     //变更总股本数为增发量 + 原始股本总数
        log.info(gdCF.contructEnterpriseSubInfo(subStoreId).toString().replaceAll("\"",""));
//        enterpriseSubjectInfo.put("subject_object_information_type",1);
        log.info(enterpriseSubjectInfo.toString());
        assertEquals(enterpriseSubjectInfo.toString(), gdCF.contructEnterpriseSubInfo(subStoreId).toString().replaceAll("\"",""));

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

        assertEquals("检查总股东数变更是否正确",totalHolderAccount - 1,totalHolderAccountAft);
    }

    //使用不同币种和登记类型 针对开发提出更新数据中有更新这两个部分 但实际上不需要做强制变更的修改
    @Test
    public void shareTransfer_DiffMoneyType()throws Exception{

        String keyId = gdAccountKeyID1;
        String fromAddr = gdAccount1;
        String toAddr = gdAccount2;
        int shareProperty = 0;
        String eqCode = gdEquityCode;

        String tempObjIdFrom = mapAccAddr.get(fromAddr).toString();
        String tempObjIdTo = mapAccAddr.get(toAddr).toString();

        Map fromNow = gdBF.init05RegInfo();
        Map toNow = gdBF.init05RegInfo();

        regNo = "Eq" + "transfer" + (new Date()).getTime();   //区分不同类型的交易登记以流水号
        fromNow.put("register_registration_type",2);       //更新对比的登记流水号
        toNow.put("register_registration_type",3);       //更新对比的登记流水号
        fromNow.put("register_currency","860");       //更新对比的登记流水号
        toNow.put("register_currency","861");       //更新对比的登记流水号
        fromNow.put("register_registration_serial_number",regNo);       //更新对比的登记流水号
        toNow.put("register_registration_serial_number",regNo);       //更新对比的登记流水号
        fromNow.put("register_account_obj_id",tempObjIdFrom);       //更新对比的权利人账户引用
        toNow.put("register_account_obj_id",tempObjIdTo);       //更新对比的权利人账户引用

        txInformation.put("transaction_original_owner_subject_ref",tempObjIdFrom);

        log.info("转让前查询机构主体信息");
        String query2 = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);
        int totalHolderAccount = JSONObject.fromObject(query2).getJSONObject("data").getInt("subject_shareholders_number");

        //执行交易
        String response= gd.GDShareTransfer(keyId,fromAddr,transferAmount,toAddr,shareProperty,eqCode,txInformation,fromNow,toNow);

        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        sleepAndSaveInfo(SLEEPTIME);
        //获取上链交易时间戳
        long onChainTS = JSONObject.fromObject(store.GetTxDetail(txId)).getJSONObject("data").getJSONObject("header").getLong("timestamp");

        log.info("转让后查询机构主体信息");
        query2 = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);
        int totalHolderAccountAft = JSONObject.fromObject(query2).getJSONObject("data").getInt("subject_shareholders_number");

        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200", JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");

        log.info("================================检查存证数据格式化《开始》================================");
        //获取监管数据存证hash
        String storeId = gdCF.getJGStoreHash(txId, 1);

//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        String sd = sdf.format(new Date(onChainTS)); // 时间戳转换日期
        log.info("检查过户转让存证登记格式化及信息内容与传入一致:" + tempObjIdFrom);
//        fromNow.put("register_rights_change_amount","-" + transferAmount);
//        fromNow.put("register_time",txInformation.get("transaction_close_time").toString());
//        fromNow.put("register_available_balance",issueAmount - transferAmount);
//        fromNow.put("register_rights_frozen_balance", 0);   //当前冻结余额修改为实际冻结数
//
//        toNow.put("register_rights_change_amount",transferAmount);
//        toNow.put("register_rights_frozen_balance", 0);   //当前冻结余额修改为实际冻结数
//        toNow.put("register_time",txInformation.get("transaction_close_time").toString());
//        toNow.put("register_available_balance",issueAmount + transferAmount);
        log.info(gdCF.contructRegisterInfo(storeId, 3, tempObjIdFrom).toString().replaceAll("\"", ""));
        log.info(fromNow.toString());
        assertEquals(fromNow.toString(), gdCF.contructRegisterInfo(storeId,3,tempObjIdFrom).toString().replaceAll("\"",""));

        log.info("检查过户转让存证交易格式化及信息内容与传入一致:" + tempObjIdFrom);

        log.info(gdCF.contructTxInfo(storeId, 4, tempObjIdFrom).toString().replaceAll("\"", ""));
        log.info(txInformation.toString());
        assertEquals(txInformation.toString(), gdCF.contructTxInfo(storeId, 4, tempObjIdFrom).toString().replaceAll("\"", ""));

        log.info("检查过户转让存证登记格式化及信息内容与传入一致:" + tempObjIdTo);
        log.info(gdCF.contructRegisterInfo(storeId, 4, tempObjIdTo).toString().replaceAll("\"", ""));
        log.info(toNow.toString());
        assertEquals(toNow.toString(), gdCF.contructRegisterInfo(storeId,4,tempObjIdTo).toString().replaceAll("\"",""));


        log.info("检查转让存证主体格式化及信息内容与传入一致");

        //获取监管数据存证hash
        String jgType = subjectType;
        String subStoreId = gdCF.getJGStoreHash2(txId,jgType,1);

        String getTotal = enterpriseSubjectInfo.get("subject_shareholders_number").toString();
        int oldTotal = Integer.parseInt(getTotal);
        enterpriseSubjectInfo.put("subject_shareholders_number",oldTotal - 1);     //变更总股本数为增发量 + 原始股本总数
        log.info(gdCF.contructEnterpriseSubInfo(subStoreId).toString().replaceAll("\"",""));
//        enterpriseSubjectInfo.put("subject_object_information_type",1);
        log.info(enterpriseSubjectInfo.toString());
        assertEquals(enterpriseSubjectInfo.toString(), gdCF.contructEnterpriseSubInfo(subStoreId).toString().replaceAll("\"",""));

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

        assertEquals("检查总股东数变更是否正确",totalHolderAccount - 1,totalHolderAccountAft);
    }


    /***
     * 增发给新的股东 * 2
     * @throws Exception
     */
    @Test
    public void TC09_shareIncrease() throws Exception {

        log.info("增发前查询机构主体信息");
        String query2 = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);
//        BigDecimal totalShares = new BigDecimal(JSONObject.fromObject(query2).getJSONObject("data").getString("subject_total_share_capital"));
        int totalHolderAccount = JSONObject.fromObject(query2).getJSONObject("data").getInt("subject_shareholders_number");

        String eqCode = gdEquityCode;
        String reason = "股份分红";

        regNo = "Eq" + "increase" + (new Date()).getTime();   //区分不同类型的交易登记以流水号
        registerInfo.put("register_registration_serial_number",regNo);       //更新对比的登记流水号

        List<Map> shareList = gdConstructShareList2(gdAccount5,increaseAmount,0);
        List<Map> shareList4 = gdConstructShareList2(gdAccount6,increaseAmount,0, shareList);

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
            registerInfo.put("register_rights_frozen_balance","null");
            registerInfo.put("register_creditor_subscription_count","null");
            registerInfo.put("register_rights_frozen_change_amount","null");
            registerInfo.put("register_available_balance","null");
            registerInfo.put("register_rights_change_amount",increaseAmount);
            log.info(gdCF.contructRegisterInfo(regStoreId,2,tempObjId).toString().replaceAll("\"",""));
            log.info(registerInfo.toString());
            assertEquals(registerInfo.toString(), gdCF.contructRegisterInfo(regStoreId,2,tempObjId).toString().replaceAll("\"",""));

            log.info("检查增发存证产品格式化及信息内容与传入一致");
            log.info(gdCF.contructEquityProdInfo(prodStoreId).toString().replaceAll("\"",""));
            log.info(equityProductInfo.toString());
            assertEquals(equityProductInfo.toString().replaceAll("(\")?( )?",""),
                    gdCF.contructEquityProdInfo(prodStoreId).toString().replaceAll("(\")?( )?","").replaceAll(":","="));
        }

        log.info("检查增发存证主体格式化及信息内容与传入一致");
        String getTotalMem = enterpriseSubjectInfo.get("subject_shareholders_number").toString();
        int oldTotalMem = Integer.parseInt(getTotalMem);
        enterpriseSubjectInfo.put("subject_shareholders_number",oldTotalMem + 2);     //变更总股本数为增发量 + 原始股本总数

//        String getTotal = enterpriseSubjectInfo.get("subject_total_share_capital").toString();
//        BigDecimal oldTotal = new BigDecimal(getTotal);
//        enterpriseSubjectInfo.put("subject_total_share_capital",oldTotal.add(new BigDecimal(increaseAmount * 2)));     //变更总股本数为增发量 + 原始股本总数
        log.info(gdCF.contructEnterpriseSubInfo(subStoreId).toString().replaceAll("\"",""));
//        enterpriseSubjectInfo.put("subject_object_information_type",1);
        log.info(enterpriseSubjectInfo.toString());
        assertEquals(enterpriseSubjectInfo.toString(), gdCF.contructEnterpriseSubInfo(subStoreId).toString().replaceAll("\"",""));

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


        log.info("增发后查询机构主体信息");
        String query3 = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);
//        BigDecimal totalShares2 = new BigDecimal(JSONObject.fromObject(query3).getJSONObject("data").getString("subject_total_share_capital"));

        log.info("判断增发前后机构主体查询总股本数增加数正确");
//        assertEquals(totalShares.add(new BigDecimal("2000")),totalShares2);

        int totalHolderAccountAft = JSONObject.fromObject(query3).getJSONObject("data").getInt("subject_shareholders_number");

        assertEquals(totalHolderAccount + 2,totalHolderAccountAft);

    }



    /***
     * 增发给新的股东 * 2  增发带交易报告（0 发行融资）
     * @throws Exception
     */
    @Test
    public void shareIncreaseWithType0() throws Exception {

        log.info("增发前查询机构主体信息");
        String query2 = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);
//        BigDecimal totalShares = new BigDecimal(JSONObject.fromObject(query2).getJSONObject("data").getString("subject_total_share_capital"));
        int totalHolderAccount = JSONObject.fromObject(query2).getJSONObject("data").getInt("subject_shareholders_number");

        String eqCode = gdEquityCode;
        String reason = "股份分红";

        regNo = "Eq" + "increase" + (new Date()).getTime();   //区分不同类型的交易登记以流水号
        registerInfo.put("register_registration_serial_number",regNo);       //更新对比的登记流水号

        List<Map> shareList = gdConstructShareList2(gdAccount5,increaseAmount,0);
        List<Map> shareList4 = gdConstructShareList2(gdAccount6,increaseAmount,0, shareList);
        txInformation.put("transaction_type",0);
        txInformation.put("transaction_original_owner_subject_ref",mapAccAddr.get(gdAccount5));
        String response= gd.GDShareIncrease(gdPlatfromKeyID,eqCode,shareList4,reason, equityProductInfo,txInformation);
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
        jgType = txrpType;
        String subTxReportStoreId = gdCF.getJGStoreHash2(txId,jgType,1);

        //遍历检查所有账户登记及交易存证信息
        for(int k = 0 ;k < shareList4.size(); k++) {
            String tempAddr = JSONObject.fromObject(shareList4.get(k)).getString("address");
            String tempObjId = mapAccAddr.get(tempAddr).toString();

            registerInfo = gdBF.init05RegInfo();

            log.info("检查增发存证登记格式化及信息内容与传入一致");
            registerInfo.put("register_account_obj_id",tempObjId);
            registerInfo.put("register_rights_frozen_balance","null");
            registerInfo.put("register_creditor_subscription_count","null");
            registerInfo.put("register_rights_frozen_change_amount","null");
            registerInfo.put("register_available_balance","null");
            registerInfo.put("register_rights_change_amount",increaseAmount);
            log.info(gdCF.contructRegisterInfo(regStoreId,2,tempObjId).toString().replaceAll("\"",""));
            log.info(registerInfo.toString());
            assertEquals(registerInfo.toString(), gdCF.contructRegisterInfo(regStoreId,2,tempObjId).toString().replaceAll("\"",""));

            log.info("检查增发存证产品格式化及信息内容与传入一致");
            log.info(gdCF.contructEquityProdInfo(prodStoreId).toString().replaceAll("\"",""));
            log.info(equityProductInfo.toString());
            assertEquals(equityProductInfo.toString().replaceAll("(\")?( )?",""),
                    gdCF.contructEquityProdInfo(prodStoreId).toString().replaceAll("(\")?( )?","").replaceAll(":","="));

        }

        log.info("检查发行融资存证交易格式化及信息内容与传入一致:" + mapAccAddr.get(gdAccount5));
        log.info(gdCF.contructTxInfo(subTxReportStoreId,
                4,mapAccAddr.get(gdAccount5).toString()).toString().replaceAll("\"", ""));
        log.info(txInformation.toString());
        assertEquals(txInformation.toString(), gdCF.contructTxInfo(subTxReportStoreId,
                4,mapAccAddr.get(gdAccount5).toString()).toString().replaceAll("\"", ""));


        log.info("检查增发存证主体格式化及信息内容与传入一致");
        String getTotalMem = enterpriseSubjectInfo.get("subject_shareholders_number").toString();
        int oldTotalMem = Integer.parseInt(getTotalMem);
        enterpriseSubjectInfo.put("subject_shareholders_number",oldTotalMem + 2);     //变更总股本数为增发量 + 原始股本总数

//        String getTotal = enterpriseSubjectInfo.get("subject_total_share_capital").toString();
//        BigDecimal oldTotal = new BigDecimal(getTotal);
//        enterpriseSubjectInfo.put("subject_total_share_capital",oldTotal.add(new BigDecimal(increaseAmount * 2)));     //变更总股本数为增发量 + 原始股本总数
        log.info(gdCF.contructEnterpriseSubInfo(subStoreId).toString().replaceAll("\"",""));
//        enterpriseSubjectInfo.put("subject_object_information_type",1);
        log.info(enterpriseSubjectInfo.toString());
        assertEquals(enterpriseSubjectInfo.toString(), gdCF.contructEnterpriseSubInfo(subStoreId).toString().replaceAll("\"",""));


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


        log.info("增发后查询机构主体信息");
        String query3 = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);
//        BigDecimal totalShares2 = new BigDecimal(JSONObject.fromObject(query3).getJSONObject("data").getString("subject_total_share_capital"));

        log.info("判断增发前后机构主体查询总股本数增加数正确");
//        assertEquals(totalShares.add(new BigDecimal("2000")),totalShares2);

        int totalHolderAccountAft = JSONObject.fromObject(query3).getJSONObject("data").getInt("subject_shareholders_number");

        assertEquals(totalHolderAccount + 2,totalHolderAccountAft);

    }


    /***
     * 增发给新的股东 * 2  增发带交易报告（交易类型非0）
     * @throws Exception
     */
    @Test
    public void shareIncreaseWithTypeNon0() throws Exception {

        log.info("增发前查询机构主体信息");
        String query2 = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);
//        BigDecimal totalShares = new BigDecimal(JSONObject.fromObject(query2).getJSONObject("data").getString("subject_total_share_capital"));
        int totalHolderAccount = JSONObject.fromObject(query2).getJSONObject("data").getInt("subject_shareholders_number");

        String eqCode = gdEquityCode;
        String reason = "股份分红";

        regNo = "Eq" + "increase" + (new Date()).getTime();   //区分不同类型的交易登记以流水号
        registerInfo.put("register_registration_serial_number",regNo);       //更新对比的登记流水号

        List<Map> shareList = gdConstructShareList2(gdAccount5,increaseAmount,0);
        List<Map> shareList4 = gdConstructShareList2(gdAccount6,increaseAmount,0, shareList);
        txInformation.put("transaction_type",1);
        String response= gd.GDShareIncrease(gdPlatfromKeyID,eqCode,shareList4,reason, equityProductInfo,txInformation);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        sleepAndSaveInfo(SLEEPTIME);
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
        jgType = txrpType;
        String subTxReportStoreId = gdCF.getJGStoreHash2(txId,jgType,1);
        assertEquals("判断无交易报告数据报送","",subTxReportStoreId);

        //遍历检查所有账户登记及交易存证信息
        for(int k = 0 ;k < shareList4.size(); k++) {
            String tempAddr = JSONObject.fromObject(shareList4.get(k)).getString("address");
            String tempObjId = mapAccAddr.get(tempAddr).toString();

            registerInfo = gdBF.init05RegInfo();

            log.info("检查增发存证登记格式化及信息内容与传入一致");
            registerInfo.put("register_account_obj_id",tempObjId);
            registerInfo.put("register_rights_frozen_balance","null");
            registerInfo.put("register_creditor_subscription_count","null");
            registerInfo.put("register_rights_frozen_change_amount","null");
            registerInfo.put("register_available_balance","null");
            registerInfo.put("register_rights_change_amount",increaseAmount);
            log.info(gdCF.contructRegisterInfo(regStoreId,2,tempObjId).toString().replaceAll("\"",""));
            log.info(registerInfo.toString());
            assertEquals(registerInfo.toString(), gdCF.contructRegisterInfo(regStoreId,2,tempObjId).toString().replaceAll("\"",""));

            log.info("检查增发存证产品格式化及信息内容与传入一致");
            log.info(gdCF.contructEquityProdInfo(prodStoreId).toString().replaceAll("\"",""));
            log.info(equityProductInfo.toString());
            assertEquals(equityProductInfo.toString().replaceAll("(\")?( )?",""),
                    gdCF.contructEquityProdInfo(prodStoreId).toString().replaceAll("(\")?( )?","").replaceAll(":","="));

        }

        log.info("检查增发存证主体格式化及信息内容与传入一致");
        String getTotalMem = enterpriseSubjectInfo.get("subject_shareholders_number").toString();
        int oldTotalMem = Integer.parseInt(getTotalMem);
        enterpriseSubjectInfo.put("subject_shareholders_number",oldTotalMem + 2);     //变更总股本数为增发量 + 原始股本总数

//        String getTotal = enterpriseSubjectInfo.get("subject_total_share_capital").toString();
//        BigDecimal oldTotal = new BigDecimal(getTotal);
//        enterpriseSubjectInfo.put("subject_total_share_capital",oldTotal.add(new BigDecimal(increaseAmount * 2)));     //变更总股本数为增发量 + 原始股本总数
        log.info(gdCF.contructEnterpriseSubInfo(subStoreId).toString().replaceAll("\"",""));
//        enterpriseSubjectInfo.put("subject_object_information_type",1);
        log.info(enterpriseSubjectInfo.toString());
        assertEquals(enterpriseSubjectInfo.toString(), gdCF.contructEnterpriseSubInfo(subStoreId).toString().replaceAll("\"",""));


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


        log.info("增发后查询机构主体信息");
        String query3 = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);
//        BigDecimal totalShares2 = new BigDecimal(JSONObject.fromObject(query3).getJSONObject("data").getString("subject_total_share_capital"));

        log.info("判断增发前后机构主体查询总股本数增加数正确");
//        assertEquals(totalShares.add(new BigDecimal("2000")),totalShares2);

        int totalHolderAccountAft = JSONObject.fromObject(query3).getJSONObject("data").getInt("subject_shareholders_number");

        assertEquals(totalHolderAccount + 2,totalHolderAccountAft);

    }

    /***
     * 质押融资方式冻结 需带交易报告 交易类型为质押融资 即5
     * @throws Exception
     */
    @Test
    public void TC10_shareLock_Type5() throws Exception {

        sleepAndSaveInfo(5000);
        String bizNo = "lockTxReport2" + Random(12);
        String eqCode = gdEquityCode;
        String address = lockAddress;
        int shareProperty = 0;
        String reason = "司法冻结";
        String cutoffDate = "2022-09-30";
        String tempObjId = mapAccAddr.get(address).toString();

        registerInfo.put("register_account_obj_id",mapAccAddr.get(address));
        regNo = "Eq" + "lock" + bizNo + (new Date()).getTime();   //区分不同类型的交易登记以流水号
        registerInfo.put("register_registration_serial_number",regNo);       //更新对比的登记流水号
        registerInfo.remove("register_rights_frozen_change_amount");

        txInformation.put("transaction_type",5);//将交易类型设置为质押融资类型
        txInformation.put("transaction_original_owner_subject_ref",tempObjId);

        String response= gd.GDShareLock(bizNo,address,eqCode,lockAmount,shareProperty,reason,cutoffDate,registerInfo,txInformation);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        log.info("================================检查存证数据格式化《开始》================================");

        //获取监管数据存证hash
        String storeId = gdCF.getJGStoreHash(txId,1);

        log.info("检查冻结存证登记格式化及信息内容与传入一致");
        registerInfo.put("register_rights_frozen_change_amount",lockAmount);   //冻结变动额修改为当前实际冻结变更额
        log.info(gdCF.contructOneRegisterInfo(storeId).toString().replaceAll("\"",""));
        log.info(registerInfo.toString());
        assertEquals(registerInfo.toString(), gdCF.contructOneRegisterInfo(storeId).toString().replaceAll("\"",""));

        log.info("检查质押融资存证交易格式化及信息内容与传入一致:" + tempObjId);
        log.info(gdCF.contructTxInfo(storeId, 2,tempObjId).toString().replaceAll("\"", ""));
        log.info(txInformation.toString());
        assertEquals(txInformation.toString(), gdCF.contructTxInfo(storeId, 2,tempObjId).toString().replaceAll("\"", ""));

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
     * 质押融资方式冻结 需带交易报告 交易类型非质押融资 非5 不报送交易报告
     * @throws Exception
     */
    @Test
    public void TC11_shareLock_Type12346() throws Exception {

        sleepAndSaveInfo(5000);
        String bizNo = "lockTxReport" + Random(12);
        String eqCode = gdEquityCode;
        String address = lockAddress;
        int shareProperty = 0;
        String reason = "司法冻结";
        String cutoffDate = "2022-09-30";
        String tempObjId = mapAccAddr.get(address).toString();

        registerInfo.put("register_account_obj_id",mapAccAddr.get(address));
        regNo = "Eq" + "lock" + bizNo + (new Date()).getTime();   //区分不同类型的交易登记以流水号
        registerInfo.put("register_registration_serial_number",regNo);       //更新对比的登记流水号
        registerInfo.remove("register_rights_frozen_change_amount");


        txInformation.put("transaction_type",1);//将交易类型设置为质押融资类型
        txInformation.put("transaction_original_owner_subject_ref",tempObjId);
        String response= gd.GDShareLock(bizNo,address,eqCode,lockAmount,shareProperty,reason,cutoffDate,registerInfo,txInformation);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));
        log.info("================================检查存证数据格式化《开始》================================");
        //获取监管数据存证hash
        String storeId = gdCF.getJGStoreHash(txId,1);
        log.info("检查冻结存证登记格式化及信息内容与传入一致");
        registerInfo.put("register_rights_frozen_change_amount",lockAmount);   //冻结变动额修改为当前实际冻结变更额
        log.info(gdCF.contructOneRegisterInfo(storeId).toString().replaceAll("\"",""));
        log.info(registerInfo.toString());
        assertEquals(registerInfo.toString(), gdCF.contructOneRegisterInfo(storeId).toString().replaceAll("\"",""));
        assertEquals("检查冻结1不包送交易报告数据",false,store.GetTxDetail(storeId).contains("\"type\":\"交易报告\""));
        log.info("================================检查存证数据格式化《结束》================================");


        bizNo = "lockTxReport" + Random(12);
        txInformation.put("transaction_type",2);//将交易类型设置为非质押融资类型
        response= gd.GDShareLock(bizNo,address,eqCode,lockAmount,shareProperty,reason,cutoffDate,registerInfo,txInformation);
        jsonObject=JSONObject.fromObject(response);
        txId = jsonObject.getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));
        log.info("================================检查存证数据格式化《开始》================================");
        //获取监管数据存证hash
        storeId = gdCF.getJGStoreHash(txId,1);
        assertEquals("检查冻结2不包送交易报告数据",false,store.GetTxDetail(storeId).contains("\"type\":\"交易报告\""));
        log.info("================================检查存证数据格式化《结束》================================");


        bizNo = "lockTxReport" + Random(12);
        txInformation.put("transaction_type",3);//将交易类型设置为非质押融资类型
        response= gd.GDShareLock(bizNo,address,eqCode,lockAmount,shareProperty,reason,cutoffDate,registerInfo,txInformation);
        jsonObject=JSONObject.fromObject(response);
        txId = jsonObject.getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));
        log.info("================================检查存证数据格式化《开始》================================");
        //获取监管数据存证hash
        storeId = gdCF.getJGStoreHash(txId,1);
        assertEquals("检查冻结3不包送交易报告数据",false,store.GetTxDetail(storeId).contains("\"type\":\"交易报告\""));
        log.info("================================检查存证数据格式化《结束》================================");


        bizNo = "lockTxReport" + Random(12);
        txInformation.put("transaction_type",4);//将交易类型设置为非质押融资类型
        response= gd.GDShareLock(bizNo,address,eqCode,lockAmount,shareProperty,reason,cutoffDate,registerInfo,txInformation);
        jsonObject=JSONObject.fromObject(response);
        txId = jsonObject.getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));
        log.info("================================检查存证数据格式化《开始》================================");
        //获取监管数据存证hash
        storeId = gdCF.getJGStoreHash(txId,1);
        assertEquals("检查冻结4不包送交易报告数据",false,store.GetTxDetail(storeId).contains("\"type\":\"交易报告\""));
        log.info("================================检查存证数据格式化《结束》================================");


        bizNo = "lockTxReport" + Random(12);
        txInformation.put("transaction_type",6);//将交易类型设置为非质押融资类型
        response= gd.GDShareLock(bizNo,address,eqCode,lockAmount,shareProperty,reason,cutoffDate,registerInfo,txInformation);
        jsonObject=JSONObject.fromObject(response);
        txId = jsonObject.getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));
        log.info("================================检查存证数据格式化《开始》================================");
        //获取监管数据存证hash
        storeId = gdCF.getJGStoreHash(txId,1);
        assertEquals("检查冻结6不包送交易报告数据",false,store.GetTxDetail(storeId).contains("\"type\":\"交易报告\""));
        log.info("================================检查存证数据格式化《结束》================================");
    }

    /***
     * 解除冻结还款解质押方式 需带交易报告 会报送交易报告
     * @throws Exception
     */
    @Test
    public void TC11_shareUnlock_Type6() throws Exception {
        bizNoTest = "unlockReport" + Random(13);
        String bizNo = bizNoTest;
        String eqCode = gdEquityCode;
        long amount = 500;
        int shareProperty = 0;
        String cutoffDate = "2022-09-30";
        String tempObjId = mapAccAddr.get(lockAddress).toString();

        registerInfo = gdBF.init05RegInfo();

        regNo = "Eq" + "unlock" + bizNo + (new Date()).getTime();   //区分不同类型的交易登记以流水号
        registerInfo.put("register_registration_serial_number",regNo);       //更新对比的登记流水号

        txInformation.put("transaction_type",6);//将交易类型设置为还款解质押
        txInformation.put("transaction_original_owner_subject_ref",tempObjId);

        String response2 = gd.GDShareLock(bizNo,lockAddress,eqCode,lockAmount,shareProperty,"冻结",cutoffDate,registerInfo,txInformation);
        sleepAndSaveInfo(6000);


        String response= gd.GDShareUnlock(bizNo,eqCode,amount,registerInfo,txInformation);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));


        log.info("================================检查存证数据格式化《开始》================================");
        //获取监管数据存证hash
        String storeId = gdCF.getJGStoreHash(txId,1);

        log.info("检查解除冻结存证登记格式化及信息内容与传入一致");
        log.info(gdCF.contructOneRegisterInfo(storeId).toString().replaceAll("\"",""));
        log.info(registerInfo.toString());
        assertEquals(registerInfo.toString(), gdCF.contructOneRegisterInfo(storeId).toString().replaceAll("\"",""));

        log.info("检查质押融资存证交易格式化及信息内容与传入一致:" + tempObjId);
        log.info(gdCF.contructTxInfo(storeId, 2,tempObjId).toString().replaceAll("\"", ""));
        log.info(txInformation.toString());
        assertEquals(txInformation.toString(), gdCF.contructTxInfo(storeId, 2,tempObjId).toString().replaceAll("\"", ""));

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
    public void TC11_shareUnlock_Type123457() throws Exception {
        bizNoTest = "unlockReport" + Random(13);
        log.info(bizNoTest);
        int shareProperty = 0;
        String cutoffDate = "2022-09-30";
        String bizNo = bizNoTest;
        String eqCode = gdEquityCode;
        long amount = 50;
        String tempObjId = mapAccAddr.get(lockAddress).toString();

        registerInfo = gdBF.init05RegInfo();

        regNo = "Eq" + "unlock" + bizNo + (new Date()).getTime();   //区分不同类型的交易登记以流水号
        registerInfo.put("register_registration_serial_number",regNo);       //更新对比的登记流水号

        txInformation.put("transaction_type",1);//将交易类型设置为还款解质押
        txInformation.put("transaction_original_owner_subject_ref",tempObjId);

        String response2 = gd.GDShareLock(bizNo,lockAddress,eqCode,lockAmount,shareProperty,"冻结",cutoffDate,registerInfo,txInformation);
        sleepAndSaveInfo(6000);


        String response= gd.GDShareUnlock(bizNo,eqCode,amount,registerInfo,txInformation);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));
        log.info("================================检查存证数据格式化《开始》================================");
        //获取监管数据存证hash
        String storeId = gdCF.getJGStoreHash(txId,1);
        log.info("检查解除冻结存证登记格式化及信息内容与传入一致");
        log.info(gdCF.contructOneRegisterInfo(storeId).toString().replaceAll("\"",""));
        log.info(registerInfo.toString());
        assertEquals(registerInfo.toString(), gdCF.contructOneRegisterInfo(storeId).toString().replaceAll("\"",""));
        assertEquals("检查解除冻结1不包送交易报告数据",false,store.GetTxDetail(storeId).contains("\"type\":\"交易报告\""));
        log.info("================================检查存证数据格式化《结束》================================");


        txInformation.put("transaction_type",2);//将交易类型设置为2
        response= gd.GDShareUnlock(bizNo,eqCode,amount,registerInfo,txInformation);
        jsonObject=JSONObject.fromObject(response);
        txId = jsonObject.getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));
        log.info("================================检查存证数据格式化《开始》================================");
        //获取监管数据存证hash
        storeId = gdCF.getJGStoreHash(txId,1);
        log.info("检查解除冻结存证登记格式化及信息内容与传入一致");
        log.info(gdCF.contructOneRegisterInfo(storeId).toString().replaceAll("\"",""));
        log.info(registerInfo.toString());
        assertEquals(registerInfo.toString(), gdCF.contructOneRegisterInfo(storeId).toString().replaceAll("\"",""));
        assertEquals("检查解除冻结2不包送交易报告数据",false,store.GetTxDetail(storeId).contains("\"type\":\"交易报告\""));
        log.info("================================检查存证数据格式化《结束》================================");


        txInformation.put("transaction_type",3);//将交易类型设置为3
        response= gd.GDShareUnlock(bizNo,eqCode,amount,registerInfo,txInformation);
        jsonObject=JSONObject.fromObject(response);
        txId = jsonObject.getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));
        log.info("================================检查存证数据格式化《开始》================================");
        //获取监管数据存证hash
        storeId = gdCF.getJGStoreHash(txId,1);
        log.info("检查解除冻结存证登记格式化及信息内容与传入一致");
        log.info(gdCF.contructOneRegisterInfo(storeId).toString().replaceAll("\"",""));
        log.info(registerInfo.toString());
        assertEquals(registerInfo.toString(), gdCF.contructOneRegisterInfo(storeId).toString().replaceAll("\"",""));
        assertEquals("检查解除冻结3不包送交易报告数据",false,store.GetTxDetail(storeId).contains("\"type\":\"交易报告\""));
        log.info("================================检查存证数据格式化《结束》================================");


        txInformation.put("transaction_type",4);//将交易类型设置为4
        response= gd.GDShareUnlock(bizNo,eqCode,amount,registerInfo,txInformation);
        jsonObject=JSONObject.fromObject(response);
        txId = jsonObject.getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));
        log.info("================================检查存证数据格式化《开始》================================");
        //获取监管数据存证hash
        storeId = gdCF.getJGStoreHash(txId,1);
        log.info("检查解除冻结存证登记格式化及信息内容与传入一致");
        log.info(gdCF.contructOneRegisterInfo(storeId).toString().replaceAll("\"",""));
        log.info(registerInfo.toString());
        assertEquals(registerInfo.toString(), gdCF.contructOneRegisterInfo(storeId).toString().replaceAll("\"",""));
        assertEquals("检查解除冻结4不包送交易报告数据",false,store.GetTxDetail(storeId).contains("\"type\":\"交易报告\""));
        log.info("================================检查存证数据格式化《结束》================================");


        txInformation.put("transaction_type",5);//将交易类型设置为5
        response= gd.GDShareUnlock(bizNo,eqCode,amount,registerInfo,txInformation);
        jsonObject=JSONObject.fromObject(response);
        txId = jsonObject.getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));
        log.info("================================检查存证数据格式化《开始》================================");
        //获取监管数据存证hash
        storeId = gdCF.getJGStoreHash(txId,1);
        log.info("检查解除冻结存证登记格式化及信息内容与传入一致");
        log.info(gdCF.contructOneRegisterInfo(storeId).toString().replaceAll("\"",""));
        log.info(registerInfo.toString());
        assertEquals(registerInfo.toString(), gdCF.contructOneRegisterInfo(storeId).toString().replaceAll("\"",""));
        assertEquals("检查解除冻结5不包送交易报告数据",false,store.GetTxDetail(storeId).contains("\"type\":\"交易报告\""));
        log.info("================================检查存证数据格式化《结束》================================");


        txInformation.put("transaction_type",7);//将交易类型设置为不支持的类型
        response= gd.GDShareUnlock(bizNo,eqCode,amount,registerInfo,txInformation);
        jsonObject=JSONObject.fromObject(response);
        txId = jsonObject.getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));
        log.info("================================检查存证数据格式化《开始》================================");
        //获取监管数据存证hash
        storeId = gdCF.getJGStoreHash(txId,1);
        log.info("检查解除冻结存证登记格式化及信息内容与传入一致");
        log.info(gdCF.contructOneRegisterInfo(storeId).toString().replaceAll("\"",""));
        log.info(registerInfo.toString());
        assertEquals(registerInfo.toString(), gdCF.contructOneRegisterInfo(storeId).toString().replaceAll("\"",""));
        assertEquals("检查解除冻结7不包送交易报告数据",false,store.GetTxDetail(storeId).contains("\"type\":\"交易报告\""));
        log.info("================================检查存证数据格式化《结束》================================");

    }

    /***
     * 账户存在股份性质0*1000 1*1000
     * 冻结0*500 1*500
     * @throws Exception
     */
    public void lockMutli()throws Exception{

    }
}
