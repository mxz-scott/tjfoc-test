package com.tjfintech.common.functionTest.Conditions;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static com.tjfintech.common.CommonFunc.*;
import static com.tjfintech.common.utils.UtilsClass.*;

@Slf4j
public class SetDatabaseMysql {
   CommonFunc commonFunc = new CommonFunc();
   UtilsClass utilsClass = new UtilsClass();
   @Test
    public void setDBMysql()throws Exception{
       //设置SDK 使用mysql 清空数据库  20200915 不再cp config.toml文件
       shellExeCmd(utilsClass.getIPFromStr(SDKADD),killSDKCmd);
       utilsClass.delDataBase();//清空sdk当前使用数据库数据

       //设置节点 清空db数据 并重启
//       setPeerCluster();//设置节点集群默认全部共识节点 1/2/4
       utilsClass.setAndRestartPeerList(clearPeerDB,resetPeerBase);

       //20200615 合约交易不再测试
//      SetAllPeersDockerImagesClear setAllPeersDockerImagesClear = new SetAllPeersDockerImagesClear();
//      setAllPeersDockerImagesClear.clearAllPeersDockerImages();

       //重启SDK
       utilsClass.setAndRestartSDK();

       //检查节点及sdk启动无异常
       commonFunc.checkProgramActive(PEER1IP,PeerTPName);
       commonFunc.checkProgramActive(PEER2IP,PeerTPName);
       commonFunc.checkProgramActive(PEER4IP,PeerTPName);
       commonFunc.checkProgramActive(PEER1IP,SDKTPName);

       //设置管理工具sm3
       shellExeCmd(PEER1IP,"sed -i 's/sha256/sm3/g' " + ToolPATH + "conf/base.toml");
       subLedger = "";
    }

}
