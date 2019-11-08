package com.tjfintech.common;

import com.tjfintech.common.Interface.Token;
import com.tjfintech.common.utils.GetTest;
import com.tjfintech.common.utils.PostTest;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.SDKADD;

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

        String result = PostTest.postMethod(SDKADD + "/v1/group/create", map);
        log.info(result);
        return result;
    }

    /** 创建账户
     *
     * @param entityID
     * @param entityName
     * @param groupID
     * @param comments
     * @param tags
     * @return
     */
    public String tokenCreateAccount(String entityID, String entityName, String groupID, String comments, Map tags){
        List<Object> tagsArray = new ArrayList<>();
        for (Object value : tags.values()) {
            tagsArray.add(value);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("entityID", entityID);
        map.put("entityName", entityName);
        map.put("groupID", groupID);
        map.put("comments", comments);
        map.put("tags", tagsArray);

        String result = PostTest.postMethod(SDKADD + "/v1/account/create", map);
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
     * @param tags
     * @return
     */
    public String tokenCreateMultiAddr(Map addresses, String name, String minSignatures,
                                         String groupID, String comments, Map tags){

        List<Object> addressesArray = new ArrayList<>();
        for (Object value : addresses.values()) {
            addressesArray.add(value);
        }

        List<Object> tagsArray = new ArrayList<>();
        for (Object value : tags.values()) {
            tagsArray.add(value);
        }

        Map<String, Object> map = new HashMap<>();
        map.put("addresses", addressesArray);
        map.put("name", name);
        map.put("minSignatures", minSignatures);
        map.put("groupID", groupID);
        map.put("comments", comments);
        map.put("tags", tagsArray);

        String result = PostTest.postMethod(SDKADD + "/v1/account/multiaddr/create", map);
        log.info(result);
        return result;
    }

    public String tokenTransfer(String from,String to,String tokenType,String amount,String comments){

        Map<String, String> mapTo = new HashMap();
        mapTo.put("address", to);
        mapTo.put("tokenType", tokenType);
        mapTo.put("amount", amount);

        Map<String, Object> map = new HashMap<>();
        map.put("from", from);
        map.put("to", mapTo.toString());
        map.put("comments", comments);

        String result = PostTest.postMethod(SDKADD + "/v1/token/transfer", map);
        log.info(result);
        return result;
    }

    public String tokenGetBalance(String address,String tokenType){
        String param;
        Map<String,Object>map=new HashMap<>();
        map.put("address",address);
        if(tokenType!="") map.put("tt",tokenType);
        param= GetTest.ParamtoUrl(map);
        String result=GetTest.SendGetTojson(SDKADD+"/v1/token/balance?"+param);
        log.info(result);
        return result ;
    }
    public String tokenGetDestroyBalance(String address,String tokenType){
        String param;
        Map<String,Object>map=new HashMap<>();
        map.put("address",address);
        if(tokenType!="") map.put("tt",tokenType);
        param= GetTest.ParamtoUrl(map);
        String result=GetTest.SendGetTojson(SDKADD+"/v1/token/balance/destroyed?"+param);
        log.info(result);
        return result ;
    }

    /**
     *  添加发行地址
     */
    public String tokenAddMintAddr(String address){

        Map<String, Object> map = new HashMap<>();
        map.put("address", address);

        String result = PostTest.postMethod(SDKADD + "/v1/setting/mintaddr/add", map);
        log.info(result);
        return result;

    }

    /**
     *  添加归集地址
     */
    public String tokenAddCollAddr(String address){

        Map<String, Object> map = new HashMap<>();
        map.put("address", address);

        String result = PostTest.postMethod(SDKADD + "/v1/setting/colladdr/add", map);
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

        String result = PostTest.postMethod(SDKADD + "/v1/token/issue", map);
        log.info(result);
        return result;


    }

    /**
     *  token销毁
     */
    public String tokenDestory(String address,String tokenType,String amount, String comments){
        Map<String, Object> map = new HashMap<>();
        map.put("address", address);
        map.put("tokenType", tokenType);
        map.put("amount", amount);
        map.put("comments", comments);

        String result = PostTest.postMethod(SDKADD + "/v1/token/destroy", map);
        log.info(result);
        return result;
    }


}
