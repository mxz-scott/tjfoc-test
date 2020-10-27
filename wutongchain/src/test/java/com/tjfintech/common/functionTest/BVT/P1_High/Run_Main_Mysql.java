package com.tjfintech.common.functionTest.BVT.P1_High;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.functionTest.Conditions.*;
import com.tjfintech.common.functionTest.PermissionTest.PeerStartNoPermTest;
import com.tjfintech.common.functionTest.PermissionTest.TestPermission;
import com.tjfintech.common.functionTest.contract.*;
import com.tjfintech.common.functionTest.mixTest.TestTxType;
import com.tjfintech.common.functionTest.mixTest.VerifyTests;
import com.tjfintech.common.functionTest.mixTestWithConfigChange.AddPeerAndSyncData;
import com.tjfintech.common.functionTest.store.*;
import com.tjfintech.common.functionTest.utxoMultiSign.*;
import com.tjfintech.common.functionTest.utxoSingleSign.SingleSignInvalidTest;
import com.tjfintech.common.functionTest.utxoSingleSign.SoloTest;
import com.tjfintech.common.functionTest.utxoSingleSign.SoloTestInvalid;
import com.tjfintech.common.functionTest.utxoSingleSign.SoloTest_UnspentTxOutput_test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
//        SetDatabaseMysql.class,
//        SetAccountEmpty.class,
//        SetCertSM2.class,
//        SetMainLedger.class,
//        PeerStartNoPermTest.class,
//
//        TestTxType.class,

        BeforeCondition.class,

        SetURLToSDKAddr.class,
        StoreTest.class,
        PrivateStoreTest.class,
//        PrivateStoreTestFastGet.class,
//        SetWVMversion1.class,
//        WVMContractTest.class,

//        WVMContractInvalidTest.class,

        SetWVMversionEmpty.class,
        WVMContractTest.class,
        WVMContractInvalidTest.class,
        WVMContractTest_withVersionUpgradeTest.class,

        StoreInvalidTest.class,

//        AddPeerAndSyncData.class,

        VerifyTests.class,
//        TestPermission.class,

})

//Build Validation Test
public class Run_Main_Mysql {
    //执行这个类将执行suiteClass中的测试项

}
