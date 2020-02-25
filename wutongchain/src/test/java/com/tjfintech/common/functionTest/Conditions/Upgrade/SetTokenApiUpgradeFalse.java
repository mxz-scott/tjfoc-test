package com.tjfintech.common.functionTest.Conditions.Upgrade;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static com.tjfintech.common.utils.UtilsClass.bUpgradeSDK;
import static com.tjfintech.common.utils.UtilsClass.bUpgradeTokenApi;

@Slf4j
public class SetTokenApiUpgradeFalse {

   @Test
    public void test()throws Exception {
        bUpgradeTokenApi = false;
   }
}
