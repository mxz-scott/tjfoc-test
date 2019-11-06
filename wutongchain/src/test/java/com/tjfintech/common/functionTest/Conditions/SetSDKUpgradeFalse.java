package com.tjfintech.common.functionTest.Conditions;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static com.tjfintech.common.utils.UtilsClass.bUpgradePeer;
import static com.tjfintech.common.utils.UtilsClass.bUpgradeSDK;

@Slf4j
public class SetSDKUpgradeFalse {

   @Test
    public void test()throws Exception {
        bUpgradeSDK = false;
   }
}
