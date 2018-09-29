package com.tjfintech.common.functionTest;

import com.tjfintech.common.MultiSign;
import com.tjfintech.common.SoloSign;
import com.tjfintech.common.Store;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static com.tjfintech.common.functionTest.StoreTest.SLEEPTIME;
import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@Slf4j
public class SoloTestInvalid {
    SoloSign soloSign = new SoloSign();
    Store store = new Store();
    MultiSign multiSign = new MultiSign();
    UtilsClass utilsClass = new UtilsClass();

    public static String tokenType;
    public static String tokenType2;

    @Before
    public void beforeConfig() throws Exception {
        log.info("发行两种token");
        tokenType = "SOLOTC-" + UtilsClass.Random(3);
        String issueInfo1=  soloSign.issueToken(PRIKEY1, tokenType, "100.123456789", tokenType);
        tokenType2 = "SOLOTC-" + UtilsClass.Random(3);
        String issueInfo2= soloSign.issueToken(PRIKEY1, tokenType2, "200.87654321", tokenType2);
        Thread.sleep(SLEEPTIME);
        assertThat(issueInfo1,containsString("200"));
        assertThat(issueInfo2,containsString("200"));
        log.info("查询归集地址中两种token余额");
        String response1 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        String response2 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType2);
        assertThat(tokenType + "查询余额错误", response1, containsString("200"));
        assertThat(tokenType + "查询余额错误", response2, containsString("200"));
        assertThat(tokenType + "查询余额不正确", response1, containsString("100.123456789"));
        assertThat(tokenType + "查询余额不正确", response2, containsString("200.87654321"));
        String transferData="归集地址向单签地址转账" ;
        List<Map> list0 = utilsClass.constructToken(ADDRESS1, tokenType, "100.123456789");
        List<Map> listInit = utilsClass.constructToken(ADDRESS1, tokenType2, "200.87654321");
        String transferInfo0=multiSign.Transfer(PRIKEY4,transferData,IMPPUTIONADD,list0);//1 归集地址向单签地址转账
        String transferInfoInit=multiSign.Transfer(PRIKEY4,transferData,IMPPUTIONADD,listInit);
        Thread.sleep(SLEEPTIME);
        assertThat(transferInfo0, containsString("200"));
        assertThat(transferInfoInit,containsString("200"));
        String queryInfo1=soloSign.Balance(PRIKEY1,tokenType);
        String queryInfo2=soloSign.Balance(PRIKEY1,tokenType2);
        assertThat(queryInfo1,containsString("200"));
        assertThat(queryInfo2,containsString("200"));
        assertThat(JSONObject.fromObject(queryInfo1).getJSONObject("Data").getString("Total"), containsString("100.123456789"));
        assertThat(JSONObject.fromObject(queryInfo2).getJSONObject("Data").getString("Total"), containsString("200.87654321"));
        log.info("----------------------------------------------------------------------");

    }

    /**
     * TC247发行token后, 再发行一笔存证交易，两笔交易的data字段相同
     */
    @Test
    public void TC247_issueThenStore() throws Exception {
        String response = store.CreateStore(tokenType);
        Thread.sleep(SLEEPTIME);
        String response2=store.CreateStore(tokenType);
        assertThat(response, containsString("200"));
        assertThat(response2, containsString("500"));
        assertThat(response2,containsString("Duplicate transaction"));



    }

    /**
     * TC248 转账金额非法测试
     * 1    转账金额为0
     * 2	转账金额为非数字字符
     * 3	转账金额为负数
     * 4	转账金额超过最大值
     * 5	转账token type不存在
     */
    @Test
    public void TC248_transferInvalid() throws Exception {
        String transferData = "单签地址向" + PUBKEY3 + "转账非法测试";


        List<Map> list1 = soloSign.constructToken(ADDRESS3, tokenType, "0");
        List<Map> list2 = soloSign.constructToken(ADDRESS3, tokenType, "abc");
        List<Map> list3 = soloSign.constructToken(ADDRESS3, tokenType, "-10");
        List<Map> list4 = soloSign.constructToken(ADDRESS3, tokenType, "92000000000000000000000000000000000000000000000000");
        List<Map> list5 = soloSign.constructToken(ADDRESS3, "nullToken", "100.25");
        log.info(transferData);


        String transferInfo1 = soloSign.Transfer(list1, PRIKEY1, transferData);
        String transferInfo2 = soloSign.Transfer(list2, PRIKEY1, transferData);
        String transferInfo3 = soloSign.Transfer(list3, PRIKEY1, transferData);
        String transferInfo4 = soloSign.Transfer(list4, PRIKEY1, transferData);
        String transferInfo5 = soloSign.Transfer(list5, PRIKEY1, transferData);

        Thread.sleep(SLEEPTIME);

        assertThat(transferInfo1, containsString("400"));
        assertThat(transferInfo2, containsString("400"));
        assertThat(transferInfo3, containsString("400"));
        assertThat(transferInfo4, containsString("400"));
        assertThat(transferInfo5, containsString("400"));
        assertThat(transferInfo1, containsString("Amount must be greater than 0 and less than 900000000"));
        assertThat(transferInfo2, containsString("Token amount must be a valid number and less than 900000000"));
        assertThat(transferInfo3, containsString("Token amount must be a valid number and less than 900000000"));
        assertThat(transferInfo4, containsString("Token amount must be a valid number and less than 900000000"));
        assertThat(transferInfo5, containsString("insufficient balance"));
    }

    /**
     * Tc 249发行token的非法测试
     * 1	发行token的金额超过允许的最大值
     * 2	发行token的金额为0
     * 3	发行token的金额为负数
     * 4	发行token的金额为非数字字符
     * 5	token type为空
     */
    @Test
    public void TC249_IssueTokenInvalid() {
        String tokenTypeInvalid = "SOLOTC-" + UtilsClass.Random(2);
        String issueInfo1 = soloSign.issueToken(PRIKEY1, tokenTypeInvalid, "900000000000000000000000000000", "发行token");
        String issueInfo2 = soloSign.issueToken(PRIKEY1, tokenTypeInvalid, "0", "发行token");
        String issueInfo3 = soloSign.issueToken(PRIKEY1, tokenTypeInvalid, "-140", "发行token");
        String issueInfo4 = soloSign.issueToken(PRIKEY1, tokenTypeInvalid, "abc", "发行token");
        String issueInfo5 = soloSign.issueToken(PRIKEY1, "", "1000", "发行token");
        assertThat(issueInfo1, containsString("400"));
        assertThat(issueInfo2, containsString("400"));
        assertThat(issueInfo3, containsString("400"));
        assertThat(issueInfo4, containsString("400"));
        assertThat(issueInfo5, containsString("400"));
        assertThat(issueInfo1, containsString("Amount must be greater than 0 and less than 900000000"));
        assertThat(issueInfo2, containsString("Amount must be greater than 0 and less than 900000000"));
        assertThat(issueInfo3, containsString("Amount must be greater than 0 and less than 900000000"));
        assertThat(issueInfo4, containsString("Amount must be greater than 0 and less than 900000000"));
        assertThat(issueInfo5,containsString("TokenType shouldn't be empty"));
        log.info("查询归集地址中token余额");
        String response1 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenTypeInvalid);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("0"));

    }

    /**
     * TC250用错误的公钥创建账号地址
     */
    @Test
    public void TC250_createAddressInvalid() {
        String response=soloSign.genAddress("123");
        assertThat(response,containsString("400"));
        assertThat(response,containsString("Public key must be base64 string"));
    }

    /**
     * TC251重复发行相同token
     */
    @Test
    public void TC251_issueDoubleInvalid() throws Exception {

        String issueInfo2 = soloSign.issueToken(PRIKEY1, tokenType, "1000", "发行token1");
        Thread.sleep(SLEEPTIME);
        String issueInfo3 = soloSign.issueToken(PRIKEY1, tokenType, "50", "发行token2");
        assertThat(issueInfo2, containsString("400"));
        assertThat(issueInfo3, containsString("400"));
        assertThat(issueInfo2,containsString("Token type "+tokenType+" has been issued"));
        assertThat(issueInfo3,containsString("Token type "+tokenType+" has been issued"));

        log.info("查询归集地址中token余额");
        String response1 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        assertThat(response1, containsString("200"));



    }

    /**
     * Tc252删除CA管理系统中的地址，确认不能发token
     */
    @Test
    public void TC252_issueDeleteInvalid() throws Exception {
        String tokenTypeInvalid = "SOLOTC-" + UtilsClass.Random(3);
        String issueInfo1 = soloSign.issueToken(PRIKEY4, tokenTypeInvalid, "100.1123", "发行token");
        Thread.sleep(SLEEPTIME);
        assertThat(issueInfo1, containsString("400"));
        assertThat(issueInfo1,containsString("tokenaddress verify failed"));


        log.info("查询归集地址中token余额");
        String response1 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenTypeInvalid);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("0"));


    }

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
        Thread.sleep(SLEEPTIME);
        assertThat(transferInfo1, containsString("400"));
        assertThat(transferInfo2, containsString("400"));
        assertThat(transferInfo1,containsString("invalid address"));
        assertThat(transferInfo2,containsString("Private key is mandatory"));

    }

    @After
    public void afterConfig() throws Exception {
        log.info("回收token------------------------------------------------------------------------------");
        String recycleInfo = multiSign.Recycle(PRIKEY1, tokenType, "100.123456789");
        String recycleInfo2 = multiSign.Recycle(PRIKEY1, tokenType2, "200.87654321");
        assertThat(recycleInfo, containsString("200"));
        assertThat(recycleInfo2, containsString("200"));
        Thread.sleep(SLEEPTIME);

        log.info("查询余额判断回收成功与否");
        String queryInfo = soloSign.Balance(PRIKEY1, tokenType);
        String queryInfo2 = soloSign.Balance(PRIKEY1, tokenType2);
        assertThat(queryInfo, containsString("200"));
        assertThat(queryInfo2, containsString("200"));
        assertThat(JSONObject.fromObject(queryInfo).getJSONObject("Data").getString("Total"), containsString("0"));
        assertThat(JSONObject.fromObject(queryInfo2).getJSONObject("Data").getString("Total"), containsString("0"));

    }


}
