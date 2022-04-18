package com.tjfintech.common.GoInterface;

import com.tjfintech.common.Interface.Copyright;
import com.tjfintech.common.utils.GetTest;
import com.tjfintech.common.utils.PostTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.SDKADD;

@Slf4j
public class GoCopyright implements Copyright {

    /***
     * 艺术品发行
     */
    public String crArtworkIssue(String keyId, String scAddress,  String artworkId, String artHash) {

        Map<String, Object> map = new HashMap<>();
        map.put("keyId", keyId);
        map.put("scAddress", scAddress);
        map.put("artworkId", artworkId);
        map.put("artHash", artHash);

        String result = PostTest.postMethod(SDKADD + "/cr/v1/artwork/issue", map);
        log.info(result);
        return result;
    }

    /***
     * 艺术品流转（废弃）
     */
    public String crArtworkTransfer(String keyId, String pin, String fromAddress, String toAddress, String ORDERDETAILINFO) {

        Map<String, Object> map = new HashMap<>();
        map.put("keyId", keyId);
        map.put("pin", pin);
        map.put("fromAddress", fromAddress);
        map.put("toAddress", toAddress);
        map.put("detail", ORDERDETAILINFO);

        String result = PostTest.postMethod(SDKADD + "/cr/v1/artwork/transfer", map);
        log.info(result);
        return result;
    }

    /***
     * 查询艺术品信息（废弃）
     */
    public String crArtworkQuery(String artworkId, String num) {

        Map<String, Object> map = new HashMap<>();

        String result = GetTest.doGet2(SDKADD + "/cr/v1/artwork/query?" + "artworkId=" + artworkId + "&num=" + num);
        log.info(result);
        return result;
    }

    /***
     * 艺术品交易历史
     */
    public String crArtworkHistory(String artworkId) {

        Map<String, Object> map = new HashMap<>();

        String result = GetTest.doGet2(SDKADD + "/cr/v1/artwork/tx/history?" + "artworkId=" + artworkId);
        log.info(result);
        return result;
    }

    /***
     * 艺术品系列合约初始化
     */
    public String crArtworkScinit(String symbol, int max, String owner, int typeNo, String baseUrl) {

        Map<String, Object> map = new HashMap<>();
        map.put("symbol", symbol);
        map.put("max", max);
        map.put("owner", owner);
        map.put("typeNo", typeNo);
        map.put("baseUrl", baseUrl);

        String result = PostTest.postMethod(SDKADD + "/cr/v1/artwork/scinit", map);
        log.info(result);
        return result;
    }

    /***
     * 获取艺术品URL
     */
    public String crArtworkUrlQuery(String scAddress, String artworkId) {

        Map<String, Object> map = new HashMap<>();

        String result = GetTest.doGet2(SDKADD + "/cr/v1/artwork/url/query?" + "scAddress=" + scAddress + "&artworkId=" + artworkId);
        log.info(result);
        return result;
    }

    /***
     * 艺术品状态更新
     */
    public String crArtworkScupdate(String scAddress, String artworkId, int time, boolean state) {

        Map<String, Object> map = new HashMap<>();
        map.put("scAddress", scAddress);
        map.put("artworkId", artworkId);
        map.put("time", time);
        map.put("state", state);

        String result = PostTest.postMethod(SDKADD + "/cr/v1/artwork/scupdate", map);
        log.info(result);
        return result;
    }

    /***
     * 注册账户
     */
    public String crAcountRegister(String keyId, String type) {

        Map<String, Object> map = new HashMap<>();
        map.put("keyId", keyId);
        map.put("type", type);

        String result = PostTest.postMethod(SDKADD + "/cr/v1/account/register", map);
        log.info(result);
        return result;
    }

    /***
     * 查询账户下艺术品
     */
    public String crAccountQuery(String address, String scAddress) {

        Map<String, Object> map = new HashMap<>();

        String result = GetTest.doGet2(SDKADD + "/cr/v1/account/query?" + "addr=" + address + "&scAddress=" + scAddress);
        log.info(result);
        return result;
    }

    /***
     * 账户信息更新（废弃）
     */
    public String crAccountUpdate(String address, String detail) {

        Map<String, Object> map = new HashMap<>();
        map.put("address", address);
        map.put("detail", detail);

        String result = PostTest.postMethod(SDKADD + "/cr/v1/account/update", map);
        log.info(result);
        return result;
    }

    /***
     * 账户信息查询（废弃）
     */
    public String crAccountDetail(String address) {

        Map<String, Object> map = new HashMap<>();

        String result = GetTest.doGet2(SDKADD + "/cr/v1/account/detail?" + "addr=" + address);
        log.info(result);
        return result;
    }

    /***
     * 艺术品审核信息存储
     */
    public String crOrderStore(String bianHao, String yiShuPinBianHao, String shenHeJiGou, String shenHeZhangHao, String shenHeTime,
                               String shenHeJieGuo, String shenHeShuoMing, String shenHeLeiXing, String chuShouFangShi, int baoZhengJin, int diJia) {

        Map<String, Object> map = new HashMap<>();
        map.put("bianHao", bianHao);
        map.put("yiShuPinBianHao", yiShuPinBianHao);
        map.put("shenHeJiGou", shenHeJiGou);
        map.put("shenHeZhangHao", shenHeZhangHao);
        map.put("shenHeTime", shenHeTime);
        map.put("shenHeJieGuo", shenHeJieGuo);
        map.put("shenHeShuoMing", shenHeShuoMing);
        map.put("shenHeLeiXing", shenHeLeiXing);
        map.put("chuShouFangShi", chuShouFangShi);
        map.put("baoZhengJin", baoZhengJin);
        map.put("diJia", diJia);

        String result = PostTest.postMethod(SDKADD + "/cr/v1/order/store", map);
        log.info(result);
        return result;
    }

    /***
     * 订单预支付（小程序）
     */
    public String crPayPrepartion(String outTradeNo, String desc, String openId, int total, String notifyUrl, String goodsTag,
                                  boolean share, String clientIp, String deviceId, int expireTime, String attach) {

        Map<String, Object> map = new HashMap<>();
        map.put("outTradeNo", outTradeNo);
        map.put("desc", desc);
        map.put("openId", openId);
        map.put("total", total);
        map.put("notifyUrl", notifyUrl);
        map.put("goodsTag", goodsTag);
        map.put("share", share);
        map.put("clientIp", clientIp);
        map.put("deviceId", deviceId);
        map.put("expireTime", expireTime);
        map.put("attach", attach);

        String result = PostTest.postMethod(SDKADD + "/cr/v1/pay/prepartion", map);
        log.info(result);
        return result;
    }

    /***
     * 订单预支付（微信H5）
     */
    public String crPayPrepartionH5(String outTradeNo, String desc, String type, int total, String notifyUrl, String goodsTag,
                                  boolean share, String clientIp, String deviceId, int expireTime, String attach) {

        Map<String, Object> map = new HashMap<>();
        map.put("outTradeNo", outTradeNo);
        map.put("desc", desc);
        map.put("type", type);
        map.put("total", total);
        map.put("notifyUrl", notifyUrl);
        map.put("goodsTag", goodsTag);
        map.put("share", share);
        map.put("clientIp", clientIp);
        map.put("deviceId", deviceId);
        map.put("expireTime", expireTime);
        map.put("attach", attach);

        String result = PostTest.postMethod(SDKADD + "/cr/v1/pay/prepartion/h5", map);
        log.info(result);
        return result;
    }

    /***
     * 取消订单（小程序）
     */
    public String crOrderClose(String outTradeNo, String detail) {

        Map<String, Object> map = new HashMap<>();
        map.put("outTradeNo", outTradeNo);
        map.put("detail", detail);


        String result = PostTest.postMethod(SDKADD + "/cr/v1/order/close", map);
        log.info(result);
        return result;
    }

    /***
     * 取消订单（微信H5）
     */
    public String crOrderCloseH5(String outTradeNo, String detail) {

        Map<String, Object> map = new HashMap<>();
        map.put("outTradeNo", outTradeNo);
        map.put("detail", detail);


        String result = PostTest.postMethod(SDKADD + "/cr/v1/order/close/h5", map);
        log.info(result);
        return result;
    }

    /***
     * 订单查询（小程序）
     */
    public String crOrderQuery(String no, String qt) {

        Map<String, Object> map = new HashMap<>();

        String result = GetTest.doGet2(SDKADD + "/cr/v1/order/query?" + "no=" + no + "&qt=" + qt);
        log.info(result);
        return result;
    }

    /***
     * 订单查询（微信H5）
     */
    public String crOrderQueryH5(String no, String qt) {

        Map<String, Object> map = new HashMap<>();

        String result = GetTest.doGet2(SDKADD + "/cr/v1/order/query/h5?" + "no=" + no + "&qt=" + qt);
        log.info(result);
        return result;
    }

    /***
     * 生成艺术品证书
     */
    public String crArtworkCertPicture(String certId, String name, String artworkName, String address, int time, String artHash, String authHash) {

        Map<String, Object> map = new HashMap<>();
        map.put("certId", certId);
        map.put("name", name);
        map.put("artworkName", artworkName);
        map.put("address", address);
        map.put("time", time);
        map.put("artHash", artHash);
        map.put("authHash", authHash);

        String result = PostTest.postMethod(SDKADD + "/cr/v1/artwork/cert/picture", map);
        log.info(result);
        return result;
    }

}
