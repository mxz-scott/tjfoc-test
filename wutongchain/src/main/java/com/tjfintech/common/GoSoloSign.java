package com.tjfintech.common;

import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.utils.GetTest;
import com.tjfintech.common.utils.PostTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.SDKADD;
import static com.tjfintech.common.utils.UtilsClass.subLedger;

@Slf4j
public class GoSoloSign implements SoloSign {

    @Before
    /**
     * 每次测试都会事先执行的测试环境准备内容
     * 目前为空
     */
    public void TestBefore(){

    }


    /**单签转账，本地签名
     *
     *
     */
    public String TransferLocalSign(List<Map> token,String pubKey,String data) {
        Map<String, Object> map = new HashMap<>();
        map.put("pubKey", pubKey);
        map.put("data", data);
        map.put("token", token);
        String param="";
        if(subLedger!="") param = param +"?ledger="+subLedger;
        String result = PostTest.sendPostToJson(SDKADD + "/v2/tx/utxo/transfer/sign"+param, map);
//        log.info(result);
        return result;
    }

    /**查询单签地址余额
     * @param tokenType 币种
     */
    public String BalanceByAddr(String address,String tokenType){
        Map<String,Object>map=new HashMap<>();
        map.put("address",address);
        map.put("tokenType",tokenType);
        String param = "";
        if(subLedger!="") param = param +"?ledger=" + subLedger;
        String result=PostTest.sendPostToJson(SDKADD+"/v2/tx/utxo/balance" + param, map);
        log.info(result);
        return result ;
    }

    /**单签账号向其他地址转账
     *
     *
     */
    public String Transfer(List<Map> token,String priKey,String data) {
        Map<String, Object> map = new HashMap<>();
        map.put("prikey", priKey);
        map.put("data", data);
        map.put("token", token);
        String param="";
        if(subLedger!="") param = param +"?ledger="+subLedger;
       String result=PostTest.sendPostToJson(SDKADD+"/v2/tx/utxo/transfer"+param, map);
       log.info(result);
        return result ;

    }

    /**
     * 发行token
     */
    public String issueToken(String priKey,String tokenType,String amount,String data,String address){
        Map<String, Object> map = new HashMap<>();
        map.put("priKey", priKey);
        map.put("tokenType", tokenType);
        map.put("amount", amount);
        map.put("data",data);
        map.put("addresses",address);
        String param="";
        if(subLedger!="") param = param +"?ledger="+subLedger;
        String result=PostTest.sendPostToJson(SDKADD+"/v2/tx/utxo/issue"+param, map);
        log.info(result);
        return result;
    }




    /**
     * 创建1/1单签账号
     */
    public String genAddress(String publicKey){
        Map<String, Object> map = new HashMap<>();
        map.put("pubkey", publicKey);
        String param="";
        if(subLedger!="") param = param +"?ledger="+subLedger;
        String result=PostTest.postMethod(SDKADD+"/v2/address/gen"+param, map);
        log.info(result);
        return result;
    }
    /**
     * 转账操作的TOKEN数组构建方法
     * @param toAddr     发送地址
     * @param tokenType  币种
     * @param amount      数量
     * @return     返回TOKEN的LIST
     */

   public List<Map> constructToken(String toAddr,String tokenType,String amount){
       Map<String,Object>amountMap=new HashMap<>();
       amountMap.put("tokenType",tokenType);
       amountMap.put("amount",amount);
       amountMap.put("toAddress",toAddr);
       List<Map>list=new ArrayList<>();
       list.add(amountMap);
       return list;
   }
    public List<Map> constructToken(String toAddr,String tokenType,String amount,List<Map>mapList){
        List<Map>list=new ArrayList<>();
        for (int i=0;i<mapList.size();i++){
            list.add(mapList.get(i));
        }
        Map<String,Object>amountMap=new HashMap<>();
        amountMap.put("tokenType",tokenType);
        amountMap.put("amount",amount);
        amountMap.put("toAddress",toAddr);

        list.add(amountMap);
        return list;
    }

    /**
     * 单签发行token,本地签名
     */
    public String issueTokenLocalSign(String pubKey, String tokenType,String amount,String data){
        Map<String, Object> map = new HashMap<>();
        map.put("PubKey", pubKey);
        map.put("TokenType", tokenType);
        map.put("Amount", amount);
        map.put("Data",data);
        String param="";
        if(subLedger!="") param = param +"?ledger="+subLedger;
        String result=PostTest.sendPostToJson(SDKADD+"/v2/tx/utxo/issue"+param, map);
        log.info(result);
        return result;
    }

    /**单签回收，本地签名
     */
    public String RecycleLocalSign(String pubKey,String tokenType,String amount){
        Map<String ,Object>map=new HashMap<>();
//        map.put("MultiAddr","");
        map.put("PubKey",pubKey);
        map.put("TokenType",tokenType);
        map.put("Amount",amount);
        String param="";
        if(subLedger!="") param = param +"?ledger="+subLedger;
        String result =PostTest.sendPostToJson(SDKADD+"/v2/tx/utxo/destroy"+param,map);
        log.info(result);
        return result;
    }


    /**
     * 发送签名数据
     */
    public String sendSign(String signData){
        Map<String, Object> map = new HashMap<>();
        map.put("Data", signData);
        String param="";
        if(subLedger!="") param = param +"?ledger="+subLedger;
        String result=PostTest.sendPostToJson(SDKADD+"/utxo/send_sign"+param, map);
//        log.info(result);
        return result;
    }

    /**
     * 回收数字资产
     */
    public String Recycle(String priKey,String tokenType,String amount){
        Map<String ,Object>map=new HashMap<>();
        map.put("priKey",priKey);
        map.put("tokenType",tokenType);
        map.put("amount",amount);
        String param="";
        if(subLedger!="") param = param +"?ledger="+subLedger;
        String result =PostTest.sendPostToJson(SDKADD+"/v2/tx/utxo/destroy"+param,map);
        log.info(result);
        return result;

    }

    /**
     * 同步转账交易
     * @param timeout
     * @param token
     * @param priKey
     * @param data
     * @return
     */
    @Override
    public String SyncTransfer(Integer timeout, List<Map> token, String priKey, String data) {
        Map<String, Object> map = new HashMap<>();
        map.put("Prikey", priKey);
        map.put("Data", data);
        map.put("Token", token);
        String param="";
        if(subLedger!="") param = param +"&ledger="+subLedger;
        String result=PostTest.sendPostToJson(SDKADD+"/sync/utxo/transfer?timeout="+timeout+param, map);
        log.info(result);
        return result ;
    }

    /**
     * 同步单签发行token
     * @param timeout
     * @param priKey
     * @param tokenType
     * @param amount
     * @param data
     * @param address
     * @return
     */
    @Override
    public String SyncIssueToken(Integer timeout, String priKey, String tokenType, String amount, String data, String address) {
        Map<String, Object> map = new HashMap<>();
        map.put("PriKey", priKey);
        map.put("TokenType", tokenType);
        map.put("Amount", amount);
        map.put("Data",data);
        map.put("Addr",address);
        String param="";
        if(subLedger!="") param = param +"&ledger="+subLedger;
        String result=PostTest.sendPostToJson(SDKADD+"/sync/utxo/issuetoken?timeout="+timeout+param, map);
        log.info(result);
        return result;
    }

}
