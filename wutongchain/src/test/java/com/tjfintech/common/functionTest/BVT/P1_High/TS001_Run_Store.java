package com.tjfintech.common.functionTest.BVT.P1_High;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.functionTest.Conditions.*;
import com.tjfintech.common.functionTest.mixTest.VerifyTests;
import com.tjfintech.common.functionTest.store.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;



@RunWith(Suite.class)
@Suite.SuiteClasses({

        SetCertSM2.class,
//        PeerStartNoPermTest.class,
        BeforeCondition.class,
        SetURLToSDKAddr.class,

        StoreTest.class,
        PrivateStoreTest.class,
        PrivateStoreTestFastGet.class,
        CommonInterfaceTest.class,
        NoPriPrivateStoreTest.class,
        StoreInvalidTest.class,

})

//Build Validation Test
public class TS001_Run_Store {
    //执行这个类将执行suiteClass中的测试项

}
