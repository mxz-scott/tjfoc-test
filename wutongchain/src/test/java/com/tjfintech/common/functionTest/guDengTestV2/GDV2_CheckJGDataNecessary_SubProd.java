package com.tjfintech.common.functionTest.guDengTestV2;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.GuDeng;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.*;
import org.junit.rules.TestName;
import org.junit.runners.MethodSorters;

import java.util.*;

import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassGD.*;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class GDV2_CheckJGDataNecessary_SubProd {

    TestBuilder testBuilder= TestBuilder.getInstance();
    GuDeng gd =testBuilder.getGuDeng();
    Store store =testBuilder.getStore();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    GDBeforeCondition gdBF = new GDBeforeCondition();
    GDCommonFunc gdCF = new GDCommonFunc();
    GDUnitFunc uf = new GDUnitFunc();

    @Rule
    public TestName tm = new TestName();

    @BeforeClass
    public static void Before()throws Exception{
        GDBeforeCondition gdBefore = new GDBeforeCondition();
//        gdBefore.gdCreateAccout();
        gdBefore.initRegulationData();
    }

    @After
    public void calJGDataAfterTx()throws Exception{
        testCurMethodName = tm.getMethodName();
        GDUnitFunc uf = new GDUnitFunc();
        int endHeight = net.sf.json.JSONObject.fromObject(store.GetHeight()).getInt("data");
        uf.checkJGHeaderOpVer(blockHeight,endHeight);
//        uf.updateBlockHeightParam(endHeight);
    }

    @Test
    public void subjectInfo_Must_Test()throws Exception{
        gdEquityCode = "nece" + Random(12);
        //挂牌企业登记
        long shareTotals = 1000000;
        String key = "";
        Map testSub = gdBF.init01EnterpriseSubjectInfo();
        Map testEquityProdInfo = gdBF.init03EquityProductInfo();
        key = "subject_object_id";
        log.info("测试字段 " + key);
        testSub.remove(key);
        String response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,testSub,
                testEquityProdInfo,null,null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("400",JSONObject.fromObject(response).getString("state"));

        testSub = gdBF.init01EnterpriseSubjectInfo();
        key = "subject_type";
        log.info("测试字段 " + key);
        testSub.remove(key);
        response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,testSub,
                testEquityProdInfo,null,null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("400",JSONObject.fromObject(response).getString("state"));

        testSub = gdBF.init01EnterpriseSubjectInfo();
        key = "subject_main_administrative_region";
        log.info("测试字段 " + key);
        testSub.remove(key);
        response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,testSub,
                testEquityProdInfo,null,null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("400",JSONObject.fromObject(response).getString("state"));

        testSub = gdBF.init01EnterpriseSubjectInfo();
        key = "subject_create_time";
        log.info("测试字段 " + key);
        testSub.remove(key);
        response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,testSub,
                testEquityProdInfo,null,null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("400",JSONObject.fromObject(response).getString("state"));

        testSub = gdBF.init01EnterpriseSubjectInfo();
        key = "subject_company_name";
        log.info("测试字段 " + key);
        testSub.remove(key);
        response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,testSub,
                testEquityProdInfo,null,null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("400",JSONObject.fromObject(response).getString("state"));

        testSub = gdBF.init01EnterpriseSubjectInfo();
        key = "subject_document_information";
        log.info("测试字段 " + key);
        testSub.remove(key);
        response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,testSub,
                testEquityProdInfo,null,null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("400",JSONObject.fromObject(response).getString("state"));

        testSub = gdBF.init01EnterpriseSubjectInfo();
        key = "subject_investor_name";
        log.info("测试字段 " + key);
        testSub.remove(key);
        response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,testSub,
                testEquityProdInfo,null,null);
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        assertEquals("400",JSONObject.fromObject(response).getString("state"));

        testSub = gdBF.init01EnterpriseSubjectInfo();
        key = "subject_id_type";
        log.info("测试字段 " + key);
        testSub.remove(key);
        response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,testSub,
                testEquityProdInfo,null,null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("400",JSONObject.fromObject(response).getString("state"));

        testSub = gdBF.init01EnterpriseSubjectInfo();
        key = "subject_id_number";
        log.info("测试字段 " + key);
        testSub.remove(key);
        response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,testSub,
                testEquityProdInfo,null,null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("400",JSONObject.fromObject(response).getString("state"));

    }

    @Test
    public void equityProductInfo_Must_Test()throws Exception{
        gdEquityCode = "nece" + Random(12);
        //挂牌企业登记
        long shareTotals = 1000000;
        String key = "";
        Map testSub = gdBF.init01EnterpriseSubjectInfo();
        Map testEquityProdInfo = gdBF.init03EquityProductInfo();
        key = "product_object_id";
        log.info("测试字段 " + key);
        testEquityProdInfo.remove(key);
        String response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,testSub,
                testEquityProdInfo,null,null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("400",JSONObject.fromObject(response).getString("state"));

        testEquityProdInfo = gdBF.init03EquityProductInfo();
        key = "product_trading_market_category";
        log.info("测试字段 " + key);
        testEquityProdInfo.remove(key);
        response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,testSub,
                testEquityProdInfo,null,null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("400",JSONObject.fromObject(response).getString("state"));

        testEquityProdInfo = gdBF.init03EquityProductInfo();
        key = "product_market_subject_ref";
        log.info("测试字段 " + key);
        testEquityProdInfo.remove(key);
        response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,testSub,
                testEquityProdInfo,null,null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("400",JSONObject.fromObject(response).getString("state"));

        testEquityProdInfo = gdBF.init03EquityProductInfo();
        key = "product_market_subject_name";
        log.info("测试字段 " + key);
        testEquityProdInfo.remove(key);
        response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,testSub,
                testEquityProdInfo,null,null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("400",JSONObject.fromObject(response).getString("state"));

        testEquityProdInfo = gdBF.init01EnterpriseSubjectInfo();
        key = "product_issuer_subject_ref";
        log.info("测试字段 " + key);
        testEquityProdInfo.remove(key);
        response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,testSub,
                testEquityProdInfo,null,null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("400",JSONObject.fromObject(response).getString("state"));

        testEquityProdInfo = gdBF.init03EquityProductInfo();
        key = "product_issuer_name";
        log.info("测试字段 " + key);
        testEquityProdInfo.remove(key);
        response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,testSub,
                testEquityProdInfo,null,null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("400",JSONObject.fromObject(response).getString("state"));

        testEquityProdInfo = gdBF.init03EquityProductInfo();
        key = "product_code";
        log.info("测试字段 " + key);
        testEquityProdInfo.remove(key);
        response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,testSub,
                testEquityProdInfo,null,null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("400",JSONObject.fromObject(response).getString("state"));

        testEquityProdInfo = gdBF.init03EquityProductInfo();
        key = "product_name";
        log.info("测试字段 " + key);
        testEquityProdInfo.remove(key);
        response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,testSub,
                testEquityProdInfo,null,null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("400",JSONObject.fromObject(response).getString("state"));

        testEquityProdInfo = gdBF.init03EquityProductInfo();
        key = "product_type";
        log.info("测试字段 " + key);
        testEquityProdInfo.remove(key);
        response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,testSub,
                testEquityProdInfo,null,null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("400",JSONObject.fromObject(response).getString("state"));

        testEquityProdInfo = gdBF.init03EquityProductInfo();
        key = "product_create_time";
        log.info("测试字段 " + key);
        testEquityProdInfo.remove(key);
        response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,testSub,
                testEquityProdInfo,null,null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("400",JSONObject.fromObject(response).getString("state"));

        testEquityProdInfo = gdBF.init03EquityProductInfo();
        key = "product_shares_issued_class";
        log.info("测试字段 " + key);
        testEquityProdInfo.remove(key);
        response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,testSub,
                testEquityProdInfo,null,null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("400",JSONObject.fromObject(response).getString("state"));

    }

    @Test
    public void fundProductInfo_Must_Test()throws Exception{
        gdEquityCode = "nece" + Random(12);
        //挂牌企业登记
        long shareTotals = 1000000;
        String key = "";
        Map testSub = gdBF.init01EnterpriseSubjectInfo();
        Map testEquityProdInfo = gdBF.init03FundProductInfo();
        key = "product_object_id";
        log.info("测试字段 " + key);
        testEquityProdInfo.remove(key);
        String response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,testSub,
                testEquityProdInfo,null,null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("400",JSONObject.fromObject(response).getString("state"));

        testEquityProdInfo = gdBF.init03FundProductInfo();
        key = "product_trading_market_category";
        log.info("测试字段 " + key);
        testEquityProdInfo.remove(key);
        response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,testSub,
                testEquityProdInfo,null,null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("400",JSONObject.fromObject(response).getString("state"));

        testEquityProdInfo = gdBF.init03FundProductInfo();
        key = "product_market_subject";
        log.info("测试字段 " + key);
        testEquityProdInfo.remove(key);
        response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,testSub,
                testEquityProdInfo,null,null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("400",JSONObject.fromObject(response).getString("state"));

        testEquityProdInfo = gdBF.init03FundProductInfo();
        key = "product_market_subject_name";
        log.info("测试字段 " + key);
        testEquityProdInfo.remove(key);
        response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,testSub,
                testEquityProdInfo,null,null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("400",JSONObject.fromObject(response).getString("state"));

        testEquityProdInfo = gdBF.init03FundProductInfo();
        key = "product_issuer_subject_ref";
        log.info("测试字段 " + key);
        testEquityProdInfo.remove(key);
        response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,testSub,
                testEquityProdInfo,null,null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("400",JSONObject.fromObject(response).getString("state"));

        testEquityProdInfo = gdBF.init03FundProductInfo();
        key = "product_issuer_name";
        log.info("测试字段 " + key);
        testEquityProdInfo.remove(key);
        response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,testSub,
                testEquityProdInfo,null,null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("400",JSONObject.fromObject(response).getString("state"));

        testEquityProdInfo = gdBF.init03FundProductInfo();
        key = "product_code";
        log.info("测试字段 " + key);
        testEquityProdInfo.remove(key);
        response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,testSub,
                testEquityProdInfo,null,null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("400",JSONObject.fromObject(response).getString("state"));

        testEquityProdInfo = gdBF.init03FundProductInfo();
        key = "product_name";
        log.info("测试字段 " + key);
        testEquityProdInfo.remove(key);
        response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,testSub,
                testEquityProdInfo,null,null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("400",JSONObject.fromObject(response).getString("state"));

        testEquityProdInfo = gdBF.init03FundProductInfo();
        key = "product_type";
        log.info("测试字段 " + key);
        testEquityProdInfo.remove(key);
        response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,testSub,
                testEquityProdInfo,null,null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("400",JSONObject.fromObject(response).getString("state"));

        testEquityProdInfo = gdBF.init03FundProductInfo();
        key = "product_create_time";
        log.info("测试字段 " + key);
        testEquityProdInfo.remove(key);
        response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,testSub,
                testEquityProdInfo,null,null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("400",JSONObject.fromObject(response).getString("state"));

    }

    @Test
    public void bondProductInfo_Must_Test()throws Exception{
        gdEquityCode = "nece" + Random(12);
        //挂牌企业登记
        long shareTotals = 1000000;
        String key = "";
        Map testSub = gdBF.init01EnterpriseSubjectInfo();
        Map testEquityProdInfo = gdBF.init03BondProductInfo();
        key = "product_object_id";
        log.info("测试字段 " + key);
        testEquityProdInfo.remove(key);
        String response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,testSub,
                testEquityProdInfo,null,null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("400",JSONObject.fromObject(response).getString("state"));

        testEquityProdInfo = gdBF.init03BondProductInfo();
        key = "product_trading_market_category";
        log.info("测试字段 " + key);
        testEquityProdInfo.remove(key);
        response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,testSub,
                testEquityProdInfo,null,null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("400",JSONObject.fromObject(response).getString("state"));

        testEquityProdInfo = gdBF.init03BondProductInfo();
        key = "product_market_subject";
        log.info("测试字段 " + key);
        testEquityProdInfo.remove(key);
        response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,testSub,
                testEquityProdInfo,null,null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("400",JSONObject.fromObject(response).getString("state"));

        testEquityProdInfo = gdBF.init03BondProductInfo();
        key = "product_market_subject_name";
        log.info("测试字段 " + key);
        testEquityProdInfo.remove(key);
        response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,testSub,
                testEquityProdInfo,null,null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("400",JSONObject.fromObject(response).getString("state"));

        testEquityProdInfo = gdBF.init03BondProductInfo();
        key = "product_issuer_subject_ref";
        log.info("测试字段 " + key);
        testEquityProdInfo.remove(key);
        response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,testSub,
                testEquityProdInfo,null,null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("400",JSONObject.fromObject(response).getString("state"));

        testEquityProdInfo = gdBF.init03BondProductInfo();
        key = "product_issuer_name";
        log.info("测试字段 " + key);
        testEquityProdInfo.remove(key);
        response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,testSub,
                testEquityProdInfo,null,null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("400",JSONObject.fromObject(response).getString("state"));

        testEquityProdInfo = gdBF.init03BondProductInfo();
        key = "product_code";
        log.info("测试字段 " + key);
        testEquityProdInfo.remove(key);
        response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,testSub,
                testEquityProdInfo,null,null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("400",JSONObject.fromObject(response).getString("state"));

        testEquityProdInfo = gdBF.init03BondProductInfo();
        key = "product_name";
        log.info("测试字段 " + key);
        testEquityProdInfo.remove(key);
        response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,testSub,
                testEquityProdInfo,null,null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("400",JSONObject.fromObject(response).getString("state"));

        testEquityProdInfo = gdBF.init03BondProductInfo();
        key = "product_type";
        log.info("测试字段 " + key);
        testEquityProdInfo.remove(key);
        response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,testSub,
                testEquityProdInfo,null,null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("400",JSONObject.fromObject(response).getString("state"));

        testEquityProdInfo = gdBF.init03BondProductInfo();
        key = "product_create_time";
        log.info("测试字段 " + key);
        testEquityProdInfo.remove(key);
        response= gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,shareTotals,testSub,
                testEquityProdInfo,null,null);
        assertEquals("400",JSONObject.fromObject(response).getString("state"));
        assertEquals("400",JSONObject.fromObject(response).getString("state"));

    }

}
