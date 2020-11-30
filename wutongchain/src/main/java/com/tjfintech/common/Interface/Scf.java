package com.tjfintech.common.Interface;

import java.util.List;
import java.util.Map;

public interface Scf {

    String AccountCreate(String platformKeyID, String PIN, String pubkey, String comments);
    String IssuingApply(String AccountAddress, String companyID1, String coreCompanyKeyID, String PIN, String tokenType, int levelLimit, long expireDate, String supplyAddress1, String amount);
    String IssuingApprove(String platformKeyID, String tokenType, String platformPIN);
    String IssuingCancel(String tokenType, String companyID1, String keyID, String PIN, String comments);
    String IssuingConfirm(String coreCompanyKeyID, String tokenType, String PIN, String comments);
    String IssuingReject(String coreCompanyKeyID, String tokenType, String PIN, String companyID1, String comments);
//    String AssignmentApply(String fromAddress, String keyID, String PIN, String proof, String tokenType, Object Array tokenList, );
    String getowneraddr(String tokentype);
    String SendMsg(String msgcode, String sender, List<Map> list, String mode, String reftx, String msgdata);
}
