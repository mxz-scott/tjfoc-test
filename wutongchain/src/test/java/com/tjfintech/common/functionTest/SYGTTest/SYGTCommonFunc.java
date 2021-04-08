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
    public Boolean JoinApproveTwoLeaders(String code,Boolean bAgree1,Boolean bAgree2) throws Exception {
        SDKADD = SDKURL1;     //SDK设置为盟主1 SDK
        String response = sygt.SSMemberJoinApprove(code, bAgree1);
        assertEquals(200, JSONObject.fromObject(response).getString("state"));

        SDKADD = SDKURL2;     //SDK设置为盟主2 SDK
        response = sygt.SSMemberJoinApprove(code, bAgree2);
        if (bAgree1) assertEquals(200, JSONObject.fromObject(response).getString("state"));
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
    public Boolean ExitApproveTwoLeaders(String code,Boolean bAgree1,Boolean bAgree2) throws Exception {
        SDKADD = SDKURL1;     //SDK设置为盟主1 SDK
        String response = sygt.SSMemberExitApprove(code, bAgree1);
        assertEquals(200, JSONObject.fromObject(response).getString("state"));

        SDKADD = SDKURL2;     //SDK设置为盟主2 SDK
        response = sygt.SSMemberExitApprove(code, bAgree2);
        if (bAgree1) assertEquals(200, JSONObject.fromObject(response).getString("state"));
        else assertEquals(400, JSONObject.fromObject(response).getString("state"));

        return bAgree1 && bAgree2;
    }

}
