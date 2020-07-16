package com.tjfintech.common.functionTest.Conditions.PersonalTestEnvironment;

import static com.tjfintech.common.utils.UtilsClass.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class SetTestEnv_Dirzllchain201 {

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
     PTPATH = "/root/zll/chain2.0.1/";
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

//    public static String SDKADD = "http://10.1.5.240:7779";
//    public static String rSDKADD = "http://10.1.5.240:7779";
//    public static String TOKENADD = "http://10.1.5.240:7779";
//    //设置测试环境使用的节点端口及部署目录信息
//    public static String PEER1IP = "10.1.3.160";
//    public static String PEER2IP = "10.1.5.240";
//    public static String PEER3IP = "10.1.5.168";
//    public static String PEER4IP = "10.1.3.161";
//    public static String PEER1RPCPort = "9400";
//    public static String PEER2RPCPort = "9500";
//    public static String PEER3RPCPort = "9400";
//    public static String PEER4RPCPort = "9400";
//    public static String PEER1TCPPort = "60011";
//    public static String PEER2TCPPort = "60011";
//    public static String PEER3TCPPort = "60011";
//    public static String PEER4TCPPort = "60012";
//    //节点、SDK、Toolkit对等目录放置于PTPATH目录下
//    public static String PTPATH = "/root/zll/chain2.0.1/";
//    public static String SDKPATH = PTPATH + "sdk/";
//    public static String PeerPATH = PTPATH + "peer/";
//    public static String ToolPATH = PTPATH + "toolkit/";
//    public static String TokenApiPATH = PTPATH + "wtfinservice/";
//    public static String PeerTPName = "Mp";
//    public static String SDKTPName = "sdk";
//    public static String ToolTPName = "toolkit";
//    public static String TokenTPName = "fintoken";
//    public static String tmuxSessionTokenApi = "tmux send -t t_M2 ";
//    public static String tmuxSessionPeer = "tmux send -t M2 ";
//    public static String tmuxSessionSDK = "tmux send -t s_M2 ";
//    public static String sReleaseLocalDir = "D:\\GoWorks\\src\\github.com\\tjfoc\\wtsys-release\\release\\梧桐链已发布版本\\2.4.x\\2.4.1\\";
//    public static String sLatestLocalDir = "D:\\GoWorks\\src\\github.com\\tjfoc\\tjfoc\\bin\\";

   }

}
