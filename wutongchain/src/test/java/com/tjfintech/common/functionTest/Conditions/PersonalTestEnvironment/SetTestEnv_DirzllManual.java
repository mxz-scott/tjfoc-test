package com.tjfintech.common.functionTest.Conditions.PersonalTestEnvironment;

import static com.tjfintech.common.utils.UtilsClass.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class SetTestEnv_DirzllManual {

   @Test
    public void test(){
     SDKADD = "http://10.1.5.240:7779";
     rSDKADD = "http://10.1.5.240:7779";
     TOKENADD = "http://10.1.5.240:9190";
     //设置测试环境使用的节点端口及部署目录信息
     PEER1IP = "10.1.3.160";
     PEER2IP = "10.1.5.240";
     PEER3IP = "10.1.5.168";
     PEER4IP = "10.1.3.161";
     PEER1RPCPort = "9400";
     PEER2RPCPort = "9500";
     PEER3RPCPort = "9400";
     PEER4RPCPort = "9400";
     PEER1TCPPort = "60011";
     PEER2TCPPort = "60011";
     PEER3TCPPort = "60011";
     PEER4TCPPort = "60012";
     //节点、SDK、Toolkit对等目录放置于PTPATH目录下
     PTPATH = "/root/zll/manual/";
     SDKPATH = PTPATH + "sdk/";
     PeerPATH = PTPATH + "peer/";
     ToolPATH = PTPATH + "toolkit/";
     TokenApiPATH = PTPATH + "wtfinservice/";
     PeerTPName = "Mp";
     SDKTPName = "sdk";
     ToolTPName = "toolkit";
     TokenTPName = "fintoken";
     tmuxSessionTokenApi = "tmux send -t t_M2 ";
     tmuxSessionPeer = "tmux send -t M2 ";
     tmuxSessionSDK = "tmux send -t s_M2 ";
     sReleaseLocalDir = "D:\\GoWorks\\src\\github.com\\tjfoc\\wtsys-release\\release\\梧桐链已发布版本\\2.4.x\\2.4.1\\";
     sLatestLocalDir = "D:\\GoWorks\\src\\github.com\\tjfoc\\tjfoc\\bin\\";
     
   }

}
