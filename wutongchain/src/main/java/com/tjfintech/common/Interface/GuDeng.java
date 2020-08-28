package com.tjfintech.common.Interface;

import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface GuDeng {

    String GDEnterpriseResister(String contractAddress,Map basicInfo, Map businessInfo, Map legalPersonInfo, String extend);
    String GDCreateAccout(String contractAddress,Map investorInfo);

    String GDShareIssue(String contractAddress,String platformKeyId,String equityCode,List<Map>shareList);
    String GDShareChangeProperty(String platformKeyId, String address, String equityCode, double amout, int oldShareProperty, int newShareProperty);

    String GDShareChangeBoard(String platformKeyId,String companyId,String oldEquityCode,String newEquityCode);

    String GDShareTransfer(String keyId,String fromAddr,double amount,String toAddr, int shareProperty,String equityCode,int txType,
                           String orderNo,int orderWay,int orderType,String price,String time,String remark);
    String GDShareIncrease(String contractAddress,String platformKeyId,String equityCode,List<Map>shareList,String reason);

    String GDAccountDestroy(String contractAddress,String clientNo);

}
