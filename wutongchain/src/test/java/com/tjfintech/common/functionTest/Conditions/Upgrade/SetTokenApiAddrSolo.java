package com.tjfintech.common.functionTest.Conditions.Upgrade;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static com.tjfintech.common.utils.UtilsClass.*;

@Slf4j
public class SetTokenApiAddrSolo {
   @Test
    public void set(){
       TOKENADD = "http://10.1.3.161:9190";
   }

}
