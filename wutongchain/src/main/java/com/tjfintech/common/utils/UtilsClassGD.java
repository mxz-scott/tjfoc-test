package com.tjfintech.common.utils;

import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;

import java.util.*;



@Slf4j
public class UtilsClassGD {

    //gudeng 信息
//    public static String gdContractAddress = "faa54ef5e71e8f69cb30af684ae9f145b5c099ed4253a49fc1b144db1fe27f3c";
//    public static String gdPlatfromKeyID = "btguv5bsnk7r259qh85g";
    //10.1.3.161:7779
    public static String gdContractAddress = "018a976954474678a3a100d33200b7f9fd1b34a60118c4cb13cfd79f6f324b5d";
    public static String gdPlatfromKeyID = "bu4iruhpgflsbm0msddg";
    //股登164环境
//    public static String gdContractAddress = "48cb62af2f6363a5088264ce41193a362455b27fedb1d72eb512a7bfeb339523";
//    public static String gdPlatfromKeyID = "bu1qdg1pgfltc7no3hp0";
    //股登性能测试环境
//    public static String gdContractAddress = "e0618bd8a18d799e18d6716824e4b767e381d2ce58a49be20c89b48b53cff900";
//    public static String gdPlatfromKeyID = "bu2n8f52vf0as7lne720";
    //外网对接
//    public static String gdContractAddress = "48cb62af2f6363a5088264ce41193a362455b27fedb1d72eb512a7bfeb339523";
//    public static String gdPlatfromKeyID = "bu8kas9hdkef95op7i2g";
    public static String gdEquityCode = "gdECSZ00" + UtilsClass.Random(6);
    public static String gdCompanyID = "gdCmpyId0001" + UtilsClass.Random(6);
//    public static String gdCompanyID = "gdCmpyId0001";

    public static String gdAccClientNo1 = "No000" + UtilsClass.Random(10);
    public static String gdAccClientNo2 = "No100" + UtilsClass.Random(10);
    public static String gdAccClientNo3 = "No200" + UtilsClass.Random(10);
    public static String gdAccClientNo4 = "No300" + UtilsClass.Random(10);
    public static String gdAccClientNo5 = "No400" + UtilsClass.Random(10);
    public static String gdAccClientNo6 = "No500" + UtilsClass.Random(10);
    public static String gdAccClientNo7 = "No600" + UtilsClass.Random(10);
    public static String gdAccClientNo8 = "No700" + UtilsClass.Random(10);
    public static String gdAccClientNo9 = "No800" + UtilsClass.Random(10);
    public static String gdAccClientNo10 = "No900" + UtilsClass.Random(10);

    public static String gdAccount1,gdAccount2,gdAccount3,gdAccount4,gdAccount5,
                         gdAccount6,gdAccount7,gdAccount8,gdAccount9,gdAccount10;
    public static String gdAccountKeyID1,gdAccountKeyID2,gdAccountKeyID3,gdAccountKeyID4,gdAccountKeyID5,
                         gdAccountKeyID6,gdAccountKeyID7,gdAccountKeyID8,gdAccountKeyID9,gdAccountKeyID10;

    public static Map mapAccAddr = new HashMap<>(); //存放地址和客户号（账户对象标识 主体标识）

    public static Map registerInfo = new HashMap();//05登记 //发行 股份性质变更 过户转让 股份增发
    public static Map txInformation = new HashMap();//04交易报告  //过户转让
    public static Map enterpriseSubjectInfo = new HashMap();//01主体  //挂牌企业登记
    public static Map equityProductInfo = new HashMap();//03产品 股权类 //挂牌企业登记  股份增发 场内转板
    public static Map bondProductInfo = new HashMap();//03产品 债券类 //挂牌企业登记  股份发行
    public static Map fundProductInfo = new HashMap();//03产品 基金类 //挂牌企业登记  股份发行
    public static Map shAccountInfo = new HashMap();//02账户 股权账户 //投资者开户
    public static Map fundAccountInfo = new HashMap();//02账户 资金账户 //投资者开户
    public static Map investorSubjectInfo = new HashMap();//01主体  //投资者开户
    public static Map disclosureInfo = new HashMap();//07信披  //写入公告 信息披露
    public static Map settleInfo = new HashMap();//06资金结算  //资金结算
    public static List<Map> listRegInfo = new ArrayList<>();//主体信息列表
    public static String regNo = "regNo000000";


    public static long start = (new Date()).getTime();
    public static long end = 0;
    public static int beginHeigh = 0;
    public static int endHeight = 0;
	public static int blockHeight = 4892;
    public static long timeStamp = (new Date()).getTime();
    public static String testCurMethodName = "";

}
