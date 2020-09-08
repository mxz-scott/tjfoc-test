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
public class GDSceneTest_ChangeBoard {

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
     * 场内转板 双花测试
     */

    @Test
    public void changeBoardDoubleSpend_01()throws Exception{
        String newEqCode1 = gdEquityCode + Random(7);
        String newEqCode2 = gdEquityCode + Random(7);

        String response1 = uf.changeBoard(gdEquityCode,newEqCode1,false);
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

        String query  = gd.GDGetEnterpriseShareInfo(newEquityCode);
        assertEquals("400",JSONObject.fromObject(query).getString("state"));
        assertEquals("该股权代码还未发行或者已经转场",JSONObject.fromObject(query).getString("message"));

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
        assertEquals("该股权代码还未发行或者已经转场",JSONObject.fromObject(query).getString("message"));

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
        assertEquals("该股权代码还未发行或者已经转场",JSONObject.fromObject(query).getString("message"));

        query  = gd.GDGetEnterpriseShareInfo(newEquityCode1);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));


        //第三次转板 new1->new2
        response = uf.changeBoard(newEquityCode1,newEquityCode2,true);

        query  = gd.GDGetEnterpriseShareInfo(newEquityCode1);
        assertEquals("400",JSONObject.fromObject(query).getString("state"));
        assertEquals("该股权代码还未发行或者已经转场",JSONObject.fromObject(query).getString("message"));

        query  = gd.GDGetEnterpriseShareInfo(oldEquityCode);
        assertEquals("400",JSONObject.fromObject(query).getString("state"));
        assertEquals("该股权代码还未发行或者已经转场",JSONObject.fromObject(query).getString("message"));

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
        assertEquals("该股权代码还未发行或者已经转场",JSONObject.fromObject(query).getString("message"));

        query  = gd.GDGetEnterpriseShareInfo(oldEquityCode);
        assertEquals("400",JSONObject.fromObject(query).getString("state"));
        assertEquals("该股权代码还未发行或者已经转场",JSONObject.fromObject(query).getString("message"));

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
    
}