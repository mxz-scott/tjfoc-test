package com.tjfintech.common.functionTest.BVT.P1_High;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.functionTest.Conditions.SetCertSM2;
import com.tjfintech.common.functionTest.Conditions.SetURLToSDKAddr;
import com.tjfintech.common.functionTest.Conditions.SetWVMversionEmpty;
import com.tjfintech.common.functionTest.contract.WVMContractInvalidTest;
import com.tjfintech.common.functionTest.contract.WVMContractTest;
import com.tjfintech.common.functionTest.mixTest.VerifyTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({

        SetCertSM2.class,
//        PeerStartNoPermTest.class,
        BeforeCondition.class,
        SetURLToSDKAddr.class,

//        SetWVMversion1.class,
//        WVMContractTest.class,
//        WVMContractInvalidTest.class,
//        WVMContractTest_withVersionUpgradeTest.class,

        SetWVMversionEmpty.class,
        WVMContractTest.class,
        WVMContractInvalidTest.class,

})

//Build Validation Test
public class TS002_Run_Contract {
    //执行这个类将执行suiteClass中的测试项

}
