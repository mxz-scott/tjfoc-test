package com.tjfintech.common;

import com.tjfintech.common.Interface.MultiSign;

import java.util.List;
import java.util.Map;

public class JavaMultiSign implements MultiSign {
    @Override
    public String genMultiAddress(int M, Map keyMap) {
        return null;
    }

    @Override
    public String Balance(String addr, String priKey, String tokenType) {
        return null;
    }

    @Override
    public String Balance(String priKey, String tokenType) {
        return null;
    }

    @Override
    public String issueToken(String MultiAddr, String TokenType, String Amount, String Data) {
        return null;
    }

    @Override
    public String issueToken(String MultiAddr, String ToAddr, String TokenType, String Amount, String Data) {
        return null;
    }

    @Override
    public String Sign(String Tx, String Prikey, String Pwd) {
        return null;
    }

    @Override
    public String Sign(String Tx, String Prikey) {
        return null;
    }

    @Override
    public String Transfer(String PriKey, String Pwd, String Data, String fromAddr, List<Map> tokenList) {
        return null;
    }

    @Override
    public String Transfer(String PriKey, String Data, String fromAddr, List<Map> tokenList) {
        return null;
    }

    @Override
    public String CheckPriKey(String PriKey, String Pwd) {
        return null;
    }

    @Override
    public String Recycle(String multiAddr, String priKey, String Pwd, String tokenType, String amount) {
        return null;
    }

    @Override
    public String Recycle(String multiAddr, String priKey, String tokenType, String amount) {
        return null;
    }

    @Override
    public String Recycle(String priKey, String tokenType, String amount) {
        return null;
    }

    @Override
    public String QueryZero(String tokenType) {
        return null;
    }

    @Override
    public String collAddress(String pubKey, String ...address) {
        return null;
    }
}
