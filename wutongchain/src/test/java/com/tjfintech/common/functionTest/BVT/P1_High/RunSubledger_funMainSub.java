package com.tjfintech.common.functionTest.BVT.P1_High;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.functionTest.Conditions.SetDatabaseMysql;
import com.tjfintech.common.functionTest.Conditions.SetMainLedger;
import com.tjfintech.common.functionTest.mainSubChain.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
        SetDatabaseMysql.class,
        SetMainLedger.class,
        BeforeCondition.class,
        TestMainSubChain_Create_01.class,
        TestMainSubChain_Create_02.class,
        TestMainSubChain_FRDG.class,
        TestMainSubChain_Perm.class,
        TestMainSubChain_UTXO.class,
        TestMainSubChain_DockerContract.class //当前存在bug 20191031
})

//Build Validation Test
public class RunSubledger_funMainSub {
    //执行这个类将执行suiteClass中的测试项

}
