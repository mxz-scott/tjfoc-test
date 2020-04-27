package com.tjfintech.common.functionTest.Conditions.Upgrade;

import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static com.tjfintech.common.utils.FileOperation.uploadFiletoDestDirByssh;
import static com.tjfintech.common.utils.UtilsClass.*;
import static org.junit.Assert.assertEquals;

@Slf4j
public class SetTokenApiVerRelease {

    UtilsClass utilsClass = new UtilsClass();
    @Test
    public void test() throws Exception{
        String replaceTP = releaseTokenApi;

        //windows本地上传版本文件
        String fileDir = sReleaseLocalDir + sLocalTokenApi;
        shellExeCmd(utilsClass.getIPFromStr(TOKENADD),killTokenApiCmd, "rm -f " + TokenApiPATH + replaceTP);
        uploadFiletoDestDirByssh(fileDir,utilsClass.getIPFromStr(TOKENADD),USERNAME,PASSWD,TokenApiPATH,replaceTP);

        //检查sdk是否包含release文件
        String response = shExeAndReturn(utilsClass.getIPFromStr(TOKENADD),"cd " + TokenApiPATH + ";find . -name " + replaceTP);
        if(response.isEmpty())   log.info("host " + utilsClass.getIPFromStr(TOKENADD) + " not found file: " + TokenApiPATH + replaceTP);
        assertEquals(false, response.isEmpty());
        shellExeCmd(utilsClass.getIPFromStr(TOKENADD),"cd " + TokenApiPATH + ";find . -name \""+ TokenTPName + "*\" | xargs chmod +x");

        shellExeCmd(utilsClass.getIPFromStr(TOKENADD),killTokenApiCmd, "rm -f " + TokenApiPATH + TokenTPName,
                "cp " + TokenApiPATH + replaceTP + " " + TokenApiPATH + TokenTPName);
        shellExeCmd(utilsClass.getIPFromStr(TOKENADD),startTokenApiCmd);
    }
}
