package com.tjfintech.common.functionTest.Conditions;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static com.tjfintech.common.CommonFunc.*;
import static com.tjfintech.common.utils.UtilsClass.*;

@Slf4j
public class SetHashTypeSM3 {
   UtilsClass utilsClass = new UtilsClass();
   CommonFunc commonFunc = new CommonFunc();
   @Test
    public void setHashsm3()throws Exception{
       //设置SDK 使用sm3 清空数据库
       shellExeCmd(utilsClass.getIPFromStr(SDKADD),resetSDKConfig,killSDKCmd);
       utilsClass.delDataBase();//清空sdk当前使用数据库数据

       //设置节点使用sm3 清空db数据 并重启
       commonFunc.setPeerCluster();//设置节点集群默认全部共识节点 1/2/4
       utilsClass.setAndRestartPeerList(clearPeerDB,resetPeerBase);

       //重启SDK
       utilsClass.setAndRestartSDK();

       //检查节点及sdk启动无异常
       commonFunc.checkProgramActive(PEER1IP,PeerTPName);
       commonFunc.checkProgramActive(PEER2IP,PeerTPName);
       commonFunc.checkProgramActive(PEER4IP,PeerTPName);
       commonFunc.checkProgramActive(PEER1IP,SDKTPName);

       //设置管理工具hashtype为sm3
       shellExeCmd(PEER1IP,"cp " + ToolPATH + "conf/baseOK.toml " + ToolPATH + "conf/base.toml");
    }

}
