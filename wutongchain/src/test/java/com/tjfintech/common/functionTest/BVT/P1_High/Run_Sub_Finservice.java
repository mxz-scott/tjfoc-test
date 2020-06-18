package com.tjfintech.common.functionTest.BVT.P1_High;

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
        SetTokenApiPerm999.class,
        TokenInterfaceTest.class,
        TokenMultiTest.class,
        TokenMultiInvalidTest.class,
        TokenSoloTest.class,
        TokenSoloInvalidTest.class,
        TokenAccurateTest.class,
        TokenPrivateStoreTest.class,
        TokenStoreTest.class,
        SDKToTokenMultiTest.class,
        TokenTxTypeTest_SDK.class,
        TokenTxTypeTest_Token.class,
        SetURLToSDKAddr.class,

})

//Build Validation Test
public class Run_Sub_Finservice {
    //执行这个类将执行suiteClass中的测试项

}
