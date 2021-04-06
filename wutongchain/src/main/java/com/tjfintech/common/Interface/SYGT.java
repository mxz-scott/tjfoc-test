package com.tjfintech.common.Interface;

import java.util.List;
import java.util.Map;

public interface SYGT {

    String SSMemberJoinApply(String code, String name, String serviceEndpoint, String account,String publicKey);//提交成员加入申请
    String SSMemberJoinApprove(String code, Boolean isAgree);//成员加入申请审核
    String SSMemberExitApply(String code, String isAgree,String serviceEndpoint);//提交成员退出申请
    String SSMemberExitApprove(String code, Boolean isAgree);//成员退出申请审核
    String SSMembersGet(String code);//获取成员列表
    String SSPendingApplyGet();//获取待处理的成员申请列表
}
