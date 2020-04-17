package com.tjfintech.common.functionTest.BVT.P1_High;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.functionTest.Conditions.SetDatabaseMysql;
import com.tjfintech.common.functionTest.Conditions.SetMainLedger;
import com.tjfintech.common.functionTest.Conditions.SetSubLedgerSleepTime;
import com.tjfintech.common.functionTest.Conditions.SetTokenApiDatabaseMysql;
import com.tjfintech.common.functionTest.mainSubChain.*;
import com.tjfintech.common.functionTest.tokenModuleTest.TokenTestMainSubChain_UTXO;
import com.tjfintech.common.functionTest.tokenModuleTest.TokenTestMultiSubChain_Store;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
        SetDatabaseMysql.class,
        SetTokenApiDatabaseMysql.class,
        SetMainLedger.class,
        SetSubLedgerSleepTime.class,
        BeforeCondition.class,
        TokenTestMainSubChain_UTXO.class,
        TokenTestMultiSubChain_Store.class,

})

//Build Validation Test
public class Run_MainSub_Isolation_TokenApi {
    //执行这个类将执行suiteClass中的测试项

}