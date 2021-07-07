package com.tjfintech.common.functionTest.BVT.P1_High;

import com.tjfintech.common.functionTest.appChainTest.*;
import com.tjfintech.common.functionTest.guDengTestV2.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({

        GDV2_JGFormat_Part1_EnterpriseRegister_AccCreate_Publish_Test.class,
        GDV2_JGFormat_Part2_EquityProduct_Settlement_Test.class,
        GDV2_JGFormat_Part3_SubjectChangeTxSend_Test.class,
        GDV2_AllFlowTest_Equity_ChkTxReport.class,
        GDV2_AllFlowTest_Equity_URIStoreTxDetail.class,
        GDV2_AllFlowTest_Equity_WVMTxDetail.class,

        GDV2_JGFormat_Part5_Superviseinfo_Delete_Update_Test01.class,
        GDV2_JGFormat_Part5_Superviseinfo_Delete_Update_Test02.class,

        GDV2_JGFormat_Part4_DoubleRoles_EnterpriseRegisterThenAccCreate.class,
        GDV2_JGFormat_Part4_DoubleRoles_FlowTest01_Test.class,
        GDV2_JGFormat_Part4_DoubleRoles_FlowTest02_Test.class,
        GDV2_JGFormat_Part4_DoubleRoles_Update_SubAccProd_Test.class,

        GDV2_FlowTest_Prod_Bond.class,
        GDV2_FlowTest_Prod_Fund.class,
        GDV2_JGData_CheckJGHeaderPrinciple_Test.class,
        GDV2_JGDataNecessary_SubProd_Test.class,
        GDV2_JGData_Update_SubAccProd_Test.class,
        GDV2_JGData_RefObjParamExceptionScene.class,
        GDV2_JGData_NonEssentialParamTest.class,

        GDV2_SceneTest_01.class,
        GDV2_SceneTest_02.class,
        GDV2_SceneTest_Issue.class,
        GDV2_SceneTest_ChangeProperty.class,
        GDV2_SceneTest_Transfer.class,
        GDV2_SceneTest_Increase.class,
        GDV2_SceneTest_LockUnLock.class,
        GDV2_SceneTest_Recycle.class,
        GDV2_SceneTest_ChangeBoard.class,
        GDV2_SceneTest_DestroyAccount.class,

        GDV2_ShareIssue_UniqueId_Test.class,
        GDV2_ShareChangeProperty_UniqueId_Test.class,
        GDV2_ShareTransfer_UniqueId_Test.class,
        GDV2_ShareLock_UniqueId_Test.class,
        GDV2_ShareUnlock_UniqueId_Test.class,
        GDV2_ShareIncrease_UniqueId_Test.class,
        GDV2_ShareRecycle_UniqueId_Test.class,
        GDV2_ShareChangeBoard_UniqueId_Test.class,

        GuDengV2_InterfaceTest.class,
        GDV2_TestIssueStop.class,
        GDV2_AuxTool.class


})

//Build Validation Test
public class TS005_Run_GuDeng {
    //执行这个类将执行suiteClass中的测试项

}
