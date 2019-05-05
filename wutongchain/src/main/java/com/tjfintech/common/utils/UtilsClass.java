package com.tjfintech.common.utils;

import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.Test;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.*;
import java.util.*;

import static com.tjfoc.utils.ReadFiletoByte.log;
import static org.junit.Assert.assertEquals;

@Slf4j
public class UtilsClass {
    public static final String SDKADD="http://10.1.3.165:3333";

    //SM2公私钥对
     public static String  ADDRESS1 = "4QqVU8DvcZNWQ7mAiuq8SFzZkhKW27PRAgo91Q716KqvK3jYxo";
     public static String  ADDRESS2 = "3UycKc8qvVWpVcBr3ipNqDC9oZPd86wj3qSJ6GMbLrVPgeqVwY";
     public static String  ADDRESS3 = "3r1vxdDjkg9uVke2YdaPTjmWVjV2bsVmySiU99hYuCUjLFYDFb";
    public static String  ADDRESS4 = "32XBduKk48RJfoA2wJuQ7doaJimMymEmZLYYbYHyH5Eyi5Hs68";
    public static String  ADDRESS5 = "4NbMaH4L4vrCfgExftQEWF2RFfBkwZqPoDdApqScLjTgZNC6gp";
//    public static String  ADDRESS6 = "4PMCowmBGWWLHed9wanTq4HmH4WZdsJr86i212zZbWRUtLD9gM";
//    public static String  ADDRESS7 = "4qTRGSuJqmq4HLZFUQvRx2MKKEwULzzEkf3GGXa5d3CYxivGXf";
    public static String  ADDRESS6 = "3tjENxmVEuAEaMDqgu6NnAnJpMkvMH6spb156NCydwfTBi2JZP";
    public static String  ADDRESS7 = "3zXrXd1UCZgsf9919j33T5Lh9t8YEWHYrTMsc7PzLckdBpB7SX";

     public static String  MULITADD1 ="SrvKGcQvu6ytWMPxTMkDMnpFmJyPaYbmAQEY71zuGaH7bzSAutr";//123
     public static String  MULITADD2 ="SsdcTnfMArqR6Yfon2UiLnSn1zEDkPdVxNGLkBNHKT2Rte3pM5n";//126
     public static String  MULITADD3 ="SqsV1UFpVQ3PVrxGLfjVTa4u1Xsz5PYT92C5Gav9vSF1xxFJqLS";//167
     public static String  MULITADD4 ="Snj8kGTdJy4qcj1ABNRK6cq7TEJqbjVw6xTR9VKQk5cKsSfbsss";//12
    public static String  MULITADD5 ="SnFXgygehAXLHjuhHciGJWnwd99TwC8pmAWhpiY6YXmdRRQDxzD";//13
    public static String  MULITADD6 ="SogNyzAxSByMtRZygvpkbu8V9PGswhqXGh2Hp5PoyQmdaepQt1i";//34
    public static String  MULITADD7 ="SoeNv1VcYqJBFST4Q7eNYzqh6WMmQkKGW37rfRYodYWZNFVSaav";//16
    public static String  IMPPUTIONADD="Soirv9ikykVHArKCdJqVNegxxqZWUj1g4ixFFYbBLMExy4zUTUe";//45

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
    public static Account account1 =new Account(ADDRESS1,PRIKEY1,PUBKEY1);
    public static Account account2 =new Account(ADDRESS2,PRIKEY2,PUBKEY2);
    public static Account account3 =new Account(ADDRESS3,PRIKEY3,PUBKEY3);
    public static MulitAccount mulitAccount1=new MulitAccount(MULITADD1,PUBKEY1,PUBKEY2,PUBKEY3,PRIKEY1,PRIKEY2,PRIKEY3);
    public static MulitAccount mulitAccount2=new MulitAccount(MULITADD2,PUBKEY1,PUBKEY2,PUBKEY6,PRIKEY1,PRIKEY2,PRIKEY6,PWD6);
    public static MulitAccount mulitAccount3=new MulitAccount(MULITADD3,PUBKEY1,PUBKEY6,PUBKEY7,PRIKEY1,PRIKEY6,PRIKEY7);
    public static MulitAccount mulitAccount4=new MulitAccount(MULITADD4,PUBKEY1,PUBKEY2,PRIKEY1,PRIKEY2);
    public static MulitAccount mulitAccount5=new MulitAccount(MULITADD5,PUBKEY1,PUBKEY3,PRIKEY1,PRIKEY3);
    public static MulitAccount mulitAccount6=new MulitAccount(MULITADD6,PUBKEY3,PUBKEY4,PRIKEY3,PRIKEY4);
    public static MulitAccount ImputationAccount=new MulitAccount(IMPPUTIONADD,PUBKEY4,PUBKEY5,PRIKEY4,PRIKEY5);

    public final static String PRIKEY1PATH = ".\\src\\main\\resources\\key\\key1.pem";
    public final static String PRIKEY2PATH = ".\\src\\main\\resources\\key\\key2.pem";
    public final static String PRIKEY3PATH = ".\\src\\main\\resources\\key\\key3.pem";
    public final static String PRIKEY4PATH = ".\\src\\main\\resources\\key\\key4.pem";
    public final static String PRIKEY5PATH = ".\\src\\main\\resources\\key\\key5.pem";
    public final static String PRIKEY6PATH = ".\\src\\main\\resources\\key\\key6.pem";
    public final static String PRIKEY7PATH = ".\\src\\main\\resources\\key\\key7.pem";
    public final static String PRIKEY8PATH = ".\\src\\main\\resources\\key\\key.pem";
    public final static String PUBKEY8 = ".\\src\\main\\resources\\key\\pubkey.pem";

    //add for diff type prikey & pubkey:SM2 ECDSA or RSA
    public static final String certPath="SM2";
    //public static final String certPath="SM2";
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
    public static String PEER1MAC="02:42:fc:a2:5b:1b";
    public static String PEER2MAC="02:42:c0:31:6b:5c";
    public static String PEER3MAC="02:42:c4:b9:82:6e";
    public static String PEER4MAC="02:42:dd:6c:4a:92";
    public static final String version="2.0_2.1_1904";
    public static final String USERNAME="root";
    public static final String PASSWD="root";
    //节点、SDK、Toolkit对等目录放置于PTPATH目录下
    public static final String PTPATH="/root/zll/permission/";
    public  static String SDKID=null;
    public static String PeerTPName="auto";
    public static ArrayList<String > peerList=new ArrayList<>();
    public static int RESTARTTIME=20000;

    public static String dockerFileName="simple.go";
    public static String fullPerm="[1 2 3 4 5 6 7 8 9 10 21 211 212 22 221 222 223 224 23 231 232 233 235 236 24 25 251 252 253 254 255 256]";



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

        shellSDK.execute("cd "+PTPATH+"toolkit;"+"./toolkit getid -p "+PTPATH+"sdk/auth/key.pem");

        ArrayList<String> stdout3 = shellSDK.getStandardOutput();
        for (String str1 : stdout3){
            if(str1.contains("id:"))
            {
                SDKID=str1.split(":")[1];
                break;
            }
        }
        log.info("SDK "+sdkIP+" ID:\n"+SDKID);
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

    public static void setAndRestartPeerList(String...cmdList)throws Exception{

        peerList.clear();
        peerList.add(PEER1IP);
        peerList.add(PEER2IP);
        peerList.add(PEER4IP);
        //重启节点集群
        for (String IP:peerList
        ) {
            Shell shellPeer=new Shell(IP,USERNAME,PASSWD);
            shellPeer.execute("ps -ef |grep " + PeerTPName +" |grep -v grep |awk '{print $2}'|xargs kill -9");
            for (String cmd:cmdList
            ) {
                shellPeer.execute(cmd);
                Thread.sleep(300);
            }
            Thread.sleep(500);
            shellPeer.execute("sh "+PTPATH+"peer/start.sh");
        }
        //重启sdk

        Thread.sleep(RESTARTTIME);
        resetAndRestartSDK();
        Thread.sleep(5000);
    }

    public static void resetAndRestartSDK()throws Exception{
        String sdkIP=SDKADD.substring(SDKADD.lastIndexOf("/")+1,SDKADD.lastIndexOf(":"));
        Shell shellSDK=new Shell(sdkIP,USERNAME,PASSWD);

        shellSDK.execute("ps -ef |grep httpservice |grep -v grep |awk '{print $2}'|xargs kill -9");
        shellSDK.execute("cp "+PTPATH+"sdk/conf/configOK.toml "+PTPATH+"sdk/conf/config.toml");

        shellSDK.execute("sh "+PTPATH+"sdk/start.sh");
    }

    public static String getKeyPairsFromFile(String pemFileName)throws Exception{
        String filePath = System.getProperty("user.dir") + "/src/main/resources/"+pemFileName;
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
}
