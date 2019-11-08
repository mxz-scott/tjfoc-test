package com.tjfintech.common;

import com.tjfintech.common.Interface.Token;
import com.tjfintech.common.utils.PostTest;
import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public  class GoToken implements Token {

    public static final String APIADDRESS = "http://10.1.3.165:6666";

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

        String result = PostTest.postMethod(APIADDRESS + "/v1/group/create", map);
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

        String result = PostTest.postMethod(APIADDRESS + "/v1/account/create", map);
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

        String result = PostTest.postMethod(APIADDRESS + "/v1/account/mutliaddr/create", map);
        log.info(result);
        return result;
    }

}
