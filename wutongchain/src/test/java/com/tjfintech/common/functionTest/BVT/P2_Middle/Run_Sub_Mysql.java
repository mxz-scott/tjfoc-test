package com.tjfintech.common.functionTest.BVT.P2_Middle;

import com.tjfintech.common.functionTest.Conditions.*;
import com.tjfintech.common.functionTest.contract.WVMContractInvalidTest;
import com.tjfintech.common.functionTest.contract.WVMContractTest;
import com.tjfintech.common.functionTest.mixTest.VerifyTests;
import com.tjfintech.common.functionTest.store.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses(value = {
//        SetDatabaseMysql.class,
        SetAccountEmpty.class,
        SetCertSM2.class,
        SetAppChain.class,
        SetSubLedgerSleepTime.class,
//        BeforeCondition.class,

//        TestTxType.class,

        StoreTest.class,
        PrivateStoreTest.class,
        PrivateStoreTestFastGet.class,
        CommonInterfaceTest.class,
        NoPriPrivateStoreTest.class,
        StoreInvalidTest.class,

        WVMContractTest.class,
        WVMContractInvalidTest.class,

        VerifyTests.class,

})

//Build Validation Test
public class Run_Sub_Mysql {
    //执行这个类将执行suiteClass中的测试项
    //子链测试 + mysql + sm2（旧版本 certPath为空） + sm3

}
