package com.tjfintech.common.functionTest.CreditTest;

import com.alibaba.fastjson.JSON;
import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.CreditBeforeCondition;
import com.tjfintech.common.Interface.Credit;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.functionTest.contract.WVMContractTest;
import com.tjfintech.common.utils.FileOperation;
import com.tjfintech.common.utils.UtilsClass;
import com.tjfintech.common.utils.UtilsClassCredit;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.*;
import org.junit.runners.MethodSorters;
import org.mockito.internal.configuration.MockAnnotationProcessor;

import java.util.*;

import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassCredit.*;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class CreditDataTest {

    TestBuilder testBuilder = TestBuilder.getInstance();
    Credit credit = testBuilder.getCredit();
    Store store = testBuilder.getStore();
    CommonFunc commonFunc = new CommonFunc();
    UtilsClass utilsClass = new UtilsClass();
    UtilsClassCredit utilsClassCredit = new UtilsClassCredit();
    CreditBeforeCondition creditBeforeCondition = new CreditBeforeCondition();

    @BeforeClass
    public static void init() throws Exception {

        UtilsClassCredit utilsClassCredit = new UtilsClassCredit();
        UtilsClass utilsClass = new UtilsClass();

        if (authContractName.isEmpty()) {
            BeforeCondition beforeCondition = new BeforeCondition();
            CreditBeforeCondition creditBeforeCondition = new CreditBeforeCondition();
            beforeCondition.updatePubPriKey();
            creditBeforeCondition.installZXContract();
        }
        //??????zxconfig????????????Code???CreditdataPath
        utilsClassCredit.setZXConfig(utilsClass.getIPFromStr(SDKADD), "Common", "Code", zxCode);
        utilsClassCredit.setZXConfig(utilsClass.getIPFromStr(SDKADD), "SmartContract", "CreditdataPath", creditContractName);
        utilsClassCredit.setZXConfig(utilsClass.getIPFromStr(SDKADD), "SmartContract", "AuthorizationPath", authContractName);
        utilsClassCredit.setZXConfig(utilsClass.getIPFromStr(SDKADD), "SmartContract", "ViewhistoryPath", viewContractName);
        utilsClassCredit.setZXConfig(utilsClass.getIPFromStr(SDKADD), "SmartContract", "IdentityPath", identityContractName);
        shellExeCmd(utilsClass.getIPFromStr(SDKADD), killSDKCmd, startSDKCmd); //??????sdk api
        sleepAndSaveInfo(SLEEPTIME, "??????SDK??????");
    }

    /**
     * ??????????????????????????????????????????????????????zxconfig?????????
     * ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     */
    @Test
    public void creditProcessTest() throws Exception {


        //????????????
        String response = credit.creditIdentityAdd
                (zxCode, zxCode, "????????????", creditContractName, PUBKEY1, "aa", "bb");
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        //????????????
        response = credit.creditIdentityQuery(zxCode);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(zxCode, JSONObject.fromObject(response).getJSONObject("data").getString("Code"));

        //??????????????????
        response = credit.creditIdentityQueryAll();
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains(zxCode));

        int i = 3;
        for (int g = 1; g < i; g++) {
            //??????????????????
            String enterprisecode = "enterprise" + utilsClass.Random(8);
            String hash = "hash" + utilsClass.Random(8);
            String time = String.valueOf(System.currentTimeMillis());
            List<Map> creditlist = utilsClassCredit.constructCreditData(enterprisecode, enterprisecode, zxCode, zxCode, hash,
                    "a", time, "a", "A");
            response = credit.creditCreditdataAdd(creditlist, "");
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                    utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

            //??????????????????
            response = credit.creditCreditdataQuery(enterprisecode);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(enterprisecode, JSONObject.fromObject(response).getJSONArray("data").getJSONObject(0).
                    getString("EnterpriseCode"));

            //??????????????????
            ArrayList<String> orgid = new ArrayList<>();
            orgid.add(zxCode);
            List<Map> authlist = utilsClassCredit.constructAuthorizationData(enterprisecode, enterprisecode, zxCode, zxCode, "ICBC",
                    "ICBC", zxCode, zxCode, "aa", "https://www.baidu.com/", hash, "aa", time, 10);
            response = credit.creditAuthorizationAdd(orgid, authlist);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                    utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
            String authkey = JSONObject.fromObject(response).getJSONObject("data").getJSONArray("key").getString(0);

            //??????????????????
            response = credit.creditAuthorizationQuery(authkey);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertThat(response, allOf(containsString(zxCode), containsString(enterprisecode)));

            //??????????????????
            List<Map> viewlist = utilsClassCredit.constructViewData(enterprisecode, enterprisecode, zxCode, zxCode, "ICBC", "ICBC",
                    zxCode, zxCode, "aa", "aa", "aa", time);
            response = credit.creditViewhistoryAdd(orgid, viewlist);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                    utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        }

    }


    /**
     * ????????????????????????
     * ?????????????????? creditdata.wlang
     */
    @Test
    public void multiInstallCreditdataContractTest() throws Exception {

        String creditfilePath = testDataPath + "credit/";
        String creditdata = "creditdata.wlang";

        //????????????creditdata??????
        int i = 3;
        String oldStr = "CreditDataContract";
        for (int g = 1; g < i; g++) {
            String repStr = "CreditDataContract" + g;
            FileOperation fileOper = new FileOperation();
            //??????creditdata.wlang
            fileOper.replace(creditfilePath + creditdata, "CreditDataContract", repStr);
            sleepAndSaveInfo(2000, "??????????????????");
            String response = creditBeforeCondition.contractInstallTest(creditfilePath + "creditdata" + "_temp.wlang", "");
            String contractname = JSONObject.fromObject(response).getJSONObject("data").getString("name");
            log.info(contractname + "****" + repStr);
            commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                    utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
            sleepAndSaveInfo(2000, "??????worldstate??????");

            credit.creditIdentityAdd
                    (repStr, repStr, "????????????", contractname, PUBKEY1, "aa", "bb");
            commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                    utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

            List<Map> list = utilsClassCredit.constructCreditData("A", "2020092506", oldStr, oldStr,
                    "hash1", "a", "2020-09-24-15-00-00", "a", "A");
            credit.creditCreditdataAdd(list, contractname);
            commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                    utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        }

    }


}