package com.tjfintech.common.functionTest.ScfTest;

import com.jcraft.jsch.jce.Random;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.Kms;
import com.tjfintech.common.Interface.Scf;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import com.tjfintech.common.utils.UtilsClassScf;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.SLEEPTIME;
import static com.tjfintech.common.utils.UtilsClassKMS.size;
import static com.tjfintech.common.utils.UtilsClassScf.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;



public class ScfMultiInvalidTest {

    TestBuilder testBuilder = TestBuilder.getInstance();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    Scf scf = testBuilder.getScf();
    Store store = testBuilder.getStore();
    public static double timeStampNow = System.currentTimeMillis();
    public static BigDecimal expireDate = new BigDecimal(timeStampNow + 12356789);
    Kms kms = testBuilder.getKms();


    @BeforeClass
    public static void beforeConfig() throws Exception {
        ScfBeforeCondition bf = new ScfBeforeCondition();
        bf.B001_createPlatformAccount();
        bf.Getcomments();
        bf.B002_createCoreCompanyAccount();
        bf.B003_installContracts();

        bf.B004_createSupplyAccounts();

        Thread.sleep(5000);
    }

    /**
     * tokentytpe 增发///为了接超时逻辑处理，增发资产链上报错，接口返回值返回200，唯一情况不同步。
     */
    @Test
    public void Test001_IssuingApply() throws Exception {
        int levelLimit = 5;
        String tokentype = "T"+UtilsClass.Random(4);
        String UID = "a"+UtilsClass.Random(4);
        String UID1 = "b"+UtilsClass.Random(4);
        String UID2 = "b"+UtilsClass.Random(4);

        //资产开立申请
        String response1 = scf.IssuingApply( UID, AccountAddress, companyID2, coreCompanyKeyID, PIN, tokentype, levelLimit, expireDate, supplyAddress1, "10");
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("success"));
        assertThat(response1, containsString("data"));
        Thread.sleep(5000);
        //开立审核
        String response2 = scf.IssuingApprove(UID, platformKeyID, tokentype, platformPIN);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("success"));
        assertThat(response2, containsString("data"));
        Thread.sleep(5000);

        String response3 = scf.IssuingConfirm(UID1, PlatformAddress, coreCompanyKeyID, tokentype, PIN, comments);
        System.out.println("response3 = " + response3);
        assertThat(response3, containsString("200"));
        assertThat(response3, containsString("success"));
        assertThat(response3, containsString("data"));

        JSONObject KLjsonObject = JSONObject.fromObject(response3);
        String KLstoreHash = KLjsonObject.getString("data");
        String KLQSstoreHash = UtilsClassScf.strToHex(KLstoreHash);
        System.out.println("KLQSstoreHash = " + KLQSstoreHash);

        commonFunc.sdkCheckTxOrSleep(KLQSstoreHash, utilsClass.sdkGetTxDetailType, SLEEPTIME);
        //验证上链返回值
        String checking = store.GetTxDetail(KLQSstoreHash);
        assertThat(checking, containsString("200"));
        assertThat(checking, containsString("success"));
        //资产开立申请
        String response4 = scf.IssuingApply(UID2, AccountAddress, companyID2, coreCompanyKeyID, PIN, tokentype, levelLimit, expireDate, supplyAddress1, "10");
        assertThat(response4, containsString("200"));
        //assertThat(response4, containsString("400"));
//        assertThat(response4, containsString("error"));
//        assertThat(response4, containsString("Failed! Err:这个数字凭证不可增发"));

    }

    /**
     * 过期资产转让/是否精确到毫秒/
     */
    @Test
    public void Test002_expiredtransfer() throws Exception {
        int levelLimit = 5;
        String response = kms.genRandom(size);
        String tokenType = UtilsClassScf.gettokenType(response);
        BigDecimal expireDate = new BigDecimal(timeStampNow + 60000);
        String UID = "a"+UtilsClass.Random(4);
        String UID1 = "b"+UtilsClass.Random(4);
        String UID2 = "c"+UtilsClass.Random(4);
        //资产开立申请
        //资产开立申请
        String response1 = scf.IssuingApply(UID, AccountAddress, companyID1, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, "10");
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("success"));
        assertThat(response1, containsString("data"));
        Thread.sleep(5000);
        //开立审核
        String response2 = scf.IssuingApprove(UID, platformKeyID, tokenType, platformPIN);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("success"));
        assertThat(response2, containsString("data"));
        Thread.sleep(5000);
        //开立签收
        String response3 = scf.IssuingConfirm(UID1,PlatformAddress, coreCompanyKeyID, tokenType, PIN, comments);
        assertThat(response3, containsString("200"));
        assertThat(response3, containsString("success"));
        assertThat(response3, containsString("data"));

        JSONObject KLjsonObject = JSONObject.fromObject(response3);
        String KLstoreHash = KLjsonObject.getString("data");
        String KLQSstoreHash = UtilsClassScf.strToHex(KLstoreHash);
        System.out.println("KLQSstoreHash = " + KLQSstoreHash);

        commonFunc.sdkCheckTxOrSleep(KLQSstoreHash, utilsClass.sdkGetTxDetailType, SLEEPTIME);
        //验证上链返回值
        String checking = store.GetTxDetail(KLQSstoreHash);
        assertThat(checking, containsString("200"));
        assertThat(checking, containsString("success"));

        Thread.sleep(180000);//3分钟过期应该转让不成功
        //资产转让申请
        List<Map> list = new ArrayList<>(10);
        List<Map> list1 = UtilsClassScf.Assignment("1", "0", list);
        String response4 = scf.AssignmentApply(supplyAddress1, supplyID1, PIN, "123456", tokenType, list1, "n", supplyAddress2);
        assertThat(response4, containsString("200"));
//        assertThat(response4, containsString("success"));
//        assertThat(response4, containsString("Failed! Err:资产不在有效期内"));
        Thread.sleep(5000);
        //资产转让签收
        String response5 = scf.AssignmentConfirm(UID2,PlatformAddress, supplyID1, PIN, "123456", tokenType, comments);
        assertThat(response5, containsString("400"));
        assertThat(response5, containsString("error"));
        assertThat(response5, containsString("Failed"));

    }

    /**
     * 资产流转层级限制levelLimit
     */
    @Test
    public void TC003_MAXleveltransfer() throws Exception {
        int levelLimit = 2;
        String response = kms.genRandom(size);
        String tokenType = UtilsClassScf.gettokenType(response);
        BigDecimal expireDate = new BigDecimal(timeStampNow + 6000000);
        String UID = "a"+UtilsClass.Random(4);
        String UID1 = "b"+UtilsClass.Random(4);
        String UID2 = ""+UtilsClass.Random(4);
        //资产开立申请
        String response1 = scf.IssuingApply(UID, AccountAddress, companyID1, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, "10");
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("success"));
        assertThat(response1, containsString("data"));
        Thread.sleep(5000);
        //开立审核
        String response2 = scf.IssuingApprove(UID, platformKeyID, tokenType, platformPIN);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("success"));
        assertThat(response2, containsString("data"));
        Thread.sleep(5000);
        //开立签收
        String response3 = scf.IssuingConfirm(UID1, PlatformAddress, coreCompanyKeyID, tokenType, PIN, comments);
        assertThat(response3, containsString("200"));
        assertThat(response3, containsString("success"));
        assertThat(response3, containsString("data"));

        JSONObject KLjsonObject = JSONObject.fromObject(response3);
        String KLstoreHash = KLjsonObject.getString("data");
        String KLQSstoreHash = UtilsClassScf.strToHex(KLstoreHash);
        System.out.println("KLQSstoreHash = " + KLQSstoreHash);

        commonFunc.sdkCheckTxOrSleep(KLQSstoreHash, utilsClass.sdkGetTxDetailType, SLEEPTIME);
        //验证上链返回值
        String checking = store.GetTxDetail(KLQSstoreHash);
        assertThat(checking, containsString("200"));
        assertThat(checking, containsString("success"));

        Thread.sleep(180000);//3分钟过期应该转让不成功
        //资产转让申请
        List<Map> list = new ArrayList<>(10);
        List<Map> list1 = UtilsClassScf.Assignment("1", "0", list);
        String response4 = scf.AssignmentApply(supplyAddress1, supplyID1, PIN, "123456", tokenType, list1, "n", supplyAddress2);
        assertThat(response4, containsString("200"));
        assertThat(response4, containsString("success"));
        assertThat(response4, containsString("data"));
        Thread.sleep(5000);
        //资产转让签收
        String response5 = scf.AssignmentConfirm(UID2, PlatformAddress, supplyID1, PIN, "123456", tokenType, comments);
        assertThat(response5, containsString("400"));
        assertThat(response5, containsString("error"));
        assertThat(response5, containsString("error"));

    }

    /**
     * 发起融资申请后转让//融资申请后资产相当于冻结应该无法转让
     */
    @Test
    public void TC004_transfer() throws Exception {
        int levelLimit = 5;
        String response = kms.genRandom(size);
        String tokenType = UtilsClassScf.gettokenType(response);
        BigDecimal expireDate = new BigDecimal(timeStampNow + 6000000);
        String UID = "a"+UtilsClass.Random(4);
        String UID1 = "b"+UtilsClass.Random(4);
        String UID2 = ""+UtilsClass.Random(4);
        //资产开立申请
        String response1 = scf.IssuingApply(UID, AccountAddress, companyID1, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, "10");
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("success"));
        assertThat(response1, containsString("data"));
        Thread.sleep(5000);
        //开立审核
        String response2 = scf.IssuingApprove(UID, platformKeyID, tokenType, platformPIN);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("success"));
        assertThat(response2, containsString("data"));
        Thread.sleep(5000);
        //开立签收
        String response3 = scf.IssuingConfirm(UID1, PlatformAddress, coreCompanyKeyID, tokenType, PIN, comments);
        assertThat(response3, containsString("200"));
        assertThat(response3, containsString("success"));
        assertThat(response3, containsString("data"));

        JSONObject KLjsonObject = JSONObject.fromObject(response3);
        String KLstoreHash = KLjsonObject.getString("data");
        String KLQSstoreHash = UtilsClassScf.strToHex(KLstoreHash);
        System.out.println("KLQSstoreHash = " + KLQSstoreHash);
        commonFunc.sdkCheckTxOrSleep(KLQSstoreHash, utilsClass.sdkGetTxDetailType, SLEEPTIME);
        //验证上链返回值
        String checking = store.GetTxDetail(KLQSstoreHash);
        assertThat(checking, containsString("200"));
        assertThat(checking, containsString("success"));

        //融资申请
        String newFromSubType = "2";
        String newToSubType = "1";
        String rzamount = "10";
        String proof = "123456";
        String subType = "0";
        String response4 = scf.FinacingApply(supplyAddress1, supplyID1, PIN, proof, tokenType, rzamount, subType, newFromSubType,newToSubType, supplyAddress2);
        assertThat(response4, containsString("200"));
        assertThat(response4, containsString("success"));
        assertThat(response4, containsString("data"));


        //资产转让申请
        List<Map> list = new ArrayList<>(10);
        List<Map> list1 = UtilsClassScf.Assignment("10", "0", list);
        String response5 = scf.AssignmentApply(supplyAddress1, supplyID1, PIN, "123456", tokenType, list1, "1", supplyAddress2);
        assertThat(response5, containsString("200"));
        assertThat(response5, containsString("success"));
        assertThat(response5, containsString("data"));
        Thread.sleep(5000);
        //资产转让签收
        String response6 = scf.AssignmentConfirm(UID2, PlatformAddress, supplyID1, PIN, "123456", tokenType, comments);
//        assertThat(response6, containsString("400"));
//        assertThat(response6, containsString("error"));
//        assertThat(response6, containsString("error"));

    }
    /**
     * 资产装让时连接超时，模拟【发起了两次请求，模拟第一次交易没有上链】
     */
    @Test
    public void TC005_() throws Exception {
        int levelLimit = 5;
        String response = kms.genRandom(size);
        String tokenType = UtilsClassScf.gettokenType(response);
        BigDecimal expireDate = new BigDecimal(timeStampNow + 60000);
        String UID = "abb";
        String UID1 = "b"+UtilsClass.Random(4);
        String UID2 = "b"+UtilsClass.Random(4);

        //资产开立申请
        String response1 = scf.IssuingApply(UID, AccountAddress, companyID1, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, "10");
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("success"));
        assertThat(response1, containsString("data"));
        Thread.sleep(5000);

        String response11= scf.IssuingApply(UID, AccountAddress, companyID1, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, "10");
        assertThat(response11, containsString("200"));
        assertThat(response11, containsString("success"));
        assertThat(response11, containsString("data"));
        //开立审核
        String response2 = scf.IssuingApprove(UID, platformKeyID, tokenType, platformPIN);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("success"));
        assertThat(response2, containsString("data"));
        Thread.sleep(5000);
        //开立签收
        String response3 = scf.IssuingConfirm(UID1, PlatformAddress, coreCompanyKeyID, tokenType, PIN, comments);
        assertThat(response3, containsString("200"));
        assertThat(response3, containsString("success"));
        assertThat(response3, containsString("data"));

        String response33 = scf.IssuingConfirm(UID1, PlatformAddress, coreCompanyKeyID, tokenType, PIN, comments);
        assertThat(response33, containsString("200"));
        assertThat(response33, containsString("success"));
        assertThat(response33, containsString("data"));


        JSONObject KLjsonObject = JSONObject.fromObject(response3);
        String KLstoreHash = KLjsonObject.getString("data");
        String KLQSstoreHash = UtilsClassScf.strToHex(KLstoreHash);
        System.out.println("KLQSstoreHash = " + KLQSstoreHash);
        commonFunc.sdkCheckTxOrSleep(KLQSstoreHash, utilsClass.sdkGetTxDetailType, SLEEPTIME);
        //验证上链返回值
        String checking = store.GetTxDetail(KLQSstoreHash);
        assertThat(checking, containsString("200"));
        assertThat(checking, containsString("success"));


        //资产转让申请
        List<Map> list = new ArrayList<>(10);
        List<Map> list1 = UtilsClassScf.Assignment("10", "0", list);
        String response5 = scf.AssignmentApply(supplyAddress1, supplyID1, PIN, "123456", tokenType, list1, "1", supplyAddress2);
        assertThat(response5, containsString("200"));
        assertThat(response5, containsString("success"));
        assertThat(response5, containsString("data"));
        Thread.sleep(5000);
        //资产转让签收
        String response6 = scf.AssignmentConfirm(UID2, PlatformAddress, supplyID1, PIN, "123456", tokenType, comments);
        assertThat(response6, containsString("200"));
        assertThat(response6, containsString("success"));
        assertThat(response6, containsString("data"));
        String response7 = scf.AssignmentConfirm(UID2, PlatformAddress, supplyID1, PIN, "123456", tokenType, comments);
        assertThat(response7, containsString("200"));
        assertThat(response7, containsString("success"));
        assertThat(response7, containsString("data"));


    }
}