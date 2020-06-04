package com.tjfintech.common.functionTest.utxoMultiSign;


import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CommonFunc;
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
    CommonFunc commonFunc = new CommonFunc();
    private static String tokenType;
    private static String tokenType2;
    String issueAm1 = "100";
    String issueAm2 = "100.123";


    @Before
    public void beforeConfig() throws Exception {

        if(MULITADD1.isEmpty()) {
            BeforeCondition bf = new BeforeCondition();
            bf.updatePubPriKey();
            bf.collAddressTest();
            commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                    utilsClass.sdkGetTxDetailType,SLEEPTIME);
        }

        log.info("发行两种token100.123个");
        tokenType = multiTest.IssueToken(7, issueAm1);
        tokenType2 = multiTest.IssueToken(5, issueAm2);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType02),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        log.info("查询归集地址中两种token余额");
        String response1 = multiSign.BalanceByAddr(IMPPUTIONADD, tokenType);
        String response2 = multiSign.BalanceByAddr(IMPPUTIONADD, tokenType2);
        log.info(response1);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString(issueAm1));
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString(issueAm2));

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

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType02),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        String response1 = multiSign.BalanceByAddr(ADDRESS1, tokenType);
        String response2 = multiSign.BalanceByAddr(MULITADD4, tokenType);
        String response3 = multiSign.BalanceByAddr(MULITADD4, tokenType2);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("\"total\":\"0\""));
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("\"total\":\"0\""));
        assertThat(response3, containsString("200"));
        assertThat(response3, containsString("\"total\":\"1000\""));
    }

    /**
     * Tc37 归集地址向两个多签地址转账异常测试
     * @throws Exception
     */
    @Test
    public void TC37_transferMultiInvalid() throws Exception {
        log.info("查询归集地址中两种token余额");
        String response1 = multiSign.BalanceByAddr(IMPPUTIONADD,  tokenType);
        String response2 = multiSign.BalanceByAddr(IMPPUTIONADD,  tokenType2);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString(issueAm1));
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString(issueAm2));

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

//        assertThat(transferInfo2, containsString("400"));
//        assertThat(transferInfo3, containsString("400"));
//        assertThat(transferInfo4, containsString("400"));
//        assertThat(transferInfo5, containsString("400"));
//        assertThat(transferInfo6, containsString("400"));
//        assertThat(transferInfo7, containsString("400"));
        assertThat(JSONObject.fromObject(transferInfo2).getString("message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo3).getString("message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo4).getString("message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo5).getString("message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo6).getString("message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo7).getString("message"),equalTo("insufficient balance"));



        log.info("查询余额判断转账是否成功");
        String queryInfo = multiSign.BalanceByAddr(MULITADD4,  tokenType);
        String queryInfo2 = multiSign.BalanceByAddr(MULITADD5, tokenType2);
        assertThat(queryInfo, containsString("200"));
        assertThat(queryInfo2, containsString("200"));
        assertThat(JSONObject.fromObject(queryInfo).getJSONObject("data").getString("total"), containsString("0"));
        assertThat(JSONObject.fromObject(queryInfo2).getJSONObject("data").getString("total"), containsString("0"));

        log.info("回收Token");
        String recycleInfo = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType, issueAm1);
        String recycleInfo2 = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType2, issueAm2);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType02),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        assertThat(recycleInfo, containsString("200"));
        assertThat(recycleInfo2, containsString("200"));


        log.info("查询余额判断回收成功与否");
        String queryInfo3 = multiSign.BalanceByAddr(IMPPUTIONADD,  tokenType);
        String queryInfo4 = multiSign.BalanceByAddr(IMPPUTIONADD,  tokenType2);
        assertThat(queryInfo3, containsString("200"));
        assertThat(queryInfo4, containsString("200"));
        assertThat(JSONObject.fromObject(queryInfo3).getJSONObject("data").getString("total"), containsString("0"));
        assertThat(JSONObject.fromObject(queryInfo4).getJSONObject("data").getString("total"), containsString("0"));


    }


    /**
     * Tc38归集地址向单签、多签地址转账异常测试
     * @throws Exception
     */

    @Test
    public  void TC38_transferSoloMultiInvalid() throws  Exception{
        log.info("查询归集地址中两种token余额");
        String response1 = multiSign.BalanceByAddr(IMPPUTIONADD,  tokenType);
        String response2 = multiSign.BalanceByAddr(IMPPUTIONADD,  tokenType2);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString(issueAm1));
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString(issueAm2));

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

//        assertThat(transferInfo2, containsString("400"));
//        assertThat(transferInfo3, containsString("400"));
//        assertThat(transferInfo4, containsString("400"));
//        assertThat(transferInfo5, containsString("400"));
//        assertThat(transferInfo6, containsString("400"));
//        assertThat(transferInfo7, containsString("400"));
        assertThat(JSONObject.fromObject(transferInfo2).getString("message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo3).getString("message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo4).getString("message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo5).getString("message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo6).getString("message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo7).getString("message"),equalTo("insufficient balance"));



        log.info("查询余额判断转账是否成功");
        String queryInfo = multiSign.BalanceByAddr( ADDRESS1, tokenType);
        String queryInfo2 = multiSign.BalanceByAddr(MULITADD5,  tokenType2);
        assertThat(queryInfo, containsString("200"));
        assertThat(queryInfo2, containsString("200"));
        assertThat(JSONObject.fromObject(queryInfo).getJSONObject("data").getString("total"), containsString("0"));
        assertThat(JSONObject.fromObject(queryInfo2).getJSONObject("data").getString("total"), containsString("0"));

        log.info("回收Token");
        String recycleInfo = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType, issueAm1);
        String recycleInfo2 = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType2, issueAm2);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType02),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        assertThat(recycleInfo, containsString("200"));
        assertThat(recycleInfo2, containsString("200"));


        log.info("查询余额判断回收成功与否");
        String queryInfo3 = multiSign.BalanceByAddr(IMPPUTIONADD,  tokenType);
        String queryInfo4 = multiSign.BalanceByAddr(IMPPUTIONADD,  tokenType2);
        assertThat(queryInfo3, containsString("200"));
        assertThat(queryInfo4, containsString("200"));
        assertThat(JSONObject.fromObject(queryInfo3).getJSONObject("data").getString("total"), containsString("0"));
        assertThat(JSONObject.fromObject(queryInfo4).getJSONObject("data").getString("total"), containsString("0"));


    }
    /**
     * Tc39 归集地址向两个单签转账异常测试
     * @throws Exception
     */

    @Test
    public void TC39_transferSoloInvalid()throws  Exception{  log.info("查询归集地址中两种token余额");
        String response1 = multiSign.BalanceByAddr(IMPPUTIONADD,  tokenType);
        String response2 = multiSign.BalanceByAddr(IMPPUTIONADD,  tokenType2);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString(issueAm1));
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString(issueAm2));

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

//        assertThat(transferInfo2, containsString("400"));
//        assertThat(transferInfo3, containsString("400"));
//        assertThat(transferInfo4, containsString("400"));
//        assertThat(transferInfo5, containsString("400"));
//        assertThat(transferInfo6, containsString("400"));
//        assertThat(transferInfo7, containsString("400"));
        assertThat(JSONObject.fromObject(transferInfo2).getString("message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo3).getString("message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo4).getString("message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo5).getString("message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo6).getString("message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo7).getString("message"),equalTo("insufficient balance"));



        log.info("查询余额判断转账是否成功");
        String queryInfo = multiSign.BalanceByAddr( ADDRESS1, tokenType);
        String queryInfo2 = multiSign.BalanceByAddr( ADDRESS2, tokenType2);
        assertThat(queryInfo, containsString("200"));
        assertThat(queryInfo2, containsString("200"));
        assertThat(JSONObject.fromObject(queryInfo).getJSONObject("data").getString("total"), containsString("0"));
        assertThat(JSONObject.fromObject(queryInfo2).getJSONObject("data").getString("total"), containsString("0"));

        log.info("回收Token");
        String recycleInfo = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType, issueAm1);
        String recycleInfo2 = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType2, issueAm2);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType02),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        assertThat(recycleInfo, containsString("200"));
        assertThat(recycleInfo2, containsString("200"));


        log.info("查询余额判断回收成功与否");
        String queryInfo3 = multiSign.BalanceByAddr(IMPPUTIONADD,  tokenType);
        String queryInfo4 = multiSign.BalanceByAddr(IMPPUTIONADD,  tokenType2);
        assertThat(queryInfo3, containsString("200"));
        assertThat(queryInfo4, containsString("200"));
        assertThat(JSONObject.fromObject(queryInfo3).getJSONObject("data").getString("total"), containsString("0"));
        assertThat(JSONObject.fromObject(queryInfo4).getJSONObject("data").getString("total"), containsString("0"));

    }

    /**
     * Tc238 多签地址向两个单签转账异常测试
     * @throws Exception
     */

    @Test
    public void TC238_MultiToSoloInvalid()throws  Exception{
        log.info("查询归集地址中两种token余额");
        String response1 = multiSign.BalanceByAddr(IMPPUTIONADD,  tokenType);
        String response2 = multiSign.BalanceByAddr(IMPPUTIONADD,  tokenType2);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString(issueAm1));
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString(issueAm2));
        String transferDataInit = "归集地址向" + "MULITADD6" + "转账100个" + tokenType + "归集地址向" + "MULITADD6" + "转账100.123个" + tokenType;
        List<Map> listInit = utilsClass.constructToken(MULITADD6, tokenType, issueAm1);
        List<Map> listInit2 = utilsClass.constructToken(MULITADD6, tokenType2, issueAm2, listInit);
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

//        assertThat(transferInfo2, containsString("400"));
//        assertThat(transferInfo3, containsString("400"));
//        assertThat(transferInfo4, containsString("400"));
//        assertThat(transferInfo5, containsString("400"));
//        assertThat(transferInfo6, containsString("400"));
//        assertThat(transferInfo7, containsString("400"));
        assertThat(JSONObject.fromObject(transferInfo2).getString("message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo3).getString("message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo4).getString("message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo5).getString("message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo6).getString("message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo7).getString("message"),equalTo("insufficient balance"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(transferInfoInit,utilsClass.sdkGetTxHashType02),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        log.info("查询余额判断转账是否成功");
        String queryInfo = multiSign.BalanceByAddr( ADDRESS1, tokenType);
        String queryInfo2 = multiSign.BalanceByAddr( ADDRESS2, tokenType2);
        assertThat(queryInfo, containsString("200"));
        assertThat(queryInfo2, containsString("200"));
        assertThat(JSONObject.fromObject(queryInfo).getJSONObject("data").getString("total"), containsString("0"));
        assertThat(JSONObject.fromObject(queryInfo2).getJSONObject("data").getString("total"), containsString("0"));

        log.info("回收Token");
        String recycleInfo = multiSign.Recycle(MULITADD6, PRIKEY4, tokenType, issueAm1);
        String recycleInfo2 = multiSign.Recycle(MULITADD6, PRIKEY4, tokenType2, issueAm2);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType02),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        assertThat(recycleInfo, containsString("200"));
        assertThat(recycleInfo2, containsString("200"));


        log.info("查询余额判断回收成功与否");
        String queryInfo3 = multiSign.BalanceByAddr(MULITADD6,  tokenType);
        String queryInfo4 = multiSign.BalanceByAddr(MULITADD6,  tokenType2);
        assertThat(queryInfo3, containsString("200"));
        assertThat(queryInfo4, containsString("200"));
        assertThat(JSONObject.fromObject(queryInfo3).getJSONObject("data").getString("total"), containsString("0"));
        assertThat(JSONObject.fromObject(queryInfo4).getJSONObject("data").getString("total"), containsString("0"));

    }

    /**
     * Tc239 多签地址向两个多签转账异常测试
     * @throws Exception
     */

    @Test
    public void TC239_MultiToMulitInvalid()throws  Exception{
        log.info("查询归集地址中两种token余额");
        String response1 = multiSign.BalanceByAddr(IMPPUTIONADD,  tokenType);
        String response2 = multiSign.BalanceByAddr(IMPPUTIONADD,  tokenType2);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString(issueAm1));
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString(issueAm2));
        String transferDataInit = "归集地址向" + "MULITADD6" + "转账100个" + tokenType + "归集地址向" + "MULITADD6" + "转账100.123个" + tokenType;
        List<Map> listInit = utilsClass.constructToken(MULITADD6, tokenType, issueAm1);
        List<Map> listInit2 = utilsClass.constructToken(MULITADD6, tokenType2, issueAm2, listInit);
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

//        assertThat(transferInfo2, containsString("400"));
//        assertThat(transferInfo3, containsString("400"));
//        assertThat(transferInfo4, containsString("400"));
//        assertThat(transferInfo5, containsString("400"));
//        assertThat(transferInfo6, containsString("400"));
//        assertThat(transferInfo7, containsString("400"));
        assertThat(JSONObject.fromObject(transferInfo2).getString("message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo3).getString("message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo4).getString("message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo5).getString("message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo6).getString("message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo7).getString("message"),equalTo("insufficient balance"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(transferInfoInit,utilsClass.sdkGetTxHashType02),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        log.info("查询余额判断转账是否成功");
        String queryInfo = multiSign.BalanceByAddr( MULITADD4, tokenType);
        String queryInfo2 = multiSign.BalanceByAddr( MULITADD5, tokenType2);
        assertThat(queryInfo, containsString("200"));
        assertThat(queryInfo2, containsString("200"));
        assertThat(JSONObject.fromObject(queryInfo).getJSONObject("data").getString("total"), containsString("0"));
        assertThat(JSONObject.fromObject(queryInfo2).getJSONObject("data").getString("total"), containsString("0"));

        log.info("回收Token");
        String recycleInfo = multiSign.Recycle(MULITADD6, PRIKEY4, tokenType, issueAm1);
        String recycleInfo2 = multiSign.Recycle(MULITADD6, PRIKEY4, tokenType2, issueAm2);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType02),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        assertThat(recycleInfo, containsString("200"));
        assertThat(recycleInfo2, containsString("200"));


        log.info("查询余额判断回收成功与否");
        String queryInfo3 = multiSign.BalanceByAddr(MULITADD6,  tokenType);
        String queryInfo4 = multiSign.BalanceByAddr(MULITADD6,  tokenType2);
        assertThat(queryInfo3, containsString("200"));
        assertThat(queryInfo4, containsString("200"));
        assertThat(JSONObject.fromObject(queryInfo3).getJSONObject("data").getString("total"), containsString("0"));
        assertThat(JSONObject.fromObject(queryInfo4).getJSONObject("data").getString("total"), containsString("0"));

    }

    /**
     * Tc240 多签地址向两个多签转账异常测试
     * @throws Exception
     */

    @Test
    public void TC240_MultiToSoloMulitInvalid()throws  Exception{
        log.info("查询归集地址中两种token余额");
        String response1 = multiSign.BalanceByAddr(IMPPUTIONADD,  tokenType);
        String response2 = multiSign.BalanceByAddr(IMPPUTIONADD,  tokenType2);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString(issueAm1));
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString(issueAm2));
        String transferDataInit = "归集地址向" + "MULITADD6" + "转账100个" + tokenType + "归集地址向" + "MULITADD6" + "转账100.123个" + tokenType;
        List<Map> listInit = utilsClass.constructToken(MULITADD6, tokenType, issueAm1);
        List<Map> listInit2 = utilsClass.constructToken(MULITADD6, tokenType2, issueAm2, listInit);
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

//        assertThat(transferInfo2, containsString("400"));
//        assertThat(transferInfo3, containsString("400"));
//        assertThat(transferInfo4, containsString("400"));
//        assertThat(transferInfo5, containsString("400"));
//        assertThat(transferInfo6, containsString("400"));
//        assertThat(transferInfo7, containsString("400"));
        assertThat(JSONObject.fromObject(transferInfo2).getString("message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo3).getString("message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo4).getString("message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo5).getString("message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo6).getString("message"),equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo7).getString("message"),equalTo("insufficient balance"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(transferInfoInit,utilsClass.sdkGetTxHashType02),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);



        log.info("查询余额判断转账是否成功");
        String queryInfo = multiSign.BalanceByAddr( ADDRESS1, tokenType);
        String queryInfo2 = multiSign.BalanceByAddr( MULITADD5, tokenType2);
        assertThat(queryInfo, containsString("200"));
        assertThat(queryInfo2, containsString("200"));
        assertThat(JSONObject.fromObject(queryInfo).getJSONObject("data").getString("total"), containsString("0"));
        assertThat(JSONObject.fromObject(queryInfo2).getJSONObject("data").getString("total"), containsString("0"));

        log.info("回收Token");
        String recycleInfo = multiSign.Recycle(MULITADD6, PRIKEY4, tokenType, issueAm1);
        String recycleInfo2 = multiSign.Recycle(MULITADD6, PRIKEY4, tokenType2, issueAm2);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType02),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        assertThat(recycleInfo, containsString("200"));
        assertThat(recycleInfo2, containsString("200"));


        log.info("查询余额判断回收成功与否");
        String queryInfo3 = multiSign.BalanceByAddr(MULITADD6,  tokenType);
        String queryInfo4 = multiSign.BalanceByAddr(MULITADD6,  tokenType2);
        assertThat(queryInfo3, containsString("200"));
        assertThat(queryInfo4, containsString("200"));
        assertThat(JSONObject.fromObject(queryInfo3).getJSONObject("data").getString("total"), containsString("0"));
        assertThat(JSONObject.fromObject(queryInfo4).getJSONObject("data").getString("total"), containsString("0"));

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
        assertThat(response3, containsString("Duplicate pubkey"));

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
        assertThat(JSONObject.fromObject(response).getJSONObject("data").getString("address"), equalTo(MULITADD1));
        map.put("1", PUBKEY2);
        map.put("2", PUBKEY1);
        String response2 = multiSign.genMultiAddress(M, map);
        assertThat(response2, containsString("200"));
        assertThat(JSONObject.fromObject(response2).getJSONObject("data").getString("address"), equalTo(MULITADD1));

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
        assertThat(JSONObject.fromObject(response).getJSONObject("data").getString("address"), equalTo(MULITADD1));
        map.remove("3");
        String response2 = multiSign.genMultiAddress(1, map);
        assertThat(response2, containsString("200"));
        assertThat(JSONObject.fromObject(response2).getJSONObject("data").getString("address"), equalTo(MULITADD4));
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
        String Tx1 = JSONObject.fromObject(response).getJSONObject("data").getString("tx");
        String response2 = multiSign.Sign(Tx1, PRIKEY6);//不带密码
        String response3 = multiSign.Sign(Tx1, PRIKEY6, PWD7);//密码错误
        String response4 = multiSign.Sign(Tx1, "112");  //非法密钥
        String response5 = multiSign.Sign(Tx1, PRIKEY3);   //无关密钥
        String response6 = multiSign.Sign("123", PRIKEY1);//Tx非法
        String response7 = multiSign.Sign(Tx1, PRIKEY1);
        String Tx2 = JSONObject.fromObject(response7).getJSONObject("data").getString("tx");
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
        String queryInfo = multiSign.BalanceByAddr(IMPPUTIONADD,  tokenType);
        assertThat(queryInfo, containsString("200"));
        assertThat(JSONObject.fromObject(queryInfo).getJSONObject("data").getString("total"), containsString("0"));

    }



//    /**
//     * TC13 14 20 多签查询异常测试
//     * 私钥 地址 tokenType
//     */
//    @Test
//    public void TC13_20balanceInvalid() {
//        String tokenType = "cx-8oVNI";
//        String queryInfo = multiSign.BalanceByAddr(IMPPUTIONADD, "0", tokenType);
//        String queryInfo1 = multiSign.BalanceByAddr(IMPPUTIONADD, PRIKEY3, tokenType);
//        String queryInfo2 = multiSign.BalanceByAddr(IMPPUTIONADD, "1234abc", tokenType);
//        String queryInfo3 = multiSign.BalanceByAddr("0", PRIKEY4, tokenType);
//        String queryInfo4 = multiSign.BalanceByAddr("Soirv9ikykFYbBLMExy4zUTUa", PRIKEY4, tokenType);
//        String queryInfo5 = multiSign.BalanceByAddr(IMPPUTIONADD, PRIKEY4, "0");
//        String queryInfo6 = multiSign.BalanceByAddr(IMPPUTIONADD, PRIKEY4, "abc123e");
//        assertThat(queryInfo, containsString("400"));
//        assertThat(queryInfo, containsString("Private key must be base64 string"));
//        assertThat(queryInfo1, containsString("400"));
//        assertThat(queryInfo1, containsString("Multiaddr is not matching for the prikey"));
//        assertThat(queryInfo2, containsString("400"));
//        assertThat(queryInfo2, containsString("Private key must be base64 string"));
//        assertThat(queryInfo3, containsString("400"));
//        assertThat(queryInfo4, containsString("400"));
//        assertThat(queryInfo4, containsString("Invalid multiple address"));
//        assertThat(queryInfo5, containsString("200"));
//        assertThat(JSONObject.fromObject(queryInfo5).getJSONObject("data").getString("total"), containsString("0"));
//        assertThat(queryInfo6, containsString("200"));
//        assertThat(JSONObject.fromObject(queryInfo6).getJSONObject("data").getString("total"), containsString("0"));
//
//    }

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
        String queryInfo = multiSign.BalanceByAddr(IMPPUTIONADD,  tokenType);
        Boolean flag=JSONObject.fromObject(queryInfo).getJSONObject("data").getString("total").equals("0");
        if(flag){
            BeforeCondition beforeCondition=new BeforeCondition();
            beforeCondition.T284_BeforeCondition(tokenType);

            commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                    utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);


            queryInfo = multiSign.BalanceByAddr(IMPPUTIONADD,  tokenType);
        }

        assertThat(queryInfo, containsString("200"));
        assertEquals(JSONObject.fromObject(queryInfo).getJSONObject("data").getString("total").equals("0"), false);
        /**
         * 如果测试不通过请执行BeforeConditon类中的第二个方法.发行相应的币种
         */
        List<Map> list0 = utilsClass.constructToken(MULITADD4, tokenType, "1");

        String transferInfo0 = multiSign.Transfer(PRIKEY4, "cx-test", IMPPUTIONADD, list0);//1 归集地址向单签地址转账

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);


        assertThat(transferInfo0, containsString("200"));
        String queryInfo1 = multiSign.BalanceByAddr(MULITADD4,  tokenType);
        assertThat(transferInfo0, containsString("200"));
        assertEquals(JSONObject.fromObject(queryInfo1).getJSONObject("data").getString("total").equals("0"), false);

        List<Map> list1 = utilsClass.constructToken(MULITADD3, tokenType, "0.5");
        String transferInfo1 = multiSign.Transfer(PRIKEY4, "cx-test", IMPPUTIONADD, list1);//多签地址向归集地址转账
        assertThat(transferInfo1, containsString("200"));

    }

    @Test
    public void TC980_MultiAddrNotInDB() throws Exception{
        String MultiAddr="Ss6iNcwoJFf5EWbuqqU7o96XNVH9izRKk9exj96giAntEFPaWZ6";
//        assertThat(multiSign.BalanceByAddr(MultiAddr,PRIKEY1,"test"),
//                containsString("GetMulBalance : Multiaddr not generated, cannot be used !"));
        assertThat(multiSign.BalanceByAddr(MultiAddr,"test"),
                containsString("\"total\":\"0\""));
    }

    @Test
    public void TC979_recoverUnIssueToken()throws Exception{
        String unIssueToken = "unIssueToken";
        String resp1 = multiSign.recoverFrozenToken(unIssueToken);
        Thread.sleep(SLEEPTIME);
        assertEquals(true,store.GetTxDetail(JSONObject.fromObject(resp1).getString("data")).contains("failed to find transaction"));
    }


    @Test
    public void TC979_recoverUnFreezedToken()throws Exception{
        String resp1 = multiSign.recoverFrozenToken(tokenType);
        Thread.sleep(SLEEPTIME);
        assertEquals(true,store.GetTxDetail(JSONObject.fromObject(resp1).getString("data")).contains("failed to find transaction"));
    }


    //发行时大小写敏感性检查
//    @Test
    public void issueTokenIgnoreCase()throws Exception{
        String issueResp = multiSign.issueToken(IMPPUTIONADD,IMPPUTIONADD,tokenType.toLowerCase(),
                issueAm1,"发行已有tokentype字符全部小写的token");
        assertEquals("200",JSONObject.fromObject(issueResp).getString("state"));

        String issueResp2 = multiSign.issueToken(IMPPUTIONADD,IMPPUTIONADD,tokenType.toUpperCase(),
                issueAm1,"发行已有tokentype字符全部大写的token");
        assertEquals("200",JSONObject.fromObject(issueResp2).getString("state"));

        sleepAndSaveInfo(SLEEPTIME);

        assertEquals(issueAm1,commonFunc.GetBalance(IMPPUTIONADD,tokenType.toLowerCase()));
        assertEquals(issueAm1,commonFunc.GetBalance(IMPPUTIONADD,tokenType.toUpperCase()));

    }

    //tokenType大小写敏感性检查
//    @Test
    public void testMatchCaseQueryBalance()throws Exception{

        //查询余额账户地址大小写敏感性检查  当前不敏感
        log.info("查询余额账户地址大小写敏感性检查  当前不敏感");
        assertEquals(issueAm1,commonFunc.GetBalance(IMPPUTIONADD.toLowerCase(),tokenType));
        assertEquals(issueAm1,commonFunc.GetBalance(IMPPUTIONADD.toUpperCase(),tokenType));


        log.info("查询余额tokentype敏感检查");
        //查询余额tokentype敏感检查
        assertEquals("0",commonFunc.GetBalance(IMPPUTIONADD,tokenType.toUpperCase()));
        assertEquals("0",commonFunc.GetBalance(IMPPUTIONADD,tokenType.toLowerCase()));

    }

    //    @Test
    public void testMatchCaseTransfer()throws Exception{

        //转账检查大小写敏感
        //检查小写tokentype转账
        log.info("转账检查大小写敏感");
        List<Map> list = utilsClass.constructToken(MULITADD1, tokenType.toLowerCase(), issueAm1);
        String transferInfo = multiSign.Transfer(PRIKEY4,"转账全小写tokentype",IMPPUTIONADD, list);
        assertEquals("400",JSONObject.fromObject(transferInfo).getString("state"));
        assertEquals("Insufficient Balance",JSONObject.fromObject(transferInfo).getString("data"));

        //检查小写tokentype转账
        list = utilsClass.constructToken(MULITADD1, tokenType.toUpperCase(), issueAm1);
        transferInfo = multiSign.Transfer(PRIKEY4,"转账全大写tokentype",IMPPUTIONADD, list);
        assertEquals("400",JSONObject.fromObject(transferInfo).getString("state"));
        assertEquals("Insufficient Balance",JSONObject.fromObject(transferInfo).getString("data"));
    }

    //    @Test
    public void testMatchCaseDestroy()throws Exception{
        List<Map> list;
        //回收检查大小写敏感
        log.info("回收检查大小写敏感");

        String desResp = multiSign.Recycle(IMPPUTIONADD,PRIKEY4,tokenType.toLowerCase(),"10");
        assertEquals("400",JSONObject.fromObject(desResp).getString("state"));
        assertEquals("Insufficient Balance",JSONObject.fromObject(desResp).getString("data"));

        desResp = multiSign.Recycle(IMPPUTIONADD,PRIKEY4,tokenType.toUpperCase(),"10");
        assertEquals("400",JSONObject.fromObject(desResp).getString("state"));
        assertEquals("Insufficient Balance",JSONObject.fromObject(desResp).getString("data"));
    }

    //    @Test
    public void testMatchCaseFreezeRecover()throws Exception{
        //冻结检查大小写
        log.info("冻结检查大小写");
        String freezeResp = multiSign.freezeToken(tokenType.toLowerCase());
        assertEquals("200",JSONObject.fromObject(freezeResp).getString("state"));
        String hash1 = JSONObject.fromObject(freezeResp).getString("data");

        freezeResp = multiSign.freezeToken(tokenType.toUpperCase());
        assertEquals("200",JSONObject.fromObject(freezeResp).getString("state"));
        String hash2 = JSONObject.fromObject(freezeResp).getString("data");
        sleepAndSaveInfo(SLEEPTIME);

        assertEquals("404",JSONObject.fromObject(store.GetTxDetail(hash1)).getString("state"));
        assertEquals("404",JSONObject.fromObject(store.GetTxDetail(hash2)).getString("state"));


        String recoverResp = multiSign.recoverFrozenToken(tokenType);
        assertEquals("400",JSONObject.fromObject(recoverResp).getString("state"));
        assertEquals(true,JSONObject.fromObject(recoverResp).getString("data").contains("has not been freezed!"));

        //冻结tokentype测试解除
        String freezeBeforeRecover = multiSign.recoverFrozenToken(tokenType);
        assertEquals("200",JSONObject.fromObject(freezeBeforeRecover).getString("state"));

        sleepAndSaveInfo(SLEEPTIME);
        recoverResp = multiSign.recoverFrozenToken(tokenType.toUpperCase());
        assertEquals("400",JSONObject.fromObject(recoverResp).getString("state"));
        assertEquals(true,JSONObject.fromObject(recoverResp).getString("data").contains("has not been freezed!"));

        recoverResp = multiSign.recoverFrozenToken(tokenType.toLowerCase());
        assertEquals("400",JSONObject.fromObject(recoverResp).getString("state"));
        assertEquals(true,JSONObject.fromObject(recoverResp).getString("data").contains("has not been freezed!"));

        String recoverR2 = multiSign.recoverFrozenToken(tokenType);
        assertEquals("200",JSONObject.fromObject(recoverR2).getString("state"));
    }

}
