package com.tjfintech.common.functionTest;


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
    MultiSignTransferAccounts multiTrans = new MultiSignTransferAccounts();


    @Before
    public void beforeConfig() throws Exception {
        log.info("发行两种token1000个");
        tokenType = IssueTokenLocalSign(7, "1000");
        tokenType2 = IssueTokenLocalSign(8, "1000");

        Thread.sleep(SLEEPTIME);
        log.info("查询归集地址中token余额");
        String balance = multiSign.BalanceByAddr(IMPPUTIONADD, tokenType);
        String balance2 = multiSign.BalanceByAddr(IMPPUTIONADD, tokenType2);
        assertThat(tokenType+"查询余额错误",balance, containsString("200"));
        assertThat(tokenType+"查询余额不正确",balance, containsString("\"Total\":\"1000\""));
        assertThat(tokenType2+"查询余额错误",balance2, containsString("200"));
        assertThat(tokenType2+"查询余额不正确",balance2, containsString("\"Total\":\"1000\""));

    }




    /**
     * Tc03多签正常流程-发币：签名：查询：转账：查询:回收：查询
     */
    @Test
    public void TC03_multiProgress_LocalSign() throws Exception {


        String transferData = "归集地址向MULITADD4转账10个: " + tokenType;
        log.info(transferData);
        List<Map> transferList=utilsClass.constructToken(MULITADD4,tokenType,"10");
        multiSignTransfer_LocalSign(IMPPUTIONADD,PUBKEY4, transferData, transferList, PRIKEY4PATH);

        Thread.sleep(SLEEPTIME);

        log.info("查询归集地址和MULITADD4余额，判断转账是否成功");
        String queryInfo = multiSign.BalanceByAddr(MULITADD4, tokenType);
        assertThat(queryInfo, containsString("200"));
        assertThat(queryInfo, containsString("\"Total\":\"10\""));

        String queryInfo2 = multiSign.BalanceByAddr(IMPPUTIONADD, tokenType);
        assertThat(queryInfo2, containsString("200"));
        assertThat(queryInfo2, containsString("\"Total\":\"990\""));


        log.info("回收归集地址和MULITADD4的token");
        String recycleInfo = multiSignRecycle_LocalSign(IMPPUTIONADD, PUBKEY4, tokenType, "990", PRIKEY4PATH);
        String recycleInfo2 = multiSignRecycle_LocalSign(MULITADD4, PUBKEY1, tokenType, "10", PRIKEY1PATH);
        assertThat(recycleInfo, containsString("200"));
        assertThat(recycleInfo2, containsString("200"));
        Thread.sleep(SLEEPTIME);

        log.info("查询回收后账户余额是否为0");
        String queryInfo3 = multiSign.BalanceByAddr(IMPPUTIONADD, tokenType);
        String queryInfo4 = multiSign.BalanceByAddr(MULITADD5, tokenType);
        assertThat(queryInfo3, containsString("200"));
        assertThat(queryInfo3, containsString("\"Total\":\"0\""));
        assertThat(queryInfo4, containsString("200"));
        assertThat(queryInfo4, containsString("\"Total\":\"0\""));

        log.info("查询零地址余额");
        String queryInfo5 = multiSign.QueryZero(tokenType);
        assertThat(queryInfo5, containsString("200"));
        assertThat(queryInfo5, containsString("\"Total\":\"1000\""));

    }

    /**
     *TC19归集地址向两个多签地址转账
     * 发两种币-查询归集地址-转账两个地址不同token-查询-回收-查询
     * @throws Exception
     */
    @Test
    public void TC19_transferMulti_LocalSign()throws  Exception{


        List<Map> transferList=utilsClass.constructToken(MULITADD4,tokenType,"10");
        List<Map> transferList2=utilsClass.constructToken(MULITADD5,tokenType2,"10",transferList);
        List<Map> transferList3=utilsClass.constructToken(MULITADD5,tokenType,"10",transferList);

        String transferData = "归集地址向MULITADD4转账10个" + tokenType+", 归集地址向MULITADD5转账10个" + tokenType2;
        log.info(transferData);
        String transferInfo= multiSignTransfer_LocalSign(IMPPUTIONADD,PUBKEY4, transferData, transferList2, PRIKEY4PATH);//不同币种

        Thread.sleep(SLEEPTIME); //UTXO关系，两笔交易之间需要休眠

        String transferData2 = "归集地址向MULITADD4转账10个" + tokenType+", 归集地址向MULITADD5转账10个" + tokenType;
        log.info(transferData2);
        String transferInfo2= multiSignTransfer_LocalSign(IMPPUTIONADD,PUBKEY4, transferData2, transferList3, PRIKEY4PATH);//相同币种

        assertThat(transferInfo,containsString("200"));
        assertThat(transferInfo2,containsString("200"));
        Thread.sleep(SLEEPTIME);

        log.info("查询余额判断转账是否成功");
        String queryInfo= multiSign.BalanceByAddr(MULITADD4,tokenType);
        String queryInfo2= multiSign.BalanceByAddr(MULITADD5,tokenType2);
        assertThat(queryInfo,containsString("200"));
        assertThat(queryInfo2,containsString("200"));
        assertThat(JSONObject.fromObject(queryInfo).getJSONObject("Data").getString("Total"), containsString("20"));
        assertThat(JSONObject.fromObject(queryInfo2).getJSONObject("Data").getString("Total"), containsString("10"));

        log.info("回收Token");
        String recycleInfo = multiSignRecycle_LocalSign(IMPPUTIONADD, PUBKEY4, tokenType, "970", PRIKEY4PATH);
        String recycleInfo2 = multiSignRecycle_LocalSign(IMPPUTIONADD, PUBKEY4, tokenType2, "990", PRIKEY4PATH);
        String recycleInfo3 = multiSignRecycle_LocalSign(MULITADD4, PUBKEY1, tokenType, "20", PRIKEY1PATH);
        String recycleInfo4 = multiSignRecycle_LocalSign(MULITADD5, PUBKEY1, tokenType2, "10", PRIKEY1PATH);
        String recycleInfo5 = multiSignRecycle_LocalSign(MULITADD5, PUBKEY1, tokenType, "10", PRIKEY1PATH);
        Thread.sleep(SLEEPTIME);
        assertThat(recycleInfo,containsString("200"));
        assertThat(recycleInfo2,containsString("200"));
        assertThat(recycleInfo3,containsString("200"));
        assertThat(recycleInfo4,containsString("200"));
        assertThat(recycleInfo5,containsString("200"));

        log.info("查询余额判断回收成功与否");
        String queryInfo3= multiSign.BalanceByAddr(MULITADD4,tokenType);
        String queryInfo4= multiSign.BalanceByAddr(MULITADD5,tokenType2);
        String queryInfo5= multiSign.BalanceByAddr(MULITADD5,tokenType);
        String queryInfo6= multiSign.BalanceByAddr(IMPPUTIONADD,tokenType);
        String queryInfo7= multiSign.BalanceByAddr(IMPPUTIONADD,tokenType2);
        assertThat(queryInfo3, containsString("\"Total\":\"0\""));
        assertThat(queryInfo4, containsString("\"Total\":\"0\""));
        assertThat(queryInfo5, containsString("\"Total\":\"0\""));
        assertThat(queryInfo6, containsString("\"Total\":\"0\""));
        assertThat(queryInfo7, containsString("\"Total\":\"0\""));

        log.info("查询零地址余额");
        String queryInfo8 = multiSign.QueryZero(tokenType);
        String queryInfo9 = multiSign.QueryZero(tokenType2);
        assertThat(queryInfo8, containsString("200"));
        assertThat(queryInfo8, containsString("\"Total\":\"1000\""));
        assertThat(queryInfo9, containsString("200"));
        assertThat(queryInfo9, containsString("\"Total\":\"1000\""));

    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * 多签发行，本地签名
     * @param length
     * @param amount
     * @return
     * @throws Exception
     */
    public String IssueTokenLocalSign(int length, String amount) throws Exception {
        String tokenType = "MT-" + UtilsClass.Random(length);
        String data = "MULITADD1" + "发行" + tokenType + "，数量为：" + amount;
        log.info(data);

        String response = multiSign.issueTokenLocalSign(MULITADD1, tokenType, amount, data);

        String preSignData = JSONObject.fromObject(response).getJSONObject("Data").getString("TxData");
        //log.info(preSignData);

        //log.info("第一次签名");
        String signedData1 = multiIssue.multiSignIssueMethod(preSignData, PRIKEY1PATH);

        //log.info("第二次签名");
        String signedData2 = multiIssue.multiSignIssueMethod(signedData1, PRIKEY2PATH);

       // log.info("第三次签名");
        String signedData3 = multiIssue.multiSignIssueMethod(signedData2, PRIKEY3PATH);
//        log.info("签名结果：" + signedData3);

        multiSign.sendSign(signedData3);

        return tokenType;

    }

    /**
     * 多签转账，本地签名
     * @param fromAddr
     * @param fromPubKey
     * @param data
     * @param fromPriKeyPath
     * @return
     * @throws Exception
     */
    public String multiSignTransfer_LocalSign(String fromAddr, String fromPubKey, String data,
                                              List<Map>tokenList, String fromPriKeyPath) throws Exception {

//        List<Map> listModel = utilsClass.constructToken(toAddr,tokenType,amount);

        String transferInfo= multiSign.TransferLocalSign(fromAddr, fromPubKey, data, tokenList);

        String preSignData = JSONObject.fromObject(transferInfo).getJSONObject("Data").toString();
        //log.info("签名前数据: "+preSignData);

        String signedData = multiTrans.multiSignTransferAccountsMethod(preSignData, fromPriKeyPath);

        //log.info("签名后的数据：" + signedData);

        String response =  multiSign.sendSign(signedData);

        return response;
    }


    public String multiSignRecycle_LocalSign(String fromAddr, String fromPubKey, String tokenType,
                                              String amount, String fromPriKeyPath) throws Exception {

        String recycleResponse = multiSign.RecycleLocalSign(fromAddr, fromPubKey, tokenType, amount);

//        log.info("回收：" + recycleResponse);

        String preSignData = JSONObject.fromObject(recycleResponse).getJSONObject("Data").toString();
//        log.info("签名前数据: " + preSignData);

        String signedData = multiTrans.multiSignTransferAccountsMethod(preSignData, fromPriKeyPath);

//        log.info("签名后的数据：" + signedData);

        String txInfo = multiSign.sendSign(signedData);

//        assertThat("发送交易",txInfo, containsString("200"));

        return txInfo;
    }

}
