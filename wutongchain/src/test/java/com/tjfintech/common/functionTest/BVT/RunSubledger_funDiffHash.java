package com.tjfintech.common.functionTest.BVT;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.functionTest.Conditions.SetDatabaseMysql;
import com.tjfintech.common.functionTest.Conditions.SetMgToolHashTypeSM3;
import com.tjfintech.common.functionTest.mainSubChain.TestDiffHashType;
import com.tjfintech.common.functionTest.mainSubChain.TestMultiSubChain;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
        SetDatabaseMysql.class,
        SetMgToolHashTypeSM3.class,
        BeforeCondition.class,
        TestDiffHashType.class
})

//Build Validation Test
public class RunSubledger_funDiffHash {
    //执行这个类将执行suiteClass中的测试项

}
