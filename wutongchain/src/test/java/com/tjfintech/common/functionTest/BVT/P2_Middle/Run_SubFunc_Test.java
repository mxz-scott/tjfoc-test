package com.tjfintech.common.functionTest.BVT.P2_Middle;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.functionTest.Conditions.*;
import com.tjfintech.common.functionTest.mainSubChain.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
        SetDatabaseMysql.class,
        SetAccountEmpty.class,
        SetMainLedger.class,
        SetSubLedgerSleepTime.class,
        BeforeCondition.class,
        TestMainSubChain_Create_01.class,
        TestMainSubChain_Create_02.class,
        TestMainSubChain_FRDG.class,

})

//Build Validation Test
public class Run_SubFunc_Test {
    //执行这个类将执行suiteClass中的测试项

}
