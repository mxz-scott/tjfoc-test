package com.tjfintech.common.functionTest.BVT.P2_Middle;

import com.tjfintech.common.functionTest.Conditions.*;
import com.tjfintech.common.functionTest.Conditions.SetSDKPerm999;
import com.tjfintech.common.functionTest.contract.WVMContractInvalidTest;
import com.tjfintech.common.functionTest.contract.WVMContractTest;
import com.tjfintech.common.functionTest.store.PrivateStoreTest;
import com.tjfintech.common.functionTest.store.PrivateStoreTestFastGet;
import com.tjfintech.common.functionTest.store.StoreInvalidTest;
import com.tjfintech.common.functionTest.store.StoreTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
        SetCertSM2.class,
        SetMainLedger.class,
        SetURLToSDKAddr.class,
        SetSDKPerm999.class,
        SetSDKWalletDisabled.class,

        StoreTest.class,
        PrivateStoreTest.class,
        PrivateStoreTestFastGet.class,
        StoreInvalidTest.class,

        WVMContractTest.class,
        WVMContractInvalidTest.class,

})

//Build Validation Test
public class Run_Main_WalletDisabled {
    //执行这个类将执行suiteClass中的测试项

}
