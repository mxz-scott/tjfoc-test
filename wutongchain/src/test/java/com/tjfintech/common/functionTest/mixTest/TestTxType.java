package com.tjfintech.common.functionTest.mixTest;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.MgToolCmd;
import com.tjfintech.common.TestBuilder;

import com.tjfintech.common.functionTest.Conditions.SetAppChain;
import com.tjfintech.common.functionTest.Conditions.SetDatabaseMysql;
import com.tjfintech.common.functionTest.contract.DockerContractTest;
import com.tjfintech.common.functionTest.contract.WVMContractTest;
import com.tjfintech.common.utils.FileOperation;
import com.tjfintech.common.utils.UtilsClass;
import static com.tjfintech.common.CommonFunc.*;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.math.RandomUtils;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.*;


import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class TestTxType {

    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    MultiSign multiSign =testBuilder.getMultiSign();
    SoloSign soloSign = testBuilder.getSoloSign();
    DockerContractTest ct =new DockerContractTest();
    UtilsClass utilsClass=new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    MgToolCmd mgToolCmd = new MgToolCmd();
    String typeStore="0";
    String subTypeStore="0";
    String subTypePriStore="1";

    String versionStore="0";
    String versionPriStore="1";
    String versionWVM1="2";
    String versionSUTXO="0";
    String versionMUTXO="0";

    String typeUTXO="1";
    String subTypeIssue="10";
    String subTypeTransfer="11";
    String subTypeRecycle="12";

    String typeDocker="2";
    String subTypeCreateDocker="30";
    String subTypeDockerTx="32";
    String subTypeDeleteDocker="31";

    String typeWVM="3";
    String subTypeCreateWVM="40";
    String subTypeWVMTx="42";
    String subTypeDeleteWVM="41";

    String typeAdmin="20";
    String subTypeAddColl="200";
    String subTypeDelColl="201";
    String subTypeAddIssue="202";
    String subTypeDelIssue="203";
    String subTypeFreezeToken="204";
    String subTypeRecoverToken="205";

    String typeSystem = "4";
    String subTypePerm = "3";
    String subTypeAddPeer = "0";
    String subTypeQuitPeer = "1";
    String subTypeAddLedger = "4";
    String subTypeFreezeLedger = "5";
    String subTypeRecoverLedger = "6";
    String subTypeDestroyLedger = "7";

    String zeroAddr="0000000000000000";
    SetAppChain setSubLedger = new SetAppChain();

    String id1 = getPeerId(PEER1IP,USERNAME,PASSWD);
    String id2 = getPeerId(PEER2IP,USERNAME,PASSWD);
    String id3 = getPeerId(PEER4IP,USERNAME,PASSWD);
    String ids = " -m "+ id1+","+ id2+","+ id3;

   @Before
    public void beforeConfig() throws Exception {
       initSetting();
    }

    public void initSetting()throws Exception{
        log.info("current Ledger:" + subLedger);
        if(subLedger != "")   setSubLedger.createSubledger();
        BeforeCondition bf = new BeforeCondition();
        bf.setPermission999();
        bf.updatePubPriKey();
//        bf.collAddressTest();
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType21),
//                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
    }

    @Test
    public void checkA1SysTx() throws Exception{
        /**??????????????????
         * |System|4|
         * ### ???????????????
         * |???????????????|??????|
         * |????????????/??????|0|
         * |????????????|1|
         * |????????????|3|
         * |????????????|4|
         * |????????????|5|
         * |????????????|6|
         * |????????????|7|
         */

        MgToolCmd mgToolCmd = new MgToolCmd();
        SetDatabaseMysql setDatabaseMysql = new SetDatabaseMysql();


        //??????????????????ID
        String resp = mgToolCmd.getID(PEER1IP,ToolPATH + "crypt/key.pem","");
        String toolID = resp.substring(resp.lastIndexOf(":")+1).trim();
        assertEquals(false,toolID.isEmpty());  //??????????????????????????????????????????????????????????????????????????????
        if(subLedger == "") {
            setDatabaseMysql.setDBMysql();
            initSetting();
        }

        //??????????????? ????????????????????????
        String permResp = mgToolCmd.setPeerPerm(PEER1IP+":"+PEER1RPCPort,utilsClass.getSDKID(),"999","sdkName");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(permResp,utilsClass.mgGetTxHashType),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        String permHash = permResp.substring(permResp.lastIndexOf(":")+1).trim();
        JSONObject jsonObjectPerm = checkTXDetailHeaderMsg(permHash,versionStore,typeSystem,subTypePerm);

        assertEquals(toolID,
                jsonObjectPerm.getJSONObject("data").getJSONObject("system").getJSONObject("permissionTransaction").getString("sendId"));
        assertEquals(utilsClass.getSDKID(),
                jsonObjectPerm.getJSONObject("data").getJSONObject("system").getJSONObject("permissionTransaction").getString("peerId"));
        assertEquals("sdkName",
                jsonObjectPerm.getJSONObject("data").getJSONObject("system").getJSONObject("permissionTransaction").getString("shownName"));
        String permListStr = jsonObjectPerm.getJSONObject("data").getJSONObject("system").getJSONObject("permissionTransaction").getString("permissionList");
        assertEquals(fullPerm,permListStr.substring(permListStr.lastIndexOf(":")+1).trim().replaceAll(","," "));

        //?????????????????? ??????????????????????????????
        if(subLedger != "")  return;

        //??????????????????????????????
        String respQuit = mgToolCmd.quitPeer(PEER1IP + ":" + PEER1RPCPort, PEER2IP);
        assertEquals(true, respQuit.contains("success"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(respQuit,utilsClass.mgGetTxHashType),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        String quitPeerHash = respQuit.substring(respQuit.lastIndexOf(":") + 1).trim();
        JSONObject jsonObjectQuitPeer = checkTXDetailHeaderMsg(quitPeerHash, versionStore, typeSystem, subTypeQuitPeer);
        assertEquals(toolID,
                jsonObjectQuitPeer.getJSONObject("data").getJSONObject("system").getJSONObject("peerTransaction").getString("sendId"));
        assertEquals(getPeerId(PEER2IP, USERNAME, PASSWD),
                jsonObjectQuitPeer.getJSONObject("data").getJSONObject("system").getJSONObject("peerTransaction").getString("id"));

            //??????????????????????????????
        String respAdd = mgToolCmd.addPeer("join", PEER1IP + ":" + PEER1RPCPort,
                ipv4 + PEER2IP, tcpProtocol + PEER2TCPPort, PEER2RPCPort);
        assertEquals(true, respAdd.contains("success"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(respAdd,utilsClass.mgGetTxHashType),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        String addPeerHash = respAdd.substring(respAdd.lastIndexOf(":") + 1).trim();
        JSONObject jsonObjectAddPeer = checkTXDetailHeaderMsg(addPeerHash, versionStore, typeSystem, subTypeAddPeer);
        assertEquals(toolID,
                jsonObjectAddPeer.getJSONObject("data").getJSONObject("system").getJSONObject("peerTransaction").getString("sendId"));
        assertEquals(getPeerId(PEER2IP, USERNAME, PASSWD),
                jsonObjectAddPeer.getJSONObject("data").getJSONObject("system").getJSONObject("peerTransaction").getString("id"));
        assertEquals("peer" + PEER2IP.substring(PEER2IP.lastIndexOf(".") + 1).trim(),
                jsonObjectAddPeer.getJSONObject("data").getJSONObject("system").getJSONObject("peerTransaction").getString("shownName"));
        assertEquals(true,
                jsonObjectAddPeer.getJSONObject("data").getJSONObject("system").getJSONObject("peerTransaction").getString("lanAddrs").contains(ipv4 + PEER2IP + tcpProtocol + PEER2TCPPort));
        assertEquals(true,
                jsonObjectAddPeer.getJSONObject("data").getJSONObject("system").getJSONObject("peerTransaction").getString("wlanAddrs").contains(ipv4 + PEER2IP + tcpProtocol + PEER2TCPPort));
//        assertEquals(PEER2RPCPort,
//                jsonObjectAddPeer.getJSONObject("data").getJSONObject("system").getJSONObject("peerTransaction").getString("RpcPort"));
        assertEquals("0",
                jsonObjectAddPeer.getJSONObject("data").getJSONObject("system").getJSONObject("peerTransaction").getString("peerType"));

        //????????????????????????????????????????????? ?????????????????? ?????????????????? 20200915
//        //??????????????????
//        String chainName="tx_"+sdf.format(dt)+ RandomUtils.nextInt(10000);
//        String addLedgerResp = mgToolCmd.createSubChain(PEER1IP,PEER1RPCPort," -c "+chainName,
//                " -t sm3"," -w first"," -c raft",ids);
//        assertEquals(addLedgerResp.contains("send transaction success"), true);
//
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(addLedgerResp,utilsClass.mgGetTxHashType),
//                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//
//
//        String addLedgerHash = addLedgerResp.substring(addLedgerResp.lastIndexOf(":")+1).trim();
//        JSONObject jsonObjectAddLedger = checkTXDetailHeaderMsg(addLedgerHash,versionStore,typeSystem,subTypeAddLedger);
//
//        assertEquals(toolID,
//                jsonObjectAddLedger.getJSONObject("data").getJSONObject("system").getJSONObject("subLedgerTransaction").getString("sendId"));
//        assertEquals("0",
//                jsonObjectAddLedger.getJSONObject("data").getJSONObject("system").getJSONObject("subLedgerTransaction").getString("opType"));
//        assertEquals(chainName,
//                jsonObjectAddLedger.getJSONObject("data").getJSONObject("system").getJSONObject("subLedgerTransaction").getString("name"));
//        assertEquals("sm3",
//                jsonObjectAddLedger.getJSONObject("data").getJSONObject("system").getJSONObject("subLedgerTransaction").getString("hashType"));
//        assertEquals("first",
//                jsonObjectAddLedger.getJSONObject("data").getJSONObject("system").getJSONObject("subLedgerTransaction").getString("word"));
//        assertEquals("raft",
//                jsonObjectAddLedger.getJSONObject("data").getJSONObject("system").getJSONObject("subLedgerTransaction").getString("consensus"));
//        assertEquals("["+ids.replaceAll("-m","").replaceAll(" ","")+"]",
//                jsonObjectAddLedger.getJSONObject("data").getJSONObject("system").getJSONObject("subLedgerTransaction").getString("member").replaceAll("\"",""));
//
//        //??????????????????
//        String freezeLedgerResp = mgToolCmd.freezeSubChain(PEER1IP,PEER1RPCPort," -c "+chainName);
//        assertEquals(freezeLedgerResp.contains("send transaction success"), true);
//
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(freezeLedgerResp,utilsClass.mgGetTxHashType),
//                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//
//        String freezeLedgerHash = freezeLedgerResp.substring(freezeLedgerResp.lastIndexOf(":")+1).trim();
//        JSONObject jsonObjectFreezeLedger = checkTXDetailHeaderMsg(freezeLedgerHash,versionStore,typeSystem,subTypeFreezeLedger);
//        assertEquals(toolID,
//                jsonObjectFreezeLedger.getJSONObject("data").getJSONObject("system").getJSONObject("subLedgerTransaction").getString("sendId"));
//        assertEquals("1",
//                jsonObjectFreezeLedger.getJSONObject("data").getJSONObject("system").getJSONObject("subLedgerTransaction").getString("opType"));
//        assertEquals(chainName,
//                jsonObjectFreezeLedger.getJSONObject("data").getJSONObject("system").getJSONObject("subLedgerTransaction").getString("name"));
//        assertEquals("sm3",
//                jsonObjectFreezeLedger.getJSONObject("data").getJSONObject("system").getJSONObject("subLedgerTransaction").getString("hashType"));
//        assertEquals("first",
//                jsonObjectFreezeLedger.getJSONObject("data").getJSONObject("system").getJSONObject("subLedgerTransaction").getString("word"));
//        assertEquals("raft",
//                jsonObjectFreezeLedger.getJSONObject("data").getJSONObject("system").getJSONObject("subLedgerTransaction").getString("consensus"));
//        assertEquals("["+ids.replaceAll("-m","").replaceAll(" ","")+"]",
//                jsonObjectFreezeLedger.getJSONObject("data").getJSONObject("system").getJSONObject("subLedgerTransaction").getString("member").replaceAll("\"",""));
//
//        //????????????????????????
//
//        String recoverLedgerResp = mgToolCmd.recoverSubChain(PEER1IP,PEER1RPCPort," -c "+chainName);
//        assertEquals(recoverLedgerResp.contains("send transaction success"), true);
//
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(recoverLedgerResp,utilsClass.mgGetTxHashType),
//                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//
//        String recoverLedgerHash = recoverLedgerResp.substring(recoverLedgerResp.lastIndexOf(":")+1).trim();
//        JSONObject jsonObjectRecoverLedger = checkTXDetailHeaderMsg(recoverLedgerHash,versionStore,typeSystem,subTypeRecoverLedger);
//        assertEquals(toolID,
//                jsonObjectRecoverLedger.getJSONObject("data").getJSONObject("system").getJSONObject("subLedgerTransaction").getString("sendId"));
//        assertEquals("2",
//                jsonObjectRecoverLedger.getJSONObject("data").getJSONObject("system").getJSONObject("subLedgerTransaction").getString("opType"));
//        assertEquals(chainName,
//                jsonObjectRecoverLedger.getJSONObject("data").getJSONObject("system").getJSONObject("subLedgerTransaction").getString("name"));
//        assertEquals("sm3",
//                jsonObjectRecoverLedger.getJSONObject("data").getJSONObject("system").getJSONObject("subLedgerTransaction").getString("hashType"));
//        assertEquals("first",
//                jsonObjectRecoverLedger.getJSONObject("data").getJSONObject("system").getJSONObject("subLedgerTransaction").getString("word"));
//        assertEquals("raft",
//                jsonObjectRecoverLedger.getJSONObject("data").getJSONObject("system").getJSONObject("subLedgerTransaction").getString("consensus"));
//        assertEquals("["+ids.replaceAll("-m","").replaceAll(" ","")+"]",
//                jsonObjectRecoverLedger.getJSONObject("data").getJSONObject("system").getJSONObject("subLedgerTransaction").getString("member").replaceAll("\"",""));
//
//        //??????????????????
//        String destroyLedgerResp = mgToolCmd.destroySubChain(PEER1IP,PEER1RPCPort," -c "+chainName);
//        assertEquals(destroyLedgerResp.contains("send transaction success"), true);
//
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(destroyLedgerResp,utilsClass.mgGetTxHashType),
//                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//
//        String destroyLedgerHash = destroyLedgerResp.substring(destroyLedgerResp.lastIndexOf(":")+1).trim();
//        JSONObject jsonObjectDestroyLedger = checkTXDetailHeaderMsg(destroyLedgerHash,versionStore,typeSystem,subTypeDestroyLedger);
//        assertEquals(toolID,
//                jsonObjectDestroyLedger.getJSONObject("data").getJSONObject("system").getJSONObject("subLedgerTransaction").getString("sendId"));
//        assertEquals("3",
//                jsonObjectDestroyLedger.getJSONObject("data").getJSONObject("system").getJSONObject("subLedgerTransaction").getString("opType"));
//        assertEquals(chainName,
//                jsonObjectDestroyLedger.getJSONObject("data").getJSONObject("system").getJSONObject("subLedgerTransaction").getString("name"));
//        assertEquals("sm3",
//                jsonObjectDestroyLedger.getJSONObject("data").getJSONObject("system").getJSONObject("subLedgerTransaction").getString("hashType"));
//        assertEquals("first",
//                jsonObjectDestroyLedger.getJSONObject("data").getJSONObject("system").getJSONObject("subLedgerTransaction").getString("word"));
//        assertEquals("raft",
//                jsonObjectDestroyLedger.getJSONObject("data").getJSONObject("system").getJSONObject("subLedgerTransaction").getString("consensus"));
//        assertEquals("["+ids.replaceAll("-m","").replaceAll(" ","")+"]",
//                jsonObjectDestroyLedger.getJSONObject("data").getJSONObject("system").getJSONObject("subLedgerTransaction").getString("member").replaceAll("\"",""));
//
    }

    @Test
    public void checkStoreTx()throws Exception{

        /**|??????|0|
         * |????????????|0|
         * |????????????|1|
         * |0.9????????????|2|
         */
        //??????????????????
        String Data="TxType tx store "+sdf.format(dt)+ RandomUtils.nextInt(100000);
        log.info("?????????????????????"+Data);
        String response1=store.CreateStore(Data);

        //??????????????????
        String priData = "TxTypePri-" + UtilsClass.Random(2);
        Map<String,Object> map=new HashMap<>();
        map.put("pubKeys",PUBKEY1);
        map.put("pubkeys",PUBKEY6);
        log.info("?????????????????????"+priData);
        String response2 = store.CreatePrivateStore(priData,map);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType21),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        String txHash1 = JSONObject.fromObject(response1).getString("data");
        checkTXDetailHeaderMsg(txHash1,versionStore,typeStore,subTypeStore);
        checkStore(txHash1,Data,"store");


        //????????????????????????
        String txHash2 = JSONObject.fromObject(response2).getString("data");
        checkTXDetailHeaderMsg(txHash2,versionPriStore,typeStore,subTypePriStore);

//        JSONObject jsonObjecttran=JSONObject.fromObject(store.GetTransaction(txHash2)).getJSONObject("data");
        JSONObject jsonObjecttx=JSONObject.fromObject(store.GetTxDetail(txHash2)).getJSONObject("data");

        //????????????storeData????????????????????????????????????????????????????????????data??????
//        assertEquals(jsonObjecttran.getString("storeData").contains(priData), false);
        //assertEquals("store",jsonObjecttran.getString("transactionType"));//20190514 ?????????????????????
//        assertEquals(true,jsonObjecttran.getJSONObject("extra").isNullObject());//????????????extra

        assertEquals(jsonObjecttx.getJSONObject("store").getString("storeData").contains(priData), false);
        //assertEquals("store",jsonObjecttx.getJSONObject("store").getString("transactionType"));
        assertEquals(true,jsonObjecttx.getJSONObject("store").getJSONObject("extra").isNullObject());//????????????extra
    }

//    @Test
    public void checkUTXOTx()throws Exception{
        /**|UTXO|1|
         * |UTXO??????|10|
         * |UTXO??????|11|
         * |UTXO??????|12|
         */
        //??????0.0* ??????token
        String minToken = "TxTypeSoMin-" + UtilsClass.Random(6);
        String minData = "??????" + ADDRESS1 + "??????token " + minToken;
        String minAmount = "0.001";
        log.info(minData);

        String minResp = soloSign.issueToken(PRIKEY1,minToken,minAmount,minData,ADDRESS1);


        //UTXO????????? Type 1 SubType 10 11 12
        //????????????
        String tokenTypeS = "TxTypeSOLOTC-"+ UtilsClass.Random(6);
        String siData="??????"+ADDRESS1+"??????token "+tokenTypeS;
        String amount="10000";
        log.info(siData);
        String response3= soloSign.issueToken(PRIKEY1,tokenTypeS,amount,siData,ADDRESS1);


        //????????????
        String tokenTypeM = "TxTypeMULTIC" + UtilsClass.Random(8);
        String amount1 = "50000";
        String  mulData= "??????"+MULITADD3 + "???????????????" + tokenTypeM + " token???????????????" + amount1;
        log.info(mulData);
        String response51 = multiSign.issueToken(IMPPUTIONADD,tokenTypeM, amount1, mulData);
        assertThat(response51, containsString("200"));
        String Tx1 = JSONObject.fromObject(response51).getJSONObject("data").getString("tx");
        log.info("???????????????");
        String response52 = multiSign.Sign(Tx1, PRIKEY5);

        String tokenTypeM2 = "TxTypeMULTIC" + UtilsClass.Random(8);
        String amount12 = "50000";
        String  mulData2= "??????"+MULITADD3 + "?????????"+MULITADD7 +" "+ tokenTypeM2 + " token???????????????" + amount12;
        log.info(mulData2);
        String response53 = multiSign.issueToken(IMPPUTIONADD,MULITADD7,tokenTypeM2,amount12,mulData2);
        String Tx13 = JSONObject.fromObject(response53).getJSONObject("data").getString("tx");
        log.info("???????????????");
        String response54 = multiSign.Sign(Tx13, PRIKEY5);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //????????????
        assertEquals(JSONObject.fromObject(soloSign.BalanceByAddr(ADDRESS1,tokenTypeS)).getJSONObject("data").getString("total"),amount);
        String amountTransfer="0.01";
        List<Map> list=soloSign.constructToken(ADDRESS3,tokenTypeS,amountTransfer);
        String tranferdata="transfer to "+ADDRESS3+" with amount "+amountTransfer;
        String response4= soloSign.Transfer(list,PRIKEY1,tranferdata);

        //????????????
        assertEquals(JSONObject.fromObject( multiSign.BalanceByAddr(IMPPUTIONADD,tokenTypeM)).getJSONObject("data").getString("total"),amount1);
        String tranferAmount="3000";
        String transferData = IMPPUTIONADD+" ??? " + ADDRESS5 + " ?????? " + tranferAmount + " " +tokenTypeM;
        List<Map> listInit = utilsClass.constructToken(ADDRESS5, tokenTypeM, "3000");
        log.info(transferData);
        String response6 = multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD, listInit);


        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType02),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //???????????????????????????????????????
        String txHashMin = JSONObject.fromObject(minResp).getString("data");
        checkTXDetailHeaderMsg(txHashMin,versionSUTXO,typeUTXO,subTypeIssue);
        JSONObject uxtoJsonMin= JSONObject.fromObject(JSONObject.fromObject(store.GetTxDetail(txHashMin)).getJSONObject("data").getJSONObject("utxo"));
        assertEquals(minData,uxtoJsonMin.getString("data"));
        assertEquals(1,uxtoJsonMin.getJSONArray("txRecords").size());
        checkFromTo(uxtoJsonMin,ADDRESS1,ADDRESS1,minToken,minAmount,0);


        //??????????????????????????????
        String txHash3 = JSONObject.fromObject(response3).getString("data");
        checkTXDetailHeaderMsg(txHash3,versionSUTXO,typeUTXO,subTypeIssue);
        JSONObject uxtoJson= JSONObject.fromObject(JSONObject.fromObject(store.GetTxDetail(txHash3)).getJSONObject("data").getJSONObject("utxo"));
        assertEquals(siData,uxtoJson.getString("data"));
        assertEquals(1,uxtoJson.getJSONArray("txRecords").size());
        checkFromTo(uxtoJson,ADDRESS1,ADDRESS1,tokenTypeS,amount,0);

        //??????????????????????????????
        String txHash4 = JSONObject.fromObject(response4).getString("data");
        checkTXDetailHeaderMsg(txHash4,versionSUTXO,typeUTXO,subTypeTransfer);
        uxtoJson= JSONObject.fromObject(JSONObject.fromObject(store.GetTxDetail(txHash4)).getJSONObject("data").getJSONObject("utxo"));
        assertEquals(tranferdata,uxtoJson.getString("data"));

        List<Map> listST = commonFunc.constructUTXOTxDetailList(ADDRESS1,ADDRESS3,tokenTypeS,amountTransfer);
        List<Map> listST2 = commonFunc.constructUTXOTxDetailList(ADDRESS1,ADDRESS1,tokenTypeS,"9999.99",listST);

        JSONArray jsonArrayCheck = uxtoJson.getJSONArray("txRecords");
        commonFunc.checkListArray(listST2,jsonArrayCheck);


        //??????????????????????????????
        String txHash5 = JSONObject.fromObject(response52).getJSONObject("data").get("txId").toString();
        checkTXDetailHeaderMsg(txHash5,versionMUTXO,typeUTXO,subTypeIssue);
        uxtoJson= JSONObject.fromObject(JSONObject.fromObject(store.GetTxDetail(txHash5)).getJSONObject("data").getJSONObject("utxo"));
        assertEquals(mulData,uxtoJson.getString("data"));
        assertEquals(1,uxtoJson.getJSONArray("txRecords").size());
        checkFromTo(uxtoJson,IMPPUTIONADD,IMPPUTIONADD,tokenTypeM,amount1,0);

        String txHash51 = JSONObject.fromObject(response54).getJSONObject("data").get("txId").toString();
        checkTXDetailHeaderMsg(txHash51,versionMUTXO,typeUTXO,subTypeIssue);
        uxtoJson= JSONObject.fromObject(JSONObject.fromObject(store.GetTxDetail(txHash51)).getJSONObject("data").getJSONObject("utxo"));
        assertEquals(mulData2,uxtoJson.getString("data"));
        assertEquals(1,uxtoJson.getJSONArray("txRecords").size());
        checkFromTo(uxtoJson,IMPPUTIONADD,MULITADD7,tokenTypeM2,amount12,0);


        //??????????????????????????????
        String txHash6 = JSONObject.fromObject(response6).getJSONObject("data").get("txId").toString();
        checkTXDetailHeaderMsg(txHash6,versionMUTXO,typeUTXO,subTypeTransfer);
        uxtoJson= JSONObject.fromObject(JSONObject.fromObject(store.GetTxDetail(txHash6)).getJSONObject("data").getJSONObject("utxo"));
        assertEquals(transferData,uxtoJson.getString("data"));
        List<Map> listMT = commonFunc.constructUTXOTxDetailList(IMPPUTIONADD,ADDRESS5,tokenTypeM,tranferAmount);
        List<Map> listMT2 = constructUTXOTxDetailList(IMPPUTIONADD,IMPPUTIONADD,tokenTypeM,String.valueOf(Integer.parseInt(amount1)-Integer.parseInt(tranferAmount)),listMT);

        jsonArrayCheck = uxtoJson.getJSONArray("txRecords");
        commonFunc.checkListArray(listMT2,jsonArrayCheck);

        //????????????
        String recySoloAmount="600.05";
        log.info("????????????");
        log.info(soloSign.BalanceByAddr(ADDRESS1,tokenTypeS));
        String RecycleSoloInfo = soloSign.Recycle( PRIKEY1, tokenTypeS, recySoloAmount);


        //????????????
        log.info("????????????");
        log.info(multiSign.BalanceByAddr(IMPPUTIONADD,tokenTypeM));
        String recyMultiAmount="0.07";
        String RecycleMultiInfo = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenTypeM, recyMultiAmount);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType02),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //??????????????????????????????
        String txHash7 = JSONObject.fromObject(RecycleSoloInfo).getString("data");
        checkTXDetailHeaderMsg(txHash7,versionMUTXO,typeUTXO,subTypeRecycle);
        uxtoJson.clear();
        log.info("****************");
        uxtoJson= JSONObject.fromObject(JSONObject.fromObject(store.GetTxDetail(txHash7)).getJSONObject("data").getJSONObject("utxo"));

        List<Map> listSD = commonFunc.constructUTXOTxDetailList(ADDRESS1,zeroAddr,tokenTypeS,recySoloAmount);
        List<Map> listSD2 = commonFunc.constructUTXOTxDetailList(ADDRESS1,ADDRESS1,tokenTypeS,"9399.94",listSD);
        jsonArrayCheck = uxtoJson.getJSONArray("txRecords");
        commonFunc.checkListArray(listSD2,jsonArrayCheck);

        //??????????????????????????????
        String txHash8 = JSONObject.fromObject(RecycleMultiInfo).getJSONObject("data").get("txId").toString();
        checkTXDetailHeaderMsg(txHash8,versionMUTXO,typeUTXO,subTypeRecycle);
        uxtoJson= JSONObject.fromObject(JSONObject.fromObject(store.GetTxDetail(txHash8)).getJSONObject("data").getJSONObject("utxo"));

        List<Map> listMD = commonFunc.constructUTXOTxDetailList(IMPPUTIONADD,zeroAddr,tokenTypeM,"0.07");
        List<Map> listMD2 = commonFunc.constructUTXOTxDetailList(IMPPUTIONADD,IMPPUTIONADD,tokenTypeM,"46999.93",listMD);
        jsonArrayCheck = uxtoJson.getJSONArray("txRecords");
        commonFunc.checkListArray(listMD2,jsonArrayCheck);
   }

    public void checkFromTo(JSONObject jsonObject,String from,String to,String TokenType,String amount,int index)throws Exception{
        assertEquals(from,jsonObject.getJSONArray("txRecords").getJSONObject(index).getString("from"));
        assertEquals(to,jsonObject.getJSONArray("txRecords").getJSONObject(index).getString("to"));
        assertEquals(TokenType,jsonObject.getJSONArray("txRecords").getJSONObject(index).getString("tokenType"));
        assertEquals(amount,jsonObject.getJSONArray("txRecords").getJSONObject(index).getString("amount"));
    }

    //20200413 ??????docker??????????????????

    @Test
    public void checkWVMTx()throws Exception{
        /**|WVM|3|
         * |WVM??????|40|
         * |WVM??????|41|
         * |WVM invoke|42|
         */
        FileOperation fileOper = new FileOperation();
        String ctName="UI_" + sdf.format(dt)+ RandomUtils.nextInt(100000);
        WVMContractTest wvm = new WVMContractTest();
        // ?????????wvm??????????????????????????????????????????????????????????????????
        // ?????????????????????????????????????????????"_temp"????????????????????????????????????????????????
        fileOper.replace(testDataPath + "wvm/" + wvm.wvmFile + ".txt", wvm.orgName, ctName);

        //??????????????????????????????hash??????Prikey???ctName??????????????????
        String response1 = wvm.wvmInstallTest( wvm.wvmFile +"_temp.txt",PRIKEY1);
        log.info("Install Pri:"+PRIKEY1);
        String txHash1 = JSONObject.fromObject(response1).getJSONObject("data").getString("txId");
        String ctHash = JSONObject.fromObject(response1).getJSONObject("data").getString("name");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        //????????????????????????
        String response2 = wvm.invokeNew(ctHash,"initAccount",wvm.accountA,wvm.amountA);//???????????????A ????????????50
        String txHash2 = JSONObject.fromObject(response2).getJSONObject("data").getString("txId");

        String response3 = wvm.invokeNew(ctHash,"initAccount",wvm.accountB,wvm.amountB);//???????????????B ????????????60
        String txHash3 = JSONObject.fromObject(response3).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        String response4 = wvm.invokeNew(ctHash,"transfer",wvm.accountA,wvm.accountB,wvm.transfer);//A???B???30
        String txHash4 = JSONObject.fromObject(response4).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //?????????????????????????????????success??????????????????
        //????????????invoke??????
        String response5 = wvm.invokeNew(ctHash,"BalanceTest",wvm.accountA);//????????????A????????????
        String txHash5 = JSONObject.fromObject(response5).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //??????wvm??????
        String response9 = wvm.wvmDestroyTest(ctHash);
        String txHash9 = JSONObject.fromObject(response9).getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //?????????????????? ??????Type???SubType??????
        JSONObject jsonObjectCreate = checkTXDetailHeaderMsg(txHash1,versionWVM1,typeWVM,subTypeCreateWVM);
        JSONObject jsonObjectInvokeInit = checkTXDetailHeaderMsg(txHash2,versionWVM1,typeWVM,subTypeWVMTx);
        JSONObject jsonObjectInvokeTransfer = checkTXDetailHeaderMsg(txHash4,versionWVM1,typeWVM,subTypeWVMTx);
        JSONObject jsonObjectDestroy = checkTXDetailHeaderMsg(txHash9,versionWVM1,typeWVM,subTypeDeleteWVM);

        //???????????????????????????????????????
        String data = utilsClass.encryptBASE64(utilsClass.readInput(
                testDataPath + "wvm/" + "wvm_temp.txt").toString().trim().getBytes()).replaceAll("\r\n", "");
        //????????????????????????????????????????????????
//        assertEquals(ctHash,
//                jsonObjectCreate.getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getString("name"));
        //20200703 ??????????????????owner??????????????????
//        //??????owner????????????PubKey ?????????????????????????????????????????? ????????????????????????pubkey
//        String p1 = shExeAndReturn(utilsClass.getIPFromStr(SDKADD),"cat " + SDKPATH + "tls/pubkey.pem").trim();
//        String p2 = new String (utilsClass.decryptBASE64(jsonObjectCreate.getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getString("owner")));
//        assertEquals(p1.replaceAll("\n",""),p2.replaceAll("\n",""));
//        assertEquals("",
//                jsonObjectCreate.getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getString("owner"));
        assertEquals(data,
                jsonObjectCreate.getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getString("src"));
        log.info("Check create wvm tx detail complete");


        //??????invoke init?????????????????????
        assertEquals(ctHash,
                jsonObjectInvokeInit.getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getString("name"));
        assertEquals("initAccount",
                jsonObjectInvokeInit.getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getJSONObject("arg").getString("method"));
        //20200728 ?????????????????????sdk?????????id
//        assertEquals(wvm.caller,new String(utilsClass.decryptBASE64(
//                jsonObjectInvokeInit.getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getJSONObject("arg").getString("caller"))));

        String argsinit0= jsonObjectInvokeInit.getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getJSONObject("arg").getJSONArray("args").getString(0);
        String argsinit1= jsonObjectInvokeInit.getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getJSONObject("arg").getJSONArray("args").getString(1);
        assertEquals(wvm.accountA, argsinit0);
        assertEquals(Integer.toString(wvm.amountA),argsinit1);
        log.info("Check invoke init wvm tx detail complete");


        //??????invoke transfer?????????????????????
        assertEquals(ctHash,
                jsonObjectInvokeTransfer.getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getString("name"));
        assertEquals("transfer",
                jsonObjectInvokeTransfer.getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getJSONObject("arg").getString("method"));
//        assertEquals(wvm.caller,new String(utilsClass.decryptBASE64(
//                jsonObjectInvokeTransfer.getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getJSONObject("arg").getString("caller"))));

        String argstrf0= jsonObjectInvokeTransfer.getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getJSONObject("arg").getJSONArray("args").getString(0);
        String argstrf1= jsonObjectInvokeTransfer.getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getJSONObject("arg").getJSONArray("args").getString(1);
        String argstrf2= jsonObjectInvokeTransfer.getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getJSONObject("arg").getJSONArray("args").getString(2);
        assertEquals(wvm.accountA, argstrf0);
        assertEquals(wvm.accountB,argstrf1);
        assertEquals(Integer.toString(wvm.transfer),argstrf2);
        log.info("Check invoke transfer wvm tx detail complete");


        //????????????????????????????????????
        assertEquals(ctHash,
                jsonObjectDestroy.getJSONObject("data").getJSONObject("wvm").getJSONObject("wvmContractTx").getString("name"));
        log.info("Check destroy wvm tx detail complete");
    }

   // @Test
    public void checkAdminTx()throws Exception{
        String tokenType = "FreezeToken-"+ UtilsClass.Random(6);
        String respon= soloSign.issueToken(PRIKEY1,tokenType,"100","??????"+ADDRESS1+"??????token "+tokenType,ADDRESS1);

        //???????????????????????????????????????????????????????????????token???????????????????????????????????????
        assertThat(multiSign.delCollAddrs(ADDRESS6),
                anyOf(containsString("\"state\":500"),containsString("not exist")));
        assertThat(multiSign.delIssueaddrs(ADDRESS6),
                anyOf(containsString("\"state\":500"),containsString("not exist")));
//        assertThat(multiSign.recoverFrozenToken(tokenType),containsString("200"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //Admin????????? Type 20 SubType 200 201 202 203
        String response10= multiSign.addCollAddrs(ADDRESS6);
        String response11= multiSign.addIssueAddrs(ADDRESS6);
        String response3=multiSign.freezeToken(tokenType);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //????????????????????????????????????
        String txHash10 = JSONObject.fromObject(response10).getString("data");
        checkTXDetailHeaderMsg(txHash10,versionStore,typeAdmin,subTypeAddColl);
        checkAdmin(txHash10,"collAddress","collAddress",ADDRESS6,"admin");

        //????????????????????????????????????
        String txHash11 = JSONObject.fromObject(response11).getString("data");
        checkTXDetailHeaderMsg(txHash11,versionStore,typeAdmin,subTypeAddIssue);
        checkAdmin(txHash11,"issueAddress","issueAddress",ADDRESS6,"admin");

        //??????token??????????????????
        String txHash31 = JSONObject.fromObject(response3).getString("data");
        checkTXDetailHeaderMsg(txHash31,versionStore,typeAdmin,subTypeFreezeToken);
        checkAdmin2(txHash31,"freezeToken",tokenType,"admin");

        //??????????????????
        String response12= multiSign.delCollAddrs(ADDRESS6);
        //??????????????????
        String response13= multiSign.delIssueaddrs(ADDRESS6);
        //????????????token
        String response4=multiSign.recoverFrozenToken(tokenType);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //????????????????????????????????????
        String txHash12 = JSONObject.fromObject(response12).getString("data");
        checkTXDetailHeaderMsg(txHash12,versionStore,typeAdmin,subTypeDelColl);
        checkAdmin(txHash12,"collAddress","collAddress",ADDRESS6,"admin");

        //????????????????????????????????????
        String txHash13 = JSONObject.fromObject(response13).getString("data");
        checkTXDetailHeaderMsg(txHash13,versionStore,typeAdmin,subTypeDelIssue);
        checkAdmin(txHash13,"issueAddress","issueAddress",ADDRESS6,"admin");

        //????????????token
        String txHash41 = JSONObject.fromObject(response4).getString("data");
        checkTXDetailHeaderMsg(txHash41,versionStore,typeAdmin,subTypeRecoverToken);
        checkAdmin2(txHash41,"recoverToken",tokenType,"admin");

    }

    public JSONObject checkTXDetailHeaderMsg(String hash, String version, String type, String subType)throws Exception{
        log.info("hash:"+hash);
        JSONObject objectDetail = JSONObject.fromObject(store.GetTxDetail(hash));
        JSONObject jsonObject = objectDetail.getJSONObject("data").getJSONObject("header");
        assertEquals(version,jsonObject.getString("version"));
        assertEquals(type,jsonObject.getString("type"));
        assertEquals(subType,jsonObject.getString("subType"));
        assertEquals(hash,jsonObject.getString("transactionId"));

        return objectDetail;
    }


    public void checkStore(String hash,String storeData,String transactionType)throws Exception{
        JSONObject jsonObjectOrg =JSONObject.fromObject(store.GetTxDetail(hash)).getJSONObject("data");
        JSONObject jsonObject =jsonObjectOrg.getJSONObject("store");
        assertEquals(storeData,jsonObject.getString("storeData"));//??????????????????
        //assertEquals(transactionType,jsonObject.getString("transactionType"));//??????????????????
        assertEquals(true,jsonObject.getJSONObject("extra").isNullObject());//??????extra

        //????????????????????????

        assertEquals(true,jsonObjectOrg.getJSONObject("Contract").isNullObject());
        assertEquals(true,jsonObjectOrg.getJSONObject("Store9").isNullObject());
        assertEquals(true,jsonObjectOrg.getJSONObject("utxo").isNullObject());
        assertEquals(true,jsonObjectOrg.getJSONObject("admin").isNullObject());
        assertEquals(true,jsonObjectOrg.getJSONObject("wvm").isNullObject());

    }

    public void checkAdmin(String hash,String keywordTran,String keywordTxdetail,String checkstr,String txType)throws Exception{
        JSONObject jsonObjectOrg2 =JSONObject.fromObject(store.GetTxDetail(hash)).getJSONObject("data");
        assertThat(jsonObjectOrg2.getJSONObject("admin").getJSONArray(keywordTxdetail).getString(0),containsString(checkstr));
        assertEquals(true,jsonObjectOrg2.getJSONObject("admin").getJSONObject("extra").isNullObject());//??????extra

        //????????????????????????
        assertEquals(true,jsonObjectOrg2.getJSONObject("Contract").isNullObject());
        assertEquals(true,jsonObjectOrg2.getJSONObject("Store9").isNullObject());
        assertEquals(true,jsonObjectOrg2.getJSONObject("utxo").isNullObject());
        assertEquals(true,jsonObjectOrg2.getJSONObject("store").isNullObject());
        assertEquals(true,jsonObjectOrg2.getJSONObject("wvm").isNullObject());
    }

    public void checkAdmin2(String hash,String keyword,String checkstr,String txType)throws Exception{
        JSONObject jsonObjectOrg2 =JSONObject.fromObject(store.GetTxDetail(hash)).getJSONObject("data");
        assertThat(jsonObjectOrg2.getJSONObject("admin").getString(keyword),containsString(checkstr));
        assertEquals(true,jsonObjectOrg2.getJSONObject("admin").getJSONObject("extra").isNullObject());//??????extra

    }
}
