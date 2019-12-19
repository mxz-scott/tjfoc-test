package com.tjfintech.common;

import com.sun.org.apache.xpath.internal.operations.Equals;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.Interface.Token;
import com.tjfintech.common.utils.UtilsClass;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.Test;

import java.util.*;

//import static com.java.tar.gz.FileUtil.log;
import static com.tjfintech.common.utils.FileOperation.*;
import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class CommonFunc {
    TestBuilder testBuilder= TestBuilder.getInstance();
    MultiSign multiSign =testBuilder.getMultiSign();
    SoloSign soloSign= testBuilder.getSoloSign();
    Token tokenModule= testBuilder.getToken();
    UtilsClass utilsClass=new UtilsClass();


    //-----------------------------------------------------------------------------------------------------------
    //token模块相关通用函数
    public  String tokenModule_IssueToken(String issueAddr,String collAddr,String amount){
        String issueToken = "tokenSo-"+ UtilsClass.Random(8);
        String comments = issueAddr + "向" + collAddr + " 发行token：" + issueToken + " 数量：" + amount;
        System.out.print(comments);
        tokenModule.tokenIssue(issueAddr,collAddr,issueToken,amount,comments);
        return issueToken;
    }

    public  String tokenModule_TransferToken(String from,String to, String tokenType,String amount){
        String comments = from + "向" + to + " 转账token：" + tokenType + " 数量：" + amount;
        System.out.print(comments);
        List<Map> list = utilsClass.tokenConstructToken(to,tokenType,amount);
        return tokenModule.tokenTransfer(from,comments,list);
    }

    public  String tokenModule_TransferTokenList(String from, List<Map> tokenList){
        String comments = from + "一转多";
        System.out.print(comments);
        return tokenModule.tokenTransfer(from,comments,tokenList);
    }

    public String tokenModule_DestoryToken(String addr,String tokenType,String amount){
        String comments = addr + "销毁token：" + tokenType + " 数量：" + amount;
        System.out.print(comments);
        List<Map> list = utilsClass.tokenConstructToken(addr,tokenType,amount);
        return tokenModule.tokenDestoryByList(list,comments);
    }

    public String tokenModule_DestoryTokenByList2(List<Map> list){
        return tokenModule.tokenDestoryByList(list,"destory by token list");
    }

    public String tokenModule_DestoryTokenByTokenType(String tokenType){
        String comments = "destory tokentype " + tokenType;
        return tokenModule.tokenDestoryByTokenType(tokenType,comments);
    }

    //-----------------------------------------------------------------------------------------------------------
    //sdk相关函数封装
    public  String sdkMultiIssueToken(String issueAddr, String  amount, String ToAddr,
                                      ArrayList<String> priKeys, ArrayList<String> pwds){
        String tokenType = "MUCX-" + UtilsClass.Random(8);
        String data = issueAddr + "发行" + tokenType + " token，数量为：" + amount;
        System.out.print(data);
        String response = multiSign.issueToken(issueAddr,ToAddr,tokenType, amount, data);
        assertThat(response, containsString("200"));
        String Tx1 = JSONObject.fromObject(response).getJSONObject("Data").getString("Tx");
        System.out.print("第一次签名");
        String tx = Tx1;
        for(int i=0;i<priKeys.size();i++) {
            String response2 = "";
            if(pwds.get(i).isEmpty())
                response2 = multiSign.Sign(tx, priKeys.get(i));
            else
                response2 = multiSign.Sign(tx,priKeys.get(i),pwds.get(i));
            assertEquals("200", JSONObject.fromObject(response2).getString("State"));
            if(JSONObject.fromObject(response2).getJSONObject("Data").getString("IsCompleted").contains("true")) break;
            else
                tx = JSONObject.fromObject(response2).getJSONObject("Data").getString("Tx");
        }
        return tokenType;
    }

    public String sdkSoloIssueToken(String Prikey,String amount,String toAddr)throws Exception{
        String tokenType = "SOLOTC-"+UtilsClass.Random(6);
        String data = "Prikey issue token: " + tokenType + "*" + amount + "to " + toAddr;
        System.out.print(data);
        String isResult= soloSign.issueToken(Prikey,tokenType,amount,data,toAddr);
        assertEquals("200",JSONObject.fromObject(isResult).getString("State"));
        return tokenType;
    }

    //-----------------------------------------------------------------------------------------------------------
    //结果确认辅助函数 回收结果list map for 回收bytoken
    /***
     * 返回信息中的结果确认
     * @param address
     * @param amount
     * @return
     */
    public List<Map> ConstructDesByTokenRespList(String address, String amount){
        List<Map> tokenList = new ArrayList<>();
        Map<String,Object> amountMap = new LinkedHashMap<>();
        amountMap.put("\"address\"","\"" + address + "\"");
        amountMap.put("\"amount\"","\"" + amount + "\"");

        tokenList.add(amountMap);
        return tokenList;
    }
    /***
     * 返回信息中的结果确认
     * @param address
     * @param amount
     * @param list
     * @return
     */
    public List<Map> ConstructDesByTokenRespList(String address, String amount, List<Map> list){
        List<Map> tokenList = new ArrayList<>();
        for(int i = 0 ; i < list.size() ; i++) {
            tokenList.add(list.get(i));
        }
        Map<String,Object> amountMap = new LinkedHashMap<String, Object>();
        amountMap.put("\"address\"","\"" + address + "\"");
        amountMap.put("\"amount\"","\"" + amount + "\"");


        tokenList.add(amountMap);
        return tokenList;
    }


    /***
     * 返回信息中的结果确认
     * @param from
     * @param to
     * @param tokenType
     * @param amount
     * @return
     */
    public static List<Map> constructUTXOTxDetailList(String from, String to, String tokenType, String amount){
        List<Map> tokenList = new ArrayList<>();
        Map<String,Object> amountMap = new LinkedHashMap<>();

        amountMap.put("\"From\"","\"" + from + "\"");
        amountMap.put("\"To\"","\"" + to + "\"");
        amountMap.put("\"TokenType\"","\"" + tokenType + "\"");
        amountMap.put("\"Amount\"","\"" + amount + "\"");

        tokenList.add(amountMap);
        return tokenList;
    }
    /***
     * 返回信息中的结果确认
     * @param from
     * @param to
     * @param tokenType
     * @param amount
     * @param list
     * @return
     */
    public static List<Map> constructUTXOTxDetailList(String from, String to, String tokenType, String amount, List<Map> list){
        List<Map>tokenList=new ArrayList<>();
        for(int i = 0 ; i < list.size() ; i++) {
            tokenList.add(list.get(i));
        }
        Map<String,Object>amountMap=new LinkedHashMap<>();

        amountMap.put("\"From\"","\"" + from + "\"");
        amountMap.put("\"To\"","\"" + to + "\"");
        amountMap.put("\"TokenType\"","\"" + tokenType + "\"");
        amountMap.put("\"Amount\"","\"" + amount + "\"");

        tokenList.add(amountMap);
        return tokenList;
    }


    public static boolean checkListArray(List<Map> list, JSONArray jsonArray){
        boolean bResult = true;

        for(int i= 0;i<list.size();i++){
            String checkStr = list.get(i).toString().replaceAll("=",":").replaceAll(" ","");
            boolean bMatch = false;
            System.out.print("+++++++++++++++++" + checkStr);
            System.out.print("+++++++++++++++++" + jsonArray.toString());
            for(int j = 0;j < jsonArray.size(); j++){
                if(checkStr.equals(jsonArray.get(j).toString())){
                    bMatch = true;
                    jsonArray.remove(j);
                    break;
                }
            }
            bResult = bResult && bMatch;
            assertEquals(true,bMatch);
        }
        System.out.print("matching complete.............");
        return bResult;
    }

    //----------------------------------------------------------------------------------------------------------//
    //以下为对配置文件修改 通过shell脚本方式
    //此部分读写不支持重复section的内容 例如 peer config.toml中会有多个 [[Members.Peers]]
    //此部分读写不支持重复section的内容 例如 sdk conf/config.toml中会有多个 [[Peers]]

    //读取sdk配置文件中指定配置项信息
    public static boolean getSDKWalletEnabled(){
        boolean bEnabled = true;
        String resp = getSDKConfigValueByShell(getIPFromStr(SDKADD),"Wallet","Enabled");
        if(resp.contains("true")) bEnabled = true;
        else if(resp.contains("false")) bEnabled = false;
        return bEnabled;
    }


    public static void setSDKTLSCertECDSA(String SDKIP)throws Exception{
        setSDKConfigByShell(SDKIP,"Rpc","TLSCaPath","\"\\\".\\/ecdsa\\/ca.pem\"\\\"");
        setSDKConfigByShell(SDKIP,"Rpc","TLSCertPath","\"\\\".\\/ecdsa\\/cert.pem\"\\\"");
        setSDKConfigByShell(SDKIP,"Rpc","TLSKeyPath","\"\\\".\\/ecdsa\\/key.pem\"\\\"");

        assertEquals(true,getSDKConfigValueByShell(SDKIP,"Rpc","TLSCaPath").trim().contains("./ecdsa/ca.pem"));
        assertEquals(true,getSDKConfigValueByShell(SDKIP,"Rpc","TLSCertPath").trim().contains("./ecdsa/cert.pem"));
        assertEquals(true,getSDKConfigValueByShell(SDKIP,"Rpc","TLSKeyPath").trim().contains("./ecdsa/key.pem"));
    }

    public static void setSDKTLSCertExpired(String SDKIP)throws Exception{
        setSDKConfigByShell(SDKIP,"Rpc","TLSCaPath","\"\\\".\\/expired\\/ca.pem\"\\\"");
        setSDKConfigByShell(SDKIP,"Rpc","TLSCertPath","\"\\\".\\/expired\\/cert.pem\"\\\"");
        setSDKConfigByShell(SDKIP,"Rpc","TLSKeyPath","\"\\\".\\/expired\\/key.pem\"\\\"");

        assertEquals(true,getSDKConfigValueByShell(SDKIP,"Rpc","TLSCaPath").trim().contains("./expired/ca.pem"));
        assertEquals(true,getSDKConfigValueByShell(SDKIP,"Rpc","TLSCertPath").trim().contains("./expired/cert.pem"));
        assertEquals(true,getSDKConfigValueByShell(SDKIP,"Rpc","TLSKeyPath").trim().contains("./expired/key.pem"));
    }

    public static void setSDKTLSCertDiffCa(String SDKIP)throws Exception{
        setSDKConfigByShell(SDKIP,"Rpc","TLSCaPath","\"\\\".\\/diffca\\/ca.pem\"\\\"");
        setSDKConfigByShell(SDKIP,"Rpc","TLSCertPath","\"\\\".\\/diffca\\/cert.pem\"\\\"");
        setSDKConfigByShell(SDKIP,"Rpc","TLSKeyPath","\"\\\".\\/diffca\\/key.pem\"\\\"");

        assertEquals(true,getSDKConfigValueByShell(SDKIP,"Rpc","TLSCaPath").trim().contains("./diffca/ca.pem"));
        assertEquals(true,getSDKConfigValueByShell(SDKIP,"Rpc","TLSCertPath").trim().contains("./diffca/cert.pem"));
        assertEquals(true,getSDKConfigValueByShell(SDKIP,"Rpc","TLSKeyPath").trim().contains("./diffca/key.pem"));
    }

    public static void setSDKTLSCertDismatch(String SDKIP)throws Exception{
        setSDKConfigByShell(SDKIP,"Rpc","TLSCaPath","\"\\\".\\/dismatch\\/ca.pem\"\\\"");
        setSDKConfigByShell(SDKIP,"Rpc","TLSCertPath","\"\\\".\\/dismatch\\/cert.pem\"\\\"");
        setSDKConfigByShell(SDKIP,"Rpc","TLSKeyPath","\"\\\".\\/dismatch\\/key.pem\"\\\"");

        assertEquals(true,getSDKConfigValueByShell(SDKIP,"Rpc","TLSCaPath").trim().contains("./dismatch/ca.pem"));
        assertEquals(true,getSDKConfigValueByShell(SDKIP,"Rpc","TLSCertPath").trim().contains("./dismatch/cert.pem"));
        assertEquals(true,getSDKConfigValueByShell(SDKIP,"Rpc","TLSKeyPath").trim().contains("./dismatch/key.pem"));
    }

    public static void setSDKCryptKeyType(String SDKIP,String keytype)throws Exception{
        setSDKConfigByShell(SDKIP,"Rpc","KeyType","\"\\\"" + keytype + "\"\\\"");
        assertEquals(true,getSDKConfigValueByShell(SDKIP,"Rpc","KeyType").trim().contains(keytype));
    }

    public static void setSDKCryptHashType(String SDKIP,String hashtype)throws Exception{
        setSDKConfigByShell(SDKIP,"Rpc","HashType","\"\\\""+ hashtype + "\"\\\"");
        assertEquals(true,getSDKConfigValueByShell(SDKIP,"Rpc","HashType").trim().contains(hashtype));

    }

    public static void setSDKWalletEnabled(String SDKIP, String flag)throws Exception{
        setSDKConfigByShell(SDKIP,"Wallet","Enabled",flag);
        assertEquals(true,getSDKConfigValueByShell(SDKIP,"Wallet","Enabled").trim().contains(flag));
    }

    public static void setSDKWalletAddrDBMysql(String SDKIP)throws Exception{
        setSDKConfigByShell(SDKIP,"Wallet","Provider","\"\\\"mysql\"\\\"");
        setSDKConfigByShell(SDKIP,"Wallet","DBPath",mysqlDBAddr);
        setSDKConfigByShell(SDKIP,"AddrService","Provider","\"\\\"mysql\"\\\"");
        setSDKConfigByShell(SDKIP,"AddrService","DBPath",mysqlDBAddr);

        String checkMyqlDBAddr = mysqlDBAddr.replaceAll("\"","").replaceAll("\\\\","");
//        System.out.print(checkMyqlDBAddr);

        assertEquals(true,getSDKConfigValueByShell(SDKIP,"Wallet","Provider").trim().contains("mysql"));
        assertEquals(true,getSDKConfigValueByShell(SDKIP,"Wallet","DBPath").trim().contains(checkMyqlDBAddr));
        assertEquals(true,getSDKConfigValueByShell(SDKIP,"AddrService","Provider").trim().contains("mysql"));
        assertEquals(true,getSDKConfigValueByShell(SDKIP,"AddrService","DBPath").trim().contains(checkMyqlDBAddr));
    }

    public static void setSDKWalletDBMysqlAddrDBMongo(String SDKIP)throws Exception{
        setSDKConfigByShell(SDKIP,"Wallet","Provider","\"\\\"mysql\"\\\"");
        setSDKConfigByShell(SDKIP,"Wallet","DBPath",mysqlDBAddr);
        setSDKConfigByShell(SDKIP,"AddrService","Provider","\"\\\"mongodb\"\\\"");
        setSDKConfigByShell(SDKIP,"AddrService","DBPath",mongoDBAddr);

        String checkMongoDBAddr = mongoDBAddr.replaceAll("\"","").replaceAll("\\\\","");
        String checkMyqlDBAddr = mysqlDBAddr.replaceAll("\"","").replaceAll("\\\\","");

        assertEquals(true,getSDKConfigValueByShell(SDKIP,"Wallet","Provider").trim().contains("mysql"));
        assertEquals(true,getSDKConfigValueByShell(SDKIP,"Wallet","DBPath").trim().contains(checkMyqlDBAddr));
        assertEquals(true,getSDKConfigValueByShell(SDKIP,"AddrService","Provider").trim().contains("mongodb"));
        assertEquals(true,getSDKConfigValueByShell(SDKIP,"AddrService","DBPath").trim().contains(checkMongoDBAddr));
    }

    public static void setSDKWalletDBMongoAddrDBMysql(String SDKIP)throws Exception{
        setSDKConfigByShell(SDKIP,"Wallet","Provider","\"\\\"mongodb\"\\\"");
        setSDKConfigByShell(SDKIP,"Wallet","DBPath",mongoDBAddr);
        setSDKConfigByShell(SDKIP,"AddrService","Provider","\"\\\"mysql\"\\\"");
        setSDKConfigByShell(SDKIP,"AddrService","DBPath",mysqlDBAddr);

        String checkMongoDBAddr = mongoDBAddr.replaceAll("\"","").replaceAll("\\\\","");
        String checkMyqlDBAddr = mysqlDBAddr.replaceAll("\"","").replaceAll("\\\\","");

        assertEquals(true,getSDKConfigValueByShell(SDKIP,"Wallet","Provider").trim().contains("mongodb"));
        assertEquals(true,getSDKConfigValueByShell(SDKIP,"Wallet","DBPath").trim().contains(checkMongoDBAddr));
        assertEquals(true,getSDKConfigValueByShell(SDKIP,"AddrService","Provider").trim().contains("mysql"));
        assertEquals(true,getSDKConfigValueByShell(SDKIP,"AddrService","DBPath").trim().contains(checkMyqlDBAddr));
    }

    public static void setSDKWalletAddrDBMongo(String SDKIP)throws Exception{
        setSDKConfigByShell(SDKIP,"Wallet","Provider","\"\\\"mongodb\"\\\"");
        setSDKConfigByShell(SDKIP,"Wallet","DBPath",mongoDBAddr);
        setSDKConfigByShell(SDKIP,"AddrService","Provider","\"\\\"mongodb\"\\\"");
        setSDKConfigByShell(SDKIP,"AddrService","DBPath",mongoDBAddr);

        String checkMongoDBAddr = mongoDBAddr.replaceAll("\"","").replaceAll("\\\\","");

        assertEquals(true,getSDKConfigValueByShell(SDKIP,"Wallet","Provider").trim().contains("mongodb"));
        assertEquals(true,getSDKConfigValueByShell(SDKIP,"Wallet","DBPath").trim().contains(checkMongoDBAddr));
        assertEquals(true,getSDKConfigValueByShell(SDKIP,"AddrService","Provider").trim().contains("mongodb"));
        assertEquals(true,getSDKConfigValueByShell(SDKIP,"AddrService","DBPath").trim().contains(checkMongoDBAddr));
    }


    public static void setSDKOnePeer(String SDKIP,String PeerIPPort,String TLSEnabled){
        //获取第一个[[Peer]]所在行号 后面加入节点集群信息时插入的行号
        String lineNo = shExeAndReturn(SDKIP,"grep -n Peer "+ SDKConfigPath + " | cut -d \":\" -f 1 |sed -n '1p'");
        //先删除conf/config.toml文件中的所有Peers
        shExeAndReturn(SDKIP,"sed -i '/Peer/d' " + SDKConfigPath);
        shExeAndReturn(SDKIP,"sed -i '/Address/d' " + SDKConfigPath);
        shExeAndReturn(SDKIP,"sed -i '/TLSEnabled/d' " + SDKConfigPath);

        shExeAndReturn(SDKIP,"sed -i '" + Integer.parseInt(lineNo) + "i[[Peer]]' " + SDKConfigPath);
        shExeAndReturn(SDKIP,"sed -i '" + (Integer.parseInt(lineNo)+1) + "iAddress = \"" + PeerIPPort + "\"' " + SDKConfigPath);
        shExeAndReturn(SDKIP,"sed -i '" + (Integer.parseInt(lineNo)+2) + "iTLSEnabled = " + TLSEnabled + "' " + SDKConfigPath);
    }

    public static void addSDKPeerCluster(String SDKIP,String PeerIPPort,String TLSEnabled){
        //获取第一个[[Peer]]所在行号 后面加入节点集群信息时插入的行号
        String lineNo = shExeAndReturn(SDKIP,"grep -n TLSEnabled "+ SDKConfigPath + " | cut -d \":\" -f 1 |sed -n '$p'");

        shExeAndReturn(SDKIP,"sed -i '" + (Integer.parseInt(lineNo)+1) + "i[[Peer]]' " + SDKConfigPath);
        shExeAndReturn(SDKIP,"sed -i '" + (Integer.parseInt(lineNo)+2)  + "iAddress = \"" + PeerIPPort + "\"' " + SDKConfigPath);
        shExeAndReturn(SDKIP,"sed -i '" + (Integer.parseInt(lineNo)+3) + "iTLSEnabled = " + TLSEnabled + "' " + SDKConfigPath);
    }




    //------------------------------------------------------------------------------------------------------
    //修改节点conf/base.toml文件中的相关配置项信息
    public static void setPeerTLSCertECDSA(String PeerIP)throws Exception{
        setPeerBaseByShell(PeerIP,"Rpc","TLSCaPath","\"\\\".\\/ecdsa\\/ca.pem\"\\\"");
        setPeerBaseByShell(PeerIP,"Rpc","TLSCertPath","\"\\\".\\/ecdsa\\/cert.pem\"\\\"");
        setPeerBaseByShell(PeerIP,"Rpc","TLSKeyPath","\"\\\".\\/ecdsa\\/key.pem\"\\\"");

        assertEquals(true,getPeerBaseValueByShell(PeerIP,"Rpc","TLSCaPath").trim().contains("./ecdsa/ca.pem"));
        assertEquals(true,getPeerBaseValueByShell(PeerIP,"Rpc","TLSCertPath").trim().contains("./ecdsa/cert.pem"));
        assertEquals(true,getPeerBaseValueByShell(PeerIP,"Rpc","TLSKeyPath").trim().contains("./ecdsa/key.pem"));
    }

    public static void setPeerTLSCertDismatch(String PeerIP)throws Exception{
        setPeerBaseByShell(PeerIP,"Rpc","TLSCaPath","\"\\\".\\/dismatch\\/ca.pem\"\\\"");
        setPeerBaseByShell(PeerIP,"Rpc","TLSCertPath","\"\\\".\\/dismatch\\/cert.pem\"\\\"");
        setPeerBaseByShell(PeerIP,"Rpc","TLSKeyPath","\"\\\".\\/dismatch\\/key.pem\"\\\"");

        assertEquals(true,getPeerBaseValueByShell(PeerIP,"Rpc","TLSCaPath").trim().contains("./dismatch/ca.pem"));
        assertEquals(true,getPeerBaseValueByShell(PeerIP,"Rpc","TLSCertPath").trim().contains("./dismatch/cert.pem"));
        assertEquals(true,getPeerBaseValueByShell(PeerIP,"Rpc","TLSKeyPath").trim().contains("./dismatch/key.pem"));
    }
    public static void setPeerTLSCertExpired(String PeerIP)throws Exception{
        setPeerBaseByShell(PeerIP,"Rpc","TLSCaPath","\"\\\".\\/expired\\/ca.pem\"\\\"");
        setPeerBaseByShell(PeerIP,"Rpc","TLSCertPath","\"\\\".\\/expired\\/cert.pem\"\\\"");
        setPeerBaseByShell(PeerIP,"Rpc","TLSKeyPath","\"\\\".\\/expired\\/key.pem\"\\\"");

        assertEquals(true,getPeerBaseValueByShell(PeerIP,"Rpc","TLSCaPath").trim().contains("./expired/ca.pem"));
        assertEquals(true,getPeerBaseValueByShell(PeerIP,"Rpc","TLSCertPath").trim().contains("./expired/cert.pem"));
        assertEquals(true,getPeerBaseValueByShell(PeerIP,"Rpc","TLSKeyPath").trim().contains("./expired/key.pem"));
    }

    public static void setPeerLicence(String PeerIP,String value)throws Exception{
        setPeerBaseByShell(PeerIP,"Node","Licence","\"\\\".\\/" + value + "\"\\\"");
        assertEquals(true,getPeerBaseValueByShell(PeerIP,"Node","Licence").trim().contains(value));
    }

    public static void setPeerContractEnabled(String PeerIP, String flag)throws Exception{
        setPeerBaseByShell(PeerIP,"Contract","Enabled",flag);
        assertEquals(true,getPeerBaseValueByShell(PeerIP,"Contract","Enabled").trim().contains(flag));
    }

    public static void setPeerCryptKeyType(String PeerIP,String keytype)throws Exception{
        setPeerBaseByShell(PeerIP,"Crypt","KeyType","\"\\\"" + keytype + "\"\\\"");
        assertEquals(true,getPeerBaseValueByShell(PeerIP,"Crypt","KeyType").trim().contains(keytype));
    }

    public static void setPeerCryptHashType(String PeerIP,String hashtype)throws Exception{
        setPeerBaseByShell(PeerIP,"Crypt","HashType","\"\\\"" + hashtype + "\"\\\"");
        assertEquals(true,getPeerBaseValueByShell(PeerIP,"Crypt","HashType").trim().contains(hashtype));
    }

    public static void setPeerPackTime(String PeerIP,String PackTime)throws Exception{
        setPeerBaseByShell(PeerIP,"BlockChain","PackTime",PackTime);
        assertEquals(true,getPeerBaseValueByShell(PeerIP,"BlockChain","PackTime").trim().contains(PackTime));
    }


    public static void setPeerClusterOnePeer(String PeerIP,String addIP,String Port,String Type,String IPformat,String TcpType){
        shExeAndReturn(PeerIP,"echo \" \" > " + PeerMemConfigPath); //清空peer config.toml文件

        String peerID = getPeerId(addIP,USERNAME,PASSWD);
        String showName = "peer" + addIP.substring(addIP.lastIndexOf(".")+1);
        String addr = "\\/" + IPformat + "\\/" + addIP +"\\/" + TcpType + "\\/" + Port;

        shExeAndReturn(PeerIP,"sed -i '1i[Members]' " + PeerMemConfigPath);
        shExeAndReturn(PeerIP,"sed -i '2i\n' " + PeerMemConfigPath);
        shExeAndReturn(PeerIP,"sed -i '3i[[Members.Peers]]' " + PeerMemConfigPath);
        shExeAndReturn(PeerIP,"sed -i '4iId = \"" + peerID + "\"' " + PeerMemConfigPath);
        shExeAndReturn(PeerIP,"sed -i '5iShownName = \"" + showName + "\"' " + PeerMemConfigPath);
        shExeAndReturn(PeerIP,"sed -i '6iAddr = \"" + addr + "\"' " + PeerMemConfigPath);
        shExeAndReturn(PeerIP,"sed -i '7iType = " + Type + "' " + PeerMemConfigPath);
    }

    public static void addPeerCluster(String PeerIP,String addIP,String Port,String Type,String IPformat,String TcpType){
        //获取第一个[[Peer]]所在行号 后面加入节点集群信息时插入的行号
        String lineNo = shExeAndReturn(PeerIP,"grep -n Type "+ PeerMemConfigPath + " | cut -d \":\" -f 1 |sed -n '$p'");

        String peerID = getPeerId(addIP,USERNAME,PASSWD);
        String showName = "peer" + addIP.substring(addIP.lastIndexOf(".")+1);
        String addr = "\\/" + IPformat + "\\/" + addIP +"\\/" + TcpType + "\\/" + Port;
        shExeAndReturn(PeerIP,"sed -i '" + (Integer.parseInt(lineNo)+1) + "i\n' " + PeerMemConfigPath);
        shExeAndReturn(PeerIP,"sed -i '" + (Integer.parseInt(lineNo)+2) + "i[[Members.Peers]]' " + PeerMemConfigPath);
        shExeAndReturn(PeerIP,"sed -i '" + (Integer.parseInt(lineNo)+3) + "iId = \"" + peerID + "\"' " + PeerMemConfigPath);
        shExeAndReturn(PeerIP,"sed -i '" + (Integer.parseInt(lineNo)+4) + "iShownName = \"" + showName + "\"' " + PeerMemConfigPath);
        shExeAndReturn(PeerIP,"sed -i '" + (Integer.parseInt(lineNo)+5) + "iAddr = \"" + addr + "\"' " + PeerMemConfigPath);
        shExeAndReturn(PeerIP,"sed -i '" + (Integer.parseInt(lineNo)+6) + "iType = " + Type + "' " + PeerMemConfigPath);
    }

    public static void setPeerClusterWithOneDataPeer(){
        //设置节点Peer4为数据节点
        setPeerConfigOneData(PEER1IP);
        setPeerConfigOneData(PEER2IP);
        setPeerConfigOneData(PEER4IP);
    }

    public static void setPeerCluster(){
        //设置所有节点均为共识节点
        setPeerConfig(PEER1IP);
        setPeerConfig(PEER2IP);
        setPeerConfig(PEER4IP);
    }

    public static void setPeerConfigOneData(String PeerIP){
        //设置节点Peer1 config 配置文件 节点Peer4为数据节点
        setPeerClusterOnePeer(PeerIP,PEER1IP,PEER1TCPPort,"0",ipv4,tcpProtocol);
        addPeerCluster(PeerIP,PEER2IP,PEER2TCPPort,"0",ipv4,tcpProtocol);
        addPeerCluster(PeerIP,PEER4IP,PEER4TCPPort,"1",ipv4,tcpProtocol);
    }

    public static void setPeerConfig(String PeerIP){
        //设置节点Peer1 config 配置文件 均为共识节点
        setPeerClusterOnePeer(PeerIP,PEER1IP,PEER1TCPPort,"0",ipv4,tcpProtocol);
        addPeerCluster(PeerIP,PEER2IP,PEER2TCPPort,"0",ipv4,tcpProtocol);
        addPeerCluster(PeerIP,PEER4IP,PEER4TCPPort,"0",ipv4,tcpProtocol);
    }

    public static void uploadFileToPeer(String PeerIP,String...filelist){
        for (String file : filelist) {
            uploadFiletoDestDirByssh(srcShellScriptDir + file,PeerIP,USERNAME,PASSWD,destShellScriptDir);
        }
    }

    public static boolean checkProgramActive(String queryIP, String programName){
        boolean bRunning = false;

        String resp = shExeAndReturn(queryIP,"ps -ef|grep " + programName + " |grep -v grep");

        //需要保证进程名称的唯一性
        if (!resp.trim().isEmpty()){
            bRunning = true;
        }
        return bRunning;
    }

    @Test
    public void test()throws Exception{
        setPeerConfig(PEER1IP);
    }

}
