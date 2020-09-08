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
public class GDSceneTest_Recycle {

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
     * 回收流通股 双花测试
     */

    @Test
    public void shareRecycleDoubleSpend_01()throws Exception{
        List<Map> shareList = gdConstructShareList(gdAccount1,100,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount1,200,0);

        String response1 = uf.shareRecycle(gdEquityCode,shareList,false);
        String txId1 = JSONObject.fromObject(response1).getJSONObject("data").getString("txId");
        String response2 = uf.shareRecycle(gdEquityCode,shareList2,false);
        String txId2 = JSONObject.fromObject(response2).getJSONObject("data").getString("txId");

        sleepAndSaveInfo(SLEEPTIME);

        //异或判断两种其中只有一个上链
        assertEquals(true,JSONObject.fromObject(store.GetTxDetail(txId1)).getString("state").equals("200")
                ^JSONObject.fromObject(store.GetTxDetail(txId2)).getString("state").equals("200"));

        gd.GDGetEnterpriseShareInfo(gdEquityCode);
    }

    /***
     * 回收高管股 双花测试
     */

    @Test
    public void shareRecycleDoubleSpend_02()throws Exception{

        List<Map> shareList = gdConstructShareList(gdAccount2,100,1);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,200,1);

        String response1 = uf.shareRecycle(gdEquityCode,shareList,false);
        String txId1 = JSONObject.fromObject(response1).getJSONObject("data").getString("txId");
        String response2 = uf.shareRecycle(gdEquityCode,shareList2,false);
        String txId2 = JSONObject.fromObject(response2).getJSONObject("data").getString("txId");

        sleepAndSaveInfo(SLEEPTIME);

        //异或判断两种其中只有一个上链
        assertEquals(true,JSONObject.fromObject(store.GetTxDetail(txId1)).getString("state").equals("200")
                ^JSONObject.fromObject(store.GetTxDetail(txId2)).getString("state").equals("200"));

        gd.GDGetEnterpriseShareInfo(gdEquityCode);
    }


    /***
     * 冻结部分后 回收流通股
     */

    @Test
    public void shareRecycle_withLock_01()throws Exception{
        String response = "";
        //冻结流通股 *500
        String bizNoTemp = "2000" + Random(12);
        uf.lock(bizNoTemp,gdAccount1,gdEquityCode,500,0,"2022-09-03",true);

        //当前可用余额500 回收小于可用余额
        List<Map> shareList = gdConstructShareList(gdAccount1,100,0);
        uf.shareRecycle(gdEquityCode,shareList,true);

        //当前可用余额500 回收大于可用余额
        shareList = gdConstructShareList(gdAccount1,500,0);
        response = uf.shareRecycle(gdEquityCode,shareList,false);


        if(JSONObject.fromObject(response).getString("state").equals("200")) {
            sleepAndSaveInfo(SLEEPTIME);
            assertEquals("404",JSONObject.fromObject(store.GetTxDetail(
                    JSONObject.fromObject(response).getJSONObject("data").getString("txId"))).getString("state"));
        }
        else{
            assertEquals("501",JSONObject.fromObject(response).getString("state"));
        }

        //当前可用余额500 回收等于可用余额
        shareList = gdConstructShareList(gdAccount1,500,0);
        uf.shareRecycle(gdEquityCode,shareList,true);

    }

    /***
     * 冻结全部后 回收流通股
     */

    @Test
    public void shareRecycle_withLock_05()throws Exception{
        String response = "";
        //全部冻结
        String bizNoTemp = "2000" + Random(12);
        uf.lock(bizNoTemp,gdAccount1,gdEquityCode,1000,0,"2022-09-03",true);


        //尝试回收
        List<Map> shareList = gdConstructShareList(gdAccount1,100,0);
        response = uf.shareRecycle(gdEquityCode,shareList,false);

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
     * 回收流通股超出余额
     */

    @Test
    public void shareRecycle_NotEnough()throws Exception{
        String response = "";
        //尝试回收超过余额
        List<Map> shareList = gdConstructShareList(gdAccount1,10000,0);
        response = uf.shareRecycle(gdEquityCode,shareList,false);

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
     * 回收账户没有的其他性质股份
     */

    @Test
    public void shareRecycle_NotEnough_02()throws Exception{
        String response = "";
        //尝试回收超过余额
        List<Map> shareList = gdConstructShareList(gdAccount6,100,1);
        response = uf.shareRecycle(gdEquityCode,shareList,false);

        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("余额不足",JSONObject.fromObject(response).getString("message"));

    }

    /***
     * 冻结后 回收  流通股
     */

    @Test
    public void shareRecycle_withLock_02()throws Exception{
        String response = "";
        //冻结流通股 *500
        String bizNoTemp = "2000" + Random(12);
        uf.lock(bizNoTemp,gdAccount1,gdEquityCode,500,0,"2022-09-03",true);

        //当前可用余额500 回收小于可用余额
        List<Map> shareList = gdConstructShareList(gdAccount1,100,0);
        uf.shareRecycle(gdEquityCode,shareList,true);

        //当前可用余额500 回收大于可用余额
        shareList = gdConstructShareList(gdAccount1,500,0);
        response = uf.shareRecycle(gdEquityCode,shareList,true);

        if(JSONObject.fromObject(response).getString("state").equals("200")) {
            sleepAndSaveInfo(SLEEPTIME);
            assertEquals("404",JSONObject.fromObject(store.GetTxDetail(
                    JSONObject.fromObject(response).getJSONObject("data").getString("txId"))).getString("state"));
        }
        else{
            assertEquals("501",JSONObject.fromObject(response).getString("state"));
        }

        //当前可用余额500 回收等于可用余额
        shareList = gdConstructShareList(gdAccount1,100,0);
        uf.shareRecycle(gdEquityCode,shareList,true);
    }


    /***
     * 冻结后 回收  高管股股
     * 若高管股存在冻结 可回收可用部分
     */

    @Test
    public void shareRecycle_withLock_03()throws Exception{
        String response = "";
        //冻结高管股 * 1
        String bizNoTemp = "2000" + Random(12);
        uf.lock(bizNoTemp,gdAccount2,gdEquityCode,1,1,"2022-09-03",true);

        //回收小于可用余额
        List<Map> shareList = gdConstructShareList(gdAccount2,100,1);
        uf.shareRecycle(gdEquityCode,shareList,true);

        //回收大于可用余额
        shareList = gdConstructShareList(gdAccount2,100,1);
        response = uf.shareRecycle(gdEquityCode,shareList,false);
        assertEquals("501",JSONObject.fromObject(response).getString("state"));

        //回收等于可用余额
        shareList = gdConstructShareList(gdAccount2,899,1);
        uf.shareRecycle(gdEquityCode,shareList,true);
    }

    /***
     * 账户包含流通股 高管股
     * 分别冻结一部分的流通股和高管股
     * 高管股可以回收可用部分
     * 流通股则可以回收可用部分
     */

    @Test
    public void shareRecycle_withLock_04()throws Exception{
        String response = "";

        //转入余额
        uf.shareTransfer(gdAccountKeyID1,gdAccount1,1000,gdAccount5,0,
                gdEquityCode,0,"test202008280952",true);
        uf.shareTransfer(gdAccountKeyID2,gdAccount2,1000,gdAccount5,1,
                gdEquityCode,0,"test202008280952",true);


        //冻结高管股 * 100 流通股*100
        String bizNoTemp = "2000" + Random(12);
        uf.lock(bizNoTemp,gdAccount5,gdEquityCode,100,0,"2022-09-03",true);

        bizNoTemp = "2000" + Random(12);
        uf.lock(bizNoTemp,gdAccount5,gdEquityCode,100,1,"2022-09-03",true);


        //高管股回收小于可用余额
        List<Map> shareList = gdConstructShareList(gdAccount5,100,1);
        uf.shareRecycle(gdEquityCode,shareList,true);


        //高管股回收回收等于可用余额
        shareList = gdConstructShareList(gdAccount5,800,1);
        uf.shareRecycle(gdEquityCode,shareList,true);


        //流通股回收小于可用余额
        shareList = gdConstructShareList(gdAccount5,100,0);
        uf.shareRecycle(gdEquityCode,shareList,true);

        //高管股回收回收大于可用余额
        shareList = gdConstructShareList(gdAccount5,1000,0);
        response = uf.shareRecycle(gdEquityCode,shareList,false);
        assertEquals("501",JSONObject.fromObject(response).getString("state"));

        //高管股回收转等于可用余额
        shareList = gdConstructShareList(gdAccount5,800,0);
        uf.shareRecycle(gdEquityCode,shareList,true);

    }
    
}