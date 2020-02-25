package com.tjfintech.common.functionTest.Conditions.Upgrade;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static com.tjfintech.common.utils.FileOperation.uploadFiletoDestDirByssh;
import static com.tjfintech.common.utils.UtilsClass.*;
import static org.junit.Assert.assertEquals;

@Slf4j
public class SetTokenApiVerRelease {

    @Test
    public void test() throws Exception{
        String replaceTP = releaseTokenApi;

        //windows本地上传版本文件
        String fileDir = sReleaseLocalDir + sLocalTokenApi;
        shellExeCmd(getIPFromStr(TOKENADD),killTokenApiCmd, "rm -f " + TokenApiPATH + replaceTP);
        uploadFiletoDestDirByssh(fileDir,getIPFromStr(TOKENADD),USERNAME,PASSWD,TokenApiPATH,replaceTP);

        //检查sdk是否包含release文件
        String response = shExeAndReturn(getIPFromStr(TOKENADD),"cd " + TokenApiPATH + ";find . -name " + replaceTP);
        if(response.isEmpty())   log.info("host " + getIPFromStr(TOKENADD) + " not found file: " + TokenApiPATH + replaceTP);
        assertEquals(false, response.isEmpty());
        shellExeCmd(getIPFromStr(TOKENADD),"cd " + TokenApiPATH + ";find . -name \""+ TokenTPName + "*\" | xargs chmod +x");

        //SDK
        shellExeCmd(getIPFromStr(TOKENADD),killTokenApiCmd, "rm -f " + TokenApiPATH + TokenTPName,
                "cp " + TokenApiPATH + replaceTP + " " + TokenApiPATH + TokenTPName);
        //从2.1.190826.1版本升级时，需要注释掉下面这行。
        verMap.put("token_"+ getIPFromStr(TOKENADD),shExeAndReturn(getIPFromStr(TOKENADD),getTokenApiVerByShell));//存储release sdk版本 以便后续检查

        //SDK
        shellExeCmd(getIPFromStr(TOKENADD),startTokenApiCmd);
    }
}
