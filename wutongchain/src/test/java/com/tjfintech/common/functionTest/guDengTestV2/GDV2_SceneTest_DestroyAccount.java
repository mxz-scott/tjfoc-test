package com.tjfintech.common.functionTest.guDengTestV2;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.GuDeng;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.*;
import org.junit.rules.TestName;
import org.junit.runners.MethodSorters;

import java.util.List;
import java.util.Map;

import static com.tjfintech.common.functionTest.guDengTestV2.GDCommonFunc.*;
import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassGD.*;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class GDV2_SceneTest_DestroyAccount {

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
        register_event_type = "1";
    }

    @Before
    public void IssueEquity()throws Exception{
//        bizNoTest = "test" + Random(12);
        gdEquityCode = "test_des" + Random(6);

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
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("账户还有余额，不可以销户",JSONObject.fromObject(response).getString("message"));

    }

    /***
     * 合约传入特殊字符
     */

    @Test
    public void destroyAccWitContractAddrSpecChar()throws Exception{
        String clientNo = "spec" + Random(10);
        uf.createAcc(clientNo,true);
        String name = "销户代理人姓名";
        String number = "销户代理人电话";
        String response = gd.GDAccountDestroy("@",clientNo,date1,getListFileObj(),date2,getListFileObj(),name,number);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("查询账号信息失败",JSONObject.fromObject(response).getString("message"));

        response = gd.GDAccountDestroy("#",clientNo,date1,getListFileObj(),date2,getListFileObj(),name,number);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("查询账号信息失败",JSONObject.fromObject(response).getString("message"));

        response = gd.GDAccountDestroy("%",clientNo,date1,getListFileObj(),date2,getListFileObj(),name,number);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("查询账号信息失败",JSONObject.fromObject(response).getString("message"));

        response = gd.GDAccountDestroy("_",clientNo,date1,getListFileObj(),date2,getListFileObj(),name,number);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("查询账号信息失败",JSONObject.fromObject(response).getString("message"));

        response = gd.GDAccountDestroy("|",clientNo,date1,getListFileObj(),date2,getListFileObj(),name,number);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("查询账号信息失败",JSONObject.fromObject(response).getString("message"));

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
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("账户还有余额，不可以销户",JSONObject.fromObject(response).getString("message"));

    }

    /***
     * 销户一个不存在的客户号 2472
     */

    @Test
    public void destroyNotExist()throws Exception{

        String response = uf.destroyAcc("testNotExist" + Random(6),false);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("客户号不存在",JSONObject.fromObject(response).getString("message"));
    }

    /***
     * TC2479 TC2480 TC2481 TC2482 TC2483 TC2475
     * 销户前存在发行交易流水，销户后恢复
     * 进行冻结、回收、转让、增发
     */

//    @Test
    public void repeatCreateDestroy()throws Exception{
        String clientNo = "test000DA" + Random(10);


        //创建账户
        String response = uf.createAcc(clientNo,true);
        log.info(response);
        String addr= JSONObject.fromObject(response).getJSONObject("data").getJSONObject("accountList").getString("address");
        List<Map> shareList = gdConstructShareList(addr,500,0);

        //发行
        String eqCode = "Da00" + Random(10);
        uf.shareIssue(eqCode,shareList,true);
        uf.shareRecycle(eqCode,shareList,true);

        //销户
        uf.destroyAcc(clientNo,true);
        String query = gd.GDGetEnterpriseShareInfo(eqCode);
        assertEquals(1,JSONObject.fromObject(query).getJSONArray("data").size());
        assertEquals(true,query.contains("{\"amount\":500,\"lockAmount\":0,\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"address\":\"" + zeroAccount + "\"}"));

        //再次使用相同的clientNo创建账户
        response = uf.createAcc(clientNo,true);
        String keyID = JSONObject.fromObject(response).getJSONObject("data").getJSONObject("accountList").getString("keyId");
        addr= JSONObject.fromObject(response).getJSONObject("data").getJSONObject("accountList").getString("address");

        //变更账户状态为正常 //当前无法变更
        eqCode = "Da00" + Random(10);

        //发行
        uf.shareIssue(eqCode,shareList,true);

        query = gd.GDGetShareHolderInfo(gdContractAddress,clientNo);
        assertEquals(clientNo,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + clientNo + "\""));
        assertEquals(true,query.contains("\"address\":\"" + addr + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + eqCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":500,\"lockAmount\":0}"));

//        //冻结 解冻
//        String bizNo = "test" + Random(12);
//        uf.lock(bizNo,addr,eqCode,1000,0,"2025-09-13",true);
//        uf.unlock(bizNo,eqCode,1000,true);
//
//        query = gd.GDGetShareHolderInfo(gdContractAddress,clientNo);
//        assertEquals(clientNo,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
//        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + clientNo + "\""));
//        assertEquals(true,query.contains("\"address\":\"" + addr + "\""));
//        assertEquals(true,query.contains("{\"equityCode\":\"" + eqCode +
//                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":500,\"lockAmount\":0}"));
//
//        String newEqCode = "new" + Random(10);
//        uf.changeBoard(eqCode,newEqCode,true);
//
//        query = gd.GDGetShareHolderInfo(gdContractAddress,clientNo);
//        assertEquals(clientNo,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
//        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + clientNo + "\""));
//        assertEquals(true,query.contains("\"address\":\"" + addr + "\""));
//        assertEquals(true,query.contains("{\"equityCode\":\"" + newEqCode +
//                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":500,\"lockAmount\":0}"));
//
//        uf.shareIncrease(newEqCode,shareList,true);
//
//        query = gd.GDGetShareHolderInfo(gdContractAddress,clientNo);
//        assertEquals(clientNo,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
//        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + clientNo + "\""));
//        assertEquals(true,query.contains("\"address\":\"" + addr + "\""));
//        assertEquals(true,query.contains("{\"equityCode\":\"" + newEqCode +
//                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));
        String newEqCode = eqCode;
        uf.shareTransfer(keyID,addr,200,gdAccount1,0,newEqCode,true);

        query = gd.GDGetShareHolderInfo(gdContractAddress,clientNo);
        assertEquals(clientNo,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + clientNo + "\""));
        assertEquals(true,query.contains("\"address\":\"" + addr + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + newEqCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":800,\"lockAmount\":0}"));

        shareList = gdConstructShareList(addr,800,0);
        shareList = gdConstructShareList(gdAccount1,200,0,shareList);
        uf.shareRecycle(newEqCode,shareList,true);

        response = uf.destroyAcc(clientNo,false);

        query = gd.GDGetShareHolderInfo(gdContractAddress,clientNo);

        response = uf.destroyAcc(clientNo,false);
        sleepAndSaveInfo(SLEEPTIME);
        assertEquals("404",JSONObject.fromObject(store.GetTxDetail(
                JSONObject.fromObject(response).getJSONObject("data").getString("txId"))).getString("state"));
    }

    /***
     * TC2479 TC2480 TC2481 TC2482 TC2483 TC2475
     * 销户前不存在任何交易流水，销户后恢复  目前会失败
     * 进行冻结、回收、转让、增发
     */

//    @Test
    public void repeatCreateDestroy02()throws Exception{
        String clientNo = "test000DA" + Random(10);

        //创建账户
        uf.createAcc(clientNo,true);

        //销户
        uf.destroyAcc(clientNo,true);

        //再次使用相同的clientNo创建账户
        String response = uf.createAcc(clientNo,true);
        String keyID = JSONObject.fromObject(response).getJSONObject("data").getJSONObject("accountList").getString("keyId");
        String addr= JSONObject.fromObject(response).getJSONObject("data").getJSONObject("accountList").getString("address");

        //变更账户状态为正常


        String eqCode = "Da00" + Random(10);
        List<Map> shareList = gdConstructShareList(addr,500,0);
        //发行
        uf.shareIssue(eqCode,shareList,true);

        String query = gd.GDGetShareHolderInfo(gdContractAddress,clientNo);
        assertEquals(clientNo,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + clientNo + "\""));
        assertEquals(true,query.contains("\"address\":\"" + addr + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + eqCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":500,\"lockAmount\":0}"));

        uf.lockAndUnlock("r"+Random(15),eqCode,addr,500,0);

        query = gd.GDGetShareHolderInfo(gdContractAddress,clientNo);
        assertEquals(clientNo,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + clientNo + "\""));
        assertEquals(true,query.contains("\"address\":\"" + addr + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + eqCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":500,\"lockAmount\":0}"));

        String newEqCode = "new" + Random(10);
        uf.changeBoard(eqCode,newEqCode,true);

        query = gd.GDGetShareHolderInfo(gdContractAddress,clientNo);
        assertEquals(clientNo,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + clientNo + "\""));
        assertEquals(true,query.contains("\"address\":\"" + addr + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + newEqCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":500,\"lockAmount\":0}"));

        uf.shareIncrease(newEqCode,shareList,true);

        query = gd.GDGetShareHolderInfo(gdContractAddress,clientNo);
        assertEquals(clientNo,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + clientNo + "\""));
        assertEquals(true,query.contains("\"address\":\"" + addr + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + newEqCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

        uf.shareTransfer(keyID,addr,200,gdAccount1,0,newEqCode,true);

        query = gd.GDGetShareHolderInfo(gdContractAddress,clientNo);
        assertEquals(clientNo,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + clientNo + "\""));
        assertEquals(true,query.contains("\"address\":\"" + addr + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + newEqCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":800,\"lockAmount\":0}"));

        shareList = gdConstructShareList(addr,800,0);
        shareList = gdConstructShareList(gdAccount1,200,0,shareList);
        uf.shareRecycle(newEqCode,shareList,true);

        response = uf.destroyAcc(clientNo,false);

        query = gd.GDGetShareHolderInfo(gdContractAddress,clientNo);

        response = uf.destroyAcc(clientNo,false);
        sleepAndSaveInfo(SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(
                JSONObject.fromObject(response).getJSONObject("data").getString("txId"))).getString("state"));
    }

}
