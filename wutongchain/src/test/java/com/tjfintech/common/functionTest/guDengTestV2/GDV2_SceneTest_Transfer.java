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
 * 过户转让异常场景测试用例
 */
public class GDV2_SceneTest_Transfer {

    TestBuilder testBuilder= TestBuilder.getInstance();
    GuDeng gd =testBuilder.getGuDeng();
    Store store =testBuilder.getStore();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    public static String bizNoTest = "test" + Random(12);
    GDUnitFunc uf = new GDUnitFunc();

    @Rule
    public TestName tm = new TestName();

    @BeforeClass
    public static void Before()throws Exception{
        GDBeforeCondition gdBefore = new GDBeforeCondition();
        gdBefore.gdCreateAccout();
        register_event_type = 1;
    }

    @Before
    public void IssueEquity()throws Exception{
        bizNoTest = "test" + Random(12);
        gdEquityCode = "test_trf" + Random(8);
        register_event_type = 1;

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
    public void shareTransferWithErrorKeyId()throws Exception{

        String response1 = uf.shareTransfer(gdAccountKeyID4,gdAccount1,100,gdAccount5,0,
                gdEquityCode,false);
        assertEquals(true,JSONObject.fromObject(response1).getString("message").contains("signature verify failed"));
    }

    /***
     * 转让流通股 交易过户 双花测试
     */

    @Test
    public void shareTransferDoubleSpend_01()throws Exception{

        String response1 = uf.shareTransfer(gdAccountKeyID1,gdAccount1,100,gdAccount5,0,
                gdEquityCode,false);
        String txId1 = JSONObject.fromObject(response1).getJSONObject("data").getString("txId");

        //新版本接口走同步 可能不太容易出现double spend的情况
        String response2 = uf.shareTransfer(gdAccountKeyID1,gdAccount1,200,gdAccount6,0,
                gdEquityCode,false);
        if(JSONObject.fromObject(response2).getString("state").equals("200")){
//            String txId2 = JSONObject.fromObject(response2).getJSONObject("data").getString("txId");
//            sleepAndSaveInfo(SLEEPTIME/2);
//            //有时候会执行成功 有时候会失败 根据交易处理时机
//            assertEquals(true,JSONObject.fromObject(store.GetTxDetail(txId1)).getString("state").equals("200")
//                ^JSONObject.fromObject(store.GetTxDetail(txId2)).getString("state").equals("200"));
        }
        else {
            assertEquals(true,response2.contains("Err:double spend"));
        }
        gd.GDGetEnterpriseShareInfo(gdEquityCode);
    }

    /***
     * 转让流通股 非交易过户 双花测试
     */

    @Test
    public void shareTransferDoubleSpend_02()throws Exception{

        String response1 = uf.shareTransfer(gdAccountKeyID2,gdAccount2,100,gdAccount5,1,
                gdEquityCode,false);
        String txId1 = JSONObject.fromObject(response1).getJSONObject("data").getString("txId");
        String response2 = uf.shareTransfer(gdAccountKeyID2,gdAccount2,200,gdAccount6,1,
                gdEquityCode,false);
        if(JSONObject.fromObject(response2).getString("state").equals("200")) {
//            String txId2 = JSONObject.fromObject(response2).getJSONObject("data").getString("txId");
//            //新版本接口全部走同步 可能会出现double spend 以及成功两种场景
//            sleepAndSaveInfo(SLEEPTIME / 2);
//
//            //异或判断两种其中只有一个上链
//            assertEquals("200", JSONObject.fromObject(store.GetTxDetail(txId2)).getString("state"));
        }
        else{
            assertEquals(true,response2.contains("Err:double spend"));
        }
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId1)).getString("state"));
//        assertEquals(true,JSONObject.fromObject(store.GetTxDetail(txId1)).getString("state").equals("200")
//                ^JSONObject.fromObject(store.GetTxDetail(txId2)).getString("state").equals("200"));

        gd.GDGetEnterpriseShareInfo(gdEquityCode);
    }


    /***
     * 需求说明书中P18页举例实现
     * 支持部分转让
     */

    @Test
    public void shareTransfer_StoryEgTest()throws Exception{
        String response = "";


        gdEquityCode = "gdEC" + Random(12);
        List<Map> shareList = gdConstructShareList(gdAccount1,6000,0);
        uf.shareIssue(gdEquityCode,shareList,true);

        uf.changeSHProperty(gdAccount1,gdEquityCode,1000,0,1,true);



        //冻结流通股 *2000
        String bizNoTemp = "2000" + Random(12);
        uf.lock(bizNoTemp,gdAccount1,gdEquityCode,2000,0,"2022-09-03",true);

        //冻结流通股 *300
        bizNoTemp = "2000" + Random(12);
        uf.lock(bizNoTemp,gdAccount1,gdEquityCode,300,1,"2022-09-03",true);

        //交易过户 流通股股*3500
        response = uf.shareTransfer(gdAccountKeyID1,gdAccount1,3500,gdAccount5,0,
                gdEquityCode,false);
        assertEquals("500",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,JSONObject.fromObject(response).getString("message").contains(" Err:账户可用余额不足"));
//        sleepAndSaveInfo(SLEEPTIME);
//        assertEquals("404",JSONObject.fromObject(
//                store.GetTxDetail(JSONObject.fromObject(response).getJSONObject("data").getString("txId"))
//                                                                ).getString("state"));

        //非交易过户 高管股*500
        uf.shareTransfer(gdAccountKeyID1,gdAccount1,500,gdAccount5,1,
                gdEquityCode,true);

        //交易过户 流通股股*1000
        uf.shareTransfer(gdAccountKeyID1,gdAccount1,1000,gdAccount5,0,
                gdEquityCode,true);

        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));
        assertEquals(false,query.contains(
                "{\"amount\":2500,\"lockAmount\":0,\"shareProperty\":0,\"sharePropertyCN\":\"\",\"address\":\"" + zeroAccount + "\"}"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");

        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,4000,0,2000,mapShareENCN().get("0"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount1,500,1,300,mapShareENCN().get("1"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount5,1000,0,0,mapShareENCN().get("0"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount5,500,1,0,mapShareENCN().get("1"),respShareList);

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
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":4000,\"lockAmount\":2000}"));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":500,\"lockAmount\":300}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo3);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo4);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo5);
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo5 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount5 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":500,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo6);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

    }

    /***
     * 冻结部分后 转让流通股 交易过户
     * 支持部分转让
     */

    @Test
    public void shareTransfer_withLock_01()throws Exception{
        String response = "";
        //冻结流通股 *500
        String bizNoTemp = "2000" + Random(12);
        uf.lock(bizNoTemp,gdAccount1,gdEquityCode,500,0,"2022-09-03",true);

        //当前可用余额500 转出小于可用余额
        uf.shareTransfer(gdAccountKeyID1,gdAccount1,100,gdAccount5,0,
                gdEquityCode,true);

        //当前可用余额500 转出大于可用余额
        response = uf.shareTransfer(gdAccountKeyID1,gdAccount1,500,gdAccount5,0,
                gdEquityCode,false);

        assertEquals("500",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("Err:账户可用余额不足"));

//        sleepAndSaveInfo(SLEEPTIME);
//        assertEquals("404",JSONObject.fromObject(store.GetTxDetail(
//                JSONObject.fromObject(response).getJSONObject("data").getString("txId"))).getString("state"));


        //当前可用余额500 转出等于可用余额
        uf.shareTransfer(gdAccountKeyID1,gdAccount1,400,gdAccount5,0,
                gdEquityCode,true);

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
                gdEquityCode,false);

        assertEquals("500",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("Err:账户可用余额不足"));

//        sleepAndSaveInfo(SLEEPTIME);
//        assertEquals("404",JSONObject.fromObject(store.GetTxDetail(
//                JSONObject.fromObject(response).getJSONObject("data").getString("txId"))).getString("state"));

    }

    /***
     * 转让流通股超出余额
     */

    @Test
    public void shareTransfer_NotEnough()throws Exception{
        String response = "";
         //尝试转出超过余额
        response = uf.shareTransfer(gdAccountKeyID1,gdAccount1,10000,gdAccount5,0,
                gdEquityCode,false);

        assertEquals("500",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,JSONObject.fromObject(response).getString("message").contains("查询余额出错"));

    }

    /***
     * 转让账户没有的其他性质股份
     */

    @Test
    public void shareTransfer_NotEnough_02()throws Exception{
        String response = "";
        //尝试转出超过余额
        response = uf.shareTransfer(gdAccountKeyID1,gdAccount1,1000,gdAccount5,1,
                gdEquityCode,false);

        assertEquals("500",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,JSONObject.fromObject(response).getString("message").contains("查询余额出错"));
    }


    /***
     * 转让账户 引用错长度的交易报告对象标识
     */
    @Test
    public void shareTransfer_txType2ErrorTXObjectLenTest()throws Exception{
        String response = "";
        register_event_type = 2;
        GDBeforeCondition gdBF = new GDBeforeCondition();
        //交易报告数据
        Map txInfo = gdBF.init04TxInfo();
        String txRpObjId = "txReport" + Random(126);
        txInfo.put("transaction_object_id",txRpObjId);
        txInfo.put("transaction_type",2);//交易过户类型
        //登记数据
        String tempObjIdFrom = "reg" + mapAccAddr.get(gdAccount1).toString() + Random(3);
        String tempObjIdTo = "reg" + mapAccAddr.get(gdAccount5).toString() + Random(3);
        register_transaction_ref = txRpObjId;//登记引用的是交易报告的对象标识
        Map fromNow = gdBF.init05RegInfo();
        Map toNow = gdBF.init05RegInfo();
        fromNow.put("register_registration_object_id",tempObjIdFrom);
        toNow.put("register_registration_object_id",tempObjIdTo);

        response= gd.GDShareTransfer(gdAccountKeyID2,gdAccount2,100,gdAccount5,1,gdEquityCode,txInfo, fromNow,toNow);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));

        sleepAndSaveInfo(3000);

        //查询股东持股情况 无当前股权代码信息
        String query = gd.GDGetShareHolderInfo(gdContractAddress, gdAccClientNo5);
        assertEquals(gdAccClientNo5, JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));

        assertEquals(true, query.contains("\"shareholderNo\":\"SH" + gdAccClientNo5 + "\""));
        assertEquals(true, query.contains("\"address\":\"" + gdAccount5 + "\""));
        assertEquals(false, query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":100,\"lockAmount\":0}"));
    }

    /***
     * 转让账户 引用不存在的交易报告对象标识
     */
    @Test
    public void shareTransfer_txType2NotExistTXObjectTest()throws Exception{
        register_event_type = 2;
        String response = "";

        GDBeforeCondition gdBF = new GDBeforeCondition();
        //交易报告数据
        Map txInfo = gdBF.init04TxInfo();
        String txRpObjId = "txReport" + Random(12);
        txInfo.put("transaction_object_id",txRpObjId);
        txInfo.put("transaction_type",2);//交易过户类型
        //登记数据
        String tempObjIdFrom = "reg" + mapAccAddr.get(gdAccount1).toString() + Random(3);
        String tempObjIdTo = "reg" + mapAccAddr.get(gdAccount5).toString() + Random(3);
        register_transaction_ref = txRpObjId + "1111";//登记引用的是交易报告的对象标识
        Map fromNow = gdBF.init05RegInfo();
        Map toNow = gdBF.init05RegInfo();
        fromNow.put("register_registration_object_id",tempObjIdFrom);
        toNow.put("register_registration_object_id",tempObjIdTo);

        response= gd.GDShareTransfer(gdAccountKeyID2,gdAccount2,100,gdAccount5,1,gdEquityCode,txInfo, fromNow,toNow);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));

        sleepAndSaveInfo(3000);

        //查询股东持股情况 无当前股权代码信息
        String query = gd.GDGetShareHolderInfo(gdContractAddress, gdAccClientNo5);
        assertEquals(gdAccClientNo5, JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));

        assertEquals(true, query.contains("\"shareholderNo\":\"SH" + gdAccClientNo5 + "\""));
        assertEquals(true, query.contains("\"address\":\"" + gdAccount5 + "\""));
        assertEquals(false, query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":100,\"lockAmount\":0}"));
    }


    /***
     * 冻结后 转让非流通股 交易过户
     * 交易过户不支持非流通股转让 P18需求
     */

    @Test
    public void shareTransfer_txTypeTest()throws Exception{
        String response = "";

        log.info("股权代码过户转让非流通股");
        GDBeforeCondition gdBF = new GDBeforeCondition();
        //交易报告数据
        Map txInfo = gdBF.init04TxInfo();
        String txRpObjId = "txReport" + Random(6);
        txInfo.put("transaction_object_id",txRpObjId);
        txInfo.put("transaction_type",3);//交易过户类型
        //登记数据
        String tempObjIdFrom = "reg" + mapAccAddr.get(gdAccount1).toString() + Random(3);
        String tempObjIdTo = "reg" + mapAccAddr.get(gdAccount5).toString() + Random(3);
        register_transaction_ref = txRpObjId;//登记引用的是交易报告的对象标识
        Map fromNow = gdBF.init05RegInfo();
        Map toNow = gdBF.init05RegInfo();
        fromNow.put("register_registration_object_id",tempObjIdFrom);
        toNow.put("register_registration_object_id",tempObjIdTo);

        response= gd.GDShareTransfer(gdAccountKeyID2,gdAccount2,100,gdAccount5,1,gdEquityCode,txInfo, fromNow,toNow);

        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("交易过户非流通股不可以转让",JSONObject.fromObject(response).getString("message"));


        //交易报告数据
        txInfo = gdBF.init04TxInfo();
        txRpObjId = "txReport" + Random(6);
        txInfo.put("transaction_object_id",txRpObjId);
        txInfo.put("transaction_type",5);
        //登记数据
        tempObjIdFrom = "reg" + mapAccAddr.get(gdAccount1).toString() + Random(3);
        tempObjIdTo = "reg" + mapAccAddr.get(gdAccount5).toString() + Random(3);
        register_transaction_ref = txRpObjId;//登记引用的是交易报告的对象标识
        fromNow = gdBF.init05RegInfo();
        toNow = gdBF.init05RegInfo();
        fromNow.put("register_registration_object_id",tempObjIdFrom);
        toNow.put("register_registration_object_id",tempObjIdTo);
        response= gd.GDShareTransfer(gdAccountKeyID2,gdAccount2,100,gdAccount5,1,gdEquityCode,txInfo, fromNow,toNow);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));


        //交易报告数据
        txInfo = gdBF.init04TxInfo();
        txRpObjId = "txReport" + Random(6);
        txInfo.put("transaction_object_id",txRpObjId);
        txInfo.put("transaction_type",255);
        //登记数据
        tempObjIdFrom = "reg" + mapAccAddr.get(gdAccount1).toString() + Random(3);
        tempObjIdTo = "reg" + mapAccAddr.get(gdAccount5).toString() + Random(3);
        register_transaction_ref = txRpObjId;//登记引用的是交易报告的对象标识
        fromNow = gdBF.init05RegInfo();
        toNow = gdBF.init05RegInfo();
        fromNow.put("register_registration_object_id",tempObjIdFrom);
        toNow.put("register_registration_object_id",tempObjIdTo);
        txInformation.put("transaction_type",4);//配置为交易过户类型1
        response= gd.GDShareTransfer(gdAccountKeyID4,gdAccount4,100,gdAccount5,1,gdEquityCode,txInfo, fromNow,toNow);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
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
                gdEquityCode,true);

        //当前可用余额500 转出大于可用余额
        response = uf.shareTransfer(gdAccountKeyID1,gdAccount1,500,gdAccount5,0,
                gdEquityCode,false);

        assertEquals("500",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("Err:账户可用余额不足"));

//        sleepAndSaveInfo(SLEEPTIME);
//        assertEquals("404",JSONObject.fromObject(store.GetTxDetail(
//                JSONObject.fromObject(response).getJSONObject("data").getString("txId"))).getString("state"));

        //当前可用余额500 转出等于可用余额
        uf.shareTransfer(gdAccountKeyID1,gdAccount1,400,gdAccount5,0,
                gdEquityCode,true);
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
                gdEquityCode,true);

        //转出大于可用余额
        response = uf.shareTransfer(gdAccountKeyID2,gdAccount2,1000,gdAccount5,1,
                gdEquityCode,false);
        assertEquals("500",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,JSONObject.fromObject(response).getString("message").contains("查询余额出错"));


        //转出等于可用余额
        response = uf.shareTransfer(gdAccountKeyID2,gdAccount2,899,gdAccount5,1,
                gdEquityCode,true);
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
                gdEquityCode,true);
        uf.shareTransfer(gdAccountKeyID2,gdAccount2,1000,gdAccount5,1,
                gdEquityCode,true);


        //冻结高管股 * 100 流通股*100
        String bizNoTemp = "2000" + Random(12);
        uf.lock(bizNoTemp,gdAccount5,gdEquityCode,100,0,"2022-09-03",true);

        bizNoTemp = "2000" + Random(12);
        uf.lock(bizNoTemp,gdAccount5,gdEquityCode,100,1,"2022-09-03",true);


        //高管股转出小于可用余额
        response = uf.shareTransfer(gdAccountKeyID5,gdAccount5,100,gdAccount6,1,
                gdEquityCode,true);

        //高管股转出转出等于可用余额
        response = uf.shareTransfer(gdAccountKeyID5,gdAccount5,800,gdAccount6,1,
                gdEquityCode,true);



        //流通股转出小于可用余额
        uf.shareTransfer(gdAccountKeyID5,gdAccount5,100,gdAccount6,0,
                gdEquityCode,true);

        //高管股转出转出大于可用余额
        response = uf.shareTransfer(gdAccountKeyID5,gdAccount5,1000,gdAccount6,0,
                gdEquityCode,false);
        assertEquals("500",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,JSONObject.fromObject(response).getString("message").contains("查询余额出错"));

        //高管股转出转等大于可用余额
        uf.shareTransfer(gdAccountKeyID5,gdAccount5,800,gdAccount6,0,
                gdEquityCode,true);

    }

    /***
     * 多次转让
     */

    @Test
    public void multTransfer()throws Exception{

        String response = "";
        //发行
        gdEquityCode = "gdEC" + Random(12);
        List<Map> shareList = gdConstructShareList(gdAccount3,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount3,3000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,2000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount3,3000,0, shareList3);

        //发行
        uf.shareIssue(gdEquityCode, shareList4, true);

        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        JSONArray jsonArrayGet = JSONObject.fromObject(query).getJSONArray("data");
        for(int i = 0;i < 15; i++){
            log.info("tx time " + i);
            uf.shareTransfer(gdAccountKeyID3,gdAccount3,600,gdAccount5,0,gdEquityCode,
                    true);
            query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
            assertEquals("9000",getTotalAmountFromShareList(jsonArrayGet));
        }

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo5);
        assertEquals(gdAccClientNo5,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo5 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount5 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":9000,\"lockAmount\":0}"));

    }

    /***
     * 同一账户持有不同股权代码时 其中一个股权代码存在冻结，不影响其他股权代码变更股权性质
     */

    @Test
    public void shareTransfer_lockMatchEqcode()throws Exception{
        String EqCode1 = "trf021_" + Random(6);
        String EqCode2 = "trf022_" + Random(6);
        String EqCode3 = "trf023_" + Random(6);


        String response = "";

        gdEquityCode = EqCode1;
        List<Map> shareList21 = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList22 = gdConstructShareList(gdAccount2,1000,1, shareList21);
        List<Map> shareList23 = gdConstructShareList(gdAccount3,1000,0, shareList22);
        List<Map> shareList24 = gdConstructShareList(gdAccount4,1000,1, shareList23);
        uf.shareIssue(gdEquityCode,shareList24,true);

        gdEquityCode = EqCode2;
        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,1, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,1, shareList3);
        uf.shareIssue(gdEquityCode,shareList4,true);

        gdEquityCode = EqCode3;
        List<Map> shareList5 = gdConstructShareList(gdAccount5,1000,0);
        uf.shareIssue(gdEquityCode,shareList5,true);

        //冻结账户4 EqCode2 * 股权性质1 *100
        gdEquityCode = EqCode2;
        uf.lock(bizNoTest,gdAccount4,gdEquityCode,100,1,"2032-09-30",true);

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

        gdEquityCode = EqCode1;
        uf.shareTransfer(gdAccountKeyID4,gdAccount4,1000,gdAccount6,1,gdEquityCode,true);

        gdEquityCode = EqCode3;
        uf.shareTransfer(gdAccountKeyID5,gdAccount5,1000,gdAccount6,0,gdEquityCode,true);

        response = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo6);
        assertEquals(true,response.contains("{\"equityCode\":\"" + EqCode1 +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));
        assertEquals(true,response.contains("{\"equityCode\":\"" + EqCode3 +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));
    }


    /***
     * 股权代码大小写敏感性检查
     */

    @Test
    public void transfer_MatchCase()throws Exception{

        //大小写匹配检查
        String response = uf.shareTransfer(gdAccountKeyID1,gdAccount1,1000,gdAccount5,0,
                gdEquityCode.toLowerCase(),false);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("股权代码还未发行或者已经转场",JSONObject.fromObject(response).getString("message"));
//        assertEquals("500",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,JSONObject.fromObject(response).getString("message").contains("查询余额出错"));

        response = uf.shareTransfer(gdAccountKeyID1,gdAccount1,1000,gdAccount5,0,
                gdEquityCode.toUpperCase(),false);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("股权代码还未发行或者已经转场",JSONObject.fromObject(response).getString("message"));
//        assertEquals("500",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,JSONObject.fromObject(response).getString("message").contains("查询余额出错"));
    }
}
