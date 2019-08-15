package com.tjfintech.common.functionTest.BVT.P1_High;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.functionTest.mainSubChain.TestMainSubChain;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
        BeforeCondition.class,
        //SetDatabaseMongo.class,
        TestMainSubChain.class
})

//Build Validation Test
public class RunSubledger_funMainSub {
    //执行这个类将执行suiteClass中的测试项

}
