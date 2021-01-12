package com.tjfintech.common.functionTest.upgrade;

import com.tjfintech.common.functionTest.Conditions.SetURLToSDKAddr;
import com.tjfintech.common.functionTest.tokenModuleTest.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({

        TokenAccurateTest.class,
        TokenInterfaceTest.class,
        TokenMultiInvalidTest.class,
        TokenMultiTest.class,
        TokenPrivateStoreTest.class,
        TokenSoloInvalidTest.class,
        TokenSoloTest.class,
        TokenStoreTest.class,
        TokenTxTypeTest_SDK.class,
        TokenTxTypeTest_Token.class,
        SetURLToSDKAddr.class,

})

//Build Validation Test
public class UpgradeFinserviceTest {
    //执行这个类将执行suiteClass中的测试项

}
