package com.tjfintech.common.functionTest.Conditions;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static com.tjfintech.common.CommonFunc.setSDKWalletEnabled;
import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClass.SDKADD;

@Slf4j
public class SetSDKWalletDisabled {

   @Test
    public void setWalletDisabled()throws Exception{
       setSDKWalletEnabled(getIPFromStr(SDKADD),"false");

       shellExeCmd(getIPFromStr(SDKADD),killSDKCmd,startSDKCmd); //重启sdk
       sleepAndSaveInfo(SLEEPTIME/2,"restart sdk after disable wallet");
    }

}
