package com.tjfintech.common.PersonalTestEnvironment;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static com.tjfintech.common.utils.UtilsClass.*;

@Slf4j
public class SetTestEnv_hff {

   @Test
    public void setEnvParam(){
      SDKADD = "http://10.1.5.162:8089";
      rSDKADD = "http://10.1.5.162:8089";
      TOKENADD = "http://10.1.5.162:8089";
      //设置测试环境使用的节点端口及部署目录信息
      PEER1IP = "10.1.5.162";
      PEER2IP = "10.1.5.243";
      PEER3IP = "10.1.5.162";
      PEER4IP = "10.1.5.243";
      PEER1RPCPort = "9001";
      PEER2RPCPort = "9001";
      PEER3RPCPort = "9001";
      PEER4RPCPort = "9001";
      PEER1TCPPort = "60001";
      PEER2TCPPort = "60001";
      PEER3TCPPort = "60001";
      PEER4TCPPort = "60001";
      //节点、SDK、Toolkit对等目录放置于PTPATH目录下
      PTPATH = "/root/wutongchain/";
      SDKPATH = PTPATH + "wtsdk/";
      PeerPATH = PTPATH + "wtchain/";
      ToolPATH = PTPATH + "wttool/";
      TokenApiPATH = PTPATH + "wtfinservice/";
      PeerTPName = "wtchain";
      SDKTPName = "wtsdk";
      ToolTPName = "wttool";
      TokenTPName = "wtfinservice";
      tmuxSessionTokenApi = "tmux at -t sdk_ff ";
      tmuxSessionPeer = "tmux at -t wtchain_ff ";
      tmuxSessionSDK = "tmux at -t wtsdk_ff";
      sReleaseLocalDir = "D:\\GoWorks\\src\\github.com\\tjfoc\\wtsys-release\\release\\梧桐链已发布版本\\2.1\\2.1.3\\";
      sLatestLocalDir = "D:\\GoWorks\\src\\github.com\\tjfoc\\wtsys-release\\release\\梧桐链已发布版本\\2.2\\";
//
//     public static String urlAddr = ""; // 向指定节点发请求 格式 IP:RPCPort
//     public static String SDKADD = "http://10.1.5.162:8089";
//     public static String rSDKADD = "http://10.1.5.162:8089";
//     public static String TOKENADD = "http://10.1.5.162:8089";
//     public static String subLedger = "xidttox79n";//应用链
//
//     //设置测试环境使用的节点端口及部署目录信息
//     public static String PEER1IP = "10.1.5.162";
//     public static String PEER2IP = "10.1.5.243";
//     public static String PEER3IP = "10.1.5.162";
//     public static String PEER4IP = "10.1.5.243";
//     public static String PEER1RPCPort = "9001";
//     public static String PEER2RPCPort = "9001";
//     public static String PEER3RPCPort = "9001";
//     public static String PEER4RPCPort = "9001";
//     public static String PEER1TCPPort = "60001";
//     public static String PEER2TCPPort = "60001";
//     public static String PEER3TCPPort = "60001";
//     public static String PEER4TCPPort = "60001";
//     //节点、SDK、Toolkit对等目录放置于PTPATH目录下
//     public static String PTPATH = "/root/wutongchain/";
//     public static String SDKPATH = PTPATH + "wtsdk/";
//     public static String PeerPATH = PTPATH + "wtchain/";
//     public static String ToolPATH = PTPATH + "wttool/";
//     public static String TokenApiPATH = PTPATH + "wtfinservice/";
//     public static String PeerTPName = "wtchain";
//     public static String SDKTPName = "wtsdk";
//     public static String ToolTPName = "wttool";
//     public static String TokenTPName = "wtfinservice";
//     public static String tmuxSessionTokenApi = "tmux at -t sdk_ff ";
//     public static String tmuxSessionPeer = "tmux at -t wtchain_ff ";
//     public static String tmuxSessionSDK = "tmux at -t wtsdk_ff ";
//     public static String sReleaseLocalDir = "E:\\gopath\\src\\github.com\\tjfoc\\wtsys-release\\release\\梧桐链已发布版本\\2.3\\";
//     public static String sLatestLocalDir = "E:\\test\\2.4.2\\";
//////    //duyuyang env use end -----------
////

   }

}
