package com.tjfintech.common.functionTest.BVT.P1_High;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.functionTest.Conditions.SetCertSM2;
import com.tjfintech.common.functionTest.Conditions.SetURLToSDKAddr;
import com.tjfintech.common.functionTest.contract.WVMContractTest;
import com.tjfintech.common.functionTest.smartTokenTest.smtInterfaceTest;
import com.tjfintech.common.functionTest.smartTokenTest.smtMultiInvalidTest;
import com.tjfintech.common.functionTest.smartTokenTest.smtMultiTest;
import com.tjfintech.common.functionTest.store.PrivateStoreTest;
import com.tjfintech.common.functionTest.store.StoreTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({

        SetCertSM2.class,
        BeforeCondition.class,
        SetURLToSDKAddr.class,

        StoreTest.class,
        PrivateStoreTest.class,
        WVMContractTest.class,
        smtMultiTest.class,

})

//Build Validation Test
public class TS000_Run_SmokingTest {
    //执行这个类将执行suiteClass中的测试项

}
