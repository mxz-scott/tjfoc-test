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

    public static String userAddress1, userAddress2, userAddress3;
    public static String brokerAddress1, brokerAddress2, brokerAddress3;

    public static String YSPBH = constructData("YSPBH_", 8);
    public static int ShuLiang = 100;

    public static String userDetailInfo, brokerDetailInfo, artDetailInfo, artReviewDetailInfo, orderDetailInfo;

    public static String AddrNotInDB = "4AEeTzUkL8g2GN2kcK3GXWdv7nPyNjKR4hxJ5J96nFqxAGAHnB";
    public static String OPENID = "oTTwx448IcNzDkV-NjWc8LqhfOUw";

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
