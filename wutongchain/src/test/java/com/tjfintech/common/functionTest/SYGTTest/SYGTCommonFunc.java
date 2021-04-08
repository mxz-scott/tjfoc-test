package com.tjfintech.common.functionTest.SYGTTest;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.SYGT;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.util.StringUtils;

import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassSYGT.*;
import static com.tjfintech.common.utils.UtilsClassSYGT.code1;
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
        assertEquals(200, JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        SDKADD = SDKURL2;     //SDK设置为盟主2 SDK
        response = sygt.SSMemberJoinApprove(code, bAgree2);
        if (bAgree1) {
            assertEquals(200, JSONObject.fromObject(response).getString("state"));
            commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                    utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        }
        else assertEquals(400, JSONObject.fromObject(response).getString("state"));

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
        assertEquals(200, JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        SDKADD = SDKURL2;     //SDK设置为盟主2 SDK
        response = sygt.SSMemberExitApprove(code, bAgree2);
        if (bAgree1) {
            assertEquals(200, JSONObject.fromObject(response).getString("state"));
            commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                    utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        }
        else assertEquals(400, JSONObject.fromObject(response).getString("state"));

        return bAgree1 && bAgree2;
    }

    /**
     * 整合成员退出申请及审批通过
     * @param code  成员机构代码
     * @param name  成员机构名称
     * @param endPoint 成员隐私服务接入点
     */
    public void memberJoin(String code,String name,String endPoint)throws Exception{
        SDKADD = SDKURL1;     //SDK设置为盟主1 SDK
        //初始确认无待审批列表
        String response = sygt.SSPendingApplyGet();
        assertEquals(false, response.contains("join"));
        assertEquals(false, response.contains("exit"));
        assertEquals(0, StringUtils.countOccurrencesOf(response,"\"code\":\"" + code + "\""));
        int joinNum = StringUtils.countOccurrencesOf(response,"\"join\"");
        int exitNum = StringUtils.countOccurrencesOf(response,"\"exit\"");


        SDKADD = SDKURLm1;     //SDK设置为成员SDK
        //提交成员加入申请
        response = sygt.SSMemberJoinApply(code,name,endPoint,account1);
        assertEquals(200, JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);


        SDKADD = SDKURL1;     //SDK设置为盟主1 SDK
        //检查成员列表 方式1
        response = sygt.SSMembersGet("");
        assertEquals(false, response.contains(code));

        //检查成员列表 方式2
        response = sygt.SSMembersGet(code);
        assertEquals(false, response.contains(code));

        //获取审批列表
        response = sygt.SSPendingApplyGet();
        assertEquals(1, StringUtils.countOccurrencesOf(response,"\"code\":\"" + code + "\""));
        assertEquals(joinNum + 1, StringUtils.countOccurrencesOf(response,"\"join\""));
        assertEquals(exitNum, StringUtils.countOccurrencesOf(response,"\"exit\""));

        //盟主审批通过
        JoinApproveTwoLeaders(code,true,true);

        //检查成员列表 方式1
        response = sygt.SSMembersGet("");
        assertEquals(true, response.contains(code));

        //检查成员列表 方式2
        response = sygt.SSMembersGet(code);
        assertEquals(true, response.contains(code));

        //获取审批列表
        response = sygt.SSPendingApplyGet();
        assertEquals(0, StringUtils.countOccurrencesOf(response,"\"code\":\"" + code + "\""));
        assertEquals(joinNum, StringUtils.countOccurrencesOf(response,"\"join\""));
        assertEquals(exitNum, StringUtils.countOccurrencesOf(response,"\"exit\""));
    }

    /**
     * 整合成员退出申请及审批通过
     * @param code  成员机构代码
     * @param name  成员机构名称
     * @param endPoint 成员隐私服务接入点
     */
    public void memberExit(String code,String name,String endPoint)throws Exception{
        //初始确认无待审批列表
        String response = sygt.SSPendingApplyGet();
        assertEquals(false, response.contains("join"));
        assertEquals(false, response.contains("exit"));
        assertEquals(0, StringUtils.countOccurrencesOf(response,"\"code\":\"" + code + "\""));
        int joinNum = StringUtils.countOccurrencesOf(response,"\"join\"");
        int exitNum = StringUtils.countOccurrencesOf(response,"\"exit\"");
        //盟主退出申请

        response = sygt.SSMemberExitApply(code,name,endPoint);
        assertEquals(200, JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //获取审批列表
        response = sygt.SSPendingApplyGet();
        assertEquals(1, StringUtils.countOccurrencesOf(response,"\"code\":\"" + code + "\""));
        assertEquals(joinNum, StringUtils.countOccurrencesOf(response,"\"join\""));
        assertEquals(exitNum + 1, StringUtils.countOccurrencesOf(response,"\"exit\""));

        //检查成员列表 方式1
        response = sygt.SSMembersGet("");
        assertEquals(true, response.contains(code));

        //检查成员列表 方式2
        response = sygt.SSMembersGet(code);
        assertEquals(true, response.contains(code));


        //退出盟主审批通过
        ExitApproveTwoLeaders(code,true,true);

        //获取审批列表
        response = sygt.SSPendingApplyGet();
        assertEquals(0, StringUtils.countOccurrencesOf(response,"\"code\":\"" + code + "\""));
        assertEquals(joinNum, StringUtils.countOccurrencesOf(response,"\"join\""));
        assertEquals(exitNum, StringUtils.countOccurrencesOf(response,"\"exit\""));

        //检查成员列表 方式1
        response = sygt.SSMembersGet("");
        assertEquals(false, response.contains(code));

        //检查成员列表 方式2
        response = sygt.SSMembersGet(code);
        assertEquals(false, response.contains(code));
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

}
