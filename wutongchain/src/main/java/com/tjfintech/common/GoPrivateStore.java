package com.tjfintech.common;

import com.tjfintech.common.utils.PostTest;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.SDKADD;
import static com.tjfintech.common.utils.UtilsClass.subLedger;

@Slf4j
public class GoPrivateStore {

    public String GetPrivacyStore(String hash, String PubKey){
        Map<String, Object> map = new HashMap<>();
        map.put("txId", hash);
        map.put("PubKey", PubKey);
        String param = "";
        if (!subLedger.isEmpty()) param = param + "?ledger=" + subLedger;
        String result = PostTest.postMethod(SDKADD + "/v2/tx/store/querybypubkey" + param, map);
        log.info(result);
        return result;
    }
    public String PrivacyStoreAuthorize(String hash, List<Map> Secrets,String OwnerPubKey,String Sign){
        Map<String, Object> map = new HashMap<>();
        map.put("txId", hash);
        map.put("Secrets", Secrets);
        map.put("OwnerPubKey", OwnerPubKey);
        map.put("Sign", Sign);
        String param = "";
        if (!subLedger.isEmpty()) param = param + "?ledger=" + subLedger;
        String result = PostTest.postMethod(SDKADD + "/v2/tx/store/authorizebypubkey" + param, map);
        log.info(result);
        return result;
    }
}
