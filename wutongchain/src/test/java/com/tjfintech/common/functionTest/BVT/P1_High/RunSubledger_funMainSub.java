package com.tjfintech.common.functionTest.BVT.P1_High;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.functionTest.Conditions.SetDatabaseMysql;
import com.tjfintech.common.functionTest.mainSubChain.TestMainSubChain_Create_01;
import com.tjfintech.common.functionTest.mainSubChain.TestMainSubChain_FRDG;
import com.tjfintech.common.functionTest.mainSubChain.TestMainSubChain_Perm;
import com.tjfintech.common.functionTest.mainSubChain.TestMainSubChain_UTXO;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
        SetDatabaseMysql.class,
        BeforeCondition.class,
        TestMainSubChain_Create_01.class,
        TestMainSubChain_FRDG.class,
        TestMainSubChain_Perm.class
})

//Build Validation Test
public class RunSubledger_funMainSub {
    //执行这个类将执行suiteClass中的测试项

}
