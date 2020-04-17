package com.tjfintech.common.functionTest.BVT.P1_High;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.functionTest.Conditions.SetCertSM2;
import com.tjfintech.common.functionTest.Conditions.SetDatabaseMysql;
import com.tjfintech.common.functionTest.Conditions.SetMainLedger;
import com.tjfintech.common.functionTest.Conditions.SetSDKWalletDisabled;
import com.tjfintech.common.functionTest.contract.DockerContractTest;
import com.tjfintech.common.functionTest.contract.WVMContractInvalidTest;
import com.tjfintech.common.functionTest.contract.WVMContractTest;
import com.tjfintech.common.functionTest.mixTest.TestTxType;
import com.tjfintech.common.functionTest.store.PrivateStoreTest;
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
        BeforeCondition.class,

        SetSDKWalletDisabled.class,

        StoreTest.class,
        PrivateStoreTest.class,
        StoreInvalidTest.class,

        WVMContractTest.class,
        WVMContractInvalidTest.class,

})

//Build Validation Test
public class Run_Main_WalletDisabled {
    //执行这个类将执行suiteClass中的测试项

}
