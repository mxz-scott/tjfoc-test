package com.tjfintech.common.functionTest.BVT.P3_Low;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.functionTest.Conditions.SetMainLedger;
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
        SetMainLedger.class,
        BeforeCondition.class,

        SyncDockerContractTest.class,
        SyncMultiSignTest.class,
        SyncSingleSignTest.class,
        SyncStoreTest.class,
        SyncManageTest.class,
})

//Build Validation Test
public class Run_Main_SyncTest {
    //执行这个类将执行suiteClass中的测试项
}
