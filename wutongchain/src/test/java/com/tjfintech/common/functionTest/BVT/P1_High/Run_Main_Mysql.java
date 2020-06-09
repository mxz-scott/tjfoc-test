package com.tjfintech.common.functionTest.BVT.P1_High;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.functionTest.Conditions.*;
import com.tjfintech.common.functionTest.PermissionTest.PeerStartNoPermTest;
import com.tjfintech.common.functionTest.contract.DockerContractInvalidTest;
import com.tjfintech.common.functionTest.contract.DockerContractTest;
import com.tjfintech.common.functionTest.contract.WVMContractInvalidTest;
import com.tjfintech.common.functionTest.contract.WVMContractTest;
import com.tjfintech.common.functionTest.mixTest.TestTxType;
import com.tjfintech.common.functionTest.mixTest.VerifyTests;
import com.tjfintech.common.functionTest.mixTestWithConfigChange.AddPeerAndSyncData;
import com.tjfintech.common.functionTest.store.*;
import com.tjfintech.common.functionTest.utxoMultiSign.*;
import com.tjfintech.common.functionTest.utxoSingleSign.SingleSignInvalidTest;
import com.tjfintech.common.functionTest.utxoSingleSign.SoloTest;
import com.tjfintech.common.functionTest.utxoSingleSign.SoloTestInvalid;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
        SetDatabaseMysql.class,
        SetTokenApiDatabaseMysql.class,
        SetAccountEmpty.class,
        SetCertSM2.class,
        SetMainLedger.class,
        PeerStartNoPermTest.class,

        TestTxType.class,

        BeforeCondition.class,

        SetURLToSDKAddr.class,
        StoreTest.class,
        PrivateStoreTest.class,
        PrivateStoreTestFastGet.class,
        StoreInvalidTest.class,
        StoreWalletRelatedTest.class,

        MultiTest.class,
        MultiTest_33_12.class,
        MultiSignDetailTest.class,
        MultiSignInvalidTest.class,
        MultiTestInvalid.class,

        SoloTest.class,
        SingleSignInvalidTest.class,
        SoloTestInvalid.class,

        WVMContractTest.class,
        WVMContractInvalidTest.class,

        VerifyTests.class,

        AddPeerAndSyncData.class

})

//Build Validation Test
public class Run_Main_Mysql {
    //执行这个类将执行suiteClass中的测试项

}
