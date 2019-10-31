package com.tjfintech.common.functionTest.BVT.P2_Middle;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.functionTest.Conditions.*;
import com.tjfintech.common.functionTest.PermissionTest.TestPermission;
import com.tjfintech.common.functionTest.mixTestWithConfigChange.MixTxTest;
import com.tjfintech.common.functionTest.mixTestWithConfigChange.TestMgTool;
import com.tjfintech.common.functionTest.store.LocalStoreTest;
import com.tjfintech.common.functionTest.utxoMultiSign.LocalMultiSignInvalidTest;
import com.tjfintech.common.functionTest.utxoMultiSign.LocalMultiSignTest;
import com.tjfintech.common.functionTest.utxoSingleSign.LocalSingleSignInvalidTest;
import com.tjfintech.common.functionTest.utxoSingleSign.LocalSingleSignTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@Slf4j
@RunWith(Suite.class)
@Suite.SuiteClasses({
//        SetDatabaseMysql.class,
        SetCertSM2.class,
        SetSubLedger.class,
        SetSubLedgerSleepTime.class,
        BeforeCondition.class,

//        LocalStoreTest.class,
        LocalMultiSignTest.class,
        LocalMultiSignInvalidTest.class,

        LocalSingleSignTest.class,
        LocalSingleSignInvalidTest.class,

})

//Build Validation Test
public class Run_Sub_LocalSign {
    //执行这个类将执行suiteClass中的测试项
}
