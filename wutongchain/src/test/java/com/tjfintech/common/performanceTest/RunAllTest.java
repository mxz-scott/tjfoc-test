package com.tjfintech.common.performanceTest;

import com.tjfintech.common.functionTest.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        MultiSignTest.class,
        MultiSignInvalidTest1.class,
        MultiSignInvalidTest2.class,
        SingleSignTest.class,
        SingleSignInvalidTest.class,
        StoreTest.class,
        StorelnvalidTest.class
})
public class RunAllTest {
    //执行这个类将执行suiteClass中的测试项
}
