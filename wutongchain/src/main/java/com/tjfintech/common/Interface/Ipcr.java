package com.tjfintech.common.Interface;

import java.util.Map;

public interface Ipcr {

    String crArtworkIssue(String keyId, String scAddress, String artworkId, String artHash);

    String crArtworkHistory(String artworkId);

    String crArtworkScinit(String symbol, int max, String owner, int typeNo, String baseUrl);

    String crArtworkUrlQuery(String scAddress, String artworkId);

    String crArtworkScupdate(String scAddress, String artworkId, int time, boolean state);

    String crAcountRegister(String keyId, String type);

    String crAccountQuery(String address, String scAddress);

    String crAccountUpdate(String address, String detail);

    String crAccountDetail(String address);

    String crOrderStore(String BianHao, String YiShuPinBianHao, String ShenHeJiGou, String ShenHeZhangHao, String ShenHeTime,
                        String ShenHeJieGuo, String ShenHeShuoMing, String ShenHeLeiXing, String ChuShouFangShi, int BaoZhengJin, int DiJia);

    String crPayPrepartion(String outTradeNo, String desc, String openId, int total, String notifyUrl, String goodsTag,
                           boolean share, String clientIp, String deviceId, int expireTime, String attach);

    String crPayPrepartionH5(String outTradeNo, String desc, String type, int total, String notifyUrl, String goodsTag,
                             boolean share, String clientIp, String deviceId, int expireTime, String attach);

    String crOrderClose(String outTradeNo, String detail);

    String crOrderCloseH5(String outTradeNo, String detail);

    String crOrderQuery(String no, String qt);

    String crOrderQueryH5(String no, String qt);

    String crArtworkCertPicture(String certId, String name, String artworkName, String address, int time, String artHash, String authHash);

    String crPayAliPage(String outTradeNo, String desc, int total, String notifyUrl, int expireTime, String attach);

    String crPayAliWap(String outTradeNo, String desc, int total, String quitUrl, String notifyUrl, int expireTime, String attach);

    String crOrderCloseAli(String outTradeNo, String detail);

    String crOrderQueryAli(String no, String qt);

    String ipcrMallBroker(String brokerNo);

    String ipcrMallArtworkStock(String artworkNo);

    String ipcrMallArtworkSeries(String seriesId);

    String ipcrMallArtwork(String artworkNo);

    String ipcrMallArtworkSeriesUser(String seriesId);

    String ipcrMallCarouselUrls();

    String ipcrMallSeriesUp(String seriesId);

    String ipcrMallSeriesUser(int page, int size);

    String ipcrMallSeriesPage(int page, int size, String keyword, int isTest);

    String ipcrMallFileGetToken();

    String ipcrMallFile(int id);

    String ipcrMallUserInfo();

    String ipcrMallSpreadValidate(String code);

    String ipcrMallSpreadInviteNum();

    String ipcrMallSpreadInvite ();

    String ipcrMallPointBalance ();

    String ipcrMallPointRecords(String[] codes, int page, int size);

    String ipcrMallUserPenInfo(int id);

    String ipcrMallUserPenList();

    String ipcrMallOrder(String artworkNo, int num);

    String ipcrMallOrderCancel(String orderNo);

    String ipcrMallOrderPay(String orderNo);

    String ipcrMallOrder(String orderNo);

    String ipcrMallOrderUser(int page, int size, int status);

    String ipcrMallOrderStatus(String orderNo);

    String ipcrMallRealName(String realName,String idCard);


}
