package com.tjfintech.common.functionTest.Conditions.Upgrade;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.ArrayList;

import static com.tjfintech.common.utils.FileOperation.uploadFiletoDestDirByssh;
import static com.tjfintech.common.utils.UtilsClass.*;
import static org.junit.Assert.assertEquals;

@Slf4j
public class SetPeerVerRelease {

    @Test
    public void test() throws Exception{
        String replaceTP = releasePeer;

        ArrayList<String> hostList = new ArrayList<>();
        hostList.add(PEER1IP);
        hostList.add(PEER2IP);
        hostList.add(PEER4IP);
        hostList.add(PEER3IP);

        //检查所有测试主机是否包含指定的release文件
        for(int i =0 ;i<hostList.size();i++){
            //windows本地上传版本文件
            String fileDir = sReleaseLocalDir + sLocalPeer;
            shellExeCmd(hostList.get(i),killPeerCmd, "rm -f " + PeerPATH + replaceTP);
            uploadFiletoDestDirByssh(fileDir,hostList.get(i),USERNAME,PASSWD,PeerPATH,replaceTP);

            String resp = shExeAndReturn(hostList.get(i),"cd " + PeerPATH + ";find . -name " + replaceTP);
            if(resp.isEmpty())   log.info("host " + hostList.get(i) + " not found file: " + PeerPATH + replaceTP);
            assertEquals(false, resp.isEmpty());

            shellExeCmd(hostList.get(i),"cd " + PeerPATH + ";find . -name \""+ PeerTPName + "*\" | xargs chmod +x");
        }

        //存在则停止现有进程后替换
        //节点
        for(int i =0 ;i<hostList.size();i++){
            shellExeCmd(hostList.get(i),
                    killPeerCmd,
                    "rm -f " + PeerPATH + PeerTPName,
                    "cp " + PeerPATH + replaceTP + " " + PeerPATH + PeerTPName);
            sleepAndSaveInfo(500,"shell execute waiting...");
        }

        //启动替换后的进程
        //节点仅启动集群节点 不启动动态加入节点
        for(int i =0 ;i<hostList.size()-1;i++){
            shellExeCmd(hostList.get(i),startPeerCmd);
        }
        sleepAndSaveInfo(SLEEPTIME*2,"start peer sleeptime");
    }
}
