package com.tjfintech.common.functionTest.Conditions.Upgrade;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static com.tjfintech.common.CommonFunc.getIDByMgTool;
import static com.tjfintech.common.CommonFunc.setPerm999WithParam;
import static com.tjfintech.common.utils.UtilsClass.*;

@Slf4j
public class SetTokenApiPerm999 {

   @Test
    public void test()throws Exception{
       setPerm999WithParam(getIDByMgTool(getIPFromStr(TOKENADD),TokenApiPATH + "auth/key.pem"));
    }

}
