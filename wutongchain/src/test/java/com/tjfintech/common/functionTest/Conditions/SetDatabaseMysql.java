package com.tjfintech.common.functionTest.Conditions;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static com.tjfintech.common.CommonFunc.*;
import static com.tjfintech.common.utils.UtilsClass.*;

@Slf4j
public class SetDatabaseMysql {
   UtilsClass utilsClass = new UtilsClass();
   @Test
    public void setDBMysql()throws Exception{
       //设置SDK 使用mysql 清空数据库
       shellExeCmd(utilsClass.getIPFromStr(SDKADD),resetSDKConfig,killSDKCmd);
       utilsClass.delDataBase();//清空sdk当前使用数据库数据

       //设置节点 清空db数据 并重启
//       setPeerCluster();//设置节点集群默认全部共识节点 1/2/4
       utilsClass.setAndRestartPeerList(clearPeerDB,resetPeerBase);

       //重启SDK
       shellExeCmd(utilsClass.getIPFromStr(SDKADD),startSDKCmd);

       //检查节点及sdk启动无异常
       checkProgramActive(PEER1IP,PeerTPName);
       checkProgramActive(PEER2IP,PeerTPName);
       checkProgramActive(PEER4IP,PeerTPName);
       checkProgramActive(PEER1IP,SDKTPName);

       //设置管理工具sm3
       shellExeCmd(PEER1IP,"sed -i 's/sha256/sm3/g' " + ToolPATH + "conf/base.toml");
       subLedger = "";
    }

}
