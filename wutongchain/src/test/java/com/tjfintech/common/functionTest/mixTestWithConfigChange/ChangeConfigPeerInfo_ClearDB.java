package com.tjfintech.common.functionTest.mixTestWithConfigChange;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.MgToolCmd;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.Shell;
import lombok.extern.slf4j.Slf4j;
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
public class ChangeConfigPeerInfo_ClearDB {
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

    String peer1IPPort=PEER1IP+":"+PEER1RPCPort;



    @Before
    public void resetPeerEnv()throws Exception{
        BeforeCondition bf = new BeforeCondition();
        bf.clearDataSetPerm999();

        mgToolCmd.quitPeer(peer1IPPort,PEER3IP);

        Shell shellPeer3=new Shell(PEER3IP,USERNAME,PASSWD);
        shellPeer3.execute(killPeerCmd);
        shellPeer3.execute(resetPeerBase);
        testMgTool.queryPeerListNo(peer1IPPort,basePeerNo);

    }


    //该测试用例必须保证系统无子链使用待测试节点，否则将会执行失败
    //动态变更原配置文件中的节点信息
    //20191106 节点动态变更仅支持变更name 和 datatype 不支持其他信息变更 包括rpc tcp端口
    @Test
    public void TC1759_2129_1470_changeClusterPeerInfo()throws Exception{
        //检查正常配置共识节点mem信息
        String meminfo = mgToolCmd.queryMemberList(peer1IPPort);
        testMgTool.checkMemInfoExHeight(meminfo,PEER1IP,
                getPeerId(PEER1IP,USERNAME,PASSWD), //id信息
                "0",  //state 连接状态
                shExeAndReturn(PEER1IP,getPeerVerByShell).trim(), //版本信息
                PEER1RPCPort, //节点rpc端口信息
                "peer240",   //节点名称
                ipType+PEER1IP+tcpType+PEER1TCPPort,  //节点inaddr信息
                ipType+PEER1IP+tcpType+PEER1TCPPort,  //节点outaddr信息
                "0",  //节点类型 共识节点还是数据节点
                "0",  //tls是否开启
                "sm3", //hash 类型 当前默认sm3
                "raft" //共识算法
        );
        assertNotEquals("0",testMgTool.parseMemInfo(meminfo,PEER1IP,"height")); //不确定当前区块高度是否有新交易目前自动化仅判断非0
        testMgTool.chkPeerSimInfoOK(peer1IPPort,PEER1TCPPort,version,consType);//自己查询节点信息



        String opIP = PEER2IP;
        tcpPort = PEER2TCPPort;
        rpcPort = PEER2RPCPort;

        int TpcPort2 = Integer.parseInt(tcpPort) + 1;
        int RpcPort2 = Integer.parseInt(rpcPort) + 1;

        String respChange= mgToolCmd.addPeer("observer",peer1IPPort,ipType+opIP,tcpType+TpcPort2,String.valueOf(RpcPort2));
        assertEquals(true,respChange.contains("success"));

        Thread.sleep(SLEEPTIME);//等待P2P更新

        testMgTool.queryPeerListNo(peer1IPPort,basePeerNo);
        testMgTool.queryPeerListNo(opIP + ":" + rpcPort,basePeerNo);

        meminfo = mgToolCmd.queryMemberList(PEER1IP + ":" + PEER1RPCPort);//其他节点查询
        assertEquals(ipType + opIP + tcpType + tcpPort,testMgTool.parseMemInfo(meminfo,opIP,"inAddr"));
        assertEquals(String.valueOf(rpcPort),testMgTool.parseMemInfo(meminfo,opIP,"port"));
        assertEquals("1",testMgTool.parseMemInfo(meminfo,opIP,"typ"));

        meminfo = mgToolCmd.queryMemberList(opIP + ":" + rpcPort);//自己查询
        assertEquals(ipType + opIP + tcpType + tcpPort,testMgTool.parseMemInfo(meminfo,opIP,"inAddr"));
        assertEquals(String.valueOf(rpcPort),testMgTool.parseMemInfo(meminfo,opIP,"port"));
        assertEquals("1",testMgTool.parseMemInfo(meminfo,opIP,"typ"));
        testMgTool.chkPeerSimInfoOK(opIP + ":" + rpcPort,String.valueOf(tcpPort),version,dataType);//自己查询节点信息
    }
}
