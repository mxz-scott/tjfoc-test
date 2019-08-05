package com.tjfintech.common.functionTest.mainSubChain;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.functionTest.mainSubChain.TestMainSubChain;
import com.tjfintech.common.functionTest.mixTestWithConfigChange.TestMgTool;
import com.tjfintech.common.utils.Shell;
import com.tjfintech.common.utils.UtilsClass;

import com.tjfoc.base.SingleSignIssue;

import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClass.USERNAME;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class TestMainSubChain_UTXO {
    TestBuilder testBuilder = TestBuilder.getInstance();
    private static String tokenType;
    private static String tokenType2;
    private static String sdkid = "a05b1c716e7dc89dc1a3d947b08596a175f1091d510f88af9302f8729e75c47dc7490c7d2ee4315478830d9afedb0324386025c3af8d2082f428171be2f6fa3c";
    private static String subLedgerA = "leg1";
    MultiSign multiSign = testBuilder.getMultiSign();
    UtilsClass utilsClass = new UtilsClass();
    TestMgTool testMgTool = new TestMgTool();
    TestMainSubChain testMainSubChain = new TestMainSubChain();



    String id1 = getPeerId(PEER1IP, USERNAME, PASSWD);
    String id2 = getPeerId(PEER2IP, USERNAME, PASSWD);
    String id3 = getPeerId(PEER4IP, USERNAME, PASSWD);
    String ids = " -m " + id1 + "," + id2 + "," + id3;


    @Before
    public void beforeConfig() throws Exception {

        String resp = testMainSubChain.getSubChain(PEER1IP, PEER1RPCPort, ""); //获取子链的信息

        if (!resp.contains("\"name\": \"" + subLedgerA + "\"")) {//如果子链中不包含subLedgerA就新建一条子链
            testMainSubChain.createSubChain(PEER1IP, PEER1RPCPort, " -z " + subLedgerA, " -t sm3", " -w subA", " -c raft", ids);
            Thread.sleep(SLEEPTIME * 2);
            assertEquals(testMainSubChain.getSubChain(PEER1IP, PEER1RPCPort, "").contains("\"name\": \"" + subLedgerA + "\""), true);
        }

    }

    /**
     * 在子链上进行多签的发行，转账，回收，冻结
     *
     * @throws Exception
     */
    @Test
    public void TC1526_UTXOTranction_leg() throws Exception {
        subLedger = subLedgerA;
        testMgTool.setPeerPerm(PEER1IP + ":" + PEER1RPCPort, sdkid, "999"); //给sdk的id赋权限 ================sdkid
//        testMainSubChain.getSubChain(PEER1IP,PEER1RPCPort,""); //获取子链的信息
        //生成地址
        if (certPath != "" && bReg == false) {
            BeforeCondition bf = new BeforeCondition();
            bf.updatePubPriKey();
            bf.collAddressTest();
            Thread.sleep(SLEEPTIME * 1);
            bReg = true;
        }

        Thread.sleep(SLEEPTIME);

        //多签的token发行
        log.info("发行两种token1000个");
        //两次发行之前不可以有sleep时间
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
        String freezeToken2 = multiSign.freezeToken(PRIKEY4, tokenType);
        assertThat(freezeToken, containsString("200"));
        assertThat(freezeToken2, containsString("200"));



    }

    @Test
    public void TC1526_UTXOTranction() throws Exception {
        //在主链上查询余额
        subLedger = "";
        testMgTool.setPeerPerm(PEER1IP + ":" + PEER1RPCPort, sdkid, "999"); //给sdk的id赋权限 ================sdkid

        //生成地址
        if (certPath != "" && bReg == false) {
            BeforeCondition bf = new BeforeCondition();
            bf.updatePubPriKey();
            bf.collAddressTest();
            Thread.sleep(SLEEPTIME * 1);
            bReg = true;
        }
        Thread.sleep(SLEEPTIME);

        log.info("主链查询子链上归集地址中两种token余额");
        String response1 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        String response2 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType2);
        assertEquals("200", JSONObject.fromObject(response1).getString("State"));
        assertEquals("0", JSONObject.fromObject(response1).getJSONObject("Data").getString("Total"));
        assertEquals("200", JSONObject.fromObject(response2).getString("State"));
        assertEquals("0", JSONObject.fromObject(response2).getJSONObject("Data").getString("Total"));


        //转账
        String transferData = "归集地址向" + MULITADD4 + "转账10个" + tokenType;
        log.info(transferData);
        List<Map> list = utilsClass.constructToken(MULITADD4, tokenType, "10");
        log.info(transferData);
        String transferInfo = multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD, list);
        Thread.sleep(SLEEPTIME);
        assertThat(transferInfo, containsString("400"));

        log.info("查询归集地址跟MULITADD4余额，判断转账是否成功");
        String queryInfo = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        String queryInfo2 = multiSign.Balance(MULITADD4, PRIKEY1, tokenType);
        assertEquals("200", JSONObject.fromObject(queryInfo).getString("State"));
        assertEquals("0", JSONObject.fromObject(queryInfo).getJSONObject("Data").getString("Total"));
        assertEquals("200", JSONObject.fromObject(queryInfo2).getString("State"));
        assertEquals("0", JSONObject.fromObject(queryInfo2).getJSONObject("Data").getString("Total"));

        log.info("回收归集地址跟MULITADD4的新发token");
        String recycleInfo = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType, "990");
        String recycleInfo2 = multiSign.Recycle(MULITADD4, PRIKEY1, tokenType, "10");
        assertEquals("400", JSONObject.fromObject(recycleInfo).getString("State"));
        assertEquals("400", JSONObject.fromObject(recycleInfo2).getString("State"));
        Thread.sleep(SLEEPTIME);


    }


    public String IssueToken(int length, String amount) {
        multiSign.addissueaddress(IMPPUTIONADD,PUBKEY4);
        multiSign.collAddress(IMPPUTIONADD,PUBKEY4);
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
