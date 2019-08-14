package com.tjfintech.common.functionTest.BVT;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.functionTest.Conditions.SetDatabaseMongo;
import com.tjfintech.common.functionTest.Conditions.SetMainLedger;
import com.tjfintech.common.functionTest.PermissionTest.TestPermission;
import com.tjfintech.common.functionTest.mixTest.TestTxType;
import com.tjfintech.common.functionTest.contract.DockerContractTest;
import com.tjfintech.common.functionTest.store.StoreTest;
import com.tjfintech.common.functionTest.utxoMultiSign.MultiTest;
import com.tjfintech.common.functionTest.utxoMultiSign.MultiTest_33_12;
import com.tjfintech.common.functionTest.utxoSingleSign.SoloTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
        SetMainLedger.class,
//        SetDatabaseMongo.class,
//        BeforeCondition.class,
//        StoreTest.class,
//        MultiTest.class,
//        MultiTest_33_12.class,
//        SoloTest.class,
        DockerContractTest.class,
//        TestTxType.class,
//        TestPermission.class
})

//Build Validation Test
public class RunBVT {
    //执行这个类将执行suiteClass中的测试项
    //默认主链测试（SubLedger为空）mongodb sm2（旧版本 certPath为空） sha-sm3

}
