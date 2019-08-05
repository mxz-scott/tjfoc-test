package com.tjfintech.common.functionTest.BVT;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.functionTest.Conditions.SetDatabaseMysql;
import com.tjfintech.common.functionTest.Conditions.SetSubLedger;
import com.tjfintech.common.functionTest.dockerContract.ContractTest;
import com.tjfintech.common.functionTest.mixTest.TestTxType;
import com.tjfintech.common.functionTest.store.StoreTest;
import com.tjfintech.common.functionTest.utxoMultiSign.MultiTest;
import com.tjfintech.common.functionTest.utxoMultiSign.MultiTest_33_12;
import com.tjfintech.common.functionTest.utxoSingleSign.SoloTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
        SetSubLedger.class,
        SetDatabaseMysql.class,
        BeforeCondition.class,
        StoreTest.class,
        MultiTest.class,
        MultiTest_33_12.class,
        SoloTest.class,
        ContractTest.class,
        TestTxType.class
})

//Build Validation Test
public class RunBVTSubledger_TX_Mysql {
    //执行这个类将执行suiteClass中的测试项

}
