package com.tjfintech.common.functionTest.BVT.P1_High;

import com.tjfintech.common.functionTest.guDengTestV2.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;


@RunWith(Suite.class)
@Suite.SuiteClasses({

        GDV2_JGFormat_Part1_EnterpriseRegister_AccCreate_Publish_Test.class,//挂牌登记 开户 信披 流程及监管数据校验
        GDV2_JGFormat_Part2_EquityProduct_Settlement_Test.class,//初始股份登记 股份性质变更 转让 冻结 解除冻结 回收 销户 流程及监管数据校验
        GDV2_AllFlowTest_Equity_ChkTxReport.class,//交易报告库数据检查
        GDV2_JGData_Update_SubAccProd_Test.class,//更新主体 更新账户 更新产品接口及监管数据检查

        GDV2_JGData_NonEssentialParamTest.class,//监管数据模型中非必填字段不填写检查
        GDV2_SceneTest_Transfer.class,      //转让双花检查
        GDV2_ShareIssue_UniqueId_Test.class,    //事务一致性检查
        GDV2_AuxTool.class  //跨链对接 区块及交易数据检查


})

//Build Validation Test
public class TS005_Run_GuDeng_Prior {
    //执行这个类将执行suiteClass中的测试项

}
