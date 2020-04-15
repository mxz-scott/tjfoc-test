package com.tjfintech.common.functionTest.tokenModuleTest;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.Interface.Token;
import com.tjfintech.common.MgToolCmd;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.SubLedgerCmd;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.HashMap;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class TokenTestMultiSubChain_Store {

    TestBuilder testBuilder= TestBuilder.getInstance();
    Token tokenModule =testBuilder.getToken();

    SubLedgerCmd subLedgerCmd = new SubLedgerCmd();
    MgToolCmd mgToolCmd = new MgToolCmd();
    CommonFunc commonFunc = new CommonFunc();
    UtilsClass utilsClass = new UtilsClass();


    String glbChain01= "glbCh1";
    String glbChain02= "glbCh2";
    String noPerm="not found";

    @BeforeClass
    public static void init()throws Exception
    {
        SDKADD = TOKENADD;
        BeforeCondition beforeCondition = new BeforeCondition();
        beforeCondition.setPermission999();
        beforeCondition.createTokenAccount();
    }

    @Before
    public void beforeConfig() throws Exception {
        subLedger="";
        String respWithHash ="";
        mgToolCmd.setPeerPerm(PEER1IP+":"+PEER1RPCPort,utilsClass.getSDKID(),"999");
        String resp = mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"");
        if(! resp.contains("\"name\": \""+glbChain01+"\"")) {
            respWithHash = mgToolCmd.createSubChain(PEER1IP, PEER1RPCPort, " -z " + glbChain01,
                    " -t sm3", " -w first", " -c raft", ids);
            commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(respWithHash,utilsClass.mgGetTxHashType),
                    utilsClass.sdkGetTxDetailType,SLEEPTIME*2);

            assertEquals(mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"").contains("\"name\": \""+glbChain01+"\""), true);
        }

        if(! resp.contains("\"name\": \""+glbChain02+"\"")) {
            respWithHash = mgToolCmd.createSubChain(PEER1IP, PEER1RPCPort, " -z " + glbChain02,
                    " -t sm3", " -w first", " -c raft", ids);
            commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(respWithHash,utilsClass.mgGetTxHashType),
                    utilsClass.sdkGetTxDetailType,SLEEPTIME*2);
            assertEquals(mgToolCmd.getSubChain(PEER1IP,PEER1RPCPort,"").contains("\"name\": \""+glbChain02+"\""), true);
        }
    }

    //主链发送交易 子链检查无此交易
    @Test
    public void TC_MainSubCreateCheck01()throws Exception{
        //主链上执行存证和隐私存证 子链上查询
        Map<String,Object> map=new HashMap<>();
        map.put("address1",tokenAccount1);
        map.put("address2",tokenAccount2);

        subLedger = "";
        String Data="main tx token Store "+sdf.format(dt)+ RandomUtils.nextInt(1000);
        String response = tokenModule.tokenCreateStore(Data);
        String response1 = tokenModule.tokenCreatePrivateStore(Data,map);

        commonFunc.sdkCheckTxOrSleep(
                commonFunc.getTxHash(response,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        commonFunc.sdkCheckTxOrSleep(
                commonFunc.getTxHash(response1,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        //确认主链交易上链
        assertEquals("200",JSONObject.fromObject(
                tokenModule.tokenGetTxDetail(
                        commonFunc.getTxHash(response,utilsClass.tokenApiGetTxHashType))).getString("state"));
        assertEquals("200",JSONObject.fromObject(
                tokenModule.tokenGetTxDetail(
                        commonFunc.getTxHash(response1,utilsClass.tokenApiGetTxHashType))).getString("state"));

        //向子链1上查询
        subLedger=glbChain01;
        assertEquals("400",JSONObject.fromObject(
                tokenModule.tokenGetTxDetail(
                        commonFunc.getTxHash(response,utilsClass.tokenApiGetTxHashType))).getString("state"));
        assertEquals("400",JSONObject.fromObject(
                tokenModule.tokenGetTxDetail(
                        commonFunc.getTxHash(response1,utilsClass.tokenApiGetTxHashType))).getString("state"));

        //向子链2上查询
        subLedger=glbChain02;
        assertEquals("400",JSONObject.fromObject(
                tokenModule.tokenGetTxDetail(
                        commonFunc.getTxHash(response,utilsClass.tokenApiGetTxHashType))).getString("state"));
        assertEquals("400",JSONObject.fromObject(
                tokenModule.tokenGetTxDetail(
                        commonFunc.getTxHash(response1,utilsClass.tokenApiGetTxHashType))).getString("state"));


    }

    //子链发送交易 主链和其他子链检查无此交易
    @Test
    public void TC_MainSubCreateCheck02()throws Exception{
        //主链上执行存证和隐私存证 子链上查询
        Map<String,Object> map=new HashMap<>();
        map.put("address1",tokenAccount1);
        map.put("address2",tokenAccount2);

        subLedger = glbChain01;
        String Data="main tx token Store "+sdf.format(dt)+ RandomUtils.nextInt(1000);
        String response = tokenModule.tokenCreateStore(Data);
        String response1 = tokenModule.tokenCreatePrivateStore(Data,map);

        commonFunc.sdkCheckTxOrSleep(
                commonFunc.getTxHash(response,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        commonFunc.sdkCheckTxOrSleep(
                commonFunc.getTxHash(response1,utilsClass.tokenApiGetTxHashType),
                utilsClass.tokenApiGetTxDetailTType,SLEEPTIME);

        //确认主链交易上链
        assertEquals("200",JSONObject.fromObject(
                tokenModule.tokenGetTxDetail(
                        commonFunc.getTxHash(response,utilsClass.tokenApiGetTxHashType))).getString("state"));
        assertEquals("200",JSONObject.fromObject(
                tokenModule.tokenGetTxDetail(
                        commonFunc.getTxHash(response1,utilsClass.tokenApiGetTxHashType))).getString("state"));

        //向子链1上查询
        subLedger = "";
        assertEquals("400",JSONObject.fromObject(
                tokenModule.tokenGetTxDetail(
                        commonFunc.getTxHash(response,utilsClass.tokenApiGetTxHashType))).getString("state"));
        assertEquals("400",JSONObject.fromObject(
                tokenModule.tokenGetTxDetail(
                        commonFunc.getTxHash(response1,utilsClass.tokenApiGetTxHashType))).getString("state"));

        //向子链2上查询
        subLedger=glbChain02;
        assertEquals("400",JSONObject.fromObject(
                tokenModule.tokenGetTxDetail(
                        commonFunc.getTxHash(response,utilsClass.tokenApiGetTxHashType))).getString("state"));
        assertEquals("400",JSONObject.fromObject(
                tokenModule.tokenGetTxDetail(
                        commonFunc.getTxHash(response1,utilsClass.tokenApiGetTxHashType))).getString("state"));


    }



}
