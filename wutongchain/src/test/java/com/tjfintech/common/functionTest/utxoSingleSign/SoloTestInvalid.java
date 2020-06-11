package com.tjfintech.common.functionTest.utxoSingleSign;


import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;

import java.util.List;
import java.util.Map;

//import static com.tjfintech.common.functionTest.store.StoreTest.SLEEPTIME;
import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@Slf4j
public class SoloTestInvalid {
    TestBuilder testBuilder= TestBuilder.getInstance();
    MultiSign multiSign =testBuilder.getMultiSign();
    SoloSign soloSign=testBuilder.getSoloSign();
    Store store=testBuilder.getStore();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();

    private static String tokenType;
    private static String tokenType2;

    private static String issueAmount1;
    private static String issueAmount2;

    private static String actualAmount1;
    private static String actualAmount2;

    @Before
    //@Test
    public void beforeConfig() throws Exception {
        if(MULITADD1.isEmpty()) {
            BeforeCondition bf = new BeforeCondition();
            bf.updatePubPriKey();
            bf.collAddressTest();
        }

        issueAmount1 = "100.12345678912345";
        issueAmount2 = "200.876543212345";

        if (UtilsClass.PRECISION == 10) {
            actualAmount1 = "100.1234567891";
            actualAmount2 = "200.8765432123";
        }else {
            actualAmount1 = "100.123456";
            actualAmount2 = "200.876543";
        }

        log.info("发行两种token");
        log.info(ADDRESS1);
        log.info(PRIKEY1);
        tokenType = "SOLOTC-" + UtilsClass.Random(6);
        String issueInfo1=  soloSign.issueToken(PRIKEY1, tokenType, issueAmount1, tokenType,ADDRESS1);
        //Thread.sleep(SLEEPTIME);
        tokenType2 = "SOLOTC-" + UtilsClass.Random(6);
        String issueInfo2= soloSign.issueToken(PRIKEY1, tokenType2, issueAmount2, tokenType2,ADDRESS1);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        assertThat(issueInfo1,containsString("200"));
        assertThat(issueInfo2,containsString("200"));
        log.info("查询归集地址中两种token余额");
        String response1 = soloSign.Balance(PRIKEY1, tokenType);
        String response2 = soloSign.Balance(PRIKEY1, tokenType2);
        assertThat(tokenType + "查询余额错误", response1, containsString("200"));
        assertThat(tokenType + "查询余额错误", response2, containsString("200"));
        assertThat(tokenType + "查询余额不正确", response1, containsString(actualAmount1));
        assertThat(tokenType + "查询余额不正确", response2, containsString(actualAmount2));
    }

    /**
     * TC247发行token后, 再发行一笔存证交易，两笔交易的data字段相同
     */
    @Test
    public void TC247_issueThenStore() throws Exception {
        String response = store.CreateStore(tokenType);
        Thread.sleep(1*1000);
        String response2= store.CreateStore(tokenType);
        assertThat(response, containsString("200"));
        assertThat(response2, containsString("500"));
        assertThat(response2,containsString("Duplicate transaction"));

    }





    /**
     * TC250用错误的公钥创建账号地址
     */
    @Test
    public void TC250_createAddressInvalid() {
        String response= soloSign.genAddress("123");
        assertThat(response,containsString("400"));
        assertThat(response,containsString("Public key must be base64 string"));
    }

    /**
     * TC251重复发行相同token
     */
    @Test
    public void TC251_issueDoubleInvalid() throws Exception {

        String issueInfo2 = soloSign.issueToken(PRIKEY1, tokenType, "1000", "发行token1",ADDRESS1);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


         String issueInfo3 = soloSign.issueToken(PRIKEY1, tokenType, "50", "发行token2",ADDRESS1);
//        assertThat(issueInfo2, containsString("400"));
//        assertThat(issueInfo3, containsString("400"));
        assertThat(issueInfo2,containsString("Token type "+tokenType+" has been issued"));
        assertThat(issueInfo3,containsString("Token type "+tokenType+" has been issued"));

        log.info("查询归集地址中token余额");
        String response1 = soloSign.Balance(PRIKEY1, tokenType);
        assertThat(response1, containsString("200"));



    }

    /**
     * Tc252删除CA管理系统中的地址，确认不能发token
     */
    //暂不支持
//    @Test
//    public void TC252_issueDeleteInvalid() throws Exception {
//        String tokenTypeInvalid = "SOLOTC-" + UtilsClass.Random(3);
//        String issueInfo1 = soloSign.issueToken(PRIKEY1, tokenTypeInvalid, "100.1123", "发行token",ADDRESS1);
//        Thread.sleep(SLEEPTIME);
//        assertThat(issueInfo1, containsString("400"));
//        assertThat(issueInfo1,containsString("tokenaddress verify failed"));
//
//
//        log.info("查询归集地址中token余额");
//        String response1 = multiSign.Balance(PRIKEY1, tokenTypeInvalid);
//        assertThat(response1, containsString("200"));
//        assertThat(response1, containsString("0"));
//
//
//    }

    /**
     * Tc253 向不存在的账号地址转账，从不存在的私钥转出
     */

    @Test
    public void TC253_transferAddInvalid() throws Exception {
        String transferData = "单签地址向" + "空地址" + "转账非法测试";
        List<Map> list1 = soloSign.constructToken("null", tokenType, "100");
        List<Map> list2 = soloSign.constructToken(ADDRESS3, tokenType, "10.123");
        log.info(transferData);
        String transferInfo1 = soloSign.Transfer(list1, PRIKEY1, transferData);
        String transferInfo2 = soloSign.Transfer(list2, "null", transferData);
//        assertThat(transferInfo1, containsString("400"));
//        assertThat(transferInfo2, containsString("400"));
        assertThat(transferInfo1,containsString("invalid address"));
        assertThat(transferInfo2,containsString("Private key is mandatory"));

    }
//新增其他异常用例余额可能有变更 暂时先移除after回收token操作 20200319
//    @After
    public void afterConfig() throws Exception {
        log.info("回收token------------------------------------------------------------------------------");
        String recycleInfo = soloSign.Recycle(PRIKEY1, tokenType, "100.123456789");
        String recycleInfo2 = soloSign.Recycle(PRIKEY1, tokenType2, "200.87654321");
        assertThat(recycleInfo, containsString("200"));
        assertThat(recycleInfo2, containsString("200"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        log.info("查询余额判断回收成功与否");
        String queryInfo = soloSign.Balance(PRIKEY1, tokenType);
        String queryInfo2 = soloSign.Balance(PRIKEY1, tokenType2);
        assertThat(queryInfo, containsString("200"));
        assertThat(queryInfo2, containsString("200"));
        assertThat(JSONObject.fromObject(queryInfo).getJSONObject("Data").getString("Total"), containsString("0"));
        assertThat(JSONObject.fromObject(queryInfo2).getJSONObject("Data").getString("Total"), containsString("0"));

    }

    //发行时大小写敏感性检查
//    @Test
    public void issueTokenIgnoreCase()throws Exception{
        String issueResp = soloSign.issueToken(PRIKEY1,tokenType.toLowerCase(),
                "100","发行已有tokentype字符全部小写的token","");
        assertEquals("200",JSONObject.fromObject(issueResp).getString("state"));

        String issueResp2 = soloSign.issueToken(PRIKEY1,tokenType.toUpperCase(),
                "100","发行已有tokentype字符全部大写的token","");
        assertEquals("200",JSONObject.fromObject(issueResp2).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        assertEquals("100",commonFunc.GetBalance(ADDRESS1,tokenType.toLowerCase()));
        assertEquals("100",commonFunc.GetBalance(ADDRESS1,tokenType.toUpperCase()));

    }

    //tokenType大小写敏感性检查
//    @Test
    public void testMatchCaseQueryBalance()throws Exception{

        //查询余额账户地址大小写敏感性检查  当前不敏感
        log.info("查询余额tokentype敏感检查");
        //查询余额tokentype敏感检查
        assertEquals("0",commonFunc.GetBalance(ADDRESS1,tokenType.toUpperCase()));
        assertEquals("0",commonFunc.GetBalance(ADDRESS1,tokenType.toLowerCase()));

    }

    //    @Test
    public void testMatchCaseTransfer()throws Exception{

        //转账检查大小写敏感
        //检查小写tokentype转账
        log.info("转账检查大小写敏感");
        List<Map> list = soloSign.constructToken(ADDRESS3,tokenType.toLowerCase(),"10");
        String transferInfo = soloSign.Transfer(list,PRIKEY1,"转账全小写tokentype");
        assertEquals("400",JSONObject.fromObject(transferInfo).getString("state"));
        assertEquals("Insufficient Balance",JSONObject.fromObject(transferInfo).getString("Data"));

        //检查小写tokentype转账
        list = soloSign.constructToken(ADDRESS3,tokenType.toUpperCase(),"10");
        transferInfo = soloSign.Transfer(list,PRIKEY1,"转账全小写tokentype");
        assertEquals("400",JSONObject.fromObject(transferInfo).getString("state"));
        assertEquals("Insufficient Balance",JSONObject.fromObject(transferInfo).getString("Data"));
    }

    //    @Test
    public void testMatchCaseDestroy()throws Exception{
        List<Map> list;
        //回收检查大小写敏感
        log.info("回收检查大小写敏感");

        String desResp = multiSign.Recycle("",PRIKEY1,tokenType.toLowerCase(),"10");
        assertEquals("400",JSONObject.fromObject(desResp).getString("state"));
        assertEquals("Insufficient Balance",JSONObject.fromObject(desResp).getString("Data"));

        desResp = multiSign.Recycle("",PRIKEY1,tokenType.toUpperCase(),"10");
        assertEquals("400",JSONObject.fromObject(desResp).getString("state"));
        assertEquals("Insufficient Balance",JSONObject.fromObject(desResp).getString("Data"));
    }
}
