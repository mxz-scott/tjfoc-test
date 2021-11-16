package com.tjfintech.common.PersonalTestEnvironment;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static com.tjfintech.common.utils.UtilsClass.*;

@Slf4j
public class SetTestEnv_edu {

   @Test
    public void setEnvParam(){
      subLedger = "hmcm9gx1sg";
      SDKADD = "http://10.1.3.160:7999";
      //设置测试环境使用的节点端口及部署目录信息
      PEER1IP = "10.1.3.160";
      PEER2IP = "10.1.5.240";
      PEER3IP = "10.1.5.168";
      PEER4IP = "10.1.3.161";
      PEER1RPCPort = "9200";
      PEER2RPCPort = "9200";
      PEER3RPCPort = "9200";
      PEER4RPCPort = "9200";
      PEER1TCPPort = "60020";
      PEER2TCPPort = "60020";
      PEER3TCPPort = "60020";
      PEER4TCPPort = "60020";
      //节点、SDK、Toolkit对等目录放置于PTPATH目录下
      PTPATH = "/root/zll/edu/";
      SDKPATH = PTPATH + "sdk/";
      PeerPATH = PTPATH + "peer/";
      ToolPATH = PTPATH + "toolkit/";
      TokenApiPATH = PTPATH + "wtfinservice/";
      PeerTPName = "wtchain_edu";
      SDKTPName = "wtsdk_edu";
      ToolTPName = "wttool_edu";
      tmuxSessionPeer = "tmux send -t edu ";
      tmuxSessionSDK = "tmux send -t edu_s ";
      sReleaseLocalDir = "D:\\GoWorks\\src\\github.com\\tjfoc\\wtsys-release\\release\\梧桐链已发布版本\\2.1\\2.1.3\\";
      sLatestLocalDir = "D:\\GoWorks\\src\\github.com\\tjfoc\\wtsys-release\\release\\梧桐链已发布版本\\2.2\\";
   }

}
