package com.tjfintech.common.functionTest.CopyrightTest;

import com.tjfintech.common.CertTool;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.Copyright;
import com.tjfintech.common.Interface.Kms;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassCopyright.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;


@Slf4j
public class CopyrightCommonFunc {
    TestBuilder testBuilder = TestBuilder.getInstance();
    Store store = testBuilder.getStore();
    Copyright copyright = testBuilder.getCopyright();
    Kms kms = testBuilder.getKms();
    CertTool certTool = new CertTool();
    CommonFunc commonFunc = new CommonFunc();
    UtilsClass utilsClass = new UtilsClass();

    /***
     * 构造普通用户相关信息json
     */
    public String initUSERDETAILINFO() {

        log.info("初始化普通用户信息");
        Map maptemp = new HashMap();

        maptemp.put("BianHao", constructData("BianHao", 8));
        maptemp.put("PingTaiBianHao", constructData("PingTaiBianHao", 8));
        maptemp.put("ShouJiHao", "15962601234");
        maptemp.put("Id", "342221199207221234");
        maptemp.put("XieYi", "7eeebb7e1f826c8a8e9504ad341b8da9");

        JSONObject jsonObject = JSONObject.fromObject(maptemp);
        String json = jsonObject.toString();
        return json;
    }

    /***
     * 构造经纪商相关信息json
     */

    public String initBROKERDETAILINFO() {

        log.info("初始化经纪商信息");
        Map maptemp = new HashMap();

        maptemp.put("BianHao", constructData("BianHao", 8));
        maptemp.put("Name", "Name");
        maptemp.put("ZhuCeHao", constructData("", 8));
        maptemp.put("QiYeDiZhi", "企业地址");
        maptemp.put("ChengLiTime", "2020/02");
        maptemp.put("QuYu", "QuYu");
        maptemp.put("LeiXing", "LeiXing");
        maptemp.put("GuDongId", "7eeebb7e1f826c8a8e9504ad341b8da9");
        maptemp.put("ShouJiHao", "15962601234");
        maptemp.put("EMail", "zhangsan@163.com");
        maptemp.put("JingYingXuKe", "7eeebb7e1f826c8a8e9504ad341b8da9");
        maptemp.put("YingYeZhiZhao", "7eeebb7e1f826c8a8e9504ad341b8da9");
        maptemp.put("GongSiZhangCheng", "7eeebb7e1f826c8a8e9504ad341b8da9");
        maptemp.put("XieYi", "7eeebb7e1f826c8a8e9504ad341b8da9");
        maptemp.put("YuLiu", "预留字段");

        JSONObject jsonObject = JSONObject.fromObject(maptemp);
        String json = jsonObject.toString();
        return json;
    }

    /***
     * 构造艺术品相关信息json
     */

//    public String initARTDETAILINFO() {
//
//        log.info("初始化艺术品信息");
//        Map maptemp = new HashMap();
//
//        maptemp.put("BianHao", YSPBH);
//        maptemp.put("Name", "春山揽玉");
//        maptemp.put("XingShi", "数字艺术品");
//        maptemp.put("ZhuTi", "山水");
//        maptemp.put("FengGe", "写实");
//        maptemp.put("ShuLiang", ShuLiang);
//        maptemp.put("ChuangZuoTime", constructUnixTime(0));
//        maptemp.put("XiangQing", "文本");
//        maptemp.put("YiShuJia", "张文华");
//        maptemp.put("FenBianLv", "1080*2376");
//        maptemp.put("ChiFu", "1080*2376");
//        maptemp.put("ChuShouFang", "ChuShouFang");
//        maptemp.put("CSFIdLeiXing", "身份证");
//        maptemp.put("CSFId", "342221199207221234");
//        maptemp.put("GaoQingTu", "7eeebb7e1f826c8a8e9504ad341b8da9");
//        maptemp.put("XiaoTu", "7eeebb7e1f826c8a8e9504ad341b8da9");
//        maptemp.put("FuJian", "7eeebb7e1f826c8a8e9504ad341b8da9");
//        maptemp.put("HeYingTu", "7eeebb7e1f826c8a8e9504ad341b8da9");
//        maptemp.put("JJS", "灰墙艺术");
//        maptemp.put("XieYi", "7eeebb7e1f826c8a8e9504ad341b8da9");
//        maptemp.put("YuLiu", "预留字段");
//
//        JSONObject jsonObject = JSONObject.fromObject(maptemp);
//        String json = jsonObject.toString();
//        return json;
//    }

    /***
     * 构造艺术品审核相关信息json
     */

//    public String initARTREVIEWDETAILINFO() {
//
//        log.info("初始化艺术品审核信息");
//        Map maptemp = new HashMap();
//
//        maptemp.put("BianHao", constructData("SHBH", 8));
//        maptemp.put("YiShuPinBianHao", YSPBH);
//        maptemp.put("ShenHeJiGou", "上技所");
//        maptemp.put("ShenHeZhangHao", "yyds");
//        maptemp.put("ShenHeTime", constructTime(0));
//        maptemp.put("ShenHeJieGuo", "通过");
//        maptemp.put("ShenHeSHuoMing", "文本");
//        maptemp.put("ShenHeLeiXing", "上技所审核");
//        maptemp.put("ChuShouFangShi", "一口价");
//        maptemp.put("BaoZhengJin", 1000);
//        maptemp.put("DiJia", 1000);
//
//        JSONObject jsonObject = JSONObject.fromObject(maptemp);
//        String json = jsonObject.toString();
//        return json;
//    }

    /***
     * 构造订单相关信息json
     */

//    public String initORDERDETAILINFO() {
//
//        log.info("初始化订单信息");
//        Map maptemp = new HashMap();
//
//        maptemp.put("BianHao", constructData("DDBH", 8));
//        maptemp.put("ShengChengTime", constructUnixTime(0));
//        maptemp.put("JiaGe", 1000000);
//        maptemp.put("JiaoYiShuLiang", 1);
//        maptemp.put("ZhuanChuZhangHao", "621226102089261234");
//        maptemp.put("ZhuanChuName", "微信支付");
//        maptemp.put("ZhuanRuZhangHao", "621226102089264321");
//        maptemp.put("ChuShouLeiXing", "一口价");
//        maptemp.put("SuoYouZhe", "张三");
//        maptemp.put("SuoYouZheLeiXing", "身份证");
//        maptemp.put("SuoYouZheId", "34222119920722601234");
//        maptemp.put("YiShuPinBianHao", YSPBH);
//        maptemp.put("YiShuPinHash", "7eeebb7e1f826c8a8e9504ad341b8da9");
//        maptemp.put("HeTong", "7eeebb7e1f826c8a8e9504ad341b8da9");
//        maptemp.put("JJS", "");
//        maptemp.put("ZhiZhao", "7eeebb7e1f826c8a8e9504ad341b8da9");
//        maptemp.put("TiGongFangName", "李四");
//        maptemp.put("TiGongFangLeiXing", "身份证");
//        maptemp.put("TiGongFangBianHao", "34222119920722604321");
//        maptemp.put("JiaoGeTime", constructUnixTime(0));
//        maptemp.put("ZhengMingJiGou", "证明机构");
//        maptemp.put("ChuZhengTime", constructUnixTime(0));
//        maptemp.put("YuLiu", "预留");
//
//        JSONObject jsonObject = JSONObject.fromObject(maptemp);
//        String json = jsonObject.toString();
//        return json;
//    }

    /***
     * 初始化上链相关信息json
     */
//    public void initDetailInfo() {
//
//        log.info("初始化上链相关信息json");
//
//        USERDETAILINFO = initUSERDETAILINFO();
//        BROKERDETAILINFO = initBROKERDETAILINFO();
//        ARTDETAILINFO = initARTDETAILINFO();
//        ARTREVIEWDETAILINFO = initARTREVIEWDETAILINFO();
//        ORDERDETAILINFO = initORDERDETAILINFO();
//
//    }

    /***
     * 初始化账户信息
     */
    public void initAccountInfo() {

        log.info("初始化KMS和账户地址相关信息");

        PIN1 = "PIN1";

        String response = kms.createKey("sm2", "PIN1");
        USERKEYID1 = JSONObject.fromObject(response).getJSONObject("data").getString("keyId");
        response = copyright.crAcountRegister(USERKEYID1, "user");
        USERADDRESS1 = JSONObject.fromObject(response).getJSONObject("data").getString("address");
        log.info("普通用户1：" + USERKEYID1 + "地址：" + USERADDRESS1);

        response = kms.createKey("sm2", "PIN1");
        USERKEYID2 = JSONObject.fromObject(response).getJSONObject("data").getString("keyId");
        response = copyright.crAcountRegister(USERKEYID2, "user");
        USERADDRESS2 = JSONObject.fromObject(response).getJSONObject("data").getString("address");
        log.info("普通用户2：" + USERKEYID2 + "地址：" + USERADDRESS2);

        response = kms.createKey("sm2", "PIN1");
        USERKEYID3 = JSONObject.fromObject(response).getJSONObject("data").getString("keyId");
        response = copyright.crAcountRegister(USERKEYID3, "user");
        USERADDRESS3 = JSONObject.fromObject(response).getJSONObject("data").getString("address");
        log.info("普通用户3：" + USERKEYID3 + "地址：" + USERADDRESS3);

        response = kms.createKey("sm2", "PIN1");
        BROKERKEYID1 = JSONObject.fromObject(response).getJSONObject("data").getString("keyId");
        response = copyright.crAcountRegister(BROKERKEYID1, "broker");
        BROKERADDRESS1 = JSONObject.fromObject(response).getJSONObject("data").getString("address");
        log.info("经纪商1：" + BROKERKEYID1 + "地址：" + BROKERADDRESS1);

        response = kms.createKey("sm2", "PIN1");
        BROKERKEYID2 = JSONObject.fromObject(response).getJSONObject("data").getString("keyId");
        response = copyright.crAcountRegister(BROKERKEYID2, "broker");
        BROKERADDRESS2 = JSONObject.fromObject(response).getJSONObject("data").getString("address");
        log.info("经纪商2：" + BROKERKEYID2 + "地址：" + BROKERADDRESS2);

        response = kms.createKey("sm2", "PIN1");
        BROKERKEYID3 = JSONObject.fromObject(response).getJSONObject("data").getString("keyId");
        response = copyright.crAcountRegister(BROKERKEYID3, "broker");
        BROKERADDRESS3 = JSONObject.fromObject(response).getJSONObject("data").getString("address");
        log.info("经纪商3：" + BROKERKEYID3 + "地址：" + BROKERADDRESS3);

    }

    /***
     * 艺术品系列合约初始化
     */
    public void initArtSmartContract() throws Exception {

        //艺术品系列初始化
        String response = copyright.crArtworkScinit(SYMBOL, MAX, BROKERADDRESS1, TYPENO, BASEURL);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        SCADDRESS1 = JSONObject.fromObject(response).getJSONObject("data").getString("name");

        //艺术品状态更新
        response = copyright.crArtworkScupdate(SCADDRESS1, ARTWORKID, constructUnixTime(0), true);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));


    }

    /***
     * 验证账户艺术品查询
     */
    public void verifyAccountQuery(String address, String scAddress, String artworkId, String symbol, boolean flag) throws Exception {

        String response = copyright.crAccountQuery(address, scAddress);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        if (flag == false) {
            JSONObject.fromObject(response).getJSONObject("data").getString("collection").equals("null");
        } else {
            JSONArray jsonArray = JSONObject.fromObject(response).getJSONObject("data").getJSONArray("collection");
            log.info(jsonArray.toString());
            assertEquals(true, jsonArray.toString().contains(artworkId));
            for (int i = 0; i < jsonArray.size(); i++) {
                if (JSONObject.fromObject(jsonArray.get(i)).getString("artworkId").equals(artworkId)) {
                    assertEquals(BASEURL + "/" + artworkId, JSONObject.fromObject(jsonArray.get(i)).getString("custom"));
                    assertEquals(symbol, JSONObject.fromObject(jsonArray.get(i)).getString("symbol"));
                }
            }
        }

    }

    /***
     * 验证艺术品流转历史
     */
    public void verifyArtworkHistory(String artworkId, String to, String txid) throws Exception {

        String response = copyright.crArtworkHistory(artworkId);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));

        JSONArray jsonArray = JSONObject.fromObject(response).getJSONObject("data").getJSONArray("hisList");
        for (int i = 0; i < jsonArray.size(); i++) {
            if (JSONObject.fromObject(jsonArray.get(i)).getString("txid").equals(txid)) {
                assertEquals(ZEROADDRESS, JSONObject.fromObject(jsonArray.get(i)).getString("from"));
                assertEquals(to, JSONObject.fromObject(jsonArray.get(i)).getString("to"));
            }


        }
    }
}