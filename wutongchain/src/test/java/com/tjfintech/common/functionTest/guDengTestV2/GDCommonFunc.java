package com.tjfintech.common.functionTest.guDengTestV2;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.*;

import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassGD.*;
import static org.junit.Assert.assertEquals;


@Slf4j
public class GDCommonFunc {
    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store = testBuilder.getStore();
    CommonFunc cf = new CommonFunc();
    UtilsClass utilsClass=new UtilsClass();
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
            log.info("区块交易数 " + txArr.size());
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

    public String getJGStoreHash(String txId,int offset) throws Exception{
        //获取交易所在区块
        String height = JSONObject.fromObject(store.GetTransactionBlock(txId)).getString("data");
        //获取区块交易列表
        JSONArray txArr = JSONObject.fromObject(store.GetBlockByHeight(Integer.parseInt(height))).getJSONObject("data").getJSONArray("txs");
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

    public static List<Map> gdConstructShareList(String address, double amount, int shareProperty){
        GDBeforeCondition gdbf = new GDBeforeCondition();
        Map tempReg = gdbf.init05RegInfo();
        tempReg.put("register_account_obj_id",mapAccAddr.get(address));
        tempReg.put("register_nature_of_shares",shareProperty);

//        Map tempTxInfo = gdbf.init04TxInfo();
//        tempTxInfo.put("transaction_original_owner_subject_ref",mapAccAddr.get(address));

        Map<String,Object> shares = new HashMap<>();
        shares.put("address",address);
        shares.put("amount",amount);
        shares.put("shareProperty",shareProperty);
        shares.put("registerInformation",tempReg);
//        shares.put("transactionReport",tempTxInfo);

        List<Map> shareList = new ArrayList<>();
        shareList.add(shares);
        return shareList;
    }

    public static List<Map> gdConstructShareList(String address, double amount, int shareProperty,List<Map> list){
        GDBeforeCondition gdbf = new GDBeforeCondition();
        Map tempReg = gdbf.init05RegInfo();
        tempReg.put("register_account_obj_id",mapAccAddr.get(address));
        tempReg.put("register_nature_of_shares",shareProperty);

//        Map tempTxInfo = gdbf.init04TxInfo();
//        tempTxInfo.put("transaction_original_owner_subject_ref",mapAccAddr.get(address));

        List<Map> shareList = new ArrayList<>();
        for(int i = 0 ; i < list.size() ; i++) {
            shareList.add(list.get(i));
        }
        Map<String,Object> shares = new HashMap<>();
        shares.put("address",address);
        shares.put("amount",amount);
        shares.put("shareProperty",shareProperty);
        shares.put("registerInformation",tempReg);
//        shares.put("transactionReport",tempTxInfo);

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

    public static List<Map> gdConstructShareList2(String address, double amount, int shareProperty){
        GDBeforeCondition gdbf = new GDBeforeCondition();
        Map tempReg = gdbf.init05RegInfo();
        tempReg.put("register_account_obj_id",mapAccAddr.get(address));
        tempReg.put("register_nature_of_shares",shareProperty);

        //不填写如下字段
        tempReg.remove("register_rights_change_amount");
        tempReg.remove("register_rights_frozen_balance");
        tempReg.remove("register_available_balance");
        tempReg.remove("register_creditor_subscription_count");
        tempReg.remove("register_rights_frozen_change_amount");


        Map<String,Object> shares = new HashMap<>();
        shares.put("address",address);
        shares.put("amount",amount);
        shares.put("shareProperty",shareProperty);
        shares.put("registerInformation",tempReg);
//        shares.put("transactionReport",tempTxInfo);

        List<Map> shareList = new ArrayList<>();
        shareList.add(shares);
        return shareList;
    }

    public static List<Map> gdConstructShareList2(String address, double amount, int shareProperty,List<Map> list){
        GDBeforeCondition gdbf = new GDBeforeCondition();
        Map tempReg = gdbf.init05RegInfo();
        tempReg.put("register_account_obj_id",mapAccAddr.get(address));
        tempReg.put("register_nature_of_shares",shareProperty);

        //不填写如下字段
        tempReg.remove("register_rights_change_amount");
        tempReg.remove("register_rights_frozen_balance");
        tempReg.remove("register_available_balance");
        tempReg.remove("register_creditor_subscription_count");
        tempReg.remove("register_rights_frozen_change_amount");


        List<Map> shareList = new ArrayList<>();
        for(int i = 0 ; i < list.size() ; i++) {
            shareList.add(list.get(i));
        }
        Map<String,Object> shares = new HashMap<>();
        shares.put("address",address);
        shares.put("amount",amount);
        shares.put("shareProperty",shareProperty);
        shares.put("registerInformation",tempReg);
//        shares.put("transactionReport",tempTxInfo);

        shareList.add(shares);
        return shareList;
    }



    public static List<Map> getShareListFromQueryNoZeroAcc(JSONArray dataShareList)throws Exception {
        List<Map> getShareList = new ArrayList<>();
        for (int i = 0; i < dataShareList.size(); i++) {

            if (dataShareList.get(i).toString().contains(zeroAccount))
                continue;
            else {
                double amount = JSONObject.fromObject(dataShareList.get(i)).getDouble("amount");
                double lockAmount = JSONObject.fromObject(dataShareList.get(i)).getDouble("lockAmount");
                String address = JSONObject.fromObject(dataShareList.get(i)).getString("address");
                int shareProperty = JSONObject.fromObject(dataShareList.get(i)).getInt("shareProperty");
                String sharePropertyCN = JSONObject.fromObject(dataShareList.get(i)).getString("sharePropertyCN");
                getShareList = gdConstructQueryShareList(address, amount, shareProperty, lockAmount,sharePropertyCN,getShareList);
            }
        }
        return getShareList;
    }

    public static List<Map> gdConstructQueryShareList(String address, double amount, int shareProperty,double lockAmount,String sharePropertyCN, List<Map> list){
        //处理登记
        GDBeforeCondition gdbf = new GDBeforeCondition();
        Map tempReg = gdbf.init05RegInfo();
        tempReg.put("register_account_obj_id",mapAccAddr.get(address));
        tempReg.put("register_nature_of_shares",shareProperty);

        //处理交易
        Map tempTxInfo = gdbf.init04TxInfo();
        tempTxInfo.put("transaction_original_owner_subject_ref",mapAccAddr.get(address));

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
        shares.put("transactionReport",tempTxInfo);

        shareList.add(shares);
        return shareList;
    }

    public static List<Map> gdConstructQueryShareListNoTxReport(String address, double amount, int shareProperty,
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
                    (objTemp.getJSONObject("body").getJSONObject("account_information").getJSONObject("basic_account_information").getIntValue("account_type") == 1) &&
                    objTemp.getJSONObject("body").getJSONObject("account_object_information").getString("account_object_id").equals(objId)) {
                jobjOK = objTemp;
                break;
            }
        }
        log.info("***********" + jobjOK.toString());

        //schema校验数据格式
        assertEquals("schema校验数据是否匹配",true,
                cf.schemaCheckData(dirSchemaData,chkSchemaToolName,gdSchema,jobjOK.toString(),"2"));
        return accountInfo(jobjOK);
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
                    (objTemp.getJSONObject("body").getJSONObject("account_information").getJSONObject("basic_account_information").getIntValue("account_type") == 0) &&
                    objTemp.getJSONObject("body").getJSONObject("account_object_information").getString("account_object_id").equals(objId)){
                jobjOK = objTemp;
                break;
            }
        }

        //schema校验数据格式
        assertEquals("schema校验数据是否匹配",true,
                cf.schemaCheckData(dirSchemaData,chkSchemaToolName,gdSchema,jobjOK.toString(),"2"));

        return accountInfo(jobjOK);
    }


    public Map contructEquityProdInfo(String prodTxId){
        log.info("检查的交易id " + prodTxId);
        com.alibaba.fastjson.JSONObject object2 = com.alibaba.fastjson.JSONObject.parseObject(store.GetTxDetail(prodTxId));
        String storeData2 = object2.getJSONObject("data").getJSONObject("store").getString("storeData");

        com.alibaba.fastjson.JSONObject jobjOK = parseJSONBaseOnJSONStr(storeData2,prodType);

        //schema校验数据格式
//        assertEquals("schema校验数据是否匹配",true,
//                cf.schemaCheckData(dirSchemaData,chkSchemaToolName,gdSchema,jobjOK.toString(),"2"));
        return productInfo(jobjOK,"1");
    }

    public Map contructBondProdInfo(String prodTxId){
        log.info("检查的交易id " + prodTxId);
        com.alibaba.fastjson.JSONObject object2 = com.alibaba.fastjson.JSONObject.parseObject(store.GetTxDetail(prodTxId));
        String storeData2 = object2.getJSONObject("data").getJSONObject("store").getString("storeData");

        com.alibaba.fastjson.JSONObject jobjOK = parseJSONBaseOnJSONStr(storeData2,prodType);

//        //schema校验数据格式
//        assertEquals("schema校验数据是否匹配",true,
//                cf.schemaCheckData(dirSchemaData,chkSchemaToolName,gdSchema,jobjOK.toString(),"2"));
        return productInfo(jobjOK,"2");
    }

    public Map contructFundProdInfo(String prodTxId){
        log.info("检查的交易id " + prodTxId);
        com.alibaba.fastjson.JSONObject object2 = com.alibaba.fastjson.JSONObject.parseObject(store.GetTxDetail(prodTxId));
        String storeData2 = object2.getJSONObject("data").getJSONObject("store").getString("storeData");

        com.alibaba.fastjson.JSONObject jobjOK = parseJSONBaseOnJSONStr(storeData2,prodType);

//        //schema校验数据格式
//        assertEquals("schema校验数据是否匹配",true,
//                cf.schemaCheckData(dirSchemaData,chkSchemaToolName,gdSchema,jobjOK.toString(),"2"));

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
//        com.alibaba.fastjson.JSONObject objInfo = jobj2.getJSONObject("body").getJSONObject("subject_information");
        com.alibaba.fastjson.JSONObject objInfo = jobj2.getJSONObject("body").getJSONObject("subject");
        com.alibaba.fastjson.JSONObject objBaseGIS = objInfo.getJSONObject("basic_information_subject").getJSONObject("general_information_subject");
        key = "subject_id";                          getSubjectInfo.put(key,objBaseGIS.getString(key));
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
        key = "subject_high_technology_enterpris";getSubjectInfo.put(key,objBID.getString(key));
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
        key = "subject_enterprise_fax";getSubjectInfo.put(key,objBID.getString(key));
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

        return getSubjectInfo;
    }

//    public Map subjectInfoPerson(com.alibaba.fastjson.JSONObject jobj2){
//
//        Map getSubjectInfo = new HashMap();
//
//        //获取对象标识信息
//        key = "subject_object_id";                  getSubjectInfo.put(key,getSubObjId(jobj2));
//
//        com.alibaba.fastjson.JSONObject objInfo = jobj2.getJSONObject("body").getJSONObject("subject_object_information");
////        key = "subject_object_information_type";    getSubjectInfo.put(key,objInfo.getString(key));
//        key = "subject_type";                         getSubjectInfo.put(key,objInfo.getString(key));
//
//        //获取主体信息 主体基本信息
//        com.alibaba.fastjson.JSONObject objSubI = jobj2.getJSONObject("body").getJSONObject("subject_information");
//        com.alibaba.fastjson.JSONObject objSubBase = objSubI.getJSONObject("basic_information_subject");
//        com.alibaba.fastjson.JSONObject objSubBaseCommon = objSubBase.getJSONObject("general_information_subject");
////        com.alibaba.fastjson.JSONObject objSubBaseQual = objSubBase.getJSONObject("subject_qualification_information");
////        com.alibaba.fastjson.JSONObject objSubBaseQQ = objSubBaseQual.getJSONObject("subject_qualification_information");
////        com.alibaba.fastjson.JSONObject objSubBaseQQ1 = objSubBaseQual.getJSONObject("subject_qualification_information1");
//
//        //获取 主体信息 主体基本信息 主体通用信息
//        key = "subject_main_administrative_region";                      getSubjectInfo.put(key,objSubBaseCommon.getString(key));
//        key = "subject_id";                               getSubjectInfo.put(key,objSubBaseCommon.getString(key));
//        key = "subject_create_time";                        getSubjectInfo.put(key,objSubBaseCommon.getString(key));
//        //获取 主体信息 主体基本信息 主体资质信息
//        key = "subject_qualification_information";      getSubjectInfo.put(key,objSubBase.getString(key));
//
//        //获取 主体信息 个人主体信息 个人主体基本信息
//        com.alibaba.fastjson.JSONObject objPersonSub = objSubI.getJSONObject("personal_subject_information");
//        com.alibaba.fastjson.JSONObject objPersonSubBase = objPersonSub.getJSONObject("personal_subject_basic_information");
//        key = "subject_investor_name";getSubjectInfo.put(key,objPersonSubBase.getString(key));
//        key = "subject_id_type";getSubjectInfo.put(key,objPersonSubBase.getString(key));
//        key = "subject_id_number";getSubjectInfo.put(key,objPersonSubBase.getString(key));
//        key = "subject_id_address";getSubjectInfo.put(key,objPersonSubBase.getString(key));
//        key = "subject_contact_address";getSubjectInfo.put(key,objPersonSubBase.getString(key));
//        key = "subject_contact_number";getSubjectInfo.put(key,objPersonSubBase.getString(key));
//        key = "subject_cellphone_number";getSubjectInfo.put(key,objPersonSubBase.getString(key));
//        key = "subject_personal_fax";getSubjectInfo.put(key,objPersonSubBase.getString(key));
//        key = "subject_postalcode_number";getSubjectInfo.put(key,objPersonSubBase.getString(key));
//        key = "subject_id_doc_mailbox";getSubjectInfo.put(key,objPersonSubBase.getString(key));
//        key = "subject_education";getSubjectInfo.put(key,objPersonSubBase.getString(key));
//        key = "subject_occupation";getSubjectInfo.put(key,objPersonSubBase.getString(key));
//        key = "subject_industry";getSubjectInfo.put(key,objPersonSubBase.getString(key));
//        key = "subject_birthday";getSubjectInfo.put(key,objPersonSubBase.getString(key));
//        key = "subject_gender";getSubjectInfo.put(key,objPersonSubBase.getString(key));
//        key = "subject_work_unit";getSubjectInfo.put(key,objPersonSubBase.getString(key));
//        key = "subject_investment_period";getSubjectInfo.put(key,objPersonSubBase.getString(key));
//        key = "subject_investment_experience";getSubjectInfo.put(key,objPersonSubBase.getString(key));
//        key = "subject_native_place";getSubjectInfo.put(key,objPersonSubBase.getString(key));
//        key = "subject_province";getSubjectInfo.put(key,objPersonSubBase.getString(key));
//        key = "subject_city";getSubjectInfo.put(key,objPersonSubBase.getString(key));
//
//
//        return getSubjectInfo;
//    }

    public Map subjectInfoPerson(com.alibaba.fastjson.JSONObject jobj2){

        Map getSubjectInfo = new HashMap();

        //获取对象标识信息
        key = "subject_object_id";                  getSubjectInfo.put(key,getSubObjId(jobj2));

        com.alibaba.fastjson.JSONObject objInfo = jobj2.getJSONObject("body").getJSONObject("subject");
        key = "subject_type";                         getSubjectInfo.put(key,objInfo.getString(key));

        //获取主体信息 主体基本信息
        com.alibaba.fastjson.JSONObject objSubBase = objInfo.getJSONObject("basic_information_subject");
        com.alibaba.fastjson.JSONObject objSubBaseCommon = objInfo.getJSONObject("general_information_subject");

        //获取 主体信息 主体基本信息 主体通用信息
        key = "subject_main_administrative_region";         getSubjectInfo.put(key,objSubBaseCommon.getString(key));
        key = "subject_id";                                 getSubjectInfo.put(key,objSubBaseCommon.getString(key));
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
        key = "subject_postalcode_number";                  getSubjectInfo.put(key,objPersonSubBase.getString(key));
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

        return getSubjectInfo;
    }

//    public Map accountInfo(com.alibaba.fastjson.JSONObject jobjOK){
//        com.alibaba.fastjson.JSONObject objInfo = jobjOK.getJSONObject("body").getJSONObject("account_object_information");
//
//        com.alibaba.fastjson.JSONObject objAccbase = jobjOK.getJSONObject("body").getJSONObject("account_information").getJSONObject("basic_account_information");
//
//        com.alibaba.fastjson.JSONObject objAccQaul = jobjOK.getJSONObject("body").getJSONObject("account_information").getJSONObject("account_qualificat_information");
//
//        com.alibaba.fastjson.JSONObject objAccLife = jobjOK.getJSONObject("body").getJSONObject("account_information").getJSONObject("account_cycle_information");
//        com.alibaba.fastjson.JSONObject objAccLifeOpen = objAccLife.getJSONObject("account_opening_information");
//        com.alibaba.fastjson.JSONObject objAccLifeCancel = objAccLife.getJSONObject("account_cancellation_information");
//        com.alibaba.fastjson.JSONObject objAccLifeFreeze = objAccLife.getJSONObject("freeze_information");
//        com.alibaba.fastjson.JSONObject objAccLifeUnfreeze = objAccLife.getJSONObject("unfreezing_information");
//
////        com.alibaba.fastjson.JSONObject objAccRela = jobjOK.getJSONObject("body").getJSONObject("account_information").getJSONObject("account_association_info");
//        com.alibaba.fastjson.JSONObject objAccRela = jobjOK.getJSONObject("body").getJSONObject("account_information").getJSONObject("account_related_information");
//        Map getSubjectInfo = new HashMap();
//        key = "account_object_id";                  getSubjectInfo.put(key,getSubObjId(jobjOK));
//        key = "account_object_information_type";    getSubjectInfo.put(key,objInfo.getString(key));
//
//        key = "account_holder_subject_ref";         getSubjectInfo.put(key,objAccbase.getString(key));
//        key = "account_depository_subject_ref";     getSubjectInfo.put(key,objAccbase.getString(key));
//        key = "account_number";                     getSubjectInfo.put(key,objAccbase.getString(key));
//        key = "account_type";                       getSubjectInfo.put(key,objAccbase.getString(key));
//        key = "account_never";                      getSubjectInfo.put(key,objAccbase.getString(key));
//        key = "account_status";                     getSubjectInfo.put(key,objAccbase.getString(key));
//
//        key = "account_qualification_certification_file";   getSubjectInfo.put(key,objAccQaul.getString(key));//可以直接get list string
//        key = "account_certifier";                          getSubjectInfo.put(key,objAccQaul.getString(key));
//        key = "account_auditor";                            getSubjectInfo.put(key,objAccQaul.getString(key));
//        key = "account_certification_time";                 getSubjectInfo.put(key,objAccQaul.getString(key));
//        key = "account_audit_time";                         getSubjectInfo.put(key,objAccQaul.getString(key));
//
//        key = "account_opening_date";               getSubjectInfo.put(key,objAccLifeOpen.getString(key));
//        key = "account_opening_certificate";        getSubjectInfo.put(key,objAccLifeOpen.getString(key));
//        key = "account_closing_date";               getSubjectInfo.put(key,objAccLifeCancel.getString(key));
//        key = "account_closing_certificate";        getSubjectInfo.put(key,com.alibaba.fastjson.JSONObject.parseArray(objAccLifeCancel.getJSONArray(key).toJSONString(), String.class));
//        key = "account_forzen_date";                getSubjectInfo.put(key,objAccLifeFreeze.getString(key));
//        key = "account_forzen_certificate";         getSubjectInfo.put(key,com.alibaba.fastjson.JSONObject.parseArray(objAccLifeFreeze.getJSONArray(key).toJSONString(), String.class));
//        key = "account_thaw_date";                  getSubjectInfo.put(key,objAccLifeUnfreeze.getString(key));
//        key = "account_thaw_certificate";           getSubjectInfo.put(key,com.alibaba.fastjson.JSONObject.parseArray(objAccLifeUnfreeze.getJSONArray(key).toJSONString(), String.class));
//
//        key = "account_association";                    getSubjectInfo.put(key,objAccRela.getString(key));
//        key = "account_associated_account_ref";         getSubjectInfo.put(key,objAccRela.getString(key));
//        key = "account_associated_acct_certificates";   getSubjectInfo.put(key,objAccRela.getString(key));
//
//
//        return getSubjectInfo;
//    }

    public Map accountInfo(com.alibaba.fastjson.JSONObject jobjOK){
        com.alibaba.fastjson.JSONObject objInfo = jobjOK.getJSONObject("body").getJSONObject("account_information");

        com.alibaba.fastjson.JSONObject objAccbase = objInfo.getJSONObject("basic_account_information");

        com.alibaba.fastjson.JSONObject objAccLife = objInfo.getJSONObject("account_cycle_information");
        com.alibaba.fastjson.JSONObject objAccLifeOpen = objAccLife.getJSONObject("account_opening_information");
        com.alibaba.fastjson.JSONObject objAccLifeCancel = objAccLife.getJSONObject("account_cancellation_information");
        com.alibaba.fastjson.JSONObject objAccLifeFreeze = objAccLife.getJSONObject("freeze_information");
        com.alibaba.fastjson.JSONObject objAccLifeUnfreeze = objAccLife.getJSONObject("unfreezing_information");

        com.alibaba.fastjson.JSONObject objAccRela = objInfo.getJSONObject("account_related_information");
        Map getSubjectInfo = new HashMap();
        key = "account_object_id";                    getSubjectInfo.put(key,getSubObjId(jobjOK));
        key = "account_object_information_type";      getSubjectInfo.put(key,objInfo.getString(key));

        //账户信息 账户基本信息
        key = "account_subject_ref";                  getSubjectInfo.put(key,objAccbase.getString(key));
        key = "account_depository_ref";               getSubjectInfo.put(key,objAccbase.getString(key));
        key = "account_number";                       getSubjectInfo.put(key,objAccbase.getString(key));
        key = "account_type";                         getSubjectInfo.put(key,objAccbase.getString(key));
        key = "account_purpose";                      getSubjectInfo.put(key,objAccbase.getString(key));
        key = "account_status";                       getSubjectInfo.put(key,objAccbase.getString(key));
        key = "account_create_time";                  getSubjectInfo.put(key,objAccbase.getString(key));

        //账户信息 账户生命周期信息 开户信息
        key = "account_establish_date";               getSubjectInfo.put(key,objAccLifeOpen.getString(key));
        key = "account_open_date";                    getSubjectInfo.put(key,objAccLifeOpen.getString(key));
        key = "account_open_doc";                     getSubjectInfo.put(key,objAccLifeOpen.getString(key));
        key = "account_open_agency_name";             getSubjectInfo.put(key,objAccLifeOpen.getString(key));
        key = "account_open_agency_phone";            getSubjectInfo.put(key,objAccLifeOpen.getString(key));

        //账户信息 账户生命周期信息 销户信息
        key = "account_close_date";                   getSubjectInfo.put(key,objAccLifeCancel.getString(key));
        key = "account_close_doc";                    getSubjectInfo.put(key,objAccLifeCancel.getString(key));
        key = "account_close_agency_name";            getSubjectInfo.put(key,objAccLifeCancel.getString(key));
        key = "account_close_agency_phone";           getSubjectInfo.put(key,objAccLifeCancel.getString(key));
//        key = "account_thaw_certificate";           getSubjectInfo.put(key,com.alibaba.fastjson.JSONObject.parseArray(objAccLifeUnfreeze.getJSONArray(key).toJSONString(), String.class));

        //账户信息 账户生命周期信息 冻结信息
        key = "account_frozen_date";                  getSubjectInfo.put(key,objAccLifeFreeze.getString(key));
        key = "account_frozen_doc";                   getSubjectInfo.put(key,objAccLifeFreeze.getString(key));
        key = "account_frozen_applicant_name";        getSubjectInfo.put(key,objAccLifeFreeze.getString(key));
        key = "account_frozen_event_description";     getSubjectInfo.put(key,objAccLifeFreeze.getString(key));

        //账户信息 账户生命周期信息 解冻信息
        key = "account_thaw_date";                    getSubjectInfo.put(key,objAccLifeUnfreeze.getString(key));
        key = "account_thaw_doc";                     getSubjectInfo.put(key,objAccLifeUnfreeze.getString(key));
        key = "account_thaw_applicant_name";          getSubjectInfo.put(key,objAccLifeUnfreeze.getString(key));
        key = "account_thaw_event_description";       getSubjectInfo.put(key,objAccLifeUnfreeze.getString(key));

        //账户信息 账户关联信息
        key = "account_association";                  getSubjectInfo.put(key,objAccRela.getString(key));
        key = "account_associated_account_ref";       getSubjectInfo.put(key,objAccRela.getString(key));

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
        key = "product_market_subject";getSubjectInfo.put(key,objProdBase.getString(key));
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
        key = "product_Investment_proportion_range";   getSubjectInfo.put(key,objPortfolio.getString(key));
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
        key = "product_trasfer_validity";getSubjectInfo.put(key,objProdTxList.getString(key));
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
        key = "product_transfer_board_mrket";getSubjectInfo.put(key,objProdTxDelist.getString(key));
        key = "product_acquisition_company_market";getSubjectInfo.put(key,objProdTxDelist.getString(key));
        key = "product_delisting_remarks";getSubjectInfo.put(key,objProdTxDelist.getString(key));

        //产品信息 托管信息
        com.alibaba.fastjson.JSONObject objProdEscrow = objInfo.getJSONObject("escrow_information");//产品信息 托管信息对象
        key = "product_custodian_registration_date";getSubjectInfo.put(key,objProdEscrow.getString(key));
        key = "product_cusodian_documents";getSubjectInfo.put(key,objProdEscrow.getString(key));
        key = "product_custodian_notes";getSubjectInfo.put(key,objProdEscrow.getString(key));
        key = "product_escrow_deregistration_date";getSubjectInfo.put(key,objProdEscrow.getString(key));
        key = "product_escrow_deregistration_document";getSubjectInfo.put(key,objProdEscrow.getString(key));
        key = "product_escrow_deregistration_remarks";getSubjectInfo.put(key,objProdEscrow.getString(key));

        return getSubjectInfo;
    }

    public Map transInfo(com.alibaba.fastjson.JSONObject jobjOK){
        com.alibaba.fastjson.JSONObject objInfo = jobjOK.getJSONObject("body").getJSONObject("transaction_report_information");

        com.alibaba.fastjson.JSONObject objBase = objInfo.getJSONObject("basic_transaction_information");
        com.alibaba.fastjson.JSONObject objBaseBIR = objBase.getJSONObject("basic_information_remark");
        com.alibaba.fastjson.JSONObject objBaseTAI = objBase.getJSONObject("trading_asset_information");


        Map getSubjectInfo = new HashMap();
        key = "transaction_object_id";                      getSubjectInfo.put(key,getSubObjId(jobjOK));
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
        //交易资产托管状态
        key = "transaction_product_custody_status";                     getSubjectInfo.put(key,objBaseTAIACS.getString(key));
        //已托管交易资产交易
        key = "transaction_custody_product_ref";                   getSubjectInfo.put(key,objBaseTAICAT.getString(key));
        //未托管交易资产交易
        key = "transaction_product_name";getSubjectInfo.put(key,objBaseTAINCAT.getString(key));
        key = "transaction_product_issuer_ref";getSubjectInfo.put(key,objBaseTAINCAT.getString(key));
        key = "transaction_product_issuer_name";getSubjectInfo.put(key,objBaseTAINCAT.getString(key));
        key = "transaction_product_asset_type";getSubjectInfo.put(key,objBaseTAINCAT.getString(key));
        key = "transaction_product_asset_unit";getSubjectInfo.put(key,objBaseTAINCAT.getString(key));
        key = "transaction_product_asset_currency";getSubjectInfo.put(key,objBaseTAINCAT.getString(key));
        key = "transaction_product_asset_value";getSubjectInfo.put(key,objBaseTAINCAT.getString(key));
        key = "transaction_product_asset_doc";getSubjectInfo.put(key,objBaseTAINCAT.getString(key));
        key = "transaction_product_decription";getSubjectInfo.put(key,objBaseTAINCAT.getString(key));


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
                jobjOK.getJSONObject("body").getJSONObject("transaction_report_information").getJSONArray(key).toJSONString(), Map.class));

        return getSubjectInfo;
    }


    public Map regiInfo(com.alibaba.fastjson.JSONObject jobjOK){
        com.alibaba.fastjson.JSONObject objInfo = jobjOK.getJSONObject("body").getJSONObject("registration_information");
        com.alibaba.fastjson.JSONObject objRegBase = objInfo.getJSONObject("register_basic_infomation");

        com.alibaba.fastjson.JSONObject objRegRights = objInfo.getJSONObject("registration_rights");
        com.alibaba.fastjson.JSONObject objRegRigBase = objRegRights.getJSONObject("basic_information_rights");



        Map getSubjectInfo = new HashMap();
        //登记信息 登记基本信息
        key = "register_registration_object_id"; getSubjectInfo.put(key,getSubObjId(jobjOK));
        key = "register_object_type";        getSubjectInfo.put(key,objRegBase.getString(key));
        key = "register_event_type";       getSubjectInfo.put(key,objRegBase.getString(key));

        //登记信息 权利登记 权利基本信息 权利登记基本信息描述
        key = "register_serial_number";getSubjectInfo.put(key,objRegRigBase.getString(key));
        key = "register_time";getSubjectInfo.put(key,objRegRigBase.getString(key));
        key = "register_subject_ref";getSubjectInfo.put(key,objRegRigBase.getString(key));
        key = "register_subject_type";getSubjectInfo.put(key,objRegRigBase.getString(key));
        key = "register_subject_account_reference";getSubjectInfo.put(key,objRegRigBase.getString(key));
        key = "register_asset_type";getSubjectInfo.put(key,objRegRigBase.getString(key));
        key = "register_asset_unit";getSubjectInfo.put(key,objRegRigBase.getString(key));
        key = "register_asset_currency";getSubjectInfo.put(key,objRegRigBase.getString(key));
        key = "register_transaction_ref";getSubjectInfo.put(key,objRegRigBase.getString(key));
        key = "register_product_ref";getSubjectInfo.put(key,objRegRigBase.getString(key));
        key = "register_description";getSubjectInfo.put(key,objRegRigBase.getString(key));
        key = "register_create_time";getSubjectInfo.put(key,objRegRigBase.getString(key));
        key = "register_authentic_right_recognition_status";getSubjectInfo.put(key,objRegRigBase.getString(key));

        //登记信息 权利登记 权利基本信息 确权记录
        key = "register_authentic_right_recognition_date";getSubjectInfo.put(key,objRegRigBase.getString(key));
        key = "register_right_recognition_mode";getSubjectInfo.put(key,objRegRigBase.getString(key));
        key = "register_right_recognition_subject_ref";getSubjectInfo.put(key,objRegRigBase.getString(key));
        key = "register_right_recognition_subject_name";getSubjectInfo.put(key,objRegRigBase.getString(key));
        key = "register_right_recognition_agent_subject_ref";getSubjectInfo.put(key,objRegRigBase.getString(key));
        key = "register_right_recognition_agent_subject_name";getSubjectInfo.put(key,objRegRigBase.getString(key));
        key = "register_right_recognition_doc";getSubjectInfo.put(key,objRegRigBase.getString(key));
        key = "register_right_recognition_description";getSubjectInfo.put(key,objRegRigBase.getString(key));

        //登记信息 权利登记 权利基本信息 可用登记
        key = "register_asset_balance_change";getSubjectInfo.put(key,objRegRigBase.getString(key));
        key = "register_asset_balance_before";getSubjectInfo.put(key,objRegRigBase.getString(key));
        key = "register_asset_balance_after";getSubjectInfo.put(key,objRegRigBase.getString(key));

        //登记信息 权利登记 权利基本信息 质押登记
        key = "register_pledge_balance_change";getSubjectInfo.put(key,objRegRigBase.getString(key));
        key = "register_pledge_balance_before";getSubjectInfo.put(key,objRegRigBase.getString(key));
        key = "register_plged_balance_after";getSubjectInfo.put(key,objRegRigBase.getString(key));

        //登记信息 权利登记 权利基本信息 冻结登记
        key = "register_frozen_balance_change";getSubjectInfo.put(key,objRegRigBase.getString(key));
        key = "register_frozen_balance";getSubjectInfo.put(key,objRegRigBase.getString(key));
        key = "register_frozen_balance_after";getSubjectInfo.put(key,objRegRigBase.getString(key));
        key = "register_thaw_doc";getSubjectInfo.put(key,objRegRigBase.getString(key));
        key = "register_thaw_description";getSubjectInfo.put(key,objRegRigBase.getString(key));

        //登记信息 权利登记 权利基本信息 状态信息描述
        key = "register_asset_holding_status";getSubjectInfo.put(key,objRegRigBase.getString(key));
        key = "register_asset_holding_status_description";getSubjectInfo.put(key,objRegRigBase.getString(key));
        key = "register_asset_holding_nature";getSubjectInfo.put(key,objRegRigBase.getString(key));
        key = "register_asset_equity_type";getSubjectInfo.put(key,objRegRigBase.getString(key));
        key = "register_source_type";getSubjectInfo.put(key,objRegRigBase.getString(key));
        key = "register_asset_note";getSubjectInfo.put(key,objRegRigBase.getString(key));
        key = "register_asset_verrification_doc";getSubjectInfo.put(key,objRegRigBase.getString(key));

        //名册登记 名册基本信息
        com.alibaba.fastjson.JSONObject objRollRecords = objInfo.getJSONObject("roll_records");
        com.alibaba.fastjson.JSONObject objRollBase = objRollRecords.getJSONObject("basic_information_roster");

        key = "register_list_subject_ref";getSubjectInfo.put(key,objRollBase.getString(key));
        key = "register_product_ref";getSubjectInfo.put(key,objRollBase.getString(key));
        key = "register_product_name";getSubjectInfo.put(key,objRollBase.getString(key));
        key = "register_product_description";getSubjectInfo.put(key,objRollBase.getString(key));
        key = "register_list_asset_type";getSubjectInfo.put(key,objRollBase.getString(key));
        key = "register_list_date";getSubjectInfo.put(key,objRollBase.getString(key));

        //名册登记 股东名册
        key = "register_shareholders";getSubjectInfo.put(key,objRollRecords.getString(key));
        //名册登记 债权人名册
        key = "register_creditors";getSubjectInfo.put(key,objRollRecords.getString(key));
        //名册登记 基金投资人名册
        key = "fund_investors";getSubjectInfo.put(key,objRollRecords.getString(key));
        return getSubjectInfo;
    }

//    public Map settleInfo(com.alibaba.fastjson.JSONObject jobjOK){
//
//        com.alibaba.fastjson.JSONObject objInfo = jobjOK.getJSONObject("body").getJSONObject("capital_object_information");
//        com.alibaba.fastjson.JSONObject objBase = jobjOK.getJSONObject("body").getJSONObject("capital_settlement_information").getJSONObject("basic_information_capital_settlement");
//        com.alibaba.fastjson.JSONObject objIn = jobjOK.getJSONObject("body").getJSONObject("capital_settlement_information").getJSONObject("transferee_information");
//        com.alibaba.fastjson.JSONObject objOut = jobjOK.getJSONObject("body").getJSONObject("capital_settlement_information").getJSONObject("transferor_information");
//
//        Map getSubjectInfo = new HashMap();
//        key = "capita_settlement_object_id";            getSubjectInfo.put(key,getSubObjId(jobjOK));
//        key = "capita_object_information_type";         getSubjectInfo.put(key,objInfo.getString(key));
//
//
//        key = "capita_clearing_house_subject_ref";      getSubjectInfo.put(key,objBase.getString(key));
//        key = "capita_settlement_type";                 getSubjectInfo.put(key,objBase.getString(key));
//        key = "capita_settlement_serial_num";           getSubjectInfo.put(key,objBase.getString(key));
//        key = "capita_settlement_time";                 getSubjectInfo.put(key,objBase.getString(key));
//        key = "capita_transaction_ref";                 getSubjectInfo.put(key,objBase.getString(key));
//        key = "capita_currency";                        getSubjectInfo.put(key,objBase.getString(key));
//        key = "capita_amount";                          getSubjectInfo.put(key,objBase.getString(key));
//        key = "capita_notes";                           getSubjectInfo.put(key,objBase.getString(key));
//        key = "capita_operation_certificates";          getSubjectInfo.put(key,objBase.getString(key));
//
//        key = "capita_out_bank_code";                   getSubjectInfo.put(key,objOut.getString(key));
//        key = "capita_out_bank_name";                   getSubjectInfo.put(key,objOut.getString(key));
//        key = "capita_out_bank_number";                 getSubjectInfo.put(key,objOut.getString(key));
//        key = "capita_out_account_obj_ref";             getSubjectInfo.put(key,objOut.getString(key));
//        key = "capita_out_fund_account_name";           getSubjectInfo.put(key,objOut.getString(key));
//        key = "capita_out_amount_before_transfer";      getSubjectInfo.put(key,objOut.getString(key));
//        key = "capita_out_amount_after_transfer";       getSubjectInfo.put(key,objOut.getString(key));
//
//        key = "capita_in_bank_code";                    getSubjectInfo.put(key,objIn.getString(key));
//        key = "capita_in_bank_name";                    getSubjectInfo.put(key,objIn.getString(key));
//        key = "capita_in_bank_number";                  getSubjectInfo.put(key,objIn.getString(key));
//        key = "capita_in_account_obj_ref";              getSubjectInfo.put(key,objIn.getString(key));
//        key = "capita_in_fund_account_name";            getSubjectInfo.put(key,objIn.getString(key));
//        key = "capita_in_account_number";               getSubjectInfo.put(key,objIn.getString(key));
//        key = "capita_in_amount_before_transfer";       getSubjectInfo.put(key,objIn.getString(key));
//        key = "capita_in_amount_after_transfer";        getSubjectInfo.put(key,objIn.getString(key));
//
//        return getSubjectInfo;
//    }

    public Map settleInfo(com.alibaba.fastjson.JSONObject jobjOK){

        com.alibaba.fastjson.JSONObject objInfo = jobjOK.getJSONObject("body").getJSONObject("capital_object_information");
        com.alibaba.fastjson.JSONObject objBase = jobjOK.getJSONObject("body").getJSONObject("capital_settlement_information").getJSONObject("basic_information_capital_settlement");
        com.alibaba.fastjson.JSONObject objIn = jobjOK.getJSONObject("body").getJSONObject("capital_settlement_information").getJSONObject("transferor_information");
        com.alibaba.fastjson.JSONObject objOut = jobjOK.getJSONObject("body").getJSONObject("capital_settlement_information").getJSONObject("transferee_information");

        Map getSubjectInfo = new HashMap();
        key = "capita_settlement_object_id";                        getSubjectInfo.put(key,getSubObjId(jobjOK));
        key = "capita_object_information_type";                     getSubjectInfo.put(key,objInfo.getString(key));


        key = "settlement_subject_ref";                             getSubjectInfo.put(key,objBase.getString(key));
        key = "settlement_type";                                    getSubjectInfo.put(key,objBase.getString(key));
        key = "settlement_serial_number";                           getSubjectInfo.put(key,objBase.getString(key));
        key = "settlement_time";                                    getSubjectInfo.put(key,objBase.getString(key));
        key = "settlement_product_ref";                             getSubjectInfo.put(key,objBase.getString(key));
        key = "settlement_transaction_reference";                   getSubjectInfo.put(key,objBase.getString(key));
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

        return getSubjectInfo;
    }

//    public Map pubInfo(com.alibaba.fastjson.JSONObject jobjOK) {
//        com.alibaba.fastjson.JSONObject objInfo = jobjOK.getJSONObject("body").getJSONObject("letter_object_identification");
//        com.alibaba.fastjson.JSONObject objDis = jobjOK.getJSONObject("body").getJSONObject("letter_approval_information").getJSONObject("enterprise_display_information");
//        com.alibaba.fastjson.JSONObject objletter = jobjOK.getJSONObject("body").getJSONObject("letter_approval_information").getJSONObject("enterprise_letter_information");
//        com.alibaba.fastjson.JSONObject objLetterBase = objletter.getJSONObject("basic_informati_letter_approval");
//        com.alibaba.fastjson.JSONObject objFin = objletter.getJSONObject("financial_information");
//
//
//        Map getSubjectInfo = new HashMap();
//        //对象标识信息
//        key = "letter_disclosure_object_id";            getSubjectInfo.put(key, getSubObjId(jobjOK));
//        key = "letter_object_information_type";         getSubjectInfo.put(key, objInfo.getString(key));
//
//        //企业展示信息
//        key = "letter_show_subject_reference";          getSubjectInfo.put(key, objDis.getString(key));
//        key = "letter_show_subject_reference_ref";      getSubjectInfo.put(key, objDis.getString(key));
//        key = "letter_display_code";                    getSubjectInfo.put(key, objDis.getString(key));
//        key = "letter_display_content";                 getSubjectInfo.put(key, objDis.getString(key));
//        key = "letter_display_main_audit_voucher";      getSubjectInfo.put(key, objDis.getString(key));
//        key = "letter_show_content_audit_voucher";      getSubjectInfo.put(key, objDis.getString(key));
//        key = "letter_show_start_date";                 getSubjectInfo.put(key, objDis.getString(key));
//        key = "letter_show_end_date";                   getSubjectInfo.put(key, objDis.getString(key));
//
//        //信批基本信息
//        key = "letter_approval_time";                   getSubjectInfo.put(key, objLetterBase.getString(key));
//        key = "letter_disclosure_subject_ref";          getSubjectInfo.put(key, objLetterBase.getString(key));
//        key = "letter_disclosure_uploader_ref";         getSubjectInfo.put(key, objLetterBase.getString(key));
//
//
//        //拼组诚信档案信息
//        com.alibaba.fastjson.JSONArray arrCD = objletter.getJSONArray("integrity_archives");
//        List<Map> tempList = new ArrayList<>();
//        for (int i = 0; i < arrCD.size(); i++) {
//            Map tempMap = new HashMap();
//            com.alibaba.fastjson.JSONObject objTemp = com.alibaba.fastjson.JSONObject.parseObject(arrCD.get(i).toString());
//            key = "letter_provider_subject_ref";        tempMap.put(key, objTemp.getJSONObject("basic_information").getString(key));
//            key = "letter_provider_name";               tempMap.put(key, objTemp.getJSONObject("basic_information").getString(key));
//            key = "letter_identified_party_subject_ref";tempMap.put(key, objTemp.getJSONObject("basic_information").getString(key));
//            key = "letter_identified_party_name";       tempMap.put(key, objTemp.getJSONObject("basic_information").getString(key));
//            key = "letter_appraiser_subject_ref";       tempMap.put(key, objTemp.getJSONObject("basic_information").getString(key));
//            key = "letter_appraiser_name";              tempMap.put(key, objTemp.getJSONObject("basic_information").getString(key));
//
//            key = "letter_item_number";                 tempMap.put(key, objTemp.getJSONObject("item_details").getString(key));
//            key = "letter_item_name";                   tempMap.put(key, objTemp.getJSONObject("item_details").getString(key));
//            key = "letter_item_type";                   tempMap.put(key, objTemp.getJSONObject("item_details").getString(key));
//            key = "letter_item_describe";               tempMap.put(key, objTemp.getJSONObject("item_details").getString(key));
//            key = "letter_term_of_validity";            tempMap.put(key, objTemp.getJSONObject("item_details").getString(key));
//            key = "letter_start_time";                  tempMap.put(key, objTemp.getJSONObject("item_details").getString(key));
//            key = "letter_end_time";                    tempMap.put(key, objTemp.getJSONObject("item_details").getString(key));
//            key = "letter_item_state";                  tempMap.put(key, objTemp.getJSONObject("item_details").getString(key));
//            key = "letter_item_file";                   tempMap.put(key, com.alibaba.fastjson.JSONObject.parseArray(
//                    objTemp.getJSONObject("item_details").getJSONArray(key).toJSONString(), String.class));
//
//            tempList.add(tempMap);
//        }
//
//        getSubjectInfo.put("integrity_archives", tempList);
//
//        //财务信息 基本财务信息
//        key = "letter_start_date";              getSubjectInfo.put(key, objFin.getJSONObject("basic_financial_information").getString(key));
//        key = "letter_deadline";                getSubjectInfo.put(key, objFin.getJSONObject("basic_financial_information").getString(key));
//        key = "letter_report_type";             getSubjectInfo.put(key, objFin.getJSONObject("basic_financial_information").getString(key));
//        key = "letter_ending_total_asset";      getSubjectInfo.put(key, objFin.getJSONObject("basic_financial_information").getString(key));
//        key = "letter_ending_net_asset";        getSubjectInfo.put(key, objFin.getJSONObject("basic_financial_information").getString(key));
//        key = "letter_total_liability";         getSubjectInfo.put(key, objFin.getJSONObject("basic_financial_information").getString(key));
//        key = "letter_current_operating_income";getSubjectInfo.put(key, objFin.getJSONObject("basic_financial_information").getString(key));
//        key = "letter_current_total_profit";    getSubjectInfo.put(key, objFin.getJSONObject("basic_financial_information").getString(key));
//        key = "letter_current_net_profit";      getSubjectInfo.put(key, objFin.getJSONObject("basic_financial_information").getString(key));
//        key = "letter_cash_flow";               getSubjectInfo.put(key, objFin.getJSONObject("basic_financial_information").getString(key));
//        key = "letter_whether_r&d_costs";       getSubjectInfo.put(key, objFin.getJSONObject("basic_financial_information").getString(key));
//        key = "letter_r&d_costs";               getSubjectInfo.put(key, objFin.getJSONObject("basic_financial_information").getString(key));
//
//        //财务信息 财务报表文件
//        key = "letter_balance_sheet";           getSubjectInfo.put(key, objFin.getJSONObject("financial_statement_documents").getString(key));
//        key = "letter_cash_flow_sheet";         getSubjectInfo.put(key, objFin.getJSONObject("financial_statement_documents").getString(key));
//        key = "letter_profit_sheet";            getSubjectInfo.put(key, objFin.getJSONObject("financial_statement_documents").getString(key));
//
//
//        //拼组重大事件信息
////        com.alibaba.fastjson.JSONArray objKeyEvent = objletter.getJSONArray("major_event_information");
////        List<Map> tempList2 = new ArrayList<>();
////        for(int i=0;i< objKeyEvent.size();i++){
////            Map tempMap = new HashMap();
////            com.alibaba.fastjson.JSONObject objTemp = com.alibaba.fastjson.JSONObject.parseObject(objKeyEvent.get(i).toString());
////            key = "letter_major_event_type";        tempMap.put(key, objTemp.getString(key));
////            key = "letter_file_list";               tempMap.put(key, objTemp.getString(key));
////            key = "letter_description_document";    tempMap.put(key, objTemp.getString(key));
////            key = "letter_submission_time";         tempMap.put(key, objTemp.getString(key));
////
////            tempList2.add(tempMap);
////        }
////        getSubjectInfo.put("major_event_information", tempList2);
//
//        //拼组公告信息
////        com.alibaba.fastjson.JSONArray objNotice = objletter.getJSONArray("letter_notice");
////        List<Map> tempList3 = new ArrayList<>();
////        for(int i=0;i< objNotice.size();i++){
////            Map tempMap = new HashMap();
////            com.alibaba.fastjson.JSONObject objTemp = com.alibaba.fastjson.JSONObject.parseObject(objNotice.get(i).toString());
////            key = "letter_announcement_type";        tempMap.put(key, objTemp.getString(key));
////            key = "letter_file_list";                tempMap.put(key, objTemp.getString(key));
////            key = "letter_description_announcement"; tempMap.put(key, objTemp.getString(key));
////            key = "letter_announcement_time";        tempMap.put(key, objTemp.getString(key));
////
////            tempList3.add(tempMap);
////        }
////        getSubjectInfo.put("letter_notice",tempList3);
//        key = "major_event_information";        getSubjectInfo.put(key,
//                com.alibaba.fastjson.JSONObject.parseArray(objletter.getJSONArray(key).toJSONString(), Map.class));
//        key = "letter_notice";                  getSubjectInfo.put(key,
//                com.alibaba.fastjson.JSONObject.parseArray(objletter.getJSONArray(key).toJSONString(), Map.class));
//
//
//        return getSubjectInfo;
//    }

    public Map pubInfo(com.alibaba.fastjson.JSONObject jobjOK) {
        com.alibaba.fastjson.JSONObject objInfo = jobjOK.getJSONObject("body").getJSONObject("letter_object_identification");
        com.alibaba.fastjson.JSONObject objDis = jobjOK.getJSONObject("body").getJSONObject("disclosure_approval_information").getJSONObject("enterprise_display_information");
        com.alibaba.fastjson.JSONObject objRegulatory = jobjOK.getJSONObject("body").getJSONObject("disclosure_approval_information").getJSONObject("regulatory_infomation");
        com.alibaba.fastjson.JSONObject objReport = jobjOK.getJSONObject("body").getJSONObject("disclosure_approval_information").getJSONObject("enterprise_report");
        com.alibaba.fastjson.JSONObject objNotice = jobjOK.getJSONObject("body").getJSONObject("disclosure_approval_information").getJSONObject("disclosure_notice");
        com.alibaba.fastjson.JSONObject objMajor = jobjOK.getJSONObject("body").getJSONObject("disclosure_approval_information").getJSONObject("major_event_information");
        com.alibaba.fastjson.JSONObject objIntegrity = jobjOK.getJSONObject("body").getJSONObject("disclosure_approval_information").getJSONObject("integrity_archives");
        com.alibaba.fastjson.JSONObject objExpandBasic = objIntegrity.getJSONObject("basic_information");
        com.alibaba.fastjson.JSONObject objExpandItem = objIntegrity.getJSONObject("item_details");
        com.alibaba.fastjson.JSONObject objFinancial = jobjOK.getJSONObject("body").getJSONObject("disclosure_approval_information").getJSONObject("financial_information");
        com.alibaba.fastjson.JSONObject objFinancialBasic = objFinancial.getJSONObject("basic_financial_information");
        com.alibaba.fastjson.JSONObject objFinancialDocuments = objFinancial.getJSONObject("financial_statement_documents");
        com.alibaba.fastjson.JSONObject objBusiness = jobjOK.getJSONObject("body").getJSONObject("disclosure_approval_information").getJSONObject("business_information");
        com.alibaba.fastjson.JSONObject objBusinessBasic = objBusiness.getJSONObject("business_basic_information");
        com.alibaba.fastjson.JSONObject objBusinessInvestment = objBusiness.getJSONObject("Investment_and_financing");
        com.alibaba.fastjson.JSONObject objExpand = jobjOK.getJSONObject("body").getJSONObject("disclosure_approval_information").getJSONObject("expand_information");

        Map getSubjectInfo = new HashMap();

        //信批基本信息
        key = "disclosure_subject_ref";                          getSubjectInfo.put(key, objInfo.getString(key));
        key = "disclosure_type";                                 getSubjectInfo.put(key, objInfo.getString(key));
        key = "disclosure_submit_type";                          getSubjectInfo.put(key, objInfo.getString(key));
        key = "disclosure_referer_subject_ref";                  getSubjectInfo.put(key, objInfo.getString(key));
        key = "disclosure_referer_name";                         getSubjectInfo.put(key, objInfo.getString(key));
        key = "disclosure_submit_date";                          getSubjectInfo.put(key, objInfo.getString(key));
        key = "disclosure_submit_description";                   getSubjectInfo.put(key, objInfo.getString(key));


        //拼组企业展示信息1
        com.alibaba.fastjson.JSONArray arrEDI = objDis.getJSONArray("enterprise_display_information");
        List<Map> tempList1 = new ArrayList<>();
        for (int i = 0; i < arrEDI.size(); i++) {
            Map tempMap = new HashMap();
            com.alibaba.fastjson.JSONObject objTemp = com.alibaba.fastjson.JSONObject.parseObject(arrEDI.get(i).toString());
            key = "disclosure_display_platform_ref";                                tempMap.put(key, objDis.getJSONObject("enterprise_display_information").getString(key));
            key = "disclosure_display_name";                                        tempMap.put(key, objDis.getJSONObject("enterprise_display_information").getString(key));
            key = "disclosure_display_doc";                                         tempMap.put(key, objDis.getJSONObject("enterprise_display_information").getString(key));
            key = "disclosure_display_description";                                 tempMap.put(key, objDis.getJSONObject("enterprise_display_information").getString(key));
            key = "disclosure_display_code";                                        tempMap.put(key, objDis.getJSONObject("enterprise_display_information").getString(key));
            key = "disclosure_display_content";                                     tempMap.put(key, objDis.getJSONObject("enterprise_display_information").getString(key));
            key = "disclosure_display_start_date";                                  tempMap.put(key, objDis.getJSONObject("enterprise_display_information").getString(key));
            key = "disclosure_display_end_date";                                    tempMap.put(key, objDis.getJSONObject("enterprise_display_information").getString(key));

            tempList1.add(tempMap);
        }

        getSubjectInfo.put("enterprise_display_information", tempList1);

        //拼组监管信息3
        com.alibaba.fastjson.JSONArray arrRI = objRegulatory.getJSONArray("regulatory_infomation");
        List<Map> tempList3 = new ArrayList<>();
        for (int i = 0; i < arrRI.size(); i++) {
            Map tempMap = new HashMap();
            com.alibaba.fastjson.JSONObject objTemp = com.alibaba.fastjson.JSONObject.parseObject(arrRI.get(i).toString());
            key = "disclosure_enterprise_report_type";                                        tempMap.put(key, objRegulatory.getJSONObject("regulatory_infomation").getString(key));
            key = "disclosure_enterprise_report_name";                                        tempMap.put(key, objRegulatory.getJSONObject("regulatory_infomation").getString(key));
            key = "disclosure_enterprise_report_doc";                                         tempMap.put(key, objRegulatory.getJSONObject("regulatory_infomation").getString(key));
            key = "disclosure_enterprise_report_description";                                 tempMap.put(key, objRegulatory.getJSONObject("regulatory_infomation").getString(key));
            key = "disclosure_enterprise_report_date";                                        tempMap.put(key, objRegulatory.getJSONObject("regulatory_infomation").getString(key));

            tempList3.add(tempMap);
        }

        getSubjectInfo.put("regulatory_infomation", tempList3);

        //拼组企业报告4
        com.alibaba.fastjson.JSONArray arrER = objReport.getJSONArray("enterprise_report");
        List<Map> tempList4 = new ArrayList<>();
        for (int i = 0; i < arrER.size(); i++) {
            Map tempMap = new HashMap();
            com.alibaba.fastjson.JSONObject objTemp = com.alibaba.fastjson.JSONObject.parseObject(arrER.get(i).toString());
            key = "disclosure_enterprise_report_type";                                        tempMap.put(key, objReport.getJSONObject("enterprise_report").getString(key));
            key = "disclosure_enterprise_report_name";                                        tempMap.put(key, objReport.getJSONObject("enterprise_report").getString(key));
            key = "disclosure_enterprise_report_doc";                                         tempMap.put(key, objReport.getJSONObject("enterprise_report").getString(key));
            key = "disclosure_enterprise_report_description";                                 tempMap.put(key, objReport.getJSONObject("enterprise_report").getString(key));
            key = "disclosure_enterprise_report_date";                                        tempMap.put(key, objReport.getJSONObject("enterprise_report").getString(key));

            tempList4.add(tempMap);
        }

        getSubjectInfo.put("enterprise_report", tempList4);

        //拼组公告信息5
        com.alibaba.fastjson.JSONArray arrDN = objNotice.getJSONArray("disclosure_notice");
        List<Map> tempList5 = new ArrayList<>();
        for (int i = 0; i < arrDN.size(); i++) {
            Map tempMap = new HashMap();
            com.alibaba.fastjson.JSONObject objTemp = com.alibaba.fastjson.JSONObject.parseObject(arrDN.get(i).toString());
            key = "disclosure_announcement_type";                                        tempMap.put(key, objNotice.getJSONObject("disclosure_notice").getString(key));
            key = "disclosure_announcement_name";                                        tempMap.put(key, objNotice.getJSONObject("disclosure_notice").getString(key));
            key = "disclosure_announcement_doc";                                         tempMap.put(key, objNotice.getJSONObject("disclosure_notice").getString(key));
            key = "disclosure_announcement_description";                                 tempMap.put(key, objNotice.getJSONObject("disclosure_notice").getString(key));
            key = "disclosure_announcement_date";                                        tempMap.put(key, objNotice.getJSONObject("disclosure_notice").getString(key));

            tempList5.add(tempMap);
        }

        getSubjectInfo.put("disclosure_notice", tempList5);

        //拼组重大事件信息6
        com.alibaba.fastjson.JSONArray arrMAE = objMajor.getJSONArray("major_event_information");
        List<Map> tempList6 = new ArrayList<>();
        for (int i = 0; i < arrMAE.size(); i++) {
            Map tempMap = new HashMap();
            com.alibaba.fastjson.JSONObject objTemp = com.alibaba.fastjson.JSONObject.parseObject(arrMAE.get(i).toString());
            key = "disclosure_major_event_type";                                        tempMap.put(key, objMajor.getJSONObject("major_event_information").getString(key));
            key = "disclosure_major_event_name";                                        tempMap.put(key, objMajor.getJSONObject("major_event_information").getString(key));
            key = "disclosure_major_event_doc";                                         tempMap.put(key, objMajor.getJSONObject("major_event_information").getString(key));
            key = "disclosure_major_event_document_description";                        tempMap.put(key, objMajor.getJSONObject("major_event_information").getString(key));
            key = "disclosure_major_event_reporting_date";                              tempMap.put(key, objMajor.getJSONObject("major_event_information").getString(key));

            tempList6.add(tempMap);
        }

        getSubjectInfo.put("major_event_information", tempList6);

        //拼组诚信档案信息7
        com.alibaba.fastjson.JSONArray arrCD = objIntegrity.getJSONArray("integrity_archives");
        List<Map> tempList7 = new ArrayList<>();
        for (int i = 0; i < arrCD.size(); i++) {
            Map tempMap = new HashMap();
            com.alibaba.fastjson.JSONObject objTemp = com.alibaba.fastjson.JSONObject.parseObject(arrCD.get(i).toString());
            //事项基本信息
            key = "disclosure_identifier_ref";                      tempMap.put(key, objExpandBasic.getJSONObject("basic_information").getString(key));
            key = "disclosure_identifier_name";                     tempMap.put(key, objExpandBasic.getJSONObject("basic_information").getString(key));
            key = "disclosure_auditor_ref";                         tempMap.put(key, objExpandBasic.getJSONObject("basic_information").getString(key));
            key = "disclosure_auditor_name";                        tempMap.put(key, objExpandBasic.getJSONObject("basic_information").getString(key));
            //事项明细
            key = "disclosure_event_id";                            tempMap.put(key, objExpandItem.getJSONObject("item_details").getString(key));
            key = "disclosure_event_name";                          tempMap.put(key, objExpandItem.getJSONObject("item_details").getString(key));
            key = "disclosure_event_doc";                           tempMap.put(key, objExpandItem.getJSONObject("item_details").getString(key));
            key = "disclosure_event_description";                   tempMap.put(key, objExpandItem.getJSONObject("item_details").getString(key));
            key = "disclosure_event_type";                          tempMap.put(key, objExpandItem.getJSONObject("item_details").getString(key));
            key = "disclosure_event_valid_time";                    tempMap.put(key, objExpandItem.getJSONObject("item_details").getString(key));
            key = "disclosure_event_start_date";                    tempMap.put(key, objExpandItem.getJSONObject("item_details").getString(key));
            key = "disclosure_event_end_date";                      tempMap.put(key, objExpandItem.getJSONObject("item_details").getString(key));
            key = "disclosure_event_status";                        tempMap.put(key, objExpandItem.getJSONObject("item_details").getString(key));

            tempList7.add(tempMap);
        }

        getSubjectInfo.put("integrity_archives", tempList7);

        //拼组财务信息8
        com.alibaba.fastjson.JSONArray arrFI = objFinancial.getJSONArray("financial_information");
        List<Map> tempList8 = new ArrayList<>();
        for (int i = 0; i < arrFI.size(); i++) {
            Map tempMap = new HashMap();
            com.alibaba.fastjson.JSONObject objTemp = com.alibaba.fastjson.JSONObject.parseObject(arrFI.get(i).toString());
            //财务信息 基本财务信息
            key = "disclosure_financial_start_date";                        getSubjectInfo.put(key, objFinancialBasic.getJSONObject("basic_financial_information").getString(key));
            key = "disclosure_financial_end_date";                          getSubjectInfo.put(key, objFinancialBasic.getJSONObject("basic_financial_information").getString(key));
            key = "disclosure_financial_type";                              getSubjectInfo.put(key, objFinancialBasic.getJSONObject("basic_financial_information").getString(key));
            key = "disclosure_financial_periodend_total_asset";             getSubjectInfo.put(key, objFinancialBasic.getJSONObject("basic_financial_information").getString(key));
            key = "disclosure_financial_periodend_net_asset";               getSubjectInfo.put(key, objFinancialBasic.getJSONObject("basic_financial_information").getString(key));
            key = "disclosure_financial_periodend_total_liability";         getSubjectInfo.put(key, objFinancialBasic.getJSONObject("basic_financial_information").getString(key));
            key = "disclosure_financial_periodend_revenue";                 getSubjectInfo.put(key, objFinancialBasic.getJSONObject("basic_financial_information").getString(key));
            key = "disclosure_financial_periodend_gross_profit";            getSubjectInfo.put(key, objFinancialBasic.getJSONObject("basic_financial_information").getString(key));
            key = "disclosure_financial_periodend_net_profit";              getSubjectInfo.put(key, objFinancialBasic.getJSONObject("basic_financial_information").getString(key));
            key = "disclosure_financial_cashflow";                          getSubjectInfo.put(key, objFinancialBasic.getJSONObject("basic_financial_information").getString(key));
            key = "disclosure_financial_whether_rd&d_costs";                getSubjectInfo.put(key, objFinancialBasic.getJSONObject("basic_financial_information").getString(key));
            key = "disclosure_financial_rd_cost";                           getSubjectInfo.put(key, objFinancialBasic.getJSONObject("basic_financial_information").getString(key));
            //财务信息 财务报表文件
            key = "disclosure_financial_balance_sheet_name";               getSubjectInfo.put(key, objFinancialDocuments.getJSONObject("financial_statement_documents").getString(key));
            key = "disclosure_financial_balance_sheet";                    getSubjectInfo.put(key, objFinancialDocuments.getJSONObject("financial_statement_documents").getString(key));
            key = "disclosure_financial_balance_sheet_description";        getSubjectInfo.put(key, objFinancialDocuments.getJSONObject("financial_statement_documents").getString(key));
            key = "disclosure_financial_cashflow_statement_name";          getSubjectInfo.put(key, objFinancialDocuments.getJSONObject("financial_statement_documents").getString(key));
            key = "disclosure_financial_cashflow_statement";               getSubjectInfo.put(key, objFinancialDocuments.getJSONObject("financial_statement_documents").getString(key));
            key = "disclosure_financial_cashflow_statement_description";   getSubjectInfo.put(key, objFinancialDocuments.getJSONObject("financial_statement_documents").getString(key));
            key = "disclosure_financial_income_statement_name";            getSubjectInfo.put(key, objFinancialDocuments.getJSONObject("financial_statement_documents").getString(key));
            key = "disclosure_financial_income_statement";                 getSubjectInfo.put(key, objFinancialDocuments.getJSONObject("financial_statement_documents").getString(key));
            key = "disclosure_financial_income_statement_description";     getSubjectInfo.put(key, objFinancialDocuments.getJSONObject("financial_statement_documents").getString(key));

            tempList8.add(tempMap);
        }

        getSubjectInfo.put("financial_information", tempList8);

        //拼组企业经营信息9
        com.alibaba.fastjson.JSONArray arrBI = objBusiness.getJSONArray("business_information");
        List<Map> tempList9 = new ArrayList<>();
        for (int i = 0; i < arrFI.size(); i++) {
            Map tempMap = new HashMap();
            com.alibaba.fastjson.JSONObject objTemp = com.alibaba.fastjson.JSONObject.parseObject(arrBI.get(i).toString());
            //企业经营信息 经营基本信息
            key = "disclosure_business_overview";                             getSubjectInfo.put(key, objBusinessBasic.getJSONObject("business_basic_information").getString(key));
            key = "disclosure_main_business_analysis";                        getSubjectInfo.put(key, objBusinessBasic.getJSONObject("business_basic_information").getString(key));
            key = "disclosure_main_business_analysis_non";                    getSubjectInfo.put(key, objBusinessBasic.getJSONObject("business_basic_information").getString(key));
            key = "disclosure_assets_and_liabilities_analysis";               getSubjectInfo.put(key, objBusinessBasic.getJSONObject("business_basic_information").getString(key));
            key = "disclosure_major_shareholders_analysis";                   getSubjectInfo.put(key, objBusinessBasic.getJSONObject("business_basic_information").getString(key));
            key = "disclosure_major_events";                                  getSubjectInfo.put(key, objBusinessBasic.getJSONObject("business_basic_information").getString(key));
            key = "disclosure_business_report_name";                          getSubjectInfo.put(key, objBusinessBasic.getJSONObject("business_basic_information").getString(key));
            key = "disclosure_business_report_doc";                           getSubjectInfo.put(key, objBusinessBasic.getJSONObject("business_basic_information").getString(key));
            key = "disclosure_business_report_description";                   getSubjectInfo.put(key, objBusinessBasic.getJSONObject("business_basic_information").getString(key));
            key = "disclosure_business_report_date";                          getSubjectInfo.put(key, objBusinessBasic.getJSONObject("business_basic_information").getString(key));
            //企业经营信息 投融资信息
            key = "disclosure_investment_analysis";                          getSubjectInfo.put(key, objBusinessInvestment.getJSONObject("Investment_and_financing").getString(key));
            key = "disclosure_fund_raising_analysis";                        getSubjectInfo.put(key, objBusinessInvestment.getJSONObject("Investment_and_financing").getString(key));
            key = "disclosure_sell_situation_analysis";                      getSubjectInfo.put(key, objBusinessInvestment.getJSONObject("Investment_and_financing").getString(key));

            tempList9.add(tempMap);
        }

        getSubjectInfo.put("business_information", tempList9);

        //拼组第三方拓展信息10
        com.alibaba.fastjson.JSONArray arrEI = objExpand.getJSONArray("expand_information");
        List<Map> tempList10 = new ArrayList<>();
        for (int i = 0; i < arrFI.size(); i++) {
            Map tempMap = new HashMap();
            com.alibaba.fastjson.JSONObject objTemp = com.alibaba.fastjson.JSONObject.parseObject(arrEI.get(i).toString());

            key = "disclosure_expand_name";                             getSubjectInfo.put(key, objExpand.getJSONObject("expand_information").getString(key));
            key = "disclosure_expand_doc";                              getSubjectInfo.put(key, objExpand.getJSONObject("expand_information").getString(key));
            key = "disclosure_expand_description";                      getSubjectInfo.put(key, objExpand.getJSONObject("expand_information").getString(key));
            key = "disclosure_expand_date";                             getSubjectInfo.put(key, objExpand.getJSONObject("expand_information").getString(key));

            tempList10.add(tempMap);
        }

        getSubjectInfo.put("expand_information", tempList10);

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
}