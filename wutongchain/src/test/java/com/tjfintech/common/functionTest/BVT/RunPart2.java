package com.tjfintech.common.functionTest.BVT;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.functionTest.PermissionTest.TestPermission;
import com.tjfintech.common.functionTest.mixTest.TimeofTxOnChain;
import com.tjfintech.common.functionTest.mixTestWithConfigChange.*;
import com.tjfintech.common.functionTest.syncInterfaceTest.SyncManageTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        BeforeCondition.class,
        BlockSyncTest.class,
        SyncManageTest.class,
        TestSDKPeerConn.class,
        MixTxTest.class,
        TestTLSCert.class,

})

//Build Validation Test
public class RunPart2 {
    //执行这个类将执行suiteClass中的测试项
}