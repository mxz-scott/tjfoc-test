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
public class TokenSyncShortTimeoutTest {
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
        syncFlag = true;
//        SDKADD = TOKENADD;
        if(tokenAccount1.isEmpty()) {
            BeforeCondition beforeCondition = new BeforeCondition();
            beforeCondition.createTokenAccount();
            beforeCondition.tokenAddIssueCollAddr();
        }
    }

    @AfterClass
    public static void afterConfig(){
        syncTimeout = 8;
    }

    @Before
    public void beforeConfig() throws Exception {
        syncTimeout = 8;

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

//        assertThat(tokenType+"??????????????????",response1, containsString("200"));
//        assertThat(tokenType+"??????????????????",response2, containsString("200"));
        assertThat(tokenType+"?????????????????????",response1, containsString(actualAmount1));
        assertThat(tokenType+"?????????????????????",response2, containsString(actualAmount2));
    }


    @Test
    public void shortTimeoutTest() throws Exception {
        ArrayList<String> hashList = new ArrayList<>();
        //???????????????????????????timeout????????????????????????
        syncTimeout=0;
        String resp = tokenModule.tokenCreateStore("short timeout" + Random(6));
        assertEquals("400",JSONObject.fromObject(resp).getString("state"));
        assertEquals(true,resp.contains("??????????????????"));

        String issueToken = "tokenSoMU_"+ UtilsClass.Random(12);
        log.info("??????????????? " + tokenAccount2);
        log.info("??????????????? " + tokenAccount2);
        String comments = tokenAccount2 + " ??????token???" + issueToken + " ?????????" + "500";
        log.info(comments);
        String issueResp = tokenModule.tokenIssue(tokenAccount2,tokenAccount2,issueToken,"500",comments);
        assertEquals("400",JSONObject.fromObject(issueResp).getString("state"));
        assertEquals(true,issueResp.contains("??????????????????"));

        sleepAndSaveInfo(7000,"????????????????????????????????????");

        log.info("?????????????????????token??????");
        String response1 = tokenModule.tokenGetBalance( tokenAccount2, issueToken);
        assertEquals("500",JSONObject.fromObject(response1).getJSONObject("data").getString(issueToken));


        //??????
        List<Map> list = utilsClass.tokenConstructToken(tokenAccount3,issueToken,"30");
        List<Map> list0 = utilsClass.tokenConstructToken(tokenAccount3,issueToken,"40");

        String transferInfo = commonFunc.tokenModule_TransferTokenList(tokenAccount2, list);
        String transferInfo2 = commonFunc.tokenModule_TransferTokenList(tokenAccount2, list0);

        assertEquals("400",JSONObject.fromObject(transferInfo).getString("state"));
        assertEquals(true,transferInfo2.contains("??????????????????"));

        assertEquals("400",JSONObject.fromObject(transferInfo2).getString("state"));
        assertEquals(true,transferInfo2.contains("??????????????????"));


        sleepAndSaveInfo(7000,"????????????????????????????????????");

        String queryInfo = tokenModule.tokenGetBalance(tokenAccount3,issueToken);
        assertEquals("30",JSONObject.fromObject(queryInfo).getJSONObject("data").getString(issueToken));

        String queryInfo2 = tokenModule.tokenGetBalance(tokenAccount2,issueToken);
        assertEquals("470",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString(issueToken));

        //??????by list
        //"???????????????" + PUBKEY3 + "??????3000???" + tokenType+",??????"+PUBKEY4+"??????";
        List<Map> listRec = utilsClass.tokenConstructToken(tokenAccount3,issueToken,"10");
        List<Map> listRec2= utilsClass.tokenConstructToken(tokenAccount2,issueToken,"40",listRec);

        String desInfo = commonFunc.tokenModule_DestoryTokenByList2(listRec2);
        assertEquals("400",JSONObject.fromObject(desInfo).getString("state"));
        assertEquals(true,desInfo.contains("??????????????????"));

        sleepAndSaveInfo(7000,"????????????????????????????????????");

        queryInfo = tokenModule.tokenGetBalance(tokenAccount3,issueToken);
        assertEquals("20",JSONObject.fromObject(queryInfo).getJSONObject("data").getString(issueToken));

        queryInfo2 = tokenModule.tokenGetBalance(tokenAccount2,issueToken);
        assertEquals("430",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString(issueToken));

        String zeroQuery = tokenModule.tokenGetDestroyBalance();
        assertEquals("50",JSONObject.fromObject(zeroQuery).getJSONObject("data").getString(issueToken));

        //??????by token
        String desInfo2 = commonFunc.tokenModule_DestoryTokenByTokenType(issueToken);
        assertEquals("400",JSONObject.fromObject(desInfo2).getString("state"));

        sleepAndSaveInfo(7000,"????????????????????????????????????");
        queryInfo = tokenModule.tokenGetBalance(tokenAccount3,"");
        assertEquals(false,queryInfo.contains(issueToken));

        queryInfo2 = tokenModule.tokenGetBalance(tokenAccount2,"");
        assertEquals(false,queryInfo2.contains(issueToken));

        zeroQuery = tokenModule.tokenGetDestroyBalance();
        assertEquals("500",JSONObject.fromObject(zeroQuery).getJSONObject("data").getString(issueToken));
    }

    //???????????????????????????api????????????????????????
    @Test
    public void destoryByTokenTest()throws Exception{
        //???????????????????????????timeout????????????????????????
        syncTimeout=0;
        String resp = tokenModule.tokenCreateStore("short timeout" + Random(6));
        assertEquals("400",JSONObject.fromObject(resp).getString("state"));
        assertEquals(true,resp.contains("??????????????????"));

        syncTimeout = 8;

        ArrayList<String> listAddrTTAc = new ArrayList<String>();
        //"???????????????" + PUBKEY3 + "??????3000???" + tokenType+",??????"+PUBKEY4+"??????";
        List<Map> list = utilsClass.tokenConstructToken(tokenAccount3,tokenType2,"3000");
        List<Map> list2= utilsClass.tokenConstructToken(tokenAccount3,tokenType,"4000",list);

        List<Map> list3 = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType,"3000",list2);
        List<Map> list4 = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType2,"4000",list3);
        String transferInfo = commonFunc.tokenModule_TransferTokenList(tokenAccount1, list4);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));

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

        //????????????list-list4????????????
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

        assertEquals(3,jsonArray.size());//????????????????????????
        assertEquals(true, commonFunc.checkListArray(listR3,jsonArray));//??????detail??????????????????


        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashTypeDesByType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        String getZeroAc = tokenModule.tokenGetDestroyBalance();
        assertEquals(actualAmount1,JSONObject.fromObject(getZeroAc).getJSONObject("data").getString(tokenType));

        desInfo = commonFunc.tokenModule_DestoryTokenByTokenType(tokenType2);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
        assertEquals(actualAmount2,JSONObject.fromObject(desInfo).getJSONObject("data").getString("total"));

        jsonArray.clear();
        jsonArray = JSONObject.fromObject(desInfo).getJSONObject("data").getJSONArray("detail");

        assertEquals(3,jsonArray.size());//????????????????????????
        assertEquals(true, commonFunc.checkListArray(list1R3,jsonArray));//??????detail??????????????????


        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashTypeDesByType),
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

        //???????????????????????????timeout????????????????????????
        syncTimeout=0;
        String resp = tokenModule.tokenCreateStore("short timeout" + Random(6));
        assertEquals("400",JSONObject.fromObject(resp).getString("state"));
        assertEquals(true,resp.contains("??????????????????"));

        syncTimeout = 8;

        //"???????????????" + PUBKEY3 + "??????3000???" + tokenType+",??????"+PUBKEY4+"??????";
        List<Map> list = utilsClass.tokenConstructToken(tokenAccount3,tokenType2,"300");
        List<Map> list2= utilsClass.tokenConstructToken(tokenAccount3,tokenType,"400",list);
        List<Map> list3 = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType,"300",list2);
        List<Map> list4 = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType2,"400",list3);

        //????????????
        String transferInfo = commonFunc.tokenModule_TransferTokenList(tokenAccount1, list4);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        String desInfo = commonFunc.tokenModule_DestoryTokenByList2(list4);
        assertEquals("200",JSONObject.fromObject(desInfo).getString("state"));


        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        String getZeroAc = tokenModule.tokenGetDestroyBalance();
        assertEquals("700",JSONObject.fromObject(getZeroAc).getJSONObject("data").getString(tokenType));
        assertEquals("700",JSONObject.fromObject(getZeroAc).getJSONObject("data").getString(tokenType2));
    }
}
