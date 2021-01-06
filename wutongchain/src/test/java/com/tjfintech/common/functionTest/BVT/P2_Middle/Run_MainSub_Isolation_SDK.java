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
//        TestMainSubChain_UTXO.class,
        TestMainSubChain_WVM.class,
        TestMainSubChain_Perm.class,
        SetURLToSDKAddr.class,

})

//Build Validation Test
public class Run_MainSub_Isolation_SDK {
    //执行这个类将执行suiteClass中的测试项

}
