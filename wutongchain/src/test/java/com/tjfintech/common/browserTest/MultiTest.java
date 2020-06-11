package com.tjfintech.common.browserTest;


import com.tjfintech.common.BeforeCondition;
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

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;


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


    @BeforeClass
    public static void beforeClass() throws Exception {
        BeforeCondition bf = new BeforeCondition();
        bf.updatePubPriKey();
        bf.collAddressTest();
        Thread.sleep(SLEEPTIME);
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
        Thread.sleep(SLEEPTIME);
        log.info("查询归集地址中两种token余额");
        String response1 = multiSign.Balance(IMPPUTIONADD,PRIKEY4, tokenType);
        String response2 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType2);
        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
        assertEquals(actualAmount1,JSONObject.fromObject(response1).getJSONObject("Data").getString("Total"));
        assertEquals("200",JSONObject.fromObject(response2).getString("state"));
        assertEquals(actualAmount2,JSONObject.fromObject(response2).getJSONObject("Data").getString("Total"));
        Thread.sleep(SLEEPTIME);

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

        String amount1;

        if (UtilsClass.PRECISION == 10) {
            amount1 = "990.1234567891";
        }else {
            amount1 = "990.123456";
        }

        log.info("查询归集地址跟MULITADD4余额，判断转账是否成功");
        String queryInfo = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        String queryInfo2 = multiSign.Balance(MULITADD4, PRIKEY1, tokenType);
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
        assertEquals(amount1,JSONObject.fromObject(queryInfo).getJSONObject("Data").getString("Total"));
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("state"));
        assertEquals("10",JSONObject.fromObject(queryInfo2).getJSONObject("Data").getString("Total"));

        log.info("回收归集地址跟MULITADD4的新发token");
        String recycleInfo = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType, amount1);
        String recycleInfo2 = multiSign.Recycle(MULITADD4, PRIKEY1, tokenType, "10");
        assertEquals("200",JSONObject.fromObject(recycleInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("state"));
        Thread.sleep(SLEEPTIME);

        log.info("查询回收后账户余额是否为0");
        String queryInfo3 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        String queryInfo4 = multiSign.Balance(MULITADD4, PRIKEY1, tokenType);
        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("state"));
        assertEquals("0",JSONObject.fromObject(queryInfo3).getJSONObject("Data").getString("Total"));
        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("state"));
        assertEquals("0",JSONObject.fromObject(queryInfo4).getJSONObject("Data").getString("Total"));


    }



    /**
     * Tc024锁定后转账:
     *
     */
    //@Test
    public void TC024_TransferAfterFrozen() throws Exception {

        //20190411增加锁定步骤后进行转账
        log.info("锁定待转账Token: "+tokenType);
        String resp=multiSign.freezeToken(tokenType);
        Thread.sleep(SLEEPTIME);


        log.info("查询归集地址中两种token余额");
        String response1 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
        assertEquals(actualAmount1,JSONObject.fromObject(response1).getJSONObject("Data").getString("Total"));

        String transferData = "归集地址向MULITADD4转账10个" + tokenType;
        List<Map>list=utilsClass.constructToken(MULITADD4,tokenType,"10");
        log.info(transferData);

        String transferInfo= multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD,list);//相同币种
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
        Thread.sleep(SLEEPTIME);

        log.info("查询余额判断转账是否成功");
        String queryInfo= multiSign.Balance(MULITADD4,PRIKEY1,tokenType);
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
        assertEquals("0",JSONObject.fromObject(queryInfo).getJSONObject("Data").getString("Total"));

        log.info("解除锁定待转账Token: "+tokenType);
        String resp1=multiSign.recoverFrozenToken(tokenType);
        Thread.sleep(SLEEPTIME);

        log.info("查询归集地址中两种token余额");
        response1 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        String response2 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType2);
        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
        assertEquals(actualAmount1,JSONObject.fromObject(response1).getJSONObject("Data").getString("Total"));
        assertEquals("200",JSONObject.fromObject(response2).getString("state"));
        assertEquals(actualAmount2,JSONObject.fromObject(response2).getJSONObject("Data").getString("Total"));

        transferData = "归集地址向" + "MULITADD4" + "转账10个" + tokenType+"归集地址向" + "MULITADD4" + "转账10个" + tokenType;
        list=utilsClass.constructToken(MULITADD4,tokenType,"10");
        List<Map>list2=utilsClass.constructToken(MULITADD5,tokenType2,"10",list);
        List<Map>list3=utilsClass.constructToken(MULITADD5,tokenType,"10",list);
        log.info(transferData);
        transferInfo= multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD,list2);//不同币种
        Thread.sleep(SLEEPTIME); //UTXO关系，两笔交易之间需要休眠
        String transferInfo2= multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD,list3);//相同币种
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("state"));
        Thread.sleep(SLEEPTIME);

        log.info("查询余额判断转账是否成功");
        queryInfo= multiSign.Balance(MULITADD4,PRIKEY1,tokenType);
        String queryInfo2= multiSign.Balance(MULITADD5,PRIKEY1,tokenType2);
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("state"));
        assertEquals("20",JSONObject.fromObject(queryInfo).getJSONObject("Data").getString("Total"));
        assertEquals("10",JSONObject.fromObject(queryInfo2).getJSONObject("Data").getString("Total"));

        multiSign.Balance(IMPPUTIONADD,PRIKEY4, tokenType);
        multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType2);

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
        Thread.sleep(SLEEPTIME);
        assertEquals("200",JSONObject.fromObject(recycleInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo3).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo4).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo5).getString("state"));


        log.info("查询余额判断回收成功与否");
        String queryInfo3= multiSign.Balance(MULITADD4,PRIKEY1,tokenType);
        String queryInfo4= multiSign.Balance(MULITADD5,PRIKEY1,tokenType2);
        String queryInfo5= multiSign.Balance(MULITADD5,PRIKEY1,tokenType);

        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo5).getString("state"));

        assertEquals("0",JSONObject.fromObject(queryInfo3).getJSONObject("Data").getString("Total"));
        assertEquals("0",JSONObject.fromObject(queryInfo4).getJSONObject("Data").getString("Total"));
        assertEquals("0",JSONObject.fromObject(queryInfo5).getJSONObject("Data").getString("Total"));

    }


    //-----------------------------------------------------------------------------------------------------------

    public  String IssueToken(int length,String  amount){
        String tokenType = "Multi-" + UtilsClass.Random(length);
        log.info(IMPPUTIONADD+ "发行" + tokenType + "，数量为：" + amount);
        String data = "发行" + tokenType + "，数量为：" + amount;
        String response = multiSign.issueToken(IMPPUTIONADD, tokenType, amount, data);
        assertThat(response, containsString("200"));
        String Tx1 = JSONObject.fromObject(response).getJSONObject("Data").getString("Tx");
        log.info("第一次签名");
        String response2 = multiSign.Sign(Tx1, PRIKEY5);
        assertThat(response2, containsString("200"));
        return tokenType;
    }


}
