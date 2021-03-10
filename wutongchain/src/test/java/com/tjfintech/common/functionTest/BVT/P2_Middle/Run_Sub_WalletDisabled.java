package com.tjfintech.common.functionTest.BVT.P2_Middle;

import com.tjfintech.common.functionTest.Conditions.*;
import com.tjfintech.common.functionTest.contract.WVMContractInvalidTest;
import com.tjfintech.common.functionTest.contract.WVMContractTest;
import com.tjfintech.common.functionTest.store.StoreInvalidTest;
import com.tjfintech.common.functionTest.store.StoreTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses(value = {
        SetCertSM2.class,
        SetAppChain.class,
        SetSubLedgerSleepTime.class,

        SetSDKWalletDisabled.class,

        StoreTest.class,
        StoreInvalidTest.class,

        WVMContractTest.class,
        WVMContractInvalidTest.class
})

//Build Validation Test
public class Run_Sub_WalletDisabled {
    //执行这个类将执行suiteClass中的测试项
    //子链测试 + mysql + sm2（旧版本 certPath为空） + sm3

}
