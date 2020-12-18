package com.tjfintech.common;

import com.tjfintech.common.Interface.Scf;
import com.tjfintech.common.utils.PostTest;
//import com.tjfoc.sdk.SDK_TjfocSDK_WalletSDK;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.SDKADD;
import static com.tjfintech.common.utils.UtilsClass.subLedger;
import static com.tjfintech.common.utils.UtilsClassScf.*;

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

    /**
     * 资产开立申请
     * @param AccountAddress
     * @param companyID1
     * @param coreCompanyKeyID
     * @param PIN
     * @param tokenType
     * @param levelLimit
     * @param expireDate
     * @param supplyAddress1
     * @param amount
     * @return
     */
    public String IssuingApply(String AccountAddress, String companyID1, String coreCompanyKeyID, String PIN, String tokenType, int levelLimit, long expireDate, String supplyAddress1, String amount) {
        Map<String, Object> map = new HashMap<>();

        map.put("contractAddress", AccountAddress);
        map.put("companyID", companyID1);
        map.put("keyID", coreCompanyKeyID);
        map.put("PIN", PIN);
        map.put("tokenType", tokenType);
        map.put("levelLimit", levelLimit);
        map.put("expireDate", expireDate);
        map.put("toAddr", supplyAddress1);
        map.put("amount", amount);

        String param="";
        if(subLedger!="") param = param +"&ledger="+subLedger;

        String result = PostTest.postMethod(SDKADD + "/scf/issuing/apply", map);
        log.info(result);
        return result;
    }

    /**
     * 开立审核
     * @param platformKeyID
     * @param tokenType
     * @param platformPIN
     * @return
     */
    public String IssuingApprove(String platformKeyID, String tokenType, String platformPIN) {
        Map<String, Object> map = new HashMap<>();

        map.put("keyID", platformKeyID);
        map.put("tokenType", tokenType);
        map.put("PIN", platformPIN);

        String param="";
        if (subLedger!="") param = param +"ledger"+subLedger;

        String result = PostTest.postMethod(SDKADD + "/scf/issuing/approve", map);
        log.info(result);
        return result;
    }

    /**
     * 开立取消
     * @param tokenType
     * @param companyID1
     * @param keyID
     * @param PIN
     * @param comments
     * @return
     */
    public String IssuingCancel(String tokenType, String companyID1, String keyID, String PIN, String comments) {
        Map<String, Object> map = new HashMap<>();

        map.put("tokenType", tokenType);
        map.put("companyID", companyID1);
        map.put("keyID", keyID);
        map.put("PIN", PIN);
        map.put("comments", comments);


        String param="";
        if (subLedger!="") param = param +"ledger"+subLedger;

        String result = PostTest.postMethod(SDKADD + "/scf/issuing/cancel", map);
        log.info(result);
        return result;
    }

    /**
     * 开立签收
     * @param coreCompanyKeyID
     * @param tokenType
     * @param PIN
     * @param comments
     * @return
     */
    public String IssuingConfirm(String coreCompanyKeyID, String tokenType, String PIN, String comments) {
        Map<String, Object> map = new HashMap<>();

        map.put("keyID", coreCompanyKeyID);
        map.put("tokenType", tokenType);
        map.put("PIN", PIN);
        map.put("comments", comments);

        String param="";
        if (subLedger!="") param = param +"ledger"+subLedger;

        String result = PostTest.postMethod(SDKADD + "/scf/issuing/confirm", map);
        log.info(result);
        return result;
    }

    /**
     * 开单拒收
     * @param coreCompanyKeyID
     * @param tokenType
     * @param PIN
     * @param companyID1
     * @param comments
     * @return
     */
    public String IssuingReject(String coreCompanyKeyID, String tokenType, String PIN, String companyID1, String comments) {
        Map<String, Object> map = new HashMap<>();

        map.put("keyID", coreCompanyKeyID);
        map.put("tokenType", tokenType);
        map.put("PIN", PIN);
        map.put("companyID", companyID1);
        map.put("comments", comments);

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

    /**
     * 查询token资产
     * @param tokentype
     * @return
     */
    public String getowneraddr(String tokentype) {
        Map<String, Object> map = new HashMap<>();

        map.put("tokentype", tokentype);

        String param="";
        if (subLedger!="") param = param +"ledger"+subLedger;

        String result = PostTest.postMethod(SDKADD + "/v2/tx/stoken/getowneraddr", map);
        log.info(result);
        return result;
    }

    /**
     * 资产转让申请
     * @param supplyAddress1
     * @param supplyID1
     * @param PIN
     * @param proof
     * @param tokenType
     * @param list1
     * @param newSubType
     * @param supplyAddress2
     * @param comments
     * @return
     */
    public String AssignmentApply(String supplyAddress1, String supplyID1, String PIN, String proof, String tokenType, List<Map> list1,String newSubType, String supplyAddress2, String comments) {

        Map<String, Object> map = new HashMap<>();
        map.put("fromAddress", supplyAddress1);
        map.put("keyID", supplyID1);
        map.put("PIN", PIN);
        map.put("proof", proof);
        map.put("tokenType", tokenType);
        map.put("tokenList",list1);
        map.put("newSubType",newSubType);
        map.put("toAddress",supplyAddress2);
        map.put("comments",comments);

        String param="";
        if (subLedger!="") param = param +"ledger"+subLedger;

        String result = PostTest.postMethod(SDKADD + "/scf/assignment/apply", map);
        log.info(result);
        return result;
    }

    /**
     * 资产转让签收
     * @param supplyID1
     * @param PIN
     * @param challenge
     * @param tokenType
     * @param comments
     * @return
     */
    public String AssignmentConfirm(String supplyID1, String PIN, String challenge, String tokenType, String comments) {
        Map<String, Object> map = new HashMap<>();

        map.put("keyID", supplyID1);
        map.put("PIN",PIN);
        map.put("challenge",challenge);
        map.put("tokenType",tokenType);
        map.put("comments",comments);

        String param="";
        if (subLedger!="") param = param +"ledger"+subLedger;

        String result = PostTest.postMethod(SDKADD + "/scf/assignment/confirm", map);
        log.info(result);
        return result;
    }

    /**
     * 转让资产拒收
     * @param challenge
     * @param tokenType
     * @param comments
     * @return
     */
    public String AssignmentReject(String challenge, String tokenType, String comments) {
        Map<String, Object> map = new HashMap<>();

        map.put("challenge", challenge);
        map.put("tokenType", tokenType);
        map.put("comments", comments);

        String param = "";
        if (subLedger != "") param = param + "ledger" + subLedger;

        String result = PostTest.postMethod(SDKADD + "/scf/assignment/reject", map);
        log.info(result);
        return result;
    }

    /**
     * 开立额度调整
     * @param AccountAddress
     * @param companyID1
     * @param amount
     * @return
     */
    public String CreditAdjust(String AccountAddress, String companyID1, String amount) {
        Map<String, Object> map = new HashMap<>();

        map.put("contractAddress", AccountAddress);
        map.put("companyID", companyID2);
        map.put("amount", amount);

        String param = "";
        if (subLedger != "") param = param + "&ledger=" + subLedger;

        String result = PostTest.postMethod(SDKADD + "/scf/credit/adjust", map);
        log.info(result);
        return result;
    }
    /**
     * 获取output的交易id和index
     * @param supplyAddress1
     * @param tokenType
     * @param subType
     * @return
     */
    public String FuncGetoutputinfo(String supplyAddress1, String tokenType, String subType) {
        Map<String, Object> map = new HashMap<>();

        map.put("address", supplyAddress1);
        map.put("tokenType", tokenType);
        map.put("subType", subType);

        String param = "";
        if (subLedger != "") param = param + "&ledger=" + subLedger;

        String result = PostTest.postMethod(SDKADD + "/scf/func/getoutputinfo", map);
        log.info(result);
        return result;
    }

    /**
     * 融资申请
     */
    public String FinacingApply(String supplyAddress1, String supplyID1, String PIN, String rzproof, String tokenType, String rzamount, String subType, String newFromSubType, String newToSubType, String supplyAddress2) {
        Map<String, Object> map = new HashMap<>();

        map.put("fromAddress",supplyAddress1);
        map.put("keyID",supplyID1);
        map.put("PIN",PIN);
        map.put("proof",rzproof);
        map.put("tokenType",tokenType);
        map.put("amount",rzamount);
        map.put("subType",subType);
        map.put("newFromSubType",newFromSubType);
        map.put("newToSubType",newToSubType);
        map.put("toAddress",supplyAddress2);

        String param="";
        if (subLedger!="") param = param +"ledger"+subLedger;

        String result = PostTest.postMethod(SDKADD + "/scf/finacing/apply", map);
        log.info(result);
        return result;
    }

    /**
     * 融资试算
     */
    public String FinacingTest(String ZJFAddress, String rzamount, String timeLimit) {
        Map<String, Object> map = new HashMap<>();

        map.put("contractAddress",ZJFAddress);
        map.put("amount",rzamount);
        map.put("timeLimit",timeLimit);

        String param="";
        if (subLedger!="") param = param +"ledger"+subLedger;

        String result = PostTest.postMethod(SDKADD + "/scf/finacing/test", map);
        log.info(result);
        return result;
    }

    /**
     * 融资申请反馈
     */
    public String FinacingFeedback(String ZJFAddress, String applyNo, String state, String comments) {
        Map<String, Object> map = new HashMap<>();

        map.put("contractAddress",ZJFAddress);
        map.put("applyNo",applyNo);
        map.put("state",state);
        map.put("comments",comments);

        String param="";
        if (subLedger!="") param = param +"ledger"+subLedger;

        String result = PostTest.postMethod(SDKADD + "/scf/finacing/feedback", map);
        log.info(result);
        return result;
    }

    /**
     * 融资签收
     */
    public String FinacingConfirm(String applyNo, String ZJFAddress, String supplyID, String companyID1, String PIN, String tokenType, String supplyAddress2, String rzchallenge, String comments) {
        Map<String, Object> map = new HashMap<>();

        map.put("applyNo",applyNo);
        map.put("contractAddress",ZJFAddress);
        map.put("keyID",supplyID);
        map.put("companyID",companyID1);
        map.put("PIN",PIN);
        map.put("tokenType",tokenType);
        map.put("toAddr",supplyAddress2);
        map.put("challenge",rzchallenge);
        map.put("comments",comments);

        String param="";
        if (subLedger!="") param = param +"ledger"+subLedger;

        String result = PostTest.postMethod(SDKADD + "/scf/finacing/confirm", map);
        log.info(result);
        return result;
    }

    /**
     * 融资取消
     */
    public String FinacingCancel(String challenge, String tokenType, String comments) {
        Map<String, Object> map = new HashMap<>();

        map.put("challenge",challenge);
        map.put("tokenType",tokenType);
        map.put("comments",comments);

        String param="";
        if (subLedger!="") param = param +"ledger"+subLedger;

        String result = PostTest.postMethod(SDKADD + "/scf/finacing/cancel", map);
        log.info(result);
        return result;
    }

    /**
     * 兑付申请
     */
    public String PayingApply(String tokenType, String companyID1) {
        Map<String, Object> map = new HashMap<>();

        map.put("tokenType",tokenType);
        map.put("companyID",companyID1);

        String param="";
        if (subLedger!="") param = param +"ledger"+subLedger;

        String result = PostTest.postMethod(SDKADD + "/scf/paying/apply", map);
        log.info(result);
        return result;
    }

    /**
     * 兑付通知
     */
    public String PayingNotify(String AccountAddress, String message) {
        Map<String, Object> map = new HashMap<>();

        map.put("AccountAddress",AccountAddress);
        map.put("message",message);

        String param="";
        if (subLedger!="") param = param +"ledger"+subLedger;

        String result = PostTest.postMethod(SDKADD + "/scf/paying/notify", map);
        log.info(result);
        return result;
    }

    /**
     * 兑付反馈
     */
    public String PayingFeedback(String QFJGAddress, String tokenType, String state, String comments) {
        Map<String, Object> map = new HashMap<>();

        map.put("contractAddress",QFJGAddress);
        map.put("tokenType",tokenType);
        map.put("state",state);
        map.put("comments",comments);

        String param="";
        if (subLedger!="") param = param +"ledger"+subLedger;

        String result = PostTest.postMethod(SDKADD + "/scf/paying/feedback", map);
        log.info(result);
        return result;
    }

    /**
     * 兑付确认
     */
    public String PayingConfirm(String QFJGAddress, String companyID1, List<Map> list4, String platformKeyID, String platformPIN, String tokenType, String comments) {
        Map<String, Object> map = new HashMap<>();
        map.put("contractAddress",QFJGAddress);
        map.put("companyID",companyID1);
        map.put("accounts",list4);
        map.put("platformKeyID",platformKeyID);
        map.put("PIN",platformPIN);
        map.put("tokenType",tokenType);
        map.put("comments",comments);

        String param="";
        if (subLedger!="") param = param +"ledger"+subLedger;

        String result = PostTest.postMethod(SDKADD + "/scf/paying/confirm", map);
        log.info(result);
        return result;
    }


}
