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
import static com.tjfintech.common.utils.UtilsClassTap.ORDERNO;
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

        BID_DOC_REFER_END_TIME = constructTime(20000);
        KAIBIAODATE = constructTime(20000);
        log.info(ZBRPULICKEY);
        String response = tap.tapProjectInit(TENDER_PROJECT_CODE, TENDER_PROJECT_NAME, BID_SECTION_NAME, BID_SECTION_CODE, KAIBIAODATE,
                BID_DOC_REFER_END_TIME, "1", "jstf", TBALLOWFILESIZE, "1.0",
                "1.0", ZBRPULICKEY, BID_SECTION_CODE_EX, EXTRA);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        ORDERNO = JSONObject.fromObject(response).getJSONObject("data").getString("ORDERNO");

    }

    public String initProject() throws Exception {

        BID_DOC_REFER_END_TIME = constructTime(20000);
        KAIBIAODATE = constructTime(30000);
        TENDER_PROJECT_CODE = constructData("PC");
        TENDER_PROJECT_NAME = constructData("项目");
        BID_SECTION_CODE = constructData("SC");
        BID_SECTION_NAME = constructData("标段");
        BID_SECTION_CODE_EX = constructData("SC_EX/+==");
        UID = constructData("UID");
        String response = tap.tapProjectInit(TENDER_PROJECT_CODE, TENDER_PROJECT_NAME, BID_SECTION_NAME, BID_SECTION_CODE, KAIBIAODATE,
                BID_DOC_REFER_END_TIME, "1", "jstf", TBALLOWFILESIZE, "1.0",
                "1.0", ZBRPULICKEY, BID_SECTION_CODE_EX, EXTRA);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        String txid = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        String ORDERNO = JSONObject.fromObject(response).getJSONObject("data").getString("ORDERNO");
        commonFunc.verifyTxDetailField(txid, "", "2", "3", "42");
        return ORDERNO;

    }


    //循环初始化项目并且上传投标文件，获取项目标识和签名数据，批量开标
    @Test
    public void tapTenderOpenBatchTest() throws Exception {

        List<Map> listmap = tapProjectInitBatchTest();
        sleepAndSaveInfo(10 * 1000);
        for (Map<String, String> map : listmap) {
            String projectid = "";
            for (String k : map.keySet()) {
                Object ob = map.get(k);
                System.out.println(k + ":" + ob);
                projectid = map.get(k);
            }
            String response = tap.tapTenderOpen(projectid);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
        }

    }

    public List<Map> tapProjectInitBatchTest() throws Exception {

        List<Map> listmap = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Map map = new HashMap();
            String ORDERNO = initProject();
            tap.tapProjectUpdate(ORDERNO, "", "",
                    "", "", constructTime(40000), constructTime(40000),
                    "", "", 0, "",
                    "", "", null);
            map.put("orderNo", ORDERNO);
            listmap.add(map);

            for (int k = 0; k < 20; k++) {
                UID = constructData("UID");
                String recordId = "tender" + UtilsClass.Random(8);
                String response = tap.tapTenderUpload(ORDERNO, UID, recordId, fileHead, path, constructUnixTime(0));
                assertEquals("200", JSONObject.fromObject(response).getString("state"));
                log.info("------------upload"+(k+1)+"success--------------");
            }
            log.info("------------init"+(i+1)+"success--------------");
        }
        log.info(listmap.toString());
        return listmap;

    }


}