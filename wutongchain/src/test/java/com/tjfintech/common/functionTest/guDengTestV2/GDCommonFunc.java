package com.tjfintech.common.functionTest.guDengTestV2;

import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.Interface.Token;
import com.tjfintech.common.MgToolCmd;
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
            sleepAndSaveInfo(6000,"等待下一个块交易打块");
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

//    public static List<Map> gdConstructShareListNoTxReport(String address, double amount, int shareProperty){
//        GDBeforeCondition gdbf = new GDBeforeCondition();
//        Map tempReg = gdbf.init05RegInfo();
//        tempReg.put("权利人账户引用",mapAccAddr.get(address));
//        tempReg.put("register_nature_of_shares",shareProperty);
//
//        Map<String,Object> shares = new HashMap<>();
//        shares.put("address",address);
//        shares.put("amount",amount);
//        shares.put("shareProperty",shareProperty);
//        shares.put("registerInformation",tempReg);
//
//        List<Map> shareList = new ArrayList<>();
//        shareList.add(shares);
//        return shareList;
//    }

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
//        tempReg.put("权利人账户引用",mapAccAddr.get(address));
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

        com.alibaba.fastjson.JSONObject jobj2 = parseJSONBaseOnJSONStr(storeData2,"主体");

        return subjectInfoEnterprise(jobj2);
    }


    public Map contructPersonalSubInfo(String personalTxId){
        log.info("检查的交易id " + personalTxId);

        com.alibaba.fastjson.JSONObject object2 = com.alibaba.fastjson.JSONObject.parseObject(store.GetTxDetail(personalTxId));
        String storeData2 = object2.getJSONObject("data").getJSONObject("store").getString("storeData");

        com.alibaba.fastjson.JSONObject jobj2 = parseJSONBaseOnJSONStr(storeData2,"主体");

        return subjectInfoPerson(jobj2);
    }


    public Map getEnterpriseSubInfo(String response){
        com.alibaba.fastjson.JSONObject object2 = com.alibaba.fastjson.JSONObject.parseObject(response);
        String storeData2 = object2.getString("data");//.getJSONObject("store").getString("storeData");

        com.alibaba.fastjson.JSONObject jobj2 = parseJSONBaseOnJSONStrCompatible(storeData2,"主体");
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
            if (objTemp.getJSONObject("header").getJSONObject("content").getString("type").equals("账户") &&
                    (objTemp.getJSONObject("body").getJSONObject("account_information").getJSONObject("basic_account_information").getIntValue("account_type") == 1) &&
                    objTemp.getJSONObject("body").getJSONObject("account_object_information").getString("account_object_id").equals(objId)) {
                jobjOK = objTemp;
                break;
            }
        }
        log.info("***********" + jobjOK.toString());
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
            if( objTemp.getJSONObject("header").getJSONObject("content").getString("type").equals("账户") &&
                    (objTemp.getJSONObject("body").getJSONObject("account_information").getJSONObject("basic_account_information").getIntValue("account_type") == 0) &&
                    objTemp.getJSONObject("body").getJSONObject("account_object_information").getString("account_object_id").equals(objId)){
                jobjOK = objTemp;
                break;
            }
        }

        return accountInfo(jobjOK);
    }


    public Map contructEquityProdInfo(String prodTxId){
        log.info("检查的交易id " + prodTxId);
        com.alibaba.fastjson.JSONObject object2 = com.alibaba.fastjson.JSONObject.parseObject(store.GetTxDetail(prodTxId));
        String storeData2 = object2.getJSONObject("data").getJSONObject("store").getString("storeData");

        com.alibaba.fastjson.JSONObject jobj2 = parseJSONBaseOnJSONStr(storeData2,"产品");
        return productInfo(jobj2,"1");
    }

    public Map contructBondProdInfo(String prodTxId){
        log.info("检查的交易id " + prodTxId);
        com.alibaba.fastjson.JSONObject object2 = com.alibaba.fastjson.JSONObject.parseObject(store.GetTxDetail(prodTxId));
        String storeData2 = object2.getJSONObject("data").getJSONObject("store").getString("storeData");

        com.alibaba.fastjson.JSONObject jobj2 = parseJSONBaseOnJSONStr(storeData2,"产品");
        return productInfo(jobj2,"2");
    }

    public Map contructFundProdInfo(String prodTxId){
        log.info("检查的交易id " + prodTxId);
        com.alibaba.fastjson.JSONObject object2 = com.alibaba.fastjson.JSONObject.parseObject(store.GetTxDetail(prodTxId));
        String storeData2 = object2.getJSONObject("data").getJSONObject("store").getString("storeData");

        com.alibaba.fastjson.JSONObject jobj2 = parseJSONBaseOnJSONStr(storeData2,"产品");
        return productInfo(jobj2,"3");
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
            if( objTemp.getJSONObject("header").getJSONObject("content").getString("type").equals("交易报告") &&
                    objTemp.getJSONObject("body").getJSONObject("transaction_report_information"
                    ).getJSONObject("transaction_information").getJSONObject("transaction_party_information"
                    ).getString("transaction_original_owner_subject_ref").equals(objId)){
                jobjOK = objTemp;
                break;
            }
        }
        log.info(jobjOK.toString());
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

            if( type.equals("登记") ){
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
        return regiInfo(jobjOK);
    }

    //一个登记信息
    public Map contructOneRegisterInfo(String TxId){
        com.alibaba.fastjson.JSONObject object2 = com.alibaba.fastjson.JSONObject.parseObject(store.GetTxDetail(TxId));
        String storeData2 = object2.getJSONObject("data").getJSONObject("store").getString("storeData");

        com.alibaba.fastjson.JSONObject jobjOK = parseJSONBaseOnJSONStr(storeData2,"登记");

        return regiInfo(jobjOK);
    }

    public Map contructSettleInfo(String TxId){
        com.alibaba.fastjson.JSONObject object2 = com.alibaba.fastjson.JSONObject.parseObject(store.GetTxDetail(TxId));
        String storeData2 = object2.getJSONObject("data").getJSONObject("store").getString("storeData");

        com.alibaba.fastjson.JSONObject jobjOK = parseJSONBaseOnJSONStr(storeData2,"资金结算");

        return settleInfo(jobjOK);
    }

    public Map contructPublishInfo(String TxId){
        com.alibaba.fastjson.JSONObject object2 = com.alibaba.fastjson.JSONObject.parseObject(store.GetTxDetail(TxId));
        String storeData2 = object2.getJSONObject("data").getJSONObject("store").getString("storeData");

        com.alibaba.fastjson.JSONObject jobjOK = parseJSONBaseOnJSONStr(storeData2,"信批");

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
        com.alibaba.fastjson.JSONObject objInfo = jobj2.getJSONObject("body").getJSONObject("subject_object_information");
        com.alibaba.fastjson.JSONObject objSubI = jobj2.getJSONObject("body").getJSONObject("subject_information");
        com.alibaba.fastjson.JSONObject objSubBase = objSubI.getJSONObject("basic_information_subject");
        com.alibaba.fastjson.JSONObject objSubBaseCommon = objSubBase.getJSONObject("general_information_subject");
        com.alibaba.fastjson.JSONObject objSubBaseQual = objSubBase.getJSONObject("subject_qualification_information");
        com.alibaba.fastjson.JSONObject objSubBaseQQ = objSubBaseQual.getJSONObject("subject_qualification_information");
        com.alibaba.fastjson.JSONObject objSubBaseQQ1 = objSubBaseQual.getJSONObject("subject_qualification_information1");

        com.alibaba.fastjson.JSONObject objEnterpriseSub = jobj2.getJSONObject("body").getJSONObject("subject_information").getJSONObject("subject_main_body_information");
        com.alibaba.fastjson.JSONObject objEnterpriseSubClassify = objEnterpriseSub.getJSONObject("classification_information");
        com.alibaba.fastjson.JSONObject objEnterpriseSubBase = objEnterpriseSub.getJSONObject("basic_information_enterprise");
        com.alibaba.fastjson.JSONObject objEnterpriseSubCorp = objEnterpriseSub.getJSONObject("corporate_information");

        Map getSubjectInfo = new HashMap();
        key = "subject_object_id";                          getSubjectInfo.put(key,objInfo.getString(key));
        key = "subject_object_information_type";            getSubjectInfo.put(key,objInfo.getString(key));

        key = "subject_id";                                 getSubjectInfo.put(key,objSubBaseCommon.getString(key));
        key = "subject_industry_code";                      getSubjectInfo.put(key,objSubBaseCommon.getString(key));
        key = "subject_type";                               getSubjectInfo.put(key,objSubBaseCommon.getString(key));
        key = "subject_create_time";                        getSubjectInfo.put(key,objSubBaseCommon.getString(key));


        key = "subject_qualification_category";             getSubjectInfo.put(key,objSubBaseQQ.getString(key));
        key = "subject_market_roles_types";                 getSubjectInfo.put(key,objSubBaseQQ.getString(key));
        key = "subject_financial_qualification_types";      getSubjectInfo.put(key,objSubBaseQQ.getString(key));
        key = "subject_qualification_category";             getSubjectInfo.put(key,objSubBaseQQ.getString(key));
        key = "subject_market_roles_types";                 getSubjectInfo.put(key,objSubBaseQQ.getString(key));
        key = "subject_financial_qualification_types";      getSubjectInfo.put(key,objSubBaseQQ.getString(key));

        key = "subject_role_qualification_number";          getSubjectInfo.put(key,objSubBaseQQ1.getString(key));
        key = "subject_role_qualification_certification_document";getSubjectInfo.put(key, com.alibaba.fastjson.JSONObject.parseArray(
                objSubBaseQQ1.getJSONArray(key).toJSONString(), String.class));
        key = "subject_qualification_party_certification";  getSubjectInfo.put(key,objSubBaseQQ1.getString(key));
        key = "subject_role_qualification_reviewer";        getSubjectInfo.put(key,objSubBaseQQ1.getString(key));
        key = "subject_certification_time";                 getSubjectInfo.put(key,objSubBaseQQ1.getString(key));
        key = "subject_audit_time";                         getSubjectInfo.put(key,objSubBaseQQ1.getString(key));

//        key = "subject_qualification_information";   getSubjectInfo.put(key, com.alibaba.fastjson.JSONObject.parseArray(
//                objSubBase.getJSONArray(key).toJSONString(), Map.class));

        key = "subject_organization_type";          getSubjectInfo.put(key,objEnterpriseSubClassify.getString(key));
        key = "subject_organization_nature";        getSubjectInfo.put(key,objEnterpriseSubClassify.getString(key));

        key = "subject_company_name";               getSubjectInfo.put(key,objEnterpriseSubBase.getString(key));
        key = "subject_company_english_name";       getSubjectInfo.put(key,objEnterpriseSubBase.getString(key));
        key = "subject_company_short_name";         getSubjectInfo.put(key,objEnterpriseSubBase.getString(key));
        key = "subject_company_short_english_name"; getSubjectInfo.put(key,objEnterpriseSubBase.getString(key));
        key = "subject_company_type";               getSubjectInfo.put(key,objEnterpriseSubBase.getString(key));
        key = "subject_company_component";          getSubjectInfo.put(key,objEnterpriseSubBase.getString(key));
        key = "subject_unified_social_credit_code"; getSubjectInfo.put(key,objEnterpriseSubBase.getString(key));
        key = "subject_organization_code";          getSubjectInfo.put(key,objEnterpriseSubBase.getString(key));
        key = "subject_establishment_day";          getSubjectInfo.put(key,objEnterpriseSubBase.getString(key));
        key = "subject_business_license";           getSubjectInfo.put(key,objEnterpriseSubBase.getString(key));
        key = "subject_business_scope";             getSubjectInfo.put(key,objEnterpriseSubBase.getString(key));
        key = "subject_industry";                   getSubjectInfo.put(key,objEnterpriseSubBase.getString(key));
        key = "subject_company_business";           getSubjectInfo.put(key,objEnterpriseSubBase.getString(key));
        key = "subject_company_profile";            getSubjectInfo.put(key,objEnterpriseSubBase.getString(key));
        key = "subject_registered_capital";         getSubjectInfo.put(key,objEnterpriseSubBase.getString(key));
        key = "subject_registered_capital_currency";getSubjectInfo.put(key,objEnterpriseSubBase.getString(key));
        key = "subject_paid_in_capital";            getSubjectInfo.put(key,objEnterpriseSubBase.getString(key));
        key = "subject_paid_in_capital_currency";   getSubjectInfo.put(key,objEnterpriseSubBase.getString(key));
        key = "subject_registered_address";         getSubjectInfo.put(key,objEnterpriseSubBase.getString(key));
        key = "subject_office_address";             getSubjectInfo.put(key,objEnterpriseSubBase.getString(key));
        key = "subject_contact_address";            getSubjectInfo.put(key,objEnterpriseSubBase.getString(key));
        key = "subject_contact_number";             getSubjectInfo.put(key,objEnterpriseSubBase.getString(key));
        key = "subject_fax";                        getSubjectInfo.put(key,objEnterpriseSubBase.getString(key));
        key = "subject_postal_code";                getSubjectInfo.put(key,objEnterpriseSubBase.getString(key));
        key = "subject_internet_address";           getSubjectInfo.put(key,objEnterpriseSubBase.getString(key));
        key = "subject_mail_box";                   getSubjectInfo.put(key,objEnterpriseSubBase.getString(key));
        key = "subject_association_articles";       getSubjectInfo.put(key,objEnterpriseSubBase.getString(key));
        key = "subject_competent_unit";             getSubjectInfo.put(key,objEnterpriseSubBase.getString(key));
        key = "subject_shareholders_number";        getSubjectInfo.put(key,objEnterpriseSubBase.getString(key));
        key = "subject_total_share_capital";        getSubjectInfo.put(key,objEnterpriseSubBase.getString(key));
        key = "subject_actual_controller";          getSubjectInfo.put(key,objEnterpriseSubBase.getString(key));
        key = "subject_actual_controller_id_type";  getSubjectInfo.put(key,objEnterpriseSubBase.getString(key));
        key = "subject_actual_controller_id";       getSubjectInfo.put(key,objEnterpriseSubBase.getString(key));

        key = "subject_legal_rep_name";             getSubjectInfo.put(key,objEnterpriseSubCorp.getString(key));
        key = "subject_legal_person_nature";        getSubjectInfo.put(key,objEnterpriseSubCorp.getString(key));
        key = "subject_legal_rep_id_doc_type";      getSubjectInfo.put(key,objEnterpriseSubCorp.getString(key));
        key = "subject_legal_rep_id_doc_number";    getSubjectInfo.put(key,objEnterpriseSubCorp.getString(key));
        key = "subject_legal_rep_post";             getSubjectInfo.put(key,objEnterpriseSubCorp.getString(key));
        key = "subject_legal_rep_cellphone_number"; getSubjectInfo.put(key,objEnterpriseSubCorp.getString(key));

        return getSubjectInfo;
    }

    public Map subjectInfoPerson(com.alibaba.fastjson.JSONObject jobj2){
        com.alibaba.fastjson.JSONObject objInfo = jobj2.getJSONObject("body").getJSONObject("subject_object_information");
        com.alibaba.fastjson.JSONObject objSubI = jobj2.getJSONObject("body").getJSONObject("subject_information");
        com.alibaba.fastjson.JSONObject objSubBase = objSubI.getJSONObject("basic_information_subject");
        com.alibaba.fastjson.JSONObject objSubBaseCommon = objSubBase.getJSONObject("general_information_subject");
        com.alibaba.fastjson.JSONObject objSubBaseQual = objSubBase.getJSONObject("subject_qualification_information");
        com.alibaba.fastjson.JSONObject objSubBaseQQ = objSubBaseQual.getJSONObject("subject_qualification_information");
        com.alibaba.fastjson.JSONObject objSubBaseQQ1 = objSubBaseQual.getJSONObject("subject_qualification_information1");

        com.alibaba.fastjson.JSONObject objPersonSub = objSubI.getJSONObject("personal_subject_information");
        com.alibaba.fastjson.JSONObject objPersonSubBase = objPersonSub.getJSONObject("basic_information_individual_subject");
        com.alibaba.fastjson.JSONObject objPersonRate = objPersonSub.getJSONObject("individual_subject_rating");

        Map getSubjectInfo = new HashMap();
        key = "subject_object_id";                  getSubjectInfo.put(key,objInfo.getString(key));
        key = "subject_object_information_type";    getSubjectInfo.put(key,objInfo.getString(key));

        key = "subject_id";                         getSubjectInfo.put(key,objSubBaseCommon.getString(key));
        key = "subject_industry_code";              getSubjectInfo.put(key,objSubBaseCommon.getString(key));
        key = "subject_type";                       getSubjectInfo.put(key,objSubBaseCommon.getString(key));
        key = "subject_create_time";                getSubjectInfo.put(key,objSubBaseCommon.getString(key));


        key = "subject_qualification_category";             getSubjectInfo.put(key,objSubBaseQQ.getString(key));
        key = "subject_market_roles_types";                 getSubjectInfo.put(key,objSubBaseQQ.getString(key));
        key = "subject_financial_qualification_types";      getSubjectInfo.put(key,objSubBaseQQ.getString(key));

        key = "subject_role_qualification_number";          getSubjectInfo.put(key,objSubBaseQQ1.getString(key));
        key = "subject_role_qualification_certification_document";getSubjectInfo.put(key, com.alibaba.fastjson.JSONObject.parseArray(
                objSubBaseQQ1.getJSONArray(key).toJSONString(), String.class));
        key = "subject_qualification_party_certification";  getSubjectInfo.put(key,objSubBaseQQ1.getString(key));
        key = "subject_role_qualification_reviewer";        getSubjectInfo.put(key,objSubBaseQQ1.getString(key));
        key = "subject_certification_time";                 getSubjectInfo.put(key,objSubBaseQQ1.getString(key));
        key = "subject_audit_time";                         getSubjectInfo.put(key,objSubBaseQQ1.getString(key));

//        key = "subject_qualification_information";   getSubjectInfo.put(key, com.alibaba.fastjson.JSONObject.parseArray(
//                objSubBase.getJSONArray(key).toJSONString(), Map.class));

        key = "subject_investor_name";              getSubjectInfo.put(key,objPersonSubBase.getString(key));
        key = "subject_id_doc_type";                getSubjectInfo.put(key,objPersonSubBase.getString(key));
        key = "subject_id_doc_number";              getSubjectInfo.put(key,objPersonSubBase.getString(key));
        key = "subject_id_address";                 getSubjectInfo.put(key,objPersonSubBase.getString(key));
        key = "subject_contact_address";            getSubjectInfo.put(key,objPersonSubBase.getString(key));
        key = "subject_investor_contact_number";    getSubjectInfo.put(key,objPersonSubBase.getString(key));
        key = "subject_cellphone_number";           getSubjectInfo.put(key,objPersonSubBase.getString(key));
        key = "subject_personal_fax_number";        getSubjectInfo.put(key,objPersonSubBase.getString(key));
        key = "subject_postalcode_number";          getSubjectInfo.put(key,objPersonSubBase.getString(key));
        key = "subject_id_doc_mailbox";             getSubjectInfo.put(key,objPersonSubBase.getString(key));
        key = "subject_education";                  getSubjectInfo.put(key,objPersonSubBase.getString(key));
        key = "subject_industry";                   getSubjectInfo.put(key,objPersonSubBase.getString(key));
        key = "subject_birthday";                   getSubjectInfo.put(key,objPersonSubBase.getString(key));
        key = "subject_gender";                     getSubjectInfo.put(key,objPersonSubBase.getString(key));
        key = "subject_work_unit";                  getSubjectInfo.put(key,objPersonSubBase.getString(key));
        key = "subject_Investment_period";          getSubjectInfo.put(key,objPersonSubBase.getString(key));
        key = "subject_Investment_experience";      getSubjectInfo.put(key,objPersonSubBase.getString(key));
        key = "subject_native_place";               getSubjectInfo.put(key,objPersonSubBase.getString(key));
        key = "subject_mail_box";                   getSubjectInfo.put(key,objPersonSubBase.getString(key));
        key = "subject_province";                   getSubjectInfo.put(key,objPersonSubBase.getString(key));
        key = "subject_city";                       getSubjectInfo.put(key,objPersonSubBase.getString(key));

        key = "subject_rating_results";             getSubjectInfo.put(key,objPersonRate.getString(key));
        key = "subject_rating_time";                getSubjectInfo.put(key,objPersonRate.getString(key));
        key = "subject_rating_record";              getSubjectInfo.put(key,objPersonRate.getString(key));

        return getSubjectInfo;
    }

    public Map accountInfo(com.alibaba.fastjson.JSONObject jobjOK){
        log.info("++++++++++++++++++++++" + jobjOK.toString());
        com.alibaba.fastjson.JSONObject objInfo = jobjOK.getJSONObject("body").getJSONObject("account_object_information");

        com.alibaba.fastjson.JSONObject objAccbase = jobjOK.getJSONObject("body").getJSONObject("account_information").getJSONObject("basic_account_information");

        com.alibaba.fastjson.JSONObject objAccQaul = jobjOK.getJSONObject("body").getJSONObject("account_information").getJSONObject("account_qualificat_information");

        com.alibaba.fastjson.JSONObject objAccLife = jobjOK.getJSONObject("body").getJSONObject("account_information").getJSONObject("account_cycle_information");
        com.alibaba.fastjson.JSONObject objAccLifeOpen = objAccLife.getJSONObject("account_opening_information");
        com.alibaba.fastjson.JSONObject objAccLifeCancel = objAccLife.getJSONObject("account_cancellation_information");
        com.alibaba.fastjson.JSONObject objAccLifeFreeze = objAccLife.getJSONObject("freeze_information");
        com.alibaba.fastjson.JSONObject objAccLifeUnfreeze = objAccLife.getJSONObject("unfreezing_information");

//        com.alibaba.fastjson.JSONObject objAccRela = jobjOK.getJSONObject("body").getJSONObject("account_information").getJSONObject("account_association_info");
        com.alibaba.fastjson.JSONObject objAccRela = jobjOK.getJSONObject("body").getJSONObject("account_information").getJSONObject("account_related_information");
        Map getSubjectInfo = new HashMap();
        key = "account_object_id";                  getSubjectInfo.put(key,objInfo.getString(key));
        key = "account_object_information_type";    getSubjectInfo.put(key,objInfo.getString(key));

        key = "account_holder_subject_ref";         getSubjectInfo.put(key,objAccbase.getString(key));
        key = "account_depository_subject_ref";     getSubjectInfo.put(key,objAccbase.getString(key));
        key = "account_number";                     getSubjectInfo.put(key,objAccbase.getString(key));
        key = "account_type";                       getSubjectInfo.put(key,objAccbase.getString(key));
        key = "account_never";                      getSubjectInfo.put(key,objAccbase.getString(key));
        key = "account_status";                     getSubjectInfo.put(key,objAccbase.getString(key));

        key = "account_qualification_certification_file";   getSubjectInfo.put(key,objAccQaul.getString(key));
        key = "account_certifier";                          getSubjectInfo.put(key,objAccQaul.getString(key));
        key = "account_auditor";                            getSubjectInfo.put(key,objAccQaul.getString(key));
        key = "account_certification_time";                 getSubjectInfo.put(key,objAccQaul.getString(key));
        key = "account_audit_time";                         getSubjectInfo.put(key,objAccQaul.getString(key));

        key = "account_opening_date";               getSubjectInfo.put(key,objAccLifeOpen.getString(key));
        key = "account_opening_certificate";        getSubjectInfo.put(key,com.alibaba.fastjson.JSONObject.parseArray(objAccLifeOpen.getJSONArray(key).toJSONString(), String.class));
        key = "account_closing_date";               getSubjectInfo.put(key,objAccLifeCancel.getString(key));
        key = "account_closing_certificate";        getSubjectInfo.put(key,com.alibaba.fastjson.JSONObject.parseArray(objAccLifeCancel.getJSONArray(key).toJSONString(), String.class));
        key = "account_forzen_date";                getSubjectInfo.put(key,objAccLifeFreeze.getString(key));
        key = "account_forzen_certificate";         getSubjectInfo.put(key,com.alibaba.fastjson.JSONObject.parseArray(objAccLifeFreeze.getJSONArray(key).toJSONString(), String.class));
        key = "account_thaw_date";                  getSubjectInfo.put(key,objAccLifeUnfreeze.getString(key));
        key = "account_thaw_certificate";           getSubjectInfo.put(key,com.alibaba.fastjson.JSONObject.parseArray(objAccLifeUnfreeze.getJSONArray(key).toJSONString(), String.class));

        key = "account_association";                    getSubjectInfo.put(key,objAccRela.getString(key));
        key = "account_associated_account_ref";         getSubjectInfo.put(key,objAccRela.getString(key));
        key = "account_associated_acct_certificates";   getSubjectInfo.put(key,objAccRela.getString(key));


        return getSubjectInfo;
    }

    public Map productInfo(com.alibaba.fastjson.JSONObject jobj2,String ProductType){
        //对象标识对象
        com.alibaba.fastjson.JSONObject objInfo = jobj2.getJSONObject("body").getJSONObject("product_object_identification");

        //产品信息对象
        com.alibaba.fastjson.JSONObject objProd = jobj2.getJSONObject("body").getJSONObject("product_information");
        com.alibaba.fastjson.JSONObject objBase = objProd.getJSONObject("basic_information");//产品信息 基本信息对象
        com.alibaba.fastjson.JSONObject objProdBase = objBase.getJSONObject("essential_information");//产品信息 基本信息 产品基本信息对象
        com.alibaba.fastjson.JSONObject objProdService = objBase.getJSONObject("service_provider_information");//产品信息 基本信息 服务方信息对象

        com.alibaba.fastjson.JSONObject objProdSub = objProd.getJSONObject("product_subject_information");//产品信息 产品标的信息对象

        com.alibaba.fastjson.JSONObject objProdIssue = objProd.getJSONObject("release_information");//产品信息 发行信息对象
        com.alibaba.fastjson.JSONObject objProdIssueEquity = objProdIssue.getJSONObject("equity_issue_information");//产品信息 发行信息 私募股权对象
        com.alibaba.fastjson.JSONObject objProdIssueBond = objProdIssue.getJSONObject("private_convertible_bonds");//产品信息 发行信息 私募可转债对象
        com.alibaba.fastjson.JSONObject objProdIssueFund = objProdIssue.getJSONObject("private_offering_fund");//产品信息 发行信息 私募基金对象

        com.alibaba.fastjson.JSONObject objProdTran = objProd.getJSONObject("transaction_information");//产品信息 交易信息对象

        Map getSubjectInfo = new HashMap();

        key = "product_object_id";                  getSubjectInfo.put(key,objInfo.getString(key));
        key = "product_object_information_type";    getSubjectInfo.put(key,objInfo.getString(key));

        key = "product_trading_market_category";    getSubjectInfo.put(key,objProdBase.getString(key));
        key = "product_market_subject";             getSubjectInfo.put(key,objProdBase.getString(key));
        key = "product_market_subject_name";        getSubjectInfo.put(key,objProdBase.getString(key));
        key = "product_plate_trading_name";         getSubjectInfo.put(key,objProdBase.getString(key));
        key = "product_issuer_subject_ref";         getSubjectInfo.put(key,objProdBase.getString(key));
        key = "product_issuer_name";                getSubjectInfo.put(key,objProdBase.getString(key));
        key = "product_code";                       getSubjectInfo.put(key,objProdBase.getString(key));
        key = "product_name";                       getSubjectInfo.put(key,objProdBase.getString(key));
        key = "product_abbreviation";               getSubjectInfo.put(key,objProdBase.getString(key));
        key = "product_use";                        getSubjectInfo.put(key,objProdBase.getString(key));
        key = "product_type";                       getSubjectInfo.put(key,objProdBase.getString(key));
        key = "product_term";                       getSubjectInfo.put(key,objProdBase.getString(key));
        key = "product_info_disclosure_way";        getSubjectInfo.put(key,objProdBase.getString(key));
        key = "product_scale_unit";                 getSubjectInfo.put(key,objProdBase.getString(key));
        key = "product_scale_currency";             getSubjectInfo.put(key,objProdBase.getString(key));
        key = "product_scale";                      getSubjectInfo.put(key,objProdBase.getString(key));
        key = "product_customer_browsing_right";    getSubjectInfo.put(key,objProdBase.getString(key));
        key = "product_customer_trading_right";     getSubjectInfo.put(key,objProdBase.getString(key));
        key = "product_issuer_contact_person";      getSubjectInfo.put(key,objProdBase.getString(key));
        key = "product_issuer_contact_info";        getSubjectInfo.put(key,objProdBase.getString(key));

        key = "product_registry_subject_ref";       getSubjectInfo.put(key,objProdService.getString(key));
        key = "product_name_registration_body";     getSubjectInfo.put(key,objProdService.getString(key));
        key = "product_trustee_subject_ref";        getSubjectInfo.put(key,objProdService.getString(key));
        key = "product_name_trustee";               getSubjectInfo.put(key,objProdService.getString(key));
        key = "product_underwriter_subject_ref";    getSubjectInfo.put(key,objProdService.getString(key));
        key = "product_underwriter_name";           getSubjectInfo.put(key,objProdService.getString(key));
        key = "product_sponsor_subject";            getSubjectInfo.put(key,objProdService.getString(key));
        key = "product_sponsor_subject_name";       getSubjectInfo.put(key,objProdService.getString(key));
        key = "product_fund_administrator_account"; getSubjectInfo.put(key,objProdService.getString(key));
        key = "product_fund_administrator_name";    getSubjectInfo.put(key,objProdService.getString(key));
        key = "product_guarantor_subject_ref";      getSubjectInfo.put(key,objProdService.getString(key));
        key = "product_guarantor_name";             getSubjectInfo.put(key,objProdService.getString(key));
        key = "product_law_firm_subject_ref";       getSubjectInfo.put(key,objProdService.getString(key));
        key = "product_law_firm_name";              getSubjectInfo.put(key,objProdService.getString(key));
        key = "product_accounting_firm_subject_ref";getSubjectInfo.put(key,objProdService.getString(key));
        key = "product_accounting_firm_name";       getSubjectInfo.put(key,objProdService.getString(key));
        key = "product_credit_enhancement_agency_subject_ref";  getSubjectInfo.put(key,objProdService.getString(key));
        key = "product_credit_enhancement_agency_name";         getSubjectInfo.put(key,objProdService.getString(key));
        key = "product_credit_rating_agency_subject_ref";       getSubjectInfo.put(key,objProdService.getString(key));
        key = "product_credit_rating_agency_name";              getSubjectInfo.put(key,objProdService.getString(key));

        key = "product_file_information";getSubjectInfo.put(key,
                com.alibaba.fastjson.JSONObject.parseArray(objBase.getJSONArray(key).toJSONString(), String.class));
//        key = "product_issue_doc_num";getSubjectInfo.put(key,jobj.getString(key));
//        key = "product_file_name";getSubjectInfo.put(key,jobj.getString(key));
//        key = "product_issue_doc";getSubjectInfo.put(key,jobj.getString(key));

        key = "product_registration_information";getSubjectInfo.put(key,
                com.alibaba.fastjson.JSONObject.parseArray(objBase.getJSONArray(key).toJSONString(), String.class));
//        key = "product_license_type";getSubjectInfo.put(key,jobj.getString(key));
//        key = "product_license_number";getSubjectInfo.put(key,jobj.getString(key));
//        key = "product_license_file_name";getSubjectInfo.put(key,jobj.getString(key));
//        key = "product_license_documents";getSubjectInfo.put(key,jobj.getString(key));

        key = "product_fund_type";              getSubjectInfo.put(key,objProdSub.getString(key));
        key = "product_description_fund_use";   getSubjectInfo.put(key,objProdSub.getString(key));

        key = "business_information";getSubjectInfo.put(key,
                com.alibaba.fastjson.JSONObject.parseArray(objProdSub.getJSONArray(key).toJSONString(), String.class));
//        key = "product_business_purpose_name";getSubjectInfo.put(key,jobj.getString(key));
//        key = "product_business_purpose_details";getSubjectInfo.put(key,jobj.getString(key));
//        key = "product_documents_list";getSubjectInfo.put(key,jobj.getString(key));

        key = "portfolio_information";getSubjectInfo.put(key,
                com.alibaba.fastjson.JSONObject.parseArray(objProdSub.getJSONArray(key).toJSONString(), String.class));
//        key = "product_investment_type";getSubjectInfo.put(key,jobj.getString(key));
//        key = "product_investment_proportion_range";getSubjectInfo.put(key,jobj.getString(key));
//        key = "product_investment_details";getSubjectInfo.put(key,jobj.getString(key));
//        key = "investment_documents_list";getSubjectInfo.put(key,jobj.getString(key));

        //私募股权 既不是2也不是3则默认私募
        if(!(ProductType.equals("2") || ProductType.equals("3"))) {
            key = "product_issue_price";                    getSubjectInfo.put(key, objProdIssueEquity.getString(key));
            key = "product_issue_price_method";             getSubjectInfo.put(key, objProdIssueEquity.getString(key));
            key = "product_before_authorized_shares";       getSubjectInfo.put(key, objProdIssueEquity.getString(key));
            key = "product_after_authorized_shares";        getSubjectInfo.put(key, objProdIssueEquity.getString(key));
            key = "product_after_issue_market_value";       getSubjectInfo.put(key, objProdIssueEquity.getString(key));
            key = "product_net_profit";                     getSubjectInfo.put(key, objProdIssueEquity.getString(key));
            key = "product_annual_net_profit";              getSubjectInfo.put(key, objProdIssueEquity.getString(key));
            key = "product_number_of_directional_issuers";  getSubjectInfo.put(key, objProdIssueEquity.getString(key));
            key = "product_issue_start_date";               getSubjectInfo.put(key, objProdIssueEquity.getString(key));
            key = "product_issue_end_date";                 getSubjectInfo.put(key, objProdIssueEquity.getString(key));
            key = "product_registration_date";              getSubjectInfo.put(key, objProdIssueEquity.getString(key));
            key = "product_unlimited_sales_number_shares";  getSubjectInfo.put(key, objProdIssueEquity.getString(key));
            key = "restricted_shares_number";               getSubjectInfo.put(key, objProdIssueEquity.getString(key));
        }
        else if(ProductType.equals("2")) {

            //私募可转债
            key = "product_duration";                       getSubjectInfo.put(key, objProdIssueBond.getString(key));
            key = "product_min_account_num";                getSubjectInfo.put(key, objProdIssueBond.getString(key));
            key = "product_face_value";                     getSubjectInfo.put(key, objProdIssueBond.getString(key));
            key = "product_conversion_right";               getSubjectInfo.put(key, objProdIssueBond.getString(key));
            key = "product_conversion_price";               getSubjectInfo.put(key, objProdIssueBond.getString(key));
            key = "product_conversion_price_remark";        getSubjectInfo.put(key, objProdIssueBond.getString(key));
            key = "product_adjustment_change_control";      getSubjectInfo.put(key, objProdIssueBond.getString(key));
            key = "product_conversion_ratio";               getSubjectInfo.put(key, objProdIssueBond.getString(key));
            key = "product_conversion_period";              getSubjectInfo.put(key, objProdIssueBond.getString(key));
            key = "product_conversion_share_ranking";       getSubjectInfo.put(key, objProdIssueBond.getString(key));
            key = "product_redemption";                     getSubjectInfo.put(key, objProdIssueBond.getString(key));
            key = "product_conversion_premium";             getSubjectInfo.put(key, objProdIssueBond.getString(key));
            key = "product_conversion_price_ref";           getSubjectInfo.put(key, objProdIssueBond.getString(key));
            key = "product_coupon_rate";                    getSubjectInfo.put(key, objProdIssueBond.getString(key));
            key = "product_lnterest_rate_form";             getSubjectInfo.put(key, objProdIssueBond.getString(key));
            key = "product_interest_payment_method";        getSubjectInfo.put(key, objProdIssueBond.getString(key));
            key = "product_interest_type";                  getSubjectInfo.put(key, objProdIssueBond.getString(key));
            key = "product_Interest_payment_frequency";     getSubjectInfo.put(key, objProdIssueBond.getString(key));
            key = "product_nonleap_year_interest_bearing_days";         getSubjectInfo.put(key, objProdIssueBond.getString(key));
            key = "product_leap_year_interest_bearing_days";            getSubjectInfo.put(key, objProdIssueBond.getString(key));
            key = "product_issue_price";                                getSubjectInfo.put(key, objProdIssueBond.getString(key));
            key = "product_option_clause";                              getSubjectInfo.put(key, objProdIssueBond.getString(key));
            key = "product_issue_scale_up";                             getSubjectInfo.put(key, objProdIssueBond.getString(key));
            key = "product_issue_scale_low";                            getSubjectInfo.put(key, objProdIssueBond.getString(key));
            key = "product_issue_start_date";                           getSubjectInfo.put(key, objProdIssueBond.getString(key));
            key = "product_issue_end_date";                             getSubjectInfo.put(key, objProdIssueBond.getString(key));
            key = "product_registration_date";                          getSubjectInfo.put(key, objProdIssueBond.getString(key));
            key = "product_value_date";                                 getSubjectInfo.put(key, objProdIssueBond.getString(key));
            key = "product_due_date";                                   getSubjectInfo.put(key, objProdIssueBond.getString(key));
            key = "product_first_interest_payment_date";                getSubjectInfo.put(key, objProdIssueBond.getString(key));
            key = "product_issuer_credit_rating";                       getSubjectInfo.put(key, objProdIssueBond.getString(key));
            key = "product_credit_enhancement_agency_credit_rating";    getSubjectInfo.put(key, objProdIssueBond.getString(key));
            key = "product_guarantee_arrangement";                      getSubjectInfo.put(key, objProdIssueBond.getString(key));
            key = "product_lockup";                                     getSubjectInfo.put(key, objProdIssueBond.getString(key));
            key = "product_termination_conditions";                     getSubjectInfo.put(key, objProdIssueBond.getString(key));
        }
        else if(ProductType.equals("3")) {
            //私募基金
            key = "product_fund_type";            getSubjectInfo.put(key, objProdIssueFund.getString(key));
            key = "product_foundation_time";      getSubjectInfo.put(key, objProdIssueFund.getString(key));
            key = "product_fund_legal_form";      getSubjectInfo.put(key, objProdIssueFund.getString(key));
            key = "product_fund_fairvalue";       getSubjectInfo.put(key, objProdIssueFund.getString(key));
            key = "product_starting_time_raising";getSubjectInfo.put(key, objProdIssueFund.getString(key));
            key = "product_closing_time_raising"; getSubjectInfo.put(key, objProdIssueFund.getString(key));
        }

        key = "product_listing_code";         getSubjectInfo.put(key, objProdTran.getString(key));
        key = "product_listing_time";         getSubjectInfo.put(key, objProdTran.getString(key));
        key = "product_listing_status";       getSubjectInfo.put(key, objProdTran.getString(key));
        key = "product_listing_remarks";      getSubjectInfo.put(key, objProdTran.getString(key));
        key = "product_delisting_time";       getSubjectInfo.put(key, objProdTran.getString(key));
        key = "product_delisting_reason";     getSubjectInfo.put(key, objProdTran.getString(key));

        return getSubjectInfo;
    }

    public Map transInfo(com.alibaba.fastjson.JSONObject jobjOK){
        com.alibaba.fastjson.JSONObject objInfo = jobjOK.getJSONObject("body").getJSONObject("transaction_object_information");

        com.alibaba.fastjson.JSONObject objBase = jobjOK.getJSONObject("body").getJSONObject("transaction_report_information").getJSONObject("basic_transaction_information");
        com.alibaba.fastjson.JSONObject objDeal = jobjOK.getJSONObject("body").getJSONObject("transaction_report_information").getJSONObject("transaction_information");
        com.alibaba.fastjson.JSONObject objDealContent = objDeal.getJSONObject("transaction_content_information");
        com.alibaba.fastjson.JSONObject objDealFina = objDeal.getJSONObject("financing_transaction_party_information");
        com.alibaba.fastjson.JSONObject objDealVerify = objDeal.getJSONObject("transaction_verification_information");

        Map getSubjectInfo = new HashMap();
        key = "transaction_object_id";                      getSubjectInfo.put(key,objInfo.getString(key));
        key = "transaction_object_information_type";        getSubjectInfo.put(key,objInfo.getString(key));


        key = "transaction_traded_product_ref";             getSubjectInfo.put(key,objBase.getString(key));
        key = "transaction_product_name";                   getSubjectInfo.put(key,objBase.getString(key));
        key = "transaction_type";                           getSubjectInfo.put(key,objBase.getString(key));
        key = "transaction_description";                    getSubjectInfo.put(key,objBase.getString(key));

        key = "transaction_serial_num";                     getSubjectInfo.put(key,objDealContent.getString(key));
        key = "transaction_close_method";                   getSubjectInfo.put(key,objDealContent.getString(key));
        key = "transaction_close_currency";                 getSubjectInfo.put(key,objDealContent.getString(key));
        key = "transaction_close_price";                    getSubjectInfo.put(key,objDealContent.getString(key));
        key = "transaction_close_amount";                   getSubjectInfo.put(key,objDealContent.getString(key));
        key = "transaction_close_time";                     getSubjectInfo.put(key,objDealContent.getString(key));
        key = "transaction_close_description";              getSubjectInfo.put(key,objDealContent.getString(key));

        key = "transaction_Issuer_principal_subject_ref";   getSubjectInfo.put(key,objDealFina.getString(key));
        key = "transaction_issuer_name";                    getSubjectInfo.put(key,objDealFina.getString(key));
        key = "transaction_Investor_subject_ref";           getSubjectInfo.put(key,objDealFina.getString(key));
        key = "transaction_Investor_name";                  getSubjectInfo.put(key,objDealFina.getString(key));

        key = "transaction_original_owner_subject_ref";     getSubjectInfo.put(key,objDealVerify.getString(key));
        key = "transaction_original_owner_name";            getSubjectInfo.put(key,objDealVerify.getString(key));
        key = "transaction_counterparty_subject_ref";       getSubjectInfo.put(key,objDealVerify.getString(key));
        key = "transaction_counterparty_name";              getSubjectInfo.put(key,objDealVerify.getString(key));

        key = "transaction_order_verification_certificates"; getSubjectInfo.put(key,com.alibaba.fastjson.JSONObject.parseArray(
                objDeal.getJSONObject("transaction_verification_information").getJSONArray(key).toJSONString(), String.class));
        key = "transaction_close_verification_certificates"; getSubjectInfo.put(key,com.alibaba.fastjson.JSONObject.parseArray(
                objDeal.getJSONObject("transaction_verification_information").getJSONArray(key).toJSONString(), String.class));

        key = "transaction_intermediary_information";
        getSubjectInfo.put(key,com.alibaba.fastjson.JSONObject.parseArray(
                jobjOK.getJSONObject("body").getJSONObject("transaction_report_information").getJSONArray(key).toJSONString(), Map.class));

        return getSubjectInfo;
    }


    public Map regiInfo(com.alibaba.fastjson.JSONObject jobjOK){
        com.alibaba.fastjson.JSONObject objInfo = jobjOK.getJSONObject("body").getJSONObject("register_object_information");
        com.alibaba.fastjson.JSONObject objRegRig = jobjOK.getJSONObject("body").getJSONObject("registration_information").getJSONObject("registration_rights");
        com.alibaba.fastjson.JSONObject objRegRigBase = objRegRig.getJSONObject("basic_information_rights");

        com.alibaba.fastjson.JSONObject objRefList = jobjOK.getJSONObject("body").getJSONObject("registration_information").getJSONObject("roll_records");
        com.alibaba.fastjson.JSONObject objRollBase = objRefList.getJSONObject("basic_information_roster");
        com.alibaba.fastjson.JSONObject objRollSH = objRefList.getJSONObject("register_shareholders");
        com.alibaba.fastjson.JSONObject objRollCred = objRefList.getJSONObject("register_creditors");

        Map getSubjectInfo = new HashMap();
        key = "register_registration_object_id";        getSubjectInfo.put(key,objInfo.getString(key));
        key = "register_object_information_type";       getSubjectInfo.put(key,objInfo.getString(key));
        key = "register_registration_type";             getSubjectInfo.put(key,objInfo.getString(key));


        key = "register_registration_serial_number";    getSubjectInfo.put(key,objRegRigBase.getString(key));
        key = "register_time";                          getSubjectInfo.put(key,objRegRigBase.getString(key));
        key = "register_rights_subject_ref";            getSubjectInfo.put(key,objRegRigBase.getString(key));
        key = "register_rights_subject_type";           getSubjectInfo.put(key,objRegRigBase.getString(key));
        key = "register_account_obj_id";                getSubjectInfo.put(key,objRegRigBase.getString(key));
        key = "registration_rights_type";               getSubjectInfo.put(key,objRegRigBase.getString(key));
        key = "registration_object_right";              getSubjectInfo.put(key,objRegRigBase.getString(key));
        key = "register_unit";                          getSubjectInfo.put(key,objRegRigBase.getString(key));
        key = "register_currency";                      getSubjectInfo.put(key,objRegRigBase.getString(key));
        key = "register_rights_change_amount";          getSubjectInfo.put(key,objRegRigBase.getString(key));
        key = "register_available_balance";             getSubjectInfo.put(key,objRegRigBase.getString(key));
        key = "register_available_percentage";          getSubjectInfo.put(key,objRegRigBase.getString(key));
        key = "register_rights_pledge_change_amount";   getSubjectInfo.put(key,objRegRigBase.getString(key));
        key = "register_rights_pledge_balance";         getSubjectInfo.put(key,objRegRigBase.getString(key));
        key = "register_frozen_category";               getSubjectInfo.put(key,objRegRigBase.getString(key));
        key = "register_rights_frozen_change_amount";   getSubjectInfo.put(key,objRegRigBase.getString(key));
        key = "register_rights_frozen_balance";         getSubjectInfo.put(key,objRegRigBase.getString(key));
        key = "register_freeze_deadline_time";          getSubjectInfo.put(key,objRegRigBase.getString(key));
        key = "register_holding_status";                getSubjectInfo.put(key,objRegRigBase.getString(key));
        key = "register_holding_attribute";            getSubjectInfo.put(key,objRegRigBase.getString(key));
        key = "registration_source";                    getSubjectInfo.put(key,objRegRigBase.getString(key));
        key = "register_source_type";                   getSubjectInfo.put(key,objRegRigBase.getString(key));
        key = "register_notes";                         getSubjectInfo.put(key,objRegRigBase.getString(key));
        key = "register_verification_certificates";     getSubjectInfo.put(key,com.alibaba.fastjson.JSONObject.parseArray(
                objRegRigBase.getJSONArray(key).toJSONString(), String.class));
        key = "transaction_type";                       getSubjectInfo.put(key,objRegRigBase.getString(key));
        key = "register_transaction_obj_id";            getSubjectInfo.put(key,objRegRigBase.getString(key));


        key = "register_roster_subject_ref";            getSubjectInfo.put(key,objRollBase.getString(key));
        key = "register_rights_type";                   getSubjectInfo.put(key,objRollBase.getString(key));
        key = "register_date";                          getSubjectInfo.put(key,objRollBase.getString(key));

        key = "register_shareholder_subject_ref";       getSubjectInfo.put(key,objRollSH.getString(key));
        key = "register_shareholder_subject_type";      getSubjectInfo.put(key,objRollSH.getString(key));
        key = "register_nature_of_shares";              getSubjectInfo.put(key,objRollSH.getString(key));
        key = "register_subscription_amount";           getSubjectInfo.put(key,objRollSH.getString(key));
        key = "register_paid_in_amount";                getSubjectInfo.put(key,objRollSH.getString(key));
        key = "register_shareholding_ratio";            getSubjectInfo.put(key,objRollSH.getString(key));

        key = "register_creditor_subject_ref";          getSubjectInfo.put(key,objRollCred.getString(key));
        key = "register_creditor_type";                 getSubjectInfo.put(key,objRollCred.getString(key));
        key = "register_creditor_subscription_count";   getSubjectInfo.put(key,objRollCred.getString(key));
        key = "register_creditor_paid_in_amount";       getSubjectInfo.put(key,objRollCred.getString(key));
        key = "register_creditor_contact_info";         getSubjectInfo.put(key,objRollCred.getString(key));

        return getSubjectInfo;
    }

    public Map settleInfo(com.alibaba.fastjson.JSONObject jobjOK){

        com.alibaba.fastjson.JSONObject objInfo = jobjOK.getJSONObject("body").getJSONObject("capital_object_information");
        com.alibaba.fastjson.JSONObject objBase = jobjOK.getJSONObject("body").getJSONObject("capital_settlement_information").getJSONObject("basic_information_capital_settlement");
        com.alibaba.fastjson.JSONObject objIn = jobjOK.getJSONObject("body").getJSONObject("capital_settlement_information").getJSONObject("transferee_information");
        com.alibaba.fastjson.JSONObject objOut = jobjOK.getJSONObject("body").getJSONObject("capital_settlement_information").getJSONObject("transferor_information");

        Map getSubjectInfo = new HashMap();
        key = "capita_settlement_object_id";            getSubjectInfo.put(key,objInfo.getString(key));
        key = "capita_object_information_type";         getSubjectInfo.put(key,objInfo.getString(key));


        key = "capita_clearing_house_subject_ref";      getSubjectInfo.put(key,objBase.getString(key));
        key = "capita_settlement_type";                 getSubjectInfo.put(key,objBase.getString(key));
        key = "capita_settlement_serial_num";           getSubjectInfo.put(key,objBase.getString(key));
        key = "capita_settlement_time";                 getSubjectInfo.put(key,objBase.getString(key));
        key = "capita_transaction_ref";                 getSubjectInfo.put(key,objBase.getString(key));
        key = "capita_currency";                        getSubjectInfo.put(key,objBase.getString(key));
        key = "capita_amount";                          getSubjectInfo.put(key,objBase.getString(key));
        key = "capita_notes";                           getSubjectInfo.put(key,objBase.getString(key));
        key = "capita_operation_certificates";          getSubjectInfo.put(key,objBase.getString(key));

        key = "capita_out_bank_code";                   getSubjectInfo.put(key,objOut.getString(key));
        key = "capita_out_bank_name";                   getSubjectInfo.put(key,objOut.getString(key));
        key = "capita_out_bank_number";                 getSubjectInfo.put(key,objOut.getString(key));
        key = "capita_out_account_obj_ref";             getSubjectInfo.put(key,objOut.getString(key));
        key = "capita_out_fund_account_name";           getSubjectInfo.put(key,objOut.getString(key));
        key = "capita_out_amount_before_transfer";      getSubjectInfo.put(key,objOut.getString(key));
        key = "capita_out_amount_after_transfer";       getSubjectInfo.put(key,objOut.getString(key));

        key = "capita_in_bank_code";                    getSubjectInfo.put(key,objIn.getString(key));
        key = "capita_in_bank_name";                    getSubjectInfo.put(key,objIn.getString(key));
        key = "capita_in_bank_number";                  getSubjectInfo.put(key,objIn.getString(key));
        key = "capita_in_account_obj_ref";              getSubjectInfo.put(key,objIn.getString(key));
        key = "capita_in_fund_account_name";            getSubjectInfo.put(key,objIn.getString(key));
        key = "capita_in_account_number";               getSubjectInfo.put(key,objIn.getString(key));
        key = "capita_in_amount_before_transfer";       getSubjectInfo.put(key,objIn.getString(key));
        key = "capita_in_amount_after_transfer";        getSubjectInfo.put(key,objIn.getString(key));

        return getSubjectInfo;
    }

    public Map pubInfo(com.alibaba.fastjson.JSONObject jobjOK) {
        com.alibaba.fastjson.JSONObject objInfo = jobjOK.getJSONObject("body").getJSONObject("letter_object_identification");
        com.alibaba.fastjson.JSONObject objDis = jobjOK.getJSONObject("body").getJSONObject("letter_approval_information").getJSONObject("enterprise_display_information");
        com.alibaba.fastjson.JSONObject objletter = jobjOK.getJSONObject("body").getJSONObject("letter_approval_information").getJSONObject("enterprise_letter_information");
        com.alibaba.fastjson.JSONObject objLetterBase = objletter.getJSONObject("basic_informati_letter_approval");
        com.alibaba.fastjson.JSONObject objFin = objletter.getJSONObject("financial_information");


        Map getSubjectInfo = new HashMap();
        //对象标识信息
        key = "letter_disclosure_object_id";            getSubjectInfo.put(key, objInfo.getString(key));
        key = "letter_object_information_type";         getSubjectInfo.put(key, objInfo.getString(key));

        //企业展示信息
        key = "letter_show_subject_reference";          getSubjectInfo.put(key, objDis.getString(key));
        key = "letter_show_subject_reference_ref";      getSubjectInfo.put(key, objDis.getString(key));
        key = "letter_display_code";                    getSubjectInfo.put(key, objDis.getString(key));
        key = "letter_display_content";                 getSubjectInfo.put(key, objDis.getString(key));
        key = "letter_display_main_audit_voucher";      getSubjectInfo.put(key, objDis.getString(key));
        key = "letter_show_content_audit_voucher";      getSubjectInfo.put(key, objDis.getString(key));
        key = "letter_show_start_date";                 getSubjectInfo.put(key, objDis.getString(key));
        key = "letter_show_end_date";                   getSubjectInfo.put(key, objDis.getString(key));

        //信批基本信息
        key = "letter_approval_time";                   getSubjectInfo.put(key, objLetterBase.getString(key));
        key = "letter_disclosure_subject_ref";          getSubjectInfo.put(key, objLetterBase.getString(key));
        key = "letter_disclosure_uploader_ref";         getSubjectInfo.put(key, objLetterBase.getString(key));


        //拼组诚信档案信息
        com.alibaba.fastjson.JSONArray arrCD = objletter.getJSONArray("integrity_archives");
        List<Map> tempList = new ArrayList<>();
        for (int i = 0; i < arrCD.size(); i++) {
            Map tempMap = new HashMap();
            com.alibaba.fastjson.JSONObject objTemp = com.alibaba.fastjson.JSONObject.parseObject(arrCD.get(i).toString());
            key = "letter_provider_subject_ref";        tempMap.put(key, objTemp.getJSONObject("basic_information").getString(key));
            key = "letter_provider_name";               tempMap.put(key, objTemp.getJSONObject("basic_information").getString(key));
            key = "letter_identified_party_subject_ref";tempMap.put(key, objTemp.getJSONObject("basic_information").getString(key));
            key = "letter_identified_party_name";       tempMap.put(key, objTemp.getJSONObject("basic_information").getString(key));
            key = "letter_appraiser_subject_ref";       tempMap.put(key, objTemp.getJSONObject("basic_information").getString(key));
            key = "letter_appraiser_name";              tempMap.put(key, objTemp.getJSONObject("basic_information").getString(key));

            key = "letter_item_number";                 tempMap.put(key, objTemp.getJSONObject("item_details").getString(key));
            key = "letter_item_name";                   tempMap.put(key, objTemp.getJSONObject("item_details").getString(key));
            key = "letter_item_type";                   tempMap.put(key, objTemp.getJSONObject("item_details").getString(key));
            key = "letter_item_describe";               tempMap.put(key, objTemp.getJSONObject("item_details").getString(key));
            key = "letter_term_of_validity";            tempMap.put(key, objTemp.getJSONObject("item_details").getString(key));
            key = "letter_start_time";                  tempMap.put(key, objTemp.getJSONObject("item_details").getString(key));
            key = "letter_end_time";                    tempMap.put(key, objTemp.getJSONObject("item_details").getString(key));
            key = "letter_item_state";                  tempMap.put(key, objTemp.getJSONObject("item_details").getString(key));
            key = "letter_item_file";                   tempMap.put(key, com.alibaba.fastjson.JSONObject.parseArray(
                    objTemp.getJSONObject("item_details").getJSONArray(key).toJSONString(), String.class));

            tempList.add(tempMap);
        }

        getSubjectInfo.put("integrity_archives", tempList);

        //财务信息 基本财务信息
        key = "letter_start_date";              getSubjectInfo.put(key, objFin.getJSONObject("basic_financial_information").getString(key));
        key = "letter_deadline";                getSubjectInfo.put(key, objFin.getJSONObject("basic_financial_information").getString(key));
        key = "letter_report_type";             getSubjectInfo.put(key, objFin.getJSONObject("basic_financial_information").getString(key));
        key = "letter_ending_total_asset";      getSubjectInfo.put(key, objFin.getJSONObject("basic_financial_information").getString(key));
        key = "letter_ending_net_asset";        getSubjectInfo.put(key, objFin.getJSONObject("basic_financial_information").getString(key));
        key = "letter_total_liability";         getSubjectInfo.put(key, objFin.getJSONObject("basic_financial_information").getString(key));
        key = "letter_current_operating_income";getSubjectInfo.put(key, objFin.getJSONObject("basic_financial_information").getString(key));
        key = "letter_current_total_profit";    getSubjectInfo.put(key, objFin.getJSONObject("basic_financial_information").getString(key));
        key = "letter_current_net_profit";      getSubjectInfo.put(key, objFin.getJSONObject("basic_financial_information").getString(key));
        key = "letter_cash_flow";               getSubjectInfo.put(key, objFin.getJSONObject("basic_financial_information").getString(key));
        key = "letter_whether_r&d_costs";       getSubjectInfo.put(key, objFin.getJSONObject("basic_financial_information").getString(key));
        key = "letter_r&d_costs";               getSubjectInfo.put(key, objFin.getJSONObject("basic_financial_information").getString(key));

        //财务信息 财务报表文件
        key = "letter_balance_sheet";           getSubjectInfo.put(key, objFin.getJSONObject("financial_statement_documents").getString(key));
        key = "letter_cash_flow_sheet";         getSubjectInfo.put(key, objFin.getJSONObject("financial_statement_documents").getString(key));
        key = "letter_profit_sheet";            getSubjectInfo.put(key, objFin.getJSONObject("financial_statement_documents").getString(key));


        //拼组重大事件信息
//        com.alibaba.fastjson.JSONArray objKeyEvent = objletter.getJSONArray("major_event_information");
//        List<Map> tempList2 = new ArrayList<>();
//        for(int i=0;i< objKeyEvent.size();i++){
//            Map tempMap = new HashMap();
//            com.alibaba.fastjson.JSONObject objTemp = com.alibaba.fastjson.JSONObject.parseObject(objKeyEvent.get(i).toString());
//            key = "letter_major_event_type";        tempMap.put(key, objTemp.getString(key));
//            key = "letter_file_list";               tempMap.put(key, objTemp.getString(key));
//            key = "letter_description_document";    tempMap.put(key, objTemp.getString(key));
//            key = "letter_submission_time";         tempMap.put(key, objTemp.getString(key));
//
//            tempList2.add(tempMap);
//        }
//        getSubjectInfo.put("major_event_information", tempList2);

        //拼组公告信息
//        com.alibaba.fastjson.JSONArray objNotice = objletter.getJSONArray("letter_notice");
//        List<Map> tempList3 = new ArrayList<>();
//        for(int i=0;i< objNotice.size();i++){
//            Map tempMap = new HashMap();
//            com.alibaba.fastjson.JSONObject objTemp = com.alibaba.fastjson.JSONObject.parseObject(objNotice.get(i).toString());
//            key = "letter_announcement_type";        tempMap.put(key, objTemp.getString(key));
//            key = "letter_file_list";                tempMap.put(key, objTemp.getString(key));
//            key = "letter_description_announcement"; tempMap.put(key, objTemp.getString(key));
//            key = "letter_announcement_time";        tempMap.put(key, objTemp.getString(key));
//
//            tempList3.add(tempMap);
//        }
//        getSubjectInfo.put("letter_notice",tempList3);
        key = "major_event_information";        getSubjectInfo.put(key,
                com.alibaba.fastjson.JSONObject.parseArray(objletter.getJSONArray(key).toJSONString(), Map.class));
        key = "letter_notice";                  getSubjectInfo.put(key,
                com.alibaba.fastjson.JSONObject.parseArray(objletter.getJSONArray(key).toJSONString(), Map.class));


        return getSubjectInfo;
    }
}