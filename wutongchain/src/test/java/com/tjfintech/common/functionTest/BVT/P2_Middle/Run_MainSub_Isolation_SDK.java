package com.tjfintech.common.functionTest.BVT.P2_Middle;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.functionTest.Conditions.*;
import com.tjfintech.common.functionTest.mainAppChain.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
        SetDatabaseMysql.class,
        SetAccountEmpty.class,
        SetMainLedger.class,
        SetSubLedgerSleepTime.class,
        BeforeCondition.class,
//        TestMainAppChain_UTXO.class,
        TestMainAppChain_WVM.class,
        TestMainAppChain_Perm.class,
        SetURLToSDKAddr.class,

})

//Build Validation Test
public class Run_MainSub_Isolation_SDK {
    //执行这个类将执行suiteClass中的测试项

}
