package com.tjfintech.common.functionTest.guDengTestV2;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.GuDeng;
import com.tjfintech.common.Interface.GuDengV1;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class GDUnitFunc {

    TestBuilder testBuilder= TestBuilder.getInstance();
    GuDeng gd =testBuilder.getGuDeng();
    Store store =testBuilder.getStore();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    String result = "check on chain success";
//    public static Map registerInfo = new HashMap();//05登记 //发行 股份性质变更 过户转让 股份增发
//    public static Map txInformatio = new HashMap();//04交易报告  //过户转让
//    public static Map enterpriseSubjectInfo = new HashMap();//01主体  //挂牌企业登记
//    public static Map productInfo = new HashMap();//03产品 //挂牌企业登记  股份增发
//    public static Map accountInfo = new HashMap();//02账户  //投资者开户
//    public static Map investorInfo = new HashMap();//01主体  //投资者开户


    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    public Map<String,String> createAcc(String clientNo,String equityCode,boolean bCheckOnchain)throws Exception{
        log.info("创建账户 " + clientNo);

        Map<String,String> addrInfo = new HashMap<>();

        String cltNo = clientNo;
        String shareHolderNo = "SH" + cltNo;
        String fundNo = "fund" + cltNo;
        String eqCode = equityCode;

        Map mapFundInfo = new HashMap();
        accountInfo.put("test","1");
        mapFundInfo.put("fundNo",fundNo);
        mapFundInfo.put("accountInfo",accountInfo);

        Map mapShareHodlInfo = new HashMap();
        accountInfo.clear();
        accountInfo.put("test","2");
        mapShareHodlInfo.put("shareholderNo",shareHolderNo);
        mapFundInfo.put("accountInfo",accountInfo);

        investorInfo.put("test","3");

        String response= gd.GDCreateAccout(gdContractAddress,cltNo,mapFundInfo,mapShareHodlInfo,investorInfo);
        addrInfo.put("response",response);

        if(bCheckOnchain) {
            JSONObject jsonObject = JSONObject.fromObject(response);
            String txId = jsonObject.getJSONObject("data").getString("txId");
            assertEquals(cltNo, JSONObject.fromObject(response).getJSONObject("data").getJSONObject("accountList").getString("clientNo"));
            assertEquals(shareHolderNo, JSONObject.fromObject(response).getJSONObject("data").getJSONObject("accountList").getString("shareholderNo"));
            String keyId = JSONObject.fromObject(response).getJSONObject("data").getJSONObject("accountList").getString("keyId");
            String address = JSONObject.fromObject(response).getJSONObject("data").getJSONObject("accountList").getString("address");
            String shareHdNo = JSONObject.fromObject(response).getJSONObject("data").getJSONObject("accountList").getString("shareholderNo");
            commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
            assertEquals("200", JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

            addrInfo.put("clientNo", cltNo);
            addrInfo.put("keyId", keyId);
            addrInfo.put("address", address);
            addrInfo.put("holderNo", shareHdNo);
        }
        return addrInfo;
    }

    /***
     * 股份性质变更
     * @param eqCode    股权代码
     * @param address    变更账户
     * @param changeAmount  变更数量
     * @param oldProperty  变更前股权性质
     * @param newProperty  变更后股权性质
     * @throws Exception
     */
    public String changeSHProperty(String address,String eqCode,long changeAmount,
                                 int oldProperty,int newProperty,List<Map> regInfoList,boolean bCheckOnchain) throws Exception{
        log.info("股权性质变更");

        String response= gd.GDShareChangeProperty(gdPlatfromKeyID,address,eqCode,changeAmount,oldProperty,newProperty,regInfoList);

        if(bCheckOnchain) {
            JSONObject jsonObject = JSONObject.fromObject(response);
            String txId = jsonObject.getJSONObject("data").getString("txId");

            commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
            assertEquals("200", JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

            gd.GDGetEnterpriseShareInfo(eqCode);
            return result;
        }
        gd.GDGetEnterpriseShareInfo(eqCode);
        return response;
    }


    /***
     * 过户转让
     * @param keyID  转出账户地址的keyID
     * @param fromAddr   转出地址
     * @param amount    转出数量
     * @param toAddr    转入地址
     * @param shareProperty  股权代码性质
     * @param eqCode  股权代码
     * @param txInfo  交易信息 对应监管数据04交易部分
     * @param regInfo 主体信息
     * @throws Exception
     */
    public String shareTransfer(String keyID,String fromAddr,long amount,String toAddr,int shareProperty,String eqCode,
                                Map txInfo,List<Map> regInfo,boolean bCheckOnchain) throws Exception{
        log.info("股权代码过户转让");

        String response= gd.GDShareTransfer(keyID,fromAddr,amount,toAddr,shareProperty,eqCode,txInfo, regInfo);

        if(bCheckOnchain) {
            JSONObject jsonObject = JSONObject.fromObject(response);
            String txId = jsonObject.getJSONObject("data").getString("txId");

            commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
            assertEquals("200", JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

//            gd.GDGetEnterpriseShareInfo(eqCode);
            return result;
        }
//        gd.GDGetEnterpriseShareInfo(eqCode);
        return response;
    }

    /***
     * 股份初始登记
     * @param eqCode  待发股权代码
     * @param shareList  待发列表
     * @throws Exception
     */
    public String shareIssue(String eqCode,List<Map> shareList,boolean bCheckOnchain) throws Exception{
        log.info("发行");

        String response= gd.GDShareIssue(gdContractAddress,gdPlatfromKeyID,eqCode,shareList);
        if(bCheckOnchain) {
            JSONObject jsonObject = JSONObject.fromObject(response);
            String txId = jsonObject.getJSONObject("data").getString("txId");

            commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
            assertEquals("200", JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

//            gd.GDGetEnterpriseShareInfo(eqCode);
            return result;
        }
//        gd.GDGetEnterpriseShareInfo(eqCode);
        return response;
    }

    /***
     * 股份增发
     * @param eqCode  待增发股权代码
     * @param shareList  增发列表
     * @throws Exception
     */
    public String shareIncrease(String eqCode,List<Map> shareList,Map pdtInfo,boolean bCheckOnchain) throws Exception{

        String reason = "股份分红";

        String response= gd.GDShareIncrease(gdPlatfromKeyID,eqCode,shareList,reason,pdtInfo);
        if(bCheckOnchain) {
            JSONObject jsonObject = JSONObject.fromObject(response);
            String txId = jsonObject.getJSONObject("data").getString("txId");

            commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
            assertEquals("200", JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

            gd.GDGetEnterpriseShareInfo(eqCode);
            return result;
        }
        gd.GDGetEnterpriseShareInfo(eqCode);
        return response;
    }

    /***
     * 冻结解冻
     * @param bizNo  商务编号 唯一
     * @param eqCode  股权代码
     * @param address  待冻结账户
     * @param lockAmount    冻结数量
     * @param shareProperty     冻结股权性质
     * @throws Exception
     */
    public String lock(String bizNo,String address,String eqCode,long lockAmount,int shareProperty,
                       String cutoffDate,Map regInfo,boolean bCheckOnchain) throws Exception{
        log.info("股份冻结");
        String reason = "司法冻结";

        String response= gd.GDShareLock(bizNo,address,eqCode,lockAmount,shareProperty,reason,cutoffDate,regInfo);

        if(bCheckOnchain) {
            JSONObject jsonObject = JSONObject.fromObject(response);
            String txId = jsonObject.getJSONObject("data").getString("txId");

            commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
            assertEquals("200", JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

            gd.GDGetEnterpriseShareInfo(eqCode);
            return result;
        }
        gd.GDGetEnterpriseShareInfo(eqCode);
        return response;

    }


    /***
     * 冻结解冻
     * @param bizNo  商务编号 唯一
     * @param eqCode  股权代码
     * @param unlockAmount    解除冻结数量
     * @throws Exception
     */
    public String unlock(String bizNo,String eqCode,long unlockAmount,Map regInfo,boolean bCheckOnchain) throws Exception{
        log.info("股份解除冻结");
        String response= gd.GDShareUnlock(bizNo,eqCode,unlockAmount,regInfo);

        if(bCheckOnchain) {
            JSONObject jsonObject = JSONObject.fromObject(response);
            String txId = jsonObject.getJSONObject("data").getString("txId");

            commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
            assertEquals("200", JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

            gd.GDGetEnterpriseShareInfo(eqCode);
            return result;
        }
        gd.GDGetEnterpriseShareInfo(eqCode);
        return response;

    }

    /***
     * 场内转板接口
     * @param oldEquityCode  转前股权代码
     * @param newEquityCode  转后股权代码
     * @throws Exception
     */
    public String changeBoard(String oldEquityCode,String newEquityCode,Map regInfo,Map productInfo,boolean bCheckOnchain) throws Exception{
        log.info("场内转板");
        String response= gd.GDShareChangeBoard(gdPlatfromKeyID,gdCompanyID,oldEquityCode,newEquityCode,regInfo,productInfo);

        if(bCheckOnchain) {
            JSONObject jsonObject = JSONObject.fromObject(response);
            String txId = jsonObject.getJSONObject("data").getString("txId");

            commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
            assertEquals("200", JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));
            gdEquityCode = newEquityCode;

            gd.GDGetEnterpriseShareInfo(oldEquityCode);
            gd.GDGetEnterpriseShareInfo(newEquityCode);
            return result;
        }

        gd.GDGetEnterpriseShareInfo(oldEquityCode);
        gd.GDGetEnterpriseShareInfo(newEquityCode);

        return response;
    }

    /***
     * 回收/减资
     * @param eqCode   回收的股权代码
     * @param shareList  回收地址账户列表
     * @throws Exception
     */
    public String shareRecycle(String eqCode ,List<Map> shareList,boolean bCheckOnchain) throws Exception {
        log.info("股份回收/减持");

        String remark = "777777";
        String response= gd.GDShareRecycle(gdPlatfromKeyID,eqCode,shareList,remark);

        if(bCheckOnchain) {
            JSONObject jsonObject = JSONObject.fromObject(response);
            String txId = jsonObject.getJSONObject("data").getString("txId");

            commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
            assertEquals("200", JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

            gd.GDGetEnterpriseShareInfo(eqCode);
            return result;
        }
        gd.GDGetEnterpriseShareInfo(eqCode);
        return response;
    }


    /***
     * 销户接口
     * @param clntNo   客户号
     * @throws Exception
     */
    public String destroyAcc(String clntNo,boolean bCheckOnchain) throws Exception {
        log.info("销户 " + clntNo);

        String response= gd.GDAccountDestroy(gdContractAddress,clntNo);

        if(bCheckOnchain) {
            JSONObject jsonObject = JSONObject.fromObject(response);
            String txId = jsonObject.getJSONObject("data").getString("txId");

            commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
            assertEquals("200", JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

            return result;
        }
        return response;

    }

}
