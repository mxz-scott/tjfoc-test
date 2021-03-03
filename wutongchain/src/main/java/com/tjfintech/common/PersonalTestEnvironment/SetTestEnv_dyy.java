package com.tjfintech.common.PersonalTestEnvironment;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static com.tjfintech.common.utils.UtilsClass.*;

@Slf4j
public class SetTestEnv_dyy {

   @Test
    public void setEnvParam(){
      SDKADD = "http://10.1.5.162:8777";
      rSDKADD = "http://10.1.5.162:8777";
      TOKENADD = "http://10.1.5.162:8777";
      //设置测试环境使用的节点端口及部署目录信息
      PEER1IP = "10.1.5.162";
      PEER2IP = "10.1.5.243";
      PEER3IP = "10.1.5.162";
      PEER4IP = "10.1.5.162";
      PEER1RPCPort = "9009";
      PEER2RPCPort = "9009";
      PEER3RPCPort = "9009";
      PEER4RPCPort = "9009";
      PEER1TCPPort = "60009";
      PEER2TCPPort = "60009";
      PEER3TCPPort = "60009";
      PEER4TCPPort = "60009";
      //节点、SDK、Toolkit对等目录放置于PTPATH目录下
      PTPATH = "/mnt/dyy/";
      SDKPATH = PTPATH + "wtsdk/";
      PeerPATH = PTPATH + "wtchain/";
      ToolPATH = PTPATH + "toolkit/";
      TokenApiPATH = PTPATH + "wtfinservice/";
      PeerTPName = "wtchain";
      SDKTPName = "wtsdk";
      ToolTPName = "toolkit";
      TokenTPName = "wtsdk";
      tmuxSessionTokenApi = "tmux send -t auto_t ";
      tmuxSessionPeer = "tmux send -t auto ";
      tmuxSessionSDK = "tmux send -t auto_s ";
      sReleaseLocalDir = "D:\\GoWorks\\src\\github.com\\tjfoc\\wtsys-release\\release\\梧桐链已发布版本\\2.1\\2.1.3\\";
      sLatestLocalDir = "D:\\GoWorks\\src\\github.com\\tjfoc\\wtsys-release\\release\\梧桐链已发布版本\\2.2\\";

//     public static String SDKADD = "http://10.1.3.161:7779";
//     public static String rSDKADD = "http://10.1.3.161:7779";
//     public static String TOKENADD = "http://10.1.3.161:9190";
//     //设置测试环境使用的节点端口及部署目录信息
//     public static String PEER1IP = "10.1.3.160";
//     public static String PEER2IP = "10.1.5.240";
//     public static String PEER3IP = "10.1.5.168";
//     public static String PEER4IP = "10.1.3.161";
//     public static String PEER1RPCPort = "9800";
//     public static String PEER2RPCPort = "9800";
//     public static String PEER3RPCPort = "9800";
//     public static String PEER4RPCPort = "9800";
//     public static String PEER1TCPPort = "60080";
//     public static String PEER2TCPPort = "60080";
//     public static String PEER3TCPPort = "60080";
//     public static String PEER4TCPPort = "60080";
//     //节点、SDK、Toolkit对等目录放置于PTPATH目录下
//     public static String PTPATH = "/root/zll/auto/";
//     public static String SDKPATH = PTPATH + "sdk/";
//     public static String PeerPATH = PTPATH + "peer/";
//     public static String ToolPATH = PTPATH + "toolkit/";
//     public static String TokenApiPATH = PTPATH + "wtfinservice/";
//     public static String PeerTPName = "Autop";
//     public static String SDKTPName = "Autos";
//     public static String ToolTPName = "Autokit";
//     public static String TokenTPName = "Autot";
//     public static String tmuxSessionTokenApi = "tmux send -t auto_t ";
//     public static String tmuxSessionPeer = "tmux send -t auto ";
//     public static String tmuxSessionSDK = "tmux send -t auto_s ";
//     public static String sReleaseLocalDir = "D:\\GoWorks\\src\\github.com\\tjfoc\\wtsys-release\\release\\梧桐链已发布版本\\2.1\\2.1.3\\";
//     public static String sLatestLocalDir = "D:\\GoWorks\\src\\github.com\\tjfoc\\wtsys-release\\release\\梧桐链已发布版本\\2.2\\";

   }

}
