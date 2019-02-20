package com.tjfintech.common.functionTest;


import com.bw.base.ByteStringArrayListToBytes;
import com.bw.base.MultiSignIssue;
import com.bw.base.MultiSignTransferAccounts;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.Util;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.List;
import java.util.Map;

import static com.tjfintech.common.functionTest.StoreTest.SLEEPTIME;
import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LocalMultiSignTest {
    private static String tokenType;
    private static String tokenType2;
    TestBuilder testBuilder = TestBuilder.getInstance();
    MultiSign multiSign = testBuilder.getMultiSign();
    UtilsClass utilsClass = new UtilsClass();
    MultiSignIssue multiIssue = new MultiSignIssue();
    MultiSignTransferAccounts multiSignTrans = new MultiSignTransferAccounts();
    ByteStringArrayListToBytes convert = new ByteStringArrayListToBytes();

    @Before
    public void beforeConfig() throws Exception {

        tokenType = IssueTokenV2(5, "1000");
        Thread.sleep(SLEEPTIME);


    }

    @Test
    public void test() throws Exception {
        log.info("test");
    }

    public String IssueTokenV2(int length, String amount) throws Exception {
        String tokenType = "MT-" + UtilsClass.Random(length);
        String data = "MULITADD1" + "发行" + tokenType + " token，数量为：" + amount;
        log.info(data);
        String response = multiSign.issueToken(MULITADD1, tokenType, amount, data);
        log.info(response);
        //assertThat(response, containsString("200"));
        // String Tx1 = JSONObject.fromObject(response).getJSONObject("Data").getString("Tx");
        //log.info(Tx1);

        log.info("第一次签名");
        byte[] response1 = multiIssue.multiSignIssueMethod(response, "C:\\Users\\Administrator\\Downloads\\163\\key1.pem");
        System.out.println("-----------------------多签发行--------------------------");
        System.out.println(Util.byteToHex(response1));



//        //log.info("第二次签名");
//        String response3 = multiSign.Sign(Tx2, PRIKEY7,PWD7);
//        String Tx3 = JSONObject.fromObject(response3).getJSONObject("Data").getString("Tx");
//        //log.info("第三次签名");
//        String response4 = multiSign.Sign(Tx3, PRIKEY1);
//        assertThat(JSONObject.fromObject(response4).getJSONObject("Data").getString("IsCompleted"), containsString("true"));
//        assertThat(response4, containsString("200"));
        return tokenType;

    }


    /**
     * Tc03多签正常流程-发币：签名：查询：转账：查询:回收：查询
     */
 //   @Test
//    public void TC03_multiProgress() throws Exception {
//        String transferData = "归集地址向" + MULITADD4 + "转账10个" + tokenType;
//        log.info(transferData);
//        List<Map> list = utilsClass.constructToken(MULITADD4, tokenType, "10");
//        log.info(transferData);
//        String transferInfo = multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD, list);
//        Thread.sleep(SLEEPTIME);
//        assertThat(transferInfo, containsString("200"));
//
//        log.info("查询归集地址跟MULITADD4余额，判断转账是否成功");
//        String queryInfo = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
//        String queryInfo2 = multiSign.Balance(MULITADD4, PRIKEY1, tokenType);
//        assertThat(queryInfo, containsString("200"));
//        assertThat(queryInfo, containsString("990"));
//        assertThat(queryInfo2, containsString("200"));
//        assertThat(queryInfo2, containsString("10"));
//
//        log.info("回收归集地址跟MULITADD4的新发token");
//        String recycleInfo = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType, "990");
//        String recycleInfo2 = multiSign.Recycle(MULITADD4, PRIKEY1, tokenType, "10");
//        assertThat(recycleInfo, containsString("200"));
//        assertThat(recycleInfo2, containsString("200"));
//        Thread.sleep(SLEEPTIME);
//
//        log.info("查询回收后账户余额是否为0");
//        String queryInfo3 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
//        String queryInfo4 = multiSign.Balance(MULITADD4, PRIKEY1, tokenType);
//        assertThat(queryInfo3, containsString("200"));
//        assertThat(JSONObject.fromObject(queryInfo3).getJSONObject("Data").getString("Total"), containsString("0"));
//        assertThat(queryInfo4, containsString("200"));
//        assertThat(JSONObject.fromObject(queryInfo4).getJSONObject("Data").getString("Total"), containsString("0"));
//
//
//    }



}
