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
    String SmartTransferApprove(List<Map> payInfoList, String UTXOInfo);
    String SmartDestroyReq(String tokenType, List<Map> payList, String extendArgs, String extendData);
    String SmartDestroyApprove(List<Map> payInfoList, String UTXOInfo);

//    String SmartDestroyApprove(String Address, String prikey, String fromAddr, List<Map> tokenList);

    String SmartGetBalanceByAddr(String addr, String tokenType); //按地址查询余额
    String SmartGetZeroBalance(String tokenType); //按地址查询余额
    String SmartGetAssetsTotal(BigDecimal startTime, BigDecimal endTime, String tokenType);
    String SmartGetOwnerAddrs(String tokenType);
    String SmartGenarateAddress(int number, Map pubkeys);
}
