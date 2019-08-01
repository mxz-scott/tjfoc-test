package com.tjfintech.common.functionTest.Conditions;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static com.tjfintech.common.utils.UtilsClass.*;

@Slf4j
public class SetDatabaseMysql {

   @Test
    public void setDBMysql()throws Exception{
       //设置SDK 使用mysql 清空数据库
       shellExeCmd(getIPFromStr(SDKADD),"cp " + PTPATH + "sdk/conf/configMysql.toml " + PTPATH + "sdk/conf/config.toml");
       delDataBase();//清空sdk当前使用数据库数据

       //设置节点 清空db数据 并重启
       setAndRestartPeerList("rm -rf "+ PTPATH + "peer/*.db "
               ,"cp " + PTPATH + "peer/conf/baseOK.toml " + PTPATH + "peer/conf/base.toml");

       //重启SDK
       setAndRestartSDK();

    }

}
