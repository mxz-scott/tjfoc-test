package com.tjfintech.common.functionTest.BVT.P1_High;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.functionTest.Conditions.SetCertSM2;
import com.tjfintech.common.functionTest.Conditions.SetDatabaseMysql;
import com.tjfintech.common.functionTest.Conditions.SetMainLedger;
import com.tjfintech.common.functionTest.contract.DockerContractInvalidTest;
import com.tjfintech.common.functionTest.contract.DockerContractTest;
import com.tjfintech.common.functionTest.contract.WVMContractTest;
import com.tjfintech.common.functionTest.mixTest.TestTxType;
import com.tjfintech.common.functionTest.mixTest.VerifyTests;
import com.tjfintech.common.functionTest.store.PrivateStoreTest;
import com.tjfintech.common.functionTest.store.StoreInvalidTest;
import com.tjfintech.common.functionTest.store.StoreTest;
import com.tjfintech.common.functionTest.store.StoreWalletRelatedTest;
import com.tjfintech.common.functionTest.utxoMultiSign.*;
import com.tjfintech.common.functionTest.utxoSingleSign.SingleSignInvalidTest;
import com.tjfintech.common.functionTest.utxoSingleSign.SoloTest;
import com.tjfintech.common.functionTest.utxoSingleSign.SoloTestInvalid;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
        SetDatabaseMysql.class,
        SetCertSM2.class,
        SetMainLedger.class,
        BeforeCondition.class,

        StoreTest.class,
        PrivateStoreTest.class,
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

        DockerContractTest.class,
        DockerContractInvalidTest.class,

        WVMContractTest.class,

        TestTxType.class,

        VerifyTests.class,

})

//Build Validation Test
public class Run_Main_Mysql {
    //执行这个类将执行suiteClass中的测试项

}
