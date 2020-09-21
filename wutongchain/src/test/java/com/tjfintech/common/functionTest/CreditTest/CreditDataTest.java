package com.tjfintech.common.functionTest.CreditTest;

import com.alibaba.fastjson.JSON;
import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CommonFunc;
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

import java.util.HashMap;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class CreditDataTest {

    TestBuilder testBuilder = TestBuilder.getInstance();
    Store store = testBuilder.getStore();
    CommonFunc commonFunc = new CommonFunc();
    UtilsClass utilsClass = new UtilsClass();
    String creditfilePath = "credit/";
    String identity = "identity.wlang";
    String authorization = "authorization.wlang";
    String creditdata = "creditdata.wlang";
    String viewhistory = "viewhistory.wlang";

    WVMContractTest wvm = new WVMContractTest();


    /**
     * 安装合约
     * 顺序要先执行 identity.wlang
     * authorization.wlang
     * creditdata.wlang
     * viewhistory.wlang
     */
    @Test
    public void creditRelatedTest() throws Exception {

        //安装identity合约
        String installfile = creditfilePath + identity;
        //安装合约后会得到合约hash：由Prikey和ctName进行运算得到
        String response1 = wvm.wvmInstallTest(installfile, "");
        String txHash1 = JSONObject.fromObject(response1).getJSONObject("data").getString("txId");
        String ctHashId = JSONObject.fromObject(response1).getJSONObject("data").getString("name");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        sleepAndSaveInfo(2000, "等待worldstate更新");

        //安装其他三个合约  需要将其他三个合约的identityContractAddr 更新为上一步骤中的ctHash

        String repStr = "string identityContractAddr  = \"" + ctHashId + "\"";
        FileOperation fileOper = new FileOperation();

        //安装authorization.wlang
        fileOper.replaceKeyword(resourcePath + creditfilePath + authorization, "string identityContractAddr", repStr);
        response1 = wvm.wvmInstallTest(creditfilePath + authorization, "");
        String ctHashAuth = JSONObject.fromObject(response1).getJSONObject("data").getString("name");

        //安装creditdata.wlang
        fileOper.replaceKeyword(resourcePath + creditfilePath + creditdata, "string identityContractAddr", repStr);
        response1 = wvm.wvmInstallTest(creditfilePath + creditdata, "");
        String ctHashCredit = JSONObject.fromObject(response1).getJSONObject("data").getString("name");

        //安装viewhistory.wlang
        fileOper.replaceKeyword(resourcePath + creditfilePath + viewhistory, "string identityContractAddr", repStr);
        response1 = wvm.wvmInstallTest(creditfilePath + viewhistory, "");
        String ctHashHist = JSONObject.fromObject(response1).getJSONObject("data").getString("name");

        //首先执行添加identity 添加id操作
        String response = wvm.invokeNew(ctHashId,
                "AddIdentity",
                "\"{\"Name\":\"123\",\"Code\":\"456\",\"Type\":\"\"," +
                        "\"PubKey\":\"LS0tLS1CRUdJTiBQVUJMSUMgS0VZLS0tLS0NCk1Ga3dFd1lIS29aSXpqMENBUVlJS29FY3oxVUJnaTBEUWdBRWQxdkt0aGhISmw3SWZFbHNWN3A5QmZVWEtYSWcNCnRhYm8zeEZyNU41d2x4VnRCbTAxVDEwQjlQSFYzOWthYXFNaS9XcURPM01ucStDMmJ6bmpSa04wMGc9PQ0KLS0tLS1FTkQgUFVCTElDIEtFWS0tLS0tDQo=\"," +
                        "\"Address\":\"\",\"PubKeyEnc\":\"\",\"Description\":\"\",\"Sender\":\"\",\"TxTime\":0,\"TxHash\":\"\",\"BlockNo\":0}\"",
                        "304502207e576f15d61b4bf696e62aafcaf7fc42eab58bb19c4cd4a24ef1136dd7b995c7022100e0d1c9c7a859d239e2cd77d04ae46ac7d5f3d99fef0d6893091be35c7087dc5c");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(JSONObject.fromObject(response).getJSONObject("data").getString("txId"))).getString("state"));

        //添加creditdata
        response = wvm.invokeNew(ctHashCredit,
                "AddCreditDataList",
                "\"{\"CreditDataList\":[{\"EnterpriseName\":\"123\",\"EnterpriseCode\":\"123\",\"CreditName\":\"123123\"," +
                        "\"CreditCode\":\"\",\"Hash\":\"123\",\"Catalogue\":\"\",\"MakeTime\":0,\"AccessInterface\":\"\"," +
                        "\"Description\":\"\",\"Sender\":\"\",\"TxTime\":0,\"TxHash\":\"\",\"BlockNo\":0}," +
                        "{\"EnterpriseName\":\"456\",\"EnterpriseCode\":\"\",\"CreditName\":\"456456\"," +
                        "\"CreditCode\":\"\",\"Hash\":\"\",\"Catalogue\":\"\",\"MakeTime\":0,\"AccessInterface\":\"\"," +
                        "\"Description\":\"\",\"Sender\":\"\",\"TxTime\":0,\"TxHash\":\"\",\"BlockNo\":0}]}\"",
                "3046022100d990bc25ce586263b30b42bc2352423e0c6f72e9ed5e6b30b9919b0867b1fe8302210083ebbf324fd345cffa92bedf352c8919d7f37f8085e32014aa499e50340629f4","456");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(JSONObject.fromObject(response).getJSONObject("data").getString("txId"))).getString("state"));


        //添加Auth
        response = wvm.invokeNew(ctHashAuth,
                "AddAuthorizationList",
                "\"{\"AuthorizationList\":[{\"EnterpriseName\":\"123\",\"EnterpriseCode\":\"111\"," +
                        "\"BankName\":\"111\",\"BankCode\":\"\",\"CreditName\":\"123123\",\"CreditCode\":\"111\"," +
                        "\"RemoteCreditName\":\"222\",\"RemoteCreditCode\":\"222\",\"LocalRemote\":\"\"," +
                        "\"AuthorizedUrl\":\"\",\"AuthorizedHash\":\"\",\"AuthType\":\"333\",\"AuthStartTime\":0," +
                        "\"AuthDays\":0,\"Sender\":\"\",\"TxTime\":0,\"TxHash\":\"\",\"BlockNo\":0}," +
                        "{\"EnterpriseName\":\"456\",\"EnterpriseCode\":\"\",\"BankName\":\"444\",\"BankCode\":\"\"," +
                        "\"CreditName\":\"456456\",\"CreditCode\":\"\",\"RemoteCreditName\":\"555\",\"RemoteCreditCode\":\"\"," +
                        "\"LocalRemote\":\"\",\"AuthorizedUrl\":\"\",\"AuthorizedHash\":\"\",\"AuthType\":\"666\"," +
                        "\"AuthStartTime\":0,\"AuthDays\":0,\"Sender\":\"\",\"TxTime\":0,\"TxHash\":\"\",\"BlockNo\":0}]}\"",
                "3044022057d1be68213072cb877d0672cd816da969520b814889f9b64266a2d7a0776d4c02207254e62d945596d52271379871b80a75bf17922ff94ab82b778aa4e935b465b3","456");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(JSONObject.fromObject(response).getJSONObject("data").getString("txId"))).getString("state"));

        //添加History
        response = wvm.invokeNew(ctHashHist,
                "AddViewHistoryList",
                "\"{\"ViewHistoryList\":[{\"EnterpriseName\":\"123\",\"EnterpriseCode\":\"555\"," +
                        "\"BankName\":\"111\",\"BankCode\":\"\",\"CreditName\":\"123123\",\"CreditCode\":\"555\"," +
                        "\"RemoteCreditName\":\"222\",\"RemoteCreditCode\":\"666\",\"LocalRemote\":\"\"," +
                        "\"QueryOperator\":\"\",\"QueryReason\":\"\",\"QueryTime\":0,\"Sender\":\"\",\"TxTime\":0," +
                        "\"TxHash\":\"\",\"BlockNo\":0},{\"EnterpriseName\":\"456\",\"EnterpriseCode\":\"\"," +
                        "\"BankName\":\"444\",\"BankCode\":\"\",\"CreditName\":\"456456\",\"CreditCode\":\"\"," +
                        "\"RemoteCreditName\":\"555\",\"RemoteCreditCode\":\"\",\"LocalRemote\":\"\",\"QueryOperator\":\"\"," +
                        "\"QueryReason\":\"\",\"QueryTime\":0,\"Sender\":\"\",\"TxTime\":0,\"TxHash\":\"\",\"BlockNo\":0}]}\"",
                "30450221008d0cc320296c47c1a74d661b0b69d05dd107ff268f8a84f5cb93aeffd8855be502206ae96481a4a61ee28d308902b1c15029db5aa2a19f8811b0f4323465346fab7e","456");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        assertEquals("200", JSONObject.fromObject(
                store.GetTxDetail(JSONObject.fromObject(response).getJSONObject("data").getString("txId"))).getString("state"));
    }
}