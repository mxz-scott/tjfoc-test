package com.tjfintech.common;

import com.tjfintech.common.Interface.Credit;
import com.tjfintech.common.Interface.Tap;
import com.tjfintech.common.utils.GetTest;
import com.tjfintech.common.utils.PostTest;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.SDKADD;

@Slf4j
public class GoTap implements Tap {

    /***
     * 招标信息初始化
     */
    public String tapProjectInit(long expireDate, long openDate, String publicKey, String identity, int filesize, String name, Map metaData) {

        Map<String, Object> map = new HashMap<>();
        map.put("expireDate", expireDate);
        map.put("openDate", openDate);
        map.put("publicKey", publicKey);
        map.put("identity", identity);
        map.put("filesize", filesize);
        map.put("name", name);
        map.put("metaData", metaData);

        String result = PostTest.postMethod(SDKADD + "/tap/v1/project/init", map);
        log.info(result);
        return result;
    }

    /***
     * 招标信息更新
     */
    public String tapProjectUpdate(String projectId, long expireDate, long openDate, Map metaData, String name, int state, int filesize, String sign) {

        Map<String, Object> map = new HashMap<>();
        map.put("projectId", projectId);
        map.put("expireDate", expireDate);
        map.put("openDate", openDate);
        map.put("metaData", metaData);
        map.put("name", name);
        map.put("state", state);
        map.put("filesize", filesize);
        map.put("sign", sign);

        String result = PostTest.postMethod(SDKADD + "/tap/v1/project/update", map);
        log.info(result);
        return result;
    }

    /***
     * 招标信息查询
     * @return
     */
    public String tapProjectDetail(String projectId) {

        String result = GetTest.doGet2(SDKADD + "/tap/v1/project/detail/" + projectId);
        log.info(result);
        return result;
    }

    /***
     * 投标文件合规性校验接口
     * @return
     */
    public String tapTenderVerify(String hashvalue, String sender) {
        Map<String, Object> map = new HashMap<>();
        map.put("hashvalue", hashvalue);
        map.put("sender", sender);

        String result = PostTest.postMethod(SDKADD + "/tap/v1/tender/verify", map);
        log.info(result);
        return result;
    }

    /***
     * 投标文件上传
     * @return
     */
    public String tapTenderUpload(String projectId, String recordId, String fileHead, String path) {

        Map<String, Object> map = new HashMap<>();
        map.put("projectId", projectId);
        map.put("recordId", recordId);
        map.put("fileHead", fileHead);
        map.put("path", path);

        String result = PostTest.postMethod(SDKADD + "/tap/v1/tender/upload", map);
        log.info(result);
        return result;
    }

    /***
     * 撤销投标接口
     * @return
     */
    public String tapTenderRevoke(String data, String projectId) {
        Map<String, Object> map = new HashMap<>();
        map.put("data", data);
        map.put("projectId", projectId);

        String result = PostTest.postMethod(SDKADD + "/tap/v1/tender/revoke", map);
        log.info(result);
        return result;
    }

    /***
     * 获取招标信息列表
     * @return
     */
    public String tapProjectList() {

        String result = GetTest.doGet2(SDKADD + "/tap/v1/project/list");
        log.info(result);
        return result;
    }

    /***
     * 获取投标信息列表
     * @return
     */
    public String tapTenderRecord(String projectId, String recordId, boolean detail, String sign) {

        Map<String, Object> map = new HashMap<>();
        map.put("projectId", projectId);
        map.put("recordId", recordId);
        map.put("detail", detail);
        map.put("sign", sign);

        String result = PostTest.postMethod(SDKADD + "/tap/v1/tender/record", map);
        log.info(result);
        return result;
    }

    /***
     * 开标
     * @return
     */
    public String tapTenderOpen(String projectId, String sign) {

        String result = GetTest.doGet2(SDKADD + "/tap/v1/tender/open" + "?projectId=" + projectId + "&sign=" + sign);
        log.info(result);
        return result;
    }

}
