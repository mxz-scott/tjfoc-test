package com.tjfintech.common.functionTest.BVT.P3_Low;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.functionTest.Conditions.*;
import com.tjfintech.common.functionTest.PermissionTest.TestPermission_Docker;
import com.tjfintech.common.functionTest.contract.DockerContractInvalidTest;
import com.tjfintech.common.functionTest.contract.DockerContractTest;
import com.tjfintech.common.functionTest.contract.WVMContractTest;
import com.tjfintech.common.functionTest.mainSubChain.TestMainSubChain_DockerContract;
import com.tjfintech.common.functionTest.mixTest.TestTxType;
import com.tjfintech.common.functionTest.mixTest.TestTxType_Docker;
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
@Suite.SuiteClasses(value = {
        SetDatabaseMysql.class,
        SetCertSM2.class,
        SetMainLedger.class,
        BeforeCondition.class,

        DockerContractTest.class,
        DockerContractInvalidTest.class,

        TestTxType_Docker.class,
        TestPermission_Docker.class,

        TestMainSubChain_DockerContract.class,

        SetSubLedger.class,
        DockerContractTest.class,
        DockerContractInvalidTest.class,

})

//Build Validation Test
public class Run_Mysql_DockerTest {
    //执行这个类将执行suiteClass中的测试项
    //子链测试 + Mongo + sm2（旧版本 certPath为空） + sm3

}
