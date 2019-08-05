package com.tjfintech.common.functionTest.BVT;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.functionTest.PermissionTest.TestPermission;
import com.tjfintech.common.functionTest.mixTestWithConfigChange.*;
import com.tjfintech.common.functionTest.syncInterfaceTest.SyncManageTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        BeforeCondition.class,

        TestMgTool.class, // failed
        SysTest.class,        // failed
        TestPermission.class,     // failed

})

//Build Validation Test
public class RunPart3 {
    //执行这个类将执行suiteClass中的测试项
}
