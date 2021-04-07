package com.tjfintech.common.Interface;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface Scf {

    String AccountCreate(String PlatformAddress,String platformKeyID, String PIN, String pubkey, String comments);
    String IssuingApply( String UID, String AccountAddress, String companyID1, String coreCompanyKeyID, String PIN, String tokenType, int levelLimit, BigDecimal expireDate, String supplyAddress1, String amount);
    String IssuingApprove(String UID,String platformKeyID, String tokenType, String platformPIN);
    String IssuingCancel(String tokenType, String companyID1, String keyID, String PIN, String comments);
    String IssuingConfirm(String UID,String PlatformAddress, String coreCompanyKeyID, String tokenType, String PIN, String comments);
    String IssuingReject(String UID,String coreCompanyKeyID, String tokenType, String PIN, String companyID1, String comments);
    String AssignmentApply(String supplyAddress1, String supplyID1, String PIN, String proof, String tokenType, List<Map> list1,String newSubType, String supplyAddress2);
    String AssignmentConfirm(String UID,String PlatformAddress, String supplyID1, String PIN, String challenge, String tokenType, String comments);
    String AssignmentReject(String UID, String challenge, String tokenType);
    String FinacingApply(String supplyAddress1, String supplyID1, String PIN, String rzproof, String tokenType, String rzamount, String subType, String newFromSubType, String newToSubType, String supplyAddress2);
    String FinacingTest(String ZJFAddress, String rzamount, String timeLimit);
    String FinacingFeedback(String ZJFAddress, String applyNo, String state, String comments, String msg);
    String FinacingConfirm(String UID,String PlatformAddress, String applyNo, String ZJFAddress, String supplyID1, String companyID1, String PIN, String tokenType, String supplyAddress2, String rzchallenge, String comments);
    String FinacingCancel(String UID,String challenge,String tokenType );
    String PayingApply(String tokenType, String companyID1,String comments);
    String PayingNotify(String AccountAddress, String message);
    String PayingFeedback(String QFJGAddress, String tokenType, String state,String comments);
    String PayingConfirm(String UID,String PlatformAddress, String QFJGAddress, String companyID1, List<Map> list2, String platformKeyID, String platformPIN, String tokenType, String comments);


    String getowneraddr(String tokentype);
    String SendMsg(String msgcode, String sender, String platformKeyID, List<Map> list, String mode, String reftx, String msgdata);
    String CreditAdjust(String AccountAddress, String companyID2, String amount);
    String FuncGetoutputinfo(String supplyAddress1, String tokenType, String subtype);
    String FinacingBack(String UID,String PlartformAddress, String  platformKeyID, String PIN, String KeyID, String txID, String comments);
    String FuncGethistory(String txID);
    String FuncAuthorization(String AccountAddress, String supplierMsg, String financeTxID, ArrayList<String> kIDList, String platformKeyID, String PIN);

    String AccountInform(String AccountAddress, String comments);
    String FuncGetsubtype(String txID, String index);
    String AddressGen(String pubkey);
    String Send(String comments);
    String FunGethistoryinfo(String AccountAddress, ArrayList<String> Msglist, String platformKeyID, String PIN);
}
