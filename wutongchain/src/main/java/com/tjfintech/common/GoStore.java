package com.tjfintech.common;

import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.utils.GetTest;
import com.tjfintech.common.utils.PostTest;
import lombok.extern.slf4j.Slf4j;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import static com.tjfintech.common.utils.UtilsClass.SDKADD;
import static com.tjfintech.common.utils.UtilsClass.subLedger;

@Slf4j
public  class GoStore implements Store {

    /**
     * 获取系统健康状态
     *
     * @method GET
     */

    public String GetApiHealth() {
        //String result= GetTest.SendGetTojson(SDKADD+"/apihealth");
        String result = GetTest.doGet2(SDKADD + "/apihealth");
        log.info(result);
        return result;

    }

    /**
     * 获取交易详情
     *
     * @author chenxu
     * @version 1.0
     * @method GET
     */
    public String GetTransaction(String hashData) {
        String param;
        String hashEncode = URLEncoder.encode(hashData);
        Map<String, Object> map = new HashMap<>();
        map.put("hashData", hashEncode);
        param = GetTest.ParamtoUrl(map);
        if (!subLedger.isEmpty()) param = param + "&ledger=" + subLedger;
        String result = GetTest.doGet2(SDKADD + "/gettransaction" + "?" + param);
        log.info(result);
        return result;

    }

    public String GetTxDetail(String hashData) {
        String param;
        String hashEncode = URLEncoder.encode(hashData);
        Map<String, Object> map = new HashMap<>();
        map.put("hashData", hashEncode);
        param = GetTest.ParamtoUrl(map);
        if (!subLedger.isEmpty()) param = param + "&ledger=" + subLedger;
        String result = GetTest.doGet2(SDKADD + "/gettxdetail" + "?" + param);
        log.info(result);
        return result;

    }


    /**
     * 创建存证交易-带公私钥
     *
     * @author chenxu
     * @version 1.0
     * @method POST
     */

    public String CreateStore(String Data) {
        Map<String, Object> map = new HashMap<>();
        map.put("Data", Data);
        String param = "";
        if (!subLedger.isEmpty()) param = param + "?ledger=" + subLedger;
        String result = PostTest.sendPostToJson(SDKADD + "/store" + param, map);
        log.info(result);
        return result;
    }


//    /**
//     * 创建存证交易-带公钥
//     *
//     * @param Data
//     * @param PubKeys
//     * @return
//     */
//    @Override
//    public String CreateStore(String Data, String... PubKeys) {
//        Map<String, Object> map = new HashMap<>();
//        List<Object> addrs = new ArrayList<>();
//        for (int i = 0; i < PubKeys.length; i++) {
//            addrs.add(PubKeys[i]);
//        }
//        map.put("Addrs", addrs);
//        map.put("Data", Data);
//        String param = "";
//        if (!subLedger.isEmpty()) param = param + "?ledger=" + subLedger;
//        String result = PostTest.sendPostToJson(SDKADD + "/store" + param, map);
//        log.info(result);
//        return result;
//    }

    /***
     * 同步创建存证交易
     * @param timeout
     * @param Data
     * @return
     */
    @Override
    public String SynCreateStore(Integer timeout, String Data) {
        Map<String, Object> map = new HashMap<>();
        map.put("Data", Data);
        String param = "";
        if (!subLedger.isEmpty()) param = param + "&ledger=" + subLedger;
        String result = PostTest.sendPostToJson(SDKADD + "/sync/store?timeout=" + timeout + param, map);
        log.info(result);
        return result;
    }

    /**
     * 同步创建存证交易-带公钥
     *
     * @param timeout
     * @param Data
     * @param PubKeys
     * @return
     */
    @Override
    public String SynCreateStore(Integer timeout, String Data, String... PubKeys) {
        Map<String, Object> map = new HashMap<>();
        List<Object> addrs = new ArrayList<>();
        for (int i = 0; i < PubKeys.length; i++) {
            addrs.add(PubKeys[i]);
        }
        map.put("Addrs", addrs);
        map.put("Data", Data);

        String param = "";
        if (!subLedger.isEmpty()) param = param + "&ledger=" + subLedger;

        String result = PostTest.sendPostToJson(SDKADD + "/sync/store?timeout=" + timeout + param, map);
        log.info(result);
        return result;
    }

    /**
     * 同步获取隐私存证
     *
     * @param timeout
     * @param Hash
     * @param priKey
     * @param keyPwd
     * @return
     */
    @Override
    public String SynGetStorePost(Integer timeout, String Hash, String priKey, String keyPwd) {
        Map<String, Object> map = new HashMap<>();
        map.put("Prikey", priKey);
        map.put("Hash", Hash);
        map.put("KeyPwd", keyPwd);
        String result = PostTest.sendPostToJson(SDKADD + "/sync/getstore?timeout=" + timeout, map);
        log.info(result);
        return result;
    }

    /**
     * 同步创建隐私存证
     *
     * @param Data
     * @param keyMap
     * @return
     */
    @Override
    public String SynCreatePrivateStore(Integer timeout, String Data, Map keyMap) {
        List<Object> PubkeysObjects = new ArrayList<>();
        for (Object value : keyMap.values()) {
            PubkeysObjects.add(value);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("Data", Data);
        map.put("Pubkeys", PubkeysObjects);
        String result = PostTest.sendPostToJson(SDKADD + "/sync/store?timeout=" + timeout, map);
        log.info(result);
        return result;
    }


    /**
     * 创建带密码存证交易
     *
     * @author chenxu
     * @version 1.0
     * @method POST
     */

    public String CreatePrivateStore(String Data, Map keyMap) {

        List<Object> PubkeysObjects = new ArrayList<>();
        for (Object value : keyMap.values()) {
            PubkeysObjects.add(value);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("Data", Data);
        map.put("Pubkeys", PubkeysObjects);
        String param = "";
        if (!subLedger.isEmpty()) param = param + "?ledger=" + subLedger;
        String result = PostTest.postMethod(SDKADD + "/store" + param, map);
        log.info(result);
        return result;
    }


    /***
     * 查询存证交易
     * @author chenxu
     * @version 1.0
     * @method GET
     */
    public String GetStore(String hash) {
        String param;
        String hashEncode = URLEncoder.encode(hash);
        //hash需要urlEncode编码
        Map<String, Object> map = new HashMap<>();
        map.put("hash", hashEncode);
        param = GetTest.ParamtoUrl(map);
        if (!subLedger.isEmpty()) param = param + "&ledger=" + subLedger;
        String result = GetTest.SendGetTojson(SDKADD + "/getstore" + "?" + param);
        log.info(result);
        return result;
    }


    public String GetStoreLocal(String hash) {
        String param;
        String hashEncode = URLEncoder.encode(hash);
        //hash需要urlEncode编码
        Map<String, Object> map = new HashMap<>();
        map.put("hash", hashEncode);
        param = GetTest.ParamtoUrl(map);
        if (!subLedger.isEmpty()) param = param + "&ledger=" + subLedger;
        String result = GetTest.SendGetTojson(SDKADD + "/getstore2" + "?" + param);
//        log.info(result);
        return result;
    }


    /***
     * 获取隐私存证
     * @author chenxu
     * @version 1.0
     * @method POST
     */
    public String GetStorePost(String Hash, String priKey) {
        Map<String, Object> map = new HashMap<>();
        map.put("Prikey", priKey);
        map.put("Hash", Hash);
        String param = "";
        if (!subLedger.isEmpty()) param = param + "?ledger=" + subLedger;
        String result = PostTest.sendPostToJson(SDKADD + "/getstore" + param, map);
        log.info(result);
        return result;
    }

    /**
     * 获取带密码隐私存证
     *
     * @author chenxu
     * @version 1.0
     * @method POST
     */
    public String GetStorePostPwd(String Hash, String priKey, String keyPwd) {

        Map<String, Object> map = new HashMap<>();
        map.put("Prikey", priKey);
        map.put("Hash", Hash);
        map.put("KeyPwd", keyPwd);
        String param = "";
        if (!subLedger.isEmpty()) param = param + "?ledger=" + subLedger;
        String result = PostTest.sendPostToJson(SDKADD + "/getstore" + param, map);
        log.info(result);
        return result;

    }

    /**
     * 获取交易索引
     *
     * @author chenxu
     * @version 1.0
     * @method POST
     */
    public String GetTransactionIndex(String hashData) {

        String param;
        String hashencode = URLEncoder.encode(hashData);
        Map<String, Object> map = new HashMap<>();
        map.put("hashData", hashencode);
        param = GetTest.ParamtoUrl(map);
        if (!subLedger.isEmpty()) param = param + "&ledger=" + subLedger;
        String result = GetTest.SendGetTojson(SDKADD + "/gettransactionindex" + "?" + param);
        log.info(result);
        return result;

    }

    /***
     * 获取区块高度
     * @author chenxu
     * @version 1.0
     * @method GET
     */
    public String GetHeight() {
        String param = "";
        if (!subLedger.isEmpty()) param = param + "?ledger=" + subLedger;
        String result = GetTest.SendGetTojson(SDKADD + "/getheight" + param);
        log.info(result);
        return result;
    }

    /**
     * 按高度获取区块信息
     *
     * @author chenxu
     * @version 1.0
     * @method GET
     */
    public String GetBlockByHeight(int height) {
        String param;
        Map<String, Object> map = new HashMap<>();
        map.put("number", height);
        param = GetTest.ParamtoUrl(map);
        if (!subLedger.isEmpty()) param = param + "&ledger=" + subLedger;
        String result = GetTest.SendGetTojson(SDKADD + "/getblockbyheight" + "?" + param);
        log.info(result);
        return result;
    }

    /**
     * 按哈希获取区块信息
     *
     * @author chenxu
     * @version 1.0
     * @method GET
     */
    public String GetBlockByHash(String hash) {
        String param;
        String hashencode = URLEncoder.encode(hash);
        Map<String, Object> map = new HashMap<>();
        map.put("hash", hashencode);
        param = GetTest.ParamtoUrl(map);
        if (!subLedger.isEmpty()) param = param + "&ledger=" + subLedger;
        String result = (GetTest.SendGetTojson(SDKADD + "/getblockbyhash" + "?" + param));
        log.info(result);
        return result;
    }


    /**
     * 交易复杂查询
     *
     * @author chenxu
     * @version 1.0
     * @method GET
     */
    public String GetTxSearch(int skip, int size, String regex) {
        Map<String, Object> map = new HashMap<>();
        map.put("skip", skip);
        map.put("size", size);
        Map<String, Object> regexmap = new HashMap<>();
        regexmap.put("$regex", regex);
        Map<String, Object> datamap = new HashMap<>();
        datamap.put("scargs.data", regexmap);
        map.put("qry", datamap);
        String param = "";
        if (!subLedger.isEmpty()) param = param + "?ledger=" + subLedger;
        log.info("应该查询出" + size + "条数据");
        String result = PostTest.sendPostToJson(SDKADD + "/tx/search" + param, map);
        log.info(result);
        return result;

    }

    /**
     * 查询交易是否存在于钱包数据库
     *
     * @author chenxu
     * @version 1.0
     * @method Get
     */
    public String GetInlocal(String hashcode) {
        String hash = URLEncoder.encode(hashcode);
        Map<String, Object> map = new HashMap<>();
        map.put("hash", hash);
        String param = GetTest.ParamtoUrl(map);
        if (!subLedger.isEmpty()) param = param + "&ledger=" + subLedger;
        String result = GetTest.SendGetTojson(SDKADD + "/tx/inlocal" + "?" + param);
        log.info(result);
        return result;
    }

    /**
     * 统计某种交易类型的数量
     *
     * @author chenxu
     * @version 1.0
     * @method GET
     */
    public String GetStat(String type) {
        Map<String, Object> map = new HashMap<>();
        map.put("type", type);
        String param = GetTest.ParamtoUrl(map);
        String result = GetTest.SendGetTojson(SDKADD + "/tx/stat" + "?" + param);
        log.info(result);
        return result;
    }

    @Override
    public String GetTransactionBlock(String hash) {
        String hashcode = URLEncoder.encode(hash);
        Map<String, Object> map = new HashMap<>();
        map.put("hashData", hashcode);
        String param = GetTest.ParamtoUrl(map);
        if (!subLedger.isEmpty()) param = param + "&ledger=" + subLedger;
        String result = GetTest.SendGetTojson(SDKADD + "/gettransactionblock" + "?" + param);
        log.info(result);
        return result;
    }


    @Override
    public String GetPeerList() {
        String param = "";
        if (!subLedger.isEmpty()) param = param + "?ledger=" + subLedger;
        String result = GetTest.SendGetTojson(SDKADD + "/getpeerlist" + param);
        log.info(result);
        return result;
    }

    /**
     * 隐私存证授权
     */
    public String StoreAuthorize(String hash, Map toPubKeys, String toPriKey) {

        List<Object> PubkeysObjects = new ArrayList<>();
        for (Object value : toPubKeys.values()) {
            PubkeysObjects.add(value);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("Hash", hash);
        map.put("Pubkeys", PubkeysObjects);
        map.put("PriKey", toPriKey);
        String param = "";
        if (!subLedger.isEmpty()) param = param + "?ledger=" + subLedger;
        String result = PostTest.postMethod(SDKADD + "/storeauthorize" + param, map);
        log.info(result);
        return result;
    }

    /**
     * 隐私存证授权-带密码
     */
    public String StoreAuthorize(String hash, Map toPubKeys, String toPriKey, String pwd) {

        List<Object> PubkeysObjects = new ArrayList<>();
        for (Object value : toPubKeys.values()) {
            PubkeysObjects.add(value);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("Hash", hash);
        map.put("Pubkeys", PubkeysObjects);
        map.put("PriKey", toPriKey);
        map.put("KeyPwd", pwd);
        String param = "";
        if (!subLedger.isEmpty()) param = param + "?ledger=" + subLedger;
        String result = PostTest.postMethod(SDKADD + "/storeauthorize" + param, map);
        log.info(result);
        return result;
    }

}
