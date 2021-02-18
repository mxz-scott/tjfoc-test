package com.tjfintech.common.functionTest.BVT.P1_High;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.functionTest.Conditions.*;

import com.tjfintech.common.functionTest.Conditions.SetTokenApiPerm999;
import com.tjfintech.common.functionTest.sendMessage.CallBack;
import com.tjfintech.common.functionTest.sendMessage.Event;
import com.tjfintech.common.functionTest.tokenModuleTest.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
//        SetDatabaseMysql.class,
////        SetTokenApiDatabaseMysql.class,
//        SetAccountEmpty.class,
//        SetMainLedger.class,
//        SetURLToSDKAddr.class,
//        BeforeCondition.class,
//        SetTokenApiPerm999.class,

        TokenInterfaceTest.class,

        TokenMultiTest.class,
        TokenPrivateStoreTest.class,
        TokenSoloTest.class,
        TokenStoreTest.class,

        TokenAccurateTest.class,
        CallBack.class,
        Event.class,

        TokenMultiInvalidTest.class,
        TokenSoloInvalidTest.class,

        TokenTxTypeTest_Token.class,
        TokenTwoApiMultiTest.class,  //需要部署第二个api并且配置在该class的TOKENADD2参数

})

//Build Validation Test
public class Run_Main_Finservice {
    //执行这个类将执行suiteClass中的测试项

}
