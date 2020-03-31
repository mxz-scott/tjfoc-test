package com.tjfintech.common.functionTest.Conditions.PersonalTestEnvironment;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static com.tjfintech.common.utils.UtilsClass.*;

@Slf4j
public class SetTestEnv_Dirzllauto {

   @Test
    public void test(){
      SDKADD = "http://10.1.3.247:7779";
      rSDKADD = "http://10.1.3.247:7779";
      TOKENADD = "http://10.1.3.247:9190";
      //设置测试环境使用的节点端口及部署目录信息
      PEER1IP = "10.1.3.240";
      PEER2IP = "10.1.3.246";
      PEER3IP = "10.1.5.168";
      PEER4IP = "10.1.3.247";
      PEER1RPCPort = "9800";
      PEER2RPCPort = "9800";
      PEER3RPCPort = "9800";
      PEER4RPCPort = "9800";
      PEER1TCPPort = "60080";
      PEER2TCPPort = "60080";
      PEER3TCPPort = "60080";
      PEER4TCPPort = "60080";
      //节点、SDK、Toolkit对等目录放置于PTPATH目录下
      PTPATH = "/root/zll/auto/";
      SDKPATH = PTPATH + "sdk/";
      PeerPATH = PTPATH + "peer/";
      ToolPATH = PTPATH + "toolkit/";
      TokenApiPATH = PTPATH + "wtfinservice/";
      PeerTPName = "Autop";
      SDKTPName = "Autos";
      ToolTPName = "Autokit";
      TokenTPName = "Autofin";
      tmuxSessionTokenApi = "tmux send -t auto_t ";
      tmuxSessionPeer = "tmux send -t auto ";
      tmuxSessionSDK = "tmux send -t auto_s ";
      sReleaseLocalDir = "D:\\GoWorks\\src\\github.com\\tjfoc\\wtsys-release\\release\\梧桐链已发布版本\\2.1\\2.1.3\\";
      sLatestLocalDir = "D:\\GoWorks\\src\\github.com\\tjfoc\\wtsys-release\\release\\梧桐链已发布版本\\2.2\\";

   }

}
