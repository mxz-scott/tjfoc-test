package com.tjfintech.common.functionTest.ScfTest;

import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.Kms;
import com.tjfintech.common.Interface.Scf;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import com.tjfintech.common.utils.UtilsClassScf;
import net.sf.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.SLEEPTIME;
import static com.tjfintech.common.utils.UtilsClassKMS.size;
import static com.tjfintech.common.utils.UtilsClassScf.*;
import static com.tjfintech.common.utils.UtilsClassScf.comments;
import static com.tjfintech.common.utils.UtilsClassScf.supplyAddress1;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

public class ScfInvalidTest {

    TestBuilder testBuilder = TestBuilder.getInstance();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    Scf scf = testBuilder.getScf();
    Store store = testBuilder.getStore();
    public static long expireDate = System.currentTimeMillis() + 100000000;
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
     *资产开立申请
     */
    @Test
    public void Test001_IssuingApply() throws Exception {
        int levelLimit = 5;
        String amount = "100";
        String subType = "0";
        String response = kms.genRandom(size);
        String tokenType = UtilsClassScf.gettokenType(response);
        // 输入供应链金融合约地址为空
        String response1 = scf.IssuingApply("", companyID1, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
        assertThat(response1, containsString("400"));
        assertThat(response1, containsString("error"));
        assertThat(response1, containsString("Key: 'ContractAddress' Error:Field validation for 'ContractAddress' failed on the 'min' tag"));
        //输入资金方为空
        String response2 = scf.IssuingApply(AccountAddress, "", coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
        assertThat(response2, containsString("400"));
        assertThat(response2, containsString("error"));
        assertThat(response2, containsString("Key: 'CompanyID' Error:Field validation for 'CompanyID' failed on the 'required' tag"));
        //输入核心企业ID为空
        String response3 = scf.IssuingApply(AccountAddress, companyID1, "", PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
        assertThat(response3, containsString("400"));
        assertThat(response3, containsString("error"));
        assertThat(response3, containsString("Key: 'KeyID' Error:Field validation for 'KeyID' failed on the 'len' tag"));
        //输入核心企业pin码为空
        String response4 = scf.IssuingApply(AccountAddress, companyID1, coreCompanyKeyID, "", tokenType, levelLimit, expireDate, supplyAddress1, amount);
        assertThat(response4, containsString("400"));
        assertThat(response4, containsString("error"));
        assertThat(response4, containsString("Key: 'PIN' Error:Field validation for 'PIN' failed on the 'min' tag"));
        //输入tokentype为空
        String response5 = scf.IssuingApply(AccountAddress, companyID1, coreCompanyKeyID, PIN, "", levelLimit, expireDate, supplyAddress1, amount);
        assertThat(response5, containsString("400"));
        assertThat(response5, containsString("error"));
        assertThat(response5, containsString("Key: 'TokenType' Error:Field validation for 'TokenType' failed on the 'min' tag"));
        //受让地址为空
        String response6 = scf.IssuingApply(AccountAddress, companyID1, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, "", amount);
        assertThat(response6, containsString("400"));
        assertThat(response6, containsString("error"));
        assertThat(response6, containsString("Key: 'ToAddr' Error:Field validation for 'ToAddr' failed on the 'min' tag"));
        //开立额度为空
        String response7 = scf.IssuingApply(AccountAddress, companyID1, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, "");
        assertThat(response7, containsString("400"));
        assertThat(response7, containsString("error"));
        assertThat(response7, containsString("Key: 'Amount' Error:Field validation for 'Amount' failed on the 'min' tag"));
    }

    @Test
    public void Test002_IssuingApply() throws Exception {
        int levelLimit = 5;
        String amount = "100";
        String subType = "0";
        String response = kms.genRandom(size);
        String tokenType = UtilsClassScf.gettokenType(response);
        //供应链金融合约填写错误
        String response1 = scf.IssuingApply("3228bf4f6c3d20b641fb508cfddcf578c1fbd92b64b962327b476bc9fb123456", companyID1, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
        assertThat(response1, containsString("400"));
        assertThat(response1, containsString("error"));
        assertThat(response1, containsString("get  enterprise err:rpc error: code = Unknown desc = Smart contract does not exist"));
       //资金方id填写错误
        String response2 = scf.IssuingApply(AccountAddress, "008", coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
        assertThat(response2, containsString("400"));
        assertThat(response2, containsString("error"));
        assertThat(response2, containsString("Err:账号合约验证失败"));
        //输入错误资金方ID
        String response3 = scf.IssuingApply(AccountAddress, companyID1, "c02jbhbsnk7pq9fsojfg", PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
        assertThat(response3, containsString("400"));
        assertThat(response3, containsString("error"));
        assertThat(response3, containsString("pin码与密钥不匹配,或请稍后再试"));
        //输入错误pin码
        String response4 = scf.IssuingApply(AccountAddress, companyID1, coreCompanyKeyID, "789", tokenType, levelLimit, expireDate, supplyAddress1, amount);
        assertThat(response4, containsString("400"));
        assertThat(response4, containsString("error"));
        assertThat(response4, containsString("pin码与密钥不匹配,或请稍后再试"));
    }

    @Test
    public void Test003_IssuingApply() throws Exception {
        int levelLimit = 5;
        String amount = "100";
        String subType = "0";
        String response = kms.genRandom(size);
        String tokenType = UtilsClassScf.gettokenType(response);
        //输入重复tokentype
        String response1 = scf.IssuingApply(AccountAddress, companyID1, coreCompanyKeyID, PIN, "99", levelLimit, expireDate, supplyAddress1, amount);
        assertThat(response1, containsString("400"));
        assertThat(response1, containsString("error"));
        assertThat(response1, containsString("Err:这个数字资产正在发行中"));

    }

    @Test
    public void Test004_IssuingApply() throws Exception {
        int levelLimit = 5;
        String amount = "100";
        String subType = "0";
        String response = kms.genRandom(size);
        String tokenType = UtilsClassScf.gettokenType(response);
        //开立特大金额
        String response1 = scf.IssuingApply(AccountAddress, companyID1, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, "10000000");
        assertThat(response1, containsString("400"));
        assertThat(response1, containsString("error"));
        assertThat(response1, containsString("Err:合约配额已经耗尽"));
        //开立超过小数点后6位数字资产
        String response2 = scf.IssuingApply(AccountAddress, companyID1, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, "100.123456789");
        assertThat(response2, containsString("503"));
        assertThat(response2, containsString("error"));
        assertThat(response2, containsString("timeout!"));
        //开立空资产
        String response3 = scf.IssuingApply(AccountAddress, companyID1, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, "0");
        assertThat(response3, containsString("400"));
        assertThat(response3, containsString("Token amount must be a valid number and less than 18446744073709"));
    }
    /**
     * 开立审核接口
     */
    @Test
    public void Test005_IssuingApply() throws Exception {
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
        //输入空平台方id
        String response2 = scf.IssuingApprove("", tokenType, platformPIN);
        assertThat(response2, containsString("400"));
        assertThat(response2, containsString("error"));
        assertThat(response2, containsString("Key: 'KeyID' Error:Field validation for 'KeyID' failed on the 'len' tag"));
        //输入错误平台方id
        String response3 = scf.IssuingApprove("", tokenType, platformPIN);
        assertThat(response3, containsString("400"));
        assertThat(response3, containsString("error"));
        assertThat(response3, containsString("Key: 'KeyID' Error:Field validation for 'KeyID' failed on the 'len' tag"));
        //输入空tokentype
        String response4 = scf.IssuingApprove(platformKeyID, "", platformPIN);
        assertThat(response4, containsString("400"));
        assertThat(response4, containsString("error"));
        assertThat(response4, containsString("Key: 'TokenType' Error:Field validation for 'TokenType' failed on the 'min' tag"));
        //输入错误tokentype
        String response5 = scf.IssuingApprove(platformKeyID, "abcd", platformPIN);
        assertThat(response5, containsString("400"));
        assertThat(response5, containsString("error"));
        assertThat(response5, containsString("empty sign data or \\u003cnil\\u003e"));
        //输入空平台pin码
        String response6 = scf.IssuingApprove(platformKeyID, tokenType, "");
        assertThat(response6, containsString("400"));
        assertThat(response6, containsString("error"));
        assertThat(response6, containsString("Key: 'PIN' Error:Field validation for 'PIN' failed on the 'min' tag"));
        //输入错误pin码
        String response7 = scf.IssuingApprove(platformKeyID, tokenType, "7654321");
        assertThat(response7, containsString("400"));
        assertThat(response7, containsString("error"));
        assertThat(response7, containsString("pin码与密钥不匹配,或请稍后再试"));
    }

    /**
     * 开立取消
     */
    @Test
    public void Test006_IssuingCancel() {
        int levelLimit = 5;
        String amount = "100";
        String response = kms.genRandom(size);
        String tokenType = UtilsClassScf.gettokenType(response);
        //资产开立
        String response1 = scf.IssuingApply(AccountAddress, companyID1, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("success"));
        assertThat(response1, containsString("data"));
        //输入空tokentype
        String response2 = scf.IssuingCancel("", companyID1, platformKeyID, platformPIN, comments);
        assertThat(response2, containsString("400"));
        assertThat(response2, containsString("error"));
        assertThat(response2, containsString("Key: 'TokenType' Error:Field validation for 'TokenType' failed on the 'required' tag"));
        //输入错误tokentype
//        String response3 = scf.IssuingCancel("asdf", companyID1, platformKeyID, platformPIN, comments);
//        assertThat(response3, containsString("400"));
//        assertThat(response3, containsString("error"));
//        assertThat(response3, containsString("Key: 'TokenType' Error:Field validation for 'TokenType' failed on the 'required' tag"));
        //输入空资金方id
//        String response4 = scf.IssuingCancel(tokenType, "", platformKeyID, platformPIN, comments);
//        assertThat(response4, containsString("400"));
//        assertThat(response4, containsString("error"));
//        assertThat(response4, containsString("data"));
        //输入错误资金方id
//        String response5 = scf.IssuingCancel(tokenType, "008", platformKeyID, platformPIN, comments);
//        assertThat(response5, containsString("400"));
//        assertThat(response5, containsString("error"));
//        assertThat(response5, containsString("data"));
        //输入空平台方id
        String response6 = scf.IssuingCancel(tokenType, companyID1, "", platformPIN, comments);
        assertThat(response6, containsString("400"));
        assertThat(response6, containsString("error"));
        assertThat(response6, containsString("Key: 'KeyID' Error:Field validation for 'KeyID' failed on the 'len' tag"));
        //输入错误平台方id
        String response7 = scf.IssuingCancel(tokenType, companyID1, "c02jbhbsnk7pq9fsojfg", platformPIN, comments);
        assertThat(response7, containsString("400"));
        assertThat(response7, containsString("error"));
        assertThat(response7, containsString("pin码与密钥不匹配,或请稍后再试"));
        //输入空平台方pin码
        String response8 = scf.IssuingCancel(tokenType, companyID1, platformKeyID, "", comments);
        assertThat(response8, containsString("400"));
        assertThat(response8, containsString("error"));
        assertThat(response8, containsString("Key: 'PIN' Error:Field validation for 'PIN' failed on the 'required' tag"));
        //输入错误平台方pin码
        String response9 = scf.IssuingCancel(tokenType, companyID1, platformKeyID, "9999", comments);
        assertThat(response9, containsString("400"));
        assertThat(response9, containsString("error"));
        assertThat(response9, containsString("pin码与密钥不匹配,或请稍后再试"));
        //输入空comments
//        String response10 = scf.IssuingCancel(tokenType, companyID1, platformKeyID, platformPIN, "");
//        assertThat(response10, containsString("400"));
//        assertThat(response10, containsString("error"));
//        assertThat(response10, containsString("data"));
        //输入错误comments
//        String response11 = scf.IssuingCancel(tokenType, companyID1, platformKeyID, platformPIN, "adasdaskahbfhja");
//        assertThat(response11, containsString("400"));
//        assertThat(response11, containsString("error"));
//        assertThat(response11, containsString("data"));
    }

    /**
     * 开单签收
     */
    @Test
    public void Test007_Assignmentapply() throws Exception {
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
        //开立签收，输入空平台方合约地址
        String response3 = scf.IssuingConfirm("", coreCompanyKeyID, tokenType, PIN, comments);
        assertThat(response3, containsString("400"));
        assertThat(response3, containsString("error"));
        assertThat(response3, containsString("Key: 'PlatFormAddress' Error:Field validation for 'PlatFormAddress' failed on the 'required' tag"));
        //输入错误平台方合约地址
        String response4 = scf.IssuingConfirm("274532de3b53ccce779132fa9b5a2a10dcad4e2b230e5625605a2d7c0fdebce2", coreCompanyKeyID, tokenType, PIN, comments);
        assertThat(response4, containsString("400"));
        assertThat(response4, containsString("error"));
        assertThat(response4, containsString("Err:empty stack"));
        //输入空核心企业id
        String response5 = scf.IssuingConfirm(PlatformAddress, "", tokenType, PIN, comments);
        assertThat(response5, containsString("400"));
        assertThat(response5, containsString("error"));
        assertThat(response5, containsString("Key: 'KeyID' Error:Field validation for 'KeyID' failed on the 'len' tag"));
        //输入错误核心企业id
        String response6 = scf.IssuingConfirm(PlatformAddress, "c02jbhbsnk7pq9fsojfg", tokenType, PIN, comments);
        assertThat(response6, containsString("400"));
        assertThat(response6, containsString("error"));
        assertThat(response6, containsString("pin码与密钥不匹配,或请稍后再试"));
        //输入空核心企业pin码
        String response7 = scf.IssuingConfirm(PlatformAddress, coreCompanyKeyID, tokenType, "", comments);
        assertThat(response7, containsString("400"));
        assertThat(response7, containsString("error"));
        assertThat(response7, containsString("Key: 'PIN' Error:Field validation for 'PIN' failed on the 'min' tag"));
        //输入错误核心企业pin码
        String response8 = scf.IssuingConfirm(PlatformAddress, coreCompanyKeyID, tokenType, "8999", comments);
        assertThat(response8, containsString("400"));
        assertThat(response8, containsString("error"));
        assertThat(response8, containsString("pin码与密钥不匹配,或请稍后再试"));
        //输入空tokentype
        String response9 = scf.IssuingConfirm(PlatformAddress, coreCompanyKeyID, "", PIN, comments);
        assertThat(response9, containsString("400"));
        assertThat(response9, containsString("error"));
        assertThat(response9, containsString("Key: 'TokenType' Error:Field validation for 'TokenType' failed on the 'min' tag"));
        //输入错误tokentype
        String response10 = scf.IssuingConfirm(PlatformAddress, coreCompanyKeyID, "asdfw", PIN, comments);
        assertThat(response10, containsString("400"));
        assertThat(response10, containsString("error"));
        assertThat(response10, containsString("tokentype未发行!"));
        //输入空comments
        String response11 = scf.IssuingConfirm(PlatformAddress, coreCompanyKeyID, tokenType, PIN, "");
        assertThat(response11, containsString("400"));
        assertThat(response11, containsString("error"));
        assertThat(response11, containsString("Key: 'Comments' Error:Field validation for 'Comments' failed on the 'required' tag"));
        //输入错误comments
        String response12 = scf.IssuingConfirm(PlatformAddress, coreCompanyKeyID, tokenType, PIN, "dadasdadasdasdasdasdasdad");
        assertThat(response12, containsString("400"));
        assertThat(response12, containsString("error"));
        assertThat(response12, containsString("comments不合法!"));

    }

    /**
     * 开单拒收
     */
     @Test
     public void Test008_IssuingReject() throws Exception {
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
         //输入空供应商地址
         String response3 = scf.IssuingReject("", tokenType, PIN, companyID1, comments);
         assertThat(response3, containsString("400"));
         assertThat(response3, containsString("error"));
         assertThat(response3, containsString("Key: 'KeyID' Error:Field validation for 'KeyID' failed on the 'len' tag"));
         //输入错误供应商地址
         String response4 = scf.IssuingReject("c02jbhbsnk7pq9fsojfg", tokenType, PIN, companyID1, comments);
         assertThat(response4, containsString("400"));
         assertThat(response4, containsString("error"));
         assertThat(response4, containsString("pin码与密钥不匹配,或请稍后再试"));
         //输入空核心企业pin码
         String response5 = scf.IssuingReject(coreCompanyKeyID, tokenType, "", companyID1, comments);
         assertThat(response5, containsString("400"));
         assertThat(response5, containsString("error"));
         assertThat(response5, containsString("Key: 'PIN' Error:Field validation for 'PIN' failed on the 'min' tag"));
         //输入错误核心企业pin码
         String response6 = scf.IssuingReject(coreCompanyKeyID, tokenType, "5855", companyID1, comments);
         assertThat(response6, containsString("400"));
         assertThat(response6, containsString("error"));
         assertThat(response6, containsString("pin码与密钥不匹配,或请稍后再试"));
         //输入空tokentype
         String response7 = scf.IssuingReject(coreCompanyKeyID, "", PIN, companyID1, comments);
         assertThat(response7, containsString("400"));
         assertThat(response7, containsString("error"));
         assertThat(response7, containsString("Key: 'TokenType' Error:Field validation for 'TokenType' failed on the 'min' tag"));
         //输入错误tokentype
         String response8 = scf.IssuingReject(coreCompanyKeyID, "qwer", PIN, companyID1, comments);
         assertThat(response8, containsString("400"));
         assertThat(response8, containsString("error"));
         assertThat(response8, containsString("tokentype未发行!"));
         //输入空资金方id
         String response9 = scf.IssuingReject(coreCompanyKeyID, tokenType, PIN, "", comments);
         assertThat(response9, containsString("400"));
         assertThat(response9, containsString("error"));
         assertThat(response9, containsString("Key: 'CompanyID' Error:Field validation for 'CompanyID' failed on the 'required' tag"));
         //输入错误资金方id
//         String response10 = scf.IssuingReject(coreCompanyKeyID, tokenType, PIN, "008", comments);
//         assertThat(response10, containsString("400"));
//         assertThat(response10, containsString("error"));
//         assertThat(response10, containsString("asd"));
         //输入空comments
//         String response11 = scf.IssuingReject(coreCompanyKeyID, tokenType, PIN, companyID1, "");
//         assertThat(response11, containsString("400"));
//         assertThat(response11, containsString("error"));
//         assertThat(response11, containsString("asd"));
         //输入错误comments
//         String response12 = scf.IssuingReject(coreCompanyKeyID, tokenType, PIN, companyID1, "qwenqwjiiqjwndijqwnipdn");
//         assertThat(response12, containsString("400"));
//         assertThat(response12, containsString("error"));
//         assertThat(response12, containsString("asd"));
     }

    /**
     * 资产转让申请（参数错误）
     */
    @Test
    public void Test009_Assignmentapply() throws Exception {
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
        String response3 = scf.IssuingConfirm(PlatformAddress, coreCompanyKeyID, tokenType, PIN, comments);
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
        //输入空供应商账户地址
        List<Map> list = new ArrayList<>(10);
        List<Map> list1 = UtilsClassScf.Assignment("1", "0", list);
        String response4 = scf.AssignmentApply("", supplyID1, PIN, proof, tokenType, list1, newSubType, supplyAddress2);
        assertThat(response4, containsString("400"));
        assertThat(response4, containsString("error"));
        assertThat(response4, containsString("Key: 'FromAddress' Error:Field validation for 'FromAddress' failed on the 'min' tag"));
        //输入错误供应商账户地址
        String response5 = scf.AssignmentApply("SnwNAEuLDPUZRqkuDbVmbNugisspXgdx4wfBgoiL6F1ArAWVhEn", supplyID1, PIN, proof, tokenType, list1, newSubType, supplyAddress2);
        assertThat(response5, containsString("400"));
        assertThat(response5, containsString("error"));
        assertThat(response5, containsString("Err:wvm invoke err"));
        //输入空供应商id
        String response6 = scf.AssignmentApply(supplyAddress1, "", PIN, proof, tokenType, list1, newSubType, supplyAddress2);
        assertThat(response6, containsString("400"));
        assertThat(response6, containsString("error"));
        assertThat(response6, containsString("Key: 'KeyID' Error:Field validation for 'KeyID' failed on the 'len' tag"));
        //输入错误供应商id
//        String response7 = scf.AssignmentApply(supplyAddress1, "c02jbhjsnk7pq9fsojva", PIN, proof, tokenType, list1, newSubType, supplyAddress2);
//        assertThat(response7, containsString("503"));
//        assertThat(response7, containsString("error"));
//        assertThat(response7, containsString("timeout"));
        //输入错空pin码
        String response8 = scf.AssignmentApply(supplyAddress1, supplyID1, "", proof, tokenType, list1, newSubType, supplyAddress2);
        assertThat(response8, containsString("400"));
        assertThat(response8, containsString("error"));
        assertThat(response8, containsString("Key: 'PIN' Error:Field validation for 'PIN' failed on the 'min' tag"));
        //输入错误pin码
//        String response9 = scf.AssignmentApply(supplyAddress1, supplyID1, "56464", proof, tokenType, list1, newSubType, supplyAddress2);
//        assertThat(response9, containsString("200"));
//        assertThat(response9, containsString("success"));
//        assertThat(response9, containsString("data"));
        //输入空tokentype
        String response10 = scf.AssignmentApply(supplyAddress1, supplyID1, PIN, proof, "", list1, newSubType, supplyAddress2);
        assertThat(response10, containsString("400"));
        assertThat(response10, containsString("error"));
        assertThat(response10, containsString("Key: 'TokenType' Error:Field validation for 'TokenType' failed on the 'required' tag"));
        //输入负值字符tokentype
        String response11 = scf.AssignmentApply(supplyAddress1, supplyID1, PIN, proof, "-1", list1, newSubType, supplyAddress2);
        assertThat(response11, containsString("400"));
        assertThat(response11, containsString("error"));
        assertThat(response11, containsString("tokentype未发行!"));
        //输入错误tokentype
        String response12 = scf.AssignmentApply(supplyAddress1, supplyID1, PIN, proof, "55421", list1, newSubType, supplyAddress2);
        assertThat(response12, containsString("400"));
        assertThat(response12, containsString("error"));
        assertThat(response12, containsString("tokentype未发行!"));
        //输入错误空newSubType
        String response13 = scf.AssignmentApply(supplyAddress1, supplyID1, PIN, proof, tokenType, list1, "", supplyAddress2);
        assertThat(response13, containsString("400"));
        assertThat(response13, containsString("error"));
        assertThat(response13, containsString("Key: 'NewSubType' Error:Field validation for 'NewSubType' failed on the 'required' tag"));
        //输入负值字符newSubType
//        String response14 = scf.AssignmentApply(supplyAddress1, supplyID1, PIN, proof, tokenType, list1, "-1", supplyAddress2);
//        assertThat(response14, containsString("200"));
//        assertThat(response14, containsString("success"));
//        assertThat(response14, containsString("data"));
        //输入空接收方地址
        String response15 = scf.AssignmentApply(supplyAddress1, supplyID1, PIN, proof, tokenType, list1, newSubType, "");
        assertThat(response15, containsString("400"));
        assertThat(response15, containsString("error"));
        assertThat(response15, containsString("Key: 'ToAddress' Error:Field validation for 'ToAddress' failed on the 'required' tag"));
        //输入错误接收方地址
//        String response16 = scf.AssignmentApply(supplyAddress1, supplyID1, PIN, proof, tokenType, list1, newSubType, "SoV1KWJqSr4TWXZd1kpnLG19WU95LbjMMQv7Dygph9nkHvRCNvE");
//        assertThat(response16, containsString("200"));
//        assertThat(response16, containsString("success"));
//        assertThat(response16, containsString("data"));
    }

    /**
     * 资产转让申请（amount为负值）
     */
    @Test
    public void Test0010_Assignmentapply() throws Exception {
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
        String response3 = scf.IssuingConfirm(PlatformAddress, coreCompanyKeyID, tokenType, PIN, comments);
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
        List<Map> list1 = UtilsClassScf.Assignment("-1", "0", list);
        String response4 = scf.AssignmentApply(supplyAddress1, supplyID1, PIN, proof, tokenType, list1, newSubType, supplyAddress2);
        assertThat(response4, containsString("400"));
        assertThat(response4, containsString("error"));
        assertThat(response4, containsString("strconv.ParseUint: parsing \\\"-1\\\": invalid syntax"));
    }

    /**
     * 资产转让申请（输入转让额度超过开立额度）
     */
    @Test
    public void Test0011_Assignmentapply() throws Exception {
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
        String response3 = scf.IssuingConfirm(PlatformAddress, coreCompanyKeyID, tokenType, PIN, comments);
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
        List<Map> list1 = UtilsClassScf.Assignment("1000", "0", list);
        String response4 = scf.AssignmentApply(supplyAddress1, supplyID1, PIN, proof, tokenType, list1, newSubType, supplyAddress2);
        assertThat(response4, containsString("400"));
        assertThat(response4, containsString("error"));
        assertThat(response4, containsString("Err:wvm invoke err"));
    }

    /**
     * 资产转让申请（输入转让额度为0）
     */
    @Test
    public void Test0012_Assignmentapply() throws Exception {
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
        String response3 = scf.IssuingConfirm(PlatformAddress, coreCompanyKeyID, tokenType, PIN, comments);
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
        List<Map> list1 = UtilsClassScf.Assignment("0", "0", list);
        String response4 = scf.AssignmentApply(supplyAddress1, supplyID1, PIN, proof, tokenType, list1, newSubType, supplyAddress2);
        assertThat(response4, containsString("400"));
        assertThat(response4, containsString("error"));
        assertThat(response4, containsString("Incoming value should not be 0"));
    }

    /**
     * 资产转让签收
     */
    @Test
    public void Test0013_Assignmentapply() throws Exception {
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
        String response3 = scf.IssuingConfirm(PlatformAddress, coreCompanyKeyID, tokenType, PIN, comments);
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
        //输入错误平台方合约地址
        String response5 = scf.AssignmentConfirm("274532de3b53ccce779132fa9b5a2a10dcad4e2b230e5625605a2d7c0fdebce2", supplyID1, PIN, challenge, tokenType, comments);
        assertThat(response5, containsString("400"));
        assertThat(response5, containsString("error"));
        assertThat(response5, containsString("Err:empty stack"));
        //输入空平台方合约地址
        String response6 = scf.AssignmentConfirm("", supplyID1, PIN, challenge, tokenType, comments);
        assertThat(response6, containsString("400"));
        assertThat(response6, containsString("error"));
        assertThat(response6, containsString("Key: 'PlatFormAddress' Error:Field validation for 'PlatFormAddress' failed on the 'required' tag"));
        //输入错误供应商id
        String response7 = scf.AssignmentConfirm(PlatformAddress, "c02jbhjsnk7pq9fsojvg", PIN, challenge, tokenType, comments);
        assertThat(response7, containsString("400"));
        assertThat(response7, containsString("error"));
        assertThat(response7, containsString("pin码与密钥不匹配,或请稍后再试"));
        //输入空供应商id
        String response8 = scf.AssignmentConfirm(PlatformAddress, "", PIN, challenge, tokenType, comments);
        assertThat(response8, containsString("400"));
        assertThat(response8, containsString("error"));
        assertThat(response8, containsString("Key: 'KeyID' Error:Field validation for 'KeyID' failed on the 'len' tag"));
        //输入错误pin码
        String response9 = scf.AssignmentConfirm(PlatformAddress, supplyID1, "4567", challenge, tokenType, comments);
        assertThat(response9, containsString("400"));
        assertThat(response9, containsString("error"));
        assertThat(response9, containsString("pin码与密钥不匹配,或请稍后再试"));
        //输入空pin码
        String response10 = scf.AssignmentConfirm(PlatformAddress, supplyID1, "", challenge, tokenType, comments);
        assertThat(response10, containsString("400"));
        assertThat(response10, containsString("error"));
        assertThat(response10, containsString("Key: 'PIN' Error:Field validation for 'PIN' failed on the 'required' tag"));
        //输入错误challenge
        String response11 = scf.AssignmentConfirm(PlatformAddress, supplyID1, PIN, "5566", tokenType, comments);
        assertThat(response11, containsString("400"));
        assertThat(response11, containsString("error"));
        assertThat(response11, containsString("unexpected end of JSON input"));
        //输入空challenge
        String response12 = scf.AssignmentConfirm(PlatformAddress, supplyID1, PIN, "", tokenType, comments);
        assertThat(response12, containsString("400"));
        assertThat(response12, containsString("error"));
        assertThat(response12, containsString("Key: 'Challenge' Error:Field validation for 'Challenge' failed on the 'required' tag"));
        //输入错误tokentype
        String response13 = scf.AssignmentConfirm(PlatformAddress, supplyID1, PIN, challenge, "9898", comments);
        assertThat(response13, containsString("400"));
        assertThat(response13, containsString("error"));
        assertThat(response13, containsString("tokentype未发行!"));
        //输入空tokentype
        String response14 = scf.AssignmentConfirm(PlatformAddress, supplyID1, PIN, challenge, "", comments);
        assertThat(response14, containsString("400"));
        assertThat(response14, containsString("error"));
        assertThat(response14, containsString("Key: 'TokenType' Error:Field validation for 'TokenType' failed on the 'required' tag"));
        //输入错误comments
//        String response15 = scf.AssignmentConfirm(PlatformAddress, supplyID1, PIN, challenge, tokenType, "asdasdccdsdgweewr");
//        assertThat(response15, containsString("503"));
//        assertThat(response15, containsString("error"));
//        assertThat(response15, containsString("timeout!"));
        //输入空comments
//        String response16 = scf.AssignmentConfirm(PlatformAddress, supplyID1, PIN, challenge, tokenType, "");
//        assertThat(response16, containsString("200"));
//        assertThat(response16, containsString("success"));
//        assertThat(response16, containsString("data"));
    }

    /**
     * 资产转让拒收
     */
    @Test
    public void Test0014_Assignmentapply() throws Exception {
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
        String response3 = scf.IssuingConfirm(PlatformAddress, coreCompanyKeyID, tokenType, PIN, comments);
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
        //资产转让拒收
        //输入错误challenge
        String response6 = scf.AssignmentReject("123aaa", tokenType);
        assertThat(response6, containsString("400"));
        assertThat(response6, containsString("error"));
        assertThat(response6, containsString("Err:wvm invoke err"));
        //输入空challenge
        String response7 = scf.AssignmentReject("", tokenType);
        assertThat(response7, containsString("400"));
        assertThat(response7, containsString("error"));
        assertThat(response7, containsString("Key: 'Challenge' Error:Field validation for 'Challenge' failed on the 'min' tag"));
        //输入错误tokenType
        String response8 = scf.AssignmentReject(challenge, "2161651");
        assertThat(response8, containsString("400"));
        assertThat(response8, containsString("error"));
        assertThat(response8, containsString("tokentype未发行!"));
        //输入空tokenType
        String response9 = scf.AssignmentReject(challenge, "");
        assertThat(response9, containsString("400"));
        assertThat(response9, containsString("error"));
        assertThat(response9, containsString("Key: 'TokenType' Error:Field validation for 'TokenType' failed on the 'required' tag"));
    }

    /**
     * 融资申请（参数错误）
     */
     @Test
     public void Test0015_FinacingApply() throws Exception {
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
         String response3 = scf.IssuingConfirm(PlatformAddress, coreCompanyKeyID, tokenType, PIN, comments);
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
         //输入错误供应商地址
         String response4 = scf.FinacingApply("", supplyID1, PIN, proof, tokenType, rzamount, subType, newFromSubType, newToSubType, supplyAddress2);
         assertThat(response4, containsString("400"));
         assertThat(response4, containsString("error"));
         assertThat(response4, containsString("Key: 'FromAddress' Error:Field validation for 'FromAddress' failed on the 'min' tag"));
         //输入错误供应商地址
         String response5 = scf.FinacingApply("SnwNAEuLDPUZRqkuDbVmbNugisspXgdx4wfBgoiL6F1ArAWVhEn", supplyID1, PIN, proof, tokenType, rzamount, subType, newFromSubType, newToSubType, supplyAddress2);
         assertThat(response5, containsString("400"));
         assertThat(response5, containsString("error"));
         assertThat(response5, containsString("Err:wvm invoke err"));
         //输入错误供应商id
//         String response6 = scf.FinacingApply(supplyAddress1, "c02jbhjsnk7pq9fsojvg", PIN, proof, tokenType, rzamount, subType, newFromSubType, newToSubType, supplyAddress2);
//         assertThat(response6, containsString("200"));
//         assertThat(response6, containsString("success"));
//         assertThat(response6, containsString("data"));
         //输入空供应商id
         String response7 = scf.FinacingApply(supplyAddress1, "", PIN, proof, tokenType, rzamount, subType, newFromSubType, newToSubType, supplyAddress2);
         assertThat(response7, containsString("400"));
         assertThat(response7, containsString("error"));
         assertThat(response7, containsString("Key: 'KeyID' Error:Field validation for 'KeyID' failed on the 'len' tag"));
         //输入错误pin码
//         String response8 = scf.FinacingApply(supplyAddress1, supplyID1, "5556", proof, tokenType, rzamount, subType, newFromSubType, newToSubType, supplyAddress2);
//         assertThat(response8, containsString("200"));
//         assertThat(response8, containsString("success"));
//         assertThat(response8, containsString("data"));
         //输入空pin码
         String response9 = scf.FinacingApply(supplyAddress1, supplyID1, "", proof, tokenType, rzamount, subType, newFromSubType, newToSubType, supplyAddress2);
         assertThat(response9, containsString("400"));
         assertThat(response9, containsString("error"));
         assertThat(response9, containsString("Key: 'PIN' Error:Field validation for 'PIN' failed on the 'min' tag"));
         //输入错误tokentype
         String response10 = scf.FinacingApply(supplyAddress1, supplyID1, PIN, proof, "615516515", rzamount, subType, newFromSubType, newToSubType, supplyAddress2);
         assertThat(response10, containsString("400"));
         assertThat(response10, containsString("error"));
         assertThat(response10, containsString("tokentype未发行!"));
         //输入空tokentype
         String response11 = scf.FinacingApply(supplyAddress1, supplyID1, PIN, proof, "", rzamount, subType, newFromSubType, newToSubType, supplyAddress2);
         assertThat(response11, containsString("400"));
         assertThat(response11, containsString("error"));
         assertThat(response11, containsString("tokentype未发行!"));
         //融资金额大于小数点后6位
//         String response12 = scf.FinacingApply(supplyAddress1, supplyID1, PIN, proof, tokenType, "10.123456789", subType, newFromSubType, newToSubType, supplyAddress2);
//         assertThat(response12, containsString("400"));
//         assertThat(response12, containsString("error"));
//         assertThat(response12, containsString("Failed! Err:wvm invoke err"));
         //融资金额为0
         String response13 = scf.FinacingApply(supplyAddress1, supplyID1, PIN, proof, tokenType, "0", subType, newFromSubType, newToSubType, supplyAddress2);
         assertThat(response13, containsString("400"));
         assertThat(response13, containsString("error"));
         assertThat(response13, containsString("Incoming value should not be 0"));
         //融资金额为负数
         String response14 = scf.FinacingApply(supplyAddress1, supplyID1, PIN, proof, tokenType, "-1", subType, newFromSubType, newToSubType, supplyAddress2);
         assertThat(response14, containsString("400"));
         assertThat(response14, containsString("error"));
         assertThat(response14, containsString("strconv.ParseUint: parsing \\\"-1\\\": invalid syntax"));
         //融资金额过大
         String response15 = scf.FinacingApply(supplyAddress1, supplyID1, PIN, proof, tokenType, "1000000", subType, newFromSubType, newToSubType, supplyAddress2);
         assertThat(response15, containsString("400"));
         assertThat(response15, containsString("error"));
         assertThat(response15, containsString("Failed! Err:wvm invoke err"));
         //输入错误toaddress
         String response16 = scf.FinacingApply(supplyAddress1, supplyID1, PIN, proof, tokenType, rzamount, subType, newFromSubType, newToSubType, "SoV1KWJqSr4TWXZd1kpnLG19WU95LbjMMQv7Dygph9nkHvRCNvE");
         assertThat(response16, containsString("200"));
         assertThat(response16, containsString("success"));
         assertThat(response16, containsString("data"));
         //输入空toaddress
         String response17 = scf.FinacingApply(supplyAddress1, supplyID1, PIN, proof, tokenType, rzamount, subType, newFromSubType, newToSubType, "");
         assertThat(response17, containsString("400"));
         assertThat(response17, containsString("error"));
         assertThat(response17, containsString("Key: 'ToAddress' Error:Field validation for 'ToAddress' failed on the 'required' tag"));
     }

    /**
     * 融资申请试算（错误参数）
     */
    @Test
    public void Test0016_FinacingApply() throws Exception {
        String timeLimit = "10";
        String amount = "1";
        //输入错误资金方合约地址
        String response1 = scf.FinacingTest("d08a969370898876d08b4314cba416ec26fa6ce1857fd4f6335c68eaf0872dca", amount, timeLimit);
        assertThat(response1, containsString("400"));
        assertThat(response1, containsString("error"));
        assertThat(response1, containsString("rpc error: code = Unknown desc = Smart contract does not exist"));
        //输入空资金方合约地址
        String response2 = scf.FinacingTest("", amount, timeLimit);
        assertThat(response2, containsString("400"));
        assertThat(response2, containsString("error"));
        assertThat(response2, containsString("Key: 'ContractAddress' Error:Field validation for 'ContractAddress' failed on the 'min' tag"));
        //输入amount为空
        String response3 = scf.FinacingTest(ZJFAddress, "", timeLimit);
        assertThat(response3, containsString("400"));
        assertThat(response3, containsString("error"));
        assertThat(response3, containsString("Key: 'Amount' Error:Field validation for 'Amount' failed on the 'required' tag"));
        //输入amount为负值
        String response4 = scf.FinacingTest(ZJFAddress, "-1", timeLimit);
        assertThat(response4, containsString("200"));
        assertThat(response4, containsString("success"));
        assertThat(response4, containsString("data"));
        //输入amount为空
        String response5 = scf.FinacingTest(ZJFAddress, "0", timeLimit);
        assertThat(response5, containsString("400"));
        assertThat(response5, containsString("error"));
        assertThat(response5, containsString("rpc error: code = Unknown desc = overflow double"));
        //输入timeLimit为0
        String response6 = scf.FinacingTest(ZJFAddress, amount, "0");
        assertThat(response6, containsString("400"));
        assertThat(response6, containsString("error"));
        assertThat(response6, containsString("rpc error: code = Unknown desc = overflow double"));
        //输入timeLimit为负数
        String response7 = scf.FinacingTest(ZJFAddress, amount, "-1");
        assertThat(response7, containsString("200"));
        assertThat(response7, containsString("success"));
        assertThat(response7, containsString("data"));
        //输入timeLimit为空
        String response8 = scf.FinacingTest(ZJFAddress, amount, "");
        assertThat(response8, containsString("400"));
        assertThat(response8, containsString("error"));
        assertThat(response8, containsString("Key: 'TimeLimit' Error:Field validation for 'TimeLimit' failed on the 'required' tag"));
    }

    /**
     * 融资申请反馈
     */
    @Test
    public void Test0017_FinacingApply() throws Exception {
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
        String response3 = scf.IssuingConfirm(PlatformAddress, coreCompanyKeyID, tokenType, PIN, comments);
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
        //输入错误资金方合约
        String response6 = scf.FinacingFeedback("d08a969370898876d08b4314cba416ec26fa6ce1857fd4f6335c68eaf0872dca", applyNo, state, comments, msg);
        assertThat(response6, containsString("400"));
        assertThat(response6, containsString("error"));
        assertThat(response6, containsString("rpc error: code = InvalidArgument desc = Smart contract does not exist"));
        //输入空资金方合约
        String response7 = scf.FinacingFeedback("", applyNo, state, comments, msg);
        assertThat(response7, containsString("400"));
        assertThat(response7, containsString("error"));
        assertThat(response7, containsString("Key: 'ContractAddress' Error:Field validation for 'ContractAddress' failed on the 'min' tag"));
        //输入错误还款状态：为空
        String response8 = scf.FinacingFeedback(ZJFAddress, applyNo, "", comments, msg);
        assertThat(response8, containsString("400"));
        assertThat(response8, containsString("error"));
        assertThat(response8, containsString("Key: 'State' Error:Field validation for 'State' failed on the 'min' tag"));
        //输入错误还款状态：为负值
        String response9 = scf.FinacingFeedback(ZJFAddress, applyNo, "-1", comments, msg);
        assertThat(response9, containsString("200"));
        assertThat(response9, containsString("success"));
        assertThat(response9, containsString("data"));
        //输入错误comments
        String response10 = scf.FinacingFeedback(ZJFAddress, applyNo, state, "SAJDNAJKLNDAJNCOA", msg);
        assertThat(response10, containsString("200"));
        assertThat(response10, containsString("success"));
        assertThat(response10, containsString("data"));
        //输入空comments
        String response11 = scf.FinacingFeedback(ZJFAddress, applyNo, state, "", msg);
        assertThat(response11, containsString("400"));
        assertThat(response11, containsString("error"));
        assertThat(response11, containsString("Key: 'Comments' Error:Field validation for 'Comments' failed on the 'required' tag"));
    }

    /**
     * 融资签收
     */
     @Test
     public void Test018_FinacingApply() throws Exception {
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
         String response3 = scf.IssuingConfirm(PlatformAddress, coreCompanyKeyID, tokenType, PIN, comments);
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
         //输入错误平台方合约
         String response7 = scf.FinacingConfirm("274532de3b53ccce779132fa9b5a2a10dcad4e2b230e5625605a2d7c0fdebce2", applyNo, ZJFAddress, supplyID1, companyID1, PIN, tokenType, supplyAddress2, challenge, comments);
         assertThat(response7, containsString("400"));
         assertThat(response7, containsString("error"));
         assertThat(response7, containsString("Failed! Err:empty stack"));
         //输入空平台方合约
         String response8 = scf.FinacingConfirm("", applyNo, ZJFAddress, supplyID1, companyID1, PIN, tokenType, supplyAddress2, challenge, comments);
         assertThat(response8, containsString("400"));
         assertThat(response8, containsString("error"));
         assertThat(response8, containsString("Key: 'PlatFormAddress' Error:Field validation for 'PlatFormAddress' failed on the 'required' tag"));
         //输入错误融资编号
         String response9 = scf.FinacingConfirm(PlatformAddress, "1516151", ZJFAddress, supplyID1, companyID1, PIN, tokenType, supplyAddress2, challenge, comments);
         assertThat(response9, containsString("503"));
         assertThat(response9, containsString("error"));
         assertThat(response9, containsString("timeout!"));
         //输入空融资编号
         String response10 = scf.FinacingConfirm(PlatformAddress, "", ZJFAddress, supplyID1, companyID1, PIN, tokenType, supplyAddress2, challenge, comments);
         assertThat(response10, containsString("400"));
         assertThat(response10, containsString("error"));
         assertThat(response10, containsString("Key: 'ApplyNo' Error:Field validation for 'ApplyNo' failed on the 'required' tag"));
         //输入错误资金方合约地址
//         String response11 = scf.FinacingConfirm(PlatformAddress, applyNo, "d08a969370898876d08b4314cba416ec26fa6ce185335c68e1123af213172dca", supplyID1, companyID1, PIN, tokenType, supplyAddress2, challenge, comments);
//         assertThat(response11, containsString("400"));
//         assertThat(response11, containsString("error"));
//         assertThat(response11, containsString("Failed! Err:empty stack"));
         //输出空资金方合约地址
         String response12 = scf.FinacingConfirm(PlatformAddress, applyNo, "", supplyID1, companyID1, PIN, tokenType, supplyAddress2, challenge, comments);
         assertThat(response12, containsString("400"));
         assertThat(response12, containsString("error"));
         assertThat(response12, containsString("Key: 'ContractAddress' Error:Field validation for 'ContractAddress' failed on the 'required' tag"));
         //输入错误资产接收方ID
         String response13 = scf.FinacingConfirm(PlatformAddress, applyNo, ZJFAddress, "c02jbhjsnk7pq9fsojvg", companyID1, PIN, tokenType, supplyAddress2, challenge, comments);
         assertThat(response13, containsString("400"));
         assertThat(response13, containsString("error"));
         assertThat(response13, containsString("pin码与密钥不匹配,或请稍后再试"));
         //输入空资产接收方ID
         String response14 = scf.FinacingConfirm(PlatformAddress, applyNo, ZJFAddress, "", companyID1, PIN, tokenType, supplyAddress2, challenge, comments);
         assertThat(response14, containsString("400"));
         assertThat(response14, containsString("error"));
         assertThat(response14, containsString("Key: 'KeyID' Error:Field validation for 'KeyID' failed on the 'len' tag"));
         //输入错误资金方id
//         String response15 = scf.FinacingConfirm(PlatformAddress, applyNo, ZJFAddress, supplyID1, "1238", PIN, tokenType, supplyAddress2, challenge, comments);
//         assertThat(response15, containsString("200"));
//         assertThat(response15, containsString("success"));
//         assertThat(response15, containsString("data"));
         //输入空资金方id
//         String response16 = scf.FinacingConfirm(PlatformAddress, applyNo, ZJFAddress, supplyID1, "", PIN, tokenType, supplyAddress2, challenge, comments);
//         assertThat(response16, containsString("400"));
//         assertThat(response16, containsString("error"));
//         assertThat(response16, containsString("has been spent"));
         //输入错误pin码
         String response17 = scf.FinacingConfirm(PlatformAddress, applyNo, ZJFAddress, supplyID1, companyID1, "65465", tokenType, supplyAddress2, challenge, comments);
         assertThat(response17, containsString("400"));
         assertThat(response17, containsString("error"));
         assertThat(response17, containsString("pin码与密钥不匹配,或请稍后再试"));
         //输入空pin码
         String response18 = scf.FinacingConfirm(PlatformAddress, applyNo, ZJFAddress, supplyID1, companyID1, "", tokenType, supplyAddress2, challenge, comments);
         assertThat(response18, containsString("400"));
         assertThat(response18, containsString("error"));
         assertThat(response18, containsString("Key: 'PIN' Error:Field validation for 'PIN' failed on the 'required' tag"));
         //输入错误接收方地址
//         String response19 = scf.FinacingConfirm(PlatformAddress, applyNo, ZJFAddress, supplyID1, companyID1, PIN, tokenType, "08a969370898876d08b4314cba416ec26fa6ce1857fd4f6335c68eaf", challenge, comments);
//         assertThat(response19, containsString("200"));
//         assertThat(response19, containsString("success"));
//         assertThat(response19, containsString("data"));
         //输入空接收方地址
//         String response20 = scf.FinacingConfirm(PlatformAddress, applyNo, ZJFAddress, supplyID1, companyID1, PIN, tokenType, "", challenge, comments);
//         assertThat(response20, containsString("400"));
//         assertThat(response20, containsString("error"));
//         assertThat(response20, containsString("invalid character 's' looking for beginning of value"));
         //输入错误challenge
         String response21 = scf.FinacingConfirm(PlatformAddress, applyNo, ZJFAddress, supplyID1, companyID1, PIN, tokenType, supplyAddress2, "qweqcas", comments);
         assertThat(response21, containsString("400"));
         assertThat(response21, containsString("error"));
         assertThat(response21, containsString("unexpected end of JSON input"));
         //输入空challenge
         String response22 = scf.FinacingConfirm(PlatformAddress, applyNo, ZJFAddress, supplyID1, companyID1, PIN, tokenType, supplyAddress2, "", comments);
         assertThat(response22, containsString("400"));
         assertThat(response22, containsString("error"));
         assertThat(response22, containsString("Key: 'Challenge' Error:Field validation for 'Challenge' failed on the 'required' tag"));
         //输入错误comments
//         String response23 = scf.FinacingConfirm(PlatformAddress, applyNo, ZJFAddress, supplyID1, companyID1, PIN, tokenType, supplyAddress2, challenge, "asdeadfascasdsafasfvdvdfssgv");
//         assertThat(response23, containsString("200"));
//         assertThat(response23, containsString("success"));
//         assertThat(response23, containsString("data"));
         //输入空comments
//         String response24 = scf.FinacingConfirm(PlatformAddress, applyNo, ZJFAddress, supplyID1, companyID1, PIN, tokenType, supplyAddress2, challenge, "");
//         assertThat(response24, containsString("200"));
//         assertThat(response24, containsString("success"));
//         assertThat(response24, containsString("data"));

     }

    /**
     * 融资取消
     */
    @Test
    public void Test0019_FinacingCancel() throws Exception {
        int levelLimit = 5;
        String amount = "1";
        String subType = "0";
        String response = kms.genRandom(size);
        String proof = "1234567";
        String challenge = "1234567";
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
        String response3 = scf.IssuingConfirm(PlatformAddress, coreCompanyKeyID, tokenType, PIN, comments);
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
        //输入错误challenge
        String response7 = scf.FinacingCancel("123sss", tokenType);
        assertThat(response7, containsString("400"));
        assertThat(response7, containsString("error"));
        assertThat(response7, containsString("wvm invoke err"));
        //输入空challenge
        String response8 = scf.FinacingCancel("", tokenType);
        assertThat(response8, containsString("400"));
        assertThat(response8, containsString("error"));
        assertThat(response8, containsString("Key: 'Challenge' Error:Field validation for 'Challenge' failed on the 'min' tag"));
        //输入错误tokentype
        String response9 = scf.FinacingCancel(challenge, "sadasdasc");
        assertThat(response9, containsString("400"));
        assertThat(response9, containsString("error"));
        assertThat(response9, containsString("tokentype未发行!"));
        //输入空tokentype
        String response10 = scf.FinacingCancel(challenge, "");
        assertThat(response10, containsString("400"));
        assertThat(response10, containsString("error"));
        assertThat(response10, containsString("Key: 'TokenType' Error:Field validation for 'TokenType' failed on the 'required' tag"));
    }

    /**
     * 兑付申请
     */
    @Test
    public void Test0020_PayingConfirm() throws Exception {
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
        String response3 = scf.IssuingConfirm(PlatformAddress, coreCompanyKeyID, tokenType, PIN, comments);
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
        //输入错误tokenType
        String response4 = scf.PayingApply("123ccasd", companyID1, comments);
        assertThat(response4, containsString("400"));
        assertThat(response4, containsString("error"));
        assertThat(response4, containsString("wvm invoke err"));
        //输入空tokentype
        String response5 = scf.PayingApply("", companyID1, comments);
        assertThat(response5, containsString("400"));
        assertThat(response5, containsString("error"));
        assertThat(response5, containsString("Key: 'TokenType' Error:Field validation for 'TokenType' failed on the 'min' tag"));
        //输入错误companyID1
//        String response6 = scf.PayingApply(tokenType, "0010", comments);
//        assertThat(response6, containsString("200"));
//        assertThat(response6, containsString("success"));
//        assertThat(response6, containsString("data"));
        //输入空companyID1
//        String response7 = scf.PayingApply(tokenType, "", comments);
//        assertThat(response7, containsString("200"));
//        assertThat(response7, containsString("success"));
//        assertThat(response7, containsString("data"));
        //输入错误comments
//        String response8 = scf.PayingApply(tokenType, companyID1, "dasdacajoncasocnoan");
//        assertThat(response8, containsString("200"));
//        assertThat(response8, containsString("success"));
//        assertThat(response8, containsString("data"));
        //输入空comments
        String response9 = scf.PayingApply(tokenType, companyID1, "");
        assertThat(response9, containsString("400"));
        assertThat(response9, containsString("error"));
        assertThat(response9, containsString("Key: 'Comments' Error:Field validation for 'Comments' failed on the 'required' tag"));

    }

    /**
     * 兑付反馈
     */
    @Test
    public void Test0021_PayingConfirm() throws Exception {
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
        String response3 = scf.IssuingConfirm(PlatformAddress, coreCompanyKeyID, tokenType, PIN, comments);
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
        //输入错误清分机构合约地址
        String response6 = scf.PayingFeedback("ajnfnasnpcnapnp", tokenType, state, comments);
        assertThat(response6, containsString("400"));
        assertThat(response6, containsString("error"));
        assertThat(response6, containsString("rpc error: code = InvalidArgument desc = Smart contract does not exist"));
        //输入空清分机构合约地址
        String response7 = scf.PayingFeedback("", tokenType, state, comments);
        assertThat(response7, containsString("400"));
        assertThat(response7, containsString("error"));
        assertThat(response7, containsString("Key: 'ContractAddress' Error:Field validation for 'ContractAddress' failed on the 'min' tag"));
        //输入错误tokentype
        String response8 = scf.PayingFeedback(QFJGAddress, "asdadw", state, comments);
        assertThat(response8, containsString("200"));
        assertThat(response8, containsString("success"));
        assertThat(response8, containsString("data"));
        //输入空tokentype
        String response9 = scf.PayingFeedback(QFJGAddress, "", state, comments);
        assertThat(response9, containsString("400"));
        assertThat(response9, containsString("error"));
        assertThat(response9, containsString("Key: 'TokenType' Error:Field validation for 'TokenType' failed on the 'min' tag"));
        //输入负state
//        String response10 = scf.PayingFeedback(QFJGAddress, tokenType, "-1", comments);
//        assertThat(response10, containsString("200"));
//        assertThat(response10, containsString("success"));
//        assertThat(response10, containsString("data"));
        //输入空state
        String response11 = scf.PayingFeedback(QFJGAddress, tokenType, "", comments);
        assertThat(response11, containsString("400"));
        assertThat(response11, containsString("error"));
        assertThat(response11, containsString("Key: 'State' Error:Field validation for 'State' failed on the 'min' tag"));
        //输入错误comments
//        String response12 = scf.PayingFeedback(QFJGAddress, tokenType, state, "ancijnasicnasijnciabsi");
//        assertThat(response12, containsString("200"));
//        assertThat(response12, containsString("success"));
//        assertThat(response12, containsString("data"));
        //输入空comments
        String response13 = scf.PayingFeedback(QFJGAddress, tokenType, state, "");
        assertThat(response13, containsString("400"));
        assertThat(response13, containsString("error"));
        assertThat(response13, containsString("Key: 'Comments' Error:Field validation for 'Comments' failed on the 'required' tag"));
    }

    /**
     * 兑付确认(错误+空字段）
     */
    @Test
    public void Test0022_PayingConfirm() throws Exception {
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
        String response3 = scf.IssuingConfirm(PlatformAddress, coreCompanyKeyID, tokenType, PIN, comments);
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
        //输入错误平台方合约地址
        List<Map> list = new ArrayList<>(10);
        List<Map> list1 = UtilsClassScf.paying(supplyAddress1, supplyID1, "0", "100", list);
        String response9 = scf.PayingConfirm("asdacascasfcdgsagasfgaca", QFJGAddress, companyID1, list1, platformKeyID, platformPIN, tokenType, comments);
        assertThat(response9, containsString("400"));
        assertThat(response9, containsString("error"));
        assertThat(response9, containsString("Failed! Err:empty stack"));
        //输入空平台方合约地址
        String response10 = scf.PayingConfirm("", QFJGAddress, companyID1, list1, platformKeyID, platformPIN, tokenType, comments);
        assertThat(response10, containsString("400"));
        assertThat(response10, containsString("error"));
        assertThat(response10, containsString("Key: 'PlatFormAddress' Error:Field validation for 'PlatFormAddress' failed on the 'required' tag"));
        //输入错误清分机构地址
        String response11 = scf.PayingConfirm(PlatformAddress, "aicbnasjnfpiancpajinpa", companyID1, list1, platformKeyID, platformPIN, tokenType, comments);
        assertThat(response11, containsString("503"));
        assertThat(response11, containsString("error"));
        assertThat(response11, containsString("timeout!"));
        //输入空清分机构地址
        String response12 = scf.PayingConfirm(PlatformAddress, "", companyID1, list1, platformKeyID, platformPIN, tokenType, comments);
        assertThat(response12, containsString("400"));
        assertThat(response12, containsString("error"));
        assertThat(response12, containsString("Key: 'ContractAddress' Error:Field validation for 'ContractAddress' failed on the 'required' tag"));
        //输入错误资金方ID
//        String response13 = scf.PayingConfirm(PlatformAddress, QFJGAddress, "008", list1, platformKeyID, platformPIN, tokenType, comments);
//        assertThat(response13, containsString("503"));
//        assertThat(response13, containsString("error"));
//        assertThat(response13, containsString("out idx[0] has been spen"));
        //输入空资金方ID
        String response14 = scf.PayingConfirm(PlatformAddress, QFJGAddress, "", list1, platformKeyID, platformPIN, tokenType, comments);
        assertThat(response14, containsString("400"));
        assertThat(response14, containsString("error"));
        assertThat(response14, containsString("Key: 'CompanyID' Error:Field validation for 'CompanyID' failed on the 'required' tag"));
        //输入错误平台方id
        String response15 = scf.PayingConfirm(PlatformAddress, QFJGAddress, companyID1, list1, "c02jbhjsnk7pq9fsojvg", platformPIN, tokenType, comments);
        assertThat(response15, containsString("400"));
        assertThat(response15, containsString("error"));
        assertThat(response15, containsString("pin码与密钥不匹配,或请稍后再试"));
        //输入空平台方id
        String response16 = scf.PayingConfirm(PlatformAddress, QFJGAddress, companyID1, list1, "", platformPIN, tokenType, comments);
        assertThat(response16, containsString("400"));
        assertThat(response16, containsString("error"));
        assertThat(response16, containsString("Key: 'PlatformKeyID' Error:Field validation for 'PlatformKeyID' failed on the 'required' tag"));
        //输入错误平台方pin码
        String response17 = scf.PayingConfirm(PlatformAddress, QFJGAddress, companyID1, list1, platformKeyID, "dacasasd", tokenType, comments);
        assertThat(response17, containsString("400"));
        assertThat(response17, containsString("error"));
        assertThat(response17, containsString("pin码与密钥不匹配,或请稍后再试"));
        //输入空平台方pin码
        String response18 = scf.PayingConfirm(PlatformAddress, QFJGAddress, companyID1, list1, platformKeyID, "", tokenType, comments);
        assertThat(response18, containsString("400"));
        assertThat(response18, containsString("error"));
        assertThat(response18, containsString("Key: 'PIN' Error:Field validation for 'PIN' failed on the 'min' tag"));
        //输入错误tokentype
        String response19 = scf.PayingConfirm(PlatformAddress, QFJGAddress, companyID1, list1, platformKeyID, platformPIN, "asccacadfdd", comments);
        assertThat(response19, containsString("400"));
        assertThat(response19, containsString("error"));
        assertThat(response19, containsString("销毁申请失败： Insufficient Balance!"));
        //输入空tokentype
        String response20 = scf.PayingConfirm(PlatformAddress, QFJGAddress, companyID1, list1, platformKeyID, platformPIN, "", comments);
        assertThat(response20, containsString("400"));
        assertThat(response20, containsString("error"));
        assertThat(response20, containsString("Key: 'TokenType' Error:Field validation for 'TokenType' failed on the 'min' tag"));
        //输入错误comments
//        String response21 = scf.PayingConfirm(PlatformAddress, QFJGAddress, companyID1, list1, platformKeyID, platformPIN, tokenType, "sacancnijasniasninaisfnai");
//        assertThat(response21, containsString("503"));
//        assertThat(response21, containsString("error"));
//        assertThat(response21, containsString("has been spent"));
        //输入空comments
        String response22 = scf.PayingConfirm(PlatformAddress, QFJGAddress, companyID1, list1, platformKeyID, platformPIN, tokenType, "");
        assertThat(response22, containsString("400"));
        assertThat(response22, containsString("error"));
        assertThat(response22, containsString("Key: 'Comments' Error:Field validation for 'Comments' failed on the 'required' tag"));

    }

    /**
     * 兑付确认（兑付供应商地址错误、空）
     */
    @Test
    public void Test0023_PayingConfirm() throws Exception {
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
        String response3 = scf.IssuingConfirm(PlatformAddress, coreCompanyKeyID, tokenType, PIN, comments);
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
        //输入错误供应商地址
        List<Map> list = new ArrayList<>(10);
        List<Map> list1 = UtilsClassScf.paying("adasnjanijanasmd", supplyID1, "0", "100", list);
        String response9 = scf.PayingConfirm(PlatformAddress, QFJGAddress, companyID1, list1, platformKeyID, platformPIN, tokenType, comments);
        assertThat(response9, containsString("400"));
        assertThat(response9, containsString("error"));
        assertThat(response9, containsString("销毁申请失败： Insufficient Balance!"));
    }
    @Test
    public void Test0024_PayingConfirm() throws Exception {
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
        String response3 = scf.IssuingConfirm(PlatformAddress, coreCompanyKeyID, tokenType, PIN, comments);
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
        //输入错误供应商地址
        List<Map> list = new ArrayList<>(10);
        List<Map> list1 = UtilsClassScf.paying("", supplyID1, "0", "100", list);
        String response9 = scf.PayingConfirm(PlatformAddress, QFJGAddress, companyID1, list1, platformKeyID, platformPIN, tokenType, comments);
        assertThat(response9, containsString("400"));
        assertThat(response9, containsString("error"));
        assertThat(response9, containsString("销毁申请失败： Insufficient Balance!"));
    }


    /**
     * 兑付确认（兑付供应商id错误、空）
     */
    @Test
    public void Test0025_PayingConfirm() throws Exception {
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
        String response3 = scf.IssuingConfirm(PlatformAddress, coreCompanyKeyID, tokenType, PIN, comments);
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
        List<Map> list1 = UtilsClassScf.paying(supplyAddress1, "sdasdwaceffg", "0", "100", list);
        String response9 = scf.PayingConfirm(PlatformAddress, QFJGAddress, companyID1, list1, platformKeyID, platformPIN, tokenType, comments);
        assertThat(response9, containsString("400"));
        assertThat(response9, containsString("error"));
        assertThat(response9, containsString("密钥不存在，请核对密钥ID"));
    }

    @Test
    public void Test0026_PayingConfirm() throws Exception {
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
        String response3 = scf.IssuingConfirm(PlatformAddress, coreCompanyKeyID, tokenType, PIN, comments);
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
        List<Map> list1 = UtilsClassScf.paying(supplyAddress1, "", "0", "100", list);
        String response9 = scf.PayingConfirm(PlatformAddress, QFJGAddress, companyID1, list1, platformKeyID, platformPIN, tokenType, comments);
        assertThat(response9, containsString("400"));
        assertThat(response9, containsString("error"));
        assertThat(response9, containsString("KeyID和pubkey不能同时为空!"));
    }

    /**
     *兑付确认（授信额度超大、为空、负值）
     */
    @Test
    public void Test0027_PayingConfirm() throws Exception {
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
        String response3 = scf.IssuingConfirm(PlatformAddress, coreCompanyKeyID, tokenType, PIN, comments);
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
        //授信额度为空
        List<Map> list = new ArrayList<>(10);
        List<Map> list1 = UtilsClassScf.paying(supplyAddress1, supplyID1, "0", "", list);
        String response9 = scf.PayingConfirm(PlatformAddress, QFJGAddress, companyID1, list1, platformKeyID, platformPIN, tokenType, comments);
        assertThat(response9, containsString("200"));
        assertThat(response9, containsString("success"));
        assertThat(response9, containsString("data"));
    }

    @Test
    public void Test0028_PayingConfirm() throws Exception {
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
        String response3 = scf.IssuingConfirm(PlatformAddress, coreCompanyKeyID, tokenType, PIN, comments);
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
        //授信额度为超大
        List<Map> list = new ArrayList<>(10);
        List<Map> list1 = UtilsClassScf.paying(supplyAddress1, supplyID1, "0", "1000000", list);
        String response9 = scf.PayingConfirm(PlatformAddress, QFJGAddress, companyID1, list1, platformKeyID, platformPIN, tokenType, comments);
        assertThat(response9, containsString("400"));
        assertThat(response9, containsString("error"));
        assertThat(response9, containsString("销毁申请失败： Insufficient Balance!"));
    }

    @Test
    public void Test0029_PayingConfirm() throws Exception {
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
        String response3 = scf.IssuingConfirm(PlatformAddress, coreCompanyKeyID, tokenType, PIN, comments);
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
        //授信额度为负数
        List<Map> list = new ArrayList<>(10);
        List<Map> list1 = UtilsClassScf.paying(supplyAddress1, supplyID1, "0", "-10", list);
        String response9 = scf.PayingConfirm(PlatformAddress, QFJGAddress, companyID1, list1, platformKeyID, platformPIN, tokenType, comments);
        assertThat(response9, containsString("400"));
        assertThat(response9, containsString("error"));
        assertThat(response9, containsString("销毁申请失败：strconv.ParseUint: parsing \\\"-10\\\": invalid syntax"));
    }

}

