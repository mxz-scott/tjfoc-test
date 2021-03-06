package com.tjfintech.common.functionTest.BVT.P2_Middle;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.functionTest.Conditions.SetDatabaseMysql;
import com.tjfintech.common.functionTest.Conditions.SetMgToolHashTypeSM3;
import com.tjfintech.common.functionTest.Conditions.SetSubLedgerSleepTime;
import com.tjfintech.common.functionTest.appChainTest.AppChain_Z_DiffHashType;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
        SetDatabaseMysql.class,
        SetSubLedgerSleepTime.class,
        SetMgToolHashTypeSM3.class,
        BeforeCondition.class,
        AppChain_Z_DiffHashType.class
})

//Build Validation Test
public class RunSubledger_funDiffHash {
    //执行这个类将执行suiteClass中的测试项

}
