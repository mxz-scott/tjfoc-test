package com.tjfintech.common.functionTest.Conditions;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static com.tjfintech.common.utils.UtilsClass.*;

@Slf4j
public class SetTestEnv_Dirzxx {

   @Test
    public void test(){
       //设置测试环境使用的节点端口及部署目录信息
      PEER1IP="10.1.3.240";
      PEER2IP="10.1.3.246";
      PEER3IP="10.1.5.168";
      PEER4IP="10.1.3.247";
      PEER1RPCPort="9000";
      PEER2RPCPort="9000";
      PEER3RPCPort="9000";
      PEER4RPCPort="9000";
      PEER1TCPPort="60000";
      PEER2TCPPort="60000";
      PEER3TCPPort="60000";
      PEER4TCPPort="60000";
      //节点、SDK、Toolkit对等目录放置于PTPATH目录下
      PTPATH="/root/zxx/";
      SDKPATH = PTPATH + "wtsdk/";
      PeerPATH = PTPATH + "wtchain/";
      ToolPATH = PTPATH + "wttool/";
      PeerTPName="wtyp";
      SDKTPName="wtys";
      ToolTPName="wttool";
    }

}
