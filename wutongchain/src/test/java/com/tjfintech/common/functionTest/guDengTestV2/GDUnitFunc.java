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

import static com.tjfintech.common.functionTest.guDengTestV2.GDCommonFunc.gdConstructShareList;
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
        testReg1.put("register_nature_of_shares", oldProperty);

        testReg2.put("register_account_obj_id",mapAccAddr.get(address));
        testReg2.put("register_registration_object_id",regObjId2);
        testReg2.put("register_nature_of_shares", newProperty);

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
        String txRpObjId = "txReport" + Random(6);
        txInfo.put("transaction_object_id",txRpObjId);

        //登记数据
        String tempObjIdFrom = "reg" + mapAccAddr.get(gdAccount1).toString() + Random(3);
        String tempObjIdTo = "reg" + mapAccAddr.get(gdAccount5).toString() + Random(3);

        register_transaction_ref = txRpObjId;//登记引用的是交易报告的对象标识

        Map fromNow = gdBF.init05RegInfo();
        Map toNow = gdBF.init05RegInfo();

        fromNow.put("register_registration_object_id",tempObjIdFrom);
        toNow.put("register_registration_object_id",tempObjIdTo);

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
        List<Map> shareList = gdConstructShareList(gdAccount1,amount,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,amount,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,amount,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,amount,0, shareList3);

        //发行
        gdEquityCode = "gdEC" + Random(12);
        shareIssue(gdEquityCode, shareList4, true);
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
    }

    public void commonIssuePP01(long amount)throws Exception{
        List<Map> shareList = gdConstructShareList(gdAccount1,amount,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,amount,1, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,amount,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,amount,1, shareList3);

        //发行
        gdEquityCode = "gdEC" + Random(12);
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
        List<Map> regListTemp = getAllHolderListReg(oldEquityCode,"cbSpec" + Random(10));
        String response= gd.GDShareChangeBoard(gdPlatfromKeyID,gdCompanyID,oldEquityCode,newEquityCode,regListTemp, equityProductInfo,bondProductInfo);

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
            log.info("temp index " + i);
            String tempObjId = mapAccAddr.get(tempAddr).toString();
            log.info("检查发行存证登记格式化及信息内容与传入一致:" + tempObjId);
            Map tempReg =  gdBF.init05RegInfo();
            String regObjId = mapAccAddr.get(tempAddr) + Random(6);
            GDBeforeCondition gdbf = new GDBeforeCondition();

            tempReg.put("register_registration_object_id",regObjId);

            mapAddrRegObjId.put(tempAddr,regObjId);//方便后面测试验证
            tempReg.put("register_nature_of_shares",tempPP);
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

            log.info(storeData);

            if(storeData.contains(keyWord)) break;

        }
    }
}
