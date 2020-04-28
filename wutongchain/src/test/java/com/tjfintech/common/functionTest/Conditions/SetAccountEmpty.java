package com.tjfintech.common.functionTest.Conditions;

import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class SetAccountEmpty {

   @Test
    public void test(){
        UtilsClass.tokenAccount1 = "";
        UtilsClass.tokenMultiAddr1 = "";
        UtilsClass.ADDRESS1 = "";
        UtilsClass.IMPPUTIONADD = "";
        UtilsClass.MULITADD1 = "";
    }

}
