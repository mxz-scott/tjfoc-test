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
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;


@Slf4j
public class Temp_Test {
    TestBuilder testBuilder = TestBuilder.getInstance();
    Store store = testBuilder.getStore();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();

    @BeforeClass
    public static void beforeConfig() throws Exception {
        BeforeCondition bf = new BeforeCondition();
        bf.updatePubPriKey();
        Thread.sleep(2000);

    }


    /**
     * 隐私存证快速查询报错问题调查
     * 合约数据写入变慢导致
     */
    @Test
    public void priStoreQuery() throws Exception {

        int sleep = 100;
        int length = 50;
        int[] times = new int[length];

        for (int j = 0; j < length; j++) {
            String Data = "cxTest-private" + UtilsClass.Random(7);
            Map<String, Object> map = new HashMap<>();
            map.put("pubKeys", PUBKEY1);
            map.put("pubkeys", PUBKEY6);
            String response1 = store.CreatePrivateStore(Data, map);
            JSONObject jsonObject = JSONObject.fromObject(response1);
            String StoreHashPwd = jsonObject.getString("data");

            commonFunc.sdkCheckTxOrSleepNoDBQuery(StoreHashPwd, utilsClass.sdkGetTxDetailType, SHORTMEOUT);

            int code = 200;
            int i = 0;
            do {
                String response2 = store.GetStorePost(StoreHashPwd, PRIKEY1);
                code = JSONObject.fromObject(response2).getInt("state");
                i++;
                if ( code == 200 ) break;
                Thread.sleep(sleep);
            }while (code != 200);

            times[j] = i * sleep;

        }

        int i = 0;
        for (int t : times) {
            if (t > sleep ){
                log.info("查询到结果耗时 " + (500 + t) + " 毫秒");
                i++;
            }
        }
        log.info("一共 " + i + "  次超时");
    }
}