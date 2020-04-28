package com.tjfintech.common.functionTest.BVT.P1_High;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.functionTest.Conditions.*;
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
        TokenTestMultiSubChain_Store.class, SetURLToSDKAddr.class,

})

//Build Validation Test
public class Run_MainSub_Isolation_TokenApi {
    //执行这个类将执行suiteClass中的测试项

}
