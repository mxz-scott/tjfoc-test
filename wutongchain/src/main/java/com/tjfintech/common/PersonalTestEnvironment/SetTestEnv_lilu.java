package com.tjfintech.common.PersonalTestEnvironment;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static com.tjfintech.common.utils.UtilsClass.*;

@Slf4j
public class SetTestEnv_lilu {

    @Test
    public void setEnvParam() {

        //***************************************************************zhengxin**********************************************************************************

        SDKADD = "http://10.1.5.225:8088";
        rSDKADD = "http://10.1.5.225:8088";
        TOKENADD = "http://10.1.5.225:8088";

        //设置测试环境使用的节点端口及部署目录信息
        PEER1IP = "10.1.5.225";
        PEER2IP = "10.1.5.226";
        PEER3IP = "10.1.3.161";
        PEER4IP = "10.1.5.227";
        PEER1RPCPort = "8200";
        PEER2RPCPort = "8200";
        PEER3RPCPort = "8200";
        PEER4RPCPort = "8200";
        PEER1TCPPort = "8000";
        PEER2TCPPort = "8000";
        PEER3TCPPort = "8000";
        PEER4TCPPort = "8000";
        //节点、SDK、Toolkit对等目录放置于PTPATH目录下
        PTPATH = "/opt/";
        SDKPATH = PTPATH + "zxsdk/";
        PeerPATH = PTPATH + "zxchain/";
        ToolPATH = PTPATH + "wttool/";
        TokenApiPATH = PTPATH + "wtfinservice/";
        PeerTPName = "wtchain";
        SDKTPName = "wtsdk";
        ToolTPName = "wttool";
        TokenTPName = "wtfinservice";
        tmuxSessionTokenApi = "tmux send -t zx-sdk ";
        tmuxSessionPeer = "tmux send -t zx-chain ";
        tmuxSessionSDK = "tmux send -t zx-sdk ";
        sReleaseLocalDir = "E:\\gopath\\src\\github.com\\tjfoc\\wtsys-release\\release\\梧桐链已发布版本\\2.3\\";
        sLatestLocalDir = "E:\\test\\2.4.2\\";


        //    public static String resourcePath = System.getProperty("user.dir") + "/src/main/resources/";
        testResultPath = "testresult/";
        testDataPath = "testdata/";
        srcShellScriptDir = testDataPath + "/configFiles/shell/";
        destShellScriptDir = "/opt/tjshell/";
        tempWVMDir = testDataPath + "WVM/";


        //***************************************************************应用链**********************************************************************************
//     SDKADD = "http://10.1.5.225:7088";
//     rSDKADD = "http://10.1.5.225:7088";
//     TOKENADD = "http://10.1.5.225:7088";
//
//     //设置测试环境使用的节点端口及部署目录信息
//     PEER1IP = "10.1.5.225";
//     PEER2IP = "10.1.5.226";
//     PEER3IP = "10.1.3.161";
//     PEER4IP = "10.1.5.227";
//     PEER1RPCPort = "7200";
//     PEER2RPCPort = "7200";
//     PEER3RPCPort = "7200";
//     PEER4RPCPort = "7200";
//     PEER1TCPPort = "7000";
//     PEER2TCPPort = "7000";
//     PEER3TCPPort = "7000";
//     PEER4TCPPort = "7000";
//     //节点、SDK、Toolkit对等目录放置于PTPATH目录下
//     PTPATH = "/opt/";
//     SDKPATH = PTPATH + "wtsdk/";
//     PeerPATH = PTPATH + "wtchain/";
//     ToolPATH = PTPATH + "wttool/";
//     TokenApiPATH = PTPATH + "wtfinservice/";
//     PeerTPName = "wtchain";
//     SDKTPName = "wtsdk";
//     ToolTPName = "wttool";
//     TokenTPName = "wtfinservice";
//     tmuxSessionTokenApi = "tmux send -t wt-sdk ";
//     tmuxSessionPeer = "tmux send -t wt-chain ";
//     tmuxSessionSDK = "tmux send -t wt-sdk ";
//     sReleaseLocalDir = "E:\\\\test\\\\2.3\\\\";
//     sLatestLocalDir = "E:\\\\test\\\\2.4.2\\\\";
//     destShellScriptDir = "/opt/tjshell/";


        //**********************************************************************pufa****************************************************************************
//       SDKADD = "http://10.1.5.225:7082";
//       rSDKADD = "http://10.1.5.225:7082";
//       TOKENADD = "http://10.1.5.225:7082";
//
//     //设置测试环境使用的节点端口及部署目录信息
//       PEER1IP = "10.1.5.225";
//       PEER2IP = "10.1.5.226";
//       PEER3IP = "10.1.3.161";
//       PEER4IP = "10.1.5.227";
//       PEER1RPCPort = "7201";
//       PEER2RPCPort = "7201";
//       PEER3RPCPort = "7201";
//       PEER4RPCPort = "7201";
//       PEER1TCPPort = "7001";
//       PEER2TCPPort = "7001";
//       PEER3TCPPort = "7001";
//       PEER4TCPPort = "7001";
//     //节点、SDK、Toolkit对等目录放置于PTPATH目录下
//       PTPATH = "/opt/";
//       SDKPATH = PTPATH + "wtsdkpufa/";
//       PeerPATH = PTPATH + "wtchainpufa/";
//       ToolPATH = PTPATH + "wttool/";
//       TokenApiPATH = PTPATH + "wtsdkpufa/";
//       PeerTPName = "wtchainpufa";
//       SDKTPName = "wtsdkpufa";
//       ToolTPName = "wttool";
//       TokenTPName = "wtsdkpufa";
//       tmuxSessionTokenApi = "tmux send -t sdk-pufa ";
//       tmuxSessionPeer = "tmux send -t chain-pufa ";
//       tmuxSessionSDK = "tmux send -t sdk-pufa ";
//       sReleaseLocalDir = "E:\\test\\2.3\\";
//       sLatestLocalDir = "E:\\test\\2.4.2\\";
//      destShellScriptDir = "/opt/tjshell/";

    }

}
