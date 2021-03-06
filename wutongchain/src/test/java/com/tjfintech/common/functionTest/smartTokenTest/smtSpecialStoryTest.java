//package com.tjfintech.common.functionTest.smartTokenTest;
//
//import com.tjfintech.common.BeforeCondition;
//import com.tjfintech.common.CertTool;
//import com.tjfintech.common.CommonFunc;
//import com.tjfintech.common.Interface.MultiSign;
//import com.tjfintech.common.Interface.SoloSign;
//import com.tjfintech.common.TestBuilder;
//import com.tjfintech.common.functionTest.contract.WVMContractTest;
//import com.tjfintech.common.utils.FileOperation;
//import com.tjfintech.common.utils.UtilsClass;
//import lombok.extern.slf4j.Slf4j;
//import net.sf.json.JSONObject;
//import org.junit.BeforeClass;
//import org.junit.FixMethodOrder;
//import org.junit.Test;
//import org.junit.runners.MethodSorters;
//
//import java.math.BigDecimal;
//import java.util.List;
//import java.util.Map;
//
//import static com.tjfintech.common.utils.UtilsClass.*;
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertThat;
//
////import static com.tjfintech.common.functionTest.store.StoreTest.SLEEPTIME;
//
//@Slf4j
//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
//public class smtSpecialStoryTest {
//    TestBuilder testBuilder= TestBuilder.getInstance();
//    MultiSign multiSign =testBuilder.getMultiSign();
//    SoloSign soloSign = testBuilder.getSoloSign();
//
//    UtilsClass utilsClass=new UtilsClass();
//    CommonFunc commonFunc = new CommonFunc();
//    CertTool certTool = new CertTool();
//
//
//    private static String tokenType;
//    private static String tokenType2;
//
//    private static String issueAmount1;
//    private static String issueAmount2;
//
//    private static String actualAmount1;
//    private static String actualAmount2;
//
//    String constFileName = "wvm\\account_simple.wlang";
//    String contractFileName = "wvm\\account_simple.wlang";
//    String HQuotaFileName = "wvm\\account_simple_HQuota.wlang";
//
//    @BeforeClass
//    public static void beforeClass() throws Exception {
//        if (MULITADD1.isEmpty()) {
//            BeforeCondition bf = new BeforeCondition();
//            bf.updatePubPriKey();
//            bf.createAddresses();
//        }
//
//    }
//
////    @Before
//    public void beforeConfig() throws Exception {
//
//        //??????smart token???????????????
//        installSmartAccountContract(contractFileName);
//
//
//        issueAmount1 = "10000.12345678912345";
//        issueAmount2 = "20000.876543212345";
//
//        if (UtilsClass.PRECISION == 10) {
//            actualAmount1 = "10000.1234567891";
//            actualAmount2 = "20000.8765432123";
//        }else {
//            actualAmount1 = "10000.123456";
//            actualAmount2 = "20000.876543";
//        }
//
//        double timeStampNow = System.currentTimeMillis();
//        BigDecimal deadline = new BigDecimal(timeStampNow + 12356789);
//
//        List<Map>list = utilsClass.smartConstuctIssueToList(ADDRESS1,actualAmount1);
//
//
//        log.info("????????????token");
//        tokenType = "SOLOTC-"+UtilsClass.Random(6);
//        String issueResp = smartIssueToken(tokenType,deadline,list);
//        assertEquals("200",JSONObject.fromObject(issueResp).getString("state"));
//
//        list.clear();
//        list = utilsClass.smartConstuctIssueToList(ADDRESS1,actualAmount2);
//        tokenType2 = "SOLOTC-"+UtilsClass.Random(6);
//        String issueResp2 = smartIssueToken(tokenType2,deadline,list);
//        assertEquals("200",JSONObject.fromObject(issueResp2).getString("state"));
//
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(issueResp,utilsClass.sdkGetTxHashType21),
//                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//
//        log.info("???????????????????????????token??????");
//        String queryBalance = multiSign.SmartGetBalanceByAddr(ADDRESS1, "");
//        assertEquals(actualAmount1,JSONObject.fromObject(queryBalance).getJSONObject("data").getJSONObject("detail").getString(tokenType));
//        assertEquals(actualAmount1,JSONObject.fromObject(queryBalance).getJSONObject("data").getJSONObject("detail").getString(tokenType));
//
//        assertEquals("200",JSONObject.fromObject(queryBalance).getString("state"));
//
//    }
//
//    /**
//     *  ????????????????????? ?????????????????????????????????????????????6????????? uint64????????????18446744073709
//     *  ??????????????????account_simple_HQuota.wlang ?????????uint64????????????18446744073709 * 2
//     *
//     */
//    @Test
//    public void TC2291_TestMaxValueForuint64Max()throws Exception {
//
//        //??????HQuota?????? ???????????????????????? ???????????????????????????????????????????????????
//        contractFileName = HQuotaFileName;
//        installSmartAccountContract(contractFileName);
//
//        //
//        actualAmount1 = "36893488147418";
//        double timeStampNow = System.currentTimeMillis();
//        BigDecimal deadline = new BigDecimal(timeStampNow + 12356789);
//
//        List<Map>list = utilsClass.smartConstuctIssueToList(ADDRESS1,actualAmount1);
//
//
//        log.info("???????????????????????????");
//        tokenType = "soloMaxAmt-" + UtilsClass.Random(6);
//        String issueResp = smartIssueToken(tokenType,deadline,list);
//        assertEquals("400", JSONObject.fromObject(issueResp).getString("state"));
//        assertEquals("Token amount must be a valid number and less than 18446744073709", JSONObject.fromObject(issueResp).getString("message"));
//
//
//        log.info("?????????????????????token??????");
//        String response1 = multiSign.SmartGetBalanceByAddr(ADDRESS1, tokenType);
//
//        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
//        assertEquals("0",JSONObject.fromObject(response1).getJSONObject("data").getJSONObject("detail").getString(tokenType));
//    }
//
//
//    /**
//     *  ???????????????????????? ???????????????????????????????????? ????????????
//     *
//     */
//    @Test
//    public void TC2284_testOnceIssAmountOverCtLimit()throws Exception {
//        //??????HQuota?????? ???????????????????????? ???????????????????????????????????????????????????
//        contractFileName = constFileName;
//        installSmartAccountContract(contractFileName);
//
//        actualAmount1 = "8446744073709";
//
//        double timeStampNow = System.currentTimeMillis();
//        BigDecimal deadline = new BigDecimal(timeStampNow + 12356789);
//
//        List<Map>list = utilsClass.smartConstuctIssueToList(ADDRESS1,actualAmount1);
//
//
//        log.info("???????????????????????????");
//        tokenType = "soloMaxAmt-"+UtilsClass.Random(6);
//        String issueResp = smartIssueToken(tokenType,deadline,list);
//        assertEquals("200",JSONObject.fromObject(issueResp).getString("state"));
//
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(issueResp,utilsClass.sdkGetTxHashType21),
//                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//
//        log.info("?????????????????????token??????");
//        String response1 = multiSign.SmartGetBalanceByAddr(ADDRESS1, tokenType);
//
//        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
//        assertEquals(false,response1.contains(tokenType));
////        assertEquals(actualAmount1,JSONObject.fromObject(response1).getJSONObject("data").getJSONObject("detail").getString(tokenType));
//    }
//
//    /**
//     *  ???????????????????????? ???????????????????????????????????? ??????tokentype
//     *
//     */
//    @Test
//    public void TC2305_testMultiIssAmountOverCtLimit()throws Exception {
//        //??????HQuota?????? ???????????????????????? ???????????????????????????????????????????????????
//        contractFileName = constFileName;
//        installSmartAccountContract(contractFileName);
//        //??????????????????????????????????????????1000000:1 ???????????????50000000000???sdk??????????????????????????????
//        actualAmount1 = "30000";
//
//        double timeStampNow = System.currentTimeMillis();
//        BigDecimal deadline = new BigDecimal(timeStampNow + 12356789);
//
//        List<Map>list = utilsClass.smartConstuctIssueToList(ADDRESS1,actualAmount1);
//
//
//        log.info("???????????????");
//        tokenType = "soloMaxAmt-"+UtilsClass.Random(6);
//        String issueResp = smartIssueToken(tokenType,deadline,list);
//        assertEquals("200",JSONObject.fromObject(issueResp).getString("state"));
//
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(issueResp,utilsClass.sdkGetTxHashType21),
//                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//
//        log.info("?????????????????????token??????");
//        String response1 = multiSign.SmartGetBalanceByAddr(ADDRESS1, tokenType);
//
//        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
//        assertEquals(actualAmount1,JSONObject.fromObject(response1).getJSONObject("data").getJSONObject("detail").getString(tokenType));
//
//        log.info("?????????????????????token");
//        issueResp = smartIssueToken(tokenType,deadline,list);
//        assertEquals("200",JSONObject.fromObject(issueResp).getString("state"));
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(issueResp,utilsClass.sdkGetTxHashType21),
//                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//
//        log.info("?????????????????????token??????");
//        response1 = multiSign.SmartGetBalanceByAddr(ADDRESS1, tokenType);
//
//        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
//        assertEquals(actualAmount1,JSONObject.fromObject(response1).getJSONObject("data").getJSONObject("detail").getString(tokenType));
//
//
//        log.info("?????????????????????");
//        tokenType2 = "soloMaxAmt-"+UtilsClass.Random(6);
//        issueResp = smartIssueToken(tokenType2,deadline,list);
//        assertEquals("200",JSONObject.fromObject(issueResp).getString("state"));
//        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(issueResp,utilsClass.sdkGetTxHashType21),
//                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
//
//        log.info("?????????????????????token??????");
//        response1 = multiSign.SmartGetBalanceByAddr(ADDRESS1, tokenType);
//
//        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
//        assertEquals(false,response1.contains(tokenType2));
//    }
//
//
////    /**
////     *  ????????????????????????
////     *
////     */
////    @Test
////    public void testMaxValue002()throws Exception {
////
////        if (UtilsClass.PRECISION == 10) {
////            actualAmount1 = "1844674407";
////        }else {
////            actualAmount1 = "18446744073709";
////        }
////
////        double timeStampNow = System.currentTimeMillis();
////        BigDecimal deadline = new BigDecimal(timeStampNow + 12356789);
////
////        List<Map>list = utilsClass.smartConstuctIssueToList(ADDRESS1,actualAmount1);
////
////
////        log.info("???????????????????????????");
////        tokenType = "soloMaxAmt-"+UtilsClass.Random(6);
////        String issueResp = smartIssueToken(tokenType,deadline,list);
////        assertEquals("200",JSONObject.fromObject(issueResp).getString("state"));
////
////        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(issueResp,utilsClass.sdkGetTxHashType21),
////                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
////
////        log.info("?????????????????????token??????");
////        String response1 = multiSign.SmartGetBalanceByAddr(ADDRESS1, tokenType);
////
////        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
////        assertEquals(false,response1.contains(tokenType));//??????????????????????????????50000000000
//////        assertEquals("",JSONObject.fromObject(response1).getJSONObject("data").getJSONObject("detail").getString(tokenType));
////    }
////
////    /**
////     *  ????????????????????? ?????????????????????
////     *
////     */
////    @Test
////    public void testMaxValue003()throws Exception {
////
////        actualAmount1 = "50000000000";  //????????????????????????
////        double timeStampNow = System.currentTimeMillis();
////        BigDecimal deadline = new BigDecimal(timeStampNow + 12356789);
////
////        List<Map>list = utilsClass.smartConstuctIssueToList(ADDRESS1,actualAmount1);
////
////
////        log.info("???????????????????????????");
////        tokenType = "soloMaxAmt-"+UtilsClass.Random(6);
////        String issueResp = smartIssueToken(tokenType,deadline,list);
////        assertEquals("200",JSONObject.fromObject(issueResp).getString("state"));
////
////        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(issueResp,utilsClass.sdkGetTxHashType21),
////                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);
////
////        log.info("?????????????????????token??????");
////        String response1 = multiSign.SmartGetBalanceByAddr(ADDRESS1, "");
////
////        assertEquals("200",JSONObject.fromObject(response1).getString("state"));
////        assertEquals(actualAmount1,JSONObject.fromObject(response1).getJSONObject("data").getJSONObject("detail").getString(tokenType));
////    }
//
//
//
//    public void installSmartAccountContract(String abfileName)throws Exception{
//        WVMContractTest wvmContractTestSA = new WVMContractTest();
//        UtilsClass utilsClassSA = new UtilsClass();
//        CommonFunc commonFuncTeSA = new CommonFunc();
//        FileOperation fileOper = new FileOperation();
//        String originKey = "contract Account";
//        fileOper.replace(testDataPath + abfileName, originKey, originKey+System.currentTimeMillis());
//
//        //??????????????????????????????hash??????Prikey???ctName??????????????????
//        String response = wvmContractTestSA.wvmInstallTest(abfileName.substring(0,abfileName.lastIndexOf(".")) + "_temp.wlang","");
//        assertEquals("200",JSONObject.fromObject(response).getString("state"));
//        commonFuncTeSA.sdkCheckTxOrSleep(commonFuncTeSA.getTxHash(response,utilsClassSA.sdkGetTxHashType20),
//                    utilsClassSA.sdkGetTxDetailTypeV2,SLEEPTIME);
//        smartAccoutContractAddress = JSONObject.fromObject(response).getJSONObject("data").getString("name");
//    }
//
//    //??????????????????????????????????????????PUBKEY1 PRIKEY1
//    public String smartIssueToken(String tokenType,BigDecimal deadline,List<Map> issueToList)throws Exception{
//        String isResult= multiSign.SmartIssueTokenReq(smartAccoutContractAddress,tokenType,true,
//                deadline,new BigDecimal(0),0,issueToList,"");
//        String sigMsg1 = JSONObject.fromObject(isResult).getJSONObject("data").getString("sigMsg");
////        assertEquals(sigMsg1,String.valueOf(Hex.encodeHex(
////                JSONObject.fromObject(isResult).getJSONObject("data").getString("msg").getBytes(StandardCharsets.UTF_8))));
//
//        String tempSM3Hash = certTool.getSm3Hash(PEER4IP,sigMsg1);
//        String cryptMsg = certTool.sign(PEER4IP ,PRIKEY1,"",tempSM3Hash,"hex");
//
//        String pubkey = utilsClass.readStringFromFile(testDataPath + "cert/SM2/keys1/pubkey.pem").replaceAll("\r\n","\n");
////        pubkey = (new BASE64Decoder()).decodeBuffer(PUBKEY1).toString().replaceAll("\r\n","\n");
//
//        String approveResp = multiSign.SmartIssueTokenApprove(sigMsg1,cryptMsg,pubkey);
//        return approveResp;
//    }
//
//
//}
