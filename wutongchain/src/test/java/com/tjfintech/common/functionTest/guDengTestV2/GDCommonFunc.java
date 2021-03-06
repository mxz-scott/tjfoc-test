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
import org.springframework.util.StringUtils;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassGD.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;


@Slf4j
public class GDCommonFunc {
    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store = testBuilder.getStore();
    GuDeng gd = testBuilder.getGuDeng();
    CommonFunc cf = new CommonFunc();
    UtilsClass utilsClass=new UtilsClass();
    MinIOOperation minio = new MinIOOperation();
    String key = "";


    //-----------------------------------------------------------------------------------------------------------
    //获取交易hash函数 此处兼容

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public String getJGStoreHash2(String txId,String type,int offset) throws Exception{
        //获取交易所在区块
        String height = JSONObject.fromObject(store.GetTransactionBlock(txId)).getString("data");
        //获取区块交易列表
        JSONArray txArr = JSONObject.fromObject(store.GetBlockByHeight(Integer.parseInt(height))).getJSONObject("data").getJSONArray("txs");
        String storeId = "";
        for (int i = 0; i < txArr.size(); i++) {
//            log.info("区块交易数 " + txArr.size());
            String txdetail = store.GetTxDetail(txArr.get(i).toString());

            com.alibaba.fastjson.JSONObject object2 = com.alibaba.fastjson.JSONObject.parseObject(txdetail);
            String storeData2 = "";

            //判断是存证则存储
            if (JSONObject.fromObject(txdetail).getJSONObject("data").getJSONObject("header").getInt("type") == 0){
                storeData2 = object2.getJSONObject("data").getJSONObject("store").getString("storeData");
                com.alibaba.fastjson.JSONObject jobj2 = null;
                if(storeData2.startsWith("{")){
                    jobj2 = com.alibaba.fastjson.JSONObject.parseObject(storeData2);
                    if (jobj2.getJSONObject("header").getJSONObject("content").getString("type").equals(type)) {
                        storeId = txArr.get(i).toString();
                        break;
                    }
                }else {
                    //storedata是个list时
                    com.alibaba.fastjson.JSONArray jsonArray2 = com.alibaba.fastjson.JSONArray.parseArray(storeData2);
                    for (int k = 0; k < jsonArray2.size(); k++) {
                        com.alibaba.fastjson.JSONObject objTemp = com.alibaba.fastjson.JSONObject.parseObject(jsonArray2.get(k).toString());
                        if (objTemp.getJSONObject("header").getJSONObject("content").getString("type").equals(type)) {
                            storeId = txArr.get(i).toString();
                            break;
                        }
                    }
                }
            }
        }
        if(storeId.equals("")) {
            log.info("存证与交易并未打在同一个区块中，尝试在下一个区块中查找报送数据存证");
            sleepAndSaveInfo(3000,"等待下一个块交易打块");
            //如果没有新的区块则不处理
            if(JSONObject.fromObject(store.GetHeight()).getInt("data") >= Integer.parseInt(height) + offset) {
                txArr = JSONObject.fromObject(store.GetBlockByHeight(Integer.parseInt(height) + offset)).getJSONObject("data").getJSONArray("txs");
                for (int j = 0; j < txArr.size(); j++) {
                    String txdetail = store.GetTxDetail(txArr.get(j).toString());
                    //判断是存证则存储
                    if (JSONObject.fromObject(txdetail).getJSONObject("data").getJSONObject("header").getInt("type") == 0
                            && txdetail.contains("\\\"type\\\":\\\"" + type + "\\\"")) {
                        storeId = txArr.get(j).toString();
                        break;
                    }
                }
            }else{
                log.info("无新的区块产生");
            }
        }

//        assertEquals(false, storeId.equals(""));
        return storeId;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public Map getJGURIStoreHash(String txId,String keyword,int offset) throws Exception{
        //获取交易所在区块
        String height = JSONObject.fromObject(store.GetTransactionBlock(txId)).getString("data");
        Map mapStoreInfo = new HashMap();

        //在交易hash所在区块高度查找
        mapStoreInfo = findDataInBlock(Integer.parseInt(height),keyword);

        if(mapStoreInfo.get("storeId").equals("")) {
            log.info("存证与交易并未打在同一个区块中，尝试在下一个区块中查找报送数据存证");
            sleepAndSaveInfo(3000,"等待下一个块交易打块");
            //如果没有新的区块则不处理
            if(JSONObject.fromObject(store.GetHeight()).getInt("data") >= Integer.parseInt(height) + offset) {
                //在高度+1 到高度+offset的范围内查找存证
                for(int k =Integer.parseInt(height) +1;k<=Integer.parseInt(height) + offset;k++ ) {
                    mapStoreInfo = findDataInBlock(k, keyword);
                    //如果交易id非空 则表示查找到包含关键字的存证
                    if(!mapStoreInfo.get("storeId").equals("")) break;
                }
            }else{
                log.info("无新的区块产生");
            }
        }
        return mapStoreInfo;
    }

    public Map findDataInBlock(int blockHeiht,String keyword){
        log.info("查找区块高度 " + blockHeiht + " 查找关键字 " + keyword);
        //获取区块交易列表
        JSONArray txArr = JSONObject.fromObject(store.GetBlockByHeight(blockHeiht)).getJSONObject("data").getJSONArray("txs");
        String storeId = "";
        String storeData = "";
        Map mapStoreInfo = new HashMap();

        //同区块高度中查找
        for (int i = 0; i < txArr.size(); i++) {
//            log.info("区块交易数 " + txArr.size());
            String txdetail = store.GetTxDetail(txArr.get(i).toString());
//            log.info(txArr.get(i).toString() + "\n " + txdetail);

            com.alibaba.fastjson.JSONObject object2 = com.alibaba.fastjson.JSONObject.parseObject(txdetail);
            String storeData2 = "";

            //判断是存证则存储
            if (JSONObject.fromObject(txdetail).getJSONObject("data").getJSONObject("header").getInt("type") == 0){
                storeData2 = object2.getJSONObject("data").getJSONObject("store").getString("storeData");
                //如果存证中包含关键字则返回交易的storeData 存关键信息的内容
                if(storeData2.contains(keyword)) {
                    storeId = txArr.get(i).toString();
                    storeData = storeData2;
                    log.info("查找到的当前区块高度 " + blockHeiht);
                    break;
                }
            }
        }
        mapStoreInfo.put("storeId",storeId);
        mapStoreInfo.put("storeData",storeData);
        uriStoreData = storeData;

        return mapStoreInfo;
    }

    public String getJGStoreHash(String txId,int offset) throws Exception{
        //获取交易所在区块
        String height = JSONObject.fromObject(store.GetTransactionBlock(txId)).getString("data");
        //获取区块交易列表
        JSONArray txArr = JSONObject.fromObject(store.GetBlockByHeight(Integer.parseInt(height))).getJSONObject("data").getJSONArray("txs");
        String storeId = "";
        for (int i = 0; i < txArr.size(); i++) {
//            log.info("区块交易数 " + txArr.size());
            //判断是存证则存储
            if (JSONObject.fromObject(store.GetTxDetail(txArr.get(i).toString())).getJSONObject("data").getJSONObject("header").getInt("type") == 0) {
                storeId = txArr.get(i).toString();
                break;
            }
        }
        if(storeId.equals("")) {
            log.info("存证与交易并未打在同一个区块中，尝试在下一个区块中查找报送数据存证");
            sleepAndSaveInfo(4000,"等待下一个区块打包时间");
            txArr = JSONObject.fromObject(store.GetBlockByHeight(Integer.parseInt(height) + offset)).getJSONObject("data").getJSONArray("txs");
            for (int j = 0; j < txArr.size(); j++) {
                //判断是存证则存储
                if (JSONObject.fromObject(store.GetTxDetail(txArr.get(j).toString())).getJSONObject("data").getJSONObject("header").getInt("type") == 0) {
                    storeId = txArr.get(j).toString();
                    break;
                }
            }
        }

        assertEquals(false, storeId.equals(""));
        return storeId;
    }


    public String getJGStoreHash2(JSONArray txArr,String Height,int offset) {
        String storeId = "";
        for (int i = 0; i < txArr.size(); i++) {
            log.info("区块交易数 " + txArr.size());
            //判断是存证则存储
            if (JSONObject.fromObject(store.GetTxDetail(txArr.get(i).toString())).getJSONObject("data").getJSONObject("header").getInt("type") == 0) {
                storeId = txArr.get(i).toString();
                break;
            }
        }
        if(storeId.equals("")) {
            log.info("存证与交易并未打在同一个区块中，尝试在下一个区块中查找报送数据存证");
            txArr = JSONObject.fromObject(store.GetBlockByHeight(Integer.parseInt(Height) + offset)).getJSONObject("data").getJSONArray("txs");
            for (int j = 0; j < txArr.size(); j++) {
                //判断是存证则存储
                if (JSONObject.fromObject(store.GetTxDetail(txArr.get(j).toString())).getJSONObject("data").getJSONObject("header").getInt("type") == 0) {
                    storeId = txArr.get(j).toString();
                    break;
                }
            }
        }

        assertEquals(false, storeId.equals(""));
        return storeId;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static List<Map> gdConstructShareListWithObjID(String address, long amount, int shareProperty,String regObjID){
        register_product_ref = gdEquityCode;

        String regObjId = regObjID;// + "_" + indexReg;
        GDBeforeCondition gdbf = new GDBeforeCondition();
        Map tempReg = gdbf.init05RegInfo();
        tempReg.put("register_registration_object_id",regObjId);
        if(regObjType == 1) {
            tempReg.put("register_subject_account_ref", "SH" + mapAccAddr.get(address));
        }
        if(bChangeRegSN) tempReg.put("register_serial_number", regObjId);//区分同一账户多次登记

        mapAddrRegObjId.put(address + shareProperty + indexReg,regObjId);//方便后面测试验证

//        Map tempTxInfo = gdbf.init04TxInfo();
//        tempTxInfo.put("transaction_original_owner_subject_ref",mapAccAddr.get(address));

        Map<String,Object> shares = new HashMap<>();
        shares.put("address",address);
        shares.put("amount",amount);
        shares.put("shareProperty",shareProperty);
        shares.put("createTime",ts5);
        shares.put("registerInformation",tempReg);
//        shares.put("transactionReport",tempTxInfo);

        List<Map> shareList = new ArrayList<>();
        shareList.add(shares);
        return shareList;
    }
    public static List<Map> gdConstructShareListWithObjID(String address, long amount, int shareProperty,String regObjID,List<Map> list){
        String regObjId = regObjID;// + "_" + indexReg;
        GDBeforeCondition gdbf = new GDBeforeCondition();
        Map tempReg = gdbf.init05RegInfo();
        tempReg.put("register_registration_object_id",regObjId);
        if(regObjType == 1) {
            tempReg.put("register_subject_account_ref", "SH" + mapAccAddr.get(address));
        }
        if(bChangeRegSN) tempReg.put("register_serial_number", regObjId);//区分同一账户多次登记

        mapAddrRegObjId.put(address + shareProperty + indexReg,regObjId);//方便后面测试验证


        List<Map> shareList = new ArrayList<>();
        for(int i = 0 ; i < list.size() ; i++) {
            shareList.add(list.get(i));
        }
        Map<String,Object> shares = new HashMap<>();
        shares.put("address",address);
        shares.put("amount",amount);
        shares.put("shareProperty",shareProperty);
        shares.put("createTime",ts5);
        shares.put("registerInformation",tempReg);
//        shares.put("transactionReport",tempTxInfo);

        shareList.add(shares);
        return shareList;
    }

    public static List<Map> gdConstructShareList(String address, long amount, int shareProperty){
        register_product_ref = gdEquityCode;

        String regObjId = "5" + mapAccAddr.get(address) + Random(6);// + "_" + indexReg;
//        try {
//            FileOperation fileOperation = new FileOperation();
//            fileOperation.appendToFile(regObjId, "regobj.txt");
//        }catch (Exception e){
//            log.info("error");
//        }
        GDBeforeCondition gdbf = new GDBeforeCondition();
        Map tempReg = gdbf.init05RegInfo();
        tempReg.put("register_registration_object_id",regObjId);
        if(regObjType == 1) {
            tempReg.put("register_subject_account_ref", "SH" + mapAccAddr.get(address));
        }
        if(bChangeRegSN) tempReg.put("register_serial_number", regObjId);//区分同一账户多次登记

        mapAddrRegObjId.put(address + shareProperty + indexReg,regObjId);//方便后面测试验证

//        Map tempTxInfo = gdbf.init04TxInfo();
//        tempTxInfo.put("transaction_original_owner_subject_ref",mapAccAddr.get(address));

        Map<String,Object> shares = new HashMap<>();
        shares.put("address",address);
        shares.put("amount",amount);
        shares.put("shareProperty",shareProperty);
        shares.put("createTime",ts5);
        shares.put("registerInformation",tempReg);
//        shares.put("transactionReport",tempTxInfo);

        List<Map> shareList = new ArrayList<>();
        shareList.add(shares);
        return shareList;
    }

    //测试登记带全部参数场景
    public static List<Map> gdConstructShareListFull(String address, long amount, int shareProperty){
        register_product_ref = gdEquityCode;

        String regObjId = "5" + mapAccAddr.get(address) + Random(6);// + "_" + indexReg;
//        try {
//            FileOperation fileOperation = new FileOperation();
//            fileOperation.appendToFile(regObjId, "regobj.txt");
//        }catch (Exception e){
//            log.info("error");
//        }
        GDBeforeCondition gdbf = new GDBeforeCondition();
        Map tempReg = gdbf.init05RegInfoFull();
        tempReg.put("register_registration_object_id",regObjId);
        tempReg.put("register_subject_account_ref", "SH" + mapAccAddr.get(address));
        if(bChangeRegSN) tempReg.put("register_serial_number", regObjId);//区分同一账户多次登记

        mapAddrRegObjId.put(address + shareProperty + indexReg,regObjId);//方便后面测试验证

//        Map tempTxInfo = gdbf.init04TxInfo();
//        tempTxInfo.put("transaction_original_owner_subject_ref",mapAccAddr.get(address));

        Map<String,Object> shares = new HashMap<>();
        shares.put("address",address);
        shares.put("amount",amount);
        shares.put("shareProperty",shareProperty);
        shares.put("createTime",ts5);
        shares.put("registerInformation",tempReg);
//        shares.put("transactionReport",tempTxInfo);

        List<Map> shareList = new ArrayList<>();
        shareList.add(shares);
        return shareList;
    }

    public static List<Map> gdConstructShareList(String address, long amount, int shareProperty,List<Map> list){
        String regObjId = "5" + mapAccAddr.get(address) + Random(6);// + "_" + indexReg;
//        try {
//            FileOperation fileOperation = new FileOperation();
//            fileOperation.appendToFile(regObjId, "regobj.txt");
//        }catch (Exception e){
//            log.info("error");
//        }
        GDBeforeCondition gdbf = new GDBeforeCondition();
        Map tempReg = gdbf.init05RegInfo();
        tempReg.put("register_registration_object_id",regObjId);
        if(regObjType == 1) {
            tempReg.put("register_subject_account_ref", "SH" + mapAccAddr.get(address));
        }
        if(bChangeRegSN) tempReg.put("register_serial_number", regObjId);//区分同一账户多次登记

        mapAddrRegObjId.put(address + shareProperty + indexReg,regObjId);//方便后面测试验证


        List<Map> shareList = new ArrayList<>();
        for(int i = 0 ; i < list.size() ; i++) {
            shareList.add(list.get(i));
        }
        Map<String,Object> shares = new HashMap<>();
        shares.put("address",address);
        shares.put("amount",amount);
        shares.put("shareProperty",shareProperty);
        shares.put("createTime",ts5);
        shares.put("registerInformation",tempReg);
//        shares.put("transactionReport",tempTxInfo);

        shareList.add(shares);
        return shareList;
    }


    public static List<Map> gdConstructShareListWithRegMap(String address, long amount, int shareProperty,Map regMap){
        Map<String,Object> shares = new HashMap<>();
        shares.put("address",address);
        shares.put("amount",amount);
        shares.put("shareProperty",shareProperty);
        shares.put("createTime",ts5);
        shares.put("registerInformation",regMap);

        List<Map> shareList = new ArrayList<>();
        shareList.add(shares);
        return shareList;
    }

    public static List<Map> gdConstructShareListWithRegMap(String address, long amount, int shareProperty,Map regMap,List<Map> list){
        List<Map> shareList = new ArrayList<>();
        for(int i = 0 ; i < list.size() ; i++) {
            shareList.add(list.get(i));
        }
        Map<String,Object> shares = new HashMap<>();
        shares.put("address",address);
        shares.put("amount",amount);
        shares.put("shareProperty",shareProperty);
        shares.put("createTime",ts5);
        shares.put("registerInformation",regMap);

        shareList.add(shares);
        return shareList;
    }

//    public static List<Map> gdConstructShareListNoTxReport(String address, double amount, int shareProperty,List<Map> list){
//        GDBeforeCondition gdbf = new GDBeforeCondition();
//        Map tempReg = gdbf.init05RegInfo();
//        tempReg.put("register_account_obj_id",mapAccAddr.get(address));
//        tempReg.put("register_nature_of_shares",shareProperty);
//
//        List<Map> shareList = new ArrayList<>();
//        for(int i = 0 ; i < list.size() ; i++) {
//            shareList.add(list.get(i));
//        }
//        Map<String,Object> shares = new HashMap<>();
//        shares.put("address",address);
//        shares.put("amount",amount);
//        shares.put("shareProperty",shareProperty);
//        shares.put("registerInformation",tempReg);
//
//        shareList.add(shares);
//        return shareList;
//    }

//    public static List<Map> gdConstructShareList2(String address, long amount, int shareProperty){
//        String regObjId = "5" + mapAccAddr.get(address) + Random(6);
//        GDBeforeCondition gdbf = new GDBeforeCondition();
//        Map tempReg = gdbf.init05RegInfo();
////        tempReg.put("register_account_obj_id",mapAccAddr.get(address));
////        tempReg.put("register_nature_of_shares",shareProperty);
//        tempReg.put("register_registration_object_id",regObjId);
//        tempReg.put("register_subject_account_ref","SH" + mapAccAddr.get(address));
//
//        mapAddrRegObjId.put(address + shareProperty,regObjId);//方便后面测试验证
//
//        //不填写如下字段
//        tempReg.remove("register_rights_change_amount");
//        tempReg.remove("register_rights_frozen_balance");
//        tempReg.remove("register_available_balance");
//        tempReg.remove("register_creditor_subscription_count");
//        tempReg.remove("register_rights_frozen_change_amount");
//
//
//        Map<String,Object> shares = new HashMap<>();
//        shares.put("address",address);
//        shares.put("amount",amount);
//        shares.put("shareProperty",shareProperty);
//        shares.put("createTime",ts5);
//        shares.put("registerInformation",tempReg);
////        shares.put("transactionReport",tempTxInfo);
//
//        List<Map> shareList = new ArrayList<>();
//        shareList.add(shares);
//        return shareList;
//    }
//
//    public static List<Map> gdConstructShareList2(String address, long amount, int shareProperty,List<Map> list){
//        String regObjId = "5" + mapAccAddr.get(address) + Random(6);
//        GDBeforeCondition gdbf = new GDBeforeCondition();
//        Map tempReg = gdbf.init05RegInfo();
////        tempReg.put("register_account_obj_id",mapAccAddr.get(address));
////        tempReg.put("register_nature_of_shares",shareProperty);
//        tempReg.put("register_registration_object_id",regObjId);
//        tempReg.put("register_subject_account_ref","SH" + mapAccAddr.get(address));
//
//        mapAddrRegObjId.put(address + shareProperty,regObjId);//方便后面测试验证
//
//        //不填写如下字段
//        tempReg.remove("register_rights_change_amount");
//        tempReg.remove("register_rights_frozen_balance");
//        tempReg.remove("register_available_balance");
//        tempReg.remove("register_creditor_subscription_count");
//        tempReg.remove("register_rights_frozen_change_amount");
//
//
//        List<Map> shareList = new ArrayList<>();
//        for(int i = 0 ; i < list.size() ; i++) {
//            shareList.add(list.get(i));
//        }
//        Map<String,Object> shares = new HashMap<>();
//        shares.put("address",address);
//        shares.put("amount",amount);
//        shares.put("shareProperty",shareProperty);
//        shares.put("createTime",ts5);
//        shares.put("registerInformation",tempReg);
////        shares.put("transactionReport",tempTxInfo);
//
//        shareList.add(shares);
//        return shareList;
//    }

    public static List<Map> gdConstructShareListN(String address, long amount, int shareProperty){
        GDBeforeConditionN gdbf = new GDBeforeConditionN();
        Map tempReg = gdbf.init05RegInfo();
        tempReg.remove("register_registration_object_id");

        Map<String,Object> shares = new HashMap<>();
        shares.put("address",address);
        shares.put("amount",amount);
        shares.put("shareProperty",shareProperty);
        shares.put("createTime",ts5);
        shares.put("registerInformation",tempReg);

        List<Map> shareList = new ArrayList<>();
        shareList.add(shares);
        return shareList;
    }

    public static List<Map> gdConstructShareListN(String address, long amount, int shareProperty,List<Map> list){
        GDBeforeConditionN gdbf = new GDBeforeConditionN();
        Map tempReg = gdbf.init05RegInfo();
        tempReg.remove("register_registration_object_id");

        List<Map> shareList = new ArrayList<>();
        for(int i = 0 ; i < list.size() ; i++) {
            shareList.add(list.get(i));
        }
        Map<String,Object> shares = new HashMap<>();
        shares.put("address",address);
        shares.put("amount",amount);
        shares.put("shareProperty",shareProperty);
        shares.put("createTime",ts5);
        shares.put("registerInformation",tempReg);

        shareList.add(shares);
        return shareList;
    }



    public static List<Map> getShareListFromQueryNoZeroAcc(JSONArray dataShareList)throws Exception {
        List<Map> getShareList = new ArrayList<>();
        for (int i = 0; i < dataShareList.size(); i++) {

            if (dataShareList.get(i).toString().contains(zeroAccount))
                continue;
            else {
                long amount = JSONObject.fromObject(dataShareList.get(i)).getLong("amount");
                long lockAmount = JSONObject.fromObject(dataShareList.get(i)).getLong("lockAmount");
                String address = JSONObject.fromObject(dataShareList.get(i)).getString("address");
                int shareProperty = JSONObject.fromObject(dataShareList.get(i)).getInt("shareProperty");
                String sharePropertyCN = JSONObject.fromObject(dataShareList.get(i)).getString("sharePropertyCN");
                getShareList = gdConstructQueryShareList(address, amount, shareProperty, lockAmount,sharePropertyCN,getShareList);
            }
        }
        return getShareList;
    }

    public static List<Map> gdConstructQueryShareList(String address, long amount, int shareProperty,double lockAmount,String sharePropertyCN, List<Map> list){
        //处理登记
        GDBeforeCondition gdbf = new GDBeforeCondition();
        Map tempReg = gdbf.init05RegInfo();
        tempReg.put("register_account_obj_id",mapAccAddr.get(address));
        tempReg.put("register_nature_of_shares",shareProperty);

//        //处理交易
//        Map tempTxInfo = gdbf.init04TxInfo();
//        tempTxInfo.put("transaction_original_owner_subject_ref",mapAccAddr.get(address));

        List<Map> shareList = new ArrayList<>();
        for(int i = 0 ; i < list.size() ; i++) {
            shareList.add(list.get(i));
        }
        Map<String,Object> shares = new HashMap<>();
        shares.put("address",address);
        shares.put("amount",amount);
        shares.put("lockAmount",lockAmount);
        shares.put("shareProperty",shareProperty);
        shares.put("sharePropertyCN",sharePropertyCN);
        shares.put("registerInformation",tempReg);
//        shares.put("transactionReport",tempTxInfo);

        shareList.add(shares);
        return shareList;
    }

    public static List<Map> gdConstructQueryShareListNoTxReport(String address, long amount, int shareProperty,
                                                                double lockAmount,String sharePropertyCN, List<Map> list){
        //处理登记
        GDBeforeCondition gdbf = new GDBeforeCondition();
        Map tempReg = gdbf.init05RegInfo();
        tempReg.put("register_account_obj_id",mapAccAddr.get(address));
        tempReg.put("register_nature_of_shares",shareProperty);

        List<Map> shareList = new ArrayList<>();
        for(int i = 0 ; i < list.size() ; i++) {
            shareList.add(list.get(i));
        }
        Map<String,Object> shares = new HashMap<>();
        shares.put("address",address);
        shares.put("amount",amount);
        shares.put("lockAmount",lockAmount);
        shares.put("shareProperty",shareProperty);
        shares.put("sharePropertyCN",sharePropertyCN);
        shares.put("registerInformation",tempReg);

        shareList.add(shares);
        return shareList;
    }


    public static String getTotalAmountFromShareList(JSONArray dataShareList)throws Exception {
        List<Map> getShareList = new ArrayList<>();
        long total = 0;
        for (int i = 0; i < dataShareList.size(); i++) {
            total = JSONObject.fromObject(dataShareList.get(i)).getLong("amount") + total;
        }
        return String.valueOf(total);
    }

    public static String getTotalAmountFromShareList(List dataShareList)throws Exception {
        List<Map> getShareList = new ArrayList<>();
        long total = 0;
        for (int i = 0; i < dataShareList.size(); i++) {
            total = JSONObject.fromObject(dataShareList.get(i)).getLong("amount") + total;
        }
        return String.valueOf(total);
    }

    /***
     * 构造股权性质编码表
     * @return
     * @throws Exception
     */
    public static Map<String,String> mapShareENCN()throws Exception{
        Map<String,String> mapShareTypeCN = new HashMap<>();
        mapShareTypeCN.put("0","流通股");
        mapShareTypeCN.put("1","优先股");
        mapShareTypeCN.put("11","公众已托管-社会公众股");
        mapShareTypeCN.put("12","公众已托管-高管买入股份");
        mapShareTypeCN.put("13","公众已托管-限售流通股(个人)");
        mapShareTypeCN.put("14","公众已托管-限售流通股(机构)");
        mapShareTypeCN.put("2","资格股");
        mapShareTypeCN.put("21","发起人股-境内法人股");
        mapShareTypeCN.put("22","发起人股-国有股");
        mapShareTypeCN.put("23","发起人股-境外法人股");
        mapShareTypeCN.put("24","发起人股-境内自然人股");
        mapShareTypeCN.put("25","发起人股-境外自然人股");
        mapShareTypeCN.put("26","发起人股-境内其它机构");
        mapShareTypeCN.put("27","发起人股-境外其它机构");
        mapShareTypeCN.put("28","发起人股-认缴未出资");
        mapShareTypeCN.put("29","发起人股-其它");
        mapShareTypeCN.put("3","定增股");
        mapShareTypeCN.put("31","定增股-国有股");
        mapShareTypeCN.put("32","定增股-境内法人股");
        mapShareTypeCN.put("33","定增股-境外法人股");
        mapShareTypeCN.put("34","定增股-境内自然人股");
        mapShareTypeCN.put("35","定增股-境外自然人股");
        mapShareTypeCN.put("36","定增股-境内其它机构");
        mapShareTypeCN.put("37","定增股-境外其它机构");
        mapShareTypeCN.put("39","定增股-其它");
        mapShareTypeCN.put("4","特限股");
        mapShareTypeCN.put("41","控股股东、实控人、一致行动人股");
        mapShareTypeCN.put("42","承诺人股");
        mapShareTypeCN.put("43","高管分红股、转增股");
        mapShareTypeCN.put("44","高管定增股");
        mapShareTypeCN.put("45","高管限售股");
        mapShareTypeCN.put("46","股权激励股");
        mapShareTypeCN.put("49","特限股-其它");
        mapShareTypeCN.put("5","托管股");
        mapShareTypeCN.put("51","托管股-国有股");
        mapShareTypeCN.put("52","托管股-境内法人股");
        mapShareTypeCN.put("53","托管股-境外法人股");
        mapShareTypeCN.put("54","托管股-境内自然人股");
        mapShareTypeCN.put("55","托管股-境外自然人股");
        mapShareTypeCN.put("56","托管股-境内其它机构");
        mapShareTypeCN.put("57","托管股-境外其它机构");
        mapShareTypeCN.put("58","托管股-高管股");
        mapShareTypeCN.put("59","托管股-其它");
        mapShareTypeCN.put("6","有限公司股");
        mapShareTypeCN.put("61","有限公司股-国有股");
        mapShareTypeCN.put("62","有限公司股-境内法人股");
        mapShareTypeCN.put("63","有限公司股-境外法人股");
        mapShareTypeCN.put("64","有限公司股-境内自然人股");
        mapShareTypeCN.put("65","有限公司股-境外自然人股");
        mapShareTypeCN.put("66","有限公司股-境内其它机构");
        mapShareTypeCN.put("67","有限公司股-境外其它机构");
        mapShareTypeCN.put("68","有限公司股-高管股");
        mapShareTypeCN.put("69","有限公司股-其它");
        mapShareTypeCN.put("9","其他类型");
        mapShareTypeCN.put("91","股份合作制");
        mapShareTypeCN.put("92","退托管股-记录信息无法律效力");
        mapShareTypeCN.put("93","退托管股-记录信息无法律效力-未缴");
        mapShareTypeCN.put("94","退托管股-记录信息无法律效力-承诺人");
        mapShareTypeCN.put("99","其他");


        return mapShareTypeCN;
    }
    public boolean mapCompare(Map<String, Object> map1,Map<String, Object> map2) {
        boolean isChange = false;
        for (Map.Entry<String, Object> entry1 : map1.entrySet()) {
            Object m1value = entry1.getValue() == null ? "" : entry1.getValue();
            Object m2value = map2.get(entry1.getKey()) == null ? "" : map2.get(entry1.getKey());
            if (!m1value.equals(m2value)) {
                isChange = true;
            }
        }
        return isChange;
    }


    //------------------------------------------------------------------------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------

    public Map contructEnterpriseSubInfo(String subTxId){
        log.info("检查的交易id " + subTxId);

        com.alibaba.fastjson.JSONObject object2 = com.alibaba.fastjson.JSONObject.parseObject(store.GetTxDetail(subTxId));
        String storeData2 = object2.getJSONObject("data").getJSONObject("store").getString("storeData");

        com.alibaba.fastjson.JSONObject jobjOK = parseJSONBaseOnJSONStr(storeData2, subjectType);

        //schema校验数据格式
        assertEquals("schema校验数据是否匹配",true,
                cf.schemaCheckData(dirSchemaData,chkSchemaToolName,gdSchema,jobjOK.toString(),"2"));

        return subjectInfoEnterprise(jobjOK);
    }

    public static String conJGFileName(String objectId,String version){
        return objectId + "/" + version;// + ".json";
    }

    public Map constructJGDataFromStr(String miniofileName,String type,String subTypeSubProd)throws Exception{
        log.info("监管存证: " + miniofileName + " 待检测数据模型：" + type + " 数据模型子类型：" + subTypeSubProd);

        String storeData2 = minio.getFileFromMinIO(minIOEP,jgBucket,miniofileName,"");

        //schema校验数据格式
//        assertEquals("schema校验数据是否匹配",true,
//                cf.schemaCheckData(dirSchemaData,chkSchemaToolName,gdSchema,storeData2,"2"));

        com.alibaba.fastjson.JSONObject jobjOK = com.alibaba.fastjson.JSONObject.parseObject(storeData2);

        Map mapData = new HashMap();
        switch (type){
            case "subject":
                if(subTypeSubProd.equals("1"))     {  mapData = subjectInfoEnterprise(jobjOK);   }
                else if(subTypeSubProd.equals("2")){  mapData = subjectInfoPerson(jobjOK);       }
                break;
            case "account":                 mapData = accountInfo(jobjOK,subTypeSubProd);break;
            case "product":                 mapData = productInfo(jobjOK,subTypeSubProd);break;
            case "transactionreport":       mapData = transInfo(jobjOK);break;
            case "registration":            mapData = regiInfo(jobjOK);break;
            case "settlement":              mapData = settleInfo(jobjOK);break;
            case "infodisclosure":          mapData = pubInfo(jobjOK);break;
            default: log.info("未能识别的类型 " + type);
        }
        return mapData;
    }


    public Map contructPersonalSubInfo(String personalTxId){
        log.info("检查的交易id " + personalTxId);

        com.alibaba.fastjson.JSONObject object2 = com.alibaba.fastjson.JSONObject.parseObject(store.GetTxDetail(personalTxId));
        String storeData2 = object2.getJSONObject("data").getJSONObject("store").getString("storeData");

        com.alibaba.fastjson.JSONObject jobjOK = parseJSONBaseOnJSONStr(storeData2, subjectType);

        //schema校验数据格式
        assertEquals("schema校验数据是否匹配",true,
                cf.schemaCheckData(dirSchemaData,chkSchemaToolName,gdSchema,jobjOK.toString(),"2"));

        return subjectInfoPerson(jobjOK);
    }

    public Map conPersonalSubInfoNew(String miniofileName)throws Exception{
        log.info("监管存证: " + miniofileName);

        String storeData2 = minio.getFileFromMinIO(minIOEP,jgBucket,miniofileName,"");

        //schema校验数据格式
        assertEquals("schema校验数据是否匹配",true,
                cf.schemaCheckData(dirSchemaData,chkSchemaToolName,gdSchema,storeData2,"2"));

        com.alibaba.fastjson.JSONObject jobjOK = com.alibaba.fastjson.JSONObject.parseObject(storeData2);
        return subjectInfoPerson(jobjOK);
    }


    public Map getEnterpriseSubInfo(String response){
        com.alibaba.fastjson.JSONObject object2 = com.alibaba.fastjson.JSONObject.parseObject(response);
        String storeData2 = object2.getString("data");//.getJSONObject("store").getString("storeData");
        com.alibaba.fastjson.JSONObject jobj2 = parseJSONBaseOnJSONStrCompatible(storeData2, subjectType);
        return subjectInfoEnterprise(jobj2);
    }

    public Map getPersonalSubInfo(String response){
        com.alibaba.fastjson.JSONObject object2 = com.alibaba.fastjson.JSONObject.parseObject(response);
        return subjectInfoPerson(object2.getJSONObject("data"));
    }


    /***
     * 构造资金账户
     * @param TxId  交易id
     * @param objId  账户对象标识
     * @return
     */
    public Map contructFundAccountInfo(String TxId,String objId){
        log.info("检查的交易id " + TxId);
        com.alibaba.fastjson.JSONObject object2 = com.alibaba.fastjson.JSONObject.parseObject(store.GetTxDetail(TxId));
        String storeData2 = object2.getJSONObject("data").getJSONObject("store").getString("storeData");
        com.alibaba.fastjson.JSONArray jsonArray2 = com.alibaba.fastjson.JSONArray.parseArray(storeData2);

        if(!storeData2.contains(objId)){
            assertEquals("未包含检查objID：" + objId,false,true);
            return null;
        }

        com.alibaba.fastjson.JSONObject jobjOK = null;

        //获取账户类型  资金账户 指定账户对象标识的存证信息
        for(int i=0;i<jsonArray2.size();i++) {
            com.alibaba.fastjson.JSONObject objTemp = com.alibaba.fastjson.JSONObject.parseObject(jsonArray2.get(i).toString());
            if (objTemp.getJSONObject("header").getJSONObject("content").getString("type").equals(accType) &&
                    (objTemp.getJSONObject("body").getJSONObject("account_information").getJSONObject("basic_account_information").getIntValue("account_type") == 2) &&
                    objTemp.getJSONObject("header").getJSONObject("content").getString("object_id").equals(objId)) {
                jobjOK = objTemp;
                break;
            }
        }
        log.info("***********" + jobjOK.toString());

        //schema校验数据格式
        assertEquals("schema校验数据是否匹配",true,
                cf.schemaCheckData(dirSchemaData,chkSchemaToolName,gdSchema,jobjOK.toString(),"2"));
        return accountInfo(jobjOK,"2");
    }


    /***
     * 构造股权账户
     * @param TxId  交易id
     * @param objId  账户对象标识
     * @return
     */
    public Map contructEquityAccountInfo(String TxId,String objId){
        log.info("检查的交易id " + TxId);
        com.alibaba.fastjson.JSONObject object2 = com.alibaba.fastjson.JSONObject.parseObject(store.GetTxDetail(TxId));
        String storeData2 = object2.getJSONObject("data").getJSONObject("store").getString("storeData");
        com.alibaba.fastjson.JSONArray jsonArray2 = com.alibaba.fastjson.JSONArray.parseArray(storeData2);

        if(!storeData2.contains(objId)){
            assertEquals("未包含检查objID：" + objId,false,true);
            return null;
        }

        com.alibaba.fastjson.JSONObject jobjOK = null;

        //获取账户类型  股权账户 指定账户对象标识的存证信息
        for(int i=0;i<jsonArray2.size();i++){
            com.alibaba.fastjson.JSONObject objTemp = com.alibaba.fastjson.JSONObject.parseObject(jsonArray2.get(i).toString());
            if( objTemp.getJSONObject("header").getJSONObject("content").getString("type").equals(accType) &&
                    (objTemp.getJSONObject("body").getJSONObject("account_information").getJSONObject("basic_account_information").getIntValue("account_type") == 1) &&
                    objTemp.getJSONObject("header").getJSONObject("content").getString("object_id").equals(objId)){
                jobjOK = objTemp;
                break;
            }
        }

        //schema校验数据格式
        assertEquals("schema校验数据是否匹配",true,
                cf.schemaCheckData(dirSchemaData,chkSchemaToolName,gdSchema,jobjOK.toString(),"2"));

        return accountInfo(jobjOK,"1");
    }


    public Map contructEquityProdInfo(String prodTxId){
        log.info("检查的交易id " + prodTxId);
        com.alibaba.fastjson.JSONObject object2 = com.alibaba.fastjson.JSONObject.parseObject(store.GetTxDetail(prodTxId));
        String storeData2 = object2.getJSONObject("data").getJSONObject("store").getString("storeData");

        com.alibaba.fastjson.JSONObject jobjOK = parseJSONBaseOnJSONStr(storeData2,prodType);

        //schema校验数据格式
        assertEquals("schema校验数据是否匹配",true,
                cf.schemaCheckData(dirSchemaData,chkSchemaToolName,gdSchema,jobjOK.toString(),"2"));
        return productInfo(jobjOK,"1");
    }

    public Map contructBondProdInfo(String prodTxId){
        log.info("检查的交易id " + prodTxId);
        com.alibaba.fastjson.JSONObject object2 = com.alibaba.fastjson.JSONObject.parseObject(store.GetTxDetail(prodTxId));
        String storeData2 = object2.getJSONObject("data").getJSONObject("store").getString("storeData");

        com.alibaba.fastjson.JSONObject jobjOK = parseJSONBaseOnJSONStr(storeData2,prodType);

//        //schema校验数据格式
        assertEquals("schema校验数据是否匹配",true,
                cf.schemaCheckData(dirSchemaData,chkSchemaToolName,gdSchema,jobjOK.toString(),"2"));
        return productInfo(jobjOK,"2");
    }

    public Map contructFundProdInfo(String prodTxId){
        log.info("检查的交易id " + prodTxId);
        com.alibaba.fastjson.JSONObject object2 = com.alibaba.fastjson.JSONObject.parseObject(store.GetTxDetail(prodTxId));
        String storeData2 = object2.getJSONObject("data").getJSONObject("store").getString("storeData");

        com.alibaba.fastjson.JSONObject jobjOK = parseJSONBaseOnJSONStr(storeData2,prodType);

//        //schema校验数据格式
        assertEquals("schema校验数据是否匹配",true,
                cf.schemaCheckData(dirSchemaData,chkSchemaToolName,gdSchema,jobjOK.toString(),"2"));

        return productInfo(jobjOK,"3");
    }

    //交易信息
    public Map contructTxInfo(String TxId,int checkSize,String objId){
        com.alibaba.fastjson.JSONObject object2 = com.alibaba.fastjson.JSONObject.parseObject(store.GetTxDetail(TxId));
        String storeData2 = object2.getJSONObject("data").getJSONObject("store").getString("storeData");
        com.alibaba.fastjson.JSONArray jsonArray2 = com.alibaba.fastjson.JSONArray.parseArray(storeData2);

        if(!storeData2.contains(objId)){
            assertEquals("未包含检查objID：" + objId,false,true);
            return null;
        }
        com.alibaba.fastjson.JSONObject jobjOK = null;

        log.info("检查交易及登记array size:" + jsonArray2.size()) ;
        assertEquals(checkSize,jsonArray2.size());

        log.info("获取交易存证数据 且交易的原持有方主体引用为指定对象标识");
        for(int i=0;i<jsonArray2.size();i++){
//            log.info("index " + i);
            com.alibaba.fastjson.JSONObject objTemp = com.alibaba.fastjson.JSONObject.parseObject(jsonArray2.get(i).toString());
//            log.info(objTemp.toString());
            if( objTemp.getJSONObject("header").getJSONObject("content").getString("type").equals(txrpType) &&
                    objTemp.getJSONObject("body").getJSONObject("transaction_report_information"
                    ).getJSONObject("transaction_information").getJSONObject("transaction_party_information"
                    ).getString("transaction_original_owner_subject_ref").equals(objId)){
                jobjOK = objTemp;
                break;
            }
        }
        log.info(jobjOK.toString());

        //schema校验数据格式
        assertEquals("schema校验数据是否匹配",true,
                cf.schemaCheckData(dirSchemaData,chkSchemaToolName,gdSchema,jobjOK.toString(),"2"));

        return transInfo(jobjOK);

    }

    //构造监管数据格式检查
    //登记信息
    public Map contructRegisterInfo(String TxId,int checkSize,String... objIdArr){
        assertEquals("至少需要传objId 对象标识",true ,objIdArr.length > 0);
        String objId = objIdArr[0];
        int shareProperty = 0;
        if(objIdArr.length == 2){
            shareProperty = Integer.parseInt(objIdArr[1]);
        }
        log.info("test obj " + objId + " share property " + shareProperty);

        com.alibaba.fastjson.JSONObject object2 = com.alibaba.fastjson.JSONObject.parseObject(store.GetTxDetail(TxId));
        String storeData2 = object2.getJSONObject("data").getJSONObject("store").getString("storeData");
        com.alibaba.fastjson.JSONArray jsonArray2 = com.alibaba.fastjson.JSONArray.parseArray(storeData2);

        if(!storeData2.contains(objId)){
            log.info("storedata2 " + storeData2);
            assertEquals("未包含检查objID：" + objId,false,true);
            return null;
        }

        com.alibaba.fastjson.JSONObject jobjOK = null;

        log.info("检查交易及登记array size:" + jsonArray2.size());
        //登记和主体 合在一起了 同一个存证
//        assertEquals("json array:\n " + jsonArray2,checkSize+1,jsonArray2.size());

        log.info("获取指定存证信息");
        //获取登记存证信息 且权利人账户引用为指定的对象标识 股份性质为指定股份性质
        for(int i=0;i<jsonArray2.size();i++){
//            log.info("check index " + i);
            com.alibaba.fastjson.JSONObject objTemp = com.alibaba.fastjson.JSONObject.parseObject(jsonArray2.get(i).toString());
//            log.info(objTemp.toString());
            String type = objTemp.getJSONObject("header").getJSONObject("content").getString("type");
            log.info(type + "123");

            if( type.equals(regType) ){
                String memRef = objTemp.getJSONObject("body").getJSONObject("registration_information"
                ).getJSONObject("registration_rights").getJSONObject("basic_information_rights").getString("register_account_obj_id");
                int sharepp = objTemp.getJSONObject("body").getJSONObject("registration_information").getJSONObject("roll_records"
                ).getJSONObject("register_shareholders").getIntValue("register_nature_of_shares");
                if (memRef.equals(objId) && (sharepp == shareProperty)) {
                    jobjOK = objTemp;
                    break;
                }
            }
        }
        log.info(jobjOK.toString());

        //schema校验数据格式
        assertEquals("schema校验数据是否匹配",true,
                cf.schemaCheckData(dirSchemaData,chkSchemaToolName,gdSchema,jobjOK.toString(),"2"));

        return regiInfo(jobjOK);
    }

    //一个登记信息
    public Map contructOneRegisterInfo(String TxId){
        com.alibaba.fastjson.JSONObject object2 = com.alibaba.fastjson.JSONObject.parseObject(store.GetTxDetail(TxId));
        String storeData2 = object2.getJSONObject("data").getJSONObject("store").getString("storeData");

        com.alibaba.fastjson.JSONObject jobjOK = parseJSONBaseOnJSONStr(storeData2,regType);

        //schema校验数据格式
        assertEquals("schema校验数据是否匹配",true,
                cf.schemaCheckData(dirSchemaData,chkSchemaToolName,gdSchema,jobjOK.toString(),"2"));

        return regiInfo(jobjOK);
    }

    public Map contructSettleInfo(String TxId){
        com.alibaba.fastjson.JSONObject object2 = com.alibaba.fastjson.JSONObject.parseObject(store.GetTxDetail(TxId));
        String storeData2 = object2.getJSONObject("data").getJSONObject("store").getString("storeData");

        com.alibaba.fastjson.JSONObject jobjOK = parseJSONBaseOnJSONStr(storeData2,settleType);

        //schema校验数据格式
        assertEquals("schema校验数据是否匹配",true,
                cf.schemaCheckData(dirSchemaData,chkSchemaToolName,gdSchema,jobjOK.toString(),"2"));

        return settleInfo(jobjOK);
    }

    public Map contructPublishInfo(String TxId){
        com.alibaba.fastjson.JSONObject object2 = com.alibaba.fastjson.JSONObject.parseObject(store.GetTxDetail(TxId));
        String storeData2 = object2.getJSONObject("data").getJSONObject("store").getString("storeData");

        com.alibaba.fastjson.JSONObject jobjOK = parseJSONBaseOnJSONStr(storeData2,infoType);

        //schema校验数据格式
        assertEquals("schema校验数据是否匹配",true,
                cf.schemaCheckData(dirSchemaData,chkSchemaToolName,gdSchema,jobjOK.toString(),"2"));

        return pubInfo(jobjOK);
    }

    //------------------------------------------------------------------------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------


    public com.alibaba.fastjson.JSONObject parseJSONBaseOnJSONStr(String JSONString, String contentType) {
        String storeData = JSONString;
        com.alibaba.fastjson.JSONObject jobj2 = null;
        //storedata是个list时
        com.alibaba.fastjson.JSONArray jsonArray2 = com.alibaba.fastjson.JSONArray.parseArray(storeData);
        for (int i = 0; i < jsonArray2.size(); i++) {
            com.alibaba.fastjson.JSONObject objTemp = com.alibaba.fastjson.JSONObject.parseObject(jsonArray2.get(i).toString());
            if (objTemp.getJSONObject("header").getJSONObject("content").getString("type").equals(contentType)) {
                jobj2 = objTemp;
                break;
            }
        }
        return jobj2;
    }

    public com.alibaba.fastjson.JSONObject parseJSONBaseOnJSONStrCompatible(String JSONString,String contentType){
        String storeData = JSONString;
        com.alibaba.fastjson.JSONObject jobj2 = null;

        //如果是单独的一个则直接使用JSONObject进行解析
        if(storeData.startsWith("{")){
            jobj2 = com.alibaba.fastjson.JSONObject.parseObject(storeData);
        }else {
            //storedata是个list时
            com.alibaba.fastjson.JSONArray jsonArray2 = com.alibaba.fastjson.JSONArray.parseArray(storeData);
            for (int i = 0; i < jsonArray2.size(); i++) {
                com.alibaba.fastjson.JSONObject objTemp = com.alibaba.fastjson.JSONObject.parseObject(jsonArray2.get(i).toString());
                if (objTemp.getJSONObject("header").getJSONObject("content").getString("type").equals(contentType)) {
                    jobj2 = objTemp;
                    break;
                }
            }
        }
        return jobj2;
    }

    //------------------------------------------------------------------------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------


    public Map subjectInfoEnterprise(com.alibaba.fastjson.JSONObject jobj2){

        Map getSubjectInfo = new HashMap();
        //获取对象标识
        key = "subject_object_id";                  getSubjectInfo.put(key,getSubObjId(jobj2));

        //获取 主体信息 主体基本信息 主体通用信息
        com.alibaba.fastjson.JSONObject objInfo = jobj2.getJSONObject("body").getJSONObject("subject_information");
//        com.alibaba.fastjson.JSONObject objInfo = jobj2.getJSONObject("body").getJSONObject("subject");
        com.alibaba.fastjson.JSONObject objBaseGIS = objInfo.getJSONObject("basic_information_subject").getJSONObject("general_information_subject");
//        key = "subject_id";                          getSubjectInfo.put(key,objBaseGIS.getString(key));
        key = "subject_type";                               getSubjectInfo.put(key,objBaseGIS.getString(key));
        key = "subject_main_administrative_region";                      getSubjectInfo.put(key,objBaseGIS.getString(key));
        key = "subject_create_time";                        getSubjectInfo.put(key,objBaseGIS.getString(key));

        //获取 主体信息 主体基本信息 主体资质信息
//        com.alibaba.fastjson.JSONObject objBaseSQI = objInfo.getJSONObject("basic_information_subject").getJSONObject("subject_qualification_information");
        key = "subject_qualification_information";      getSubjectInfo.put(key,objInfo.getJSONObject("basic_information_subject").getString(key));

        //获取 主体信息 机构主体信息 企业基本信息 基本信息描述
        com.alibaba.fastjson.JSONObject objBID = objInfo.getJSONObject("organization_subject_information"
                        ).getJSONObject("basic_information_of_enterprise").getJSONObject("basic_information_description");

        key = "subject_company_name";getSubjectInfo.put(key,objBID.getString(key));
        key = "subject_company_english_name";getSubjectInfo.put(key,objBID.getString(key));
        key = "subject_company_short_name";getSubjectInfo.put(key,objBID.getString(key));
        key = "subject_company_short_english_name";getSubjectInfo.put(key,objBID.getString(key));
        key = "subject_organization_nature";getSubjectInfo.put(key,objBID.getString(key));
        key = "subject_legal_type";getSubjectInfo.put(key,objBID.getString(key));
        key = "subject_economic_type";getSubjectInfo.put(key,objBID.getString(key));
        key = "subject_company_type";getSubjectInfo.put(key,objBID.getString(key));
        key = "subject_scale_type";getSubjectInfo.put(key,objBID.getString(key));
        key = "subject_high_technology_enterprise";getSubjectInfo.put(key,objBID.getString(key));
        key = "subject_document_information";getSubjectInfo.put(key,objBID.getString(key));
        key = "subject_registry_date";getSubjectInfo.put(key,objBID.getString(key));
        key = "subject_business_license";getSubjectInfo.put(key,objBID.getString(key));
        key = "subject_business_scope";getSubjectInfo.put(key,objBID.getString(key));
        key = "subject_industry";getSubjectInfo.put(key,objBID.getString(key));
        key = "subject_company_business";getSubjectInfo.put(key,objBID.getString(key));
        key = "subject_company_profile";getSubjectInfo.put(key,objBID.getString(key));
        key = "subject_registered_capital";getSubjectInfo.put(key,objBID.getString(key));
        key = "subject_registered_capital_currency";getSubjectInfo.put(key,objBID.getString(key));
        key = "subject_paid_in_capital";getSubjectInfo.put(key,objBID.getString(key));
        key = "subject_paid_in_capital_currency";getSubjectInfo.put(key,objBID.getString(key));
        key = "subject_registered_address";getSubjectInfo.put(key,objBID.getString(key));
        key = "subject_province";getSubjectInfo.put(key,objBID.getString(key));
        key = "subject_city";getSubjectInfo.put(key,objBID.getString(key));
        key = "subject_district";getSubjectInfo.put(key,objBID.getString(key));
        key = "subject_office_address";getSubjectInfo.put(key,objBID.getString(key));
        key = "subject_contact_address";getSubjectInfo.put(key,objBID.getString(key));
        key = "subject_contact_number";getSubjectInfo.put(key,objBID.getString(key));
        key = "subject_fax";getSubjectInfo.put(key,objBID.getString(key));
        key = "subject_postal_code";getSubjectInfo.put(key,objBID.getString(key));
        key = "subject_internet_address";getSubjectInfo.put(key,objBID.getString(key));
        key = "subject_mail_box";getSubjectInfo.put(key,objBID.getString(key));
        key = "subject_association_articles";getSubjectInfo.put(key,objBID.getString(key));
        key = "subject_regulator";getSubjectInfo.put(key,objBID.getString(key));
        key = "subject_shareholders_number";getSubjectInfo.put(key,objBID.getString(key));
        key = "subject_taxpayer_id_number";getSubjectInfo.put(key,objBID.getString(key));
        key = "subject_invoice_bank";getSubjectInfo.put(key,objBID.getString(key));
        key = "subject_invoice_account_number";getSubjectInfo.put(key,objBID.getString(key));
        key = "subject_invoice_address";getSubjectInfo.put(key,objBID.getString(key));
        key = "subject_invoice_telephone_number";getSubjectInfo.put(key,objBID.getString(key));
        key = "subject_approval_time";getSubjectInfo.put(key,objBID.getString(key));
        key = "subject_insured_number";getSubjectInfo.put(key,objBID.getString(key));
        key = "subject_company_status";getSubjectInfo.put(key,objBID.getString(key));
        key = "subject_company_status_deregistration";getSubjectInfo.put(key,objBID.getString(key));
        key = "subject_company_status_deregistration_date";getSubjectInfo.put(key,objBID.getString(key));
        key = "subject_company_status_windingup";getSubjectInfo.put(key,objBID.getString(key));
        key = "subject_company_status_windingup_date";getSubjectInfo.put(key,objBID.getString(key));
        key = "subject_name_used_before";getSubjectInfo.put(key,objBID.getString(key));
        key = "subject_personnel_size";getSubjectInfo.put(key,objBID.getString(key));


        //获取 主体信息 机构主体信息 企业基本信息 主要人员信息
        com.alibaba.fastjson.JSONObject objLMI = objInfo.getJSONObject("organization_subject_information"
        ).getJSONObject("basic_information_of_enterprise");
        key = "leading_member_information";getSubjectInfo.put(key,objLMI.getString(key));

        //填充header content字段
        addContent(getSubjectInfo,jobj2);

        return getSubjectInfo;
    }

    public Map subjectInfoPerson(com.alibaba.fastjson.JSONObject jobj2){

        Map getSubjectInfo = new HashMap();

        //获取对象标识信息
        key = "subject_object_id";                  getSubjectInfo.put(key,getSubObjId(jobj2));

        com.alibaba.fastjson.JSONObject objInfo = jobj2.getJSONObject("body").getJSONObject("subject_information");


        //获取主体信息 主体基本信息
        com.alibaba.fastjson.JSONObject objSubBase = objInfo.getJSONObject("basic_information_subject");
        com.alibaba.fastjson.JSONObject objSubBaseCommon = objSubBase.getJSONObject("general_information_subject");

        //获取 主体信息 主体基本信息 主体通用信息
        key = "subject_main_administrative_region";         getSubjectInfo.put(key,objSubBaseCommon.getString(key));
//        key = "subject_id";                                 getSubjectInfo.put(key,objSubBaseCommon.getString(key));
        key = "subject_type";                               getSubjectInfo.put(key,objSubBaseCommon.getString(key));
        key = "subject_create_time";                        getSubjectInfo.put(key,objSubBaseCommon.getString(key));

        //获取 主体信息 主体基本信息 主体资质信息
        key = "subject_qualification_information";          getSubjectInfo.put(key,objSubBase.getString(key));

        //获取 主体信息 个人主体信息 个人主体基本信息
        com.alibaba.fastjson.JSONObject objPersonSub = objInfo.getJSONObject("personal_subject_information");
        com.alibaba.fastjson.JSONObject objPersonSubBase = objPersonSub.getJSONObject("personal_subject_basic_information");
        key = "subject_investor_name";                      getSubjectInfo.put(key,objPersonSubBase.getString(key));
        key = "subject_id_type";                            getSubjectInfo.put(key,objPersonSubBase.getString(key));
        key = "subject_id_number";                          getSubjectInfo.put(key,objPersonSubBase.getString(key));
        key = "subject_id_address";                         getSubjectInfo.put(key,objPersonSubBase.getString(key));
        key = "subject_contact_address";                    getSubjectInfo.put(key,objPersonSubBase.getString(key));
        key = "subject_contact_number";                     getSubjectInfo.put(key,objPersonSubBase.getString(key));
        key = "subject_cellphone_number";                   getSubjectInfo.put(key,objPersonSubBase.getString(key));
        key = "subject_personal_fax";                       getSubjectInfo.put(key,objPersonSubBase.getString(key));
        key = "subject_postal_code";                        getSubjectInfo.put(key,objPersonSubBase.getString(key));
        key = "subject_id_doc_mailbox";                     getSubjectInfo.put(key,objPersonSubBase.getString(key));
        key = "subject_education";                          getSubjectInfo.put(key,objPersonSubBase.getString(key));
        key = "subject_occupation";                         getSubjectInfo.put(key,objPersonSubBase.getString(key));
        key = "subject_industry";                           getSubjectInfo.put(key,objPersonSubBase.getString(key));
        key = "subject_birthday";                           getSubjectInfo.put(key,objPersonSubBase.getString(key));
        key = "subject_gender";                             getSubjectInfo.put(key,objPersonSubBase.getString(key));
        key = "subject_work_unit";                          getSubjectInfo.put(key,objPersonSubBase.getString(key));
        key = "subject_investment_period";                  getSubjectInfo.put(key,objPersonSubBase.getString(key));
        key = "subject_investment_experience";              getSubjectInfo.put(key,objPersonSubBase.getString(key));
        key = "subject_native_place";                       getSubjectInfo.put(key,objPersonSubBase.getString(key));
        key = "subject_province";                           getSubjectInfo.put(key,objPersonSubBase.getString(key));
        key = "subject_city";                               getSubjectInfo.put(key,objPersonSubBase.getString(key));

//        //填充header content字段
//        key = "content";getSubjectInfo.put(key,jobj2.getJSONObject("header").getString(key));

        addContent(getSubjectInfo,jobj2);

        return getSubjectInfo;
    }

    public Map accountInfo(com.alibaba.fastjson.JSONObject jobj2,String type){
        com.alibaba.fastjson.JSONObject objInfo = jobj2.getJSONObject("body").getJSONObject("account_information");

        com.alibaba.fastjson.JSONObject objAccbase = objInfo.getJSONObject("basic_account_information");

        com.alibaba.fastjson.JSONObject objAccLife = objInfo.getJSONObject("account_cycle_information");
        com.alibaba.fastjson.JSONObject objAccLifeOpen = objAccLife.getJSONObject("account_opening_information");
        com.alibaba.fastjson.JSONObject objAccLifeCancel = objAccLife.getJSONObject("account_cancellation_information");
        com.alibaba.fastjson.JSONObject objAccLifeFreeze = objAccLife.getJSONObject("freeze_information");
        com.alibaba.fastjson.JSONObject objAccLifeUnfreeze = objAccLife.getJSONObject("unfreezing_information");

        com.alibaba.fastjson.JSONObject objAccRela = objInfo.getJSONObject("account_related_information");
        Map getSubjectInfo = new HashMap();
        key = "account_object_id";                    getSubjectInfo.put(key,getSubObjId(jobj2));

        //账户信息 账户基本信息
        key = "account_subject_ref";                  getSubjectInfo.put(key,objAccbase.getString(key));
        key = "account_depository_ref";               getSubjectInfo.put(key,objAccbase.getString(key));
        key = "account_number";                       getSubjectInfo.put(key,objAccbase.getString(key));
        key = "account_type";                         getSubjectInfo.put(key,objAccbase.getString(key));
        key = "account_purpose";                      getSubjectInfo.put(key,objAccbase.getString(key));
        key = "account_status";                       getSubjectInfo.put(key,objAccbase.getString(key));
        key = "account_create_time";                  getSubjectInfo.put(key,objAccbase.getString(key));

        //账户信息 账户生命周期信息 开户信息
        key = "account_establish_date";                  getSubjectInfo.put(key,objAccLifeOpen.getString(key));
        key = "account_opening_date";                    getSubjectInfo.put(key,objAccLifeOpen.getString(key));
        key = "account_opening_doc";                     getSubjectInfo.put(key,objAccLifeOpen.getString(key));
        key = "account_opening_agent_name";              getSubjectInfo.put(key,objAccLifeOpen.getString(key));
        key = "account_opening_agent_contact_number";    getSubjectInfo.put(key,objAccLifeOpen.getString(key));

        //账户信息 账户生命周期信息 销户信息
        key = "account_closing_date";                   getSubjectInfo.put(key,objAccLifeCancel.getString(key));
        key = "account_closing_doc";                    getSubjectInfo.put(key,objAccLifeCancel.getString(key));
        key = "account_closing_agent_name";             getSubjectInfo.put(key,objAccLifeCancel.getString(key));
        key = "account_closing_agent_contact_number";   getSubjectInfo.put(key,objAccLifeCancel.getString(key));
//        key = "account_thaw_certificate";           getSubjectInfo.put(key,com.alibaba.fastjson.JSONObject.parseArray(objAccLifeUnfreeze.getJSONArray(key).toJSONString(), String.class));

        //账户信息 账户生命周期信息 冻结信息
        key = "account_frozen_date";                  getSubjectInfo.put(key,objAccLifeFreeze.getString(key));
        key = "account_frozen_doc";                   getSubjectInfo.put(key,objAccLifeFreeze.getString(key));
        key = "account_frozen_applicant_name";        getSubjectInfo.put(key,objAccLifeFreeze.getString(key));
        key = "account_frozen_remark";                getSubjectInfo.put(key,objAccLifeFreeze.getString(key));

        //账户信息 账户生命周期信息 解冻信息
        key = "account_thaw_date";                    getSubjectInfo.put(key,objAccLifeUnfreeze.getString(key));
        key = "account_thaw_doc";                     getSubjectInfo.put(key,objAccLifeUnfreeze.getString(key));
        key = "account_thaw_applicant_name";          getSubjectInfo.put(key,objAccLifeUnfreeze.getString(key));
        key = "account_thaw_remark";                  getSubjectInfo.put(key,objAccLifeUnfreeze.getString(key));

        //资金账户组 证券账户未组
        if(type == "2") {
            //账户信息 账户关联信息
            key = "account_association";
            getSubjectInfo.put(key, objAccRela.getString(key));
            key = "account_associated_account_ref";
            getSubjectInfo.put(key, objAccRela.getString(key));
        }

        //填充header content字段
         addContent(getSubjectInfo,jobj2);

        return getSubjectInfo;
    }

    public Map productInfo(com.alibaba.fastjson.JSONObject jobj2,String ProductType){

        Map getSubjectInfo = new HashMap();

        //对象标识对象
        key = "product_object_id";                  getSubjectInfo.put(key,getSubObjId(jobj2));

        com.alibaba.fastjson.JSONObject objInfo = jobj2.getJSONObject("body").getJSONObject("product_information");

        //产品信息 基本信息 产品基本信息
        //产品信息对象
        com.alibaba.fastjson.JSONObject objBase = objInfo.getJSONObject("essential_information");//产品信息 基本信息对象
        com.alibaba.fastjson.JSONObject objProdBase = objBase.getJSONObject("basic_product_information");//产品信息 基本信息 产品基本信息对象
        key = "product_trading_market_category";getSubjectInfo.put(key,objProdBase.getString(key));
        key = "product_market_subject_ref";getSubjectInfo.put(key,objProdBase.getString(key));
        key = "product_market_subject_name";getSubjectInfo.put(key,objProdBase.getString(key));
        key = "product_plate_trading_name";getSubjectInfo.put(key,objProdBase.getString(key));
        key = "product_issuer_subject_ref";getSubjectInfo.put(key,objProdBase.getString(key));
        key = "product_issuer_name";getSubjectInfo.put(key,objProdBase.getString(key));
        key = "product_code";getSubjectInfo.put(key,objProdBase.getString(key));
        key = "product_name";getSubjectInfo.put(key,objProdBase.getString(key));
        key = "product_name_abbreviation";getSubjectInfo.put(key,objProdBase.getString(key));
        key = "product_type_function";getSubjectInfo.put(key,objProdBase.getString(key));
        key = "product_type";getSubjectInfo.put(key,objProdBase.getString(key));
        key = "product_account_number_max";getSubjectInfo.put(key,objProdBase.getString(key));
        key = "product_info_disclosure_way";getSubjectInfo.put(key,objProdBase.getString(key));
        key = "product_scale_unit";getSubjectInfo.put(key,objProdBase.getString(key));
        key = "product_scale_currency";getSubjectInfo.put(key,objProdBase.getString(key));
        key = "product_scale";getSubjectInfo.put(key,objProdBase.getString(key));
        key = "product_customer_browsing_right";getSubjectInfo.put(key,objProdBase.getString(key));
        key = "product_issuer_contact_person";getSubjectInfo.put(key,objProdBase.getString(key));
        key = "product_issuer_contact_info";getSubjectInfo.put(key,objProdBase.getString(key));
        key = "product_create_time";getSubjectInfo.put(key,objProdBase.getString(key));

        //获取 产品信息 基本信息 服务方信息
        key = "service_provider_information"; getSubjectInfo.put(key,
                com.alibaba.fastjson.JSONObject.parseArray(objBase.getJSONArray(key).toJSONString(), Map.class));


        //产品信息 基本信息 产品文件信息
        key = "product_file_information";getSubjectInfo.put(key,
                com.alibaba.fastjson.JSONObject.parseArray(objBase.getJSONArray(key).toJSONString(), String.class));

        //产品信息 产品标的信息 资金用途信息
        com.alibaba.fastjson.JSONObject objPSI = objInfo.getJSONObject("product_subject_information");//产品信息 产品标的信息对象
        com.alibaba.fastjson.JSONObject objFundUse = objPSI.getJSONObject("fund_use_information");//产品信息 产品标的信息对象 资金用途信息
        key = "product_fund_use_type";              getSubjectInfo.put(key,objFundUse.getString(key));
        key = "product_description_fund_use";   getSubjectInfo.put(key,objFundUse.getString(key));
        key = "product_document_describing_funds";   getSubjectInfo.put(key,objFundUse.getString(key));

        //产品信息 产品标的信息 经营用途信息
        com.alibaba.fastjson.JSONObject objBusi = objPSI.getJSONObject("business_information");//产品信息 产品标的信息对象 经营用途信息
        key = "product_business_purpose_name";   getSubjectInfo.put(key,objBusi.getString(key));
        key = "product_business_purpose_details";   getSubjectInfo.put(key,objBusi.getString(key));
        key = "product_business_purpose_documents";   getSubjectInfo.put(key,objBusi.getString(key));

        //产品信息 产品标的信息 投资组合信息
        com.alibaba.fastjson.JSONObject objPortfolio = objPSI.getJSONObject("portfolio_information");//产品信息 产品标的信息对象 投资组合信息
        key = "product_investment_products_type";   getSubjectInfo.put(key,objPortfolio.getString(key));
        key = "product_investment_proportion_range";   getSubjectInfo.put(key,objPortfolio.getString(key));
        key = "product_Investment_product_details";   getSubjectInfo.put(key,objPortfolio.getString(key));
        key = "product_detailed_description_document";   getSubjectInfo.put(key,objPortfolio.getString(key));

        //产品信息 发行信息 备案信息
        com.alibaba.fastjson.JSONObject objProdIssue = objInfo.getJSONObject("release_information");//产品信息 发行信息对象
        key = "filing_information"; getSubjectInfo.put(key,objProdIssue.getString(key));

        //产品信息 发行信息
        //私募股权 既不是2也不是3则默认私募
        if(!(ProductType.equals("2") || ProductType.equals("3"))) {
            com.alibaba.fastjson.JSONObject objProdIssueEquity = objProdIssue.getJSONObject("equity_issue_information");//产品信息 发行信息 私募股权对象

            key = "product_issue_scope";getSubjectInfo.put(key,objProdIssueEquity.getString(key));
            key = "product_issue_type";getSubjectInfo.put(key,objProdIssueEquity.getString(key));
            key = "product_shares_issued_class";getSubjectInfo.put(key,objProdIssueEquity.getString(key));
            key = "release_note_information";getSubjectInfo.put(key,objProdIssueEquity.getString(key));
            key = "product_issue_price";getSubjectInfo.put(key,objProdIssueEquity.getString(key));
            key = "product_issue_price_method";getSubjectInfo.put(key,objProdIssueEquity.getString(key));
            key = "product_before_authorized_shares";getSubjectInfo.put(key,objProdIssueEquity.getString(key));
            key = "product_after_authorized_shares";getSubjectInfo.put(key,objProdIssueEquity.getString(key));
            key = "product_after_issue_market_value";getSubjectInfo.put(key,objProdIssueEquity.getString(key));
            key = "product_net_profit";getSubjectInfo.put(key,objProdIssueEquity.getString(key));
            key = "product_annual_net_profit";getSubjectInfo.put(key,objProdIssueEquity.getString(key));
            key = "product_actual_raising_scale";getSubjectInfo.put(key,objProdIssueEquity.getString(key));
            key = "product_raising_start_time";getSubjectInfo.put(key,objProdIssueEquity.getString(key));
            key = "product_raising_end_time";getSubjectInfo.put(key,objProdIssueEquity.getString(key));
            key = "product_registered_capital_before_issuance";getSubjectInfo.put(key,objProdIssueEquity.getString(key));
            key = "product_registered_capital_issuance";getSubjectInfo.put(key,objProdIssueEquity.getString(key));
            key = "product_paid_shares";getSubjectInfo.put(key,objProdIssueEquity.getString(key));
            key = "product_shares_subscribed_number";getSubjectInfo.put(key,objProdIssueEquity.getString(key));
            key = "product_unlimited_sales_number_shares";getSubjectInfo.put(key,objProdIssueEquity.getString(key));
            key = "product_restricted_shares_number";getSubjectInfo.put(key,objProdIssueEquity.getString(key));

        }
        else if(ProductType.equals("2")) {
            com.alibaba.fastjson.JSONObject objProdIssueBond = objProdIssue.getJSONObject("private_convertible_bonds");//产品信息 发行信息 私募可转债对象
            //私募可转债
            key = "product_scope_issue";getSubjectInfo.put(key,objProdIssueBond.getString(key));
            key = "product_bond_duration_unit";getSubjectInfo.put(key,objProdIssueBond.getString(key));
            key = "product_bond_duration";getSubjectInfo.put(key,objProdIssueBond.getString(key));
            key = "product_filing_amount";getSubjectInfo.put(key,objProdIssueBond.getString(key));
            key = "product_by_stages";getSubjectInfo.put(key,objProdIssueBond.getString(key));
            key = "product_bond_interest_rate_floor";getSubjectInfo.put(key,objProdIssueBond.getString(key));
            key = "product_bond_interest_rate_cap";getSubjectInfo.put(key,objProdIssueBond.getString(key));
            key = "product_staging_frequency";getSubjectInfo.put(key,objProdIssueBond.getString(key));
            key = "product_initial_issue_amount";getSubjectInfo.put(key,objProdIssueBond.getString(key));
            key = "product_initial_ratio";getSubjectInfo.put(key,objProdIssueBond.getString(key));
            key = "product_initial_interest_rate";getSubjectInfo.put(key,objProdIssueBond.getString(key));
            key = "product_interest_calculation_method";getSubjectInfo.put(key,objProdIssueBond.getString(key));
            key = "product_interest_calculation_method_remarks";getSubjectInfo.put(key,objProdIssueBond.getString(key));
            key = "product_payment_method";getSubjectInfo.put(key,objProdIssueBond.getString(key));
            key = "product_payment_method_remarks";getSubjectInfo.put(key,objProdIssueBond.getString(key));
            key = "product_is_appoint_repayment_date";getSubjectInfo.put(key,objProdIssueBond.getString(key));
            key = "product_appoint_repayment_date";getSubjectInfo.put(key,objProdIssueBond.getString(key));
            key = "product_guarantee_measure";getSubjectInfo.put(key,objProdIssueBond.getString(key));
            key = "product_converting_shares_condition";getSubjectInfo.put(key,objProdIssueBond.getString(key));
            key = "product_converting_shares_price_mode";getSubjectInfo.put(key,objProdIssueBond.getString(key));
            key = "product_converting_shares_term";getSubjectInfo.put(key,objProdIssueBond.getString(key));
            key = "product_redemption";getSubjectInfo.put(key,objProdIssueBond.getString(key));
            key = "product_issue_price";getSubjectInfo.put(key,objProdIssueBond.getString(key));
            key = "product_face_value";getSubjectInfo.put(key,objProdIssueBond.getString(key));
            key = "product_subscription_base";getSubjectInfo.put(key,objProdIssueBond.getString(key));
            key = "product_successful_release_proportion";getSubjectInfo.put(key,objProdIssueBond.getString(key));
            key = "product_fund_raising_conversion_condition";getSubjectInfo.put(key,objProdIssueBond.getString(key));
            key = "product_is_make_over";getSubjectInfo.put(key,objProdIssueBond.getString(key));
            key = "product_number_of_holders_max";getSubjectInfo.put(key,objProdIssueBond.getString(key));
            key = "product_subscription_upper_limit";getSubjectInfo.put(key,objProdIssueBond.getString(key));
            key = "product_subscription_lower_limit";getSubjectInfo.put(key,objProdIssueBond.getString(key));
            key = "product_redemption_clause";getSubjectInfo.put(key,objProdIssueBond.getString(key));
            key = "product_termination_conditions";getSubjectInfo.put(key,objProdIssueBond.getString(key));
            key = "product_duration";getSubjectInfo.put(key,objProdIssueBond.getString(key));
            key = "product_adjustment_change_control";getSubjectInfo.put(key,objProdIssueBond.getString(key));
            key = "product_conversion_premium";getSubjectInfo.put(key,objProdIssueBond.getString(key));
            key = "product_conversion_price_ref";getSubjectInfo.put(key,objProdIssueBond.getString(key));
            key = "product_actual_issue_size";getSubjectInfo.put(key,objProdIssueBond.getString(key));
            key = "product_raising_start_date";getSubjectInfo.put(key,objProdIssueBond.getString(key));
            key = "product_raising_end_date";getSubjectInfo.put(key,objProdIssueBond.getString(key));
            key = "product_start_date";getSubjectInfo.put(key,objProdIssueBond.getString(key));
            key = "product_due_date";getSubjectInfo.put(key,objProdIssueBond.getString(key));
            key = "product_amount_cashed";getSubjectInfo.put(key,objProdIssueBond.getString(key));
            key = "product_first_interest_payment_date";getSubjectInfo.put(key,objProdIssueBond.getString(key));
            key = "product_issuer_credit_rating";getSubjectInfo.put(key,objProdIssueBond.getString(key));
            key = "product_credit_enhancement_agency_credit_rating";getSubjectInfo.put(key,objProdIssueBond.getString(key));
            key = "product_guarantee_arrangement";getSubjectInfo.put(key,objProdIssueBond.getString(key));
            key = "product_repo_arrangement";getSubjectInfo.put(key,objProdIssueBond.getString(key));
            key = "product_lockup";getSubjectInfo.put(key,objProdIssueBond.getString(key));

        }
        else if(ProductType.equals("3")) {
            com.alibaba.fastjson.JSONObject objProdIssueFund = objProdIssue.getJSONObject("private_offering_fund");//产品信息 发行信息 私募基金对象
            com.alibaba.fastjson.JSONObject objProdIssueFundBase = objProdIssueFund.getJSONObject("fund_basic_information");//产品信息 发行信息 私募基金对象 基金基本信息
            com.alibaba.fastjson.JSONObject objProdIssueFundSale = objProdIssueFund.getJSONObject("fund_sales_agency");//产品信息 发行信息 私募基金对象 基金销售机构
            com.alibaba.fastjson.JSONObject objProdIssueFundMger = objProdIssueFund.getJSONObject("fund_manager_information");//产品信息 发行信息 私募基金对象 基金管理人信息

            //私募基金 基金基本信息
            key = "product_raising_information_identification";getSubjectInfo.put(key,objProdIssueFundBase.getString(key));
            key = "product_scope_fund_raising";getSubjectInfo.put(key,objProdIssueFundBase.getString(key));
            key = "product_record_number";getSubjectInfo.put(key,objProdIssueFundBase.getString(key));
            key = "product_fund_filing_date";getSubjectInfo.put(key,objProdIssueFundBase.getString(key));
            key = "product_fund_type";getSubjectInfo.put(key,objProdIssueFundBase.getString(key));
            key = "product_foundation_date";getSubjectInfo.put(key,objProdIssueFundBase.getString(key));
            key = "product_escrow_bank";getSubjectInfo.put(key,objProdIssueFundBase.getString(key));
            key = "product_total_fund_share";getSubjectInfo.put(key,objProdIssueFundBase.getString(key));
            key = "product_fund_unit_holders_number";getSubjectInfo.put(key,objProdIssueFundBase.getString(key));
            key = "product_fund_nav";getSubjectInfo.put(key,objProdIssueFundBase.getString(key));
            key = "product_fund_fairvalue";getSubjectInfo.put(key,objProdIssueFundBase.getString(key));
            key = "product_raise_start_date";getSubjectInfo.put(key,objProdIssueFundBase.getString(key));
            key = "product_raise_end_date";getSubjectInfo.put(key,objProdIssueFundBase.getString(key));

            //私募基金 基金销售机构
            key = "sales_organization_name";getSubjectInfo.put(key,objProdIssueFundSale.getString(key));
            key = "product_unified_social_credit_code";getSubjectInfo.put(key,objProdIssueFundSale.getString(key));
            key = "product_sales_organization_member_code";getSubjectInfo.put(key,objProdIssueFundSale.getString(key));

            //私募基金 基金管理人信息
            key = "product_fund_manager_name";getSubjectInfo.put(key,objProdIssueFundMger.getString(key));
            key = "product_fund_manager_certificate_number";getSubjectInfo.put(key,objProdIssueFundMger.getString(key));
            key = "product_management_style";getSubjectInfo.put(key,objProdIssueFundMger.getString(key));
            key = "product_funds_under_management_number";getSubjectInfo.put(key,objProdIssueFundMger.getString(key));
            key = "product_fund_management_scale";getSubjectInfo.put(key,objProdIssueFundMger.getString(key));

        }

        //产品信息 交易信息
        com.alibaba.fastjson.JSONObject objProdTran = objInfo.getJSONObject("transaction_information");//产品信息 交易信息对象
        //产品信息 交易信息 交易状态
        com.alibaba.fastjson.JSONObject objProdTxStatus = objProdTran.getJSONObject("transaction_status");//产品信息 交易信息对象
        key = "product_transaction_status";getSubjectInfo.put(key,objProdTxStatus.getString(key));

        //产品信息 交易信息 挂牌信息
        com.alibaba.fastjson.JSONObject objProdTxList = objProdTran.getJSONObject("listing_information");//产品信息 交易信息对象
        key = "product_transaction_scope";getSubjectInfo.put(key,objProdTxList.getString(key));
        key = "product_transfer_permission_institution_to_individual";getSubjectInfo.put(key,objProdTxList.getString(key));
        key = "product_transfer_lockup_days";getSubjectInfo.put(key,objProdTxList.getString(key));
        key = "product_transfer_validity";getSubjectInfo.put(key,objProdTxList.getString(key));
        key = "product_risk_level";getSubjectInfo.put(key,objProdTxList.getString(key));
        key = "product_transaction_unit";getSubjectInfo.put(key,objProdTxList.getString(key));
        key = "product_listing_code";getSubjectInfo.put(key,objProdTxList.getString(key));
        key = "product_listing_date";getSubjectInfo.put(key,objProdTxList.getString(key));
        key = "product_listing_remarks";getSubjectInfo.put(key,objProdTxList.getString(key));

        //产品信息 交易信息 摘牌信息
        com.alibaba.fastjson.JSONObject objProdTxDelist = objProdTran.getJSONObject("delisting_information");//产品信息 交易信息对象
        key = "product_delisting_date";getSubjectInfo.put(key,objProdTxDelist.getString(key));
        key = "product_delisting_type";getSubjectInfo.put(key,objProdTxDelist.getString(key));
        key = "product_delisting_reason";getSubjectInfo.put(key,objProdTxDelist.getString(key));
        key = "product_transfer_board_market";getSubjectInfo.put(key,objProdTxDelist.getString(key));
        key = "product_acquisition_company_market";getSubjectInfo.put(key,objProdTxDelist.getString(key));
        key = "product_delisting_remarks";getSubjectInfo.put(key,objProdTxDelist.getString(key));

        //产品信息 托管信息
        com.alibaba.fastjson.JSONObject objProdEscrow = objInfo.getJSONObject("escrow_information");//产品信息 托管信息对象
        key = "product_custodian_registration_date";getSubjectInfo.put(key,objProdEscrow.getString(key));
        key = "product_custodian_documents";getSubjectInfo.put(key,objProdEscrow.getString(key));
        key = "product_custodian_notes";getSubjectInfo.put(key,objProdEscrow.getString(key));
        key = "product_escrow_deregistration_date";getSubjectInfo.put(key,objProdEscrow.getString(key));
        key = "product_escrow_deregistration_document";getSubjectInfo.put(key,objProdEscrow.getString(key));
        key = "product_escrow_deregistration_remarks";getSubjectInfo.put(key,objProdEscrow.getString(key));

        //填充header content字段
         addContent(getSubjectInfo,jobj2);

        return getSubjectInfo;
    }

    public Map transInfo(com.alibaba.fastjson.JSONObject jobj2){
        com.alibaba.fastjson.JSONObject objInfo = jobj2.getJSONObject("body").getJSONObject("transaction_report_information");

        com.alibaba.fastjson.JSONObject objBase = objInfo.getJSONObject("basic_transaction_information");
        com.alibaba.fastjson.JSONObject objBaseBIR = objBase.getJSONObject("basic_information_remark");
        com.alibaba.fastjson.JSONObject objBaseTAI = objBase.getJSONObject("trading_asset_information");


        Map getSubjectInfo = new HashMap();
        key = "transaction_object_id";                      getSubjectInfo.put(key,getSubObjId(jobj2));
//        key = "transaction_object_information_type";        getSubjectInfo.put(key,objInfo.getString(key));

        //交易报告信息 交易基本信息 基本信息描述
        key = "transaction_market_type";getSubjectInfo.put(key,objBaseBIR.getString(key));
        key = "transaction_type";getSubjectInfo.put(key,objBaseBIR.getString(key));
        key = "transaction_method";getSubjectInfo.put(key,objBaseBIR.getString(key));
        key = "transaction_description";getSubjectInfo.put(key,objBaseBIR.getString(key));
        key = "transaction_create_time";getSubjectInfo.put(key,objBaseBIR.getString(key));


        //交易报告信息 交易基本信息 交易资产信息
        com.alibaba.fastjson.JSONObject objBaseTAIACS = objBaseTAI.getJSONObject("asset_custody_state");
        com.alibaba.fastjson.JSONObject objBaseTAICAT = objBaseTAI.getJSONObject("custody_asset_transaction");
        com.alibaba.fastjson.JSONObject objBaseTAINCAT = objBaseTAI.getJSONObject("no_custody_asset_transaction");

        //交易报告信息 交易基本信息 交易资产信息 交易资产托管状态
        key = "transaction_product_custody_status";                     getSubjectInfo.put(key,objBaseTAIACS.getString(key));
        //交易报告信息 交易基本信息 交易资产信息 已托管交易资产交易
        key = "transaction_custody_product_ref";                   getSubjectInfo.put(key,objBaseTAICAT.getString(key));
        //交易报告信息 交易基本信息 交易资产信息 未托管交易资产交易
        key = "transaction_product_name";getSubjectInfo.put(key,objBaseTAINCAT.getString(key));
        key = "transaction_product_issuer_ref";getSubjectInfo.put(key,objBaseTAINCAT.getString(key));
        key = "transaction_product_issuer_name";getSubjectInfo.put(key,objBaseTAINCAT.getString(key));
        key = "transaction_product_asset_type";getSubjectInfo.put(key,objBaseTAINCAT.getString(key));
        key = "transaction_product_asset_unit";getSubjectInfo.put(key,objBaseTAINCAT.getString(key));
        key = "transaction_product_asset_currency";getSubjectInfo.put(key,objBaseTAINCAT.getString(key));
        key = "transaction_product_asset_value";getSubjectInfo.put(key,objBaseTAINCAT.getString(key));
        key = "transaction_product_asset_doc";getSubjectInfo.put(key,objBaseTAINCAT.getString(key));
        key = "transaction_product_description";getSubjectInfo.put(key,objBaseTAINCAT.getString(key));

        //交易报告信息 交易成交信息
        com.alibaba.fastjson.JSONObject objDeal = objInfo.getJSONObject("transaction_information");
        com.alibaba.fastjson.JSONObject objDealContent = objDeal.getJSONObject("transaction_content_information");
        com.alibaba.fastjson.JSONObject objDealFina = objDeal.getJSONObject("financing_transaction_party_information");
        com.alibaba.fastjson.JSONObject objDealTxParty = objDeal.getJSONObject("transaction_party_information");
        com.alibaba.fastjson.JSONObject objDealVerify = objDeal.getJSONObject("transaction_verification_information");
        //交易报告信息 交易成交信息 成交内容信息
        key = "transaction_series_number";getSubjectInfo.put(key,objDealContent.getString(key));
        key = "transaction_settlement_currency";getSubjectInfo.put(key,objDealContent.getString(key));
        key = "transaction_settlement_price";getSubjectInfo.put(key,objDealContent.getString(key));
        key = "transaction_settlement_quantity";getSubjectInfo.put(key,objDealContent.getString(key));
        key = "transaction_settlement_time";getSubjectInfo.put(key,objDealContent.getString(key));
        key = "transaction_settlement_description";getSubjectInfo.put(key,objDealContent.getString(key));

        //交易报告信息 交易成交信息 融资类交易成交方信息
        key = "transaction_issuer_ref";getSubjectInfo.put(key,objDealFina.getString(key));
        key = "transaction_issuer_name";getSubjectInfo.put(key,objDealFina.getString(key));
        key = "transaction_investor_ref";getSubjectInfo.put(key,objDealFina.getString(key));
        key = "transaction_investor_name";getSubjectInfo.put(key,objDealFina.getString(key));

        //交易报告信息 交易成交信息 交易成交方信息
        key = "transaction_investor_original_ref";getSubjectInfo.put(key,objDealTxParty.getString(key));
        key = "transaction_investor_original_name";getSubjectInfo.put(key,objDealTxParty.getString(key));
        key = "transaction_investor_counterparty_ref";getSubjectInfo.put(key,objDealTxParty.getString(key));
        key = "transaction_investor_counterparty_name";getSubjectInfo.put(key,objDealTxParty.getString(key));

        //交易报告信息 交易成交信息 成交核验信息
        key = "transaction_orderplacing_verification_doc";getSubjectInfo.put(key,objDealVerify.getString(key));
        key = "transaction_verification_doc";getSubjectInfo.put(key,objDealVerify.getString(key));

        //交易报告信息 交易中介信息
        key = "transaction_intermediary_information";
        getSubjectInfo.put(key,com.alibaba.fastjson.JSONObject.parseArray(
                jobj2.getJSONObject("body").getJSONObject("transaction_report_information").getJSONArray(key).toJSONString(), Map.class));

        //填充header content字段
         addContent(getSubjectInfo,jobj2);

        return getSubjectInfo;
    }

    public Map addContent(Map mapSrc,com.alibaba.fastjson.JSONObject jobj2){
        //填充header content字段
        TreeMap contentMap = new TreeMap(
                com.alibaba.fastjson.JSONObject.toJavaObject(
                        jobj2.getJSONObject("header").getJSONObject("content"), Map.class));
        key = "content";mapSrc.put(key,contentMap);
        return mapSrc;
    }


    public Map regiInfo(com.alibaba.fastjson.JSONObject jobj2){
        com.alibaba.fastjson.JSONObject objInfo = jobj2.getJSONObject("body").getJSONObject("registration_information");
        com.alibaba.fastjson.JSONObject objRegBase = objInfo.getJSONObject("register_basic_information");

        Map getSubjectInfo = new HashMap();

        log.info("-------------------------"+getSubObjId(jobj2));

        //登记信息 登记基本信息
        key = "register_registration_object_id"; getSubjectInfo.put(key,getSubObjId(jobj2));
        log.info(getSubjectInfo.toString());
        key = "register_object_type";        getSubjectInfo.put(key,objRegBase.getString(key));
        key = "register_event_type";       getSubjectInfo.put(key,objRegBase.getString(key));
        log.info(getSubjectInfo.toString());

        if(regObjType == 1) {
            com.alibaba.fastjson.JSONObject objRegRights = objInfo.getJSONObject("registration_rights");
            com.alibaba.fastjson.JSONObject objRegRigBase = objRegRights.getJSONObject("basic_information_rights");
            //登记信息 权利登记 权利基本信息 权利登记基本信息描述
            com.alibaba.fastjson.JSONObject objRegRigBaseDesp = objRegRigBase.getJSONObject("basic_information_description");

            key = "register_serial_number";
            getSubjectInfo.put(key, objRegRigBaseDesp.getString(key));
            key = "register_time";
            getSubjectInfo.put(key, objRegRigBaseDesp.getString(key));
            key = "register_subject_ref";
            getSubjectInfo.put(key, objRegRigBaseDesp.getString(key));
            key = "register_subject_type";
            getSubjectInfo.put(key, objRegRigBaseDesp.getString(key));
            key = "register_subject_account_ref";
            getSubjectInfo.put(key, objRegRigBaseDesp.getString(key));
            key = "register_asset_type";
            getSubjectInfo.put(key, objRegRigBaseDesp.getString(key));
            key = "register_asset_unit";
            getSubjectInfo.put(key, objRegRigBaseDesp.getString(key));
            key = "register_asset_currency";
            getSubjectInfo.put(key, objRegRigBaseDesp.getString(key));
            if (register_event_type == 2) {
                key = "register_transaction_ref";
                getSubjectInfo.put(key, objRegRigBaseDesp.getString(key));
            }
            log.info(getSubjectInfo.toString());
            key = "register_product_ref";
            getSubjectInfo.put(key, objRegRigBaseDesp.getString(key));
            key = "register_description";
            getSubjectInfo.put(key, objRegRigBaseDesp.getString(key));
            key = "register_create_time";
            getSubjectInfo.put(key, objRegRigBaseDesp.getString(key));
            key = "register_authentic_right_recognition_status";
            getSubjectInfo.put(key, objRegRigBaseDesp.getString(key));

            //登记信息 权利登记 权利基本信息 确权记录
            com.alibaba.fastjson.JSONObject objRegRigBaseAuth = objRegRigBase.getJSONObject("authentic_right_record");

            key = "register_authentic_right_recognition_date";
            getSubjectInfo.put(key, objRegRigBaseAuth.getString(key));
            key = "register_right_recognition_mode";
            getSubjectInfo.put(key, objRegRigBaseAuth.getString(key));
            key = "register_right_recognition_subject_ref";
            getSubjectInfo.put(key, objRegRigBaseAuth.getString(key));
            key = "register_right_recognition_subject_name";
            getSubjectInfo.put(key, objRegRigBaseAuth.getString(key));
            key = "register_right_recognition_agent_subject_ref";
            getSubjectInfo.put(key, objRegRigBaseAuth.getString(key));
            key = "register_right_recognition_agent_subject_name";
            getSubjectInfo.put(key, objRegRigBaseAuth.getString(key));
            key = "register_right_recognition_doc";
            getSubjectInfo.put(key, objRegRigBaseAuth.getString(key));
            key = "register_right_recognition_description";
            getSubjectInfo.put(key, objRegRigBaseAuth.getString(key));

            //登记信息 权利登记 权利基本信息 可用登记
            com.alibaba.fastjson.JSONObject objRegRigBaseAvail = objRegRigBase.getJSONObject("available_registration");
            key = "register_asset_balance_change";
            getSubjectInfo.put(key, objRegRigBaseAvail.getString(key));
            key = "register_asset_balance_before";
            getSubjectInfo.put(key, objRegRigBaseAvail.getString(key));
            key = "register_asset_balance_after";
            getSubjectInfo.put(key, objRegRigBaseAvail.getString(key));

            //登记信息 权利登记 权利基本信息 质押登记
            com.alibaba.fastjson.JSONObject objRegRigBasePledge = objRegRigBase.getJSONObject("pledge_registration");
            key = "register_pledge_balance_change";
            getSubjectInfo.put(key, objRegRigBasePledge.getString(key));
            key = "register_pledge_balance_before";
            getSubjectInfo.put(key, objRegRigBasePledge.getString(key));
            key = "register_pledge_balance_after";
            getSubjectInfo.put(key, objRegRigBasePledge.getString(key));

            //登记信息 权利登记 权利基本信息 冻结登记
            com.alibaba.fastjson.JSONObject objRegRigBaseFreeze = objRegRigBase.getJSONObject("freezing_registration");
            key = "register_frozen_balance_change";
            getSubjectInfo.put(key, objRegRigBaseFreeze.getString(key));
            key = "register_frozen_balance";
            getSubjectInfo.put(key, objRegRigBaseFreeze.getString(key));
            key = "register_frozen_balance_after";
            getSubjectInfo.put(key, objRegRigBaseFreeze.getString(key));
            key = "register_thaw_doc";
            getSubjectInfo.put(key, objRegRigBaseFreeze.getString(key));
            key = "register_thaw_description";
            getSubjectInfo.put(key, objRegRigBaseFreeze.getString(key));

            //登记信息 权利登记 权利基本信息 状态信息描述
            com.alibaba.fastjson.JSONObject objRegRigBaseStatus = objRegRigBase.getJSONObject("description_status_information");
            key = "register_asset_holding_status";
            getSubjectInfo.put(key, objRegRigBaseStatus.getString(key));
            key = "register_asset_holding_status_description";
            getSubjectInfo.put(key, objRegRigBaseStatus.getString(key));
            key = "register_asset_holding_nature";
            getSubjectInfo.put(key, objRegRigBaseStatus.getString(key));
            key = "register_asset_equity_type";
            getSubjectInfo.put(key, objRegRigBaseStatus.getString(key));
            key = "register_source_type";
            getSubjectInfo.put(key, objRegRigBaseStatus.getString(key));
            key = "register_asset_note";
            getSubjectInfo.put(key, objRegRigBaseStatus.getString(key));
            key = "register_asset_verification_doc";
            getSubjectInfo.put(key, objRegRigBaseStatus.getString(key));
        }else if(regObjType == 2) {
            //名册登记 名册基本信息
            com.alibaba.fastjson.JSONObject objRollRecords = objInfo.getJSONObject("roll_records");
            com.alibaba.fastjson.JSONObject objRollBase = objRollRecords.getJSONObject("basic_information_roster");

            key = "register_subject_ref";
            getSubjectInfo.put(key, objRollBase.getString(key));
            key = "register_product_ref";
            getSubjectInfo.put(key, objRollBase.getString(key));
            key = "register_product_name";
            getSubjectInfo.put(key, objRollBase.getString(key));
            key = "register_product_description";
            getSubjectInfo.put(key, objRollBase.getString(key));
            key = "register_list_asset_type";
            getSubjectInfo.put(key, objRollBase.getString(key));
            key = "register_list_date";
            getSubjectInfo.put(key, objRollBase.getString(key));

            //名册登记 股东名册
            key = "register_shareholders";
            getSubjectInfo.put(key, objRollRecords.getString(key));
            //名册登记 债权人名册
            key = "register_creditors";
            getSubjectInfo.put(key, objRollRecords.getString(key));
            //名册登记 基金投资人名册
            key = "fund_investors";
            getSubjectInfo.put(key, objRollRecords.getString(key));
        }

        //填充header content字段
         addContent(getSubjectInfo,jobj2);
        log.info(getSubjectInfo.toString());
        return getSubjectInfo;
    }

    public Map settleInfo(com.alibaba.fastjson.JSONObject jobj2){

        com.alibaba.fastjson.JSONObject objInfo = jobj2.getJSONObject("body").getJSONObject("capital_settlement_information");
        com.alibaba.fastjson.JSONObject objBase = objInfo.getJSONObject("basic_information_capital_settlement");
        com.alibaba.fastjson.JSONObject objOut = objInfo.getJSONObject("transferor_information");
        com.alibaba.fastjson.JSONObject objIn = objInfo.getJSONObject("transferee_information");

        Map getSubjectInfo = new HashMap();
//        key = "capita_settlement_object_id";                        getSubjectInfo.put(key,getSubObjId(jobj2));

        key = "settlement_subject_ref";                             getSubjectInfo.put(key,objBase.getString(key));
        key = "settlement_type";                                    getSubjectInfo.put(key,objBase.getString(key));
        key = "settlement_serial_number";                           getSubjectInfo.put(key,objBase.getString(key));
        key = "settlement_time";                                    getSubjectInfo.put(key,objBase.getString(key));
        key = "settlement_product_ref";                             getSubjectInfo.put(key,objBase.getString(key));
        key = "settlement_transaction_ref";                         getSubjectInfo.put(key,objBase.getString(key));
        key = "settlement_currency";                                getSubjectInfo.put(key,objBase.getString(key));
        key = "settlement_value";                                   getSubjectInfo.put(key,objBase.getString(key));
        key = "settlement_note";                                    getSubjectInfo.put(key,objBase.getString(key));
        key = "settlement_operation_doc";                           getSubjectInfo.put(key,objBase.getString(key));
        key = "settlement_information_maintenance_time";            getSubjectInfo.put(key,objBase.getString(key));

        key = "settlement_out_bank_code";                           getSubjectInfo.put(key,objOut.getString(key));
        key = "settlement_out_bank_name";                           getSubjectInfo.put(key,objOut.getString(key));
        key = "settlement_out_bank_account";                        getSubjectInfo.put(key,objOut.getString(key));
        key = "settlement_out_account_object_ref";                  getSubjectInfo.put(key,objOut.getString(key));
        key = "settlement_out_account_name";                        getSubjectInfo.put(key,objOut.getString(key));
        key = "settlement_out_account_balance_before_transfer";     getSubjectInfo.put(key,objOut.getString(key));
        key = "settlement_out_account_balance_after_transfer";      getSubjectInfo.put(key,objOut.getString(key));

        key = "settlement_in_bank_code";                            getSubjectInfo.put(key,objIn.getString(key));
        key = "settlement_in_bank_name";                            getSubjectInfo.put(key,objIn.getString(key));
        key = "settlement_in_bank_account";                         getSubjectInfo.put(key,objIn.getString(key));
        key = "settlement_in_account_object_ref";                   getSubjectInfo.put(key,objIn.getString(key));
        key = "settlement_in_account_name";                         getSubjectInfo.put(key,objIn.getString(key));
        key = "settlement_in_account_balance_before_transfer";      getSubjectInfo.put(key,objIn.getString(key));
        key = "settlement_in_account_balance_after_transfer";       getSubjectInfo.put(key,objIn.getString(key));

        //填充header content字段
         addContent(getSubjectInfo,jobj2);

        return getSubjectInfo;
    }

    public Map pubInfo(com.alibaba.fastjson.JSONObject jobj2) {
        com.alibaba.fastjson.JSONObject objInfo = jobj2.getJSONObject("body").getJSONObject("disclosure_approval_information");
        com.alibaba.fastjson.JSONObject objBaic = objInfo.getJSONObject("disclosure_basic_information");

        Map getSubjectInfo = new HashMap();
        String keyType = "";

        //信批基本信息
        key = "disclosure_subject_ref";                          getSubjectInfo.put(key, objBaic.getString(key));
        key = "disclosure_type";                                 getSubjectInfo.put(key, objBaic.getString(key));keyType=objBaic.getString(key);
        key = "disclosure_submit_type";                          getSubjectInfo.put(key, objBaic.getString(key));
        key = "disclosure_referer_subject_ref";                  getSubjectInfo.put(key, objBaic.getString(key));
        key = "disclosure_referer_name";                         getSubjectInfo.put(key, objBaic.getString(key));
        key = "disclosure_submit_date";                          getSubjectInfo.put(key, objBaic.getString(key));
        key = "disclosure_submit_description";                   getSubjectInfo.put(key, objBaic.getString(key));

        switch (keyType) {
            case "1":
                //拼组企业展示信息1
                key = "enterprise_display_information";
                getSubjectInfo.put(key, objInfo.getString(key));
                break;
            case "3":
                //拼组监管信息3
                key = "regulatory_information";
                getSubjectInfo.put(key, objInfo.getString(key));
                break;
            case "4":
                //拼组企业报告4
                key = "enterprise_report";
                getSubjectInfo.put(key, objInfo.getString(key));
                break;
            case "5":
                //拼组公告信息5
                key = "disclosure_notice";
                getSubjectInfo.put(key, objInfo.getString(key));
                break;
            case "6":
                //拼组重大事件信息6
                key = "major_event_information";
                getSubjectInfo.put(key, objInfo.getString(key));
                break;
            case "7":
                //拼组诚信档案信息7
                key = "integrity_archives";
                JSONObject objBaseInfo = JSONObject.fromObject(objInfo.getJSONArray(key).get(0).toString()).getJSONObject("basic_information");
                JSONObject objItemDetails = JSONObject.fromObject(objInfo.getJSONArray(key).get(0).toString()).getJSONObject("item_details");

                Map mapIR = new HashMap();

                key = "disclosure_identifier_ref"; mapIR.put(key, objBaseInfo.getString(key));
                key = "disclosure_identifier_name"; mapIR.put(key, objBaseInfo.getString(key));
                key = "disclosure_auditor_ref" ; mapIR.put(key, objBaseInfo.getString(key));
                key = "disclosure_auditor_name" ; mapIR.put(key, objBaseInfo.getString(key));

                key = "disclosure_event_id" ; mapIR.put(key, objItemDetails.getString(key));
                key = "disclosure_event_name" ; mapIR.put(key, objItemDetails.getString(key));
                key = "disclosure_event_doc" ; mapIR.put(key, objItemDetails.getString(key));
                key = "disclosure_event_description" ; mapIR.put(key, objItemDetails.getString(key));
                key = "disclosure_event_type" ; mapIR.put(key, objItemDetails.getString(key));
                key = "disclosure_event_valid_time" ; mapIR.put(key, objItemDetails.getString(key));
                key = "disclosure_event_start_date" ; mapIR.put(key, objItemDetails.getString(key));
                key = "disclosure_event_end_date"; mapIR.put(key, objItemDetails.getString(key));
                key = "disclosure_event_status"; mapIR.put(key, objItemDetails.getString(key));

                ArrayList listIR = new ArrayList();
                listIR.add(mapIR);

                key = "integrity_archives"; getSubjectInfo.put(key, listIR);
                break;
            case "8":
                //拼组财务信息8
                key = "financial_information";
                JSONObject objBaseFi = JSONObject.fromObject(objInfo.getJSONArray(key).get(0).toString()).getJSONObject("basic_financial_information");
                JSONObject objFiStat = JSONObject.fromObject(objInfo.getJSONArray(key).get(0).toString()).getJSONObject("financial_statement_documents");

                Map mapFi = new HashMap();

                key = "disclosure_financial_start_date";mapFi.put(key, objBaseFi.getString(key));
                key = "disclosure_financial_end_date" ;mapFi.put(key, objBaseFi.getString(key));
                key = "disclosure_financial_type" ;mapFi.put(key, objBaseFi.getString(key));
                key = "disclosure_financial_periodend_total_asset" ;mapFi.put(key, objBaseFi.getString(key));
                key = "disclosure_financial_periodend_net_asset" ;mapFi.put(key, objBaseFi.getString(key));
                key = "disclosure_financial_periodend_total_liability" ;mapFi.put(key, objBaseFi.getString(key));
                key = "disclosure_financial_periodend_revenue" ;mapFi.put(key, objBaseFi.getString(key));
                key = "disclosure_financial_periodend_gross_profit" ;mapFi.put(key, objBaseFi.getString(key));
                key = "disclosure_financial_periodend_net_profit" ;mapFi.put(key, objBaseFi.getString(key));
                key = "disclosure_financial_cashflow" ;mapFi.put(key, objBaseFi.getString(key));
                key = "disclosure_financial_whether_rd" ;mapFi.put(key, objBaseFi.getString(key));
                key = "disclosure_financial_rd_cost" ;mapFi.put(key, objBaseFi.getString(key));

                key = "disclosure_financial_balance_sheet_name" ;mapFi.put(key, objFiStat.getString(key));
                key = "disclosure_financial_balance_sheet" ;mapFi.put(key, objFiStat.getString(key));
                key = "disclosure_financial_balance_sheet_description" ;mapFi.put(key, objFiStat.getString(key));
                key = "disclosure_financial_cashflow_statement_name" ;mapFi.put(key, objFiStat.getString(key));
                key = "disclosure_financial_cashflow_statement" ;mapFi.put(key, objFiStat.getString(key));
                key = "disclosure_financial_cashflow_statement_description" ;mapFi.put(key, objFiStat.getString(key));
                key = "disclosure_financial_income_statement_name" ;mapFi.put(key, objFiStat.getString(key));
                key = "disclosure_financial_income_statement" ;mapFi.put(key, objFiStat.getString(key));
                key = "disclosure_financial_income_statement_description";mapFi.put(key, objFiStat.getString(key));

                ArrayList listFI = new ArrayList();
                listFI.add(mapFi);

                key = "financial_information";getSubjectInfo.put(key, listFI);

                break;
            case "9":
                //拼组企业经营信息9
                key = "business_information";

                JSONObject objBusBase = JSONObject.fromObject(objInfo.getJSONArray(key).get(0).toString()).getJSONObject("business_basic_information");
                JSONObject objInvest = JSONObject.fromObject(objInfo.getJSONArray(key).get(0).toString()).getJSONObject("Investment_and_financing");

                Map mapBI = new HashMap();

                key = "disclosure_business_overview" ;mapBI.put(key, objBusBase.getString(key));
                key = "disclosure_main_business_analysis" ;mapBI.put(key, objBusBase.getString(key));
                key = "disclosure_main_business_analysis_non" ;mapBI.put(key, objBusBase.getString(key));
                key = "disclosure_assets_and_liabilities_analysis" ;mapBI.put(key, objBusBase.getString(key));
                key = "disclosure_major_shareholders_analysis" ;mapBI.put(key, objBusBase.getString(key));
                key = "disclosure_major_events" ;mapBI.put(key, objBusBase.getString(key));
                key = "disclosure_business_report_name" ;mapBI.put(key, objBusBase.getString(key));
                key = "disclosure_business_report_doc" ;mapBI.put(key, objBusBase.getString(key));
                key = "disclosure_business_report_description" ;mapBI.put(key, objBusBase.getString(key));
                key = "disclosure_business_report_date" ;mapBI.put(key, objBusBase.getString(key));

                key = "disclosure_investment_analysis" ;mapBI.put(key, objInvest.getString(key));
                key = "disclosure_fund_raising_analysis" ;mapBI.put(key, objInvest.getString(key));
                key = "disclosure_sell_situation_analysis" ;mapBI.put(key, objInvest.getString(key));

                ArrayList listBI = new ArrayList();
                listBI.add(mapBI);
                key = "business_information"; getSubjectInfo.put(key, listBI);
                break;
            case "10":
                //拼组第三方拓展信息10
                key = "expand_information";
                getSubjectInfo.put(key, objInfo.getString(key));
        }

        //填充header content字段
//        key = "content";getSubjectInfo.put(key,jobj2.getJSONObject("header").getString(key));
        addContent(getSubjectInfo,jobj2);

        return getSubjectInfo;
    }

    public String getSubObjId(com.alibaba.fastjson.JSONObject objTemp){

        return objTemp.getJSONObject("header").getJSONObject("content").getString("object_id");
    }
    public static String replaceCertain(String src){

       return src.replaceAll("(\")?( )?","").replaceAll(":","=");
    }

    public Boolean checkJGDataHeader(com.alibaba.fastjson.JSONObject objTemp,String... checkList){

        Boolean bResult = true;
        assertEquals(gdJGModelProtocol,objTemp.getJSONObject("header").getJSONObject("model").getString("protocol"));
        assertEquals(gdJGModelVersion,objTemp.getJSONObject("header").getJSONObject("model").getString("protocol"));
        //一般检查content中的五个字段 0-4 ：type object_id version operation timestamp
        log.info("check parameter no: " + checkList.length);
        String[] checkKey = new String[]{"type","object_id","version","operation","timestamp"};
        com.alibaba.fastjson.JSONObject headerContent = objTemp.getJSONObject("header").getJSONObject("content");
        for(int i =0;i<checkList.length;i++){
            if(!checkList[i].equals(headerContent.getString(checkKey[i]))){
                log.info("检查字段 "+ checkKey[i] + " 不匹配 " + headerContent.getString(checkKey[i]));
                bResult = false;
            }

        }

        return  bResult;

    }

    public Map constructContentMap(String...valueList){
        Map tempMap = new HashMap();
        String[] checkKey = new String[]{"type","object_id","version","operation","timestamp"};

        for(int i =0;i<valueList.length;i++){
            String temp = valueList[i];
//            log.info("temp **** " + temp);
            if(checkKey[i].equals( "version")) {
                tempMap.put(checkKey[i],Integer.valueOf(temp));
            }else if(checkKey[i].equals( "timestamp")) {
                tempMap.put(checkKey[i], Long.valueOf(temp));
            }else {
                tempMap.put(checkKey[i], temp);
            }
        }
        return tempMap;
    }

    public TreeMap constructContentTreeMap(String...valueList){
        TreeMap tempMap = new TreeMap();
        String[] checkKey = new String[]{"type","object_id","version","operation","timestamp"};

//        log.info("operation " + valueList[3] + " version " + valueList[2]);
        if(valueList[3] == "create") {
//            log.info("create ----------------------------------");
            assertEquals("0",valueList[2]);}
        if(valueList[3] == "update") {
//            log.info("update ----------------------------------");
            assertNotEquals("0",valueList[2]);
        }
        if(valueList[3] == "delete") assertNotEquals("0",valueList[2]);
        for(int i =0;i<valueList.length;i++){
            String temp = valueList[i];
//            log.info("temp **** " + temp);
            if(checkKey[i].equals( "version")) {
                tempMap.put(checkKey[i],Integer.valueOf(temp));
            }else if(checkKey[i].equals( "timestamp")) {
                tempMap.put(checkKey[i], Long.valueOf(temp));
            }else {
                tempMap.put(checkKey[i], temp);
            }
        }


        return tempMap;
    }

    public String getTimeStampFromMap(Map objMap,String timeKeyWord)throws Exception{
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        long dt = sdf.parse(objMap.get(timeKeyWord).toString()).getTime()/1000;
        return String.valueOf(dt);

    }

    public String matchRefMapCertVer(Map dataInfo,String type,String...ver){
        String tempStr = dataInfo.toString();
        String key = "";
        switch (type){
            case "subject":
                key = "=" + subject_investor_qualification_certifier_ref;
                tempStr = tempStr.replaceAll(key,key + ver[0]);
                break;
            case "account":
                key = "=" + account_subject_ref; tempStr = tempStr.replaceAll(key,key + ver[0]);
                key = account_depository_ref; tempStr = tempStr.replaceAll(key,key + ver[1]);
                key = "=" + account_associated_account_ref; tempStr = tempStr.replaceAll(key,key + ver[2]);
                break;
//            case "accountAcc":
//                key = account_subject_ref; tempStr = tempStr.replaceAll(key,key + ver[0]);
//                key = account_depository_ref; tempStr = tempStr.replaceAll(key,key + ver[1]);
//                key = "account_associated_account_ref=" + account_associated_account_ref ; tempStr = tempStr.replaceAll(key,key + ver[2]);
//                break;
            case "product":
                key = product_market_subject_ref; tempStr = tempStr.replaceAll(key,key + ver[0]);
                key = product_issuer_subject_ref; tempStr = tempStr.replaceAll(key,key + ver[1]);
                key = service_provider_subject_ref; tempStr = tempStr.replaceAll(key,key + ver[2]);
                break;
            case "transactionreport" : break;
            case "registration" : break;
            case "settlement" : break;
            case "infodisclosure" : break;
        }
        return tempStr;
    }

    public String matchRefMapCertVer2(Map dataInfo,String type){
        String tempStr = dataInfo.toString();
        String certainVer = "";
        String key = "";
        switch (type){
            case "subject":
                key = "=" + subject_investor_qualification_certifier_ref;
                certainVer = getObjectLatestVer(subject_investor_qualification_certifier_ref);
                tempStr = tempStr.replaceAll(key,key + "/" + certainVer);
                break;
            case "account":
                key = "=" + account_subject_ref;   certainVer = getObjectLatestVer(account_subject_ref);
                tempStr = tempStr.replaceAll(key,key + "/" + certainVer);
                key = account_depository_ref;           certainVer = getObjectLatestVer(key);
                tempStr = tempStr.replaceAll(key,key + "/" + certainVer);
                key = "account_associated_account_ref=" + account_associated_account_ref;   certainVer = getObjectLatestVer(account_associated_account_ref);
                tempStr = tempStr.replaceAll(key,key + "/" + certainVer);

                break;
            case "product":
                key = product_market_subject_ref;       certainVer = getObjectLatestVer(key);
                tempStr = tempStr.replaceAll(key,key + "/" + certainVer);
                key = product_issuer_subject_ref;       certainVer = getObjectLatestVer(key);
                tempStr = tempStr.replaceAll(key,key + "/" + certainVer);
                key = service_provider_subject_ref;     certainVer = getObjectLatestVer(key);
                tempStr = tempStr.replaceAll(key,key + "/" + certainVer);
                break;
            case "transactionreport" :
                key =  "transaction_custody_product_ref=" + transaction_custody_product_ref;
                    certainVer = getObjectLatestVer(transaction_custody_product_ref);
                tempStr = tempStr.replaceAll(key,key + "/" + certainVer);
                key =  transaction_product_issuer_ref;       certainVer = getObjectLatestVer(key);
                tempStr = tempStr.replaceAll(key,key + "/" + certainVer);
                key =  transaction_issuer_ref;       certainVer = getObjectLatestVer(key);
                tempStr = tempStr.replaceAll(key,key + "/" + certainVer);
                key =  transaction_investor_ref;       certainVer = getObjectLatestVer(key);
                tempStr = tempStr.replaceAll(key,key + "/" + certainVer);
                key =  transaction_investor_original_ref;       certainVer = getObjectLatestVer(key);
                tempStr = tempStr.replaceAll(key,key + "/" + certainVer);
                key =  transaction_investor_counterparty_ref;       certainVer = getObjectLatestVer(key);
                tempStr = tempStr.replaceAll(key,key + "/" + certainVer);
                key =  transaction_intermediary_subject_ref;       certainVer = getObjectLatestVer(key);
                tempStr = tempStr.replaceAll(key,key + "/" + certainVer);

                break;
            case "registration" :
                key =  "=" + register_subject_ref;       certainVer = getObjectLatestVer(register_subject_ref);
                tempStr = tempStr.replaceAll(key,key + "/" + certainVer);

                key = "register_product_ref=" + register_product_ref;       certainVer = getObjectLatestVer(register_product_ref);
                tempStr = tempStr.replaceAll(key,key + "/" + certainVer);

                if(regObjType == 1) {
                    key = "register_subject_account_ref=" + register_subject_account_ref;
                    certainVer = getObjectLatestVer(register_subject_account_ref);
                    log.info("++++++++++++++++++++++" + key + certainVer);
                    tempStr = tempStr.replaceAll(key, key + "/" + certainVer);


                    key = "register_transaction_ref=" + register_transaction_ref;
                    certainVer = getObjectLatestVer(register_transaction_ref);
                    tempStr = tempStr.replaceAll(key, key + "/" + certainVer);

                    key = "=" + register_right_recognition_subject_ref;
                    certainVer = getObjectLatestVer(register_right_recognition_subject_ref);
                    tempStr = tempStr.replaceAll(key, key + "/" + certainVer);

                    key = register_right_recognition_agent_subject_ref;
                    certainVer = getObjectLatestVer(key);
                    tempStr = tempStr.replaceAll(key, key + "/" + certainVer);


                }else if(regObjType == 2){
                    key = "=" + register_equity_subject_ref;       certainVer = getObjectLatestVer(register_equity_subject_ref);
                    tempStr = tempStr.replaceAll(key,key + "/" + certainVer);

                    key =  "=" + register_debt_holder_ref;       certainVer = getObjectLatestVer(register_debt_holder_ref);
                    tempStr = tempStr.replaceAll(key,key + "/" + certainVer);

                    key =  "=" + register_investor_subject_ref;       certainVer = getObjectLatestVer(register_investor_subject_ref);
                    tempStr = tempStr.replaceAll(key,key + "/" + certainVer);
                }

//                key =  "=" + roll_register_subject_ref;       certainVer = getObjectLatestVer(roll_register_subject_ref);
//                tempStr = tempStr.replaceAll(key,key + "/" + certainVer);

//                key = "roll_register_product_ref=" + roll_register_product_ref;       certainVer = getObjectLatestVer(roll_register_product_ref);
//                tempStr = tempStr.replaceAll(key,key + "/" + certainVer);


                break;
            case "settlement" :
                key = settlement_subject_ref;       certainVer = getObjectLatestVer(key);
                tempStr = tempStr.replaceAll(key,key + "/" + certainVer);
                key = "settlement_product_ref=" + settlement_product_ref;       certainVer = getObjectLatestVer(settlement_product_ref);
                tempStr = tempStr.replaceAll(key,key + "/" + certainVer);
                key = settlement_transaction_ref;       certainVer = getObjectLatestVer(key);
                tempStr = tempStr.replaceAll(key,key + "/" + certainVer);
                key = settlement_out_account_object_ref;       certainVer = getObjectLatestVer(key);
                tempStr = tempStr.replaceAll(key,key + "/" + certainVer);
                key = settlement_in_account_object_ref;       certainVer = getObjectLatestVer(key);
                tempStr = tempStr.replaceAll(key,key + "/" + certainVer);

//                replaceObjIdKey(tempStr,"settlement_subject_ref");
//                replaceObjIdKey(tempStr,"settlement_product_ref");
//                replaceObjIdKey(tempStr,"settlement_transaction_ref");
//                replaceObjIdKey(tempStr,"settlement_out_account_object_ref");
//                replaceObjIdKey(tempStr,"settlement_in_account_object_ref");
                break;
            case "infodisclosure" :
                key = disclosure_subject_ref;       certainVer = getObjectLatestVer(key);
                tempStr = tempStr.replaceAll(key,key + "/" + certainVer);

                key = disclosure_referer_subject_ref;       certainVer = getObjectLatestVer(key);
                tempStr = tempStr.replaceAll(key,key + "/" + certainVer);
                key = disclosure_display_platform_ref;       certainVer = getObjectLatestVer(key);
                tempStr = tempStr.replaceAll(key,key + "/" + certainVer);
                key = disclosure_identifier_ref;       certainVer = getObjectLatestVer(key);
                tempStr = tempStr.replaceAll(key,key + "/" + certainVer);
                key = disclosure_auditor_ref;       certainVer = getObjectLatestVer(key);
                tempStr = tempStr.replaceAll(key,key + "/" + certainVer);
//                replaceObjIdKey(tempStr,"disclosure_subject_ref");
//                replaceObjIdKey(tempStr,"disclosure_referer_subject_ref");
//                replaceObjIdKey(tempStr,"disclosure_display_platform_ref");
//                replaceObjIdKey(tempStr,"disclosure_identifier_ref");
//                replaceObjIdKey(tempStr,"disclosure_auditor_ref");
                break;
        }
        return tempStr;
    }

    public String replaceObjIdKey2(String srcStr,String objectName) {
        String certainVer = "";
        String tempStr = srcStr;
        log.info("/*/*/////////////////////////////////////////////");
        log.info(objectName);
        log.info(refInfo.get(objectName));
        key = objectName + "=" + refInfo.get(objectName);
        log.info(key);
        certainVer = getObjectLatestVer(refInfo.get(objectName));
        tempStr = tempStr.replaceAll(key, key + "/" + certainVer);
        log.info(tempStr);

        return tempStr;
    }




    public String getObjectLatestVer(String objectId){
        String response = gd.GDObjectQueryByVer(objectId,-1);
        if(response.contains("\"state\":400")) return "-1";
        else
            return JSONObject.fromObject(response).getJSONObject("data").getJSONObject("header"
                                                ).getJSONObject("content").getString("version");
    }

    public Boolean chkSensitiveWord(String chkStr,String type){
        Boolean noSensWord = true;

        String[] subSensWords = new String[]{"subject_invoice_account_number","subject_key_personnel_name",
                "subject_key_personnel_id","subject_key_personnel_contact",
                "subject_investor_name","subject_id_number",
                "subject_contact_number","subject_cellphone_number",
                "subject_personal_fax"};

        String[] accSensWords = new String[]{"account_opening_agent_name" ,"account_opening_agent_contact_number",
                "account_closing_agent_name","account_closing_agent_contact_number"};

        String[] prodSensWords = new String[]{"product_issuer_contact_person" ,"product_issuer_contact_info",
                "product_fund_manager_name","product_fund_manager_certificate_number"};

        String[] txRpSensWords = new String[]{"transaction_product_issuer_name","transaction_issuer_name",
                "transaction_investor_name","transaction_investor_original_name",
                "transaction_investor_counterparty_name"};

        String[] regSensWords = new String[]{"register_right_recognition_subject_name","register_right_recognition_agent_subject_name",
                "register_debt_holder_contact_number","register_investor_name"};

        String[] settleSensWords = new String[]{"settlement_out_bank_account","settlement_out_account_name",
                "settlement_in_bank_account","settlement_in_account_name"};
        String[] disclosureSensWords = new String[]{"disclosure_identifier_name","disclosure_auditor_name"};

        switch (type) {
            case "subject":
                for (String sensKey : subSensWords) {
                    if (chkStr.contains(sensKey)) noSensWord = false;
                }
                break;
            case "accout":
                for (String sensKey : accSensWords) {
                    if (chkStr.contains(sensKey)) noSensWord = false;
                }
                break;
            case "product":
                for (String sensKey : prodSensWords) {
                    if (chkStr.contains(sensKey)) noSensWord = false;
                }
                break;
            case "transactionreport":
                for (String sensKey : txRpSensWords) {
                    if (chkStr.contains(sensKey)) noSensWord = false;
                }
                break;
            case "registration":
                for (String sensKey : regSensWords) {
                    if (chkStr.contains(sensKey)) noSensWord = false;
                }
                break;
            case "settlement":
                for (String sensKey : settleSensWords) {
                    if (chkStr.contains(sensKey)) noSensWord = false;
                }
                break;
            case "infodisclosure":
                for (String sensKey : disclosureSensWords) {
                    if (chkStr.contains(sensKey)) noSensWord = false;
                }
                break;
            default:
                log.info("错误的类型");
        }

        return noSensWord;
    }

    public Boolean bContainJGFlag(String jsonStr){
        //确认meta信息包含监管标识 且每笔都包含meta data_type supervision
        if(!jsonStr.contains("[")) return jsonStr.contains("\"meta\":{\"data_type\":\"supervision\"}");
        else return (StringUtils.countOccurrencesOf(jsonStr,"\"meta\":{\"data_type\":\"supervision\"}") ==
                JSONArray.fromObject(jsonStr).size());
    }

    public Boolean bCheckJGParams(Map mapKeyWod)throws Exception{
        GDBeforeCondition gdBF = new GDBeforeCondition();
        CommonFunc commonFunc = new CommonFunc();

        Boolean bResult = true;
//        非权利登记移除 代码最小改动
//        if(regObjType != 1){
//            mapKeyWod.remove("register_subject_account_ref");
//        }

        String tempAddr = mapKeyWod.get("address").toString();
        String tempTxId = mapKeyWod.get("txId").toString();
        String tempObjId = mapKeyWod.get("objectid").toString();
        String tempObjVer = mapKeyWod.get("version").toString();
//        String keyWordGetUriStore = mapKeyWod.get("hashKeyWord").toString();//固定使用监管提供的supervision字符串
        String operationType = mapKeyWod.get("operationType").toString();
        String contentType = mapKeyWod.get("contentType").toString();
        String subProdSubType = mapKeyWod.get("subProdSubType").toString();

        Map update = null;
        //如果存在key 或者
        if(mapKeyWod.containsKey("updateMap") && (!mapKeyWod.get("updateMap").equals(null)))
        {
            update = com.alibaba.fastjson.JSONObject.parseObject(mapKeyWod.get("updateMap").toString(), Map.class);
            if(regObjType != 1){
                update.remove("register_subject_account_ref");
                assertEquals(false,update.containsKey("register_subject_account_ref"));//确认已移除
            }
        }

        //检查各个交易详情信息中不包含敏感词
//        assertEquals("不包含敏感词",true,chkSensitiveWord(store.GetTxDetail(tempTxId),contentType));
        //获取链上的uri存证信息所在交易hash 不是从链上取版本
        Map uriInfo = new HashMap();
        if(bUriMapExist){
            uriInfo = mapURI;
        }
        else {
            uriInfo = getJGURIStoreHash(tempTxId,conJGFileName(tempObjId,tempObjVer),1);
            assertEquals(true,bContainJGFlag(uriInfo.get("storeData").toString()));//确认meta信息包含监管关键字
            mapURI = uriInfo;
        }
//        log.info(uriInfo.toString());
        String storeData = uriInfo.get("storeData").toString();
        //获取链上mini url的存证信息 并检查是否包含uri信息 每个登记都是新的 则都是0
        String storeFileName = conJGFileName(tempObjId,tempObjVer);
        String chkStoreURI = storeFileName;
//        log.info("uri StoreData" + storeData + "\n检查存证信息是否包含" + chkStoreURI);
        log.info("检查存证信息是否包含" + chkStoreURI);
        assertEquals(true,uriInfo.get("storeData").toString().contains(chkStoreURI));
        //直接从minio上获取报送数据文件信息
        Map getRegInfo = constructJGDataFromStr(storeFileName,contentType,subProdSubType);
        //重组的入参信息 即对比信息
        Map mapParam = null;

        //时间戳
        long tempTS = ts1;

        switch (contentType) {
            case "subject":
                if(subProdSubType.equals("1"))           mapParam = gdBF.init01EnterpriseSubjectInfo();
                else if(subProdSubType.equals("2"))      mapParam = gdBF.init01PersonalSubjectInfo();
                tempTS = ts1;
                //这边固定主体的更新时间都是ts1 方便测试检查
                mapParam.put("subject_object_id",tempObjId);
                break;

            case "account":
                if(subProdSubType.equals("1"))          mapParam = gdBF.init02ShareholderAccountInfo();
                else if(subProdSubType.equals("2"))     mapParam = gdBF.init02FundAccountInfo();
                tempTS = ts2;
                mapParam.put("account_object_id",tempObjId);
                break;

            case "product":
                if(subProdSubType.equals("1"))          mapParam = gdBF.init03EquityProductInfo();
                else if(subProdSubType.equals("3"))     mapParam = gdBF.init03BondProductInfo();
                else if(subProdSubType.equals("5"))     mapParam = gdBF.init03FundProductInfo();
                tempTS = ts3;
                mapParam.put("product_object_id",tempObjId);
                break;

            case "transactionreport":
                mapParam = gdBF.init04TxInfo();
                tempTS = ts4;
                mapParam.put("transaction_object_id",tempObjId);
                break;

            case "registration":
                mapParam = gdBF.init05RegInfo();
                tempTS = ts5;
                mapParam.put("register_registration_object_id",tempObjId);
                break;

            case "settlement":
                mapParam = gdBF.init06SettleInfo();
                tempTS = ts6;
                break;

            case "infodisclosure":
                mapParam = gdBF.init07PublishInfo();
                tempTS = ts7;
                break;

            default:
                log.info("错误的类型");

        }
        assertEquals("检查参数map不为空",false,mapParam.equals(null));
        if(!(update == null)) {
            //更新其他字段信息
            Iterator iter = update.keySet().iterator();
            while (iter.hasNext()) {
                Object key = iter.next();
                String value = update.get(key).toString();
                mapParam.put(key, value);
            }
        }

        //采用有序TreeMap存 header content
        mapParam.put("content",constructContentTreeMap(contentType,tempObjId,tempObjVer,operationType,String.valueOf(tempTS)));

        log.info("检查" + contentType + "存证信息内容与传入一致\n" + mapParam.toString() + "\n" + getRegInfo.toString());
        bResult = commonFunc.compareTwoStr(replaceCertain(matchRefMapCertVer2(mapParam,contentType)),
                            replaceCertain(getRegInfo.toString()));
        assertEquals("交易hash：" + tempTxId + "\n" + tempObjId + "检查" + contentType + "数据是否一致" ,
                true,bResult);

        return bResult;
    }

    public void checkHeaderContentInfo(String objectID,int version,String objType,String operationType){
        String queryInfo = gd.GDObjectQueryByVer(objectID,version);
        assertEquals("{}",JSONObject.fromObject(queryInfo).getJSONObject("data").getString("body"));
        JSONObject objHeaderContent = JSONObject.fromObject(queryInfo).getJSONObject("data").getJSONObject("header").getJSONObject("content");
        assertEquals(objectID,objHeaderContent.getString("object_id"));
        assertEquals(operationType,objHeaderContent.getString("operation"));
        assertEquals(version,objHeaderContent.getInt("version"));
        assertEquals(objType,objHeaderContent.getString("type"));
    }
}