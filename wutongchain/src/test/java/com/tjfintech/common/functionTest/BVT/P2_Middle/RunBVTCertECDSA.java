package com.tjfintech.common.functionTest.BVT.P2_Middle;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.functionTest.Conditions.SetCertECDSA;
import com.tjfintech.common.functionTest.Conditions.SetDatabaseMysql;
import com.tjfintech.common.functionTest.mixTest.TestTxType;
import com.tjfintech.common.functionTest.utxoMultiSign.MultiSignDetailTest;
import com.tjfintech.common.functionTest.utxoMultiSign.MultiTest;
import com.tjfintech.common.functionTest.utxoMultiSign.MultiTestInvalid;
import com.tjfintech.common.functionTest.utxoMultiSign.MultiTest_33_12;
import com.tjfintech.common.functionTest.utxoSingleSign.SoloTest;
import com.tjfintech.common.functionTest.utxoSingleSign.SoloTestInvalid;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
        SetDatabaseMysql.class,
        SetCertECDSA.class,
        BeforeCondition.class,
        SoloTest.class,
        MultiTest.class,
        MultiTest_33_12.class,
        MultiSignDetailTest.class,
        SoloTestInvalid.class,
        MultiTestInvalid.class,
        TestTxType.class
})

//Build Validation Test
public class RunBVTCertECDSA {
    //执行这个类将执行suiteClass中的测试项

}
