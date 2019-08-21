package com.tjfintech.common.functionTest.mainSubChain;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.MgToolCmd;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.SubLedgerCmd;
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

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class TestMainSubChain_UTXO {
    TestBuilder testBuilder = TestBuilder.getInstance();
    private static String tokenType;
    private static String tokenType2;
    private static String subLedgerA = "leg3";
    private static String subLedgerB = "leg4";
    MultiSign multiSign = testBuilder.getMultiSign();
    UtilsClass utilsClass = new UtilsClass();
    MgToolCmd mgToolCmd = new MgToolCmd();
    BeforeCondition beforeCondition = new BeforeCondition();

    @BeforeClass
    public static void clearData() throws Exception{
        BeforeCondition beforeCondition = new BeforeCondition();
        beforeCondition.clearDataSetPerm999();
        beforeCondition.collAddressTest();
        beforeCondition.createAdd();
        sleepAndSaveInfo(SLEEPTIME);
        bReg = false;
    }

    @Before
    public void beforeConfig() throws Exception {

        String resp = mgToolCmd.getSubChain(PEER1IP, PEER1RPCPort, ""); //获取子链的信息

        if (!resp.contains("\"name\": \"" + subLedgerA + "\"")) {//如果子链中不包含subLedgerA就新建一条子链
            mgToolCmd.createSubChain(PEER1IP, PEER1RPCPort, " -z " + subLedgerA, " -t sm3", " -w subA", " -c raft", ids);
            Thread.sleep(SLEEPTIME * 2);
            assertEquals(mgToolCmd.getSubChain(PEER1IP, PEER1RPCPort, "").contains("\"name\": \"" + subLedgerA + "\""), true);
        }

        if (!resp.contains("\"name\": \"" + subLedgerB + "\"")) {//如果子链中不包含subLedgerB就新建一条子链
            mgToolCmd.createSubChain(PEER1IP, PEER1RPCPort, " -z " + subLedgerB, " -t sm3", " -w subA", " -c raft", ids);
            Thread.sleep(SLEEPTIME * 2);
            assertEquals(mgToolCmd.getSubChain(PEER1IP, PEER1RPCPort, "").contains("\"name\": \"" + subLedgerB + "\""), true);
        }
    }

    /**
     * 在子链上进行UTXO交易发行，转账，回收，冻结,在主链上进行交易查询
     *
     * @throws Exception
     */

    @Test
    public void TC1529_UTXOTranction() throws Exception {
        subLedger = subLedgerA;
        log.info(subLedger);
        beforeCondition.collAddressTest();
        sleepAndSaveInfo(SLEEPTIME,"添加归集地址和发行地址到子链");
        token_issue();

        subLedger = "";
        beforeCondition.setPermission999();
        log.info("主链查询子链上归集地址中两种token余额");
        String response3 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        String response4= multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType2);
        assertEquals("200", JSONObject.fromObject(response3).getString("State"));
        assertEquals("0", JSONObject.fromObject(response3).getJSONObject("Data").getString("Total"));
        assertEquals("200", JSONObject.fromObject(response4).getString("State"));
        assertEquals("0", JSONObject.fromObject(response4).getJSONObject("Data").getString("Total"));

        subLedger = subLedgerA;
        Transfer();

        subLedger = "";
        beforeCondition.setPermission999();
        log.info("查询归集地址跟MULITADD4余额，判断转账是否成功");
        String queryInfo5 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        String queryInfo6 = multiSign.Balance(MULITADD4, PRIKEY1, tokenType);
        assertEquals("200", JSONObject.fromObject(queryInfo5).getString("State"));
        assertEquals("0", JSONObject.fromObject(queryInfo5).getJSONObject("Data").getString("Total"));
        assertEquals("200", JSONObject.fromObject(queryInfo6).getString("State"));
        assertEquals("0", JSONObject.fromObject(queryInfo6).getJSONObject("Data").getString("Total"));

        subLedger = subLedgerA;
        Recovery_query_freeze();

    }

    /**
     * 在主链上进行UTXO交易发行，转账，回收，冻结,在子链上进行交易查询
     * @throws Exception
     */
    @Test
    public void TC1532_UTXOTranction()throws Exception{
        subLedger = "";
        log.info(subLedger);
        beforeCondition.collAddressTest();
        sleepAndSaveInfo(SLEEPTIME,"添加归集地址和发行地址到子链");

        token_issue();

        subLedger = subLedgerA;
        beforeCondition.setPermission999();
        log.info("主链查询子链上归集地址中两种token余额");
        String response3 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        String response4= multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType2);
        assertEquals("200", JSONObject.fromObject(response3).getString("State"));
        assertEquals("0", JSONObject.fromObject(response3).getJSONObject("Data").getString("Total"));
        assertEquals("200", JSONObject.fromObject(response4).getString("State"));
        assertEquals("0", JSONObject.fromObject(response4).getJSONObject("Data").getString("Total"));

        subLedger = "";
        Transfer();

        subLedger =subLedgerA;
        beforeCondition.setPermission999();
        log.info("查询归集地址跟MULITADD4余额，判断转账是否成功");
        String queryInfo5 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        String queryInfo6 = multiSign.Balance(MULITADD4, PRIKEY1, tokenType);
        assertEquals("200", JSONObject.fromObject(queryInfo5).getString("State"));
        assertEquals("0", JSONObject.fromObject(queryInfo5).getJSONObject("Data").getString("Total"));
        assertEquals("200", JSONObject.fromObject(queryInfo6).getString("State"));
        assertEquals("0", JSONObject.fromObject(queryInfo6).getJSONObject("Data").getString("Total"));

        subLedger = "";
        Recovery_query_freeze();


    }

    /**
     * 在子链A进行UTXO交易发行，转账，回收，冻结,在子链B上进行交易查询
     * @throws Exception
     */
    @Test
    public void TC1526_UTXOTranction()throws Exception{
        subLedger = subLedgerA;
        log.info(subLedger);
        beforeCondition.collAddressTest();
        sleepAndSaveInfo(SLEEPTIME,"添加归集地址和发行地址到子链");
        token_issue();

        subLedger = subLedgerB;
        beforeCondition.setPermission999();
        log.info("主链查询子链上归集地址中两种token余额");
        String response3 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        String response4= multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType2);
        assertEquals("200", JSONObject.fromObject(response3).getString("State"));
        assertEquals("0", JSONObject.fromObject(response3).getJSONObject("Data").getString("Total"));
        assertEquals("200", JSONObject.fromObject(response4).getString("State"));
        assertEquals("0", JSONObject.fromObject(response4).getJSONObject("Data").getString("Total"));

        subLedger = subLedgerA;
        Transfer();

        subLedger =subLedgerB;
        beforeCondition.setPermission999();
        log.info("查询归集地址跟MULITADD4余额，判断转账是否成功");
        String queryInfo5 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        String queryInfo6 = multiSign.Balance(MULITADD4, PRIKEY1, tokenType);
        assertEquals("200", JSONObject.fromObject(queryInfo5).getString("State"));
        assertEquals("0", JSONObject.fromObject(queryInfo5).getJSONObject("Data").getString("Total"));
        assertEquals("200", JSONObject.fromObject(queryInfo6).getString("State"));
        assertEquals("0", JSONObject.fromObject(queryInfo6).getJSONObject("Data").getString("Total"));

        subLedger = subLedgerA;
        Recovery_query_freeze();



    }


    /**
     * token发行
     */
    public void token_issue()throws Exception{

        //多签的token发行
        log.info("发行两种token1000个");
        //两次发行之间不可以有sleep时间
        tokenType = IssueToken(5, "1000");
        tokenType2 = IssueToken(6, "1000");
        Thread.sleep(SLEEPTIME*2);

        log.info("查询归集地址中两种token余额");
        String response1 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        String response2 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType2);
        assertEquals("200", JSONObject.fromObject(response1).getString("State"));
        assertEquals("1000", JSONObject.fromObject(response1).getJSONObject("Data").getString("Total"));
        assertEquals("200", JSONObject.fromObject(response2).getString("State"));
        assertEquals("1000", JSONObject.fromObject(response2).getJSONObject("Data").getString("Total"));

    }

    /**
     * 回收 查询 冻结操作
     */
    public void Recovery_query_freeze()throws Exception{
        log.info("回收归集地址跟MULITADD4的新发token");
        String recycleInfo = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType, "990");
        String recycleInfo2 = multiSign.Recycle(MULITADD4, PRIKEY1, tokenType, "10");
        assertEquals("200", JSONObject.fromObject(recycleInfo).getString("State"));
        assertEquals("200", JSONObject.fromObject(recycleInfo2).getString("State"));
        Thread.sleep(SLEEPTIME);

        log.info("查询回收后账户余额是否为0");
        String queryInfo3 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        String queryInfo4 = multiSign.Balance(MULITADD4, PRIKEY1, tokenType);
        assertEquals("200", JSONObject.fromObject(queryInfo3).getString("State"));
        assertEquals("0", JSONObject.fromObject(queryInfo3).getJSONObject("Data").getString("Total"));
        assertEquals("200", JSONObject.fromObject(queryInfo4).getString("State"));
        assertEquals("0", JSONObject.fromObject(queryInfo4).getJSONObject("Data").getString("Total"));

        log.info("冻结token");
        String freezeToken = multiSign.freezeToken(PRIKEY4, tokenType);
        String freezeToken2 = multiSign.freezeToken(PRIKEY4, tokenType2);
        assertThat(freezeToken, containsString("200"));
        assertThat(freezeToken2, containsString("200"));

    }

    /**
     * 转账操作
     * @throws Exception
     */

    public void Transfer()throws Exception{
        //转账
        String transferData = "归集地址向" + MULITADD4 + "转账10个" + tokenType;
        log.info(transferData);
        List<Map> list = utilsClass.constructToken(MULITADD4, tokenType, "10");
        log.info(transferData);
        String transferInfo = multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD, list);
        Thread.sleep(SLEEPTIME);
        assertThat(transferInfo, containsString("200"));

        log.info("查询归集地址跟MULITADD4余额，判断转账是否成功");
        String queryInfo = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        String queryInfo2 = multiSign.Balance(MULITADD4, PRIKEY1, tokenType);
        assertEquals("200", JSONObject.fromObject(queryInfo).getString("State"));
        assertEquals("990", JSONObject.fromObject(queryInfo).getJSONObject("Data").getString("Total"));
        assertEquals("200", JSONObject.fromObject(queryInfo2).getString("State"));
        assertEquals("10", JSONObject.fromObject(queryInfo2).getJSONObject("Data").getString("Total"));
    }

    public String IssueToken(int length, String amount) {
        String tokenType = "-" + UtilsClass.Random(length);
        log.info(IMPPUTIONADD + "发行" + tokenType + " token，数量为：" + amount);
        String data = "IMPPUTIONADD" + "发行" + tokenType + " token，数量为：" + amount;
        String response = multiSign.issueToken(IMPPUTIONADD, tokenType, amount, data);
        assertThat(response, containsString("200"));
        String Tx1 = JSONObject.fromObject(response).getJSONObject("Data").getString("Tx");
        log.info("第一次签名");
        String response2 = multiSign.Sign(Tx1, PRIKEY5);
        assertThat(response2, containsString("200"));
        return tokenType;
    }

}
