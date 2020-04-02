package com.tjfintech.common.functionTest.Conditions.PersonalTestEnvironment;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static com.tjfintech.common.utils.UtilsClass.*;

@Slf4j
public class SetTestEnv_Dirzllchain201sync {

   @Test
    public void test(){
       SDKADD = "http://10.1.3.240:7780";
       //设置测试环境使用的节点端口及部署目录信息
       PEER1IP = "10.1.3.240";
       PEER2IP = "10.1.3.246";
       PEER3IP = "10.1.5.168";
       PEER4IP = "10.1.3.247";
       PEER1RPCPort = "9700";
       PEER2RPCPort = "9800";
       PEER3RPCPort = "9700";
       PEER4RPCPort = "9700";
       PEER1TCPPort = "60071";
       PEER2TCPPort = "60071";
       PEER3TCPPort = "60071";
       PEER4TCPPort = "60072";
       //节点、SDK、Toolkit对等目录放置于PTPATH目录下
       PTPATH = "/root/zll/chain2.0.1/";
       SDKPATH = PTPATH + "syncsdk/";
       PeerPATH = PTPATH + "syncpeer/";
       ToolPATH = PTPATH + "toolkit/";
       PeerTPName = "syncpeer";
       SDKTPName = "syncsdk";
       ToolTPName = "toolkit";
       tmuxSessionPeer = "tmux send -t sync ";
       tmuxSessionSDK = "tmux send -t sync_sdk ";
    }

}
