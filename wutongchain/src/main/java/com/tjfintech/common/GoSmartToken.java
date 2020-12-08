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
    public String SmartIssueTokenReq(String contractAddress, String tokenType, Boolean reissued,
                                     BigDecimal expiredDate, BigDecimal activeDate,
                                     int maxLevel, List<Map> toList, String extend) {

        Map<String, Object> map = new HashMap<>();
        map.put("ContractAddress", contractAddress);
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
        map.put("MinSignatures", number);

        String param = "";
        if (subLedger != "") param = param + "&ledger=" + subLedger;

        String response = PostTest.postMethod(SDKADD + "/v2/tx/stoken/address/generate?" + param, map);
        log.info(response);
        return response;
    }


    public String SmartTransfer(String Address, String prikey, String prikeyPwd, List<Map> tokenList,
                                String data, String extendArgs) {
        Map<String, Object> map = new HashMap<>();
        map.put("multiAddr", Address);
        map.put("prikey", prikey);
        map.put("data", data);
        if (!prikeyPwd.isEmpty()) {
            map.put("password", prikeyPwd);
        }
        map.put("token", tokenList);

        String param = "";
        if (syncFlag) param = param + "&sync=true&timeout=" + syncTimeout;
        if (subLedger != "") param = param + "&ledger=" + subLedger;

        String result = PostTest.postMethod(SDKADD + "/v2/tx/stoken/transfer?" + param, map);
        log.info(result);
        return result;
    }

    public String SmartRecyle(String Address, String prikey, String prikeyPwd, String tokenType, String amount, String data) {
        Map<String, Object> map = new HashMap<>();
        map.put("multiAddress", Address);
        map.put("prikey", prikey);
        if (!prikeyPwd.isEmpty()) {
            map.put("password", prikeyPwd);
        }
        map.put("tokenType", tokenType);
        map.put("amount", amount);
        map.put("data", data);


        String param = "";
        if (syncFlag) param = param + "&sync=true&timeout=" + syncTimeout;
        if (subLedger != "") param = param + "&ledger=" + subLedger;

        String result = PostTest.sendPostToJson(SDKADD + "/v2/tx/stoken/recycle?" + param, map);
        log.info(result);
        return result;
    }

    public String SmartSign(String Address, String prikey, String fromAddr, List<Map> tokenList) {

        String param = "";
        if (syncFlag) param = param + "&sync=true&timeout=" + syncTimeout;
        if (subLedger != "") param = param + "&ledger=" + subLedger;
        return "";
    }

    public String SmartGetBalanceByAddr(String addr, String tokenType) {
        Map<String, Object> map = new HashMap<>();
        map.put("tokenType", tokenType);
        map.put("address", addr);

        String param = "";
        if (subLedger != "") param = param + "&ledger=" + subLedger;

        String result = PostTest.postMethod(SDKADD + "/v2/tx/stoken/balance?" + param, map);
        log.info(result);
        return result;
    }


    public String SmartGetZeroBalance(String tokenType) {
        String param = "";
        if (subLedger != "") param = param + "&ledger=" + subLedger;
        String result = (GetTest.SendGetTojson(SDKADD + "/v2/tx/stoken/zeroaddr/balance/?" + tokenType + param));
        log.info(result);
        return result;
    }


    public String SmartGetAssetsTotal(BigDecimal startTime, BigDecimal endTime, String tokenType) {

        Map<String, Object> map = new HashMap<>();
        map.put("startTime", startTime);
        map.put("endTime", endTime);
        map.put("tokenType", tokenType);
        String param = "";
        if (subLedger != "") param = param + "&ledger=" + subLedger;
        String result = PostTest.postMethod(SDKADD + "/v2/tx/utxo/assets/total?" + param, map);
        log.info(result);
        return result;

    }

    public String SmartGetOwnerAddrs(String tokenType) {

        Map<String, Object> map = new HashMap<>();
        map.put("tokenType", tokenType);
        String param = "";
        if (subLedger != "") param = param + "&ledger=" + subLedger;
        String result = PostTest.postMethod(SDKADD + "/v2/tx/stoken/getowneraddr?" + param, map);
        log.info(result);
        return result;

    }


}


