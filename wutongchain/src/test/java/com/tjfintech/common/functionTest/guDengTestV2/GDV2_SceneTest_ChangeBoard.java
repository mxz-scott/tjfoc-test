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
public class GDV2_SceneTest_ChangeBoard {

    TestBuilder testBuilder= TestBuilder.getInstance();
    GuDeng gd =testBuilder.getGuDeng();
    Store store =testBuilder.getStore();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    public static String bizNoTest = "test" + Random(12);
    GDUnitFunc uf = new GDUnitFunc();
    List<Map> regList = new ArrayList<>();

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
//        uf.calJGData();
        uf.calJGDataEachHeight();
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
     * 场内转板 双花测试
     */

    @Test
    public void changeBoardDoubleSpend_01()throws Exception{
        String newEqCode1 = gdEquityCode + Random(7);
        String newEqCode2 = gdEquityCode + Random(7);

        regList = uf.getAllHolderListReg(gdEquityCode,"cbSpec" + Random(10));
        String response1 = uf.changeBoard(gdEquityCode,newEqCode1,false);

        regList = uf.getAllHolderListReg(gdEquityCode,"cbSpec" + Random(10));
        String response2 = uf.changeBoard(gdEquityCode,newEqCode2,false);
        String txId1 = JSONObject.fromObject(response1).getJSONObject("data").getString("txId");
        String txId2 = JSONObject.fromObject(response2).getJSONObject("data").getString("txId");

        sleepAndSaveInfo(SLEEPTIME);

        //异或判断两种其中只有一个上链
        assertEquals(true,JSONObject.fromObject(store.GetTxDetail(txId1)).getString("state").equals("200")
                ^JSONObject.fromObject(store.GetTxDetail(txId2)).getString("state").equals("200"));

        gd.GDGetEnterpriseShareInfo(gdEquityCode);
    }


    /***
     * 冻结后场内转板  冻结非流通股*1
     */

    @Test
    public void changeBoard_AfterLock01()throws Exception{

        String response = "";
        //冻结高管股 * 1
        String bizNoTemp = "2000" + Random(12);
        uf.lock(bizNoTemp,gdAccount2,gdEquityCode,1,1,"2022-09-03",true);


        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");

        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,1000,0,0,mapShareENCN().get("0"),respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2,1000,1,1,mapShareENCN().get("1"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3,1000,0,0,mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4,1000,1,0,mapShareENCN().get("1"), respShareList3);

        //检查存在余额的股东列表
        assertEquals(respShareList4.size(),dataShareList.size());

        List<Map> getShareList = getShareListFromQueryNoZeroAcc(dataShareList);
        log.info(getShareList.toString());

        assertEquals(respShareList4.size(),getShareList.size());
        assertEquals(true,respShareList4.containsAll(getShareList) && getShareList.containsAll(respShareList4));


        //查询股东持股情况 无当前股权代码信息
        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(gdAccClientNo2,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));

        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo2 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount2 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":1000,\"lockAmount\":1}"));

        regList = uf.getAllHolderListReg(gdEquityCode,"cbSpec" + Random(10));
        //冻结后转板
        String newEquityCode = gdEquityCode + Random(5);
        response = uf.changeBoard(gdEquityCode,newEquityCode,false);

        if(JSONObject.fromObject(response).getString("state").equals("200")) {
            sleepAndSaveInfo(SLEEPTIME);
            assertEquals("404",JSONObject.fromObject(store.GetTxDetail(
                    JSONObject.fromObject(response).getJSONObject("data").getString("txId"))).getString("state"));
        }
        else{
            assertEquals("400",JSONObject.fromObject(response).getString("state"));
        }

        query  = gd.GDGetEnterpriseShareInfo(newEquityCode);
        assertEquals("400",JSONObject.fromObject(query).getString("state"));
        assertEquals("股权代码还未发行或者已经转场",JSONObject.fromObject(query).getString("message"));

        query  = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));
    }


    /***
     * 冻结后场内转板  冻结流通股*1
     */

    @Test
    public void changeBoard_AfterLock02()throws Exception{

        String response = "";
        //冻结高管股 * 1
        String bizNoTemp = "2000" + Random(12);
        uf.lock(bizNoTemp,gdAccount1,gdEquityCode,1,0,"2022-09-03",true);

        String newEquityCode = gdEquityCode + Random(5);
        response = uf.changeBoard(gdEquityCode,newEquityCode,false);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("账户地址[" + gdAccount1 + "]还存在冻结的资产，不可以转场",JSONObject.fromObject(response).getString("message"));


        String query  = gd.GDGetEnterpriseShareInfo(newEquityCode);
        assertEquals("400",JSONObject.fromObject(query).getString("state"));
        assertEquals("股权代码还未发行或者已经转场",JSONObject.fromObject(query).getString("message"));

        query  = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
    }


    /***
     * 全部冻结后场内转板 TC2526
     */

    @Test
    public void changeBoard_AllLock()throws Exception{

        String response = "";
        //冻结高管股 * 1
        String bizNoTemp = "2000" + Random(12);
        uf.lock(bizNoTemp,gdAccount1,gdEquityCode,1000,0,"2022-09-03",true);
        bizNoTemp = "2000" + Random(12);
        uf.lock(bizNoTemp,gdAccount1,gdEquityCode,1000,1,"2022-09-03",true);
        bizNoTemp = "2000" + Random(12);
        uf.lock(bizNoTemp,gdAccount1,gdEquityCode,1000,0,"2022-09-03",true);
        bizNoTemp = "2000" + Random(12);
        uf.lock(bizNoTemp,gdAccount1,gdEquityCode,1000,1,"2022-09-03",true);

        String newEquityCode = gdEquityCode + Random(5);
        response = uf.changeBoard(gdEquityCode,newEquityCode,false);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("账户地址[" + gdAccount1 + "]还存在冻结的资产，不可以转场",JSONObject.fromObject(response).getString("message"));


        String query  = gd.GDGetEnterpriseShareInfo(newEquityCode);
        assertEquals("400",JSONObject.fromObject(query).getString("state"));
        assertEquals("股权代码还未发行或者已经转场",JSONObject.fromObject(query).getString("message"));

        query  = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
    }


    /***
     * 转板后再转回去
     * 转板使用已存在的股权代码
     */

    @Test
    public void changeBoard_Back()throws Exception{

        String response = "";

        String oldEquityCode = gdEquityCode;
        String newEquityCode1 = gdEquityCode + Random(5);
        String newEquityCode2 = gdEquityCode + Random(6);

        //第一次转板
        response = uf.changeBoard(gdEquityCode,newEquityCode1,true);

        //第二次转板再转回old
        response = uf.changeBoard(newEquityCode1,oldEquityCode,false);

        if(JSONObject.fromObject(response).getString("state").equals("200")) {
            sleepAndSaveInfo(SLEEPTIME);
            assertEquals("404",JSONObject.fromObject(store.GetTxDetail(
                    JSONObject.fromObject(response).getJSONObject("data").getString("txId"))).getString("state"));
        }
        else{
            assertEquals("501",JSONObject.fromObject(response).getString("state"));
        }

        String query  = gd.GDGetEnterpriseShareInfo(oldEquityCode);
        assertEquals("400",JSONObject.fromObject(query).getString("state"));
        assertEquals("股权代码还未发行或者已经转场",JSONObject.fromObject(query).getString("message"));

        query  = gd.GDGetEnterpriseShareInfo(newEquityCode1);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));


        //第三次转板 new1->new2
        response = uf.changeBoard(newEquityCode1,newEquityCode2,true);

        query  = gd.GDGetEnterpriseShareInfo(newEquityCode1);
        assertEquals("400",JSONObject.fromObject(query).getString("state"));
        assertEquals("股权代码还未发行或者已经转场",JSONObject.fromObject(query).getString("message"));

        query  = gd.GDGetEnterpriseShareInfo(oldEquityCode);
        assertEquals("400",JSONObject.fromObject(query).getString("state"));
        assertEquals("股权代码还未发行或者已经转场",JSONObject.fromObject(query).getString("message"));

        query  = gd.GDGetEnterpriseShareInfo(newEquityCode2);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));


        //第四次转板再转回old
        response = uf.changeBoard(newEquityCode2,oldEquityCode,false);

        if(JSONObject.fromObject(response).getString("state").equals("200")) {
            sleepAndSaveInfo(SLEEPTIME);
            assertEquals("404",JSONObject.fromObject(store.GetTxDetail(
                    JSONObject.fromObject(response).getJSONObject("data").getString("txId"))).getString("state"));
        }
        else{
            assertEquals("501",JSONObject.fromObject(response).getString("state"));
        }

        query  = gd.GDGetEnterpriseShareInfo(newEquityCode1);
        assertEquals("400",JSONObject.fromObject(query).getString("state"));
        assertEquals("股权代码还未发行或者已经转场",JSONObject.fromObject(query).getString("message"));

        query  = gd.GDGetEnterpriseShareInfo(oldEquityCode);
        assertEquals("400",JSONObject.fromObject(query).getString("state"));
        assertEquals("股权代码还未发行或者已经转场",JSONObject.fromObject(query).getString("message"));

        query  = gd.GDGetEnterpriseShareInfo(newEquityCode2);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));
    }


    /***
     * 全部回收后转板
     */

    @Test
    public void changeBoard_AfterAllRecycle()throws Exception{

        String response = "";

        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,1, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,1, shareList3);

        uf.shareRecycle(gdEquityCode,shareList4,true);

        String query  = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));
        assertEquals(2,JSONObject.fromObject(query).getJSONArray("data").size());
//        assertEquals(zeroAccount,JSONObject.fromObject(JSONObject.fromObject(query).getJSONArray("data").get(0)).getString("address"));

        String newEquityCode = gdEquityCode + Random(5);
        response = uf.changeBoard(gdEquityCode,newEquityCode,false);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("未查到该股份的任何有效信息",JSONObject.fromObject(response).getString("message"));
    }


    /***
     * 多个股东 多种股权性质 全部回收后转板
     */

    @Test
    public void changeBoard_TC2527()throws Exception{

        String response = "";

        List<Map> listRegInfo = new ArrayList<>();
        listRegInfo.add(registerInfo);
        listRegInfo.add(registerInfo);
        //构造一个股东 多种股权性质
        uf.changeSHProperty(gdAccount1,gdEquityCode,500,0,2,true);
        uf.changeSHProperty(gdAccount2,gdEquityCode,500,1,0,true);
        uf.changeSHProperty(gdAccount3,gdEquityCode,500,0,3,true);
        uf.changeSHProperty(gdAccount4,gdEquityCode,500,1,4,true);

        List<Map> shareList = gdConstructShareList(gdAccount1,500,0);
        shareList = gdConstructShareList(gdAccount1,500,2,shareList);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,500,1, shareList);
        shareList2 = gdConstructShareList(gdAccount2,500,0, shareList2);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,500,0, shareList2);
        shareList3 = gdConstructShareList(gdAccount3,500,3, shareList3);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,500,1, shareList3);
        shareList4 = gdConstructShareList(gdAccount4,500,4, shareList4);

        uf.shareRecycle(gdEquityCode,shareList4,true);

        String query  = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));
        assertEquals(5,JSONObject.fromObject(query).getJSONArray("data").size());
        assertEquals(true,query.contains("{\"amount\":1500,\"lockAmount\":0,\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"address\":\"" + zeroAccount + "\"}"));
        assertEquals(true,query.contains("{\"amount\":1000,\"lockAmount\":0,\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"address\":\"" + zeroAccount + "\"}"));
        assertEquals(true,query.contains("{\"amount\":500,\"lockAmount\":0,\"shareProperty\":2,\"sharePropertyCN\":\"" + mapShareENCN().get("2") + "\",\"address\":\"" + zeroAccount + "\"}"));
        assertEquals(true,query.contains("{\"amount\":500,\"lockAmount\":0,\"shareProperty\":3,\"sharePropertyCN\":\"" + mapShareENCN().get("3") + "\",\"address\":\"" + zeroAccount + "\"}"));
        assertEquals(true,query.contains("{\"amount\":500,\"lockAmount\":0,\"shareProperty\":4,\"sharePropertyCN\":\"" + mapShareENCN().get("4") + "\",\"address\":\"" + zeroAccount + "\"}"));

        String newEquityCode = gdEquityCode + Random(5);
        response = uf.changeBoard(gdEquityCode,newEquityCode,false);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("未查到该股份的任何有效信息",JSONObject.fromObject(response).getString("message"));
    }

    /***
     * 部分回收后转板
     * 转板后股权代码查询时不包含转板前已回收的股权数量
     */

    @Test
    public void changeBoard_AfterPartRecycle()throws Exception{

        String response = "";
        //回收一半数额
        List<Map> shareList = gdConstructShareList(gdAccount1,500,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,500,1, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,500,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,500,1, shareList3);

        uf.shareRecycle(gdEquityCode,shareList4,true);

        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));
        assertEquals(6,JSONObject.fromObject(query).getJSONArray("data").size());

        String newEquityCode = gdEquityCode + Random(5);
        response = uf.changeBoard(gdEquityCode,newEquityCode,true);
         //转板后检查 无回收信息
        query = gd.GDGetEnterpriseShareInfo(newEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));
        assertEquals(4,JSONObject.fromObject(query).getJSONArray("data").size());
    }

    /***
     * 同一账户持有不同股权代码时 其中一个股权代码存在冻结，不影响其他股权代码转场
     */

    @Test
    public void changeBoard_lockMatchEqcode()throws Exception{
        String EqCode1 = gdEquityCode;
        String EqCode2 = gdEquityCode + Random(8);
        String EqCode3 = gdEquityCode + Random(8);
        String EqCode4 = gdEquityCode + Random(8);

        String response = "";
        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,1, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,1, shareList3);
        List<Map> shareList5 = gdConstructShareList(gdAccount5,1000,0);

        uf.shareIssue(EqCode2,shareList4,true);
        uf.shareIssue(EqCode3,shareList5,true);
        uf.shareIssue(EqCode4,shareList4,true);

        //冻结账户4 EqCode2 * 股权性质1 *100
        uf.lock(bizNoTest,gdAccount4,EqCode2,100,1,"2032-09-30",true);

        response = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        assertEquals(false,response.contains("{\"equityCode\":\"" + EqCode3 + "\",\"shareProperty\":0"));
        assertEquals(true,response.contains("{\"equityCode\":\"" + EqCode1 +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));
        assertEquals(true,response.contains("{\"equityCode\":\"" + EqCode2 +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

        response = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(false,response.contains("{\"equityCode\":\"" + EqCode3 + "\",\"shareProperty\":0"));
        assertEquals(true,response.contains("{\"equityCode\":\"" + EqCode1 +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));
        assertEquals(true,response.contains("{\"equityCode\":\"" + EqCode2 +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

        response = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo3);
        assertEquals(false,response.contains("{\"equityCode\":\"" + EqCode3 + "\",\"shareProperty\":0"));
        assertEquals(true,response.contains("{\"equityCode\":\"" + EqCode1 +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));
        assertEquals(true,response.contains("{\"equityCode\":\"" + EqCode2 +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

        response = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo4);
        assertEquals(false,response.contains("{\"equityCode\":\"" + EqCode3 + "\",\"shareProperty\":0"));
        assertEquals(true,response.contains("{\"equityCode\":\"" + EqCode1 +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));
        assertEquals(true,response.contains("{\"equityCode\":\"" + EqCode2 +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":1000,\"lockAmount\":100}"));

        response = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo5);
        assertEquals(false,response.contains("{\"equityCode\":\"" + EqCode1 + "\",\"shareProperty\":0"));
        assertEquals(false,response.contains("{\"equityCode\":\"" + EqCode2 + "\",\"shareProperty\":0"));
        assertEquals(false,response.contains("{\"equityCode\":\"" + EqCode1 + "\",\"shareProperty\":1"));
        assertEquals(false,response.contains("{\"equityCode\":\"" + EqCode2 + "\",\"shareProperty\":1"));
        assertEquals(true,response.contains("{\"equityCode\":\"" + EqCode3 +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

        uf.changeBoard(EqCode4,EqCode4 + Random(3),true);
        uf.changeBoard(EqCode1,EqCode1 + Random(3),true);
        uf.changeBoard(EqCode3,EqCode3 + Random(3),true);
    }


    /***
     * 股东持股为最小单位1
     */

    @Test
    public void changeBoard_TC2505()throws Exception{
        String EqCode1 = gdEquityCode + Random(8);
        String response = "";
        List<Map> shareList = gdConstructShareList(gdAccount1,1,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1,1, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1,1, shareList3);

        uf.shareIssue(EqCode1,shareList4,true);

        String newEqCode = gdEquityCode + Random(8);
        uf.changeBoard(EqCode1,newEqCode,true);
        gdEquityCode = newEqCode;

        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");

        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,1,0,0,mapShareENCN().get("0"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount2,1,1,0,mapShareENCN().get("1"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount3,1,0,0,mapShareENCN().get("0"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount4,1,1,0,mapShareENCN().get("1"),respShareList);

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
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(gdAccClientNo2,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo2 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount2 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":1,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo3);
        assertEquals(gdAccClientNo3,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo3 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount3 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo4);
        assertEquals(gdAccClientNo4,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo4 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount4 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":1,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo5);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo6);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));
    }

    /***
     * 股权代码大小写敏感性检查
     */

    @Test
    public void changeBoard_MatchCase()throws Exception{

        //转板 大小写匹配检查
        String response = uf.changeBoard(gdEquityCode.toLowerCase(),gdEquityCode + Random(6),false);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("股权代码还未发行或者已经转场",JSONObject.fromObject(response).getString("message"));

        response = uf.changeBoard(gdEquityCode.toUpperCase(),gdEquityCode + Random(6),false);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("股权代码还未发行或者已经转场",JSONObject.fromObject(response).getString("message"));
    }


    @Test
    public void changeBoard_TC2532() throws Exception{

        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,1, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,1, shareList3);

        uf.shareRecycle(gdEquityCode,shareList4,true);

        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals(true,query.contains("{\"amount\":2000,\"lockAmount\":0,\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"address\":\"" + zeroAccount + "\"}"));
        assertEquals(true,query.contains("{\"amount\":2000,\"lockAmount\":0,\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"address\":\"" + zeroAccount + "\"}"));
        assertEquals(false,query.contains(gdAccount1));
        assertEquals(false,query.contains(gdAccount4));

        String newEqCode1 = "gdEC" + Random(12);
        List<Map> shareList21 = gdConstructShareList(gdAccount1,1000,0);
        uf.shareIssue(newEqCode1,shareList21,true);

        String response = uf.changeBoard(newEqCode1,gdEquityCode,false);
        //链上报错
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
        sleepAndSaveInfo(SLEEPTIME);
        assertEquals("404",JSONObject.fromObject(
                store.GetTxDetail(JSONObject.fromObject(response).getJSONObject("data").getString("txId"))
        ).getString("state"));

    }


    @Test
    public void changeBoard_TC2529() throws Exception{

        String newEqCode1 = "gdEC" + Random(12);
        List<Map> shareList21 = gdConstructShareList(gdAccount1,1000,0);
        uf.shareIssue(newEqCode1,shareList21,true);

        String response = uf.changeBoard(newEqCode1,gdEquityCode,false);
        //链上报错
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
        sleepAndSaveInfo(SLEEPTIME);
        assertEquals("404",JSONObject.fromObject(
                store.GetTxDetail(JSONObject.fromObject(response).getJSONObject("data").getString("txId"))
        ).getString("state"));
    }

    @Test
    public void changeBoard_TC2524() throws Exception{

        String newEqCode1 = "gdEC" + Random(12);

        String response = uf.changeBoard(newEqCode1,"gdEC" + Random(12),false);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("股权代码还未发行或者已经转场",JSONObject.fromObject(response).getString("message"));

    }

    /***
     * 场内转板使用不匹配的平台keyID
     * @throws Exception
     */
    @Test
    public void changeBoard_TC2521_2515() throws Exception{

        String newEqCode1 = "gdEC" + Random(12);
        List<Map> regList = uf.getAllHolderListReg(gdEquityCode,"testErr" + Random(12));
        String response = gd.GDShareChangeBoard(gdAccountKeyID1,gdCompanyID,gdEquityCode,
                "gdEC" + Random(12),regList, equityProductInfo,bondProductInfo);
        assertEquals("505",JSONObject.fromObject(response).getString("state"));
        assertEquals("数字签名出错",JSONObject.fromObject(response).getString("message"));

    }

    /***
     * 场内转板使用不匹配的平台keyID
     * @throws Exception
     */
    @Test
    public void changeBoard_TC2520() throws Exception{

        String newEqCode1 = "gdEC" + Random(12);
        List<Map> regList = uf.getAllHolderListReg(gdEquityCode,"testErr" + Random(12));
        String response = gd.GDShareChangeBoard(gdPlatfromKeyID.substring(3),gdCompanyID,gdEquityCode,
                "gdEC" + Random(12),regList, equityProductInfo,bondProductInfo);
        assertEquals("505",JSONObject.fromObject(response).getString("state"));
        assertEquals("数字签名出错",JSONObject.fromObject(response).getString("message"));

    }

    /***
     * 场内转板使用不存在的keyID （历史版本的）
     * @throws Exception
     */
    @Test
    public void changeBoard_TC2514() throws Exception{

        String newEqCode1 = "gdEC" + Random(12);
        List<Map> regList = uf.getAllHolderListReg(gdEquityCode,"testErr" + Random(12));
        String response = gd.GDShareChangeBoard("bta47g1pgfltc7nntfb0",gdCompanyID,gdEquityCode,
                "gdEC" + Random(12),regList, equityProductInfo,bondProductInfo);
        //链上报错
        assertEquals("505",JSONObject.fromObject(response).getString("state"));
        assertEquals("数字签名出错",JSONObject.fromObject(response).getString("message"));
//        sleepAndSaveInfo(SLEEPTIME);
//        assertEquals("404",JSONObject.fromObject(
//                store.GetTxDetail(JSONObject.fromObject(response).getJSONObject("data").getString("txId"))
//                    ).getString("state"));

    }

    /***
     * 场内转板平台KeyID使用特殊字符
     * @throws Exception
     */
    @Test
    public void changeBoard_TC2516() throws Exception{

        String newEqCode1 = "gdEC" + Random(12);
        List<Map> regList = uf.getAllHolderListReg(gdEquityCode,"test" + Random(15));
        String response = gd.GDShareChangeBoard("@",gdCompanyID,gdEquityCode,
                "gdEC" + Random(12),regList, equityProductInfo,bondProductInfo);
        assertEquals("505",JSONObject.fromObject(response).getString("state"));
        assertEquals("数字签名出错",JSONObject.fromObject(response).getString("message"));
        response = gd.GDShareChangeBoard("#",gdCompanyID,gdEquityCode,
                "gdEC" + Random(12),regList, equityProductInfo,bondProductInfo);
        assertEquals("505",JSONObject.fromObject(response).getString("state"));
        response = gd.GDShareChangeBoard("%",gdCompanyID,gdEquityCode,
                "gdEC" + Random(12),regList, equityProductInfo,bondProductInfo);
        assertEquals("505",JSONObject.fromObject(response).getString("state"));
        response = gd.GDShareChangeBoard("^",gdCompanyID,gdEquityCode,
                "gdEC" + Random(12),regList, equityProductInfo,bondProductInfo);
        assertEquals("505",JSONObject.fromObject(response).getString("state"));
        response = gd.GDShareChangeBoard("|",gdCompanyID,gdEquityCode,
                "gdEC" + Random(12),regList, equityProductInfo,bondProductInfo);
        assertEquals("505",JSONObject.fromObject(response).getString("state"));
        response = gd.GDShareChangeBoard("_",gdCompanyID,gdEquityCode,
                "gdEC" + Random(12),regList, equityProductInfo,bondProductInfo);
        assertEquals("505",JSONObject.fromObject(response).getString("state"));

    }

    /***
     * 不存在的股权代码场内转板
     */

    @Test
    public void changeBoard_NotExistCode_TC2513()throws Exception{

        String response = "";

        String newEquityCode = gdEquityCode + Random(5);
        response = uf.changeBoard(newEquityCode,gdEquityCode + Random(5),false);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("股权代码还未发行或者已经转场",JSONObject.fromObject(response).getString("message"));

        response  = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));

        response = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
    }

    /***
     * 转板前后股权代码相同
     */

    @Test
    public void changeBoard_SameCode_TC2509()throws Exception{

        String response = "";
        response = uf.changeBoard(gdEquityCode,gdEquityCode,false);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("新的股权代码和旧的股权代码一样",JSONObject.fromObject(response).getString("message"));

    }
}
