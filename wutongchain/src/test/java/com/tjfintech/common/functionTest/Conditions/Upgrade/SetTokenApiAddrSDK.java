package com.tjfintech.common.functionTest.Conditions.Upgrade;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static com.tjfintech.common.utils.UtilsClass.TOKENADD;

@Slf4j
public class SetTokenApiAddrSDK {
   @Test
    public void set(){
       TOKENADD = "http://10.1.3.161:7779";
   }

}
