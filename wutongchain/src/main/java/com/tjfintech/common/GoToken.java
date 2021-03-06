package com.tjfintech.common;

import com.tjfintech.common.Interface.Token;
import com.tjfintech.common.utils.GetTest;
import com.tjfintech.common.utils.PostTest;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;

import java.net.URLEncoder;
import java.util.*;

import static com.tjfintech.common.utils.UtilsClass.*;

@Slf4j
public  class GoToken implements Token {

    /** 创建群组
     *
     * @param id
     * @param name
     * @param comments
     * @param tags
     * @return
     */
    public String createGroup(String id, String name, String comments, Map tags){
        List<Object> tagsArray = new ArrayList<>();
        for (Object value : tags.values()) {
            tagsArray.add(value);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("comments", comments);
        map.put("tags", tagsArray);

        String param = "";
        if (!subLedger.isEmpty()) param = param + "&ledger=" + subLedger;

        String result = PostTest.postMethod(SDKADD + "/v1/group/create" + "?" + param, map);
        log.info(result);
        return result;
    }

    /** 创建账户
     *
     * @param entityID
     * @param entityName
     * @param groupID
     * @param comments
     * @param listTag
     * @return
     */
    public String tokenCreateAccount(String entityID, String entityName, String groupID, String comments,  ArrayList<String> listTag){
//        List<Object> tagsArray = new ArrayList<>();
//        for (Object value : tags.values()) {
//            tagsArray.add(value);
//        }

        Map<String, Object> map = new HashMap<>();
        map.put("entityID", entityID);
        map.put("entityName", entityName);
        map.put("groupID", groupID);
        map.put("comments", comments);
        map.put("tags", listTag);

        String param = "";
        if (!subLedger.isEmpty()) param = param + "&ledger=" + subLedger;

        String result = PostTest.postMethod(SDKADD + "/v1/account/create" + "?" + param , map);
        log.info(result);
        return result;
    }

    /** 创建账户(支持数据存证扩展功能)
     */
    public String tokenCreateAccount(String entityID, String entityName, String groupID, String comments, ArrayList<String> listTag,
                                     Map<String, Object> mapSendMsg){

        Map<String, Object> map = new HashMap<>();
        map.put("entityID", entityID);
        map.put("entityName", entityName);
        map.put("groupID", groupID);
        map.put("comments", comments);
        map.put("tags", listTag);

        Iterator iter = mapSendMsg.keySet().iterator();
        while (iter.hasNext()) {
            Object key = iter.next();
            map.put(key.toString(),mapSendMsg.get(key));
        }

        String param = "";
        if(syncFlag)  param = param + "&sync=" + syncFlag + "&timeout=" + syncTimeout;
        if (!subLedger.isEmpty()) param = param + "&ledger=" + subLedger;

        String result = PostTest.postMethod(SDKADD + "/v1/account/create" + "?" + param , map);
        log.info(result);
        return result;
    }

    /** 创建多签账户
     *
     * @param addresses
     * @param name
     * @param minSignatures
     * @param groupID
     * @param comments
     * @param listTag
     * @return
     */
    public String tokenCreateMultiAddr(Map addresses, String name, int minSignatures,
                                         String groupID, String comments, ArrayList<String> listTag){

        List<Object> addressesArray = new ArrayList<>();
        for (Object value : addresses.values()) {
            addressesArray.add(value);
        }

//        List<Object> tagsArray = new ArrayList<>();
//        for (Object value : tags.values()) {
//            tagsArray.add(value);
//        }

        Map<String, Object> map = new HashMap<>();
        map.put("addresses", addressesArray);
        map.put("name", name);
        map.put("minSignatures", minSignatures);
        map.put("groupID", groupID);
        map.put("comments", comments);
        map.put("tags", listTag);

        String param = "";
        if (!subLedger.isEmpty()) param = param + "&ledger=" + subLedger;

        String result = PostTest.postMethod(SDKADD + "/v1/account/multiaddr/create" + "?" + param, map);
        log.info(result);
        return result;
    }

    /** 创建多签账户
     *
     * @param pubkeys
     * @param name
     * @param minSignatures
     * @param groupID
     * @param comments
     * @param listTag
     * @return
     */
    public String tokenCreateMultiAddrByPubkeys(Map pubkeys, String name, int minSignatures,
                                       String groupID, String comments, ArrayList<String> listTag){

        List<Object> addressesArray = new ArrayList<>();
        for (Object value : pubkeys.values()) {
            addressesArray.add(value);
        }


        Map<String, Object> map = new HashMap<>();
        map.put("pubkeys", addressesArray);
        map.put("name", name);
        map.put("minSignatures", minSignatures);
        map.put("groupID", groupID);
        map.put("comments", comments);
        map.put("tags", listTag);

        String param = "";
        if (!subLedger.isEmpty()) param = param + "&ledger=" + subLedger;

        String result = PostTest.postMethod(SDKADD + "/v1/account/multiaddr/create" + "?" + param, map);
        log.info(result);
        return result;
    }

    public String tokenTransfer(String from,String to,String tokenType,String amount,String comments){
        List<Map>tokenList=new ArrayList<>();
        Map<String, String> mapTo = new HashMap();
        mapTo.put("address", to);
        mapTo.put("tokenType", tokenType);
        mapTo.put("amount", amount);
        tokenList.add(mapTo);
        Map<String, Object> map = new HashMap<>();
        map.put("from", from);
        map.put("to", tokenList);
        map.put("comments", comments);

        String param = "";
        if(syncFlag)  param = param + "&sync=" + syncFlag + "&timeout=" + syncTimeout;
        if (!subLedger.isEmpty()) param = param + "&ledger=" + subLedger;

        String result = PostTest.postMethod(SDKADD + "/v1/token/transfer" + "?" + param, map);
        log.info(result);
        return result;
    }

    public String tokenTransferWithID(String from,ArrayList<String> IDList,String comments,List<Map>tokenList) {
        Map<String, Object> map = new HashMap<>();
        map.put("from", from);
        map.put("idlist",IDList);
        map.put("comments", comments);
        if (String.valueOf(tokenList).contains("index")) {
            map.put("utxo", tokenList);
        }else {
            map.put("to", tokenList);
        }

        String param = "";
        if(syncFlag)  param = param + "&sync=" + syncFlag + "&timeout=" + syncTimeout;
        if(subLedger!="") param = param + "&ledger=" + subLedger;

        String result=PostTest.postMethod(SDKADD + "/v1/token/transfer" + "?" + param, map);
        log.info(result);
        return result;

    }

    public String tokenTransfer(String from,String comments,List<Map>tokenList) {
        Map<String, Object> map = new HashMap<>();
        map.put("from", from);
        map.put("comments", comments);
        if (String.valueOf(tokenList).contains("index")) {
            map.put("utxo", tokenList);
        }else {
            map.put("to", tokenList);
        }

        String param = "";
        if(syncFlag)  param = param + "&sync=" + syncFlag + "&timeout=" + syncTimeout;
        if(subLedger!="") param = param + "&ledger=" + subLedger;

        String result=PostTest.postMethod(SDKADD + "/v1/token/transfer" + "?" + param, map);
        log.info(result);
        return result;

    }

    /**
     *  转让token（支持指定utxo未花费列表转让）
     */
    public String tokenTransfer(String from,String comments,List<Map>tokenList,List<Map>UTXOList) {
        Map<String, Object> map = new HashMap<>();
        map.put("from", from);
        map.put("comments", comments);
        map.put("to", tokenList);
        map.put("utxo", UTXOList);

        String param = "";
        if(syncFlag)  param = param + "&sync=" + syncFlag + "&timeout=" + syncTimeout;
        if(subLedger!="") param = param + "&ledger=" + subLedger;

        String result=PostTest.postMethod(SDKADD + "/v1/token/transfer" + "?" + param, map);
        log.info(result);
        return result;

    }

    /**
     *  转让token（支持指定utxo未花费列表转让）
     */
    public String tokenTransferUTXOMsg(String from,String comments,List<Map>UTXOList,Map<String,Object>mapSendMsg) {
        Map<String, Object> map = new HashMap<>();
        map.put("from", from);
        map.put("comments", comments);
        map.put("utxo", UTXOList);

        Iterator iter = mapSendMsg.keySet().iterator();
        while (iter.hasNext()) {
            Object key = iter.next();
            map.put(key.toString(),mapSendMsg.get(key));
        }


        String param = "";
        if(syncFlag)  param = param + "&sync=" + syncFlag + "&timeout=" + syncTimeout;
        if(subLedger!="") param = param + "&ledger=" + subLedger;

        String result=PostTest.postMethod(SDKADD + "/v1/token/transfer" + "?" + param, map);
        log.info(result);
        return result;

    }

    /**
     *  转让token（支持数据存证扩展功能）
     */
    public String tokenTransfer(String from,String comments,List<Map>tokenList,Map<String,Object>mapSendMsg) {
        Map<String, Object> map = new HashMap<>();
        map.put("from", from);
        map.put("comments", comments);
        map.put("to", tokenList);

        Iterator iter = mapSendMsg.keySet().iterator();
        while (iter.hasNext()) {
            Object key = iter.next();
            map.put(key.toString(),mapSendMsg.get(key));
        }

        String param = "";
        if(syncFlag)  param = param + "&sync=" + syncFlag + "&timeout=" + syncTimeout;
        if(subLedger!="") param = param + "&ledger=" + subLedger;

        String result=PostTest.postMethod(SDKADD + "/v1/token/transfer" + "?" + param, map);
        log.info(result);
        return result;

    }

    public String tokenGetBalance(String address,String tokenType){
        String param;
        Map<String,Object>map=new HashMap<>();
        map.put("address",address);
        if(tokenType!="") map.put("tt",tokenType);

        param= GetTest.ParamtoUrl(map);
        if (!subLedger.isEmpty()) param = param + "&ledger=" + subLedger;

        String result=GetTest.doGet2(SDKADD+"/v1/token/balance" + "?" + param);
        log.info(result);
        return result ;
    }
    public String tokenGetDestroyBalance(){
        String param = "";
        if (!subLedger.isEmpty()) param = param + "ledger=" + subLedger;

        String result=GetTest.doGet2(SDKADD+"/v1/token/balance/destroyed" + "?" + param);
        log.info(result);
        return result ;
    }

    /**
     *  添加发行地址
     */
    public String tokenAddMintAddr(String address){

        Map<String, Object> map = new HashMap<>();
        map.put("address", address);

        String param = "";
        if(syncFlag)  param = param + "&sync=" + syncFlag + "&timeout=" + syncTimeout;
        if (!subLedger.isEmpty()) param = param + "&ledger=" + subLedger;

        String result = PostTest.postMethod(SDKADD + "/v1/setting/mintaddr/add" + "?" + param, map);
        log.info(result);
        return result;
    }
    /**
     *  删除发行地址
     */
    public String tokenDelMintAddr(String address){

        Map<String, Object> map = new HashMap<>();
        map.put("address", address);

        String param = "";
        if(syncFlag)  param = param + "&sync=" + syncFlag + "&timeout=" + syncTimeout;
        if (!subLedger.isEmpty()) param = param + "&ledger=" + subLedger;

        String result = PostTest.postMethod(SDKADD + "/v1/setting/mintaddr/remove" + "?" + param, map);
        log.info(result);
        return result;

    }

    /**
     *  添加归集地址
     */
    public String tokenAddCollAddr(String address){

        Map<String, Object> map = new HashMap<>();
        map.put("address", address);

        String param = "";
        if(syncFlag)  param = param + "&sync=" + syncFlag + "&timeout=" + syncTimeout;
        if (!subLedger.isEmpty()) param = param + "&ledger=" + subLedger;

        String result = PostTest.postMethod(SDKADD + "/v1/setting/colladdress/add" + "?" + param, map);
        log.info(result);
        return result;

    }

    /**
     *  删除归集地址
     */
    public String tokenDelCollAddr(String address){

        Map<String, Object> map = new HashMap<>();
        map.put("address", address);

        String param = "";
        if(syncFlag)  param = param + "&sync=" + syncFlag + "&timeout=" + syncTimeout;
        if (!subLedger.isEmpty()) param = param + "&ledger=" + subLedger;

        String result = PostTest.postMethod(SDKADD + "/v1/setting/colladdress/remove" + "?" + param, map);
        log.info(result);
        return result;

    }

    /**
     *  发行token
     */
    public String tokenIssue(String address ,String collAddr,String tokenType,String amount,String comments){

        Map<String, Object> map = new HashMap<>();
        map.put("address", address);
        map.put("collAddr", collAddr);
        map.put("tokenType", tokenType);
        map.put("amount", amount);
        map.put("comments", comments);

        String param = "";
        if(syncFlag)  param = param + "&sync=" + syncFlag + "&timeout=" + syncTimeout;
        if (!subLedger.isEmpty()) param = param + "&ledger=" + subLedger;

        String result = PostTest.postMethod(SDKADD + "/v1/token/issue" + "?" + param, map);
        log.info(result);
        return result;
    }

    /**
     *  发行token，（支持数据存证扩展功能）
     */
    public String tokenIssue(String address ,String collAddr,String tokenType,String amount,String comments,Map<String, Object>mapSendMsg){

        Map<String, Object> map = new HashMap<>();
        map.put("address", address);
        map.put("collAddr", collAddr);
        map.put("tokenType", tokenType);
        map.put("amount", amount);
        map.put("comments", comments);

        Iterator iter = mapSendMsg.keySet().iterator();
        while (iter.hasNext()) {
            Object key = iter.next();
            map.put(key.toString(),mapSendMsg.get(key));
        }

        String param = "";
        if(syncFlag)  param = param + "&sync=" + syncFlag + "&timeout=" + syncTimeout;
        if (!subLedger.isEmpty()) param = param + "&ledger=" + subLedger;

        String result = PostTest.postMethod(SDKADD + "/v1/token/issue" + "?" + param, map);
        log.info(result);
        return result;
    }

    /**
     *  token销毁
     */
    public String tokenDestoryByTokenType(String tokenType,String comments){
        Map<String, Object> map = new HashMap<>();
        map.put("tokenType", tokenType);
        map.put("comments", comments);

        String param = "";
        if(syncFlag)  param = param + "&sync=" + syncFlag + "&timeout=" + syncTimeout;
        if (!subLedger.isEmpty()) param = param + "&ledger=" + subLedger;

        String result = PostTest.postMethod(SDKADD + "/v1/token/destroy/bytype" + "?" + param, map);
        log.info(result);
        return result;
    }

    /**
     *  token销毁（支持数据存证扩展功能）
     */
    public String tokenDestoryByTokenType(String tokenType,String comments,Map<String,Object>mapSendMsg){
        Map<String, Object> map = new HashMap<>();
        map.put("tokenType", tokenType);
        map.put("comments", comments);

        Iterator iter = mapSendMsg.keySet().iterator();
        while (iter.hasNext()) {
            Object key = iter.next();
            map.put(key.toString(),mapSendMsg.get(key));
        }
        String param = "";
        if(syncFlag)  param = param + "&sync=" + syncFlag + "&timeout=" + syncTimeout;
        if (!subLedger.isEmpty()) param = param + "&ledger=" + subLedger;

        String result = PostTest.postMethod(SDKADD + "/v1/token/destroy/bytype" + "?" + param, map);
        log.info(result);
        return result;
    }

    /**
     *  token销毁
     */
    public String tokenDestoryByList(String address,String tokenType ,String amount, String comments){
        List<Map>tokenList=new ArrayList<>();
        Map<String, String> mapTo = new HashMap();
        mapTo.put("address", address);
        mapTo.put("tokenType", tokenType);
        mapTo.put("amount", amount);
        tokenList.add(mapTo);
        Map<String, Object> map = new HashMap<>();
        map.put("list", tokenList);
        map.put("comments", comments);

        String param = "";
        if(syncFlag)  param = param + "&sync=" + syncFlag + "&timeout=" + syncTimeout;
        if (!subLedger.isEmpty()) param = param + "&ledger=" + subLedger;

        String result = PostTest.postMethod(SDKADD + "/v1/token/destroy" + "?" + param, map);
        log.info(result);
        return result;
    }
    /**
     *  token销毁
     */
    public String tokenDestoryByList(List<Map> tokenList, String comments){
        Map<String, Object> map = new HashMap<>();
        map.put("list", tokenList);
        map.put("comments",comments);

        String param = "";
        if(syncFlag)  param = param + "&sync=" + syncFlag + "&timeout=" + syncTimeout;
        if (!subLedger.isEmpty()) param = param + "&ledger=" + subLedger;

        String result = PostTest.postMethod(SDKADD + "/v1/token/destroy" + "?" + param, map);
        log.info(result);
        return result;
    }

    /**
     *  token销毁（支持数据存证扩展功能）
     */
    public String tokenDestoryByList(List<Map> tokenList, String comments,Map<String, Object>mapSendMsg){
        Map<String, Object> map = new HashMap<>();
        map.put("list", tokenList);
        map.put("comments",comments);

        Iterator iter = mapSendMsg.keySet().iterator();
        while (iter.hasNext()) {
            Object key = iter.next();
            map.put(key.toString(),mapSendMsg.get(key));
        }

        String param = "";
        if(syncFlag)  param = param + "&sync=" + syncFlag + "&timeout=" + syncTimeout;
        if (!subLedger.isEmpty()) param = param + "&ledger=" + subLedger;

        String result = PostTest.postMethod(SDKADD + "/v1/token/destroy" + "?" + param, map);
        log.info(result);
        return result;
    }

    /**
     *  token销毁（支持通过UTXO回收功能）
     */
    public String tokenDestoryByList(List<Map> tokenList,List<Map> utxoList, String comments,Map<String, Object>mapSendMsg){
        Map<String, Object> map = new HashMap<>();
        map.put("list", tokenList);
        map.put("utxo", utxoList);
        map.put("comments",comments);

        Iterator iter = mapSendMsg.keySet().iterator();
        while (iter.hasNext()) {
            Object key = iter.next();
            map.put(key.toString(),mapSendMsg.get(key));
        }

        String param = "";
        if(syncFlag)  param = param + "&sync=" + syncFlag + "&timeout=" + syncTimeout;
        if (!subLedger.isEmpty()) param = param + "&ledger=" + subLedger;

        String result = PostTest.postMethod(SDKADD + "/v1/token/destroy" + "?" + param, map);
        log.info(result);
        return result;
    }

    /**
     * 数据存证扩展
     */

    public String tokenSendMsg(Map<String,Object>mapSendMsg) {
        Map<String, Object> map = new HashMap<>();

        Iterator iter = mapSendMsg.keySet().iterator();
        while (iter.hasNext()) {
            Object key = iter.next();
            map.put(key.toString(),mapSendMsg.get(key));
        }

        String param = "";
        if(syncFlag)  param = param + "&sync=" + syncFlag + "&timeout=" + syncTimeout;
        if (!subLedger.isEmpty()) param = param + "&ledger=" + subLedger;

        String result = PostTest.postMethod(SDKADD + "/v1/sendmsg" + "?" + param, map);
        log.info(result);
        return result;
    }

    public String tokenCreateStore(String Data) {
        Map<String, Object> map = new HashMap<>();
        map.put("Data", Data);
        String param = "";
        if(syncFlag)  param = param + "&sync=" + syncFlag + "&timeout=" + syncTimeout;
        if (!subLedger.isEmpty()) param = param + "&ledger=" + subLedger;

        String result = PostTest.postMethod(SDKADD + "/v1/store" + "?" + param, map);
        log.info(result);
        return result;
    }

    public String tokenCreatePrivateStore(String Data, Map keyMap) {

        List<Object> addrsObjects = new ArrayList<>();
        for (Object value : keyMap.values()) {
            addrsObjects.add(value);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("Data", Data);
        map.put("addresses", addrsObjects);
        String param = "";
        if(syncFlag)  param = param + "&sync=" + syncFlag + "&timeout=" + syncTimeout;
        if (!subLedger.isEmpty()) param = param + "&ledger=" + subLedger;

        String result = PostTest.postMethod(SDKADD + "/v1/store" + "?" + param, map);
        log.info(result);
        return result;
    }

    public String tokenGetPrivateStore(String Hash, String addr) {
        Map<String, Object> map = new HashMap<>();
        if (!addr.isEmpty()) map.put("address", addr);
        map.put("Hash", Hash);
        String param = "";
        if (!subLedger.isEmpty()) param = param + "&ledger=" + subLedger;

        String result = PostTest.postMethod(SDKADD + "/v1/getstore" + "?" + param, map);
        log.info(result);
        return result;
    }

    public String tokenFreezeToken(String tokenType) {
        Map<String ,Object>map=new HashMap<>();
        map.put("tokenType",tokenType);
        String param="";
        if(syncFlag)  param = param + "&sync=" + syncFlag + "&timeout=" + syncTimeout;
        if(subLedger!="") param = param +"&ledger="+subLedger;

        String result =PostTest.postMethod(SDKADD+"/v1/token/freeze" + "?" + param,map);
        log.info(result);
        return result;
    }

    public String tokenRecoverToken(String tokenType) {
        Map<String ,Object>map=new HashMap<>();
        map.put("TokenType",tokenType);
        String param="";
        if(syncFlag)  param = param + "&sync=" + syncFlag + "&timeout=" + syncTimeout;
        if(subLedger!="") param = param + "&ledger="+subLedger;

        String result =PostTest.postMethod(SDKADD+"/v1/token/recover" + "?" + param,map);
        log.info(result);
        return result;
    }

    public String tokenGetPubkey(String address){
        Map<String ,Object>map=new HashMap<>();
        map.put("address",address);
        String param="";
        if(subLedger!="") param = param +"&ledger="+subLedger;

        String result =PostTest.postMethod(SDKADD+"/v1/account/getpublickey" + "?" + param,map);
        log.info(result);
        return result;
    }

    public String tokenGetTxDetail(String hashData){
        String param;
        String hashEncode = URLEncoder.encode(hashData);
        Map<String, Object> map = new HashMap<>();
        map.put("hash", hashEncode);
        param = GetTest.ParamtoUrl(map);
        if (!subLedger.isEmpty()) param = param + "&ledger=" + subLedger;

        String result = GetTest.doGet2(SDKADD + "/v1/gettxdetail" + "?" + param);
        log.info(result);
        return result;
    }

    public String tokenGetApiHealth(){
        String param="";
        if(subLedger!="") param = param +"&ledger="+subLedger;

        String result = GetTest.doGet2(SDKADD + "/v1/apihealth"+ "?" + param );
        log.info(result);
        return result;
    }

    public String GetRawTx(String hashData) {
        String param;
        String hashEncode = URLEncoder.encode(hashData);
        Map<String, Object> map = new HashMap<>();
        map.put("hashData", hashEncode);
        param = GetTest.ParamtoUrl(map);
        if (!subLedger.isEmpty()) param = param + "&ledger=" + subLedger;
        String result = GetTest.doGet2(SDKADD + "/getrawtx" + "?" + param);
        log.info(result);
        return result;

    }
}
