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
//        bf.GetcommentsV2();
        bf.B002_createCoreCompanyAccount();
        bf.B003_installContracts();

        bf.B004_createSupplyAccounts();

        Thread.sleep(5000);
    }

    /**
     * ??????-??????-??????-????????????-??????output??????ID???index
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
        //??????????????????
        String response1 = scf.IssuingApply(UID,AccountAddress, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("success"));
        assertThat(response1, containsString("data"));
        Thread.sleep(5000);
        //????????????
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
        //?????????????????????
        String checking = store.GetTxDetail(KLQSstoreHash);
        assertThat(checking, containsString("200"));
        assertThat(checking, containsString("success"));
        //??????output?????????id???index
        String putinfo = scf.FuncGetsubtype(KLstoreHash, "0");
        assertThat(putinfo, containsString("200"));
        assertThat(putinfo, containsString("success"));



        //??????????????????id
        String txID = KLstoreHash;
        String historyid = scf.FuncGethistory(txID);
        assertThat(historyid, containsString("200"));
        assertThat(historyid, containsString("200"));

//        //??????output?????????id???index
//        String putinfo = scf.FuncGetoutputinfo(supplyAddress1, tokenType,"0");
//        assertThat(putinfo, containsString("200"));
//        assertThat(putinfo, containsString("success"));
//        JSONObject putinfo = JSONObject.fromObject(response3);
//        String KLstoreHash = KLjsonObject.getString("data");
//        String KLQSstoreHash = UtilsClassScf.strToHex(KLstoreHash);
//        System.out.println("KLQSstoreHash = " + KLQSstoreHash);


        //??????tokentype??????
//        String response5 = scf.getowneraddr(tokenType);
//        assertThat(response5, containsString("200"));
//        assertThat(response5, containsString("success"));
//        assertThat(response5, containsString("\"address\":\""+ supplyAddress1));
//        assertThat(response5, containsString("\"value\":100"));
//        //??????output?????????id???index
//        String response6 = scf.FuncGetoutputinfo(supplyAddress1, tokenType, subType);
//        assertThat(response6, containsString("200"));
//        assertThat(response6, containsString("success"));
//        assertThat(response6, containsString("data"));
//        assertThat(response6, containsString("\"index\":0"));
    }

    /**
     * ??????-??????-??????
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
        //??????????????????
        String response1 = scf.IssuingApply(UID,AccountAddress, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("success"));
        assertThat(response1, containsString("data"));
        Thread.sleep(5000);
        //????????????
        String response2 = scf.IssuingApprove(UID,platformKeyID, tokenType, platformPIN);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("success"));
        assertThat(response2, containsString("data"));
        Thread.sleep(5000);
        //????????????
        String response3 = scf.IssuingReject(UID1 ,coreCompanyKeyID, tokenType, PIN, companyID1, comments);
        assertThat(response3, containsString("200"));
        assertThat(response3, containsString("success"));
        assertThat(response3, containsString("data"));
    }

    /**
     * ??????-????????????
     */
    @Test
    public void Test003_IssuingCancel() {
        int levelLimit = 5;
        String amount = "100";
        String response = kms.genRandom(size);
        String tokenType = UtilsClassScf.gettokenType(response);
        String UID = "a"+UtilsClass.Random(4);
        //????????????
        String response1 = scf.IssuingApply(UID,AccountAddress, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("success"));
        assertThat(response1, containsString("data"));
        //????????????
        String response2 = scf.IssuingCancel(tokenType, platformKeyID, platformPIN);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("success"));
        assertThat(response2, containsString("data"));
    }

    /**
     * ??????-??????-??????-??????-??????-??????????????????id
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
        //??????????????????
        String response1 = scf.IssuingApply( UID,AccountAddress, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("success"));
        assertThat(response1, containsString("data"));
        Thread.sleep(5000);
        //????????????
        String response2 = scf.IssuingApprove(UID,platformKeyID, tokenType, platformPIN);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("success"));
        assertThat(response2, containsString("data"));
        Thread.sleep(5000);
        //????????????
        String response3 = scf.IssuingConfirm(UID1,PlatformAddress, coreCompanyKeyID, tokenType, PIN, comments);
        assertThat(response3, containsString("200"));
        assertThat(response3, containsString("success"));
        assertThat(response3, containsString("data"));

        JSONObject KLjsonObject = JSONObject.fromObject(response3);
        String KLstoreHash = KLjsonObject.getString("data");
        String KLQSstoreHash = UtilsClassScf.strToHex(KLstoreHash);
        System.out.println("KLQSstoreHash = " + KLQSstoreHash);

        commonFunc.sdkCheckTxOrSleep(KLQSstoreHash, utilsClass.sdkGetTxDetailType, SLEEPTIME);
        //?????????????????????
        String checking = store.GetTxDetail(KLQSstoreHash);
        assertThat(checking, containsString("200"));
        assertThat(checking, containsString("success"));
        //??????????????????
        List<Map> list = new ArrayList<>(10);
        List<Map> list1 = UtilsClassScf.Assignment("1", "0", list);
        String response4 = scf.AssignmentApply(supplyAddress1, supplyID1, PIN, proof, tokenType, list1, newSubType, supplyAddress2);
        assertThat(response4, containsString("200"));
        assertThat(response4, containsString("success"));
        assertThat(response4, containsString("data"));
        Thread.sleep(5000);
        //??????????????????
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

        //??????output?????????id???index
        String putinfo = scf.FuncGetsubtype(ZRstoreHash, "1");
        assertThat(putinfo, containsString("200"));
        assertThat(putinfo, containsString("success"));

        //??????tokentype??????
//        String response6 = scf.getowneraddr(tokenType);
//        assertThat(response6, containsString("200"));
//        assertThat(response6, containsString("success"));
//        assertThat(response6, containsString("\"address\":\""+ supplyAddress2));
//        assertThat(response6, containsString("\"value\":1"));
//        assertThat(response6, containsString("\"address\":\""+ supplyAddress1));
//        assertThat(response6, containsString("\"value\":99"));
    }

    /**
     * ??????-??????-??????- ????????????????????????????????????-????????????-??????-??????
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
        //??????????????????
        String response1 = scf.IssuingApply( UID, AccountAddress, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("success"));
        assertThat(response1, containsString("data"));
        Thread.sleep(5000);
        //????????????
        String response2 = scf.IssuingApprove(UID, platformKeyID, tokenType, platformPIN);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("success"));
        assertThat(response2, containsString("data"));
        Thread.sleep(5000);
        //????????????
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

        //????????????????????????????????????
//        String financeTxID = KLstoreHash;
//        ArrayList<String> kIDlist = new ArrayList<>();
//        kIDlist.add(supplyID1);
//        kIDlist.add(supplyID2);
//        String authorization = scf.FuncAuthorization(PlatformAddress, supplierMsg1, financeTxID, kIDlist, platformKeyID, platformPIN);
//        assertThat(authorization, containsString("200"));
//        assertThat(authorization, containsString("success"));
//        assertThat(authorization, containsString("data"));

        JSONObject SHjsonObject = JSONObject.fromObject(response3);
        String SHstoreHash = SHjsonObject.getString("data");
        String SHQSstoreHash = UtilsClassScf.strToHex(SHstoreHash);
        System.out.println("SHQSstoreHash = " + SHQSstoreHash);

        commonFunc.sdkCheckTxOrSleep(KLQSstoreHash, utilsClass.sdkGetTxDetailType, SLEEPTIME);
        String checking1 = store.GetTxDetail(SHQSstoreHash);
        assertThat(checking1, containsString("200"));
        assertThat(checking1, containsString("success"));

        //??????????????????
//        ArrayList<String> Msglist = new ArrayList<>();
//        Msglist.add(supplierMsg1);
//        String response4 = scf.FunGethistoryinfo(PlatformAddress, Msglist, platformKeyID, platformPIN);
//        assertThat(response4, containsString("200"));
//        assertThat(response4, containsString("success"));
//        assertThat(response4, containsString("data"));

        //??????????????????
        List<Map> list = new ArrayList<>(10);
        List<Map> list1 = UtilsClassScf.Assignment("1", "0", list);
        String response5 = scf.AssignmentApply(supplyAddress1, supplyID1, PIN, proof, tokenType, list1, newSubType, supplyAddress2);
        assertThat(response5, containsString("200"));
        assertThat(response5, containsString("success"));
        assertThat(response5, containsString("data"));
        Thread.sleep(5000);
        //??????????????????
        String response6 = scf.AssignmentReject(UID2, challenge, tokenType);
        assertThat(response6, containsString("200"));
        assertThat(response6, containsString("success"));
        assertThat(response6, containsString("data"));
    }

    /**
     * ??????-??????-??????-??????-????????????-??????????????????-????????????-??????
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
        //??????????????????
        String response1 = scf.IssuingApply( UID, AccountAddress, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("success"));
        assertThat(response1, containsString("data"));
        Thread.sleep(5000);
        //????????????
        String response2 = scf.IssuingApprove(UID, platformKeyID, tokenType, platformPIN);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("success"));
        assertThat(response2, containsString("data"));
        Thread.sleep(5000);
        //????????????
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
        //????????????
        String newFromSubType = "0";
        String newToSubType = "b";
        String rzamount = "1";
        String response4 = scf.FinacingApply(supplyAddress1, supplyID1,companyID1, PIN, proof, tokenType, rzamount, subType, newFromSubType, newToSubType, supplyAddress2);
        assertThat(response4, containsString("200"));
        assertThat(response4, containsString("success"));
        assertThat(response4, containsString("data"));
        //????????????
//        String timeLimit = "10";
//        String response5 = scf.FinacingTest(ZJFAddress, rzamount, timeLimit);
//        assertThat(response5, containsString("200"));
//        assertThat(response5, containsString("success"));
//        assertThat(response5, containsString("data"));
        //??????????????????
        String applyNo = "7777";
        String state = "1";
        String msg = "receive";
        String response6 = scf.FinacingFeedback(ZJFAddress, applyNo, state, comments, msg);
        assertThat(response6, containsString("200"));
        assertThat(response6, containsString("success"));
        assertThat(response6, containsString("data"));
        Thread.sleep(5000);
        //????????????
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

        //??????
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
     * ??????-??????-??????-????????????-????????????-??????????????????-????????????
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
        //??????????????????
        String response1 = scf.IssuingApply(UID, AccountAddress, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("success"));
        assertThat(response1, containsString("data"));
        Thread.sleep(5000);
        //????????????
        String response2 = scf.IssuingApprove(UID, platformKeyID, tokenType, platformPIN);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("success"));
        assertThat(response2, containsString("data"));
        Thread.sleep(5000);
        //????????????
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
        //????????????
        String newFromSubType = "0";
        String newToSubType = "b";
        String response4 = scf.FinacingApply(supplyAddress1, supplyID1, companyID1, PIN, proof, tokenType, amount, subType, newFromSubType, newToSubType, supplyAddress2);
        assertThat(response4, containsString("200"));
        assertThat(response4, containsString("success"));
        assertThat(response4, containsString("data"));
        //????????????
//        String timeLimit = "10";
//        String response5 = scf.FinacingTest(ZJFAddress, amount, timeLimit);
//        assertThat(response5, containsString("200"));
//        assertThat(response5, containsString("success"));
//        assertThat(response5, containsString("data"));
        //??????????????????
        String applyNo = "7777";
        String state = "1";
        String msg = "remove";
        String response6 = scf.FinacingFeedback(ZJFAddress, applyNo, state, comments, msg);
        assertThat(response6, containsString("200"));
        assertThat(response6, containsString("success"));
        assertThat(response6, containsString("data"));
        //????????????
        String response7 = scf.FinacingCancel(UID2,challenge, tokenType);
        assertThat(response7, containsString("200"));
        assertThat(response7, containsString("success"));
        assertThat(response7, containsString("data"));
    }

    /**
     * ??????-??????-??????-????????????-????????????-????????????-????????????
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
        //??????????????????
        String response1 = scf.IssuingApply(UID, AccountAddress, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("success"));
        assertThat(response1, containsString("data"));
        Thread.sleep(5000);
        //????????????
        String response2 = scf.IssuingApprove(UID, platformKeyID, tokenType, platformPIN);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("success"));
        assertThat(response2, containsString("data"));
        Thread.sleep(5000);
        //????????????
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

        //????????????
        String response4 = scf.PayingApply(tokenType, comments);
        assertThat(response4, containsString("200"));
        assertThat(response4, containsString("success"));
        assertThat(response4, containsString("data"));
        Thread.sleep(5000);
        //????????????
        String message = "????????????????????????";
        String response5 = scf.PayingNotify(AccountAddress, message);
        assertThat(response5, containsString("200"));
        assertThat(response5, containsString("success"));
        assertThat(response5, containsString("data"));
        Thread.sleep(5000);
        //????????????
        String state = "1";
        String response6 = scf.PayingFeedback(QFJGAddress, tokenType, state, comments);
        assertThat(response6, containsString("200"));
        assertThat(response6, containsString("success"));
        assertThat(response6, containsString("data"));
        Thread.sleep(5000);
        //????????????
        List<Map> list = new ArrayList<>(10);
        List<Map> list1 = UtilsClassScf.paying(supplyAddress1, supplyID1, "0", "100", list);
        String response9 = scf.PayingConfirmV2(UID2, PlatformAddress, QFJGAddress, list1, platformKeyID, platformPIN, tokenType, comments);
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
     * ??????-??????-??????-??????-??????-????????????-????????????-????????????-????????????
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
        //??????????????????
        String response1 = scf.IssuingApply(UID, AccountAddress, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("success"));
        assertThat(response1, containsString("data"));
        Thread.sleep(5000);
        //????????????
        String response2 = scf.IssuingApprove(UID, platformKeyID, tokenType, platformPIN);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("success"));
        assertThat(response2, containsString("data"));
        Thread.sleep(5000);
        //????????????
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

        //??????????????????
        List<Map> list = new ArrayList<>(10);
        List<Map> list1 = UtilsClassScf.Assignment("1", "0", list);
        String response4 = scf.AssignmentApply(supplyAddress1, supplyID1, PIN, proof, tokenType, list1, newSubType, supplyAddress2);
        assertThat(response4, containsString("200"));
        assertThat(response4, containsString("success"));
        assertThat(response4, containsString("data"));
        Thread.sleep(5000);
        //??????????????????
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
        //??????????????????
        String response6 = scf.PayingApply(tokenType,  comments);
        assertThat(response6, containsString("200"));
        assertThat(response6, containsString("success"));
        assertThat(response6, containsString("data"));
        Thread.sleep(5000);
        //????????????
        String message = "????????????????????????";
        String response7 = scf.PayingNotify(AccountAddress, message);
        assertThat(response7, containsString("200"));
        assertThat(response7, containsString("success"));
        assertThat(response7, containsString("data"));
        Thread.sleep(5000);
        //????????????
        String state = "1";
        String response8 = scf.PayingFeedback(QFJGAddress, tokenType, state, comments);
        assertThat(response8, containsString("200"));
        assertThat(response8, containsString("success"));
        assertThat(response8, containsString("data"));
        Thread.sleep(5000);
        //????????????
        List<Map> list2 = UtilsClassScf.paying(supplyAddress1, supplyID1, "0", "99", list);
        List<Map> list3 = UtilsClassScf.paying(supplyAddress2, supplyID2, "n", "1", list2);
        String response9 = scf.PayingConfirmV2(UID3, PlatformAddress, QFJGAddress, list3, platformKeyID, platformPIN, tokenType, comments);
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
     * ??????-??????-??????-??????-????????????-??????????????????-????????????-????????????-????????????-????????????-????????????
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
        //??????????????????,?????????1
        String response1 = scf.IssuingApply(UID, AccountAddress, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("success"));
        assertThat(response1, containsString("data"));
        Thread.sleep(5000);
        //????????????
        String response2 = scf.IssuingApprove(UID, platformKeyID, tokenType, platformPIN);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("success"));
        assertThat(response2, containsString("data"));
        Thread.sleep(5000);
        //????????????
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

        //?????????????????????????????????1????????????????????????????????????2
        String newFromSubType = "0";
        String newToSubType = "b";
        String rzamount = "10";
        String response4 = scf.FinacingApply(supplyAddress1, supplyID1, companyID1, PIN, proof, tokenType, rzamount, subType, newFromSubType, newToSubType, supplyAddress2);
        assertThat(response4, containsString("200"));
        assertThat(response4, containsString("success"));
        assertThat(response4, containsString("data"));
        //????????????
//        String timeLimit = "10";
//        String response5 = scf.FinacingTest(ZJFAddress, rzamount, timeLimit);
//        assertThat(response5, containsString("200"));
//        assertThat(response5, containsString("success"));
//        assertThat(response5, containsString("data"));
        //??????????????????
        String applyNo = "7777";
        String msg = "receive";
        String response6 = scf.FinacingFeedback(ZJFAddress, applyNo, state, comments, msg);
        assertThat(response6, containsString("200"));
        assertThat(response6, containsString("success"));
        assertThat(response6, containsString("data"));
        Thread.sleep(5000);
        //????????????
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

        //??????????????????
        String response8 = scf.PayingApply(tokenType, comments);
        assertThat(response8, containsString("200"));
        assertThat(response8, containsString("success"));
        assertThat(response8, containsString("data"));
        Thread.sleep(5000);
        //????????????
        String message = "????????????????????????";
        String response9 = scf.PayingNotify(AccountAddress, message);
        assertThat(response9, containsString("200"));
        assertThat(response9, containsString("success"));
        assertThat(response9, containsString("data"));
        Thread.sleep(5000);
        //????????????
        String response10 = scf.PayingFeedback(QFJGAddress, tokenType, state, comments);
        assertThat(response10, containsString("200"));
        assertThat(response10, containsString("success"));
        assertThat(response10, containsString("data"));
        Thread.sleep(5000);
        //????????????
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

    }

    /**
     * ??????-??????-??????-??????-??????-??????-????????????-??????????????????-????????????-????????????-????????????-????????????-????????????
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
        //??????????????????
        String response1 = scf.IssuingApply(UID, AccountAddress, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("success"));
        assertThat(response1, containsString("data"));
        Thread.sleep(5000);
        //????????????
        String response2 = scf.IssuingApprove(UID, platformKeyID, tokenType, platformPIN);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("success"));
        assertThat(response2, containsString("data"));
        Thread.sleep(5000);
        //????????????
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

        //??????????????????
        List<Map> list = new ArrayList<>(10);
        List<Map> list1 = UtilsClassScf.Assignment("99", "0", list);
        String response4 = scf.AssignmentApply(supplyAddress1, supplyID1, PIN, proof, tokenType, list1, newSubType, supplyAddress2);
        assertThat(response4, containsString("200"));
        assertThat(response4, containsString("success"));
        assertThat(response4, containsString("data"));
        Thread.sleep(5000);
        //??????????????????
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
        //????????????
        String newFromSubType = "0";
        String newToSubType = "b";
        String rzamount = "10";
        String response6 = scf.FinacingApply(supplyAddress2, supplyID2, companyID1, PIN, rzproof, tokenType, rzamount, subType, newFromSubType, newToSubType, supplyAddress3);
        assertThat(response6, containsString("200"));
        assertThat(response6, containsString("success"));
        assertThat(response6, containsString("data"));
        //????????????
//        String timeLimit = "10";
//        String response7 = scf.FinacingTest(ZJFAddress, rzamount, timeLimit);
//        assertThat(response7, containsString("200"));
//        assertThat(response7, containsString("success"));
//        assertThat(response7, containsString("data"));
        //??????????????????
        String applyNo = "7777";
        String msg = "receive";
        String response8 = scf.FinacingFeedback(ZJFAddress, applyNo, state, comments, msg);
        assertThat(response8, containsString("200"));
        assertThat(response8, containsString("success"));
        assertThat(response8, containsString("data"));
        Thread.sleep(5000);
        //????????????
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

        //??????????????????
        String response10 = scf.PayingApply(tokenType, comments);
        assertThat(response10, containsString("200"));
        assertThat(response10, containsString("success"));
        assertThat(response10, containsString("data"));
        //????????????
        String message = "????????????????????????";
        String response11 = scf.PayingNotify(AccountAddress, message);
        assertThat(response11, containsString("200"));
        assertThat(response11, containsString("success"));
        assertThat(response11, containsString("data"));
        //????????????
        String response12 = scf.PayingFeedback(QFJGAddress, tokenType, state, comments);
        assertThat(response12, containsString("200"));
        assertThat(response12, containsString("success"));
        assertThat(response12, containsString("data"));
        Thread.sleep(5000);
        //????????????
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

    }

    /**
     * ??????????????????==companyID2
     */
    @Test
    public void Test012_CreditAdjust() {
        String amount = "30000000.0";
        String response1 = scf.CreditAdjust(AccountAddress, companyID1, amount);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("success"));
        assertThat(response1, containsString("data"));
    }

    /**
     * ??????output?????????id???index
     */
    @Test//???????????????????????????????????????tokentype
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
     * ????????????-??????????????????-??????-??????-??????
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
        //??????????????????
        String response3 = scf.IssuingApply(UID, AccountAddress, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
        assertThat(response3, containsString("200"));
        assertThat(response3, containsString("success"));
        assertThat(response3, containsString("data"));
        Thread.sleep(5000);
        //????????????
        String response4 = scf.IssuingApprove(UID, platformKeyID, tokenType, platformPIN);
        assertThat(response4, containsString("200"));
        assertThat(response4, containsString("success"));
        assertThat(response4, containsString("data"));
        Thread.sleep(5000);
        //????????????
        String response5 = scf.IssuingConfirm(UID1, PlatformAddress, coreCompanyKeyID, tokenType, PIN, comments);
        assertThat(response5, containsString("200"));
        assertThat(response5, containsString("success"));
        assertThat(response5, containsString("data"));
    }

    /**
     * ??????????????????
     */
    @Test
    public void Test015_Send() {
        String response1 = scf.Send(comments);

        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("success"));
        assertThat(response1, containsString("data"));
    }

    /**
     * ????????????????????????//??????????????????
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
        //??????????????????
        String response1 = scf.IssuingApply(UID, AccountAddress, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("success"));
        assertThat(response1, containsString("data"));
        Thread.sleep(5000);
        //????????????
        String response2 = scf.IssuingApprove(UID, platformKeyID, tokenType, platformPIN);
        assertThat(response2, containsString("200"));
        assertThat(response2, containsString("success"));
        assertThat(response2, containsString("data"));
        Thread.sleep(5000);
        //????????????
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
        //????????????
        String newFromSubType = "0";
        String newToSubType = "b";
        String rzamount = "1";
        String response4 = scf.FinacingApply(supplyAddress1, supplyID1, companyID1, PIN, proof, tokenType, rzamount, subType, newFromSubType, newToSubType, supplyAddress2);
        assertThat(response4, containsString("200"));
        assertThat(response4, containsString("success"));
        assertThat(response4, containsString("data"));

        //??????????????????
        String applyNo = "7777";
        String state = "1";
        String msg = "receive";
        String response6 = scf.FinacingFeedback(ZJFAddress, applyNo, state, comments, msg);
        assertThat(response6, containsString("200"));
        assertThat(response6, containsString("success"));
        assertThat(response6, containsString("data"));
        Thread.sleep(5000);
        //????????????
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

        //??????
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

        //??????
        //??????????????????
        List<Map> list = new ArrayList<>(10);
        List<Map> list1 = UtilsClassScf.Assignment("1", "b", list);
        String response8 = scf.AssignmentApply(supplyAddress1, supplyID1, PIN, proof, tokenType, list1, "2", supplyAddress2);
        assertThat(response8, containsString("200"));
        assertThat(response8, containsString("success"));
        assertThat(response8, containsString("data"));
        Thread.sleep(5000);
        //??????????????????
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
