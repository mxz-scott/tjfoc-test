package com.tjfintech.common.GoInterface;

import com.tjfintech.common.Interface.Ipcr;
import com.tjfintech.common.utils.GetTest;
import com.tjfintech.common.utils.PostTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.SDKADD;
import static com.tjfintech.common.utils.UtilsClass.mongoDBAddr;
import static com.tjfintech.common.utils.UtilsClassIpcr.MALLURL;

@Slf4j
public class GoIpcr implements Ipcr {

    /***
     * 艺术品发行
     */
    public String crArtworkIssue(String keyId, String scAddress, String artworkId, String artHash) {

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
     * 订单预支付（支付宝PC）
     */
    public String crPayAliPage(String outTradeNo, String desc, int total, String notifyUrl, int expireTime, String attach) {

        Map<String, Object> map = new HashMap<>();
        map.put("outTradeNo", outTradeNo);
        map.put("desc", desc);
        map.put("total", total);
        map.put("notifyUrl", notifyUrl);
        map.put("expireTime", expireTime);
        map.put("attach", attach);

        String result = PostTest.postMethod(SDKADD + "/cr/v1/pay/ali/page", map);
        log.info(result);
        return result;
    }

    /***
     * 订单预支付（支付宝Wap）
     */
    public String crPayAliWap(String outTradeNo, String desc, int total, String quitUrl, String notifyUrl, int expireTime, String attach) {

        Map<String, Object> map = new HashMap<>();
        map.put("outTradeNo", outTradeNo);
        map.put("desc", desc);
        map.put("total", total);
        map.put("quitUrl", quitUrl);
        map.put("notifyUrl", notifyUrl);
        map.put("expireTime", expireTime);
        map.put("attach", attach);

        String result = PostTest.postMethod(SDKADD + "/cr/v1/pay/ali/wap", map);
        log.info(result);
        return result;
    }

    /***
     * 取消订单（支付宝）
     */
    public String crOrderCloseAli(String outTradeNo, String detail) {

        Map<String, Object> map = new HashMap<>();
        map.put("outTradeNo", outTradeNo);
        map.put("detail", detail);


        String result = PostTest.postMethod(SDKADD + "/cr/v1/order/close/ali", map);
        log.info(result);
        return result;
    }

    /***
     * 订单查询（支付宝）
     */
    public String crOrderQueryAli(String no, String qt) {

        Map<String, Object> map = new HashMap<>();

        String result = GetTest.doGet2(SDKADD + "/cr/v1/order/query/ali?" + "no=" + no + "&qt=" + qt);
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

    /***
     * 灵鲸商城-经纪商详情
     */
    public String ipcrMallBroker(String brokerNo) {

        String result = GetTest.doGetIpcrMall(MALLURL + "/broker/" + brokerNo);
        log.info(result);
        return result;
    }

    /***
     * 灵鲸商城-艺术品库存
     */
    public String ipcrMallArtworkStock(String artworkNo) {

        String result = GetTest.doGetIpcrMall(MALLURL + "/artwork/stock/" + artworkNo);
        log.info(result);
        return result;
    }

    /***
     * 灵鲸商城-系列下的艺术品列表
     */
    public String ipcrMallArtworkSeries(String seriesId) {

        String result = GetTest.doGetIpcrMall(MALLURL + "/artwork/series/" + seriesId);
        log.info(result);
        return result;
    }

    /***
     * 灵鲸商城-艺术品详情
     */
    public String ipcrMallArtwork(String artworkNo) {

        String result = GetTest.doGetIpcrMall(MALLURL + "/artwork/" + artworkNo);
        log.info(result);
        return result;
    }

    /***
     * 灵鲸商城-用户购买的系列下的艺术品列表
     */
    public String ipcrMallArtworkSeriesUser(String seriesId) {

        String result = GetTest.doGetIpcrMall(MALLURL + "/artwork/series/user?seriesId=" + seriesId);
        log.info(result);
        return result;
    }

    /***
     * 灵鲸商城-获取展示的轮播图路径
     */
    public String ipcrMallCarouselUrls() {

        String result = GetTest.doGetIpcrMall(MALLURL + "/carousel/urls");
        log.info(result);
        return result;
    }

    /***
     * 灵鲸商城-系列的详情
     */
    public String ipcrMallSeriesUp(String seriesId) {

        String result = GetTest.doGetIpcrMall(MALLURL + "/series/up/" + seriesId);
        log.info(result);
        return result;
    }

    /***
     * 灵鲸商城-用户已经购买的系列列表
     */
    public String ipcrMallSeriesUser(int page, int size) {

        String result = GetTest.doGetIpcrMall(MALLURL + "/series/user?page=" + page + "&page=" + size);
        log.info(result);
        return result;
    }

    /***
     * 灵鲸商城-系列分页列表
     */
    public String ipcrMallSeriesPage(int page, int size, String keyword, int isTest) {

        String result = GetTest.doGetIpcrMall(MALLURL + "/series/page/" + page + "?size=" + size + "&keyword=" + keyword + "&isTest=" + isTest);
        log.info(result);
        return result;
    }

    /***
     * 灵鲸商城-获取文件上传TOKEN
     */
    public String ipcrMallFileGetToken() {

        String result = GetTest.doGetIpcrMall(MALLURL + "/file/getToken");
        log.info(result);
        return result;
    }

    /***
     * 灵鲸商城-获取文件地址
     */
    public String ipcrMallFile(int id) {

        String result = GetTest.doGetIpcrMall(MALLURL + "/file/getToken");
        log.info(result);
        return result;
    }

    /***
     * 灵鲸商城-用户信息
     */
    public String ipcrMallUserInfo() {

        String result = GetTest.doGetIpcrMall(MALLURL + "/user/info");
        log.info(result);
        return result;
    }

    /***
     * 灵鲸商城-邀请码有效性验证
     */
    public String ipcrMallSpreadValidate(String code) {

        String result = GetTest.doGetIpcrMall(MALLURL + "/spread/validate/" + code);
        log.info(result);
        return result;
    }

    /***
     * 灵鲸商城-获取用户邀请成功数
     */
    public String ipcrMallSpreadInviteNum() {

        String result = GetTest.doGetIpcrMall(MALLURL + "/spread/invite/num");
        log.info(result);
        return result;
    }

    /***
     * 灵鲸商城-邀请码获取
     */
    public String ipcrMallSpreadInvite() {

        String result = GetTest.doGetIpcrMall(MALLURL + "/spread/invite");
        log.info(result);
        return result;
    }

    /***
     * 灵鲸商城-积分余额
     */
    public String ipcrMallPointBalance() {

        String result = GetTest.doGetIpcrMall(MALLURL + "/point/balance");
        log.info(result);
        return result;
    }

    /***
     * 灵鲸商城-积分记录列表
     */
    public String ipcrMallPointRecords(String[] codes, int page, int size) {

        String param = "";
        String params = "";
        for (int i = 0; i < codes.length; i++) {
            param = "&code=" + codes[i];
            params = params + param;
        }

        String result = GetTest.doGetIpcrMall(MALLURL + "/point/records?page=" + page + "&size=" + size + params);
        log.info(result);
        return result;
    }

    /***
     * 灵鲸商城-文章详情
     */
    public String ipcrMallUserPenInfo(int id) {

        String result = GetTest.doGetIpcrMall(MALLURL + "/userPen/info/" + id);
        log.info(result);
        return result;
    }

    /***
     * 灵鲸商城-文章列表
     */
    public String ipcrMallUserPenList() {

        String result = GetTest.doGetIpcrMall(MALLURL + "/userPen/list/");
        log.info(result);
        return result;
    }

    /***
     * 灵鲸商城-订单创建
     */
    public String ipcrMallOrder(String artworkNo, int num) {

        Map<String, Object> map = new HashMap<>();
        map.put("artworkNo", artworkNo);
        map.put("num", num);

        String result = PostTest.postMethodIpcrMall(MALLURL + "/order", map);
        log.info(result);
        return result;
    }

    /***
     * 灵鲸商城-订单取消
     */
    public String ipcrMallOrderCancel(String orderNo) {

        Map<String, Object> map = new HashMap<>();
        map.put("orderNo", orderNo);

        String result = PostTest.postMethodIpcrMall(MALLURL + "/order/cancel/" + orderNo, null);
        log.info(result);
        return result;
    }

    /***
     * 灵鲸商城-订单支付
     */
    public String ipcrMallOrderPay(String orderNo) {

        Map<String, Object> map = new HashMap<>();
        map.put("orderNo", orderNo);

        String result = PostTest.postMethodIpcrMall(MALLURL + "/order/pay/" + orderNo, null);
        log.info(result);
        return result;
    }

    /***
     * 灵鲸商城-订单详情
     */
    public String ipcrMallOrder(String orderNo) {

        String result = GetTest.doGetIpcrMall(MALLURL + "/order/" + orderNo);
        log.info(result);
        return result;
    }

    /***
     * 灵鲸商城-用户的订单列表
     */
    public String ipcrMallOrderUser(int page, int size, int status) {

        String result = GetTest.doGetIpcrMall(MALLURL + "/order/user?page=" + page + "&size=" + size + "&status=" + status);
        log.info(result);
        return result;
    }

    /***
     * 灵鲸商城-订单状态
     */
    public String ipcrMallOrderStatus(String orderNo) {

        String result = GetTest.doGetIpcrMall(MALLURL + "/order/status/" + orderNo);
        log.info(result);
        return result;
    }

    /***
     * 灵鲸商城-实名认证
     */
    public String ipcrMallRealName(String realName,String idCard) {

        Map<String, Object> map = new HashMap<>();
        map.put("realName", realName);
        map.put("idCard", idCard);

        String result = PostTest.postMethodIpcrMall(MALLURL + "/realname" ,map);
        log.info(result);
        return result;
    }

}
