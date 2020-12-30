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

import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClass.syncTimeout;

@Slf4j
public  class GoStore implements Store {

    /**
     * 获取系统健康状态
     *
     * @method GET
     */

    public String GetApiHealth() {
        String param = "";
        if (!subLedger.isEmpty()) param = param + "&ledger=" + subLedger;
        String result = GetTest.doGet2(SDKADD + "/v2/chain/apihealth?" + param);
        log.info(result);
        return result;

    }

    /**
     *  获取应用链信息
     *
     * @method GET
     */

    public String GetLedger(String ledger) {
        String param = "";
        if (!subLedger.isEmpty()) param = "&ledger=" + subLedger;
        String result = GetTest.doGet2(SDKADD + "/xxxxxxxxxx?" + param);
        log.info(result);
        return result;
    }

    /**
     *  获取应用链信息
     *
     * @method GET
     */

    public String GetLedger() {
//        String param = "";
//        if (!subLedger.isEmpty()) param = "&ledger=" + subLedger;
        String result = GetTest.doGet2(SDKADD + "/v2/ledger");
        log.info(result);
        return result;
    }

    /**
     *  获取节点信息详情
     *
     * @method GET
     */

    public String GetMemberList() {
        String param = "";
        if (!subLedger.isEmpty()) param = "&ledger=" + subLedger;
        String result = GetTest.doGet2(SDKADD + "/v2/memberlist?" + param);
        log.info(result);
        return result;
    }


    /**
     * 获取交易详情。
     */
    public String GetTxDetail(String hash) {
        String param = "";
        if (!subLedger.isEmpty()) param = "&ledger=" + subLedger;
        String result = GetTest.doGet2(SDKADD + "/v2/tx/detail/" + hash + "?" + param);
        log.info(result);
        return result;

    }

    /**
     * 获取交易raw data
     */
    public String GetTxRaw(String hash) {
        String param = "";
        if (!subLedger.isEmpty()) param = "&ledger=" + subLedger;
        String result = GetTest.doGet2(SDKADD + "/v2/tx/raw/" + hash + "?" + param);
        log.info(result);
        return result;

    }


    /**
     * 创建存证交易
     *
     * @author chenxu
     * @version 1.0
     * @method POST
     */

    public String CreateStore(String Data) {
        Map<String, Object> map = new HashMap<>();
        map.put("data", Data);

        String param="";
        if(syncFlag)  param = param + "&sync=true&timeout=" + syncTimeout;
        if(subLedger!="") param = param +"&ledger="+subLedger;

        String result = PostTest.postMethod(SDKADD + "/v2/tx/store?" + param, map);
        log.info(result);
        return result;
    }


    /**
     * 创建隐私存证交易
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
        map.put("data", Data);
        map.put("pubKeys", PubkeysObjects);

        String param="";
        if(syncFlag)  param = param + "&sync=true&timeout=" + syncTimeout;
        if(subLedger!="") param = param +"&ledger="+subLedger;

        String result = PostTest.postMethod(SDKADD + "/v2/tx/store?" + param, map);
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

        String param = "";
        if (!subLedger.isEmpty()) param = "&ledger=" + subLedger;
        String result = GetTest.doGet2(SDKADD + "/v2/tx/detail/" + hash + "?" + param);
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
        String result = GetTest.doGet2(SDKADD + "/getstore2" + "?" + param);
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
        map.put("priKey", priKey);
        map.put("txId", Hash);

        String param = "";
        if (!subLedger.isEmpty()) param = param + "&ledger=" + subLedger;

        String result = PostTest.postMethod(SDKADD + "/v2/tx/store/query?" + param, map);
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
        map.put("priKey", priKey);
        map.put("txId", Hash);
        map.put("password", keyPwd);

        String param="";
//        if(syncFlag)  param = param + "&sync=true&timeout=" + syncTimeout;
        if(subLedger!="") param = param +"&ledger="+subLedger;

        String result = PostTest.postMethod(SDKADD + "/v2/tx/store/query?" + param, map);
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
    public String GetTransactionIndex(String hash) {

        String param = "";
        if (!subLedger.isEmpty()) param = "&ledger=" + subLedger;
        String result = GetTest.doGet2(SDKADD + "/v2/block/tx/index/" + hash + "?" + param);
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
        if (!subLedger.isEmpty()) param = param + "&ledger=" + subLedger;
        String result = GetTest.doGet2(SDKADD + "/v2/block/height" + "?" + param);
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
        String param = "";
        String strHeight = Integer.toString(height);
        if (!subLedger.isEmpty()) param = "&ledger=" + subLedger;
        String result = GetTest.doGet2(SDKADD + "/v2/block/detail/" + strHeight + "?" + param);
        log.info(result);
        return result;
    }

    /**
     * 按高度获取区块原始数据（base64编码）
     */

    public String GetBlockRawDetail(int height) {
        String param = "";
        String strHeight = Integer.toString(height);
        if (!subLedger.isEmpty()) param = "&ledger=" + subLedger;
        String result = GetTest.doGet2(SDKADD + "/v2/block/rawdetail/" + strHeight + "?" + param);
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
        String param = "";
        if (!subLedger.isEmpty()) param = "&ledger=" + subLedger;
        String result = GetTest.doGet2(SDKADD + "/v2/block/detail/" + hash + "?" + param);
//        log.info(result);
        return result;
    }


    /**
     * 按哈希获取所在区块高度
     *
     * @author chenxu
     * @version 1.0
     * @method GET
     */
    public String GetTransactionBlock(String hash) {
        String param = "";
        if (!subLedger.isEmpty()) param = "&ledger=" + subLedger;
        String result = GetTest.doGet2(SDKADD + "/v2/block/height/" + hash + "?" + param);
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
    public String GetInlocal(String hash) {
        String param = "";
        if (!subLedger.isEmpty()) param = "&ledger=" + subLedger;
        String result = GetTest.doGet2(SDKADD + "/v2/tx/inlocal/" + hash + "?" + param);
        log.info(result);
        return result;
    }


    /**
     * 获取节点列表
     *
     * @author chenxu
     * @version 1.0
     * @method Get
     */
    @Override
    public String GetPeerList() {
        String param = "";
        if (!subLedger.isEmpty()) param = param + "&ledger=" + subLedger;
        String result = GetTest.doGet2(SDKADD + "/v2/peerlist?" + param);
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
        map.put("txId", hash);
        map.put("pubKeys", PubkeysObjects);
        map.put("priKey", toPriKey);
        if(!pwd.isEmpty())  map.put("password", pwd);

        String param="";
        if(syncFlag)  param = param + "&sync=true&timeout=" + syncTimeout;
        if(subLedger!="") param = param +"&ledger="+subLedger;

        String result = PostTest.postMethod(SDKADD + "/v2/tx/store/authorize?" + param, map);
        log.info(result);
        return result;
    }

    /**
     * 隐私存证授权-不带密码
     */
    public String StoreAuthorize(String hash, Map toPubKeys, String toPriKey) {

        List<Object> PubkeysObjects = new ArrayList<>();
        for (Object value : toPubKeys.values()) {
            PubkeysObjects.add(value);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("txId", hash);
        map.put("pubKeys", PubkeysObjects);
        map.put("priKey", toPriKey);

        String param="";
        if(syncFlag)  param = param + "&sync=true&timeout=" + syncTimeout;
        if(subLedger!="") param = param +"&ledger="+subLedger;

        String result = PostTest.postMethod(SDKADD + "/v2/tx/store/authorize?" + param, map);
        log.info(result);
        return result;
    }

}
