package com.tjfintech.common.functionTest.SYGTTest;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.SYGT;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class InterfacesTest {

    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    SYGT sygt = testBuilder.getSygt();


    @Test
    public void TC01_MemberJoinApply() throws Exception {
        String code = "test";
        String name = "name";
        String serviceEndPoint = "www.wutongchain.com";
        String account = "www.wutongchain.com";

        log.info(" +++++++++++++++ 非法类型测试 需要结合手动测试 +++++++++++++++ ");

        log.info(" +++++++++++++++ 必填字段校验 +++++++++++++++ ");
        code = "";
        String response  = sygt.SSMemberJoinApply(code,name,serviceEndPoint,account);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        code = "123";
        name = "";
        response  = sygt.SSMemberJoinApply(code,name,serviceEndPoint,account);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        name = "123";
        serviceEndPoint = "";
        response  = sygt.SSMemberJoinApply(code,name,serviceEndPoint,account);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        serviceEndPoint = "www.com";
        account = "";
        response  = sygt.SSMemberJoinApply(code,name,serviceEndPoint,account);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));


        log.info(" +++++++++++++++ code 长度检查 +++++++++++++++ ");//具体长度限制需要开发再确认
        code = UtilsClass.Random(512);
        response  = sygt.SSMemberJoinApply(code,name,serviceEndPoint,account);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        code = UtilsClass.Random(63);
        response  = sygt.SSMemberJoinApply(code,name,serviceEndPoint,account);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        code = UtilsClass.Random(64);
        response  = sygt.SSMemberJoinApply(code,name,serviceEndPoint,account);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        code = UtilsClass.Random(65);
        response  = sygt.SSMemberJoinApply(code,name,serviceEndPoint,account);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        code = UtilsClass.Random(8);//恢复合理长度

        log.info(" +++++++++++++++ name 长度检查 +++++++++++++++ ");
        name = UtilsClass.Random(512);
        response  = sygt.SSMemberJoinApply(code,name,serviceEndPoint,account);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        name = UtilsClass.Random(127);
        response  = sygt.SSMemberJoinApply(code,name,serviceEndPoint,account);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        name = UtilsClass.Random(128);
        response  = sygt.SSMemberJoinApply(code,name,serviceEndPoint,account);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        name = UtilsClass.Random(129);
        response  = sygt.SSMemberJoinApply(code,name,serviceEndPoint,account);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));


        name = UtilsClass.Random(8);//恢复合理长度

        log.info(" +++++++++++++++ serviceEndPoint 长度检查 +++++++++++++++ ");
        serviceEndPoint = UtilsClass.Random(512);
        response  = sygt.SSMemberJoinApply(code,name,serviceEndPoint,account);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        serviceEndPoint = UtilsClass.Random(127);
        response  = sygt.SSMemberJoinApply(code,name,serviceEndPoint,account);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        serviceEndPoint = UtilsClass.Random(128);
        response  = sygt.SSMemberJoinApply(code,name,serviceEndPoint,account);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        serviceEndPoint = UtilsClass.Random(129);
        response  = sygt.SSMemberJoinApply(code,name,serviceEndPoint,account);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));


        serviceEndPoint = UtilsClass.Random(20);//恢复合理长度

        log.info(" +++++++++++++++ account 长度检查 +++++++++++++++ ");
        account = UtilsClass.Random(512);
        response  = sygt.SSMemberJoinApply(code,name,serviceEndPoint,account);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        account = UtilsClass.Random(127);
        response  = sygt.SSMemberJoinApply(code,name,serviceEndPoint,account);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        account = UtilsClass.Random(128);
        response  = sygt.SSMemberJoinApply(code,name,serviceEndPoint,account);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        account = UtilsClass.Random(129);
        response  = sygt.SSMemberJoinApply(code,name,serviceEndPoint,account);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));
    }

    @Test
    public void TC02_MemberJoinApprove() throws Exception {
        String code = "test";

        log.info(" +++++++++++++++ 必填字段校验 +++++++++++++++ ");
        log.info(" +++++++++++++++ Boolean 必填字段校验需要手动测试 +++++++++++++++ ");
        code = "";
        String response  = sygt.SSMemberJoinApprove(code,true);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));


        log.info(" +++++++++++++++ code 长度检查 +++++++++++++++ ");//具体长度限制需要开发再确认
        code = UtilsClass.Random(512);
        response  = sygt.SSMemberJoinApprove(code,true);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        code = UtilsClass.Random(63);
        response  = sygt.SSMemberJoinApprove(code,true);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        code = UtilsClass.Random(64);
        response  = sygt.SSMemberJoinApprove(code,true);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        code = UtilsClass.Random(65);
        response  = sygt.SSMemberJoinApprove(code,true);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

    }

    @Test
    public void TC03_MemberExitApply() throws Exception {
        String code = "test";
        String name = "name";
        String serviceEndPoint = "www.wutongchain.com";
        String account = "www.wutongchain.com";

        log.info(" +++++++++++++++ 非法类型测试 需要结合手动测试 +++++++++++++++ ");

        log.info(" +++++++++++++++ 必填字段校验 +++++++++++++++ ");
        code = "";
        String response  = sygt.SSMemberExitApply(code,name,serviceEndPoint);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        code = "123";
        name = "";
        response  = sygt.SSMemberExitApply(code,name,serviceEndPoint);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        name = "123";
        serviceEndPoint = "";
        response  = sygt.SSMemberExitApply(code,name,serviceEndPoint);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        serviceEndPoint = "www.com";
        account = "";
        response  = sygt.SSMemberExitApply(code,name,serviceEndPoint);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));


        log.info(" +++++++++++++++ code 长度检查 +++++++++++++++ ");//具体长度限制需要开发再确认
        code = UtilsClass.Random(512);
        response  = sygt.SSMemberExitApply(code,name,serviceEndPoint);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        code = UtilsClass.Random(63);
        response  = sygt.SSMemberExitApply(code,name,serviceEndPoint);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        code = UtilsClass.Random(64);
        response  = sygt.SSMemberExitApply(code,name,serviceEndPoint);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        code = UtilsClass.Random(65);
        response  = sygt.SSMemberExitApply(code,name,serviceEndPoint);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        code = UtilsClass.Random(8);//恢复合理长度

        log.info(" +++++++++++++++ name 长度检查 +++++++++++++++ ");
        name = UtilsClass.Random(512);
        response  = sygt.SSMemberExitApply(code,name,serviceEndPoint);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        name = UtilsClass.Random(127);
        response  = sygt.SSMemberExitApply(code,name,serviceEndPoint);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        name = UtilsClass.Random(128);
        response  = sygt.SSMemberExitApply(code,name,serviceEndPoint);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        name = UtilsClass.Random(129);
        response  = sygt.SSMemberExitApply(code,name,serviceEndPoint);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));


        name = UtilsClass.Random(8);//恢复合理长度

        log.info(" +++++++++++++++ serviceEndPoint 长度检查 +++++++++++++++ ");
        serviceEndPoint = UtilsClass.Random(512);
        response  = sygt.SSMemberExitApply(code,name,serviceEndPoint);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        serviceEndPoint = UtilsClass.Random(127);
        response  = sygt.SSMemberExitApply(code,name,serviceEndPoint);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        serviceEndPoint = UtilsClass.Random(128);
        response  = sygt.SSMemberExitApply(code,name,serviceEndPoint);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        serviceEndPoint = UtilsClass.Random(129);
        response  = sygt.SSMemberExitApply(code,name,serviceEndPoint);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));


        serviceEndPoint = UtilsClass.Random(20);//恢复合理长度

    }

    @Test
    public void TC04_MemberExitApprove() throws Exception {
        String code = "test";

        log.info(" +++++++++++++++ 必填字段校验 +++++++++++++++ ");
        log.info(" +++++++++++++++ Boolean 必填字段校验需要手动测试 +++++++++++++++ ");
        code = "";
        String response  = sygt.SSMemberExitApprove(code,true);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));


        log.info(" +++++++++++++++ code 长度检查 +++++++++++++++ ");//具体长度限制需要开发再确认
        code = UtilsClass.Random(512);
        response  = sygt.SSMemberExitApprove(code,true);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        code = UtilsClass.Random(63);
        response  = sygt.SSMemberExitApprove(code,true);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        code = UtilsClass.Random(64);
        response  = sygt.SSMemberExitApprove(code,true);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        code = UtilsClass.Random(65);
        response  = sygt.SSMemberExitApprove(code,true);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

    }

    @Test
    public void TC05_MemberGet() throws Exception {
        String code = "test";

        log.info(" +++++++++++++++ 必填字段校验 +++++++++++++++ ");
        code = "";
        String response  = sygt.SSMembersGet(code);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));


        log.info(" +++++++++++++++ code 长度检查 +++++++++++++++ ");//具体长度限制需要开发再确认
        code = UtilsClass.Random(512);
        response  = sygt.SSMembersGet(code);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        code = UtilsClass.Random(63);
        response  = sygt.SSMembersGet(code);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        code = UtilsClass.Random(64);
        response  = sygt.SSMembersGet(code);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        code = UtilsClass.Random(65);
        response  = sygt.SSMembersGet(code);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

    }

    @Test
    public void TC07_AssetPublish() throws Exception {
        String assetID = "test";
        String scene = "test";
        String label = "test";
        int amount = 20;
        String desc = "test";

        log.info(" +++++++++++++++ 必填字段校验 +++++++++++++++ ");
        assetID = "";
        String response  = sygt.SSAssetPublish(assetID,scene,label,amount,desc);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        assetID = "123";
        scene = "";
        response  = sygt.SSAssetPublish(assetID,scene,label,amount,desc);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        scene = "123";
        label = "";
        response  = sygt.SSAssetPublish(assetID,scene,label,amount,desc);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        //非必填字段检查
        desc = "";
        label = "123";
        response  = sygt.SSAssetPublish(assetID,scene,label,amount,desc);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));


        log.info(" +++++++++++++++ assetID 长度检查 +++++++++++++++ ");//具体长度限制需要开发再确认
        assetID = UtilsClass.Random(512);
        response  = sygt.SSAssetPublish(assetID,scene,label,amount,desc);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        assetID = UtilsClass.Random(63);
        response  = sygt.SSAssetPublish(assetID,scene,label,amount,desc);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        assetID = UtilsClass.Random(64);
        response  = sygt.SSAssetPublish(assetID,scene,label,amount,desc);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        assetID = UtilsClass.Random(65);
        response  = sygt.SSAssetPublish(assetID,scene,label,amount,desc);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        assetID = UtilsClass.Random(8);

        log.info(" +++++++++++++++ scene 长度检查 +++++++++++++++ ");//具体长度限制需要开发再确认
        scene = UtilsClass.Random(512);
        response  = sygt.SSAssetPublish(assetID,scene,label,amount,desc);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        scene = UtilsClass.Random(63);
        response  = sygt.SSAssetPublish(assetID,scene,label,amount,desc);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        scene = UtilsClass.Random(64);
        response  = sygt.SSAssetPublish(assetID,scene,label,amount,desc);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        scene = UtilsClass.Random(65);
        response  = sygt.SSAssetPublish(assetID,scene,label,amount,desc);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        scene = UtilsClass.Random(6);

        log.info(" +++++++++++++++ label 长度检查 +++++++++++++++ ");//具体长度限制需要开发再确认
        label = UtilsClass.Random(512);
        response  = sygt.SSAssetPublish(assetID,scene,label,amount,desc);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        label = UtilsClass.Random(63);
        response  = sygt.SSAssetPublish(assetID,scene,label,amount,desc);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        label = UtilsClass.Random(64);
        response  = sygt.SSAssetPublish(assetID,scene,label,amount,desc);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        label = UtilsClass.Random(65);
        response  = sygt.SSAssetPublish(assetID,scene,label,amount,desc);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        label = UtilsClass.Random(6);

        log.info(" +++++++++++++++ amount 检查 +++++++++++++++ ");//具体长度限制需要开发再确认
        amount = 0;
        response  = sygt.SSAssetPublish(assetID,scene,label,amount,desc);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        amount = -1;
        response  = sygt.SSAssetPublish(assetID,scene,label,amount,desc);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        amount = 20;

        log.info(" +++++++++++++++ desc 检查 +++++++++++++++ ");//具体长度限制需要开发再确认
        desc = UtilsClass.Random(1028);
        response  = sygt.SSAssetPublish(assetID,scene,label,amount,desc);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        desc = UtilsClass.Random(513);
        response  = sygt.SSAssetPublish(assetID,scene,label,amount,desc);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        desc = UtilsClass.Random(512);
        response  = sygt.SSAssetPublish(assetID,scene,label,amount,desc);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));

        desc = UtilsClass.Random(511);
        response  = sygt.SSAssetPublish(assetID,scene,label,amount,desc);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));

    }

    @Test
    public void TC08_AssetUpdate() throws Exception {
        String assetID = "test";
        String scene = "test";
        String label = "test";
        int amount = 20;
        String desc = "test";

        log.info(" +++++++++++++++ 必填字段校验 +++++++++++++++ ");
        assetID = "";
        String response  = sygt.SSAssetUpdate(assetID,scene,label,amount,desc);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        assetID = "123";
        scene = "";
        response  = sygt.SSAssetUpdate(assetID,scene,label,amount,desc);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        scene = "123";
        label = "";
        response  = sygt.SSAssetUpdate(assetID,scene,label,amount,desc);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        //非必填字段检查
        desc = "";
        label = "123";
        response  = sygt.SSAssetUpdate(assetID,scene,label,amount,desc);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));


        log.info(" +++++++++++++++ assetID 长度检查 +++++++++++++++ ");//具体长度限制需要开发再确认
        assetID = UtilsClass.Random(512);
        response  = sygt.SSAssetUpdate(assetID,scene,label,amount,desc);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        assetID = UtilsClass.Random(63);
        response  = sygt.SSAssetUpdate(assetID,scene,label,amount,desc);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        assetID = UtilsClass.Random(64);
        response  = sygt.SSAssetUpdate(assetID,scene,label,amount,desc);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        assetID = UtilsClass.Random(65);
        response  = sygt.SSAssetUpdate(assetID,scene,label,amount,desc);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        assetID = UtilsClass.Random(8);

        log.info(" +++++++++++++++ scene 长度检查 +++++++++++++++ ");//具体长度限制需要开发再确认
        scene = UtilsClass.Random(512);
        response  = sygt.SSAssetUpdate(assetID,scene,label,amount,desc);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        scene = UtilsClass.Random(63);
        response  = sygt.SSAssetUpdate(assetID,scene,label,amount,desc);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        scene = UtilsClass.Random(64);
        response  = sygt.SSAssetUpdate(assetID,scene,label,amount,desc);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        scene = UtilsClass.Random(65);
        response  = sygt.SSAssetUpdate(assetID,scene,label,amount,desc);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        scene = UtilsClass.Random(6);

        log.info(" +++++++++++++++ label 长度检查 +++++++++++++++ ");//具体长度限制需要开发再确认
        label = UtilsClass.Random(512);
        response  = sygt.SSAssetUpdate(assetID,scene,label,amount,desc);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        label = UtilsClass.Random(63);
        response  = sygt.SSAssetUpdate(assetID,scene,label,amount,desc);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        label = UtilsClass.Random(64);
        response  = sygt.SSAssetUpdate(assetID,scene,label,amount,desc);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        label = UtilsClass.Random(65);
        response  = sygt.SSAssetUpdate(assetID,scene,label,amount,desc);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        label = UtilsClass.Random(6);

        log.info(" +++++++++++++++ amount 检查 +++++++++++++++ ");//具体长度限制需要开发再确认
        amount = 0;
        response  = sygt.SSAssetUpdate(assetID,scene,label,amount,desc);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        amount = -1;
        response  = sygt.SSAssetUpdate(assetID,scene,label,amount,desc);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        amount = 20;

        log.info(" +++++++++++++++ desc 检查 +++++++++++++++ ");//具体长度限制需要开发再确认
        desc = UtilsClass.Random(1028);
        response  = sygt.SSAssetUpdate(assetID,scene,label,amount,desc);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        desc = UtilsClass.Random(513);
        response  = sygt.SSAssetUpdate(assetID,scene,label,amount,desc);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        desc = UtilsClass.Random(512);
        response  = sygt.SSAssetUpdate(assetID,scene,label,amount,desc);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));

        desc = UtilsClass.Random(511);
        response  = sygt.SSAssetUpdate(assetID,scene,label,amount,desc);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));

    }

    @Test
    public void TC09_AssetOff() throws Exception {
        String assetID = "test";

        log.info(" +++++++++++++++ 必填字段校验 +++++++++++++++ ");
        assetID = "";
        String response  = sygt.SSAssetOff(assetID);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        assetID = "123";

        log.info(" +++++++++++++++ assetID 长度检查 +++++++++++++++ ");//具体长度限制需要开发再确认
        assetID = UtilsClass.Random(512);
        response  = sygt.SSAssetOff(assetID);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        assetID = UtilsClass.Random(63);
        response  = sygt.SSAssetOff(assetID);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        assetID = UtilsClass.Random(64);
        response  = sygt.SSAssetOff(assetID);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        assetID = UtilsClass.Random(65);
        response  = sygt.SSAssetOff(assetID);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        assetID = UtilsClass.Random(8);

    }

    @Test
    public void TC10_AssetQuery() throws Exception {
        String scene = "test";
        String label = "test";

        log.info(" +++++++++++++++ 必填字段校验 +++++++++++++++ ");

        scene = "";
        String response  = sygt.SSAssetQuery(scene,label);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        scene = "123";
        label = "";
        response  = sygt.SSAssetQuery(scene,label);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        label = "123";


        log.info(" +++++++++++++++ scene 长度检查 +++++++++++++++ ");//具体长度限制需要开发再确认
        scene = UtilsClass.Random(512);
        response  = sygt.SSAssetQuery(scene,label);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        scene = UtilsClass.Random(63);
        response  = sygt.SSAssetQuery(scene,label);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        scene = UtilsClass.Random(64);
        response  = sygt.SSAssetQuery(scene,label);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        scene = UtilsClass.Random(65);
        response  = sygt.SSAssetQuery(scene,label);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        scene = UtilsClass.Random(6);

        log.info(" +++++++++++++++ label 长度检查 +++++++++++++++ ");//具体长度限制需要开发再确认
        label = UtilsClass.Random(512);
        response  = sygt.SSAssetQuery(scene,label);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        label = UtilsClass.Random(63);
        response  = sygt.SSAssetQuery(scene,label);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        label = UtilsClass.Random(64);
        response  = sygt.SSAssetQuery(scene,label);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        label = UtilsClass.Random(65);
        response  = sygt.SSAssetQuery(scene,label);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        label = UtilsClass.Random(6);

    }

    @Test
    public void TC11_AssetAuthorize() throws Exception {
        String assetID = "test";
        String code = "test";
        String serviceID = "test";
        String expireStart = "2021-03-30 10:00:00";
        String expireEnd = "2023-03-30 10:00:00";

        log.info(" +++++++++++++++ 必填字段校验 +++++++++++++++ ");

        assetID = "";
        String response  = sygt.SSAssetAuthorize(assetID,code,serviceID,expireStart,expireEnd);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        assetID = "123";
        code = "";
        response  = sygt.SSAssetAuthorize(assetID,code,serviceID,expireStart,expireEnd);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        code = "123";
        serviceID = "";
        response  = sygt.SSAssetAuthorize(assetID,code,serviceID,expireStart,expireEnd);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        serviceID = "123";
        expireStart = "";
        response  = sygt.SSAssetAuthorize(assetID,code,serviceID,expireStart,expireEnd);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        expireStart = "2021-03-30 10:00:00";
        expireEnd = "";
        response  = sygt.SSAssetAuthorize(assetID,code,serviceID,expireStart,expireEnd);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        expireEnd = "2023-03-30 10:00:00";

        log.info(" +++++++++++++++ assetID 长度检查 +++++++++++++++ ");//具体长度限制需要开发再确认
        assetID = UtilsClass.Random(512);
        response  = sygt.SSAssetAuthorize(assetID,code,serviceID,expireStart,expireEnd);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        assetID = UtilsClass.Random(63);
        response  = sygt.SSAssetAuthorize(assetID,code,serviceID,expireStart,expireEnd);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        assetID = UtilsClass.Random(64);
        response  = sygt.SSAssetAuthorize(assetID,code,serviceID,expireStart,expireEnd);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        assetID = UtilsClass.Random(65);
        response  = sygt.SSAssetAuthorize(assetID,code,serviceID,expireStart,expireEnd);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        assetID = UtilsClass.Random(6);

        log.info(" +++++++++++++++ code 长度检查 +++++++++++++++ ");//具体长度限制需要开发再确认
        code = UtilsClass.Random(512);
        response  = sygt.SSAssetAuthorize(assetID,code,serviceID,expireStart,expireEnd);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        code = UtilsClass.Random(63);
        response  = sygt.SSAssetAuthorize(assetID,code,serviceID,expireStart,expireEnd);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        code = UtilsClass.Random(64);
        response  = sygt.SSAssetAuthorize(assetID,code,serviceID,expireStart,expireEnd);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        code = UtilsClass.Random(65);
        response  = sygt.SSAssetAuthorize(assetID,code,serviceID,expireStart,expireEnd);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        code = UtilsClass.Random(6);

        log.info(" +++++++++++++++ serviceID 长度检查 +++++++++++++++ ");//具体长度限制需要开发再确认
        serviceID = UtilsClass.Random(512);
        response  = sygt.SSAssetAuthorize(assetID,code,serviceID,expireStart,expireEnd);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        serviceID = UtilsClass.Random(63);
        response  = sygt.SSAssetAuthorize(assetID,code,serviceID,expireStart,expireEnd);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        serviceID = UtilsClass.Random(64);
        response  = sygt.SSAssetAuthorize(assetID,code,serviceID,expireStart,expireEnd);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        serviceID = UtilsClass.Random(65);
        response  = sygt.SSAssetAuthorize(assetID,code,serviceID,expireStart,expireEnd);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        serviceID = UtilsClass.Random(6);

        log.info(" +++++++++++++++ expireStart 格式非法 +++++++++++++++ ");//具体长度限制需要开发再确认
        expireStart = UtilsClass.Random(512);
        response  = sygt.SSAssetAuthorize(assetID,code,serviceID,expireStart,expireEnd);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        expireStart = "中文";
        response  = sygt.SSAssetAuthorize(assetID,code,serviceID,expireStart,expireEnd);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));


        expireStart = "2021-03-30 10:00:00";

        log.info(" +++++++++++++++ expireEnd 格式非法 +++++++++++++++ ");//具体长度限制需要开发再确认
        expireEnd = UtilsClass.Random(512);
        response  = sygt.SSAssetAuthorize(assetID,code,serviceID,expireStart,expireEnd);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        expireEnd = "中文";
        response  = sygt.SSAssetAuthorize(assetID,code,serviceID,expireStart,expireEnd);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        expireEnd = "2031-03-30 10:00:00";

    }

    @Test
    public void TC12_AssetCancelAuthorize() throws Exception {
        String assetID = "test";
        String code = "test";

        log.info(" +++++++++++++++ 必填字段校验 +++++++++++++++ ");

        assetID = "";
        String response  = sygt.SSAssetCancelAuthority(assetID,code);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        assetID = "123";
        code = "";
        response  = sygt.SSAssetCancelAuthority(assetID,code);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        code = "123";

        log.info(" +++++++++++++++ assetID 长度检查 +++++++++++++++ ");//具体长度限制需要开发再确认
        assetID = UtilsClass.Random(512);
        response  = sygt.SSAssetCancelAuthority(assetID,code);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        assetID = UtilsClass.Random(63);
        response  = sygt.SSAssetCancelAuthority(assetID,code);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        assetID = UtilsClass.Random(64);
        response  = sygt.SSAssetCancelAuthority(assetID,code);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        assetID = UtilsClass.Random(65);
        response  = sygt.SSAssetCancelAuthority(assetID,code);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        assetID = UtilsClass.Random(6);

        log.info(" +++++++++++++++ code 长度检查 +++++++++++++++ ");//具体长度限制需要开发再确认
        code = UtilsClass.Random(512);
        response  = sygt.SSAssetCancelAuthority(assetID,code);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        code = UtilsClass.Random(63);
        response  = sygt.SSAssetCancelAuthority(assetID,code);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        code = UtilsClass.Random(64);
        response  = sygt.SSAssetCancelAuthority(assetID,code);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        code = UtilsClass.Random(65);
        response  = sygt.SSAssetCancelAuthority(assetID,code);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        code = UtilsClass.Random(6);
    }

    @Test
    public void TC17_AssetVeriryAuth() throws Exception {
        String assetID = "test";
        String account = "test";
        log.info(" +++++++++++++++ 必填字段校验 +++++++++++++++ ");

        assetID = "";
        String response  = sygt.SSAssetVeriryAuthority(assetID,account);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        assetID = "test";
        account = "";
        response  = sygt.SSAssetVeriryAuthority(assetID,account);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        account = "test";

        log.info(" +++++++++++++++ assetID 长度检查 +++++++++++++++ ");//具体长度限制需要开发再确认
        assetID = UtilsClass.Random(512);
        response  = sygt.SSAssetVeriryAuthority(assetID,account);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        assetID = UtilsClass.Random(63);
        response  = sygt.SSAssetVeriryAuthority(assetID,account);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        assetID = UtilsClass.Random(64);
        response  = sygt.SSAssetVeriryAuthority(assetID,account);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        assetID = UtilsClass.Random(65);
        response  = sygt.SSAssetVeriryAuthority(assetID,account);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        assetID = UtilsClass.Random(6);

        log.info(" +++++++++++++++ account 长度检查 +++++++++++++++ ");//具体长度限制需要开发再确认
        account = UtilsClass.Random(512);
        response  = sygt.SSAssetVeriryAuthority(assetID,account);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        account = UtilsClass.Random(63);
        response  = sygt.SSAssetVeriryAuthority(assetID,account);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        account = UtilsClass.Random(64);
        response  = sygt.SSAssetVeriryAuthority(assetID,account);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        account = UtilsClass.Random(65);
        response  = sygt.SSAssetVeriryAuthority(assetID,account);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        account = UtilsClass.Random(6);
    }

    @Test
    public void TC13_SceneLableUpdate() throws Exception {
        List<String> scenes = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        scenes.add("123");
        labels.add("1235");

        log.info(" +++++++++++++++ 必填字段校验 +++++++++++++++ ");
        scenes.add("");//存在一个空值元素
        String response = sygt.SSSettingUpdate(scenes,labels);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));


        scenes.add("");//存在多个空值元素
        response = sygt.SSSettingUpdate(scenes,labels);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        scenes.clear();//空值
        response = sygt.SSSettingUpdate(scenes,labels);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));


        scenes.add("test eee");//待空格的元素
        response = sygt.SSSettingUpdate(scenes,labels);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));


        labels.add("");//存在一个空值元素
        response = sygt.SSSettingUpdate(scenes,labels);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        labels.add("");//存在多个空值元素
        response = sygt.SSSettingUpdate(scenes,labels);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        labels.clear();//存在多个空值元素
        response = sygt.SSSettingUpdate(scenes,labels);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        labels.add("test eee");//待空格的元素
        response = sygt.SSSettingUpdate(scenes,labels);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));


        log.info(" +++++++++++++++ list较长 +++++++++++++++ ");
        for(int i =0;i<50;i++){
            scenes.add("scene" + i);
            labels.add("labels" + i);
        }

        response = sygt.SSSettingUpdate(scenes,labels);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));
        for(int i =0;i<500;i++){
            scenes.add("scene" + i);
            labels.add("labels" + i);
        }
        response = sygt.SSSettingUpdate(scenes,labels);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

    }

    @Test
    public void TC15_PonitUpdate() throws Exception {
        String account = "12344";
        String type = "credit";
        String code = "A001";
        int amount = 30;

        log.info(" +++++++++++++++ 必填字段校验 +++++++++++++++ ");
        account = "";
        String response = sygt.SSPointUpdate(account,type,code,amount);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));


        account = "366666";
        type = "";
        response = sygt.SSPointUpdate(account,type,code,amount);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));


        type = "debit";
        code = "";
        response = sygt.SSPointUpdate(account,type,code,amount);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        log.info(" +++++++++++++++ account 长度检查 +++++++++++++++ ");//具体长度限制需要开发再确认
        account = UtilsClass.Random(512);
        response  = sygt.SSPointUpdate(account,type,code,amount);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        account = UtilsClass.Random(63);
        response  = sygt.SSPointUpdate(account,type,code,amount);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        account = UtilsClass.Random(64);
        response  = sygt.SSPointUpdate(account,type,code,amount);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        account = UtilsClass.Random(65);
        response  = sygt.SSPointUpdate(account,type,code,amount);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        account = UtilsClass.Random(8);

        log.info(" +++++++++++++++ type 长度检查 +++++++++++++++ ");//具体长度限制需要开发再确认
        type = UtilsClass.Random(512);
        response  = sygt.SSPointUpdate(account,type,code,amount);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        type = UtilsClass.Random(63);
        response  = sygt.SSPointUpdate(account,type,code,amount);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        type = UtilsClass.Random(64);
        response  = sygt.SSPointUpdate(account,type,code,amount);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        type = UtilsClass.Random(65);
        response  = sygt.SSPointUpdate(account,type,code,amount);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        type = "add";//非法字段值
        response  = sygt.SSPointUpdate(account,type,code,amount);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        type = "credit";

        log.info(" +++++++++++++++ code 长度检查 +++++++++++++++ ");//具体长度限制需要开发再确认
        code = UtilsClass.Random(512);
        response  = sygt.SSPointUpdate(account,type,code,amount);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        code = UtilsClass.Random(63);
        response  = sygt.SSPointUpdate(account,type,code,amount);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        code = UtilsClass.Random(64);
        response  = sygt.SSPointUpdate(account,type,code,amount);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        code = UtilsClass.Random(65);
        response  = sygt.SSPointUpdate(account,type,code,amount);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        log.info("需要考虑是否存在code非法的情况，待需求文档确认");
        code = UtilsClass.Random(6);
        response  = sygt.SSPointUpdate(account,type,code,amount);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        code = "A001";

        log.info(" +++++++++++++++ amount 检查 +++++++++++++++ ");//具体长度限制需要开发再确认
        amount = 0;
        response  = sygt.SSPointUpdate(account,type,code,amount);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        amount = -1;
        response  = sygt.SSPointUpdate(account,type,code,amount);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        amount = 20;

    }

    @Test
    public void TC16_PointQuery() throws Exception {
        String account = "test";

        log.info(" +++++++++++++++ 必填字段校验 +++++++++++++++ ");

        account = "";
        String response  = sygt.SSPointQuery(account);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        account = "test";

        log.info(" +++++++++++++++ account 长度检查 +++++++++++++++ ");//具体长度限制需要开发再确认
        account = UtilsClass.Random(512);
        response  = sygt.SSPointQuery(account);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        account = UtilsClass.Random(63);
        response  = sygt.SSPointQuery(account);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        account = UtilsClass.Random(64);
        response  = sygt.SSPointQuery(account);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        account = UtilsClass.Random(65);
        response  = sygt.SSPointQuery(account);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        account = UtilsClass.Random(6);
    }

    @Test
    public void TC18_SingleSafeComplete() throws Exception {
        String requestID = "test";
        String partyA = "test";
        String partyB = "test";
        String replyDigest = "2021-03-30 10:00:00";

        log.info(" +++++++++++++++ 必填字段校验 +++++++++++++++ ");

        requestID = "";
        String response  = sygt.SSSingleSafeQueryComplete(requestID,partyA,partyB,replyDigest);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        requestID = "123";
        partyA = "";
        response  = sygt.SSSingleSafeQueryComplete(requestID,partyA,partyB,replyDigest);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        partyA = "123";
        partyB = "";
        response  = sygt.SSSingleSafeQueryComplete(requestID,partyA,partyB,replyDigest);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        partyB = "123";
        replyDigest = "";
        response  = sygt.SSSingleSafeQueryComplete(requestID,partyA,partyB,replyDigest);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        replyDigest = "摘要信息";

        log.info(" +++++++++++++++ requestID 长度检查 +++++++++++++++ ");//具体长度限制需要开发再确认
        requestID = UtilsClass.Random(512);
        response  = sygt.SSSingleSafeQueryComplete(requestID,partyA,partyB,replyDigest);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        requestID = UtilsClass.Random(63);
        response  = sygt.SSSingleSafeQueryComplete(requestID,partyA,partyB,replyDigest);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        requestID = UtilsClass.Random(64);
        response  = sygt.SSSingleSafeQueryComplete(requestID,partyA,partyB,replyDigest);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        requestID = UtilsClass.Random(65);
        response  = sygt.SSSingleSafeQueryComplete(requestID,partyA,partyB,replyDigest);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        requestID = UtilsClass.Random(6);

        log.info(" +++++++++++++++ partyA 长度检查 +++++++++++++++ ");//具体长度限制需要开发再确认
        partyA = UtilsClass.Random(512);
        response  = sygt.SSSingleSafeQueryComplete(requestID,partyA,partyB,replyDigest);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        partyA = UtilsClass.Random(63);
        response  = sygt.SSSingleSafeQueryComplete(requestID,partyA,partyB,replyDigest);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        partyA = UtilsClass.Random(64);
        response  = sygt.SSSingleSafeQueryComplete(requestID,partyA,partyB,replyDigest);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        partyA = UtilsClass.Random(65);
        response  = sygt.SSSingleSafeQueryComplete(requestID,partyA,partyB,replyDigest);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        partyA = UtilsClass.Random(6);

        log.info(" +++++++++++++++ partyB 长度检查 +++++++++++++++ ");//具体长度限制需要开发再确认
        partyB = UtilsClass.Random(512);
        response  = sygt.SSSingleSafeQueryComplete(requestID,partyA,partyB,replyDigest);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        partyB = UtilsClass.Random(63);
        response  = sygt.SSSingleSafeQueryComplete(requestID,partyA,partyB,replyDigest);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        partyB = UtilsClass.Random(64);
        response  = sygt.SSSingleSafeQueryComplete(requestID,partyA,partyB,replyDigest);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        partyB = UtilsClass.Random(65);
        response  = sygt.SSSingleSafeQueryComplete(requestID,partyA,partyB,replyDigest);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        partyB = UtilsClass.Random(6);

        log.info(" +++++++++++++++ replyDigest 长度检查 +++++++++++++++ ");//具体长度限制需要开发再确认
        replyDigest = UtilsClass.Random(512);
        response  = sygt.SSSingleSafeQueryComplete(requestID,partyA,partyB,replyDigest);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        replyDigest = UtilsClass.Random(63);
        response  = sygt.SSSingleSafeQueryComplete(requestID,partyA,partyB,replyDigest);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        replyDigest = UtilsClass.Random(64);
        response  = sygt.SSSingleSafeQueryComplete(requestID,partyA,partyB,replyDigest);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        replyDigest = UtilsClass.Random(65);
        response  = sygt.SSSingleSafeQueryComplete(requestID,partyA,partyB,replyDigest);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        replyDigest = UtilsClass.Random(6);

    }

    @Test
    public void TC19_SingleSafeReply() throws Exception {
        String requestID = "test";
        Boolean bHit = true;
        int elapsed = 30000;
        String metadata = "test";
        String createdOn = "2021-03-30 10:00:00";

        log.info(" +++++++++++++++ 必填字段校验 +++++++++++++++ ");

        requestID = "";
        String response  = sygt.SSSingleSafeQueryReply(requestID,bHit,elapsed,metadata,createdOn);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        requestID = "123";
        metadata = "";
        response  = sygt.SSSingleSafeQueryReply(requestID,bHit,elapsed,metadata,createdOn);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        metadata = "123";
        createdOn = "";
        response  = sygt.SSSingleSafeQueryReply(requestID,bHit,elapsed,metadata,createdOn);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        createdOn = "2021-03-30 10:00:00";

        log.info(" +++++++++++++++ requestID 长度检查 +++++++++++++++ ");//具体长度限制需要开发再确认
        requestID = UtilsClass.Random(512);
        response  = sygt.SSSingleSafeQueryReply(requestID,bHit,elapsed,metadata,createdOn);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        requestID = UtilsClass.Random(63);
        response  = sygt.SSSingleSafeQueryReply(requestID,bHit,elapsed,metadata,createdOn);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        requestID = UtilsClass.Random(64);
        response  = sygt.SSSingleSafeQueryReply(requestID,bHit,elapsed,metadata,createdOn);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        requestID = UtilsClass.Random(65);
        response  = sygt.SSSingleSafeQueryReply(requestID,bHit,elapsed,metadata,createdOn);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        requestID = UtilsClass.Random(6);

        log.info(" +++++++++++++++ metadata 长度检查 +++++++++++++++ ");//具体长度限制需要开发再确认
        metadata = UtilsClass.Random(512);
        response  = sygt.SSSingleSafeQueryReply(requestID,bHit,elapsed,metadata,createdOn);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        metadata = UtilsClass.Random(63);
        response  = sygt.SSSingleSafeQueryReply(requestID,bHit,elapsed,metadata,createdOn);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        metadata = UtilsClass.Random(64);
        response  = sygt.SSSingleSafeQueryReply(requestID,bHit,elapsed,metadata,createdOn);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        metadata = UtilsClass.Random(65);
        response  = sygt.SSSingleSafeQueryReply(requestID,bHit,elapsed,metadata,createdOn);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        metadata = UtilsClass.Random(6);

        log.info(" +++++++++++++++ elapsed 非法 +++++++++++++++ ");//具体长度限制需要开发再确认
        elapsed = 0;
        response  = sygt.SSSingleSafeQueryReply(requestID,bHit,elapsed,metadata,createdOn);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        elapsed = -100;
        response  = sygt.SSSingleSafeQueryReply(requestID,bHit,elapsed,metadata,createdOn);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        elapsed = 5000000;//超过设计超时时间
        response  = sygt.SSSingleSafeQueryReply(requestID,bHit,elapsed,metadata,createdOn);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        log.info(" +++++++++++++++ createdOn 格式非法 +++++++++++++++ ");//具体长度限制需要开发再确认
        createdOn = UtilsClass.Random(512);
        response  = sygt.SSSingleSafeQueryReply(requestID,bHit,elapsed,metadata,createdOn);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        createdOn = "中文";
        response  = sygt.SSSingleSafeQueryReply(requestID,bHit,elapsed,metadata,createdOn);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        createdOn = "2031-03-30 10:00:00";
    }

    @Test
    public void TC20_MultiSafeComplete() throws Exception {
        String requestID = "test";
        String partyA = "test";
        String partyB = "test";
        String replyDigest = "2021-03-30 10:00:00";

        log.info(" +++++++++++++++ 必填字段校验 +++++++++++++++ ");

        requestID = "";
        String response  = sygt.SSMultiSafeQueryComplete(requestID,partyA,partyB,replyDigest);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        requestID = "123";
        partyA = "";
        response  = sygt.SSMultiSafeQueryComplete(requestID,partyA,partyB,replyDigest);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        partyA = "123";
        partyB = "";
        response  = sygt.SSMultiSafeQueryComplete(requestID,partyA,partyB,replyDigest);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        partyB = "123";
        replyDigest = "";
        response  = sygt.SSMultiSafeQueryComplete(requestID,partyA,partyB,replyDigest);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        replyDigest = "摘要信息";

        log.info(" +++++++++++++++ requestID 长度检查 +++++++++++++++ ");//具体长度限制需要开发再确认
        requestID = UtilsClass.Random(512);
        response  = sygt.SSMultiSafeQueryComplete(requestID,partyA,partyB,replyDigest);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        requestID = UtilsClass.Random(63);
        response  = sygt.SSMultiSafeQueryComplete(requestID,partyA,partyB,replyDigest);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        requestID = UtilsClass.Random(64);
        response  = sygt.SSMultiSafeQueryComplete(requestID,partyA,partyB,replyDigest);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        requestID = UtilsClass.Random(65);
        response  = sygt.SSMultiSafeQueryComplete(requestID,partyA,partyB,replyDigest);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        requestID = UtilsClass.Random(6);

        log.info(" +++++++++++++++ partyA 长度检查 +++++++++++++++ ");//具体长度限制需要开发再确认
        partyA = UtilsClass.Random(512);
        response  = sygt.SSMultiSafeQueryComplete(requestID,partyA,partyB,replyDigest);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        partyA = UtilsClass.Random(63);
        response  = sygt.SSMultiSafeQueryComplete(requestID,partyA,partyB,replyDigest);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        partyA = UtilsClass.Random(64);
        response  = sygt.SSMultiSafeQueryComplete(requestID,partyA,partyB,replyDigest);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        partyA = UtilsClass.Random(65);
        response  = sygt.SSMultiSafeQueryComplete(requestID,partyA,partyB,replyDigest);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        partyA = UtilsClass.Random(6);

        log.info(" +++++++++++++++ partyB 长度检查 +++++++++++++++ ");//具体长度限制需要开发再确认
        partyB = UtilsClass.Random(512);
        response  = sygt.SSMultiSafeQueryComplete(requestID,partyA,partyB,replyDigest);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        partyB = UtilsClass.Random(63);
        response  = sygt.SSMultiSafeQueryComplete(requestID,partyA,partyB,replyDigest);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        partyB = UtilsClass.Random(64);
        response  = sygt.SSMultiSafeQueryComplete(requestID,partyA,partyB,replyDigest);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        partyB = UtilsClass.Random(65);
        response  = sygt.SSMultiSafeQueryComplete(requestID,partyA,partyB,replyDigest);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        partyB = UtilsClass.Random(6);

        log.info(" +++++++++++++++ replyDigest 长度检查 +++++++++++++++ ");//具体长度限制需要开发再确认
        replyDigest = UtilsClass.Random(512);
        response  = sygt.SSMultiSafeQueryComplete(requestID,partyA,partyB,replyDigest);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        replyDigest = UtilsClass.Random(63);
        response  = sygt.SSMultiSafeQueryComplete(requestID,partyA,partyB,replyDigest);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        replyDigest = UtilsClass.Random(64);
        response  = sygt.SSMultiSafeQueryComplete(requestID,partyA,partyB,replyDigest);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        replyDigest = UtilsClass.Random(65);
        response  = sygt.SSMultiSafeQueryComplete(requestID,partyA,partyB,replyDigest);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        replyDigest = UtilsClass.Random(6);

    }

    @Test
    public void TC21_MultiSafeReply() throws Exception {
        String requestID = "test";
        Boolean bHit = true;
        int elapsed = 30000;
        String metadata = "test";
        String createdOn = "2021-03-30 10:00:00";

        log.info(" +++++++++++++++ 必填字段校验 +++++++++++++++ ");

        requestID = "";
        String response  = sygt.SSMultiSafeQueryReply(requestID,bHit,elapsed,metadata,createdOn);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        requestID = "123";
        metadata = "";
        response  = sygt.SSMultiSafeQueryReply(requestID,bHit,elapsed,metadata,createdOn);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        metadata = "123";
        createdOn = "";
        response  = sygt.SSMultiSafeQueryReply(requestID,bHit,elapsed,metadata,createdOn);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        createdOn = "2021-03-30 10:00:00";

        log.info(" +++++++++++++++ requestID 长度检查 +++++++++++++++ ");//具体长度限制需要开发再确认
        requestID = UtilsClass.Random(512);
        response  = sygt.SSMultiSafeQueryReply(requestID,bHit,elapsed,metadata,createdOn);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        requestID = UtilsClass.Random(63);
        response  = sygt.SSMultiSafeQueryReply(requestID,bHit,elapsed,metadata,createdOn);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        requestID = UtilsClass.Random(64);
        response  = sygt.SSMultiSafeQueryReply(requestID,bHit,elapsed,metadata,createdOn);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        requestID = UtilsClass.Random(65);
        response  = sygt.SSMultiSafeQueryReply(requestID,bHit,elapsed,metadata,createdOn);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        requestID = UtilsClass.Random(6);

        log.info(" +++++++++++++++ metadata 长度检查 +++++++++++++++ ");//具体长度限制需要开发再确认
        metadata = UtilsClass.Random(512);
        response  = sygt.SSMultiSafeQueryReply(requestID,bHit,elapsed,metadata,createdOn);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        metadata = UtilsClass.Random(63);
        response  = sygt.SSMultiSafeQueryReply(requestID,bHit,elapsed,metadata,createdOn);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        metadata = UtilsClass.Random(64);
        response  = sygt.SSMultiSafeQueryReply(requestID,bHit,elapsed,metadata,createdOn);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        metadata = UtilsClass.Random(65);
        response  = sygt.SSMultiSafeQueryReply(requestID,bHit,elapsed,metadata,createdOn);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        metadata = UtilsClass.Random(6);

        log.info(" +++++++++++++++ elapsed 非法 +++++++++++++++ ");//具体长度限制需要开发再确认
        elapsed = 0;
        response  = sygt.SSMultiSafeQueryReply(requestID,bHit,elapsed,metadata,createdOn);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        elapsed = -100;
        response  = sygt.SSMultiSafeQueryReply(requestID,bHit,elapsed,metadata,createdOn);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        elapsed = 5000000;//超过设计超时时间
        response  = sygt.SSMultiSafeQueryReply(requestID,bHit,elapsed,metadata,createdOn);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        log.info(" +++++++++++++++ createdOn 格式非法 +++++++++++++++ ");//具体长度限制需要开发再确认
        createdOn = UtilsClass.Random(512);
        response  = sygt.SSMultiSafeQueryReply(requestID,bHit,elapsed,metadata,createdOn);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        createdOn = "中文";
        response  = sygt.SSMultiSafeQueryReply(requestID,bHit,elapsed,metadata,createdOn);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals(true,response.contains("error"));

        createdOn = "2031-03-30 10:00:00";
    }

    @Test
    public void TC22_SafequeryDo() throws Exception {
        String scene = "";
        String label = "";
        Map inputs = new HashMap<>();

        log.info(" +++++++++++++++ 必填字段校验 +++++++++++++++ ");
        inputs.clear();
        String response  = sygt.SSComplexSafetyQuery(scene,label,inputs);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        inputs.put("11","");//一笔空值
        response  = sygt.SSComplexSafetyQuery(scene,label,inputs);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));


        inputs.put("22","3666");  inputs.put("22","");//多笔空值
        response  = sygt.SSComplexSafetyQuery(scene,label,inputs);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

        inputs.clear();
        inputs.put("22",3666);  //类型错误
        response  = sygt.SSComplexSafetyQuery(scene,label,inputs);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("error"));

    }
}
