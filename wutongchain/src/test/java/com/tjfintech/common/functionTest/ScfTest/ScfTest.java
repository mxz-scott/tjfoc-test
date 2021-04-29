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

import java.math.BigDecimal;
import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class ScfTest {

    TestBuilder testBuilder = TestBuilder.getInstance();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    Scf scf = testBuilder.getScf();
    Store store = testBuilder.getStore();
    public static double timeStampNow = System.currentTimeMillis();
    public static BigDecimal expireDate = new BigDecimal(timeStampNow + 1000000000);
    //public static long expireDate = System.currentTimeMillis() + 100000000;
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
     * 开立-审核-签收-查询资产-获取output交易ID和index
     *
     * @throws InterruptedException
     */
    @Test
    public void Test001_IssuingApply() throws Exception {
        int levelLimit = 5;
        String amount = "100";
        String subType = "0";
        String response = kms.genRandom(size);
        String tokenType = UtilsClassScf.gettokenType(response);
        String UID = "a"+UtilsClass.Random(4);
        String UID1 = "b"+UtilsClass.Random(4);
        //资产开立申请
        String response1 = scf.IssuingApply(UID,AccountAddress, companyID1, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("success"));
        assertThat(response1, containsString("data"));
        Thread.sleep(5000);
        //开立审核
        String response2 = scf.IssuingApprove(UID,platformKeyID, tokenType, platformPIN);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("success"));
        assertThat(response2, containsString("data"));
        Thread.sleep(5000);

        String response3 = scf.IssuingConfirm(UID1,PlatformAddress, coreCompanyKeyID, tokenType, PIN, comments);
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

        //获取历史交易id
        String txID = KLstoreHash;
        String historyid = scf.FuncGethistory(txID);
        assertThat(historyid, containsString("200"));
        assertThat(historyid, containsString("200"));

    }

    /**
     * 开立-审核-拒收
     *
     * @throws InterruptedException
     */
    @Test
    public void Test002_IssuingReject() throws Exception {
        int levelLimit = 5;
        String amount = "100";
        String response = kms.genRandom(size);
        String tokenType = UtilsClassScf.gettokenType(response);
        String UID = "a"+UtilsClass.Random(4);
        String UID1 = "b"+UtilsClass.Random(4);
        //资产开立申请
        String response1 = scf.IssuingApply(UID,AccountAddress, companyID1, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("success"));
        assertThat(response1, containsString("data"));
        Thread.sleep(5000);
        //开立审核
        String response2 = scf.IssuingApprove(UID,platformKeyID, tokenType, platformPIN);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("success"));
        assertThat(response2, containsString("data"));
        Thread.sleep(5000);
        //开立拒收
        String response3 = scf.IssuingReject(UID1 ,coreCompanyKeyID, tokenType, PIN, companyID1, comments);
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
        String UID = "a"+UtilsClass.Random(4);
        //资产开立
        String response1 = scf.IssuingApply(UID,AccountAddress, companyID1, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("success"));
        assertThat(response1, containsString("data"));
        //开立取消
        String response2 = scf.IssuingCancel(tokenType,platformKeyID, platformPIN);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("success"));
        assertThat(response2, containsString("data"));
    }

    /**
     * 开立-审核-签收-转让-签收-获取历史交易id
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
        String UID = "a"+UtilsClass.Random(4);
        String UID1 = "b"+UtilsClass.Random(4);
        String UID2 = "c"+UtilsClass.Random(4);
        //资产开立申请
        String response1 = scf.IssuingApply( UID,AccountAddress, companyID1, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("success"));
        assertThat(response1, containsString("data"));
        Thread.sleep(5000);
        //开立审核
        String response2 = scf.IssuingApprove(UID,platformKeyID, tokenType, platformPIN);
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
        //资产转让申请
        List<Map> list = new ArrayList<>(10);
        List<Map> list1 = UtilsClassScf.Assignment("1", "0", list);
        String response4 = scf.AssignmentApply(supplyAddress1, supplyID1, PIN, proof, tokenType, list1, newSubType, supplyAddress2);
        assertThat(response4, containsString("200"));
        assertThat(response4, containsString("success"));
        assertThat(response4, containsString("data"));
        Thread.sleep(5000);
        //资产转让签收
        String response5 = scf.AssignmentConfirm(UID2,PlatformAddress, supplyID1, PIN, challenge, tokenType, comments);
        assertThat(response5, containsString("200"));
        assertThat(response5, containsString("success"));
        assertThat(response5, containsString("data"));

        JSONObject ZRjsonObject = JSONObject.fromObject(response5);
        String ZRstoreHash = ZRjsonObject.getString("data");
        String ZRQSstoreHash = UtilsClassScf.strToHex(ZRstoreHash);
        System.out.println("ZRQSstoreHash = " + ZRQSstoreHash);

        commonFunc.sdkCheckTxOrSleep(ZRQSstoreHash, utilsClass.sdkGetTxDetailType, SLEEPTIME);
        String checking1 = store.GetTxDetail(ZRQSstoreHash);

        assertThat(checking1, containsString("200"));
        assertThat(checking1, containsString("success"));

    }

    /**
     * 开立-审核-签收- 授权供应商信息和贸易背景-查看信息-转让-拒收
     */
    @Test
    public void Test005_AssignmentReject() throws Exception {
        int levelLimit = 5;
        String amount = "100";
        String response = kms.genRandom(size);
        String newSubType = "n";
        String proof = "123456";
        String challenge = "123456";
        String tokenType = UtilsClassScf.gettokenType(response);
        String UID = "a"+UtilsClass.Random(4);
        String UID1 = "b"+UtilsClass.Random(4);
        String UID2 = "c"+UtilsClass.Random(4);
        //资产开立申请
        String response1 = scf.IssuingApply( UID, AccountAddress, companyID1, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
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
        String checking = store.GetTxDetail(KLQSstoreHash);
        assertThat(checking, containsString("200"));
        assertThat(checking, containsString("success"));

        //授权供应商背景和贸易信息
        String financeTxID = KLstoreHash;
        ArrayList<String> kIDlist = new ArrayList<>();
        kIDlist.add(supplyID1);
        kIDlist.add(supplyID2);
        String authorization = scf.FuncAuthorization(PlatformAddress, supplierMsg1, financeTxID, kIDlist, platformKeyID, platformPIN);
        assertThat(authorization, containsString("200"));
        assertThat(authorization, containsString("success"));
        assertThat(authorization, containsString("data"));

        JSONObject SHjsonObject = JSONObject.fromObject(response3);
        String SHstoreHash = SHjsonObject.getString("data");
        String SHQSstoreHash = UtilsClassScf.strToHex(SHstoreHash);
        System.out.println("SHQSstoreHash = " + SHQSstoreHash);

        commonFunc.sdkCheckTxOrSleep(KLQSstoreHash, utilsClass.sdkGetTxDetailType, SLEEPTIME);
        String checking1 = store.GetTxDetail(SHQSstoreHash);
        assertThat(checking1, containsString("200"));
        assertThat(checking1, containsString("success"));

        //查看授权信息
        ArrayList<String> Msglist = new ArrayList<>();
        Msglist.add(supplierMsg1);
        String response4 = scf.FunGethistoryinfo(PlatformAddress, Msglist, platformKeyID, platformPIN);
        assertThat(response4, containsString("200"));
        assertThat(response4, containsString("success"));
        assertThat(response4, containsString("data"));

        //资产转让申请
        List<Map> list = new ArrayList<>(10);
        List<Map> list1 = UtilsClassScf.Assignment("1", "0", list);
        String response5 = scf.AssignmentApply(supplyAddress1, supplyID1, PIN, proof, tokenType, list1, newSubType, supplyAddress2);
        assertThat(response5, containsString("200"));
        assertThat(response5, containsString("success"));
        assertThat(response5, containsString("data"));
        Thread.sleep(5000);
        //资产转让拒收
        String response6 = scf.AssignmentReject(UID2, challenge, tokenType);
        assertThat(response6, containsString("200"));
        assertThat(response6, containsString("success"));
        assertThat(response6, containsString("data"));
    }

    /**
     * 开立-审核-签收-融资-融资试算-融资申请反馈-融资签收-抹账
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
        String UID = "a"+UtilsClass.Random(4);
        String UID1 = "b"+UtilsClass.Random(4);
        String UID2 = "c"+UtilsClass.Random(4);
        String UID3 = "d"+UtilsClass.Random(4);
        //资产开立申请
        String response1 = scf.IssuingApply( UID, AccountAddress, companyID1, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
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
        String checking = store.GetTxDetail(KLQSstoreHash);
        assertThat(checking, containsString("200"));
        assertThat(checking, containsString("success"));
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
        String msg = "receive";
        String response6 = scf.FinacingFeedback(ZJFAddress, applyNo, state, comments, msg);
        assertThat(response6, containsString("200"));
        assertThat(response6, containsString("success"));
        assertThat(response6, containsString("data"));
        Thread.sleep(5000);
        //融资签收
        String response7 = scf.FinacingConfirm(UID2, PlatformAddress, applyNo, ZJFAddress, supplyID1, companyID1, PIN, tokenType, supplyAddress2, challenge, comments);
        assertThat(response7, containsString("200"));
        assertThat(response7, containsString("success"));
        assertThat(response7, containsString("data"));

        JSONObject RZjsonObject = JSONObject.fromObject(response7);
        String RZstoreHash = RZjsonObject.getString("data");
        String RZQSstoreHash = UtilsClassScf.strToHex(RZstoreHash);
        System.out.println("RZQSstoreHash = " + RZQSstoreHash);

        commonFunc.sdkCheckTxOrSleep(RZQSstoreHash, utilsClass.sdkGetTxDetailType, SLEEPTIME);
        String checking1 = store.GetTxDetail(RZQSstoreHash);
        assertThat(checking1, containsString("200"));
        assertThat(checking1, containsString("success"));
        Thread.sleep(5000);

        //抹账
        String txID = RZstoreHash;
        String Mzrespones = scf.FinacingBack(UID3, PlatformAddress, platformKeyID, platformPIN, supplyID2, txID, comments);
        assertThat(Mzrespones, containsString("200"));
        assertThat(Mzrespones, containsString("success"));
        assertThat(Mzrespones, containsString("data"));

        JSONObject MZjsonObject = JSONObject.fromObject(Mzrespones);
        Object data = MZjsonObject.get("data");
        JSONObject jsonObject = JSONObject.fromObject(data);
        Object txId = jsonObject.get("txId");
        System.out.println("txId = " + txId.toString());
        String MZQSstoreHash = UtilsClassScf.strToHex(txId.toString());
        System.out.println("MZQSstoreHash = " + MZQSstoreHash);

        commonFunc.sdkCheckTxOrSleep(MZQSstoreHash, utilsClass.sdkGetTxDetailType, SLEEPTIME);
        String checking2 = store.GetTxDetail(MZQSstoreHash);
        assertThat(checking2, containsString("200"));
        assertThat(checking2, containsString("success"));

    }

    /**
     * 开立-审核-签收-融资申请-融资试算-融资申请反馈-融资取消
     */
    @Test
    public void Test007_FinacingCancel() throws Exception {
        int levelLimit = 5;
        String amount = "1";
        String subType = "0";
        String response = kms.genRandom(size);
        String proof = "1234567";
        String challenge = "1234567";
        String tokenType = UtilsClassScf.gettokenType(response);
        String UID = "a"+UtilsClass.Random(4);
        String UID1 = "b"+UtilsClass.Random(4);
        String UID2 = "c"+UtilsClass.Random(4);
        //资产开立申请
        String response1 = scf.IssuingApply(UID, AccountAddress, companyID1, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
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
        String checking = store.GetTxDetail(KLQSstoreHash);
        assertThat(checking, containsString("200"));
        assertThat(checking, containsString("success"));
        //融资申请
        String newFromSubType = "0";
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
        String msg = "remove";
        String response6 = scf.FinacingFeedback(ZJFAddress, applyNo, state, comments, msg);
        assertThat(response6, containsString("200"));
        assertThat(response6, containsString("success"));
        assertThat(response6, containsString("data"));
        //融资取消
        String response7 = scf.FinacingCancel(UID2,challenge, tokenType);
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
        String UID = "a"+UtilsClass.Random(4);
        String UID1 = "b"+UtilsClass.Random(4);
        String UID2 = "c"+UtilsClass.Random(4);
        //资产开立申请
        String response1 = scf.IssuingApply(UID, AccountAddress, companyID1, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
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
        String checking = store.GetTxDetail(KLQSstoreHash);
        assertThat(checking, containsString("200"));
        assertThat(checking, containsString("success"));

        //兑付申请
        String response4 = scf.PayingApply(tokenType, companyID1, comments);
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
        String response9 = scf.PayingConfirm(UID2, PlatformAddress, QFJGAddress, companyID1, list1, platformKeyID, platformPIN, tokenType, comments);
        assertThat(response9, containsString("200"));
        assertThat(response9, containsString("success"));
        assertThat(response9, containsString("data"));

        JSONObject DFjsonObject = JSONObject.fromObject(response9);
        String DFstoreHash = DFjsonObject.getString("data");
        String DFQRstoreHash = UtilsClassScf.strToHex(DFstoreHash);
        System.out.println("DFQRstoreHash = " + DFQRstoreHash);

        commonFunc.sdkCheckTxOrSleep(DFQRstoreHash, utilsClass.sdkGetTxDetailType, SLEEPTIME);
        String checking1 = store.GetTxDetail(DFQRstoreHash);

        assertThat(checking1, containsString("200"));
        assertThat(checking1, containsString("success"));

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
        String UID = "a"+UtilsClass.Random(4);
        String UID1 = "b"+UtilsClass.Random(4);
        String UID2 = "c"+UtilsClass.Random(4);
        String UID3 = "d"+UtilsClass.Random(4);
        //资产开立申请
        String response1 = scf.IssuingApply(UID, AccountAddress, companyID1, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
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
        String checking = store.GetTxDetail(KLQSstoreHash);
        assertThat(checking, containsString("200"));
        assertThat(checking, containsString("success"));

        //资产转让申请
        List<Map> list = new ArrayList<>(10);
        List<Map> list1 = UtilsClassScf.Assignment("1", "0", list);
        String response4 = scf.AssignmentApply(supplyAddress1, supplyID1, PIN, proof, tokenType, list1, newSubType, supplyAddress2);
        assertThat(response4, containsString("200"));
        assertThat(response4, containsString("success"));
        assertThat(response4, containsString("data"));
        Thread.sleep(5000);
        //资产转让签收
        String response5 = scf.AssignmentConfirm(UID2, PlatformAddress, supplyID1, PIN, challenge, tokenType, comments);
        assertThat(response5, containsString("200"));
        assertThat(response5, containsString("success"));
        assertThat(response5, containsString("data"));

        JSONObject ZRjsonObject = JSONObject.fromObject(response5);
        String ZRstoreHash = ZRjsonObject.getString("data");
        String ZRQSstoreHash = UtilsClassScf.strToHex(ZRstoreHash);
        System.out.println("ZRQSstoreHash = " + ZRQSstoreHash);

        commonFunc.sdkCheckTxOrSleep(ZRQSstoreHash, utilsClass.sdkGetTxDetailType, SLEEPTIME);
        String checking1 = store.GetTxDetail(ZRQSstoreHash);
        assertThat(checking1, containsString("200"));
        assertThat(checking1, containsString("success"));
        //资产兑付申请
        String response6 = scf.PayingApply(tokenType, companyID1, comments);
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
        String response9 = scf.PayingConfirm(UID3, PlatformAddress, QFJGAddress, companyID1, list3, platformKeyID, platformPIN, tokenType, comments);
        assertThat(response9, containsString("200"));
        assertThat(response9, containsString("success"));
        assertThat(response9, containsString("data"));

        JSONObject DFjsonObject = JSONObject.fromObject(response9);
        String DFstoreHash = DFjsonObject.getString("data");
        String DFQRstoreHash = UtilsClassScf.strToHex(DFstoreHash);
        System.out.println("DFQRstoreHash = " + DFQRstoreHash);

        commonFunc.sdkCheckTxOrSleep(DFQRstoreHash, utilsClass.sdkGetTxDetailType, SLEEPTIME);
        String checking2 = store.GetTxDetail(DFQRstoreHash);
        assertThat(checking2, containsString("200"));
        assertThat(checking2, containsString("success"));

    }

    /**
     * 开立-审核-签收-融资-融资试算-融资申请反馈-融资签收-兑付申请-兑付通知-兑付反馈-兑付确认-获取账户未使用余额
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
        String UID = "a"+UtilsClass.Random(4);
        String UID1 = "b"+UtilsClass.Random(4);
        String UID2 = "c"+UtilsClass.Random(4);
        String UID3 = "d"+UtilsClass.Random(4);
        //资产开立申请,金额为1
        String response1 = scf.IssuingApply(UID, AccountAddress, companyID1, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
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
        String checking = store.GetTxDetail(KLQSstoreHash);
        assertThat(checking, containsString("200"));
        assertThat(checking, containsString("success"));

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
        String msg = "receive";
        String response6 = scf.FinacingFeedback(ZJFAddress, applyNo, state, comments, msg);
        assertThat(response6, containsString("200"));
        assertThat(response6, containsString("success"));
        assertThat(response6, containsString("data"));
        Thread.sleep(5000);
        //融资签收
        String response7 = scf.FinacingConfirm(UID2, PlatformAddress, applyNo, ZJFAddress, supplyID1, companyID1, PIN, tokenType, supplyAddress2, challenge, comments);
        assertThat(response7, containsString("200"));
        assertThat(response7, containsString("success"));
        assertThat(response7, containsString("data"));

        JSONObject RZjsonObject = JSONObject.fromObject(response7);
        String RZstoreHash = RZjsonObject.getString("data");
        String RZQSstoreHash = UtilsClassScf.strToHex(RZstoreHash);
        System.out.println("RZQSstoreHash = " + RZQSstoreHash);

        commonFunc.sdkCheckTxOrSleep(RZQSstoreHash, utilsClass.sdkGetTxDetailType, SLEEPTIME);
        String checking1 = store.GetTxDetail(RZQSstoreHash);
        assertThat(checking1, containsString("200"));
        assertThat(checking1, containsString("success"));

        //资产兑付申请
        String response8 = scf.PayingApply(tokenType, companyID1, comments);
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
        String response11 = scf.PayingConfirm(UID3,PlatformAddress, QFJGAddress, companyID1, list2, platformKeyID, platformPIN, tokenType, comments);
        assertThat(response11, containsString("200"));
        assertThat(response11, containsString("success"));
        assertThat(response11, containsString("data"));

        JSONObject DFjsonObject = JSONObject.fromObject(response11);
        String DFstoreHash = DFjsonObject.getString("data");
        String DFQRstoreHash = UtilsClassScf.strToHex(DFstoreHash);
        System.out.println("DFQRstoreHash = " + DFQRstoreHash);

        commonFunc.sdkCheckTxOrSleep(DFQRstoreHash, utilsClass.sdkGetTxDetailType, SLEEPTIME);
        String checking2 = store.GetTxDetail(DFQRstoreHash);
        assertThat(checking2, containsString("200"));
        assertThat(checking2, containsString("success"));

        String response12 = scf.FunBalanceunused(AccountAddress);
        assertThat(response12, containsString("200"));
        assertThat(response12, containsString("success"));
        assertThat(response12, containsString("52410"));

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
        String UID = "a"+UtilsClass.Random(4);
        String UID1 = "b"+UtilsClass.Random(4);
        String UID2 = "c"+UtilsClass.Random(4);
        String UID3 = "d"+UtilsClass.Random(4);
        String UID4 = "e"+UtilsClass.Random(4);
        //资产开立申请
        String response1 = scf.IssuingApply(UID, AccountAddress, companyID1, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
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
        String checking = store.GetTxDetail(KLQSstoreHash);
        assertThat(checking, containsString("200"));
        assertThat(checking, containsString("success"));

        //资产转让申请
        List<Map> list = new ArrayList<>(10);
        List<Map> list1 = UtilsClassScf.Assignment("99", "0", list);
        String response4 = scf.AssignmentApply(supplyAddress1, supplyID1, PIN, proof, tokenType, list1, newSubType, supplyAddress2);
        assertThat(response4, containsString("200"));
        assertThat(response4, containsString("success"));
        assertThat(response4, containsString("data"));
        Thread.sleep(5000);
        //资产转让签收
        String response5 = scf.AssignmentConfirm(UID2, PlatformAddress, supplyID1, PIN, challenge, tokenType, comments);
        assertThat(response5, containsString("200"));
        assertThat(response5, containsString("success"));
        assertThat(response5, containsString("data"));
        JSONObject ZRjsonObject = JSONObject.fromObject(response3);
        String ZRstoreHash = ZRjsonObject.getString("data");
        String ZRQSstoreHash = UtilsClassScf.strToHex(ZRstoreHash);
        System.out.println("ZRQSstoreHash = " + ZRQSstoreHash);

        commonFunc.sdkCheckTxOrSleep(ZRQSstoreHash, utilsClass.sdkGetTxDetailType, SLEEPTIME);
        String checking1 = store.GetTxDetail(ZRQSstoreHash);
        assertThat(checking1, containsString("200"));
        assertThat(checking1, containsString("success"));
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
        String msg = "receive";
        String response8 = scf.FinacingFeedback(ZJFAddress, applyNo, state, comments, msg);
        assertThat(response8, containsString("200"));
        assertThat(response8, containsString("success"));
        assertThat(response8, containsString("data"));
        Thread.sleep(5000);
        //融资签收
        String response9 = scf.FinacingConfirm(UID3, PlatformAddress, applyNo, ZJFAddress, supplyID2, companyID1, PIN, tokenType, supplyAddress3, rzchallenge, comments);
        assertThat(response9, containsString("200"));
        assertThat(response9, containsString("success"));
        assertThat(response9, containsString("data"));

        JSONObject RZjsonObject = JSONObject.fromObject(response9);
        String RZstoreHash = RZjsonObject.getString("data");
        String RZQSstoreHash = UtilsClassScf.strToHex(RZstoreHash);
        System.out.println("RZQSstoreHash = " + RZQSstoreHash);

        commonFunc.sdkCheckTxOrSleep(RZQSstoreHash, utilsClass.sdkGetTxDetailType, SLEEPTIME);
        String checking2 = store.GetTxDetail(RZQSstoreHash);

        assertThat(checking2, containsString("200"));
        assertThat(checking2, containsString("success"));

        //资产兑付申请
        String response10 = scf.PayingApply(tokenType, companyID1, comments);
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
        String response13 = scf.PayingConfirm(UID4, PlatformAddress, QFJGAddress, companyID1, list4, platformKeyID, platformPIN, tokenType, comments);
        assertThat(response13, containsString("200"));
        assertThat(response13, containsString("success"));
        assertThat(response13, containsString("data"));
        JSONObject DFjsonObject = JSONObject.fromObject(response13);
        String DFstoreHash = DFjsonObject.getString("data");
        String DFQRstoreHash = UtilsClassScf.strToHex(DFstoreHash);
        System.out.println("DFQRstoreHash = " + DFQRstoreHash);

        commonFunc.sdkCheckTxOrSleep(DFQRstoreHash, utilsClass.sdkGetTxDetailType, SLEEPTIME);
        String checking3 = store.GetTxDetail(DFQRstoreHash);
        assertThat(checking3, containsString("200"));
        assertThat(checking3, containsString("success"));

//         String response14 = scf.getowneraddr(tokenType);
//         assertThat(response14, containsString("200"));
//         assertThat(response14, containsString("success"));
//         assertThat(response14, containsString("\"address\":\"0000000000000000\""));
//         assertThat(response14, containsString("\"value\":100"));
    }

    /**
     * 授信额度调整==companyID2
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
     * 获取output的交易id和index
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

    /**
     * 创建账户-修改账户信息-开立-审核-签收
     */
    @Test
    public void Test014_AccountInform() throws Exception {
        int levelLimit = 5;
        String amount = "100";
        String response = kms.genRandom(size);
        String tokenType = UtilsClassScf.gettokenType(response);
        String UID = "a"+UtilsClass.Random(4);
        String UID1 = "b"+UtilsClass.Random(4);

        String response1 = scf.AccountCreate(PlatformAddress, platformKeyID, PIN, "", comments);

        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("success"));
        assertThat(response1, containsString("data"));

        String response2 = scf.AccountInform(PlatformAddress, comments);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("success"));
        assertThat(response2, containsString("data"));
        //资产开立申请
        String response3 = scf.IssuingApply(UID, AccountAddress, companyID1, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
        assertThat(response3, containsString("200"));
        assertThat(response3, containsString("success"));
        assertThat(response3, containsString("data"));
        Thread.sleep(5000);
        //开立审核
        String response4 = scf.IssuingApprove(UID, platformKeyID, tokenType, platformPIN);
        assertThat(response4, containsString("200"));
        assertThat(response4, containsString("success"));
        assertThat(response4, containsString("data"));
        Thread.sleep(5000);
        //开立签收
        String response5 = scf.IssuingConfirm(UID1, PlatformAddress, coreCompanyKeyID, tokenType, PIN, comments);
        assertThat(response5, containsString("200"));
        assertThat(response5, containsString("success"));
        assertThat(response5, containsString("data"));
    }

    /**
     * 发送事件通知
     */
    @Test
    public void Test015_Send() {
        String response1 = scf.Send(comments);

        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("success"));
        assertThat(response1, containsString("data"));
    }

    /**
     * 开立融资抹账转让//融资需要授权
     */
    @Test
    public void Test016_MZ_ZR() throws Exception {
        int levelLimit = 5;
        String amount = "1";
        String subType = "0";
        String response = kms.genRandom(size);
        String proof = "123456";
        String challenge = "123456";
        String tokenType = UtilsClassScf.gettokenType(response);
        String UID = "a"+UtilsClass.Random(4);
        String UID1 = "b"+UtilsClass.Random(4);
        String UID2 = "b"+UtilsClass.Random(4);
        String UID3 = "b"+UtilsClass.Random(4);
        String UID4 = "b"+UtilsClass.Random(4);
        //资产开立申请
        String response1 = scf.IssuingApply(UID, AccountAddress, companyID1, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
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
        String checking = store.GetTxDetail(KLQSstoreHash);
        assertThat(checking, containsString("200"));
        assertThat(checking, containsString("success"));
        //融资申请
        String newFromSubType = "0";
        String newToSubType = "b";
        String rzamount = "1";
        String response4 = scf.FinacingApply(supplyAddress1, supplyID1, PIN, proof, tokenType, rzamount, subType, newFromSubType, newToSubType, supplyAddress2);
        assertThat(response4, containsString("200"));
        assertThat(response4, containsString("success"));
        assertThat(response4, containsString("data"));

        //融资申请反馈
        String applyNo = "7777";
        String state = "1";
        String msg = "receive";
        String response6 = scf.FinacingFeedback(ZJFAddress, applyNo, state, comments, msg);
        assertThat(response6, containsString("200"));
        assertThat(response6, containsString("success"));
        assertThat(response6, containsString("data"));
        Thread.sleep(5000);
        //融资签收
        String response7 = scf.FinacingConfirm(UID2, PlatformAddress, applyNo, ZJFAddress, supplyID1, companyID1, PIN, tokenType, supplyAddress2, challenge, comments);
        assertThat(response7, containsString("200"));
        assertThat(response7, containsString("success"));
        assertThat(response7, containsString("data"));

        JSONObject RZjsonObject = JSONObject.fromObject(response7);
        String RZstoreHash = RZjsonObject.getString("data");
        String RZQSstoreHash = UtilsClassScf.strToHex(RZstoreHash);
        System.out.println("RZQSstoreHash = " + RZQSstoreHash);

        commonFunc.sdkCheckTxOrSleep(RZQSstoreHash, utilsClass.sdkGetTxDetailType, SLEEPTIME);
        String checking1 = store.GetTxDetail(RZQSstoreHash);
        assertThat(checking1, containsString("200"));
        assertThat(checking1, containsString("success"));
        Thread.sleep(5000);

        //抹账
        String txID = RZstoreHash;
        String Mzrespones = scf.FinacingBack(UID3, PlatformAddress, platformKeyID, platformPIN, supplyID2, txID, comments);
        assertThat(Mzrespones, containsString("200"));
        assertThat(Mzrespones, containsString("success"));
        assertThat(Mzrespones, containsString("data"));

        JSONObject MZjsonObject = JSONObject.fromObject(Mzrespones);
        Object data = MZjsonObject.get("data");
        JSONObject jsonObject = JSONObject.fromObject(data);
        Object txId = jsonObject.get("txId");
        System.out.println("txId = " + txId.toString());
        String MZQSstoreHash = UtilsClassScf.strToHex(txId.toString());
        System.out.println("MZQSstoreHash = " + MZQSstoreHash);

        commonFunc.sdkCheckTxOrSleep(MZQSstoreHash, utilsClass.sdkGetTxDetailType, SLEEPTIME);
        String checking2 = store.GetTxDetail(MZQSstoreHash);
        assertThat(checking2, containsString("200"));
        assertThat(checking2, containsString("success"));

        //转账
        //资产转让申请
        List<Map> list = new ArrayList<>(10);
        List<Map> list1 = UtilsClassScf.Assignment("1", "b", list);
        String response8 = scf.AssignmentApply(supplyAddress1, supplyID1, PIN, proof, tokenType, list1, "1", supplyAddress2);
        assertThat(response8, containsString("200"));
        assertThat(response8, containsString("success"));
        assertThat(response8, containsString("data"));
        Thread.sleep(5000);
        //资产转让签收
        String response9 = scf.AssignmentConfirm(UID4, PlatformAddress, supplyID1, PIN, challenge, tokenType, comments);
        assertThat(response9, containsString("200"));
        assertThat(response9, containsString("success"));
        assertThat(response9, containsString("data"));

        JSONObject ZRjsonObject = JSONObject.fromObject(response9);
        String ZRstoreHash = ZRjsonObject.getString("data");
        String ZRQSstoreHash = UtilsClassScf.strToHex(ZRstoreHash);
        System.out.println("ZRQSstoreHash = " + ZRQSstoreHash);

        commonFunc.sdkCheckTxOrSleep(ZRQSstoreHash, utilsClass.sdkGetTxDetailType, SLEEPTIME);
        String checking3 = store.GetTxDetail(ZRQSstoreHash);

        assertThat(checking3, containsString("200"));
        assertThat(checking3, containsString("success"));

    }

}
