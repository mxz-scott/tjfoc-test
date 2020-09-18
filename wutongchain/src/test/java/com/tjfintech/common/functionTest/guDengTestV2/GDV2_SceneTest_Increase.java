package com.tjfintech.common.functionTest.guDengTestV2;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.GDBeforeCondition;
import com.tjfintech.common.Interface.GuDeng;
import com.tjfintech.common.Interface.GuDengV1;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.functionTest.guDengTest.GDUnitFuncV1;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.CommonFunc.*;
import static com.tjfintech.common.utils.UtilsClass.*;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class GDV2_SceneTest_Increase {

    TestBuilder testBuilder= TestBuilder.getInstance();
    GuDeng gd =testBuilder.getGuDeng();
    Store store =testBuilder.getStore();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    public static String bizNoTest = "test" + Random(12);
    GDUnitFunc uf = new GDUnitFunc();

    @BeforeClass
    public static void Before()throws Exception{
        GDBeforeCondition gdBefore = new GDBeforeCondition();
        gdBefore.gdCreateAccout();
    }

    @Before
    public void IssueEquity()throws Exception{
        bizNoTest = "test" + Random(12);

        //重新创建账户
//        gdAccClientNo1 = "No000" + Random(10);
//        gdAccClientNo2 = "No100" + Random(10);
//        gdAccClientNo3 = "No200" + Random(10);
//        gdAccClientNo4 = "No300" + Random(10);
//        gdAccClientNo5 = "No400" + Random(10);
//        gdAccClientNo6 = "No500" + Random(10);
//        gdAccClientNo7 = "No600" + Random(10);
//        gdAccClientNo8 = "No700" + Random(10);
//        gdAccClientNo9 = "No800" + Random(10);
//       gdAccClientNo10 = "No900" + Random(10);
//
//       GDBeforeCondition gdBC = new GDBeforeCondition();
//       gdBC.gdCreateAccout();

//       sleepAndSaveInfo(3000);

        //发行
        gdEquityCode = "gdEC" + Random(12);
        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,1, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,1, shareList3);

        //发行
        gdEquityCode = "gdEC" + Random(12);
        uf.shareIssue(gdEquityCode,shareList4,true);
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
    }

//    @After
    public void DestroyEquityAndAcc()throws Exception{
        //查询企业所有股东持股情况
        String response = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        String response10 = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        String response11 = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        String response12 = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo3);
        String response13 = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo4);
        String response14 = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo5);
        String response15 = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo6);
        String response16 = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo7);
        String response17 = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo8);
        String response18 = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo9);
        String response19 = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo10);



        //依次回收

        //依次销户

    }


    /***
     * 增发 一个不存在的股权代码
     */

    @Test
    public void IncreaseNotExistEquitycode()throws Exception{
        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,0, shareList3);

        String response = uf.shareIncrease(gdEquityCode + Random(12),shareList4,false);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));
        assertEquals("该股份从未发行过，不可以增发", JSONObject.fromObject(response).getString("message"));

    }


    /***
     * 增发 shareList异常测试
     */

    @Test
    public void Increase_TC2391()throws Exception{
        List<Map> shareList = gdConstructShareList(gdAccount1,-100,0);
        List<Map> shareList2 = gdConstructShareList("",1000,0);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0,shareList);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,0, shareList2);

        String response = uf.shareIncrease(gdEquityCode,shareList3,false);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:json: cannot unmarshal number -100 into Go struct field Shares.ShareList.Amount of type uint64", JSONObject.fromObject(response).getString("message"));


        response = uf.shareIncrease(gdEquityCode,shareList4,false);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, JSONObject.fromObject(response).getString("message").contains("Error:Field validation for 'Address' failed on the 'required' tag"));

        shareList.clear();
        response = uf.shareIncrease(gdEquityCode,shareList,false);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));
        assertEquals("请填写股权账号信息", JSONObject.fromObject(response).getString("message"));

    }

    /***
     * 冻结后 增发
     */

    @Test
    public void IncreaseAfterLock()throws Exception{

        //冻结高管股 * 100 流通股*100
        String bizNoTemp = "2000" + Random(12);
        uf.lock(bizNoTemp,gdAccount1,gdEquityCode,100,0,"2022-09-03",true);

        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);

        bizNoTemp = "2000" + Random(12);
        uf.lock(bizNoTemp,gdAccount2,gdEquityCode,100,1,"2022-09-03",true);

        query = gd.GDGetEnterpriseShareInfo(gdEquityCode);

        bizNoTemp = "2000" + Random(12);
        uf.lock(bizNoTemp,gdAccount3,gdEquityCode,100,0,"2022-09-03",true);

        query = gd.GDGetEnterpriseShareInfo(gdEquityCode);

        bizNoTemp = "2000" + Random(12);
        uf.lock(bizNoTemp,gdAccount4,gdEquityCode,100,1,"2022-09-03",true);

        query = gd.GDGetEnterpriseShareInfo(gdEquityCode);

        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,0, shareList3);

        String response = uf.shareIncrease(gdEquityCode ,shareList4,true);

        query = gd.GDGetEnterpriseShareInfo(gdEquityCode);

    }

    /***
     * 连续两次增发
     */

    @Test
    public void increaseDouble_01()throws Exception{
        List<Map> shareListIn = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareListIn2 = gdConstructShareList(gdAccount2,1000,1, shareListIn);
        List<Map> shareListIn3 = gdConstructShareList(gdAccount3,1000,0, shareListIn2);
        List<Map> shareListIn4 = gdConstructShareList(gdAccount4,1000,1, shareListIn3);

        String response1 = uf.shareIncrease(gdEquityCode,shareListIn4,false);
        String response2 = uf.shareIncrease(gdEquityCode,shareListIn4,false);
        String txId1 = JSONObject.fromObject(response1).getJSONObject("data").getString("txId");
        String txId2 = JSONObject.fromObject(response2).getJSONObject("data").getString("txId");

        sleepAndSaveInfo(SLEEPTIME);

        //判断两笔交易均上链
        assertEquals(true,JSONObject.fromObject(store.GetTxDetail(txId1)).getString("state").equals("200"));
        assertEquals(true,JSONObject.fromObject(store.GetTxDetail(txId2)).getString("state").equals("200"));

        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        JSONArray jsonArrayGet = JSONObject.fromObject(query).getJSONArray("data");

        assertEquals(12000,getTotalAmountFromShareList(jsonArrayGet),0.0001);
    }


    /***
     * 股权代码大小写敏感性检查
     */

    @Test
    public void recycle_MatchCase()throws Exception{
        List<Map> shareListIn = gdConstructShareList(gdAccount1,1000,0);

        //大小写匹配检查
        String response = uf.shareIncrease(gdEquityCode.toLowerCase(),shareListIn,false);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("股权代码还未发行或者已经转场",JSONObject.fromObject(response).getString("message"));

        response = uf.shareIncrease(gdEquityCode.toUpperCase(),shareListIn,false);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("股权代码还未发行或者已经转场",JSONObject.fromObject(response).getString("message"));
    }


    /***
     * 多次增发
     */

    @Test
    public void increaseMultiTime()throws Exception{
        List<Map> shareListIn = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareListIn2 = gdConstructShareList(gdAccount2,1000,1, shareListIn);
        List<Map> shareListIn3 = gdConstructShareList(gdAccount3,1000,0, shareListIn2);
        List<Map> shareListIn4 = gdConstructShareList(gdAccount4,1000,1, shareListIn3);

        for(int i =0 ;i< 20;i++) {
            uf.shareIncrease(gdEquityCode, shareListIn4, false);
        }


        sleepAndSaveInfo(SLEEPTIME*2);

        String query = "";

        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");

        JSONArray jsonArrayGet = JSONObject.fromObject(query).getJSONArray("data");
        assertEquals(84000,getTotalAmountFromShareList(jsonArrayGet),0.0001);

        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,21000,0,0,mapShareENCN().get("0"),respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2,21000,1,0,mapShareENCN().get("1"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3,21000,0,0,mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4,21000,1,0,mapShareENCN().get("1"), respShareList3);

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
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":21000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(gdAccClientNo2,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo2 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount2 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":21000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo3);
        assertEquals(gdAccClientNo3,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo3 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount3 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":21000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo4);
        assertEquals(gdAccClientNo4,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo4 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount4 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":21000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo5);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo6);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

    }
    
}
