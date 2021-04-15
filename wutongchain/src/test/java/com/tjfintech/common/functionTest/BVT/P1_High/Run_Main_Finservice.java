package com.tjfintech.common.functionTest.BVT.P1_High;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.functionTest.Conditions.*;

import com.tjfintech.common.functionTest.Conditions.SetTokenApiPerm999;
import com.tjfintech.common.functionTest.Conditions.Upgrade.SetTokenApiTransactionLatest;
import com.tjfintech.common.functionTest.Conditions.Upgrade.SetTokenApiTransactionRelease;
import com.tjfintech.common.functionTest.sendMessage.CallBack;
import com.tjfintech.common.functionTest.sendMessage.Event;
import com.tjfintech.common.functionTest.tokenModuleTest.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
//        SetDatabaseMysql.class,
//        SetTokenApiDatabaseMysql.class,
//        SetMainLedger.class,
//        BeforeCondition.class,
//        SetTokenApiPerm999.class,
        SetSyncFlagFalse.class,
//        TokenInterfaceTest.class,
        SetSyncFlagTrue.class,
        SetSyncTestSleepTime.class,     //同步测试将交易上链查询及等待时间设置为2000ms
        TokenMultiTest.class,
        TokenMultiInvalidTest.class,
        TokenSoloTest.class,
        TokenSoloInvalidTest.class,
        TokenStoreTest.class,
        TokenPrivateStoreTest.class,
        TokenAccurateTest.class,
        CallBack.class,
        TokenTxTypeTest_Token.class,

//        Event.class,                 //token api不启用事件通知服务
//        TokenTwoApiMultiTest.class,  //需要部署第二个api并且配置在该class的TOKENADD2参数，数据库地址和第一个api分离
//        SetTokenApiTransactionRelease.class, //用于UTXO升级测试,旧版本执行
//        SetTokenApiTransactionLatest.class //用于UTXO升级测试,新版本执行

})

//Build Validation Test
public class Run_Main_Finservice {
    //执行这个类将执行suiteClass中的测试项

}
