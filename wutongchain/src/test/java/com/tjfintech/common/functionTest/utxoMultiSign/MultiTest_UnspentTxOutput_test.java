package com.tjfintech.common.functionTest.utxoMultiSign;


import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import static com.tjfintech.common.utils.FileOperation.*;
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
public class MultiTest_UnspentTxOutput_test {
    TestBuilder testBuilder= TestBuilder.getInstance();
    MultiSign multiSign =testBuilder.getMultiSign();
    SoloSign soloSign = testBuilder.getSoloSign();
    Store store = testBuilder.getStore();
    UtilsClass utilsClass=new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    MultiTest_33_12 multiTestSign = new MultiTest_33_12();
    private static String tokenType;
    private static String tokenType2;
    int amount ;

    int uxtoLockTime = 300;

    //@Test
    @Before
    public void beforeConfig() throws Exception {
        uxtoLockTime = getUTXOLockTime();
        log.info("utxo lock time config: " + uxtoLockTime);
        if(MULITADD1.isEmpty()) {
            BeforeCondition bf = new BeforeCondition();
            bf.updatePubPriKey();
            bf.collAddressTest();//有多签地址创建及添加

            commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                    utilsClass.sdkGetTxDetailType,SLEEPTIME);

        }

        tokenType = "CX-" + UtilsClass.Random(9);
        amount = 222;
        String issData = MULITADD3 + "带无密码私钥发行" + tokenType + " token，数量为：" + amount;

        log.info(issData);
        //发行申请带无密码私钥，签名为：不带密码->带密码->带密码
        String response1 = multiSign.issueTokenCarryPri(MULITADD3, tokenType, String.valueOf(amount),PRIKEY1, issData);
        assertThat(response1, containsString("200"));
        String Tx11 = JSONObject.fromObject(response1).getJSONObject("Data").getString("Tx");
        //签名流程1
        String response14 = multiTestSign.SignPro4(Tx11);
        assertThat(JSONObject.fromObject(response14).getJSONObject("Data").getString("IsCompleted"), containsString("true"));
        assertEquals(MULITADD3,JSONObject.fromObject(response14).getJSONObject("Data").getString("CollectAddr"));
        assertThat(response14, containsString("200"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType02),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        //发行后查询余额不带密码
        log.info("发行后不带密码私钥查询余额: "+tokenType);
        String queryInfo= multiSign.BalanceByAddr(MULITADD3,tokenType);
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("State"));
        assertEquals(String.valueOf(amount),JSONObject.fromObject(queryInfo).getJSONObject("Data").getString("Total"));


    }



    /**
     * 3.0版本开始支持若账户存在多个input同时转出（转账时的转出账户来说）
     * 若引用同一个input则一旦发生转账transfer请求则会锁定该input，不管是否签名完全，锁定时间默认5min，最小5s
     * wallet下 存在 UtxoLockTime
     * @throws Exception
     */
    @Test
    public void TC2247_TranferFromOneInput1() throws Exception{

        //转账时使用带密码私钥,签名顺序4：带密码--带密码
        //tf2+tf1 > amount-222
        int tf1=100;
        int tf2=110;

        //第一笔向Address1转tf1
        String transferData1 = "MULITADD3向ADDRESS1转账"+tf1+"*"+tokenType;
        List<Map>list=utilsClass.constructToken(ADDRESS1,tokenType,String.valueOf(tf1));
        log.info(transferData1);

        String transferInfo= multiSign.Transfer(PRIKEY1,transferData1, MULITADD3,list);
        String tfTx21 = JSONObject.fromObject(transferInfo).getJSONObject("Data").getString("Tx");

        //理论上此时已经锁定

        //第二笔向Address2转tf2 tf2+tf1 > amount
        String transferData2 = "MULITADD3向ADDRESS2转账"+tf2+"*"+tokenType;
        List<Map>list2=utilsClass.constructToken(ADDRESS2,tokenType,String.valueOf(tf2));
        log.info(transferData2);

        String transferInfo2= multiSign.Transfer(PRIKEY1,transferData2, MULITADD3,list2);
        assertEquals("400",JSONObject.fromObject(transferInfo2).getString("State"));
        assertEquals("insufficient balance",JSONObject.fromObject(transferInfo2).getString("Message"));

        String query1= multiSign.BalanceByAddr(ADDRESS1,tokenType);
        String query2= multiSign.BalanceByAddr(ADDRESS2,tokenType);
        String query3= multiSign.BalanceByAddr(MULITADD3,tokenType);

        assertEquals("200",JSONObject.fromObject(query1).getString("State"));
        assertEquals("200",JSONObject.fromObject(query2).getString("State"));
        assertEquals("200",JSONObject.fromObject(query3).getString("State"));
        assertEquals("0",JSONObject.fromObject(query1).getJSONObject("Data").getString("Total"));
        assertEquals("0",JSONObject.fromObject(query2).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(amount),JSONObject.fromObject(query3).getJSONObject("Data").getString("Total"));

        //第一笔签名流程4：带密码+带密码
        String tfResponse24 = multiTestSign.SignPro4(tfTx21);

        String tfHash1=JSONObject.fromObject(tfResponse24).getJSONObject("Data").getString("TxId");
        assertEquals("200",JSONObject.fromObject(tfResponse24).getString("State"));
        assertThat(JSONObject.fromObject(tfResponse24).getJSONObject("Data").getString("IsCompleted"), containsString("true"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType02),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(tfHash1)).getString("State"));

        log.info("查询余额判断转账成功与否");
        query1= soloSign.Balance(PRIKEY1,tokenType);
        query2= soloSign.Balance(PRIKEY2,tokenType);
        query3= multiSign.Balance(MULITADD3,PRIKEY1,tokenType);

        assertEquals("200",JSONObject.fromObject(query1).getString("State"));
        assertEquals("200",JSONObject.fromObject(query2).getString("State"));
        assertEquals("200",JSONObject.fromObject(query3).getString("State"));
        assertEquals(String.valueOf(tf1),JSONObject.fromObject(query1).getJSONObject("Data").getString("Total"));
        assertEquals("0",JSONObject.fromObject(query2).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(amount-tf1),JSONObject.fromObject(query3).getJSONObject("Data").getString("Total"));

        //第二笔向Address2转tf2 tf2+tf1 > amount
        transferInfo2= multiSign.Transfer(PRIKEY1,transferData2, MULITADD3,list2);
        String tfTx22 = JSONObject.fromObject(transferInfo2).getJSONObject("Data").getString("Tx");
        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("State"));

        String tfResponse25 = multiTestSign.SignPro4(tfTx22);

        String tfHash2 = JSONObject.fromObject(tfResponse25).getJSONObject("Data").getString("TxId");
        assertEquals("200",JSONObject.fromObject(tfResponse25).getString("State"));
        assertThat(JSONObject.fromObject(tfResponse25).getJSONObject("Data").getString("IsCompleted"), containsString("true"));

        commonFunc.sdkCheckTxOrSleep(tfHash2,
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(tfHash2)).getString("State"));

        log.info("查询余额判断转账成功与否");
        query1= multiSign.BalanceByAddr(ADDRESS1,tokenType);
        query2= multiSign.BalanceByAddr(ADDRESS2,tokenType);
        query3= multiSign.BalanceByAddr(MULITADD3,tokenType);

        assertEquals("200",JSONObject.fromObject(query1).getString("State"));
        assertEquals("200",JSONObject.fromObject(query2).getString("State"));
        assertEquals("200",JSONObject.fromObject(query3).getString("State"));
        assertEquals(String.valueOf(tf1),JSONObject.fromObject(query1).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(tf2),JSONObject.fromObject(query2).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(amount-tf1-tf2),JSONObject.fromObject(query3).getJSONObject("Data").getString("Total"));
    }

    /**
     * 验证utxo锁定后 等待锁定超时后 原未签名完成交易将失效 可以继续发起新的转账交易
     * @throws Exception
     */
    @Test
    public void TC2249_TranferFromOneInput2() throws Exception{

        //转账时使用带密码私钥,签名顺序4：带密码--带密码
        //tf2+tf1 > amount-222
        int tf1=100;
        int tf2=110;

        //第一笔向Address1转tf1
        String transferData1 = "MULITADD3向ADDRESS1转账"+tf1+"*"+tokenType;
        List<Map>list=utilsClass.constructToken(ADDRESS1,tokenType,String.valueOf(tf1));
        log.info(transferData1);

        String transferInfo= multiSign.Transfer(PRIKEY1,transferData1, MULITADD3,list);
        String tfTx21 = JSONObject.fromObject(transferInfo).getJSONObject("Data").getString("Tx");

        //理论上此时已经锁定
        //第二笔向Address2转tf2 tf2+tf1 > amount
        String transferData2 = "MULITADD3向ADDRESS2转账"+tf2+"*"+tokenType;
        List<Map>list2=utilsClass.constructToken(ADDRESS2,tokenType,String.valueOf(tf2));
        log.info(transferData2);

        String transferInfo2= multiSign.Transfer(PRIKEY1,transferData2, MULITADD3,list2);
        assertEquals("400",JSONObject.fromObject(transferInfo2).getString("State"));
        assertEquals("insufficient balance",JSONObject.fromObject(transferInfo2).getString("Message"));

        String query1= multiSign.BalanceByAddr(ADDRESS1,tokenType);
        String query2= multiSign.BalanceByAddr(ADDRESS2,tokenType);
        String query3= multiSign.BalanceByAddr(MULITADD3,tokenType);

        assertEquals("200",JSONObject.fromObject(query1).getString("State"));
        assertEquals("200",JSONObject.fromObject(query2).getString("State"));
        assertEquals("200",JSONObject.fromObject(query3).getString("State"));
        assertEquals("0",JSONObject.fromObject(query1).getJSONObject("Data").getString("Total"));
        assertEquals("0",JSONObject.fromObject(query2).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(amount),JSONObject.fromObject(query3).getJSONObject("Data").getString("Total"));


        sleepAndSaveInfo(uxtoLockTime * 1000,"等待UTXO锁定时间过后");

        //解锁之后向第二个账户转账
        //第二笔向Address2转tf2 tf2+tf1 > amount
        transferInfo2= multiSign.Transfer(PRIKEY1,transferData2, MULITADD3,list2);
        String tfTx22 = JSONObject.fromObject(transferInfo2).getJSONObject("Data").getString("Tx");
        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("State"));

        //超时之后继续签名之前第一笔转账的交易
        String tfResponse24 = multiTestSign.SignPro4(tfTx21);
        assertEquals("200",JSONObject.fromObject(tfResponse24).getString("State"));

        //签名第二笔转账交易
        String tfResponse25 = multiTestSign.SignPro4(tfTx22);
        if(JSONObject.fromObject(tfResponse25).getString("State") != "400"){
            String tfHash2 = JSONObject.fromObject(tfResponse25).getJSONObject("Data").getString("TxId");
            assertEquals("200",JSONObject.fromObject(tfResponse25).getString("State"));
            assertThat(JSONObject.fromObject(tfResponse25).getJSONObject("Data").getString("IsCompleted"), containsString("true"));

            commonFunc.sdkCheckTxOrSleep(tfHash2,
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

            assertEquals("404",JSONObject.fromObject(store.GetTxDetail(tfHash2)).getString("State"));
            log.info("block chain check transfer fail");
        }
        else{
            log.info("SDK check transfer fail");
        }

        log.info("查询余额判断转账成功与否");
        query1= multiSign.BalanceByAddr(ADDRESS1,tokenType);
        query2= multiSign.BalanceByAddr(ADDRESS2,tokenType);
        query3= multiSign.BalanceByAddr(MULITADD3,tokenType);

        assertEquals("200",JSONObject.fromObject(query1).getString("State"));
        assertEquals("200",JSONObject.fromObject(query2).getString("State"));
        assertEquals("200",JSONObject.fromObject(query3).getString("State"));
        assertEquals(String.valueOf(tf1),JSONObject.fromObject(query1).getJSONObject("Data").getString("Total"));
        assertEquals("0",JSONObject.fromObject(query2).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(amount-tf1),JSONObject.fromObject(query3).getJSONObject("Data").getString("Total"));


    }


    /**
     * 账户存在两个未花费交易 同时向两个账户转出充足余额
     * 可以转账成功
     * @throws Exception
     */
    @Test
    public void TC2250_TwoTranferFromDiffInput1() throws Exception{

        //先向MULITADD4转一半的发行数量
        int tf = 100;
        String transferData = "MULITADD3向MULITADD4转账"+tf+"*"+tokenType;
        List<Map>list0=utilsClass.constructToken(MULITADD4,tokenType,String.valueOf(tf));
        log.info(transferData);
        String transfer= multiSign.Transfer(PRIKEY1,transferData, MULITADD3,list0);
        String tfTx = JSONObject.fromObject(transfer).getJSONObject("Data").getString("Tx");
        String tfResp = multiTestSign.SignPro4(tfTx);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType02),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);



        //转账后查询余额不带密码
        log.info("发行后不带密码私钥查询余额: "+tokenType);
        String query1= multiSign.BalanceByAddr(MULITADD3,tokenType);
        assertEquals("200",JSONObject.fromObject(query1).getString("State"));
        assertEquals(String.valueOf(amount-tf),JSONObject.fromObject(query1).getJSONObject("Data").getString("Total"));

        String query2= multiSign.BalanceByAddr(MULITADD4,tokenType);
        assertEquals("200",JSONObject.fromObject(query2).getString("State"));
        assertEquals(String.valueOf(tf),JSONObject.fromObject(query2).getJSONObject("Data").getString("Total"));


        //MULITADD4转回110给MULITADD3

        int tfA = 80;
        String tfData2 = "MULITADD4向MULITADD3转账"+tfA+"*"+tokenType;
        List<Map>list02=utilsClass.constructToken(MULITADD3,tokenType,String.valueOf(tfA));
        log.info(tfData2);
        String transfer2= multiSign.Transfer(PRIKEY1,tfData2, MULITADD4,list02);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType02),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);



        //转账后查询余额不带密码
        log.info("转账后不带密码私钥查询余额: "+tokenType);
        String query3= multiSign.BalanceByAddr(MULITADD3,tokenType);
        assertEquals("200",JSONObject.fromObject(query3).getString("State"));
        assertEquals(String.valueOf(amount-tf+tfA),JSONObject.fromObject(query3).getJSONObject("Data").getString("Total"));

        String query4= multiSign.BalanceByAddr(MULITADD4,tokenType);
        assertEquals("200",JSONObject.fromObject(query4).getString("State"));
        assertEquals(String.valueOf(tf-tfA),JSONObject.fromObject(query4).getJSONObject("Data").getString("Total"));


        //转账时使用带密码私钥,签名顺序4：带密码--带密码
        //tf2+tf1 > amount-222
        int tf1=70;
        int tf2=72;

        //第一笔向Address1转tf1
        String transferData1 = "MULITADD3向ADDRESS1转账"+tf1+"*"+tokenType;
        List<Map>list=utilsClass.constructToken(ADDRESS1,tokenType,String.valueOf(tf1));
        log.info(transferData1);

        String transferInfo= multiSign.Transfer(PRIKEY1,transferData1, MULITADD3,list);
        String tfTx21 = JSONObject.fromObject(transferInfo).getJSONObject("Data").getString("Tx");


        //第二笔向Address2转tf2 tf2+tf1 > amount
        String transferData2 = "MULITADD3向ADDRESS2转账"+tf2+"*"+tokenType;
        List<Map>list2=utilsClass.constructToken(ADDRESS2,tokenType,String.valueOf(tf2));
        log.info(transferData2);

        String transferInfo2= multiSign.Transfer(PRIKEY1,transferData2, MULITADD3,list2);
        String tfTx22 = JSONObject.fromObject(transferInfo2).getJSONObject("Data").getString("Tx");
        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("State"));

        //第一笔签名流程4：带密码+带密码
        String tfResponse24 = multiTestSign.SignPro4(tfTx21);
        String tfResponse25 = multiTestSign.SignPro4(tfTx22);

        String tfHash1=JSONObject.fromObject(tfResponse24).getJSONObject("Data").getString("TxId");
        assertEquals("200",JSONObject.fromObject(tfResponse24).getString("State"));
        assertThat(JSONObject.fromObject(tfResponse24).getJSONObject("Data").getString("IsCompleted"), containsString("true"));


//        assertEquals("mutli transfer failed!",JSONObject.fromObject(tfResponse25).getString("Message"));
        String tfHash2=JSONObject.fromObject(tfResponse25).getJSONObject("Data").getString("TxId");
        assertEquals("200",JSONObject.fromObject(tfResponse25).getString("State"));
        assertThat(JSONObject.fromObject(tfResponse25).getJSONObject("Data").getString("IsCompleted"), containsString("true"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType02),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(tfHash1)).getString("State"));
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(tfHash2)).getString("State"));

        log.info("查询余额判断回收成功与否");
        String query6= soloSign.Balance(PRIKEY1,tokenType);
        String query7= soloSign.Balance(PRIKEY2,tokenType);
        String query8= multiSign.Balance(MULITADD3,PRIKEY1,tokenType);

        assertEquals("200",JSONObject.fromObject(query6).getString("State"));
        assertEquals("200",JSONObject.fromObject(query7).getString("State"));
        assertEquals("200",JSONObject.fromObject(query8).getString("State"));
        assertEquals(String.valueOf(tf1),JSONObject.fromObject(query6).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(tf2),JSONObject.fromObject(query7).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(amount-tf+tfA-tf1-tf2),JSONObject.fromObject(query8).getJSONObject("Data").getString("Total"));


    }


    /**
     * 账户存在两个未花费交易 转出一个后 另外一个余额不足 在第一个转出成功后有充足的余额
     * 可以转账成功
     * @throws Exception
     */
    @Test
    public void TC2251_TwoTranferFromDiffInput1() throws Exception{

        //先向MULITADD4转一半的发行数量
        int tf = 100;
        String transferData = "MULITADD3向MULITADD4转账"+tf+"*"+tokenType;
        List<Map>list0=utilsClass.constructToken(MULITADD4,tokenType,String.valueOf(tf));
        log.info(transferData);
        String transfer= multiSign.Transfer(PRIKEY1,transferData, MULITADD3,list0);
        String tfTx = JSONObject.fromObject(transfer).getJSONObject("Data").getString("Tx");
        String tfResp = multiTestSign.SignPro4(tfTx);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType02),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);



        //转账后查询余额不带密码
        log.info("发行后不带密码私钥查询余额: "+tokenType);
        String query1= multiSign.Balance(MULITADD3,PRIKEY1,tokenType);
        assertEquals("200",JSONObject.fromObject(query1).getString("State"));
        assertEquals(String.valueOf(amount-tf),JSONObject.fromObject(query1).getJSONObject("Data").getString("Total"));

        String query2= multiSign.Balance(MULITADD4,PRIKEY1,tokenType);
        assertEquals("200",JSONObject.fromObject(query2).getString("State"));
        assertEquals(String.valueOf(tf),JSONObject.fromObject(query2).getJSONObject("Data").getString("Total"));


        //MULITADD4转回110给MULITADD3

        int tfA = 80;
        String tfData2 = "MULITADD4向MULITADD3转账"+tfA+"*"+tokenType;
        List<Map>list02=utilsClass.constructToken(MULITADD3,tokenType,String.valueOf(tfA));
        log.info(tfData2);
        String transfer2= multiSign.Transfer(PRIKEY1,tfData2, MULITADD4,list02);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType02),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        //多笔未花费交易转账测试
        //转账后查询余额不带密码
        log.info("转账后不带密码私钥查询余额: "+tokenType);
        String query3= multiSign.Balance(MULITADD3,PRIKEY1,tokenType);
        assertEquals("200",JSONObject.fromObject(query3).getString("State"));
        assertEquals(String.valueOf(amount-tf+tfA),JSONObject.fromObject(query3).getJSONObject("Data").getString("Total"));

        String query4= multiSign.Balance(MULITADD4,PRIKEY1,tokenType);
        assertEquals("200",JSONObject.fromObject(query4).getString("State"));
        assertEquals(String.valueOf(tf-tfA),JSONObject.fromObject(query4).getJSONObject("Data").getString("Total"));


        //转账时使用带密码私钥,签名顺序4：带密码--带密码
        //tf2+tf1 > amount-222
        int tf1=100;
        int tf2=95;

        //第一笔向Address1转tf1
        String transferData1 = "MULITADD3向ADDRESS1转账"+tf1+"*"+tokenType;
        List<Map>list=utilsClass.constructToken(ADDRESS1,tokenType,String.valueOf(tf1));
        log.info(transferData1);

        String transferInfo= multiSign.Transfer(PRIKEY1,transferData1, MULITADD3,list);
        String tfTx21 = JSONObject.fromObject(transferInfo).getJSONObject("Data").getString("Tx");


        //第二笔向Address2转tf2 tf2+tf1 > amount
        String transferData2 = "MULITADD3向ADDRESS2转账"+tf2+"*"+tokenType;
        List<Map>list2=utilsClass.constructToken(ADDRESS2,tokenType,String.valueOf(tf2));
        log.info(transferData2);

        String transferInfo2= multiSign.Transfer(PRIKEY1,transferData2, MULITADD3,list2);
        assertEquals("400",JSONObject.fromObject(transferInfo2).getString("State"));
        assertEquals("insufficient balance",JSONObject.fromObject(transferInfo2).getString("Message"));

        //第一笔签名流程4：带密码+带密码
        String tfResponse24 = multiTestSign.SignPro4(tfTx21);
        String tfHash1=JSONObject.fromObject(tfResponse24).getJSONObject("Data").getString("TxId");
        assertEquals("200",JSONObject.fromObject(tfResponse24).getString("State"));
        assertThat(JSONObject.fromObject(tfResponse24).getJSONObject("Data").getString("IsCompleted"), containsString("true"));

        //发起第二笔转账  第一笔交易可能还未上链 sdk数据库还未更新
        transferInfo2= multiSign.Transfer(PRIKEY1,transferData2, MULITADD3,list2);
//        String tfTx22 = JSONObject.fromObject(transferInfo2).getJSONObject("Data").getString("Tx");
        assertEquals("400",JSONObject.fromObject(transferInfo2).getString("State"));
        assertEquals("insufficient balance",JSONObject.fromObject(transferInfo2).getString("Message"));

        //等待第一笔交易上链后
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(tfResponse24,utilsClass.sdkGetTxHashType02),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        transferInfo2= multiSign.Transfer(PRIKEY1,transferData2, MULITADD3,list2);
        String tfTx22 = JSONObject.fromObject(transferInfo2).getJSONObject("Data").getString("Tx");
        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("State"));

        String tfResponse25 = multiTestSign.SignPro4(tfTx22);
//        assertEquals("mutli transfer failed!",JSONObject.fromObject(tfResponse25).getString("Message"));
        String tfHash2=JSONObject.fromObject(tfResponse25).getJSONObject("Data").getString("TxId");
        assertEquals("200",JSONObject.fromObject(tfResponse25).getString("State"));
        assertThat(JSONObject.fromObject(tfResponse25).getJSONObject("Data").getString("IsCompleted"), containsString("true"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType02),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(tfHash1)).getString("State"));
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(tfHash2)).getString("State"));

        log.info("查询余额判断回收成功与否");
        String query6= soloSign.Balance(PRIKEY1,tokenType);
        String query7= soloSign.Balance(PRIKEY2,tokenType);
        String query8= multiSign.Balance(MULITADD3,PRIKEY1,tokenType);

        assertEquals("200",JSONObject.fromObject(query6).getString("State"));
        assertEquals("200",JSONObject.fromObject(query7).getString("State"));
        assertEquals("200",JSONObject.fromObject(query8).getString("State"));
        assertEquals(String.valueOf(tf1),JSONObject.fromObject(query6).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(tf2),JSONObject.fromObject(query7).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(amount-tf+tfA-tf1-tf2),JSONObject.fromObject(query8).getJSONObject("Data").getString("Total"));


    }

    /**
     * 账户存在两个未花费交易 转出一个后 另外一个余额不足 在第一个转出成功后有充足的余额
     * 可以转账成功
     * @throws Exception
     */
    @Test
    public void TC2252_TwoTranferFromDiffInput1() throws Exception{

        //先转出
        int tf = 142;
        String transferData = "MULITADD3向MULITADD4转账"+tf+"*"+tokenType;
        List<Map>list0=utilsClass.constructToken(MULITADD4,tokenType,String.valueOf(tf));
        log.info(transferData);
        String transfer= multiSign.Transfer(PRIKEY1,transferData, MULITADD3,list0);
        String tfTx = JSONObject.fromObject(transfer).getJSONObject("Data").getString("Tx");
        String tfResp = multiTestSign.SignPro4(tfTx);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType02),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);



        //转账后查询余额不带密码
        log.info("发行后不带密码私钥查询余额: "+tokenType);
        String query1= multiSign.Balance(MULITADD3,PRIKEY1,tokenType);
        assertEquals("200",JSONObject.fromObject(query1).getString("State"));
        assertEquals(String.valueOf(amount-tf),JSONObject.fromObject(query1).getJSONObject("Data").getString("Total"));

        String query2= multiSign.Balance(MULITADD4,PRIKEY1,tokenType);
        assertEquals("200",JSONObject.fromObject(query2).getString("State"));
        assertEquals(String.valueOf(tf),JSONObject.fromObject(query2).getJSONObject("Data").getString("Total"));


        //MULITADD4转回110给MULITADD3

        int tfA = 122;
        String tfData2 = "MULITADD4向MULITADD3转账"+tfA+"*"+tokenType;
        List<Map>list02=utilsClass.constructToken(MULITADD3,tokenType,String.valueOf(tfA));
        log.info(tfData2);
        String transfer2= multiSign.Transfer(PRIKEY1,tfData2, MULITADD4,list02);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType02),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        //多笔未花费交易转账测试
        //转账后查询余额不带密码
        log.info("转账后不带密码私钥查询余额: "+tokenType);
        String query3= multiSign.Balance(MULITADD3,PRIKEY1,tokenType);
        assertEquals("200",JSONObject.fromObject(query3).getString("State"));
        assertEquals(String.valueOf(amount-tf+tfA),JSONObject.fromObject(query3).getJSONObject("Data").getString("Total"));

        String query4= multiSign.Balance(MULITADD4,PRIKEY1,tokenType);
        assertEquals("200",JSONObject.fromObject(query4).getString("State"));
        assertEquals(String.valueOf(tf-tfA),JSONObject.fromObject(query4).getJSONObject("Data").getString("Total"));


        //转账时使用带密码私钥,签名顺序4：带密码--带密码
        //tf2+tf1 > amount-222
        int tf1=100;
        int tf2=95;

        //第一笔向Address1转tf1
        String transferData1 = "MULITADD3向ADDRESS1转账"+tf1+"*"+tokenType;
        List<Map>list=utilsClass.constructToken(ADDRESS1,tokenType,String.valueOf(tf1));
        log.info(transferData1);

        String transferInfo= multiSign.Transfer(PRIKEY1,transferData1, MULITADD3,list);
        String tfTx21 = JSONObject.fromObject(transferInfo).getJSONObject("Data").getString("Tx");


        //第二笔向Address2转tf2 tf2+tf1 > amount
        String transferData2 = "MULITADD3向ADDRESS2转账"+tf2+"*"+tokenType;
        List<Map>list2=utilsClass.constructToken(ADDRESS2,tokenType,String.valueOf(tf2));
        log.info(transferData2);

        String transferInfo2= multiSign.Transfer(PRIKEY1,transferData2, MULITADD3,list2);
        assertEquals("400",JSONObject.fromObject(transferInfo2).getString("State"));
        assertEquals("insufficient balance",JSONObject.fromObject(transferInfo2).getString("Message"));

        //第一笔签名流程4：带密码+带密码
        String tfResponse24 = multiTestSign.SignPro4(tfTx21);
        String tfHash1=JSONObject.fromObject(tfResponse24).getJSONObject("Data").getString("TxId");
        assertEquals("200",JSONObject.fromObject(tfResponse24).getString("State"));
        assertThat(JSONObject.fromObject(tfResponse24).getJSONObject("Data").getString("IsCompleted"), containsString("true"));

        //发起第二笔转账  第一笔交易可能还未上链 sdk数据库还未更新
        transferInfo2= multiSign.Transfer(PRIKEY1,transferData2, MULITADD3,list2);
//        String tfTx22 = JSONObject.fromObject(transferInfo2).getJSONObject("Data").getString("Tx");
        assertEquals("400",JSONObject.fromObject(transferInfo2).getString("State"));
        assertEquals("insufficient balance",JSONObject.fromObject(transferInfo2).getString("Message"));

        //等待第一笔交易上链后
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(tfResponse24,utilsClass.sdkGetTxHashType02),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        transferInfo2= multiSign.Transfer(PRIKEY1,transferData2, MULITADD3,list2);
        String tfTx22 = JSONObject.fromObject(transferInfo2).getJSONObject("Data").getString("Tx");
        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("State"));

        String tfResponse25 = multiTestSign.SignPro4(tfTx22);
//        assertEquals("mutli transfer failed!",JSONObject.fromObject(tfResponse25).getString("Message"));
        String tfHash2=JSONObject.fromObject(tfResponse25).getJSONObject("Data").getString("TxId");
        assertEquals("200",JSONObject.fromObject(tfResponse25).getString("State"));
        assertThat(JSONObject.fromObject(tfResponse25).getJSONObject("Data").getString("IsCompleted"), containsString("true"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType02),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(tfHash1)).getString("State"));
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(tfHash2)).getString("State"));

        log.info("查询余额判断回收成功与否");
        String query6= soloSign.Balance(PRIKEY1,tokenType);
        String query7= soloSign.Balance(PRIKEY2,tokenType);
        String query8= multiSign.Balance(MULITADD3,PRIKEY1,tokenType);

        assertEquals("200",JSONObject.fromObject(query6).getString("State"));
        assertEquals("200",JSONObject.fromObject(query7).getString("State"));
        assertEquals("200",JSONObject.fromObject(query8).getString("State"));
        assertEquals(String.valueOf(tf1),JSONObject.fromObject(query6).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(tf2),JSONObject.fromObject(query7).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(amount-tf+tfA-tf1-tf2),JSONObject.fromObject(query8).getJSONObject("Data").getString("Total"));


    }


    /**
     * 账户存在两个未花费交易 转给三个账户 转两次余额均充足 第三次需要前两次执行完成后再执行 可以成功 总余额充足
     * 可以转账成功
     * @throws Exception
     */
    @Test
    public void TC2253_TwoTranferFromDiffInput1() throws Exception{

        //先转出
        int tf = 152;
        String transferData = "MULITADD3向MULITADD4转账"+tf+"*"+tokenType;
        List<Map>list0=utilsClass.constructToken(MULITADD4,tokenType,String.valueOf(tf));
        log.info(transferData);
        String transfer= multiSign.Transfer(PRIKEY1,transferData, MULITADD3,list0);
        String tfTx = JSONObject.fromObject(transfer).getJSONObject("Data").getString("Tx");
        String tfResp = multiTestSign.SignPro4(tfTx);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType02),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);



        //转账后查询余额不带密码
        log.info("发行后不带密码私钥查询余额: "+tokenType);
        String query1= multiSign.Balance(MULITADD3,PRIKEY1,tokenType);
        assertEquals("200",JSONObject.fromObject(query1).getString("State"));
        assertEquals(String.valueOf(amount-tf),JSONObject.fromObject(query1).getJSONObject("Data").getString("Total"));

        String query2= multiSign.Balance(MULITADD4,PRIKEY1,tokenType);
        assertEquals("200",JSONObject.fromObject(query2).getString("State"));
        assertEquals(String.valueOf(tf),JSONObject.fromObject(query2).getJSONObject("Data").getString("Total"));


        //MULITADD4转回给MULITADD3

        int tfA = 100;
        String tfData2 = "MULITADD4向MULITADD3转账"+tfA+"*"+tokenType;
        List<Map>list02=utilsClass.constructToken(MULITADD3,tokenType,String.valueOf(tfA));
        log.info(tfData2);
        String transfer2= multiSign.Transfer(PRIKEY1,tfData2, MULITADD4,list02);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType02),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        //多笔未花费交易转账测试
        //转账后查询余额不带密码
        log.info("转账后不带密码私钥查询余额: "+tokenType);
        String query3= multiSign.Balance(MULITADD3,PRIKEY1,tokenType);
        assertEquals("200",JSONObject.fromObject(query3).getString("State"));
        assertEquals(String.valueOf(amount-tf+tfA),JSONObject.fromObject(query3).getJSONObject("Data").getString("Total"));

        String query4= multiSign.Balance(MULITADD4,PRIKEY1,tokenType);
        assertEquals("200",JSONObject.fromObject(query4).getString("State"));
        assertEquals(String.valueOf(tf-tfA),JSONObject.fromObject(query4).getJSONObject("Data").getString("Total"));


        //转账时使用带密码私钥,签名顺序4：带密码--带密码
        //tf2+tf1 > amount-222
        int tf1 = 70;
        int tf2 = 60;
        int tf3 = 10;

        //第一笔向Address1转tf1
        String transferData1 = "MULITADD3向ADDRESS1转账"+tf1+"*"+tokenType;
        List<Map>list=utilsClass.constructToken(ADDRESS1,tokenType,String.valueOf(tf1));
        log.info(transferData1);

        String transferInfo= multiSign.Transfer(PRIKEY1,transferData1, MULITADD3,list);
        String tfTx21 = JSONObject.fromObject(transferInfo).getJSONObject("Data").getString("Tx");


        //第二笔向Address2转tf2
        String transferData2 = "MULITADD3向ADDRESS2转账"+tf2+"*"+tokenType;
        List<Map>list2=utilsClass.constructToken(ADDRESS2,tokenType,String.valueOf(tf2));
        log.info(transferData2);

        String transferInfo2= multiSign.Transfer(PRIKEY1,transferData2, MULITADD3,list2);
        String tfTx22 = JSONObject.fromObject(transferInfo2).getJSONObject("Data").getString("Tx");

        //第三笔向Address3转tf3
        String transferData3 = "MULITADD3向ADDRESS3转账"+tf2+"*"+tokenType;
        List<Map>list3=utilsClass.constructToken(ADDRESS3,tokenType,String.valueOf(tf3));
        log.info(transferData3);

        String transferInfo3= multiSign.Transfer(PRIKEY1,transferData3, MULITADD3,list3);
        assertEquals("400",JSONObject.fromObject(transferInfo3).getString("State"));
        assertEquals("insufficient balance",JSONObject.fromObject(transferInfo3).getString("Message"));

        //第一笔签名流程4：带密码+带密码
        String tfResponse24 = multiTestSign.SignPro4(tfTx21);
        String tfHash1=JSONObject.fromObject(tfResponse24).getJSONObject("Data").getString("TxId");
        assertEquals("200",JSONObject.fromObject(tfResponse24).getString("State"));
        assertThat(JSONObject.fromObject(tfResponse24).getJSONObject("Data").getString("IsCompleted"), containsString("true"));

        //发起第二笔转账签名：带密码+带密码
        String tfResponse25 = multiTestSign.SignPro4(tfTx22);
        String tfHash2=JSONObject.fromObject(tfResponse25).getJSONObject("Data").getString("TxId");
        assertEquals("200",JSONObject.fromObject(tfResponse25).getString("State"));
        assertThat(JSONObject.fromObject(tfResponse25).getJSONObject("Data").getString("IsCompleted"), containsString("true"));

        //第一二笔交易未上链时 仍然会提示余额不足
        transferInfo3 = multiSign.Transfer(PRIKEY1,transferData3, MULITADD3,list3);
        assertEquals("400",JSONObject.fromObject(transferInfo3).getString("State"));
        assertEquals("insufficient balance",JSONObject.fromObject(transferInfo3).getString("Message"));

        //等待第一、二笔交易上链后
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(tfResponse24,utilsClass.sdkGetTxHashType02),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        transferInfo3= multiSign.Transfer(PRIKEY1,transferData3, MULITADD3,list3);
        String tfTx23 = JSONObject.fromObject(transferInfo3).getJSONObject("Data").getString("Tx");
        assertEquals("200",JSONObject.fromObject(transferInfo3).getString("State"));

        String tfResponse26 = multiTestSign.SignPro4(tfTx23);
        String tfHash3=JSONObject.fromObject(tfResponse26).getJSONObject("Data").getString("TxId");
        assertEquals("200",JSONObject.fromObject(tfResponse26).getString("State"));
        assertThat(JSONObject.fromObject(tfResponse26).getJSONObject("Data").getString("IsCompleted"), containsString("true"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType02),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(tfHash1)).getString("State"));
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(tfHash2)).getString("State"));
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(tfHash3)).getString("State"));

        log.info("查询余额判断回收成功与否");
        String query6= soloSign.Balance(PRIKEY1,tokenType);
        String query7= soloSign.Balance(PRIKEY2,tokenType);
        String query9= soloSign.Balance(PRIKEY3,tokenType);
        String query8= multiSign.Balance(MULITADD3,PRIKEY1,tokenType);

        assertEquals("200",JSONObject.fromObject(query6).getString("State"));
        assertEquals("200",JSONObject.fromObject(query7).getString("State"));
        assertEquals("200",JSONObject.fromObject(query8).getString("State"));
        assertEquals("200",JSONObject.fromObject(query9).getString("State"));
        assertEquals(String.valueOf(tf1),JSONObject.fromObject(query6).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(tf2),JSONObject.fromObject(query7).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(tf3),JSONObject.fromObject(query9).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(amount-tf+tfA-tf1-tf2-tf3),JSONObject.fromObject(query8).getJSONObject("Data").getString("Total"));


    }


    /**
     * 账户存在三个未花费交易 转给三个账户 每次均有充足余额
     * 可以转账成功
     * @throws Exception
     */
    @Test
    public void TC2254_TwoTranferFromDiffInput1() throws Exception{

        //先转出
        int tf = 152;
        String transferData = "MULITADD3向MULITADD4转账"+tf+"*"+tokenType;
        List<Map>list0=utilsClass.constructToken(MULITADD4,tokenType,String.valueOf(tf));
        log.info(transferData);
        String transfer= multiSign.Transfer(PRIKEY1,transferData, MULITADD3,list0);
        String tfTx = JSONObject.fromObject(transfer).getJSONObject("Data").getString("Tx");
        String tfResp = multiTestSign.SignPro4(tfTx);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType02),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);



        //转账后查询余额不带密码
        log.info("发行后不带密码私钥查询余额: "+tokenType);
        String query1= multiSign.Balance(MULITADD3,PRIKEY1,tokenType);
        assertEquals("200",JSONObject.fromObject(query1).getString("State"));
        assertEquals(String.valueOf(amount-tf),JSONObject.fromObject(query1).getJSONObject("Data").getString("Total"));

        String query2= multiSign.Balance(MULITADD4,PRIKEY1,tokenType);
        assertEquals("200",JSONObject.fromObject(query2).getString("State"));
        assertEquals(String.valueOf(tf),JSONObject.fromObject(query2).getJSONObject("Data").getString("Total"));


        //MULITADD4转回给MULITADD3

        int tfA = 70;
        String tfData2 = "MULITADD4向MULITADD3转账"+tfA+"*"+tokenType;
        List<Map>list02=utilsClass.constructToken(MULITADD3,tokenType,String.valueOf(tfA));
        log.info(tfData2);
        String transfer2= multiSign.Transfer(PRIKEY1,tfData2, MULITADD4,list02);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(transfer2,utilsClass.sdkGetTxHashType02),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        //再次转回70  账户M3 就有3个未花费交易 每个余额均为70
        transfer2= multiSign.Transfer(PRIKEY1,tfData2, MULITADD4,list02);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(transfer2,utilsClass.sdkGetTxHashType02),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        //多笔未花费交易转账测试
        //转账后查询余额不带密码
        log.info("转账后不带密码私钥查询余额: "+tokenType);
        String query3= multiSign.Balance(MULITADD3,PRIKEY1,tokenType);
        assertEquals("200",JSONObject.fromObject(query3).getString("State"));
        assertEquals(String.valueOf(amount-tf+tfA*2),JSONObject.fromObject(query3).getJSONObject("Data").getString("Total"));

        String query4= multiSign.Balance(MULITADD4,PRIKEY1,tokenType);
        assertEquals("200",JSONObject.fromObject(query4).getString("State"));
        assertEquals(String.valueOf(tf-tfA*2),JSONObject.fromObject(query4).getJSONObject("Data").getString("Total"));


        //转账时使用带密码私钥,签名顺序4：带密码--带密码
        int tf1 = 70;
        int tf2 = 60;
        int tf3 = 10;

        //第一笔向Address1转tf1
        String transferData1 = "MULITADD3向ADDRESS1转账"+tf1+"*"+tokenType;
        List<Map>list=utilsClass.constructToken(ADDRESS1,tokenType,String.valueOf(tf1));
        log.info(transferData1);

        String transferInfo= multiSign.Transfer(PRIKEY1,transferData1, MULITADD3,list);
        String tfTx21 = JSONObject.fromObject(transferInfo).getJSONObject("Data").getString("Tx");


        //第二笔向Address2转tf2
        String transferData2 = "MULITADD3向ADDRESS2转账"+tf2+"*"+tokenType;
        List<Map>list2=utilsClass.constructToken(ADDRESS2,tokenType,String.valueOf(tf2));
        log.info(transferData2);

        String transferInfo2= multiSign.Transfer(PRIKEY1,transferData2, MULITADD3,list2);
        String tfTx22 = JSONObject.fromObject(transferInfo2).getJSONObject("Data").getString("Tx");

        //第三笔向Address3转tf3
        String transferData3 = "MULITADD3向ADDRESS3转账"+tf2+"*"+tokenType;
        List<Map>list3=utilsClass.constructToken(ADDRESS3,tokenType,String.valueOf(tf3));
        log.info(transferData3);

        String transferInfo3= multiSign.Transfer(PRIKEY1,transferData3, MULITADD3,list3);
        String tfTx23 = JSONObject.fromObject(transferInfo3).getJSONObject("Data").getString("Tx");

        //第一笔签名流程4：带密码+带密码
        String tfResponse24 = multiTestSign.SignPro4(tfTx21);
        String tfHash1=JSONObject.fromObject(tfResponse24).getJSONObject("Data").getString("TxId");
        assertEquals("200",JSONObject.fromObject(tfResponse24).getString("State"));
        assertThat(JSONObject.fromObject(tfResponse24).getJSONObject("Data").getString("IsCompleted"), containsString("true"));

        //发起第二笔转账签名：带密码+带密码
        String tfResponse25 = multiTestSign.SignPro4(tfTx22);
        String tfHash2=JSONObject.fromObject(tfResponse25).getJSONObject("Data").getString("TxId");
        assertEquals("200",JSONObject.fromObject(tfResponse25).getString("State"));
        assertThat(JSONObject.fromObject(tfResponse25).getJSONObject("Data").getString("IsCompleted"), containsString("true"));

        //发起第三笔转账签名：带密码+带密码
        String tfResponse26 = multiTestSign.SignPro4(tfTx23);
        String tfHash3=JSONObject.fromObject(tfResponse26).getJSONObject("Data").getString("TxId");
        assertEquals("200",JSONObject.fromObject(tfResponse26).getString("State"));
        assertThat(JSONObject.fromObject(tfResponse26).getJSONObject("Data").getString("IsCompleted"), containsString("true"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType02),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(tfHash1)).getString("State"));
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(tfHash2)).getString("State"));
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(tfHash3)).getString("State"));

        log.info("查询余额判断回收成功与否");
        String query6= soloSign.Balance(PRIKEY1,tokenType);
        String query7= soloSign.Balance(PRIKEY2,tokenType);
        String query9= soloSign.Balance(PRIKEY3,tokenType);
        String query8= multiSign.Balance(MULITADD3,PRIKEY1,tokenType);

        assertEquals("200",JSONObject.fromObject(query6).getString("State"));
        assertEquals("200",JSONObject.fromObject(query7).getString("State"));
        assertEquals("200",JSONObject.fromObject(query8).getString("State"));
        assertEquals("200",JSONObject.fromObject(query9).getString("State"));
        assertEquals(String.valueOf(tf1),JSONObject.fromObject(query6).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(tf2),JSONObject.fromObject(query7).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(tf3),JSONObject.fromObject(query9).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(amount-tf+tfA*2-tf1-tf2-tf3),JSONObject.fromObject(query8).getJSONObject("Data").getString("Total"));


    }

    /**
     * 账户存在三个未花费交易 先转给一个账户 花费一个未花费 再转给一个账户花费三个未花费
     * 可以转账成功
     * @throws Exception
     */
    @Test
    public void TC2255_TwoTranferFromDiffInput1() throws Exception{

        //先转出
        int tf = 152;
        String transferData = "MULITADD3向MULITADD4转账"+tf+"*"+tokenType;
        List<Map>list0=utilsClass.constructToken(MULITADD4,tokenType,String.valueOf(tf));
        log.info(transferData);
        String transfer= multiSign.Transfer(PRIKEY1,transferData, MULITADD3,list0);
        String tfTx = JSONObject.fromObject(transfer).getJSONObject("Data").getString("Tx");
        String tfResp = multiTestSign.SignPro4(tfTx);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType02),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);



        //转账后查询余额不带密码
        log.info("发行后不带密码私钥查询余额: "+tokenType);
        String query1= multiSign.Balance(MULITADD3,PRIKEY1,tokenType);
        assertEquals("200",JSONObject.fromObject(query1).getString("State"));
        assertEquals(String.valueOf(amount-tf),JSONObject.fromObject(query1).getJSONObject("Data").getString("Total"));

        String query2= multiSign.Balance(MULITADD4,PRIKEY1,tokenType);
        assertEquals("200",JSONObject.fromObject(query2).getString("State"));
        assertEquals(String.valueOf(tf),JSONObject.fromObject(query2).getJSONObject("Data").getString("Total"));


        //MULITADD4转回给MULITADD3

        int tfA = 70;
        String tfData2 = "MULITADD4向MULITADD3转账"+tfA+"*"+tokenType;
        List<Map>list02=utilsClass.constructToken(MULITADD3,tokenType,String.valueOf(tfA));
        log.info(tfData2);
        String transfer2= multiSign.Transfer(PRIKEY1,tfData2, MULITADD4,list02);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(transfer2,utilsClass.sdkGetTxHashType02),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        //再次转回70  账户M3 就有3个未花费交易 每个余额均为70
        transfer2= multiSign.Transfer(PRIKEY1,tfData2, MULITADD4,list02);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(transfer2,utilsClass.sdkGetTxHashType02),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        //多笔未花费交易转账测试
        //转账后查询余额不带密码
        log.info("转账后不带密码私钥查询余额: "+tokenType);
        String query3= multiSign.Balance(MULITADD3,PRIKEY1,tokenType);
        assertEquals("200",JSONObject.fromObject(query3).getString("State"));
        assertEquals(String.valueOf(amount-tf+tfA*2),JSONObject.fromObject(query3).getJSONObject("Data").getString("Total"));

        String query4= multiSign.Balance(MULITADD4,PRIKEY1,tokenType);
        assertEquals("200",JSONObject.fromObject(query4).getString("State"));
        assertEquals(String.valueOf(tf-tfA*2),JSONObject.fromObject(query4).getJSONObject("Data").getString("Total"));


        //转账时使用带密码私钥,签名顺序4：带密码--带密码
        int tf1 = 10;
        int tf2 = 150;

        //第一笔向Address1转tf1  花费一个未花费交易
        String transferData1 = "MULITADD3向ADDRESS1转账"+tf1+"*"+tokenType;
        List<Map>list=utilsClass.constructToken(ADDRESS1,tokenType,String.valueOf(tf1));
        log.info(transferData1);

        String transferInfo= multiSign.Transfer(PRIKEY1,transferData1, MULITADD3,list);
        String tfTx21 = JSONObject.fromObject(transferInfo).getJSONObject("Data").getString("Tx");
        //第一笔签名流程4：带密码+带密码
        String tfResponse24 = multiTestSign.SignPro4(tfTx21);
        String tfHash1=JSONObject.fromObject(tfResponse24).getJSONObject("Data").getString("TxId");
        assertEquals("200",JSONObject.fromObject(tfResponse24).getString("State"));
        assertThat(JSONObject.fromObject(tfResponse24).getJSONObject("Data").getString("IsCompleted"), containsString("true"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType02),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        //第二笔向Address2转tf2  花费3个未花费交易
        String transferData2 = "MULITADD3向ADDRESS2转账"+tf2+"*"+tokenType;
        List<Map>list2=utilsClass.constructToken(ADDRESS2,tokenType,String.valueOf(tf2));
        log.info(transferData2);

        String transferInfo2= multiSign.Transfer(PRIKEY1,transferData2, MULITADD3,list2);
        String tfTx22 = JSONObject.fromObject(transferInfo2).getJSONObject("Data").getString("Tx");


        //发起第二笔转账签名：带密码+带密码
        String tfResponse25 = multiTestSign.SignPro4(tfTx22);
        String tfHash2=JSONObject.fromObject(tfResponse25).getJSONObject("Data").getString("TxId");
        assertEquals("200",JSONObject.fromObject(tfResponse25).getString("State"));
        assertThat(JSONObject.fromObject(tfResponse25).getJSONObject("Data").getString("IsCompleted"), containsString("true"));



        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType02),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(tfHash1)).getString("State"));
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(tfHash2)).getString("State"));

        log.info("查询余额判断回收成功与否");
        String query6= soloSign.Balance(PRIKEY1,tokenType);
        String query7= soloSign.Balance(PRIKEY2,tokenType);
        String query8= multiSign.Balance(MULITADD3,PRIKEY1,tokenType);

        assertEquals("200",JSONObject.fromObject(query6).getString("State"));
        assertEquals("200",JSONObject.fromObject(query7).getString("State"));
        assertEquals("200",JSONObject.fromObject(query8).getString("State"));
        assertEquals(String.valueOf(tf1),JSONObject.fromObject(query6).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(tf2),JSONObject.fromObject(query7).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(amount-tf+tfA*2-tf1-tf2),JSONObject.fromObject(query8).getJSONObject("Data").getString("Total"));


    }
}
