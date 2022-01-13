package com.tjfintech.common.stableTest;

import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.CommonFunc;
import com.tjfintech.common.Interface.*;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.functionTest.mixTest.VerifyTests;
import com.tjfintech.common.functionTest.smartTokenTest.SmartTokenCommon;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.checkerframework.checker.units.qual.C;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;


@Slf4j
public class Temp_Test {
    TestBuilder testBuilder = TestBuilder.getInstance();
    Store store = testBuilder.getStore();
    Contract contract = testBuilder.getContract();
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
                if (code == 200) break;
                Thread.sleep(sleep);
            } while (code != 200);

            times[j] = i * sleep;

        }

        int i = 0;
        for (int t : times) {
            if (t > sleep) {
                log.info("查询到结果耗时 " + (500 + t) + " 毫秒");
                i++;
            }
        }
        log.info("一共 " + i + "  次超时");
    }

    /**
     * 循环执行WVM合约调用，写入数据
     */
    @Test
    public void WVMContractInvokeBatch() throws Exception {

        String file = "Ly/ouqvku73orqTor4HlkIjnuqYKY29udHJhY3QgSWRlbnRpdHkgewoKICAgIC8v6Lqr5Lu957uT5p6E5L2TCiAgICBzdHJ1Y3QgVXNlckluZm8gewoKICAgICAgICBzdHJpbmcgTmFtZSAgICAgICAgIC8v5aeT5ZCNCiAgICAgICAgc3RyaW5nIElEICAgICAgICAgICAvL+i6q+S7veivgeWPtwogICAgICAgIHN0cmluZyBBZ2UgICAgICAgICAgLy/lubTpvoQKICAgICAgICBzdHJpbmcgRGF0YSAgICAgICAgIC8v5pWw5o2uCgogICAgfQogIC8v5re75Yqg55So5oi35L+h5oGvCiAgICBwdWJsaWMgc3RyaW5nIEFkZFVzZXJJbmZvIChzdHJpbmcgdXNlckluZm9Kc29uKSB7CgogICAgICAgIFVzZXJJbmZvIHVpbmZvID0ganNvbl90b19vYmo8VXNlckluZm8+KHVzZXJJbmZvSnNvbikKICAgICAgICBzdHJpbmcga2V5ID0gInVzZXJfIit1aW5mby5JRCAKICAgICAgICBkYl9zZXQoa2V5LHVzZXJJbmZvSnNvbikKICAgICAgICBwcmludChrZXkrdXNlckluZm9Kc29uKQogICAgICAgIHJldHVybigic3VjY2VzcyIpCgogICAgfQogICAgLy/ojrflj5bnlKjmiLfkv6Hmga8KICAgIHB1YmxpYyBzdHJpbmcgR2V0VXNlckluZm8oc3RyaW5nIGtleSkgewogICAgICAgIHN0cmluZyBpbmZvID0gZGJfZ2V0PHN0cmluZz4oa2V5KQogICAgICAgIHByaW50KGluZm8pCiAgICAgICAgcmV0dXJuIGluZm8gCiAgICB9CiAgICAvL+WQiOe6puWIneWni+WMlgogICAgcHVibGljIHN0cmluZyBpbml0KCkgewogICAgICAgIHJldHVybiAiaW5pdCBzdWNjZXNzIgogICAgfQoKCn0=";
        String response = contract.InstallWVM(file, "wvm", "");
        assertEquals("200", JSONObject.fromObject(response).getString("state"));
        String contractName = JSONObject.fromObject(response).getJSONObject("data").getString("name");
        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);

        List<Object> args = new LinkedList<>();
        int i = 10;
        for (int j = 1; j < i; j++) {
            String ID = System.currentTimeMillis() + UtilsClass.Random(6);
            String arg = " {\"Name\":\"123\",\"ID\":\"" + ID + "\",\"Age\":\"20\",\"Data\":\"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaabbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaabbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaabbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaabbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaabbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaabbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaabbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaabbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbcccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc\"}";

            args.clear();
            args.add(arg);
            response = contract.Invoke(contractName, "", "wvm", "AddUserInfo", args);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse, utilsClass.sdkGetTxHashType20),
                    utilsClass.sdkGetTxDetailTypeV2, SLEEPTIME);
            args.clear();
            args.add("user_" + ID);
            response = contract.QueryWVM(contractName, "", "wvm", "GetUserInfo", "", args);
            assertEquals("200", JSONObject.fromObject(response).getString("state"));
            assertEquals(true, response.contains(ID));
            log.info("---------------------invoke " + j + " success-------------------------");
        }
    }
}