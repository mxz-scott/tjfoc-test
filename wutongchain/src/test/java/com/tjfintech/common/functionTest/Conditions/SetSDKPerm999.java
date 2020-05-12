package com.tjfintech.common.functionTest.Conditions;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;


@Slf4j
public class SetSDKPerm999 {

    CommonFunc commonFunc = new CommonFunc();

   @Test
    public void test()throws Exception{
       UtilsClass utilsClass = new UtilsClass();
       commonFunc.setPerm999WithParam(utilsClass.getSDKID());
    }

}
