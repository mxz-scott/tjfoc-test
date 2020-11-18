package com.tjfintech.common.utils;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class UtilsClassKMS {
    public static String keySpecSm4 = "sm4";
    public static String keySpecSm2 = "sm2";
    public static String KMSADD = "http://10.1.5.223:8881";
    public static String password = "123";
    public static String oldPwd = "123";
    public static String newPwd = "321";
    public static String pubFormat = "sm2_pem";
    public static int size = 16;
    public static String plainText = "U3ltbWV0cmljIGtleSBlbmNyeXB0aW9u";
    public static String Digest = "MTIzNDU2Nzg5MDEyMzEyMw==";
    public static String a(String response) {
//        String response = new String();
        Map mapkey = JSON.parseObject(response, Map.class);
        Map mapType1 = JSON.parseObject(mapkey.get("data").toString(), Map.class);
        String s = mapType1.get("keyId").toString();
        System.out.println("s = " + s);
        return s;
    }
    public static String b(String response1) {
//        String response = new String();
        Map mapkey = JSON.parseObject(response1, Map.class);
        Map mapType1 = JSON.parseObject(mapkey.get("data").toString(), Map.class);
        String x = mapType1.get("cipherText").toString();
        System.out.println("x = " + x);
        return x;
    }
}
