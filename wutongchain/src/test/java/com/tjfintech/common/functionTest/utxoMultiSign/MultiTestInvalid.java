package com.tjfintech.common.functionTest.utxoMultiSign;


import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import static com.tjfintech.common.functionTest.store.StoreTest.SLEEPTIME;
import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class  MultiTestInvalid {
    TestBuilder testBuilder= TestBuilder.getInstance();
    MultiSign multiSign =testBuilder.getMultiSign();
    UtilsClass utilsClass = new UtilsClass();
    MultiTest multiTest = new MultiTest();
    SoloSign soloSign=testBuilder.getSoloSign();
    Store store = testBuilder.getStore();
    private static String tokenType;
    private static String tokenType2;


    @Before
    public void beforeConfig() throws Exception {

        if(certPath!=""&& bReg==false) {
            BeforeCondition bf = new BeforeCondition();
            bf.updatePubPriKey();
            bf.collAddressTest();
            Thread.sleep(SLEEPTIME);
            bReg=true;
        }

        log.info("发行两种token100.123个");
        tokenType = multiTest.IssueToken(7, "100");
        tokenType2 = multiTest.IssueToken(5, "100.123");
        Thread.sleep(SLEEPTIME);
        log.info("查询归集地址中两种token余额");
        String response1 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        String response2 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType2);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("100"));
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("100.123"));

    }
    @Test
    public void TC994_issueToOtherInvalid()throws Exception{
        int length=5;
        String amount="10000";
        String tokenType = "CX-" + UtilsClass.Random(length);
        //String amount = "1000";
        log.info(MULITADD3+ "发行" + tokenType + " token，数量为：" + amount);
        String data = "MULITADD3" + "发行" + tokenType + " token，数量为：" + amount;
        String response = multiSign.issueToken(IMPPUTIONADD,ADDRESS1,tokenType, amount, data);
//        assertThat(response, containsString("400"));
        assertThat(response, containsString("Invalid multiple address(to addr)"));
        tokenType2 = multiTest.IssueToken(5, "1000",MULITADD4);
        Thread.sleep(SLEEPTIME);
        String response1 = soloSign.Balance(PRIKEY1, tokenType);
        String response2 = multiSign.Balance(MULITADD4,PRIKEY1, tokenType);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("\"Total\":\"0\""));
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("\"Total\":\"0\""));
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
        assertThat(response2, containsString("100.123"));

        String transferData = "归集地址向两个多签地址转账异常测试";
        List<Map> list = utilsClass.constructToken(ADDRESS1, tokenType, "10");//A足
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
//        assertThat(transferInfo2, containsString("400"));
//        assertThat(transferInfo3, containsString("400"));
//        assertThat(transferInfo4, containsString("400"));
//        assertThat(transferInfo5, containsString("400"));
//        assertThat(transferInfo6, containsString("400"));
//        assertThat(transferInfo7, containsString("400"));
        assertThat(JSONObject.fromObject(transferInfo2).getString("Message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo3).getString("Message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo4).getString("Message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo5).getString("Message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo6).getString("Message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo7).getString("Message"),equalTo("insufficient balance"));



        log.info("查询余额判断转账是否成功");
        String queryInfo = multiSign.Balance(MULITADD4, PRIKEY1, tokenType);
        String queryInfo2 = multiSign.Balance(MULITADD5, PRIKEY1, tokenType2);
        assertThat(queryInfo, containsString("200"));
        assertThat(queryInfo2, containsString("200"));
        assertThat(JSONObject.fromObject(queryInfo).getJSONObject("Data").getString("Total"), containsString("0"));
        assertThat(JSONObject.fromObject(queryInfo2).getJSONObject("Data").getString("Total"), containsString("0"));

        log.info("回收Token");
        String recycleInfo = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType, "100");
        String recycleInfo2 = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType2, "100.123");
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
        assertThat(response2, containsString("100.123"));

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
//        assertThat(transferInfo2, containsString("400"));
//        assertThat(transferInfo3, containsString("400"));
//        assertThat(transferInfo4, containsString("400"));
//        assertThat(transferInfo5, containsString("400"));
//        assertThat(transferInfo6, containsString("400"));
//        assertThat(transferInfo7, containsString("400"));
        assertThat(JSONObject.fromObject(transferInfo2).getString("Message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo3).getString("Message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo4).getString("Message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo5).getString("Message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo6).getString("Message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo7).getString("Message"),equalTo("insufficient balance"));

        Thread.sleep(SLEEPTIME);


        log.info("查询余额判断转账是否成功");
        String queryInfo = soloSign.Balance( PRIKEY1, tokenType);
        String queryInfo2 = multiSign.Balance(MULITADD5, PRIKEY1, tokenType2);
        assertThat(queryInfo, containsString("200"));
        assertThat(queryInfo2, containsString("200"));
        assertThat(JSONObject.fromObject(queryInfo).getJSONObject("Data").getString("Total"), containsString("0"));
        assertThat(JSONObject.fromObject(queryInfo2).getJSONObject("Data").getString("Total"), containsString("0"));

        log.info("回收Token");
        String recycleInfo = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType, "100");
        String recycleInfo2 = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType2, "100.123");
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
        assertThat(response2, containsString("100.123"));

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
//        assertThat(transferInfo2, containsString("400"));
//        assertThat(transferInfo3, containsString("400"));
//        assertThat(transferInfo4, containsString("400"));
//        assertThat(transferInfo5, containsString("400"));
//        assertThat(transferInfo6, containsString("400"));
//        assertThat(transferInfo7, containsString("400"));
        assertThat(JSONObject.fromObject(transferInfo2).getString("Message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo3).getString("Message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo4).getString("Message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo5).getString("Message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo6).getString("Message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo7).getString("Message"),equalTo("insufficient balance"));

        Thread.sleep(SLEEPTIME);


        log.info("查询余额判断转账是否成功");
        String queryInfo = soloSign.Balance( PRIKEY1, tokenType);
        String queryInfo2 = soloSign.Balance( PRIKEY2, tokenType2);
        assertThat(queryInfo, containsString("200"));
        assertThat(queryInfo2, containsString("200"));
        assertThat(JSONObject.fromObject(queryInfo).getJSONObject("Data").getString("Total"), containsString("0"));
        assertThat(JSONObject.fromObject(queryInfo2).getJSONObject("Data").getString("Total"), containsString("0"));

        log.info("回收Token");
        String recycleInfo = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType, "100");
        String recycleInfo2 = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType2, "100.123");
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
     * Tc238 多签地址向两个单签转账异常测试
     * @throws Exception
     */

    @Test
    public void TC238_MultiToSoloInvalid()throws  Exception{
        log.info("查询归集地址中两种token余额");
        String response1 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        String response2 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType2);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("100"));
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("100.123"));
        String transferDataInit = "归集地址向" + "MULITADD6" + "转账100个" + tokenType + "归集地址向" + "MULITADD6" + "转账100.123个" + tokenType;
        List<Map> listInit = utilsClass.constructToken(MULITADD6, tokenType, "100");
        List<Map> listInit2 = utilsClass.constructToken(MULITADD6, tokenType2, "100.123", listInit);
        log.info(transferDataInit);
        String transferInfoInit = multiSign.Transfer(PRIKEY4, transferDataInit, IMPPUTIONADD, listInit2);//转账给多签地址
        assertThat(transferInfoInit,containsString("200"));
        String transferData = "多签地址向两个单签地址转账异常测试";
        List<Map> list = utilsClass.constructToken(ADDRESS1, tokenType, "10");//A足
        List<Map> list0 = utilsClass.constructToken(ADDRESS1, tokenType2, "101");//A 不足
        List<Map> list2 = utilsClass.constructToken(ADDRESS2, tokenType, "101", list);//A足B不足
        List<Map> list3 = utilsClass.constructToken(ADDRESS2, tokenType, "10", list0);//A不足B足
        List<Map> list4 = utilsClass.constructToken(ADDRESS2, tokenType, "101", list0);//A不足B不足

        List<Map> list5 = utilsClass.constructToken(ADDRESS2, tokenType2, "101", list);//A足B不足
        List<Map> list6 = utilsClass.constructToken(ADDRESS2, tokenType2, "10", list0);//A不足B足
        List<Map> list7 = utilsClass.constructToken(ADDRESS2, tokenType2, "101", list0);//A不足B不足
        log.info(transferData);
        String transferInfo2 = multiSign.Transfer(PRIKEY4, transferData, MULITADD6, list2);
        String transferInfo3 = multiSign.Transfer(PRIKEY4, transferData, MULITADD6, list3);
        String transferInfo4 = multiSign.Transfer(PRIKEY4, transferData, MULITADD6, list4);
        String transferInfo5 = multiSign.Transfer(PRIKEY4, transferData, MULITADD6, list5);
        String transferInfo6 = multiSign.Transfer(PRIKEY4, transferData, MULITADD6, list6);
        String transferInfo7 = multiSign.Transfer(PRIKEY4, transferData, MULITADD6, list7);
        Thread.sleep(SLEEPTIME); //UTXO关系，两笔交易之间需要休眠
//        assertThat(transferInfo2, containsString("400"));
//        assertThat(transferInfo3, containsString("400"));
//        assertThat(transferInfo4, containsString("400"));
//        assertThat(transferInfo5, containsString("400"));
//        assertThat(transferInfo6, containsString("400"));
//        assertThat(transferInfo7, containsString("400"));
        assertThat(JSONObject.fromObject(transferInfo2).getString("Message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo3).getString("Message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo4).getString("Message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo5).getString("Message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo6).getString("Message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo7).getString("Message"),equalTo("insufficient balance"));

        Thread.sleep(SLEEPTIME);


        log.info("查询余额判断转账是否成功");
        String queryInfo = soloSign.Balance( PRIKEY1, tokenType);
        String queryInfo2 = soloSign.Balance( PRIKEY2, tokenType2);
        assertThat(queryInfo, containsString("200"));
        assertThat(queryInfo2, containsString("200"));
        assertThat(JSONObject.fromObject(queryInfo).getJSONObject("Data").getString("Total"), containsString("0"));
        assertThat(JSONObject.fromObject(queryInfo2).getJSONObject("Data").getString("Total"), containsString("0"));

        log.info("回收Token");
        String recycleInfo = multiSign.Recycle(MULITADD6, PRIKEY4, tokenType, "100");
        String recycleInfo2 = multiSign.Recycle(MULITADD6, PRIKEY4, tokenType2, "100.123");
        Thread.sleep(SLEEPTIME);
        assertThat(recycleInfo, containsString("200"));
        assertThat(recycleInfo2, containsString("200"));


        log.info("查询余额判断回收成功与否");
        String queryInfo3 = multiSign.Balance(MULITADD6, PRIKEY4, tokenType);
        String queryInfo4 = multiSign.Balance(MULITADD6, PRIKEY4, tokenType2);
        assertThat(queryInfo3, containsString("200"));
        assertThat(queryInfo4, containsString("200"));
        assertThat(JSONObject.fromObject(queryInfo3).getJSONObject("Data").getString("Total"), containsString("0"));
        assertThat(JSONObject.fromObject(queryInfo4).getJSONObject("Data").getString("Total"), containsString("0"));

    }

    /**
     * Tc239 多签地址向两个多签转账异常测试
     * @throws Exception
     */

    @Test
    public void TC239_MultiToMulitInvalid()throws  Exception{
        log.info("查询归集地址中两种token余额");
        String response1 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        String response2 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType2);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("100"));
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("100.123"));
        String transferDataInit = "归集地址向" + "MULITADD6" + "转账100个" + tokenType + "归集地址向" + "MULITADD6" + "转账100.123个" + tokenType;
        List<Map> listInit = utilsClass.constructToken(MULITADD6, tokenType, "100");
        List<Map> listInit2 = utilsClass.constructToken(MULITADD6, tokenType2, "100.123", listInit);
        log.info(transferDataInit);
        String transferInfoInit = multiSign.Transfer(PRIKEY4, transferDataInit, IMPPUTIONADD, listInit2);//转账给多签地址
        assertThat(transferInfoInit,containsString("200"));
        String transferData = "多签地址向两个多签地址转账异常测试";
        List<Map> list = utilsClass.constructToken(MULITADD4, tokenType, "10");//A足
        List<Map> list0 = utilsClass.constructToken(MULITADD4, tokenType2, "101");//A 不足
        List<Map> list2 = utilsClass.constructToken(MULITADD5, tokenType, "101", list);//A足B不足
        List<Map> list3 = utilsClass.constructToken(MULITADD5, tokenType, "10", list0);//A不足B足
        List<Map> list4 = utilsClass.constructToken(MULITADD5, tokenType, "101", list0);//A不足B不足

        List<Map> list5 = utilsClass.constructToken(MULITADD5, tokenType2, "101", list);//A足B不足
        List<Map> list6 = utilsClass.constructToken(MULITADD5, tokenType2, "10", list0);//A不足B足
        List<Map> list7 = utilsClass.constructToken(MULITADD5, tokenType2, "101", list0);//A不足B不足
        log.info(transferData);
        String transferInfo2 = multiSign.Transfer(PRIKEY4, transferData, MULITADD6, list2);
        String transferInfo3 = multiSign.Transfer(PRIKEY4, transferData, MULITADD6, list3);
        String transferInfo4 = multiSign.Transfer(PRIKEY4, transferData, MULITADD6, list4);
        String transferInfo5 = multiSign.Transfer(PRIKEY4, transferData, MULITADD6, list5);
        String transferInfo6 = multiSign.Transfer(PRIKEY4, transferData, MULITADD6, list6);
        String transferInfo7 = multiSign.Transfer(PRIKEY4, transferData, MULITADD6, list7);
        Thread.sleep(SLEEPTIME); //UTXO关系，两笔交易之间需要休眠
//        assertThat(transferInfo2, containsString("400"));
//        assertThat(transferInfo3, containsString("400"));
//        assertThat(transferInfo4, containsString("400"));
//        assertThat(transferInfo5, containsString("400"));
//        assertThat(transferInfo6, containsString("400"));
//        assertThat(transferInfo7, containsString("400"));
        assertThat(JSONObject.fromObject(transferInfo2).getString("Message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo3).getString("Message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo4).getString("Message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo5).getString("Message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo6).getString("Message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo7).getString("Message"),equalTo("insufficient balance"));

        Thread.sleep(SLEEPTIME);


        log.info("查询余额判断转账是否成功");
        String queryInfo = multiSign.Balance( MULITADD4,PRIKEY1, tokenType);
        String queryInfo2 = multiSign.Balance( MULITADD5,PRIKEY1, tokenType2);
        assertThat(queryInfo, containsString("200"));
        assertThat(queryInfo2, containsString("200"));
        assertThat(JSONObject.fromObject(queryInfo).getJSONObject("Data").getString("Total"), containsString("0"));
        assertThat(JSONObject.fromObject(queryInfo2).getJSONObject("Data").getString("Total"), containsString("0"));

        log.info("回收Token");
        String recycleInfo = multiSign.Recycle(MULITADD6, PRIKEY4, tokenType, "100");
        String recycleInfo2 = multiSign.Recycle(MULITADD6, PRIKEY4, tokenType2, "100.123");
        Thread.sleep(SLEEPTIME);
        assertThat(recycleInfo, containsString("200"));
        assertThat(recycleInfo2, containsString("200"));


        log.info("查询余额判断回收成功与否");
        String queryInfo3 = multiSign.Balance(MULITADD6, PRIKEY4, tokenType);
        String queryInfo4 = multiSign.Balance(MULITADD6, PRIKEY4, tokenType2);
        assertThat(queryInfo3, containsString("200"));
        assertThat(queryInfo4, containsString("200"));
        assertThat(JSONObject.fromObject(queryInfo3).getJSONObject("Data").getString("Total"), containsString("0"));
        assertThat(JSONObject.fromObject(queryInfo4).getJSONObject("Data").getString("Total"), containsString("0"));

    }

    /**
     * Tc240 多签地址向两个多签转账异常测试
     * @throws Exception
     */

    @Test
    public void TC240_MultiToSoloMulitInvalid()throws  Exception{
        log.info("查询归集地址中两种token余额");
        String response1 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        String response2 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType2);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("100"));
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("100.123"));
        String transferDataInit = "归集地址向" + "MULITADD6" + "转账100个" + tokenType + "归集地址向" + "MULITADD6" + "转账100.123个" + tokenType;
        List<Map> listInit = utilsClass.constructToken(MULITADD6, tokenType, "100");
        List<Map> listInit2 = utilsClass.constructToken(MULITADD6, tokenType2, "100.123", listInit);
        log.info(transferDataInit);
        String transferInfoInit = multiSign.Transfer(PRIKEY4, transferDataInit, IMPPUTIONADD, listInit2);//转账给多签地址
        assertThat(transferInfoInit,containsString("200"));
        String transferData = "多签地址向单签和多签地址转账异常测试";
        List<Map> list = utilsClass.constructToken(ADDRESS1, tokenType, "10");//A足
        List<Map> list0 = utilsClass.constructToken(ADDRESS1, tokenType2, "101");//A 不足
        List<Map> list2 = utilsClass.constructToken(MULITADD5, tokenType, "101", list);//A足B不足
        List<Map> list3 = utilsClass.constructToken(MULITADD5, tokenType, "10", list0);//A不足B足
        List<Map> list4 = utilsClass.constructToken(MULITADD5, tokenType, "101", list0);//A不足B不足

        List<Map> list5 = utilsClass.constructToken(MULITADD5, tokenType2, "101", list);//A足B不足
        List<Map> list6 = utilsClass.constructToken(MULITADD5, tokenType2, "10", list0);//A不足B足
        List<Map> list7 = utilsClass.constructToken(MULITADD5, tokenType2, "101", list0);//A不足B不足
        log.info(transferData);
        String transferInfo2 = multiSign.Transfer(PRIKEY4, transferData, MULITADD6, list2);
        String transferInfo3 = multiSign.Transfer(PRIKEY4, transferData, MULITADD6, list3);
        String transferInfo4 = multiSign.Transfer(PRIKEY4, transferData, MULITADD6, list4);
        String transferInfo5 = multiSign.Transfer(PRIKEY4, transferData, MULITADD6, list5);
        String transferInfo6 = multiSign.Transfer(PRIKEY4, transferData, MULITADD6, list6);
        String transferInfo7 = multiSign.Transfer(PRIKEY4, transferData, MULITADD6, list7);
        Thread.sleep(SLEEPTIME); //UTXO关系，两笔交易之间需要休眠
//        assertThat(transferInfo2, containsString("400"));
//        assertThat(transferInfo3, containsString("400"));
//        assertThat(transferInfo4, containsString("400"));
//        assertThat(transferInfo5, containsString("400"));
//        assertThat(transferInfo6, containsString("400"));
//        assertThat(transferInfo7, containsString("400"));
        assertThat(JSONObject.fromObject(transferInfo2).getString("Message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo3).getString("Message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo4).getString("Message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo5).getString("Message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo6).getString("Message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo7).getString("Message"),equalTo("insufficient balance"));

        Thread.sleep(SLEEPTIME);


        log.info("查询余额判断转账是否成功");
        String queryInfo = soloSign.Balance( PRIKEY1, tokenType);
        String queryInfo2 = multiSign.Balance( MULITADD5,PRIKEY1, tokenType2);
        assertThat(queryInfo, containsString("200"));
        assertThat(queryInfo2, containsString("200"));
        assertThat(JSONObject.fromObject(queryInfo).getJSONObject("Data").getString("Total"), containsString("0"));
        assertThat(JSONObject.fromObject(queryInfo2).getJSONObject("Data").getString("Total"), containsString("0"));

        log.info("回收Token");
        String recycleInfo = multiSign.Recycle(MULITADD6, PRIKEY4, tokenType, "100");
        String recycleInfo2 = multiSign.Recycle(MULITADD6, PRIKEY4, tokenType2, "100.123");
        Thread.sleep(SLEEPTIME);
        assertThat(recycleInfo, containsString("200"));
        assertThat(recycleInfo2, containsString("200"));


        log.info("查询余额判断回收成功与否");
        String queryInfo3 = multiSign.Balance(MULITADD6, PRIKEY4, tokenType);
        String queryInfo4 = multiSign.Balance(MULITADD6, PRIKEY4, tokenType2);
        assertThat(queryInfo3, containsString("200"));
        assertThat(queryInfo4, containsString("200"));
        assertThat(JSONObject.fromObject(queryInfo3).getJSONObject("Data").getString("Total"), containsString("0"));
        assertThat(JSONObject.fromObject(queryInfo4).getJSONObject("Data").getString("Total"), containsString("0"));

    }

    /**
     * TC6 M参数非法时创建多签地址
     */
    @Test
    public void TC6_createAddInvalid1() {
        //int M = 0;
        Map<String, Object> map = new HashMap<>();
        map.put("1", PUBKEY1);
        map.put("2", PUBKEY2);
        map.put("3", PUBKEY3);
        String response = multiSign.genMultiAddress(0, map);
        String response2 = multiSign.genMultiAddress(4, map);
        assertThat(response, containsString("400"));
        assertThat(response, containsString("M can't be 0"));
        assertThat(response2, containsString("400"));
        assertThat(response2, containsString("Parameter 'm' must be less than or equal the number of address"));


    }

    /**
     * Tc08公钥参数异常创建多签地址测试
     */
    @Test
    public void TC8_createAddInvalid2() {
        int M = 1;
        Map<String, Object> map = new HashMap<>();
        map.put("1", "123");
        map.put("2", PUBKEY2);
        map.put("3", PUBKEY3);
        String response = multiSign.genMultiAddress(M, map);
        map.put("1", "@#$%");
        String response2 = multiSign.genMultiAddress(M, map);
        map.put("1", PUBKEY2);
        String response3 = multiSign.genMultiAddress(M, map);
        assertThat(response, containsString("400"));
        assertThat(response2, containsString("400"));
        assertThat(response3, containsString("400"));
        assertThat(response3, containsString("duplicate pubkey"));

    }

    /**
     * TC12变更多签公钥顺序，多签地址不变
     */
    @Test
    public void TC12_createAdd() {
        int M = 3;
        Map<String, Object> map = new HashMap<>();
        map.put("1", PUBKEY1);
        map.put("2", PUBKEY2);
        map.put("3", PUBKEY3);
        String response = multiSign.genMultiAddress(M, map);
        assertThat(response, containsString("200"));
        assertThat(JSONObject.fromObject(response).getJSONObject("Data").getString("Address"), equalTo(MULITADD1));
        map.put("1", PUBKEY2);
        map.put("2", PUBKEY1);
        String response2 = multiSign.genMultiAddress(M, map);
        assertThat(response2, containsString("200"));
        assertThat(JSONObject.fromObject(response2).getJSONObject("Data").getString("Address"), equalTo(MULITADD1));

    }


    /**
     * TC281创建多签地址
     */
    @Test
    public void TC281_testGenMultiAddress() {
        int M = 3;
        Map<String, Object> map = new HashMap<>();
        map.put("1", PUBKEY1);
        map.put("2", PUBKEY2);
        map.put("3", PUBKEY3);
        String response = multiSign.genMultiAddress(M, map);
        assertThat(response, containsString("200"));
        assertThat(JSONObject.fromObject(response).getJSONObject("Data").getString("Address"), equalTo(MULITADD1));
        map.remove("3");
        String response2 = multiSign.genMultiAddress(1, map);
        assertThat(response2, containsString("200"));
        assertThat(JSONObject.fromObject(response2).getJSONObject("Data").getString("Address"), equalTo(MULITADD4));
    }

    /**
     * Tc272核对公私钥接口
     * 20190429 开发确认要删除此接口，故不再对此接口进行测试
     */
    // @Test
    public void TC272_testCheckPriKey() {
        String response = multiSign.CheckPriKey(PRIKEY6, PWD6);
        assertThat(response, containsString("200"));
        assertThat(response, containsString("This password match for the private key"));

    }

    /**
     * TC282 发行Token异常测试
     * token类型长度
     * 入参金额的大小、精度
     * 未在CA中配置3/3地址/删除配置的3/3地址
     */
    @Test
    public void TC282_issueTokenInvalid() {
        String tokenType = "CX-" + UtilsClass.Random(4);
        String amount = "90";
        log.info(MULITADD2 + "发行" + tokenType + " token，数量为：" + amount);
        String data = "MULITADD2" + "发行" + tokenType + " token，数量为：" + amount;
        //String response = multiSign.issueToken(MULITADD2, tokenType+"123456789000000000000000000", amount, data);
        String response2 = multiSign.issueToken(MULITADD2, tokenType, "900000000000000000000000000000000000", data);
        String response4 = multiSign.issueToken("0123", tokenType, amount, data);
        // String response5 = multiSign.issueToken("0123", tokenType, amount, data);
        // assertThat(response, containsString("400"));
//        assertThat(response2, containsString("400"));
//        assertThat(response4, containsString("400"));
        assertThat(response2, containsString("Amount must be greater than 0 and less than 900000000"));
        assertThat(response4, containsString("Invalid multiple address"));
        //     assertThat(response5, containsString("400"));

    }

    /**
     * TC283签名发行token交易异常测试
     * 入参缺a输入的密码
     * 入参传错的a的密码
     * 参数传非法私钥
     * 入参传非abc的私钥 d
     * 入参传错误的缺待签名的交易
     * 同一私钥重复签名的交易
     */
    @Test
    public void TC283_signIssueInvalid() {
        String tokenType = "CX-" + UtilsClass.Random(4);
        String amount = "1000";
        String data = "MULITADD2" + "发行" + tokenType + " token，数量为：" + amount;
        String response = multiSign.issueToken(MULITADD3, tokenType, amount, data);
        assertThat(response, containsString("200"));
        String Tx1 = JSONObject.fromObject(response).getJSONObject("Data").getString("Tx");
        String response2 = multiSign.Sign(Tx1, PRIKEY6);//不带密码
        String response3 = multiSign.Sign(Tx1, PRIKEY6, PWD7);//密码错误
        String response4 = multiSign.Sign(Tx1, "112");  //非法密钥
        String response5 = multiSign.Sign(Tx1, PRIKEY3);   //无关密钥
        String response6 = multiSign.Sign("123", PRIKEY1);//Tx非法
        String response7 = multiSign.Sign(Tx1, PRIKEY1);
        String Tx2 = JSONObject.fromObject(response7).getJSONObject("Data").getString("Tx");
        String response8 = multiSign.Sign(Tx2, PRIKEY1);//重复密钥
//        assertThat(response2, containsString("400"));
//        assertThat(response3, containsString("400"));
//        assertThat(response4, containsString("400"));
//        assertThat(response5, containsString("400"));
//        assertThat(response6, containsString("400"));
//        assertThat(response8, containsString("400"));
        assertThat(response7, containsString("200"));
        assertThat(response2, containsString("Incorrect private key or password"));
        assertThat(response3, containsString("Incorrect private key or password"));
        assertThat(response4, containsString("Private Key must be base64 string"));
        assertThat(response5, containsString("Multiaddr is not matching for the prikey"));
        assertThat(response6, containsString("Invalid parameter -- Tx"));
        assertThat(response8, containsString("Private key signed already"));
        String queryInfo = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        assertThat(queryInfo, containsString("200"));
        assertThat(JSONObject.fromObject(queryInfo).getJSONObject("Data").getString("Total"), containsString("0"));

    }

    /**
     * TC21-23多签地址回收异常测试
     */
    @Test
    public void TC21_23recycleInvalid() {
        String tokenType = "cx-8oVNI";
        String queryInfo = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        assertThat(queryInfo, containsString("200"));
        String recycleInfo = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, "abc", "1");
        String recycleInfo2 = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType, "0");
        String recycleInfo3 = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType, "9000000000000000");
        String recycleInfo4 = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType, "-10");
        String recycleInfo5 = multiSign.Recycle(IMPPUTIONADD, "123", tokenType, "1");
        String recycleInfo6 = multiSign.Recycle(IMPPUTIONADD, PRIKEY3, tokenType, "1");
        String recycleInfo7 = multiSign.Recycle(IMPPUTIONADD, "0", tokenType, "1");
        String recycleInfo8 = multiSign.Recycle("0", PRIKEY4, tokenType, "1");
        String recycleInfo9 = multiSign.Recycle(MULITADD3, PRIKEY4, tokenType, "1");
//        assertThat(recycleInfo, containsString("400"));
//        assertThat(recycleInfo2, containsString("400"));
//        assertThat(recycleInfo3, containsString("400"));
//        assertThat(recycleInfo4, containsString("400"));
//        assertThat(recycleInfo5, containsString("400"));
//        assertThat(recycleInfo6, containsString("400"));
//        assertThat(recycleInfo7, containsString("400"));
//        assertThat(recycleInfo8, containsString("400"));
//        assertThat(recycleInfo9, containsString("400"));
        assertThat(recycleInfo, containsString("insufficient balance"));
        assertThat(recycleInfo2, containsString("Amount must be greater than 0 and less than 900000000"));
        assertThat(recycleInfo3, containsString("Amount must be greater than 0 and less than 900000000"));
        assertThat(recycleInfo4, containsString("Token amount must be a valid number and less than 900000000"));
        assertThat(recycleInfo5, containsString("Private key must be base64 string"));
        assertThat(recycleInfo6, containsString("Multiaddr is not matching for the prikey"));
        assertThat(recycleInfo8, containsString("Invalid multiple address"));
        assertThat(recycleInfo9, containsString("Multiaddr is not matching for the prikey"));
    }

    /**
     * TC13 14 20 多签查询异常测试
     * 私钥 地址 tokenType
     */
    @Test
    public void TC13_20balanceInvalid() {
        String tokenType = "cx-8oVNI";
        String queryInfo = multiSign.Balance(IMPPUTIONADD, "0", tokenType);
        String queryInfo1 = multiSign.Balance(IMPPUTIONADD, PRIKEY3, tokenType);
        String queryInfo2 = multiSign.Balance(IMPPUTIONADD, "1234abc", tokenType);
        String queryInfo3 = multiSign.Balance("0", PRIKEY4, tokenType);
        String queryInfo4 = multiSign.Balance("Soirv9ikykFYbBLMExy4zUTUa", PRIKEY4, tokenType);
        String queryInfo5 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, "0");
        String queryInfo6 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, "abc123e");
        assertThat(queryInfo, containsString("400"));
        assertThat(queryInfo, containsString("Private key must be base64 string"));
        assertThat(queryInfo1, containsString("400"));
        assertThat(queryInfo1, containsString("Multiaddr is not matching for the prikey"));
        assertThat(queryInfo2, containsString("400"));
        assertThat(queryInfo2, containsString("Private key must be base64 string"));
        assertThat(queryInfo3, containsString("400"));
        assertThat(queryInfo4, containsString("400"));
        assertThat(queryInfo4, containsString("Invalid multiple address"));
        assertThat(queryInfo5, containsString("200"));
        assertThat(JSONObject.fromObject(queryInfo5).getJSONObject("Data").getString("Total"), containsString("0"));
        assertThat(queryInfo6, containsString("200"));
        assertThat(JSONObject.fromObject(queryInfo6).getJSONObject("Data").getString("Total"), containsString("0"));

    }

    /**
     * 1/2多签地址向归集地址转账异常测试
     * 归集地址先向ADD1转入1个token
     * ADD1再向归集地址转入0.5个token
     *
     *
     * @throws Exception
     */
    @Test
    public void TC284_transferToImppution() throws Exception {//TODO
        String tokenType = "cx-chenxu"+ RandomUtils.nextInt(10000);
        String queryInfo = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        Boolean flag=JSONObject.fromObject(queryInfo).getJSONObject("Data").getString("Total").equals("0");
        if(flag){
            BeforeCondition beforeCondition=new BeforeCondition();
            beforeCondition.T284_BeforeCondition(tokenType);
            Thread.sleep(SLEEPTIME);
            queryInfo = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        }

        assertThat(queryInfo, containsString("200"));
        assertEquals(JSONObject.fromObject(queryInfo).getJSONObject("Data").getString("Total").equals("0"), false);
        /**
         * 如果测试不通过请执行BeforeConditon类中的第二个方法.发行相应的币种
         */
        List<Map> list0 = utilsClass.constructToken(MULITADD4, tokenType, "1");

        String transferInfo0 = multiSign.Transfer(PRIKEY4, "cx-test", IMPPUTIONADD, list0);//1 归集地址向单签地址转账
        Thread.sleep(SLEEPTIME);
        assertThat(transferInfo0, containsString("200"));
        String queryInfo1 = multiSign.Balance(MULITADD4, PRIKEY1, tokenType);
        assertThat(transferInfo0, containsString("200"));
        assertEquals(JSONObject.fromObject(queryInfo1).getJSONObject("Data").getString("Total").equals("0"), false);

        List<Map> list1 = utilsClass.constructToken(MULITADD3, tokenType, "0.5");
        String transferInfo1 = multiSign.Transfer(PRIKEY4, "cx-test", IMPPUTIONADD, list1);//多签地址向归集地址转账
        assertThat(transferInfo1, containsString("200"));

    }

    @Test
    public void TC980_MultiAddrNotInDB() throws Exception{
        String MultiAddr="Ss6iNcwoJFf5EWbuqqU7o96XNVH9izRKk9exj96giAntEFPaWZ6";
//        assertThat(multiSign.Balance(MultiAddr,PRIKEY1,"test"),
//                containsString("GetMulBalance : Multiaddr not generated, cannot be used !"));
        assertThat(multiSign.Balance(MultiAddr,PRIKEY1,"test"),
                containsString("not found multiaddress"));
    }

    @Test
    public void TC979_recoverUnIssueToken()throws Exception{
        String unIssueToken = "unIssueToken";
        String resp1 = multiSign.recoverFrozenToken(unIssueToken);
        Thread.sleep(SLEEPTIME);
        assertEquals(true,store.GetTxDetail(JSONObject.fromObject(resp1).getString("Data")).contains("failed to find transaction"));
    }


    @Test
    public void TC979_recoverUnFreezedToken()throws Exception{
        String resp1 = multiSign.recoverFrozenToken(tokenType);
        Thread.sleep(SLEEPTIME);
        assertEquals(true,store.GetTxDetail(JSONObject.fromObject(resp1).getString("Data")).contains("failed to find transaction"));
    }

}
