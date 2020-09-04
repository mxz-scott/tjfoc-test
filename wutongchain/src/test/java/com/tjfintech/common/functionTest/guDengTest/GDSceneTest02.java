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
public class GDSceneTest02 {

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

    @After
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
     * 存在冻结的流通股 可用部分全部变更
     */

    @Test
    public void changeProperty_withLock_01()throws Exception{
        String response = "";
        //查询账户余额  总余额 1000

        //冻结流通股 *500
        String bizNoTemp = "2000" + Random(12);
        uf.lock(bizNoTemp,gdAccount1,gdEquityCode,500,0,"2022-09-03",true);

        //检查账户余额 总股权无变更

        //可用部分部分变更
        uf.changeSHProperty(gdAccount1,gdEquityCode,400,0,1,true);

        //检查账户余额 总股权无变更

        //变更部分超过可用余额
        response = uf.changeSHProperty(gdAccount1,gdEquityCode,200,0,1,false);
        sleepAndSaveInfo(5000);

        //检查账户余额 总股权无变更

        //变更剩下所有可用余额
        uf.changeSHProperty(gdAccount1,gdEquityCode,100,0,1,true);

        //检查账户余额 总股权无变更

        //无可用变更
        response = uf.changeSHProperty(gdAccount1,gdEquityCode,100,0,1,false);

        //检查账户余额 总股权无变更

        //解除冻结部分 Part1
        uf.unlock(bizNoTemp,gdEquityCode,400,true);

        //检查账户余额 总股权无变更

        //可用部分部分变更
        uf.changeSHProperty(gdAccount1,gdEquityCode,100,0,1,true);

        //检查账户余额 总股权无变更

        //变更部分超过可用余额
        response = uf.changeSHProperty(gdAccount1,gdEquityCode,400,0,1,false);
        sleepAndSaveInfo(5000);

        //检查账户余额 总股权无变更

        //变更剩下所有可用余额
        uf.changeSHProperty(gdAccount1,gdEquityCode,300,0,1,true);

        //检查账户余额 总股权无变更

        //解除所有冻结部分 Part1
        uf.unlock(bizNoTemp,gdEquityCode,100,true);

        //检查账户余额 总股权无变更

        //变更剩下所有可用余额
        uf.changeSHProperty(gdAccount1,gdEquityCode,100,0,1,true);

        //检查账户余额 总股权无变更
    }

    /***
     * 存在冻结的高管股 可用部分全部变更
     */

    @Test
    public void changeProperty_withLock_02()throws Exception{
        String response = "";
        //查询账户余额  总余额 1000

        //冻结流通股 *500
        String bizNoTemp = "2000" + Random(12);
        uf.lock(bizNoTemp,gdAccount2,gdEquityCode,500,1,"2022-09-03",true);

        //检查账户余额 总股权无变更

        //可用部分部分变更
        uf.changeSHProperty(gdAccount2,gdEquityCode,400,1,0,true);

        //检查账户余额 总股权无变更

        //变更部分超过可用余额
        response = uf.changeSHProperty(gdAccount2,gdEquityCode,200,1,0,false);
        sleepAndSaveInfo(5000);

        //检查账户余额 总股权无变更

        //变更剩下所有可用余额
        uf.changeSHProperty(gdAccount2,gdEquityCode,100,1,0,true);

        //检查账户余额 总股权无变更

        //无可用变更
        response = uf.changeSHProperty(gdAccount2,gdEquityCode,100,1,0,false);

        //检查账户余额 总股权无变更

        //解除冻结部分 Part1
        uf.unlock(bizNoTemp,gdEquityCode,400,true);

        //检查账户余额 总股权无变更

        //可用部分部分变更
        uf.changeSHProperty(gdAccount2,gdEquityCode,100,1,0,true);

        //检查账户余额 总股权无变更

        //变更部分超过可用余额
        response = uf.changeSHProperty(gdAccount2,gdEquityCode,400,1,0,false);
        sleepAndSaveInfo(5000);

        //检查账户余额 总股权无变更

        //变更剩下所有可用余额
        uf.changeSHProperty(gdAccount2,gdEquityCode,300,1,0,true);

        //检查账户余额 总股权无变更

        //解除所有冻结部分 Part1
        uf.unlock(bizNoTemp,gdEquityCode,100,true);

        //检查账户余额 总股权无变更

        //变更剩下所有可用余额
        uf.changeSHProperty(gdAccount2,gdEquityCode,100,1,0,true);

        //检查账户余额 总股权无变更
    }

    /***
     * 开户 销户 开户 销户
     */

    @Test
    public void dupCreateDestoryWithSameClientNo()throws Exception{
//        Map<String,String> accInfo = new HashMap<>();
        String clientNo = "abTest" + Random(12);

        //开户
        uf.createAcc(clientNo,gdEquityCode,true);

        //销户
        uf.destroyAcc(clientNo,true);

        //开户
        uf.createAcc(clientNo,gdEquityCode,true);

        //销户
        uf.destroyAcc(clientNo,true);

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
            assertEquals("501",JSONObject.fromObject(response).getString("state"));
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
            assertEquals("501",JSONObject.fromObject(response).getString("state"));
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
            assertEquals("501",JSONObject.fromObject(response).getString("state"));
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
     * 发行A
     * 冻结超出总余额
     * 回收B
     * 销户C
     */

    @Test
    public void lock()throws Exception{
        uf.lockAndUnlock(bizNoTest,gdEquityCode,gdAccount1,500,0,true);
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
        List<Map> shareList = gdConstructShareList(gdAccount1,100,1);
        uf.shareRecycle(gdEquityCode,shareList,false);

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

        if(JSONObject.fromObject(response).getString("state").equals("200")) {
            sleepAndSaveInfo(SLEEPTIME);
            assertEquals("404",JSONObject.fromObject(store.GetTxDetail(
                    JSONObject.fromObject(response).getJSONObject("data").getString("txId"))).getString("state"));
        }
        else{
            assertEquals("501",JSONObject.fromObject(response).getString("state"));
        }

        String query  = gd.GDGetEnterpriseShareInfo(newEquityCode);
        assertEquals("400",JSONObject.fromObject(query).getString("state"));
        assertEquals("股权代码还未发行",JSONObject.fromObject(query).getString("data"));

        query  = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));
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
        assertEquals(1,JSONObject.fromObject(query).getJSONArray("data").size());
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
        assertEquals(5,JSONObject.fromObject(query).getJSONArray("data").size());

        String newEquityCode = gdEquityCode + Random(5);
        response = uf.changeBoard(gdEquityCode,newEquityCode,true);
         //转板后检查 无回收信息
        query = gd.GDGetEnterpriseShareInfo(newEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));
        assertEquals(4,JSONObject.fromObject(query).getJSONArray("data").size());
    }


    /***
     * 账户存在余额时销户
     */

    @Test
    public void destroyAccWithBalance()throws Exception{

        String response = "";
        //回收一半数额
        List<Map> shareList = gdConstructShareList(gdAccount1,500,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,500,1, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,500,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,500,1, shareList3);

        uf.shareRecycle(gdEquityCode,shareList4,true);

        response = uf.destroyAcc(gdAccClientNo1,false);
        assertEquals("501",JSONObject.fromObject(response).getString("state"));
        assertEquals("账户还有余额，不可以销户",JSONObject.fromObject(response).getString("message"));

    }

    /***
     * 全部冻结后销户
     */

    @Test
    public void destroyAccWithAllLock()throws Exception{

        String response = "";
        //全部冻结
        String bizNoTemp = "2000" + Random(12);
        uf.lock(bizNoTemp,gdAccount1,gdEquityCode,1000,0,"2020-09-03",true);

        response = uf.destroyAcc(gdAccClientNo1,false);
        assertEquals("501",JSONObject.fromObject(response).getString("state"));
        assertEquals("账户还有余额，不可以销户",JSONObject.fromObject(response).getString("message"));

    }

    /***
     * 全部冻结后 在冻结有效期外转账
     */

    @Test
    public void transferOutOfCutOffDate()throws Exception{

        String response = "";
        //全部冻结
        String bizNoTemp = "2000" + Random(12);
        uf.lock(bizNoTemp,gdAccount1,gdEquityCode,1000,0,"2020-09-02",true);

        uf.shareTransfer(gdAccountKeyID1,gdAccount1,500,gdAccount6,0,gdEquityCode,0,"test123456",true);

    }
    
}
