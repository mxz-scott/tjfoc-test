package com.tjfintech.common.functionTest.BVT.P1_High;

import com.tjfintech.common.functionTest.guDengTest.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
        GDAllFlowTest.class,

        GuDengInterfaceTest.class,

        GDSceneTest01.class,

        GDSceneTest_Issue.class,
        GDSceneTest_ChangeProperty.class,
        GDSceneTest_ChangeBoard.class,
        GDSceneTest_Increase.class,
        GDSceneTest_Transfer.class,
        GDSceneTest_LockUnLock.class,
        GDSceneTest_Recycle.class,
        GDSceneTest_DestroyAccount.class


})

//Build Validation Test
public class Run_Main_GuDeng {
    //执行这个类将执行suiteClass中的测试项

}
