package com.tjfintech.common.functionTest.smartTokenTest;

import com.tjfintech.common.*;
import com.tjfintech.common.functionTest.contract.WVMContractTest;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.*;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SmartTokenCommon {
    TestBuilder testBuilder = TestBuilder.getInstance();
    GoSmartToken st = new GoSmartToken();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    CertTool certTool = new CertTool();

    private String tokenType;


    public String beforeConfigIssueNewToken(String amount) throws Exception {

        log.info("发行数字资产");
        tokenType = "TB_" + UtilsClass.Random(10);
        double timeStampNow = System.currentTimeMillis();
        BigDecimal deadline = new BigDecimal(timeStampNow + 12356789);
        List<Map> list = smartConstructTokenList(ADDRESS1, "test", amount,null);

        String issueResp = smartIssueToken(tokenType, deadline, list, true, 0, "");
        assertEquals("200", JSONObject.fromObject(issueResp).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(issueResp, utilsClass.sdkGetTxHashType21),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        log.info("验证数字资产余额");
        verifyAddressHasBalance(ADDRESS1, tokenType, amount);

        return tokenType;

    }

    /**
     * tokenList 数组构建方法
     *
     * @param toAddr
     * @param subType
     * @param amount
     * @param list   之前的数组
     * @return
     */
    public List<Map> smartConstructTokenList(String toAddr, String subType, String amount, List<Map> list ) {

        Map<String, Object> amountMap = new HashMap<>();
        amountMap.put("address", toAddr);
        amountMap.put("amount", amount);

        if (subType != "")
            amountMap.put("subType", subType);

        List<Map>tokenList=new ArrayList<>();
        if (list == null){
            tokenList.add(amountMap);
            return tokenList;
        }else {
            for(int i = 0 ; i < list.size() ; i++) {
                tokenList.add(list.get(i));
            }
            tokenList.add(amountMap);
            return tokenList;
        }

    }


    /**
     * payAddressInfoList 数组构建方法
     *
     * @param fromAddr
     * @param payList
     * @param signList
     * @param list       之前的数组
     * @return
     */
    public List<Map> smartConstructPayAddressInfoList(String fromAddr, List<String> payList, List<String> signList, List<Map> list) {

        Map<String, Object> signMap = new HashMap<>();
        signMap.put("address", fromAddr);
        signMap.put("pubkeyList", payList);
        signMap.put("signList", signList);

        List<Map> payAddressInfoList = new ArrayList<>();
        if (list == null){
            payAddressInfoList.add(signMap);
            return payAddressInfoList;
        }else {
            for(int i = 0 ; i < list.size() ; i++) {
                payAddressInfoList.add(list.get(i));
            }
            payAddressInfoList.add(signMap);
            return payAddressInfoList;
        }
    }

    //单签账户目前的签名公私钥对为PUBKEY1 PRIKEY1
    public String smartIssueToken(String tokenType, BigDecimal deadline, List<Map> issueToList,
                                  boolean reissued, int maxLevel, String extend) throws Exception {

        //发行申请
        String isResult = st.SmartIssueTokenReq(smartAccoutContractAddress, tokenType,
                deadline, issueToList, new BigDecimal(0), reissued, maxLevel, extend);
        String sigMsg1 = JSONObject.fromObject(isResult).getJSONObject("data").getString("sigMsg");

        //发行审核
        String tempSM3Hash = certTool.getSm3Hash(PEER4IP, sigMsg1);
        String cryptMsg = certTool.sign(PEER4IP, PRIKEY1, "", tempSM3Hash, "hex");
        String approveResp = st.SmartIssueTokenApprove(sigMsg1, cryptMsg, PUBKEY1);
        return approveResp;
    }

    //转让
    //单签账户目前的签名公私钥对为PUBKEY1 PRIKEY1
    public String smartTransfer(String tokenType, List<Map> payList, List<Map> collList, String newSubType,
                                String extendArgs, String extendData) throws Exception {
        //转让申请
        String transferInfo = st.SmartTransferReq(tokenType, payList, collList, newSubType, extendArgs, extendData);

        assertThat(transferInfo, containsString("200"));
        String UTXOInfo = JSONObject.fromObject(transferInfo).getJSONObject("data").getString("UTXOInfo");

        //组装信息列表
        String signMsg = JSONObject.fromObject(transferInfo).getJSONObject("data").getString("sigMsg");

        JSONArray signMsgArray = JSONArray.fromObject(signMsg);
        ArrayList<String> pubkeys = new ArrayList();
        ArrayList<String> prikeys = new ArrayList();
        ArrayList<String> signList = new ArrayList();
        String signAddress = "";

        for (int i = 0; i < signMsgArray.size(); i++) {

            log.info("签名数组长度" + signMsgArray.size());

            String signData = JSONObject.fromObject(signMsgArray.get(i)).getString("signMsg");
            log.info(signData);
            signAddress = JSONObject.fromObject(signMsgArray.get(i)).getString("address");
            log.info(signAddress);
            pubkeys = getPubkeyListFromAddress(signAddress);
            prikeys = getPrikeyListFromAddress(signAddress);
            log.info("私钥数组长度" + prikeys.size());
            for (int j = 0; j < prikeys.size(); j++) {
//                log.info(prikeys.get(j));
                String cryptMsg = certTool.smartSign(PEER4IP, prikeys.get(j), "", signData, "hex");
                signList.add(cryptMsg);
            }
        }

        //转让审核
        List<Map> payInfoList = smartConstructPayAddressInfoList(signAddress, pubkeys, signList,null);

        String approveResp = st.SmartTEDApprove("transfer", payInfoList, UTXOInfo);

        return approveResp;

    }

    //根据地址返回公钥列表
    public ArrayList<String> getPubkeyListFromAddress(String fromAddress) throws Exception {

        ArrayList<String> pubkeys = new ArrayList();

        if (fromAddress.equals(ADDRESS1)) {
            log.info("进入这里了" + ADDRESS1);
            pubkeys.add(PUBKEY1);
        } else if (fromAddress.equals(ADDRESS2)){
            pubkeys.add(PUBKEY2);
        } else if (fromAddress.equals(MULITADD2)){ //126 (3/3签名)
            pubkeys.add(PUBKEY1);
            pubkeys.add(PUBKEY2);
            pubkeys.add(PUBKEY6);
        } else if (fromAddress.equals(MULITADD4)){ //12  (1/2签名)
            pubkeys.add(PUBKEY1);
            pubkeys.add(PUBKEY2);
        } else if (fromAddress.equals(MULITADD7)){ //16  (1/2签名)
            pubkeys.add(PUBKEY1);
            pubkeys.add(PUBKEY6);
        }

        return pubkeys;

    }


    //根据地址返回私钥列表
    public ArrayList<String> getPrikeyListFromAddress(String fromAddress) throws Exception {

        ArrayList<String> prikeys = new ArrayList();

        if (fromAddress.equals(ADDRESS1)) {
            prikeys.add(PRIKEY1);
        } else if (fromAddress.equals(ADDRESS2)){
            prikeys.add(PRIKEY2);
        } else if (fromAddress.equals(MULITADD2)){ //126 (3/3签名)
            prikeys.add(PRIKEY1);
            prikeys.add(PRIKEY2);
            prikeys.add(PRIKEY6);
        } else if (fromAddress.equals(MULITADD4)){ //12  (1/2签名)
            prikeys.add(PRIKEY1);
            prikeys.add(PRIKEY2);
        } else if (fromAddress.equals(MULITADD7)){ //16  (1/2签名)
            prikeys.add(PRIKEY1);
            prikeys.add(PRIKEY6);
        }

        return prikeys;

    }

    //销毁
    //单签账户目前的签名公私钥对为PUBKEY1 PRIKEY1
    public String smartDestroy(String tokenType, List<Map> payList, String extendArgs, String extendData) throws Exception {
        //销毁申请
        String destroyInfo = st.SmartDestroyReq(tokenType, payList, extendArgs, extendData);

        assertThat(destroyInfo, containsString("200"));
        String UTXOInfo = JSONObject.fromObject(destroyInfo).getJSONObject("data").getString("UTXOInfo");

        //组装信息列表
        String signMsg = JSONObject.fromObject(destroyInfo).getJSONObject("data").getString("sigMsg");

        JSONArray signMsgArray = JSONArray.fromObject(signMsg);
        ArrayList<String> pubkeys = new ArrayList();
        ArrayList<String> prikeys = new ArrayList();
        ArrayList<String> signList = new ArrayList();
        String signAddress = "";

        for (int i = 0; i < signMsgArray.size(); i++) {

            log.info("签名数组长度" + signMsgArray.size());

            String signData = JSONObject.fromObject(signMsgArray.get(i)).getString("signMsg");
            log.info(signData);
            signAddress = JSONObject.fromObject(signMsgArray.get(i)).getString("address");
            log.info(signAddress);
            pubkeys = getPubkeyListFromAddress(signAddress);
            prikeys = getPrikeyListFromAddress(signAddress);
            log.info("私钥数组长度" + prikeys.size());
            for (int j = 0; j < prikeys.size(); j++) {
                String cryptMsg = certTool.smartSign(PEER4IP, prikeys.get(j), "", signData, "hex");
                signList.add(cryptMsg);
            }
        }

        //审核
        List<Map> payInfoList = smartConstructPayAddressInfoList(signAddress, pubkeys, signList,null);

        String approveResp = st.SmartTEDApprove("destroy", payInfoList, UTXOInfo);
        return approveResp;

    }


    //转换
    //单签账户目前的签名公私钥对为PUBKEY1 PRIKEY1
    public String smartExchange(String tokenType, List<Map> payList, List<Map> collList, String newTokenType,
                                String extendArgs, String extendData) throws Exception {
        //转换申请
        String exchangeInfo = st.SmartExchangeReq(tokenType, payList, collList, newTokenType,
                extendArgs, extendData);

        assertThat(exchangeInfo, containsString("200"));
        String UTXOInfo = JSONObject.fromObject(exchangeInfo).getJSONObject("data").getString("UTXOInfo");

        //组装信息列表
        String signMsg = JSONObject.fromObject(exchangeInfo).getJSONObject("data").getString("sigMsg");

        JSONArray signMsgArray = JSONArray.fromObject(signMsg);
        ArrayList<String> pubkeys = new ArrayList();
        ArrayList<String> prikeys = new ArrayList();
        ArrayList<String> signList = new ArrayList();
        String signAddress = "";

        for (int i = 0; i < signMsgArray.size(); i++) {

            log.info("签名数组长度" + signMsgArray.size());

            String signData = JSONObject.fromObject(signMsgArray.get(i)).getString("signMsg");
            log.info(signData);
            signAddress = JSONObject.fromObject(signMsgArray.get(i)).getString("address");
            log.info(signAddress);
            pubkeys = getPubkeyListFromAddress(signAddress);
            prikeys = getPrikeyListFromAddress(signAddress);
            log.info("私钥数组长度" + prikeys.size());
            for (int j = 0; j < prikeys.size(); j++) {
//                log.info(prikeys.get(j));
                String cryptMsg = certTool.smartSign(PEER4IP, prikeys.get(j), "", signData, "hex");
                signList.add(cryptMsg);
            }
        }

        //审核
        List<Map> payInfoList = smartConstructPayAddressInfoList(signAddress, pubkeys, signList,null);

        String approveResp = st.SmartTEDApprove("exchange", payInfoList, UTXOInfo);

        return approveResp;

    }


    //验证账户地址余额
    public void verifyAddressHasBalance(String address, String tokenType, String amount) throws Exception {

        String queryBalance = st.SmartGetBalanceByAddr(address, tokenType);
        assertEquals("200", JSONObject.fromObject(queryBalance).getString("state"));
        assertEquals("success", JSONObject.fromObject(queryBalance).getString("message"));

        if (tokenType.equals("")){
            assertThat(JSONObject.fromObject(queryBalance).getJSONObject("data").getString(tokenType), containsString(amount));
            assertThat(JSONObject.fromObject(queryBalance).getJSONObject("data").getString(tokenType), containsString("test"));
            assertThat(JSONObject.fromObject(queryBalance).getJSONObject("data").getString(tokenType), containsString("true"));
        }else{
            assertThat(JSONObject.fromObject(queryBalance).getString("data"), containsString(amount));
            assertThat(JSONObject.fromObject(queryBalance).getString("data"), containsString("test"));
            assertThat(JSONObject.fromObject(queryBalance).getString("data"), containsString("true"));
        }
    }

    //验证账户地址余额
    public void verifyAddressNoBalance(String address, String tokenType) throws Exception {

        String queryBalance = st.SmartGetBalanceByAddr(address, tokenType);
        assertEquals("200", JSONObject.fromObject(queryBalance).getString("state"));
        assertEquals("success", JSONObject.fromObject(queryBalance).getString("message"));
        assertEquals("null", JSONObject.fromObject(queryBalance).getString("data"));

    }

}

