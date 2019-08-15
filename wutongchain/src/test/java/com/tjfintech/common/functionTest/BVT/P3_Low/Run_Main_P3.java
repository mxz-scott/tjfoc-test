package com.tjfintech.common.functionTest.BVT.P3_Low;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.functionTest.Conditions.SetMainLedger;
import com.tjfintech.common.functionTest.PermissionTest.TestPermission;
import com.tjfintech.common.functionTest.mixTest.TimeofTxOnChain;
import com.tjfintech.common.functionTest.mixTestWithConfigChange.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        SetMainLedger.class,
        BeforeCondition.class,

        TestSDKPeerConn.class, // failed
        TimeofTxOnChain.class,        // failed
        TestTLSCert.class,     // failed
        SysTest.class,     // failed
})

//Build Validation Test
public class Run_Main_P3 {
    //执行这个类将执行suiteClass中的测试项
}
