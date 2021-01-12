package com.tjfintech.common.Interface;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface Token {

    String createGroup(String id, String name, String comments, Map tags);

    String tokenCreateAccount(String entityID, String entityName, String groupID, String comments, ArrayList<String> listTag);
    String tokenCreateAccount(String entityID, String entityName, String groupID, String comments, ArrayList<String> listTag,
                              Map<String, Object>mapSendMsg);//带消息存证功能功能

    String tokenCreateMultiAddr(Map addresses, String name, int minSignatures, String groupID,
                                  String comments,  ArrayList<String> listTag);
    String tokenCreateMultiAddrByPubkeys(Map pubkeys, String name, int minSignatures,
                                                String groupID, String comments, ArrayList<String> listTag);

    String tokenAddMintAddr(String address);
    String tokenAddCollAddr(String address);
    String tokenDelMintAddr(String address);
    String tokenDelCollAddr(String address);
    String tokenIssue(String address ,String collAddr,String tokenType,String amount,String comments);
    String tokenIssue(String address ,String collAddr,String tokenType,String amount,String comments,Map<String, Object>mapSendMsg);
    String tokenTransfer(String from,String to,String tokenType,String amount,String comments);
    String tokenTransfer(String from,String comments,List<Map>tokenList);
    String tokenTransferWithID(String from,ArrayList<String> IDList,String comments,List<Map>tokenList);
    String tokenTransfer(String from,String comments,List<Map>tokenList,List<Map>UTXOList);
    String tokenTransfer(String from,String comments,List<Map>tokenList,Map<String, Object>mapSendMsg);
    String tokenTransferUTXOMsg(String from,String comments,List<Map>UTXOList,Map<String,Object>mapSendMsg);
    String tokenDestoryByTokenType(String tokenType,String comments);
    String tokenDestoryByTokenType(String tokenType,String comments,Map<String, Object>mapSendMsg);
    String tokenDestoryByList(List<Map>tokenList, String comments);
    String tokenDestoryByList(String address,String tokenType ,String amount, String comments);
    String tokenDestoryByList(List<Map>tokenList, String comments,Map<String, Object>mapSendMsg);
    String tokenDestoryByList(List<Map> tokenList,List<Map> utxoList, String comments,Map<String, Object>mapSendMsg);
    String tokenGetBalance(String address,String tokenType);
    String tokenGetDestroyBalance();

    String tokenFreezeToken(String tokenType);
    String tokenRecoverToken(String tokenType);

    String tokenSendMsg(Map<String,Object>mapSendMsg);

    String tokenCreateStore(String Data);
    String tokenCreatePrivateStore(String Data, Map keyMap);
    String tokenGetPrivateStore(String Data, String addr);

    String tokenGetPubkey(String address);
    String tokenGetTxDetail(String hashData);

    String tokenGetApiHealth();
}
