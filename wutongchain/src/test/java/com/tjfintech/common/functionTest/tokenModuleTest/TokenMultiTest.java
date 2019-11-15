package com.tjfintech.common.functionTest.tokenModuleTest;


import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.Interface.Token;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.junit.Assert.assertEquals;


@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TokenMultiTest {
    TestBuilder testBuilder= TestBuilder.getInstance();
    MultiSign multiSign =testBuilder.getMultiSign();
    SoloSign soloSign= testBuilder.getSoloSign();
    UtilsClass utilsClass=new UtilsClass();
    private static String tokenType;
    private static String tokenType2;

    private static String issueAmount1;
    private static String issueAmount2;

    private static String actualAmount1;
    private static String actualAmount2;

    Token tokenModule = testBuilder.getToken();


    @BeforeClass
    public static void init()throws Exception
    {
        SDKADD = TOKENADD;
        if(tokenAccount1.isEmpty()) {
            BeforeCondition beforeCondition = new BeforeCondition();
            beforeCondition.tokenAddIssueCollAddr();
        }
    }

    @Before
    public void beforeConfig() throws Exception {

        SDKADD = TOKENADD;
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
        tokenType = IssueToken(tokenMultiAddr1,tokenMultiAddr1,issueAmount1);
        tokenType2 = IssueToken(tokenMultiAddr1,tokenMultiAddr1,issueAmount2);
        sleepAndSaveInfo(SLEEPTIME,"issue waiting......");
        log.info("查询归集地址中两种token余额");

        String response1 = tokenModule.tokenGetBalance(tokenMultiAddr1, tokenType);
        String response2 = tokenModule.tokenGetBalance(tokenMultiAddr1, tokenType2);
        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
        assertEquals(actualAmount1,JSONObject.fromObject(response1).getJSONObject("data").getString(tokenType));
        assertEquals("200",JSONObject.fromObject(response2).getString("state"));
        assertEquals(actualAmount2,JSONObject.fromObject(response2).getJSONObject("data").getString(tokenType2));
    }

    /**
     *  测试最大发行量
     *
     */
    @Test
    public void TC001_TestMaxValue()throws Exception {

        if (UtilsClass.PRECISION == 10) {
            actualAmount1 = "1844674407";
        }else {
            actualAmount1 = "18446744073709";
        }
        tokenType = IssueToken(tokenMultiAddr1,tokenMultiAddr1,actualAmount1);
        sleepAndSaveInfo(SLEEPTIME,"issue waiting......");
        log.info("查询归集地址中token余额");
        String response1 = tokenModule.tokenGetBalance(tokenMultiAddr1,tokenType);
        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
        assertEquals(actualAmount1,JSONObject.fromObject(response1).getJSONObject("data").getString(tokenType));

    }




    /**
     * 多签发行检查发行地址注册、未注册时,归集地址注册、未注册时的发行结果
     * 发行给其他账户
     * @throws Exception
     */
    @Test
    public void TC1280_checkMultiIssueAddr()throws Exception {
        //先前已经注册发行及归集地址tokenMultiAddr1及tokenMultiAddr2,确认发行无异常
        tokenType = IssueToken(tokenMultiAddr3, tokenMultiAddr3,"1000");
        sleepAndSaveInfo(SLEEPTIME,"issue waiting......");
        String response1 = tokenModule.tokenGetBalance(tokenMultiAddr3,tokenType);
        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
        assertEquals("1000",JSONObject.fromObject(response1).getJSONObject("data").getString(tokenType));

        //删除发行地址，保留归集地址
        String response2 = tokenModule.tokenDelMintAddr(tokenMultiAddr3);
        sleepAndSaveInfo(SLEEPTIME,"issue waiting......");
        tokenType = IssueToken(tokenMultiAddr3, tokenMultiAddr3,"1000");
        sleepAndSaveInfo(SLEEPTIME,"issue waiting......");
        String response3 = tokenModule.tokenGetBalance(tokenMultiAddr3,tokenType);
        assertEquals("200",JSONObject.fromObject(response3).getString("state"));
        assertEquals(false,response3.contains(tokenType));

        //删除发行地址和归集地址
        String response41=tokenModule.tokenDelCollAddr(tokenMultiAddr3);
        assertEquals("200",JSONObject.fromObject(response41).getString("state"));
        sleepAndSaveInfo(SLEEPTIME,"issue waiting......");
        tokenType = IssueToken(tokenMultiAddr3, tokenMultiAddr3,"1000");
        sleepAndSaveInfo(SLEEPTIME,"issue waiting......");
        String response42 = tokenModule.tokenGetBalance(tokenMultiAddr3,tokenType);
        assertEquals("200",JSONObject.fromObject(response42).getString("state"));
        assertEquals(false,response42.contains(tokenType));

        //重新添加发行地址，归集地址已删除
        String response5=tokenModule.tokenAddMintAddr(tokenMultiAddr3);
        assertEquals("200",JSONObject.fromObject(response5).getString("state"));
        sleepAndSaveInfo(SLEEPTIME,"issue waiting......");
        tokenType = IssueToken(tokenMultiAddr3, tokenMultiAddr3,"1000");
        sleepAndSaveInfo(SLEEPTIME,"issue waiting......");
        String response53 = tokenModule.tokenGetBalance(tokenMultiAddr3,tokenType);
        assertEquals("200",JSONObject.fromObject(response53).getString("state"));
        assertEquals(false,response53.contains(tokenType));

        //重新添加发行地址和归集地址
        String response6=tokenModule.tokenAddCollAddr(tokenMultiAddr3);
        assertEquals("200",JSONObject.fromObject(response6).getString("state"));
        sleepAndSaveInfo(SLEEPTIME,"issue waiting......");
        tokenType = IssueToken(tokenMultiAddr3, tokenMultiAddr3,"1000");
        sleepAndSaveInfo(SLEEPTIME,"issue waiting......");
        String response62 = tokenModule.tokenGetBalance(tokenMultiAddr3,tokenType);
        assertEquals("200",JSONObject.fromObject(response62).getString("state"));
        assertEquals("1000",JSONObject.fromObject(response62).getJSONObject("data").getString(tokenType));
    }


    /**
     * 多签发行token。接收地址不为本身。指定其他多签地址
     * @throws Exception
     */
     @Test
     public void TC994_issueToOther()throws Exception {
         tokenType = IssueToken(tokenMultiAddr1, tokenMultiAddr2,"1000");
         tokenType2 = IssueToken(tokenMultiAddr1, tokenMultiAddr2,"1000");
         sleepAndSaveInfo(SLEEPTIME,"issue waiting......");
         String response1 = tokenModule.tokenGetBalance(tokenMultiAddr2,tokenType);
         String response2 = tokenModule.tokenGetBalance(tokenMultiAddr2,tokenType2);
         assertEquals("200",JSONObject.fromObject(response1).getString("state"));
         assertEquals("1000",JSONObject.fromObject(response1).getJSONObject("data").getString(tokenType));
         assertEquals("200",JSONObject.fromObject(response2).getString("state"));
         assertEquals("1000",JSONObject.fromObject(response2).getJSONObject("data").getString(tokenType2));
     }




    /**
     * Tc03多签正常流程：转账 回收
     *
     */
    @Test
    public void TC03_multiProgress() throws Exception {
        // tokenMultiAddr1 + "向" + tokenMultiAddr3 + "转账10个" + tokenType;
        String transferInfo= TransferToken(tokenMultiAddr1,tokenMultiAddr3,tokenType,"10");
        sleepAndSaveInfo(SLEEPTIME,"issue waiting......");
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));

        String amount1;

        if (UtilsClass.PRECISION == 10) {
            amount1 = "990.1234567891";
        }else {
            amount1 = "990.123456";
        }

        log.info("查询"+ tokenMultiAddr1 +"和" + tokenMultiAddr3 + "余额，判断转账是否成功");
        String queryInfo = tokenModule.tokenGetBalance(tokenMultiAddr1, tokenType);
        String queryInfo2 = tokenModule.tokenGetBalance(tokenMultiAddr3, tokenType);
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
        assertEquals(amount1,JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType));
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("state"));
        assertEquals("10",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString(tokenType));

        //回收
        log.info(tokenMultiAddr1 +"和" + tokenMultiAddr3 + "回收" + tokenType);
        String recycleInfo = DestoryToken(tokenMultiAddr1,tokenType, amount1);
        String recycleInfo2 = DestoryToken(tokenMultiAddr3,tokenType, "10");
        assertEquals("200",JSONObject.fromObject(recycleInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("state"));
        sleepAndSaveInfo(SLEEPTIME,"issue waiting......");

        log.info("查询回收后账户余额是否为0");
        String queryInfo3 = tokenModule.tokenGetBalance(tokenMultiAddr1,tokenType);
        String queryInfo4 = tokenModule.tokenGetBalance(tokenMultiAddr3,tokenType);
        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("state"));
        assertEquals(false,queryInfo3.contains(tokenType));
        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("state"));
        assertEquals(false,queryInfo4.contains(tokenType));
    }


    /**
     * 精度测试
     *
     */
    @Test
    public void TC03_PrecisionTest() throws Exception {

        //tokenMultiAddr1 + "向" + tokenMultiAddr3 + "转账"+ issueAmount1 + " * " + tokenType;
        String transferInfo= TransferToken(tokenMultiAddr1,tokenMultiAddr3,tokenType,issueAmount1);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
        sleepAndSaveInfo(SLEEPTIME,"transfer waiting......");

        String amount1;

        if (UtilsClass.PRECISION == 10) {
            amount1 = "1000.1234567891";
        }else {
            amount1 = "1000.123456";
        }

        log.info("查询归集地址跟MULITADD4余额，判断转账是否成功");
        String queryInfo = tokenModule.tokenGetBalance(tokenMultiAddr1, tokenType);
        String queryInfo2 = tokenModule.tokenGetBalance(tokenMultiAddr3, tokenType);
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
        assertEquals(false,queryInfo.contains(tokenType));
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("state"));
        assertEquals(amount1,JSONObject.fromObject(queryInfo2).getJSONObject("data").getString(tokenType));

        //tokenMultiAddr1 +"和" + tokenMultiAddr3 + "回收" + tokenType);
        String recycleInfo2 = DestoryToken(tokenMultiAddr3, tokenType, issueAmount1);
        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("state"));
        sleepAndSaveInfo(SLEEPTIME,"destory waiting......");

        log.info("查询回收后账户余额是否为0");
        String queryInfo3 = tokenModule.tokenGetBalance(tokenMultiAddr1, tokenType);
        String queryInfo4 = tokenModule.tokenGetBalance(tokenMultiAddr3, tokenType);
        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("state"));
        assertEquals(false,queryInfo3.contains(tokenType));
        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("state"));
        assertEquals(false,queryInfo4.contains(tokenType));

        String queryInfo5 = tokenModule.tokenGetDestroyBalance(tokenType);
        assertEquals(amount1,JSONObject.fromObject(queryInfo5).getJSONObject("data").getString(tokenType));

    }



    /**
     * Tc024锁定后转账:当前token模块暂未提供锁定功能
     *
     */
//    @Test
//    public void TC024_TransferAfterFrozen() throws Exception {
//
//        //20190411增加锁定步骤后进行转账
//        log.info("锁定待转账Token: "+tokenType);
//        String resp=multiSign.freezeToken(PRIKEY1,tokenType);
//        sleepAndSaveInfo(SLEEPTIME,"issue waiting......");
//
//
//        log.info("查询归集地址中两种token余额");
//        String response1 = tokenModule.tokenGetBalance(tokenMultiAddr1, tokenType);
//        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
//        assertEquals(actualAmount1,JSONObject.fromObject(response1).getJSONObject("data").getString(tokenType));
//
//        String transferData = "归集地址向MULITADD4转账10个" + tokenType;
//        List<Map>list=utilsClass.constructToken(MULITADD4,tokenType,"10");
//        log.info(transferData);
//
//        String transferInfo= TransferToken(PRIKEY4, IMPPUTIONADD,list);//相同币种
//        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
//        sleepAndSaveInfo(SLEEPTIME,"issue waiting......");
//
//        log.info("查询余额判断转账是否成功");
//        String queryInfo= tokenModule.tokenGetBalance(MULITADD4,PRIKEY1,tokenType);
//        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
//        assertEquals(false,queryInfo.contains(tokenType));
//
//        log.info("解除锁定待转账Token: "+tokenType);
//        String resp1=multiSign.recoverFrozenToken(PRIKEY1,tokenType);
//        sleepAndSaveInfo(SLEEPTIME,"issue waiting......");
//
//        log.info("查询归集地址中两种token余额");
//        response1 = tokenModule.tokenGetBalance(tokenMultiAddr1, tokenType);
//        String response2 = tokenModule.tokenGetBalance(tokenMultiAddr1, tokenType2);
//        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
//        assertEquals(actualAmount1,JSONObject.fromObject(response1).getJSONObject("data").getString(tokenType));
//        assertEquals("200",JSONObject.fromObject(response2).getString("state"));
//        assertEquals(actualAmount2,JSONObject.fromObject(response2).getJSONObject("data").getString(tokenType));
//
//        transferData = "归集地址向" + "MULITADD4" + "转账10个" + tokenType+"归集地址向" + "MULITADD4" + "转账10个" + tokenType;
//        list=utilsClass.constructToken(MULITADD4,tokenType,"10");
//        List<Map>list2=utilsClass.constructToken(MULITADD5,tokenType2,"10",list);
//        List<Map>list3=utilsClass.constructToken(MULITADD5,tokenType,"10",list);
//        log.info(transferData);
//        transferInfo= TransferToken(PRIKEY4, IMPPUTIONADD,list2);//不同币种
//        sleepAndSaveInfo(SLEEPTIME,"issue waiting......"); //UTXO关系，两笔交易之间需要休眠
//        String transferInfo2= TransferToken(PRIKEY4, IMPPUTIONADD,list3);//相同币种
//        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
//        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("state"));
//        sleepAndSaveInfo(SLEEPTIME,"issue waiting......");
//
//        log.info("查询余额判断转账是否成功");
//        queryInfo= tokenModule.tokenGetBalance(MULITADD4,PRIKEY1,tokenType);
//        String queryInfo2= tokenModule.tokenGetBalance(MULITADD5,PRIKEY1,tokenType2);
//        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
//        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("state"));
//        assertEquals("20",JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType));
//        assertEquals("10",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString(tokenType));
//
//        tokenModule.tokenGetBalance(IMPPUTIONADD,PRIKEY4, tokenType);
//        tokenModule.tokenGetBalance(tokenMultiAddr1, tokenType2);
//
//        String amount1, amount2;
//
//        if (UtilsClass.PRECISION == 10) {
//            amount1 = "970.1234567891";
//            amount2 = "990.123456.8765432123";
//        }else {
//            amount1 = "970.123456";
//            amount2 = "990.123456.876543";
//        }
//
//
//        log.info("回收Token");
//        String recycleInfo = DestoryToken(tokenMultiAddr1, tokenType, amount1);
//        String recycleInfo2 = DestoryToken(tokenMultiAddr1, tokenType2, amount2);
//        String recycleInfo3 = DestoryToken(MULITADD4, PRIKEY1, tokenType, "20");
//        String recycleInfo4 = DestoryToken(MULITADD5, PRIKEY1, tokenType2, "10");
//        String recycleInfo5 = DestoryToken(MULITADD5, PRIKEY1, tokenType, "10");
//        sleepAndSaveInfo(SLEEPTIME,"issue waiting......");
//        assertEquals("200",JSONObject.fromObject(recycleInfo).getString("state"));
//        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("state"));
//        assertEquals("200",JSONObject.fromObject(recycleInfo3).getString("state"));
//        assertEquals("200",JSONObject.fromObject(recycleInfo4).getString("state"));
//        assertEquals("200",JSONObject.fromObject(recycleInfo5).getString("state"));
//
//
//        log.info("查询余额判断回收成功与否");
//        String queryInfo3= tokenModule.tokenGetBalance(MULITADD4,PRIKEY1,tokenType);
//        String queryInfo4= tokenModule.tokenGetBalance(MULITADD5,PRIKEY1,tokenType2);
//        String queryInfo5= tokenModule.tokenGetBalance(MULITADD5,PRIKEY1,tokenType);
//
//        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("state"));
//        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("state"));
//        assertEquals("200",JSONObject.fromObject(queryInfo5).getString("state"));
//
//        assertEquals("0",JSONObject.fromObject(queryInfo3).getJSONObject("data").getString(tokenType));
//        assertEquals("0",JSONObject.fromObject(queryInfo4).getJSONObject("data").getString(tokenType));
//        assertEquals("0",JSONObject.fromObject(queryInfo5).getJSONObject("data").getString(tokenType));
//
//    }

    /**
     *TC19归集地址向分别两个多签地址转账
     * 发两种币-查询归集地址-转账两个地址不同token-查询-回收-查询
     * @throws Exception
     */
     @Test
     public void TC19_transferMulti()throws  Exception{

         log.info("查询归集地址中两种token余额");
         String response1 = tokenModule.tokenGetBalance(tokenMultiAddr1, tokenType);
         String response2 = tokenModule.tokenGetBalance(tokenMultiAddr1, tokenType2);
         assertEquals("200",JSONObject.fromObject(response1).getString("state"));
         assertEquals("200",JSONObject.fromObject(response2).getString("state"));
         assertEquals(actualAmount1,JSONObject.fromObject(response1).getJSONObject("data").getString(tokenType));
         assertEquals(actualAmount2,JSONObject.fromObject(response2).getJSONObject("data").getString(tokenType2));

         //tokenMultiAddr1 +"向" + tokenMultiAddr2 + "转账10个" + tokenType;
         String transferInfo= TransferToken(tokenMultiAddr1, tokenMultiAddr2,tokenType,"10");
         assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));

         sleepAndSaveInfo(SLEEPTIME,"transfer waiting......"); //UTXO关系，两笔交易之间需要休眠

         //tokenMultiAddr1 +"向" + tokenMultiAddr3 + "转账10个" + tokenType2;
         String transferInfo2= TransferToken(tokenMultiAddr1, tokenMultiAddr3,tokenType2,"10");
         assertEquals("200",JSONObject.fromObject(transferInfo2).getString("state"));

         sleepAndSaveInfo(SLEEPTIME,"transfer waiting......");

         //tokenMultiAddr1 +"向" + tokenMultiAddr3 + "转账10个" + tokenType;
         String transferInfo3= TransferToken(tokenMultiAddr1, tokenMultiAddr3,tokenType,"10");
         assertEquals("200",JSONObject.fromObject(transferInfo3).getString("state"));

         sleepAndSaveInfo(SLEEPTIME*2,"transfer waiting......");

         log.info("查询余额判断转账是否成功");
         String queryInfo= tokenModule.tokenGetBalance(tokenMultiAddr2,tokenType);
         String queryInfo2= tokenModule.tokenGetBalance(tokenMultiAddr3,"");
         assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
         assertEquals("200",JSONObject.fromObject(queryInfo2).getString("state"));
         assertEquals("10",JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType));
         assertEquals("10",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString(tokenType));
         assertEquals("10",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString(tokenType2));

         tokenModule.tokenGetBalance(tokenMultiAddr1, tokenType);
         tokenModule.tokenGetBalance(tokenMultiAddr1, tokenType2);

         String amount1, amount2;

         if (UtilsClass.PRECISION == 10) {
             amount1 = "980.1234567891";
             amount2 = "990.8765432123";
         }else {
             amount1 = "980.123456";
             amount2 = "990.876543";
         }

         log.info("回收Token");
         String recycleInfo = DestoryToken(tokenMultiAddr1, tokenType, amount1);
         String recycleInfo2 = DestoryToken(tokenMultiAddr1, tokenType2, amount2);
         String recycleInfo3 = DestoryToken(tokenMultiAddr2, tokenType, "10");
         String recycleInfo4 = DestoryToken(tokenMultiAddr3, tokenType, "10");
         String recycleInfo5 = DestoryToken(tokenMultiAddr3, tokenType2, "10");
         sleepAndSaveInfo(SLEEPTIME,"destory waiting......");

         assertEquals("200",JSONObject.fromObject(recycleInfo).getString("state"));
         assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("state"));
         assertEquals("200",JSONObject.fromObject(recycleInfo3).getString("state"));
         assertEquals("200",JSONObject.fromObject(recycleInfo4).getString("state"));
         assertEquals("200",JSONObject.fromObject(recycleInfo5).getString("state"));

         log.info("查询余额判断回收成功与否");
         String queryInfo3= tokenModule.tokenGetBalance(tokenMultiAddr2,tokenType);
         String queryInfo4= tokenModule.tokenGetBalance(tokenMultiAddr3,tokenType2);
         String queryInfo5= tokenModule.tokenGetBalance(tokenMultiAddr3,tokenType);
         assertEquals("200",JSONObject.fromObject(queryInfo3).getString("state"));
         assertEquals("200",JSONObject.fromObject(queryInfo4).getString("state"));
         assertEquals("200",JSONObject.fromObject(queryInfo5).getString("state"));
         assertEquals(false,queryInfo3.contains(tokenType));
         assertEquals(false,queryInfo4.contains(tokenType2));
         assertEquals(false,queryInfo5.contains(tokenType));
     }


    /**
     *TC31归集地址向单签和多签地址转账
     * 发两种币-查询归集地址-转账两个地址不同token-查询-回收-查询
     * @throws Exception
     */
    @Test
    public void TC31_transferSoloMulti()throws  Exception{

        //tokenMultiAddr1 +"向" + tokenAccount1 tokenMultiAddr1 转账tokenType tokenType2
        String transferInfo= TransferToken(tokenMultiAddr1,tokenAccount1,tokenType,"10");
        sleepAndSaveInfo(SLEEPTIME,"transfer waiting......");//UTXO关系，两笔交易之间需要休眠
        String transferInfo2= TransferToken(tokenMultiAddr1,tokenMultiAddr2,tokenType2,"10");
        sleepAndSaveInfo(SLEEPTIME,"transfer waiting......");//UTXO关系，两笔交易之间需要休眠
        String transferInfo3= TransferToken(tokenMultiAddr1,tokenMultiAddr2,tokenType,"10");
        sleepAndSaveInfo(SLEEPTIME,"transfer waiting......");

        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("state"));
        assertEquals("200",JSONObject.fromObject(transferInfo3).getString("state"));

        log.info("查询余额判断转账是否成功");
        String queryInfo= tokenModule.tokenGetBalance(tokenAccount1,"");
        String queryInfo2= tokenModule.tokenGetBalance(tokenMultiAddr2,"");
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("state"));
        assertEquals("10",JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType));
        assertEquals("10",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString(tokenType2));
        assertEquals("10",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString(tokenType));

        log.info("回收Token");
        String recycleInfo = DestoryToken(tokenMultiAddr1, tokenType, "980.123456");
        String recycleInfo2 = DestoryToken(tokenMultiAddr1, tokenType2, "990.876543");
        String recycleInfo3 = DestoryToken(tokenMultiAddr2, tokenType,"10");
        String recycleInfo4 = DestoryToken(tokenMultiAddr2, tokenType2,"10");
        String recycleInfo5 = DestoryToken(tokenAccount1, tokenType,"10");

        assertEquals("200",JSONObject.fromObject(recycleInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo3).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo4).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo5).getString("state"));

        log.info("查询余额判断回收成功与否");
        String queryInfo3= tokenModule.tokenGetBalance(tokenAccount1,"");
        String queryInfo4= tokenModule.tokenGetBalance(tokenMultiAddr1,"");
        String queryInfo5= tokenModule.tokenGetBalance(tokenMultiAddr2,"");

        //此时交易未上链
//        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("state"));
//        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("state"));
//        assertEquals("200",JSONObject.fromObject(queryInfo5).getString("state"));
//        assertEquals("10",JSONObject.fromObject(queryInfo3).getJSONObject("data").getString(tokenType));
//        assertEquals("980.123456",JSONObject.fromObject(queryInfo4).getJSONObject("data").getString(tokenType));
//        assertEquals("970.876543",JSONObject.fromObject(queryInfo4).getJSONObject("data").getString(tokenType2));
//        assertEquals("10",JSONObject.fromObject(queryInfo5).getJSONObject("data").getString(tokenType));
//        assertEquals("10",JSONObject.fromObject(queryInfo5).getJSONObject("data").getString(tokenType2));

        sleepAndSaveInfo(SLEEPTIME*2,"transfer waiting......");


        log.info("查询余额判断回收成功与否");
        String queryInfo6= tokenModule.tokenGetBalance(tokenAccount1,"");
        String queryInfo7= tokenModule.tokenGetBalance(tokenMultiAddr1,tokenType);
        String queryInfo9= tokenModule.tokenGetBalance(tokenMultiAddr1,tokenType2);
        String queryInfo8= tokenModule.tokenGetBalance(tokenMultiAddr2,"");

        //此时交易上链
        assertEquals("200",JSONObject.fromObject(queryInfo6).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo7).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo8).getString("state"));

        assertEquals(false,queryInfo6.contains(tokenType));
        assertEquals(false,queryInfo7.contains(tokenType));
        assertEquals(false,queryInfo9.contains(tokenType2));
        assertEquals(false,queryInfo8.contains(tokenType));
        assertEquals(false,queryInfo8.contains(tokenType2));
    }


    /**
     *TC32归集地址向两个单签地址转账
     * 发两种币-查询归集地址-转账两个地址不同token-查询-回收-查询
     * @throws Exception
     */
    @Test
    public void TC32_transferSolo()throws  Exception{
        // "归集地址向" + tokenAccount1 + "转账10个" + tokenType;
        String transferInfo= TransferToken(tokenMultiAddr1, tokenAccount1,tokenType,"10");//两个地址不同币种
        sleepAndSaveInfo(SLEEPTIME,"transfer waiting......");//UTXO关系，两笔交易之间需要休眠
        String transferInfo2= TransferToken(tokenMultiAddr1, tokenAccount2,tokenType2,"10");//两个地址相同币种
        sleepAndSaveInfo(SLEEPTIME,"transfer waiting......");//UTXO关系，两笔交易之间需要休眠
        String transferInfo3= TransferToken(tokenMultiAddr1, tokenAccount2,tokenType,"10");//两个地址相同币种
        sleepAndSaveInfo(SLEEPTIME,"transfer waiting......");//UTXO关系，两笔交易之间需要休眠

        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("state"));
        assertEquals("200",JSONObject.fromObject(transferInfo3).getString("state"));


        log.info("查询余额判断转账是否成功");
        String queryInfo= tokenModule.tokenGetBalance(tokenAccount1,tokenType);
        String queryInfo2= tokenModule.tokenGetBalance(tokenAccount2,"");
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("state"));
        assertEquals("10",JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType));
        assertEquals("10",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString(tokenType));
        assertEquals("10",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString(tokenType2));

        log.info("回收Token");
        String recycleInfo = DestoryToken(tokenMultiAddr1, tokenType, "980.123456");
        String recycleInfo2 = DestoryToken(tokenMultiAddr1, tokenType2, "990.876543");
        String recycleInfo3 = DestoryToken(tokenAccount1, tokenType, "10");
        String recycleInfo4 = DestoryToken( tokenAccount2, tokenType2, "10");
        String recycleInfo5 = DestoryToken(tokenAccount2, tokenType, "10");

        assertEquals("200",JSONObject.fromObject(recycleInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo3).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo4).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo5).getString("state"));

        sleepAndSaveInfo(SLEEPTIME*2,"destory waiting......");

        log.info("查询余额判断回收成功与否");
        String queryInfo3= tokenModule.tokenGetBalance(tokenAccount1,tokenType);
        String queryInfo4= tokenModule.tokenGetBalance(tokenAccount2,"");
        String queryInfo5= tokenModule.tokenGetBalance(tokenMultiAddr1,"");
        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo5).getString("state"));

        assertEquals(false,queryInfo3.contains(tokenType));
        assertEquals(false,queryInfo4.contains(tokenType));
        assertEquals(false,queryInfo4.contains(tokenType2));
        assertEquals(false,queryInfo5.contains(tokenType));
        assertEquals(false,queryInfo5.contains(tokenType2));
    }

//    /**
//     * token模块token向非token模块地址转账
//     * 非token模块再向token模块转账
//     * @throws Exception
//     */
//    @Test
//    public void TC35_MultiToSoloMulti() throws Exception {
//
//        String transferData = tokenMultiAddr1 + "归集地址向" + MULITADD4 + "转账10个" + tokenType;
//        log.info(transferData);
//        String transferInfoInit = TransferToken(tokenMultiAddr1,MULITADD4, tokenType, "10");//转账给多签地址
//        assertEquals("200",JSONObject.fromObject(transferInfoInit).getString("state"));
//        sleepAndSaveInfo(SLEEPTIME,"on chain waiting......");
//
//        transferData = tokenMultiAddr1 + "归集地址向" + ADDRESS1 + "转账10个" + tokenType2;
//        String transferInfo = TransferToken(tokenMultiAddr1,ADDRESS1, tokenType2, "10");//不相同币种
//
//        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
//        assertEquals("200",JSONObject.fromObject(transferInfoInit).getString("state"));
//        sleepAndSaveInfo(SLEEPTIME,"on chain waiting......");//UTXO关系，两笔交易之间需要休眠
//
//        log.info("查询余额判断转账是否成功");
//        String queryInfo = tokenModule.tokenGetBalance(tokenMultiAddr1, "");
//        SDKADD = rSDKADD;
//        String queryInfo2 = multiSign.Balance(MULITADD4, PRIKEY1, tokenType);
//        String queryInfo3 = soloSign.Balance(PRIKEY1, tokenType2);
//
//        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
//        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("State"));
//        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("State"));
//
//        assertEquals("990.123456",JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType));
//        assertEquals("990.876543",JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType2));
//        assertEquals("0",JSONObject.fromObject(queryInfo2).getJSONObject("Data").getString("Total"));
//        assertEquals("0",JSONObject.fromObject(queryInfo3).getJSONObject("Data").getString("Total"));
//
//    }



    /**
     * 多签账户转账给自己 无法转给自己
     * @throws Exception
     */
    @Test
    public void TransferToSelf() throws Exception {

        String transferData = "归集地址向自己" + "转账10个" + tokenType;
        String transferInfo= TransferToken(tokenMultiAddr1, tokenMultiAddr1,tokenType,"10");//两个地址不同币种
        assertEquals(true,transferInfo.contains("can't transfer it to yourself"));
    }

    //-----------------------------------------------------------------------------------------------------------

    public  String IssueToken(String issueAddr,String collAddr,String amount){
        String issueToken = "tokenSo-"+ UtilsClass.Random(8);
        String comments = issueAddr + "向" + collAddr + " 发行token：" + issueToken + " 数量：" + amount;
        log.info(comments);
        tokenModule.tokenIssue(issueAddr,collAddr,issueToken,amount,comments);
        return issueToken;
    }

    public  String TransferToken(String from,String to, String tokenType,String amount){
        String comments = from + "向" + to + " 转账token：" + tokenType + " 数量：" + amount;
        log.info(comments);
        return tokenModule.tokenTransfer(from,to,tokenType,amount,comments);
    }

    public  String DestoryToken(String addr,String tokenType,String amount){
        String comments = addr + "销毁token：" + tokenType + " 数量：" + amount;
        log.info(comments);
        return tokenModule.tokenDestory(addr,tokenType,amount,comments);
    }
}
