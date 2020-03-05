package com.tjfintech.common.functionTest.Conditions.Upgrade;

import com.tjfintech.common.BeforeCondition;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static com.tjfintech.common.utils.UtilsClass.*;

@Slf4j
public class SetTokenAccountUpdate {

   @Test
    public void test()throws Exception {
        userId01 = "tkAc1" + Random(6);
        userId02 = "tkAc2" + Random(6);
        userId03 = "tkAc3" + Random(6);
        userId04 = "tkAc4" + Random(6);
        userId05 = "tkAc5" + Random(6);
        userId06 = "tkAc6" + Random(6);
        userId07 = "tkAc7" + Random(6);

       SDKADD = TOKENADD;
       BeforeCondition beforeCondition = new BeforeCondition();
       beforeCondition.createTokenAccount();
       beforeCondition.tokenAddIssueCollAddr();
   }
}
