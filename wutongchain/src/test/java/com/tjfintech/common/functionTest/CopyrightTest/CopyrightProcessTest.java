package com.tjfintech.common.functionTest.CopyrightTest;

import com.alibaba.fastjson.JSON;
import com.gmsm.utils.GmUtils;
import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CertTool;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.Copyright;
import com.tjfintech.common.Interface.Kms;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.functionTest.Tap.TapCommonFunc;
import com.tjfintech.common.functionTest.sendMessage.CallBack;
import com.tjfintech.common.utils.FileOperation;
import com.tjfintech.common.utils.UtilsClass;
import com.tjfintech.common.utils.UtilsClassCopyright;
import com.tjfoc.base.Util;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;

import static com.tjfintech.common.utils.UtilsClass.*;

import static com.tjfintech.common.utils.UtilsClassCopyright.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class CopyrightProcessTest {

    TestBuilder testBuilder = TestBuilder.getInstance();
    Copyright copyright = testBuilder.getCopyright();
    CertTool certTool = new CertTool();
    Store store = testBuilder.getStore();
    Kms kms = testBuilder.getKms();
    CommonFunc commonFunc = new CommonFunc();
    UtilsClass utilsClass = new UtilsClass();
    CopyrightCommonFunc copyrightCommonFunc = new CopyrightCommonFunc();


    @BeforeClass
    public static void init() throws Exception {

        CopyrightCommonFunc copyrightCommonFunc = new CopyrightCommonFunc();
//        copyrightCommonFunc.initDetailInfo();
        copyrightCommonFunc.initAccountInfo();
        copyrightCommonFunc.initArtSmartContract();

    }

    @Test
    public void crInterfaceTest() throws Exception {

        String response;

        //艺术品发行接口,账户KeyID为空
        response = copyright.crArtworkIssue("", SCADDRESS1, ARTWORKID, ARTHASH);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("'KeyId' failed on the 'required'"));

        //艺术品发行接口,合约地址scAddress为空
        response = copyright.crArtworkIssue(BROKERKEYID1, "", ARTWORKID, ARTHASH);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("'ScAddress' failed on the 'required'"));

        //艺术品发行接口,艺术品artworkId为空
        response = copyright.crArtworkIssue(BROKERKEYID1, SCADDRESS1, "", ARTHASH);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("'ArtWorkId' failed on the 'required'"));

        //艺术品发行接口,艺术品描述信息哈希artHash为空
        response = copyright.crArtworkIssue(BROKERKEYID1, SCADDRESS1, ARTWORKID, "");
        assertEquals("400", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("'ArtHash' failed on the 'required'"));

        //艺术品发行接口,账户KeyID为异常数据
        response = copyright.crArtworkIssue("123", SCADDRESS1, ARTWORKID, ARTHASH);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("invoke kms  server  failed"));

        //艺术品发行接口,合约地址scAddress为异常数据
        response = copyright.crArtworkIssue(BROKERKEYID1, "123456", ARTWORKID, ARTHASH);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("Smart contract does not exist"));

        //艺术品交易历史接口，艺术品artworkId为空
        response = copyright.crArtworkHistory("");
        assertEquals("400", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("艺术品编号不能为空"));

        //艺术品交易历史接口，艺术品artworkId为未发行的数据
        response = copyright.crArtworkHistory("123");
        assertEquals("400", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("The 123 has not been on the sale"));

        //艺术品系列合约初始化接口，艺术品系列名称symbol为空
        response = copyright.crArtworkScinit("", MAX, BROKERADDRESS1, TYPENO, BASEURL);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("'Symbol' failed on the 'required' tag"));

        //艺术品系列合约初始化接口，最大发行数量max为空
        response = copyright.crArtworkScinit(SYMBOL, 0, BROKERADDRESS1, TYPENO, BASEURL);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("'Max' failed on the 'gt' tag"));

        //艺术品系列合约初始化接口，合约拥有地址owner为空
        response = copyright.crArtworkScinit(SYMBOL, MAX, "", TYPENO, BASEURL);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("'Owner' failed on the 'required' tag"));

        //艺术品系列合约初始化接口，种类数量typeNo为空
        response = copyright.crArtworkScinit(SYMBOL, MAX, BROKERADDRESS1, 0, BASEURL);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("'TypeNo' failed on the 'gt' tag"));

        //艺术品系列合约初始化接口，艺术品系统的baseUrl为空
        response = copyright.crArtworkScinit(SYMBOL, MAX, BROKERADDRESS1, TYPENO, "");
        assertEquals("400", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("'BaseUrl' failed on the 'required' tag"));

//        //艺术品系列合约初始化接口，合约拥有地址owner为不存在的地址数据
//        response = copyright.crArtworkScinit(SYMBOL,MAX,"123",TYPENO,BASEURL);
//        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //获取艺术品URL接口，合约地址scAddress为空
        response = copyright.crArtworkUrlQuery("", "");
        assertEquals("400", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("Invalid parameter"));

        //艺术品状态更新接口，合约地址scAddress为空
        response = copyright.crArtworkScupdate("", ARTWORKID, constructUnixTime(0), true);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("'ScAddress' failed on the 'required' tag"));

        //艺术品状态更新接口，艺术品编号artworkId为空
        response = copyright.crArtworkScupdate(SCADDRESS1, "", constructUnixTime(0), true);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("'ArtworkId' failed on the 'required' tag"));

        //艺术品状态更新接口，time为空
        response = copyright.crArtworkScupdate(SCADDRESS1, ARTWORKID, 0, true);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("'TimeStamp' failed on the 'required' tag"));

        //艺术品状态更新接口，合约地址scAddress为不存在的数据
        response = copyright.crArtworkScupdate("123", ARTWORKID, constructUnixTime(0), true);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("Smart contract does not exist"));

//        //艺术品状态更新接口，艺术品编号artworkId为不存在的数据
//        response = copyright.crArtworkScupdate(SCADDRESS1,"123",constructUnixTime(0),true);
//        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //账户注册接口，keyId为空
        response = copyright.crAcountRegister("", "user");
        assertEquals("400", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("'KeyId' failed on the 'required' tag"));

        //账户注册接口，type为空
        response = kms.createKey("sm2", "PIN1");
        String keyid = JSONObject.fromObject(response).getJSONObject("data").getString("keyId");
        response = copyright.crAcountRegister(keyid, "");
        assertEquals("400", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains(" 'Type' failed on the 'oneof' tag"));

        //账户注册接口，type字段传入platform/broker/user之外的参数
        response = copyright.crAcountRegister(keyid, "123");
        assertEquals("400", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains(" 'Type' failed on the 'oneof' tag"));

        //账户艺术品查询接口，addr为空
        response = copyright.crAccountQuery("", SCADDRESS1);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("addr cannot empty"));

        //账户艺术品查询接口，合约地址scAddress为空
        response = copyright.crAccountQuery(USERADDRESS1, "");
        assertEquals("400", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("addr cannot empty"));

//        //账户艺术品查询接口，addr为不存在的数据
//        response = copyright.crAccountQuery("123",SCADDRESS1);
//        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //账户艺术品查询接口，合约地址scAddress为不存在的数据
        response = copyright.crAccountQuery(USERADDRESS1, "123");
        assertEquals("500", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains(" Smart contract does not exist"));

        //（小程序）订单预支付接口,商户内部订单号outTradeNo为空
        response = copyright.crPayPrepartion("", "数字藏品", OPENID, 1, "", "",
                false, "", "", constructUnixTime(100), ATTACH);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("'OutTradeNo' failed on the 'required' tag"));

        //（小程序）订单预支付接口,商品描述desc为空
        response = copyright.crPayPrepartion(String.valueOf(constructUnixTime(0)), "", OPENID, 1, "", "",
                false, "", "", constructUnixTime(100), ATTACH);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("'Desc' failed on the 'required' tag"));

        //（小程序）订单预支付接口,小程序的唯一标识openId为空
        response = copyright.crPayPrepartion
                (String.valueOf(constructUnixTime(0)), "数字藏品", "", 1, "", "",
                        false, "", "", constructUnixTime(100), ATTACH);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("openId is required"));

        //（小程序）订单预支付接口,订单总金额total为空
        response = copyright.crPayPrepartion
                (String.valueOf(constructUnixTime(0)), "数字藏品", OPENID, 0, "", "",
                        false, "", "", constructUnixTime(100), ATTACH);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("'Total' failed on the 'required' tag"));

        //（小程序）订单预支付接口,小程序的唯一标识openId为错误不存在数据
        response = copyright.crPayPrepartion
                (String.valueOf(constructUnixTime(0)), "数字藏品", "123", 1, "", "",
                        false, "", "", constructUnixTime(100), ATTACH);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("无效的openid"));

        //（H5）订单预支付接口,商户内部订单号outTradeNo为空
        response = copyright.crPayPrepartionH5("", "数字藏品", "Wap",
                1, "", "", false, "10.1.1.1", "", constructUnixTime(100), ATTACH);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("'OutTradeNo' failed on the 'required' tag"));

        //（H5）订单预支付接口,商品描述desc为空
        response = copyright.crPayPrepartionH5((String.valueOf(constructUnixTime(0))), "", "Wap",
                1, "", "", false, "10.1.1.1", "", constructUnixTime(100), ATTACH);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("'Desc' failed on the 'required' tag"));

        //（H5）订单预支付接口,场景类型type为空
        response = copyright.crPayPrepartionH5((String.valueOf(constructUnixTime(0))), "数字藏品", "",
                1, "", "", false, "10.1.1.1", "", constructUnixTime(100), ATTACH);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("'Type' failed on the 'required' tag"));

        //（H5）订单预支付接口,订单总金额total为空
        response = copyright.crPayPrepartionH5((String.valueOf(constructUnixTime(0))), "数字藏品", "Wap",
                0, "", "", false, "10.1.1.1", "", constructUnixTime(100), ATTACH);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("'Total' failed on the 'required' tag"));

        //（H5）订单预支付接口,用户终端IP为空
        response = copyright.crPayPrepartionH5((String.valueOf(constructUnixTime(0))), "数字藏品", "Wap",
                1, "", "", false, "", "", constructUnixTime(100), ATTACH);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("clientIp is required"));

        //（小程序）订单取消接口,商户内部订单号outTradeNo为空
        response = copyright.crOrderClose("", "取消支付");
        assertEquals("400", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("'Order' failed on the 'required' tag"));

        //（H5）订单取消接口,商户内部订单号outTradeNo为空
        response = copyright.crOrderCloseH5("", "取消支付");
        assertEquals("400", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains("'Order' failed on the 'required' tag"));

        //（小程序）订单查询接口,订单号order为空
        response = copyright.crOrderQuery("", "");
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //（H5）订单查询接口,订单号order为空
        response = copyright.crOrderQueryH5("", "");
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

//        //生成艺术品证书接口,证书编号certId为空
//        response = copyright.crArtworkCertPicture("","证书","艺术品","123",
//                constructUnixTime(0),"hash","e10adc3949ba59abbe56e057f20f883e");
//        assertEquals("400", JSONObject.fromObject(response).getString("state"));
//
//        //生成艺术品证书接口,文件名称name为空
//        response = copyright.crArtworkCertPicture(String.valueOf(constructUnixTime(0)),"","艺术品","123",
//                constructUnixTime(0),"hash","e10adc3949ba59abbe56e057f20f883e");
//        assertEquals("400", JSONObject.fromObject(response).getString("state"));
//
//        //生成艺术品证书接口,艺术品名称artworkName为空
//        response = copyright.crArtworkCertPicture(String.valueOf(constructUnixTime(0)),"证书","","123",
//                constructUnixTime(0),"hash","e10adc3949ba59abbe56e057f20f883e");
//        assertEquals("400", JSONObject.fromObject(response).getString("state"));
//
//        //生成艺术品证书接口,用户地址address为空
//        response = copyright.crArtworkCertPicture(String.valueOf(constructUnixTime(0)),"证书","艺术品","123",
//                constructUnixTime(0),"hash","e10adc3949ba59abbe56e057f20f883e");
//        assertEquals("400", JSONObject.fromObject(response).getString("state"));
//
//        //生成艺术品证书接口,上链成功时间time为空
//        response = copyright.crArtworkCertPicture(String.valueOf(constructUnixTime(0)),"证书","艺术品","123",
//                0,"hash","e10adc3949ba59abbe56e057f20f883e");
//        assertEquals("400", JSONObject.fromObject(response).getString("state"));
//
//        //生成艺术品证书接口,艺术品哈希artHash为空
//        response = copyright.crArtworkCertPicture(String.valueOf(constructUnixTime(0)),"证书","艺术品","123",
//                constructUnixTime(0),"","e10adc3949ba59abbe56e057f20f883e");
//        assertEquals("400", JSONObject.fromObject(response).getString("state"));
//
//        //生成艺术品证书接口,艺术品审核哈希authHash为空
//        response = copyright.crArtworkCertPicture(String.valueOf(constructUnixTime(0)),"证书","艺术品","123",
//                constructUnixTime(0),"hash","");
//        assertEquals("400", JSONObject.fromObject(response).getString("state"));

    }

    /**
     * 正常流程测试
     * 初始化用户注册（经纪商1\2\3,普通用户1\2\3）,经纪商1初始化系列和变更艺术品状态
     * 用户1账户艺术品查询为空，艺术品审核信息存储，
     * 艺术品A流转（经纪商1->普通用户1），账户艺术品查询、交易历史查询、获取艺术品url
     * 订单预支付、订单取消、订单查询
     * 生成艺术品证书
     */

    @Test
    public void crProcessTest() throws Exception {

        //查询用户1账户艺术品为空
        String artworkId = String.valueOf(constructUnixTime(0));
        log.info("发行艺术品编码：" + artworkId);
        copyrightCommonFunc.verifyAccountQuery(USERADDRESS1, SCADDRESS1, artworkId, SYMBOL, false);

        //艺术品审核信息存储
        String response = copyright.crOrderStore(constructData("SHBH", 8), artworkId, "上技所", "yyds",
                constructTime(0), "通过", "文本", "上技所审核", "一口价", 1000, 1000);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        String storeHash = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        commonFunc.verifyTxDetailField(storeHash, "store", "0", "0", "0");

        //艺术品状态更新
        response = copyright.crArtworkScupdate(SCADDRESS1, artworkId, constructUnixTime(0), true);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        sleepAndSaveInfo(2000);
        copyrightCommonFunc.verifyArtworkHistory(artworkId, USERADDRESS1, storeHash);

        //艺术品发行上链
        response = copyright.crArtworkIssue(USERKEYID1, SCADDRESS1, artworkId, ARTHASH);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        storeHash = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        commonFunc.verifyTxDetailField(storeHash, "wvm_invoke", "2", "3", "42");

        //查询用户1账户艺术品,返回正确数据
        copyrightCommonFunc.verifyAccountQuery(USERADDRESS1, SCADDRESS1, artworkId, SYMBOL, true);

        //艺术品流转历史
        copyrightCommonFunc.verifyArtworkHistory(artworkId, USERADDRESS1, storeHash);

        //订单预支付接口(小程序)
        String outTradeNo = String.valueOf(constructUnixTime(0));
        response = copyright.crPayPrepartion
                (outTradeNo, "数字藏品", OPENID, 1, "", "",
                        false, "", "", constructUnixTime(100), ATTACH);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));

        //取消订单接口（小程序）
        response = copyright.crOrderClose(outTradeNo, "取消支付");
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        storeHash = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        commonFunc.verifyTxDetailField(storeHash, "store", "0", "0", "0");

        //订单预支付接口(H5)
        outTradeNo = String.valueOf(constructUnixTime(0));
        response = copyright.crPayPrepartionH5(outTradeNo, "数字藏品", "WAP", 1, "", "", false,
                "10.1.1.1", "", constructUnixTime(0), ATTACH);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));

        //取消订单接口（H5）
        response = copyright.crOrderCloseH5(outTradeNo, "取消支付");
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        storeHash = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        commonFunc.verifyTxDetailField(storeHash, "store", "0", "0", "0");

        //生成艺术品证书接
        response = copyright.crArtworkCertPicture(String.valueOf(constructUnixTime(0)), "证书", "艺术品", "123",
                constructUnixTime(0), "hash", "hash");
        assertEquals("200", JSONObject.fromObject(response).getString("state"));

    }

    /**
     * 正常流程测试
     * 经纪商1发行系列1/2，每个系列发行艺术品1/2到普通用户1/2/3
     * 艺术品交易历史查询，账户艺术品查询
     */

    @Test
    public void crIssueMultiTest() throws Exception {

        String symbol1 = constructData("SYMBOL1-", 8);
        String symbol2 = constructData("SYMBOL2-", 8);
        log.info("@@@@@@symbol1:" + symbol1);
        log.info("@@@@@@symbol2:" + symbol2);

        String artworkId11 = String.valueOf(constructUnixTime(0));
        log.info("@@@@@@artworkId11:" + artworkId11);

        //艺术品系列初始化
        String response = copyright.crArtworkScinit(symbol1, MAX, BROKERADDRESS1, TYPENO, BASEURL);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        String scAddress1 = JSONObject.fromObject(response).getJSONObject("data").getString("name");
        log.info("@@@@@@scAddress1:" + scAddress1);

        response = copyright.crArtworkScinit(symbol2, MAX, BROKERADDRESS1, TYPENO, BASEURL);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        String scAddress2 = JSONObject.fromObject(response).getJSONObject("data").getString("name");
        log.info("@@@@@@scAddress2:" + scAddress2);

        //艺术品状态更新
        response = copyright.crArtworkScupdate(scAddress1, artworkId11, constructUnixTime(0), true);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));

        String artworkId12 = String.valueOf(constructUnixTime(0));
        log.info("@@@@@@artworkId12:" + artworkId12);
        response = copyright.crArtworkScupdate(scAddress1, artworkId12, constructUnixTime(0), true);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        sleepAndSaveInfo(2000);

        String artworkId21 = String.valueOf(constructUnixTime(0));
        log.info("@@@@@@artworkId21:" + artworkId21);
        response = copyright.crArtworkScupdate(scAddress2, artworkId21, constructUnixTime(0), true);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        sleepAndSaveInfo(2000);

        String artworkId22 = String.valueOf(constructUnixTime(0));
        log.info("@@@@@@artworkId22:" + artworkId22);
        response = copyright.crArtworkScupdate(scAddress2, artworkId22, constructUnixTime(0), true);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        sleepAndSaveInfo(2000);

        //艺术品发行上链
        response = copyright.crArtworkIssue(USERKEYID1, scAddress1, artworkId11, ARTHASH);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        String txId111 = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        response = copyright.crArtworkIssue(USERKEYID1, scAddress1, artworkId12, ARTHASH);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        String txId121 = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        response = copyright.crArtworkIssue(USERKEYID1, scAddress2, artworkId21, ARTHASH);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        String txId211 = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        response = copyright.crArtworkIssue(USERKEYID1, scAddress2, artworkId22, ARTHASH);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        String txId221 = JSONObject.fromObject(response).getJSONObject("data").getString("txId");


        response = copyright.crArtworkIssue(USERKEYID2, scAddress1, artworkId11, ARTHASH);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        String txId112 = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        response = copyright.crArtworkIssue(USERKEYID2, scAddress1, artworkId12, ARTHASH);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        String txId122 = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        response = copyright.crArtworkIssue(USERKEYID2, scAddress2, artworkId21, ARTHASH);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        String txId212 = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        response = copyright.crArtworkIssue(USERKEYID2, scAddress2, artworkId22, ARTHASH);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        String txId222 = JSONObject.fromObject(response).getJSONObject("data").getString("txId");

        //查询用户1账户艺术品,返回正确数据
        copyrightCommonFunc.verifyAccountQuery(USERADDRESS1, scAddress1, artworkId11, symbol1, true);
        copyrightCommonFunc.verifyAccountQuery(USERADDRESS1, scAddress1, artworkId12, symbol1, true);
        copyrightCommonFunc.verifyAccountQuery(USERADDRESS1, scAddress2, artworkId21, symbol2, true);
        copyrightCommonFunc.verifyAccountQuery(USERADDRESS1, scAddress2, artworkId22, symbol2, true);

        //艺术品流转历史
        copyrightCommonFunc.verifyArtworkHistory(artworkId11, USERADDRESS1, txId111);
        copyrightCommonFunc.verifyArtworkHistory(artworkId12, USERADDRESS1, txId121);
        copyrightCommonFunc.verifyArtworkHistory(artworkId21, USERADDRESS1, txId211);
        copyrightCommonFunc.verifyArtworkHistory(artworkId22, USERADDRESS1, txId221);

        copyrightCommonFunc.verifyArtworkHistory(artworkId11, USERADDRESS1, txId112);
        copyrightCommonFunc.verifyArtworkHistory(artworkId12, USERADDRESS1, txId122);
        copyrightCommonFunc.verifyArtworkHistory(artworkId21, USERADDRESS1, txId212);
        copyrightCommonFunc.verifyArtworkHistory(artworkId22, USERADDRESS1, txId222);
    }

    /**
     * 异常流程测试-艺术品系列合约初始化max为10，typeno为3
     * 艺术品状态更新，第4种失败
     * 发行艺术品1，循环发给用户1用户2至库存（10）结束，转给用户3失败
     */
    @Test
    public void crArtworkIssueInvalidTest() throws Exception {

        //艺术品系列初始化
        int max = 10;
        int typeno = 3;
        String symbol1 = constructData("SYMBOL1-", 8);
        String artworkId1 = String.valueOf(constructUnixTime(1));
        String artworkId2 = String.valueOf(constructUnixTime(2));
        String artworkId3 = String.valueOf(constructUnixTime(3));
        String artworkId4 = String.valueOf(constructUnixTime(4));
        String response = copyright.crArtworkScinit(symbol1, max, BROKERADDRESS1, typeno, BASEURL);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        String scAddress = JSONObject.fromObject(response).getJSONObject("data").getString("name");
        log.info(scAddress + "@@@" + symbol1 + "@@@" + artworkId1 + "@@@" + artworkId2 + "@@@" + artworkId3 + "@@@" + artworkId4);

        //艺术品状态更新
        response = copyright.crArtworkScupdate(scAddress, artworkId1, constructUnixTime(0), true);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        response = copyright.crArtworkScupdate(scAddress, artworkId2, constructUnixTime(0), true);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        response = copyright.crArtworkScupdate(scAddress, artworkId3, constructUnixTime(0), true);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        response = copyright.crArtworkScupdate(scAddress, artworkId4, constructUnixTime(0), true);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));

        //艺术品发行上链，发行用户1，用户2成功，发行用户3失败
        for (int i = 0; i < max / 2; i++) {
            response = copyright.crArtworkIssue(USERKEYID1, scAddress, artworkId1, ARTHASH);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                    utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

            response = copyright.crArtworkIssue(USERKEYID2, scAddress, artworkId1, ARTHASH);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                    utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        }
        response = copyright.crArtworkIssue(USERKEYID3, scAddress, artworkId1, ARTHASH);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));

        //艺术品发行上链，发行艺术品种类1/2/3成功，4失败
        response = copyright.crArtworkIssue(USERKEYID1, scAddress, artworkId2, ARTHASH);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        response = copyright.crArtworkIssue(USERKEYID2, scAddress, artworkId3, ARTHASH);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        response = copyright.crArtworkIssue(USERKEYID2, scAddress, artworkId4, ARTHASH);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));

    }


    /**
     * 异常流程测试-账户注册接口
     * 相同的keyid重复注册接口
     */
    @Test
    public void crDuplicateAccountRegisterTest() throws Exception {

        String response = copyright.crAcountRegister(BROKERKEYID1, "broker");
        assertEquals("500", JSONObject.fromObject(response).getString("state"));

        response = copyright.crAcountRegister(USERKEYID1, "user");
        assertEquals("500", JSONObject.fromObject(response).getString("state"));

    }

    /**
     * 临时测试-账户注册接口
     * 批量生成账户
     */
    @Test
    public void crAccountRegisterTest() throws Exception {

        CallBack callBack = new CallBack();
        FileOperation fo = new FileOperation();

        String msgdatafile = System.getProperty("user.dir") + "\\callBackData.txt";
        callBack.clearMsgDateForFile(msgdatafile);

//        for (int i = 0; i < 10000; i++) {
//            String response = kms.createKey("sm2", "PIN1");
//            String keyid = JSONObject.fromObject(response).getJSONObject("data").getString("keyId");
//            response = copyright.crAcountRegister(keyid, "user", USERDETAILINFO);
//            String address = JSONObject.fromObject(response).getJSONObject("data").getString("address");
//            log.info("普通用户1：" + keyid + "地址：" + address);
//
//            File destFile = new File(msgdatafile);
//            fo.appendToFile(address, msgdatafile);
//        }

//        //艺术品发行上链
//        for (int i = 0; i < 10; i++) {
//            String bianhao = constructData("YSPBH_", 8);
//            int shuliang = 10000;
//
//            copyrightCommonFunc.initArtIssue(BROKERKEYID1, bianhao, shuliang);
//            copyrightCommonFunc.verifyAccountQuery(brokerAddress1, bianhao, shuliang, true);
//            copyrightCommonFunc.verifyArtworkQuery(brokerAddress1, bianhao, "", shuliang, true, false);
//            log.info(i + "*********" + "发行艺术品编号：" + bianhao + "数量：" + shuliang);
//
//            fo.appendToFile(bianhao, msgdatafile);
//        }

    }


}