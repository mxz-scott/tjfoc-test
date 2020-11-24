package com.tjfintech.common.utils;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class UtilsClassKMS {
    public static String keySpecSm4 = "sm4";
    public static String keySpecSm2 = "sm2";
    public static String KMSADD = "http://10.1.3.164:6080";
    public static String password = "123";
    public static String oldPwd = "123";
    public static String newPwd = "321";
    public static String pubFormat = "sm2_pem";
    public static int size = 16;
    public static String plainText = "U3ltbWV0cmljIGtleSBlbmNyeXB0aW9u";
    public static String Digest = "MTIzNDU2Nzg5MDEyMzEyMw==";
    public static String Digesterror = "MTIzNDU2Nzg5MDEyMz";

    //获取参数
    public static String a(String response) {
        Map mapkey = JSON.parseObject(response, Map.class);
        Map mapType1 = JSON.parseObject(mapkey.get("data").toString(), Map.class);
        String a = mapType1.get("keyId").toString();
        System.out.println("a = " + a);
        return a;
    }
    public static String b(String response) {
        Map mapkey = JSON.parseObject(response, Map.class);
        Map mapType1 = JSON.parseObject(mapkey.get("data").toString(), Map.class);
        String b = mapType1.get("cipherText").toString();
        System.out.println("x = " + b);
        return b;
    }
    public static String c(String response) {
        Map mapkey = JSON.parseObject(response, Map.class);
        Map mapType1 = JSON.parseObject(mapkey.get("data").toString(), Map.class);
        String c = mapType1.get("importToken").toString();
        System.out.println("y = " + c);
        return c;
    }
    public static String d(String response) {
        Map mapkey = JSON.parseObject(response, Map.class);
        Map mapType1 = JSON.parseObject(mapkey.get("data").toString(), Map.class);
        String d = mapType1.get("encryptPublicKey").toString();
        System.out.println("z = " + d);
        return d;
    }
    public static String e(String response1) {
        Map mapkey = JSON.parseObject(response1, Map.class);
        Map mapType1 = JSON.parseObject(mapkey.get("data").toString(), Map.class);
        String e = mapType1.get("keyId").toString();
        System.out.println("e = " + e);
        return e;
    }
    public static String f(String response2) {
        Map mapkey = JSON.parseObject(response2, Map.class);
        Map mapType1 = JSON.parseObject(mapkey.get("data").toString(), Map.class);
        String f = mapType1.get("cipherText").toString();
        System.out.println("f = " + f);
        return f;
    }
    public static String g(String response2) {
        Map mapkey = JSON.parseObject(response2, Map.class);
        Map mapType1 = JSON.parseObject(mapkey.get("data").toString(), Map.class);
        String g = mapType1.get("value").toString();
        System.out.println("g = " + g);
        return g;
    }
}
