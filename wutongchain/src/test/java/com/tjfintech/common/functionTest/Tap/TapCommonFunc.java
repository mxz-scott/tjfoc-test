package com.tjfintech.common.functionTest.Tap;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CertTool;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.Interface.Tap;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.MinIOOperation;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;

import static com.tjfintech.common.utils.UtilsClass.*;
import static com.tjfintech.common.utils.UtilsClassTap.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;


@Slf4j
public class TapCommonFunc {
    TestBuilder testBuilder = TestBuilder.getInstance();
    Store store = testBuilder.getStore();
    Tap tap = testBuilder.getTap();
    CertTool certTool = new CertTool();
    CommonFunc commonFunc = new CommonFunc();
    UtilsClass utilsClass = new UtilsClass();


//    @BeforeClass
//    public static void init() throws Exception {
//        BeforeCondition bf = new BeforeCondition();
//        bf.updatePubPriKey();
//    }


    public void init() throws Exception {

        expireDate = System.currentTimeMillis() / 1000 + 20;
        openDate = System.currentTimeMillis() / 1000 + 20;
        String sdkIP = SDKADD.substring(SDKADD.lastIndexOf("/") + 1, SDKADD.lastIndexOf(":"));
        publicKey = certTool.tapPubToHex(sdkIP, PRIKEY1, "", "", "");
        log.info(publicKey);
        String response = tap.tapProjectInit(expireDate, openDate, publicKey, identity, filesize, name, metaData);
        projectId = JSONObject.fromObject(response).getJSONObject("data").getString("projectId");
        log.info(projectId);
        sign = certTool.tapSign(sdkIP, PRIKEY1, "", projectId, "");
        log.info(sign);

    }

    public String initProject() throws Exception {

        expireDate = System.currentTimeMillis() / 1000 + 20;
        openDate = System.currentTimeMillis() / 1000 + 20;
        String sdkIP = SDKADD.substring(SDKADD.lastIndexOf("/") + 1, SDKADD.lastIndexOf(":"));
        publicKey = certTool.tapPubToHex(sdkIP, PRIKEY1, "", "", "");
        log.info(publicKey);
        String response = tap.tapProjectInit(expireDate, openDate, publicKey, identity, filesize, name, metaData);
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        String txid = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        String projectid = JSONObject.fromObject(response).getJSONObject("data").getString("projectId");
        commonFunc.verifyTxDetailField(txid, "", "2", "3", "42");
        return projectid;


    }


    public void pubTest() throws Exception {
        String publicKeyHex = certTool.tapPubToHex("10.1.3.153", PRIKEY1, "", "", "");
        log.info(publicKeyHex);

    }

}