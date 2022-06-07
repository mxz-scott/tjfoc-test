package com.tjfintech.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.SDKADD;

@Slf4j
public class UtilsClassIpcr {

    public static String USERKEYID1, USERKEYID2, USERKEYID3;
    public static String BROKERKEYID1, BROKERKEYID2, BROKERKEYID3;
    public static String PIN1, PIN2, PIN3;

    public static String USERADDRESS1, USERADDRESS2, USERADDRESS3;
    public static String BROKERADDRESS1, BROKERADDRESS2, BROKERADDRESS3;

    public static String SCADDRESS1, SCADDRESS2, SCADDRESS3;

    public static String INVALIDADDRESS = "4AEeTzUkL8g2GN2kcK3GXWdv7nPyNjKR4hxJ5J96nFqxAGAHnB";
    public static String OPENID = "oTTwx448IcNzDkV-NjWc8LqhfOUw";

    public static String BASEURL = "http://10.1.3.153:58086";

    public static String MALLURL = "http://10.1.3.224:8080";
    public static String MALLTOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VySW5mbyI6eyJpc1JlYWwiOjEsImNoYWluQWNjb3VudCI6IjM0aUI5UDlVTnFzeHVkRlZuMjNhN3FaYktSY1FRMzdHRlZlRkE5Y1h3MU5EQlJHZlZZIiwiaGVhZEltZ1VybCI6IiIsInJvbGVJZCI6NSwibmlja25hbWUiOiIiLCJpZCI6MjU0LCJpc09wZW5JZCI6MH0sImV4cCI6MTY1NTEzNDg1OX0.cqQWuqeChqC0Zs6d-cyntZ3-z3qpPFdnu0vOjR1TWQs";

    public static String BROKERNO = "1654478151069403";
    public static String SERIESID = "66";
    public static String ARTWORKNO = "1654478353467381";
    public static String SPREADCODE = "";
    public static String POINTCODE[] = {"ARR001", "ARR002"};
    public static int PENID = 14;

    public static int MAX = 100;
    public static int TYPENO = 5;

    public static String SYMBOL = constructData("SYMBOL-", 8);
    public static String ARTWORKID = String.valueOf(constructUnixTime(0));
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
