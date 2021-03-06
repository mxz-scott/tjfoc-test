package com.tjfintech.common.functionTest.BVT.P2_Middle;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.functionTest.Conditions.SetCertMIX2;
import com.tjfintech.common.functionTest.Conditions.SetDatabaseMysql;
import com.tjfintech.common.functionTest.mixTest.TestTxType;
import com.tjfintech.common.functionTest.store.PrivateStoreTest;
import com.tjfintech.common.functionTest.store.StoreInvalidTest;
import com.tjfintech.common.functionTest.store.StoreTest;
import com.tjfintech.common.functionTest.utxoMultiSign.MultiSignDetailTest;
import com.tjfintech.common.functionTest.utxoMultiSign.MultiTest;
import com.tjfintech.common.functionTest.utxoMultiSign.MultiTestInvalid;
import com.tjfintech.common.functionTest.utxoMultiSign.MultiTest_33_12;
import com.tjfintech.common.functionTest.utxoSingleSign.SoloTest;
import com.tjfintech.common.functionTest.utxoSingleSign.SoloTestInvalid;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
//        SetDatabaseMysql.class,
        SetCertMIX2.class,
        BeforeCondition.class,

        PrivateStoreTest.class,
        StoreInvalidTest.class,

        MultiTest.class,
        MultiTest_33_12.class,
        MultiSignDetailTest.class,
        MultiTestInvalid.class,

        SoloTest.class,
        SoloTestInvalid.class,
})

//Build Validation Test
public class RunBVTCertMIX2 {
    //执行这个类将执行suiteClass中的测试项

}
