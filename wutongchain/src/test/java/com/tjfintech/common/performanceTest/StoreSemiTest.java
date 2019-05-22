package com.tjfintech.common.performanceTest;

import com.tjfintech.common.Interface.MultiSign;
import com.tjfintech.common.Interface.SoloSign;
import com.tjfintech.common.Interface.Store;

import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import java.util.*;

//import static com.tjfintech.common.functionTest.store.StoreTest.SLEEPTIME;
import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StoreSemiTest {

    public static String tokenType;
    public static String tokenType2;

    @Before
    public void beforeConfig() throws Exception {

    }
    public static void main(String[] args) throws  Exception {
        TestBuilder testBuilder= TestBuilder.getInstance();
        Store store =testBuilder.getStore();


        Map<String,Object> modelMap=new HashMap<>();
        tokenType = "StoreTc-" + UtilsClass.Random(6);
        modelMap.put("tokenType",tokenType);
        String modelStr  = modelMap.toString();
        Map<String,Object> keyMap=new HashMap<>();
        keyMap.put("key",PUBKEY1);
        String result = store.CreateStorePwd(modelStr,keyMap);
        JSONObject jsonObject=JSONObject.fromObject(result);
        String storeHash = jsonObject.getJSONObject("Data").get("Figure").toString();
        assertEquals(storeHash.isEmpty(),false);

        Map<String,Object> modelMap1=new HashMap<>();
        tokenType2 = "StoreTc-" + UtilsClass.Random(6);
        modelMap1.put("tokenType",tokenType2);

        String modelStr1  = modelMap1.toString();
        Map<String,Object> keyMap1=new HashMap<>();
        keyMap1.put("key",PUBKEY1);
        String result1 = store.CreateStorePwd(modelStr1,keyMap1);
        JSONObject jsonObject1=JSONObject.fromObject(result1);
        String storeHash1 = jsonObject1.getJSONObject("Data").get("Figure").toString();
        assertEquals(storeHash1.isEmpty(),false);
        Thread.sleep(SLEEPTIME);
        log.info("将SDK里面不配置解密的私钥或配置错误的私钥");
        Scanner scanner = new Scanner(System.in);
        System.out.println(scanner.nextLine());
        String response=store.GetTxSearch(0,5,tokenType);
        assertThat(response,containsString("200"));
        jsonObject1=JSONObject.fromObject(response);
        storeHash1 = jsonObject1.getJSONObject("Data").toString();
        assertThat(storeHash1,containsString(""));

        String response1=store.GetTxSearch(0,5,tokenType2);
        jsonObject1=JSONObject.fromObject(response);
        storeHash1 = jsonObject1.getJSONObject("Data").toString();
        assertThat(storeHash1,containsString(""));
        assertThat(response1,containsString("200"));
        log.info("将SDK里面配置正确解密的私钥");
         scanner = new Scanner(System.in);
        System.out.println(scanner.nextLine());
         response=store.GetTxSearch(0,5,tokenType);
        assertThat(response,containsString("200"));
        assertThat(response,containsString(tokenType));
         response=store.GetTxSearch(0,5,tokenType2);
            assertThat(response1,containsString("200"));
        assertThat(response1,containsString(tokenType2));
        assertThat(response1,containsString("200"));


    }
}
