package com.tjfintech.common.functionTest.mixTest;

import com.tjfintech.common.utils.Shell;
import lombok.extern.slf4j.Slf4j;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class PeerCmdTest {
    /**
     * 当前支持命令
     * root@ubuntu:~/zll/chain2.0.1/peer# ./Mp help
     * Copyright 2018 Suzhou Tongji Blockchain Research Institute
     *
     * Usage:
     *   peer [flags]
     *   peer [command]
     *
     * Available Commands:
     *   check       Peer check befor start
     *   help        Help about any command
     *   id          Print local peerID info
     *   init        Generate peer key pair
     *   start       Peer startup
     *   stop        Stop peer
     *   test        test communication with other peer
     *   version     Current Version
     *
     * Flags:
     *   -h, --help   help for peer
     *
     * Use "peer [command] --help" for more information about a command.
     */
    String exePre = "cd "+ PeerPATH + ";./" + PeerTPName;

    @Test
    public void testPeerCheck()throws Exception{
        shExeAndReturn(PEER1IP,killPeerCmd);
        sleepAndSaveInfo(100,"停止节点");
        String resp1 = shExeAndReturn(PEER1IP,tmuxSession + "'./" + PeerTPName + " check' ENTER");
        sleepAndSaveInfo(SLEEPTIME/2);
        String resp2 = shExeAndReturn(PEER1IP,"ps -ef |grep " + PeerTPName +" |grep -v grep |awk '{print $2}'");
        assertEquals(false,resp2.trim().isEmpty());
    }

    @Test
    public void testPeerId()throws Exception{
        String resp1 = shExeAndReturn(PEER1IP,exePre + " id");
        assertEquals(true,resp1.toLowerCase().contains("localpeerid"));
        assertEquals(true,resp1.toLowerCase().contains("peername"));
        assertEquals(true,resp1.toLowerCase().contains("p2p"));
    }

    @Test
    public void testPeerInit()throws Exception{
        String resp1 = shExeAndReturn(PEER1IP,exePre + " init");
        assertEquals(true,resp1.toLowerCase().contains("generate key pair success"));
        assertEquals(true,resp1.toLowerCase().contains("local peerid"));
    }

    @Test
    public void testPeerStart()throws Exception{
        String resp = shExeAndReturn(PEER1IP,killPeerCmd);
        String resp2 = shExeAndReturn(PEER1IP,"ps -ef |grep " + PeerTPName +" |grep -v grep |awk '{print $2}'");
        assertEquals(true,resp2.trim().isEmpty()); //确认进程未启动

        String resp1 = shExeAndReturn(PEER1IP,tmuxSession + "'./" + PeerTPName + " start -d' ENTER"); //使用start -d方式启动
        sleepAndSaveInfo(100,"start -d 启动节点进程");

        resp2 = shExeAndReturn(PEER1IP,"ps -ef |grep " + PeerTPName +" |grep -v grep |awk '{print $2}'");
        assertEquals(false,resp2.trim().isEmpty()); //确认进程启动
        String resp3 = shExeAndReturn(PEER1IP,exePre + " stop");//使用stop命令停止节点（20190909目前仅支持停止采用start -d启动命令）
        resp2 = shExeAndReturn(PEER1IP,"ps -ef |grep " + PeerTPName +" |grep -v grep |awk '{print $2}'");
        assertEquals(true,resp2.trim().isEmpty());

        resp = shExeAndReturn(PEER1IP,startPeerCmd); //采用原方式start.sh脚本启动节点
    }

    //@Test
    public void testPeerStop()throws Exception{
        String resp2 = shExeAndReturn(PEER1IP,"ps -ef |grep " + PeerTPName +" |grep -v grep |awk '{print $2}'");
        assertEquals(false,resp2.trim().isEmpty());
        String resp1 = shExeAndReturn(PEER1IP,exePre + " stop");
        resp2 = shExeAndReturn(PEER1IP,"ps -ef |grep " + PeerTPName +" |grep -v grep |awk '{print $2}'");
        assertEquals(true,resp2.trim().isEmpty());
    }

    @Test
    public void testPeerVersion()throws Exception{
        String resp1 = shExeAndReturn(PEER1IP,exePre + " version");
        assertEquals(true,resp1.toLowerCase().contains("peer version"));
        assertEquals(true,resp1.toLowerCase().contains("git commit"));
        assertEquals(true,resp1.toLowerCase().contains("build time"));
        assertEquals(true,resp1.toLowerCase().contains("run  mode"));
    }

    @Test
    public void testPeerTest()throws Exception{
        String resp1 = shExeAndReturn(PEER4IP,exePre + " test");
        assertEquals(true,resp1.toLowerCase().contains(getPeerId(PEER1IP,USERNAME,PASSWD).toLowerCase() + "] successfully"));
        assertEquals(true,resp1.toLowerCase().contains(getPeerId(PEER2IP,USERNAME,PASSWD).toLowerCase() + "] successfully"));
    }

    @Test
    public void testPeerHelp()throws Exception{
        Shell shell1=new Shell(PEER1IP,USERNAME,PASSWD);
        shell1.execute(exePre + " help");
        ArrayList<String> stdout = shell1.getStandardOutput();
        //String resp1 = shExeAndReturn(PEER1IP,exePre + " help");
        //log.info(resp1);
        boolean bCount = false;
        int CmdCount = 0;
        for (String str:stdout) {
            if (str.trim().isEmpty()) continue;

            log.info(str);
            if (str.contains("Available")) {
                bCount = true;
                continue;
            }
            if (str.contains("Flags:")) break;
            if(bCount) CmdCount ++;
        }
        assertEquals(8,CmdCount);
        }


}
