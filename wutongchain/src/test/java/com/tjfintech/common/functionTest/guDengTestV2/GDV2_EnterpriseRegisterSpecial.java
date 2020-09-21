package com.tjfintech.common.functionTest.guDengTestV2;

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
import org.springframework.util.StringUtils;
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
    //产品主体引用置为空
    @Test
    public void TC01_enterpriseRegister() throws Exception {
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
    public void TC02_enterpriseRegister() throws Exception {
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
        Map fundOk = accountInfo;
        accountInfo.put("账户所属主体引用","");
        accountInfo.put("关联账户对象引用","");


        String cltNo = "tet00" + Random(12);
        String shareHolderNo = "SH" + cltNo;
        String fundNo = "fund" + cltNo;

        //构造股权账户信息
        Map shareHolderInfo = new HashMap();
        accountInfo.put("账户对象标识",cltNo);  //更新账户对象标识字段
        shareHolderInfo.put("shareholderNo",shareHolderNo);
        shareHolderInfo.put("accountInfo",accountInfo);

        log.info(shareHolderInfo.toString());

        //构造资金账户信息
        Map accountFundInfo = accountInfo;
        accountFundInfo.put("账户类型",1);
        Map mapFundInfo = new HashMap();
        mapFundInfo.put("fundNo",fundNo);
        mapFundInfo.put("accountInfo",accountFundInfo);

        //构造个人/投资者主体信息
        gdBC.init01PersonalSubjectInfo();
        investorSubjectInfo.put("对象标识",cltNo);  //更新对象标识字段
        investorSubjectInfo.put("主体标识","sid" + cltNo);  //更新主体标识字段

        String response = gd.GDCreateAccout(gdContractAddress,cltNo,mapFundInfo,shareHolderInfo, investorSubjectInfo);
        commonFunc.sdkCheckTxOrSleep(net.sf.json.JSONObject.fromObject(response).getJSONObject("data").getString("txId"),utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //检查传空的内容是否自动传主体的对象标识
        String check = store.GetTxDetail(net.sf.json.JSONObject.fromObject(response).getJSONObject("data").getString("txId"));
        assertEquals("200",net.sf.json.JSONObject.fromObject(check).getString("state"));
        log.info(investorSubjectInfo.get("对象标识").toString());

        String questInfo = net.sf.json.JSONObject.fromObject(check).getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getJSONObject("arg").getJSONArray("args").get(0).toString();

        JSONObject jsonObject = JSONObject.parseObject(questInfo);
        log.info(jsonObject.getString("ClientNo"));
        log.info(jsonObject.getJSONObject("Investor").getString("对象标识"));
        String accInfoList = jsonObject.getJSONArray("AccountInfoList").get(0).toString();
        JSONObject jsonObjectAcc = JSONObject.parseObject(accInfoList);
        //检查账户所属主体引用 是否使用了investor的对象标识
        assertEquals(jsonObject.getJSONObject("Investor").getString("对象标识"),
                jsonObjectAcc.getJSONObject("FundAccount").getJSONObject("AccountInfo").getString("账户所属主体引用"));
        assertEquals(jsonObject.getJSONObject("Investor").getString("对象标识"),
                jsonObjectAcc.getJSONObject("FundAccount").getJSONObject("AccountInfo").getString("关联账户对象引用"));
        assertEquals(jsonObject.getJSONObject("Investor").getString("对象标识"),
                jsonObjectAcc.getJSONObject("ShareholderAccount").getJSONObject("AccountInfo").getString("账户所属主体引用"));
//        assertEquals(jsonObject.getJSONObject("Investor").getString("对象标识"),
//                jsonObjectAcc.getJSONObject("ShareholderAccount").getJSONObject("AccountInfo").getString("关联账户对象引用"));

    }

}
