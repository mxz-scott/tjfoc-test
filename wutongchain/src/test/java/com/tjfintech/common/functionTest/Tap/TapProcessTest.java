package com.tjfintech.common.functionTest.Tap;

import com.alibaba.fastjson.JSONArray;
import com.tjfintech.common.*;
import com.tjfintech.common.Interface.Credit;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.Interface.Tap;
import com.tjfintech.common.utils.FileOperation;
import com.tjfintech.common.utils.UtilsClass;
import com.tjfintech.common.utils.UtilsClassCredit;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.checkerframework.checker.units.qual.C;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassTap.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class TapProcessTest {

    TestBuilder testBuilder = TestBuilder.getInstance();
    Tap tap = testBuilder.getTap();
    CertTool certTool = new CertTool();
    Store store = testBuilder.getStore();
    CommonFunc commonFunc = new CommonFunc();
    UtilsClass utilsClass = new UtilsClass();
    TapCommonFunc tapCommonFunc = new TapCommonFunc();

    String sdkIP = SDKADD.substring(SDKADD.lastIndexOf("/") + 1, SDKADD.lastIndexOf(":"));

    @BeforeClass
    public static void init() throws Exception {
        BeforeCondition bf = new BeforeCondition();
        bf.updatePubPriKey();

        TapCommonFunc tapCommonFunc = new TapCommonFunc();
        tapCommonFunc.init();
    }

    /**
     * 正常流程测试
     * 招标初始化-查询-更新-查询
     * 投标文件校验-投标文件上传-撤销投标
     * 获取招标信息列表-获取投标信息列表-开标
     */
    @Test
    public void tapProcessTest() throws Exception {

        //招标信息初始化
        BID_DOC_REFER_END_TIME = constructTime(20000);
        KAIBIAODATE = constructTime(20000);
        String response = tap.tapProjectInit(TENDER_PROJECT_CODE, TENDER_PROJECT_NAME, BID_SECTION_NAME, BID_SECTION_CODE, KAIBIAODATE,
                BID_DOC_REFER_END_TIME, "1", TBFILE_ALLOWLIST, TBALLOWFILESIZE, "1.0",
                "1.0", ZBRPULICKEY, BID_SECTION_CODE_EX, EXTRA);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        String txid = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        String orderNo = JSONObject.fromObject(response).getJSONObject("data").getString("ORDERNO");
        commonFunc.verifyTxDetailField(txid, "", "2", "3", "42");

        //招标信息查询比对
        response = tap.tapProjectDetail(orderNo);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(TENDER_PROJECT_CODE, JSONObject.fromObject(response).getJSONObject("data").getString("TENDER_PROJECT_CODE"));
        assertEquals(TENDER_PROJECT_NAME, JSONObject.fromObject(response).getJSONObject("data").getString("TENDER_PROJECT_NAME"));
        assertEquals(BID_SECTION_NAME, JSONObject.fromObject(response).getJSONObject("data").getString("BID_SECTION_NAME"));
        assertEquals(BID_SECTION_CODE, JSONObject.fromObject(response).getJSONObject("data").getString("BID_SECTION_CODE"));
        assertEquals(BID_DOC_REFER_END_TIME, JSONObject.fromObject(response).getJSONObject("data").getString("BID_DOC_REFER_END_TIME"));
        assertEquals(KAIBIAODATE, JSONObject.fromObject(response).getJSONObject("data").getString("KAIBIAODATE"));
        assertEquals("1", JSONObject.fromObject(response).getJSONObject("data").getString("BID_SECTION_STATUS"));
        assertEquals(TBFILE_ALLOWLIST, JSONObject.fromObject(response).getJSONObject("data").getString("TBFILE_ALLOWLIST"));
        assertEquals(TBALLOWFILESIZE, JSONObject.fromObject(response).getJSONObject("data").getInt("TBALLOWFILESIZE"));
        assertEquals("1.0", JSONObject.fromObject(response).getJSONObject("data").getString("TBTOOL_ALLOWVERSION"));
        assertEquals("1.0", JSONObject.fromObject(response).getJSONObject("data").getString("TBFILEVERSION"));
        assertEquals(BID_SECTION_CODE_EX, JSONObject.fromObject(response).getJSONObject("data").getString("BID_SECTION_CODE_EX"));
        assertEquals(true, JSONObject.fromObject(response).getJSONObject("data").getString("EXTRA").contains("old"));
        assertEquals(true, JSONObject.fromObject(response).getJSONObject("data").toString().contains("INIT_DATE"));

        //招标信息更新，比对更新后的信息
        ORDERNOSIGN = certTool.tapSign(sdkIP, "ORDERNO", "", orderNo, "");
        orderNoSIGN = certTool.tapSign(sdkIP, "orderNo", "", orderNo, "");
        BID_DOC_REFER_END_TIME = constructTime(15000);
        KAIBIAODATE = constructTime(15000);
        response = tap.tapProjectUpdate(orderNo, TENDER_PROJECT_CODE + "NEW", TENDER_PROJECT_NAME + "NEW",
                BID_SECTION_NAME + "NEW", BID_SECTION_CODE + "NEW", KAIBIAODATE, BID_DOC_REFER_END_TIME,
                "0", TBFILE_ALLOWLIST + "NEW", TBALLOWFILESIZENew, "2.0",
                "2.0", BID_SECTION_CODE_EX + "NEW", EXTRANew, ORDERNOSIGN);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        txid = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        commonFunc.verifyTxDetailField(txid, "", "2", "3", "42");
        response = tap.tapProjectDetail(orderNo);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(TENDER_PROJECT_CODE + "NEW", JSONObject.fromObject(response).getJSONObject("data").getString("TENDER_PROJECT_CODE"));
        assertEquals(TENDER_PROJECT_NAME + "NEW", JSONObject.fromObject(response).getJSONObject("data").getString("TENDER_PROJECT_NAME"));
        assertEquals(BID_SECTION_NAME + "NEW", JSONObject.fromObject(response).getJSONObject("data").getString("BID_SECTION_NAME"));
        assertEquals(BID_SECTION_CODE + "NEW", JSONObject.fromObject(response).getJSONObject("data").getString("BID_SECTION_CODE"));
        assertEquals(BID_DOC_REFER_END_TIME, JSONObject.fromObject(response).getJSONObject("data").getString("BID_DOC_REFER_END_TIME"));
        assertEquals(KAIBIAODATE, JSONObject.fromObject(response).getJSONObject("data").getString("KAIBIAODATE"));
        assertEquals("0", JSONObject.fromObject(response).getJSONObject("data").getString("BID_SECTION_STATUS"));
        assertEquals(TBFILE_ALLOWLIST + "NEW", JSONObject.fromObject(response).getJSONObject("data").getString("TBFILE_ALLOWLIST"));
        assertEquals(TBALLOWFILESIZENew, JSONObject.fromObject(response).getJSONObject("data").getInt("TBALLOWFILESIZE"));
        assertEquals("2.0", JSONObject.fromObject(response).getJSONObject("data").getString("TBTOOL_ALLOWVERSION"));
        assertEquals("2.0", JSONObject.fromObject(response).getJSONObject("data").getString("TBFILEVERSION"));
        assertEquals(BID_SECTION_CODE_EX + "NEW", JSONObject.fromObject(response).getJSONObject("data").getString("BID_SECTION_CODE_EX"));
        assertEquals(true, JSONObject.fromObject(response).getJSONObject("data").getString("EXTRA").contains("update"));

        //该项目更新为正常状态
        response = tap.tapProjectUpdate(orderNo, "", "",
                "", "", "", "",
                "1", "", 0, "",
                "", "", null, ORDERNOSIGN);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));

        //投标文件合规性校验
        response = tap.tapTenderVerify("123", senderBidPlatform);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        response = tap.tapTenderVerify("123", senderFilePlatform);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, JSONObject.fromObject(response).getJSONObject("data").getBoolean("pass"));

        response = tap.tapTenderVerify("123--", senderFilePlatform);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(false, JSONObject.fromObject(response).getJSONObject("data").getBoolean("pass"));

        //投标文件上传
        response = tap.tapTenderUpload(orderNo, recordIdA, fileHead, path);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        response = tap.tapTenderUpload(orderNo, recordIdB, fileHead, path);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        //撤销投标接口
        response = tap.tapTenderRevoke(EXTRA.toString(), orderNo);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        //获取招标信息列表
        response = tap.tapProjectList();
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains(orderNo));

        //获取投标信息列表,获取投标列表接口请求时间大于开标时间
        assertThat((Long.parseLong(constructTime(0))), lessThan(Long.parseLong(KAIBIAODATE)));
        response = tap.tapTenderRecord(orderNo, recordIdA, true, orderNoSIGN);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("KAIBIAODATE is later"));
        sleepAndSaveInfo(20 * 1000);
        response = tap.tapTenderRecord(orderNo, recordIdA, true, orderNoSIGN);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(recordIdA, com.alibaba.fastjson.JSONObject.parseObject(
                com.alibaba.fastjson.JSONObject.parseArray(com.alibaba.fastjson.JSONObject.parseObject(response).getJSONObject(
                        "data").getString("RecordInfos")).get(0).toString()).getString("recordId"));
        assertEquals("1", com.alibaba.fastjson.JSONObject.parseObject(
                com.alibaba.fastjson.JSONObject.parseArray(com.alibaba.fastjson.JSONObject.parseObject(response).getJSONObject(
                        "data").getString("RecordInfos")).get(0).toString()).getInteger("version").toString());

        //开标,再次调更新接口，更新状态为5
        response = tap.tapTenderOpen(orderNo, orderNoSIGN);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        sleepAndSaveInfo(5 * 1000);//在招标平台获取到token的时候，才会变更招标状态
        response = tap.tapProjectDetail(orderNo);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals("1", JSONObject.fromObject(response).getJSONObject("data").getString("BID_SECTION_STATUS"));

        response = tap.tapProjectUpdate(orderNo, "", "",
                "", "", "", "",
                "5", "", 0, "",
                "", "", null, ORDERNOSIGN);

        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        response = tap.tapProjectDetail(orderNo);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals("5", JSONObject.fromObject(response).getJSONObject("data").getString("BID_SECTION_STATUS"));
    }

    /**
     * 异常流程测试-投标文件上传接口
     * 项目标识为0-异常、5-开标的项目，不可以再上传投标文件
     */
    @Test
    public void tapTenderUploadInvalidTest() throws Exception {

        String orderNo = tapCommonFunc.initProject();

        //项目标识orderNo为0-异常的项目
        String response = tap.tapProjectUpdate(orderNo, "", "",
                "", "", "", "",
                "0", "", 0, "",
                "", "", null, ORDERNOSIGN);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        response = tap.tapTenderUpload(orderNo, recordIdA, fileHead, path);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("BID_SECTION_STATUS is abnormal"));

        //项目标识orderNo为5-开标的项目
        response = tap.tapProjectUpdate(orderNo, "", "",
                "", "", "", "",
                "5", "", 0, "",
                "", "", null, ORDERNOSIGN);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        response = tap.tapTenderUpload(orderNo, recordIdA, fileHead, path);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("BID_SECTION_STATUS is abnormal"));
    }

    /**
     * 异常流程测试-招标信息更新接口
     * 状态不可以更新为除0和1其他值
     * 项目标识5-已开标的项目，不可以再更新招标信息
     */
    @Test
    public void tapProjectUpdateInvalidTest() throws Exception {

        String orderNo = tapCommonFunc.initProject();

        //更新状态为3，接口返回错误
        String response = tap.tapProjectUpdate(orderNo, "", "",
                "", "", "", "",
                "3", "", 0, "",
                "", "", null, ORDERNOSIGN);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("Illegal BID_SECTION_STATUS"));

        //更新状态为5-开标已结束的项目，再更新项目信息报错
        response = tap.tapProjectUpdate(orderNo, "", "",
                "", "", "", "",
                "5", "", 0, "",
                "", "", null, ORDERNOSIGN);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));

        response = tap.tapProjectUpdate(orderNo, TENDER_PROJECT_CODE + "NEW", TENDER_PROJECT_NAME + "NEW",
                BID_SECTION_NAME + "NEW", BID_SECTION_CODE + "NEW", KAIBIAODATE, BID_DOC_REFER_END_TIME,
                "0", TBFILE_ALLOWLIST + "NEW", TBALLOWFILESIZENew, "2.0",
                "2.0", BID_SECTION_CODE_EX + "NEW", EXTRANew, ORDERNOSIGN);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("can not modify finished bid"));
    }

    /**
     * 正常流程测试-获取招标信息接口
     * 只返回状态为1-正常的招标项目信息
     * 不返回0-异常；5-开标的招标项目信息
     */
    @Test
    public void tapProjectListTest() throws Exception {

        String orderNo0 = tapCommonFunc.initProject();
        String orderNo1 = tapCommonFunc.initProject();
        String orderNo5 = tapCommonFunc.initProject();

        //变更项目状态orderNoSIGN0（0-异常）、orderNoSIGN1（1-正常）、orderNoSIGN5（5-已开标）
        String ORDERNOSIGN0 = certTool.tapSign(sdkIP, "ORDERNO", "", orderNo0, "");
        String ORDERNOSIGN1 = certTool.tapSign(sdkIP, "ORDERNO", "", orderNo1, "");
        String ORDERNOSIGN5 = certTool.tapSign(sdkIP, "ORDERNO", "", orderNo5, "");

        String response = tap.tapProjectUpdate(orderNo0, "", "",
                "", "", "", "",
                "0", "", 0, "",
                "", "", null, ORDERNOSIGN0);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));

        response = tap.tapProjectUpdate(orderNo5, "", "",
                "", "", "", "",
                "5", "", 0, "",
                "", "", null, ORDERNOSIGN5);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));

        response = tap.tapProjectList();
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains(orderNo1));
        assertEquals(false, response.contains(orderNo0));
        assertEquals(false, response.contains(orderNo5));

    }

    /**
     * 正常流程测试-获取投标信息列表接口
     * 同一个投标标识可以多次上传标书，查询接口正常
     * detail不传或者传值false返回指定信息，传值true返回详细信息
     * recordId传值返回指定投标信息，不传返回所有投标信息
     */
    @Test
    public void tapTenderRecordTest() throws Exception {

        String orderNo = tapCommonFunc.initProject();
        String orderNo1 = tapCommonFunc.initProject();
        orderNoSIGN = certTool.tapSign(sdkIP, "orderNo", "", orderNo, "");
        String orderNoSIGN1 = certTool.tapSign(sdkIP, "orderNo", "", orderNo1, "");

        //投标文件上传
        String response = tap.tapTenderUpload(orderNo, recordIdA, fileHead, path);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        response = tap.tapTenderUpload(orderNo, recordIdB, fileHead, path);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));

        response = tap.tapTenderUpload(orderNo1, recordIdB, fileHead, path);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        //使用orderNo1查询
        sleepAndSaveInfo(20 * 1000);
        response = tap.tapTenderRecord(orderNo1, recordIdB, true, orderNoSIGN1);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains(recordIdB));
        assertThat(response, allOf(containsString("filePath"), containsString("keySecret")));

        response = tap.tapTenderRecord(orderNo1, "", false, orderNoSIGN1);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains(recordIdB));
        assertThat(response, allOf(not(containsString("filePath")), not(containsString("keySecret"))));

        //recordId数据不存在
        response = tap.tapTenderRecord(orderNo, "123456", true, orderNoSIGN);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(false, response.contains("123456"));

        //recordId传值存在的数据recordIdA、detail传值true
        response = tap.tapTenderRecord(orderNo, recordIdA, true, orderNoSIGN);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains(recordIdA));
        assertEquals(false, response.contains(recordIdB));
        assertThat(response, allOf(containsString("filePath"), containsString("keySecret")));

        //recordId传值存在的数据recordIdB、detail传值false
        response = tap.tapTenderRecord(orderNo, recordIdB, false, orderNoSIGN);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(false, response.contains(recordIdA));
        assertEquals(true, response.contains(recordIdB));
        assertThat(response, allOf(not(containsString("filePath")), not(containsString("keySecret"))));

        //recordId传值存在的数据recordIdB、detail传值null
        response = tap.tapTenderRecord(orderNo, recordIdB, null, orderNoSIGN);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(false, response.contains(recordIdA));
        assertEquals(true, response.contains(recordIdB));
        assertThat(response, allOf(not(containsString("filePath")), not(containsString("keySecret"))));

        //recordI为空、detail传值true
        response = tap.tapTenderRecord(orderNo, "", true, orderNoSIGN);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains(recordIdA));
        assertEquals(true, response.contains(recordIdB));
        assertThat(response, allOf(containsString("filePath"), containsString("keySecret")));

        //recordI为空、detail传值false
        response = tap.tapTenderRecord(orderNo, "", false, orderNoSIGN);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains(recordIdA));
        assertEquals(true, response.contains(recordIdB));
        assertThat(response, allOf(not(containsString("filePath")), not(containsString("keySecret"))));

        //recordI为空、detail为空
        response = tap.tapTenderRecord(orderNo, "", null, orderNoSIGN);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains(recordIdA));
        assertEquals(true, response.contains(recordIdB));
        assertThat(response, allOf(not(containsString("filePath")), not(containsString("keySecret"))));

    }

    /**
     * 正常流程测试-获取投标信息列表接口
     * 同一个recordId和orderNo再次上传投标文件，获取列表的版本version+1
     * 解密filePath数据,数据为最后更新的path数据
     */
    @Test
    public void tapDecryptPathTest() throws Exception {

        String orderNo = tapCommonFunc.initProject();
        //投标文件上传
        String response = tap.tapTenderUpload(orderNo, recordIdA, fileHead, path);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

         response = tap.tapTenderUpload(orderNo, recordIdA, fileHead, "top/sub11/sub22/sub33");
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        response = tap.tapTenderUpload(orderNo, recordIdA, fileHead, "top/sub111/sub222/sub333");
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        //使用orderNo查询
        sleepAndSaveInfo(20 * 1000);
        response = tap.tapTenderRecord(orderNo, recordIdA, true, orderNoSIGN);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));

        String RecordInfos = com.alibaba.fastjson.JSONObject.parseObject(response).getJSONObject("data").getString("RecordInfos");
        Object RecordInfo = com.alibaba.fastjson.JSONObject.parseArray(RecordInfos).get(0);
        String filePath = JSONObject.fromObject(RecordInfo).getString("filePath");
        String keySecret = JSONObject.fromObject(RecordInfo).getString("keySecret");
        assertEquals(recordIdA, JSONObject.fromObject(RecordInfo).getString("recordId"));
        assertEquals(3, JSONObject.fromObject(RecordInfo).getInt("version"));

        String decryptFilePath = certTool.tapDecryptFilePath(sdkIP, "", "", filePath, keySecret);
        assertEquals(false, decryptFilePath.contains(path));
        assertEquals(true, decryptFilePath.contains("top/sub111/sub222/sub333"));

    }

    /**
     * 异常流程测试-开标接口
     * 状态为5和0的项目不可以调开标接口
     */
    @Test
    public void tapTenderOpenInvalidTest() throws Exception {

        String orderNo = tapCommonFunc.initProject();

        //项目标识projectId为5-开标的项目
        String response = tap.tapProjectUpdate(orderNo, "", "",
                "", "", "", "",
                "0", "", 0, "",
                "", "", null, ORDERNOSIGN);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        sleepAndSaveInfo(20 * 1000);
        response = tap.tapTenderOpen(orderNo, orderNoSIGN);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("BID_SECTION_STATUS is abnormal"));

        response = tap.tapProjectUpdate(orderNo, "", "",
                "", "", "", "",
                "5", "", 0, "",
                "", "", null, ORDERNOSIGN);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        response = tap.tapTenderOpen(orderNo, orderNoSIGN);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("BID_SECTION_STATUS is abnormal"));
    }


}