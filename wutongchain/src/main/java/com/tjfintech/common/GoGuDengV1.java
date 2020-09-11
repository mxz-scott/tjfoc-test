package com.tjfintech.common;

import com.tjfintech.common.Interface.GuDengV1;
import com.tjfintech.common.utils.GetTest;
import com.tjfintech.common.utils.PostTest;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.SDKADD;

@Slf4j
public  class GoGuDengV1 implements GuDengV1 {

    /***
     * 挂牌企业登记
     * @param contractAddress  托管合约地址
     * @param basicInfo  基本信息
     * @param businessInfo  工商信息
     * @param legalPersonInfo 法人信息
     * @param extend 扩展字段
     * @return
     */
    public String GDEnterpriseResister(String contractAddress, Map basicInfo, Map businessInfo, Map legalPersonInfo, String extend){

        Map<String, Object> map = new HashMap<>();
        map.put("basicInfo", basicInfo);
        map.put("businessInfo", businessInfo);
        map.put("legalPersonInfo", legalPersonInfo);
        map.put("contractAddress", contractAddress);
        if(!extend.isEmpty())   map.put("extend", extend);

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
    public String GDCreateAccout(String contractAddress,Map investorInfo){
        Map<String, Object> map = new HashMap<>();
        map.put("contractAddress", contractAddress);
        map.put("investorInfo", investorInfo);

        String result = PostTest.postMethod(SDKADD + "/equity/account/create", map);
        log.info(result);
        return result;
    }

    public String GDShareIssue(String contractAddress,String platformKeyId,String equityCode,List<Map> shareList){
        Map<String, Object> map = new HashMap<>();
        map.put("platformKeyId", platformKeyId);
        map.put("contractAddress", contractAddress);
        map.put("equityCode", equityCode);
        map.put("shareList", shareList);

        String result = PostTest.postMethod(SDKADD + "/equity/share/issue", map);
        log.info(result);
        return result;
    }

    public String GDShareChangeProperty(String platformKeyId, String address, String equityCode, double amount, int oldShareProperty, int newShareProperty){
        Map<String, Object> map = new HashMap<>();
        map.put("platformkeyId", platformKeyId);
        map.put("address", address);
        map.put("equityCode", equityCode);
        map.put("amount", amount);
        map.put("oldShareProperty", oldShareProperty);
        map.put("newShareProperty", newShareProperty);

        String result = PostTest.postMethod(SDKADD + "/equity/share/change", map);
        log.info(result);
        return result;
    }

    public String GDShareChangeProperty(Map change){
        String result = PostTest.postMethod(SDKADD + "/equity/share/change", change);
        log.info(result);
        return result;
    }

    public String GDShareChangeBoard(String platformKeyId,String companyId,String oldEquityCode,String newEquityCode){
        Map<String, Object> map = new HashMap<>();
        map.put("platformKeyId", platformKeyId);
        map.put("companyId", companyId);
        map.put("oldEquityCode", oldEquityCode);
        map.put("newEquityCode", newEquityCode);

        String result = PostTest.postMethod(SDKADD + "/equity/share/changeboard", map);
        log.info(result);
        return result;
    }

    public String GDShareTransfer(String keyId,String fromAddr,double amount,String toAddr, int shareProperty,String equityCode,int txType,
                           String orderNo,int orderWay,int orderType,String price,String time,String remark){
        Map<String, Object> map = new HashMap<>();
        map.put("keyId", keyId);
        map.put("fromAddress", fromAddr);
        map.put("amount", amount);
        map.put("toAddress", toAddr);
        map.put("shareProperty", shareProperty);
        map.put("equityCode", equityCode);
        map.put("txType", txType);
        map.put("orderNo", orderNo);
        map.put("orderWay", orderWay);
        map.put("orderType", orderType);
        map.put("price", price);
        map.put("time", time);
        map.put("remark", remark);

        String result = PostTest.postMethod(SDKADD + "/equity/share/transfer", map);
        log.info(result);
        return result;
    }

    public String GDShareTransfer(Map mapTransfer){
        String result = PostTest.postMethod(SDKADD + "/equity/share/transfer", mapTransfer);
        log.info(result);
        return result;
    }

    public String GDShareIncrease(String contractAddress,String platformKeyId,String equityCode,List<Map>shareList,String reason){
        Map<String, Object> map = new HashMap<>();
        map.put("platformKeyId", platformKeyId);
        map.put("contractAddress", contractAddress);
        map.put("equityCode", equityCode);
        map.put("shareList", shareList);
        map.put("reason", reason);

        String result = PostTest.postMethod(SDKADD + "/equity/share/increase", map);
        log.info(result);
        return result;
    }

    public String GDShareLock(String bizNo, String address, String equityCode, double amount, int shareProperty, String reason,String cutoffDate){
        Map<String, Object> map = new HashMap<>();
        map.put("bizNo", bizNo);
        map.put("address", address);
        map.put("equityCode", equityCode);
        map.put("amount", amount);
        map.put("shareProperty", shareProperty);
        map.put("reason", reason);
        map.put("cutoffDate", cutoffDate);

        String result = PostTest.postMethod(SDKADD + "/equity/share/lock", map);
        log.info(result);
        return result;
    }

    public String GDShareUnlock(String bizNo, String equityCode, double amount){
        Map<String, Object> map = new HashMap<>();
        map.put("bizNo", bizNo);
        map.put("equityCode", equityCode);
        map.put("amount", amount);

        String result = PostTest.postMethod(SDKADD + "/equity/share/unlock", map);
        log.info(result);
        return result;
    }

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

    public String GDAccountDestroy(String contractAddress,String clientNo){
        Map<String, Object> map = new HashMap<>();
        map.put("contractAddress", contractAddress);
        map.put("clientNo", clientNo);

        String result = PostTest.postMethod(SDKADD + "/equity/account/destroy", map);
        log.info(result);
        return result;
    }

    public String GDInfoPublish(String type,String subType,String title,String fileHash,String fileURL,
                                String hashAlgo,String publisher,String publishTime,String enterprise){
        Map<String, Object> map = new HashMap<>();
        map.put("type", type);
        map.put("subType", subType);
        map.put("title", title);
        map.put("fileHash", fileHash);
        map.put("fileURL", fileURL);
        map.put("hashAlgo", hashAlgo);
        map.put("publisher", publisher);
        map.put("publishTime", publishTime);
        map.put("enterprise", enterprise);

        String result = PostTest.postMethod(SDKADD + "/equity/infodisclosure/publish", map);
        log.info(result);
        return result;
    }
    public String GDInfoPublishGet(String txId){
        String result = GetTest.doGet2(SDKADD + "/equity/infodisclosure/query/" + txId );
        log.info(result);
        return result;
    }

    public String GDGetEnterpriseShareInfo(String equityCode){
        Map<String, Object> map = new HashMap<>();
        map.put("equityCode", equityCode);

        String result = PostTest.postMethod(SDKADD + "/equity/share/shareholder/list", map);
        log.info(result);
        return result;
    }

    public String GDGetShareHolderInfo(String contractAddress,String clientNo){
        Map<String, Object> map = new HashMap<>();
        map.put("contractAddress", contractAddress);
        map.put("clientNo", clientNo);

        String result = PostTest.postMethod(SDKADD + "/equity/share/query", map);
        log.info(result);
        return result;
    }

}
