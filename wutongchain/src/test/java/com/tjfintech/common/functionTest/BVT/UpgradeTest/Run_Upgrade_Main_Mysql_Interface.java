package com.tjfintech.common.functionTest.BVT.UpgradeTest;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.functionTest.Conditions.*;
import com.tjfintech.common.functionTest.contract.DockerContractTest;
import com.tjfintech.common.functionTest.contract.WVMContractTest;
import com.tjfintech.common.functionTest.contract.WVMContractTest_UpgradeTestOnly;
import com.tjfintech.common.functionTest.mainSubChain.*;
import com.tjfintech.common.functionTest.mixTest.TestTxType;
import com.tjfintech.common.functionTest.mixTestWithConfigChange.DynamicChangePeerCluster;
import com.tjfintech.common.functionTest.store.StoreTest;
import com.tjfintech.common.functionTest.store.StoreTest_UpgradeTestOnly;
import com.tjfintech.common.functionTest.store.StoreWalletRelatedTest;
import com.tjfintech.common.functionTest.upgrade.UpgradeTestHistoryData;
import com.tjfintech.common.functionTest.utxoMultiSign.*;
import com.tjfintech.common.functionTest.utxoSingleSign.SoloTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
        SetDatabaseMysql.class,
        SetCertSM2.class,
        SetMainLedger.class,

        SetVerRelease.class,
        BeforeCondition.class,

        StoreTest_UpgradeTestOnly.class,

        MultiTest.class,
        SoloTest.class,

        DockerContractTest.class,
        WVMContractTest_UpgradeTestOnly.class,

        DynamicChangePeerCluster.class,

        TestMainSubChain_UpgradeTestOnly.class,

        UpgradeTestHistoryData.class
})

//Build Validation Test
public class Run_Upgrade_Main_Mysql_Interface {
    //执行这个类将执行suiteClass中的测试项

}
