package com.tjfintech.common.Interface;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface SmartToken {

    String SmartIssueTokenReq(String userContract, String tokenType, Boolean reissued, BigDecimal expiredDate, BigDecimal activeDate,
                              int maxLevel, List<Map> toList, String extend);
    String SmartIssueTokenApprove(String sigMsg, String sigCrypt, String pubKey);
    String SmartTransfer(String Address, String prikey, String prikeyPwd, List<Map> tokenList, String data, String extendArgs);
    String SmartRecyle(String Address, String prikey, String prikeyPwd, String tokenType, String amount, String data);
    String SmartSign(String Address, String prikey, String fromAddr, List<Map> tokenList);

    String SmartGetBalanceByAddr(String addr, String tokenType); //按地址查询余额
    String SmartGetZeroBalance(String tokenType); //按地址查询余额
    String SmartGetAssetsTotal(BigDecimal startTime, BigDecimal endTime, String tokenType);
    String SmartGetOwnerAddrs(String tokenType);
    String SmartGenarateAddress(int number, Map pubkeys);
}
