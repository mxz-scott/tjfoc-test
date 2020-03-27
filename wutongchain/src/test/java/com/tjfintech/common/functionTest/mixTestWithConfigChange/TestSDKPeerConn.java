package com.tjfintech.common.functionTest.mixTestWithConfigChange;

import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.Shell;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;


import static com.tjfintech.common.CommonFunc.addSDKPeerCluster;
import static com.tjfintech.common.CommonFunc.setSDKOnePeer;
import static com.tjfintech.common.utils.UtilsClass.*;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class TestSDKPeerConn {

    public   final static int   SLEEPTIME=15*1000;
    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    MultiSign multiSign =testBuilder.getMultiSign();
    UtilsClass utilsClass = new UtilsClass();

    @Test
    public void testSDKConnections()throws Exception{
        //设定sdk config文件中对接节点数为2个节点，较易呈现inconsistent data问题
        String sdkIP=SDKADD.substring(SDKADD.lastIndexOf("/")+1,SDKADD.lastIndexOf(":"));
        Shell shellSDK=new Shell(sdkIP,USERNAME,PASSWD);

        shellSDK.execute(killSDKCmd);
        //配置sdk节点集群为两个
        setSDKOnePeer(utilsClass.getIPFromStr(SDKADD),PEER1IP + ":" + PEER1RPCPort,"true");
        addSDKPeerCluster(utilsClass.getIPFromStr(SDKADD),PEER2IP + ":" + PEER2RPCPort,"true");
        shellSDK.execute(startSDKCmd);

        Shell shellPeer1 = new Shell(PEER1IP, USERNAME, PASSWD);
        Shell shellPeer2 = new Shell(PEER2IP, USERNAME, PASSWD);
        Shell shellPeer4 = new Shell(PEER4IP, USERNAME, PASSWD);
        boolean bError=false;

        for(int i=0;i<20;i++) {

            log.info("**************--------------test times: "+i+"--------------**************");
            shellPeer1.execute(killPeerCmd);
            shellPeer2.execute(killPeerCmd);
            shellPeer4.execute(killPeerCmd);

            startPeer(PEER1IP);
            startPeer(PEER2IP);
            startPeer(PEER4IP);
            Thread.sleep(SLEEPTIME);
            Thread.sleep(15000);

            //inconsistant data表示其中部分节点链接不上
            //rpc error 则意味着所有的节点都链接不上

            if(store.GetHeight().contains("Inconsistent data") ||store.GetHeight().contains("rpc error: code = Unavailable desc = all SubConns are in TransientFailure") ){

                bError=true;
                break;
            }
        }
        assertEquals(bError,false);
    }

    public void startPeer(String peerIP)throws Exception{
        Shell shell1=new Shell(peerIP,USERNAME,PASSWD);
        Thread.sleep(2000);
        shell1.execute(startPeerCmd);
    }

}
