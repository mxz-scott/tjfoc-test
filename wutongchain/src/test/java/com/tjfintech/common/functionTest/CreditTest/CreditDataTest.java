package com.tjfintech.common.functionTest.CreditTest;

import com.alibaba.fastjson.JSON;
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
import org.junit.*;
import org.junit.runners.MethodSorters;
import org.mockito.internal.configuration.MockAnnotationProcessor;

import java.util.*;

import static com.tjfintech.common.utils.UtilsClass.*;
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

        if (tokenAccount1.isEmpty()) {
            BeforeCondition beforeCondition = new BeforeCondition();
            beforeCondition.updatePubPriKey();
        }
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
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        String authname = JSONObject.fromObject(response).getJSONObject("data").getString("name");

        //安装viewhistory.wlang
        response = wvm.wvmInstallTest(creditfilePath + viewhistory, "");
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        String viewname = JSONObject.fromObject(response).getJSONObject("data").getString("name");

        //安装identity.wlang
        response = wvm.wvmInstallTest(creditfilePath + identity, "");
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        String identityname = JSONObject.fromObject(response).getJSONObject("data").getString("name");

        //安装creditdata.wlang
        FileOperation fileOper = new FileOperation();
        fileOper.replace(resourcePath + creditfilePath + creditdata, "CreditDataContract", "Credit225DataContract");
        response = wvm.wvmInstallTest(creditfilePath + "creditdata" + "_temp.wlang", "");
//        response = wvm.wvmInstallTest(creditfilePath + creditdata, "");
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        String creditname = JSONObject.fromObject(response).getJSONObject("data").getString("name");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        //添加身份
        String code = "ZX" + utilsClass.Random(8);
        response = credit.creditIdentityAdd
                (code, code, "征信机构", creditname, PUBKEY1, "aa", "bb");
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        //查询身份
        response = credit.creditIdentityQuery(code);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(code, JSONObject.fromObject(response).getJSONObject("data").getString("Code"));

        //查询所有身份
        response = credit.creditIdentityQueryAll();
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(true, response.contains(code));

        //添加征信数据
        String enterprisecode = "enterprise" + utilsClass.Random(8);
        List<Map> creditlist = creditCommonFunc.constructCreditData(enterprisecode, enterprisecode, code, code, "hash1",
                "a", "2020-09-24-15-00-00", "a", "A");
        response = credit.creditCreditdataAdd(creditlist, "");
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        //查询征信数据
        response = credit.creditCreditdataQuery(enterprisecode);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertEquals(enterprisecode, JSONObject.fromObject(response).getJSONArray("data").getJSONObject(0).getString("EnterpriseCode"));

        //添加授权记录
        ArrayList<String> orgid = new ArrayList<>();
        orgid.add(code);
        List<Map> authlist = creditCommonFunc.constructAuthorizationData(enterprisecode, enterprisecode, code, code, "ICBC", "ICBC",
                code, code, "aa", "https://www.baidu.com/", "hash1", "aa",
                "2020-09-24-15-00-00", 10);
        response = credit.creditAuthorizationAdd(orgid, authlist);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        String authkey = JSONObject.fromObject(response).getJSONObject("data").getJSONArray("key").getString(0);

        //查询授权数据
        response = credit.creditAuthorizationQuery(authkey);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        assertThat(response, allOf(containsString(code), containsString(enterprisecode)));

        //查询记录上链
        List<Map> viewlist = creditCommonFunc.constructViewData(enterprisecode, enterprisecode, code, code, "ICBC", "ICBC",
                code, code, "aa", "aa", "aa", "2020-09-24-15-00-00");
        response = credit.creditViewhistoryAdd(orgid, viewlist);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        String viewkey;

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


}