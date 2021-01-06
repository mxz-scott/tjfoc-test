package com.tjfintech.common.functionTest.ScfTest;

import com.alibaba.fastjson.JSON;
import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.Kms;
import com.tjfintech.common.Interface.Scf;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;

import static com.tjfintech.common.performanceTest.ConfigurationTest.tokenType;
import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassKMS.size;
import static com.tjfintech.common.utils.UtilsClassScf.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

import com.tjfintech.common.utils.UtilsClass;
import com.tjfintech.common.utils.UtilsClassScf;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class GoScfTest {

    TestBuilder testBuilder= TestBuilder.getInstance();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    Scf scf = testBuilder.getScf();
    public static long expireDate = System.currentTimeMillis() + 100000000;
    Kms kms = testBuilder.getKms();

//    @BeforeClass
    public static void beforeConfig() throws Exception {
        ScfBeforeCondition bf = new ScfBeforeCondition();
        bf.B001_createPlatformAccount();
        bf.B002_createCoreCompanyAccount();
        bf.B003_installContracts();
        bf.B004_createSupplyAccounts();
        Thread.sleep(5000);
    }
    /**
     * 开立-审核-签收-查询资产-获取output交易ID和index
     * @throws InterruptedException
     */
    @Test
    public void Test001_IssuingApply() throws Exception {
        int levelLimit = 5;
        String amount = "100";
        String subType = "0";
        String response = kms.genRandom(size);
        String tokenType = UtilsClassScf.gettokenType(response);
        //资产开立申请
        String response1 = scf.IssuingApply(AccountAddress, companyID1, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("success"));
        assertThat(response1, containsString("data"));
        Thread.sleep(5000);
        //开立审核
        String response2 = scf.IssuingApprove(platformKeyID, tokenType, platformPIN);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("success"));
        assertThat(response2, containsString("data"));
        Thread.sleep(5000);
        //开立签收
        String response3 = scf.IssuingConfirm(coreCompanyKeyID, tokenType, PIN, comments);
        System.out.println("response3 = " + response3);
        assertThat(response3, containsString("200"));
        assertThat(response3, containsString("success"));
        assertThat(response3, containsString("data"));

        JSONObject KLjsonObject = JSONObject.fromObject(response3);
        String KLstoreHash = KLjsonObject.getString("data");
        String KLQSstoreHash = UtilsClassScf.strToHex(KLstoreHash);
        System.out.println("KLQSstoreHash = " + KLQSstoreHash);

        commonFunc.sdkCheckTxOrSleep(KLQSstoreHash,utilsClass.sdkGetTxDetailType,SLEEPTIME);

        assertThat(response3, containsString("200"));
        assertThat(response3,containsString("success"));

        //查询tokentype资产
//        String response5 = scf.getowneraddr(tokenType);
//        assertThat(response5, containsString("200"));
//        assertThat(response5, containsString("success"));
//        assertThat(response5, containsString("\"address\":\""+ supplyAddress1));
//        assertThat(response5, containsString("\"value\":100"));
        //获取output的交易id和index
        String response6 = scf.FuncGetoutputinfo(supplyAddress1, tokenType, subType);
        assertThat(response6, containsString("200"));
        assertThat(response6, containsString("success"));
        assertThat(response6, containsString("data"));
        assertThat(response6, containsString("\"index\":0"));
    }

    /**
     * 开立-审核-拒收
     * @throws InterruptedException
     */
    @Test
    public void Test002_IssuingReject() throws Exception  {
        int levelLimit = 5;
        String amount = "100";
        String response = kms.genRandom(size);
        String tokenType = UtilsClassScf.gettokenType(response);
        //资产开立申请
        String response1 = scf.IssuingApply(AccountAddress, companyID1, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("success"));
        assertThat(response1, containsString("data"));
        Thread.sleep(5000);
        //开立审核
        String response2 = scf.IssuingApprove(platformKeyID, tokenType, platformPIN);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("success"));
        assertThat(response2, containsString("data"));
        Thread.sleep(5000);
        //开立拒收
        String response3 = scf.IssuingReject(coreCompanyKeyID, tokenType, PIN, companyID1, comments);
        assertThat(response3, containsString("200"));
        assertThat(response3, containsString("success"));
        assertThat(response3, containsString("data"));
    }

    /**
     * 开立-开立取消
     */
    @Test
    public void Test003_IssuingCancel() {
        int levelLimit = 5;
        String amount = "100";
        String response = kms.genRandom(size);
        String tokenType = UtilsClassScf.gettokenType(response);
        //资产开立
        String response1 = scf.IssuingApply(AccountAddress, companyID1, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("success"));
        assertThat(response1, containsString("data"));
        //开立取消
        String response2 = scf.IssuingCancel(tokenType, companyID1, platformKeyID, platformPIN, comments);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("success"));
        assertThat(response2, containsString("data"));
    }

    /**
    开立-审核-签收-转让-签收
     */
    @Test
    public void Test004_Assignmentapply() throws Exception {
        int levelLimit = 5;
        String amount = "100";
        String response = kms.genRandom(size);
        String newSubType = "n";
        String proof = "123456";
        String challenge = "123456";
        String tokenType = UtilsClassScf.gettokenType(response);
        //资产开立申请
        String response1 = scf.IssuingApply(AccountAddress, companyID1, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("success"));
        assertThat(response1, containsString("data"));
        Thread.sleep(5000);
        //开立审核
        String response2 = scf.IssuingApprove(platformKeyID, tokenType, platformPIN);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("success"));
        assertThat(response2, containsString("data"));
        Thread.sleep(5000);
        //开立签收
        String response3 = scf.IssuingConfirm(coreCompanyKeyID, tokenType, PIN, comments);
        assertThat(response3, containsString("200"));
        assertThat(response3, containsString("success"));
        assertThat(response3, containsString("data"));

        JSONObject KLjsonObject = JSONObject.fromObject(response3);
        String KLstoreHash = KLjsonObject.getString("data");
        String KLQSstoreHash = UtilsClassScf.strToHex(KLstoreHash);
        System.out.println("KLQSstoreHash = " + KLQSstoreHash);

        commonFunc.sdkCheckTxOrSleep(KLQSstoreHash,utilsClass.sdkGetTxDetailType,SLEEPTIME);

        assertThat(response3, containsString("200"));
        assertThat(response3,containsString("success"));
        //资产转让申请
        List<Map> list = new ArrayList<>(10);
        List<Map> list1 = UtilsClassScf.Assignment("1", "0", list);
        String response4 = scf.AssignmentApply(supplyAddress1, supplyID1, PIN, proof, tokenType, list1, newSubType, supplyAddress2, comments);
        assertThat(response4, containsString("200"));
        assertThat(response4, containsString("success"));
        assertThat(response4, containsString("data"));
        Thread.sleep(5000);
        //资产转让签收
        String response5 = scf.AssignmentConfirm(supplyID1, PIN, challenge, tokenType, comments);
        assertThat(response5, containsString("200"));
        assertThat(response5, containsString("success"));
        assertThat(response5, containsString("data"));

        JSONObject ZRjsonObject = JSONObject.fromObject(response5);
        String ZRstoreHash = ZRjsonObject.getString("data");
        String ZRQSstoreHash = UtilsClassScf.strToHex(ZRstoreHash);
        System.out.println("ZRQSstoreHash = " + ZRQSstoreHash);

        commonFunc.sdkCheckTxOrSleep(ZRQSstoreHash,utilsClass.sdkGetTxDetailType,SLEEPTIME);

        assertThat(response5, containsString("200"));
        assertThat(response5, containsString("success"));

        //查询tokentype资产
//        String response6 = scf.getowneraddr(tokenType);
//        assertThat(response6, containsString("200"));
//        assertThat(response6, containsString("success"));
//        assertThat(response6, containsString("\"address\":\""+ supplyAddress2));
//        assertThat(response6, containsString("\"value\":1"));
//        assertThat(response6, containsString("\"address\":\""+ supplyAddress1));
//        assertThat(response6, containsString("\"value\":99"));
    }

    /**
     * 开立-审核-签收-转让-拒收
     */
     @Test
    public void Test005_AssignmentReject() throws Exception  {
         int levelLimit = 5;
         String amount = "100";
         String response = kms.genRandom(size);
         String newSubType = "n";
         String proof = "123456";
         String challenge = "123456";
         String tokenType = UtilsClassScf.gettokenType(response);
         //资产开立申请
         String response1 = scf.IssuingApply(AccountAddress, companyID1, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
         assertThat(response1, containsString("200"));
         assertThat(response1, containsString("success"));
         assertThat(response1, containsString("data"));
         Thread.sleep(5000);
         //开立审核
         String response2 = scf.IssuingApprove(platformKeyID, tokenType, platformPIN);
         assertThat(response2, containsString("200"));
         assertThat(response2, containsString("success"));
         assertThat(response2, containsString("data"));
         Thread.sleep(5000);
         //开立签收
         String response3 = scf.IssuingConfirm(coreCompanyKeyID, tokenType, PIN, comments);
         assertThat(response3, containsString("200"));
         assertThat(response3, containsString("success"));
         assertThat(response3, containsString("data"));

         JSONObject KLjsonObject = JSONObject.fromObject(response3);
         String KLstoreHash = KLjsonObject.getString("data");
         String KLQSstoreHash = UtilsClassScf.strToHex(KLstoreHash);
         System.out.println("KLQSstoreHash = " + KLQSstoreHash);

         commonFunc.sdkCheckTxOrSleep(KLQSstoreHash,utilsClass.sdkGetTxDetailType,SLEEPTIME);

         assertThat(response3, containsString("200"));
         assertThat(response3,containsString("success"));

         //资产转让申请
         List<Map> list = new ArrayList<>(10);
         List<Map> list1 = UtilsClassScf.Assignment("1", "0", list);
         String response4 = scf.AssignmentApply(supplyAddress1, supplyID1, PIN, proof, tokenType, list1, newSubType, supplyAddress2, comments);
         assertThat(response4, containsString("200"));
         assertThat(response4, containsString("success"));
         assertThat(response4, containsString("data"));
         Thread.sleep(5000);
        //资产转让拒收
         String response5 = scf.AssignmentReject(challenge, tokenType, comments);
         assertThat(response5, containsString("200"));
         assertThat(response5, containsString("success"));
         assertThat(response5, containsString("data"));
     }

    /**
     * 开立-审核-签收-融资-融资试算-融资申请反馈-融资签收
     */
    @Test
    public void Test006_FinacingApply() throws Exception {
        int levelLimit = 5;
        String amount = "1";
        String subType = "0";
        String response = kms.genRandom(size);
        String proof = "123456";
        String challenge = "123456";
        String tokenType = UtilsClassScf.gettokenType(response);
        //资产开立申请
        String response1 = scf.IssuingApply(AccountAddress, companyID1, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("success"));
        assertThat(response1, containsString("data"));
        Thread.sleep(5000);
        //开立审核
        String response2 = scf.IssuingApprove(platformKeyID, tokenType, platformPIN);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("success"));
        assertThat(response2, containsString("data"));
        Thread.sleep(5000);
        //开立签收
        String response3 = scf.IssuingConfirm(coreCompanyKeyID, tokenType, PIN, comments);
        assertThat(response3, containsString("200"));
        assertThat(response3, containsString("success"));
        assertThat(response3, containsString("data"));

        JSONObject KLjsonObject = JSONObject.fromObject(response3);
        String KLstoreHash = KLjsonObject.getString("data");
        String KLQSstoreHash = UtilsClassScf.strToHex(KLstoreHash);
        System.out.println("KLQSstoreHash = " + KLQSstoreHash);

        commonFunc.sdkCheckTxOrSleep(KLQSstoreHash,utilsClass.sdkGetTxDetailType,SLEEPTIME);

        assertThat(response3, containsString("200"));
        assertThat(response3,containsString("success"));
        //融资申请
        String newFromSubType = "0";
        String newToSubType = "b";
        String rzamount = "1";
        String response4 = scf.FinacingApply(supplyAddress1, supplyID1, PIN, proof, tokenType, rzamount, subType, newFromSubType, newToSubType, supplyAddress2);
        assertThat(response4, containsString("200"));
        assertThat(response4, containsString("success"));
        assertThat(response4, containsString("data"));
        //融资试算
        String timeLimit = "10";
        String response5 = scf.FinacingTest(ZJFAddress, rzamount, timeLimit);
        assertThat(response5, containsString("200"));
        assertThat(response5, containsString("success"));
        assertThat(response5, containsString("data"));
        //融资申请反馈
        String applyNo = "7777";
        String state = "1";
        String response6 = scf.FinacingFeedback(ZJFAddress, applyNo, state, comments);
        assertThat(response6, containsString("200"));
        assertThat(response6, containsString("success"));
        assertThat(response6, containsString("data"));
        Thread.sleep(5000);
        //融资签收
        String response7 = scf.FinacingConfirm(applyNo, ZJFAddress, supplyID1, companyID1, PIN, tokenType, supplyAddress2, challenge, comments);
        assertThat(response7, containsString("200"));
        assertThat(response7, containsString("success"));
        assertThat(response7, containsString("data"));

        JSONObject RZjsonObject = JSONObject.fromObject(response7);
        String RZstoreHash = RZjsonObject.getString("data");
        String RZQSstoreHash = UtilsClassScf.strToHex(RZstoreHash);
        System.out.println("RZQSstoreHash = " + RZQSstoreHash);

        commonFunc.sdkCheckTxOrSleep(RZQSstoreHash,utilsClass.sdkGetTxDetailType,SLEEPTIME);

        assertThat(response7, containsString("200"));
        assertThat(response7,containsString("success"));
    }

    /**
     * 开立-审核-签收-融资申请-融资试算-融资申请反馈-融资取消
     */
    @Test
    public void Test007_FinacingCancel() throws Exception  {
        int levelLimit = 5;
        String amount = "1";
        String subType = "0";
        String response = kms.genRandom(size);
        String proof = "123456";
        String challenge = "123456";
        String tokenType = UtilsClassScf.gettokenType(response);
        //资产开立申请
        String response1 = scf.IssuingApply(AccountAddress, companyID1, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("success"));
        assertThat(response1, containsString("data"));
        Thread.sleep(5000);
        //开立审核
        String response2 = scf.IssuingApprove(platformKeyID, tokenType, platformPIN);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("success"));
        assertThat(response2, containsString("data"));
        Thread.sleep(5000);
        //开立签收
        String response3 = scf.IssuingConfirm(coreCompanyKeyID, tokenType, PIN, comments);
        assertThat(response3, containsString("200"));
        assertThat(response3, containsString("success"));
        assertThat(response3, containsString("data"));

        JSONObject KLjsonObject = JSONObject.fromObject(response3);
        String KLstoreHash = KLjsonObject.getString("data");
        String KLQSstoreHash = UtilsClassScf.strToHex(KLstoreHash);
        System.out.println("KLQSstoreHash = " + KLQSstoreHash);

        commonFunc.sdkCheckTxOrSleep(KLQSstoreHash,utilsClass.sdkGetTxDetailType,SLEEPTIME);

        assertThat(response3, containsString("200"));
        assertThat(response3,containsString("success"));
        //融资申请
        String newFromSubType = "a";
        String newToSubType = "b";
        String response4 = scf.FinacingApply(supplyAddress1, supplyID1, PIN, proof, tokenType, amount, subType, newFromSubType, newToSubType, supplyAddress2);
        assertThat(response4, containsString("200"));
        assertThat(response4, containsString("success"));
        assertThat(response4, containsString("data"));
        //融资试算
        String timeLimit = "10";
        String response5 = scf.FinacingTest(ZJFAddress, amount, timeLimit);
        assertThat(response5, containsString("200"));
        assertThat(response5, containsString("success"));
        assertThat(response5, containsString("data"));
        //融资申请反馈
        String applyNo = "7777";
        String state = "1";
        String response6 = scf.FinacingFeedback(ZJFAddress, applyNo, state, comments);
        assertThat(response6, containsString("200"));
        assertThat(response6, containsString("success"));
        assertThat(response6, containsString("data"));
        //融资取消
        String response7 = scf.FinacingCancel(challenge, tokenType, comments);
        assertThat(response7, containsString("200"));
        assertThat(response7, containsString("success"));
        assertThat(response7, containsString("data"));
    }

    /**
     * 开立-审核-签收-兑付申请-兑付试算-兑付反馈-兑付确认
     * 兑付需要给清分机构和资金方合约里的方法everyone调用权限。
     */
    @Test
     public void Test008_PayingConfirm() throws Exception {
        int levelLimit = 5;
        String amount = "100.0";
        String response = kms.genRandom(size);
        String tokenType = UtilsClassScf.gettokenType(response);
        //资产开立申请
        String response1 = scf.IssuingApply(AccountAddress, companyID1, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("success"));
        assertThat(response1, containsString("data"));
        Thread.sleep(5000);
        //开立审核
        String response2 = scf.IssuingApprove(platformKeyID, tokenType, platformPIN);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("success"));
        assertThat(response2, containsString("data"));
        Thread.sleep(5000);
        //开立签收
        String response3 = scf.IssuingConfirm(coreCompanyKeyID, tokenType, PIN, comments);
        assertThat(response3, containsString("200"));
        assertThat(response3, containsString("success"));
        assertThat(response3, containsString("data"));

        JSONObject KLjsonObject = JSONObject.fromObject(response3);
        String KLstoreHash = KLjsonObject.getString("data");
        String KLQSstoreHash = UtilsClassScf.strToHex(KLstoreHash);
        System.out.println("KLQSstoreHash = " + KLQSstoreHash);

        commonFunc.sdkCheckTxOrSleep(KLQSstoreHash,utilsClass.sdkGetTxDetailType,SLEEPTIME);

        assertThat(response3, containsString("200"));
        assertThat(response3,containsString("success"));

        //兑付申请
        String response4 = scf.PayingApply(tokenType, companyID1);
        assertThat(response4, containsString("200"));
        assertThat(response4, containsString("success"));
        assertThat(response4, containsString("data"));
        Thread.sleep(5000);
        //兑付通知
        String message = "资产兑付申请通知";
        String response5 = scf.PayingNotify(AccountAddress, message);
        assertThat(response5, containsString("200"));
        assertThat(response5, containsString("success"));
        assertThat(response5, containsString("data"));
        Thread.sleep(5000);
        //兑付反馈
        String state = "1";
        String response6 = scf.PayingFeedback(QFJGAddress, tokenType, state, comments);
        assertThat(response6, containsString("200"));
        assertThat(response6, containsString("success"));
        assertThat(response6, containsString("data"));
        Thread.sleep(5000);
        //兑付确认
        List<Map> list = new ArrayList<>(10);
        List<Map> list1 = UtilsClassScf.paying(supplyAddress1, supplyID1, "0", "100", list);
        String response9 = scf.PayingConfirm(QFJGAddress, companyID1, list1, platformKeyID, platformPIN, tokenType, comments);
        assertThat(response9, containsString("200"));
        assertThat(response9, containsString("success"));
        assertThat(response9, containsString("data"));

        JSONObject DFjsonObject = JSONObject.fromObject(response9);
        String DFstoreHash = DFjsonObject.getString("data");
        String DFQRstoreHash = UtilsClassScf.strToHex(DFstoreHash);
        System.out.println("DFQRstoreHash = " + DFQRstoreHash);

        commonFunc.sdkCheckTxOrSleep(DFQRstoreHash,utilsClass.sdkGetTxDetailType,SLEEPTIME);

        assertThat(response9, containsString("200"));
        assertThat(response9,containsString("success"));

//        String response10 = scf.getowneraddr(tokenType);
//        assertThat(response10, containsString("200"));
//        assertThat(response10, containsString("success"));
//        assertThat(response10, containsString("\"address\":\"0000000000000000\""));
//        assertThat(response10, containsString("\"value\":100"));
     }

    /**
     * 开立-审核-签收-转让-签收-兑付申请-兑付通知-兑付反馈-兑付确认
     */
    @Test
    public void Test009_PayingConfirm1() throws Exception {
        int levelLimit = 5;
        String amount = "100";
        String response = kms.genRandom(size);
        String newSubType = "n";
        String proof = "123456";
        String challenge = "123456";
        String tokenType = UtilsClassScf.gettokenType(response);
        //资产开立申请
        String response1 = scf.IssuingApply(AccountAddress, companyID1, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("success"));
        assertThat(response1, containsString("data"));
        Thread.sleep(5000);
        //开立审核
        String response2 = scf.IssuingApprove(platformKeyID, tokenType, platformPIN);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("success"));
        assertThat(response2, containsString("data"));
        Thread.sleep(5000);
        //开立签收
        String response3 = scf.IssuingConfirm(coreCompanyKeyID, tokenType, PIN, comments);
        assertThat(response3, containsString("200"));
        assertThat(response3, containsString("success"));
        assertThat(response3, containsString("data"));

        JSONObject KLjsonObject = JSONObject.fromObject(response3);
        String KLstoreHash = KLjsonObject.getString("data");
        String KLQSstoreHash = UtilsClassScf.strToHex(KLstoreHash);
        System.out.println("KLQSstoreHash = " + KLQSstoreHash);

        commonFunc.sdkCheckTxOrSleep(KLQSstoreHash,utilsClass.sdkGetTxDetailType,SLEEPTIME);
        assertThat(response3, containsString("200"));
        assertThat(response3,containsString("success"));

        //资产转让申请
        List<Map> list = new ArrayList<>(10);
        List<Map> list1 = UtilsClassScf.Assignment("1", "0", list);
        String response4 = scf.AssignmentApply(supplyAddress1, supplyID1, PIN, proof, tokenType, list1, newSubType, supplyAddress2, comments);
        assertThat(response4, containsString("200"));
        assertThat(response4, containsString("success"));
        assertThat(response4, containsString("data"));
        Thread.sleep(5000);
        //资产转让签收
        String response5 = scf.AssignmentConfirm(supplyID1, PIN, challenge, tokenType, comments);
        assertThat(response5, containsString("200"));
        assertThat(response5, containsString("success"));
        assertThat(response5, containsString("data"));

        JSONObject ZRjsonObject = JSONObject.fromObject(response5);
        String ZRstoreHash = ZRjsonObject.getString("data");
        String ZRQSstoreHash = UtilsClassScf.strToHex(ZRstoreHash);
        System.out.println("ZRQSstoreHash = " + ZRQSstoreHash);

        commonFunc.sdkCheckTxOrSleep(ZRQSstoreHash,utilsClass.sdkGetTxDetailType,SLEEPTIME);
        assertThat(response5, containsString("200"));
        assertThat(response5,containsString("success"));
        //资产兑付申请
        String response6 = scf.PayingApply(tokenType, companyID1);
        assertThat(response6, containsString("200"));
        assertThat(response6, containsString("success"));
        assertThat(response6, containsString("data"));
        Thread.sleep(5000);
        //兑付通知
        String message = "资产兑付申请通知";
        String response7 = scf.PayingNotify(AccountAddress, message);
        assertThat(response7, containsString("200"));
        assertThat(response7, containsString("success"));
        assertThat(response7, containsString("data"));
        Thread.sleep(5000);
        //兑付反馈
        String state = "1";
        String response8 = scf.PayingFeedback(QFJGAddress, tokenType, state, comments);
        assertThat(response8, containsString("200"));
        assertThat(response8, containsString("success"));
        assertThat(response8, containsString("data"));
        Thread.sleep(5000);
        //兑付确认
        List<Map> list2 = UtilsClassScf.paying(supplyAddress1, supplyID1, "0", "99", list);
        List<Map> list3 = UtilsClassScf.paying(supplyAddress2, supplyID2, "n", "1", list2);
        String response9 = scf.PayingConfirm(QFJGAddress, companyID1, list3, platformKeyID, platformPIN, tokenType, comments);
        assertThat(response9, containsString("200"));
        assertThat(response9, containsString("success"));
        assertThat(response9, containsString("data"));

        JSONObject DFjsonObject = JSONObject.fromObject(response9);
        String DFstoreHash = DFjsonObject.getString("data");
        String DFQRstoreHash = UtilsClassScf.strToHex(DFstoreHash);
        System.out.println("DFQRstoreHash = " + DFQRstoreHash);

        commonFunc.sdkCheckTxOrSleep(DFQRstoreHash,utilsClass.sdkGetTxDetailType,SLEEPTIME);

        assertThat(response9, containsString("200"));
        assertThat(response9,containsString("success"));

//        String response10 = scf.getowneraddr(tokenType);
//        assertThat(response10, containsString("200"));
//        assertThat(response10, containsString("success"));
//        assertThat(response10, containsString("\"address\":\"0000000000000000\""));
//        assertThat(response10, containsString("\"value\":100"));
    }

    /**
     * 开立-审核-签收-融资-融资试算-融资申请反馈-融资签收-兑付申请-兑付通知-兑付反馈-兑付确认
     */
    @Test
    public void Test010_PayingConfirm2() throws Exception {
        int levelLimit = 5;
        String amount = "100";
        String subType = "0";
        String response = kms.genRandom(size);
        String proof = "123456";
        String challenge = "123456";
        String state = "1";
        String tokenType = UtilsClassScf.gettokenType(response);
        //资产开立申请,金额为1
        String response1 = scf.IssuingApply(AccountAddress, companyID1, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("success"));
        assertThat(response1, containsString("data"));
        Thread.sleep(5000);
        //开立审核
        String response2 = scf.IssuingApprove(platformKeyID, tokenType, platformPIN);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("success"));
        assertThat(response2, containsString("data"));
        Thread.sleep(5000);
        //开立签收
        String response3 = scf.IssuingConfirm(coreCompanyKeyID, tokenType, PIN, comments);
        assertThat(response3, containsString("200"));
        assertThat(response3, containsString("success"));
        assertThat(response3, containsString("data"));

        JSONObject KLjsonObject = JSONObject.fromObject(response3);
        String KLstoreHash = KLjsonObject.getString("data");
        String KLQSstoreHash = UtilsClassScf.strToHex(KLstoreHash);
        System.out.println("KLQSstoreHash = " + KLQSstoreHash);

        commonFunc.sdkCheckTxOrSleep(KLQSstoreHash,utilsClass.sdkGetTxDetailType,SLEEPTIME);

        assertThat(response3, containsString("200"));
        assertThat(response3,containsString("success"));
        //融资申请，开立给供应商1的金额，全部融资给供应商2
        String newFromSubType = "0";
        String newToSubType = "b";
        String rzamount = "10";
        String response4 = scf.FinacingApply(supplyAddress1, supplyID1, PIN, proof, tokenType, rzamount, subType, newFromSubType, newToSubType, supplyAddress2);
        assertThat(response4, containsString("200"));
        assertThat(response4, containsString("success"));
        assertThat(response4, containsString("data"));
        //融资试算
        String timeLimit = "10";
        String response5 = scf.FinacingTest(ZJFAddress, rzamount, timeLimit);
        assertThat(response5, containsString("200"));
        assertThat(response5, containsString("success"));
        assertThat(response5, containsString("data"));
        //融资申请反馈
        String applyNo = "7777";
        String response6 = scf.FinacingFeedback(ZJFAddress, applyNo, state, comments);
        assertThat(response6, containsString("200"));
        assertThat(response6, containsString("success"));
        assertThat(response6, containsString("data"));
        Thread.sleep(5000);
        //融资签收
        String response7 = scf.FinacingConfirm(applyNo, ZJFAddress, supplyID1, companyID1, PIN, tokenType, supplyAddress2, challenge, comments);
        assertThat(response7, containsString("200"));
        assertThat(response7, containsString("success"));
        assertThat(response7, containsString("data"));

        JSONObject RZjsonObject = JSONObject.fromObject(response7);
        String RZstoreHash = RZjsonObject.getString("data");
        String RZQSstoreHash = UtilsClassScf.strToHex(RZstoreHash);
        System.out.println("RZQSstoreHash = " + RZQSstoreHash);

        commonFunc.sdkCheckTxOrSleep(RZQSstoreHash,utilsClass.sdkGetTxDetailType,SLEEPTIME);

        assertThat(response7, containsString("200"));
        assertThat(response7,containsString("success"));

        //资产兑付申请
        String response8 = scf.PayingApply(tokenType, companyID1);
        assertThat(response8, containsString("200"));
        assertThat(response8, containsString("success"));
        assertThat(response8, containsString("data"));
        Thread.sleep(5000);
        //兑付通知
        String message = "资产兑付申请通知";
        String response9 = scf.PayingNotify(AccountAddress, message);
        assertThat(response9, containsString("200"));
        assertThat(response9, containsString("success"));
        assertThat(response9, containsString("data"));
        Thread.sleep(5000);
        //兑付反馈
        String response10 = scf.PayingFeedback(QFJGAddress, tokenType, state, comments);
        assertThat(response10, containsString("200"));
        assertThat(response10, containsString("success"));
        assertThat(response10, containsString("data"));
        Thread.sleep(5000);
        //兑付确认
        List<Map> list = new ArrayList<>(10);
        List<Map> list2 = UtilsClassScf.paying(supplyAddress2, supplyID2, "b", "10", list);
        String response11 = scf.PayingConfirm(QFJGAddress, companyID1, list2, platformKeyID, platformPIN, tokenType, comments);
        assertThat(response11, containsString("200"));
        assertThat(response11, containsString("success"));
        assertThat(response11, containsString("data"));

        JSONObject DFjsonObject = JSONObject.fromObject(response11);
        String DFstoreHash = DFjsonObject.getString("data");
        String DFQRstoreHash = UtilsClassScf.strToHex(DFstoreHash);
        System.out.println("DFQRstoreHash = " + DFQRstoreHash);

        commonFunc.sdkCheckTxOrSleep(DFQRstoreHash,utilsClass.sdkGetTxDetailType,SLEEPTIME);

        assertThat(response11, containsString("200"));
        assertThat(response11,containsString("success"));

//        String response12 = scf.getowneraddr(tokenType);
//        assertThat(response12, containsString("200"));
//        assertThat(response12, containsString("success"));
//        assertThat(response12, containsString("\"address\":\"0000000000000000\""));
//        assertThat(response12, containsString("\"value\":1"));
    }

    /**
     * 开立-审核-签收-转让-签收-融资-融资试算-融资申请反馈-融资签收-兑付申请-兑付通知-兑付反馈-兑付确认
     */
     @Test
     public void Test011_PayingConfirm3() throws Exception {
         int levelLimit = 5;
         String amount = "100";
         String subType = "0";
         String response = kms.genRandom(size);
         String newSubType = "0";
         String proof = "123456";
         String challenge = "123456";
         String rzproof = "9527";
         String rzchallenge = "9527";
         String state = "1";
         String tokenType = UtilsClassScf.gettokenType(response);
         //资产开立申请
         String response1 = scf.IssuingApply(AccountAddress, companyID1, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
         assertThat(response1, containsString("200"));
         assertThat(response1, containsString("success"));
         assertThat(response1, containsString("data"));
         Thread.sleep(5000);
         //开立审核
         String response2 = scf.IssuingApprove(platformKeyID, tokenType, platformPIN);
         assertThat(response2, containsString("200"));
         assertThat(response2, containsString("success"));
         assertThat(response2, containsString("data"));
         Thread.sleep(5000);
         //开立签收
         String response3 = scf.IssuingConfirm(coreCompanyKeyID, tokenType, PIN, comments);
         assertThat(response3, containsString("200"));
         assertThat(response3, containsString("success"));
         assertThat(response3, containsString("data"));

         JSONObject KLjsonObject = JSONObject.fromObject(response3);
         String KLstoreHash = KLjsonObject.getString("data");
         String KLQSstoreHash = UtilsClassScf.strToHex(KLstoreHash);
         System.out.println("KLQSstoreHash = " + KLQSstoreHash);

         commonFunc.sdkCheckTxOrSleep(KLQSstoreHash,utilsClass.sdkGetTxDetailType,SLEEPTIME);
         assertThat(response3, containsString("200"));
         assertThat(response3,containsString("success"));

         //资产转让申请
         List<Map> list = new ArrayList<>(10);
         List<Map> list1 = UtilsClassScf.Assignment("99", "0", list);
         String response4 = scf.AssignmentApply(supplyAddress1, supplyID1, PIN, proof, tokenType, list1, newSubType, supplyAddress2, comments);
         assertThat(response4, containsString("200"));
         assertThat(response4, containsString("success"));
         assertThat(response4, containsString("data"));
         Thread.sleep(5000);
         //资产转让签收
         String response5 = scf.AssignmentConfirm(supplyID1, PIN, challenge, tokenType, comments);
         assertThat(response5, containsString("200"));
         assertThat(response5, containsString("success"));
         assertThat(response5, containsString("data"));
         JSONObject ZRjsonObject = JSONObject.fromObject(response3);
         String ZRstoreHash = ZRjsonObject.getString("data");
         String ZRQSstoreHash = UtilsClassScf.strToHex(ZRstoreHash);
         System.out.println("ZRQSstoreHash = " + ZRQSstoreHash);

         commonFunc.sdkCheckTxOrSleep(ZRQSstoreHash,utilsClass.sdkGetTxDetailType,SLEEPTIME);
         assertThat(response5, containsString("200"));
         assertThat(response5,containsString("success"));
         //融资申请
         String newFromSubType = "0";
         String newToSubType = "b";
         String rzamount = "10";
         String response6 = scf.FinacingApply(supplyAddress2, supplyID2, PIN, rzproof, tokenType, rzamount, subType, newFromSubType, newToSubType, supplyAddress3);
         assertThat(response6, containsString("200"));
         assertThat(response6, containsString("success"));
         assertThat(response6, containsString("data"));
         //融资试算
         String timeLimit = "10";
         String response7 = scf.FinacingTest(ZJFAddress, rzamount, timeLimit);
         assertThat(response7, containsString("200"));
         assertThat(response7, containsString("success"));
         assertThat(response7, containsString("data"));
         //融资申请反馈
         String applyNo = "7777";
         String response8 = scf.FinacingFeedback(ZJFAddress, applyNo, state, comments);
         assertThat(response8, containsString("200"));
         assertThat(response8, containsString("success"));
         assertThat(response8, containsString("data"));
         Thread.sleep(5000);
         //融资签收
         String response9 = scf.FinacingConfirm(applyNo, ZJFAddress, supplyID2, companyID1, PIN, tokenType, supplyAddress3, rzchallenge, comments);
         assertThat(response9, containsString("200"));
         assertThat(response9, containsString("success"));
         assertThat(response9, containsString("data"));

         JSONObject RZjsonObject = JSONObject.fromObject(response9);
         String RZstoreHash = RZjsonObject.getString("data");
         String RZQSstoreHash = UtilsClassScf.strToHex(RZstoreHash);
         System.out.println("RZQSstoreHash = " + RZQSstoreHash);

         commonFunc.sdkCheckTxOrSleep(RZQSstoreHash,utilsClass.sdkGetTxDetailType,SLEEPTIME);

         assertThat(response9, containsString("200"));
         assertThat(response9,containsString("success"));

         //资产兑付申请
         String response10 = scf.PayingApply(tokenType, companyID1);
         assertThat(response10, containsString("200"));
         assertThat(response10, containsString("success"));
         assertThat(response10, containsString("data"));
         //兑付通知
         String message = "资产兑付申请通知";
         String response11 = scf.PayingNotify(AccountAddress, message);
         assertThat(response11, containsString("200"));
         assertThat(response11, containsString("success"));
         assertThat(response11, containsString("data"));
         //兑付反馈
         String response12 = scf.PayingFeedback(QFJGAddress, tokenType, state, comments);
         assertThat(response12, containsString("200"));
         assertThat(response12, containsString("success"));
         assertThat(response12, containsString("data"));
         Thread.sleep(5000);
         //兑付确认
         List<Map> list2 = UtilsClassScf.paying(supplyAddress1, supplyID1, "0", "1", list);
         List<Map> list3 = UtilsClassScf.paying(supplyAddress2, supplyID2, "0", "89", list2);
         List<Map> list4 = UtilsClassScf.paying(supplyAddress3, supplyID3, "b", "10", list3);
         String response13 = scf.PayingConfirm(QFJGAddress, companyID1, list4, platformKeyID, platformPIN, tokenType, comments);
         assertThat(response13, containsString("200"));
         assertThat(response13, containsString("success"));
         assertThat(response13, containsString("data"));
         JSONObject DFjsonObject = JSONObject.fromObject(response13);
         String DFstoreHash = DFjsonObject.getString("data");
         String DFQRstoreHash = UtilsClassScf.strToHex(DFstoreHash);
         System.out.println("DFQRstoreHash = " + DFQRstoreHash);

         commonFunc.sdkCheckTxOrSleep(DFQRstoreHash,utilsClass.sdkGetTxDetailType,SLEEPTIME);

         assertThat(response13, containsString("200"));
         assertThat(response13,containsString("success"));

//         String response14 = scf.getowneraddr(tokenType);
//         assertThat(response14, containsString("200"));
//         assertThat(response14, containsString("success"));
//         assertThat(response14, containsString("\"address\":\"0000000000000000\""));
//         assertThat(response14, containsString("\"value\":100"));
     }
    /**
     授信额度调整==companyID2
     */
    @Test
    public void Test012_CreditAdjust() {
        String amount = "30000000.0";
        String response1 = scf.CreditAdjust(AccountAddress, companyID2, amount);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("success"));
        assertThat(response1, containsString("data"));
    }
    /**
     获取output的交易id和index
     */
    @Test//输入错误随机的未开立资产的tokentype
    public void Test013_FuncGetoutputinfo() {
        String subType = "0";
        String response = kms.genRandom(size);
        String tokenType = UtilsClassScf.gettokenType(response);
        String response1 = scf.FuncGetoutputinfo(supplyAddress1, tokenType, subType);
        assertThat(response1, containsString("400"));
        assertThat(response1, containsString("error"));
        assertThat(response1, containsString(""));
    }
}
