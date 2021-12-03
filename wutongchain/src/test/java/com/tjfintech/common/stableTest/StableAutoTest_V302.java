package com.tjfintech.common.stableTest;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.Interface.Token;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.functionTest.mixTest.VerifyTests;
import com.tjfintech.common.functionTest.smartTokenTest.SmartTokenCommon;
import com.tjfintech.common.utils.GetTest;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.junit.Assert.assertEquals;


@Slf4j
public class StableAutoTest_V302 {
    TestBuilder testBuilder = TestBuilder.getInstance();
    Store store = testBuilder.getStore();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();

    @BeforeClass
    public static void beforeConfig() throws Exception {

    }


    /**
     *  V3.0.2 节点内存是否溢出测试
     */
    @Test
    public void OutOfMemoryTest_V302() throws Exception {

        int i = 0;
        int loop = 10000; // 循环次数

        while (i < loop) {
            bigStoreTest("") ;
            i++;
//            Thread.sleep(200);
        }
    }

    /**
     * V3.0.2  事件稳定性测试
     */
    @Test
    public void EventStableTest_V302() throws Exception {

        int i = 0;
        int loop = 1000; // 循环次数
        int total = 0;

        while (i < loop) {

            if ( storeTest("") == 200 ){
                total++;
            };
            i++;

            Thread.sleep(SHORTMEOUT); // 3秒
            log.info("i ===================== " + i);
            if ( i % 10 == 0) {
                utilsClass.setAndRestartPeer(PEER2IP);
            }
        }

        commonFunc.sdkCheckTxOrSleep(storeHash, utilsClass.sdkGetTxDetailType, SLEEPTIME);
        Thread.sleep(SLEEPTIME);

        Thread.sleep(120 * 1000); // 120秒
        syncFlag = true;
        assertEquals("事件不稳定", storeTest(""), 200);

    }

    // 普通存证
    public int bigStoreTest(String id) throws Exception {
        subLedger = id;
        String Data = UtilsClass.Random(10) + utilsClass.readStringFromFile(testDataPath
                + "store/bigsize3.txt");
        String response = store.CreateStore(Data);

        return JSONObject.fromObject(response).getInt("state");
    }

    // 普通存证
    public int storeTest(String id) throws Exception {
        subLedger = id;
        JSONObject fileInfo = new JSONObject();
        JSONObject data = new JSONObject();

        fileInfo.put("fileName", "201911041058.jpg");
        fileInfo.put("fileSize", "298KB");
        fileInfo.put("fileModel", "iphoneXR");

        data.put("projectCode", UtilsClass.Random(10));
        data.put("waybillId", "1260");
        data.put("fileInfo", fileInfo);
        String Data = data.toString();

        String response = store.CreateStore(Data);
        return JSONObject.fromObject(response).getInt("state");
    }


}
