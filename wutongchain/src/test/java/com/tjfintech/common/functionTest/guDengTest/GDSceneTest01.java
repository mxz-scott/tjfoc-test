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
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.CommonFunc.gdConstructShareList;
import static com.tjfintech.common.CommonFunc.getShareListFromQueryNoZeroAcc;
import static com.tjfintech.common.utils.UtilsClass.*;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class GDSceneTest01 {

    TestBuilder testBuilder= TestBuilder.getInstance();
    GuDeng gd =testBuilder.getGuDeng();
    Store store =testBuilder.getStore();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    public static String bizNoTest = "test" + Random(12);
    GDUnitFunc uf = new GDUnitFunc();
    public Boolean bCreateAccOnce = false;

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

    @After
    public void DestroyEquityAndAcc()throws Exception{
        List<Map> shareList = new ArrayList<>();
        Boolean bOnlyZero = false;

        //查询企业所有股东持股情况
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");

        //判断股权代码股东列表中是否只有回收地址存在额度
        //若有其他账户存在余额 则信息存入shareList中准备全部回收
        if(dataShareList.size() == 1 &&
                (JSONObject.fromObject(dataShareList.get(0)).getString("address").equals(zeroAccount)
                ||JSONObject.fromObject(dataShareList.get(0)).getString("address").equals(zeroAccount2))){
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


    @AfterClass
    public static void destroyAccAfterComplete(){

    }

    /***
     * 发行A
     * 股份性质变更 1
     * 回收B
     * 销户C
     */

    @Test
    public void Test1()throws Exception{
        uf.changeSHProperty(gdAccount1,gdEquityCode,500,0,1,true);
    }


    /***
     * 发行A
     * 转让 2 交易过户
     * 回收B
     * 销户C
     */

    @Test
    public void Test2_1()throws Exception{
        uf.shareTransfer(gdAccountKeyID1,gdAccount1,100,gdAccount5,0,
                gdEquityCode,1,"test202008280952",true);
    }

    /***
     * 发行A
     * 转让 2 非交易过户
     * 回收B
     * 销户C
     */

    @Test
    public void Test2_2()throws Exception{
        uf.shareTransfer(gdAccountKeyID1,gdAccount1,100,gdAccount5,0,
                gdEquityCode,0,"test202008280952",true);
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

        uf.shareIncrease(gdEquityCode,shareList4,true);

    }


    /***
     * 发行A
     * 冻结/解冻 4
     * 回收B
     * 销户C
     */

    @Test
    public void Test4()throws Exception{
        uf.lockAndUnlock(bizNoTest,gdEquityCode,gdAccount1,500,0,true);
    }


    /***
     * 发行A
     * 场内转板 5
     * 回收B
     * 销户C
     */

    @Test
    public void Test5()throws Exception{
        uf.changeBoard(gdEquityCode,gdEquityCode + Random(5),true);
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
        uf.changeSHProperty(gdAccount1,gdEquityCode,500,0,1,true);

        uf.shareTransfer(gdAccountKeyID1,gdAccount1,100,gdAccount5,0,
                gdEquityCode,1,"test202008280952",true);
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
        uf.shareTransfer(gdAccountKeyID1,gdAccount1,100,gdAccount5,0,
                gdEquityCode,1,"test202008280952",true);

        uf.changeSHProperty(gdAccount1,gdEquityCode,500,0,1,true);
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
        uf.changeSHProperty(gdAccount1,gdEquityCode,500,0,1,true);

        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,0, shareList3);
        uf.shareIncrease(gdEquityCode,shareList4,true);
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
        uf.shareIncrease(gdEquityCode,shareList4,true);

        uf.changeSHProperty(gdAccount1,gdEquityCode,500,0,1,true);
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
        uf.changeSHProperty(gdAccount1,gdEquityCode,500,0,1,true);
        uf.lockAndUnlock(bizNoTest,gdEquityCode,gdAccount1,500,0,true);
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
        uf.lockAndUnlock(bizNoTest,gdEquityCode,gdAccount1,500,0,true);
        uf.changeSHProperty(gdAccount1,gdEquityCode,500,0,1,true);
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
        uf.changeSHProperty(gdAccount1,gdEquityCode,500,0,1,true);
        uf.changeBoard(gdEquityCode,gdEquityCode + Random(5),true);
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
        uf.changeBoard(gdEquityCode,gdEquityCode + Random(5),true);
        uf.changeSHProperty(gdAccount1,gdEquityCode,500,0,1,true);
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
        uf.shareTransfer(gdAccountKeyID1,gdAccount1,100,gdAccount5,0,
                gdEquityCode,0,"test202008280952",true);

        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,0, shareList3);

        uf.shareIncrease(gdEquityCode,shareList4,true);
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

        uf.shareIncrease(gdEquityCode,shareList4,true);

        uf.shareTransfer(gdAccountKeyID1,gdAccount1,100,gdAccount5,0,
                gdEquityCode,1,"test202008280952",true);

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

        uf.shareTransfer(gdAccountKeyID1,gdAccount1,100,gdAccount5,0,
                gdEquityCode,1,"test202008280952",true);
        uf.lockAndUnlock(bizNoTest,gdEquityCode,gdAccount1,500,0,true);
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
        uf.lockAndUnlock(bizNoTest,gdEquityCode,gdAccount1,500,0,true);
        uf.shareTransfer(gdAccountKeyID1,gdAccount1,100,gdAccount5,0,
                gdEquityCode,0,"test202008280952",true);
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
        uf.shareTransfer(gdAccountKeyID1,gdAccount1,100,gdAccount5,0,
                gdEquityCode,1,"test202008280952",true);
        uf.changeBoard(gdEquityCode,gdEquityCode + Random(5),true);
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
        uf.changeBoard(gdEquityCode,gdEquityCode + Random(5),true);
        uf.shareTransfer(gdAccountKeyID1,gdAccount1,100,gdAccount5,0,
                gdEquityCode,0,"test202008280952",true);
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

        uf.shareIncrease(gdEquityCode,shareList4,true);

        uf.lockAndUnlock(bizNoTest,gdEquityCode,gdAccount1,500,0,true);
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
        uf.lockAndUnlock(bizNoTest,gdEquityCode,gdAccount1,500,0,true);

        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,0, shareList3);

        uf.shareIncrease(gdEquityCode,shareList4,true);
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
        uf.shareIncrease(gdEquityCode,shareList4,true);

        uf.changeBoard(gdEquityCode,gdEquityCode + Random(5),true);

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
        uf.changeBoard(gdEquityCode,gdEquityCode + Random(5),true);

        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,0, shareList3);
        uf.shareIncrease(gdEquityCode,shareList4,true);
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
        uf.lockAndUnlock(bizNoTest,gdEquityCode,gdAccount1,500,0,true);

        uf.changeBoard(gdEquityCode,gdEquityCode + Random(5),true);
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
        uf.changeBoard(gdEquityCode,gdEquityCode + Random(5),true);

        uf.lockAndUnlock(bizNoTest,gdEquityCode,gdAccount1,500,0,true);
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
        uf.changeSHProperty(gdAccount1,gdEquityCode,500,0,1,true);

        uf.shareTransfer(gdAccountKeyID1,gdAccount1,100,gdAccount5,0,
                gdEquityCode,1,"test202008280952",true);


        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,0, shareList3);

        uf.shareIncrease(gdEquityCode,shareList4,true);
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
        uf.changeSHProperty(gdAccount1,gdEquityCode,500,0,1,true);

        uf.shareTransfer(gdAccountKeyID1,gdAccount1,100,gdAccount5,0,
                gdEquityCode,1,"test202008280952",true);


        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,0, shareList3);

        uf.lockAndUnlock(bizNoTest,gdEquityCode,gdAccount1,500,0,true);
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

        uf.changeSHProperty(gdAccount1,gdEquityCode,500,0,1,true);

        uf.shareTransfer(gdAccountKeyID1,gdAccount1,100,gdAccount5,0,
                gdEquityCode,0,"test202008280952",true);

        uf.changeBoard(gdEquityCode,gdEquityCode + Random(5),true);

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

        uf.changeSHProperty(gdAccount1,gdEquityCode,500,0,1,true);

        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,0, shareList3);

        uf.shareIncrease(gdEquityCode,shareList4,true);

        uf.lockAndUnlock(bizNoTest,gdEquityCode,gdAccount1,500,0,true);
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

        uf.changeSHProperty(gdAccount1,gdEquityCode,500,0,1,true);

        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,0, shareList3);

        uf.shareIncrease(gdEquityCode,shareList4,true);

        uf.changeBoard(gdEquityCode,gdEquityCode + Random(5),true);
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

        uf.changeSHProperty(gdAccount1,gdEquityCode,500,0,1,true);

        uf.lockAndUnlock(bizNoTest,gdEquityCode,gdAccount1,500,0,true);

        uf.changeBoard(gdEquityCode,gdEquityCode + Random(5),true);
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

        uf.shareTransfer(gdAccountKeyID1,gdAccount1,100,gdAccount5,0,
                gdEquityCode,1,"test202008280952",true);


        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,0, shareList3);

        uf.shareIncrease(gdEquityCode,shareList4,true);

        uf.lockAndUnlock(bizNoTest,gdEquityCode,gdAccount1,500,0,true);
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
        uf.shareTransfer(gdAccountKeyID1,gdAccount1,100,gdAccount5,0,
                gdEquityCode,1,"test202008280952",true);

        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,0, shareList3);

        uf.shareIncrease(gdEquityCode,shareList4,true);

        uf.changeBoard(gdEquityCode,gdEquityCode + Random(5),true);
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

        uf.shareIncrease(gdEquityCode,shareList4,true);

        uf.lockAndUnlock(bizNoTest,gdEquityCode,gdAccount1,500,0,true);

        uf.changeBoard(gdEquityCode,gdEquityCode + Random(5),true);
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
                gdEquityCode,1,"test202008280952",true);


        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,0, shareList3);

        uf.shareIncrease(gdEquityCode,shareList4,true);

        uf.lockAndUnlock(bizNoTest,gdEquityCode,gdAccount1,500,0,true);
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
                gdEquityCode,1,"test202008280952",true);


        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,0, shareList3);

        uf.shareIncrease(gdEquityCode,shareList4,true);

        uf.changeBoard(gdEquityCode,gdEquityCode + Random(5),true);

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

        uf.lockAndUnlock(bizNoTest,gdEquityCode,gdAccount1,500,0,true);

        uf.changeBoard(gdEquityCode,gdEquityCode + Random(5),true);
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
                gdEquityCode,1,"test202008280952",true);


        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,0, shareList3);

        uf.shareIncrease(gdEquityCode,shareList4,true);

        uf.lockAndUnlock(bizNoTest,gdEquityCode,gdAccount1,500,0,true);

        uf.changeBoard(gdEquityCode,gdEquityCode + Random(5),true);
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

        uf.changeBoard(gdEquityCode,gdEquityCode + Random(5),true);

        uf.lockAndUnlock(bizNoTest,gdEquityCode,gdAccount1,500,0,true);

        uf.shareTransfer(gdAccountKeyID1,gdAccount1,100,gdAccount5,0,
                gdEquityCode,1,"test202008280952",true);


        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,0, shareList3);

        uf.shareIncrease(gdEquityCode,shareList4,true);
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
        uf.changeBoard(gdEquityCode,gdEquityCode + Random(5),true);

        uf.lockAndUnlock(bizNoTest,gdEquityCode,gdAccount1,500,0,true);


        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,0, shareList3);

        uf.shareIncrease(gdEquityCode,shareList4,true);

        uf.shareTransfer(gdAccountKeyID1,gdAccount1,100,gdAccount5,0,
                gdEquityCode,1,"test202008280952",true);

        uf.changeSHProperty(gdAccount1,gdEquityCode,500,0,1,true);
    }


    /***
     * 存在各种交易后场内转板
     */

    @Test
    public void changeBoard_AfterAllPro()throws Exception{
        uf.changeSHProperty(gdAccount1,gdEquityCode,500,0,1,true);

        uf.shareTransfer(gdAccountKeyID1,gdAccount1,100,gdAccount5,0,
                gdEquityCode,1,"test202008280952",true);


        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,0, shareList3);

        uf.shareIncrease(gdEquityCode,shareList4,true);

        uf.shareRecycle(gdEquityCode,shareList2,true);

        uf.lockAndUnlock(bizNoTest,gdEquityCode,gdAccount1,500,0,true);

        uf.changeBoard(gdEquityCode,gdEquityCode + Random(5),true);
    }


}
