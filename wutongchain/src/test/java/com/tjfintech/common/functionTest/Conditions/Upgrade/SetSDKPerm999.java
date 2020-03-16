package com.tjfintech.common.functionTest.Conditions.Upgrade;

import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static com.tjfintech.common.CommonFunc.setPerm999WithParam;
import static com.tjfintech.common.utils.UtilsClass.SDKID;
import static com.tjfintech.common.utils.UtilsClass.getSDKID;

@Slf4j
public class SetSDKPerm999 {

   @Test
    public void test()throws Exception{
       setPerm999WithParam(getSDKID());
    }

}
