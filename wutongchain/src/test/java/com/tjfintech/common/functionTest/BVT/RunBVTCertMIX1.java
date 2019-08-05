package com.tjfintech.common.functionTest.BVT;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.functionTest.Conditions.SetCertMIX1;
import com.tjfintech.common.functionTest.Conditions.SetSubLedger;
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
        SetCertMIX1.class,
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
public class RunBVTCertMIX1 {
    //执行这个类将执行suiteClass中的测试项

}
