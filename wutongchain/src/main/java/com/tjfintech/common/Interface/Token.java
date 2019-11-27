package com.tjfintech.common.Interface;

import java.util.List;
import java.util.Map;

public interface Token {

    String createGroup(String id, String name, String comments, Map tags);

    String tokenCreateAccount(String entityID, String entityName, String groupID, String comments, Map tags);

    String tokenCreateMultiAddr(Map addresses, String name, int minSignatures, String groupID,
                                  String comments, Map tags);

    String tokenAddMintAddr(String address);
    String tokenAddCollAddr(String address);
    String tokenDelMintAddr(String address);
    String tokenDelCollAddr(String address);
    String tokenIssue(String address ,String collAddr,String tokenType,String amount,String comments);
    String tokenTransfer(String from,String to,String tokenType,String amount,String comments);
    String tokenTransfer(String from,String comments,List<Map>tokenList);
    String tokenDestoryByTokenType(String tokenType,String comments);
    String tokenDestoryByList(List<Map>tokenList, String comments);
    String tokenDestoryByList(String address,String tokenType ,String amount, String comments);
    String tokenGetBalance(String address,String tokenType);
    String tokenGetDestroyBalance();

    String tokenFreezeToken(String tokenType);
    String tokenRecoverToken(String tokenType);

    String tokenCreateStore(String Data);
    String tokenCreatePrivateStore(String Data, Map keyMap);
    String tokenGetPrivateStore(String Data, String addr);

    String tokenGetPubkey(String address);
    String tokenGetTxDetail(String hashData);


}
