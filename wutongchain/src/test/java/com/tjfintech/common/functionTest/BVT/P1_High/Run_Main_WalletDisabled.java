package com.tjfintech.common.functionTest.BVT.P1_High;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.functionTest.Conditions.*;
import com.tjfintech.common.functionTest.Conditions.Upgrade.SetSDKPerm999;
import com.tjfintech.common.functionTest.contract.DockerContractTest;
import com.tjfintech.common.functionTest.Conditions.SetSDKPerm999;
import com.tjfintech.common.functionTest.contract.WVMContractInvalidTest;
import com.tjfintech.common.functionTest.contract.WVMContractTest;
import com.tjfintech.common.functionTest.mixTest.TestTxType;
import com.tjfintech.common.functionTest.store.PrivateStoreTest;
import com.tjfintech.common.functionTest.store.PrivateStoreTestFastGet;
import com.tjfintech.common.functionTest.store.StoreInvalidTest;
import com.tjfintech.common.functionTest.store.StoreTest;
import com.tjfintech.common.functionTest.utxoMultiSign.*;
import com.tjfintech.common.functionTest.utxoSingleSign.SingleSignInvalidTest;
import com.tjfintech.common.functionTest.utxoSingleSign.SoloTest;
import com.tjfintech.common.functionTest.utxoSingleSign.SoloTestInvalid;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import sun.reflect.generics.tree.Wildcard;


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
