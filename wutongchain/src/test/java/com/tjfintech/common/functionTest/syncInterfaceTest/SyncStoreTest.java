package com.tjfintech.common.functionTest.syncInterfaceTest;

import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import net.sf.json.JSONObject;
import org.junit.Test;

import static com.tjfintech.common.utils.UtilsClass.PUBKEY1;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

public class SyncStoreTest {
    TestBuilder testBuilder= TestBuilder.getInstance();
    UtilsClass utilsClass = new UtilsClass();
    Store store =testBuilder.getStore();
    public   final static int   SLEEPTIME=8*1000;
    public   final static int   SHORTSLEEPTIME=3*1000;
    /**
     * TC05-创建存证交易，数据格式为Json
     * 创建后需要休眠5秒等待数据上链
     * 预期：返回200，Data为交易哈希
     * 将返回的交易hash存入全局变量storeHash中用于查询测试
     */
    @Test
    public void TC05_createStore() throws Exception {

        String Data = "\"test\":\"json"+UtilsClass.Random(4)+"\"";
        String response= store.SynCreateStore(utilsClass.LONGTIMEOUT,Data);
        JSONObject jsonObject=JSONObject.fromObject(response);
        String storeHash = jsonObject.getJSONObject("Data").get("Figure").toString();
        Thread.sleep(SLEEPTIME);
        assertThat(response, containsString("200"));
        assertThat(response,containsString("Data"));

    }
}
