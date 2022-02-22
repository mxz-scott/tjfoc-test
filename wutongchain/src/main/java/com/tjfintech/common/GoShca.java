package com.tjfintech.common;

import com.tjfintech.common.Interface.Shca;
import com.tjfintech.common.utils.GetTest;
import com.tjfintech.common.utils.PostTest;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.syncFlag;
import static com.tjfintech.common.utils.UtilsClass.syncTimeout;
import static com.tjfintech.common.utils.UtilsClassShca.SHCAADD;
import static com.tjfintech.common.utils.UtilsClassShca.*;

@Slf4j
public class GoShca implements Shca{

    /**
     * 获取所有DID文档信息
     */
    public String DIDlist() {

        String result = GetTest.doGet2(SHCAADD + "/ca/v1/did/list");
        log.info(result);
        return result;
    }

    /**
     * 根据ID获取DID信息
     */
    public String DIDget() {
        String result = GetTest.doGet2(SHCAADD + "/ca/v1/did/get/" + id);
        log.info(result);
        return result;
    }

    /**
     * 根据ID删除DID文档
     */
    public String DIDdelete() {
        String result = GetTest.doDel(SHCAADD + "/ca/v1/did/delete/" + id);
        log.info(result);
        return result;
    }

    /**
     * 新增DID文档
     */
    public String DIDadd(String didJson, String id) {
        Map<String, Object> map = new HashMap<>();
        map.put("didJson", didJson);
        map.put("id", id);

        String param="";
//        if(subLedger!="") param = param +"ledger="+subLedger;
        if(syncFlag)  param = param + "?sync=true&timeout=" + syncTimeout;

        String result = PostTest.postMethod(SHCAADD + "/ca/v1/did/add" + param, map);
        log.info(result);
        return result;
    }

    /**
     * 根据ID获取vc信息
     */
    public String VCget() {
        String result = GetTest.doGet2(SHCAADD + "/ca/v1/vc/get/" + vcId);
        log.info(result);
        return result;
    }

    /**
     * 新增VC
     */
    public String VCadd(String vcId, String vcJson) {
        Map<String, Object> map = new HashMap<>();
        map.put("vcId", vcId);
        map.put("vcJson", vcJson);

        String param="";
//        if(subLedger!="") param = param +"ledger="+subLedger;
        if(syncFlag)  param = param + "?sync=true&timeout=" + syncTimeout;

        String result = PostTest.postMethod(SHCAADD + "/ca/v1/vc/add" + param, map);
        log.info(result);
        return result;
    }

    /**
     * 创建VC
     */
    public String VC(String applicantDid, String vcType, Map vcSubject) {
        Map<String, Object> map = new HashMap<>();
        map.put("applicantDid", applicantDid);
        map.put("vcType", vcType);
        map.put("vcSubject", vcSubject);
        String param="";
//        if(subLedger!="") param = param +"ledger="+subLedger;
        if(syncFlag)  param = param + "?sync=true&timeout=" + syncTimeout;

        String result = PostTest.postMethod(SHCAADD3 + "/vc" + param, map);
        log.info(result);
        return result;
    }

    /**
     * 根据ID删除VC
     */
    public String VCdelete() {
        String result = GetTest.doDel(SHCAADD + "/ca/v1/vc/delete/" + id);
        log.info(result);
        return result;
    }



}
