package com.tjfintech.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.*;

import static com.tjfintech.common.utils.UtilsClass.testDataPath;


@Slf4j
public class UtilsClassSYGT {

    public static String code1 = "BOS"; //盟主1
    public static String code2 = "GTJA"; //盟主2
    public static String code3 = "m3" + UtilsClass.Random(6); //成员1

    public static String name1 = "上海银行"; //盟主1
    public static String name2 = "国泰君安"; //盟主2
    public static String name3 = "m3" + UtilsClass.Random(6); //成员1

    public static String endPoint1 = "http://47.103.164.174:17700"; //盟主1
    public static String endPoint2 = "http://47.103.164.174:27700"; //盟主2
    public static String endPoint3 = "http://47.103.164.174:37700"; //成员1

    public static String account1 = "3PLyMde9qkEeAvFfxCgW7DGq3L9Cgc39A82CjhY3xQiLUpU1Tm"; //盟主1
    public static String account2 = "3zwDYB5AwELc2fUjEaUghCPSS91Px1mtrfXjfcD7jDpdgPdoxF"; //盟主2
    public static String account3 = "3i8P821hZd16AmB82MDdvGs8B7uN7e9hrsMTjg2Eiy1eN3MywX"; //成员1

    public static String SDKURL1 = "http://222.93.106.197:38080"; //盟主1
    public static String SDKURL2 = "http://58.208.84.253:38080"; //盟主2
    public static String SDKURLm1 = "http://121.229.40.124:38080"; //成员1

    public static int memberJoinPoint = 500;
    public static int leaderJoinPoint = 10000;

    public static String platformPoint = "1";
    public static String contributePointType = "2";

    public static String accStatusJoinApply = "New";
    public static String accStatusJoinReview = "JoinPending";
    public static String accStatusJoinSuccess = "Actived";
    public static String accStatusJoinReject = "JoinReject";
    public static String accStatusExitApply = "RemovePending";
    public static String accStatusExitSuccess = "Removed";

    public static Map mapMem = new HashMap();
    public static String joinDate = "";


}
