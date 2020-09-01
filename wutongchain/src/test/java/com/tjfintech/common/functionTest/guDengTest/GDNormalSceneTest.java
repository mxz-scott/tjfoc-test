package com.tjfintech.common.functionTest.guDengTest;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.GDBeforeCondition;
import com.tjfintech.common.Interface.GuDeng;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.CommonFunc.gdConstructShareList;
import static com.tjfintech.common.utils.UtilsClass.*;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class GDNormalSceneTest {

    TestBuilder testBuilder= TestBuilder.getInstance();
    GuDeng gd =testBuilder.getGuDeng();
    Store store =testBuilder.getStore();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    public static String bizNoTest = "test" + Random(12);

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
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,0, shareList3);

        //发行
        String response= gd.GDShareIssue(gdContractAddress,gdPlatfromKeyID,gdEquityCode,shareList4);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));
    }

//    @After
    public void DestroyEquityAndAcc()throws Exception{
        //查询企业所有股东持股情况

        //依次回收

        //依次销户

    }


    /***
     * 发行A
     * 股份性质变更 1
     * 回收B
     * 销户C
     */

    @Test
    public void Test1()throws Exception{
        changeSHProperty(gdAccount1,gdEquityCode,500,0,1);
    }


    /***
     * 发行A
     * 转让 2 交易过户
     * 回收B
     * 销户C
     */

    @Test
    public void Test2_1()throws Exception{
        shareTransfer(gdAccountKeyID1,gdAccount1,100,gdAccount5,0,
                gdEquityCode,1,"test202008280952");
    }

    /***
     * 发行A
     * 转让 2 非交易过户
     * 回收B
     * 销户C
     */

    @Test
    public void Test2_2()throws Exception{
        shareTransfer(gdAccountKeyID1,gdAccount1,100,gdAccount5,0,
                gdEquityCode,0,"test202008280952");
    }

    /***
     * 发行A
     * 增发 3
     * 回收B
     * 销户C
     */

    @Test
    public void Test3()throws Exception{
        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,0, shareList3);

        shareIncrease(gdEquityCode,shareList4);

    }


    /***
     * 发行A
     * 冻结/解冻 4
     * 回收B
     * 销户C
     */

    @Test
    public void Test4()throws Exception{
        lockAndUnlock(bizNoTest,gdEquityCode,gdAccount1,500,0);
    }


    /***
     * 发行A
     * 场内转板 5
     * 回收B
     * 销户C
     */

    @Test
    public void Test5()throws Exception{
        changeBoard(gdEquityCode,gdEquityCode + Random(5));
    }


    /***
     * 发行A
     * 股份性质变更 1
     * 转让 2
     * 回收B
     * 销户C
     */

    @Test
    public void Test12()throws Exception{
        changeSHProperty(gdAccount1,gdEquityCode,500,0,1);

        shareTransfer(gdAccountKeyID1,gdAccount1,100,gdAccount5,0,
                gdEquityCode,1,"test202008280952");
    }

    /***
     * 发行A
     * 转让 2
     * 股份性质变更 1
     * 回收B
     * 销户C
     */

    @Test
    public void Test21()throws Exception{
        shareTransfer(gdAccountKeyID1,gdAccount1,100,gdAccount5,0,
                gdEquityCode,1,"test202008280952");

        changeSHProperty(gdAccount1,gdEquityCode,500,0,1);
    }

    /***
     * 发行A
     * 股份性质变更 1
     * 增发 3
     * 回收B
     * 销户C
     */
    @Test
    public void Test13()throws Exception{
        changeSHProperty(gdAccount1,gdEquityCode,500,0,1);

        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,0, shareList3);
        shareIncrease(gdEquityCode,shareList4);
    }

    /***
     * 发行A
     * 增发 3
     * 股份性质变更 1
     * 回收B
     * 销户C
     */

    @Test
    public void Test31()throws Exception{
        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,0, shareList3);
        shareIncrease(gdEquityCode,shareList4);

        changeSHProperty(gdAccount1,gdEquityCode,500,0,1);
    }

    /***
     * 发行A
     * 股份性质变更 1
     * 冻结/解冻 4
     * 回收B
     * 销户C
     */

    @Test
    public void Test14()throws Exception{
        changeSHProperty(gdAccount1,gdEquityCode,500,0,1);
        lockAndUnlock(bizNoTest,gdEquityCode,gdAccount1,500,0);
    }

    /***
     * 发行A
     * 冻结/解冻 4
     * 股份性质变更 1
     * 回收B
     * 销户C
     */

    @Test
    public void Test41()throws Exception{
        lockAndUnlock(bizNoTest,gdEquityCode,gdAccount1,500,0);
        changeSHProperty(gdAccount1,gdEquityCode,500,0,1);
    }


    /***
     * 发行A
     * 股份性质变更 1
     * 场内转板 5
     * 回收B
     * 销户C
     */

    @Test
    public void Test15()throws Exception{
        changeSHProperty(gdAccount1,gdEquityCode,500,0,1);
        changeBoard(gdEquityCode,gdEquityCode + Random(5));
    }


    /***
     * 发行A
     * 场内转板 5
     * 股份性质变更 1
     * 回收B
     * 销户C
     */

    @Test
    public void Test51()throws Exception{
        changeBoard(gdEquityCode,gdEquityCode + Random(5));
        changeSHProperty(gdAccount1,gdEquityCode,500,0,1);
    }

    /***
     * 发行A
     * 转让 2
     * 增发 3
     * 回收B
     * 销户C
     */

    @Test
    public void Test23()throws Exception{
        shareTransfer(gdAccountKeyID1,gdAccount1,100,gdAccount5,0,
                gdEquityCode,0,"test202008280952");

        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,0, shareList3);

        shareIncrease(gdEquityCode,shareList4);
    }

    /***
     * 发行A
     * 增发 3
     * 转让 2
     * 回收B
     * 销户C
     */

    @Test
    public void Test32()throws Exception{
        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,0, shareList3);

        shareIncrease(gdEquityCode,shareList4);

        shareTransfer(gdAccountKeyID1,gdAccount1,100,gdAccount5,0,
                gdEquityCode,1,"test202008280952");

    }


    /***
     * 发行A
     * 转让 2
     * 冻结/解冻 4
     * 回收B
     * 销户C
     */

    @Test
    public void Test24()throws Exception{

        shareTransfer(gdAccountKeyID1,gdAccount1,100,gdAccount5,0,
                gdEquityCode,1,"test202008280952");
        lockAndUnlock(bizNoTest,gdEquityCode,gdAccount1,500,0);
    }

    /***
     * 发行A
     * 冻结/解冻 4
     * 转让 2
     * 回收B
     * 销户C
     */

    @Test
    public void Test42()throws Exception{
        lockAndUnlock(bizNoTest,gdEquityCode,gdAccount1,500,0);
        shareTransfer(gdAccountKeyID1,gdAccount1,100,gdAccount5,0,
                gdEquityCode,0,"test202008280952");
    }

    /***
     * 发行A
     * 转让 2
     * 场内转板 5
     * 回收B
     * 销户C
     */

    @Test
    public void Test25()throws Exception{
        shareTransfer(gdAccountKeyID1,gdAccount1,100,gdAccount5,0,
                gdEquityCode,1,"test202008280952");
        changeBoard(gdEquityCode,gdEquityCode + Random(5));
    }

    /***
     * 发行A
     * 场内转板 5
     * 转让 2
     * 回收B
     * 销户C
     */

    @Test
    public void Test52()throws Exception{
        changeBoard(gdEquityCode,gdEquityCode + Random(5));
        shareTransfer(gdAccountKeyID1,gdAccount1,100,gdAccount5,0,
                gdEquityCode,0,"test202008280952");
    }


    /***
     * 发行A
     * 增发 3
     * 冻结/解冻 4
     * 回收B
     * 销户C
     */

    @Test
    public void Test34()throws Exception{
        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,0, shareList3);

        shareIncrease(gdEquityCode,shareList4);

        lockAndUnlock(bizNoTest,gdEquityCode,gdAccount1,500,0);
    }

    /***
     * 发行A
     * 冻结/解冻 4
     * 增发 3
     * 回收B
     * 销户C
     */

    @Test
    public void Test43()throws Exception{
        lockAndUnlock(bizNoTest,gdEquityCode,gdAccount1,500,0);

        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,0, shareList3);

        shareIncrease(gdEquityCode,shareList4);
    }


    /***
     * 发行A
     * 增发 3
     * 场内转板 5
     * 回收B
     * 销户C
     */

    @Test
    public void Test35()throws Exception{
        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,0, shareList3);
        shareIncrease(gdEquityCode,shareList4);

        changeBoard(gdEquityCode,gdEquityCode + Random(5));

    }

    /***
     * 发行A
     * 场内转板 5
     * 增发 3
     * 回收B
     * 销户C
     */

    @Test
    public void Test53()throws Exception{
        changeBoard(gdEquityCode,gdEquityCode + Random(5));

        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,0, shareList3);
        shareIncrease(gdEquityCode,shareList4);
    }


    /***
     * 发行A
     * 冻结/解冻 4
     * 场内转板 5
     * 回收B
     * 销户C
     */

    @Test
    public void Test45()throws Exception{
        lockAndUnlock(bizNoTest,gdEquityCode,gdAccount1,500,0);

        changeBoard(gdEquityCode,gdEquityCode + Random(5));
    }

    /***
     * 发行A
     * 场内转板 5
     * 冻结/解冻 4
     * 回收B
     * 销户C
     */

    @Test
    public void Test54()throws Exception{
        changeBoard(gdEquityCode,gdEquityCode + Random(5));

        lockAndUnlock(bizNoTest,gdEquityCode,gdAccount1,500,0);
    }

    /***
     * 发行A
     * 股份性质变更 1
     * 转让 2
     * 增发 3
     * 回收B
     * 销户C
     */

    @Test
    public void Test123()throws Exception{
        changeSHProperty(gdAccount1,gdEquityCode,500,0,1);

        shareTransfer(gdAccountKeyID1,gdAccount1,100,gdAccount5,0,
                gdEquityCode,1,"test202008280952");


        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,0, shareList3);

        shareIncrease(gdEquityCode,shareList4);
    }

    /***
     * 发行A
     * 股份性质变更 1
     * 转让 2
     * 冻结/解冻 4
     * 回收B
     * 销户C
     */

    @Test
    public void Test124()throws Exception{
        changeSHProperty(gdAccount1,gdEquityCode,500,0,1);

        shareTransfer(gdAccountKeyID1,gdAccount1,100,gdAccount5,0,
                gdEquityCode,1,"test202008280952");


        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,0, shareList3);

        lockAndUnlock(bizNoTest,gdEquityCode,gdAccount1,500,0);
    }

    /***
     * 发行A
     * 股份性质变更 1
     * 转让 2
     * 场内转板 5
     * 回收B
     * 销户C
     */

    @Test
    public void Test125()throws Exception{

        changeSHProperty(gdAccount1,gdEquityCode,500,0,1);

        shareTransfer(gdAccountKeyID1,gdAccount1,100,gdAccount5,0,
                gdEquityCode,0,"test202008280952");

        changeBoard(gdEquityCode,gdEquityCode + Random(5));

    }

    /***
     * 发行A
     * 股份性质变更 1
     * 增发 3
     * 冻结/解冻 4
     * 回收B
     * 销户C
     */

    @Test
    public void Test134()throws Exception{

        changeSHProperty(gdAccount1,gdEquityCode,500,0,1);

        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,0, shareList3);

        shareIncrease(gdEquityCode,shareList4);

        lockAndUnlock(bizNoTest,gdEquityCode,gdAccount1,500,0);
    }

    /***
     * 发行A
     * 股份性质变更 1
     * 增发 3
     * 场内转板 5
     * 回收B
     * 销户C
     */

    @Test
    public void Test135()throws Exception{

        changeSHProperty(gdAccount1,gdEquityCode,500,0,1);

        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,0, shareList3);

        shareIncrease(gdEquityCode,shareList4);

        changeBoard(gdEquityCode,gdEquityCode + Random(5));
    }


    /***
     * 发行A
     * 股份性质变更 1
     * 冻结/解冻 4
     * 场内转板 5
     * 回收B
     * 销户C
     */

    @Test
    public void Test145()throws Exception{

        changeSHProperty(gdAccount1,gdEquityCode,500,0,1);

        lockAndUnlock(bizNoTest,gdEquityCode,gdAccount1,500,0);

        changeBoard(gdEquityCode,gdEquityCode + Random(5));
    }

    /***
     * 发行A
     * 转让 2
     * 增发 3
     * 冻结/解冻 4
     * 回收B
     * 销户C
     */

    @Test
    public void Test234()throws Exception{

        shareTransfer(gdAccountKeyID1,gdAccount1,100,gdAccount5,0,
                gdEquityCode,1,"test202008280952");


        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,0, shareList3);

        shareIncrease(gdEquityCode,shareList4);

        lockAndUnlock(bizNoTest,gdEquityCode,gdAccount1,500,0);
    }

    /***
     * 发行A
     * 转让 2
     * 增发 3
     * 场内转板 5
     * 回收B
     * 销户C
     */

    @Test
    public void Test235()throws Exception{
        shareTransfer(gdAccountKeyID1,gdAccount1,100,gdAccount5,0,
                gdEquityCode,1,"test202008280952");

        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,0, shareList3);

        shareIncrease(gdEquityCode,shareList4);

        changeBoard(gdEquityCode,gdEquityCode + Random(5));
    }

    /***
     * 发行A
     * 增发 3
     * 冻结/解冻 4
     * 场内转板 5
     * 回收B
     * 销户C
     */

    @Test
    public void Test345()throws Exception{
        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,0, shareList3);

        shareIncrease(gdEquityCode,shareList4);

        lockAndUnlock(bizNoTest,gdEquityCode,gdAccount1,500,0);

        changeBoard(gdEquityCode,gdEquityCode + Random(5));
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

        changeSHProperty(gdAccount1,gdEquityCode,500,0,1);

        shareTransfer(gdAccountKeyID1,gdAccount1,100,gdAccount5,0,
                gdEquityCode,1,"test202008280952");


        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,0, shareList3);

        shareIncrease(gdEquityCode,shareList4);

        lockAndUnlock(bizNoTest,gdEquityCode,gdAccount1,500,0);
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
        changeSHProperty(gdAccount1,gdEquityCode,500,0,1);

        shareTransfer(gdAccountKeyID1,gdAccount1,100,gdAccount5,0,
                gdEquityCode,1,"test202008280952");


        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,0, shareList3);

        shareIncrease(gdEquityCode,shareList4);

        changeBoard(gdEquityCode,gdEquityCode + Random(5));

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

        changeSHProperty(gdAccount1,gdEquityCode,500,0,1);

        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,0, shareList3);

        shareIncrease(gdEquityCode,shareList4);

        lockAndUnlock(bizNoTest,gdEquityCode,gdAccount1,500,0);

        changeBoard(gdEquityCode,gdEquityCode + Random(5));
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
        changeSHProperty(gdAccount1,gdEquityCode,500,0,1);

        shareTransfer(gdAccountKeyID1,gdAccount1,100,gdAccount5,0,
                gdEquityCode,1,"test202008280952");


        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,0, shareList3);

        shareIncrease(gdEquityCode,shareList4);

        lockAndUnlock(bizNoTest,gdEquityCode,gdAccount1,500,0);

        changeBoard(gdEquityCode,gdEquityCode + Random(5));
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

        changeSHProperty(gdAccount1,gdEquityCode,500,0,1);

        changeBoard(gdEquityCode,gdEquityCode + Random(5));

        lockAndUnlock(bizNoTest,gdEquityCode,gdAccount1,500,0);

        shareTransfer(gdAccountKeyID1,gdAccount1,100,gdAccount5,0,
                gdEquityCode,1,"test202008280952");


        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,0, shareList3);

        shareIncrease(gdEquityCode,shareList4);
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
        changeBoard(gdEquityCode,gdEquityCode + Random(5));

        lockAndUnlock(bizNoTest,gdEquityCode,gdAccount1,500,0);


        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,0, shareList3);

        shareIncrease(gdEquityCode,shareList4);

        shareTransfer(gdAccountKeyID1,gdAccount1,100,gdAccount5,0,
                gdEquityCode,1,"test202008280952");

        changeSHProperty(gdAccount1,gdEquityCode,500,0,1);
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /***
     * 股份性质变更
     * @param eqCode    股权代码
     * @param address    变更账户
     * @param changeAmount  变更数量
     * @param oldProperty  变更前股权性质
     * @param newProperty  变更后股权性质
     * @throws Exception
     */
    public void changeSHProperty(String address,String eqCode,double changeAmount,int oldProperty,int newProperty)throws Exception{

        String response= gd.GDShareChangeProperty(gdPlatfromKeyID,address,eqCode,changeAmount,oldProperty,newProperty);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

    }


    /***
     * 过户转让
     * @param keyID  转出账户地址的keyID
     * @param fromAddr   转出地址
     * @param amount    转出数量
     * @param toAddr    转入地址
     * @param shareProperty  股权代码性质
     * @param eqCode  股权代码
     * @param txType  交易类型 （0：非交易过户，1：交易过户）
     * @param orderNo 委托编号
     * @throws Exception
     */
    public void shareTransfer(String keyID,String fromAddr,double amount,String toAddr,int shareProperty,String eqCode,int txType,String orderNo)throws Exception{
        int orderWay = 0;
        int orderType = 0;
        String price = "10000";
        String time = "20200828";
        String remark = "转账";
        String response= gd.GDShareTransfer(keyID,fromAddr,amount,toAddr,shareProperty,eqCode,txType,
                orderNo,orderWay,orderType,price,time,remark);

        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));
    }

    /***
     * 股份增发
     * @param eqCode  待增发股权代码
     * @param shareList  增发列表
     * @throws Exception
     */
    public void shareIncrease(String eqCode,List<Map> shareList)throws Exception{

        String reason = "股份分红";

        String response= gd.GDShareIncrease(gdContractAddress,gdPlatfromKeyID,eqCode,shareList,reason);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));
    }


    /***
     * 冻结解冻
     * @param bizNo  商务编号 唯一
     * @param eqCode  股权代码
     * @param address  待冻结账户
     * @param lockAmount    冻结数量
     * @param shareProperty     冻结股权性质
     * @throws Exception
     */
    public void lockAndUnlock(String bizNo,String eqCode,String address,double lockAmount,int shareProperty)throws Exception{

        String reason = "司法冻结";
        String cutoffDate = "20220930";

        String response= gd.GDShareLock(bizNo,address,eqCode,lockAmount,shareProperty,reason,cutoffDate);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        //解除冻结
        response= gd.GDShareUnlock(bizNo,eqCode,lockAmount);
        txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

    }

    /***
     * 场内转板接口
     * @param oldEquityCode  转前股权代码
     * @param newEquityCode  转后股权代码
     * @throws Exception
     */
    public void changeBoard(String oldEquityCode,String newEquityCode)throws Exception{

        String response= gd.GDShareChangeBoard(gdPlatfromKeyID,gdCompanyID,oldEquityCode,newEquityCode);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

        gdEquityCode = newEquityCode;
    }

    /***
     * 回收/减资
     * @param eqCode   回收的股权代码
     * @param shareList  回收地址账户列表
     * @throws Exception
     */
    public void recycleAcc(String eqCode ,List<Map> shareList) throws Exception {
        String remark = "777777";
        String response= gd.GDShareRecycle(gdPlatfromKeyID,eqCode,shareList,remark);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));
    }


    /***
     * 销户接口
     * @param clntNo   客户号
     * @throws Exception
     */
    public void destroyAcc(String clntNo) throws Exception {
        String response= gd.GDAccountDestroy(gdContractAddress,clntNo);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

    }
}
