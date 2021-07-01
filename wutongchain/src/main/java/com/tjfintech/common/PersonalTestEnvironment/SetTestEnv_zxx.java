package com.tjfintech.common.PersonalTestEnvironment;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static com.tjfintech.common.utils.UtilsClass.*;

@Slf4j
public class SetTestEnv_zxx {

   @Test
    public void setEnvParam(){

       //设置测试环境使用的节点端口及部署目录信息
      SDKADD = "http://10.1.3.164:7310";
      TOKENADD = SDKADD;
      rSDKADD = SDKADD;
      PEER1IP="10.1.3.162";
      PEER2IP="10.1.3.163";
      PEER4IP="10.1.3.164";
      PEER3IP="10.1.5.161";
      PEER1RPCPort="9300";
      PEER2RPCPort="9300";
      PEER3RPCPort="9300";
      PEER4RPCPort="9300";
      PEER1TCPPort="60030";
      PEER2TCPPort="60030";
      PEER3TCPPort="60030";
      PEER4TCPPort="60030";
      //节点、SDK、Toolkit对等目录放置于PTPATH目录下
      PTPATH="/root/auto/";
      SDKPATH = PTPATH + "sdk/";
      PeerPATH = PTPATH + "peer/";
      ToolPATH = PTPATH + "toolkit/";
      PeerTPName="wtchain";
      SDKTPName="wtsdk";
      ToolTPName="wttool";
     destShellScriptDir = "/root/tjshell/";
    }

}
