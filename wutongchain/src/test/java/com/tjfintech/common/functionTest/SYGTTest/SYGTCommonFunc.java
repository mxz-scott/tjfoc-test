package com.tjfintech.common.functionTest.SYGTTest;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.SYGT;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import org.springframework.util.StringUtils;

import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassSYGT.*;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class SYGTCommonFunc {

    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    SYGT sygt = testBuilder.getSygt();


    /***
     * 双盟主审批成员加入
     * @param code  申请加入成员机构代码
     * @param bAgree1  盟主1是否同意
     * @param bAgree2  盟主2是否同意
     * @throws Exception
     */
    public Boolean JoinApproveTwoLeaders(String code,Boolean bAgree1,Boolean bAgree2)throws Exception{
        SDKADD = SDKURL1;     //SDK设置为盟主1 SDK
        String response = sygt.SSMemberJoinApprove(code, bAgree1);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));

        SDKADD = SDKURL2;     //SDK设置为盟主2 SDK
        response = sygt.SSMemberJoinApprove(code, bAgree2);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        if (bAgree1) {
            assertEquals("200", JSONObject.fromObject(
                    store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));
        }
        else {
            assertEquals("404", JSONObject.fromObject(
                    store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));
        }

        return bAgree1 && bAgree2;
    }

    /***
     * 双盟主审批成员退出
     * @param code  退出成员机构代码
     * @param bAgree1  盟主1是否同意
     * @param bAgree2  盟主2是否同意
     * @throws Exception
     */
    public Boolean ExitApproveTwoLeaders(String code,Boolean bAgree1,Boolean bAgree2)throws Exception{
        SDKADD = SDKURL1;     //SDK设置为盟主1 SDK
        String response = sygt.SSMemberExitApprove(code, bAgree1);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        SDKADD = SDKURL2;     //SDK设置为盟主2 SDK
        response = sygt.SSMemberExitApprove(code, bAgree2);
        if (bAgree1) {
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                    utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        }
        else assertEquals(400, JSONObject.fromObject(response).getString("state"));

        return bAgree1 && bAgree2;
    }

    /***
     * 双盟主审批成员退出
     * @param sdkurl  退出成员机构代码
     * @param code  退出成员机构代码
     * @param bAgree  盟主是否同意
     * @throws Exception
     */
    public void ExitApprove(String sdkurl,String code,Boolean bAgree)throws Exception{
        SDKADD = sdkurl;
        String response = sygt.SSMemberExitApprove(code, bAgree);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20))).getString("state"));
    }

    /**
     * 整合成员退出申请及审批通过
     * @param code  成员机构代码
     * @param name  成员机构名称
     * @param endPoint 成员隐私服务接入点
     */
    public void memberJoin(String code,String name,String endPoint,String account)throws Exception{
        SDKADD = SDKURL1;     //SDK设置为盟主1 SDK
        //初始确认无待审批列表
        String response = sygt.SSPendingApplyGet();
//        assertEquals(false, response.contains("join"));
//        assertEquals(false, response.contains("exit"));
        assertEquals(0, StringUtils.countOccurrencesOf(response,code));
        int joinNum = StringUtils.countOccurrencesOf(response,accStatusJoinReview);
        int exitNum = StringUtils.countOccurrencesOf(response,accStatusExitApply);


        SDKADD = SDKURLm1;     //SDK设置为成员SDK
        //提交成员加入申请
        response = sygt.SSMemberJoinApply(code,name,endPoint,account);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);


        SDKADD = SDKURL1;     //SDK设置为盟主1 SDK
        //检查成员列表 方式1
        response = sygt.SSMembersGet();
        checkMemberInfo(response,code,name,endPoint,account,accStatusJoinApply,false,"");

        //检查成员列表 方式2
        response = sygt.SSMembersGet();
        checkMemberInfo(response,code,name,endPoint,account,accStatusJoinApply,false,"");

        //获取审批列表
        response = sygt.SSPendingApplyGet();
//        assertEquals(1, StringUtils.countOccurrencesOf(response,"\"code\":\"" + code + "\""));
        assertEquals(joinNum + 1, StringUtils.countOccurrencesOf(response,accStatusJoinReview));
        assertEquals(exitNum, StringUtils.countOccurrencesOf(response,accStatusExitApply));

        //盟主审批通过
        JoinApproveTwoLeaders(code,true,true);

        //检查成员列表 方式1
        response = sygt.SSMembersGet();
        checkMemberInfo(response,code,name,endPoint,account,accStatusJoinSuccess,false,"");

        //检查成员列表 方式2
        response = sygt.SSMembersGet();
        checkMemberInfo(response,code,name,endPoint,account,accStatusJoinSuccess,false,"");

        //获取审批列表
        response = sygt.SSPendingApplyGet();
//        assertEquals(0, StringUtils.countOccurrencesOf(response,"\"code\":\"" + code + "\""));
        assertEquals(joinNum, StringUtils.countOccurrencesOf(response,accStatusJoinReview));
        assertEquals(exitNum, StringUtils.countOccurrencesOf(response,accStatusExitApply));
    }

    /**
     * 整合成员退出申请及审批通过
     * @param code  成员机构代码
     * @param desc  退出原因
     */
    public void memberExit(String code,String desc)throws Exception{
        SDKADD = SDKURL1;
        //盟主退出申请
        String response = sygt.SSMemberExitApply(code,desc);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        String tempState = JSONObject.fromObject(
                store.GetTxDetail(commonFunc.getTxHash(response,utilsClass.sdkGetTxHashType20))).getString("state");
        if(tempState == "404") return;

        //退出盟主审批通过
        ExitApprove(SDKURL2,code,true);
    }

    public void checkAsset(String response,String assetID,int amount,String desc,String code,String name,String account,String endPoint){
        assertEquals(true, response.contains(assetID));

        JSONObject jsonObject = new JSONObject();
        for(int i=0;i<JSONObject.fromObject(response).getJSONArray("data").size();i++){
            if(JSONObject.fromObject(response).getJSONArray("data").get(i).toString().contains(assetID)){
                jsonObject = JSONObject.fromObject(JSONObject.fromObject(response).getJSONArray("data").get(i).toString());
            }
        }
        assertEquals(assetID,jsonObject.getString("assetID"));
        assertEquals(code,jsonObject.getJSONObject("owner").getString("code"));
        assertEquals(name,jsonObject.getJSONObject("owner").getString("name"));
        assertEquals(account,jsonObject.getJSONObject("owner").getString("account"));
        assertEquals(endPoint,jsonObject.getJSONObject("owner").getString("serviceEndpoint"));
        assertEquals(desc,jsonObject.getString("desc"));
        assertEquals(amount,jsonObject.getInt("amount"));
    }

    public void checkMemberInfo(String response,String code,String name,String serviceEndpoint,String account,String status,Boolean isLeader,String jointime){
        assertEquals(true, response.contains(code));

        JSONObject jsonObject = new JSONObject();
        for(int i=0;i<JSONObject.fromObject(response).getJSONArray("data").size();i++){
            if(JSONObject.fromObject(response).getJSONArray("data").get(i).toString().contains(code)){
                jsonObject = JSONObject.fromObject(JSONObject.fromObject(response).getJSONArray("data").get(i).toString());
            }
        }

        assertEquals(code,jsonObject.getString("code"));
        assertEquals(name,jsonObject.getString("name"));
        assertEquals(serviceEndpoint,jsonObject.getString("serviceEndpoint"));
        assertEquals(account,jsonObject.getString("account"));
        assertEquals(status,jsonObject.getString("status"));
        assertEquals(isLeader,jsonObject.getBoolean("isLeader"));
    }

    public void checkApplyInfo(String response,String code,String name,String serviceEndpoint,String account,String status,String approveAccount){
        assertEquals(true, response.contains(code));

        JSONObject jsonObject = new JSONObject();
        for(int i=0;i<JSONObject.fromObject(response).getJSONArray("data").size();i++){
            if(JSONObject.fromObject(response).getJSONArray("data").get(i).toString().contains(code)){
                jsonObject = JSONObject.fromObject(JSONObject.fromObject(response).getJSONArray("data").get(i).toString());
            }
        }

        assertEquals(code,jsonObject.getString("code"));
        assertEquals(name,jsonObject.getString("name"));
        assertEquals(serviceEndpoint,jsonObject.getString("serviceEndpoint"));
        assertEquals(account,jsonObject.getString("account"));
        assertEquals(status,jsonObject.getString("status"));
        assertEquals(approveAccount,jsonObject.getString("approveAccount"));
    }

    public void checkMemberInfo(String response, Map mapMem){
        assertEquals(true, response.contains(mapMem.get("code").toString()));

        JSONObject jsonObject = new JSONObject();
        for(int i=0;i<JSONObject.fromObject(response).getJSONArray("data").size();i++){
            if(JSONObject.fromObject(response).getJSONArray("data").get(i).toString().contains(mapMem.get("code").toString())){
                jsonObject = JSONObject.fromObject(JSONObject.fromObject(response).getJSONArray("data").get(i).toString());
            }
        }

        assertEquals(mapMem.get("code").toString(),jsonObject.getString("code"));
        assertEquals(mapMem.get("name").toString(),jsonObject.getString("name"));
        assertEquals(mapMem.get("serviceEndpoint").toString(),jsonObject.getString("serviceEndpoint"));
        assertEquals(mapMem.get("account").toString(),jsonObject.getString("account"));
        assertEquals(mapMem.get("status").toString(),jsonObject.getString("status"));
        assertEquals(mapMem.get("isLeader").toString(),jsonObject.getString("isLeader"));
//        assertEquals(mapMem.get("joinApplyApproveDate").toString(),jsonObject.getString("joinApplyApproveDate"));
        assertEquals(mapMem.get("isSelf").toString(),jsonObject.getString("isSelf"));
        assertEquals(mapMem.get("joinCheckDetail").toString(),jsonObject.getString("joinCheckDetail"));
        assertEquals(mapMem.get("exitCheckDetail").toString(),jsonObject.getString("exitCheckDetail"));
    }

    public void checkAssetAuth(String assertID,String account,Boolean bValid){
        //查看授权情况
        String response = sygt.SSAssetVeriryAuthority(assertID,account);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(bValid, JSONObject.fromObject(response).getJSONObject("data").getBoolean("isValid"));

    }

    public void checkMemInfoInAllSDK(String code,String name,String endPoint,String account,String status,Boolean isLeader,
                                    String joinDate,String joinCheckDetail,String exitCheckDetail,
                                     Boolean isSelf1,Boolean isSelf2,Boolean isSelf3){
        SDKADD = SDKURL1;
        String response = sygt.SSMembersGet();
        mapMem.clear();
        mapMem.put("code",code);
        mapMem.put("name",name);
        mapMem.put("serviceEndpoint",endPoint);
        mapMem.put("account",account);
        mapMem.put("status",status);
        mapMem.put("isLeader",isLeader);
        mapMem.put("joinApplyApproveDate",joinDate);
        mapMem.put("isSelf",isSelf1);
        mapMem.put("joinCheckDetail",joinCheckDetail);
        mapMem.put("exitCheckDetail",exitCheckDetail);
        checkMemberInfo(response,mapMem);

        SDKADD = SDKURL2;
        response = sygt.SSMembersGet();
        mapMem.clear();
        mapMem.put("code",code);
        mapMem.put("name",name);
        mapMem.put("serviceEndpoint",endPoint);
        mapMem.put("account",account);
        mapMem.put("status",status);
        mapMem.put("isLeader",isLeader);
        mapMem.put("joinApplyApproveDate",joinDate);
        mapMem.put("isSelf",isSelf2);
        mapMem.put("joinCheckDetail",joinCheckDetail);
        mapMem.put("exitCheckDetail",exitCheckDetail);
        checkMemberInfo(response,mapMem);

        SDKADD = SDKURLm1;
        response = sygt.SSMembersGet();
        mapMem.clear();
        mapMem.put("code",code);
        mapMem.put("name",name);
        mapMem.put("serviceEndpoint",endPoint);
        mapMem.put("account",account);
        mapMem.put("status",status);
        mapMem.put("isLeader",isLeader);
        mapMem.put("joinApplyApproveDate",joinDate);
        mapMem.put("isSelf",isSelf3);
        mapMem.put("joinCheckDetail",joinCheckDetail);
        mapMem.put("exitCheckDetail",exitCheckDetail);
        checkMemberInfo(response,mapMem);
    }

    public void checkJoinApplyInfoInAllSDK(String code,String name,String endPoint,String account,String approveAccount,
                                           int joinPendingNum,int exitPendingNum){
        SDKADD = SDKURL1;     //SDK设置为盟主1 SDK
        //获取审批列表
        String response = sygt.SSPendingApplyGet();
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(1, StringUtils.countOccurrencesOf(response,code));
        assertEquals(joinPendingNum + 1, StringUtils.countOccurrencesOf(response,accStatusJoinReview));
        assertEquals(exitPendingNum, StringUtils.countOccurrencesOf(response,accStatusExitApply));
        checkApplyInfo(response,code,name,endPoint,account,accStatusJoinReview,approveAccount);

        SDKADD = SDKURL2;     //SDK设置为盟主2 SDK
        //获取审批列表
        response = sygt.SSPendingApplyGet();
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(1, StringUtils.countOccurrencesOf(response,code));
        assertEquals(joinPendingNum + 1, StringUtils.countOccurrencesOf(response,accStatusJoinReview));
        assertEquals(exitPendingNum, StringUtils.countOccurrencesOf(response,accStatusExitApply));
        checkApplyInfo(response,code,name,endPoint,account,accStatusJoinReview,approveAccount);

        SDKADD = SDKURLm1;     //SDK设置为成员SDK
        //获取审批列表
        response = sygt.SSPendingApplyGet();
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(1, StringUtils.countOccurrencesOf(response,code));
        assertEquals(joinPendingNum + 1, StringUtils.countOccurrencesOf(response,accStatusJoinReview));
        assertEquals(exitPendingNum, StringUtils.countOccurrencesOf(response,accStatusExitApply));
        checkApplyInfo(response,code,name,endPoint,account,accStatusJoinReview,approveAccount);
    }

    public void checkNoApplyInfoInAllSDK(String code,int joinPendingNum,int exitPendingNum){
        SDKADD = SDKURL1;     //SDK设置为盟主1 SDK
        //获取审批列表
        String response = sygt.SSPendingApplyGet();
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(0, StringUtils.countOccurrencesOf(response,code));
        assertEquals(joinPendingNum, StringUtils.countOccurrencesOf(response,accStatusJoinReview));
        assertEquals(exitPendingNum, StringUtils.countOccurrencesOf(response,accStatusExitApply));
        assertEquals(false, response.contains(code));


        SDKADD = SDKURL2;     //SDK设置为盟主2 SDK
        //获取审批列表
        response = sygt.SSPendingApplyGet();
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(0, StringUtils.countOccurrencesOf(response,code));
        assertEquals(joinPendingNum, StringUtils.countOccurrencesOf(response,accStatusJoinReview));
        assertEquals(exitPendingNum, StringUtils.countOccurrencesOf(response,accStatusExitApply));
        assertEquals(false, response.contains(code));

        SDKADD = SDKURLm1;     //SDK设置为成员SDK
        //获取审批列表
        response = sygt.SSPendingApplyGet();
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(0, StringUtils.countOccurrencesOf(response,code));
        assertEquals(joinPendingNum, StringUtils.countOccurrencesOf(response,accStatusJoinReview));
        assertEquals(exitPendingNum, StringUtils.countOccurrencesOf(response,accStatusExitApply));
        assertEquals(false, response.contains(code));
    }

    public void checkExitApplyInfoInAllSDK(String code,String name,String endPoint,String account,String approveAccount,
                                           int joinPendingNum,int exitPendingNum){
        SDKADD = SDKURL1;     //SDK设置为盟主1 SDK
        //获取审批列表
        String response = sygt.SSPendingApplyGet();
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(1, StringUtils.countOccurrencesOf(response,code));
        assertEquals(joinPendingNum, StringUtils.countOccurrencesOf(response,accStatusJoinReview));
        assertEquals(exitPendingNum + 1, StringUtils.countOccurrencesOf(response,accStatusExitApply));
        checkApplyInfo(response,code,name,endPoint,account,accStatusExitApply,approveAccount);


        SDKADD = SDKURL2;     //SDK设置为盟主2 SDK
        //获取审批列表
        response = sygt.SSPendingApplyGet();
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(1, StringUtils.countOccurrencesOf(response,code));
        assertEquals(joinPendingNum, StringUtils.countOccurrencesOf(response,accStatusJoinReview));
        assertEquals(exitPendingNum + 1, StringUtils.countOccurrencesOf(response,accStatusExitApply));
        checkApplyInfo(response,code,name,endPoint,account,accStatusExitApply,approveAccount);

        SDKADD = SDKURLm1;     //SDK设置为成员SDK
        //获取审批列表
        response = sygt.SSPendingApplyGet();
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(1, StringUtils.countOccurrencesOf(response,code));
        assertEquals(joinPendingNum, StringUtils.countOccurrencesOf(response,accStatusJoinReview));
        assertEquals(exitPendingNum + 1, StringUtils.countOccurrencesOf(response,accStatusExitApply));
        checkApplyInfo(response,code,name,endPoint,account,accStatusExitApply,approveAccount);
    }

    public void checkAccPoint(String SDK,String account,int pointPlatform,int pointContribute){
        SDKADD = SDK;
        String response = sygt.SSPointQuery(account,platformPoint);
        assertEquals(pointPlatform,JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));

        response = sygt.SSPointQuery(account, contributePointType);
        assertEquals(pointContribute,JSONObject.fromObject(response).getJSONObject("data").getInt("balance"));

    }
}
