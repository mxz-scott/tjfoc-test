package com.tjfintech.common.PersonalTestEnvironment;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static com.tjfintech.common.utils.UtilsClass.*;

@Slf4j
public class SetTestEnv_lilu {

   @Test
    public void setEnvParam(){

     //***************************************************************zhengxin**********************************************************************************
//     public static String ADD = "http://10.1.5.225:8088";
//     public static String SDKADD = ADD;
//     public static String rSDKADD = ADD;
//     public static String TOKENADD = ADD;
//
//     //设置测试环境使用的节点端口及部署目录信息
//     public static String PEER1IP = "10.1.5.225";
//     public static String PEER2IP = "10.1.5.226";
//     public static String PEER3IP = "10.1.3.161";
//     public static String PEER4IP = "10.1.5.227";
//     public static String PEER1RPCPort = "8200";
//     public static String PEER2RPCPort = "8200";
//     public static String PEER3RPCPort = "8200";
//     public static String PEER4RPCPort = "8200";
//     public static String PEER1TCPPort = "8000";
//     public static String PEER2TCPPort = "8000";
//     public static String PEER3TCPPort = "8000";
//     public static String PEER4TCPPort = "8000";
//     //节点、SDK、Toolkit对等目录放置于PTPATH目录下
//     public static String PTPATH = "/opt/";
//     public static String SDKPATH = PTPATH + "zxsdk/";
//     public static String PeerPATH = PTPATH + "zxchain/";
//     public static String ToolPATH = PTPATH + "wttool/";
//     public static String TokenApiPATH = PTPATH + "wtfinservice/";
//     public static String PeerTPName = "wtchain";
//     public static String SDKTPName = "wtsdk";
//     public static String ToolTPName = "wttool";
//     public static String TokenTPName = "wtfinservice";
//     public static String tmuxSessionTokenApi = "tmux send -t zx-sdk ";
//     public static String tmuxSessionPeer = "tmux send -t zx-chain ";
//     public static String tmuxSessionSDK = "tmux send -t zx-sdk ";
//     public static String sReleaseLocalDir = "E:\\gopath\\src\\github.com\\tjfoc\\wtsys-release\\release\\梧桐链已发布版本\\2.3\\";
//     public static String sLatestLocalDir = "E:\\test\\2.4.2\\";
//
//
//     //    public static String resourcePath = System.getProperty("user.dir") + "/src/main/resources/";
//     public static String testResultPath = "testresult/";
//     public static String testDataPath = "testdata/";
//     public static String srcShellScriptDir = testDataPath + "/configFiles/shell/";
//     public static String destShellScriptDir = "/opt/tjshell/";
//     public static String tempWVMDir = testDataPath + "WVM/";
//
//
//     //***************************************************************应用链**********************************************************************************
//     public static String ADD = "http://10.1.5.225:7088";
//     public static String SDKADD = ADD;
//     public static String rSDKADD = ADD;
//     public static String TOKENADD = ADD;
//
//     //设置测试环境使用的节点端口及部署目录信息
//     public static String PEER1IP = "10.1.5.225";
//     public static String PEER2IP = "10.1.5.226";
//     public static String PEER3IP = "10.1.3.161";
//     public static String PEER4IP = "10.1.5.227";
//     public static String PEER1RPCPort = "7200";
//     public static String PEER2RPCPort = "7200";
//     public static String PEER3RPCPort = "7200";
//     public static String PEER4RPCPort = "7200";
//     public static String PEER1TCPPort = "7000";
//     public static String PEER2TCPPort = "7000";
//     public static String PEER3TCPPort = "7000";
//     public static String PEER4TCPPort = "7000";
//     //节点、SDK、Toolkit对等目录放置于PTPATH目录下
//     public static String PTPATH = "/opt/";
//     public static String SDKPATH = PTPATH + "wtsdk/";
//     public static String PeerPATH = PTPATH + "wtchain/";
//     public static String ToolPATH = PTPATH + "wttool/";
//     public static String TokenApiPATH = PTPATH + "wtfinservice/";
//     public static String PeerTPName = "wtchain";
//     public static String SDKTPName = "wtsdk";
//     public static String ToolTPName = "wttool";
//     public static String TokenTPName = "wtfinservice";
//     public static String tmuxSessionTokenApi = "tmux send -t wt-sdk ";
//     public static String tmuxSessionPeer = "tmux send -t wt-chain ";
//     public static String tmuxSessionSDK = "tmux send -t wt-sdk ";
//     public static String sReleaseLocalDir = "E:\\gopath\\src\\github.com\\tjfoc\\wtsys-release\\release\\梧桐链已发布版本\\2.3\\";
//     public static String sLatestLocalDir = "E:\\test\\2.4.2\\";


     //**********************************************************************pufa****************************************************************************
       SDKADD = "http://10.1.5.225:7081";
       rSDKADD = "http://10.1.5.225:7081";
       TOKENADD = "http://10.1.5.225:7081";

     //设置测试环境使用的节点端口及部署目录信息
       PEER1IP = "10.1.5.225";
       PEER2IP = "10.1.5.226";
       PEER3IP = "10.1.3.161";
       PEER4IP = "10.1.5.227";
       PEER1RPCPort = "7201";
       PEER2RPCPort = "7201";
       PEER3RPCPort = "7201";
       PEER4RPCPort = "7201";
       PEER1TCPPort = "7001";
       PEER2TCPPort = "7001";
       PEER3TCPPort = "7001";
       PEER4TCPPort = "7001";
     //节点、SDK、Toolkit对等目录放置于PTPATH目录下
       PTPATH = "/opt/";
       SDKPATH = PTPATH + "wtsdkpufa/";
       PeerPATH = PTPATH + "wtchainpufa/";
       ToolPATH = PTPATH + "wttool/";
       TokenApiPATH = PTPATH + "wtsdkpufa/";
       PeerTPName = "wtchainpufa";
       SDKTPName = "wtsdkpufa";
       ToolTPName = "wttool";
       TokenTPName = "wtsdkpufa";
       tmuxSessionTokenApi = "tmux send -t sdk-pufa ";
       tmuxSessionPeer = "tmux send -t chain-pufa ";
       tmuxSessionSDK = "tmux send -t sdk-pufa ";
       sReleaseLocalDir = "E:\\gopath\\src\\github.com\\tjfoc\\wtsys-release\\release\\梧桐链已发布版本\\2.3\\";
       sLatestLocalDir = "E:\\test\\2.4.2\\";


   }

}
