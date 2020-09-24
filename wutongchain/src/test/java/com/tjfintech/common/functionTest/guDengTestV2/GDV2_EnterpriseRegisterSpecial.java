package com.tjfintech.common.functionTest.guDengTestV2;

import com.alibaba.fastjson.JSONArray;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.GDBeforeCondition;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    public void TC01_enterpriseRegister() throws Exception {
        long shareTotals = 1000000;
        String response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,enterpriseSubjectInfo, equityProductInfo,null);
        String txId = net.sf.json.JSONObject.fromObject(response).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", net.sf.json.JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        //查询挂牌企业主体数据
        response = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);
        assertEquals("200", net.sf.json.JSONObject.fromObject(response).getString("state"));

        Map jsonMap = net.sf.json.JSONObject.fromObject(response).getJSONObject("data");
        jsonMap.put("对象标识",gdCompanyID);

        log.info("判断获取的主体信息是否与传入的一致");
        assertEquals(44,enterpriseSubjectInfo.size());
        assertEquals(enterpriseSubjectInfo.size(),jsonMap.size());


        //获取主体/产品存证hash
        String query = store.GetTxDetail(txId);
        String questInfo = net.sf.json.JSONObject.fromObject(query).getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getJSONObject("arg").getJSONArray("args").get(0).toString();
        JSONObject jsonObject = JSONObject.parseObject(questInfo);

        String SubjectObjectTxId = jsonObject.getString("SubjectObjectTxId");
        String ProductInfoTxId = jsonObject.getString("ProductInfoTxId");

        //检查主体存证信息内容与传入一致
        log.info(contructEnterpriseSubInfo(SubjectObjectTxId).toString().replaceAll("\"",""));
        log.info(enterpriseSubjectInfo.toString());
        assertEquals(enterpriseSubjectInfo.toString(), contructEnterpriseSubInfo(SubjectObjectTxId).toString().replaceAll("\"",""));

        //检查产品存证信息内容与传入一致
        log.info(contructEquityProdInfo(ProductInfoTxId).toString().replaceAll("\"",""));
        log.info(equityProductInfo.toString());
        assertEquals(equityProductInfo.toString(), contructEquityProdInfo(ProductInfoTxId).toString().replaceAll("\"",""));
    }


    //企业 债券类 登记
    @Test
    public void TC02_enterpriseRegister() throws Exception {
        long shareTotals = 1000000;
        String response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,enterpriseSubjectInfo, null,bondProductInfo);
        net.sf.json.JSONObject jsonObject= net.sf.json.JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", net.sf.json.JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        //查询挂牌企业主体数据
        response = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);
        assertEquals("200", net.sf.json.JSONObject.fromObject(response).getString("state"));

        Map jsonMap = net.sf.json.JSONObject.fromObject(response).getJSONObject("data");
        jsonMap.put("对象标识",gdCompanyID);

        log.info("判断获取的主体信息是否与传入的一致");
        assertEquals(44,enterpriseSubjectInfo.size());
        assertEquals(enterpriseSubjectInfo.size(),jsonMap.size());

    }

    //会员登记
    @Test
    public void TC03_enterpriseRegister() throws Exception {
        long shareTotals = 1000000;
        String response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,enterpriseSubjectInfo, null,null);
        net.sf.json.JSONObject jsonObject= net.sf.json.JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", net.sf.json.JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        //查询挂牌企业主体数据
        response = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);
        assertEquals("200", net.sf.json.JSONObject.fromObject(response).getString("state"));

        Map jsonMap = net.sf.json.JSONObject.fromObject(response).getJSONObject("data");
        jsonMap.put("对象标识",gdCompanyID);

        log.info("判断获取的主体信息是否与传入的一致");
        assertEquals(44,enterpriseSubjectInfo.size());
        assertEquals(enterpriseSubjectInfo.size(),jsonMap.size());

    }


    //企业 股权类 登记
    //产品主体引用置为空
    @Test
    public void TC011_enterpriseRegister() throws Exception {
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
    public void TC012_enterpriseRegister() throws Exception {
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
    public void TC03_createAccTest() throws Exception {
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

    public Map contructEnterpriseSubInfo(String subTxId){
        JSONObject object2 = JSONObject.parseObject(store.GetTxDetail(subTxId));
        String storeData2 = object2.getJSONObject("data").getJSONObject("store").getString("storeData");
        JSONArray jsonArray2 = JSONArray.parseArray(storeData2);
        JSONObject jobj2 = JSONObject.parseObject(jsonArray2.get(0).toString());
        JSONObject objSubBase = jobj2.getJSONObject("body").getJSONObject("主体信息").getJSONObject("主体基本信息");
        JSONObject objEnterpriseSub = jobj2.getJSONObject("body").getJSONObject("主体信息").getJSONObject("机构主体信息");

        Map getSubjectInfo = new HashMap();
        getSubjectInfo.put("对象标识",jobj2.getJSONObject("body").getJSONObject("对象信息").getString("对象标识"));
        getSubjectInfo.put("主体标识",objSubBase.getJSONObject("主体通用信息").getString("主体标识"));
        getSubjectInfo.put("行业主体代号",objSubBase.getJSONObject("主体通用信息").getString("行业主体代号"));
        getSubjectInfo.put("主体类型",objSubBase.getJSONObject("主体通用信息").getIntValue("主体类型"));
        getSubjectInfo.put("主体信息创建时间",objSubBase.getJSONObject("主体通用信息").getString("主体信息创建时间"));
        List<Map> list = JSONObject.parseArray(objSubBase.getJSONArray("主体资质信息").toJSONString(), Map.class);
        getSubjectInfo.put("主体资质信息",list);

        getSubjectInfo.put("机构类型",objEnterpriseSub.getJSONObject("机构分类信息").getIntValue("机构类型"));
        getSubjectInfo.put("机构性质",objEnterpriseSub.getJSONObject("机构分类信息").getIntValue("机构性质"));

        getSubjectInfo.put("公司全称",objEnterpriseSub.getJSONObject("企业基本信息").getString("公司全称"));
        getSubjectInfo.put("英文名称",objEnterpriseSub.getJSONObject("企业基本信息").getString("英文名称"));
        getSubjectInfo.put("公司简称",objEnterpriseSub.getJSONObject("企业基本信息").getString("公司简称"));
        getSubjectInfo.put("英文简称",objEnterpriseSub.getJSONObject("企业基本信息").getString("英文简称"));
        getSubjectInfo.put("企业类型",objEnterpriseSub.getJSONObject("企业基本信息").getIntValue("企业类型"));
        getSubjectInfo.put("企业成分",objEnterpriseSub.getJSONObject("企业基本信息").getIntValue("企业成分"));
        getSubjectInfo.put("统一社会信用代码",objEnterpriseSub.getJSONObject("企业基本信息").getString("统一社会信用代码"));
        getSubjectInfo.put("组织机构代码",objEnterpriseSub.getJSONObject("企业基本信息").getString("组织机构代码"));
        getSubjectInfo.put("设立日期",objEnterpriseSub.getJSONObject("企业基本信息").getString("设立日期"));
        getSubjectInfo.put("营业执照",objEnterpriseSub.getJSONObject("企业基本信息").getString("营业执照"));
        getSubjectInfo.put("经营范围",objEnterpriseSub.getJSONObject("企业基本信息").getString("经营范围"));
        getSubjectInfo.put("企业所属行业",objEnterpriseSub.getJSONObject("企业基本信息").getIntValue("企业所属行业"));
        getSubjectInfo.put("主营业务",objEnterpriseSub.getJSONObject("企业基本信息").getString("主营业务"));
        getSubjectInfo.put("公司简介",objEnterpriseSub.getJSONObject("企业基本信息").getString("公司简介"));
        getSubjectInfo.put("注册资本",objEnterpriseSub.getJSONObject("企业基本信息").getString("注册资本"));
        getSubjectInfo.put("注册资本币种",objEnterpriseSub.getJSONObject("企业基本信息").getIntValue("注册资本币种"));
        getSubjectInfo.put("实收资本",objEnterpriseSub.getJSONObject("企业基本信息").getString("实收资本"));
        getSubjectInfo.put("实收资本币种",objEnterpriseSub.getJSONObject("企业基本信息").getIntValue("实收资本币种"));
        getSubjectInfo.put("注册地址",objEnterpriseSub.getJSONObject("企业基本信息").getString("注册地址"));
        getSubjectInfo.put("办公地址",objEnterpriseSub.getJSONObject("企业基本信息").getString("办公地址"));
        getSubjectInfo.put("联系地址",objEnterpriseSub.getJSONObject("企业基本信息").getString("联系地址"));
        getSubjectInfo.put("联系电话",objEnterpriseSub.getJSONObject("企业基本信息").getString("联系电话"));
        getSubjectInfo.put("传真",objEnterpriseSub.getJSONObject("企业基本信息").getString("传真"));
        getSubjectInfo.put("邮政编码",objEnterpriseSub.getJSONObject("企业基本信息").getString("邮政编码"));
        getSubjectInfo.put("互联网地址",objEnterpriseSub.getJSONObject("企业基本信息").getString("互联网地址"));
        getSubjectInfo.put("电子邮箱",objEnterpriseSub.getJSONObject("企业基本信息").getString("电子邮箱"));
        getSubjectInfo.put("公司章程",objEnterpriseSub.getJSONObject("企业基本信息").getString("公司章程"));
        getSubjectInfo.put("主管单位",objEnterpriseSub.getJSONObject("企业基本信息").getString("主管单位"));
        getSubjectInfo.put("股东总数（个）",objEnterpriseSub.getJSONObject("企业基本信息").getIntValue("股东总数（个）"));
        getSubjectInfo.put("股本总数(股)",objEnterpriseSub.getJSONObject("企业基本信息").getString("股本总数(股)"));
        getSubjectInfo.put("法定代表人姓名",objEnterpriseSub.getJSONObject("法人信息").getString("法定代表人姓名"));
        getSubjectInfo.put("法人性质",objEnterpriseSub.getJSONObject("法人信息").getIntValue("法人性质"));
        getSubjectInfo.put("法定代表人身份证件类型",objEnterpriseSub.getJSONObject("法人信息").getIntValue("法定代表人身份证件类型"));
        getSubjectInfo.put("法定代表人身份证件号码",objEnterpriseSub.getJSONObject("法人信息").getString("法定代表人身份证件号码"));
        getSubjectInfo.put("法定代表人职务",objEnterpriseSub.getJSONObject("法人信息").getIntValue("法定代表人职务"));
        getSubjectInfo.put("法定代表人手机号",objEnterpriseSub.getJSONObject("法人信息").getString("法定代表人手机号"));

        return getSubjectInfo;
    }

    public Map contructPersonalSubInfo(String subTxId){
        JSONObject object2 = JSONObject.parseObject(store.GetTxDetail(subTxId));
        String storeData2 = object2.getJSONObject("data").getJSONObject("store").getString("storeData");
        JSONArray jsonArray2 = JSONArray.parseArray(storeData2);
        JSONObject jobj2 = JSONObject.parseObject(jsonArray2.get(0).toString());
        JSONObject objSubBase = jobj2.getJSONObject("body").getJSONObject("主体信息").getJSONObject("主体基本信息");
        JSONObject objEnterpriseSub = jobj2.getJSONObject("body").getJSONObject("主体信息").getJSONObject("机构主体信息");

        Map getSubjectInfo = new HashMap();
        getSubjectInfo.put("对象标识",jobj2.getJSONObject("body").getJSONObject("对象信息").getString("对象标识"));
        getSubjectInfo.put("主体标识",objSubBase.getJSONObject("主体通用信息").getString("主体标识"));
        getSubjectInfo.put("行业主体代号",objSubBase.getJSONObject("主体通用信息").getString("行业主体代号"));
        getSubjectInfo.put("主体类型",objSubBase.getJSONObject("主体通用信息").getIntValue("主体类型"));
        getSubjectInfo.put("主体信息创建时间",objSubBase.getJSONObject("主体通用信息").getString("主体信息创建时间"));
        List<Map> list = JSONObject.parseArray(objSubBase.getJSONArray("主体资质信息").toJSONString(), Map.class);
        getSubjectInfo.put("主体资质信息",list);

        getSubjectInfo.put("机构类型",objEnterpriseSub.getJSONObject("机构分类信息").getIntValue("机构类型"));
        getSubjectInfo.put("机构性质",objEnterpriseSub.getJSONObject("机构分类信息").getIntValue("机构性质"));

        getSubjectInfo.put("公司全称",objEnterpriseSub.getJSONObject("企业基本信息").getString("公司全称"));
        getSubjectInfo.put("英文名称",objEnterpriseSub.getJSONObject("企业基本信息").getString("英文名称"));
        getSubjectInfo.put("公司简称",objEnterpriseSub.getJSONObject("企业基本信息").getString("公司简称"));
        getSubjectInfo.put("英文简称",objEnterpriseSub.getJSONObject("企业基本信息").getString("英文简称"));
        getSubjectInfo.put("企业类型",objEnterpriseSub.getJSONObject("企业基本信息").getIntValue("企业类型"));
        getSubjectInfo.put("企业成分",objEnterpriseSub.getJSONObject("企业基本信息").getIntValue("企业成分"));
        getSubjectInfo.put("统一社会信用代码",objEnterpriseSub.getJSONObject("企业基本信息").getString("统一社会信用代码"));
        getSubjectInfo.put("组织机构代码",objEnterpriseSub.getJSONObject("企业基本信息").getString("组织机构代码"));
        getSubjectInfo.put("设立日期",objEnterpriseSub.getJSONObject("企业基本信息").getString("设立日期"));
        getSubjectInfo.put("营业执照",objEnterpriseSub.getJSONObject("企业基本信息").getString("营业执照"));
        getSubjectInfo.put("经营范围",objEnterpriseSub.getJSONObject("企业基本信息").getString("经营范围"));
        getSubjectInfo.put("企业所属行业",objEnterpriseSub.getJSONObject("企业基本信息").getIntValue("企业所属行业"));
        getSubjectInfo.put("主营业务",objEnterpriseSub.getJSONObject("企业基本信息").getString("主营业务"));
        getSubjectInfo.put("公司简介",objEnterpriseSub.getJSONObject("企业基本信息").getString("公司简介"));
        getSubjectInfo.put("注册资本",objEnterpriseSub.getJSONObject("企业基本信息").getString("注册资本"));
        getSubjectInfo.put("注册资本币种",objEnterpriseSub.getJSONObject("企业基本信息").getIntValue("注册资本币种"));
        getSubjectInfo.put("实收资本",objEnterpriseSub.getJSONObject("企业基本信息").getString("实收资本"));
        getSubjectInfo.put("实收资本币种",objEnterpriseSub.getJSONObject("企业基本信息").getIntValue("实收资本币种"));
        getSubjectInfo.put("注册地址",objEnterpriseSub.getJSONObject("企业基本信息").getString("注册地址"));
        getSubjectInfo.put("办公地址",objEnterpriseSub.getJSONObject("企业基本信息").getString("办公地址"));
        getSubjectInfo.put("联系地址",objEnterpriseSub.getJSONObject("企业基本信息").getString("联系地址"));
        getSubjectInfo.put("联系电话",objEnterpriseSub.getJSONObject("企业基本信息").getString("联系电话"));
        getSubjectInfo.put("传真",objEnterpriseSub.getJSONObject("企业基本信息").getString("传真"));
        getSubjectInfo.put("邮政编码",objEnterpriseSub.getJSONObject("企业基本信息").getString("邮政编码"));
        getSubjectInfo.put("互联网地址",objEnterpriseSub.getJSONObject("企业基本信息").getString("互联网地址"));
        getSubjectInfo.put("电子邮箱",objEnterpriseSub.getJSONObject("企业基本信息").getString("电子邮箱"));
        getSubjectInfo.put("公司章程",objEnterpriseSub.getJSONObject("企业基本信息").getString("公司章程"));
        getSubjectInfo.put("主管单位",objEnterpriseSub.getJSONObject("企业基本信息").getString("主管单位"));
        getSubjectInfo.put("股东总数（个）",objEnterpriseSub.getJSONObject("企业基本信息").getIntValue("股东总数（个）"));
        getSubjectInfo.put("股本总数(股)",objEnterpriseSub.getJSONObject("企业基本信息").getString("股本总数(股)"));
        getSubjectInfo.put("法定代表人姓名",objEnterpriseSub.getJSONObject("法人信息").getString("法定代表人姓名"));
        getSubjectInfo.put("法人性质",objEnterpriseSub.getJSONObject("法人信息").getIntValue("法人性质"));
        getSubjectInfo.put("法定代表人身份证件类型",objEnterpriseSub.getJSONObject("法人信息").getIntValue("法定代表人身份证件类型"));
        getSubjectInfo.put("法定代表人身份证件号码",objEnterpriseSub.getJSONObject("法人信息").getString("法定代表人身份证件号码"));
        getSubjectInfo.put("法定代表人职务",objEnterpriseSub.getJSONObject("法人信息").getIntValue("法定代表人职务"));
        getSubjectInfo.put("法定代表人手机号",objEnterpriseSub.getJSONObject("法人信息").getString("法定代表人手机号"));

        return getSubjectInfo;
    }


    public Map contructEquityProdInfo(String prodTxId){
        JSONObject object2 = JSONObject.parseObject(store.GetTxDetail(prodTxId));
        String storeData2 = object2.getJSONObject("data").getJSONObject("store").getString("storeData");
        JSONArray jsonArray2 = JSONArray.parseArray(storeData2);
        JSONObject jobj2 = JSONObject.parseObject(jsonArray2.get(0).toString());
        JSONObject objProdBase = jobj2.getJSONObject("body").getJSONObject("产品信息").getJSONObject("产品基本信息");
        JSONObject objProdIssue = jobj2.getJSONObject("body").getJSONObject("产品信息").getJSONObject("产品发行信息");

        Map getSubjectInfo = new HashMap();

        getSubjectInfo.put("产品对象标识",jobj2.getJSONObject("body").getJSONObject("对象标识").getString("产品对象标识"));
        getSubjectInfo.put("发行主体引用",objProdBase.getString("发行主体引用"));
        getSubjectInfo.put("发行主体名称",objProdBase.getString("发行主体名称"));
        getSubjectInfo.put("登记机构主体引用",objProdBase.getString("登记机构主体引用"));
        getSubjectInfo.put("托管机构主体引用",objProdBase.getString("托管机构主体引用"));
        getSubjectInfo.put("产品代码",objProdBase.getString("产品代码"));
        getSubjectInfo.put("产品全称",objProdBase.getString("产品全称"));
        getSubjectInfo.put("产品简称",objProdBase.getString("产品简称"));
        getSubjectInfo.put("产品类型",objProdBase.getString("产品类型"));
        getSubjectInfo.put("最大账户数量",objProdBase.getString("最大账户数量"));
        getSubjectInfo.put("信息披露方式",objProdBase.getString("信息披露方式"));
        getSubjectInfo.put("产品规模单位",objProdBase.getString("产品规模单位"));
        getSubjectInfo.put("产品规模币种",objProdBase.getString("产品规模币种"));
        getSubjectInfo.put("产品规模总额",objProdBase.getString("产品规模总额"));
        getSubjectInfo.put("浏览范围",objProdBase.getString("浏览范围"));
        getSubjectInfo.put("交易范围",objProdBase.getString("交易范围"));

        getSubjectInfo.put("承销机构主体引用",objProdIssue.getJSONObject("发行服务方信息").getString("承销机构主体引用"));
        getSubjectInfo.put("承销机构名称",objProdIssue.getJSONObject("发行服务方信息").getString("承销机构名称"));
        getSubjectInfo.put("律师事务所主体引用",objProdIssue.getJSONObject("发行服务方信息").getString("律师事务所主体引用"));
        getSubjectInfo.put("律师事务所名称",objProdIssue.getJSONObject("发行服务方信息").getString("律师事务所名称"));
        getSubjectInfo.put("会计事务所主体引用",objProdIssue.getJSONObject("发行服务方信息").getString("会计事务所主体引用"));
        getSubjectInfo.put("会计事务所名称",objProdIssue.getJSONObject("发行服务方信息").getString("会计事务所名称"));

        getSubjectInfo.put("发行方联系人",objProdIssue.getJSONObject("联系信息").getString("发行方联系人"));
        getSubjectInfo.put("发行方联系信息",objProdIssue.getJSONObject("联系信息").getString("发行方联系信息"));

        getSubjectInfo.put("股权类-发行增资信息",JSONObject.parseArray(objProdIssue.getJSONArray("股权类-发行增资信息").toJSONString(), Map.class));

        return getSubjectInfo;
    }

}
