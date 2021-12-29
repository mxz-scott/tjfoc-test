package com.tjfintech.common.utils;

import com.tjfintech.common.PersonalTestEnvironment.*;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.tjfintech.common.utils.FileOperation.getSDKConfigValueByShell;
import static com.tjfintech.common.utils.UtilsClass.SDKADD;
import static net.sf.ezmorph.test.ArrayAssertions.assertEquals;

@Slf4j
public class UtilsClassTap {

    public static String sdkIP = SDKADD.substring(SDKADD.lastIndexOf("/") + 1, SDKADD.lastIndexOf(":"));
    public static String ORDERNO = "";

    public static String BID_DOC_REFER_END_TIME = constructTime(20000);
    public static String KAIBIAODATE = constructTime(30000);

    public static String ZBRPULICKEY = "MIIBiDCCAS+gAwIBAgIRAJQFtggQsKI+STPwDmHyQVowCgYIKoEcz1UBg3UwRTEL\n" +
            "MAkGA1UEChMCdGoxGDAWBgNVBAMTD3d1dG9uZ2NoYWluLmNvbTEPMA0GA1UEKhMG\n" +
            "R29waGVyMQswCQYDVQQGEwJOTDAeFw0yMTExMjEwODIwMTNaFw0zNjEyMDExNTU5\n" +
            "NTlaMDIxCTAHBgNVBAYTADELMAkGA1UEChMCdGoxGDAWBgNVBAMTD3d1dG9uZ2No\n" +
            "YWluLmNvbTBZMBMGByqGSM49AgEGCCqBHM9VAYItA0IABKBdWzEew67pCjSsaGco\n" +
            "T2rKUkuvMt8VLtx2Au75eUJMfV+e6OQmip20RAdPaUyrpUUQ60jW/l1l5LE0gFxR\n" +
            "wpijEzARMA8GA1UdIwQIMAaABAECAwQwCgYIKoEcz1UBg3UDRwAwRAIgQobo0RU4\n" +
            "VHBgIatSkshG6vqj1IAx4tdKs+bAp+KuVI0CIGFVxRsDiH992EAsSEKkDG5bE34t\n" +
            "qKzq7Rwgjq4cViJ/";

//    public static String ZBRPRIKEY = "MIGTAgEAMBMGByqGSM49AgEGCCqBHM9VAYItBHkwdwIBAQQgBKjyJ9LNeEufZiS6dj2mLBIs7IgoNCAnVOqWCn6aD2WgCgYIKoEcz1UBgi2hRANCAASgXVsxHsOu6Qo0rGhnKE9qylJLrzLfFS7cdgLu+XlCTH1fnujkJoqdtEQHT2lMq6VFEOtI1v5dZeSxNIBcUcKY";

    public static String ZBRPRIKEY =
            "-----BEGIN PRIVATE KEY-----\n" +
            "MIGTAgEAMBMGByqGSM49AgEGCCqBHM9VAYItBHkwdwIBAQQgBKjyJ9LNeEufZiS6\n" +
            "dj2mLBIs7IgoNCAnVOqWCn6aD2WgCgYIKoEcz1UBgi2hRANCAASgXVsxHsOu6Qo0\n" +
            "rGhnKE9qylJLrzLfFS7cdgLu+XlCTH1fnujkJoqdtEQHT2lMq6VFEOtI1v5dZeSx\n" +
            "NIBcUcKY\n" +
            "-----END PRIVATE KEY-----\n";


    public static String TENDER_PROJECT_CODE = constructData("PC");
    public static String TENDER_PROJECT_NAME = constructData("项目");
    public static String BID_SECTION_CODE = constructData("SC");
    public static String BID_SECTION_NAME = constructData("标段");
    public static String BID_SECTION_CODE_EX = constructData("SC_EX/+==");
    public static String UID = constructData("UID");

    public static Map EXTRANew = constructMetaDataMap("update");
    public static Map EXTRA = constructMetaDataMap("old");
    public static int TBALLOWFILESIZE = 512000;
    public static int TBALLOWFILESIZENew = 1024000;
    public static String TBFILE_ALLOWLIST = "jstf";
    public static String senderBidPlatform = "bidplatform";
    public static String senderFilePlatform = "fileplatform";
    public static String recordIdA = "tenderA" + UtilsClass.Random(8);
    public static String recordIdB = "tenderB" + UtilsClass.Random(8);
    public static String recordIdC = "tenderC" + UtilsClass.Random(8);
    public static String fileHead = "fileHead" + UtilsClass.Random(8);
    public static String path = "top/sub1/sub2/sub3";


    public static Map constructMetaDataMap(String data) {

        Map metaDataMap = new HashMap();
        metaDataMap.put("A", "aa");
        metaDataMap.put("B", "bb");
        metaDataMap.put("C", data);

        return metaDataMap;
    }

    public static String constructTime(int data) {

        String date = new SimpleDateFormat("yyyyMMddHHmmss").format(System.currentTimeMillis() + data);

        return date;
    }

    public static int constructUnixTime(int data) {

        Long time = System.currentTimeMillis() /1000 + data;
        int date = time.intValue();

        return date;
    }

    public static String constructData(String data) {

        String constructData = data + UtilsClass.Random(8);

        return constructData;
    }

}
