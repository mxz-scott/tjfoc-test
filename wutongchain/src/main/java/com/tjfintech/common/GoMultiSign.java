package com.tjfintech.common;


import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.utils.GetTest;
import com.tjfintech.common.utils.PostTest;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClass.syncTimeout;


@Slf4j
public class GoMultiSign implements MultiSign {

    /**
     * 回收token，本地签名
     *
     * @param multiAddr 多签地址
     * @param pubKey    公钥
     * @param tokenType 数字货币类型
     * @param amount    货币数量
     */
    public String RecycleLocalSign(String multiAddr, String pubKey, String tokenType, String amount) {
        Map<String, Object> map = new HashMap<>();
        map.put("multiAddress", multiAddr);
        map.put("pubkey", pubKey);
        map.put("tokenType", tokenType);
        map.put("amount", amount);

        String param = "";
        if (syncFlag) param = param + "&sync=true&timeout=" + syncTimeout;
        if (subLedger != "") param = param + "&ledger=" + subLedger;

        String result = PostTest.sendPostToJson(SDKADD + "/v2/tx/utxo/multi/recycle?" + param, map);
//        log.info(result);
        return result;
    }

    /**
     * 添加发行地址
     *
     * @param pubKey
     * @param address
     * @return
     */
    public String addissueaddress(String pubKey, String... address) {
        Map<String, Object> map = new HashMap<>();
        List<Object> addrs = new ArrayList<>();
        for (int i = 0; i < address.length; i++) {
            addrs.add(address[i]);
        }

        map.put("prikey", pubKey);
        map.put("addresses", addrs);

        String param = "";
        if (syncFlag) param = param + "&sync=true&timeout=" + syncTimeout;
        if (subLedger != "") param = param + "&ledger=" + subLedger;

        String result = PostTest.sendPostToJson(SDKADD + "/v2/address/addissue?" + param, map);
        log.info(result);
        return result;
    }

    /**
     * 带私钥的多签Token发行
     *
     * @param MultiAddr
     * @param TokenType
     * @param Amount
     * @param Prikey
     * @param Data
     * @return
     */
    @Override
    public String issueTokenCarryPri(String MultiAddr, String TokenType, String Amount, String Prikey, String Data) {
        Map<String, Object> map = new HashMap<>();
        map.put("multiAddress", MultiAddr);
        map.put("prikey", Prikey);
        map.put("tokenType", TokenType);
        map.put("amount", Amount);
        map.put("data", Data);

        String param = "";
        if (syncFlag) param = param + "&sync=true&timeout=" + syncTimeout;
        if (subLedger != "") param = param + "&ledger=" + subLedger;

        String response = PostTest.sendPostToJson(SDKADD + "/v2/tx/utxo/multi/issue?" + param, map);
        log.info(response);
        return response;
    }

    /**
     * 带私钥带密码的多签Token发行
     *
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
        Map<String, Object> map = new HashMap<>();
        map.put("multiAddress", MultiAddr);
        map.put("prikey", Prikey);
        map.put("password", Pwd);
        map.put("tokenType", TokenType);
        map.put("amount", Amount);
        map.put("data", Data);

        String param = "";
        if (syncFlag) param = param + "&sync=true&timeout=" + syncTimeout;
        if (subLedger != "") param = param + "&ledger=" + subLedger;

        String response = PostTest.sendPostToJson(SDKADD + "/v2/tx/utxo/multi/issue?" + param, map);
        log.info(response);
        return response;
    }

    /**
     * 添加发行地址不携带私钥
     *
     * @param address
     * @return
     */
    @Override
    public String addIssueAddrs(String... address) {
        Map<String, Object> map = new HashMap<>();
        List<Object> addrs = new ArrayList<>();
        for (int i = 0; i < address.length; i++) {
            addrs.add(address[i]);
        }
        map.put("addresses", addrs);

        String param = "";
        if (syncFlag) param = param + "&sync=true&timeout=" + syncTimeout;
        if (subLedger != "") param = param + "&ledger=" + subLedger;

        String result = PostTest.sendPostToJson(SDKADD + "/v2/address/addissue?" + param, map);
        log.info(result);
        return result;
    }

    /**
     * 转账，本地签名
     *
     * @param PubKey   公钥
     * @param Data     详情内容
     * @param fromAddr 发起地址
     * @return
     */
    public String TransferLocalSign(String fromAddr, String PubKey, String Data, List<Map> tokenList) {

        Map<String, Object> map = new HashMap<>();
        map.put("multiAddress", fromAddr);
        map.put("pubKey", PubKey);
        map.put("data", Data);
        map.put("token", tokenList);

        String param = "";
        if (syncFlag) param = param + "&sync=true&timeout=" + syncTimeout;
        if (subLedger != "") param = param + "&ledger=" + subLedger;

        String result = PostTest.sendPostToJson(SDKADD + "/v2/tx/utxo/multi/transfer?" + param, map);
//        log.info(result);
        return result;

    }

    /**
     * 多账号同时回收，本地签名
     *
     * @param tokenList
     * @return
     */
    public String RecyclesLocalSign(List<Map> tokenList) {
        Map<String, Object> map = new HashMap<>();
        map.put("token", tokenList);

        String param = "";
        if (syncFlag) param = param + "&sync=true&timeout=" + syncTimeout;
        if (subLedger != "") param = param + "&ledger=" + subLedger;

        String result = PostTest.sendPostToJson(SDKADD + "/v2/tx/utxo/multi/destory?" + param, map);
        log.info(result);
        return result;
    }


    /**
     * 删除发行地址不携带私钥
     *
     * @param address
     * @return
     */
    @Override
    public String delIssueaddrs(String... address) {
        Map<String, Object> map = new HashMap<>();
        List<Object> addrs = new ArrayList<>();
        for (int i = 0; i < address.length; i++) {
            addrs.add(address[i]);
        }
        map.put("addresses", addrs);

        String param = "";
        if (syncFlag) param = param + "&sync=true&timeout=" + syncTimeout;
        if (subLedger != "") param = param + "&ledger=" + subLedger;

        String result = PostTest.sendPostToJson(SDKADD + "/v2/address/delissue?" + param, map);
        log.info(result);
        return result;
    }

    /**
     * 获取总发行量,总回收量,总冻结量
     *
     * @param StartTime
     * @param EndTime
     * @param TokenType
     * @return
     */
    @Override
    public String gettotal(long StartTime, long EndTime, String TokenType) {
        Map<String, Object> map = new HashMap<>();
        map.put("startTime", StartTime);
        map.put("endTime", EndTime);
        map.put("tokenType", TokenType);

        String param = "";
//        if(syncFlag)  param = param + "&sync=true&timeout=" + syncTimeout;
        if (subLedger != "") param = param + "&ledger=" + subLedger;

        String result = PostTest.postMethod(SDKADD + "/v2/tx/utxo/assets/total?" + param, map);
        log.info(result);
        return result;
    }

    /**
     * 获取总发行量,总回收量,总冻结量(将tokentype类型设置为double类型)
     *
     * @param StartTime
     * @param EndTime
     * @param TokenType
     * @return
     */
    @Override
    public String gettotal(long StartTime, long EndTime, double TokenType) {
        Map<String, Object> map = new HashMap<>();
        map.put("startTime", StartTime);
        map.put("endTime", EndTime);
        map.put("tokenType", TokenType);

        String param = "";
//        if(syncFlag)  param = param + "&sync=true&timeout=" + syncTimeout;
        if (subLedger != "") param = param + "&ledger=" + subLedger;

        String result = PostTest.postMethod(SDKADD + "/v2/tx/utxo/assets/total?" + param, map);
        log.info(result);
        return result;
    }

    /**
     * 获取总发行量,总回收量,总冻结量（将参数体设置为空）
     *
     * @return
     */
    @Override
    public String gettotal() {
        Map<String, Object> map = new HashMap<>();

        String param = "";
//        if(syncFlag)  param = param + "&sync=true&timeout=" + syncTimeout;
        if (subLedger != "") param = param + "&ledger=" + subLedger;

        String result = PostTest.postMethod(SDKADD + "/v2/tx/utxo/assets/total?" + param, map);
        log.info(result);
        return result;
    }

    /**
     * 获取tokentype发行量
     *
     * @param TokenType
     * @return
     */
    @Override
    public String tokenstate(String TokenType) {
        String param = "";
//        if(syncFlag)  param = param + "&sync=true&timeout=" + syncTimeout;
        if (subLedger != "") param = param + "&ledger=" + subLedger;

        String result = GetTest.SendGetTojson(SDKADD + "/v2/tx/utxo/assets/state/" + TokenType + "?" + param);
        log.info(result);
        return result;
    }

    /**
     * 根据tokentype获取账户余额
     *
     * @param TokenType
     * @param Address
     * @return
     */
    @Override
    public String getSDKBalance(String TokenType, String Address) {
        Map<String, Object> map = new HashMap<>();
        map.put("TokenType", TokenType);
        map.put("Addr", Address);

        String param = "";
//        if(syncFlag)  param = param + "&sync=true&timeout=" + syncTimeout;
        if (subLedger != "") param = param + "&ledger=" + subLedger;

        String result = PostTest.postMethod(SDKADD + "/utxo/getsdkbalance?" + param, map);
        log.info(result);
        return result;
    }

    @Override
    public String getTotalbyDay(int starttime, int endtime) {

        Map<String, Object> map = new HashMap<>();
        map.put("StartTime", starttime);
        map.put("EndTime", endtime);

        String param = "";
//        if(syncFlag)  param = param + "&sync=true&timeout=" + syncTimeout;
        if (subLedger != "") param = param + "&ledger=" + subLedger;

        String result = PostTest.postMethod(SDKADD + "/utxo/totalbyday?" + param, map);
        log.info(result);
        return result;

    }

    @Override
    public String getChainBalance(String addr, String tokenType) {
        Map<String, Object> map = new HashMap<>();
        map.put("Addr", addr);
        map.put("TokenType", tokenType);

        String param = "";
        if (syncFlag) param = param + "&sync=true&timeout=" + syncTimeout;
        if (subLedger != "") param = param + "&ledger=" + subLedger;

        String result = PostTest.postMethod(SDKADD + "/utxo/getchainbalance?" + param, map);
        log.info(result);
        return result;
    }

    /**
     * 获取utxo交易详情
     *
     * @param StartTime
     * @param EndTime
     * @param tokenType
     * @param UTXOtype
     * @param FromAddr
     * @param ToAddr
     * @return
     */
    @Override
    public String getUTXODetail(long StartTime, long EndTime, String tokenType, int UTXOtype, String FromAddr, String ToAddr) {
        Map<String, Object> map = new HashMap<>();
        map.put("startTime", StartTime);
        map.put("endTime", EndTime);
        map.put("tokenType", tokenType);
        map.put("UTXOType", UTXOtype);
        map.put("fromAddress", FromAddr);
        map.put("toAddress", ToAddr);

        String param = "";
//        if(syncFlag)  param = param + "&sync=true&timeout=" + syncTimeout;
        if (subLedger != "") param = param + "&ledger=" + subLedger;

        String result = PostTest.postMethod(SDKADD + "/v2/tx/utxo/detail?" + param, map);
        log.info(result);
        return result;
    }

    /**
     * 获取utxo交易详情（设定ToAddr为数值型数据（负数、正数、浮点数））
     *
     * @param StartTime
     * @param EndTime
     * @param tokenType
     * @param UTXOtype
     * @param FromAddr
     * @param ToAddr
     * @return
     */
    @Override
    public String getUTXODetail(long StartTime, long EndTime, String tokenType, int UTXOtype, String FromAddr, double ToAddr) {
        Map<String, Object> map = new HashMap<>();
        map.put("startTime", StartTime);
        map.put("endTime", EndTime);
        map.put("tokenType", tokenType);
        map.put("UTXOType", UTXOtype);
        map.put("fromAddress", FromAddr);
        map.put("toAddress", ToAddr);

        String param = "";
        if (syncFlag) param = param + "&sync=true&timeout=" + syncTimeout;
        if (subLedger != "") param = param + "&ledger=" + subLedger;

        String result = PostTest.postMethod(SDKADD + "/v2/tx/utxo/detail?" + param, map);
        log.info(result);
        return result;
    }

    /**
     * 获取utxo交易详情（验证UTXOtype）将类型转换为String类型
     *
     * @param StartTime
     * @param EndTime
     * @param tokenType
     * @param UTXOtype
     * @param FromAddr
     * @param ToAddr
     * @return
     */
    @Override
    public String getUTXODetail(long StartTime, long EndTime, String tokenType, String UTXOtype, String FromAddr, String ToAddr) {
        Map<String, Object> map = new HashMap<>();
        map.put("startTime", StartTime);
        map.put("endTime", EndTime);
        map.put("tokenType", tokenType);
        map.put("UTXOType", UTXOtype);
        map.put("fromAddress", FromAddr);
        map.put("toAddress", ToAddr);

        String param = "";
//        if(syncFlag)  param = param + "&sync=true&timeout=" + syncTimeout;
        if (subLedger != "") param = param + "&ledger=" + subLedger;

        String result = PostTest.postMethod(SDKADD + "/v2/tx/utxo/detail?" + param, map);
        log.info(result);
        return result;
    }

    /**
     * 获取utxo交易详情(body体为空的情况下)
     *
     * @return
     */
    @Override
    public String getUTXODetail() {
        Map<String, Object> map = new HashMap<>();

        String param = "";
//        if(syncFlag)  param = param + "&sync=true&timeout=" + syncTimeout;
        if (subLedger != "") param = param + "&ledger=" + subLedger;

        String result = PostTest.postMethod(SDKADD + "/v2/tx/utxo/detail?" + param, map);
        log.info(result);
        return result;
    }


    @Override
    public String BalanceByAddr(String addr, String tokenType) {
        Map<String, Object> map = new HashMap<>();
        map.put("tokenType", tokenType);
        map.put("address", addr);

        String param = "";
//        if(syncFlag)  param = param + "&sync=true&timeout=" + syncTimeout;
        if (subLedger != "") param = param + "&ledger=" + subLedger;

        String result = PostTest.postMethod(SDKADD + "/v2/tx/utxo/balance?" + param, map);
        log.info(result);
        return result;
    }


    public String delCollAddrs(String... address) {
        Map<String, Object> map = new HashMap<>();
        List<Object> addrs = new ArrayList<>();
        for (int i = 0; i < address.length; i++) {
            addrs.add(address[i]);
        }
        map.put("addresses", addrs);

        String param = "";
        if (syncFlag) param = param + "&sync=true&timeout=" + syncTimeout;
        if (subLedger != "") param = param + "&ledger=" + subLedger;

        String result = PostTest.sendPostToJson(SDKADD + "/v2/address/delcoll?" + param, map);
        log.info(result);
        return result;
    }

    /**
     * 添加归集地址不携带私钥
     *
     * @param address
     * @return
     */
    @Override
    public String addCollAddrs(String... address) {
        Map<String, Object> map = new HashMap<>();
        List<Object> addrs = new ArrayList<>();
        for (int i = 0; i < address.length; i++) {
            addrs.add(address[i]);
        }
        map.put("addresses", addrs);

        String param = "";
        if (syncFlag) param = param + "&sync=true&timeout=" + syncTimeout;
        if (subLedger != "") param = param + "&ledger=" + subLedger;

        String result = PostTest.sendPostToJson(SDKADD + "/v2/address/addcoll?" + param, map);
        log.info(result);
        return result;
    }


    /**
     * 创建多签地址
     *
     * @author chenxu
     * @version 1.0
     */

    public String genMultiAddress(int M, Map keyMap) {

        Map<String, Object> map = new HashMap<>();
        List<Object> PubkeysObjects = new ArrayList<>();
        for (Object value : keyMap.values()) {
            PubkeysObjects.add(value);
        }
        map.put("pubkeys", PubkeysObjects);
        map.put("m", M);

        String param = "";
//        if(syncFlag)  param = param + "&sync=true&timeout=" + syncTimeout;
        if (subLedger != "") param = param + "&ledger=" + subLedger;

        String result = PostTest.sendPostToJson(SDKADD + "/v2/multiaddress/gen?" + param, map);
        log.info(result);
        return result;

    }

    /**
     * 使用3/3账户发行Token申请
     *
     * @param MultiAddr 多签地址
     * @param TokenType 币种类型
     * @param Amount    货币数量
     * @param Data      额外数据
     * @return
     */
    public String issueToken(String MultiAddr, String TokenType, String Amount, String Data) {
        Map<String, Object> map = new HashMap<>();
        map.put("multiAddress", MultiAddr);
        map.put("tokenType", TokenType);
        map.put("amount", Amount);
        map.put("data", Data);

        String param = "";
        if (syncFlag) param = param + "&sync=true&timeout=" + syncTimeout;
        if (subLedger != "") param = param + "&ledger=" + subLedger;

        String response = PostTest.sendPostToJson(SDKADD + "/v2/tx/utxo/multi/issue?" + param, map);
        log.info(response);
        return response;
    }

    public String issueToken(String MultiAddr, String ToAddr, String TokenType, String Amount, String Data) {
        Map<String, Object> map = new HashMap<>();
        map.put("multiAddress", MultiAddr);
        map.put("toAddress", ToAddr);
        map.put("tokenType", TokenType);
        map.put("amount", Amount);
        map.put("data", Data);

        String param = "";
        if (syncFlag) param = param + "&sync=true&timeout=" + syncTimeout;
        if (subLedger != "") param = param + "&ledger=" + subLedger;

        String response = PostTest.sendPostToJson(SDKADD + "/v2/tx/utxo/multi/issue?" + param, map);
        log.info(response);
        return response;
    }

    public String issueToken(String MultiAddr, String ToAddr, String TokenType, String Amount, String priKey, String Pwd, String Data) {
        Map<String, Object> map = new HashMap<>();
        map.put("multiAddress", MultiAddr);
        if (!ToAddr.isEmpty()) map.put("toAddress", ToAddr);
        map.put("tokenType", TokenType);
        map.put("amount", Amount);
        if (!priKey.isEmpty()) map.put("priKey", priKey);
        if (!Pwd.isEmpty()) map.put("password", Pwd);
        map.put("data", Data);

        String param = "";
        if (syncFlag) param = param + "&sync=true&timeout=" + syncTimeout;
        if (subLedger != "") param = param + "&ledger=" + subLedger;

        String response = PostTest.sendPostToJson(SDKADD + "/v2/tx/utxo/multi/issue?" + param, map);
        log.info(response);
        return response;
    }


    /**
     * 使用3/3账户发行Token申请，使用本地签名
     *
     * @param MultiAddr 多签地址
     * @param TokenType 币种类型
     * @param Amount    货币数量
     * @param Data      额外数据
     * @return
     */
    public String issueTokenLocalSign(String MultiAddr, String TokenType, String Amount, String Data) {
        Map<String, Object> map = new HashMap<>();
        map.put("multiAddress", MultiAddr);
        map.put("tokenType", TokenType);
        map.put("amount", Amount);
        map.put("data", Data);


        String param = "";
        if (syncFlag) param = param + "&sync=true&timeout=" + syncTimeout;
        if (subLedger != "") param = param + "&ledger=" + subLedger;

        String response = PostTest.sendPostToJson(SDKADD + "/v2/tx/utxo/issue?" + param, map);
        //log.info("发行token："+response);
        return response;
    }


    public String issueTokenLocalSign(String MultiAddr, String toAddr, String TokenType, String Amount, String Data) {
        Map<String, Object> map = new HashMap<>();
        map.put("multiAddress", MultiAddr);
        map.put("toAddress", toAddr);
        map.put("tokenType", TokenType);
        map.put("amount", Amount);
        map.put("data", Data);

        String param = "";
        if (syncFlag) param = param + "&sync=true&timeout=" + syncTimeout;
        if (subLedger != "") param = param + "&ledger=" + subLedger;

        String response = PostTest.sendPostToJson(SDKADD + "/v2/tx/utxo/multi/issue?" + param, map);
        //log.info("发行token："+response);
        return response;
    }


    /**
     * 发送签名
     *
     * @param signedData 本地签名后的数据
     * @return
     */
    public String sendSign(String signedData) {

        Map<String, Object> map = new HashMap<>();
        map.put("data", signedData);

        String param = "";
        if (syncFlag) param = param + "&sync=true&timeout=" + syncTimeout;
        if (subLedger != "") param = param + "&ledger=" + subLedger;

        String response = PostTest.sendPostToJson(SDKADD + "/v2/tx/utxo/multi/send_multisign?" + param, map);
        log.info(response);
        return response;
    }


    /**
     * 签名多签发行Token交易-带密码
     *
     * @param Tx     交易ID
     * @param Prikey 签名所用私钥
     * @param Pwd    签名所用私钥密码
     * @return 返回未经过处理内容
     */

    public String Sign(String Tx, String Prikey, String Pwd) {

        Map<String, Object> map = new HashMap<>();
        map.put("prikey", Prikey);
        map.put("password", Pwd);
        map.put("tx", Tx);
        //String response = PostTest.sendPostToJson(SDKADD+"/utxo/multi/sign", map);

        String param = "";
        if (syncFlag) param = param + "&sync=true&timeout=" + syncTimeout;
        if (subLedger != "") param = param + "&ledger=" + subLedger;

        String response = PostTest.postMethod(SDKADD + "/v2/tx/utxo/multi/sign?" + param, map);
        log.info(response);
        return response;

    }

    public String Sign(String Tx, String Prikey) {

        Map<String, Object> map = new HashMap<>();
        map.put("prikey", Prikey);
        map.put("tx", Tx);

        String param = "";
        if (syncFlag) param = param + "&sync=true&timeout=" + syncTimeout;
        if (subLedger != "") param = param + "&ledger=" + subLedger;

        String response = PostTest.postMethod(SDKADD + "/v2/tx/utxo/multi/sign?" + param, map);
        log.info(response);
        return response;
    }

    /**
     * 转账
     *
     * @param PriKey   私钥
     * @param Pwd      密码
     * @param Data     详情内容
     * @param fromAddr 发起地址
     * @return
     */
    public String Transfer(String PriKey, String Pwd, String Data, String fromAddr, List<Map> tokenList) {

        Map<String, Object> map = new HashMap<>();
        map.put("multiAddress", fromAddr);
        map.put("prikey", PriKey);
        map.put("data", Data);
        map.put("password", Pwd);
        map.put("token", tokenList);

        String param = "";
        if (syncFlag) param = param + "&sync=true&timeout=" + syncTimeout;
        if (subLedger != "") param = param + "&ledger=" + subLedger;

        String result = PostTest.postMethod(SDKADD + "/v2/tx/utxo/multi/transfer?" + param, map);
        log.info(result);
        return result;

    }

    public String Transfer(String PriKey, String Data, String fromAddr, List<Map> tokenList) {

        Map<String, Object> map = new HashMap<>();
        map.put("multiAddress", fromAddr);
        map.put("prikey", PriKey);
        map.put("data", Data);
        map.put("token", tokenList);

        String param = "";
        if (syncFlag) param = param + "&sync=true&timeout=" + syncTimeout;
        if (subLedger != "") param = param + "&ledger=" + subLedger;

        String result = PostTest.postMethod(SDKADD + "/v2/tx/utxo/multi/transfer?" + param, map);
        log.info(result);
        return result;

    }


    /**
     * 核对私钥接口测试
     *
     * @param PriKey 私钥
     * @param Pwd    密码
     */
    public String CheckPriKey(String PriKey, String Pwd) {
        Map<String, Object> map = new HashMap<>();
        map.put("priKey", PriKey);
        map.put("password", Pwd);
        System.out.println(map.get("password"));
        String param = "";
        if (subLedger != "") param = param + "&ledger=" + subLedger;
        String result = PostTest.sendPostToJson(SDKADD + "/utxo/validatekey?" + param, map);
        log.info(result);

        return result;
    }


    /**
     * 回收token测试
     *
     * @param multiAddr 多签地址
     * @param priKey    私钥
     * @param Pwd       私钥密码
     * @param tokenType 数字货币类型
     * @param amount    货币数量
     */
    public String Recycle(String multiAddr, String priKey, String Pwd, String tokenType, String amount) {

        Map<String, Object> map = new HashMap<>();
        map.put("multiAddress", multiAddr);
        map.put("priKey", priKey);
        map.put("password", Pwd);
        map.put("tokenType", tokenType);
        map.put("amount", amount);

        String param = "";
        if (syncFlag) param = param + "&sync=true&timeout=" + syncTimeout;
        if (subLedger != "") param = param + "&ledger=" + subLedger;

        String response = PostTest.sendPostToJson(SDKADD + "/v2/tx/utxo/multi/destroy?" + param, map);
        log.info(response);
        return response;

    }

    public String Recycle(String multiAddr, String priKey, String tokenType, String amount) {

        Map<String, Object> map = new HashMap<>();
        map.put("multiAddress", multiAddr);
        map.put("priKey", priKey);
        map.put("tokenType", tokenType);
        map.put("amount", amount);

        String param = "";
        if (syncFlag) param = param + "&sync=true&timeout=" + syncTimeout;
        if (subLedger != "") param = param + "&ledger=" + subLedger;

        String result = PostTest.sendPostToJson(SDKADD + "/v2/tx/utxo/multi/destroy?" + param, map);
        log.info(result);
        return result;

    }


    /**
     * 查询回收账户余额
     *
     * @param tokenType 数字货币类型
     */
    public String QueryZero(String tokenType) {
        String param = "";
        if (subLedger != "") param = param + "&ledger=" + subLedger;

        String result = (GetTest.SendGetTojson(SDKADD + "/v2/tx/utxo/zeroaddr/balance/" + tokenType + "?" + param));
        log.info(result);
        return result;
    }

    /**
     * 冻结token（不使用私钥的情况）
     *
     * @param tokenType
     * @return
     */
    @Override
    public String freezeToken(String tokenType) {
        Map<String, Object> map = new HashMap<>();
        map.put("tokenType", tokenType);

        String param = "";
        if (syncFlag) param = param + "&sync=true&timeout=" + syncTimeout;
        if (subLedger != "") param = param + "&ledger=" + subLedger;

        String result = PostTest.sendPostToJson(SDKADD + "/v2/tx/utxo/freeze?" + param, map);
        log.info(result);
        return result;
    }

    /**
     * 恢复token(不使用私钥的情况)
     *
     * @param tokenType
     * @return
     */
    @Override
    public String recoverFrozenToken(String tokenType) {
        Map<String, Object> map = new HashMap<>();
        map.put("tokenType", tokenType);

        String param = "";
        if (syncFlag) param = param + "&sync=true&timeout=" + syncTimeout;
        if (subLedger != "") param = param + "&ledger=" + subLedger;

        String result = PostTest.sendPostToJson(SDKADD + "/v2/tx/utxo/recover?" + param, map);
        log.info(result);
        return result;
    }
}


