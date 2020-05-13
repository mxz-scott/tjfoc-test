package com.tjfintech.common.functionTest.utxoSingleSign;


import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.functionTest.utxoMultiSign.MultiTest_33_12;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.FileOperation.getUTXOLockTime;
import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SoloTest_UnspentTxOutput_test {
    TestBuilder testBuilder= TestBuilder.getInstance();
    SoloSign soloSign =testBuilder.getSoloSign();
    Store store = testBuilder.getStore();
    UtilsClass utilsClass=new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    private static String tokenType;

    int amount ;

    int uxtoLockTime = 300;

    //@Test
    @Before
    public void beforeConfig() throws Exception {
        uxtoLockTime = getUTXOLockTime();
        if(MULITADD1.isEmpty()) {
            BeforeCondition bf = new BeforeCondition();
            bf.updatePubPriKey();
            bf.collAddressTest();//有多签地址创建及添加

            commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                    utilsClass.sdkGetTxDetailType,SLEEPTIME);

        }

        tokenType = "CX-" + UtilsClass.Random(9);
        amount = 222;
        String issData = "ADDRESS4发行 " + tokenType + " token，数量为：" + amount;

        log.info(issData);
        String response1 = soloSign.issueToken(PRIKEY4,tokenType,String.valueOf(amount),"发行token",ADDRESS4);
        assertThat(response1, containsString("200"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        //发行后查询余额
        log.info("发行后查询余额: "+tokenType);
        String queryInfo= soloSign.Balance( PRIKEY4, tokenType);
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
    public void TC2256_TranferFromOneInput1() throws Exception{

        int tf1=100;
        int tf2=110;

        String transferData = "ADDRESS4向ADDRESS1转账 " + tf1 + "*" + tokenType;
        List<Map> list1 = soloSign.constructToken(ADDRESS1,tokenType,String.valueOf(tf1));

        String transferData2 = "ADDRESS4向ADDRESS2转账 " + tf2 + "*" + tokenType;
        List<Map>list2 = soloSign.constructToken(ADDRESS2,tokenType,String.valueOf(tf2));

        //第一笔向Address1转tf1
        String transferInfo= soloSign.Transfer(list1,PRIKEY4, transferData);
        //第一笔向Address2转tf2
        String transferInfo2= soloSign.Transfer(list2,PRIKEY4,transferData2);
        assertEquals("500",JSONObject.fromObject(transferInfo2).getString("State"));
        assertEquals("insufficient balance",JSONObject.fromObject(transferInfo2).getString("Message"));

        String query1= soloSign.Balance(PRIKEY1,tokenType);
        String query2= soloSign.Balance(PRIKEY2,tokenType);
        String query3= soloSign.Balance(PRIKEY4,tokenType);

        assertEquals("200",JSONObject.fromObject(query1).getString("State"));
        assertEquals("200",JSONObject.fromObject(query2).getString("State"));
        assertEquals("200",JSONObject.fromObject(query3).getString("State"));
        assertEquals("0",JSONObject.fromObject(query1).getJSONObject("Data").getString("Total"));
        assertEquals("0",JSONObject.fromObject(query2).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(amount),JSONObject.fromObject(query3).getJSONObject("Data").getString("Total"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(transferInfo,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(
                commonFunc.getTxHash(transferInfo,utilsClass.sdkGetTxHashType01))).getString("State"));

        transferInfo2= soloSign.Transfer(list2,PRIKEY4,transferData2);
        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("State"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(transferInfo2,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        log.info("查询余额判断转账成功与否");
        query1= soloSign.Balance(PRIKEY1,tokenType);
        query2= soloSign.Balance(PRIKEY2,tokenType);
        query3= soloSign.Balance(PRIKEY4,tokenType);

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
    public void TC2257_TranferFromOneInput2() throws Exception{

        int tf1=100;
        int tf2=110;

        //第一笔向Address1转tf1
        String transferData = "ADDRESS4向ADDRESS1转账 " + tf1 + "*" + tokenType;
        List<Map> list1 = soloSign.constructToken(ADDRESS1,tokenType,String.valueOf(tf1));
        String transferInfo= soloSign.Transfer(list1,PRIKEY4, transferData);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("State"));

        //第一笔向Address2转tf2
        String transferData2 = "ADDRESS4向ADDRESS2转账 " + tf2 + "*" + tokenType;
        List<Map>list2 = soloSign.constructToken(ADDRESS2,tokenType,String.valueOf(tf2));
        String transferInfo2= soloSign.Transfer(list2,PRIKEY4,transferData2);
        assertEquals("500",JSONObject.fromObject(transferInfo2).getString("State"));
        assertEquals("insufficient balance",JSONObject.fromObject(transferInfo2).getString("Message"));

        String query1= soloSign.Balance(PRIKEY1,tokenType);
        String query2= soloSign.Balance(PRIKEY2,tokenType);
        String query3= soloSign.Balance(PRIKEY4,tokenType);

        assertEquals("200",JSONObject.fromObject(query1).getString("State"));
        assertEquals("200",JSONObject.fromObject(query2).getString("State"));
        assertEquals("200",JSONObject.fromObject(query3).getString("State"));
        assertEquals("0",JSONObject.fromObject(query1).getJSONObject("Data").getString("Total"));
        assertEquals("0",JSONObject.fromObject(query2).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(amount),JSONObject.fromObject(query3).getJSONObject("Data").getString("Total"));


        sleepAndSaveInfo(uxtoLockTime * 1000,"等待UTXO锁定时间过后");

        transferInfo2= soloSign.Transfer(list2,PRIKEY4,transferData2);
        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("State"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(transferInfo2,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        log.info("查询余额判断转账成功与否");
        query1= soloSign.Balance(PRIKEY1,tokenType);
        query2= soloSign.Balance(PRIKEY2,tokenType);
        query3= soloSign.Balance(PRIKEY4,tokenType);

        assertEquals("200",JSONObject.fromObject(query1).getString("State"));
        assertEquals("200",JSONObject.fromObject(query2).getString("State"));
        assertEquals("200",JSONObject.fromObject(query3).getString("State"));
        assertEquals(String.valueOf(tf1),JSONObject.fromObject(query1).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(tf2),JSONObject.fromObject(query2).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(amount-tf1-tf2),JSONObject.fromObject(query3).getJSONObject("Data").getString("Total"));
    }


    /**
     * 账户存在两个未花费交易 同时向两个账户转出充足余额
     * 可以转账成功
     * @throws Exception
     */
    @Test
    public void TC2258_TwoTranferFromDiffInput1() throws Exception{

        int tf = 100;
        String transferData = "ADDRESS4向ADDRESS5转账"+tf+"*"+tokenType;
        List<Map> list0 = soloSign.constructToken(ADDRESS5,tokenType,String.valueOf(tf));
        log.info(transferData);
        String transfer = soloSign.Transfer(list0,PRIKEY4,transferData);
        assertEquals("200",JSONObject.fromObject(transfer).getString("State"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(transfer,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        //转账后查询余额
        log.info("发行后查询余额: "+tokenType);
        String query1= soloSign.Balance(PRIKEY4,tokenType);
        assertEquals("200",JSONObject.fromObject(query1).getString("State"));
        assertEquals(String.valueOf(amount-tf),JSONObject.fromObject(query1).getJSONObject("Data").getString("Total"));

        String query2= soloSign.Balance(PRIKEY5,tokenType);
        assertEquals("200",JSONObject.fromObject(query2).getString("State"));
        assertEquals(String.valueOf(tf),JSONObject.fromObject(query2).getJSONObject("Data").getString("Total"));

        //ADDRESS5转回80给ADDRESS4 构造ADDRESS4存在两个未花费交易
        int tfA = 80;
        String tfData2 = "ADDRESS5向ADDRESS4转账"+tfA+"*"+tokenType;
        List<Map>list02 = soloSign.constructToken(ADDRESS4,tokenType,String.valueOf(tfA));
        log.info(tfData2);
        String transfer2= soloSign.Transfer(list02,PRIKEY5,tfData2);
        assertEquals("200",JSONObject.fromObject(transfer2).getString("State"));
        
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        //转账后查询余额
        log.info("转账后查询余额: "+tokenType);
        String query3= soloSign.Balance(PRIKEY4,tokenType);
        assertEquals("200",JSONObject.fromObject(query3).getString("State"));
        assertEquals(String.valueOf(amount-tf+tfA),JSONObject.fromObject(query3).getJSONObject("Data").getString("Total"));

        String query4= soloSign.Balance(PRIKEY5,tokenType);
        assertEquals("200",JSONObject.fromObject(query4).getString("State"));
        assertEquals(String.valueOf(tf-tfA),JSONObject.fromObject(query4).getJSONObject("Data").getString("Total"));


        //将从ADDRESS4的两个未花费交易中转出余额
        int tf1=70;
        int tf2=72;

        //第一笔向Address1转tf1
        String transferData1 = "ADDRESS4向ADDRESS1转账"+tf1+"*"+tokenType;
        List<Map>list=soloSign.constructToken(ADDRESS1,tokenType,String.valueOf(tf1));
        log.info(transferData1);

        String transferInfo= soloSign.Transfer(list,PRIKEY4,transferData1);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("State"));


        //第二笔向Address2转tf2
        String transferData2 = "ADDRESS4向ADDRESS2转账"+tf2+"*"+tokenType;
        List<Map>list2=soloSign.constructToken(ADDRESS2,tokenType,String.valueOf(tf2));
        log.info(transferData2);

        String transferInfo2= soloSign.Transfer(list2,PRIKEY4,transferData2);
        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("State"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        log.info("查询余额判断回收成功与否");
        String query6= soloSign.Balance(PRIKEY1,tokenType);
        String query7= soloSign.Balance(PRIKEY2,tokenType);
        String query8= soloSign.Balance(PRIKEY4,tokenType);

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
    public void TC2259_TwoTranferFromDiffInput1() throws Exception{

        //先向ADDRESS5转一半的发行数量
        int tf = 100;
        String transferData = "ADDRESS4向ADDRESS5转账"+tf+"*"+tokenType;
        List<Map>list0=soloSign.constructToken(ADDRESS5,tokenType,String.valueOf(tf));
        log.info(transferData);
        String transfer= soloSign.Transfer(list0,PRIKEY4,transferData);
        assertEquals("200",JSONObject.fromObject(transfer).getString("State"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        //转账后查询余额
        log.info("发行后查询余额: "+tokenType);
        String query1= soloSign.Balance(PRIKEY4,tokenType);
        assertEquals("200",JSONObject.fromObject(query1).getString("State"));
        assertEquals(String.valueOf(amount-tf),JSONObject.fromObject(query1).getJSONObject("Data").getString("Total"));

        String query2= soloSign.Balance(PRIKEY5,tokenType);
        assertEquals("200",JSONObject.fromObject(query2).getString("State"));
        assertEquals(String.valueOf(tf),JSONObject.fromObject(query2).getJSONObject("Data").getString("Total"));


        //ADDRESS5转回80给ADDRESS4
        int tfA = 80;
        String tfData2 = "ADDRESS5向ADDRESS4转账"+tfA+"*"+tokenType;
        List<Map>list02=soloSign.constructToken(ADDRESS4,tokenType,String.valueOf(tfA));
        log.info(tfData2);
        String transfer2= soloSign.Transfer(list02,PRIKEY5,tfData2);
        assertEquals("200",JSONObject.fromObject(transfer2).getString("State"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);
        
        //多笔未花费交易转账测试
        //转账后查询余额
        log.info("转账后查询余额: "+tokenType);
        String query3= soloSign.Balance(PRIKEY4,tokenType);
        assertEquals("200",JSONObject.fromObject(query3).getString("State"));
        assertEquals(String.valueOf(amount-tf+tfA),JSONObject.fromObject(query3).getJSONObject("Data").getString("Total"));

        String query4= soloSign.Balance(PRIKEY5,tokenType);
        assertEquals("200",JSONObject.fromObject(query4).getString("State"));
        assertEquals(String.valueOf(tf-tfA),JSONObject.fromObject(query4).getJSONObject("Data").getString("Total"));

        int tf1=100;
        int tf2=95;

        String transferData1 = "ADDRESS4向ADDRESS1转账"+tf1+"*"+tokenType;
        List<Map>list=soloSign.constructToken(ADDRESS1,tokenType,String.valueOf(tf1));

        String transferData2 = "ADDRESS4向ADDRESS2转账"+tf2+"*"+tokenType;
        List<Map>list2=soloSign.constructToken(ADDRESS2,tokenType,String.valueOf(tf2));


        //第一笔向Address1转tf1
        String transferInfo= soloSign.Transfer(list,PRIKEY4,transferData1);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("State"));

        //第二笔向Address2转tf2 
        String transferInfo2= soloSign.Transfer(list2,PRIKEY4,transferData2);
        assertEquals("500",JSONObject.fromObject(transferInfo2).getString("State"));
        assertEquals("insufficient balance",JSONObject.fromObject(transferInfo2).getString("Message"));


        //等待第一笔交易上链后
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(transferInfo,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        transferInfo2= soloSign.Transfer(list2,PRIKEY4,transferData2);
        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("State"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);
        

        log.info("查询余额判断回收成功与否");
        String query6= soloSign.Balance(PRIKEY1,tokenType);
        String query7= soloSign.Balance(PRIKEY2,tokenType);
        String query8= soloSign.Balance(PRIKEY4,tokenType);

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
    public void TC2260_TwoTranferFromDiffInput1() throws Exception{

        //先转出
        int tf = 142;
        String transferData = "ADDRESS4向ADDRESS5转账"+tf+"*"+tokenType;
        List<Map>list0=soloSign.constructToken(ADDRESS5,tokenType,String.valueOf(tf));
        log.info(transferData);

        String transfer= soloSign.Transfer(list0,PRIKEY4,transferData);
        assertEquals("200",JSONObject.fromObject(transfer).getString("State"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);
        
        //转账后查询余额
        log.info("发行后查询余额: "+tokenType);
        String query1= soloSign.Balance(PRIKEY4,tokenType);
        assertEquals("200",JSONObject.fromObject(query1).getString("State"));
        assertEquals(String.valueOf(amount-tf),JSONObject.fromObject(query1).getJSONObject("Data").getString("Total"));

        String query2= soloSign.Balance(PRIKEY5,tokenType);
        assertEquals("200",JSONObject.fromObject(query2).getString("State"));
        assertEquals(String.valueOf(tf),JSONObject.fromObject(query2).getJSONObject("Data").getString("Total"));


        //ADDRESS5转回110给ADDRESS4

        int tfA = 122;
        String tfData2 = "ADDRESS5向ADDRESS4转账"+tfA+"*"+tokenType;
        List<Map>list02=soloSign.constructToken(ADDRESS4,tokenType,String.valueOf(tfA));
        log.info(tfData2);
        String transfer2= soloSign.Transfer(list02,PRIKEY5,tfData2);
        assertEquals("200",JSONObject.fromObject(transfer2).getString("State"));
        
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        //多笔未花费交易转账测试
        //转账后查询余额
        log.info("转账后查询余额: "+tokenType);
        String query3= soloSign.Balance(PRIKEY4,tokenType);
        assertEquals("200",JSONObject.fromObject(query3).getString("State"));
        assertEquals(String.valueOf(amount-tf+tfA),JSONObject.fromObject(query3).getJSONObject("Data").getString("Total"));

        String query4= soloSign.Balance(PRIKEY5,tokenType);
        assertEquals("200",JSONObject.fromObject(query4).getString("State"));
        assertEquals(String.valueOf(tf-tfA),JSONObject.fromObject(query4).getJSONObject("Data").getString("Total"));

        
        int tf1=100;
        int tf2=95;

        String transferData1 = "ADDRESS4向ADDRESS1转账"+tf1+"*"+tokenType;
        List<Map>list=soloSign.constructToken(ADDRESS1,tokenType,String.valueOf(tf1));

        String transferData2 = "ADDRESS4向ADDRESS2转账"+tf2+"*"+tokenType;
        List<Map>list2=soloSign.constructToken(ADDRESS2,tokenType,String.valueOf(tf2));


        //第一笔向Address1转tf1
        String transferInfo= soloSign.Transfer(list,PRIKEY4,transferData1);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("State"));

        //第二笔向Address2转tf2
        String transferInfo2= soloSign.Transfer(list2,PRIKEY4,transferData2);
        assertEquals("500",JSONObject.fromObject(transferInfo2).getString("State"));
        assertEquals("insufficient balance",JSONObject.fromObject(transferInfo2).getString("Message"));


        //等待第一笔交易上链后
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(transferInfo,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        transferInfo2= soloSign.Transfer(list2,PRIKEY4,transferData2);
        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("State"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        log.info("查询余额判断回收成功与否");
        String query6= soloSign.Balance(PRIKEY1,tokenType);
        String query7= soloSign.Balance(PRIKEY2,tokenType);
        String query8= soloSign.Balance(PRIKEY4,tokenType);

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
    public void TC2261_TwoTranferFromDiffInput1() throws Exception{

        //先转出
        int tf = 152;
        String transferData = "ADDRESS4向ADDRESS5转账"+tf+"*"+tokenType;
        List<Map>list0=soloSign.constructToken(ADDRESS5,tokenType,String.valueOf(tf));
        log.info(transferData);
        String transfer= soloSign.Transfer(list0,PRIKEY4,transferData);
        assertEquals("200",JSONObject.fromObject(transfer).getString("State"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        //转账后查询余额
        log.info("发行后查询余额: "+tokenType);
        String query1= soloSign.Balance(PRIKEY4,tokenType);
        assertEquals("200",JSONObject.fromObject(query1).getString("State"));
        assertEquals(String.valueOf(amount-tf),JSONObject.fromObject(query1).getJSONObject("Data").getString("Total"));

        String query2= soloSign.Balance(PRIKEY5,tokenType);
        assertEquals("200",JSONObject.fromObject(query2).getString("State"));
        assertEquals(String.valueOf(tf),JSONObject.fromObject(query2).getJSONObject("Data").getString("Total"));


        //ADDRESS5转回给ADDRESS4

        int tfA = 100;
        String tfData2 = "ADDRESS5向ADDRESS4转账"+tfA+"*"+tokenType;
        List<Map>list02=soloSign.constructToken(ADDRESS4,tokenType,String.valueOf(tfA));
        String transfer2= soloSign.Transfer(list02,PRIKEY5,tfData2);
        assertEquals("200",JSONObject.fromObject(transfer2).getString("State"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        //多笔未花费交易转账测试
        //转账后查询余额
        log.info("转账后查询余额: "+tokenType);
        String query3= soloSign.Balance(PRIKEY4,tokenType);
        assertEquals("200",JSONObject.fromObject(query3).getString("State"));
        assertEquals(String.valueOf(amount-tf+tfA),JSONObject.fromObject(query3).getJSONObject("Data").getString("Total"));

        String query4= soloSign.Balance(PRIKEY5,tokenType);
        assertEquals("200",JSONObject.fromObject(query4).getString("State"));
        assertEquals(String.valueOf(tf-tfA),JSONObject.fromObject(query4).getJSONObject("Data").getString("Total"));


        //转账时使用带密码私钥,签名顺序4：带密码--带密码
        //-222
        int tf1 = 70;
        int tf2 = 60;
        int tf3 = 10;

        //第一笔向Address1转tf1
        String transferData1 = "ADDRESS4向ADDRESS1转账"+tf1+"*"+tokenType;
        List<Map>list=soloSign.constructToken(ADDRESS1,tokenType,String.valueOf(tf1));
        log.info(transferData1);

        //第二笔向Address2转tf2
        String transferData2 = "ADDRESS4向ADDRESS2转账"+tf2+"*"+tokenType;
        List<Map>list2=soloSign.constructToken(ADDRESS2,tokenType,String.valueOf(tf2));
        log.info(transferData2);

        //第三笔向Address3转tf3
        String transferData3 = "ADDRESS4向ADDRESS3转账"+tf2+"*"+tokenType;
        List<Map>list3=soloSign.constructToken(ADDRESS3,tokenType,String.valueOf(tf3));
        log.info(transferData3);


        String transferInfo= soloSign.Transfer(list,PRIKEY4,transferData1);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("State"));

        String transferInfo2= soloSign.Transfer(list2,PRIKEY4,transferData2);
        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("State"));

        String transferInfo3= soloSign.Transfer(list3,PRIKEY4,transferData3);
        assertEquals("500",JSONObject.fromObject(transferInfo3).getString("State"));
        assertEquals("insufficient balance",JSONObject.fromObject(transferInfo3).getString("Message"));


        //等待第一、二笔交易上链后
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(transferInfo2,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        transferInfo3= soloSign.Transfer(list3,PRIKEY4,transferData3);
        assertEquals("200",JSONObject.fromObject(transferInfo3).getString("State"));


        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(transferInfo3,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        log.info("查询余额判断回收成功与否");
        String query6= soloSign.Balance(PRIKEY1,tokenType);
        String query7= soloSign.Balance(PRIKEY2,tokenType);
        String query9= soloSign.Balance(PRIKEY3,tokenType);
        String query8= soloSign.Balance(PRIKEY4,tokenType);

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
    public void TC2262_TwoTranferFromDiffInput1() throws Exception{

        //先转出
        int tf = 152;
        String transferData = "ADDRESS4向ADDRESS5转账"+tf+"*"+tokenType;
        List<Map>list0=soloSign.constructToken(ADDRESS5,tokenType,String.valueOf(tf));
        log.info(transferData);
        String transfer= soloSign.Transfer(list0,PRIKEY4,transferData);
        assertEquals("200",JSONObject.fromObject(transfer).getString("State"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        //转账后查询余额
        log.info("发行后查询余额: "+tokenType);
        String query1= soloSign.Balance(PRIKEY4,tokenType);
        assertEquals("200",JSONObject.fromObject(query1).getString("State"));
        assertEquals(String.valueOf(amount-tf),JSONObject.fromObject(query1).getJSONObject("Data").getString("Total"));

        String query2= soloSign.Balance(PRIKEY5,tokenType);
        assertEquals("200",JSONObject.fromObject(query2).getString("State"));
        assertEquals(String.valueOf(tf),JSONObject.fromObject(query2).getJSONObject("Data").getString("Total"));


        //ADDRESS5转回给ADDRESS4

        int tfA = 70;
        String tfData2 = "ADDRESS5向ADDRESS4转账"+tfA+"*"+tokenType;
        List<Map>list02=soloSign.constructToken(ADDRESS4,tokenType,String.valueOf(tfA));
        log.info(tfData2);
        String transfer2= soloSign.Transfer(list02,PRIKEY5,tfData2);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(transfer2,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        //再次转回70  账户M3 就有3个未花费交易 每个余额均为70
        transfer2= soloSign.Transfer(list02,PRIKEY5,tfData2);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(transfer2,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        //多笔未花费交易转账测试
        //转账后查询余额
        log.info("转账后查询余额: "+tokenType);
        String query3= soloSign.Balance(PRIKEY4,tokenType);
        assertEquals("200",JSONObject.fromObject(query3).getString("State"));
        assertEquals(String.valueOf(amount-tf+tfA*2),JSONObject.fromObject(query3).getJSONObject("Data").getString("Total"));

        String query4= soloSign.Balance(PRIKEY5,tokenType);
        assertEquals("200",JSONObject.fromObject(query4).getString("State"));
        assertEquals(String.valueOf(tf-tfA*2),JSONObject.fromObject(query4).getJSONObject("Data").getString("Total"));


        //转账时使用带密码私钥,签名顺序4：带密码--带密码
        int tf1 = 70;
        int tf2 = 60;
        int tf3 = 10;

        //第一笔向Address1转tf1
        String transferData1 = "ADDRESS4向ADDRESS1转账"+tf1+"*"+tokenType;
        List<Map>list=soloSign.constructToken(ADDRESS1,tokenType,String.valueOf(tf1));
        log.info(transferData1);

        //第二笔向Address2转tf2
        String transferData2 = "ADDRESS4向ADDRESS2转账"+tf2+"*"+tokenType;
        List<Map>list2=soloSign.constructToken(ADDRESS2,tokenType,String.valueOf(tf2));
        log.info(transferData2);

        //第三笔向Address3转tf3
        String transferData3 = "ADDRESS4向ADDRESS3转账"+tf2+"*"+tokenType;
        List<Map>list3=soloSign.constructToken(ADDRESS3,tokenType,String.valueOf(tf3));
        log.info(transferData3);

        String transferInfo= soloSign.Transfer(list,PRIKEY4,transferData1);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("State"));

        String transferInfo2= soloSign.Transfer(list2,PRIKEY4,transferData2);
        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("State"));

        String transferInfo3= soloSign.Transfer(list3,PRIKEY4,transferData3);
        assertEquals("200",JSONObject.fromObject(transferInfo3).getString("State"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        log.info("查询余额判断回收成功与否");
        String query6= soloSign.Balance(PRIKEY1,tokenType);
        String query7= soloSign.Balance(PRIKEY2,tokenType);
        String query9= soloSign.Balance(PRIKEY3,tokenType);
        String query8= soloSign.Balance(PRIKEY4,tokenType);

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
    public void TC2263_TwoTranferFromDiffInput1() throws Exception{

        //先转出
        int tf = 152;
        String transferData = "ADDRESS4向ADDRESS5转账"+tf+"*"+tokenType;
        List<Map>list0=soloSign.constructToken(ADDRESS5,tokenType,String.valueOf(tf));
        log.info(transferData);
        String transfer= soloSign.Transfer(list0,PRIKEY4,transferData);
        assertEquals("200",JSONObject.fromObject(transfer).getString("State"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        //转账后查询余额
        log.info("发行后查询余额: "+tokenType);
        String query1= soloSign.Balance(PRIKEY4,tokenType);
        assertEquals("200",JSONObject.fromObject(query1).getString("State"));
        assertEquals(String.valueOf(amount-tf),JSONObject.fromObject(query1).getJSONObject("Data").getString("Total"));

        String query2= soloSign.Balance(PRIKEY5,tokenType);
        assertEquals("200",JSONObject.fromObject(query2).getString("State"));
        assertEquals(String.valueOf(tf),JSONObject.fromObject(query2).getJSONObject("Data").getString("Total"));


        //ADDRESS5转回给ADDRESS4

        int tfA = 70;
        String tfData2 = "ADDRESS5向ADDRESS4转账"+tfA+"*"+tokenType;
        List<Map>list02=soloSign.constructToken(ADDRESS4,tokenType,String.valueOf(tfA));
        log.info(tfData2);
        String transfer2= soloSign.Transfer(list02,PRIKEY5,tfData2);
        assertEquals("200",JSONObject.fromObject(transfer2).getString("State"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(transfer2,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        //再次转回70  账户M3 就有3个未花费交易 每个余额均为70
        transfer2= soloSign.Transfer(list02,PRIKEY5,tfData2);
        assertEquals("200",JSONObject.fromObject(transfer2).getString("State"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(transfer2,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        //多笔未花费交易转账测试
        //转账后查询余额
        log.info("转账后查询余额: "+tokenType);
        String query3= soloSign.Balance(PRIKEY4,tokenType);
        assertEquals("200",JSONObject.fromObject(query3).getString("State"));
        assertEquals(String.valueOf(amount-tf+tfA*2),JSONObject.fromObject(query3).getJSONObject("Data").getString("Total"));

        String query4= soloSign.Balance(PRIKEY5,tokenType);
        assertEquals("200",JSONObject.fromObject(query4).getString("State"));
        assertEquals(String.valueOf(tf-tfA*2),JSONObject.fromObject(query4).getJSONObject("Data").getString("Total"));


        //转账时使用带密码私钥,签名顺序4：带密码--带密码
        int tf1 = 10;
        int tf2 = 150;

        //第一笔向Address1转tf1  花费一个未花费交易
        String transferData1 = "ADDRESS4向ADDRESS1转账"+tf1+"*"+tokenType;
        List<Map>list=soloSign.constructToken(ADDRESS1,tokenType,String.valueOf(tf1));
        log.info(transferData1);

        String transferInfo= soloSign.Transfer(list,PRIKEY4,transferData1);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("State"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        //第二笔向Address2转tf2  花费3个未花费交易
        String transferData2 = "ADDRESS4向ADDRESS2转账"+tf2+"*"+tokenType;
        List<Map>list2=soloSign.constructToken(ADDRESS2,tokenType,String.valueOf(tf2));
        log.info(transferData2);

        String transferInfo2= soloSign.Transfer(list2,PRIKEY4,transferData2);
        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("State"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        log.info("查询余额判断回收成功与否");
        String query6= soloSign.Balance(PRIKEY1,tokenType);
        String query7= soloSign.Balance(PRIKEY2,tokenType);
        String query8= soloSign.Balance(PRIKEY4,tokenType);

        assertEquals("200",JSONObject.fromObject(query6).getString("State"));
        assertEquals("200",JSONObject.fromObject(query7).getString("State"));
        assertEquals("200",JSONObject.fromObject(query8).getString("State"));
        assertEquals(String.valueOf(tf1),JSONObject.fromObject(query6).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(tf2),JSONObject.fromObject(query7).getJSONObject("Data").getString("Total"));
        assertEquals(String.valueOf(amount-tf+tfA*2-tf1-tf2),JSONObject.fromObject(query8).getJSONObject("Data").getString("Total"));


    }
}
