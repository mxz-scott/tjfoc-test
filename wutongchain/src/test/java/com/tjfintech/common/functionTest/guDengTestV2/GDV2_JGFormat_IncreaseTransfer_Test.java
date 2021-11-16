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

import java.text.SimpleDateFormat;
import java.util.*;

import static com.tjfintech.common.functionTest.guDengTestV2.GDCommonFunc.*;
import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassGD.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j

/***
 * 单独补充增发后转让 重复多次测试 测试用例
 * 甲方环境出现增发后转让报错问题
 */
public class GDV2_JGFormat_IncreaseTransfer_Test {

    TestBuilder testBuilder= TestBuilder.getInstance();
    GuDeng gd =testBuilder.getGuDeng();
    Store store =testBuilder.getStore();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    GDCommonFunc gdCF = new GDCommonFunc();
    GDUnitFunc uf = new GDUnitFunc();
    GDBeforeCondition gdBF = new GDBeforeCondition();
    public static String bizNoTest = "test" + Random(12);
    long issueAmount = 5000;
    long increaseAmount = 1000;
    long lockAmount = 500;
    long recycleAmount = 100;
    long changeAmount = 500;
    long transferAmount = 1000;
    Boolean bSame = true;

    Boolean bCheckList = true;

    String tempaccount_subject_ref = account_subject_ref;
    String tempsubject_investor_qualification_certifier_ref = subject_investor_qualification_certifier_ref;
    String tempproduct_issuer_subject_ref = product_issuer_subject_ref;
    String tempregister_transaction_ref = register_transaction_ref;

    public static String objID1 = "form1Reg"+Random(10);
    public static String objID11 = "form1Reg"+Random(10);
    public static String objID2 = "form2Reg"+Random(10);
    public static String objID3 = "form3Reg"+Random(10);
    public static String objID4 = "form4Reg"+Random(10);
    public static String objID5 = "form5Reg"+Random(10);


    @Rule
    public TestName tm = new TestName();

    @BeforeClass
    public static void Before()throws Exception{
        gdEquityCode = "prodEq" + Random(12);
        gdCompanyID = "P1Re" + Random(8);
        register_product_ref = gdEquityCode;
        roll_register_product_ref = gdEquityCode;
        transaction_custody_product_ref = gdEquityCode;
        GDBeforeCondition gdBefore = new GDBeforeCondition();
        gdBefore.gdCreateAccout();
//        gdBefore.initRegulationData();
        equityProductInfo = gdBefore.init03EquityProductInfo();
        bondProductInfo = null;
        fundProductInfo = null;

        log.info("+++++++++++++++++++++++++++++++++++++++++");
        log.info(objID1);log.info(objID11);log.info(objID2);log.info(objID3);log.info(objID4);log.info(objID5);
    }

    @Before
    public void resetVar(){


        register_event_type = 1;//非交易登记
        tempsubject_investor_qualification_certifier_ref =subject_investor_qualification_certifier_ref;
        tempregister_transaction_ref = register_transaction_ref;
    }

    @After
    public void calJGDataAfterTx()throws Exception{
        testCurMethodName = tm.getMethodName();
        GDUnitFunc uf = new GDUnitFunc();
        int endHeight = JSONObject.fromObject(store.GetHeight()).getInt("data");
//        uf.checkJGHeaderOpVer(blockHeight,endHeight);

        subject_investor_qualification_certifier_ref = tempsubject_investor_qualification_certifier_ref;
        register_transaction_ref = tempregister_transaction_ref;
    }

    @Test
    public void test20()throws Exception{
        for(int i=0;i<20;i++) {
            log.info("++++++++++++++++++++++++++++++++++++++++++++  " + i);
            gdEquityCode = "prodEq" + Random(12);
            gdCompanyID = "P1Re" + Random(8);
            register_product_ref = gdEquityCode;
            roll_register_product_ref = gdEquityCode;
            transaction_custody_product_ref = gdEquityCode;
            GDBeforeCondition gdBefore = new GDBeforeCondition();
//            gdBefore.gdCreateAccout();
//        gdBefore.initRegulationData();
            equityProductInfo = gdBefore.init03EquityProductInfo();
            bondProductInfo = null;
            fundProductInfo = null;

            objID1 = "form1Reg"+Random(10);
            objID11 = "form1Reg"+Random(10);
            objID2 = "form2Reg"+Random(10);
            objID3 = "form3Reg"+Random(10);
            objID4 = "form4Reg"+Random(10);
            objID5 = "form5Reg"+Random(10);

            TC01_shareIssue();
            TC02_IncreaseThenTransfer();
        }
    }

    @Test
    public void TC01_shareIssue() throws Exception {
        log.info(registerInfo.toString());
        mapAddrRegObjId.clear(); //先清空map 这样后面map中拿到的就是此次发行的登记对象标识

        List<Map> shareList = gdConstructShareListWithObjID(gdAccount1,issueAmount,1,objID1);
        List<Map> shareList2 = gdConstructShareListWithObjID(gdAccount2,issueAmount,1,objID2,shareList);
//        List<Map> shareList3 = gdConstructShareListWithObjID(gdAccount3,issueAmount,1,objID3, shareList2);
//        List<Map> shareList4 = gdConstructShareListWithObjID(gdAccount4,issueAmount,1,objID4,shareList3);

        String response= uf.shareIssue(gdEquityCode,shareList2,false);
        JSONObject jsonObject=JSONObject.fromObject(response);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        String txId = jsonObject.getJSONObject("data").getString("txId");

        commonFunc.sdkCheckTxOrSleep(txId, utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        String txDetail = store.GetTxDetail(txId);
        assertEquals("200", JSONObject.fromObject(txDetail).getString("state"));


    }


    @Test
    public void TC02_IncreaseThenTransfer()throws Exception{

        String keyId = gdAccountKeyID1;
        String fromAddr = gdAccount1;
        String toAddr = gdAccount4;
        int shareProperty = 0;
        String eqCode = gdEquityCode;

        register_event_type = 2;//交易登记
        //交易报告数据
        Map txInfo = gdBF.init04TxInfo();
        String txRpObjId = "txReport" + Random(6);
        txInfo.put("transaction_object_id",txRpObjId);
//        txInfo.put("transaction_custody_product_ref",gdEquityCode);

        //登记数据
        String tempObjIdFrom = objID1;
        String tempObjIdTo = objID4;

        register_transaction_ref = txRpObjId;//登记引用的是交易报告的对象标识
        transaction_custody_product_ref = gdEquityCode;
        register_product_ref = gdEquityCode;

        Map fromNow = gdBF.init05RegInfo();
        Map toNow = gdBF.init05RegInfo();

        fromNow.put("register_registration_object_id",tempObjIdFrom);
        fromNow.put("register_subject_account_ref","SH" + gdAccClientNo1);

        toNow.put("register_registration_object_id",tempObjIdTo);
        toNow.put("register_subject_account_ref","SH" + gdAccClientNo5);

//        fromNow.put("register_transaction_ref",txRpObjId);
//        toNow.put("register_transaction_ref",txRpObjId);

        mapAddrRegObjId.put(toAddr + shareProperty,tempObjIdTo);//方便后面测试验证

        String tempObj = gdCompanyID;

        String query2 = gd.GDObjectQueryByVer(gdCompanyID,-1);

        TC08_shareIncrease();

        //执行交易
        String response= gd.GDShareTransfer(keyId,fromAddr,transferAmount,toAddr,shareProperty,eqCode,txInfo,fromNow,toNow);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
    }

    //交易报告填写且类型为1 发行融资 即需要报送交易报告
//    @Test
    public void TC08_shareIncrease() throws Exception {

        log.info("增发前查询机构主体信息");
        String query2 = gd.GDMainSubjectQuery(gdContractAddress,gdCompanyID);

        register_event_type = 1;//非交易登记 不报送交易报告库

        String eqCode = gdEquityCode;
        String reason = "股份分红";


        List<Map> shareList = gdConstructShareListWithObjID(gdAccount1,increaseAmount,0,objID1);
        List<Map> shareList2 = gdConstructShareListWithObjID(gdAccount2,increaseAmount,0, objID2,shareList);
//        List<Map> shareList3 = gdConstructShareListWithObjID(gdAccount3,increaseAmount,0, objID3,shareList2);
//        List<Map> shareList4 = gdConstructShareListWithObjID(gdAccount4,increaseAmount,0, objID4,shareList3);

        String response= gd.GDShareIncreaseNoProduct(gdPlatfromKeyID,eqCode,shareList2,reason);
    }



}
