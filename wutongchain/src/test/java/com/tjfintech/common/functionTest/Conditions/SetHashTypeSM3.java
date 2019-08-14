package com.tjfintech.common.functionTest.Conditions;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static com.tjfintech.common.utils.UtilsClass.*;

@Slf4j
public class SetHashTypeSM3 {

   @Test
    public void setHashsm3()throws Exception{
       //设置SDK 使用sm3 清空数据库
       shellExeCmd(getIPFromStr(SDKADD),resetSDKConfig);
       delDataBase();//清空sdk当前使用数据库数据

       //设置节点使用sm3 清空db数据 并重启
       setAndRestartPeerList(clearPeerDB,resetPeerBase);

       //重启SDK
       setAndRestartSDK();

       //设置管理工具hashtype为sm3
       shellExeCmd(PEER1IP,"cp " + ToolPATH + "conf/baseOK.toml " + ToolPATH + "conf/base.toml");
    }

}
