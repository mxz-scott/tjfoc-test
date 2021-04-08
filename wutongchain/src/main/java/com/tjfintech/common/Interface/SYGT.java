package com.tjfintech.common.Interface;

import java.util.List;
import java.util.Map;

public interface SYGT {

    String SSMemberJoinApply(String code, String name, String serviceEndpoint, String account);//提交成员加入申请
    String SSMemberJoinApprove(String code, Boolean isAgree);//成员加入申请审核
    String SSMemberExitApply(String code, String name,String serviceEndpoint);//提交成员退出申请
    String SSMemberExitApprove(String code, Boolean isAgree);//成员退出申请审核
    String SSMembersGet(String code);//获取成员列表
    String SSPendingApplyGet();//获取待处理的成员申请列表

    String SSAssetPublish(String assetID,String scene,String label,int amount,String desc); //发布数据资产
    String SSAssetUpdate(String assetID,String scene,String label,int amount,String desc); //更新数据资产
    String SSAssetOff(String assetID); //下架数据资产
    String SSAssetQuery(String scene,String label); //查询可用数据资产

    String SSAssetAuthorize(String assetID,String code,String serviceID,String expireStart,String expireEnd); //资产授权
    String SSAssetCancelAuthority(String assetID,String code); //资产取消授权
    String SSAssetVeriryAuthority(String assetID,String code); //验证查询授权是否有效

    String SSSettingUpdate(List<Map> scene,List<Map> label); //更新使用场景和数据标签列表
    String SSSettingGet(); //获取使用场景和数据标签列表

    String SSPointUpdate(String account,String type,int pointType,String code,int amount); //增加\扣减积分
    String SSPointQuery(String account,int pointType); //获取积分余额

    String SSSingleSafeQueryRequest(String requestID, String partyA, String partyB, String replyDigest,String createdTime); //单笔匿踪查询数据源方添加被查询记录
    String SSSingleSafeQueryReply(String requestID, String respTime,String replyDigest); //单笔匿踪查询查询发起方添加完整查询结果记录
    String SSSingleSafeQueryComplete(String requestID, Boolean hit, int elapsed, int errCode,String metadata, String completedTime); //单笔匿踪查询查询发起方添加完整查询结果记录

    String SSMultiSafeQueryRequest(String requestID, String partyA, String partyB, String replyDigest,String createdTime); //批量匿踪查询数据源方添加被查询记录
    String SSMultiSafeQueryReply(String requestID, String respTime,String replyDigest); //批量匿踪查询查询发起方添加完整查询结果记录
    String SSMultiSafeQueryComplete(String requestID, Boolean hit, int elapsed, int errCode,String metadata, String completedTime); //批量匿踪查询查询发起方添加完整查询结果记录

    String SSSafeQueryDo(String scene, String label, Map<String,String> inputs); //匿踪查询

}
