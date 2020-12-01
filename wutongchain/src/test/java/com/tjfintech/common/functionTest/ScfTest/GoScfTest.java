package com.tjfintech.common.functionTest.ScfTest;

import com.alibaba.fastjson.JSON;
import com.tjfintech.common.Interface.Kms;
import com.tjfintech.common.Interface.Scf;
import com.tjfintech.common.TestBuilder;

import static com.tjfintech.common.performanceTest.ConfigurationTest.tokenType;
import static com.tjfintech.common.utils.UtilsClassKMS.size;
import static com.tjfintech.common.utils.UtilsClassScf.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

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

    /**
     * 开立-审核-签收-查询资产
     * @throws InterruptedException
     */
    @Test
    public void IssuingApply_Test01() throws InterruptedException {
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
    public void IssuingReject_Test02() throws InterruptedException {
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
    public void Assignmentapply_Test04() throws InterruptedException{
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
        List<Map> list1 = UtilsClassScf.Assignment("10", "0", list);
        String response4 = scf.AssignmentApply(supplyAddress1, supplyID1, PIN, proof, tokenType, list1, newSubType, supplyAddress2, comments);
        assertThat(response4, containsString("200"));
        assertThat(response4, containsString("success"));
        assertThat(response4, containsString("data"));
        //资产转让签收
        String response5 = scf.AssignmentConfirm(supplyID2, PIN, challenge, tokenType, comments);
        assertThat(response5, containsString("200"));
        assertThat(response5, containsString("success"));
        assertThat(response5, containsString("data"));
    }

    /**
     * 开立-审核-签收-转让-拒收
     */
     @Test
    public void AssignmentReject_Test05 () throws InterruptedException {
         int levelLimit = 5;
         String amount = "100.0";
         String response = kms.genRandom(size);
         String newSubType = "n";
         String proof = "123456";
         String challenge = "123456";
         String tokenType = UtilsClassScf.gettokenType(response);
         //资产开立申请
         String response1 = scf.IssuingApply(AccountAddress, companyID2, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
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
         List<Map> list1 = UtilsClassScf.Assignment("10", "0", list);
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
}
