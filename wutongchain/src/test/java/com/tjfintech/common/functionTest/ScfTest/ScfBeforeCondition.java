package com.tjfintech.common.functionTest.ScfTest;

import com.tjfintech.common.Interface.Kms;
import com.tjfintech.common.Interface.Scf;
import com.tjfintech.common.MgToolCmd;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClassKMS;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Test;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.Contract;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.functionTest.Conditions.SetSDKPerm999;
import com.tjfintech.common.utils.FileOperation;
import com.tjfintech.common.utils.UtilsClass;
import com.tjfintech.common.utils.UtilsClassScf;

import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.After;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import com.tjfintech.common.functionTest.contract.WVMContractTest;

import java.util.*;
import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClass.SLEEPTIME;
import static com.tjfintech.common.utils.UtilsClass.globalResponse;
import static com.tjfintech.common.utils.UtilsClassKMS.*;
import static com.tjfintech.common.utils.UtilsClassScf.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import java.util.Base64;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)

@Slf4j
public class ScfBeforeCondition {
    TestBuilder testBuilder = TestBuilder.getInstance();
    Scf scf = testBuilder.getScf();
    WVMContractTest wvm = new WVMContractTest();
    CommonFunc commonFunc = new CommonFunc();
    UtilsClass utilsClass = new UtilsClass();
    UtilsClassScf utilsClassScf = new UtilsClassScf();
    UtilsClassKMS UtilsClassKMS = new UtilsClassKMS();
    Kms kms = testBuilder.getKms();
    FileOperation fileOper = new FileOperation();
    String wvmFile = "account";

    /***
     * ??????????????????
     *
     *
     */
    @Test
    public void B001_createPlatformAccount() throws  Exception {

        String response = kms.createKey(keySpecSm2, password, pubFormatSM2);
        assertThat(response, containsString("200"));

        platformKeyID = JSONObject.fromObject(response).getJSONObject("data").getString("keyId");
        platformPubkey = JSONObject.fromObject(response).getJSONObject("data").getString("publicKey");


        platformPubkeyPem = utilsClassScf.decodeBase64(platformPubkey);

        log.info(platformKeyID);
        log.info(platformPubkeyPem);
        log.info(platformPubkey);

    }

    /***
     * ????????????????????????
     *
     *
     */
    @Test
    public void B002_createCoreCompanyAccount() throws  Exception {

        //?????????????????????
        String response1 = wvm.ScfwvmInstallTest("platform.wlang","use sdk prikey");
        String txHash1 = JSONObject.fromObject(response1).getJSONObject("data").getString("txId");
        utilsClassScf.PlatformAddress = JSONObject.fromObject(response1).getJSONObject("data").getString("name");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        wvm.chkTxDetailRsp("200",txHash1);

        //String commmets = utilsClassScf.generateMessage();

        String response = scf.AccountCreate( PlatformAddress, platformKeyID, PIN, "",comments);
        assertThat(response, containsString("200"));

        coreCompanyKeyID = JSONObject.fromObject(response).getString("keyID");
        coreCompanyPubkeyPem = JSONObject.fromObject(response).getString("pubKey");
        coreCompanyAddress = JSONObject.fromObject(response).getString("data");

        //        log.info(coreCompanyPubkeyPem);

    }


    /***
     * ????????????
     *
     *
     */
    @Test
    public void B003_installContracts() throws  Exception {



        //??????????????????

        log.info(platformPubkeyPem);
        log.info(coreCompanyPubkeyPem);

        fileOper.replace(tempWVMDir + wvmFile + ".wlang", "PlatformPubkey", platformPubkeyPem);
        fileOper.replace(tempWVMDir + wvmFile + "_temp.wlang", "CoreEnterprisePubkey", coreCompanyPubkeyPem);

        String response1 = wvm.ScfwvmInstallTest(wvmFile + "_temp_temp.wlang","use sdk prikey");
        String txHash1 = JSONObject.fromObject(response1).getJSONObject("data").getString("txId");
        utilsClassScf.AccountAddress = JSONObject.fromObject(response1).getJSONObject("data").getString("name");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        wvm.chkTxDetailRsp("200",txHash1);



        //????????????????????????
        response1 = wvm.ScfwvmInstallTest("QFJG.wlang","use sdk prikey");
        txHash1 = JSONObject.fromObject(response1).getJSONObject("data").getString("txId");
        utilsClassScf.QFJGAddress = JSONObject.fromObject(response1).getJSONObject("data").getString("name");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        wvm.chkTxDetailRsp("200",txHash1);

        //?????????????????????
        response1 = wvm.ScfwvmInstallTest("ZJF.wlang","use sdk prikey");
        txHash1 = JSONObject.fromObject(response1).getJSONObject("data").getString("txId");
        utilsClassScf.ZJFAddress = JSONObject.fromObject(response1).getJSONObject("data").getString("name");

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2,SLEEPTIME);

        wvm.chkTxDetailRsp("200",txHash1);

        //??????????????????????????????
        MgToolCmd mgToolCmd = new MgToolCmd();
        String permitStr = "everyone";

        //???????????????????????????????????????everyone??????
        mgToolCmd.contractFuncPermit(PEER1IP,PEER1RPCPort,subLedger,utilsClassScf.QFJGAddress,"","GetProofInfo",permitStr);
        mgToolCmd.contractFuncPermit(PEER1IP,PEER1RPCPort,subLedger,utilsClassScf.QFJGAddress,"","PutFeedback",permitStr);

        //????????????????????????????????????everyone??????
        mgToolCmd.contractFuncPermit(PEER1IP,PEER1RPCPort,subLedger,utilsClassScf.ZJFAddress,"","GetProofInfo",permitStr);
        mgToolCmd.contractFuncPermit(PEER1IP,PEER1RPCPort,subLedger,utilsClassScf.ZJFAddress,"","PutFeedback",permitStr);
        mgToolCmd.contractFuncPermit(PEER1IP,PEER1RPCPort,subLedger,utilsClassScf.ZJFAddress,"","Test",permitStr);

        //?????????????????????????????????everyone??????
        mgToolCmd.contractFuncPermit(PEER1IP,PEER1RPCPort,subLedger,utilsClassScf.PlatformAddress,"","CreateAuthoritys",permitStr);
        mgToolCmd.contractFuncPermit(PEER1IP,PEER1RPCPort,subLedger,utilsClassScf.PlatformAddress,"","GetSecurityKey",permitStr);
        sleepAndSaveInfo(SLEEPTIME/2);

    }



    /***
     * ?????????????????????
     *
     *
     */
    @Test
    public void B004_createSupplyAccounts() throws  Exception {

//        String commmets = utilsClassScf.generateMessage();

        String response1 = scf.AccountCreate(PlatformAddress, platformKeyID, PIN, "",comments);
        supplyAddress1 = JSONObject.fromObject(response1).getString("data");
        supplyID1 = JSONObject.fromObject(response1).getString("keyID");
        supplierMsg1 = JSONObject.fromObject(response1).getString("txId");

        String response2 = scf.AccountCreate(PlatformAddress, platformKeyID, PIN, "",comments);
        supplyAddress2 = JSONObject.fromObject(response2).getString("data");
        supplyID2 = JSONObject.fromObject(response2).getString("keyID");
        supplierMsg2 = JSONObject.fromObject(response2).getString("txId");

        String response3 = scf.AccountCreate(PlatformAddress,platformKeyID, PIN, "",comments);
        supplyAddress3 = JSONObject.fromObject(response3).getString("data");
        supplyID3 = JSONObject.fromObject(response3).getString("keyID");
        supplierMsg3 = JSONObject.fromObject(response3).getString("txId");
    }
//    /***
//     * ??????comments
//     *
//     *
//     */
//    public void Getcomments() throws  Exception {
//
//        List<Map> list = new ArrayList<>(10);
//        log.info("$$$$$$$$$$$$$$$$$$$$"+platformPubkey);
//        List<Map> list1 = UtilsClassScf.Sendmsg("1", platformPubkey, list);
//        String comments0 = scf.SendMsg("finance","abc", platformKeyID,list1,"","","123");
//        comments = JSONObject.fromObject(comments0).getString("data");
//
//    }

    /***
     * ??????commentsV2
     */
    public void GetcommentsV2() throws  Exception {
        List<Map> list = new ArrayList<>(10);
        log.info("$$$$$$$$$$$$$$$$$$$$"+platformPubkey);
        List<Map> list1 = UtilsClassScf.SendMsgV2("sztj", platformPubkey, list);
        String comments0 = scf.SendMsgV2("aaa","sztj",list,"123");
        comments = JSONObject.fromObject(comments0).getString("data");
    }

}
