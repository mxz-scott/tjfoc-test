package com.tjfintech.common.functionTest.utxoMultiSign;


import com.bw.base.MultiSignIssue;
import com.bw.base.MultiSignTransferAccounts;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.TestBuilder;
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
import static com.tjfoc.utils.ReadFiletoByte.log;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LocalMultiSignInvalidTest {
    private static String tokenType;
    private static String tokenType2;
    TestBuilder testBuilder = TestBuilder.getInstance();
    MultiSign multiSign = testBuilder.getMultiSign();
    UtilsClass utilsClass = new UtilsClass();
    MultiSignIssue multiIssue = new MultiSignIssue();
    MultiSignTransferAccounts multiTrans = new MultiSignTransferAccounts();
    LocalMultiSignTest multiSignTest = new LocalMultiSignTest();

    @Before
    public void beforeConfig() throws Exception {

        log.info("发行两种token1000个");
        tokenType = multiSignTest.IssueTokenLocalSign(7, "100");
        tokenType2 = multiSignTest.IssueTokenLocalSign(8, "100.123");

        Thread.sleep(SLEEPTIME);
        log.info("查询归集地址中token余额");
        String balance = multiSign.BalanceByAddr(IMPPUTIONADD, tokenType);
        String balance2 = multiSign.BalanceByAddr(IMPPUTIONADD, tokenType2);

        assertThat(tokenType + "查询余额不正确", balance, containsString("\"Total\":\"100\""));
        assertThat(tokenType2 + "查询余额不正确", balance2, containsString("\"Total\":\"100.123\""));
    }

    /**
     * 多账号同时转账，但余额不足。
     */
    @Test
    public void TC_multiProgress_Transfer() throws Exception {


        String transferData = "归集地址向两个多签地址转账异常测试";
        List<Map> list = utilsClass.constructToken(MULITADD4, tokenType, "10");//A足
        List<Map> list0 = utilsClass.constructToken(MULITADD4, tokenType2, "101");//A 不足

        List<Map> list2 = utilsClass.constructToken(MULITADD5, tokenType, "101", list);//相同币种，A足B不足
        List<Map> list3 = utilsClass.constructToken(MULITADD5, tokenType, "10", list0);//相同币种，A不足B足
        List<Map> list4 = utilsClass.constructToken(MULITADD5, tokenType, "101", list0);//相同币种，A不足B不足

        List<Map> list5 = utilsClass.constructToken(MULITADD5, tokenType2, "101", list);//不同币种，A足B不足
        List<Map> list6 = utilsClass.constructToken(MULITADD5, tokenType2, "10", list0);//不同币种，A不足B足
        List<Map> list7 = utilsClass.constructToken(MULITADD5, tokenType2, "101", list0);//不同币种，A不足B不足


        log.info(transferData);
        String transferInfo2 = multiSignTest.multiSignTransfer_LocalSign(IMPPUTIONADD, PUBKEY4, transferData, list2, PRIKEY4PATH);
        String transferInfo3 = multiSignTest.multiSignTransfer_LocalSign(IMPPUTIONADD, PUBKEY4, transferData, list3, PRIKEY4PATH);
        String transferInfo4 = multiSignTest.multiSignTransfer_LocalSign(IMPPUTIONADD, PUBKEY4, transferData, list4, PRIKEY4PATH);

        String transferInfo5 = multiSignTest.multiSignTransfer_LocalSign(IMPPUTIONADD, PUBKEY4, transferData, list5, PRIKEY4PATH);
        String transferInfo6 = multiSignTest.multiSignTransfer_LocalSign(IMPPUTIONADD, PUBKEY4, transferData, list6, PRIKEY4PATH);
        String transferInfo7 = multiSignTest.multiSignTransfer_LocalSign(IMPPUTIONADD, PUBKEY4, transferData, list7, PRIKEY4PATH);
        Thread.sleep(SLEEPTIME); //UTXO关系，两笔交易之间需要休眠
        assertThat(transferInfo2, containsString("400"));
        assertThat(transferInfo3, containsString("400"));
        assertThat(transferInfo4, containsString("400"));
        assertThat(transferInfo5, containsString("400"));
        assertThat(transferInfo6, containsString("400"));
        assertThat(transferInfo7, containsString("400"));
        assertThat(JSONObject.fromObject(transferInfo2).getString("Message"), equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo3).getString("Message"), equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo4).getString("Message"), equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo5).getString("Message"), equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo6).getString("Message"), equalTo("insufficient balance"));
        assertThat(JSONObject.fromObject(transferInfo7).getString("Message"), equalTo("insufficient balance"));

    }


    /**
     * 多账号同时回收，回收多个多签地址,但余额不足。
     */
    @Test
    public void TC_multiProgress_Recycles() throws Exception {


        String transferData = "归集地址向MULITADD4转账10个: " + tokenType;
        log.info(transferData);
        List<Map> transferList = utilsClass.constructToken(MULITADD4, tokenType, "10");
        List<Map> transferList2 = utilsClass.constructToken(MULITADD4, tokenType2, "20.123", transferList);
        multiSignTest.multiSignTransfer_LocalSign(IMPPUTIONADD, PUBKEY4, transferData, transferList2, PRIKEY4PATH);

        Thread.sleep(SLEEPTIME);

        log.info("查询归集地址和MULITADD4余额，判断转账是否成功");
        String queryInfo = multiSign.BalanceByAddr(MULITADD4, tokenType);
        assertThat(queryInfo, containsString("200"));
        assertThat(queryInfo, containsString("\"Total\":\"10\""));

        String queryInfo11 = multiSign.BalanceByAddr(MULITADD4, tokenType2);
        assertThat(queryInfo11, containsString("200"));
        assertThat(queryInfo11, containsString("\"Total\":\"20.123\""));

        String queryInfo2 = multiSign.BalanceByAddr(IMPPUTIONADD, tokenType);
        assertThat(queryInfo2, containsString("200"));
        assertThat(queryInfo2, containsString("\"Total\":\"90\""));

        String queryInfo22 = multiSign.BalanceByAddr(IMPPUTIONADD, tokenType2);
        assertThat(queryInfo22, containsString("200"));
        assertThat(queryInfo22, containsString("\"Total\":\"80\""));

        log.info("多账号同时回收，余额都不足");
        List<Map> recycleList1 = utilsClass.constructToken(IMPPUTIONADD, PUBKEY4, tokenType, "1001");
        List<Map> recycleList2 = utilsClass.constructToken(MULITADD4, PUBKEY1, tokenType, "1002", recycleList1);

        String response1 = multiSign.RecyclesLocalSign(recycleList2);
//        log.info(response1);
        assertThat(response1, containsString("insufficient balance"));

        log.info("多账号同时回收，2余额不足");
        List<Map> recycleList3 = utilsClass.constructToken(IMPPUTIONADD, PUBKEY4, tokenType, "1");
        List<Map> recycleList4 = utilsClass.constructToken(MULITADD4, PUBKEY1, tokenType, "1002", recycleList3);

        String response2 = multiSign.RecyclesLocalSign(recycleList4);
//        log.info(response2);
        assertThat(response2, containsString("insufficient balance"));


        log.info("多账号同时回收，1余额不足");
        List<Map> recycleList5 = utilsClass.constructToken(IMPPUTIONADD, PUBKEY4, tokenType, "1002");
        List<Map> recycleList6 = utilsClass.constructToken(MULITADD4, PUBKEY1, tokenType, "1", recycleList5);

        String response3 = multiSign.RecyclesLocalSign(recycleList6);
//        log.info(response3);
        assertThat(response3, containsString("insufficient balance"));


        log.info("不同币种，多账号同时回收，余额都不足");
        List<Map> recycleList11 = utilsClass.constructToken(IMPPUTIONADD, PUBKEY4, tokenType, "1001");
        List<Map> recycleList22 = utilsClass.constructToken(MULITADD4, PUBKEY1, tokenType2, "1002", recycleList11);

        String response11 = multiSign.RecyclesLocalSign(recycleList22);
//        log.info(response11);
        assertThat(response11, containsString("insufficient balance"));

        log.info("不同币种，多账号同时回收，2余额不足");
        List<Map> recycleList33 = utilsClass.constructToken(IMPPUTIONADD, PUBKEY4, tokenType, "1");
        List<Map> recycleList43 = utilsClass.constructToken(MULITADD4, PUBKEY1, tokenType2, "1002", recycleList33);

        String response22 = multiSign.RecyclesLocalSign(recycleList43);
//        log.info(response22);
        assertThat(response22, containsString("insufficient balance"));


        log.info("不同币种，多账号同时回收，1余额不足");
        List<Map> recycleList52 = utilsClass.constructToken(IMPPUTIONADD, PUBKEY4, tokenType, "1002");
        List<Map> recycleList62 = utilsClass.constructToken(MULITADD4, PUBKEY1, tokenType2, "1", recycleList52);

        String response32 = multiSign.RecyclesLocalSign(recycleList62);
//        log.info(response32);
        assertThat(response32, containsString("insufficient balance"));


    }

}
