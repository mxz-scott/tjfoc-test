package com.tjfintech.common.functionTest.Conditions;

import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class SetPrecision10 {

   @Test
    public void test(){
        UtilsClass.PRECISION = 10;
    }

}
