package com.tjfintech.common.functionTest.BVT.P1_High;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.functionTest.Conditions.*;
import com.tjfintech.common.functionTest.sendMessage.CallBackSyncTest;
import com.tjfintech.common.functionTest.tokenModuleTest.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
        SetDatabaseMysql.class,     //负载均衡场景需要注释掉
        SetEventClientStart.class,  //  启动事件通知消息客户端
        SetAccountEmpty.class,
        SetMainLedger.class,
        SetSyncTestSleepTime.class,     //同步测试必须将交易上链查询及等待时间设置为0 即 不等待 复用之前用例
        SetSyncFlagTrue.class,

//        SetURLToSDKAddr.class,
        BeforeCondition.class,

        TokenMultiTest.class,
        TokenPrivateStoreTest.class,
        TokenSoloTest.class,
        TokenStoreTest.class,
        TokenSyncShortTimeoutTest.class,
        CallBackSyncTest.class,

//        SetSubLedgerSleepTime.class,
//        SetSubLedger.class,
//        SetSyncTestSleepTime.class,
//
//        BeforeCondition.class,
//
//        TokenMultiTest.class,
//        TokenPrivateStoreTest.class,
//        TokenSoloTest.class,
//        TokenStoreTest.class,

//        SetURLToSDKAddr.class,
        SetSyncFlagFalse.class,
        SetNormalTestSleepTime.class,
        SetEventClientStop.class,   // 关闭事件通知消息客户端


})

//Build Validation Test
public class Run_Main_Finservice_SyncTrue {
    //执行这个类将执行suiteClass中的测试项

}
