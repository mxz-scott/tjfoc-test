package com.tjfintech.common.Interface;

import java.util.Map;

public interface Copyright {

    String crArtworkIssue(String keyId, String scAddress,  String artworkId, String artHash);

    String crArtworkTransfer(String keyId, String pin, String fromAddress, String toAddress, String ORDERDETAILINFO);

    String crArtworkQuery(String artworkId, String num);

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


}
