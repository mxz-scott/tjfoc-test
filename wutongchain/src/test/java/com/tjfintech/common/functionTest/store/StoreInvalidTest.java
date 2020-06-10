package com.tjfintech.common.functionTest.store;
import com.tjfintech.common.BeforeCondition;
import com.tjfintech.common.Interface.Store;
import com.tjfintech.common.TestBuilder;
import com.tjfintech.common.utils.UtilsClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

//import static com.tjfintech.common.functionTest.store.StoreTest.SLEEPTIME;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static com.tjfintech.common.utils.UtilsClass.*;

@Slf4j
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StoreInvalidTest {
    TestBuilder testBuilder= TestBuilder.getInstance();
    Store store = testBuilder.getStore();
    public String hash;
    public String privacyhash;

    @BeforeClass
    public static void beforeConfig() throws Exception {
        BeforeCondition bf = new BeforeCondition();
        bf.updatePubPriKey();
        Thread.sleep(SLEEPTIME/2);
    }
    /**
     * 创建存证交易
     */
    @Before
    public void TC1380_store() throws InterruptedException {
        String createstore;
        createstore = store.CreateStore("创建普通存证");
        hash = JSONObject.fromObject(createstore).getString("data");
        log.info("获取普通存证交易hash: "+hash);

        Map<String,Object> map=new HashMap<>();
        map.put("pubKeys",PUBKEY6);
        createstore = store.CreatePrivateStore("创建隐私存证",map);
        privacyhash = JSONObject.fromObject(createstore).getString("data");
        log.info("获取隐私存证交易hash: "+privacyhash);

        Thread.sleep(SLEEPTIME);
    }

    /**
     * 获取交易详情
     */
    @Test
    public void TC1370_gettxdetail(){

        String gettxdetail;

        log.info("通过hash获取交易详情: " + hash);
        gettxdetail = store.GetTxDetail(hash);
        assertThat(gettxdetail,containsString("200"));

        gettxdetail = store.GetTxDetail("");//交易id字段为空值
        assertThat(gettxdetail,containsString("404 page not found"));

        gettxdetail = store.GetTxDetail("04cf125c3d488ceb9baa6aa68973896d7de18658c75f4a2c5309cdea4e4dfb54");//交易id不存在
        assertThat(gettxdetail,containsString("404"));
        assertThat(gettxdetail,containsString("BlockchainGetTransaction: failed to find transaction"));

        gettxdetail = store.GetTxDetail("123456");//交易id为非法字符
        assertThat(gettxdetail,containsString("400"));
        assertThat(gettxdetail,containsString("Invalid parameter"));

    }


    /**
     *查询存证交易
     */
    @Test
    public void TC1382_getStore(){
        String getStore;
        getStore = store.GetStore(hash);
        assertThat(getStore,containsString("200"));

        getStore = store.GetStore("04cf125c3d488ceb9baa6aa68973896d7de18658c75f4a2c5309cdea4e4dfb54");//交易id不存在
        assertThat(getStore,containsString("404"));
        assertThat(getStore,containsString("BlockchainGetTransaction: failed to find transaction"));

        getStore = store.GetStore("12345");//交易id为非法字符
        assertThat(getStore,containsString("400"));
        assertThat(getStore,containsString("Invalid parameter"));

        getStore = store.GetStore("");//hash字段为空值
        assertThat(getStore,containsString("404 page not found"));
    }

    /**
     * 获取隐私存证
     */
    @Test
    public void TC1396_getStorePost(){
        String GetStorePostPwd;
        GetStorePostPwd = store.GetStorePostPwd(privacyhash, UtilsClass.PRIKEY6, "111");
        assertThat(GetStorePostPwd,containsString("200"));

        GetStorePostPwd = store.GetStorePostPwd("", UtilsClass.PRIKEY6, "111");//Hash字段为空值
        assertThat(GetStorePostPwd,containsString("400"));
        assertThat(GetStorePostPwd,containsString("TxId is mandatory"));

        GetStorePostPwd = store.GetStorePostPwd("111", UtilsClass.PRIKEY6, "111");//Hash字段为非法字符
        assertThat(GetStorePostPwd,containsString("400"));
        assertThat(GetStorePostPwd,containsString("Invalid parameter"));

        GetStorePostPwd = store.GetStorePostPwd(privacyhash, "", "111");//Prikey字段为空值
        assertThat(GetStorePostPwd,containsString("400"));
        assertThat(GetStorePostPwd,containsString("PriKey is mandatory"));

        GetStorePostPwd = store.GetStorePostPwd(privacyhash, "111", "111");//Prikey字段为非base64格式的字符
        assertThat(GetStorePostPwd,containsString("400"));
        assertThat(GetStorePostPwd,containsString("illegal base64 data at input byte 0"));

        GetStorePostPwd = store.GetStorePostPwd(privacyhash, "YWJjeHg=", "111");//Prikey字段为无效的base64格式的字符
        assertThat(GetStorePostPwd,containsString("400"));
        assertThat(GetStorePostPwd,containsString("Invalid private key or the password is not match the private key"));

        GetStorePostPwd = store.GetStorePostPwd(privacyhash,  UtilsClass.PRIKEY6, "");//KeyPwd字段为空
        assertThat(GetStorePostPwd,containsString("400"));
        assertThat(GetStorePostPwd,containsString("Invalid private key or the password is not match the private key"));

        GetStorePostPwd = store.GetStorePostPwd(privacyhash,  UtilsClass.PRIKEY6, "22333");//KeyPwd字段与私钥不匹配
        assertThat(GetStorePostPwd,containsString("400"));
        assertThat(GetStorePostPwd,containsString("Invalid private key or the password is not match the private key"));
    }

    /**
     * 获取交易索引
     */
    @Test
    public void TC1403_gettransactionindex(){
        String gettransactionindex;
        gettransactionindex = store.GetTransactionIndex(privacyhash);
        assertThat(gettransactionindex,containsString("200"));

        gettransactionindex = store.GetTransactionIndex("04cf125c3d488ceb9baa6aa68973896d7de18658c75f4a2c5309cdea4e4dfb54");//交易id不存在
        assertThat(gettransactionindex,containsString("404"));
        assertThat(gettransactionindex,containsString("Failed to find transaction"));

        gettransactionindex = store.GetTransactionIndex("12345");
        assertThat(gettransactionindex,containsString("400"));
        assertThat(gettransactionindex,containsString("Invalid parameter"));

//        gettransactionindex = store.GetTransactionIndex("");//hash字段为空值
//        assertThat(gettransactionindex,containsString("404 page not found"));
    }



    /**
     * 按高度获取区块时
     */
    @Test
    public void TC1407_getblockbyheight(){
        String getblockbyheight;
        getblockbyheight = store.GetBlockByHeight(0);
        assertThat(getblockbyheight,containsString("200"));

        getblockbyheight = store.GetBlockByHeight(4);
        assertThat(getblockbyheight,containsString("200"));

        getblockbyheight = store.GetBlockByHeight(-12);
        assertThat(getblockbyheight,containsString("400"));
        assertThat(getblockbyheight,containsString("Invalid Parameter"));
    }


    /**
     * Tc16 发送存证交易，data为空字符串
     * 预期返回400，提示空data
     */
    @Test
    public void TC16_CreateStoreNull(){
        String data = "";
        String  response = store.CreateStore(data);
        String message= JSONObject.fromObject(response).getString("message");
        assertThat(message,equalTo("Data is mandatory"));
        assertEquals(response.contains("400"),true);
    }

}
