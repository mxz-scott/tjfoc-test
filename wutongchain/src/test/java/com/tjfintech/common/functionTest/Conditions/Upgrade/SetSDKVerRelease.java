package com.tjfintech.common.functionTest.Conditions.Upgrade;

import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.ArrayList;

import static com.tjfintech.common.utils.FileOperation.uploadFiletoDestDirByssh;
import static com.tjfintech.common.utils.UtilsClass.*;
import static org.junit.Assert.assertEquals;

@Slf4j
public class SetSDKVerRelease {
    UtilsClass utilsClass = new UtilsClass();

    @Test
    public void test() throws Exception{
        String replaceTP = releaseSDK;

        //windows本地上传版本文件
        String fileDir = sReleaseLocalDir + sLocalSDK;
        shellExeCmd(utilsClass.getIPFromStr(SDKADD),killSDKCmd, "rm -f " + SDKPATH + replaceTP);
        uploadFiletoDestDirByssh(fileDir,utilsClass.getIPFromStr(SDKADD),USERNAME,PASSWD,SDKPATH,replaceTP);

        //检查sdk是否包含release文件
        String response = shExeAndReturn(utilsClass.getIPFromStr(SDKADD),"cd " + SDKPATH + ";find . -name " + replaceTP);
        if(response.isEmpty())   log.info("host " + utilsClass.getIPFromStr(SDKADD) + " not found file: " + SDKPATH + replaceTP);
        assertEquals(false, response.isEmpty());
        shellExeCmd(utilsClass.getIPFromStr(SDKADD),"cd " + SDKPATH + ";find . -name \""+ SDKTPName + "*\" | xargs chmod +x");

        //SDK
        shellExeCmd(utilsClass.getIPFromStr(SDKADD),killSDKCmd, "rm -f " + SDKPATH + SDKTPName,"cp " + SDKPATH + replaceTP + " " + SDKPATH + SDKTPName);

        //SDK
        shellExeCmd(utilsClass.getIPFromStr(SDKADD),startSDKCmd);
    }
}
