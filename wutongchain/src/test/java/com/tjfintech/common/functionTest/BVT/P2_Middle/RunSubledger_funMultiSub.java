package com.tjfintech.common.functionTest.BVT.P2_Middle;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.functionTest.Conditions.SetDatabaseMysql;
import com.tjfintech.common.functionTest.mainSubChain.TestMultiSubChain;
import com.tjfintech.common.functionTest.mainSubChain.TestMultiSubChain_02;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
        SetDatabaseMysql.class,
        BeforeCondition.class,
        TestMultiSubChain.class,
        TestMultiSubChain_02.class
})

//Build Validation Test
public class RunSubledger_funMultiSub {
    //执行这个类将执行suiteClass中的测试项

}
