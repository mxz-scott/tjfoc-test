package com.tjfintech.common.functionTest.store;

import com.tjfintech.common.*;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.tjfintech.common.utils.UtilsClass.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Slf4j
public class SyncStoreTest {

    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store =testBuilder.getStore();
    GoPrivateStore priStore = new GoPrivateStore();
    UtilsClass utilsClass = new UtilsClass();
    CommonFunc commonFunc = new CommonFunc();
    CertTool certTool = new CertTool();
    long onChainTime = 400;

    @BeforeClass
    public static void beforeConfig() throws Exception {
        BeforeCondition bf = new BeforeCondition();
        bf.updatePubPriKey();
        UtilsClass.syncFlag = true;
    }

    /**
     * 普通存证，内容为json
     *
     */
    @Test
    public void TC001_createStore() throws Exception {

        JSONObject fileInfo = new JSONObject();
        JSONObject data = new JSONObject();

        fileInfo.put("fileName", "201911041058.jpg");
        fileInfo.put("fileSize", "298KB");

        data.put("projectCode", UtilsClass.Random(10));
        data.put("waybillId", "1260");
        data.put("fileInfo", fileInfo);

        String Data = data.toString();
        log.info(Data);

        long start = System.currentTimeMillis();
        String response= store.CreateStore(Data);
        long end = System.currentTimeMillis();

        log.info("时间差：" + (end - start));

        assertTrue((end - start) >= onChainTime);
        assertThat(response, containsString("200"));

    }

    /**
     * 创建隐私存证交易，数据格式为Json
     *
     */

    @Test
    public void TC002_createStorePwd()throws  Exception {
        String Data = "{\"test\":\"json"+UtilsClass.Random(4)+"\"}";
        Map<String,Object> map=new HashMap<>();
        map.put("pubKeys",PUBKEY1);
        map.put("pubkeys",PUBKEY6);

        long start = System.currentTimeMillis();
        String response1= store.CreatePrivateStore(Data,map);
        long end = System.currentTimeMillis();

        log.info("时间差：" + (end - start));

        assertTrue((end - start) >= onChainTime);
        assertThat(response1, containsString("200"));

    }


    /**
     * 隐私存证授权,传私钥
     *
     */
    @Test
    public void TC003_CreatePrivateStore()throws  Exception{

        JSONObject result = new JSONObject();
        result.put("test1", true);
        result.put("test2", "30");
        String Data = result.toString();
        log.info(Data);

        Map<String,Object>map=new HashMap<>();
        map.put("pubKeys",PUBKEY1);
        String response= store.CreatePrivateStore(Data,map);

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SHORTMEOUT);

        JSONObject jsonObject=JSONObject.fromObject(response);
        String hash = jsonObject.getString("data");
        String res3 = store.GetStorePost(hash,PRIKEY1);
        assertThat(res3,containsString("200"));
        JSONObject jsonResult=JSONObject.fromObject(res3);
        assertThat(jsonResult.get("data").toString(),containsString(Data));


        map=new HashMap<>();
        map.put("pubKeys",PUBKEY2);

        long start = System.currentTimeMillis();
        String res1 = store.StoreAuthorize(hash, map, PRIKEY1,"");
        long end = System.currentTimeMillis();

        log.info("时间差：" + (end - start));

        assertTrue((end - start) >= onChainTime);
        assertThat(res1,containsString("200"));
        assertThat(res1,containsString("success"));

        commonFunc.sdkCheckTxOrSleep(commonFunc.getTxHash(globalResponse,utilsClass.sdkGetTxHashType01),
                utilsClass.sdkGetTxDetailType,SHORTMEOUT);

        String res4 = store.GetStorePost(hash,PRIKEY2);
        assertThat(res4,containsString("200"));
        jsonResult=JSONObject.fromObject(res4);
        assertThat(jsonResult.get("data").toString(),containsString(Data));


    }

}
