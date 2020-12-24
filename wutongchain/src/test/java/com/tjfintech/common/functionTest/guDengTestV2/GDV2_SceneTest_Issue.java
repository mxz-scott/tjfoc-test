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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.functionTest.guDengTestV2.GDCommonFunc.*;
import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassGD.*;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class GDV2_SceneTest_Issue {

    TestBuilder testBuilder= TestBuilder.getInstance();
    GuDeng gd =testBuilder.getGuDeng();
    Store store =testBuilder.getStore();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    public static String bizNoTest = "test" + Random(12);
    GDUnitFunc uf = new GDUnitFunc();
    String typeProduct = "1";

    @Rule
    public TestName tm = new TestName();

    @BeforeClass
    public static void Before()throws Exception{
        GDBeforeCondition gdBefore = new GDBeforeCondition();
        gdBefore.gdCreateAccout();
    }

    @Before
    public void IssueEquity()throws Exception{
        bizNoTest = "test" + Random(12);
        gdCompanyID = CNKey + "Sub2_" + Random(4);
        gdEquityCode = CNKey + "Token2_" + Random(4);

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
        uf.commonIssuePP01(1000);//发行给账户1~4 股权性质对应 0 1 0 1
    }

    @After
    public void calJGDataAfterTx()throws Exception{
        testCurMethodName = tm.getMethodName();
        GDUnitFunc uf = new GDUnitFunc();
        int endHeight = net.sf.json.JSONObject.fromObject(store.GetHeight()).getInt("data");
        uf.checkJGHeaderOpVer(blockHeight,endHeight);
        uf.updateBlockHeightParam(endHeight);
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
     * 重复发行同一个股权代码
     */

    @Test
    public void issueSameEquityCode()throws Exception{
        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,1, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,1, shareList3);

        //发行已经发行过的股权代码
        String response = uf.shareIssue(gdEquityCode,shareList4,false);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("该股权代码已经初始登记过，不可以再次调用",JSONObject.fromObject(response).getString("message"));

    }

    /***
     * 发行不同股权代码性质的股权 多个股东
     */

    @Test
    public void issueDiffPropertyEquityCode()throws Exception{
        gdEquityCode = "gdEC" + Random(13);
        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        shareList = gdConstructShareList(gdAccount2,1000,1,shareList);
        shareList = gdConstructShareList(gdAccount3,1000,2,shareList);
        shareList = gdConstructShareList(gdAccount4,1000,3,shareList);
        shareList = gdConstructShareList(gdAccount5,1000,4,shareList);
        shareList = gdConstructShareList(gdAccount6,1000,5,shareList);
        shareList = gdConstructShareList(gdAccount1,1000,6,shareList);
        shareList = gdConstructShareList(gdAccount5,1000,21,shareList);
        shareList = gdConstructShareList(gdAccount6,1000,32,shareList);
        shareList = gdConstructShareList(gdAccount1,1000,43,shareList);
        shareList = gdConstructShareList(gdAccount5,1000,88,shareList);
        shareList = gdConstructShareList(gdAccount6,1000,99,shareList);


        //发行已经发行过的股权代码
        String response = uf.shareIssue(gdEquityCode,shareList,true);


        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");

        JSONArray jsonArrayGet = JSONObject.fromObject(query).getJSONArray("data");
        assertEquals("12000",getTotalAmountFromShareList(jsonArrayGet));

        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,1000,0,0,mapShareENCN().get("0"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount1,1000,6,0,mapShareENCN().get("6"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount1,1000,43,0,mapShareENCN().get("43"),respShareList);
//        respShareList = gdConstructQueryShareList(gdAccount1,1000,100,0,mapShareENCN().get("0"),respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2,1000,1,0,mapShareENCN().get("1"), respShareList);
//        respShareList2 = gdConstructQueryShareList(gdAccount2,1000,7,0,mapShareENCN().get("0"), respShareList2);
//        respShareList2 = gdConstructQueryShareList(gdAccount2,1000,54,0,mapShareENCN().get("0"), respShareList2);
//        respShareList2 = gdConstructQueryShareList(gdAccount2,1000,101,0,mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3,1000,2,0,mapShareENCN().get("2"), respShareList2);
//        respShareList3 = gdConstructQueryShareList(gdAccount3,1000,8,0,mapShareENCN().get("0"), respShareList3);
//        respShareList3 = gdConstructQueryShareList(gdAccount3,1000,65,0,mapShareENCN().get("0"), respShareList3);
//        respShareList3 = gdConstructQueryShareList(gdAccount3,1000,156,0,mapShareENCN().get("0"), respShareList3);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4,1000,3,0,mapShareENCN().get("3"), respShareList3);
//        respShareList4 = gdConstructQueryShareList(gdAccount4,1000,10,0,mapShareENCN().get("0"), respShareList4);
//        respShareList4 = gdConstructQueryShareList(gdAccount4,1000,76,0,mapShareENCN().get("0"), respShareList4);
        List<Map> respShareList5 = gdConstructQueryShareList(gdAccount5,1000,4,0,mapShareENCN().get("4"), respShareList4);
        respShareList5 = gdConstructQueryShareList(gdAccount5,1000,21,0,mapShareENCN().get("21"), respShareList5);
        respShareList5 = gdConstructQueryShareList(gdAccount5,1000,88,0,"", respShareList5);
        List<Map> respShareList6 = gdConstructQueryShareList(gdAccount6,1000,5,0,mapShareENCN().get("5"), respShareList5);
        respShareList6 = gdConstructQueryShareList(gdAccount6,1000,32,0,mapShareENCN().get("32"), respShareList6);
        respShareList6 = gdConstructQueryShareList(gdAccount6,1000,99,0,mapShareENCN().get("99"), respShareList6);

        log.info(respShareList6.toString());
        //检查存在余额的股东列表
        assertEquals(respShareList6.size(),dataShareList.size());

        List<Map> getShareList = getShareListFromQueryNoZeroAcc(dataShareList);

        assertEquals(respShareList6.size(),getShareList.size());
        assertEquals(true,respShareList6.containsAll(getShareList) && getShareList.containsAll(respShareList6));


        //查询股东持股情况 无当前股权代码信息
        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo3);
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":2,\"sharePropertyCN\":\"" + mapShareENCN().get("2") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo4);
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":3,\"sharePropertyCN\":\"" + mapShareENCN().get("3") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo5);
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":4,\"sharePropertyCN\":\"" + mapShareENCN().get("4") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo6);
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":5,\"sharePropertyCN\":\"" + mapShareENCN().get("5") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo7);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

    }



    /***
     * 发行不同股权代码性质的股权 一个股东
     */

    @Test
    public void issueOneHolderDiffPropertyEquityCode()throws Exception{
        gdEquityCode = "gdEC" + Random(13);
        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        shareList = gdConstructShareList(gdAccount1,1000,1,shareList);
        shareList = gdConstructShareList(gdAccount1,1000,2,shareList);
        shareList = gdConstructShareList(gdAccount1,1000,3,shareList);
        shareList = gdConstructShareList(gdAccount1,1000,4,shareList);
        shareList = gdConstructShareList(gdAccount1,1000,5,shareList);
        shareList = gdConstructShareList(gdAccount1,1000,6,shareList);
        shareList = gdConstructShareList(gdAccount1,1000,7,shareList);
        shareList = gdConstructShareList(gdAccount1,1000,8,shareList);
        shareList = gdConstructShareList(gdAccount1,1000,10,shareList);
        shareList = gdConstructShareList(gdAccount1,1000,21,shareList);
        shareList = gdConstructShareList(gdAccount1,1000,32,shareList);
        shareList = gdConstructShareList(gdAccount1,1000,43,shareList);
        shareList = gdConstructShareList(gdAccount1,1000,54,shareList);
        shareList = gdConstructShareList(gdAccount1,1000,65,shareList);

        //发行已经发行过的股权代码
        String response = uf.shareIssue(gdEquityCode,shareList,true);


        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");

        JSONArray jsonArrayGet = JSONObject.fromObject(query).getJSONArray("data");
        assertEquals("15000",getTotalAmountFromShareList(jsonArrayGet));

        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,1000,0,0,mapShareENCN().get("0"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount1,1000,6,0,mapShareENCN().get("6"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount1,1000,43,0,mapShareENCN().get("43"),respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount1,1000,1,0,mapShareENCN().get("1"), respShareList);
        respShareList2 = gdConstructQueryShareList(gdAccount1,1000,7,0,"", respShareList2);
        respShareList2 = gdConstructQueryShareList(gdAccount1,1000,54,0,mapShareENCN().get("54"), respShareList2);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount1,1000,2,0,mapShareENCN().get("2"), respShareList2);
        respShareList3 = gdConstructQueryShareList(gdAccount1,1000,8,0,"", respShareList3);
        respShareList3 = gdConstructQueryShareList(gdAccount1,1000,65,0,mapShareENCN().get("65"), respShareList3);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount1,1000,3,0,mapShareENCN().get("3"), respShareList3);
        respShareList4 = gdConstructQueryShareList(gdAccount1,1000,10,0,"", respShareList4);
        List<Map> respShareList5 = gdConstructQueryShareList(gdAccount1,1000,4,0,mapShareENCN().get("4"), respShareList4);
        respShareList5 = gdConstructQueryShareList(gdAccount1,1000,21,0,mapShareENCN().get("21"), respShareList5);
        List<Map> respShareList6 = gdConstructQueryShareList(gdAccount1,1000,5,0,mapShareENCN().get("5"), respShareList5);
        respShareList6 = gdConstructQueryShareList(gdAccount1,1000,32,0,mapShareENCN().get("32"), respShareList6);

        log.info(respShareList6.toString());
        //检查存在余额的股东列表
        assertEquals(respShareList6.size(),dataShareList.size());

        List<Map> getShareList = getShareListFromQueryNoZeroAcc(dataShareList);

        assertEquals(respShareList6.size(),getShareList.size());
        assertEquals(true,respShareList6.containsAll(getShareList) && getShareList.containsAll(respShareList6));


        //查询股东持股情况 无当前股权代码信息
        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":2,\"sharePropertyCN\":\"" + mapShareENCN().get("2") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":3,\"sharePropertyCN\":\"" + mapShareENCN().get("3") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":4,\"sharePropertyCN\":\"" + mapShareENCN().get("4") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":5,\"sharePropertyCN\":\"" + mapShareENCN().get("5") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

    }


    /***
     * 股权代码大小写敏感性检查
     */

    @Test
    public void issue_MatchCase()throws Exception{

        String query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        assertEquals(false,query.contains(gdEquityCode.toLowerCase()));
        assertEquals(false,query.contains(gdEquityCode.toUpperCase()));

        query = gd.GDGetEnterpriseShareInfo(gdEquityCode.toLowerCase());
        assertEquals("400",JSONObject.fromObject(query).getString("state"));

        query = gd.GDGetEnterpriseShareInfo(gdEquityCode.toUpperCase());
        assertEquals("400",JSONObject.fromObject(query).getString("state"));

        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,1, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,1, shareList3);

        //大小写匹配检查
        uf.shareIssue(gdEquityCode.toLowerCase(),shareList4,true);
        uf.shareIssue(gdEquityCode.toUpperCase(),shareList4,true);

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        assertEquals(true,query.contains(gdEquityCode.toLowerCase()));
        assertEquals(true,query.contains(gdEquityCode.toUpperCase()));

        query = gd.GDGetEnterpriseShareInfo(gdEquityCode.toLowerCase());
        assertEquals("200",JSONObject.fromObject(query).getString("state"));

        query = gd.GDGetEnterpriseShareInfo(gdEquityCode.toUpperCase());
        assertEquals("200",JSONObject.fromObject(query).getString("state"));

    }


    /***
     * 股权代码包含特殊字符
     */

    @Test
    public void issueWithSpecialChar_TC2519()throws Exception{

        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);

        //股权代码为特殊字符
        String EC1 = "@" + Random(6);
        String EC2 = "%" + Random(6);
        String EC3 = "#" + Random(6);
        String EC4 = "_" + Random(6);
        String EC5 = "|" + Random(6);
        String EC6 = "^" + Random(6);

        uf.shareIssue(EC1,shareList,true);
        uf.shareIssue(EC2,shareList,true);
        uf.shareIssue(EC3,shareList,true);
        uf.shareIssue(EC4,shareList,true);
        uf.shareIssue(EC5,shareList,true);
        uf.shareIssue(EC6,shareList,true);

        String query = gd.GDGetEnterpriseShareInfo(EC1);
        assertEquals(1,JSONObject.fromObject(query).getJSONArray("data").size());

        query = gd.GDGetEnterpriseShareInfo(EC2);
        assertEquals(1,JSONObject.fromObject(query).getJSONArray("data").size());

        query = gd.GDGetEnterpriseShareInfo(EC3);
        assertEquals(1,JSONObject.fromObject(query).getJSONArray("data").size());

        query = gd.GDGetEnterpriseShareInfo(EC4);
        assertEquals(1,JSONObject.fromObject(query).getJSONArray("data").size());

        query = gd.GDGetEnterpriseShareInfo(EC5);
        assertEquals(1,JSONObject.fromObject(query).getJSONArray("data").size());

        query = gd.GDGetEnterpriseShareInfo(EC6);
        assertEquals(1,JSONObject.fromObject(query).getJSONArray("data").size());
    }

    /***
     * 最小登记额度1测试
     */

    @Test
    public void issueMin_TC2492()throws Exception{

        List<Map> shareList = gdConstructShareList(gdAccount1,1,0);
        shareList = gdConstructShareList(gdAccount1,1,1,shareList);

        //股权代码为特殊字符
        String EC1 = "gdEC" + Random(6);

        uf.shareIssue(EC1,shareList,true);

        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(EC1);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));
        assertEquals(2,JSONObject.fromObject(query).getJSONArray("data").size());
        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");

        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,1,0,0,mapShareENCN().get("0"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount1,1,1,0,mapShareENCN().get("1"),respShareList);

        log.info(respShareList.toString());
        //检查存在余额的股东列表
        assertEquals(respShareList.size(),dataShareList.size());

        List<Map> getShareList = getShareListFromQueryNoZeroAcc(dataShareList);

        assertEquals(respShareList.size(),getShareList.size());
        assertEquals(true,respShareList.containsAll(getShareList) && getShareList.containsAll(respShareList));


        //查询股东持股情况 无当前股权代码信息
        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        assertEquals(gdAccClientNo1,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));

        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo1 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount1 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + EC1 +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1,\"lockAmount\":0}"));
        assertEquals(true,query.contains("{\"equityCode\":\"" + EC1 +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":1,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(false,query.contains("\"equityCode\": \"" + EC1 + "\""));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo3);
        assertEquals(false,query.contains("\"equityCode\": \"" + EC1 + "\""));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo4);
        assertEquals(false,query.contains("\"equityCode\": \"" + EC1 + "\""));
//        assertEquals("未查到任何信息",JSONObject.fromObject(query).getString("message"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo5);
        assertEquals(false,query.contains("\"equityCode\": \"" + EC1 + "\""));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo6);
        assertEquals(false,query.contains("\"equityCode\": \"" + EC1 + "\""));
    }

}
