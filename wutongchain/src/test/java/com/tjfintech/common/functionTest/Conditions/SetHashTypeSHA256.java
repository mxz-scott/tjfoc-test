package com.tjfintech.common.functionTest.Conditions;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static com.tjfintech.common.CommonFunc.*;
import static com.tjfintech.common.utils.UtilsClass.*;

@Slf4j
public class SetHashTypeSHA256 {

   @Test
    public void setHashSHA256()throws Exception{
       //设置SDK 使用SHA256 清空数据库
       shellExeCmd(getIPFromStr(SDKADD),resetSDKConfig,"sed -i 's/sm3/sha256/g' " + SDKConfigPath);
       delDataBase();//清空sdk当前使用数据库数据

       //设置节点使用sha256 清空db数据 并重启
       setPeerCluster();//设置节点集群默认全部共识节点 1/2/4
       setAndRestartPeerList(clearPeerDB,resetPeerBase,"sed -i 's/sm3/sha256/g' " + PeerBaseConfigPath);

       //重启SDK
       setAndRestartSDK();

       //检查节点及sdk启动无异常
       checkProgramActive(PEER1IP,PeerTPName);
       checkProgramActive(PEER2IP,PeerTPName);
       checkProgramActive(PEER4IP,PeerTPName);
       checkProgramActive(PEER1IP,SDKTPName);

       //设置管理工具hashtype为sha256
       shellExeCmd(PEER1IP,"sed -i 's/sm3/sha256/g' " + ToolPATH + "conf/base.toml");
    }

}
