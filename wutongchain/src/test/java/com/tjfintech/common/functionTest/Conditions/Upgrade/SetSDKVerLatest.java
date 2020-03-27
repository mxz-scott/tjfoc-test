package com.tjfintech.common.functionTest.Conditions.Upgrade;

import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.ArrayList;

import static com.tjfintech.common.utils.FileOperation.uploadFiletoDestDirByssh;
import static com.tjfintech.common.utils.UtilsClass.*;
import static org.junit.Assert.assertEquals;

@Slf4j
public class SetSDKVerLatest {
    UtilsClass utilsClass = new UtilsClass();

   @Test
    public void test()throws Exception{
       String replaceTP = latestSDK;

      //windows本地上传版本文件
      String fileDir = sLatestLocalDir + sLocalSDK;
      shellExeCmd(utilsClass.getIPFromStr(SDKADD),killSDKCmd, "rm -f " + SDKPATH + replaceTP);
      uploadFiletoDestDirByssh(fileDir,utilsClass.getIPFromStr(SDKADD),USERNAME,PASSWD,SDKPATH,replaceTP);

       //检查sdk是否包含release文件
       String response = shExeAndReturn(utilsClass.getIPFromStr(SDKADD),"cd " + SDKPATH + ";find . -name " + replaceTP);
       if(response.isEmpty())   log.info("host " + utilsClass.getIPFromStr(SDKADD) + " not found file: " + SDKPATH + replaceTP);
       assertEquals(false, response.isEmpty());
       shellExeCmd(utilsClass.getIPFromStr(SDKADD),"cd " + SDKPATH + ";find . -name \""+ SDKTPName + "*\" | xargs chmod +x");

       //存储当前 sdk版本 以便后续检查
       verMap.put("sdk_"+ utilsClass.getIPFromStr(SDKADD),shExeAndReturn(utilsClass.getIPFromStr(SDKADD),getSDKVerByShell));

       //SDK
       shellExeCmd(utilsClass.getIPFromStr(SDKADD),killSDKCmd, "rm -f " + SDKPATH + SDKTPName,"cp " + SDKPATH + replaceTP + " " + SDKPATH + SDKTPName);
       assertEquals(false,verMap.get("sdk_" + utilsClass.getIPFromStr(SDKADD)).equals(shExeAndReturn(utilsClass.getIPFromStr(SDKADD),getSDKVerByShell)));

       //SDK
       shellExeCmd(utilsClass.getIPFromStr(SDKADD),startSDKCmd);
    }

}
