package com.tjfintech.common.functionTest.mixTest;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.MgToolCmd;
import com.tjfintech.common.TestBuilder;

import com.tjfintech.common.functionTest.Conditions.SetDatabaseMysql;
import com.tjfintech.common.functionTest.Conditions.SetSubLedger;
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
    SetSubLedger setSubLedger = new SetSubLedger();

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
        bf.collAddressTest();
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);
    }

    @Test
    public void checkA1SysTx() throws Exception{
        /**系统类型交易
         * |System|4|
         * ### 系统子类型
         * |交易子类型|数值|
         * |节点新增/修改|0|
         * |删除节点|1|
         * |权限交易|3|
         * |创建子链|4|
         * |冻结子链|5|
         * |解冻子链|6|
         * |销毁子链|7|
         */

        MgToolCmd mgToolCmd = new MgToolCmd();
        SetDatabaseMysql setDatabaseMysql = new SetDatabaseMysql();

        //获取管理工具ID
        String resp = mgToolCmd.getID(PEER1IP,ToolPATH + "crypt/key.pem","");
        String toolID = resp.substring(resp.lastIndexOf(":")+1).trim();
        assertEquals(false,toolID.isEmpty());  //主链才做数据库清理操作，因子链不测试节点动态变更交易
        if(subLedger == "") {
            setDatabaseMysql.setDBMysql();
            initSetting();
        }

        //给链赋权限 权限变更交易检查
        String permResp = mgToolCmd.setPeerPerm(PEER1IP+":"+PEER1RPCPort,utilsClass.getSDKID(),"999","sdkName");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(permResp,utilsClass.mgGetTxHashType),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        String permHash = permResp.substring(permResp.lastIndexOf(":")+1).trim();
        JSONObject jsonObjectPerm = checkTXDetailHeaderMsg(permHash,versionStore,typeSystem,subTypePerm);

        assertEquals(toolID,
                jsonObjectPerm.getJSONObject("Data").getJSONObject("System").getJSONObject("PermissionTransaction").getString("SendID"));
        assertEquals(utilsClass.getSDKID(),
                jsonObjectPerm.getJSONObject("Data").getJSONObject("System").getJSONObject("PermissionTransaction").getString("PeerID"));
        assertEquals("sdkName",
                jsonObjectPerm.getJSONObject("Data").getJSONObject("System").getJSONObject("PermissionTransaction").getString("ShownName"));
        String permListStr = jsonObjectPerm.getJSONObject("Data").getJSONObject("System").getJSONObject("PermissionTransaction").getString("PermissionList");
        assertEquals(fullPerm,permListStr.substring(permListStr.lastIndexOf(":")+1).trim().replaceAll(","," "));

        //如果测试子链 则不测试其他系统交易
        if(subLedger != "")  return;

        //退出节点交易详情检查
        String respQuit = mgToolCmd.quitPeer(PEER1IP + ":" + PEER1RPCPort, PEER2IP);
        assertEquals(true, respQuit.contains("success"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(respQuit,utilsClass.mgGetTxHashType),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        String quitPeerHash = respQuit.substring(respQuit.lastIndexOf(":") + 1).trim();
        JSONObject jsonObjectQuitPeer = checkTXDetailHeaderMsg(quitPeerHash, versionStore, typeSystem, subTypeQuitPeer);
        assertEquals(toolID,
                jsonObjectQuitPeer.getJSONObject("Data").getJSONObject("System").getJSONObject("PeerTransaction").getString("SendID"));
        assertEquals(getPeerId(PEER2IP, USERNAME, PASSWD),
                jsonObjectQuitPeer.getJSONObject("Data").getJSONObject("System").getJSONObject("PeerTransaction").getString("Id"));

            //节点加入交易详情检查
        String respAdd = mgToolCmd.addPeer("join", PEER1IP + ":" + PEER1RPCPort,
                "/" + ipv4 + "/" + PEER2IP, "/" + tcpProtocol + "/" + PEER2TCPPort, PEER2RPCPort);
        assertEquals(true, respAdd.contains("success"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(respAdd,utilsClass.mgGetTxHashType),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        String addPeerHash = respAdd.substring(respAdd.lastIndexOf(":") + 1).trim();
        JSONObject jsonObjectAddPeer = checkTXDetailHeaderMsg(addPeerHash, versionStore, typeSystem, subTypeAddPeer);
        assertEquals(toolID,
                jsonObjectAddPeer.getJSONObject("Data").getJSONObject("System").getJSONObject("PeerTransaction").getString("SendID"));
        assertEquals(getPeerId(PEER2IP, USERNAME, PASSWD),
                jsonObjectAddPeer.getJSONObject("Data").getJSONObject("System").getJSONObject("PeerTransaction").getString("Id"));
        assertEquals("peer" + PEER2IP.substring(PEER2IP.lastIndexOf(".") + 1).trim(),
                jsonObjectAddPeer.getJSONObject("Data").getJSONObject("System").getJSONObject("PeerTransaction").getString("ShownName"));
        assertEquals(true,
                jsonObjectAddPeer.getJSONObject("Data").getJSONObject("System").getJSONObject("PeerTransaction").getString("LanAddrs").contains("/" + ipv4 + "/" + PEER2IP + "/" + tcpProtocol + "/" + PEER2TCPPort));
        assertEquals(true,
                jsonObjectAddPeer.getJSONObject("Data").getJSONObject("System").getJSONObject("PeerTransaction").getString("WlanAddrs").contains("/" + ipv4 + "/" + PEER2IP + "/" + tcpProtocol + "/" + PEER2TCPPort));
//        assertEquals(PEER2RPCPort,
//                jsonObjectAddPeer.getJSONObject("Data").getJSONObject("System").getJSONObject("PeerTransaction").getString("RpcPort"));
        assertEquals("0",
                jsonObjectAddPeer.getJSONObject("Data").getJSONObject("System").getJSONObject("PeerTransaction").getString("PeerType"));

        //创建子链交易
        String chainName="tx_"+sdf.format(dt)+ RandomUtils.nextInt(10000);
        String addLedgerResp = mgToolCmd.createSubChain(PEER1IP,PEER1RPCPort," -z "+chainName,
                " -t sm3"," -w first"," -c raft",ids);
        assertEquals(addLedgerResp.contains("send transaction success"), true);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(addLedgerResp,utilsClass.mgGetTxHashType),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);


        String addLedgerHash = addLedgerResp.substring(addLedgerResp.lastIndexOf(":")+1).trim();
        JSONObject jsonObjectAddLedger = checkTXDetailHeaderMsg(addLedgerHash,versionStore,typeSystem,subTypeAddLedger);

        assertEquals(toolID,
                jsonObjectAddLedger.getJSONObject("Data").getJSONObject("System").getJSONObject("SubLedgerTransaction").getString("SendID"));
        assertEquals("0",
                jsonObjectAddLedger.getJSONObject("Data").getJSONObject("System").getJSONObject("SubLedgerTransaction").getString("OpType"));
        assertEquals(chainName,
                jsonObjectAddLedger.getJSONObject("Data").getJSONObject("System").getJSONObject("SubLedgerTransaction").getString("Name"));
        assertEquals("sm3",
                jsonObjectAddLedger.getJSONObject("Data").getJSONObject("System").getJSONObject("SubLedgerTransaction").getString("HashType"));
        assertEquals("first",
                jsonObjectAddLedger.getJSONObject("Data").getJSONObject("System").getJSONObject("SubLedgerTransaction").getString("Word"));
        assertEquals("raft",
                jsonObjectAddLedger.getJSONObject("Data").getJSONObject("System").getJSONObject("SubLedgerTransaction").getString("Consensus"));
        assertEquals("["+ids.replaceAll("-m","").replaceAll(" ","")+"]",
                jsonObjectAddLedger.getJSONObject("Data").getJSONObject("System").getJSONObject("SubLedgerTransaction").getString("Member").replaceAll("\"",""));

        //冻结子链交易
        String freezeLedgerResp = mgToolCmd.freezeSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(freezeLedgerResp.contains("send transaction success"), true);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(freezeLedgerResp,utilsClass.mgGetTxHashType),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        String freezeLedgerHash = freezeLedgerResp.substring(freezeLedgerResp.lastIndexOf(":")+1).trim();
        JSONObject jsonObjectFreezeLedger = checkTXDetailHeaderMsg(freezeLedgerHash,versionStore,typeSystem,subTypeFreezeLedger);
        assertEquals(toolID,
                jsonObjectFreezeLedger.getJSONObject("Data").getJSONObject("System").getJSONObject("SubLedgerTransaction").getString("SendID"));
        assertEquals("1",
                jsonObjectFreezeLedger.getJSONObject("Data").getJSONObject("System").getJSONObject("SubLedgerTransaction").getString("OpType"));
        assertEquals(chainName,
                jsonObjectFreezeLedger.getJSONObject("Data").getJSONObject("System").getJSONObject("SubLedgerTransaction").getString("Name"));
        assertEquals("sm3",
                jsonObjectFreezeLedger.getJSONObject("Data").getJSONObject("System").getJSONObject("SubLedgerTransaction").getString("HashType"));
        assertEquals("first",
                jsonObjectFreezeLedger.getJSONObject("Data").getJSONObject("System").getJSONObject("SubLedgerTransaction").getString("Word"));
        assertEquals("raft",
                jsonObjectFreezeLedger.getJSONObject("Data").getJSONObject("System").getJSONObject("SubLedgerTransaction").getString("Consensus"));
        assertEquals("["+ids.replaceAll("-m","").replaceAll(" ","")+"]",
                jsonObjectFreezeLedger.getJSONObject("Data").getJSONObject("System").getJSONObject("SubLedgerTransaction").getString("Member").replaceAll("\"",""));

        //恢复冻结子链交易

        String recoverLedgerResp = mgToolCmd.recoverSubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(recoverLedgerResp.contains("send transaction success"), true);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(recoverLedgerResp,utilsClass.mgGetTxHashType),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        String recoverLedgerHash = recoverLedgerResp.substring(recoverLedgerResp.lastIndexOf(":")+1).trim();
        JSONObject jsonObjectRecoverLedger = checkTXDetailHeaderMsg(recoverLedgerHash,versionStore,typeSystem,subTypeRecoverLedger);
        assertEquals(toolID,
                jsonObjectRecoverLedger.getJSONObject("Data").getJSONObject("System").getJSONObject("SubLedgerTransaction").getString("SendID"));
        assertEquals("2",
                jsonObjectRecoverLedger.getJSONObject("Data").getJSONObject("System").getJSONObject("SubLedgerTransaction").getString("OpType"));
        assertEquals(chainName,
                jsonObjectRecoverLedger.getJSONObject("Data").getJSONObject("System").getJSONObject("SubLedgerTransaction").getString("Name"));
        assertEquals("sm3",
                jsonObjectRecoverLedger.getJSONObject("Data").getJSONObject("System").getJSONObject("SubLedgerTransaction").getString("HashType"));
        assertEquals("first",
                jsonObjectRecoverLedger.getJSONObject("Data").getJSONObject("System").getJSONObject("SubLedgerTransaction").getString("Word"));
        assertEquals("raft",
                jsonObjectRecoverLedger.getJSONObject("Data").getJSONObject("System").getJSONObject("SubLedgerTransaction").getString("Consensus"));
        assertEquals("["+ids.replaceAll("-m","").replaceAll(" ","")+"]",
                jsonObjectRecoverLedger.getJSONObject("Data").getJSONObject("System").getJSONObject("SubLedgerTransaction").getString("Member").replaceAll("\"",""));

        //销毁子链交易
        String destroyLedgerResp = mgToolCmd.destroySubChain(PEER1IP,PEER1RPCPort," -z "+chainName);
        assertEquals(destroyLedgerResp.contains("send transaction success"), true);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(destroyLedgerResp,utilsClass.mgGetTxHashType),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        String destroyLedgerHash = destroyLedgerResp.substring(destroyLedgerResp.lastIndexOf(":")+1).trim();
        JSONObject jsonObjectDestroyLedger = checkTXDetailHeaderMsg(destroyLedgerHash,versionStore,typeSystem,subTypeDestroyLedger);
        assertEquals(toolID,
                jsonObjectDestroyLedger.getJSONObject("Data").getJSONObject("System").getJSONObject("SubLedgerTransaction").getString("SendID"));
        assertEquals("3",
                jsonObjectDestroyLedger.getJSONObject("Data").getJSONObject("System").getJSONObject("SubLedgerTransaction").getString("OpType"));
        assertEquals(chainName,
                jsonObjectDestroyLedger.getJSONObject("Data").getJSONObject("System").getJSONObject("SubLedgerTransaction").getString("Name"));
        assertEquals("sm3",
                jsonObjectDestroyLedger.getJSONObject("Data").getJSONObject("System").getJSONObject("SubLedgerTransaction").getString("HashType"));
        assertEquals("first",
                jsonObjectDestroyLedger.getJSONObject("Data").getJSONObject("System").getJSONObject("SubLedgerTransaction").getString("Word"));
        assertEquals("raft",
                jsonObjectDestroyLedger.getJSONObject("Data").getJSONObject("System").getJSONObject("SubLedgerTransaction").getString("Consensus"));
        assertEquals("["+ids.replaceAll("-m","").replaceAll(" ","")+"]",
                jsonObjectDestroyLedger.getJSONObject("Data").getJSONObject("System").getJSONObject("SubLedgerTransaction").getString("Member").replaceAll("\"",""));

    }

    @Test
    public void checkStoreTx()throws Exception{

        /**|存证|0|
         * |基本存证|0|
         * |加密存证|1|
         * |0.9基本存证|2|
         */
        //创建普通存证
        String Data="TxType tx store "+sdf.format(dt)+ RandomUtils.nextInt(100000);
        log.info("普通存证数据："+Data);
        String response1=store.CreateStore(Data);

        //创建隐私存证
        String priData = "TxTypePri-" + UtilsClass.Random(2);
        Map<String,Object> map=new HashMap<>();
        map.put("pubKeys",PUBKEY1);
        map.put("pubkeys",PUBKEY6);
        log.info("隐私存证数据："+priData);
        String response2 = store.CreatePrivateStore(priData,map);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        String txHash1 = JSONObject.fromObject(response1).getJSONObject("Data").get("Figure").toString();
        checkTXDetailHeaderMsg(txHash1,versionStore,typeStore,subTypeStore);
        checkStore(txHash1,Data,"store");


        //检查隐私存证信息
        String txHash2 = JSONObject.fromObject(response2).getJSONObject("Data").get("Figure").toString();
        checkTXDetailHeaderMsg(txHash2,versionPriStore,typeStore,subTypePriStore);

//        JSONObject jsonObjecttran=JSONObject.fromObject(store.GetTransaction(txHash2)).getJSONObject("Data");
        JSONObject jsonObjecttx=JSONObject.fromObject(store.GetTxDetail(txHash2)).getJSONObject("Data");

        //隐私存证storeData中为加密后数据，目前仅判断不包含明文原始data数据
//        assertEquals(jsonObjecttran.getString("storeData").contains(priData), false);
        //assertEquals("store",jsonObjecttran.getString("transactionType"));//20190514 开发移除该字段
//        assertEquals(true,jsonObjecttran.getJSONObject("extra").isNullObject());//检查合约extra

        assertEquals(jsonObjecttx.getJSONObject("Store").getString("StoreData").contains(priData), false);
        //assertEquals("store",jsonObjecttx.getJSONObject("Store").getString("transactionType"));
        assertEquals(true,jsonObjecttx.getJSONObject("Store").getJSONObject("Extra").isNullObject());//检查合约extra
    }

    @Test
    public void checkUTXOTx()throws Exception{
        /**|UTXO|1|
         * |UTXO发行|10|
         * |UTXO转账|11|
         * |UTXO回收|12|
         */
        //发行0.0* 数量token
        String minToken = "TxTypeSoMin-" + UtilsClass.Random(6);
        String minData = "单签" + ADDRESS1 + "发行token " + minToken;
        String minAmount = "0.001";
        log.info(minData);

        String minResp = soloSign.issueToken(PRIKEY1,minToken,minAmount,minData,ADDRESS1);


        //UTXO类交易 Type 1 SubType 10 11 12
        //单签发行
        String tokenTypeS = "TxTypeSOLOTC-"+ UtilsClass.Random(6);
        String siData="单签"+ADDRESS1+"发行token "+tokenTypeS;
        String amount="10000";
        log.info(siData);
        String response3= soloSign.issueToken(PRIKEY1,tokenTypeS,amount,siData,ADDRESS1);


        //多签发行
        String tokenTypeM = "TxTypeMULTIC" + UtilsClass.Random(8);
        String amount1 = "50000";
        String  mulData= "多签"+MULITADD3 + "发行给自己" + tokenTypeM + " token，数量为：" + amount1;
        log.info(mulData);
        String response51 = multiSign.issueToken(IMPPUTIONADD,tokenTypeM, amount1, mulData);
        assertThat(response51, containsString("200"));
        String Tx1 = JSONObject.fromObject(response51).getJSONObject("Data").getString("Tx");
        log.info("第一次签名");
        String response52 = multiSign.Sign(Tx1, PRIKEY5);

        String tokenTypeM2 = "TxTypeMULTIC" + UtilsClass.Random(8);
        String amount12 = "50000";
        String  mulData2= "多签"+MULITADD3 + "发行给"+MULITADD7 +" "+ tokenTypeM2 + " token，数量为：" + amount12;
        log.info(mulData2);
        String response53 = multiSign.issueToken(IMPPUTIONADD,MULITADD7,tokenTypeM2,amount12,mulData2);
        String Tx13 = JSONObject.fromObject(response53).getJSONObject("Data").getString("Tx");
        log.info("第一次签名");
        String response54 = multiSign.Sign(Tx13, PRIKEY5);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType02),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        //单签转账
        assertEquals(JSONObject.fromObject(soloSign.Balance(PRIKEY1,tokenTypeS)).getJSONObject("Data").getString("Total"),amount);
        String amountTransfer="0.01";
        List<Map> list=soloSign.constructToken(ADDRESS3,tokenTypeS,amountTransfer);
        String tranferdata="transfer to "+ADDRESS3+" with amount "+amountTransfer;
        String response4= soloSign.Transfer(list,PRIKEY1,tranferdata);

        //多签转账
        assertEquals(JSONObject.fromObject( multiSign.Balance(IMPPUTIONADD,PRIKEY4,tokenTypeM)).getJSONObject("Data").getString("Total"),amount1);
        String tranferAmount="3000";
        String transferData = IMPPUTIONADD+" 向 " + ADDRESS5 + " 转账 " + tranferAmount + " " +tokenTypeM;
        List<Map> listInit = utilsClass.constructToken(ADDRESS5, tokenTypeM, "3000");
        log.info(transferData);
        String response6 = multiSign.Transfer(PRIKEY4, transferData, IMPPUTIONADD, listInit);


        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType02),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        //检查小数量单签发行交易信息
        String txHashMin = JSONObject.fromObject(minResp).getString("Data");
        checkTXDetailHeaderMsg(txHashMin,versionSUTXO,typeUTXO,subTypeIssue);
        JSONObject uxtoJsonMin= JSONObject.fromObject(JSONObject.fromObject(store.GetTxDetail(txHashMin)).getJSONObject("Data").getJSONObject("UTXO"));
        assertEquals(minData,uxtoJsonMin.getString("Data"));
        assertEquals(1,uxtoJsonMin.getJSONArray("Records").size());
        checkFromTo(uxtoJsonMin,ADDRESS1,ADDRESS1,minToken,minAmount,0);


        //检查单签发行交易信息
        String txHash3 = JSONObject.fromObject(response3).getString("Data");
        checkTXDetailHeaderMsg(txHash3,versionSUTXO,typeUTXO,subTypeIssue);
        JSONObject uxtoJson= JSONObject.fromObject(JSONObject.fromObject(store.GetTxDetail(txHash3)).getJSONObject("Data").getJSONObject("UTXO"));
        assertEquals(siData,uxtoJson.getString("Data"));
        assertEquals(1,uxtoJson.getJSONArray("Records").size());
        checkFromTo(uxtoJson,ADDRESS1,ADDRESS1,tokenTypeS,amount,0);

        //检查单签转账交易信息
        String txHash4 = JSONObject.fromObject(response4).getString("Data");
        checkTXDetailHeaderMsg(txHash4,versionSUTXO,typeUTXO,subTypeTransfer);
        uxtoJson= JSONObject.fromObject(JSONObject.fromObject(store.GetTxDetail(txHash4)).getJSONObject("Data").getJSONObject("UTXO"));
        assertEquals(tranferdata,uxtoJson.getString("Data"));

        List<Map> listST = commonFunc.constructUTXOTxDetailList(ADDRESS1,ADDRESS3,tokenTypeS,amountTransfer);
        List<Map> listST2 = commonFunc.constructUTXOTxDetailList(ADDRESS1,ADDRESS1,tokenTypeS,"9999.99",listST);

        JSONArray jsonArrayCheck = uxtoJson.getJSONArray("Records");
        commonFunc.checkListArray(listST2,jsonArrayCheck);


        //检查多签发行交易信息
        String txHash5 = JSONObject.fromObject(response52).getJSONObject("Data").get("TxId").toString();
        checkTXDetailHeaderMsg(txHash5,versionMUTXO,typeUTXO,subTypeIssue);
        uxtoJson= JSONObject.fromObject(JSONObject.fromObject(store.GetTxDetail(txHash5)).getJSONObject("Data").getJSONObject("UTXO"));
        assertEquals(mulData,uxtoJson.getString("Data"));
        assertEquals(1,uxtoJson.getJSONArray("Records").size());
        checkFromTo(uxtoJson,IMPPUTIONADD,IMPPUTIONADD,tokenTypeM,amount1,0);

        String txHash51 = JSONObject.fromObject(response54).getJSONObject("Data").get("TxId").toString();
        checkTXDetailHeaderMsg(txHash51,versionMUTXO,typeUTXO,subTypeIssue);
        uxtoJson= JSONObject.fromObject(JSONObject.fromObject(store.GetTxDetail(txHash51)).getJSONObject("Data").getJSONObject("UTXO"));
        assertEquals(mulData2,uxtoJson.getString("Data"));
        assertEquals(1,uxtoJson.getJSONArray("Records").size());
        checkFromTo(uxtoJson,IMPPUTIONADD,MULITADD7,tokenTypeM2,amount12,0);


        //检查多签转账交易信息
        String txHash6 = JSONObject.fromObject(response6).getJSONObject("Data").get("TxId").toString();
        checkTXDetailHeaderMsg(txHash6,versionMUTXO,typeUTXO,subTypeTransfer);
        uxtoJson= JSONObject.fromObject(JSONObject.fromObject(store.GetTxDetail(txHash6)).getJSONObject("Data").getJSONObject("UTXO"));
        assertEquals(transferData,uxtoJson.getString("Data"));
        List<Map> listMT = commonFunc.constructUTXOTxDetailList(IMPPUTIONADD,ADDRESS5,tokenTypeM,tranferAmount);
        List<Map> listMT2 = constructUTXOTxDetailList(IMPPUTIONADD,IMPPUTIONADD,tokenTypeM,String.valueOf(Integer.parseInt(amount1)-Integer.parseInt(tranferAmount)),listMT);

        jsonArrayCheck = uxtoJson.getJSONArray("Records");
        commonFunc.checkListArray(listMT2,jsonArrayCheck);

        //单签回收
        String recySoloAmount="600.05";
        log.info("单签回收");
        log.info(soloSign.Balance(PRIKEY1,tokenTypeS));
        String RecycleSoloInfo = soloSign.Recycle( PRIKEY1, tokenTypeS, recySoloAmount);


        //多签回收
        log.info("多签回收");
        log.info(multiSign.Balance(IMPPUTIONADD,PRIKEY4,tokenTypeM));
        String recyMultiAmount="0.07";
        String RecycleMultiInfo = multiSign.Recycle(IMPPUTIONADD, PRIKEY4, tokenTypeM, recyMultiAmount);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType02),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        //检查单签回收交易信息
        String txHash7 = JSONObject.fromObject(RecycleSoloInfo).getJSONObject("Data").getString("Figure");
        checkTXDetailHeaderMsg(txHash7,versionMUTXO,typeUTXO,subTypeRecycle);
        uxtoJson.clear();
        log.info("****************");
        uxtoJson= JSONObject.fromObject(JSONObject.fromObject(store.GetTxDetail(txHash7)).getJSONObject("Data").getJSONObject("UTXO"));

        List<Map> listSD = commonFunc.constructUTXOTxDetailList(ADDRESS1,zeroAddr,tokenTypeS,recySoloAmount);
        List<Map> listSD2 = commonFunc.constructUTXOTxDetailList(ADDRESS1,ADDRESS1,tokenTypeS,"9399.94",listSD);
        jsonArrayCheck = uxtoJson.getJSONArray("Records");
        commonFunc.checkListArray(listSD2,jsonArrayCheck);

        //检查多签回收交易信息
        String txHash8 = JSONObject.fromObject(RecycleMultiInfo).getJSONObject("Data").get("TxId").toString();
        checkTXDetailHeaderMsg(txHash8,versionMUTXO,typeUTXO,subTypeRecycle);
        uxtoJson= JSONObject.fromObject(JSONObject.fromObject(store.GetTxDetail(txHash8)).getJSONObject("Data").getJSONObject("UTXO"));

        List<Map> listMD = commonFunc.constructUTXOTxDetailList(IMPPUTIONADD,zeroAddr,tokenTypeM,"0.07");
        List<Map> listMD2 = commonFunc.constructUTXOTxDetailList(IMPPUTIONADD,IMPPUTIONADD,tokenTypeM,"46999.93",listMD);
        jsonArrayCheck = uxtoJson.getJSONArray("Records");
        commonFunc.checkListArray(listMD2,jsonArrayCheck);
   }

    public void checkFromTo(JSONObject jsonObject,String from,String to,String TokenType,String amount,int index)throws Exception{
        assertEquals(from,jsonObject.getJSONArray("Records").getJSONObject(index).getString("From"));
        assertEquals(to,jsonObject.getJSONArray("Records").getJSONObject(index).getString("To"));
        assertEquals(TokenType,jsonObject.getJSONArray("Records").getJSONObject(index).getString("TokenType"));
        assertEquals(amount,jsonObject.getJSONArray("Records").getJSONObject(index).getString("Amount"));
    }

    //20200413 分离docker合约相关测试

    @Test
    public void checkWVMTx()throws Exception{
        /**|WVM|3|
         * |WVM安装|40|
         * |WVM销毁|41|
         * |WVM invoke|42|
         */
        FileOperation fileOper = new FileOperation();
        String ctName="UI_" + sdf.format(dt)+ RandomUtils.nextInt(100000);
        WVMContractTest wvm = new WVMContractTest();
        // 替换原wvm合约文件中的合约名称，防止合约重复导致的问题
        // 替换后会重新生成新的文件名多出"_temp"的文件作为后面合约安装使用的文件
        fileOper.replace(resourcePath + wvm.wvmFile + ".txt", wvm.orgName, ctName);

        //安装合约后会得到合约hash：由Prikey和ctName进行运算得到
        String response1 = wvm.wvmInstallTest( wvm.wvmFile +"_temp.txt",PRIKEY1);
        log.info("Install Pri:"+PRIKEY1);
        String txHash1 = JSONObject.fromObject(response1).getJSONObject("Data").getString("Figure");
        String ctHash = JSONObject.fromObject(response1).getJSONObject("Data").getString("Name");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);
        //调用合约内的交易
        String response2 = wvm.invokeNew(ctHash,"initAccount",wvm.accountA,wvm.amountA);//初始化账户A 账户余额50
        String txHash2 = JSONObject.fromObject(response2).getJSONObject("Data").getString("Figure");

        String response3 = wvm.invokeNew(ctHash,"initAccount",wvm.accountB,wvm.amountB);//初始化账户B 账户余额60
        String txHash3 = JSONObject.fromObject(response3).getJSONObject("Data").getString("Figure");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        String response4 = wvm.invokeNew(ctHash,"transfer",wvm.accountA,wvm.accountB,wvm.transfer);//A向B转30
        String txHash4 = JSONObject.fromObject(response4).getJSONObject("Data").getString("Figure");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        //查询余额invoke接口
        String response5 = wvm.invokeNew(ctHash,"getBalance",wvm.accountA);//获取账户A账户余额
        String txHash5 = JSONObject.fromObject(response5).getJSONObject("Data").getString("Figure");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        //销毁wvm合约
        String response9 = wvm.wvmDestroyTest(ctHash);
        String txHash9 = JSONObject.fromObject(response9).getJSONObject("Data").getString("Figure");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType00),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        //检查合约创建 检查Type和SubType类型
        JSONObject jsonObjectCreate = checkTXDetailHeaderMsg(txHash1,versionWVM1,typeWVM,subTypeCreateWVM);
        JSONObject jsonObjectInvokeInit = checkTXDetailHeaderMsg(txHash2,versionWVM1,typeWVM,subTypeWVMTx);
        JSONObject jsonObjectInvokeTransfer = checkTXDetailHeaderMsg(txHash4,versionWVM1,typeWVM,subTypeWVMTx);
        JSONObject jsonObjectDestroy = checkTXDetailHeaderMsg(txHash9,versionWVM1,typeWVM,subTypeDeleteWVM);

        //检查安装合约交易详情内参数
        String data = utilsClass.encryptBASE64(utilsClass.readInput(
                resourcePath + "wvm_temp.txt").toString().trim().getBytes()).replaceAll("\r\n", "");
        assertEquals(ctHash,
                jsonObjectCreate.getJSONObject("Data").getJSONObject("WVM").getJSONObject("WVMContractTx").getString("Name"));
        //此处owner对应的是PubKey 因编解码使用的库可能不太一样 因此此处校验原始pubkey
        String p1 = new String(utilsClass.decryptBASE64(PUBKEY1));
        String p2 = new String (utilsClass.decryptBASE64(jsonObjectCreate.getJSONObject("Data").getJSONObject("WVM").getJSONObject("WVMContractTx").getString("Owner")));
        assertEquals(p1.replaceAll("\r\n",""),p2.replaceAll("\n",""));
        assertEquals(data,
                jsonObjectCreate.getJSONObject("Data").getJSONObject("WVM").getJSONObject("WVMContractTx").getString("Src"));
        log.info("Check create wvm tx detail complete");


        //检查invoke init交易详情内参数
        assertEquals(ctHash,
                jsonObjectInvokeInit.getJSONObject("Data").getJSONObject("WVM").getJSONObject("WVMContractTx").getString("Name"));
        assertEquals("initAccount",
                jsonObjectInvokeInit.getJSONObject("Data").getJSONObject("WVM").getJSONObject("WVMContractTx").getJSONObject("Arg").getString("Method"));
        assertEquals(wvm.caller,new String(utilsClass.decryptBASE64(
                jsonObjectInvokeInit.getJSONObject("Data").getJSONObject("WVM").getJSONObject("WVMContractTx").getJSONObject("Arg").getString("Caller"))));

        String argsinit0= jsonObjectInvokeInit.getJSONObject("Data").getJSONObject("WVM").getJSONObject("WVMContractTx").getJSONObject("Arg").getJSONArray("Args").getString(0);
        String argsinit1= jsonObjectInvokeInit.getJSONObject("Data").getJSONObject("WVM").getJSONObject("WVMContractTx").getJSONObject("Arg").getJSONArray("Args").getString(1);
        assertEquals(wvm.accountA, argsinit0);
        assertEquals(Integer.toString(wvm.amountA),argsinit1);
        log.info("Check invoke init wvm tx detail complete");


        //检查invoke transfer交易详情内参数
        assertEquals(ctHash,
                jsonObjectInvokeTransfer.getJSONObject("Data").getJSONObject("WVM").getJSONObject("WVMContractTx").getString("Name"));
        assertEquals("transfer",
                jsonObjectInvokeTransfer.getJSONObject("Data").getJSONObject("WVM").getJSONObject("WVMContractTx").getJSONObject("Arg").getString("Method"));
        assertEquals(wvm.caller,new String(utilsClass.decryptBASE64(
                jsonObjectInvokeTransfer.getJSONObject("Data").getJSONObject("WVM").getJSONObject("WVMContractTx").getJSONObject("Arg").getString("Caller"))));

        String argstrf0= jsonObjectInvokeTransfer.getJSONObject("Data").getJSONObject("WVM").getJSONObject("WVMContractTx").getJSONObject("Arg").getJSONArray("Args").getString(0);
        String argstrf1= jsonObjectInvokeTransfer.getJSONObject("Data").getJSONObject("WVM").getJSONObject("WVMContractTx").getJSONObject("Arg").getJSONArray("Args").getString(1);
        String argstrf2= jsonObjectInvokeTransfer.getJSONObject("Data").getJSONObject("WVM").getJSONObject("WVMContractTx").getJSONObject("Arg").getJSONArray("Args").getString(2);
        assertEquals(wvm.accountA, argstrf0);
        assertEquals(wvm.accountB,argstrf1);
        assertEquals(Integer.toString(wvm.transfer),argstrf2);
        log.info("Check invoke transfer wvm tx detail complete");


        //检查销毁合约交易详情参数
        assertEquals(ctHash,
                jsonObjectDestroy.getJSONObject("Data").getJSONObject("WVM").getJSONObject("WVMContractTx").getString("Name"));
        log.info("Check destroy wvm tx detail complete");
    }

    @Test
    public void checkAdminTx()throws Exception{
        String tokenType = "FreezeToken-"+ UtilsClass.Random(6);
        String respon= soloSign.issueToken(PRIKEY1,tokenType,"100","单签"+ADDRESS1+"发行token "+tokenType,ADDRESS1);

        //预先做删除归集地址、删除发行地址操作、解除token锁定，以便后续操作正常进行
        assertThat(multiSign.delCollAddress(PRIKEY1,ADDRESS6),containsString("200"));
        assertThat(multiSign.delissueaddress(PRIKEY1,ADDRESS6),containsString("200"));
        assertThat(multiSign.recoverFrozenToken(PRIKEY1,tokenType),containsString("200"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        //Admin类交易 Type 20 SubType 200 201 202 203
        String response10= multiSign.collAddress(PRIKEY1,ADDRESS6);
        String response11= multiSign.addissueaddress(PRIKEY1,ADDRESS6);
        String response3=multiSign.freezeToken(PRIKEY1,tokenType);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        //添加归集地址交易信息检查
        String txHash10 = JSONObject.fromObject(response10).getString("Data");
        checkTXDetailHeaderMsg(txHash10,versionStore,typeAdmin,subTypeAddColl);
        checkAdmin(txHash10,"collAddress","CollAddress",ADDRESS6,"admin");

        //添加发行地址交易信息检查
        String txHash11 = JSONObject.fromObject(response11).getString("Data");
        checkTXDetailHeaderMsg(txHash11,versionStore,typeAdmin,subTypeAddIssue);
        checkAdmin(txHash11,"issueAddress","IssueAddress",ADDRESS6,"admin");

        //冻结token交易信息检查
        String txHash31 = JSONObject.fromObject(response3).getString("Data");
        checkTXDetailHeaderMsg(txHash31,versionStore,typeAdmin,subTypeFreezeToken);
        checkAdmin2(txHash31,"FreezeToken",tokenType,"admin");

        //删除归集地址
        String response12= multiSign.delCollAddress(PRIKEY1,ADDRESS6);
        //删除发行地址
        String response13= multiSign.delissueaddress(PRIKEY1,ADDRESS6);
        //解除冻结token
        String response4=multiSign.recoverFrozenToken(PRIKEY1,tokenType);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SLEEPTIME);

        //检查删除归集地址交易信息
        String txHash12 = JSONObject.fromObject(response12).getString("Data");
        checkTXDetailHeaderMsg(txHash12,versionStore,typeAdmin,subTypeDelColl);
        checkAdmin(txHash12,"collAddress","CollAddress",ADDRESS6,"admin");

        //检查删除发行地址交易信息
        String txHash13 = JSONObject.fromObject(response13).getString("Data");
        checkTXDetailHeaderMsg(txHash13,versionStore,typeAdmin,subTypeDelIssue);
        checkAdmin(txHash13,"issueAddress","IssueAddress",ADDRESS6,"admin");

        //解除冻结token
        String txHash41 = JSONObject.fromObject(response4).getString("Data");
        checkTXDetailHeaderMsg(txHash41,versionStore,typeAdmin,subTypeRecoverToken);
        checkAdmin2(txHash41,"RecoverToken",tokenType,"admin");

    }

    public JSONObject checkTXDetailHeaderMsg(String hash, String version, String type, String subType)throws Exception{
        log.info("hash:"+hash);
        JSONObject objectDetail = JSONObject.fromObject(store.GetTxDetail(hash));
        JSONObject jsonObject = objectDetail.getJSONObject("Data").getJSONObject("Header");
        assertEquals(version,jsonObject.getString("Version"));
        assertEquals(type,jsonObject.getString("Type"));
        assertEquals(subType,jsonObject.getString("SubType"));
        assertEquals(hash,jsonObject.getString("TransactionHash"));

        return objectDetail;
    }


    public void checkStore(String hash,String storeData,String transactionType)throws Exception{
        JSONObject jsonObjectOrg =JSONObject.fromObject(store.GetTxDetail(hash)).getJSONObject("Data");
        JSONObject jsonObject =jsonObjectOrg.getJSONObject("Store");
        assertEquals(storeData,jsonObject.getString("StoreData"));//检查存证数据
        //assertEquals(transactionType,jsonObject.getString("transactionType"));//检查交易类型
        assertEquals(true,jsonObject.getJSONObject("Extra").isNullObject());//检查extra

        //检查其他字段为空

        assertEquals(true,jsonObjectOrg.getJSONObject("Contract").isNullObject());
        assertEquals(true,jsonObjectOrg.getJSONObject("Store9").isNullObject());
        assertEquals(true,jsonObjectOrg.getJSONObject("UTXO").isNullObject());
        assertEquals(true,jsonObjectOrg.getJSONObject("Admin").isNullObject());
        assertEquals(true,jsonObjectOrg.getJSONObject("WVM").isNullObject());

    }

    public void checkAdmin(String hash,String keywordTran,String keywordTxdetail,String checkstr,String txType)throws Exception{
        JSONObject jsonObjectOrg2 =JSONObject.fromObject(store.GetTxDetail(hash)).getJSONObject("Data");
        assertThat(jsonObjectOrg2.getJSONObject("Admin").getJSONArray(keywordTxdetail).getString(0),containsString(checkstr));
        assertEquals(true,jsonObjectOrg2.getJSONObject("Admin").getJSONObject("extra").isNullObject());//检查extra

        //检查其他字段为空
        assertEquals(true,jsonObjectOrg2.getJSONObject("Contract").isNullObject());
        assertEquals(true,jsonObjectOrg2.getJSONObject("Store9").isNullObject());
        assertEquals(true,jsonObjectOrg2.getJSONObject("UTXO").isNullObject());
        assertEquals(true,jsonObjectOrg2.getJSONObject("Store").isNullObject());
        assertEquals(true,jsonObjectOrg2.getJSONObject("WVM").isNullObject());
    }

    public void checkAdmin2(String hash,String keyword,String checkstr,String txType)throws Exception{
        JSONObject jsonObjectOrg2 =JSONObject.fromObject(store.GetTxDetail(hash)).getJSONObject("Data");
        assertThat(jsonObjectOrg2.getJSONObject("Admin").getString(keyword),containsString(checkstr));
        assertEquals(true,jsonObjectOrg2.getJSONObject("Admin").getJSONObject("extra").isNullObject());//检查extra

    }
}
