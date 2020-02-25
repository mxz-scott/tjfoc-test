package com.tjfintech.common.functionTest.Conditions.Upgrade;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.ArrayList;

import static com.tjfintech.common.utils.FileOperation.uploadFiletoDestDirByssh;
import static com.tjfintech.common.utils.UtilsClass.*;
import static org.junit.Assert.assertEquals;

@Slf4j
public class SetSDKVerLatest {

   @Test
    public void test()throws Exception{
       String replaceTP = latestSDK;

      //windows本地上传版本文件
      String fileDir = sLatestLocalDir + sLocalSDK;
      shellExeCmd(getIPFromStr(SDKADD),killSDKCmd, "rm -f " + SDKPATH + replaceTP);
      uploadFiletoDestDirByssh(fileDir,getIPFromStr(SDKADD),USERNAME,PASSWD,SDKPATH,replaceTP);

       //检查sdk是否包含release文件
       String response = shExeAndReturn(getIPFromStr(SDKADD),"cd " + SDKPATH + ";find . -name " + replaceTP);
       if(response.isEmpty())   log.info("host " + getIPFromStr(SDKADD) + " not found file: " + SDKPATH + replaceTP);
       assertEquals(false, response.isEmpty());
       shellExeCmd(getIPFromStr(SDKADD),"cd " + SDKPATH + ";find . -name \""+ SDKTPName + "*\" | xargs chmod +x");


       //SDK
       shellExeCmd(getIPFromStr(SDKADD),killSDKCmd, "rm -f " + SDKPATH + SDKTPName,"cp " + SDKPATH + replaceTP + " " + SDKPATH + SDKTPName);
       assertEquals(false,verMap.get("sdk_" + getIPFromStr(SDKADD)).equals(shExeAndReturn(getIPFromStr(SDKADD),getSDKVerByShell)));

       //SDK
       shellExeCmd(getIPFromStr(SDKADD),startSDKCmd);
    }

}
