package com.tjfintech.common.performanceTest;

import com.tjfintech.common.functionTest.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        MultiTest.class,
        MultiTestInvalid.class,
        MultiTestInvalid2.class,
        SoloTest.class,
        SoloTestInvalid.class,
        StoreTest.class,
        StoreTestlnvalid.class
})
public class RunAllTest {
    //执行这个类将执行suiteClass中的测试项
}
