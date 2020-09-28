package com.tjfintech.common.functionTest.guDengTestV2;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.GDBeforeCondition;
import com.tjfintech.common.GDCommonFunc;
import com.tjfintech.common.Interface.GuDeng;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
//import net.sf.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class GDV2_EnterpriseRegisterSpecial {

    TestBuilder testBuilder= TestBuilder.getInstance();
    GuDeng gd =testBuilder.getGuDeng();
    Store store =testBuilder.getStore();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    GDCommonFunc gdCF = new GDCommonFunc();
    GDUnitFunc uf = new GDUnitFunc();
    public static String bizNoTest = "test" + Random(12);

    @BeforeClass
    public static void Before()throws Exception{
        GDBeforeCondition gdBefore = new GDBeforeCondition();
//        gdBefore.gdCreateAccout();
        gdBefore.initRegulationData();
    }

    @Before
    public void BeforeTest()throws Exception{
        gdEquityCode = Random(20);
    }

    //企业 股权类 登记
    @Test
    public void TC011_enterpriseRegisterCheckFormat() throws Exception {
        long shareTotals = 1000000;
        String response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,enterpriseSubjectInfo, equityProductInfo,null);
        String txId = net.sf.json.JSONObject.fromObject(response).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", net.sf.json.JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        //查询挂牌企业主体数据
        response = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);
        assertEquals("200", net.sf.json.JSONObject.fromObject(response).getString("state"));


        //获取主体/产品存证hash
        String query = store.GetTxDetail(txId);
        String questInfo = net.sf.json.JSONObject.fromObject(query).getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getJSONObject("arg").getJSONArray("args").get(0).toString();
        JSONObject jsonObject = JSONObject.parseObject(questInfo);

        String SubjectObjectTxId = jsonObject.getString("SubjectObjectTxId");
        String ProductInfoTxId = jsonObject.getString("ProductInfoTxId");

        //检查主体存证信息内容与传入一致
        log.info("检查主体存证信息内容与传入一致");
        log.info(commonFunc.contructEnterpriseSubInfo(SubjectObjectTxId).toString().replaceAll("\"",""));
        log.info(enterpriseSubjectInfo.toString());
        assertEquals(enterpriseSubjectInfo.toString(), commonFunc.contructEnterpriseSubInfo(SubjectObjectTxId).toString().replaceAll("\"",""));

        //检查产品存证信息内容与传入一致
        log.info("检查产品存证信息内容与传入一致");
        log.info(commonFunc.contructEquityProdInfo(ProductInfoTxId).toString().replaceAll("\"",""));
        log.info(equityProductInfo.toString());
        assertEquals(equityProductInfo.toString(), commonFunc.contructEquityProdInfo(ProductInfoTxId).toString().replaceAll("\"",""));
    }


    //企业 债券类 登记
    @Test
    public void TC012_enterpriseRegisterCheckFormat() throws Exception {
        long shareTotals = 1000000;
        String response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,enterpriseSubjectInfo, null,bondProductInfo);
        String txId = net.sf.json.JSONObject.fromObject(response).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", net.sf.json.JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        //查询挂牌企业主体数据
        response = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);
        assertEquals("200", net.sf.json.JSONObject.fromObject(response).getString("state"));

        //获取主体/产品存证hash
        String query = store.GetTxDetail(txId);
        String questInfo = net.sf.json.JSONObject.fromObject(query).getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getJSONObject("arg").getJSONArray("args").get(0).toString();
        JSONObject jsonObject = JSONObject.parseObject(questInfo);

        String SubjectObjectTxId = jsonObject.getString("SubjectObjectTxId");
        String ProductInfoTxId = jsonObject.getString("ProductInfoTxId");

        //检查主体存证信息内容与传入一致
        log.info("检查主体存证信息内容与传入一致");
        log.info(commonFunc.contructEnterpriseSubInfo(SubjectObjectTxId).toString().replaceAll("\"",""));
        log.info(enterpriseSubjectInfo.toString());
        assertEquals(enterpriseSubjectInfo.toString(), commonFunc.contructEnterpriseSubInfo(SubjectObjectTxId).toString().replaceAll("\"",""));

        //检查产品存证信息内容与传入一致
        log.info("检查产品存证信息内容与传入一致");
        log.info(commonFunc.contructBondProdInfo(ProductInfoTxId).toString().replaceAll("\"",""));
        log.info(bondProductInfo.toString());
        assertEquals(bondProductInfo.toString(), commonFunc.contructBondProdInfo(ProductInfoTxId).toString().replaceAll("\"",""));


    }

    //会员登记
    @Test
    public void TC013_enterpriseRegisterCheckFormat() throws Exception {
        long shareTotals = 1000000;
        String response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,enterpriseSubjectInfo, null,null);
        String txId = net.sf.json.JSONObject.fromObject(response).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", net.sf.json.JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        //查询挂牌企业主体数据
        response = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);
        assertEquals("200", net.sf.json.JSONObject.fromObject(response).getString("state"));

        //获取主体/产品存证hash
        String query = store.GetTxDetail(txId);
        String questInfo = net.sf.json.JSONObject.fromObject(query).getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getJSONObject("arg").getJSONArray("args").get(0).toString();
        JSONObject jsonObject = JSONObject.parseObject(questInfo);

        String SubjectObjectTxId = jsonObject.getString("SubjectObjectTxId");
        String ProductInfoTxId = jsonObject.getString("ProductInfoTxId");

        log.info("判断无产品存证交易");
        assertEquals("",ProductInfoTxId);

        //检查主体存证信息内容与传入一致
        log.info("检查主体存证信息内容与传入一致");
        log.info(commonFunc.contructEnterpriseSubInfo(SubjectObjectTxId).toString().replaceAll("\"",""));
        log.info(enterpriseSubjectInfo.toString());
        assertEquals(enterpriseSubjectInfo.toString(), commonFunc.contructEnterpriseSubInfo(SubjectObjectTxId).toString().replaceAll("\"",""));

    }

    @Test
    public void TC014_createAccTestCheckFormat() throws Exception {
        GDBeforeCondition gdBC = new GDBeforeCondition();

        String cltNo = "tet00" + Random(12);
        Map mapCreate = gdBC.gdCreateAccParam(cltNo);
        String txId = mapCreate.get("txId").toString();
        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", net.sf.json.JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        //查询投资者账户信息
        String response = gd.GDGetShareHolderInfo(gdContractAddress,cltNo);
        assertEquals("200", net.sf.json.JSONObject.fromObject(response).getString("state"));

        //查询个人主体信息
        response = gd.GDMainSubjectQuery(gdContractAddress,cltNo);
        assertEquals("200", net.sf.json.JSONObject.fromObject(response).getString("state"));

        //获取主体/产品存证hash
        String query = store.GetTxDetail(txId);
        String questInfo = net.sf.json.JSONObject.fromObject(query).getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getJSONObject("arg").getJSONArray("args").get(0).toString();
        JSONObject jsonObject = JSONObject.parseObject(questInfo);

        String SubjectObjectTxId = jsonObject.getString("SubjectObjectTxId");
        String AccountInfoListTxId = jsonObject.getString("AccountInfoListTxId");


        //检查主体存证信息内容与传入一致
        log.info("检查主体存证信息内容与传入一致");
        log.info(commonFunc.contructPersonalSubInfo(SubjectObjectTxId).toString().replaceAll("\"",""));
        log.info(investorSubjectInfo.toString());
        assertEquals(investorSubjectInfo.toString().replaceAll(" ","").replaceAll("\"",""),
                commonFunc.contructPersonalSubInfo(SubjectObjectTxId).toString().replaceAll(" ","").replaceAll("\"",""));

        //检查账户存证信息内容与传入一致
        log.info("检查资金账户存证信息内容与传入一致");
        log.info(commonFunc.contructFundAccountInfo(AccountInfoListTxId,cltNo).toString().replaceAll("\"",""));
        log.info(fundaccountInfo.toString());
        assertEquals(fundaccountInfo.toString().replaceAll(" ","").replaceAll("\"",""),
                commonFunc.contructFundAccountInfo(AccountInfoListTxId,cltNo).toString().replaceAll(" ","").replaceAll("\"",""));

        log.info("检查资金账户存证信息内容与传入一致");
        log.info(commonFunc.contructEquityAccountInfo(AccountInfoListTxId,cltNo).toString().replaceAll("\"",""));
        log.info(equityaccountInfo.toString());
        assertEquals(equityaccountInfo.toString().replaceAll(" ","").replaceAll("\"",""),
                commonFunc.contructEquityAccountInfo(AccountInfoListTxId,cltNo).toString().replaceAll(" ","").replaceAll("\"",""));


    }


    //企业 股权类 登记
    //产品主体引用置为空
    @Test
    public void TC021_enterpriseRegisterEmpty() throws Exception {
        Map mapEqOk = bondProductInfo;
        String obj = bondProductInfo.get("发行主体引用").toString();
        bondProductInfo.put("发行主体引用","");
        long shareTotals = 1000000;
        String response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,enterpriseSubjectInfo, bondProductInfo,null);
        String txId = net.sf.json.JSONObject.fromObject(response).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",net.sf.json.JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        bondProductInfo = mapEqOk;

        String query = store.GetTxDetail(txId);

        //检查传空的内容是否自动传主体的对象标识
        String questInfo = net.sf.json.JSONObject.fromObject(query).getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getJSONObject("arg").getJSONArray("args").get(0).toString();
        JSONObject jsonObject = JSONObject.parseObject(questInfo);

        String productInfoInfoList = jsonObject.getJSONArray("ProductInfo").get(0).toString();
        JSONObject jsonObject2 = JSONObject.parseObject(productInfoInfoList);

        //检查账户所属主体引用 是否使用了enterpriseSubjectInfo结构中的对象标识
        assertEquals(enterpriseSubjectInfo.get("对象标识").toString(),jsonObject2.getString("发行主体引用"));

    }


    //企业 债券类 登记
    //产品主体引用置为空
    @Test
    public void TC022_enterpriseRegisterEmpty() throws Exception {
        Map mapEqOk = equityProductInfo;
        String obj = equityProductInfo.get("发行主体引用").toString();
        equityProductInfo.put("发行主体引用","");
        long shareTotals = 1000000;
        String response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,enterpriseSubjectInfo, equityProductInfo,null);
        String txId =  net.sf.json.JSONObject.fromObject(response).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",net.sf.json.JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        equityProductInfo = mapEqOk;

        String query = store.GetTxDetail(txId);

        //检查传空的内容是否自动传主体的对象标识
        String questInfo = net.sf.json.JSONObject.fromObject(query).getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getJSONObject("arg").getJSONArray("args").get(0).toString();
        JSONObject jsonObject = JSONObject.parseObject(questInfo);

        String productInfoInfoList = jsonObject.getJSONArray("ProductInfo").get(0).toString();
        JSONObject jsonObject2 = JSONObject.parseObject(productInfoInfoList);

        //检查账户所属主体引用 是否使用了enterpriseSubjectInfo结构中的对象标识
        assertEquals(enterpriseSubjectInfo.get("对象标识").toString(),jsonObject2.getString("发行主体引用"));

    }

    //开户
    //对象标识为空
    @Test
    public void TC023_createAccTestEmpty() throws Exception {
        GDBeforeCondition gdBC = new GDBeforeCondition();
        Map fundOk = equityaccountInfo;
        equityaccountInfo.put("账户所属主体引用","");
        equityaccountInfo.put("关联账户对象引用","");

        fundaccountInfo.put("账户所属主体引用","");
        fundaccountInfo.put("关联账户对象引用","");


        String cltNo = "tet00" + Random(12);
        String shareHolderNo = "SH" + cltNo;
        String fundNo = "fund" + cltNo;

        //构造股权账户信息
        Map shareHolderInfo = new HashMap();
        equityaccountInfo.put("账户对象标识",cltNo);  //更新账户对象标识字段
        shareHolderInfo.put("shareholderNo",shareHolderNo);
        shareHolderInfo.put("accountInfo", equityaccountInfo);

        //构造资金账户信息
        fundaccountInfo.put("账户对象标识",cltNo);  //更新账户对象标识字段
        Map mapFundInfo = new HashMap();
        mapFundInfo.put("fundNo",fundNo);
        mapFundInfo.put("accountInfo",fundaccountInfo);

        //构造个人/投资者主体信息
        gdBC.init01PersonalSubjectInfo();
        investorSubjectInfo.put("对象标识",cltNo);  //更新对象标识字段
        investorSubjectInfo.put("主体标识","sid" + cltNo);  //更新主体标识字段

        String response = gd.GDCreateAccout(gdContractAddress,cltNo,mapFundInfo,shareHolderInfo, investorSubjectInfo);
        commonFunc.sdkCheckTxOrSleep(net.sf.json.JSONObject.fromObject(response).getJSONObject("data").getString("txId"),utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //检查传空的内容是否自动传主体的对象标识
        String check = store.GetTxDetail(net.sf.json.JSONObject.fromObject(response).getJSONObject("data").getString("txId"));
        assertEquals("200",net.sf.json.JSONObject.fromObject(check).getString("state"));
//        log.info(investorSubjectInfo.get("对象标识").toString());

        String questInfo = net.sf.json.JSONObject.fromObject(check).getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getJSONObject("arg").getJSONArray("args").get(0).toString();

        JSONObject jsonObject = JSONObject.parseObject(questInfo);
//        log.info(jsonObject.getString("ClientNo"));
//        log.info(jsonObject.getJSONObject("Investor").getString("对象标识"));
        String accInfoList = jsonObject.getJSONArray("AccountInfoList").get(0).toString();
        JSONObject jsonObjectAcc = JSONObject.parseObject(accInfoList);
        //检查账户所属主体引用 是否使用了investor的对象标识
        assertEquals(jsonObject.getJSONObject("Investor").getString("对象标识"),
                jsonObjectAcc.getJSONObject("FundAccount").getJSONObject("AccountInfo").getString("账户所属主体引用"));
        assertEquals(jsonObject.getJSONObject("Investor").getString("对象标识"),
                jsonObjectAcc.getJSONObject("FundAccount").getJSONObject("AccountInfo").getString("关联账户对象引用"));
        assertEquals(jsonObject.getJSONObject("Investor").getString("对象标识"),
                jsonObjectAcc.getJSONObject("ShareholderAccount").getJSONObject("AccountInfo").getString("账户所属主体引用"));
        //当前未自动填入
//        assertEquals(jsonObject.getJSONObject("Investor").getString("对象标识"),
//                jsonObjectAcc.getJSONObject("ShareholderAccount").getJSONObject("AccountInfo").getString("关联账户对象引用"));

    }



}
