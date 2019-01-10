package com.tjfintech.common.Interface;

import java.util.List;
import java.util.Map;

public interface MultiSign {
    String genMultiAddress(int M, Map keyMap);
    String Balance(String addr,String priKey,String tokenType);
    String Balance(String priKey,String tokenType);
    String issueToken(String MultiAddr,String TokenType,String Amount,String Data);
    String issueToken(String MultiAddr,String ToAddr,String TokenType,String Amount,String Data);
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
}
