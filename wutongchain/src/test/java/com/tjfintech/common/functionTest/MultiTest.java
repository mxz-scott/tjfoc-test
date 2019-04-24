package com.tjfintech.common.functionTest;


import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.security.cert.CertPath;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.functionTest.StoreTest.SLEEPTIME;
import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.*;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MultiTest {
    TestBuilder testBuilder= TestBuilder.getInstance();
    MultiSign multiSign =testBuilder.getMultiSign();
    UtilsClass utilsClass=new UtilsClass();
    private static String tokenType;
    private static String tokenType2;

    //@Test
    @Before
    public void beforeConfig() throws Exception {
        if(certPath!=""&& bReg==false) {
            BeforeCondition bf = new BeforeCondition();
            bf.updatePubPriKey();
            bf.collAddressTest();

            bReg=true;
        }

        log.info("发行两种token1000个");
        //两次发行之前不可以有sleep时间
        tokenType = IssueToken(5, "1000");
        tokenType2 = IssueToken(6, "1000");
        Thread.sleep(SLEEPTIME);
        log.info("查询归集地址中两种token余额");
        String response1 = multiSign.Balance(IMPPUTIONADD,PRIKEY4, tokenType);
        String response2 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType2);
        assertEquals("200",JSONObject.fromObject(response1).getString("State"));
        assertEquals("1000",JSONObject.fromObject(response1).getJSONObject("Data").getString("Total"));
        assertEquals("200",JSONObject.fromObject(response2).getString("State"));
        assertEquals("1000",JSONObject.fromObject(response2).getJSONObject("Data").getString("Total"));
        Thread.sleep(SLEEPTIME);

    }

    /**
     * 多签发行检查发行地址注册、未注册时的发行结果
     * @throws Exception
     */
    @Test
    public void    TC1280_checkMultiIssueAddr()throws Exception {
        //先前已经注册发行及归集地址IMPPUTIONADD,确认发行无异常
        tokenType = IssueToken(5, "1000",MULITADD3);
        Thread.sleep(SLEEPTIME);
        String response1 = multiSign.Balance(MULITADD3,PRIKEY1, tokenType);
        assertEquals("200",JSONObject.fromObject(response1).getString("State"));
        assertEquals("1000",JSONObject.fromObject(response1).getJSONObject("Data").getString("Total"));

        //删除发行地址，保留归集地址
        String response2=multiSign.delissueaddress(PRIKEY1,IMPPUTIONADD);
        Thread.sleep(SLEEPTIME);
        tokenType = IssueToken(6, "1000",MULITADD3);
        Thread.sleep(SLEEPTIME);
        String response3 = multiSign.Balance(MULITADD3,PRIKEY1, tokenType);
        assertEquals("200",JSONObject.fromObject(response3).getString("State"));
        assertEquals("0",JSONObject.fromObject(response3).getJSONObject("Data").getString("Total"));

        //删除发行地址和归集地址
        String response41=multiSign.delCollAddress(PRIKEY1,IMPPUTIONADD);
        assertEquals("200",JSONObject.fromObject(response41).getString("State"));
        Thread.sleep(SLEEPTIME);
        tokenType = IssueToken(7, "1000",MULITADD3);
        Thread.sleep(SLEEPTIME);
        String response42 = multiSign.Balance(MULITADD3,PRIKEY1, tokenType);
        assertEquals("200",JSONObject.fromObject(response42).getString("State"));
        assertEquals("0",JSONObject.fromObject(response42).getJSONObject("Data").getString("Total"));

        //重新添加发行地址，归集地址已删除
        String response5=multiSign.addissueaddress(PRIKEY1,IMPPUTIONADD);
        assertEquals("200",JSONObject.fromObject(response5).getString("State"));
        Thread.sleep(SLEEPTIME);
        tokenType = IssueToken(7, "1000",MULITADD3);
        Thread.sleep(SLEEPTIME);
        String response53 = multiSign.Balance(MULITADD3,PRIKEY1, tokenType);
        assertEquals("200",JSONObject.fromObject(response53).getString("State"));
        assertEquals("1000",JSONObject.fromObject(response53).getJSONObject("Data").getString("Total"));

        //重新添加发行地址和归集地址
        String response6=multiSign.collAddress(PRIKEY1,IMPPUTIONADD);
        assertEquals("200",JSONObject.fromObject(response6).getString("State"));
        Thread.sleep(SLEEPTIME);
        tokenType = IssueToken(7, "1000",MULITADD3);
        Thread.sleep(SLEEPTIME);
        String response62 = multiSign.Balance(MULITADD3,PRIKEY1, tokenType);
        assertEquals("200",JSONObject.fromObject(response62).getString("State"));
        assertEquals("1000",JSONObject.fromObject(response62).getJSONObject("Data").getString("Total"));
    }


    /**
     * 多签发行token。接收地址不为本身。指定其他多签地址
     * @throws Exception
     */
     @Test
     public void TC994_issueToOther()throws Exception {
         tokenType = IssueToken(5, "1000",MULITADD3);
         tokenType2 = IssueToken(5, "1000",MULITADD4);
         Thread.sleep(SLEEPTIME);
         String response1 = multiSign.Balance(MULITADD3,PRIKEY1, tokenType);
         String response2 = multiSign.Balance(MULITADD4,PRIKEY1, tokenType2);
         assertEquals("200",JSONObject.fromObject(response1).getString("State"));
         assertEquals("1000",JSONObject.fromObject(response1).getJSONObject("Data").getString("Total"));
         assertEquals("200",JSONObject.fromObject(response2).getString("State"));
         assertEquals("1000",JSONObject.fromObject(response2).getJSONObject("Data").getString("Total"));
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
        assertThat(transferInfo, containsString("200"));

        log.info("查询归集地址跟MULITADD4余额，判断转账是否成功");
        String queryInfo = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        String queryInfo2 = multiSign.Balance(MULITADD4, PRIKEY1, tokenType);
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("State"));
        assertEquals("990",JSONObject.fromObject(queryInfo).getJSONObject("Data").getString("Total"));
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("State"));
        assertEquals("10",JSONObject.fromObject(queryInfo2).getJSONObject("Data").getString("Total"));

        log.info("回收归集地址跟MULITADD4的新发token");
        String recycleInfo = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType, "990");
        String recycleInfo2 = multiSign.Recycle(MULITADD4, PRIKEY1, tokenType, "10");
        assertEquals("200",JSONObject.fromObject(recycleInfo).getString("State"));
        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("State"));
        Thread.sleep(SLEEPTIME);

        log.info("查询回收后账户余额是否为0");
        String queryInfo3 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        String queryInfo4 = multiSign.Balance(MULITADD4, PRIKEY1, tokenType);
        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("State"));
        assertEquals("0",JSONObject.fromObject(queryInfo3).getJSONObject("Data").getString("Total"));
        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("State"));
        assertEquals("0",JSONObject.fromObject(queryInfo4).getJSONObject("Data").getString("Total"));


    }

    /**
     * Tc024锁定后转账:
     *
     */
    @Test
    public void TC024_TransferAfterFrozen() throws Exception {

        //20190411增加锁定步骤后进行转账
        log.info("锁定待转账Token: "+tokenType);
        String resp=multiSign.freezeToken(PRIKEY1,tokenType);
        Thread.sleep(SLEEPTIME);


        log.info("查询归集地址中两种token余额");
        String response1 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        assertEquals("200",JSONObject.fromObject(response1).getString("State"));
        assertEquals("1000",JSONObject.fromObject(response1).getJSONObject("Data").getString("Total"));

        String transferData = "归集地址向MULITADD4转账10个" + tokenType;
        List<Map>list=utilsClass.constructToken(MULITADD4,tokenType,"10");
        log.info(transferData);

        String transferInfo= multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD,list);//相同币种
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("State"));
        Thread.sleep(SLEEPTIME);

        log.info("查询余额判断转账是否成功");
        String queryInfo= multiSign.Balance(MULITADD4,PRIKEY1,tokenType);
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("State"));
        assertEquals("0",JSONObject.fromObject(queryInfo).getJSONObject("Data").getString("Total"));

        log.info("解除锁定待转账Token: "+tokenType);
        String resp1=multiSign.recoverFrozenToken(PRIKEY1,tokenType);
        Thread.sleep(SLEEPTIME);

        log.info("查询归集地址中两种token余额");
        response1 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        String response2 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType2);
        assertEquals("200",JSONObject.fromObject(response1).getString("State"));
        assertEquals("1000",JSONObject.fromObject(response1).getJSONObject("Data").getString("Total"));
        assertEquals("200",JSONObject.fromObject(response2).getString("State"));
        assertEquals("1000",JSONObject.fromObject(response2).getJSONObject("Data").getString("Total"));

        transferData = "归集地址向" + "MULITADD4" + "转账10个" + tokenType+"归集地址向" + "MULITADD4" + "转账10个" + tokenType;
        list=utilsClass.constructToken(MULITADD4,tokenType,"10");
        List<Map>list2=utilsClass.constructToken(MULITADD5,tokenType2,"10",list);
        List<Map>list3=utilsClass.constructToken(MULITADD5,tokenType,"10",list);
        log.info(transferData);
        transferInfo= multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD,list2);//不同币种
        Thread.sleep(SLEEPTIME); //UTXO关系，两笔交易之间需要休眠
        String transferInfo2= multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD,list3);//相同币种
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("State"));
        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("State"));
        Thread.sleep(SLEEPTIME);

        log.info("查询余额判断转账是否成功");
        queryInfo= multiSign.Balance(MULITADD4,PRIKEY1,tokenType);
        String queryInfo2= multiSign.Balance(MULITADD5,PRIKEY1,tokenType2);
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("State"));
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("State"));
        assertEquals("20",JSONObject.fromObject(queryInfo).getJSONObject("Data").getString("Total"));
        assertEquals("10",JSONObject.fromObject(queryInfo2).getJSONObject("Data").getString("Total"));

        multiSign.Balance(IMPPUTIONADD,PRIKEY4, tokenType);
        multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType2);

        log.info("回收Token");
        String recycleInfo = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType, "970");
        String recycleInfo2 = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType2, "990");
        String recycleInfo3 = multiSign.Recycle(MULITADD4, PRIKEY1, tokenType, "20");
        String recycleInfo4 = multiSign.Recycle(MULITADD5, PRIKEY1, tokenType2, "10");
        String recycleInfo5 = multiSign.Recycle(MULITADD5, PRIKEY1, tokenType, "10");
        Thread.sleep(SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(recycleInfo).getString("State"));
        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("State"));
        assertEquals("200",JSONObject.fromObject(recycleInfo3).getString("State"));
        assertEquals("200",JSONObject.fromObject(recycleInfo4).getString("State"));
        assertEquals("200",JSONObject.fromObject(recycleInfo5).getString("State"));


        log.info("查询余额判断回收成功与否");
        String queryInfo3= multiSign.Balance(MULITADD4,PRIKEY1,tokenType);
        String queryInfo4= multiSign.Balance(MULITADD5,PRIKEY1,tokenType2);
        String queryInfo5= multiSign.Balance(MULITADD5,PRIKEY1,tokenType);

        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("State"));
        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("State"));
        assertEquals("200",JSONObject.fromObject(queryInfo5).getString("State"));

        assertEquals("0",JSONObject.fromObject(queryInfo3).getJSONObject("Data").getString("Total"));
        assertEquals("0",JSONObject.fromObject(queryInfo4).getJSONObject("Data").getString("Total"));
        assertEquals("0",JSONObject.fromObject(queryInfo5).getJSONObject("Data").getString("Total"));

    }

    /**
     *TC19归集地址向两个多签地址转账
     * 发两种币-查询归集地址-转账两个地址不同token-查询-回收-查询
     * @throws Exception
     */
     @Test
     public void TC19_transferMulti()throws  Exception{


         log.info("查询归集地址中两种token余额");
         String response1 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
         String response2 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType2);
         assertEquals("200",JSONObject.fromObject(response1).getString("State"));
         assertEquals("200",JSONObject.fromObject(response2).getString("State"));
         assertEquals("1000",JSONObject.fromObject(response1).getJSONObject("Data").getString("Total"));
         assertEquals("1000",JSONObject.fromObject(response2).getJSONObject("Data").getString("Total"));

         String transferData = "归集地址向" + "MULITADD4" + "转账10个" + tokenType+"归集地址向" + "MULITADD4" + "转账10个" + tokenType;
         List<Map>list=utilsClass.constructToken(MULITADD4,tokenType,"10");
         List<Map>list2=utilsClass.constructToken(MULITADD5,tokenType2,"10",list);
         List<Map>list3=utilsClass.constructToken(MULITADD5,tokenType,"10",list);
         log.info(transferData);
         String transferInfo= multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD,list2);//不同币种
         Thread.sleep(SLEEPTIME); //UTXO关系，两笔交易之间需要休眠
         String transferInfo2= multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD,list3);//相同币种
         assertEquals("200",JSONObject.fromObject(transferInfo).getString("State"));
         assertEquals("200",JSONObject.fromObject(transferInfo2).getString("State"));
         Thread.sleep(SLEEPTIME);

         log.info("查询余额判断转账是否成功");
         String queryInfo= multiSign.Balance(MULITADD4,PRIKEY1,tokenType);
         String queryInfo2= multiSign.Balance(MULITADD5,PRIKEY1,tokenType2);
         assertEquals("200",JSONObject.fromObject(queryInfo).getString("State"));
         assertEquals("200",JSONObject.fromObject(queryInfo2).getString("State"));
         assertEquals("20",JSONObject.fromObject(queryInfo).getJSONObject("Data").getString("Total"));
         assertEquals("10",JSONObject.fromObject(queryInfo2).getJSONObject("Data").getString("Total"));

         multiSign.Balance(IMPPUTIONADD,PRIKEY4, tokenType);
         multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType2);

         log.info("回收Token");
         String recycleInfo = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType, "970");
         String recycleInfo2 = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType2, "990");
         String recycleInfo3 = multiSign.Recycle(MULITADD4, PRIKEY1, tokenType, "20");
         String recycleInfo4 = multiSign.Recycle(MULITADD5, PRIKEY1, tokenType2, "10");
         String recycleInfo5 = multiSign.Recycle(MULITADD5, PRIKEY1, tokenType, "10");
         Thread.sleep(SLEEPTIME);

         assertEquals("200",JSONObject.fromObject(recycleInfo).getString("State"));
         assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("State"));
         assertEquals("200",JSONObject.fromObject(recycleInfo3).getString("State"));
         assertEquals("200",JSONObject.fromObject(recycleInfo4).getString("State"));
         assertEquals("200",JSONObject.fromObject(recycleInfo5).getString("State"));

         log.info("查询余额判断回收成功与否");
         String queryInfo3= multiSign.Balance(MULITADD4,PRIKEY1,tokenType);
         String queryInfo4= multiSign.Balance(MULITADD5,PRIKEY1,tokenType2);
         String queryInfo5= multiSign.Balance(MULITADD5,PRIKEY1,tokenType);
         assertEquals("200",JSONObject.fromObject(queryInfo3).getString("State"));
         assertEquals("200",JSONObject.fromObject(queryInfo4).getString("State"));
         assertEquals("200",JSONObject.fromObject(queryInfo5).getString("State"));
         assertEquals("0",JSONObject.fromObject(queryInfo3).getJSONObject("Data").getString("Total"));
         assertEquals("0",JSONObject.fromObject(queryInfo4).getJSONObject("Data").getString("Total"));
         assertEquals("0",JSONObject.fromObject(queryInfo5).getJSONObject("Data").getString("Total"));
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
        Thread.sleep(SLEEPTIME);//UTXO关系，两笔交易之间需要休眠
        String transferInfo2= multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD,list3);//相同币种
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("State"));
        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("State"));
        Thread.sleep(SLEEPTIME);
        log.info("查询余额判断转账是否成功");
        String queryInfo= multiSign.Balance(PRIKEY1,tokenType);
        String queryInfo2= multiSign.Balance(MULITADD4,PRIKEY1,tokenType2);
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("State"));
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("State"));
        assertEquals("20",JSONObject.fromObject(queryInfo).getJSONObject("Data").getString("Total"));
        assertEquals("10",JSONObject.fromObject(queryInfo2).getJSONObject("Data").getString("Total"));

        log.info("回收Token");
        String recycleInfo = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType, "970");
        String recycleInfo2 = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType2, "990");
        String recycleInfo3 = multiSign.Recycle(PRIKEY1, tokenType, "20");
        String recycleInfo4 = multiSign.Recycle(MULITADD4, PRIKEY1, tokenType2, "10");
        String recycleInfo5 = multiSign.Recycle(MULITADD4, PRIKEY1, tokenType, "10");

        assertEquals("200",JSONObject.fromObject(recycleInfo).getString("State"));
        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("State"));
        assertEquals("200",JSONObject.fromObject(recycleInfo3).getString("State"));
        assertEquals("200",JSONObject.fromObject(recycleInfo4).getString("State"));
        assertEquals("200",JSONObject.fromObject(recycleInfo5).getString("State"));

        log.info("查询余额判断回收成功与否");
        String queryInfo3= multiSign.Balance(PRIKEY1,tokenType);
        String queryInfo4= multiSign.Balance(MULITADD4,PRIKEY1,tokenType2);
        String queryInfo5= multiSign.Balance(MULITADD4,PRIKEY1,tokenType);

        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("State"));
        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("State"));
        assertEquals("200",JSONObject.fromObject(queryInfo5).getString("State"));
        assertEquals("20",JSONObject.fromObject(queryInfo3).getJSONObject("Data").getString("Total"));
        assertEquals("10",JSONObject.fromObject(queryInfo4).getJSONObject("Data").getString("Total"));
        assertEquals("10",JSONObject.fromObject(queryInfo5).getJSONObject("Data").getString("Total"));
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
        Thread.sleep(SLEEPTIME);//UTXO关系，两笔交易之间需要休眠
        String transferInfo2= multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD,list3);//两个地址相同币种
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("State"));
        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("State"));
        Thread.sleep(SLEEPTIME);

        log.info("查询余额判断转账是否成功");
        String queryInfo= multiSign.Balance(PRIKEY1,tokenType);
        String queryInfo2= multiSign.Balance(PRIKEY2,tokenType2);
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("State"));
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("State"));
        assertEquals("20",JSONObject.fromObject(queryInfo).getJSONObject("Data").getString("Total"));
        assertEquals("10",JSONObject.fromObject(queryInfo2).getJSONObject("Data").getString("Total"));

        log.info("回收Token");
        String recycleInfo = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType, "970");
        String recycleInfo2 = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType2, "990");
        String recycleInfo3 = multiSign.Recycle(PRIKEY1, tokenType, "20");
        String recycleInfo4 = multiSign.Recycle( PRIKEY2, tokenType2, "10");
        String recycleInfo5 = multiSign.Recycle(PRIKEY2, tokenType, "10");
        Thread.sleep(SLEEPTIME);

        assertEquals("200",JSONObject.fromObject(recycleInfo).getString("State"));
        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("State"));
        assertEquals("200",JSONObject.fromObject(recycleInfo3).getString("State"));
        assertEquals("200",JSONObject.fromObject(recycleInfo4).getString("State"));
        assertEquals("200",JSONObject.fromObject(recycleInfo5).getString("State"));

        log.info("查询余额判断回收成功与否");
        String queryInfo3= multiSign.Balance(PRIKEY1,tokenType);
        String queryInfo4= multiSign.Balance(PRIKEY2,tokenType2);
        String queryInfo5= multiSign.Balance(PRIKEY2,tokenType);
        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("State"));
        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("State"));
        assertEquals("200",JSONObject.fromObject(queryInfo5).getString("State"));
        assertEquals("0",JSONObject.fromObject(queryInfo3).getJSONObject("Data").getString("Total"));
        assertEquals("0",JSONObject.fromObject(queryInfo4).getJSONObject("Data").getString("Total"));
        assertEquals("0",JSONObject.fromObject(queryInfo5).getJSONObject("Data").getString("Total"));
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
        assertEquals("200",JSONObject.fromObject(transferInfoInit).getString("State"));
        Thread.sleep(SLEEPTIME);
        List<Map> list = utilsClass.constructToken(ADDRESS1, tokenType, "10");
        List<Map> list2 = utilsClass.constructToken(MULITADD5, tokenType2, "10", list);
        List<Map> list3 = utilsClass.constructToken(MULITADD5, tokenType, "10", list);
        log.info(transferData);
        String transferInfo = multiSign.Transfer(PRIKEY1, transferData, MULITADD4, list2);//不相同币种
        Thread.sleep(SLEEPTIME);//UTXO关系，两笔交易之间需要休眠
        String transferInfo2 = multiSign.Transfer(PRIKEY1, transferData, MULITADD4, list3);//相同币种
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("State"));
        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("State"));
        Thread.sleep(SLEEPTIME);

        log.info("查询余额判断转账是否成功");
        String queryInfo = multiSign.Balance(PRIKEY1, tokenType);
        String queryInfo2 = multiSign.Balance(MULITADD5, PRIKEY1, tokenType2);
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("State"));
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("State"));
        assertEquals("20",JSONObject.fromObject(queryInfo).getJSONObject("Data").getString("Total"));
        assertEquals("10",JSONObject.fromObject(queryInfo2).getJSONObject("Data").getString("Total"));

        log.info("锁定待回收Token: "+tokenType);
        String resp2=multiSign.freezeToken(PRIKEY1,tokenType);
        Thread.sleep(SLEEPTIME);

        log.info("回收Token");
        String recycleInfo = multiSign.Recycle(MULITADD4, PRIKEY1, tokenType, "970");
        String recycleInfo2 = multiSign.Recycle(MULITADD4, PRIKEY1, tokenType2, "990");
        String recycleInfo3 = multiSign.Recycle(PRIKEY1, tokenType, "20");
        String recycleInfo4 = multiSign.Recycle(MULITADD5, PRIKEY1, tokenType2, "10");
        String recycleInfo5 = multiSign.Recycle(MULITADD5, PRIKEY1, tokenType, "10");
        Thread.sleep(SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(recycleInfo).getString("State"));
        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("State"));
        assertEquals("200",JSONObject.fromObject(recycleInfo3).getString("State"));
        assertEquals("200",JSONObject.fromObject(recycleInfo4).getString("State"));
        assertEquals("200",JSONObject.fromObject(recycleInfo5).getString("State"));

        log.info("查询余额判断回收成功与否");
        String queryInfo3 = multiSign.Balance(PRIKEY1, tokenType);
        String queryInfo4 = multiSign.Balance(MULITADD5, PRIKEY1, tokenType2);
        String queryInfo5 = multiSign.Balance(MULITADD5, PRIKEY1, tokenType);
        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("State"));
        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("State"));
        assertEquals("200",JSONObject.fromObject(queryInfo5).getString("State"));
        assertEquals("0",JSONObject.fromObject(queryInfo3).getJSONObject("Data").getString("Total"));
        assertEquals("0",JSONObject.fromObject(queryInfo4).getJSONObject("Data").getString("Total"));
        assertEquals("0",JSONObject.fromObject(queryInfo5).getJSONObject("Data").getString("Total"));
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
        assertEquals("200",JSONObject.fromObject(transferInfoInit).getString("State"));
        Thread.sleep(SLEEPTIME);
        String ab = multiSign.Balance(MULITADD4,PRIKEY1, tokenType);
        log.info("----------------------");
        List<Map> list = utilsClass.constructToken(MULITADD6, tokenType, "10");
        List<Map> list2 = utilsClass.constructToken(MULITADD5, tokenType2, "10", list);
        List<Map> list3 = utilsClass.constructToken(MULITADD5, tokenType, "10", list);
        log.info(transferData);
        String transferInfo = multiSign.Transfer(PRIKEY1, transferData, MULITADD4, list2);//不相同币种
        Thread.sleep(SLEEPTIME);//UTXO关系，两笔交易之间需要休眠
        String transferInfo2 = multiSign.Transfer(PRIKEY1, transferData, MULITADD4, list3);//相同币种
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("State"));
        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("State"));
        Thread.sleep(SLEEPTIME);

        log.info("查询余额判断转账是否成功");
        String queryInfo = multiSign.Balance(MULITADD6,PRIKEY4, tokenType);
        String queryInfo2 = multiSign.Balance(MULITADD5, PRIKEY1, tokenType2);
        String queryInfo1 = multiSign.Balance(MULITADD5, PRIKEY1, tokenType);
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("State"));
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("State"));
        assertEquals("200",JSONObject.fromObject(queryInfo1).getString("State"));
        assertEquals("20",JSONObject.fromObject(queryInfo).getJSONObject("Data").getString("Total"));
        assertEquals("10",JSONObject.fromObject(queryInfo2).getJSONObject("Data").getString("Total"));
        assertEquals("10",JSONObject.fromObject(queryInfo1).getJSONObject("Data").getString("Total"));


        //20190411增加锁定解锁操作步骤后进行回收
        log.info("锁定待回收Token: "+tokenType);
        String resp=multiSign.freezeToken(PRIKEY1,tokenType);
        Thread.sleep(SLEEPTIME);
        log.info("解除锁定待回收Token: "+tokenType);
        String resp1=multiSign.recoverFrozenToken(PRIKEY1,tokenType);
        Thread.sleep(SLEEPTIME);

        log.info("回收Token");
        String recycleInfo = multiSign.Recycle(MULITADD4, PRIKEY1, tokenType, "970");
        String recycleInfo2 = multiSign.Recycle(MULITADD4, PRIKEY1, tokenType2, "990");
        String recycleInfo3 = multiSign.Recycle(MULITADD6,PRIKEY4, tokenType, "20");
        String recycleInfo4 = multiSign.Recycle(MULITADD5, PRIKEY1, tokenType2, "10");
        String recycleInfo5 = multiSign.Recycle(MULITADD5, PRIKEY1, tokenType, "10");
        Thread.sleep(SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(recycleInfo).getString("State"));
        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("State"));
        assertEquals("200",JSONObject.fromObject(recycleInfo3).getString("State"));
        assertEquals("200",JSONObject.fromObject(recycleInfo4).getString("State"));
        assertEquals("200",JSONObject.fromObject(recycleInfo5).getString("State"));

        log.info("查询余额判断回收成功与否");
        String queryInfo3 = multiSign.Balance(MULITADD6,PRIKEY4, tokenType);
        String queryInfo4 = multiSign.Balance(MULITADD5, PRIKEY1, tokenType2);
        String queryInfo5 = multiSign.Balance(MULITADD5, PRIKEY1, tokenType);
        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("State"));
        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("State"));
        assertEquals("200",JSONObject.fromObject(queryInfo5).getString("State"));
        assertEquals("0",JSONObject.fromObject(queryInfo3).getJSONObject("Data").getString("Total"));
        assertEquals("0",JSONObject.fromObject(queryInfo4).getJSONObject("Data").getString("Total"));
        assertEquals("0",JSONObject.fromObject(queryInfo5).getJSONObject("Data").getString("Total"));

        String zero=  multiSign.QueryZero(tokenType);
        String zero2= multiSign.QueryZero(tokenType2);
        Thread.sleep(SLEEPTIME);//回收操作相当于转账
        assertEquals("200",JSONObject.fromObject(zero).getString("State"));
        assertEquals("1000",JSONObject.fromObject(zero).getJSONObject("Data").getJSONObject("Detail").getString(tokenType));
        assertEquals("200",JSONObject.fromObject(zero2).getString("State"));
        assertEquals("1000",JSONObject.fromObject(zero2).getJSONObject("Data").getJSONObject("Detail").getString(tokenType2));
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
        assertEquals("200",JSONObject.fromObject(transferInfoInit).getString("State"));
        Thread.sleep(SLEEPTIME);
        List<Map> list = utilsClass.constructToken(ADDRESS1, tokenType, "10");
        List<Map> list2 = utilsClass.constructToken(ADDRESS2,tokenType2, "10", list);
        List<Map> list3 = utilsClass.constructToken(ADDRESS2, tokenType, "10", list);
        log.info(transferData);
        String transferInfo = multiSign.Transfer(PRIKEY1, transferData, MULITADD4, list2);//不相同币种
        Thread.sleep(SLEEPTIME);//UTXO关系，两笔交易之间需要休眠
        String transferInfo2 = multiSign.Transfer(PRIKEY1, transferData, MULITADD4, list3);//相同币种
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("State"));
        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("State"));
        Thread.sleep(SLEEPTIME);

        log.info("查询余额判断转账是否成功");
        String queryInfo = multiSign.Balance(PRIKEY1, tokenType);
        String queryInfo2 = multiSign.Balance(PRIKEY2, tokenType2);
        String queryInfo_2=multiSign.Balance(PRIKEY2,tokenType);
        assertEquals("20",JSONObject.fromObject(queryInfo).getJSONObject("Data").getString("Total"));
        assertEquals("10",JSONObject.fromObject(queryInfo2).getJSONObject("Data").getString("Total"));
        assertEquals("10",JSONObject.fromObject(queryInfo_2).getJSONObject("Data").getString("Total"));

        log.info("锁定待回收Token: "+tokenType);
        String resp=multiSign.freezeToken(PRIKEY1,tokenType);
        Thread.sleep(SLEEPTIME);
        log.info("回收Token");
        String recycleInfo = multiSign.Recycle(MULITADD4, PRIKEY1, tokenType, "970");
        String recycleInfo2 = multiSign.Recycle(MULITADD4, PRIKEY1, tokenType2, "990");
        String recycleInfo3 = multiSign.Recycle(PRIKEY1, tokenType, "20");
        String recycleInfo4 = multiSign.Recycle(PRIKEY2, tokenType2, "10");
        String recycleInfo5 = multiSign.Recycle(PRIKEY2, tokenType, "10");
        Thread.sleep(SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(recycleInfo).getString("State"));
        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("State"));
        assertEquals("200",JSONObject.fromObject(recycleInfo3).getString("State"));
        assertEquals("200",JSONObject.fromObject(recycleInfo4).getString("State"));
        assertEquals("200",JSONObject.fromObject(recycleInfo5).getString("State"));
        log.info("查询余额判断回收成功与否");
        String queryInfo3 = multiSign.Balance(PRIKEY1, tokenType);
        String queryInfo4 = multiSign.Balance(PRIKEY2, tokenType2);
        String queryInfo5 = multiSign.Balance(PRIKEY2, tokenType);
        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("State"));
        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("State"));
        assertEquals("200",JSONObject.fromObject(queryInfo5).getString("State"));
        assertEquals("0",JSONObject.fromObject(queryInfo3).getJSONObject("Data").getString("Total"));
        assertEquals("0",JSONObject.fromObject(queryInfo4).getJSONObject("Data").getString("Total"));
        assertEquals("0",JSONObject.fromObject(queryInfo5).getJSONObject("Data").getString("Total"));
    }

    //-----------------------------------------------------------------------------------------------------------

    public  String IssueToken(int length,String  amount,String ToAddr){
        String tokenType = "CX-" + UtilsClass.Random(length);
        //String amount = "1000";
        log.info(MULITADD3+ "发行" + tokenType + " token，数量为：" + amount);
        String data = "MULITADD3" + "发行" + tokenType + " token，数量为：" + amount;
        String response = multiSign.issueToken(IMPPUTIONADD,ToAddr,tokenType, amount, data);
        assertThat(response, containsString("200"));
        String Tx1 = JSONObject.fromObject(response).getJSONObject("Data").getString("Tx");
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
        log.info(MULITADD3+ "发行" + tokenType + " token，数量为：" + amount);
        String data = "MULITADD3" + "发行" + tokenType + " token，数量为：" + amount;
        String response = multiSign.issueToken(IMPPUTIONADD, tokenType, amount, data);
        assertThat(response, containsString("200"));
        String Tx1 = JSONObject.fromObject(response).getJSONObject("Data").getString("Tx");
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
