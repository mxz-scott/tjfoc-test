package com.tjfintech.common.functionTest.utxoMultiSign;


import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import com.tjfoc.base.MultiSignIssue;
import com.tjfoc.base.MultiSignTransferAccounts;
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


    }


    /**
     * Tc03多签正常流程-发行：签名：查询：转账：查询:回收：查询
     */
    @Test
    public void TC03_multiProgress_LocalSign() throws Exception {
        log.info("发行token1000个");
        tokenType = IssueTokenLocalSign(7, "1000", IMPPUTIONADD);
        Thread.sleep(SLEEPTIME);
        log.info("查询归集地址中token余额");
        String balance = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        assertThat(tokenType + "查询余额错误", balance, containsString("200"));
        assertThat(tokenType + "查询余额不正确", balance, containsString("\"Total\":\"1000\""));


        String transferData = "归集地址向MULITADD4转账10个: " + tokenType;
        log.info(transferData);
        List<Map> transferList = utilsClass.constructToken(MULITADD4, tokenType, "10");
        multiSignTransfer_LocalSign(IMPPUTIONADD, PUBKEY4, transferData, transferList, PRIKEY4); //向单个账号转账

        Thread.sleep(SLEEPTIME);

        log.info("查询归集地址和MULITADD4余额，判断转账是否成功");
        String queryInfo = multiSign.Balance(MULITADD4,PRIKEY1, tokenType);
        assertThat(queryInfo, containsString("200"));
        assertThat(queryInfo, containsString("\"Total\":\"10\""));

        String queryInfo2 = multiSign.Balance(IMPPUTIONADD,PRIKEY4, tokenType);
        assertThat(queryInfo2, containsString("200"));
        assertThat(queryInfo2, containsString("\"Total\":\"990\""));


        log.info("回收归集地址和MULITADD4的token");
        String recycleInfo = multiSignRecycle_LocalSign(IMPPUTIONADD, PUBKEY4, tokenType, "990", PRIKEY4); //单账号回收
        String recycleInfo2 = multiSignRecycle_LocalSign(MULITADD4, PUBKEY1, tokenType, "10", PRIKEY1); //单账号回收
        assertThat(recycleInfo, containsString("200"));
        assertThat(recycleInfo2, containsString("200"));
        Thread.sleep(SLEEPTIME);

        log.info("查询回收后账户余额是否为0");
        String queryInfo3 = multiSign.BalanceByAddr(IMPPUTIONADD, tokenType);
        String queryInfo4 = multiSign.BalanceByAddr(MULITADD5, tokenType);
        assertThat(queryInfo3, containsString("200"));
        assertThat(queryInfo3, containsString("\"Data\":{}"));
        assertThat(queryInfo4, containsString("200"));
        assertThat(queryInfo4, containsString("\"Data\":{}"));

        log.info("查询零地址余额");
        String queryInfo5 = multiSign.QueryZero(tokenType);
        assertThat(queryInfo5, containsString("200"));
        assertThat(queryInfo5, containsString("\"Total\":\"1000\""));

    }



    /**
     * 3/3多签发行(私钥带密码)，发行给1/2签。
     * 1/2签转账和回收（私钥带密码），转给1/2签。
     */
    @Test
    public void TC1420_1425_multiProgress_LocalSign_Pwd() throws Exception {
        log.info("发行token1000个");
        tokenType = IssueTokenLocalSignPwd(7, "1000", IMPPUTIONADD);
        Thread.sleep(SLEEPTIME);
        log.info("查询归集地址中token余额");
        String balance = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        assertThat(tokenType + "查询余额错误", balance, containsString("200"));
        assertThat(tokenType + "查询余额不正确", balance, containsString("\"Total\":\"1000\""));


        String transferData = "归集地址向MULITADD7转账10个: " + tokenType;
        log.info(transferData);
        List<Map> transferList = utilsClass.constructToken(MULITADD7, tokenType, "10");
        multiSignTransfer_LocalSign(IMPPUTIONADD, PUBKEY4, transferData, transferList, PRIKEY4); //向单个账号转账

        Thread.sleep(SLEEPTIME);

        log.info("查询归集地址和MULITADD7余额，判断转账是否成功");
        String queryInfo = multiSign.Balance(MULITADD7, PRIKEY1, tokenType);
        assertThat(queryInfo, containsString("200"));
        assertThat(queryInfo, containsString("\"Total\":\"10\""));

        String queryInfo2 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        assertThat(queryInfo2, containsString("200"));
        assertThat(queryInfo2, containsString("\"Total\":\"990\""));



        String transferData2 = "MULITADD7向MULITADD4转账3个: " + tokenType;
        log.info(transferData2);
        List<Map> transferList2 = utilsClass.constructToken(MULITADD4, tokenType, "3");
        multiSignTransfer_LocalSign(MULITADD7, PUBKEY6, transferData2, transferList2, PRIKEY6, PWD6); //向单个账号转账

        Thread.sleep(SLEEPTIME);

        log.info("查询地址MULITADD7和MULITADD4余额，判断转账是否成功");
        queryInfo = multiSign.Balance(MULITADD7, PRIKEY1, tokenType);
        assertThat(queryInfo, containsString("200"));
        assertThat(queryInfo, containsString("\"Total\":\"7\""));

        queryInfo2 = multiSign.Balance(MULITADD4, PRIKEY1, tokenType);
        assertThat(queryInfo2, containsString("200"));
        assertThat(queryInfo2, containsString("\"Total\":\"3\""));




        log.info("回收归集地址和MULITADD4的token");
        String recycleInfo = multiSignRecycle_LocalSign(IMPPUTIONADD, PUBKEY4, tokenType, "990", PRIKEY4); //单账号回收
        String recycleInfo2 = multiSignRecycle_LocalSign(MULITADD7, PUBKEY6, tokenType, "7", PRIKEY6, PWD6); //单账号回收
        String recycleInfo3 = multiSignRecycle_LocalSign(MULITADD4, PUBKEY1, tokenType, "3", PRIKEY1); //单账号回收
        assertThat(recycleInfo, containsString("200"));
        assertThat(recycleInfo2, containsString("200"));
        assertThat(recycleInfo3, containsString("200"));
        Thread.sleep(SLEEPTIME);

        log.info("查询回收后账户余额是否为0");
        String queryInfo3 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        String queryInfo4 = multiSign.Balance(MULITADD7, PRIKEY1, tokenType);
        String queryInfo6 = multiSign.Balance(MULITADD4, PRIKEY1, tokenType);
        assertThat(queryInfo3, containsString("200"));
        assertThat(queryInfo3, containsString("\"Total\":\"0\""));
        assertThat(queryInfo4, containsString("200"));
        assertThat(queryInfo4, containsString("\"Total\":\"0\""));
        assertThat(queryInfo6, containsString("200"));
        assertThat(queryInfo6, containsString("\"Total\":\"0\""));

        log.info("查询零地址余额");
        String queryInfo5 = multiSign.QueryZero(tokenType);
        assertThat(queryInfo5, containsString("200"));
        assertThat(queryInfo5, containsString("\"Total\":\"1000\""));

    }



    /**
     * 3/3多签发行（私钥带密码），发行给自己。
     *
     */
    @Test
    public void TC1421_multiProgress_LocalSign_Pwd() throws Exception {
        log.info("发行token1000个");
        tokenType = IssueTokenLocalSignPwd(7, "1000");
        Thread.sleep(SLEEPTIME);
        log.info("查询归集地址中token余额");
        String balance = multiSign.Balance(MULITADD3, PRIKEY6, PWD6, tokenType);
        assertThat(tokenType + "查询余额错误", balance, containsString("200"));
        assertThat(tokenType + "查询余额不正确", balance, containsString("\"Total\":\"1000\""));


        String transferData = "归集地址向MULITADD7转账10个: " + tokenType;
        log.info(transferData);
        List<Map> transferList = utilsClass.constructToken(MULITADD7, tokenType, "10");
        multiSignTransfer_LocalSign(MULITADD3, PUBKEY1, transferData, transferList, PRIKEY1); //向单个账号转账

//        Thread.sleep(SLEEPTIME);
//
//        log.info("查询归集地址和MULITADD7余额，判断转账是否成功");
//        String queryInfo = multiSign.Balance(MULITADD7, PRIKEY1, tokenType);
//        assertThat(queryInfo, containsString("200"));
//        assertThat(queryInfo, containsString("\"Total\":\"10\""));
//
//        String queryInfo2 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
//        assertThat(queryInfo2, containsString("200"));
//        assertThat(queryInfo2, containsString("\"Total\":\"990\""));
//
//
//
//        String transferData2 = "MULITADD7向MULITADD4转账3个: " + tokenType;
//        log.info(transferData2);
//        List<Map> transferList2 = utilsClass.constructToken(MULITADD4, tokenType, "3");
//        multiSignTransfer_LocalSign(MULITADD7, PUBKEY6, transferData2, transferList2, PRIKEY6PATH, PWD6); //向单个账号转账
//
//        Thread.sleep(SLEEPTIME);
//
//        log.info("查询地址MULITADD7和MULITADD4余额，判断转账是否成功");
//        queryInfo = multiSign.Balance(MULITADD7, PRIKEY1, tokenType);
//        assertThat(queryInfo, containsString("200"));
//        assertThat(queryInfo, containsString("\"Total\":\"7\""));
//
//        queryInfo2 = multiSign.Balance(MULITADD4, PRIKEY1, tokenType);
//        assertThat(queryInfo2, containsString("200"));
//        assertThat(queryInfo2, containsString("\"Total\":\"3\""));
//
//
//
//
//        log.info("回收归集地址和MULITADD4的token");
//        String recycleInfo = multiSignRecycle_LocalSign(IMPPUTIONADD, PUBKEY4, tokenType, "990", PRIKEY4PATH); //单账号回收
//        String recycleInfo2 = multiSignRecycle_LocalSign(MULITADD7, PUBKEY6, tokenType, "7", PRIKEY6PATH, PWD6); //单账号回收
//        String recycleInfo3 = multiSignRecycle_LocalSign(MULITADD4, PUBKEY1, tokenType, "3", PRIKEY1PATH); //单账号回收
//        assertThat(recycleInfo, containsString("200"));
//        assertThat(recycleInfo2, containsString("200"));
//        assertThat(recycleInfo3, containsString("200"));
//        Thread.sleep(SLEEPTIME);
//
//        log.info("查询回收后账户余额是否为0");
//        String queryInfo3 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
//        String queryInfo4 = multiSign.Balance(MULITADD7, PRIKEY1, tokenType);
//        String queryInfo6 = multiSign.Balance(MULITADD4, PRIKEY1, tokenType);
//        assertThat(queryInfo3, containsString("200"));
//        assertThat(queryInfo3, containsString("\"Total\":\"0\""));
//        assertThat(queryInfo4, containsString("200"));
//        assertThat(queryInfo4, containsString("\"Total\":\"0\""));
//        assertThat(queryInfo6, containsString("200"));
//        assertThat(queryInfo6, containsString("\"Total\":\"0\""));
//
//        log.info("查询零地址余额");
//        String queryInfo5 = multiSign.QueryZero(tokenType);
//        assertThat(queryInfo5, containsString("200"));
//        assertThat(queryInfo5, containsString("\"Total\":\"1000\""));

    }



    /**
     * TC19归集地址向两个多签地址转账
     * 发两种币-查询归集地址-转账两个地址不同token-查询-回收-查询
     *
     * @throws Exception
     */
    @Test
    public void TC19_transferMulti_LocalSign() throws Exception {
        log.info("发行两种token1000个");

        tokenType = IssueTokenLocalSign(7, "1000");
        tokenType2 = IssueTokenLocalSign(8, "1000");
        Thread.sleep(SLEEPTIME);
        log.info("查询归集地址中token余额");
        String balance = multiSign.BalanceByAddr(MULITADD1, tokenType);
        log.info(balance);
        String balance2 = multiSign.BalanceByAddr(MULITADD1, tokenType2);
        log.info(balance2);
        assertThat(tokenType + "查询余额错误", balance, containsString("200"));
        assertThat(tokenType + "查询余额不正确", balance, containsString(tokenType + "\":\"1000\""));
        assertThat(tokenType2 + "查询余额错误", balance2, containsString("200"));
        assertThat(tokenType2 + "查询余额不正确", balance2, containsString(tokenType2 + "\":\"1000\""));

        List<Map> transferList = utilsClass.constructToken(MULITADD4, tokenType, "10");
        List<Map> transferList2 = utilsClass.constructToken(MULITADD5, tokenType2, "10", transferList);
        List<Map> transferList3 = utilsClass.constructToken(MULITADD5, tokenType, "10", transferList);

        String transferData = "归集地址向MULITADD4转账10个" + tokenType + ", 归集地址向MULITADD5转账10个" + tokenType2;
        log.info(transferData);
        String transferInfo = multiSignTransfer_LocalSign(MULITADD1, PUBKEY1, transferData, transferList2, PRIKEY1, PRIKEY2, PRIKEY3);//向两个账号转账，不同币种

        Thread.sleep(SLEEPTIME); //UTXO关系，两笔交易之间需要休眠


        String transferData2 = "归集地址向MULITADD4转账10个" + tokenType + ", 归集地址向MULITADD5转账10个" + tokenType;
        log.info(transferData2);
        String transferInfo2 = multiSignTransfer_LocalSign(MULITADD1, PUBKEY2, transferData2, transferList3, PRIKEY1, PRIKEY2, PRIKEY3);//向两个账号转账，相同币种

        assertThat(transferInfo, containsString("200"));
        assertThat(transferInfo2, containsString("200"));
        Thread.sleep(SLEEPTIME);

        log.info("查询余额判断转账是否成功");
        String queryInfo = multiSign.BalanceByAddr(MULITADD4, tokenType);
        log.info(queryInfo);
        String queryInfo2 = multiSign.BalanceByAddr(MULITADD5, tokenType2);
        log.info(queryInfo2);
        assertThat(queryInfo, containsString("200"));
        assertThat(queryInfo2, containsString("200"));
        assertThat(tokenType + "查询余额不正确", queryInfo, containsString(tokenType + "\":\"20\""));
        assertThat(tokenType2 + "查询余额不正确", queryInfo2, containsString(tokenType2 + "\":\"10\""));

//        assertThat(JSONObject.fromObject(queryInfo).getJSONObject("Data").getString(tokenType), containsString("20"));
//        assertThat(JSONObject.fromObject(queryInfo2).getJSONObject("Data").getString(tokenType), containsString("10"));

        log.info("回收Token");
        String recycleInfo = multiSignRecycle_LocalSign(MULITADD1, PUBKEY3, tokenType, "970", PRIKEY1, PRIKEY2, PRIKEY3);
        String recycleInfo2 = multiSignRecycle_LocalSign(MULITADD1, PUBKEY1, tokenType2, "990", PRIKEY1, PRIKEY2, PRIKEY3);
        String recycleInfo3 = multiSignRecycle_LocalSign(MULITADD4, PUBKEY2, tokenType, "20", PRIKEY2);
        String recycleInfo4 = multiSignRecycle_LocalSign(MULITADD5, PUBKEY1, tokenType2, "10", PRIKEY1);
        String recycleInfo5 = multiSignRecycle_LocalSign(MULITADD5, PUBKEY3, tokenType, "10", PRIKEY3);
        Thread.sleep(SLEEPTIME);
        assertThat(recycleInfo, containsString("200"));
        assertThat(recycleInfo2, containsString("200"));
        assertThat(recycleInfo3, containsString("200"));
        assertThat(recycleInfo4, containsString("200"));
        assertThat(recycleInfo5, containsString("200"));

        log.info("查询余额判断回收成功与否");
        String queryInfo3 = multiSign.BalanceByAddr(MULITADD4, tokenType);
        String queryInfo4 = multiSign.BalanceByAddr(MULITADD5, tokenType2);
        String queryInfo5 = multiSign.BalanceByAddr(MULITADD5, tokenType);
        String queryInfo6 = multiSign.BalanceByAddr(MULITADD1, tokenType);
        String queryInfo7 = multiSign.BalanceByAddr(MULITADD1, tokenType2);
        assertThat(queryInfo3, containsString("\"Data\":{}"));
        assertThat(queryInfo4, containsString("\"Data\":{}"));
        assertThat(queryInfo5, containsString("\"Data\":{}"));
        assertThat(queryInfo6, containsString("\"Data\":{}"));
        assertThat(queryInfo7, containsString("\"Data\":{}"));

        log.info("查询零地址余额");
        String queryInfo8 = multiSign.QueryZero(tokenType);
        String queryInfo9 = multiSign.QueryZero(tokenType2);
        assertThat(queryInfo8, containsString("200"));
        assertThat(queryInfo8, containsString(tokenType + "\":\"1000\""));
        assertThat(queryInfo9, containsString("200"));
        assertThat(queryInfo9, containsString(tokenType2 + "\":\"1000\""));

    }


    /**
     * 多账号同时回收，回收多个多签地址。
     */
    @Test
    public void TC_multiProgress_Recycles() throws Exception {

        log.info("发行token1000个");
        tokenType = IssueTokenLocalSign(7, "1000",IMPPUTIONADD);
        Thread.sleep(SLEEPTIME);
        log.info("查询归集地址中token余额");
        String balance = multiSign.BalanceByAddr(IMPPUTIONADD, tokenType);
        log.info("???"+balance);
        assertThat(tokenType + "查询余额错误", balance, containsString("200"));
//        assertThat(tokenType + "查询余额不正确", balance, containsString("\"Total\":\"1000\""));

        String transferData = "归集地址向MULITADD4转账10个: " + tokenType;
        log.info(transferData);
        List<Map> transferList = utilsClass.constructToken(MULITADD4, tokenType, "10");
        multiSignTransfer_LocalSign(IMPPUTIONADD, PUBKEY4, transferData, transferList, PRIKEY4);

        Thread.sleep(SLEEPTIME);

        log.info("查询归集地址和MULITADD4余额，判断转账是否成功");
        String queryInfo = multiSign.BalanceByAddr(MULITADD4, tokenType);
        assertThat(queryInfo, containsString("200"));
//        assertThat(queryInfo, containsString("\"Total\":\"10\""));

        String queryInfo2 = multiSign.BalanceByAddr(IMPPUTIONADD, tokenType);
        assertThat(queryInfo2, containsString("200"));
//        assertThat(queryInfo2, containsString("\"Total\":\"990\""));


//        log.info("多账号同时回收，回收归集地址和MULITADD4余额");
//        List<Map> recycleList1 = utilsClass.constructToken(IMPPUTIONADD, PUBKEY4, tokenType, "990");
//        List<Map> recycleList2 = utilsClass.constructToken(MULITADD4, PUBKEY1, tokenType, "10", recycleList1);
//
//        String response = multiSign.RecyclesLocalSign(recycleList2);
//
//        log.info("多账号回收："+ response);
//
//        multiSignRecycles(response, "0", PRIKEY4PATH);
//        multiSignRecycles(response, "1", PRIKEY1PATH);
//
//        Thread.sleep(SLEEPTIME);
//
//        log.info("查询回收后账户余额是否为0");
//        String queryInfo3 = multiSign.BalanceByAddr(IMPPUTIONADD, tokenType);
//        String queryInfo4 = multiSign.BalanceByAddr(MULITADD4, tokenType);
//        assertThat(queryInfo3, containsString("200"));
////        assertThat(queryInfo3, containsString("\"Total\":\"0\""));
//        assertThat(queryInfo4, containsString("200"));
////        assertThat(queryInfo4, containsString("\"Total\":\"0\""));
//
//        log.info("查询零地址余额");
//        String queryInfo5 = multiSign.QueryZero(tokenType);
//        assertThat(queryInfo5, containsString("200"));
////        assertThat(queryInfo5, containsString("\"Total\":\"1000\""));

    }

    //------------------------------------------------------------------------------------------------------------------

    /**
     * 多签发行，本地签名
     *
     * @param length
     * @param amount
     * @return
     * @throws Exception
     */
    public String IssueTokenLocalSign(int length, String amount) throws Exception {
        String tokenType = "MT-" + UtilsClass.Random(length);
        String data = "MULITADD1" + "发行" + tokenType + "，数量为：" + amount;
//        log.info(data);

        String response = multiSign.issueTokenLocalSign(MULITADD1, tokenType, amount, data);

//        log.info("发行返回："+response);

        String preSignData = JSONObject.fromObject(response).getJSONObject("Data").getString("TxData");
//        log.info("发行签名前数据："+preSignData);

        log.info("第一次签名");
        String signedData1 = multiIssue.multiSignIssueMethod(preSignData, PRIKEY1);

        log.info("第二次签名");
        String signedData2 = multiIssue.multiSignIssueMethod(signedData1, PRIKEY2);

        log.info("第三次签名");
        String signedData3 = multiIssue.multiSignIssueMethod(signedData2, PRIKEY3);
//        log.info("发行最后签名结果：" + signedData3);

        multiSign.sendSign(signedData3);

        return tokenType;

    }


    /**
     * 多签发行,发行给其他地址，本地签名
     *
     * @param length
     * @param amount
     * @return
     * @throws Exception
     */
    public String IssueTokenLocalSign(int length, String amount, String toAddr) throws Exception {
        String tokenType = "MT-" + UtilsClass.Random(length);
        String data = "MULITADD1" + "发行" + tokenType + "，数量为：" + amount;
//        log.info(data);

        String response = multiSign.issueTokenLocalSign(MULITADD1, toAddr, tokenType, amount, data);

//       log.info("发行返回："+response);

        String preSignData = JSONObject.fromObject(response).getJSONObject("Data").getString("TxData");
//        log.info("发行签名前数据："+preSignData);

        log.info("第一次签名");
        String signedData1 = multiIssue.multiSignIssueMethod(preSignData,PRIKEY1);
//        log.info(signedData1);
        log.info("第二次签名");
        String signedData2 = multiIssue.multiSignIssueMethod(signedData1, PRIKEY2);
//
//        log.info("第三次签名");
        String signedData3 = multiIssue.multiSignIssueMethod(signedData2, PRIKEY3);
//        log.info("发行最后签名结果：" + signedData3);

        multiSign.sendSign(signedData3);

        return tokenType;

    }

    /**
     * 多签转账，本地签名
     *
     * @param fromAddr
     * @param fromPubKey
     * @param data
     * @param fromPriKeyPath
     * @return
     * @throws Exception
     */
    public String multiSignTransfer_LocalSign(String fromAddr, String fromPubKey, String data,List<Map> tokenList,
                                               String fromPriKey1, String fromPriKey2, String fromPriKey3) throws Exception {

//        List<Map> listModel = utilsClass.constructToken(toAddr,tokenType,amount);

        String transferInfo = multiSign.TransferLocalSign(fromAddr, fromPubKey, data, tokenList);

        if (transferInfo.contains("insufficient balance")) {
            return transferInfo;
        }
        String preSignData = JSONObject.fromObject(transferInfo).getJSONObject("Data").toString();
        String signedData1 = multiTrans.multiSignTransferAccountsMethod(preSignData, fromPriKey1);
        String signedData2 = multiTrans.multiSignTransferAccountsMethod(signedData1, fromPriKey2);
        String signedData3 = multiTrans.multiSignTransferAccountsMethod(signedData2, fromPriKey3);
        log.info("多签转账签名后的数据：" + signedData3);
        String response = multiSign.sendSign(signedData3);
        return response;
    }






    /**
     * 回收多签账号余额 - 单笔回收
     *
     * @param fromAddr
     * @param fromPubKey
     * @param tokenType
     * @param amount
     * @param fromPriKeyPath
     * @return
     * @throws Exception
     */
    public String multiSignRecycle_LocalSign(String fromAddr, String fromPubKey, String tokenType,
                                             String amount, String fromPriKeyPath) throws Exception {

        String recycleResponse = multiSign.RecycleLocalSign(fromAddr, fromPubKey, tokenType, amount);

        if (recycleResponse.contains("insufficient balance")) {
            return recycleResponse;
        }

        log.info("回收：" + recycleResponse);

        String preSignData = JSONObject.fromObject(recycleResponse).getJSONObject("Data").toString();
//        log.info("签名前数据: " + preSignData);

        String signedData1 = multiTrans.multiSignTransferAccountsMethod(preSignData, fromPriKeyPath);

        log.info("签名后的数据：" + signedData1);

        String txInfo = multiSign.sendSign(signedData1);

        assertThat("发送交易",txInfo, containsString("200"));

        return txInfo;
    }

    /**
     * 回收多签账号余额-多账号同时回收
     *
     * @param response
     * @param index
     * @param fromPriKeyPath
     * @return
     * @throws Exception
     */
    public String multiSignRecycles(String response, String index, String fromPriKeyPath) throws Exception {

        String key = "mul-tx-" + index;

        String preSignData1 = JSONObject.fromObject(response).getJSONObject("Data").getString(key);
        log.info(preSignData1);

        String signedData = multiTrans.multiSignTransferAccountsMethod(preSignData1, fromPriKeyPath);

//        log.info("签名后的数据：" + signedData);

        String txInfo = multiSign.sendSign(signedData);

        return txInfo;
    }


    /**
     * 多签发行，本地签名
     * 私钥带密码
     * @param length
     * @param amount
     * @return
     * @throws Exception
     */
    public String IssueTokenLocalSignPwd(int length, String amount) throws Exception {
        String tokenType = "MT-" + UtilsClass.Random(length);
        String data = "MULITADD3" + "发行" + tokenType + "，数量为：" + amount;
//        log.info(data);

        String response = multiSign.issueTokenLocalSign(MULITADD3, tokenType, amount, data);

//        log.info("发行返回："+response);

        String preSignData = JSONObject.fromObject(response).getJSONObject("Data").getString("TxData");
//        log.info("发行签名前数据："+preSignData);

        log.info("第一次签名");
        String signedData1 = multiIssue.multiSignIssueMethod(preSignData, PRIKEY6, PWD6);

        log.info("第二次签名");
        String signedData2 = multiIssue.multiSignIssueMethod(signedData1, PRIKEY1);

        log.info("第三次签名");
        String signedData3 = multiIssue.multiSignIssueMethod(signedData2, PRIKEY7, PWD7);
//        log.info("发行最后签名结果：" + signedData3);

        multiSign.sendSign(signedData3);

        return tokenType;

    }

    /**
     * 多签发行，本地签名
     * 私钥带密码
     * @param length
     * @param amount
     * @return
     * @throws Exception
     */
    public String IssueTokenLocalSignPwd(int length, String amount, String toAddr) throws Exception {
        String tokenType = "MT-" + UtilsClass.Random(length);
        String data = "MULITADD3" + "发行" + tokenType + "，数量为：" + amount;
//        log.info(data);

        String response = multiSign.issueTokenLocalSign(MULITADD3, toAddr, tokenType, amount, data);
        System.out.println(response);
//        log.info("发行返回："+response);

        String preSignData = JSONObject.fromObject(response).getJSONObject("Data").getString("TxData");
//        log.info("发行签名前数据："+preSignData);

//        log.info("第一次签名");
        String signedData1 = multiIssue.multiSignIssueMethod(preSignData, PRIKEY6,PWD6);

//        log.info("第二次签名");
        String signedData2 = multiIssue.multiSignIssueMethod(signedData1, PRIKEY1);

//        log.info("第三次签名");
        String signedData3 = multiIssue.multiSignIssueMethod(signedData2, PRIKEY7, PWD7);
//        log.info("发行最后签名结果：" + signedData3);

        multiSign.sendSign(signedData3);

        return tokenType;

    }


    /**
     * 多签转账，本地签名
     * 私钥带密码
     * @param fromAddr
     * @param fromPubKey
     * @param data
     * @param fromPriKeyPath
     * @return
     * @throws Exception
     */
    public String multiSignTransfer_LocalSign(String fromAddr, String fromPubKey, String data,
                                              List<Map> tokenList, String fromPriKeyPath, String pwd) throws Exception {

//        List<Map> listModel = utilsClass.constructToken(toAddr,tokenType,amount);

        String transferInfo = multiSign.TransferLocalSign(fromAddr, fromPubKey, data, tokenList);

        if (transferInfo.contains("insufficient balance")) {
            return transferInfo;
        }

        String preSignData = JSONObject.fromObject(transferInfo).getJSONObject("Data").toString();
//        log.info("多签转账签名前数据: "+preSignData);

        String signedData = multiTrans.multiSignTransferAccountsMethod(preSignData, fromPriKeyPath, pwd);

//        log.info("多签转账签名后的数据：" + signedData);

        String response = multiSign.sendSign(signedData);

        return response;
    }

    /**
     * 多签转账，本地签名
     * 私钥带密码
     * @param fromAddr
     * @param fromPubKey
     * @param data
     * @param fromPriKeyPath
     * @return
     * @throws Exception
     */
    public String multiSignTransfer_LocalSign(String fromAddr, String fromPubKey, String data,
                                              List<Map> tokenList, String fromPriKey) throws Exception {

//        List<Map> listModel = utilsClass.constructToken(toAddr,tokenType,amount);

        String transferInfo = multiSign.TransferLocalSign(fromAddr, fromPubKey, data, tokenList);

        if (transferInfo.contains("insufficient balance")) {
            return transferInfo;
        }

        String preSignData = JSONObject.fromObject(transferInfo).getJSONObject("Data").toString();
//        log.info("多签转账签名前数据: "+preSignData);

        String signedData = multiTrans.multiSignTransferAccountsMethod(preSignData, fromPriKey);

//        log.info("多签转账签名后的数据：" + signedData);

        String response = multiSign.sendSign(signedData);

        return response;
    }

    /**
     * 回收多签账号余额 - 单笔回收
     *私钥带密码
     * @param fromAddr
     * @param fromPubKey
     * @param tokenType
     * @param amount
     * @param fromPriKeyPath
     * @return
     * @throws Exception
     */
    public String multiSignRecycle_LocalSign(String fromAddr, String fromPubKey, String tokenType,
                                             String amount, String fromPriKeyPath, String pwd) throws Exception {

        String recycleResponse = multiSign.RecycleLocalSign(fromAddr, fromPubKey, tokenType, amount);

//        log.info("回收：" + recycleResponse);

        String preSignData = JSONObject.fromObject(recycleResponse).getJSONObject("Data").toString();
//        log.info("签名前数据: " + preSignData);

        String signedData = multiTrans.multiSignTransferAccountsMethod(preSignData, fromPriKeyPath, pwd);

//        log.info("签名后的数据：" + signedData);

        String txInfo = multiSign.sendSign(signedData);

//        assertThat("发送交易",txInfo, containsString("200"));

        return txInfo;
    }


    /**
     * 回收多签账号余额 - 单笔回收
     *私钥带密码
     * @param fromAddr
     * @param fromPubKey
     * @param tokenType
     * @param amount
     * @param fromPriKeyPath
     * @return
     * @throws Exception
     */
    public String multiSignRecycle_LocalSign(String fromAddr, String fromPubKey, String tokenType,String amount,
                                             String fromPriKey1, String fromPriKey2, String fromPriKey3) throws Exception {

        String recycleResponse = multiSign.RecycleLocalSign(fromAddr, fromPubKey, tokenType, amount);

//        log.info("回收：" + recycleResponse);

        String preSignData = JSONObject.fromObject(recycleResponse).getJSONObject("Data").toString();
//        log.info("签名前数据: " + preSignData);

        String signedData = multiTrans.multiSignTransferAccountsMethod(preSignData, fromPriKey1);

        String signedData2 = multiTrans.multiSignTransferAccountsMethod(signedData, fromPriKey2);

        String signedData3 = multiTrans.multiSignTransferAccountsMethod(signedData2, fromPriKey3);

//        log.info("签名后的数据：" + signedData);

        String txInfo = multiSign.sendSign(signedData3);

//        assertThat("发送交易",txInfo, containsString("200"));

        return txInfo;
    }

}
