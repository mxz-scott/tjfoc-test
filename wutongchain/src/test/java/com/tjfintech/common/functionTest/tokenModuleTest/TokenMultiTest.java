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
import sun.rmi.runtime.Log;

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
public class TokenMultiTest {
    TestBuilder testBuilder= TestBuilder.getInstance();
    MultiSign multiSign =testBuilder.getMultiSign();
    SoloSign soloSign= testBuilder.getSoloSign();
    UtilsClass utilsClass=new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    private static String tokenType;
    private static String tokenType2;

    private static String issueAmount1;
    private static String issueAmount2;

    private static String actualAmount1;
    private static String actualAmount2;

    Token tokenModule = testBuilder.getToken();
    String issueResp = "";

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

        SDKADD = TOKENADD;
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
        tokenType = commonFunc.tokenModule_IssueToken(tokenMultiAddr1,tokenMultiAddr1,issueAmount1);
        issueResp = globalResponse;
        tokenType2 = commonFunc.tokenModule_IssueToken(tokenMultiAddr1,tokenMultiAddr1,issueAmount2);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        log.info("???????????????????????????token??????");

        String response1 = tokenModule.tokenGetBalance(tokenMultiAddr1, tokenType);
        String response2 = tokenModule.tokenGetBalance(tokenMultiAddr1, tokenType2);
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
        tokenType = commonFunc.tokenModule_IssueToken(tokenMultiAddr1,tokenAccount1,issueAmount1);
        tokenType2 = commonFunc.tokenModule_IssueToken(tokenMultiAddr1,tokenAccount2,issueAmount2);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        log.info("???????????????????????????token??????");

        String response1 = tokenModule.tokenGetBalance(tokenAccount1, tokenType);
        String response2 = tokenModule.tokenGetBalance(tokenAccount2, tokenType2);
        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
        assertEquals(actualAmount1,JSONObject.fromObject(response1).getJSONObject("data").getString(tokenType));
        assertEquals("200",JSONObject.fromObject(response2).getString("state"));
        assertEquals(actualAmount2,JSONObject.fromObject(response2).getJSONObject("data").getString(tokenType2));
    }

    /**
     *  ?????????????????????
     *
     */
    @Test
    public void TC001_TestMaxValue()throws Exception {

        if (UtilsClass.PRECISION == 10) {
            actualAmount1 = "1844674407";
        }else {
            actualAmount1 = "18446744073709";
        }
        tokenType = commonFunc.tokenModule_IssueToken(tokenMultiAddr1,tokenMultiAddr1,actualAmount1);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        log.info("?????????????????????token??????");
        String response1 = tokenModule.tokenGetBalance(tokenMultiAddr1,tokenType);
        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
        assertEquals(actualAmount1,JSONObject.fromObject(response1).getJSONObject("data").getString(tokenType));

    }

    //??????????????????????????????????????????????????????
    @Test
    public void issueTwo()throws Exception{
        String issueAddr = "";
        String collAddr = "";
        String issueToken = "";
        String issAmount ="";

        //??????????????????token 5000.999999
        String stokenType = "tokenSo_"+ UtilsClass.Random(8);
        issueAddr = tokenAccount2;
        collAddr = tokenMultiAddr1;
        issueToken = stokenType;
        issAmount = "1844674";
        String issueToken2 = "tokenS2o_"+ UtilsClass.Random(8);
        String issAmount2 = "18022.1";

        //?????????????????????????????????
        tokenModule.tokenAddMintAddr(issueAddr);
        tokenModule.tokenAddCollAddr(collAddr);
        tokenModule.tokenAddMintAddr(collAddr);
        tokenModule.tokenAddCollAddr(issueAddr);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        issueToken = commonFunc.tokenModule_IssueToken(issueAddr,collAddr,issAmount);
        issueToken2 = commonFunc.tokenModule_IssueToken(collAddr,issueAddr,issAmount2);


        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        String queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken2);
        assertEquals(false,queryBalance.contains(issueToken2));


        queryBalance = tokenModule.tokenGetBalance(issueAddr,"");
        assertEquals(issAmount2, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken2));
        queryBalance = tokenModule.tokenGetBalance(issueAddr,issueToken);
        assertEquals(false,queryBalance.contains(issueToken));

    }



    /**
     * ???????????????????????????????????????????????????,????????????????????????????????????????????????
     * ?????????????????????
     * @throws Exception
     */
    @Test
    public void TC1280_checkMultiIssueAddr()throws Exception {
        if(syncFlag) return; //????????????sync???????????? ????????????????????? ??????sync????????????????????????
        //???????????????????????????????????????tokenMultiAddr1???tokenMultiAddr2,?????????????????????
        tokenType = commonFunc.tokenModule_IssueToken(tokenMultiAddr3, tokenMultiAddr3,"1000");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        String response1 = tokenModule.tokenGetBalance(tokenMultiAddr3,tokenType);
        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
        assertEquals("1000",JSONObject.fromObject(response1).getJSONObject("data").getString(tokenType));

        //???????????????????????????????????????
        String response2 = tokenModule.tokenDelMintAddr(tokenMultiAddr3);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        tokenType = commonFunc.tokenModule_IssueToken(tokenMultiAddr3, tokenMultiAddr3,"1000");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        String response3 = tokenModule.tokenGetBalance(tokenMultiAddr3,tokenType);
        assertEquals("200",JSONObject.fromObject(response3).getString("state"));
        assertEquals(false,response3.contains(tokenType));

        //?????????????????????????????????
        String response41=tokenModule.tokenDelCollAddr(tokenMultiAddr3);
        assertEquals("200",JSONObject.fromObject(response41).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        tokenType = commonFunc.tokenModule_IssueToken(tokenMultiAddr3, tokenMultiAddr3,"1000");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        String response42 = tokenModule.tokenGetBalance(tokenMultiAddr3,tokenType);
        assertEquals("200",JSONObject.fromObject(response42).getString("state"));
        assertEquals(false,response42.contains(tokenType));

        //????????????????????????????????????????????????
        String response5=tokenModule.tokenAddMintAddr(tokenMultiAddr3);
        assertEquals("200",JSONObject.fromObject(response5).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        tokenType = commonFunc.tokenModule_IssueToken(tokenMultiAddr3, tokenMultiAddr3,"1000");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        String response53 = tokenModule.tokenGetBalance(tokenMultiAddr3,tokenType);
        assertEquals("200",JSONObject.fromObject(response53).getString("state"));
        assertEquals(false,response53.contains(tokenType));

        //???????????????????????????????????????
        String response6=tokenModule.tokenAddCollAddr(tokenMultiAddr3);
        assertEquals("200",JSONObject.fromObject(response6).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        tokenType = commonFunc.tokenModule_IssueToken(tokenMultiAddr3, tokenMultiAddr3,"1000");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        String response62 = tokenModule.tokenGetBalance(tokenMultiAddr3,tokenType);
        assertEquals("200",JSONObject.fromObject(response62).getString("state"));
        assertEquals("1000",JSONObject.fromObject(response62).getJSONObject("data").getString(tokenType));
    }

    @Test
    public void TC1280_checkMultiIssueAddrSync()throws Exception {
        if(!syncFlag)  return;  //??????????????????sync?????????????????? ????????????????????????
        //???????????????????????????????????????tokenMultiAddr1???tokenMultiAddr2,?????????????????????
        tokenType = commonFunc.tokenModule_IssueToken(tokenMultiAddr3, tokenMultiAddr3,"1000");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        String response1 = tokenModule.tokenGetBalance(tokenMultiAddr3,tokenType);
        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
        assertEquals("1000",JSONObject.fromObject(response1).getJSONObject("data").getString(tokenType));

        //???????????????????????????????????????
        String response2 = tokenModule.tokenDelMintAddr(tokenMultiAddr3);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        String testToken = "spec" + Random(6);
        tokenModule.tokenIssue(tokenMultiAddr3,tokenMultiAddr3,testToken,"500","issue without issue addr");
        assertEquals("400",JSONObject.fromObject(globalResponse).getString("state"));
        assertEquals(true,globalResponse.contains("Err:unauthorized issue addr " + tokenMultiAddr3));

        String response3 = tokenModule.tokenGetBalance(tokenMultiAddr3,testToken);
        assertEquals("200",JSONObject.fromObject(response3).getString("state"));
        assertEquals(false,response3.contains(testToken));


        //?????????????????????????????????
        String response41=tokenModule.tokenDelCollAddr(tokenMultiAddr3);
        assertEquals("200",JSONObject.fromObject(response41).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        testToken = "spec" + Random(6);
        tokenModule.tokenIssue(tokenMultiAddr3,tokenMultiAddr3,testToken,"500","issue without issue & coll addr");
        assertEquals("400",JSONObject.fromObject(globalResponse).getString("state"));
        assertEquals(true,globalResponse.contains("Err:unauthorized issue addr " + tokenMultiAddr3));

        String response42 = tokenModule.tokenGetBalance(tokenMultiAddr3,testToken);
        assertEquals("200",JSONObject.fromObject(response42).getString("state"));
        assertEquals(false,response42.contains(testToken));


        //????????????????????????????????????????????????
        String response5=tokenModule.tokenAddMintAddr(tokenMultiAddr3);
        assertEquals("200",JSONObject.fromObject(response5).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        testToken = "spec" + Random(6);
        tokenModule.tokenIssue(tokenMultiAddr3,tokenMultiAddr3,testToken,"500","issue without coll addr");
        assertEquals("400",JSONObject.fromObject(globalResponse).getString("state"));
        assertEquals(true,globalResponse.contains("Err:unauthorized collect addr " +  tokenMultiAddr3));

        String response53 = tokenModule.tokenGetBalance(tokenMultiAddr3,testToken);
        assertEquals("200",JSONObject.fromObject(response53).getString("state"));
        assertEquals(false,response53.contains(testToken));

        //???????????????????????????????????????
        String response6=tokenModule.tokenAddCollAddr(tokenMultiAddr3);
        assertEquals("200",JSONObject.fromObject(response6).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        tokenType = commonFunc.tokenModule_IssueToken(tokenMultiAddr3, tokenMultiAddr3,"1000");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        String response62 = tokenModule.tokenGetBalance(tokenMultiAddr3,tokenType);
        assertEquals("200",JSONObject.fromObject(response62).getString("state"));
        assertEquals("1000",JSONObject.fromObject(response62).getJSONObject("data").getString(tokenType));
    }


    /**
     * ????????????token??????????????????????????????????????????????????????
     * @throws Exception
     */
     @Test
     public void TC994_issueToOther()throws Exception {
         tokenType = commonFunc.tokenModule_IssueToken(tokenMultiAddr1, tokenMultiAddr2,"1000");
         tokenType2 = commonFunc.tokenModule_IssueToken(tokenMultiAddr1, tokenMultiAddr2,"1000");
         commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
         String response1 = tokenModule.tokenGetBalance(tokenMultiAddr2,tokenType);
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
        List<Map> list = utilsClass.tokenConstructToken(tokenMultiAddr3,tokenType,"10");
        String transferInfo = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1,list);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        String amount1;

        if (UtilsClass.PRECISION == 10) {
            amount1 = "990.1234567891";
        }else {
            amount1 = "990.123456";
        }

        log.info("??????"+ tokenMultiAddr1 +"???" + tokenMultiAddr3 + "?????????????????????????????????");
        String queryInfo = tokenModule.tokenGetBalance(tokenMultiAddr1, tokenType);
        String queryInfo2 = tokenModule.tokenGetBalance(tokenMultiAddr3, tokenType);
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
        assertEquals(amount1,JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType));
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("state"));
        assertEquals("10",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString(tokenType));

        //??????
        log.info(tokenMultiAddr1 +"???" + tokenMultiAddr3 + "??????" + tokenType);
        String recycleInfo = commonFunc.tokenModule_DestoryToken(tokenMultiAddr1,tokenType, amount1);
        String recycleInfo2 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr3,tokenType, "10");
        assertEquals("200",JSONObject.fromObject(recycleInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        log.info("????????????????????????????????????0");
        String queryInfo3 = tokenModule.tokenGetBalance(tokenMultiAddr1,tokenType);
        String queryInfo4 = tokenModule.tokenGetBalance(tokenMultiAddr3,tokenType);
        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("state"));
        assertEquals(false,queryInfo3.contains(tokenType));
        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("state"));
        assertEquals(false,queryInfo4.contains(tokenType));
    }


    /**
     * ????????????
     *
     */
    @Test
    public void TC03_PrecisionTest() throws Exception {
        List<Map> list = utilsClass.tokenConstructToken(tokenMultiAddr3,tokenType,issueAmount1);
        //tokenMultiAddr1 + "???" + tokenMultiAddr3 + "??????"+ issueAmount1 + " * " + tokenType;
        String transferInfo = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1,list);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        String amount1;

        if (UtilsClass.PRECISION == 10) {
            amount1 = "1000.1234567891";
        }else {
            amount1 = "1000.123456";
        }

        log.info("?????????????????????MULITADD4?????????????????????????????????");
        String queryInfo = tokenModule.tokenGetBalance(tokenMultiAddr1, tokenType);
        String queryInfo2 = tokenModule.tokenGetBalance(tokenMultiAddr3, tokenType);
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
        assertEquals(false,queryInfo.contains(tokenType));
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("state"));
        assertEquals(amount1,JSONObject.fromObject(queryInfo2).getJSONObject("data").getString(tokenType));

        //tokenMultiAddr1 +"???" + tokenMultiAddr3 + "??????" + tokenType);
        String recycleInfo2 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr3, tokenType, issueAmount1);
        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        log.info("????????????????????????????????????0");
        String queryInfo3 = tokenModule.tokenGetBalance(tokenMultiAddr1, tokenType);
        String queryInfo4 = tokenModule.tokenGetBalance(tokenMultiAddr3, tokenType);
        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("state"));
        assertEquals(false,queryInfo3.contains(tokenType));
        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("state"));
        assertEquals(false,queryInfo4.contains(tokenType));

        String queryInfo5 = tokenModule.tokenGetDestroyBalance();
        assertEquals(amount1,JSONObject.fromObject(queryInfo5).getJSONObject("data").getString(tokenType));

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
        log.info("???????????????Token: " + tokenType);
        String resp = tokenModule.tokenFreezeToken(tokenType);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        Thread.sleep(2000);
        log.info("???????????????????????????token??????");
        String response1 = tokenModule.tokenGetBalance(tokenMultiAddr1, tokenType);
        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
        assertEquals(actualAmount1,JSONObject.fromObject(response1).getJSONObject("data").getString(tokenType));

        log.info("?????????????????????????????????token????????????token");
        List<Map> list = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType,"10");
        List<Map> list2 = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType2,"10",list);
        String transferInfo= commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1,list2); //???????????????????????????????????????token
//        assertEquals("400",JSONObject.fromObject(transferInfo).getString("state"));
//        assertEquals(true,transferInfo.contains("has been frozen"));
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
//                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        log.info("????????????????????????????????????");
        String queryInfo= tokenModule.tokenGetBalance(tokenMultiAddr2,"");
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
        assertEquals(false,queryInfo.contains(tokenType));
        assertEquals(false,queryInfo.contains(tokenType2));

        queryInfo= tokenModule.tokenGetBalance(tokenMultiAddr1,"");
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
        assertEquals(actualAmount1,JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType));
        assertEquals(actualAmount2,JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType2));


        log.info("??????token???????????????token????????????token");
        String desInfo = commonFunc.tokenModule_DestoryToken(tokenMultiAddr1,tokenType,"10");
        assertEquals("200",JSONObject.fromObject(desInfo).getString("state"));
        String desInfo2 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr1,tokenType2,"10");
        assertEquals("200",JSONObject.fromObject(desInfo2).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        queryInfo= tokenModule.tokenGetBalance(tokenMultiAddr1,"");
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
        //???????????? tokenType 990.123456  tokenType2 980.876543
        list = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType,"10");
        list2 = utilsClass.tokenConstructToken(tokenMultiAddr3,tokenType,"10",list);
        List<Map>list3 = utilsClass.tokenConstructToken(tokenAccount1,tokenType,"10",list2);
        List<Map>list4 = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType2,"10",list3);
        List<Map>list5 = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType2,"10",list4);//???????????????????????????
        List<Map>list6 = utilsClass.tokenConstructToken(tokenAccount1,tokenType2,"10",list5);

        Thread.sleep(2000);
        //??????tokenType * 30 tokenType2 * 30
        transferInfo= commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1,list6);//????????????
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME); //UTXO???????????????????????????????????????

        //??????tokenType * 30 tokenType2 * 10
        String transferInfo2 = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1,list4);//????????????
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
        queryInfo = tokenModule.tokenGetBalance(tokenMultiAddr1,"");
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
        String recycleInfo = commonFunc.tokenModule_DestoryToken(tokenMultiAddr1, tokenType, amount1);
        String recycleInfo2 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr1, tokenType2, amount2);
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
     *TC19?????????????????????????????????????????????
     * ????????????-??????????????????-????????????????????????token-??????-??????-??????
     * @throws Exception
     */
     @Test
     public void TC19_transferMulti()throws  Exception{

         log.info("???????????????????????????token??????");
         String response1 = tokenModule.tokenGetBalance(tokenMultiAddr1, tokenType);
         String response2 = tokenModule.tokenGetBalance(tokenMultiAddr1, tokenType2);
         assertEquals("200",JSONObject.fromObject(response1).getString("state"));
         assertEquals("200",JSONObject.fromObject(response2).getString("state"));
         assertEquals(actualAmount1,JSONObject.fromObject(response1).getJSONObject("data").getString(tokenType));
         assertEquals(actualAmount2,JSONObject.fromObject(response2).getJSONObject("data").getString(tokenType2));

         List<Map> list = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType,"10");
         List<Map> list2 = utilsClass.tokenConstructToken(tokenMultiAddr3,tokenType2,"10",list);
         List<Map> list3 = utilsClass.tokenConstructToken(tokenMultiAddr3,tokenType,"10",list);

         String transferInfo= commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1, list2);
         assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));

         commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME); //UTXO???????????????????????????????????????

         String transferInfo2= commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1,list3);
         assertEquals("200",JSONObject.fromObject(transferInfo2).getString("state"));

         commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

         log.info("????????????????????????????????????");
         String queryInfo= tokenModule.tokenGetBalance(tokenMultiAddr2,"");
         String queryInfo2= tokenModule.tokenGetBalance(tokenMultiAddr3,"");
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
         queryInfo = tokenModule.tokenGetBalance(tokenMultiAddr1, "");
         assertEquals(amount1,JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType));
         assertEquals(amount2,JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType2));

         log.info("??????Token");
         String recycleInfo = commonFunc.tokenModule_DestoryToken(tokenMultiAddr1, tokenType, amount1);
         String recycleInfo2 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr1, tokenType2, amount2);
         String recycleInfo3 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr2, tokenType, "20");
         String recycleInfo4 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr3, tokenType, "10");
         String recycleInfo5 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr3, tokenType2, "10");

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
         String queryInfo3= tokenModule.tokenGetBalance(tokenMultiAddr1,"");
         String queryInfo4= tokenModule.tokenGetBalance(tokenMultiAddr2,"");
         String queryInfo5= tokenModule.tokenGetBalance(tokenMultiAddr3,"");
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
     *TC31??????????????????????????????????????????
     * ????????????-??????????????????-????????????????????????token-??????-??????-??????
     * @throws Exception
     */
    @Test
    public void TC31_transferSoloMulti()throws  Exception{

        List<Map>list=utilsClass.tokenConstructToken(tokenAccount1,tokenType,"10");
        List<Map>list2=utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType2,"10",list);
        List<Map>list3=utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType,"10",list);

        //tokenMultiAddr1 +"???" + tokenAccount1 tokenMultiAddr1 ??????tokenType tokenType2
        String transferInfo = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1,list2);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);//UTXO???????????????????????????????????????

        String transferInfo2 = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1,list3);
        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);//UTXO???????????????????????????????????????


        log.info("????????????????????????????????????");
        String queryInfo= tokenModule.tokenGetBalance(tokenAccount1,"");
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
        queryInfo = tokenModule.tokenGetBalance(tokenMultiAddr1, "");
        assertEquals(amount1,JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType));
        assertEquals(amount2,JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType2));

        log.info("??????Token");
        String recycleInfo = commonFunc.tokenModule_DestoryToken(tokenMultiAddr1, tokenType, amount1);
        String recycleInfo2 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr1, tokenType2, amount2);
        String recycleInfo3 = commonFunc.tokenModule_DestoryToken(tokenAccount1, tokenType, "20");
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
        String queryInfo3= tokenModule.tokenGetBalance(tokenMultiAddr1,"");
        String queryInfo4= tokenModule.tokenGetBalance(tokenAccount1,"");
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
     *TC32???????????????????????????????????????
     * ????????????-??????????????????-????????????????????????token-??????-??????-??????
     * @throws Exception
     */
    @Test
    public void TC32_transferSolo()throws  Exception{
        List<Map>list=utilsClass.tokenConstructToken(tokenAccount1,tokenType,"10");
        List<Map>list2=utilsClass.tokenConstructToken(tokenAccount2,tokenType2,"10",list);
        List<Map>list3=utilsClass.tokenConstructToken(tokenAccount2,tokenType,"10",list);

        //tokenMultiAddr1 +"???" + tokenAccount1 tokenMultiAddr1 ??????tokenType tokenType2
        String transferInfo = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1,list2);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);//UTXO???????????????????????????????????????

        String transferInfo2 = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1,list3);
        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);//UTXO???????????????????????????????????????


        log.info("????????????????????????????????????");
        String queryInfo= tokenModule.tokenGetBalance(tokenAccount1,"");
        String queryInfo2= tokenModule.tokenGetBalance(tokenAccount2,"");
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
        queryInfo = tokenModule.tokenGetBalance(tokenMultiAddr1, "");
        assertEquals(amount1,JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType));
        assertEquals(amount2,JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType2));

        log.info("??????Token");
        String recycleInfo = commonFunc.tokenModule_DestoryToken(tokenMultiAddr1, tokenType, amount1);
        String recycleInfo2 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr1, tokenType2, amount2);
        String recycleInfo3 = commonFunc.tokenModule_DestoryToken(tokenAccount1, tokenType, "20");
        String recycleInfo4 = commonFunc.tokenModule_DestoryToken(tokenAccount2, tokenType, "10");
        String recycleInfo5 = commonFunc.tokenModule_DestoryToken(tokenAccount2, tokenType2, "10");

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
        String queryInfo3= tokenModule.tokenGetBalance(tokenMultiAddr1,"");
        String queryInfo4= tokenModule.tokenGetBalance(tokenAccount1,"");
        String queryInfo5= tokenModule.tokenGetBalance(tokenAccount2,"");
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
     *TC35???????????????????????????????????????????????????
     * ????????????-??????????????????-????????????????????????token-??????-??????-??????
     * @throws Exception
     */
    @Test
    public void TC35_MultiToSoloMulti() throws Exception {

        List<Map>listInit = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType,"1000");
        List<Map>list0 = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType2,"1000",listInit);

        //??????????????????????????????????????????token
        String transferInfoInit = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1,list0);
        assertEquals("200",JSONObject.fromObject(transferInfoInit).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);//UTXO???????????????????????????????????????

        List<Map> list = utilsClass.tokenConstructToken(tokenAccount1, tokenType, "10");
        List<Map> list2 = utilsClass.tokenConstructToken(tokenMultiAddr3, tokenType2, "10", list);
        List<Map> list3 = utilsClass.tokenConstructToken(tokenMultiAddr3, tokenType, "10", list);

        String transferInfo = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr2,list2);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);//UTXO???????????????????????????????????????

        String transferInfo2 = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr2,list3);
        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        log.info("????????????????????????????????????");
        String queryInfo= tokenModule.tokenGetBalance(tokenAccount1,"");
        String queryInfo2= tokenModule.tokenGetBalance(tokenMultiAddr2,"");
        String queryInfo3= tokenModule.tokenGetBalance(tokenMultiAddr3,"");
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("state"));
        assertEquals("20",JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType));
        assertEquals("10",JSONObject.fromObject(queryInfo3).getJSONObject("data").getString(tokenType));
        assertEquals("10",JSONObject.fromObject(queryInfo3).getJSONObject("data").getString(tokenType2));

        assertEquals("970",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString(tokenType));
        assertEquals("990",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString(tokenType2));

        log.info("???????????????Token: "+tokenType);
        String freezeInfo = tokenModule.tokenFreezeToken(tokenType);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        log.info("??????Token");
        String recycleInfo = commonFunc.tokenModule_DestoryToken(tokenMultiAddr2, tokenType, "970");
        String recycleInfo2 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr2, tokenType2, "990");
        String recycleInfo3 = commonFunc.tokenModule_DestoryToken(tokenAccount1, tokenType, "20");
        String recycleInfo4 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr3, tokenType, "10");
        String recycleInfo5 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr3, tokenType2, "10");

        assertEquals("200",JSONObject.fromObject(recycleInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo3).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo4).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo5).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(recycleInfo2,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(recycleInfo5,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        log.info("????????????????????????????????????");
        String queryInfo6= tokenModule.tokenGetBalance(tokenMultiAddr3,"");
        String queryInfo4= tokenModule.tokenGetBalance(tokenAccount1,"");
        String queryInfo5= tokenModule.tokenGetBalance(tokenMultiAddr2,"");
        assertEquals("200",JSONObject.fromObject(queryInfo6).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo5).getString("state"));
        assertEquals(false,queryInfo6.contains(tokenType2));
        assertEquals(false,queryInfo6.contains(tokenType));
        assertEquals(false,queryInfo4.contains(tokenType2));
        assertEquals(false,queryInfo4.contains(tokenType));
        assertEquals(false,queryInfo5.contains(tokenType2));
        assertEquals(false,queryInfo5.contains(tokenType));


        String zeroInfo = tokenModule.tokenGetDestroyBalance();
        assertEquals("1000",JSONObject.fromObject(zeroInfo).getJSONObject("data").getString(tokenType));
        assertEquals("1000",JSONObject.fromObject(zeroInfo).getJSONObject("data").getString(tokenType2));
    }

    /**
     *TC33?????????????????????????????????????????????
     * ????????????-??????????????????-????????????????????????token-??????-??????-??????
     * @throws Exception
     */
    @Test
    public void TC33_MultiToMulti() throws Exception {

        List<Map>listInit = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType,"1000");
        List<Map>list0 = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType2,"1000",listInit);

        //??????????????????????????????????????????token
        String transferInfoInit = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1,list0);
        assertEquals("200",JSONObject.fromObject(transferInfoInit).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);//UTXO???????????????????????????????????????

        List<Map> list = utilsClass.tokenConstructToken(tokenMultiAddr4, tokenType, "10");
        List<Map> list2 = utilsClass.tokenConstructToken(tokenMultiAddr3, tokenType2, "10", list);
        List<Map> list3 = utilsClass.tokenConstructToken(tokenMultiAddr3, tokenType, "10", list);

        String transferInfo = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr2,list2);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);//UTXO???????????????????????????????????????

        String transferInfo2 = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr2,list3);
        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        log.info("????????????????????????????????????");
        String queryInfo= tokenModule.tokenGetBalance(tokenMultiAddr4,"");
        String queryInfo2= tokenModule.tokenGetBalance(tokenMultiAddr2,"");
        String queryInfo3= tokenModule.tokenGetBalance(tokenMultiAddr3,"");
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("state"));
        assertEquals("20",JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType));
        assertEquals("10",JSONObject.fromObject(queryInfo3).getJSONObject("data").getString(tokenType));
        assertEquals("10",JSONObject.fromObject(queryInfo3).getJSONObject("data").getString(tokenType2));

        assertEquals("970",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString(tokenType));
        assertEquals("990",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString(tokenType2));

        log.info("???????????????Token: "+tokenType);
        String freezeInfo = tokenModule.tokenFreezeToken(tokenType);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        log.info("???????????????Token: "+tokenType);
        String recoverInfo = tokenModule.tokenRecoverToken(tokenType);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        log.info("??????Token");
        String recycleInfo = commonFunc.tokenModule_DestoryToken(tokenMultiAddr2, tokenType, "970");
        String recycleInfo2 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr2, tokenType2, "990");
        String recycleInfo3 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr4, tokenType, "20");
        String recycleInfo4 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr3, tokenType, "10");
        String recycleInfo5 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr3, tokenType2, "10");

        assertEquals("200",JSONObject.fromObject(recycleInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo3).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo4).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo5).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(recycleInfo2,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(recycleInfo5,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        log.info("????????????????????????????????????");
        String queryInfo6= tokenModule.tokenGetBalance(tokenMultiAddr3,"");
        String queryInfo4= tokenModule.tokenGetBalance(tokenMultiAddr4,"");
        String queryInfo5= tokenModule.tokenGetBalance(tokenMultiAddr2,"");
        assertEquals("200",JSONObject.fromObject(queryInfo6).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo5).getString("state"));
        assertEquals(false,queryInfo6.contains(tokenType2));
        assertEquals(false,queryInfo6.contains(tokenType));
        assertEquals(false,queryInfo4.contains(tokenType2));
        assertEquals(false,queryInfo4.contains(tokenType));
        assertEquals(false,queryInfo5.contains(tokenType2));
        assertEquals(false,queryInfo5.contains(tokenType));


        String zeroInfo = tokenModule.tokenGetDestroyBalance();
        assertEquals("1000",JSONObject.fromObject(zeroInfo).getJSONObject("data").getString(tokenType));
        assertEquals("1000",JSONObject.fromObject(zeroInfo).getJSONObject("data").getString(tokenType2));
    }

    /**
     *TC34?????????????????????????????????????????????
     * ????????????-??????????????????-????????????????????????token-??????-??????token-??????-??????
     * @throws Exception
     */
    @Test
    public void TC34_MultiToSolo() throws Exception {
        List<Map>listInit = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType,"1000");
        List<Map>list0 = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType2,"1000",listInit);

        //??????????????????????????????????????????token
        String transferInfoInit = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1,list0);
        assertEquals("200",JSONObject.fromObject(transferInfoInit).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);//UTXO???????????????????????????????????????

        List<Map> list = utilsClass.tokenConstructToken(tokenAccount1, tokenType, "10");
        List<Map> list2 = utilsClass.tokenConstructToken(tokenAccount2, tokenType2, "10", list);
        List<Map> list3 = utilsClass.tokenConstructToken(tokenAccount2, tokenType, "10", list);

        String transferInfo = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr2,list2);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);//UTXO???????????????????????????????????????

        String transferInfo2 = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr2,list3);
        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        log.info("????????????????????????????????????");
        String queryInfo= tokenModule.tokenGetBalance(tokenAccount1,"");
        String queryInfo2= tokenModule.tokenGetBalance(tokenMultiAddr2,"");
        String queryInfo3= tokenModule.tokenGetBalance(tokenAccount2,"");
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo2).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo3).getString("state"));
        assertEquals("20",JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType));
        assertEquals("10",JSONObject.fromObject(queryInfo3).getJSONObject("data").getString(tokenType));
        assertEquals("10",JSONObject.fromObject(queryInfo3).getJSONObject("data").getString(tokenType2));

        assertEquals("970",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString(tokenType));
        assertEquals("990",JSONObject.fromObject(queryInfo2).getJSONObject("data").getString(tokenType2));

        log.info("???????????????Token: "+tokenType);
        String freezeInfo = tokenModule.tokenFreezeToken(tokenType);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        log.info("???????????????Token: "+tokenType);
        String recoverInfo = tokenModule.tokenRecoverToken(tokenType);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        log.info("??????Token");
        String recycleInfo = commonFunc.tokenModule_DestoryToken(tokenMultiAddr2, tokenType, "970");
        String recycleInfo2 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr2, tokenType2, "990");
        String recycleInfo3 = commonFunc.tokenModule_DestoryToken(tokenAccount1, tokenType, "20");
        String recycleInfo4 = commonFunc.tokenModule_DestoryToken(tokenAccount2, tokenType, "10");
        String recycleInfo5 = commonFunc.tokenModule_DestoryToken(tokenAccount2, tokenType2, "10");

        assertEquals("200",JSONObject.fromObject(recycleInfo).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo2).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo3).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo4).getString("state"));
        assertEquals("200",JSONObject.fromObject(recycleInfo5).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(recycleInfo2,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(recycleInfo5,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        log.info("????????????????????????????????????");
        String queryInfo6= tokenModule.tokenGetBalance(tokenAccount2,"");
        String queryInfo4= tokenModule.tokenGetBalance(tokenAccount1,"");
        String queryInfo5= tokenModule.tokenGetBalance(tokenMultiAddr2,"");
        assertEquals("200",JSONObject.fromObject(queryInfo6).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo4).getString("state"));
        assertEquals("200",JSONObject.fromObject(queryInfo5).getString("state"));
        assertEquals(false,queryInfo6.contains(tokenType2));
        assertEquals(false,queryInfo6.contains(tokenType));
        assertEquals(false,queryInfo4.contains(tokenType2));
        assertEquals(false,queryInfo4.contains(tokenType));
        assertEquals(false,queryInfo5.contains(tokenType2));
        assertEquals(false,queryInfo5.contains(tokenType));


        String zeroInfo = tokenModule.tokenGetDestroyBalance();
        assertEquals("1000",JSONObject.fromObject(zeroInfo).getJSONObject("data").getString(tokenType));
        assertEquals("1000",JSONObject.fromObject(zeroInfo).getJSONObject("data").getString(tokenType2));
    }

    /**
     * ??????????????????????????? ??????????????????
     * @throws Exception
     */
    @Test
    public void TransferToSelf() throws Exception {
        String transferInfo= commonFunc.tokenModule_TransferToken(tokenMultiAddr1, tokenMultiAddr1,tokenType,"10");
        assertEquals(true,transferInfo.contains("can't transfer it to yourself"));
    }


    @Test
    public void multi33AccountDoubleSpend_IssueSelf()throws Exception{
        if(syncFlag) return;//???????????????????????????????????????
        String issueAddr = "";
        String collAddr = "";
        String issueToken = "";
        String issAmount ="";

        //??????????????????token 5000.999999
        double sAmount = 5000.999999;
        issueAddr = tokenMultiAddr1;
        collAddr = tokenMultiAddr1;
        issAmount = String.valueOf(sAmount);

        //????????????
        String from = collAddr;
        String to = "";
        String to1 = tokenMultiAddr2;
        String to2 = tokenAccount2;
        double trfAmount1 = 100.253;
        double trfAmount2 = 689.333;


        issueToken = commonFunc.tokenModule_IssueToken(issueAddr,collAddr,issAmount);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        String queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

        //????????????????????????????????????????????????

        String transferToken = issueToken;
        String transferAmount = String.valueOf(trfAmount1);
        to = to1;
        String transferResp = commonFunc.tokenModule_TransferToken(from,to,transferToken,transferAmount);

        to = to2;
        transferAmount = String.valueOf(trfAmount2);
        transferResp = commonFunc.tokenModule_TransferToken(from,to,issueToken,transferAmount);


        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        //????????????
        queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertThat(JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken),
                anyOf(containsString("4900.746999"),containsString("4311.666999")));


        //????????????
        String destroyResp = commonFunc.tokenModule_DestoryTokenByTokenType(issueToken);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashTypeDesByType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        //????????????
        queryBalance = tokenModule.tokenGetBalance(collAddr,"");
        assertEquals(false,queryBalance.contains(issueToken));
    }


    /**
     * ??????????????????????????????UTXO???????????????
     * @throws Exception
     */
    @Test
    public void issueCheckUTXO() throws Exception {
        issueAmount1 = "1000.12345678912345";
        issueAmount2 = "1000.876543212345";

        if (UtilsClass.PRECISION == 10) {
            actualAmount1 = "1000.1234567891";
            actualAmount2 = "1000.8765432123";
        }else {
            actualAmount1 = "1000.123456";
            actualAmount2 = "1000.876543";
        }

        log.info("????????????token");
        //??????????????????????????????sleep??????
        tokenType = utilsClass.Random(6);
        tokenType2 = UtilsClass.Random(6);
        String response1 = tokenModule.tokenIssue(tokenAccount1,tokenAccount1,tokenType,actualAmount1,"??????token");
        String response2 = tokenModule.tokenIssue(tokenMultiAddr1,tokenMultiAddr2,tokenType2,actualAmount2,"??????token2");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        log.info("??????UTXO??????");

        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
        assertEquals(tokenAccount1,JSONObject.fromObject(response1).getJSONArray("utxo").getJSONObject(0).getString("address"));
        assertEquals(actualAmount1,JSONObject.fromObject(response1).getJSONArray("utxo").getJSONObject(0).getString("amount"));
        assertEquals(tokenType,JSONObject.fromObject(response1).getJSONArray("utxo").getJSONObject(0).getString("tokentype"));
        assertEquals("0",JSONObject.fromObject(response1).getJSONArray("utxo").getJSONObject(0).getString("index"));

        assertEquals("200",JSONObject.fromObject(response2).getString("state"));
        assertEquals(tokenMultiAddr2,JSONObject.fromObject(response2).getJSONArray("utxo").getJSONObject(0).getString("address"));
        assertEquals(actualAmount2,JSONObject.fromObject(response2).getJSONArray("utxo").getJSONObject(0).getString("amount"));
        assertEquals(tokenType2,JSONObject.fromObject(response2).getJSONArray("utxo").getJSONObject(0).getString("tokentype"));
        assertEquals("0",JSONObject.fromObject(response2).getJSONArray("utxo").getJSONObject(0).getString("index"));

    }


    /**
     * ?????????????????????????????????UTXO???????????????
     * @throws Exception
     */
    @Test
    public void transferByUTXO()throws Exception{

        //T1>T1??????1000.123456???token1???M1>M2??????1000.876543???token2
        tokenType = utilsClass.Random(6);
        tokenType2 = UtilsClass.Random(6);
        String response1 = tokenModule.tokenIssue(tokenAccount1,tokenAccount1,tokenType,actualAmount1,"??????token/1000.123456");
        String response2 = tokenModule.tokenIssue(tokenMultiAddr1,tokenMultiAddr2,tokenType2,actualAmount2,"??????token2/1000.876543");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        String hash1 = JSONObject.fromObject(response1).getString("data");
        String hash2 = JSONObject.fromObject(response2).getString("data");

        //T1>>M1\M2\M3????????????200???token1
        List<Map> list = utilsClass.tokenConstructUTXO(hash1,0,"200",tokenMultiAddr1);
        List<Map> list2 = utilsClass.tokenConstructUTXO(hash1,0,"200",tokenMultiAddr2,list);
        List<Map> list3 = utilsClass.tokenConstructUTXO(hash1,0,"200",tokenMultiAddr3,list2);
        String transferInfo1 = commonFunc.tokenModule_TransferTokenList(tokenAccount1,list3);
        String transferInfoHash1 = JSONObject.fromObject(transferInfo1).getString("data");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        //M2>>T1????????????100???token1\token2???M2>>M3??????100???token2
        List<Map>list4 = utilsClass.tokenConstructUTXO(transferInfoHash1,1,"100",tokenAccount1);
        List<Map>list5 = utilsClass.tokenConstructUTXO(hash2,0,"100",tokenAccount1,list4);
        List<Map>list6 = utilsClass.tokenConstructUTXO(hash2,0,"100",tokenMultiAddr3,list5);
        List<Map>listforto = utilsClass.tokenConstructToken(tokenMultiAddr1,tokenType2,"100");
        String transferInfo2 = tokenModule.tokenTransfer(tokenMultiAddr2,"to???utxo??????????????????utxo",listforto,list6);
        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        //M1>>T1??????100???token1
        List<Map>list7 = utilsClass.tokenConstructUTXO(transferInfoHash1,0,"100",tokenAccount1);
        String transferInfo3 = tokenModule.tokenTransfer(tokenMultiAddr1,"to???utxo??????????????????utxo",listforto,list7);
        assertEquals("200",JSONObject.fromObject(transferInfo3).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        //T1-token1??????600.123456???T1-token2??????100???M1-token1??????100,M2-token1??????100,M2-token2??????800.876543,M3-token1??????200,M3-token2??????100
        String queryBalance = tokenModule.tokenGetBalance(tokenAccount1,"");
        String queryBalance1= tokenModule.tokenGetBalance(tokenMultiAddr1,"");
        String queryBalance2 = tokenModule.tokenGetBalance(tokenMultiAddr2,"");
        String queryBalance3 = tokenModule.tokenGetBalance(tokenMultiAddr3,"");

        assertEquals("600.123456", JSONObject.fromObject(queryBalance).getJSONObject("data").getString(tokenType));
        assertEquals("100", JSONObject.fromObject(queryBalance).getJSONObject("data").getString(tokenType2));
        assertEquals("100", JSONObject.fromObject(queryBalance1).getJSONObject("data").getString(tokenType));
        assertEquals("100", JSONObject.fromObject(queryBalance2).getJSONObject("data").getString(tokenType));
        assertEquals("800.876543", JSONObject.fromObject(queryBalance2).getJSONObject("data").getString(tokenType2));
        assertEquals("200", JSONObject.fromObject(queryBalance3).getJSONObject("data").getString(tokenType));
        assertEquals("100", JSONObject.fromObject(queryBalance3).getJSONObject("data").getString(tokenType2));

        //????????????
        String destroyResp = commonFunc.tokenModule_DestoryTokenByTokenType(tokenType);
        String destroyResp2 = commonFunc.tokenModule_DestoryTokenByTokenType(tokenType2);

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
        //T1>T1??????1000.123456???token1???M1>M2??????1000.876543???token2
        tokenType = utilsClass.Random(6);
        tokenType2 = UtilsClass.Random(6);
        String response1 = tokenModule.tokenIssue(tokenAccount1,tokenAccount1,tokenType,actualAmount1,"??????token/1000.123456");
        String response2 = tokenModule.tokenIssue(tokenMultiAddr1,tokenMultiAddr2,tokenType2,actualAmount2,"??????token2/1000.876543");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        String hash1 = JSONObject.fromObject(response1).getString("data");
        String hash2 = JSONObject.fromObject(response2).getString("data");

        //T1>>M1\M2\M3????????????200???token1
        List<Map> list = utilsClass.tokenConstructUTXO(hash1,0,"200",tokenMultiAddr1);
        List<Map> list2 = utilsClass.tokenConstructUTXO(hash1,0,"200",tokenMultiAddr2,list);
        List<Map> list3 = utilsClass.tokenConstructUTXO(hash1,0,"200",tokenMultiAddr3,list2);
        String transferInfo1 = commonFunc.tokenModule_TransferTokenList(tokenAccount1,list3);
        String transferInfoHash1 = JSONObject.fromObject(transferInfo1).getString("data");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        //M1???M2???M3???T1????????????utxo??????100???token1???M2??????utxo??????100???token2
        List<Map>list4 = utilsClass.tokenConstrucDestroytUTXO(transferInfoHash1,0,"100");
        List<Map>list5 = utilsClass.tokenConstrucDestroytUTXO(transferInfoHash1,1,"100",list4);
        List<Map>list6 = utilsClass.tokenConstrucDestroytUTXO(transferInfoHash1,2,"100",list5);
        List<Map>list7 = utilsClass.tokenConstrucDestroytUTXO(transferInfoHash1,3,"100",list6);
        List<Map>list8 = utilsClass.tokenConstrucDestroytUTXO(hash2,0,"100",list7);
        List<Map>destroylist = utilsClass.tokenConstructToken(tokenMultiAddr3,tokenType,"100");
        String destroyInfo = tokenModule.tokenDestoryByList(destroylist,list8,"to???utxo??????????????????utxo",mapSendMsg);
        assertEquals("200",JSONObject.fromObject(destroyInfo).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        //????????????
        String queryBalance = tokenModule.tokenGetBalance(tokenAccount1,tokenType);
        String queryBalance1 = tokenModule.tokenGetBalance(tokenMultiAddr1,tokenType);
        String queryBalance2 = tokenModule.tokenGetBalance(tokenMultiAddr2,tokenType);
        String queryBalance3 = tokenModule.tokenGetBalance(tokenMultiAddr3,tokenType);
        String queryBalance4 = tokenModule.tokenGetBalance(tokenMultiAddr2,tokenType2);

        assertEquals("300.123456",JSONObject.fromObject(queryBalance).getJSONObject("data").getString(tokenType));
        assertEquals("100",JSONObject.fromObject(queryBalance1).getJSONObject("data").getString(tokenType));
        assertEquals("100",JSONObject.fromObject(queryBalance2).getJSONObject("data").getString(tokenType));
        assertEquals("100",JSONObject.fromObject(queryBalance3).getJSONObject("data").getString(tokenType));
        assertEquals("900.876543",JSONObject.fromObject(queryBalance4).getJSONObject("data").getString(tokenType2));


    }


    //----------------------------------------------------------------------------------------------------------------------//
    //?????????????????????
    @Test
    public void multi33AccountDoubleSpend_IssueOther()throws Exception{
        if(syncFlag) return;//???????????????????????????????????????
        String issueAddr = "";
        String collAddr = "";
        String issueToken = "";
        String issAmount ="";

        //??????????????????token 5000.999999
        double sAmount = 5000.999999;
        issueAddr = tokenMultiAddr1;
        collAddr = tokenMultiAddr2;
        issAmount = String.valueOf(sAmount);

        //????????????
        String from = collAddr;
        String to = "";
        String to1 = tokenAccount1;
        String to2 = tokenMultiAddr1;
        double trfAmount1 = 100.253;
        double trfAmount2 = 689.333;

        issueToken = commonFunc.tokenModule_IssueToken(issueAddr,collAddr,issAmount);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        //???????????????????????? ??? ????????????
        String queryBalance = tokenModule.tokenGetBalance(issueAddr,issueToken);
        assertEquals(false,queryBalance.contains(issueToken));

        queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

        //????????????????????????????????????????????????
        String transferToken = issueToken;
        String transferAmount = String.valueOf(trfAmount1);
        to = to1;
        String transferResp = commonFunc.tokenModule_TransferToken(from,to,transferToken,transferAmount);

        to = to2;
        transferAmount = String.valueOf(trfAmount2);
        transferResp = commonFunc.tokenModule_TransferToken(from,to,issueToken,transferAmount);


        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        //????????????
        queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(String.valueOf(sAmount - trfAmount1), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        queryBalance = tokenModule.tokenGetBalance(to1,issueToken);
        assertEquals(String.valueOf(trfAmount1), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        queryBalance = tokenModule.tokenGetBalance(to2,issueToken);
        assertEquals(false,queryBalance.contains(issueToken));

        //????????????
        String desAddr = collAddr;
        double desAmount = 500.698547;
        String desToken = issueToken;
        String desAmountStr = String.valueOf(desAmount);
        String destroyResp = commonFunc.tokenModule_DestoryToken(desAddr,desToken,desAmountStr);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        //????????????
        queryBalance = tokenModule.tokenGetBalance(collAddr,desToken);
        assertEquals(utilsClass.get6(sAmount - trfAmount1 - desAmount), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));
        queryBalance = tokenModule.tokenGetBalance(to1,desToken);
        assertEquals(String.valueOf(trfAmount1), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));
        queryBalance = tokenModule.tokenGetBalance(to2,desToken);
        assertEquals(false,queryBalance.contains(desToken));

        queryBalance = tokenModule.tokenGetDestroyBalance();
        assertEquals(String.valueOf(desAmount), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));

    }


    @Test
    public void multi12AccountDoubleSpend_IssueSelf()throws Exception{
        if(syncFlag) return;//???????????????????????????????????????

        String issueAddr = "";
        String collAddr = "";
        String issueToken = "";
        String issAmount ="";

        //??????????????????token 5000.999999
        double sAmount = 5000.999999;
        issueAddr = tokenMultiAddr2;
        collAddr = tokenMultiAddr2;
        issAmount = String.valueOf(sAmount);

        //????????????
        String from = collAddr;
        String to = "";
        String to1 = tokenMultiAddr1;
        String to2 = tokenAccount2;
        double trfAmount1 = 100.253;
        double trfAmount2 = 689.333;

        issueToken = commonFunc.tokenModule_IssueToken(issueAddr,collAddr,issAmount);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        String queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

        //????????????????????????????????????????????????

        String transferToken = issueToken;
        String transferAmount = String.valueOf(trfAmount1);
        to = to1;
        String transferResp = commonFunc.tokenModule_TransferToken(from,to,transferToken,transferAmount);

        to = to2;
        transferAmount = String.valueOf(trfAmount2);
        transferResp = commonFunc.tokenModule_TransferToken(from,to,issueToken,transferAmount);


        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        //????????????
        queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(String.valueOf(sAmount - trfAmount1), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        queryBalance = tokenModule.tokenGetBalance(to1,issueToken);
        assertEquals(String.valueOf(trfAmount1), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        queryBalance = tokenModule.tokenGetBalance(to2,issueToken);
        assertEquals(false,queryBalance.contains(issueToken));

        //????????????
        String desAddr = collAddr;
        double desAmount = 500.698547;
        String desToken = issueToken;
        String desAmountStr = String.valueOf(desAmount);
        String destroyResp = commonFunc.tokenModule_DestoryToken(desAddr,desToken,desAmountStr);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        //????????????
        queryBalance = tokenModule.tokenGetBalance(collAddr,desToken);
        assertEquals(utilsClass.get6(sAmount - trfAmount1 - desAmount), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));
        queryBalance = tokenModule.tokenGetBalance(to1,desToken);
        assertEquals(String.valueOf(trfAmount1), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));
        queryBalance = tokenModule.tokenGetBalance(to2,desToken);
        assertEquals(false,queryBalance.contains(desToken));

        queryBalance = tokenModule.tokenGetDestroyBalance();
        assertEquals(String.valueOf(desAmount), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));

    }

    //???????????????3?????????
    @Test
    public void multi12AccountDoubleSpend_IssueOther()throws Exception{
        if(syncFlag) return;//???????????????????????????????????????

        String issueAddr = "";
        String collAddr = "";
        String issueToken = "";
        String issAmount ="";

        //??????????????????token 5000.999999
        double sAmount = 5000.999999;
        issueAddr = tokenMultiAddr2;
        collAddr = tokenMultiAddr1;
        issAmount = String.valueOf(sAmount);

        //????????????
        String from = collAddr;
        String to = "";
        String to1 = tokenAccount1;
        String to2 = tokenMultiAddr2;
        double trfAmount1 = 100.253;
        double trfAmount2 = 689.333;


       issueToken = commonFunc.tokenModule_IssueToken(issueAddr,collAddr,issAmount);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        //???????????????????????? ??? ????????????
        String queryBalance = tokenModule.tokenGetBalance(issueAddr,issueToken);
        assertEquals(false,queryBalance.contains(issueToken));

        queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

        //????????????????????????????????????????????????
        String transferToken = issueToken;
        String transferAmount = String.valueOf(trfAmount1);
        to = to1;
        String transferResp = commonFunc.tokenModule_TransferToken(from,to,transferToken,transferAmount);

        to = to2;
        transferAmount = String.valueOf(trfAmount2);
        transferResp = commonFunc.tokenModule_TransferToken(from,to,issueToken,transferAmount);

        transferResp = commonFunc.tokenModule_TransferToken(from,tokenAccount3,issueToken,transferAmount);


        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        //????????????
        queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(String.valueOf(sAmount - trfAmount1), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        queryBalance = tokenModule.tokenGetBalance(to1,issueToken);
        assertEquals(String.valueOf(trfAmount1), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        queryBalance = tokenModule.tokenGetBalance(to2,issueToken);
        assertEquals(false,queryBalance.contains(issueToken));

        //????????????
        String desAddr = collAddr;
        double desAmount = 500.698547;
        String desToken = issueToken;
        String desAmountStr = String.valueOf(desAmount);
        String destroyResp = commonFunc.tokenModule_DestoryToken(desAddr,desToken,desAmountStr);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        //????????????
        queryBalance = tokenModule.tokenGetBalance(collAddr,desToken);
        assertEquals(utilsClass.get6(sAmount - trfAmount1 - desAmount), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));
        queryBalance = tokenModule.tokenGetBalance(to1,desToken);
        assertEquals(String.valueOf(trfAmount1), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));
        queryBalance = tokenModule.tokenGetBalance(to2,desToken);
        assertEquals(false,queryBalance.contains(desToken));

        queryBalance = tokenModule.tokenGetDestroyBalance();
        assertEquals(String.valueOf(desAmount), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));

    }

    @Test
    public void multi12AccountDoubleSpendSameAcc_IssueOther()throws Exception{
        if(syncFlag) return;//???????????????????????????????????????

        String issueAddr = "";
        String collAddr = "";
        String issueToken = "";
        String issAmount ="";

        //??????????????????token 5000.999999
        double sAmount = 5000.999999;
        issueAddr = tokenMultiAddr2;
        collAddr = tokenMultiAddr1;
        issAmount = String.valueOf(sAmount);

        //????????????
        String from = collAddr;
        String to = "";
        String to1 = tokenAccount1;
        String to2 = tokenMultiAddr2;
        double trfAmount1 = 100.253;
        double trfAmount2 = 689.333;

        //?????????????????????????????????
        tokenModule.tokenAddMintAddr(issueAddr);
        tokenModule.tokenAddCollAddr(collAddr);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        issueToken = commonFunc.tokenModule_IssueToken(issueAddr,collAddr,issAmount);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        //???????????????????????? ??? ????????????
        String queryBalance = tokenModule.tokenGetBalance(issueAddr,issueToken);
        assertEquals(false,queryBalance.contains(issueToken));

        queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

        //????????????????????????????????????????????????
        String transferToken = issueToken;
        String transferAmount = String.valueOf(trfAmount1);
        to = to1;
        String transferResp = commonFunc.tokenModule_TransferToken(from,to,transferToken,transferAmount);

        transferAmount = String.valueOf(trfAmount2);
        transferResp = commonFunc.tokenModule_TransferToken(from,to,issueToken,transferAmount);
        transferResp = commonFunc.tokenModule_TransferToken(from,to,issueToken,transferAmount);


        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        //????????????
        queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);

        assertThat(JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken),
                anyOf(containsString(String.valueOf(sAmount - trfAmount1)),containsString(String.valueOf(sAmount - trfAmount2))));
        queryBalance = tokenModule.tokenGetBalance(to1,issueToken);
        assertThat(JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken),
                anyOf(containsString(String.valueOf(trfAmount1)),containsString(String.valueOf(trfAmount2))));

        //????????????
        String desAddr = collAddr;
        double desAmount = 50.123;
        String desToken = issueToken;
        String desAmountStr = String.valueOf(desAmount);
        String destroyResp = commonFunc.tokenModule_DestoryToken(desAddr,desToken,desAmountStr);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        //????????????
        queryBalance = tokenModule.tokenGetBalance(collAddr,desToken);
        assertThat(JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken),
                anyOf(containsString(String.valueOf(sAmount - trfAmount1 - desAmount)),
                        containsString(String.valueOf(sAmount - trfAmount2 - desAmount))));

        queryBalance = tokenModule.tokenGetDestroyBalance();
        assertEquals(String.valueOf(desAmount), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));

    }

    @Test
    public void multi33Account_IssueSelf()throws Exception{
        String issueAddr = "";
        String collAddr = "";
        String issueToken = "";
        String issAmount ="";

        //??????????????????token 5000.999999
        double sAmount = 5000.999999;
        issueAddr = tokenMultiAddr1;
        collAddr = tokenMultiAddr1;
        issAmount = String.valueOf(sAmount);

        //????????????
        String from = collAddr;
        String to = "";
        String to1 = tokenMultiAddr2;
        String to2 = tokenAccount2;
        double trfAmount1 = 100.253;
        double trfAmount2 = 689.333;

        issueToken = commonFunc.tokenModule_IssueToken(issueAddr,collAddr,issAmount);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        String queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

        //????????????????????????????????????????????????

        String transferToken = issueToken;
        String transferAmount = String.valueOf(trfAmount1);
        to = to1;
        String transferResp = commonFunc.tokenModule_TransferToken(from,to,transferToken,transferAmount);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        to = to2;
        transferAmount = String.valueOf(trfAmount2);
        transferResp = commonFunc.tokenModule_TransferToken(from,to,issueToken,transferAmount);


        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        //????????????
        queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(String.valueOf(sAmount - trfAmount1 - trfAmount2), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        queryBalance = tokenModule.tokenGetBalance(to1,issueToken);
        assertEquals(String.valueOf(trfAmount1), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        queryBalance = tokenModule.tokenGetBalance(to2,issueToken);
        assertEquals(String.valueOf(trfAmount2), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

        //????????????
        String desAddr = collAddr;
        double desAmount = 500.698547;
        String desToken = issueToken;
        String desAmountStr = String.valueOf(desAmount);
        String destroyResp = commonFunc.tokenModule_DestoryToken(desAddr,desToken,desAmountStr);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        //????????????
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
    public void multi33Account_IssueOther()throws Exception{
        String issueAddr = "";
        String collAddr = "";
        String issueToken = "";
        String issAmount ="";

        //??????????????????token 5000.999999
        String stokenType = "tokenSo_"+ UtilsClass.Random(8);
        double sAmount = 5000.999999;
        issueAddr = tokenMultiAddr1;
        collAddr = tokenMultiAddr2;
        issueToken =stokenType;
        issAmount = String.valueOf(sAmount);

        //????????????
        String from = collAddr;
        String to = "";
        String to1 = tokenAccount1;
        String to2 = tokenMultiAddr1;
        double trfAmount1 = 100.253;
        double trfAmount2 = 689.333;

        //?????????????????????????????????
        tokenModule.tokenAddMintAddr(issueAddr);
        tokenModule.tokenAddCollAddr(collAddr);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        issueToken = commonFunc.tokenModule_IssueToken(issueAddr,collAddr,issAmount);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        //???????????????????????? ??? ????????????
        String queryBalance = tokenModule.tokenGetBalance(issueAddr,issueToken);
        assertEquals(false,queryBalance.contains(issueToken));

        queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

        //????????????????????????????????????????????????
        String transferToken = issueToken;
        String transferAmount = String.valueOf(trfAmount1);
        to = to1;
        String transferResp = commonFunc.tokenModule_TransferToken(from,to,transferToken,transferAmount);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        to = to2;
        transferAmount = String.valueOf(trfAmount2);
        transferResp = commonFunc.tokenModule_TransferToken(from,to,issueToken,transferAmount);


        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        //????????????
        queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(String.valueOf(sAmount - trfAmount1 - trfAmount2), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        queryBalance = tokenModule.tokenGetBalance(to1,issueToken);
        assertEquals(String.valueOf(trfAmount1), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        queryBalance = tokenModule.tokenGetBalance(to2,issueToken);
        assertEquals(String.valueOf(trfAmount2), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

        //????????????
        String desAddr = collAddr;
        double desAmount = 500.698547;
        String desToken = issueToken;
        String desAmountStr = String.valueOf(desAmount);
        String destroyResp = commonFunc.tokenModule_DestoryToken(desAddr,desToken,desAmountStr);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        //????????????
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
    public void multi12Account_IssueSelf()throws Exception{
        String issueAddr = "";
        String collAddr = "";
        String issueToken = "";
        String issAmount ="";

        //??????????????????token 5000.999999
        double sAmount = 5000.999999;
        issueAddr = tokenMultiAddr2;
        collAddr = tokenMultiAddr2;
        issAmount = String.valueOf(sAmount);

        //????????????
        String from = collAddr;
        String to = "";
        String to1 = tokenMultiAddr1;
        String to2 = tokenAccount2;
        double trfAmount1 = 100.253;
        double trfAmount2 = 689.333;


        issueToken = commonFunc.tokenModule_IssueToken(issueAddr,collAddr,issAmount);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        String queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

        //????????????????????????????????????????????????

        String transferToken = issueToken;
        String transferAmount = String.valueOf(trfAmount1);
        to = to1;
        String transferResp = commonFunc.tokenModule_TransferToken(from,to,transferToken,transferAmount);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        to = to2;
        transferAmount = String.valueOf(trfAmount2);
        transferResp = commonFunc.tokenModule_TransferToken(from,to,issueToken,transferAmount);


        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        //????????????
        queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(String.valueOf(sAmount - trfAmount1 - trfAmount2), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        queryBalance = tokenModule.tokenGetBalance(to1,issueToken);
        assertEquals(String.valueOf(trfAmount1), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        queryBalance = tokenModule.tokenGetBalance(to2,issueToken);
        assertEquals(String.valueOf(trfAmount2), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

        //????????????
        String desAddr = collAddr;
        double desAmount = 500.698547;
        String desToken = issueToken;
        String desAmountStr = String.valueOf(desAmount);
        String destroyResp = commonFunc.tokenModule_DestoryToken(desAddr,desToken,desAmountStr);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        //????????????
        queryBalance = tokenModule.tokenGetBalance(collAddr,desToken);
        assertEquals(utilsClass.get6(sAmount - trfAmount1 - trfAmount2 - desAmount), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));
        queryBalance = tokenModule.tokenGetBalance(to1,desToken);
        assertEquals(String.valueOf(trfAmount1), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));
        queryBalance = tokenModule.tokenGetBalance(to2,desToken);
        assertEquals(String.valueOf(trfAmount2), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

        queryBalance = tokenModule.tokenGetDestroyBalance();
        assertEquals(String.valueOf(desAmount), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));

    }

    //1/2???????????????3/3?????????????????????
    //3/3??????????????????????????????1/2??????
    //????????????????????????????????????
    //??????????????????
    @Test
    public void multi12Account_IssueOther()throws Exception{
        String issueAddr = "";
        String collAddr = "";
        String issueToken = "";
        String issAmount ="";

        //??????????????????token 5000.999999
        String stokenType = "tokenSo_"+ UtilsClass.Random(8);
        double sAmount = 5000.999999;
        issueAddr = tokenMultiAddr2;
        collAddr = tokenMultiAddr1;
        issueToken =stokenType;
        issAmount = String.valueOf(sAmount);

        //????????????
        String from = collAddr;
        String to = "";
        String to1 = tokenAccount1;
        String to2 = tokenMultiAddr2;
        double trfAmount1 = 1000.253;
        double trfAmount2 = 689.333;
        double trfAmount3 = 500.123456;


        //?????????????????????????????????
        tokenModule.tokenAddMintAddr(issueAddr);
        tokenModule.tokenAddCollAddr(collAddr);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        issueToken = commonFunc.tokenModule_IssueToken(issueAddr,collAddr,issAmount);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        //???????????????????????? ??? ????????????
        String queryBalance = tokenModule.tokenGetBalance(issueAddr,issueToken);
        assertEquals(false,queryBalance.contains(issueToken));

        queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(issAmount, JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

        //????????????????????????????????????????????????
        String transferToken = issueToken;
        String transferAmount = String.valueOf(trfAmount1);
        to = to1;
        String transferResp = commonFunc.tokenModule_TransferToken(from,to,transferToken,transferAmount);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);
        to = to2;
        transferAmount = String.valueOf(trfAmount2);
        transferResp = commonFunc.tokenModule_TransferToken(from,to,issueToken,transferAmount);


        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        //????????????
        queryBalance = tokenModule.tokenGetBalance(collAddr,issueToken);
        assertEquals(utilsClass.get6(sAmount - trfAmount1 - trfAmount2), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        queryBalance = tokenModule.tokenGetBalance(to1,issueToken);
        assertEquals(String.valueOf(trfAmount1), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));
        queryBalance = tokenModule.tokenGetBalance(to2,issueToken);
        assertEquals(String.valueOf(trfAmount2), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

        //????????????
        String desAddr = collAddr;
        double desAmount = 500.698547;
        String desToken = issueToken;
        String desAmountStr = String.valueOf(desAmount);
        String destroyResp = commonFunc.tokenModule_DestoryToken(desAddr,desToken,desAmountStr);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        //????????????
        queryBalance = tokenModule.tokenGetBalance(collAddr,desToken);
        assertEquals(utilsClass.get6(sAmount - trfAmount1 - trfAmount2 - desAmount), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));
        queryBalance = tokenModule.tokenGetBalance(to1,desToken);
        assertEquals(String.valueOf(trfAmount1), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));
        queryBalance = tokenModule.tokenGetBalance(to2,desToken);
        assertEquals(String.valueOf(trfAmount2), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(issueToken));

        queryBalance = tokenModule.tokenGetDestroyBalance();
        assertEquals(String.valueOf(desAmount), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));

        from = tokenAccount1;
        to = tokenAccount3;
        transferAmount = String.valueOf(trfAmount3);
        transferResp = commonFunc.tokenModule_TransferToken(from,to,issueToken,transferAmount);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        queryBalance = tokenModule.tokenGetBalance(from,desToken);
        assertEquals(utilsClass.get6(trfAmount1 - trfAmount3), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));
        queryBalance = tokenModule.tokenGetBalance(to,desToken);
        assertEquals(String.valueOf(trfAmount3), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));

        //????????????
        desAddr = tokenAccount1;
        double desAmount2 = 100.123252;
        desToken = issueToken;
        desAmountStr = String.valueOf(desAmount2);
        destroyResp = commonFunc.tokenModule_DestoryToken(desAddr,desToken,desAmountStr);
        desAddr = tokenAccount3;
        double desAmount3 = 100.123252;
        desToken = issueToken;
        desAmountStr = String.valueOf(desAmount2);
        destroyResp = commonFunc.tokenModule_DestoryToken(desAddr,desToken,desAmountStr);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        queryBalance = tokenModule.tokenGetDestroyBalance();
        assertEquals(String.valueOf(desAmount + desAmount2 + desAmount3), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));
        queryBalance = tokenModule.tokenGetBalance(tokenAccount1,desToken);
        assertEquals(utilsClass.get6(trfAmount1 - trfAmount3 - desAmount2), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));

        queryBalance = tokenModule.tokenGetBalance(tokenAccount3,desToken);
        assertEquals(utilsClass.get6(trfAmount3 - desAmount3), JSONObject.fromObject(queryBalance).getJSONObject("data").getString(desToken));

    }

    @Test
    public void transfer10Addr()throws Exception{
        List<Map> list = utilsClass.tokenConstructToken(tokenAccount1,tokenType,"10");
        List<Map> list2 = utilsClass.tokenConstructToken(tokenAccount2,tokenType,"10",list);
        List<Map> list3 = utilsClass.tokenConstructToken(tokenAccount3,tokenType,"10",list2);
        List<Map> list4 = utilsClass.tokenConstructToken(tokenAccount4,tokenType,"10",list3);
        List<Map> list5 = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType,"10",list4);
        List<Map> list6 = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType2,"10",list5);
        List<Map> list7 = utilsClass.tokenConstructToken(tokenMultiAddr3,tokenType2,"10",list6);
        List<Map> list8 = utilsClass.tokenConstructToken(tokenAccount1,tokenType2,"10",list7);
        List<Map> list9 = utilsClass.tokenConstructToken(tokenAccount2,tokenType2,"10",list8);
        List<Map> list10 = utilsClass.tokenConstructToken(tokenAccount3,tokenType2,"10",list9);
        List<Map> list11 = utilsClass.tokenConstructToken(tokenAccount3,tokenType2,"10",list10);

        String transferInfo = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1,list11);
        assertEquals(true,transferInfo.contains("Transfer list cannot be more than 10"));

        transferInfo = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1,list10);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        String query = tokenModule.tokenGetBalance(tokenMultiAddr1,"");
        assertEquals(true,JSONObject.fromObject(query).getJSONObject("data").getString(tokenType).contains("950."));
        assertEquals(true,JSONObject.fromObject(query).getJSONObject("data").getString(tokenType2).contains("950."));


    }
    @Test
    public void destory10AddrAndCheckTxDetail()throws Exception{
        List<Map> list = utilsClass.tokenConstructToken(tokenAccount1,tokenType,"10");
        List<Map> list2 = utilsClass.tokenConstructToken(tokenAccount2,tokenType,"10",list);
        List<Map> list3 = utilsClass.tokenConstructToken(tokenAccount3,tokenType,"10",list2);
        List<Map> list4 = utilsClass.tokenConstructToken(tokenAccount4,tokenType,"10",list3);
        List<Map> list5 = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType,"10",list4);
        List<Map> list6 = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType2,"10",list5);
        List<Map> list7 = utilsClass.tokenConstructToken(tokenMultiAddr3,tokenType2,"10",list6);
        List<Map> list8 = utilsClass.tokenConstructToken(tokenAccount1,tokenType2,"10",list7);
        List<Map> list9 = utilsClass.tokenConstructToken(tokenAccount2,tokenType2,"10",list8);
        List<Map> list10 = utilsClass.tokenConstructToken(tokenAccount3,tokenType2,"10",list9);
        List<Map> list11 = utilsClass.tokenConstructToken(tokenAccount3,tokenType2,"10",list10);

        List<Map> list12 = utilsClass.tokenConstructToken(tokenMultiAddr1,tokenType2,"10",list);

        String amount1,amount2;
        if (UtilsClass.PRECISION == 10) {
            amount1 = "950.1234567891";
            amount2 = "950.8765432123";
        }else {
            amount1 = "950.123456";
            amount2 = "950.876543";
        }
        //?????????????????????????????????list??????
        List<Map> listT = commonFunc.constructUTXOTxDetailList(tokenMultiAddr1,tokenAccount1,tokenType,"10");
        List<Map> listT2 = commonFunc.constructUTXOTxDetailList(tokenMultiAddr1,tokenAccount2,tokenType,"10",listT);
        List<Map> listT3 = commonFunc.constructUTXOTxDetailList(tokenMultiAddr1,tokenAccount3,tokenType,"10",listT2);
        List<Map> listT4 = commonFunc.constructUTXOTxDetailList(tokenMultiAddr1,tokenAccount4,tokenType,"10",listT3);
        List<Map> listT5 = commonFunc.constructUTXOTxDetailList(tokenMultiAddr1,tokenMultiAddr2,tokenType,"10",listT4);
        List<Map> listT6 = commonFunc.constructUTXOTxDetailList(tokenMultiAddr1,tokenMultiAddr2,tokenType2,"10",listT5);
        List<Map> listT7 = commonFunc.constructUTXOTxDetailList(tokenMultiAddr1,tokenMultiAddr3,tokenType2,"10",listT6);
        List<Map> listT8 = commonFunc.constructUTXOTxDetailList(tokenMultiAddr1,tokenAccount1,tokenType2,"10",listT7);
        List<Map> listT9 = commonFunc.constructUTXOTxDetailList(tokenMultiAddr1,tokenAccount2,tokenType2,"10",listT8);
        List<Map> listT10 = commonFunc.constructUTXOTxDetailList(tokenMultiAddr1,tokenAccount3,tokenType2,"10",listT9);
        List<Map> listT11 = commonFunc.constructUTXOTxDetailList(tokenMultiAddr1,tokenMultiAddr1,tokenType,amount1,listT10);//??????????????????
        List<Map> listT12 = commonFunc.constructUTXOTxDetailList(tokenMultiAddr1,tokenMultiAddr1,tokenType2,amount2,listT11);//??????????????????

        //???????????????????????????????????????list??????
        List<Map> listR = commonFunc.constructUTXOTxDetailList(tokenAccount1,zeroAccount,tokenType,"10");
        List<Map> listR2 = commonFunc.constructUTXOTxDetailList(tokenAccount2,zeroAccount,tokenType,"10",listR);
        List<Map> listR3 = commonFunc.constructUTXOTxDetailList(tokenAccount3,zeroAccount,tokenType,"10",listR2);
        List<Map> listR4 = commonFunc.constructUTXOTxDetailList(tokenAccount4,zeroAccount,tokenType,"10",listR3);
        List<Map> listR5 = commonFunc.constructUTXOTxDetailList(tokenMultiAddr2,zeroAccount,tokenType,"10",listR4);
        List<Map> listR6 = commonFunc.constructUTXOTxDetailList(tokenMultiAddr2,zeroAccount,tokenType2,"10",listR5);
        List<Map> listR7 = commonFunc.constructUTXOTxDetailList(tokenMultiAddr3,zeroAccount,tokenType2,"10",listR6);
        List<Map> listR8 = commonFunc.constructUTXOTxDetailList(tokenAccount1,zeroAccount,tokenType2,"10",listR7);
        List<Map> listR9 = commonFunc.constructUTXOTxDetailList(tokenAccount2,zeroAccount,tokenType2,"10",listR8);
        List<Map> listR10 = commonFunc.constructUTXOTxDetailList(tokenAccount3,zeroAccount,tokenType2,"10",listR9);

        String transferInfo = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1,list10);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
        String transferHash = JSONObject.fromObject(transferInfo).getString("data");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);


        String query = tokenModule.tokenGetBalance(tokenMultiAddr1,"");
        assertEquals(amount1,JSONObject.fromObject(query).getJSONObject("data").getString(tokenType));
        assertEquals(amount2,JSONObject.fromObject(query).getJSONObject("data").getString(tokenType2));

        String destoryInfo = commonFunc.tokenModule_DestoryTokenByList2(list11);
        assertEquals(true,destoryInfo.contains("Transfer list cannot be more than 10"));

        destoryInfo = commonFunc.tokenModule_DestoryTokenByList2(list10);
        assertEquals("200",JSONObject.fromObject(destoryInfo).getString("state"));
        String desHash = JSONObject.fromObject(destoryInfo).getString("data");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        String query2 = tokenModule.tokenGetDestroyBalance();
        assertEquals("50",JSONObject.fromObject(query2).getJSONObject("data").getString(tokenType));
        assertEquals("50",JSONObject.fromObject(query2).getJSONObject("data").getString(tokenType2));

        destoryInfo = commonFunc.tokenModule_DestoryTokenByList2(list12);
        assertEquals("Insufficient Balance",JSONObject.fromObject(destoryInfo).getString("data"));

        //????????????????????????????????????????????????
        String detailInfo = tokenModule.tokenGetTxDetail(desHash);
        log.info(detailInfo);
        JSONArray jsonArray = JSONObject.fromObject(detailInfo).getJSONObject("data").getJSONObject("UTXO").getJSONArray("Records");
        assertEquals(true,commonFunc.checkListArray(listR10,jsonArray));

        String timestamp = JSONObject.fromObject(detailInfo).getJSONObject("data").getJSONObject("Header").get("Timestamp").toString();
        assertEquals(10, timestamp.length());

        //????????????????????????????????????
        String detailInfo2 = tokenModule.tokenGetTxDetail(transferHash);
        JSONArray jsonArray2 = JSONObject.fromObject(detailInfo2).getJSONObject("data").getJSONObject("UTXO").getJSONArray("Records");
        assertEquals(true,commonFunc.checkListArray(listT12,jsonArray2));

        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //???????????????????????????????????????bytokentype
        String transferInfo2 = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1,list10);
        assertEquals("200",JSONObject.fromObject(transferInfo2).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        if (UtilsClass.PRECISION == 10) {
            amount1 = "900.1234567891";
            amount2 = "900.8765432123";
        }else {
            amount1 = "900.123456";
            amount2 = "900.876543";
        }

        String query3 = tokenModule.tokenGetBalance(tokenMultiAddr1,"");
        assertEquals(amount1,JSONObject.fromObject(query3).getJSONObject("data").getString(tokenType));
        assertEquals(amount2,JSONObject.fromObject(query3).getJSONObject("data").getString(tokenType2));


        String desInfo2 = commonFunc.tokenModule_DestoryTokenByTokenType(tokenType);
        String desHash2 = JSONObject.fromObject(desInfo2).getJSONObject("data").getString("hash");
        String desInfo3 = commonFunc.tokenModule_DestoryTokenByTokenType(tokenType2);
        String desHash3 = JSONObject.fromObject(desInfo3).getJSONObject("data").getString("hash");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashTypeDesByType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        //tokenType
        List<Map> list2R = commonFunc.constructUTXOTxDetailList(tokenAccount1,zeroAccount,tokenType,"10");
        List<Map> list2R2 = commonFunc.constructUTXOTxDetailList(tokenAccount2,zeroAccount,tokenType,"10",list2R);
        List<Map> list2R3 = commonFunc.constructUTXOTxDetailList(tokenAccount3,zeroAccount,tokenType,"10",list2R2);
        List<Map> list2R4 = commonFunc.constructUTXOTxDetailList(tokenAccount4,zeroAccount,tokenType,"10",list2R3);
        List<Map> list2R5 = commonFunc.constructUTXOTxDetailList(tokenMultiAddr2,zeroAccount,tokenType,"10",list2R4);
        List<Map> list2R6 = commonFunc.constructUTXOTxDetailList(tokenMultiAddr1,zeroAccount,tokenType,amount1,list2R5);

        //tokenType2
        List<Map> list3R6 = commonFunc.constructUTXOTxDetailList(tokenMultiAddr2,zeroAccount,tokenType2,"10");
        List<Map> list3R7 = commonFunc.constructUTXOTxDetailList(tokenMultiAddr3,zeroAccount,tokenType2,"10",list3R6);
        List<Map> list3R8 = commonFunc.constructUTXOTxDetailList(tokenAccount1,zeroAccount,tokenType2,"10",list3R7);
        List<Map> list3R9 = commonFunc.constructUTXOTxDetailList(tokenAccount2,zeroAccount,tokenType2,"10",list3R8);
        List<Map> list3R10 = commonFunc.constructUTXOTxDetailList(tokenAccount3,zeroAccount,tokenType2,"10",list3R9);
        List<Map> list3R11 = commonFunc.constructUTXOTxDetailList(tokenMultiAddr1,zeroAccount,tokenType2,amount2,list3R10);
        //????????????bytokentype??????????????????list??????

        //???????????????????????????????????????
        String detailInfo3 = tokenModule.tokenGetTxDetail(desHash2);
        JSONArray jsonArray3 = JSONObject.fromObject(detailInfo3).getJSONObject("data").getJSONObject("UTXO").getJSONArray("Records");
        assertEquals(true,commonFunc.checkListArray(list2R6,jsonArray3));

        String detailInfo4 = tokenModule.tokenGetTxDetail(desHash3);
        JSONArray jsonArray4 = JSONObject.fromObject(detailInfo4).getJSONObject("data").getJSONObject("UTXO").getJSONArray("Records");
        assertEquals(true,commonFunc.checkListArray(list3R11,jsonArray4));

    }

    @Test
    public void destoryByTokenTest()throws Exception{
        //"???????????????" + PUBKEY3 + "??????3000???" + tokenType+",??????"+PUBKEY4+"??????";
        List<Map> list = utilsClass.tokenConstructToken(tokenAccount3,tokenType2,"300");
        List<Map> list2= utilsClass.tokenConstructToken(tokenAccount3,tokenType,"400",list);
        List<Map> list3 = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType,"300",list2);
        List<Map> list4 = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType2,"400",list3);
        String transferInfo = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1, list4);
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
        List<Map> listR = commonFunc.ConstructDesByTokenRespList(tokenAccount3,"400");
        List<Map> listR2= commonFunc.ConstructDesByTokenRespList(tokenMultiAddr2,"300",listR);
        List<Map> listR3= commonFunc.ConstructDesByTokenRespList(tokenMultiAddr1,amount1,listR2);

        List<Map> list1R = commonFunc.ConstructDesByTokenRespList(tokenAccount3,"300");
        List<Map> list1R2= commonFunc.ConstructDesByTokenRespList(tokenMultiAddr2,"400",list1R);
        List<Map> list1R3= commonFunc.ConstructDesByTokenRespList(tokenMultiAddr1,amount2,list1R2);

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
        String queryInfo2 = tokenModule.tokenGetBalance(tokenMultiAddr1,"");
        String queryInfo3 = tokenModule.tokenGetBalance(tokenMultiAddr2,"");

        assertEquals(false,queryInfo1.contains(tokenType2));
        assertEquals(false,queryInfo1.contains(tokenType));
        assertEquals(false,queryInfo2.contains(tokenType2));
        assertEquals(false,queryInfo2.contains(tokenType));
        assertEquals(false,queryInfo3.contains(tokenType2));
        assertEquals(false,queryInfo3.contains(tokenType));


    }

    @Test
    public void destoryByList()throws Exception{
        //"???????????????" + PUBKEY3 + "??????3000???" + tokenType+",??????"+PUBKEY4+"??????";
        List<Map> list = utilsClass.tokenConstructToken(tokenAccount3,tokenType2,"300");
        List<Map> list2= utilsClass.tokenConstructToken(tokenAccount3,tokenType,"400",list);
        List<Map> list3 = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType,"300",list2);
        List<Map> list4 = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType2,"400",list3);

        String transferInfo = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1, list4);
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
    //?????????????????????????????????
    @Test
    public void issueTokenMatchCase()throws Exception{
        String issueResp = tokenModule.tokenIssue(tokenMultiAddr1,tokenMultiAddr1,tokenType.toLowerCase(),
                "100","????????????tokentype?????????????????????token");
        assertEquals("200",JSONObject.fromObject(issueResp).getString("state"));

        String issueResp2 = tokenModule.tokenIssue(tokenMultiAddr1,tokenMultiAddr1,tokenType.toUpperCase(),
                "100","????????????tokentype?????????????????????token");
        assertEquals("200",JSONObject.fromObject(issueResp2).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        String query = tokenModule.tokenGetBalance(tokenMultiAddr1,"");
        assertEquals(true,query.contains(tokenType.toLowerCase()));
        assertEquals(true,query.contains(tokenType.toUpperCase()));
        assertEquals(true,query.contains(tokenType));

        query = tokenModule.tokenGetBalance(tokenMultiAddr1,tokenType);
        assertEquals(true,query.contains(tokenType));
        assertEquals(false,query.contains(tokenType.toLowerCase()));
        assertEquals(false,query.contains(tokenType.toUpperCase()));

        query = tokenModule.tokenGetBalance(tokenMultiAddr1,tokenType.toUpperCase());
        assertEquals(false,query.contains(tokenType.toLowerCase()));
        assertEquals(false,query.contains(tokenType));
        assertEquals(true,query.contains(tokenType.toUpperCase()));

        query = tokenModule.tokenGetBalance(tokenMultiAddr1,tokenType.toLowerCase());
        assertEquals(false,query.contains(tokenType.toUpperCase()));
        assertEquals(false,query.contains(tokenType));
        assertEquals(true,query.contains(tokenType.toLowerCase()));

    }

    //???????????????????????????token??????????????????
    @Test
    public void transferToZeroAccountByAddrList()throws Exception{
        String DBZeroAccout = "osEoy933LkHyyBcgjE7vCivfsX";
        log.info("?????????????????? ???????????????????????? ??????????????????token ????????????????????????");
        tokenModule.tokenFreezeToken(tokenType);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        List<Map> list = utilsClass.tokenConstructToken(DBZeroAccout,tokenType2,actualAmount2);
        List<Map> list2= utilsClass.tokenConstructToken(DBZeroAccout,tokenType,actualAmount1,list);

        String transferInfo = commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1, list2);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        log.info("?????????????????????????????????????????????token");
        String query = tokenModule.tokenGetBalance(tokenMultiAddr1,"");
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

        log.info("??????????????????UTXO????????????");
        assertEquals(tokenMultiAddr1,JSONObject.fromObject(issueResp).getJSONArray("utxo").getJSONObject(0).getString("address"));
        assertEquals(actualAmount1,JSONObject.fromObject(issueResp).getJSONArray("utxo").getJSONObject(0).getString("amount"));
        assertEquals(tokenType,JSONObject.fromObject(issueResp).getJSONArray("utxo").getJSONObject(0).getString("tokentype"));
        assertEquals("0",JSONObject.fromObject(issueResp).getJSONArray("utxo").getJSONObject(0).getString("index"));

        assertEquals(tokenMultiAddr1,JSONObject.fromObject(globalResponse).getJSONArray("utxo").getJSONObject(0).getString("address"));
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
        List<Map>listutxo2 = utilsClass.tokenConstructUTXO(utxoHash2,0,actualAmount2,DBZeroAccout,listutxo);

        String transferResp = tokenModule.tokenTransfer(tokenMultiAddr1,"utxo list transfer",null,listutxo2);
        assertEquals("200",JSONObject.fromObject(transferResp).getString("state"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        log.info("?????????????????????????????????????????????token");
        String query = tokenModule.tokenGetBalance(tokenMultiAddr1,"");
        assertEquals(false,query.contains(tokenType));
        assertEquals(false,query.contains(tokenType2));

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


    @AfterClass
    public static void resetAddr()throws Exception{
        SDKADD = TOKENADD;
        BeforeCondition beforeCondition = new BeforeCondition();
        beforeCondition.tokenAddIssueCollAddr();
    }
}
