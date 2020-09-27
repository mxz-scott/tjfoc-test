package com.tjfintech.common.functionTest.CreditTest;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.Credit;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.functionTest.contract.WVMContractTest;
import com.tjfintech.common.utils.FileOperation;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.junit.Assert.assertEquals;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class CreditInterfaceTest {

    TestBuilder testBuilder = TestBuilder.getInstance();
    Credit credit = testBuilder.getCredit();
    Store store = testBuilder.getStore();
    CommonFunc commonFunc = new CommonFunc();
    CreditCommonFunc creditCommonFunc = new CreditCommonFunc();
    UtilsClass utilsClass = new UtilsClass();
    String creditfilePath = "credit/";
    String identity = "identity.wlang";
    String authorization = "authorization.wlang";
    String creditdata = "creditdata.wlang";
    String viewhistory = "viewhistory.wlang";

    WVMContractTest wvm = new WVMContractTest();

    @BeforeClass
    public static void init() throws Exception {
        BeforeCondition beforeCondition = new BeforeCondition();
        beforeCondition.updatePubPriKey();
    }

    /**
     * 安装合约
     * 机构私有合约 creditdata.wlang
     * 公共合约3个
     * authorization.wlang
     * creditdata.wlang
     * viewhistory.wlang
     */
    @Test
    public void creditProcessTest() throws Exception {

        //安装合约后会得到合约hash：由Prikey和ctName进行运算得到
        //安装3个公共合约和1个机构合约
        //安装authorization.wlang
        String response = wvm.wvmInstallTest(creditfilePath + authorization, "");
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
        String authname = JSONObject.fromObject(response).getJSONObject("data").getString("name");

        //安装viewhistory.wlang
        response = wvm.wvmInstallTest(creditfilePath + viewhistory, "");
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
        String viewname = JSONObject.fromObject(response).getJSONObject("data").getString("name");

        //安装identity.wlang
        response = wvm.wvmInstallTest(creditfilePath + identity, "");
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
        String identityname = JSONObject.fromObject(response).getJSONObject("data").getString("name");

        //安装creditdata.wlang
        response = wvm.wvmInstallTest(creditfilePath + "creditdata" , "");
        assertEquals("200",JSONObject.fromObject(response).getString("state"));
        String creditname = JSONObject.fromObject(response).getJSONObject("data").getString("name");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

    }


    /**
     * 安装多个机构合约
     * 机构私有合约 creditdata.wlang
     */
    @Test
    public void multiInstallCreditdataContractTest() throws Exception {

        //安装多个creditdata合约
        int i = 200;
        String oldStr = "CreditDataContract";
        for (int g = 100; g < i; g++) {
            String repStr = "CreditDataContract" + String.valueOf(g);
            FileOperation fileOper = new FileOperation();
            //安装creditdata.wlang
            fileOper.replace(resourcePath + creditfilePath + creditdata, "CreditDataContract", repStr);
            sleepAndSaveInfo(2000, "等待合约更新");
            String response = wvm.wvmInstallTest(creditfilePath + "creditdata" + "_temp.wlang", "");
            String contractname = JSONObject.fromObject(response).getJSONObject("data").getString("name");
            log.info(contractname + "****" + repStr);
            commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                    utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
            sleepAndSaveInfo(2000, "等待worldstate更新");

            credit.creditIdentityAdd
                    (repStr, repStr, "征信机构", contractname, PUBKEY1, "aa", "bb");
            commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                    utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

            List<Map> list = creditCommonFunc.constructCreditData("A", "2020092506", oldStr, oldStr, "hash1",
                    "a", "2020-09-24-15-00-00", "a", "A");
            credit.creditCreditdataAdd(list, contractname);
            commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                    utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        }

    }

    @Test
    public void creditIdentityAddTest() throws Exception {
        String code = "ZX" + utilsClass.Random(8);
        credit.creditIdentityAdd
                (code, code, "征信机构", "", PUBKEY1, "aa", "bb");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
    }

    @Test
    public void creditIdentityQueryTest() throws Exception {

//        //安装合约后会得到合约hash：由Prikey和ctName进行运算得到
//        //安装3个公共合约
//        //安装authorization.wlang
//        response1 = wvm.wvmInstallTest(creditfilePath + authorization, "");
//        String ctHashAuth = JSONObject.fromObject(response1).getJSONObject("data").getString("name");

//        //安装viewhistory.wlang
//        response1 = wvm.wvmInstallTest(creditfilePath + viewhistory, "");
//        String ctHashHist = JSONObject.fromObject(response1).getJSONObject("data").getString("name");

//        //安装identity.wlang
//        response1 = wvm.wvmInstallTest(creditfilePath + identity, "");
//        String ctHashHist = JSONObject.fromObject(response1).getJSONObject("data").getString("name");

        //安装多个creditdata合约
        int i = 300;
        for (int g = 1; g < i; g++) {
            String oldStr = "CreditDataContract";
            String repStr = "CreditDataContract" + String.valueOf(g);
            FileOperation fileOper = new FileOperation();
            //安装creditdata.wlang
            fileOper.replace(resourcePath + creditfilePath + creditdata, oldStr, repStr);
            String response = wvm.wvmInstallTest(creditfilePath + creditdata, "");
            String ctHashCredit = JSONObject.fromObject(response).getJSONObject("data").getString("name");
            log.info(ctHashCredit + "****" + repStr);
            commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                    utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
            sleepAndSaveInfo(2000, "等待worldstate更新");
            oldStr = repStr;
        }

    }

    @Test
    public void creditIdentityQueryAllTest() throws Exception {

//        //安装合约后会得到合约hash：由Prikey和ctName进行运算得到
//        //安装3个公共合约
//        //安装authorization.wlang
//        response1 = wvm.wvmInstallTest(creditfilePath + authorization, "");
//        String ctHashAuth = JSONObject.fromObject(response1).getJSONObject("data").getString("name");

//        //安装viewhistory.wlang
//        response1 = wvm.wvmInstallTest(creditfilePath + viewhistory, "");
//        String ctHashHist = JSONObject.fromObject(response1).getJSONObject("data").getString("name");

//        //安装identity.wlang
//        response1 = wvm.wvmInstallTest(creditfilePath + identity, "");
//        String ctHashHist = JSONObject.fromObject(response1).getJSONObject("data").getString("name");

        //安装多个creditdata合约
        int i = 300;
        for (int g = 1; g < i; g++) {
            String oldStr = "CreditDataContract";
            String repStr = "CreditDataContract" + String.valueOf(g);
            FileOperation fileOper = new FileOperation();
            //安装creditdata.wlang
            fileOper.replace(resourcePath + creditfilePath + creditdata, oldStr, repStr);
            String response = wvm.wvmInstallTest(creditfilePath + creditdata, "");
            String ctHashCredit = JSONObject.fromObject(response).getJSONObject("data").getString("name");
            log.info(ctHashCredit + "****" + repStr);
            commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                    utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
            sleepAndSaveInfo(2000, "等待worldstate更新");
            oldStr = repStr;
        }

    }

    @Test
    public void creditCreditdataAddTest() throws Exception {

//        //安装合约后会得到合约hash：由Prikey和ctName进行运算得到
//        //安装3个公共合约
//        //安装authorization.wlang
//        response1 = wvm.wvmInstallTest(creditfilePath + authorization, "");
//        String ctHashAuth = JSONObject.fromObject(response1).getJSONObject("data").getString("name");

//        //安装viewhistory.wlang
//        response1 = wvm.wvmInstallTest(creditfilePath + viewhistory, "");
//        String ctHashHist = JSONObject.fromObject(response1).getJSONObject("data").getString("name");

//        //安装identity.wlang
//        response1 = wvm.wvmInstallTest(creditfilePath + identity, "");
//        String ctHashHist = JSONObject.fromObject(response1).getJSONObject("data").getString("name");

        //安装多个creditdata合约
        int i = 300;
        for (int g = 1; g < i; g++) {
            String oldStr = "CreditDataContract";
            String repStr = "CreditDataContract" + String.valueOf(g);
            FileOperation fileOper = new FileOperation();
            //安装creditdata.wlang
            fileOper.replace(resourcePath + creditfilePath + creditdata, oldStr, repStr);
            String response = wvm.wvmInstallTest(creditfilePath + creditdata, "");
            String ctHashCredit = JSONObject.fromObject(response).getJSONObject("data").getString("name");
            log.info(ctHashCredit + "****" + repStr);
            commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                    utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
            sleepAndSaveInfo(2000, "等待worldstate更新");
            oldStr = repStr;
        }

    }

    @Test
    public void creditCreditdataQueryTest() throws Exception {

//        //安装合约后会得到合约hash：由Prikey和ctName进行运算得到
//        //安装3个公共合约
//        //安装authorization.wlang
//        response1 = wvm.wvmInstallTest(creditfilePath + authorization, "");
//        String ctHashAuth = JSONObject.fromObject(response1).getJSONObject("data").getString("name");

//        //安装viewhistory.wlang
//        response1 = wvm.wvmInstallTest(creditfilePath + viewhistory, "");
//        String ctHashHist = JSONObject.fromObject(response1).getJSONObject("data").getString("name");

//        //安装identity.wlang
//        response1 = wvm.wvmInstallTest(creditfilePath + identity, "");
//        String ctHashHist = JSONObject.fromObject(response1).getJSONObject("data").getString("name");

        //安装多个creditdata合约
        int i = 300;
        for (int g = 1; g < i; g++) {
            String oldStr = "CreditDataContract";
            String repStr = "CreditDataContract" + String.valueOf(g);
            FileOperation fileOper = new FileOperation();
            //安装creditdata.wlang
            fileOper.replace(resourcePath + creditfilePath + creditdata, oldStr, repStr);
            String response = wvm.wvmInstallTest(creditfilePath + creditdata, "");
            String ctHashCredit = JSONObject.fromObject(response).getJSONObject("data").getString("name");
            log.info(ctHashCredit + "****" + repStr);
            commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                    utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
            sleepAndSaveInfo(2000, "等待worldstate更新");
            oldStr = repStr;
        }

    }

    @Test
    public void creditAuthorizationAddTest() throws Exception {

//        //安装合约后会得到合约hash：由Prikey和ctName进行运算得到
//        //安装3个公共合约
//        //安装authorization.wlang
//        response1 = wvm.wvmInstallTest(creditfilePath + authorization, "");
//        String ctHashAuth = JSONObject.fromObject(response1).getJSONObject("data").getString("name");

//        //安装viewhistory.wlang
//        response1 = wvm.wvmInstallTest(creditfilePath + viewhistory, "");
//        String ctHashHist = JSONObject.fromObject(response1).getJSONObject("data").getString("name");

//        //安装identity.wlang
//        response1 = wvm.wvmInstallTest(creditfilePath + identity, "");
//        String ctHashHist = JSONObject.fromObject(response1).getJSONObject("data").getString("name");

        //安装多个creditdata合约
        int i = 300;
        for (int g = 1; g < i; g++) {
            String oldStr = "CreditDataContract";
            String repStr = "CreditDataContract" + String.valueOf(g);
            FileOperation fileOper = new FileOperation();
            //安装creditdata.wlang
            fileOper.replace(resourcePath + creditfilePath + creditdata, oldStr, repStr);
            String response = wvm.wvmInstallTest(creditfilePath + creditdata, "");
            String ctHashCredit = JSONObject.fromObject(response).getJSONObject("data").getString("name");
            log.info(ctHashCredit + "****" + repStr);
            commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                    utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
            sleepAndSaveInfo(2000, "等待worldstate更新");
            oldStr = repStr;
        }

    }

    @Test
    public void creditAuthorizationQueryTest() throws Exception {

//        //安装合约后会得到合约hash：由Prikey和ctName进行运算得到
//        //安装3个公共合约
//        //安装authorization.wlang
//        response1 = wvm.wvmInstallTest(creditfilePath + authorization, "");
//        String ctHashAuth = JSONObject.fromObject(response1).getJSONObject("data").getString("name");

//        //安装viewhistory.wlang
//        response1 = wvm.wvmInstallTest(creditfilePath + viewhistory, "");
//        String ctHashHist = JSONObject.fromObject(response1).getJSONObject("data").getString("name");

//        //安装identity.wlang
//        response1 = wvm.wvmInstallTest(creditfilePath + identity, "");
//        String ctHashHist = JSONObject.fromObject(response1).getJSONObject("data").getString("name");

        //安装多个creditdata合约
        int i = 300;
        for (int g = 1; g < i; g++) {
            String oldStr = "CreditDataContract";
            String repStr = "CreditDataContract" + String.valueOf(g);
            FileOperation fileOper = new FileOperation();
            //安装creditdata.wlang
            fileOper.replace(resourcePath + creditfilePath + creditdata, oldStr, repStr);
            String response = wvm.wvmInstallTest(creditfilePath + creditdata, "");
            String ctHashCredit = JSONObject.fromObject(response).getJSONObject("data").getString("name");
            log.info(ctHashCredit + "****" + repStr);
            commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                    utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
            sleepAndSaveInfo(2000, "等待worldstate更新");
            oldStr = repStr;
        }

    }

    @Test
    public void creditViewhistoryAddTest() throws Exception {

//        //安装合约后会得到合约hash：由Prikey和ctName进行运算得到
//        //安装3个公共合约
//        //安装authorization.wlang
//        response1 = wvm.wvmInstallTest(creditfilePath + authorization, "");
//        String ctHashAuth = JSONObject.fromObject(response1).getJSONObject("data").getString("name");

//        //安装viewhistory.wlang
//        response1 = wvm.wvmInstallTest(creditfilePath + viewhistory, "");
//        String ctHashHist = JSONObject.fromObject(response1).getJSONObject("data").getString("name");

//        //安装identity.wlang
//        response1 = wvm.wvmInstallTest(creditfilePath + identity, "");
//        String ctHashHist = JSONObject.fromObject(response1).getJSONObject("data").getString("name");

        //安装多个creditdata合约
        int i = 300;
        for (int g = 1; g < i; g++) {
            String oldStr = "CreditDataContract";
            String repStr = "CreditDataContract" + String.valueOf(g);
            FileOperation fileOper = new FileOperation();
            //安装creditdata.wlang
            fileOper.replace(resourcePath + creditfilePath + creditdata, oldStr, repStr);
            String response = wvm.wvmInstallTest(creditfilePath + creditdata, "");
            String ctHashCredit = JSONObject.fromObject(response).getJSONObject("data").getString("name");
            log.info(ctHashCredit + "****" + repStr);
            commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                    utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
            sleepAndSaveInfo(2000, "等待worldstate更新");
            oldStr = repStr;
        }

    }

    @Test
    public void creditViewhistoryQueryTest() throws Exception {

//        //安装合约后会得到合约hash：由Prikey和ctName进行运算得到
//        //安装3个公共合约
//        //安装authorization.wlang
//        response1 = wvm.wvmInstallTest(creditfilePath + authorization, "");
//        String ctHashAuth = JSONObject.fromObject(response1).getJSONObject("data").getString("name");

//        //安装viewhistory.wlang
//        response1 = wvm.wvmInstallTest(creditfilePath + viewhistory, "");
//        String ctHashHist = JSONObject.fromObject(response1).getJSONObject("data").getString("name");

//        //安装identity.wlang
//        response1 = wvm.wvmInstallTest(creditfilePath + identity, "");
//        String ctHashHist = JSONObject.fromObject(response1).getJSONObject("data").getString("name");

        //安装多个creditdata合约
        int i = 300;
        for (int g = 1; g < i; g++) {
            String oldStr = "CreditDataContract";
            String repStr = "CreditDataContract" + String.valueOf(g);
            FileOperation fileOper = new FileOperation();
            //安装creditdata.wlang
            fileOper.replace(resourcePath + creditfilePath + creditdata, oldStr, repStr);
            String response = wvm.wvmInstallTest(creditfilePath + creditdata, "");
            String ctHashCredit = JSONObject.fromObject(response).getJSONObject("data").getString("name");
            log.info(ctHashCredit + "****" + repStr);
            commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                    utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
            sleepAndSaveInfo(2000, "等待worldstate更新");
            oldStr = repStr;
        }

    }

}