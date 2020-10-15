package com.tjfintech.common;

import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.Interface.Token;
import com.tjfintech.common.utils.FileOperation;
import com.tjfintech.common.utils.MysqlOperation;
import com.tjfintech.common.utils.Shell;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.hamcrest.CoreMatchers;
import sun.misc.BASE64Encoder;

import java.io.File;
import java.util.*;

import static com.tjfintech.common.utils.FileOperation.*;
import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassGD.*;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@Slf4j
public class GDCommonFunc {
    TestBuilder testBuilder= TestBuilder.getInstance();
    MultiSign multiSign =testBuilder.getMultiSign();
    SoloSign soloSign= testBuilder.getSoloSign();
    Token tokenModule= testBuilder.getToken();
    Store store = testBuilder.getStore();
    UtilsClass utilsClass=new UtilsClass();
    MgToolCmd mgToolCmd = new MgToolCmd();
    //获取所有地址账户与私钥密码信息
    JSONObject jsonObjectAddrPri;


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
            sleepAndSaveInfo(4000,"等待下一个块交易打块");
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
        tempReg.put("权利人账户引用",mapAccAddr.get(address));
        tempReg.put("股份性质",shareProperty);

        Map tempTxInfo = gdbf.init04TxInfo();
        tempTxInfo.put("原持有方主体引用",mapAccAddr.get(address));

        Map<String,Object> shares = new HashMap<>();
        shares.put("address",address);
        shares.put("amount",amount);
        shares.put("shareProperty",shareProperty);
        shares.put("registerInformation",tempReg);
        shares.put("transactionReport",tempTxInfo);

        List<Map> shareList = new ArrayList<>();
        shareList.add(shares);
        return shareList;
    }

    public static List<Map> gdConstructShareList(String address, double amount, int shareProperty,List<Map> list){
        GDBeforeCondition gdbf = new GDBeforeCondition();
        Map tempReg = gdbf.init05RegInfo();
        tempReg.put("权利人账户引用",mapAccAddr.get(address));
        tempReg.put("股份性质",shareProperty);

        Map tempTxInfo = gdbf.init04TxInfo();
        tempTxInfo.put("原持有方主体引用",mapAccAddr.get(address));

        List<Map> shareList = new ArrayList<>();
        for(int i = 0 ; i < list.size() ; i++) {
            shareList.add(list.get(i));
        }
        Map<String,Object> shares = new HashMap<>();
        shares.put("address",address);
        shares.put("amount",amount);
        shares.put("shareProperty",shareProperty);
        shares.put("registerInformation",tempReg);
        shares.put("transactionReport",tempTxInfo);

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
        tempReg.put("权利人账户引用",mapAccAddr.get(address));
        tempReg.put("股份性质",shareProperty);

        //处理交易
        Map tempTxInfo = gdbf.init04TxInfo();
        tempTxInfo.put("原持有方主体引用",mapAccAddr.get(address));

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


    public static String getTotalAmountFromShareList(JSONArray dataShareList)throws Exception {
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
            assertEquals("未包含检查objID：" + objId,false,true);
            return null;
        }

        com.alibaba.fastjson.JSONObject jobjOK = null;

        log.info("检查交易及登记array size:" + jsonArray2.size());
        assertEquals(checkSize,jsonArray2.size());

        log.info("获取指定存证信息");
        //获取登记存证信息 且权利人账户引用为指定的对象标识 股份性质为指定股份性质
        for(int i=0;i<jsonArray2.size();i++){
//            log.info("check index " + i);
            com.alibaba.fastjson.JSONObject objTemp = com.alibaba.fastjson.JSONObject.parseObject(jsonArray2.get(i).toString());
//            log.info(objTemp.toString());
            String type = objTemp.getJSONObject("header").getJSONObject("content").getString("type");
            log.info(type + "123");

            if( type.equals("登记") ){
                String memRef = objTemp.getJSONObject("body").getJSONObject("登记信息").getJSONObject("权利登记").getJSONObject("产品登记").getString("权利人账户引用");
                int sharepp = objTemp.getJSONObject("body").getJSONObject("登记信息").getJSONObject("名册登记").getJSONObject("股东名册").getIntValue("股份性质");
                if (memRef.equals(objId) && (sharepp == shareProperty)) {
                    jobjOK = objTemp;
                    break;
                }
            }
        }
        log.info(jobjOK.toString());
        com.alibaba.fastjson.JSONObject objRefList = jobjOK.getJSONObject("body").getJSONObject("登记信息").getJSONObject("名册登记");
        com.alibaba.fastjson.JSONObject objRegRig = jobjOK.getJSONObject("body").getJSONObject("登记信息").getJSONObject("权利登记");

        Map getSubjectInfo = new HashMap();
        getSubjectInfo.put("登记对象标识",jobjOK.getJSONObject("body").getJSONObject("对象信息").getString("登记对象标识"));
        getSubjectInfo.put("登记类型",jobjOK.getJSONObject("body").getJSONObject("对象信息").getString("登记类型"));

        getSubjectInfo.put("登记流水号",objRegRig.getJSONObject("权利基本信息").getString("登记流水号"));
        getSubjectInfo.put("登记时间",objRegRig.getJSONObject("权利基本信息").getString("登记时间"));
        getSubjectInfo.put("登记主体引用",objRegRig.getJSONObject("权利基本信息").getString("登记主体引用"));
        getSubjectInfo.put("登记主体类型",objRegRig.getJSONObject("权利基本信息").getString("登记主体类型"));
        getSubjectInfo.put("权利登记单位",objRegRig.getJSONObject("权利基本信息").getString("权利登记单位"));
        getSubjectInfo.put("登记币种",objRegRig.getJSONObject("权利基本信息").getString("登记币种"));
        getSubjectInfo.put("变动额",objRegRig.getJSONObject("权利基本信息").getString("变动额"));
        getSubjectInfo.put("当前可用余额",objRegRig.getJSONObject("权利基本信息").getString("当前可用余额"));
        getSubjectInfo.put("当前可用余额占比",objRegRig.getJSONObject("权利基本信息").getString("当前可用余额占比"));
        getSubjectInfo.put("质押变动额",objRegRig.getJSONObject("权利基本信息").getString("质押变动额"));
        getSubjectInfo.put("当前质押余额",objRegRig.getJSONObject("权利基本信息").getString("当前质押余额"));
        getSubjectInfo.put("冻结变动额",objRegRig.getJSONObject("权利基本信息").getString("冻结变动额"));
        getSubjectInfo.put("当前冻结余额",objRegRig.getJSONObject("权利基本信息").getString("当前冻结余额"));
        getSubjectInfo.put("持有状态",objRegRig.getJSONObject("权利基本信息").getString("持有状态"));
        getSubjectInfo.put("持有属性",objRegRig.getJSONObject("权利基本信息").getString("持有属性"));
        getSubjectInfo.put("来源类型",objRegRig.getJSONObject("权利基本信息").getString("来源类型"));
        getSubjectInfo.put("登记说明",objRegRig.getJSONObject("权利基本信息").getString("登记说明"));
        getSubjectInfo.put("登记核验凭证",com.alibaba.fastjson.JSONObject.parseArray(
                objRegRig.getJSONObject("权利基本信息").getJSONArray("登记核验凭证").toJSONString(), String.class));
        getSubjectInfo.put("登记产品类型",objRegRig.getJSONObject("产品登记").getString("登记产品类型"));
        getSubjectInfo.put("登记产品引用",objRegRig.getJSONObject("产品登记").getString("登记产品引用"));
        getSubjectInfo.put("权利人账户引用",objRegRig.getJSONObject("产品登记").getString("权利人账户引用"));
        getSubjectInfo.put("交易报告引用",objRegRig.getJSONObject("产品登记").getString("交易报告引用"));

        getSubjectInfo.put("名册主体引用",objRefList.getJSONObject("名册基本信息").getString("名册主体引用"));
        getSubjectInfo.put("权利类型",objRefList.getJSONObject("名册基本信息").getString("权利类型"));
        getSubjectInfo.put("登记日期",objRefList.getJSONObject("名册基本信息").getString("登记日期"));

        getSubjectInfo.put("股东主体引用",objRefList.getJSONObject("股东名册").getString("股东主体引用"));
        getSubjectInfo.put("股东主体类型",objRefList.getJSONObject("股东名册").getString("股东主体类型"));
        getSubjectInfo.put("股份性质",objRefList.getJSONObject("股东名册").getString("股份性质"));
        getSubjectInfo.put("认缴金额",objRefList.getJSONObject("股东名册").getString("认缴金额"));
        getSubjectInfo.put("实缴金额",objRefList.getJSONObject("股东名册").getString("实缴金额"));
        getSubjectInfo.put("持股比例",objRefList.getJSONObject("股东名册").getString("持股比例"));

        getSubjectInfo.put("债权人主体引用",objRefList.getJSONObject("债权人名册").getString("债权人主体引用"));
        getSubjectInfo.put("债权人类型",objRefList.getJSONObject("债权人名册").getString("债权人类型"));
        getSubjectInfo.put("认购数量",objRefList.getJSONObject("债权人名册").getString("认购数量"));
        getSubjectInfo.put("认购金额",objRefList.getJSONObject("债权人名册").getString("认购金额"));
        getSubjectInfo.put("债权人联系方式",objRefList.getJSONObject("债权人名册").getString("债权人联系方式"));

        return getSubjectInfo;
    }

    //一个登记信息
    public Map contructOneRegisterInfo(String TxId){
        com.alibaba.fastjson.JSONObject object2 = com.alibaba.fastjson.JSONObject.parseObject(store.GetTxDetail(TxId));
        String storeData2 = object2.getJSONObject("data").getJSONObject("store").getString("storeData");
        com.alibaba.fastjson.JSONObject jobjOK = null;
        //如果是单独的一个则直接使用JSONObject进行解析
        if(storeData2.startsWith("{")){
            jobjOK = com.alibaba.fastjson.JSONObject.parseObject(storeData2);
        }else {
            //storedata是个list时
            com.alibaba.fastjson.JSONArray jsonArray2 = com.alibaba.fastjson.JSONArray.parseArray(storeData2);

            for (int i = 0; i < jsonArray2.size(); i++) {
                com.alibaba.fastjson.JSONObject objTemp = com.alibaba.fastjson.JSONObject.parseObject(jsonArray2.get(i).toString());
                if (objTemp.getJSONObject("header").getJSONObject("content").getString("type").equals("登记")) {
                    jobjOK = objTemp;
                    break;
                }
            }
        }

        com.alibaba.fastjson.JSONObject objRefList = jobjOK.getJSONObject("body").getJSONObject("登记信息").getJSONObject("名册登记");
        com.alibaba.fastjson.JSONObject objRegRig = jobjOK.getJSONObject("body").getJSONObject("登记信息").getJSONObject("权利登记");

        Map getSubjectInfo = new HashMap();
        getSubjectInfo.put("登记对象标识",jobjOK.getJSONObject("body").getJSONObject("对象信息").getString("登记对象标识"));
        getSubjectInfo.put("登记类型",jobjOK.getJSONObject("body").getJSONObject("对象信息").getString("登记类型"));

        getSubjectInfo.put("登记流水号",objRegRig.getJSONObject("权利基本信息").getString("登记流水号"));
        getSubjectInfo.put("登记时间",objRegRig.getJSONObject("权利基本信息").getString("登记时间"));
        getSubjectInfo.put("登记主体引用",objRegRig.getJSONObject("权利基本信息").getString("登记主体引用"));
        getSubjectInfo.put("登记主体类型",objRegRig.getJSONObject("权利基本信息").getString("登记主体类型"));
        getSubjectInfo.put("权利登记单位",objRegRig.getJSONObject("权利基本信息").getString("权利登记单位"));
        getSubjectInfo.put("登记币种",objRegRig.getJSONObject("权利基本信息").getString("登记币种"));
        getSubjectInfo.put("变动额",objRegRig.getJSONObject("权利基本信息").getString("变动额"));
        getSubjectInfo.put("当前可用余额",objRegRig.getJSONObject("权利基本信息").getString("当前可用余额"));
        getSubjectInfo.put("当前可用余额占比",objRegRig.getJSONObject("权利基本信息").getString("当前可用余额占比"));
        getSubjectInfo.put("质押变动额",objRegRig.getJSONObject("权利基本信息").getString("质押变动额"));
        getSubjectInfo.put("当前质押余额",objRegRig.getJSONObject("权利基本信息").getString("当前质押余额"));
        getSubjectInfo.put("冻结变动额",objRegRig.getJSONObject("权利基本信息").getString("冻结变动额"));
        getSubjectInfo.put("当前冻结余额",objRegRig.getJSONObject("权利基本信息").getString("当前冻结余额"));
        getSubjectInfo.put("持有状态",objRegRig.getJSONObject("权利基本信息").getString("持有状态"));
        getSubjectInfo.put("持有属性",objRegRig.getJSONObject("权利基本信息").getString("持有属性"));
        getSubjectInfo.put("来源类型",objRegRig.getJSONObject("权利基本信息").getString("来源类型"));
        getSubjectInfo.put("登记说明",objRegRig.getJSONObject("权利基本信息").getString("登记说明"));
        getSubjectInfo.put("登记核验凭证",com.alibaba.fastjson.JSONObject.parseArray(
                objRegRig.getJSONObject("权利基本信息").getJSONArray("登记核验凭证").toJSONString(), String.class));
        getSubjectInfo.put("登记产品类型",objRegRig.getJSONObject("产品登记").getString("登记产品类型"));
        getSubjectInfo.put("登记产品引用",objRegRig.getJSONObject("产品登记").getString("登记产品引用"));
        getSubjectInfo.put("权利人账户引用",objRegRig.getJSONObject("产品登记").getString("权利人账户引用"));
        getSubjectInfo.put("交易报告引用",objRegRig.getJSONObject("产品登记").getString("交易报告引用"));

        getSubjectInfo.put("名册主体引用",objRefList.getJSONObject("名册基本信息").getString("名册主体引用"));
        getSubjectInfo.put("权利类型",objRefList.getJSONObject("名册基本信息").getString("权利类型"));
        getSubjectInfo.put("登记日期",objRefList.getJSONObject("名册基本信息").getString("登记日期"));

        getSubjectInfo.put("股东主体引用",objRefList.getJSONObject("股东名册").getString("股东主体引用"));
        getSubjectInfo.put("股东主体类型",objRefList.getJSONObject("股东名册").getString("股东主体类型"));
        getSubjectInfo.put("股份性质",objRefList.getJSONObject("股东名册").getString("股份性质"));
        getSubjectInfo.put("认缴金额",objRefList.getJSONObject("股东名册").getString("认缴金额"));
        getSubjectInfo.put("实缴金额",objRefList.getJSONObject("股东名册").getString("实缴金额"));
        getSubjectInfo.put("持股比例",objRefList.getJSONObject("股东名册").getString("持股比例"));

        getSubjectInfo.put("债权人主体引用",objRefList.getJSONObject("债权人名册").getString("债权人主体引用"));
        getSubjectInfo.put("债权人类型",objRefList.getJSONObject("债权人名册").getString("债权人类型"));
        getSubjectInfo.put("认购数量",objRefList.getJSONObject("债权人名册").getString("认购数量"));
        getSubjectInfo.put("认购金额",objRefList.getJSONObject("债权人名册").getString("认购金额"));
        getSubjectInfo.put("债权人联系方式",objRefList.getJSONObject("债权人名册").getString("债权人联系方式"));

        return getSubjectInfo;
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
                    objTemp.getJSONObject("body").getJSONObject("交易报告信息").getJSONObject("交易成交信息").getJSONObject("交易成交方信息").getString("原持有方主体引用").equals(objId)){
                jobjOK = objTemp;
                break;
            }
        }
        log.info(jobjOK.toString());
        com.alibaba.fastjson.JSONObject objBase = jobjOK.getJSONObject("body").getJSONObject("交易报告信息").getJSONObject("交易基本信息");
        com.alibaba.fastjson.JSONObject objDeal = jobjOK.getJSONObject("body").getJSONObject("交易报告信息").getJSONObject("交易成交信息");

        Map getSubjectInfo = new HashMap();
        getSubjectInfo.put("交易对象标识",jobjOK.getJSONObject("body").getJSONObject("对象信息").getString("交易对象标识"));

        getSubjectInfo.put("交易产品引用",objBase.getString("交易产品引用"));
        getSubjectInfo.put("产品名称",objBase.getString("产品名称"));
        getSubjectInfo.put("交易类型",objBase.getString("交易类型"));
        getSubjectInfo.put("交易场所",objBase.getString("交易场所"));
        getSubjectInfo.put("交易描述信息",objBase.getString("交易描述信息"));

        getSubjectInfo.put("交易成交流水号",objDeal.getJSONObject("成交内容信息").getString("交易成交流水号"));
        getSubjectInfo.put("成交方式",objDeal.getJSONObject("成交内容信息").getString("成交方式"));
        getSubjectInfo.put("成交币种",objDeal.getJSONObject("成交内容信息").getString("成交币种"));
        getSubjectInfo.put("成交价格",objDeal.getJSONObject("成交内容信息").getString("成交价格"));
        getSubjectInfo.put("成交数量",objDeal.getJSONObject("成交内容信息").getString("成交数量"));
        getSubjectInfo.put("成交时间",objDeal.getJSONObject("成交内容信息").getString("成交时间"));
        getSubjectInfo.put("交易成交描述信息",objDeal.getJSONObject("成交内容信息").getString("交易成交描述信息"));

        getSubjectInfo.put("发行方主体引用",objDeal.getJSONObject("融资类交易成交方信息").getString("发行方主体引用"));
        getSubjectInfo.put("发行方名称",objDeal.getJSONObject("融资类交易成交方信息").getString("发行方名称"));
        getSubjectInfo.put("投资方主体引用",objDeal.getJSONObject("融资类交易成交方信息").getString("投资方主体引用"));
        getSubjectInfo.put("投资方名称",objDeal.getJSONObject("融资类交易成交方信息").getString("投资方名称"));

        getSubjectInfo.put("原持有方主体引用",objDeal.getJSONObject("交易成交方信息").getString("原持有方主体引用"));
        getSubjectInfo.put("原持有方名称",objDeal.getJSONObject("交易成交方信息").getString("原持有方名称"));
        getSubjectInfo.put("对手方主体引用",objDeal.getJSONObject("交易成交方信息").getString("对手方主体引用"));
        getSubjectInfo.put("对手方名称",objDeal.getJSONObject("交易成交方信息").getString("对手方名称"));

        getSubjectInfo.put("委托核验凭证",objDeal.getJSONObject("成交核验信息").getString("委托核验凭证"));
        getSubjectInfo.put("成交核验凭证",objDeal.getJSONObject("成交核验信息").getString("成交核验凭证"));

        getSubjectInfo.put("交易中介信息",com.alibaba.fastjson.JSONObject.parseArray(
                jobjOK.getJSONObject("body").getJSONObject("交易报告信息").getJSONArray("交易中介信息").toJSONString(), Map.class));

        return getSubjectInfo;
    }

    public Map contructSettleInfo(String TxId){
        com.alibaba.fastjson.JSONObject object2 = com.alibaba.fastjson.JSONObject.parseObject(store.GetTxDetail(TxId));
        String storeData2 = object2.getJSONObject("data").getJSONObject("store").getString("storeData");

        com.alibaba.fastjson.JSONObject jobjOK = null;
        //如果是单独的一个则直接使用JSONObject进行解析
        if(storeData2.startsWith("{")){
            jobjOK = com.alibaba.fastjson.JSONObject.parseObject(storeData2);
        }else {
            //storedata是个list时
            com.alibaba.fastjson.JSONArray jsonArray2 = com.alibaba.fastjson.JSONArray.parseArray(storeData2);

            for (int i = 0; i < jsonArray2.size(); i++) {
                com.alibaba.fastjson.JSONObject objTemp = com.alibaba.fastjson.JSONObject.parseObject(jsonArray2.get(i).toString());
                if (objTemp.getJSONObject("header").getJSONObject("content").getString("type").equals("资金结算")) {
                    jobjOK = objTemp;
                    break;
                }
            }
        }

        com.alibaba.fastjson.JSONObject objBase = jobjOK.getJSONObject("body").getJSONObject("资金结算信息").getJSONObject("资金结算基本信息");
        com.alibaba.fastjson.JSONObject objIn = jobjOK.getJSONObject("body").getJSONObject("资金结算信息").getJSONObject("转入方信息");
        com.alibaba.fastjson.JSONObject objOut = jobjOK.getJSONObject("body").getJSONObject("资金结算信息").getJSONObject("转出方信息");

        Map getSubjectInfo = new HashMap();
        getSubjectInfo.put("资金结算对象标识",jobjOK.getJSONObject("body").getJSONObject("对象信息").getString("资金结算对象标识"));

        getSubjectInfo.put("结算机构主体引用",objBase.getString("结算机构主体引用"));
        getSubjectInfo.put("结算类型",objBase.getString("结算类型"));
        getSubjectInfo.put("结算流水号",objBase.getString("结算流水号"));
        getSubjectInfo.put("结算时间",objBase.getString("结算时间"));
        getSubjectInfo.put("交易报告引用",objBase.getString("交易报告引用"));
        getSubjectInfo.put("结算币种",objBase.getString("结算币种"));
        getSubjectInfo.put("结算金额",objBase.getString("结算金额"));
        getSubjectInfo.put("结算说明",objBase.getString("结算说明"));
        getSubjectInfo.put("结算操作凭证",objBase.getString("结算操作凭证"));

        getSubjectInfo.put("转出方银行代号",objOut.getString("转出方银行代号"));
        getSubjectInfo.put("转出方银行名称",objOut.getString("转出方银行名称"));
        getSubjectInfo.put("转出方银行账号",objOut.getString("转出方银行账号"));
        getSubjectInfo.put("转出方账户引用",objOut.getString("转出方账户引用"));
        getSubjectInfo.put("转出方账户名称",objOut.getString("转出方账户名称"));
        getSubjectInfo.put("转出方发生前金额",objOut.getString("转出方发生前金额"));
        getSubjectInfo.put("转出方发生后余额",objOut.getString("转出方发生后余额"));

        getSubjectInfo.put("转入方银行代号",objIn.getString("转入方银行代号"));
        getSubjectInfo.put("转入方银行名称",objIn.getString("转入方银行名称"));
        getSubjectInfo.put("转入方银行账号",objIn.getString("转入方银行账号"));
        getSubjectInfo.put("转入方账户引用",objIn.getString("转入方账户引用"));
        getSubjectInfo.put("转入方账户名称",objIn.getString("转入方账户名称"));
        getSubjectInfo.put("转入方资金账号",objIn.getString("转入方资金账号"));
        getSubjectInfo.put("转入方发生前金额",objIn.getString("转入方发生前金额"));
        getSubjectInfo.put("转入方发生后余额",objIn.getString("转入方发生后余额"));

        return getSubjectInfo;
    }

    public Map contructPublishInfo(String TxId){
        com.alibaba.fastjson.JSONObject object2 = com.alibaba.fastjson.JSONObject.parseObject(store.GetTxDetail(TxId));
        String storeData2 = object2.getJSONObject("data").getJSONObject("store").getString("storeData");
        com.alibaba.fastjson.JSONObject jobjOK = null;
        //如果是单独的一个则直接使用JSONObject进行解析
        if(storeData2.startsWith("{")){
            jobjOK = com.alibaba.fastjson.JSONObject.parseObject(storeData2);
        }else {
            //storedata是个list时
            com.alibaba.fastjson.JSONArray jsonArray2 = com.alibaba.fastjson.JSONArray.parseArray(storeData2);

            for (int i = 0; i < jsonArray2.size(); i++) {
                com.alibaba.fastjson.JSONObject objTemp = com.alibaba.fastjson.JSONObject.parseObject(jsonArray2.get(i).toString());
                if (objTemp.getJSONObject("header").getJSONObject("content").getString("type").equals("信批")) {
                    jobjOK = objTemp;
                    break;
                }
            }
        }

//        com.alibaba.fastjson.JSONObject jobjOK = com.alibaba.fastjson.JSONObject.parseObject(storeData2);

        com.alibaba.fastjson.JSONObject objBase = jobjOK.getJSONObject("body").getJSONObject("信批信息").getJSONObject("信批基本信息");
        com.alibaba.fastjson.JSONObject objAccount = jobjOK.getJSONObject("body").getJSONObject("信批信息").getJSONObject("财务信息");
        com.alibaba.fastjson.JSONObject objKeyEvent = jobjOK.getJSONObject("body").getJSONObject("信批信息").getJSONObject("重大时间信息");

        Map getSubjectInfo = new HashMap();
        getSubjectInfo.put("信批对象标识",jobjOK.getJSONObject("body").getJSONObject("对象标识").getString("信批对象标识"));

        getSubjectInfo.put("信批主体引用", objBase.getString("信批主体引用"));

        getSubjectInfo.put("期间起始日期", objAccount.getJSONObject("基本财务信息").getString("期间起始日期"));
        getSubjectInfo.put("截止日期", objAccount.getJSONObject("基本财务信息").getString("截止日期"));
        getSubjectInfo.put("报表类型", objAccount.getJSONObject("基本财务信息").getString("报表类型"));
        getSubjectInfo.put("期末总资产(元)", objAccount.getJSONObject("基本财务信息").getString("期末总资产(元)"));
        getSubjectInfo.put("期末净资产(元)", objAccount.getJSONObject("基本财务信息").getString("期末净资产(元)"));
        getSubjectInfo.put("总负债(元)", objAccount.getJSONObject("基本财务信息").getString("总负债(元)"));
        getSubjectInfo.put("本期营业收入(元)", objAccount.getJSONObject("基本财务信息").getString("本期营业收入(元)"));
        getSubjectInfo.put("本期利润总额（元）", objAccount.getJSONObject("基本财务信息").getString("本期利润总额（元）"));
        getSubjectInfo.put("本期净利润（元）",objAccount.getJSONObject("基本财务信息").getString("本期净利润（元）"));
        getSubjectInfo.put("现金流量（元）",objAccount.getJSONObject("基本财务信息").getString("现金流量（元）"));
        getSubjectInfo.put("是否有研发费用",objAccount.getJSONObject("基本财务信息").getString("是否有研发费用"));
        getSubjectInfo.put("研发费用（元）",objAccount.getJSONObject("基本财务信息").getString("研发费用（元）"));

        getSubjectInfo.put("资产负债表(PDF)", objAccount.getJSONObject("财务报表文件").getString("资产负债表(PDF)"));
        getSubjectInfo.put("现金流量表(PDF)", objAccount.getJSONObject("财务报表文件").getString("现金流量表(PDF)"));
        getSubjectInfo.put("利润表(PDF)", objAccount.getJSONObject("财务报表文件").getString("利润表(PDF)"));

        getSubjectInfo.put("重大事件类型", objKeyEvent.getJSONObject("事件类型").getString("重大事件类型"));
        getSubjectInfo.put("文件列表", com.alibaba.fastjson.JSONObject.parseArray(
                objKeyEvent.getJSONObject("文件").getJSONArray("文件列表").toJSONString(), String.class));
        getSubjectInfo.put("提报时间", objKeyEvent.getJSONObject("提报时间").getString("提报时间"));

        return getSubjectInfo;
    }

    public Map contructEnterpriseSubInfo(String subTxId){
        log.info("检查的交易id " + subTxId);
        com.alibaba.fastjson.JSONObject jobj2 = null;
        com.alibaba.fastjson.JSONObject object2 = com.alibaba.fastjson.JSONObject.parseObject(store.GetTxDetail(subTxId));
        String storeData2 = object2.getJSONObject("data").getJSONObject("store").getString("storeData");
        //如果是单独的一个则直接使用JSONObject进行解析
        if(storeData2.startsWith("{")){
            jobj2 = com.alibaba.fastjson.JSONObject.parseObject(storeData2);
        }else {
        //storedata是个list时
            com.alibaba.fastjson.JSONArray jsonArray2 = com.alibaba.fastjson.JSONArray.parseArray(storeData2);

            for (int i = 0; i < jsonArray2.size(); i++) {
                com.alibaba.fastjson.JSONObject objTemp = com.alibaba.fastjson.JSONObject.parseObject(jsonArray2.get(i).toString());
                if (objTemp.getJSONObject("header").getJSONObject("content").getString("type").equals("主体")) {
                    jobj2 = objTemp;
                    break;
                }
            }
        }

        com.alibaba.fastjson.JSONObject objSubBase = jobj2.getJSONObject("body").getJSONObject("主体信息").getJSONObject("主体基本信息");
        com.alibaba.fastjson.JSONObject objEnterpriseSub = jobj2.getJSONObject("body").getJSONObject("主体信息").getJSONObject("机构主体信息");

        Map getSubjectInfo = new HashMap();
        getSubjectInfo.put("对象标识",jobj2.getJSONObject("body").getJSONObject("对象信息").getString("对象标识"));
        getSubjectInfo.put("主体标识",objSubBase.getJSONObject("主体通用信息").getString("主体标识"));
        getSubjectInfo.put("行业主体代号",objSubBase.getJSONObject("主体通用信息").getString("行业主体代号"));
        getSubjectInfo.put("主体类型",objSubBase.getJSONObject("主体通用信息").getIntValue("主体类型"));
        getSubjectInfo.put("主体信息创建时间",objSubBase.getJSONObject("主体通用信息").getString("主体信息创建时间"));
        getSubjectInfo.put("主体资质信息", com.alibaba.fastjson.JSONObject.parseArray(objSubBase.getJSONArray("主体资质信息").toJSONString(), Map.class));

        getSubjectInfo.put("机构类型",objEnterpriseSub.getJSONObject("机构分类信息").getIntValue("机构类型"));
        getSubjectInfo.put("机构性质",objEnterpriseSub.getJSONObject("机构分类信息").getIntValue("机构性质"));

        getSubjectInfo.put("公司全称",objEnterpriseSub.getJSONObject("企业基本信息").getString("公司全称"));
        getSubjectInfo.put("英文名称",objEnterpriseSub.getJSONObject("企业基本信息").getString("英文名称"));
        getSubjectInfo.put("公司简称",objEnterpriseSub.getJSONObject("企业基本信息").getString("公司简称"));
        getSubjectInfo.put("英文简称",objEnterpriseSub.getJSONObject("企业基本信息").getString("英文简称"));
        getSubjectInfo.put("企业类型",objEnterpriseSub.getJSONObject("企业基本信息").getIntValue("企业类型"));
        getSubjectInfo.put("企业成分",objEnterpriseSub.getJSONObject("企业基本信息").getIntValue("企业成分"));
        getSubjectInfo.put("统一社会信用代码",objEnterpriseSub.getJSONObject("企业基本信息").getString("统一社会信用代码"));
        getSubjectInfo.put("组织机构代码",objEnterpriseSub.getJSONObject("企业基本信息").getString("组织机构代码"));
        getSubjectInfo.put("设立日期",objEnterpriseSub.getJSONObject("企业基本信息").getString("设立日期"));
        getSubjectInfo.put("营业执照",objEnterpriseSub.getJSONObject("企业基本信息").getString("营业执照"));
        getSubjectInfo.put("经营范围",objEnterpriseSub.getJSONObject("企业基本信息").getString("经营范围"));
        getSubjectInfo.put("企业所属行业",objEnterpriseSub.getJSONObject("企业基本信息").getIntValue("企业所属行业"));
        getSubjectInfo.put("主营业务",objEnterpriseSub.getJSONObject("企业基本信息").getString("主营业务"));
        getSubjectInfo.put("公司简介",objEnterpriseSub.getJSONObject("企业基本信息").getString("公司简介"));
        getSubjectInfo.put("注册资本",objEnterpriseSub.getJSONObject("企业基本信息").getString("注册资本"));
        getSubjectInfo.put("注册资本币种",objEnterpriseSub.getJSONObject("企业基本信息").getIntValue("注册资本币种"));
        getSubjectInfo.put("实收资本",objEnterpriseSub.getJSONObject("企业基本信息").getString("实收资本"));
        getSubjectInfo.put("实收资本币种",objEnterpriseSub.getJSONObject("企业基本信息").getIntValue("实收资本币种"));
        getSubjectInfo.put("注册地址",objEnterpriseSub.getJSONObject("企业基本信息").getString("注册地址"));
        getSubjectInfo.put("办公地址",objEnterpriseSub.getJSONObject("企业基本信息").getString("办公地址"));
        getSubjectInfo.put("联系地址",objEnterpriseSub.getJSONObject("企业基本信息").getString("联系地址"));
        getSubjectInfo.put("联系电话",objEnterpriseSub.getJSONObject("企业基本信息").getString("联系电话"));
        getSubjectInfo.put("传真",objEnterpriseSub.getJSONObject("企业基本信息").getString("传真"));
        getSubjectInfo.put("邮政编码",objEnterpriseSub.getJSONObject("企业基本信息").getString("邮政编码"));
        getSubjectInfo.put("互联网地址",objEnterpriseSub.getJSONObject("企业基本信息").getString("互联网地址"));
        getSubjectInfo.put("电子邮箱",objEnterpriseSub.getJSONObject("企业基本信息").getString("电子邮箱"));
        getSubjectInfo.put("公司章程",objEnterpriseSub.getJSONObject("企业基本信息").getString("公司章程"));
        getSubjectInfo.put("主管单位",objEnterpriseSub.getJSONObject("企业基本信息").getString("主管单位"));
        getSubjectInfo.put("股东总数（个）",objEnterpriseSub.getJSONObject("企业基本信息").getIntValue("股东总数（个）"));
        getSubjectInfo.put("股本总数(股)",objEnterpriseSub.getJSONObject("企业基本信息").getString("股本总数(股)"));
        getSubjectInfo.put("法定代表人姓名",objEnterpriseSub.getJSONObject("法人信息").getString("法定代表人姓名"));
        getSubjectInfo.put("法人性质",objEnterpriseSub.getJSONObject("法人信息").getIntValue("法人性质"));
        getSubjectInfo.put("法定代表人身份证件类型",objEnterpriseSub.getJSONObject("法人信息").getIntValue("法定代表人身份证件类型"));
        getSubjectInfo.put("法定代表人身份证件号码",objEnterpriseSub.getJSONObject("法人信息").getString("法定代表人身份证件号码"));
        getSubjectInfo.put("法定代表人职务",objEnterpriseSub.getJSONObject("法人信息").getIntValue("法定代表人职务"));
        getSubjectInfo.put("法定代表人手机号",objEnterpriseSub.getJSONObject("法人信息").getString("法定代表人手机号"));

        return getSubjectInfo;
    }

    public Map contructPersonalSubInfo(String personalTxId){
        log.info("检查的交易id " + personalTxId);
        com.alibaba.fastjson.JSONObject object2 = com.alibaba.fastjson.JSONObject.parseObject(store.GetTxDetail(personalTxId));
        String storeData2 = object2.getJSONObject("data").getJSONObject("store").getString("storeData");

        com.alibaba.fastjson.JSONObject jobj2 = null;

        //如果是单独的一个则直接使用JSONObject进行解析
        if(storeData2.startsWith("{")){
            jobj2 = com.alibaba.fastjson.JSONObject.parseObject(storeData2);
        }else {
            //storedata是个list时
            com.alibaba.fastjson.JSONArray jsonArray2 = com.alibaba.fastjson.JSONArray.parseArray(storeData2);

            for (int i = 0; i < jsonArray2.size(); i++) {
                com.alibaba.fastjson.JSONObject objTemp = com.alibaba.fastjson.JSONObject.parseObject(jsonArray2.get(i).toString());
                if (objTemp.getJSONObject("header").getJSONObject("content").getString("type").equals("主体")) {
                    jobj2 = objTemp;
                    break;
                }
            }
        }

        com.alibaba.fastjson.JSONObject objSubBase = jobj2.getJSONObject("body").getJSONObject("主体信息").getJSONObject("主体基本信息");
        com.alibaba.fastjson.JSONObject objPersonalSub = jobj2.getJSONObject("body").getJSONObject("主体信息").getJSONObject("个人主体信息");

        Map getSubjectInfo = new HashMap();
        getSubjectInfo.put("对象标识",jobj2.getJSONObject("body").getJSONObject("对象信息").getString("对象标识"));

        getSubjectInfo.put("主体标识",objSubBase.getJSONObject("主体通用信息").getString("主体标识"));
        getSubjectInfo.put("行业主体代号",objSubBase.getJSONObject("主体通用信息").getString("行业主体代号"));
        getSubjectInfo.put("主体类型",objSubBase.getJSONObject("主体通用信息").getIntValue("主体类型"));
        getSubjectInfo.put("主体信息创建时间",objSubBase.getJSONObject("主体通用信息").getString("主体信息创建时间"));

        getSubjectInfo.put("主体资质信息", com.alibaba.fastjson.JSONObject.parseArray(objSubBase.getJSONArray("主体资质信息").toJSONString(), Map.class));

        getSubjectInfo.put("个人姓名",objPersonalSub.getJSONObject("个人主体基本信息").get("个人姓名"));
        getSubjectInfo.put("个人身份证类型",objPersonalSub.getJSONObject("个人主体基本信息").get("个人身份证类型"));
        getSubjectInfo.put("个人身份证件号",objPersonalSub.getJSONObject("个人主体基本信息").get("个人身份证件号"));
        getSubjectInfo.put("个人联系地址",objPersonalSub.getJSONObject("个人主体基本信息").get("个人联系地址"));
        getSubjectInfo.put("个人联系电话",objPersonalSub.getJSONObject("个人主体基本信息").get("个人联系电话"));
        getSubjectInfo.put("个人手机号",objPersonalSub.getJSONObject("个人主体基本信息").get("个人手机号"));
        getSubjectInfo.put("学历",objPersonalSub.getJSONObject("个人主体基本信息").get("学历"));
        getSubjectInfo.put("个人所属行业",objPersonalSub.getJSONObject("个人主体基本信息").get("个人所属行业"));
        getSubjectInfo.put("出生日期",objPersonalSub.getJSONObject("个人主体基本信息").get("出生日期"));
        getSubjectInfo.put("性别",objPersonalSub.getJSONObject("个人主体基本信息").get("性别"));

        getSubjectInfo.put("评级结果",objPersonalSub.getJSONObject("个人主体风险评级").get("评级结果"));
        getSubjectInfo.put("评级时间",objPersonalSub.getJSONObject("个人主体风险评级").get("评级时间"));
        getSubjectInfo.put("评级原始记录",objPersonalSub.getJSONObject("个人主体风险评级").get("评级原始记录"));

        return getSubjectInfo;
    }


    public Map getEnterpriseSubInfo(String response){
        com.alibaba.fastjson.JSONObject jobj2 = null;
        com.alibaba.fastjson.JSONObject object2 = com.alibaba.fastjson.JSONObject.parseObject(response);
        String storeData2 = object2.getString("data");//.getJSONObject("store").getString("storeData");

        //如果是单独的一个则直接使用JSONObject进行解析
        if(storeData2.startsWith("{")){
            jobj2 = com.alibaba.fastjson.JSONObject.parseObject(storeData2);
        }else {
            //storedata是个list时
            com.alibaba.fastjson.JSONArray jsonArray2 = com.alibaba.fastjson.JSONArray.parseArray(storeData2);

            for (int i = 0; i < jsonArray2.size(); i++) {
                com.alibaba.fastjson.JSONObject objTemp = com.alibaba.fastjson.JSONObject.parseObject(jsonArray2.get(i).toString());
                if (objTemp.getJSONObject("header").getJSONObject("content").getString("type").equals("主体")) {
                    jobj2 = objTemp;
                    break;
                }
            }
        }


        com.alibaba.fastjson.JSONObject objSubBase = jobj2.getJSONObject("body").getJSONObject("主体信息").getJSONObject("主体基本信息");
        com.alibaba.fastjson.JSONObject objEnterpriseSub = jobj2.getJSONObject("body").getJSONObject("主体信息").getJSONObject("机构主体信息");

        Map getSubjectInfo = new HashMap();
        getSubjectInfo.put("对象标识",jobj2.getJSONObject("body").getJSONObject("对象信息").getString("对象标识"));
        getSubjectInfo.put("主体标识",objSubBase.getJSONObject("主体通用信息").getString("主体标识"));
        getSubjectInfo.put("行业主体代号",objSubBase.getJSONObject("主体通用信息").getString("行业主体代号"));
        getSubjectInfo.put("主体类型",objSubBase.getJSONObject("主体通用信息").getIntValue("主体类型"));
        getSubjectInfo.put("主体信息创建时间",objSubBase.getJSONObject("主体通用信息").getString("主体信息创建时间"));
        getSubjectInfo.put("主体资质信息", com.alibaba.fastjson.JSONObject.parseArray(objSubBase.getJSONArray("主体资质信息").toJSONString(), Map.class));

        getSubjectInfo.put("机构类型",objEnterpriseSub.getJSONObject("机构分类信息").getIntValue("机构类型"));
        getSubjectInfo.put("机构性质",objEnterpriseSub.getJSONObject("机构分类信息").getIntValue("机构性质"));

        getSubjectInfo.put("公司全称",objEnterpriseSub.getJSONObject("企业基本信息").getString("公司全称"));
        getSubjectInfo.put("英文名称",objEnterpriseSub.getJSONObject("企业基本信息").getString("英文名称"));
        getSubjectInfo.put("公司简称",objEnterpriseSub.getJSONObject("企业基本信息").getString("公司简称"));
        getSubjectInfo.put("英文简称",objEnterpriseSub.getJSONObject("企业基本信息").getString("英文简称"));
        getSubjectInfo.put("企业类型",objEnterpriseSub.getJSONObject("企业基本信息").getIntValue("企业类型"));
        getSubjectInfo.put("企业成分",objEnterpriseSub.getJSONObject("企业基本信息").getIntValue("企业成分"));
        getSubjectInfo.put("统一社会信用代码",objEnterpriseSub.getJSONObject("企业基本信息").getString("统一社会信用代码"));
        getSubjectInfo.put("组织机构代码",objEnterpriseSub.getJSONObject("企业基本信息").getString("组织机构代码"));
        getSubjectInfo.put("设立日期",objEnterpriseSub.getJSONObject("企业基本信息").getString("设立日期"));
        getSubjectInfo.put("营业执照",objEnterpriseSub.getJSONObject("企业基本信息").getString("营业执照"));
        getSubjectInfo.put("经营范围",objEnterpriseSub.getJSONObject("企业基本信息").getString("经营范围"));
        getSubjectInfo.put("企业所属行业",objEnterpriseSub.getJSONObject("企业基本信息").getIntValue("企业所属行业"));
        getSubjectInfo.put("主营业务",objEnterpriseSub.getJSONObject("企业基本信息").getString("主营业务"));
        getSubjectInfo.put("公司简介",objEnterpriseSub.getJSONObject("企业基本信息").getString("公司简介"));
        getSubjectInfo.put("注册资本",objEnterpriseSub.getJSONObject("企业基本信息").getString("注册资本"));
        getSubjectInfo.put("注册资本币种",objEnterpriseSub.getJSONObject("企业基本信息").getIntValue("注册资本币种"));
        getSubjectInfo.put("实收资本",objEnterpriseSub.getJSONObject("企业基本信息").getString("实收资本"));
        getSubjectInfo.put("实收资本币种",objEnterpriseSub.getJSONObject("企业基本信息").getIntValue("实收资本币种"));
        getSubjectInfo.put("注册地址",objEnterpriseSub.getJSONObject("企业基本信息").getString("注册地址"));
        getSubjectInfo.put("办公地址",objEnterpriseSub.getJSONObject("企业基本信息").getString("办公地址"));
        getSubjectInfo.put("联系地址",objEnterpriseSub.getJSONObject("企业基本信息").getString("联系地址"));
        getSubjectInfo.put("联系电话",objEnterpriseSub.getJSONObject("企业基本信息").getString("联系电话"));
        getSubjectInfo.put("传真",objEnterpriseSub.getJSONObject("企业基本信息").getString("传真"));
        getSubjectInfo.put("邮政编码",objEnterpriseSub.getJSONObject("企业基本信息").getString("邮政编码"));
        getSubjectInfo.put("互联网地址",objEnterpriseSub.getJSONObject("企业基本信息").getString("互联网地址"));
        getSubjectInfo.put("电子邮箱",objEnterpriseSub.getJSONObject("企业基本信息").getString("电子邮箱"));
        getSubjectInfo.put("公司章程",objEnterpriseSub.getJSONObject("企业基本信息").getString("公司章程"));
        getSubjectInfo.put("主管单位",objEnterpriseSub.getJSONObject("企业基本信息").getString("主管单位"));
        getSubjectInfo.put("股东总数（个）",objEnterpriseSub.getJSONObject("企业基本信息").getIntValue("股东总数（个）"));
        getSubjectInfo.put("股本总数(股)",objEnterpriseSub.getJSONObject("企业基本信息").getString("股本总数(股)"));
        getSubjectInfo.put("法定代表人姓名",objEnterpriseSub.getJSONObject("法人信息").getString("法定代表人姓名"));
        getSubjectInfo.put("法人性质",objEnterpriseSub.getJSONObject("法人信息").getIntValue("法人性质"));
        getSubjectInfo.put("法定代表人身份证件类型",objEnterpriseSub.getJSONObject("法人信息").getIntValue("法定代表人身份证件类型"));
        getSubjectInfo.put("法定代表人身份证件号码",objEnterpriseSub.getJSONObject("法人信息").getString("法定代表人身份证件号码"));
        getSubjectInfo.put("法定代表人职务",objEnterpriseSub.getJSONObject("法人信息").getIntValue("法定代表人职务"));
        getSubjectInfo.put("法定代表人手机号",objEnterpriseSub.getJSONObject("法人信息").getString("法定代表人手机号"));

        return getSubjectInfo;
    }

    public Map getPersonalSubInfo(String response){

        com.alibaba.fastjson.JSONObject object2 = com.alibaba.fastjson.JSONObject.parseObject(response);

        com.alibaba.fastjson.JSONObject objSubBase = object2.getJSONObject("data").getJSONObject("body").getJSONObject("主体信息").getJSONObject("主体基本信息");
        com.alibaba.fastjson.JSONObject objPersonalSub = object2.getJSONObject("data").getJSONObject("body").getJSONObject("主体信息").getJSONObject("个人主体信息");

        Map getSubjectInfo = new HashMap();
        getSubjectInfo.put("对象标识",object2.getJSONObject("data").getJSONObject("body").getJSONObject("对象信息").getString("对象标识"));

        getSubjectInfo.put("主体标识",objSubBase.getJSONObject("主体通用信息").getString("主体标识"));
        getSubjectInfo.put("行业主体代号",objSubBase.getJSONObject("主体通用信息").getString("行业主体代号"));
        getSubjectInfo.put("主体类型",objSubBase.getJSONObject("主体通用信息").getIntValue("主体类型"));
        getSubjectInfo.put("主体信息创建时间",objSubBase.getJSONObject("主体通用信息").getString("主体信息创建时间"));

        getSubjectInfo.put("主体资质信息", com.alibaba.fastjson.JSONObject.parseArray(objSubBase.getJSONArray("主体资质信息").toJSONString(), Map.class));

        getSubjectInfo.put("个人姓名",objPersonalSub.getJSONObject("个人主体基本信息").get("个人姓名"));
        getSubjectInfo.put("个人身份证类型",objPersonalSub.getJSONObject("个人主体基本信息").get("个人身份证类型"));
        getSubjectInfo.put("个人身份证件号",objPersonalSub.getJSONObject("个人主体基本信息").get("个人身份证件号"));
        getSubjectInfo.put("个人联系地址",objPersonalSub.getJSONObject("个人主体基本信息").get("个人联系地址"));
        getSubjectInfo.put("个人联系电话",objPersonalSub.getJSONObject("个人主体基本信息").get("个人联系电话"));
        getSubjectInfo.put("个人手机号",objPersonalSub.getJSONObject("个人主体基本信息").get("个人手机号"));
        getSubjectInfo.put("学历",objPersonalSub.getJSONObject("个人主体基本信息").get("学历"));
        getSubjectInfo.put("个人所属行业",objPersonalSub.getJSONObject("个人主体基本信息").get("个人所属行业"));
        getSubjectInfo.put("出生日期",objPersonalSub.getJSONObject("个人主体基本信息").get("出生日期"));
        getSubjectInfo.put("性别",objPersonalSub.getJSONObject("个人主体基本信息").get("性别"));

        getSubjectInfo.put("评级结果",objPersonalSub.getJSONObject("个人主体风险评级").get("评级结果"));
        getSubjectInfo.put("评级时间",objPersonalSub.getJSONObject("个人主体风险评级").get("评级时间"));
        getSubjectInfo.put("评级原始记录",objPersonalSub.getJSONObject("个人主体风险评级").get("评级原始记录"));

        return getSubjectInfo;
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
        for(int i=0;i<jsonArray2.size();i++){
            com.alibaba.fastjson.JSONObject objTemp = com.alibaba.fastjson.JSONObject.parseObject(jsonArray2.get(i).toString());
            if( objTemp.getJSONObject("header").getJSONObject("content").getString("type").equals("账户") &&
                    (objTemp.getJSONObject("body").getJSONObject("账户信息").getJSONObject("账户基本信息").getIntValue("账户类型") == 1) &&
                    objTemp.getJSONObject("body").getJSONObject("对象信息").getString("账户对象标识").equals(objId)){
                jobjOK = objTemp;
                break;
            }
        }

        com.alibaba.fastjson.JSONObject objAccbase = jobjOK.getJSONObject("body").getJSONObject("账户信息").getJSONObject("账户基本信息");
        com.alibaba.fastjson.JSONObject objAccRela = jobjOK.getJSONObject("body").getJSONObject("账户信息").getJSONObject("账户关联信息");
        com.alibaba.fastjson.JSONObject objAccLife = jobjOK.getJSONObject("body").getJSONObject("账户信息").getJSONObject("账户生命周期信息");

        Map getSubjectInfo = new HashMap();
        getSubjectInfo.put("账户对象标识",jobjOK.getJSONObject("body").getJSONObject("对象信息").getString("账户对象标识"));

        getSubjectInfo.put("账户所属主体引用",objAccbase.getString("账户所属主体引用"));
        getSubjectInfo.put("开户机构主体引用",objAccbase.getString("开户机构主体引用"));
        getSubjectInfo.put("账号",objAccbase.getString("账号"));
        getSubjectInfo.put("账户类型",objAccbase.getString("账户类型"));  //默认股权账户
        getSubjectInfo.put("账户用途",objAccbase.getString("账户用途"));
        getSubjectInfo.put("账号状态",objAccbase.getString("账号状态"));

        getSubjectInfo.put("账户开户时间",objAccLife.getJSONObject("开户信息").getString("账户开户时间"));
        getSubjectInfo.put("账户开户核验凭证", objAccLife.getJSONObject("开户信息").getString("账户开户核验凭证"));

        getSubjectInfo.put("账户销户时间",objAccLife.getJSONObject("销户信息").getString("账户销户时间"));
        getSubjectInfo.put("账户销户核验凭证", objAccLife.getJSONObject("销户信息").getString("账户销户核验凭证"));

        getSubjectInfo.put("账户冻结时间",objAccLife.getJSONObject("冻结信息").getString("账户冻结时间"));
        getSubjectInfo.put("账户冻结核验凭证", objAccLife.getJSONObject("冻结信息").getString("账户冻结核验凭证"));

        getSubjectInfo.put("账户解冻时间",objAccLife.getJSONObject("解冻信息").getString("账户解冻时间"));
        getSubjectInfo.put("账户解冻核验凭证", objAccLife.getJSONObject("解冻信息").getString("账户解冻核验凭证"));


        getSubjectInfo.put("关联关系",objAccRela.getString("关联关系"));
        getSubjectInfo.put("关联账户对象引用",objAccRela.getString("关联账户对象引用"));
        getSubjectInfo.put("关联账户开户文件", objAccRela.getString("关联账户开户文件"));
//
//        getSubjectInfo.put("账户开户时间",objAccLife.getJSONObject("开户信息").getString("账户开户时间"));
//        getSubjectInfo.put("账户开户核验凭证", com.alibaba.fastjson.JSONObject.parseArray(
//                objAccLife.getJSONObject("开户信息").getJSONArray("账户开户核验凭证").toJSONString(), String.class));
//
//        getSubjectInfo.put("账户销户时间",objAccLife.getJSONObject("销户信息").getString("账户销户时间"));
//        getSubjectInfo.put("账户销户核验凭证", com.alibaba.fastjson.JSONObject.parseArray(
//                objAccLife.getJSONObject("销户信息").getJSONArray("账户销户核验凭证").toJSONString(), String.class));
//
//        getSubjectInfo.put("账户冻结时间",objAccLife.getJSONObject("冻结信息").getString("账户冻结时间"));
//        getSubjectInfo.put("账户冻结核验凭证", com.alibaba.fastjson.JSONObject.parseArray(
//                objAccLife.getJSONObject("冻结信息").getJSONArray("账户冻结核验凭证").toJSONString(), String.class));
//
//        getSubjectInfo.put("账户解冻时间",objAccLife.getJSONObject("解冻信息").getString("账户解冻时间"));
//        getSubjectInfo.put("账户解冻核验凭证", com.alibaba.fastjson.JSONObject.parseArray(
//                objAccLife.getJSONObject("解冻信息").getJSONArray("账户解冻核验凭证").toJSONString(), String.class));
//
//
//        getSubjectInfo.put("关联关系",objAccRela.getString("关联关系"));
//        getSubjectInfo.put("关联账户对象引用",objAccRela.getString("关联账户对象引用"));
//        getSubjectInfo.put("关联账户开户文件", com.alibaba.fastjson.JSONObject.parseArray(
//                objAccRela.getJSONArray("关联账户开户文件").toJSONString(), String.class));

        return getSubjectInfo;
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
                    (objTemp.getJSONObject("body").getJSONObject("账户信息").getJSONObject("账户基本信息").getIntValue("账户类型") == 0) &&
                    objTemp.getJSONObject("body").getJSONObject("对象信息").getString("账户对象标识").equals(objId)){
                jobjOK = objTemp;
                break;
            }
        }

        com.alibaba.fastjson.JSONObject objAccbase = jobjOK.getJSONObject("body").getJSONObject("账户信息").getJSONObject("账户基本信息");
        com.alibaba.fastjson.JSONObject objAccRela = jobjOK.getJSONObject("body").getJSONObject("账户信息").getJSONObject("账户关联信息");
        com.alibaba.fastjson.JSONObject objAccLife = jobjOK.getJSONObject("body").getJSONObject("账户信息").getJSONObject("账户生命周期信息");

        Map getSubjectInfo = new HashMap();
        getSubjectInfo.put("账户对象标识",jobjOK.getJSONObject("body").getJSONObject("对象信息").getString("账户对象标识"));

        getSubjectInfo.put("账户所属主体引用",objAccbase.getString("账户所属主体引用"));
        getSubjectInfo.put("开户机构主体引用",objAccbase.getString("开户机构主体引用"));
        getSubjectInfo.put("账号",objAccbase.getString("账号"));
        getSubjectInfo.put("账户类型",objAccbase.getString("账户类型"));  //默认股权账户
        getSubjectInfo.put("账户用途",objAccbase.getString("账户用途"));
        getSubjectInfo.put("账号状态",objAccbase.getString("账号状态"));

        getSubjectInfo.put("账户开户时间",objAccLife.getJSONObject("开户信息").getString("账户开户时间"));
        getSubjectInfo.put("账户开户核验凭证", objAccLife.getJSONObject("开户信息").getString("账户开户核验凭证"));

        getSubjectInfo.put("账户销户时间",objAccLife.getJSONObject("销户信息").getString("账户销户时间"));
        getSubjectInfo.put("账户销户核验凭证", objAccLife.getJSONObject("销户信息").getString("账户销户核验凭证"));

        getSubjectInfo.put("账户冻结时间",objAccLife.getJSONObject("冻结信息").getString("账户冻结时间"));
        getSubjectInfo.put("账户冻结核验凭证", objAccLife.getJSONObject("冻结信息").getString("账户冻结核验凭证"));

        getSubjectInfo.put("账户解冻时间",objAccLife.getJSONObject("解冻信息").getString("账户解冻时间"));
        getSubjectInfo.put("账户解冻核验凭证", objAccLife.getJSONObject("解冻信息").getString("账户解冻核验凭证"));


        getSubjectInfo.put("关联关系",objAccRela.getString("关联关系"));
        getSubjectInfo.put("关联账户对象引用",objAccRela.getString("关联账户对象引用"));
        getSubjectInfo.put("关联账户开户文件", objAccRela.getString("关联账户开户文件"));


//        getSubjectInfo.put("账户开户时间",objAccLife.getJSONObject("开户信息").getString("账户开户时间"));
//        getSubjectInfo.put("账户开户核验凭证", com.alibaba.fastjson.JSONObject.parseArray(
//                objAccLife.getJSONObject("开户信息").getJSONArray("账户开户核验凭证").toJSONString(), String.class));
//
//        getSubjectInfo.put("账户销户时间",objAccLife.getJSONObject("销户信息").getString("账户销户时间"));
//        getSubjectInfo.put("账户销户核验凭证", com.alibaba.fastjson.JSONObject.parseArray(
//                objAccLife.getJSONObject("销户信息").getJSONArray("账户销户核验凭证").toJSONString(), String.class));
//
//        getSubjectInfo.put("账户冻结时间",objAccLife.getJSONObject("冻结信息").getString("账户冻结时间"));
//        getSubjectInfo.put("账户冻结核验凭证", com.alibaba.fastjson.JSONObject.parseArray(
//                objAccLife.getJSONObject("冻结信息").getJSONArray("账户冻结核验凭证").toJSONString(), String.class));
//
//        getSubjectInfo.put("账户解冻时间",objAccLife.getJSONObject("解冻信息").getString("账户解冻时间"));
//        getSubjectInfo.put("账户解冻核验凭证", com.alibaba.fastjson.JSONObject.parseArray(
//                objAccLife.getJSONObject("解冻信息").getJSONArray("账户解冻核验凭证").toJSONString(), String.class));
//
//
//        getSubjectInfo.put("关联关系",objAccRela.getString("关联关系"));
//        getSubjectInfo.put("关联账户对象引用",objAccRela.getString("关联账户对象引用"));
//        getSubjectInfo.put("关联账户开户文件", com.alibaba.fastjson.JSONObject.parseArray(
//                objAccRela.getJSONArray("关联账户开户文件").toJSONString(), String.class));

        return getSubjectInfo;
    }


    public Map contructEquityProdInfo(String prodTxId){
        log.info("检查的交易id " + prodTxId);
        com.alibaba.fastjson.JSONObject jobj2 = null;
        com.alibaba.fastjson.JSONObject object2 = com.alibaba.fastjson.JSONObject.parseObject(store.GetTxDetail(prodTxId));
        String storeData2 = object2.getJSONObject("data").getJSONObject("store").getString("storeData");

        //如果是单独的一个则直接使用JSONObject进行解析
        if(storeData2.startsWith("{")){
            jobj2 = com.alibaba.fastjson.JSONObject.parseObject(storeData2);
        }else {
            //storedata是个list时
            com.alibaba.fastjson.JSONArray jsonArray2 = com.alibaba.fastjson.JSONArray.parseArray(storeData2);
            for (int i = 0; i < jsonArray2.size(); i++) {
                com.alibaba.fastjson.JSONObject objTemp = com.alibaba.fastjson.JSONObject.parseObject(jsonArray2.get(i).toString());
                if (objTemp.getJSONObject("header").getJSONObject("content").getString("type").equals("产品")) {
                    jobj2 = objTemp;
                    break;
                }
            }
        }

        com.alibaba.fastjson.JSONObject objProdBase = jobj2.getJSONObject("body").getJSONObject("产品信息").getJSONObject("产品基本信息");
        com.alibaba.fastjson.JSONObject objProdIssue = jobj2.getJSONObject("body").getJSONObject("产品信息").getJSONObject("产品发行信息");

        Map getSubjectInfo = new HashMap();

        getSubjectInfo.put("产品对象标识",jobj2.getJSONObject("body").getJSONObject("对象标识").getString("产品对象标识"));
        getSubjectInfo.put("发行主体引用",objProdBase.getString("发行主体引用"));
        getSubjectInfo.put("发行主体名称",objProdBase.getString("发行主体名称"));
        getSubjectInfo.put("登记机构主体引用",objProdBase.getString("登记机构主体引用"));
        getSubjectInfo.put("托管机构主体引用",objProdBase.getString("托管机构主体引用"));
        getSubjectInfo.put("产品代码",objProdBase.getString("产品代码"));
        getSubjectInfo.put("产品全称",objProdBase.getString("产品全称"));
        getSubjectInfo.put("产品简称",objProdBase.getString("产品简称"));
        getSubjectInfo.put("产品类型",objProdBase.getString("产品类型"));
        getSubjectInfo.put("最大账户数量",objProdBase.getString("最大账户数量"));
        getSubjectInfo.put("信息披露方式",objProdBase.getString("信息披露方式"));
        getSubjectInfo.put("产品规模单位",objProdBase.getString("产品规模单位"));
        getSubjectInfo.put("产品规模币种",objProdBase.getString("产品规模币种"));
        getSubjectInfo.put("产品规模总额",objProdBase.getString("产品规模总额"));
        getSubjectInfo.put("浏览范围",objProdBase.getString("浏览范围"));
        getSubjectInfo.put("交易范围",objProdBase.getString("交易范围"));

        getSubjectInfo.put("承销机构主体引用",objProdIssue.getJSONObject("发行服务方信息").getString("承销机构主体引用"));
        getSubjectInfo.put("承销机构名称",objProdIssue.getJSONObject("发行服务方信息").getString("承销机构名称"));
        getSubjectInfo.put("律师事务所主体引用",objProdIssue.getJSONObject("发行服务方信息").getString("律师事务所主体引用"));
        getSubjectInfo.put("律师事务所名称",objProdIssue.getJSONObject("发行服务方信息").getString("律师事务所名称"));
        getSubjectInfo.put("会计事务所主体引用",objProdIssue.getJSONObject("发行服务方信息").getString("会计事务所主体引用"));
        getSubjectInfo.put("会计事务所名称",objProdIssue.getJSONObject("发行服务方信息").getString("会计事务所名称"));

        getSubjectInfo.put("发行方联系人",objProdIssue.getJSONObject("联系信息").getString("发行方联系人"));
        getSubjectInfo.put("发行方联系信息",objProdIssue.getJSONObject("联系信息").getString("发行方联系信息"));

        getSubjectInfo.put("股权类-发行增资信息", com.alibaba.fastjson.JSONObject.parseArray(objProdIssue.getJSONArray("股权类-发行增资信息").toJSONString(), Map.class));

        return getSubjectInfo;
    }

    public Map contructBondProdInfo(String prodTxId){
        com.alibaba.fastjson.JSONObject jobj2 = null;
        com.alibaba.fastjson.JSONObject object2 = com.alibaba.fastjson.JSONObject.parseObject(store.GetTxDetail(prodTxId));
        String storeData2 = object2.getJSONObject("data").getJSONObject("store").getString("storeData");

        //如果是单独的一个则直接使用JSONObject进行解析
        if(storeData2.startsWith("{")){
            jobj2 = com.alibaba.fastjson.JSONObject.parseObject(storeData2);
        }else {
            //storedata是个list时
            com.alibaba.fastjson.JSONArray jsonArray2 = com.alibaba.fastjson.JSONArray.parseArray(storeData2);
            for (int i = 0; i < jsonArray2.size(); i++) {
                com.alibaba.fastjson.JSONObject objTemp = com.alibaba.fastjson.JSONObject.parseObject(jsonArray2.get(i).toString());
                if (objTemp.getJSONObject("header").getJSONObject("content").getString("type").equals("产品")) {
                    jobj2 = objTemp;
                    break;
                }
            }
        }
        com.alibaba.fastjson.JSONObject objProdBase = jobj2.getJSONObject("body").getJSONObject("产品信息").getJSONObject("产品基本信息");
        com.alibaba.fastjson.JSONObject objProdIssue = jobj2.getJSONObject("body").getJSONObject("产品信息").getJSONObject("产品发行信息");

        Map getSubjectInfo = new HashMap();

        getSubjectInfo.put("产品对象标识",jobj2.getJSONObject("body").getJSONObject("对象标识").getString("产品对象标识"));

        getSubjectInfo.put("发行主体引用",objProdBase.getString("发行主体引用"));
        getSubjectInfo.put("发行主体名称",objProdBase.getString("发行主体名称"));
        getSubjectInfo.put("登记机构主体引用",objProdBase.getString("登记机构主体引用"));
        getSubjectInfo.put("托管机构主体引用",objProdBase.getString("托管机构主体引用"));
        getSubjectInfo.put("产品代码",objProdBase.getString("产品代码"));
        getSubjectInfo.put("产品全称",objProdBase.getString("产品全称"));
        getSubjectInfo.put("产品简称",objProdBase.getString("产品简称"));
        getSubjectInfo.put("产品类型",objProdBase.getString("产品类型"));
        getSubjectInfo.put("最大账户数量",objProdBase.getString("最大账户数量"));
        getSubjectInfo.put("信息披露方式",objProdBase.getString("信息披露方式"));
        getSubjectInfo.put("产品规模单位",objProdBase.getString("产品规模单位"));
        getSubjectInfo.put("产品规模币种",objProdBase.getString("产品规模币种"));
        getSubjectInfo.put("产品规模总额",objProdBase.getString("产品规模总额"));
        getSubjectInfo.put("浏览范围",objProdBase.getString("浏览范围"));
        getSubjectInfo.put("交易范围",objProdBase.getString("交易范围"));

        getSubjectInfo.put("承销机构主体引用",objProdIssue.getJSONObject("发行服务方信息").getString("承销机构主体引用"));
        getSubjectInfo.put("承销机构名称",objProdIssue.getJSONObject("发行服务方信息").getString("承销机构名称"));
        getSubjectInfo.put("律师事务所主体引用",objProdIssue.getJSONObject("发行服务方信息").getString("律师事务所主体引用"));
        getSubjectInfo.put("律师事务所名称",objProdIssue.getJSONObject("发行服务方信息").getString("律师事务所名称"));
        getSubjectInfo.put("会计事务所主体引用",objProdIssue.getJSONObject("发行服务方信息").getString("会计事务所主体引用"));
        getSubjectInfo.put("会计事务所名称",objProdIssue.getJSONObject("发行服务方信息").getString("会计事务所名称"));

        getSubjectInfo.put("发行方联系人",objProdIssue.getJSONObject("联系信息").getString("发行方联系人"));
        getSubjectInfo.put("发行方联系信息",objProdIssue.getJSONObject("联系信息").getString("发行方联系信息"));

        getSubjectInfo.put("发行代码",objProdIssue.getJSONObject("债券类-发行信息").getString("发行代码"));
        getSubjectInfo.put("存续期限",objProdIssue.getJSONObject("债券类-发行信息").getString("存续期限"));
        getSubjectInfo.put("最小账户数量",objProdIssue.getJSONObject("债券类-发行信息").getString("最小账户数量"));
        getSubjectInfo.put("产品面值",objProdIssue.getJSONObject("债券类-发行信息").getString("产品面值"));
        getSubjectInfo.put("票面利率",objProdIssue.getJSONObject("债券类-发行信息").getString("票面利率"));
        getSubjectInfo.put("利率形式",objProdIssue.getJSONObject("债券类-发行信息").getString("利率形式"));
        getSubjectInfo.put("付息频率",objProdIssue.getJSONObject("债券类-发行信息").getString("付息频率"));
        getSubjectInfo.put("非闰年计息天数",objProdIssue.getJSONObject("债券类-发行信息").getString("非闰年计息天数"));
        getSubjectInfo.put("闰年计息天数",objProdIssue.getJSONObject("债券类-发行信息").getString("闰年计息天数"));
        getSubjectInfo.put("发行价格",objProdIssue.getJSONObject("债券类-发行信息").getString("发行价格"));
        getSubjectInfo.put("选择权条款",objProdIssue.getJSONObject("债券类-发行信息").getString("选择权条款"));
        getSubjectInfo.put("（本期）发行规模上限",objProdIssue.getJSONObject("债券类-发行信息").getString("（本期）发行规模上限"));
        getSubjectInfo.put("（本期）发行规模下限",objProdIssue.getJSONObject("债券类-发行信息").getString("（本期）发行规模下限"));
        getSubjectInfo.put("发行开始日期",objProdIssue.getJSONObject("债券类-发行信息").getString("发行开始日期"));
        getSubjectInfo.put("发行结束日期",objProdIssue.getJSONObject("债券类-发行信息").getString("发行结束日期"));
        getSubjectInfo.put("登记日期",objProdIssue.getJSONObject("债券类-发行信息").getString("登记日期"));
        getSubjectInfo.put("起息日期",objProdIssue.getJSONObject("债券类-发行信息").getString("起息日期"));
        getSubjectInfo.put("到期日期",objProdIssue.getJSONObject("债券类-发行信息").getString("到期日期"));
        getSubjectInfo.put("首次付息日期",objProdIssue.getJSONObject("债券类-发行信息").getString("首次付息日期"));
        getSubjectInfo.put("发行文件编号",objProdIssue.getJSONObject("债券类-发行信息").getString("发行文件编号"));
        getSubjectInfo.put("发行文件列表", com.alibaba.fastjson.JSONObject.parseArray(
                objProdIssue.getJSONObject("债券类-发行信息").getJSONArray("发行文件列表").toJSONString(), String.class));
        getSubjectInfo.put("发行方主体信用评级",objProdIssue.getJSONObject("债券类-发行信息").getString("发行方主体信用评级"));
        getSubjectInfo.put("增信机构主体引用",objProdIssue.getJSONObject("债券类-发行信息").getString("增信机构主体引用"));
        getSubjectInfo.put("增信机构名称",objProdIssue.getJSONObject("债券类-发行信息").getString("增信机构名称"));
        getSubjectInfo.put("增信机构主体评级",objProdIssue.getJSONObject("债券类-发行信息").getString("增信机构主体评级"));
        getSubjectInfo.put("信用评级机构主体引用",objProdIssue.getJSONObject("债券类-发行信息").getString("信用评级机构主体引用"));
        getSubjectInfo.put("信用评级机构名称",objProdIssue.getJSONObject("债券类-发行信息").getString("信用评级机构名称"));
        getSubjectInfo.put("担保机构主体引用",objProdIssue.getJSONObject("债券类-发行信息").getString("担保机构主体引用"));
        getSubjectInfo.put("担保机构名称",objProdIssue.getJSONObject("债券类-发行信息").getString("担保机构名称"));
        getSubjectInfo.put("担保安排",objProdIssue.getJSONObject("债券类-发行信息").getString("担保安排"));
        getSubjectInfo.put("产品终止条件",objProdIssue.getJSONObject("债券类-发行信息").getString("产品终止条件"));

        return getSubjectInfo;
    }
}
