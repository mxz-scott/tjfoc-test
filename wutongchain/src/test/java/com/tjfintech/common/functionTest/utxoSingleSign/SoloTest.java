package com.tjfintech.common.functionTest.utxoSingleSign;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.TestBuilder;
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

//import static com.tjfintech.common.functionTest.store.StoreTest.SLEEPTIME;
import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SoloTest {
    TestBuilder testBuilder= TestBuilder.getInstance();
    MultiSign multiSign =testBuilder.getMultiSign();
    SoloSign soloSign = testBuilder.getSoloSign();
    UtilsClass utilsClass=new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    private static String tokenType;
    private static String tokenType2;

    private static String issueAmount1;
    private static String issueAmount2;

    private static String actualAmount1;
    private static String actualAmount2;

    @BeforeClass
    public static void beforeClass() throws Exception {
        CommonFunc commonFuncTe = new CommonFunc();
        UtilsClass utilsClassTe = new UtilsClass();
        if (MULITADD1.isEmpty()) {
            BeforeCondition bf = new BeforeCondition();
            bf.updatePubPriKey();
            bf.collAddressTest();//有多签地址创建及添加
            commonFuncTe.sdkCheckTxOrSleep(commonFuncTe.getTxHash(globalResponse,utilsClassTe.sdkGetTxHashType01),
                    utilsClassTe.sdkGetTxDetailType,SLEEPTIME);
        }
    }

    @Before
    public void beforeConfig() throws Exception {

        issueAmount1 = "10000.12345678912345";
        issueAmount2 = "20000.876543212345";

        if (UtilsClass.PRECISION == 10) {
            actualAmount1 = "10000.1234567891";
            actualAmount2 = "20000.8765432123";
        }else {
            actualAmount1 = "10000.123456";
            actualAmount2 = "20000.876543";
        }

        log.info("发行两种token");
        tokenType = "SOLOTC-"+UtilsClass.Random(6);
        String isResult= soloSign.issueToken(PRIKEY1,tokenType,issueAmount1,"发行token",ADDRESS1);
        //Thread.sleep(SLEEPTIME);
        tokenType2 = "SOLOTC-"+UtilsClass.Random(6);
        String isResult2= soloSign.issueToken(PRIKEY1,tokenType2,issueAmount2,"发行token",ADDRESS1);
        assertThat(tokenType+"发行token错误",isResult, containsString("200"));
        assertThat(tokenType+"发行token错误",isResult2, containsString("200"));

//        Thread.sleep(SLEEPTIME);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        log.info("查询归集地址中两种token余额");
        String response1 = soloSign.Balance( PRIKEY1, tokenType);
        String response2 = soloSign.Balance( PRIKEY1, tokenType2);

        assertThat(tokenType+"查询余额错误",response1, containsString("200"));
        assertThat(tokenType+"查询余额错误",response2, containsString("200"));
        assertThat(tokenType+"查询余额不正确",response1, containsString(actualAmount1));
        assertThat(tokenType+"查询余额不正确",response2, containsString(actualAmount2));
    }

    /**
     *  测试最大发行量
     *
     */
    @Test
    public void TC001_TestMaxValue()throws Exception {

        if (UtilsClass.PRECISION == 10) {
            actualAmount1 = "1844674407";
        }else {
            actualAmount1 = "18446744073709";
        }

        tokenType = "SOLOTC-"+UtilsClass.Random(6);

        String isResult = soloSign.issueToken(PRIKEY1,tokenType,actualAmount1,"发行token",ADDRESS1);
        assertThat(isResult, containsString("200"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        log.info("查询归集地址中token余额");
        String response1 = soloSign.Balance( PRIKEY1, tokenType);

        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
        assertEquals(actualAmount1,JSONObject.fromObject(response1).getJSONObject("data").getString("total"));
    }

    /**
     * 单签发行检查发行地址注册、未注册时的发行结果
     * @throws Exception
     */
    @Test
    public void    TC1279_checkSoloIssueAddr()throws Exception {
        //Thread.sleep(8000);
        //先前已经注册发行和归集地址ADDRESS1，确认发行无问题
        tokenType = "SOLOTC-"+UtilsClass.Random(6);
        String isResult= soloSign.issueToken(PRIKEY1,tokenType,"1009","发行token",ADDRESS1);
        assertThat(isResult, containsString("200"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        String response1 = soloSign.Balance( PRIKEY1, tokenType);
        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
        assertEquals("1009",JSONObject.fromObject(response1).getJSONObject("data").getString("total"));

        log.info("删除发行地址，保留归集地址");
        //删除发行地址，保留归集地址
        String response3=multiSign.delissueaddress(PRIKEY1,ADDRESS1);
        assertThat(response3, containsString("200"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        tokenType = "SOLOTC-"+UtilsClass.Random(7);
        isResult= soloSign.issueToken(PRIKEY1,tokenType,"1009","发行token",ADDRESS1);
        assertThat(isResult, containsString("200"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);
        String response2 = soloSign.Balance( PRIKEY1, tokenType);
        assertEquals("200",JSONObject.fromObject(response2).getString("state"));
        assertEquals("0",JSONObject.fromObject(response2).getJSONObject("data").getString("total"));

        //删除发行地址和归集地址
        log.info("删除发行地址和归集地址");
        String response4=multiSign.delCollAddress(PRIKEY1,ADDRESS1);
        assertThat(response4, containsString("200"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        tokenType = "SOLOTC-"+UtilsClass.Random(8);
        isResult= soloSign.issueToken(PRIKEY1,tokenType,"1009","发行token",ADDRESS1);
        assertThat(isResult, containsString("200"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);
        String response41 = soloSign.Balance( PRIKEY1, tokenType);
        assertEquals("200",JSONObject.fromObject(response41).getString("state"));
        assertEquals("0",JSONObject.fromObject(response41).getJSONObject("data").getString("total"));

        //重新添加发行地址，保留删除归集地址
        String response51=multiSign.addissueaddress(PRIKEY1,ADDRESS1);
        assertThat(response51, containsString("200"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);
        tokenType = "SOLOTC-"+UtilsClass.Random(9);
        isResult= soloSign.issueToken(PRIKEY1,tokenType,"1009","发行token",ADDRESS1);
        assertThat(isResult, containsString("200"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);
        String response52 = soloSign.Balance( PRIKEY1, tokenType);
        assertEquals("200",JSONObject.fromObject(response52).getString("state"));
        assertEquals("0",JSONObject.fromObject(response52).getJSONObject("data").getString("total"));

        //重新添加归集地址
        String response6=multiSign.collAddress(PRIKEY1,ADDRESS1);
        assertThat(response6, containsString("200"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);
    }

    /**
     * Tc024单签正常流程:
     *
     */
    @Test
    public void TC024_SoloProgress() throws Exception {
        String transferData = "归集地址向" + PUBKEY3 + "转账100.25个" + tokenType+",并向"+PUBKEY4+"转账";
        log.info(transferData);
        List<Map> listModel = soloSign.constructToken(ADDRESS3,tokenType,"100.25");
        log.info(ADDRESS3);
        List<Map> list=soloSign.constructToken(ADDRESS5,tokenType2,"200.555",listModel);
        String transferInfo= soloSign.Transfer(list,PRIKEY1, transferData);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        assertThat(transferInfo, containsString("200"));
        log.info("查询帐号3跟帐号5余额，判断转账是否成功");
        String queryInfo = soloSign.Balance( PRIKEY3, tokenType);
        String queryInfo2 = soloSign.Balance( PRIKEY5, tokenType2);
        assertThat(queryInfo, containsString("200"));
        assertThat(queryInfo, containsString("100.25"));
        assertThat(queryInfo2, containsString("200"));
        assertThat(queryInfo2, containsString("200.555"));
        log.info("3向4转账token1");
        List<Map> list1 = soloSign.constructToken(ADDRESS4,tokenType,"30");
        String recycleInfo = soloSign.Transfer(list1, PRIKEY3,"3向4转账token1");
        assertThat(recycleInfo, containsString("200"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        List<Map> list2 = soloSign.constructToken(ADDRESS4,tokenType2,"80");
        String recycleInfo1 = soloSign.Transfer(list2, PRIKEY5,"5向4转账token2");
        assertThat(recycleInfo1, containsString("200"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        List<Map> list3 = soloSign.constructToken(ADDRESS2,tokenType,"30");
        List<Map>list4= soloSign.constructToken(ADDRESS2,tokenType2,"70",list3);
        String recycleInfo2 = soloSign.Transfer(list4, PRIKEY4, "李四向小六转账30 TT001, 70 TT002");
        assertThat(recycleInfo2, containsString("200"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        List<Map> list5 = soloSign.constructToken(ADDRESS2,tokenType2,"20");
        String recycleInfo3 = soloSign.Transfer(list5, PRIKEY5, "王五向小六转账20 TT002");
        assertThat(recycleInfo3, containsString("200"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        List<Map> list6 = (soloSign.constructToken(ADDRESS4,tokenType2,"30"));
        List<Map> list7= soloSign.constructToken(ADDRESS4,tokenType2,"50",list6);
        String recycleInfo4 = soloSign.Transfer(list7, PRIKEY2, "小六向李四转账80");
        log.info(recycleInfo4);
        assertThat(recycleInfo4, containsString("200"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        String queryInfo3TK1 = soloSign.Balance(PRIKEY3, tokenType);
        assertThat(queryInfo3TK1, containsString("70.25"));
        String Info1 = soloSign.Recycle( PRIKEY3, tokenType, "70.25");
        assertThat(Info1, containsString("200"));
        log.info("帐号3，token1余额正确");
        String queryInfo4TK1 = soloSign.Balance(PRIKEY4, tokenType);
        assertThat(queryInfo4TK1, containsString("0"));
        log.info("帐号4，token1余额正确");
        String queryInfo4TK2 = soloSign.Balance(PRIKEY4, tokenType2);
        assertThat(queryInfo4TK2, containsString("90"));
        String Info2 = soloSign.Recycle( PRIKEY4, tokenType2, "90");
        assertThat(Info2, containsString("200"));
        log.info("帐号4，token2余额正确");
        String queryInfo5TK2 = soloSign.Balance(PRIKEY5, tokenType2);
        assertThat(queryInfo5TK2, containsString("100.555"));
        String Info3 = soloSign.Recycle( PRIKEY5, tokenType2, "100.555");
        assertThat(Info3, containsString("200"));
        log.info("帐号5，token2余额正确");
        String queryInfo6TK1 = soloSign.Balance(PRIKEY2, tokenType);
        assertThat(queryInfo6TK1, containsString("30"));
        String Info4 = soloSign.Recycle( PRIKEY2, tokenType, "30");
        assertThat(Info4, containsString("200"));
        log.info("帐号6，token1余额正确");
        String queryInfo6TK2 = soloSign.Balance(PRIKEY2, tokenType2);
        assertThat(queryInfo6TK2, containsString("10"));
        String Info5 = soloSign.Recycle(PRIKEY2, tokenType2, "10");
        assertThat(Info5, containsString("200"));
        log.info("帐号6，token2余额正确");
    }


    /**
     * 精度测试
     *
     */
    @Test
    public void TC024_PrecisionTest() throws Exception {
        String transferData = "归集地址向" + PUBKEY3 + "转账" + tokenType+",并向"+PUBKEY5+"转账";
        log.info(transferData);
        List<Map> listModel = soloSign.constructToken(ADDRESS3,tokenType,issueAmount1);
        log.info(ADDRESS3);
        List<Map> list=soloSign.constructToken(ADDRESS5,tokenType2,issueAmount2,listModel);
        String transferInfo= soloSign.Transfer(list,PRIKEY1, transferData);
        assertThat(transferInfo, containsString("200"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        String amount1, amount2;
        if (UtilsClass.PRECISION == 10) {
            amount1 = "10000.1234567891";
            amount2 = "20000.8765432123";
        }else {
            amount1 = "10000.123456";
            amount2 = "20000.876543";
        }

        log.info("查询帐号3跟帐号5余额，判断转账是否成功");
        String queryInfo = soloSign.Balance( PRIKEY3, tokenType);
        String queryInfo2 = soloSign.Balance( PRIKEY5, tokenType2);
        assertThat(queryInfo, containsString("200"));
        assertThat(queryInfo, containsString(amount1));
        assertThat(queryInfo2, containsString("200"));
        assertThat(queryInfo2, containsString(amount2));

        String Info3 = soloSign.Recycle( PRIKEY3, tokenType, issueAmount1);
        assertThat(Info3, containsString("200"));
        String Info4 = soloSign.Recycle(PRIKEY5, tokenType2, issueAmount2);
        assertThat(Info4, containsString("200"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        String queryInfo5 = multiSign.QueryZero(tokenType);
        assertEquals(amount1,JSONObject.fromObject(queryInfo5).getJSONObject("data").getJSONObject("detail").getString(tokenType));
        String queryInfo6 = multiSign.QueryZero(tokenType2);
        assertEquals(amount2,JSONObject.fromObject(queryInfo6).getJSONObject("data").getJSONObject("detail").getString(tokenType2));


    }

    //小数量发行token
    @Test
    public void TC_SoloMinIssue() throws Exception{
        String minToken = "TxTypeSoMin-" + UtilsClass.Random(6);
        String minData = "单签" + ADDRESS1 + "发行token " + minToken;
        String minAmount = "";

        if (UtilsClass.PRECISION == 10) {
            minAmount = "0.0000000001";
        }else {
            minAmount = "0.000001";
        }
        log.info(minData);

        String minResp = soloSign.issueToken(PRIKEY1,minToken,minAmount,minData,ADDRESS1);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        log.info("查询归集地址中两种token余额");
        String response1 = soloSign.Balance( PRIKEY1, minToken);

        assertThat(minToken +"查询余额错误",response1, containsString("200"));
        assertEquals(minAmount,JSONObject.fromObject(response1).getJSONObject("data").getJSONObject("detail").getString(minToken));

    }
    /**
     * 小数量转账、回收测试
     *
     */
    @Test
    public void TC_MiniTest() throws Exception {
        String transferData = "归集地址向" + PUBKEY3 + "转账" + tokenType+",并向"+PUBKEY5+"转账";
        log.info(transferData);
        List<Map> listModel = soloSign.constructToken(ADDRESS3,tokenType,"0.003");
        log.info(ADDRESS3);
        List<Map> list=soloSign.constructToken(ADDRESS5,tokenType2,"0.8",listModel);
        String transferInfo= soloSign.Transfer(list,PRIKEY1, transferData);
        assertThat(transferInfo, containsString("200"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        String amount1, amount2;
        if (UtilsClass.PRECISION == 10) {
            amount1 = "10000.1204567891";
            amount2 = "20000.0735432123";
        }else {
            amount1 = "10000.120456";
            amount2 = "20000.073543";
        }

        log.info("查询帐号3跟帐号5余额，判断转账是否成功");
        String queryInfo = soloSign.Balance( PRIKEY3, tokenType);
        String queryInfo2 = soloSign.Balance( PRIKEY5, tokenType2);
        assertThat(queryInfo, containsString("200"));
        assertThat(queryInfo, containsString("0.003"));
        assertThat(queryInfo2, containsString("200"));
        assertThat(queryInfo2, containsString("0.8"));

        String Info3 = soloSign.Recycle(PRIKEY3, tokenType, "0.002");
        assertThat(Info3, containsString("200"));
        String Info4 = soloSign.Recycle( PRIKEY5, tokenType2, "0.5");
        assertThat(Info4, containsString("200"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        String queryInfo5 = multiSign.QueryZero(tokenType);
        assertEquals("0.002",JSONObject.fromObject(queryInfo5).getJSONObject("data").getJSONObject("detail").getString(tokenType));
        String queryInfo6 = multiSign.QueryZero(tokenType2);
        assertEquals("0.5",JSONObject.fromObject(queryInfo6).getJSONObject("data").getJSONObject("detail").getString(tokenType2));


    }


    /**
     * Tc024锁定后转账:
     *
     */
    @Test
    public void TC024_TransferAfterFrozen() throws Exception {

        //20190411增加锁定步骤后进行转账
        log.info("锁定待转账Token: "+tokenType);
        String resp=multiSign.freezeToken(tokenType);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        String transferData = "归集地址向" + PUBKEY3 + "转账100.25个" + tokenType+",并向"+PUBKEY4+"转账";
        log.info(transferData);
        List<Map> listModel1 = soloSign.constructToken(ADDRESS3,tokenType,"100.25");
        log.info(ADDRESS3);
        List<Map> list1=soloSign.constructToken(ADDRESS5,tokenType2,"200.555",listModel1);
        String transferInfo= soloSign.Transfer(list1,PRIKEY1, transferData);
//        assertThat(transferInfo, containsString("400"));


        log.info("查询帐号3跟帐号5余额，判断转账是否成功");
        String queryInfo = multiSign.BalanceByAddr( ADDRESS3, tokenType);
        String queryInfo2 = multiSign.BalanceByAddr( ADDRESS5, tokenType2);
        assertThat(JSONObject.fromObject(queryInfo).getJSONObject("data").getString("total"), containsString("0"));
        assertThat(JSONObject.fromObject(queryInfo2).getJSONObject("data").getString("total"), containsString("0"));


        log.info("解除锁定待转账Token: "+tokenType);
        String resp1=multiSign.recoverFrozenToken(tokenType);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        transferData = "归集地址向" + PUBKEY3 + "转账100.25个" + tokenType+",并向"+PUBKEY4+"转账";
        log.info(transferData);
        List<Map> listModel = soloSign.constructToken(ADDRESS3,tokenType,"100.25");
        log.info(ADDRESS3);
        List<Map> list=soloSign.constructToken(ADDRESS5,tokenType2,"200.555",listModel);
        transferInfo= soloSign.Transfer(list,PRIKEY1, transferData);
        assertThat(transferInfo, containsString("200"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        log.info("查询帐号3跟帐号5余额，判断转账是否成功");
        queryInfo = multiSign.BalanceByAddr( ADDRESS3, tokenType);
        queryInfo2 = multiSign.BalanceByAddr( ADDRESS5, tokenType2);
        assertThat(queryInfo, containsString("200"));
        assertThat(queryInfo, containsString("100.25"));
        assertThat(queryInfo2, containsString("200"));
        assertThat(queryInfo2, containsString("200.555"));

    }

    /**
     * Tc040单签转单签异常测试:
     *
     */
    @Test
    public void TC040_SoloProgress() throws Exception {
        String transferData = "归集地址向" + PUBKEY3 + "转账3000个" + tokenType+",并向"+PUBKEY4+"转账";
        log.info(transferData);
        List<Map> list=soloSign.constructToken(ADDRESS3,tokenType,"3000");
        List<Map> list1=soloSign.constructToken(ADDRESS3,tokenType2,"3000",list);
        String transferInfo= soloSign.Transfer(list1,PRIKEY1,transferData);
        assertThat(transferInfo, containsString("200"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        List<Map> list2 = soloSign.constructToken(ADDRESS4,tokenType,"4000");
        List<Map>list3= soloSign.constructToken(ADDRESS5,tokenType,"70",list2);
        String recycleInfo2 = soloSign.Transfer(list3, PRIKEY3, "李四向小六转账4000 TT001, 70 TT001");
        assertThat(recycleInfo2, containsString("insufficient balance"));

        List<Map> list4 = soloSign.constructToken(ADDRESS4,tokenType,"4000");
        List<Map>list5= soloSign.constructToken(ADDRESS5,tokenType2,"4001",list4);
        String recycleInfo3 = soloSign.Transfer(list5, PRIKEY3, "李四向小六转账4000 TT001, 4001 TT002");
        assertThat(recycleInfo3, containsString("insufficient balance"));

        List<Map> list6 = soloSign.constructToken(ADDRESS4,tokenType,"4000");
        List<Map>list7= soloSign.constructToken(ADDRESS5,tokenType2,"60",list6);
        String recycleInfo4 = soloSign.Transfer(list7, PRIKEY3, "李四向小六转账30 TT001, 60 TT002");
        assertThat(recycleInfo4, containsString("insufficient balance"));

        String Info = soloSign.Recycle( PRIKEY3, tokenType, "3000");
        String Info3 = soloSign.Recycle( PRIKEY3, tokenType2, "3000");
        assertThat(Info, containsString("200"));
        assertThat(Info3, containsString("200"));
    }
    /**
     * Tc041单签转单签+多签异常测试:锁定解锁后执行回收
     *
     */
    @Test
    public void TC041_SoloProgress() throws Exception {
        String transferData = "归集地址向" + PUBKEY3 + "转账3000个" + tokenType+",并向"+PUBKEY4+"转账";
        log.info(transferData);
        List<Map> list=soloSign.constructToken(ADDRESS3,tokenType,"3000");
        List<Map> list1=soloSign.constructToken(ADDRESS3,tokenType2,"3000",list);
        String transferInfo= soloSign.Transfer(list1,PRIKEY1, transferData);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        List<Map> list2 = soloSign.constructToken(ADDRESS4,tokenType,"200");
        List<Map>list3= soloSign.constructToken(MULITADD5,tokenType,"7000",list2);
        String recycleInfo2 = soloSign.Transfer(list3, PRIKEY3, "李四向小六转账4000 TT001, 70 TT001");
        assertThat(recycleInfo2, containsString("insufficient balance"));


        List<Map> list4 = soloSign.constructToken(ADDRESS4,tokenType,"4000");
        List<Map>list5= soloSign.constructToken(MULITADD5,tokenType2,"4001",list4);
        String recycleInfo3 = soloSign.Transfer(list5, PRIKEY3, "李四向小六转账4000 TT001, 4001 TT002");
        assertThat(recycleInfo3, containsString("insufficient balance"));


        List<Map> list6 = soloSign.constructToken(ADDRESS4,tokenType,"4000");
        List<Map>list7= soloSign.constructToken(MULITADD5,tokenType2,"400",list6);
        String recycleInfo4 = soloSign.Transfer(list7, PRIKEY3, "李四向小六转账4000 TT001, 4001 TT002");
        assertThat(recycleInfo3, containsString("insufficient balance"));


        //20190411增加锁定解锁操作步骤后进行回收
        log.info("锁定待回收Token: "+tokenType);
        String resp=multiSign.freezeToken(tokenType);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        log.info("解除锁定待回收Token: "+tokenType);
        String resp1=multiSign.recoverFrozenToken(tokenType);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        log.info("开始回收....");
        String Info = soloSign.Recycle(PRIKEY3, tokenType, "3000");
        String Info1 = soloSign.Recycle(PRIKEY3, tokenType2, "3000");
        String Info2 = soloSign.Recycle(PRIKEY1, tokenType, "7000.123456789");
        String Info3 = soloSign.Recycle(PRIKEY1, tokenType2, "17000.87654321");

        assertEquals("200",JSONObject.fromObject(Info).getString("state"));
        assertEquals("200",JSONObject.fromObject(Info1).getString("state"));
        assertEquals("200",JSONObject.fromObject(Info2).getString("state"));
        assertEquals("200",JSONObject.fromObject(Info3).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        log.info("开始查询余额....");
        String response1 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        String response2 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType2);
        String response3 = soloSign.Balance( PRIKEY3, tokenType);
        String response4 = soloSign.Balance( PRIKEY3, tokenType2);
        assertThat(tokenType+"查询余额错误",response1, containsString("200"));
        assertThat(tokenType+"查询余额错误",response2, containsString("200"));
        assertThat(tokenType+"查询余额错误",response3, containsString("200"));
        assertThat(tokenType+"查询余额错误",response4, containsString("200"));
        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
        assertEquals("200",JSONObject.fromObject(response2).getString("state"));
        assertEquals("200",JSONObject.fromObject(response3).getString("state"));
        assertEquals("200",JSONObject.fromObject(response4).getString("state"));
        assertThat(JSONObject.fromObject(response1).getJSONObject("data").getString("total"), containsString("0"));
        assertThat(JSONObject.fromObject(response2).getJSONObject("data").getString("total"), containsString("0"));
        assertThat(JSONObject.fromObject(response3).getJSONObject("data").getString("total"), containsString("0"));
        assertThat(JSONObject.fromObject(response4).getJSONObject("data").getString("total"), containsString("0"));

    }

    /**
     * Tc042单签转单签+多签测试:回收前锁定token
     *
     */
    @Test
    public void TC042_SoloProgress() throws Exception {
        String transferData = "归集地址向" + "PUBKEY3" + "转账3000个" + "tokenType"+",并向"+"PUBKEY4"+"转账tokenType2";
        log.info(transferData);

        List<Map> list=soloSign.constructToken(ADDRESS3,tokenType,"3000");
        List<Map> list1=soloSign.constructToken(ADDRESS3,tokenType2,"3000",list);
        String transferInfo= soloSign.Transfer(list1,PRIKEY1,transferData);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        List<Map> list2 = soloSign.constructToken(ADDRESS4,tokenType,"200");
        List<Map>list3= soloSign.constructToken(MULITADD5,tokenType,"70",list2);
        String recycleInfo2 = soloSign.Transfer(list3, PRIKEY3, "李四向小六转账4000 TT001, 70 TT001");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        List<Map> list4 = soloSign.constructToken(ADDRESS4,tokenType,"400");
        List<Map>list5= soloSign.constructToken(MULITADD5,tokenType2,"401",list4);
        String recycleInfo3 = soloSign.Transfer(list5, PRIKEY3, "李四向小六转账4000 TT001, 4001 TT002");
        assertEquals("200",JSONObject.fromObject(recycleInfo3).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        //20190411增加锁定操作步骤后进行回收
        log.info("锁定待回收Token: "+tokenType);
        String resp=multiSign.freezeToken(tokenType);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        log.info("开始回收....");
        String Info = soloSign.Recycle( PRIKEY3, tokenType, "2330");
        String Info1 = soloSign.Recycle( PRIKEY3, tokenType2, "2599");
        String Info2 = soloSign.Recycle( PRIKEY4, tokenType, "600");
        String Info3 = multiSign.Recycle(MULITADD5, PRIKEY1, tokenType, "70");
        String Info4 = multiSign.Recycle(MULITADD5, PRIKEY3, tokenType2, "401");
        String Info5 = soloSign.Recycle(PRIKEY1, tokenType, "7000.123456789");
        String Info6 = soloSign.Recycle(PRIKEY1, tokenType2, "17000.87654321");

        assertEquals("200",JSONObject.fromObject(Info).getString("state"));
        assertEquals("200",JSONObject.fromObject(Info1).getString("state"));
        assertEquals("200",JSONObject.fromObject(Info2).getString("state"));
        assertEquals("200",JSONObject.fromObject(Info3).getString("state"));
        assertEquals("200",JSONObject.fromObject(Info4).getString("state"));
        assertEquals("200",JSONObject.fromObject(Info5).getString("state"));
        assertEquals("200",JSONObject.fromObject(Info6).getString("state"));


        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        log.info("开始查询余额....");
        String response1 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        String response2 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType2);
        String response3 = multiSign.Balance(MULITADD5, PRIKEY1, tokenType);
        String response4 = multiSign.Balance(MULITADD5, PRIKEY1, tokenType2);
        String response5 = soloSign.Balance( PRIKEY3, tokenType);
        String response6 = soloSign.Balance( PRIKEY3, tokenType2);
        String response7 = soloSign.Balance( PRIKEY4, tokenType);

        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
        assertEquals("200",JSONObject.fromObject(response2).getString("state"));
        assertEquals("200",JSONObject.fromObject(response3).getString("state"));
        assertEquals("200",JSONObject.fromObject(response4).getString("state"));
        assertEquals("200",JSONObject.fromObject(response5).getString("state"));
        assertEquals("200",JSONObject.fromObject(response6).getString("state"));
        assertEquals("200",JSONObject.fromObject(response7).getString("state"));

        assertEquals(JSONObject.fromObject(response1).getJSONObject("data").getString("total").equals("0"),true);
        assertEquals(JSONObject.fromObject(response2).getJSONObject("data").getString("total").equals("0"),true);
        assertEquals(JSONObject.fromObject(response3).getJSONObject("data").getString("total").equals("0"),true);
        assertEquals(JSONObject.fromObject(response4).getJSONObject("data").getString("total").equals("0"),true);
        assertEquals(JSONObject.fromObject(response5).getJSONObject("data").getString("total").equals("0"),true);
        assertEquals(JSONObject.fromObject(response6).getJSONObject("data").getString("total").equals("0"),true);
        assertEquals(JSONObject.fromObject(response7).getJSONObject("data").getString("total").equals("0"),true);
    }

    /**
     * Tc244单签接口双花测试:
     *
     */
    @Test
    public void TC0244_SoloProgress() throws Exception {
        String transferData = "归集地址向" + PUBKEY3 + "转账3000个" + tokenType+",并向"+PUBKEY4+"转账";
        log.info(transferData);
        List<Map> list=soloSign.constructToken(ADDRESS3,tokenType2,"3000");
        List<Map> list0=soloSign.constructToken(ADDRESS3,tokenType,"4000",list);
        String transferInfo= soloSign.Transfer(list0,PRIKEY1, transferData);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        List<Map> list1= soloSign.constructToken(ADDRESS4,tokenType,"300");
        String transferInfo1= soloSign.Transfer(list1, PRIKEY3, "双花验证");
        List<Map> list2= soloSign.constructToken(ADDRESS4,tokenType,"301");
        String transferInfo2= soloSign.Transfer(list2, PRIKEY3, "双花验证");
        assertThat(transferInfo1, containsString("200"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        String response=soloSign.Balance(PRIKEY4,tokenType);
        assertThat(response,anyOf(containsString("300"),containsString("301")));
        List<Map> list3= soloSign.constructToken(ADDRESS4,tokenType,"400");
        List<Map> list4= soloSign.constructToken(ADDRESS4,tokenType2,"411",list3);
        String transferInfo3= soloSign.Transfer(list3, PRIKEY3, "双花验证");
        String transferInfo4= soloSign.Transfer(list4, PRIKEY3, "双花验证");
        assertThat(transferInfo3, containsString("200"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        String response1=soloSign.Balance(PRIKEY4,tokenType);
        String response2=soloSign.Balance(PRIKEY4,tokenType2);
        assertThat(response1,anyOf(containsString("700"),containsString("701")));
        assertThat(response2,anyOf(containsString("411"),containsString("0")));

        List<Map> list5= soloSign.constructToken(ADDRESS4,tokenType,"320");
        List<Map> list6= soloSign.constructToken(ADDRESS4,tokenType2,"320");
        String transferInfo5= soloSign.Transfer(list5, PRIKEY3, "双花验证");
        String transferInfo6= soloSign.Transfer(list6, PRIKEY3, "双花验证");
        assertThat(transferInfo5, containsString("200"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        String response3=soloSign.Balance(PRIKEY4,tokenType);
        String response4=soloSign.Balance(PRIKEY4,tokenType2);
        assertThat(response3,anyOf(containsString("1020"),containsString("1021")));
        assertThat(response4,anyOf(containsString("731"),containsString("320")));

    }

    //验证无法转账给自己
    @Test
    public void TransferToSelf()throws Exception{
        String transferData = ADDRESS1 + "转给自己" + "转账100.25个" + tokenType;
        log.info(transferData);
        List<Map> listModel = soloSign.constructToken(ADDRESS1,tokenType,"100.25");
        String transferInfo= soloSign.Transfer(listModel,PRIKEY1, transferData);
        assertEquals(true,transferInfo.contains("can't transfer to self"));
    }
    /**
     * 单签发行token时指定其他地址，不发行token到本身.指定单签地址与多签地址
     * 2019-1-10 开发备注单签只能发行给自己 不支持发行给其他账户
     * @throws Exception
     */
    //@Test
    public void TC993_issueToOther()throws Exception{
        BeforeCondition beforeCondition=new BeforeCondition();
        beforeCondition.collAddressTest();
        log.info("发行两种token1000个");
        tokenType = "SOLOTC-"+UtilsClass.Random(6);
        String isResult= soloSign.issueToken(PRIKEY1,tokenType,"10000","发行token",ADDRESS2);
        tokenType2 = "SOLOTC-"+UtilsClass.Random(6);
        String isResult2= soloSign.issueToken(PRIKEY1,tokenType2,"20000","发行token",MULITADD3);
        assertThat(tokenType+"发行token错误",isResult, containsString("200"));
        assertThat(tokenType+"发行token错误",isResult2, containsString("200"));
        Thread.sleep(SLEEPTIME*3);
        log.info("查询归集地址中两种token余额");
        String response1 = soloSign.Balance( PRIKEY2, tokenType);
        String response3 = soloSign.Balance( PRIKEY1, tokenType);
        String response2 = multiSign.Balance( MULITADD3,PRIKEY1,tokenType2 );
        assertThat(tokenType+"查询余额错误",response1, containsString("200"));
        assertThat(tokenType+"查询余额错误",response2, containsString("200"));
        assertThat(tokenType+"查询余额不正确",response1, containsString("total\":\"0\""));
        assertThat(tokenType+"查询余额不正确",response2, containsString("total\":\"0\""));
        assertThat(tokenType+"查询余额不正确",response3, containsString("1000"));
    }
}
