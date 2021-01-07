package com.tjfintech.common.functionTest.BVT.P1_High;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.functionTest.Conditions.SetCertSM2;
import com.tjfintech.common.functionTest.Conditions.SetURLToSDKAddr;
import com.tjfintech.common.functionTest.mixTest.VerifyTests;
import com.tjfintech.common.functionTest.smartTokenTest.smtInterfaceTest;
import com.tjfintech.common.functionTest.smartTokenTest.smtMultiInvalidTest;
import com.tjfintech.common.functionTest.smartTokenTest.smtMultiTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({

        SetCertSM2.class,
//        PeerStartNoPermTest.class,
        BeforeCondition.class,
        SetURLToSDKAddr.class,

        smtInterfaceTest.class,
        smtMultiTest.class,
        smtMultiInvalidTest.class,
        VerifyTests.class,

})

//Build Validation Test
public class TS003_Run_SmartToken {
    //执行这个类将执行suiteClass中的测试项

}
