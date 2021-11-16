package com.tjfintech.common.functionTest.guDengTestV2;

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
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.functionTest.guDengTestV2.GDCommonFunc.*;
import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassGD.*;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
/***
 * 增发异常场景测试用例
 */
public class GDV2_SceneTest_Increase {

    TestBuilder testBuilder= TestBuilder.getInstance();
    GuDeng gd =testBuilder.getGuDeng();
    Store store =testBuilder.getStore();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    public static String bizNoTest = "test" + Random(12);
    GDUnitFunc uf = new GDUnitFunc();
    GDBeforeCondition gdBF = new GDBeforeCondition();
    GDCommonFunc gdCF = new GDCommonFunc();
    String tempregister_transaction_ref = register_transaction_ref;

    @Rule
    public TestName tm = new TestName();

    @BeforeClass
    public static void Before()throws Exception{
        GDBeforeCondition gdBefore = new GDBeforeCondition();
        gdBefore.gdCreateAccout();
        register_event_type = 1;
    }

    @Before
    public void IssueEquity()throws Exception{
        register_event_type = 1;
        bizNoTest = "test" + Random(12);
        tempregister_transaction_ref = register_transaction_ref;
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

    @After
    public void calJGDataAfterTx()throws Exception{
        register_event_type = 1;
        register_transaction_ref = tempregister_transaction_ref;
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
     * 增发 一个不存在的股权代码
     */

    @Test
    public void IncreaseNotExistEquitycode()throws Exception{
        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,0, shareList3);

        String response = uf.shareIncrease(gdEquityCode + Random(12),shareList4,false);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));
        assertEquals("股权代码还未发行或者已经转场", JSONObject.fromObject(response).getString("message"));

    }

    //增发一个不存在的产品对象
    @Test
    public void TC09_shareIncreaseNotExistProductObject() throws Exception {

        log.info("增发前查询机构主体信息");
        String query2 = gd.GDMainSubjectQuery(gdContractAddress, gdCompanyID);
//        BigDecimal totalShares = new BigDecimal(JSONObject.fromObject(query2).getJSONObject("data").getString("subject_total_share_capital"));

        String eqCode = gdEquityCode;
        String reason = "股份分红";
        String txObjId = "increaseObj" + Random(6);

        Map eqProd = gdBF.init03EquityProductInfo();
        Map txInfo = gdBF.init04TxInfo();
        txInfo.put("transaction_object_id", txObjId);
        register_transaction_ref = txObjId; //此处为发行融资 设置登记引用接口中的交易报告

        log.info("发行主体版本  " + gdCF.getObjectLatestVer(gdCompanyID));

        List<Map> shareList = gdConstructShareList(gdAccount5, 1000, 0);

        //测试增发一个不存在的产品对象 2020/12/19
        Map eqPErr = gdBF.init03EquityProductInfo();
        eqPErr.put("product_object_id", "testErr" + Random(3));
        String err = gd.GDShareIncrease(gdPlatfromKeyID, eqCode, shareList, reason, eqPErr, txInfo);
        assertEquals("400", net.sf.json.JSONObject.fromObject(err).getString("state"));

        sleepAndSaveInfo(4000);
        //查询股东持股情况 无当前股权代码信息
        String query = gd.GDGetShareHolderInfo(gdContractAddress, gdAccClientNo5);
        assertEquals(gdAccClientNo5, JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));

        assertEquals(true, query.contains("\"shareholderNo\":\"SH" + gdAccClientNo5 + "\""));
        assertEquals(true, query.contains("\"address\":\"" + gdAccount5 + "\""));
        assertEquals(false, query.contains(gdEquityCode ));

    }

    //增发时交易报告对象长度超长
    @Test
    public void TC09_shareIncreaseLongObjectId() throws Exception {
        register_event_type = 2;
        log.info("增发前查询机构主体信息");
        String query2 = gd.GDMainSubjectQuery(gdContractAddress, gdCompanyID);
//        BigDecimal totalShares = new BigDecimal(JSONObject.fromObject(query2).getJSONObject("data").getString("subject_total_share_capital"));

        String eqCode = gdEquityCode;
        String reason = "股份分红";
        String txObjId = "increaseObj" + Random(126);

        Map eqProd = gdBF.init03EquityProductInfo();
        Map txInfo = gdBF.init04TxInfo();
        txInfo.put("transaction_object_id", txObjId);
        register_transaction_ref = txObjId; //此处为发行融资 设置登记引用接口中的交易报告

        log.info("发行主体版本  " + gdCF.getObjectLatestVer(gdCompanyID));

        List<Map> shareList = gdConstructShareList(gdAccount5, 1000, 0);

        String err = gd.GDShareIncrease(gdPlatfromKeyID, eqCode, shareList, reason, eqProd, txInfo);
        assertEquals("400", net.sf.json.JSONObject.fromObject(err).getString("state"));

        sleepAndSaveInfo(4000);
        //查询股东持股情况 无当前股权代码信息
        String query = gd.GDGetShareHolderInfo(gdContractAddress, gdAccClientNo5);
        assertEquals(gdAccClientNo5, JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));

        assertEquals(true, query.contains("\"shareholderNo\":\"SH" + gdAccClientNo5 + "\""));
        assertEquals(true, query.contains("\"address\":\"" + gdAccount5 + "\""));
        assertEquals(false, query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":1000,\"lockAmount\":0}"));

    }


    /***
     * 增发 shareList异常测试
     */

    @Test
    public void Increase_TC2391()throws Exception{
        List<Map> shareList = gdConstructShareList(gdAccount1,-100,0);
        List<Map> shareList2 = gdConstructShareList("",1000,0);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0,shareList);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,0, shareList2);

        String response = uf.shareIncrease(gdEquityCode,shareList3,false);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));
        assertEquals("无效的参数:json: cannot unmarshal number -100 into Go struct field Shares.ShareList.Amount of type uint64", JSONObject.fromObject(response).getString("message"));


        response = uf.shareIncrease(gdEquityCode,shareList4,false);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, JSONObject.fromObject(response).getString("message").contains("Error:Field validation for 'Address' failed on the 'required' tag"));

        shareList.clear();
        response = uf.shareIncrease(gdEquityCode,shareList,false);
        assertEquals("400", JSONObject.fromObject(response).getString("state"));
        assertEquals("请填写股权账号信息", JSONObject.fromObject(response).getString("message"));

    }

    /***
     * 冻结后 增发
     */

    @Test
    public void IncreaseAfterLock()throws Exception{

        //冻结高管股 * 100 流通股*100
        String bizNoTemp = "2000" + Random(12);
        uf.lock(bizNoTemp,gdAccount1,gdEquityCode,100,0,"2022-09-03",true);

        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);

        bizNoTemp = "2000" + Random(12);
        uf.lock(bizNoTemp,gdAccount2,gdEquityCode,100,1,"2022-09-03",true);

        query = gd.GDGetEnterpriseShareInfo(gdEquityCode);

        bizNoTemp = "2000" + Random(12);
        uf.lock(bizNoTemp,gdAccount3,gdEquityCode,100,0,"2022-09-03",true);

        query = gd.GDGetEnterpriseShareInfo(gdEquityCode);

        bizNoTemp = "2000" + Random(12);
        uf.lock(bizNoTemp,gdAccount4,gdEquityCode,100,1,"2022-09-03",true);

        query = gd.GDGetEnterpriseShareInfo(gdEquityCode);

        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount2,1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount3,1000,0, shareList2);
        List<Map> shareList4 = gdConstructShareList(gdAccount4,1000,0, shareList3);

        String response = uf.shareIncrease(gdEquityCode ,shareList4,true);

        query = gd.GDGetEnterpriseShareInfo(gdEquityCode);

    }

    /***
     * 连续两次增发 2021/01/05 因接口全部修改为同步接口 而且存在登记对象标识唯一的限制
     * 20210929 登记对象标识允许update
     */

    @Test
    public void increaseDouble_01()throws Exception{
        List<Map> shareListIn = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareListIn2 = gdConstructShareList(gdAccount2,1000,1, shareListIn);
        List<Map> shareListIn3 = gdConstructShareList(gdAccount3,1000,0, shareListIn2);
        List<Map> shareListIn4 = gdConstructShareList(gdAccount4,1000,1, shareListIn3);

        String response1 = uf.shareIncrease(gdEquityCode,shareListIn4,false);
        String response2 = uf.shareIncrease(gdEquityCode,shareListIn4,false);
        String txId1 = JSONObject.fromObject(response1).getJSONObject("data").getString("txId");
        assertEquals("200",JSONObject.fromObject(response2).getString("state"));
//        String txId2 = JSONObject.fromObject(response2).getJSONObject("data").getString("txId");

        sleepAndSaveInfo(SLEEPTIME);

        //判断两笔交易均上链
        assertEquals(true,JSONObject.fromObject(store.GetTxDetail(txId1)).getString("state").equals("200"));
//        assertEquals(true,JSONObject.fromObject(store.GetTxDetail(txId2)).getString("state").equals("200"));

        String query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        JSONArray jsonArrayGet = JSONObject.fromObject(query).getJSONArray("data");

        assertEquals("12000",getTotalAmountFromShareList(jsonArrayGet));//报送数据验证失败 但合约交易会执行
    }


    /***
     * 股权代码大小写敏感性检查
     */

    @Test
    public void increase_MatchCase()throws Exception{
        List<Map> shareListIn = gdConstructShareList(gdAccount1,1000,0);

        //大小写匹配检查
        String response = uf.shareIncrease(gdEquityCode.toLowerCase(),shareListIn,false);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("股权代码还未发行或者已经转场",JSONObject.fromObject(response).getString("message"));

        response = uf.shareIncrease(gdEquityCode.toUpperCase(),shareListIn,false);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("股权代码还未发行或者已经转场",JSONObject.fromObject(response).getString("message"));
    }


    /***
     * 多次增发
     */

    @Test
    public void increaseMultiTime()throws Exception{
        String eqCode = gdEquityCode;
        String reason = "股份分红";

        Map eqProd = gdBF.init03EquityProductInfo();
        List<String> txList = new ArrayList<>();
        for(int i =0 ;i< 20;i++) {

            String txObjId = "4increaseObj2" + Random(6);
            Map txInfo = gdBF.init04TxInfo();
            txInfo.put("transaction_object_id",txObjId);
            log.info(txInfo.toString());
            register_transaction_ref = txObjId; //此处为发行融资 设置登记引用接口中的交易报告

            List<Map> shareListIn = gdConstructShareList(gdAccount1,1000,0);
            List<Map> shareListIn2 = gdConstructShareList(gdAccount2,1000,1, shareListIn);
            List<Map> shareListIn3 = gdConstructShareList(gdAccount3,1000,0, shareListIn2);
            List<Map> shareListIn4 = gdConstructShareList(gdAccount4,1000,1, shareListIn3);

            String response= gd.GDShareIncrease(gdPlatfromKeyID,eqCode,shareListIn4,reason, eqProd,txInfo);
            String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
            commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
            assertEquals("200", JSONObject.fromObject(store.GetTxDetail(txId)).getString("state"));
            txList.add(JSONObject.fromObject(response).getJSONObject("data").getString("txId"));
        }

        sleepAndSaveInfo(SLEEPTIME*2);



        String query = "";

        //查询挂牌企业数据
        //查询投资者信息
        //查询企业股东信息
        query = gd.GDGetEnterpriseShareInfo(gdEquityCode);
        assertEquals("200",JSONObject.fromObject(query).getString("state"));

        for(int i=0;i<txList.size();i++)
        {
            assertEquals("200",JSONObject.fromObject(store.GetTxDetail(txList.get(i))).getString("state"));
        }

        JSONArray dataShareList = JSONObject.fromObject(query).getJSONArray("data");

        JSONArray jsonArrayGet = JSONObject.fromObject(query).getJSONArray("data");
        assertEquals("84000",getTotalAmountFromShareList(jsonArrayGet));

        //实际应该持股情况信息
        List<Map> respShareList = new ArrayList<>();
        respShareList = gdConstructQueryShareList(gdAccount1,21000,0,0,mapShareENCN().get("0"),respShareList);
        List<Map> respShareList2 = gdConstructQueryShareList(gdAccount2,21000,1,0,mapShareENCN().get("1"), respShareList);
        List<Map> respShareList3 = gdConstructQueryShareList(gdAccount3,21000,0,0,mapShareENCN().get("0"), respShareList2);
        List<Map> respShareList4 = gdConstructQueryShareList(gdAccount4,21000,1,0,mapShareENCN().get("1"), respShareList3);

        log.info(respShareList4.toString());
        //检查存在余额的股东列表
        assertEquals(respShareList4.size(),dataShareList.size());

        List<Map> getShareList = getShareListFromQueryNoZeroAcc(dataShareList);

        assertEquals(respShareList4.size(),getShareList.size());
        assertEquals(true,respShareList4.containsAll(getShareList) && getShareList.containsAll(respShareList4));


        //查询股东持股情况 无当前股权代码信息
        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo1);
        assertEquals(gdAccClientNo1,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));

        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo1 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount1 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":21000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo2);
        assertEquals(gdAccClientNo2,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo2 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount2 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":21000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo3);
        assertEquals(gdAccClientNo3,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo3 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount3 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":0,\"sharePropertyCN\":\"" + mapShareENCN().get("0") + "\",\"totalAmount\":21000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo4);
        assertEquals(gdAccClientNo4,JSONObject.fromObject(query).getJSONObject("data").getString("clientNo"));
        assertEquals(true,query.contains("\"shareholderNo\":\"SH" + gdAccClientNo4 + "\""));
        assertEquals(true,query.contains("\"address\":\"" + gdAccount4 + "\""));
        assertEquals(true,query.contains("{\"equityCode\":\"" + gdEquityCode +
                "\",\"shareProperty\":1,\"sharePropertyCN\":\"" + mapShareENCN().get("1") + "\",\"totalAmount\":21000,\"lockAmount\":0}"));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo5);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

        query = gd.GDGetShareHolderInfo(gdContractAddress,gdAccClientNo6);
        assertEquals(false,query.contains("\"equityCode\": \"" + gdEquityCode + "\""));

    }

    @Test
    public void Increase200()throws Exception{
        register_event_type = 2;//非交易登记

        String eqCode = gdEquityCode;
        String reason = "股份分红";
        String txObjId = "4increaseObj" + Random(6);

        Map eqProd = gdBF.init03EquityProductInfo();
        Map txInfo = gdBF.init04TxInfo();
        txInfo.put("transaction_object_id",txObjId);
        register_transaction_ref = txObjId; //此处为发行融资 设置登记引用接口中的交易报告

        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        for(int i =0;i< 200;i++) {
            shareList = gdConstructShareList(gdAccount2, 1000, 1, shareList);
        }

        String response= gd.GDShareIncrease(gdPlatfromKeyID,eqCode,shareList,reason, eqProd,txInfo);
        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        String txDetail = store.GetTxDetail(txId);
        assertEquals("200", net.sf.json.JSONObject.fromObject(txDetail).getString("state"));
    }

    @Test
    public void IncreaseToNewAccount()throws Exception{
        register_event_type = 1;//非交易登记

        log.info("多个回收前查询机构主体信息");
        String query2 = gd.GDObjectQueryByVer(gdCompanyID,-1);
        int totalMembersBf = JSONObject.fromObject(query2).getJSONObject("data").getJSONObject("body"
        ).getJSONObject("subject_information").getJSONObject("organization_subject_information"
        ).getJSONObject("basic_information_of_enterprise").getJSONObject("basic_information_description"
        ).getInt("subject_shareholders_number");

        String subVerInit = gdCF.getObjectLatestVer(gdCompanyID);//获取初始主体版本

        log.info(registerInfo.toString());
        mapAddrRegObjId.clear(); //先清空map 这样后面map中拿到的就是此次发行的登记对象标识
        register_product_ref = gdEquityCode;

        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        gd.GDGetEnterpriseShareInfo(gdEquityCode);
        log.info("====================================================================================");
        String response= gd.GDShareRecycle(gdPlatfromKeyID,gdEquityCode,shareList,"1111");
        assertEquals("200",JSONObject.fromObject(response).getString("state"));

        sleepAndSaveInfo(2000);

        query2 = gd.GDObjectQueryByVer(gdCompanyID,-1);
        int totalMembers02 = JSONObject.fromObject(query2).getJSONObject("data").getJSONObject("body"
        ).getJSONObject("subject_information").getJSONObject("organization_subject_information"
        ).getJSONObject("basic_information_of_enterprise").getJSONObject("basic_information_description"
        ).getInt("subject_shareholders_number");

        assertEquals(totalMembersBf -1,totalMembers02);//回收一个账户

        register_event_type = 2;//非交易登记
        String eqCode = gdEquityCode;
        String reason = "股份分红";
        String txObjId = "4increaseObj" + Random(6);

        Map eqProd = gdBF.init03EquityProductInfo();
        Map txInfo = gdBF.init04TxInfo();
        txInfo.put("transaction_object_id",txObjId);
        register_transaction_ref = txObjId; //此处为发行融资 设置登记引用接口中的交易报告

        query2 = gd.GDObjectQueryByVer(gdCompanyID,-1);
        int totalMembers = JSONObject.fromObject(query2).getJSONObject("data").getJSONObject("body"
        ).getJSONObject("subject_information").getJSONObject("organization_subject_information"
        ).getJSONObject("basic_information_of_enterprise").getJSONObject("basic_information_description"
        ).getInt("subject_shareholders_number");

        shareList = gdConstructShareList(gdAccount5,1000,0);


        response= gd.GDShareIncrease(gdPlatfromKeyID,eqCode,shareList,reason, eqProd,txInfo);
        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        String txDetail = store.GetTxDetail(txId);
        assertEquals("200", net.sf.json.JSONObject.fromObject(txDetail).getString("state"));

        query2 = gd.GDObjectQueryByVer(gdCompanyID,-1);
        int totalMembersAft = JSONObject.fromObject(query2).getJSONObject("data").getJSONObject("body"
        ).getJSONObject("subject_information").getJSONObject("organization_subject_information"
        ).getJSONObject("basic_information_of_enterprise").getJSONObject("basic_information_description"
        ).getInt("subject_shareholders_number");

        assertEquals(totalMembers + 1,totalMembersAft);//判断主体数据更新
    }


    @Test
    public void IncreaseToNewAccount02()throws Exception{
        register_event_type = 2;//非交易登记

        String eqCode = gdEquityCode;
        String reason = "股份分红";
        String txObjId = "4increaseObj" + Random(6);

        Map eqProd = gdBF.init03EquityProductInfo();
        Map txInfo = gdBF.init04TxInfo();
        txInfo.put("transaction_object_id",txObjId);
        register_transaction_ref = txObjId; //此处为发行融资 设置登记引用接口中的交易报告

        String query2 = gd.GDObjectQueryByVer(gdCompanyID,-1);
        int totalMembers = JSONObject.fromObject(query2).getJSONObject("data").getJSONObject("body"
        ).getJSONObject("subject_information").getJSONObject("organization_subject_information"
        ).getJSONObject("basic_information_of_enterprise").getJSONObject("basic_information_description"
        ).getInt("subject_shareholders_number");

        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        shareList = gdConstructShareList(gdAccount5,1000,0,shareList);


        String response= gd.GDShareIncrease(gdPlatfromKeyID,eqCode,shareList,reason, eqProd,txInfo);
        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        String txDetail = store.GetTxDetail(txId);
        assertEquals("200", net.sf.json.JSONObject.fromObject(txDetail).getString("state"));

        query2 = gd.GDObjectQueryByVer(gdCompanyID,-1);
        int totalMembersAft = JSONObject.fromObject(query2).getJSONObject("data").getJSONObject("body"
        ).getJSONObject("subject_information").getJSONObject("organization_subject_information"
        ).getJSONObject("basic_information_of_enterprise").getJSONObject("basic_information_description"
        ).getInt("subject_shareholders_number");

        assertEquals(totalMembers + 1,totalMembersAft);//判断主体数据更新
    }


    @Test
    public void Increase12()throws Exception{
        register_event_type = 2;//非交易登记

        String eqCode = gdEquityCode;
        String reason = "股份分红";
        String txObjId = "4increaseObj" + Random(6);

        Map eqProd = gdBF.init03EquityProductInfo();
        Map txInfo = gdBF.init04TxInfo();
        txInfo.put("transaction_object_id",txObjId);
        register_transaction_ref = txObjId; //此处为发行融资 设置登记引用接口中的交易报告

        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        for(int i =0;i< 11;i++) {
            shareList = gdConstructShareList(gdAccount2, 1000, 1, shareList);
        }

        String response= gd.GDShareIncrease(gdPlatfromKeyID,eqCode,shareList,reason, eqProd,txInfo);
        String txId = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        String txDetail = store.GetTxDetail(txId);
        assertEquals("200", net.sf.json.JSONObject.fromObject(txDetail).getString("state"));
    }

    /***
     * 增发不支持债券类
     * @throws Exception
     */
//    @Test
    public void TC09_shareIncrease() throws Exception {

        String eqCode = gdEquityCode;
        String reason = "股份分红";


        List<Map> shareList = gdConstructShareList(gdAccount1,1000,0);
        List<Map> shareList2 = gdConstructShareList(gdAccount5,1000,0, shareList);
        List<Map> shareList3 = gdConstructShareList(gdAccount6,1000,0, shareList2);

        String response= gd.GDShareIncrease(gdPlatfromKeyID,eqCode,shareList3,reason, bondProductInfo);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
//        assertEquals("产品主体信息中发行主体引用不可以为空",JSONObject.fromObject(response).getString("message"));
        assertEquals("债券类产品不可以增发",JSONObject.fromObject(response).getString("message"));


    }


}
