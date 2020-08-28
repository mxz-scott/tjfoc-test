package com.tjfintech.common;

import com.tjfintech.common.Interface.GuDeng;
import com.tjfintech.common.Interface.Token;
import com.tjfintech.common.utils.GetTest;
import com.tjfintech.common.utils.PostTest;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.SDKADD;
import static com.tjfintech.common.utils.UtilsClass.subLedger;

@Slf4j
public  class GoGuDeng implements GuDeng {

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

    public String GDShareChangeProperty(String platformKeyId, String address, String equityCode, double amout, int oldShareProperty, int newShareProperty){
        Map<String, Object> map = new HashMap<>();
        map.put("platformkeyId", platformKeyId);
        map.put("address", address);
        map.put("equityCode", equityCode);
        map.put("amount", amout);
        map.put("oldShareProperty", oldShareProperty);
        map.put("newShareProperty", newShareProperty);

        String result = PostTest.postMethod(SDKADD + "/equity/share/change", map);
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
        map.put("tradeTime", time);
        map.put("remark", remark);

        String result = PostTest.postMethod(SDKADD + "/equity/share/transfer", map);
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

    public String GDAccountDestroy(String contractAddress,String clientNo){
        Map<String, Object> map = new HashMap<>();
        map.put("contractAddress", contractAddress);
        map.put("clientNo", clientNo);

        String result = PostTest.postMethod(SDKADD + "/equity/account/destroy", map);
        log.info(result);
        return result;
    }

}
