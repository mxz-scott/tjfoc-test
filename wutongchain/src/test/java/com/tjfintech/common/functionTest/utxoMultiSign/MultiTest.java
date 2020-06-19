package com.tjfintech.common.functionTest.utxoMultiSign;


import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.List;
import java.util.Map;

//import static com.tjfintech.common.functionTest.store.StoreTest.SLEEPTIME;
import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.*;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MultiTest {
    TestBuilder testBuilder= TestBuilder.getInstance();
    MultiSign multiSign =testBuilder.getMultiSign();
    SoloSign soloSign=testBuilder.getSoloSign();
    UtilsClass utilsClass=new UtilsClass();
    private static String tokenType;
    private static String tokenType2;

    private static String issueAmount1;
    private static String issueAmount2;

    private static String actualAmount1;
    private static String actualAmount2;

    CommonFunc commonFunc = new CommonFunc();


    @BeforeClass
    public static void beforeClass() throws Exception {
        CommonFunc commonFuncTe = new CommonFunc();
        UtilsClass utilsClassTe = new UtilsClass();
        BeforeCondition bf = new BeforeCondition();
        bf.updatePubPriKey();
        bf.collAddressTest();//有多签地址创建及添加
        commonFuncTe.sdkCheckTxOrSleep(commonFuncTe.getTxHash(globalResponse,utilsClassTe.sdkGetTxHashType21),
                utilsClassTe.sdkGetTxDetailTypeV2,SLEEPTIME);
    }

    @Before
    public void beforeConfig() throws Exception {

        issueAmount1 = "1000.12345678912345";
        issueAmount2 = "1000.876543212345";

        if (UtilsClass.PRECISION == 10) {
            actualAmount1 = "1000.1234567891";
            actualAmount2 = "1000.8765432123";
        }else {
            actualAmount1 = "1000.123456";
            actualAmount2 = "1000.876543";
        }

        log.info("多签发行两种token");
        //两次发行之前不可以有sleep时间
        tokenType = IssueToken(5, issueAmount1);
        tokenType2 = IssueToken(6, issueAmount2);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);


        log.info("查询归集地址中两种token余额");
        String response1 = multiSign.BalanceByAddr(IMPPUTIONADD,tokenType);
        String response2 = multiSign.BalanceByAddr(IMPPUTIONADD,tokenType2);
        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
        assertEquals(actualAmount1,JSONObject.fromObject(response1).getJSONObject("data").getString("total"));
        assertEquals("200",JSONObject.fromObject(response2).getString("state"));
        assertEquals(actualAmount2,JSONObject.fromObject(response2).getJSONObject("data").getString("total"));
    }

    /**
     *  测试最大发行量
     *
     */
    @Test
    public void TC001_TestMaxValue()throws Exception {

        if (UtilsClass.PRECISION == 10) {
            actualAmount1 = "900000000";
        }else {
            actualAmount1 = "18446744073709";
        }
        tokenType = IssueToken(5, actualAmount1);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);


        log.info("查询归集地址中token余额");
        String response1 = multiSign.BalanceByAddr(IMPPUTIONADD, tokenType);

        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
        assertEquals(actualAmount1,JSONObject.fromObject(response1).getJSONObject("data").getString("total"));
    }




    /**
     * 多签发行检查发行地址注册、未注册时的发行结果
     * @throws Exception
     */
    @Test
    public void    TC1280_checkMultiIssueAddr()throws Exception {
        //先前已经注册发行及归集地址IMPPUTIONADD,确认发行无异常
        tokenType = IssueToken(5, "1000",MULITADD3);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);


        String response1 = multiSign.BalanceByAddr(MULITADD3,tokenType);
        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
        assertEquals("1000",JSONObject.fromObject(response1).getJSONObject("data").getString("total"));

        //删除发行地址，保留归集地址
        String response2=multiSign.delissueaddressRemovePri(IMPPUTIONADD);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType21),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        tokenType = IssueToken(6, "1000",MULITADD3);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        String response3 = multiSign.BalanceByAddr(MULITADD3, tokenType);
        assertEquals("200",JSONObject.fromObject(response3).getString("state"));
        assertEquals("0",JSONObject.fromObject(response3).getJSONObject("data").getString("total"));

        //删除发行地址和归集地址
        String response41=multiSign.delCollAddressRemovePri(MULITADD3);
        assertEquals("200",JSONObject.fromObject(response41).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType21),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        tokenType = IssueToken(7, "1000",MULITADD3);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);


        String response42 = multiSign.BalanceByAddr(MULITADD3, tokenType);
        assertEquals("200",JSONObject.fromObject(response42).getString("state"));
        assertEquals("0",JSONObject.fromObject(response42).getJSONObject("data").getString("total"));

        //重新添加发行地址，归集地址已删除
        String response5=multiSign.addissueaddressRemovePri(IMPPUTIONADD);
        assertEquals("200",JSONObject.fromObject(response5).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType21),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        tokenType = IssueToken(7, "1000",MULITADD3);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);


        String response53 = multiSign.BalanceByAddr(MULITADD3,tokenType);
        assertEquals("200",JSONObject.fromObject(response53).getString("state"));
        assertEquals("0",JSONObject.fromObject(response53).getJSONObject("data").getString("total"));

        //重新添加发行地址和归集地址
        String response6=multiSign.collAddressRemovePri(MULITADD3);
        assertEquals("200",JSONObject.fromObject(response6).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType21),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        tokenType = IssueToken(7, "1000",MULITADD3);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);


        String response62 = multiSign.BalanceByAddr(MULITADD3,tokenType);
        assertEquals("200",JSONObject.fromObject(response62).getString("state"));
        assertEquals("1000",JSONObject.fromObject(response62).getJSONObject("data").getString("total"));
    }


    /**
     * 多签发行token。接收地址不为本身。指定其他多签地址
     * @throws Exception
     */
     @Test
     public void TC994_issueToOther()throws Exception {
         tokenType = IssueToken(5, "1000",MULITADD3);
         tokenType2 = IssueToken(5, "1000",MULITADD4);

         commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                 utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);


         String response1 = multiSign.BalanceByAddr(MULITADD3,tokenType);
         String response2 = multiSign.BalanceByAddr(MULITADD4,tokenType2);
         assertEquals("200",JSONObject.fromObject(response1).getString("state"));
         assertEquals("1000",JSONObject.fromObject(response1).getJSONObject("data").getString("total"));
         assertEquals("200",JSONObject.fromObject(response2).getString("state"));
         assertEquals("1000",JSONObject.fromObject(response2).getJSONObject("data").getString("total"));
     }




    /**
     * Tc03多签正常流程-发币：签名：查询：转账：查询:回收：查询
     *
     */
    @Test
    public void TC03_multiProgress() throws Exception {
        String transferData = "归集地址向" + MULITADD4 + "转账10个" + tokenType;
        log.info(transferData);
        List<Map>list=utilsClass.constructToken(MULITADD4,tokenType,"10");
        log.info(transferData);
        String transferInfo= multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD,list);
        Thread.sleep(SLEEPTIME);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);


        assertThat(transferInfo, containsString("200"));

        String amount1;

        if (UtilsClass.PRECISION == 10) {
            amount1 = "990.1234567891";
        }else {
            amount1 = "990.123456";
        }

        log.info("查询归集地址跟MULITADD4余额，判断转账是否成功");
        String queryInfo = multiSign.BalanceByAddr(IMPPUTIONADD, tokenType);
        String queryInfo2 = multiSign.BalanceByAddr(MULITADD4,  tokenType);
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
        assertEquals(amount1,JSONObject.fromObject(queryInfo).getJSONObject("data").getString("total"));
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("state"));
        assertEquals("10",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString("total"));

        log.info("回收归集地址跟MULITADD4的新发token");
        String recycleInfo = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType, amount1);
        String recycleInfo2 = multiSign.Recycle(MULITADD4, PRIKEY1, tokenType, "10");
        assertEquals("200",JSONObject.fromObject(recycleInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("state"));


        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);


        log.info("查询回收后账户余额是否为0");
        String queryInfo3 = multiSign.BalanceByAddr(IMPPUTIONADD,  tokenType);
        String queryInfo4 = multiSign.BalanceByAddr(MULITADD4,tokenType);
        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("state"));
        assertEquals("0",JSONObject.fromObject(queryInfo3).getJSONObject("data").getString("total"));
        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("state"));
        assertEquals("0",JSONObject.fromObject(queryInfo4).getJSONObject("data").getString("total"));


    }


    /**
     * 精度测试
     *
     */
    @Test
    public void TC03_PrecisionTest() throws Exception {


        String transferData = "归集地址向" + MULITADD4 + "转账" + tokenType;
        log.info(transferData);
        List<Map>list=utilsClass.constructToken(MULITADD4,tokenType,issueAmount1);
        log.info(transferData);
        String transferInfo= multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD,list);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);


        assertThat(transferInfo, containsString("200"));

        String amount1;

        if (UtilsClass.PRECISION == 10) {
            amount1 = "1000.1234567891";
        }else {
            amount1 = "1000.123456";
        }

        log.info("查询归集地址跟MULITADD4余额，判断转账是否成功");
        String queryInfo = multiSign.BalanceByAddr(IMPPUTIONADD,  tokenType);
        String queryInfo2 = multiSign.BalanceByAddr(MULITADD4, tokenType);
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
        assertEquals("0",JSONObject.fromObject(queryInfo).getJSONObject("data").getString("total"));
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("state"));
        assertEquals(amount1,JSONObject.fromObject(queryInfo2).getJSONObject("data").getString("total"));

        log.info("回收归集地址跟MULITADD4的新发token");
        String recycleInfo2 = multiSign.Recycle(MULITADD4, PRIKEY1, tokenType, issueAmount1);
        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);


        log.info("查询回收后账户余额是否为0");
        String queryInfo3 = multiSign.BalanceByAddr(IMPPUTIONADD,  tokenType);
        String queryInfo4 = multiSign.BalanceByAddr(MULITADD4, tokenType);
        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("state"));
        assertEquals("0",JSONObject.fromObject(queryInfo3).getJSONObject("data").getString("total"));
        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("state"));
        assertEquals("0",JSONObject.fromObject(queryInfo4).getJSONObject("data").getString("total"));

        String queryInfo5 = multiSign.QueryZero(tokenType);
        assertEquals(amount1,JSONObject.fromObject(queryInfo5).getJSONObject("data").getJSONObject("detail").getString(tokenType));

    }

    /**
     * 小数量发行
     *
     */
    @Test
    public void TC_MultiMinIssue()throws Exception{
        String minToken = "TxTypeMulMin-" + UtilsClass.Random(6);
        String minData = "多签" + IMPPUTIONADD + "发行token " + minToken;
        String minAmount = "";

        if (UtilsClass.PRECISION == 10) {
            minAmount = "0.0000000001";
        }else {
            minAmount = "0.000001";
        }
        log.info(minData);

        log.info("多签发行小数量token");
        String response = multiSign.issueTokenCarryPri(IMPPUTIONADD,minToken,minAmount,PRIKEY4,minData);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        
        log.info("查询归集地址中token余额");
        String response1 = multiSign.BalanceByAddr(IMPPUTIONADD,minToken);
        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
        assertEquals(minAmount,JSONObject.fromObject(response1).getJSONObject("data").getString("total"));
    }


    /**
     * 小数量转账、回收测试
     *
     */
    @Test
    public void TC_MultiMiniTest() throws Exception {
        String transferData = "归集地址向" + MULITADD4 + "转账" + tokenType;
        log.info(transferData);
        List<Map>list=utilsClass.constructToken(MULITADD4,tokenType,"0.02");
        log.info(transferData);
        String transferInfo= multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD,list);
        assertThat(transferInfo, containsString("200"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);


        String amount1;

        if (UtilsClass.PRECISION == 10) {
            amount1 = "1000.1034567891";
        }else {
            amount1 = "1000.103456";
        }

        log.info("查询归集地址跟MULITADD4余额，判断转账是否成功");
        String queryInfo = multiSign.BalanceByAddr(IMPPUTIONADD,  tokenType);
        String queryInfo2 = multiSign.BalanceByAddr(MULITADD4,  tokenType);
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
        assertEquals(amount1,JSONObject.fromObject(queryInfo).getJSONObject("data").getString("total"));
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("state"));
        assertEquals("0.02",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString("total"));

        log.info("回收MULITADD4的新发token");
        String recycleInfo2 = multiSign.Recycle(MULITADD4, PRIKEY1, tokenType, "0.01");
        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);


        log.info("查询回收后账户余额是否为0.01");
        String queryInfo3 = multiSign.BalanceByAddr(IMPPUTIONADD, tokenType);
        String queryInfo4 = multiSign.BalanceByAddr(MULITADD4, tokenType);
        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("state"));
        assertEquals(amount1,JSONObject.fromObject(queryInfo3).getJSONObject("data").getString("total"));
        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("state"));
        assertEquals("0.01",JSONObject.fromObject(queryInfo4).getJSONObject("data").getString("total"));

        String queryInfo5 = multiSign.QueryZero(tokenType);
        assertEquals("0.01",JSONObject.fromObject(queryInfo5).getJSONObject("data").getJSONObject("detail").getString(tokenType));

    }


    /**
     * Tc024锁定后转账:
     *
     */
    @Test
    public void TC024_TransferAfterFrozen() throws Exception {

        //20190411增加锁定步骤后进行转账
        log.info("锁定待转账Token: "+tokenType);
        String resp=multiSign.freezeToken(tokenType);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType21),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        log.info("查询归集地址中两种token余额");
        String response1 = multiSign.BalanceByAddr(IMPPUTIONADD,  tokenType);
        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
        assertEquals(actualAmount1,JSONObject.fromObject(response1).getJSONObject("data").getString("total"));

        String transferData = "归集地址向MULITADD4转账10个" + tokenType;
        List<Map>list=utilsClass.constructToken(MULITADD4,tokenType,"10");
        log.info(transferData);

        String transferInfo= multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD,list);//相同币种
        assertEquals("500",JSONObject.fromObject(transferInfo).getString("state"));


//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
//                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);


        log.info("查询余额判断转账是否成功");
        String queryInfo= multiSign.BalanceByAddr(MULITADD4,tokenType);
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
        assertEquals("0",JSONObject.fromObject(queryInfo).getJSONObject("data").getString("total"));

        log.info("解除锁定待转账Token: "+tokenType);
        String resp1=multiSign.recoverFrozenToken(tokenType);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType21),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);


        log.info("查询归集地址中两种token余额");
        response1 = multiSign.BalanceByAddr(IMPPUTIONADD, tokenType);
        String response2 = multiSign.BalanceByAddr(IMPPUTIONADD, tokenType2);
        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
        assertEquals(actualAmount1,JSONObject.fromObject(response1).getJSONObject("data").getString("total"));
        assertEquals("200",JSONObject.fromObject(response2).getString("state"));
        assertEquals(actualAmount2,JSONObject.fromObject(response2).getJSONObject("data").getString("total"));

        transferData = "归集地址向" + "MULITADD4" + "转账10个" + tokenType+"归集地址向" + "MULITADD4" + "转账10个" + tokenType;
        list=utilsClass.constructToken(MULITADD4,tokenType,"10");
        List<Map>list2=utilsClass.constructToken(MULITADD5,tokenType2,"10",list);
        List<Map>list3=utilsClass.constructToken(MULITADD5,tokenType,"10",list);
        log.info(transferData);
        transferInfo= multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD,list2);//不同币种

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);


        String transferInfo2= multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD,list3);//相同币种
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);


        log.info("查询余额判断转账是否成功");
        queryInfo= multiSign.BalanceByAddr(MULITADD4,tokenType);
        String queryInfo2= multiSign.BalanceByAddr(MULITADD5,tokenType2);
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("state"));
        assertEquals("20",JSONObject.fromObject(queryInfo).getJSONObject("data").getString("total"));
        assertEquals("10",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString("total"));

        multiSign.BalanceByAddr(IMPPUTIONADD,tokenType);
        multiSign.BalanceByAddr(IMPPUTIONADD, tokenType2);

        String amount1, amount2;

        if (UtilsClass.PRECISION == 10) {
            amount1 = "970.1234567891";
            amount2 = "990.8765432123";
        }else {
            amount1 = "970.123456";
            amount2 = "990.876543";
        }


        log.info("回收Token");
        String recycleInfo = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType, amount1);
        String recycleInfo2 = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType2, amount2);
        String recycleInfo3 = multiSign.Recycle(MULITADD4, PRIKEY1, tokenType, "20");
        String recycleInfo4 = multiSign.Recycle(MULITADD5, PRIKEY1, tokenType2, "10");
        String recycleInfo5 = multiSign.Recycle(MULITADD5, PRIKEY1, tokenType, "10");

        assertEquals("200",JSONObject.fromObject(recycleInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo3).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo4).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo5).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);


        log.info("查询余额判断回收成功与否");
        String queryInfo3= multiSign.BalanceByAddr(MULITADD4,tokenType);
        String queryInfo4= multiSign.BalanceByAddr(MULITADD5,tokenType2);
        String queryInfo5= multiSign.BalanceByAddr(MULITADD5,tokenType);

        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo5).getString("state"));

        assertEquals("0",JSONObject.fromObject(queryInfo3).getJSONObject("data").getString("total"));
        assertEquals("0",JSONObject.fromObject(queryInfo4).getJSONObject("data").getString("total"));
        assertEquals("0",JSONObject.fromObject(queryInfo5).getJSONObject("data").getString("total"));

    }

    /**
     *TC19归集地址向两个多签地址转账
     * 发两种币-查询归集地址-转账两个地址不同token-查询-回收-查询
     * @throws Exception
     */
     @Test
     public void TC19_transferMulti()throws  Exception{

         log.info("查询归集地址中两种token余额");
         String response1 = multiSign.BalanceByAddr(IMPPUTIONADD,tokenType);
         String response2 = multiSign.BalanceByAddr(IMPPUTIONADD,tokenType2);
         assertEquals("200",JSONObject.fromObject(response1).getString("state"));
         assertEquals("200",JSONObject.fromObject(response2).getString("state"));
         assertEquals(actualAmount1,JSONObject.fromObject(response1).getJSONObject("data").getString("total"));
         assertEquals(actualAmount2,JSONObject.fromObject(response2).getJSONObject("data").getString("total"));

         String transferData = "归集地址向" + "MULITADD4" + "转账10个" + tokenType+"归集地址向" + "MULITADD4" + "转账10个" + tokenType;
         List<Map>list=utilsClass.constructToken(MULITADD4,tokenType,"10");
         List<Map>list2=utilsClass.constructToken(MULITADD5,tokenType2,"10",list);
         List<Map>list3=utilsClass.constructToken(MULITADD5,tokenType,"10",list);
         log.info(transferData);
         String transferInfo= multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD,list2);//不同币种

         commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                 utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);


         String transferInfo2= multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD,list3);//相同币种
         assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
         assertEquals("200",JSONObject.fromObject(transferInfo2).getString("state"));

         commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                 utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);


         log.info("查询余额判断转账是否成功");
         String queryInfo= multiSign.BalanceByAddr(MULITADD4,tokenType);
         String queryInfo2= multiSign.BalanceByAddr(MULITADD5,tokenType2);
         assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
         assertEquals("200",JSONObject.fromObject(queryInfo2).getString("state"));
         assertEquals("20",JSONObject.fromObject(queryInfo).getJSONObject("data").getString("total"));
         assertEquals("10",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString("total"));

         multiSign.BalanceByAddr(IMPPUTIONADD,tokenType);
         multiSign.BalanceByAddr(IMPPUTIONADD,tokenType2);

         String amount1, amount2;

         if (UtilsClass.PRECISION == 10) {
             amount1 = "970.1234567891";
             amount2 = "990.8765432123";
         }else {
             amount1 = "970.123456";
             amount2 = "990.876543";
         }

         log.info("回收Token");
         String recycleInfo = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType, amount1);
         String recycleInfo2 = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType2, amount2);
         String recycleInfo3 = multiSign.Recycle(MULITADD4, PRIKEY1, tokenType, "20");
         String recycleInfo4 = multiSign.Recycle(MULITADD5, PRIKEY1, tokenType2, "10");
         String recycleInfo5 = multiSign.Recycle(MULITADD5, PRIKEY1, tokenType, "10");

         assertEquals("200",JSONObject.fromObject(recycleInfo).getString("state"));
         assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("state"));
         assertEquals("200",JSONObject.fromObject(recycleInfo3).getString("state"));
         assertEquals("200",JSONObject.fromObject(recycleInfo4).getString("state"));
         assertEquals("200",JSONObject.fromObject(recycleInfo5).getString("state"));

         commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                 utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);


         log.info("查询余额判断回收成功与否");
         String queryInfo3= multiSign.BalanceByAddr(MULITADD4,tokenType);
         String queryInfo4= multiSign.BalanceByAddr(MULITADD5,tokenType2);
         String queryInfo5= multiSign.BalanceByAddr(MULITADD5,tokenType);
         assertEquals("200",JSONObject.fromObject(queryInfo3).getString("state"));
         assertEquals("200",JSONObject.fromObject(queryInfo4).getString("state"));
         assertEquals("200",JSONObject.fromObject(queryInfo5).getString("state"));
         assertEquals("0",JSONObject.fromObject(queryInfo3).getJSONObject("data").getString("total"));
         assertEquals("0",JSONObject.fromObject(queryInfo4).getJSONObject("data").getString("total"));
         assertEquals("0",JSONObject.fromObject(queryInfo5).getJSONObject("data").getString("total"));
     }


    /**
     *TC31归集地址向单签和多签地址转账
     * 发两种币-查询归集地址-转账两个地址不同token-查询-回收-查询
     * @throws Exception
     */
    @Test
    public void TC31_transferSoloMulti()throws  Exception{

        String transferData = "归集地址向" + "ADDRESS1" + "转账10个" + tokenType+"归集地址向" + "MULITADD4" + "转账10个" + tokenType;
        List<Map>list=utilsClass.constructToken(ADDRESS1,tokenType,"10");
        List<Map>list2=utilsClass.constructToken(MULITADD4,tokenType2,"10",list);
        List<Map>list3=utilsClass.constructToken(MULITADD4,tokenType,"10",list);
        log.info(transferData);
        String transferInfo= multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD,list2);//不相同币种

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);


        String transferInfo2= multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD,list3);//相同币种
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);


        log.info("查询余额判断转账是否成功");
        String queryInfo= soloSign.Balance(PRIKEY1,tokenType);
        String queryInfo2= multiSign.BalanceByAddr(MULITADD4,tokenType2);
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("state"));
        assertEquals("20",JSONObject.fromObject(queryInfo).getJSONObject("data").getString("total"));
        assertEquals("10",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString("total"));

        log.info("回收Token");
        String recycleInfo = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType, "970");
        String recycleInfo2 = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType2, "990");
        String recycleInfo3 = soloSign.Recycle(PRIKEY1, tokenType, "20");
        String recycleInfo4 = multiSign.Recycle(MULITADD4, PRIKEY1, tokenType2, "10");
        String recycleInfo5 = multiSign.Recycle(MULITADD4, PRIKEY1, tokenType, "10");

        assertEquals("200",JSONObject.fromObject(recycleInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo3).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo4).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo5).getString("state"));

        log.info("查询余额判断回收成功与否");
        String queryInfo3= soloSign.Balance(PRIKEY1,tokenType);
        String queryInfo4= multiSign.BalanceByAddr(MULITADD4,tokenType2);
        String queryInfo5= multiSign.BalanceByAddr(MULITADD4,tokenType);

        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo5).getString("state"));
        assertEquals("20",JSONObject.fromObject(queryInfo3).getJSONObject("data").getString("total"));
        assertEquals("10",JSONObject.fromObject(queryInfo4).getJSONObject("data").getString("total"));
        assertEquals("10",JSONObject.fromObject(queryInfo5).getJSONObject("data").getString("total"));
    }


    /**
     *TC32归集地址向两个单签地址转账
     * 发两种币-查询归集地址-转账两个地址不同token-查询-回收-查询
     * @throws Exception
     */
    @Test
    public void TC32_transferSolo()throws  Exception{

        String transferData = "归集地址向" + "ADDRESS1" + "转账10个" + tokenType+"归集地址向" + "ADDRESS2" + "转账10个" + tokenType;
        List<Map>list=utilsClass.constructToken(ADDRESS1,tokenType,"10");
        List<Map>list2=utilsClass.constructToken(ADDRESS2,tokenType2,"10",list);
        List<Map>list3=utilsClass.constructToken(ADDRESS2,tokenType,"10",list);
        log.info(transferData);
        String transferInfo= multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD,list2);//两个地址不同币种

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);


        String transferInfo2= multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD,list3);//两个地址相同币种
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);


        log.info("查询余额判断转账是否成功");
        String queryInfo= soloSign.Balance(PRIKEY1,tokenType);
        String queryInfo2= soloSign.Balance(PRIKEY2,tokenType2);
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("state"));
        assertEquals("20",JSONObject.fromObject(queryInfo).getJSONObject("data").getString("total"));
        assertEquals("10",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString("total"));

        log.info("回收Token");
        String recycleInfo = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType, "970");
        String recycleInfo2 = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType2, "990");
        String recycleInfo3 = soloSign.Recycle(PRIKEY1, tokenType, "20");
        String recycleInfo4 = soloSign.Recycle( PRIKEY2, tokenType2, "10");
        String recycleInfo5 = soloSign.Recycle(PRIKEY2, tokenType, "10");

        assertEquals("200",JSONObject.fromObject(recycleInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo3).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo4).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo5).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType21),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);


        log.info("查询余额判断回收成功与否");
        String queryInfo3= soloSign.Balance(PRIKEY1,tokenType);
        String queryInfo4= soloSign.Balance(PRIKEY2,tokenType2);
        String queryInfo5= soloSign.Balance(PRIKEY2,tokenType);
        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo5).getString("state"));
        assertEquals("0",JSONObject.fromObject(queryInfo3).getJSONObject("data").getString("total"));
        assertEquals("0",JSONObject.fromObject(queryInfo4).getJSONObject("data").getString("total"));
        assertEquals("0",JSONObject.fromObject(queryInfo5).getJSONObject("data").getString("total"));
    }

    /**
     *TC35多签地址向单签和多签地址转账
     * 发两种币-查询归集地址-转账两个地址不同token-查询-回收-查询
     * @throws Exception
     */
    @Test
    public void TC35_MultiToSoloMulti() throws Exception {


        String transferData = "归集地址向" + "MULITADD4" + "转账1000个" + tokenType + "归集地址向" + "MULITADD4" + "转账1000个" + tokenType;
        List<Map> listInit = utilsClass.constructToken(MULITADD4, tokenType, "1000");
        List<Map> list0 = utilsClass.constructToken(MULITADD4, tokenType2, "1000", listInit);
        log.info(transferData);
        String transferInfoInit = multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD, list0);//转账给多签地址
        assertEquals("200",JSONObject.fromObject(transferInfoInit).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);


        List<Map> list = utilsClass.constructToken(ADDRESS1, tokenType, "10");
        List<Map> list2 = utilsClass.constructToken(MULITADD5, tokenType2, "10", list);
        List<Map> list3 = utilsClass.constructToken(MULITADD5, tokenType, "10", list);
        log.info(transferData);
        String transferInfo = multiSign.Transfer(PRIKEY1, transferData, MULITADD4, list2);//不相同币种

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);


        String transferInfo2 = multiSign.Transfer(PRIKEY1, transferData, MULITADD4, list3);//相同币种
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);


        log.info("查询余额判断转账是否成功");
        String queryInfo = soloSign.Balance(PRIKEY1, tokenType);
        String queryInfo2 = multiSign.BalanceByAddr(MULITADD5,tokenType2);
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("state"));
        assertEquals("20",JSONObject.fromObject(queryInfo).getJSONObject("data").getString("total"));
        assertEquals("10",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString("total"));

        log.info("锁定待回收Token: "+tokenType);
        String resp2=multiSign.freezeToken(tokenType);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType21),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        log.info("回收Token");
        String recycleInfo = multiSign.Recycle(MULITADD4, PRIKEY1, tokenType, "970");
        String recycleInfo2 = multiSign.Recycle(MULITADD4, PRIKEY1, tokenType2, "990");
        String recycleInfo3 = soloSign.Recycle(PRIKEY1, tokenType, "20");
        String recycleInfo4 = multiSign.Recycle(MULITADD5, PRIKEY1, tokenType2, "10");
        String recycleInfo5 = multiSign.Recycle(MULITADD5, PRIKEY1, tokenType, "10");

        assertEquals("200",JSONObject.fromObject(recycleInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo3).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo4).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo5).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);


        log.info("查询余额判断回收成功与否");
        String queryInfo3 = soloSign.Balance(PRIKEY1, tokenType);
        String queryInfo4 = multiSign.BalanceByAddr(MULITADD5,  tokenType2);
        String queryInfo5 = multiSign.BalanceByAddr(MULITADD5,  tokenType);
        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo5).getString("state"));
        assertEquals("0",JSONObject.fromObject(queryInfo3).getJSONObject("data").getString("total"));
        assertEquals("0",JSONObject.fromObject(queryInfo4).getJSONObject("data").getString("total"));
        assertEquals("0",JSONObject.fromObject(queryInfo5).getJSONObject("data").getString("total"));
    }


    /**
     *TC33多签地址向两个多签地址转账
     * 发两种币-查询归集地址-转账两个地址不同token-查询-回收-查询
     * @throws Exception
     */
    @Test
    public void TC33_MultiToMulti() throws Exception {


        String transferData = "归集地址向" + "MULITADD4" + "转账1000个" + tokenType + "归集地址向" + "MULITADD4" + "转账1000个" + tokenType;
        List<Map> listInit = utilsClass.constructToken(MULITADD4, tokenType, "1000");
        List<Map> list0 = utilsClass.constructToken(MULITADD4, tokenType2, "1000", listInit);
        log.info(transferData);
        String transferInfoInit = multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD, list0);//转账给多签地址
        assertEquals("200",JSONObject.fromObject(transferInfoInit).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);


        String ab = multiSign.BalanceByAddr(MULITADD4,tokenType);
        log.info("----------------------");
        List<Map> list = utilsClass.constructToken(MULITADD6, tokenType, "10");
        List<Map> list2 = utilsClass.constructToken(MULITADD5, tokenType2, "10", list);
        List<Map> list3 = utilsClass.constructToken(MULITADD5, tokenType, "10", list);
        log.info(transferData);
        String transferInfo = multiSign.Transfer(PRIKEY1, transferData, MULITADD4, list2);//不相同币种

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);


        String transferInfo2 = multiSign.Transfer(PRIKEY1, transferData, MULITADD4, list3);//相同币种
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);


        log.info("查询余额判断转账是否成功");
        String queryInfo = multiSign.BalanceByAddr(MULITADD6,tokenType);
        String queryInfo2 = multiSign.BalanceByAddr(MULITADD5,tokenType2);
        String queryInfo1 = multiSign.BalanceByAddr(MULITADD5,tokenType);
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo1).getString("state"));
        assertEquals("20",JSONObject.fromObject(queryInfo).getJSONObject("data").getString("total"));
        assertEquals("10",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString("total"));
        assertEquals("10",JSONObject.fromObject(queryInfo1).getJSONObject("data").getString("total"));


        //20190411增加锁定解锁操作步骤后进行回收
        log.info("锁定待回收Token: "+tokenType);
        String resp=multiSign.freezeToken(tokenType);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType21),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        log.info("解除锁定待回收Token: "+tokenType);
        String resp1=multiSign.recoverFrozenToken(tokenType);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType21),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);


        log.info("回收Token");
        String recycleInfo = multiSign.Recycle(MULITADD4, PRIKEY1, tokenType, "970");
        String recycleInfo2 = multiSign.Recycle(MULITADD4, PRIKEY1, tokenType2, "990");
        String recycleInfo3 = multiSign.Recycle(MULITADD6,PRIKEY4, tokenType, "20");
        String recycleInfo4 = multiSign.Recycle(MULITADD5, PRIKEY1, tokenType2, "10");
        String recycleInfo5 = multiSign.Recycle(MULITADD5, PRIKEY1, tokenType, "10");

        assertEquals("200",JSONObject.fromObject(recycleInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo3).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo4).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo5).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(recycleInfo2,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        log.info("查询余额判断回收成功与否");
        String queryInfo3 = multiSign.BalanceByAddr(MULITADD6,tokenType);
        String queryInfo4 = multiSign.BalanceByAddr(MULITADD5, tokenType2);
        String queryInfo5 = multiSign.BalanceByAddr(MULITADD5, tokenType);
        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo5).getString("state"));
        assertEquals("0",JSONObject.fromObject(queryInfo3).getJSONObject("data").getString("total"));
        assertEquals("0",JSONObject.fromObject(queryInfo4).getJSONObject("data").getString("total"));
        assertEquals("0",JSONObject.fromObject(queryInfo5).getJSONObject("data").getString("total"));

        String zero=  multiSign.QueryZero(tokenType);
        String zero2= multiSign.QueryZero(tokenType2);

        assertEquals("200",JSONObject.fromObject(zero).getString("state"));
        assertEquals("1000",JSONObject.fromObject(zero).getJSONObject("data").getJSONObject("detail").getString(tokenType));
        assertEquals("200",JSONObject.fromObject(zero2).getString("state"));
        assertEquals("1000",JSONObject.fromObject(zero2).getJSONObject("data").getJSONObject("detail").getString(tokenType2));
    }


    /**
     *TC34多签地址向两个单签地址转账
     * 发两种币-查询归集地址-转账两个地址不同token-查询-锁定token-回收-查询
     * @throws Exception
     */
    @Test
    public void TC34_MultiToSolo() throws Exception {


        String transferData = "归集地址向" + "MULITADD4" + "转账1000个" + tokenType + "归集地址向" + "MULITADD4" + "转账1000个" + tokenType;
        List<Map> listInit = utilsClass.constructToken(MULITADD4, tokenType, "1000");
        List<Map> list0 = utilsClass.constructToken(MULITADD4, tokenType2, "1000", listInit);
        log.info(transferData);
        String transferInfoInit = multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD, list0);//转账给多签地址
        assertEquals("200",JSONObject.fromObject(transferInfoInit).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);


        List<Map> list = utilsClass.constructToken(ADDRESS1, tokenType, "10");
        List<Map> list2 = utilsClass.constructToken(ADDRESS2,tokenType2, "10", list);
        List<Map> list3 = utilsClass.constructToken(ADDRESS2, tokenType, "10", list);
        log.info(transferData);
        String transferInfo = multiSign.Transfer(PRIKEY1, transferData, MULITADD4, list2);//不相同币种
        Thread.sleep(SLEEPTIME);//UTXO关系，两笔交易之间需要休眠
        String transferInfo2 = multiSign.Transfer(PRIKEY1, transferData, MULITADD4, list3);//相同币种
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);


        log.info("查询余额判断转账是否成功");
        String queryInfo = soloSign.Balance(PRIKEY1, tokenType);
        String queryInfo2 = soloSign.Balance(PRIKEY2, tokenType2);
        String queryInfo_2=soloSign.Balance(PRIKEY2,tokenType);
        assertEquals("20",JSONObject.fromObject(queryInfo).getJSONObject("data").getString("total"));
        assertEquals("10",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString("total"));
        assertEquals("10",JSONObject.fromObject(queryInfo_2).getJSONObject("data").getString("total"));

        log.info("锁定待回收Token: "+tokenType);
        String resp=multiSign.freezeToken(tokenType);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType21),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        log.info("回收Token");
        String recycleInfo = multiSign.Recycle(MULITADD4, PRIKEY1, tokenType, "970");
        String recycleInfo2 = multiSign.Recycle(MULITADD4, PRIKEY1, tokenType2, "990");
        String recycleInfo3 = soloSign.Recycle(PRIKEY1, tokenType, "20");
        String recycleInfo4 = soloSign.Recycle(PRIKEY2, tokenType2, "10");
        String recycleInfo5 = soloSign.Recycle(PRIKEY2, tokenType, "10");

        assertEquals("200",JSONObject.fromObject(recycleInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo3).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo4).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo5).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType21),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);


        log.info("查询余额判断回收成功与否");
        String queryInfo3 = soloSign.Balance(PRIKEY1, tokenType);
        String queryInfo4 = soloSign.Balance(PRIKEY2, tokenType2);
        String queryInfo5 = soloSign.Balance(PRIKEY2, tokenType);
        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo5).getString("state"));
        assertEquals("0",JSONObject.fromObject(queryInfo3).getJSONObject("data").getString("total"));
        assertEquals("0",JSONObject.fromObject(queryInfo4).getJSONObject("data").getString("total"));
        assertEquals("0",JSONObject.fromObject(queryInfo5).getJSONObject("data").getString("total"));
    }

    /**
     * 多签账户转账给自己 无法转给自己
     * @throws Exception
     */
    @Test
    public void TransferToSelf() throws Exception {
        String transferData = IMPPUTIONADD + "转给自己1000个" + tokenType;
        List<Map> listInit = utilsClass.constructToken(IMPPUTIONADD, tokenType, "1000");
        List<Map> list0 = utilsClass.constructToken(MULITADD4, tokenType2, "1000", listInit);
        log.info(transferData);
        String transferInfoInit = multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD, list0);//转账给包含自己地址的多个地址
        assertEquals(true,transferInfoInit.contains("can't transfer to self"));
        String transferInfoInit2 = multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD, listInit);//仅转账给自己
        assertEquals(true,transferInfoInit2.contains("can't transfer to self"));
    }

    //-----------------------------------------------------------------------------------------------------------

    public  String IssueToken(int length,String  amount,String ToAddr){
        String tokenType = "CX-" + UtilsClass.Random(length);
        //String amount = "1000";
        log.info(MULITADD3+ "发行" + tokenType + " token，数量为：" + amount);
        String data = "MULITADD3" + "发行" + tokenType + " token，数量为：" + amount;
        String response = multiSign.issueToken(IMPPUTIONADD,ToAddr,tokenType, amount, data);
        assertThat(response, containsString("200"));
        String Tx1 = JSONObject.fromObject(response).getJSONObject("data").getString("tx");
        log.info("第一次签名");
        String response2 = multiSign.Sign(Tx1, PRIKEY5);
        assertThat(response2, containsString("200"));
        /*String response = multiSign.issueToken(MULITADD3, tokenType, amount, data);
        assertThat(response, containsString("200"));
        String Tx1 = JSONObject.fromObject(response).getJSONObject("Data").getString("Tx");
        log.info("第一次签名");
        String response2 = multiSign.Sign(Tx1, PRIKEY1);
        String Tx2 = JSONObject.fromObject(response2).getJSONObject("Data").getString("Tx");
        log.info("第二次签名");
        String response3 = multiSign.Sign(Tx2, PRIKEY6,PWD6);
        String Tx3 = JSONObject.fromObject(response3).getJSONObject("Data").getString("Tx");
        log.info("第三次签名");
        String response4 = multiSign.Sign(Tx3, PRIKEY7,PWD7);
        assertThat(JSONObject.fromObject(response4).getJSONObject("Data").getString("IsCompleted"), containsString("true"));
        assertThat(response4, containsString("200"));*/
        return tokenType;
    }


    public  String IssueToken(int length,String  amount){
        String tokenType = "CX-" + UtilsClass.Random(length);
        //String amount = "1000";
        log.info(IMPPUTIONADD+ "发行" + tokenType + " token，数量为：" + amount);
        String data = "IMPPUTIONADD" + "发行" + tokenType + " token，数量为：" + amount;
        String response = multiSign.issueToken(IMPPUTIONADD, tokenType, amount, data);
        assertThat(response, containsString("200"));
        String Tx1 = JSONObject.fromObject(response).getJSONObject("data").getString("tx");
        log.info("第一次签名");
        String response2 = multiSign.Sign(Tx1, PRIKEY5);
        assertThat(response2, containsString("200"));
        /*String response = multiSign.issueToken(MULITADD3, tokenType, amount, data);
        assertThat(response, containsString("200"));
        String Tx1 = JSONObject.fromObject(response).getJSONObject("Data").getString("Tx");
        log.info("第一次签名");
        String response2 = multiSign.Sign(Tx1, PRIKEY1);
        String Tx2 = JSONObject.fromObject(response2).getJSONObject("Data").getString("Tx");
        log.info("第二次签名");
        String response3 = multiSign.Sign(Tx2, PRIKEY6,PWD6);
        String Tx3 = JSONObject.fromObject(response3).getJSONObject("Data").getString("Tx");
        log.info("第三次签名");
        String response4 = multiSign.Sign(Tx3, PRIKEY7,PWD7);
        assertThat(JSONObject.fromObject(response4).getJSONObject("Data").getString("IsCompleted"), containsString("true"));
        assertThat(response4, containsString("200"));*/
        return tokenType;

    }
}
