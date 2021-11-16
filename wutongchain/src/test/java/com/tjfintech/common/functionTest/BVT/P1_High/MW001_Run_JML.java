package com.tjfintech.common.functionTest.BVT.P1_High;
import com.tjfintech.common.functionTest.Conditions.SetSyncFlagTrue;
import com.tjfintech.common.functionTest.JmlTest.JmlTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({

        JmlTest.class,
        SetSyncFlagTrue.class,
        JmlTest.class,


})

//Build Validation Test
public class MW001_Run_JML {
    //执行这个类将执行suiteClass中的测试项

}
