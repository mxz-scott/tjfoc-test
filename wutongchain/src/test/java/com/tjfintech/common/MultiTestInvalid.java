package com.tjfintech.common;

import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.List;
import java.util.Map;

import static com.tjfintech.common.StoreTest.SLEEPTIME;
import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MultiTestInvalid {
    MultiSign multiSign = new MultiSign();
    UtilsClass utilsClass = new UtilsClass();
    MultiTest multiTest = new MultiTest();
    public static String tokenType;
    public static String tokenType2;

    @Before
    public void beforeConfig() throws Exception {
        log.info("发行两种token1000个");
        tokenType = multiTest.IssueToken(3, "100");
        tokenType2 = multiTest.IssueToken(2, "100");
        Thread.sleep(SLEEPTIME);
        log.info("查询归集地址中两种token余额");
        String response1 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        String response2 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType2);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("100"));
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("100"));

    }

    /**
     * Tc37 归集地址向两个多签地址转账异常测试
     * @throws Exception
     */
    @Test
    public void TC37_transferMultiInvalid() throws Exception {
        log.info("查询归集地址中两种token余额");
        String response1 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        String response2 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType2);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("100"));
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("100"));

        String transferData = "归集地址向两个多签地址转账异常测试";
        List<Map> list = utilsClass.constructToken(MULITADD4, tokenType, "10");//A足
        List<Map> list0 = utilsClass.constructToken(MULITADD4, tokenType2, "101");//A 不足
        List<Map> list2 = utilsClass.constructToken(MULITADD5, tokenType, "101", list);//A足B不足
        List<Map> list3 = utilsClass.constructToken(MULITADD5, tokenType, "10", list0);//A不足B足
        List<Map> list4 = utilsClass.constructToken(MULITADD5, tokenType, "101", list0);//A不足B不足

        List<Map> list5 = utilsClass.constructToken(MULITADD5, tokenType2, "101", list);//A足B不足
        List<Map> list6 = utilsClass.constructToken(MULITADD5, tokenType2, "10", list0);//A不足B足
        List<Map> list7 = utilsClass.constructToken(MULITADD5, tokenType2, "101", list0);//A不足B不足
        log.info(transferData);
        String transferInfo2 = multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD, list2);
        String transferInfo3 = multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD, list3);
        String transferInfo4 = multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD, list4);
        String transferInfo5 = multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD, list5);
        String transferInfo6 = multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD, list6);
        String transferInfo7 = multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD, list7);
        Thread.sleep(SLEEPTIME); //UTXO关系，两笔交易之间需要休眠
        assertThat(transferInfo2, containsString("400"));
        assertThat(transferInfo3, containsString("400"));
        assertThat(transferInfo4, containsString("400"));
        assertThat(transferInfo5, containsString("400"));
        assertThat(transferInfo6, containsString("400"));
        assertThat(transferInfo7, containsString("400"));
        assertThat(JSONObject.fromObject(transferInfo2).getString("Message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo3).getString("Message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo4).getString("Message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo5).getString("Message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo6).getString("Message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo7).getString("Message"),equalTo("insufficient balance"));

        Thread.sleep(SLEEPTIME);


        log.info("查询余额判断转账是否成功");
        String queryInfo = multiSign.Balance(MULITADD4, PRIKEY1, tokenType);
        String queryInfo2 = multiSign.Balance(MULITADD5, PRIKEY1, tokenType2);
        assertThat(queryInfo, containsString("200"));
        assertThat(queryInfo2, containsString("200"));
        assertThat(JSONObject.fromObject(queryInfo).getJSONObject("Data").getString("Total"), containsString("0"));
        assertThat(JSONObject.fromObject(queryInfo2).getJSONObject("Data").getString("Total"), containsString("0"));

        log.info("回收Token");
        String recycleInfo = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType, "100");
        String recycleInfo2 = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType2, "100");
        Thread.sleep(SLEEPTIME);
        assertThat(recycleInfo, containsString("200"));
        assertThat(recycleInfo2, containsString("200"));


        log.info("查询余额判断回收成功与否");
        String queryInfo3 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        String queryInfo4 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType2);
        assertThat(queryInfo3, containsString("200"));
        assertThat(queryInfo4, containsString("200"));
        assertThat(JSONObject.fromObject(queryInfo3).getJSONObject("Data").getString("Total"), containsString("0"));
        assertThat(JSONObject.fromObject(queryInfo4).getJSONObject("Data").getString("Total"), containsString("0"));


    }


    /**
     * Tc38归集地址向单签、多签地址转账异常测试
     * @throws Exception
     */

    @Test
    public  void TC38_transferSoloMultiInvalid() throws  Exception{
        log.info("查询归集地址中两种token余额");
        String response1 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        String response2 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType2);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("100"));
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("100"));

        String transferData = "归集地址向单签与多签地址转账异常测试";
        List<Map> list = utilsClass.constructToken(ADDRESS1, tokenType, "10");//A足
        List<Map> list0 = utilsClass.constructToken(ADDRESS1, tokenType2, "101");//A 不足
        List<Map> list2 = utilsClass.constructToken(MULITADD5, tokenType, "101", list);//A足B不足
        List<Map> list3 = utilsClass.constructToken(MULITADD5, tokenType, "10", list0);//A不足B足
        List<Map> list4 = utilsClass.constructToken(MULITADD5, tokenType, "101", list0);//A不足B不足

        List<Map> list5 = utilsClass.constructToken(MULITADD5, tokenType2, "101", list);//A足B不足
        List<Map> list6 = utilsClass.constructToken(MULITADD5, tokenType2, "10", list0);//A不足B足
        List<Map> list7 = utilsClass.constructToken(MULITADD5, tokenType2, "101", list0);//A不足B不足
        log.info(transferData);
        String transferInfo2 = multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD, list2);
        String transferInfo3 = multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD, list3);
        String transferInfo4 = multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD, list4);
        String transferInfo5 = multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD, list5);
        String transferInfo6 = multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD, list6);
        String transferInfo7 = multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD, list7);
        Thread.sleep(SLEEPTIME); //UTXO关系，两笔交易之间需要休眠
        assertThat(transferInfo2, containsString("400"));
        assertThat(transferInfo3, containsString("400"));
        assertThat(transferInfo4, containsString("400"));
        assertThat(transferInfo5, containsString("400"));
        assertThat(transferInfo6, containsString("400"));
        assertThat(transferInfo7, containsString("400"));
        assertThat(JSONObject.fromObject(transferInfo2).getString("Message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo3).getString("Message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo4).getString("Message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo5).getString("Message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo6).getString("Message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo7).getString("Message"),equalTo("insufficient balance"));

        Thread.sleep(SLEEPTIME);


        log.info("查询余额判断转账是否成功");
        String queryInfo = multiSign.Balance( PRIKEY1, tokenType);
        String queryInfo2 = multiSign.Balance(MULITADD5, PRIKEY1, tokenType2);
        assertThat(queryInfo, containsString("200"));
        assertThat(queryInfo2, containsString("200"));
        assertThat(JSONObject.fromObject(queryInfo).getJSONObject("Data").getString("Total"), containsString("0"));
        assertThat(JSONObject.fromObject(queryInfo2).getJSONObject("Data").getString("Total"), containsString("0"));

        log.info("回收Token");
        String recycleInfo = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType, "100");
        String recycleInfo2 = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType2, "100");
        Thread.sleep(SLEEPTIME);
        assertThat(recycleInfo, containsString("200"));
        assertThat(recycleInfo2, containsString("200"));


        log.info("查询余额判断回收成功与否");
        String queryInfo3 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        String queryInfo4 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType2);
        assertThat(queryInfo3, containsString("200"));
        assertThat(queryInfo4, containsString("200"));
        assertThat(JSONObject.fromObject(queryInfo3).getJSONObject("Data").getString("Total"), containsString("0"));
        assertThat(JSONObject.fromObject(queryInfo4).getJSONObject("Data").getString("Total"), containsString("0"));


    }
    /**
     * Tc39 归集地址向两个单签转账异常测试 
     * @throws Exception
     */

    @Test
    public void TC39_transferSoloInvalid()throws  Exception{  log.info("查询归集地址中两种token余额");
        String response1 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        String response2 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType2);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("100"));
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("100"));

        String transferData = "归集地址向两个单签地址转账异常测试";
        List<Map> list = utilsClass.constructToken(ADDRESS1, tokenType, "10");//A足
        List<Map> list0 = utilsClass.constructToken(ADDRESS1, tokenType2, "101");//A 不足
        List<Map> list2 = utilsClass.constructToken(ADDRESS2, tokenType, "101", list);//A足B不足
        List<Map> list3 = utilsClass.constructToken(ADDRESS2, tokenType, "10", list0);//A不足B足
        List<Map> list4 = utilsClass.constructToken(ADDRESS2, tokenType, "101", list0);//A不足B不足

        List<Map> list5 = utilsClass.constructToken(ADDRESS2, tokenType2, "101", list);//A足B不足
        List<Map> list6 = utilsClass.constructToken(ADDRESS2, tokenType2, "10", list0);//A不足B足
        List<Map> list7 = utilsClass.constructToken(ADDRESS2, tokenType2, "101", list0);//A不足B不足
        log.info(transferData);
        String transferInfo2 = multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD, list2);
        String transferInfo3 = multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD, list3);
        String transferInfo4 = multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD, list4);
        String transferInfo5 = multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD, list5);
        String transferInfo6 = multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD, list6);
        String transferInfo7 = multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD, list7);
        Thread.sleep(SLEEPTIME); //UTXO关系，两笔交易之间需要休眠
        assertThat(transferInfo2, containsString("400"));
        assertThat(transferInfo3, containsString("400"));
        assertThat(transferInfo4, containsString("400"));
        assertThat(transferInfo5, containsString("400"));
        assertThat(transferInfo6, containsString("400"));
        assertThat(transferInfo7, containsString("400"));
        assertThat(JSONObject.fromObject(transferInfo2).getString("Message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo3).getString("Message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo4).getString("Message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo5).getString("Message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo6).getString("Message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo7).getString("Message"),equalTo("insufficient balance"));

        Thread.sleep(SLEEPTIME);


        log.info("查询余额判断转账是否成功");
        String queryInfo = multiSign.Balance( PRIKEY1, tokenType);
        String queryInfo2 = multiSign.Balance( PRIKEY2, tokenType2);
        assertThat(queryInfo, containsString("200"));
        assertThat(queryInfo2, containsString("200"));
        assertThat(JSONObject.fromObject(queryInfo).getJSONObject("Data").getString("Total"), containsString("0"));
        assertThat(JSONObject.fromObject(queryInfo2).getJSONObject("Data").getString("Total"), containsString("0"));

        log.info("回收Token");
        String recycleInfo = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType, "100");
        String recycleInfo2 = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType2, "100");
        Thread.sleep(SLEEPTIME);
        assertThat(recycleInfo, containsString("200"));
        assertThat(recycleInfo2, containsString("200"));


        log.info("查询余额判断回收成功与否");
        String queryInfo3 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        String queryInfo4 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType2);
        assertThat(queryInfo3, containsString("200"));
        assertThat(queryInfo4, containsString("200"));
        assertThat(JSONObject.fromObject(queryInfo3).getJSONObject("Data").getString("Total"), containsString("0"));
        assertThat(JSONObject.fromObject(queryInfo4).getJSONObject("Data").getString("Total"), containsString("0"));

    }
}
