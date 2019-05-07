package com.tjfintech.common.Interface;

import java.util.List;
import java.util.Map;

public interface MultiSign {
    String genMultiAddress(int M, Map keyMap);
    String Balance(String addr,String priKey,String tokenType);
    String Balance(String addr,String priKey,String Pwd,String tokenType);
    String Balance(String priKey,String tokenType);
    String issueToken(String MultiAddr,String TokenType,String Amount,String Data);
    String issueToken(String MultiAddr,String ToAddr,String TokenType,String Amount,String Data);
    String issueToken(String MultiAddr,String ToAddr,String TokenType,String Amount,String PriKey,String Pwd,String Data);
    String issueTokenCarryPri(String MultiAddr,String TokenType, String Amount,String PriKey,String  Data);
    String issueTokenCarryPri(String MultiAddr,String TokenType, String Amount,String PriKey,String Pwd,String  Data);
    String Sign(String Tx, String Prikey, String Pwd);
    String Sign(String Tx, String Prikey);
    String Transfer(String PriKey, String Pwd, String Data , String fromAddr, List<Map> tokenList);
    String Transfer(String PriKey,String Data,String fromAddr ,List<Map>tokenList);

    String CheckPriKey(String PriKey ,String Pwd);
    String Recycle(String multiAddr,String priKey,String Pwd,String tokenType,String amount);
    String Recycle(String multiAddr,String priKey,String tokenType,String amount);
    String Recycle(String priKey,String tokenType,String amount);
    String QueryZero(String tokenType);
    String collAddress(String pubKey,String ...address);
    String delCollAddress(String priKey,String ...address);
    String collAddressRemovePri(String ...address);
    String freezeToken(String priKey,String tokenType);
    String recoverFrozenToken(String priKey,String tokenType);
    String addissueaddress(String pubKey,String ...address);
    String delissueaddress(String priKey,String ...address);
    String addissueaddressRemovePri(String ...address);
    String delissueaddressRemovePri(String ...address);
    String gettotal(int StartTime,int EndTime,String TokenType);
    String tokenstate(String TokenType);
    String getbalancebytt(String TokenType,String Address);
    String getTotalbyDay(int year,int month,int day);
    String getChainBalance(String tokenType,String Addr);
    String getUTXODetail(int StartTime,int EndTime,String tokenType,int UTXOtype,String FromAddr,String ToAddr);

    String BalanceByAddr(String addr,String tokenType); //按地址查询余额

    //本地签名
    String issueTokenLocalSign(String MultiAddr,String TokenType,String Amount,String Data);
    String issueTokenLocalSign(String MultiAddr,String ToAddr, String TokenType,String Amount,String Data);
    String sendSign(String signData);
    String TransferLocalSign(String PubKey, String Data,String fromAddr ,List<Map>tokenList);
    String RecycleLocalSign(String multiAddr,String pubKey,String tokenType,String amount);
    String RecyclesLocalSign(List<Map> tokenList);

    //同步接口
    String SyncIssueToken(String MultiAddr,String TokenType,String Amount,String Data);
    String SyncIssueToken(String MultiAddr,String ToAddr,String TokenType,String Amount,String Data);
    String SyncIssueToken(String MultiAddr,String ToAddr,String TokenType,String Amount,String PriKey,String Pwd,String Data);
    String SyncIssueTokenCarryPri(String MultiAddr,String TokenType, String Amount,String PriKey,String  Data);
    String SyncIssueTokenCarryPri(String MultiAddr,String TokenType, String Amount,String PriKey,String Pwd,String  Data);
    String SyncSign(String Tx, String Prikey, String Pwd);
    String SyncSign(String Tx, String Prikey);
    String SyncTransfer(String PriKey, String Pwd, String Data , String fromAddr, List<Map> tokenList);
    String SyncTransfer(String PriKey,String Data,String fromAddr ,List<Map>tokenList);
    String SyncRecycle(String multiAddr,String priKey,String Pwd,String tokenType,String amount);
    String SyncRecycle(String multiAddr,String priKey,String tokenType,String amount);
    String SyncRecycle(String priKey,String tokenType,String amount);
}
