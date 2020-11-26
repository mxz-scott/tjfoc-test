package com.tjfintech.common;

import com.tjfintech.common.Interface.Scf;
import com.tjfintech.common.utils.PostTest;
//import com.tjfoc.sdk.SDK_TjfocSDK_WalletSDK;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.SDKADD;
import static com.tjfintech.common.utils.UtilsClass.subLedger;

@Slf4j
public class GoScf implements Scf {

    /**
     * 创建账户（地址）
     *
     */
    public String AccountCreate(String platformKeyID, String PIN,String pubkey, String comments) {

        Map<String, Object> map = new HashMap<>();
        map.put("platformKeyID", platformKeyID);
        if (PIN !="")   map.put("PIN", PIN);
        if (pubkey !="")   map.put("pubkey", pubkey);
        map.put("comments", comments);

        String param="";
        if(subLedger!="") param = param +"&ledger="+subLedger;

        String result = PostTest.postMethod(SDKADD + "/scf/account/create" + param, map);
        log.info(result);
        return result;
    }

    public String IssuingApply(String contractAddress, String companyID, String keyID, String PIN, String tokenType, long levelLimit, long expireDate, String toAddr, long amount) {
        Map<String, Object> map = new HashMap<>();

        map.put("contractAddress", contractAddress);
        map.put("companyID", companyID);
        map.put("keyID", keyID);
        map.put("PIN", PIN);
        map.put("tokenType", tokenType);
        map.put("levelLimit", levelLimit);
        map.put("expireDate", expireDate);
        map.put("toAddr", toAddr);
        map.put("amount", amount);

        String param="";
        if(subLedger!="") param = param +"&ledger="+subLedger;

        String result = PostTest.postMethod(SDKADD + "/scf/issuing/apply", map);
        log.info(result);
        return result;
    }

    public String IssuingApprove(String tokenType, String keyID, String PIN) {
        Map<String, Object> map = new HashMap<>();

        map.put("tokenType", tokenType);
        map.put("keyID", keyID);
        map.put("PIN", PIN);

        String param="";
        if (subLedger!="") param = param +"ledger"+subLedger;

        String result = PostTest.postMethod(SDKADD + "/scf/issuing/approve", map);
        log.info(result);
        return result;
    }

    public String IssuingCancel(String tokenType, String keyID, String PIN) {
        Map<String, Object> map = new HashMap<>();

        map.put("tokenType", tokenType);
        map.put("keyID", keyID);
        map.put("PIN", PIN);

        String param="";
        if (subLedger!="") param = param +"ledger"+subLedger;

        String result = PostTest.postMethod(SDKADD + "/scf/issuing/cancel", map);
        log.info(result);
        return result;
    }

    public String IssuingConfirm(String keyID, String PIN, String tokenType) {
        Map<String, Object> map = new HashMap<>();

        map.put("keyID", keyID);
        map.put("PIN", PIN);
        map.put("tokenType", tokenType);

        String param="";
        if (subLedger!="") param = param +"ledger"+subLedger;

        String result = PostTest.postMethod(SDKADD + "/scf/issuing/confirm", map);
        log.info(result);
        return result;
    }

    public String IssuingReject(String keyID, String PIN, String tokenType) {
        Map<String, Object> map = new HashMap<>();

        map.put("keyID", keyID);
        map.put("PIN", PIN);
        map.put("tokenType", tokenType);

        String param="";
        if (subLedger!="") param = param +"ledger"+subLedger;

        String result = PostTest.postMethod(SDKADD + "/scf/issuing/reject", map);
        log.info(result);
        return result;
    }

    /**
     * 发送存证扩展消息
     *
     */
    public String SendMsg(String msgcode, String sender, List<Map> list, String mode, String reftx, String msgdata) {
        Map<String, Object> map = new HashMap<>();

        map.put("msgcode", msgcode);
        map.put("sender", sender);
        map.put("receivers", list);
        map.put("mode", mode);
        map.put("reftx", reftx);
        map.put("msgdata", msgdata);

        String param="";
        if (subLedger!="") param = param +"ledger"+subLedger;

        String result = PostTest.postMethod(SDKADD + "/scf/func/sendmsg", map);
        log.info(result);
        return result;
    }
}
