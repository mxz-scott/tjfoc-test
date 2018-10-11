package com.tjfintech.common;

import com.tjfintech.common.Interface.SoloSign;

import java.util.List;
import java.util.Map;

public class JavaSoloSign implements SoloSign {
    @Override
    public List<Map> constructToken(String toAddr, String tokenType, String amount) {
        return null;
    }

    @Override
    public List<Map> constructToken(String toAddr, String tokenType, String amount, List<Map> mapList) {
        return null;
    }

    @Override
    public String Balance(String key, String tokenType) {
        return null;
    }

    @Override
    public String Transfer(List<Map> token, String priKey, String data) {
        return null;
    }

    @Override
    public String issueToken(String priKey, String tokenType, String amount, String data) {
        return null;
    }

    @Override
    public String genAddress(String publicKey) {
        return null;
    }


}
