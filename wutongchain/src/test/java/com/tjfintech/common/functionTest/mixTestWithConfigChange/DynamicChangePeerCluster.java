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
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.junit.Assert.assertEquals;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DynamicChangePeerCluster {
    public static final int STARTSLEEPTIME=40000;
    TestBuilder testBuilder=TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    MgToolCmd mgToolCmd = new MgToolCmd();

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

    ArrayList<String > txHashList =new ArrayList<>();

    @Before
    public void resetPeerEnv()throws Exception{
        BeforeCondition bf =new BeforeCondition();
        bf.setPermission999();
        PEER1MAC=getMACAddr(PEER1IP,USERNAME,PASSWD).trim();
        PEER2MAC=getMACAddr(PEER2IP,USERNAME,PASSWD).trim();
        PEER3MAC=getMACAddr(PEER3IP,USERNAME,PASSWD).trim();
        PEER4MAC=getMACAddr(PEER4IP,USERNAME,PASSWD).trim();

        mgToolCmd.quitPeer(peer1IPPort,PEER3IP);

        Shell shellPeer3=new Shell(PEER3IP,USERNAME,PASSWD);
        shellPeer3.execute(killPeerCmd);
        shellPeer3.execute(resetPeerBase);
        queryPeerListNo(peer1IPPort,basePeerNo);

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
        chkPeerSimInfoOK(peer3IPPort,tcpPort,version,consType);
        queryPeerListNo(peer1IPPort,DynamicPeerNo);
        queryPeerListNo(PEER3IP+":"+rpcPort,DynamicPeerNo);

        //做简单数据高度大于零判断，因原系统中数据较多时，同步完成时间无法确认，因此不检查是否与其他节点高度一致
        assertEquals(true,Integer.parseInt(mgToolCmd.queryBlockHeight(peer3IPPort)) > 0);

        shellPeer3.execute(killPeerCmd);
        Thread.sleep(3000);

        mgToolCmd.quitPeer(peer1IPPort,PEER3IP);

    }

    //动态加入数据节点
    @Test
    public void joinDataPeer()throws Exception{
        Shell shellPeer3=new Shell(PEER3IP,USERNAME,PASSWD);
        shellPeer3.execute("cp " + PeerPATH + "configobs.toml " + PeerPATH + "config.toml" );//配置文件中节点共识节点标识为0
        //检查动态加入的共识节点，即使用管理工具加入的共识节点信息
        String resp = mgToolCmd.addPeer("observer",peer1IPPort,ipType+PEER3IP,tcpType+tcpPort,rpcPort);
        assertEquals(true,resp.contains("success"));
        queryPeerListNo(peer1IPPort,DynamicPeerNo);

        Thread.sleep(3000);

        shellExeCmd(PEER3IP,startPeerCmd);
        Thread.sleep(STARTSLEEPTIME);
        chkPeerSimInfoOK(peer3IPPort,tcpPort,version,dataType);
        queryPeerListNo(peer1IPPort,DynamicPeerNo);
        queryPeerListNo(PEER3IP+":"+rpcPort,DynamicPeerNo);

        //做简单数据高度大于零判断，因原系统中数据较多时，同步完成时间无法确认，因此不检查是否与其他节点高度一致
        assertEquals(true,Integer.parseInt(mgToolCmd.queryBlockHeight(peer3IPPort)) > 0);

        shellPeer3.execute(killPeerCmd);
        Thread.sleep(3000);

        mgToolCmd.quitPeer(peer1IPPort,PEER3IP);
    }


    //动态变更已加入节点信息 无子链
    //@Test
    public void changePeerInfo()throws Exception{
        Shell shellPeer3=new Shell(PEER3IP,USERNAME,PASSWD);
        //检查动态加入的共识节点，即使用管理工具加入的共识节点信息
        String resp = mgToolCmd.addPeer("observer",peer1IPPort,ipType+PEER3IP,tcpType+tcpPort,rpcPort);
        assertEquals(true,resp.contains("success"));
        queryPeerListNo(peer1IPPort,DynamicPeerNo);

        Thread.sleep(3000);

        shellExeCmd(PEER3IP,startPeerCmd);
        int TpcPort2 = Integer.parseInt(tcpPort) + 1;
        resp = mgToolCmd.addPeer("observer",peer1IPPort,ipType+PEER3IP,tcpType+TpcPort2,rpcPort);
        Thread.sleep(STARTSLEEPTIME);
        chkPeerSimInfoOK(peer3IPPort,String.valueOf(TpcPort2),version,dataType);
        queryPeerListNo(peer1IPPort,DynamicPeerNo);
        queryPeerListNo(PEER3IP+":"+rpcPort,DynamicPeerNo);

        //做简单数据高度大于零判断，因原系统中数据较多时，同步完成时间无法确认，因此不检查是否与其他节点高度一致
        assertEquals(true,Integer.parseInt(mgToolCmd.queryBlockHeight(peer3IPPort)) > 0);

        shellPeer3.execute(killPeerCmd);
        Thread.sleep(3000);

        mgToolCmd.quitPeer(peer1IPPort,PEER3IP);
    }

    public void chkPeerSimInfoOK(String queryIPPort,String tcpPort,String version,String Type)throws Exception{

        String rpcPort=queryIPPort.split(":")[1];//9300
        String queryIP=queryIPPort.split(":")[0];//10.1.3.240
        String[] temp = queryIP.split("\\.");
        String tcpIP=queryIP+"/tcp/"+tcpPort; //10.1.3.240:60030
        String peerID=getPeerId(queryIP,USERNAME,PASSWD);  //取IP的最后一位点分十进制作为节点ID,ex. 240
        String peerName="peer"+temp[3];//peer240

        String response = mgToolCmd.getPeerSimpleInfo(queryIPPort);

        //assertEquals(response.contains("失败"), false);
        assertEquals(response.contains(version), true);
        assertEquals(response.contains(tcpIP), true);
        assertEquals(response.contains(peerID), true);
        assertEquals(response.contains(peerName), true);
        assertEquals(response.contains(Type), true);
        assertEquals(response.contains(rpcPort), true);
    }



    public void queryPeerListNo(String queryIPPort,int peerNo) throws Exception{
        Thread.sleep(1500);
        String tempCmd="";

        String rpcPort=queryIPPort.split(":")[1];//9300
        String queryIP=queryIPPort.split(":")[0];//10.1.3.240

        tempCmd=toolPath+"./toolkit mem -p "+rpcPort;

        Shell shell1=new Shell(queryIP,USERNAME,PASSWD);
        shell1.execute(tempCmd);
        int No=0;
        ArrayList<String> stdout = shell1.getStandardOutput();
        for(String str :stdout)
        {
            if(str.contains("shownName"))
                No++;
        }
        String response = StringUtils.join(stdout,"\n");
        log.info("\n"+response);
        assertEquals(peerNo,No);
        //assertEquals(response.contains("isLeader"), true);

    }



}
