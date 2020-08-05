package com.tjfintech.common.functionTest.Conditions;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static com.tjfintech.common.utils.UtilsClass.wvmVersion;

@Slf4j
public class SetWVMversionEmpty {

   @Test
    public void setMainledger()throws Exception{
       wvmVersion = "";
    }

}
