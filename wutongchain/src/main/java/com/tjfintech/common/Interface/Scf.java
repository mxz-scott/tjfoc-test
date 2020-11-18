package com.tjfintech.common.Interface;

public interface Scf {

    String AccountCreate(String platformKeyID, String PIN);
    String IssuingApply(String contractAddress, String companyID, String keyID, String PIN, String tokenType, long levelLimit, long expireDate, String toAddr, long amount);
//    String IssuingApprove(String tokenType, String keyID, String PIN);
//    String IssuingCancel(String tokenType, String keyID, String PIN);
//    String IssuingConfirm(String keyID, String PIN, String tokenType);
//    String IssuingReject(String keyID, String PIN, String tokenType);
//    String AssignmentApply(String fromAddress, String keyID, String PIN, String proof, String tokenType, Object Array tokenList, );



}
