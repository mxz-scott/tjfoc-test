package com.tjfintech.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.Base64;

import static com.tjfintech.common.utils.UtilsClass.testDataPath;

@Slf4j
public class UtilsClassScf {

    public static String platformKeyID = "";//平台ID
    public static String platformPubkey = "";//平台公钥
    public static String platformPubkeyPem= "";//平台公钥PEM格式

    public static String coreCompanyKeyID = "";//核心企业ID
    public static String coreCompanyPubkey = "";//核心企业公钥
    public static String coreCompanyPubkeyPem= "";//核心企业公钥PEM格式

    public static String PIN = "123";

    public static String AccountAddress = "";
    public static String PlatformAddress = "";
    public static String QFJGAddress = "";
    public static String ZJFAddress = "";
    public static String pubFormatSM2 = "sm2_pem";

    /**
    * base64解码
    */
    public String decodeBase64(String text) throws  Exception {

        Base64.Decoder decoder = Base64.getDecoder();

        final byte[] textByte = text.getBytes("UTF-8");

        String result = new String(decoder.decode(textByte));

        return result;
    }

}

