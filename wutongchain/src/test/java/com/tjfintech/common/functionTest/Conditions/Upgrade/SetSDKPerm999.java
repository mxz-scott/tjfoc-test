package com.tjfintech.common.functionTest.Conditions.Upgrade;

import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static com.tjfintech.common.CommonFunc.setPerm999WithParam;

@Slf4j
public class SetSDKPerm999 {

   @Test
    public void test()throws Exception{
       UtilsClass utilsClass = new UtilsClass();
       setPerm999WithParam(utilsClass.getSDKID());
    }

}
