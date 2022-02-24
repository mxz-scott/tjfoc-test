package com.tjfintech.common.functionTest.ShcaTest;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.Shca;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import com.tjfintech.common.utils.UtilsClassShca;
import lombok.extern.slf4j.Slf4j;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.SLEEPTIME;
import static com.tjfintech.common.utils.UtilsClassShca.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;



@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class ShcaTest {
    TestBuilder testBuilder = TestBuilder.getInstance();
    Shca shca = testBuilder.getShca();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    Store store = testBuilder.getStore();

    /*
    获取所有DID文档信息
     */
    @Test
    public void Test001_didlist () {
        String response1 = shca.DIDlist();
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("success"));
        assertThat(response1, containsString("data"));
    }

    /*
    根据ID获取DID信息
     */
    @Test
    public void Test002_didget() {
        String response1 = shca.DIDget();
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("success"));
        assertThat(response1, containsString("data"));
    }

    /*
    根据ID删除DID
     */
    @Test
    public void Test003_DIDdelete() {
        String response1 = shca.DIDdelete();
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("success"));
        assertThat(response1, containsString("data"));
    }

    /*
    新增DID文档，且查询上链信息
     */
    @Test
    public void Test004_didadd() throws Exception{
        String response1 = shca.DIDadd(didJson,id);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("success"));
        assertThat(response1, containsString("data"));
        Thread.sleep(5000);
        String data = getdata(response1);
        System.out.println("data = " + data);
        String checking = store.GetTxDetail(data);
        assertThat(checking, containsString("200"));
        assertThat(checking, containsString("success"));

    }

    /*
    根据ID获取VC信息
     */
    @Test
    public void Test005_VCget() {
        String response1 = shca.VCget();
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("success"));
        assertThat(response1, containsString("data"));
    }

    /*
    新增VC
     */
    @Test
    public void Test006_VCadd() {
        String response1 = shca.VCadd(vcId, vcJson);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("success"));
        assertThat(response1, containsString("data"));
    }

    /*
    创建VC
     */
    @Test
    public void Test007_VC() {
        Map vcSubject = UtilsClassShca.vcSubject("duyuyang");
        String response1 = shca.VC(applicantDid, vcType, vcSubject);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("success"));
        assertThat(response1, containsString("data"));
    }

    /*
    根据id删除vc
     */
    @Test
    public void Test008_VCdelete() {
        String response1 = shca.VCdelete();
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("success"));
        assertThat(response1, containsString("data"));
    }

    /*
    DID初始化
     */
    @Test
    public void Test009_DIDinit() {
        String response1 = shca.DIDinit(password, pubKey, publicKeyId);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("success"));
        assertThat(response1, containsString("data"));
    }

    /*
    DID创建
     */
    @Test
    public void Test0010_DID() {
        String response1 = shca.DID(publicKeyId, pubKey);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("success"));
        assertThat(response1, containsString("data"));
    }

    /*
    DID解析
     */
    @Test
    public void Test0011_DIDid() {
        String response1 = shca.DIDid();
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("success"));
        assertThat(response1, containsString("data"));
    }
}