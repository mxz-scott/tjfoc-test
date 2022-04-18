package com.tjfintech.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.SDKADD;

@Slf4j
public class UtilsClassCopyright {

    public static String USERKEYID1, USERKEYID2, USERKEYID3;
    public static String BROKERKEYID1, BROKERKEYID2, BROKERKEYID3;
    public static String PIN1, PIN2, PIN3;

    public static String USERADDRESS1, USERADDRESS2, USERADDRESS3;
    public static String BROKERADDRESS1, BROKERADDRESS2, BROKERADDRESS3;

    public static String SCADDRESS1, SCADDRESS2, SCADDRESS3;

    public static String USERDETAILINFO, BROKERDETAILINFO, ARTDETAILINFO, ARTREVIEWDETAILINFO, ORDERDETAILINFO;

    public static String INVALIDADDRESS = "4AEeTzUkL8g2GN2kcK3GXWdv7nPyNjKR4hxJ5J96nFqxAGAHnB";
    public static String OPENID = "oTTwx448IcNzDkV-NjWc8LqhfOUw";

    public static String BASEURL = "http://10.1.3.153:58086";
    public static int MAX = 100;
    public static int TYPENO = 5;

    public static String SYMBOL = constructData("SYMBOL-", 8);
    public static String ARTWORKID =  String.valueOf(constructUnixTime(0));
    public static String ARTHASH = "ARTHASH";

    public static String ATTACH = "ATTACH";

    public static Map constructMetaDataMap(String data) {

        Map metaDataMap = new HashMap();
        metaDataMap.put("A", "aa");
        metaDataMap.put("B", "bb");
        metaDataMap.put("C", data);

        return metaDataMap;
    }

    public static String constructTime(int data) {

        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis() + data);

        return date;
    }

    public static int constructUnixTime(int data) {

        Long time = System.currentTimeMillis() / 1000 + data;
        int date = time.intValue();

        return date;
    }

    public static String constructData(String data, int length) {

        String constructData = data + UtilsClass.Random(length);

        return constructData;
    }

}
