package com.tjfintech.common.Interface;

import java.util.Map;

public interface Copyright {

    String crArtworkIssue(String keyId, String pin, String artDetailInfo);

    String crArtworkTransfer(String keyId, String pin, String fromAddress, String toAddress, String orderDetailInfo);

    String crArtworkQuery(String bianhao, String num);

    String crAcountRegister(String keyId, String type, String AccountDetailInfo);

    String crAccountQuery(String addr);

    String crOrderStore(String BianHao, String YiShuPingBianHao, String ShenHeJiGou, String ShenHeZhangHao, String ShenHeTime,
                        String ShenHeJieGuo, String ShenHeShuoMing, String ShenHeLeiXing, String ChuShouFangShi, int BaoZhengJin,int DiJia);

    String crPayPrepartion(String outTradeNo, String desc, String openId, String total, String notifyUrl, String goodsTag,
                           boolean share, String clientIp, String deviceId, String expireTime,String attach);

}
