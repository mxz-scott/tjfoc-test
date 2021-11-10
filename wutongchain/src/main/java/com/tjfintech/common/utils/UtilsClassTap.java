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
import static net.sf.ezmorph.test.ArrayAssertions.assertEquals;

@Slf4j
public class UtilsClassTap {


    public static String projectId = "";
    public static long expireDate = System.currentTimeMillis() / 1000 + 20;
    public static long openDate = System.currentTimeMillis() / 1000 + 20;
    public static String publicKey = "";
    public static String sign = "";
    public static String identity = "ZBF" + UtilsClass.Random(8);
    public static String name = "Project" + UtilsClass.Random(8);
    public static Map metaDataNew = constructMetaDataMap("update");
    public static Map metaData = constructMetaDataMap("old");
    public static int filesize = 512;
    public static int filesizeNew = 1024;
    public static int stateNormal = 0;
    public static int stateSuspend = 1;
    public static int stateAbortivebid = 2;

    public static String senderBidPlatform = "bidplatform";
    public static String senderFilePlatform = "fileplatform";

    public static String recordIdA = "tenderA" + UtilsClass.Random(8);
    public static String recordIdB = "tenderB" + UtilsClass.Random(8);
    public static String recordIdC = "tenderB" + UtilsClass.Random(8);

    public static String fileHead = "fileHead" + UtilsClass.Random(8);
    public static String path = "top/sub1/sub2/sub3";


    public static Map constructMetaDataMap(String data) {

        Map metaDataMap = new HashMap();
        metaDataMap.put("A", "aa");
        metaDataMap.put("B", "bb");
        metaDataMap.put("C", data);

        return metaDataMap;
    }

}
