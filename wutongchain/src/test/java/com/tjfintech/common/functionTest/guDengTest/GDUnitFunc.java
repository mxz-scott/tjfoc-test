package com.tjfintech.common.functionTest.guDengTest;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.GDBeforeCondition;
import com.tjfintech.common.Interface.GuDeng;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.CommonFunc.gdConstructShareList;
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


    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    public Map<String,String> createAcc(String clientNo,String equityCode,boolean bCheckOnchain)throws Exception{
        Map<String,String> addrInfo = new HashMap<>();

        String cltNo = clientNo;
        String shareHolderNo = "SH" + cltNo;
        String eqCode = equityCode;

        Map mapPersonInfo = new HashMap();
        mapPersonInfo.put("clientFullName","fullname" + cltNo);
        mapPersonInfo.put("organizationType","苏州股权代码");
        mapPersonInfo.put("certificateType",0);
        mapPersonInfo.put("certificateNo","123456468123153");
        mapPersonInfo.put("certificateAddress","certificateAddress");
        mapPersonInfo.put("gender",0);
        mapPersonInfo.put("telephone","1598222555555");
        mapPersonInfo.put("phone","1598222555555");
        mapPersonInfo.put("postalCode","1585685245666821236");
        mapPersonInfo.put("contactAddress","人民币");
        mapPersonInfo.put("mailBox","1598222555555");
        mapPersonInfo.put("fax","苏州同济区块链研究");
        mapPersonInfo.put("equityCode","苏同院");
        mapPersonInfo.put("equityAmount",5000);
        mapPersonInfo.put("shareProperty",0);

        Map mapinvestor = new HashMap();
        mapinvestor.put("salesDepartment","业务一部");
        mapinvestor.put("clientGroups","群组");
        mapinvestor.put("equityAccountNo","111111");
        mapinvestor.put("currency","人民币");
        mapinvestor.put("board","E板");
        mapinvestor.put("accountType",0);
        mapinvestor.put("accountStatus",0);
        mapinvestor.put("registrationDate","621552");
        mapinvestor.put("lastTradingDate","20200828");
        mapinvestor.put("closingDate","20200828");
        mapinvestor.put("shareholderAmount",3);

        String extend = "";

        Map mapInvestorInfo = new HashMap();

        mapInvestorInfo.put("clientName","name" + cltNo);
        mapInvestorInfo.put("shareholderNo",shareHolderNo);
        mapInvestorInfo.put("fundNo","fund" + cltNo);
        mapInvestorInfo.put("clientNo",cltNo);
        mapInvestorInfo.put("extend",extend);
        mapInvestorInfo.put("personalInfo",mapPersonInfo);
        mapInvestorInfo.put("investor",mapinvestor);


        String response= gd.GDCreateAccout(gdContractAddress,mapInvestorInfo);
        addrInfo.put("response",response);

        if(bCheckOnchain) {
            JSONObject jsonObject = JSONObject.fromObject(response);
            String txId = jsonObject.getJSONObject("data").getString("txId");
            assertEquals(cltNo, JSONObject.fromObject(response).getJSONObject("data").getJSONObject("accountList").getString("clientNo"));
            assertEquals(shareHolderNo, JSONObject.fromObject(response).getJSONObject("data").getJSONObject("accountList").getString("shareholderNo"));
            String keyId = JSONObject.fromObject(response).getJSONObject("data").getJSONObject("accountList").getString("keyId");
            String address = JSONObject.fromObject(response).getJSONObject("data").getJSONObject("accountList").getString("address");

            commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
            assertEquals("200", JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

            addrInfo.put("clientNo", cltNo);
            addrInfo.put("keyId", keyId);
            addrInfo.put("address", address);
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
    public String changeSHProperty(String address,String eqCode,double changeAmount,
                                 int oldProperty,int newProperty,boolean bCheckOnchain) throws Exception{

        String response= gd.GDShareChangeProperty(gdPlatfromKeyID,address,eqCode,changeAmount,oldProperty,newProperty);

        if(bCheckOnchain) {
            JSONObject jsonObject = JSONObject.fromObject(response);
            String txId = jsonObject.getJSONObject("data").getString("txId");

            commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
            assertEquals("200", JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

            return result;
        }
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
     * @param txType  交易类型 （0：非交易过户，1：交易过户）
     * @param orderNo 委托编号
     * @throws Exception
     */
    public String shareTransfer(String keyID,String fromAddr,double amount,String toAddr,int shareProperty,String eqCode,
                              int txType,String orderNo,boolean bCheckOnchain) throws Exception{
        int orderWay = 0;
        int orderType = 0;
        String price = "10000";
        String time = "20200828";
        String remark = "转账";
        String response= gd.GDShareTransfer(keyID,fromAddr,amount,toAddr,shareProperty,eqCode,txType,
                orderNo,orderWay,orderType,price,time,remark);

        if(bCheckOnchain) {
            JSONObject jsonObject = JSONObject.fromObject(response);
            String txId = jsonObject.getJSONObject("data").getString("txId");

            commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
            assertEquals("200", JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

            return result;
        }
        return response;
    }

    /***
     * 股份增发
     * @param eqCode  待增发股权代码
     * @param shareList  增发列表
     * @throws Exception
     */
    public String shareIncrease(String eqCode,List<Map> shareList,boolean bCheckOnchain) throws Exception{

        String reason = "股份分红";

        String response= gd.GDShareIncrease(gdContractAddress,gdPlatfromKeyID,eqCode,shareList,reason);
        if(bCheckOnchain) {
            JSONObject jsonObject = JSONObject.fromObject(response);
            String txId = jsonObject.getJSONObject("data").getString("txId");

            commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
            assertEquals("200", JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

            return result;
        }
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
    public void lockAndUnlock(String bizNo,String eqCode,String address,double lockAmount,int shareProperty,boolean bCheckOnchain) throws Exception{

        String cutoffDate = "20220930";
        //冻结
        lock(bizNo,address,eqCode,lockAmount,shareProperty,cutoffDate,bCheckOnchain);

        //解除冻结
        unlock(bizNo,eqCode,lockAmount,bCheckOnchain);
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
    public String lock(String bizNo,String address,String eqCode,double lockAmount,int shareProperty,String cutoffDate,boolean bCheckOnchain) throws Exception{

        String reason = "司法冻结";

        String response= gd.GDShareLock(bizNo,address,eqCode,lockAmount,shareProperty,reason,cutoffDate);

        if(bCheckOnchain) {
            JSONObject jsonObject = JSONObject.fromObject(response);
            String txId = jsonObject.getJSONObject("data").getString("txId");

            commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
            assertEquals("200", JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

            return result;
        }
        return response;

    }


    /***
     * 冻结解冻
     * @param bizNo  商务编号 唯一
     * @param eqCode  股权代码
     * @param unlockAmount    解除冻结数量
     * @throws Exception
     */
    public String unlock(String bizNo,String eqCode,double unlockAmount,boolean bCheckOnchain) throws Exception{

        String response= gd.GDShareUnlock(bizNo,eqCode,unlockAmount);

        if(bCheckOnchain) {
            JSONObject jsonObject = JSONObject.fromObject(response);
            String txId = jsonObject.getJSONObject("data").getString("txId");

            commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
            assertEquals("200", JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

            return result;
        }
        return response;

    }

    /***
     * 场内转板接口
     * @param oldEquityCode  转前股权代码
     * @param newEquityCode  转后股权代码
     * @throws Exception
     */
    public String changeBoard(String oldEquityCode,String newEquityCode,boolean bCheckOnchain) throws Exception{

        String response= gd.GDShareChangeBoard(gdPlatfromKeyID,gdCompanyID,oldEquityCode,newEquityCode);

        if(bCheckOnchain) {
            JSONObject jsonObject = JSONObject.fromObject(response);
            String txId = jsonObject.getJSONObject("data").getString("txId");

            commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
            assertEquals("200", JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));
            gdEquityCode = newEquityCode;
            return result;
        }

        return response;
    }

    /***
     * 回收/减资
     * @param eqCode   回收的股权代码
     * @param shareList  回收地址账户列表
     * @throws Exception
     */
    public String shareRecycle(String eqCode ,List<Map> shareList,boolean bCheckOnchain) throws Exception {
        String remark = "777777";
        String response= gd.GDShareRecycle(gdPlatfromKeyID,eqCode,shareList,remark);

        if(bCheckOnchain) {
            JSONObject jsonObject = JSONObject.fromObject(response);
            String txId = jsonObject.getJSONObject("data").getString("txId");

            commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
            assertEquals("200", JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

            return result;
        }
        return response;
    }


    /***
     * 销户接口
     * @param clntNo   客户号
     * @throws Exception
     */
    public String destroyAcc(String clntNo,boolean bCheckOnchain) throws Exception {
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

    public Map<String,String> ParseShareList(String response) throws Exception{
        Map<String,String> mapShareList = new HashMap<>();
        JSONObject jsonObjectData = JSONObject.fromObject(response);


        return mapShareList;
    }

}
