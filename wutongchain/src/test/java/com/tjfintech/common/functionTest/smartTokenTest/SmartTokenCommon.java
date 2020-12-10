package com.tjfintech.common.functionTest.smartTokenTest;

import com.tjfintech.common.*;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.functionTest.contract.WVMContractTest;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.checkerframework.checker.units.qual.A;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SmartTokenCommon {
    TestBuilder testBuilder= TestBuilder.getInstance();
    MultiSign multiSign =testBuilder.getMultiSign();
    SoloSign soloSign = testBuilder.getSoloSign();
    Store store = testBuilder.getStore();

    GoSmartToken st = new GoSmartToken();
    UtilsClass utilsClass=new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    CertTool certTool = new CertTool();

    private String tokenType;
    String constFileName = "account_simple.wlang";
    String contractFileName = "account_simple.wlang";


    public String beforeConfigIssueNewToken(String amount) throws Exception {

        //安装smart token定制化合约
        installSmartAccountContract(contractFileName);

        log.info("发行数字资产");
        tokenType = "TB_"+UtilsClass.Random(10);
        double timeStampNow = System.currentTimeMillis();
        BigDecimal deadline = new BigDecimal(timeStampNow + 12356789);
        List<Map>list = smartConstructTokenList(ADDRESS1, "test", amount);

        String issueResp = smartIssueToken(tokenType,deadline,list,true, 0, "");
        assertEquals("200",JSONObject.fromObject(issueResp).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(issueResp,utilsClass.sdkGetTxHashType21),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        log.info("查询数字资产余额");
        String queryBalance = st.SmartGetBalanceByAddr(ADDRESS1, "");
        assertEquals("200",JSONObject.fromObject(queryBalance).getString("state"));
        assertThat(JSONObject.fromObject(queryBalance).getJSONObject("data").getString(tokenType),containsString(amount));
        assertThat(JSONObject.fromObject(queryBalance).getJSONObject("data").getString(tokenType),containsString("test"));
        assertThat(JSONObject.fromObject(queryBalance).getJSONObject("data").getString(tokenType),containsString("true"));

        return tokenType;

    }

    //安装账户合约
    public void installSmartAccountContract(String abfileName)throws Exception{
        WVMContractTest wvmContractTestSA = new WVMContractTest();
        UtilsClass utilsClassSA = new UtilsClass();
        CommonFunc commonFuncTeSA = new CommonFunc();

        //如果smartAccoutCtHash为空或者contractFileName不为constFileName 即"wvm\\account_simple.wlang" 时会重新安装
        if(smartAccoutContractAddress.equals("") || (!contractFileName.equals(constFileName))){
            //安装
            String response =wvmContractTestSA.wvmInstallTest(abfileName,"");
            assertEquals("200",JSONObject.fromObject(response).getString("state"));
            commonFuncTeSA.sdkCheckTxOrSleep(commonFuncTeSA.getTxHash(response,utilsClassSA.sdkGetTxHashType20),
                    utilsClassSA.sdkGetTxDetailTypeV2,SLEEPTIME);
            smartAccoutContractAddress = JSONObject.fromObject(response).getJSONObject("data").getString("name");
        }
    }

    /**
     * tokenList 数组构建方法
     * @param toAddr
     * @param subType
     * @param amount
     * @return
     */
    public  List<Map>   smartConstructTokenList(String toAddr, String subType, String amount){

        Map<String,Object>amountMap=new HashMap<>();
        amountMap.put("address",toAddr);
        amountMap.put("amount",amount);

        if(subType != "")
            amountMap.put("subType",subType);

        List<Map>tokenList=new ArrayList<>();
        tokenList.add(amountMap);
        return tokenList;
    }

    /**
     * payAddressInfoList 数组构建方法
     * @param fromAddr
     * @param payList
     * @param signList
     * @return
     */
    public  List<Map>   smartConstructPayAddressInfoList(String fromAddr, List<Map> payList, List<Map> signList){

        Map<String,Object>signMap=new HashMap<>();
        signMap.put("address",fromAddr);
        signMap.put("pubkeyList",payList);
        signMap.put("signList",signList);

        List<Map>payAddressInfoList=new ArrayList<>();
        payAddressInfoList.add(signMap);
        return payAddressInfoList;
    }

    //单签账户目前的签名公私钥对为PUBKEY1 PRIKEY1
    public String smartIssueToken(String tokenType, BigDecimal deadline, List<Map> issueToList,
                                  boolean reissued, int maxLevel, String extend)throws Exception{

        //发行申请
        String isResult= st.SmartIssueTokenReq(smartAccoutContractAddress,tokenType,
                deadline,issueToList, new BigDecimal(0), reissued, maxLevel, extend);
        String sigMsg1 = JSONObject.fromObject(isResult).getJSONObject("data").getString("sigMsg");

        //发行审核
        String tempSM3Hash = certTool.getSm3Hash(PEER4IP,sigMsg1);
        String cryptMsg = certTool.sign(PEER4IP ,PRIKEY1,"",tempSM3Hash,"hex");
//        String pubkey = utilsClass.readStringFromFile(testDataPath + "cert/SM2/keys1/pubkey.pem").replaceAll("\r\n","\n");

        String approveResp = st.SmartIssueTokenApprove(sigMsg1,cryptMsg,PUBKEY1);
        return approveResp;
    }

    //单签账户目前的签名公私钥对为PUBKEY1 PRIKEY1
    public String smartTransfer(String tokenType, List<Map> payList, List<Map> collList,
                                String newSubType, String extendArgs, String extendData)throws Exception {
        //转让申请
        String transferInfo= st.SmartTransferReq(tokenType, payList, collList, newSubType, extendArgs, extendData);

        assertThat(transferInfo, containsString("200"));
        String UTXOInfo = JSONObject.fromObject(transferInfo).getJSONObject("data").getString("UTXOInfo");

        //组装信息列表
        String signMsg = JSONObject.fromObject(transferInfo).getJSONObject("data").getString("sigMsg");

        JSONArray signMsgArray = JSONArray.fromObject(signMsg);
        ArrayList  pubkeys = new ArrayList();
        ArrayList  signList = new ArrayList();

        for(int i = 0; i < signMsgArray.size(); i++) {
            String signData = JSONObject.fromObject(signMsgArray.get(i)).getString("signMsg");
            String cryptMsg = certTool.sign(PEER4IP ,PRIKEY1,"",signData,"hex");
            pubkeys.add(PUBKEY1);
            signList.add(cryptMsg);
        }

        //转让审核
        List<Map> payInfoList = smartConstructPayAddressInfoList(ADDRESS1, pubkeys, signList);

        String approveResp = st.SmartTransferApprove(payInfoList,UTXOInfo);
        log.info(approveResp);
        return approveResp;

    }

}
