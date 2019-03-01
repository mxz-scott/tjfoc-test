package com.tjfintech.common.Interface;

import java.util.List;
import java.util.Map;

public interface MultiSign {

    String BalanceByAddr(String addr); //按地址查询余额
    String BalanceByAddr(String addr,String tokenType); //按地址查询余额
    String Balance(String addr,String priKey,String tokenType);
    String Balance(String priKey,String tokenType); //单签接口，也可以用于多签
    String genMultiAddress(int M, Map keyMap);
    String issueToken(String MultiAddr,String TokenType,String Amount,String Data);
    String Sign(String Tx, String Prikey, String Pwd);
    String Sign(String Tx, String Prikey);
    String Transfer(String PriKey, String Pwd, String Data , String fromAddr, List<Map> tokenList);
    String Transfer(String PriKey,String Data,String fromAddr ,List<Map>tokenList);
    String CheckPriKey(String PriKey ,String Pwd);
//    String Recycle(String multiAddr,String priKey,String Pwd,String tokenType,String amount);
    String Recycle(String multiAddr,String priKey,String tokenType,String amount);
    String Recycle(String priKey,String tokenType,String amount);
//    String Recycles(List<Map> tokenList);//多账号同时回收，非本地签名，只支持多签地址。
    String QueryZero(String tokenType);

    //String RecycleMultiAccount();

    //本地签名
    String issueTokenLocalSign(String MultiAddr,String TokenType,String Amount,String Data);
    String sendSign(String signData);
    String TransferLocalSign(String PubKey, String Data,String fromAddr ,List<Map>tokenList);
    String RecycleLocalSign(String multiAddr,String pubKey,String tokenType,String amount);
    String RecyclesLocalSign(List<Map> tokenList);


}
