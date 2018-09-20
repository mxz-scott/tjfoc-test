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
    UtilsClass utilsClass=new UtilsClass();

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
        String tokenType=IssueToken(3,"1000");
        Thread.sleep(SLEEPTIME);

        log.info("查询归集地址中发行的token是否成功");
        String response5 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        assertThat(response5, containsString("200"));
        assertThat(response5, containsString("1000"));
        String transferData = "归集地址向" + MULITADD4 + "转账10个" + tokenType;

        log.info(transferData);
        List<Map>list=utilsClass.constructToken(MULITADD4,tokenType,"10");
        log.info(transferData);
        String transferInfo=multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD,list);
        Thread.sleep(SLEEPTIME);
        assertThat(transferInfo, containsString("200"));

        log.info("查询归集地址跟MULITADD4余额，判断转账是否成功");
        String queryInfo = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        String queryInfo2 = multiSign.Balance(MULITADD4, PRIKEY1, tokenType);
        assertThat(queryInfo, containsString("200"));
        assertThat(queryInfo, containsString("990"));
        assertThat(queryInfo2, containsString("200"));
        assertThat(queryInfo2, containsString("10"));

        log.info("回收归集地址跟MULITADD4的新发token");
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

    /**
     *TC19归集地址向两个多签地址转账
     * 发两种币-查询归集地址-转账两个地址不同token-查询-回收-查询
     * @throws Exception
     */
     @Test
     public void TC19_transferMulti()throws  Exception{
         log.info("发行两种token1000个");
         String tokenType=IssueToken(2,"1000");
         String tokenType2=IssueToken(3,"1000");
         Thread.sleep(SLEEPTIME);

         log.info("查询归集地址中两种token余额");
         String response1 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
         String response2 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType2);
         assertThat(response1, containsString("200"));
         assertThat(response1, containsString("1000"));
         assertThat(response2, containsString("200"));
         assertThat(response2, containsString("1000"));
         String transferData = "归集地址向" + "ADDRESS1" + "转账10个" + tokenType+"归集地址向" + "MULITADD4" + "转账10个" + tokenType;
         List<Map>list=utilsClass.constructToken(MULITADD4,tokenType,"10");
         List<Map>list2=utilsClass.constructToken(MULITADD5,tokenType2,"10",list);
         List<Map>list3=utilsClass.constructToken(MULITADD5,tokenType,"10",list);
         log.info(transferData);
         String transferInfo=multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD,list2);//不同币种
         Thread.sleep(SLEEPTIME); //UTXO关系，两笔交易之间需要休眠
         String transferInfo2=multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD,list3);//相同币种
         assertThat(transferInfo,containsString("200"));
         assertThat(transferInfo2,containsString("200"));
         Thread.sleep(SLEEPTIME);

         log.info("查询余额判断转账是否成功");
         String queryInfo=multiSign.Balance(MULITADD4,PRIKEY1,tokenType);
         String queryInfo2=multiSign.Balance(MULITADD5,PRIKEY1,tokenType2);
         assertThat(queryInfo,containsString("200"));
         assertThat(queryInfo2,containsString("200"));
         assertThat(JSONObject.fromObject(queryInfo).getJSONObject("Data").getString("Total"), containsString("20"));
         assertThat(JSONObject.fromObject(queryInfo2).getJSONObject("Data").getString("Total"), containsString("10"));

         log.info("回收Token");
         String recycleInfo = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType, "970");
         String recycleInfo2 = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType2, "990");
         String recycleInfo3 = multiSign.Recycle(MULITADD4, PRIKEY1, tokenType, "20");
         String recycleInfo4 = multiSign.Recycle(MULITADD5, PRIKEY1, tokenType2, "10");
         Thread.sleep(SLEEPTIME); //UTXO关系，回收同一个账号需要休眠
         String recycleInfo5 = multiSign.Recycle(MULITADD5, PRIKEY1, tokenType, "10");
         Thread.sleep(SLEEPTIME);
         assertThat(recycleInfo,containsString("200"));
         assertThat(recycleInfo2,containsString("200"));
         assertThat(recycleInfo3,containsString("200"));
         assertThat(recycleInfo4,containsString("200"));
         assertThat(recycleInfo5,containsString("200"));

         log.info("查询余额判断回收成功与否");
         String queryInfo3=multiSign.Balance(MULITADD4,PRIKEY1,tokenType);
         String queryInfo4=multiSign.Balance(MULITADD5,PRIKEY1,tokenType2);
         String queryInfo5=multiSign.Balance(MULITADD5,PRIKEY1,tokenType);
         assertThat(queryInfo3,containsString("200"));
         assertThat(queryInfo4,containsString("200"));
         assertThat(JSONObject.fromObject(queryInfo3).getJSONObject("Data").getString("Total"), containsString("0"));
         assertThat(JSONObject.fromObject(queryInfo4).getJSONObject("Data").getString("Total"), containsString("0"));
         assertThat(JSONObject.fromObject(queryInfo5).getJSONObject("Data").getString("Total"), containsString("0"));

     }




    /**
     *TC31归集地址向单签和多签地址转账
     * 发两种币-查询归集地址-转账两个地址不同token-查询-回收-查询
     * @throws Exception
     */
    @Test
    public void TC31_transferSoloMulti()throws  Exception{
        log.info("发行两种token1000个");
        String tokenType=IssueToken(4,"1000");
        String tokenType2=IssueToken(5,"1000");
        Thread.sleep(SLEEPTIME);

        log.info("查询归集地址中两种token余额");
        String response1 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        String response2 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType2);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("1000"));
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("1000"));
        String transferData = "归集地址向" + "ADDRESS1" + "转账10个" + tokenType+"归集地址向" + "MULITADD4" + "转账10个" + tokenType;
        List<Map>list=utilsClass.constructToken(ADDRESS1,tokenType,"10");
        List<Map>list2=utilsClass.constructToken(MULITADD4,tokenType2,"10",list);
        List<Map>list3=utilsClass.constructToken(MULITADD4,tokenType,"10",list);
        log.info(transferData);
        String transferInfo=multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD,list2);//不相同币种
        Thread.sleep(SLEEPTIME);//UTXO关系，两笔交易之间需要休眠
        String transferInfo2=multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD,list3);//相同币种
        assertThat(transferInfo,containsString("200"));
        assertThat(transferInfo2,containsString("200"));
        Thread.sleep(SLEEPTIME);

        log.info("查询余额判断转账是否成功");
        String queryInfo=multiSign.Balance(PRIKEY1,tokenType);
        String queryInfo2=multiSign.Balance(MULITADD4,PRIKEY1,tokenType2);
        assertThat(queryInfo,containsString("200"));
        assertThat(queryInfo2,containsString("200"));
        assertThat(JSONObject.fromObject(queryInfo).getJSONObject("Data").getString("Total"), containsString("20"));
        assertThat(JSONObject.fromObject(queryInfo2).getJSONObject("Data").getString("Total"), containsString("10"));

        log.info("回收Token");
        String recycleInfo = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType, "970");
        String recycleInfo2 = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType2, "990");
        String recycleInfo3 = multiSign.Recycle(PRIKEY1, tokenType, "20");
        String recycleInfo4 = multiSign.Recycle(MULITADD4, PRIKEY1, tokenType2, "10");
        Thread.sleep(SLEEPTIME); //UTXO关系，回收同一个账号需要休眠
        String recycleInfo5 = multiSign.Recycle(MULITADD4, PRIKEY1, tokenType, "10");
        Thread.sleep(SLEEPTIME);
        assertThat(recycleInfo,containsString("200"));
        assertThat(recycleInfo2,containsString("200"));
        assertThat(recycleInfo3,containsString("200"));
        assertThat(recycleInfo4,containsString("200"));
        assertThat(recycleInfo5,containsString("200"));

        log.info("查询余额判断回收成功与否");
        String queryInfo3=multiSign.Balance(PRIKEY1,tokenType);
        String queryInfo4=multiSign.Balance(MULITADD4,PRIKEY1,tokenType2);
        String queryInfo5=multiSign.Balance(MULITADD4,PRIKEY1,tokenType);
        assertThat(queryInfo3,containsString("200"));
        assertThat(queryInfo4,containsString("200"));
        assertThat(JSONObject.fromObject(queryInfo3).getJSONObject("Data").getString("Total"), containsString("0"));
        assertThat(JSONObject.fromObject(queryInfo4).getJSONObject("Data").getString("Total"), containsString("0"));
        assertThat(JSONObject.fromObject(queryInfo5).getJSONObject("Data").getString("Total"), containsString("0"));
    }


    /**
     *TC32归集地址向两个单签地址转账
     * 发两种币-查询归集地址-转账两个地址不同token-查询-回收-查询
     * @throws Exception
     */
    @Test
    public void TC31_transferSolo()throws  Exception{
        log.info("发行两种token1000个");
        String tokenType=IssueToken(4,"1000");
        String tokenType2=IssueToken(5,"1000");
        Thread.sleep(SLEEPTIME);

        log.info("查询归集地址中两种token余额");
        String response1 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        String response2 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType2);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("1000"));
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("1000"));
        String transferData = "归集地址向" + "ADDRESS1" + "转账10个" + tokenType+"归集地址向" + "ADDRESS2" + "转账10个" + tokenType;
        List<Map>list=utilsClass.constructToken(ADDRESS1,tokenType,"10");
        List<Map>list2=utilsClass.constructToken(ADDRESS2,tokenType2,"10",list);
        List<Map>list3=utilsClass.constructToken(ADDRESS2,tokenType,"10",list);
        log.info(transferData);
        String transferInfo=multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD,list2);//两个地址不同币种
        Thread.sleep(SLEEPTIME);//UTXO关系，两笔交易之间需要休眠
        String transferInfo2=multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD,list3);//两个地址相同币种
        assertThat(transferInfo,containsString("200"));
        assertThat(transferInfo2,containsString("200"));
        Thread.sleep(SLEEPTIME);

        log.info("查询余额判断转账是否成功");
        String queryInfo=multiSign.Balance(PRIKEY1,tokenType);
        String queryInfo2=multiSign.Balance(PRIKEY2,tokenType2);
        assertThat(queryInfo,containsString("200"));
        assertThat(queryInfo2,containsString("200"));
        assertThat(JSONObject.fromObject(queryInfo).getJSONObject("Data").getString("Total"), containsString("20"));
        assertThat(JSONObject.fromObject(queryInfo2).getJSONObject("Data").getString("Total"), containsString("10"));

        log.info("回收Token");
        String recycleInfo = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType, "970");
        String recycleInfo2 = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType2, "990");
        String recycleInfo3 = multiSign.Recycle(PRIKEY1, tokenType, "20");
        String recycleInfo4 = multiSign.Recycle( PRIKEY2, tokenType2, "10");
        String recycleInfo5 = multiSign.Recycle(PRIKEY2, tokenType, "10");
        Thread.sleep(SLEEPTIME);
        assertThat(recycleInfo,containsString("200"));
        assertThat(recycleInfo2,containsString("200"));
        assertThat(recycleInfo3,containsString("200"));
        assertThat(recycleInfo4,containsString("200"));
        assertThat(recycleInfo5,containsString("200"));
        log.info("查询余额判断回收成功与否");
        String queryInfo3=multiSign.Balance(PRIKEY1,tokenType);
        String queryInfo4=multiSign.Balance(PRIKEY2,tokenType2);
        String queryInfo5=multiSign.Balance(PRIKEY2,tokenType);
        assertThat(queryInfo3,containsString("200"));
        assertThat(queryInfo4,containsString("200"));
        assertThat(JSONObject.fromObject(queryInfo3).getJSONObject("Data").getString("Total"), containsString("0"));
        assertThat(JSONObject.fromObject(queryInfo4).getJSONObject("Data").getString("Total"), containsString("0"));
        assertThat(JSONObject.fromObject(queryInfo5).getJSONObject("Data").getString("Total"), containsString("0"));
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


   


    String IssueToken(int length,String  amount){
        String tokenType = "CX-" + UtilsClass.Random(length);
        //String amount = "1000";
        log.info(MULITADD2 + "发行" + tokenType + " token，数量为：" + amount);
        String data = "MULITADD2" + "发行" + tokenType + " token，数量为：" + amount;
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
        return tokenType;

    }
}
