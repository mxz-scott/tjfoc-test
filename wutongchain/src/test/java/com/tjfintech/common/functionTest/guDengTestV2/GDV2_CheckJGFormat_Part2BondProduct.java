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
public class GDV2_CheckJGFormat_Part2BondProduct {

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
//    long increaseAmount = 1000;
    long lockAmount = issueAmount;
    long unlockAmount = 1000;
    long recycleAmount = issueAmount;
    long changeAmount = 5000;
    long transferAmount = 1000;

    @Rule
    public TestName tm = new TestName();
    /***
     * 测试说明
     * 股权性质变更 变更全部
     * 转让 不会转给新的账户 转让不会增加总股东数
     * 场内转板
     * 增发 增发给新账户  会增加总股东数
     * 冻结 冻结全部
     * 解除冻结 解除部分
     * 回收1 先一个账户为回收全部  会减少总股东数-1
     * 解除冻结 解除剩余部分
     * 回收2 回收全部 总股东数-all
     */

    @BeforeClass
    public static void Before()throws Exception{
        GDBeforeCondition gdBefore = new GDBeforeCondition();
        gdBefore.gdCreateAccout();
//        gdBefore.initRegulationData();
        equityProductInfo = null;
        bondProductInfo = gdBefore.init03BondProductInfo();
        fundProductInfo = null;
        gdEquityCode = "fondTest" + Random(12);
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
    public void TC06_shareIssue() throws Exception {

        List<Map> shareList = gdConstructShareList(gdAccount1,issueAmount,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,issueAmount,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,issueAmount,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,issueAmount,0,shareList3);

        log.info(shareList4.toString());

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

            log.info("检查发行存证登记格式化及信息内容与传入一致:" + tempObjId);
            registerInfo.put("register_account_obj_id",tempObjId);

//            registerInfo.put("register_rights_change_amount", issueAmount);     //变动额修改为单个账户发行数量
//            registerInfo.put("register_rights_frozen_balance", 0);   //当前冻结余额修改为实际冻结数
//            registerInfo.put("register_available_balance", issueAmount);   //当前当前可用余额修改为当前实际可用余额
//            registerInfo.put("register_creditor_subscription_count", issueAmount);   //当前认购数量修改为当前实际余额
//            registerInfo.put("register_rights_frozen_change_amount", 0);   //冻结变动额修改为当前实际冻结变更额
            log.info(gdCF.contructRegisterInfo(storeId, 4,tempObjId).toString().replaceAll("\"", ""));
            log.info(registerInfo.toString());
            assertEquals(registerInfo.toString(), gdCF.contructRegisterInfo(storeId, 4,tempObjId).toString().replaceAll("\"", ""));

            assertEquals("检查发行不包送交易报告数据",false,store.GetTxDetail(storeId).contains("\"type\":\"交易报告\""));
//            log.info("检查发行存证交易格式化及信息内容与传入一致:" + tempObjId);
//            txInformation.put("transaction_original_owner_subject_ref",tempObjId);
//            txInformation.put("transaction_traded_product_ref",gdEquityCode + "01");
//            log.info(gdCF.contructTxInfo(storeId, 8,tempObjId).toString().replaceAll("\"", ""));
//            log.info(txInformation.toString());
//            assertEquals(txInformation.toString(), gdCF.contructTxInfo(storeId, 8,tempObjId).toString().replaceAll("\"", ""));
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

//        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo5);
//        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));
//
//        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo6);
//        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));
    }

    /***
     * 股权性质变更 部分变更
     * 变更后 未变更部分保持原有股权性质不变 P17对应的需求点
     * @throws Exception
     */
    @Test
    public void TC071_shareChangeProperty() throws Exception {

        String eqCode = gdEquityCode;
        String address = gdAccount1;

        int oldProperty = 0;
        int newProperty = 1;
        Map tempReg1 = new HashMap();
        Map tempReg2 = new HashMap();

        tempReg1 = gdBF.init05RegInfo();
        tempReg2 = gdBF.init05RegInfo();

        tempReg1.put("register_account_obj_id", mapAccAddr.get(address));
        regNo = "Eq" + "ChangeProperty" + (new Date()).getTime();   //区分不同类型的交易登记以流水号
        tempReg1.put("register_registration_serial_number", regNo);       //更新对比的登记流水号
        tempReg1.put("register_nature_of_shares", oldProperty);

        List<Map> regListInfo = new ArrayList<>();

        regListInfo.add(tempReg1);

        tempReg2.put("register_account_obj_id", mapAccAddr.get(address));
        tempReg2.put("register_registration_serial_number", regNo);       //更新对比的登记流水号
        tempReg2.put("register_nature_of_shares", newProperty);
        regListInfo.add(tempReg2);

        log.info(regListInfo.toString());

        String response = gd.GDShareChangeProperty(gdPlatfromKeyID, address, eqCode, changeAmount, oldProperty, newProperty, regListInfo);
        JSONObject jsonObject = JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200", JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");

        log.info("================================检查存证数据格式化《开始》================================");
        //获取监管数据存证hash
        String storeId = gdCF.getJGStoreHash(txId, 1);
        String tempAddr = address;
        String tempObjId = mapAccAddr.get(tempAddr).toString();

        log.info("检查股权性质变更存证登记格式化及信息内容与传入一致");

        log.info(gdCF.contructRegisterInfo(storeId, 2, tempObjId, String.valueOf(oldProperty)).toString().replaceAll("\"", ""));
        log.info(tempReg1.toString());
        assertEquals(tempReg1.toString(), gdCF.contructRegisterInfo(storeId, 2, tempObjId, String.valueOf(oldProperty)).toString().replaceAll("\"", ""));

        log.info(gdCF.contructRegisterInfo(storeId, 2, tempObjId, String.valueOf(newProperty)).toString().replaceAll("\"", ""));
        log.info(tempReg2.toString());
        assertEquals(tempReg2.toString(), gdCF.contructRegisterInfo(storeId, 2, tempObjId, String.valueOf(newProperty)).toString().replaceAll("\"", ""));

        log.info("================================检查存证数据格式化《结束》================================");

        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,changeAmount,1,0,mapShareENCN().get("1"),respShareList);
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
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":5000,\"lockAmount\":0}"));

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

//    @Test
    public void TC072_shareChangeBoard() throws Exception {

        String oldEquityCode = gdEquityCode;
        String newEquityCode = gdEquityCode + Random(5);
        String cpnyId = gdCompanyID;

        regNo = "changeboard" + (new Date()).getTime();   //区分不同类型的交易登记以流水号
        List<Map> regList = uf.getAllHolderListReg(gdEquityCode,regNo);

        String response= gd.GDShareChangeBoard(gdPlatfromKeyID,cpnyId,oldEquityCode,newEquityCode,regList,equityProductInfo,bondProductInfo);
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


        log.info("================================检查存证数据格式化《开始》================================");
        //获取监管数据存证hash
        String storeId = gdCF.getJGStoreHash(txId, 1);

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

//            registerInfo.put("register_rights_change_amount", tempAmount);
//            registerInfo.put("register_available_balance", tempAmount);
//            registerInfo.put("register_time", sd);
//            log.info(gdCF.contructRegisterInfo(storeId, 7,tempObjId).toString().replaceAll("\"", ""));
//            log.info(registerInfo.toString());
//            assertEquals(registerInfo.toString(), gdCF.contructRegisterInfo(storeId, 7,tempObjId).toString().replaceAll("\"", ""));

        }
        log.info("检查场内转板存证产品格式化及信息内容与传入一致");

        log.info(gdCF.contructBondProdInfo(storeId).toString().replaceAll("\"",""));
        log.info(bondProductInfo.toString());
        bondProductInfo.put("product_code",gdEquityCode);
        assertEquals(bondProductInfo.toString(), gdCF.contructBondProdInfo(storeId).toString().replaceAll("\"",""));

        log.info("================================检查存证数据格式化《结束》================================");


        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,5000,1,0,mapShareENCN().get("1"),respShareList);
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
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":5000,\"lockAmount\":0}"));

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
    public void TC08_shareTransfer()throws Exception {
//        sleepAndSaveInfo(5000);

        String keyId = gdAccountKeyID1;
        String fromAddr = gdAccount1;
        String toAddr = gdAccount3;
        int shareProperty = 1;
        String eqCode = gdEquityCode;

        String tempObjIdFrom = mapAccAddr.get(gdAccount1).toString();
        String tempObjIdTo = mapAccAddr.get(gdAccount3).toString();

        Map fromNow = gdBF.init05RegInfo();
        Map toNow = gdBF.init05RegInfo();

        regNo = "Eq" + "transfer" + (new Date()).getTime();   //区分不同类型的交易登记以流水号
        fromNow.put("register_registration_serial_number", regNo);       //更新对比的登记流水号
        toNow.put("register_registration_serial_number", regNo);       //更新对比的登记流水号
        fromNow.put("register_account_obj_id", tempObjIdFrom);       //更新对比的权利人账户引用
        toNow.put("register_account_obj_id", tempObjIdTo);       //更新对比的权利人账户引用

        txInformation.put("transaction_original_owner_subject_ref", tempObjIdFrom);
        String response = gd.GDShareTransfer(keyId, fromAddr, transferAmount, toAddr, shareProperty, eqCode, txInformation, fromNow, toNow);

        JSONObject jsonObject = JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));


        //获取上链交易时间戳
        long onChainTS = JSONObject.fromObject(store.GetTxDetail(txId)).getJSONObject("data").getJSONObject("header").getLong("timestamp");

        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200", JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");

        log.info("================================检查存证数据格式化《开始》================================");
        //获取监管数据存证hash
        String storeId = gdCF.getJGStoreHash(txId, 1);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        String sd = sdf.format(new Date(onChainTS)); // 时间戳转换日期
        log.info("检查过户转让存证登记格式化及信息内容与传入一致:" + tempObjIdFrom);
//        fromNow.put("register_rights_change_amount","-" + transferAmount);
//        fromNow.put("register_time", txInformation.get("transaction_close_time").toString());
//        fromNow.put("register_available_balance", issueAmount - transferAmount);
//        fromNow.put("register_rights_frozen_balance", 0);   //当前冻结余额修改为实际冻结数
//
//        toNow.put("register_rights_change_amount", transferAmount);
//        toNow.put("register_rights_frozen_balance", 0);   //当前冻结余额修改为实际冻结数
//        toNow.put("register_time", txInformation.get("transaction_close_time").toString());
//        toNow.put("register_available_balance", transferAmount);
        log.info(gdCF.contructRegisterInfo(storeId, 3, tempObjIdFrom).toString().replaceAll("\"", ""));
        log.info(fromNow.toString());
        assertEquals(fromNow.toString(), gdCF.contructRegisterInfo(storeId, 3, tempObjIdFrom).toString().replaceAll("\"", ""));

        log.info("检查过户转让存证交易格式化及信息内容与传入一致:" + tempObjIdFrom);

        log.info(gdCF.contructTxInfo(storeId, 3, tempObjIdFrom).toString().replaceAll("\"", ""));
        log.info(txInformation.toString());
        assertEquals(txInformation.toString(), gdCF.contructTxInfo(storeId, 3, tempObjIdFrom).toString().replaceAll("\"", ""));

        log.info("检查过户转让存证登记格式化及信息内容与传入一致:" + tempObjIdTo);
        log.info(gdCF.contructRegisterInfo(storeId, 3, tempObjIdTo).toString().replaceAll("\"", ""));
        log.info(toNow.toString());
        assertEquals(toNow.toString(), gdCF.contructRegisterInfo(storeId, 3, tempObjIdTo).toString().replaceAll("\"", ""));

        log.info("================================检查存证数据格式化《结束》================================");


        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1, 4000, 1, 0, mapShareENCN().get("1"), respShareList);
        respShareList = gdConstructQueryShareList(gdAccount3, 1000, 1, 0, mapShareENCN().get("1"), respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2, 5000, 0, 0, mapShareENCN().get("0"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3, 5000, 0, 0, mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4, 5000, 0, 0, mapShareENCN().get("0"), respShareList3);

        log.info(respShareList4.toString());
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
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":4000,\"lockAmount\":0}"));

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
        assertEquals(true, query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress, gdAccClientNo4);
        assertEquals(gdAccClientNo4, JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true, query.contains("\"shareholderNo\":\"SH" + gdAccClientNo4 + "\""));
        assertEquals(true, query.contains("\"address\":\"" + gdAccount4 + "\""));
        assertEquals(true, query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":5000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress, gdAccClientNo5);
        assertEquals(false, query.contains("\"equityCode\": \"" + gdEquityCode + "\""));
        ;

        query = gd.GDGetShareHolderInfo(gdContractAddress, gdAccClientNo6);
        assertEquals(false, query.contains("\"equityCode\": \"" + gdEquityCode + "\""));
    }

    //债权类支不支持增发 增发仅股权类


    @Test
    public void TC10_shareLock() throws Exception {

        sleepAndSaveInfo(5000);

        String bizNo = bizNoTest;
        String eqCode = gdEquityCode;
        String address = gdAccount2;
        int shareProperty = 0;
        String reason = "司法冻结";
        String cutoffDate = "2022-09-30";

        registerInfo.put("register_account_obj_id",mapAccAddr.get(address));
        regNo = "lock" + bizNo + (new Date()).getTime();   //区分不同类型的交易登记以流水号
        registerInfo.put("register_registration_serial_number",regNo);       //更新对比的登记流水号

        String response= gd.GDShareLock(bizNo,address,eqCode,lockAmount,shareProperty,reason,cutoffDate,registerInfo);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        log.info("================================检查存证数据格式化《开始》================================");

        //获取监管数据存证hash
        String storeId = gdCF.getJGStoreHash(txId,1);

        log.info("检查冻结存证登记格式化及信息内容与传入一致");
//        registerInfo.put("register_rights_frozen_change_amount",lockAmount);   //冻结变动额修改为当前实际冻结变更额
        log.info(gdCF.contructOneRegisterInfo(storeId).toString().replaceAll("\"",""));
        log.info(registerInfo.toString());
        assertEquals(registerInfo.toString(), gdCF.contructOneRegisterInfo(storeId).toString().replaceAll("\"",""));

        log.info("================================检查存证数据格式化《结束》================================");


        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");


        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1, 4000, 1, 0, mapShareENCN().get("1"), respShareList);
        respShareList = gdConstructQueryShareList(gdAccount3, 1000, 1, 0, mapShareENCN().get("1"), respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2, 5000, 0, lockAmount, mapShareENCN().get("0"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3, 5000, 0, 0, mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4, 5000, 0, 0, mapShareENCN().get("0"), respShareList3);

        log.info(respShareList4.toString());
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
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":4000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress, gdAccClientNo2);
        assertEquals(gdAccClientNo2, JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true, query.contains("\"shareholderNo\":\"SH" + gdAccClientNo2 + "\""));
        assertEquals(true, query.contains("\"address\":\"" + gdAccount2 + "\""));
        assertEquals(true, query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":5000,\"lockAmount\":"+ lockAmount + "}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress, gdAccClientNo3);
        assertEquals(gdAccClientNo3, JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true, query.contains("\"shareholderNo\":\"SH" + gdAccClientNo3 + "\""));
        assertEquals(true, query.contains("\"address\":\"" + gdAccount3 + "\""));
        assertEquals(true, query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":5000,\"lockAmount\":0}"));
        assertEquals(true, query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress, gdAccClientNo4);
        assertEquals(gdAccClientNo4, JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true, query.contains("\"shareholderNo\":\"SH" + gdAccClientNo4 + "\""));
        assertEquals(true, query.contains("\"address\":\"" + gdAccount4 + "\""));
        assertEquals(true, query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":5000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress, gdAccClientNo5);
        assertEquals(false, query.contains("\"equityCode\": \"" + gdEquityCode + "\""));
        ;

        query = gd.GDGetShareHolderInfo(gdContractAddress, gdAccClientNo6);
        assertEquals(false, query.contains("\"equityCode\": \"" + gdEquityCode + "\""));
    }

    @Test
    public void TC11_shareUnlock() throws Exception {
        sleepAndSaveInfo(3000);

        String bizNo = bizNoTest;
        String eqCode = gdEquityCode;

        regNo = "unlock" + bizNo + (new Date()).getTime();   //区分不同类型的交易登记以流水号
        registerInfo.put("register_registration_serial_number",regNo);       //更新对比的登记流水号

        String response= gd.GDShareUnlock(bizNo,eqCode,unlockAmount,registerInfo);
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

        log.info("================================检查存证数据格式化《结束》================================");


        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");


        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1, 4000, 1, 0, mapShareENCN().get("1"), respShareList);
        respShareList = gdConstructQueryShareList(gdAccount3, 1000, 1, 0, mapShareENCN().get("1"), respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2, 5000, 0, lockAmount - unlockAmount, mapShareENCN().get("0"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3, 5000, 0, 0, mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4, 5000, 0, 0, mapShareENCN().get("0"), respShareList3);

        log.info(respShareList4.toString());
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
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":4000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress, gdAccClientNo2);
        assertEquals(gdAccClientNo2, JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true, query.contains("\"shareholderNo\":\"SH" + gdAccClientNo2 + "\""));
        assertEquals(true, query.contains("\"address\":\"" + gdAccount2 + "\""));
        assertEquals(true, query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0")
                + "\",\"totalAmount\":5000,\"lockAmount\":" + (lockAmount - unlockAmount) + "}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress, gdAccClientNo3);
        assertEquals(gdAccClientNo3, JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true, query.contains("\"shareholderNo\":\"SH" + gdAccClientNo3 + "\""));
        assertEquals(true, query.contains("\"address\":\"" + gdAccount3 + "\""));
        assertEquals(true, query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":5000,\"lockAmount\":0}"));
        assertEquals(true, query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress, gdAccClientNo4);
        assertEquals(gdAccClientNo4, JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true, query.contains("\"shareholderNo\":\"SH" + gdAccClientNo4 + "\""));
        assertEquals(true, query.contains("\"address\":\"" + gdAccount4 + "\""));
        assertEquals(true, query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":5000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress, gdAccClientNo5);
        assertEquals(false, query.contains("\"equityCode\": \"" + gdEquityCode + "\""));
        ;

        query = gd.GDGetShareHolderInfo(gdContractAddress, gdAccClientNo6);
        assertEquals(false, query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

    }

    @Test
    public void TC1201_shareRecycleOneAcc() throws Exception {
        String eqCode = gdEquityCode;
        String remark = "777777";

        log.info("回收前查询机构主体信息");
        String query2 = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);
//        BigDecimal totalShares = new BigDecimal(JSONObject.fromObject(query2).getJSONObject("data").getString("subject_total_share_capital"));
        int totalHolderAccount = JSONObject.fromObject(query2).getJSONObject("data").getInt("subject_shareholders_number");

//        log.info("before recycle 股本总数(股) " + totalShares + " 股东总数（个） " + totalHolderAccount);


        //账户4全部回收
        List<Map> shareList = gdConstructShareList(gdAccount4,recycleAmount,0);

        String response= gd.GDShareRecycle(gdPlatfromKeyID,eqCode,shareList,remark);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        sleepAndSaveInfo(SLEEPTIME,"等待所有交易涉及交易全部打包上链");


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

        String tempObjId = mapAccAddr.get(gdAccount4).toString();
        log.info("检查回收存证登记格式化及信息内容与传入一致");

        registerInfo = gdBF.init05RegInfo();
//        registerInfo.put("register_rights_change_amount",(-1) * recycleAmount);     //变动额修改为单个账户发行数量
        registerInfo.put("register_account_obj_id",tempObjId);
        log.info(gdCF.contructRegisterInfo(regStoreId,1,tempObjId).toString().replaceAll("\"",""));
        log.info(registerInfo.toString());
        assertEquals(registerInfo.toString(), gdCF.contructRegisterInfo(regStoreId,1,tempObjId).toString().replaceAll("\"",""));


        log.info("检查回收存证主体格式化及信息内容与传入一致");

//        String getTotal = enterpriseSubjectInfo.get("subject_total_share_capital").toString();
//        BigDecimal oldTotal = new BigDecimal(getTotal);
//        enterpriseSubjectInfo.put("subject_total_share_capital",oldTotal.subtract(new BigDecimal(recycleAmount)));     //变更总股本数为增发量 + 原始股本总数
        enterpriseSubjectInfo.put("subject_shareholders_number",totalHolderAccount - 1);     //股东个数为原个数减1
        log.info(gdCF.contructEnterpriseSubInfo(subStoreId).toString().replaceAll("\"",""));
        log.info(enterpriseSubjectInfo.toString());
//        enterpriseSubjectInfo.put("subject_object_information_type",1);
        assertEquals(enterpriseSubjectInfo.toString(), gdCF.contructEnterpriseSubInfo(subStoreId).toString().replaceAll("\"",""));

        log.info("================================检查存证数据格式化《结束》================================");


        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1, 4000, 1, 0, mapShareENCN().get("1"), respShareList);
        respShareList = gdConstructQueryShareList(gdAccount3, 1000, 1, 0, mapShareENCN().get("1"), respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2, 5000, 0, lockAmount - unlockAmount, mapShareENCN().get("0"), respShareList);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount3, 5000, 0, 0, mapShareENCN().get("0"), respShareList2);

        log.info(respShareList4.toString());
        //检查存在余额的股东列表
        assertEquals(respShareList4.size() + 1, dataShareList.size());

        List<Map> getShareList = getShareListFromQueryNoZeroAcc(dataShareList);

        assertEquals(respShareList4.size(), getShareList.size());
        assertEquals(true, respShareList4.containsAll(getShareList) && getShareList.containsAll(respShareList4));


        //查询股东持股情况 无当前股权代码信息
        query = gd.GDGetShareHolderInfo(gdContractAddress, gdAccClientNo1);
        assertEquals(gdAccClientNo1, JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));

        assertEquals(true, query.contains("\"shareholderNo\":\"SH" + gdAccClientNo1 + "\""));
        assertEquals(true, query.contains("\"address\":\"" + gdAccount1 + "\""));
        assertEquals(true, query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":4000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress, gdAccClientNo2);
        assertEquals(gdAccClientNo2, JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true, query.contains("\"shareholderNo\":\"SH" + gdAccClientNo2 + "\""));
        assertEquals(true, query.contains("\"address\":\"" + gdAccount2 + "\""));
        assertEquals(true, query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0")
                + "\",\"totalAmount\":5000,\"lockAmount\":" + (lockAmount - unlockAmount) + "}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress, gdAccClientNo3);
        assertEquals(gdAccClientNo3, JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true, query.contains("\"shareholderNo\":\"SH" + gdAccClientNo3 + "\""));
        assertEquals(true, query.contains("\"address\":\"" + gdAccount3 + "\""));
        assertEquals(true, query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":5000,\"lockAmount\":0}"));
        assertEquals(true, query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress, gdAccClientNo4);
        assertEquals(false, query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        query = gd.GDGetShareHolderInfo(gdContractAddress, gdAccClientNo5);
        assertEquals(false, query.contains("\"equityCode\": \"" + gdEquityCode + "\""));        ;

        query = gd.GDGetShareHolderInfo(gdContractAddress, gdAccClientNo6);
        assertEquals(false, query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        log.info("判断回收一个完全账户后机构主体查询总股本数增加数正确");
        query2 = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);
//        BigDecimal totalShares2 = new BigDecimal(JSONObject.fromObject(query2).getJSONObject("data").getString("subject_total_share_capital"));
        int totalHolderAccount2 = JSONObject.fromObject(query2).getJSONObject("data").getInt("subject_shareholders_number");
//        assertEquals(totalShares.subtract(new BigDecimal(recycleAmount)),totalShares2);
        assertEquals(totalHolderAccount2 + 1,totalHolderAccount);//判断股东总数减一
    }

//    @Test
    public void shareUnlockAll() throws Exception {
        sleepAndSaveInfo(3000);

        String bizNo = bizNoTest;
        String eqCode = gdEquityCode;

        regNo = "unlock" + bizNo + (new Date()).getTime();   //区分不同类型的交易登记以流水号
        registerInfo.put("register_registration_serial_number",regNo);       //更新对比的登记流水号

        String response= gd.GDShareUnlock(bizNo,eqCode,lockAmount - unlockAmount,registerInfo);
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

        log.info("================================检查存证数据格式化《结束》================================");


        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");


        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1, 4000, 1, 0, mapShareENCN().get("1"), respShareList);
        respShareList = gdConstructQueryShareList(gdAccount3, 1000, 1, 0, mapShareENCN().get("1"), respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2, 5000, 0, 0, mapShareENCN().get("0"), respShareList);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount3, 5000, 0, 0, mapShareENCN().get("0"), respShareList2);
//        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4, 5000, 0, 0, mapShareENCN().get("0"), respShareList3);

        log.info(respShareList4.toString());
        //检查存在余额的股东列表
        assertEquals(respShareList4.size() + 1, dataShareList.size());

        List<Map> getShareList = getShareListFromQueryNoZeroAcc(dataShareList);

        assertEquals(respShareList4.size(), getShareList.size());
        assertEquals(true, respShareList4.containsAll(getShareList) && getShareList.containsAll(respShareList4));


        //查询股东持股情况 无当前股权代码信息
        query = gd.GDGetShareHolderInfo(gdContractAddress, gdAccClientNo1);
        assertEquals(gdAccClientNo1, JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));

        assertEquals(true, query.contains("\"shareholderNo\":\"SH" + gdAccClientNo1 + "\""));
        assertEquals(true, query.contains("\"address\":\"" + gdAccount1 + "\""));
        assertEquals(true, query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":4000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress, gdAccClientNo2);
        assertEquals(gdAccClientNo2, JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true, query.contains("\"shareholderNo\":\"SH" + gdAccClientNo2 + "\""));
        assertEquals(true, query.contains("\"address\":\"" + gdAccount2 + "\""));
        assertEquals(true, query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0")
                + "\",\"totalAmount\":5000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress, gdAccClientNo3);
        assertEquals(gdAccClientNo3, JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true, query.contains("\"shareholderNo\":\"SH" + gdAccClientNo3 + "\""));
        assertEquals(true, query.contains("\"address\":\"" + gdAccount3 + "\""));
        assertEquals(true, query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":5000,\"lockAmount\":0}"));
        assertEquals(true, query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress, gdAccClientNo4);
        assertEquals(false, query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        query = gd.GDGetShareHolderInfo(gdContractAddress, gdAccClientNo5);
        assertEquals(false, query.contains("\"equityCode\": \"" + gdEquityCode + "\""));        ;

        query = gd.GDGetShareHolderInfo(gdContractAddress, gdAccClientNo6);
        assertEquals(false, query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

    }


    @Test
    public void TC1202_shareRecycleMultiAcc() throws Exception {
        log.info("解除冻结剩下所有冻结数量");
        shareUnlockAll();//解除冻结剩下所有数量

        String eqCode = gdEquityCode;
        String remark = "777777";

        log.info("多个回收前查询机构主体信息");
        String query2 = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);
//        BigDecimal totalShares = new BigDecimal(JSONObject.fromObject(query2).getJSONObject("data").getString("subject_total_share_capital"));
        int totalHolderAccount = JSONObject.fromObject(query2).getJSONObject("data").getInt("subject_shareholders_number");

        log.info("before recycle  " + " 股东总数（个） " + totalHolderAccount);
//        log.info("before recycle 股本总数(股) " + totalShares + " 股东总数（个） " + totalHolderAccount);

        List<Map> shareList = new ArrayList<>();
        Boolean bOnlyZero = false;

        //查询企业所有股东持股情况
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");

        //判断股权代码股东列表中是否只有回收地址存在额度
        //若有其他账户存在余额 则信息存入shareList中准备全部回收
        if(dataShareList.size() == 1 &&
                dataShareList.get(0).toString().contains(zeroAccount)){
            log.info(gdEquityCode + " all recycle!!");
            bOnlyZero = true;
        }
        else{
            //获取排除零地址外的所有账户列表
            shareList = getShareListFromQueryNoZeroAcc(dataShareList);
        }
        String txId = "";
        //全部回收
        if(!bOnlyZero) {
            String response = uf.shareRecycle(gdEquityCode, shareList, false);
            txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");

            commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
            assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));
        }

        String total = getTotalAmountFromShareList(shareList);


        log.info("================================检查存证数据格式化《开始》================================");
        //获取监管数据存证hash
        String jgType = regType;
        String regStoreId = gdCF.getJGStoreHash2(txId,jgType,1);
        jgType = subjectType;
        String subStoreId = gdCF.getJGStoreHash2(txId,jgType,1);

        for(int k = 0;k < shareList.size();k ++) {
            String tempAddr = JSONObject.fromObject(shareList.get(k)).getString("address");
            String tempObjId = mapAccAddr.get(tempAddr).toString();
            String tempAmount = JSONObject.fromObject(shareList.get(k)).getString("amount").replaceAll("\\.0","");
            String tempPP = JSONObject.fromObject(shareList.get(k)).getString("shareProperty");

            registerInfo = gdBF.init05RegInfo();

            log.info("检查回收存证登记格式化及信息内容与传入一致");
//            registerInfo.put("register_rights_change_amount", "-" + tempAmount);     //变动额修改为单个账户发行数量
            registerInfo.put("register_account_obj_id", mapAccAddr.get(tempAddr));
            log.info(gdCF.contructRegisterInfo(regStoreId, shareList.size(),tempObjId,tempPP).toString().replaceAll("\"", ""));
            log.info(registerInfo.toString());
            registerInfo.put("register_nature_of_shares",tempPP);
            assertEquals(registerInfo.toString(), gdCF.contructRegisterInfo(regStoreId, shareList.size(),tempObjId,tempPP).toString().replaceAll("\"", ""));

        }
        log.info("检查回收存证主体格式化及信息内容与传入一致");

//        String getTotal = enterpriseSubjectInfo.get("subject_total_share_capital").toString();
//        BigDecimal oldTotal = new BigDecimal(getTotal);
//        enterpriseSubjectInfo.put("subject_total_share_capital",oldTotal.subtract(new BigDecimal(total)));     //变更总股本数为增发量 + 原始股本总数
        enterpriseSubjectInfo.put("subject_shareholders_number",totalHolderAccount - 3);     //股东个数为原个数减1
        log.info(gdCF.contructEnterpriseSubInfo(subStoreId).toString().replaceAll("\"",""));
//        enterpriseSubjectInfo.put("subject_object_information_type",1);
        log.info(enterpriseSubjectInfo.toString());
        assertEquals(enterpriseSubjectInfo.toString(), gdCF.contructEnterpriseSubInfo(subStoreId).toString().replaceAll("\"",""));

        log.info("================================检查存证数据格式化《结束》================================");


        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息

        //查询企业所有股东持股情况
        query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        dataShareList = JSONObject.fromObject(query).getJSONArray("data");

        assertEquals(true,query.contains("{\"amount\":15000,\"lockAmount\":0,\"shareProperty\":0,\"sharePropertyCN\":\"流通股\",\"address\":\"0000000000000000\"}"));
        assertEquals(true,query.contains("{\"amount\":5000,\"lockAmount\":0,\"shareProperty\":1,\"sharePropertyCN\":\"优先股\",\"address\":\"0000000000000000\"}"));

        //查询股东持股情况 无当前股权代码信息
        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo3);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo4);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo5);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo6);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        log.info("多个回收后查询机构主体信息");
        String query3 = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);
//        BigDecimal totalShares2 = new BigDecimal(JSONObject.fromObject(query3).getJSONObject("data").getString("subject_total_share_capital"));
//
//        log.info("判断回收后机构主体查询总股本数变更正确");
//        assertEquals(oldTotal.subtract(new BigDecimal(total)),totalShares2);


        int totalHolderAccount2 = JSONObject.fromObject(query3).getJSONObject("data").getInt("subject_shareholders_number");
        assertEquals(totalHolderAccount - 3,totalHolderAccount2);//判断股东总数减3
    }

}
