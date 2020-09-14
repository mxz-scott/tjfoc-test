package com.tjfintech.common;


import com.tjfintech.common.Interface.GuDengV1;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.junit.Assert.assertEquals;


@Slf4j

public class GDBeforeConditionV1 {
    TestBuilder testBuilder = TestBuilder.getInstance();
    GuDengV1 gd = testBuilder.getGuDengV1();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    Store store = testBuilder.getStore();


    //赋值权限999 区分是否主子链
    public void setPermission999()throws Exception{

        String toolPath="cd "+ ToolPATH +";";
        String exeCmd="./" + ToolTPName + " permission ";

        SDKID=utilsClass.getSDKID();
        String ledger ="";
        ledger=(subLedger!="")?" -z "+subLedger:"";
        String preCmd=toolPath+exeCmd+"-p "+PEER1RPCPort+" -s SDK "+ledger+" -d "+SDKID+" -m ";
        String getPerm=toolPath+"./" + ToolTPName + " getpermission -p "+PEER1RPCPort + " -d " + SDKID + ledger;


        //如果没有权限 则设置权限  修改为设置 兼容版本升级时 权限列表变更需要重新赋权限的问题
//        if(!shExeAndReturn(PEER1IP,getPerm).contains(fullPerm)){
            assertEquals(true,shExeAndReturn(PEER1IP,preCmd + "999").contains("success"));
            sleepAndSaveInfo(SLEEPTIME);
            assertEquals(true,shExeAndReturn(PEER1IP,getPerm).contains(fullPerm));
//        }
    }

    public void clearDataSetPerm999() throws Exception{
        utilsClass.delDataBase();//清空sdk当前使用数据库数据
        //设置节点 清空db数据 并重启
        utilsClass.setAndRestartPeerList(clearPeerDB,resetPeerBase);
        //重启SDK
        utilsClass.setAndRestartSDK();

        setPermission999();
    }

    @Test
    public void gdCreateAccout()throws Exception{
        String cltNo = gdAccClientNo1;
        String shareHolderNo = "SH" + cltNo;
        String clientName = "name" + cltNo;
        String fundNo = "fund" + cltNo;

        Map mapPersonInfo = new HashMap();
        mapPersonInfo.put("clientFullName",clientName);
        mapPersonInfo.put("organizationType","苏州股权代码");
        mapPersonInfo.put("certificateType",0);
        mapPersonInfo.put("certificateNo","123456468123153");
        mapPersonInfo.put("certificateAddress","certificateAddress");
        mapPersonInfo.put("gender",0);
        mapPersonInfo.put("telephone","051266616688");
        mapPersonInfo.put("phone","1598222555555");
        mapPersonInfo.put("postalCode","200120");
        mapPersonInfo.put("contactAddress","人民币");
        mapPersonInfo.put("mailBox","www@163.com");
        mapPersonInfo.put("fax","051266616688");
        mapPersonInfo.put("equityCode",gdEquityCode);
        mapPersonInfo.put("equityAmount",5000000);
        mapPersonInfo.put("shareProperty",0);

        Map mapinvestor = new HashMap();
        mapinvestor.put("salesDepartment","业务一部");
        mapinvestor.put("clientGroups","群组");
        mapinvestor.put("equityAccountNo","111111");
        mapinvestor.put("currency","人民币");
        mapinvestor.put("board","E板");
        mapinvestor.put("accountType",0);
        mapinvestor.put("accountStatus",0);
        mapinvestor.put("registrationDate","20200828");
        mapinvestor.put("lastTradingDate","20200828");
        mapinvestor.put("closingDate","20200828");
        mapinvestor.put("shareholderAmount",3);

        String extend = "";

        Map mapInvestorInfo = new HashMap();

        mapInvestorInfo.put("clientName",clientName);
        mapInvestorInfo.put("shareholderNo",shareHolderNo);
        mapInvestorInfo.put("fundNo",fundNo);
        mapInvestorInfo.put("clientNo",cltNo);
        mapInvestorInfo.put("extend",extend);
        mapInvestorInfo.put("personalInfo",mapPersonInfo);
        mapInvestorInfo.put("investor",mapinvestor);

        //创建第1个账户
        Map mapAcc = new HashMap();
        mapAcc = gdCreateAccParam(cltNo,mapPersonInfo,mapInvestorInfo);
        gdAccountKeyID1 = mapAcc.get("keyID").toString();
        gdAccount1 = mapAcc.get("accout").toString();
        String txId1 = mapAcc.get("txId").toString();

        //创建第2个账户
        mapAcc.clear();
        cltNo = gdAccClientNo2;
        mapAcc = gdCreateAccParam(cltNo,mapPersonInfo,mapInvestorInfo);
        gdAccountKeyID2 = mapAcc.get("keyID").toString();
        gdAccount2 = mapAcc.get("accout").toString();
        String txId2 = mapAcc.get("txId").toString();

        //创建第3个账户
        mapAcc.clear();
        cltNo = gdAccClientNo3;
        mapAcc = gdCreateAccParam(cltNo,mapPersonInfo,mapInvestorInfo);
        gdAccountKeyID3 = mapAcc.get("keyID").toString();
        gdAccount3 = mapAcc.get("accout").toString();
        String txId3 = mapAcc.get("txId").toString();

        //创建第4个账户
        mapAcc.clear();
        cltNo = gdAccClientNo4;
        mapAcc = gdCreateAccParam(cltNo,mapPersonInfo,mapInvestorInfo);
        gdAccountKeyID4 = mapAcc.get("keyID").toString();
        gdAccount4 = mapAcc.get("accout").toString();
        String txId4 = mapAcc.get("txId").toString();

        //创建第5个账户
        mapAcc.clear();
        cltNo = gdAccClientNo5;
        mapAcc = gdCreateAccParam(cltNo,mapPersonInfo,mapInvestorInfo);
        gdAccountKeyID5 = mapAcc.get("keyID").toString();
        gdAccount5 = mapAcc.get("accout").toString();
        String txId5 = mapAcc.get("txId").toString();

        //创建第6个账户
        mapAcc.clear();
        cltNo = gdAccClientNo6;
        mapAcc = gdCreateAccParam(cltNo,mapPersonInfo,mapInvestorInfo);
        gdAccountKeyID6 = mapAcc.get("keyID").toString();
        gdAccount6 = mapAcc.get("accout").toString();
        String txId6 = mapAcc.get("txId").toString();

        //创建第7个账户
        mapAcc.clear();
        cltNo = gdAccClientNo7;
        mapAcc = gdCreateAccParam(cltNo,mapPersonInfo,mapInvestorInfo);
        gdAccountKeyID7 = mapAcc.get("keyID").toString();
        gdAccount7 = mapAcc.get("accout").toString();
        String txId7 = mapAcc.get("txId").toString();

        //创建第8个账户
        mapAcc.clear();
        cltNo = gdAccClientNo8;
        mapAcc = gdCreateAccParam(cltNo,mapPersonInfo,mapInvestorInfo);
        gdAccountKeyID8 = mapAcc.get("keyID").toString();
        gdAccount8 = mapAcc.get("accout").toString();
        String txId8 = mapAcc.get("txId").toString();

        //创建第9个账户
        mapAcc.clear();
        cltNo = gdAccClientNo9;
        mapAcc = gdCreateAccParam(cltNo,mapPersonInfo,mapInvestorInfo);
        gdAccountKeyID9 = mapAcc.get("keyID").toString();
        gdAccount9 = mapAcc.get("accout").toString();
        String txId9 = mapAcc.get("txId").toString();

        //创建第10个账户
        mapAcc.clear();
        cltNo = gdAccClientNo10;
        mapAcc = gdCreateAccParam(cltNo,mapPersonInfo,mapInvestorInfo);
        gdAccountKeyID10 = mapAcc.get("keyID").toString();
        gdAccount10 = mapAcc.get("accout").toString();
        String txId10 = mapAcc.get("txId").toString();

        commonFunc.sdkCheckTxOrSleep(txId10,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        //判断所有开户接口交易上链
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId1)).getString("state"));
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId2)).getString("state"));
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId3)).getString("state"));
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId4)).getString("state"));
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId5)).getString("state"));
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId6)).getString("state"));
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId7)).getString("state"));
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId8)).getString("state"));
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId9)).getString("state"));
        assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txId10)).getString("state"));


    }

    public Map<String,String> gdCreateAccParam(String clientNo,Map mapPersonInfo,Map mapInvestorInfo){
        String cltNo = clientNo;
        String shareHolderNo = "SH" + cltNo;
        String clientName = "name" + cltNo;
        String fundNo = "fund" + cltNo;

        mapPersonInfo.put("clientFullName",clientName);//更新full name

        mapInvestorInfo.put("clientName",clientName);
        mapInvestorInfo.put("shareholderNo",shareHolderNo);
        mapInvestorInfo.put("fundNo",fundNo);
        mapInvestorInfo.put("clientNo",cltNo);
        mapInvestorInfo.put("personalInfo",mapPersonInfo);

        String response = gd.GDCreateAccout(gdContractAddress,mapInvestorInfo);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        assertEquals(cltNo,JSONObject.fromObject(response).getJSONObject("data").getJSONObject("accountList").getString("clientNo"));
        assertEquals(shareHolderNo,JSONObject.fromObject(response).getJSONObject("data").getJSONObject("accountList").getString("shareholderNo"));
        String keyID = JSONObject.fromObject(response).getJSONObject("data").getJSONObject("accountList").getString("keyId");
        String addr= JSONObject.fromObject(response).getJSONObject("data").getJSONObject("accountList").getString("address");

        Map mapAccInfo = new HashMap();
        mapAccInfo.put("keyID",keyID);
        mapAccInfo.put("accout",addr);
        mapAccInfo.put("txId",txId);

        return mapAccInfo;
    }


}
