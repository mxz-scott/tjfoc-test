package com.tjfintech.common.functionTest.Conditions;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.ArrayList;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.junit.Assert.assertEquals;

@Slf4j
public class SetContractSysLatest {

   @Test
    public void test()throws Exception{

//       String replacePeer = latestPeer;
       String operateDir =  PeerPATH + "contracts/bin/" ;

       ArrayList<String> hostList = new ArrayList<>();
//       hostList.add(PEER1IP);
//       hostList.add(PEER2IP);
//       hostList.add(PEER4IP);
       hostList.add(PEER3IP);

       //检查所有测试主机是否包含指定的release文件
       for(int i =0 ;i<hostList.size();i++){
           String resp = shExeAndReturn(hostList.get(i),"ls " + operateDir);
           if(!resp.contains(latestContractSys))
               log.info("host " + hostList.get(i) + " not found dir: " + operateDir + latestContractSys);
           assertEquals(true, resp.contains(latestContractSys));
       }

       //停止现有进程后替换系统合约文件

       for(int i =0 ;i<hostList.size();i++){
           shellExeCmd(hostList.get(i),killPeerCmd,
                   "rm -rf " + operateDir + SysContract,
                   "cp -r " + operateDir + latestContractSys + " " + operateDir + SysContract);
       }

       //启动替换后的进程
       //节点 不启动动态加入节点
       for(int i =0 ;i<hostList.size()-1;i++){
           shellExeCmd(hostList.get(i),startPeerCmd);
       }
       sleepAndSaveInfo(SLEEPTIME*2,"start peer sleeptime");
    }

}
