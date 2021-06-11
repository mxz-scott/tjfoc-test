package com.tjfintech.common.utils;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class UtilsClassKMS {
    public static String keySpecSm4 = "sm4";
    public static String keySpecSm2 = "sm2";
    public static  String keySpecSm9 ="sm9_enc";
    public static  String keySpecabe ="cp_abe";
    public static String KMSADD = "http://10.1.5.243:6080";
//    public static String KMSADD = "http://117.62.22.95:8001";
    public static String password = "123";
    public static String oldPwd = "Pass7899";
    public static String newPwd = "Pass4321";
    public static String pubFormat = "sm2_pem";
    public static int size = 16;
    public static String plainText = "VGhpcyBpcyBwbGFpbiB0ZXh0Lg==";
    public static String Digest = "VGhpcyBpcyB0ZXN0aW5nIGZvciBzaWduIGFuZCB2ZXJpZnku";
    public static String Digesterror = "VGhpcyBpcyB0ZXN0aW5nIGZvciBzaWduLg==";

    //获取参数
    public static String getKeyIdSm4(String response) {
        Map mapkey = JSON.parseObject(response, Map.class);
        Map mapType1 = JSON.parseObject(mapkey.get("data").toString(), Map.class);
        String keyIdsm4 = mapType1.get("keyId").toString();
        System.out.println("a = " + keyIdsm4);
        return keyIdsm4;
    }

    public static String getKeySm2(String response1) {
        Map mapkey = JSON.parseObject(response1, Map.class);
        Map mapType1 = JSON.parseObject(mapkey.get("data").toString(), Map.class);
        String keyIdsm2 = mapType1.get("keyId").toString();
        System.out.println("b = " + keyIdsm2);
        return keyIdsm2;
    }
    public static String getImportToken(String response) {
        Map mapkey = JSON.parseObject(response, Map.class);
        Map mapType1 = JSON.parseObject(mapkey.get("data").toString(), Map.class);
        String importToken = mapType1.get("importToken").toString();
        System.out.println("c = " + importToken);
        return importToken;
    }
    public static String getEncryptPublicKey(String response) {
        Map mapkey = JSON.parseObject(response, Map.class);
        Map mapType1 = JSON.parseObject(mapkey.get("data").toString(), Map.class);
        String encryptPublicKey = mapType1.get("encryptPublicKey").toString();
        System.out.println("d = " + encryptPublicKey);
        return encryptPublicKey;
    }

    public static String getCipherText(String response2) {
        Map mapkey = JSON.parseObject(response2, Map.class);
        Map mapType1 = JSON.parseObject(mapkey.get("data").toString(), Map.class);
        String cipherText = mapType1.get("cipherText").toString();
        System.out.println("e = " + cipherText);
        return cipherText;
    }
    public static String getValue(String response2) {
        Map mapkey = JSON.parseObject(response2, Map.class);
        Map mapType1 = JSON.parseObject(mapkey.get("data").toString(), Map.class);
        String value = mapType1.get("value").toString();
        System.out.println("f = " + value);
        return value;
    }

}
