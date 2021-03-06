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
     * ??????????????????
     * @param eqCode    ????????????
     * @param address    ????????????
     * @param changeAmount  ????????????
     * @param oldProperty  ?????????????????????
     * @param newProperty  ?????????????????????
     * @throws Exception
     */
    public String changeSHProperty(String address,String eqCode,long changeAmount,
                                 int oldProperty,int newProperty,boolean bCheckOnchain) throws Exception{
        log.info("??????????????????");
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
     * ????????????
     * @param keyID  ?????????????????????keyID
     * @param fromAddr   ????????????
     * @param amount    ????????????
     * @param toAddr    ????????????
     * @param shareProperty  ??????????????????
     * @param eqCode  ????????????
     * @throws Exception
     */
    public String shareTransfer(String keyID,String fromAddr,long amount,String toAddr,int shareProperty,String eqCode,
            boolean bCheckOnchain) throws Exception{
        log.info("????????????????????????");
        //??????????????????
        Map txInfo = gdBF.init04TxInfo();
        String txRpObjId = "txReport" + Random(10);
        txInfo.put("transaction_object_id",txRpObjId);
//        txInfo.remove("transaction_object_id");

        //????????????
        String tempObjIdFrom = "reg" + mapAccAddr.get(gdAccount1).toString() + Random(9);
        String tempObjIdTo = "reg" + mapAccAddr.get(gdAccount5).toString() + Random(9);

        register_transaction_ref = txRpObjId;//?????????????????????????????????????????????

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

        //??????
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

        //??????

        shareIssue(gdEquityCode, shareList4, true);
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
    }

    /***
     * ??????????????????
     * @param eqCode  ??????????????????
     * @param shareList  ????????????
     * @throws Exception
     */
    public String shareIssue(String eqCode,List<Map> shareList,boolean bCheckOnchain) throws Exception{
        log.info("????????????");
        enterpriseReg(eqCode,true);
        String query2 = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);

        log.info("??????");
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
     * ????????????
     * @param eqCode  ?????????????????????
     * @param shareList  ????????????
     * @throws Exception
     */
    public String shareIncrease(String eqCode,List<Map> shareList,boolean bCheckOnchain) throws Exception{

        String reason = "????????????";
        String txObjId = "4increaseObj" + Random(6);

        Map eqProd = gdBF.init03EquityProductInfo();
        Map txInfo = gdBF.init04TxInfo();
        txInfo.put("transaction_object_id",txObjId);
        register_transaction_ref = txObjId; //????????????????????? ??????????????????????????????????????????

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
     * ????????????
     * @param bizNo  ???????????? ??????
     * @param eqCode  ????????????
     * @param address  ???????????????
     * @param lockAmount    ????????????
     * @param shareProperty     ??????????????????
     * @throws Exception
     */
    public String lock(String bizNo,String address,String eqCode,long lockAmount,int shareProperty,
                       String cutoffDate,boolean bCheckOnchain) throws Exception{
        log.info("????????????");
        String reason = "????????????";

        //????????????
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
     * ????????????
     * @param bizNo  ???????????? ??????
     * @param eqCode  ????????????
     * @param unlockAmount    ??????????????????
     * @throws Exception
     */
    public String unlock(String bizNo,String eqCode,long unlockAmount,boolean bCheckOnchain) throws Exception{
        log.info("??????????????????");

        //????????????
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
     * ??????????????????
     * @param oldEquityCode  ??????????????????
     * @param newEquityCode  ??????????????????
     * @throws Exception
     */
    public String changeBoard(String oldEquityCode,String newEquityCode,boolean bCheckOnchain) throws Exception{
        log.info("????????????");
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
     * ??????/??????
     * @param eqCode   ?????????????????????
     * @param shareList  ????????????????????????
     * @throws Exception
     */
    public String shareRecycle(String eqCode ,List<Map> shareList,boolean bCheckOnchain) throws Exception {
        log.info("????????????/??????");

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
     * ????????????
     * @param clntNo   ?????????
     * @throws Exception
     */
    public String destroyAcc(String clntNo,boolean bCheckOnchain) throws Exception {
        log.info("?????? " + clntNo);
        String name = "?????????????????????1";
        String number = "?????????????????????1";
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
                    //???????????????
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

        //??????????????????
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
            log.info("???????????????????????????????????????????????????????????????:" + tempObjId);
            Map tempReg =  gdBF.init05RegInfo();
            String regObjId = mapAccAddr.get(tempAddr) + Random(6);
            GDBeforeCondition gdbf = new GDBeforeCondition();

            tempReg.put("register_registration_object_id",regObjId);
            if(regObjType == 1){
            tempReg.put("register_subject_account_ref","SH" + cltNo);}
            tempReg.put("register_product_ref",gdEquityCode);

            mapAddrRegObjId.put(tempAddr + tempPP,regObjId);//????????????????????????
            regList.add(tempReg);
        }

        return regList;
    }

    public List<Map> getAllHolderListRegWithExistRegObjID(String equityCode ,String flowNo){
        List<Map> regList = new ArrayList<>();
        GDBeforeCondition gdBF = new GDBeforeCondition();

        //??????????????????
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
//            log.info("???????????????????????????????????????????????????????????????:" + tempObjId);
            Map tempReg =  gdBF.init05RegInfo();
            log.info(tempAddr + tempPP);
            String regObjId = mapAddrRegObjId.get(tempAddr + tempPP).toString();//??????????????????
//            GDBeforeCondition gdbf = new GDBeforeCondition();

            tempReg.put("register_registration_object_id",regObjId);
            if(regObjType == 1){
                tempReg.put("register_subject_account_ref","SH" + cltNo);}
            tempReg.put("register_product_ref",gdEquityCode);

            mapAddrRegObjId.put(tempAddr + tempPP,regObjId);//????????????????????????
            regList.add(tempReg);
        }

        return regList;
    }

    public void calJGData()throws Exception{
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String sdStart = sdf.format((new Date()).getTime()); // ?????????????????????
        String saveFile = testResultPath + "JGData/" + timeStamp + "_JG.txt";
        int height = JSONObject.fromObject(store.GetHeight()).getInt("data");
        log.info("check begin height " + blockHeight + " and end height " + height);
        ArrayList<String> txStore = new ArrayList<>();
        if(height > blockHeight){
            txStore = commonFunc.getTxArrayWithKeyWord(
                    commonFunc.getTxFromBlock(blockHeight + 1,height),"\"type\":0");
        }
        int[] dataNum = new int[7]; //???????????? ??????/??????/??????/????????????/??????/????????????/??????
        for(int i = 0;i < txStore.size();i++){
            String response = store.GetTxDetail(txStore.get(i));
            String txType = "\\\"type\\\":\\\"??????\\\"";
            dataNum[0] = dataNum[0] + StringUtils.countOccurrencesOf(response,txType);
            txType = "\\\"type\\\":\\\"??????\\\"";
            dataNum[1] = dataNum[1] + StringUtils.countOccurrencesOf(response,txType);
            txType = "\\\"type\\\":\\\"??????\\\"";
            dataNum[2] = dataNum[2] + StringUtils.countOccurrencesOf(response,txType);
            txType = "\\\"type\\\":\\\"????????????\\\"";
            dataNum[3] = dataNum[3] + StringUtils.countOccurrencesOf(response,txType);
            txType = "\\\"type\\\":\\\"??????\\\"";
            dataNum[4] = dataNum[4] + StringUtils.countOccurrencesOf(response,txType);
            txType = "\\\"type\\\":\\\"????????????\\\"";
            dataNum[5] = dataNum[5] + StringUtils.countOccurrencesOf(response,txType);
            txType = "\\\"type\\\":\\\"??????\\\"";
            dataNum[6] = dataNum[6] + StringUtils.countOccurrencesOf(response,txType);
        }

        String dataTopic = "????????????\t\t\t\t????????????\t????????????\t??????\t??????\t??????\t????????????\t??????\t????????????\t??????";
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

        String dataTopic = "????????????\t\t\t\t??????\t??????\t??????\t??????\t????????????\t??????\t????????????\t??????\t????????????";
        FileOperation fo = new FileOperation();
        if((new File(saveFile)).exists()) {
            String get = FileOperation.read(saveFile);
            if (!get.contains(subjectType)) fo.appendToFile(dataTopic, saveFile);
        }
        else
            fo.appendToFile(dataTopic,saveFile);

        for(int k = blockHeight + 1;k <= height;k++){
            int[] dataNum = new int[7]; //???????????? ??????/??????/??????/????????????/??????/????????????/??????
            String [] txArr = commonFunc.getTxsArray(k);
            for(int m = 0;m<txArr.length;m++){
                String response = store.GetTxDetail(txArr[m]);
                if(!response.contains("\"type\":0")) continue;

                String txType = "\\\"type\\\":\\\"??????\\\"";
                dataNum[0] = dataNum[0] + StringUtils.countOccurrencesOf(response,txType);
                txType = "\\\"type\\\":\\\"??????\\\"";
                dataNum[1] = dataNum[1] + StringUtils.countOccurrencesOf(response,txType);
                txType = "\\\"type\\\":\\\"??????\\\"";
                dataNum[2] = dataNum[2] + StringUtils.countOccurrencesOf(response,txType);
                txType = "\\\"type\\\":\\\"????????????\\\"";
                dataNum[3] = dataNum[3] + StringUtils.countOccurrencesOf(response,txType);
                txType = "\\\"type\\\":\\\"??????\\\"";
                dataNum[4] = dataNum[4] + StringUtils.countOccurrencesOf(response,txType);
                txType = "\\\"type\\\":\\\"????????????\\\"";
                dataNum[5] = dataNum[5] + StringUtils.countOccurrencesOf(response,txType);
                txType = "\\\"type\\\":\\\"??????\\\"";
                dataNum[6] = dataNum[6] + StringUtils.countOccurrencesOf(response,txType);

            }
            String sdStart = sdf.format((new Date()).getTime()); // ?????????????????????
            String data = sdStart + "\t\t" + k + "\t" +
                    dataNum[0] + "\t\t" + dataNum[1]+ "\t\t" + dataNum[2] + "\t\t" + dataNum[3]+
                    "\t\t\t" + dataNum[4] + "\t\t" + dataNum[5]+ "\t\t\t" + dataNum[6] + "\t\t" + testCurMethodName;
            fo.appendToFile(data,saveFile);
        }
        blockHeight = height;
        //???????????????????????????????????????
        FileOperation fo2 = new FileOperation();
        fo2.replaceKeyword(System.getProperty("user.dir")  +
                "\\src\\main\\java\\com\\tjfintech\\common\\utils\\UtilsClassGD.java",
                "public static int blockHeight =",
                "\tpublic static int blockHeight = " + blockHeight + ";");
    }

    public void updateBlockHeightParam(int height)throws Exception{
        //???????????????????????????????????????
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
            //storeData??????List???
            if(storeData.contains("[")){
                for(int k=0;k<com.alibaba.fastjson.JSONArray.parseArray(storeData).size();k++) {
                    chkData = checkBlkUriDataJGPrinciple(com.alibaba.fastjson.JSONArray.parseArray(storeData).get(k).toString());
                    if(!chkData.isEmpty()){
                        //??????????????? ????????????????????? uri?????? ??????????????????????????? ????????????
                        fo.appendToFile("//===================================================================",saveFile);
                        fo.appendToFile(sdfSec.format((new Date()).getTime()) + " test case " + testCurMethodName
                                + "\nblock height " + i + " \nuri " + chkData.get("uri").toString()
                                + "\n" + chkData.get("JGData").toString() ,saveFile );
                        bHeaderCalOK = bHeaderCalOK && false;
                    }
                }
            }
            else {
                //StoreData????????????JSON????????????
                chkData =checkBlkUriDataJGPrinciple(storeData);
                if(!chkData.equals(null)){
                    //??????????????? ????????????????????? uri?????? ??????????????????????????? ????????????
                    fo.appendToFile("**===================================================================",saveFile);
                    fo.appendToFile(sdfSec.format((new Date()).getTime()) + " test case " + testCurMethodName
                            + "\nblock height " + i + " \nuri " + chkData.get("uri").toString()
                            + "\n" + chkData.get("JGData").toString() ,saveFile );
                    bHeaderCalOK = bHeaderCalOK && false;
                }
            }

        }
        updateBlockHeightParam(iEnd);
        assertEquals("??????header operation & version ??????",true,bHeaderCalOK);
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
//            log.info("??????uri " + uri + "\n" + data);
            mapData.put("uri" ,uri);
            mapData.put("JGData",data);
//            assertEquals(false,bFlag);
        }
        return mapData;
    }

    //????????????????????????????????????????????????
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


    //????????????????????????
    public void regTestUnit(String type,Boolean bSetEmpty,String subObjID)throws Exception{
        long shareTotals = 1000000;
        gdCompanyID = subObjID;
        Map enSubInfo = gdBF.init01EnterpriseSubjectInfo();
        Map prodInfo = null;

        int gdCpmIdOldVer = Integer.parseInt(gdCF.getObjectLatestVer(gdCompanyID));//??????????????????????????????????????????
        String response= "";

        //???????????????????????????????????????????????????
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
            default:    assertEquals("????????????" + type, false,true);
        }

        //????????????hash
        String txId = net.sf.json.JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        //??????????????????
        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        String txDetail = store.GetTxDetail(txId);
        assertEquals("200", net.sf.json.JSONObject.fromObject(txDetail).getString("state"));

        //?????????????????????????????????????????????????????????
        assertEquals("??????????????????",true,
                gdCF.chkSensitiveWord(txDetail,subjectType));
        assertEquals("??????????????????",true,
                gdCF.chkSensitiveWord(txDetail,prodType));

        //??????????????????????????????  ??????????????? ?????????????????????????????? ?????????2s???????????????
        response = gd.GDMainSubjectQuery(gdContractAddress, gdCompanyID);
//        assertEquals("200", net.sf.json.JSONObject.fromObject(response).getString("state"));


        //??????????????????????????????
        String newSubVer = gdCF.getObjectLatestVer(gdCompanyID);
        String newEqProdVer = gdCF.getObjectLatestVer(gdEquityCode);

        String subSIQCRefVer = gdCF.getObjectLatestVer(subject_investor_qualification_certifier_ref);
        String prodPMSRefVer = gdCF.getObjectLatestVer(product_market_subject_ref);
        String prodSPSRefVer = gdCF.getObjectLatestVer(service_provider_subject_ref);

        //????????????mini url????????????????????????????????????uri??????
        String subfileName = conJGFileName(gdCompanyID,newSubVer);
        String prodfileName = conJGFileName(gdEquityCode,newEqProdVer);

        Map uriInfo = gdCF.getJGURIStoreHash(txId,conJGFileName(gdCompanyID,newSubVer),1);
        String chkSubURI = subfileName;
        String chkProdURI = prodfileName;
        log.info(uriInfo.get("storeData").toString());
        log.info(chkSubURI);
        assertEquals(true,uriInfo.get("storeData").toString().contains(chkSubURI));
        if(!type.equals("4")) assertEquals(true,uriInfo.get("storeData").toString().contains(chkProdURI));
        assertEquals(true,gdCF.bContainJGFlag(uriInfo.get("storeData").toString()));//??????meta???????????????????????????

        //?????????minio?????????????????????+??????????????????????????????????????????
        Map getSubInfo = gdCF.constructJGDataFromStr(conJGFileName(gdCompanyID,newSubVer),subjectType,"1");
        Map getProInfo = null;
        if(!type.equals("4")) getProInfo = gdCF.constructJGDataFromStr(conJGFileName(gdEquityCode, newEqProdVer), prodType, type);

        //??????header content??????
        log.info("********************************" + newSubVer);
        if(newSubVer.equals("0")) {
            enSubInfo.put("content", gdCF.constructContentTreeMap(subjectType, gdCompanyID, newSubVer, "create", String.valueOf(ts1)));
        }else {
            enSubInfo.put("content", gdCF.constructContentTreeMap(subjectType, gdCompanyID, newSubVer, "update", String.valueOf(ts1)));
        }
        //?????????????????????????????? ?????????????????????header content??????
        if(!type.equals("4")) {
            prodInfo.put("content",gdCF.constructContentTreeMap(prodType, gdEquityCode, newEqProdVer, "create", String.valueOf(ts3)));
        }

        //?????????????????????????????????????????? ???????????????????????????????????????????????????
        if(bSetEmpty) prodInfo.put("product_issuer_subject_ref", enSubInfo.get("subject_object_id").toString());
        //??????????????????????????????????????????
        product_issuer_subject_ref = gdCompanyID;
//        assertEquals(String.valueOf(gdCpmIdOldVer + 1),gdCF.getObjectLatestVer(gdCompanyID));

        assertEquals(String.valueOf(gdCpmIdOldVer + 1),newSubVer);

        String[] verForSub = new String[]{"/" + subSIQCRefVer };
        String[] verForProd = new String[]{"/" + prodPMSRefVer,"/" + newSubVer,"/" + prodSPSRefVer};

        log.info("?????????????????????????????????????????????\n" + enSubInfo.toString() + "\n" + getSubInfo.toString());
        assertEquals(replaceCertain(gdCF.matchRefMapCertVer(enSubInfo,subjectType,verForSub)),replaceCertain(getSubInfo.toString()));

        if(!type.equals("4")) {
            log.info("?????????????????????????????????????????????\n" + prodInfo.toString() + "\n" + getProInfo.toString());
            assertEquals(replaceCertain(gdCF.matchRefMapCertVer(prodInfo, prodType, verForProd)), replaceCertain(getProInfo.toString()));
        }
    }


    //?????????????????????????????????
    public void CreateAcc_ThenRegTestUnit(String type,Boolean bSetEmpty)throws Exception{
                //??????????????????
//        Map enSubInfo = gdBF.init01EnterpriseSubjectInfo();
//        createAccForEnterpriseSub(gdCompanyID,enSubInfo);
        createAcc(gdCompanyID,true);

        //??????????????????
        regTestUnit(type,bSetEmpty,gdCompanyID);
    }

    //?????????????????????????????????
    public Map regTestUnit_ThenCreateAcc(String type,Boolean bSetEmpty)throws Exception{
        //??????????????????
        regTestUnit(type,bSetEmpty,gdCompanyID);
        //??????????????????
        Map enSubInfo = gdBF.init01EnterpriseSubjectInfo();
        return createAccForEnterpriseSub(gdCompanyID,enSubInfo);
    }

    //????????????
    public Map createAccForEnterpriseSub(String subObjID,Map enSubInfo)throws Exception{
        int newSubVer = Integer.parseInt(gdCF.getObjectLatestVer(subObjID));
        //???????????????
        String cltNo = subObjID;
        String shareHolderNo = "SH" + cltNo;
        String fundNo = "fund" + cltNo;

        //????????????????????????
        Map shareHolderInfo = new HashMap();
        Map mapSHAcc = gdBF.init02ShareholderAccountInfo();
        mapSHAcc.put("account_object_id", shareHolderNo);  //??????????????????????????????
        mapSHAcc.put("account_subject_ref", cltNo);  //?????????????????????????????? -- ????????????????????????id

        log.info(shAccountInfo.toString());
        shareHolderInfo.put("createTime", ts2);
        shareHolderInfo.put("shareholderNo", shareHolderNo);
        shareHolderInfo.put("accountInfo", mapSHAcc);
        log.info(shareHolderInfo.toString());

        //??????????????????
        Map mapFundAcc = gdBF.init02FundAccountInfo();
        mapFundAcc.put("account_object_id", fundNo);  //??????????????????????????????
        mapFundAcc.put("account_subject_ref", cltNo);  //?????????????????????????????? -- ????????????????????????id
        mapFundAcc.put("account_associated_account_ref", shareHolderNo);  //??????????????????????????????

        Map mapFundInfo = new HashMap();
        mapFundInfo.put("createTime", ts2);
        mapFundInfo.put("fundNo", fundNo);
        mapFundInfo.put("accountInfo", mapFundAcc);

        //????????????/?????????????????????
        Map investor = enSubInfo;//??????????????????????????????????????????

        String response = gd.GDCreateAccout(gdContractAddress, cltNo, mapFundInfo, shareHolderInfo, investor);
        String txId = net.sf.json.JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        //??????????????????
        commonFunc.sdkCheckTxOrSleep(txId,utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
        String txDetail = store.GetTxDetail(txId);
        assertEquals("200", net.sf.json.JSONObject.fromObject(txDetail).getString("state"));

        //???????????????????????????
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

        //?????????????????????????????????????????????????????????
        assertEquals("??????????????????",true,
                gdCF.chkSensitiveWord(txDetail,subjectType));
        assertEquals("??????????????????",true,
                gdCF.chkSensitiveWord(txDetail,accType));

        //???????????????????????????
        response = gd.GDGetShareHolderInfo(gdContractAddress,cltNo);
        assertEquals("200", net.sf.json.JSONObject.fromObject(response).getString("state"));


        //????????????????????????????????????
        String accASrefVer = gdCF.getObjectLatestVer(account_subject_ref);
        String accADrefVer =  gdCF.getObjectLatestVer(account_depository_ref);
        String accAAARefVer =  gdCF.getObjectLatestVer(account_associated_account_ref);

        String shAccVer =  gdCF.getObjectLatestVer("SH" + cltNo);
        String fundAccVer =  gdCF.getObjectLatestVer("fund" + cltNo);
        String personSubVer =  gdCF.getObjectLatestVer(gdCompanyID);

        String subSIQCRefVer = gdCF.getObjectLatestVer(subject_investor_qualification_certifier_ref);

        String fundObjId = fundNo;
        String SHObjId = shareHolderNo;
        //????????????mini url??????????????? ?????????????????????uri??????
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
        assertEquals(true,gdCF.bContainJGFlag(uriInfo.get("storeData").toString()));//??????meta???????????????????????????


        //?????????minio?????????????????????????????????
        Map getSubInfo = gdCF.constructJGDataFromStr(subfileName,subjectType,"1");//??????????????????
        Map getFundAccInfo = gdCF.constructJGDataFromStr(fundAccfileName,accType,"2");
        Map getSHAccInfo = gdCF.constructJGDataFromStr(shAccfileName,accType,"1");


        Map accFund = gdBF.init02FundAccountInfo();
        Map accSH = gdBF.init02ShareholderAccountInfo();

//        enSubInfoBk.put("subject_object_id",cltNo);

        //??????header content ??????
        enSubInfo.put("content",gdCF.constructContentTreeMap(subjectType,gdCompanyID,personSubVer,"update",String.valueOf(ts1)));
        accFund.put("content",gdCF.constructContentTreeMap(accType,fundObjId,fundAccVer,"create",String.valueOf(ts2)));
        accSH.put("content",gdCF.constructContentTreeMap(accType,SHObjId,shAccVer,"create",String.valueOf(ts2)));

        assertEquals(String.valueOf(Integer.valueOf(newSubVer) + 1),personSubVer);

        //??????????????????????????????
        account_subject_ref = cltNo;

        //???????????????????????????????????????????????????
        String[] verForSub = new String[]{"/" + subSIQCRefVer};
        String[] verForAccSH = new String[]{"/" + personSubVer,"/" + accADrefVer,"/" + accAAARefVer};


        log.info("?????????????????????????????????????????????\n" + enSubInfo.toString() + "\n" + getSubInfo.toString());
        assertEquals(replaceCertain(gdCF.matchRefMapCertVer(enSubInfo,subjectType,verForSub)),replaceCertain(getSubInfo.toString()));

        accSH.put("account_object_id",SHObjId);
        accSH.put("account_subject_ref",cltNo);
        log.info("???????????????????????????????????????????????????\n" + accSH.toString() + "\n" + getSHAccInfo.toString());
        assertEquals(replaceCertain(gdCF.matchRefMapCertVer2(accSH,accType)),replaceCertain(getSHAccInfo.toString()));

        account_associated_account_ref = SHObjId;
        String[] verForAccFund = new String[]{"/" + personSubVer,"/" + accADrefVer,"/" + shAccVer};

        accFund.put("account_object_id",fundObjId);
        accFund.put("account_subject_ref",cltNo);
        accFund.put("account_associated_account_ref",SHObjId);
        log.info("???????????????????????????????????????????????????\n" + accFund.toString() + "\n" + getFundAccInfo.toString());
        assertEquals(replaceCertain(gdCF.matchRefMapCertVer2(accFund,accType)),replaceCertain(getFundAccInfo.toString()));

        return mapAccInfo;
    }
}
