package com.tjfintech.common.functionTest.BVT.UpgradeTest;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.functionTest.Conditions.*;
import com.tjfintech.common.functionTest.Conditions.Upgrade.*;
import com.tjfintech.common.functionTest.contract.WVMContractTest_UpgradeTestOnly;
import com.tjfintech.common.functionTest.mainAppChain.AppChain_Y_UpgradeTestOnly;
import com.tjfintech.common.functionTest.mixTestWithConfigChange.AddPeerAndSyncData;
import com.tjfintech.common.functionTest.store.StoreTest_UpgradeTestOnly;
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
//        SetDatabaseMysql.class,             //清空节点、SDK数据库信息
        SetCertSM2.class,
        SetMainLedger.class,                //设置主链测试

        SetTestVersionRelease.class,        //将版本设置为升级前版本进行后续测试
        BeforeCondition.class,              //执行预设条件 赋权限 创建UTXO地址 添加地址等

        StoreTest_UpgradeTestOnly.class,

        WVMContractTest_UpgradeTestOnly.class,

        AddPeerAndSyncData.class,

        AppChain_Y_UpgradeTestOnly.class,

        //升级比对 设置主链 设置升级节点和sdk
        SetMainLedger.class,
        SetPeerUpgradeTrue.class,           //升级节点 Flag设置为true，即会升级
        SetContractSysUpgradeTrue.class,    //升级系统合约 Flag设置为true，即会升级
        SetSDKUpgradeTrue.class,            //升级SDK Flag设置为true，即会升级
        SetTokenApiUpgradeFalse.class,      //升级Api Flag设置为false，即不会升级

        GetReleaseVersionDataSDK.class,     //获取升级前版本数据

        SetTokenApiAddrSDK.class,           //设置升级后Api地址和SDK地址一致
        SetTestVersionLatest.class,         //升级版本为升级后的指定版本

        GetLatestVersionDataSDK.class,      //获取升级后版本数据

        CompareReleaseLatestData.class,     //比较升级前后数据是否存在差异

        SetSDKPerm999.class, //2.1向3.0升级后需要给SDK重新赋权限
        SetMgToolPerm999.class,//2.1向3.0升级后需要给超级管理员或者管理系统重新赋权限
        //升级后简单回归测试
        StoreTest_UpgradeTestOnly.class,
        WVMContractTest_UpgradeTestOnly.class,
        AddPeerAndSyncData.class,
        AppChain_Y_UpgradeTestOnly.class,
})

//Build Validation Test
public class Run_UpgradePeerSDK_Main_Interface {
    //执行这个类将执行suiteClass中的测试项

}
