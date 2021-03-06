package com.tjfintech.common.Interface;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface MultiSign {
    String genMultiAddress(int M, Map keyMap);
    String addIssueAddrs(String ...address);
    String delIssueaddrs(String ...address);
    String addCollAddrs(String ...address);
    String delCollAddrs(String... address);

    String BalanceByAddr(String addr,String tokenType); //按地址查询余额
    String QueryZero(String tokenType);
    String getSDKBalance(String TokenType,String Address);
    String getChainBalance(String tokenType,String Addr);

    String issueToken(String MultiAddr,String TokenType,String Amount,String Data);
    String issueToken(String MultiAddr,String ToAddr,String TokenType,String Amount,String Data);
    String issueToken(String MultiAddr,String ToAddr,String TokenType,String Amount,String PriKey,String Pwd,String Data);
    String issueTokenCarryPri(String MultiAddr,String TokenType, String Amount,String PriKey,String  Data);
    String issueTokenCarryPri(String MultiAddr,String TokenType, String Amount,String PriKey,String Pwd,String  Data);

    String Sign(String Tx, String Prikey, String Pwd);
    String Sign(String Tx, String Prikey);
    String Transfer(String PriKey, String Pwd, String Data , String fromAddr, List<Map> tokenList);
    String Transfer(String PriKey,String Data,String fromAddr ,List<Map>tokenList);
    String Recycle(String multiAddr,String priKey,String Pwd,String tokenType,String amount);
    String Recycle(String multiAddr,String priKey,String tokenType,String amount);

    String freezeToken(String tokenType);
    String recoverFrozenToken(String tokenType);

    String gettotal(long StartTime,long EndTime,String TokenType);
    String gettotal(long StartTime,long EndTime,double TokenType);
    String gettotal();
    String tokenstate(String TokenType);
    String getTotalbyDay(int starttime,int endtime);
    String getUTXODetail(long StartTime,long EndTime,String tokenType,int UTXOtype,String FromAddr,String ToAddr);
    String getUTXODetail(long StartTime,long EndTime,String tokenTypeSmartGetBalanceByAddr,int UTXOtype,String FromAddr,double ToAddr);
    String getUTXODetail(long StartTime,long EndTime,String tokenType,String UTXOtype,String FromAddr,String ToAddr);
    String getUTXODetail();

    String CheckPriKey(String PriKey ,String Pwd);

    //本地签名
    String issueTokenLocalSign(String MultiAddr,String TokenType,String Amount,String Data);
    String issueTokenLocalSign(String MultiAddr,String ToAddr, String TokenType,String Amount,String Data);
    String sendSign(String signData);
    String TransferLocalSign(String PubKey, String Data,String fromAddr ,List<Map>tokenList);
    String RecycleLocalSign(String multiAddr,String pubKey,String tokenType,String amount);
    String RecyclesLocalSign(List<Map> tokenList);

}
