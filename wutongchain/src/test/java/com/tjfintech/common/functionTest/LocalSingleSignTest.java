package com.tjfintech.common.functionTest;

import com.bw.base.SingleSignIssue;
import com.bw.base.SingleSignTransferAccounts;
import com.bw.base.MultiSignTransferAccounts;
import com.google.protobuf.ByteString;
import com.sun.corba.se.impl.oa.toa.TOA;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
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

import static com.tjfintech.common.functionTest.StoreTest.SHORTSLEEPTIME;
import static com.tjfintech.common.functionTest.StoreTest.SLEEPTIME;
import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LocalSingleSignTest {
    TestBuilder testBuilder = TestBuilder.getInstance();
    MultiSign multiSign = testBuilder.getMultiSign();
    SoloSign soloSign = testBuilder.getSoloSign();
    UtilsClass utilsClass = new UtilsClass();
    LocalMultiSignTest multiSignTest = new LocalMultiSignTest();
    private static String tokenType;
    private static String tokenType2;
    SingleSignIssue singleSign = new SingleSignIssue();
    MultiSignTransferAccounts multiTrans = new MultiSignTransferAccounts();
    SingleSignTransferAccounts singleTrans = new SingleSignTransferAccounts();

    @Before
    public void beforeConfig() throws Exception {

        //单签发行
        log.info("发行两种token");
        tokenType = issueTokenLocalSign(7, "10000.123456789");
        tokenType2 = issueTokenLocalSign(8, "20000.87654321");

        //查询余额
        Thread.sleep(SLEEPTIME);
        log.info("查询归集地址中token余额");
        String balance = multiSign.BalanceByAddr(IMPPUTIONADD, tokenType);
        String balance2 = multiSign.BalanceByAddr(IMPPUTIONADD, tokenType2);
        assertThat(tokenType + "查询余额错误", balance, containsString("200"));
        assertThat(tokenType + "查询余额不正确", balance, containsString("10000.123456789"));
        assertThat(tokenType2 + "查询余额错误", balance2, containsString("200"));
        assertThat(tokenType2 + "查询余额不正确", balance2, containsString("20000.87654321"));

    }


    /**
     * Tc024单签正常流程:
     */
    @Test
    public void TC024_SoloProgress_LocalSign() throws Exception {

        //多签转单签
        String transferData = "归集地址向ADDRESS3转账100.25个" + tokenType + "，并向ADDRESS5转账200.555个" + tokenType2;

        log.info(transferData);
        List<Map> transferList = utilsClass.constructToken(ADDRESS3, tokenType, "100.25");
        List<Map> transferList2 = utilsClass.constructToken(ADDRESS5, tokenType2, "200.555", transferList);
        multiSignTest.multiSignTransfer_LocalSign(IMPPUTIONADD, PUBKEY4, transferData, transferList2, PRIKEY4PATH);

        Thread.sleep(SLEEPTIME);

        log.info("查询ADDRESS3和ADDRESS5余额");
        String queryInfo = multiSign.BalanceByAddr(ADDRESS3, tokenType);
        assertThat(queryInfo, containsString("200"));
        assertThat(queryInfo, containsString("\"Total\":\"100.25\""));

        String queryInfo3 = multiSign.BalanceByAddr(ADDRESS5, tokenType2);
        assertThat(queryInfo3, containsString("200"));
        assertThat(queryInfo3, containsString("\"Total\":\"200.555\""));

        //查询归集地址
        String queryInfo2 = multiSign.BalanceByAddr(IMPPUTIONADD, tokenType);
        assertThat(queryInfo2, containsString("200"));
        assertThat(queryInfo2, containsString("\"Total\":\"9899.873456789\""));

        String queryInfo4 = multiSign.BalanceByAddr(IMPPUTIONADD, tokenType2);
        assertThat(queryInfo4, containsString("200"));
        assertThat(queryInfo4, containsString("\"Total\":\"19800.32154321\""));


        String data1 = "ADDRESS3向ADDRESS4转账30个" + tokenType;
        log.info(data1);
        List<Map> list1 = soloSign.constructToken(ADDRESS4, tokenType, "30");
        String res1 = singleSignTransfer_LocalSign(PUBKEY3, data1, list1, PRIKEY3PATH);
        log.info(res1);
        assertThat(res1, containsString("200"));
        Thread.sleep(SLEEPTIME);

        String data2 = "ADDRESS5向ADDRESS4转账80个" + tokenType2;
        log.info(data2);
        List<Map> list2 = soloSign.constructToken(ADDRESS4, tokenType2, "80");
        String res2 = singleSignTransfer_LocalSign(PUBKEY5, data2, list2, PRIKEY5PATH);
        log.info(res2);
        assertThat(res2, containsString("200"));
        Thread.sleep(SLEEPTIME);

        log.info("查询地址4余额");
        String queryInfo21 = multiSign.BalanceByAddr(ADDRESS4, tokenType);
        assertThat(queryInfo21, containsString("200"));
        assertThat(queryInfo21, containsString("\"Total\":\"30\""));

        String queryInfo22 = multiSign.BalanceByAddr(ADDRESS4, tokenType2);
        assertThat(queryInfo22, containsString("200"));
        assertThat(queryInfo22, containsString("\"Total\":\"80\""));

        String data3 = "ADDRESS4向ADDRESS2转账30个" + tokenType + "，转账70个" + tokenType2;
        log.info(data3);
        List<Map> list3 = soloSign.constructToken(ADDRESS2, tokenType, "30");
        List<Map> list4 = soloSign.constructToken(ADDRESS2, tokenType2, "70", list3);
        String res3 = singleSignTransfer_LocalSign(PUBKEY4, data3, list4, PRIKEY4PATH);
        assertThat(res3, containsString("200"));
        Thread.sleep(SLEEPTIME);

        String data4 = "ADDRESS5向ADDRESS2转账20个" + tokenType2;
        log.info(data4);
        List<Map> list5 = soloSign.constructToken(ADDRESS2, tokenType2, "20");
        String res4 = singleSignTransfer_LocalSign(PUBKEY5, data4, list5, PRIKEY5PATH);
        assertThat(res4, containsString("200"));
        Thread.sleep(SLEEPTIME);

        String data5 = "ADDRESS2向ADDRESS4转账80个" + tokenType2;
        log.info(data5);
        List<Map> list6 = soloSign.constructToken(ADDRESS4, tokenType2, "30");
        List<Map> list7 = soloSign.constructToken(ADDRESS4, tokenType2, "50", list6);
        String res5 = singleSignTransfer_LocalSign(PUBKEY2, data5, list7, PRIKEY2PATH);
        assertThat(res5, containsString("200"));
        Thread.sleep(SLEEPTIME);


        log.info("查询ADDRESS3余额");
        String queryInfo3TK1 = multiSign.BalanceByAddr(ADDRESS3, tokenType);
        assertThat(queryInfo3TK1, containsString("70.25"));

        String Info1 = singleSignRecycle_LocalSign(PUBKEY3, tokenType, "70.25", PRIKEY3PATH);
        assertThat(Info1, containsString("200"));
        Thread.sleep(SLEEPTIME);

        String balanceInfo3TK1 = multiSign.BalanceByAddr(ADDRESS3, tokenType);
        assertThat(balanceInfo3TK1, containsString("\"Total\":\"0\""));

        log.info("查询ADDRESS4余额");
        Thread.sleep(SHORTSLEEPTIME);
        String queryInfo4TK1 = multiSign.BalanceByAddr(ADDRESS4, tokenType);
        assertThat(queryInfo4TK1, containsString("\"Total\":\"0\""));

        String queryInfo4TK2 = multiSign.BalanceByAddr(ADDRESS4, tokenType2);
        assertThat(queryInfo4TK2, containsString("90"));


        String Info2 = singleSignRecycle_LocalSign(PUBKEY4, tokenType2, "90", PRIKEY4PATH);
        assertThat(Info2, containsString("200"));
        Thread.sleep(SLEEPTIME);

        String balanceInfo4TK2 = multiSign.BalanceByAddr(ADDRESS4, tokenType2);
        assertThat(balanceInfo4TK2, containsString("\"Total\":\"0\""));


        log.info("查询ADDRESS5余额");
        String queryInfo5TK2 = multiSign.BalanceByAddr(ADDRESS5, tokenType2);
        assertThat(queryInfo5TK2, containsString("100.555"));


        String Info3 = singleSignRecycle_LocalSign(PUBKEY5, tokenType2, "100.555", PRIKEY5PATH);
        assertThat(Info3, containsString("200"));
        Thread.sleep(SLEEPTIME);

        String balanceInfo5TK2 = multiSign.BalanceByAddr(ADDRESS5, tokenType2);
        assertThat(balanceInfo5TK2, containsString("\"Total\":\"0\""));

        log.info("查询ADDRESS2余额");
        String queryInfo6TK1 = multiSign.BalanceByAddr(ADDRESS2, tokenType);
        assertThat(queryInfo6TK1, containsString("30"));

        String Info4 = singleSignRecycle_LocalSign(PUBKEY2, tokenType, "30", PRIKEY2PATH);
        assertThat(Info4, containsString("200"));
        Thread.sleep(SLEEPTIME);

        String balanceInfo6TK1 = multiSign.BalanceByAddr(ADDRESS2, tokenType);
        assertThat(balanceInfo6TK1, containsString("\"Total\":\"0\""));


        String queryInfo6TK2 = multiSign.BalanceByAddr(ADDRESS2, tokenType2);
        assertThat(queryInfo6TK2, containsString("10"));

        String Info5 = singleSignRecycle_LocalSign(PUBKEY2, tokenType2, "10", PRIKEY2PATH);
        assertThat(Info5, containsString("200"));
        Thread.sleep(SLEEPTIME);

        String balanceInfo6TK2 = multiSign.BalanceByAddr(ADDRESS2, tokenType2);
        assertThat(balanceInfo6TK2, containsString("\"Total\":\"0\""));

    }

    /**
     * Tc040单签转单签异常测试:
     */
    @Test
    public void TC040_SoloProgress_LocalSign() throws Exception {
        String transferData = "归集地址向" + ADDRESS3 + "转账3000个" + tokenType + ",和3000个" + tokenType2;
        log.info(transferData);
        List<Map> list = utilsClass.constructToken(ADDRESS3, tokenType, "3000");
        List<Map> list1 = utilsClass.constructToken(ADDRESS3, tokenType2, "3000", list);

        multiSignTest.multiSignTransfer_LocalSign(IMPPUTIONADD, PUBKEY4, transferData, list1, PRIKEY4PATH);
        Thread.sleep(SLEEPTIME);


        String transferData2 = "向地址4转账4000，向地址5转账70";
        log.info(transferData2);
        List<Map> list2 = soloSign.constructToken(ADDRESS4, tokenType, "4000");
        List<Map> list3 = soloSign.constructToken(ADDRESS5, tokenType, "70", list2);

        String res3 = singleSignTransfer_LocalSign(PUBKEY3, transferData2, list3, PRIKEY3PATH);
        log.info(res3);
        assertThat(res3, containsString("insufficient balance"));
        Thread.sleep(SLEEPTIME);

        String transferData3 = "向地址4转账4000，向地址5转账4001";
        log.info(transferData3);
        List<Map> list4 = soloSign.constructToken(ADDRESS4, tokenType, "4000");
        List<Map> list5 = soloSign.constructToken(ADDRESS5, tokenType2, "4001", list4);

        String res4 = singleSignTransfer_LocalSign(PUBKEY3, transferData3, list5, PRIKEY3PATH);
        log.info(res4);
        assertThat(res4, containsString("insufficient balance"));
        Thread.sleep(SLEEPTIME);

        String transferData4 = "向地址4转账4000，向地址5转账60";
        log.info(transferData4);
        List<Map> list6 = soloSign.constructToken(ADDRESS4, tokenType, "4000");
        List<Map> list7 = soloSign.constructToken(ADDRESS5, tokenType2, "60", list6);
        String res5 = singleSignTransfer_LocalSign(PUBKEY3, transferData4, list7, PRIKEY3PATH);
        log.info(res5);
        assertThat(res5, containsString("insufficient balance"));
        Thread.sleep(SLEEPTIME);


        String Info = singleSignRecycle_LocalSign(PUBKEY3, tokenType, "3000", PRIKEY3PATH);
        String Info3 = singleSignRecycle_LocalSign(PUBKEY3, tokenType2, "3000", PRIKEY3PATH);
        assertThat(Info, containsString("200"));
        assertThat(Info3, containsString("200"));
    }

    /**
     * 单签发行，本地签名
     *
     * @param length
     * @param amount
     * @return 数字资产类型
     * @throws Exception
     */
    public String issueTokenLocalSign(int length, String amount) throws Exception {
        String tokenType = "ST-" + UtilsClass.Random(length);
        String data = "" + "发行token: " + tokenType + " ，数量为：" + amount;
        String issueResult = soloSign.issueTokenLocalSign(PUBKEY1, tokenType, amount, data);
        //log.info("发行返回"+issueResult);
        String preSignData = JSONObject.fromObject(issueResult).getJSONObject("Data").toString();
        //log.info("签名前的数据：" + preSignData);
        String signedData = singleSign.singleSignIssueMethod(preSignData, PRIKEY1PATH);
        //log.info("签名后的数据：" + signedData);
        String response = soloSign.sendSign(signedData);
        //log.info("发送交易：" + response);
        assertThat(tokenType + "发行token错误", response, containsString("200"));
        return tokenType;
    }

    /**
     * 单签转账，本地签名
     *
     * @param fromPubKey
     * @param toAddr
     * @param data
     * @param tokenType
     * @param amount
     * @param fromPriKeyPath
     * @return
     * @throws Exception
     */
    public String singleSignTransfer_LocalSign(String fromPubKey, String data,
                                               List<Map> tokenList, String fromPriKeyPath) throws Exception {
//        List<Map> list1 = soloSign.constructToken(toAddr, tokenType, amount);
        String transferInfo2 = soloSign.TransferLocalSign(tokenList, fromPubKey, data);

        if (transferInfo2.contains("insufficient balance")){
            return transferInfo2;
        }

        String preSignData2 = JSONObject.fromObject(transferInfo2).getJSONObject("Data").toString();
//        log.info("签名前数据: " + preSignData2);

        String signedData2 = singleTrans.singleSignTransferAccountsMethod(preSignData2, fromPriKeyPath);

        //log.info("签名后的数据：" + signedData2);

        String response2 = soloSign.sendSign(signedData2);

        return response2;


    }

    /**
     * 单签回收，本地签名
     *
     * @param fromPubKey
     * @param tokenType
     * @param amount
     * @param fromPriKeyPath
     * @return
     * @throws Exception
     */
    public String singleSignRecycle_LocalSign(String fromPubKey, String tokenType,
                                              String amount, String fromPriKeyPath) throws Exception {

        String recycleResponse = soloSign.RecycleLocalSign(fromPubKey, tokenType, amount);

//        log.info("回收：" + recycleResponse);

        String preSignData = JSONObject.fromObject(recycleResponse).getJSONObject("Data").toString();
//        log.info("签名前数据: " + preSignData);

        String signedData = singleTrans.singleSignTransferAccountsMethod(preSignData, fromPriKeyPath);

//        log.info("签名后的数据：" + signedData);

        String txInfo = soloSign.sendSign(signedData);

        assertThat("发送交易", txInfo, containsString("200"));

        return txInfo;
    }


}