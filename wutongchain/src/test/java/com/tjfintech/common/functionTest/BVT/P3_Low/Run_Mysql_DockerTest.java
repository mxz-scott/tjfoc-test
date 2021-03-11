package com.tjfintech.common.functionTest.BVT.P3_Low;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.functionTest.Conditions.*;
import com.tjfintech.common.functionTest.PermissionTest.TestPermission_Docker;
import com.tjfintech.common.functionTest.contract.DockerContractInvalidTest;
import com.tjfintech.common.functionTest.contract.DockerContractTest;

import com.tjfintech.common.functionTest.mixTest.TestTxType_Docker;
import com.tjfintech.common.functionTest.mixTestWithConfigChange.BlockSyncTest_DockerImageFlag_WithContract;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses(value = {
        SetDatabaseMysql.class,
        SetCertSM2.class,
        SetMainLedger.class,
        BeforeCondition.class,

        DockerContractTest.class,
        DockerContractInvalidTest.class,

        TestTxType_Docker.class,
        TestPermission_Docker.class,

        SetAppChain.class,
        DockerContractTest.class,
        DockerContractInvalidTest.class,

        BlockSyncTest_DockerImageFlag_WithContract.class,
})

//Build Validation Test
public class Run_Mysql_DockerTest {
    //执行这个类将执行suiteClass中的测试项
    //子链测试 + Mongo + sm2（旧版本 certPath为空） + sm3

}
