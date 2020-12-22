package com.tjfintech.common.Interface;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface SmartToken {

    String SmartIssueTokenReq(String userContract, String tokenType, BigDecimal expiredDate, List<Map> toList,
                              BigDecimal activeDate, Boolean reissued, int maxLevel, String extend);
    String SmartIssueTokenApprove(String sigMsg, String sigCrypt, String pubKey);

    String SmartTransferReq(String tokenType, List<Map> payList, List<Map> collList,
                            String newSubType, String extendArgs, String extendData);
    String SmartDestroyReq(String tokenType, List<Map> payList, String extendArgs, String extendData);
    String SmartExchangeReq(String tokenType, List<Map> payList,  String newTokenType,
                            String extendArgs, String extendData);
    String SmartTEDApprove(String type, List<Map> payInfoList, String UTXOInfo);
    String SmartGetBalanceByAddr(String addr, String tokenType); //按地址查询余额
    String SmartGenarateAddress(int number, Map pubkeys);
    String SmartFreeze(String tokenType, String comments);
    String SmartRecover(String tokenType, String comments);

//    String SmartGetZeroBalance(String tokenType); //按地址查询余额
//    String SmartGetAssetsTotal(BigDecimal startTime, BigDecimal endTime, String tokenType);
//    String SmartGetOwnerAddrs(String tokenType);

}
