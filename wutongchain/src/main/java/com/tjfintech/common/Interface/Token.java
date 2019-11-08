package com.tjfintech.common.Interface;

import java.util.List;
import java.util.Map;

public interface Token {

    String createGroup(String id, String name, String comments, Map tags);

    String tokenCreateAccount(String entityID, String entityName, String groupID, String comments, Map tags);

    String tokenCreateMultiAddr(Map addresses, String name, String minSignatures, String groupID,
                                  String comments, Map tags);

    String tokenAddMintAddr(String address);
    String tokenAddCollAddr(String address);
    String tokenIssue(String address ,String collAddr,String tokenType,String amount,String comments);
    String tokenTransfer(String from,String to,String tokenType,String amount,String comments);
    String tokenDestory(String address,String tokenType,String amount,String comments);
    String tokenGetBalance(String address,String tokenType);
    String tokenGetDestroyBalance(String address,String tokenType);


}
