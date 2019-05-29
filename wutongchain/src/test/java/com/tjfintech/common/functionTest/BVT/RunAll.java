package com.tjfintech.common.functionTest.BVT;

import com.tjfintech.common.functionTest.*;
import com.tjfintech.common.functionTest.PermissionTest.TestPermission;
import com.tjfintech.common.functionTest.dockerContract.ContractTest;
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
        StoreTest.class,
        MultiTest.class,
        SoloTest.class,
        MultiTest_33_12.class,
        ContractTest.class,

        LocalStoreTest.class,
        LocalMultiSignTest.class,
        LocalSingleSignTest.class,

        TestTxType.class,
        TestMgTool.class,
        TestPermission.class,

        MultiSignDetailTest.class,

        StoreInvalidTest.class,
        SoloTestInvalid.class,
        SingleSignInvalidTest.class,
        MultiTestInvalid.class,
        MultiSignInvalidTest.class,
        LocalMultiSignInvalidTest.class,
        LocalSingleSignInvalidTest.class,

        SyncDockerContractTest.class,
        SyncManageTest.class,
        SyncMultiSignTest.class,
        SyncSingleSignTest.class,
        SyncStoreTest.class,

        MixTxTest.class,
        SysTest.class,
        TestTLSCert.class,
        TestZBlockHash.class,
        TimeofTxOnChain.class,
        TestSDKPeerConn.class,
})

//Build Validation Test
public class RunAll {
    //执行这个类将执行suiteClass中的测试项
}
