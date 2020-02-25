package com.tjfintech.common.functionTest.Conditions.Upgrade;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;

import static com.tjfintech.common.utils.FileOperation.uploadFiletoDestDirByssh;
import static com.tjfintech.common.utils.UtilsClass.*;
import static org.junit.Assert.assertEquals;

@Slf4j
public class SetContractSysRelease {

   @Test
    public void test()throws Exception{
       String operateDir =  PeerPATH + "contracts/bin/" ;

       ArrayList<String> hostList = new ArrayList<>();
       hostList.add(PEER1IP);
       hostList.add(PEER2IP);
       hostList.add(PEER4IP);
       hostList.add(PEER3IP);

       //检查所有测试主机是否包含指定的release文件
       for(int i =0 ;i< hostList.size();i++){
           //windows本地上传版本文件
           String fileDir = sReleaseLocalDir + sLocalStoreContract;
           shellExeCmd(hostList.get(i),"rm -rf " + operateDir + releaseContractSys);

           File file = new File(fileDir);		//获取其file对象
           File[] fs = file.listFiles();	//遍历path下的文件和目录，放在File数组中
           for(File filedes : fs){					//遍历File[]数组
               if(!filedes.isDirectory())		//若非目录(即文件)，则打印
               {
                   uploadFiletoDestDirByssh(filedes.getAbsolutePath(), hostList.get(i), USERNAME, PASSWD,
                           operateDir + releaseContractSys, "");
               }
           }

           String resp = shExeAndReturn(hostList.get(i),"ls " + operateDir);
           if(!resp.contains(releaseContractSys))
               log.info("host " + hostList.get(i) + " not found dir: " + operateDir + releaseContractSys);
           assertEquals(true, resp.contains(releaseContractSys));
       }

       //停止现有进程后替换系统合约文件

       for(int i =0 ;i<hostList.size();i++){
           shellExeCmd(hostList.get(i),killPeerCmd,
                   "rm -rf " + operateDir + SysContract,
                   "cp -r " + operateDir + releaseContractSys + " " + operateDir + SysContract);
       }

       //启动替换后的进程
       //节点 不启动动态加入节点
       for(int i =0 ;i<hostList.size()-1;i++){
           shellExeCmd(hostList.get(i),startPeerCmd);
       }
       sleepAndSaveInfo(SLEEPTIME*2,"start peer sleeptime");
    }

}
