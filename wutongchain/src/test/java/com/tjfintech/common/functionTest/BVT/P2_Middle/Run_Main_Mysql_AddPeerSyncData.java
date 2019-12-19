package com.tjfintech.common.functionTest.BVT.P2_Middle;

import com.tjfintech.common.functionTest.BVT.P1_High.Run_Main_Mysql;
import com.tjfintech.common.functionTest.mixTestWithConfigChange.AddPeerAndSyncData;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
        Run_Main_Mysql.class,
        AddPeerAndSyncData.class
})

//Build Validation Test
public class Run_Main_Mysql_AddPeerSyncData {
    //执行这个类将执行suiteClass中的测试项

}
