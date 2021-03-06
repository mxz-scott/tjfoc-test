package com.tjfintech.common.Interface;

import java.util.List;
import java.util.Map;

public interface SoloSign {
    String BalanceByAddr(String Address,String tokenType);
    String Transfer(List<Map> token, String priKey, String data);
    String issueToken(String priKey,String tokenType,String amount,String data,String address);
    String Recycle(String priKey,String tokenType,String amount);
    String genAddress(String publicKey);
    List<Map> constructToken(String toAddr,String tokenType,String amount);
    List<Map> constructToken(String toAddr,String tokenType,String amount,List<Map>mapList);

    //本地签名
    String issueTokenLocalSign(String pubKey,String tokenType,String amount,String data);
    String TransferLocalSign(List<Map> token, String pubKey, String data);
    String RecycleLocalSign(String pubKey,String tokenType,String amount);
    String sendSign(String signData);
}
