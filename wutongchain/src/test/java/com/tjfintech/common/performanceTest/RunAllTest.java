package com.tjfintech.common.performanceTest;

import com.tjfintech.common.functionTest.store.StoreTest;
import com.tjfintech.common.functionTest.store.StoreInvalidTest;
import com.tjfintech.common.functionTest.utxoMultiSign.MultiTest;
import com.tjfintech.common.functionTest.utxoMultiSign.MultiTestInvalid;
import com.tjfintech.common.functionTest.utxoSingleSign.SoloTest;
import com.tjfintech.common.functionTest.utxoSingleSign.SoloTestInvalid;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        MultiTest.class,
        MultiTestInvalid.class,
        SoloTest.class,
        SoloTestInvalid.class,
        StoreTest.class,
        StoreInvalidTest.class
})
public class RunAllTest {
    //执行这个类将执行suiteClass中的测试项
}
