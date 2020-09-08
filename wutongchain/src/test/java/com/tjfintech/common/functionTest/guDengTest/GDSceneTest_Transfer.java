package com.tjfintech.common.functionTest.guDengTest;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.GDBeforeCondition;
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

import java.util.List;
import java.util.Map;

import static com.tjfintech.common.CommonFunc.*;
import static com.tjfintech.common.utils.UtilsClass.*;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class GDSceneTest_Transfer {

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
        String response= gd.GDShareIssue(gdContractAddress,gdPlatfromKeyID,gdEquityCode,shareList4);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

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
     * 转让流通股 交易过户 双花测试
     */

    @Test
    public void shareTransferDoubleSpend_01()throws Exception{

        String response1 = uf.shareTransfer(gdAccountKeyID1,gdAccount1,100,gdAccount5,0,
                gdEquityCode,1,"test202008280952",false);
        String txId1 = JSONObject.fromObject(response1).getJSONObject("data").getString("txId");
        String response2 = uf.shareTransfer(gdAccountKeyID1,gdAccount1,200,gdAccount6,0,
                gdEquityCode,1,"test202008280953",false);
        String txId2 = JSONObject.fromObject(response2).getJSONObject("data").getString("txId");

        sleepAndSaveInfo(SLEEPTIME);

        //异或判断两种其中只有一个上链
        assertEquals(true,JSONObject.fromObject(store.GetTxDetail(txId1)).getString("state").equals("200")
                ^JSONObject.fromObject(store.GetTxDetail(txId2)).getString("state").equals("200"));

        gd.GDGetEnterpriseShareInfo(gdEquityCode);
    }

    /***
     * 转让流通股 非交易过户 双花测试
     */

    @Test
    public void shareTransferDoubleSpend_02()throws Exception{

        String response1 = uf.shareTransfer(gdAccountKeyID2,gdAccount2,100,gdAccount5,1,
                gdEquityCode,0,"test202008280954",false);
        String txId1 = JSONObject.fromObject(response1).getJSONObject("data").getString("txId");
        String response2 = uf.shareTransfer(gdAccountKeyID2,gdAccount2,200,gdAccount6,1,
                gdEquityCode,0,"test202008280955",false);
        String txId2 = JSONObject.fromObject(response2).getJSONObject("data").getString("txId");

        sleepAndSaveInfo(SLEEPTIME);

        //异或判断两种其中只有一个上链
        assertEquals(true,JSONObject.fromObject(store.GetTxDetail(txId1)).getString("state").equals("200")
                ^JSONObject.fromObject(store.GetTxDetail(txId2)).getString("state").equals("200"));

        gd.GDGetEnterpriseShareInfo(gdEquityCode);
    }


    /***
     * 冻结部分后 转让流通股 交易过户
     */

    @Test
    public void shareTransfer_withLock_01()throws Exception{
        String response = "";
        //冻结流通股 *500
        String bizNoTemp = "2000" + Random(12);
        uf.lock(bizNoTemp,gdAccount1,gdEquityCode,500,0,"2022-09-03",true);

        //当前可用余额500 转出小于可用余额
        uf.shareTransfer(gdAccountKeyID1,gdAccount1,100,gdAccount5,0,
                gdEquityCode,1,"test202008280952",true);

        //当前可用余额500 转出大于可用余额
        response = uf.shareTransfer(gdAccountKeyID1,gdAccount1,500,gdAccount5,0,
                gdEquityCode,1,"test202008280952",false);

        if(JSONObject.fromObject(response).getString("state").equals("200")) {
            sleepAndSaveInfo(SLEEPTIME);
            assertEquals("404",JSONObject.fromObject(store.GetTxDetail(
                    JSONObject.fromObject(response).getJSONObject("data").getString("txId"))).getString("state"));
        }
        else{
            assertEquals("501",JSONObject.fromObject(response).getString("state"));
        }

        //当前可用余额500 转出等于可用余额
        uf.shareTransfer(gdAccountKeyID1,gdAccount1,400,gdAccount5,0,
                gdEquityCode,1,"test202008280952",true);

    }

    /***
     * 冻结全部后 转让流通股 交易过户
     */

    @Test
    public void shareTransfer_withLock_05()throws Exception{
        String response = "";
        //全部冻结
        String bizNoTemp = "2000" + Random(12);
        uf.lock(bizNoTemp,gdAccount1,gdEquityCode,1000,0,"2022-09-03",true);


        //尝试转出
        response = uf.shareTransfer(gdAccountKeyID1,gdAccount1,1,gdAccount5,0,
                gdEquityCode,1,"test202008280952",false);

        if(JSONObject.fromObject(response).getString("state").equals("200")) {
            sleepAndSaveInfo(SLEEPTIME);
            assertEquals("404",JSONObject.fromObject(store.GetTxDetail(
                    JSONObject.fromObject(response).getJSONObject("data").getString("txId"))).getString("state"));
        }
        else{
            assertEquals("501",JSONObject.fromObject(response).getString("state"));
        }

    }

    /***
     * 转让流通股超出余额
     */

    @Test
    public void shareTransfer_NotEnough()throws Exception{
        String response = "";
         //尝试转出超过余额
        response = uf.shareTransfer(gdAccountKeyID1,gdAccount1,10000,gdAccount5,0,
                gdEquityCode,1,"test202008280952",false);

        if(JSONObject.fromObject(response).getString("state").equals("200")) {
            sleepAndSaveInfo(SLEEPTIME);
            assertEquals("404",JSONObject.fromObject(store.GetTxDetail(
                    JSONObject.fromObject(response).getJSONObject("data").getString("txId"))).getString("state"));
        }
        else{
            assertEquals("400",JSONObject.fromObject(response).getString("state"));
        }

    }

    /***
     * 转让账户没有的其他性质股份
     */

    @Test
    public void shareTransfer_NotEnough_02()throws Exception{
        String response = "";
        //尝试转出超过余额
        response = uf.shareTransfer(gdAccountKeyID1,gdAccount1,1000,gdAccount5,1,
                gdEquityCode,0,"test202008280952",false);

        if(JSONObject.fromObject(response).getString("state").equals("200")) {
            sleepAndSaveInfo(SLEEPTIME);
            assertEquals("404",JSONObject.fromObject(store.GetTxDetail(
                    JSONObject.fromObject(response).getJSONObject("data").getString("txId"))).getString("state"));
        }
        else{
            assertEquals("400",JSONObject.fromObject(response).getString("state"));
        }

    }

    /***
     * 冻结后 转让非流通股 交易过户
     * 交易过户不支持非流通股转让
     */

    @Test
    public void shareTransferInvalid01()throws Exception{
        String response = "";

        //当前可用余额500 转出小于可用余额
        response = uf.shareTransfer(gdAccountKeyID1,gdAccount1,100,gdAccount5,1,
                gdEquityCode,1,"test202008280952",false);

        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("交易过户非流通股不可以转让",JSONObject.fromObject(response).getString("message"));

    }

    /***
     * 冻结后 转让 非交易过户 流通股
     */

    @Test
    public void shareTransfer_withLock_02()throws Exception{
        String response = "";
        //冻结流通股 *500
        String bizNoTemp = "2000" + Random(12);
        uf.lock(bizNoTemp,gdAccount1,gdEquityCode,500,0,"2022-09-03",true);

        //当前可用余额500 转出小于可用余额
        uf.shareTransfer(gdAccountKeyID1,gdAccount1,100,gdAccount5,0,
                gdEquityCode,0,"test202008280952",true);

        //当前可用余额500 转出大于可用余额
        response = uf.shareTransfer(gdAccountKeyID1,gdAccount1,500,gdAccount5,0,
                gdEquityCode,0,"test202008280952",false);

        if(JSONObject.fromObject(response).getString("state").equals("200")) {
            sleepAndSaveInfo(SLEEPTIME);
            assertEquals("404",JSONObject.fromObject(store.GetTxDetail(
                    JSONObject.fromObject(response).getJSONObject("data").getString("txId"))).getString("state"));
        }
        else{
            assertEquals("400",JSONObject.fromObject(response).getString("state"));
        }

        //当前可用余额500 转出等于可用余额
        uf.shareTransfer(gdAccountKeyID1,gdAccount1,400,gdAccount5,0,
                gdEquityCode,0,"test202008280952",true);
    }


    /***
     * 冻结后 转让 非交易过户 高管股股
     * 若高管股存在冻结 可转出可用部分
     */

    @Test
    public void shareTransfer_withLock_03()throws Exception{
        String response = "";
        //冻结高管股 * 1
        String bizNoTemp = "2000" + Random(12);
        uf.lock(bizNoTemp,gdAccount2,gdEquityCode,1,1,"2022-09-03",true);

        //转出小于可用余额
        response = uf.shareTransfer(gdAccountKeyID2,gdAccount2,100,gdAccount5,1,
                gdEquityCode,0,"test202008280952",true);

        //转出大于可用余额
        response = uf.shareTransfer(gdAccountKeyID2,gdAccount2,1000,gdAccount5,1,
                gdEquityCode,0,"test202008280952",false);
        assertEquals("501",JSONObject.fromObject(response).getString("state"));

        //转出等于可用余额
        response = uf.shareTransfer(gdAccountKeyID2,gdAccount2,899,gdAccount5,1,
                gdEquityCode,0,"test202008280952",true);
    }

    /***
     * 账户包含流通股 高管股
     * 分别冻结一部分的流通股和高管股
     * 高管股可以转出可用部分
     * 流通股则可以转出可用部分
     */

    @Test
    public void shareTransfer_withLock_04()throws Exception{
        String response = "";

        //转出小于可用余额
        uf.shareTransfer(gdAccountKeyID1,gdAccount1,1000,gdAccount5,0,
                gdEquityCode,0,"test202008280952",true);
        uf.shareTransfer(gdAccountKeyID2,gdAccount2,1000,gdAccount5,1,
                gdEquityCode,0,"test202008280952",true);


        //冻结高管股 * 100 流通股*100
        String bizNoTemp = "2000" + Random(12);
        uf.lock(bizNoTemp,gdAccount5,gdEquityCode,100,0,"2022-09-03",true);

        bizNoTemp = "2000" + Random(12);
        uf.lock(bizNoTemp,gdAccount5,gdEquityCode,100,1,"2022-09-03",true);


        //高管股转出小于可用余额
        response = uf.shareTransfer(gdAccountKeyID5,gdAccount5,100,gdAccount6,1,
                gdEquityCode,0,"test202008280952",true);

        //高管股转出转出等于可用余额
        response = uf.shareTransfer(gdAccountKeyID5,gdAccount5,800,gdAccount6,1,
                gdEquityCode,0,"test202008280952",true);



        //流通股转出小于可用余额
        uf.shareTransfer(gdAccountKeyID5,gdAccount5,100,gdAccount6,0,
                gdEquityCode,1,"test202008280952",true);

        //高管股转出转出大于可用余额
        response = uf.shareTransfer(gdAccountKeyID5,gdAccount5,1000,gdAccount6,0,
                gdEquityCode,0,"test202008280952",false);
        assertEquals("501",JSONObject.fromObject(response).getString("state"));

        //高管股转出转等大于可用余额
        uf.shareTransfer(gdAccountKeyID5,gdAccount5,800,gdAccount6,0,
                gdEquityCode,0,"test202008280952",true);

    }

    /***
     * 多次转让
     */

    @Test
    public void multChangeProperty()throws Exception{

        String response = "";
        //发行
        gdEquityCode = "gdEC" + Random(12);
        List<Map> shareList = gdConstructShareList(gdAccount3,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount3,3000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,2000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount3,3000,0, shareList3);

        //发行
        response= gd.GDShareIssue(gdContractAddress,gdPlatfromKeyID,gdEquityCode,shareList4);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        JSONArray jsonArrayGet = JSONObject.fromObject(query).getJSONArray("data");
        for(int i = 0;i < 15; i++){
            log.info("change time " + i);
            uf.shareTransfer(gdAccountKeyID1,gdAccount3,600,gdAccount5,0,gdEquityCode,1,"test0212352",true);
            query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
            assertEquals(9000,getTotalAmountFromShareList(jsonArrayGet),0.0001);
        }

        gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo5);
        assertEquals(gdAccClientNo5,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo5 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount5 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":9000,\"lockAmount\":0}"));

    }
}