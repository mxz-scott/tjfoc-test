package com.tjfintech.common.functionTest.BVT.P2_Middle;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.functionTest.Conditions.SetDatabaseMysql;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
        SetDatabaseMysql.class,
        BeforeCondition.class,
//        TestWithConfigChange_ClearDB.class
})

//Build Validation Test
public class RunSubledger_funConfigChange {
    //执行这个类将执行suiteClass中的测试项

}
