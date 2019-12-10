package com.tjfintech.common;

import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.Interface.Token;
import com.tjfintech.common.utils.UtilsClass;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.Test;

import java.util.*;

import static com.java.tar.gz.FileUtil.log;
import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClass.SDKPATH;
import static org.hamcrest.CoreMatchers.both;
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
        log.info(comments);
        tokenModule.tokenIssue(issueAddr,collAddr,issueToken,amount,comments);
        return issueToken;
    }

    public  String tokenModule_TransferToken(String from,String to, String tokenType,String amount){
        String comments = from + "向" + to + " 转账token：" + tokenType + " 数量：" + amount;
        log.info(comments);
        List<Map> list = utilsClass.tokenConstructToken(to,tokenType,amount);
        return tokenModule.tokenTransfer(from,comments,list);
    }

    public  String tokenModule_TransferTokenList(String from, List<Map> tokenList){
        String comments = from + "一转多";
        log.info(comments);
        return tokenModule.tokenTransfer(from,comments,tokenList);
    }

    public String tokenModule_DestoryToken(String addr,String tokenType,String amount){
        String comments = addr + "销毁token：" + tokenType + " 数量：" + amount;
        log.info(comments);
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
        log.info(data);
        String response = multiSign.issueToken(issueAddr,ToAddr,tokenType, amount, data);
        assertThat(response, containsString("200"));
        String Tx1 = JSONObject.fromObject(response).getJSONObject("Data").getString("Tx");
        log.info("第一次签名");
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
        log.info(data);
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
            log.info("+++++++++++++++++" + checkStr);
            log.info("+++++++++++++++++" + jsonArray.toString());
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
        log.info("matching complete.............");
        return bResult;
    }

    public static boolean checkWalletEnabled(){
        boolean bEnabled = true;
        String sdkIP = getIPFromStr(SDKADD);
        String resp = shExeAndReturn(sdkIP,"grep -n \"\\[Wallet\\]\" "+ SDKPATH + "conf/config.toml | cut -d \":\" -f 1 ");
        String checkLineNo = String.valueOf(Integer.parseInt(resp) + 1);//正常配置文件中[Wallet]下面一行就是Enabled=true 如果不是此种情况则设置无效
        resp = shExeAndReturn(sdkIP,"sed -n \"" + checkLineNo + "p\" " + SDKPATH + "conf/config.toml");
        if(resp.contains("true")) bEnabled = true;
        else if(resp.contains("false")) bEnabled = false;
        return bEnabled;
    }


    public static void setSDKTLSCertECDSA()throws Exception{
        String sdkIP = getIPFromStr(SDKADD);
        String resp = shExeAndReturn(sdkIP,"grep -n \"\\[Rpc\\]\" "+ SDKPATH + "conf/config.toml | cut -d \":\" -f 1 ");
        String TLSCaPathLineNo = String.valueOf(Integer.parseInt(resp) + 1);
        String TLSCertPathLineNo = String.valueOf(Integer.parseInt(resp) + 2);
        String TLSKeyPathLineNo = String.valueOf(Integer.parseInt(resp) + 3);
        shellExeCmd(sdkIP,"sed -i '" + TLSCaPathLineNo + "d' " + SDKPATH + "conf/config.toml");//删除原文件中的TLSCaPath行
        shellExeCmd(sdkIP,"sed -i '" + TLSCaPathLineNo + "i  TLSCaPath = \"./ecdsa/ca.pem\"' " + SDKPATH + "conf/config.toml");//插入TLSCaPath新配置

        shellExeCmd(sdkIP,"sed -i '" + TLSCertPathLineNo + "d' " + SDKPATH + "conf/config.toml");//删除原文件中的TLSCaPath行
        shellExeCmd(sdkIP,"sed -i '" + TLSCertPathLineNo + "i  TLSCertPath = \"./ecdsa/cert.pem\"' " + SDKPATH + "conf/config.toml");//插入TLSCertPath新配置

        shellExeCmd(sdkIP,"sed -i '" + TLSKeyPathLineNo + "d' " + SDKPATH + "conf/config.toml");//删除原文件中的TLSCaPath行
        shellExeCmd(sdkIP,"sed -i '" + TLSKeyPathLineNo + "i TLSKeyPath = \"./ecdsa/key.pem\"' " + SDKPATH + "conf/config.toml");//插入TLSKeyPath新配置
    }

    public static void setPeerTLSCertECDSA(String PeerIP)throws Exception{
        String resp = shExeAndReturn(PeerIP,"grep -n \"\\[Rpc\\]\" "+ PeerPATH + "conf/base.toml | cut -d \":\" -f 1 ");
        String TLSCaPathLineNo = String.valueOf(Integer.parseInt(resp) + 4);
        String TLSCertPathLineNo = String.valueOf(Integer.parseInt(resp) + 5);
        String TLSKeyPathLineNo = String.valueOf(Integer.parseInt(resp) + 6);
        //删除原文件中的Rpc TLS配置后再插入新的配置信息
        shellExeCmd(PeerIP,"sed -i '" + TLSCaPathLineNo + "d' " + PeerPATH + "conf/base.toml");
        shellExeCmd(PeerIP,"sed -i '" + TLSCaPathLineNo + "i  TLSCaPath = \"./ecdsa/ca.pem\"' " + PeerPATH + "conf/base.toml");//插入TLSCaPath新配置

        shellExeCmd(PeerIP,"sed -i '" + TLSCertPathLineNo + "d' " + PeerPATH + "conf/base.toml");
        shellExeCmd(PeerIP,"sed -i '" + TLSCertPathLineNo + "i  TLSCertPath = \"./ecdsa/cert.pem\"' " + PeerPATH + "conf/base.toml");//插入TLSCertPath新配置

        shellExeCmd(PeerIP,"sed -i '" + TLSKeyPathLineNo + "d' " + PeerPATH + "conf/base.toml");
        shellExeCmd(PeerIP,"sed -i '" + TLSKeyPathLineNo + "i TLSKeyPath = \"./ecdsa/key.pem\"' " + PeerPATH + "conf/base.toml");//插入TLSKeyPath新配置
    }

}
