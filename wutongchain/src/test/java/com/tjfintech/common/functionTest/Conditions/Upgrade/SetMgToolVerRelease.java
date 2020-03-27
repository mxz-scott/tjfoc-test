package com.tjfintech.common.functionTest.Conditions.Upgrade;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.ArrayList;

import static com.tjfintech.common.utils.FileOperation.uploadFiletoDestDirByssh;
import static com.tjfintech.common.utils.UtilsClass.*;
import static org.junit.Assert.assertEquals;

@Slf4j
public class SetMgToolVerRelease {

    @Test
    public void test() throws Exception{
        String replaceTP = releaseMgTool;

        ArrayList<String> hostList = new ArrayList<>();
        hostList.add(PEER1IP);
        hostList.add(PEER2IP);
        hostList.add(PEER4IP);
        hostList.add(PEER3IP);

        //检查所有测试主机是否包含指定的release文件
        for(int i =0 ;i<hostList.size();i++){
            //windows本地上传版本文件
            String fileDir = sReleaseLocalDir + sLocalMgTool;
            shellExeCmd(hostList.get(i), "rm -f " + ToolPATH + replaceTP);
            uploadFiletoDestDirByssh(fileDir,hostList.get(i),USERNAME,PASSWD,ToolPATH,replaceTP);

            String resp = shExeAndReturn(hostList.get(i),"cd " + ToolPATH + ";find . -name " + replaceTP);
            if(resp.isEmpty())   log.info("host " + hostList.get(i) + " not found file: " + ToolPATH + replaceTP);
            assertEquals(false, resp.isEmpty());

            shellExeCmd(hostList.get(i),"cd " + ToolPATH + ";find . -name \""+ ToolTPName + "*\" | xargs chmod +x");
        }

        for(int i =0 ;i<hostList.size();i++){
            shellExeCmd(hostList.get(i),
                    "rm -f " + ToolPATH + ToolTPName,
                    "cp " + ToolPATH + replaceTP + " " + ToolPATH + ToolTPName);
        }
    }
}
