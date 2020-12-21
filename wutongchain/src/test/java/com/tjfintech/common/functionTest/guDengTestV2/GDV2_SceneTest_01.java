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

/***
 * 该class用例为超过4个以上交易类型的复杂用例
 */
public class GDV2_SceneTest_01 {

    TestBuilder testBuilder= TestBuilder.getInstance();
    GuDeng gd =testBuilder.getGuDeng();
    Store store =testBuilder.getStore();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    GDCommonFunc gdCF = new GDCommonFunc();
    public static String bizNoTest = "test" + Random(12);
    GDUnitFunc uf = new GDUnitFunc();
    public Boolean bCreateAccOnce = true;

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

        if(!bCreateAccOnce) {
            //重新创建账户
            gdAccClientNo1 = "No000" + Random(10);
            gdAccClientNo2 = "No100" + Random(10);
            gdAccClientNo3 = "No200" + Random(10);
            gdAccClientNo4 = "No300" + Random(10);
            gdAccClientNo5 = "No400" + Random(10);
            gdAccClientNo6 = "No500" + Random(10);
            gdAccClientNo7 = "No600" + Random(10);
            gdAccClientNo8 = "No700" + Random(10);
            gdAccClientNo9 = "No800" + Random(10);
            gdAccClientNo10 = "No900" + Random(10);

            GDBeforeCondition gdBC = new GDBeforeCondition();
            gdBC.gdCreateAccout();

            sleepAndSaveInfo(3000);
        }

        //发行
        uf.commonIssuePP0(1000);//发行给账户1~4
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

        //全部回收
        if(!bOnlyZero)
            uf.shareRecycle(gdEquityCode,shareList,true);

        //每个测试用例都会重新创建账户时则用例执行完成后则全部销户
        if(!bCreateAccOnce){
            uf.destroyAcc(gdAccClientNo1,true);
            uf.destroyAcc(gdAccClientNo2,true);
            uf.destroyAcc(gdAccClientNo3,true);
            uf.destroyAcc(gdAccClientNo4,true);
            uf.destroyAcc(gdAccClientNo5,true);
            uf.destroyAcc(gdAccClientNo6,true);
            uf.destroyAcc(gdAccClientNo7,true);
            uf.destroyAcc(gdAccClientNo8,true);
            uf.destroyAcc(gdAccClientNo9,true);
            uf.destroyAcc(gdAccClientNo10,true);
        }
    }



    /***
     * 发行A
     * 股份性质变更 1
     * 转让 2
     * 增发 3
     * 冻结/解冻 4
     * 回收B
     * 销户C
     */

    @Test
    public void Test1234()throws Exception{

        uf.changeSHProperty(gdAccount1,gdEquityCode,500,0,1,true);

        uf.shareTransfer(gdAccountKeyID1,gdAccount1,100,gdAccount5,0,
                gdEquityCode,true);
        uf.shareTransfer(gdAccountKeyID2,gdAccount2,100,gdAccount5,0,
                gdEquityCode,true);
        uf.shareTransfer(gdAccountKeyID3,gdAccount3,100,gdAccount5,0,
                gdEquityCode,true);
        uf.shareTransfer(gdAccountKeyID4,gdAccount4,100,gdAccount5,0,
                gdEquityCode,true);


        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,0, shareList3);

        uf.shareIncrease(gdEquityCode,shareList4,true);

        uf.lock(bizNoTest,gdAccount1,gdEquityCode,500,0,"2025-09-30",true);

        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");

        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,1400,0,500,mapShareENCN().get("0"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount1,500,1,0,mapShareENCN().get("1"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount5,400,0,0,mapShareENCN().get("0"),respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2,1900,0,0,mapShareENCN().get("0"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3,1900,0,0,mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4,1900,0,0,mapShareENCN().get("0"), respShareList3);

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
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1400,\"lockAmount\":500}"));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":500,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(gdAccClientNo2,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo2 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount2 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1900,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo3);
        assertEquals(gdAccClientNo3,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo3 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount3 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1900,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo4);
        assertEquals(gdAccClientNo4,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo4 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount4 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1900,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo5);
        assertEquals(gdAccClientNo5,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo5 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount5 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":400,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo6);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        //解除全部冻结
        uf.unlock(bizNoTest,gdEquityCode,500,true);

        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));

        dataShareList = JSONObject.fromObject(query).getJSONArray("data");

        //实际应该持股情况信息
        List<Map> respShareList21 = new ArrayList<>();
        respShareList21 = gdConstructQueryShareList(gdAccount1,1400,0,0,mapShareENCN().get("0"),respShareList21);
        respShareList21 = gdConstructQueryShareList(gdAccount1,500,1,0,mapShareENCN().get("1"),respShareList21);
        respShareList21 = gdConstructQueryShareList(gdAccount5,400,0,0,mapShareENCN().get("0"),respShareList21);
        List<Map> respShareList22 = gdConstructQueryShareList(gdAccount2,1900,0,0,mapShareENCN().get("0"), respShareList21);
        List<Map> respShareList23 = gdConstructQueryShareList(gdAccount3,1900,0,0,mapShareENCN().get("0"), respShareList22);
        List<Map> respShareList24 = gdConstructQueryShareList(gdAccount4,1900,0,0,mapShareENCN().get("0"), respShareList23);

        log.info(respShareList24.toString());
        //检查存在余额的股东列表
        assertEquals(respShareList24.size(),dataShareList.size());

        getShareList = getShareListFromQueryNoZeroAcc(dataShareList);

        assertEquals(respShareList24.size(),getShareList.size());
        assertEquals(true,respShareList24.containsAll(getShareList) && getShareList.containsAll(respShareList24));


        //查询股东持股情况 无当前股权代码信息
        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        assertEquals(gdAccClientNo1,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));

        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo1 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount1 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1400,\"lockAmount\":0}"));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":500,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(gdAccClientNo2,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo2 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount2 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1900,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo3);
        assertEquals(gdAccClientNo3,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo3 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount3 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1900,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo4);
        assertEquals(gdAccClientNo4,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo4 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount4 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1900,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo5);
        assertEquals(gdAccClientNo5,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo5 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount5 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":400,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo6);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));
    }

    /***
     * 发行A
     * 股份性质变更 1
     * 转让 2
     * 增发 3
     * 场内转板 5
     * 回收B
     * 销户C
     */

    @Test
    public void Test1235()throws Exception{
        uf.changeSHProperty(gdAccount1,gdEquityCode,500,0,1,true);

        uf.shareTransfer(gdAccountKeyID1,gdAccount1,100,gdAccount5,0,
                gdEquityCode,true);
        uf.shareTransfer(gdAccountKeyID2,gdAccount2,100,gdAccount5,0,
                gdEquityCode,true);
        uf.shareTransfer(gdAccountKeyID3,gdAccount3,100,gdAccount5,0,
                gdEquityCode,true);
        uf.shareTransfer(gdAccountKeyID4,gdAccount4,100,gdAccount5,0,
                gdEquityCode,true);

        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,0, shareList3);

        uf.shareIncrease(gdEquityCode,shareList4,true);

        String newEqCode = gdEquityCode + Random(5);
        uf.changeBoard(gdEquityCode,newEqCode,true);
        gdEquityCode = newEqCode;


        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");

        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,1400,0,0,mapShareENCN().get("0"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount1,500,1,0,mapShareENCN().get("1"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount5,400,0,0,mapShareENCN().get("0"),respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2,1900,0,0,mapShareENCN().get("0"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3,1900,0,0,mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4,1900,0,0,mapShareENCN().get("0"), respShareList3);

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
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1400,\"lockAmount\":0}"));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":500,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(gdAccClientNo2,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo2 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount2 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1900,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo3);
        assertEquals(gdAccClientNo3,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo3 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount3 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1900,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo4);
        assertEquals(gdAccClientNo4,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo4 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount4 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1900,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo5);
        assertEquals(gdAccClientNo5,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo5 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount5 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":400,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo6);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));
    }


    /***
     * 发行A
     * 股份性质变更 1
     * 增发 3
     * 冻结/解冻 4
     * 场内转板 5
     * 回收B
     * 销户C
     */

    @Test
    public void Test1345()throws Exception{

        uf.changeSHProperty(gdAccount1,gdEquityCode,500,0,1,true);

        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,0, shareList3);

        uf.shareIncrease(gdEquityCode,shareList4,true);

        uf.lockAndUnlock(bizNoTest,gdEquityCode,gdAccount1,500,0);

        String newEqCode = gdEquityCode + Random(5);
        uf.changeBoard(gdEquityCode,newEqCode,true);
        gdEquityCode = newEqCode;

        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");

        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,1500,0,0,mapShareENCN().get("0"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount1,500,1,0,mapShareENCN().get("1"),respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2,2000,0,0,mapShareENCN().get("0"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3,2000,0,0,mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4,2000,0,0,mapShareENCN().get("0"), respShareList3);

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
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1500,\"lockAmount\":0}"));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":500,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(gdAccClientNo2,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo2 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount2 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":2000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo3);
        assertEquals(gdAccClientNo3,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo3 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount3 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":2000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo4);
        assertEquals(gdAccClientNo4,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo4 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount4 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":2000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo5);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo6);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));


    }


    /***
     * 发行A
     * 股份性质变更 1
     * 转让 2
     * 增发 3
     * 冻结/解冻 4
     * 场内转板 5
     * 回收B
     * 销户C
     */

    @Test
    public void Test12345()throws Exception{
        uf.changeSHProperty(gdAccount1,gdEquityCode,500,0,1,true);

        uf.shareTransfer(gdAccountKeyID1,gdAccount1,100,gdAccount5,0,
                gdEquityCode,true);
        uf.shareTransfer(gdAccountKeyID2,gdAccount2,100,gdAccount5,0,
                gdEquityCode,true);
        uf.shareTransfer(gdAccountKeyID3,gdAccount3,100,gdAccount5,0,
                gdEquityCode,true);
        uf.shareTransfer(gdAccountKeyID4,gdAccount4,100,gdAccount5,0,
                gdEquityCode,true);


        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,0, shareList3);

        uf.shareIncrease(gdEquityCode,shareList4,true);

        uf.lockAndUnlock(bizNoTest,gdEquityCode,gdAccount1,500,0);

        String newEqCode = gdEquityCode + Random(5);
        uf.changeBoard(gdEquityCode,newEqCode,true);
        gdEquityCode = newEqCode;

        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");

        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,1400,0,0,mapShareENCN().get("0"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount1,500,1,0,mapShareENCN().get("1"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount5,400,0,0,mapShareENCN().get("0"),respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2,1900,0,0,mapShareENCN().get("0"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3,1900,0,0,mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4,1900,0,0,mapShareENCN().get("0"), respShareList3);

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
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1400,\"lockAmount\":0}"));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":500,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(gdAccClientNo2,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo2 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount2 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1900,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo3);
        assertEquals(gdAccClientNo3,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo3 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount3 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1900,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo4);
        assertEquals(gdAccClientNo4,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo4 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount4 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1900,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo5);
        assertEquals(gdAccClientNo5,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo5 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount5 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":400,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo6);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

    }


    /***
     * 发行A
     * 股份性质变更 1
     * 场内转板 5
     * 冻结/解冻 4
     * 转让 2
     * 增发 3
     * 回收B
     * 销户C
     */

    @Test
    public void Test15423()throws Exception{

        uf.changeSHProperty(gdAccount1,gdEquityCode,500,0,1,true);

        String newEqCode = gdEquityCode + Random(5);
        uf.changeBoard(gdEquityCode,newEqCode,true);
        gdEquityCode = newEqCode;

        uf.lockAndUnlock(bizNoTest,gdEquityCode,gdAccount1,500,0);

        uf.shareTransfer(gdAccountKeyID1,gdAccount1,100,gdAccount5,0,
                gdEquityCode,true);
        uf.shareTransfer(gdAccountKeyID2,gdAccount2,100,gdAccount5,0,
                gdEquityCode,true);
        uf.shareTransfer(gdAccountKeyID3,gdAccount3,100,gdAccount5,0,
                gdEquityCode,true);
        uf.shareTransfer(gdAccountKeyID4,gdAccount4,100,gdAccount5,0,
                gdEquityCode,true);


        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,0, shareList3);

        uf.shareIncrease(gdEquityCode,shareList4,true);


        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");

        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,1400,0,0,mapShareENCN().get("0"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount1,500,1,0,mapShareENCN().get("1"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount5,400,0,0,mapShareENCN().get("0"),respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2,1900,0,0,mapShareENCN().get("0"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3,1900,0,0,mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4,1900,0,0,mapShareENCN().get("0"), respShareList3);

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
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1400,\"lockAmount\":0}"));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":500,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(gdAccClientNo2,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo2 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount2 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1900,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo3);
        assertEquals(gdAccClientNo3,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo3 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount3 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1900,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo4);
        assertEquals(gdAccClientNo4,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo4 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount4 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1900,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo5);
        assertEquals(gdAccClientNo5,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo5 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount5 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":400,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo6);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));
    }


    /***
     * 发行A

     * 场内转板 5
     * 冻结/解冻 4
     * 增发 3
     * 转让 2
     * 股份性质变更 1
     * 回收B
     * 销户C
     */

    @Test
    public void Test54321()throws Exception{
        String newEqCode = gdEquityCode + Random(5);
        uf.changeBoard(gdEquityCode,newEqCode,true);
        gdEquityCode = newEqCode;

        uf.lockAndUnlock(bizNoTest,gdEquityCode,gdAccount1,500,0);


        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,0, shareList3);

        uf.shareIncrease(gdEquityCode,shareList4,true);

        uf.shareTransfer(gdAccountKeyID1,gdAccount1,100,gdAccount5,0,
                gdEquityCode,true);
        uf.shareTransfer(gdAccountKeyID2,gdAccount2,100,gdAccount5,0,
                gdEquityCode,true);
        uf.shareTransfer(gdAccountKeyID3,gdAccount3,100,gdAccount5,0,
                gdEquityCode,true);
        uf.shareTransfer(gdAccountKeyID4,gdAccount4,100,gdAccount5,0,
                gdEquityCode,true);

        uf.changeSHProperty(gdAccount1,gdEquityCode,500,0,1,true);


        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");

        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,1400,0,0,mapShareENCN().get("0"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount1,500,1,0,mapShareENCN().get("1"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount5,400,0,0,mapShareENCN().get("0"),respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2,1900,0,0,mapShareENCN().get("0"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3,1900,0,0,mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4,1900,0,0,mapShareENCN().get("0"), respShareList3);

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
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1400,\"lockAmount\":0}"));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":500,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(gdAccClientNo2,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo2 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount2 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1900,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo3);
        assertEquals(gdAccClientNo3,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo3 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount3 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1900,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo4);
        assertEquals(gdAccClientNo4,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo4 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount4 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1900,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo5);
        assertEquals(gdAccClientNo5,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo5 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount5 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":400,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo6);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));
    }


    /***
     * 存在各种交易后场内转板
     */

    @Test
    public void changeBoard_AfterAllPro()throws Exception{
        uf.changeSHProperty(gdAccount1,gdEquityCode,500,0,1,true);

        uf.shareTransfer(gdAccountKeyID1,gdAccount1,100,gdAccount5,0,
                gdEquityCode,true);


        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,0, shareList3);

        uf.shareIncrease(gdEquityCode,shareList4,true);

        uf.shareRecycle(gdEquityCode,shareList2,true);

        uf.lockAndUnlock(bizNoTest,gdEquityCode,gdAccount1,500,0);

        String newEqCode = gdEquityCode + Random(5);
        uf.changeBoard(gdEquityCode,newEqCode,true);
        gdEquityCode = newEqCode;


        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");

        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,400,0,0,mapShareENCN().get("0"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount1,500,1,0,mapShareENCN().get("1"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount5,100,0,0,mapShareENCN().get("0"),respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2,1000,0,0,mapShareENCN().get("0"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3,2000,0,0,mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4,2000,0,0,mapShareENCN().get("0"), respShareList3);

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
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":400,\"lockAmount\":0}"));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":500,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(gdAccClientNo2,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo2 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount2 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo3);
        assertEquals(gdAccClientNo3,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo3 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount3 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":2000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo4);
        assertEquals(gdAccClientNo4,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo4 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount4 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":2000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo5);
        assertEquals(gdAccClientNo5,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo5 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount5 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":100,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo6);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));
    }


    /***
     * 发行A
     * 股份性质变更 1
     * 转让 2
     * 增发 3
     * 冻结/解冻 4      解除全部冻结
     * 场内转板 5
     * 回收B
     * 销户C
     */

    @Test
    public void TestB12345()throws Exception{
        //回收部分 其中存在部分账户回收全部
        List<Map> shareListR1 = gdConstructShareList(gdAccount1,400,0);
        List<Map> shareListR2 = gdConstructShareList(gdAccount2,1000,0, shareListR1);
        List<Map> shareListR3 = gdConstructShareList(gdAccount3,900,0, shareListR2);
        List<Map> shareListR4 = gdConstructShareList(gdAccount4,500,0, shareListR3);

        uf.shareRecycle(gdEquityCode,shareListR4,true);

        uf.changeSHProperty(gdAccount1,gdEquityCode,500,0,1,true);

        uf.shareTransfer(gdAccountKeyID1,gdAccount1,100,gdAccount5,0,
                gdEquityCode,true);
//        uf.shareTransfer(gdAccountKeyID2,gdAccount2,100,gdAccount5,0,
//                gdEquityCode,true);
        uf.shareTransfer(gdAccountKeyID3,gdAccount3,100,gdAccount5,0,
                gdEquityCode,true);
        uf.shareTransfer(gdAccountKeyID4,gdAccount4,100,gdAccount5,0,
                gdEquityCode,true);


        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,0, shareList3);

        uf.shareIncrease(gdEquityCode,shareList4,true);

        uf.lockAndUnlock(bizNoTest,gdEquityCode,gdAccount1,500,0);

        String newEqCode = gdEquityCode + Random(5);
        uf.changeBoard(gdEquityCode,newEqCode,true);
        gdEquityCode = newEqCode;

        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");

        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,1000,0,0,mapShareENCN().get("0"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount1,500,1,0,mapShareENCN().get("1"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount5,300,0,0,mapShareENCN().get("0"),respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2,1000,0,0,mapShareENCN().get("0"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3,1000,0,0,mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4,1400,0,0,mapShareENCN().get("0"), respShareList3);

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
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":500,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(gdAccClientNo2,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo2 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount2 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo3);
        assertEquals(gdAccClientNo3,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo3 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount3 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo4);
        assertEquals(gdAccClientNo4,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo4 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount4 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1400,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo5);
        assertEquals(gdAccClientNo5,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo5 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount5 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":300,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo6);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

    }


    /***
     * 发行A
     * 股份性质变更 1
     * 场内转板 5
     * 冻结/解冻 4
     * 转让 2
     * 增发 3
     * 回收B
     * 销户C
     */

    @Test
    public void TestB15423()throws Exception{

        //回收部分 其中存在部分账户回收全部
        List<Map> shareListR1 = gdConstructShareList(gdAccount1,400,0);
        List<Map> shareListR2 = gdConstructShareList(gdAccount2,1000,0, shareListR1);
        List<Map> shareListR3 = gdConstructShareList(gdAccount3,900,0, shareListR2);
        List<Map> shareListR4 = gdConstructShareList(gdAccount4,500,0, shareListR3);

        uf.shareRecycle(gdEquityCode,shareListR4,true);

        uf.changeSHProperty(gdAccount1,gdEquityCode,500,0,1,true);

        String newEqCode = gdEquityCode + Random(5);
        uf.changeBoard(gdEquityCode,newEqCode,true);
        gdEquityCode = newEqCode;

        uf.lockAndUnlock(bizNoTest,gdEquityCode,gdAccount1,500,0);

        uf.shareTransfer(gdAccountKeyID1,gdAccount1,100,gdAccount5,0,
                gdEquityCode,true);
//        uf.shareTransfer(gdAccountKeyID2,gdAccount2,100,gdAccount5,0,
//                gdEquityCode,true);
        uf.shareTransfer(gdAccountKeyID3,gdAccount3,100,gdAccount5,0,
                gdEquityCode,true);
        uf.shareTransfer(gdAccountKeyID4,gdAccount4,100,gdAccount5,0,
                gdEquityCode,true);


        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,0, shareList3);

        uf.shareIncrease(gdEquityCode,shareList4,true);


        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");

        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,1000,0,0,mapShareENCN().get("0"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount1,500,1,0,mapShareENCN().get("1"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount5,300,0,0,mapShareENCN().get("0"),respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2,1000,0,0,mapShareENCN().get("0"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3,1000,0,0,mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4,1400,0,0,mapShareENCN().get("0"), respShareList3);

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
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":500,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(gdAccClientNo2,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo2 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount2 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo3);
        assertEquals(gdAccClientNo3,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo3 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount3 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo4);
        assertEquals(gdAccClientNo4,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo4 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount4 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1400,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo5);
        assertEquals(gdAccClientNo5,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo5 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount5 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":300,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo6);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));
    }


    /***
     * 发行A

     * 场内转板 5
     * 冻结/解冻 4
     * 增发 3
     * 转让 2
     * 股份性质变更 1
     * 回收B
     * 销户C
     */

    @Test
    public void TestB54321()throws Exception{
        //回收部分 其中存在部分账户回收全部
        List<Map> shareListR1 = gdConstructShareList(gdAccount1,400,0);
        List<Map> shareListR2 = gdConstructShareList(gdAccount2,1000,0, shareListR1);
        List<Map> shareListR3 = gdConstructShareList(gdAccount3,900,0, shareListR2);
        List<Map> shareListR4 = gdConstructShareList(gdAccount4,500,0, shareListR3);

        uf.shareRecycle(gdEquityCode,shareListR4,true);

        String newEqCode = gdEquityCode + Random(5);
        uf.changeBoard(gdEquityCode,newEqCode,true);
        gdEquityCode = newEqCode;

        uf.lockAndUnlock(bizNoTest,gdEquityCode,gdAccount1,500,0);

        uf.shareTransfer(gdAccountKeyID1,gdAccount1,100,gdAccount5,0,
                gdEquityCode,true);
//        uf.shareTransfer(gdAccountKeyID2,gdAccount2,100,gdAccount5,0,
//                gdEquityCode,true);
        uf.shareTransfer(gdAccountKeyID3,gdAccount3,100,gdAccount5,0,
                gdEquityCode,true);
        uf.shareTransfer(gdAccountKeyID4,gdAccount4,100,gdAccount5,0,
                gdEquityCode,true);


        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,0, shareList3);

        uf.shareIncrease(gdEquityCode,shareList4,true);

        uf.changeSHProperty(gdAccount1,gdEquityCode,500,0,1,true);


        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");

        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,1000,0,0,mapShareENCN().get("0"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount1,500,1,0,mapShareENCN().get("1"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount5,300,0,0,mapShareENCN().get("0"),respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2,1000,0,0,mapShareENCN().get("0"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3,1000,0,0,mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4,1400,0,0,mapShareENCN().get("0"), respShareList3);

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
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":500,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(gdAccClientNo2,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo2 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount2 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo3);
        assertEquals(gdAccClientNo3,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo3 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount3 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo4);
        assertEquals(gdAccClientNo4,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo4 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount4 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1400,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo5);
        assertEquals(gdAccClientNo5,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo5 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount5 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":300,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo6);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));
    }

}
