package com.tjfintech.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.*;

import static com.tjfintech.common.utils.UtilsClass.*;


@Slf4j
public class UtilsClassApp {

    //应用链 信息
    //10.1.3.161:7779
    public static String globalAppId1 = "5jl5q6n1vc";
    public static String globalAppId2 = "mfifxsnf06";
    public static String tempLedgerId = "";
    public static String tempLedgerId1 = "";
    public static String tempLedgerId2 = "";
    public static String glbChain01= "glbCh1";
    public static String glbChain02= "glbCh2";

    public static String id1 = getPeerId(PEER1IP,USERNAME,PASSWD);
    public static String id2 = getPeerId(PEER2IP,USERNAME,PASSWD);
    public static String id3 = getPeerId(PEER4IP,USERNAME,PASSWD);
    public static String id4 = getPeerId(PEER3IP,USERNAME,PASSWD);
    public static String ids = " -m "+ id1+","+ id2+","+ id3;
    public static String stateDestroyed = "off-time not support service";//"has been destroyed";
    public static String stateDestroyed2 ="has been destroyed";
    public static String notSupport="not support service";
    public static String stateFreezed ="has been frozen";



}
