package com.tjfintech.common.functionTest.tokenModuleTest;


import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.Interface.Token;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;


@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TokenTwoApiMultiTest {
    TestBuilder testBuilder= TestBuilder.getInstance();
    MultiSign multiSign =testBuilder.getMultiSign();
    SoloSign soloSign= testBuilder.getSoloSign();
    Token tokenModule = testBuilder.getToken();
    UtilsClass utilsClass=new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();

    private static String TOKENADD2 = "http://10.1.5.225:7083";
    private static String token2Account1;
    private static String tokenMultiAddr12;
    private static String token2Account2;
    private static String tokenMultiAddr21;

    private static String tokenType;
    private static String tokenType2;

    private static String issueAmount1;
    private static String issueAmount2;

    private static String actualAmount1;
    private static String actualAmount2;

    String issueResp = "";
    ArrayList<String> listTag = new ArrayList<>();
    Map<String, Object> pubkeys = new HashMap<>();

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

        //API2??????????????????token2Account1???token2Account2
        SDKADD = TOKENADD2;
        token2Account1 = JSONObject.fromObject(tokenModule.tokenCreateAccount
                ("token2Account1"+utilsClass.Random(6), "token2Account1", "", "", listTag)).getString("data");
        String token2Account1Pubkey = JSONObject.fromObject(tokenModule.tokenGetPubkey(token2Account1)).getString("data");
        token2Account2 = JSONObject.fromObject(tokenModule.tokenCreateAccount
                ("token2Account2"+utilsClass.Random(6), "token2Account2", "", "", listTag)).getString("data");
        String token2Account2Pubkey = JSONObject.fromObject(tokenModule.tokenGetPubkey(token2Account2)).getString("data");

        //API1???????????????????????????tokenMultiAddr21???tokenMultiAddr12
        SDKADD = TOKENADD;
        String tokenAccount1Pubkey = JSONObject.fromObject(tokenModule.tokenGetPubkey(tokenAccount1)).getString("data");
        pubkeys.put("1",token2Account1Pubkey);
        pubkeys.put("2",tokenAccount1Pubkey);
        tokenMultiAddr12 = JSONObject.fromObject(tokenModule.tokenCreateMultiAddrByPubkeys
                (pubkeys,"tokenMultiAddr12",1,"","",listTag)).getString("data");
        pubkeys.clear();
        pubkeys.put("1",tokenAccount1Pubkey);
        pubkeys.put("2",token2Account2Pubkey);
        tokenMultiAddr21 = JSONObject.fromObject(tokenModule.tokenCreateMultiAddrByPubkeys
                (pubkeys,"tokenMultiAddr21",1,"","",listTag)).getString("data");
        Assert.assertThat(JSONObject.fromObject(tokenModule.tokenGetPubkey(token2Account1)).toString(), containsString("200"));
        Assert.assertThat(JSONObject.fromObject(tokenModule.tokenGetPubkey(token2Account2)).toString(), containsString("200"));

        //API2???????????????????????????tokenMultiAddr21???tokenMultiAddr12
        SDKADD = TOKENADD2;
        tokenMultiAddr21 = JSONObject.fromObject(tokenModule.tokenCreateMultiAddrByPubkeys
                (pubkeys,"tokenMultiAddr12",1,"","",listTag)).getString("data");
        pubkeys.clear();
        pubkeys.put("1",token2Account1Pubkey);
        pubkeys.put("2",tokenAccount1Pubkey);
        tokenMultiAddr12 = JSONObject.fromObject(tokenModule.tokenCreateMultiAddrByPubkeys
                (pubkeys,"tokenMultiAddr12",1,"","",listTag)).getString("data");
        Assert.assertThat(JSONObject.fromObject(tokenModule.tokenGetPubkey(tokenAccount1)).toString(), containsString("200"));

        //???????????????????????????tokenMultiAddr12???tokenMultiAddr21???token2Account1???token2Account2
        String response = tokenModule.tokenAddMintAddr(tokenMultiAddr12);
        Assert.assertThat(response, containsString("200"));
        response = tokenModule.tokenAddCollAddr(tokenMultiAddr12);
        Assert.assertThat(response, containsString("200"));
        response = tokenModule.tokenAddMintAddr(tokenMultiAddr21);
        Assert.assertThat(response, containsString("200"));
        response = tokenModule.tokenAddCollAddr(tokenMultiAddr21);
        Assert.assertThat(response, containsString("200"));
        response = tokenModule.tokenAddMintAddr(token2Account1);
        Assert.assertThat(response, containsString("200"));
        response = tokenModule.tokenAddCollAddr(token2Account1);
        Assert.assertThat(response, containsString("200"));
        response = tokenModule.tokenAddMintAddr(token2Account2);
        Assert.assertThat(response, containsString("200"));
        response = tokenModule.tokenAddCollAddr(token2Account2);
        Assert.assertThat(response, containsString("200"));
        sleepAndSaveInfo(SLEEPTIME, "add issue and collect addr waiting......");

        issueAmount1 = "1000.12345678912345";
        issueAmount2 = "1000.876543212345";

        if (UtilsClass.PRECISION == 10) {
            actualAmount1 = "1000.1234567891";
            actualAmount2 = "1000.8765432123";
        }else {
            actualAmount1 = "1000.123456";
            actualAmount2 = "1000.876543";
        }

        log.info("??????????????????token");
        //??????????????????????????????sleep??????
        tokenType = commonFunc.tokenModule_IssueToken(tokenMultiAddr12,tokenMultiAddr12,issueAmount1);
        issueResp = globalResponse;
        SDKADD = TOKENADD;
        tokenType2 = commonFunc.tokenModule_IssueToken(tokenMultiAddr12,tokenMultiAddr12,issueAmount2);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        log.info("???????????????????????????token??????");

        String response1 = tokenModule.tokenGetBalance(tokenMultiAddr12, tokenType);
        String response2 = tokenModule.tokenGetBalance(tokenMultiAddr12, tokenType2);
        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
        assertEquals(actualAmount1,JSONObject.fromObject(response1).getJSONObject("data").getString(tokenType));
        assertEquals("200",JSONObject.fromObject(response2).getString("state"));
        assertEquals(actualAmount2,JSONObject.fromObject(response2).getJSONObject("data").getString(tokenType2));
    }

    @Test
    public void multiToSoloIssue() throws Exception {

        issueAmount1 = "1000.12345678912345";
        issueAmount2 = "1000.876543212345";

        if (UtilsClass.PRECISION == 10) {
            actualAmount1 = "1000.1234567891";
            actualAmount2 = "1000.8765432123";
        }else {
            actualAmount1 = "1000.123456";
            actualAmount2 = "1000.876543";
        }

        log.info("??????????????????token");
        //??????????????????????????????sleep??????
        SDKADD = TOKENADD2;
        tokenType = commonFunc.tokenModule_IssueToken(tokenMultiAddr12,token2Account1,issueAmount1);
        SDKADD = TOKENADD;
        tokenType2 = commonFunc.tokenModule_IssueToken(tokenMultiAddr12,tokenAccount2,issueAmount2);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        log.info("???????????????????????????token??????");
        SDKADD = TOKENADD2;
        String response1 = tokenModule.tokenGetBalance(token2Account1, tokenType);
        SDKADD = TOKENADD;
        String response2 = tokenModule.tokenGetBalance(tokenAccount2, tokenType2);
        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
        assertEquals(actualAmount1,JSONObject.fromObject(response1).getJSONObject("data").getString(tokenType));
        assertEquals("200",JSONObject.fromObject(response2).getString("state"));
        assertEquals(actualAmount2,JSONObject.fromObject(response2).getJSONObject("data").getString(tokenType2));
    }

    /**
     * ????????????token??????????????????????????????????????????????????????
     */
     @Test
     public void multiToMultiIssue()throws Exception {

         SDKADD = TOKENADD2;
         tokenType = commonFunc.tokenModule_IssueToken(tokenMultiAddr12, tokenMultiAddr21,"1000");
         SDKADD = TOKENADD;
         tokenType2 = commonFunc.tokenModule_IssueToken(tokenMultiAddr12, tokenMultiAddr2,"1000");
         commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
         String response1 = tokenModule.tokenGetBalance(tokenMultiAddr21,tokenType);
         String response2 = tokenModule.tokenGetBalance(tokenMultiAddr2,tokenType2);
         assertEquals("200",JSONObject.fromObject(response1).getString("state"));
         assertEquals("1000",JSONObject.fromObject(response1).getJSONObject("data").getString(tokenType));
         assertEquals("200",JSONObject.fromObject(response2).getString("state"));
         assertEquals("1000",JSONObject.fromObject(response2).getJSONObject("data").getString(tokenType2));
     }

    /**
     * Tc03??????????????????????????? ??????
     *
     */
    @Test
    public void TC03_multiProgress() throws Exception {
        // tokenMultiAddr1 + "???" + tokenMultiAddr3 + "??????10???" + tokenType;
        SDKADD = TOKENADD2;
        List<Map> list = utilsClass.tokenConstructToken(token2Account1,tokenType,"10");
        List<Map> list2= utilsClass.tokenConstructToken(tokenMultiAddr21,tokenType,"10",list);
        String transferInfo = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr12,list2);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        String amount1;

        if (UtilsClass.PRECISION == 10) {
            amount1 = "980.1234567891";
        }else {
            amount1 = "980.123456";
        }

        log.info("??????"+ tokenMultiAddr12 +"???" + token2Account1 +"???" + tokenMultiAddr21 + "?????????????????????????????????");
        String queryInfo = tokenModule.tokenGetBalance(tokenMultiAddr12, tokenType);
        String queryInfo2 = tokenModule.tokenGetBalance(token2Account1, tokenType);
        String queryInfo3 = tokenModule.tokenGetBalance(tokenMultiAddr21, tokenType);
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
        assertEquals(amount1,JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType));
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("state"));
        assertEquals("10",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString(tokenType));
        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("state"));
        assertEquals("10",JSONObject.fromObject(queryInfo3).getJSONObject("data").getString(tokenType));

        //??????
        log.info(tokenMultiAddr12 +"???" + token2Account1 +"???" + tokenMultiAddr21 + "??????" + tokenType);
        String recycleInfo = commonFunc.tokenModule_DestoryToken(tokenMultiAddr12,tokenType, amount1);
        String recycleInfo2 = commonFunc.tokenModule_DestoryToken(token2Account1,tokenType, "10");
        String recycleInfo3 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr21,tokenType, "10");
        assertEquals("200",JSONObject.fromObject(recycleInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo3).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        log.info("????????????????????????????????????0");
        String queryInfo4 = tokenModule.tokenGetBalance(tokenMultiAddr12,tokenType);
        String queryInfo5 = tokenModule.tokenGetBalance(token2Account1,tokenType);
        String queryInfo6 = tokenModule.tokenGetBalance(tokenMultiAddr21,tokenType);
        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("state"));
        assertEquals(false,queryInfo4.contains(tokenType));
        assertEquals("200",JSONObject.fromObject(queryInfo5).getString("state"));
        assertEquals(false,queryInfo5.contains(tokenType));
        assertEquals("200",JSONObject.fromObject(queryInfo6).getString("state"));
        assertEquals(false,queryInfo6.contains(tokenType));
    }


    /**
     * Tc024??????????????? ?????????token????????????token
     * ???????????????
     * ???????????? ?????? ??????????????????
     * ?????????
     */
    @Test
    public void TC024_TransferAfterFrozen() throws Exception {

        //?????????????????????????????????
        SDKADD = TOKENADD2;
        log.info("???????????????Token: " + tokenType);
        String resp = tokenModule.tokenFreezeToken(tokenType);
        assertEquals("200",JSONObject.fromObject(resp).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        Thread.sleep(2000);
        log.info("???????????????????????????token??????");
        String response1 = tokenModule.tokenGetBalance(tokenMultiAddr12, tokenType);
        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
        assertEquals(actualAmount1,JSONObject.fromObject(response1).getJSONObject("data").getString(tokenType));

        log.info("?????????????????????????????????token????????????token");
        List<Map> list = utilsClass.tokenConstructToken(token2Account1,tokenType,"10");
        List<Map> list2 = utilsClass.tokenConstructToken(token2Account1,tokenType2,"10",list);
        String transferInfo= commonFunc.tokenModule_TransferTokenList(tokenMultiAddr12,list2);


        log.info("????????????????????????????????????");
        String queryInfo= tokenModule.tokenGetBalance(token2Account1,"");
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
        assertEquals(false,queryInfo.contains(tokenType));
        assertEquals(false,queryInfo.contains(tokenType2));

        queryInfo= tokenModule.tokenGetBalance(tokenMultiAddr12,"");
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
        assertEquals(actualAmount1,JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType));
        assertEquals(actualAmount2,JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType2));


        log.info("??????token???????????????token????????????token");
        String desInfo = commonFunc.tokenModule_DestoryToken(tokenMultiAddr12,tokenType,"10");
        assertEquals("200",JSONObject.fromObject(desInfo).getString("state"));
        String desInfo2 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr12,tokenType2,"10");
        assertEquals("200",JSONObject.fromObject(desInfo2).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        queryInfo= tokenModule.tokenGetBalance(tokenMultiAddr12,"");
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));

        //????????????????????????????????????token??????
        queryInfo = tokenModule.tokenGetDestroyBalance();
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
        assertEquals("10",JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType));
        assertEquals("10",JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType2));


        log.info("?????????????????????Token: " + tokenType);
        String resp1 = tokenModule.tokenRecoverToken(tokenType);
        assertEquals("200",JSONObject.fromObject(resp1).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        log.info("???????????????????????????token??????");
        //???????????? tokenType 990.123456  tokenType2 990.876543
        list = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType,"10");
        list2 = utilsClass.tokenConstructToken(tokenMultiAddr3,tokenType,"10",list);
        List<Map>list3 = utilsClass.tokenConstructToken(tokenAccount1,tokenType,"10",list2);
        List<Map>list4 = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType2,"10",list3);
        List<Map>list5 = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType2,"10",list4);//???????????????????????????
        List<Map>list6 = utilsClass.tokenConstructToken(tokenAccount1,tokenType2,"10",list5);

        Thread.sleep(2000);
        //??????tokenType * 30 tokenType2 * 30
        transferInfo= commonFunc.tokenModule_TransferTokenList(tokenMultiAddr12,list6);//????????????
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME); //UTXO???????????????????????????????????????

        //??????tokenType * 30 tokenType2 * 10
        String transferInfo2 = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr12,list4);//????????????
        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        String amount1, amount2;

        if (UtilsClass.PRECISION == 10) {
            amount1 = "930.1234567891";
            amount2 = "950.8765432123";
        }else {
            amount1 = "930.123456";
            amount2 = "950.876543";
        }

        log.info("????????????????????????????????????");
        queryInfo = tokenModule.tokenGetBalance(tokenMultiAddr12,"");
        SDKADD = TOKENADD;
        String queryInfo2 = tokenModule.tokenGetBalance(tokenMultiAddr2,"");
        String queryInfo3 = tokenModule.tokenGetBalance(tokenMultiAddr3,"");
        String queryInfo4 = tokenModule.tokenGetBalance(tokenAccount1,"");

        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("state"));

        assertEquals(amount1,JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType));//??????tokenMultiAddr1 tokenType
        assertEquals(amount2,JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType2));//??????tokenMultiAddr1 tokenType2

        assertEquals("20",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString(tokenType));//??????tokenMultiAddr2 tokenType
        assertEquals("30",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString(tokenType2));//??????tokenMultiAddr2 tokenType2

        assertEquals("20",JSONObject.fromObject(queryInfo3).getJSONObject("data").getString(tokenType));//??????tokenMultiAddr3 tokenType
        assertEquals(false,queryInfo3.contains(tokenType2));//??????tokenMultiAddr3 tokenType2

        assertEquals("20",JSONObject.fromObject(queryInfo4).getJSONObject("data").getString(tokenType));//tokenAccount1 tokenType
        assertEquals("10",JSONObject.fromObject(queryInfo4).getJSONObject("data").getString(tokenType2));//tokenAccount1 tokenType2


        log.info("??????Token");
        SDKADD = TOKENADD2;
        String recycleInfo = commonFunc.tokenModule_DestoryToken(tokenMultiAddr12, tokenType, amount1);
        String recycleInfo2 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr12, tokenType2, amount2);
        SDKADD = TOKENADD;
        String recycleInfo3 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr2, tokenType, "20");
        String recycleInfo4 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr2, tokenType2, "30");
        String recycleInfo5 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr3, tokenType, "20");
        String recycleInfo6 = commonFunc.tokenModule_DestoryToken(tokenAccount1, tokenType2, "10");
        String recycleInfo7 = commonFunc.tokenModule_DestoryToken(tokenAccount1, tokenType, "20");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(recycleInfo2,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(recycleInfo7,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        assertEquals("200",JSONObject.fromObject(recycleInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo3).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo4).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo5).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo6).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo7).getString("state"));


        log.info("????????????????????????????????????");
        queryInfo = tokenModule.tokenGetBalance(tokenMultiAddr1,"");
        queryInfo2 = tokenModule.tokenGetBalance(tokenMultiAddr2,"");
        queryInfo3 = tokenModule.tokenGetBalance(tokenMultiAddr3,"");
        queryInfo4 = tokenModule.tokenGetBalance(tokenAccount1,"");

        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("state"));

        assertEquals(false,queryInfo.contains(tokenType));
        assertEquals(false,queryInfo.contains(tokenType2));
        assertEquals(false,queryInfo2.contains(tokenType));
        assertEquals(false,queryInfo2.contains(tokenType2));
        assertEquals(false,queryInfo3.contains(tokenType));
        assertEquals(false,queryInfo3.contains(tokenType2));
        assertEquals(false,queryInfo4.contains(tokenType));
        assertEquals(false,queryInfo4.contains(tokenType2));

    }

    /**
     *TC31??????????????????????????????????????????
     * ????????????-??????????????????-????????????????????????token-??????-??????-??????
     * @throws Exception
     */
    @Test
    public void TC31_transferSoloMulti()throws  Exception{

        List<Map>list=utilsClass.tokenConstructToken(tokenMultiAddr21,tokenType,"10");
        List<Map>list2=utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType2,"10",list);
        List<Map>list3=utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType,"10",list);

        //tokenMultiAddr1 +"???" + tokenAccount1 tokenMultiAddr1 ??????tokenType tokenType2
        SDKADD = TOKENADD2;
        String transferInfo = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr12,list2);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);//UTXO???????????????????????????????????????

        SDKADD = TOKENADD;
        String transferInfo2 = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr12,list3);
        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);//UTXO???????????????????????????????????????

        log.info("????????????????????????????????????");
        String queryInfo= tokenModule.tokenGetBalance(tokenMultiAddr21,"");
        String queryInfo2= tokenModule.tokenGetBalance(tokenMultiAddr2,"");
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("state"));
        assertEquals("20",JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType));
        assertEquals("10",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString(tokenType));
        assertEquals("10",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString(tokenType2));

        String amount1, amount2;

        if (UtilsClass.PRECISION == 10) {
            amount1 = "970.1234567891";
            amount2 = "990.8765432123";
        }else {
            amount1 = "970.123456";
            amount2 = "990.876543";
        }
        queryInfo = tokenModule.tokenGetBalance(tokenMultiAddr12, "");
        assertEquals(amount1,JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType));
        assertEquals(amount2,JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType2));

        log.info("??????Token");
        SDKADD = TOKENADD2;
        String recycleInfo = commonFunc.tokenModule_DestoryToken(tokenMultiAddr12, tokenType, amount1);
        SDKADD = TOKENADD;
        String recycleInfo2 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr12, tokenType2, amount2);
        String recycleInfo3 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr21, tokenType, "20");
        String recycleInfo4 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr2, tokenType, "10");
        String recycleInfo5 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr2, tokenType2, "10");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(recycleInfo2,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(recycleInfo5,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        assertEquals("200",JSONObject.fromObject(recycleInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo3).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo4).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo5).getString("state"));

        log.info("????????????????????????????????????");
        String queryInfo3= tokenModule.tokenGetBalance(tokenMultiAddr12,"");
        String queryInfo4= tokenModule.tokenGetBalance(tokenMultiAddr21,"");
        String queryInfo5= tokenModule.tokenGetBalance(tokenMultiAddr2,"");
        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo5).getString("state"));
        assertEquals(false,queryInfo3.contains(tokenType2));
        assertEquals(false,queryInfo3.contains(tokenType));
        assertEquals(false,queryInfo4.contains(tokenType2));
        assertEquals(false,queryInfo4.contains(tokenType));
        assertEquals(false,queryInfo5.contains(tokenType2));
        assertEquals(false,queryInfo5.contains(tokenType));


        String zeroInfo = tokenModule.tokenGetDestroyBalance();
        assertEquals(actualAmount1,JSONObject.fromObject(zeroInfo).getJSONObject("data").getString(tokenType));
        assertEquals(actualAmount2,JSONObject.fromObject(zeroInfo).getJSONObject("data").getString(tokenType2));
    }

    /**
     * ??????????????????????????? ??????????????????
     * @throws Exception
     */
    @Test
    public void TransferToSelf() throws Exception {

        SDKADD = TOKENADD2;
        String transferInfo= commonFunc.tokenModule_TransferToken(tokenMultiAddr12, tokenMultiAddr12,tokenType,"10");
        assertEquals(true,transferInfo.contains("can't transfer it to yourself"));
    }


    /**
     * ?????????????????????????????????UTXO???????????????
     * @throws Exception
     */
    @Test
    public void transferByUTXO()throws Exception{

        //M12>M21??????1000.123456???token1???M12>M21??????1000.876543???token2
        tokenType = utilsClass.Random(6);
        tokenType2 = UtilsClass.Random(6);
        SDKADD = TOKENADD2;
        String response1 = tokenModule.tokenIssue(tokenMultiAddr12,tokenMultiAddr21,tokenType,actualAmount1,"??????token/1000.123456");
        SDKADD = TOKENADD;
        String response2 = tokenModule.tokenIssue(tokenMultiAddr12,tokenMultiAddr21,tokenType2,actualAmount2,"??????token2/1000.876543");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        String hash1 = JSONObject.fromObject(response1).getString("data");
        String hash2 = JSONObject.fromObject(response2).getString("data");

        //M21>>M1\M2\M3????????????200???token1
        SDKADD = TOKENADD2;
        List<Map> list = utilsClass.tokenConstructUTXO(hash1,0,"200",tokenMultiAddr1);
        List<Map> list2 = utilsClass.tokenConstructUTXO(hash1,0,"200",tokenMultiAddr2,list);
        List<Map> list3 = utilsClass.tokenConstructUTXO(hash1,0,"200",tokenMultiAddr3,list2);
        String transferInfo1 = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr21,list3);
        String transferInfoHash1 = JSONObject.fromObject(transferInfo1).getString("data");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        //M21>>T1????????????100???token1\token2???M21>>M3??????100???token2
        List<Map>list4 = utilsClass.tokenConstructUTXO(transferInfoHash1,3,"100",tokenAccount1);
        List<Map>list5 = utilsClass.tokenConstructUTXO(hash2,0,"100",tokenAccount1,list4);
        List<Map>list6 = utilsClass.tokenConstructUTXO(hash2,0,"100",tokenMultiAddr3,list5);
        List<Map>listforto = utilsClass.tokenConstructToken(tokenMultiAddr1,tokenType2,"100");
        String transferInfo2 = tokenModule.tokenTransfer(tokenMultiAddr21,"to???utxo??????????????????utxo",listforto,list6);
        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        //M1>>T1??????100???token1
        SDKADD = TOKENADD;
        List<Map>list7 = utilsClass.tokenConstructUTXO(transferInfoHash1,0,"100",tokenAccount1);
        String transferInfo3 = tokenModule.tokenTransfer(tokenMultiAddr1,"to???utxo??????????????????utxo",listforto,list7);
        assertEquals("200",JSONObject.fromObject(transferInfo3).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        //T1-token1??????200???M1-token1??????100,M2-token1??????200,M3-token1??????200,M21-token1??????300.123456
        // T1-token2??????100???,M21-token2??????800.876543,M3-token2??????100
        String queryBalance = tokenModule.tokenGetBalance(tokenAccount1,"");
        String queryBalance1= tokenModule.tokenGetBalance(tokenMultiAddr1,"");
        String queryBalance2 = tokenModule.tokenGetBalance(tokenMultiAddr2,"");
        String queryBalance3 = tokenModule.tokenGetBalance(tokenMultiAddr3,"");
        String queryBalance4 = tokenModule.tokenGetBalance(tokenMultiAddr21,"");

        assertEquals("200", JSONObject.fromObject(queryBalance).getJSONObject("data").getString(tokenType));
        assertEquals("100", JSONObject.fromObject(queryBalance).getJSONObject("data").getString(tokenType2));
        assertEquals("100", JSONObject.fromObject(queryBalance1).getJSONObject("data").getString(tokenType));
        assertEquals("200", JSONObject.fromObject(queryBalance2).getJSONObject("data").getString(tokenType));
        assertEquals("200", JSONObject.fromObject(queryBalance3).getJSONObject("data").getString(tokenType));
        assertEquals("100", JSONObject.fromObject(queryBalance3).getJSONObject("data").getString(tokenType2));
        assertEquals("300.123456", JSONObject.fromObject(queryBalance4).getJSONObject("data").getString(tokenType));
        assertEquals("800.876543", JSONObject.fromObject(queryBalance4).getJSONObject("data").getString(tokenType2));

        //????????????
        commonFunc.tokenModule_DestoryTokenByTokenType(tokenType);
        commonFunc.tokenModule_DestoryTokenByTokenType(tokenType2);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashTypeDesByType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        //????????????
        queryBalance = tokenModule.tokenGetBalance(tokenAccount1,"");
        assertEquals(false,queryBalance.contains(tokenType));
        assertEquals(false,queryBalance.contains(tokenType2));

    }

    /**
     * ?????????????????????????????????UTXO???????????????
     * @throws Exception
     */
    @Test
    public void destroyByUTXO()throws Exception{

        HashMap<String, Object> mapSendMsg = new HashMap<>();
        //M12>T21??????1000.123456???token1???M12>M21??????1000.876543???token2
        SDKADD = TOKENADD2;
        tokenType = utilsClass.Random(6);
        tokenType2 = UtilsClass.Random(6);
        String response1 = tokenModule.tokenIssue(tokenMultiAddr12,token2Account1,tokenType,actualAmount1,"??????token/1000.123456");
        String response2 = tokenModule.tokenIssue(tokenMultiAddr12,tokenMultiAddr21,tokenType2,actualAmount2,"??????token2/1000.876543");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        String hash1 = JSONObject.fromObject(response1).getString("data");
        String hash2 = JSONObject.fromObject(response2).getString("data");

        //T21>>M1\M2\M3????????????200???token1
        List<Map> list = utilsClass.tokenConstructUTXO(hash1,0,"200",tokenMultiAddr1);
        List<Map> list2 = utilsClass.tokenConstructUTXO(hash1,0,"200",tokenMultiAddr2,list);
        List<Map> list3 = utilsClass.tokenConstructUTXO(hash1,0,"200",tokenMultiAddr3,list2);
        String transferInfo1 = commonFunc.tokenModule_TransferTokenList(token2Account1,list3);
        String transferInfoHash1 = JSONObject.fromObject(transferInfo1).getString("data");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        //T21??????utxo??????100???token1???M21??????utxo??????100???token2
        List<Map>list7 = utilsClass.tokenConstrucDestroytUTXO(transferInfoHash1,3,"100");
        List<Map>list8 = utilsClass.tokenConstrucDestroytUTXO(hash2,0,"100",list7);
        List<Map>destroylist = utilsClass.tokenConstructToken(tokenMultiAddr3,tokenType,"100");
        String destroyInfo = tokenModule.tokenDestoryByList(destroylist,list8,"to???utxo??????????????????utxo",mapSendMsg);
        assertEquals("200",JSONObject.fromObject(destroyInfo).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        SDKADD = TOKENADD;
        //M1???M2???M3????????????utxo??????100???token1
        List<Map>list4 = utilsClass.tokenConstrucDestroytUTXO(transferInfoHash1,0,"100");
        List<Map>list5 = utilsClass.tokenConstrucDestroytUTXO(transferInfoHash1,1,"100",list4);
        List<Map>list6 = utilsClass.tokenConstrucDestroytUTXO(transferInfoHash1,2,"100",list5);
        destroyInfo = tokenModule.tokenDestoryByList(destroylist,list6,"to???utxo??????????????????utxo",mapSendMsg);
        assertEquals("200",JSONObject.fromObject(destroyInfo).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        //????????????
        String queryBalance2 = tokenModule.tokenGetBalance(tokenMultiAddr1,tokenType);
        String queryBalance3 = tokenModule.tokenGetBalance(tokenMultiAddr2,tokenType);
        String queryBalance4 = tokenModule.tokenGetBalance(tokenMultiAddr3,tokenType);

        SDKADD = TOKENADD2;
        String queryBalance = tokenModule.tokenGetBalance(token2Account1,tokenType);
        String queryBalance1 = tokenModule.tokenGetBalance(tokenMultiAddr21,tokenType2);

        assertEquals("300.123456",JSONObject.fromObject(queryBalance).getJSONObject("data").getString(tokenType));
        assertEquals("900.876543",JSONObject.fromObject(queryBalance1).getJSONObject("data").getString(tokenType2));
        assertEquals("100",JSONObject.fromObject(queryBalance2).getJSONObject("data").getString(tokenType));
        assertEquals("100",JSONObject.fromObject(queryBalance3).getJSONObject("data").getString(tokenType));
        assertEquals("100",JSONObject.fromObject(queryBalance4).getJSONObject("data").getString(tokenType));


    }

    @Test
    public void destoryByToken()throws Exception{
        //"???????????????" + PUBKEY3 + "??????3000???" + tokenType+",??????"+PUBKEY4+"??????";
        SDKADD = TOKENADD2;
        List<Map> list = utilsClass.tokenConstructToken(tokenAccount3,tokenType2,"300");
        List<Map> list2= utilsClass.tokenConstructToken(token2Account1,tokenType,"400",list);
        List<Map> list3 = utilsClass.tokenConstructToken(tokenMultiAddr21,tokenType,"300",list2);
        List<Map> list4 = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType2,"400",list3);
        String transferInfo = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr12, list4);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        String amount1,amount2;
        if (UtilsClass.PRECISION == 10) {
            amount1 = "300.1234567891";
            amount2 = "300.8765432123";
        }else {
            amount1 = "300.123456";
            amount2 = "300.876543";
        }
        //????????????list-list4????????????
        List<Map> listR = commonFunc.ConstructDesByTokenRespList(token2Account1,"400");
        List<Map> listR2= commonFunc.ConstructDesByTokenRespList(tokenMultiAddr21,"300",listR);
        List<Map> listR3= commonFunc.ConstructDesByTokenRespList(tokenMultiAddr12,amount1,listR2);

        List<Map> list1R = commonFunc.ConstructDesByTokenRespList(tokenAccount3,"300");
        List<Map> list1R2= commonFunc.ConstructDesByTokenRespList(tokenMultiAddr2,"400",list1R);
        List<Map> list1R3= commonFunc.ConstructDesByTokenRespList(tokenMultiAddr12,amount2,list1R2);

        String desInfo = commonFunc.tokenModule_DestoryTokenByTokenType(tokenType);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
        assertEquals(actualAmount1,JSONObject.fromObject(desInfo).getJSONObject("data").getString("total"));
        JSONArray jsonArray = JSONObject.fromObject(desInfo).getJSONObject("data").getJSONArray("detail");

        assertEquals(3,jsonArray.size());//????????????????????????
        assertEquals(true, commonFunc.checkListArray(listR3,jsonArray));//??????detail??????????????????

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashTypeDesByType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        String getZeroAc = tokenModule.tokenGetDestroyBalance();
        assertEquals(actualAmount1,JSONObject.fromObject(getZeroAc).getJSONObject("data").getString(tokenType));

        SDKADD = TOKENADD;
        desInfo = commonFunc.tokenModule_DestoryTokenByTokenType(tokenType2);
        assertEquals("200",JSONObject.fromObject(desInfo).getString("state"));
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
        String queryInfo2 = tokenModule.tokenGetBalance(tokenMultiAddr12,"");
        String queryInfo3 = tokenModule.tokenGetBalance(tokenMultiAddr2,"");
        String queryInfo4 = tokenModule.tokenGetBalance(token2Account1,"");
        String queryInfo5 = tokenModule.tokenGetBalance(tokenMultiAddr21,"");

        assertEquals(false,queryInfo1.contains(tokenType2));
        assertEquals(false,queryInfo1.contains(tokenType));
        assertEquals(false,queryInfo2.contains(tokenType2));
        assertEquals(false,queryInfo2.contains(tokenType));
        assertEquals(false,queryInfo3.contains(tokenType2));
        assertEquals(false,queryInfo3.contains(tokenType));
        assertEquals(false,queryInfo4.contains(tokenType2));
        assertEquals(false,queryInfo4.contains(tokenType));
        assertEquals(false,queryInfo5.contains(tokenType2));
        assertEquals(false,queryInfo5.contains(tokenType));


    }

    @Test
    public void destoryByList()throws Exception{
        //"???????????????" + PUBKEY3 + "??????3000???" + tokenType+",??????"+PUBKEY4+"??????";
        SDKADD = TOKENADD2;
        List<Map> list = utilsClass.tokenConstructToken(tokenAccount3,tokenType2,"300");
        List<Map> list2= utilsClass.tokenConstructToken(tokenAccount3,tokenType,"400",list);
        List<Map> list3 = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType,"300",list2);
        List<Map> list4 = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType2,"400",list3);
        List<Map> list5 = utilsClass.tokenConstructToken(tokenMultiAddr12,tokenType,"300",list4);
        String transferInfo = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr12, list4);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        SDKADD = TOKENADD;
        String desInfo = commonFunc.tokenModule_DestoryTokenByList2(list5);
        assertEquals("200",JSONObject.fromObject(desInfo).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        SDKADD = TOKENADD2;
        List<Map> list6 = utilsClass.tokenConstructToken(tokenMultiAddr12,tokenType2,"300");
        desInfo = commonFunc.tokenModule_DestoryTokenByList2(list6);
        assertEquals("200",JSONObject.fromObject(desInfo).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        String getZeroAc = tokenModule.tokenGetDestroyBalance();
        assertEquals("1000",JSONObject.fromObject(getZeroAc).getJSONObject("data").getString(tokenType));
        assertEquals("1000",JSONObject.fromObject(getZeroAc).getJSONObject("data").getString(tokenType2));
    }


    //???????????????????????????token??????????????????
    @Test
    public void transferToZeroAccountByAddrList()throws Exception{

        SDKADD = TOKENADD2;
        String DBZeroAccout = "osEoy933LkHyyBcgjE7vCivfsX";
        log.info("?????????????????? ???????????????????????? ??????????????????token ????????????????????????");
        tokenModule.tokenFreezeToken(tokenType);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        List<Map> list = utilsClass.tokenConstructToken(DBZeroAccout,tokenType2,actualAmount2);
        String transferInfo = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr12, list);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
        SDKADD = TOKENADD;
        List<Map> list2= utilsClass.tokenConstructToken(DBZeroAccout,tokenType,actualAmount1);
        transferInfo = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr12, list2);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        log.info("?????????????????????????????????????????????token");
        String query = tokenModule.tokenGetBalance(tokenMultiAddr12,"");
        assertEquals(false,query.contains(tokenType));
        assertEquals(false,query.contains(tokenType2));

        log.info("??????????????????????????????????????????????????????????????????");
        String queryDBZeroAcc = tokenModule.tokenGetBalance(DBZeroAccout,"");
        String queryZeroBalance = tokenModule.tokenGetDestroyBalance();

        assertEquals(actualAmount1,JSONObject.fromObject(queryDBZeroAcc).getJSONObject("data").getString(tokenType));
        assertEquals(actualAmount2,JSONObject.fromObject(queryDBZeroAcc).getJSONObject("data").getString(tokenType2));

        assertEquals(actualAmount1,JSONObject.fromObject(queryZeroBalance).getJSONObject("data").getString(tokenType));
        assertEquals(actualAmount2,JSONObject.fromObject(queryZeroBalance).getJSONObject("data").getString(tokenType2));

    }

    //???????????????????????????token??????????????????
    @Test
    public void transferToZeroAccountByUTXO()throws Exception{

        SDKADD = TOKENADD2;
        log.info("??????????????????UTXO????????????");
        assertEquals(tokenMultiAddr12,JSONObject.fromObject(issueResp).getJSONArray("utxo").getJSONObject(0).getString("address"));
        assertEquals(actualAmount1,JSONObject.fromObject(issueResp).getJSONArray("utxo").getJSONObject(0).getString("amount"));
        assertEquals(tokenType,JSONObject.fromObject(issueResp).getJSONArray("utxo").getJSONObject(0).getString("tokentype"));
        assertEquals("0",JSONObject.fromObject(issueResp).getJSONArray("utxo").getJSONObject(0).getString("index"));

        assertEquals(tokenMultiAddr12,JSONObject.fromObject(globalResponse).getJSONArray("utxo").getJSONObject(0).getString("address"));
        assertEquals(actualAmount2,JSONObject.fromObject(globalResponse).getJSONArray("utxo").getJSONObject(0).getString("amount"));
        assertEquals(tokenType2,JSONObject.fromObject(globalResponse).getJSONArray("utxo").getJSONObject(0).getString("tokentype"));
        assertEquals("0",JSONObject.fromObject(globalResponse).getJSONArray("utxo").getJSONObject(0).getString("index"));

        String utxoHash1 = JSONObject.fromObject(issueResp).getString("data");
        String utxoHash2 = JSONObject.fromObject(globalResponse).getString("data");


        String DBZeroAccout = "osEoy933LkHyyBcgjE7vCivfsX";
        log.info("?????????????????? ??????????????????token ??????UTXO+???????????? ??????token??????????????????");
        tokenModule.tokenFreezeToken(tokenType);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        List<Map>listutxo = utilsClass.tokenConstructUTXO(utxoHash1,0,actualAmount1,DBZeroAccout);
        String transferResp = tokenModule.tokenTransfer(tokenMultiAddr12,"utxo list transfer",null,listutxo);
        assertEquals("200",JSONObject.fromObject(transferResp).getString("state"));

        SDKADD = TOKENADD;
        List<Map>listutxo2 = utilsClass.tokenConstructUTXO(utxoHash2,0,actualAmount2,DBZeroAccout);
        transferResp = tokenModule.tokenTransfer(tokenMultiAddr12,"utxo list transfer",null,listutxo2);
        assertEquals("200",JSONObject.fromObject(transferResp).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        log.info("?????????????????????????????????????????????token");
        String query = tokenModule.tokenGetBalance(tokenMultiAddr12,"");
        assertEquals(false,query.contains(tokenType));
        assertEquals(false,query.contains(tokenType2));

        SDKADD = TOKENADD2;
        log.info("??????????????????????????????????????????????????????????????????");
        String queryDBZeroAcc = tokenModule.tokenGetBalance(DBZeroAccout,"");
        String queryZeroBalance = tokenModule.tokenGetDestroyBalance();

        assertEquals(actualAmount1,JSONObject.fromObject(queryDBZeroAcc).getJSONObject("data").getString(tokenType));
        assertEquals(actualAmount2,JSONObject.fromObject(queryDBZeroAcc).getJSONObject("data").getString(tokenType2));

        assertEquals(actualAmount1,JSONObject.fromObject(queryZeroBalance).getJSONObject("data").getString(tokenType));
        assertEquals(actualAmount2,JSONObject.fromObject(queryZeroBalance).getJSONObject("data").getString(tokenType2));

        log.info("???????????????????????? ???????????????");
        List<Map> list = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType2,actualAmount2);
        String testZeroTransfer =  tokenModule.tokenTransfer(DBZeroAccout,"????????????????????????token",list);
        assertEquals("400",JSONObject.fromObject(testZeroTransfer).getString("state"));


        log.info("????????????????????????");
        String desBDZeroAccout = tokenModule.tokenDestoryByList(DBZeroAccout,tokenType,actualAmount1,"????????????????????????");
        assertEquals("400",JSONObject.fromObject(desBDZeroAccout).getString("state"));
    }


//    @AfterClass
    public static void resetAddr()throws Exception{
        SDKADD = TOKENADD;
        BeforeCondition beforeCondition = new BeforeCondition();
        beforeCondition.tokenAddIssueCollAddr();
    }
}
