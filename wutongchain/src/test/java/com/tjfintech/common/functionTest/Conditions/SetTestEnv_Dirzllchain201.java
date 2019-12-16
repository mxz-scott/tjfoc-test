package com.tjfintech.common.functionTest.Conditions;

import static com.tjfintech.common.utils.UtilsClass.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class SetTestEnv_Dirzllchain201 {

   @Test
    public void test(){
       SDKADD = "http://10.1.3.240:7779";
       //设置测试环境使用的节点端口及部署目录信息
       PEER1IP = "10.1.3.240";
       PEER2IP = "10.1.3.246";
       PEER3IP = "10.1.5.168";
       PEER4IP = "10.1.3.247";
       PEER1RPCPort = "9400";
       PEER2RPCPort = "9500";
       PEER3RPCPort = "9400";
       PEER4RPCPort = "9400";
       PEER1TCPPort = "60011";
       PEER2TCPPort = "60011";
       PEER3TCPPort = "60011";
       PEER4TCPPort = "60012";
       //节点、SDK、Toolkit对等目录放置于PTPATH目录下
       PTPATH = "/root/zll/chain2.0.1/";
       SDKPATH = PTPATH + "sdk/";
       PeerPATH = PTPATH + "peer/";
       ToolPATH = PTPATH + "toolkit/";
       PeerTPName = "Mp";
       SDKTPName = "sdk";
       ToolTPName = "toolkit";
       tmuxSessionPeer = "tmux send -t M2 ";
       tmuxSessionSDK = "tmux send -t s_M2 ";
    }

}
