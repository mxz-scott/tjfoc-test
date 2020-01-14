package com.tjfintech.common.functionTest.BVT.UpgradeTest;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.functionTest.Conditions.*;
import com.tjfintech.common.functionTest.contract.DockerContractTest;
import com.tjfintech.common.functionTest.contract.WVMContractTest_UpgradeTestOnly;
import com.tjfintech.common.functionTest.mainSubChain.*;
import com.tjfintech.common.functionTest.mixTestWithConfigChange.DynamicChangePeerCluster_ClearDB;
import com.tjfintech.common.functionTest.store.StoreTest_UpgradeTestOnly;
import com.tjfintech.common.functionTest.upgrade.UpgradeTestHistoryData;
import com.tjfintech.common.functionTest.utxoMultiSign.*;
import com.tjfintech.common.functionTest.utxoSingleSign.SoloTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/***
 * 重点信息：
 * 升级测试需要测试节点具备如下两种文件，节点升级前版本和节点升级后版本
 * 目前节点升级前版本为2.1 8026版本，节点目录下对应名称为MpRelease
 * sdk升级前名称 sdkRelease
 * 节点升级后版本即当前构建的最新版本，节点目录下对应的名称为MpLatest
 * sdk升级后名称 sdkLatest
 * 其中Mp为实际环境中的节点名称 sdk为实际环境中SDK名称
 * 如果没有以上两类文件，会在执行时报错
 */

@RunWith(Suite.class)
@Suite.SuiteClasses({
        SetDatabaseMysql.class,
        SetCertSM2.class,
        SetMainLedger.class,

        SetTestVersionRelease.class,

        BeforeCondition.class,

        StoreTest_UpgradeTestOnly.class,
        MultiTest.class,
        SoloTest.class,
        DockerContractTest.class,
        WVMContractTest_UpgradeTestOnly.class,

        DynamicChangePeerCluster_ClearDB.class,

        TestMainSubChain_UpgradeTestOnly.class,

        //升级比对 设置主链 设置升级节点和sdk
        SetMainLedger.class,
        SetPeerUpgradeTrue.class,
        SetSDKUpgradeTrue.class,
        SetContractSysUpgradeTrue.class,

        UpgradeTestHistoryData.class,

        //升级后简单回归测试
        StoreTest_UpgradeTestOnly.class,
        MultiTest.class,
        SoloTest.class,
        DockerContractTest.class,
        WVMContractTest_UpgradeTestOnly.class,
        DynamicChangePeerCluster_ClearDB.class,
        TestMainSubChain_UpgradeTestOnly.class,
})

//Build Validation Test
public class Run_UpgradePeerSDK_Main_Interface {
    //执行这个类将执行suiteClass中的测试项

}
