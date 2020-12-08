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
    String AssignmentApply(String supplyAddress1, String supplyID1, String PIN, String proof, String tokenType, List<Map> list1,String newSubType, String supplyAddress2, String comments);
    String AssignmentConfirm(String supplyID1, String PIN, String challenge, String tokenType, String comments);
    String AssignmentReject(String challenge, String tokenType, String comments);
    String FinacingApply(String supplyAddress1, String supplyID1, String PIN, String proof, String tokenType, String amount, String subType, String newFromSubType, String newToSubType, String supplyAddress2);
    String FinacingTest(String ZJFAddress, String amount, String timeLimit);
    String FinacingFeedback(String ZJFAddress, String applyNo, String state, String comments);
    String FinacingConfirm(String applyNo, String ZJFAddress, String supplyID1, String companyID1, String PIN, String tokenType, String supplyAddress2, String challenge, String comments);
    String FinacingCancel(String challenge, String tokenType, String comments);
    String PayingApply(String tokenType, String companyID1);
    String PayingNotify(String AccountAddress, String message);
    String PayingFeedback(String QFJGAddress, String tokenType, String state, String comments);
    String PayingConfirm(String QFJGAddress, String companyID1, List<Map> list2, String platformKeyID, String platformPIN, String tokenType, String comments);


    String getowneraddr(String tokentype);
    String SendMsg(String msgcode, String sender, List<Map> list, String mode, String reftx, String msgdata);
}
