package com.tjfintech.common;

import com.tjfintech.common.Interface.SYGT;
import com.tjfintech.common.utils.GetTest;
import com.tjfintech.common.utils.PostTest;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.Random;
import static com.tjfintech.common.utils.UtilsClass.SDKADD;
import static com.tjfintech.common.utils.UtilsClassGD.*;

@Slf4j
public  class GoSYGT implements SYGT {

    /***
     * 提交成员加入申请
     * @param code 机构代码
     * @param name 机构名称
     * @param serviceEndpoint 隐私服务接入点
     * @param account 账号
     * @param publicKey 机构身份证对应公钥
     * @return
     */
    public String SSMemberJoinApply(String code, String name, String serviceEndpoint, String account,String publicKey){
        Map<String, Object> map = new HashMap<>();
        map.put("code", code);
        map.put("name", name);
        map.put("serviceEndpoint", serviceEndpoint);
        map.put("account", account);
        map.put("publicKey", publicKey);

        String result = PostTest.postMethod(SDKADD + "/v1/alliance/joinapply", map);
        log.info(result);
        return result;
    }

    /**
     * 成员加入申请审核
     * @param code 机构代码
     * @param isAgree 是否同意成员申请，true同意，false拒绝
     * @return
     */
    public String SSMemberJoinApprove(String code, Boolean isAgree){
        Map<String, Object> map = new HashMap<>();
        map.put("code", code);
        map.put("isAgree", isAgree);

        String result = PostTest.postMethod(SDKADD + "/v1/alliance/joinapprove", map);
        log.info(result);
        return result;
    }

    /***
     * 提交成员退出申请
     * @param code 机构代码
     * @param isAgree 机构名称
     * @param serviceEndpoint 隐私服务接入点
     * @return
     */
    public String SSMemberExitApply(String code, String isAgree,String serviceEndpoint){
        Map<String, Object> map = new HashMap<>();
        map.put("code", code);
        map.put("isAgree", isAgree);
        map.put("serviceEndpoint", serviceEndpoint);

        String result = PostTest.postMethod(SDKADD + "/v1/alliance/exitapply", map);
        log.info(result);
        return result;
    }

    /**
     * 成员退出申请审核
     * @param code 机构代码
     * @param isAgree 是否同意删除成员申请，true同意，false拒绝
     * @return
     */
    public String SSMemberExitApprove(String code, Boolean isAgree){
        Map<String, Object> map = new HashMap<>();
        map.put("code", code);
        map.put("isAgree", isAgree);

        String result = PostTest.postMethod(SDKADD + "/v1/alliance/exitapprove", map);
        log.info(result);
        return result;
    }

    /***
     * 获取成员列表
     * @param code 某成员的名称，不填则查询所有成员
     * @return
     */
    public String SSMembersGet(String code){
        Map<String, Object> map = new HashMap<>();
        map.put("code", code);

        String result = PostTest.postMethod(SDKADD + "/v1/alliance/joinapply", map);
        log.info(result);
        return result;
    }

    /**
     * 获取待处理的成员申请列表
     * @return
     */
    public String SSPendingApplyGet(){
        String result = GetTest.doGet2(SDKADD + "/getpendingapply");
        log.info(result);
        return result;
    }
}
