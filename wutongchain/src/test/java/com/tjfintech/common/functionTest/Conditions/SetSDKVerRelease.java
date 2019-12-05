package com.tjfintech.common.functionTest.Conditions;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.ArrayList;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.junit.Assert.assertEquals;

@Slf4j
public class SetSDKVerRelease {

    @Test
    public void test() throws Exception{
        String replaceSDK = releaseSDK;

        //检查sdk是否包含release文件
        String response = shExeAndReturn(getIPFromStr(SDKADD),"cd " + SDKPATH + ";find . -name " + replaceSDK);
        if(response.isEmpty())   log.info("host " + getIPFromStr(SDKADD) + " not found file: " + SDKPATH + replaceSDK);
        assertEquals(false, response.isEmpty());
        shellExeCmd(getIPFromStr(SDKADD),"cd " + SDKPATH + ";find . -name \""+ SDKTPName + "*\" | xargs chmod +x");

        //SDK
        shellExeCmd(getIPFromStr(SDKADD),killSDKCmd, "rm -f " + SDKPATH + SDKTPName,"cp " + SDKPATH + replaceSDK + " " + SDKPATH + SDKTPName);
        //从2.1.190826.1版本升级时，需要注释掉下面这行。
        verMap.put("sdk_"+ getIPFromStr(SDKADD),shExeAndReturn(getIPFromStr(SDKADD),getSDKVerByShell));//存储release sdk版本 以便后续检查

        //SDK
        shellExeCmd(getIPFromStr(SDKADD),startSDKCmd);
    }
}
