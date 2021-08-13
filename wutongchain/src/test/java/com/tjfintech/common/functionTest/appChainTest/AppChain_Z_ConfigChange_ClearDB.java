package com.tjfintech.common.functionTest.appChainTest;

import com.alibaba.fastjson.JSON;
import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.MgToolCmd;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.functionTest.mixTestWithConfigChange.TestMgTool;
import com.tjfintech.common.utils.Shell;
import com.tjfintech.common.utils.SubLedgerCmd;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.*;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassApp.*;
import static com.tjfintech.common.utils.UtilsClassApp.glbChain02;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class AppChain_Z_ConfigChange_ClearDB {

    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();

    MgToolCmd mgToolCmd = new MgToolCmd();
    TestMgTool testMgTool = new TestMgTool();
    SubLedgerCmd subLedgerCmd = new SubLedgerCmd();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();


    @BeforeClass
    public static void clearPeerDB()throws Exception{
        UtilsClass utilsClass = new UtilsClass();
        //设置节点 清空db数据 并重启
        utilsClass.setAndRestartPeerList(clearPeerDB);
        //重启SDK
        utilsClass.setAndRestartSDK();
    }

    @Before
    public void beforeConfig() throws Exception {
        //设置节点 清空db数据 并重启
//        utilsClass.setAndRestartPeerList(clearPeerDB);
        //重启SDK
//        utilsClass.setAndRestartSDK();

        AppChain_CommonFunc cf = new AppChain_CommonFunc();
        cf.createTwoAppChain(glbChain01,glbChain02);

        commonFunc.setPeerConfig(PEER3IP);
        shExeAndReturn(PEER3IP,killPeerCmd);

        urlAddr = "";
    }

    /***
     * 启动新节点 新节点配置文件中配置自己和任意n个现有网络中的节点
     * 应用链活跃
     * @throws Exception
     */
    @Test
    public void addPeerInMemberList()throws Exception{
        String chain1 = "Sub005";

        mgToolCmd.createAppChain(PEER1IP, PEER1RPCPort, " -n " + chain1,
                " -t sm3", " -w first", " -c raft",
                " -m " + id2 + "," + id1);
//        tempLedgerId1 = subLedger;

        sleepAndSaveInfo(SLEEPTIME/2);

        testMgTool.queryPeerListNo(PEER1IP + ":" + PEER1RPCPort,2);

        //设置被添加新节点config.toml包含自己的信息
        commonFunc.setPeerConfig(PEER3IP);
        commonFunc.addPeerCluster(PEER3IP,PEER3IP,PEER3TCPPort,"0",ipv4,tcpProtocol);

        shExeAndReturn(PEER3IP,startPeerCmd);//启动新节点
        sleepAndSaveInfo(SLEEPTIME);

        String checkStr = "success";

        //添加不在集群中的新节点
        String respChange = mgToolCmd.addPeer("join",PEER1IP + ":" + PEER1RPCPort,
                ipv4 + PEER3IP,tcpProtocol + PEER3TCPPort,PEER3RPCPort);
        assertEquals(true,respChange.contains(checkStr));
        sleepAndSaveInfo(SLEEPTIME/2);



        testMgTool.queryPeerListNo(PEER1IP + ":" + PEER1RPCPort,3);

        String heightPeer1 = mgToolCmd.queryBlockHeight(PEER1IP+ ":" + PEER1RPCPort);
        String heightPeer3 = mgToolCmd.queryBlockHeight(PEER1IP,PEER3IP + ":" + PEER3RPCPort);
        assertEquals(heightPeer1,heightPeer3);
    }


    //加入节点后，使用新节点创建新的应用链
    @Test
    public void createAppChainWithLaterJoinPeer()throws Exception{
        //启动新加入节点3
        commonFunc.addPeerCluster(PEER3IP,PEER3IP,PEER3TCPPort,"0",ipv4,tcpProtocol);
        shExeAndReturn(PEER3IP,startPeerCmd);
        sleepAndSaveInfo(SLEEPTIME);

        //创建应用链01 包含节点A、B、C
        String chainName1 = "tc1537_B" + sdf.format(dt) + RandomUtils.nextInt(1000);
        String res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n " + chainName1,
                " -t sm3"," -w first",
                " -c raft",ids + "," + getPeerId(PEER3IP,USERNAME,PASSWD));

        sleepAndSaveInfo(SLEEPTIME/2);
        assertEquals(4,subLedgerCmd.getLedgerMemNo(subLedger));//动态加入节点前检查节点集群信息

        //检查可以获取应用链列表 存在其他应用链
        String resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName1), true);


        String Data = "tc1537 tx1 test";

        sleepAndSaveInfo(SLEEPTIME/2);
        //检查可以获取应用链列表 存在其他应用链
        resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName1), true);

        String response1=store.CreateStore(Data);
        String txHash1= commonFunc.getTxHash(response1,utilsClass.sdkGetTxHashType21);

        sleepAndSaveInfo(SLEEPTIME/2);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash1)).getString("state"));  //确认可以c查询成功


        //销毁应用链 以便恢复集群（退出动态加入的节点）
        mgToolCmd.destroyAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
        sleepAndSaveInfo(SLEEPTIME/2);
        resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
        assertEquals(resp.contains(ledgerStateDestroy), true);

    }

    //测试使用数据节点作为应用链集群节点时是否能够创建 预期是能创建
    //bug未修复 3.2迭代修复
    @Test
    public void appChainWithDataPeer()throws Exception{

        //启动新加入节点3
        commonFunc.addPeerCluster(PEER3IP,PEER3IP,PEER3TCPPort,"0",ipv4,tcpProtocol);
        shExeAndReturn(PEER3IP,startPeerCmd);
        sleepAndSaveInfo(SLEEPTIME);

        //创建应用链01 包含节点A、B、C
        String chainName1 = "tc1537_A" + sdf.format(dt) + RandomUtils.nextInt(1000);
        String res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n " + chainName1,
                " -t sm3"," -w first",
                " -c raft",ids + "," + getPeerId(PEER3IP,USERNAME,PASSWD));

        sleepAndSaveInfo(SLEEPTIME/2);
        assertEquals(3,subLedgerCmd.getLedgerMemNo(subLedger));//动态加入节点前检查节点集群信息


        mgToolCmd.addPeer("observer",PEER1IP + ":" + PEER1RPCPort,
                ipv4 + PEER3IP,tcpProtocol + PEER3TCPPort,PEER3RPCPort);
        sleepAndSaveInfo(SLEEPTIME);
        //加入后节点集群信息检查
        assertEquals(4,subLedgerCmd.getLedgerMemNo(subLedger));//动态加入节点前检查节点集群信息

        //检查可以获取应用链列表 存在其他应用链
        String resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName1), true);

        testMgTool.queryPeerListNo(PEER1IP + ":" + PEER1RPCPort,4);

        String Data = "tx1 test";

        sleepAndSaveInfo(SLEEPTIME/2);
        //检查可以获取应用链列表 存在其他应用链
        resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName1), true);

        String response1=store.CreateStore(Data);
        String txHash1= commonFunc.getTxHash(response1,utilsClass.sdkGetTxHashType21);

        sleepAndSaveInfo(SLEEPTIME/2);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash1)).getString("state"));  //确认可以c查询成功

        String heightPeer1 = mgToolCmd.queryBlockHeight(PEER1IP+ ":" + PEER1RPCPort);
        String heightPeer3 = mgToolCmd.queryBlockHeight(PEER1IP,PEER3IP + ":" + PEER3RPCPort);
        assertEquals(heightPeer1,heightPeer3);

        //销毁应用链
        mgToolCmd.destroyAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
        sleepAndSaveInfo(SLEEPTIME/2);
        resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
        assertEquals(resp.contains(ledgerStateDestroy), true);
    }


    //检查两个节点的应用链停止其中一个节点时能否正常打包交易 预期是不可以 不满足共识条件
    @Test
    public void appChainStatusCheckStopOnePeer()throws Exception{
        //创建应用链，包含两个节点
        String chainName = "tc1523_01";
        String res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n "+chainName,
                " -t sm3"," -w first"," -c raft",
                " -m " + id1 + "," + id2);

        sleepAndSaveInfo(SLEEPTIME);
        //检查可以获取应用链列表 存在其他应用链
        String resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), true);
        assertEquals(resp.contains(glbChain01), true);

        urlAddr = PEER1IP + ":" + PEER1RPCPort;//仅向应用链中存在的节点发送交易  否则交易可能会发向应用链中不包含的节点

        String response1 = store.CreateStore("tc1523 data");
        assertEquals("200",JSONObject.fromObject(response1).getString("state"));  //确认可以发送成功

        sleepAndSaveInfo(SLEEPTIME/2);

        String txHash1 = commonFunc.getTxHash(response1,utilsClass.sdkGetTxHashType21);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash1)).getString("state"));  //确认可以c查询成功

        //停止节点id2
       shExeAndReturn(PEER2IP,killPeerCmd);

        sleepAndSaveInfo(SLEEPTIME);
        String response2 = store.CreateStore("tc1523 data2");
        assertEquals("200",JSONObject.fromObject(response2).getString("state"));  //确认可以发送成功
        sleepAndSaveInfo(SLEEPTIME);
        String txHash2 = commonFunc.getTxHash(response2,utilsClass.sdkGetTxHashType21);
        assertEquals("404",JSONObject.fromObject(store.GetTxDetail(txHash2)).getString("state"));  //确认不可以c查询成功

        shExeAndReturn(PEER2IP,startPeerCmd);

        urlAddr = "";
    }

    //重启之后确认主应用链是否可以正常发送交易
    //旨在测试应用链是否能够正常启动
//    @Test
    public void TC1608_1620_restartPeer()throws Exception{
        utilsClass.setAndRestartPeerList();
        sleepAndSaveInfo(SLEEPTIME*2);
        subLedgerCmd.sendTxToMultiActiveChain("tc1608 data",globalAppId1,globalAppId2);
    }

    //测试应用链包含的节点在应用链创建前停止能否正常创建应用链
//    @Test
    public void TC1726_createWithStopPeer()throws Exception{

        shExeAndReturn(PEER2IP,killPeerCmd);
        String chainName = "tc1726_01";
        String res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n "+chainName,
                " -t sm3"," -w first"," -c raft",
                ids);

        sleepAndSaveInfo(SLEEPTIME);
        //检查可以获取应用链列表 存在其他应用链
        String resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains(chainName), true);

        shExeAndReturn(PEER2IP,startPeerCmd);

    }
}
