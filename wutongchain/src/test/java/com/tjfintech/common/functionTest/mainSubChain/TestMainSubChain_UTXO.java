package com.tjfintech.common.functionTest.mainSubChain;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CommonFunc;
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

import javax.smartcardio.CommandAPDU;
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
    private static String subLedgerA = "Leg3";
    private static String subLedgerB = "Leg4";
    MultiSign multiSign = testBuilder.getMultiSign();
    UtilsClass utilsClass = new UtilsClass();
    MgToolCmd mgToolCmd = new MgToolCmd();
    BeforeCondition beforeCondition = new BeforeCondition();
    CommonFunc commonFunc = new CommonFunc();

    String id1 = getPeerId(PEER1IP,USERNAME,PASSWD);
    String id2 = getPeerId(PEER2IP,USERNAME,PASSWD);
    String id3 = getPeerId(PEER4IP,USERNAME,PASSWD);
    String ids = " -m "+ id1+","+ id2+","+ id3;

    @BeforeClass
    public static void clearData() throws Exception{
        BeforeCondition beforeCondition = new BeforeCondition();
//        beforeCondition.clearDataSetPerm999();
        beforeCondition.updatePubPriKey();
        beforeCondition.collAddressTest();
        sleepAndSaveInfo(SLEEPTIME);
    }

    @Before
    public void beforeConfig() throws Exception {

        String resp = mgToolCmd.getSubChain(PEER1IP, PEER1RPCPort, ""); //获取子链的信息

        if (!resp.contains("\"name\": \"" + subLedgerA.toLowerCase() + "\"")) {//如果子链中不包含subLedgerA就新建一条子链
            String respCreate = mgToolCmd.createSubChain(PEER1IP, PEER1RPCPort, " -z " + subLedgerA, " -t sm3", " -w subA", " -c raft", ids);

            commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(respCreate,utilsClass.mgGetTxHashType),
                    utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME*2);

            assertEquals(mgToolCmd.getSubChain(PEER1IP, PEER1RPCPort, "").contains("\"name\": \"" + subLedgerA.toLowerCase() + "\""), true);
        }

        if (!resp.contains("\"name\": \"" + subLedgerB.toLowerCase() + "\"")) {//如果子链中不包含subLedgerB就新建一条子链
            String respCreate = mgToolCmd.createSubChain(PEER1IP, PEER1RPCPort, " -z " + subLedgerB, " -t sm3", " -w subA", " -c raft", ids);

            commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(respCreate,utilsClass.mgGetTxHashType),
                    utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME*2);

            assertEquals(true,mgToolCmd.getSubChain(PEER1IP, PEER1RPCPort, "").contains("\"name\": \"" + subLedgerB.toLowerCase() + "\""));
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
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType21),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        token_issue();

        subLedger = "";
        beforeCondition.setPermission999();
        log.info("主链查询子链上归集地址中两种token余额");
        String response3 = multiSign.BalanceByAddr(IMPPUTIONADD, tokenType);
        String response4= multiSign.BalanceByAddr(IMPPUTIONADD, tokenType2);
        assertEquals("200", JSONObject.fromObject(response3).getString("state"));
        assertEquals("0", JSONObject.fromObject(response3).getJSONObject("data").getString("total"));
        assertEquals("200", JSONObject.fromObject(response4).getString("state"));
        assertEquals("0", JSONObject.fromObject(response4).getJSONObject("data").getString("total"));

        subLedger = subLedgerA;
        Transfer();

        subLedger = "";
        beforeCondition.setPermission999();
        log.info("查询归集地址跟MULITADD4余额，判断转账是否成功");
        String queryInfo5 = multiSign.BalanceByAddr(IMPPUTIONADD, tokenType);
        String queryInfo6 = multiSign.BalanceByAddr(MULITADD4, tokenType);
        assertEquals("200", JSONObject.fromObject(queryInfo5).getString("state"));
        assertEquals("0", JSONObject.fromObject(queryInfo5).getJSONObject("data").getString("total"));
        assertEquals("200", JSONObject.fromObject(queryInfo6).getString("state"));
        assertEquals("0", JSONObject.fromObject(queryInfo6).getJSONObject("data").getString("total"));

        log.info("主链上发行与子链A相同tokentype");
        String issueResp = multiSign.issueTokenCarryPri(IMPPUTIONADD,tokenType,"13",PRIKEY4,"主链上发行" + tokenType);
        assertEquals("200", JSONObject.fromObject(issueResp).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType02),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        String queryInfoM5 = multiSign.BalanceByAddr(IMPPUTIONADD, tokenType);
        assertEquals("200", JSONObject.fromObject(queryInfoM5).getString("state"));
        assertEquals("13", JSONObject.fromObject(queryInfoM5).getJSONObject("data").getString("total"));

        log.info("子链上冻结tokentype");
        subLedger = subLedgerA;
        Recovery_query_freeze();

        log.info("主链上进行tokentype转账，测试子链冻结相同tokenType对主链无影响");
        subLedger = "";
        List<Map>list=utilsClass.constructToken(MULITADD4,tokenType,"5");
        String transferInfo= multiSign.Transfer(PRIKEY4, "主链上转账", IMPPUTIONADD,list);//相同币种
        assertEquals("200", JSONObject.fromObject(transferInfo).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType02),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        log.info("主链转账后查询余额");

        String queryInfoM6 = multiSign.BalanceByAddr(IMPPUTIONADD, tokenType);
        assertEquals("200", JSONObject.fromObject(queryInfoM6).getString("state"));
        assertEquals("8", JSONObject.fromObject(queryInfoM6).getJSONObject("data").getString("total"));

        String queryInfoM7 = multiSign.BalanceByAddr(MULITADD4, tokenType);
        assertEquals("200", JSONObject.fromObject(queryInfoM7).getString("state"));
        assertEquals("5", JSONObject.fromObject(queryInfoM7).getJSONObject("data").getString("total"));

        log.info("查询主链地址账户余额");
        String zero=  multiSign.QueryZero(tokenType);
        String zero2= multiSign.QueryZero(tokenType2);
        assertEquals("200",JSONObject.fromObject(zero).getString("state"));
        assertEquals("0",JSONObject.fromObject(zero).getJSONObject("data").getString("total"));
        assertEquals("200",JSONObject.fromObject(zero2).getString("state"));
        assertEquals("0",JSONObject.fromObject(zero2).getJSONObject("data").getString("total"));

        log.info("查询子链A上的零地址账户余额");
        subLedger = subLedgerA;
        String zeroA=  multiSign.QueryZero(tokenType);
        String zeroA2= multiSign.QueryZero(tokenType2);
        assertEquals("200",JSONObject.fromObject(zeroA).getString("state"));
        assertEquals("1000",JSONObject.fromObject(zeroA).getJSONObject("data").getJSONObject("detail").getString(tokenType));
        assertEquals("200",JSONObject.fromObject(zeroA2).getString("state"));
        assertEquals("0",JSONObject.fromObject(zero2).getJSONObject("data").getString("total"));
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
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType21),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        token_issue();

        subLedger = subLedgerA;
        beforeCondition.setPermission999();
        log.info("主链查询子链上归集地址中两种token余额");
        String response3 = multiSign.BalanceByAddr(IMPPUTIONADD, tokenType);
        String response4= multiSign.BalanceByAddr(IMPPUTIONADD, tokenType2);
        assertEquals("200", JSONObject.fromObject(response3).getString("state"));
        assertEquals("0", JSONObject.fromObject(response3).getJSONObject("data").getString("total"));
        assertEquals("200", JSONObject.fromObject(response4).getString("state"));
        assertEquals("0", JSONObject.fromObject(response4).getJSONObject("data").getString("total"));

        subLedger = "";
        Transfer();

        subLedger =subLedgerA;
        beforeCondition.setPermission999();
        log.info("查询归集地址跟MULITADD4余额，判断转账是否成功");
        String queryInfo5 = multiSign.BalanceByAddr(IMPPUTIONADD, tokenType);
        String queryInfo6 = multiSign.BalanceByAddr(MULITADD4, tokenType);
        assertEquals("200", JSONObject.fromObject(queryInfo5).getString("state"));
        assertEquals("0", JSONObject.fromObject(queryInfo5).getJSONObject("data").getString("total"));
        assertEquals("200", JSONObject.fromObject(queryInfo6).getString("state"));
        assertEquals("0", JSONObject.fromObject(queryInfo6).getJSONObject("data").getString("total"));


        log.info("子链A上发行与主链相同tokentype");
        subLedger = subLedgerA;
        beforeCondition.collAddressTest();
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType21),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        String issueResp = multiSign.issueTokenCarryPri(IMPPUTIONADD,tokenType,"13",PRIKEY4,"主链上发行" + tokenType);
        assertEquals("200", JSONObject.fromObject(issueResp).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType02),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        String queryInfoM5 = multiSign.BalanceByAddr(IMPPUTIONADD, tokenType);
        assertEquals("200", JSONObject.fromObject(queryInfoM5).getString("state"));
        assertEquals("13", JSONObject.fromObject(queryInfoM5).getJSONObject("data").getString("total"));

        subLedger = "";
        Recovery_query_freeze();


        log.info("子链A上进行tokentype转账，测试主链冻结相同tokenType对子链无影响");
        subLedger = subLedgerA;
        List<Map>list=utilsClass.constructToken(MULITADD4,tokenType,"5");
        String transferInfo= multiSign.Transfer(PRIKEY4, "主链上转账", IMPPUTIONADD,list);//相同币种
        assertEquals("200", JSONObject.fromObject(transferInfo).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType02),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        log.info("子链A转账后查询余额");

        String queryInfoM6 = multiSign.BalanceByAddr(IMPPUTIONADD, tokenType);
        assertEquals("200", JSONObject.fromObject(queryInfoM6).getString("state"));
        assertEquals("8", JSONObject.fromObject(queryInfoM6).getJSONObject("data").getString("total"));

        String queryInfoM7 = multiSign.BalanceByAddr(MULITADD4, tokenType);
        assertEquals("200", JSONObject.fromObject(queryInfoM7).getString("state"));
        assertEquals("5", JSONObject.fromObject(queryInfoM7).getJSONObject("data").getString("total"));

        log.info("查询子链地址账户余额");
        String zero=  multiSign.QueryZero(tokenType);
        String zero2= multiSign.QueryZero(tokenType2);
        assertEquals("200",JSONObject.fromObject(zero).getString("state"));
        assertEquals("0",JSONObject.fromObject(zero).getJSONObject("data").getString("total"));
        assertEquals("200",JSONObject.fromObject(zero2).getString("state"));
        assertEquals("0",JSONObject.fromObject(zero2).getJSONObject("data").getString("total"));

        log.info("查询主链上的零地址账户余额");
        subLedger = "";
        String zeroA=  multiSign.QueryZero(tokenType);
        String zeroA2= multiSign.QueryZero(tokenType2);
        assertEquals("200",JSONObject.fromObject(zeroA).getString("state"));
        assertEquals("1000",JSONObject.fromObject(zeroA).getJSONObject("data").getJSONObject("detail").getString(tokenType));
        assertEquals("200",JSONObject.fromObject(zeroA2).getString("state"));
        assertEquals("0",JSONObject.fromObject(zero2).getJSONObject("data").getString("total"));


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
        String response3 = multiSign.BalanceByAddr(IMPPUTIONADD, tokenType);
        String response4= multiSign.BalanceByAddr(IMPPUTIONADD, tokenType2);
        assertEquals("200", JSONObject.fromObject(response3).getString("state"));
        assertEquals("0", JSONObject.fromObject(response3).getJSONObject("data").getString("total"));
        assertEquals("200", JSONObject.fromObject(response4).getString("state"));
        assertEquals("0", JSONObject.fromObject(response4).getJSONObject("data").getString("total"));

        subLedger = subLedgerA;
        Transfer();

        subLedger =subLedgerB;
        beforeCondition.setPermission999();
        log.info("查询归集地址跟MULITADD4余额，判断转账是否成功");
        String queryInfo5 = multiSign.BalanceByAddr(IMPPUTIONADD, tokenType);
        String queryInfo6 = multiSign.BalanceByAddr(MULITADD4, tokenType);
        assertEquals("200", JSONObject.fromObject(queryInfo5).getString("state"));
        assertEquals("0", JSONObject.fromObject(queryInfo5).getJSONObject("data").getString("total"));
        assertEquals("200", JSONObject.fromObject(queryInfo6).getString("state"));
        assertEquals("0", JSONObject.fromObject(queryInfo6).getJSONObject("data").getString("total"));

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
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME*2);

        log.info("查询归集地址中两种token余额");
        String response1 = multiSign.BalanceByAddr(IMPPUTIONADD, tokenType);
        String response2 = multiSign.BalanceByAddr(IMPPUTIONADD, tokenType2);
        assertEquals("200", JSONObject.fromObject(response1).getString("state"));
        assertEquals("1000", JSONObject.fromObject(response1).getJSONObject("data").getString("total"));
        assertEquals("200", JSONObject.fromObject(response2).getString("state"));
        assertEquals("1000", JSONObject.fromObject(response2).getJSONObject("data").getString("total"));

    }

    /**
     * 回收 查询 冻结操作
     */
    public void Recovery_query_freeze()throws Exception{
        log.info("回收归集地址跟MULITADD4的新发token");
        String recycleInfo = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType, "990");
        String recycleInfo2 = multiSign.Recycle(MULITADD4, PRIKEY1, tokenType, "10");
        assertEquals("200", JSONObject.fromObject(recycleInfo).getString("state"));
        assertEquals("200", JSONObject.fromObject(recycleInfo2).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        log.info("查询回收后账户余额是否为0");
        String queryInfo3 = multiSign.BalanceByAddr(IMPPUTIONADD, tokenType);
        String queryInfo4 = multiSign.BalanceByAddr(MULITADD4, tokenType);
        assertEquals("200", JSONObject.fromObject(queryInfo3).getString("state"));
        assertEquals("0", JSONObject.fromObject(queryInfo3).getJSONObject("data").getString("total"));
        assertEquals("200", JSONObject.fromObject(queryInfo4).getString("state"));
        assertEquals("0", JSONObject.fromObject(queryInfo4).getJSONObject("data").getString("total"));

        log.info("冻结token");
        String freezeToken = multiSign.freezeToken( tokenType);
        String freezeToken2 = multiSign.freezeToken( tokenType2);
        assertThat(freezeToken, containsString("200"));
        assertThat(freezeToken2, containsString("200"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType21),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

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
        assertThat(transferInfo, containsString("200"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType02),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        log.info("查询归集地址跟MULITADD4余额，判断转账是否成功");
        String queryInfo = multiSign.BalanceByAddr(IMPPUTIONADD, tokenType);
        String queryInfo2 = multiSign.BalanceByAddr(MULITADD4, tokenType);
        assertEquals("200", JSONObject.fromObject(queryInfo).getString("state"));
        assertEquals("990", JSONObject.fromObject(queryInfo).getJSONObject("data").getString("total"));
        assertEquals("200", JSONObject.fromObject(queryInfo2).getString("state"));
        assertEquals("10", JSONObject.fromObject(queryInfo2).getJSONObject("data").getString("total"));
    }

    public String IssueToken(int length, String amount) {
        String tokenType = "-" + UtilsClass.Random(length);
        log.info(IMPPUTIONADD + "发行" + tokenType + " token，数量为：" + amount);
        String data = "IMPPUTIONADD" + "发行" + tokenType + " token，数量为：" + amount;
        String response = multiSign.issueToken(IMPPUTIONADD, tokenType, amount, data);
        assertThat(response, containsString("200"));
        String Tx1 = JSONObject.fromObject(response).getJSONObject("data").getString("tx");
        log.info("第一次签名");
        String response2 = multiSign.Sign(Tx1, PRIKEY5);
        assertThat(response2, containsString("200"));
        return tokenType;
    }

}
