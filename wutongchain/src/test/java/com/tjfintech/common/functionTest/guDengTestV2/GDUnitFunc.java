package com.tjfintech.common.functionTest.guDengTestV2;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.GuDeng;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.FileOperation;
import com.tjfintech.common.utils.MinIOOperation;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.junit.runners.MethodSorters;
import org.springframework.util.StringUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.tjfintech.common.functionTest.guDengTestV2.GDCommonFunc.*;
import static com.tjfintech.common.functionTest.guDengTestV2.GDCommonFunc.replaceCertain;
import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassGD.*;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class GDUnitFunc {

    TestBuilder testBuilder= TestBuilder.getInstance();
    GuDeng gd =testBuilder.getGuDeng();
    Store store =testBuilder.getStore();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    GDCommonFunc gdCF = new GDCommonFunc();
    String result = "check on chain success";
    SimpleDateFormat sdfSec = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    GDBeforeCondition gdBF = new GDBeforeCondition();

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public String createAcc(String clientNo,Boolean bCheckOnchain)throws Exception{
        GDBeforeCondition gdBC = new GDBeforeCondition();
        Map<String,String> mapAcc = gdBC.gdCreateAccParam(clientNo);
        String response = mapAcc.get("response");
        if(bCheckOnchain) {
            JSONObject jsonObject = JSONObject.fromObject(mapAcc.get("response"));
            String txId = jsonObject.getJSONObject("data").getString("txId");

            commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
            assertEquals("200", JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));
        }
        return response;
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
                                 int oldProperty,int newProperty,boolean bCheckOnchain) throws Exception{
        log.info("股权性质变更");
        Map testReg1 = gdBF.init05RegInfo();
        Map testReg2 = gdBF.init05RegInfo();
        String regObjId1 = mapAccAddr.get(address) + "CProp1" + Random(6);
        String regObjId2 = mapAccAddr.get(address) + "CProp2" + Random(6);
        testReg1.put("register_account_obj_id",mapAccAddr.get(address));
        testReg1.put("register_registration_object_id",regObjId1);

        testReg2.put("register_account_obj_id",mapAccAddr.get(address));
        testReg2.put("register_registration_object_id",regObjId2);

        List<Map> regListInfo = new ArrayList<>();
        regListInfo.add(testReg1);
        regListInfo.add(testReg2);

        String response= gd.GDShareChangeProperty(gdPlatfromKeyID,address,eqCode,changeAmount,oldProperty,newProperty,regListInfo);

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
     * @throws Exception
     */
    public String shareTransfer(String keyID,String fromAddr,long amount,String toAddr,int shareProperty,String eqCode,
            boolean bCheckOnchain) throws Exception{
        log.info("股权代码过户转让");
        //交易报告数据
        Map txInfo = gdBF.init04TxInfo();
        String txRpObjId = "txReport" + Random(10);
        txInfo.put("transaction_object_id",txRpObjId);
//        txInfo.remove("transaction_object_id");

        //登记数据
        String tempObjIdFrom = "reg" + mapAccAddr.get(gdAccount1).toString() + Random(9);
        String tempObjIdTo = "reg" + mapAccAddr.get(gdAccount5).toString() + Random(9);

        register_transaction_ref = txRpObjId;//登记引用的是交易报告的对象标识

        Map fromNow = gdBF.init05RegInfo();
        Map toNow = gdBF.init05RegInfo();

        fromNow.put("register_registration_object_id",tempObjIdFrom);
        toNow.put("register_registration_object_id",tempObjIdTo);

//        fromNow.remove("register_registration_object_id");
//        toNow.remove("register_registration_object_id");

        String response= gd.GDShareTransfer(keyID,fromAddr,amount,toAddr,shareProperty,eqCode,txInfo, fromNow,toNow);

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

    public void commonIssuePP0(long amount)throws Exception{
//        gdEquityCode = "gdEC" + Random(12);

        List<Map> shareList = gdConstructShareList(gdAccount1,amount,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,amount,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,amount,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,amount,0, shareList3);

        //发行
        shareIssue(gdEquityCode, shareList4, true);
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
    }

    public void commonIssuePP01(long amount)throws Exception{
//        gdEquityCode = "gdEC" + Random(12);
        register_product_ref = gdEquityCode;

        List<Map> shareList = gdConstructShareList(gdAccount1,amount,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,amount,1, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,amount,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,amount,1, shareList3);

        //发行

        shareIssue(gdEquityCode, shareList4, true);
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
    }

    /***
     * 股份初始登记
     * @param eqCode  待发股权代码
     * @param shareList  待发列表
     * @throws Exception
     */
    public String shareIssue(String eqCode,List<Map> shareList,boolean bCheckOnchain) throws Exception{
        log.info("挂牌登记");
        enterpriseReg(eqCode,true);
        String query2 = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);

        log.info("发行");
        String response= gd.GDShareIssue(gdContractAddress,gdPlatfromKeyID,eqCode,shareList);
        if(bCheckOnchain) {
            JSONObject jsonObject = JSONObject.fromObject(response);
            String txId = jsonObject.getJSONObject("data").getString("txId");

            sleepAndSaveInfo(3000);

//            commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
//            assertEquals("200", JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

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
    public String shareIncrease(String eqCode,List<Map> shareList,boolean bCheckOnchain) throws Exception{

        String reason = "股份分红";
        String txObjId = "4increaseObj" + Random(6);

        Map eqProd = gdBF.init03EquityProductInfo();
        Map txInfo = gdBF.init04TxInfo();
        txInfo.put("transaction_object_id",txObjId);
        register_transaction_ref = txObjId; //此处为发行融资 设置登记引用接口中的交易报告

        String response= gd.GDShareIncrease(gdPlatfromKeyID,eqCode,shareList,reason, eqProd,txInfo);
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
                       String cutoffDate,boolean bCheckOnchain) throws Exception{
        log.info("股份冻结");
        String reason = "司法冻结";

        //登记数据
        Map regInfo = gdBF.init05RegInfo();
        String tempObjId = mapAccAddr.get(gdAccount1).toString() + Random(5);
        regInfo.put("register_registration_object_id",tempObjId);

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
    public String unlock(String bizNo,String eqCode,long unlockAmount,boolean bCheckOnchain) throws Exception{
        log.info("股份解除冻结");

        //登记数据
        Map regInfo = gdBF.init05RegInfo();
        String tempObjId = mapAccAddr.get(gdAccount1).toString() + Random(5);
        regInfo.put("register_registration_object_id",tempObjId);

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

    public void lockAndUnlock(String bizNo,String eqCode,String addr,long amount,int shareProperty)throws Exception{
        lock(bizNo,addr,eqCode,amount,shareProperty,"2025-09-13",true);
        unlock(bizNo,eqCode,amount,true);
    }

    /***
     * 场内转板接口
     * @param oldEquityCode  转前股权代码
     * @param newEquityCode  转后股权代码
     * @throws Exception
     */
    public String changeBoard(String oldEquityCode,String newEquityCode,boolean bCheckOnchain) throws Exception{
        log.info("场内转板");
        product_issuer_subject_ref = gdCompanyID;
        Map mapOldProd = gdBF.init03EquityProductInfo();
        mapOldProd.put("product_object_id",oldEquityCode);
        Map mapNewProd = gdBF.init03EquityProductInfo();
        mapNewProd.put("product_object_id",newEquityCode);
        List<Map> regListTemp = getAllHolderListReg(oldEquityCode,"cbSpec" + Random(10));
        String response= gd.GDShareChangeBoard(gdPlatfromKeyID,gdCompanyID,oldEquityCode,newEquityCode,regListTemp, mapOldProd,mapNewProd);

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
        String name = "销户代理人姓名1";
        String number = "销户代理人电话1";
        String response= gd.GDAccountDestroy(gdContractAddress,clntNo,date1,getListFileObj(),date2,getListFileObj(),name,number);

        if(bCheckOnchain) {
            JSONObject jsonObject = JSONObject.fromObject(response);
            String txId = jsonObject.getJSONObject("data").getString("txId");

            commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
            assertEquals("200", JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

            return result;
        }
        return response;

    }

    public String enterpriseReg(String eqCode,Boolean bCheckOnchain)throws Exception{
        Map enSub = gdBF.init01EnterpriseSubjectInfo();
        Map mapProd = null;
        String response = "";
        switch (productType){
            case "1":
                mapProd = gdBF.init03EquityProductInfo();
                response= gd.GDEnterpriseResister(gdContractAddress,eqCode,1000000,enSub, mapProd,null,null);
                break;
            case "2":
                mapProd = gdBF.init03BondProductInfo();
                response= gd.GDEnterpriseResister(gdContractAddress,eqCode,1000000,enSub, null,mapProd,null);
                break;
            case "3":
                mapProd = gdBF.init03FundProductInfo();
                response= gd.GDEnterpriseResister(gdContractAddress,eqCode,1000000,enSub, null,null,mapProd);
                break;
                default:
                    //默认股权类
                    mapProd = gdBF.init03EquityProductInfo();
                    response= gd.GDEnterpriseResister(gdContractAddress,eqCode,1000000,enSub, mapProd,null,null);

        }


        if(bCheckOnchain) {
            JSONObject jsonObject = JSONObject.fromObject(response);
            String txId = jsonObject.getJSONObject("data").getString("txId");

            commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
            assertEquals("200", JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));

            return result;
        }
        return response;
    }

    public List<Map> getAllHolderListReg(String equityCode ,String flowNo){
        List<Map> regList = new ArrayList<>();
        GDBeforeCondition gdBF = new GDBeforeCondition();

        //获取股东列表
        String Qry = gd.GDGetEnterpriseShareInfo(equityCode);
        if(!JSONObject.fromObject(Qry).getString("state").equals("200")){
//            regList.add(registerInfo);
            return  regList;
        }
//        assertEquals("200",JSONObject.fromObject(Qry).getString("state"));

        JSONArray holderList = JSONObject.fromObject(Qry).getJSONArray("data");

        for(int i = 0;i < holderList.size(); i ++){
            String tempAddr = JSONObject.fromObject(holderList.get(i)).getString("address");
            if(tempAddr.equals(zeroAccount)) continue;
            String tempPP = JSONObject.fromObject(holderList.get(i)).getString("shareProperty");
            String cltNo = mapAccAddr.get(tempAddr).toString();
            String tempObjId = cltNo + Random(4);
            log.info("检查发行存证登记格式化及信息内容与传入一致:" + tempObjId);
            Map tempReg =  gdBF.init05RegInfo();
            String regObjId = mapAccAddr.get(tempAddr) + Random(6);
            GDBeforeCondition gdbf = new GDBeforeCondition();

            tempReg.put("register_registration_object_id",regObjId);
            if(regObjType == 1){
            tempReg.put("register_subject_account_ref","SH" + cltNo);}
            tempReg.put("register_product_ref",gdEquityCode);

            mapAddrRegObjId.put(tempAddr + tempPP,regObjId);//方便后面测试验证
            regList.add(tempReg);
        }

        return regList;
    }

    public List<Map> getAllHolderListRegWithExistRegObjID(String equityCode ,String flowNo){
        List<Map> regList = new ArrayList<>();
        GDBeforeCondition gdBF = new GDBeforeCondition();

        //获取股东列表
        String Qry = gd.GDGetEnterpriseShareInfo(equityCode);
        if(!JSONObject.fromObject(Qry).getString("state").equals("200")){
//            regList.add(registerInfo);
            return  regList;
        }
//        assertEquals("200",JSONObject.fromObject(Qry).getString("state"));

        JSONArray holderList = JSONObject.fromObject(Qry).getJSONArray("data");

        for(int i = 0;i < holderList.size(); i ++){
            String tempAddr = JSONObject.fromObject(holderList.get(i)).getString("address");
            if(tempAddr.equals(zeroAccount)) continue;
            String tempPP = JSONObject.fromObject(holderList.get(i)).getString("shareProperty");
            String cltNo = mapAccAddr.get(tempAddr).toString();
//            String tempObjId = cltNo + Random(4);
//            log.info("检查发行存证登记格式化及信息内容与传入一致:" + tempObjId);
            Map tempReg =  gdBF.init05RegInfo();
            log.info(tempAddr + tempPP);
            String regObjId = mapAddrRegObjId.get(tempAddr + tempPP).toString();//获取已存在的
//            GDBeforeCondition gdbf = new GDBeforeCondition();

            tempReg.put("register_registration_object_id",regObjId);
            if(regObjType == 1){
                tempReg.put("register_subject_account_ref","SH" + cltNo);}
            tempReg.put("register_product_ref",gdEquityCode);

            mapAddrRegObjId.put(tempAddr + tempPP,regObjId);//方便后面测试验证
            regList.add(tempReg);
        }

        return regList;
    }

    public void calJGData()throws Exception{
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String sdStart = sdf.format((new Date()).getTime()); // 时间戳转换日期
        String saveFile = testResultPath + "JGData/" + timeStamp + "_JG.txt";
        int height = JSONObject.fromObject(store.GetHeight()).getInt("data");
        log.info("check begin height " + blockHeight + " and end height " + height);
        ArrayList<String> txStore = new ArrayList<>();
        if(height > blockHeight){
            txStore = commonFunc.getTxArrayWithKeyWord(
                    commonFunc.getTxFromBlock(blockHeight + 1,height),"\"type\":0");
        }
        int[] dataNum = new int[7]; //分别对应 主体/账户/产品/交易报告/登记/资金结算/信披
        for(int i = 0;i < txStore.size();i++){
            String response = store.GetTxDetail(txStore.get(i));
            String txType = "\\\"type\\\":\\\"主体\\\"";
            dataNum[0] = dataNum[0] + StringUtils.countOccurrencesOf(response,txType);
            txType = "\\\"type\\\":\\\"账户\\\"";
            dataNum[1] = dataNum[1] + StringUtils.countOccurrencesOf(response,txType);
            txType = "\\\"type\\\":\\\"产品\\\"";
            dataNum[2] = dataNum[2] + StringUtils.countOccurrencesOf(response,txType);
            txType = "\\\"type\\\":\\\"交易报告\\\"";
            dataNum[3] = dataNum[3] + StringUtils.countOccurrencesOf(response,txType);
            txType = "\\\"type\\\":\\\"登记\\\"";
            dataNum[4] = dataNum[4] + StringUtils.countOccurrencesOf(response,txType);
            txType = "\\\"type\\\":\\\"资金结算\\\"";
            dataNum[5] = dataNum[5] + StringUtils.countOccurrencesOf(response,txType);
            txType = "\\\"type\\\":\\\"信披\\\"";
            dataNum[6] = dataNum[6] + StringUtils.countOccurrencesOf(response,txType);
        }

        String dataTopic = "获取时间\t\t\t\t起始高度\t结束高度\t主体\t账户\t产品\t交易报告\t登记\t资金结算\t信披";
        String data = sdStart + "\t\t" + (blockHeight + 1) + "\t\t" + height + "\t\t" +
                dataNum[0] + "\t\t" + dataNum[1]+ "\t\t" + dataNum[2] + "\t\t" + dataNum[3]+
                "\t\t\t" + dataNum[4] + "\t\t" + dataNum[5]+ "\t\t\t" + dataNum[6];
        FileOperation fo = new FileOperation();
        String get = FileOperation.read(saveFile);
        if(!get.contains(subjectType))    fo.appendToFile(dataTopic,saveFile);
        fo.appendToFile(data,saveFile);


        blockHeight = height;
    }

    public void calJGDataEachHeight()throws Exception{
        String testClassName = Thread.currentThread().getStackTrace()[2].getClassName();
        testClassName = testClassName.substring(testClassName.lastIndexOf(".")+1);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String saveFile = testResultPath + "JGData/" + sdf2.format(timeStamp)  + "_" + testClassName + "_JG.txt";

        int height = JSONObject.fromObject(store.GetHeight()).getInt("data");
        log.info("check begin height " + blockHeight + " and end height " + height);

        String dataTopic = "获取时间\t\t\t\t高度\t主体\t账户\t产品\t交易报告\t登记\t资金结算\t信披\t用例名称";
        FileOperation fo = new FileOperation();
        if((new File(saveFile)).exists()) {
            String get = FileOperation.read(saveFile);
            if (!get.contains(subjectType)) fo.appendToFile(dataTopic, saveFile);
        }
        else
            fo.appendToFile(dataTopic,saveFile);

        for(int k = blockHeight + 1;k <= height;k++){
            int[] dataNum = new int[7]; //分别对应 主体/账户/产品/交易报告/登记/资金结算/信披
            String [] txArr = commonFunc.getTxsArray(k);
            for(int m = 0;m<txArr.length;m++){
                String response = store.GetTxDetail(txArr[m]);
                if(!response.contains("\"type\":0")) continue;

                String txType = "\\\"type\\\":\\\"主体\\\"";
                dataNum[0] = dataNum[0] + StringUtils.countOccurrencesOf(response,txType);
                txType = "\\\"type\\\":\\\"账户\\\"";
                dataNum[1] = dataNum[1] + StringUtils.countOccurrencesOf(response,txType);
                txType = "\\\"type\\\":\\\"产品\\\"";
                dataNum[2] = dataNum[2] + StringUtils.countOccurrencesOf(response,txType);
                txType = "\\\"type\\\":\\\"交易报告\\\"";
                dataNum[3] = dataNum[3] + StringUtils.countOccurrencesOf(response,txType);
                txType = "\\\"type\\\":\\\"登记\\\"";
                dataNum[4] = dataNum[4] + StringUtils.countOccurrencesOf(response,txType);
                txType = "\\\"type\\\":\\\"资金结算\\\"";
                dataNum[5] = dataNum[5] + StringUtils.countOccurrencesOf(response,txType);
                txType = "\\\"type\\\":\\\"信批\\\"";
                dataNum[6] = dataNum[6] + StringUtils.countOccurrencesOf(response,txType);

            }
            String sdStart = sdf.format((new Date()).getTime()); // 时间戳转换日期
            String data = sdStart + "\t\t" + k + "\t" +
                    dataNum[0] + "\t\t" + dataNum[1]+ "\t\t" + dataNum[2] + "\t\t" + dataNum[3]+
                    "\t\t\t" + dataNum[4] + "\t\t" + dataNum[5]+ "\t\t\t" + dataNum[6] + "\t\t" + testCurMethodName;
            fo.appendToFile(data,saveFile);
        }
        blockHeight = height;
        //执行成功后更新文件中的参数
        FileOperation fo2 = new FileOperation();
        fo2.replaceKeyword(System.getProperty("user.dir")  +
                "\\src\\main\\java\\com\\tjfintech\\common\\utils\\UtilsClassGD.java",
                "public static int blockHeight =",
                "\tpublic static int blockHeight = " + blockHeight + ";");
    }

    public void updateBlockHeightParam(int height)throws Exception{
        //执行成功后更新文件中的参数
        FileOperation fo2 = new FileOperation();
        fo2.replaceKeyword(System.getProperty("user.dir")  +
                        "\\src\\main\\java\\com\\tjfintech\\common\\utils\\UtilsClassGD.java",
                "public static int blockHeight =",
                "\tpublic static int blockHeight = " + height + ";");
        blockHeight = height;
    }

    public void checkJGHeaderOpVer(int iStart,int iEnd)throws Exception{
        GDCommonFunc gdCF = new GDCommonFunc();

        String testClassName = Thread.currentThread().getStackTrace()[2].getClassName();
        testClassName = testClassName.substring(testClassName.lastIndexOf(".")+1);

        Map chkData = new HashMap();

        String saveFile = testResultPath + "JGData/" + testClassName + "_JGPrinciple.txt";
        FileOperation fo = new FileOperation();

        for(int i=iStart;i<=iEnd;i++) {
            Map temp = gdCF.findDataInBlock(i, "supervision");
            String storeData = temp.get("storeData").toString();
            if(storeData.equals("")) continue;
            //storeData是个List时
            if(storeData.contains("[")){
                for(int k=0;k<com.alibaba.fastjson.JSONArray.parseArray(storeData).size();k++) {
                    chkData = checkBlkUriDataJGPrinciple(com.alibaba.fastjson.JSONArray.parseArray(storeData).get(k).toString());
                    if(!chkData.isEmpty()){
                        //将当前时间 单元测试用例名 uri信息 报送的监管数据信息 写入文件
                        fo.appendToFile("//===================================================================",saveFile);
                        fo.appendToFile(sdfSec.format((new Date()).getTime()) + " test case " + testCurMethodName
                                + "\nblock height " + i + " \nuri " + chkData.get("uri").toString()
                                + "\n" + chkData.get("JGData").toString() ,saveFile );
                        bHeaderCalOK = bHeaderCalOK && false;
                    }
                }
            }
            else {
                //StoreData仅是一个JSON字符串时
                chkData =checkBlkUriDataJGPrinciple(storeData);
                if(!chkData.equals(null)){
                    //将当前时间 单元测试用例名 uri信息 报送的监管数据信息 写入文件
                    fo.appendToFile("**===================================================================",saveFile);
                    fo.appendToFile(sdfSec.format((new Date()).getTime()) + " test case " + testCurMethodName
                            + "\nblock height " + i + " \nuri " + chkData.get("uri").toString()
                            + "\n" + chkData.get("JGData").toString() ,saveFile );
                    bHeaderCalOK = bHeaderCalOK && false;
                }
            }

        }
        updateBlockHeightParam(iEnd);
        assertEquals("校验header operation & version 规则",true,bHeaderCalOK);
    }

    public Map checkBlkUriDataJGPrinciple(String storeData)throws Exception{
        Map mapData = new HashMap();
        MinIOOperation mo = new MinIOOperation();
        com.alibaba.fastjson.JSONObject jsonStore = com.alibaba.fastjson.JSONObject.parseObject(storeData);
        String uri = jsonStore.getString("uri");
        String data = mo.getFileFromMinIO(minIOEP,jgBucket,uri,"");
        log.info(uri + "  " + data);
//        FileOperation g = new FileOperation();
//        g.appendToFile(data,"chk.txt");
        Boolean bFlag = false;


        if(data.contains("\"operation\":\"create\"") && (!(
                data.contains("\"operation\":\"create\",\"version\":0") ||
                        data.contains("\"version\":0,\"operation\":\"create\"")))){
            bFlag = true;
        }

        if(data.contains("\"operation\":\"update\"") && (data.contains("\"operation\":\"update\",\"version\":0")
                || data.contains("\"version\":0,\"operation\":update"))){
            bFlag = true;
        }

        if(data.contains("\"operation\":\"delete\"") && (data.contains("\"operation\":\"delete\",\"version\":0")
                || data.contains("\"version\":0,\"operation\":delete"))){
            bFlag = true;
        }
        if(bFlag) {
//            log.info("检查uri " + uri + "\n" + data);
            mapData.put("uri" ,uri);
            mapData.put("JGData",data);
//            assertEquals(false,bFlag);
        }
        return mapData;
    }

    //检查包含特定字符的交易及区块信息
    public void storeDataCheckKeyWord(int iStart,int iEnd,String keyWord)throws Exception{
        GDCommonFunc gdCF = new GDCommonFunc();
        MinIOOperation mo = new MinIOOperation();
        for(int i=iStart;i<=iEnd;i++) {
            Map temp = gdCF.findDataInBlock(i, keyWord);
//            Map temp = findDataInBlock(i, "gdCmpyId01z3k4gF");
            String storeData = temp.get("storeData").toString();

//            log.info(storeData);

            if(storeData.contains(keyWord)) {
                log.info(storeData);
//                break;
            }

        }
    }


    //挂牌登记模块封装
    public void regTestUnit(String type,Boolean bSetEmpty,String subObjID)throws Exception{
        long shareTotals = 1000000;
        gdCompanyID = subObjID;
        Map enSubInfo = gdBF.init01EnterpriseSubjectInfo();
        Map prodInfo = null;

        int gdCpmIdOldVer = Integer.parseInt(gdCF.getObjectLatestVer(gdCompanyID));//获取当前挂牌主体最新版本信息
        String response= "";

        //根据产品类型获取产品信息并执行挂牌
        switch (type){
            case "1":   prodInfo = gdBF.init03EquityProductInfo();
                if(bSetEmpty) prodInfo.put("product_issuer_subject_ref","");
                response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,enSubInfo,
                        prodInfo,null,null);
                break;
            case "2":   prodInfo = gdBF.init03BondProductInfo();
                if(bSetEmpty) prodInfo.put("product_issuer_subject_ref","");
                response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,enSubInfo,
                        null,prodInfo,null);
                break;
            case "3":   prodInfo = gdBF.init03FundProductInfo();
                if(bSetEmpty) prodInfo.put("product_issuer_subject_ref","");
                response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,enSubInfo,
                        null,null,prodInfo);
                break;
            case "4":
                response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,enSubInfo,
                        null,null,null);
                break;
            default:    assertEquals("非法类型" + type, false,true);
        }

        //获取交易hash
        String txId = net.sf.json.JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        //判断交易上链
        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        String txDetail = store.GetTxDetail(txId);
        assertEquals("200", net.sf.json.JSONObject.fromObject(txDetail).getString("state"));

        //检查各个查询对象返回信息中不包含敏感词
        assertEquals("不包含敏感词",true,
                gdCF.chkSensitiveWord(txDetail,subjectType));
        assertEquals("不包含敏感词",true,
                gdCF.chkSensitiveWord(txDetail,prodType));

        //查询挂牌企业主体数据  交易上链后 数据可能还未写入合约 在此做2s内数据查询
        response = gd.GDMainSubjectQuery(gdContractAddress, gdCompanyID);
//        assertEquals("200", net.sf.json.JSONObject.fromObject(response).getString("state"));


        //设置各个主体版本变量
        String newSubVer = gdCF.getObjectLatestVer(gdCompanyID);
        String newEqProdVer = gdCF.getObjectLatestVer(gdEquityCode);

        String subSIQCRefVer = gdCF.getObjectLatestVer(subject_investor_qualification_certifier_ref);
        String prodPMSRefVer = gdCF.getObjectLatestVer(product_market_subject_ref);
        String prodSPSRefVer = gdCF.getObjectLatestVer(service_provider_subject_ref);

        //获取链上mini url的存证信息并检查是否包含uri信息
        String subfileName = conJGFileName(gdCompanyID,newSubVer);
        String prodfileName = conJGFileName(gdEquityCode,newEqProdVer);

        Map uriInfo = gdCF.getJGURIStoreHash(txId,conJGFileName(gdCompanyID,newSubVer),1);
        String chkSubURI = subfileName;
        String chkProdURI = prodfileName;
        log.info(uriInfo.get("storeData").toString());
        log.info(chkSubURI);
        assertEquals(true,uriInfo.get("storeData").toString().contains(chkSubURI));
        if(!type.equals("4")) assertEquals(true,uriInfo.get("storeData").toString().contains(chkProdURI));
        assertEquals(true,gdCF.bContainJGFlag(uriInfo.get("storeData").toString()));//确认meta信息包含监管关键字

        //直接从minio上通过对象标识+版本号的方式获取指定对象文件
        Map getSubInfo = gdCF.constructJGDataFromStr(conJGFileName(gdCompanyID,newSubVer),subjectType,"1");
        Map getProInfo = null;
        if(!type.equals("4")) getProInfo = gdCF.constructJGDataFromStr(conJGFileName(gdEquityCode, newEqProdVer), prodType, type);

        //填充header content字段
        log.info("********************************" + newSubVer);
        if(newSubVer.equals("0")) {
            enSubInfo.put("content", gdCF.constructContentTreeMap(subjectType, gdCompanyID, newSubVer, "create", String.valueOf(ts1)));
        }else {
            enSubInfo.put("content", gdCF.constructContentTreeMap(subjectType, gdCompanyID, newSubVer, "update", String.valueOf(ts1)));
        }
        //如果不是机构会员登记 则执行产品填充header content字段
        if(!type.equals("4")) {
            prodInfo.put("content",gdCF.constructContentTreeMap(prodType, gdEquityCode, newEqProdVer, "create", String.valueOf(ts3)));
        }

        //产品发行主体引用设置为空场景 当前代码会自动补充发行主体对象标识
        if(bSetEmpty) prodInfo.put("product_issuer_subject_ref", enSubInfo.get("subject_object_id").toString());
        //产品如下字段引用的是发行主体
        product_issuer_subject_ref = gdCompanyID;
//        assertEquals(String.valueOf(gdCpmIdOldVer + 1),gdCF.getObjectLatestVer(gdCompanyID));

        assertEquals(String.valueOf(gdCpmIdOldVer + 1),newSubVer);

        String[] verForSub = new String[]{"/" + subSIQCRefVer };
        String[] verForProd = new String[]{"/" + prodPMSRefVer,"/" + newSubVer,"/" + prodSPSRefVer};

        log.info("检查主体存证信息内容与传入一致\n" + enSubInfo.toString() + "\n" + getSubInfo.toString());
        assertEquals(replaceCertain(gdCF.matchRefMapCertVer(enSubInfo,subjectType,verForSub)),replaceCertain(getSubInfo.toString()));

        if(!type.equals("4")) {
            log.info("检查产品存证信息内容与传入一致\n" + prodInfo.toString() + "\n" + getProInfo.toString());
            assertEquals(replaceCertain(gdCF.matchRefMapCertVer(prodInfo, prodType, verForProd)), replaceCertain(getProInfo.toString()));
        }
    }


    //开户后挂牌登记模块封装
    public void CreateAcc_ThenRegTestUnit(String type,Boolean bSetEmpty)throws Exception{
                //挂牌机构开户
//        Map enSubInfo = gdBF.init01EnterpriseSubjectInfo();
//        createAccForEnterpriseSub(gdCompanyID,enSubInfo);
        createAcc(gdCompanyID,true);

        //机构挂牌登记
        regTestUnit(type,bSetEmpty,gdCompanyID);
    }

    //挂牌登记后开户模块封装
    public Map regTestUnit_ThenCreateAcc(String type,Boolean bSetEmpty)throws Exception{
        //机构挂牌登记
        regTestUnit(type,bSetEmpty,gdCompanyID);
        //挂牌机构开户
        Map enSubInfo = gdBF.init01EnterpriseSubjectInfo();
        return createAccForEnterpriseSub(gdCompanyID,enSubInfo);
    }

    //开户模块
    public Map createAccForEnterpriseSub(String subObjID,Map enSubInfo)throws Exception{
        int newSubVer = Integer.parseInt(gdCF.getObjectLatestVer(subObjID));
        //挂牌后开户
        String cltNo = subObjID;
        String shareHolderNo = "SH" + cltNo;
        String fundNo = "fund" + cltNo;

        //构造股权账户信息
        Map shareHolderInfo = new HashMap();
        Map mapSHAcc = gdBF.init02ShareholderAccountInfo();
        mapSHAcc.put("account_object_id", shareHolderNo);  //更新账户对象标识字段
        mapSHAcc.put("account_subject_ref", cltNo);  //更新账户所属主体引用 -- 需修改为机构主体id

        log.info(shAccountInfo.toString());
        shareHolderInfo.put("createTime", ts2);
        shareHolderInfo.put("shareholderNo", shareHolderNo);
        shareHolderInfo.put("accountInfo", mapSHAcc);
        log.info(shareHolderInfo.toString());

        //资金账户信息
        Map mapFundAcc = gdBF.init02FundAccountInfo();
        mapFundAcc.put("account_object_id", fundNo);  //更新账户对象标识字段
        mapFundAcc.put("account_subject_ref", cltNo);  //更新账户所属主体引用 -- 需修改为机构主体id
        mapFundAcc.put("account_associated_account_ref", shareHolderNo);  //更新关联账户对象引用

        Map mapFundInfo = new HashMap();
        mapFundInfo.put("createTime", ts2);
        mapFundInfo.put("fundNo", fundNo);
        mapFundInfo.put("accountInfo", mapFundAcc);

        //构造个人/投资者主体信息
        Map investor = enSubInfo;//开户时仍然使用挂牌时主体信息

        String response = gd.GDCreateAccout(gdContractAddress, cltNo, mapFundInfo, shareHolderInfo, investor);
        String txId = net.sf.json.JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        //判断交易上链
        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        String txDetail = store.GetTxDetail(txId);
        assertEquals("200", net.sf.json.JSONObject.fromObject(txDetail).getString("state"));

        //将开户相关信息存储
        String keyID = JSONObject.fromObject(response).getJSONObject("data").getJSONObject("accountList").getString("keyId");
        String addr = JSONObject.fromObject(response).getJSONObject("data").getJSONObject("accountList").getString("address");
        Map mapAccInfo = new HashMap();
        mapAccInfo.put("keyID", keyID);
        mapAccInfo.put("accout", addr);
        mapAccInfo.put("txId", txId);
        mapAccInfo.put("shareholderNo", shareHolderNo);
        mapAccInfo.put("fundNo", fundNo);
        mapAccInfo.put("response", response);
        mapAccAddr.put(addr, cltNo);


        account_associated_account_ref = shareHolderNo;

        //检查各个查询对象返回信息中不包含敏感词
        assertEquals("不包含敏感词",true,
                gdCF.chkSensitiveWord(txDetail,subjectType));
        assertEquals("不包含敏感词",true,
                gdCF.chkSensitiveWord(txDetail,accType));

        //查询投资者账户信息
        response = gd.GDGetShareHolderInfo(gdContractAddress,cltNo);
        assertEquals("200", net.sf.json.JSONObject.fromObject(response).getString("state"));


        //定义相关对象标识版本变量
        String accASrefVer = gdCF.getObjectLatestVer(account_subject_ref);
        String accADrefVer =  gdCF.getObjectLatestVer(account_depository_ref);
        String accAAARefVer =  gdCF.getObjectLatestVer(account_associated_account_ref);

        String shAccVer =  gdCF.getObjectLatestVer("SH" + cltNo);
        String fundAccVer =  gdCF.getObjectLatestVer("fund" + cltNo);
        String personSubVer =  gdCF.getObjectLatestVer(gdCompanyID);

        String subSIQCRefVer = gdCF.getObjectLatestVer(subject_investor_qualification_certifier_ref);

        String fundObjId = fundNo;
        String SHObjId = shareHolderNo;
        //获取链上mini url的存证信息 并检查是否包含uri信息
        String subfileName = conJGFileName(cltNo,personSubVer);
        String shAccfileName = conJGFileName(SHObjId,shAccVer);
        String fundAccfileName = conJGFileName(fundObjId,fundAccVer);

        Map uriInfo = gdCF.getJGURIStoreHash(txId,subfileName,1);
        String chkSubURI = subfileName;
        String chkSHAccURI = shAccfileName;
        String chkFundAccURI = fundAccfileName;
        log.info(uriInfo.get("storeData").toString());
        log.info(chkSubURI);
        assertEquals(true,uriInfo.get("storeData").toString().contains(chkSubURI));
        assertEquals(true,uriInfo.get("storeData").toString().contains(chkSHAccURI));
        assertEquals(true,uriInfo.get("storeData").toString().contains(chkFundAccURI));
        assertEquals(true,gdCF.bContainJGFlag(uriInfo.get("storeData").toString()));//确认meta信息包含监管关键字


        //直接从minio上获取报送数据文件信息
        Map getSubInfo = gdCF.constructJGDataFromStr(subfileName,subjectType,"1");//还是主体机构
        Map getFundAccInfo = gdCF.constructJGDataFromStr(fundAccfileName,accType,"2");
        Map getSHAccInfo = gdCF.constructJGDataFromStr(shAccfileName,accType,"1");


        Map accFund = gdBF.init02FundAccountInfo();
        Map accSH = gdBF.init02ShareholderAccountInfo();

//        enSubInfoBk.put("subject_object_id",cltNo);

        //填充header content 信息
        enSubInfo.put("content",gdCF.constructContentTreeMap(subjectType,gdCompanyID,personSubVer,"update",String.valueOf(ts1)));
        accFund.put("content",gdCF.constructContentTreeMap(accType,fundObjId,fundAccVer,"create",String.valueOf(ts2)));
        accSH.put("content",gdCF.constructContentTreeMap(accType,SHObjId,shAccVer,"create",String.valueOf(ts2)));

        assertEquals(String.valueOf(Integer.valueOf(newSubVer) + 1),personSubVer);

        //引用机构主体对象标识
        account_subject_ref = cltNo;

        //需要将比较的对象标识增加版本号信息
        String[] verForSub = new String[]{"/" + subSIQCRefVer};
        String[] verForAccSH = new String[]{"/" + personSubVer,"/" + accADrefVer,"/" + accAAARefVer};


        log.info("检查主体存证信息内容与传入一致\n" + enSubInfo.toString() + "\n" + getSubInfo.toString());
        assertEquals(replaceCertain(gdCF.matchRefMapCertVer(enSubInfo,subjectType,verForSub)),replaceCertain(getSubInfo.toString()));

        accSH.put("account_object_id",SHObjId);
        accSH.put("account_subject_ref",cltNo);
        log.info("检查股权账户存证信息内容与传入一致\n" + accSH.toString() + "\n" + getSHAccInfo.toString());
        assertEquals(replaceCertain(gdCF.matchRefMapCertVer2(accSH,accType)),replaceCertain(getSHAccInfo.toString()));

        account_associated_account_ref = SHObjId;
        String[] verForAccFund = new String[]{"/" + personSubVer,"/" + accADrefVer,"/" + shAccVer};

        accFund.put("account_object_id",fundObjId);
        accFund.put("account_subject_ref",cltNo);
        accFund.put("account_associated_account_ref",SHObjId);
        log.info("检查资金账户存证信息内容与传入一致\n" + accFund.toString() + "\n" + getFundAccInfo.toString());
        assertEquals(replaceCertain(gdCF.matchRefMapCertVer2(accFund,accType)),replaceCertain(getFundAccInfo.toString()));

        return mapAccInfo;
    }
}
