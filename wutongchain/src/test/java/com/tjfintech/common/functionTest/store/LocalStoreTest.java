//package com.tjfintech.common.functionTest.store;
//
//import com.tjfintech.common.BeforeCondition;
//import com.tjfintech.common.Interface.Store;
//import com.tjfintech.common.TestBuilder;
//import com.tjfintech.common.utils.UtilsClass;
//import com.tjfoc.base.PrivacyPolicyStore;
//import lombok.extern.slf4j.Slf4j;
//import net.sf.json.JSONObject;
//import org.junit.BeforeClass;
//import org.junit.FixMethodOrder;
//import org.junit.Test;
//import org.junit.runners.MethodSorters;
//
//import java.util.*;
//
//import static com.tjfintech.common.utils.UtilsClass.*;
//import static com.tjfintech.common.utils.UtilsClass.SLEEPTIME;
//import static org.hamcrest.Matchers.containsString;
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertThat;
//
//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
//@Slf4j
//public class LocalStoreTest {
//
//    public final static int SLEEPTIME = 5 * 1000;
//    TestBuilder testBuilder = TestBuilder.getInstance();
//    Store store = testBuilder.getStore();
//    PrivacyPolicyStore priStore = new PrivacyPolicyStore();
//
//    @BeforeClass
//    public static void beforeClass() throws Exception {
//        if (MULITADD1.isEmpty()) {
//            BeforeCondition bf = new BeforeCondition();
//            bf.updatePubPriKey();
//            Thread.sleep(SLEEPTIME);
//        }
//    }
//
//    @Test
//    public void TC09_createPriStore() throws Exception {
//        String Data = "\"test123\":\"json" + UtilsClass.Random(10) + "\"";
//        log.info(Data);
//        Map<String, Object> map = new HashMap<>();
//        map.put("pubKey1", PUBKEY1);
//        map.put("pubKey2", PUBKEY2);
//        map.put("pubKey3", PUBKEY3);
//
//        String response1 = store.CreatePrivateStore(Data, map);
//        JSONObject jsonObject = JSONObject.fromObject(response1);
//        String StoreHash = jsonObject.getJSONObject("Data").get("Figure").toString();
//        Thread.sleep(SLEEPTIME);
//        assertThat(response1, containsString("200"));
//        assertThat(response1, containsString("Data"));
//
//        String txContent2 = store.GetStoreLocal(StoreHash);
//
//        JSONObject jsonObject2 = JSONObject.fromObject(txContent2);
//        String data = jsonObject2.getJSONObject("Data").toString();
//        log.info("tx:" + data);
//
//
//        String result1 = priStore.privacyPolicyStoreMethod(data, PRIKEY1);
//        String result2 = priStore.privacyPolicyStoreMethod(data, PRIKEY2);
//        String result3 = priStore.privacyPolicyStoreMethod(data, PRIKEY3);
//        String result4 = priStore.privacyPolicyStoreMethod(data, PRIKEY4);
//
//        log.info("??????1???????????????" + result1);
//        log.info("??????2???????????????" + result2);
//        log.info("??????3???????????????" + result3);
//        log.info("??????4???????????????" + result4);
//
//        assertThat(result1, containsString(Data));
//        assertThat(result2, containsString(Data));
//        assertThat(result3, containsString(Data));
//
//        assertThat(result1, containsString("test123"));
//        assertThat(result2, containsString("test123"));
//        assertThat(result3, containsString("test123"));
//        assertThat(result4, containsString("The private key is not matching"));
//        assertEquals(result4.contains("test123"), false);
//    }
//
//
//    //???????????????
//    @Test
//    public void TC09_createPriStore_PWD() throws Exception {
//        String Data = "\"test123\":\"json" + UtilsClass.Random(10) + "\"";
//        log.info(Data);
//        Map<String, Object> map = new HashMap<>();
//        map.put("pubKey1", PUBKEY1);
//        map.put("pubKey6", PUBKEY6);
//        map.put("pubKey7", PUBKEY7);
//
//        String response1 = store.CreatePrivateStore(Data, map);
//        JSONObject jsonObject = JSONObject.fromObject(response1);
//        String StoreHash = jsonObject.getJSONObject("Data").get("Figure").toString();
//        Thread.sleep(SLEEPTIME);
//        assertThat(response1, containsString("200"));
//        assertThat(response1, containsString("Data"));
//
//        String txContent2 = store.GetStoreLocal(StoreHash);
//
//        JSONObject jsonObject2 = JSONObject.fromObject(txContent2);
//        String data = jsonObject2.getJSONObject("Data").toString();
////        log.info("tx:" + data);
//
//
//        String result1 = priStore.privacyPolicyStoreMethod(data, PRIKEY1);
//        String result2 = priStore.privacyPolicyStoreMethod(data, PRIKEY6, PWD6);
//        String result3 = priStore.privacyPolicyStoreMethod(data, PRIKEY7, PWD7);
////        String result4 = priStore.privacyPolicyStoreMethod(data, PRIKEY6, PWD7); //????????????????????????
//        String result5 = priStore.privacyPolicyStoreMethod(data, PRIKEY4); //???????????????
//
//        log.info("??????1???????????????" + result1);
//        log.info("??????2???????????????" + result2);
//        log.info("??????3???????????????" + result3);
////        log.info("??????4???????????????" + result4);
//        log.info("??????5???????????????" + result5);
//
//        assertThat(result1, containsString(Data));
//        assertThat(result2, containsString(Data));
//        assertThat(result3, containsString(Data));
//
//        assertThat(result1, containsString("test123"));
//        assertThat(result2, containsString("test123"));
//        assertThat(result3, containsString("test123"));
////        assertThat(result4, containsString("The private key is not matching"));
//        assertThat(result5, containsString("The private key is not matching"));
//        assertEquals(result5.contains("test123"), false);
//    }
//
//
//
//
//
//
//    @Test
//    public void TC09_createPriStore2() throws Exception {
//        String Data = "test456" + UtilsClass.Random(10);
//        log.info(Data);
//        Map<String, Object> map = new HashMap<>();
//        map.put("pubKey1", PUBKEY1);
//
//        String response1 = store.CreatePrivateStore(Data, map);
//        JSONObject jsonObject = JSONObject.fromObject(response1);
//        String StoreHash = jsonObject.getJSONObject("Data").get("Figure").toString();
//        Thread.sleep(SLEEPTIME);
//        assertThat(response1, containsString("200"));
//        assertThat(response1, containsString("Data"));
//
//        String txContent2 = store.GetStoreLocal(StoreHash);
//
//        JSONObject jsonObject2 = JSONObject.fromObject(txContent2);
//        String data = jsonObject2.getJSONObject("Data").toString();
////        log.info("Data:" + data);
//
//        String result1 = priStore.privacyPolicyStoreMethod(data, PRIKEY1);
//        String result2 = priStore.privacyPolicyStoreMethod(data, PRIKEY2);
//
//        log.info("??????1???????????????" + result1);
//        log.info("??????2???????????????" + result2);
//
//        assertThat(result1, containsString(Data));
//        assertEquals(result1.equals(Data), true);
//        assertThat(result2, containsString("The private key is not matching"));
//        assertEquals(result2.contains("test456"), false);
//
//    }
//
//
//
//    @Test
//    public void TC09_createPriStore_PWD2() throws Exception {
//        String Data = "test789" + UtilsClass.Random(10);
//        log.info(Data);
//        Map<String, Object> map = new HashMap<>();
//        map.put("pubKey1", PUBKEY6);
//
//        String response1 = store.CreatePrivateStore(Data, map);
//        JSONObject jsonObject = JSONObject.fromObject(response1);
//        String StoreHash = jsonObject.getJSONObject("Data").get("Figure").toString();
//        Thread.sleep(SLEEPTIME);
//        assertThat(response1, containsString("200"));
//        assertThat(response1, containsString("Data"));
//
//        String txContent2 = store.GetStoreLocal(StoreHash);
//
//        JSONObject jsonObject2 = JSONObject.fromObject(txContent2);
//        String data = jsonObject2.getJSONObject("Data").toString();
////        log.info("Data:" + data);
//
//        String result1 = priStore.privacyPolicyStoreMethod(data, PRIKEY6, PWD6);
//        String result2 = priStore.privacyPolicyStoreMethod(data, PRIKEY2);
//
//        log.info("??????1???????????????" + result1);
//        log.info("??????2???????????????" + result2);
//
//        assertThat(result1, containsString(Data));
//        assertEquals(result1.equals(Data), true);
//        assertThat(result2, containsString("The private key is not matching"));
//        assertEquals(result2.contains("test789"), false);
//
//    }
//
//
//}
