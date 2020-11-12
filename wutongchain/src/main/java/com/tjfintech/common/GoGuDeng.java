package com.tjfintech.common;

import com.tjfintech.common.Interface.GuDeng;
import com.tjfintech.common.utils.GetTest;
import com.tjfintech.common.utils.PostTest;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.SDKADD;

@Slf4j
public  class GoGuDeng implements GuDeng {

    /***
     * 登记挂牌企业
     * @param contractAddress
     * @param equityCode
     * @param totalShares
     * @param enterpriseSubjectInfo
     * @param equityProductInfo
     * @param bondProductInfo
     * @return
     */
    public String GDEnterpriseResister(String contractAddress, String equityCode, long totalShares, Map enterpriseSubjectInfo,
                                       Map equityProductInfo,Map bondProductInfo,Map fundProductInfo){

        Map<String, Object> map = new HashMap<>();
        map.put("contractAddress", contractAddress);
        map.put("equityCode", equityCode);
        map.put("totalShares", totalShares);
        map.put("enterpriseSubjectInfo", enterpriseSubjectInfo);
        map.put("equityProductInfo", equityProductInfo);
        map.put("bondProductInfo", bondProductInfo);
        map.put("fundProductInfo", fundProductInfo);

        String result = PostTest.postMethod(SDKADD + "/equity/enterprise/issue", map);
        log.info(result);
        return result;
    }

    /***
     * 股东/投资者开户
     * @param contractAddress   合约地址
     * @param investorInfo  投资者信息
     * @return
     */
    public String GDCreateAccout(String contractAddress, String clientNo,Map fundInfo,Map shareholderInfo,Map investorInfo){
        Map<String, Object> map = new HashMap<>();
        map.put("contractAddress", contractAddress);
        map.put("clientNo", clientNo);
        map.put("fundInfo", fundInfo);
        map.put("shareholderInfo", shareholderInfo);
        map.put("investorInfo", investorInfo);

        String result = PostTest.postMethod(SDKADD + "/equity/account/create", map);
        log.info(result);
        return result;
    }

    /***
     * 股东股份初始登记
     * 发行
     * @param contractAddress
     * @param platformKeyId
     * @param equityCode
     * @param shareList
     * @return
     */
    public String GDShareIssue(String contractAddress, String platformKeyId, String equityCode, List<Map> shareList){
        Map<String, Object> map = new HashMap<>();
        map.put("platformKeyId", platformKeyId);
        map.put("contractAddress", contractAddress);
        map.put("equityCode", equityCode);
        map.put("shareList", shareList);

        String result = PostTest.postMethod(SDKADD + "/equity/share/issue", map);
        log.info(result);
        return result;
    }

    /***
     * 股份性质变更
     * @param platformKeyId
     * @param address
     * @param equityCode
     * @param amount
     * @param oldShareProperty
     * @param newShareProperty
     * @param registerInformationList
     * @return
     */
    public String GDShareChangeProperty(String platformKeyId, String address, String equityCode, long amount,
                                        int oldShareProperty, int newShareProperty,List<Map> registerInformationList){
        Map<String, Object> map = new HashMap<>();
        map.put("platformkeyId", platformKeyId);
        map.put("address", address);
        map.put("equityCode", equityCode);
        map.put("amount", amount);
        map.put("oldShareProperty", oldShareProperty);
        map.put("newShareProperty", newShareProperty);
        map.put("registerInformationList", registerInformationList);

        String result = PostTest.postMethod(SDKADD + "/equity/share/change", map);
        log.info(result);
        return result;
    }

    public String GDShareChangeProperty(Map change){
        String result = PostTest.postMethod(SDKADD + "/equity/share/change", change);
        log.info(result);
        return result;
    }

    /***
     * 股份过户转让
     * @param keyId
     * @param fromAddr
     * @param amount
     * @param toAddr
     * @param shareProperty
     * @param equityCode
     * @param txInformation
     * @param fromRegisterInfo  转出账户登记信息
     * @param toRegisterInfo  转入账户登记信息
     * @return
     */
    public String GDShareTransfer(String keyId, String fromAddr, long amount, String toAddr, int shareProperty,
                                  String equityCode, Map txInformation, Map fromRegisterInfo,Map toRegisterInfo){
        Map<String, Object> map = new HashMap<>();
        map.put("keyId", keyId);
        map.put("fromAddress", fromAddr);
        map.put("amount", amount);
        map.put("toAddress", toAddr);
        map.put("shareProperty", shareProperty);
        map.put("equityCode", equityCode);
        map.put("txInformation", txInformation);
        map.put("fromRegisterInformation", fromRegisterInfo);
        map.put("toRegisterInformation", toRegisterInfo);

        String result = PostTest.postMethod(SDKADD + "/equity/share/transfer", map);
        log.info(result);
        return result;
    }

    /***
     * 股份过户转让
     * @param mapTransfer
     * @return
     */
    public String GDShareTransfer(Map mapTransfer){
        String result = PostTest.postMethod(SDKADD + "/equity/share/transfer", mapTransfer);
        log.info(result);
        return result;
    }

    /***
     * 股份增发
     * @param platformKeyId
     * @param equityCode
     * @param shareList
     * @param reason
     * @param equityProductInfo bondProductInfos
     * @return
     */
    public String GDShareIncrease(String platformKeyId, String equityCode, List<Map> shareList, String reason,Map equityProductInfo){
        Map<String, Object> map = new HashMap<>();
        map.put("platformKeyId", platformKeyId);
        map.put("equityCode", equityCode);
        map.put("shareList", shareList);
        map.put("reason", reason);
        map.put("equityProductInfo", equityProductInfo);

        String result = PostTest.postMethod(SDKADD + "/equity/share/increase", map);
        log.info(result);
        return result;
    }

    /***
     * 股份增发
     * @param platformKeyId
     * @param equityCode
     * @param shareList
     * @param reason
     * @param equityProductInfo bondProductInfos
     * @return
     */
    public String GDShareIncrease(String platformKeyId, String equityCode, List<Map> shareList, String reason,Map equityProductInfo,Map txInfo){
        Map<String, Object> map = new HashMap<>();
        map.put("platformKeyId", platformKeyId);
        map.put("equityCode", equityCode);
        map.put("shareList", shareList);
        map.put("reason", reason);
        map.put("equityProductInfo", equityProductInfo);
        map.put("transactionReport", txInfo);

        String result = PostTest.postMethod(SDKADD + "/equity/share/increase", map);
        log.info(result);
        return result;
    }

    /***
     * 股份冻结 带交易报告
     * @param bizNo
     * @param address
     * @param equityCode
     * @param amount
     * @param shareProperty
     * @param reason
     * @param cutoffDate
     * @param registerInformation
     * @return
     */
    public String GDShareLock(String bizNo, String address, String equityCode, long amount, int shareProperty, String reason,
                              String cutoffDate,Map registerInformation,Map txInfo){
        Map<String, Object> map = new HashMap<>();
        map.put("bizNo", bizNo);
        map.put("address", address);
        map.put("equityCode", equityCode);
        map.put("amount", amount);
        map.put("shareProperty", shareProperty);
        map.put("reason", reason);
        map.put("cutoffDate", cutoffDate);
        map.put("registerInformation", registerInformation);
        map.put("transactionReport", txInfo);

        String result = PostTest.postMethod(SDKADD + "/equity/share/lock", map);
        log.info(result);
        return result;
    }

    /***
     * 股份冻结 不带交易报告
     * @param bizNo
     * @param address
     * @param equityCode
     * @param amount
     * @param shareProperty
     * @param reason
     * @param cutoffDate
     * @param registerInformation
     * @return
     */
    public String GDShareLock(String bizNo, String address, String equityCode, long amount, int shareProperty, String reason,
                              String cutoffDate,Map registerInformation){
        Map<String, Object> map = new HashMap<>();
        map.put("bizNo", bizNo);
        map.put("address", address);
        map.put("equityCode", equityCode);
        map.put("amount", amount);
        map.put("shareProperty", shareProperty);
        map.put("reason", reason);
        map.put("cutoffDate", cutoffDate);
        map.put("registerInformation", registerInformation);

        String result = PostTest.postMethod(SDKADD + "/equity/share/lock", map);
        log.info(result);
        return result;
    }

    /***
     * 股份解除冻结 带交易报告
     * @param bizNo
     * @param equityCode
     * @param amount
     * @param registerInformation
     * @return
     */
    public String GDShareUnlock(String bizNo, String equityCode, long amount,Map registerInformation,Map txInfo){
        Map<String, Object> map = new HashMap<>();
        map.put("bizNo", bizNo);
        map.put("equityCode", equityCode);
        map.put("amount", amount);
        map.put("registerInformation", registerInformation);
        map.put("transactionReport", txInfo);

        String result = PostTest.postMethod(SDKADD + "/equity/share/unlock", map);
        log.info(result);
        return result;
    }

    /***
     * 股份解除冻结 不带交易报告
     * @param bizNo
     * @param equityCode
     * @param amount
     * @param registerInformation
     * @return
     */
    public String GDShareUnlock(String bizNo, String equityCode, long amount,Map registerInformation){
        Map<String, Object> map = new HashMap<>();
        map.put("bizNo", bizNo);
        map.put("equityCode", equityCode);
        map.put("amount", amount);
        map.put("registerInformation", registerInformation);

        String result = PostTest.postMethod(SDKADD + "/equity/share/unlock", map);
        log.info(result);
        return result;
    }

    /***
     * 股份回收
     * @param platformKeyId
     * @param equityCode
     * @param addressList
     * @param remark
     * @return
     */
    public String GDShareRecycle(String platformKeyId,String equityCode,List<Map> addressList,String remark){
        Map<String, Object> map = new HashMap<>();
        map.put("platformKeyId", platformKeyId);
        map.put("equityCode", equityCode);
        map.put("addressList", addressList);
        map.put("remark", remark);

        String result = PostTest.postMethod(SDKADD + "/equity/share/recycle", map);
        log.info(result);
        return result;
    }

    /***
     * 投资者销户
     * @param contractAddress
     * @param clientNo
     * @return
     */
    public String GDAccountDestroy(String contractAddress, String clientNo,String shareholderClosingCertificate,String fundClosingCertificate){
        Map<String, Object> map = new HashMap<>();
        map.put("contractAddress", contractAddress);
        map.put("clientNo", clientNo);
        map.put("shareholderClosingCertificate", shareholderClosingCertificate);
        map.put("fundClosingCertificate", fundClosingCertificate);

        String result = PostTest.postMethod(SDKADD + "/equity/account/destroy", map);
        log.info(result);
        return result;
    }

    /***
     * 写入公告信息
     * @param infoDisclosure
     * @return
     */
    public String GDInfoPublish(Map infoDisclosure){
        Map<String, Object> map = new HashMap<>();
        map.put("infoDisclosure", infoDisclosure);

        String result = PostTest.postMethod(SDKADD + "/equity/infodisclosure/publish", map);
        log.info(result);
        return result;
    }

    /***
     * 获取公告信息
     * @param txId
     * @return
     */
    public String GDInfoPublishGet(String txId){
        String result = GetTest.doGet2(SDKADD + "/equity/infodisclosure/query/" + txId );
        log.info(result);
        return result;
    }

    /***
     * 查询股东列表
     * @param equityCode
     * @return
     */
    public String GDGetEnterpriseShareInfo(String equityCode){
        Map<String, Object> map = new HashMap<>();
        map.put("equityCode", equityCode);

        String result = PostTest.postMethod(SDKADD + "/equity/share/shareholder/list", map);
        log.info(result);
        return result;
    }

    /***
     * 查询股东持股情况
     * @param contractAddress
     * @param clientNo
     * @return
     */
    public String GDGetShareHolderInfo(String contractAddress,String clientNo){
        Map<String, Object> map = new HashMap<>();
        map.put("contractAddress", contractAddress);
        map.put("clientNo", clientNo);

        String result = PostTest.postMethod(SDKADD + "/equity/share/query", map);
        log.info(result);
        return result;
    }

//    /***
//     * 场内转板接口
//     * @param platformKeyId
//     * @param companyId
//     * @param oldEquityCode
//     * @param newEquityCode
//     * @param registerInformation
//     * @return
//     */
//    public String GDShareChangeBoard(String platformKeyId, String companyId, String oldEquityCode, String newEquityCode,Map registerInformation, Map equityProductInfo,Map bondProductInfo){
//        Map<String, Object> map = new HashMap<>();
//        map.put("platformKeyId", platformKeyId);
//        map.put("companyId", companyId);
//        map.put("oldEquityCode", oldEquityCode);
//        map.put("newEquityCode", newEquityCode);
//        map.put("registerInformation", registerInformation);
//        map.put("equityProductInfo", equityProductInfo);
//        map.put("bondProductInfo", bondProductInfo);
//
//        String result = PostTest.postMethod(SDKADD + "/equity/share/changeboard", map);
//        log.info(result);
//        return result;
//    }

    /***
     * 场内转板接口
     * @param platformKeyId
     * @param companyId
     * @param oldEquityCode
     * @param newEquityCode
     * @param regInfoList
     * @return
     */
    public String GDShareChangeBoard(String platformKeyId, String companyId, String oldEquityCode, String newEquityCode,
                                     List<Map> regInfoList, Map equityProductInfo,Map bondProductInfo){
        Map<String, Object> map = new HashMap<>();
        map.put("platformKeyId", platformKeyId);
        map.put("companyId", companyId);
        map.put("oldEquityCode", oldEquityCode);
        map.put("newEquityCode", newEquityCode);
        map.put("registerInformationList", regInfoList);
        map.put("equityProductInfo", equityProductInfo);
        map.put("bondProductInfo", bondProductInfo);

        String result = PostTest.postMethod(SDKADD + "/equity/share/changeboard", map);
        log.info(result);
        return result;
    }

    /***
     * 资金清算接口
     * @param balanceAccount
     * @return
     */
    public String GDCapitalSettlement(Map balanceAccount){
        Map<String, Object> map = new HashMap<>();
        map.put("balanceAccount", balanceAccount);

        String result = PostTest.postMethod(SDKADD + "/equity/balance/count", map);
        log.info(result);
        return result;
    }

    /***
     * 投资者账号查询接口
     * @param contractAddress  合约地址
     * @param clientNo  客户号
     * @return
     */
    public String GDAccountQuery(String contractAddress,String clientNo){
        Map<String, Object> map = new HashMap<>();
        map.put("contractAddress", contractAddress);
        map.put("clientNo", clientNo);
        String result = PostTest.postMethod(SDKADD + "/equity/investor/account/query", map);
        log.info(result);
        return result;
    }

    /***
     * 主体信息查询接口
     * @param contractAddress  合约地址
     * @param subjectObjectId 主体数据中的对象标识
     * @return
     */
    public String GDMainSubjectQuery(String contractAddress,String subjectObjectId){
        Map<String, Object> map = new HashMap<>();
        map.put("contractAddress", contractAddress);
        map.put("subjectObjectId", subjectObjectId);
        String result = PostTest.postMethod(SDKADD + "/equity/subject/query", map);
        log.info(result);
        return result;
    }

    public String GDEquitySystemInit(String contractAddress,String platformKeyId){
        Map<String, Object> map = new HashMap<>();
        if(!contractAddress.isEmpty()) map.put("contractAddress", contractAddress);
        if(!platformKeyId.isEmpty()) map.put("platformKeyId", platformKeyId);
        String result = PostTest.postMethod(SDKADD + "/equity/system/init", map);
        log.info(result);
        return result;
    }

    public String GDUpdateSubjectInfo(String contractAddress,int type,Map subjectInfo){
        Map<String, Object> map = new HashMap<>();
        map.put("contractAddress", contractAddress);
        map.put("type", type);
        map.put("subjectInfo", subjectInfo);
        String result = PostTest.postMethod(SDKADD + "/equity/subject/update", map);
        log.info(result);
        return result;
    }
    public String GDGetTxReportInfo(String type, String value,String beginTime,String endTime){
        Map<String, Object> map = new HashMap<>();
        map.put("type", type);
        map.put("value", value);
        map.put("begin", beginTime);
        map.put("end", endTime);
        String result = PostTest.postMethod(SDKADD + "/equity/txreport/query", map);
        log.info(result);
        return result;
    }

}
