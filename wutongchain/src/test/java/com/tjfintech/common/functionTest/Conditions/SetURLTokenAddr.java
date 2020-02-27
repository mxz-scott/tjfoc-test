package com.tjfintech.common.functionTest.Conditions;

import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class SetURLTokenAddr {

   @Test
    public void test(){
        UtilsClass.SDKADD = UtilsClass.TOKENADD;
    }

}
