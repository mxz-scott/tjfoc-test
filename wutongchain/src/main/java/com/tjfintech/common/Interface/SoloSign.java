package com.tjfintech.common.Interface;

import java.util.List;
import java.util.Map;

public interface SoloSign {
    String Balance(String key,String tokenType);
    String Transfer(List<Map> token, String priKey, String data);
    String issueToken(String priKey,String tokenType,String amount,String data);
    String issueTokenV2(String tokenType,String amount,String data);
    String genAddress(String publicKey);
    List<Map> constructToken(String toAddr,String tokenType,String amount);
    List<Map> constructToken(String toAddr,String tokenType,String amount,List<Map>mapList);
}
