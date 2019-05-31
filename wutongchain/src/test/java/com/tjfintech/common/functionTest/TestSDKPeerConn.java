package com.tjfintech.common.functionTest;

import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.Shell;
import lombok.extern.slf4j.Slf4j;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;


import static com.tjfintech.common.utils.UtilsClass.*;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class TestSDKPeerConn {

    public   final static int   SLEEPTIME=15*1000;
    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    MultiSign multiSign =testBuilder.getMultiSign();



    @Test
    public void testSDKConnections()throws Exception{
        //设定sdk config文件中对接节点数为2个节点，较易呈现inconsistent data问题
        String sdkIP=SDKADD.substring(SDKADD.lastIndexOf("/")+1,SDKADD.lastIndexOf(":"));
        Shell shellSDK=new Shell(sdkIP,USERNAME,PASSWD);

        shellSDK.execute("ps -ef |grep httpservice |grep -v grep |awk '{print $2}'|xargs kill -9");
        shellSDK.execute("cp "+PTPATH+"sdk/conf/config2P.toml "+PTPATH+"sdk/conf/config.toml");
        //shellSDK.execute("cp "+PTPATH+"sdk/httpservice222 "+PTPATH+"sdk/httpservice");

        shellSDK.execute("sh "+PTPATH+"sdk/start.sh");

        Shell shellPeer1 = new Shell(PEER1IP, USERNAME, PASSWD);
        Shell shellPeer2 = new Shell(PEER2IP, USERNAME, PASSWD);
        Shell shellPeer4 = new Shell(PEER4IP, USERNAME, PASSWD);
        boolean bError=false;
        for(int i=0;i<20;i++) {

            log.info("**************--------------test times: "+i+"--------------**************");
            shellPeer1.execute("ps -ef |grep " + PeerTPName +" |grep -v grep |awk '{print $2}'|xargs kill -9");
            shellPeer2.execute("ps -ef |grep " + PeerTPName +" |grep -v grep |awk '{print $2}'|xargs kill -9");
            shellPeer4.execute("ps -ef |grep " + PeerTPName +" |grep -v grep |awk '{print $2}'|xargs kill -9");

            startPeer(PEER1IP);
            startPeer(PEER2IP);
            startPeer(PEER4IP);
            Thread.sleep(SLEEPTIME);

            store.GetHeight();
            Thread.sleep(2000);
            store.GetHeight();
            Thread.sleep(2000);
            store.GetHeight();
            Thread.sleep(2000);
            store.GetHeight();
            Thread.sleep(2000);

            //inconsistant data表示其中部分节点链接不上
            //rpc error 则意味着所有的节点都链接不上

            if(store.GetHeight().contains("Inconsistent data") ||store.GetHeight().contains("rpc error: code = Unavailable desc = all SubConns are in TransientFailure") ){

                bError=true;
                break;
            }
        }
        assertEquals(bError,false);
    }


    //@Test
    public void testSDKConnections1()throws Exception{
        //设定sdk config文件中对接节点数为2个节点，较易呈现inconsistent data问题
        String sdkIP=SDKADD.substring(SDKADD.lastIndexOf("/")+1,SDKADD.lastIndexOf(":"));
        Shell shellSDK=new Shell(sdkIP,USERNAME,PASSWD);

        shellSDK.execute("ps -ef |grep httpservice |grep -v grep |awk '{print $2}'|xargs kill -9");
        shellSDK.execute("cp "+PTPATH+"sdk/conf/config2P.toml "+PTPATH+"sdk/conf/config.toml");
        shellSDK.execute("cp "+PTPATH+"sdk/httpserviceNewGRPC "+PTPATH+"sdk/httpservice");

        shellSDK.execute("sh "+PTPATH+"sdk/start.sh");

        Shell shellPeer1 = new Shell(PEER1IP, USERNAME, PASSWD);
        Shell shellPeer2 = new Shell(PEER2IP, USERNAME, PASSWD);
        Shell shellPeer4 = new Shell(PEER4IP, USERNAME, PASSWD);
        boolean bError=false;
        for(int i=0;i<50;i++) {

            log.info("**************--------------test times: "+i+"--------------**************");
            shellPeer1.execute("ps -ef |grep " + PeerTPName +" |grep -v grep |awk '{print $2}'|xargs kill -9");
            shellPeer2.execute("ps -ef |grep " + PeerTPName +" |grep -v grep |awk '{print $2}'|xargs kill -9");
            shellPeer4.execute("ps -ef |grep " + PeerTPName +" |grep -v grep |awk '{print $2}'|xargs kill -9");

            startPeer(PEER1IP);
            startPeer(PEER2IP);
            startPeer(PEER4IP);
            Thread.sleep(SLEEPTIME);

            store.GetHeight();
            Thread.sleep(2000);
            store.GetHeight();
            Thread.sleep(2000);
            store.GetHeight();
            Thread.sleep(2000);
            store.GetHeight();
            Thread.sleep(2000);

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
        shell1.execute("sh "+PTPATH+"peer/start.sh");
    }

}
