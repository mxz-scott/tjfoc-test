package com.tjfintech.common.functionTest.Conditions;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;


import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClass.SDKADD;

@Slf4j
public class SetSDKWalletDisabled {
    CommonFunc commonFunc = new CommonFunc();
    UtilsClass utilsClass = new UtilsClass();

   @Test
    public void setWalletDisabled()throws Exception{
       commonFunc.setSDKWalletEnabled(utilsClass.getIPFromStr(SDKADD),"false");

       shellExeCmd(utilsClass.getIPFromStr(SDKADD),killSDKCmd,startSDKCmd); //重启sdk
       sleepAndSaveInfo(SLEEPTIME/2,"restart sdk after disable wallet");
    }

}
