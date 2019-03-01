package com.tjfintech.common.Interface;

import java.util.List;
import java.util.Map;

public interface SoloSign {


    String genAddress(String publicKey);
    String issueToken(String priKey,String tokenType,String amount,String data);
    String Transfer(List<Map> token, String priKey, String data);
    String Balance(String priKey,String tokenType);
    List<Map> constructToken(String toAddr,String tokenType,String amount);
    List<Map> constructToken(String toAddr,String tokenType,String amount,List<Map>mapList);


    //本地签名
    String issueTokenLocalSign(String pubKey,String tokenType,String amount,String data);
    String TransferLocalSign(List<Map> token, String pubKey, String data);
    String RecycleLocalSign(String pubKey,String tokenType,String amount);
    String sendSign(String signData);
//    String RecyclesLocalSign(List<Map> tokenList);

}
