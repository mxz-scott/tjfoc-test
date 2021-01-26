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
        AppChain_CommonFunc cf = new AppChain_CommonFunc();
        cf.createTwoAppChain(glbChain01,glbChain02);
    }


    @Test
    public void TC1538_quitMainJoinPeer()throws Exception{
        //配置sdk节点集群仅为Peer1并重启sdk
        commonFunc.setSDKOnePeer(utilsClass.getIPFromStr(SDKADD),PEER1IP + ":" + PEER1RPCPort,"true",peerTLSServerName);
        utilsClass.setAndRestartSDK();
        //创建子链01 包含节点A、B、C
        String chainName1="tc1538_01";
        String res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n "+chainName1,
                " -t sm3"," -w first"," -c raft",
                ids);
        assertEquals(res.contains("send transaction success"), true);

        //创建子链02 包含节点A、C
        String chainName2="tc1538_02";
        String res2 = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n "+chainName2,
                " -t sm3"," -w first"," -c raft",
                " -m "+id1+","+id3);
        assertEquals(res2.contains("send transaction success"), true);


        //创建子链03 包含节点A、B
        String chainName3="tc1538_03";
        String res3 = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n "+chainName3,
                " -t sm3"," -w first"," -c raft",
                " -m "+id1+","+id2);
        assertEquals(res3.contains("send transaction success"), true);


        sleepAndSaveInfo(SLEEPTIME*2);
        //检查可以获取子链列表 存在其他子链
        String resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName1), true);
        assertEquals(resp.contains(chainName2), true);
        assertEquals(resp.contains(chainName3), true);


        String Data = "tc1538 tx1 test";
        //动态删除节点B，因已有子链使用 无法成功删除
        String respQuit = mgToolCmd.quitPeer(PEER1IP+":"+PEER1RPCPort,PEER2IP);
        assertEquals(respQuit.contains("quit failed:some ledger is using this peer"), true);

        sleepAndSaveInfo(SLEEPTIME*2);
        //检查可以获取子链列表 存在其他子链
        resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains(chainName1), true);
        assertEquals(resp.contains(chainName2), true);
        assertEquals(resp.contains(chainName3), true);

        //确认包含节点B的子链集群信息无异常
        resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -n "+chainName1);
        assertEquals(resp.contains(PEER2IP), true);
        resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -n "+chainName3);
        assertEquals(resp.contains(PEER2IP), true);

        subLedger=chainName1;
        String response1=store.CreateStore(Data);
        String txHash1= commonFunc.getTxHash(response1,utilsClass.sdkGetTxHashType21);

        subLedger=chainName2;
        Data = "tc1538 tx2 test";
        String response2=store.CreateStore(Data);
        String txHash2= commonFunc.getTxHash(response2,utilsClass.sdkGetTxHashType21);

        subLedger=chainName3;
        Data = "tc1538 tx3 test";
        String response3=store.CreateStore(Data);
        String txHash3= commonFunc.getTxHash(response3,utilsClass.sdkGetTxHashType21);


        subLedger="";
        Data = "tc1538 tx4 test";
        String response4=store.CreateStore(Data);
        String txHash4= commonFunc.getTxHash(response4,utilsClass.sdkGetTxHashType21);

        sleepAndSaveInfo(SLEEPTIME*2);
        subLedger=chainName1;
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash1)).getString("state"));  //确认可以c查询成功
        subLedger=chainName2;
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash2)).getString("state"));  //确认可以c查询成功
        subLedger=chainName3;
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash3)).getString("state"));  //确认可以c查询成功
        subLedger="";
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash4)).getString("state"));  //确认不可以c查询成功
    }


    //测试动态加入节点后，使用动态加入的节点创建子链
    @Test
    public void TC1537_2144_createChainWithJoinPeer()throws Exception{
        assertEquals(3,subLedgerCmd.getLedgerMemNo(glbChain01));//动态加入节点前检查节点集群信息

        commonFunc.setPeerConfig(PEER3IP);//设置Peer3 config.toml文件为不包含自己节点信息的配置文件 20191219确认不用配置自己的信息
        commonFunc.addPeerCluster(PEER3IP,PEER3IP,PEER3TCPPort,"0",ipv4,tcpProtocol);
        //动态加入节点168
        String resp2 = mgToolCmd.addPeer("join",PEER1IP+":"+PEER1RPCPort,
                ipv4+PEER3IP,tcpProtocol + PEER3TCPPort,PEER3RPCPort);
        assertEquals(true,resp2.contains("success"));
        sleepAndSaveInfo(SLEEPTIME);

        assertEquals(3,subLedgerCmd.getLedgerMemNo(glbChain01));//动态加入节点前检查节点集群信息
        //创建子链01 包含节点A、B、C
        String chainName1="tc1537_"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        String res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n "+chainName1,
                " -t sm3"," -w first",
                " -c raft",ids+","+getPeerId(PEER3IP,USERNAME,PASSWD));
        assertEquals(res.contains("send transaction success"), true);


        sleepAndSaveInfo(SLEEPTIME*2);
        assertEquals(4,subLedgerCmd.getLedgerMemNo(chainName1));//动态加入节点前检查节点集群信息

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

        subLedger=chainName1;
        String response1=store.CreateStore(Data);
        String txHash1= commonFunc.getTxHash(response1,utilsClass.sdkGetTxHashType21);


        subLedger="";
        Data = "tc1538 tx4 test";
        String response4=store.CreateStore(Data);
        String txHash4= commonFunc.getTxHash(response4,utilsClass.sdkGetTxHashType21);

        sleepAndSaveInfo(SLEEPTIME*2);
        subLedger=chainName1;
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash1)).getString("state"));  //确认可以c查询成功
        subLedger="";
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash4)).getString("state"));  //确认不可以c查询成功

        //销毁子链 以便恢复集群（退出动态加入的节点）
        mgToolCmd.destroyAppChain(PEER1IP,PEER1RPCPort," -n "+chainName1);
        sleepAndSaveInfo(SLEEPTIME*3/2);
        resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort," -n "+chainName1);
        assertEquals(resp.contains(ledgerStateDestroy), true);

        //恢复节点
        mgToolCmd.quitPeer(PEER1IP+":"+PEER1RPCPort,PEER3IP);
        //停止节点id3
        Shell shell=new Shell(PEER3IP,USERNAME,PASSWD);
        shell.execute(killPeerCmd);
        ArrayList<String> stdout = shell.getStandardOutput();
        log.info(StringUtils.join(stdout,"\n"));

    }

    //测试节点变更与子链的关系
    //若节点存在与非销毁的子链中时是不允许动态变更
    @Test
    public void TC1771_changePeerInfo()throws Exception{
        //所有信息不变更，重复join，提示变更失败，因节点B参与子链
        String resp2 = mgToolCmd.addPeer("join",PEER1IP + ":" + PEER1RPCPort,
                ipv4 + PEER2IP,tcpProtocol + PEER2TCPPort,String.valueOf(Integer.valueOf(PEER2RPCPort) + 1));
        assertEquals(true,resp2.contains("join failed:some ledger is using this peer"));

        //变更节点B为数据节点，提示变更失败，因节点B参与子链
        resp2 = mgToolCmd.addPeer("observer",PEER1IP + ":" + PEER1RPCPort,
                ipv4 + PEER2IP,tcpProtocol + PEER2TCPPort,PEER2RPCPort);
        assertEquals(true,resp2.contains("join failed:some ledger is using this peer"));

        //变更节点B tcp端口信息，提示变更失败，因节点B参与子链
        resp2 = mgToolCmd.addPeer("join",PEER1IP + ":" + PEER1RPCPort,
                ipv4 + PEER2IP,"/tcp/60015",PEER2RPCPort);
        assertEquals(true,resp2.contains("join failed:some ledger is using this peer"));

        //变更节点B rpc端口信息，提示变更失败，因节点B参与子链
        resp2 = mgToolCmd.addPeer("join",PEER1IP + ":" + PEER1RPCPort,
                ipv4 + PEER2IP,tcpProtocol + PEER2TCPPort,String.valueOf(Integer.valueOf(PEER2RPCPort) + 1));
        assertEquals(true,resp2.contains("join failed:some ledger is using this peer"));

        String meminfo = mgToolCmd.queryMemberList(PEER1IP + ":" + PEER1RPCPort);
        assertEquals("",testMgTool.parseMemInfo(meminfo,PEER2IP,"typ"));
        assertEquals(PEER2RPCPort,testMgTool.parseMemInfo(meminfo,PEER2IP,"port"));
        assertEquals(ipv4 + PEER2IP + tcpProtocol + PEER2TCPPort,testMgTool.parseMemInfo(meminfo,PEER2IP,"inAddr"));

        subLedgerCmd.sendTxToMultiActiveChain("test1112222",globalAppId1,globalAppId2);

    }

    //测试使用数据节点作为子链集群节点时是否能够创建 预期是不能创建
    @Test
    public void TC1659_1655_createChainWithDataPeer()throws Exception{
        commonFunc.setPeerConfig(PEER3IP);//设置Peer3 config.toml文件为不包含自己节点信息的配置文件 20191219确认不用配置自己的信息
        commonFunc.addPeerCluster(PEER3IP,PEER3IP,PEER3TCPPort,"1",ipv4,tcpProtocol);
        //动态加入节点168
        String resp2 = mgToolCmd.addPeer("observer",PEER1IP+":"+PEER1RPCPort,
                ipv4+PEER3IP,tcpProtocol + PEER3TCPPort,PEER3RPCPort);
        assertEquals(true,resp2.contains("success"));
        sleepAndSaveInfo(SLEEPTIME);
        //创建子链01 包含节点A、B、C
        String chainName1="tc1659_"+sdf.format(dt)+ RandomUtils.nextInt(1000);
        String res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n "+chainName1,
                " -t sm3"," -w first",
                " -c raft",ids+","+getPeerId(PEER3IP,USERNAME,PASSWD));
        assertEquals(res.contains("is not Consensus Node"), true);

        sleepAndSaveInfo(SLEEPTIME);
        //检查可以获取子链列表 存在其他子链
        String resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName1), false);


        //恢复节点
        mgToolCmd.quitPeer(PEER1IP+":"+PEER1RPCPort,PEER3IP);
        //停止节点id3
        Shell shell=new Shell(PEER3IP,USERNAME,PASSWD);
        shell.execute(killPeerCmd);
        ArrayList<String> stdout = shell.getStandardOutput();
        log.info(StringUtils.join(stdout,"\n"));

        subLedgerCmd.sendTxToMultiActiveChain("1659 data",globalAppId1,globalAppId2);
    }


    //检查两个节点的子链停止其中一个节点时能否正常打包交易 预期是不可以 不满足共识条件
    @Test
    public void TC1523_subChainStatus()throws Exception{
        //配置sdk节点集群仅为Peer1并重启sdk
//        setSDKOnePeer(utilsClass.getIPFromStr(SDKADD),PEER1IP + ":" + PEER1RPCPort,"true");
//        commonFunc.setAndRestartSDK();
        //创建子链，包含两个节点
        String chainName="tc1523_01";
        String res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n "+chainName,
                " -t sm3"," -w first"," -c raft",
                " -m "+id1+","+id2);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME);
        //检查可以获取子链列表 存在其他子链
        String resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains("name"), true);
        assertEquals(resp.contains(chainName), true);
        assertEquals(resp.contains(glbChain01.toLowerCase()), true);

        subLedger=chainName;
        String response1 = store.CreateStore("tc1523 data");
        assertEquals("200",JSONObject.fromObject(response1).getString("state"));  //确认可以发送成功

        sleepAndSaveInfo(SLEEPTIME*2);

        String txHash1 =commonFunc.getTxHash(response1,utilsClass.sdkGetTxHashType21);;
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txHash1)).getString("state"));  //确认可以c查询成功

        //停止节点id2
        Shell shell=new Shell(PEER2IP,USERNAME,PASSWD);
        shell.execute(killPeerCmd);
        ArrayList<String> stdout = shell.getStandardOutput();
        log.info(StringUtils.join(stdout,"\n"));
//        return StringUtils.join(stdout,"\n");

        sleepAndSaveInfo(SLEEPTIME);
        String response2 = store.CreateStore("tc1523 data2");
        assertEquals("200",JSONObject.fromObject(response2).getString("state"));  //确认可以发送成功
        sleepAndSaveInfo(SLEEPTIME);
        String txHash2 =commonFunc.getTxHash(response2,utilsClass.sdkGetTxHashType21);
        assertEquals("404",JSONObject.fromObject(store.GetTxDetail(txHash2)).getString("state"));  //确认不可以c查询成功

        shell.execute(startPeerCmd);
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
        testMgTool.queryPeerListNo(PEER1IP+":"+PEER1RPCPort,3);

        shExeAndReturn(PEER2IP,killPeerCmd);
        String chainName="tc1726_01";
        String res = mgToolCmd.createAppChain(PEER1IP,PEER1RPCPort," -n "+chainName,
                " -t sm3"," -w first"," -c raft",
                ids);
        assertEquals(res.contains("send transaction success"), true);

        sleepAndSaveInfo(SLEEPTIME);
        //检查可以获取子链列表 存在其他子链
        String resp = mgToolCmd.getAppChain(PEER1IP,PEER1RPCPort,"");
        assertEquals(resp.contains(chainName), true);

        shExeAndReturn(PEER2IP,startPeerCmd);

    }


    @After
    public void resetPeerAndSDK()throws  Exception {
        utilsClass.setAndRestartPeerList(resetPeerBase);
        utilsClass.setAndRestartSDK(resetSDKConfig);
    }
}