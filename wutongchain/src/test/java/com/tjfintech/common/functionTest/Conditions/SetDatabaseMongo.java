package com.tjfintech.common.functionTest.Conditions;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static com.tjfintech.common.CommonFunc.setSDKWalletAddrDBMongo;
import static com.tjfintech.common.utils.UtilsClass.*;

@Slf4j
public class SetDatabaseMongo {

   @Test
    public void setDBMongo()throws Exception{
       //设置SDK 使用Mongo 清空数据库
       shellExeCmd(getIPFromStr(SDKADD),resetSDKConfig);
       setSDKWalletAddrDBMongo(getIPFromStr(SDKADD));
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
