package com.tjfintech.common.functionTest.Conditions;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClass.SDKADD;

@Slf4j
public class SetSDKWalletDisabled {

   @Test
    public void setWalletDisabled()throws Exception{
       String sdkIP = getIPFromStr(SDKADD);
       String resp = shExeAndReturn(sdkIP,"grep -n \"\\[Wallet\\]\" "+ SDKPATH + "conf/config.toml | cut -d \":\" -f 1 ");
       String checkLineNo = String.valueOf(Integer.parseInt(resp) + 1);//正常配置文件中[Wallet]下面一行就是Enabled=true 如果不是此种情况则设置无效
       shExeAndReturn(sdkIP,"sed -i \"" + checkLineNo + "s/true/false/g\" " + SDKPATH + "conf/config.toml");

       shellExeCmd(sdkIP,killSDKCmd,startSDKCmd); //重启sdk
       sleepAndSaveInfo(SLEEPTIME/2,"restart sdk after disable wallet");
    }

}
