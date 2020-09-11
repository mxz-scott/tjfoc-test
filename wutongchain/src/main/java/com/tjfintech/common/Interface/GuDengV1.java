package com.tjfintech.common.Interface;

import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface GuDengV1 {

    String GDEnterpriseResister(String contractAddress,Map basicInfo, Map businessInfo, Map legalPersonInfo, String extend);
    String GDCreateAccout(String contractAddress,Map investorInfo);

    String GDShareIssue(String contractAddress,String platformKeyId,String equityCode,List<Map> shareList);
    String GDShareChangeProperty(String platformKeyId, String address, String equityCode, double amount, int oldShareProperty, int newShareProperty);
    String GDShareChangeProperty(Map change);

    String GDShareChangeBoard(String platformKeyId,String companyId,String oldEquityCode,String newEquityCode);

    String GDShareTransfer(String keyId,String fromAddr,double amount,String toAddr, int shareProperty,String equityCode,int txType,
                           String orderNo,int orderWay,int orderType,String price,String time,String remark);
    String GDShareTransfer(Map mapTransfer);

    String GDShareIncrease(String contractAddress,String platformKeyId,String equityCode,List<Map> shareList,String reason);

    String GDShareLock(String bizNo, String address, String equityCode, double amount, int shareProperty, String reason,String cutoffDate);
    String GDShareUnlock(String bizNo, String equityCode, double amount);

    String GDShareRecycle(String platformKeyId,String equityCode,List<Map> addressList,String remark);

    String GDAccountDestroy(String contractAddress,String clientNo);

    String GDInfoPublish(String type,String subType,String title,String fileHash,String fileURL,String hashAlgo,String publisher,String publishTime,String enterprise);
    String GDInfoPublishGet(String txId);


    String GDGetEnterpriseShareInfo(String equityCode);
    String GDGetShareHolderInfo(String contractAddress,String clientNo);

}
