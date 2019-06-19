package com.tjfintech.common.functionTest.BVT;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.functionTest.PermissionTest.TestPermission;
import com.tjfintech.common.functionTest.TestMgTool;
import com.tjfintech.common.functionTest.TestTxType;
import com.tjfintech.common.functionTest.dockerContract.ContractTest;
import com.tjfintech.common.functionTest.store.StoreTest;
import com.tjfintech.common.functionTest.utxoMultiSign.MultiTest;
import com.tjfintech.common.functionTest.utxoMultiSign.MultiTest_33_12;
import com.tjfintech.common.functionTest.utxoSingleSign.SoloTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import java.security.acl.Permission;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        BeforeCondition.class,
        TestMgTool.class,
        TestPermission.class
})

//Build Validation Test
public class RunMgTool {
    //执行这个类将执行suiteClass中的测试项
}
