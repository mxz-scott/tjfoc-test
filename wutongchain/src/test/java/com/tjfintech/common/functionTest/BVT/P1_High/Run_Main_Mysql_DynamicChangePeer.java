package com.tjfintech.common.functionTest.BVT.P1_High;

import com.tjfintech.common.functionTest.Conditions.SetCertSM2;
import com.tjfintech.common.functionTest.Conditions.SetMainLedger;
import com.tjfintech.common.functionTest.mixTestWithConfigChange.ChangeConfigPeerInfo_ClearDB;
import com.tjfintech.common.functionTest.mixTestWithConfigChange.DynamicChangePeerCluster_ClearDB;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
        SetCertSM2.class,
        SetMainLedger.class,

        ChangeConfigPeerInfo_ClearDB.class,
        DynamicChangePeerCluster_ClearDB.class,

})

//Build Validation Test
public class Run_Main_Mysql_DynamicChangePeer {
    //执行这个类将执行suiteClass中的测试项

}