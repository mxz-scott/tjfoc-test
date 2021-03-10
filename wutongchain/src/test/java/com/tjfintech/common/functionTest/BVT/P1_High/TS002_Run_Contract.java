package com.tjfintech.common.functionTest.BVT.P1_High;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.functionTest.Conditions.*;
import com.tjfintech.common.functionTest.contract.WVMContractInvalidTest;
import com.tjfintech.common.functionTest.contract.WVMContractTest;
import com.tjfintech.common.functionTest.contract.WVMContractTest_withVersionUpgradeTest;
import com.tjfintech.common.functionTest.mixTest.VerifyTests;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({

        SetAppChain.class,

        SetWVMversion1.class,
        WVMContractTest.class,
        WVMContractInvalidTest.class,
        WVMContractTest_withVersionUpgradeTest.class,

//        SetWVMversionEmpty.class,
//        WVMContractTest.class,
//        WVMContractInvalidTest.class,

})

//Build Validation Test
public class TS002_Run_Contract {
    //执行这个类将执行suiteClass中的测试项

}
