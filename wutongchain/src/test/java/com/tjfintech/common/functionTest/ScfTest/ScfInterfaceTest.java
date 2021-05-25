package com.tjfintech.common.functionTest.ScfTest;

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
import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.SLEEPTIME;
import static com.tjfintech.common.utils.UtilsClassKMS.size;
import static com.tjfintech.common.utils.UtilsClassScf.*;
import static com.tjfintech.common.utils.UtilsClassScf.comments;
import static com.tjfintech.common.utils.UtilsClassScf.supplyAddress1;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;


@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ScfInterfaceTest {

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
     *资产开立申请必填字段验证
     */
    @Test
    public void Test001_IssuingApply() throws Exception {
        int levelLimit = 5;
        String amount = "100";
        String subType = "0";
        String response = kms.genRandom(size);
        String tokenType = UtilsClassScf.gettokenType(response);
        String UID = "a"+UtilsClass.Random(4);

        log.info("uid为空");
        String responseuid = scf.IssuingApply("", AccountAddress, companyID1, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
        assertThat(responseuid, containsString("400"));
        assertThat(responseuid, containsString("error"));
        assertThat(responseuid, containsString("Key: 'UID' Error:Field validation for 'UID' failed on the 'required' tag"));


        log.info("输入供应链金融合约地址为空");
        String response1 = scf.IssuingApply(UID, "", companyID1, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
        assertThat(response1, containsString("400"));
        assertThat(response1, containsString("error"));
        assertThat(response1, containsString("Key: 'ContractAddress' Error:Field validation for 'ContractAddress' failed on the 'len' tag"));

        log.info("供应链金融合约地址错误");
        String response2 = scf.IssuingApply(UID,"3228bf4f6c3d20b641fb508cfddcf578c1fbd92b64b962327b476bc9fb123456", companyID1, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
        assertThat(response2, containsString("500"));
        assertThat(response2, containsString("error"));
        assertThat(response2, containsString("error: code = Unknown desc = Smart contract does not exist"));

        log.info("资金方commpanyid为空");
        String response3 = scf.IssuingApply(UID, AccountAddress, "", coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
        assertThat(response3, containsString("400"));
        assertThat(response3, containsString("error"));
        assertThat(response3, containsString("Key: 'CompanyID' Error:Field validation for 'CompanyID' failed on the 'required' tag"));


        //log.info("资金方commpanyid不存在");
        //String response4 = scf.IssuingApply(AccountAddress, "008", coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
       // assertThat(response4, containsString("400"));
        //assertThat(response4, containsString("error"));
        //assertThat(response4, containsString("Err:账号合约验证失败"));

//        log.info("错误的核心企业id");
//        String response5 = scf.IssuingApply(AccountAddress, companyID1, "c02jbhbsnk7pq9fsojfg", PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
//        assertThat(response5, containsString("400"));
//        assertThat(response5, containsString("error"));
//        assertThat(response5, containsString("pin码与密钥不匹配,或请稍后再试"));
//
//
//        log.info("核心企业id为空");
//        String response6= scf.IssuingApply(AccountAddress, companyID1, "", PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
//        assertThat(response6, containsString("400"));
//        assertThat(response6, containsString("error"));
//        assertThat(response6, containsString("Key: 'KeyID' Error:Field validation for 'KeyID' failed on the 'len' tag"));
        log.info("pin码为空");
        String response7 = scf.IssuingApply(UID, AccountAddress, companyID1, coreCompanyKeyID, "", tokenType, levelLimit, expireDate, supplyAddress1, amount);
        assertThat(response7, containsString("400"));
        assertThat(response7, containsString("error"));
        assertThat(response7, containsString("Key: 'PIN' Error:Field validation for 'PIN' failed on the 'min' tag"));

        log.info("不存在的pin码");
        String response8 = scf.IssuingApply(UID,AccountAddress, companyID1, coreCompanyKeyID, "789", tokenType, levelLimit, expireDate, supplyAddress1, amount);
        assertThat(response8, containsString("400"));
        assertThat(response8 , containsString("error"));
        assertThat(response8, containsString("pin码与密钥不匹配,或请稍后再试"));
        log.info("tokentype为空");
        String response9 = scf.IssuingApply(UID,AccountAddress, companyID1, coreCompanyKeyID, PIN, "", levelLimit, expireDate, supplyAddress1, amount);
        assertThat(response9, containsString("400"));
        assertThat(response9, containsString("error"));
        assertThat(response9, containsString("Key: 'TokenType' Error:Field validation for 'TokenType' failed on the 'min' tag"));
        log.info("受让地址为空");
        String response10 = scf.IssuingApply(UID,AccountAddress, companyID1, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, "", amount);
        assertThat(response10, containsString("400"));
        assertThat(response10, containsString("error"));
        assertThat(response10, containsString("Key: 'ToAddr' Error:Field validation for 'ToAddr' failed on the 'min' tag"));
        log.info("开立额度为空");
        String response11 = scf.IssuingApply(UID,AccountAddress, companyID1, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, "");
        assertThat(response11, containsString("400"));
        assertThat(response11, containsString("error"));
        assertThat(response11, containsString("Key: 'Amount' Error:Field validation for 'Amount' failed on the 'min' tag"));
        log.info("开立额度为负数");
        String response12 = scf.IssuingApply(UID, AccountAddress, companyID1, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, "-1");
        assertThat(response12, containsString("400"));
        assertThat(response12, containsString("error"));
        assertThat(response12, containsString( "invalid Amount,error:aomunt must be more than 0"));

        log.info("资产有效期为空");
        String response13 = scf.IssuingApply(UID,AccountAddress, companyID1, coreCompanyKeyID, PIN, tokenType, levelLimit, null, supplyAddress1,amount);
        assertThat(response13, containsString("400"));
        assertThat(response13, containsString("error"));
        assertThat(response13, containsString("expire!"));

        log.info("开立额度不足");
        String response14= scf.IssuingApply(UID,AccountAddress, companyID1, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, "10000000");
        assertThat(response14, containsString("400"));
        assertThat(response14, containsString("error"));
        assertThat(response14, containsString("Err:合约配额已经耗尽"));

        log.info("数值超过小数点后6位");
        String response15= scf.IssuingApply(UID, AccountAddress, companyID1, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, "100.123456789");
        assertThat(response15, containsString("400"));
        assertThat(response15, containsString("error"));
        assertThat(response15, containsString("invalid Amount,error:精确度不能超过小数点后六位"));

    }

    /**
     * 开立审核必填参数验证
     */
    @Test
    public void Test005_IssuingApply() throws Exception {
        int levelLimit = 5;
        String amount = "100";
        String subType = "0";
        String response = kms.genRandom(size);
        String tokenType = UtilsClassScf.gettokenType(response);
        String UID = "a"+UtilsClass.Random(4);
        //资产开立申请
        String response1 = scf.IssuingApply(UID,AccountAddress, companyID1, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("success"));
        assertThat(response1, containsString("data"));
        Thread.sleep(5000);

        log.info("uid为空");
        String responseuid = scf.IssuingApprove("", platformKeyID,tokenType, platformPIN);
        assertThat(responseuid, containsString("400"));
        assertThat(responseuid, containsString("error"));
        assertThat(responseuid, containsString("Key: 'UID' Error:Field validation for 'UID' failed on the 'required' tag"));

        log.info("平台方id为空");
        String response2 = scf.IssuingApprove(UID, "",tokenType, platformPIN);
        assertThat(response2, containsString("400"));
        assertThat(response2, containsString("error"));
        assertThat(response2, containsString("Key: 'KeyID' Error:Field validation for 'KeyID' failed on the 'len' tag"));
        log.info("平台方id不存在");
        String response3 = scf.IssuingApprove(UID, "acdrefd",tokenType, platformPIN);
        assertThat(response3, containsString("400"));
        assertThat(response3, containsString("error"));
        assertThat(response3, containsString("Key: 'KeyID' Error:Field validation for 'KeyID' failed on the 'len' tag"));
        log.info("tokentype为空");
        String response4 = scf.IssuingApprove(UID, platformKeyID, "", platformPIN);
        assertThat(response4, containsString("400"));
        assertThat(response4, containsString("error"));
        assertThat(response4, containsString("Key: 'TokenType' Error:Field validation for 'TokenType' failed on the 'min' tag"));
        log.info("不匹配的tokentype");
        String response5 = scf.IssuingApprove(UID,platformKeyID, "abcd", platformPIN);
        assertThat(response5, containsString("400"));
        assertThat(response5, containsString("error"));
        assertThat(response5, containsString("empty sign data or \\u003cnil\\u003e"));
        log.info("输入空的平台PIN码");
        String response6 = scf.IssuingApprove(UID,platformKeyID, tokenType, "");
        assertThat(response6, containsString("400"));
        assertThat(response6, containsString("error"));
        assertThat(response6, containsString("Key: 'PIN' Error:Field validation for 'PIN' failed on the 'min' tag"));
        log.info("输入不匹配的平台pin码");
        String response7 = scf.IssuingApprove(UID,platformKeyID, tokenType, "7654321");
        assertThat(response7, containsString("400"));
        assertThat(response7, containsString("error"));
        assertThat(response7, containsString("pin码与密钥不匹配,或请稍后再试"));
    }

    /**
     * 开立取消必填参数验证
     */
    @Test
    public void Test006_IssuingCancel() {
        int levelLimit = 5;
        String amount = "100";
        String response = kms.genRandom(size);
        String tokenType = UtilsClassScf.gettokenType(response);
        String UID = "a"+UtilsClass.Random(4);
        //资产开立
        String response1 = scf.IssuingApply(UID, AccountAddress, companyID1, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("success"));
        assertThat(response1, containsString("data"));
        log.info("tokentype为空");
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
        log.info("平台id为空");
        String response6 = scf.IssuingCancel(tokenType, companyID1, "", platformPIN, comments);
        assertThat(response6, containsString("400"));
        assertThat(response6, containsString("error"));
        assertThat(response6, containsString("Key: 'KeyID' Error:Field validation for 'KeyID' failed on the 'len' tag"));
        log.info("平台id不存在//错误");
        String response7 = scf.IssuingCancel(tokenType, companyID1, "c02jbhbsnk7pq9fsojfg", platformPIN, comments);
        assertThat(response7, containsString("400"));
        assertThat(response7, containsString("error"));
        assertThat(response7, containsString("pin码与密钥不匹配,或请稍后再试"));
        log.info("平台pin码为空");
        String response8 = scf.IssuingCancel(tokenType, companyID1, platformKeyID, "", comments);
        assertThat(response8, containsString("400"));
        assertThat(response8, containsString("error"));
        assertThat(response8, containsString("Key: 'PIN' Error:Field validation for 'PIN' failed on the 'required' tag"));
        log.info("平台pin码不匹配");
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
     * 开单签收接口必填参数验证
     */
    @Test
    public void Test007_Assignmentapply() throws Exception {
        int levelLimit = 5;
        String amount = "100";
        String response = kms.genRandom(size);
        String tokenType = UtilsClassScf.gettokenType(response);
        String UID = "a"+UtilsClass.Random(4);
        String UID1 = "b"+UtilsClass.Random(4);
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

        log.info("uid为空");
        String responseuid = scf.IssuingConfirm("", PlatformAddress,coreCompanyKeyID, tokenType, PIN, comments);
        assertThat(responseuid, containsString("400"));
        assertThat(responseuid, containsString("error"));
        assertThat(responseuid, containsString("Key: 'UID' Error:Field validation for 'UID' failed on the 'required' tag"));

        log.info("开单签收的平台合约地址为空");
        String response3 = scf.IssuingConfirm(UID1, "",coreCompanyKeyID, tokenType, PIN, comments);
        assertThat(response3, containsString("400"));
        assertThat(response3, containsString("error"));
        assertThat(response3, containsString("Key: 'PlatFormAddress' Error:Field validation for 'PlatFormAddress' failed on the 'len' tag"));

//  跑不过      log.info("开单签收的平台方合约地址不匹配");
//        String response4 = scf.IssuingConfirm(UID1, "274532de3b53ccce779132fa9b5a2a10dcad4e2b230e5625605a2d7c0fdebce2", coreCompanyKeyID, tokenType, PIN, comments);
//        assertThat(response4, containsString("400"));
//        assertThat(response4, containsString("error"));
//        assertThat(response4, containsString("Err:empty stack"));
        log.info("开单签收的核心企业地址为空");
        String response5 = scf.IssuingConfirm(UID1, PlatformAddress, "", tokenType, PIN, comments);
        assertThat(response5, containsString("400"));
        assertThat(response5, containsString("error"));
        assertThat(response5, containsString("Key: 'KeyID' Error:Field validation for 'KeyID' failed on the 'len' tag"));
        log.info("开单签收的核心企业keyid不匹配");
        String response6 = scf.IssuingConfirm(UID1, PlatformAddress, "c02jbhbsnk7pq9fsojfg", tokenType, PIN, comments);
        assertThat(response6, containsString("400"));
        assertThat(response6, containsString("error"));
        assertThat(response6, containsString("pin码与密钥不匹配,或请稍后再试"));
        log.info("开单签收核心企业的pin码为空");
        String response7 = scf.IssuingConfirm(UID1, PlatformAddress, coreCompanyKeyID, tokenType, "", comments);
        assertThat(response7, containsString("400"));
        assertThat(response7, containsString("error"));
        assertThat(response7, containsString("Key: 'PIN' Error:Field validation for 'PIN' failed on the 'min' tag"));
        log.info("开单签收核心企业的pin码不匹配");
        String response8 = scf.IssuingConfirm(UID1,PlatformAddress, coreCompanyKeyID, tokenType, "8999", comments);
        assertThat(response8, containsString("400"));
        assertThat(response8, containsString("error"));
        assertThat(response8, containsString("pin码与密钥不匹配,或请稍后再试"));
        log.info("开单签收tokentype不匹配");
        String response9 = scf.IssuingConfirm(UID1, PlatformAddress, coreCompanyKeyID, "", PIN, comments);
        assertThat(response9, containsString("400"));
        assertThat(response9, containsString("error"));
        assertThat(response9, containsString("Key: 'TokenType' Error:Field validation for 'TokenType' failed on the 'min' tag"));
        log.info("开单签收tokentype不匹配");
        String response10 = scf.IssuingConfirm(UID1, PlatformAddress, coreCompanyKeyID, "asdfw", PIN, comments);
        assertEquals("400", JSONObject.fromObject(response10).getString("state"));
        assertEquals(true, response10.contains("无法根据tokentype查询到账户合约地址"));
        log.info("开单签收comments为空");
        String response11 = scf.IssuingConfirm(UID1, PlatformAddress, coreCompanyKeyID, tokenType, PIN, "");
        assertThat(response11, containsString("400"));
        assertThat(response11, containsString("error"));
        assertThat(response11, containsString("Key: 'Comments' Error:Field validation for 'Comments' failed on the 'required' tag"));
        log.info("开单签收comments格式不正确");
        String response12 = scf.IssuingConfirm(UID1, PlatformAddress, coreCompanyKeyID, tokenType, PIN, "dadasdadasdasdasdasdasdad");
        assertThat(response12, containsString("500"));
        assertThat(response12, containsString("error"));
        assertThat(response12, containsString("comments不合法!"));

    }

    /**
     * 开单拒收必填字段验证
     */
     @Test
     public void Test008_IssuingReject() throws Exception {
         int levelLimit = 5;
         String amount = "100";
         String response = kms.genRandom(size);
         String tokenType = UtilsClassScf.gettokenType(response);
         String UID = "a"+UtilsClass.Random(4);
         String UID1 = "a"+UtilsClass.Random(4);

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
         //开立拒收

         log.info("uid为空");
         String responseuid = scf.IssuingReject("", coreCompanyKeyID, tokenType, PIN, companyID1, comments);
         assertThat(responseuid, containsString("400"));
         assertThat(responseuid, containsString("error"));
         assertThat(responseuid, containsString("Key: 'UID' Error:Field validation for 'UID' failed on the 'required' tag"));


         log.info("核心企业keyid为空");
         String response3 = scf.IssuingReject(UID1, "", tokenType, PIN, companyID1, comments);
         assertThat(response3, containsString("400"));
         assertThat(response3, containsString("error"));
         assertThat(response3, containsString("Key: 'KeyID' Error:Field validation for 'KeyID' failed on the 'len' tag"));
         log.info("核心企业的keyid不正确");
         String response4 = scf.IssuingReject(UID1,"c02jbhbsnk7pq9fsojfg", tokenType, PIN, companyID1, comments);
         assertThat(response4, containsString("400"));
         assertThat(response4, containsString("error"));
         assertThat(response4, containsString("pin码与密钥不匹配,或请稍后再试"));
         log.info("核心企业的pin码为空");
         String response5 = scf.IssuingReject( UID1, coreCompanyKeyID, tokenType, "", companyID1, comments);
         assertThat(response5, containsString("400"));
         assertThat(response5, containsString("error"));
         assertThat(response5, containsString("Key: 'PIN' Error:Field validation for 'PIN' failed on the 'min' tag"));
         log.info("核心企业的pin码不匹配");
         String response6 = scf.IssuingReject(UID1, coreCompanyKeyID, tokenType, "5855", companyID1, comments);
         assertThat(response6, containsString("400"));
         assertThat(response6, containsString("error"));
         assertThat(response6, containsString("pin码与密钥不匹配,或请稍后再试"));
         log.info("tokentype为空");
         String response7 = scf.IssuingReject(UID1, coreCompanyKeyID, "", PIN, companyID1, comments);
         assertThat(response7, containsString("400"));
         assertThat(response7, containsString("error"));
         assertThat(response7, containsString("Key: 'TokenType' Error:Field validation for 'TokenType' failed on the 'min' tag"));
         log.info("tokentype 未发行");
         String response8 = scf.IssuingReject(UID1, coreCompanyKeyID, "qwer", PIN, companyID1, comments);
         assertEquals("400", JSONObject.fromObject(response8).getString("state"));
         assertEquals(true, response8.contains("无法根据tokentype查询到账户合约地址"));
         log.info("资金方的commpanyID为空");
         String response9 = scf.IssuingReject(UID1, coreCompanyKeyID, tokenType, PIN, "", comments);
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
        String tokenType = UtilsClassScf.gettokenType(response);
        String UID = "a"+UtilsClass.Random(4);
        String UID1 = "b"+UtilsClass.Random(4);
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
        //验证上链返回值
        String checking = store.GetTxDetail(KLQSstoreHash);
        assertThat(checking, containsString("200"));
        assertThat(checking, containsString("success"));
        //资产转让申请
        log.info("供应商账户地址为空");
        List<Map> list = new ArrayList<>(10);
        List<Map> list1 = UtilsClassScf.Assignment("1", "0", list);
        String response4 = scf.AssignmentApply("", supplyID1, PIN, proof, tokenType, list1, newSubType, supplyAddress2);
        assertThat(response4, containsString("400"));
        assertThat(response4, containsString("error"));
        assertThat(response4, containsString("Key: 'FromAddress' Error:Field validation for 'FromAddress' failed on the 'min' tag"));
        //输入错误供应商账户地址
        log.info("供应商账户地址不匹配开立申请地址");
        String response5 = scf.AssignmentApply("SnwNAEuLDPUZRqkuDbVmbNugisspXgdx4wfBgoiL6F1ArAWVhEn", supplyID1, PIN, proof, tokenType, list1, newSubType, supplyAddress2);
        assertThat(response5, containsString("400"));
        assertThat(response5, containsString("error"));
        assertThat(response5, containsString("Err:wvm invoke err"));


        log.info("tokenty为空");
        String response10 = scf.AssignmentApply(supplyAddress1, supplyID1, PIN, proof, "", list1, newSubType, supplyAddress2);
        assertThat(response10, containsString("400"));
        assertThat(response10, containsString("error"));
        assertThat(response10, containsString("Key: 'TokenType' Error:Field validation for 'TokenType' failed on the 'required' tag"));
        log.info("tokenteype类型为负数//无效");
        String response11 = scf.AssignmentApply(supplyAddress1, supplyID1, PIN, proof, "-1", list1, newSubType, supplyAddress2);
        assertThat(response11, containsString("400"));
        assertThat(response11, containsString("error"));
        assertThat(response11, containsString("tokentype未发行!"));//待确认
        log.info("tokenteype类型有效但未发行");
        String response12 = scf.AssignmentApply(supplyAddress1, supplyID1, PIN, proof, "55421", list1, newSubType, supplyAddress2);
        assertThat(response12, containsString("400"));
        assertThat(response12, containsString("error"));
        assertThat(response12, containsString("tokentype未发行!"));
        log.info("newSubType类型为空");
        String response13 = scf.AssignmentApply(supplyAddress1, supplyID1, PIN, proof, tokenType, list1, "", supplyAddress2);
        assertThat(response13, containsString("400"));
        assertThat(response13, containsString("error"));
        assertThat(response13, containsString("Key: 'NewSubType' Error:Field validation for 'NewSubType' failed on the 'min' tag"));
        log.info("newSubType类型为负数无效");
//        String response14 = scf.AssignmentApply(supplyAddress1, supplyID1, PIN, proof, tokenType, list1, "-1", supplyAddress2);
//        assertThat(response14, containsString("200"));
//        assertThat(response14, containsString("success"));
//        assertThat(response14, containsString("data"));
        log.info("受让供应商地址为空");
        String response15 = scf.AssignmentApply(supplyAddress1, supplyID1, PIN, proof, tokenType, list1, newSubType, "");
        assertThat(response15, containsString("400"));
        assertThat(response15, containsString("error"));
        assertThat(response15, containsString("Key: 'ToAddress' Error:Field validation for 'ToAddress' failed on the 'required' tag"));
        log.info("受让供应商地址不匹配转让申请的参数受让地址//错误受让地址");
//        String response16 = scf.AssignmentApply(supplyAddress1, supplyID1, PIN, proof, tokenType, list1, newSubType, "SoV1KWJqSr4TWXZd1kpnLG19WU95LbjMMQv7Dygph9nkHvRCNvE");
//        assertThat(response16, containsString("200"));
//        assertThat(response16, containsString("success"));
//        assertThat(response16, containsString("data"));
    }

    /**
     * 资产转让申请，必填参数参数验证（amount为负值）
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
        log.info("转让申请额度负数");
        List<Map> list = new ArrayList<>(10);
        List<Map> list1 = UtilsClassScf.Assignment("-1", "0", list);
        String response4 = scf.AssignmentApply(supplyAddress1, supplyID1, PIN, proof, tokenType, list1, newSubType, supplyAddress2);
        //response4 = scf.AssignmentApply(supplyAddress1, supplyID1, PIN, proof, tokenType, list1, newSubType, supplyAddress2);
        assertThat(response4, containsString("400"));
        assertThat(response4, containsString("error"));
        assertThat(response4, containsString("strconv.ParseUint: parsing \\\"-1\\\": invalid syntax"));

        log.info("转让额度大于开立额度");
        List<Map> list2= UtilsClassScf.Assignment("1000", "0", list);
        response4 = scf.AssignmentApply(supplyAddress1, supplyID1, PIN, proof, tokenType, list2, newSubType, supplyAddress2);
        assertThat(response4, containsString("400"));
        assertThat(response4, containsString("error"));
        assertThat(response4, containsString("Err:wvm invoke err"));

        log.info("转让额度为0");
        List<Map> list3 = UtilsClassScf.Assignment("0", "0", list);
        response4 = scf.AssignmentApply(supplyAddress1, supplyID1, PIN, proof, tokenType, list3, newSubType, supplyAddress2);
        assertThat(response4, containsString("400"));
        assertThat(response4, containsString("error"));
        assertThat(response4, containsString("Incoming value should not be 0"));

    }

//    /**
//     * 资产转让申请（输入转让额度超过开立额度）
//     */
//    @Test
//    public void Test0011_Assignmentapply() throws Exception {
//        int levelLimit = 5;
//        String amount = "100";
//        String response = kms.genRandom(size);
//        String newSubType = "n";
//        String proof = "123456";
//        String challenge = "123456";
//        String tokenType = UtilsClassScf.gettokenType(response);
//        //资产开立申请
//        String response1 = scf.IssuingApply(AccountAddress, companyID1, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
//        assertThat(response1, containsString("200"));
//        assertThat(response1, containsString("success"));
//        assertThat(response1, containsString("data"));
//        Thread.sleep(5000);
//        //开立审核
//        String response2 = scf.IssuingApprove(platformKeyID, tokenType, platformPIN);
//        assertThat(response2, containsString("200"));
//        assertThat(response2, containsString("success"));
//        assertThat(response2, containsString("data"));
//        Thread.sleep(5000);
//        //开立签收
//        String response3 = scf.IssuingConfirm(PlatformAddress, coreCompanyKeyID, tokenType, PIN, comments);
//        assertThat(response3, containsString("200"));
//        assertThat(response3, containsString("success"));
//        assertThat(response3, containsString("data"));
//
//        JSONObject KLjsonObject = JSONObject.fromObject(response3);
//        String KLstoreHash = KLjsonObject.getString("data");
//        String KLQSstoreHash = UtilsClassScf.strToHex(KLstoreHash);
//        System.out.println("KLQSstoreHash = " + KLQSstoreHash);
//
//        commonFunc.sdkCheckTxOrSleep(KLQSstoreHash, utilsClass.sdkGetTxDetailType, SLEEPTIME);
//        //验证上链返回值
//        String checking = store.GetTxDetail(KLQSstoreHash);
//        assertThat(checking, containsString("200"));
//        assertThat(checking, containsString("success"));
//        //资产转让申请
//        List<Map> list = new ArrayList<>(10);
//        List<Map> list1 = UtilsClassScf.Assignment("1000", "0", list);
//        String response4 = scf.AssignmentApply(supplyAddress1, supplyID1, PIN, proof, tokenType, list1, newSubType, supplyAddress2);
//        assertThat(response4, containsString("400"));
//        assertThat(response4, containsString("error"));
//        assertThat(response4, containsString("Err:wvm invoke err"));
//    }
//
//    /**
//     * 资产转让申请（输入转让额度为0）
//     */
//    @Test
//    public void Test0012_Assignmentapply() throws Exception {
//        int levelLimit = 5;
//        String amount = "100";
//        String response = kms.genRandom(size);
//        String newSubType = "n";
//        String proof = "123456";
//        String challenge = "123456";
//        String tokenType = UtilsClassScf.gettokenType(response);
//        //资产开立申请
//        String response1 = scf.IssuingApply(AccountAddress, companyID1, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
//        assertThat(response1, containsString("200"));
//        assertThat(response1, containsString("success"));
//        assertThat(response1, containsString("data"));
//        Thread.sleep(5000);
//        //开立审核
//        String response2 = scf.IssuingApprove(platformKeyID, tokenType, platformPIN);
//        assertThat(response2, containsString("200"));
//        assertThat(response2, containsString("success"));
//        assertThat(response2, containsString("data"));
//        Thread.sleep(5000);
//        //开立签收
//        String response3 = scf.IssuingConfirm(PlatformAddress, coreCompanyKeyID, tokenType, PIN, comments);
//        assertThat(response3, containsString("200"));
//        assertThat(response3, containsString("success"));
//        assertThat(response3, containsString("data"));
//
//        JSONObject KLjsonObject = JSONObject.fromObject(response3);
//        String KLstoreHash = KLjsonObject.getString("data");
//        String KLQSstoreHash = UtilsClassScf.strToHex(KLstoreHash);
//        System.out.println("KLQSstoreHash = " + KLQSstoreHash);
//
//        commonFunc.sdkCheckTxOrSleep(KLQSstoreHash, utilsClass.sdkGetTxDetailType, SLEEPTIME);
//        //验证上链返回值
//        String checking = store.GetTxDetail(KLQSstoreHash);
//        assertThat(checking, containsString("200"));
//        assertThat(checking, containsString("success"));
//        //资产转让申请
//        List<Map> list = new ArrayList<>(10);
//        List<Map> list1 = UtilsClassScf.Assignment("0", "0", list);
//        String response4 = scf.AssignmentApply(supplyAddress1, supplyID1, PIN, proof, tokenType, list1, newSubType, supplyAddress2);
//        assertThat(response4, containsString("400"));
//        assertThat(response4, containsString("error"));
//        assertThat(response4, containsString("Incoming value should not be 0"));
//    }

    /**
     * 资产转让签收接必填参数验证
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
        String UID = "a"+UtilsClass.Random(4);
        String UID1 = "b"+UtilsClass.Random(4);
        String UID2 = "c"+UtilsClass.Random(4);

        //资产开立申请
        String response1 = scf.IssuingApply(UID,AccountAddress, companyID1, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
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
        //资产转让申请
        List<Map> list = new ArrayList<>(10);
        List<Map> list1 = UtilsClassScf.Assignment("1", "0", list);
        String response4 = scf.AssignmentApply(supplyAddress1, supplyID1, PIN, proof, tokenType, list1, newSubType, supplyAddress2);
        assertThat(response4, containsString("200"));
        assertThat(response4, containsString("success"));
        assertThat(response4, containsString("data"));
        Thread.sleep(5000);
        //资产转让签收

        log.info("uid为空");
        String responseuid = scf.AssignmentConfirm("",PlatformAddress, supplyID1, PIN, challenge, tokenType, comments);
        assertThat(responseuid, containsString("400"));
        assertThat(responseuid, containsString("error"));
        assertThat(responseuid, containsString("Key: 'UID' Error:Field validation for 'UID' failed on the 'required' tag"));

        //输入错误平台方合约地址
        log.info("平台合约地址为错误地址/不存在/无效");
        String response5 = scf.AssignmentConfirm(UID2,"274532de3b53ccce779132fa9b5a2a10dcad4e2b230e5625605a2d7c0fdebce2", supplyID1, PIN, challenge, tokenType, comments);
        assertThat(response5, containsString("400"));
        assertThat(response5, containsString("error"));
        assertThat(response5, containsString("empty stack"));
        //输入空平台方合约地址
        log.info("平台方合约地址为空");
        String response6 = scf.AssignmentConfirm(UID2, "",supplyID1, PIN, challenge, tokenType, comments);
        assertThat(response6, containsString("400"));
        assertThat(response6, containsString("error"));
        assertThat(response6, containsString("Key: 'PlatFormAddress' Error:Field validation for 'PlatFormAddress' failed on the 'len' tag"));
        log.info("供应商id不存在系统中，不匹配转让申请的中受让方地址/");
        String response7 = scf.AssignmentConfirm(UID2,PlatformAddress, "c02jbhjsnk7pq9fsojvg", PIN, challenge, tokenType, comments);
        assertThat(response7, containsString("400"));
        assertThat(response7, containsString("error"));
        assertThat(response7, containsString("pin码与密钥不匹配,或请稍后再试"));
        log.info("供应商id为空");
        String response8 = scf.AssignmentConfirm(UID2, PlatformAddress, "", PIN, challenge, tokenType, comments);
        assertThat(response8, containsString("400"));
        assertThat(response8, containsString("error"));
        assertThat(response8, containsString("Key: 'KeyID' Error:Field validation for 'KeyID' failed on the 'len' tag"));
        log.info("转出方供应商pin码不匹配");
        String response9 = scf.AssignmentConfirm(UID2,PlatformAddress, supplyID1, "4567", challenge, tokenType, comments);
        assertThat(response9, containsString("400"));
        assertThat(response9, containsString("error"));
        assertThat(response9, containsString("pin码与密钥不匹配,或请稍后再试"));
        log.info("转出方供应商pin,码为空");
        String response10 = scf.AssignmentConfirm(UID2,PlatformAddress, supplyID1, "", challenge, tokenType, comments);
        assertThat(response10, containsString("400"));
        assertThat(response10, containsString("error"));
        assertThat(response10, containsString("Key: 'PIN' Error:Field validation for 'PIN' failed on the 'required' tag"));
        log.info("challenge错误，不匹配proof");
        String response11 = scf.AssignmentConfirm(UID2, PlatformAddress, supplyID1, PIN, "5566", tokenType, comments);
        assertThat(response11, containsString("400"));
        assertThat(response11, containsString("error"));
        assertThat(response11, containsString("get nil by WVM method GetTransferList"));
        log.info("challenge为空");
        String response12 = scf.AssignmentConfirm(UID2,PlatformAddress, supplyID1, PIN, "", tokenType, comments);
        assertThat(response12, containsString("400"));
        assertThat(response12, containsString("error"));
        assertThat(response12, containsString("Key: 'Challenge' Error:Field validation for 'Challenge' failed on the 'required' tag"));
        log.info("tokentype不匹配");
        String response13 = scf.AssignmentConfirm(UID2,PlatformAddress, supplyID1, PIN, challenge, "9898", comments);
        assertEquals("400", JSONObject.fromObject(response13).getString("state"));
        assertEquals(true, response13.contains("无法根据tokentype查询到账户合约地址"));
        log.info("tokentype为空");
        String response14 = scf.AssignmentConfirm(UID2,PlatformAddress, supplyID1, PIN, challenge, "", comments);
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
        String UID = "a"+UtilsClass.Random(4);
        String UID1 = "b"+UtilsClass.Random(4);
        String UID2= "c"+UtilsClass.Random(4);
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
        //资产转让拒收
        //输入错误challenge
        log.info("challenge不匹配proof");
        String response6 = scf.AssignmentReject(UID2,"123aaa", tokenType);
        assertThat(response6, containsString("400"));
        assertThat(response6, containsString("error"));
        assertThat(response6, containsString("Err:wvm invoke err"));
        log.info("challenge为空");
        String response7 = scf.AssignmentReject(UID2, "", tokenType);
        assertThat(response7, containsString("400"));
        assertThat(response7, containsString("error"));
        assertThat(response7, containsString("Key: 'Challenge' Error:Field validation for 'Challenge' failed on the 'min' tag"));
        log.info("token type未发行");
        String response8 = scf.AssignmentReject(UID2, challenge, "2161651");
        assertEquals("400", JSONObject.fromObject(response8).getString("state"));
        assertEquals(true, response8.contains("无法根据tokentype查询到账户合约地址"));

        log.info("tokenty为空");
        String response9 = scf.AssignmentReject(UID2, challenge, "");
        assertThat(response9, containsString("400"));
        assertThat(response9, containsString("error"));
        assertThat(response9, containsString("Key: 'TokenType' Error:Field validation for 'TokenType' failed on the 'required' tag"));
    }

    /**
     * 融资申请必填参数验证
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
         //融资申请
         String newFromSubType = "0";
         String newToSubType = "b";
         String rzamount = "1";
         log.info("供应商地址为空");
         String response4 = scf.FinacingApply("", supplyID1, PIN, proof, tokenType, rzamount, subType, newFromSubType, newToSubType, supplyAddress2);
         assertThat(response4, containsString("400"));
         assertThat(response4, containsString("error"));
         assertThat(response4, containsString("Key: 'FromAddress' Error:Field validation for 'FromAddress' failed on the 'min' tag"));
         log.info("错误的供应商地址");
         String response5 = scf.FinacingApply("SnwNAEuLDPUZRqkuDbVmbNugisspXgdx4wfBgoiL6F1ArAWVhEn", supplyID1, PIN, proof, tokenType, rzamount, subType, newFromSubType, newToSubType, supplyAddress2);
         assertThat(response5, containsString("400"));
         assertThat(response5, containsString("error"));
         assertThat(response5, containsString("Err:wvm invoke err"));


         log.info("错误的tokentype");
         String response10 = scf.FinacingApply(supplyAddress1, supplyID1, PIN, proof, "615516515", rzamount, subType, newFromSubType, newToSubType, supplyAddress2);
         assertThat(response10, containsString("400"));
         assertThat(response10, containsString("error"));
         assertThat(response10, containsString("tokentype未发行!"));
         log.info("tokentype为空");
         String response11 = scf.FinacingApply(supplyAddress1, supplyID1, PIN, proof, "", rzamount, subType, newFromSubType, newToSubType, supplyAddress2);
         assertThat(response11, containsString("400"));
         assertThat(response11, containsString("error"));
         assertThat(response11, containsString("tokentype未发行!"));
         //融资金额大于小数点后6位
//         String response12 = scf.FinacingApply(supplyAddress1, supplyID1, PIN, proof, tokenType, "10.123456789", subType, newFromSubType, newToSubType, supplyAddress2);
//         assertThat(response12, containsString("400"));
//         assertThat(response12, containsString("error"));
//         assertThat(response12, containsString("Failed! Err:wvm invoke err"));
         log.info("融资金额为空");
         String response13 = scf.FinacingApply(supplyAddress1, supplyID1, PIN, proof, tokenType, "0", subType, newFromSubType, newToSubType, supplyAddress2);
         assertThat(response13, containsString("400"));
         assertThat(response13, containsString("error"));
         assertThat(response13, containsString("invalid parameter,error:aomunt must be more than 0"));
         log.info("融资金额为负数");
         String response14 = scf.FinacingApply(supplyAddress1, supplyID1, PIN, proof, tokenType, "-1", subType, newFromSubType, newToSubType, supplyAddress2);
         assertThat(response14, containsString("400"));
         assertThat(response14, containsString("error"));
         assertThat(response14, containsString("invalid parameter,error:aomunt must be more than 0"));
         log.info("融资金额超过资产总金额");
         String response15 = scf.FinacingApply(supplyAddress1, supplyID1, PIN, proof, tokenType, "1000000", subType, newFromSubType, newToSubType, supplyAddress2);
         assertThat(response15, containsString("400"));
         assertThat(response15, containsString("error"));
         assertThat(response15, containsString("Failed! Err:wvm invoke err"));
         log.info("受让方地址不存在/错误");
         String response16 = scf.FinacingApply(supplyAddress1, supplyID1, PIN, proof, tokenType, rzamount, subType, newFromSubType, newToSubType, "SoV1KWJqSr4TWXZd1kpnLG19WU95LbjMMQv7Dygph9nkHvRCNvE");
         assertThat(response16, containsString("200"));
         assertThat(response16, containsString("success"));
         assertThat(response16, containsString("data"));
         log.info("受让方地址为空");
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
        log.info("资金方合约地址错误");
        String response1 = scf.FinacingTest("d08a969370898876d08b4314cba416ec26fa6ce1857fd4f6335c68eaf0872dca", amount, timeLimit);
        assertThat(response1, containsString("500"));
        assertThat(response1, containsString("error"));
        assertThat(response1, containsString("rpc error: code = Unknown desc = Smart contract does not exist"));
        log.info("资金方合约地址为空");
        String response2 = scf.FinacingTest("", amount, timeLimit);
        assertThat(response2, containsString("400"));
        assertThat(response2, containsString("error"));
        assertThat(response2, containsString("Key: 'ContractAddress' Error:Field validation for 'ContractAddress' failed on the 'len' tag"));
        log.info("金额为空");
        String response3 = scf.FinacingTest(ZJFAddress, "", timeLimit);
        assertThat(response3, containsString("400"));
        assertThat(response3, containsString("error"));
        assertThat(response3, containsString("Key: 'Amount' Error:Field validation for 'Amount' failed on the 'required' tag"));
        log.info("金额为负数");
        String response4 = scf.FinacingTest(ZJFAddress, "-1", timeLimit);
        assertThat(response4, containsString("400"));
        assertThat(response4, containsString("error"));
        assertThat(response4, containsString("aomunt must be more than 0"));
        log.info("金额为0");
        String response5 = scf.FinacingTest(ZJFAddress, "0", timeLimit);
        assertThat(response5, containsString("400"));
        assertThat(response5, containsString("error"));
        assertThat(response5, containsString("aomunt must be more than 0"));
        log.info("融资期限为0");
        String response6 = scf.FinacingTest(ZJFAddress, amount, "0");
        assertThat(response6, containsString("500"));
        assertThat(response6, containsString("error"));
        assertThat(response6, containsString("rpc error: code = Unknown desc = overflow double"));
        log.info("融资期限为负数");
        String response7 = scf.FinacingTest(ZJFAddress, amount, "-1");
        assertThat(response7, containsString("400"));
        assertThat(response7, containsString("error"));
        assertThat(response7, containsString("error timeLimit!"));
        log.info("融资期限为空");
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
        log.info("资金方合约不存在系统中");
        String response6 = scf.FinacingFeedback("d08a969370898876d08b4314cba416ec26fa6ce1857fd4f6335c68eaf0872dca", applyNo, state, comments, msg);
        assertThat(response6, containsString("500"));
        assertThat(response6, containsString("error"));
        assertThat(response6, containsString("rpc error: code = InvalidArgument desc = Smart contract does not exist"));
        log.info("资金方合约为空");
        String response7 = scf.FinacingFeedback("", applyNo, state, comments, msg);
        assertThat(response7, containsString("400"));
        assertThat(response7, containsString("error"));
        assertThat(response7, containsString("Key: 'ContractAddress' Error:Field validation for 'ContractAddress' failed on the 'len' tag"));
        log.info("错误的还款状态为空");
        String response8 = scf.FinacingFeedback(ZJFAddress, applyNo, "", comments, msg);
        assertThat(response8, containsString("400"));
        assertThat(response8, containsString("error"));
        assertThat(response8, containsString("Key: 'State' Error:Field validation for 'State' failed on the 'required' tag"));
        log.info("错误的还款状态为负数");
        String response9 = scf.FinacingFeedback(ZJFAddress, applyNo, "-1", comments, msg);
        assertThat(response9, containsString("400"));
        assertThat(response9, containsString("error"));
        assertThat(response9, containsString("Key: 'State' Error:Field validation for 'State' failed on the 'oneof' tag"));
        log.info("错误的comments格式");
        String response10 = scf.FinacingFeedback(ZJFAddress, applyNo, state, "SAJDNAJKLNDAJNCOA", msg);
        assertThat(response10, containsString("500"));
        assertThat(response10, containsString("error"));
        assertThat(response10, containsString("comments不合法!(需要通过post http://ip:port/scf/func/sendmsg生成的数据"));
        log.info("comments为空");
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
         String UID = "a"+UtilsClass.Random(4);
         String UID1 = "b"+UtilsClass.Random(4);
         String UID2 = "c"+UtilsClass.Random(4);
         //资产开立申请
         String response1 = scf.IssuingApply(UID,AccountAddress, companyID1, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
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
         log.info("平台合约不存在/错误");
         String response7 = scf.FinacingConfirm(UID2,"274532de3b53ccce779132fa9b5a2a10dcad4e2b230e5625605a2d7c0fdebce2", applyNo, ZJFAddress, supplyID1, companyID1, PIN, tokenType, supplyAddress2, challenge, comments);
         assertThat(response7, containsString("400"));
         assertThat(response7, containsString("error"));
         assertThat(response7, containsString("Failed! Err:empty stack"));
         log.info("平台合约地址为空");
         String response8 = scf.FinacingConfirm(UID2, "", applyNo, ZJFAddress, supplyID1, companyID1, PIN, tokenType, supplyAddress2, challenge, comments);
         assertThat(response8, containsString("400"));
         assertThat(response8, containsString("error"));
         assertThat(response8, containsString("Key: 'PlatFormAddress' Error:Field validation for 'PlatFormAddress' failed on the 'required' tag"));
         log.info("错误的融资编号");
         String response9 = scf.FinacingConfirm(UID2, PlatformAddress, "1516151", ZJFAddress, supplyID1, companyID1, PIN, tokenType, supplyAddress2, challenge, comments);
         assertThat(response9, containsString("400"));
         assertThat(response9, containsString("error"));
         assertThat(response9, containsString("Failed! Err:error"));
         log.info("融资编号为空");
         String response10 = scf.FinacingConfirm(UID2, PlatformAddress, "", ZJFAddress, supplyID1, companyID1, PIN, tokenType, supplyAddress2, challenge, comments);
         assertThat(response10, containsString("400"));
         assertThat(response10, containsString("error"));
         assertThat(response10, containsString("Key: 'ApplyNo' Error:Field validation for 'ApplyNo' failed on the 'required' tag"));
         log.info("资金方合约地址为不存在系统中");
//         String response11 = scf.FinacingConfirm(PlatformAddress, applyNo, "d08a969370898876d08b4314cba416ec26fa6ce185335c68e1123af213172dca", supplyID1, companyID1, PIN, tokenType, supplyAddress2, challenge, comments);
//         assertThat(response11, containsString("400"));
//         assertThat(response11, containsString("error"));
//         assertThat(response11, containsString("Failed! Err:empty stack"));
         log.info("资金方合约地址为空");
         String response12 = scf.FinacingConfirm(UID2, PlatformAddress, applyNo, "", supplyID1, companyID1, PIN, tokenType, supplyAddress2, challenge, comments);
         assertThat(response12, containsString("400"));
         assertThat(response12, containsString("error"));
         assertThat(response12, containsString("Key: 'ContractAddress' Error:Field validation for 'ContractAddress' failed on the 'required' tag"));
         log.info("错误资产接受方的id");
         String response13 = scf.FinacingConfirm(UID2, PlatformAddress, applyNo, ZJFAddress, "c02jbhjsnk7pq9fsojvg", companyID1, PIN, tokenType, supplyAddress2, challenge, comments);
         assertThat(response13, containsString("400"));
         assertThat(response13, containsString("error"));
         assertThat(response13, containsString("pin码与密钥不匹配,或请稍后再试"));
         log.info("受让方的id为空");
         String response14 = scf.FinacingConfirm(UID2, PlatformAddress, applyNo, ZJFAddress, "", companyID1, PIN, tokenType, supplyAddress2, challenge, comments);
         assertThat(response14, containsString("400"));
         assertThat(response14, containsString("error"));
         assertThat(response14, containsString("Key: 'KeyID' Error:Field validation for 'KeyID' failed on the 'len' tag"));
         log.info("资金方id不匹配/错误");
//         String response15 = scf.FinacingConfirm(PlatformAddress, applyNo, ZJFAddress, supplyID1, "1238", PIN, tokenType, supplyAddress2, challenge, comments);
//         assertThat(response15, containsString("200"));
//         assertThat(response15, containsString("success"));
//         assertThat(response15, containsString("data"));
         log.info("资金方id为空");
//         String response16 = scf.FinacingConfirm(PlatformAddress, applyNo, ZJFAddress, supplyID1, "", PIN, tokenType, supplyAddress2, challenge, comments);
//         assertThat(response16, containsString("400"));
//         assertThat(response16, containsString("error"));
//         assertThat(response16, containsString("has been spent"));
         log.info("pin码 不匹配");
         String response17 = scf.FinacingConfirm(UID2, PlatformAddress, applyNo, ZJFAddress, supplyID1, companyID1, "65465", tokenType, supplyAddress2, challenge, comments);
         assertThat(response17, containsString("400"));
         assertThat(response17, containsString("error"));
         assertThat(response17, containsString("pin码与密钥不匹配,或请稍后再试"));
         log.info("pin码为空");
         String response18 = scf.FinacingConfirm(UID2, PlatformAddress, applyNo, ZJFAddress, supplyID1, companyID1, "", tokenType, supplyAddress2, challenge, comments);
         assertThat(response18, containsString("400"));
         assertThat(response18, containsString("error"));
         assertThat(response18, containsString("Key: 'PIN' Error:Field validation for 'PIN' failed on the 'required' tag"));
         log.info("接受地址错误");
//         String response19 = scf.FinacingConfirm(PlatformAddress, applyNo, ZJFAddress, supplyID1, companyID1, PIN, tokenType, "08a969370898876d08b4314cba416ec26fa6ce1857fd4f6335c68eaf", challenge, comments);
//         assertThat(response19, containsString("200"));
//         assertThat(response19, containsString("success"));
//         assertThat(response19, containsString("data"));
         log.info("接收地址为空");
//         String response20 = scf.FinacingConfirm(PlatformAddress, applyNo, ZJFAddress, supplyID1, companyID1, PIN, tokenType, "", challenge, comments);
//         assertThat(response20, containsString("400"));
//         assertThat(response20, containsString("error"));
//         assertThat(response20, containsString("invalid character 's' looking for beginning of value"));
         log.info("challenge不匹配/错误");
         String response21 = scf.FinacingConfirm(UID2, PlatformAddress, applyNo, ZJFAddress, supplyID1, companyID1, PIN, tokenType, supplyAddress2, "qweqcas", comments);
         assertThat(response21, containsString("400"));
         assertThat(response21, containsString("error"));
         assertThat(response21, containsString("get nil by WVM method GetTransferList"));
         log.info("challenge为空");
         String response22 = scf.FinacingConfirm(UID2, PlatformAddress, applyNo, ZJFAddress, supplyID1, companyID1, PIN, tokenType, supplyAddress2, "", comments);
         assertThat(response22, containsString("400"));
         assertThat(response22, containsString("error"));
         assertThat(response22, containsString("Key: 'Challenge' Error:Field validation for 'Challenge' failed on the 'required' tag"));
         log.info("comments不匹配/错误");
//         String response23 = scf.FinacingConfirm(PlatformAddress, applyNo, ZJFAddress, supplyID1, companyID1, PIN, tokenType, supplyAddress2, challenge, "asdeadfascasdsafasfvdvdfssgv");
//         assertThat(response23, containsString("200"));
//         assertThat(response23, containsString("success"));
//         assertThat(response23, containsString("data"));
         log.info("comments为空");
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
        log.info("challenge不匹配");
        String response7 = scf.FinacingCancel(UID2,"123sss", tokenType);
        assertThat(response7, containsString("400"));
        assertThat(response7, containsString("error"));
        assertThat(response7, containsString("wvm invoke err"));
        log.info("challenge为空");
        String response8 = scf.FinacingCancel(UID2, "", tokenType);
        assertThat(response8, containsString("400"));
        assertThat(response8, containsString("error"));
        assertThat(response8, containsString("Key: 'Challenge' Error:Field validation for 'Challenge' failed on the 'min' tag"));
        log.info("tokentype不匹配/错误");
        String response9 = scf.FinacingCancel(UID2, challenge, "sadasdasc");
        assertEquals("400", JSONObject.fromObject(response9).getString("state"));
        assertEquals(true, response9.contains("无法根据tokentype查询到账户合约地址"));
        log.info("tokentype为空");
        String response10 = scf.FinacingCancel(UID2, challenge, "");
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
        String UID = "a"+UtilsClass.Random(4);
        String UID1 = "b"+UtilsClass.Random(4);
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

        //兑付申请
        log.info("tokentype不匹配");
        String response4 = scf.PayingApply("123ccasd", companyID1, comments);
        assertThat(response4, containsString("400"));
        assertThat(response4, containsString("error"));
        assertThat(response4, containsString("wvm invoke err"));
        log.info("tokentype为空");
        String response5 = scf.PayingApply("", companyID1, comments);
        assertThat(response5, containsString("400"));
        assertThat(response5, containsString("error"));
        assertThat(response5, containsString("Key: 'TokenType' Error:Field validation for 'TokenType' failed on the 'min' tag"));
        log.info("commpanyID错误");
//        String response6 = scf.PayingApply(tokenType, "0010", comments);
//        assertThat(response6, containsString("200"));
//        assertThat(response6, containsString("success"));
//        assertThat(response6, containsString("data"));
        log.info("commpanyid为空");
//        String response7 = scf.PayingApply(tokenType, "", comments);
//        assertThat(response7, containsString("200"));
//        assertThat(response7, containsString("success"));
//        assertThat(response7, containsString("data"));
        log.info("comments不匹配");
//        String response8 = scf.PayingApply(tokenType, companyID1, "dasdacajoncasocnoan");
//        assertThat(response8, containsString("200"));
//        assertThat(response8, containsString("success"));
//        assertThat(response8, containsString("data"));
        log.info("comments为空");
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
        String UID = "a"+UtilsClass.Random(4);
        String UID1 = "b"+UtilsClass.Random(4);
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
        assertThat(response6, containsString("ContractAddress' Error:Field validation for 'ContractAddress' failed on the 'len' tag"));
        //输入空清分机构合约地址
        String response7 = scf.PayingFeedback("", tokenType, state, comments);
        assertThat(response7, containsString("400"));
        assertThat(response7, containsString("error"));
        assertThat(response7, containsString("ContractAddress' Error:Field validation for 'ContractAddress' failed on the 'len' tag"));
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
        assertThat(response11, containsString("Key: 'State' Error:Field validation for 'State' failed on the 'oneof' tag"));
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
        //输入错误平台方合约地址
        List<Map> list = new ArrayList<>(10);
        List<Map> list1 = UtilsClassScf.paying(supplyAddress1, supplyID1, "0", "100", list);
        String response9 = scf.PayingConfirm(UID2, "asdacascasfcdgsagasfgaca", QFJGAddress, companyID1, list1, platformKeyID, platformPIN, tokenType, comments);
        assertThat(response9, containsString("400"));
        assertThat(response9, containsString("error"));
        assertThat(response9, containsString("PlatFormAddress' Error:Field validation for 'PlatFormAddress' failed on the 'len' tag"));
        //输入空平台方合约地址
        String response10 = scf.PayingConfirm(UID2, "", QFJGAddress, companyID1, list1, platformKeyID, platformPIN, tokenType, comments);
        assertThat(response10, containsString("400"));
        assertThat(response10, containsString("error"));
        assertThat(response10, containsString("Key: 'PlatFormAddress' Error:Field validation for 'PlatFormAddress' failed on the 'len' tag"));
        //输入错误清分机构地址
        String response11 = scf.PayingConfirm(UID2, PlatformAddress, "aicbnasjnfpiancpajinpa", companyID1, list1, platformKeyID, platformPIN, tokenType, comments);
        assertThat(response11, containsString("400"));
        assertThat(response11, containsString("error"));
        assertThat(response11, containsString("Key: 'ContractAddress' Error:Field validation for 'ContractAddress' failed on the 'len' tag"));
        //输入空清分机构地址
        String response12 = scf.PayingConfirm(UID2, PlatformAddress, "", companyID1, list1, platformKeyID, platformPIN, tokenType, comments);
        assertThat(response12, containsString("400"));
        assertThat(response12, containsString("error"));
        assertThat(response12, containsString("Key: 'ContractAddress' Error:Field validation for 'ContractAddress' failed on the 'len' tag"));
        //输入错误资金方ID
//        String response13 = scf.PayingConfirm(PlatformAddress, QFJGAddress, "008", list1, platformKeyID, platformPIN, tokenType, comments);
//        assertThat(response13, containsString("503"));
//        assertThat(response13, containsString("error"));
//        assertThat(response13, containsString("out idx[0] has been spen"));
        //输入空资金方ID
        String response14 = scf.PayingConfirm(UID2, PlatformAddress, QFJGAddress, "", list1, platformKeyID, platformPIN, tokenType, comments);
        assertThat(response14, containsString("400"));
        assertThat(response14, containsString("error"));
        assertThat(response14, containsString("Key: 'CompanyID' Error:Field validation for 'CompanyID' failed on the 'required' tag"));
        //输入错误平台方id
        String response15 = scf.PayingConfirm(UID2, PlatformAddress, QFJGAddress, companyID1, list1, "c02jbhjsnk7pq9fsojvg", platformPIN, tokenType, comments);
        assertThat(response15, containsString("400"));
        assertThat(response15, containsString("error"));
        assertThat(response15, containsString("pin码与密钥不匹配,或请稍后再试"));
        //输入空平台方id
        String response16 = scf.PayingConfirm(UID2, PlatformAddress, QFJGAddress, companyID1, list1, "", platformPIN, tokenType, comments);
        assertThat(response16, containsString("400"));
        assertThat(response16, containsString("error"));
        assertThat(response16, containsString("Key: 'PlatformKeyID' Error:Field validation for 'PlatformKeyID' failed on the 'required' tag"));
        //输入错误平台方pin码
        String response17 = scf.PayingConfirm(UID2, PlatformAddress, QFJGAddress, companyID1, list1, platformKeyID, "dacasasd", tokenType, comments);
        assertThat(response17, containsString("400"));
        assertThat(response17, containsString("error"));
        assertThat(response17, containsString("pin码与密钥不匹配,或请稍后再试"));
        //输入空平台方pin码
        String response18 = scf.PayingConfirm(UID2, PlatformAddress, QFJGAddress, companyID1, list1, platformKeyID, "", tokenType, comments);
        assertThat(response18, containsString("400"));
        assertThat(response18, containsString("error"));
        assertThat(response18, containsString("Key: 'PIN' Error:Field validation for 'PIN' failed on the 'min' tag"));
        //输入错误tokentype
        String response19 = scf.PayingConfirm(UID2, PlatformAddress, QFJGAddress, companyID1, list1, platformKeyID, platformPIN, "asccacadfdd", comments);
        assertThat(response19, containsString("400"));
        assertThat(response19, containsString("error"));
        assertThat(response19, containsString("上链失败，err:无法根据tokentype查询到账户合约地址"));
        //输入空tokentype
        String response20 = scf.PayingConfirm(UID2, PlatformAddress, QFJGAddress, companyID1, list1, platformKeyID, platformPIN, "", comments);
        assertThat(response20, containsString("400"));
        assertThat(response20, containsString("error"));
        assertThat(response20, containsString("Key: 'TokenType' Error:Field validation for 'TokenType' failed on the 'min' tag"));
        //输入错误comments
//        String response21 = scf.PayingConfirm(PlatformAddress, QFJGAddress, companyID1, list1, platformKeyID, platformPIN, tokenType, "sacancnijasniasninaisfnai");
//        assertThat(response21, containsString("503"));
//        assertThat(response21, containsString("error"));
//        assertThat(response21, containsString("has been spent"));
        //输入空comments
        String response22 = scf.PayingConfirm(UID2, PlatformAddress, QFJGAddress, companyID1, list1, platformKeyID, platformPIN, tokenType, "");
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
        log.info("输入错误供应商地址");
        List<Map> list = new ArrayList<>(10);
        List<Map> list1 = UtilsClassScf.paying("adasnjanijanasmd", supplyID1, "0", "100", list);
        String response9 = scf.PayingConfirm(UID2, PlatformAddress, QFJGAddress, companyID1, list1, platformKeyID, platformPIN, tokenType, comments);
        assertThat(response9, containsString("400"));
        assertThat(response9, containsString("error"));
        assertThat(response9, containsString("销毁申请失败： Insufficient Balance!"));

        log.info("供应商地址为空");

        List<Map> list2 = UtilsClassScf.paying("", supplyID1, "0", "100", list);
        response9 = scf.PayingConfirm(UID2, PlatformAddress, QFJGAddress, companyID1, list2, platformKeyID, platformPIN, tokenType, comments);
        assertThat(response9, containsString("400"));
        assertThat(response9, containsString("error"));
        assertThat(response9, containsString("invalid parameter:回收地址不可以为空"));

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
        String UID = "a"+UtilsClass.Random(4);
        String UID1 = "b"+UtilsClass.Random(4);
        String UID2 = "c"+UtilsClass.Random(4);
        //资产开立申请
        String response1 = scf.IssuingApply(UID,AccountAddress, companyID1, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
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
        String response9 = scf.PayingConfirm(UID2, PlatformAddress, QFJGAddress, companyID1, list1, platformKeyID, platformPIN, tokenType, comments);
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
        log.info("keyid和subtyp为空");
        List<Map> list = new ArrayList<>(10);
        List<Map> list1 = UtilsClassScf.paying(supplyAddress1, "", "0", "100", list);
        String response9 = scf.PayingConfirm(UID2, PlatformAddress, QFJGAddress, companyID1, list1, platformKeyID, platformPIN, tokenType, comments);
        assertThat(response9, containsString("400"));
        assertThat(response9, containsString("error"));
        assertThat(response9, containsString("KeyID和pubkey不能同时为空!"));
    }

    /**
     *兑付确认（兑付金额超大、为空、负值）必填参数确认
     */
    @Test
    public void Test0027_PayingConfirm() throws Exception {
        int levelLimit = 5;
        String amount = "100.0";
        String response = kms.genRandom(size);
        String tokenType = UtilsClassScf.gettokenType(response);
        String UID = "a"+UtilsClass.Random(4);
        String UID1 = "b"+UtilsClass.Random(4);
        String UID2 = "b"+UtilsClass.Random(4);
        //资产开立申请
        String response1 = scf.IssuingApply(UID, AccountAddress, companyID1, coreCompanyKeyID, PIN, tokenType, levelLimit, expireDate, supplyAddress1, amount);
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
        log.info("兑付金额为空,");
        List<Map> list = new ArrayList<>(10);
        List<Map> list1 = UtilsClassScf.paying(supplyAddress1, supplyID1, "0", "", list);
        String response9 = scf.PayingConfirm(UID2, PlatformAddress, QFJGAddress, companyID1, list1, platformKeyID, platformPIN, tokenType, comments);
        assertThat(response9, containsString("400"));
        assertThat(response9, containsString("error"));
        assertThat(response9, containsString("invalid amount"));

        log.info("兑付金额超过开立额度");

        List<Map> list2 = UtilsClassScf.paying(supplyAddress1, supplyID1, "0", "1000000", list);
        response9 = scf.PayingConfirm(UID2, PlatformAddress, QFJGAddress, companyID1, list2, platformKeyID, platformPIN, tokenType, comments);
        assertThat(response9, containsString("400"));
        assertThat(response9, containsString("error"));
        assertThat(response9, containsString("销毁申请失败： Insufficient Balance!"));

        log.info("兑付金额为负数");

        List<Map> list3= UtilsClassScf.paying(supplyAddress1, supplyID1, "0", "-10", list);
        response9 = scf.PayingConfirm(UID2,PlatformAddress, QFJGAddress, companyID1, list3, platformKeyID, platformPIN, tokenType, comments);
        assertThat(response9, containsString("400"));
        assertThat(response9, containsString("error"));
        assertThat(response9, containsString("aomunt must be more than 0"));

    }
    /**
     *抹账接口必填参数验证//ff
     */
    @Test
    public void Test0029_Finacingback() throws Exception {


        String UID = "a"+UtilsClass.Random(4);

        log.info("txid为空");
        String response10 = scf.FinacingBack(UID, PlatformAddress, platformKeyID, PIN, supplyID2, "", comments);
        assertEquals("400", JSONObject.fromObject(response10).getString("state"));
        assertEquals(true, response10.contains("'TxId' Error:Field validation for 'TxId' failed on the 'required' tag"));

        log.info("PlatformAddress为空");
        response10 = scf.FinacingBack(UID,"", platformKeyID, PIN, supplyID2, "dPvql1mJ/ubwpTsPiP6fUzSjSIg6KmasC8htnpSIlrQ=", comments);
        assertEquals("400", JSONObject.fromObject(response10).getString("state"));
        assertEquals(true, response10.contains("'PlatFormAddress' Error:Field validation for 'PlatFormAddress' failed on the 'len' tag"));

        log.info("PlatformkeyID为空");
        response10 = scf.FinacingBack(UID,PlatformAddress, "", PIN, supplyID2, "aaaaa", comments);
        assertEquals("400", JSONObject.fromObject(response10).getString("state"));
        assertEquals(true, response10.contains("'PlartformKeyID' Error:Field validation for 'PlartformKeyID' failed on the 'len' tag"));

        log.info("PIN为空");
        response10 = scf.FinacingBack(UID,PlatformAddress,platformKeyID, "", supplyID2, "dPvql1mJ/ubwpTsPiP6fUzSjSIg6KmasC8htnpSIlrQ=", comments);
        assertEquals("400", JSONObject.fromObject(response10).getString("state"));
        assertEquals(true, response10.contains("'PIN' Error:Field validation for 'PIN' failed on the 'required' tag"));

        log.info("keyid为空");
        response10 = scf.FinacingBack(UID,PlatformAddress, platformKeyID, PIN, "", "dPvql1mJ/ubwpTsPiP6fUzSjSIg6KmasC8htnpSIlrQ=", comments);
        assertEquals("400", JSONObject.fromObject(response10).getString("state"));
        assertEquals(true, response10.contains("'KeyID' Error:Field validation for 'KeyID' failed on the 'len' tag"));

        log.info("Comments为空");
        response10 = scf.FinacingBack(UID,PlatformAddress, platformKeyID, PIN, supplyID2, "dPvql1mJ/ubwpTsPiP6fUzSjSIg6KmasC8htnpSIlrQ=", "");
        assertEquals("400", JSONObject.fromObject(response10).getString("state"));
        assertEquals(true, response10.contains("'Comments' Error:Field validation for 'Comments' failed on the 'required' tag"));

    }
    /**
     *修改账户信息通知接口必填参数验证//ff
     */
    @Test
    public void Test0030_AccountInform() throws Exception {

        log.info("Comments为空");
        String response10 = scf.AccountInform(PlatformAddress,"");
        assertEquals("400", JSONObject.fromObject(response10).getString("state"));
        assertEquals(true, response10.contains("'Comments' Error:Field validation for 'Comments' failed on the 'required' tag"));

        log.info("comments为空");
        response10 = scf.AccountInform("",comments);
        assertEquals("400", JSONObject.fromObject(response10).getString("state"));
        assertEquals(true, response10.contains("'ContractAddress' Error:Field validation for 'ContractAddress' failed on the 'len' tag"));

    }

    /**
     *授信二度调整必填参数验证//ff
     */
    @Test
    public void Test0030_C() throws Exception {

        log.info("contractAddress为空");
        String response10 = scf.CreditAdjust("",companyID1,"1000000");
        assertEquals("400", JSONObject.fromObject(response10).getString("state"));
        assertEquals(true, response10.contains("'ContractAddress' Error:Field validation for 'ContractAddress' failed on the 'len' tag"));

        log.info("companyID为空");
        response10 = scf.CreditAdjust(AccountAddress,"","1000000");
        assertEquals("400", JSONObject.fromObject(response10).getString("state"));
        assertEquals(true, response10.contains("'CompanyID' Error:Field validation for 'CompanyID' failed on the 'required' tag"));

        log.info("amount为空");
        response10 = scf.CreditAdjust(AccountAddress,companyID1,"");
        assertEquals("400", JSONObject.fromObject(response10).getString("state"));
        assertEquals(true, response10.contains("'Amount' Error:Field validation for 'Amount' failed on the 'min' tag"));

    }

    /**
     *发送事件通知必填参数验证//ff
     */
    @Test
    public void Test0031_send() throws Exception {

        log.info("comments空");
        String response10 = scf.Send("");
        assertEquals("500", JSONObject.fromObject(response10).getString("state"));
        assertEquals(true, response10.contains("'Comments' Error:Field validation for 'Comments' failed on the 'required' tag"));

        log.info("comment不合法//格式不正确//不是由接口加密生成");
        response10 = scf.Send("1234");
        assertEquals("500", JSONObject.fromObject(response10).getString("state"));
        assertEquals(true, response10.contains("comments不合法!(需要通过post http://ip:port/scf/func/sendmsg生成的数据!)"));
    }

    /**
     *获取历史交易交易id必填参数验证//ff
     */
    @Test
    public void Test0031_FuncGethistory() throws Exception {

        log.info("Txid不存在//不匹配utxo交易");
        String response10 = scf.FuncGethistory("1234345");
        assertEquals("500", JSONObject.fromObject(response10).getString("state"));
        assertEquals(true, response10.contains("invalid txId illegal base64 data at input byte 4"));
    }
}

