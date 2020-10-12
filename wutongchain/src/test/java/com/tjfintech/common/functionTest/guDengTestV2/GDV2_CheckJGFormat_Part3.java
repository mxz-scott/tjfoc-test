package com.tjfintech.common.functionTest.guDengTestV2;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.GDBeforeCondition;
import com.tjfintech.common.GDCommonFunc;
import com.tjfintech.common.Interface.GuDeng;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.tjfintech.common.GDCommonFunc.*;
import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassGD.*;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class GDV2_CheckJGFormat_Part3 {

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
    }

    @Before
    public void TC06_shareIssue() throws Exception {
        gdEquityCode = "fondTest" + Random(12);

        regNo = "Eq" + "issue" + ( new Date()).getTime();   //区分不同类型的交易登记以流水号
        registerInfo.put("登记流水号",regNo);       //更新对比的登记流水号

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


    //转让全部转出转给已存在的股东
    @Test
    public void TC08_shareTransfer()throws Exception{

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
        fromNow.put("登记流水号",regNo);       //更新对比的登记流水号
        toNow.put("登记流水号",regNo);       //更新对比的登记流水号
        fromNow.put("权利人账户引用",tempObjIdFrom);       //更新对比的权利人账户引用
        toNow.put("权利人账户引用",tempObjIdTo);       //更新对比的权利人账户引用

        txInformation.put("原持有方主体引用",tempObjIdFrom);

        //执行交易
        String response= gd.GDShareTransfer(keyId,fromAddr,transferAmount,toAddr,shareProperty,eqCode,txInformation,fromNow,toNow);

        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

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

//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        String sd = sdf.format(new Date(onChainTS)); // 时间戳转换日期
        log.info("检查过户转让存证登记格式化及信息内容与传入一致:" + tempObjIdFrom);
        fromNow.put("变动额",0);
        fromNow.put("登记时间",txInformation.get("成交时间").toString());
        fromNow.put("当前可用余额",issueAmount - transferAmount);
        fromNow.put("当前冻结余额", 0);   //当前冻结余额修改为实际冻结数

        toNow.put("变动额",0);
        toNow.put("当前冻结余额", 0);   //当前冻结余额修改为实际冻结数
        toNow.put("登记时间",txInformation.get("成交时间").toString());
        toNow.put("当前可用余额",issueAmount + transferAmount);
        log.info(gdCF.contructRegisterInfo(storeId, 3, tempObjIdFrom).toString().replaceAll("\"", ""));
        log.info(fromNow.toString());
//        assertEquals(fromNow.toString(), gdCF.contructRegisterInfo(storeId,3,tempObjIdFrom).toString().replaceAll("\"",""));

        log.info("检查过户转让存证交易格式化及信息内容与传入一致:" + tempObjIdFrom);

        log.info(gdCF.contructTxInfo(storeId, 3, tempObjIdFrom).toString().replaceAll("\"", ""));
        log.info(txInformation.toString());
        assertEquals(txInformation.toString(), gdCF.contructTxInfo(storeId, 3, tempObjIdFrom).toString().replaceAll("\"", ""));

        log.info("检查过户转让存证登记格式化及信息内容与传入一致:" + tempObjIdTo);
        log.info(gdCF.contructRegisterInfo(storeId, 3, tempObjIdTo).toString().replaceAll("\"", ""));
        log.info(toNow.toString());
        assertEquals(toNow.toString(), gdCF.contructRegisterInfo(storeId,3,tempObjIdTo).toString().replaceAll("\"",""));


        log.info("检查转让存证主体格式化及信息内容与传入一致");

        //获取监管数据存证hash
        String jgType = "主体";
        String subStoreId = gdCF.getJGStoreHash2(txId,jgType,1);

        String getTotal = enterpriseSubjectInfo.get("股东总数（个）").toString();
        int oldTotal = Integer.parseInt(getTotal);
        enterpriseSubjectInfo.put("股东总数（个）",oldTotal - 1);     //变更总股本数为增发量 + 原始股本总数
        log.info(gdCF.contructEnterpriseSubInfo(subStoreId).toString().replaceAll("\"",""));
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


        //查询股东持股情况 无当前股权代码信息
        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(gdAccClientNo2,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo2 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount2 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":10000,\"lockAmount\":0}"));

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
     * 增发给新的股东 * 2
     * @throws Exception
     */
    @Test
    public void TC09_shareIncrease() throws Exception {

        log.info("增发前查询机构主体信息");
        String query2 = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);
        BigDecimal totalShares = new BigDecimal(JSONObject.fromObject(query2).getJSONObject("data").getJSONObject(
                "body").getJSONObject("主体信息").getJSONObject("机构主体信息").getJSONObject("企业基本信息").getString("股本总数(股)"));
        int totalHolderAccount = JSONObject.fromObject(query2).getJSONObject("data").getJSONObject(
                "body").getJSONObject("主体信息").getJSONObject("机构主体信息").getJSONObject("企业基本信息").getInt("股东总数（个）");

        String eqCode = gdEquityCode;
        String reason = "股份分红";

        regNo = "Eq" + "increase" + (new Date()).getTime();   //区分不同类型的交易登记以流水号
        registerInfo.put("登记流水号",regNo);       //更新对比的登记流水号

        List<Map> shareList = gdConstructShareList(gdAccount5,increaseAmount,0);
        List<Map> shareList4 = gdConstructShareList(gdAccount6,increaseAmount,0, shareList);

        String response= gd.GDShareIncrease(gdPlatfromKeyID,eqCode,shareList4,reason, equityProductInfo,null);
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
        String jgType = "登记";
        String regStoreId = gdCF.getJGStoreHash2(txId,jgType,1);
        jgType = "产品";
        String prodStoreId = gdCF.getJGStoreHash2(txId,jgType,-1);
        jgType = "主体";
        String subStoreId = gdCF.getJGStoreHash2(txId,jgType,1);

        //遍历检查所有账户登记及交易存证信息
        for(int k = 0 ;k < shareList4.size(); k++) {
            String tempAddr = JSONObject.fromObject(shareList4.get(k)).getString("address");
            String tempObjId = mapAccAddr.get(tempAddr).toString();

            registerInfo = gdBF.init05RegInfo();

            log.info("检查增发存证登记格式化及信息内容与传入一致");
            registerInfo.put("权利人账户引用",tempObjId);
            registerInfo.put("变动额",increaseAmount);     //变动额修改为单个账户发行数量
            log.info(gdCF.contructRegisterInfo(regStoreId,4,tempObjId).toString().replaceAll("\"",""));
            log.info(registerInfo.toString());
            assertEquals(registerInfo.toString(), gdCF.contructRegisterInfo(regStoreId,4,tempObjId).toString().replaceAll("\"",""));

            log.info("检查增发存证产品格式化及信息内容与传入一致");

            log.info(gdCF.contructEquityProdInfo(prodStoreId).toString().replaceAll("\"",""));
            log.info(equityProductInfo.toString());
            assertEquals(equityProductInfo.toString(), gdCF.contructEquityProdInfo(prodStoreId).toString().replaceAll("\"",""));
        }

        log.info("检查增发存证主体格式化及信息内容与传入一致");
        String getTotalMem = enterpriseSubjectInfo.get("股东总数（个）").toString();
        int oldTotalMem = Integer.parseInt(getTotalMem);
        enterpriseSubjectInfo.put("股东总数（个）",oldTotalMem + 2);     //变更总股本数为增发量 + 原始股本总数

        String getTotal = enterpriseSubjectInfo.get("股本总数(股)").toString();
        BigDecimal oldTotal = new BigDecimal(getTotal);
        enterpriseSubjectInfo.put("股本总数(股)",oldTotal.add(new BigDecimal(increaseAmount * 2)));     //变更总股本数为增发量 + 原始股本总数
        log.info(gdCF.contructEnterpriseSubInfo(subStoreId).toString().replaceAll("\"",""));
        log.info(enterpriseSubjectInfo.toString());
        assertEquals(enterpriseSubjectInfo.toString(), gdCF.contructEnterpriseSubInfo(subStoreId).toString().replaceAll("\"",""));

        log.info("================================检查存证数据格式化《结束》================================");


        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,5000,0,0,mapShareENCN().get("0"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount5,5000,1,0,mapShareENCN().get("0"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount6,5000,0,0,mapShareENCN().get("0"),respShareList);
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
        assertEquals(gdAccClientNo5,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo5 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount5 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":5000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo6);
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":5000,\"lockAmount\":0}"));

        log.info("增发后查询机构主体信息");
        String query3 = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);
        BigDecimal totalShares2 = new BigDecimal(JSONObject.fromObject(query3).getJSONObject("data").getJSONObject(
                "body").getJSONObject("主体信息").getJSONObject("机构主体信息").getJSONObject("企业基本信息").getString("股本总数(股)"));

        log.info("判断增发前后机构主体查询总股本数增加数正确");
        assertEquals(totalShares.add(new BigDecimal("4000")),totalShares2);

        int totalHolderAccountAft = JSONObject.fromObject(query2).getJSONObject("data").getJSONObject(
                "body").getJSONObject("主体信息").getJSONObject("机构主体信息").getJSONObject("企业基本信息").getInt("股东总数（个）");

        assertEquals(totalHolderAccountAft,totalHolderAccount + 2);

    }

    /***
     * 账户存在股份性质0*1000 1*1000
     * 冻结0*500 1*500
     * @throws Exception
     */
    public void lockMutli()throws Exception{

    }
}
