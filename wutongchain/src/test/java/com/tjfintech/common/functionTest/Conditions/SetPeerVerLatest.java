package com.tjfintech.common.functionTest.Conditions;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.ArrayList;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.junit.Assert.assertEquals;

@Slf4j
public class SetPeerVerLatest {

   @Test
    public void test()throws Exception{

       String replacePeer = latestPeer;

       ArrayList<String> hostList = new ArrayList<>();
       hostList.add(PEER1IP);
       hostList.add(PEER2IP);
       hostList.add(PEER4IP);
       hostList.add(PEER3IP);

       //检查所有测试主机是否包含指定的release文件
       for(int i =0 ;i<hostList.size();i++){
           String resp = shExeAndReturn(hostList.get(i),"cd " + PeerPATH + ";find . -name " + replacePeer);
           if(resp.isEmpty())   log.info("host " + hostList.get(i) + " not found file: " + PeerPATH + replacePeer);
           assertEquals(false, resp.isEmpty());

           shellExeCmd(hostList.get(i),"cd " + PeerPATH + ";find . -name \""+ PeerTPName + "*\" | xargs chmod +x");
       }

       //停止现有进程后替换

       for(int i =0 ;i<hostList.size();i++){
           shellExeCmd(hostList.get(i),killPeerCmd,"rm -f " + PeerPATH + PeerTPName,"cp " + PeerPATH + replacePeer + " " + PeerPATH + PeerTPName);
           //确认版本已经更新 版本号不一致
           assertEquals(false,verMap.get("peer_" + hostList.get(i)).equals(shExeAndReturn(hostList.get(i),getPeerVerByShell)));
       }

       //启动替换后的进程
       //节点 不启动动态加入节点
       for(int i =0 ;i<hostList.size()-1;i++){
           shellExeCmd(hostList.get(i),startPeerCmd);
       }
       sleepAndSaveInfo(SLEEPTIME*2,"start peer sleeptime");
    }

}
