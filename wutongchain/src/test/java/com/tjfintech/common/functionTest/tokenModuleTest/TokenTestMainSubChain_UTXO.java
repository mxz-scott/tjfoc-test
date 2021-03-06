package com.tjfintech.common.functionTest.tokenModuleTest;

import com.alibaba.fastjson.JSON;
import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.Token;
import com.tjfintech.common.MgToolCmd;
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

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class TokenTestMainSubChain_UTXO {
    TestBuilder testBuilder= TestBuilder.getInstance();

    private static String tokenType;
    private static String tokenType2;

    private static String issueAmount1;
    private static String issueAmount2;

    Token tokenModule = testBuilder.getToken();
    CommonFunc commonFunc = new CommonFunc();
    UtilsClass utilsClass=new UtilsClass();
    MgToolCmd mgToolCmd = new MgToolCmd();
    BeforeCondition beforeCondition = new BeforeCondition();

    String glbChain01= "glbCh1";
    String glbChain02= "glbCh2";

    String id1 = getPeerId(PEER1IP,USERNAME,PASSWD);
    String id2 = getPeerId(PEER2IP,USERNAME,PASSWD);
    String id3 = getPeerId(PEER4IP,USERNAME,PASSWD);
    String ids = " -m "+ id1+","+ id2+","+ id3;


    @BeforeClass
    public static void init()throws Exception
    {
        SDKADD = TOKENADD;
        if(tokenAccount1.isEmpty()) {
            BeforeCondition beforeCondition = new BeforeCondition();
            beforeCondition.setPermission999();
            beforeCondition.createTokenAccount();
            beforeCondition.tokenAddIssueCollAddr();
        }
    }

    @Before
    public void beforeConfig() throws Exception {

        subLedger="";
        String respWithHash ="";
        mgToolCmd.setPeerPerm(PEER1IP+":"+PEER1RPCPort,utilsClass.getSDKID(),"999");
        String resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        if(! resp.contains("\"name\": \""+glbChain01.toLowerCase()+"\"")) {
            respWithHash = mgToolCmd.createAppChain(PEER1IP, PEER1RPCPort, " -c " + glbChain01,
                    " -t sm3", " -w first", " -c raft", ids);
            commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(respWithHash,utilsClass.mgGetTxHashType),
                    utilsClass.tokenApiGetTxDetailTType,SLEEPTIME*2);

            assertEquals(mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"").contains("\"name\": \""+glbChain01.toLowerCase()+"\""), true);
        }

        if(! resp.contains("\"name\": \""+glbChain02.toLowerCase()+"\"")) {
            respWithHash = mgToolCmd.createAppChain(PEER1IP, PEER1RPCPort, " -c " + glbChain02,
                    " -t sm3", " -w first", " -c raft", ids);
            commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(respWithHash,utilsClass.mgGetTxHashType),
                    utilsClass.tokenApiGetTxDetailTType,SLEEPTIME*2);
            assertEquals(mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"").contains("\"name\": \""+glbChain02.toLowerCase()+"\""), true);
        }
    }


    /**
     * ??????????????????UTXO???????????????????????????????????????,?????????????????????????????????????????????
     */
    @Test
    public void TC1529_UTXOTranction() throws Exception {
        subLedger = glbChain01;
        log.info(subLedger);

        beforeCondition.tokenAddIssueCollAddr();
        log.info("??????1???????????????????????????");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        log.info("??????1?????? tokentype");
        token_issue();

        log.info("???????????????????????????token??????"+ tokenType + " " + tokenType2);
        subLedger = "";
        String response1 = tokenModule.tokenGetBalance(tokenMultiAddr1, "");
        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
        assertEquals(false,response1.contains(tokenType));
        assertEquals(false,response1.contains(tokenType2));

        log.info("????????????2????????????????????????token??????");
        subLedger = glbChain02;
        String response21 = tokenModule.tokenGetBalance(tokenMultiAddr1, "");
        assertEquals("200",JSONObject.fromObject(response21).getString("state"));
        assertEquals(false,response21.contains(tokenType));
        assertEquals(false,response21.contains(tokenType2));


        log.info("??????1?????????");
        subLedger = glbChain01;
        Transfer();


        log.info("?????????????????????????????????????????????");
        subLedger = "";
        String response2 = tokenModule.tokenGetBalance(tokenMultiAddr2, "");
        assertEquals("200",JSONObject.fromObject(response2).getString("state"));
        assertEquals(false,response2.contains(tokenType));
        assertEquals(false,response2.contains(tokenType2));


        log.info("??????2???????????????????????????????????????");
        subLedger = glbChain02;
        String response22 = tokenModule.tokenGetBalance(tokenMultiAddr2, "");
        assertEquals("200",JSONObject.fromObject(response22).getString("state"));
        assertEquals(false,response22.contains(tokenType));
        assertEquals(false,response22.contains(tokenType2));


        //?????????????????????tokentype
        log.info("????????????????????????1?????????tokentype");
        subLedger = "";
        tokenModule.tokenIssue(tokenMultiAddr1,tokenMultiAddr1,tokenType,"9","???????????????tokentype");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        String response3 = tokenModule.tokenGetBalance(tokenMultiAddr1, "");
        assertEquals("200",JSONObject.fromObject(response3).getString("state"));
        assertEquals("9",JSONObject.fromObject(response3).getJSONObject("data").getString(tokenType));
        assertEquals(false,response3.contains(tokenType2));

        //??????2???????????????tokentype2
        log.info("??????2???????????????1?????????tokentype2");
        subLedger = glbChain02;
        beforeCondition.tokenAddIssueCollAddr();
        log.info("?????????????????????????????????");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        tokenModule.tokenIssue(tokenMultiAddr1,tokenMultiAddr1,tokenType2,"9","???????????????tokentype2");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        String response23 = tokenModule.tokenGetBalance(tokenMultiAddr1, "");
        assertEquals("200",JSONObject.fromObject(response23).getString("state"));
        assertEquals("9",JSONObject.fromObject(response23).getJSONObject("data").getString(tokenType2));
        assertEquals(false,response23.contains(tokenType));

        log.info("???????????????tokenType???tokenType2??????????????????token");
        subLedger = glbChain01;
        Recycle_query_freeze();

        log.info("???????????????tokenType?????????????????????");
        subLedger = "";
        String tranfMain = commonFunc.tokenModule_TransferToken(tokenMultiAddr1,tokenMultiAddr2,tokenType,"3");
        assertEquals("200",JSONObject.fromObject(tranfMain).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        String response4 = tokenModule.tokenGetBalance(tokenMultiAddr1, "");
        assertEquals("200",JSONObject.fromObject(response4).getString("state"));
        assertEquals("6",JSONObject.fromObject(response4).getJSONObject("data").getString(tokenType));

        String response5 = tokenModule.tokenGetBalance(tokenMultiAddr2, "");
        assertEquals("200",JSONObject.fromObject(response5).getString("state"));
        assertEquals("3",JSONObject.fromObject(response5).getJSONObject("data").getString(tokenType));


        log.info("??????2?????????tokenType2?????????????????????");
        subLedger = glbChain02;
        String tranfMain2 = commonFunc.tokenModule_TransferToken(tokenMultiAddr1,tokenMultiAddr2,tokenType2,"3");
        assertEquals("200",JSONObject.fromObject(tranfMain2).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        String response24 = tokenModule.tokenGetBalance(tokenMultiAddr1, "");
        assertEquals("200",JSONObject.fromObject(response24).getString("state"));
        assertEquals("6",JSONObject.fromObject(response24).getJSONObject("data").getString(tokenType2));

        String response25 = tokenModule.tokenGetBalance(tokenMultiAddr2, "");
        assertEquals("200",JSONObject.fromObject(response25).getString("state"));
        assertEquals("3",JSONObject.fromObject(response25).getJSONObject("data").getString(tokenType2));


        log.info("??????1???????????????1???2token type ??????");
        subLedger = glbChain01;
        String response6 = tokenModule.tokenGetBalance(tokenMultiAddr1, "");
        assertEquals("200",JSONObject.fromObject(response6).getString("state"));
        assertEquals("990",JSONObject.fromObject(response6).getJSONObject("data").getString(tokenType));
        assertEquals("990",JSONObject.fromObject(response6).getJSONObject("data").getString(tokenType2));

        String response7 = tokenModule.tokenGetBalance(tokenMultiAddr2, "");
        assertEquals("200",JSONObject.fromObject(response7).getString("state"));
        assertEquals(false,response7.contains(tokenType));
        assertEquals(false,response7.contains(tokenType2));

        log.info("??????1???????????????????????????");
        String response8 = tokenModule.tokenGetDestroyBalance();
        assertEquals("200",JSONObject.fromObject(response8).getString("state"));
        assertEquals("10",JSONObject.fromObject(response8).getJSONObject("data").getString(tokenType));
        assertEquals("10",JSONObject.fromObject(response8).getJSONObject("data").getString(tokenType2));

        log.info("?????????????????????2???????????????????????????");
        subLedger = "";
        String response81 = tokenModule.tokenGetDestroyBalance();
        assertEquals("200",JSONObject.fromObject(response81).getString("state"));
        assertEquals(false,response81.contains(tokenType));
        assertEquals(false,response81.contains(tokenType2));

        subLedger = glbChain02;
        String response82 = tokenModule.tokenGetDestroyBalance();
        assertEquals("200",JSONObject.fromObject(response82).getString("state"));
        assertEquals(false,response82.contains(tokenType));
        assertEquals(false,response82.contains(tokenType2));
    }

    /**
     * ??????????????????UTXO???????????????????????????????????????,??????????????????????????????
     * @throws Exception
     */
    @Test
    public void TC1532_UTXOTranction()throws Exception{
        subLedger = "";
        log.info(subLedger);
        log.info("???????????? tokentype");
        token_issue();

        log.info("??????1?????????????????????token??????");
        subLedger = glbChain01;
        String response1 = tokenModule.tokenGetBalance(tokenMultiAddr1, "");
        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
        assertEquals(false,response1.contains(tokenType));
        assertEquals(false,response1.contains(tokenType2));

        log.info("????????????2????????????????????????token??????");
        subLedger = glbChain02;
        String response21 = tokenModule.tokenGetBalance(tokenMultiAddr1, "");
        assertEquals("200",JSONObject.fromObject(response21).getString("state"));
        assertEquals(false,response21.contains(tokenType));
        assertEquals(false,response21.contains(tokenType2));


        log.info("???????????????");
        subLedger = "";
        Transfer();


        log.info("??????1???????????????????????????????????????");
        subLedger = glbChain01;
        String response2 = tokenModule.tokenGetBalance(tokenMultiAddr2, "");
        assertEquals("200",JSONObject.fromObject(response2).getString("state"));
        assertEquals(false,response2.contains(tokenType));
        assertEquals(false,response2.contains(tokenType2));


        log.info("??????2???????????????????????????????????????");
        subLedger = glbChain02;
        String response22 = tokenModule.tokenGetBalance(tokenMultiAddr2, "");
        assertEquals("200",JSONObject.fromObject(response22).getString("state"));
        assertEquals(false,response22.contains(tokenType));
        assertEquals(false,response22.contains(tokenType2));


        log.info("??????1???????????????????????????tokentype");
        subLedger = glbChain01;

        beforeCondition.tokenAddIssueCollAddr();
        log.info("?????????????????????????????????");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        tokenModule.tokenIssue(tokenMultiAddr1,tokenMultiAddr1,tokenType,"9","??????1?????????tokentype");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        String response3 = tokenModule.tokenGetBalance(tokenMultiAddr1, "");
        assertEquals("200",JSONObject.fromObject(response3).getString("state"));
        assertEquals("9",JSONObject.fromObject(response3).getJSONObject("data").getString(tokenType));
        assertEquals(false,response3.contains(tokenType2));

        //??????2???????????????tokentype2
        log.info("??????2???????????????1?????????tokentype2");
        subLedger = glbChain02;
        beforeCondition.tokenAddIssueCollAddr();
        log.info("??????2???????????????????????????");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        tokenModule.tokenIssue(tokenMultiAddr1,tokenMultiAddr1,tokenType2,"9","???????????????tokentype2");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        String response23 = tokenModule.tokenGetBalance(tokenMultiAddr1, "");
        assertEquals("200",JSONObject.fromObject(response23).getString("state"));
        assertEquals("9",JSONObject.fromObject(response23).getJSONObject("data").getString(tokenType2));
        assertEquals(false,response23.contains(tokenType));

        log.info("???????????????tokenType???tokenType2??????????????????token");
        subLedger = "";
        Recycle_query_freeze();

        log.info("??????1?????????tokenType?????????????????????");
        subLedger = glbChain01;
        String tranfMain = commonFunc.tokenModule_TransferToken(tokenMultiAddr1,tokenMultiAddr2,tokenType,"3");
        assertEquals("200",JSONObject.fromObject(tranfMain).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        String response4 = tokenModule.tokenGetBalance(tokenMultiAddr1, "");
        assertEquals("200",JSONObject.fromObject(response4).getString("state"));
        assertEquals("6",JSONObject.fromObject(response4).getJSONObject("data").getString(tokenType));

        String response5 = tokenModule.tokenGetBalance(tokenMultiAddr2, "");
        assertEquals("200",JSONObject.fromObject(response5).getString("state"));
        assertEquals("3",JSONObject.fromObject(response5).getJSONObject("data").getString(tokenType));


        log.info("??????2?????????tokenType2?????????????????????");
        subLedger = glbChain02;
        String tranfMain2 = commonFunc.tokenModule_TransferToken(tokenMultiAddr1,tokenMultiAddr2,tokenType2,"3");
        assertEquals("200",JSONObject.fromObject(tranfMain2).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        String response24 = tokenModule.tokenGetBalance(tokenMultiAddr1, "");
        assertEquals("200",JSONObject.fromObject(response24).getString("state"));
        assertEquals("6",JSONObject.fromObject(response24).getJSONObject("data").getString(tokenType2));

        String response25 = tokenModule.tokenGetBalance(tokenMultiAddr2, "");
        assertEquals("200",JSONObject.fromObject(response25).getString("state"));
        assertEquals("3",JSONObject.fromObject(response25).getJSONObject("data").getString(tokenType2));


        log.info("?????????????????????1???2token type ??????");
        subLedger = "";
        String response6 = tokenModule.tokenGetBalance(tokenMultiAddr1, "");
        assertEquals("200",JSONObject.fromObject(response6).getString("state"));
        assertEquals("990",JSONObject.fromObject(response6).getJSONObject("data").getString(tokenType));
        assertEquals("990",JSONObject.fromObject(response6).getJSONObject("data").getString(tokenType2));

        String response7 = tokenModule.tokenGetBalance(tokenMultiAddr2, "");
        assertEquals("200",JSONObject.fromObject(response7).getString("state"));
        assertEquals(false,response7.contains(tokenType));
        assertEquals(false,response7.contains(tokenType2));

        log.info("?????????????????????????????????");
        String response8 = tokenModule.tokenGetDestroyBalance();
        assertEquals("200",JSONObject.fromObject(response8).getString("state"));
        assertEquals("10",JSONObject.fromObject(response8).getJSONObject("data").getString(tokenType));
        assertEquals("10",JSONObject.fromObject(response8).getJSONObject("data").getString(tokenType2));

        log.info("????????????1?????????2???????????????????????????");
        subLedger = glbChain01;
        String response81 = tokenModule.tokenGetDestroyBalance();
        assertEquals("200",JSONObject.fromObject(response81).getString("state"));
        assertEquals(false,response81.contains(tokenType));
        assertEquals(false,response81.contains(tokenType2));

        subLedger = glbChain02;
        String response82 = tokenModule.tokenGetDestroyBalance();
        assertEquals("200",JSONObject.fromObject(response82).getString("state"));
        assertEquals(false,response82.contains(tokenType));
        assertEquals(false,response82.contains(tokenType2));
    }


    /**
     * token??????
     */
    public void token_issue()throws Exception{

        SDKADD = TOKENADD;
        issueAmount1 = "1000";
        issueAmount2 = "1000";


        log.info("??????????????????token");
        tokenType = commonFunc.tokenModule_IssueToken(tokenMultiAddr1,tokenMultiAddr1,issueAmount1);
        tokenType2 = commonFunc.tokenModule_IssueToken(tokenMultiAddr1,tokenMultiAddr1,issueAmount2);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        log.info("???????????????????????????token??????");

        String response1 = tokenModule.tokenGetBalance(tokenMultiAddr1, "");
        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
        assertEquals(issueAmount1,JSONObject.fromObject(response1).getJSONObject("data").getString(tokenType));
        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
        assertEquals(issueAmount2,JSONObject.fromObject(response1).getJSONObject("data").getString(tokenType2));

    }

    /**
     * ?????? ?????? ????????????
     */
    public void Recycle_query_freeze()throws Exception{
        log.info("??????tokenMultiAddr1");
        String recycleInfo = commonFunc.tokenModule_DestoryToken(tokenMultiAddr2,tokenType,"10");
        String recycleInfo2 = commonFunc.tokenModule_DestoryToken(tokenMultiAddr2, tokenType2, "10");
        assertEquals("200", JSONObject.fromObject(recycleInfo).getString("state"));
        assertEquals("200", JSONObject.fromObject(recycleInfo2).getString("state"));


        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        log.info("????????????????????????????????????0");
        String response2 = tokenModule.tokenGetBalance(tokenMultiAddr2, "");
        assertEquals("200",JSONObject.fromObject(response2).getString("state"));
        assertEquals(false,response2.contains(tokenType));
        assertEquals(false,response2.contains(tokenType2));

        log.info("??????token");
        String freezeToken = tokenModule.tokenFreezeToken(tokenType);
        String freezeToken2 = tokenModule.tokenFreezeToken(tokenType2);
        assertThat(freezeToken, containsString("200"));
        assertThat(freezeToken2, containsString("200"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

    }

    /**
     * ????????????
     * @throws Exception
     */

    public void Transfer()throws Exception{
        //??????
        log.info("?????????????????????????????????token????????????token");
        List<Map> list = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType,"10");
        List<Map> list2 = utilsClass.tokenConstructToken(tokenMultiAddr2,tokenType2,"10",list);
        String transferInfo= commonFunc.tokenModule_TransferTokenList(tokenMultiAddr1,list2);
        assertEquals("200",JSONObject.fromObject(transferInfo).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        log.info("?????????????????????MULITADD4?????????????????????????????????");
        String queryInfo = tokenModule.tokenGetBalance(tokenMultiAddr2,"");
        String queryInfo2 = tokenModule.tokenGetBalance(tokenMultiAddr1,"");
        assertEquals("200",JSONObject.fromObject(queryInfo).getString("state"));
        assertEquals("10",JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType));
        assertEquals("10",JSONObject.fromObject(queryInfo).getJSONObject("data").getString(tokenType2));
    }

}
