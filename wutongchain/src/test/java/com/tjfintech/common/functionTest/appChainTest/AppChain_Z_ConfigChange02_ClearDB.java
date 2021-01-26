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
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassApp.*;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class AppChain_Z_ConfigChange02_ClearDB {

    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store = testBuilder.getStore();

    MgToolCmd mgToolCmd = new MgToolCmd();
    TestMgTool testMgTool = new TestMgTool();
    SubLedgerCmd subLedgerCmd = new SubLedgerCmd();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();

    public static String ledgerId1 = "";
    public static String ledgerId2 = "";
    public static String ledgerId3 = "";
    public static String ledgerId4 = "";

//    @Before
    public void clearData()throws Exception{
        //设置节点 清空db数据 并重启
        utilsClass.setAndRestartPeerList(clearPeerDB);
        //重启SDK
        utilsClass.setAndRestartSDK();
    }


    public void dynamicPeerChk(String checkResp,long sleep,int peerNo,String dyIP,String dyRpcPort,String dyTcpPort)throws Exception{
        String respQuit = mgToolCmd.quitPeer(PEER1IP + ":" + PEER1RPCPort,dyIP);
        if(!respQuit.contains("peer not found")) {
            assertEquals(respQuit.contains(checkResp), true);
            sleepAndSaveInfo(sleep, "operation waiting......");
        }
        testMgTool.queryPeerListNo(PEER1IP + ":" + PEER1RPCPort,peerNo);
        String respChange = mgToolCmd.addPeer("observer",PEER1IP + ":" + PEER1RPCPort,
                ipv4 + dyIP,tcpProtocol + dyTcpPort,dyRpcPort);
        assertEquals(respChange.contains(checkResp), true);

        respChange = mgToolCmd.addPeer("join",PEER1IP + ":" + PEER1RPCPort,
                ipv4 + dyIP,tcpProtocol + dyTcpPort,dyRpcPort);
        assertEquals(respChange.contains(checkResp), true);
    }

    /***
     * 允许变更节点类型 但不允许变更节点信息
     * 应用链销毁后 执行节点动态变更会失败
     * @throws Exception
     */
    @Test
    public void testClusterPeerWithDestroyAppLedger()throws Exception{
        String chain1 = "Sub001";
        mgToolCmd.createAppChain(PEER1IP, PEER1RPCPort, " -n " + chain1,
                " -t sm3", " -w first", " -c raft",
                ids);
        ledgerId1 = subLedger;

        sleepAndSaveInfo(SLEEPTIME/2);
        String respGetAllledger = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(true,respGetAllledger.contains("\"name\": \"" + chain1 + "\""));

        //冻结应用链
        String respDestory = mgToolCmd.destroyAppChain(PEER1IP,PEER1RPCPort," -c " + ledgerId1);
        assertEquals(respDestory.contains("send transaction success"), true);
        sleepAndSaveInfo(SLEEPTIME/2);

        testMgTool.queryPeerListNo(PEER1IP + ":" + PEER1RPCPort,0);

        String checkStr = "off-time not support service";

        //不可退出
        String respQuit = mgToolCmd.quitPeer(PEER1IP + ":" + PEER1RPCPort,PEER2IP);
        assertEquals(true,respQuit.contains(checkStr));

        testMgTool.queryPeerListNo(PEER1IP + ":" + PEER1RPCPort,0);

        //不可变更
        String respChange = mgToolCmd.addPeer("observer",PEER1IP + ":" + PEER1RPCPort,
                ipv4 + PEER2IP,tcpProtocol + PEER2TCPPort,PEER2RPCPort);
        assertEquals(true,respChange.contains(checkStr));

        //不可变更
        respChange = mgToolCmd.addPeer("join",PEER1IP + ":" + PEER1RPCPort,
                ipv4 + PEER2IP,tcpProtocol + PEER2TCPPort,PEER2RPCPort);
        assertEquals(true,respChange.contains(checkStr));

    }

    /***
     * 允许变更节点类型 但不允许变更节点信息
     * 应用链冻结后 只允许执行系统交易 不允许执行其他类交易
     * @throws Exception
     */
    @Test
    public void testClusterPeerWithFrozenAppLedger()throws Exception{
        String chain1 = "Sub002";

        mgToolCmd.createAppChain(PEER1IP, PEER1RPCPort, " -n " + chain1,
                " -t sm3", " -w first", " -c raft",
                ids);
        ledgerId1 = subLedger;

        sleepAndSaveInfo(SLEEPTIME/2);
        String respGetAllledger = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(true,respGetAllledger.contains("\"name\": \"" + chain1 + "\""));

        //冻结应用链
        String respDestory = mgToolCmd.freezeAppChain(PEER1IP,PEER1RPCPort," -c " + ledgerId1);
        assertEquals(respDestory.contains("send transaction success"), true);
        sleepAndSaveInfo(SLEEPTIME/2);

        testMgTool.queryPeerListNo(PEER1IP + ":" + PEER1RPCPort,3);

        String checkStr = "success";

        //可退出
        String respQuit = mgToolCmd.quitPeer(PEER1IP + ":" + PEER1RPCPort,PEER2IP);
        assertEquals(true,respQuit.contains(checkStr));
        sleepAndSaveInfo(SLEEPTIME/2);
        testMgTool.queryPeerListNo(PEER1IP + ":" + PEER1RPCPort,2);

        //可变更
        String respChange = mgToolCmd.addPeer("observer",PEER1IP + ":" + PEER1RPCPort,
                ipv4 + PEER2IP,tcpProtocol + PEER2TCPPort,PEER2RPCPort);
        assertEquals(true,respChange.contains(checkStr));
        sleepAndSaveInfo(SLEEPTIME/2);

        testMgTool.queryPeerListNo(PEER1IP + ":" + PEER1RPCPort,3);

        //可变更
        respChange = mgToolCmd.addPeer("join",PEER1IP + ":" + PEER1RPCPort,
                ipv4 + PEER2IP,tcpProtocol + PEER2TCPPort,PEER2RPCPort);
        assertEquals(true,respChange.contains(checkStr));
        sleepAndSaveInfo(SLEEPTIME/2);

        testMgTool.queryPeerListNo(PEER1IP + ":" + PEER1RPCPort,3);

    }

    /***
     * 允许变更节点类型 但不允许变更节点信息
     * 应用链冻结后恢复
     * @throws Exception
     */
    @Test
    public void testClusterPeerWithRecoverFrozenAppLedger()throws Exception{
        String chain1 = "Sub003";

        mgToolCmd.createAppChain(PEER1IP, PEER1RPCPort, " -n " + chain1,
                " -t sm3", " -w first", " -c raft",
                ids);
        ledgerId1 = subLedger;

        sleepAndSaveInfo(SLEEPTIME/2);
        String respGetAllledger = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(true,respGetAllledger.contains("\"name\": \"" + chain1 + "\""));

        //冻结应用链
        String respDestory = mgToolCmd.freezeAppChain(PEER1IP,PEER1RPCPort," -c " + ledgerId1);
        assertEquals(respDestory.contains("send transaction success"), true);
        sleepAndSaveInfo(SLEEPTIME/2);

        //恢复冻结应用链
        String respRecover = mgToolCmd.freezeAppChain(PEER1IP,PEER1RPCPort," -c " + ledgerId1);
        assertEquals(respRecover.contains("send transaction success"), true);
        sleepAndSaveInfo(SLEEPTIME/2);

        String getMsg = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
        assertEquals(true,getMsg.contains("\"state\": 1"));

        testMgTool.queryPeerListNo(PEER1IP + ":" + PEER1RPCPort,3);

        String checkStr = "success";

        //可退出
        String respQuit = mgToolCmd.quitPeer(PEER1IP + ":" + PEER1RPCPort,PEER2IP);
        assertEquals(true,respQuit.contains(checkStr));
        sleepAndSaveInfo(SLEEPTIME/2);
        testMgTool.queryPeerListNo(PEER1IP + ":" + PEER1RPCPort,2);

        //可变更
        String respChange = mgToolCmd.addPeer("observer",PEER1IP + ":" + PEER1RPCPort,
                ipv4 + PEER2IP,tcpProtocol + PEER2TCPPort,PEER2RPCPort);
        assertEquals(true,respChange.contains(checkStr));
        sleepAndSaveInfo(SLEEPTIME/2);

        testMgTool.queryPeerListNo(PEER1IP + ":" + PEER1RPCPort,3);

        //可变更
        respChange = mgToolCmd.addPeer("join",PEER1IP + ":" + PEER1RPCPort,
                ipv4 + PEER2IP,tcpProtocol + PEER2TCPPort,PEER2RPCPort);
        assertEquals(true,respChange.contains(checkStr));
        sleepAndSaveInfo(SLEEPTIME/2);

        testMgTool.queryPeerListNo(PEER1IP + ":" + PEER1RPCPort,3);

    }

    /***
     * 允许变更节点类型 但不允许变更节点信息
     * 应用链活跃
     * @throws Exception
     */
    @Test
    public void testClusterPeerWithActiveAppLedger()throws Exception{
        String chain1 = "Sub004";

        mgToolCmd.createAppChain(PEER1IP, PEER1RPCPort, " -n " + chain1,
                " -t sm3", " -w first", " -c raft",
                ids);
        ledgerId1 = subLedger;

        sleepAndSaveInfo(SLEEPTIME/2);

        testMgTool.queryPeerListNo(PEER1IP + ":" + PEER1RPCPort,3);

        String checkStr = "success";

        //可退出
        String respQuit = mgToolCmd.quitPeer(PEER1IP + ":" + PEER1RPCPort,PEER2IP);
        assertEquals(true,respQuit.contains(checkStr));
        sleepAndSaveInfo(SLEEPTIME);
        testMgTool.queryPeerListNo(PEER1IP + ":" + PEER1RPCPort,2);

        //不可变更 因区块链网络已经无法完成共识机制 交易无法正常上链
        String respChange = mgToolCmd.addPeer("observer",PEER1IP + ":" + PEER1RPCPort,
                ipv4 + PEER2IP,tcpProtocol + PEER2TCPPort,PEER2RPCPort);
        assertEquals(true,respChange.contains(checkStr));
        sleepAndSaveInfo(SLEEPTIME);

        testMgTool.queryPeerListNo(PEER1IP + ":" + PEER1RPCPort,3);

        //不可变更 因区块链网络已经无法完成共识机制 交易无法正常上链
        respChange = mgToolCmd.addPeer("join",PEER1IP + ":" + PEER1RPCPort,
                ipv4 + PEER2IP,tcpProtocol + PEER2TCPPort,PEER2RPCPort);
        assertEquals(true,respChange.contains(checkStr));
        sleepAndSaveInfo(SLEEPTIME);

        testMgTool.queryPeerListNo(PEER1IP + ":" + PEER1RPCPort,3);

    }

    /***
     * 只存在两个共识节点时 不允许退出节点或者变更共识节点为数据节点
     * 应用链活跃
     * @throws Exception
     */
    @Test
    public void testClusterPeer2WithActiveAppLedger()throws Exception{
        String chain1 = "Sub005";

        mgToolCmd.createAppChain(PEER1IP, PEER1RPCPort, " -n " + chain1,
                " -t sm3", " -w first", " -c raft",
                " -m " + id2 + "," + id1);
        ledgerId1 = subLedger;

        sleepAndSaveInfo(SLEEPTIME/2);

        testMgTool.queryPeerListNo(PEER1IP + ":" + PEER1RPCPort,2);

        String checkStr = "error";

        //不可退出
        String respQuit = mgToolCmd.quitPeer(PEER1IP + ":" + PEER1RPCPort,PEER2IP);
        assertEquals(true,respQuit.contains(checkStr));
        sleepAndSaveInfo(SLEEPTIME/2);
        testMgTool.queryPeerListNo(PEER1IP + ":" + PEER1RPCPort,2);

        //不可变更为数据节点
        String respChange = mgToolCmd.addPeer("observer",PEER1IP + ":" + PEER1RPCPort,
                ipv4 + PEER2IP,tcpProtocol + PEER2TCPPort,PEER2RPCPort);
        assertEquals(true,respChange.contains(checkStr));
        sleepAndSaveInfo(SLEEPTIME/2);

        testMgTool.queryPeerListNo(PEER1IP + ":" + PEER1RPCPort,2);

    }

    /***
     * 向应用链添加不在节点集群中的节点
     * 应用链活跃
     * @throws Exception
     */
    @Test
    public void addPeerNotInMemberList()throws Exception{
        String chain1 = "Sub005";

        mgToolCmd.createAppChain(PEER1IP, PEER1RPCPort, " -n " + chain1,
                " -t sm3", " -w first", " -c raft",
                " -m " + id2 + "," + id1);
        ledgerId1 = subLedger;

        sleepAndSaveInfo(SLEEPTIME/2);

        testMgTool.queryPeerListNo(PEER1IP + ":" + PEER1RPCPort,2);

        String checkStr = "error";

        //添加不在集群中的节点
        String respChange = mgToolCmd.addPeer("join",PEER1IP + ":" + PEER1RPCPort,
                ipv4 + PEER3IP,tcpProtocol + PEER3TCPPort,PEER3RPCPort);
//        assertEquals(true,respChange.contains(checkStr));
        sleepAndSaveInfo(SLEEPTIME/2);

        testMgTool.queryPeerListNo(PEER1IP + ":" + PEER1RPCPort,2);

    }


    public void createComplexSubledgers()throws Exception{
        String chain1 = "1735Sub1";//A/B/C节点将要销毁应用链
        String chain2 = "1735Sub2";//B/C节点将要销毁应用链
        String chain3 = "1735Sub3";//A/B节点将要冻结应用链
        String chain4 = "1735Sub4";//A/B节点 活跃应用链

        ledgerId1 = "";
        ledgerId2 = "";
        ledgerId3 = "";
        ledgerId4 = "";

        mgToolCmd.createAppChain(PEER1IP, PEER1RPCPort, " -n " + chain1,
                " -t sm3", " -w first", " -c raft",
                ids);
        ledgerId1 = subLedger;
        mgToolCmd.createAppChain(PEER1IP, PEER1RPCPort, " -n " + chain2,
                " -t sm3", " -w first", " -c raft",
                " -m " + id2 + "," + id3);
        ledgerId2 = subLedger;
        mgToolCmd.createAppChain(PEER1IP, PEER1RPCPort, " -n " + chain3,
                " -t sm3", " -w first", " -c raft",
                " -m " + id2 + "," + id1);
        ledgerId3 = subLedger;
        mgToolCmd.createAppChain(PEER1IP, PEER1RPCPort, " -n " + chain4,
                " -t sm3", " -w first", " -c raft",
                " -m " + id2+ "," + id1);
        ledgerId4 = subLedger;
        sleepAndSaveInfo(SLEEPTIME);
        String respGetAllledger = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(true,respGetAllledger.contains("\"name\": \"" + chain1 + "\""));
        assertEquals(true,respGetAllledger.contains("\"name\": \"" + chain2 + "\""));
        assertEquals(true,respGetAllledger.contains("\"name\": \"" + chain3 + "\""));
        assertEquals(true,respGetAllledger.contains("\"name\": \"" + chain4 + "\""));

        //冻结应用链
        String respDestory = mgToolCmd.destroyAppChain(PEER1IP,PEER1RPCPort," -c " + ledgerId1);
        assertEquals(respDestory.contains("send transaction success"), true);
        respDestory = mgToolCmd.destroyAppChain(PEER1IP,PEER2IP + ":" + PEER2RPCPort," -c " + ledgerId2);
        assertEquals(respDestory.contains("send transaction success"), true);
        sleepAndSaveInfo(SLEEPTIME);
        //检查应用链状态正确
        respDestory = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -c " + ledgerId1);
        assertEquals(respDestory.contains(ledgerStateDestroy), true);
        respDestory = mgToolCmd.getAppChain(PEER1IP,PEER2IP + ":" + PEER2RPCPort," -c " + ledgerId2);
        assertEquals(respDestory.contains(ledgerStateDestroy), true);


        //销毁应用链
        String respFreeze = mgToolCmd.freezeAppChain(PEER1IP,PEER1RPCPort," -c " + ledgerId3);
        assertEquals(respFreeze.contains("send transaction success"), true);
        sleepAndSaveInfo(SLEEPTIME);
        //检查应用链状态正确
        respFreeze = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -c " + ledgerId3);
        assertEquals(respFreeze.contains(ledgerStateFreeze), true);
    }

}
