package com.tjfintech.common.functionTest.Conditions;

import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class SetSubLedgerSleepTime {

   @Test
    public void test(){
        UtilsClass.SLEEPTIME = 10 * 1000;
    }

}
