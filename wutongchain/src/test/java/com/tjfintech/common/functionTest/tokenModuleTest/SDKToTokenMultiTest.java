package com.tjfintech.common.functionTest.tokenModuleTest;


import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.Interface.Token;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.junit.Assert.assertEquals;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SDKToTokenMultiTest {
    TestBuilder testBuilder= TestBuilder.getInstance();
    MultiSign multiSign =testBuilder.getMultiSign();
    SoloSign soloSign=testBuilder.getSoloSign();
    Token tokenModule = testBuilder.getToken();
    TokenMultiTest tokenMultiTest = new TokenMultiTest();
    CommonFunc commonFunc = new CommonFunc();

    UtilsClass utilsClass=new UtilsClass();

    private static String tokenType;
    private static String tokenType2;

    private static String issueAmount1;
    private static String issueAmount2;

    private static String actualAmount1;
    private static String actualAmount2;
    ArrayList<String> prikeyList = new ArrayList<>();
    ArrayList<String> pwdList = new ArrayList<>();


    @BeforeClass
    public static void init()throws Exception
    {
        BeforeCondition beforeCondition = new BeforeCondition();
        //添加sdk的发行地址归集地址
        SDKADD = rSDKADD;
        beforeCondition.updatePubPriKey();
        beforeCondition.createAddresses();
        beforeCondition.collAddressTest();

        //添加token模块的账户、发行地址归集地址
        SDKADD = TOKENADD;
        if(tokenAccount1.isEmpty()) {
            beforeCondition = new BeforeCondition();
            beforeCondition.createTokenAccount();
            beforeCondition.tokenAddIssueCollAddr();
        }
    }
    @Before
    public void beforeTest()throws Exception{
        prikeyList.clear();
        pwdList.clear();
    }

    /**
     * sdk多签发行不能发行给单签账户
     * 发行：sdk 1/2多签地址发行给token模块3/3多签地址
     * 转账：token多签地址转账给sdk单签和多签地址
     * 回收：回收
     * @throws Exception
     */

    @Test
    public void sdkMutliIssueToTokenModule33() throws Exception {
        issueAmount1 = "1000.12345678912345";
        issueAmount2 = "1000.876543212345";

        if (UtilsClass.PRECISION == 10) {
            actualAmount1 = "1000.1234567891";
            actualAmount2 = "1000.8765432123";
        }else {
            actualAmount1 = "1000.123456";
            actualAmount2 = "1000.876543";
        }
        //使用1/2多签地址IMPPUTIONADD作为发行地址 - 45无密码私钥签名
        prikeyList.add(PRIKEY4);
        prikeyList.add(PRIKEY5);
        pwdList.add("");
        pwdList.add("");

        //http地址设置为sdk模块地址
        SDKADD = rSDKADD;
        log.info("多签发行两种token");
        //sdk多签地址发行给token模块多签地址  sdk多签地址仅可以发行给多签地址
        tokenType = commonFunc.sdkMultiIssueToken(IMPPUTIONADD, issueAmount1,tokenMultiAddr1,prikeyList,pwdList);
        tokenType2 = commonFunc.sdkMultiIssueToken(IMPPUTIONADD, issueAmount2,tokenMultiAddr1,prikeyList,pwdList);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType02),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);
        log.info("查询归集地址中两种token余额");

        //http地址设置为token模块地址 做账户余额查询
        SDKADD = TOKENADD;
        String response1 = tokenModule.tokenGetBalance(tokenMultiAddr1, "");
        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
        assertEquals(actualAmount1,JSONObject.fromObject(response1).getJSONObject("data").getString(tokenType));
        assertEquals(actualAmount2,JSONObject.fromObject(response1).getJSONObject("data").getString(tokenType2));

        //转账
        //tokenMultiAddr1 + "归集地址向" + MULITADD4 + "转账10个" + tokenType;'
        List<Map> list = utilsClass.tokenConstructToken(IMPPUTIONADD, tokenType, "10");
        List<Map> list2 = utilsClass.tokenConstructToken(ADDRESS1, tokenType2, "10",list);
        String transferInfoInit = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1,list2);//转账给多签地址
        assertEquals("200",JSONObject.fromObject(transferInfoInit).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);//UTXO关系，两笔交易之间需要休眠

        log.info("查询余额判断转账是否成功");
        String queryInfo = tokenModule.tokenGetBalance(tokenMultiAddr1, "");

        //当前token模块不支持非托管账户回收bytokentype
        String desInfo1 = commonFunc.tokenModule_DestoryTokenByTokenType(tokenType);
        String desInfo2 = commonFunc.tokenModule_DestoryTokenByTokenType(tokenType2);
        assertEquals(false,JSONObject.fromObject(desInfo1).getString("state").equals("200"));
        assertEquals(false,JSONObject.fromObject(desInfo2).getString("state").equals("200"));

        SDKADD = rSDKADD;
        String queryInfo2 = multiSign.BalanceByAddr(IMPPUTIONADD, tokenType);
        String queryInfo3 = soloSign.BalanceByAddr(ADDRESS1, tokenType2);

        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("State"));
        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("State"));

        if (UtilsClass.PRECISION == 10) {
            actualAmount1 = "990.1234567891";
            actualAmount2 = "990.8765432123";
        }else {
            actualAmount1 = "990.123456";
            actualAmount2 = "990.876543";
        }

        assertEquals(actualAmount1,JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType));
        assertEquals(actualAmount2,JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType2));
        assertEquals("10",JSONObject.fromObject(queryInfo2).getJSONObject("Data").getString("Total"));
        assertEquals("10",JSONObject.fromObject(queryInfo3).getJSONObject("Data").getString("Total"));

        String Info = multiSign.Recycle("", PRIKEY1, tokenType2, "10");
        String Info2 = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType, "10");
        assertEquals("200",JSONObject.fromObject(Info).getString("State"));
        assertEquals("200",JSONObject.fromObject(Info2).getString("State"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType02),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        queryInfo3 = soloSign.BalanceByAddr(ADDRESS1, tokenType2);
        assertEquals("0",JSONObject.fromObject(queryInfo3).getJSONObject("Data").getString("Total"));
        queryInfo3 = multiSign.BalanceByAddr(IMPPUTIONADD, tokenType);
        assertEquals("0",JSONObject.fromObject(queryInfo3).getJSONObject("Data").getString("Total"));

    }


    /**
     * 发行：token 单签发行给自己
     * 转账：token单签地址转账给sdk单签和多签地址
     * 回收：回收
     * @throws Exception
     */
    @Test
    public void sdkSoloTransferToTokenSoloMulti() throws Exception {
        issueAmount1 = "1000.12345678912345";
        issueAmount2 = "1000.876543212345";

        if (UtilsClass.PRECISION == 10) {
            actualAmount1 = "1000.1234567891";
            actualAmount2 = "1000.8765432123";
        }else {
            actualAmount1 = "1000.123456";
            actualAmount2 = "1000.876543";
        }
        //使用1/2多签地址IMPPUTIONADD作为发行地址 - 45无密码私钥签名
        prikeyList.add(PRIKEY4);
        prikeyList.add(PRIKEY5);
        pwdList.add("");
        pwdList.add("");

        //http地址设置为sdk模块地址
        SDKADD = TOKENADD;

        tokenType = commonFunc.tokenModule_IssueToken(tokenAccount1,tokenAccount1,issueAmount1);
        tokenType2 = commonFunc.tokenModule_IssueToken(tokenAccount1,tokenAccount1,issueAmount2);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        log.info("查询归集地址中两种token余额");
        String response1 = tokenModule.tokenGetBalance(tokenAccount1, "");
        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
        assertEquals(actualAmount1,JSONObject.fromObject(response1).getJSONObject("data").getString(tokenType));
        assertEquals(actualAmount2,JSONObject.fromObject(response1).getJSONObject("data").getString(tokenType2));

        //转账
        //tokenMultiAddr1 + "归集地址向" + MULITADD4 + "转账10个" + tokenType;'
        List<Map> list = utilsClass.tokenConstructToken(IMPPUTIONADD, tokenType, "10");
        List<Map> list2 = utilsClass.tokenConstructToken(ADDRESS1, tokenType2, "10",list);
        String transferInfoInit = commonFunc.tokenModule_TransferTokenList(tokenAccount1,list2);//转账给多签地址
        assertEquals("200",JSONObject.fromObject(transferInfoInit).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);//UTXO关系，两笔交易之间需要休眠

        log.info("查询余额判断转账是否成功");
        String queryInfo = tokenModule.tokenGetBalance(tokenAccount1, "");

        //当前token模块不支持非托管账户回收bytokentype
        String desInfo1 = commonFunc.tokenModule_DestoryTokenByTokenType(tokenType);
        String desInfo2 = commonFunc.tokenModule_DestoryTokenByTokenType(tokenType2);
        assertEquals(false,JSONObject.fromObject(desInfo1).getString("state").equals("200"));
        assertEquals(false,JSONObject.fromObject(desInfo2).getString("state").equals("200"));

        SDKADD = rSDKADD;
        String queryInfo2 = multiSign.BalanceByAddr(IMPPUTIONADD, tokenType);
        String queryInfo3 = soloSign.BalanceByAddr(ADDRESS1, tokenType2);

        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("State"));
        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("State"));

        if (UtilsClass.PRECISION == 10) {
            actualAmount1 = "990.1234567891";
            actualAmount2 = "990.8765432123";
        }else {
            actualAmount1 = "990.123456";
            actualAmount2 = "990.876543";
        }

        assertEquals(actualAmount1,JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType));
        assertEquals(actualAmount2,JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType2));
        assertEquals("10",JSONObject.fromObject(queryInfo2).getJSONObject("Data").getString("Total"));
        assertEquals("10",JSONObject.fromObject(queryInfo3).getJSONObject("Data").getString("Total"));

        String Info = multiSign.Recycle("", PRIKEY1, tokenType2, "10");
        String Info2 = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType, "10");
        assertEquals("200",JSONObject.fromObject(Info).getString("State"));
        assertEquals("200",JSONObject.fromObject(Info2).getString("State"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType02),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        queryInfo3 = soloSign.BalanceByAddr(ADDRESS1, tokenType2);
        assertEquals("0",JSONObject.fromObject(queryInfo3).getJSONObject("Data").getString("Total"));
        queryInfo3 = multiSign.BalanceByAddr(IMPPUTIONADD, tokenType);
        assertEquals("0",JSONObject.fromObject(queryInfo3).getJSONObject("Data").getString("Total"));

    }

    /**
     * token多签发行给sdk多签地址
     * 发行：token 单签 多签地址发行给 sdk模块1/2多签地址
     * 转账：sdk多签地址转账给token单签和多签地址
     * 回收：回收
     * @throws Exception
     */

    @Test
    public void tokenMutliIssueToSDKMulti() throws Exception {
        issueAmount1 = "1000.12345678912345";
        issueAmount2 = "1000.876543212345";

        if (UtilsClass.PRECISION == 10) {
            actualAmount1 = "1000.1234567891";
            actualAmount2 = "1000.8765432123";
        }else {
            actualAmount1 = "1000.123456";
            actualAmount2 = "1000.876543";
        }

        prikeyList.add(PRIKEY4);
        prikeyList.add(PRIKEY5);
        pwdList.add("");
        pwdList.add("");

        //http地址设置为sdk模块地址
        SDKADD = TOKENADD;
        log.info("多签发行两种token");
        //sdk多签地址发行给token模块多签地址  sdk多签地址仅可以发行给多签地址
        tokenType = commonFunc.tokenModule_IssueToken(tokenAccount1,IMPPUTIONADD,issueAmount1);
        tokenType2 = commonFunc.tokenModule_IssueToken(tokenMultiAddr1,IMPPUTIONADD,issueAmount2);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        log.info("查询归集地址中两种token余额");

        //http地址设置为sdk模块地址 做账户余额查询
        SDKADD = rSDKADD;
        log.info("查询归集地址中两种token余额");
        String response1 = multiSign.BalanceByAddr(IMPPUTIONADD, tokenType);
        String response2 = multiSign.BalanceByAddr(IMPPUTIONADD, tokenType2);
        assertEquals("200",JSONObject.fromObject(response1).getString("State"));
        assertEquals(actualAmount1,JSONObject.fromObject(response1).getJSONObject("Data").getString("Total"));
        assertEquals("200",JSONObject.fromObject(response2).getString("State"));
        assertEquals(actualAmount2,JSONObject.fromObject(response2).getJSONObject("Data").getString("Total"));

        //转账
        String transferData = "归集地址向" + "tokenAccount1" + "转账10个" + tokenType+"归集地址向" + "tokenMultiAddr1" + "转账10个" + tokenType;
        List<Map>list=utilsClass.constructToken(tokenAccount1,tokenType,"10");
        List<Map>list2=utilsClass.constructToken(tokenMultiAddr1,tokenType2,"10",list);
        log.info(transferData);
        String transferInfo= multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD,list2);//不同币种
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("State"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType02),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        if (UtilsClass.PRECISION == 10) {
            actualAmount1 = "990.1234567891";
            actualAmount2 = "990.8765432123";
        }else {
            actualAmount1 = "990.123456";
            actualAmount2 = "990.876543";
        }

        log.info("查询余额判断转账是否成功");
        String queryInfo= multiSign.BalanceByAddr(IMPPUTIONADD,tokenType);
        String queryInfo2= multiSign.BalanceByAddr(IMPPUTIONADD,tokenType2);
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("State"));
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("State"));
        assertEquals(actualAmount1,JSONObject.fromObject(queryInfo).getJSONObject("Data").getString("Total"));
        assertEquals(actualAmount2,JSONObject.fromObject(queryInfo2).getJSONObject("Data").getString("Total"));

        SDKADD = TOKENADD;
        String queryInfo3 = tokenModule.tokenGetBalance(tokenAccount1,tokenType);
        String queryInfo4 = tokenModule.tokenGetBalance(tokenMultiAddr1,tokenType2);
        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("state"));
        assertEquals("10",JSONObject.fromObject(queryInfo3).getJSONObject("data").getString(tokenType));
        assertEquals("10",JSONObject.fromObject(queryInfo4).getJSONObject("data").getString(tokenType2));

        //当前token模块不支持非托管账户回收bytokentype
        String desInfo1 = commonFunc.tokenModule_DestoryTokenByTokenType(tokenType);
        String desInfo2 = commonFunc.tokenModule_DestoryTokenByTokenType(tokenType2);
        assertEquals(false,JSONObject.fromObject(desInfo1).getString("state").equals("200"));
        assertEquals(false,JSONObject.fromObject(desInfo2).getString("state").equals("200"));

        List<Map>listT = utilsClass.tokenConstructToken(tokenAccount1,tokenType,"10");
        List<Map>listT2 = utilsClass.tokenConstructToken(tokenMultiAddr1,tokenType2,"10",listT);
        String Info = commonFunc.tokenModule_DestoryTokenByList2(listT2);

        assertEquals("200",JSONObject.fromObject(Info).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        String queryInfo5 = tokenModule.tokenGetBalance(tokenAccount1,"");
        String queryInfo6 = tokenModule.tokenGetBalance(tokenAccount1,"");
        assertEquals(false,queryInfo5.contains(tokenType));
        assertEquals(false,queryInfo5.contains(tokenType2));
        assertEquals(false,queryInfo6.contains(tokenType));
        assertEquals(false,queryInfo6.contains(tokenType2));

    }


    /**
     * 发行：token 单签发行给sdk模块3/3多签地址
     * 转账：sdk多签地址转账给token单签和多签地址
     * 回收：回收
     * @throws Exception
     */
    @Test
    public void tokenSoloMultiIssueToSoloMulti() throws Exception {
        issueAmount1 = "1000.12345678912345";
        issueAmount2 = "1000.876543212345";

        if (UtilsClass.PRECISION == 10) {
            actualAmount1 = "1000.1234567891";
            actualAmount2 = "1000.8765432123";
        }else {
            actualAmount1 = "1000.123456";
            actualAmount2 = "1000.876543";
        }

        prikeyList.add(PRIKEY4);
        prikeyList.add(PRIKEY5);
        pwdList.add("");
        pwdList.add("");

        //http地址设置为sdk模块地址
        SDKADD = TOKENADD;
        log.info("多签发行两种token");
        //sdk多签地址发行给token模块多签地址  sdk多签地址仅可以发行给多签地址
        tokenType = commonFunc.tokenModule_IssueToken(tokenAccount1,ADDRESS1,issueAmount1);
        tokenType2 = commonFunc.tokenModule_IssueToken(tokenMultiAddr1,ADDRESS1,issueAmount2);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        log.info("查询归集地址中两种token余额");

        //http地址设置为sdk模块地址 做账户余额查询
        SDKADD = rSDKADD;
        log.info("查询归集地址中两种token余额");
        String response1 = soloSign.BalanceByAddr(ADDRESS1, tokenType);
        String response2 = soloSign.BalanceByAddr(ADDRESS1, tokenType2);
        assertEquals("200",JSONObject.fromObject(response1).getString("State"));
        assertEquals(actualAmount1,JSONObject.fromObject(response1).getJSONObject("Data").getString("Total"));
        assertEquals("200",JSONObject.fromObject(response2).getString("State"));
        assertEquals(actualAmount2,JSONObject.fromObject(response2).getJSONObject("Data").getString("Total"));

        //转账
        String transferData = "归集地址向" + "tokenAccount1" + "转账10个" + tokenType+"归集地址向" + "tokenMultiAddr1" + "转账10个" + tokenType;
        List<Map> list = soloSign.constructToken(tokenAccount1,tokenType,"10");
        List<Map> list2 = soloSign.constructToken(tokenMultiAddr1,tokenType2,"10",list);

        log.info(transferData);
        String transferInfo= soloSign.Transfer(list2,PRIKEY1,transferData);//不同币种
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("State"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        if (UtilsClass.PRECISION == 10) {
            actualAmount1 = "990.1234567891";
            actualAmount2 = "990.8765432123";
        }else {
            actualAmount1 = "990.123456";
            actualAmount2 = "990.876543";
        }

        log.info("查询余额判断转账是否成功");
        String queryInfo = soloSign.BalanceByAddr(ADDRESS1,tokenType);
        String queryInfo2 = soloSign.BalanceByAddr(ADDRESS1,tokenType2);
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("State"));
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("State"));
        assertEquals(actualAmount1,JSONObject.fromObject(queryInfo).getJSONObject("Data").getString("Total"));
        assertEquals(actualAmount2,JSONObject.fromObject(queryInfo2).getJSONObject("Data").getString("Total"));

        SDKADD = TOKENADD;
        String queryInfo3 = tokenModule.tokenGetBalance(tokenAccount1,tokenType);
        String queryInfo4 = tokenModule.tokenGetBalance(tokenMultiAddr1,tokenType2);
        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("state"));
        assertEquals("10",JSONObject.fromObject(queryInfo3).getJSONObject("data").getString(tokenType));
        assertEquals("10",JSONObject.fromObject(queryInfo4).getJSONObject("data").getString(tokenType2));

        //当前token模块不支持非托管账户回收bytokentype
        String desInfo1 = commonFunc.tokenModule_DestoryTokenByTokenType(tokenType);
        String desInfo2 = commonFunc.tokenModule_DestoryTokenByTokenType(tokenType2);
        assertEquals(false,JSONObject.fromObject(desInfo1).getString("state").equals("200"));
        assertEquals(false,JSONObject.fromObject(desInfo2).getString("state").equals("200"));

        List<Map>listT = utilsClass.tokenConstructToken(tokenAccount1,tokenType,"10");
        List<Map>listT2 = utilsClass.tokenConstructToken(tokenMultiAddr1,tokenType2,"10",listT);
        String Info = commonFunc.tokenModule_DestoryTokenByList2(listT2);

        assertEquals("200",JSONObject.fromObject(Info).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        String queryInfo5 = tokenModule.tokenGetBalance(tokenAccount1,"");
        String queryInfo6 = tokenModule.tokenGetBalance(tokenAccount1,"");
        assertEquals(false,queryInfo5.contains(tokenType));
        assertEquals(false,queryInfo5.contains(tokenType2));
        assertEquals(false,queryInfo6.contains(tokenType));
        assertEquals(false,queryInfo6.contains(tokenType2));

    }


}
