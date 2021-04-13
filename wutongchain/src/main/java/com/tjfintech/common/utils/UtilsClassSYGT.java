package com.tjfintech.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.*;

import static com.tjfintech.common.utils.UtilsClass.testDataPath;


@Slf4j
public class UtilsClassSYGT {

    public static String code1 = "m1" + UtilsClass.Random(6); //盟主1
    public static String code2 = "m2" + UtilsClass.Random(6); //盟主2
    public static String code3 = "m3" + UtilsClass.Random(6); //成员1

    public static String name1 = "m1" + UtilsClass.Random(6); //盟主1
    public static String name2 = "m2" + UtilsClass.Random(6); //盟主2
    public static String name3 = "m3" + UtilsClass.Random(6); //成员1

    public static String endPoint1 = "http://www.wutongchain1.com"; //盟主1
    public static String endPoint2 = "http://www.wutongchain1.com"; //盟主2
    public static String endPoint3 = "http://www.wutongchain1.com"; //成员1

    public static String account1 = "111111111"; //盟主1
    public static String account2 = "111111111"; //盟主2
    public static String account3 = "111111111"; //成员1

    public static String SDKURL1 = "http://222.93.106.197:38080"; //盟主1
    public static String SDKURL2 = "http://58.208.84.253:38080"; //盟主2
    public static String SDKURLm1 = "http://121.229.40.124:38080"; //成员1

    public static int memberJoinPoint = 500;
    public static int leaderJoinPoint = 10000;

    public static int platformPoint = 1;
    public static int effortPointType = 2;

}
