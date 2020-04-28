package com.tjfintech.common.functionTest.BVT.P1_High;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.Interface.Token;
import com.tjfintech.common.functionTest.Conditions.*;
import com.tjfintech.common.functionTest.Conditions.Upgrade.SetTokenApiPerm999;
import com.tjfintech.common.functionTest.contract.DockerContractInvalidTest;
import com.tjfintech.common.functionTest.contract.DockerContractTest;
import com.tjfintech.common.functionTest.contract.WVMContractTest;
import com.tjfintech.common.functionTest.mixTest.TestTxType;
import com.tjfintech.common.functionTest.mixTest.VerifyTests;
import com.tjfintech.common.functionTest.store.PrivateStoreTest;
import com.tjfintech.common.functionTest.store.StoreInvalidTest;
import com.tjfintech.common.functionTest.store.StoreTest;
import com.tjfintech.common.functionTest.store.StoreWalletRelatedTest;
import com.tjfintech.common.functionTest.tokenModuleTest.*;
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
        SetMainLedger.class,
        SetURLToSDKAddr.class,
        SetTokenApiPerm999.class,
        SDKToTokenMultiTest.class,
        TokenAccurateTest.class,
        TokenInterfaceTest.class,
        TokenMultiInvalidTest.class,
        TokenMultiTest.class,
        TokenPrivateStoreTest.class,
        TokenSoloInvalidTest.class,
        TokenSoloTest.class,
        TokenStoreTest.class,
        TokenTxTypeTest_SDK.class,
        TokenTxTypeTest_Token.class,
        SetURLToSDKAddr.class,

})

//Build Validation Test
public class Run_Main_Finservice {
    //执行这个类将执行suiteClass中的测试项

}
