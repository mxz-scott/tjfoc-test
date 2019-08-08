package com.tjfintech.common.functionTest.BVT;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.functionTest.contract.DockerContractTest;
import com.tjfintech.common.functionTest.mixTest.TestTxType;
import com.tjfintech.common.functionTest.mixTest.TimeofTxOnChain;
import com.tjfintech.common.functionTest.store.LocalStoreTest;
import com.tjfintech.common.functionTest.store.StoreInvalidTest;
import com.tjfintech.common.functionTest.store.StoreTest;
import com.tjfintech.common.functionTest.utxoMultiSign.*;
import com.tjfintech.common.functionTest.utxoSingleSign.*;
import com.tjfintech.common.functionTest.syncInterfaceTest.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        BeforeCondition.class,

        StoreTest.class,
        LocalStoreTest.class,
        StoreInvalidTest.class,

        LocalSingleSignInvalidTest.class,
        LocalSingleSignTest.class,
        SingleSignInvalidTest.class,
        SoloTest.class,
        SoloTestInvalid.class,

        MultiTest.class,
        MultiTestInvalid.class,
        MultiSignInvalidTest.class,
        LocalMultiSignInvalidTest.class,
        MultiTest_33_12.class,
        LocalMultiSignTest.class,
        MultiSignDetailTest.class,

        DockerContractTest.class,

        TestTxType.class,
        TimeofTxOnChain.class,

        SyncDockerContractTest.class,
        SyncMultiSignTest.class,
        SyncSingleSignTest.class,
        SyncStoreTest.class,
})

//Build Validation Test
public class RunPart1 {
    //执行这个类将执行suiteClass中的测试项
}
