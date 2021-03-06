package com.tjfintech.common.functionTest.tokenModuleTest;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.Token;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static net.sf.ezmorph.test.ArrayAssertions.assertEquals;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TokenSoloInvalidTest {
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
    //@Test
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

        log.info("????????????token");
        tokenType = commonFunc.tokenModule_IssueToken(tokenAccount1,tokenAccount1,issueAmount1);
        tokenType2 = commonFunc.tokenModule_IssueToken(tokenAccount1,tokenAccount1,issueAmount2);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        log.info("???????????????????????????token??????");
        String response1 = tokenModule.tokenGetBalance( tokenAccount1, tokenType);
        String response2 = tokenModule.tokenGetBalance( tokenAccount1, tokenType2);

        assertThat(tokenType+"??????????????????",response1, containsString("200"));
        assertThat(tokenType+"??????????????????",response2, containsString("200"));
        assertThat(tokenType+"?????????????????????",response1, containsString(actualAmount1));
        assertThat(tokenType+"?????????????????????",response2, containsString(actualAmount2));
    }


    /**
     * TC247??????token???, ?????????????????????????????????????????????data????????????
     * 20200724 ????????????????????????
     */
//    @Test
    public void TC247_issueThenStore() throws Exception {
        String response = tokenModule.tokenCreateStore(tokenType);
        Thread.sleep(1*1000);
        String response2= tokenModule.tokenCreateStore(tokenType);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
        assertEquals("400",JSONObject.fromObject(response2).getString("state"));
        assertThat(response2,
                anyOf(containsString("Duplicate transaction"),
                        containsString("transactionFilter exist")));

    }

    /**
     * TC251??????????????????token
     */
    @Test
    public void TC251_issueDoubleInvalid() throws Exception {

        Thread.sleep(2000);
        String issueInfo2 = tokenModule.tokenIssue(tokenAccount1, tokenAccount3, tokenType, "1000","??????token1");
        String issueInfo3 = tokenModule.tokenIssue(tokenAccount1, tokenAccount2, tokenType, "1000","??????token");
//        assertThat(issueInfo2,containsString("tokentype has been used :" + tokenType));//20200727 ???????????????????????????
//        assertThat(issueInfo3,containsString("tokentype has been used :" + tokenType));

        log.info("?????????????????????token??????");
        String response1 = tokenModule.tokenGetBalance(tokenAccount3, tokenType);
        assertThat(response1, containsString("200"));



    }

    @Test
    public void issueExtMax_IssueSelf()throws Exception{
        String issueAddr = "";
        String collAddr = "";
        String issueToken = "";
        String issAmount ="";

        //??????????????????token 5000.999999
        issueAddr = tokenAccount1;
        collAddr = tokenAccount1;
        issAmount = "18446744073709.1";

        issueToken = commonFunc.tokenModule_IssueToken(issueAddr,collAddr,issAmount);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        String queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(false, queryBalance.contains(issueToken));

    }




    //??????????????????
    @Test
    public void destoryExtBalance()throws Exception{
        String issueAddr = "";
        String collAddr = "";
        String issueToken = "";
        String issAmount ="";

        //??????????????????token 5000.999999
        double sAmount = 500.999999;
        issueAddr = tokenAccount1;
        collAddr = tokenAccount1;
        issAmount = String.valueOf(sAmount);

        issueToken = commonFunc.tokenModule_IssueToken(issueAddr,collAddr,issAmount);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        String queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));


        //????????????????????????
        String desAddr = collAddr;
        double desAmount = 510.698547;
        String desToken = issueToken;
        String desAmountStr = String.valueOf(desAmount);
        String destroyResp = commonFunc.tokenModule_DestoryToken(desAddr,desToken,desAmountStr);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        //????????????
        queryBalance = tokenModule.tokenGetBalance(collAddr,desToken);
        assertEquals("500.999999", JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));

        queryBalance = tokenModule.tokenGetDestroyBalance();
        assertEquals(false,queryBalance.contains(desToken));

    }


    //tokenType????????????????????????
    @Test
    public void testMatchCaseQueryBalance()throws Exception{

        //????????????????????????????????????????????????  ???????????????
        log.info("????????????????????????????????????????????????  ???????????????");
        String query = tokenModule.tokenGetBalance(tokenAccount1.toLowerCase(),tokenType);
        assertEquals("invalid address",JSONObject.fromObject(query).getString("data"));
//        assertEquals(true,
//                JSONObject.fromObject(query).getString("data").contains(tokenType));
//
        query = tokenModule.tokenGetBalance(tokenAccount1.toUpperCase(),tokenType);
        assertEquals("invalid address",JSONObject.fromObject(query).getString("data"));
//        assertEquals(true,
//                JSONObject.fromObject(query).getString("data").contains(tokenType));


        log.info("????????????tokentype????????????");
        //????????????tokentype????????????
        query = tokenModule.tokenGetBalance(tokenAccount1,tokenType.toUpperCase());
        assertEquals(false,
                JSONObject.fromObject(query).getString("data").contains(tokenType.toUpperCase()));

        query = tokenModule.tokenGetBalance(tokenAccount1,tokenType.toLowerCase());
        assertEquals(false,
                JSONObject.fromObject(query).getString("data").contains(tokenType.toLowerCase()));

    }

    @Test
    public void testMatchCaseTransfer()throws Exception{

        //???????????????????????????
        //????????????tokentype??????
        log.info("???????????????????????????");
        List<Map> list = utilsClass.tokenConstructToken(tokenAccount3,tokenType.toLowerCase(),"10");
        String transferInfo = commonFunc.tokenModule_TransferTokenList(tokenAccount1, list);
        assertEquals("400",JSONObject.fromObject(transferInfo).getString("state"));
        assertEquals("Insufficient Balance",JSONObject.fromObject(transferInfo).getString("data"));

        //????????????tokentype??????
        list = utilsClass.tokenConstructToken(tokenAccount3,tokenType.toUpperCase(),"10");
        transferInfo = commonFunc.tokenModule_TransferTokenList(tokenAccount1, list);
        assertEquals("400",JSONObject.fromObject(transferInfo).getString("state"));
        assertEquals("Insufficient Balance",JSONObject.fromObject(transferInfo).getString("data"));
    }

    @Test
    public void testMatchCaseDestroy()throws Exception{
        List<Map> list;
        //???????????????????????????
        log.info("???????????????????????????");
        list = utilsClass.tokenConstructToken(tokenAccount1,tokenType.toLowerCase(),"10");
        String desResp = commonFunc.tokenModule_DestoryTokenByList2(list);
        assertEquals("400",JSONObject.fromObject(desResp).getString("state"));
        assertEquals("Insufficient Balance",JSONObject.fromObject(desResp).getString("data"));

        list = utilsClass.tokenConstructToken(tokenAccount1,tokenType.toUpperCase(),"10");
        desResp = commonFunc.tokenModule_DestoryTokenByList2(list);
        assertEquals("400",JSONObject.fromObject(desResp).getString("state"));
        assertEquals("Insufficient Balance",JSONObject.fromObject(desResp).getString("data"));


        desResp = commonFunc.tokenModule_DestoryTokenByTokenType(tokenType.toUpperCase());
        assertEquals("400",JSONObject.fromObject(desResp).getString("state"));
        assertEquals("invalid tokenType",JSONObject.fromObject(desResp).getString("data").trim());

        desResp = commonFunc.tokenModule_DestoryTokenByTokenType(tokenType.toLowerCase());
        assertEquals("400",JSONObject.fromObject(desResp).getString("state"));
        assertEquals("invalid tokenType",JSONObject.fromObject(desResp).getString("data").trim());
    }
}
