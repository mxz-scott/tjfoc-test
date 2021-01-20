package com.tjfintech.common.functionTest.appChainTest;

import com.alibaba.fastjson.JSON;
import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.MgToolCmd;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.functionTest.mixTestWithConfigChange.TestMgTool;
import com.tjfintech.common.utils.SubLedgerCmd;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class AppChain_Z_ConfigChange02_ClearDB {

    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();

    MgToolCmd mgToolCmd = new MgToolCmd();
    TestMgTool testMgTool = new TestMgTool();
    SubLedgerCmd subLedgerCmd = new SubLedgerCmd();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();

    public static String glbChain01= "glbCh1";

    String id1 = getPeerId(PEER1IP,USERNAME,PASSWD);
    String id2 = getPeerId(PEER2IP,USERNAME,PASSWD);
    String id3 = getPeerId(PEER4IP,USERNAME,PASSWD);
    String id4 = getPeerId(PEER3IP,USERNAME,PASSWD);
    String ids = " -m "+ id1+","+ id2+","+ id3;
    List<String> listPeer = new ArrayList<>();


    @Before
    public void clearData()throws Exception{
        BeforeCondition beforeCondition = new BeforeCondition();
        beforeCondition.clearDataSetPerm999();
        sleepAndSaveInfo(SLEEPTIME);

        HashMap peer = new HashMap();
        peer.put("ID",id4);
        peer.put("ShownName","testName");
        List inAddr = new ArrayList();
        List outAddr = new ArrayList();
        inAddr.add(ipv4 + PEER3IP + tcpProtocol + PEER3TCPPort);
        outAddr.add("");
        peer.put("InAddrs",inAddr);
        peer.put("OutAddrs",outAddr);
        peer.put("PeerType",2);
        //        peer.put("RpcPort",Integer.valueOf(PEER3RPCPort));;

        listPeer.add(JSON.toJSONString(peer).replace("\"","\\\""));
    }

    @Test
    public void TC1767_1766_1765_1732_1730_1602_changePeerWithSubledger()throws Exception{
        String resp = mgToolCmd.getAppChain(PEER1IP, PEER1RPCPort, "");
        assertEquals(true,resp.contains("{}"));

        String mgResp = mgToolCmd.createAppChain(PEER1IP, PEER1RPCPort, " -n " + glbChain01,
                " -t sm3", " -w first", " -c raft",
                ids);
//        sleepAndSaveInfo(SLEEPTIME*2);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(mgResp,utilsClass.mgGetTxHashType),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME*2);

        assertEquals(mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"").contains(
                "\"name\": \""+glbChain01.toLowerCase()+"\""), true);

        //动态删除节点B，因已有活跃子链使用 无法成功删除
        String checkStr = "failed:some ledger is using this peer";
        dynamicPeerChk(checkStr,0,3,PEER2IP,PEER2RPCPort,PEER2TCPPort);

        //冻结子链
        String respFreeze = mgToolCmd.freezeAppChain(PEER1IP,PEER1RPCPort," -n " + glbChain01);
        assertEquals(respFreeze.contains("send transaction success"), true);
//        sleepAndSaveInfo(SLEEPTIME);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(respFreeze,utilsClass.mgGetTxHashType),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //检查子链状态正确
        respFreeze = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -n " + glbChain01);
        assertEquals(respFreeze.contains(ledgerStateFreeze), true);

        dynamicPeerChk(checkStr,0,3,PEER2IP,PEER2RPCPort,PEER2TCPPort);



        //销毁子链 可以退出变更节点
        String respDestory = mgToolCmd.destroyAppChain(PEER1IP,PEER1RPCPort," -n " + glbChain01);
        assertEquals(respDestory.contains("send transaction success"), true);
//        sleepAndSaveInfo(SLEEPTIME);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(respDestory,utilsClass.mgGetTxHashType),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //检查子链状态正确
        respDestory = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -n " + glbChain01);
        assertEquals(respDestory.contains(ledgerStateDestroy), true);

        checkStr = "success";
        dynamicPeerChk(checkStr,SLEEPTIME,2,PEER2IP,PEER2RPCPort,PEER2TCPPort);
        sleepAndSaveInfo(SLEEPTIME);
        testMgTool.queryPeerListNo(PEER1IP+":"+PEER1RPCPort,3);
    }

    public void dynamicPeerChk(String checkResp,long sleep,int peerNo,String dyIP,String dyRpcPort,String dyTcpPort)throws Exception{
        String respQuit = mgToolCmd.quitPeer(PEER1IP+":"+PEER1RPCPort,dyIP);
        if(!respQuit.contains("peer not found")) {
            assertEquals(respQuit.contains(checkResp), true);
            sleepAndSaveInfo(sleep, "operation waiting......");
        }
        testMgTool.queryPeerListNo(PEER1IP+":"+PEER1RPCPort,peerNo);
        String respChange = mgToolCmd.addPeer("observer",PEER1IP + ":" + PEER1RPCPort,
                ipv4 + dyIP,tcpProtocol + dyTcpPort,dyRpcPort);
        assertEquals(respChange.contains(checkResp), true);

        respChange = mgToolCmd.addPeer("join",PEER1IP + ":" + PEER1RPCPort,
                ipv4 + dyIP,tcpProtocol + dyTcpPort,dyRpcPort);
        assertEquals(respChange.contains(checkResp), true);
    }

    @Test
    public void TC1735_1768_1769_1772_testClusterPeerWithMultiSubledger()throws Exception{
        createComplexSubledgers();

        //1735 1768
        String checkStr = "failed:some ledger is using this peer";
        dynamicPeerChk(checkStr,0,3,PEER2IP,PEER2RPCPort,PEER2TCPPort);

        checkStr = "success";
        dynamicPeerChk(checkStr,SLEEPTIME,2,PEER4IP,PEER4RPCPort,PEER4TCPPort);
        sleepAndSaveInfo(SLEEPTIME);
        testMgTool.queryPeerListNo(PEER1IP+":"+PEER1RPCPort,3);

        //1768 1772
        dynamicPeerChk(checkStr,SLEEPTIME,3,PEER3IP,PEER3RPCPort,PEER3TCPPort);
        sleepAndSaveInfo(SLEEPTIME);
        testMgTool.queryPeerListNo(PEER1IP+":"+PEER1RPCPort,4);
    }



    public void createComplexSubledgers()throws Exception{
        String chain1 = "1735Sub1";//A/B/C节点将要销毁子链
        String chain2 = "1735Sub2";//B/C节点将要销毁子链
        String chain3 = "1735Sub3";//A/B节点将要冻结子链
        String chain4 = "1735Sub4";//A/B节点 活跃子链

        mgToolCmd.createAppChain(PEER1IP, PEER1RPCPort, " -n " + chain1,
                " -t sm3", " -w first", " -c raft",
                ids);
        mgToolCmd.createAppChain(PEER1IP, PEER1RPCPort, " -n " + chain2,
                " -t sm3", " -w first", " -c raft",
                " -m " + id2 + "," + id3);
        mgToolCmd.createAppChain(PEER1IP, PEER1RPCPort, " -n " + chain3,
                " -t sm3", " -w first", " -c raft",
                " -m " + id2 + "," + id1);
        mgToolCmd.createAppChain(PEER1IP, PEER1RPCPort, " -n " + chain4,
                " -t sm3", " -w first", " -c raft",
                " -m " + id2 + "," + id1);
        sleepAndSaveInfo(SLEEPTIME);
        String respGetAllledger = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(true,respGetAllledger.contains("\"name\": \"" + chain1.toLowerCase() + "\""));
        assertEquals(true,respGetAllledger.contains("\"name\": \"" + chain2.toLowerCase() + "\""));
        assertEquals(true,respGetAllledger.contains("\"name\": \"" + chain3.toLowerCase() + "\""));
        assertEquals(true,respGetAllledger.contains("\"name\": \"" + chain4.toLowerCase() + "\""));

        //冻结子链
        String respDestory = mgToolCmd.destroyAppChain(PEER1IP,PEER1RPCPort," -n " + chain1);
        assertEquals(respDestory.contains("send transaction success"), true);
        respDestory = mgToolCmd.destroyAppChain(PEER1IP,PEER1RPCPort," -n " + chain2);
        assertEquals(respDestory.contains("send transaction success"), true);
        sleepAndSaveInfo(SLEEPTIME);
        //检查子链状态正确
        respDestory = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -n " + chain1);
        assertEquals(respDestory.contains(ledgerStateDestroy), true);
        respDestory = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -n " + chain2);
        assertEquals(respDestory.contains(ledgerStateDestroy), true);


        //销毁子链 可以退出变更节点
        String respFreeze = mgToolCmd.freezeAppChain(PEER1IP,PEER1RPCPort," -n " + chain3);
        assertEquals(respFreeze.contains("send transaction success"), true);
        sleepAndSaveInfo(SLEEPTIME);
        //检查子链状态正确
        respFreeze = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -n " + chain3);
        assertEquals(respFreeze.contains(ledgerStateFreeze), true);
    }

    //@AfterClass
    public static void resetPeerAndSDK()throws  Exception {
        UtilsClass utilsClassTemp = new UtilsClass();
        utilsClassTemp.setAndRestartPeerList(resetPeerBase);
        utilsClassTemp.setAndRestartSDK(resetSDKConfig);
    }
}
