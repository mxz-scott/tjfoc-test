package com.tjfintech.common.Interface;

import java.util.List;
import java.util.Map;

public interface Scf {

    String AccountCreate(String platformKeyID, String PIN, String pubkey, String comments);
    String IssuingApply(String contractAddress, String companyID, String keyID, String PIN, String tokenType, long levelLimit, long expireDate, String toAddr, long amount);
//    String IssuingApprove(String tokenType, String keyID, String PIN);
//    String IssuingCancel(String tokenType, String keyID, String PIN);
//    String IssuingConfirm(String keyID, String PIN, String tokenType);
//    String IssuingReject(String keyID, String PIN, String tokenType);
//    String AssignmentApply(String fromAddress, String keyID, String PIN, String proof, String tokenType, Object Array tokenList, );

    String SendMsg(String msgcode, String sender, List<Map> list, String mode, String reftx, String msgdata);
}
