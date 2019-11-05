package com.tjfintech.common.functionTest.mixTestWithConfigChange;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.MgToolCmd;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.Shell;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DynamicChangePeerCluster {
    public static final int STARTSLEEPTIME=40000;
    TestBuilder testBuilder=TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    MgToolCmd mgToolCmd = new MgToolCmd();
    TestMgTool testMgTool = new TestMgTool();

    String rpcPort=PEER3RPCPort;
    String tcpPort=PEER3TCPPort;
    String consType="L";
    String dataType="D";
    int basePeerNo = 3;
    int DynamicPeerNo = 4;
    String ipType="/ip4/";
    String tcpType="/tcp/";


    String toolPath="cd " + ToolPATH + ";";
    String peer1IPPort=PEER1IP+":"+PEER1RPCPort;
    String peer2IPPort=PEER2IP+":"+PEER2RPCPort;
    String peer3IPPort=PEER3IP+":"+PEER3RPCPort;


    @Before
    public void resetPeerEnv()throws Exception{
        BeforeCondition bf = new BeforeCondition();
        bf.setPermission999();

        mgToolCmd.quitPeer(peer1IPPort,PEER3IP);

        Shell shellPeer3=new Shell(PEER3IP,USERNAME,PASSWD);
        shellPeer3.execute(killPeerCmd);
        shellPeer3.execute(resetPeerBase);
        testMgTool.queryPeerListNo(peer1IPPort,basePeerNo);

    }

    //动态加入共识节点
    @Test
    public void joinConsensusPeer()throws Exception{
        Shell shellPeer3=new Shell(PEER3IP,USERNAME,PASSWD);
        shellPeer3.execute("cp " + PeerPATH + "configjoin.toml " + PeerPATH + "config.toml" );//配置文件中节点共识节点标识为0
        //检查动态加入的共识节点，即使用管理工具加入的共识节点信息
        String resp = mgToolCmd.addPeer("join",peer1IPPort,ipType+PEER3IP,tcpType+tcpPort,rpcPort);
        assertEquals(true,resp.contains("success"));
        //queryPeerListNo(peer1IPPort,DynamicPeerNo);

        Thread.sleep(3000);

        shellExeCmd(PEER3IP,startPeerCmd);
        Thread.sleep(STARTSLEEPTIME);


        String meminfo = mgToolCmd.queryMemberList(PEER1IP + ":" + PEER1RPCPort);//查询集群信息
        testMgTool.checkMemInfoExHeight(meminfo,PEER3IP,
                getPeerId(PEER3IP,USERNAME,PASSWD), //id信息
                "0",  //state 连接状态
                shExeAndReturn(PEER3IP,getPeerVerByShell).trim(), //版本信息
                PEER3RPCPort, //节点rpc端口信息
                "peer168",   //节点名称
                ipType+PEER3IP+tcpType+tcpPort,  //节点inaddr信息
                ipType+PEER3IP+tcpType+tcpPort,  //节点outaddr信息
                "0",  //节点类型 共识节点还是数据节点
                "1",  //tls是否开启
                "sm3", //hash 类型 当前默认sm3
                "raft" //共识算法
        );
        assertNotEquals("0",testMgTool.parseMemInfo(meminfo,PEER3IP,"height")); //不确定当前区块高度是否有新交易目前自动化仅判断非0


        testMgTool.chkPeerSimInfoOK(peer3IPPort,tcpPort,version,consType);
        testMgTool.queryPeerListNo(peer1IPPort,DynamicPeerNo);
        testMgTool.queryPeerListNo(PEER3IP+":"+rpcPort,DynamicPeerNo);

        //做简单数据高度大于零判断，因原系统中数据较多时，同步完成时间无法确认，因此不检查是否与其他节点高度一致
        assertEquals(true,Integer.parseInt(mgToolCmd.queryBlockHeight(peer3IPPort)) > 0);

        shellPeer3.execute(killPeerCmd);
        Thread.sleep(3000);

        mgToolCmd.quitPeer(peer1IPPort,PEER3IP);

    }


    //动态加入数据节点
    @Test
    public void TC2136_2137_joinDataPeer()throws Exception{
        Shell shellPeer3=new Shell(PEER3IP,USERNAME,PASSWD);
        shellPeer3.execute("cp " + PeerPATH + "configobs.toml " + PeerPATH + "config.toml" );//配置文件中节点共识节点标识为0
        //检查动态加入的共识节点，即使用管理工具加入的共识节点信息
        String resp = mgToolCmd.addPeer("observer",peer1IPPort,ipType+PEER3IP,tcpType+tcpPort,rpcPort);
        assertEquals(true,resp.contains("success"));
        testMgTool.queryPeerListNo(peer1IPPort,DynamicPeerNo);

        String meminfo = mgToolCmd.queryMemberList(PEER1IP + ":" + PEER1RPCPort);//查询集群信息
        testMgTool.checkMemInfoExHeight(meminfo,PEER3IP,
                getPeerId(PEER3IP,USERNAME,PASSWD), //id信息
                "1",  //state 连接状态
                shExeAndReturn(PEER3IP,getPeerVerByShell).trim(), //版本信息
                PEER3RPCPort, //节点rpc端口信息
                "peer168",   //节点名称
                ipType+PEER3IP+tcpType+tcpPort,  //节点inaddr信息
                ipType+PEER3IP+tcpType+tcpPort,  //节点outaddr信息
                "0",  //节点类型 共识节点还是数据节点
                "0",  //tls是否开启
                "", //hash 类型 当前默认sm3
                "" //共识算法
        );
        assertEquals("0",testMgTool.parseMemInfo(meminfo,PEER3IP,"height"));

        shellExeCmd(PEER3IP,startPeerCmd);//启动节点
        Thread.sleep(STARTSLEEPTIME);//等待启动时间

        meminfo = mgToolCmd.queryMemberList(PEER1IP + ":" + PEER1RPCPort);//查询集群信息
        testMgTool.checkMemInfoExHeight(meminfo,PEER3IP,
                getPeerId(PEER3IP,USERNAME,PASSWD), //id信息
                "0",  //state 连接状态
                shExeAndReturn(PEER3IP,getPeerVerByShell).trim(), //版本信息
                PEER3RPCPort, //节点rpc端口信息
                "peer168",   //节点名称
                ipType+PEER3IP+tcpType+tcpPort,  //节点inaddr信息
                ipType+PEER3IP+tcpType+tcpPort,  //节点outaddr信息
                "1",  //节点类型 共识节点还是数据节点
                "1",  //tls是否开启
                "sm3", //hash 类型 当前默认sm3
                "raft" //共识算法
        );
        assertNotEquals("0",testMgTool.parseMemInfo(meminfo,PEER3IP,"height")); //不确定当前区块高度是否有新交易目前自动化仅判断非0

        testMgTool.chkPeerSimInfoOK(peer3IPPort,tcpPort,version,dataType);
        testMgTool.queryPeerListNo(peer1IPPort,DynamicPeerNo);
        testMgTool.queryPeerListNo(PEER3IP+":"+rpcPort,DynamicPeerNo);


        //做简单数据高度大于零判断，因原系统中数据较多时，同步完成时间无法确认，因此不检查是否与其他节点高度一致
        assertEquals(true,Integer.parseInt(mgToolCmd.queryBlockHeight(peer3IPPort)) > 0);

        shellPeer3.execute(killPeerCmd);
        Thread.sleep(3000);

        mgToolCmd.quitPeer(peer1IPPort,PEER3IP);
    }
}
