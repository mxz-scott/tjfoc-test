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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.functionTest.guDengTestV2.GDCommonFunc.*;
import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassGD.*;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class GDV2_SceneTest_Recycle {

    TestBuilder testBuilder= TestBuilder.getInstance();
    GuDeng gd =testBuilder.getGuDeng();
    Store store =testBuilder.getStore();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    public static String bizNoTest = "test" + Random(12);
    GDUnitFunc uf = new GDUnitFunc();

    @Rule
    public TestName tm = new TestName();

    @BeforeClass
    public static void Before()throws Exception{
        GDBeforeCondition gdBefore = new GDBeforeCondition();
        gdBefore.gdCreateAccout();
        register_event_type = "1";
    }

    @Before
    public void IssueEquity()throws Exception{
        bizNoTest = "test" + Random(12);

        gdCompanyID = CNKey + "Sub2_" + Random(4);
        gdEquityCode = CNKey + "Token2_" + Random(4);

        //重新创建账户
//        gdAccClientNo1 = "No000" + Random(10);
//        gdAccClientNo2 = "No100" + Random(10);
//        gdAccClientNo3 = "No200" + Random(10);
//        gdAccClientNo4 = "No300" + Random(10);
//        gdAccClientNo5 = "No400" + Random(10);
//        gdAccClientNo6 = "No500" + Random(10);
//        gdAccClientNo7 = "No600" + Random(10);
//        gdAccClientNo8 = "No700" + Random(10);
//        gdAccClientNo9 = "No800" + Random(10);
//       gdAccClientNo10 = "No900" + Random(10);
//
//       GDBeforeCondition gdBC = new GDBeforeCondition();
//       gdBC.gdCreateAccout();

//       sleepAndSaveInfo(3000);

        //发行
        uf.commonIssuePP01(1000);//发行给账户1~4 股权性质对应 0 1 0 1
    }

//    @After
    public void calJGDataAfterTx()throws Exception{
        testCurMethodName = tm.getMethodName();
        GDUnitFunc uf = new GDUnitFunc();
        int endHeight = net.sf.json.JSONObject.fromObject(store.GetHeight()).getInt("data");
        uf.checkJGHeaderOpVer(blockHeight,endHeight);
//        uf.updateBlockHeightParam(endHeight);
    }
//    @After
    public void DestroyEquityAndAcc()throws Exception{
        //查询企业所有股东持股情况
        String response = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        String response10 = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        String response11 = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        String response12 = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo3);
        String response13 = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo4);
        String response14 = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo5);
        String response15 = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo6);
        String response16 = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo7);
        String response17 = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo8);
        String response18 = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo9);
        String response19 = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo10);



        //依次回收

        //依次销户

    }

    /***
     * 回收流通股 双花测试
     * 2021/1/5 接口变更为同步接口 故预期结果需要兼容两种
     */

    @Test
    public void shareRecycleDoubleSpend_01()throws Exception{
        List<Map> shareList = gdConstructShareList(gdAccount1,100,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount1,200,0);

        String response1 = uf.shareRecycle(gdEquityCode,shareList,false);
        String txId1 = JSONObject.fromObject(response1).getJSONObject("data").getString("txId");
        String response2 = uf.shareRecycle(gdEquityCode,shareList2,false);
        if(!JSONObject.fromObject(response2).getString("state").equals("200")){
            assertEquals(true,JSONObject.fromObject(response2).getString("data").contains("Err:double spend"));
            sleepAndSaveInfo(SLEEPTIME);
            assertEquals("200", JSONObject.fromObject(store.GetTxDetail(txId1)).getString("state"));
        }
        else {
            String txId2 = JSONObject.fromObject(response2).getJSONObject("data").getString("txId");
            sleepAndSaveInfo(SLEEPTIME);
            assertEquals("200", JSONObject.fromObject(store.GetTxDetail(txId1)).getString("state"));
            assertEquals("200", JSONObject.fromObject(store.GetTxDetail(txId2)).getString("state"));
        }

//        //异或判断两种其中只有一个上链
//        assertEquals(true,JSONObject.fromObject(store.GetTxDetail(txId1)).getString("state").equals("200")
//                ^JSONObject.fromObject(store.GetTxDetail(txId2)).getString("state").equals("200"));

        gd.GDGetEnterpriseShareInfo(gdEquityCode);
    }

    /***
     * 回收高管股 双花测试
     */

    @Test
    public void shareRecycleDoubleSpend_02()throws Exception{

        List<Map> shareList = gdConstructShareList(gdAccount2,100,1);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,200,1);

        String response1 = uf.shareRecycle(gdEquityCode,shareList,false);
        String txId1 = JSONObject.fromObject(response1).getJSONObject("data").getString("txId");
        String response2 = uf.shareRecycle(gdEquityCode,shareList2,false);
        if(!JSONObject.fromObject(response2).getString("state").equals("200")){
            assertEquals(true,JSONObject.fromObject(response2).getString("data").contains("Err:double spend"));
            sleepAndSaveInfo(SLEEPTIME);
            assertEquals("200", JSONObject.fromObject(store.GetTxDetail(txId1)).getString("state"));
        }
        else {
            String txId2 = JSONObject.fromObject(response2).getJSONObject("data").getString("txId");
            sleepAndSaveInfo(SLEEPTIME);
            assertEquals("200", JSONObject.fromObject(store.GetTxDetail(txId1)).getString("state"));
            assertEquals("200", JSONObject.fromObject(store.GetTxDetail(txId2)).getString("state"));
        }

//        //异或判断两种其中只有一个上链
//        assertEquals(true,JSONObject.fromObject(store.GetTxDetail(txId1)).getString("state").equals("200")
//                ^JSONObject.fromObject(store.GetTxDetail(txId2)).getString("state").equals("200"));

        gd.GDGetEnterpriseShareInfo(gdEquityCode);
    }


    /***
     * 冻结部分后 回收流通股
     */

    @Test
    public void shareRecycle_withLock_01()throws Exception{
        String response = "";
        //冻结流通股 *500
        String bizNoTemp = "2000" + Random(12);
        uf.lock(bizNoTemp,gdAccount1,gdEquityCode,500,0,"2022-09-03",true);

        //当前可用余额500 回收小于可用余额
        List<Map> shareList = gdConstructShareList(gdAccount1,100,0);
        uf.shareRecycle(gdEquityCode,shareList,true);

        sleepAndSaveInfo(3000);
        //当前可用余额500 回收大于可用余额
        shareList = gdConstructShareList(gdAccount1,500,0);
        response = uf.shareRecycle(gdEquityCode,shareList,false);

        assertEquals(true,response.contains("Err:账户可用余额不足"));
        //链上校验
//        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        sleepAndSaveInfo(SLEEPTIME);
//        assertEquals("404",JSONObject.fromObject(store.GetTxDetail(
//                    JSONObject.fromObject(response).getJSONObject("data").getString("txId"))).getString("state"));


        //当前可用余额400 回收等于可用余额
        shareList = gdConstructShareList(gdAccount1,400,0);
        uf.shareRecycle(gdEquityCode,shareList,true);

    }

    /***
     * 冻结全部后 回收流通股
     */

    @Test
    public void shareRecycle_withLock_05()throws Exception{
        String response = "";
        //全部冻结
        String bizNoTemp = "2000" + Random(12);
        uf.lock(bizNoTemp,gdAccount1,gdEquityCode,1000,0,"2022-09-03",true);


        //尝试回收
        List<Map> shareList = gdConstructShareList(gdAccount1,100,0);
        response = uf.shareRecycle(gdEquityCode,shareList,false);

        if(JSONObject.fromObject(response).getString("state").equals("200")) {
            sleepAndSaveInfo(SLEEPTIME);
            assertEquals("404",JSONObject.fromObject(store.GetTxDetail(
                    JSONObject.fromObject(response).getJSONObject("data").getString("txId"))).getString("state"));
        }
        else{
            assertEquals(true,response.contains("Err:账户可用余额不足"));
//            assertEquals("501",JSONObject.fromObject(response).getString("state"));
        }

    }

    /***
     * 回收流通股超出余额
     */

    @Test
    public void shareRecycle_NotEnough()throws Exception{
        String response = "";
        //尝试回收超过余额
        List<Map> shareList = gdConstructShareList(gdAccount1,10000,0);
        response = uf.shareRecycle(gdEquityCode,shareList,false);

        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(gdAccount1 + "：余额不足",JSONObject.fromObject(response).getString("message"));

    }

    /***
     * 回收账户没有的其他性质股份
     */

    @Test
    public void shareRecycle_NotEnough_02()throws Exception{
        String response = "";
        //
        List<Map> shareList = gdConstructShareList(gdAccount6,100,1);
        response = uf.shareRecycle(gdEquityCode,shareList,false);

        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(gdAccount6 + "：余额不足",JSONObject.fromObject(response).getString("message"));

    }

    /***
     * 冻结后 回收  流通股
     */

    @Test
    public void shareRecycle_withLock_02()throws Exception{
        String response = "";
        //冻结流通股 *500
        String bizNoTemp = "2000" + Random(12);
        uf.lock(bizNoTemp,gdAccount1,gdEquityCode,500,0,"2022-09-03",true);

        //当前可用余额500 回收小于可用余额
        List<Map> shareList = gdConstructShareList(gdAccount1,100,0);
        uf.shareRecycle(gdEquityCode,shareList,true);

        sleepAndSaveInfo(2000);

        //当前可用余额500 回收大于可用余额
        shareList = gdConstructShareList(gdAccount1,500,0);
        response = uf.shareRecycle(gdEquityCode,shareList,false);



        if(JSONObject.fromObject(response).getString("state").equals("200")) {
            sleepAndSaveInfo(SLEEPTIME);
            assertEquals("404",JSONObject.fromObject(store.GetTxDetail(
                    JSONObject.fromObject(response).getJSONObject("data").getString("txId"))).getString("state"));
        }
        else{
            assertEquals(true,response.contains("Err:账户可用余额不足"));
//            assertEquals("501",JSONObject.fromObject(response).getString("state"));
        }

        //当前可用余额500 回收等于可用余额
        shareList = gdConstructShareList(gdAccount1,100,0);
        uf.shareRecycle(gdEquityCode,shareList,true);
    }


    /***
     * 冻结后 回收  高管股股
     * 若高管股存在冻结 可回收可用部分
     */

    @Test
    public void shareRecycle_withLock_03()throws Exception{
        String response = "";
        //冻结高管股 * 1
        String bizNoTemp = "2000" + Random(12);
        uf.lock(bizNoTemp,gdAccount2,gdEquityCode,1,1,"2022-09-03",true);

        //回收小于可用余额
        List<Map> shareList = gdConstructShareList(gdAccount2,100,1);
        uf.shareRecycle(gdEquityCode,shareList,true);

        //回收大于可用余额
        shareList = gdConstructShareList(gdAccount2,1000,1);
        response = uf.shareRecycle(gdEquityCode,shareList,false);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(gdAccount2 + "：余额不足",JSONObject.fromObject(response).getString("message"));

        //回收等于可用余额
        shareList = gdConstructShareList(gdAccount2,899,1);
        uf.shareRecycle(gdEquityCode,shareList,true);
    }

    /***
     * 账户包含流通股 高管股
     * 分别冻结一部分的流通股和高管股
     * 高管股可以回收可用部分
     * 流通股则可以回收可用部分
     */

    @Test
    public void shareRecycle_withLock_04()throws Exception{
        String response = "";

        //转入余额
        uf.shareTransfer(gdAccountKeyID1,gdAccount1,1000,gdAccount5,0,
                gdEquityCode,true);
        uf.shareTransfer(gdAccountKeyID2,gdAccount2,1000,gdAccount5,1,
                gdEquityCode,true);


        //冻结高管股 * 100 流通股*100
        String bizNoTemp = "2000" + Random(12);
        uf.lock(bizNoTemp,gdAccount5,gdEquityCode,100,0,"2022-09-03",true);

        bizNoTemp = "2000" + Random(12);
        uf.lock(bizNoTemp,gdAccount5,gdEquityCode,100,1,"2022-09-03",true);


        //高管股回收小于可用余额
        List<Map> shareList = gdConstructShareList(gdAccount5,100,1);
        uf.shareRecycle(gdEquityCode,shareList,true);


        //高管股回收回收等于可用余额
        shareList = gdConstructShareList(gdAccount5,800,1);
        uf.shareRecycle(gdEquityCode,shareList,true);


        //流通股回收小于可用余额
        shareList = gdConstructShareList(gdAccount5,100,0);
        uf.shareRecycle(gdEquityCode,shareList,true);

        //高管股回收回收大于可用余额
        shareList = gdConstructShareList(gdAccount5,1000,0);
        response = uf.shareRecycle(gdEquityCode,shareList,false);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals(gdAccount5 + "：余额不足",JSONObject.fromObject(response).getString("message"));

        //高管股回收转等于可用余额
        shareList = gdConstructShareList(gdAccount5,800,0);
        uf.shareRecycle(gdEquityCode,shareList,true);

    }

    /***
     * 股权代码大小写敏感性检查
     */

    @Test
    public void recycle_MatchCase()throws Exception{
        List<Map> shareList = gdConstructShareList(gdAccount4,100,1);

        //大小写匹配检查
        String response = uf.shareRecycle(gdEquityCode.toLowerCase(),shareList,false);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("股权代码还未发行或者已经转场",JSONObject.fromObject(response).getString("message"));
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("该股权代码:" + gdEquityCode.toLowerCase() + "还未发行或者已经转板",JSONObject.fromObject(response).getString("message"));

        response = uf.shareRecycle(gdEquityCode.toUpperCase(),shareList,false);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("股权代码还未发行或者已经转场",JSONObject.fromObject(response).getString("message"));
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("该股权代码:" + gdEquityCode.toUpperCase() + "还未发行或者已经转板",JSONObject.fromObject(response).getString("message"));
    }


    /***
     * 回收 shareList异常测试
     */

    @Test
    public void recycle_TC2403()throws Exception{
        List<Map> shareList = gdConstructShareList(gdAccount1,-100,0);
        List<Map> shareList2 = gdConstructShareList("",1000,0);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0,shareList);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,0, shareList2);

        String response = uf.shareRecycle(gdEquityCode,shareList3,false);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:json: cannot unmarshal number -100 into Go struct field AddressInfo.AddressList.Amount of type uint64", JSONObject.fromObject(response).getString("message"));


        response = uf.shareIncrease(gdEquityCode,shareList4,false);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, JSONObject.fromObject(response).getString("message").contains("Error:Field validation for 'Address' failed on the 'required"));

        shareList.clear();
        response = uf.shareIncrease(gdEquityCode,shareList,false);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));
        assertEquals("请填写股权账号信息", JSONObject.fromObject(response).getString("message"));

    }


    /***
     * 异常测试
     * 回收使用错误的地址
     */

    @Test
    public void recycle_TC2401()throws Exception{
        //错误的地址信息
        List<Map> shareList = gdConstructShareList("11111111",100,0);

        String response = uf.shareRecycle(gdEquityCode,shareList,false);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));
//        assertEquals("11111111" + "：余额不足", JSONObject.fromObject(response).getString("message"));
    }

    /***
     * 异常测试
     * 回收使用错误的股权代码
     */

    @Test
    public void recycle_TC2400()throws Exception{
        //错误的 或者不存在的股权代码
        List<Map> shareList = gdConstructShareList(gdAccount1,100,0);

        String tempEqcode = gdEquityCode + Random(5);
        String response = uf.shareRecycle(tempEqcode,shareList,false);

        //以下暂为错误的提示信息 提bug优化后测试再修改 20200911
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("股权代码还未发行或者已经转场",JSONObject.fromObject(response).getString("message"));
//        assertEquals("400", JSONObject.fromObject(response).getString("state"));
//        assertEquals("该股权代码:" + tempEqcode + "还未发行或者已经转板", JSONObject.fromObject(response).getString("message"));
    }


    /***
     * 异常测试
     * 回收使用客户的keyID
     */

    @Test
    public void recycle_TC2399()throws Exception{
        //错误的 或者不存在的股权代码
        List<Map> shareList = gdConstructShareList(gdAccount1,100,0);

        String response = gd.GDShareRecycle(gdAccountKeyID1,gdEquityCode,shareList,"111");

        assertEquals("505", JSONObject.fromObject(response).getString("state"));
        assertEquals("数字签名出错", JSONObject.fromObject(response).getString("message"));

    }

    @Test
    public void recycleLongRegObjectID()throws Exception{
        GDBeforeCondition gdBF = new GDBeforeCondition();
        String regObjId = "5" + mapAccAddr.get(gdAccount1) + Random(128);
        Map tempReg = gdBF.init05RegInfo();
        tempReg.put("register_registration_object_id",regObjId);
        tempReg.put("register_subject_account_ref","SH" + mapAccAddr.get(gdAccount1));

        Map<String,Object> shares = new HashMap<>();
        shares.put("address",gdAccount1);
        shares.put("amount",100);
        shares.put("shareProperty",0);
        shares.put("createTime",ts5);
        shares.put("registerInformation",tempReg);

        List<Map> shareList = new ArrayList<>();
        shareList.add(shares);

        String response = uf.shareRecycle(gdEquityCode,shareList,false);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));
        assertEquals(true,response.contains("header.content.object_id: String length must be less than or equal to 128"));

        sleepAndSaveInfo(4000);

        //检查账户余额
        response = gd.GDGetShareHolderInfo(gdContractAddress, gdAccClientNo1);
        assertEquals(true, response.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));
    }


    /**
     * 同时回收多个 但完全回收1个
     * @throws Exception
     */
    @Test
    public void shareRecycle_multiAddr01()throws Exception{
        GDBeforeCondition gdBF = new GDBeforeCondition();
        GDCommonFunc gdCF = new GDCommonFunc();
        String eqCode = gdEquityCode;
        String remark = "777777";

        log.info("多个回收前查询机构主体信息");
        String query2 = gd.GDObjectQueryByVer(gdCompanyID,-1);
        int totalMembers = JSONObject.fromObject(query2).getJSONObject("data").getJSONObject("body"
        ).getJSONObject("subject_information").getJSONObject("organization_subject_information"
        ).getJSONObject("basic_information_of_enterprise").getJSONObject("basic_information_description"
        ).getInt("subject_shareholders_number");

        String subVerInit = gdCF.getObjectLatestVer(gdCompanyID);//获取初始主体版本

        log.info(registerInfo.toString());
        mapAddrRegObjId.clear(); //先清空map 这样后面map中拿到的就是此次发行的登记对象标识
        register_product_ref = gdEquityCode;
        int beforeBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");

        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,500,1, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,500,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,500,1,shareList3);

        gd.GDGetEnterpriseShareInfo(gdEquityCode);
        log.info("====================================================================================");
        String response= gd.GDShareRecycle(gdPlatfromKeyID,eqCode,shareList4,remark);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        sleepAndSaveInfo(2000);

        gd.GDGetEnterpriseShareInfo(gdEquityCode);
        log.info("====================================================================================");

        query2 = gd.GDObjectQueryByVer(gdCompanyID,-1);
        int totalMembersAft = JSONObject.fromObject(query2).getJSONObject("data").getJSONObject("body"
        ).getJSONObject("subject_information").getJSONObject("organization_subject_information"
        ).getJSONObject("basic_information_of_enterprise").getJSONObject("basic_information_description"
        ).getInt("subject_shareholders_number");

        assertEquals(totalMembers - 1,totalMembersAft);//判断主体数据更新

        txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");

        log.info("================================检查存证数据格式化《开始》================================");

        for(int k = 0;k < shareList4.size();k ++) {
            String tempAddr = JSONObject.fromObject(shareList4.get(k)).getString("address");
            String tempSP = JSONObject.fromObject(shareList4.get(k)).getString("shareProperty");
            String tempObjId = mapAddrRegObjId.get(tempAddr + tempSP).toString();

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

        log.info("检查回收存证主体格式化及信息内容与传入一致");
        Map mapChkKeys = new HashMap();
        assertEquals("更新主体版本非0",false,gdCF.getObjectLatestVer(gdCompanyID).equals("0"));
        mapChkKeys.clear();
        mapChkKeys.put("address","");
        mapChkKeys.put("txId",txId);
        mapChkKeys.put("objectid",gdCompanyID);
        mapChkKeys.put("version",gdCF.getObjectLatestVer(gdCompanyID));
        mapChkKeys.put("contentType",subjectType);
        mapChkKeys.put("subProdSubType","1");
        mapChkKeys.put("operationType","update");
        Map mapKeyUpdate =  new HashMap();
        mapKeyUpdate.put("subject_shareholders_number",totalMembersAft);
        String json = JSON.toJSONString(mapKeyUpdate);
        mapChkKeys.put("updateMap",json);

        assertEquals("检查数据-主体",true,gdCF.bCheckJGParams(mapChkKeys));

        log.info("================================检查存证数据格式化《结束》================================");


        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");

        //查询股东持股情况 无当前股权代码信息
        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        assertEquals(gdAccClientNo1,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
//        String get = JSONObject.fromObject(JSONObject.fromObject(query).getJSONObject("data").getJSONArray("accountList").get(0)).getString("shareholderNo");
//        assertEquals("SH"+gdAccClientNo1,get);

        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo1 + "\""));
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(gdAccClientNo2,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo2 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount2 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":500,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo3);
        assertEquals(gdAccClientNo3,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo3 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount3 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":500,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo4);
        assertEquals(gdAccClientNo4,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo4 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount4 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":500,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo5);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo6);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        log.info("多个回收后查询机构主体信息");
        String query3 = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);
//        BigDecimal totalShares2 = new BigDecimal(JSONObject.fromObject(query3).getJSONObject("data").getString("subject_total_share_capital"));
//
//        log.info("判断增发前后机构主体查询总股本数增加数正确");
//        assertEquals(totalShares.subtract(new BigDecimal("400")),totalShares2);
    }


    /**
     * 多次回收 但每次完全回收1个
     * @throws Exception
     */
    @Test
    public void shareRecycle_multiRecycle()throws Exception{
        GDBeforeCondition gdBF = new GDBeforeCondition();
        GDCommonFunc gdCF = new GDCommonFunc();
        String eqCode = gdEquityCode;
        String remark = "777777";

        log.info("多个回收前查询机构主体信息");
        String query2 = gd.GDObjectQueryByVer(gdCompanyID,-1);
        int totalMembers = JSONObject.fromObject(query2).getJSONObject("data").getJSONObject("body"
        ).getJSONObject("subject_information").getJSONObject("organization_subject_information"
        ).getJSONObject("basic_information_of_enterprise").getJSONObject("basic_information_description"
        ).getInt("subject_shareholders_number");

        String subVerInit = gdCF.getObjectLatestVer(gdCompanyID);//获取初始主体版本

        log.info(registerInfo.toString());
        mapAddrRegObjId.clear(); //先清空map 这样后面map中拿到的就是此次发行的登记对象标识
        register_product_ref = gdEquityCode;
        int beforeBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");

        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,500,1, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,500,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,500,1,shareList3);

        gd.GDGetEnterpriseShareInfo(gdEquityCode);
        log.info("====================================================================================");
        String response= gd.GDShareRecycle(gdPlatfromKeyID,eqCode,shareList4,remark);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));

        sleepAndSaveInfo(2000);

        gd.GDGetEnterpriseShareInfo(gdEquityCode);
        log.info("====================================================================================");

        query2 = gd.GDObjectQueryByVer(gdCompanyID,-1);
        int totalMembersAft = JSONObject.fromObject(query2).getJSONObject("data").getJSONObject("body"
        ).getJSONObject("subject_information").getJSONObject("organization_subject_information"
        ).getJSONObject("basic_information_of_enterprise").getJSONObject("basic_information_description"
        ).getInt("subject_shareholders_number");

        assertEquals(totalMembers - 1,totalMembersAft);//判断主体数据更新

        //第二次回收
        List<Map> shareList12 = gdConstructShareList(gdAccount2,500,1);
        List<Map> shareList13 = gdConstructShareList(gdAccount3,100,0, shareList12);
        List<Map> shareList14 = gdConstructShareList(gdAccount4,100,1,shareList13);
        response= gd.GDShareRecycle(gdPlatfromKeyID,eqCode,shareList14,remark);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        sleepAndSaveInfo(2000);

        gd.GDGetEnterpriseShareInfo(gdEquityCode);
        log.info("====================================================================================");

        query2 = gd.GDObjectQueryByVer(gdCompanyID,-1);
        totalMembersAft = JSONObject.fromObject(query2).getJSONObject("data").getJSONObject("body"
        ).getJSONObject("subject_information").getJSONObject("organization_subject_information"
        ).getJSONObject("basic_information_of_enterprise").getJSONObject("basic_information_description"
        ).getInt("subject_shareholders_number");

        assertEquals(totalMembers - 2,totalMembersAft);//判断主体数据更新



        //第三次回收
        List<Map> shareList23 = gdConstructShareList(gdAccount3,400,0);
        List<Map> shareList24 = gdConstructShareList(gdAccount4,100,1,shareList23);
        response = gd.GDShareRecycle(gdPlatfromKeyID,eqCode,shareList24,remark);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));

        sleepAndSaveInfo(2000);

        gd.GDGetEnterpriseShareInfo(gdEquityCode);
        log.info("====================================================================================");

        query2 = gd.GDObjectQueryByVer(gdCompanyID,-1);
        totalMembersAft = JSONObject.fromObject(query2).getJSONObject("data").getJSONObject("body"
        ).getJSONObject("subject_information").getJSONObject("organization_subject_information"
        ).getJSONObject("basic_information_of_enterprise").getJSONObject("basic_information_description"
        ).getInt("subject_shareholders_number");

        assertEquals(totalMembers - 3,totalMembersAft);//判断主体数据更新




        txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
//
//        log.info("================================检查存证数据格式化《开始》================================");
//
//        for(int k = 0;k < shareList4.size();k ++) {
//            String tempAddr = JSONObject.fromObject(shareList4.get(k)).getString("address");
//            String tempSP = JSONObject.fromObject(shareList4.get(k)).getString("shareProperty");
//            String tempObjId = mapAddrRegObjId.get(tempAddr + tempSP).toString();
//
//            register_subject_account_ref = "SH" + mapAccAddr.get(tempAddr);
//            Map mapChkKeys = new HashMap();
//            mapChkKeys.put("address","");
//            mapChkKeys.put("txId",txId);
//            mapChkKeys.put("objectid",tempObjId);
//            mapChkKeys.put("version","0");
//            mapChkKeys.put("contentType",regType);
//            mapChkKeys.put("subProdSubType","");
//            mapChkKeys.put("operationType","create");
//
//            Map mapKeyUpdate = new HashMap();
//            mapKeyUpdate.put("register_subject_account_ref","SH" + mapAccAddr.get(tempAddr));
//
//            String json = JSON.toJSONString(mapKeyUpdate);
//            mapChkKeys.put("updateMap",json);
//
//            assertEquals("检查数据",true,gdCF.bCheckJGParams(mapChkKeys));
//        }

//        log.info("检查回收存证主体格式化及信息内容与传入一致");
//        Map mapChkKeys = new HashMap();
//        assertEquals("更新主体版本非0",false,gdCF.getObjectLatestVer(gdCompanyID).equals("0"));
//        mapChkKeys.clear();
//        mapChkKeys.put("address","");
//        mapChkKeys.put("txId",txId);
//        mapChkKeys.put("objectid",gdCompanyID);
//        mapChkKeys.put("version",gdCF.getObjectLatestVer(gdCompanyID));
//        mapChkKeys.put("contentType",subjectType);
//        mapChkKeys.put("subProdSubType","1");
//        mapChkKeys.put("operationType","update");
//        Map mapKeyUpdate =  new HashMap();
//        mapKeyUpdate.put("subject_shareholders_number",totalMembersAft);
//        String json = JSON.toJSONString(mapKeyUpdate);
//        mapChkKeys.put("updateMap",json);
//
//        assertEquals("检查数据-主体",true,gdCF.bCheckJGParams(mapChkKeys));

        log.info("================================检查存证数据格式化《结束》================================");


        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");

        //查询股东持股情况 无当前股权代码信息
        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        assertEquals(gdAccClientNo1,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
//        String get = JSONObject.fromObject(JSONObject.fromObject(query).getJSONObject("data").getJSONArray("accountList").get(0)).getString("shareholderNo");
//        assertEquals("SH"+gdAccClientNo1,get);

        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo1 + "\""));
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo3);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));


        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo4);
        assertEquals(gdAccClientNo4,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo4 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount4 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":300,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo5);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo6);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        log.info("多个回收后查询机构主体信息");
        String query3 = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);
//        BigDecimal totalShares2 = new BigDecimal(JSONObject.fromObject(query3).getJSONObject("data").getString("subject_total_share_capital"));
//
//        log.info("判断增发前后机构主体查询总股本数增加数正确");
//        assertEquals(totalShares.subtract(new BigDecimal("400")),totalShares2);
    }

    /**
     * 同时回收多个 但完全回收2个
     * @throws Exception
     */
    @Test
    public void shareRecycle_multiAddr02()throws Exception{
        GDBeforeCondition gdBF = new GDBeforeCondition();
        GDCommonFunc gdCF = new GDCommonFunc();
        String eqCode = gdEquityCode;
        String remark = "777777";

        log.info("多个回收前查询机构主体信息");
        String query2 = gd.GDObjectQueryByVer(gdCompanyID,-1);
        int totalMembers = JSONObject.fromObject(query2).getJSONObject("data").getJSONObject("body"
        ).getJSONObject("subject_information").getJSONObject("organization_subject_information"
        ).getJSONObject("basic_information_of_enterprise").getJSONObject("basic_information_description"
        ).getInt("subject_shareholders_number");

        String subVerInit = gdCF.getObjectLatestVer(gdCompanyID);//获取初始主体版本

        log.info(registerInfo.toString());
        mapAddrRegObjId.clear(); //先清空map 这样后面map中拿到的就是此次发行的登记对象标识
        register_product_ref = gdEquityCode;
        int beforeBlockHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");

        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,1, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,500,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,500,1,shareList3);

        gd.GDGetEnterpriseShareInfo(gdEquityCode);
        log.info("====================================================================================");
        String response= gd.GDShareRecycle(gdPlatfromKeyID,eqCode,shareList4,remark);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        sleepAndSaveInfo(2000);

        gd.GDGetEnterpriseShareInfo(gdEquityCode);
        log.info("====================================================================================");

        query2 = gd.GDObjectQueryByVer(gdCompanyID,-1);
        int totalMembersAft = JSONObject.fromObject(query2).getJSONObject("data").getJSONObject("body"
        ).getJSONObject("subject_information").getJSONObject("organization_subject_information"
        ).getJSONObject("basic_information_of_enterprise").getJSONObject("basic_information_description"
        ).getInt("subject_shareholders_number");

        assertEquals(totalMembers - 2,totalMembersAft);//判断主体数据更新

        txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");

        log.info("================================检查存证数据格式化《开始》================================");

        for(int k = 0;k < shareList4.size();k ++) {
            String tempAddr = JSONObject.fromObject(shareList4.get(k)).getString("address");
            String tempSP = JSONObject.fromObject(shareList4.get(k)).getString("shareProperty");
            String tempObjId = mapAddrRegObjId.get(tempAddr + tempSP).toString();

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

        log.info("检查回收存证主体格式化及信息内容与传入一致");
        Map mapChkKeys = new HashMap();
        assertEquals("更新主体版本非0",false,gdCF.getObjectLatestVer(gdCompanyID).equals("0"));
        mapChkKeys.clear();
        mapChkKeys.put("address","");
        mapChkKeys.put("txId",txId);
        mapChkKeys.put("objectid",gdCompanyID);
        mapChkKeys.put("version",gdCF.getObjectLatestVer(gdCompanyID));
        mapChkKeys.put("contentType",subjectType);
        mapChkKeys.put("subProdSubType","1");
        mapChkKeys.put("operationType","update");
        Map mapKeyUpdate =  new HashMap();
        mapKeyUpdate.put("subject_shareholders_number",totalMembersAft);
        String json = JSON.toJSONString(mapKeyUpdate);
        mapChkKeys.put("updateMap",json);

        assertEquals("检查数据-主体",true,gdCF.bCheckJGParams(mapChkKeys));

        log.info("================================检查存证数据格式化《结束》================================");


        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");


        //查询股东持股情况 无当前股权代码信息
        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        assertEquals(gdAccClientNo1,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
//        String get = JSONObject.fromObject(JSONObject.fromObject(query).getJSONObject("data").getJSONArray("accountList").get(0)).getString("shareholderNo");
//        assertEquals("SH"+gdAccClientNo1,get);

        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo1 + "\""));
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo3);
        assertEquals(gdAccClientNo3,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo3 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount3 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":500,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo4);
        assertEquals(gdAccClientNo4,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo4 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount4 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":500,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo5);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo6);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        log.info("多个回收后查询机构主体信息");
        String query3 = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);
//        BigDecimal totalShares2 = new BigDecimal(JSONObject.fromObject(query3).getJSONObject("data").getString("subject_total_share_capital"));
//
//        log.info("判断增发前后机构主体查询总股本数增加数正确");
//        assertEquals(totalShares.subtract(new BigDecimal("400")),totalShares2);
    }
}
