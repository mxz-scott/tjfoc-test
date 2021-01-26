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


    @Before
    public void beforeConfig() throws Exception {
        //设置节点 清空db数据 并重启
        utilsClass.setAndRestartPeerList(clearPeerDB);
        //重启SDK
        utilsClass.setAndRestartSDK();


        AppChain_CommonFunc cf = new AppChain_CommonFunc();
        cf.createTwoAppChain(glbChain01,glbChain02);

        commonFunc.setPeerConfig(PEER1IP);
        commonFunc.setPeerConfig(PEER3IP);
    }

    @AfterClass
    public static void afterTestReset() throws Exception {
        CommonFunc commonFunc = new CommonFunc();
        commonFunc.setPeerConfig(PEER1IP);
        commonFunc.setPeerConfig(PEER3IP);

        shExeAndReturn(PEER3IP,killPeerCmd);
        shExeAndReturn(PEER1IP,killPeerCmd);
        shExeAndReturn(PEER1IP,startCPeerCmd);
        sleepAndSaveInfo(SLEEPTIME);
    }



    /***
     * 修改节点config.toml中的集群信息 添加新节点
     * 应用链活跃
     * @throws Exception
     */
    @Test
    public void addPeerAddInMemberList()throws Exception{
        shExeAndReturn(PEER3IP,killPeerCmd);//停止节点
        String chain1 = "Sub005";

        mgToolCmd.createAppChain(PEER1IP, PEER1RPCPort, " -n " + chain1,
                " -t sm3", " -w first", " -c raft",
                " -m " + id2 + "," + id1);
//        tempLedgerId1 = subLedger;

        sleepAndSaveInfo(SLEEPTIME/2);

        testMgTool.queryPeerListNo(PEER1IP + ":" + PEER1RPCPort,2);

        //修改节点1配置文件 新增节点3的集群信息
        shExeAndReturn(PEER1IP,killPeerCmd);//停止节点
        CommonFunc cf = new CommonFunc();
        cf.addPeerCluster(PEER1IP,PEER3IP,PEER3TCPPort,"0",ipv4,tcpProtocol);//添加第4个节点信息
        shExeAndReturn(PEER1IP,startCPeerCmd);//配置文件方式启动节点

        sleepAndSaveInfo(SLEEPTIME);

        String checkStr = "success";

        //添加不在集群中的节点
        String respChange = mgToolCmd.addPeer("join",PEER1IP + ":" + PEER1RPCPort,
                ipv4 + PEER3IP,tcpProtocol + PEER3TCPPort,PEER3RPCPort);
        assertEquals(true,respChange.contains(checkStr));
        sleepAndSaveInfo(SLEEPTIME/2);

        testMgTool.queryPeerListNo(PEER1IP + ":" + PEER1RPCPort,3);


        shExeAndReturn(PEER3IP,startCPeerCmd);//启动加入的节点
        sleepAndSaveInfo(SLEEPTIME);

        String heightPeer1 = mgToolCmd.queryBlockHeight(PEER1IP+ ":" + PEER1RPCPort);
        String heightPeer3 = mgToolCmd.queryBlockHeight(PEER1IP,PEER3IP + ":" + PEER3RPCPort);
        assertEquals(heightPeer1,heightPeer3);
    }


    //加入节点后，使用新节点创建新的应用链
    @Test
    public void createAppChainWithLaterPeer()throws Exception{
        assertEquals(3,subLedgerCmd.getLedgerMemNo(""));//加入节点前检查节点集群信息
        shExeAndReturn(PEER1IP,killPeerCmd);
        commonFunc.addPeerCluster(PEER1IP,PEER3IP,PEER3TCPPort,"0",ipv4,tcpProtocol);

        //启动节点1和新加入节点3
        shExeAndReturn(PEER1IP,startCPeerCmd);
        shExeAndReturn(PEER3IP,startPeerCmd);
        sleepAndSaveInfo(SLEEPTIME);

        assertEquals(4,subLedgerCmd.getLedgerMemNo(""));//动态加入节点前检查节点集群信息
        //创建子链01 包含节点A、B、C
        String chainName1 = "tc1537_" + sdf.format(dt) + RandomUtils.nextInt(1000);
        String res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n " + chainName1,
                " -t sm3"," -w first",
                " -c raft",ids + "," + getPeerId(PEER3IP,USERNAME,PASSWD));

        sleepAndSaveInfo(SLEEPTIME/2);
        assertEquals(4,subLedgerCmd.getLedgerMemNo(subLedger));//动态加入节点前检查节点集群信息

        //检查可以获取子链列表 存在其他子链
        String resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName1), true);


        String Data = "tc1537 tx1 test";

        sleepAndSaveInfo(SLEEPTIME/2);
        //检查可以获取子链列表 存在其他子链
        resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName1), true);

        String response1=store.CreateStore(Data);
        String txHash1= commonFunc.getTxHash(response1,utilsClass.sdkGetTxHashType21);

        sleepAndSaveInfo(SLEEPTIME/2);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash1)).getString("state"));  //确认可以c查询成功


        //销毁子链 以便恢复集群（退出动态加入的节点）
        mgToolCmd.destroyAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
        sleepAndSaveInfo(SLEEPTIME/2);
        resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
        assertEquals(resp.contains(ledgerStateDestroy), true);

    }

    //测试使用数据节点作为子链集群节点时是否能够创建 预期是能创建
    @Test
    public void createChainWithDataPeer()throws Exception{
        assertEquals(3,subLedgerCmd.getLedgerMemNo(""));//加入节点前检查节点集群信息
        shExeAndReturn(PEER1IP,killPeerCmd);
        commonFunc.addPeerCluster(PEER1IP,PEER3IP,PEER3TCPPort,"1",ipv4,tcpProtocol);

        //启动节点1和新加入节点3
        shExeAndReturn(PEER1IP,startCPeerCmd);
        shExeAndReturn(PEER3IP,startPeerCmd);
        sleepAndSaveInfo(SLEEPTIME);

        assertEquals(4,subLedgerCmd.getLedgerMemNo(""));//动态加入节点前检查节点集群信息
        //创建子链01 包含节点A、B、C
        String chainName1 = "tc1537_" + sdf.format(dt) + RandomUtils.nextInt(1000);
        String res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n " + chainName1,
                " -t sm3"," -w first",
                " -c raft",ids + "," + getPeerId(PEER3IP,USERNAME,PASSWD));

        sleepAndSaveInfo(SLEEPTIME/2);
        assertEquals(4,subLedgerCmd.getLedgerMemNo(subLedger));//动态加入节点前检查节点集群信息

        //检查可以获取子链列表 存在其他子链
        String resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName1), true);


        String Data = "tx1 test";

        sleepAndSaveInfo(SLEEPTIME/2);
        //检查可以获取子链列表 存在其他子链
        resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName1), true);

        String response1=store.CreateStore(Data);
        String txHash1= commonFunc.getTxHash(response1,utilsClass.sdkGetTxHashType21);

        sleepAndSaveInfo(SLEEPTIME/2);
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash1)).getString("state"));  //确认可以c查询成功


        //销毁子链 以便恢复集群（退出动态加入的节点）
        mgToolCmd.destroyAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
        sleepAndSaveInfo(SLEEPTIME/2);
        resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -c " + subLedger);
        assertEquals(resp.contains(ledgerStateDestroy), true);
    }


    //检查两个节点的子链停止其中一个节点时能否正常打包交易 预期是不可以 不满足共识条件
    @Test
    public void TC1523_subChainStatus()throws Exception{
        //配置sdk节点集群仅为Peer1并重启sdk
//        setSDKOnePeer(utilsClass.getIPFromStr(SDKADD),PEER1IP + ":" + PEER1RPCPort,"true");
//        commonFunc.setAndRestartSDK();
        //创建子链，包含两个节点
        String chainName = "tc1523_01";
        String res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n "+chainName,
                " -t sm3"," -w first"," -c raft",
                " -m " + id1 + "," + id2);

        sleepAndSaveInfo(SLEEPTIME);
        //检查可以获取子链列表 存在其他子链
        String resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), true);
        assertEquals(resp.contains(glbChain01), true);

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
    }

    //重启之后确认主子链是否可以正常发送交易
    //旨在测试子链是否能够正常启动
    @Test
    public void TC1608_1620_restartPeer()throws Exception{
        utilsClass.setAndRestartPeerList();
        subLedgerCmd.sendTxToMultiActiveChain("tc1608 data",globalAppId1,globalAppId2);
    }

    //测试子链包含的节点在子链创建前停止能否正常创建子链
    @Test
    public void TC1726_createWithStopPeer()throws Exception{
        testMgTool.queryPeerListNo(PEER1IP + ":" + PEER1RPCPort,3);

        shExeAndReturn(PEER2IP,killPeerCmd);
        String chainName = "tc1726_01";
        String res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n "+chainName,
                " -t sm3"," -w first"," -c raft",
                ids);

        sleepAndSaveInfo(SLEEPTIME);
        //检查可以获取子链列表 存在其他子链
        String resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains(chainName), true);

        shExeAndReturn(PEER2IP,startPeerCmd);

    }
}
