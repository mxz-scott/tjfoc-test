package com.tjfintech.common;


import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.utils.GetTest;
import com.tjfintech.common.utils.PostTest;
import lombok.extern.slf4j.Slf4j;
import java.util.*;

import static com.tjfintech.common.utils.UtilsClass.SDKADD;


@Slf4j
public class GoMultiSign implements MultiSign {

    /**
     * 创建多签地址
     * @author chenxu
     * @version 1.0
     */

    public String genMultiAddress(int M,Map keyMap){

        Map<String, Object> map = new HashMap<>();
        List<Object>PubkeysObjects=new ArrayList<>();
        for (Object value : keyMap.values()) {
            PubkeysObjects.add(value);
        }
        map.put("Args", PubkeysObjects);
        map.put("M", M);
        String result=PostTest.sendPostToJson(SDKADD+"/utxo/genmultiaddress", map);
        log.info(result);
        return result;

    }


    /**
     * 查询用户余额
     * @param addr    用户地址
     * @param priKey  用户私钥
     */
    public String Balance(String addr,String priKey,String tokenType) {
        Map<String, Object> map = new HashMap<>();
        map.put("MultiAddr", addr);
        map.put("PriKey", priKey);
        map.put("tokentype", tokenType);
        String result=PostTest.sendPostToJson(SDKADD+"/utxo/balance", map);
        //log.info(result);
        return result;
    }

    public String Balance(String priKey,String tokenType) {
        Map<String, Object> map = new HashMap<>();
        map.put("key", priKey);
        map.put("tokentype", tokenType);
        String param=GetTest.ParamtoUrl(map);
        String result= GetTest.SendGetTojson(SDKADD+"/utxo/balance"+"?"+ param);
//        log.info(result);
        return result;
    }

    /**
     * 按地址查询用户余额
     * @param addr    用户地址
     *
     */
    public String BalanceByAddr(String addr) {
        String result= GetTest.SendGetTojson(SDKADD+"/utxo/balancev2/"+addr);
        log.info(result);
        return result;
    }

    public String BalanceByAddr(String addr,String tokenType) {
        String result= GetTest.SendGetTojson(SDKADD+"/utxo/balancev2/"+addr+"?tokentype"+"="+tokenType);
        log.info(result);
        return result;
    }

    /**
     * 使用3/3账户发行Token申请
     * @param MultiAddr   多签地址
     * @param TokenType   币种类型
     * @param Amount      货币数量
     * @param Data        额外数据
     *
     * @return
     */
    public String issueToken(String MultiAddr,String TokenType,String Amount,String Data) {
        Map<String, Object> map = new HashMap<>();
        map.put("MultiAddr", MultiAddr);
        map.put("TokenType", TokenType);
        map.put("Amount", Amount);
        map.put("Data", Data);
        String response = PostTest.sendPostToJson(SDKADD+"/utxo/multi/issuetoken", map);
//        log.info("发行token："+response);
        return response;
    }


    /**
     * 使用3/3账户发行Token申请，使用本地签名
     * @param MultiAddr   多签地址
     * @param TokenType   币种类型
     * @param Amount      货币数量
     * @param Data        额外数据
     *
     * @return
     */
    public String issueTokenLocalSign(String MultiAddr, String TokenType,String Amount,String Data) {
        Map<String, Object> map = new HashMap<>();
        map.put("MultiAddr", MultiAddr);
        map.put("TokenType", TokenType);
        map.put("Amount", Amount);
        map.put("Data", Data);
        String response = PostTest.sendPostToJson(SDKADD+"/utxo/multi/issuetoken_localsign", map);
        //log.info("发行token："+response);
        return response;
    }

    /**
     * 发送签名
     * @param signedData   本地签名后的数据
     *
     * @return
     */
    public String sendSign(String signedData) {

        Map<String, Object> map = new HashMap<>();
        map.put("Data", signedData);
        String response = PostTest.sendPostToJson(SDKADD+"/utxo/multi/send_multisign", map);
        log.info(response);
        return response;
    }

    /**
     签名多签发行Token交易-带密码
     * @param Tx     交易ID
     * @param Prikey  签名所用私钥
     * @param Pwd  签名所用私钥密码
     * @return    返回未经过处理内容
     */

    public String Sign(String Tx, String Prikey, String Pwd) {

        Map<String, Object> map = new HashMap<>();
        map.put("Prikey", Prikey);
        map.put("Pwd", Pwd);
        map.put("Tx", Tx);
        String response = PostTest.sendPostToJson(SDKADD+"/utxo/multi/sign", map);
        //log.info(response);
        return response;

    }
    public String Sign(String Tx, String Prikey) {

        Map<String, Object> map = new HashMap<>();
        map.put("Prikey", Prikey);
        map.put("Tx", Tx);
        String response = PostTest.sendPostToJson(SDKADD+"/utxo/multi/sign", map);
        //log.info(response);
        return response;
    }

    /**
     * 转账
     * @param PriKey 私钥
     * @param Pwd     密码
     * @param Data     详情内容
     * @param fromAddr  发起地址
     * @return
     */
    public String Transfer(String PriKey,String Pwd,String Data ,String fromAddr,List<Map>tokenList) {

        Map<String, Object> map = new HashMap<>();
        map.put("MultiAddr", fromAddr);
        map.put("Prikey", PriKey);
        map.put("Data", Data);
        map.put("Pwd",Pwd);
        map.put("Token", tokenList);
        String result=PostTest.sendPostToJson(SDKADD+"/utxo/multi/transfer", map);
        log.info(result);
        return result;

    }
    public String Transfer(String PriKey,String Data,String fromAddr ,List<Map>tokenList) {

        Map<String, Object> map = new HashMap<>();
        map.put("MultiAddr", fromAddr);
        map.put("Prikey", PriKey);
        map.put("Data", Data);
        map.put("Token", tokenList);
        String result=PostTest.sendPostToJson(SDKADD+"/utxo/multi/transfer", map);
        log.info(result);
        return result;

    }

    /**
     * 转账，本地签名
     * @param PubKey 公钥
     * @param Data     详情内容
     * @param fromAddr  发起地址
     * @return
     */
    public String TransferLocalSign(String fromAddr , String PubKey,String Data,List<Map>tokenList) {

        Map<String, Object> map = new HashMap<>();
        map.put("MultiAddr", fromAddr);
        map.put("PubKey", PubKey);
        map.put("Data", Data);
        map.put("Token", tokenList);
        String result=PostTest.sendPostToJson(SDKADD+"/utxo/multi/transfer_localsign", map);
//        log.info(result);
        return result;

    }

    /**
     * 核对私钥接口测试
     * @param PriKey  私钥
     * @param Pwd    密码
     */
    public String CheckPriKey(String PriKey ,String Pwd){
          Map<String,Object>map=new HashMap<>();
          map.put("PriKey",PriKey);
          map.put("Pwd",Pwd);
          String result=PostTest.sendPostToJson(SDKADD+"/utxo/validatekey",map);
          log.info(result);
          return result;
    }



    /**
     * 回收token
     * @param multiAddr 多签地址
     * @param priKey 私钥
     * @param Pwd 私钥密码
     * @param tokenType 数字货币类型
     * @param amount 货币数量
     */
    public String Recycle(String multiAddr,String priKey,String Pwd,String tokenType,String amount){

        Map<String ,Object>map=new HashMap<>();
        map.put("MultiAddr",multiAddr);
        map.put("PriKey",priKey);
        map.put("Pwd",Pwd);
        map.put("TokenType",tokenType);
        map.put("Amount",amount);
        String response =PostTest.sendPostToJson(SDKADD+"/utxo/multi/recycle",map);
        log.info(response);
        return response;

    }
    public String Recycle(String multiAddr,String priKey,String tokenType,String amount){

        Map<String ,Object>map=new HashMap<>();
        map.put("MultiAddr",multiAddr);
        map.put("PriKey",priKey);
        map.put("TokenType",tokenType);
        map.put("Amount",amount);
        String result =PostTest.sendPostToJson(SDKADD+"/utxo/multi/recycle",map);
        log.info(result);
        return result;

    }

    public String Recycle(String priKey,String tokenType,String amount){
        Map<String ,Object>map=new HashMap<>();
        map.put("PriKey",priKey);
        map.put("TokenType",tokenType);
        map.put("Amount",amount);
        String result =PostTest.sendPostToJson(SDKADD+"/utxo/multi/recycle",map);
        log.info(result);
        return result;

    }
    /**
     * 回收token，本地签名
     * @param multiAddr 多签地址
     * @param pubKey 公钥
     * @param tokenType 数字货币类型
     * @param amount 货币数量
     */
    public String RecycleLocalSign(String multiAddr,String pubKey,String tokenType,String amount){
        Map<String ,Object>map=new HashMap<>();
        map.put("MultiAddr",multiAddr);
        map.put("PubKey",pubKey);
        map.put("TokenType",tokenType);
        map.put("Amount",amount);
        String result =PostTest.sendPostToJson(SDKADD+"/utxo/multi/recycle_localsign",map);
//        log.info(result);
        return result;
    }



    /**
     * 查询回收账户余额
     * @param tokenType 数字货币类型
     */
    public String QueryZero(String tokenType){
        Map<String ,Object>map=new HashMap<>();
        map.put("tokentype",tokenType);
        String param= GetTest.ParamtoUrl(map);
        String result= (GetTest.SendGetTojson(SDKADD+"/utxo/balance/zero"+"?"+param));
        log.info(result);
        return result;
    }


}

