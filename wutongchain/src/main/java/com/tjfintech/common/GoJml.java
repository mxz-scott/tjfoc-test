package com.tjfintech.common;


import com.tjfintech.common.Interface.Jml;
import com.tjfintech.common.utils.PostTest;
import com.tjfintech.common.utils.GetTest;
import lombok.extern.slf4j.Slf4j;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassJml.JMLADD;

@Slf4j
public class GoJml implements Jml {

    /**
     * 获取银行列表
     * @return
     */

    public String BankList() {
        String result = GetTest.doGet2(JMLADD + "/jml/v1/bank/list");
        log.info(result);
        return result;
    }

    /**
     * 授权新用户
     *
     */
    public String AuthorizeAdd(String subjectType, String bankId, String endTime, String fileHash, Map subject) {

        Map<String, Object> map = new HashMap<>();
        map.put("subjectType", subjectType);
        map.put("bankId", bankId);
        map.put("fileHash", fileHash);
        map.put("endTime", endTime);
        map.put("subject", subject);

        String param="";
//        if(subLedger!="") param = param +"ledger="+subLedger;
        if(syncFlag)  param = param + "&sync=true&timeout=" + syncTimeout;

        String result = PostTest.postMethod(JMLADD + "/jml/v1/authorize/add" + param, map);
        log.info(result);
        return result;
    }

    /**
     * 查询个人数据
     */
    public String CreditdataQuery (String requestId, String authId, String personId, String personName, String purpose) {
        Map<String, Object> map = new HashMap<>();
        map.put("requestId", requestId);
        map.put("authId", authId);
        map.put("personId", personId);
        map.put("personName", personName);
        map.put("purpose", purpose);

        String param="";
//        if(subLedger!="") param = param +"ledger="+subLedger;
        if(syncFlag)  param = param + "&sync=true&timeout=" + syncTimeout;

        String result = PostTest.postMethod(JMLADD + "/jml/v1/creditdata/query" + param, map);
        log.info(result);
        return result;
    }

    /**
     * 上传授信结果
     */
    public String CreditloanFeedback (String authId, String[] receiverPubkeys, Map results) {
        Map<String, Object> map = new HashMap<>();
        map.put("authId", authId);
        map.put("receiverPubkeys", receiverPubkeys);
        map.put("result", results);

        String param="";
//        if(subLedger!="") param = param +"ledger="+subLedger;
        if(syncFlag)  param = param + "&sync=true&timeout=" + syncTimeout;

        String result = PostTest.postMethod(JMLADD + "/jml/v1/creditloan/feedback" + param, map);
        log.info(result);
        return result;
    }


}
