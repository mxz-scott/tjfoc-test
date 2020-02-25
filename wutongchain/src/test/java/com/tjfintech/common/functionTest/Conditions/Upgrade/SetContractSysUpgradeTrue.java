package com.tjfintech.common.functionTest.Conditions.Upgrade;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static com.tjfintech.common.utils.UtilsClass.*;

@Slf4j
public class SetContractSysUpgradeTrue {

   @Test
    public void test()throws Exception {
       bUpgradeContractSys = true;
   }
}
