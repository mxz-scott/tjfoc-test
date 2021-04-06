package com.tjfintech.common.functionTest.guDengTestV2;

import com.alibaba.fastjson.JSONObject;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.GuDeng;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.MinIOOperation;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import org.junit.*;
import org.junit.rules.TestName;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.tjfintech.common.functionTest.guDengTestV2.GDCommonFunc.conJGFileName;
import static com.tjfintech.common.functionTest.guDengTestV2.GDCommonFunc.replaceCertain;
import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassGD.*;
import static org.junit.Assert.assertEquals;

//import net.sf.json.JSONObject;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class GDV2_A_recycle {

    TestBuilder testBuilder= TestBuilder.getInstance();
    GuDeng gd =testBuilder.getGuDeng();
    Store store =testBuilder.getStore();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    GDCommonFunc gdCF = new GDCommonFunc();
    GDBeforeCondition gdBF = new GDBeforeCondition();
    GDUnitFunc uf = new GDUnitFunc();
    public static String bizNoTest = "test" + Random(12);
    String tempaccount_subject_ref,tempaccount_associated_account_ref,tempproduct_issuer_subject_ref;

    Boolean bTest = false;

    @Rule
    public TestName tm = new TestName();


    @BeforeClass
    public static void Before()throws Exception{
        GDBeforeCondition gdBefore = new GDBeforeCondition();
        gdBefore.initRegulationData();
    }



    @Test
    public void testReg()throws Exception{
        for(int i =0;i<1000;i++) {
            log.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++" + i);
            gdEquityCode = Random(20);
            gdCompanyID = "P1Re" + Random(8);
            Map enSubInfo = gdBF.init01EnterpriseSubjectInfo();
            Map prodInfo = gdBF.init03EquityProductInfo();

            String response = gd.GDEnterpriseResister(gdContractAddress,gdEquityCode,1000,enSubInfo,
                    prodInfo,null,null);
            sleepAndSaveInfo(300);
        }
    }

    @Test
    public void testCreate()throws Exception{
        GDBeforeCondition gdBC = new GDBeforeCondition();
        for(int i =0;i<1000;i++) {
            log.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++" + i);
            String cltNo = Random(20);

            Map mapCreate = gdBC.gdCreateAccParam(cltNo);

            assertEquals("200", net.sf.json.JSONObject.fromObject(
                    store.GetTxDetail(mapCreate.get("txId").toString())).getString("state"));
            sleepAndSaveInfo(300);
        }
    }



}
