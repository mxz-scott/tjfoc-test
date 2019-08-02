package com.tjfintech.common.functionTest.Conditions;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static com.tjfintech.common.utils.UtilsClass.*;

@Slf4j
public class SetDatabaseMongo {

   @Test
    public void setDBMongo()throws Exception{
       //设置SDK 使用Mongo 清空数据库
       shellExeCmd(getIPFromStr(SDKADD),resetSDKConfig);
       delDataBase();//清空sdk当前使用数据库数据

       //设置节点 停止所有节点 清空所有节点db数据 并重启所有节点
       peerList.clear();
       peerList.add(PEER1IP);
       peerList.add(PEER2IP);
       peerList.add(PEER4IP);
       sendCmdPeerList(peerList,clearPeerDB,resetPeerBase,startPeerCmd );

       //重启SDK
       setAndRestartSDK();

    }

}
