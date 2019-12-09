package com.tjfintech.common.functionTest.utxoSingleSign;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import com.tjfoc.base.MultiSignTransferAccounts;
import com.tjfoc.base.SingleSignIssue;
import com.tjfoc.base.SingleSignTransferAccounts;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.List;
import java.util.Map;

import static com.tjfintech.common.functionTest.store.StoreTest.SHORTSLEEPTIME;
import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LocalSingleSignTest {
    TestBuilder testBuilder = TestBuilder.getInstance();
    MultiSign multiSign = testBuilder.getMultiSign();
    SoloSign soloSign = testBuilder.getSoloSign();
    UtilsClass utilsClass = new UtilsClass();
    private static String tokenType;
    private static String tokenType2;
    SingleSignIssue singleSign = new SingleSignIssue();
    MultiSignTransferAccounts multiTrans = new MultiSignTransferAccounts();
    SingleSignTransferAccounts singleTrans = new SingleSignTransferAccounts();

    @BeforeClass
    public static void beforeClass() throws Exception {
        if (MULITADD1.isEmpty()) {
            BeforeCondition bf = new BeforeCondition();
            bf.updatePubPriKey();
            bf.collAddressTest();
            Thread.sleep(SLEEPTIME);
        }
    }

    @Before
    public void beforeConfig() throws Exception {

        //单签发行
        log.info("发行两种token");
        tokenType = issueTokenLocalSign(7, "10000.123456789");
        tokenType2 = issueTokenLocalSign(8, "20000.87654321");
        Thread.sleep(SLEEPTIME);

        //查询余额

        log.info("查询归集地址中token余额");
        String balance = soloSign.Balance(PRIKEY1, tokenType);
        String balance2 = soloSign.Balance(PRIKEY1, tokenType2);

        assertThat(tokenType + "查询余额错误", balance, containsString("200"));
        assertThat(tokenType + "查询余额不正确", balance, containsString("10000.123456"));
        assertThat(tokenType2 + "查询余额错误", balance2, containsString("200"));
        assertThat(tokenType2 + "查询余额不正确", balance2, containsString("20000.876543"));

    }


    /**
     * Tc024单签正常流程:发行，转账，回收。
     */
    @Test
    public void TC1414_SoloProgress_LocalSign() throws Exception {


        //归集地址转出
        String transferData = "归集地址向ADDRESS3转账100.25个" + tokenType + "，并向ADDRESS5转账200.555个" + tokenType2;

        log.info(transferData);
        List<Map> transferList = soloSign.constructToken(ADDRESS3, tokenType, "100.25");  // 转账操作：向address3账户转入100.25
        List<Map> transferList2 = soloSign.constructToken(ADDRESS5, tokenType2, "200.555", transferList);// 转账操作：向address5账户转入200.555同时向address3账户转入100.25
        singleSignTransfer_LocalSign(PUBKEY1, transferData, transferList2, PRIKEY1); //多账号转账

        Thread.sleep(SLEEPTIME);

        log.info("查询ADDRESS3和ADDRESS5余额");
        String queryInfo = multiSign.BalanceByAddr(ADDRESS3, tokenType);
        assertThat(queryInfo, containsString("200"));
        assertThat(queryInfo, containsString(tokenType + "\":\"100.25\""));

        String queryInfo3 = multiSign.BalanceByAddr(ADDRESS5, tokenType2);
        assertThat(queryInfo3, containsString("200"));
        assertThat(queryInfo3, containsString(tokenType2 + "\":\"200.555\""));

        //查询归集地址
        String queryInfo2 = multiSign.BalanceByAddr(ADDRESS1, tokenType);
        assertThat(queryInfo2, containsString("200"));
        assertThat(queryInfo2, containsString(tokenType + "\":\"9899.873456\""));

        String queryInfo4 = multiSign.BalanceByAddr(ADDRESS1, tokenType2);
        assertThat(queryInfo4, containsString("200"));
        assertThat(queryInfo4, containsString(tokenType2 + "\":\"19800.321543\""));


        String data1 = "ADDRESS3向ADDRESS4转账30个" + tokenType;
        log.info(data1);
        List<Map> list1 = soloSign.constructToken(ADDRESS4, tokenType, "30");
        String res1 = singleSignTransfer_LocalSign(PUBKEY3, data1, list1, PRIKEY3); //单账号转账
        log.info(res1);
        assertThat(res1, containsString("200"));
        Thread.sleep(SLEEPTIME);

        String data2 = "ADDRESS5向ADDRESS4转账80个" + tokenType2;
        log.info(data2);
        List<Map> list2 = soloSign.constructToken(ADDRESS4, tokenType2, "80");
        String res2 = singleSignTransfer_LocalSign(PUBKEY5, data2, list2, PRIKEY5);
        log.info(res2);
        assertThat(res2, containsString("200"));
        Thread.sleep(SLEEPTIME);

        log.info("查询地址4余额");
        String queryInfo21 = multiSign.BalanceByAddr(ADDRESS4, tokenType);
        assertThat(queryInfo21, containsString("200"));
        assertThat(queryInfo21, containsString(tokenType + "\":\"30\""));

        String queryInfo22 = multiSign.BalanceByAddr(ADDRESS4, tokenType2);
        assertThat(queryInfo22, containsString("200"));
        assertThat(queryInfo22, containsString(tokenType2 + "\":\"80\""));

        String data3 = "ADDRESS4向ADDRESS2转账30个" + tokenType + "，转账70个" + tokenType2;
        log.info(data3);
        List<Map> list3 = soloSign.constructToken(ADDRESS2, tokenType, "30");
        List<Map> list4 = soloSign.constructToken(ADDRESS2, tokenType2, "70", list3);
        String res3 = singleSignTransfer_LocalSign(PUBKEY4, data3, list4, PRIKEY4); //多账号转账
        assertThat(res3, containsString("200"));
        Thread.sleep(SLEEPTIME);

        String data4 = "ADDRESS5向ADDRESS2转账20个" + tokenType2;
        log.info(data4);
        List<Map> list5 = soloSign.constructToken(ADDRESS2, tokenType2, "20");
        String res4 = singleSignTransfer_LocalSign(PUBKEY5, data4, list5, PRIKEY5);
        assertThat(res4, containsString("200"));
        Thread.sleep(SLEEPTIME);

        String data5 = "ADDRESS2向ADDRESS4转账80个" + tokenType2;
        log.info(data5);
        List<Map> list6 = soloSign.constructToken(ADDRESS4, tokenType2, "30");
        List<Map> list7 = soloSign.constructToken(ADDRESS4, tokenType2, "50", list6);
        String res5 = singleSignTransfer_LocalSign(PUBKEY2, data5, list7, PRIKEY2);
        assertThat(res5, containsString("200"));
        Thread.sleep(SLEEPTIME);


        log.info("查询ADDRESS3余额");
        String queryInfo3TK1 = multiSign.BalanceByAddr(ADDRESS3, tokenType);
        assertThat(queryInfo3TK1, containsString(tokenType + "\":\"70.25\""));

        String Info1 = singleSignRecycle_LocalSign(PUBKEY3, tokenType, "70.25", PRIKEY3); //单账号回收
        assertThat(Info1, containsString("200"));
        Thread.sleep(SLEEPTIME);

        String balanceInfo3TK1 = multiSign.BalanceByAddr(ADDRESS3, tokenType);
        assertThat(balanceInfo3TK1, containsString("\"Data\":{}"));

        log.info("查询ADDRESS4余额");
        Thread.sleep(SHORTSLEEPTIME);
        String queryInfo4TK1 = multiSign.BalanceByAddr(ADDRESS4, tokenType);
        assertThat(queryInfo4TK1, containsString("\"Data\":{}")); //总数为0

        String queryInfo4TK2 = multiSign.BalanceByAddr(ADDRESS4, tokenType2);
        assertThat(queryInfo4TK2, containsString(tokenType2 + "\":\"90\""));


        String Info2 = singleSignRecycle_LocalSign(PUBKEY4, tokenType2, "90", PRIKEY4);
        assertThat(Info2, containsString("200"));
        Thread.sleep(SLEEPTIME);

        String balanceInfo4TK2 = multiSign.BalanceByAddr(ADDRESS4, tokenType2);
        assertThat(balanceInfo4TK2, containsString("\"Data\":{}"));


        log.info("查询ADDRESS5余额");
        String queryInfo5TK2 = multiSign.BalanceByAddr(ADDRESS5, tokenType2);
        assertThat(queryInfo5TK2, containsString(tokenType2 + "\":\"100.555\""));


        String Info3 = singleSignRecycle_LocalSign(PUBKEY5, tokenType2, "100.555", PRIKEY5);
        assertThat(Info3, containsString("200"));
        Thread.sleep(SLEEPTIME);

        String balanceInfo5TK2 = multiSign.BalanceByAddr(ADDRESS5, tokenType2);
        assertThat(balanceInfo5TK2, containsString("\"Data\":{}"));

        log.info("查询ADDRESS2余额");
        String queryInfo6TK1 = multiSign.BalanceByAddr(ADDRESS2, tokenType);
        assertThat(queryInfo6TK1, containsString(tokenType + "\":\"30\""));

        String Info4 = singleSignRecycle_LocalSign(PUBKEY2, tokenType, "30", PRIKEY2);
        assertThat(Info4, containsString("200"));
        Thread.sleep(SLEEPTIME);

        String balanceInfo6TK1 = multiSign.BalanceByAddr(ADDRESS2, tokenType);
        assertThat(balanceInfo6TK1, containsString("\"Data\":{}"));


        String queryInfo6TK2 = multiSign.BalanceByAddr(ADDRESS2, tokenType2);
        assertThat(queryInfo6TK2, containsString(tokenType2 + "\":\"10\""));

        String Info5 = singleSignRecycle_LocalSign(PUBKEY2, tokenType2, "10", PRIKEY2);
        assertThat(Info5, containsString("200"));
        Thread.sleep(SLEEPTIME);

        String balanceInfo6TK2 = multiSign.BalanceByAddr(ADDRESS2, tokenType2);
        assertThat(balanceInfo6TK2, containsString("\"Data\":{}"));

    }


    /**
     * 多账号同时回收多个单签地址，本地签名
     */
    @Test
    public void TC_SoloProgress_Recycles() throws Exception {

        //归集地址转单签
        String transferData = "归集地址向ADDRESS3转账100.25个" + tokenType + "，并向ADDRESS5转账200.555个" + tokenType2;

        log.info(transferData);
        List<Map> transferList = soloSign.constructToken(ADDRESS3, tokenType, "100.25");
        List<Map> transferList2 = soloSign.constructToken(ADDRESS5, tokenType2, "200.555", transferList);
        singleSignTransfer_LocalSign(PUBKEY1, transferData, transferList2, PRIKEY1); //多账号转账

        Thread.sleep(SLEEPTIME);

        log.info("查询ADDRESS3和ADDRESS5余额");
        String queryInfo = multiSign.BalanceByAddr(ADDRESS3, tokenType);
        assertThat(queryInfo, containsString("200"));
        assertThat(queryInfo, containsString(tokenType + "\":\"100.25\""));

        String queryInfo3 = multiSign.BalanceByAddr(ADDRESS5, tokenType2);
        assertThat(queryInfo3, containsString("200"));
        assertThat(queryInfo3, containsString(tokenType2 + "\":\"200.555\""));

        //查询归集地址
        String queryInfo2 = multiSign.BalanceByAddr(ADDRESS1, tokenType);
        assertThat(queryInfo2, containsString("200"));
        assertThat(queryInfo2, containsString(tokenType + "\":\"9899.873456\""));

        String queryInfo4 = multiSign.BalanceByAddr(ADDRESS1, tokenType2);
        assertThat(queryInfo4, containsString("200"));
        assertThat(queryInfo4, containsString(tokenType2 + "\":\"19800.321543\""));


        String data1 = "ADDRESS3向ADDRESS4转账30个" + tokenType;
        log.info(data1);
        List<Map> list1 = soloSign.constructToken(ADDRESS4, tokenType, "30");
        String res1 = singleSignTransfer_LocalSign(PUBKEY3, data1, list1, PRIKEY3);
        log.info(res1);
        assertThat(res1, containsString("200"));
        Thread.sleep(SLEEPTIME);

        String data2 = "ADDRESS5向ADDRESS4转账80个" + tokenType2;
        log.info(data2);
        List<Map> list2 = soloSign.constructToken(ADDRESS4, tokenType2, "80");
        String res2 = singleSignTransfer_LocalSign(PUBKEY5, data2, list2, PRIKEY5);
        log.info(res2);
        assertThat(res2, containsString("200"));
        Thread.sleep(SLEEPTIME);

        log.info("查询地址4余额");
        String queryInfo21 = multiSign.BalanceByAddr(ADDRESS4, tokenType);
        assertThat(queryInfo21, containsString("200"));
        assertThat(queryInfo21, containsString(tokenType + "\":\"30\""));

        String queryInfo22 = multiSign.BalanceByAddr(ADDRESS4, tokenType2);
        assertThat(queryInfo22, containsString("200"));
        assertThat(queryInfo22, containsString(tokenType2 + "\":\"80\""));

        String data3 = "ADDRESS4向ADDRESS2转账30个" + tokenType + "，转账70个" + tokenType2;
        log.info(data3);
        List<Map> list3 = soloSign.constructToken(ADDRESS2, tokenType, "30");
        List<Map> list4 = soloSign.constructToken(ADDRESS2, tokenType2, "70", list3);
        String res3 = singleSignTransfer_LocalSign(PUBKEY4, data3, list4, PRIKEY4);
        assertThat(res3, containsString("200"));
        Thread.sleep(SLEEPTIME);

        String data4 = "ADDRESS5向ADDRESS2转账20个" + tokenType2;
        log.info(data4);
        List<Map> list5 = soloSign.constructToken(ADDRESS2, tokenType2, "20");
        String res4 = singleSignTransfer_LocalSign(PUBKEY5, data4, list5, PRIKEY5);
        assertThat(res4, containsString("200"));
        Thread.sleep(SLEEPTIME);

        String data5 = "ADDRESS2向ADDRESS4转账80个" + tokenType2;
        log.info(data5);
        List<Map> list6 = soloSign.constructToken(ADDRESS4, tokenType2, "30");
        List<Map> list7 = soloSign.constructToken(ADDRESS4, tokenType2, "50", list6);
        String res5 = singleSignTransfer_LocalSign(PUBKEY2, data5, list7, PRIKEY2);
        assertThat(res5, containsString("200"));
        Thread.sleep(SLEEPTIME);


        //多账号同时回收
//
//        log.info("多账号同时回收，回收归集地址和其他地址余额");
//        List<Map> recycleList1 = utilsClass.constructToken("", PUBKEY3, tokenType, "70.25");
//        List<Map> recycleList2 = utilsClass.constructToken("", PUBKEY4, tokenType2, "90", recycleList1);
//        List<Map> recycleList3 = utilsClass.constructToken("", PUBKEY5, tokenType2, "100.555", recycleList2);
//        List<Map> recycleList4 = utilsClass.constructToken("", PUBKEY2, tokenType, "30", recycleList3);
//        List<Map> recycleList5 = utilsClass.constructToken("", PUBKEY2, tokenType2, "10", recycleList4);
//
//        String response = multiSign.RecyclesLocalSign(recycleList5);
//        log.info(response);
//
//        singleSignRecycles(response, "0", PRIKEY3);
//        singleSignRecycles(response, "1", PRIKEY4);
//        singleSignRecycles(response, "2", PRIKEY5);
//        singleSignRecycles(response, "3", PRIKEY2);
//        singleSignRecycles(response, "4", PRIKEY2);
//
//
//        Thread.sleep(SLEEPTIME);
//
//
//        log.info("查询ADDRESS3余额");
//
//
//        String balanceInfo3TK1 = multiSign.BalanceByAddr(ADDRESS3, tokenType);
//        assertThat(balanceInfo3TK1, containsString("\"Data\":{}"));
//
//        log.info("查询ADDRESS4余额");
//
//
//        String balanceInfo4TK2 = multiSign.BalanceByAddr(ADDRESS4, tokenType2);
//        assertThat(balanceInfo4TK2, containsString("\"Data\":{}"));
//
//
//        log.info("查询ADDRESS5余额");
//
//
//        String balanceInfo5TK2 = multiSign.BalanceByAddr(ADDRESS5, tokenType2);
//        assertThat(balanceInfo5TK2, containsString("\"Data\":{}"));
//
//        log.info("查询ADDRESS2余额");
//
//        String balanceInfo6TK1 = multiSign.BalanceByAddr(ADDRESS2, tokenType);
//        assertThat(balanceInfo6TK1, containsString("\"Data\":{}"));
//
//        String balanceInfo6TK2 = multiSign.BalanceByAddr(ADDRESS2, tokenType2);
//        assertThat(balanceInfo6TK2, containsString("\"Data\":{}"));

    }


    /**
     * 多账号同时回收多签地址和单签地址，本地签名
     */
//    @Test
//    public void TC_SoloProgress_Recycles2() throws Exception {
//
//        //归集地址转单签
//        String transferData = "归集地址向ADDRESS3转账100.25个" + tokenType + "，并向ADDRESS5转账200.555个" + tokenType2;
//
//        log.info(transferData);
//        List<Map> transferList = utilsClass.constructToken(ADDRESS3, tokenType, "100.25");
//        List<Map> transferList2 = utilsClass.constructToken(ADDRESS5, tokenType2, "200.555", transferList);
//        singleSignTransfer_LocalSign(PUBKEY1, transferData, transferList2, PRIKEY1); //多账号转账
//
//        Thread.sleep(SLEEPTIME);
//
//        log.info("查询ADDRESS3和ADDRESS5余额");
//        String queryInfo = multiSign.BalanceByAddr(ADDRESS3, tokenType);
//        assertThat(queryInfo, containsString("200"));
//        assertThat(queryInfo, containsString(tokenType + "\":\"100.25\""));
//
//        String queryInfo3 = multiSign.BalanceByAddr(ADDRESS5, tokenType2);
//        assertThat(queryInfo3, containsString("200"));
//        assertThat(queryInfo3, containsString(tokenType + "\":\"200.555\""));
//
//        //查询归集地址
//        String queryInfo2 = multiSign.BalanceByAddr(ADDRESS1, tokenType);
//        assertThat(queryInfo2, containsString("200"));
//        assertThat(queryInfo2, containsString(tokenType + "\":\"9899.873456789\""));
//
//        String queryInfo4 = multiSign.BalanceByAddr(ADDRESS1, tokenType2);
//        assertThat(queryInfo4, containsString("200"));
//        assertThat(queryInfo4, containsString(tokenType + "\":\"19800.32154321\""));
//
//        //多账号同时回收
//
//        log.info("多账号同时回收，回收归集地址和其他地址余额");
//        List<Map> recycleList1 = utilsClass.constructToken(IMPPUTIONADD, PUBKEY4, tokenType, "9899.873456789");
//        List<Map> recycleList2 = utilsClass.constructToken("", PUBKEY3, tokenType, "100.25", recycleList1);
//        List<Map> recycleList3 = utilsClass.constructToken("", PUBKEY5, tokenType2, "200.555", recycleList2);
//
//        String response = multiSign.RecyclesLocalSign(recycleList3);
//
//        log.info(response);
//
//        multiSignTest.multiSignRecycles(response, "0", PRIKEY4);
//        singleSignRecycles(response, "1", PRIKEY3);
//        singleSignRecycles(response, "2", PRIKEY5);
//
//        Thread.sleep(SLEEPTIME);
//
//        log.info("查询ADDRESS3余额");
//
//        String balanceInfo3TK1 = multiSign.BalanceByAddr(ADDRESS3, tokenType);
//        assertThat(balanceInfo3TK1, containsString("\"Data\":{}"));
//
//        log.info("查询ADDRESS5余额");
//
//
//        String balanceInfo5TK2 = multiSign.BalanceByAddr(ADDRESS5, tokenType2);
//        assertThat(balanceInfo5TK2, containsString("\"Data\":{}"));
//
//        log.info("查询归集地址余额");
//
//        String balanceInfo6TK1 = multiSign.BalanceByAddr(IMPPUTIONADD, tokenType);
//        assertThat(balanceInfo6TK1, containsString("\"Data\":{}"));
//
//    }


    /**
     * Tc040单签转单签异常测试
     */
    @Test
    public void TC040_SoloProgress_LocalSign() throws Exception {
        String transferData = "归集地址向" + ADDRESS3 + "转账3000个" + tokenType + ",和3000个" + tokenType2;
        log.info(transferData);
        List<Map> list = soloSign.constructToken(ADDRESS3, tokenType, "3000");
        List<Map> list1 = soloSign.constructToken(ADDRESS3, tokenType2, "3000", list);

        singleSignTransfer_LocalSign(PUBKEY1, transferData, list1, PRIKEY1); //多账号转账
        Thread.sleep(SLEEPTIME);


        String transferData2 = "转账相同币种，A金额不足";
        log.info(transferData2);
        List<Map> list2 = soloSign.constructToken(ADDRESS4, tokenType, "4000");
        List<Map> list3 = soloSign.constructToken(ADDRESS5, tokenType, "70", list2);

        String res3 = singleSignTransfer_LocalSign(PUBKEY3, transferData2, list3, PRIKEY3);
        log.info(res3);
        assertThat(res3, containsString("insufficient balance"));
        Thread.sleep(SLEEPTIME);

        String transferData21 = "转账相同币种，B金额不足";
        log.info(transferData21);
        List<Map> list21 = soloSign.constructToken(ADDRESS4, tokenType, "70");
        List<Map> list31 = soloSign.constructToken(ADDRESS5, tokenType, "4000", list21);

        String res31 = singleSignTransfer_LocalSign(PUBKEY3, transferData21, list31, PRIKEY3);
        log.info(res31);
        assertThat(res31, containsString("insufficient balance"));
        Thread.sleep(SLEEPTIME);

        String transferData22 = "转账相同币种，金额都不足";
        log.info(transferData22);
        List<Map> list22 = soloSign.constructToken(ADDRESS4, tokenType, "4000.25");
        List<Map> list32 = soloSign.constructToken(ADDRESS5, tokenType, "4000", list22);

        String res32 = singleSignTransfer_LocalSign(PUBKEY3, transferData22, list32, PRIKEY3);
        log.info(res32);
        assertThat(res32, containsString("insufficient balance"));
        Thread.sleep(SLEEPTIME);


        String transferData3 = "转账不同币种，金额都不足";
        log.info(transferData3);
        List<Map> list4 = soloSign.constructToken(ADDRESS4, tokenType, "4000");//不同币种，金额都不足
        List<Map> list5 = soloSign.constructToken(ADDRESS5, tokenType2, "4001", list4);

        String res4 = singleSignTransfer_LocalSign(PUBKEY3, transferData3, list5, PRIKEY3);
        log.info(res4);
        assertThat(res4, containsString("insufficient balance"));
        Thread.sleep(SLEEPTIME);

        String transferData32 = "转账不同币种，金额都不足";
        log.info(transferData32);
        List<Map> list42 = soloSign.constructToken(ADDRESS4, tokenType, "4000");//不同币种，金额都不足
        List<Map> list52 = soloSign.constructToken(ADDRESS5, tokenType2, "4001", list42);

        String res42 = singleSignTransfer_LocalSign(PUBKEY3, transferData32, list52, PRIKEY3);
        log.info(res42);
        assertThat(res42, containsString("insufficient balance"));
        Thread.sleep(SLEEPTIME);


        String transferData4 = "转账不同币种，A金额都不足";
        log.info(transferData4);
        List<Map> list6 = soloSign.constructToken(ADDRESS4, tokenType, "4000");//不同币种，A金额都不足
        List<Map> list7 = soloSign.constructToken(ADDRESS5, tokenType2, "60", list6);
        String res5 = singleSignTransfer_LocalSign(PUBKEY3, transferData4, list7, PRIKEY3);
        log.info(res5);
        assertThat(res5, containsString("insufficient balance"));
        Thread.sleep(SLEEPTIME);

        String transferData5 = "转账不同币种，B金额都不足";
        log.info(transferData5);
        List<Map> list8 = soloSign.constructToken(ADDRESS4, tokenType, "60");//不同币种，B金额都不足
        List<Map> list9 = soloSign.constructToken(ADDRESS5, tokenType2, "4000", list8);
        String res6 = singleSignTransfer_LocalSign(PUBKEY3, transferData5, list9, PRIKEY3);
        log.info(res6);
        assertThat(res6, containsString("insufficient balance"));
        Thread.sleep(SLEEPTIME);


        String Info = singleSignRecycle_LocalSign(PUBKEY3, tokenType, "3000", PRIKEY3);
        String Info3 = singleSignRecycle_LocalSign(PUBKEY3, tokenType2, "3000", PRIKEY3);
        assertThat(Info, containsString("200"));
        assertThat(Info3, containsString("200"));
    }

    /**
     * Tc024单签正常流程:发行，转账，回收。
     *  私钥带密码
     */
    @Test
    public void TC1415_SoloProgress_LocalSign_PWD() throws Exception {

        //单签发行
        log.info("发行两种token");
        tokenType = issueTokenLocalSign(7, "10000.123456");
        //查询余额
        Thread.sleep(SLEEPTIME);
        log.info("查询归集地址中token余额");
        String balance = multiSign.BalanceByAddr(ADDRESS1, tokenType);
        assertThat(tokenType + "查询余额错误", balance, containsString("200"));
        assertThat(tokenType + "查询余额不正确", balance, containsString("10000.123456"));

        //转账
        String transferData = "归集地址向ADDRESS7转账100.25个" + tokenType ;

        log.info(transferData);
        List<Map> transferList = soloSign.constructToken(ADDRESS7, tokenType, "100.25");
        singleSignTransfer_LocalSign(PUBKEY1, transferData, transferList, PRIKEY1);//转账的本地签名

        Thread.sleep(SLEEPTIME);

        log.info("查询ADDRESS7和归集地址余额");
        String queryInfo = multiSign.BalanceByAddr(ADDRESS1, tokenType);
        assertThat(queryInfo, containsString("200"));
        assertThat(queryInfo, containsString(tokenType + "\":\"9899.873456\""));


        String queryInfo2 = multiSign.BalanceByAddr(ADDRESS7, tokenType);
        assertThat(queryInfo2, containsString("200"));
        assertThat(queryInfo2, containsString(tokenType + "\":\"100.25\""));

        //从带密码的账户转出
        String data1 = "ADDRESS7向ADDRESS4转账5个" + tokenType;
        log.info(data1);
        List<Map> list1 = soloSign.constructToken(ADDRESS4, tokenType, "5");
        String res1 = singleSignTransfer_LocalSign(PUBKEY7, data1, list1, PRIKEY7, PWD7); //单账号转账
        log.info(res1);
        assertThat(res1, containsString("200"));
        Thread.sleep(SLEEPTIME);

        log.info("查询地址7和地址4余额");
        String queryInfo21 = soloSign.Balance(PRIKEY7, PWD7,tokenType);
        assertThat(queryInfo21, containsString("200"));
        assertThat(queryInfo21, containsString(tokenType + "\":\"95.25\""));

        String queryInfo22 = soloSign.Balance(PRIKEY4, tokenType);
        assertThat(queryInfo22, containsString("200"));
        assertThat(queryInfo22, containsString(tokenType + "\":\"5\""));

        log.info("回收地址7和地址4余额");
        singleSignRecycle_LocalSign(PUBKEY7, tokenType, "95.25", PRIKEY7, PWD7); //单账号回收
        singleSignRecycle_LocalSign(PUBKEY4, tokenType, "5", PRIKEY4); //单账号回收
        Thread.sleep(SLEEPTIME);

        log.info("查询地址7和地址4余额");
        queryInfo21 = soloSign.Balance(PRIKEY7, PWD7,tokenType);
        assertThat(queryInfo21, containsString("200"));
        assertThat(queryInfo21, containsString("\"Total\":\"0\""));

        queryInfo22 = soloSign.Balance(PRIKEY4, tokenType);
        assertThat(queryInfo22, containsString("200"));
        assertThat(queryInfo22, containsString("\"Total\":\"0\""));

        log.info("查询零地址余额");
        String queryInfo5 = multiSign.QueryZero(tokenType);
        assertThat(queryInfo5, containsString("200"));
        assertThat(queryInfo5, containsString(tokenType + "\":\"100.25\""));

    }



//    /**
//     * 单签发行，本地签名
//     * 私钥带密码
//     * @param length
//     * @param amount
//     * @return 数字资产类型
//     * @throws Exception
//     */
//    public String issueTokenLocalSignPwd(int length, String amount) throws Exception {
//        String tokenType = "ST-" + UtilsClass.Random(length);
//        String data = "" + "发行token: " + tokenType + " ，数量为：" + amount;
//        String issueResult = soloSign.issueTokenLocalSign(PUBKEY6, tokenType, amount, data);
//        log.info("单签发行返回" + issueResult);
//        String preSignData = JSONObject.fromObject(issueResult).getJSONObject("Data").toString();
//        log.info("单签发行签名前的数据：" + preSignData);
//        String signedData = singleSign.singleSignIssueMethod(preSignData, PRIKEY6, PWD6);
//        log.info("单签发行签名后的数据：" + signedData);
//        String response = soloSign.sendSign(signedData);
//                log.info("发送交易：" + response);
//        assertThat(tokenType + "发行token错误", response, containsString("200"));
//        return tokenType;
//    }


    /**
     *  单签转账，本地签名
     * 私钥带密码
     * @param fromPubKey
     * @param data
     * @param tokenList
     * @param fromPriKeyPath
     * @param pwd
     * @return
     * @throws Exception
     */
    public String singleSignTransfer_LocalSign(String fromPubKey, String data,
                                               List<Map> tokenList, String fromPriKeyPath, String pwd) throws Exception {
//        List<Map> list1 = soloSign.constructToken(toAddr, tokenType, amount);
        String transferInfo2 = soloSign.TransferLocalSign(tokenList, fromPubKey, data);

        if (transferInfo2.contains("insufficient balance")) {
            return transferInfo2;
        }
//        log.info("单签转账返回" + transferInfo2);
        String preSignData2 = JSONObject.fromObject(transferInfo2).getJSONObject("Data").toString();
//        log.info("单签转账签名前数据: " + preSignData2);


        String signedData2 = singleTrans.singleSignTransferAccountsMethod(preSignData2, fromPriKeyPath, pwd);

//        log.info("单签转账签名后的数据：" + signedData2);

        String response2 = soloSign.sendSign(signedData2);

        return response2;

    }

    /**
     * 单签回收，本地签名
     * 私钥带密码
     * @param fromPubKey
     * @param tokenType
     * @param amount
     * @param fromPriKeyPath
     * @return
     * @throws Exception
     */
    public String singleSignRecycle_LocalSign(String fromPubKey, String tokenType,
                                              String amount, String fromPriKeyPath, String pwd) throws Exception {

        String recycleResponse = soloSign.RecycleLocalSign(fromPubKey, tokenType, amount);

        log.info("单签回收返回：" + recycleResponse);

        String preSignData = JSONObject.fromObject(recycleResponse).getJSONObject("Data").toString();
        log.info("单签回收签名前数据: " + preSignData);

        String signedData = singleTrans.singleSignTransferAccountsMethod(preSignData, fromPriKeyPath, pwd);

        log.info("单签回收签名后的数据：" + signedData);

        String txInfo = soloSign.sendSign(signedData);

        assertThat("发送交易", txInfo, containsString("200"));

        return txInfo;
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
//        log.info("单签发行返回" + issueResult);
        String preSignData = JSONObject.fromObject(issueResult).getJSONObject("Data").toString();
//        log.info("单签发行签名前的数据：" + preSignData);
        String signedData = singleSign.singleSignIssueMethod(preSignData, PRIKEY1);
//        log.info("单签发行签名后的数据：" + signedData);
        String response = soloSign.sendSign(signedData);
//        log.info("发送交易：" + response);
        assertThat(tokenType + "发行token错误", response, containsString("200"));
        return tokenType;
    }

    /**
     * 单签转账，本地签名
     *
     * @param fromPubKey
     * @param data
     * @param tokenList
     * @param fromPriKeyPath
     * @return
     * @throws Exception
     */
    public String singleSignTransfer_LocalSign(String fromPubKey, String data,
                                               List<Map> tokenList, String fromPriKeyPath) throws Exception {
//        List<Map> list1 = soloSign.constructToken(toAddr, tokenType, amount);
        String transferInfo2 = soloSign.TransferLocalSign(tokenList, fromPubKey, data);

        if (transferInfo2.contains("insufficient balance")) {
            return transferInfo2;
        }
//        log.info("单签转账返回" + transferInfo2);
        String preSignData2 = JSONObject.fromObject(transferInfo2).getJSONObject("Data").toString();
//        log.info("单签转账签名前数据: " + preSignData2);


        String signedData2 = singleTrans.singleSignTransferAccountsMethod(preSignData2, fromPriKeyPath);  //单签发行的方法

//        log.info("单签转账签名后的数据：" + signedData2);

        String response2 = soloSign.sendSign(signedData2);

        return response2;


    }

    /**
     * 单签回收，本地签名
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

//        log.info("单签回收返回：" + recycleResponse);

        String preSignData = JSONObject.fromObject(recycleResponse).getJSONObject("Data").toString();
//        log.info("单签回收签名前数据: " + preSignData);

        String signedData = singleTrans.singleSignTransferAccountsMethod(preSignData, fromPriKeyPath);

//        log.info("单签回收签名后的数据：" + signedData);

        String txInfo = soloSign.sendSign(signedData);

        assertThat("发送交易", txInfo, containsString("200"));

        return txInfo;
    }

    /**
     * 回收单签账号余额-多账号同时回收
     * @param response
     * @param index
     * @param fromPriKeyPath
     * @return
     * @throws Exception
     */
    public String singleSignRecycles(String response, String index, String fromPriKeyPath) throws Exception {
        System.out.println("打印的"+response);
        String key = "single-tx-" + index;
        String preSignData1 = JSONObject.fromObject(response).getJSONObject("Data").getString(key);


        String signedData = singleTrans.singleSignTransferAccountsMethod(preSignData1, fromPriKeyPath);

//        log.info("签名后的数据：" + signedData);

        String txInfo = soloSign.sendSign(signedData);

        return txInfo;
    }


}