package com.tjfintech.common;


import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.utils.GetTest;
import com.tjfintech.common.utils.PostTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.SDKADD;


@Slf4j
public class GoMultiSign implements MultiSign {

    private static final String PRIKEY1 = "LS0tLS1CRUdJTiBFTkNSWVBURUQgUFJJVkFURSBLRVktLS0tLQ0KTUlIOE1GY0dDU3FHU0liM0RRRUZEVEJLTUNrR0NTcUdTSWIzRFFFRkREQWNCQWhra283bEx2ZWtmQUlDQ0FBdw0KREFZSUtvWklodmNOQWdjRkFEQWRCZ2xnaGtnQlpRTUVBU29FRUxvc2VwNnI2azhsSXM0Tk1DNndGM2NFZ2FBbg0KeE4wWDRadHJsc2pCVG5TOXhjYnM3Wk9lcjFwY25aby9RZ2JqRWtGeThaYVBjSyt5d0NLcDRaMDVnbWgwU2M4Nw0KTVdNbGZvd1pJbXcvSHRoOHQ5Y0Z3eFRZMktiZkJEaWQ1SFpwVGRpRGU2R2tVa3hsajRnQkZhM29xMjg4UnVpOA0KOTIwY3FvQmwrWlZKKy8rZkFlaTA2b1ZqdEJzdWp0SmRjWnd6eGlxMjdzK0V3VUptV2NxaWliTWVqZGtDUWZvdQ0Kam9tQkphajZwS3pwdEhQNnIrbHkNCi0tLS0tRU5EIEVOQ1JZUFRFRCBQUklWQVRFIEtFWS0tLS0t";
    private static final String PWD1 = "123";
    private static final String PRIKEY2 = "LS0tLS1CRUdJTiBFTkNSWVBURUQgUFJJVkFURSBLRVktLS0tLQ0KTUlIOE1GY0dDU3FHU0liM0RRRUZEVEJLTUNrR0NTcUdTSWIzRFFFRkREQWNCQWcvQTR4em8xYTR2QUlDQ0FBdw0KREFZSUtvWklodmNOQWdjRkFEQWRCZ2xnaGtnQlpRTUVBU29FRUNVSmx2MFNsQ1FIVWM0Z25YVnVsODBFZ2FDNg0KRnhGZDFEUlB5ZURBR1VlY2RnK0owSndQWm9YVk5MVXZqbGUwZU5GRFRManZlTlN4K2lYd0Y5c1o3cTZJWjN5Rg0KZHgzUzBDU3o3SFBzb29EWUJ6cUg0R09qOU1FZkpYU0hMdGU0WjRUclZsRVJkSVNzVGExb0p0WUV0R2Vmcy9hdQ0KZWRMRDJMS0RZOWY1cFA5WHRGUm1iL210TW9BaHVUdVFGU3V2a0huWFhmSFhjQ3pTb2dybjNsNHZPeDFHbm8xeA0KNnQxL1BnVERMSytTUnFhbHpGNmUNCi0tLS0tRU5EIEVOQ1JZUFRFRCBQUklWQVRFIEtFWS0tLS0t";
    private static final String PWD2 = "124";
    private static final String PRIKEY3 = "LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0tDQpNSUdUQWdFQU1CTUdCeXFHU000OUFnRUdDQ3FCSE05VkFZSXRCSGt3ZHdJQkFRUWd6cVBtZnpIQWVmdXpXM1pWDQp1K2MzaFZVbVdrMldtUk1qWGJZNVhGS0lIOWlnQ2dZSUtvRWN6MVVCZ2kyaFJBTkNBQVRHZlhVSklyWnVtZVJxDQpSUmRxdTUybkJMK3I3ZTU4K05qUU9GVWV4OGVuVHBidWh2RlpmL0cvcXpQRFF4L1I2aCszWWw2UkRFVitIckVXDQpZR2RKZEpSZg0KLS0tLS1FTkQgUFJJVkFURSBLRVktLS0tLQ==";
    private static final String PUBKEY1 = "LS0tLS1CRUdJTiBQVUJMSUMgS0VZLS0tLS0NCk1Ga3dFd1lIS29aSXpqMENBUVlJS29FY3oxVUJnaTBEUWdBRXhuMTFDU0syYnBua2FrVVhhcnVkcHdTL3ErM3UNCmZQalkwRGhWSHNmSHAwNlc3b2J4V1gveHY2c3p3ME1mMGVvZnQySmVrUXhGZmg2eEZtQm5TWFNVWHc9PQ0KLS0tLS1FTkQgUFVCTElDIEtFWS0tLS0t";
    private static final String PUBKEY2 = "LS0tLS1CRUdJTiBQVUJMSUMgS0VZLS0tLS0NCk1Ga3dFd1lIS29aSXpqMENBUVlJS29FY3oxVUJnaTBEUWdBRWtiRmlaOW9VaWFaMmh3dTVsS3FYNkQ1OHdXOVYNCmNEQ1BjUEJQWThyTlVTQitNR1ZxMUlyUk8vVVBMaXRqc0RtcWN2MzdKdmVSTC9Ba0FWM1hDd2JGM3c9PQ0KLS0tLS1FTkQgUFVCTElDIEtFWS0tLS0t";
    private static final String PUBKEY3 = "LS0tLS1CRUdJTiBQVUJMSUMgS0VZLS0tLS0NCk1Ga3dFd1lIS29aSXpqMENBUVlJS29FY3oxVUJnaTBEUWdBRXZsTDZtVThFNkU1cTBRVk11NlNTbXRMMzhrdjQNCnVadnhVcG9YUlMwcHNsMkV6UHF2YTIxLzJiTzM5eW5RaHRPTTgrd0lWbExKY3ByNXZnOG1PYkVYdXc9PQ0KLS0tLS1FTkQgUFVCTElDIEtFWS0tLS0t";
    public static final String PRIKEY0="LS0tLS1CRUdJTiBQUklWQVRFIEtFWS0tLS0tCk1JR1RBZ0VBTUJNR0J5cUdTTTQ5QWdFR0NDcUJITTlWQVlJdEJIa3dkd0lCQVFRZ3BRRjEzT01KaERQVVM3bnEKTVVYQUZNK01mUlV3MFc3bFVRQnNvOW12WWZ1Z0NnWUlLb0VjejFVQmdpMmhSQU5DQUFTTFR6QWxRSk1ZQ0RGegp6cURnL2s5TkhEUWpvL1R6WEFHRFpkaGJoOHU0c2loM2FvWUljWUsrN1VCbitBQVJJVDgwNVBySHNzTmRSWGc0CnM0bTgyRkNsCi0tLS0tRU5EIFBSSVZBVEUgS0VZLS0tLS0K";
    private static String TOKENTYPE ;
    private static String RECYCLETYPE;

    private static final String USER1_1="32XBduKk48RJfoA2wJuQ7doaJimMymEmZLYYbYHyH5Eyi5Hs68";
    private static final String USER1_2="SogjzbsX6RsWwRKqdhTQeVTWQhy2SyNGfPy9LHEz6p3famV3wCe";
    private static final String USER3_3="SsUTN9RmWgrD8E48MuJY1pdLw4QDo7GJgK8fn8k7DFzpvG3pwqw";
    private static final String USER_COLLET="Soirv9ikykVHArKCdJqVNegxxqZWUj1g4ixFFYbBLMExy4zUTUe";

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
     * 添加发行地址
     * @param pubKey
     * @param address
     * @return
     */
    public String addissueaddress(String pubKey, String ...address) {
        Map<String, Object> map = new HashMap<>();
        List<Object> addrs = new ArrayList<>();
        for (int i=0;i<address.length;i++){
            addrs.add(address[i]);
        }

        map.put("PriKey", pubKey);
        map.put("Addrs", addrs);
        String result = PostTest.sendPostToJson(SDKADD + "/utxo/addissueaddress", map);
        log.info(result);
        return result;
    }
    /**
     * 带私钥的多签Token发行
     * @param MultiAddr
     * @param TokenType
     * @param Amount
     * @param Prikey
     * @param Data
     * @return
     */
    @Override
    public String issueTokenCarryPri(String MultiAddr, String TokenType, String Amount, String Prikey, String Data) {
        Map<String, Object > map = new HashMap<>();
        map.put("MultiAddr", MultiAddr);
        map.put("Prikey",Prikey);
        map.put("TokenType", TokenType);
        map.put("Amount", Amount);
        map.put("Data", Data);
        String response = PostTest.sendPostToJson(SDKADD + "/utxo/multi/issuetoken", map);
        log.info(response);
        return response;
    }
    /**
     * 带私钥带密码的多签Token发行
     * @param MultiAddr
     * @param TokenType
     * @param Amount
     * @param Prikey
     * @param Pwd
     * @param Data
     * @return
     */
    @Override
    public String issueTokenCarryPri(String MultiAddr, String TokenType, String Amount, String Prikey, String Pwd, String Data) {
        Map<String, Object > map = new HashMap<>();
        map.put("MultiAddr", MultiAddr);
        map.put("PriKey",Prikey);
        map.put("Pwd",Pwd);
        map.put("TokenType", TokenType);
        map.put("Amount", Amount);
        map.put("Data", Data);
        String response = PostTest.sendPostToJson(SDKADD + "/utxo/multi/issuetoken", map);
        log.info(response);
        return response;
    }
    /**
     * 添加发行地址不携带私钥
     * @param address
     * @return
     */
    @Override
    public String addissueaddressRemovePri(String... address) {
        Map<String, Object> map = new HashMap<>();
        List<Object> addrs = new ArrayList<>();
        for (int i=0;i<address.length;i++){
            addrs.add(address[i]);
        }
        map.put("Addrs", addrs);
        String result = PostTest.sendPostToJson(SDKADD + "/utxo/addissueaddress", map);
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
     * 按地址查询用户余额
     * @param addr    用户地址
     *
     */
    public String BalanceByAddr(String addr) {
        Map<String, Object> map = new HashMap<>();
        map.put("Addr",addr);
        String result = PostTest.postMethod(SDKADD + "/utxo/getsdkbalance", map);
//        log.info(result);
        return result;
    }

    public String BalanceByAddr(String addr,String tokenType) {
        Map<String, Object> map = new HashMap<>();
        map.put("Addr",addr);
        map.put("TokenType",tokenType);
        String result = PostTest.postMethod(SDKADD + "/utxo/getsdkbalance", map);
//        log.info(result);
        return result;
    }

    public String getUTXODetail(int StartTime,int EndTime,String tokenType,int UTXOtype,String FromAddr,String ToAddr) {
        Map<String, Object> map = new HashMap<>();
        map.put("StartTime",StartTime);
        map.put("EndTime",EndTime);
        map.put("TokenType",tokenType);
        map.put("UTXOType",UTXOtype);
        map.put("FromAddr",FromAddr);
        map.put("ToAddr",ToAddr);
        String result = PostTest.postMethod(SDKADD + "/utxo/getutxodetail", map);
//        log.info(result);
        return result;
    }

    public String getChainBalance(String tokenType,String Addr) {
        Map<String, Object> map = new HashMap<>();
        map.put("TokenType",tokenType);
        map.put("Addr",Addr);
        String result = PostTest.postMethod(SDKADD + "/utxo/getchainbalance", map);
//        log.info(result);
        return result;
    }

    public String getTotalbyDay(int year,int month,int day) {
        Map<String, Object> map = new HashMap<>();
        map.put("Year",year);
        map.put("Month",month);
        map.put("Day",day);
        String result = PostTest.postMethod(SDKADD + "/utxo/totalbyday", map);
//        log.info(result);
        return result;
    }


    /**
     * 多账号同时回收，本地签名
     * @param tokenList
     * @return
     */
    public String RecyclesLocalSign(List<Map> tokenList){
        Map<String, Object> map = new HashMap<>();
        map.put("Token", tokenList);
        String result=PostTest.sendPostToJson(SDKADD+"/utxo/multi/recycles_localsign", map);
//        log.info(result);
        return result;
    }


    public String delissueaddress(String priKey, String... address) {
        Map<String,Object> map = new HashMap<>();
        List<Object> addrs = new ArrayList<>();
        for (int i= 0;i<address.length;i++){
            addrs.add(address[i]);
        }
        map.put("PriKey", priKey);
        map.put("Addrs", addrs);
        String result = PostTest.sendPostToJson(SDKADD + "/utxo/delissueaddress", map);
        log.info(result);
        return result;
    }
    /**
     * 删除发行地址不携带私钥
     * @param address
     * @return
     */
    @Override
    public String delissueaddressRemovePri(String... address) {
        Map<String,Object> map = new HashMap<>();
        List<Object> addrs = new ArrayList<>();
        for (int i= 0;i<address.length;i++){
            addrs.add(address[i]);
        }
        map.put("Addrs", addrs);
        String result = PostTest.sendPostToJson(SDKADD + "/utxo/delissueaddress", map);
        log.info(result);
        return result;
    }
    /**
     * 获取总发行量,总回收量,总冻结量
     * @param StartTime
     * @param EndTime
     * @param TokenType
     * @return
     */
    @Override
    public String gettotal(int StartTime, int EndTime, String TokenType) {
        Map<String, Object> map = new HashMap<>();
        map.put("StartTime",StartTime);
        map.put("EndTime",EndTime);
        map.put("TokenType",TokenType);
        String result = PostTest.sendPostToJson(SDKADD + "/utxo/gettotal", map);
        log.info(result);
        return result;
    }
    /**
     * 获取tokentype发行量
     * @param TokenType
     * @return
     */
    @Override
    public String tokenstate(String TokenType) {
        Map<String, Object> map = new HashMap<>();
        map.put("TokenType",TokenType);
        String result = PostTest.sendPostToJson(SDKADD + "/utxo/tokenstate", map);
        log.info(result);
        return result;
    }

    /**
     * 根据tokentype获取账户余额
     * @param TokenType
     * @param Address
     * @return
     */
    @Override
    public String getbalancebytt(String TokenType, String Address) {
        Map<String,Object> map = new HashMap<>();
        map.put("TokenType",TokenType);
        map.put("Address",Address);
        String result = PostTest.sendPostToJson(SDKADD + "/utxo/getbalancebytt", map);
        log.info(result);
        return result;
    }

    /**
     * 添加归集地址
     * @param pubKey
     * @param address
     * @return
     */
    public String collAddress(String pubKey, String ...address) {
        Map<String, Object> map = new HashMap<>();
        List<Object> addrs = new ArrayList<>();
        for (int i=0;i<address.length;i++){
        addrs.add(address[i]);
        }

        map.put("PriKey", pubKey);
        map.put("Addrs", addrs);
        String result = PostTest.sendPostToJson(SDKADD + "/utxo/colladdress", map);
        log.info(result);
        return result;
    }


    public String delCollAddress(String priKey, String... address) {
        Map<String,Object> map = new HashMap<>();
        List<Object> addrs = new ArrayList<>();
        for (int i= 0;i<address.length;i++){
            addrs.add(address[i]);
        }
        map.put("PriKey", priKey);
        map.put("Addrs", addrs);
        String result = PostTest.sendPostToJson(SDKADD + "/utxo/deladdress", map);
        log.info(result);
        return result;
    }




    /**
     * 添加归集地址不携带私钥
     * @param address
     * @return
     */
    @Override
    public String collAddressRemovePri(String... address) {
        Map<String, Object> map = new HashMap<>();
        List<Object> addrs = new ArrayList<>();
        for (int i=0;i<address.length;i++){
            addrs.add(address[i]);
        }
        map.put("Addrs", addrs);
        String result = PostTest.sendPostToJson(SDKADD + "/utxo/colladdress", map);
        log.info(result);
        return result;
    }


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
     *
     * @param addr 查询的多签地址
     * @param priKey 多签地址绑定其中一个账户的私钥
     * @param Pwd  多签地址绑定其中一个账户的私钥的密码
     * @param tokenType
     * @return
     */
    public String Balance(String addr,String priKey,String Pwd,String tokenType) {
        Map<String, Object> map = new HashMap<>();
        map.put("MultiAddr", addr);
        map.put("PriKey", priKey);
        map.put("Pwd", Pwd);
        map.put("TokenType", tokenType);
        String result=PostTest.sendPostToJson(SDKADD+"/utxo/balance", map);
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
        log.info(result);
        return result;
    }

    public String Balance(String priKey,String tokenType) {
        Map<String, Object> map = new HashMap<>();

        map.put("key", priKey);
        map.put("tokentype", tokenType);
        String param=GetTest.ParamtoUrl(map);
        String result= GetTest.SendGetTojson(SDKADD+"/utxo/balance"+"?"+ param);
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
        log.info(response);
        return response;
    }
    public String issueToken(String MultiAddr,String ToAddr,String TokenType,String Amount,String Data) {
        Map<String, Object> map = new HashMap<>();
        map.put("MultiAddr", MultiAddr);
        map.put("ToAddr",ToAddr);
        map.put("TokenType", TokenType);
        map.put("Amount", Amount);
        map.put("Data", Data);
        String response = PostTest.sendPostToJson(SDKADD+"/utxo/multi/issuetoken", map);
        log.info(response);
        return response;
    }

    public String issueToken(String MultiAddr,String ToAddr,String TokenType,String Amount,String priKey,String Pwd,String Data) {
        Map<String, Object> map = new HashMap<>();
        map.put("MultiAddr", MultiAddr);
        if(!ToAddr.isEmpty()) map.put("ToAddr",ToAddr);
        map.put("TokenType", TokenType);
        map.put("Amount", Amount);
        if(!priKey.isEmpty()) map.put("PriKey",priKey);
        if(!Pwd.isEmpty()) map.put("Pwd",Pwd);
        map.put("Data", Data);
        String response = PostTest.sendPostToJson(SDKADD+"/utxo/multi/issuetoken", map);
        log.info(response);
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



    public String issueTokenLocalSign(String MultiAddr, String toAddr, String TokenType,String Amount,String Data) {
        Map<String, Object> map = new HashMap<>();
        map.put("MultiAddr", MultiAddr);
        map.put("ToAddr", toAddr);
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
        //String response = PostTest.sendPostToJson(SDKADD+"/utxo/multi/sign", map);
        String response = PostTest.postMethod(SDKADD+"/utxo/multi/sign", map);
        log.info("test");
        log.info(response);
        return response;

    }
    public String Sign(String Tx, String Prikey) {

        Map<String, Object> map = new HashMap<>();
        map.put("Prikey", Prikey);
        map.put("Tx", Tx);
        String response = PostTest.sendPostToJson(SDKADD+"/utxo/multi/sign", map);
        log.info(response);
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
     * 核对私钥接口测试
     * @param PriKey  私钥
     * @param Pwd    密码
     */
    public String CheckPriKey(String PriKey,String Pwd){
          Map<String,Object>map = new HashMap<>();
          map.put("PriKey",PriKey);
          map.put("Pwd",Pwd);
          System.out.println(map.get("Pwd"));
          String result=PostTest.sendPostToJson(SDKADD+"/utxo/validatekey",map);
          log.info(result);

          return result;
    }



    /**
     * 回收token测试
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

    @Override
    public String freezeToken(String priKey, String tokenType) {
        Map<String ,Object>map=new HashMap<>();
        map.put("PriKey",priKey);
        map.put("TokenType",tokenType);
        String result =PostTest.sendPostToJson(SDKADD+"/utxo/freeze",map);
        log.info(result);
        return result;
    }

    @Override
    public String recoverFrozenToken(String priKey, String tokenType) {
        Map<String ,Object>map=new HashMap<>();
        map.put("PriKey",priKey);
        map.put("TokenType",tokenType);
        String result =PostTest.sendPostToJson(SDKADD+"/utxo/recover",map);
        log.info(result);
        return result;
    }


    //同步接口实现
    @Override
    public String SyncFreezeToken(String priKey,String timeout, String tokenType) {
        Map<String ,Object>map=new HashMap<>();
        map.put("PriKey",priKey);
        map.put("TokenType",tokenType);
        String result =PostTest.sendPostToJson(SDKADD+"/sync/utxo/freeze?timeout="+timeout,map);
        log.info(result);
        return result;
    }

    @Override
    public String SyncRecoverFrozenToken(String priKey,String timeout, String tokenType) {
        Map<String ,Object>map=new HashMap<>();
        map.put("PriKey",priKey);
        map.put("TokenType",tokenType);
        String result =PostTest.sendPostToJson(SDKADD+"/sync/utxo/recover?timeout="+timeout,map);
        log.info(result);
        return result;
    }

    @Override
    public String SyncCollAddress(String timeout,String... address) {
        Map<String, Object> map = new HashMap<>();
        List<Object> addrs = new ArrayList<>();
        for (int i=0;i<address.length;i++){
            addrs.add(address[i]);
        }
        map.put("Addrs", addrs);
        String result = PostTest.sendPostToJson(SDKADD + "/sync/utxo/colladdress?timeout="+timeout, map);
        log.info(result);
        return result;
    }

    @Override
    public String SyncDelCollAddress(String timeout,String... address) {
        Map<String,Object> map = new HashMap<>();
        List<Object> addrs = new ArrayList<>();
        for (int i= 0;i<address.length;i++){
            addrs.add(address[i]);
        }
        map.put("Addrs", addrs);
        String result = PostTest.sendPostToJson(SDKADD + "/sync/utxo/deladdress?timeout="+timeout, map);
        log.info(result);
        return result;
    }
    @Override
    public String SyncAddissueaddress(String timeout,String ...address) {
        Map<String, Object> map = new HashMap<>();
        List<Object> addrs = new ArrayList<>();
        for (int i=0;i<address.length;i++){
            addrs.add(address[i]);
        }

        map.put("Addrs", addrs);
        String result = PostTest.sendPostToJson(SDKADD + "/sync/utxo/addissueaddress?timeout="+timeout, map);
        log.info(result);
        return result;
    }
    @Override
    public String SyncDelissueaddress(String timeout,String... address) {
        Map<String,Object> map = new HashMap<>();
        List<Object> addrs = new ArrayList<>();
        for (int i= 0;i<address.length;i++){
            addrs.add(address[i]);
        }
        map.put("Addrs", addrs);
        String result = PostTest.sendPostToJson(SDKADD + "/sync/utxo/delissueaddress?timeout="+timeout, map);
        log.info(result);
        return result;
    }

}
/**
 * 单签测试用
 * 初始化-发行token至归集地址
 * @return  返回发行token的tokenType币种类型
 * @throws Exception  由于使用了线程的休眠需要抛出异常
 */
/**public  String SoloInit()throws Exception{
 String tokenType ="cx-"+UtilsClass.Random(6);
 log.info("\n发行Token\n");
 String response = TestissueToken(tokenType);
 JSONObject jsonObject = JSONObject.fromObject(response);
 String issueHash = jsonObject.getJSONObject("Data").get("Tx").toString();
 log.info("\n第一次签名\n");
 Thread.sleep(1000*3);
 String signResponse1 = TestSign(issueHash, PRIKEY1, PWD1);
 Thread.sleep(1000*3);
 JSONObject signJsonObject = JSONObject.fromObject(signResponse1);
 String signHash1 = signJsonObject.getJSONObject("Data").get("Tx").toString();
 log.info("\n第二次签名\n");
 String signResponse2 = TestSign(signHash1, PRIKEY2, PWD2);
 JSONObject signJsonObject2 = JSONObject.fromObject(signResponse2);
 String signHash2 = signJsonObject2.getJSONObject("Data").get("Tx").toString();
 log.info("\n第三次签名\n");
 TestSign(signHash2, PRIKEY3);
 Thread.sleep(1000*6);
 log.info("\n查询归集账号的余额\n");
 TestQuery(tokenType);
 return  tokenType;
 }
 */

/*@Test
     public void  run()throws Exception{
    StoreTest storeTest=new StoreTest();
    int count=0;
        //while (true)
         {runTest();
        storeTest.runSDK3Test();
        // Thread.sleep(1000*5);
         count++;
         log.info("第"+count+"次测试");
         }

     }*/
 /*   public void runTest() throws Exception {

        TOKENTYPE="cx-"+UtilsClass.Random(6);
        log.info("\n发行Token\n");
        String response = TestissueToken(TOKENTYPE);
        JSONObject jsonObject = JSONObject.fromObject(response);
        String issueHash = jsonObject.getJSONObject("Data").get("Tx").toString();
        log.info("\n第一次签名\n");
        Thread.sleep(1000*3);
        String signResponse1 = TestSign(issueHash, PRIKEY1, PWD1);
        Thread.sleep(1000*3);
        JSONObject signJsonObject = JSONObject.fromObject(signResponse1);
        String signHash1 = signJsonObject.getJSONObject("Data").get("Tx").toString();
        log.info("\n第二次签名\n");
        String signResponse2 = TestSign(signHash1, PRIKEY2, PWD2);
        JSONObject signJsonObject2 = JSONObject.fromObject(signResponse2);
        String signHash2 = signJsonObject2.getJSONObject("Data").get("Tx").toString();
        log.info("\n第三次签名\n");
        TestSign(signHash2, PRIKEY3);
        Thread.sleep(1000*6);
        log.info("\n查询归集账号的余额\n");
        TestQuery(TOKENTYPE);
        log.info("\n转账\n");
        TestTransfer(TOKENTYPE,USER_COLLET,USER1_2);
        Thread.sleep(1000*5);
        TestTransfer(TOKENTYPE,USER_COLLET,USER1_2, USER1_1);
        Thread.sleep(1000 * 5);
        log.info("\n查询账号余额,用户 user1_2:40 \n");
        TestUserQuery(USER1_2,PRIKEY3);
//        log.info("\n核对私钥正确性\n");
//        CheckPrikey(PRIKEY1,PWD1);
//        log.info("\n核对私钥错误性");
//        CheckPrikey(PRIKEY1,PWD2);
//        CheckPrikey(PRIKEY1,"");
//        log.info("----------------------------------------------------");
//        CheckPrikey(PRIKEY3,PWD2);
//        CheckPrikey("",PWD1);
    }*/
