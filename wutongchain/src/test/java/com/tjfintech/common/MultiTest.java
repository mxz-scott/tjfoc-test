package com.tjfintech.common;

import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.*;

import static com.tjfintech.common.StoreTest.SLEEPTIME;
import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MultiTest {
    MultiSign multiSign = new MultiSign();

    @Test
    public void testGenMultiAddress() {
        int M = 3;
        Map<String, Object> map = new HashMap<>();
        map.put("1", PUBKEY1);
        map.put("2", PUBKEY2);
        map.put("3", PUBKEY3);
        String response = multiSign.genMultiAddress(M, map);
        assertThat(response, containsString("200"));
        assertThat(JSONObject.fromObject(response).getJSONObject("Data").getString("Address"), equalTo(MULITADD1));
    }

    @Test
    public void testBalance() throws Exception {
        String tokenType = "";
        String response = multiSign.Balance(MULITADD1, PRIKEY1, tokenType);
        // assertThat();

    }

    /**
     * Tc03多签正常流程-发币：签名：查询：转账：查询:回收：查询
     * 
     */
    @Test
    public void TC03_multiProgress() throws Exception {
        String tokenType = "cx-" + UtilsClass.Random(3);
        String amount = "1000";
        log.info(MULITADD1 + "发行" + tokenType + " token，数量为：" + amount);
        String data = MULITADD1 + "发行" + tokenType + " token，数量为：" + amount;
        String response = multiSign.issueToken(MULITADD2, tokenType, amount, data);
        assertThat(response, containsString("200"));
        String Tx1 = JSONObject.fromObject(response).getJSONObject("Data").getString("Tx");
        log.info("第一次签名");
        String response2 = multiSign.Sign(Tx1, PRIKEY1);
        String Tx2 = JSONObject.fromObject(response2).getJSONObject("Data").getString("Tx");
        log.info("第二次签名");
        String response3 = multiSign.Sign(Tx2, PRIKEY2);
        String Tx3 = JSONObject.fromObject(response3).getJSONObject("Data").getString("Tx");
        log.info("第三次签名");
        String response4 = multiSign.Sign(Tx3, PRIKEY6, PWD6);
        assertThat(JSONObject.fromObject(response4).getJSONObject("Data").getString("IsCompleted"), containsString("true"));
        assertThat(response4, containsString("200"));
        Thread.sleep(SLEEPTIME);
        log.info("查询归集地址中发行的token是否成功");
        String response5 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        assertThat(response5, containsString("200"));
        assertThat(response5, containsString("1000"));
        String transferData = "归集地址向" + MULITADD4 + "转账10个" + tokenType;
        log.info(transferData);
        Map<String, Object> map = new HashMap<>();
        List<Object> amountList = new ArrayList<>();
        Map<String, Object> amountmap = new HashMap<>();
        amountmap.put("TokenType", tokenType);
        amountmap.put("Amount", "10");
        amountList.add(amountmap);
        map.put("ToAddr", MULITADD4);
        map.put("AmountList", amountList);
        List<Object> tokenList = new ArrayList<>();
        tokenList.add(map);
        log.info("归集地址向MULITADD4转账10个token");
        String transferInfo = multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD, tokenList);
        Thread.sleep(SLEEPTIME);
        assertThat(transferInfo, containsString("200"));
        log.info("查询归集地址跟MULITADD4余额，判断转账是否成功");
        String queryInfo = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        String queryInfo2 = multiSign.Balance(MULITADD4, PRIKEY1, tokenType);
        assertThat(queryInfo, containsString("200"));
        assertThat(queryInfo, containsString("990"));
        assertThat(queryInfo2, containsString("200"));
        assertThat(queryInfo2, containsString("10"));
        log.info("回归归集地址跟MULITADD4的新发token");
        String recycleInfo = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType, "990");
        String recycleInfo2 = multiSign.Recycle(MULITADD4, PRIKEY1, tokenType, "10");
        assertThat(recycleInfo, containsString("200"));
        assertThat(recycleInfo2, containsString("200"));
        Thread.sleep(SLEEPTIME);
        log.info("查询回收后账户余额是否为0");
        String queryInfo3 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        String queryInfo4 = multiSign.Balance(MULITADD4, PRIKEY1, tokenType);
        assertThat(queryInfo3, containsString("200"));
        assertThat(JSONObject.fromObject(queryInfo3).getJSONObject("Data").getString("Total"), containsString("0"));
        assertThat(queryInfo4, containsString("200"));
        assertThat(JSONObject.fromObject(queryInfo4).getJSONObject("Data").getString("Total"), containsString("0"));


    }

    @Test
    public void testIssueToken() throws Exception {

    }

    /**
     * Method: Sign(String Tx, String Prikey, String Pwd)
     */
    @Test
    public void testSign() throws Exception {

    }

    /**
     * Method: Transfer(String PriKey, String Pwd, String Data, String MultiAddr, List<Map> TokenObject)
     */
    @Test
    public void testTransfer() throws Exception {
    }

    /**
     * Method: CheckPrikey(String PriKey, String Pwd)
     */
    @Test
    public void testCheckPriKey() throws Exception {
        String response = multiSign.CheckPriKey(PRIKEY6, PWD6);
        assertThat(response, containsString("200"));
        assertThat(response, containsString("This password match for the private key"));

    }

    /**
     * Method: Recycle(String multiAddr, String priKey, String Pwd, String tokenType, String amount)
     */
    @Test
    public void testRecycle() throws Exception {
//TODO: Test goes here...
    }

    /**
     * Method: QueryZero(String tokenType)
     */
    @Test
    public void testQueryZero() throws Exception {
//TODO: Test goes here...
    }
}
