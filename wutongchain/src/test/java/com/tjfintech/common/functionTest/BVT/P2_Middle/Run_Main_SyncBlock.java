package com.tjfintech.common.functionTest.BVT.P2_Middle;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.functionTest.Conditions.SetDatabaseMysql;
import com.tjfintech.common.functionTest.Conditions.SetMainLedger;
import com.tjfintech.common.functionTest.Conditions.SetURLToSDKAddr;
import com.tjfintech.common.functionTest.PermissionTest.TestPermission;
import com.tjfintech.common.functionTest.mixTestWithConfigChange.BlockSyncTest_DockerImageFlag;
import com.tjfintech.common.functionTest.mixTestWithConfigChange.MixTxTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@Slf4j
@RunWith(Suite.class)
@Suite.SuiteClasses({
        SetMainLedger.class,
        SetURLToSDKAddr.class,
        BlockSyncTest_DockerImageFlag.class,
})

//Build Validation Test
public class Run_Main_SyncBlock {
    //执行这个类将执行suiteClass中的测试项
}
