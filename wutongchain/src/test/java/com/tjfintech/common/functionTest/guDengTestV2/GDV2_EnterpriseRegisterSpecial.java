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
import net.sf.json.JSONArray;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassGD.*;
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
    public void TC011_enterpriseRegisterEquityCheckFormat() throws Exception {
        long shareTotals = 1000000;
        String response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,enterpriseSubjectInfo, equityProductInfo,null);
        String txId = net.sf.json.JSONObject.fromObject(response).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", net.sf.json.JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        //查询挂牌企业主体数据  交易上链后 数据可能还未写入合约 在此做2s内数据查询
        for(int i = 0;i<20;i++) {
            response = gd.GDMainSubjectQuery(gdContractAddress, gdCompanyID);
            if(net.sf.json.JSONObject.fromObject(response).getString("state").equals("200"))
                break;
            sleepAndSaveInfo(100);
        }
        assertEquals("200", net.sf.json.JSONObject.fromObject(response).getString("state"));



        //获取主体/产品存证hash
        String query = store.GetTxDetail(txId);
        String questInfo = net.sf.json.JSONObject.fromObject(query).getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getJSONObject("arg").getJSONArray("args").get(0).toString();
        JSONObject jsonObject = JSONObject.parseObject(questInfo);

        String SubjectObjectTxId = jsonObject.getString("SubjectObjectTxId");
        String ProductInfoTxId = jsonObject.getString("ProductInfoTxId");

        //检查主体存证信息内容与传入一致
        log.info("检查主体存证信息内容与传入一致");
        log.info(gdCF.contructEnterpriseSubInfo(SubjectObjectTxId).toString().replaceAll("\"",""));
        log.info(enterpriseSubjectInfo.toString());
        assertEquals(enterpriseSubjectInfo.toString(), gdCF.contructEnterpriseSubInfo(SubjectObjectTxId).toString().replaceAll("\"",""));

        //检查产品存证信息内容与传入一致
        log.info("检查产品存证信息内容与传入一致");
        log.info(gdCF.contructEquityProdInfo(ProductInfoTxId).toString().replaceAll("\"",""));
        log.info(equityProductInfo.toString());
        assertEquals(equityProductInfo.toString(), gdCF.contructEquityProdInfo(ProductInfoTxId).toString().replaceAll("\"",""));
    }


    //企业 债券类 登记
    @Test
    public void TC012_enterpriseRegisterBondCheckFormat() throws Exception {
        long shareTotals = 1000000;
        String response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,enterpriseSubjectInfo, null,bondProductInfo);
        String txId = net.sf.json.JSONObject.fromObject(response).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", net.sf.json.JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        //查询挂牌企业主体数据  交易上链后 数据可能还未写入合约 在此做2s内数据查询
        for(int i = 0;i<20;i++) {
            response = gd.GDMainSubjectQuery(gdContractAddress, gdCompanyID);
            if(net.sf.json.JSONObject.fromObject(response).getString("state").equals("200"))
                break;
            sleepAndSaveInfo(100);
        }
        assertEquals("200", net.sf.json.JSONObject.fromObject(response).getString("state"));

        //获取主体/产品存证hash
        String query = store.GetTxDetail(txId);
        String questInfo = net.sf.json.JSONObject.fromObject(query).getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getJSONObject("arg").getJSONArray("args").get(0).toString();
        JSONObject jsonObject = JSONObject.parseObject(questInfo);

        String SubjectObjectTxId = jsonObject.getString("SubjectObjectTxId");
        String ProductInfoTxId = jsonObject.getString("ProductInfoTxId");

        //检查主体存证信息内容与传入一致
        log.info("检查主体存证信息内容与传入一致");
        log.info(gdCF.contructEnterpriseSubInfo(SubjectObjectTxId).toString().replaceAll("\"",""));
        log.info(enterpriseSubjectInfo.toString());
        assertEquals(enterpriseSubjectInfo.toString(), gdCF.contructEnterpriseSubInfo(SubjectObjectTxId).toString().replaceAll("\"",""));

        //检查产品存证信息内容与传入一致
        log.info("检查产品存证信息内容与传入一致");
        log.info(gdCF.contructBondProdInfo(ProductInfoTxId).toString().replaceAll("\"",""));
        log.info(bondProductInfo.toString());
        assertEquals(bondProductInfo.toString(), gdCF.contructBondProdInfo(ProductInfoTxId).toString().replaceAll("\"",""));


    }

    //会员登记
    @Test
    public void TC013_enterpriseRegisterMemberCheckFormat() throws Exception {
        long shareTotals = 1000000;
        String response= gd.GDEnterpriseResister(gdContractAddress,"",0,enterpriseSubjectInfo, null,null);
        String txId = net.sf.json.JSONObject.fromObject(response).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", net.sf.json.JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        //查询挂牌企业主体数据  交易上链后 数据可能还未写入合约 在此做2s内数据查询
        for(int i = 0;i<20;i++) {
            response = gd.GDMainSubjectQuery(gdContractAddress, gdCompanyID);
            if(net.sf.json.JSONObject.fromObject(response).getString("state").equals("200"))
                break;
            sleepAndSaveInfo(100);
        }
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
        log.info(gdCF.contructEnterpriseSubInfo(SubjectObjectTxId).toString().replaceAll("\"",""));
        log.info(enterpriseSubjectInfo.toString());
        assertEquals(enterpriseSubjectInfo.toString(), gdCF.contructEnterpriseSubInfo(SubjectObjectTxId).toString().replaceAll("\"",""));

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
        String response = "";
        for(int i=0;i<50;i++){
            response = gd.GDGetShareHolderInfo(gdContractAddress,cltNo);
            if(net.sf.json.JSONObject.fromObject(response).getString("state").equals("200")) break;
            sleepAndSaveInfo(100);
        }
        assertEquals("200", net.sf.json.JSONObject.fromObject(response).getString("state"));

        //查询个人主体信息  交易上链后 数据可能还未写入合约 在此做2s内数据查询
        for(int i = 0;i<20;i++) {
            response = gd.GDMainSubjectQuery(gdContractAddress, cltNo);
            if(net.sf.json.JSONObject.fromObject(response).getString("state").equals("200"))
                break;
            sleepAndSaveInfo(100);
        }
        assertEquals("200", net.sf.json.JSONObject.fromObject(response).getString("state"));

        //获取监管数据存证hash
        String jgType = "账户";
        String accStoreId = gdCF.getJGStoreHash2(txId,jgType,-1);
        jgType = "主体";
        String subStoreId = gdCF.getJGStoreHash2(txId,jgType,-1);


        //检查主体存证信息内容与传入一致
        log.info("检查主体存证信息内容与传入一致");
        log.info(gdCF.contructPersonalSubInfo(subStoreId).toString().replaceAll("\"",""));
        log.info(investorSubjectInfo.toString());
        assertEquals(investorSubjectInfo.toString().replaceAll(" ","").replaceAll("\"",""),
                gdCF.contructPersonalSubInfo(subStoreId).toString().replaceAll(" ","").replaceAll("\"",""));

        //检查账户存证信息内容与传入一致
        log.info("检查资金账户存证信息内容与传入一致");
        log.info(gdCF.contructFundAccountInfo(accStoreId,cltNo).toString().replaceAll("\"",""));
        log.info(fundaccountInfo.toString());
        assertEquals(fundaccountInfo.toString().replaceAll(" ","").replaceAll("\"","").replaceAll("\\[","").replaceAll("]",""),
                gdCF.contructFundAccountInfo(accStoreId,cltNo).toString().replaceAll(" ","").replaceAll("\"",""));

        log.info("检查资金账户存证信息内容与传入一致");
        log.info(gdCF.contructEquityAccountInfo(accStoreId,cltNo).toString().replaceAll("\"",""));
        log.info(equityaccountInfo.toString());
        assertEquals(equityaccountInfo.toString().replaceAll(" ","").replaceAll("\"","").replaceAll("\\[","").replaceAll("]",""),
                gdCF.contructEquityAccountInfo(accStoreId,cltNo).toString().replaceAll(" ","").replaceAll("\"",""));


    }


    //企业 股权类 登记
    //产品主体引用置为空
    @Test
    public void TC021_enterpriseRegisterEquityEmpty() throws Exception {

        bondProductInfo.put("发行主体引用","");
        long shareTotals = 1000000;
        String response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,enterpriseSubjectInfo, equityProductInfo,null);
        String txId = net.sf.json.JSONObject.fromObject(response).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",net.sf.json.JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));


        //获取主体/产品存证hash
        String query = store.GetTxDetail(txId);
        String questInfo = net.sf.json.JSONObject.fromObject(query).getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getJSONObject("arg").getJSONArray("args").get(0).toString();
        JSONObject jsonObject = JSONObject.parseObject(questInfo);

        String SubjectObjectTxId = jsonObject.getString("SubjectObjectTxId");
        String ProductInfoTxId = jsonObject.getString("ProductInfoTxId");

        //检查主体存证信息内容与传入一致
        log.info("检查债券产品发行主体引用与主体对象标识一致");
        equityProductInfo.put("发行主体引用",enterpriseSubjectInfo.get("对象标识").toString());
        log.info(gdCF.contructEquityProdInfo(ProductInfoTxId).toString().replaceAll("\"",""));
        log.info(equityProductInfo.toString());
        assertEquals(equityProductInfo.toString(), gdCF.contructEquityProdInfo(ProductInfoTxId).toString().replaceAll("\"",""));

    }


    //企业 债券类 登记
    //产品主体引用置为空
    @Test
    public void TC022_enterpriseRegisterBondEmpty() throws Exception {
        Map mapEqOk = equityProductInfo;
        String obj = equityProductInfo.get("发行主体引用").toString();
        equityProductInfo.put("发行主体引用","");
        long shareTotals = 1000000;
        String response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,enterpriseSubjectInfo, null,bondProductInfo);
        String txId =  net.sf.json.JSONObject.fromObject(response).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",net.sf.json.JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        //获取主体/产品存证hash
        String query = store.GetTxDetail(txId);
        String questInfo = net.sf.json.JSONObject.fromObject(query).getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getJSONObject("arg").getJSONArray("args").get(0).toString();
        JSONObject jsonObject = JSONObject.parseObject(questInfo);

        String SubjectObjectTxId = jsonObject.getString("SubjectObjectTxId");
        String ProductInfoTxId = jsonObject.getString("ProductInfoTxId");

        //检查主体存证信息内容与传入一致
        log.info("检查债券产品发行主体引用与主体对象标识一致");
        bondProductInfo.put("发行主体引用",enterpriseSubjectInfo.get("对象标识").toString());
        log.info(gdCF.contructBondProdInfo(ProductInfoTxId).toString().replaceAll("\"",""));
        log.info(bondProductInfo.toString());
        assertEquals(bondProductInfo.toString(), gdCF.contructBondProdInfo(ProductInfoTxId).toString().replaceAll("\"",""));

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

        String txId = net.sf.json.JSONObject.fromObject(response).getJSONObject("data").getString("txId");

        //获取监管数据存证hash
        String jgType = "账户";
        String accStoreId = gdCF.getJGStoreHash2(txId,jgType,-1);
        jgType = "主体";
        String subStoreId = gdCF.getJGStoreHash2(txId,jgType,-1);


        equityaccountInfo.put("账户所属主体引用",investorSubjectInfo.get("对象标识"));
//        equityaccountInfo.put("关联账户对象引用",investorSubjectInfo.get("对象标识"));//当前未自动填入

        fundaccountInfo.put("账户所属主体引用",investorSubjectInfo.get("对象标识"));
        fundaccountInfo.put("关联账户对象引用",investorSubjectInfo.get("对象标识"));

        //检查主体存证信息内容与传入一致
        log.info("检查主体存证信息内容与传入一致");
        log.info(gdCF.contructPersonalSubInfo(subStoreId).toString().replaceAll("\"",""));
        log.info(investorSubjectInfo.toString());
        assertEquals(investorSubjectInfo.toString().replaceAll(" ","").replaceAll("\"",""),
                gdCF.contructPersonalSubInfo(subStoreId).toString().replaceAll(" ","").replaceAll("\"",""));

        //检查账户存证信息内容与传入一致
        log.info("检查资金账户存证信息内容与传入一致");
        log.info(gdCF.contructFundAccountInfo(accStoreId,cltNo).toString().replaceAll("\"",""));
        log.info(fundaccountInfo.toString());
        assertEquals(fundaccountInfo.toString().replaceAll(" ","").replaceAll("\"","").replaceAll("\\[","").replaceAll("]",""),
                gdCF.contructFundAccountInfo(accStoreId,cltNo).toString().replaceAll(" ","").replaceAll("\"",""));

        log.info("检查股权账户存证信息内容与传入一致");
        log.info(gdCF.contructEquityAccountInfo(accStoreId,cltNo).toString().replaceAll("\"",""));
        log.info(equityaccountInfo.toString());
        assertEquals(equityaccountInfo.toString().replaceAll(" ","").replaceAll("\"","").replaceAll("\\[","").replaceAll("]",""),
                gdCF.contructEquityAccountInfo(accStoreId,cltNo).toString().replaceAll(" ","").replaceAll("\"",""));



    }



}
