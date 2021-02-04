package com.tjfintech.common.functionTest.BVT.P1_High;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.functionTest.Conditions.SetCertSM2;
import com.tjfintech.common.functionTest.Conditions.SetURLToSDKAddr;
import com.tjfintech.common.functionTest.appChainTest.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({

        AppChain_Create_01.class,
        AppChain_Create_02.class,
        AppChain_FRDG.class,
        AppChain_MultiChainsTest.class,
        AppChain_MultiChainsTest02.class,
        AppChain_MultiThreadSampleTest.class,
        AppChain_WVM.class,
        AppChain_Perm.class,
        AppChain_Y_UpgradeTestOnly.class,
        AppChain_Z_DiffHashType.class,
        AppChain_Z_ConfigChange_ClearDB.class,
        AppChain_Z_ConfigChange02_ClearDB.class,

})

//Build Validation Test
public class TS004_Run_AppChain {
    //执行这个类将执行suiteClass中的测试项

}
