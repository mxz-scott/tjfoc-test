package com.tjfintech.common;

import com.tjfintech.common.Interface.Tap;
import com.tjfintech.common.utils.GetTest;
import com.tjfintech.common.utils.PostTest;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.SDKADD;

@Slf4j
public class GoTap implements Tap {

    /***
     * 招标信息初始化
     */
    public String tapProjectInit(String TENDER_PROJECT_CODE, String TENDER_PROJECT_NAME, String BID_SECTION_NAME, String BID_SECTION_CODE, String KAIBIAODATE,
                                 String BID_DOC_REFER_END_TIME, String BID_SECTION_STATUS, String TBFILE_ALLOWLIST, int TBALLOWFILESIZE,
                                 String TBTOOL_ALLOWVERSION, String TBFILEVERSION, String ZBRPULICKEY, String BID_SECTION_CODE_EX, Map EXTRA) {

        Map<String, Object> map = new HashMap<>();
        map.put("TENDER_PROJECT_CODE", TENDER_PROJECT_CODE);
        map.put("TENDER_PROJECT_NAME", TENDER_PROJECT_NAME);
        map.put("BID_SECTION_NAME", BID_SECTION_NAME);
        map.put("BID_SECTION_CODE", BID_SECTION_CODE);
        map.put("KAIBIAODATE", KAIBIAODATE);
        map.put("BID_DOC_REFER_END_TIME", BID_DOC_REFER_END_TIME);
        map.put("BID_SECTION_STATUS", BID_SECTION_STATUS);
        map.put("TBFILE_ALLOWLIST", TBFILE_ALLOWLIST);
        map.put("TBALLOWFILESIZE", TBALLOWFILESIZE);
        map.put("TBTOOL_ALLOWVERSION", TBTOOL_ALLOWVERSION);
        map.put("TBFILEVERSION", TBFILEVERSION);
        map.put("ZBRPULICKEY", ZBRPULICKEY);
        map.put("BID_SECTION_CODE_EX", BID_SECTION_CODE_EX);
        map.put("EXTRA", EXTRA);

        String result = PostTest.postMethod(SDKADD + "/tap/v1/project/init", map);
        log.info(result);
        return result;
    }

    /***
     * 招标信息更新
     */
    public String tapProjectUpdate(String ORDERNO, String TENDER_PROJECT_CODE, String TENDER_PROJECT_NAME, String BID_SECTION_NAME, String BID_SECTION_CODE,
                                   String KAIBIAODATE, String BID_DOC_REFER_END_TIME, String BID_SECTION_STATUS, String TBFILE_ALLOWLIST, int TBALLOWFILESIZE,
                                   String TBTOOL_ALLOWVERSION, String TBFILEVERSION, String BID_SECTION_CODE_EX, Map EXTRA, String SIGN) {

        Map<String, Object> map = new HashMap<>();
        map.put("ORDERNO", ORDERNO);
        map.put("TENDER_PROJECT_CODE", TENDER_PROJECT_CODE);
        map.put("TENDER_PROJECT_NAME", TENDER_PROJECT_NAME);
        map.put("BID_SECTION_NAME", BID_SECTION_NAME);
        map.put("BID_SECTION_CODE", BID_SECTION_CODE);
        map.put("KAIBIAODATE", KAIBIAODATE);
        map.put("BID_DOC_REFER_END_TIME", BID_DOC_REFER_END_TIME);
        map.put("BID_SECTION_STATUS", BID_SECTION_STATUS);
        map.put("TBFILE_ALLOWLIST", TBFILE_ALLOWLIST);
        map.put("TBALLOWFILESIZE", TBALLOWFILESIZE);
        map.put("TBTOOL_ALLOWVERSION", TBTOOL_ALLOWVERSION);
        map.put("TBFILEVERSION", TBFILEVERSION);
        map.put("BID_SECTION_CODE_EX", BID_SECTION_CODE_EX);
        map.put("EXTRA", EXTRA);
        map.put("SIGN", SIGN);

        String result = PostTest.postMethod(SDKADD + "/tap/v1/project/update", map);
        log.info(result);
        return result;
    }

    /***
     * 招标信息查询
     * @return
     */
    public String tapProjectDetail(String ORDERNO) {

        String result = GetTest.doGet2(SDKADD + "/tap/v1/project/detail/" + ORDERNO);
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
    public String tapTenderUpload(String orderNo, String recordId, String fileHead, String path) {

        Map<String, Object> map = new HashMap<>();
        map.put("orderNo", orderNo);
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
    public String tapTenderRevoke(String data, String orderNo) {
        Map<String, Object> map = new HashMap<>();
        map.put("data", data);
        map.put("orderNo", orderNo);

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
    public String tapTenderRecord(String orderNo, String recordId, Boolean detail, String sign) {

        Map<String, Object> map = new HashMap<>();
        map.put("orderNo", orderNo);
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
    public String tapTenderOpen(String orderNo, String sign) {

        String result = GetTest.doGet2(SDKADD + "/tap/v1/tender/open" + "?orderNo=" + orderNo + "&sign=" + sign);
        log.info(result);
        return result;
    }

}
