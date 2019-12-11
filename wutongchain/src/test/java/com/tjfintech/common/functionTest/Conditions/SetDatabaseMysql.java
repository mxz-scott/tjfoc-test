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
       shellExeCmd(getIPFromStr(SDKADD),"cp " + SDKPATH + "conf/configMysql.toml " + SDKPATH + "conf/config.toml");
       delDataBase();//清空sdk当前使用数据库数据

       //设置节点 清空db数据 并重启
       setAndRestartPeerList(clearPeerDB,resetPeerBase);

       //重启SDK
       setAndRestartSDK();
       subLedger = "";
    }

}
