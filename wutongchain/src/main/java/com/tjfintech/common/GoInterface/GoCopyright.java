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
    public String crArtworkIssue(String keyId, String pin, String artDetailInfo) {

        Map<String, Object> map = new HashMap<>();
        map.put("keyId", keyId);
        map.put("pin", pin);
        map.put("detail", artDetailInfo);

        String result = PostTest.postMethod(SDKADD + "/cr/v1/artwork/issue", map);
        log.info(result);
        return result;
    }

    /***
     * 艺术品流转
     */
    public String crArtworkTransfer(String keyId, String pin, String fromAddress, String toAddress, String orderDetailInfo) {

        Map<String, Object> map = new HashMap<>();
        map.put("keyId", keyId);
        map.put("pin", pin);
        map.put("fromAddress", fromAddress);
        map.put("toAddress", toAddress);
        map.put("detail", orderDetailInfo);

        String result = PostTest.postMethod(SDKADD + "/cr/v1/artwork/transfer", map);
        log.info(result);
        return result;
    }

    /***
     * 查询艺术品信息
     */
    public String crArtworkQuery(String bianhao, String num) {

        Map<String, Object> map = new HashMap<>();

        String result = GetTest.doGet2(SDKADD + "/cr/v1/artwork/query?" + "bianHao=" + bianhao + "&num=" + num);
        log.info(result);
        return result;
    }

    /***
     * 注册账户
     */
    public String crAcountRegister(String keyId, String type, String AccountDetailInfo) {

        Map<String, Object> map = new HashMap<>();
        map.put("keyId", keyId);
        map.put("type", type);
        map.put("detail", AccountDetailInfo);

        String result = PostTest.postMethod(SDKADD + "/cr/v1/account/register", map);
        log.info(result);
        return result;
    }

    /***
     * 查询账户下艺术品
     */
    public String crAccountQuery(String addr) {

        Map<String, Object> map = new HashMap<>();

        String result = GetTest.doGet2(SDKADD + "/cr/v1/account/query?" + "addr=" + addr);
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
     * 订单预支付
     */
    public String crPayPrepartion(String outTradeNo, String desc, String openId, int total, String notifyUrl, String goodsTag,
                                  boolean share, String clientIp, String deviceId, int expireTime,String attach) {

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
     * 取消订单
     */
    public String crOrderClose (String outTradeNo, String detail) {

        Map<String, Object> map = new HashMap<>();
        map.put("outTradeNo", outTradeNo);
        map.put("detail", detail);


        String result = PostTest.postMethod(SDKADD + "/cr/v1/order/close", map);
        log.info(result);
        return result;
    }

}
