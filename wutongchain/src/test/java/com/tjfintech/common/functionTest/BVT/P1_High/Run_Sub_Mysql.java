package com.tjfintech.common.functionTest.BVT.P1_High;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.functionTest.Conditions.*;
import com.tjfintech.common.functionTest.contract.WVMContractInvalidTest;
import com.tjfintech.common.functionTest.contract.WVMContractTest;
import com.tjfintech.common.functionTest.mixTest.TestTxType;
import com.tjfintech.common.functionTest.mixTest.VerifyTests;
import com.tjfintech.common.functionTest.store.*;
import com.tjfintech.common.functionTest.utxoMultiSign.*;
import com.tjfintech.common.functionTest.utxoSingleSign.SingleSignInvalidTest;
import com.tjfintech.common.functionTest.utxoSingleSign.SoloTest;
import com.tjfintech.common.functionTest.utxoSingleSign.SoloTestInvalid;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses(value = {
        SetDatabaseMysql.class,
        SetTokenApiDatabaseMysql.class,
        SetAccountEmpty.class,
        SetCertSM2.class,
        SetSubLedger.class,
        SetSubLedgerSleepTime.class,
        BeforeCondition.class,

        TestTxType.class,

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

})

//Build Validation Test
public class Run_Sub_Mysql {
    //执行这个类将执行suiteClass中的测试项
    //子链测试 + mysql + sm2（旧版本 certPath为空） + sm3

}
