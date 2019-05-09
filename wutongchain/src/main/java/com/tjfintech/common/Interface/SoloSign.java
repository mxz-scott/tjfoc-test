package com.tjfintech.common.Interface;

import java.util.List;
import java.util.Map;

public interface SoloSign {
    String Balance(String key,String tokenType);
    String Balance(String priKey,String pwd,String tokenType);
    String Transfer(List<Map> token, String priKey, String data);
    String issueToken(String priKey,String tokenType,String amount,String data,String address);
    String genAddress(String publicKey);
    List<Map> constructToken(String toAddr,String tokenType,String amount);
    List<Map> constructToken(String toAddr,String tokenType,String amount,List<Map>mapList);

    //本地签名
    String issueTokenLocalSign(String pubKey,String tokenType,String amount,String data);
    String TransferLocalSign(List<Map> token, String pubKey, String data);
    String RecycleLocalSign(String pubKey,String tokenType,String amount);
    String sendSign(String signData);
//    String RecyclesLocalSign(List<Map> tokenList);

    //同步接口
    String SyncTransfer(Integer timeout ,List<Map> token, String priKey, String data);
    String SyncIssueToken(Integer timeout ,String priKey,String tokenType,String amount,String data,String address);
}
