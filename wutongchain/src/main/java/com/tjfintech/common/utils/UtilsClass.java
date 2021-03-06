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

import static com.tjfintech.common.utils.FileOperation.*;
import static net.sf.ezmorph.test.ArrayAssertions.assertEquals;

@Slf4j
public class UtilsClass {

//    public static String subLedger = "w4ndmq24fx"; // 应用链ID
    public static String subLedger = "unpwj5v6k1";
//    public static String subLedger = "kffyrw4nxu";
//    public static String subLedger = "7jcg0bsoh5";
    public static String urlAddr = ""; // 向指定节点发请求 格式 IP:RPCPort
    public static String SDKADD = "http://222.93.105.78:52080";//sdk/tokenapi/中间件服务地址 http://IP:Port
    public static String TOKENADD = SDKADD;
    public static String rSDKADD = SDKADD;

    //设置测试环境使用的节点端口及部署目录信息
    public static String PEER1IP,PEER2IP,PEER3IP,PEER4IP = "10.1.0.163"; //各个节点服务器IP地址
    public static String PEER1RPCPort,PEER2RPCPort,PEER3RPCPort,PEER4RPCPort = "9009";//各个节点服务器RPC端口号
    public static String PEER1TCPPort,PEER2TCPPort,PEER3TCPPort,PEER4TCPPort = "60009";//各个节点服务器TCP端口号
    //节点、SDK、Toolkit对等目录放置于PTPATH目录下
    public static String PTPATH,SDKPATH,PeerPATH,ToolPATH,TokenApiPATH = "/root"; //各个服务部署目录
    public static String PeerTPName,SDKTPName,ToolTPName,TokenTPName = "wtchain";//各个服务进程名称
    public static String tmuxSessionPeer,tmuxSessionSDK,tmuxSessionTokenApi = "tmux send -t s ";//各个服务所在tmux会话进入命令
    public static String sReleaseLocalDir,sLatestLocalDir = "D:\\";//升级前后版本目录
    public static String destShellScriptDir = "/root/tjshell/";

    public static String envNo = "3";
    static{
        log.info("自定义环境参数");
        switch (envNo){
            case "1":  SetTestEnv_zxx envSet01 = new SetTestEnv_zxx(); envSet01.setEnvParam(); break;
            case "2":  SetTestEnv_lilu envSet02 = new SetTestEnv_lilu(); envSet02.setEnvParam(); break;
            case "3":  SetTestEnv_dyy envSet03 = new SetTestEnv_dyy(); envSet03.setEnvParam(); break;
            case "4":  SetTestEnv_hff envSet04 = new SetTestEnv_hff(); envSet04.setEnvParam(); break;
            case "5":  SetTestEnv_zllauto envSet05 = new SetTestEnv_zllauto(); envSet05.setEnvParam(); break;
            case "6":  SetTestEnv_zllManual envSet06 = new SetTestEnv_zllManual(); envSet06.setEnvParam(); break;
            case "7":  SetTestEnv_xlh envSet07 = new SetTestEnv_xlh(); envSet07.setEnvParam(); break;
            case "8":  SetTestEnv_edu envSet08 = new SetTestEnv_edu(); envSet08.setEnvParam(); break;

            default: log.info("使用初始化默认参数");
        }
    }

    public static Boolean syncFlag = false;
    public static int syncTimeout = 10;
    public static String testResultPath = "testresult/";
    public static String testDataPath = "testdata/";
    public static String srcShellScriptDir = testDataPath + "/configFiles/shell/";
    public static String tempWVMDir = testDataPath + "WVM/";
    public static String certPath = "cert/" + "SM2"; // 设置签名证书类型，可选值SM2(默认值)，ECDSA，MIX1，MIX2，RSA
    public static String globalResponse = "";
    public static String globalSSHPort = "";
    public static String peerTLSServerName = "tjfoc.com";
    public static String wvmVersion = "";

    public static String ipv4 = "/ip4/";
    public static String tcpProtocol = "/tcp/";

    public static Integer  SHORTMEOUT = 3*1000;//毫秒
    public static int  SLEEPTIME = 10*1000;
    public static int worldStateUpdTime = 1500;


    //UTXO精度
    public static Integer PRECISION = 6;

    //零地址/回收地址账户
    public static String zeroAccount = "0000000000000000";
    public static String ZEROADDRESS = "osEoy933LkHyyBcgjE7vCivfsX";
    //SM2公私钥对
    public static String  ADDRESS1, ADDRESS2, ADDRESS3, ADDRESS4, ADDRESS5, ADDRESS6, ADDRESS7;
    public static String  MULITADD1 ="";//123 (3/3签名)
    public static String  MULITADD2 ="";//126 (3/3签名)
    public static String  MULITADD3 ="";//167 (3/3签名)
    public static String  MULITADD4 ="";//12  (1/2签名)
    public static String  MULITADD5 ="";//13  (1/2签名)
    public static String  MULITADD6 ="";//34  (1/2签名)
    public static String  MULITADD7 ="";//16  (1/2签名)
    public static String  IMPPUTIONADD="";//45  (1/2签名)

    public static String PUBKEY1, PUBKEY2,PUBKEY3,PUBKEY4,PUBKEY5,PUBKEY6,PUBKEY7;
    public static String PRIKEY1, PRIKEY2,PRIKEY3,PRIKEY4,PRIKEY5,PRIKEY6,PRIKEY7;//私钥6,7带密码
    public static String PWD6="111";
    public static String PWD7="222";

    public static String tokenAccount1 = "";
    public static String tokenAccount2 = "";
    public static String tokenAccount3 = "";
    public static String tokenAccount4 = "";
    public static String tokenAccount5 = "";
    public static String tokenAccount6 = "";
    public static String tokenAccount7 = "";
    public static String tokenMultiAddr1 = "";//3/3账户
    public static String tokenMultiAddr2 = "";//1/2账户  tokenAccount1 tokenAccount2
    public static String tokenMultiAddr3 = "";//1/3账户  tokenAccount1 tokenAccount2
    public static String tokenMultiAddr4 = "";//1/2账户  tokenAccount2 tokenAccount3
    public static String tokenMultiAddr5 = "";//1/2账户  tokenAccount3 tokenAccount4
    public static String userId01 = "tkAc1" + Random(6);
    public static String userId02 = "tkAc2" + Random(6);
    public static String userId03 = "tkAc3" + Random(6);
    public static String userId04 = "tkAc4" + Random(6);
    public static String userId05 = "tkAc5" + Random(6);
    public static String userId06 = "tkAc6" + Random(6);
    public static String userId07 = "tkAc7" + Random(6);

    //add parameters for manage tool 环境变更有变更
    public static String PEER1MAC="";
    public static String PEER2MAC="";
    public static String PEER3MAC="";
    public static String PEER4MAC="";
    public static String version="3.0";
    public static String USERNAME="root";
    public static String PASSWD="root";
    public static String SDKID=null;
    public static String TOKENID=null;
    public static ArrayList<String > peerList=new ArrayList<>();
    public static int RESTARTTIME=20000;
    public static long ContractInstallSleep=75000;

    public static String dockerFileName= testDataPath + "docker/" + "simple.go";
    public static String fullPerm = "[1 2 3 4 5 6 7 8 9 10 11 21 22 23 24 25 26 27 211 212 221 222 223 224 226 227 228 231 232 233 235 236 251 252 253 254 255 256 261 262 270 271 272 281 282 283 284]";
    public static String fullPerm2 ="[1 2 3 4 5 6 7 8 9 10 11 21 22 23 24 25 26 27 211 212 221 222 223 224 226 227 228 231 232 233 235 236 251 252 253 254 255 256 261 262 270 271 272 281 282 283 284]";
    public static String PeerMemConfigPath = PeerPATH + "config.toml";//全文件名为config.toml 节点集群信息
    public static String PeerBaseConfigPath = PeerPATH + "conf/base.toml";//全文件名为base.toml 节点运行相关配置
    public static String SDKConfigPath = SDKPATH + "conf/base.toml";//全文件名为config.toml  3.1版本修改为base.toml
    public static String TokenApiConfigPath = TokenApiPATH + "conf/config_api.toml";//全文件名为config.toml

    public static String resourcePath = System.getProperty("user.dir") + "/src/main/resources/";

    //    public static String ccenvPull = "docker pull tjfoc/tjfoc-ccenv:2.1";
    public static String ccenvPull = "docker load < /root/dockerimages/ccenv_2.1.tar";//20191217出现网络慢pull需要很长时间 因此改回本地导入

    public static String startPeerCmd = "sh "+ destShellScriptDir +"startWithParam.sh \"" + tmuxSessionPeer + "\" " + PeerPATH + " " + PeerTPName;
    public static String startCPeerCmd = "sh "+ destShellScriptDir +"startWithParam.sh \"" + tmuxSessionPeer + "\" " + PeerPATH + " " + PeerTPName + " start -c";
    public static String startSDKCmd = "sh "+ destShellScriptDir +"startWithParam.sh \""+ tmuxSessionSDK + "\" " + SDKPATH + " " + SDKTPName;
    public static String startTokenApiCmd = "sh "+ destShellScriptDir +"startWithParam.sh \""+ tmuxSessionTokenApi + "\" " + TokenApiPATH + " " + TokenTPName  + " api";
    public static String killPeerCmd = "pkill " + PeerTPName;
    public static String killSDKCmd = "pkill " + SDKTPName;
    public static String killTokenApiCmd = "pkill " + TokenTPName;
    public static String clearPeerDB = "rm -rf "+ PeerPATH + "*db;rm -rf " + PeerPATH + "contracts";
    public static String clearPeerWVMsrc = "cd "+ PeerPATH + "contracts/src/;rm -rf *";
    public static String clearPeerWVMbin = "cd "+ PeerPATH + "contracts/bin/;ls |grep -v Sys_StoreEncrypted|xargs rm -rf ";
    public static String resetPeerBase = "cp " + PeerPATH + "conf/baseOK.toml " + PeerBaseConfigPath;
    public static String resetPeerConfig = "cp "+ PeerPATH + "configOK.toml "+ PeerMemConfigPath;
    public static String resetSDKConfig = "cp " + SDKPATH + "conf/configMysql.toml " + SDKConfigPath;
    public static String getPeerVerByShell = "cd " + PeerPATH + ";./"+ PeerTPName + " version| grep \"Peer Version\" |cut -d \":\" -f 2";
    public static String getSDKVerByShell = "cd " + SDKPATH + ";./"+ SDKTPName + " version| grep \"SDK Version\" |cut -d \":\" -f 2";
    public static String getTokenApiVerByShell = "cd " + TokenApiPATH + ";./"+ TokenTPName + " version| grep \"Version\" |cut -d \":\" -f 2";
    public static String getMgToolVerByShell = "cd " + ToolPATH + ";./"+ ToolTPName + " version| grep \"Tool Version\" |cut -d \":\" -f 2";

    public static Date dt = new Date();
    public static SimpleDateFormat sdf =new SimpleDateFormat("yyyyMMdd");

    public static int subTypeNo = 24;
    public static String release = "Release";
    public static String latest = "Latest";

    public static String SysContract = "Sys_StoreEncrypted";
    public static String releasePeer = PeerTPName + release;
    public static String releaseSDK = SDKTPName + release;
    public static String releaseContractSys = SysContract + release;
    public static String latestContractSys = SysContract + latest;
    public static String latestPeer = PeerTPName + latest;
    public static String latestSDK = SDKTPName + latest;
    public static String releaseTokenApi = TokenTPName + release;
    public static String latestTokenApi = TokenTPName + latest;
    public static String releaseMgTool = ToolTPName + release;
    public static String latestMgTool = ToolTPName + latest;
    public static Map<String,String> verMap = new HashMap<>();
    public static Map<String,String> beforeUpgrade = new HashMap<>();
    public static Map<String,String> afterUpgrade = new HashMap<>();
    public static ArrayList<String> txHashList = new ArrayList<>();
    public static String priStoreHash = "";
    public static String storeHash = "";

    public static String provider = "mysql";
    public static String mongoDBAddr ="\"\\\"mongodb:\\/\\/10.1.3.246:27017\\/ww22\"\\\"";
    public static String mysqlDBAddr = "\"\\\"root:root@tcp(10.1.3.246:3306)\\/wallet0703?charset=utf8\"\\\"";
    public static Map<String,String> mapledgerDockerName = new HashMap<>();


    public static String ledgerStateDestroy = "\"state\": 2";
    public static String ledgerStateFreeze = "\"state\": 1";
    public static String ledgerStateNormal = "\"state\": 0";
    public static String ledgerStateFreeze2 = "\"state\": Freeze";

    public static boolean bUpgradePeer = true;
    public static boolean bUpgradeSDK = true;
    public static boolean bUpgradeContractSys = true;
    public static boolean bUpgradeTokenApi = false; //3.0开始 合并版本 不单独部署api

    public static String sLocalPeer = "wtchain\\wtchain";
    public static String sLocalSDK = "wtsdk\\wtsdk";
    public static String sLocalTokenApi = "wtfinservice\\wtfinservice";
    public static String sLocalStoreContract = "wtchain\\contracts\\bin\\Sys_StoreEncrypted\\";
    public static String sLocalMgTool = "wttool\\wttool";

    public String sdkGetTxDetailType = "0";
    public String sdkGetTxDetailTypeV2 = "2";
    public String tokenApiGetTxDetailTType = "1";

    public String sdkGetTxHashType00 = "00";
    public String sdkGetTxHashType01 = "01";
    public String sdkGetTxHashType02 = "02";
    public String sdkGetTxHashType20 = "20";
    public String sdkGetTxHashType21 = "21";
    public String tokenApiGetTxHashType = "10";
    public String tokenApiGetTxHashTypeDesByType = "11";
    public String mgGetTxHashType = "mg";

    public static String  NoPermErrMsg = "no permission";
    public static String  NoPermErrCode = "500";

    public static String smartAccoutContractAddress = "";

    public static String IpAccountAddress = "";




    /**
     * token平台转账TOKEN数组构建方法
     * @param toAddr     接收地址
     * @param amount      数量
     * @return     返回TOKEN的LIST
     */
    public  List<Map>   smartConstuctIssueToList(String toAddr, String amount){

        Map<String,Object>amountMap=new HashMap<>();
        amountMap.put("address",toAddr);
        amountMap.put("amount",amount);

        List<Map>tokenList=new ArrayList<>();
        tokenList.add(amountMap);
        return tokenList;
    }



    /**
     * token平台转账TOKEN数组构建方法
     * @param toAddr     发送地址
     * @param amount      数量
     * @return     返回TOKEN的LIST
     */
    public  List<Map>   smartConstuctIssueToList(String toAddr,String amount, List<Map> list){
        List<Map>tokenList=new ArrayList<>();
        for(int i = 0 ; i < list.size() ; i++) {
            tokenList.add(list.get(i));
        }
        Map<String,Object>amountMap=new HashMap<>();
        amountMap.put("toAddr",toAddr);
        amountMap.put("amount",amount);

        tokenList.add(amountMap);
        return tokenList;
    }


    /**
     * token平台转账TOKEN数组构建方法
     * @param toAddr     发送地址
     * @param tokenType  币种
     * @param amount      数量
     * @return     返回TOKEN的LIST
     */
    public  List<Map>   tokenConstructToken(String toAddr, String tokenType, String amount){

        Map<String,Object>amountMap=new HashMap<>();
        amountMap.put("address",toAddr);
        amountMap.put("tokenType",tokenType);
        amountMap.put("amount",amount);

        List<Map>tokenList=new ArrayList<>();
        tokenList.add(amountMap);
        return tokenList;
    }

    /**
     * token平台转账TOKEN数组构建方法
     * @param toAddr     发送地址
     * @param tokenType  币种
     * @param amount      数量
     * @return     返回TOKEN的LIST
     */
    public  List<Map>   tokenConstructToken(String toAddr, String tokenType, String amount, List<Map> list){
        List<Map>tokenList=new ArrayList<>();
        for(int i = 0 ; i < list.size() ; i++) {
            tokenList.add(list.get(i));
        }
        Map<String,Object>amountMap=new HashMap<>();
        amountMap.put("address",toAddr);
        amountMap.put("tokenType",tokenType);
        amountMap.put("amount",amount);

        tokenList.add(amountMap);
        return tokenList;
    }

    /**
     * token平台转账utxo数组构建方法
     * @param hash     引用的交易hash
     * @param index    交易索引
     * @param amount   数量
     * @param address  受让方账户地址
     * @return     返回TOKEN的LIST
     */
    public  List<Map>   tokenConstructUTXO(String hash, int index, String amount, String address){

        Map<String,Object>amountMap=new HashMap<>();
        amountMap.put("hash",hash);
        amountMap.put("index",index);
        amountMap.put("amount",amount);
        amountMap.put("address",address);

        List<Map>tokenList=new ArrayList<>();
        tokenList.add(amountMap);
        return tokenList;
    }

    /**
     * token平台转账utxo数组构建方法
     * @param hash     引用的交易hash
     * @param index    交易索引
     * @param amount   数量
     * @param address  受让方账户地址
     * @return     返回TOKEN的LIST
     */
    public  List<Map>   tokenConstructUTXO(String hash, int index, String amount, String address, List<Map> list){
        List<Map>tokenList=new ArrayList<>();
        for(int i = 0 ; i < list.size() ; i++) {
            tokenList.add(list.get(i));
        }
        Map<String,Object>amountMap=new HashMap<>();
        amountMap.put("hash",hash);
        amountMap.put("index",index);
        amountMap.put("amount",amount);
        amountMap.put("address",address);

        tokenList.add(amountMap);
        return tokenList;
    }

    /**
     * 多签转账操作的TOKEN数组构建方法，单签的在GosoloSign类中
     * @param toAddr     发送地址
     * @param tokenType  币种
     * @param amount      数量
     * @return     返回TOKEN的LIST
     */
    public  List<Map>   constructToken(String toAddr, String tokenType, String amount){

        Map<String,Object>amountMap=new HashMap<>();
        amountMap.put("tokenType",tokenType);
        amountMap.put("amount",amount);

        List<Object>amountList=new ArrayList<>();
        amountList.add(amountMap);
        Map<String,Object>map=new HashMap<>();
        map.put("toAddress",toAddr);
        map.put("amountList",amountList);
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
        amountMap.put("tokenType",tokenType);
        amountMap.put("amount",amount);
        List<Map>amountList=new ArrayList<>();
        amountList.add(amountMap);
        Map<String,Object>map=new HashMap<>();
        map.put("toAddress",toAddr);
        map.put("amountList",amountList);
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
     * token平台回收utxo数组构建方法
     * @param hash     引用的交易hash
     * @param index    交易索引
     * @param amount   数量
     * @return     返回TOKEN的LIST
     */
    public  List<Map>   tokenConstrucDestroytUTXO(String hash, int index, String amount){

        Map<String,Object>amountMap=new HashMap<>();
        amountMap.put("hash",hash);
        amountMap.put("index",index);
        amountMap.put("amount",amount);

        List<Map>tokenList=new ArrayList<>();
        tokenList.add(amountMap);
        return tokenList;
    }

    /**
     * token平台回收utxo数组构建方法
     * @param hash     引用的交易hash
     * @param index    交易索引
     * @param amount   数量
     * @return     返回TOKEN的LIST
     */
    public  List<Map>   tokenConstrucDestroytUTXO(String hash, int index, String amount, List<Map> list){
        List<Map>tokenList=new ArrayList<>();
        for(int i = 0 ; i < list.size() ; i++) {
            tokenList.add(list.get(i));
        }
        Map<String,Object>amountMap=new HashMap<>();
        amountMap.put("hash",hash);
        amountMap.put("index",index);
        amountMap.put("amount",amount);

        tokenList.add(amountMap);
        return tokenList;
    }

    /**
     * 消息发送平台转账receiver数组构建方法
     * @param id     身份标识
     * @param pubkey  公钥
     * @return     返回TOKEN的LIST
     */
    public  List<Map>   constructReceiver(String id, String pubkey){

        Map<String,Object>receiverMap=new HashMap<>();
        receiverMap.put("id",id);
        receiverMap.put("pubkey",pubkey);

        List<Map>receiverList=new ArrayList<>();
        receiverList.add(receiverMap);
        return receiverList;
    }

    public  List<Map>   constructReceiver(String id, String pubkey,List<Map>list){
        List<Map>receiverList = new ArrayList<>();
        for (int i = 0; i<list.size(); i++){
            receiverList.add(list.get(i));
        }
        Map<String,Object>receiverMap=new HashMap<>();
        receiverMap.put("id",id);
        receiverMap.put("pubkey",pubkey);

        receiverList.add(receiverMap);
        return receiverList;
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
    public Map< String, Object> parseJSON2Map( String jsonStr){
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
    public StringBuilder readInput(String filePath) {
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

    public String encryptBASE64(byte[] key) throws Exception {
        return (new BASE64Encoder()).encodeBuffer(key);
    }


    public byte[] decryptBASE64(String key) throws Exception {
        return (new BASE64Decoder()).decodeBuffer(key);
    }
    public String getSDKID() {
        String sdkIP=SDKADD.substring(SDKADD.lastIndexOf("/")+1,SDKADD.lastIndexOf(":"));
        Shell shellSDK=new Shell(sdkIP,USERNAME,PASSWD);

//        shellSDK.execute("cd "+ ToolPATH+";./" + ToolTPName + " getid -p "+ SDKPATH +"auth/key.pem");
        shellSDK.execute("cd "+ SDKPATH+";./" + SDKTPName + " getid");

        ArrayList<String> stdout3 = shellSDK.getStandardOutput();
        for (String str1 : stdout3){
            if(str1.toLowerCase().contains("id:"))
            {
                SDKID=str1.split(":")[1];
                break;
            }
        }
        log.info("SDK " + sdkIP + " ID:\n" + SDKID);
        return SDKID.trim();
    }

    public static String SetURLExtParams(String param){
        if(syncFlag)  param = param + "&sync=true&timeout=" + syncTimeout;
        if (!subLedger.isEmpty()) param = param + "&ledger=" + subLedger;
        if (!urlAddr.isEmpty()) param = param + "&address=" + urlAddr;
        return param;
    }
    public String getToolID(String IP) {
        Shell shellSDK=new Shell(IP,USERNAME,PASSWD);
        String toolID ="";
        shellSDK.execute("cd "+ ToolPATH+";./" + ToolTPName + " getid -p "+ ToolPATH +"crypt/key.pem");
        ArrayList<String> stdout3 = shellSDK.getStandardOutput();
        for (String str1 : stdout3){
            if(str1.contains("id:"))
            {
                toolID=str1.split(":")[1];
                break;
            }
        }
        log.info("Tool " + IP + " ID:\n" + toolID);
        return toolID;
    }

    public String getMACAddr(String IP,String userName,String passWd) {
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
        String peerId="";
        shellPeer.execute("cd "+ PeerPATH + ";./" + PeerTPName + " id");
        ArrayList<String> stdout3 = shellPeer.getStandardOutput();
        for (String str1 : stdout3){
            if(str1.toLowerCase().contains("peerid"))
            {
                peerId=str1.substring(str1.indexOf(":")+1).trim();
                assertEquals(false, peerId.isEmpty());
                break;
            }
        }
        assertEquals(peerId.isEmpty(),false);
        log.info("IP "+IP+" with Id "+peerId);
        return peerId;
    }

    //该函数会让所有节点先执行同一个命令 再集群执行下一条命令
    public void sendCmdPeerList( ArrayList<String > peersList,String...cmdList)throws Exception{
        for(String cmd:cmdList){
            for (String IP:peersList) {
                Shell shellPeer = new Shell(IP, USERNAME, PASSWD);
                shellPeer.execute(cmd);
                Thread.sleep(200);
            }
        }
    }


    public void setAndRestartPeerList(String...cmdList)throws Exception{

        peerList.clear();
        peerList.add(PEER1IP);
        peerList.add(PEER2IP);
        peerList.add(PEER3IP);
        peerList.add(PEER4IP);

        //重启节点集群
        sendCmdPeerList(peerList,killPeerCmd);
        sendCmdPeerList(peerList,cmdList);

        peerList.remove(PEER3IP);

        sendCmdPeerList(peerList,startPeerCmd);

        Thread.sleep(SLEEPTIME);
    }
    public void setAndRestartPeer(String PeerIP,String...cmdList)throws Exception{

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


    public void setAndRestartSDK(String... cmdList)throws Exception{
        String sdkIP=SDKADD.substring(SDKADD.lastIndexOf("/")+1,SDKADD.lastIndexOf(":"));
        Shell shellSDK=new Shell(sdkIP,USERNAME,PASSWD);

        shellSDK.execute(killSDKCmd);

        for (String cmd:cmdList
        ) {
            shellSDK.execute(cmd);
            Thread.sleep(200);
        }

        shellSDK.execute(startSDKCmd);
        sleepAndSaveInfo(SLEEPTIME,"等待SDK重启");
    }

    public static void shellExeCmd(String IP, String... cmdList)throws Exception{
        Shell shellCmd=new Shell(IP,USERNAME,PASSWD);
        for (String cmd:cmdList
        ) {
            shellCmd.execute(cmd);
            Thread.sleep(200);
        }
    }

    public static void ExeToolCmdAndChk(String shellIP, String cmd, String chkStr)throws Exception{
        Shell shell1=new Shell(shellIP,USERNAME,PASSWD);
        String tempCmd = "cd "+ ToolPATH + ";" + cmd;
        shell1.execute(tempCmd);
        ArrayList<String> stdout = shell1.getStandardOutput();
        String response = StringUtils.join(stdout,"\n");
        log.info("\n" + response);
        Assert.assertEquals(response.contains(chkStr),true);
    }

    public String getKeyPairsFromFile(String pemFileName)throws Exception{
        String filePath = testDataPath + pemFileName;
        InputStream inStream =new FileInputStream(filePath);
        ByteArrayOutputStream out =new ByteArrayOutputStream();

        int ch;
        String res="";
        while ((ch = inStream.read())!= -1){
            out.write(ch);
        }
        byte[] result=out.toByteArray();

        res=(new BASE64Encoder()).encodeBuffer(result);
        //log.info(res.replaceAll("\r\n", ""));
        //assertEquals(res.replaceAll("\r\n", ""),manEncode);
        return res.replaceAll("\r\n", "");
    }

    public String getCertainPermissionList(String netPeerIP,String netRpcPort,String id)throws Exception{
        //权限更新后查询检查生效与否
        Shell shell1=new Shell(netPeerIP,USERNAME,PASSWD);
        if(!netRpcPort.contains(":")) netRpcPort = netPeerIP + ":" + netRpcPort;
        //String cmd1="cd zll;ls";//替换为权限命令
        String ledger = (subLedger != "") ? " -c " + subLedger : "";
        shell1.execute("cd "+ ToolPATH + ";./" + ToolTPName + " getpermission "+ ledger + " -p "+ netRpcPort);
        ArrayList<String> stdout = shell1.getStandardOutput();
        String resp = StringUtils.join(stdout,"\n");
        log.info(resp);
        int index = 0;
        boolean bflag = false;
        for(String line:stdout){
            if(line.contains(id.trim())) {
                log.info("permission contain id ok");
                bflag=true;
                break;
            }
            index++;
        }
        if( !bflag ) return "[0]";
        return stdout.get(index + 2).substring(stdout.get(index + 2).lastIndexOf(":") + 1).trim();
    }

    public String getIPFromStr(String src) {
        String IP ="";
        Pattern p = Pattern.compile("(?<=//|)((\\w)+\\.)+\\w+");
        Matcher matcher = p.matcher(src);
        if (matcher.find()) {
            IP = matcher.group();
        }
        return IP;
    }
    //仅返回一个匹配
    public static String getStrByReg(String src,String regPattern) {
        String matchStr = "";
        Pattern p = Pattern.compile(regPattern);
//        Pattern p = Pattern.compile("(?<=//|)\\/(\\w+)\\?(?<=//|)");
//        String src = "root:root@tcp(10.1.3.246:3306)/wallet22?charset=utf8";
        Matcher matcher = p.matcher(src);
        if (matcher.find()) {
            matchStr = matcher.group(1);
//            log.info("match info: " + matchStr);
        }
        return matchStr;
    }

    public String getSDKWalletDBConfig(){
        String sdkIP = getIPFromStr(SDKADD);
        String DBType=null;
        String database=null;
        String resp = getSDKConfigValueByShell(sdkIP,"Wallet","DBPath").replaceAll("\"","");
        assertEquals(false,resp.isEmpty());
        //提取IP地址
        log.info("*****" + resp);
        String IPString = getIPFromStr(resp);
        log.info("DB IP:" + IPString);

        if(resp.contains("mongo")) {
            //提取mongodb数据库地址及database信息
            DBType = "mongo";
            database = resp.substring(resp.lastIndexOf("/")+1);

        }else{
            DBType="mysql";
            database = resp.substring(resp.lastIndexOf(")/")+2, resp.lastIndexOf("?"));
        }

        return DBType + "," + IPString + "," + database;
    }

    public void delDataBase()throws Exception{
        String dbInfo = getSDKWalletDBConfig();

        if (dbInfo.contains("mongo")){
            MongoDBOperation mongo = new MongoDBOperation();
            mongo.delDatabase(dbInfo.split(",")[1],dbInfo.split(",")[2]);
        }
        else{
            MysqlOperation mysql = new MysqlOperation();
            mysql.delDatabase(dbInfo.split(",")[1],dbInfo.split(",")[2]);
        }

        Thread.sleep(3000);
    }


    public void delDataBase(String dbInfo)throws Exception{
        if (dbInfo.contains("mongo")){
            MongoDBOperation mongo = new MongoDBOperation();
            mongo.delDatabase(dbInfo.split(",")[1],dbInfo.split(",")[2]);
        }
        else{
            MysqlOperation mysql = new MysqlOperation();
            mysql.delDatabase(dbInfo.split(",")[1],dbInfo.split(",")[2]);
        }

        Thread.sleep(3000);
    }
    public static void sleepAndSaveInfo(long sleepTime,String...info)throws Exception{
        Thread.sleep(sleepTime);
        if(info.length >0) log.info(info[0] + "(ms): " + sleepTime);
        else log.info("********tx on chain waiting sleep time(ms): " + sleepTime);
    }

    public static String shExeAndReturn(String IP,String cmd){
        Shell shell1=new Shell(IP,USERNAME,PASSWD);
        shell1.execute(cmd);
        ArrayList<String> stdout = shell1.getStandardOutput();
        String response = StringUtils.join(stdout, "\n");
        log.info("\n" + response);
        return response;
    }

    /**
     * 读文件中的字符串
     *
     * @param filepath
     * @return
     */
    public String readStringFromFile(String filepath) {
        String str = "";
        File file = new File(filepath);
        try {
            FileInputStream in = new FileInputStream(file);
            // size 为字串的长度 ，这里一次性读完
            int size = in.available();
            byte[] buffer = new byte[size];
            in.read(buffer);
            in.close();
            str = new String(buffer, "utf-8");
        } catch (IOException e) {
            // TODO Auto-generated catch block

            e.printStackTrace();
            return null;
        }
        return str;
    }

    public String get6(double org){
        DecimalFormat df = new DecimalFormat("#.000000");
        String str = df.format(org);
        return str;
    }
}
