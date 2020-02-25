package com.tjfintech.common.functionTest.Conditions.Upgrade;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static com.tjfintech.common.utils.UtilsClass.bUpgradeSDK;

@Slf4j
public class SetSDKUpgradeTrue {

   @Test
    public void test()throws Exception {
        bUpgradeSDK = true;
   }
}
