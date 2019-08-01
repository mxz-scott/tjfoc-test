package com.tjfintech.common.functionTest.BVT;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.functionTest.Conditions.SetDatabaseMongo;
import com.tjfintech.common.functionTest.Conditions.SetDatabaseMysql;
import com.tjfintech.common.functionTest.mainSubChain.TestMainSubChain;
import com.tjfintech.common.functionTest.mainSubChain.TestMultiSubChain;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
        BeforeCondition.class,
        SetDatabaseMysql.class,
        TestMultiSubChain.class
})

//Build Validation Test
public class RunSubledger_funMultiSub {
    //执行这个类将执行suiteClass中的测试项

}
