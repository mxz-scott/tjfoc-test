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

    String sdkIP = SDKADD.substring(SDKADD.lastIndexOf("/") + 1, SDKADD.lastIndexOf(":"));


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

        //招标截止时间expireDate为空
        String response = tap.tapProjectInit(0, openDate, publicKey, identity, filesize, name, metaData);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("Field validation for 'ExpireDate' failed on the 'required"));

        //开标时间openDate为空
        response = tap.tapProjectInit(expireDate, 0, publicKey, identity, filesize, name, metaData);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("Field validation for 'OpenDate' failed on the 'required"));

        //招标方公钥publicKey为空
        response = tap.tapProjectInit(expireDate, openDate, "", identity, filesize, name, metaData);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("Field validation for 'PublicKey' failed on the 'required"));

        //招标方名称identity为空
        response = tap.tapProjectInit(expireDate, openDate, publicKey, "", filesize, name, metaData);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("Field validation for 'Identity' failed on the 'required"));

        //招标文件大小filesize为空
        response = tap.tapProjectInit(expireDate, openDate, publicKey, identity, 0, name, metaData);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("Field validation for 'Filesize' failed on the 'required"));

        //招标项目名称name为空
        response = tap.tapProjectInit(expireDate, openDate, publicKey, identity, filesize, "", metaData);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("Field validation for 'Name' failed on the 'required"));

        //元数据metaData为空
        response = tap.tapProjectInit(expireDate, openDate, publicKey, identity, filesize, name, null);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("Field validation for 'MetaData' failed on the 'required"));

        //招标截止时间小于当前时间
        response = tap.tapProjectInit(expireDate - 200, openDate, publicKey, identity, filesize, name, metaData);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("openDate or ExpireDate should not before now"));

        //开标时间小于当前时间
        response = tap.tapProjectInit(expireDate, openDate - 200, publicKey, identity, filesize, name, metaData);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("openDate or ExpireDate should not before now"));

        //开标时间小于招标截止时间
        response = tap.tapProjectInit(expireDate + 100, openDate, publicKey, identity, filesize, name, metaData);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("openDate should not before expireDate"));

        //公钥格式不正确
        response = tap.tapProjectInit(expireDate, openDate, PUBKEY1, identity, filesize, name, metaData);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("public verify err:encoding/hex"));
    }

    @Test
    //招标信息更新接口必输字段校验
    public void tapProjectUpdateInterfaceTest() throws Exception {

        //项目标识projectId为空
        String response = tap.tapProjectUpdate("", expireDate, openDate, metaData, name, stateNormal, filesize, sign);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("Field validation for 'ProjectId' failed on the 'required"));

        //签名sign为空
        response = tap.tapProjectUpdate(projectId, expireDate, openDate, metaData, name, stateNormal, filesize, "");
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("Field validation for 'Sign' failed on the 'required"));

        //项目标识projectId为不存在的数据
        response = tap.tapProjectUpdate("123456789", expireDate, openDate, metaData, name, stateNormal, filesize, sign);
        assertEquals("404", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("project info not found"));

        //签名sign为错误的签名数据
        response = tap.tapProjectUpdate(projectId, expireDate, openDate, metaData, name, stateNormal, filesize, "123");
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("wvm invoke err"));

    }

    @Test
    //招标信息查询接口必输字段校验
    public void tapProjectDetailInterfaceTest() throws Exception {

        //项目标识projectId为空
        String response = tap.tapProjectDetail("");
//        assertEquals("404", JSONObject.fromObject(response).getString("state"));
//        assertEquals(true, response.contains("Field validation for 'ProjectId' failed on the 'required"));

        //项目标识projectId为不存在的数据
        response = tap.tapProjectDetail("123456789");
        assertEquals("404", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("project info not found"));

    }

    @Test
    //投标文件合规性校验接口必输字段校验
    public void tapTenderVerifyInterfaceTest() throws Exception {

        //哈希hashvalue为空
        String response = tap.tapTenderVerify("", senderBidPlatform);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("Field validation for 'Hashvalue' failed on the 'required"));

        //发送方sender为空
        response = tap.tapTenderVerify("123", "");
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("Field validation for 'Sender' failed on the 'required"));

        //发送方sender为错误数据
        response = tap.tapTenderVerify("123", "abc");
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("Field validation for 'Sender' failed on the 'oneof' tag"));
    }

    @Test
    //投标文件上传接口必输字段校验
    public void tapTenderUploadInterfaceTest() throws Exception {

        //项目标识projectId为空
        String response = tap.tapTenderUpload("", recordIdA, fileHead, path);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("Field validation for 'ProjectId' failed on the 'required"));

        //投标标识recordId为空
        response = tap.tapTenderUpload(projectId, "", fileHead, path);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("Field validation for 'RecordId' failed on the 'required"));

        //文件头fileHead为空
        response = tap.tapTenderUpload(projectId, recordIdA, "", path);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("Field validation for 'FileHead' failed on the 'required"));

        //路径path为空
        response = tap.tapTenderUpload(projectId, recordIdA, fileHead, "");
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("Field validation for 'Path' failed on the 'required"));

        //项目标识projectId为不存在的数据
        response = tap.tapTenderUpload("123456", recordIdA, fileHead, path);
        assertEquals("404", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("project info not found"));

    }

    @Test
    //撤销投标接口必输字段校验
    public void tapTenderRevokeInterfaceTest() throws Exception {

        //撤销投标信息data为空
        String response = tap.tapTenderRevoke("", projectId);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("Field validation for 'Data' failed on the 'required"));

        //项目标识projectId为空
        response = tap.tapTenderRevoke(metaData.toString(), "");
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("Field validation for 'ProjectId' failed on the 'required"));

        //项目标识projectId为不存在的数据
        response = tap.tapTenderRevoke(metaData.toString(), "123456");
        assertEquals("404", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("project info not found"));
    }

    @Test
    //获取投标信息列表接口必输字段校验
    public void tapTenderRecordInterfaceTest() throws Exception {

        //项目标识projectId为空
        String response = tap.tapTenderRecord("", recordIdA, false, sign);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("Field validation for 'ProjectId' failed on the 'required"));

        //招标方签名sign空
        response = tap.tapTenderRecord(projectId, recordIdA, false, "");
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("Field validation for 'Sign' failed on the 'required"));

        //项目标识projectId为不存在的数据
        response = tap.tapTenderRecord("123456", recordIdA, false, sign);
        assertEquals("404", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("project info not found"));

        //签名sign为错误的签名数据
        response = tap.tapTenderRecord(projectId, recordIdA, false, "123456");
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("verify failed"));

    }

    @Test
    //开标接口必输字段校验
    public void tapTenderOpenInterfaceTest() throws Exception {

        //项目标识projectId为空
        String response = tap.tapTenderOpen("", sign);
        assertEquals("404", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("project info not found"));

        //招标方签名sign为空
        response = tap.tapTenderOpen(projectId, "");
        assertEquals("400", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("sign cannot empty"));

        //项目标识projectId为不存在的数据
        response = tap.tapTenderOpen("123456", sign);
        assertEquals("404", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("project info not found"));

        //签名sign为错误的签名数据
        sleepAndSaveInfo(30 * 1000);
        response = tap.tapTenderOpen(projectId, "123456");
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("wvm invoke err"));
    }

}