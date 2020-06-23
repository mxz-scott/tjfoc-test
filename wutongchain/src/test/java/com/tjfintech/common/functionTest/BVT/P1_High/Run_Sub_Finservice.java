package com.tjfintech.common.functionTest.BVT.P1_High;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.functionTest.Conditions.*;
import com.tjfintech.common.functionTest.Conditions.SetTokenApiPerm999;
import com.tjfintech.common.functionTest.tokenModuleTest.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
        SetDatabaseMysql.class,
//        SetTokenApiDatabaseMysql.class,
        SetAccountEmpty.class,
        SetSubLedger.class,
        SetSubLedgerSleepTime.class,
        BeforeCondition.class,
        SetTokenApiPerm999.class,

        TokenInterfaceTest.class,
        TokenMultiTest.class,
        TokenSoloTest.class,
        TokenStoreTest.class,
        TokenPrivateStoreTest.class,
        TokenAccurateTest.class,

        SDKToTokenMultiTest.class,

        TokenTxTypeTest_SDK.class,
        TokenTxTypeTest_Token.class,

        TokenMultiInvalidTest.class,
        TokenSoloInvalidTest.class,

        SetURLToSDKAddr.class,

})

//Build Validation Test
public class Run_Sub_Finservice {
    //执行这个类将执行suiteClass中的测试项

}
