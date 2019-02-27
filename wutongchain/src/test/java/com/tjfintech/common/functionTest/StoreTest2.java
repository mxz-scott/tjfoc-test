package com.tjfintech.common.functionTest;

import com.bw.base.PrivacyPolicyStore;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class StoreTest2 {

    public final static int SLEEPTIME = 5 * 1000;
    TestBuilder testBuilder = TestBuilder.getInstance();
    Store store = testBuilder.getStore();
    PrivacyPolicyStore priStore = new PrivacyPolicyStore();

    @Test
    public void TC09_createPriStore() throws Exception {
        String Data = "\"test123\":\"json" + UtilsClass.Random(4) + "\"";
        log.info(Data);
        Map<String, Object> map = new HashMap<>();
        map.put("pubKey1", PUBKEY1);
        map.put("pubKey2", PUBKEY2);
        map.put("pubKey3", PUBKEY3);

        String response1 = store.CreateStorePwd(Data, map);
        JSONObject jsonObject = JSONObject.fromObject(response1);
        String StoreHash = jsonObject.getJSONObject("Data").get("Figure").toString();
        Thread.sleep(SLEEPTIME);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("Data"));

        String txContent2 = store.GetStore2(StoreHash);

        JSONObject jsonObject2 = JSONObject.fromObject(txContent2);
        String data = jsonObject2.getJSONObject("Data").toString();
//        log.info("tx:" + data);


        String result1 = priStore.privacyPolicyStoreMethod(data, PRIKEY1PATH);
        String result2 = priStore.privacyPolicyStoreMethod(data, PRIKEY2PATH);
        String result3 = priStore.privacyPolicyStoreMethod(data, PRIKEY3PATH);

        log.info("私钥1解密结果：" + result1);
        log.info("私钥2解密结果：" + result2);
        log.info("私钥3解密结果：" + result3);

        assertThat(result1, containsString(Data));
        assertThat(result2, containsString(Data));
        assertThat(result3, containsString(Data));

        assertThat(result1, containsString("test123"));
        assertThat(result2, containsString("test123"));
        assertThat(result3, containsString("test123"));

    }


    @Test
    public void TC09_createPriStore2() throws Exception {
        String Data = "test" + UtilsClass.Random(4);
        log.info(Data);
        Map<String, Object> map = new HashMap<>();
        map.put("pubKey1", PUBKEY1);

        String response1 = store.CreateStorePwd(Data, map);
        JSONObject jsonObject = JSONObject.fromObject(response1);
        String StoreHash = jsonObject.getJSONObject("Data").get("Figure").toString();
        Thread.sleep(SLEEPTIME);
        assertThat(response1, containsString("200"));
        assertThat(response1, containsString("Data"));

        String txContent2 = store.GetStore2(StoreHash);

        JSONObject jsonObject2 = JSONObject.fromObject(txContent2);
        String data = jsonObject2.getJSONObject("Data").toString();
//        log.info("tx:" + data);

        String result1 = priStore.privacyPolicyStoreMethod(data, PRIKEY1PATH);
        String result2 = priStore.privacyPolicyStoreMethod(data, PRIKEY2PATH);

        log.info("私钥1解密结果：" + result1);
        log.info("私钥2解密结果：" + result2);

        assertThat(result1, containsString(Data));
        assertThat(result2, containsString("The private key is not matching"));
        assertEquals(result2.contains("test123"), false);

    }


}
