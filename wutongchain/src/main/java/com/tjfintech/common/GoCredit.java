package com.tjfintech.common;

import com.tjfintech.common.Interface.Credit;
import com.tjfintech.common.utils.GetTest;
import com.tjfintech.common.utils.PostTest;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.SDKADD;

@Slf4j
public class GoCredit implements Credit {

    /***
     * 添加机构身份信息
     * @param name
     * @param code
     * @param type
     * @param contractAddress
     * @param pubKey
     * @param address
     * @param description
     * @return
     */
    public String creditIdentityAdd(String name, String code, String type, String contractAddress, String pubKey, String address, String description) {

        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("code", code);
        map.put("type", type);
        map.put("contractAddress", contractAddress);
        map.put("pubKey", pubKey);
        map.put("address", address);
        map.put("description", description);

        String result = PostTest.postMethod(SDKADD + "/credit/identity/add", map);
        log.info(result);
        return result;
    }

    /***
     * 查询身份信息
     * @param code   公司code
     * @return
     */
    public String creditIdentityQuery(String code) {
        Map<String, Object> map = new HashMap<>();
        map.put("code", code);

        String result = PostTest.postMethod(SDKADD + "/credit/identity/query", map);
        log.info(result);
        return result;
    }

    /***
     * 查询所有身份信息
     * @return
     */
    public String creditIdentityQueryAll() {
        Map<String, Object> map = new HashMap<>();

        String result = PostTest.postMethod(SDKADD + "/credit/identity/queryall", map);
        log.info(result);
        return result;
    }

    /***
     * 添加征信数据
     * @param creditDataList   征信数据
     * @return
     */
    public String creditCreditdataAdd(List<Map> creditDataList, String name) {
        Map<String, Object> map = new HashMap<>();
        map.put("creditDataList", creditDataList);

        String result = PostTest.postMethod(SDKADD + "/credit/creditdata/add" + "?name=" + name, map);
        log.info(result);
        return result;
    }

    /***
     * 查询征信数据
     * @param EnterpriseCode   授权公司code
     * @return
     */
    public String creditCreditdataQuery(String EnterpriseCode) {
        Map<String, Object> map = new HashMap<>();
        map.put("EnterpriseCode", EnterpriseCode);

        String result = PostTest.postMethod(SDKADD + "/credit/creditdata/query", map);
        log.info(result);
        return result;
    }

    /***
     * 添加授权记录
     * @param orgID   机构ID
     * @param authorizationList  投资者信息
     * @return
     */
    public String creditAuthorizationAdd(ArrayList<String> orgID, List<Map> authorizationList) {
        Map<String, Object> map = new HashMap<>();
        map.put("orgID", orgID);
        map.put("authorizationList", authorizationList);

        String result = PostTest.postMethod(SDKADD + "/credit/authorization/add", map);
        log.info(result);
        return result;
    }

    /***
     * 查询授权记录
     * @param key   唯一标识符
     * @return
     */
    public String creditAuthorizationQuery(String key) {
        Map<String, Object> map = new HashMap<>();
        map.put("key", key);

        String result = PostTest.postMethod(SDKADD + "/credit/authorization/query", map);
        log.info(result);
        return result;
    }

    /***
     * 添加查询记录
     * @param orgID   机构ID
     * @param viewHistoryList  投资者信息
     * @return
     */
    public String creditViewhistoryAdd(ArrayList<String> orgID, List<Map> viewHistoryList) {
        Map<String, Object> map = new HashMap<>();
        map.put("orgID", orgID);
        map.put("viewHistoryList", viewHistoryList);

        String result = PostTest.postMethod(SDKADD + "/credit/viewhistory/add", map);
        log.info(result);
        return result;
    }

    /***
     * 查看查询记录
     * @param key   唯一标识符
     * @return
     */
    public String creditViewhistoryQuery(String key) {
        Map<String, Object> map = new HashMap<>();
        map.put("key", key);

        String result = PostTest.postMethod(SDKADD + "/credit/viewhistory/query", map);
        log.info(result);
        return result;
    }

}
