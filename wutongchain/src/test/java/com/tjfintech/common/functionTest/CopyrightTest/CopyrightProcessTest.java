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

import java.net.URLEncoder;

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

    String fromAddress = brokerAddress1;
    String toAddress = userAddress1;

    @BeforeClass
    public static void init() throws Exception {

        CopyrightCommonFunc copyrightCommonFunc = new CopyrightCommonFunc();
        copyrightCommonFunc.initDetailInfo();
        copyrightCommonFunc.initAccountInfo();

    }

    @Test
    public void crInterfaceTest() throws Exception {

        YSPBH = constructData("YSPBH_", 8);
        log.info("艺术品编码：" + YSPBH);
        artDetailInfo = copyrightCommonFunc.initArtDetailInfo();
        orderDetailInfo = copyrightCommonFunc.initOrderDetailInfo();
        //艺术品发行接口,账户KeyID为空
        String response = copyright.crArtworkIssue("", PIN1, artDetailInfo);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //艺术品发行接口,pin为空
//        response = copyright.crArtworkIssue(USERKEYID1, "", artDetailInfo);
//        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //艺术品发行接口,detail为空
        response = copyright.crArtworkIssue(USERKEYID1, PIN1, null);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //艺术品发行接口,账户KeyID为不存在的数据
        response = copyright.crArtworkIssue("123", PIN1, artDetailInfo);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //艺术品发行接口,detail为异常格式数据
        response = copyright.crArtworkIssue(USERKEYID1, PIN1, "123");
        assertEquals("500", JSONObject.fromObject(response).getString("state"));

        //艺术品发行接口,发行YSPBH
        response = copyright.crArtworkIssue(USERKEYID1, PIN1, artDetailInfo);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));

        sleepAndSaveInfo(2000);

        //艺术品发行接口,重复发行YSPBH
        response = copyright.crArtworkIssue(USERKEYID1, PIN1, artDetailInfo);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));

        //艺术品流转接口,keyId为空
        response = copyright.crArtworkTransfer("", PIN1, fromAddress, toAddress, orderDetailInfo);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));

        //艺术品流转接口,keyId为不存在的数据
        response = copyright.crArtworkTransfer("123", PIN1, fromAddress, toAddress, orderDetailInfo);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));

        //艺术品流转接口,pin为空
//        response = copyright.crArtworkTransfer(BROKERKEYID1, "", fromAddress, toAddress, orderDetailInfo);
//        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //艺术品流转接口,fromAddress为空
        response = copyright.crArtworkTransfer(BROKERKEYID1, PIN1, "", toAddress, orderDetailInfo);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //艺术品流转接口,fromAddress为不存在的数据
        response = copyright.crArtworkTransfer(BROKERKEYID1, PIN1, AddrNotInDB, toAddress, orderDetailInfo);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));

        //艺术品流转接口,fromAddress和keyid不匹配
        response = copyright.crArtworkTransfer(BROKERKEYID2, PIN1, fromAddress, toAddress, orderDetailInfo);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));

        //艺术品流转接口,toAddress为空
        response = copyright.crArtworkTransfer(BROKERKEYID1, PIN1, fromAddress, "", orderDetailInfo);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //艺术品流转接口,toAddress为不存在的数据
        response = copyright.crArtworkTransfer(BROKERKEYID1, PIN1, fromAddress, AddrNotInDB, orderDetailInfo);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));

        //艺术品流转接口,fromAddress和toAddress相同
        response = copyright.crArtworkTransfer(BROKERKEYID1, PIN1, fromAddress, fromAddress, orderDetailInfo);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));

        //艺术品流转接口,YSPBH未发行
        String temp = UtilsClassCopyright.YSPBH;
        YSPBH = constructData("YSPBH_", 8);
        log.info("未发行艺术品编码：" + YSPBH);
        orderDetailInfo = copyrightCommonFunc.initOrderDetailInfo();
        response = copyright.crArtworkTransfer(BROKERKEYID1, PIN1, fromAddress, toAddress, orderDetailInfo);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));

        YSPBH = temp;
        log.info("原艺术品编码：" + YSPBH);
        orderDetailInfo = copyrightCommonFunc.initOrderDetailInfo();

        //艺术品流转接口,detail为空
        response = copyright.crArtworkTransfer(BROKERKEYID1, PIN1, fromAddress, toAddress, null);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //艺术品流转接口,detail为异常格式数据
        response = copyright.crArtworkTransfer(BROKERKEYID1, PIN1, fromAddress, toAddress, "123");
        assertEquals("500", JSONObject.fromObject(response).getString("state"));

        //查询艺术品信息接口，bianhao为空
        response = copyright.crArtworkQuery("", "");
        assertEquals("404", JSONObject.fromObject(response).getString("state"));

        //查询艺术品信息接口，bianhao为不存在的数据
        response = copyright.crArtworkQuery("123", "");
        assertEquals("404", JSONObject.fromObject(response).getString("state"));

        //查询艺术品信息接口，num为不存在的数据
        response = copyright.crArtworkQuery(YSPBH, "9999");
        assertEquals("404", JSONObject.fromObject(response).getString("state"));

        //账户注册接口，keyId为空
        response = copyright.crAcountRegister("", "user", userDetailInfo);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //账户注册接口，type为空
        response = kms.createKey("sm2", "PIN1");
        String keyid = JSONObject.fromObject(response).getJSONObject("data").getString("keyId");
        response = copyright.crAcountRegister(keyid, "", userDetailInfo);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //账户注册接口，type字段传入platform/broker/user之外的参数
        response = copyright.crAcountRegister(keyid, "123", null);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //账户艺术品查询接口，addr为空
        response = copyright.crAccountQuery("");
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //账户艺术品查询接口，addr为不存在的数据
        response = copyright.crAccountQuery(AddrNotInDB);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //订单预支付接口,商户内部订单号outTradeNo为空
        response = copyright.crPayPrepartion("", "数字藏品", OPENID, 1, "", "",
                false, "", "", constructUnixTime(100), "");
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //订单预支付接口,商品描述desc为空
        response = copyright.crPayPrepartion(String.valueOf(constructUnixTime(0)), "", OPENID, 1, "", "",
                false, "", "", constructUnixTime(100), "");
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //订单预支付接口,小程序的唯一标识openId为空
        response = copyright.crPayPrepartion
                (String.valueOf(constructUnixTime(0)), "数字藏品", "", 1, "", "",
                        false, "", "", constructUnixTime(100), "");
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //订单预支付接口,订单总金额total为空
        response = copyright.crPayPrepartion
                (String.valueOf(constructUnixTime(0)), "数字藏品", OPENID, 0, "", "",
                        false, "", "", constructUnixTime(100), "");
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //订单预支付接口,小程序的唯一标识openId为错误不存在数据
        response = copyright.crPayPrepartion
                (String.valueOf(constructUnixTime(0)), "数字藏品", "123", 1, "", "",
                        false, "", "", constructUnixTime(100), "");
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

        //订单预支付接口,商户内部订单号重复下单
        String outTradeNo = String.valueOf(constructUnixTime(0));
        response = copyright.crPayPrepartion
                (outTradeNo, "数字藏品", OPENID, 1, "", "",
                        false, "", "", constructUnixTime(100), "");
        assertEquals("200", JSONObject.fromObject(response).getString("state"));

        //订单取消接口,商户内部订单号outTradeNo为空
        response = copyright.crOrderClose("", "取消支付");
        assertEquals("400", JSONObject.fromObject(response).getString("state"));

    }

    /**
     * 正常流程测试
     * 用户注册（经纪商1\2\3,普通用户1\2\3）,账户艺术品查询均为空
     * 艺术品审核信息存储，经纪商1发行艺术品A，账户艺术品查询有数据，查询艺术品信息
     * 艺术品A流转（经纪商1->普通用户1），账户艺术品查询（经纪商1无数据，普通用户1有数据），
     */

    @Test
    public void crProcessTest() throws Exception {

        //查询经纪商1账户艺术品为空
        YSPBH = constructData("YSPBH_", 8);
        ShuLiang = 100;
        log.info("发行艺术品编码：" + YSPBH + "数量为：" + ShuLiang);
        artDetailInfo = copyrightCommonFunc.initArtDetailInfo();
        copyrightCommonFunc.verifyAccountQuery(brokerAddress1, YSPBH, 0, true);

        //艺术品审核信息存储
        String response = copyright.crOrderStore(constructData("SHBH", 8), YSPBH, "上技所", "yyds", constructTime(0),
                "通过", "文本", "上技所审核", "一口价", 1000, 1000);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        String storeHash = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        commonFunc.verifyTxDetailField(storeHash, "store", "0", "0", "0");

        //艺术品发行上链
        response = copyright.crArtworkIssue(BROKERKEYID1, PIN1, artDetailInfo);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        storeHash = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        commonFunc.verifyTxDetailField(storeHash, "wvm_invoke", "2", "3", "42");

        //查询经纪商1账户艺术品,返回正确数据
        copyrightCommonFunc.verifyAccountQuery(brokerAddress1, YSPBH, 100, true);

        //查询艺术品信息接口，num为空，返回艺术品发行的数据
        copyrightCommonFunc.verifyArtworkQuery(brokerAddress1, YSPBH, "", ShuLiang, true, false);

        //艺术品流转
        orderDetailInfo = copyrightCommonFunc.initOrderDetailInfo();
        response = copyright.crArtworkTransfer(BROKERKEYID1, PIN1, brokerAddress1, userAddress1, orderDetailInfo);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        storeHash = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        commonFunc.verifyTxDetailField(storeHash, "wvm_invoke", "2", "3", "42");

        copyrightCommonFunc.verifyAccountQuery(brokerAddress1, YSPBH, 99, true);
        copyrightCommonFunc.verifyAccountQuery(userAddress1, YSPBH, 1, true);

        copyrightCommonFunc.verifyArtworkQuery(userAddress1, YSPBH, "1", 1, false, false);
        copyrightCommonFunc.verifyArtworkQuery(brokerAddress1, YSPBH, "2", 1, false, false);

        //订单预支付接口
        String outTradeNo = String.valueOf(constructUnixTime(0));
        response = copyright.crPayPrepartion
                (outTradeNo, "数字藏品", OPENID, 1, "", "",
                        false, "", "", constructUnixTime(100), "");
        assertEquals("200", JSONObject.fromObject(response).getString("state"));

        //取消订单接口
        response = copyright.crOrderClose(outTradeNo, "取消支付");
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        storeHash = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        commonFunc.verifyTxDetailField(storeHash, "store", "0", "0", "0");

    }

    /**
     * 正常流程测试
     * 经纪商1发行艺术品1-数量200，艺术品2-数量100，艺术品3-数量50
     * 分别流转给普通用户，查询余额
     */

    @Test
    public void crIssueMultiTest() throws Exception {

        //艺术品发行上链YSPBH1数量200
        String bianhao1 = constructData("YSPBH1_", 8);
        log.info("艺术品编号：" + bianhao1);
        copyrightCommonFunc.initArtIssue(BROKERKEYID1, bianhao1, 200);

        //艺术品发行上链YSPBH2数量100
        String bianhao2 = constructData("YSPBH2_", 8);
        log.info("艺术品编号2：" + bianhao2);
        copyrightCommonFunc.initArtIssue(BROKERKEYID1, bianhao2, 100);

        //艺术品发行上链YSPBH3数量1
        String bianhao3 = constructData("YSPBH3_", 8);
        log.info("艺术品编号3：" + bianhao3);
        copyrightCommonFunc.initArtIssue(BROKERKEYID1, bianhao3, 1);

        //查询经纪商1账户艺术品,返回正确数据
        copyrightCommonFunc.verifyAccountQuery(brokerAddress1, bianhao1, 200, true);
        copyrightCommonFunc.verifyAccountQuery(brokerAddress1, bianhao2, 100, true);
        copyrightCommonFunc.verifyAccountQuery(brokerAddress1, bianhao3, 1, true);

        //查询艺术品信息接口，num为空，返回艺术品发行的数据
        copyrightCommonFunc.verifyArtworkQuery(brokerAddress1, bianhao1, "", 200, true, false);
        copyrightCommonFunc.verifyArtworkQuery(brokerAddress1, bianhao2, "", 100, true, false);
        copyrightCommonFunc.verifyArtworkQuery(brokerAddress1, bianhao3, "", 1, true, false);

        //艺术品流转
        YSPBH = bianhao1;
        orderDetailInfo = copyrightCommonFunc.initOrderDetailInfo();
        String response = copyright.crArtworkTransfer(BROKERKEYID1, PIN1, brokerAddress1, userAddress1, orderDetailInfo);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        YSPBH = bianhao2;
        orderDetailInfo = copyrightCommonFunc.initOrderDetailInfo();
        response = copyright.crArtworkTransfer(BROKERKEYID1, PIN1, brokerAddress1, userAddress1, orderDetailInfo);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        YSPBH = bianhao3;
        orderDetailInfo = copyrightCommonFunc.initOrderDetailInfo();
        response = copyright.crArtworkTransfer(BROKERKEYID1, PIN1, brokerAddress1, userAddress1, orderDetailInfo);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        //查询经纪商、普通用户账户艺术品
        copyrightCommonFunc.verifyAccountQuery(brokerAddress1, bianhao1, 199, true);
        copyrightCommonFunc.verifyAccountQuery(userAddress1, bianhao1, 1, true);
        copyrightCommonFunc.verifyAccountQuery(brokerAddress1, bianhao2, 99, true);
        copyrightCommonFunc.verifyAccountQuery(userAddress1, bianhao2, 1, true);
        copyrightCommonFunc.verifyAccountQuery(brokerAddress1, bianhao3, 0, true);
        copyrightCommonFunc.verifyAccountQuery(userAddress1, bianhao3, 1, true);

        copyrightCommonFunc.verifyArtworkQuery(userAddress1, bianhao1, "1", 1, false, false);
        copyrightCommonFunc.verifyArtworkQuery(userAddress1, bianhao2, "1", 1, false, false);
        copyrightCommonFunc.verifyArtworkQuery(userAddress1, bianhao3, "1", 1, false, false);
    }

    /**
     * 异常流程测试-艺术品流转接口
     * 经纪商1发行艺术品1数量为2，转给用户1成功，转给用户2成功，转给用户3失败
     * 查询艺术品信息，num参数分别为空、1、2
     */
    @Test
    public void crArtworkTransferInvalidTest() throws Exception {

        //艺术品发行上链
        String bianhao = constructData("YSPBH_", 8);
        log.info("艺术品编号：" + bianhao);
        copyrightCommonFunc.initArtIssue(BROKERKEYID1, bianhao, 2);
        copyrightCommonFunc.verifyAccountQuery(brokerAddress1, bianhao, 2, true);
        copyrightCommonFunc.verifyArtworkQuery(brokerAddress1, bianhao, "", 2, true, false);

        //艺术品流转用户1，用户2成功，流转用户3失败
        orderDetailInfo = copyrightCommonFunc.initOrderDetailInfo();
        String response = copyright.crArtworkTransfer(BROKERKEYID1, PIN1, brokerAddress1, userAddress1, orderDetailInfo);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        response = copyright.crArtworkTransfer(BROKERKEYID1, PIN1, brokerAddress1, userAddress2, orderDetailInfo);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        response = copyright.crArtworkTransfer(BROKERKEYID1, PIN1, brokerAddress1, userAddress3, orderDetailInfo);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));

        //账户艺术品查询，经纪商1无，普通用户1、2有，普通用户3无
        copyrightCommonFunc.verifyAccountQuery(brokerAddress1, bianhao, 0, false);
        copyrightCommonFunc.verifyAccountQuery(userAddress1, bianhao, 1, true);
        copyrightCommonFunc.verifyAccountQuery(userAddress2, bianhao, 1, true);
        copyrightCommonFunc.verifyAccountQuery(userAddress3, bianhao, 0, false);

        //查询艺术品信息
        copyrightCommonFunc.verifyArtworkQuery(brokerAddress1, bianhao, "", 2, true, true);
        copyrightCommonFunc.verifyArtworkQuery(userAddress1, bianhao, "1", 1, false, false);
        copyrightCommonFunc.verifyArtworkQuery(userAddress2, bianhao, "2", 1, false, false);

    }


    /**
     * 异常流程测试-账户注册接口
     * 相同的keyid重复注册接口
     */
    @Test
    public void crDuplicateAccountRegisterTest() throws Exception {

        String response = copyright.crAcountRegister(BROKERKEYID1, "broker", userDetailInfo);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));

        response = copyright.crAcountRegister(USERKEYID1, "user", userDetailInfo);
        assertEquals("500", JSONObject.fromObject(response).getString("state"));

    }


}