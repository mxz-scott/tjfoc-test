package com.tjfintech.common.functionTest.BVT.P1_High;

import com.tjfintech.common.functionTest.Conditions.*;
import com.tjfintech.common.functionTest.contract.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({

        SetAppChain.class,

        SetWVMversionEmpty.class,
        WVMContractTest.class,
        WVMContractInvalidTest.class,

        //测试带版本号合约
        SetWVMversion1.class,

        WVMContractWithVersionTest.class,
        WVMContractWithVersionInvalidTest.class,
        WVMContractWithVersionTest_UpgradeTestOnly.class,

})

//Build Validation Test
public class TS002_Run_Contract {
    //执行这个类将执行suiteClass中的测试项

}
