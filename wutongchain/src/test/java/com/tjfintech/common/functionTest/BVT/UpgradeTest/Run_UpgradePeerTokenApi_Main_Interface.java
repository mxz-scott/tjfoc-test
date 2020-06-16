package com.tjfintech.common.functionTest.BVT.UpgradeTest;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.functionTest.upgrade.UpgradeFinserviceTest;
import com.tjfintech.common.functionTest.Conditions.*;
import com.tjfintech.common.functionTest.Conditions.Upgrade.*;
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
        SetSDKStartNoApi.class,             //设置sdk启动不带 "api"命令
        SetTokenApiAddrSolo.class,          //设置api为单独部署的token api地址
        SetDatabaseMysql.class,             //清空节点及SDK数据库
        SetTokenApiDatabaseMysql.class,     //清空Token Api数据库
        SetCertSM2.class,
        SetMainLedger.class,                //设置主链测试

        SetTestVersionRelease.class,        //设置版本为升级前版本
        BeforeCondition.class,              //执行预设条件 赋权限 创建UTXO地址 添加地址等

        UpgradeFinserviceTest.class,        //Api简单回归测试

        GetReleaseVersionDataApi.class,     //获取升级前版本数据

        SetPeerUpgradeTrue.class,           //升级节点 Flag设置为true，即会升级
        SetContractSysUpgradeTrue.class,    //升级系统合约 Flag设置为true，即会升级
        SetSDKUpgradeTrue.class,            //升级SDK Flag设置为true，即会升级
        SetTokenApiUpgradeTrue.class,       //升级Api Flag设置为true，即会升级

        SetURLToSDKAddr.class,

        SetSDKStartWithApi.class,
        SetTokenApiAddrSDK.class,           //设置升级后Api地址和SDK地址一致 升级后的合并版本 非合并版本则不需要设置
        SetTestVersionLatest.class,         //升级版本为升级后的指定版本

        GetLatestVersionDataApi.class,      //获取升级后版本数据

        CompareReleaseLatestData.class,     //比较升级前后数据是否存在差异

        UpgradeFinserviceTest.class,//升级后简单回归测试
})

//Build Validation Test
public class Run_UpgradePeerTokenApi_Main_Interface {
    //执行这个类将执行suiteClass中的测试项

}
