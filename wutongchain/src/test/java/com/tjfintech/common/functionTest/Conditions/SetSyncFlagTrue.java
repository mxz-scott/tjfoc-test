package com.tjfintech.common.functionTest.Conditions;

import com.tjfintech.common.functionTest.JmlTest.JmlTest;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static com.tjfintech.common.utils.UtilsClass.MULITADD1;

@Slf4j
public class SetSyncFlagTrue {

   @Test
    public void test(){
        UtilsClass.syncFlag = true;

    }

}
