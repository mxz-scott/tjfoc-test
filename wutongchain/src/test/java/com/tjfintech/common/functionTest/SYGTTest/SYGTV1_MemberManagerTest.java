package com.tjfintech.common.functionTest.SYGTTest;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.SYGT;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import javafx.scene.layout.BackgroundImage;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

import static com.tjfintech.common.utils.UtilsClassSYGT.*;
import static com.tjfintech.common.utils.UtilsClass.*;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class SYGTV1_MemberManagerTest {

    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    SYGT sygt = testBuilder.getSygt();
    SYGTCommonFunc sygtCF = new SYGTCommonFunc();
    public static String desc = "exit";
    Boolean bCheckPoint = false;


    @Before
    public void updateCode(){
        code3 = "m3code" + UtilsClass.Random(6); //成员1
        name3 = "m3name" + UtilsClass.Random(6); //成员1
    }

    //执行后退出操作的成员
//    @After
    public void exitMember()throws Exception{
        sygtCF.memberExit(code3,desc);
    }
    /**
     * 成员申请加入 查询成员列表 检查审批列表 盟主审批通过 查询成员列表 检查审批列表
     * 盟主退出申请 检查审批列表 同意退出
     * @throws Exception
     */
    @Test
    public void TC01_MemberJoinApplyExitApprove() throws Exception {

        String response = "";

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account3, contributePointType);
            assertEquals("400", JSONObject.fromObject(response).getString("state"));
        }
        SDKADD = SDKURL1;     //SDK设置为盟主1 SDK
        //初始确认无待审批列表
        response = sygt.SSPendingApplyGet();
        assertEquals(0, StringUtils.countOccurrencesOf(response,code3));
        int joinNum = StringUtils.countOccurrencesOf(response,accStatusJoinReview);
        int exitNum = StringUtils.countOccurrencesOf(response,accStatusExitApply);


        SDKADD = SDKURLm1;     //SDK设置为成员SDK
        //提交成员加入申请
        response = sygt.SSMemberJoinApply(code3,name3,endPoint3,account3);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //获取积分 无积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account3, contributePointType);
            assertEquals("400", JSONObject.fromObject(response).getString("state"));
        }
        SDKADD = SDKURL1;     //SDK设置为盟主1 SDK
        //检查成员列表 方式1
        response = sygt.SSMembersGet();
        sygtCF.checkMemberInfo(response,code3,name3,endPoint3,account3,accStatusJoinApply,false,"");

        //获取审批列表
        response = sygt.SSPendingApplyGet();
        assertEquals(1, StringUtils.countOccurrencesOf(response,code3));
        assertEquals(joinNum + 1, StringUtils.countOccurrencesOf(response,accStatusJoinReview));
        assertEquals(exitNum, StringUtils.countOccurrencesOf(response,accStatusExitApply));

        //盟主审批通过
        sygtCF.JoinApproveTwoLeaders(code3,true,true);

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account3, contributePointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }
        //检查成员列表 方式1
        response = sygt.SSMembersGet();
        sygtCF.checkMemberInfo(response,code3,name3,endPoint3,account3,accStatusJoinSuccess,false,"");

        //获取审批列表
        response = sygt.SSPendingApplyGet();
        assertEquals(0, StringUtils.countOccurrencesOf(response,code3));
        assertEquals(joinNum, StringUtils.countOccurrencesOf(response,accStatusJoinReview));
        assertEquals(exitNum, StringUtils.countOccurrencesOf(response,accStatusExitApply));

        SDKADD = SDKURL1;     //SDK设置为盟主1 SDK
        //盟主1退出申请
        response = sygt.SSMemberExitApply(code3,desc);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));
        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account3, contributePointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }
        //获取审批列表
        response = sygt.SSPendingApplyGet();
        assertEquals(1, StringUtils.countOccurrencesOf(response,code3));
        assertEquals(joinNum, StringUtils.countOccurrencesOf(response,accStatusJoinReview));
        assertEquals(exitNum + 1, StringUtils.countOccurrencesOf(response,accStatusExitApply));

        //检查成员列表 方式1
        response = sygt.SSMembersGet();
        sygtCF.checkMemberInfo(response,code3,name3,endPoint3,account3,accStatusExitApply,false,"");


        //退出盟主审批通过
        sygtCF.ExitApprove(SDKURL2,code3,true);
        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account3, contributePointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(0, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }
        //获取审批列表
        response = sygt.SSPendingApplyGet();
        assertEquals(0, StringUtils.countOccurrencesOf(response,code3));
        assertEquals(joinNum, StringUtils.countOccurrencesOf(response,accStatusJoinReview));
        assertEquals(exitNum, StringUtils.countOccurrencesOf(response,accStatusExitApply));

        //检查成员列表 方式1
        response = sygt.SSMembersGet();
        sygtCF.checkMemberInfo(response,code3,name3,endPoint3,account3,accStatusExitSuccess,false,"");

    }

    /**
     * 成员申请加入 查询成员列表 检查审批列表 盟主2审批拒绝 再次申请加入 审批通过 查询成员列表 检查审批列表
     * @throws Exception
     */
    @Test
    public void TC02_MemberJoinReject_JoinPass() throws Exception {
        SDKADD = SDKURL1;     //SDK设置为盟主1 SDK
        //初始确认无待审批列表
        String response = sygt.SSPendingApplyGet();
        assertEquals(0, StringUtils.countOccurrencesOf(response,code3));
        int joinNum = StringUtils.countOccurrencesOf(response,accStatusJoinReview);
        int exitNum = StringUtils.countOccurrencesOf(response,accStatusExitApply);

        SDKADD = SDKURLm1;     //SDK设置为成员SDK
        //提交成员加入申请
        response = sygt.SSMemberJoinApply(code3,name3,endPoint3,account3);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));
        SDKADD = SDKURL1;     //SDK设置为盟主1 SDK
        //检查成员列表 方式1
        response = sygt.SSMembersGet();
        sygtCF.checkMemberInfo(response,code3,name3,endPoint3,account3,accStatusJoinApply,false,"");

        //获取审批列表
        response = sygt.SSPendingApplyGet();
        assertEquals(1, StringUtils.countOccurrencesOf(response,code3));
        assertEquals(joinNum + 1, StringUtils.countOccurrencesOf(response,accStatusJoinReview));
        assertEquals(exitNum, StringUtils.countOccurrencesOf(response,accStatusExitApply));

        //盟主审批拒绝
        sygtCF.JoinApproveTwoLeaders(code3,false,false);

        //获取积分 不存在账户
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account3, contributePointType);
            assertEquals("400", JSONObject.fromObject(response).getString("state"));
            response = sygt.SSPointQuery(account3, contributePointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(0, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }
        //检查成员列表 方式1
        response = sygt.SSMembersGet();
        assertEquals(false, response.contains(code3));


        //获取审批列表
        response = sygt.SSPendingApplyGet();
        assertEquals(0, StringUtils.countOccurrencesOf(response,code3));
        assertEquals(joinNum, StringUtils.countOccurrencesOf(response,accStatusJoinReview));
        assertEquals(exitNum, StringUtils.countOccurrencesOf(response,accStatusExitApply));

        SDKADD = SDKURLm1;     //SDK设置为成员SDK
        //再次提交成员加入申请
        response = sygt.SSMemberJoinApply(code3,name3,endPoint3,account3);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));
        SDKADD = SDKURL2;     //SDK设置为盟主2 SDK
        //检查成员列表 方式1
        response = sygt.SSMembersGet();
        sygtCF.checkMemberInfo(response,code3,name3,endPoint3,account3,accStatusJoinApply,false,"");

        //获取审批列表
        response = sygt.SSPendingApplyGet();
        assertEquals(1, StringUtils.countOccurrencesOf(response,code3));
        assertEquals(joinNum + 1, StringUtils.countOccurrencesOf(response,accStatusJoinReview));
        assertEquals(exitNum, StringUtils.countOccurrencesOf(response,accStatusExitApply));

        //盟主审批通过
        sygtCF.JoinApproveTwoLeaders(code3,true,true);

        //检查成员列表 方式1
        response = sygt.SSMembersGet();
        sygtCF.checkMemberInfo(response,code3,name3,endPoint3,account3,accStatusJoinSuccess,false,"");

        //获取审批列表
        response = sygt.SSPendingApplyGet();
        assertEquals(0, StringUtils.countOccurrencesOf(response,code3));
        assertEquals(joinNum, StringUtils.countOccurrencesOf(response,accStatusJoinReview));
        assertEquals(exitNum, StringUtils.countOccurrencesOf(response,accStatusExitApply));

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account3, contributePointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }
    }


    /**
     * 成员申请加入 查询成员列表 检查审批列表 盟主1审批通过 盟主2审批拒绝 查询成员列表 检查审批列表
     * @throws Exception
     */
    @Test
    public void TC03_MemberJoinReject_JoinPass02() throws Exception {
        SDKADD = SDKURL1;     //SDK设置为盟主1 SDK
        //初始确认无待审批列表
        String response = sygt.SSPendingApplyGet();
        assertEquals(0, StringUtils.countOccurrencesOf(response,code3));
        int joinNum = StringUtils.countOccurrencesOf(response,accStatusJoinReview);
        int exitNum = StringUtils.countOccurrencesOf(response,accStatusExitApply);

        SDKADD = SDKURLm1;     //SDK设置为成员SDK
        //提交成员加入申请
        response = sygt.SSMemberJoinApply(code3,name3,endPoint3,account3);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        SDKADD = SDKURL1;     //SDK设置为盟主1 SDK
        //检查成员列表 方式1
        response = sygt.SSMembersGet();
        sygtCF.checkMemberInfo(response,code3,name3,endPoint3,account3,accStatusJoinApply,false,"");

        //获取审批列表
        response = sygt.SSPendingApplyGet();
        assertEquals(1, StringUtils.countOccurrencesOf(response,code3));
        assertEquals(joinNum + 1, StringUtils.countOccurrencesOf(response,accStatusJoinReview));
        assertEquals(exitNum, StringUtils.countOccurrencesOf(response,accStatusExitApply));

        //盟主审批拒绝
        sygtCF.JoinApproveTwoLeaders(code3,true,false);

        //获取积分 不存在账户
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account3, contributePointType);
            assertEquals("400", JSONObject.fromObject(response).getString("state"));
            response = sygt.SSPointQuery(account3, contributePointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(0, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }
        //检查成员列表 方式1
        response = sygt.SSMembersGet();
        assertEquals(false, response.contains(code3));

        //获取审批列表
        response = sygt.SSPendingApplyGet();
        assertEquals(0, StringUtils.countOccurrencesOf(response,code3));
        assertEquals(joinNum, StringUtils.countOccurrencesOf(response,accStatusJoinReview));
        assertEquals(exitNum, StringUtils.countOccurrencesOf(response,accStatusExitApply));

        SDKADD = SDKURLm1;     //SDK设置为成员SDK
        //再次提交成员加入申请
        response = sygt.SSMemberJoinApply(code3,name3,endPoint3,account3);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        SDKADD = SDKURL2;     //SDK设置为盟主2 SDK
        //检查成员列表 方式1
        response = sygt.SSMembersGet();
        sygtCF.checkMemberInfo(response,code3,name3,endPoint3,account3,accStatusJoinApply,false,"");

        //获取审批列表
        response = sygt.SSPendingApplyGet();
        assertEquals(1, StringUtils.countOccurrencesOf(response,code3));
        assertEquals(joinNum + 1, StringUtils.countOccurrencesOf(response,accStatusJoinReview));
        assertEquals(exitNum, StringUtils.countOccurrencesOf(response,accStatusExitApply));

        //盟主审批通过
        sygtCF.JoinApproveTwoLeaders(code3,true,true);

        //检查成员列表 方式1
        response = sygt.SSMembersGet();
        sygtCF.checkMemberInfo(response,code3,name3,endPoint3,account3,accStatusJoinSuccess,false,"");

        //获取审批列表
        response = sygt.SSPendingApplyGet();
        assertEquals(0, StringUtils.countOccurrencesOf(response,code3));
        assertEquals(joinNum, StringUtils.countOccurrencesOf(response,accStatusJoinReview));
        assertEquals(exitNum, StringUtils.countOccurrencesOf(response,accStatusExitApply));

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account3, contributePointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }
    }

    /**
     * 成员加入后 盟主2 提交成员退出申请 盟主审批拒绝 再次提出退出 审批通过
     * @throws Exception
     */
    @Test
    public void TC04_MemberExitApplyRejectMoreExitPass() throws Exception {

        SDKADD = SDKURL1;     //SDK设置为盟主1 SDK
        //初始确认无待审批列表
        String response = sygt.SSPendingApplyGet();
        assertEquals(0, StringUtils.countOccurrencesOf(response,code3));
        int joinNum = StringUtils.countOccurrencesOf(response,accStatusJoinReview);
        int exitNum = StringUtils.countOccurrencesOf(response,accStatusExitApply);

        SDKADD = SDKURLm1;     //SDK设置为成员SDK
        //提交加入申请
        response = sygt.SSMemberJoinApply(code3,name3,endPoint3,account3);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        SDKADD = SDKURL2;     //SDK设置为盟主2 SDK
        //盟主审批通过
        sygtCF.JoinApproveTwoLeaders(code3,true,true);

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account3, contributePointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }
        //检查成员列表 方式1
        response = sygt.SSMembersGet();
        sygtCF.checkMemberInfo(response,code3,name3,endPoint3,account3,accStatusJoinSuccess,false,"");

        //盟主2提交成员退出申请
        response = sygt.SSMemberExitApply(code3,desc);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //检查成员列表 方式1
        response = sygt.SSMembersGet();
        sygtCF.checkMemberInfo(response,code3,name3,endPoint3,account3,accStatusExitApply,false,"");

        //获取审批列表
        response = sygt.SSPendingApplyGet();
        assertEquals(1, StringUtils.countOccurrencesOf(response,code3));
        assertEquals(joinNum, StringUtils.countOccurrencesOf(response,accStatusJoinReview));
        assertEquals(exitNum + 1, StringUtils.countOccurrencesOf(response,accStatusExitApply));

        //盟主1审批退出拒绝
        sygtCF.ExitApprove(SDKURL1,code3,false);

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account3, contributePointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }
        //检查成员列表 方式1
        response = sygt.SSMembersGet();
        sygtCF.checkMemberInfo(response,code3,name3,endPoint3,account3,accStatusJoinSuccess,false,"");

        //获取审批列表
        response = sygt.SSPendingApplyGet();
        assertEquals(0, StringUtils.countOccurrencesOf(response,code3));
        assertEquals(joinNum, StringUtils.countOccurrencesOf(response,accStatusJoinReview));
        assertEquals(exitNum, StringUtils.countOccurrencesOf(response,accStatusExitApply));


        //盟主2再次提交成员退出申请
        SDKADD = SDKURL2;
        response = sygt.SSMemberExitApply(code3,desc);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account3, contributePointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }
        //检查成员列表 方式1
        response = sygt.SSMembersGet();
        sygtCF.checkMemberInfo(response,code3,name3,endPoint3,account3,accStatusExitApply,false,"");

        //获取审批列表
        response = sygt.SSPendingApplyGet();
        assertEquals(1, StringUtils.countOccurrencesOf(response,code3));
        assertEquals(joinNum, StringUtils.countOccurrencesOf(response,accStatusJoinReview));
        assertEquals(exitNum + 1, StringUtils.countOccurrencesOf(response,accStatusExitApply));

        SDKADD = SDKURL2;     //SDK设置为盟主2 SDK
        //盟主审批退出通过
        sygtCF.ExitApprove(SDKURL1,code3,true);

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account3, contributePointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(0, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }
        //检查成员列表 方式1
        response = sygt.SSMembersGet();
        sygtCF.checkMemberInfo(response,code3,name3,endPoint3,account3,accStatusExitSuccess,false,"");

        //获取审批列表
        response = sygt.SSPendingApplyGet();
        assertEquals(0, StringUtils.countOccurrencesOf(response,code3));
        assertEquals(joinNum, StringUtils.countOccurrencesOf(response,accStatusJoinReview));
        assertEquals(exitNum, StringUtils.countOccurrencesOf(response,accStatusExitApply));

    }

    /**
     * 各方查看信息
     * 申请后 盟主1审批 盟主2审批 盟主2退出申请 盟主1审批
     * @throws Exception
     */
    @Test
    public void TC10_MemberJoinExitStepByStepCheck01() throws Exception {

        SDKADD = SDKURL1;     //SDK设置为盟主1 SDK
        //初始确认无待审批列表
        String response = sygt.SSPendingApplyGet();
        assertEquals(0, StringUtils.countOccurrencesOf(response,code3));
        int joinNum = StringUtils.countOccurrencesOf(response,accStatusJoinReview);
        int joinReviewNum = StringUtils.countOccurrencesOf(response,accStatusJoinReview);
        int exitNum = StringUtils.countOccurrencesOf(response,accStatusExitApply);

        SDKADD = SDKURLm1;     //SDK设置为成员SDK
        //提交成员加入申请
        response = sygt.SSMemberJoinApply(code3,name3,endPoint3,account3);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //在各个SDK上检查成员列表中的加入成员信息 一次 SDK1 SDK2 SDKm1（盟主1 盟主2 第三方成员）
        sygtCF.checkMemInfoInAllSDK(code3,name3,endPoint3,account3,accStatusJoinApply,false,joinDate,
                "","",false,false,true);
        //在各个SDK上检查成员列表中的加入成员信息 检查审批列表及信息
        sygtCF.checkJoinApplyInfoInAllSDK(code3,name3,endPoint3,account3,"",joinNum,exitNum);



        //盟主1 审批通过
        SDKADD = SDKURL1;     //SDK设置为盟主1 SDK
        response = sygt.SSMemberJoinApprove(code3, true);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account3, contributePointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
            response = sygt.SSPointQuery(account3, contributePointType);
            assertEquals("400", JSONObject.fromObject(response).getString("state"));
        }
        //在各个SDK上检查成员列表中的加入成员信息 一次 SDK1 SDK2 SDKm1（盟主1 盟主2 第三方成员）
        sygtCF.checkMemInfoInAllSDK(code3,name3,endPoint3,account3,accStatusJoinReview,false,joinDate,
                account1,"",false,false,true);
        //在各个SDK上检查成员列表中的加入成员信息 检查审批列表及信息
        sygtCF.checkJoinApplyInfoInAllSDK(code3,name3,endPoint3,account3,"",joinNum,exitNum);


        SDKADD = SDKURL2;     //SDK设置为盟主2 SDK
        response = sygt.SSMemberJoinApprove(code3, true);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account3, contributePointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }
        //在各个SDK上检查成员列表中的加入成员信息 一次 SDK1 SDK2 SDKm1（盟主1 盟主2 第三方成员）
        sygtCF.checkMemInfoInAllSDK(code3,name3,endPoint3,account3,accStatusJoinSuccess,false,joinDate,
                account1 + "," + account2,"",false,false,true);

        //在各个SDK上检查成员列表中的加入成员信息 检查审批列表及信息
        sygtCF.checkNoApplyInfoInAllSDK(code3,joinNum,exitNum);

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account3, contributePointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }

        SDKADD = SDKURL2;     //SDK设置为盟主2 SDK
        //盟主2退出申请
        response = sygt.SSMemberExitApply(code3,desc);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //在各个SDK上检查成员列表中的成员信息 一次 SDK1 SDK2 SDKm1（盟主1 盟主2 第三方成员）
        sygtCF.checkMemInfoInAllSDK(code3,name3,endPoint3,account3,accStatusExitApply,false,joinDate,
                account1 + "," + account2,account2,false,false,true);
        //在各个SDK上检查成员列表中的加入成员信息 检查审批列表及信息
        sygtCF.checkExitApplyInfoInAllSDK(code3,name3,endPoint3,account3,"",joinNum,exitNum);



        //退出盟主1审批通过
        SDKADD = SDKURL1;
        response = sygt.SSMemberExitApprove(code3, true);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));
        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account3, contributePointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }
        //在各个SDK上检查成员列表中的成员信息 一次 SDK1 SDK2 SDKm1（盟主1 盟主2 第三方成员）
        sygtCF.checkMemInfoInAllSDK(code3,name3,endPoint3,account3,accStatusExitSuccess,false,joinDate,
                account1 + "," + account2,account2 + "," + account1,false,false,true);
        //在各个SDK上检查成员列表中的加入成员信息 检查审批列表及信息
        sygtCF.checkNoApplyInfoInAllSDK(code3,joinNum,exitNum);

    }

    /**
     * 各方查看信息
     * 申请后 盟主2审批 盟主1审批 盟主1退出申请 盟主2审批
     * @throws Exception
     */
    @Test
    public void TC11_MemberJoinExitStepByStepCheck02() throws Exception {

        SDKADD = SDKURL1;     //SDK设置为盟主1 SDK
        //初始确认无待审批列表
        String response = sygt.SSPendingApplyGet();
        assertEquals(0, StringUtils.countOccurrencesOf(response,code3));
        int joinNum = StringUtils.countOccurrencesOf(response,accStatusJoinReview);
        int joinReviewNum = StringUtils.countOccurrencesOf(response,accStatusJoinReview);
        int exitNum = StringUtils.countOccurrencesOf(response,accStatusExitApply);

        SDKADD = SDKURLm1;     //SDK设置为成员SDK
        //提交成员加入申请
        response = sygt.SSMemberJoinApply(code3,name3,endPoint3,account3);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //在各个SDK上检查成员列表中的加入成员信息 一次 SDK1 SDK2 SDKm1（盟主1 盟主2 第三方成员）
        sygtCF.checkMemInfoInAllSDK(code3,name3,endPoint3,account3,accStatusJoinApply,false,joinDate,
                "","",false,false,true);
        //在各个SDK上检查成员列表中的加入成员信息 检查审批列表及信息
        sygtCF.checkJoinApplyInfoInAllSDK(code3,name3,endPoint3,account3,"",joinNum,exitNum);

        //盟主2 审批通过
        SDKADD = SDKURL2;     //SDK设置为盟主2 SDK
        response = sygt.SSMemberJoinApprove(code3, true);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account3, contributePointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
            response = sygt.SSPointQuery(account3, contributePointType);
            assertEquals("400", JSONObject.fromObject(response).getString("state"));
        }
        //在各个SDK上检查成员列表中的加入成员信息 一次 SDK1 SDK2 SDKm1（盟主1 盟主2 第三方成员）
        sygtCF.checkMemInfoInAllSDK(code3,name3,endPoint3,account3,accStatusJoinReview,false,joinDate,
                account2,"",false,false,true);
        //在各个SDK上检查成员列表中的加入成员信息 检查审批列表及信息
        sygtCF.checkJoinApplyInfoInAllSDK(code3,name3,endPoint3,account3,"",joinNum,exitNum);


        SDKADD = SDKURL1;     //SDK设置为盟主1 SDK
        response = sygt.SSMemberJoinApprove(code3, true);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account3, contributePointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }
        //在各个SDK上检查成员列表中的加入成员信息 一次 SDK1 SDK2 SDKm1（盟主1 盟主2 第三方成员）
        sygtCF.checkMemInfoInAllSDK(code3,name3,endPoint3,account3,accStatusJoinSuccess,false,joinDate,
                account2 + "," + account1,"",false,false,true);
        //在各个SDK上检查成员列表中的加入成员信息 检查审批列表及信息
        sygtCF.checkNoApplyInfoInAllSDK(code3,joinNum,exitNum);

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account3, contributePointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }

        SDKADD = SDKURL1;     //SDK设置为盟主1 SDK
        //盟主1退出申请
        response = sygt.SSMemberExitApply(code3,desc);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //在各个SDK上检查成员列表中的成员信息 一次 SDK1 SDK2 SDKm1（盟主1 盟主2 第三方成员）
        sygtCF.checkMemInfoInAllSDK(code3,name3,endPoint3,account3,accStatusExitApply,false,joinDate,
                account2 + "," + account1,account1,false,false,true);
        //在各个SDK上检查成员列表中的加入成员信息 检查审批列表及信息
        sygtCF.checkExitApplyInfoInAllSDK(code3,name3,endPoint3,account3,"",joinNum,exitNum);


        //退出盟主2审批通过
        SDKADD = SDKURL2;
        response = sygt.SSMemberExitApprove(code3, true);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account3, contributePointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }
        //在各个SDK上检查成员列表中的成员信息 一次 SDK1 SDK2 SDKm1（盟主1 盟主2 第三方成员）
        sygtCF.checkMemInfoInAllSDK(code3,name3,endPoint3,account3,accStatusExitSuccess,false,joinDate,
                account2 + "," + account1,account1 + "," + account2,false,false,true);
        //在各个SDK上检查成员列表中的加入成员信息 检查审批列表及信息
        sygtCF.checkNoApplyInfoInAllSDK(code3,joinNum,exitNum);

    }

    /**
     * 成员申请加入 查询成员列表 检查审批列表 盟主审批通过 查询成员列表 检查审批列表 再次申请加入
     * @throws Exception
     */
    @Test
    public void TC31_MemberJoinPassMoreJoinApply() throws Exception {

        SDKADD = SDKURL1;     //SDK设置为盟主1 SDK
        //初始确认无待审批列表
        String response = sygt.SSPendingApplyGet();
        assertEquals(0, StringUtils.countOccurrencesOf(response,code3));
        int joinNum = StringUtils.countOccurrencesOf(response,accStatusJoinReview);
        int exitNum = StringUtils.countOccurrencesOf(response,accStatusExitApply);

        SDKADD = SDKURLm1;     //SDK设置为成员SDK
        //提交成员加入申请
        response = sygt.SSMemberJoinApply(code3,name3,endPoint3,account3);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        SDKADD = SDKURL1;     //SDK设置为盟主1 SDK
        //检查成员列表 方式1
        response = sygt.SSMembersGet();
        sygtCF.checkMemberInfo(response,code3,name3,endPoint3,account3,accStatusJoinApply,false,"");

        //获取审批列表
        response = sygt.SSPendingApplyGet();
        assertEquals(1, StringUtils.countOccurrencesOf(response,code3));
        assertEquals(joinNum + 1, StringUtils.countOccurrencesOf(response,accStatusJoinReview));
        assertEquals(exitNum, StringUtils.countOccurrencesOf(response,accStatusExitApply));

        //盟主审批通过
        sygtCF.JoinApproveTwoLeaders(code3,true,true);


        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account3, contributePointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }
        //检查成员列表 方式1
        response = sygt.SSMembersGet();
        sygtCF.checkMemberInfo(response,code3,name3,endPoint3,account3,accStatusJoinSuccess,false,"");

        //获取审批列表
        response = sygt.SSPendingApplyGet();
        assertEquals(0, StringUtils.countOccurrencesOf(response,code3));
        assertEquals(joinNum, StringUtils.countOccurrencesOf(response,accStatusJoinReview));
        assertEquals(exitNum, StringUtils.countOccurrencesOf(response,accStatusExitApply));

        SDKADD = SDKURLm1;     //SDK设置为成员SDK
        //再次提交成员加入申请
        response = sygt.SSMemberJoinApply(code3,name3,endPoint3,account3);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //不可以重复添加
        assertEquals("404", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        SDKADD = SDKURL1;     //SDK设置为盟主1 SDK

        //获取审批列表
        response = sygt.SSPendingApplyGet();
        assertEquals(0, StringUtils.countOccurrencesOf(response,code3));
        assertEquals(joinNum, StringUtils.countOccurrencesOf(response,accStatusJoinReview));
        assertEquals(exitNum, StringUtils.countOccurrencesOf(response,accStatusExitApply));


        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account3, contributePointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }
    }


    /**
     * 成员加入后 盟主提交成员退出申请 盟主审批通过 再次提交退出申请
     * @throws Exception
     */
    @Test
    public void TC32_MemberExitApplyPassMoreExitApply() throws Exception {

        SDKADD = SDKURL1;     //SDK设置为盟主1 SDK
        //初始确认无待审批列表
        String response = sygt.SSPendingApplyGet();
        assertEquals(0, StringUtils.countOccurrencesOf(response,code3));
        int joinNum = StringUtils.countOccurrencesOf(response,accStatusJoinReview);
        int exitNum = StringUtils.countOccurrencesOf(response,accStatusExitApply);

        SDKADD = SDKURLm1;     //SDK设置为成员SDK
        //提交加入申请
        response = sygt.SSMemberJoinApply(code3,name3,endPoint3,account3);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        SDKADD = SDKURL1;     //SDK设置为盟主1 SDK
        //盟主审批通过
        sygtCF.JoinApproveTwoLeaders(code3,true,true);

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account3, contributePointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }
        //检查成员列表 方式1
        response = sygt.SSMembersGet();
        sygtCF.checkMemberInfo(response,code3,name3,endPoint3,account3,accStatusJoinSuccess,false,"");

        SDKADD = SDKURL1;     //SDK设置为盟主1 SDK
        //盟主1提交成员退出申请
        response = sygt.SSMemberExitApply(code3,desc);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //检查成员列表 方式1
        response = sygt.SSMembersGet();
        sygtCF.checkMemberInfo(response,code3,name3,endPoint3,account3,accStatusExitApply,false,"");

        //获取审批列表
        response = sygt.SSPendingApplyGet();
        assertEquals(1, StringUtils.countOccurrencesOf(response,code3));
        assertEquals(joinNum, StringUtils.countOccurrencesOf(response,accStatusJoinReview));
        assertEquals(exitNum + 1, StringUtils.countOccurrencesOf(response,accStatusExitApply));


        SDKADD = SDKURL2;     //SDK设置为盟主2 SDK
        //盟主2审批退出通过
        sygtCF.ExitApprove(SDKURL2,code3,true);

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account3, contributePointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(0, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }
        //检查成员列表 方式1
        response = sygt.SSMembersGet();
        sygtCF.checkMemberInfo(response,code3,name3,endPoint3,account3,accStatusExitSuccess,false,"");

        //获取审批列表
        response = sygt.SSPendingApplyGet();
        assertEquals(0, StringUtils.countOccurrencesOf(response,code3));
        assertEquals(joinNum, StringUtils.countOccurrencesOf(response,accStatusJoinReview));
        assertEquals(exitNum, StringUtils.countOccurrencesOf(response,accStatusExitApply));


        //盟主再次提交成员退出申请
        response = sygt.SSMemberExitApply(code3,desc);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //不可以重复审批通过
        assertEquals("404", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //检查成员列表 方式1
        response = sygt.SSMembersGet();
        sygtCF.checkMemberInfo(response,code3,name3,endPoint3,account3,accStatusExitSuccess,false,"");

        //获取审批列表
        response = sygt.SSPendingApplyGet();
        assertEquals(0, StringUtils.countOccurrencesOf(response,code3));
        assertEquals(joinNum, StringUtils.countOccurrencesOf(response,accStatusJoinReview));
        assertEquals(exitNum, StringUtils.countOccurrencesOf(response,accStatusExitApply));

    }

    /**
     * 成员加入后 盟主提交成员退出申请 盟主审批通过 再次审批通过/拒绝
     * @throws Exception
     */
    @Test
    public void TC33_MemberExitApplyPassRePass() throws Exception {

        SDKADD = SDKURL1;     //SDK设置为盟主1 SDK
        //初始确认无待审批列表
        String response = sygt.SSPendingApplyGet();
        assertEquals(0, StringUtils.countOccurrencesOf(response,code3));
        int joinNum = StringUtils.countOccurrencesOf(response,accStatusJoinReview);
        int exitNum = StringUtils.countOccurrencesOf(response,accStatusExitApply);

        SDKADD = SDKURLm1;     //SDK设置为成员SDK
        //提交加入申请
        response = sygt.SSMemberJoinApply(code3,name3,endPoint3,account3);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        SDKADD = SDKURL1;     //SDK设置为盟主1 SDK
        //盟主审批通过
        sygtCF.JoinApproveTwoLeaders(code3,true,true);

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account3, contributePointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }
        //检查成员列表 方式1
        response = sygt.SSMembersGet();
        sygtCF.checkMemberInfo(response,code3,name3,endPoint3,account3,accStatusJoinSuccess,false,"");

        SDKADD = SDKURL1;     //SDK设置为盟主1 SDK
        //盟主1提交成员退出申请
        response = sygt.SSMemberExitApply(code3,desc);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //检查成员列表 方式1
        response = sygt.SSMembersGet();
        sygtCF.checkMemberInfo(response,code3,name3,endPoint3,account3,accStatusExitApply,false,"");

        //获取审批列表
        response = sygt.SSPendingApplyGet();
        assertEquals(1, StringUtils.countOccurrencesOf(response,code3));
        assertEquals(joinNum, StringUtils.countOccurrencesOf(response,accStatusJoinReview));
        assertEquals(exitNum + 1, StringUtils.countOccurrencesOf(response,accStatusExitApply));

        SDKADD = SDKURL1;     //SDK设置为盟主1 SDK
        //盟主审批退出通过
        sygtCF.ExitApprove(SDKURL2,code3,true);

        //检查成员列表 方式1
        response = sygt.SSMembersGet();
        sygtCF.checkMemberInfo(response,code3,name3,endPoint3,account3,accStatusExitSuccess,false,"");

        //获取审批列表
        response = sygt.SSPendingApplyGet();
        assertEquals(0, StringUtils.countOccurrencesOf(response,code3));
        assertEquals(joinNum, StringUtils.countOccurrencesOf(response,accStatusJoinReview));
        assertEquals(exitNum, StringUtils.countOccurrencesOf(response,accStatusExitApply));

        //盟主再次审批退出通过
        SDKADD = SDKURL1;     //SDK设置为盟主1 SDK
        response = sygt.SSMemberExitApprove(code3, false);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //不可以重复审批通过
        assertEquals("404", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        SDKADD = SDKURL2;     //SDK设置为盟主2 SDK
        response = sygt.SSMemberExitApprove(code3, false);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //不可以重复审批通过
        assertEquals("404", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account3, contributePointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(0, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }
    }

    /**
     * 成员加入后 盟主提交成员退出申请 盟主审批拒绝 再次审批通过/拒绝
     * @throws Exception
     */
    @Test
    public void TC34_MemberExitApplyRejectRePass() throws Exception {

        SDKADD = SDKURL1;     //SDK设置为盟主1 SDK
        //初始确认无待审批列表
        String response = sygt.SSPendingApplyGet();
        assertEquals(0, StringUtils.countOccurrencesOf(response,code3));
        int joinNum = StringUtils.countOccurrencesOf(response,accStatusJoinReview);
        int exitNum = StringUtils.countOccurrencesOf(response,accStatusExitApply);

        SDKADD = SDKURLm1;     //SDK设置为成员SDK
        //提交加入申请
        response = sygt.SSMemberJoinApply(code3,name3,endPoint3,account3);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        SDKADD = SDKURL1;     //SDK设置为盟主1 SDK
        //盟主审批通过
        sygtCF.JoinApproveTwoLeaders(code3,true,true);

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account3, contributePointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }
        //检查成员列表 方式1
        response = sygt.SSMembersGet();
        sygtCF.checkMemberInfo(response,code3,name3,endPoint3,account3,accStatusJoinSuccess,false,"");

        SDKADD = SDKURL1;     //SDK设置为盟主1 SDK
        //盟主1提交成员退出申请
        response = sygt.SSMemberExitApply(code3,desc);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //检查成员列表 方式1
        response = sygt.SSMembersGet();
        sygtCF.checkMemberInfo(response,code3,name3,endPoint3,account3,accStatusExitApply,false,"");

        //获取审批列表
        response = sygt.SSPendingApplyGet();
        assertEquals(1, StringUtils.countOccurrencesOf(response,code3));
        assertEquals(joinNum, StringUtils.countOccurrencesOf(response,accStatusJoinReview));
        assertEquals(exitNum + 1, StringUtils.countOccurrencesOf(response,accStatusExitApply));

        //盟主2审批退出拒绝
        sygtCF.ExitApprove(SDKURL2,code3,false);

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account3, contributePointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }
        //检查成员列表 方式1
        response = sygt.SSMembersGet();
        sygtCF.checkMemberInfo(response,code3,name3,endPoint3,account3,accStatusJoinSuccess,false,"");

        //获取审批列表
        response = sygt.SSPendingApplyGet();
        assertEquals(0, StringUtils.countOccurrencesOf(response,code3));
        assertEquals(joinNum, StringUtils.countOccurrencesOf(response,accStatusJoinReview));
        assertEquals(exitNum, StringUtils.countOccurrencesOf(response,accStatusExitApply));


        SDKADD = SDKURL1;     //SDK设置为盟主1 SDK
        response = sygt.SSMemberExitApprove(code3, false);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //不可以重复审批通过
        assertEquals("404", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        SDKADD = SDKURL2;     //SDK设置为盟主2 SDK
        response = sygt.SSMemberExitApprove(code3, false);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //不可以重复审批通过
        assertEquals("404", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));


        SDKADD = SDKURL1;     //SDK设置为盟主1 SDK
        response = sygt.SSMemberExitApprove(code3, true);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //不可以重复审批通过
        assertEquals("404", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        SDKADD = SDKURL2;     //SDK设置为盟主2 SDK
        response = sygt.SSMemberExitApprove(code3, true);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //不可以重复审批通过
        assertEquals("404", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

    }


    /**
     * 成员申请加入 查询成员列表 检查审批列表 盟主审批通过 再次盟主审批通过 查询成员列表 检查审批列表 再次盟主审批拒绝
     * @throws Exception
     */
    @Test
    public void TC41_MemberJoinPassRePassReject() throws Exception {

        SDKADD = SDKURL1;     //SDK设置为盟主1 SDK
        //初始确认无待审批列表
        String response = sygt.SSPendingApplyGet();
        assertEquals(0, StringUtils.countOccurrencesOf(response,code3));
        int joinNum = StringUtils.countOccurrencesOf(response,accStatusJoinReview);
        int exitNum = StringUtils.countOccurrencesOf(response,accStatusExitApply);

        SDKADD = SDKURLm1;     //SDK设置为成员SDK
        //提交成员加入申请
        response = sygt.SSMemberJoinApply(code3,name3,endPoint3,account3);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        SDKADD = SDKURL1;     //SDK设置为盟主1 SDK
        //检查成员列表 方式1
        response = sygt.SSMembersGet();
        sygtCF.checkMemberInfo(response,code3,name3,endPoint3,account3,accStatusJoinApply,false,"");

        //获取审批列表
        response = sygt.SSPendingApplyGet();
        assertEquals(1, StringUtils.countOccurrencesOf(response,code3));
        assertEquals(joinNum + 1, StringUtils.countOccurrencesOf(response,accStatusJoinReview));
        assertEquals(exitNum, StringUtils.countOccurrencesOf(response,accStatusExitApply));

        //盟主审批通过
        sygtCF.JoinApproveTwoLeaders(code3,true,true);

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account3, contributePointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }
        //再次审批通过
        SDKADD = SDKURL1;     //SDK设置为盟主1 SDK
        response = sygt.SSMemberJoinApprove(code3, true);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //当前可以重复审批通过
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        SDKADD = SDKURL2;     //SDK设置为盟主2 SDK
        response = sygt.SSMemberJoinApprove(code3, true);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //当前可以重复审批通过
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //检查成员列表 方式1
        response = sygt.SSMembersGet();
        sygtCF.checkMemberInfo(response,code3,name3,endPoint3,account3,accStatusJoinSuccess,false,"");

        //获取审批列表
        response = sygt.SSPendingApplyGet();
        assertEquals(0, StringUtils.countOccurrencesOf(response,code3));
        assertEquals(joinNum, StringUtils.countOccurrencesOf(response,accStatusJoinReview));
        assertEquals(exitNum, StringUtils.countOccurrencesOf(response,accStatusExitApply));

        //再次盟主审批拒绝
        SDKADD = SDKURL1;     //SDK设置为盟主1 SDK
        response = sygt.SSMemberJoinApprove(code3, false);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //不可以重复审批通过
        assertEquals("404", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        SDKADD = SDKURL2;     //SDK设置为盟主2 SDK
        response = sygt.SSMemberJoinApprove(code3, false);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //不可以重复审批通过
        assertEquals("404", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));


        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account3, contributePointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
        }
    }

    /**
     * 成员申请加入 查询成员列表 检查审批列表 盟主审批拒绝 再次盟主审批拒绝/通过 查询成员列表 检查审批列表
     * @throws Exception
     */
    @Test
    public void TC42_MemberJoinRejectReReject() throws Exception {

        SDKADD = SDKURL1;     //SDK设置为盟主1 SDK
        //初始确认无待审批列表
        String response = sygt.SSPendingApplyGet();
        assertEquals(0, StringUtils.countOccurrencesOf(response,code3));
        int joinNum = StringUtils.countOccurrencesOf(response,accStatusJoinReview);
        int exitNum = StringUtils.countOccurrencesOf(response,accStatusExitApply);

        SDKADD = SDKURLm1;     //SDK设置为成员SDK
        //提交成员加入申请
        response = sygt.SSMemberJoinApply(code3,name3,endPoint3,account3);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        SDKADD = SDKURL1;     //SDK设置为盟主1 SDK
        //检查成员列表 方式1
        response = sygt.SSMembersGet();
        sygtCF.checkMemberInfo(response,code3,name3,endPoint3,account3,accStatusJoinApply,false,"");

        //获取审批列表
        response = sygt.SSPendingApplyGet();
        assertEquals(1, StringUtils.countOccurrencesOf(response,code3));
        assertEquals(joinNum + 1, StringUtils.countOccurrencesOf(response,accStatusJoinReview));
        assertEquals(exitNum, StringUtils.countOccurrencesOf(response,accStatusExitApply));

        //盟主审批拒绝
        sygtCF.JoinApproveTwoLeaders(code3,false,false);

        SDKADD = SDKURL1;     //SDK设置为盟主1 SDK
        response = sygt.SSMemberJoinApprove(code3, false);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //不可以重复审批
        assertEquals("404", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        SDKADD = SDKURL2;     //SDK设置为盟主2 SDK
        response = sygt.SSMemberJoinApprove(code3, false);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //不可以重复审批通过
        assertEquals("404", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //检查成员列表 方式1
        response = sygt.SSMembersGet();
        assertEquals(false, response.contains(code3));


        //获取审批列表
        response = sygt.SSPendingApplyGet();
        assertEquals(0, StringUtils.countOccurrencesOf(response,code3));
        assertEquals(joinNum, StringUtils.countOccurrencesOf(response,accStatusJoinReview));
        assertEquals(exitNum, StringUtils.countOccurrencesOf(response,accStatusExitApply));

        //再次盟主审批通过
        SDKADD = SDKURL1;     //SDK设置为盟主1 SDK
        response = sygt.SSMemberJoinApprove(code3, true);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //不可以重复审批通过
        assertEquals("404", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        SDKADD = SDKURL2;     //SDK设置为盟主2 SDK
        response = sygt.SSMemberJoinApprove(code3, true);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //不可以重复审批通过
        assertEquals("404", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));


        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account3, contributePointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));

            response = sygt.SSPointQuery(account3, contributePointType);
            assertEquals("400", JSONObject.fromObject(response).getString("state"));
        }
    }


    /**
     * 成员申请加入 成员审批
     * @throws Exception
     */
    @Test
    public void TC43_MemberJoinApplyMemberPass() throws Exception {

        SDKADD = SDKURL1;     //SDK设置为盟主1 SDK
        //初始确认无待审批列表
        String response = sygt.SSPendingApplyGet();
        assertEquals(0, StringUtils.countOccurrencesOf(response,code3));
        int joinNum = StringUtils.countOccurrencesOf(response,accStatusJoinReview);
        int exitNum = StringUtils.countOccurrencesOf(response,accStatusExitApply);

        SDKADD = SDKURLm1;     //SDK设置为成员SDK
        //提交加入申请
        response = sygt.SSMemberJoinApply(code3,name3,endPoint3,account3);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //检查成员列表 方式1
        response = sygt.SSMembersGet();
        sygtCF.checkMemberInfo(response,code3,name3,endPoint3,account3,accStatusJoinApply,false,"");

        //获取审批列表
        response = sygt.SSPendingApplyGet();
        assertEquals(1, StringUtils.countOccurrencesOf(response,code3));
        assertEquals(joinNum + 1, StringUtils.countOccurrencesOf(response,accStatusJoinReview));
        assertEquals(exitNum, StringUtils.countOccurrencesOf(response,accStatusExitApply));


        //成员审批通过
        response = sygt.SSMemberExitApprove(code3,true);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        //链上应该无可审批信息
        assertEquals("404", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //成员审批拒绝
        response = sygt.SSMemberExitApprove(code3,false);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        //链上应该报错
        assertEquals("404", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //检查成员列表 方式1
        response = sygt.SSMembersGet();
        sygtCF.checkMemberInfo(response,code3,name3,endPoint3,account3,accStatusJoinApply,false,"");


        //获取审批列表  成员应该只能获取自己的待审批列表
        response = sygt.SSPendingApplyGet();
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(1, StringUtils.countOccurrencesOf(response,code3));
        assertEquals(joinNum + 1, StringUtils.countOccurrencesOf(response,accStatusJoinReview));
        assertEquals(exitNum, StringUtils.countOccurrencesOf(response,accStatusExitApply));

        SDKADD = SDKURL1; //设置为盟主SDK
        //盟主审批通过
        sygtCF.JoinApproveTwoLeaders(code3,true,true);

        //检查成员列表 方式1
        response = sygt.SSMembersGet();
        sygtCF.checkMemberInfo(response,code3,name3,endPoint3,account3,accStatusJoinSuccess,false,"");

        //获取审批列表
        response = sygt.SSPendingApplyGet();
        assertEquals(0, StringUtils.countOccurrencesOf(response,code3));
        assertEquals(joinNum, StringUtils.countOccurrencesOf(response,accStatusJoinReview));
        assertEquals(exitNum, StringUtils.countOccurrencesOf(response,accStatusExitApply));

        //获取积分
        if(bCheckPoint) {
            response = sygt.SSPointQuery(account3, contributePointType);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(memberJoinPoint, JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));
            response = sygt.SSPointQuery(account3, contributePointType);
            assertEquals("400", JSONObject.fromObject(response).getString("state"));
        }
    }

    /**
     * 成员加入后 成员提交成员退出申请
     * @throws Exception
     */
    @Test
    public void TC44_MemberExitApply() throws Exception {

        SDKADD = SDKURL1;     //SDK设置为盟主1 SDK
        //初始确认无待审批列表
        String response = sygt.SSPendingApplyGet();
        assertEquals(false, response.contains("join"));
        assertEquals(false, response.contains("exit"));
        int joinNum = StringUtils.countOccurrencesOf(response,accStatusJoinReview);
        int exitNum = StringUtils.countOccurrencesOf(response,accStatusExitApply);

        SDKADD = SDKURLm1;     //SDK设置为成员SDK
        //提交加入申请
        response = sygt.SSMemberJoinApply(code3,name3,endPoint3,account3);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //盟主审批通过
        sygtCF.JoinApproveTwoLeaders(code3,true,true);

        //检查成员列表 方式1
        response = sygt.SSMembersGet();
        assertEquals(true, response.contains(code3));

        //成员提交成员退出申请 成员无法提交
        SDKADD = SDKURLm1;    //设置为成员SDK
        response = sygt.SSMemberExitApply(code3,desc);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("404", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        //检查成员列表 方式1
        response = sygt.SSMembersGet();
        sygtCF.checkMemberInfo(response,code3,name3,endPoint3,account3,accStatusJoinSuccess,false,"");

        //获取审批列表
        response = sygt.SSPendingApplyGet();
        assertEquals(0, StringUtils.countOccurrencesOf(response,code3));
        assertEquals(joinNum, StringUtils.countOccurrencesOf(response,accStatusJoinReview));
        assertEquals(exitNum, StringUtils.countOccurrencesOf(response,accStatusExitApply));

    }


    /**
     * 盟主提交退出自己  无法上链 当前不支持退出自己
     * 没有盟主退出这个说法
     * @throws Exception
     */
    @Test
    public void TC45_LeaderExitApply() throws Exception {

        SDKADD = SDKURL1;     //SDK设置为盟主1 SDK
        //初始确认无待审批列表
        String response = sygt.SSPendingApplyGet();
        assertEquals(0, StringUtils.countOccurrencesOf(response,code3));

        //盟主提交退出自己申请
        response = sygt.SSMemberExitApply(code1,desc);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        //不允许
        assertEquals("404", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));
    }
}
