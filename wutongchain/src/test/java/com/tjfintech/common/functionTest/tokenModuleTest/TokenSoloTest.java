package com.tjfintech.common.functionTest.tokenModuleTest;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.Token;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TokenSoloTest {
    TestBuilder testBuilder= TestBuilder.getInstance();

    private static String tokenType;
    private static String tokenType2;

    private static String issueAmount1;
    private static String issueAmount2;

    private static String actualAmount1;
    private static String actualAmount2;

    Token tokenModule = testBuilder.getToken();
    CommonFunc commonFunc = new CommonFunc();
    UtilsClass utilsClass=new UtilsClass();

    @BeforeClass
    public static void init()throws Exception
    {
        SDKADD = TOKENADD;
        if(tokenAccount1.isEmpty()) {
            BeforeCondition beforeCondition = new BeforeCondition();
            beforeCondition.createTokenAccount();
            beforeCondition.tokenAddIssueCollAddr();
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
        tokenType = commonFunc.tokenModule_IssueToken(tokenAccount1,tokenAccount1,issueAmount1);
        tokenType2 = commonFunc.tokenModule_IssueToken(tokenAccount1,tokenAccount1,issueAmount2);


//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        log.info("查询归集地址中两种token余额");
        String response1 = tokenModule.tokenGetBalance( tokenAccount1, tokenType);
        String response2 = tokenModule.tokenGetBalance( tokenAccount1, tokenType2);

//        assertThat(tokenType+"查询余额错误",response1, containsString("200"));
//        assertThat(tokenType+"查询余额错误",response2, containsString("200"));
        assertThat(tokenType+"查询余额不正确",response1, containsString(actualAmount1));
        assertThat(tokenType+"查询余额不正确",response2, containsString(actualAmount2));
    }


    @Test
    public void soloToMultiIssue() throws Exception {

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
        tokenType = commonFunc.tokenModule_IssueToken(tokenAccount1,tokenMultiAddr1,issueAmount1);
        tokenType2 = commonFunc.tokenModule_IssueToken(tokenAccount1,tokenMultiAddr2,issueAmount2);


//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        log.info("查询归集地址中两种token余额");
        String response1 = tokenModule.tokenGetBalance( tokenMultiAddr1, tokenType);
        String response2 = tokenModule.tokenGetBalance( tokenMultiAddr2, tokenType2);

        assertThat(tokenType+"查询余额错误",response1, containsString("200"));
        assertThat(tokenType+"查询余额错误",response2, containsString("200"));
        assertThat(tokenType+"查询余额不正确",response1, containsString(actualAmount1));
        assertThat(tokenType+"查询余额不正确",response2, containsString(actualAmount2));
    }

    //同时发行地址与归集地址相互给对方发行
    @Test
    public void issueTwo()throws Exception{
        String issueAddr = "";
        String collAddr = "";
        String issueToken = "";
        String issAmount ="";

        //单签地址发行token 5000.999999
        issueAddr = tokenAccount2;
        collAddr = tokenAccount1;

        issAmount = "1844674";
        String issAmount2 = "18022.1";
        String issueToken2 = "";

        issueToken = commonFunc.tokenModule_IssueToken(issueAddr,collAddr,issAmount);
        issueToken2 = commonFunc.tokenModule_IssueToken(collAddr,issueAddr,issAmount2);


//        sleepAndSaveInfo(SLEEPTIME,"issue waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);



        String queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken2);
        assertEquals(false,queryBalance.contains(issueToken2));


        queryBalance = tokenModule.tokenGetBalance(issueAddr,issueToken2);
        assertEquals(issAmount2, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken2));
        queryBalance = tokenModule.tokenGetBalance(issueAddr,issueToken);
        assertEquals(false,queryBalance.contains(issueToken));

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

        tokenType = commonFunc.tokenModule_IssueToken(tokenAccount1,tokenAccount1,actualAmount1);

//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        log.info("查询归集地址中token余额");
        String response1 = tokenModule.tokenGetBalance( tokenAccount1, tokenType);

        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
        assertEquals(actualAmount1,JSONObject.fromObject(response1).getJSONObject("data").getString(tokenType));
    }

    /**
     * 单签发行检查发行地址注册、未注册时的发行结果
     * @throws Exception
     */
    @Test
    public void    TC1279_checkSoloIssueAddr()throws Exception {
        //Thread.sleep(8000);
        //先前已经注册发行和归集地址tokenAccount1，确认发行无问题
        tokenType = commonFunc.tokenModule_IssueToken(tokenAccount1,tokenAccount1,"1009");

//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        String response1 = tokenModule.tokenGetBalance( tokenAccount1, tokenType);
        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
        assertEquals("1009",JSONObject.fromObject(response1).getJSONObject("data").getString(tokenType));

        log.info("删除发行地址，保留归集地址");
        //删除发行地址，保留归集地址
        String response3=tokenModule.tokenDelMintAddr(tokenAccount1);
        assertThat(response3, containsString("200"));
//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        tokenType = commonFunc.tokenModule_IssueToken(tokenAccount1,tokenAccount1,"1009");
//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        String response2 = tokenModule.tokenGetBalance( tokenAccount1, tokenType);
        assertEquals("200",JSONObject.fromObject(response2).getString("state"));
        assertEquals(false,response2.contains(tokenType));

        //删除发行地址和归集地址
        log.info("删除发行地址和归集地址");
        String response4=tokenModule.tokenDelCollAddr(tokenAccount1);
        assertThat(response4, containsString("200"));
//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        tokenType = commonFunc.tokenModule_IssueToken(tokenAccount1,tokenAccount1,"1009");
//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        String response41 = tokenModule.tokenGetBalance( tokenAccount1, tokenType);
        assertEquals("200",JSONObject.fromObject(response41).getString("state"));
        assertEquals(false,response41.contains(tokenType));


        //重新添加发行地址，保留删除归集地址
        String response51=tokenModule.tokenAddMintAddr(tokenAccount1);
        assertThat(response51, containsString("200"));
//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        tokenType = commonFunc.tokenModule_IssueToken(tokenAccount1,tokenAccount1,"1009");
//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        String response52 = tokenModule.tokenGetBalance( tokenAccount1, tokenType);
        assertEquals("200",JSONObject.fromObject(response52).getString("state"));
        assertEquals(false,response52.contains(tokenType));

        //重新添加归集地址
        String response6=tokenModule.tokenAddCollAddr(tokenAccount1);
        assertThat(response6, containsString("200"));
//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        tokenType = commonFunc.tokenModule_IssueToken(tokenAccount1,tokenAccount1,"2356");
//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        String response7 = tokenModule.tokenGetBalance( tokenAccount1, tokenType);
        assertEquals("2356",JSONObject.fromObject(response7).getJSONObject("data").getString(tokenType));
    }

    /**
     * Tc024单签正常流程:归集地址向两个单签地址转账
     *
     */
    @Test
    public void TC024_SoloProgress() throws Exception {
        //"归集地址向" + tokenAccount3 + "转账100.25个" + tokenType+",并向"+tokenAccount5 +"转账";
        List<Map> listModel = utilsClass.tokenConstructToken(tokenAccount3,tokenType,"100.25");
        List<Map> list = utilsClass.tokenConstructToken(tokenAccount5,tokenType2,"200.555",listModel);

        String transferInfo= commonFunc.tokenModule_TransferTokenList(tokenAccount1,list);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        log.info("查询"+ tokenAccount3 + "跟" + tokenAccount5 + "余额，判断转账是否成功");
        String queryInfo = tokenModule.tokenGetBalance( tokenAccount3, "");
        String queryInfo2 = tokenModule.tokenGetBalance( tokenAccount5, "");
        assertEquals("100.25",JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType));
        assertEquals("200.555",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString(tokenType2));

        
        log.info(tokenAccount3 + " --> " + tokenAccount4 + "转账: " + tokenType);
        List<Map> list1 = utilsClass.tokenConstructToken(tokenAccount4,tokenType,"30");
        transferInfo = commonFunc.tokenModule_TransferTokenList(tokenAccount3,list1);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        log.info(tokenAccount5 + " --> " + tokenAccount4 + "转账: " + tokenType2);
        List<Map> list2 = utilsClass.tokenConstructToken(tokenAccount4,tokenType2,"80");
        transferInfo = commonFunc.tokenModule_TransferTokenList(tokenAccount5,list2);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));

//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        log.info(tokenAccount4 + " --> " + tokenAccount2 + "转账: " + tokenType2 + " " + tokenType2);
        List<Map> list3 = utilsClass.tokenConstructToken(tokenAccount2,tokenType,"30");
        List<Map> list4 = utilsClass.tokenConstructToken(tokenAccount2,tokenType2,"70",list3);

        transferInfo = commonFunc.tokenModule_TransferTokenList(tokenAccount4,list4);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        log.info(tokenAccount5 + " --> " + tokenAccount2 + "转账: " + tokenType2 );
        List<Map> list5 = utilsClass.tokenConstructToken(tokenAccount2,tokenType2,"20");
        transferInfo = commonFunc.tokenModule_TransferTokenList(tokenAccount5,list5);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        List<Map> list6 = utilsClass.tokenConstructToken(tokenAccount4,tokenType,"30");
        List<Map> list7 = utilsClass.tokenConstructToken(tokenAccount4,tokenType2,"50",list6);

        transferInfo = commonFunc.tokenModule_TransferTokenList(tokenAccount2,list7);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


//        String query1 = tokenModule.tokenGetBalance(tokenAccount1, "");
        String query2 = tokenModule.tokenGetBalance(tokenAccount2, "");
        String query3 = tokenModule.tokenGetBalance(tokenAccount3, "");
        String query4 = tokenModule.tokenGetBalance(tokenAccount4, "");
        String query5 = tokenModule.tokenGetBalance(tokenAccount5, "");

        assertEquals("70.25",JSONObject.fromObject(query3).getJSONObject("data").getString(tokenType));
        String Info2 = commonFunc.tokenModule_DestoryToken(tokenAccount3, tokenType, "70.25");

        assertEquals("30",JSONObject.fromObject(query4).getJSONObject("data").getString(tokenType));
        assertEquals("60",JSONObject.fromObject(query4).getJSONObject("data").getString(tokenType2));
        String Info3 = commonFunc.tokenModule_DestoryToken(tokenAccount4, tokenType2, "60");
        String Info31 = commonFunc.tokenModule_DestoryToken(tokenAccount4, tokenType, "30");

        assertEquals("100.555",JSONObject.fromObject(query5).getJSONObject("data").getString(tokenType2));
        String Info4 = commonFunc.tokenModule_DestoryToken(tokenAccount5, tokenType2, "100.555");

//        assertEquals("30",JSONObject.fromObject(query2).getJSONObject("data").getString(tokenType));
        assertEquals("40",JSONObject.fromObject(query2).getJSONObject("data").getString(tokenType2));
//        String Info5 = commonFunc.tokenModule_DestoryToken(tokenAccount2, tokenType, "30");
        String Info6 = commonFunc.tokenModule_DestoryToken(tokenAccount2, tokenType2, "40");

        assertEquals("200",JSONObject.fromObject(Info2).getString("state"));
        assertEquals("200",JSONObject.fromObject(Info3).getString("state"));
        assertEquals("200",JSONObject.fromObject(Info31).getString("state"));
        assertEquals("200",JSONObject.fromObject(Info4).getString("state"));
        assertEquals("200",JSONObject.fromObject(Info4).getString("state"));
//        assertEquals("200",JSONObject.fromObject(Info5).getString("state"));
        assertEquals("200",JSONObject.fromObject(Info6).getString("state"));

//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        String queryBalanceInfo2 = tokenModule.tokenGetBalance(tokenAccount2, "");
        String queryBalanceInfo3 = tokenModule.tokenGetBalance(tokenAccount3, "");
        String queryBalanceInfo4 = tokenModule.tokenGetBalance(tokenAccount4, "");
        String queryBalanceInfo5 = tokenModule.tokenGetBalance(tokenAccount5, "");

        assertEquals(false,queryBalanceInfo2.contains(tokenType));
        assertEquals(false,queryBalanceInfo2.contains(tokenType2));
        assertEquals(false,queryBalanceInfo3.contains(tokenType));
        assertEquals(false,queryBalanceInfo3.contains(tokenType2));
        assertEquals(false,queryBalanceInfo4.contains(tokenType));
        assertEquals(false,queryBalanceInfo4.contains(tokenType2));
        assertEquals(false,queryBalanceInfo5.contains(tokenType));
        assertEquals(false,queryBalanceInfo5.contains(tokenType2));

    }


    /**
     * 精度测试
     *
     */
    @Test
    public void TC024_PrecisionTest() throws Exception {

        List<Map> listModel = utilsClass.tokenConstructToken(tokenAccount3,tokenType,issueAmount1);
        List<Map> list = utilsClass.tokenConstructToken(tokenAccount5,tokenType2,issueAmount2,listModel);
        String transferInfo1 = commonFunc.tokenModule_TransferTokenList(tokenAccount1,list);
        assertEquals("200",JSONObject.fromObject(transferInfo1).getString("state"));
//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        String amount1, amount2;
        if (UtilsClass.PRECISION == 10) {
            amount1 = "10000.1234567891";
            amount2 = "20000.8765432123";
        }else {
            amount1 = "10000.123456";
            amount2 = "20000.876543";
        }

        log.info("查询帐号3跟帐号2余额，判断转账是否成功");
        String queryInfo = tokenModule.tokenGetBalance( tokenAccount3, tokenType);
        String queryInfo2 = tokenModule.tokenGetBalance( tokenAccount5, tokenType2);
        assertEquals(amount1,JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType));
        assertEquals(amount2,JSONObject.fromObject(queryInfo2).getJSONObject("data").getString(tokenType2));


        String Info3 = commonFunc.tokenModule_DestoryToken(tokenAccount3,tokenType, issueAmount1);
        assertEquals("200",JSONObject.fromObject(Info3).getString("state"));
        String Info4 = commonFunc.tokenModule_DestoryToken(tokenAccount5, tokenType2, issueAmount2);
        assertEquals("200",JSONObject.fromObject(Info4).getString("state"));

//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        String queryInfo11 = tokenModule.tokenGetBalance( tokenAccount3, "");
        String queryInfo12 = tokenModule.tokenGetBalance( tokenAccount5, "");
        String queryInfo5 = tokenModule.tokenGetDestroyBalance();
        String queryInfo6 = tokenModule.tokenGetDestroyBalance();

        assertEquals(false,queryInfo11.contains(tokenType));
        assertEquals(false,queryInfo12.contains(tokenType2));
        assertEquals(amount1,JSONObject.fromObject(queryInfo5).getJSONObject("data").getString(tokenType));
        assertEquals(amount2,JSONObject.fromObject(queryInfo6).getJSONObject("data").getString(tokenType2));


    }
    /**
    * 发行小数量测试
    *
    */
    @Test
    public void TC_MinIssue()throws Exception{

        if (UtilsClass.PRECISION == 10) {
            actualAmount1 = "0.0000000001";
        }else {
            actualAmount1 = "0.000001";
        }

        tokenType = commonFunc.tokenModule_IssueToken(tokenAccount1,tokenAccount1,actualAmount1);
//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        log.info("查询归集地址中token余额");
        String response1 = tokenModule.tokenGetBalance( tokenAccount1, tokenType);

        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
        assertEquals(actualAmount1,JSONObject.fromObject(response1).getJSONObject("data").getString(tokenType));
    }


    /**
     * 转账小数量测试
     *
     */
    @Test
    public void TC_MiniTest() throws Exception {

        List<Map> listModel = utilsClass.tokenConstructToken(tokenAccount3,tokenType,"0.03");
        List<Map> list = utilsClass.tokenConstructToken(tokenAccount5,tokenType2,"10.05",listModel);
        String transferInfo1 = commonFunc.tokenModule_TransferTokenList(tokenAccount1,list);
        assertEquals("200",JSONObject.fromObject(transferInfo1).getString("state"));
//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        String amount1, amount2;
        if (UtilsClass.PRECISION == 10) {
            amount1 = "10000.1234567891";
            amount2 = "20000.8765432123";
        }else {
            amount1 = "10000.123456";
            amount2 = "20000.876543";
        }

        log.info("查询帐号3跟帐号2余额，判断转账是否成功");
        String queryInfo = tokenModule.tokenGetBalance( tokenAccount3, tokenType);
        String queryInfo2 = tokenModule.tokenGetBalance( tokenAccount5, tokenType2);
        assertEquals("0.03",JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType));
        assertEquals("10.05",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString(tokenType2));


        String Info3 = commonFunc.tokenModule_DestoryToken(tokenAccount3,tokenType, "0.03");
        assertEquals("200",JSONObject.fromObject(Info3).getString("state"));
        String Info4 = commonFunc.tokenModule_DestoryToken(tokenAccount5, tokenType2, "10.05");
        assertEquals("200",JSONObject.fromObject(Info4).getString("state"));

//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        String queryInfo11 = tokenModule.tokenGetBalance( tokenAccount3, "");
        String queryInfo12 = tokenModule.tokenGetBalance( tokenAccount5, "");
        String queryInfo5 = tokenModule.tokenGetDestroyBalance();
        String queryInfo6 = tokenModule.tokenGetDestroyBalance();

        assertEquals(false,queryInfo11.contains(tokenType));
        assertEquals(false,queryInfo12.contains(tokenType2));
        assertEquals("0.03",JSONObject.fromObject(queryInfo5).getJSONObject("data").getString(tokenType));
        assertEquals("10.05",JSONObject.fromObject(queryInfo6).getJSONObject("data").getString(tokenType2));


    }


    /**
     * Tc024锁定后转账:
     *
     */
    @Test
    public void TC024_TransferAfterFrozen() throws Exception {

        //20190411增加锁定步骤后进行转账
        log.info("锁定待转账Token: "+tokenType);
        String resp = tokenModule.tokenFreezeToken(tokenType);
//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        List<Map> listModel1 = utilsClass.tokenConstructToken(tokenAccount3,tokenType,"100.25");
        List<Map> list1 = utilsClass.tokenConstructToken(tokenAccount5,tokenType2,"200.555",listModel1);
        String transferInfo= commonFunc.tokenModule_TransferTokenList(tokenAccount1, list1);
        assertEquals(true,transferInfo.contains("toketype(" + tokenType + ") has been freezed!"));
//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        log.info("查询帐号3跟帐号5余额，判断转账是否成功");
        String queryInfo = tokenModule.tokenGetBalance( tokenAccount3, "");
        String queryInfo2 = tokenModule.tokenGetBalance( tokenAccount5, "");
        assertEquals(false,queryInfo.contains(tokenType));
        assertEquals(false,queryInfo2.contains(tokenType2));


        log.info("解除锁定待转账Token: "+tokenType);
        String resp1 = tokenModule.tokenRecoverToken(tokenType);
//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        List<Map> listModel = utilsClass.tokenConstructToken(tokenAccount3,tokenType,"100.25");
        List<Map> list = utilsClass.tokenConstructToken(tokenAccount5,tokenType2,"200.555",listModel);
        transferInfo= commonFunc.tokenModule_TransferTokenList(tokenAccount1,list);
//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        assertThat(transferInfo, containsString("200"));
        log.info("查询帐号3跟帐号5余额，判断转账是否成功");
        queryInfo = tokenModule.tokenGetBalance( tokenAccount3, tokenType);
        queryInfo2 = tokenModule.tokenGetBalance( tokenAccount5, tokenType2);
        assertEquals("100.25",JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType));
        assertEquals("200.555",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString(tokenType2));

    }

    /**
     * Tc040单签转单签异常测试:
     *
     */
    @Test
    public void TC040_SoloProgress() throws Exception {
        //"归集地址向" + PUBKEY3 + "转账3000个" + tokenType+",并向"+PUBKEY4+"转账";
        List<Map> list = utilsClass.tokenConstructToken(tokenAccount3,tokenType,"3000");
        List<Map> list1 = utilsClass.tokenConstructToken(tokenAccount5,tokenType2,"3000",list);

        String transferInfo = commonFunc.tokenModule_TransferTokenList(tokenAccount1,list1);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        //tokenAccount3 向 tokenAccount4和5 转账4000 tokenType
        List<Map> list2 = utilsClass.tokenConstructToken(tokenAccount4,tokenType,"4000");
        List<Map>list3= utilsClass.tokenConstructToken(tokenAccount5,tokenType,"70",list2);
        String recycleInfo2 = commonFunc.tokenModule_TransferTokenList(tokenAccount3,list3);
        assertThat(recycleInfo2, containsString("Insufficient Balance"));


        //tokenAccount3 向 tokenAccount4和5 转账4000 tokenType
        List<Map> list4 = utilsClass.tokenConstructToken(tokenAccount4,tokenType,"4000");
        List<Map>list5 = utilsClass.tokenConstructToken(tokenAccount5,tokenType2,"4001",list4);
        String recycleInfo3 = commonFunc.tokenModule_TransferTokenList(tokenAccount3,list5);
        assertThat(recycleInfo3, containsString("Insufficient Balance"));

        //tokenAccount3 向 tokenAccount4和5 转账4000 tokenType
        List<Map> list6 = utilsClass.tokenConstructToken(tokenAccount4,tokenType,"4000");
        List<Map>list7 = utilsClass.tokenConstructToken(tokenAccount5,tokenType2,"60",list6);
        String recycleInfo4 = commonFunc.tokenModule_TransferTokenList(tokenAccount3,list7);
        assertThat(recycleInfo4, containsString("Insufficient Balance"));


        String Info = commonFunc.tokenModule_DestoryToken(tokenAccount3, tokenType, "3000");
        String Info3 = commonFunc.tokenModule_DestoryToken(tokenAccount5, tokenType2, "3000");
        assertEquals("200",JSONObject.fromObject(Info).getString("state"));
        assertEquals("200",JSONObject.fromObject(Info3).getString("state"));

//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        String queryInfo11 = tokenModule.tokenGetBalance( tokenAccount4, tokenType);
        String queryInfo12 = tokenModule.tokenGetBalance( tokenAccount5, tokenType2);
        String queryInfo5 = tokenModule.tokenGetDestroyBalance();

        assertEquals(false,queryInfo11.contains(tokenType));
        assertEquals(false,queryInfo12.contains(tokenType2));
        assertEquals("3000",JSONObject.fromObject(queryInfo5).getJSONObject("data").getString(tokenType));
        assertEquals("3000",JSONObject.fromObject(queryInfo5).getJSONObject("data").getString(tokenType2));
    }
    /**
     * Tc041单签转单签+多签异常测试:锁定解锁后执行回收
     *
     */
    @Test
    public void TC041_SoloProgress() throws Exception {
        //"归集地址向" + tokenAccount3 + "转账3000个" + tokenType+",并向"+tokenMultiAddr3+"转账";
        List<Map> list = utilsClass.tokenConstructToken(tokenAccount3,tokenType,"3000");
        List<Map> list1 = utilsClass.tokenConstructToken(tokenAccount3,tokenType2,"3000",list);

        String transferInfo = commonFunc.tokenModule_TransferTokenList(tokenAccount1, list1);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));

//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        List<Map> list2 = utilsClass.tokenConstructToken(tokenAccount2,tokenType,"200");
        List<Map>list3= utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType,"7000",list2);
        String transferInfo3 = commonFunc.tokenModule_TransferTokenList(tokenAccount3, list3);
        assertThat(transferInfo3, containsString("Insufficient Balance"));

        List<Map> list4 = utilsClass.tokenConstructToken(tokenAccount2,tokenType,"4000");
        List<Map>list5 = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType,"4001",list4);
        String transferInfo4 = commonFunc.tokenModule_TransferTokenList(tokenAccount3, list5);
        assertThat(transferInfo4, containsString("Insufficient Balance"));

        List<Map> list6 = utilsClass.tokenConstructToken(tokenAccount2,tokenType,"4000");
        List<Map>list7 = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType,"400",list6);
        String transferInfo5 = commonFunc.tokenModule_TransferTokenList(tokenAccount3, list7);
        assertThat(transferInfo5, containsString("Insufficient Balance"));


        //20190411增加锁定解锁操作步骤后进行回收
        log.info("锁定待回收Token: "+tokenType);
        String resp=tokenModule.tokenFreezeToken(tokenType);
//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        log.info("解除锁定待回收Token: "+tokenType);
        String resp1= tokenModule.tokenRecoverToken(tokenType);
//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        log.info("开始回收....");
        String Info = commonFunc.tokenModule_DestoryToken(tokenAccount3, tokenType, "3000");
        String Info2 = commonFunc.tokenModule_DestoryToken(tokenAccount3, tokenType2, "3000");

        assertEquals("200",JSONObject.fromObject(Info).getString("state"));
        assertEquals("200",JSONObject.fromObject(Info2).getString("state"));
//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        log.info("开始查询余额....");
        String response1 = tokenModule.tokenGetBalance(tokenAccount3, "");
        assertEquals("200",JSONObject.fromObject(response1).getString("state"));

        assertEquals(false,response1.contains(tokenType));
        assertEquals(false,response1.contains(tokenType2));

    }

    /**
     * Tc042单签转单签+多签测试:回收前锁定token
     *
     */
    @Test
    public void TC042_SoloProgress() throws Exception {

        List<Map> list=utilsClass.tokenConstructToken(tokenAccount3,tokenType,"3000");
        List<Map> list1=utilsClass.tokenConstructToken(tokenAccount3,tokenType2,"3000",list);
        String transferInfo= commonFunc.tokenModule_TransferTokenList(tokenAccount1,list1);
//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));

        List<Map> list2 = utilsClass.tokenConstructToken(tokenAccount4,tokenType,"200");
        List<Map>list3= utilsClass.tokenConstructToken(tokenMultiAddr3,tokenType,"70",list2);
        String recycleInfo2 = commonFunc.tokenModule_TransferTokenList(tokenAccount3, list3);
        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("state"));
//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        List<Map> list4 = utilsClass.tokenConstructToken(tokenAccount4,tokenType,"400");
        List<Map>list5= utilsClass.tokenConstructToken(tokenMultiAddr3,tokenType2,"401",list4);
        String recycleInfo3 = commonFunc.tokenModule_TransferTokenList(tokenAccount3, list5);
        assertEquals("200",JSONObject.fromObject(recycleInfo3).getString("state"));
//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        //20190411增加锁定操作步骤后进行回收
        log.info("锁定待回收Token: "+tokenType);
        String resp= tokenModule.tokenFreezeToken(tokenType);
//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        log.info("开始回收....");
        String Info = commonFunc.tokenModule_DestoryToken( tokenAccount3, tokenType, "2330");
        String Info1 = commonFunc.tokenModule_DestoryToken( tokenAccount3, tokenType2, "2599");
        String Info2 = commonFunc.tokenModule_DestoryToken( tokenAccount4, tokenType, "600");
        String Info3 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr3, tokenType, "70");
        String Info4 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr3, tokenType2, "401");

        assertEquals("200",JSONObject.fromObject(Info).getString("state"));
        assertEquals("200",JSONObject.fromObject(Info1).getString("state"));
        assertEquals("200",JSONObject.fromObject(Info2).getString("state"));
        assertEquals("200",JSONObject.fromObject(Info3).getString("state"));
        assertEquals("200",JSONObject.fromObject(Info4).getString("state"));


//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        log.info("开始查询余额....");commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        String response1 = tokenModule.tokenGetBalance(tokenAccount3, "");
        String response2 = tokenModule.tokenGetBalance(tokenAccount4, "");
        String response3 = tokenModule.tokenGetBalance(tokenMultiAddr3, "");

        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
        assertEquals("200",JSONObject.fromObject(response2).getString("state"));
        assertEquals("200",JSONObject.fromObject(response3).getString("state"));

        assertEquals(false, response1.contains(tokenType));
        assertEquals(false, response1.contains(tokenType2));
        assertEquals(false, response2.contains(tokenType));
        assertEquals(false, response2.contains(tokenType2));
        assertEquals(false, response3.contains(tokenType));
        assertEquals(false, response3.contains(tokenType2));
    }

    /**
     * Tc244单签接口双花测试:
     *
     */
    @Test
    public void TC0244_SoloProgress() throws Exception {
        //"归集地址向" + PUBKEY3 + "转账3000个" + tokenType+",并向"+PUBKEY4+"转账";
        List<Map> list = utilsClass.tokenConstructToken(tokenAccount3,tokenType2,"3000");
        List<Map> list0 = utilsClass.tokenConstructToken(tokenAccount3,tokenType,"4000",list);
        String transferInfo = commonFunc.tokenModule_TransferTokenList(tokenAccount1, list0);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        String response = tokenModule.tokenGetBalance(tokenAccount1,"");
        assertEquals("17000.876543",JSONObject.fromObject(response).getJSONObject("data").getString(tokenType2));
        assertEquals("6000.123456",JSONObject.fromObject(response).getJSONObject("data").getString(tokenType));


        List<Map> list1= utilsClass.tokenConstructToken(tokenAccount4,tokenType,"300");
        List<Map> list2= utilsClass.tokenConstructToken(tokenAccount4,tokenType,"301");
        String transferInfo1 = commonFunc.tokenModule_TransferTokenList(tokenAccount3, list1);
        String transferInfo2 = commonFunc.tokenModule_TransferTokenList(tokenAccount3, list2);
//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        String queryInfo = tokenModule.tokenGetBalance(tokenAccount4,"");
        assertThat(JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType),
                anyOf(containsString("300"),containsString("301")));



        List<Map> list3= utilsClass.tokenConstructToken(tokenAccount4,tokenType,"400");
        List<Map> list4= utilsClass.tokenConstructToken(tokenAccount4,tokenType2,"411",list3);
        String transferInfo3= commonFunc.tokenModule_TransferTokenList(tokenAccount3, list3);
        String transferInfo4= commonFunc.tokenModule_TransferTokenList(tokenAccount3,list4);
        assertEquals("200",JSONObject.fromObject(transferInfo3).getString("state"));
        assertEquals("200",JSONObject.fromObject(transferInfo4).getString("state"));

//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        String response1 = tokenModule.tokenGetBalance(tokenAccount4,"");
        assertThat(JSONObject.fromObject(response1).getJSONObject("data").getString(tokenType),
                anyOf(containsString("700"),containsString("701")));
//        assertThat(JSONObject.fromObject(response1).getJSONObject("data").getString(tokenType2),
//                anyOf(containsString("411"),containsString("0")));


        List<Map> list5= utilsClass.tokenConstructToken(tokenAccount4,tokenType,"320");
        List<Map> list6= utilsClass.tokenConstructToken(tokenAccount4,tokenType2,"320");
        String transferInfo5= commonFunc.tokenModule_TransferTokenList( tokenAccount3, list5);
        String transferInfo6= commonFunc.tokenModule_TransferTokenList(tokenAccount3, list6);
        assertEquals("200",JSONObject.fromObject(transferInfo5).getString("state"));
        assertEquals("200",JSONObject.fromObject(transferInfo6).getString("state"));

//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        String response2 = tokenModule.tokenGetBalance(tokenAccount4,"");
        assertThat(JSONObject.fromObject(response2).getJSONObject("data").getString(tokenType),
                anyOf(containsString("1020"),containsString("1021")));
        assertThat(JSONObject.fromObject(response2).getJSONObject("data").getString(tokenType2),
                anyOf(containsString("731"),containsString("320")));
    }

    //验证无法转账给自己
    @Test
    public void TransferToSelf()throws Exception{
        List<Map> list = utilsClass.tokenConstructToken(tokenAccount1,tokenType2,"3000");
        String transferInfo= commonFunc.tokenModule_TransferTokenList(tokenAccount1, list);
        assertEquals(true,transferInfo.contains("can't transfer it to yourself"));
        String response = tokenModule.tokenGetBalance(tokenAccount1,tokenType);
        assertEquals(actualAmount1,JSONObject.fromObject(response).getJSONObject("data").getString(tokenType));
    }


//    @Test
//    public void singleAccountDoubleSpend_IssueSelf()throws Exception{
//        String issueAddr = "";
//        String collAddr = "";
//        String issueToken = "";
//        String issAmount ="";
//
//        //单签地址发行token 5000.999999
//        double sAmount = 5000.999999;
//        issueAddr = tokenAccount1;
//        collAddr = tokenAccount1;
//        issAmount = String.valueOf(sAmount);
//
//        //转账信息
//        String from = collAddr;
//        String to = "";
//        String to1 = tokenAccount2;
//        String to2 = tokenMultiAddr1;
//        double trfAmount1 = 100.253;
//        double trfAmount2 = 689.333;
//
//        issueToken = commonFunc.tokenModule_IssueToken(issueAddr,collAddr,issAmount);
//        sleepAndSaveInfo(SLEEPTIME,"issue waiting......");
//
//        String queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
//        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
//
//        //连续向单签账户转账和多签账户转账
//
//        String transferToken = issueToken;
//        String transferAmount = String.valueOf(trfAmount1);
//        to = to1;
//        String transferResp = commonFunc.tokenModule_TransferToken(from,to,transferToken,transferAmount);
//
//        to = to2;
//        transferAmount = String.valueOf(trfAmount2);
//        transferResp = commonFunc.tokenModule_TransferToken(from,to,issueToken,transferAmount);
//
//
//        sleepAndSaveInfo(SLEEPTIME,"transfer waiting......");
//
//        //余额查询
//        queryBalance = tokenModule.tokenGetBalance(collAddr,"");
//        assertEquals(String.valueOf(sAmount - trfAmount1), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
//        queryBalance = tokenModule.tokenGetBalance(to1,issueToken);
//        assertEquals(String.valueOf(trfAmount1), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
//        queryBalance = tokenModule.tokenGetBalance(to2,issueToken);
//        assertEquals(false,queryBalance.contains(issueToken));
//
//        //执行回收
//        String desAddr = collAddr;
//        double desAmount = 500.698547;
//        String desToken = issueToken;
//        String desAmountStr = String.valueOf(desAmount);
//        String destroyResp = commonFunc.tokenModule_DestoryToken (desAddr,desToken,desAmountStr);
//
//        sleepAndSaveInfo(SLEEPTIME,"destroy waiting......");
//
//        //余额查询
//        queryBalance = tokenModule.tokenGetBalance(collAddr,desToken);
//        assertEquals(utilsClass.get6(sAmount - trfAmount1 - desAmount), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));
//        queryBalance = tokenModule.tokenGetBalance(to1,desToken);
//        assertEquals(String.valueOf(trfAmount1), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));
//        queryBalance = tokenModule.tokenGetBalance(to2,desToken);
//        assertEquals(false,queryBalance.contains(desToken));
//
//        queryBalance = tokenModule.tokenGetDestroyBalance();
//        assertEquals(String.valueOf(desAmount), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));
//
//    }
//
//    @Test
//    public void singleAccountDoubleSpend_IssueOther()throws Exception{
//        String issueAddr = "";
//        String collAddr = "";
//        String issueToken = "";
//        String issAmount ="";
//
//        //单签地址发行token 5000.999999
//        double sAmount = 5000.999999;
//        issueAddr = tokenAccount1;
//        collAddr = tokenAccount2;
//        issAmount = String.valueOf(sAmount);
//
//        //转账信息
//        String from = collAddr;
//        String to = "";
//        String to1 = tokenAccount1;
//        String to2 = tokenMultiAddr1;
//        double trfAmount1 = 100.253;
//        double trfAmount2 = 689.333;
//        issueToken = commonFunc.tokenModule_IssueToken(issueAddr,collAddr,issAmount);
//        sleepAndSaveInfo(SLEEPTIME,"issue waiting......");
//
//
//        //查询余额归集地址 和 发行地址
//        String queryBalance = tokenModule.tokenGetBalance(issueAddr,issueToken);
//        assertEquals(false,queryBalance.contains(issueToken));
//
//        queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
//        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
//
//        //连续向单签账户转账和多签账户转账
//        String transferToken = issueToken;
//        String transferAmount = String.valueOf(trfAmount1);
//        to = to1;
//        String transferResp = commonFunc.tokenModule_TransferToken(from,to,transferToken,transferAmount);
//
//        to = to2;
//        transferAmount = String.valueOf(trfAmount2);
//        transferResp = commonFunc.tokenModule_TransferToken(from,to,issueToken,transferAmount);
//
//
//        sleepAndSaveInfo(SLEEPTIME,"transfer waiting......");
//
//        //余额查询
//        queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
//        assertEquals(String.valueOf(sAmount - trfAmount1), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
//        queryBalance = tokenModule.tokenGetBalance(to1,issueToken);
//        assertEquals(String.valueOf(trfAmount1), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
//        queryBalance = tokenModule.tokenGetBalance(to2,issueToken);
//        assertEquals(false,queryBalance.contains(issueToken));
//
//        //执行回收
//        String desAddr = collAddr;
//        double desAmount = 500.698547;
//        String desToken = issueToken;
//        String desAmountStr = String.valueOf(desAmount);
//        String destroyResp = commonFunc.tokenModule_DestoryToken (desAddr,desToken,desAmountStr);
//
//        sleepAndSaveInfo(SLEEPTIME,"destroy waiting......");
//
//        //余额查询
//        queryBalance = tokenModule.tokenGetBalance(collAddr,desToken);
//        assertEquals(utilsClass.get6(sAmount - trfAmount1 - desAmount), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));
//        queryBalance = tokenModule.tokenGetBalance(to1,desToken);
//        assertEquals(String.valueOf(trfAmount1), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));
//        queryBalance = tokenModule.tokenGetBalance(to2,desToken);
//        assertEquals(false,queryBalance.contains(desToken));
//
//        queryBalance = tokenModule.tokenGetDestroyBalance();
//        assertEquals(String.valueOf(desAmount), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));
//
//    }
//
//
//    @Test
//    public void singleAccount_IssueSelf()throws Exception{
//        String issueAddr = "";
//        String collAddr = "";
//        String issueToken = "";
//        String issAmount ="";
//
//        //单签地址发行token 5000.999999
//        double sAmount = 5000.999999;
//        issueAddr = tokenAccount1;
//        collAddr = tokenAccount1;
//        issAmount = String.valueOf(sAmount);
//
//        //转账信息
//        String from = collAddr;
//        String to = "";
//        String to1 = tokenAccount2;
//        String to2 = tokenMultiAddr1;
//        double trfAmount1 = 100.253;
//        double trfAmount2 = 689.333;
//
//        issueToken = commonFunc.tokenModule_IssueToken(issueAddr,collAddr,issAmount);
//        sleepAndSaveInfo(SLEEPTIME,"issue waiting......");
//
//        String queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
//        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
//
//        //连续向单签账户转账和多签账户转账
//
//        String transferToken = issueToken;
//        String transferAmount = String.valueOf(trfAmount1);
//        to = to1;
//        String transferResp = commonFunc.tokenModule_TransferToken(from,to,transferToken,transferAmount);
//
//        sleepAndSaveInfo(SLEEPTIME,"transfer waiting......");
//
//        to = to2;
//        transferAmount = String.valueOf(trfAmount2);
//        transferResp = commonFunc.tokenModule_TransferToken(from,to,issueToken,transferAmount);
//
//
//        sleepAndSaveInfo(SLEEPTIME,"transfer waiting......");
//
//        //余额查询
//        queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
//        assertEquals(String.valueOf(sAmount - trfAmount1- trfAmount2), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
//        queryBalance = tokenModule.tokenGetBalance(to1,issueToken);
//        assertEquals(String.valueOf(trfAmount1), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
//        queryBalance = tokenModule.tokenGetBalance(to2,issueToken);
//        assertEquals(String.valueOf(trfAmount2), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
//
//        //执行回收
//        String desAddr = collAddr;
//        double desAmount = 500.698547;
//        String desToken = issueToken;
//        String desAmountStr = String.valueOf(desAmount);
//        String destroyResp = commonFunc.tokenModule_DestoryToken (desAddr,desToken,desAmountStr);
//
//        sleepAndSaveInfo(SLEEPTIME,"destroy waiting......");
//
//        //余额查询
//        queryBalance = tokenModule.tokenGetBalance(collAddr,desToken);
//        assertEquals(utilsClass.get6(sAmount - trfAmount1 - trfAmount2 - desAmount), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));
//        queryBalance = tokenModule.tokenGetBalance(to1,desToken);
//        assertEquals(String.valueOf(trfAmount1), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));
//        queryBalance = tokenModule.tokenGetBalance(to2,desToken);
//        assertEquals(String.valueOf(trfAmount2), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
//
//        queryBalance = tokenModule.tokenGetDestroyBalance();
//        assertEquals(String.valueOf(desAmount), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));
//
//    }

    @Test
    public void singleAccount_IssueOther()throws Exception{
        String issueAddr = "";
        String collAddr = "";
        String issueToken = "";
        String issAmount ="";

        //单签地址发行token 5000.999999
        double sAmount = 5000.999999;
        issueAddr = tokenAccount1;
        collAddr = tokenAccount2;
        issAmount = String.valueOf(sAmount);

        //转账信息
        String from = collAddr;
        String to = "";
        String to1 = tokenAccount1;
        String to2 = tokenMultiAddr1;
        double trfAmount1 = 100.253;
        double trfAmount2 = 689.333;
        issueToken = commonFunc.tokenModule_IssueToken(issueAddr,collAddr,issAmount);
//        sleepAndSaveInfo(SLEEPTIME,"issue waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);



        //查询余额归集地址 和 发行地址
        String queryBalance = tokenModule.tokenGetBalance(issueAddr,issueToken);
        assertEquals(false,queryBalance.contains(issueToken));

        queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

        //连续向单签账户转账和多签账户转账

        String transferAmount = String.valueOf(trfAmount1);
        List<Map> list = utilsClass.tokenConstructToken(to1,issueToken,transferAmount);
        String transferResp = commonFunc.tokenModule_TransferTokenList(from,list);
//        sleepAndSaveInfo(SLEEPTIME,"transfer waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        transferAmount = String.valueOf(trfAmount2);
        List<Map> list2 = utilsClass.tokenConstructToken(to2,issueToken,transferAmount);
        String transferResp2 = commonFunc.tokenModule_TransferTokenList(from,list2);
//        sleepAndSaveInfo(SLEEPTIME,"transfer waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        //余额查询
        queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(String.valueOf(sAmount - trfAmount1 - trfAmount2), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        queryBalance = tokenModule.tokenGetBalance(to1,issueToken);
        assertEquals(String.valueOf(trfAmount1), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        queryBalance = tokenModule.tokenGetBalance(to2,issueToken);
        assertEquals(String.valueOf(trfAmount2), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

               
        //执行回收
        String desAddr = collAddr;
        double desAmount = 500.698547;
        String desToken = issueToken;
        String desAmountStr = String.valueOf(desAmount);
        String destroyResp = commonFunc.tokenModule_DestoryToken (desAddr,desToken,desAmountStr);

//        sleepAndSaveInfo(SLEEPTIME,"destroy waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        //余额查询
        queryBalance = tokenModule.tokenGetBalance(collAddr,desToken);
        assertEquals(utilsClass.get6(sAmount - trfAmount1 - trfAmount2 - desAmount), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));
        queryBalance = tokenModule.tokenGetBalance(to1,desToken);
        assertEquals(String.valueOf(trfAmount1), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));
        queryBalance = tokenModule.tokenGetBalance(to2,desToken);
        assertEquals(String.valueOf(trfAmount2), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

        queryBalance = tokenModule.tokenGetDestroyBalance();
        assertEquals(String.valueOf(desAmount), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));

    }
    @Test
    public void destoryByTokenTest()throws Exception{
        ArrayList<String> listAddrTTAc = new ArrayList<String>();
        //"归集地址向" + PUBKEY3 + "转账3000个" + tokenType+",并向"+PUBKEY4+"转账";
        List<Map> list = utilsClass.tokenConstructToken(tokenAccount3,tokenType2,"3000");
        List<Map> list2= utilsClass.tokenConstructToken(tokenAccount3,tokenType,"4000",list);

        List<Map> list3 = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType,"3000",list2);
        List<Map> list4 = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType2,"4000",list3);
        String transferInfo = commonFunc.tokenModule_TransferTokenList(tokenAccount1, list4);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        String amount1,amount2;
        if (UtilsClass.PRECISION == 10) {
            amount1 = "3000.1234567891";
            amount2 = "13000.8765432123";
        }else {
            amount1 = "3000.123456";
            amount2 = "13000.876543";
        }

        //此部分与list-list4保持一致
        List<Map> listR = commonFunc.ConstructDesByTokenRespList(tokenAccount3,"4000");
        List<Map> listR2= commonFunc.ConstructDesByTokenRespList(tokenMultiAddr2,"3000",listR);
        List<Map> listR3= commonFunc.ConstructDesByTokenRespList(tokenAccount1,amount1,listR2);

        List<Map> list1R = commonFunc.ConstructDesByTokenRespList(tokenAccount3,"3000");
        List<Map> list1R2= commonFunc.ConstructDesByTokenRespList(tokenMultiAddr2,"4000",list1R);
        List<Map> list1R3= commonFunc.ConstructDesByTokenRespList(tokenAccount1,amount2,list1R2);


        String desInfo = commonFunc.tokenModule_DestoryTokenByTokenType(tokenType);
        assertEquals("200",JSONObject.fromObject(desInfo).getString("state"));
        assertEquals(actualAmount1,JSONObject.fromObject(desInfo).getJSONObject("data").getString("total"));

        JSONArray jsonArray = JSONObject.fromObject(desInfo).getJSONObject("data").getJSONArray("detail");

        assertEquals(3,jsonArray.size());//判断账户数量正确
        assertEquals(true, commonFunc.checkListArray(listR3,jsonArray));//检查detail项目结果正确

//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        String getZeroAc = tokenModule.tokenGetDestroyBalance();
        assertEquals(actualAmount1,JSONObject.fromObject(getZeroAc).getJSONObject("data").getString(tokenType));

        desInfo = commonFunc.tokenModule_DestoryTokenByTokenType(tokenType2);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
        assertEquals(actualAmount2,JSONObject.fromObject(desInfo).getJSONObject("data").getString("total"));

        jsonArray.clear();
        jsonArray = JSONObject.fromObject(desInfo).getJSONObject("data").getJSONArray("detail");

        assertEquals(3,jsonArray.size());//判断账户数量正确
        assertEquals(true, commonFunc.checkListArray(list1R3,jsonArray));//检查detail项目结果正确

//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        getZeroAc = tokenModule.tokenGetDestroyBalance();
        assertEquals(actualAmount2,JSONObject.fromObject(getZeroAc).getJSONObject("data").getString(tokenType2));

        String queryInfo1 = tokenModule.tokenGetBalance(tokenAccount3,"");
        String queryInfo2 = tokenModule.tokenGetBalance(tokenMultiAddr1,"");
        String queryInfo3 = tokenModule.tokenGetBalance(tokenAccount1,"");

        assertEquals(false,queryInfo1.contains(tokenType2));
        assertEquals(false,queryInfo1.contains(tokenType));
        assertEquals(false,queryInfo2.contains(tokenType2));
        assertEquals(false,queryInfo2.contains(tokenType));
        assertEquals(false,queryInfo3.contains(tokenType2));
        assertEquals(false,queryInfo3.contains(tokenType));

    }

    @Test
    public void destoryByList()throws Exception{
        //"归集地址向" + PUBKEY3 + "转账3000个" + tokenType+",并向"+PUBKEY4+"转账";
        List<Map> list = utilsClass.tokenConstructToken(tokenAccount3,tokenType2,"300");
        List<Map> list2= utilsClass.tokenConstructToken(tokenAccount3,tokenType,"400",list);
        List<Map> list3 = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType,"300",list2);
        List<Map> list4 = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType2,"400",list3);

        //执行转账
        String transferInfo = commonFunc.tokenModule_TransferTokenList(tokenAccount1, list4);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        String desInfo = commonFunc.tokenModule_DestoryTokenByList2(list4);
        assertEquals("200",JSONObject.fromObject(desInfo).getString("state"));

//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        String getZeroAc = tokenModule.tokenGetDestroyBalance();
        assertEquals("700",JSONObject.fromObject(getZeroAc).getJSONObject("data").getString(tokenType));
        assertEquals("700",JSONObject.fromObject(getZeroAc).getJSONObject("data").getString(tokenType2));
    }

    //@Test
    public void peerTokenBlockAsyncTest()throws Exception{
        //"归集地址向" + PUBKEY3 + "转账3000个" + tokenType+",并向"+PUBKEY4+"转账";
        List<Map> list = utilsClass.tokenConstructToken(tokenAccount3,tokenType2,"300");
        List<Map> list2= utilsClass.tokenConstructToken(tokenAccount3,tokenType,"400",list);

        List<Map> list3 = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType,"300",list2);
        List<Map> list4 = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType2,"400",list3);
        String transferInfo = commonFunc.tokenModule_TransferTokenList(tokenAccount1, list4);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
        String queryAsync = tokenModule.tokenGetBalance(tokenMultiAddr2,"");
        assertEquals("500",JSONObject.fromObject(transferInfo).getString("state"));
        assertEquals("height mismatch",JSONObject.fromObject(transferInfo).getString("message"));
        assertEquals(false,transferInfo.contains(tokenType));

//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        String query1 = tokenModule.tokenGetBalance(tokenMultiAddr2,"");
        String query2 = tokenModule.tokenGetBalance(tokenAccount3,"");
        assertEquals("400",JSONObject.fromObject(query1).getJSONObject("data").getString(tokenType));
        assertEquals("300",JSONObject.fromObject(query1).getJSONObject("data").getString(tokenType2));
        assertEquals("300",JSONObject.fromObject(query2).getJSONObject("data").getString(tokenType));
        assertEquals("400",JSONObject.fromObject(query2).getJSONObject("data").getString(tokenType2));

        String desInfo = commonFunc.tokenModule_DestoryTokenByList2(list4);
//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        String getZeroAc = tokenModule.tokenGetDestroyBalance();
        assertEquals("700",JSONObject.fromObject(getZeroAc).getJSONObject("data").getString(tokenType));
        assertEquals("700",JSONObject.fromObject(getZeroAc).getJSONObject("data").getString(tokenType2));
    }

    @Test
    public void destory10Addr()throws Exception{
        List<Map> list = utilsClass.tokenConstructToken(tokenMultiAddr1,tokenType,"10");
        List<Map> list2 = utilsClass.tokenConstructToken(tokenAccount2,tokenType,"10",list);
        List<Map> list3 = utilsClass.tokenConstructToken(tokenAccount3,tokenType,"10",list2);
        List<Map> list4 = utilsClass.tokenConstructToken(tokenAccount4,tokenType,"10",list3);
        List<Map> list5 = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType,"10",list4);
        List<Map> list6 = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType2,"10",list5);
        List<Map> list7 = utilsClass.tokenConstructToken(tokenMultiAddr3,tokenType2,"10",list6);
        List<Map> list8 = utilsClass.tokenConstructToken(tokenMultiAddr1,tokenType2,"10",list7);
        List<Map> list9 = utilsClass.tokenConstructToken(tokenAccount2,tokenType2,"10",list8);
        List<Map> list10 = utilsClass.tokenConstructToken(tokenAccount3,tokenType2,"10",list9);
        List<Map> list11 = utilsClass.tokenConstructToken(tokenAccount3,tokenType2,"10",list10);

        List<Map> list12 = utilsClass.tokenConstructToken(tokenAccount1,tokenType2,"10",list);


        String amount1,amount2;
        if (UtilsClass.PRECISION == 10) {
            amount1 = "9950.1234567891";
            amount2 = "19950.8765432123";
        }else {
            amount1 = "9950.123456";
            amount2 = "19950.876543";
        }
        //构造一转多交易详情中的list信息
        List<Map> listT = commonFunc.constructUTXOTxDetailList(tokenAccount1,tokenMultiAddr1,tokenType,"10");
        List<Map> listT2 = commonFunc.constructUTXOTxDetailList(tokenAccount1,tokenAccount2,tokenType,"10",listT);
        List<Map> listT3 = commonFunc.constructUTXOTxDetailList(tokenAccount1,tokenAccount3,tokenType,"10",listT2);
        List<Map> listT4 = commonFunc.constructUTXOTxDetailList(tokenAccount1,tokenAccount4,tokenType,"10",listT3);
        List<Map> listT5 = commonFunc.constructUTXOTxDetailList(tokenAccount1,tokenMultiAddr2,tokenType,"10",listT4);
        List<Map> listT6 = commonFunc.constructUTXOTxDetailList(tokenAccount1,tokenMultiAddr2,tokenType2,"10",listT5);
        List<Map> listT7 = commonFunc.constructUTXOTxDetailList(tokenAccount1,tokenMultiAddr3,tokenType2,"10",listT6);
        List<Map> listT8 = commonFunc.constructUTXOTxDetailList(tokenAccount1,tokenMultiAddr1,tokenType2,"10",listT7);
        List<Map> listT9 = commonFunc.constructUTXOTxDetailList(tokenAccount1,tokenAccount2,tokenType2,"10",listT8);
        List<Map> listT10 = commonFunc.constructUTXOTxDetailList(tokenAccount1,tokenAccount3,tokenType2,"10",listT9);
        List<Map> listT11 = commonFunc.constructUTXOTxDetailList(tokenAccount1,tokenAccount1,tokenType,amount1,listT10);//转出账户信息
        List<Map> listT12 = commonFunc.constructUTXOTxDetailList(tokenAccount1,tokenAccount1,tokenType2,amount2,listT11);//转出账户信息

        //构造多账户回收交易详情中的list信息
        List<Map> listR = commonFunc.constructUTXOTxDetailList(tokenMultiAddr1,zeroAccount,tokenType,"10");
        List<Map> listR2 = commonFunc.constructUTXOTxDetailList(tokenAccount2,zeroAccount,tokenType,"10",listR);
        List<Map> listR3 = commonFunc.constructUTXOTxDetailList(tokenAccount3,zeroAccount,tokenType,"10",listR2);
        List<Map> listR4 = commonFunc.constructUTXOTxDetailList(tokenAccount4,zeroAccount,tokenType,"10",listR3);
        List<Map> listR5 = commonFunc.constructUTXOTxDetailList(tokenMultiAddr2,zeroAccount,tokenType,"10",listR4);
        List<Map> listR6 = commonFunc.constructUTXOTxDetailList(tokenMultiAddr2,zeroAccount,tokenType2,"10",listR5);
        List<Map> listR7 = commonFunc.constructUTXOTxDetailList(tokenMultiAddr3,zeroAccount,tokenType2,"10",listR6);
        List<Map> listR8 = commonFunc.constructUTXOTxDetailList(tokenMultiAddr1,zeroAccount,tokenType2,"10",listR7);
        List<Map> listR9 = commonFunc.constructUTXOTxDetailList(tokenAccount2,zeroAccount,tokenType2,"10",listR8);
        List<Map> listR10 = commonFunc.constructUTXOTxDetailList(tokenAccount3,zeroAccount,tokenType2,"10",listR9);


        String transferInfo = commonFunc.tokenModule_TransferTokenList(tokenAccount1,list10);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
        String transferHash = JSONObject.fromObject(transferInfo).getString("data");

//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);



        String query = tokenModule.tokenGetBalance(tokenAccount1,"");
        assertEquals(true,JSONObject.fromObject(query).getJSONObject("data").getString(tokenType).contains("950."));
        assertEquals(true,JSONObject.fromObject(query).getJSONObject("data").getString(tokenType2).contains("950."));

        String destoryInfo = commonFunc.tokenModule_DestoryTokenByList2(list11);
        assertEquals(true,destoryInfo.contains("Transfer list cannot be more than 10"));

        destoryInfo = commonFunc.tokenModule_DestoryTokenByList2(list10);
        assertEquals("200",JSONObject.fromObject(destoryInfo).getString("state"));
        String desHash = JSONObject.fromObject(destoryInfo).getString("data");
//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        String query2 = tokenModule.tokenGetDestroyBalance();
        assertEquals("50",JSONObject.fromObject(query2).getJSONObject("data").getString(tokenType));
        assertEquals("50",JSONObject.fromObject(query2).getJSONObject("data").getString(tokenType2));

        destoryInfo = commonFunc.tokenModule_DestoryTokenByList2(list12);
        assertEquals("Insufficient Balance",JSONObject.fromObject(destoryInfo).getString("data"));

        //检查多账户回收交易详情信息正确性
        String detailInfo = tokenModule.tokenGetTxDetail(desHash);
        JSONArray jsonArray = JSONObject.fromObject(detailInfo).getJSONObject("data").getJSONObject("UTXO").getJSONArray("Records");
        assertEquals(true,commonFunc.checkListArray(listR10,jsonArray));

        //检查一转多交易信息正确性

        String detailInfo2 = tokenModule.tokenGetTxDetail(transferHash);
        JSONArray jsonArray2 = JSONObject.fromObject(detailInfo2).getJSONObject("data").getJSONObject("UTXO").getJSONArray("Records");
        assertEquals(true,commonFunc.checkListArray(listT12,jsonArray2));

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //再次执行转账，之后执行回收bytokentype
        String transferInfo2 = commonFunc.tokenModule_TransferTokenList(tokenAccount1,list10);
        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("state"));

//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        if (UtilsClass.PRECISION == 10) {
            amount1 = "9900.1234567891";
            amount2 = "19900.8765432123";
        }else {
            amount1 = "9900.123456";
            amount2 = "19900.876543";
        }

        String query3 = tokenModule.tokenGetBalance(tokenAccount1,"");
        assertEquals(amount1,JSONObject.fromObject(query3).getJSONObject("data").getString(tokenType));
        assertEquals(amount2,JSONObject.fromObject(query3).getJSONObject("data").getString(tokenType2));

        //执行回收bytokentype  当前再往下执行回收有问题
        String desInfo2 = commonFunc.tokenModule_DestoryTokenByTokenType(tokenType);
        String desHash2 = JSONObject.fromObject(desInfo2).getJSONObject("data").getString("hash");
        String desInfo3 = commonFunc.tokenModule_DestoryTokenByTokenType(tokenType2);
        String desHash3 = JSONObject.fromObject(desInfo3).getJSONObject("data").getString("hash");

//        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        //tokenType
        List<Map> list2R = commonFunc.constructUTXOTxDetailList(tokenMultiAddr1,zeroAccount,tokenType,"10");
        List<Map> list2R2 = commonFunc.constructUTXOTxDetailList(tokenAccount2,zeroAccount,tokenType,"10",list2R);
        List<Map> list2R3 = commonFunc.constructUTXOTxDetailList(tokenAccount3,zeroAccount,tokenType,"10",list2R2);
        List<Map> list2R4 = commonFunc.constructUTXOTxDetailList(tokenAccount4,zeroAccount,tokenType,"10",list2R3);
        List<Map> list2R5 = commonFunc.constructUTXOTxDetailList(tokenMultiAddr2,zeroAccount,tokenType,"10",list2R4);
        List<Map> list2R6 = commonFunc.constructUTXOTxDetailList(tokenAccount1,zeroAccount,tokenType,amount1,list2R5);

        //tokenType2
        List<Map> list3R6 = commonFunc.constructUTXOTxDetailList(tokenMultiAddr2,zeroAccount,tokenType2,"10");
        List<Map> list3R7 = commonFunc.constructUTXOTxDetailList(tokenMultiAddr3,zeroAccount,tokenType2,"10",list3R6);
        List<Map> list3R8 = commonFunc.constructUTXOTxDetailList(tokenMultiAddr1,zeroAccount,tokenType2,"10",list3R7);
        List<Map> list3R9 = commonFunc.constructUTXOTxDetailList(tokenAccount2,zeroAccount,tokenType2,"10",list3R8);
        List<Map> list3R10 = commonFunc.constructUTXOTxDetailList(tokenAccount3,zeroAccount,tokenType2,"10",list3R9);
        List<Map> list3R11 = commonFunc.constructUTXOTxDetailList(tokenAccount1,zeroAccount,tokenType2,amount2,list3R10);
        //构造回收bytokentype交易详情中的list信息

        //检查回收交易详情信息正确性
        String detailInfo3 = tokenModule.tokenGetTxDetail(desHash2);
        JSONArray jsonArray3 = JSONObject.fromObject(detailInfo3).getJSONObject("data").getJSONObject("UTXO").getJSONArray("Records");
        assertEquals(true,commonFunc.checkListArray(list2R6,jsonArray3));

        String detailInfo4 = tokenModule.tokenGetTxDetail(desHash3);
        JSONArray jsonArray4 = JSONObject.fromObject(detailInfo4).getJSONObject("data").getJSONObject("UTXO").getJSONArray("Records");
        assertEquals(true,commonFunc.checkListArray(list3R11,jsonArray4));
    }


    //发行时大小写敏感性检查
    @Test
    public void issueTokenMatchCase()throws Exception{
        String issueResp = tokenModule.tokenIssue(tokenAccount1,tokenAccount1,tokenType.toLowerCase(),
                "100","发行已有tokentype字符全部小写的token");
        assertEquals("200",JSONObject.fromObject(issueResp).getString("state"));

        String issueResp2 = tokenModule.tokenIssue(tokenAccount1,tokenAccount1,tokenType.toUpperCase(),
                "100","发行已有tokentype字符全部大写的token");
        assertEquals("200",JSONObject.fromObject(issueResp2).getString("state"));

//        sleepAndSaveInfo(SLEEPTIME);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        String query = tokenModule.tokenGetBalance(tokenAccount1,"");
        assertEquals(true,query.contains(tokenType.toLowerCase()));
        assertEquals(true,query.contains(tokenType.toUpperCase()));
        assertEquals(true,query.contains(tokenType));

        query = tokenModule.tokenGetBalance(tokenAccount1,tokenType);
        assertEquals(true,query.contains(tokenType));
        assertEquals(false,query.contains(tokenType.toLowerCase()));
        assertEquals(false,query.contains(tokenType.toUpperCase()));

        query = tokenModule.tokenGetBalance(tokenAccount1,tokenType.toUpperCase());
        assertEquals(false,query.contains(tokenType.toLowerCase()));
        assertEquals(false,query.contains(tokenType));
        assertEquals(true,query.contains(tokenType.toUpperCase()));

        query = tokenModule.tokenGetBalance(tokenAccount1,tokenType.toLowerCase());
        assertEquals(false,query.contains(tokenType.toUpperCase()));
        assertEquals(false,query.contains(tokenType));
        assertEquals(true,query.contains(tokenType.toLowerCase()));
    }


    @AfterClass
    public static void resetAddr()throws Exception{
        SDKADD = TOKENADD;
        BeforeCondition beforeCondition = new BeforeCondition();
        beforeCondition.tokenAddIssueCollAddr();
    }
}
