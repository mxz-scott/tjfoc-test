package com.tjfintech.common.utils;

import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.sf.ezmorph.test.ArrayAssertions.assertEquals;


@Slf4j
public class UtilsClass {
    public static final String SDKADD="http://10.1.3.240:7779";

    public static String certPath=""; //设置签名证书类型，需手动修改，可选值SM2，ECDSA，MIX1，MIX2，RSA
    //20190614 修改接口兼容主子链
    public static String subLedger="";

    public static Integer  LONGTIMEOUT = 100000;//毫秒
    public static Integer  SHORTMEOUT = 3000;//毫秒
    public static Integer  UTXOSHORTMEOUT = 4 * 1000;//毫秒
    public   final static int  SLEEPTIME=7*1000;

    //SM2公私钥对
     public static String  ADDRESS1 = "4QqVU8DvcZNWQ7mAiuq8SFzZkhKW27PRAgo91Q716KqvK3jYxo";
     public static String  ADDRESS2 = "3UycKc8qvVWpVcBr3ipNqDC9oZPd86wj3qSJ6GMbLrVPgeqVwY";
     public static String  ADDRESS3 = "3r1vxdDjkg9uVke2YdaPTjmWVjV2bsVmySiU99hYuCUjLFYDFb";
    public static String  ADDRESS4 = "32XBduKk48RJfoA2wJuQ7doaJimMymEmZLYYbYHyH5Eyi5Hs68";
    public static String  ADDRESS5 = "4NbMaH4L4vrCfgExftQEWF2RFfBkwZqPoDdApqScLjTgZNC6gp";
    public static String  ADDRESS6 = "3tjENxmVEuAEaMDqgu6NnAnJpMkvMH6spb156NCydwfTBi2JZP";
    public static String  ADDRESS7 = "3zXrXd1UCZgsf9919j33T5Lh9t8YEWHYrTMsc7PzLckdBpB7SX";


     public static String  MULITADD1 ="SrvKGcQvu6ytWMPxTMkDMnpFmJyPaYbmAQEY71zuGaH7bzSAutr";//123 (3/3签名)
     public static String  MULITADD2 ="SsdcTnfMArqR6Yfon2UiLnSn1zEDkPdVxNGLkBNHKT2Rte3pM5n";//126 (3/3签名)
     public static String  MULITADD3 ="SqsV1UFpVQ3PVrxGLfjVTa4u1Xsz5PYT92C5Gav9vSF1xxFJqLS";//167 (3/3签名)
     public static String  MULITADD4 ="Snj8kGTdJy4qcj1ABNRK6cq7TEJqbjVw6xTR9VKQk5cKsSfbsss";//12  (1/2签名)
     public static String  MULITADD5 ="SnFXgygehAXLHjuhHciGJWnwd99TwC8pmAWhpiY6YXmdRRQDxzD";//13  (1/2签名)
     public static String  MULITADD6 ="SogNyzAxSByMtRZygvpkbu8V9PGswhqXGh2Hp5PoyQmdaepQt1i";//34  (1/2签名)
     public static String  MULITADD7 ="SoeNv1VcYqJBFST4Q7eNYzqh6WMmQkKGW37rfRYodYWZNFVSaav";//16  (1/2签名)
     public static String  IMPPUTIONADD="Soirv9ikykVHArKCdJqVNegxxqZWUj1g4ixFFYbBLMExy4zUTUe";//45  (1/2签名)

    public static String PRIKEY1 = "LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0tCk1JR1RBZ0VBTUJNR0J5cUdTTTQ5QWdFR0NDcUJITTlWQVlJdEJIa3dkd0lCQVFRZ3FpSHZCN3ptbXZKSllFNUEKc0hTaHplMzlJT3pheVRYU3Erdjd6enJXaEJtZ0NnWUlLb0VjejFVQmdpMmhSQU5DQUFSL0w1RUJ6VXpxZk1pSQpHb2xyek8yYjJmbUZQUXNYRk5iWWQzVjFXOFNYSndhdkJRSi94OTBYSnR2VGFmcVRMQmRLVnFOWHYyNitFblhQCnVyaHM0Uy9RCi0tLS0tRU5EIFBSSVZBVEUgS0VZLS0tLS0K";
    public static String PRIKEY2 = "LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0tCk1JR1RBZ0VBTUJNR0J5cUdTTTQ5QWdFR0NDcUJITTlWQVlJdEJIa3dkd0lCQVFRZzBZYlhhUUd4bExyOGl1dm8KOEVsY1NnRFUvd3lRWTJQaDRxVVdxbG5JL1FhZ0NnWUlLb0VjejFVQmdpMmhSQU5DQUFUZjNrcUo1RnZLU0t1RgpWWlUyZzRFQTFXaHVNSVpkZGE0ejI1amhQcHh1U0xXMHVtYXVSamZ5SWpmZ1ZzV3FJanRGRDJOaEtOL1hQYjc1CkFkTkhCbnZwCi0tLS0tRU5EIFBSSVZBVEUgS0VZLS0tLS0K";
    public static String PRIKEY3 = "LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0tCk1JR1RBZ0VBTUJNR0J5cUdTTTQ5QWdFR0NDcUJITTlWQVlJdEJIa3dkd0lCQVFRZzhsakwvd1hjbkhYekRrc0QKVVp3M3l3MWJoZnptclFBZlIwV2VzclJDcnRlZ0NnWUlLb0VjejFVQmdpMmhSQU5DQUFTY3BsM1FhN1pGaGJicgp3U1Q0aWttaVNNVXdGQ2krZGVRRzUxTjNWRzBBc1hOU2RrakdKOWdPazIxNXlQblV1T3ZFN2tERU4vbDhOV0l4CkhWbTM1RXVCCi0tLS0tRU5EIFBSSVZBVEUgS0VZLS0tLS0K";
    public static String PRIKEY4 = "LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0tCk1JR1RBZ0VBTUJNR0J5cUdTTTQ5QWdFR0NDcUJITTlWQVlJdEJIa3dkd0lCQVFRZ3BRRjEzT01KaERQVVM3bnEKTVVYQUZNK01mUlV3MFc3bFVRQnNvOW12WWZ1Z0NnWUlLb0VjejFVQmdpMmhSQU5DQUFTTFR6QWxRSk1ZQ0RGegp6cURnL2s5TkhEUWpvL1R6WEFHRFpkaGJoOHU0c2loM2FvWUljWUsrN1VCbitBQVJJVDgwNVBySHNzTmRSWGc0CnM0bTgyRkNsCi0tLS0tRU5EIFBSSVZBVEUgS0VZLS0tLS0K";
    public static String PRIKEY5 = "LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0tCk1JR1RBZ0VBTUJNR0J5cUdTTTQ5QWdFR0NDcUJITTlWQVlJdEJIa3dkd0lCQVFRZ0RHd3RDcVpmWE1vY3Z2M28Kd3BNa1BiOWhKaVZTUmN2eXNMaVFnSXZOcmlHZ0NnWUlLb0VjejFVQmdpMmhSQU5DQUFUcnBoY3dNb1lZdG5aeQpueFV3cS9KUlVoZmkvU2JNUGE2WHE4S0lPS25KV3MyNy9ic0FsTGxDZWJMYUVOeE5GTDgrNSttSGxPQUZwRUx6Ck1VZ3dRN2NzCi0tLS0tRU5EIFBSSVZBVEUgS0VZLS0tLS0K";

    // 第67个公私钥带密码
    public static String PRIKEY6 = "LS0tLS1CRUdJTiBFTkNSWVBURUQgUFJJVkFURSBLRVktLS0tLQpNSUg4TUZjR0NTcUdTSWIzRFFFRkRUQktNQ2tHQ1NxR1NJYjNEUUVGRERBY0JBallnSG1VKzk1aHVnSUNDQUF3CkRBWUlLb1pJaHZjTkFnY0ZBREFkQmdsZ2hrZ0JaUU1FQVNvRUVDUnV2NytEcmt6dVdkV0FQa0djRHBFRWdhRGoKQW1JVFcwTlk5VFRPbUkvZG5Wb0RSRTJtN09ESGg5OGU3UDFwNlVDOFFaa1UvNld0dkVNVmh2QTNWWDh1THZXcgpEYlRoWUlNU25RMzZzOVM5SGRyWi8rOWw3UW55eG9xNVNSdTFoSndYUUhuRlBxVDg1RllBOHdnZytsdmlvTHRoCmZYQi9sQTFZSmtia0Z3ME5YSzYvY05PbDJVQUUzMStrc3hXV3Q5ZTh0SlFuZElISFpyVjZrblNvMDdFTk5CUHoKeTIyS1h2b2FKb3g4UUZ3UWVnVkcKLS0tLS1FTkQgRU5DUllQVEVEIFBSSVZBVEUgS0VZLS0tLS0K";
    public static String PRIKEY7 ="LS0tLS1CRUdJTiBFTkNSWVBURUQgUFJJVkFURSBLRVktLS0tLQpNSUg4TUZjR0NTcUdTSWIzRFFFRkRUQktNQ2tHQ1NxR1NJYjNEUUVGRERBY0JBaEk5Tzd4cVRmWmZBSUNDQUF3CkRBWUlLb1pJaHZjTkFnY0ZBREFkQmdsZ2hrZ0JaUU1FQVNvRUVFZGZUNDdrVDNBMElmWG80azJVRFRVRWdhQjkKSHVZZ0hiSzIxY3Vod2hwSFFLQndLVW1sMWhNK1VoeldoR3JKUThDcVBPRWFQaVZ0Zlgwdk9HL0RLY3UvZ3k3VwpRdjRnR0dSdXpucS83YjVLOEtmL01FREFlc01Kc1pRaENjQkhCUXYwVnlXekRCM0dVWHhhR2d0NFBuYTJOSDlzCi9MK2c1WUk4RVJaK1hLY0pSMVRkQllCblNocDJCamZpcEdHTGZpMW0zTjhxSWJYQUtXVnpZbmlQMUxVd01wSVUKZXVxVTI1VTdnWDRxSUFPQXFha2kKLS0tLS1FTkQgRU5DUllQVEVEIFBSSVZBVEUgS0VZLS0tLS0K";
    // 第67个公私钥带密码PWD6 PWD7
    public static String PUBKEY1 = "LS0tLS1CRUdJTiBQVUJMSUMgS0VZLS0tLS0KTUZrd0V3WUhLb1pJemowQ0FRWUlLb0VjejFVQmdpMERRZ0FFZnkrUkFjMU02bnpJaUJxSmE4enRtOW41aFQwTApGeFRXMkhkMWRWdkVseWNHcndVQ2Y4ZmRGeWJiMDJuNmt5d1hTbGFqVjc5dXZoSjF6N3E0Yk9FdjBBPT0KLS0tLS1FTkQgUFVCTElDIEtFWS0tLS0tCg==";
    public static String PUBKEY2 = "LS0tLS1CRUdJTiBQVUJMSUMgS0VZLS0tLS0KTUZrd0V3WUhLb1pJemowQ0FRWUlLb0VjejFVQmdpMERRZ0FFMzk1S2llUmJ5a2lyaFZXVk5vT0JBTlZvYmpDRwpYWFd1TTl1WTRUNmNia2kxdExwbXJrWTM4aUkzNEZiRnFpSTdSUTlqWVNqZjF6MisrUUhUUndaNzZRPT0KLS0tLS1FTkQgUFVCTElDIEtFWS0tLS0tCg==";
    public static String PUBKEY3 = "LS0tLS1CRUdJTiBQVUJMSUMgS0VZLS0tLS0KTUZrd0V3WUhLb1pJemowQ0FRWUlLb0VjejFVQmdpMERRZ0FFbktaZDBHdTJSWVcyNjhFaytJcEpva2pGTUJRbwp2blhrQnVkVGQxUnRBTEZ6VW5aSXhpZllEcE50ZWNqNTFManJ4TzVBeERmNWZEVmlNUjFadCtSTGdRPT0KLS0tLS1FTkQgUFVCTElDIEtFWS0tLS0tCg==";
    public static String PUBKEY4 = "LS0tLS1CRUdJTiBQVUJMSUMgS0VZLS0tLS0KTUZrd0V3WUhLb1pJemowQ0FRWUlLb0VjejFVQmdpMERRZ0FFaTA4d0pVQ1RHQWd4Yzg2ZzRQNVBUUncwSTZQMAo4MXdCZzJYWVc0Zkx1TElvZDJxR0NIR0N2dTFBWi9nQUVTRS9OT1Q2eDdMRFhVVjRPTE9Kdk5oUXBRPT0KLS0tLS1FTkQgUFVCTElDIEtFWS0tLS0tCg==";
    public static String PUBKEY5 = "LS0tLS1CRUdJTiBQVUJMSUMgS0VZLS0tLS0KTUZrd0V3WUhLb1pJemowQ0FRWUlLb0VjejFVQmdpMERRZ0FFNjZZWE1ES0dHTFoyY3A4Vk1LdnlVVklYNHYwbQp6RDJ1bDZ2Q2lEaXB5VnJOdS8yN0FKUzVRbm15MmhEY1RSUy9QdWZwaDVUZ0JhUkM4ekZJTUVPM0xBPT0KLS0tLS1FTkQgUFVCTElDIEtFWS0tLS0tCg==";
    public static String PUBKEY6 ="LS0tLS1CRUdJTiBQVUJMSUMgS0VZLS0tLS0KTUZrd0V3WUhLb1pJemowQ0FRWUlLb0VjejFVQmdpMERRZ0FFY0diN3R3UVREVHorTWQ3RmlkbitwaC8vcVFvTQpxSnpBcDJqU1RmZURVSkxLVUlKYXNzR0pVNEtJdUVleEszRFZ3K3RnMmpGT00vNnFVZnlnTmlZYmJnPT0KLS0tLS1FTkQgUFVCTElDIEtFWS0tLS0tCg==";//带密码
    public static String PUBKEY7 ="LS0tLS1CRUdJTiBQVUJMSUMgS0VZLS0tLS0KTUZrd0V3WUhLb1pJemowQ0FRWUlLb0VjejFVQmdpMERRZ0FFNFYxMXNmbGJpRE05RlpxK1FzMzk1RXRkZWVCMApHMDJVelM1eU5YMVlZb0pTU05FMDJWMkdTalhidTE4UGxuSmF6aFc0VnlUUzh5WjhuZUpQcXRNQ2hnPT0KLS0tLS1FTkQgUFVCTElDIEtFWS0tLS0tCg==";//带密码

    public static String PWD6="111";
    public static String PWD7="222";

    public static boolean bReg=false;
   //add parameters for manage tool
    public static final String PEER1IP="10.1.3.240";
    public static final String PEER2IP="10.1.3.246";
    public static final String PEER3IP="10.1.3.168";
    public static final String PEER4IP="10.1.3.247";
    public static final String PEER1RPCPort="9300";
    public static final String PEER2RPCPort="9300";
    public static final String PEER3RPCPort="9300";
    public static final String PEER4RPCPort="9300";
//    public static final String PEER1RPCPort="9400";
//    public static final String PEER2RPCPort="9500";
//    public static final String PEER3RPCPort="9400";
//    public static final String PEER4RPCPort="9400";
    public static final String PEER1TCPPort="60011";
    public static final String PEER2TCPPort="60011";
    public static final String PEER3TCPPort="60011";
    public static final String PEER4TCPPort="60012";
    public static String PEER1MAC="02:42:fc:a2:5b:1b";
    public static String PEER2MAC="02:42:c0:31:6b:5c";
    public static String PEER3MAC="02:42:c4:b9:82:6e";
    public static String PEER4MAC="02:42:dd:6c:4a:92";
    public static final String version="2.0";
    public static final String USERNAME="root";
    public static final String PASSWD="root";
    //节点、SDK、Toolkit对等目录放置于PTPATH目录下
    public static final String PTPATH="/root/zll/permission/";
//    public static final String PTPATH="/root/zll/chain2.0.1/";
    public static final String SDKPATH = PTPATH + "sdk/";
    public static final String PeerPATH = PTPATH + "peer/";
    public static final String ToolPATH = PTPATH + "toolkit/";
    public  static String SDKID=null;
    public static String PeerTPName="auto";
//    public static String PeerTPName="Mp";
    public static String SDKTPName="httpservice";
//    public static String SDKTPName="sdk";
    public static String ToolTPName="toolkit";
    public static ArrayList<String > peerList=new ArrayList<>();
    public static int RESTARTTIME=20000;
    public static long ContractInstallSleep=75000;

    public static String dockerFileName="simple.go";
    public static String fullPerm="[1 2 3 4 5 6 7 8 9 10 21 22 23 24 25 211 212 221 222 223 224 231 232 233 235 236 251 252 253 254 255 256]";
    public static String PeerMemConfig="config";//全文件名为config.toml 节点集群信息
    public static String PeerInfoConfig="base";//全文件名为base.toml 节点运行相关配置
    public static String SDKConfig="config";//全文件名为config.toml SDK配置信息

    public static String id1 = getPeerId(PEER1IP,USERNAME,PASSWD);
    public static String id2 = getPeerId(PEER2IP,USERNAME,PASSWD);
    public static String id3 = getPeerId(PEER4IP,USERNAME,PASSWD);
    public static String ids = " -m "+ id1+","+ id2+","+ id3;

    public static String startPeerCmd = "sh "+ PeerPATH +"start.sh";
    public static String startSDKCmd ="sh "+ SDKPATH +"start.sh";
    public static String killPeerCmd = "ps -ef |grep " + PeerTPName +" |grep -v grep |awk '{print $2}'|xargs kill -9";
    public static String killSDKCmd = "ps -ef |grep " + SDKTPName +" |grep -v grep |awk '{print $2}'|xargs kill -9";
    public static String clearPeerDB = "rm -rf "+ PeerPATH + "*db ";
    public static String resetPeerBase = "cp " + PeerPATH + "conf/baseOK.toml " + PeerPATH + "conf/base.toml";
    public static String resetPeerConfig = "cp "+ PeerPATH + "conf/configOK.toml "+ PeerPATH +"conf/"+PeerMemConfig+".toml";
    public static String resetSDKConfig = "cp " + SDKPATH + "conf/configOK.toml " + SDKPATH + "conf/config.toml";

    public static String resourcePath = System.getProperty("user.dir") + "/src/main/resources/";

    /**
     * 多签转账操作的TOKEN数组构建方法，单签的在GosoloSign类中
     * @param toAddr     发送地址
     * @param tokenType  币种
     * @param amount      数量
     * @return     返回TOKEN的LIST
     */
    public  List<Map>   constructToken(String toAddr, String tokenType, String amount){

        Map<String,Object>amountMap=new HashMap<>();
        amountMap.put("TokenType",tokenType);
        amountMap.put("Amount",amount);

        List<Object>amountList=new ArrayList<>();
        amountList.add(amountMap);
        Map<String,Object>map=new HashMap<>();
        map.put("ToAddr",toAddr);
        map.put("AmountList",amountList);
        List<Map>tokenList=new ArrayList<>();
        tokenList.add(map);
        return tokenList;
    }
    /**
     * 多签转账操作的TOKEN多数组构建方法。单签的在GosoloSign类中
     * @param toAddr     发送地址
     * @param tokenType  币种
     * @param amount      数量
     * @param list    之前的数组
     * @return     将多个数组添加在一起
     */
    public  List<Map>   constructToken(String toAddr, String tokenType, String amount, List<Map> list){
        List<Map>tokenList=new ArrayList<>();
        for(int i = 0 ; i < list.size() ; i++) {
            tokenList.add(list.get(i));
        }
        Map<String,Object>amountMap=new HashMap<>();
        amountMap.put("TokenType",tokenType);
        amountMap.put("Amount",amount);
        List<Map>amountList=new ArrayList<>();
        amountList.add(amountMap);
        Map<String,Object>map=new HashMap<>();
        map.put("ToAddr",toAddr);
        map.put("AmountList",amountList);
        tokenList.add(map);
        return tokenList;
    }


    /**
     * 回收操作的TOKEN多数组构建方法
     * @param fromAddr
     * @param pubKey
     * @param tokenType
     * @param amount
     * @param list
     * @return
     */
    public  List<Map>   constructToken(String fromAddr, String pubKey, String tokenType, String amount, List<Map> list){
        List<Map>tokenList=new ArrayList<>();
        for(int i = 0 ; i < list.size() ; i++) {
            tokenList.add(list.get(i));
        }

        Map<String,Object>map=new HashMap<>();
        map.put("Addr",fromAddr);
        map.put("PubKey",pubKey);
        map.put("Amount",amount);
        map.put("TokenType",tokenType);

        tokenList.add(map);
        return tokenList;

    }

    /**
     * 回收操作的TOKEN数组构建方法
     * @param fromAddr
     * @param pubKey
     * @param tokenType
     * @param amount
     * @return
     */
    public  List<Map>   constructToken(String fromAddr, String pubKey, String tokenType, String amount){

        Map<String,Object>map=new HashMap<>();
        map.put("Addr",fromAddr);
        map.put("PubKey",pubKey);
        map.put("Amount",amount);
        map.put("TokenType",tokenType);

        List<Map>tokenList=new ArrayList<>();
        tokenList.add(map);
        return tokenList;
    }

    /**
     * 用于生成随机数
     * @param length    随机数的长度
     * @return     返回由数字跟大小写字母组成的随机数
     */
     public final static String Random(int length) {
        char[] str= new char[length];
        int i = 0;
        int num=3;//数字的个数
        while (i < length) {
            int f = (int) (Math.random() * num);
            if (f == 0)
                str[i] = (char) ('A' + Math.random() * 26);
            else if (f == 1)
                str[i] = (char) ('a' + Math.random() * 26);
            else
                str[i] = (char) ('0' + Math.random() * 10);
            i++;
        }
        String random_str = new String(str);
        return random_str;
    }

    /**
     * json转map
     * @param jsonStr
     * @return
     */
    public static Map< String, Object> parseJSON2Map( String jsonStr){
        Map<String, Object> map = new HashMap< String, Object>();
        JSONObject json = JSONObject.fromObject(jsonStr);
        for(Object k : json.keySet()){
            Object v = json.get(k);
            if(v instanceof JSONArray){
                List<Map< String, Object>> list = new ArrayList<Map< String,Object>>();
                Iterator it = ((JSONArray)v).iterator();
                while(it.hasNext()){
                    Object json2 = it.next();
                    list.add(parseJSON2Map(json2.toString()));
                }
                map.put(k.toString(), list);
            } else {
                map.put(k.toString(), v);
            }
        }
        return map;
    }
    public static StringBuilder readInput(String filePath) {
        StringBuilder abc = new StringBuilder(" ");
        File file = new File(filePath);
        System.out.println(file.getPath());
        Reader reader = null;
        try {
            // 一次读一个字符
            reader = new InputStreamReader(new FileInputStream(file));
            int tempchar;
            while ((tempchar = reader.read()) != -1) {
                // 对于windows下，\r\n这两个字符在一起时，表示一个换行。
                // 但如果这两个字符分开显示时，会换两次行。
                // 因此，屏蔽掉\r，或者屏蔽\n。否则，将会多出很多空行。
                if (((char) tempchar) != '\r') {
                    abc.append((char) tempchar);
                    // System.out.print((char) tempchar);
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return abc;
        }

    }

    public static String encryptBASE64(byte[] key) throws Exception {
        return (new BASE64Encoder()).encodeBuffer(key);
    }


    public static byte[] decryptBASE64(String key) throws Exception {
        return (new BASE64Decoder()).decodeBuffer(key);
    }
    public static String getSDKID() {
        String sdkIP=SDKADD.substring(SDKADD.lastIndexOf("/")+1,SDKADD.lastIndexOf(":"));
        Shell shellSDK=new Shell(sdkIP,USERNAME,PASSWD);

        shellSDK.execute("cd "+ ToolPATH+";./" + ToolTPName + " getid -p "+ SDKPATH +"auth/key.pem");

        ArrayList<String> stdout3 = shellSDK.getStandardOutput();
        for (String str1 : stdout3){
            if(str1.contains("id:"))
            {
                SDKID=str1.split(":")[1];
                break;
            }
        }
        log.info("SDK " + sdkIP + " ID:\n" + SDKID);
        return SDKID;
    }

    public static String getMACAddr(String IP,String userName,String passWd) {
        Shell shellSDK=new Shell(IP,userName,passWd);
        String MACAddr=null;
        shellSDK.execute("ifconfig");
        ArrayList<String> stdout3 = shellSDK.getStandardOutput();
        for (String str1 : stdout3){
            if(str1.contains("HWaddr"))
            {
                MACAddr=str1.substring(str1.indexOf("HWaddr")+7);
                break;
            }
        }
        log.info("IP "+IP+" with MAC "+MACAddr);
        return MACAddr;
    }

    public static String getPeerId(String IP,String userName,String passWd) {
        Shell shellPeer=new Shell(IP,userName,passWd);
        String peerId=null;
        shellPeer.execute("cd "+ PeerPATH + ";./" + PeerTPName + " init");
        ArrayList<String> stdout3 = shellPeer.getStandardOutput();
        for (String str1 : stdout3){
            if(str1.contains("Local peer"))
            {
                peerId=str1.substring(str1.indexOf("peer:")+5).trim();
                break;
            }
        }
        log.info("IP "+IP+" with Id "+peerId);
        return peerId;
    }

    //该函数会让所有节点先执行同一个命令 再集群执行下一条命令
    public static void sendCmdPeerList( ArrayList<String > peersList,String...cmdList)throws Exception{
        for(String cmd:cmdList){
            for (String IP:peersList) {
                Shell shellPeer = new Shell(IP, USERNAME, PASSWD);
                shellPeer.execute(cmd);
                Thread.sleep(200);
            }
        }
    }


    public static void setAndRestartPeerList(String...cmdList)throws Exception{

        peerList.clear();
        peerList.add(PEER1IP);
        peerList.add(PEER2IP);
        peerList.add(PEER4IP);

        //重启节点集群
        sendCmdPeerList(peerList,killPeerCmd);
        sendCmdPeerList(peerList,cmdList);
        sendCmdPeerList(peerList,startPeerCmd);

        Thread.sleep(RESTARTTIME);
    }
    public static void setAndRestartPeer(String PeerIP,String...cmdList)throws Exception{

        Shell shellPeer=new Shell(PeerIP,USERNAME,PASSWD);
        shellPeer.execute(killPeerCmd);
        for (String cmd:cmdList
        ) {
            shellPeer.execute(cmd);
            Thread.sleep(100);
        }
        Thread.sleep(500);
        shellPeer.execute(startPeerCmd);


        Thread.sleep(RESTARTTIME);
    }


    public static void setAndRestartSDK(String... cmdList)throws Exception{
        String sdkIP=SDKADD.substring(SDKADD.lastIndexOf("/")+1,SDKADD.lastIndexOf(":"));
        Shell shellSDK=new Shell(sdkIP,USERNAME,PASSWD);

        shellSDK.execute(killSDKCmd);

        for (String cmd:cmdList
        ) {
            shellSDK.execute(cmd);
            Thread.sleep(200);
        }

        shellSDK.execute(startSDKCmd);
    }

    public static void shellExeCmd(String IP, String... cmdList)throws Exception{
        Shell shellCmd=new Shell(IP,USERNAME,PASSWD);
        for (String cmd:cmdList
        ) {
            shellCmd.execute(cmd);
            Thread.sleep(200);
        }
    }

    public static String getKeyPairsFromFile(String pemFileName)throws Exception{
        String filePath =resourcePath+pemFileName;
        InputStream inStream =new FileInputStream(filePath);
        ByteArrayOutputStream out =new ByteArrayOutputStream();

        //String manEncode="LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0tCk1JR1RBZ0VBTUJNR0J5cUdTTTQ5QWdFR0NDcUJITTlWQVlJdEJIa3dkd0lCQVFRZ3Y2R0NWb0NMcVp2SkpjYW4KRDQvMDRYUTF1WEJSZk80aHRNT3p6L2Q5VXFPZ0NnWUlLb0VjejFVQmdpMmhSQU5DQUFUQkdibGhmQVJIZDk0OApDYlYxUDkxT3ZyVmxKNHBtS21KcFZFLzFsQmcxS2kyZEtVOUMxK2xlTnVyM1hiZTliK3U1VDd0RUkrYWxDU0V5CkI2QXZSL1ZpCi0tLS0tRU5EIFBSSVZBVEUgS0VZLS0tLS0K";
        int ch;
        String res="";
        while ((ch = inStream.read())!= -1){
            out.write(ch);
        }
        byte[] result=out.toByteArray();

        res=(new BASE64Encoder()).encodeBuffer(result);
        log.info(res.replaceAll("\r\n", ""));
        //assertEquals(res.replaceAll("\r\n", ""),manEncode);
        return res.replaceAll("\r\n", "");
    }

    public static String getCertainPermissionList(String netPeerIP,String netRpcPort,String id)throws Exception{
        //权限更新后查询检查生效与否
        Shell shell1=new Shell(netPeerIP,USERNAME,PASSWD);
        //String cmd1="cd zll;ls";//替换为权限命令
        String ledger = (subLedger!="")?" -z "+subLedger:"";
        shell1.execute("cd "+ ToolPATH + ";./" + ToolTPName + " getpermission "+ ledger + " -p "+ netRpcPort);
        ArrayList<String> stdout = shell1.getStandardOutput();
        String resp = StringUtils.join(stdout,"\n");
        log.info(resp);
        int index=0;
        boolean bflag = false;
        for(String line:stdout){
            if(line.contains(id)) {
                log.info("permission contain id ok");
                bflag=true;
                break;
            }
            index++;
        }
        if( !bflag ) return "[0]";
        return stdout.get(index+2).substring(stdout.get(index+2).lastIndexOf(":")+1).trim();
    }

    public static String getIPFromStr(String src) {
        String IP ="";
        Pattern p = Pattern.compile("(?<=//|)((\\w)+\\.)+\\w+");
        Matcher matcher = p.matcher(src);
        if (matcher.find()) {
            IP = matcher.group();
        }
        return IP;
    }
    

    public static String getSDKWalletDBConfig() {
//        String sdkIP=SDKADD.substring(SDKADD.lastIndexOf("/")+1,SDKADD.lastIndexOf(":"));
        String sdkIP = getIPFromStr(SDKADD);
        Shell shellSDK=new Shell(sdkIP,USERNAME,PASSWD);
        String DBType=null;
        String database=null;
        shellSDK.execute("sh " + SDKPATH + "getDBPath.sh "+ SDKPATH); //即执行~/zll/chain2.0.1/sdk# ./getDBPath.sh /root/zll/chain2.0.1/sdk/
        ArrayList<String> stdout = shellSDK.getStandardOutput();
        String resp = StringUtils.join(stdout,"");

        assertEquals(false,resp.isEmpty());
        //提取IP地址
        log.info("*****"+resp);
        String IPString = getIPFromStr(resp);
        log.info("DB IP:" + IPString);

        if(resp.contains("mongo")) {
            //提取mongodb数据库地址及database信息
            DBType = "mongo";
            database = resp.substring(resp.lastIndexOf("/")+1);

        }else{
            DBType="mysql";
            database = resp.substring(resp.lastIndexOf("/")+1, resp.lastIndexOf("?"));
        }

        return DBType + "," + IPString + "," + database;
    }

    public static void delDataBase()throws Exception{
        String dbInfo = getSDKWalletDBConfig();

         if (dbInfo.contains("mongo")){
             MongoDBOperation mongo = new MongoDBOperation();
             mongo.mongoIP = dbInfo.split(",")[1];
             mongo.delDatabase(dbInfo.split(",")[2]);
         }
         else{
             MysqlOperation mysql = new MysqlOperation();
             mysql.mysqlIP = dbInfo.split(",")[1];
             mysql.delDatabase(dbInfo.split(",")[2]);
         }

        Thread.sleep(3000);
    }

    public static void sleepAndSaveInfo(long sleepTime)throws Exception{
        Thread.sleep(sleepTime);
        log.info("*************sleep time(ms): " + sleepTime);
    }
}
