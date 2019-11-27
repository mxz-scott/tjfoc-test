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
        if(tokenAccount1.isEmpty()) {
            SDKADD = rSDKADD;
            beforeCondition.updatePubPriKey();
            beforeCondition.collAddressTest();
        }
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
     * 发行：sdk 1/2多签地址发行给token平台3/3多签地址
     * 转账：token多签地址转账给sdk单签和多签地址
     * 回收：回收
     * @throws Exception
     */

    @Test
    public void sdk12IssueToTokenModule33() throws Exception {
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

        //http地址设置为sdk平台地址
        SDKADD = rSDKADD;
        log.info("多签发行两种token");
        //sdk多签地址发行给token平台多签地址  sdk多签地址仅可以发行给多签地址
        tokenType = commonFunc.sdkMultiIssueToken(IMPPUTIONADD, issueAmount1,tokenMultiAddr1,prikeyList,pwdList);
        tokenType2 = commonFunc.sdkMultiIssueToken(IMPPUTIONADD, issueAmount2,tokenMultiAddr1,prikeyList,pwdList);
        Thread.sleep(SLEEPTIME);
        log.info("查询归集地址中两种token余额");

        //http地址设置为token平台地址 做账户余额查询
        SDKADD = TOKENADD;
        String response1 = tokenModule.tokenGetBalance(tokenMultiAddr1, "");
        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
        assertEquals(actualAmount1,JSONObject.fromObject(response1).getJSONObject("data").getString(tokenType));
        assertEquals(actualAmount2,JSONObject.fromObject(response1).getJSONObject("data").getString(tokenType2));

        //token转账
        //tokenMultiAddr1 + "归集地址向" + MULITADD4 + "转账10个" + tokenType;'
        List<Map> list = utilsClass.tokenConstructToken(IMPPUTIONADD, tokenType, "10");
        List<Map> list2 = utilsClass.tokenConstructToken(ADDRESS1, tokenType2, "10",list);
        String transferInfoInit = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1,list2);//转账给多签地址
        assertEquals("200",JSONObject.fromObject(transferInfoInit).getString("state"));

        sleepAndSaveInfo(SLEEPTIME,"on chain waiting......");//UTXO关系，两笔交易之间需要休眠

        log.info("查询余额判断转账是否成功");
        String queryInfo = tokenModule.tokenGetBalance(tokenMultiAddr1, "");

//        String desInfo1 = commonFunc.tokenModule_DestoryTokenByTokenType(tokenType);
//        String desInfo2 = commonFunc.tokenModule_DestoryTokenByTokenType(tokenType2);
//
//        String queryInfo2 = tokenModule.tokenGetBalance(tokenMultiAddr1, "");
//        String queryInfo3 = tokenModule.tokenGetDestroyBalance();

        SDKADD = rSDKADD;
        String queryInfo2 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        String queryInfo3 = soloSign.Balance(PRIKEY1, tokenType2);

        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("State"));
        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("State"));

        assertEquals("990.123456",JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType));
        assertEquals("990.876543",JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType2));
        assertEquals("10",JSONObject.fromObject(queryInfo2).getJSONObject("Data").getString("Total"));
        assertEquals("10",JSONObject.fromObject(queryInfo3).getJSONObject("Data").getString("Total"));

        String Info = multiSign.Recycle("", PRIKEY1, tokenType2, "10");
        String Info2 = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenType, "10");
        assertEquals("200",JSONObject.fromObject(Info).getString("State"));
        assertEquals("200",JSONObject.fromObject(Info2).getString("State"));
        sleepAndSaveInfo(SLEEPTIME,"tx on chain waiting......");

        queryInfo3 = soloSign.Balance(PRIKEY1, tokenType2);
        assertEquals("0",JSONObject.fromObject(queryInfo3).getJSONObject("Data").getString("Total"));
        queryInfo3 = multiSign.Balance(IMPPUTIONADD, PRIKEY4, tokenType);
        assertEquals("0",JSONObject.fromObject(queryInfo3).getJSONObject("Data").getString("Total"));

    }

}
