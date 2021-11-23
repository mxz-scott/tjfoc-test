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
    public static String KAIBIAODATE = constructTime(20000);

    public static String ZBRPULICKEY = "";
    public static String ORDERNOSIGN = "";
    public static String orderNoSIGN = "";

    public static String TENDER_PROJECT_CODE = "PC" + UtilsClass.Random(8);
    public static String TENDER_PROJECT_NAME = "项目" + UtilsClass.Random(8);

    public static String BID_SECTION_CODE = "SC" + UtilsClass.Random(8);
    public static String BID_SECTION_NAME = "标段" + UtilsClass.Random(8);
    public static String BID_SECTION_CODE_EX = "SC_EX" + UtilsClass.Random(8);

    public static Map EXTRANew = constructMetaDataMap("update");
    public static Map EXTRA = constructMetaDataMap("old");
    public static int TBALLOWFILESIZE = 512000;
    public static int TBALLOWFILESIZENew = 1024000;
    public static String TBFILE_ALLOWLIST = "jstf";

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

    public static String constructTime(int data) {

        String date = new SimpleDateFormat("yyyyMMddHHmmss").format(System.currentTimeMillis() + data);

        return date;
    }

}
