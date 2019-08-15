package com.tjfintech.common.functionTest.BVT.P1_High;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.functionTest.Conditions.SetDatabaseMysql;
import com.tjfintech.common.functionTest.Conditions.SetMainLedger;
import com.tjfintech.common.functionTest.Conditions.SetSubLedger;
import com.tjfintech.common.functionTest.contract.DockerContractTest;
import com.tjfintech.common.functionTest.contract.WVMContractTest;
import com.tjfintech.common.functionTest.mixTest.TestTxType;
import com.tjfintech.common.functionTest.store.StoreInvalidTest;
import com.tjfintech.common.functionTest.store.StoreTest;
import com.tjfintech.common.functionTest.utxoMultiSign.*;
import com.tjfintech.common.functionTest.utxoSingleSign.SingleSignInvalidTest;
import com.tjfintech.common.functionTest.utxoSingleSign.SoloTest;
import com.tjfintech.common.functionTest.utxoSingleSign.SoloTestInvalid;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses(value = {
        SetSubLedger.class,
        SetDatabaseMysql.class,
        BeforeCondition.class,

        StoreTest.class,
        StoreInvalidTest.class,

        MultiTest.class,
        MultiTest_33_12.class,
        MultiSignDetailTest.class,
        MultiSignInvalidTest.class,
        MultiTestInvalid.class,

        SoloTest.class,
        SingleSignInvalidTest.class,
        SoloTestInvalid.class,

        DockerContractTest.class,
        WVMContractTest.class,

        TestTxType.class,
})

//Build Validation Test
public class Run_Sub_Mysql_SM2 {
    //执行这个类将执行suiteClass中的测试项
    //子链测试 + mysql + sm2（旧版本 certPath为空） + sm3

}
