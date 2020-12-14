package com.tjfintech.common;


import com.tjfintech.common.Interface.SmartToken;
import com.tjfintech.common.utils.GetTest;
import com.tjfintech.common.utils.PostTest;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;


@Slf4j
public class GoSmartToken implements SmartToken {


    //发行申请
    public String SmartIssueTokenReq(String contractAddress, String tokenType, BigDecimal expiredDate, List<Map> toList,
                                     BigDecimal activeDate, Boolean reissued, int maxLevel, String extend) {

        Map<String, Object> map = new HashMap<>();
        map.put("contractAddress", contractAddress);
        map.put("tokenType", tokenType);
        if (!reissued.equals(null)) {
            map.put("reissued", reissued);
        }
        if (!activeDate.equals(0)) {
            map.put("activeDate", activeDate);
        }
        map.put("expireDate", expiredDate);

        if (maxLevel != 0) {
            map.put("maxLevel", maxLevel);
        }
        map.put("tokenList", toList);

        if (!extend.isEmpty()) {
            map.put("extend", extend);
        }

        String param = "";

        if (subLedger != "") param = param + "&ledger=" + subLedger;

        String response = PostTest.postMethod(SDKADD + "/v2/tx/stoken/issue/apply?" + param, map);
        log.info(response);
        return response;

    }

    //发行审核
    public String SmartIssueTokenApprove(String sigMsg, String sigCrypt, String pubKey) {
        Map<String, Object> map = new HashMap<>();
        map.put("sigMsg", sigMsg);
        map.put("sigCrypt", sigCrypt);
        map.put("pubKey", pubKey);

        String param = "";
        if (syncFlag) param = param + "&sync=true&timeout=" + syncTimeout;
        if (subLedger != "") param = param + "&ledger=" + subLedger;

        String response = PostTest.postMethod(SDKADD + "/v2/tx/stoken/issue/approve?" + param, map);
        log.info(response);
        return response;
    }

    //生成地址
    public String SmartGenarateAddress(int number, Map pubkeys){
        Map<String, Object> map = new HashMap<>();
        List<Object> PubkeysObjects = new ArrayList<>();
        for (Object value : pubkeys.values()) {
            PubkeysObjects.add(value);
        }
        map.put("pubkeys", PubkeysObjects);
        map.put("minSignatures", number);

        String param = "";
        if (subLedger != "") param = param + "&ledger=" + subLedger;

        String response = PostTest.postMethod(SDKADD + "/v2/tx/stoken/address/generate?" + param, map);
        log.info(response);
        return response;
    }

    //转让申请
    public String SmartTransferReq(String tokenType, List<Map> payList, List<Map> collList,
                                   String newSubType, String extendArgs, String extendData) {
        Map<String, Object> map = new HashMap<>();
        map.put("tokenType", tokenType);
        map.put("paymentList", payList);
        map.put("collectionList", collList);
        if (!newSubType.isEmpty()) map.put("newSubType", newSubType);
        if (!extendArgs.isEmpty()) map.put("extendArgs", extendArgs);
        if (!extendData.isEmpty()) map.put("extendData", extendData);

        String param = "";
        if (subLedger != "") param = param + "&ledger=" + subLedger;

        String result = PostTest.postMethod(SDKADD + "/v2/tx/stoken/transfer/apply?" + param, map);
        log.info(result);
        return result;
    }


    public String SmartExchangeReq(String tokenType, List<Map> payList, List<Map> collList, String newTokenType,
                            String extendArgs, String extendData){
        Map<String, Object> map = new HashMap<>();
        map.put("tokenType", tokenType);
        map.put("paymentList", payList);
        map.put("collectionList", collList);
        map.put("newTokenType", newTokenType);
        if (!extendArgs.isEmpty()) map.put("extendArgs", extendArgs);
        if (!extendData.isEmpty()) map.put("extendData", extendData);

        String param = "";
        if (subLedger != "") param = param + "&ledger=" + subLedger;

        String result = PostTest.postMethod(SDKADD + "/v2/tx/stoken/exchange/apply?" + param, map);
        log.info(result);
        return result;
    }

    //销毁申请
    public String SmartDestroyReq(String tokenType, List<Map> payList, String extendArgs, String extendData){
        Map<String, Object> map = new HashMap<>();
        map.put("tokenType", tokenType);
        map.put("paymentList", payList);
        if (!extendArgs.isEmpty()) map.put("extendArgs", extendArgs);
        if (!extendData.isEmpty()) map.put("extendData", extendData);

        String param = "";
        if (subLedger != "") param = param + "&ledger=" + subLedger;

        String result = PostTest.postMethod(SDKADD + "/v2/tx/stoken/destroy/apply?" + param, map);
        log.info(result);
        return result;
    }


    //转让、转换、销毁审核
    public String SmartTEDApprove(String type, List<Map> payInfoList, String UTXOInfo) {
        Map<String, Object> map = new HashMap<>();
        map.put("type", type);
        map.put("payAddressInfoList", payInfoList);
        map.put("UTXOInfo", UTXOInfo);

        String param = "";
        if (syncFlag) param = param + "&sync=true&timeout=" + syncTimeout;
        if (subLedger != "") param = param + "&ledger=" + subLedger;

        String result = PostTest.postMethod(SDKADD + "/v2/tx/stoken/approve?" + param, map);
        log.info(result);
        return result;
    }

    //根据地址查询余额
    public String SmartGetBalanceByAddr(String addr, String tokenType) {

        String param = "address=" + addr;
        if (tokenType != "") param = param + "&tokentype=" + tokenType;
        if (subLedger != "") param = param + "&ledger=" + subLedger;

        String result = (GetTest.SendGetTojson(SDKADD + "/v2/tx/stoken/balance?" + param));
        log.info(result);
        return result;
    }

    public String SmartFreeze(String tokenType, String comments){
        Map<String, Object> map = new HashMap<>();
        map.put("tokenType", tokenType);
        map.put("comments", comments);

        String param = "";
        if (syncFlag) param = param + "&sync=true&timeout=" + syncTimeout;
        if (subLedger != "") param = param + "&ledger=" + subLedger;

        String result = PostTest.postMethod(SDKADD + "/v2/tx/stoken/freeze?" + param, map);
        log.info(result);
        return result;
    }

    public String SmartRecover(String tokenType, String comments){
        Map<String, Object> map = new HashMap<>();
        map.put("tokenType", tokenType);
        map.put("comments", comments);

        String param = "";
        if (syncFlag) param = param + "&sync=true&timeout=" + syncTimeout;
        if (subLedger != "") param = param + "&ledger=" + subLedger;

        String result = PostTest.postMethod(SDKADD + "/v2/tx/stoken/recover?" + param, map);
        log.info(result);
        return result;
    }


//    public String SmartGetZeroBalance(String tokenType) {
//        String param = "";
//        if (subLedger != "") param = param + "&ledger=" + subLedger;
//        String result = (GetTest.SendGetTojson(SDKADD + "/v2/tx/stoken/zeroaddr/balance/?" + tokenType + param));
//        log.info(result);
//        return result;
//    }
//
//
//    public String SmartGetAssetsTotal(BigDecimal startTime, BigDecimal endTime, String tokenType) {
//
//        Map<String, Object> map = new HashMap<>();
//        map.put("startTime", startTime);
//        map.put("endTime", endTime);
//        map.put("tokenType", tokenType);
//        String param = "";
//        if (subLedger != "") param = param + "&ledger=" + subLedger;
//        String result = PostTest.postMethod(SDKADD + "/v2/tx/utxo/assets/total?" + param, map);
//        log.info(result);
//        return result;
//
//    }
//
//    public String SmartGetOwnerAddrs(String tokenType) {
//
//        Map<String, Object> map = new HashMap<>();
//        map.put("tokenType", tokenType);
//        String param = "";
//        if (subLedger != "") param = param + "&ledger=" + subLedger;
//        String result = PostTest.postMethod(SDKADD + "/v2/tx/stoken/getowneraddr?" + param, map);
//        log.info(result);
//        return result;
//
//    }

}


