package com.tjfintech.common.Interface;

import java.util.List;
import java.util.Map;

public interface GuDeng {

    String GDEnterpriseResister(String contractAddress, String equityCode, long totalShares, Map enterpriseSubjectInfo,
                                Map equityProductInfo,Map bondProductInfo,Map fundProductInfo);
    String GDCreateAccout(String contractAddress, String clientNo,Map fundInfo,Map shareholderInfo,Map investorInfo);

    String GDShareIssue(String contractAddress, String platformKeyId, String equityCode, List<Map> shareList);
    String GDShareChangeProperty(String platformKeyId, String address, String equityCode, long amount,
                                 int oldShareProperty, int newShareProperty,List<Map> registerInformationList);
    String GDShareChangeProperty(Map change);

    String GDShareTransfer(String keyId, String fromAddr, long amount, String toAddr, int shareProperty,
                           String equityCode, Map txInformation, Map fromRegisterInfo,Map toRegisterInfo);
    String GDShareTransfer(Map mapTransfer);

    String GDShareIncrease(String platformKeyId, String equityCode, List<Map> shareList, String reason,Map equityProductInfo);
    String GDShareIncrease(String platformKeyId, String equityCode, List<Map> shareList, String reason,Map equityProductInfo,Map txInfo);

    String GDShareLock(String bizNo, String address, String equityCode, long amount, int shareProperty, String reason,
                       String cutoffDate,Map registerInformation);
    String GDShareLock(String bizNo, String address, String equityCode, long amount, int shareProperty, String reason,
                       String cutoffDate,Map registerInformation,Map txInfo);
    String GDShareUnlock(String bizNo, String equityCode, long amount,Map registerInformation);
    String GDShareUnlock(String bizNo, String equityCode, long amount,Map registerInformation,Map txInfo);

    String GDShareRecycle(String platformKeyId, String equityCode, List<Map> addressList, String remark);

//    String GDShareChangeBoard(String platformKeyId, String companyId, String oldEquityCode, String newEquityCode,Map registerInformation,Map equityProductInfo,Map bondProductInfo);
    String GDShareChangeBoard(String platformKeyId, String companyId, String oldEquityCode, String newEquityCode,List<Map> regInfoList,
                              Map oldProductInfo,Map newProductInfo);

    String GDAccountDestroy(String contractAddress, String clientNo,String shareholderClosingDate,List<Map> shareholderClosingCertificate,
                            String fundClosingDate,List<Map> fundClosingCertificate,String name,String number);

    String GDInfoPublish(Map infoDisclosure);
    String GDInfoPublishGet(String txId);


    String GDGetEnterpriseShareInfo(String equityCode);
    String GDGetShareHolderInfo(String contractAddress, String clientNo);

    String GDCapitalSettlement(Map balanceAccount);
    String GDAccountQuery(String contractAddress,String clientNo);
    String GDMainSubjectQuery(String contractAddress,String subjectObjectId);
    String GDProductQuery(String contractAddress,String productObjectId);
    String GDObjectQueryByVer(String objectId,int version);

    String GDEquitySystemInit(String contractAddress,String platformKeyId);

    String GDUpdateSubjectInfo(String contractAddress,int type,Map subjectInfo);
    String GDUpdateAccountInfo(String contractAddress,String clientNo,Map accountInfo);
    String GDUpdateProductInfo(String contractAddress,Map productInfo);
    String GDGetTxReportInfo(String type, String value,String beginTime,String endTime);


    String GDEquitySuperviseInfoDelete(String objectId, int version,String type);
    String GDEquitySuperviseInfoUpdate(String objectId,String type,long updateTime,Map objectInfo);

    String GDEquitySuperviseInfoRepair(String id, int version,String type);

}
