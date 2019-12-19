package com.tjfintech.common.functionTest.mixTestWithConfigChange;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.MgToolCmd;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.Shell;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import static com.tjfintech.common.CommonFunc.addPeerCluster;
import static com.tjfintech.common.CommonFunc.setPeerConfig;
import static com.tjfintech.common.utils.UtilsClass.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AddPeerAndSyncData {
    public static final int STARTSLEEPTIME=40000;
    TestBuilder testBuilder=TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    MgToolCmd mgToolCmd = new MgToolCmd();
    TestMgTool testMgTool = new TestMgTool();

    String rpcPort=PEER3RPCPort;
    String tcpPort=PEER3TCPPort;
    String consType="\"type\": 0";
    String dataType="\"type\": 1";
    int basePeerNo = 3;
    int DynamicPeerNo = 4;
    String ipType="/ip4/";
    String tcpType="/tcp/";


    String toolPath="cd " + ToolPATH + ";";
    String peer1IPPort=PEER1IP+":"+PEER1RPCPort;
    String peer2IPPort=PEER2IP+":"+PEER2RPCPort;
    String peer3IPPort=PEER3IP+":"+PEER3RPCPort;


    public void addDataPeerConfigWithSelfInfo(){
        //设置动态加入节点config.toml文件 不带自己的配置信息
        setPeerConfig(PEER3IP);
        addPeerCluster(PEER3IP,PEER3IP,PEER3TCPPort,"1",ipv4,tcpProtocol);
    }
    public void addConsensusPeerConfigWithSelfInfo(){
        //设置动态加入节点config.toml文件 不带自己的配置信息
        setPeerConfig(PEER3IP);
        addPeerCluster(PEER3IP,PEER3IP,PEER3TCPPort,"0",ipv4,tcpProtocol);
    }

    public void addPeerConfigWithoutSelfInfo()throws Exception{
        //设置动态加入节点config.toml文件 不带自己的配置信息 其他节点数据不同步
        shellExeCmd(PEER3IP,killPeerCmd,clearPeerDB,resetPeerBase);
        setPeerConfig(PEER3IP);
    }

    //该测试用例 在BVT Run_Main_Mysql后进行测试 同步等待时间可能会很长
    //动态加入共识节点并检查数据同步
    @Test
    public void joinConsensusPeerAndCheckSyncData()throws Exception{
        addPeerConfigWithoutSelfInfo();

        Shell shellPeer3=new Shell(PEER3IP,USERNAME,PASSWD);

        //检查动态加入的共识节点，即使用管理工具加入的共识节点信息
        String resp = mgToolCmd.addPeer("join",peer1IPPort,ipType+PEER3IP,tcpType+tcpPort,rpcPort);
        assertEquals(true,resp.contains("success"));
        sleepAndSaveInfo(SLEEPTIME,"p2p communication waiting......");
        testMgTool.queryPeerListNo(peer1IPPort,DynamicPeerNo);

        //动态加入共识节点 尚未启动节点时检查节点信息
        while(!mgToolCmd.queryMemberList(PEER1IP + ":" + PEER1RPCPort).contains(PEER3IP)){
            sleepAndSaveInfo(100,"join peer waiting......");
        }
        String meminfo = mgToolCmd.queryMemberList(PEER1IP + ":" + PEER1RPCPort);//查询集群信息
        testMgTool.checkMemInfoExHeight(meminfo,PEER3IP,
                getPeerId(PEER3IP,USERNAME,PASSWD), //id信息
                "1",  //state 连接状态
                "", //版本信息
                "0", //节点rpc端口信息
                "peer168",   //节点名称
                ipType+PEER3IP+tcpType+tcpPort,  //节点inaddr信息
                ipType+PEER3IP+tcpType+tcpPort,  //节点outaddr信息
                "0",  //节点类型 共识节点还是数据节点
                "0",  //tls是否开启
                "", //hash 类型 当前默认sm3
                "" //共识算法
        );

        Thread.sleep(3000);

        shellExeCmd(PEER3IP,startPeerCmd);
        Thread.sleep(STARTSLEEPTIME);

        //节点启动后信息检查
        meminfo = mgToolCmd.queryMemberList(PEER1IP + ":" + PEER1RPCPort);//查询集群信息
        testMgTool.checkMemInfoExHeight(meminfo,PEER3IP,
                getPeerId(PEER3IP,USERNAME,PASSWD), //id信息
                "0",  //state 连接状态
                shExeAndReturn(PEER3IP,getPeerVerByShell).trim(), //版本信息
                PEER3RPCPort, //节点rpc端口信息
                "peer168",   //节点名称
                ipType+PEER3IP+tcpType+tcpPort,  //节点inaddr信息
                ipType+PEER3IP+tcpType+tcpPort,  //节点outaddr信息
                "0",  //节点类型 共识节点还是数据节点
                "0",  //tls是否开启
                "sm3", //hash 类型 当前默认sm3
                "raft" //共识算法
        );
        assertNotEquals("0",testMgTool.parseMemInfo(meminfo,PEER3IP,"height")); //不确定当前区块高度是否有新交易目前自动化仅判断非0


        testMgTool.chkPeerSimInfoOK(peer3IPPort,tcpPort,version,consType); //自己返回peer信息校验
        testMgTool.queryPeerListNo(peer1IPPort,DynamicPeerNo);
        testMgTool.queryPeerListNo(PEER3IP+":"+rpcPort,DynamicPeerNo);

        sleepAndSaveInfo(3600*1000,"sync data waiting....");

        assertEquals(mgToolCmd.queryBlockHeight(PEER1IP + ":" + PEER1RPCPort),mgToolCmd.queryBlockHeight(PEER2IP + ":" + PEER2RPCPort));
        assertEquals(mgToolCmd.queryBlockHeight(PEER1IP + ":" + PEER1RPCPort),mgToolCmd.queryBlockHeight(PEER3IP + ":" + PEER3RPCPort));
    }


    //动态加入数据节点
    @Test
    public void joinDataPeerAndCheckSyncData()throws Exception{
        addPeerConfigWithoutSelfInfo();

        Shell shellPeer3=new Shell(PEER3IP,USERNAME,PASSWD);

        //检查动态加入的共识节点，即使用管理工具加入的共识节点信息
        String resp = mgToolCmd.addPeer("observer",peer1IPPort,ipType+PEER3IP,tcpType+tcpPort,rpcPort);
        assertEquals(true,resp.contains("success"));
        sleepAndSaveInfo(SLEEPTIME,"p2p communication waiting......");
        testMgTool.queryPeerListNo(peer1IPPort,DynamicPeerNo);

        while(!mgToolCmd.queryMemberList(PEER1IP + ":" + PEER1RPCPort).contains(PEER3IP)){
            sleepAndSaveInfo(100,"observer peer waiting......");
        }

        String meminfo = mgToolCmd.queryMemberList(PEER1IP + ":" + PEER1RPCPort);//查询集群信息
        //未启动节点前检查动态加入节点信息
        testMgTool.checkMemInfoExHeight(meminfo,PEER3IP,
                getPeerId(PEER3IP,USERNAME,PASSWD), //id信息
                "1",  //state 连接状态 当前默认值为0 为类型默认值 已提优化ID1002346
                "", //版本信息
                "0", //节点rpc端口信息
                "peer168",   //节点名称
                ipType+PEER3IP+tcpType+PEER3TCPPort,  //节点inaddr信息
                ipType+PEER3IP+tcpType+PEER3TCPPort,  //节点outaddr信息
                "1",  //节点类型 共识节点还是数据节点
                "0",  //tls是否开启
                "", //hash 类型 当前默认sm3
                "" //共识算法
        );
        assertEquals("0",testMgTool.parseMemInfo(meminfo,PEER3IP,"height"));

        shellExeCmd(PEER3IP,startPeerCmd);//启动节点
        Thread.sleep(RESTARTTIME*3);//等待启动时间

        //节点启动后信息检查
        meminfo = mgToolCmd.queryMemberList(PEER1IP + ":" + PEER1RPCPort);//查询集群信息
        testMgTool.checkMemInfoExHeight(meminfo,PEER3IP,
                getPeerId(PEER3IP,USERNAME,PASSWD), //id信息
                "0",  //state 连接状态
                shExeAndReturn(PEER3IP,getPeerVerByShell).trim(), //版本信息
                PEER3RPCPort, //节点rpc端口信息
                "peer168",   //节点名称
                ipType+PEER3IP+tcpType+PEER3TCPPort,  //节点inaddr信息
                ipType+PEER3IP+tcpType+PEER3TCPPort,  //节点outaddr信息
                "1",  //节点类型 共识节点还是数据节点
                "0",  //tls是否开启
                "sm3", //hash 类型 当前默认sm3
                "raft" //共识算法
        );
        assertNotEquals("0",testMgTool.parseMemInfo(meminfo,PEER3IP,"height")); //不确定当前区块高度是否有新交易目前自动化仅判断非0

        testMgTool.chkPeerSimInfoOK(peer3IPPort,PEER3TCPPort,version,dataType);
        testMgTool.queryPeerListNo(peer1IPPort,DynamicPeerNo);
        testMgTool.queryPeerListNo(PEER3IP+":"+PEER3RPCPort,DynamicPeerNo);


        sleepAndSaveInfo(3600*1000,"sync data waiting....");

        assertEquals(mgToolCmd.queryBlockHeight(PEER1IP + ":" + PEER1RPCPort),mgToolCmd.queryBlockHeight(PEER2IP + ":" + PEER2RPCPort));
        assertEquals(mgToolCmd.queryBlockHeight(PEER1IP + ":" + PEER1RPCPort),mgToolCmd.queryBlockHeight(PEER3IP + ":" + PEER3RPCPort));

    }
}
