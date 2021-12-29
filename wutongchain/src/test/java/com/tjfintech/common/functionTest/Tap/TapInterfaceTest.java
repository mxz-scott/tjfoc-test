package com.tjfintech.common.functionTest.Tap;

import com.tjfintech.common.*;
import com.tjfintech.common.Interface.Credit;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.Interface.Tap;
import com.tjfintech.common.functionTest.contract.WVMContractTest;
import com.tjfintech.common.utils.UtilsClass;
import com.tjfintech.common.utils.UtilsClassCredit;
import com.tjfintech.common.utils.UtilsClassTap;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassTap.*;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class TapInterfaceTest {

    TestBuilder testBuilder = TestBuilder.getInstance();
    Tap tap = testBuilder.getTap();
    CertTool certTool = new CertTool();
    CommonFunc commonFunc = new CommonFunc();
    UtilsClass utilsClass = new UtilsClass();
    UtilsClassTap utilsClassTap = new UtilsClassTap();
    TapCommonFunc tapCommonFunc = new TapCommonFunc();
    WVMContractTest wvm = new WVMContractTest();


    @BeforeClass
    public static void init() throws Exception {

        BeforeCondition bf = new BeforeCondition();
        bf.updatePubPriKey();
        TapCommonFunc tapCommonFunc = new TapCommonFunc();
        tapCommonFunc.init();

    }

    @Test
    //招标信息初始化接口必输字段校验
    public void tapProjectInitInterfaceTest() throws Exception {

        //项目编号TENDER_PROJECT_CODE为空
        String response = tap.tapProjectInit("", TENDER_PROJECT_NAME, BID_SECTION_NAME, BID_SECTION_CODE, KAIBIAODATE,
                BID_DOC_REFER_END_TIME, "1", "jstf", TBALLOWFILESIZE, "1.0",
                "1.0", ZBRPULICKEY, BID_SECTION_CODE_EX, EXTRA);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("Field validation for 'TENDER_PROJECT_CODE' failed on the 'required"));

        //项目名称TENDER_PROJECT_NAME为空
        response = tap.tapProjectInit(TENDER_PROJECT_CODE, "", BID_SECTION_NAME, BID_SECTION_CODE, KAIBIAODATE,
                BID_DOC_REFER_END_TIME, "1", "jstf", TBALLOWFILESIZE, "1.0",
                "1.0", ZBRPULICKEY, BID_SECTION_CODE_EX, EXTRA);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("Field validation for 'TENDER_PROJECT_NAME' failed on the 'required"));

        //标段名称BID_SECTION_NAME为空
        response = tap.tapProjectInit(TENDER_PROJECT_CODE, TENDER_PROJECT_NAME, "", BID_SECTION_CODE, KAIBIAODATE,
                BID_DOC_REFER_END_TIME, "1", "jstf", TBALLOWFILESIZE, "1.0",
                "1.0", ZBRPULICKEY, BID_SECTION_CODE_EX, EXTRA);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("Field validation for 'BID_SECTION_NAME' failed on the 'required"));

        //标段编号BID_SECTION_CODE为空
        response = tap.tapProjectInit(TENDER_PROJECT_CODE, TENDER_PROJECT_NAME, BID_SECTION_NAME, "", KAIBIAODATE,
                BID_DOC_REFER_END_TIME, "1", "jstf", TBALLOWFILESIZE, "1.0",
                "1.0", ZBRPULICKEY, BID_SECTION_CODE_EX, EXTRA);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("Field validation for 'BID_SECTION_CODE' failed on the 'required"));

        //开标时间KAIBIAODATE为空
        response = tap.tapProjectInit(TENDER_PROJECT_CODE, TENDER_PROJECT_NAME, BID_SECTION_NAME, BID_SECTION_CODE, "",
                BID_DOC_REFER_END_TIME, "1", "jstf", TBALLOWFILESIZE, "1.0",
                "1.0", ZBRPULICKEY, BID_SECTION_CODE_EX, EXTRA);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("Field validation for 'KAIBIAODATE' failed on the 'required"));

        //文件递交截止时间BID_DOC_REFER_END_TIME为空
        response = tap.tapProjectInit(TENDER_PROJECT_CODE, TENDER_PROJECT_NAME, BID_SECTION_NAME, BID_SECTION_CODE, KAIBIAODATE,
                "", "1", "jstf", TBALLOWFILESIZE, "1.0",
                "1.0", ZBRPULICKEY, BID_SECTION_CODE_EX, EXTRA);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("Field validation for 'BID_DOC_REFER_END_TIME' failed on the 'required"));

        //标段状态BID_SECTION_STATUS为空
        response = tap.tapProjectInit(TENDER_PROJECT_CODE, TENDER_PROJECT_NAME, BID_SECTION_NAME, BID_SECTION_CODE, KAIBIAODATE,
                BID_DOC_REFER_END_TIME, "", "jstf", TBALLOWFILESIZE, "1.0",
                "1.0", ZBRPULICKEY, BID_SECTION_CODE_EX, EXTRA);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("Field validation for 'BID_SECTION_STATUS' failed on the 'oneof' tag"));

        //文件后缀名TBFILE_ALLOWLIST为空
        response = tap.tapProjectInit(TENDER_PROJECT_CODE, TENDER_PROJECT_NAME, BID_SECTION_NAME, BID_SECTION_CODE, KAIBIAODATE,
                BID_DOC_REFER_END_TIME, "1", "", TBALLOWFILESIZE, "1.0",
                "1.0", ZBRPULICKEY, BID_SECTION_CODE_EX, EXTRA);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("Field validation for 'TBFILE_ALLOWLIST' failed on the 'required"));

        //上传文件大小TBALLOWFILESIZE为空
        response = tap.tapProjectInit(TENDER_PROJECT_CODE, TENDER_PROJECT_NAME, BID_SECTION_NAME, BID_SECTION_CODE, KAIBIAODATE,
                BID_DOC_REFER_END_TIME, "1", "jstf", 0, "1.0",
                "1.0", ZBRPULICKEY, BID_SECTION_CODE_EX, EXTRA);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("Field validation for 'TBALLOWFILESIZE' failed on the 'required"));

        //文件制作工具版本TBTOOL_ALLOWVERSION为空
        response = tap.tapProjectInit(TENDER_PROJECT_CODE, TENDER_PROJECT_NAME, BID_SECTION_NAME, BID_SECTION_CODE, KAIBIAODATE,
                BID_DOC_REFER_END_TIME, "1", "jstf", TBALLOWFILESIZE, "",
                "1.0", ZBRPULICKEY, BID_SECTION_CODE_EX, EXTRA);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("Field validation for 'TBTOOL_ALLOWVERSION' failed on the 'required"));

        //投标文件版本TBFILEVERSION为空
        response = tap.tapProjectInit(TENDER_PROJECT_CODE, TENDER_PROJECT_NAME, BID_SECTION_NAME, BID_SECTION_CODE, KAIBIAODATE,
                BID_DOC_REFER_END_TIME, "1", "jstf", TBALLOWFILESIZE, "1.0",
                "", ZBRPULICKEY, BID_SECTION_CODE_EX, EXTRA);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("Field validation for 'TBFILEVERSION' failed on the 'required"));

        //招标方公钥ZBRPULICKEY为空
        response = tap.tapProjectInit(TENDER_PROJECT_CODE, TENDER_PROJECT_NAME, BID_SECTION_NAME, BID_SECTION_CODE, KAIBIAODATE,
                BID_DOC_REFER_END_TIME, "1", "jstf", TBALLOWFILESIZE, "1.0",
                "1.0", "", BID_SECTION_CODE_EX, EXTRA);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("Field validation for 'ZBRPULICKEY' failed on the 'required"));

        //标段编号拓展字段BID_SECTION_CODE_EX为空
        response = tap.tapProjectInit(TENDER_PROJECT_CODE, TENDER_PROJECT_NAME, BID_SECTION_NAME, BID_SECTION_CODE, KAIBIAODATE,
                BID_DOC_REFER_END_TIME, "1", "jstf", TBALLOWFILESIZE, "1.0",
                "1.0", ZBRPULICKEY, "", EXTRA);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("Field validation for 'BID_SECTION_CODE_EX' failed on the 'required"));

        //开标时间KAIBIAODATE小于当前时间
        response = tap.tapProjectInit(TENDER_PROJECT_CODE, TENDER_PROJECT_NAME, BID_SECTION_NAME, BID_SECTION_CODE, constructTime(-10000),
                BID_DOC_REFER_END_TIME, "1", "jstf", TBALLOWFILESIZE, "1.0",
                "1.0", ZBRPULICKEY, BID_SECTION_CODE_EX, EXTRA);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("Illegal Time"));

        //文件递交截止时间BID_DOC_REFER_END_TIME小于当前时间
        response = tap.tapProjectInit(TENDER_PROJECT_CODE, TENDER_PROJECT_NAME, BID_SECTION_NAME, BID_SECTION_CODE, KAIBIAODATE,
                constructTime(-10000), "1", "jstf", TBALLOWFILESIZE, "1.0",
                "1.0", ZBRPULICKEY, BID_SECTION_CODE_EX, EXTRA);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("Illegal Time"));

        //开标时间小于文件递交截止时间
        response = tap.tapProjectInit(TENDER_PROJECT_CODE, TENDER_PROJECT_NAME, BID_SECTION_NAME, BID_SECTION_CODE, constructTime(20000),
                constructTime(30000), "1", "jstf", TBALLOWFILESIZE, "1.0",
                "1.0", ZBRPULICKEY, BID_SECTION_CODE_EX, EXTRA);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("Illegal Time"));

        //公钥格式不正确
        response = tap.tapProjectInit(TENDER_PROJECT_CODE, TENDER_PROJECT_NAME, BID_SECTION_NAME, constructData("SC"), KAIBIAODATE,
                BID_DOC_REFER_END_TIME, "1", "jstf", TBALLOWFILESIZE, "1.0",
                "1.0", PUBKEY1, BID_SECTION_CODE_EX, EXTRA);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("ZBRPULICKEY verify err"));
    }

    @Test
    //招标信息更新接口必输字段校验
    public void tapProjectUpdateInterfaceTest() throws Exception {

        //项目标识ORDERNO为空
        String response = tap.tapProjectUpdate("", TENDER_PROJECT_CODE, TENDER_PROJECT_NAME, BID_SECTION_NAME, BID_SECTION_CODE, KAIBIAODATE,
                BID_DOC_REFER_END_TIME, "1", "jstf", TBALLOWFILESIZE, "1.0",
                "1.0", BID_SECTION_CODE_EX, EXTRA);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("Field validation for 'ORDERNO' failed on the 'required"));

        //项目标识ORDERNO为不存在的数据
        response = tap.tapProjectUpdate("123456", TENDER_PROJECT_CODE, TENDER_PROJECT_NAME, BID_SECTION_NAME, BID_SECTION_CODE, KAIBIAODATE,
                BID_DOC_REFER_END_TIME, "1", "jstf", TBALLOWFILESIZE, "1.0",
                "1.0", BID_SECTION_CODE_EX, EXTRA);
        assertEquals("404", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("orderNo[123456] not found"));

    }

    @Test
    //招标信息查询接口必输字段校验
    public void tapProjectDetailInterfaceTest() throws Exception {

        //项目标识ORDERNO为空
        String response = tap.tapProjectDetail("");
//        assertEquals("404", JSONObject.fromObject(response).getString("state"));
//        assertEquals(true, response.contains("Field validation for 'ProjectId' failed on the 'required"));

        //项目标识ORDERNO为不存在的数据
        response = tap.tapProjectDetail("123456789");
        assertEquals("404", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("orderNo[123456789] not found"));

    }

    @Test
    //投标文件合规性校验接口必输字段校验
    public void tapTenderVerifyInterfaceTest() throws Exception {

        //项目标识orderNo为空
        String response = tap.tapTenderVerify("", "unitName", "1", "1", "127", "",
                "", "", "useZBFileGuid", "", senderBidPlatform);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("Field validation for 'OrderNo' failed on the 'required"));

        //unitName为空
        response = tap.tapTenderVerify(ORDERNO, "", "1", "1", "234", "UserIdentifier_B",
                "", "", "useZBFileGuid", "", senderBidPlatform);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("Field validation for 'UnitName' failed on the 'required"));

        //isDownLoad为空
        response = tap.tapTenderVerify(ORDERNO, recordIdA, "", "1", "234", "UserIdentifier_B",
                "", "", "useZBFileGuid", "", senderBidPlatform);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("Field validation for 'IsDownLoad' failed on the 'required"));

        //isZhiFu为空
        response = tap.tapTenderVerify(ORDERNO, recordIdA, "1", "", "234", "UserIdentifier_B",
                "", "", "useZBFileGuid", "", senderBidPlatform);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("Field validation for 'IsZhiFu' failed on the 'required"));

        //useZBFileGuid为空
        response = tap.tapTenderVerify(ORDERNO, recordIdA, "1", "1", "234", "UserIdentifier_B",
                "", "", "", "", senderBidPlatform);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("Field validation for 'UseZBFileGuid' failed on the 'required"));

        //发送方sender为空
        response = tap.tapTenderVerify(ORDERNO, "unitName", "1", "1", "234", "UserIdentifier_B",
                "", "", "useZBFileGuid", "", "");
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("Field validation for 'Sender' failed on the 'required"));

        //发送方sender为错误数据
        response = tap.tapTenderVerify(ORDERNO, "unitName", "1", "1", "127", "",
                "", "", "useZBFileGuid", "", "abc");
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("Field validation for 'Sender' failed on the 'oneof' tag"));

        //招标平台调用，userIdentifier_B和userIdentifier_C同时为空
        response = tap.tapTenderVerify(ORDERNO, recordIdA, "1", "1", "234", "",
                "", "", "useZBFileGuid", "", senderBidPlatform);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("UserIdentifier_B and UserIdentifier_C cannot be null at the same time"));

        //文件平台调用，caType为空
        response = tap.tapTenderVerify(ORDERNO, recordIdA, "1", "1", "", "",
                "", "", "useZBFileGuid", "", senderFilePlatform);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("CaType cannot be null"));

        //userIdentifier为空,（发送方为文件上传平台时，必传）
        response = tap.tapTenderVerify(ORDERNO, recordIdA, "1", "1", "234", "UserIdentifier_B",
                "", "", "useZBFileGuid", "", senderFilePlatform);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("UserIdentifier cannot be null"));
    }

    @Test
    //投标文件上传接口必输字段校验
    public void tapTenderUploadInterfaceTest() throws Exception {

        //项目标识orderNo为空
        String response = tap.tapTenderUpload("", UID, recordIdA, fileHead, path, constructUnixTime(0));
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("Field validation for 'OrderNo' failed on the 'required"));

        //文件上传唯一标识uid为空
        response = tap.tapTenderUpload(ORDERNO, "", recordIdA, fileHead, path, constructUnixTime(0));
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("Field validation for 'Uid' failed on the 'required"));

        //投标标识recordId为空
        response = tap.tapTenderUpload(ORDERNO, UID, "", fileHead, path, constructUnixTime(0));
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("Field validation for 'RecordId' failed on the 'required"));

        //文件头fileHead为空
        response = tap.tapTenderUpload(ORDERNO, UID, recordIdA, "", path, constructUnixTime(0));
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("Field validation for 'FileHead' failed on the 'required"));

        //路径path为空
        response = tap.tapTenderUpload(ORDERNO, UID, recordIdA, fileHead, "", constructUnixTime(0));
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("Field validation for 'Path' failed on the 'required"));

        //文件上传时间uploadTime为空
        response = tap.tapTenderUpload(ORDERNO, UID, recordIdA, fileHead, path, 0);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("Field validation for 'UploadTime' failed on the 'required"));

        //项目标识orderNo为不存在的数据
        response = tap.tapTenderUpload("123456", UID, recordIdA, fileHead, path, constructUnixTime(0));
        assertEquals("404", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("orderNo[123456] not found"));

    }

    @Test
    //撤销投标接口必输字段校验
    public void tapTenderRevokeInterfaceTest() throws Exception {

        //撤销投标信息data为空
        String response = tap.tapTenderRevoke("", ORDERNO);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("Field validation for 'Data' failed on the 'required"));

        //项目标识orderNo为空
        response = tap.tapTenderRevoke("{\"unitname\":\"T02\",\"revoketime\":\"20211209162920\"}", "");
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("Field validation for 'OrderNo' failed on the 'required"));

        //项目标识orderNo为不存在的数据
        response = tap.tapTenderRevoke("{\"unitname\":\"T02\",\"revoketime\":\"20211209162920\"}", "123456");
        assertEquals("404", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("orderNo[123456] not found"));
    }

    @Test
    //获取投标记录接口必输字段校验
    public void tapTenderBackInterfaceTest() throws Exception {

        //文件上传唯一标识uid为空
        String response = tap.tapTenderBack("");
//        assertEquals("500", JSONObject.fromObject(response).getString("state"));
//        assertEquals(true, response.contains("Field validation for 'OrderNo' failed on the 'required"));

        //文件上传唯一标识uid为不存在的数据
        response = tap.tapTenderBack("1234");
        assertEquals("404", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("back info not found!"));

    }

    @Test
    //获取投标信息列表接口必输字段校验
    public void tapTenderRecordInterfaceTest() throws Exception {

        //项目标识orderNo为空
        String response = tap.tapTenderRecord("", recordIdA, false);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("Field validation for 'OrderNo' failed on the 'required"));

        //项目标识orderNo为不存在的数据
        response = tap.tapTenderRecord("123456", recordIdA, false);
        assertEquals("404", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("orderNo[123456] not found"));

    }

    @Test
    //开标接口必输字段校验
    public void tapTenderOpenInterfaceTest() throws Exception {

        //项目标识orderNo为空
        String response = tap.tapTenderOpen("");
        assertEquals("404", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("orderNo[] not found"));

        //项目标识orderNo为不存在的数据
        response = tap.tapTenderOpen("123456");
        assertEquals("404", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("orderNo[123456] not found"));

    }

}