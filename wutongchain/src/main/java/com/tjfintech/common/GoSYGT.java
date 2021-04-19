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
import static com.tjfintech.common.utils.UtilsClass.*;

@Slf4j
public  class GoSYGT implements SYGT {

    /***
     * 提交成员加入申请
     * @param code 机构代码
     * @param name 机构名称
     * @param serviceEndpoint 隐私服务接入点
     * @param account 账号
     * @return
     */
    public String SSMemberJoinApply(String code, String name, String serviceEndpoint, String account){
        Map<String, Object> map = new HashMap<>();
        map.put("code", code);
        map.put("name", name);
        map.put("serviceEndpoint", serviceEndpoint);

        String result = PostTest.postMethod(SDKADD + "/dss/v1/alliance/joinapply?" + SetURLExtParams(""), map);
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

        String result = PostTest.postMethod(SDKADD + "/dss/v1/alliance/joinapprove?" + SetURLExtParams(""), map);
        log.info(result);
        return result;
    }

    /***
     * 提交成员退出申请
     * @param code 机构代码
     * @param desc 退出原因
     * @return
     */
    public String SSMemberExitApply(String code, String desc){
        Map<String, Object> map = new HashMap<>();
        map.put("code", code);
        map.put("desc", desc);

        String result = PostTest.postMethod(SDKADD + "/dss/v1/alliance/exitapply?" + SetURLExtParams(""), map);
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

        String result = PostTest.postMethod(SDKADD + "/dss/v1/alliance/exitapprove?" + SetURLExtParams(""), map);
        log.info(result);
        return result;
    }

    /***
     * 获取成员列表
     * @return
     */
    public String SSMembersGet(){
        Map<String, Object> map = new HashMap<>();
//        map.put("code", code);

        String result = GetTest.doGet2(SDKADD + "/dss/v1/alliance/members?" + SetURLExtParams(""));
        log.info(result);
        return result;
    }

    /**
     * 获取待处理的成员申请列表
     * @return
     */
    public String SSPendingApplyGet(){
//        String result = GetTest.doGet2(SDKADD + "/dss/v1/alliance/members?" + SetURLExtParams(""));
        String result = GetTest.doGet2(SDKADD + "/dss/v1/alliance/getpendingapply?" + SetURLExtParams(""));
        log.info(result);
        return result;
    }

    /**
     * 发布数据资产
     * @param assetID 数据资产唯一标识
     * @param scene 使用场景代码
     * @param label 数据标签代码
     * @param qty 数据资产数量
     * @param desc 简介
     * @return
     */
    public String SSAssetPublish(String assetID,String scene,String label,int qty,String desc){
        Map<String, Object> map = new HashMap<>();
        map.put("assetID", assetID);
        map.put("scene", scene);
        map.put("label", label);
        map.put("qty", qty);
        map.put("desc", desc);

        String result = PostTest.postMethod(SDKADD + "/dss/v1/asset/publish?" + SetURLExtParams(""), map);
        log.info(result);
        return result;
    }

    /**
     * 更新数据资产
     * @param assetID
     * @param scene
     * @param label
     * @param qty
     * @param desc
     * @return
     */
    public String SSAssetUpdate(String assetID,String scene,String label,int qty,String desc){
        Map<String, Object> map = new HashMap<>();
        map.put("assetID", assetID);
        map.put("qty", qty);
        map.put("desc", desc);

        String result = PostTest.postMethod(SDKADD + "/dss/v1/asset/update?" + SetURLExtParams(""), map);
        log.info(result);
        return result;
    }

    /**
     * 下架数据资产
     * @param assetID
     * @return
     */
    public String SSAssetOff(String assetID){
        Map<String, Object> map = new HashMap<>();
        map.put("assetID", assetID);

        String result = PostTest.postMethod(SDKADD + "/dss/v1/asset/off?" + SetURLExtParams(""), map);
        log.info(result);
        return result;
    }

    /**
     * 查询可用资产
     * @param scene
     * @param label
     * @return
     */
    public String SSAssetQuery(String scene,String label){
        Map<String, Object> map = new HashMap<>();
        map.put("scene", scene);
        map.put("label", label);

        String result = PostTest.postMethod(SDKADD + "/dss/v1/asset/query?" + SetURLExtParams(""), map);
        log.info(result);
        return result;
    }

    /***
     * 资产授权
     * @param assetID 数据资产唯一标识
     * @param code 机构代码
     * @param expireStart 授权起始时间
     * @param expireEnd 授权过期时间
     * @return
     */
    public String SSAssetAuthorize(String assetID,String code,String serviceID,String expireStart,String expireEnd){
        Map<String, Object> map = new HashMap<>();
        map.put("assetID", assetID);
        map.put("account", code);
        map.put("serviceID", serviceID);
        map.put("expireStart", expireStart);
        map.put("expireEnd", expireEnd);

        String result = PostTest.postMethod(SDKADD + "/dss/v1/asset/authorize?" + SetURLExtParams(""), map);
        log.info(result);
        return result;
    }

    /**
     * 资产取消授权
     * @param assetID 数据资产唯一标识
     * @param account 机构账户
     * @return
     */
    public String SSAssetCancelAuthority(String assetID,String account){
        Map<String, Object> map = new HashMap<>();
        map.put("assetID", assetID);
        map.put("account", account);

        String result = PostTest.postMethod(SDKADD + "/dss/v1/asset/cancelauthorize?" + SetURLExtParams(""), map);
        log.info(result);
        return result;
    }

    /**
     * 验证查询授权是否有效
     * @param assetID 数据资产唯一标识
     * @param account 查询者id
     * @return
     */
    public String SSAssetVeriryAuthority(String assetID,String account){
        Map<String, Object> map = new HashMap<>();
        map.put("assetID", assetID);
        map.put("account", account);

        String result = PostTest.postMethod(SDKADD + "/dss/v1/asset/verifyauth?" + SetURLExtParams(""), map);
        log.info(result);
        return result;
    }

    /**
     * 更新使用场景和数据标签列表
     * @param scene 场景，全量更新
     * @param label 数据标签，全量更新
     * @return
     */
    public String SSSettingUpdate(List<Map> scene,List<Map> label){
        Map<String, Object> map = new HashMap<>();
        map.put("scenes", scene);
        map.put("labels", label);

        String result = PostTest.postMethod(SDKADD + "/dss/v1/setting/update?" + SetURLExtParams(""), map);
        log.info(result);
        return result;
    }

    /**
     * 获取使用场景和数据标签列表
     * @return
     */
    public String SSSettingGet(){
        String result = GetTest.doGet2(SDKADD + "/dss/v1/setting/list?" + SetURLExtParams(""));
        log.info(result);
        return result;
    }

    /**
     * 增加/扣减积分
     * @param account 被更新积分的账号
     * @param type 积分操作类型:credit为增加,debit为减少
     * @param pointType 积分类型:1为平台积分，2为贡献积分
     * @param code 业务代码
     * @param amount 积分数量
     * @return
     */
    public String SSPointUpdate(String account,String type,int pointType,String code,int amount){
        Map<String, Object> map = new HashMap<>();
        map.put("account", account);
        map.put("type", type);
        map.put("pointType", pointType);
        map.put("code", code);
        map.put("amount", amount);

        String result = PostTest.postMethod(SDKADD + "/dss/v1/point/update?" + SetURLExtParams(""), map);
        log.info(result);
        return result;
    }

    /**
     * 获取积分余额
     * @param account
     * @return
     */
    public String SSPointQuery(String account,int pointType){
        Map<String, Object> map = new HashMap<>();
        map.put("account", account);
        map.put("pointType", pointType);

        String result = PostTest.postMethod(SDKADD + "/dss/v1/point/balance?" + SetURLExtParams(""), map);
        log.info(result);
        return result;
    }

    /**
     * 单笔匿踪查询数据源方添加被查询记录
     * @param requestID 查询请求唯一标识
     * @param partyA 查询发起方
     * @param partyB 查询响应方
//     * @param replyDigest 相应结果Hash摘要
     * @param createdTime 查询发起时间,格式"yyyy-mm-dd hh:mm:ss"
     * @return
     */
    public String SSSingleSafeQueryRequest(String requestID, String partyA, String partyB,String createdTime){
        Map<String, Object> map = new HashMap<>();
        map.put("requestID", requestID);
        map.put("partyA", partyA);
        map.put("partyB", partyB);
        map.put("createdTime", createdTime);

        String result = PostTest.postMethod(SDKADD + "/dss/v1/safequery/single/request?" + SetURLExtParams(""), map);
        log.info(result);
        return result;
    }

    /***
     * 单笔匿踪查询查询发起方添加完整查询结果记录
     * @param requestID 查询请求唯一标识
     * @param respTime 查询响应时间,格式"yyyy-mm-dd hh:mm:ss"
     * @param replyDigest 相应结果Hash摘要
     * @return
     */
    public String SSSingleSafeQueryReply(String requestID, String respTime,String replyDigest){
        Map<String, Object> map = new HashMap<>();
        map.put("requestID", requestID);
        map.put("respTime", respTime);
        map.put("replyDigest", replyDigest);

        String result = PostTest.postMethod(SDKADD + "/dss/v1/safequery/single/reply?" + SetURLExtParams(""), map);
        log.info(result);
        return result;
    }

    /***
     * 单笔匿踪查询查询发起方添加完整查询结果记录
     * @param requestID 查询请求唯一标识
     * @param hit 查询是否命中
     * @param elapsed 查询耗时，单位：毫秒
     * @param errCode 错误码
     * @param metadata 其他查询相关数据信息
     * @param completedTime 查询记录创建时间
     * @return
     */
    public String SSSingleSafeQueryComplete(String requestID, int hit, int elapsed, int errCode,String metadata, String completedTime){
        Map<String, Object> map = new HashMap<>();
        map.put("requestID", requestID);
        map.put("hit", hit);
        map.put("elapsed", elapsed);
        map.put("errCode", errCode);
        map.put("metadata", metadata);
        map.put("completedTime", completedTime);

        String result = PostTest.postMethod(SDKADD + "/dss/v1/safequery/single/complete?" + SetURLExtParams(""), map);
        log.info(result);
        return result;
    }

    /**
     * 批量匿踪查询数据源方添加被查询记录
     * @param requestID 查询请求唯一标识
     * @param partyA 查询发起方
     * @param partyB 查询响应方
     * @param createdTime 查询发起时间,格式"yyyy-mm-dd hh:mm:ss"
     * @return
     */
    public String SSMultiSafeQueryRequest(String requestID, String partyA, String partyB,String createdTime){
        Map<String, Object> map = new HashMap<>();
        map.put("requestID", requestID);
        map.put("partyA", partyA);
        map.put("partyB", partyB);
        map.put("createdTime", createdTime);

        String result = PostTest.postMethod(SDKADD + "/dss/v1/safequery/multi/request?" + SetURLExtParams(""), map);
        log.info(result);
        return result;
    }

    /***
     * 批量匿踪查询查询发起方添加完整查询结果记录
     * @param requestID 查询请求唯一标识
     * @param respTime 查询响应时间,格式"yyyy-mm-dd hh:mm:ss"
     * @param replyDigest 相应结果Hash摘要
     * @return
     */
    public String SSMultiSafeQueryReply(String requestID, String respTime,String replyDigest){
        Map<String, Object> map = new HashMap<>();
        map.put("requestID", requestID);
        map.put("respTime", respTime);
        map.put("replyDigest", replyDigest);

        String result = PostTest.postMethod(SDKADD + "/dss/v1/safequery/multi/reply?" + SetURLExtParams(""), map);
        log.info(result);
        return result;
    }

    /***
     * 批量匿踪查询查询发起方添加完整查询结果记录
     * @param requestID 查询请求唯一标识
     * @param hit 查询是否命中
     * @param elapsed 查询耗时，单位：毫秒
     * @param errCode 错误码
     * @param metadata 其他查询相关数据信息
     * @param completedTime 查询记录创建时间
     * @return
     */
    public String SSMultiSafeQueryComplete(String requestID, int hit, int elapsed, int errCode,String metadata, String completedTime){
        Map<String, Object> map = new HashMap<>();
        map.put("requestID", requestID);
        map.put("hit", hit);
        map.put("elapsed", elapsed);
        map.put("errCode", errCode);
        map.put("metadata", metadata);
        map.put("completedTime", completedTime);

        String result = PostTest.postMethod(SDKADD + "/dss/v1/safequery/multi/complete?" + SetURLExtParams(""), map);
        log.info(result);
        return result;
    }

    /**
     * 匿踪查询
     * @param scene 场景代码
     * @param label 标签代码
     * @param inputs 查询输⼊参数
     * @return
     */
    public String SSSafeQueryDo(String scene, String label, Map<String,String> inputs){
        Map<String, Object> map = new HashMap<>();
        map.put("scene", scene);
        map.put("label", label);
        map.put("inputs", inputs);

        String result = PostTest.postMethod(SDKADD + "/dss/v1/safequery/do?" + SetURLExtParams(""), map);
        log.info(result);
        return result;
    }
}
