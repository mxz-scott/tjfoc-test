package com.tjfintech.common.functionTest.BVT;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.functionTest.PermissionTest.TestPermission;
import com.tjfintech.common.functionTest.mixTest.TimeofTxOnChain;
import com.tjfintech.common.functionTest.mixTestWithConfigChange.*;
import com.tjfintech.common.functionTest.syncInterfaceTest.SyncManageTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import lombok.extern.slf4j.Slf4j;
import static com.tjfoc.utils.ReadFiletoByte.log;

@Slf4j
@RunWith(Suite.class)
@Suite.SuiteClasses({

        BeforeCondition.class,
        SyncManageTest.class,
        TestSDKPeerConn.class,
        MixTxTest.class,
        TestTLSCert.class,
        BlockSyncTest.class, //failed

})

//Build Validation Test
public class RunPart2 {
    //执行这个类将执行suiteClass中的测试项
}
