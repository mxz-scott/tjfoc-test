package com.tjfintech.common.functionTest.guDengTestV2;

import com.alibaba.fastjson.JSON;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.GuDeng;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.*;
import org.junit.rules.TestName;
import org.junit.runners.MethodSorters;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.functionTest.guDengTestV2.GDCommonFunc.*;
import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassGD.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class GDV2_SceneTest_Issue {

    TestBuilder testBuilder= TestBuilder.getInstance();
    GuDeng gd =testBuilder.getGuDeng();
    Store store =testBuilder.getStore();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    public static String bizNoTest = "test" + Random(12);
    GDUnitFunc uf = new GDUnitFunc();
    GDCommonFunc gdCF = new GDCommonFunc();
    public static int index = 0;

    @Rule
    public TestName tm = new TestName();

    @BeforeClass
    public static void Before()throws Exception{
        GDBeforeCondition gdBefore = new GDBeforeCondition();
        gdBefore.gdCreateAccout();
        register_event_type = "1";
    }
    @AfterClass
    public static void endSet()throws Exception{
        bChangeRegSN = false;
    }

    @Before
    public void IssueEquity()throws Exception{
        bizNoTest = "test" + Random(12);
        gdCompanyID = CNKey + "Sub2_" + Random(4);
        gdEquityCode = CNKey + "Token2_" + Random(4);
        bUriMapExist = false;
        index = 0;

        //发行
//        uf.commonIssuePP01(1000);//发行给账户1~4 股权性质对应 0 1 0 1
    }

//    @After
    public void calJGDataAfterTx()throws Exception{
        indexReg = "";
        bSaveBuff = false;

        testCurMethodName = tm.getMethodName();
        GDUnitFunc uf = new GDUnitFunc();
        int endHeight = net.sf.json.JSONObject.fromObject(store.GetHeight()).getInt("data");
        uf.checkJGHeaderOpVer(blockHeight,endHeight);
    }

    /***
     * 发行数量超过1000000
     */

    @Test
    public void issueBigAmount()throws Exception{

        List<Map> shareList = gdConstructShareList(gdAccount1,1000002,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,9000000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,0, shareList3);

        String response = uf.shareIssue(gdEquityCode,shareList4,true);

        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,1000002,0,0,mapShareENCN().get("0"),respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2,9000000,0,0,mapShareENCN().get("0"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3,1000,0,0,mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4,1000,0,0,mapShareENCN().get("0"), respShareList3);

        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
//        assertEquals("200",JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");
        //检查存在余额的股东列表
        assertEquals(respShareList4.size(),dataShareList.size());

        List<Map> getShareList = getShareListFromQueryNoZeroAcc(dataShareList);

        assertEquals(respShareList4.size(),getShareList.size());
        assertEquals(true,respShareList4.containsAll(getShareList) && getShareList.containsAll(respShareList4));

    }


    /***
     * 多次发行登记对象标识使用同一个
     *
     * */

    @Test
    public void issueSameRegisterObjectId()throws Exception{
        gdEquityCode = "sameRegObjIdEC" + Random(3);
        gdCompanyID = "sameRegObjIdSub" + Random(3);

        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,1, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,1, shareList3);

        //发行已经发行过的股权代码
        String response = uf.shareIssue(gdEquityCode,shareList4,false);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));

        sleepAndSaveInfo(3000);

        gdEquityCode = "sameRegObjIdEC2" + Random(3);
        gdCompanyID = "sameRegObjIdSub2" + Random(3);

        response = uf.shareIssue(gdEquityCode,shareList4,false);

        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(true,
                JSONObject.fromObject(response).getString("message").contains("查询到版本不为0,请检查此对象标识是否已经存在"));
//        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");

        sleepAndSaveInfo(3000);

        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("400",JSONObject.fromObject(query).getString("state"));
        assertEquals("股权代码还未发行或者已经转场",JSONObject.fromObject(query).getString("message"));

    }
    /***
     * 发行不同股权代码性质的股权 多个股东
     */

    //12个

    @Test
    public void issueDiffPropertyEquityCode()throws Exception{
        gdEquityCode = "gdEC" + Random(13);
        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        shareList = gdConstructShareList(gdAccount2,1000,1,shareList);
        shareList = gdConstructShareList(gdAccount3,1000,2,shareList);
        shareList = gdConstructShareList(gdAccount4,1000,3,shareList);
        shareList = gdConstructShareList(gdAccount5,1000,4,shareList);
        shareList = gdConstructShareList(gdAccount6,1000,5,shareList);
        shareList = gdConstructShareList(gdAccount1,1000,6,shareList);
        shareList = gdConstructShareList(gdAccount5,1000,21,shareList);
        shareList = gdConstructShareList(gdAccount6,1000,32,shareList);
        shareList = gdConstructShareList(gdAccount1,1000,43,shareList);
        shareList = gdConstructShareList(gdAccount5,1000,88,shareList);
        shareList = gdConstructShareList(gdAccount6,1000,99,shareList);


        //发行已经发行过的股权代码
        String response = uf.shareIssue(gdEquityCode,shareList,true);


        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");

        JSONArray jsonArrayGet = JSONObject.fromObject(query).getJSONArray("data");
        assertEquals("12000",getTotalAmountFromShareList(jsonArrayGet));

        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,1000,0,0,mapShareENCN().get("0"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount1,1000,6,0,mapShareENCN().get("6"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount1,1000,43,0,mapShareENCN().get("43"),respShareList);
//        respShareList = gdConstructQueryShareList(gdAccount1,1000,100,0,mapShareENCN().get("0"),respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2,1000,1,0,mapShareENCN().get("1"), respShareList);
//        respShareList2 = gdConstructQueryShareList(gdAccount2,1000,7,0,mapShareENCN().get("0"), respShareList2);
//        respShareList2 = gdConstructQueryShareList(gdAccount2,1000,54,0,mapShareENCN().get("0"), respShareList2);
//        respShareList2 = gdConstructQueryShareList(gdAccount2,1000,101,0,mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3,1000,2,0,mapShareENCN().get("2"), respShareList2);
//        respShareList3 = gdConstructQueryShareList(gdAccount3,1000,8,0,mapShareENCN().get("0"), respShareList3);
//        respShareList3 = gdConstructQueryShareList(gdAccount3,1000,65,0,mapShareENCN().get("0"), respShareList3);
//        respShareList3 = gdConstructQueryShareList(gdAccount3,1000,156,0,mapShareENCN().get("0"), respShareList3);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4,1000,3,0,mapShareENCN().get("3"), respShareList3);
//        respShareList4 = gdConstructQueryShareList(gdAccount4,1000,10,0,mapShareENCN().get("0"), respShareList4);
//        respShareList4 = gdConstructQueryShareList(gdAccount4,1000,76,0,mapShareENCN().get("0"), respShareList4);
        List<Map> respShareList5 = gdConstructQueryShareList(gdAccount5,1000,4,0,mapShareENCN().get("4"), respShareList4);
        respShareList5 = gdConstructQueryShareList(gdAccount5,1000,21,0,mapShareENCN().get("21"), respShareList5);
        respShareList5 = gdConstructQueryShareList(gdAccount5,1000,88,0,"", respShareList5);
        List<Map> respShareList6 = gdConstructQueryShareList(gdAccount6,1000,5,0,mapShareENCN().get("5"), respShareList5);
        respShareList6 = gdConstructQueryShareList(gdAccount6,1000,32,0,mapShareENCN().get("32"), respShareList6);
        respShareList6 = gdConstructQueryShareList(gdAccount6,1000,99,0,mapShareENCN().get("99"), respShareList6);

        log.info(respShareList6.toString());
        //检查存在余额的股东列表
        assertEquals(respShareList6.size(),dataShareList.size());

        List<Map> getShareList = getShareListFromQueryNoZeroAcc(dataShareList);

        assertEquals(respShareList6.size(),getShareList.size());
        assertEquals(true,respShareList6.containsAll(getShareList) && getShareList.containsAll(respShareList6));


        //查询股东持股情况 无当前股权代码信息
        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo3);
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":2,\"sharePropertyCN\":\"" + mapShareENCN().get("2") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo4);
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":3,\"sharePropertyCN\":\"" + mapShareENCN().get("3") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo5);
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":4,\"sharePropertyCN\":\"" + mapShareENCN().get("4") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo6);
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":5,\"sharePropertyCN\":\"" + mapShareENCN().get("5") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo7);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

    }

    //500个响应时间约46s 交易详情大小约1M
    //193个 响应时间约17s 交易大小约400K
    //12个 响应时间约 交易大小约30K
    @Test
    public void issue200()throws Exception{
        gdEquityCode = "gdEC" + Random(13);
        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        for(int i =0;i< 200;i++) {
            shareList = gdConstructShareList(gdAccount2, 1000, 1, shareList);
//            shareList = gdConstructShareList(gdAccount3, 1000, 0, shareList);
//            shareList = gdConstructShareList(gdAccount4, 1000, 0, shareList);
//            shareList = gdConstructShareList(gdAccount5, 1000, 1, shareList);
        }

        //发行已经发行过的股权代码
        String response = uf.shareIssue(gdEquityCode,shareList,true);
    }


    @Test
    public void issueByTwo()throws Exception{
        mapAddrRegObjId.clear();
        indexReg = String.valueOf(index);
        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        indexReg = String.valueOf(++index);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,1, shareList);
        indexReg = String.valueOf(++index);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0);
        indexReg = String.valueOf(++index);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,1, shareList3);

        bSaveBuff = true;
        //发行已经发行过的股权代码
        String response = uf.shareIssue(gdEquityCode,shareList2,false);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));

        sleepAndSaveInfo(3000);

        bSaveBuff = false;
        String resp = gd.GDShareIssue(gdContractAddress,gdPlatfromKeyID,gdEquityCode,shareList4);
        assertEquals("200",JSONObject.fromObject(resp).getString("state"));
        String txId = JSONObject.fromObject(resp).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        List<Map> shareListTotal = new ArrayList<>();
        for(int i = 0;i<shareList2.size();i++){
            shareListTotal.add(shareList2.get(i));
        }

        for(int i = 0;i<shareList4.size();i++){
            shareListTotal.add(shareList4.get(i));
        }


        //遍历检查所有账户登记及交易存证信息
        for(int k = 0 ;k < shareList2.size(); k++) {
            log.info("test count ----------------------- " + k);
            String tempAddr = JSONObject.fromObject(shareList2.get(k)).getString("address");
            String tempSP = JSONObject.fromObject(shareList2.get(k)).getString("shareProperty");

            log.info("test count ----------------------- " + tempAddr + tempSP + k);
            String tempObjId = mapAddrRegObjId.get(tempAddr + tempSP + k).toString();

            register_subject_account_ref = "SH" + mapAccAddr.get(tempAddr);
            Map mapChkKeys = new HashMap();
            mapChkKeys.put("address","");
            mapChkKeys.put("txId",txId);
            mapChkKeys.put("objectid",tempObjId);
            mapChkKeys.put("version","0");
            mapChkKeys.put("contentType",regType);
            mapChkKeys.put("subProdSubType","");
            mapChkKeys.put("operationType","create");

            Map mapKeyUpdate = new HashMap();
            mapKeyUpdate.put("register_subject_account_ref","SH" + mapAccAddr.get(tempAddr));

            String json = JSON.toJSONString(mapKeyUpdate);
            mapChkKeys.put("updateMap",json);

            assertEquals("检查数据",true,gdCF.bCheckJGParams(mapChkKeys));
            bUriMapExist = true;
        }

        store.GetHeight();
        bUriMapExist = false;

    }


    @Test
    public void issueRegThreeSeperate()throws Exception{
        String eqCode1 = "testCode1" + Random(6);
        String eqCode2 = "testCode2" + Random(6);
        String eqCode3 = "testCode3" + Random(6);
        List list1 = new ArrayList();
        List list2 = new ArrayList();
        List list3 = new ArrayList();


        gdEquityCode = eqCode1;
        mapAddrRegObjId.clear();
        indexReg = "0";
        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        indexReg = "1";
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,1, shareList);

        for(Object value: mapAddrRegObjId.values()) {
            list1.add(value.toString());
        }

        log.info("第一个股权代码登记 不报送");
        bSaveBuff = true;

        //发行已经发行过的股权代码
        String response = uf.shareIssue(gdEquityCode,shareList2,false);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
        sleepAndSaveInfo(3000);


        gdEquityCode = eqCode2;
        mapAddrRegObjId.clear();
        indexReg = "0";
        List<Map> shareList11 = gdConstructShareList(gdAccount1,1000,0);
        indexReg = "1";
        List<Map> shareList12 = gdConstructShareList(gdAccount2,1000,1, shareList11);
        log.info("第二个股权代码登记 不报送");
        bSaveBuff = true;

        for(Object value: mapAddrRegObjId.values()) {
            list2.add(value.toString());
        }

        //发行已经发行过的股权代码
        response = uf.shareIssue(gdEquityCode,shareList12,false);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
        sleepAndSaveInfo(3000);


        gdEquityCode = eqCode3;
        mapAddrRegObjId.clear();
        indexReg = "0";
        List<Map> shareList21 = gdConstructShareList(gdAccount1,1000,0);
        indexReg = "1";
        List<Map> shareList22 = gdConstructShareList(gdAccount2,1000,1, shareList21);
        log.info("第三个股权代码登记 不报送");
        bSaveBuff = true;

        for(Object value: mapAddrRegObjId.values()) {
            list3.add(value.toString());
        }

        //发行已经发行过的股权代码
        response = uf.shareIssue(gdEquityCode,shareList22,false);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
        sleepAndSaveInfo(3000);


        log.info("第一个股权代码登记 并报送");
        mapAddrRegObjId.clear();
        gdEquityCode = eqCode1;
        indexReg = "2";
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0);
        bSaveBuff = false;
        String resp = gd.GDShareIssue(gdContractAddress,gdPlatfromKeyID,gdEquityCode,shareList3);
        assertEquals("200",JSONObject.fromObject(resp).getString("state"));
        String txId = JSONObject.fromObject(resp).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        shareList2.add(shareList3.get(0));

        for(Object value: mapAddrRegObjId.values()) {
            list1.add(value.toString());
        }

        //遍历检查所有账户登记及交易存证信息
        for(int k = 0 ;k < shareList2.size(); k++) {
            log.info("test count ----------------------- " + k);
            String tempAddr = JSONObject.fromObject(shareList2.get(k)).getString("address");
            String tempSP = JSONObject.fromObject(shareList2.get(k)).getString("shareProperty");

            log.info("test count ----------------------- " + tempAddr + tempSP + k);
            String tempObjId = "";//list1.get(k).toString();
            for(int j =0;j<list1.size();j++){
                if(list1.get(j).toString().contains(mapAccAddr.get(tempAddr).toString())) {
                    tempObjId = list1.get(j).toString();
                    break;
                }
            }

            register_subject_account_ref = "SH" + mapAccAddr.get(tempAddr);
            Map mapChkKeys = new HashMap();
            mapChkKeys.put("address","");
            mapChkKeys.put("txId",txId);
            mapChkKeys.put("objectid",tempObjId);
            mapChkKeys.put("version","0");
            mapChkKeys.put("contentType",regType);
            mapChkKeys.put("subProdSubType","");
            mapChkKeys.put("operationType","create");

            Map mapKeyUpdate = new HashMap();
            mapKeyUpdate.put("register_subject_account_ref","SH" + mapAccAddr.get(tempAddr));

            String json = JSON.toJSONString(mapKeyUpdate);
            mapChkKeys.put("updateMap",json);

            assertEquals("检查数据",true,gdCF.bCheckJGParams(mapChkKeys));
        }

//        sleepAndSaveInfo(10000);


        log.info("第二个股权代码登记 并报送");
        mapAddrRegObjId.clear();
        gdEquityCode = eqCode2;
        List<Map> shareList13 = gdConstructShareList(gdAccount3,1000,0);
        resp = gd.GDShareIssue(gdContractAddress,gdPlatfromKeyID,gdEquityCode,shareList13);
        assertEquals("200",JSONObject.fromObject(resp).getString("state"));
        String txId2 = JSONObject.fromObject(resp).getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId2, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        for(Object value: mapAddrRegObjId.values()) {
            list2.add(value.toString());
        }

        txId = txId2;
        shareList12.add(shareList13.get(0));

        //遍历检查所有账户登记及交易存证信息
        for(int k = 0 ;k < shareList12.size(); k++) {
            log.info("test count ----------------------- " + k);
            String tempAddr = JSONObject.fromObject(shareList12.get(k)).getString("address");
            String tempSP = JSONObject.fromObject(shareList12.get(k)).getString("shareProperty");

            log.info("test count ----------------------- " + tempAddr + tempSP + k);
            String tempObjId = "";//list1.get(k).toString();
            for(int j =0;j<list2.size();j++){
                if(list1.get(j).toString().contains(mapAccAddr.get(tempAddr).toString())) {
                    tempObjId = list2.get(j).toString();
                    break;
                }
            }

            register_subject_account_ref = "SH" + mapAccAddr.get(tempAddr);
            Map mapChkKeys = new HashMap();
            mapChkKeys.put("address","");
            mapChkKeys.put("txId",txId);
            mapChkKeys.put("objectid",tempObjId);
            mapChkKeys.put("version","0");
            mapChkKeys.put("contentType",regType);
            mapChkKeys.put("subProdSubType","");
            mapChkKeys.put("operationType","create");

            Map mapKeyUpdate = new HashMap();
            mapKeyUpdate.put("register_subject_account_ref","SH" + mapAccAddr.get(tempAddr));

            String json = JSON.toJSONString(mapKeyUpdate);
            mapChkKeys.put("updateMap",json);

            assertEquals("检查数据",true,gdCF.bCheckJGParams(mapChkKeys));
        }

        sleepAndSaveInfo(10000);

        mapAddrRegObjId.clear();
        log.info("第3三个股权代码登记 并报送");
        gdEquityCode = eqCode3;
        List<Map> shareList23 = gdConstructShareList(gdAccount3,1000,0);
        resp = gd.GDShareIssue(gdContractAddress,gdPlatfromKeyID,gdEquityCode,shareList23);
        assertEquals("200",JSONObject.fromObject(resp).getString("state"));
        String txId3 = JSONObject.fromObject(resp).getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId3, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        for(Object value: mapAddrRegObjId.values()) {
            list3.add(value.toString());
        }

        txId = txId3;
        shareList22.add(shareList23.get(0));

        //遍历检查所有账户登记及交易存证信息
        for(int k = 0 ;k < shareList12.size(); k++) {
            log.info("test count ----------------------- " + k);
            String tempAddr = JSONObject.fromObject(shareList22.get(k)).getString("address");
            String tempSP = JSONObject.fromObject(shareList22.get(k)).getString("shareProperty");

            log.info("test count ----------------------- " + tempAddr + tempSP + k);
            String tempObjId = "";//list3.get(k).toString();
            for(int j =0;j<list3.size();j++){
                if(list1.get(j).toString().contains(mapAccAddr.get(tempAddr).toString())) {
                    tempObjId = list3.get(j).toString();
                    break;
                }
            }

            register_subject_account_ref = "SH" + mapAccAddr.get(tempAddr);
            Map mapChkKeys = new HashMap();
            mapChkKeys.put("address","");
            mapChkKeys.put("txId",txId);
            mapChkKeys.put("objectid",tempObjId);
            mapChkKeys.put("version","0");
            mapChkKeys.put("contentType",regType);
            mapChkKeys.put("subProdSubType","");
            mapChkKeys.put("operationType","create");

            Map mapKeyUpdate = new HashMap();
            mapKeyUpdate.put("register_subject_account_ref","SH" + mapAccAddr.get(tempAddr));

            String json = JSON.toJSONString(mapKeyUpdate);
            mapChkKeys.put("updateMap",json);

            assertEquals("检查数据",true,gdCF.bCheckJGParams(mapChkKeys));
        }


        store.GetHeight();

    }

    public List<Map> formatIssueList(int size){
        List<Map> shareList2 = new ArrayList<>();
        for(int i =0;i< size;i++) {
            index ++;
            indexReg = String.valueOf(index);
            List<Map> shareList = gdConstructShareList(gdAccount2, 1000, 1);
            shareList2.add(shareList.get(0));
        }
        return shareList2;
    }

    public List<Map> formatIssueListN(int size){
        List<Map> shareList2 = new ArrayList<>();
        for(int i =0;i< size;i++) {
            index ++;
            indexReg = String.valueOf(index);
            List<Map> shareList = gdConstructShareListN(gdAccount2, 1000, 1);
            shareList2.add(shareList.get(0));
        }
        return shareList2;
    }

    /**
     * 20210224
     * 登记总次数 3次 total 202
     * 登记不报送：一次1 + 一次200
     * 报送登记：一次1
     * @throws Exception
     */
    @Test
    public void issueStep200_Total261()throws Exception{
//        issueStepTotal(1,200);
        issueStepTimes(1,200,60);
    }


    /**
     * 20210224
     * 登记总次数 3次 total 202
     * 登记不报送：一次1 + 一次200
     * 报送登记：一次1
     * @throws Exception
     */
    @Test
    public void issueStep200_Total202()throws Exception{
//        issueStepTotal(1,200);
        issueStepTimes(1,2,1);
    }

    /**
     * 20210224
     * 登记总次数 202次 total 202
     * 登记不报送：一次1 + 200次1
     * 报送登记：一次1
     * @throws Exception
     */
    @Test
    public void issueStep1_Total202_02()throws Exception{
//       issueStepTotal(200,1);
        issueStepTimes(200,1,1);
    }


    /**
     * 20210224
     * 登记总次数 1 + 10 + 1次 total 1002
     * 登记不报送：一次1 + 10次100
     * 报送登记：一次1
     * @throws Exception
     */
    @Test
    public void issueStep100_Total1002()throws Exception{
//        issueStepTotal(10,100);
        issueStepTimes(10,10,1);
    }

    /**
     * 20210224
     * 登记总次数 1 + 100 + 1次 total 1002
     * 登记不报送：一次1 + 100次200
     * 报送登记：一次1
     * @throws Exception
     */
    @Test
    public void issueStep200_Total20002()throws Exception{
//        issueStepTotal(100,200);
        issueStepTimes(100,200,1);
    }

//    @Test
//    public void testMap()throws Exception{
////        issueStepTotal(100,200);
//        Map test = new HashMap();
//        for(int i = 0;i<20002;i++){
//            test.put("111111111111111111_"+i,"22222222222222222222222222#" + i);
//        }
//
//        for(int i = 0;i<20002;i++){
//            log.info("" + test.get("111111111111111111_"+i));
//        }
//    }

    public void issueStepTimes(int nRegTimes,int nRegStep,int yRegStep)throws Exception{
        bChangeRegSN = true;
        store.GetHeight();
        mapAddrRegObjId.clear(); //先清空map 这样后面map中拿到的就是此次发行的登记对象标识
        bSaveBuff = true;
        gdEquityCode = "gdEC" + Random(13);

        indexReg = String.valueOf(index);
        List<Map> shareList2 = gdConstructShareList(gdAccount1,1000,0);
        uf.shareIssue(gdEquityCode,shareList2,true);

        for(int i =0;i< nRegTimes;i++) {
            log.info("test count +++++++++++++++++++++++ " + i);
            List<Map> shareListTemp = formatIssueList(nRegStep);
            String resp = gd.GDShareIssue(gdContractAddress,gdPlatfromKeyID,gdEquityCode,shareListTemp);
            assertEquals("200",JSONObject.fromObject(resp).getString("state"));

            for(int size=0;size<shareListTemp.size();size++) {
                shareList2.add(shareListTemp.get(size));
            }
        }

        bSaveBuff = false;
        List<Map>  shareList3 = formatIssueList(yRegStep);
        //发行已经发行过的股权代码
        String response = gd.GDShareIssue(gdContractAddress,gdPlatfromKeyID,gdEquityCode,shareList3);
        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        assertEquals("交易hash不为空",false,txId.equals(""));
        commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        for(int size=0;size<shareList3.size();size++) {
            shareList2.add(shareList3.get(size));
        }

        log.info("sharelist2 sizse " + shareList2.size());
//        log.info(mapAddrRegObjId.toString());
        //遍历检查所有账户登记及交易存证信息
        for(int k = 0 ;k < shareList2.size(); k++) {
            log.info("test count ----------------------- " + k);
            String tempAddr = JSONObject.fromObject(shareList2.get(k)).getString("address");
            String tempSP = JSONObject.fromObject(shareList2.get(k)).getString("shareProperty");

            log.info("test count ----------------------- " + tempAddr + tempSP + k);
//            log.info(mapAddrRegObjId.toString());
            String tempObjId = mapAddrRegObjId.get(tempAddr + tempSP + k).toString();
            register_subject_account_ref = "SH" + mapAccAddr.get(tempAddr);
            Map mapChkKeys = new HashMap();
            mapChkKeys.put("address","");
            mapChkKeys.put("txId",txId);
            mapChkKeys.put("objectid",tempObjId);
            mapChkKeys.put("version","0");
            mapChkKeys.put("contentType",regType);
            mapChkKeys.put("subProdSubType","");
            mapChkKeys.put("operationType","create");

            Map mapKeyUpdate = new HashMap();
            mapKeyUpdate.put("register_subject_account_ref","SH" + mapAccAddr.get(tempAddr));
            mapKeyUpdate.put("register_serial_number", tempObjId);//区分同一账户多次登记

            String json = JSON.toJSONString(mapKeyUpdate);
            mapChkKeys.put("updateMap",json);
            assertEquals("检查数据",true,gdCF.bCheckJGParams(mapChkKeys));
            bUriMapExist = true;
        }

        store.GetHeight();
        bUriMapExist = false;
    }

//    public void issueStepTimesWithoutObjectID(int nRegTimes,int nRegStep,int yRegStep)throws Exception{
    @Test
    public void issueStepTimesWithoutRegObjectID()throws Exception{
        int nRegTimes = 100;//100
        int nRegStep = 200;//200
        int yRegStep =1;
        store.GetHeight();
        mapAddrRegObjId.clear(); //先清空map 这样后面map中拿到的就是此次发行的登记对象标识
        bSaveBuff = true;
        gdEquityCode = "gdEC" + Random(13);

        indexReg = String.valueOf(index);
        List<Map> shareList2 = gdConstructShareListN(gdAccount1,1000,0);
        uf.shareIssue(gdEquityCode,shareList2,true);

        for(int i =0;i< nRegTimes;i++) {
            log.info("test count +++++++++++++++++++++++ " + i);
            List<Map> shareListTemp = formatIssueListN(nRegStep);
            String resp = gd.GDShareIssue(gdContractAddress,gdPlatfromKeyID,gdEquityCode,shareListTemp);
            assertEquals("200",JSONObject.fromObject(resp).getString("state"));

            for(int size=0;size<shareListTemp.size();size++) {
                shareList2.add(shareListTemp.get(size));
            }
        }

        bSaveBuff = false;
        List<Map>  shareList3 = formatIssueListN(yRegStep);
        //发行已经发行过的股权代码
        String response = gd.GDShareIssue(gdContractAddress,gdPlatfromKeyID,gdEquityCode,shareList3);
        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        for(int size=0;size<shareList3.size();size++) {
            shareList2.add(shareList3.get(size));
        }

        Map uriInfo = gdCF.getJGURIStoreHash(txId,"uri",1);
        log.info(uriInfo.toString());
        assertEquals("检查报送数据个数",shareList2.size(),JSONArray.fromObject(uriInfo.get("storeData").toString()).size());

        store.GetHeight();
    }


    @Test
    public void issueStepTimesWithoutObjectIDRestartProcess()throws Exception{
        int nRegTimes = 100;//100
        int nRegStep = 20;//200
        int yRegStep =1;
        store.GetHeight();
        mapAddrRegObjId.clear(); //先清空map 这样后面map中拿到的就是此次发行的登记对象标识
        bSaveBuff = true;
        gdEquityCode = "gdEC" + Random(13);

        indexReg = String.valueOf(index);
        List<Map> shareList2 = gdConstructShareListN(gdAccount1,1000,0);
        uf.shareIssue(gdEquityCode,shareList2,true);

        for(int i =0;i< nRegTimes;i++) {
            log.info("test count +++++++++++++++++++++++ " + i);
            List<Map> shareListTemp = formatIssueListN(nRegStep);
            String resp = gd.GDShareIssue(gdContractAddress,gdPlatfromKeyID,gdEquityCode,shareListTemp);
            assertEquals("200",JSONObject.fromObject(resp).getString("state"));

            for(int size=0;size<shareListTemp.size();size++) {
                shareList2.add(shareListTemp.get(size));
            }

            if(i == nRegTimes/3){
                UtilsClass utilsClassTemp = new UtilsClass();
                utilsClassTemp.setAndRestartPeerList();
                sleepAndSaveInfo(SLEEPTIME*2);
            }
        }

        bSaveBuff = false;
        List<Map>  shareList3 = formatIssueListN(yRegStep);
        //发行已经发行过的股权代码
        String response = gd.GDShareIssue(gdContractAddress,gdPlatfromKeyID,gdEquityCode,shareList3);
        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        for(int size=0;size<shareList3.size();size++) {
            shareList2.add(shareList3.get(size));
        }

        Map uriInfo = gdCF.getJGURIStoreHash(txId,"uri",1);
        log.info(uriInfo.toString());
        assertEquals("检查报送数据个数",shareList2.size(),JSONArray.fromObject(uriInfo.get("storeData").toString()).size());


        store.GetHeight();
    }


    @Test
    public void issueTemStoreTrueNoCommit()throws Exception{
        store.GetHeight();
        mapAddrRegObjId.clear(); //先清空map 这样后面map中拿到的就是此次发行的登记对象标识
        bSaveBuff = true;
        gdEquityCode = "gdEC" + Random(13);

        indexReg = String.valueOf(index);
        List<Map> shareList2 = gdConstructShareList(gdAccount1,1000,0);
        String response= uf.shareIssue(gdEquityCode,shareList2,false);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String txId = jsonObject.getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        //遍历检查所有账户登记及交易存证信息
        for(int k = 0 ;k < shareList2.size(); k++) {
            log.info("test count ----------------------- " + k);
            String tempAddr = JSONObject.fromObject(shareList2.get(k)).getString("address");
            String tempSP = JSONObject.fromObject(shareList2.get(k)).getString("shareProperty");
            String tempObjId = mapAddrRegObjId.get(tempAddr + tempSP + k).toString();


            Map uriInfo = gdCF.getJGURIStoreHash(txId,conJGFileName(tempObjId,"0"),1);
            assertEquals(false,uriInfo.toString().contains(tempObjId));

        }

        store.GetHeight();
    }

    /**
     * 20210222
     * 使用重复的股权代码 重复报送登记 20210224 与开发确认允许登记报送后再登记报送
     * 先报送登记1个 再次报送登记1个
     * @throws Exception
     */
    @Test
    public void issueCommitRepeatSameEquityCode()throws Exception{
        bSaveBuff = false;
        gdEquityCode = "gdEC" + Random(13);
        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        //发行已经发行过的股权代码
        String response = uf.shareIssue(gdEquityCode,shareList,true);

        sleepAndSaveInfo(3000);

        shareList = gdConstructShareList(gdAccount2,1000,0);
        response = gd.GDShareIssue(gdContractAddress,gdPlatfromKeyID,gdEquityCode,shareList);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
    }

//    @Test
    public void makeDatas()throws Exception{
        for(int i = 0;i< 5000;i++){
            log.info("+++++++++++++++++++++++++++++++++++++++++++++++++++" + i);
            issue200();
        }
    }

    /***
     * 发行不同股权代码性质的股权 一个股东
     */

    @Test
    public void issueOneHolderDiffPropertyEquityCode()throws Exception{
        gdEquityCode = "gdEC1" + Random(10);
        gdCompanyID = "gdSub1" + Random(10);
        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        shareList = gdConstructShareList(gdAccount1,1000,1,shareList);
        shareList = gdConstructShareList(gdAccount1,1000,2,shareList);
        shareList = gdConstructShareList(gdAccount1,1000,3,shareList);
        shareList = gdConstructShareList(gdAccount1,1000,4,shareList);
        shareList = gdConstructShareList(gdAccount1,1000,5,shareList);
        shareList = gdConstructShareList(gdAccount1,1000,6,shareList);
        shareList = gdConstructShareList(gdAccount1,1000,7,shareList);
        shareList = gdConstructShareList(gdAccount1,1000,8,shareList);
        shareList = gdConstructShareList(gdAccount1,1000,10,shareList);
        shareList = gdConstructShareList(gdAccount1,1000,21,shareList);
        shareList = gdConstructShareList(gdAccount1,1000,32,shareList);
        shareList = gdConstructShareList(gdAccount1,1000,43,shareList);
        shareList = gdConstructShareList(gdAccount1,1000,54,shareList);
        shareList = gdConstructShareList(gdAccount1,1000,65,shareList);

        //发行已经发行过的股权代码
        String response = uf.shareIssue(gdEquityCode,shareList,true);


        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");

        JSONArray jsonArrayGet = JSONObject.fromObject(query).getJSONArray("data");
        assertEquals("15000",getTotalAmountFromShareList(jsonArrayGet));

        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,1000,0,0,mapShareENCN().get("0"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount1,1000,6,0,mapShareENCN().get("6"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount1,1000,43,0,mapShareENCN().get("43"),respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount1,1000,1,0,mapShareENCN().get("1"), respShareList);
        respShareList2 = gdConstructQueryShareList(gdAccount1,1000,7,0,"", respShareList2);
        respShareList2 = gdConstructQueryShareList(gdAccount1,1000,54,0,mapShareENCN().get("54"), respShareList2);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount1,1000,2,0,mapShareENCN().get("2"), respShareList2);
        respShareList3 = gdConstructQueryShareList(gdAccount1,1000,8,0,"", respShareList3);
        respShareList3 = gdConstructQueryShareList(gdAccount1,1000,65,0,mapShareENCN().get("65"), respShareList3);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount1,1000,3,0,mapShareENCN().get("3"), respShareList3);
        respShareList4 = gdConstructQueryShareList(gdAccount1,1000,10,0,"", respShareList4);
        List<Map> respShareList5 = gdConstructQueryShareList(gdAccount1,1000,4,0,mapShareENCN().get("4"), respShareList4);
        respShareList5 = gdConstructQueryShareList(gdAccount1,1000,21,0,mapShareENCN().get("21"), respShareList5);
        List<Map> respShareList6 = gdConstructQueryShareList(gdAccount1,1000,5,0,mapShareENCN().get("5"), respShareList5);
        respShareList6 = gdConstructQueryShareList(gdAccount1,1000,32,0,mapShareENCN().get("32"), respShareList6);

        log.info(respShareList6.toString());
        //检查存在余额的股东列表
        assertEquals(respShareList6.size(),dataShareList.size());

        List<Map> getShareList = getShareListFromQueryNoZeroAcc(dataShareList);

        assertEquals(respShareList6.size(),getShareList.size());
        assertEquals(true,respShareList6.containsAll(getShareList) && getShareList.containsAll(respShareList6));


        //查询股东持股情况 无当前股权代码信息
        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":2,\"sharePropertyCN\":\"" + mapShareENCN().get("2") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":3,\"sharePropertyCN\":\"" + mapShareENCN().get("3") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":4,\"sharePropertyCN\":\"" + mapShareENCN().get("4") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":5,\"sharePropertyCN\":\"" + mapShareENCN().get("5") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

    }


    /***
     * 股权代码大小写敏感性检查
     */

    @Test
    public void issue_MatchCase()throws Exception{

        String query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        assertEquals(false,query.contains(gdEquityCode.toLowerCase()));
        assertEquals(false,query.contains(gdEquityCode.toUpperCase()));
        String code = gdEquityCode;
        query = gd.GDGetEnterpriseShareInfo(code.toLowerCase());
        assertEquals("400",JSONObject.fromObject(query).getString("state"));

        query = gd.GDGetEnterpriseShareInfo(code.toUpperCase());
        assertEquals("400",JSONObject.fromObject(query).getString("state"));


        //大小写匹配检查
        gdEquityCode = code.toLowerCase();
        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,1, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,1, shareList3);
        uf.shareIssue(gdEquityCode,shareList4,true);

        gdEquityCode = code.toUpperCase();
        List<Map> shareList21 = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList22 = gdConstructShareList(gdAccount2,1000,1, shareList21);
        List<Map> shareList23 = gdConstructShareList(gdAccount3,1000,0, shareList22);
        List<Map> shareList24 = gdConstructShareList(gdAccount4,1000,1, shareList23);
        uf.shareIssue(gdEquityCode,shareList24,true);

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        assertEquals(true,query.contains(code.toLowerCase()));
        assertEquals(true,query.contains(code.toUpperCase()));

        query = gd.GDGetEnterpriseShareInfo(code.toLowerCase());
        assertEquals("200",JSONObject.fromObject(query).getString("state"));

        query = gd.GDGetEnterpriseShareInfo(code.toUpperCase());
        assertEquals("200",JSONObject.fromObject(query).getString("state"));

    }


    /***
     * 股权代码包含特殊字符
     */

    @Test
    public void issueWithSpecialChar_TC2519()throws Exception{

        List<Map> shareList = new ArrayList<>();
        //股权代码为特殊字符
        gdEquityCode = "@" + Random(6); String EC1 = gdEquityCode;
        shareList.clear();
        shareList = gdConstructShareList(gdAccount1,1000,0);
        uf.shareIssue(gdEquityCode,shareList,true);

        gdEquityCode = "%" + Random(6);String EC2 = gdEquityCode;
        shareList.clear();
        shareList = gdConstructShareList(gdAccount1,1000,0);
        uf.shareIssue(gdEquityCode,shareList,true);

        gdEquityCode = "#" + Random(6);String EC3 = gdEquityCode;
        shareList.clear();
        shareList = gdConstructShareList(gdAccount1,1000,0);
        uf.shareIssue(gdEquityCode,shareList,true);

        gdEquityCode = "_" + Random(6);String EC4 = gdEquityCode;
        shareList.clear();
        shareList = gdConstructShareList(gdAccount1,1000,0);
        uf.shareIssue(gdEquityCode,shareList,true);

        gdEquityCode = "|" + Random(6);String EC5 = gdEquityCode;
        shareList.clear();
        shareList = gdConstructShareList(gdAccount1,1000,0);
        uf.shareIssue(gdEquityCode,shareList,true);

        gdEquityCode = "^" + Random(6);String EC6 = gdEquityCode;
        shareList.clear();
        shareList = gdConstructShareList(gdAccount1,1000,0);
        uf.shareIssue(gdEquityCode,shareList,true);


        String query = gd.GDGetEnterpriseShareInfo(EC1);
        assertEquals(1,JSONObject.fromObject(query).getJSONArray("data").size());

        query = gd.GDGetEnterpriseShareInfo(EC2);
        assertEquals(1,JSONObject.fromObject(query).getJSONArray("data").size());

        query = gd.GDGetEnterpriseShareInfo(EC3);
        assertEquals(1,JSONObject.fromObject(query).getJSONArray("data").size());

        query = gd.GDGetEnterpriseShareInfo(EC4);
        assertEquals(1,JSONObject.fromObject(query).getJSONArray("data").size());

        query = gd.GDGetEnterpriseShareInfo(EC5);
        assertEquals(1,JSONObject.fromObject(query).getJSONArray("data").size());

        query = gd.GDGetEnterpriseShareInfo(EC6);
        assertEquals(1,JSONObject.fromObject(query).getJSONArray("data").size());
    }

    /***
     * 最小登记额度1测试
     */

    @Test
    public void issueMin_TC2492()throws Exception{

        List<Map> shareList = gdConstructShareList(gdAccount1,1,0);
        shareList = gdConstructShareList(gdAccount1,1,1,shareList);

        uf.shareIssue(gdEquityCode,shareList,true);

        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));
        assertEquals(2,JSONObject.fromObject(query).getJSONArray("data").size());
        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");

        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,1,0,0,mapShareENCN().get("0"),respShareList);
        respShareList = gdConstructQueryShareList(gdAccount1,1,1,0,mapShareENCN().get("1"),respShareList);

        log.info(respShareList.toString());
        //检查存在余额的股东列表
        assertEquals(respShareList.size(),dataShareList.size());

        List<Map> getShareList = getShareListFromQueryNoZeroAcc(dataShareList);

        assertEquals(respShareList.size(),getShareList.size());
        assertEquals(true,respShareList.containsAll(getShareList) && getShareList.containsAll(respShareList));


        //查询股东持股情况 无当前股权代码信息
        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        assertEquals(gdAccClientNo1,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));

        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo1 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount1 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1,\"lockAmount\":0}"));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":1,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo3);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo4);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));
//        assertEquals("未查到任何信息",JSONObject.fromObject(query).getString("message"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo5);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo6);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));
    }

}
