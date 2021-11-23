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
        ZBRPULICKEY = certTool.tapPubToHex(sdkIP, PRIKEY1, "", "", "");
        log.info(ZBRPULICKEY);
        String response = tap.tapProjectInit(TENDER_PROJECT_CODE, TENDER_PROJECT_NAME, BID_SECTION_NAME, BID_SECTION_CODE, KAIBIAODATE,
                BID_DOC_REFER_END_TIME, "1", "jstf", TBALLOWFILESIZE, "1.0",
                "1.0", ZBRPULICKEY, BID_SECTION_CODE_EX, EXTRA);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        ORDERNO = JSONObject.fromObject(response).getJSONObject("data").getString("ORDERNO");
        ORDERNOSIGN = certTool.tapSign(sdkIP, "ORDERNO", "", ORDERNO, "");
        orderNoSIGN = certTool.tapSign(sdkIP, "orderNo", "", ORDERNO, "");

    }

    public String initProject() throws Exception {

        BID_DOC_REFER_END_TIME = constructTime(20000);
        KAIBIAODATE = constructTime(20000);
        ZBRPULICKEY = certTool.tapPubToHex(sdkIP, PRIKEY1, "", "", "");
        log.info(ZBRPULICKEY);
        String response = tap.tapProjectInit(TENDER_PROJECT_CODE, TENDER_PROJECT_NAME, BID_SECTION_NAME, BID_SECTION_CODE, KAIBIAODATE,
                BID_DOC_REFER_END_TIME, "1", "jstf", TBALLOWFILESIZE, "1.0",
                "1.0", ZBRPULICKEY, BID_SECTION_CODE_EX, EXTRA);
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
        String txid = JSONObject.fromObject(response).getJSONObject("data").getString("txId");
        String ORDERNO = JSONObject.fromObject(response).getJSONObject("data").getString("ORDERNO");
        ORDERNOSIGN = certTool.tapSign(sdkIP, "ORDERNO", "", ORDERNO, "");//更新接口请求字段为ORDERNO=
        orderNoSIGN = certTool.tapSign(sdkIP, "orderNo", "", ORDERNO, "");//开标、获取投标信息列表接口请求字段为orderNo=
        commonFunc.verifyTxDetailField(txid, "", "2", "3", "42");
        return ORDERNO;

    }


    //循环初始化项目并且上传投标文件，获取项目标识和签名数据，批量开标
    @Test
    public void tapTenderOpenBatchTest() throws Exception {

        List<Map> listmap = tapProjectInitBatchTest();
        sleepAndSaveInfo(20*1000);
        for (Map<String, String> map : listmap) {
            String projectid = "";
            String sign = "";
            for (String k : map.keySet()) {
                Object ob = map.get(k);
                System.out.println(k + ":" + ob);
                if (k == "sign") {
                    sign = map.get(k);
                } else
                    projectid = map.get(k);
            }
            String response = tap.tapTenderOpen(projectid, sign);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
        }

    }

    public List<Map> tapProjectInitBatchTest() throws Exception {

        List<Map> listmap = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Map map = new HashMap();
            String ORDERNO = initProject();
            ORDERNOSIGN = certTool.tapSign(sdkIP, "ORDERNO", "", ORDERNO, "");
            orderNoSIGN = certTool.tapSign(sdkIP, "orderNo", "", ORDERNO, "");
            map.put("orderNo", ORDERNO);
            map.put("sign", orderNoSIGN);
            listmap.add(map);

            for (int k = 0; k < 20; k++) {
                String recordId = "tender" + UtilsClass.Random(8);
                String response = tap.tapTenderUpload(ORDERNO, recordId, fileHead, path);
                assertEquals("200", JSONObject.fromObject(response).getString("state"));
            }

        }
        log.info(listmap.toString());
        return listmap;

    }


}