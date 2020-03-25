package com.tjfintech.common.functionTest.BVT.P2_Middle;

import com.tjfintech.common.functionTest.BVT.P1_High.Run_Main_Mysql;
import com.tjfintech.common.functionTest.BVT.P1_High.Run_Sub_Mysql;
import com.tjfintech.common.functionTest.Conditions.SetSubLedgerSleepTime;
import com.tjfintech.common.functionTest.mixTestWithConfigChange.AddPeerAndSyncData;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({
        //此用例需要重新调整，动态加入节点不在子链节点集群中，无法查看子链信息 可能需要调整或者删除
//        Run_Sub_Mysql.class,
//        SetSubLedgerSleepTime.class,
//        AddPeerAndSyncData.class
})

//Build Validation Test
public class Run_Sub_Mysql_AddPeerSyncData {
    //执行这个类将执行suiteClass中的测试项

}
