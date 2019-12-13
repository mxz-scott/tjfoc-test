package com.tjfintech.common.functionTest.Conditions;

import com.tjfintech.common.BeforeCondition;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static com.tjfintech.common.utils.UtilsClass.*;

@Slf4j
public class SetDatabaseMysql {

   @Test
    public void setDBMysql()throws Exception{
       //设置SDK 使用mysql 清空数据库
       shellExeCmd(getIPFromStr(SDKADD),resetSDKConfig);
       delDataBase();//清空sdk当前使用数据库数据

       //设置节点 清空db数据 并重启
       setAndRestartPeerList(clearPeerDB,resetPeerBase);

       //重启SDK
       setAndRestartSDK();

       //设置管理工具sm3
       shellExeCmd(PEER1IP,"sed -i 's/sha256/sm3/g' " + ToolPATH + "conf/base.toml");
       subLedger = "";
    }

}
