package com.tjfintech.common.functionTest.Conditions;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static com.tjfintech.common.utils.UtilsClass.*;

@Slf4j
public class SetMgToolPerm999 {
    CommonFunc commonFunc = new CommonFunc();
    UtilsClass utilsClass = new UtilsClass();

   @Test
    public void test()throws Exception{
       commonFunc.setPerm999WithParam(
               commonFunc.getIDByMgTool(PEER1IP,ToolPATH + "crypt/key.pem"));
    }

}
