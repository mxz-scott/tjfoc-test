package com.tjfintech.common.functionTest.ScfTest;

import com.alibaba.fastjson.JSON;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.Kms;
import com.tjfintech.common.Interface.Scf;
import com.tjfintech.common.TestBuilder;

import static com.tjfintech.common.performanceTest.ConfigurationTest.tokenType;
import static com.tjfintech.common.utils.UtilsClass.SLEEPTIME;
import static com.tjfintech.common.utils.UtilsClass.peerList;
import static com.tjfintech.common.utils.UtilsClass.storeHash;
import static com.tjfintech.common.utils.UtilsClassKMS.size;
import static com.tjfintech.common.utils.UtilsClassScf.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

import com.tjfintech.common.utils.UtilsClass;
import com.tjfintech.common.utils.UtilsClassScf;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class GoScfTest {
    TestBuilder testBuilder = TestBuilder.getInstance();
    Scf scf = testBuilder.getScf();
    public static long expireDate = System.currentTimeMillis() + 100000000;
    Kms kms = testBuilder.getKms();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();

    /**
     * 开立-审核-签收-查询资产
     * @throws InterruptedException
     */
    @Test
    public void IssuingApply_Test01() throws Exception {
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
//        commonFunc.sdkCheckTxOrSleep(storeHash,utilsClass.sdkGetTxDetailType,SLEEPTIME);
        //开立审核
        String response2 = scf.IssuingApprove(platformKeyID, tokenType, platformPIN);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("success"));
        assertThat(response2, containsString("data"));
        Thread.sleep(5000);
//        commonFunc.sdkCheckTxOrSleep(storeHash,utilsClass.sdkGetTxDetailType,SLEEPTIME);
        //开立签收
        String response3 = scf.IssuingConfirm(coreCompanyKeyID, tokenType, PIN, comments);
        assertThat(response3, containsString("200"));
        assertThat(response3, containsString("success"));
        assertThat(response3, containsString("data"));
        Thread.sleep(5000);
//        commonFunc.sdkCheckTxOrSleep(storeHash,utilsClass.sdkGetTxDetailType,SLEEPTIME);
        //查询tokentype资产
        String response4 = scf.getowneraddr(tokenType);
        assertThat(response4, containsString("200"));
        assertThat(response4, containsString("success"));
        assertThat(response4, containsString("address"));
        assertThat(response4, containsString("value"));
    }

    /**
     * 开立-审核-拒收
     * @throws InterruptedException
     */
    @Test
    public void IssuingReject_Test02() throws Exception  {
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
    public void IssuingCancel_Test03() {
        int levelLimit = 5;
        String amount = "100.0";
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
    public void Assignmentapply_Test04() throws Exception {
        int levelLimit = 5;
        String amount = "100.0";
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
        Thread.sleep(5000);
        //查询tokentype资产
        String response6 = scf.getowneraddr(tokenType);
        assertThat(response6, containsString("200"));
        assertThat(response6, containsString("success"));
        assertThat(response6, containsString("address"));
        assertThat(response6, containsString("value"));
    }

    /**
     * 开立-审核-签收-转让-拒收
     */
     @Test
    public void AssignmentReject_Test05 () throws Exception  {
         int levelLimit = 5;
         String amount = "100.0";
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
         Thread.sleep(5000);
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
    public void FinacingApply_Test06 () throws Exception {
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
        //融资签收
        String response7 = scf.FinacingConfirm(applyNo, ZJFAddress, supplyID1, companyID1, PIN, tokenType, supplyAddress2, challenge, comments);
        assertThat(response7, containsString("200"));
        assertThat(response7, containsString("success"));
        assertThat(response7, containsString("data"));
    }

    /**
     * 开立-审核-签收-融资申请-融资试算-融资申请反馈-融资取消
     */
    @Test
    public void FinacingCancel_Test07 () throws Exception  {
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
     */
    @Test
     public void PayingConfirm_Test08() throws Exception {
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
        Thread.sleep(5000);
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
        List<Map> list1 = UtilsClassScf.paying("SnMn7eXperY2Vp6MMexUW5sdVC1PKEQo7grXP2SBypee8irugZg", "buushlte655bj3jflsd0", "0", list);
        String response9 = scf.PayingConfirm(QFJGAddress, companyID1, list1, platformKeyID, platformPIN, tokenType, comments);
        assertThat(response9, containsString("200"));
        assertThat(response9, containsString("success"));
        assertThat(response9, containsString("data"));

     }

    /**
     * 开立-审核-签收-转让-签收-兑付申请-兑付通知-兑付反馈-兑付确认
     */
    @Test
    public void PayingConfirm_Test09() throws Exception {
        int levelLimit = 5;
        String amount = "100.0";
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
        Thread.sleep(5000);
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
        Thread.sleep(5000);
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
        List<Map> list2 = UtilsClassScf.paying("SnsEEcAsq9364NW1y72XNKfXE3tJvTacyxX838Cwr8VRdiscmky", "buushlte655bj3jflqv0", "0", list);
        List<Map> list3 = UtilsClassScf.paying("SnMn7eXperY2Vp6MMexUW5sdVC1PKEQo7grXP2SBypee8irugZg", "buushlte655bj3jflsd0", "0", list2);
        List<Map> list4 = UtilsClassScf.paying("Sn5ANYdXD8ZK1ioghfoZ2LfFa82QTXvDWGiZiaxCxMFz6ZjxMPi", "buushlte655bj3jflslg", "n", list3);
        String response9 = scf.PayingConfirm(QFJGAddress, companyID1, list4, platformKeyID, platformPIN, tokenType, comments);
        assertThat(response9, containsString("200"));
        assertThat(response9, containsString("success"));
        assertThat(response9, containsString("data"));
    }
    /**
     * 开立-审核-签收-融资-融资试算-融资申请反馈-融资签收-兑付申请-兑付通知-兑付反馈-兑付确认
     */

}
