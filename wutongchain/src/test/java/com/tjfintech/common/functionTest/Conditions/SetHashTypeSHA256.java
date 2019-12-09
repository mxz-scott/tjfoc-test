package com.tjfintech.common.functionTest.Conditions;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClass.PTPATH;

@Slf4j
public class SetHashTypeSHA256 {

   @Test
    public void setHashSHA256()throws Exception{
       //设置SDK 使用SHA256 清空数据库
       shellExeCmd(getIPFromStr(SDKADD),resetSDKConfig,"sed -i 's/sm3/sha256/g' " + SDKPATH + "conf/config.toml");
       delDataBase();//清空sdk当前使用数据库数据

       //设置节点使用sha256 清空db数据 并重启
       setAndRestartPeerList(clearPeerDB,resetPeerBase,"sed -i 's/sm3/sha256/g' " + PeerPATH + "conf/base.toml");

       //重启SDK
       setAndRestartSDK();

       //设置管理工具hashtype为sha256
       shellExeCmd(PEER1IP,"sed -i 's/sm3/sha256/g' " + ToolPATH + "conf/base.toml");
    }

}
