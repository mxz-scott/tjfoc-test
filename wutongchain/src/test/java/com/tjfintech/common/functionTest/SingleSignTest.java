package com.tjfintech.common.functionTest;

import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.functionTest.StoreTest.SLEEPTIME;
import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SingleSignTest {
    TestBuilder testBuilder= TestBuilder.getInstance();
    MultiSign multiSign =testBuilder.getMultiSign();
    SoloSign soloSign = testBuilder.getSoloSign();
    UtilsClass utilsClass=new UtilsClass();
    private static String tokenType;
    private static String tokenType2;

    @Before
    public void beforeConfig() throws Exception {

        log.info("发行两种token");
        tokenType = "SOLOTC-"+UtilsClass.Random(6);
        String isResult= soloSign.issueToken(PRIKEY1,tokenType,"10000.123456789","发行token");
        log.info(isResult);
        tokenType2 = "SOLOTC-"+UtilsClass.Random(6);
        String isResult2= soloSign.issueToken(PRIKEY1,tokenType2,"20000.87654321","发行token");
        assertThat(tokenType+"发行token错误",isResult, containsString("200"));
        assertThat(tokenType+"发行token错误",isResult2, containsString("200"));

        Thread.sleep(SLEEPTIME);
        log.info("查询归集地址中两种token余额");
        String response1 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        String response2 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType2);
        assertThat(tokenType+"查询余额错误",response1, containsString("200"));
        assertThat(tokenType+"查询余额错误",response2, containsString("200"));
        assertThat(tokenType+"查询余额不正确",response1, containsString("10000.123456789"));
        assertThat(tokenType+"查询余额不正确",response2, containsString("20000.87654321"));
    }


    /**
     * Tc024单签正常流程:
     *
     */
    @Test
    public void TC024_SoloProgress() throws Exception {
        String transferData = "归集地址向" + PUBKEY3 + "转账100.25个" + tokenType+",并向"+PUBKEY4+"转账";
        log.info(transferData);
        List<Map> listModel = utilsClass.constructToken(ADDRESS3,tokenType,"100.25");
        log.info(ADDRESS3);
        List<Map> list=utilsClass.constructToken(ADDRESS5,tokenType2,"200.555",listModel);
        String transferInfo= multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD,list);
        Thread.sleep(SLEEPTIME);
        assertThat(transferInfo, containsString("200"));
        log.info("查询帐号3跟帐号5余额，判断转账是否成功");
        String queryInfo = soloSign.Balance( PRIKEY3, tokenType);
        String queryInfo2 = soloSign.Balance( PRIKEY5, tokenType2);
        assertThat(queryInfo, containsString("200"));
        assertThat(queryInfo, containsString("100.25"));
        assertThat(queryInfo2, containsString("200"));
        assertThat(queryInfo2, containsString("200.555"));

        log.info("使用地址查询帐号3跟帐号5余额，判断转账是否成功");
        String response3 = multiSign.BalanceByAddr(ADDRESS3, tokenType);
        String response4 = multiSign.BalanceByAddr(ADDRESS5, tokenType2);
        assertThat(response3, containsString("200"));
        assertThat(response3, containsString("100.25"));
        assertThat(response4, containsString("200"));
        assertThat(response4, containsString("200.555"));


        log.info("3向4转账token1");
        List<Map> list1 = soloSign.constructToken(ADDRESS4,tokenType,"30");
        String recycleInfo = soloSign.Transfer(list1, PRIKEY3,"3向4转账token1");
        assertThat(recycleInfo, containsString("200"));
        Thread.sleep(SLEEPTIME);
        List<Map> list2 = soloSign.constructToken(ADDRESS4,tokenType2,"80");
        String recycleInfo1 = soloSign.Transfer(list2, PRIKEY5,"5向4转账token2");
        assertThat(recycleInfo1, containsString("200"));
        Thread.sleep(SLEEPTIME);
        List<Map> list3 = soloSign.constructToken(ADDRESS2,tokenType,"30");
        List<Map>list4= soloSign.constructToken(ADDRESS2,tokenType2,"70",list3);
        String recycleInfo2 = soloSign.Transfer(list4, PRIKEY4, "李四向小六转账30 TT001, 70 TT002");
        assertThat(recycleInfo2, containsString("200"));
        Thread.sleep(SLEEPTIME);
        List<Map> list5 = soloSign.constructToken(ADDRESS2,tokenType2,"20");
        String recycleInfo3 = soloSign.Transfer(list5, PRIKEY5, "王五向小六转账20 TT002");
        assertThat(recycleInfo3, containsString("200"));
        Thread.sleep(SLEEPTIME);
        List<Map> list6 = (soloSign.constructToken(ADDRESS4,tokenType2,"30"));
        List<Map> list7= soloSign.constructToken(ADDRESS4,tokenType2,"50",list6);
        String recycleInfo4 = soloSign.Transfer(list7, PRIKEY2, "小六向李四转账80");
        log.info(recycleInfo4);
        assertThat(recycleInfo4, containsString("200"));
        Thread.sleep(SLEEPTIME);
        String queryInfo3TK1 = soloSign.Balance(PRIKEY3, tokenType);
        assertThat(queryInfo3TK1, containsString("70.25"));
        String Info1 = multiSign.Recycle("", PRIKEY3, tokenType, "70.25");
        assertThat(Info1, containsString("200"));
        log.info("帐号3，token1余额正确");
        String queryInfo4TK1 = soloSign.Balance(PRIKEY4, tokenType);
        assertThat(queryInfo4TK1, containsString("0"));
        log.info("帐号4，token1余额正确");
        String queryInfo4TK2 = soloSign.Balance(PRIKEY4, tokenType2);
        assertThat(queryInfo4TK2, containsString("90"));
        String Info2 = multiSign.Recycle("", PRIKEY4, tokenType2, "90");
        assertThat(Info2, containsString("200"));
        log.info("帐号4，token2余额正确");
        String queryInfo5TK2 = soloSign.Balance(PRIKEY5, tokenType2);
        assertThat(queryInfo5TK2, containsString("100.555"));
        String Info3 = multiSign.Recycle("", PRIKEY5, tokenType2, "100.555");
        assertThat(Info3, containsString("200"));
        log.info("帐号5，token2余额正确");
        String queryInfo6TK1 = soloSign.Balance(PRIKEY2, tokenType);
        assertThat(queryInfo6TK1, containsString("30"));
        String Info4 = multiSign.Recycle("", PRIKEY2, tokenType, "30");
        assertThat(Info4, containsString("200"));
        log.info("帐号6，token1余额正确");
        String queryInfo6TK2 = soloSign.Balance(PRIKEY2, tokenType2);
        assertThat(queryInfo6TK2, containsString("10"));
        String Info5 = multiSign.Recycle("", PRIKEY2, tokenType2, "10");
        assertThat(Info5, containsString("200"));
        log.info("帐号6，token2余额正确");
    }
    /**
     * Tc040单签转单签异常测试:
     *
     */
    @Test
    public void TC040_SoloProgress() throws Exception {
        String transferData = "归集地址向" + PUBKEY3 + "转账3000个" + tokenType+",并向"+PUBKEY4+"转账";
        log.info(transferData);
        List<Map> list=utilsClass.constructToken(ADDRESS3,tokenType,"3000");
        List<Map> list1=utilsClass.constructToken(ADDRESS3,tokenType2,"3000",list);
        String transferInfo= multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD,list1);
        Thread.sleep(SLEEPTIME);
        assertThat(transferInfo, containsString("200"));
        List<Map> list2 = soloSign.constructToken(ADDRESS4,tokenType,"4000");
        List<Map>list3= soloSign.constructToken(ADDRESS5,tokenType,"70",list2);
        String recycleInfo2 = soloSign.Transfer(list3, PRIKEY3, "李四向小六转账4000 TT001, 70 TT001");
        assertThat(recycleInfo2, containsString("insufficient balance"));
        Thread.sleep(SLEEPTIME);
        List<Map> list4 = soloSign.constructToken(ADDRESS4,tokenType,"4000");
        List<Map>list5= soloSign.constructToken(ADDRESS5,tokenType2,"4001",list4);
        String recycleInfo3 = soloSign.Transfer(list5, PRIKEY3, "李四向小六转账4000 TT001, 4001 TT002");
        assertThat(recycleInfo3, containsString("insufficient balance"));
        Thread.sleep(SLEEPTIME);
        List<Map> list6 = soloSign.constructToken(ADDRESS4,tokenType,"4000");
        List<Map>list7= soloSign.constructToken(ADDRESS5,tokenType2,"60",list6);
        String recycleInfo4 = soloSign.Transfer(list7, PRIKEY3, "李四向小六转账30 TT001, 60 TT002");
        assertThat(recycleInfo4, containsString("insufficient balance"));
        Thread.sleep(SLEEPTIME);
        String Info = multiSign.Recycle("", PRIKEY3, tokenType, "3000");
        String Info3 = multiSign.Recycle("", PRIKEY3, tokenType2, "3000");
        assertThat(Info, containsString("200"));
        assertThat(Info3, containsString("200"));
    }
    /**
     * Tc041单签转单签+多签异常测试:
     *
     */
    @Test
    public void TC041_SoloProgress() throws Exception {
        String transferData = "归集地址向" + PUBKEY3 + "转账3000个" + tokenType+",并向"+PUBKEY4+"转账";
        log.info(transferData);
        List<Map> list=utilsClass.constructToken(ADDRESS3,tokenType,"3000");
        List<Map> list1=utilsClass.constructToken(ADDRESS3,tokenType2,"3000",list);
        String transferInfo= multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD,list1);
        Thread.sleep(SLEEPTIME);
        assertThat(transferInfo, containsString("200"));
        List<Map> list2 = soloSign.constructToken(ADDRESS4,tokenType,"200");
        List<Map>list3= soloSign.constructToken(MULITADD5,tokenType,"7000",list2);
        String recycleInfo2 = soloSign.Transfer(list3, PRIKEY3, "李四向小六转账4000 TT001, 70 TT001");
        assertThat(recycleInfo2, containsString("insufficient balance"));
        Thread.sleep(SLEEPTIME);
        List<Map> list4 = soloSign.constructToken(ADDRESS4,tokenType,"4000");
        List<Map>list5= soloSign.constructToken(MULITADD5,tokenType2,"4001",list4);
        String recycleInfo3 = soloSign.Transfer(list5, PRIKEY3, "李四向小六转账4000 TT001, 4001 TT002");
        assertThat(recycleInfo3, containsString("insufficient balance"));
        Thread.sleep(SLEEPTIME);
        List<Map> list6 = soloSign.constructToken(ADDRESS4,tokenType,"4000");
        List<Map>list7= soloSign.constructToken(MULITADD5,tokenType2,"400",list6);
        String recycleInfo4 = soloSign.Transfer(list7, PRIKEY3, "李四向小六转账4000 TT001, 4001 TT002");
        assertThat(recycleInfo3, containsString("insufficient balance"));
        Thread.sleep(SLEEPTIME);
        log.info("开始回收....");
        String Info = multiSign.Recycle("", PRIKEY3, tokenType, "3000");
        String Info1 = multiSign.Recycle("", PRIKEY3, tokenType2, "3000");
        String Info2 = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType, "7000.123456789");
        String Info3 = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType2, "17000.87654321");
        assertThat(Info, containsString("200"));
        assertThat(Info1, containsString("200"));
        assertThat(Info2, containsString("200"));
        assertThat(Info3, containsString("200"));
        log.info("开始查询余额....");
        String response1 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        String response2 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType2);
        String response3 = soloSign.Balance( PRIKEY3, tokenType);
        String response4 = soloSign.Balance( PRIKEY3, tokenType2);
        assertThat(tokenType+"查询余额错误",response1, containsString("200"));
        assertThat(tokenType+"查询余额错误",response2, containsString("200"));
        assertThat(tokenType+"查询余额错误",response3, containsString("200"));
        assertThat(tokenType+"查询余额错误",response4, containsString("200"));
        assertThat(JSONObject.fromObject(response1).getJSONObject("Data").getString("Total"), containsString("0"));
        assertThat(JSONObject.fromObject(response2).getJSONObject("Data").getString("Total"), containsString("0"));
        assertThat(JSONObject.fromObject(response3).getJSONObject("Data").getString("Total"), containsString("0"));
        assertThat(JSONObject.fromObject(response4).getJSONObject("Data").getString("Total"), containsString("0"));

    }

    /**
     * Tc042单签转单签+多签测试:
     *
     */
    @Test
    public void TC042_SoloProgress() throws Exception {
        String transferData = "归集地址向" + "PUBKEY3" + "转账3000个" + "tokenType"+",并向"+"PUBKEY4"+"转账tokenType2";
        log.info(transferData);

        List<Map> list=utilsClass.constructToken(ADDRESS3,tokenType,"3000");
        List<Map> list1=utilsClass.constructToken(ADDRESS3,tokenType2,"3000",list);
        String transferInfo= multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD,list1);
        Thread.sleep(SLEEPTIME);
        assertThat(transferInfo, containsString("200"));

        List<Map> list2 = soloSign.constructToken(ADDRESS4,tokenType,"200");
        List<Map>list3= soloSign.constructToken(MULITADD5,tokenType,"70",list2);
        String recycleInfo2 = soloSign.Transfer(list3, PRIKEY3, "李四向小六转账4000 TT001, 70 TT001");
        assertThat(recycleInfo2, containsString("200"));
        Thread.sleep(SLEEPTIME);
        List<Map> list4 = soloSign.constructToken(ADDRESS4,tokenType,"400");
        List<Map>list5= soloSign.constructToken(MULITADD5,tokenType2,"401",list4);
        String recycleInfo3 = soloSign.Transfer(list5, PRIKEY3, "李四向小六转账4000 TT001, 4001 TT002");
        assertThat(recycleInfo3, containsString("200"));
        Thread.sleep(SLEEPTIME);
        log.info("开始回收....");
        String Info = multiSign.Recycle("", PRIKEY3, tokenType, "2330");
        String Info1 = multiSign.Recycle("", PRIKEY3, tokenType2, "2599");
        String Info2 = multiSign.Recycle("", PRIKEY4, tokenType, "600");
        String Info3 = multiSign.Recycle(MULITADD5, PRIKEY1, tokenType, "70");
        String Info4 = multiSign.Recycle(MULITADD5, PRIKEY3, tokenType2, "401");
        String Info5 = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType, "7000.123456789");
        String Info6 = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType2, "17000.87654321");

        assertThat(Info, containsString("200"));
        assertThat(Info1, containsString("200"));
        assertThat(Info2, containsString("200"));
        assertThat(Info3, containsString("200"));
        assertThat(Info4, containsString("200"));
        assertThat(Info5, containsString("200"));
        assertThat(Info6, containsString("200"));
        Thread.sleep(SLEEPTIME);
        log.info("开始查询余额....");
        String response1 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        String response2 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType2);
        String response3 = multiSign.Balance(MULITADD5, PRIKEY1, tokenType);
        String response4 = multiSign.Balance(MULITADD5, PRIKEY1, tokenType2);
        String response5 = soloSign.Balance( PRIKEY3, tokenType);
        String response6 = soloSign.Balance( PRIKEY3, tokenType2);
        String response7 = soloSign.Balance( PRIKEY4, tokenType);
        assertThat(tokenType+"查询余额错误",response1, containsString("200"));
        assertThat(tokenType+"查询余额错误",response2, containsString("200"));
        assertThat(tokenType+"查询余额错误",response3, containsString("200"));
        assertThat(tokenType+"查询余额错误",response4, containsString("200"));
        assertThat(tokenType+"查询余额错误",response5, containsString("200"));
        assertThat(tokenType+"查询余额错误",response6, containsString("200"));
        assertThat(tokenType+"查询余额错误",response7, containsString("200"));
        assertEquals(JSONObject.fromObject(response1).getJSONObject("Data").getString("Total").equals("0"),true);
        assertEquals(JSONObject.fromObject(response2).getJSONObject("Data").getString("Total").equals("0"),true);
        assertEquals(JSONObject.fromObject(response3).getJSONObject("Data").getString("Total").equals("0"),true);
        assertEquals(JSONObject.fromObject(response4).getJSONObject("Data").getString("Total").equals("0"),true);
        assertEquals(JSONObject.fromObject(response5).getJSONObject("Data").getString("Total").equals("0"),true);
        assertEquals(JSONObject.fromObject(response6).getJSONObject("Data").getString("Total").equals("0"),true);
        assertEquals(JSONObject.fromObject(response7).getJSONObject("Data").getString("Total").equals("0"),true);
    }

    /**
     * Tc244单签接口双花测试:
     *
     */
    @Test
    public void TC0244_SoloProgress() throws Exception {
        String transferData = "归集地址向" + PUBKEY3 + "转账3000个" + tokenType+",并向"+PUBKEY4+"转账";
        log.info(transferData);
        List<Map> list=utilsClass.constructToken(ADDRESS3,tokenType,"3000");
        String transferInfo= multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD,list);
        Thread.sleep(SLEEPTIME);
        List<Map> list1= soloSign.constructToken(ADDRESS4,tokenType,"300");
        String transferInfo1= soloSign.Transfer(list1, PRIKEY3, "双花验证");
        List<Map> list2= soloSign.constructToken(ADDRESS4,tokenType,"300");
        String transferInfo2= soloSign.Transfer(list2, PRIKEY3, "双花验证");
        assertThat(transferInfo1, containsString("200"));
        assertThat(transferInfo2, containsString("insufficient balance"));
        Thread.sleep(SLEEPTIME);
        List<Map> list3= soloSign.constructToken(ADDRESS4,tokenType,"300");
        String transferInfo3= soloSign.Transfer(list3, PRIKEY3, "双花验证");
        List<Map> list4= soloSign.constructToken(ADDRESS5,tokenType,"300");
        String transferInfo4= soloSign.Transfer(list4, PRIKEY3, "双花验证");
        assertThat(transferInfo3, containsString("200"));
        assertThat(transferInfo4, containsString("insufficient balance"));
        Thread.sleep(SLEEPTIME);
        List<Map> list5= soloSign.constructToken(ADDRESS4,tokenType,"300");
        String transferInfo5= soloSign.Transfer(list5, PRIKEY3, "双花验证");
        String transferInfo6= soloSign.Transfer(list5, PRIKEY3, "双花验证");
        assertThat(transferInfo5, containsString("200"));
        assertThat(transferInfo6, containsString("insufficient balance"));
}
}


/**
 * 单签测试用
 * 初始化-发行token至归集地址
 * @return  返回发行token的tokenType币种类型
 * @throws Exception  由于使用了线程的休眠需要抛出异常
 */
/**public  String SoloInit()throws Exception{
 String tokenType ="cx-"+UtilsClass.Random(6);
 log.info("\n发行Token\n");
 String response = TestissueToken(tokenType);
 JSONObject jsonObject = JSONObject.fromObject(response);
 String issueHash = jsonObject.getJSONObject("Data").get("Tx").toString();
 log.info("\n第一次签名\n");
 Thread.sleep(1000*3);
 String signResponse1 = TestSign(issueHash, PRIKEY1, PWD1);
 Thread.sleep(1000*3);
 JSONObject signJsonObject = JSONObject.fromObject(signResponse1);
 String signHash1 = signJsonObject.getJSONObject("Data").get("Tx").toString();
 log.info("\n第二次签名\n");
 String signResponse2 = TestSign(signHash1, PRIKEY2, PWD2);
 JSONObject signJsonObject2 = JSONObject.fromObject(signResponse2);
 String signHash2 = signJsonObject2.getJSONObject("Data").get("Tx").toString();
 log.info("\n第三次签名\n");
 TestSign(signHash2, PRIKEY3);
 Thread.sleep(1000*6);
 log.info("\n查询归集账号的余额\n");
 TestQuery(tokenType);
 return  tokenType;
 }
 */

/*@Test
     public void  run()throws Exception{
    StoreTest storeTest=new StoreTest();
    int count=0;
        //while (true)
         {runTest();
        storeTest.runSDK3Test();
        // Thread.sleep(1000*5);
         count++;
         log.info("第"+count+"次测试");
         }

     }*/
 /*   public void runTest() throws Exception {

        TOKENTYPE="cx-"+UtilsClass.Random(6);
        log.info("\n发行Token\n");
        String response = TestissueToken(TOKENTYPE);
        JSONObject jsonObject = JSONObject.fromObject(response);
        String issueHash = jsonObject.getJSONObject("Data").get("Tx").toString();
        log.info("\n第一次签名\n");
        Thread.sleep(1000*3);
        String signResponse1 = TestSign(issueHash, PRIKEY1, PWD1);
        Thread.sleep(1000*3);
        JSONObject signJsonObject = JSONObject.fromObject(signResponse1);
        String signHash1 = signJsonObject.getJSONObject("Data").get("Tx").toString();
        log.info("\n第二次签名\n");
        String signResponse2 = TestSign(signHash1, PRIKEY2, PWD2);
        JSONObject signJsonObject2 = JSONObject.fromObject(signResponse2);
        String signHash2 = signJsonObject2.getJSONObject("Data").get("Tx").toString();
        log.info("\n第三次签名\n");
        TestSign(signHash2, PRIKEY3);
        Thread.sleep(1000*6);
        log.info("\n查询归集账号的余额\n");
        TestQuery(TOKENTYPE);
        log.info("\n转账\n");
        TestTransfer(TOKENTYPE,USER_COLLET,USER1_2);
        Thread.sleep(1000*5);
        TestTransfer(TOKENTYPE,USER_COLLET,USER1_2, USER1_1);
        Thread.sleep(1000 * 5);
        log.info("\n查询账号余额,用户 user1_2:40 \n");
        TestUserQuery(USER1_2,PRIKEY3);
//        log.info("\n核对私钥正确性\n");
//        CheckPrikey(PRIKEY1,PWD1);
//        log.info("\n核对私钥错误性");
//        CheckPrikey(PRIKEY1,PWD2);
//        CheckPrikey(PRIKEY1,"");
//        log.info("----------------------------------------------------");
//        CheckPrikey(PRIKEY3,PWD2);
//        CheckPrikey("",PWD1);
    }*/