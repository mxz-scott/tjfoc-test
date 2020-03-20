package com.tjfintech.common.functionTest.mixTestWithConfigChange;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.MgToolCmd;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.Shell;
import lombok.extern.slf4j.Slf4j;
import org.junit.*;
import org.junit.runners.MethodSorters;

import static com.tjfintech.common.CommonFunc.*;
import static com.tjfintech.common.utils.UtilsClass.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DynamicChangePeerCluster_ClearDB {
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

    @BeforeClass
    public static void SetSleepTimeShort(){
        RESTARTTIME = 6000;
    }

    @AfterClass
    public static void SetSleepTimeDefault(){
        RESTARTTIME = 20000;
    }

    //20191219确认如果节点3未清数据 在clearDataSetPerm999后执行则会出现节点3无法同步数据的问题

    //先关闭、清空待加入节点db 再清空原集群中节点db及sdk数据库
//    public void initSetting1()throws Exception{
//
//        shellExeCmd(PEER3IP,killPeerCmd,clearPeerDB,resetPeerBase);
//        BeforeCondition bf = new BeforeCondition();
//        bf.clearDataSetPerm999();
//        mgToolCmd.quitPeer(peer1IPPort,PEER3IP);//带quit操作 验证错误操作后能够正常加入节点并处理交易
//
//        testMgTool.queryPeerListNo(peer1IPPort,basePeerNo);
//    }

    //先清空原集群中节点db及sdk数据库 再关闭、清空待加入节点db 启动节点
//    public void initSetting2()throws Exception{
//        shellExeCmd(PEER3IP,killPeerCmd,clearPeerDB,resetPeerBase,startPeerCmd);
//        BeforeCondition bf = new BeforeCondition();
//        bf.clearDataSetPerm999();
//        mgToolCmd.quitPeer(peer1IPPort,PEER3IP);//可以不用此步骤
//        shellExeCmd(PEER3IP,killPeerCmd,clearPeerDB);
//        testMgTool.queryPeerListNo(peer1IPPort,basePeerNo);
//    }

    //先清空原集群中节点db及sdk数据库 再关闭、清空待加入节点db 启动节点
//    public void initSetting3()throws Exception{
//        shellExeCmd(PEER3IP,killPeerCmd,clearPeerDB,resetPeerBase,startPeerCmd);
//        BeforeCondition bf = new BeforeCondition();
//        bf.clearDataSetPerm999();
//        mgToolCmd.quitPeer(peer1IPPort,PEER3IP);//可以不用此步骤
//        testMgTool.queryPeerListNo(peer1IPPort,basePeerNo);
//    }

    public void initSetting1NoQuit()throws Exception{
        shellExeCmd(PEER3IP,killPeerCmd,clearPeerDB,resetPeerBase);
        BeforeCondition bf = new BeforeCondition();
        bf.clearDataSetPerm999();
        testMgTool.queryPeerListNo(peer1IPPort,basePeerNo);
    }

    public void initSetting2NoQuit()throws Exception{
        shellExeCmd(PEER3IP,killPeerCmd,clearPeerDB,resetPeerBase,startPeerCmd);
        BeforeCondition bf = new BeforeCondition();
        bf.clearDataSetPerm999();
        shellExeCmd(PEER3IP,killPeerCmd,clearPeerDB);
        testMgTool.queryPeerListNo(peer1IPPort,basePeerNo);
    }


//    @Test
//    public void joinDataPeerWithSetting1()throws Exception{
//        initSetting1();
//        addDataPeerConfigWithSelfInfo();
//        joinDataPeer();
//    }
//
//    @Test
//    public void joinDataPeerWithSetting2()throws Exception{
//        initSetting2();
//        addDataPeerConfigWithSelfInfo();
//        joinDataPeer();
//    }
//
//
//    @Test
//    public void joinDataPeerWithSetting3()throws Exception{
//        initSetting3();
//        addDataPeerConfigWithSelfInfo();
//        joinDataPeer();
//    }
    @Test
    public void joinDataPeerWithSetting1NoQuit()throws Exception{
        initSetting1NoQuit();
        addDataPeerConfigWithSelfInfo();
        joinDataPeer();
    }

    @Test
    public void joinDataPeerWithSetting2NoQuit()throws Exception{
        initSetting1NoQuit();
        addDataPeerConfigWithSelfInfo();
        joinDataPeer();
    }

    @Test
    public void joinConsensusPeerWithSetting1NoQuit()throws Exception{
        initSetting1NoQuit();
        addConsensusPeerConfigWithSelfInfo();
        joinConsensusPeer();
    }

    @Test
    public void joinConsensusPeerWithSetting2NoQuit()throws Exception{
        initSetting2NoQuit();
        addConsensusPeerConfigWithSelfInfo();
        joinConsensusPeer();
    }

//    @Test
//    public void joinConsensusPeerWithSetting1()throws Exception{
//        initSetting1();
//        addConsensusPeerConfigWithSelfInfo();
//        joinConsensusPeer();
//    }
//
//    @Test
//    public void joinConsensusPeerWithSetting2()throws Exception{
//        initSetting2();
//        addConsensusPeerConfigWithSelfInfo();
//        joinConsensusPeer();
//    }
//
//    @Test
//    public void joinConsensusPeerWithSetting3()throws Exception{
//        initSetting3();
//        addConsensusPeerConfigWithSelfInfo();
//        joinConsensusPeer();
//    }

    //退出节点 节点未在当前的集群列表中 客户端报错
    @Test
    public void quitWhenNotInMemlist()throws Exception{
        shellExeCmd(PEER3IP,killPeerCmd,clearPeerDB,resetPeerBase);
        BeforeCondition bf = new BeforeCondition();
        bf.clearDataSetPerm999();
        String resp = mgToolCmd.quitPeer(peer1IPPort,PEER3IP);//带quit操作 验证错误操作后能够正常加入节点并处理交易
        assertEquals(true,resp.contains("not found"));
    }

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

    //动态加入共识节点
    public void joinConsensusPeer()throws Exception{
        Shell shellPeer3=new Shell(PEER3IP,USERNAME,PASSWD);

        //检查动态加入的共识节点，即使用管理工具加入的共识节点信息
        String resp = mgToolCmd.addPeer("join",peer1IPPort,ipType+PEER3IP,tcpType+tcpPort,rpcPort);
        assertEquals(true,resp.contains("success"));
        sleepAndSaveInfo(SLEEPTIME,"p2p communication waiting......");
        testMgTool.queryPeerListNo(peer1IPPort,DynamicPeerNo);

        sleepAndSaveInfo(SLEEPTIME);

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
        Thread.sleep(RESTARTTIME*2);

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

        mgToolCmd.sendNewTx(peer1IPPort,"5","");
        sleepAndSaveInfo(SLEEPTIME);

        assertEquals(mgToolCmd.queryBlockHeight(PEER1IP + ":" + PEER1RPCPort),mgToolCmd.queryBlockHeight(PEER2IP + ":" + PEER2RPCPort));
        assertEquals(mgToolCmd.queryBlockHeight(PEER1IP + ":" + PEER1RPCPort),mgToolCmd.queryBlockHeight(PEER3IP + ":" + PEER3RPCPort));


        shellPeer3.execute(killPeerCmd);
        mgToolCmd.quitPeer(peer1IPPort,PEER3IP);
        Thread.sleep(SLEEPTIME);
    }


    //动态加入数据节点
//    public void TC2136_2137_joinDataPeer()throws Exception
    public void joinDataPeer()throws Exception{
        Shell shellPeer3=new Shell(PEER3IP,USERNAME,PASSWD);

        //检查动态加入的共识节点，即使用管理工具加入的共识节点信息
        String resp = mgToolCmd.addPeer("observer",peer1IPPort,ipType+PEER3IP,tcpType+tcpPort,rpcPort);
        assertEquals(true,resp.contains("success"));
        sleepAndSaveInfo(SLEEPTIME,"p2p communication waiting......");
        testMgTool.queryPeerListNo(peer1IPPort,DynamicPeerNo);

        sleepAndSaveInfo(SLEEPTIME);

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
        Thread.sleep(RESTARTTIME*2);//等待启动时间

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

        mgToolCmd.sendNewTx(peer1IPPort,"5","");
        sleepAndSaveInfo(SLEEPTIME);

        assertEquals(mgToolCmd.queryBlockHeight(PEER1IP + ":" + PEER1RPCPort),mgToolCmd.queryBlockHeight(PEER2IP + ":" + PEER2RPCPort));
        assertEquals(mgToolCmd.queryBlockHeight(PEER1IP + ":" + PEER1RPCPort),mgToolCmd.queryBlockHeight(PEER3IP + ":" + PEER3RPCPort));


        shellPeer3.execute(killPeerCmd);
        mgToolCmd.quitPeer(peer1IPPort,PEER3IP);
        Thread.sleep(SLEEPTIME);
    }
}
